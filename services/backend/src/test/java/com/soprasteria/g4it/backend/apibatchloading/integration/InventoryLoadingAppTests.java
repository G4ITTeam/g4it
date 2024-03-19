/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
package com.soprasteria.g4it.backend.apibatchloading.integration;

import com.soprasteria.g4it.backend.apibatchloading.model.CustomExitStatus;
import com.soprasteria.g4it.backend.apiinventory.modeldb.*;
import com.soprasteria.g4it.backend.apiinventory.repository.*;
import com.soprasteria.g4it.backend.apiuser.modeldb.Organization;
import com.soprasteria.g4it.backend.apiuser.modeldb.Subscriber;
import com.soprasteria.g4it.backend.apiuser.repository.OrganizationRepository;
import com.soprasteria.g4it.backend.common.filesystem.model.FileFolder;
import com.soprasteria.g4it.backend.external.numecoeval.business.NumEcoEvalReferentialRemotingService;
import org.assertj.core.data.Index;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.JobRepositoryTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.FileSystemUtils;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.soprasteria.g4it.backend.apibatchloading.model.InventoryJobParams.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SpringBootTest
@SpringBatchTest
@ActiveProfiles({"local", "test"})
@DirtiesContext
class InventoryLoadingAppTests {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    @Qualifier("loadInventoryJob")
    private Job job;

    @Autowired
    private JobRepositoryTestUtils jobRepositoryTestUtils;

    @Autowired
    private ResourceLoader resourceLoader;

    @Autowired
    private DataCenterRepository dataCenterRepository;

    @Autowired
    private PhysicalEquipmentRepository physicalEquipmentRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private VirtualEquipmentRepository virtualEquipmentRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private JobLauncher jobLauncher;

    @MockBean
    private NumEcoEvalReferentialRemotingService numEcoEvalReferentialRemotingService;

    @Value("${filesystem.local.path}")
    private String fileSystemBasePath;

    private static final Path testFolder = Path.of("src/test/resources/apibatchloading");
    private static final String localJobWorkingPath = "local_folder_1";

    private static final String subscriber = "SSG";
    private static final String organization = "G4IT";
    Path outputPath;

    private long inventoryId;

    @BeforeEach
    public void beforeEach() {
        outputPath = Path.of(fileSystemBasePath).resolve(subscriber).resolve(organization).resolve("output");

        this.jobLauncherTestUtils.setJobLauncher(jobLauncher);
        this.jobLauncherTestUtils.setJob(job);

        Optional<Organization> organization1 = organizationRepository.findAll().stream().filter(orga -> orga.getName().equals(organization)).findFirst();
        Organization orga = organization1.orElseGet(() -> organizationRepository.save(Organization.builder()
                .name(organization)
                .subscriber(Subscriber.builder().name(subscriber).build())
                .build()));

        Optional<Inventory> inventoryOptional = inventoryRepository.findByOrganizationAndName(orga, "04-2023");
        if (inventoryOptional.isEmpty()) {
            // Create Inventory
            this.inventoryRepository.deleteAll();
            inventoryId = this.inventoryRepository.save(Inventory.builder()
                    .name("04-2023")
                    .organization(orga)
                    .build()).getId();
        } else {
            inventoryId = inventoryOptional.get().getId();
        }

    }

    @AfterEach
    public void cleanUp() throws IOException {
        this.jobRepositoryTestUtils.removeJobExecutions();
        // cleanup repositories
        applicationRepository.deleteAll();
        virtualEquipmentRepository.deleteAll();
        physicalEquipmentRepository.deleteAll();
        dataCenterRepository.deleteAll();
    }

    /**
     * Cas de test complet :
     * <p>
     * Le répertoire d'entrée contient des fichiers Datacenter, Equipement Physique,
     * Equipement Virtuel et Application.
     * Sur chacun des types d'équipement, des cas passants, des problèmes de format, et des
     * problèmes de cohérence sont inclus.
     * <p>
     * Attendu :
     * 1. DataCenter
     * 1.1. Un fichier KO en sortie avec 7 lignes (6 data + 1 header).
     * 1.1.1. 2 lignes en erreur issues du fichier des champs obligatoires manquants.
     * 1.1.2. 3 lignes en erreur issues du fichier des champs avec erreur de format.
     * 1.1.3. 2 lignes en erreur issues du fichier avec mauvaise référence.
     * 1.1.4. 1 ligne header.
     * 1.2. Un fichier OK en sortie avec 6 lignes (5 data + 1 header).
     * 2. Equipement Physique
     * 2.1. Un fichier kO en sortie avec 22 lignes (21 data + 1 header).
     * 2.1.1. 5 lignes en erreur issues du fichier des champs obligatoires manquants.
     * 2.1.2. 14 lignes en erreur issues du fichier des champs avec erreur de format.
     * 2.1.3. 4 lignes en erreur issues du fichier avec mauvaise référence.
     * 2.1.4 1 ligne en erreur issue du fichier des champs avec erreur de cohérence.
     * 2.1.5. 1 ligne header.
     * 2.2. un fichier OK en sortie avec 18 lignes (17 data + 1 header).
     * 3. Equipement Virtuel
     * 3.1. Un fichier KO en sortie avec 14 lignes (13 data + 1 header).
     * 3.1.1. 3 lignes en erreur à cause d'un champ manquant
     * 3.1.2. 9 ligne en erreur à cause d'une erreur de format
     * 3.1.3. 1 ligne en erreur à cause d'un contrôle de cohérence
     * 3.1.3. 1 ligne header.
     * 3.2. Un fichier OK en sortie avec 13 lignes (12 data + 1 header).
     * 4. Application
     * 4.1. Un fichier KO en sortie avec 12 lignes (11 data + 1 header).
     * 4.1.1. 4 lignes en erreur à cause d'un champ manquant
     * 4.1.2. 7 lignes en erreur à cause d'un contrôle de cohérence
     * 4.1.3. 1 ligne header.
     * 4.2. Un fichier OK en sortie avec 14 lignes (13 data + 1 header).
     *
     * @throws Exception when errors occurs
     */
    @Test
    void loadingBatchCompleteTest() throws Exception {

        // Create session date.
        final Calendar calendar = Calendar.getInstance();
        calendar.set(2023, Calendar.APRIL, 20, 16, 10, 15);
        final String formattedSessionDate = new SimpleDateFormat("yyyyMMdd-HHmmss").format(calendar.getTime());

        // Copy input file to work folder.
        FileSystemUtils.copyRecursively(new File(testFolder.resolve("work").resolve(formattedSessionDate).toString()),
                new File(Path.of(fileSystemBasePath, subscriber, organization, FileFolder.WORK.getFolderName(),
                        formattedSessionDate).toString()));

        // Mock NumEcoEval
        when(numEcoEvalReferentialRemotingService.getCountryList()).thenReturn(List.of("France", "FR"));
        when(numEcoEvalReferentialRemotingService.getEquipmentTypeList()).thenReturn(List.of("ServeurCalcul", "Desktop", "ComputeServer", "Ecran", "Laptop"));

        // when
        final JobExecution jobExecution = this.jobLauncherTestUtils.launchJob(new JobParametersBuilder()
                .addString("local.working.folder", localJobWorkingPath)
                .addString("delete.local.working.folder", "true")
                .addString(SUBSCRIBER_JOB_PARAM, subscriber)
                .addString(ORGANIZATION_JOB_PARAM, organization)
                .addLong(INVENTORY_ID_JOB_PARAM, inventoryId)
                .addDate("session.date", calendar.getTime())
                .addString(BATCH_NAME_JOB_PARAM, UUID.randomUUID().toString())
                .addJobParameter("locale", Locale.ENGLISH, Locale.class)
                .toJobParameters());

        // General Assertions
        assertThat(jobExecution).extracting(JobExecution::getStatus, JobExecution::getExitStatus)
                .contains(BatchStatus.COMPLETED, CustomExitStatus.COMPLETED_WITH_ERRORS);

        // DataCenter Assertions
        final List<String> datacenterAllBadLines = Files.readAllLines(outputPath.resolve(formattedSessionDate).resolve("rejected_datacenter_" + formattedSessionDate + ".csv"));
        assertThat(datacenterAllBadLines)
                .hasSize(8) // 1 header and 7 bad lines
                .contains("nomCourtDatacenter;nomLongDatacenter;pue;localisation;nomEntite;nomSourceDonnee;inputFileName;lineNumber;message", Index.atIndex(0))
                .satisfies(
                        lst -> {
                            assertThat(lst.stream().filter(line -> line.contains("datacenter_avec_erreur_champs_obligatoire.csv")).count()).isEqualTo(2);
                            assertThat(lst.stream().filter(line -> line.contains("datacenter_avec_erreur_format.csv")).count()).isEqualTo(3);
                            assertThat(lst.stream().filter(line -> line.contains("datacenter_avec_erreur_referentiel.csv")).count()).isEqualTo(2);
                        })
                .contains(
                        "Datacenter 2;Datacenter numéro 2;0.5;FR;SSG;;datacenter_avec_erreur_format.csv;2;Field 'PUE' must be a number > 1.", // Bad format field
                        "Datacenter 3;Datacenter numéro 3;a;FR;SSG;bad source;datacenter_avec_erreur_format.csv;3;Field 'PUE' must be a number > 1.", // Bad format field
                        "DC_Villeperdue_01;DC_Villeperdue_OpenStack_01;1.0;France;testEntite;;datacenter_avec_erreur_format.csv;4;Field 'PUE' must be a number > 1.", // Bad format field
                        "Datacenter 8;Datacenter numéro 8;1.1;;SSG;second bad source;datacenter_avec_erreur_champs_obligatoire.csv;2;Field 'localisation' is mandatory.", // Mandatory field
                        ";Datacenter numéro 10;1.1;FR;SSG;;datacenter_avec_erreur_champs_obligatoire.csv;3;Field 'nomCourtDatacenter' is mandatory.", // Mandatory field
                        "Datacenter bad country;Datacenter test with bad referential;1.5;England;SSG;;datacenter_avec_erreur_referentiel.csv;2;Country England does not exist in the referential. Check your reference or ask your administrator to update the referential according to your needs.", // Referential field
                        "Datacenter bad pue and bad country;Datacenter with bad pue and referential;0.5;England;SSG;;datacenter_avec_erreur_referentiel.csv;3;Field 'PUE' must be a number > 1. Country England does not exist in the referential. Check your reference or ask your administrator to update the referential according to your needs." // Format and referential fileds
                );

        final List<String> datacenterAllValidLines = Files.readAllLines(outputPath.resolve(formattedSessionDate).resolve("accepted_datacenter_" + formattedSessionDate + ".csv"));
        assertThat(datacenterAllValidLines)
                .hasSize(6) // 1 header and 5 bad lines
                .contains("nomCourtDatacenter;nomLongDatacenter;pue;localisation;nomEntite;nomSourceDonnee", Index.atIndex(0))
                .contains(
                        "Datacenter%;Datacenter numéro 5;1.1;FR;SSG;",
                        "Datacenter 1;Datacenter numéro 1;1.1;FR;SSG;My source",
                        "Datacenter 9;Datacenter numéro 9;1.2;FR;SSG;",
                        "Datacenter 4;Datacenter numéro 4;1.2;FR;SSGà;",
                        "Datacenter 5;Datacenter numéro 5;1.1;FR;SSG;");

        // PhysicalEquipment Assertions
        final List<String> physicalEquipmentAllBadLines = Files.readAllLines(outputPath.resolve(formattedSessionDate).resolve("rejected_physical_equipment_" + formattedSessionDate + ".csv"));
        assertThat(physicalEquipmentAllBadLines)
                .hasSize(25) // 1 header and 24 bad lines
                .contains("nomEquipementPhysique;nomEntite;nomSourceDonnee;modele;quantite;type;statut;nbJourUtiliseAn;paysDUtilisation;utilisateur;dateAchat;dateRetrait;nbCoeur;nomCourtDatacenter;goTelecharge;consoElecAnnuelle;fabricant;tailleDuDisque;tailleMemoire;typeDeProcesseur;inputFileName;lineNumber;message", Index.atIndex(0))
                .satisfies(
                        lst -> {
                            assertThat(lst.stream().filter(line -> line.contains("equipementPhysique_avec_erreur_champs_obligatoire.csv")).count()).isEqualTo(5);
                            assertThat(lst.stream().filter(line -> line.contains("equipementPhysique_avec_erreur_format.csv")).count()).isEqualTo(14);
                            assertThat(lst.stream().filter(line -> line.contains("equipementPhysique_avec_erreur_referentiel.csv")).count()).isEqualTo(4);
                            assertThat(lst.stream().filter(line -> line.contains("equipementPhysique_avec_erreur_coherence.csv")).count()).isEqualTo(1);
                        })
                .contains(
                        ";Sopra Steria Group;mockData;rack-server-with-hdd;3;ServeurCalcul;actif;300;FR;;2017-02-22;;15;Datacenter 9;1500;500;dell;100;250;i9;equipementPhysique_avec_erreur_champs_obligatoire.csv;2;Field 'nomEquipementPhysique' is mandatory.",
                        "Desktop 2;Sopra Steria Group;mockData;HP ProDesk 400 G1 MT;-1;Desktop;actif;215;FR;Zinedine Zidane;2019-06-02;;;;0;0;HP;;;;equipementPhysique_avec_erreur_format.csv;3;Field 'quantite' must be a positive integer.",
                        "Desktop 6;Sopra Steria Group;mockData;HP ProDesk 400 G1 MT;6;Desktop;actif;215;FR;Zinedine Zidane;2019-06-02;;1.6;;0;0;HP;;;;equipementPhysique_avec_erreur_format.csv;9;Field 'nbCoeur' must be a positive integer.",
                        "Desktop 12;Sopra Steria Group;mockData;HP ProDesk 400 G1 MT;7;Desktop;actif;215;FR;Zinedine Zidane;2019-06-02;;;;v;0;HP;;;;equipementPhysique_avec_erreur_format.csv;15;Field 'goTelecharge' must be a positive integer.",
                        "Serveur 2;Sopra Steria Group;mockData;;3;ServeurCalcul;actif;300;FR;;2017-02-22;;15;Datacenter 9;1500;500;dell;100;250;i9;equipementPhysique_avec_erreur_champs_obligatoire.csv;3;Field 'modele' is mandatory.",
                        "Desktop 1;Sopra Steria Group;mockData;HP ProDesk 400 G1 MT;v;Desktop;actif;215;FR;Zinedine Zidane;2019-06-02;;;;0;0;HP;;;;equipementPhysique_avec_erreur_format.csv;2;Field 'quantite' must be a positive integer.",
                        "Laptop 1;Sopra Steria Group;mockData;HP EliteBook 735 G6;1;Laptop;actif;215;FR;Hugo Lloris;2020/02/31;;;;120;100;HP;;;;equipementPhysique_avec_erreur_format.csv;4;Field 'dateAchat' of type date must be in format 'YYYY-MM-DD'.",
                        "Desktop 5;Sopra Steria Group;mockData;HP ProDesk 400 G1 MT;6;Desktop;actif;215;FR;Zinedine Zidane;2019-06-02;;1.7;;0;0;HP;;;;equipementPhysique_avec_erreur_format.csv;8;Field 'nbCoeur' must be a positive integer.",
                        "Desktop 7;Sopra Steria Group;mockData;HP ProDesk 400 G1 MT;6;Desktop;actif;215;FR;Zinedine Zidane;2019-06-02;;-1;;0;0;HP;;;;equipementPhysique_avec_erreur_format.csv;10;Field 'nbCoeur' must be a positive integer.",
                        "Desktop 11;Sopra Steria Group;mockData;HP ProDesk 400 G1 MT;7;Desktop;actif;400;FR;Zinedine Zidane;2019-06-02;;;;0;0;HP;;;;equipementPhysique_avec_erreur_format.csv;14;Field 'nbJourUtiliseAn' must an integer between 0 and 365.",
                        "Serveur 4;Sopra Steria Group;mockData;rack-server-with-hdd;3;;actif;300;FR;;2017-02-22;;15;Datacenter 9;1500;500;dell;100;250;i9;equipementPhysique_avec_erreur_champs_obligatoire.csv;5;Field 'type' is mandatory.",
                        "Desktop 3;Sopra Steria Group;mockData;HP ProDesk 400 G1 MT;3;Desktop;actif;215;FR;Zinedine Zidane;2019-06-02;;;;0;1.6;HP;;;;equipementPhysique_avec_erreur_format.csv;6;Field 'consoElecAnnuelle' must be a positive integer.",
                        "Desktop 9;Sopra Steria Group;mockData;HP ProDesk 400 G1 MT;7;Desktop;actif;-1;FR;Zinedine Zidane;2019-06-02;;;;0;0;HP;;;;equipementPhysique_avec_erreur_format.csv;12;Field 'nbJourUtiliseAn' must an integer between 0 and 365.",
                        "Serveur 3;Sopra Steria Group;mockData;rack-server-with-hdd;;ServeurCalcul;inactif;300;FR;;2014-03-05;2023-03-05;10;Datacenter 9;200;400;dell;1000;500;i9;equipementPhysique_avec_erreur_champs_obligatoire.csv;4;Field 'quantite' is mandatory., Field 'quantite' must be a positive integer.",
                        "Laptop 2;Sopra Steria Group;mockData;HP EliteBook 735 G6;1;Laptop;actif;215;FR;Hugo Lloris;;2020/02/31;;;120;100;HP;;;;equipementPhysique_avec_erreur_format.csv;5;Field 'dateRetrait' of type date must be in format 'YYYY-MM-DD'.",
                        "Desktop 4;Sopra Steria Group;mockData;HP ProDesk 400 G1 MT;3;Desktop;actif;215;FR;Zinedine Zidane;2019-06-02;;;;0;-1;HP;;;;equipementPhysique_avec_erreur_format.csv;7;Field 'consoElecAnnuelle' must be a positive integer.",
                        "Desktop 8;Sopra Steria Group;mockData;HP ProDesk 400 G1 MT;7;Desktop;actif;215;FR;Zinedine Zidane;2019-06-02;;1.8;;0;0;HP;;;;equipementPhysique_avec_erreur_format.csv;11;Field 'nbCoeur' must be a positive integer.",
                        "Desktop 10;Sopra Steria Group;mockData;HP ProDesk 400 G1 MT;7;Desktop;actif;v;FR;Zinedine Zidane;2019-06-02;;;;0;0;HP;;;;equipementPhysique_avec_erreur_format.csv;13;Field 'nbJourUtiliseAn' must an integer between 0 and 365.",
                        "Serveur 1;Sopra Steria Group;mockData;rack-server-with-hdd;1;ServeurCalcul;actif;365;FR;;2015-01-18;;10;Datacenter 70;1000;200;dell;200;200;i9;equipementPhysique_avec_erreur_coherence.csv;2;Datacenter Datacenter 70 does not exist within the inventory.",
                        "Serveur Without Country;Sopra Steria Group;mockData;rack-server-with-hdd;3;ServeurCalcul;actif;301;;;2018-07-07;;5;Datacenter 5;200;400;dell;2000;500;i7;equipementPhysique_avec_erreur_champs_obligatoire.csv;6;Field 'paysDUtilisation' is mandatory.",
                        "Desktop with bad country;Sopra Steria Group;mockData;HP ProDesk 400 G1 MT;10;Desktop;actif;215;England;Zinedine Zidane;2019-06-02;;;;0;0;HP;;;;equipementPhysique_avec_erreur_referentiel.csv;2;Country England does not exist in the referential. Check your reference or ask your administrator to update the referential according to your needs.",
                        "Desktop with bad type;Sopra Steria Group;mockData;HP ProDesk 400 G1 MT;10;Unknown;actif;215;FR;Zinedine Zidane;2019-06-02;;;;0;0;HP;;;;equipementPhysique_avec_erreur_referentiel.csv;3;Equipment type Unknown does not exist in the referential. Check your reference or ask your administrator to update the referential according to your needs.",
                        "Desktop with bad format and bad country;Sopra Steria Group;mockData;HP ProDesk 400 G1 MT;10;Desktop;actif;deux cent quinze;England;Zinedine Zidane;2019-06-02;;;;0;0;HP;;;;equipementPhysique_avec_erreur_referentiel.csv;4;Field 'nbJourUtiliseAn' must an integer between 0 and 365. Country England does not exist in the referential. Check your reference or ask your administrator to update the referential according to your needs.",
                        "Desktop with bad format and bad type;Sopra Steria Group;mockData;HP ProDesk 400 G1 MT;-1;Unknown;actif;215;FR;Zinedine Zidane;2019-06-02;;;;0;0;HP;;;;equipementPhysique_avec_erreur_referentiel.csv;5;Field 'quantite' must be a positive integer. Equipment type Unknown does not exist in the referential. Check your reference or ask your administrator to update the referential according to your needs."
                );

        final List<String> physicalEquipmentAllValidLines = Files.readAllLines(outputPath.resolve(formattedSessionDate).resolve("accepted_physical_equipment_" + formattedSessionDate + ".csv"));
        assertThat(physicalEquipmentAllValidLines)
                .hasSize(18) // 1 header and 18 valid lines
                .contains("nomEquipementPhysique;nomEntite;nomSourceDonnee;modele;quantite;type;statut;nbJourUtiliseAn;paysDUtilisation;utilisateur;dateAchat;dateRetrait;nbCoeur;nomCourtDatacenter;goTelecharge;consoElecAnnuelle;fabricant;tailleDuDisque;tailleMemoire;typeDeProcesseur", Index.atIndex(0))
                .contains(
                        "Serveur;Sopra Steria Group;mockData;rack-server-with-hdd;3;ServeurCalcul;actif;301;FR;;2018-07-07;;5;Datacenter 5;200;400;dell;2000;500;i7",
                        "Serveur 5;Sopra Steria Group;mockData;rack-server-with-hdd;3;ServeurCalcul;actif;300;FR;;2017-02-22;;15;Datacenter 5;1500;500;dell;100;250;i9",
                        "Serveur 6;Sopra Steria Group;mockData;rack-server-with-hdd;2;ComputeServer;actif;300;FR;;2018-07-06;;5;Datacenter 9;200;400;dell;2000;500;i9",
                        "Serveur 7;Sopra Steria Group%;mockData;rack-server-with-hdd;1;ServeurCalcul;inactif;300;FR;;2014-03-05;2023-03-05;10;Datacenter 5;200;400;dell;1000;500;i9",
                        "Serveur 8;Sopra Steria Group;mockData;rack-server-with-hdd;3;ServeurCalcul;actif;301;FR;;2018-07-07;;5;Datacenter 9;200;400;dell;2000;500;i7",
                        "Serveur 9;Sopra Steria Group;mockData%;rack-server-with-hdd;2;ServeurCalcul;actif;300;FR;;2018-07-06;;5;Datacenter 5;200;400;dell;2000;500;i9",
                        "Desktop 13;Sopra Steria Group;mockData;HP ProDesk 400 G1 MT;1;Desktop;actif;215;FR;Zinedine Zidane;2019-06-02;;;;0;0;HP;;;",
                        "Serveur 10;Sopra Steria Group;mockData;rack-server-with-hdd&;3;ServeurCalcul;actif;301;FR;;2018-07-07;;5;Datacenter 5;200;400;dell;2000;500;i7",
                        "Desktop 14;Sopra Steria Group;mockData;HP ProDesk 400 G1 MT;1;Desktop;actif;215;FR;Kylian Mbappé;2015-04-01;;;;0;0;HP;;;",
                        "Laptop 3;Sopra Steria Group;mockData;HP EliteBook 735 G6;1;Laptop;actif;215;FR;Raymond Domenech;2015-04-01;;;;10;350;HP;;;",
                        "Laptop 4;Sopra Steria Group;mockData;HP EliteBook 735 G6;1;Laptop;actif;215;FR;Hugo Lloris;2015-04-01;;;;120;100;HP;;;",
                        "Laptop 5;Sopra Steria Group;mockData;HP EliteBook 735 G6;1;Laptop;actif;215;FR;Didier Deschamp;2017-06-26;2019-09-30;;;260;10;HP;;;",
                        "Ecran 2;Sopra Steria Group;mockData;Moniteur LED 27p LG 27BK550Y;1;Ecran;actif;200;FR;Hugo Lloris;2019-09-30;;;;;20;HP;;;",
                        "Serveur 11;Sopra Steria Group;mockData;rack-server-with-hdd;3;ServeurCalcul;actif;301;FR;;2018-07-07;;5;Datacenter 9;300;400;dell;2000;500;i7",
                        "Ecran 1;Sopra Steria Group;mockData;Moniteur LED 27p LG 27BK550Y;1;Ecran;actif;200;FR;Hugo Lloris;2019-09-30;;;;;20;HP;;;",
                        "Serveur 12;Sopra Steria Group;mockData;rack-server-with-hdd;3;ServeurCalcul;actif;215;FR;;2018-07-07;;5;Datacenter 9;300;400;dell;2000;500;i7",
                        "Serveur 13;Sopra Steria Group;mockData;rack-server-with-hdd;3;ServeurCalcul;actif;301;FR;;2018-07-07;;5;Datacenter 5;300;400;dell;2000;500;i7");

        // VirtualEquipment Assertions
        final List<String> virtualEquipmentAllBadLines = Files.readAllLines(outputPath.resolve(formattedSessionDate).resolve("rejected_virtual_equipment_" + formattedSessionDate + ".csv"));
        assertThat(virtualEquipmentAllBadLines)
                .hasSize(14) // 1 header and 13 bad lines
                .contains("nomEquipementVirtuel;nomEquipementPhysique;nomSourceDonneeEquipementPhysique;vCPU;nomEntite;cluster;consoElecAn;typeEqv;cleRepartition;nomSourceDonnee;capaciteStockage;inputFileName;lineNumber;message")
                .satisfies(
                        lst -> {
                            assertThat(lst.stream().filter(line -> line.contains("equipementVirtuel_avec_erreur_champs_obligatoire.csv")).count()).isEqualTo(3);
                            assertThat(lst.stream().filter(line -> line.contains("equipementVirtuel_avec_erreurs_format.csv")).count()).isEqualTo(9);
                            assertThat(lst.stream().filter(line -> line.contains("equipementVirtuel_avec_erreur_de_coherence.csv")).count()).isEqualTo(1);
                        })
                .contains(
                        "VM 17;;;;SSG;;;calcul;;;;equipementVirtuel_avec_erreur_champs_obligatoire.csv;2;Field 'nomEquipementPhysique' is mandatory.",
                        ";Serveur 7;;;SSG;;;calcul;;;;equipementVirtuel_avec_erreur_champs_obligatoire.csv;3;Field 'nomEquipementVirtuel' is mandatory.",
                        "VM18;Serveur 7;;;S;;;;;;;equipementVirtuel_avec_erreur_champs_obligatoire.csv;4;Field 'typeEqv' is mandatory.",
                        "VM 2;Serveur 7;MockData;5;SSG;cluster1;250;calcul;x;Mock;5;equipementVirtuel_avec_erreurs_format.csv;2;Field 'cleRepartition' must be a positive number.",
                        "VM 3;Serveur 7;MockData;5;SSG;cluster1;250;calcul;-1;Mock;5;equipementVirtuel_avec_erreurs_format.csv;3;Field 'cleRepartition' must be a positive number.",
                        "VM 4;Serveur 7;MockData;x;SSG;cluster1;250;calcul;1.3;Mock;5;equipementVirtuel_avec_erreurs_format.csv;4;Field 'vCPU' must be a positive number.",
                        "VM 5;Serveur 7;MockData;-1;SSG;cluster1;250;calcul;3;Mock;5;equipementVirtuel_avec_erreurs_format.csv;5;Field 'vCPU' must be a positive number.",
                        "VM 6;Serveur 7;MockData;5;SSG;cluster1;250;calcul;1;Mock;x;equipementVirtuel_avec_erreurs_format.csv;6;Field 'capaciteStockage' must be a positive number.",
                        "VM 7;Serveur 7;MockData;5;SSG;cluster1;250;calcul;-1;Mock;-1;equipementVirtuel_avec_erreurs_format.csv;7;Field 'cleRepartition' must be a positive number.",
                        "VM 8;Serveur 7;MockData;5;SSG;cluster1;-1;calcul;1;Mock;3;equipementVirtuel_avec_erreurs_format.csv;8;Field 'consoElecAn' must be a positive integer.",
                        "VM 9;Serveur 7;MockData;5;SSG;cluster1;x;calcul;1;Mock;3;equipementVirtuel_avec_erreurs_format.csv;9;Field 'consoElecAn' must be a positive integer.",
                        "VM 10;Serveur 7;MockData;5;SSG;cluster1;1.3;calcul;1;Mock;3;equipementVirtuel_avec_erreurs_format.csv;10;Field 'consoElecAn' must be a positive integer.",
                        "VM 20;Serveur 30;;;SSG;;;calcul;;;;equipementVirtuel_avec_erreur_de_coherence.csv;2;Physical equipment Serveur 30 does not exist within the inventory."
                );

        final List<String> virtualEquipmentAllValidLines = Files.readAllLines(outputPath.resolve(formattedSessionDate).resolve("accepted_virtual_equipment_" + formattedSessionDate + ".csv"));
        assertThat(virtualEquipmentAllValidLines)
                .hasSize(13) // 1 header and 12 valid lines
                .contains("nomEquipementVirtuel;nomEquipementPhysique;nomSourceDonneeEquipementPhysique;vCPU;nomEntite;cluster;consoElecAn;typeEqv;cleRepartition;nomSourceDonnee;capaciteStockage")
                .contains(
                        "VM 48;Serveur 9;MockData;1;SSG;cluster1;1;calcul;3.3;Mock;3",
                        "VM 49;Serveur 10;MockData;2;SSG;cluster1;1;calcul;3.3;Mock;3",
                        "VM 50;Serveur 10;MockData;3;SSG;cluster1;1;calcul;3.3;Mock;3",
                        "VM 51;Serveur 11;MockData;1.3;SSGé;cluster1;1;calcul;3.3;Mock;3",
                        "VM 52;Serveur 11;MockData;1.3;SSGé;cluster1;1;calcul;3.3;Mock;3",
                        "VM 53;Serveur 11;MockData;24.340;SSG;cluster1;1;calcul;3.3;Mock;3",
                        "VM 54;Serveur 11;MockData;1;SSG;cluster1;1;calcul;3.3;Mock;3",
                        "VMà;Serveur 7;MockData;1;SSG;cluster1;1;calcul;3.3;Mock;3",
                        "VM 55;Serveur 7;MockData;1;SSGé;cluster1;1;calcul;3.3;Mock;3",
                        "VM 56;Serveur 11;MockData;1.3;SSGé;cluster1;1;calcul;3.3;Mock;3",
                        "VM 57;Serveur 7;MockData;5;SSG;cluster1;1;calcul;3.3;Mock;",
                        "VM 58;Serveur 7;MockData;5;SSG;cluster1;1;calcul;;Mock;3"
                );

        // Application Assertions
        final List<String> applicationAllBadLines = Files.readAllLines(outputPath.resolve(formattedSessionDate).resolve("rejected_application_" + formattedSessionDate + ".csv"));
        assertThat(applicationAllBadLines)
                .hasSize(12) // 1 header and 11 bad lines
                .contains("nomApplication;typeEnvironnement;nomEquipementVirtuel;nomSourceDonneeEquipementVirtuel;domaine;sousDomaine;nomEntite;nomSourceDonnee;nomEquipementPhysique;inputFileName;lineNumber;message")
                .satisfies(
                        lst -> {
                            assertThat(lst.stream().filter(line -> line.contains("application_avec_erreur_champs_obligatoire.csv")).count()).isEqualTo(4);
                            assertThat(lst.stream().filter(line -> line.contains("application_avec_erreur_coherence.csv")).count()).isEqualTo(7);
                        })
                .contains(
                        ";Recette;VM 7;;Domaine 1;Sous Domaine 1;SSG;Serveur 2;;application_avec_erreur_champs_obligatoire.csv;2;Field 'nomApplication' is mandatory.",
                        "Application 20;;VM 8;;Domaine 1;Sous Domaine 1;SSG;Serveur 2;;application_avec_erreur_champs_obligatoire.csv;3;Field 'typeEnvironnement' is mandatory.",
                        "Application 21;Production;;Source vm absente;Domaine 1;Sous Domaine 1;SSG;Serveur 2;;application_avec_erreur_champs_obligatoire.csv;4;Field 'nomEquipementVirtuel' is mandatory.",
                        "Application 22;;VM 8;;Domaine 1;Sous Domaine 1;SSG;;;application_avec_erreur_champs_obligatoire.csv;5;Field 'typeEnvironnement' is mandatory.",
                        "Application 10;Recette;VM 30;source name vm 10;Domaine 1;Sous Domaine 1;SSG;Serveur 2;;application_avec_erreur_coherence.csv;2;Virtual equipment VM 30 does not exist within the inventory.",
                        "Application 11;Recette;VM 22;;Domaine 1;Sous Domaine 1;SSG;Serveur 2;;application_avec_erreur_coherence.csv;3;Virtual equipment VM 22 does not exist within the inventory.",
                        "Application 12;Produc!tion;VM 10;;Domaine 1;Sous Domaine 1;SSG;Serveur 2;;application_avec_erreur_coherence.csv;4;Virtual equipment VM 10 does not exist within the inventory.",
                        "Application 13;Dev;VM 11;;Domaine 1;Sous Domaine 2;SS&G;Serveur 2;;application_avec_erreur_coherence.csv;5;Virtual equipment VM 11 does not exist within the inventory.",
                        "Application 14;Recette;VM 7;;Domaine 1;Sous Domaine 1;SSG;Serveur 2;;application_avec_erreur_coherence.csv;6;Virtual equipment VM 7 does not exist within the inventory.",
                        "Application 15;Production;VM 8;;Domaine 1;S&ous Domaine 1;SSG;Serveur 2;;application_avec_erreur_coherence.csv;7;Virtual equipment VM 8 does not exist within the inventory.",
                        "Application 16;Production;VM 9;;DomaineŠ1;Sous Domaine 1;SSG;Serveur 2;;application_avec_erreur_coherence.csv;8;Virtual equipment VM 9 does not exist within the inventory.");

        final List<String> applicationAllValidLines = Files.readAllLines(outputPath.resolve(formattedSessionDate).resolve("accepted_application_" + formattedSessionDate + ".csv"));
        assertThat(applicationAllValidLines)
                .hasSize(14) // 1 header and 13 valid lines
                .contains("nomApplication;typeEnvironnement;nomEquipementVirtuel;nomSourceDonneeEquipementVirtuel;domaine;sousDomaine;nomEntite;nomSourceDonnee;nomEquipementPhysique")
                .contains(
                        "Application 1;Recette;VM 48;;Domaine 1;Sous Domaine 1;SSG;Serveur 2;Serveur 9",
                        "Application 1;Recette;VM 49;Source name vm49;Domaine 1;Sous Domaine 1;SSG;Serveur 2;Serveur 10",
                        "Application 2;Dev;VM 52;;Domaine 1;Sous Domaine 2;SSG;Serveur 2;Serveur 11",
                        "Application 2;Integ;VM 53;;Domaine 1;Sous Domaine 2;SSG;;Serveur 11",
                        "Application 2;Dev;VM 52;;Domaine 1;Sous Domaine 2;SSG;Serveur 2;Serveur 11",
                        "Application 3;Integ;VM 53;;Domaine 1;Sous Domaine 2;SSG;;Serveur 11",
                        "Application 2;Recette;VM 54;;Domaine 1;Sous Domaine 2;SSG;;Serveur 11",
                        "Application 4;Recette;VM 55;;Domaine 1;Sous Domaine 1;SSG;;Serveur 7",
                        "Application 1;Produc!tion;VM 54;;Domaine 1;Sous Domaine 1;SSG;Serveur 233;Serveur 11",
                        "Application 2;Dev;VM 53;;Domaine 1;Sous Domaine 2;SS&G;Serrrveur 2;Serveur 11",
                        "Application 1;Recette;VM 52;;Domaine 1;Sous Domaine 1;SSG;Desktop 25;Serveur 11",
                        "Application 1;Production;VM 51;;Domaine 1;S&ous Domaine 1;SSG;Serveur 49;Serveur 11",
                        "Application 1;Production;VM 50;;DomaineŠ1;Sous Domaine 1;SSG;Serveur 70;Serveur 10");

        verify(numEcoEvalReferentialRemotingService, times(1)).getCountryList();
        verify(numEcoEvalReferentialRemotingService, times(1)).getEquipmentTypeList();

        // DB Verifications
        assertThat(inventoryRepository.findById(inventoryId)).get().extracting(Inventory::getDataCenterCount,
                        Inventory::getPhysicalEquipmentCount, Inventory::getVirtualEquipmentCount, Inventory::getApplicationCount)
                .contains(5L, 33L, 12L, 4L);
    }

    /**
     * Cas de test Update :
     * <p>
     * Le fichier en entrée contient un datacenter déjà existant en BDD
     * <p>
     * Attendu :
     * 1. Un fichier OK en sortie avec 1 ligne.
     * 2. Une entité mise à jour en bdd
     *
     * @throws Exception
     */
    @Test
    void givenExistingDatacenter_whenAddingSameDatacenter_thenUpdate() throws Exception {
        // Create session date.
        final Calendar calendar = Calendar.getInstance();
        calendar.set(2023, Calendar.APRIL, 21, 10, 30, 0);
        final String formattedSessionDate = new SimpleDateFormat("yyyyMMdd-HHmmss").format(calendar.getTime());

        // Copy input file to work folder.
        FileSystemUtils.copyRecursively(new File(testFolder.resolve("work").resolve(formattedSessionDate).toString()),
                new File(Path.of(fileSystemBasePath, subscriber, organization, FileFolder.WORK.getFolderName(),
                        formattedSessionDate).toString()));

        // when we have an existing datacenter entity
        final DataCenter old = dataCenterRepository.save(DataCenter.builder()
                .inventoryId(inventoryId)
                .nomCourtDatacenter("DC_A_MAJ_01")
                .pue("2.0") // previous pue was 2.0
                .sessionDate(calendar.getTime())
                .localisation("France")
                .build());

        // Mock NumEcoEval
        when(numEcoEvalReferentialRemotingService.getCountryList()).thenReturn(List.of("France"));
        when(numEcoEvalReferentialRemotingService.getEquipmentTypeList()).thenReturn(List.of("Server"));

        // when we launch the job
        final JobExecution jobExecution = this.jobLauncherTestUtils.launchJob(new JobParametersBuilder()
                .addString("local.working.folder", localJobWorkingPath)
                .addString("delete.local.working.folder", "true")
                .addString("zip.results", "false")
                .addString(SUBSCRIBER_JOB_PARAM, subscriber)
                .addString(ORGANIZATION_JOB_PARAM, organization)
                .addLong(INVENTORY_ID_JOB_PARAM, inventoryId)
                .addDate("session.date", calendar.getTime())
                .addString(INVENTORY_NAME_JOB_PARAM, "04-2023")
                .addString(BATCH_NAME_JOB_PARAM, UUID.randomUUID().toString())
                .toJobParameters());

        // then it should succeed
        assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);

        final File resultFileOk = outputPath.resolve(formattedSessionDate).resolve("accepted_datacenter_" + formattedSessionDate + ".csv").toFile();
        assertThat(resultFileOk).isNotNull();
        try (LineNumberReader reader = new LineNumberReader(new FileReader(resultFileOk))) {
            reader.skip(Integer.MAX_VALUE);
            assertThat(reader.getLineNumber()).isEqualTo(2); // Headers + One line
        }
        final File resultFileKo = outputPath.resolve(formattedSessionDate).resolve("rejected_datacenter_" + formattedSessionDate + ".csv").toFile();
        assertThat(resultFileKo).isNotNull();
        try (LineNumberReader reader = new LineNumberReader(new FileReader(resultFileKo))) {
            reader.skip(Integer.MAX_VALUE);
            assertThat(reader.getLineNumber()).isEqualTo(1); // Headers
        }

        // and the old entity must be updated
        assertThat(dataCenterRepository.findByInventoryIdAndNomCourtDatacenter(inventoryId, "DC_A_MAJ_01")).get().satisfies(
                newDc -> {
                    assertThat(newDc.getId()).isEqualTo(old.getId());
                    assertThat(newDc.getPue()).isEqualTo("1.1");
                    assertThat(newDc.getPue()).isNotEqualTo(old.getPue());
                    assertThat(newDc.getNomCourtDatacenter()).isEqualTo(old.getNomCourtDatacenter());
                });

        verify(numEcoEvalReferentialRemotingService, times(1)).getCountryList();
        verify(numEcoEvalReferentialRemotingService, times(1)).getEquipmentTypeList();
    }

    /**
     * Tests the option of zipping working directory in single archive
     */
    @Test
    void loadingBatchWithZipResultFile() throws Exception {

        final Calendar calendar = Calendar.getInstance();
        calendar.set(2023, Calendar.MAY, 12, 21, 31, 0);
        final String formattedSessionDate = new SimpleDateFormat("yyyyMMdd-HHmmss").format(calendar.getTime());

        // Copy input file to work folder.
        FileSystemUtils.copyRecursively(new File(testFolder.resolve("work").resolve(formattedSessionDate).toString()),
                new File(Path.of(fileSystemBasePath, subscriber, organization, FileFolder.WORK.getFolderName(),
                        formattedSessionDate).toString()));

        // when we launch the job
        final JobExecution jobExecution = this.jobLauncherTestUtils.launchJob(new JobParametersBuilder()
                .addString("local.working.folder", localJobWorkingPath)
                .addString("delete.local.working.folder", "true")
                .addString("zip.results", "true")
                .addString(SUBSCRIBER_JOB_PARAM, subscriber)
                .addString(ORGANIZATION_JOB_PARAM, organization)
                .addLong(INVENTORY_ID_JOB_PARAM, inventoryId)
                .addDate("session.date", calendar.getTime())
                .addString(INVENTORY_NAME_JOB_PARAM, "04-2023")
                .addString(BATCH_NAME_JOB_PARAM, UUID.randomUUID().toString())
                .toJobParameters());

        // then it should succeed and create an archive
        assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        final File zipResultFile = outputPath.resolve(formattedSessionDate).resolve(formattedSessionDate + "_results.zip").toFile();
        final File zipAcceptedFile = outputPath.resolve(formattedSessionDate).resolve(formattedSessionDate + "_accepted.zip").toFile();
        assertThat(zipResultFile).exists();
        assertThat(zipAcceptedFile).exists();
    }

    /**
     * Cas de test limit.
     * <p>
     * Avec un fichier ayant de mauvais headers, le job termine en FAILED, exitStatus FAILED_HEADERS.
     * La description contient le type de fichier en erreur.
     *
     * @throws Exception
     */
    @Test
    void loadingBatchWithInvalidHeaders() throws Exception {

        // given an existing work folder for the batch
        final Calendar calendar = Calendar.getInstance();
        calendar.set(2023, Calendar.JANUARY, 01, 10, 45, 59);
        final String formattedSessionDate = new SimpleDateFormat("yyyyMMdd-HHmmss").format(calendar.getTime());

        // Copy input file to work folder.
        FileSystemUtils.copyRecursively(new File(testFolder.resolve("work").resolve(formattedSessionDate).toString()),
                new File(Path.of(fileSystemBasePath, subscriber, organization, FileFolder.WORK.getFolderName(),
                        formattedSessionDate).toString()));

        // when
        final JobExecution jobExecution = this.jobLauncherTestUtils.launchJob(new JobParametersBuilder()
                .addString("local.working.folder", localJobWorkingPath)
                .addString("delete.local.working.folder", "true")
                .addString(SUBSCRIBER_JOB_PARAM, subscriber)
                .addString(ORGANIZATION_JOB_PARAM, organization)
                .addLong(INVENTORY_ID_JOB_PARAM, inventoryId)
                .addDate("session.date", calendar.getTime())
                .addString(INVENTORY_NAME_JOB_PARAM, "04-2023")
                .addString(BATCH_NAME_JOB_PARAM, UUID.randomUUID().toString())
                .toJobParameters());

        // General Assertions
        assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(jobExecution.getExitStatus().getExitCode()).isEqualTo("FAILED_HEADERS");

        final File physicalEquipmentValidFile = outputPath.resolve(formattedSessionDate).resolve("accepted_physical_equipment_" + formattedSessionDate + ".csv").toFile();
        assertThat(physicalEquipmentValidFile).isNotNull();


    }

    /**
     * Cas de test.
     * <p>
     * Avec des fichiers valides, le job termine en COMPLETED, exitStatus COMPLETED.
     *
     * @throws Exception
     */
    @Test
    void loadingBatchWithoutError() throws Exception {

        // given an existing work folder for the batch
        final Calendar calendar = Calendar.getInstance();
        calendar.set(2023, Calendar.APRIL, 20, 16, 10, 16);
        final String formattedSessionDate = new SimpleDateFormat("yyyyMMdd-HHmmss").format(calendar.getTime());

        // Copy input file to work folder.
        FileSystemUtils.copyRecursively(new File(testFolder.resolve("work").resolve(formattedSessionDate).toString()),
                new File(Path.of(fileSystemBasePath, subscriber, organization, FileFolder.WORK.getFolderName(),
                        formattedSessionDate).toString()));

        // Mock NumEcoEval
        when(numEcoEvalReferentialRemotingService.getCountryList()).thenReturn(List.of("France", "FR"));
        when(numEcoEvalReferentialRemotingService.getEquipmentTypeList()).thenReturn(List.of("Serveur 2", "Desktop", "Laptop", "Ecran", "ServeurCalcul"));

        // when
        final JobExecution jobExecution = this.jobLauncherTestUtils.launchJob(new JobParametersBuilder()
                .addString("local.working.folder", localJobWorkingPath)
                .addString("delete.local.working.folder", "true")
                .addString(SUBSCRIBER_JOB_PARAM, subscriber)
                .addString(ORGANIZATION_JOB_PARAM, organization)
                .addLong(INVENTORY_ID_JOB_PARAM, inventoryId)
                .addDate("session.date", calendar.getTime())
                .addString(INVENTORY_NAME_JOB_PARAM, "04-2023")
                .addString(BATCH_NAME_JOB_PARAM, UUID.randomUUID().toString())
                .toJobParameters());

        // General Assertions
        assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(jobExecution.getExitStatus().getExitCode()).isEqualTo(ExitStatus.COMPLETED.getExitCode());

        verify(numEcoEvalReferentialRemotingService, times(1)).getCountryList();
        verify(numEcoEvalReferentialRemotingService, times(1)).getEquipmentTypeList();
    }

    /**
     * Test Case - Load part of inventory
     * <p>
     * Loading with datacenter and physical equipment
     *
     * @throws Exception
     */
    @Test
    void whenLoadingPartOfInventory_thenCompleted() throws Exception {

        // given an existing work folder for the batch
        final Calendar calendar = Calendar.getInstance();
        calendar.set(2023, Calendar.JULY, 28, 10, 0, 0);
        final String formattedSessionDate = new SimpleDateFormat("yyyyMMdd-HHmmss").format(calendar.getTime());

        // Copy input file to work folder.
        FileSystemUtils.copyRecursively(new File(testFolder.resolve("work").resolve(formattedSessionDate).toString()),
                new File(Path.of(fileSystemBasePath, subscriber, organization, FileFolder.WORK.getFolderName(),
                        formattedSessionDate).toString()));

        // Mock NumEcoEval
        when(numEcoEvalReferentialRemotingService.getCountryList()).thenReturn(List.of("France", "China", "Spain", "Germany", "Allemagne", "Belgium", "Russia", "FR"));
        when(numEcoEvalReferentialRemotingService.getEquipmentTypeList()).thenReturn(List.of("Server", "Monitor", "Mobility Device", "Cashing System",
                "Personal Computer", "Printer", "Tracer", "Network Gear", "Cash registered device", "Consumable", "Communication Device", "IP Router",
                "IP Switch", "Network Load Balancer", "Smartphone", "Invoice terminal", "Self Check Out", "Plotter"));

        // when
        final JobExecution jobExecution = this.jobLauncherTestUtils.launchJob(new JobParametersBuilder()
                .addString("local.working.folder", localJobWorkingPath)
                .addString("delete.local.working.folder", "true")
                .addString(SUBSCRIBER_JOB_PARAM, subscriber)
                .addString(ORGANIZATION_JOB_PARAM, organization)
                .addLong(INVENTORY_ID_JOB_PARAM, inventoryId)
                .addDate("session.date", calendar.getTime())
                .addString(INVENTORY_NAME_JOB_PARAM, "04-2023")
                .addString(BATCH_NAME_JOB_PARAM, UUID.randomUUID().toString())
                .toJobParameters());

        // General Assertions
        assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(jobExecution.getExitStatus().getExitCode()).isEqualTo(ExitStatus.COMPLETED.getExitCode());

        verify(numEcoEvalReferentialRemotingService, times(1)).getCountryList();
        verify(numEcoEvalReferentialRemotingService, times(1)).getEquipmentTypeList();
    }

    @Test
    void whenLoadingInputFileContainingDuplicate_thenDuplicateAreIgnored() throws Exception {

        // given an existing work folder for the batch
        final Calendar calendar = Calendar.getInstance();
        calendar.set(2023, Calendar.SEPTEMBER, 25, 16, 10, 16);
        final String formattedSessionDate = new SimpleDateFormat("yyyyMMdd-HHmmss").format(calendar.getTime());

        // Copy input file to work folder.
        FileSystemUtils.copyRecursively(new File(testFolder.resolve("work").resolve(formattedSessionDate).toString()),
                new File(Path.of(fileSystemBasePath, subscriber, organization, FileFolder.WORK.getFolderName(),
                        formattedSessionDate).toString()));

        // Mock NumEcoEval
        when(numEcoEvalReferentialRemotingService.getCountryList()).thenReturn(List.of("France", "FR"));
        when(numEcoEvalReferentialRemotingService.getEquipmentTypeList()).thenReturn(List.of("Serveur 2", "Desktop", "Laptop", "Ecran", "ServeurCalcul"));

        // when
        final JobExecution jobExecution = this.jobLauncherTestUtils.launchJob(new JobParametersBuilder()
                .addString("local.working.folder", localJobWorkingPath)
                .addString("delete.local.working.folder", "true")
                .addString(SUBSCRIBER_JOB_PARAM, subscriber)
                .addString(ORGANIZATION_JOB_PARAM, organization)
                .addLong(INVENTORY_ID_JOB_PARAM, inventoryId)
                .addDate("session.date", calendar.getTime())
                .addString(INVENTORY_NAME_JOB_PARAM, "04-2023")
                .addString(BATCH_NAME_JOB_PARAM, UUID.randomUUID().toString())
                .toJobParameters());

        // General Assertions
        assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(jobExecution.getExitStatus().getExitCode()).isEqualTo(ExitStatus.COMPLETED.getExitCode());

        final Page<DataCenter> datacenters = dataCenterRepository.findByInventoryId(inventoryId, Pageable.ofSize(50));
        assertThat(datacenters).hasSize(3).extracting(DataCenter::getPue).contains("1.1", "1.3", "5.1");
        final Page<PhysicalEquipment> physicalEquipments = physicalEquipmentRepository.findByInventoryId(inventoryId, Pageable.ofSize(50));
        assertThat(physicalEquipments).hasSize(12);
        final Page<VirtualEquipment> virtualEquipments = virtualEquipmentRepository.findByInventoryId(inventoryId, Pageable.ofSize(50));
        assertThat(virtualEquipments).hasSize(12);
        final Page<Application> applications = applicationRepository.findByInventoryId(inventoryId, Pageable.ofSize(50));
        assertThat(applications).hasSize(13);

        verify(numEcoEvalReferentialRemotingService, times(1)).getCountryList();
        verify(numEcoEvalReferentialRemotingService, times(1)).getEquipmentTypeList();
    }
}
