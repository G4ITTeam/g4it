/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
package com.soprasteria.g4it.backend.apidigitalservice.business;


import com.soprasteria.g4it.backend.apidigitalservice.mapper.DigitalServiceReferentialMapper;
import com.soprasteria.g4it.backend.apidigitalservice.model.DeviceTypeBO;
import com.soprasteria.g4it.backend.apidigitalservice.model.NetworkTypeBO;
import com.soprasteria.g4it.backend.apidigitalservice.model.ServerHostBO;
import com.soprasteria.g4it.backend.apidigitalservice.modeldb.referential.DeviceTypeRef;
import com.soprasteria.g4it.backend.apidigitalservice.modeldb.referential.NetworkTypeRef;
import com.soprasteria.g4it.backend.apidigitalservice.modeldb.referential.ServerHostRef;
import com.soprasteria.g4it.backend.apidigitalservice.modeldb.referential.ServerHostRefDTO;
import com.soprasteria.g4it.backend.apidigitalservice.repository.DeviceTypeRefRepository;
import com.soprasteria.g4it.backend.apidigitalservice.repository.NetworkTypeRefRepository;
import com.soprasteria.g4it.backend.apidigitalservice.repository.ServerHostRefRepository;
import com.soprasteria.g4it.backend.exception.InvalidReferentialException;
import com.soprasteria.g4it.backend.external.numecoeval.business.NumEcoEvalReferentialRemotingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DigitalServiceReferentialServiceTest {

    @Mock
    private DeviceTypeRefRepository deviceTypeRefRepository;
    @Mock
    private NetworkTypeRefRepository networkTypeRefRepository;
    @Mock
    private ServerHostRefRepository serverHostRefRepository;
    @Mock
    private DigitalServiceReferentialMapper digitalServiceReferentialMapper;
    @Mock
    private NumEcoEvalReferentialRemotingService numEcoEvalReferentialRemotingService;

    @InjectMocks
    private DigitalServiceReferentialService digitalServiceReferentialService;

    @Test
    void shouldGetTerminalDeviceType() {
        when(deviceTypeRefRepository.findAll()).thenReturn(List.of(DeviceTypeRef.builder().build()));
        when(digitalServiceReferentialMapper.toDeviceTypeBusinessObject(anyList())).thenReturn(List.of(DeviceTypeBO.builder().build()));

        final List<DeviceTypeBO> result = digitalServiceReferentialService.getTerminalDeviceType();

        assertThat(result).hasSize(1);
        verify(deviceTypeRefRepository, times(1)).findAll();
        verify(digitalServiceReferentialMapper, times(1)).toDeviceTypeBusinessObject(anyList());
    }

    @Test
    void shouldGetTerminalDeviceType_whenTypeExist() {
        final String reference = "existing_reference";

        final DeviceTypeRef expected = DeviceTypeRef.builder().reference(reference).description("my device type").build();
        when(deviceTypeRefRepository.findByReference(reference)).thenReturn(Optional.of(expected));

        final DeviceTypeRef result = digitalServiceReferentialService.getTerminalDeviceType(reference);

        assertThat(result).isEqualTo(expected);
        verify(deviceTypeRefRepository, times(1)).findByReference(reference);
    }

    @Test
    void shouldGetTerminalDeviceType_whenTypeNotExist() {
        final String reference = "non_existing_reference";

        when(deviceTypeRefRepository.findByReference(reference)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> digitalServiceReferentialService.getTerminalDeviceType(reference))
                .isInstanceOf(InvalidReferentialException.class)
                .extracting("referentialInErrorCode").isEqualTo("terminal.type.code");

        verify(deviceTypeRefRepository, times(1)).findByReference(reference);
    }

    @Test
    void shouldGetCountry() {
        when(numEcoEvalReferentialRemotingService.getCountryList()).thenReturn(List.of("FRANCE"));

        final List<String> result = digitalServiceReferentialService.getCountry();

        assertThat(result).hasSize(1).contains("FRANCE");
        verify(numEcoEvalReferentialRemotingService, times(1)).getCountryList();
    }

    @Test
    void shouldGetNetworkType() {
        when(networkTypeRefRepository.findAll()).thenReturn(List.of(NetworkTypeRef.builder().build()));
        when(digitalServiceReferentialMapper.toNetworkTypeBusinessObject(anyList())).thenReturn(List.of(NetworkTypeBO.builder().build()));

        final List<NetworkTypeBO> result = digitalServiceReferentialService.getNetworkType();

        assertThat(result).hasSize(1);
        verify(networkTypeRefRepository, times(1)).findAll();
        verify(digitalServiceReferentialMapper, times(1)).toNetworkTypeBusinessObject(anyList());
    }

    @Test
    void shouldGetNetworkType_whenTypeExist() {
        final String reference = "existing_reference";

        final NetworkTypeRef expected = NetworkTypeRef.builder().reference(reference).description("my description").build();
        when(networkTypeRefRepository.findByReference(reference)).thenReturn(Optional.of(expected));

        NetworkTypeRef result = digitalServiceReferentialService.getNetworkType(reference);

        assertThat(result).isEqualTo(expected);
        verify(networkTypeRefRepository, times(1)).findByReference(reference);
    }

    @Test
    void shouldGetNetworkType_whenTypeNotExist() {
        final String reference = "non_existing_reference";

        when(networkTypeRefRepository.findByReference(reference)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> digitalServiceReferentialService.getNetworkType(reference))
                .isInstanceOf(InvalidReferentialException.class)
                .extracting("referentialInErrorCode").isEqualTo("network.type.code");

        verify(networkTypeRefRepository, times(1)).findByReference(reference);
    }

    @Test
    void shouldGetServerHosts_withKnownType() {
        final String knownType = "known";

        when(serverHostRefRepository.findServerHostRefByType(knownType)).thenReturn(List.of(ServerHostRefDTO.builder().build()));
        when(digitalServiceReferentialMapper.serverDTOtoServerHostBusinessObject(anyList())).thenReturn(List.of(ServerHostBO.builder().build()));

        final List<ServerHostBO> result = digitalServiceReferentialService.getServerHosts(knownType);

        assertThat(result).hasSize(1);
        verify(serverHostRefRepository, times(1)).findServerHostRefByType(knownType);
        verify(digitalServiceReferentialMapper, times(1)).serverDTOtoServerHostBusinessObject(anyList());
    }

    @Test
    void shouldGetServerHosts_withUnknownType() {
        final String unknownType = "unknown";

        when(serverHostRefRepository.findServerHostRefByType(unknownType)).thenReturn(new ArrayList<>());
        when(digitalServiceReferentialMapper.serverDTOtoServerHostBusinessObject(anyList())).thenReturn(new ArrayList<>());

        final List<ServerHostBO> result = digitalServiceReferentialService.getServerHosts(unknownType);

        assertThat(result).isEmpty();
        verify(serverHostRefRepository, times(1)).findServerHostRefByType(unknownType);
        verify(digitalServiceReferentialMapper, times(1)).serverDTOtoServerHostBusinessObject(anyList());
    }

    @Test
    void shouldGetServerHost_whenReferenceExist() {
        final long id = 1L;

        final ServerHostRef expected = ServerHostRef.builder().id(id).reference("ref").description("my device type").build();
        when(serverHostRefRepository.findById(id)).thenReturn(Optional.of(expected));

        final ServerHostRef result = digitalServiceReferentialService.getServerHost(id);

        assertThat(result).isEqualTo(expected);
        verify(serverHostRefRepository, times(1)).findById(id);
    }

    @Test
    void shouldGetServerHost_whenTypeNotExist() {
        final long id = 0L;

        when(serverHostRefRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> digitalServiceReferentialService.getServerHost(id))
                .isInstanceOf(InvalidReferentialException.class)
                .extracting("referentialInErrorCode").isEqualTo("server.host.code");

        verify(serverHostRefRepository, times(1)).findById(id);
        verifyNoInteractions(digitalServiceReferentialMapper);
    }
}
