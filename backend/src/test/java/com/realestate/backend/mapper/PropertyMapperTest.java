package com.realestate.backend.mapper;

import com.realestate.backend.dto.request.PropertyRequest;
import com.realestate.backend.dto.response.*;
import com.realestate.backend.entity.*;
import com.realestate.backend.enums.ListingType;
import com.realestate.backend.enums.PropertyStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class PropertyMapperTest {

    private PropertyMapper propertyMapper;

    @BeforeEach
    void setup() throws Exception {
        propertyMapper = Mappers.getMapper(PropertyMapper.class);

        AgencyMapper agencyMapper = Mappers.getMapper(AgencyMapper.class);
        Field field = propertyMapper.getClass().getDeclaredField("agencyMapper");
        field.setAccessible(true);
        field.set(propertyMapper, agencyMapper);
    }

    @Test
    void shouldMapAdminPropertyResponse_withAssignedAgentName() {

        PropertyEntity property = createPropertyEntity();

        PropertyResponse response = propertyMapper.toAdminPropertyResponse(property);

        assertNotNull(response);

        assertEquals(property.getAssignedAgent().getFullName(), response.getAssignedAgentName());
        assertEquals(property.getStatus(), response.getStatus());
        assertEquals(property.getUpdatedAt(), response.getUpdatedAt());

        assertNull(response.getAgencyId());
        assertNull(response.getAgencyName());
    }

    @Test
    void shouldMapAdminPropertyResponse_whenAssignedAgentIsNull() {

        PropertyEntity property = createPropertyEntity();
        property.setAssignedAgent(null);

        PropertyResponse response = propertyMapper.toAdminPropertyResponse(property);

        assertNull(response.getAssignedAgentName());
    }

    @Test
    void shouldMapPublicAgencyPropertyResponse_ignoringStatus() {

        PropertyEntity property = createPropertyEntity();

        PropertyResponse response = propertyMapper.toPublicAgencyPropertyResponse(property);

        assertNotNull(response);
        assertNull(response.getStatus());
        assertEquals(property.getTitle(), response.getTitle());
        assertEquals(property.getViewCount(), response.getViewCount());
    }

    @Test
    void shouldMapCreateResponse_withAgencyCategoryAndAgentIds() {

        PropertyEntity property = createPropertyEntity();

        PropertyResponse response = propertyMapper.toCreateResponse(property);

        assertNotNull(response);

        assertEquals(property.getAgency().getId(), response.getAgencyId());
        assertEquals(property.getAgency().getName(), response.getAgencyName());
        assertEquals(property.getCategory().getId(), response.getCategoryId());
        assertEquals(property.getCategory().getName(), response.getCategoryName());
        assertEquals(property.getAssignedAgent().getId(), response.getAssignedAgentId());
        assertEquals(property.getAssignedAgent().getFullName(), response.getAssignedAgentName());
        assertEquals(property.getStatus(), response.getStatus());
    }

    @Test
    void shouldMapCreateResponse_whenAgencyCategoryAndAgentAreNull() {

        PropertyEntity property = createPropertyEntity();
        property.setAgency(null);
        property.setCategory(null);
        property.setAssignedAgent(null);

        PropertyResponse response = propertyMapper.toCreateResponse(property);

        assertNull(response.getAgencyId());
        assertNull(response.getAgencyName());
        assertNull(response.getCategoryId());
        assertNull(response.getCategoryName());
        assertNull(response.getAssignedAgentId());
        assertNull(response.getAssignedAgentName());
    }

    @Test
    void shouldMapDetailResponse_usingAgencyMapperAndAgentMapping() {

        PropertyEntity property = createPropertyEntity();

        PropertyDetailResponse response = propertyMapper.toDetailResponse(property);

        assertNotNull(response);

        assertEquals(property.getCategory().getName(), response.getPropertyType());
        assertEquals(property.getStatus(), response.getPropertyStatus());
        assertEquals(property.getLatitude(), response.getLatitude());
        assertEquals(property.getLongitude(), response.getLongitude());
        assertNull(response.getImages());

        assertNotNull(response.getAgency());
        assertEquals(property.getAgency().getName(), response.getAgency().getName());

        assertNotNull(response.getAgent());
        assertEquals(property.getAssignedAgent().getFullName(), response.getAgent().getFullName());
    }

    @Test
    void shouldMapDetailResponse_whenAssignedAgentIsNull() {

        PropertyEntity property = createPropertyEntity();
        property.setAssignedAgent(null);

        PropertyDetailResponse response = propertyMapper.toDetailResponse(property);

        assertNull(response.getAgent());
    }

    @Test
    void shouldMapEntity_fromPropertyRequest() {

        PropertyRequest request = createPropertyRequest();

        PropertyEntity entity = propertyMapper.toEntity(request);

        assertNotNull(entity);

        assertEquals(request.getTitle(), entity.getTitle());
        assertEquals(request.getDescription(), entity.getDescription());
        assertEquals(request.getPrice(), entity.getPrice());
        assertEquals(request.getCity(), entity.getCity());
        assertEquals(request.getDistrict(), entity.getDistrict());
        assertEquals(request.getAddress(), entity.getAddress());
        assertEquals(request.getListingType(), entity.getListingType());
        assertEquals(request.getArea(), entity.getArea());
        assertEquals(request.getRooms(), entity.getRooms());
        assertEquals(request.getBathrooms(), entity.getBathrooms());
        assertEquals(request.getFloor(), entity.getFloor());
        assertEquals(request.getTotalFloors(), entity.getTotalFloors());
        assertEquals(request.getLatitude(), entity.getLatitude());
        assertEquals(request.getLongitude(), entity.getLongitude());

        assertNull(entity.getId());
        assertNull(entity.getCategory());
        assertNull(entity.getAssignedAgent());
        assertNull(entity.getCreatedAt());
        assertNull(entity.getUpdatedAt());
        assertFalse(entity.getFeatured());
    }

    @Test
    void shouldUpdateEntityFromDto_keepingAssociationsUntouched() {

        PropertyEntity property = createPropertyEntity();
        AgencyEntity originalAgency = property.getAgency();
        CategoryEntity originalCategory = property.getCategory();
        UserEntity originalAgent = property.getAssignedAgent();

        PropertyRequest request = createPropertyRequest();
        request.setTitle("Updated Title");
        request.setCity("Sumgait");

        propertyMapper.updateEntityFromDto(request, property);

        assertEquals("Updated Title", property.getTitle());
        assertEquals("Sumgait", property.getCity());
        assertEquals(request.getPrice(), property.getPrice());
        assertEquals(request.getArea(), property.getArea());

        assertSame(originalAgency, property.getAgency());
        assertSame(originalCategory, property.getCategory());
        assertSame(originalAgent, property.getAssignedAgent());
    }

    @Test
    void shouldNotUpdateEntity_whenRequestIsNull() {

        PropertyEntity property = createPropertyEntity();
        String originalTitle = property.getTitle();

        propertyMapper.updateEntityFromDto(null, property);

        assertEquals(originalTitle, property.getTitle());
    }

    @Test
    void shouldMapPublicClientResponse_ignoringStatus() {

        PropertyEntity property = createPropertyEntity();

        PropertyResponse response = propertyMapper.toPublicClientResponse(property);

        assertNotNull(response);
        assertNull(response.getStatus());
        assertEquals(property.getAgency().getName(), response.getAgencyName());
        assertEquals(property.getCategory().getName(), response.getCategoryName());
        assertEquals(property.getAssignedAgent().getFullName(), response.getAssignedAgentName());
    }

    @Test
    void shouldMapPublicClientResponseWithImage() {

        PropertyEntity property = createPropertyEntity();

        PropertyResponse response =
                propertyMapper.toPublicClientResponseWithImage(property, "https://cdn/img.jpg");

        assertNotNull(response);
        assertEquals("https://cdn/img.jpg", response.getMainImageUrl());
        assertEquals(property.getTitle(), response.getTitle());
        assertNull(response.getStatus());
    }

    @Test
    void shouldMapAgentResponse_ignoringRolesAndAgency() {

        UserEntity agent = createAgentEntity();

        AgentResponse response = propertyMapper.toAgentResponse(agent);

        assertNotNull(response);

        assertEquals(agent.getId(), response.getId());
        assertEquals(agent.getFullName(), response.getFullName());
        assertEquals(agent.getEmail(), response.getEmail());
        assertEquals(agent.getPhoneNumber(), response.getPhoneNumber());
        assertEquals(agent.getEnabled(), response.getEnabled());
        assertEquals(agent.isEmailVerified(), response.getEmailVerified());
        assertEquals(agent.getCreatedAt(), response.getCreatedAt());

        assertNull(response.getRoles());
        assertNull(response.getAgency());
    }

    @Test
    void shouldMapSuggestionsResponse() {

        List<PropertySuggestionResponse> properties =
                List.of(new PropertySuggestionResponse(UUID.randomUUID(), "Flat A"));
        List<String> cities = List.of("Baku", "Ganja");
        List<String> districts = List.of("Yasamal");

        PropertySearchSuggestionResponse response =
                propertyMapper.toSuggestionsResponse(properties, cities, districts);

        assertNotNull(response);
        assertEquals(1, response.getProperties().size());
        assertEquals("Flat A", response.getProperties().get(0).getTitle());
        assertEquals(cities, response.getCities());
        assertEquals(districts, response.getDistricts());
    }

    @Test
    void shouldMapSuggestionsResponse_withPartiallyNullLists() {

        List<String> cities = List.of("Baku");

        PropertySearchSuggestionResponse response =
                propertyMapper.toSuggestionsResponse(null, cities, null);

        assertNull(response.getProperties());
        assertEquals(cities, response.getCities());
        assertNull(response.getDistricts());
    }

    @Test
    void shouldMapPropertyMapResponse() {

        PropertyEntity property = createPropertyEntity();

        PropertyMapResponse response = propertyMapper.toPropertyMapResponse(property);

        assertNotNull(response);
        assertEquals(property.getId(), response.getId());
        assertEquals(property.getTitle(), response.getTitle());
        assertEquals(property.getPrice(), response.getPrice());
        assertEquals(property.getListingType(), response.getListingType());
        assertEquals(property.getLatitude(), response.getLatitude());
        assertEquals(property.getLongitude(), response.getLongitude());
    }

    @Test
    void shouldMapMediaResponse_fromNestedMediaFile() {

        PropertyMediaEntity propertyMedia = createPropertyMediaEntity();

        PropertyMediaResponse response = propertyMapper.toMediaResponse(propertyMedia);

        assertNotNull(response);
        assertEquals(propertyMedia.getId(), response.id());
        assertEquals(propertyMedia.getMedia().getFileUrl(), response.fileUrl());
        assertEquals(propertyMedia.getMedia().getOriginalName(), response.fileName());
        assertEquals(propertyMedia.getMedia().getMimeType(), response.fileType());
        assertEquals(propertyMedia.getMedia().getFileSize(), response.fileSize());
        assertEquals(propertyMedia.getIsPrimary(), response.isPrimary());
        assertEquals(propertyMedia.getSortOrder(), response.sortOrder());
    }

    @Test
    void shouldMapMediaResponse_whenMediaIsNull() {

        PropertyMediaEntity propertyMedia = createPropertyMediaEntity();
        propertyMedia.setMedia(null);

        PropertyMediaResponse response = propertyMapper.toMediaResponse(propertyMedia);

        assertNull(response.fileUrl());
        assertNull(response.fileName());
        assertNull(response.fileType());
        assertNull(response.fileSize());
    }

    @Test
    void shouldMapMediaPriorityResponse() {

        PropertyMediaEntity propertyMedia = createPropertyMediaEntity();

        SetPropertyMediaResponse response = propertyMapper.toMediaPriorityResponse(propertyMedia);

        assertNotNull(response);
        assertEquals(propertyMedia.getId(), response.id());
        assertEquals(propertyMedia.getIsPrimary(), response.isPrimary());
        assertEquals(propertyMedia.getSortOrder(), response.sortOrder());
    }

    @Test
    void shouldReturnNull_whenSourceArgumentsAreNull() {

        assertNull(propertyMapper.toAdminResponse(null));
        assertNull(propertyMapper.toAdminPropertyResponse(null));
        assertNull(propertyMapper.toEntity(null));
        assertNull(propertyMapper.toAgentResponse(null));
        assertNull(propertyMapper.toPropertyMapResponse(null));
        assertNull(propertyMapper.toMediaResponse(null));
        assertNull(propertyMapper.toMediaPriorityResponse(null));
        assertNull(propertyMapper.toSuggestionsResponse(null, null, null));
    }

    // HELPERS
    private AgencyEntity createAgencyEntity() {

        AgencyEntity agency = new AgencyEntity();
        agency.setId(UUID.randomUUID());
        agency.setName("Baku Realty");

        return agency;
    }

    private CategoryEntity createCategoryEntity() {

        CategoryEntity category = new CategoryEntity();
        category.setId(UUID.randomUUID());
        category.setName("Apartment");

        return category;
    }

    private UserEntity createAgentEntity() {

        UserEntity agent = new UserEntity();
        agent.setId(UUID.randomUUID());
        agent.setFullName("Elshan Hasanov");
        agent.setEmail("agent@example.com");
        agent.setPhoneNumber("+994501112233");
        agent.setEnabled(true);
        agent.setEmailVerified(true);
        agent.setCreatedAt(LocalDateTime.now());

        return agent;
    }

    private PropertyEntity createPropertyEntity() {

        PropertyEntity property = new PropertyEntity();

        property.setId(UUID.randomUUID());
        property.setAgency(createAgencyEntity());
        property.setCategory(createCategoryEntity());
        property.setAssignedAgent(createAgentEntity());
        property.setTitle("Sea View Flat");
        property.setDescription("Nice flat");
        property.setPrice(BigDecimal.valueOf(150000));
        property.setCity("Baku");
        property.setDistrict("Yasamal");
        property.setAddress("Nizami st. 10");
        property.setListingType(ListingType.SALE);
        property.setArea(BigDecimal.valueOf(85.5));
        property.setRooms(3);
        property.setBathrooms(2);
        property.setFloor(5);
        property.setTotalFloors(12);
        property.setLatitude(BigDecimal.valueOf(40.4093));
        property.setLongitude(BigDecimal.valueOf(49.8671));
        property.setStatus(PropertyStatus.ACTIVE);
        property.setFeatured(true);
        property.setViewCount(42L);
        property.setCreatedAt(LocalDateTime.now());
        property.setUpdatedAt(LocalDateTime.now());

        return property;
    }

    private PropertyRequest createPropertyRequest() {

        PropertyRequest request = new PropertyRequest();

        request.setTitle("New Listing");
        request.setDescription("Fresh description");
        request.setCategoryId(UUID.randomUUID());
        request.setAssignedAgentId(UUID.randomUUID());
        request.setPrice(BigDecimal.valueOf(99999.99));
        request.setCity("Ganja");
        request.setDistrict("Center");
        request.setAddress("Main st. 1");
        request.setListingType(ListingType.RENT);
        request.setArea(BigDecimal.valueOf(60));
        request.setRooms(2);
        request.setBathrooms(1);
        request.setFloor(3);
        request.setTotalFloors(9);
        request.setLatitude(BigDecimal.valueOf(40.6828));
        request.setLongitude(BigDecimal.valueOf(46.3606));

        return request;
    }

    private PropertyMediaEntity createPropertyMediaEntity() {

        MediaFileEntity media = new MediaFileEntity();
        media.setId(UUID.randomUUID());
        media.setFileUrl("https://cdn/photo.jpg");
        media.setOriginalName("photo.jpg");
        media.setMimeType("image/jpeg");
        media.setFileSize(2048L);

        PropertyMediaEntity propertyMedia = new PropertyMediaEntity();
        propertyMedia.setId(UUID.randomUUID());
        propertyMedia.setMedia(media);
        propertyMedia.setIsPrimary(true);
        propertyMedia.setSortOrder(1);

        return propertyMedia;
    }
}