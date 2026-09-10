/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiinout.repository;

import com.soprasteria.g4it.backend.apiindicator.model.ApplicationCriteriaFilterBO;
import com.soprasteria.g4it.backend.apiindicator.model.GraphLevel;
import com.soprasteria.g4it.backend.apiindicator.model.RepartitionType;
import com.soprasteria.g4it.backend.apiinout.modeldb.OutApplication;
import com.soprasteria.g4it.backend.apiinout.repository.projection.ApplicationDomainHierarchyProjection;
import com.soprasteria.g4it.backend.apiinout.repository.projection.ApplicationFiltersProjection;
import com.soprasteria.g4it.backend.apiinout.repository.projection.HierarchyCountsProjection;
import com.soprasteria.g4it.backend.apiinout.repository.projection.MultiCriteriaAggregateProjection;
import com.soprasteria.g4it.backend.apiinout.repository.projection.MultiCriteriaImpactProjection;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Implementation of {@link OutApplicationCustomRepository}. Every method builds a
 * single native SQL statement, executed once against PostgreSQL, doing all
 * filtering (WHERE) and aggregation (GROUP BY / SUM / COUNT / COUNT(*) FILTER)
 * in the database. Java code only maps the small, already-aggregated result set
 * into projection POJOs - no in-memory grouping/summing (solution.md §6).
 * <p>
 * Column selection for {@code graphLevel}/{@code repartition} is resolved from a
 * fixed, internal whitelist (never from raw user input), so there is no SQL
 * injection risk despite the query being built dynamically.
 */
@Repository
public class OutApplicationCustomRepositoryImpl implements OutApplicationCustomRepository {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Maps a repartition axis to its real SQL column.
     */
    private static String repartitionColumn(final RepartitionType repartition) {
        return switch (repartition) {
            case LIFE_CYCLE -> "lifecycle_step";
            case ENVIRONMENT -> "environment";
            case EQUIPMENT_TYPE -> "equipment_type";
        };
    }

    /**
     * Maps a graph level to its SQL node-label expression. {@code null} for
     * {@link GraphLevel#GLOBAL} (no hierarchy grouping).
     */
    private static String nodeLabelExpression(final GraphLevel graphLevel) {
        return switch (graphLevel) {
            case GLOBAL -> null;
            case DOMAIN -> "filters[1]";
            case SUB_DOMAIN -> "filters[2]";
            case APPLICATION -> "name";
            case VIRTUAL_EQUIPMENT -> "virtual_equipment_name";
        };
    }

    @Override
    public List<MultiCriteriaImpactProjection> aggregateMultiCriteriaImpacts(final Long taskId,
                                                                              final ApplicationCriteriaFilterBO filters) {
        final StringBuilder sql = new StringBuilder("""
                SELECT criterion, unit,
                       SUM(unit_impact)      AS impact,
                       SUM(people_eq_impact) AS sip,
                       COUNT(*)              AS count_value
                FROM out_application
                WHERE task_id = :taskId
                """);
        final List<Object[]> bindings = new ArrayList<>();
        appendFilterPredicates(sql, bindings, filters);
        sql.append(" GROUP BY criterion, unit");

        final Query query = entityManager.createNativeQuery(sql.toString());
        query.setParameter("taskId", taskId);
        bindParameters(query, bindings);

        final List<Object[]> rows = query.getResultList();
        final List<MultiCriteriaImpactProjection> result = new ArrayList<>(rows.size());
        for (final Object[] row : rows) {
            result.add(new MultiCriteriaImpactProjectionImpl(
                    (String) row[0],
                    (String) row[1],
                    toDouble(row[2]),
                    toDouble(row[3]),
                    toLong(row[4])
            ));
        }
        return result;
    }

    @Override
    public List<MultiCriteriaAggregateProjection> aggregateMultiCriteria(final Long taskId,
                                                                          final List<String> criteria,
                                                                          final GraphLevel graphLevel,
                                                                          final RepartitionType repartition,
                                                                          final ApplicationCriteriaFilterBO filters) {
        final String nodeLabel = nodeLabelExpression(graphLevel);
        final String repartitionLabel = repartitionColumn(repartition);
        final boolean hasNode = nodeLabel != null;

        final StringBuilder select = new StringBuilder("SELECT criterion, unit, ");
        select.append(hasNode ? nodeLabel + " AS node_label, " : "NULL AS node_label, ");
        select.append(repartitionLabel).append(" AS repartition_label, ")
                .append("SUM(unit_impact) AS impact, ")
                .append("SUM(people_eq_impact) AS sip, ")
                .append("COUNT(*) AS count_value, ")
                .append("COUNT(*) FILTER (WHERE status_indicator = 'OK') AS consistent_count, ")
                .append("COUNT(*) FILTER (WHERE status_indicator = 'ERREUR') AS inconsistent_count");

        // level-specific extra aggregates - only what that level needs (solution.md §4.3 table)
        final boolean needsSubDomainCount = graphLevel == GraphLevel.DOMAIN;
        final boolean needsApplicationCount = graphLevel == GraphLevel.DOMAIN || graphLevel == GraphLevel.SUB_DOMAIN;
        final boolean needsVmAttributes = graphLevel == GraphLevel.VIRTUAL_EQUIPMENT;

        if (needsSubDomainCount) {
            select.append(", COUNT(DISTINCT filters[2]) AS sub_domain_count");
        }
        if (needsApplicationCount) {
            select.append(", COUNT(DISTINCT name) AS application_count");
        }
        if (needsVmAttributes) {
            select.append(", MIN(filters_virtual_equipment[1]) AS cluster")
                    .append(", MIN(equipment_type) AS equipment_type_attr")
                    .append(", MIN(environment) AS environment_attr");
        }

        final StringBuilder sql = new StringBuilder(select)
                .append(" FROM out_application WHERE task_id = :taskId AND criterion IN (:criteria)");

        final List<Object[]> bindings = new ArrayList<>();

        // click-path / hierarchy scoping filters, applied on top of the shared filter dimensions
        if (graphLevel == GraphLevel.SUB_DOMAIN || graphLevel == GraphLevel.APPLICATION
                || graphLevel == GraphLevel.VIRTUAL_EQUIPMENT) {
            appendInPredicate(sql, bindings, "filters[1]", filters == null ? null : filters.getDomain());
        }
        if (graphLevel == GraphLevel.APPLICATION || graphLevel == GraphLevel.VIRTUAL_EQUIPMENT) {
            appendInPredicate(sql, bindings, "filters[2]", filters == null ? null : filters.getSubDomain());
        }
        if (graphLevel == GraphLevel.VIRTUAL_EQUIPMENT) {
            appendInPredicate(sql, bindings, "name", filters == null ? null : filters.getApplicationName());
        }
        appendFilterPredicates(sql, bindings, filters);

        sql.append(" GROUP BY criterion, unit, ");
        if (hasNode) {
            sql.append(nodeLabel).append(", ");
        }
        sql.append(repartitionLabel);

        final Query query = entityManager.createNativeQuery(sql.toString());
        query.setParameter("taskId", taskId);
        query.setParameter("criteria", criteria);
        bindParameters(query, bindings);

        final List<Object[]> rows = query.getResultList();
        final List<MultiCriteriaAggregateProjection> result = new ArrayList<>(rows.size());
        for (final Object[] row : rows) {
            int i = 0;
            final String rCriterion = (String) row[i++];
            final String rUnit = (String) row[i++];
            final String rNodeLabel = (String) row[i++];
            final String rRepartitionLabel = (String) row[i++];
            final Double rImpact = toDouble(row[i++]);
            final Double rSip = toDouble(row[i++]);
            final Long rCountValue = toLong(row[i++]);
            final Long rConsistent = toLong(row[i++]);
            final Long rInconsistent = toLong(row[i++]);
            final Long rSubDomainCount = needsSubDomainCount ? toLong(row[i++]) : null;
            final Long rApplicationCount = needsApplicationCount ? toLong(row[i++]) : null;
            final String rCluster = needsVmAttributes ? (String) row[i++] : null;
            final String rEquipmentType = needsVmAttributes ? (String) row[i++] : null;
            final String rEnvironment = needsVmAttributes ? (String) row[i++] : null;

            result.add(new MultiCriteriaAggregateProjectionImpl(
                    rCriterion, rUnit, rNodeLabel, rRepartitionLabel, rImpact, rSip, rCountValue,
                    rConsistent, rInconsistent, rSubDomainCount, rApplicationCount,
                    rCluster, rEquipmentType, rEnvironment
            ));
        }
        return result;
    }

    @Override
    public HierarchyCountsProjection countHierarchy(final Long taskId, final ApplicationCriteriaFilterBO filters) {
        final StringBuilder sql = new StringBuilder("""
                SELECT COUNT(DISTINCT filters[1])         AS domain_count,
                       COUNT(DISTINCT filters[2])         AS sub_domain_count,
                       COUNT(DISTINCT name)                AS application_count,
                       COUNT(DISTINCT virtual_equipment_name) AS virtual_equipment_count
                FROM out_application
                WHERE task_id = :taskId
                """);
        final List<Object[]> bindings = new ArrayList<>();
        appendFilterPredicates(sql, bindings, filters);

        final Query query = entityManager.createNativeQuery(sql.toString());
        query.setParameter("taskId", taskId);
        bindParameters(query, bindings);

        final Object[] row = (Object[]) query.getSingleResult();
        return new HierarchyCountsProjectionImpl(toLong(row[0]), toLong(row[1]), toLong(row[2]), toLong(row[3]));
    }

    @Override
    public ApplicationFiltersProjection getDistinctFilters(final Long taskId) {
        final Query scalarsQuery = entityManager.createNativeQuery("""
                SELECT
                    array_agg(DISTINCT environment)    FILTER (WHERE environment    IS NOT NULL) AS environments,
                    array_agg(DISTINCT equipment_type) FILTER (WHERE equipment_type IS NOT NULL) AS equipment_types,
                    array_agg(DISTINCT lifecycle_step) FILTER (WHERE lifecycle_step IS NOT NULL) AS lifecycle_steps
                FROM out_application
                WHERE task_id = :taskId
                """);
        scalarsQuery.setParameter("taskId", taskId);
        final Object[] scalarRow = (Object[]) scalarsQuery.getSingleResult();

        // domain -> subDomains tree, built entirely DB-side via GROUP BY + array_agg
        // so subDomains stay correctly linked to their parent domain.
        final Query domainsQuery = entityManager.createNativeQuery("""
                SELECT filters[1] AS domain,
                       array_agg(DISTINCT filters[2]) FILTER (WHERE filters[2] IS NOT NULL) AS sub_domains
                FROM out_application
                WHERE task_id = :taskId AND filters[1] IS NOT NULL
                GROUP BY filters[1]
                ORDER BY filters[1]
                """);
        domainsQuery.setParameter("taskId", taskId);

        final List<Object[]> domainRows = domainsQuery.getResultList();
        final List<ApplicationDomainHierarchyProjection> domains = new ArrayList<>(domainRows.size());
        for (final Object[] domainRow : domainRows) {
            domains.add(new ApplicationDomainHierarchyProjectionImpl((String) domainRow[0], toStringList(domainRow[1])));
        }

        return new ApplicationFiltersProjectionImpl(
                toStringList(scalarRow[0]),
                toStringList(scalarRow[1]),
                toStringList(scalarRow[2]),
                domains
        );
    }

    @Override
    public Page<OutApplication> findByTaskId(final Long taskId, final ApplicationCriteriaFilterBO filters,
                                              final Pageable pageable) {
        final StringBuilder dataSql = new StringBuilder("SELECT * FROM out_application WHERE task_id = :taskId");
        final List<Object[]> dataBindings = new ArrayList<>();
        appendFilterPredicates(dataSql, dataBindings, filters);
        dataSql.append(" ORDER BY id");

        final StringBuilder countSql = new StringBuilder("SELECT COUNT(*) FROM out_application WHERE task_id = :taskId");
        final List<Object[]> countBindings = new ArrayList<>();
        appendFilterPredicates(countSql, countBindings, filters);

        final Query dataQuery = entityManager.createNativeQuery(dataSql.toString(), OutApplication.class);
        dataQuery.setParameter("taskId", taskId);
        bindParameters(dataQuery, dataBindings);
        dataQuery.setFirstResult((int) pageable.getOffset());
        dataQuery.setMaxResults(pageable.getPageSize());

        final Query countQuery = entityManager.createNativeQuery(countSql.toString());
        countQuery.setParameter("taskId", taskId);
        bindParameters(countQuery, countBindings);

        @SuppressWarnings("unchecked")
        final List<OutApplication> content = dataQuery.getResultList();
        final long total = ((Number) countQuery.getSingleResult()).longValue();

        return new PageImpl<>(content, pageable, total);
    }

    /**
     * Appends the shared filter dimensions (environment / equipmentType / lifeCycle
     * / domain / subDomain) as {@code AND (col = ANY(:param))} predicates, skipping
     * any dimension that is null/empty.
     */
    private static void appendFilterPredicates(final StringBuilder sql,
                                                final List<Object[]> bindings,
                                                final ApplicationCriteriaFilterBO filters) {        if (filters == null) {
            return;
        }
        appendInPredicate(sql, bindings, "environment", filters.getEnvironment());
        appendInPredicate(sql, bindings, "equipment_type", filters.getEquipmentType());
        appendInPredicate(sql, bindings, "lifecycle_step", filters.getLifeCycle());
        appendInPredicate(sql, bindings, "filters[1]", filters.getDomain());
        appendInPredicate(sql, bindings, "filters[2]", filters.getSubDomain());
    }

    private static void appendInPredicate(final StringBuilder sql,
                                           final List<Object[]> bindings,
                                           final String column,
                                           final List<String> values) {
        if (values == null || values.isEmpty()) {
            return;
        }
        final String paramName = "p" + bindings.size();
        sql.append(" AND ").append(column).append(" = ANY(:").append(paramName).append(")");
        bindings.add(new Object[]{paramName, values});
    }

    private static void bindParameters(final Query query, final List<Object[]> bindings) {
        for (final Object[] binding : bindings) {
            query.setParameter((String) binding[0], binding[1]);
        }
    }

    private static Double toDouble(final Object value) {
        return value == null ? 0d : ((Number) value).doubleValue();
    }

    private static Long toLong(final Object value) {
        return value == null ? 0L : ((Number) value).longValue();
    }

    /**
     * Converts a native array result (JDBC {@link java.sql.Array}, {@code String[]}
     * or {@code List}, depending on driver/config) into a plain {@code List<String>}.
     */
    @SuppressWarnings("unchecked")
    private static List<String> toStringList(final Object value) {
        if (value == null) {
            return List.of();
        }
        try {
            if (value instanceof java.sql.Array sqlArray) {
                final Object[] array = (Object[]) sqlArray.getArray();
                final List<String> result = new ArrayList<>(array.length);
                for (final Object o : array) {
                    if (o != null) {
                        result.add(o.toString());
                    }
                }
                return result;
            }
            if (value instanceof List<?> list) {
                return (List<String>) list;
            }
            if (value instanceof String[] array) {
                return List.of(array);
            }
        } catch (final java.sql.SQLException e) {
            throw new IllegalStateException("Unable to read distinct filter values array", e);
        }
        return List.of();
    }

    /**
     * Simple POJO implementation of {@link MultiCriteriaImpactProjection}.
     */
    private record MultiCriteriaImpactProjectionImpl(String criterion, String unit, Double impact,
                                                       Double sip,
                                                       Long countValue) implements MultiCriteriaImpactProjection {
        @Override
        public String getCriterion() {
            return criterion;
        }

        @Override
        public String getUnit() {
            return unit;
        }

        @Override
        public Double getImpact() {
            return impact;
        }

        @Override
        public Double getSip() {
            return sip;
        }

        @Override
        public Long getCountValue() {
            return countValue;
        }
    }

    /**
     * Simple POJO implementation of {@link MultiCriteriaAggregateProjection}.
     */
    private record MultiCriteriaAggregateProjectionImpl(String criterion, String unit, String nodeLabel,
                                                          String repartitionLabel, Double impact, Double sip,
                                                          Long countValue, Long consistentCount,
                                                          Long inconsistentCount, Long subDomainCount,
                                                          Long applicationCount, String cluster,
                                                          String equipmentType,
                                                          String environment) implements MultiCriteriaAggregateProjection {
        @Override
        public String getCriterion() {
            return criterion;
        }

        @Override
        public String getUnit() {
            return unit;
        }

        @Override
        public String getNodeLabel() {
            return nodeLabel;
        }

        @Override
        public String getRepartitionLabel() {
            return repartitionLabel;
        }

        @Override
        public Double getImpact() {
            return impact;
        }

        @Override
        public Double getSip() {
            return sip;
        }

        @Override
        public Long getCountValue() {
            return countValue;
        }

        @Override
        public Long getConsistentCount() {
            return consistentCount;
        }

        @Override
        public Long getInconsistentCount() {
            return inconsistentCount;
        }

        @Override
        public Long getSubDomainCount() {
            return subDomainCount;
        }

        @Override
        public Long getApplicationCount() {
            return applicationCount;
        }

        @Override
        public String getCluster() {
            return cluster;
        }

        @Override
        public String getEquipmentType() {
            return equipmentType;
        }

        @Override
        public String getEnvironment() {
            return environment;
        }
    }

    /**
     * Simple POJO implementation of {@link HierarchyCountsProjection}.
     */
    private record HierarchyCountsProjectionImpl(Long domainCount, Long subDomainCount, Long applicationCount,
                                                  Long virtualEquipmentCount) implements HierarchyCountsProjection {
        @Override
        public Long getDomainCount() {
            return domainCount;
        }

        @Override
        public Long getSubDomainCount() {
            return subDomainCount;
        }

        @Override
        public Long getApplicationCount() {
            return applicationCount;
        }

        @Override
        public Long getVirtualEquipmentCount() {
            return virtualEquipmentCount;
        }
    }

    /**
     * Simple POJO implementation of {@link ApplicationDomainHierarchyProjection}.
     */
    private record ApplicationDomainHierarchyProjectionImpl(String domain,
                                                             List<String> subDomains) implements ApplicationDomainHierarchyProjection {
        @Override
        public String getDomain() {
            return domain;
        }

        @Override
        public List<String> getSubDomains() {
            return subDomains;
        }
    }

    /**
     * Simple POJO implementation of {@link ApplicationFiltersProjection}.
     */
    private record ApplicationFiltersProjectionImpl(List<String> environment, List<String> equipmentType,
                                                      List<String> lifeCycle,
                                                      List<ApplicationDomainHierarchyProjection> domains) implements ApplicationFiltersProjection {
        @Override
        public List<String> getEnvironment() {
            return environment;
        }

        @Override
        public List<String> getEquipmentType() {
            return equipmentType;
        }

        @Override
        public List<String> getLifeCycle() {
            return lifeCycle;
        }

        @Override
        public List<ApplicationDomainHierarchyProjection> getDomains() {
            return domains;
        }
    }
}




