/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apidigitalservice.business;

import com.soprasteria.g4it.backend.TestUtils;
import com.soprasteria.g4it.backend.apidigitalservice.mapper.DatacenterDigitalServiceMapper;
import com.soprasteria.g4it.backend.apidigitalservice.mapper.DigitalServiceMapper;
import com.soprasteria.g4it.backend.apidigitalservice.model.*;
import com.soprasteria.g4it.backend.apidigitalservice.modeldb.*;
import com.soprasteria.g4it.backend.apidigitalservice.modeldb.referential.NetworkTypeRef;
import com.soprasteria.g4it.backend.apidigitalservice.modeldb.referential.ServerHostRef;
import com.soprasteria.g4it.backend.apidigitalservice.repository.DatacenterDigitalServiceRepository;
import com.soprasteria.g4it.backend.apidigitalservice.repository.DigitalServiceLinkRepository;
import com.soprasteria.g4it.backend.apidigitalservice.repository.DigitalServiceRepository;
import com.soprasteria.g4it.backend.apidigitalservice.repository.DigitalServiceSharedRepository;
import com.soprasteria.g4it.backend.apiindicator.business.IndicatorService;
import com.soprasteria.g4it.backend.apiuser.business.OrganizationService;
import com.soprasteria.g4it.backend.apiuser.model.UserBO;
import com.soprasteria.g4it.backend.apiuser.modeldb.Organization;
import com.soprasteria.g4it.backend.apiuser.modeldb.Subscriber;
import com.soprasteria.g4it.backend.apiuser.modeldb.User;
import com.soprasteria.g4it.backend.apiuser.repository.SubscriberRepository;
import com.soprasteria.g4it.backend.apiuser.repository.UserRepository;
import com.soprasteria.g4it.backend.client.gen.connector.apiexposition.dto.ModeRest;
import com.soprasteria.g4it.backend.common.criteria.CriteriaByType;
import com.soprasteria.g4it.backend.common.criteria.CriteriaService;
import com.soprasteria.g4it.backend.common.filesystem.model.FileMapperInfo;
import com.soprasteria.g4it.backend.common.filesystem.model.FileType;
import com.soprasteria.g4it.backend.common.filesystem.model.Header;
import com.soprasteria.g4it.backend.exception.G4itRestException;
import com.soprasteria.g4it.backend.exception.InvalidReferentialException;
import com.soprasteria.g4it.backend.external.numecoeval.business.NumEcoEvalRemotingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.FileSystemResource;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DigitalServiceServiceTest {

    final static Long ORGANIZATION_ID = 1L;
    final static String DIGITAL_SERVICE_UID = "80651485-3f8b-49dd-a7be-753e4fe1fd36";
    final static String SUBSCRIBER = "subscriber";
    final static long User_ID = 1;

    final static List<String> criteriaList = List.of("ionising-radiation", "climate-change");

    @Mock
    private DigitalServiceRepository digitalServiceRepository;
    @Mock
    private OrganizationService organizationService;
    @Mock
    private CriteriaService criteriaService;
    @Mock
    private DatacenterDigitalServiceRepository datacenterDigitalServiceRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private SubscriberRepository subscriberRepository;
    @Mock
    private DigitalServiceLinkRepository digitalServiceLinkRepository;
    @Mock
    private DigitalServiceSharedRepository digitalServiceSharedRepository;
    @Mock
    private DatacenterDigitalServiceMapper datacenterDigitalServiceMapper;
    @Mock
    private DigitalServiceMapper digitalServiceMapper;
    @Mock
    private DigitalServiceReferentialService digitalServiceReferentialService;
    @Mock
    private IndicatorService indicatorService;
    @Mock
    private FileMapperInfo fileInfo;
    @Mock
    private NumEcoEvalRemotingService numEcoEvalRemotingService;
    @InjectMocks
    private DigitalServiceService digitalServiceService;

    @Test
    void shouldCreateNewDigitalService_first() {

        final String organizationName = "test";
        final Organization linkedOrganization = Organization.builder().name(organizationName).build();
        final User user = User.builder().id(User_ID).build();
        final DigitalServiceBO expectedBo = DigitalServiceBO.builder().build();
        final String expectedName = "Digital Service 1";
        final List<DigitalService> existingDigitalService = new ArrayList<>();

        final DigitalService digitalServiceToSave = DigitalService.builder().organization(linkedOrganization).user(user).name(expectedName).build();
        when(digitalServiceRepository.findByOrganizationAndUserId(linkedOrganization, User_ID)).thenReturn(existingDigitalService);
        when(organizationService.getOrganizationById(ORGANIZATION_ID)).thenReturn(linkedOrganization);
        when(digitalServiceRepository.save(any())).thenReturn(digitalServiceToSave);
        when(digitalServiceMapper.toBusinessObject(digitalServiceToSave)).thenReturn(expectedBo);
        when(userRepository.findById(User_ID)).thenReturn(Optional.of(user));

        final DigitalServiceBO result = digitalServiceService.createDigitalService(ORGANIZATION_ID, User_ID);

        assertThat(result).isEqualTo(expectedBo);

        verify(organizationService, times(1)).getOrganizationById(ORGANIZATION_ID);
        verify(digitalServiceRepository, times(1)).findByOrganizationAndUserId(linkedOrganization, User_ID);
        verify(digitalServiceRepository, times(1)).save(any());
        verify(digitalServiceMapper, times(1)).toBusinessObject(digitalServiceToSave);
        verify(userRepository, times(1)).findById(User_ID);
    }

    @Test
    void shouldCreateNewDigitalService_withExistingDigitalService() {
        final String organizationName = "test";
        final User user = User.builder().id(User_ID).build();
        final Organization linkedOrganization = Organization.builder().name(organizationName).build();
        final DigitalServiceBO expectedBo = DigitalServiceBO.builder().build();
        final String expectedName = "Digital Service 2";
        final List<DigitalService> existingDigitalService = List.of(DigitalService.builder().name("Digital Service 1").build(), DigitalService.builder().name("My Digital Service").build());

        final DigitalService digitalServiceToSave = DigitalService.builder().organization(linkedOrganization).user(user).name(expectedName).build();
        when(digitalServiceRepository.findByOrganizationAndUserId(linkedOrganization, User_ID)).thenReturn(existingDigitalService);
        when(organizationService.getOrganizationById(ORGANIZATION_ID)).thenReturn(linkedOrganization);
        when(digitalServiceRepository.save(any())).thenReturn(digitalServiceToSave);
        when(digitalServiceMapper.toBusinessObject(digitalServiceToSave)).thenReturn(expectedBo);
        when(userRepository.findById(User_ID)).thenReturn(Optional.of(user));

        final DigitalServiceBO result = digitalServiceService.createDigitalService(ORGANIZATION_ID, User_ID);

        assertThat(result).isEqualTo(expectedBo);

        verify(organizationService, times(1)).getOrganizationById(ORGANIZATION_ID);
        verify(digitalServiceRepository, times(1)).findByOrganizationAndUserId(linkedOrganization, User_ID);
        verify(digitalServiceRepository, times(1)).save(any());
        verify(digitalServiceMapper, times(1)).toBusinessObject(digitalServiceToSave);
        verify(userRepository, times(1)).findById(User_ID);
    }

    @Test
    void shouldListDigitalService() {
        final String organizationName = "test";
        final Organization linkedOrganization = Organization.builder().name(organizationName).build();
        User creator = User.builder().id(1L).firstName("first").lastName("last").build();
        User member = User.builder().id(2L).firstName("test").lastName("").build();

        DigitalService digitalService = DigitalService.builder().name("name").user(creator).build();
        List<DigitalServiceShared> sharedDigitalServices = List.of(DigitalServiceShared.builder().digitalService(DigitalService.builder().name("digitalService-2").user(member).build()).build());
        final DigitalServiceBO digitalServiceBo = DigitalServiceBO.builder().uid(DIGITAL_SERVICE_UID).build();

        when(digitalServiceMapper.toBusinessObject(anyList())).thenReturn(List.of(digitalServiceBo));

        when(digitalServiceRepository.findById(DIGITAL_SERVICE_UID)).thenReturn(Optional.of(digitalService));
        when(organizationService.getOrganizationById(ORGANIZATION_ID)).thenReturn(linkedOrganization);
        when(digitalServiceRepository.findByOrganizationAndUserId(linkedOrganization, User_ID)).thenReturn(List.of(digitalService));
        when(digitalServiceSharedRepository.findByOrganizationAndUserId(linkedOrganization, User_ID)).thenReturn(sharedDigitalServices);
        when(digitalServiceSharedRepository.findByDigitalServiceUid(DIGITAL_SERVICE_UID))
                .thenReturn(List.of(DigitalServiceShared.builder().user(member).build()));

        List<DigitalServiceBO> result = digitalServiceService.getDigitalServices(ORGANIZATION_ID, User_ID);
        assertThat(result).isEqualTo(List.of(digitalServiceBo));

        verify(digitalServiceRepository, times(1)).findById(DIGITAL_SERVICE_UID);
        verify(organizationService, times(1)).getOrganizationById(ORGANIZATION_ID);
        verify(digitalServiceRepository, times(1)).findByOrganizationAndUserId(linkedOrganization, User_ID);
        verify(digitalServiceSharedRepository, times(1)).findByOrganizationAndUserId(linkedOrganization, User_ID);
        verify(digitalServiceMapper, times(1)).toBusinessObject(anyList());

    }

    @Test
    void shouldDeleteDigitalService() {

        doNothing().when(digitalServiceRepository).deleteById(DIGITAL_SERVICE_UID);
        doNothing().when(indicatorService).deleteIndicators(DIGITAL_SERVICE_UID);

        digitalServiceService.deleteDigitalService(DIGITAL_SERVICE_UID);

        verify(digitalServiceRepository, times(1)).deleteById(DIGITAL_SERVICE_UID);
        verify(indicatorService, times(1)).deleteIndicators(DIGITAL_SERVICE_UID);
    }

    @Test
    void shouldUpdateDigitalService() {
        final UserBO userBO = UserBO.builder().id(1).build();
        final User user = User.builder().id(1).build();

        final DigitalServiceBO inputDigitalServiceBO = DigitalServiceBO.builder().uid(DIGITAL_SERVICE_UID).name("name").build();
        final DigitalServiceBO digitalServiceBO = DigitalServiceBO.builder().uid(DIGITAL_SERVICE_UID).build();
        final DigitalService digitalService = DigitalService.builder().uid(DIGITAL_SERVICE_UID).build();
        final DigitalService digitalServiceUpdated = DigitalService.builder().uid(DIGITAL_SERVICE_UID).name("name").build();

        when(digitalServiceRepository.findById(digitalService.getUid())).thenReturn(Optional.of(digitalService));
        when(digitalServiceMapper.toFullBusinessObject(digitalService)).thenReturn(digitalServiceBO);
        doNothing().when(digitalServiceMapper).mergeEntity(digitalService, inputDigitalServiceBO, digitalServiceReferentialService, user);
        when(digitalServiceRepository.save(digitalService)).thenReturn(digitalServiceUpdated);
        when(digitalServiceMapper.toFullBusinessObject(digitalServiceUpdated)).thenReturn(inputDigitalServiceBO);

        final DigitalServiceBO result = digitalServiceService.updateDigitalService(inputDigitalServiceBO, userBO);

        assertThat(result).isEqualTo(inputDigitalServiceBO);
        verify(digitalServiceRepository, times(1)).findById(digitalServiceBO.getUid());
        verify(digitalServiceMapper, times(1)).toFullBusinessObject(digitalService);
        verify(digitalServiceMapper, times(1)).mergeEntity(digitalService, inputDigitalServiceBO, digitalServiceReferentialService, user);
        verify(digitalServiceRepository, times(1)).save(digitalService);
        verify(digitalServiceMapper, times(1)).toFullBusinessObject(digitalServiceUpdated);
    }

    @Test
    void shouldUpdateDigitalService_withRemovedTerminalAndNetwork() {
        final UserBO userBO = UserBO.builder().id(1).build();
        final User user = User.builder().id(1).build();

        final DigitalServiceBO inputDigitalServiceBO = DigitalServiceBO.builder().uid(DIGITAL_SERVICE_UID).name("name").build();
        final DigitalService digitalService = DigitalService.builder().uid(DIGITAL_SERVICE_UID).build();
        digitalService.addTerminal(Terminal.builder().uid("uidTerminal1").build());
        digitalService.addNetwork(Network.builder().uid("uidNetwork").build());

        final DigitalServiceBO digitalServiceBO = DigitalServiceBO.builder().uid(DIGITAL_SERVICE_UID).build();
        final DigitalService digitalServiceUpdated = DigitalService.builder().uid(DIGITAL_SERVICE_UID).name("name").build();

        when(digitalServiceRepository.findById(digitalService.getUid())).thenReturn(Optional.of(digitalService));
        when(digitalServiceMapper.toFullBusinessObject(digitalService)).thenReturn(digitalServiceBO);
        doNothing().when(digitalServiceMapper).mergeEntity(digitalService, inputDigitalServiceBO, digitalServiceReferentialService, user);
        when(digitalServiceRepository.save(digitalService)).thenReturn(digitalServiceUpdated);
        when(digitalServiceMapper.toFullBusinessObject(digitalServiceUpdated)).thenReturn(inputDigitalServiceBO);

        final DigitalServiceBO result = digitalServiceService.updateDigitalService(inputDigitalServiceBO, userBO);

        assertThat(result).isEqualTo(inputDigitalServiceBO);
        verify(digitalServiceRepository, times(1)).findById(digitalServiceBO.getUid());
        verify(digitalServiceMapper, times(1)).toFullBusinessObject(digitalService);
        verify(digitalServiceMapper, times(1)).mergeEntity(digitalService, inputDigitalServiceBO, digitalServiceReferentialService, user);
        verify(digitalServiceRepository, times(1)).save(digitalService);
        verify(digitalServiceMapper, times(1)).toFullBusinessObject(digitalServiceUpdated);
    }

    @Test
    void whenNoChange_thenDigitalServiceEntityNotChange() {

        final DigitalServiceBO digitalServiceBO = DigitalServiceBO.builder().uid(DIGITAL_SERVICE_UID).build();
        final DigitalService digitalService = DigitalService.builder().uid(DIGITAL_SERVICE_UID).build();

        when(digitalServiceRepository.findById(digitalService.getUid())).thenReturn(Optional.of(digitalService));
        when(digitalServiceMapper.toFullBusinessObject(digitalService)).thenReturn(digitalServiceBO);

        final DigitalServiceBO result = digitalServiceService.updateDigitalService(digitalServiceBO, null);

        assertThat(result).isEqualTo(digitalServiceBO);
        verify(digitalServiceRepository, times(1)).findById(digitalServiceBO.getUid());
        verify(digitalServiceMapper, times(1)).toFullBusinessObject(digitalService);
    }

    @Test
    void whenUpdateNotExistDigitalService_thenThrow() {

        when(digitalServiceRepository.findById(DIGITAL_SERVICE_UID)).thenReturn(Optional.empty());

        final DigitalServiceBO bo = DigitalServiceBO.builder().uid(DIGITAL_SERVICE_UID).build();
        assertThatThrownBy(() -> digitalServiceService.updateDigitalService(bo, null))
                .hasMessageContaining("Digital Service 80651485-3f8b-49dd-a7be-753e4fe1fd36 not found.")
                .isInstanceOf(G4itRestException.class);

        verify(digitalServiceRepository, times(1)).findById(DIGITAL_SERVICE_UID);
        verifyNoInteractions(digitalServiceMapper);
    }

    @Test
    void shouldGetDigitalService() {
        User user = User.builder().id(1L).firstName("first").lastName("last").build();

        final DigitalService digitalService = DigitalService.builder().user(user).build();
        when(digitalServiceRepository.findById(DIGITAL_SERVICE_UID)).thenReturn(Optional.of(digitalService));
        final DigitalServiceBO digitalServiceBo = DigitalServiceBO.builder().build();
        when(digitalServiceMapper.toFullBusinessObject(digitalService)).thenReturn(digitalServiceBo);

        final DigitalServiceBO result = digitalServiceService.getDigitalService(DIGITAL_SERVICE_UID);

        assertThat(result).isEqualTo(digitalServiceBo);

        verify(digitalServiceRepository, times(1)).findById(DIGITAL_SERVICE_UID);
        verify(digitalServiceMapper, times(1)).toFullBusinessObject(digitalService);
    }

    @Test
    void whenGetNotExistDigitalService_thenThrow() {

        when(digitalServiceRepository.findById(DIGITAL_SERVICE_UID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> digitalServiceService.getDigitalService(DIGITAL_SERVICE_UID))
                .hasMessageContaining("Digital Service 80651485-3f8b-49dd-a7be-753e4fe1fd36 not found.")
                .isInstanceOf(G4itRestException.class);

        verify(digitalServiceRepository, times(1)).findById(DIGITAL_SERVICE_UID);
        verifyNoInteractions(digitalServiceMapper);
    }

    @Test
    void testCalculateQuantityForTerminal() throws Exception {
        final Method method = DigitalServiceService.class.getDeclaredMethod("calculateQuantity", TerminalBO.class);
        method.setAccessible(true);

        final TerminalBO terminal = TerminalBO.builder().yearlyUsageTimePerUser(90.0).numberOfUsers(53804).lifespan(2.5).build();
        final Double result = (Double) method.invoke(digitalServiceService, terminal);
        assertThat(result).isEqualTo((Double) 552.7808219178082);
        assertThat(String.valueOf(result)).isEqualTo("552.7808219178082");
    }

    @Test
    void shouldRunCalculation() {

        final String organizationName = "test";

        doNothing().when(indicatorService).deleteIndicators(DIGITAL_SERVICE_UID);
        User user = User.builder().id(1L).firstName("first").lastName("last").build();
        final DigitalService digitalService = DigitalService.builder().uid(DIGITAL_SERVICE_UID).user(user).build();
        when(digitalServiceRepository.findById(DIGITAL_SERVICE_UID)).thenReturn(Optional.of(digitalService));
        final DigitalServiceBO digitalServiceBo = DigitalServiceBO.builder()
                .terminals(List.of(TerminalBO.builder()
                        .type(DeviceTypeBO.builder().code("code").value("value").build())
                        .country("France")
                        .uid("uid")
                        .yearlyUsageTimePerUser(15.0)
                        .lifespan(2.5)
                        .numberOfUsers(53804)
                        .build()))
                .networks(List.of(NetworkBO.builder()
                        .uid("uid")
                        .yearlyQuantityOfGbExchanged(3832.5d)
                        .type(NetworkTypeBO.builder().code("code").value("value").build())
                        .build()))
                .servers(List.of(ServerBO.builder()
                                .uid("uid")
                                .name("Server A")
                                .annualOperatingTime(8000)
                                .annualElectricConsumption(5)
                                .quantity(1)
                                .mutualizationType("Shared")
                                .type("Storage")
                                .totalDisk(30)
                                .host(ServerHostBO.builder().code(1).build())
                                .datacenter(ServerDataCenterBO.builder().uid("uid").location("France").name("DC1").pue(new BigDecimal("3.5")).build())
                                .vm(List.of(VirtualEquipmentBO.builder().disk(15).annualOperatingTime(1).quantity(1).build()))
                                .lifespan(1.5)
                                .build(),
                        ServerBO.builder()
                                .uid("uid2")
                                .name("Server B")
                                .annualOperatingTime(8000)
                                .annualElectricConsumption(5)
                                .quantity(1)
                                .mutualizationType("Dedicated")
                                .type("Compute")
                                .totalVCpu(3000)
                                .host(ServerHostBO.builder().code(1).build())
                                .datacenter(ServerDataCenterBO.builder().uid("uid").location("France").name("DC1").pue(new BigDecimal("3.5")).build())
                                .vm(List.of(VirtualEquipmentBO.builder().vCpu(3000).quantity(1).annualOperatingTime(10).build()))
                                .lifespan(1.5)
                                .build()))
                .uid(DIGITAL_SERVICE_UID)
                .build();
        when(digitalServiceMapper.toFullBusinessObject(digitalService)).thenReturn(digitalServiceBo);
        when(digitalServiceReferentialService.getServerHost(1)).thenReturn(ServerHostRef.builder().reference("Reference").build());

        when(fileInfo.getMapping(FileType.EQUIPEMENT_PHYSIQUE)).thenReturn(List.of(Header.builder().build()));
        ReflectionTestUtils.setField(digitalServiceService, "localWorkingPath", "target/");

        lenient().doNothing()
                .when(numEcoEvalRemotingService).callInputDataExposition(eq(null), any(FileSystemResource.class), eq(null), eq(null),
                        eq(organizationName), eq(""), eq(DIGITAL_SERVICE_UID));
        doNothing().when(numEcoEvalRemotingService).callCalculation(DIGITAL_SERVICE_UID, ModeRest.SYNC, criteriaList);

        when(digitalServiceReferentialService.getNetworkType("code")).thenReturn(NetworkTypeRef.builder().annualQuantityOfGo(10).build());
        when(digitalServiceRepository.save(digitalService)).thenReturn(digitalService);

        final Organization linkedOrganization = TestUtils.createOrganization();
        when(organizationService.getOrganizationById(ORGANIZATION_ID)).thenReturn(linkedOrganization);
        final Subscriber subscriberObj = TestUtils.createSubscriber();
        subscriberObj.setCriteria(criteriaList);

        when(criteriaService.getSelectedCriteriaForDigitalService(SUBSCRIBER, ORGANIZATION_ID, null)).thenReturn(new CriteriaByType(criteriaList, null, null, null, null, null));
        digitalServiceService.runCalculations(SUBSCRIBER, ORGANIZATION_ID, DIGITAL_SERVICE_UID);

        verify(indicatorService, times(1)).deleteIndicators(DIGITAL_SERVICE_UID);
        verify(digitalServiceRepository, times(2)).findById(DIGITAL_SERVICE_UID);
        verify(digitalServiceMapper, times(1)).toFullBusinessObject(digitalService);
        verify(fileInfo, times(1)).getMapping(FileType.EQUIPEMENT_PHYSIQUE);
        verify(numEcoEvalRemotingService, times(1)).callInputDataExposition(any(), any(), any(), any(), any(), any(), any());
        verify(numEcoEvalRemotingService, times(1)).callCalculation(DIGITAL_SERVICE_UID, ModeRest.SYNC, criteriaList);
        verify(digitalServiceRepository, times(1)).save(digitalService);
        verify(digitalServiceReferentialService, times(1)).getNetworkType("code");
        verify(digitalServiceReferentialService, times(2)).getServerHost(1);
    }

    @Test
    void testCalculateQuantityForNetwork_whenTypeMobile_returnYearlyQuantityOfgbExchanged() throws Exception {
        final Method method = DigitalServiceService.class.getDeclaredMethod("calculateQuantity", NetworkBO.class, Integer.class);
        method.setAccessible(true);

        final NetworkBO network = NetworkBO.builder().yearlyQuantityOfGbExchanged(1825126.1075).type(NetworkTypeBO.builder().code("mobile").build()).build();
        final Double result = (Double) method.invoke(digitalServiceService, network, 0);
        assertThat(result).isEqualTo(1825126.1075);
        assertThat(String.valueOf(result)).isEqualTo("1825126.1075");
    }

    @Test
    void testCalculateQuantityForNetwork_whenAnnualQuantityOfGoIsZero_thenThrowException() throws Exception {
        final Method method = DigitalServiceService.class.getDeclaredMethod("calculateQuantity", NetworkBO.class, Integer.class);
        method.setAccessible(true);

        final NetworkBO network = NetworkBO.builder().yearlyQuantityOfGbExchanged(3650d).type(NetworkTypeBO.builder().code("fixe").build()).build();

        assertThatThrownBy(() -> method.invoke(digitalServiceService, network, 0))
                .isInstanceOf(InvocationTargetException.class)
                .extracting("targetException").isInstanceOf(InvalidReferentialException.class)
                .extracting("referentialInErrorCode").isEqualTo("network.type.code.annualQuantityOfGo");
    }

    @Test
    void testCalculateQuantityForNetwork_thenReturnCalculationResult() throws Exception {
        final Method method = DigitalServiceService.class.getDeclaredMethod("calculateQuantity", NetworkBO.class, Integer.class);
        method.setAccessible(true);

        final NetworkBO network = NetworkBO.builder().yearlyQuantityOfGbExchanged(23725d).type(NetworkTypeBO.builder().code("fixe").build()).build();

        final Double result = (Double) method.invoke(digitalServiceService, network, 4546);

        assertThat(result).isEqualTo(5.2188737351517815);
        assertThat(String.valueOf(result)).isEqualTo("5.2188737351517815");
    }

    @Test
    void testCalculateQuantityForServerDedicated_thenReturnCalculationResult() throws Exception {
        final Method method = DigitalServiceService.class.getDeclaredMethod("calculateQuantity", ServerBO.class);
        method.setAccessible(true);

        final ServerBO server = ServerBO.builder()
                .annualOperatingTime(8000)
                .quantity(1)
                .mutualizationType("Dedicated")
                .type("Storage")
                .build();

        final Double result = (Double) method.invoke(digitalServiceService, server);

        assertThat(result).isEqualTo(0.91324200913242);
        assertThat(String.valueOf(result)).isEqualTo("0.91324200913242");
    }

    @Test
    void testCalculateQuantityForServerShared_thenReturnCalculationResult() throws Exception {
        final Method method = DigitalServiceService.class.getDeclaredMethod("calculateQuantity", ServerBO.class);
        method.setAccessible(true);

        final ServerBO server = ServerBO.builder()
                .annualOperatingTime(8000)
                .quantity(1)
                .mutualizationType("Shared")
                .type("Storage")
                .build();

        final Double result = (Double) method.invoke(digitalServiceService, server);

        assertThat(result).isEqualTo(1);
        assertThat(String.valueOf(result)).isEqualTo("1.0");
    }

    @Test
    void whenGetDatacentersDatacenter_thenReturnDefaultDatacenterAndSavedDatacenter() {

        List<DatacenterDigitalService> dcEntity = List.of(DatacenterDigitalService
                .builder().name("Saved datacenter").pue(new BigDecimal("3.5")).location("UK").build());
        when(datacenterDigitalServiceRepository.findByDigitalServiceUid(DIGITAL_SERVICE_UID)).thenReturn(dcEntity);
        when(datacenterDigitalServiceMapper.toBusinessObject(dcEntity)).thenReturn(List.of(
                ServerDataCenterBO.builder().name("Saved datacenter").pue(new BigDecimal("3.5")).location("UK").build()));

        final List<ServerDataCenterBO> result = digitalServiceService.getDigitalServiceDataCenter(DIGITAL_SERVICE_UID);

        assertThat(result).hasSize(1).satisfiesExactly(
                dc -> assertThat(dc)
                        .extracting("name", "location", "pue")
                        .contains("Saved datacenter", "UK", new BigDecimal("3.5"))
        );

        verify(datacenterDigitalServiceRepository, times(1)).findByDigitalServiceUid(DIGITAL_SERVICE_UID);
        verify(datacenterDigitalServiceMapper, times(1)).toBusinessObject(dcEntity);
    }

    @Test
    void testShareDigitalService_DigitalServiceFound() {
        String subscriber = "subscriber";
        Long organizationId = 1L;

        when(digitalServiceRepository.findById(DIGITAL_SERVICE_UID)).thenReturn(Optional.of(DigitalService.builder().uid(DIGITAL_SERVICE_UID).build()));

        DigitalServiceLink savedDigitalServiceLink = DigitalServiceLink.builder().digitalService(DigitalService.builder().uid(DIGITAL_SERVICE_UID).build()).uid("mockedUid").build();
        savedDigitalServiceLink.setUid("mockedUid");

        String expectedUrl = String.format("/subscribers/%s/organizations/%d/digital-services/%s/share/%s",
                subscriber, organizationId, DIGITAL_SERVICE_UID, "mockedUid");
        when(digitalServiceLinkRepository.save(any(DigitalServiceLink.class))).thenReturn(savedDigitalServiceLink);

        String result = digitalServiceService.shareDigitalService(subscriber, organizationId, DIGITAL_SERVICE_UID);
        verify(digitalServiceLinkRepository, times(1)).save(any());
        assertEquals(expectedUrl, result);
    }

    @Test
    void testShareDigitalService_DigitalServiceNotFound() {
        String subscriber = "subscriber";
        Long organizationId = 1L;

        when(digitalServiceRepository.findById(DIGITAL_SERVICE_UID)).thenReturn(Optional.empty());

        G4itRestException exception = assertThrows(G4itRestException.class, () ->
                digitalServiceService.shareDigitalService(subscriber, organizationId, DIGITAL_SERVICE_UID));

        verify(digitalServiceRepository, times(1)).findById(DIGITAL_SERVICE_UID);

        String expectedMessage = String.format("Digital service %s not found in %s/%d", DIGITAL_SERVICE_UID, subscriber, organizationId);
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    void testLinkDigitalServiceToUser_SharedLinkExpired() {

        String sharedUid = "57d82582-b9ae-46b9-a430-b485b5e0367e";

        final DigitalService digitalService = DigitalService.builder().uid(DIGITAL_SERVICE_UID).build();

        // if the link has expired
        when(digitalServiceLinkRepository.findById(sharedUid)).thenReturn(Optional.empty());
        when(digitalServiceRepository.findById(DIGITAL_SERVICE_UID)).thenReturn(Optional.of(digitalService));
        G4itRestException exception = assertThrows(G4itRestException.class, () ->
                digitalServiceService.linkDigitalServiceToUser(SUBSCRIBER, ORGANIZATION_ID, DIGITAL_SERVICE_UID, sharedUid, User_ID));

        verify(digitalServiceLinkRepository, times(1)).findById(sharedUid);
        verify(digitalServiceRepository, times(1)).findById(DIGITAL_SERVICE_UID);

        String expectedMessage = String.format("The shared url for Digital service %s/%s/%d has expired.", DIGITAL_SERVICE_UID, SUBSCRIBER, ORGANIZATION_ID);
        assert expectedMessage.equals(exception.getMessage());
    }

    @Test
    void testLinkDigitalServiceToUser_DigitalServiceNotFound() {

        String sharedUid = "57d82582-b9ae-46b9-a430-b485b5e0367e";

        //digital service doesn't exist
        when(digitalServiceRepository.findById(DIGITAL_SERVICE_UID))
                .thenReturn(Optional.empty());

        G4itRestException exception = assertThrows(G4itRestException.class, () ->
                digitalServiceService.linkDigitalServiceToUser(SUBSCRIBER, ORGANIZATION_ID, DIGITAL_SERVICE_UID, sharedUid, User_ID));

        verify(digitalServiceRepository, times(1)).findById(DIGITAL_SERVICE_UID);

        String expectedMessage = String.format("Digital service %s not found in %s/%d", DIGITAL_SERVICE_UID, SUBSCRIBER, ORGANIZATION_ID);
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    void shouldUnlinkSharedDigitalServiceWhenPresent() {
        DigitalServiceShared digitalServiceShared = DigitalServiceShared.builder().digitalService(DigitalService.builder().name("digitalService-2").build()).build();

        when(digitalServiceSharedRepository.findByDigitalServiceUidAndUserId(DIGITAL_SERVICE_UID, User_ID))
                .thenReturn(Optional.of(digitalServiceShared));
        digitalServiceService.unlinkSharedDigitalService(DIGITAL_SERVICE_UID, User_ID);
        verify(digitalServiceSharedRepository, times(1)).delete(digitalServiceShared);

    }

}
