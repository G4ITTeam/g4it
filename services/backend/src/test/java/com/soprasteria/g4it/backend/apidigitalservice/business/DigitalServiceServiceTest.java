/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
package com.soprasteria.g4it.backend.apidigitalservice.business;

import com.soprasteria.g4it.backend.apidigitalservice.mapper.DatacenterDigitalServiceMapper;
import com.soprasteria.g4it.backend.apidigitalservice.mapper.DigitalServiceMapper;
import com.soprasteria.g4it.backend.apidigitalservice.model.*;
import com.soprasteria.g4it.backend.apidigitalservice.modeldb.DatacenterDigitalService;
import com.soprasteria.g4it.backend.apidigitalservice.modeldb.DigitalService;
import com.soprasteria.g4it.backend.apidigitalservice.modeldb.Network;
import com.soprasteria.g4it.backend.apidigitalservice.modeldb.Terminal;
import com.soprasteria.g4it.backend.apidigitalservice.modeldb.referential.DeviceTypeRef;
import com.soprasteria.g4it.backend.apidigitalservice.modeldb.referential.NetworkTypeRef;
import com.soprasteria.g4it.backend.apidigitalservice.modeldb.referential.ServerHostRef;
import com.soprasteria.g4it.backend.apidigitalservice.repository.DatacenterDigitalServiceRepository;
import com.soprasteria.g4it.backend.apidigitalservice.repository.DigitalServiceRepository;
import com.soprasteria.g4it.backend.apiindicator.business.IndicatorService;
import com.soprasteria.g4it.backend.apiuser.business.OrganizationService;
import com.soprasteria.g4it.backend.apiuser.modeldb.Organization;
import com.soprasteria.g4it.backend.apiuser.modeldb.User;
import com.soprasteria.g4it.backend.apiuser.repository.UserRepository;
import com.soprasteria.g4it.backend.client.gen.connector.apiexposition.dto.ModeRest;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DigitalServiceServiceTest {

    @Mock
    private DigitalServiceRepository digitalServiceRepository;

    @Mock
    private OrganizationService organizationService;

    @Mock
    private DatacenterDigitalServiceRepository datacenterDigitalServiceRepository;

    @Mock
    private UserRepository userRepository;

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
        final String userName = "test.test";
        final String subscriber = "subscriber";
        final String organizationName = "test";
        final Organization linkedOrganization = Organization.builder().name(organizationName).build();
        final User user = User.builder().username(userName).build();
        final DigitalServiceBO expectedBo = DigitalServiceBO.builder().build();
        final String expectedName = "Digital Service 1";
        final List<DigitalService> existingDigitalService = new ArrayList<>();

        final DigitalService digitalServiceToSave = DigitalService.builder().organization(linkedOrganization).user(user).name(expectedName).build();
        when(digitalServiceRepository.findByOrganizationAndUserUsername(linkedOrganization, userName)).thenReturn(existingDigitalService);
        when(organizationService.getOrganization(subscriber, organizationName)).thenReturn(linkedOrganization);
        when(digitalServiceRepository.save(any())).thenReturn(digitalServiceToSave);
        when(digitalServiceMapper.toBusinessObject(digitalServiceToSave)).thenReturn(expectedBo);
        when(userRepository.findByUsername(userName)).thenReturn(Optional.of(user));

        final DigitalServiceBO result = digitalServiceService.createDigitalService(subscriber, organizationName, userName);

        assertThat(result).isEqualTo(expectedBo);

        verify(organizationService, times(1)).getOrganization(subscriber, organizationName);
        verify(digitalServiceRepository, times(1)).findByOrganizationAndUserUsername(linkedOrganization, userName);
        verify(digitalServiceRepository, times(1)).save(any());
        verify(digitalServiceMapper, times(1)).toBusinessObject(digitalServiceToSave);
        verify(userRepository, times(1)).findByUsername(userName);
    }

    @Test
    void shouldCreateNewDigitalService_withExistingDigitalService() {
        final String userName = "test.test";
        final String organizationName = "test";
        final String subscriber = "subscriber";
        final User user = User.builder().username(userName).build();
        final Organization linkedOrganization = Organization.builder().name(organizationName).build();
        final DigitalServiceBO expectedBo = DigitalServiceBO.builder().build();
        final String expectedName = "Digital Service 2";
        final List<DigitalService> existingDigitalService = List.of(DigitalService.builder().name("Digital Service 1").build(), DigitalService.builder().name("My Digital Service").build());

        final DigitalService digitalServiceToSave = DigitalService.builder().organization(linkedOrganization).user(user).name(expectedName).build();
        when(digitalServiceRepository.findByOrganizationAndUserUsername(linkedOrganization, userName)).thenReturn(existingDigitalService);
        when(organizationService.getOrganization(subscriber, organizationName)).thenReturn(linkedOrganization);
        when(digitalServiceRepository.save(any())).thenReturn(digitalServiceToSave);
        when(digitalServiceMapper.toBusinessObject(digitalServiceToSave)).thenReturn(expectedBo);
        when(userRepository.findByUsername(userName)).thenReturn(Optional.of(user));

        final DigitalServiceBO result = digitalServiceService.createDigitalService(subscriber, organizationName, userName);

        assertThat(result).isEqualTo(expectedBo);

        verify(organizationService, times(1)).getOrganization(subscriber, organizationName);
        verify(digitalServiceRepository, times(1)).findByOrganizationAndUserUsername(linkedOrganization, userName);
        verify(digitalServiceRepository, times(1)).save(any());
        verify(digitalServiceMapper, times(1)).toBusinessObject(digitalServiceToSave);
        verify(userRepository, times(1)).findByUsername(userName);
    }

    @Test
    void shouldListDigitalService() {
        final String username = "test";
        final String subscriber = "subscriber";
        final String organizationName = "test";
        final Organization linkedOrganization = Organization.builder().name(organizationName).build();

        final List<DigitalService> digitalServices = List.of(DigitalService.builder().name("name").build());
        when(organizationService.getOrganization(subscriber, organizationName)).thenReturn(linkedOrganization);
        when(digitalServiceRepository.findByOrganizationAndUserUsername(linkedOrganization, username)).thenReturn(digitalServices);
        List<DigitalServiceBO> digitalServiceBOs = List.of(DigitalServiceBO.builder().name("name").build());
        when(digitalServiceMapper.toBusinessObject(digitalServices)).thenReturn(digitalServiceBOs);

        final List<DigitalServiceBO> result = digitalServiceService.getDigitalServices(subscriber, organizationName, username);

        assertThat(result).isEqualTo(digitalServiceBOs);
        verify(organizationService, times(1)).getOrganization(subscriber, organizationName);
        verify(digitalServiceRepository, times(1)).findByOrganizationAndUserUsername(linkedOrganization, username);
        verify(digitalServiceMapper, times(1)).toBusinessObject(digitalServices);
    }

    @Test
    void shouldDeleteDigitalService() {
        final String digitalServiceUid = "80651485-3f8b-49dd-a7be-753e4fe1fd36";
        final String organizationName = "test";

        doNothing().when(digitalServiceRepository).deleteById(digitalServiceUid);
        doNothing().when(indicatorService).deleteIndicators(organizationName, digitalServiceUid);

        digitalServiceService.deleteDigitalService(organizationName, digitalServiceUid);

        verify(digitalServiceRepository, times(1)).deleteById(digitalServiceUid);
        verify(indicatorService, times(1)).deleteIndicators(organizationName, digitalServiceUid);
    }

    @Test
    void shouldUpdateDigitalService() {
        final String digitalServiceUid = "80651485-3f8b-49dd-a7be-753e4fe1fd36";

        final DigitalServiceBO inputDigitalServiceBO = DigitalServiceBO.builder().uid(digitalServiceUid).name("name").build();
        final DigitalServiceBO digitalServiceBO = DigitalServiceBO.builder().uid(digitalServiceUid).build();
        final DigitalService digitalService = DigitalService.builder().uid(digitalServiceUid).build();
        final DigitalService digitalServiceUpdated = DigitalService.builder().uid(digitalServiceUid).name("name").build();

        when(digitalServiceRepository.findById(digitalService.getUid())).thenReturn(Optional.of(digitalService));
        when(digitalServiceMapper.toFullBusinessObject(digitalService)).thenReturn(digitalServiceBO);
        doNothing().when(digitalServiceMapper).mergeEntity(digitalService, inputDigitalServiceBO, digitalServiceReferentialService);
        when(digitalServiceRepository.save(digitalService)).thenReturn(digitalServiceUpdated);
        when(digitalServiceMapper.toFullBusinessObject(digitalServiceUpdated)).thenReturn(inputDigitalServiceBO);

        final DigitalServiceBO result = digitalServiceService.updateDigitalService(inputDigitalServiceBO);

        assertThat(result).isEqualTo(inputDigitalServiceBO);
        verify(digitalServiceRepository, times(1)).findById(digitalServiceBO.getUid());
        verify(digitalServiceMapper, times(1)).toFullBusinessObject(digitalService);
        verify(digitalServiceMapper, times(1)).mergeEntity(digitalService, inputDigitalServiceBO, digitalServiceReferentialService);
        verify(digitalServiceRepository, times(1)).save(digitalService);
        verify(digitalServiceMapper, times(1)).toFullBusinessObject(digitalServiceUpdated);
    }

    @Test
    void shouldUpdateDigitalService_withRemovedTerminalAndNetwork() {
        final String digitalServiceUid = "80651485-3f8b-49dd-a7be-753e4fe1fd36";

        final DigitalServiceBO inputDigitalServiceBO = DigitalServiceBO.builder().uid(digitalServiceUid).name("name").build();
        final DigitalService digitalService = DigitalService.builder().uid(digitalServiceUid).build();
        digitalService.addTerminal(Terminal.builder().uid("uidTerminal1").build());
        digitalService.addNetwork(Network.builder().uid("uidNetwork").build());

        final DigitalServiceBO digitalServiceBO = DigitalServiceBO.builder().uid(digitalServiceUid).build();
        final DigitalService digitalServiceUpdated = DigitalService.builder().uid(digitalServiceUid).name("name").build();

        when(digitalServiceRepository.findById(digitalService.getUid())).thenReturn(Optional.of(digitalService));
        when(digitalServiceMapper.toFullBusinessObject(digitalService)).thenReturn(digitalServiceBO);
        doNothing().when(digitalServiceMapper).mergeEntity(digitalService, inputDigitalServiceBO, digitalServiceReferentialService);
        when(digitalServiceRepository.save(digitalService)).thenReturn(digitalServiceUpdated);
        when(digitalServiceMapper.toFullBusinessObject(digitalServiceUpdated)).thenReturn(inputDigitalServiceBO);

        final DigitalServiceBO result = digitalServiceService.updateDigitalService(inputDigitalServiceBO);

        assertThat(result).isEqualTo(inputDigitalServiceBO);
        verify(digitalServiceRepository, times(1)).findById(digitalServiceBO.getUid());
        verify(digitalServiceMapper, times(1)).toFullBusinessObject(digitalService);
        verify(digitalServiceMapper, times(1)).mergeEntity(digitalService, inputDigitalServiceBO, digitalServiceReferentialService);
        verify(digitalServiceRepository, times(1)).save(digitalService);
        verify(digitalServiceMapper, times(1)).toFullBusinessObject(digitalServiceUpdated);
    }

    @Test
    void whenNoChange_thenDigitalServiceEntityNotChange() {
        final String digitalServiceUid = "80651485-3f8b-49dd-a7be-753e4fe1fd36";

        final DigitalServiceBO digitalServiceBO = DigitalServiceBO.builder().uid(digitalServiceUid).build();
        final DigitalService digitalService = DigitalService.builder().uid(digitalServiceUid).build();

        when(digitalServiceRepository.findById(digitalService.getUid())).thenReturn(Optional.of(digitalService));
        when(digitalServiceMapper.toFullBusinessObject(digitalService)).thenReturn(digitalServiceBO);

        final DigitalServiceBO result = digitalServiceService.updateDigitalService(digitalServiceBO);

        assertThat(result).isEqualTo(digitalServiceBO);
        verify(digitalServiceRepository, times(1)).findById(digitalServiceBO.getUid());
        verify(digitalServiceMapper, times(1)).toFullBusinessObject(digitalService);
    }

    @Test
    void whenUpdateNotExistDigitalService_thenThrow() {
        final String digitalServiceUid = "80651485-3f8b-49dd-a7be-753e4fe1fd36";

        when(digitalServiceRepository.findById(digitalServiceUid)).thenReturn(Optional.empty());

        final DigitalServiceBO bo = DigitalServiceBO.builder().uid(digitalServiceUid).build();
        assertThatThrownBy(() -> digitalServiceService.updateDigitalService(bo))
                .hasMessageContaining("Digital Service 80651485-3f8b-49dd-a7be-753e4fe1fd36 not found.")
                .isInstanceOf(G4itRestException.class);

        verify(digitalServiceRepository, times(1)).findById(digitalServiceUid);
        verifyNoInteractions(digitalServiceMapper);
    }

    @Test
    void shouldGetDigitalService() {
        final String digitalServiceUid = "80651485-3f8b-49dd-a7be-753e4fe1fd36";

        final DigitalService digitalService = DigitalService.builder().build();
        when(digitalServiceRepository.findById(digitalServiceUid)).thenReturn(Optional.of(digitalService));
        final DigitalServiceBO digitalServiceBo = DigitalServiceBO.builder().build();
        when(digitalServiceMapper.toFullBusinessObject(digitalService)).thenReturn(digitalServiceBo);

        final DigitalServiceBO result = digitalServiceService.getDigitalService(digitalServiceUid);

        assertThat(result).isEqualTo(digitalServiceBo);

        verify(digitalServiceRepository, times(1)).findById(digitalServiceUid);
        verify(digitalServiceMapper, times(1)).toFullBusinessObject(digitalService);
    }

    @Test
    void whenGetNotExistDigitalService_thenThrow() {
        final String digitalServiceUid = "80651485-3f8b-49dd-a7be-753e4fe1fd36";

        when(digitalServiceRepository.findById(digitalServiceUid)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> digitalServiceService.getDigitalService(digitalServiceUid))
                .hasMessageContaining("Digital Service 80651485-3f8b-49dd-a7be-753e4fe1fd36 not found.")
                .isInstanceOf(G4itRestException.class);

        verify(digitalServiceRepository, times(1)).findById(digitalServiceUid);
        verifyNoInteractions(digitalServiceMapper);
    }

    @Test
    void testCalculateQuantityForTerminal() throws Exception {
        final Method method = DigitalServiceService.class.getDeclaredMethod("calculateQuantity", TerminalBO.class);
        method.setAccessible(true);

        final TerminalBO terminal = TerminalBO.builder().yearlyUsageTimePerUser(90).numberOfUsers(53804).build();
        final Double result = (Double) method.invoke(digitalServiceService, terminal);
        assertThat(result).isEqualTo((Double) 552.7808219178082);
        assertThat(String.valueOf(result)).isEqualTo("552.7808219178082");
    }

    @Test
    void shouldRunCalculation() {

        final String organizationName = "test";
        final String digitalServiceUid = "80651485-3f8b-49dd-a7be-753e4fe1fd36";

        doNothing().when(indicatorService).deleteIndicators(organizationName, digitalServiceUid);
        final DigitalService digitalService = DigitalService.builder().uid(digitalServiceUid).build();
        when(digitalServiceRepository.findById(digitalServiceUid)).thenReturn(Optional.of(digitalService));
        final DigitalServiceBO digitalServiceBo = DigitalServiceBO.builder()
                .terminals(List.of(TerminalBO.builder()
                        .type(DeviceTypeBO.builder().code("code").value("value").build())
                        .country("France")
                        .uid("uid")
                        .yearlyUsageTimePerUser(15)
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
                .uid(digitalServiceUid)
                .build();
        when(digitalServiceMapper.toFullBusinessObject(digitalService)).thenReturn(digitalServiceBo);
        when(digitalServiceReferentialService.getServerHost(1)).thenReturn(ServerHostRef.builder().reference("Reference").build());

        when(fileInfo.getMapping(FileType.EQUIPEMENT_PHYSIQUE)).thenReturn(List.of(Header.builder().build()));
        ReflectionTestUtils.setField(digitalServiceService, "localWorkingPath", "target/");

        lenient().doNothing()
                .when(numEcoEvalRemotingService).callInputDataExposition(eq(null), any(FileSystemResource.class), eq(null), eq(null),
                        eq(organizationName), eq(""), eq(digitalServiceUid));
        doNothing().when(numEcoEvalRemotingService).callCalculation(digitalServiceUid, ModeRest.SYNC);

        when(digitalServiceReferentialService.getTerminalDeviceType("code")).thenReturn(DeviceTypeRef.builder().lifespan(1.5d).build());
        when(digitalServiceReferentialService.getNetworkType("code")).thenReturn(NetworkTypeRef.builder().annualQuantityOfGo(10).build());
        when(digitalServiceRepository.save(digitalService)).thenReturn(digitalService);

        digitalServiceService.runCalculations(organizationName, digitalServiceUid);

        verify(indicatorService, times(1)).deleteIndicators(organizationName, digitalServiceUid);
        verify(digitalServiceRepository, times(2)).findById(digitalServiceUid);
        verify(digitalServiceMapper, times(1)).toFullBusinessObject(digitalService);
        verify(fileInfo, times(1)).getMapping(FileType.EQUIPEMENT_PHYSIQUE);
        verify(numEcoEvalRemotingService, times(1)).callInputDataExposition(any(), any(), any(), any(), any(), any(), any());
        verify(numEcoEvalRemotingService, times(1)).callCalculation(digitalServiceUid, ModeRest.SYNC);
        verify(digitalServiceRepository, times(1)).save(digitalService);
        verify(digitalServiceReferentialService, times(1)).getTerminalDeviceType("code");
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
        final String digitalServiceUid = "ac2b7152-4540-4757-bced-f11986447449";

        List<DatacenterDigitalService> dcEntity = List.of(DatacenterDigitalService
                .builder().name("Saved datacenter").pue(new BigDecimal("3.5")).location("UK").build());
        when(datacenterDigitalServiceRepository.findByDigitalServiceUid(digitalServiceUid)).thenReturn(dcEntity);
        when(datacenterDigitalServiceMapper.toBusinessObject(dcEntity)).thenReturn(List.of(
                ServerDataCenterBO.builder().name("Saved datacenter").pue(new BigDecimal("3.5")).location("UK").build()));

        final List<ServerDataCenterBO> result = digitalServiceService.getDigitalServiceDataCenter(digitalServiceUid);

        assertThat(result).hasSize(1).satisfiesExactly(
                dc -> assertThat(dc)
                        .extracting("name", "location", "pue")
                        .contains("Saved datacenter", "UK", new BigDecimal("3.5"))
        );

        verify(datacenterDigitalServiceRepository, times(1)).findByDigitalServiceUid(digitalServiceUid);
        verify(datacenterDigitalServiceMapper, times(1)).toBusinessObject(dcEntity);
    }

}
