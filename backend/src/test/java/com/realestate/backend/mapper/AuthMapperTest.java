package com.realestate.backend.mapper;

import com.realestate.backend.dto.request.AgencyRegisterRequest;
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
        authMapper = Mappers.getMapper(AuthMapper.class);
    }

    @Test
    @DisplayName("Should cleanly map UserRegisterRequest to UserEntity with normalized values")
    void shouldMapUserRegisterRequestToUserEntity() {

        UserRegisterRequest request = UserRegisterRequest.builder()
                .fullName("   Jane Doe   ")
                .email("  Jane.DOE@Example.com   ")
                .phoneNumber("  +123456789  ")
                .password("rawPassword123")
                .build();

        UserEntity entity =
                authMapper.toUserEntity(request);

        assertThat(entity).isNotNull();

        assertThat(entity.getFullName())
                .isEqualTo("Jane Doe");

        assertThat(entity.getEmail())
                .isEqualTo("jane.doe@example.com");

        assertThat(entity.getPhoneNumber())
                .isEqualTo("+123456789");

        assertThat(entity.getEnabled())
                .isTrue();

        assertThat(entity.isEmailVerified())
                .isFalse();

        assertThat(entity.getRoles())
                .isNotNull()
                .isEmpty();

        assertThat(entity.getPasswordHash())
                .isNull();
    }

    @Test
    @DisplayName("Should map AgencyRegisterRequest to AgencyEntity with normalized values")
    void shouldMapAgencyRegisterRequestToAgencyEntity() {

        AgencyRegisterRequest request = AgencyRegisterRequest.builder()
                .agencyName("   Apex Realty   ")
                .agencyDescription("Leading agency property group.  ")
                .agencyBusinessPhone("  +994120000000  ")
                .agencyBusinessEmail("Contact@ApexRealty.com")
                .agencyWebsiteUrl("  https://apexrealty.com  ")
                .agencyCity("  Baku  ")
                .agencyAddress("  Nizami Street 42  ")
                .build();

        AgencyEntity entity =
                authMapper.toAgencyEntity(request);

        assertThat(entity).isNotNull();

        assertThat(entity.getName())
                .isEqualTo("Apex Realty");

        assertThat(entity.getDescription())
                .isEqualTo("Leading agency property group.");

        assertThat(entity.getPhoneNumber())
                .isEqualTo("+994120000000");

        assertThat(entity.getEmail())
                .isEqualTo("contact@apexrealty.com");

        assertThat(entity.getWebsite())
                .isEqualTo("https://apexrealty.com");

        assertThat(entity.getCity())
                .isEqualTo("Baku");

        assertThat(entity.getAddress())
                .isEqualTo("Nizami Street 42");

        assertThat(entity.getStatus())
                .isEqualTo(AgencyStatus.PENDING);
    }

    @Test
    @DisplayName("Should handle null strings safely within normalization logic")
    void shouldHandleNullValuesInNormalization() {

        UserRegisterRequest request = UserRegisterRequest.builder()
                .fullName(null)
                .email("Valid@Email.com")
                .phoneNumber(null)
                .password("password123")
                .build();

        UserEntity entity =
                authMapper.toUserEntity(request);

        assertThat(entity).isNotNull();

        assertThat(entity.getFullName())
                .isNull();

        assertThat(entity.getPhoneNumber())
                .isNull();

        assertThat(entity.getEmail())
                .isEqualTo("valid@email.com");
    }

    @Test
    @DisplayName("Should map optional agency fields as null")
    void shouldHandleNullAgencyFields() {

        AgencyRegisterRequest request =
                AgencyRegisterRequest.builder()
                        .agencyName("Apex Realty")
                        .agencyBusinessEmail("INFO@APEX.COM")
                        .agencyBusinessPhone("+994120000000")
                        .agencyCity("Baku")
                        .agencyAddress("Nizami Street")
                        .build();

        AgencyEntity entity =
                authMapper.toAgencyEntity(request);

        assertThat(entity).isNotNull();

        assertThat(entity.getDescription())
                .isNull();

        assertThat(entity.getWebsite())
                .isNull();

        assertThat(entity.getStatus())
                .isEqualTo(AgencyStatus.PENDING);
    }
}