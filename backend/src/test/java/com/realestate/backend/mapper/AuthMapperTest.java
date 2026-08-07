package com.realestate.backend.mapper;

import com.realestate.backend.dto.request.AgencyOwnerRegisterRequest;
import com.realestate.backend.dto.request.UserRegisterRequest;
import com.realestate.backend.entity.AgencyEntity;
import com.realestate.backend.entity.UserEntity;
import com.realestate.backend.enums.AgencyStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.assertj.core.api.Assertions.assertThat;

class AuthMapperTest {

    private AuthMapper authMapper;

    @BeforeEach
    void setUp() {
        // Direct instantiation of the MapStruct compiled implementation class
        authMapper = Mappers.getMapper(AuthMapper.class);
    }

    @Test
    @DisplayName("Should cleanly map UserRegisterRequest to UserEntity with normalized values")
    void shouldMapUserRegisterRequestToUserEntity() {
        // Arrange
        UserRegisterRequest request = UserRegisterRequest.builder()
                .fullName("   Jane Doe   ")
                .email("  Jane.DOE@Example.com   ")
                .phoneNumber("  +123456789  ")
                .password("rawPassword123")
                .build();

        // Act
        UserEntity entity = authMapper.toUserEntity(request);

        // Assert
        assertThat(entity).isNotNull();
        assertThat(entity.getFullName()).isEqualTo("Jane Doe");               // Trimmed
        assertThat(entity.getEmail()).isEqualTo("jane.doe@example.com");       // Trimmed & Lowercased
        assertThat(entity.getPhoneNumber()).isEqualTo("+123456789");           // Trimmed

        // Assert hardcoded MapStruct constants
        assertThat(entity.getEnabled()).isTrue();
        assertThat(entity.isEmailVerified()).isFalse();
        assertThat(entity.getRoles()).isNotNull().isEmpty();
    }

    @Test
    @DisplayName("Should map AgencyOwnerRegisterRequest to UserEntity correctly")
    void shouldMapAgencyOwnerToUserEntity() {
        // Arrange
        AgencyOwnerRegisterRequest request = AgencyOwnerRegisterRequest.builder()
                .fullName("  Owner Name  ")
                .email("OWNER@agency.com")
                .businessPhone("   +994500000000  ")
                .build();

        // Act
        UserEntity entity = authMapper.toAgencyOwnerUser(request);

        // Assert
        assertThat(entity).isNotNull();
        assertThat(entity.getFullName()).isEqualTo("Owner Name");
        assertThat(entity.getEmail()).isEqualTo("owner@agency.com");
        assertThat(entity.getPhoneNumber()).isEqualTo("+994500000000"); // Maps source businessPhone -> phoneNumber
        assertThat(entity.getEnabled()).isTrue();
        assertThat(entity.isEmailVerified()).isFalse();
    }

    @Test
    @DisplayName("Should map AgencyOwnerRegisterRequest to AgencyEntity with PENDING status")
    void shouldMapAgencyOwnerToAgencyEntity() {
        // Arrange
        AgencyOwnerRegisterRequest request = AgencyOwnerRegisterRequest.builder()
                .agencyName("   Apex Realty   ")
                .agencyDescription("Leading agency property group.  ")
                .businessPhone("+994120000000")
                .agencyWebsiteUrl("  ://apexrealty.com  ")
                .email("Contact@ApexRealty.com")
                .city("Baku")
                .address("Nizami Street 42")
                .build();

        // Act
        AgencyEntity entity = authMapper.toAgencyEntity(request);

        // Assert
        assertThat(entity).isNotNull();
        assertThat(entity.getName()).isEqualTo("Apex Realty");
        assertThat(entity.getDescription()).isEqualTo("Leading agency property group.");
        assertThat(entity.getPhoneNumber()).isEqualTo("+994120000000");
        assertThat(entity.getWebsite()).isEqualTo("://apexrealty.com");
        assertThat(entity.getEmail()).isEqualTo("contact@apexrealty.com");
        assertThat(entity.getCity()).isEqualTo("Baku");
        assertThat(entity.getAddress()).isEqualTo("Nizami Street 42");

        // Assert Enum generation statement constant
        assertThat(entity.getStatus()).isEqualTo(AgencyStatus.PENDING);
    }

    @Test
    @DisplayName("Should handle null strings safely within normalization logic")
    void shouldHandleNullValuesInNormalization() {
        // Arrange
        UserRegisterRequest request = UserRegisterRequest.builder()
                .fullName(null)
                .email("Valid@Email.com")
                .phoneNumber(null)
                .build();

        // Act
        UserEntity entity = authMapper.toUserEntity(request);

        // Assert
        assertThat(entity).isNotNull();
        assertThat(entity.getFullName()).isNull();
        assertThat(entity.getPhoneNumber()).isNull();
        assertThat(entity.getEmail()).isEqualTo("valid@email.com");
    }
}
