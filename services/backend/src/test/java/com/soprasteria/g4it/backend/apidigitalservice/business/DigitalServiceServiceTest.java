/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apidigitalservice.business;

import com.soprasteria.g4it.backend.apidigitalservice.mapper.DigitalServiceMapper;
import com.soprasteria.g4it.backend.apidigitalservice.model.DigitalServiceBO;
import com.soprasteria.g4it.backend.apidigitalservice.modeldb.DigitalService;
import com.soprasteria.g4it.backend.apidigitalservice.modeldb.DigitalServiceLink;
import com.soprasteria.g4it.backend.apidigitalservice.modeldb.DigitalServiceShared;
import com.soprasteria.g4it.backend.apidigitalservice.repository.DigitalServiceLinkRepository;
import com.soprasteria.g4it.backend.apidigitalservice.repository.DigitalServiceRepository;
import com.soprasteria.g4it.backend.apidigitalservice.repository.DigitalServiceSharedRepository;
import com.soprasteria.g4it.backend.apiindicator.business.IndicatorService;
import com.soprasteria.g4it.backend.apiinout.repository.InVirtualEquipmentRepository;
import com.soprasteria.g4it.backend.apiuser.business.OrganizationService;
import com.soprasteria.g4it.backend.apiuser.model.UserBO;
import com.soprasteria.g4it.backend.apiuser.modeldb.Organization;
import com.soprasteria.g4it.backend.apiuser.modeldb.User;
import com.soprasteria.g4it.backend.apiuser.repository.SubscriberRepository;
import com.soprasteria.g4it.backend.apiuser.repository.UserRepository;
import com.soprasteria.g4it.backend.common.criteria.CriteriaService;
import com.soprasteria.g4it.backend.common.filesystem.model.FileMapperInfo;
import com.soprasteria.g4it.backend.common.task.business.TaskService;
import com.soprasteria.g4it.backend.exception.G4itRestException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
    private UserRepository userRepository;
    @Mock
    private TaskService taskService;
    @Mock
    private SubscriberRepository subscriberRepository;
    @Mock
    private DigitalServiceLinkRepository digitalServiceLinkRepository;
    @Mock
    private DigitalServiceSharedRepository digitalServiceSharedRepository;

    @Mock
    private DigitalServiceMapper digitalServiceMapper;
    @Mock
    private DigitalServiceReferentialService digitalServiceReferentialService;
    @Mock
    private IndicatorService indicatorService;
    @Mock
    private FileMapperInfo fileInfo;
    @Mock
    private InVirtualEquipmentRepository inVirtualEquipmentRepository;
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
