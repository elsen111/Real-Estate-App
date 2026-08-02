package com.realestate.backend.service;

import com.realestate.backend.entity.PropertyEntity;
import com.realestate.backend.enums.PropertyStatus;
import com.realestate.backend.exception.ResourceNotFoundException;
import com.realestate.backend.repository.PropertyRepository;
import com.realestate.backend.service.impl.AdminPropertyServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminPropertyServiceImplTest {

    @Mock private PropertyRepository propertyRepository;

    @InjectMocks private AdminPropertyServiceImpl service;

    @Test
    void changePropertyStatus_updatesStatus_whenPropertyExists() {
        UUID id = UUID.randomUUID();
        PropertyEntity property = PropertyEntity.builder().id(id).title("Sea View Villa")
                .status(PropertyStatus.ACTIVE).build();
        when(propertyRepository.findById(id)).thenReturn(Optional.of(property));

        String result = service.changePropertyStatus(id, PropertyStatus.SOLD);

        assertThat(property.getStatus()).isEqualTo(PropertyStatus.SOLD);
        assertThat(result).contains("Sea View Villa").contains("SOLD");
        verify(propertyRepository).save(property);
    }

    @Test
    void changePropertyStatus_throws_whenPropertyNotFound() {
        UUID id = UUID.randomUUID();
        when(propertyRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.changePropertyStatus(id, PropertyStatus.SOLD))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}