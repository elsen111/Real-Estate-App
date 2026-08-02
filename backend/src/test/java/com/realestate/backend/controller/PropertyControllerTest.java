package com.realestate.backend.controller;

import com.realestate.backend.common.response.ApiResponse;
import com.realestate.backend.dto.request.CreateAppointmentRequest;
import com.realestate.backend.dto.request.CreateInquiryRequest;
import com.realestate.backend.dto.request.PropertyMapFilterRequest;
import com.realestate.backend.dto.request.PropertyPublicFilterRequest;
import com.realestate.backend.dto.request.PropertyRequest;
import com.realestate.backend.dto.request.PropertyStatusRequest;
import com.realestate.backend.dto.response.AppointmentResponse;
import com.realestate.backend.dto.response.InquiryResponse;
import com.realestate.backend.dto.response.PropertyDetailResponse;
import com.realestate.backend.dto.response.PropertyMapResponse;
import com.realestate.backend.dto.response.PropertyMediaResponse;
import com.realestate.backend.dto.response.PropertyResponse;
import com.realestate.backend.dto.response.PropertySearchSuggestionResponse;
import com.realestate.backend.dto.response.SetPropertyMediaResponse;
import com.realestate.backend.enums.InquiryType;
import com.realestate.backend.enums.ListingType;
import com.realestate.backend.enums.PropertyStatus;
import com.realestate.backend.security.CustomUserDetails;
import com.realestate.backend.service.AppointmentService;
import com.realestate.backend.service.InquiryService;
import com.realestate.backend.service.PropertyService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PropertyControllerTest {

    @Mock
    private PropertyService propertyService;

    @Mock
    private InquiryService inquiryService;

    @Mock
    private AppointmentService appointmentService;

    @InjectMocks
    private PropertyController controller;

    private final CustomUserDetails currentUser = mock(CustomUserDetails.class);

    private PropertyRequest buildValidPropertyRequest() {
        PropertyRequest request = new PropertyRequest();
        request.setTitle("3-room apartment");
        request.setDescription("Spacious apartment in city center");
        request.setCategoryId(UUID.randomUUID());
        request.setPrice(BigDecimal.valueOf(150000));
        request.setCity("Baku");
        request.setAddress("Nizami St. 10");
        request.setListingType(ListingType.SALE);
        request.setArea(BigDecimal.valueOf(85));
        request.setRooms(3);
        request.setBathrooms(1);
        request.setFloor(4);
        request.setTotalFloors(9);
        request.setLatitude(BigDecimal.valueOf(40.4093));
        request.setLongitude(BigDecimal.valueOf(49.8671));
        return request;
    }

    private PropertyResponse buildPropertyResponse(UUID id) {
        return PropertyResponse.builder()
                .id(id)
                .title("3-room apartment")
                .city("Baku")
                .price(BigDecimal.valueOf(150000))
                .status(PropertyStatus.PENDING)
                .build();
    }

    @Test
    void createProperty_returnsOk_withCreatedProperty() {
        PropertyRequest request = buildValidPropertyRequest();
        PropertyResponse expected = buildPropertyResponse(UUID.randomUUID());

        when(propertyService.createProperty(request, currentUser)).thenReturn(expected);

        ResponseEntity<ApiResponse<PropertyResponse>> response =
                controller.createProperty(request, currentUser);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getMessage()).isEqualTo("Property created successfully");
        assertThat(response.getBody().getData()).isEqualTo(expected);

        verify(propertyService).createProperty(request, currentUser);
        verifyNoMoreInteractions(propertyService, inquiryService, appointmentService);
    }

    @Test
    void getAllPublicProperties_returnsOk_withPropertyPage() {
        PropertyPublicFilterRequest filter = new PropertyPublicFilterRequest();
        filter.setCity("Baku");
        Pageable pageable = Pageable.ofSize(10);

        Page<PropertyResponse> page = new PageImpl<>(List.of(buildPropertyResponse(UUID.randomUUID())));

        when(propertyService.getAllPublicProperties(filter, pageable)).thenReturn(page);

        ResponseEntity<ApiResponse<Page<PropertyResponse>>> response =
                controller.getAllPublicProperties(filter, pageable);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Properties fetched successfully");
        assertThat(response.getBody().getData().getContent()).hasSize(1);

        verify(propertyService).getAllPublicProperties(filter, pageable);
    }

    @Test
    void getPropertyById_returnsOk_withPropertyDetail() {
        UUID propertyId = UUID.randomUUID();
        PropertyDetailResponse expected = PropertyDetailResponse.builder()
                .id(propertyId)
                .title("3-room apartment")
                .propertyStatus(PropertyStatus.ACTIVE)
                .build();

        when(propertyService.getPropertyDetailsById(propertyId, currentUser)).thenReturn(expected);

        ResponseEntity<ApiResponse<PropertyDetailResponse>> response =
                controller.getPropertyById(propertyId, currentUser);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Property details fetched successfully");
        assertThat(response.getBody().getData()).isEqualTo(expected);

        verify(propertyService).getPropertyDetailsById(propertyId, currentUser);
    }

    @Test
    void updateProperty_returnsOk_withUpdatedProperty() {
        UUID propertyId = UUID.randomUUID();
        PropertyRequest request = buildValidPropertyRequest();
        PropertyResponse expected = buildPropertyResponse(propertyId);

        when(propertyService.updateProperty(propertyId, request, currentUser)).thenReturn(expected);

        ResponseEntity<ApiResponse<PropertyResponse>> response =
                controller.updateProperty(request, propertyId, currentUser);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Property updated successfully");
        assertThat(response.getBody().getData()).isEqualTo(expected);

        verify(propertyService).updateProperty(propertyId, request, currentUser);
    }

    @Test
    void updatePropertyStatus_returnsOk_withDynamicMessage() {
        UUID propertyId = UUID.randomUUID();
        PropertyStatusRequest request = new PropertyStatusRequest();
        request.setStatus(PropertyStatus.ACTIVE);

        ResponseEntity<ApiResponse<Void>> response =
                controller.updatePropertyStatus(request, propertyId, currentUser);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getMessage()).isEqualTo("Property status successfully changed to ACTIVE");
        assertThat(response.getBody().getData()).isNull();

        verify(propertyService).updateStatus(propertyId, request, currentUser);
    }

    @Test
    void toggleFeatured_returnsOk_withUpdatedProperty() {
        UUID propertyId = UUID.randomUUID();
        PropertyResponse expected = buildPropertyResponse(propertyId);

        when(propertyService.toggleFeaturedProperty(propertyId, currentUser)).thenReturn(expected);

        ResponseEntity<ApiResponse<PropertyResponse>> response =
                controller.toggleFeatured(propertyId, currentUser);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Property updated successfully");
        assertThat(response.getBody().getData()).isEqualTo(expected);

        verify(propertyService).toggleFeaturedProperty(propertyId, currentUser);
    }

    @Test
    void softDeleteProperty_returnsOk_withNoData() {
        UUID propertyId = UUID.randomUUID();

        ResponseEntity<ApiResponse<Void>> response =
                controller.softDeleteProperty(propertyId, currentUser);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Property deleted successfully");
        assertThat(response.getBody().getData()).isNull();

        verify(propertyService).softDeleteProperty(propertyId, currentUser);
    }

    @Test
    void getFeaturedProperties_returnsOk_withFeaturedPropertyPage() {
        PropertyPublicFilterRequest filter = new PropertyPublicFilterRequest();
        filter.setFeatured(true);
        Pageable pageable = Pageable.ofSize(10);

        Page<PropertyResponse> page = new PageImpl<>(List.of(buildPropertyResponse(UUID.randomUUID())));

        when(propertyService.getFeaturedProperties(filter, pageable)).thenReturn(page);

        ResponseEntity<ApiResponse<Page<PropertyResponse>>> response =
                controller.getFeaturedProperties(filter, pageable);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Featured properties fetched successfully");
        assertThat(response.getBody().getData().getContent()).hasSize(1);

        verify(propertyService).getFeaturedProperties(filter, pageable);
    }

    @Test
    void getRecentProperties_returnsOk_withDefaultSize() {
        PropertyPublicFilterRequest filter = new PropertyPublicFilterRequest();

        Page<PropertyResponse> page = new PageImpl<>(List.of(buildPropertyResponse(UUID.randomUUID())));

        when(propertyService.getRecentProperties(filter, 8)).thenReturn(page);

        ResponseEntity<ApiResponse<Page<PropertyResponse>>> response =
                controller.getRecentProperties(filter, 8);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Recent properties fetched successfully");
        assertThat(response.getBody().getData().getContent()).hasSize(1);

        verify(propertyService).getRecentProperties(filter, 8);
    }

    @Test
    void getSimilarProperties_returnsOk_withSimilarPropertyPage() {
        UUID propertyId = UUID.randomUUID();
        Pageable pageable = Pageable.ofSize(10);

        Page<PropertyResponse> page = new PageImpl<>(List.of(buildPropertyResponse(UUID.randomUUID())));

        when(propertyService.getSimilarProperties(propertyId, pageable)).thenReturn(page);

        ResponseEntity<ApiResponse<Page<PropertyResponse>>> response =
                controller.getSimilarProperties(propertyId, pageable);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Similar properties fetched successfully");
        assertThat(response.getBody().getData().getContent()).hasSize(1);

        verify(propertyService).getSimilarProperties(propertyId, pageable);
    }

    @Test
    void getSuggestedProperties_returnsOk_withSuggestions() {
        String keyword = "apart";
        PropertySearchSuggestionResponse expected = PropertySearchSuggestionResponse.builder()
                .properties(List.of())
                .cities(List.of("Baku"))
                .districts(List.of("Nasimi"))
                .build();

        when(propertyService.getSearchSuggestions(keyword)).thenReturn(expected);

        ResponseEntity<ApiResponse<PropertySearchSuggestionResponse>> response =
                controller.getSuggestedProperties(keyword);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Suggested properties fetched successfully");
        assertThat(response.getBody().getData()).isEqualTo(expected);

        verify(propertyService).getSearchSuggestions(keyword);
    }

    @Test
    void getMapProperties_returnsOk_withMapPropertyPage() {
        PropertyMapFilterRequest filter = new PropertyMapFilterRequest();
        filter.setCity("Baku");
        Pageable pageable = Pageable.ofSize(100);

        Page<PropertyMapResponse> page = new PageImpl<>(List.of(
                PropertyMapResponse.builder()
                        .id(UUID.randomUUID())
                        .title("3-room apartment")
                        .latitude(BigDecimal.valueOf(40.4093))
                        .longitude(BigDecimal.valueOf(49.8671))
                        .build()
        ));

        when(propertyService.getMapProperties(filter, pageable)).thenReturn(page);

        ResponseEntity<ApiResponse<Page<PropertyMapResponse>>> response =
                controller.getMapProperties(filter, pageable);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Properties map details fetched successfully");
        assertThat(response.getBody().getData().getContent()).hasSize(1);

        verify(propertyService).getMapProperties(filter, pageable);
    }

    @Test
    void uploadPropertyMedia_returnsOk_withUploadedMedia() {
        UUID propertyId = UUID.randomUUID();
        List<MultipartFile> files = List.of(
                new MockMultipartFile("files", "photo1.jpg", "image/jpeg", "photo1-bytes".getBytes())
        );

        List<PropertyMediaResponse> expected = List.of(
                PropertyMediaResponse.builder()
                        .id(UUID.randomUUID())
                        .fileUrl("https://cdn.example.com/photo1.jpg")
                        .fileName("photo1.jpg")
                        .fileType("image/jpeg")
                        .fileSize(1024L)
                        .isPrimary(true)
                        .sortOrder(0)
                        .build()
        );

        when(propertyService.uploadMedia(propertyId, files, currentUser)).thenReturn(expected);

        ResponseEntity<ApiResponse<List<PropertyMediaResponse>>> response =
                controller.uploadPropertyMedia(propertyId, currentUser, files);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Property media files uploaded successfully");
        assertThat(response.getBody().getData()).isEqualTo(expected);

        verify(propertyService).uploadMedia(propertyId, files, currentUser);
    }

    @Test
    void getPropertyMedia_returnsOk_withMediaList() {
        UUID propertyId = UUID.randomUUID();
        List<PropertyMediaResponse> expected = List.of(
                PropertyMediaResponse.builder()
                        .id(UUID.randomUUID())
                        .fileUrl("https://cdn.example.com/photo1.jpg")
                        .isPrimary(true)
                        .build()
        );

        when(propertyService.getPropertyMedia(propertyId)).thenReturn(expected);

        ResponseEntity<ApiResponse<List<PropertyMediaResponse>>> response =
                controller.getPropertyMedia(propertyId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Property media files fetched successfully");
        assertThat(response.getBody().getData()).isEqualTo(expected);

        verify(propertyService).getPropertyMedia(propertyId);
    }

    @Test
    void setPrimaryImage_returnsOk_withUpdatedMediaList() {
        UUID propertyId = UUID.randomUUID();
        UUID mediaId = UUID.randomUUID();

        List<SetPropertyMediaResponse> expected = List.of(
                new SetPropertyMediaResponse(mediaId, true, 0)
        );

        when(propertyService.setPrimaryImage(propertyId, mediaId, currentUser)).thenReturn(expected);

        ResponseEntity<ApiResponse<List<SetPropertyMediaResponse>>> response =
                controller.setPrimaryImage(propertyId, mediaId, currentUser);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Media has been set as primary");
        assertThat(response.getBody().getData()).isEqualTo(expected);

        verify(propertyService).setPrimaryImage(propertyId, mediaId, currentUser);
    }

    @Test
    void deleteImage_returnsOk_withNoData() {
        UUID mediaId = UUID.randomUUID();

        ResponseEntity<ApiResponse<Void>> response =
                controller.deleteImage(mediaId, currentUser);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Media image deleted successfully");
        assertThat(response.getBody().getData()).isNull();

        verify(propertyService).removePropertyMediaFile(currentUser, mediaId);
    }

    @Test
    void createInquiry_returnsOk_withCreatedInquiry() {
        UUID propertyId = UUID.randomUUID();
        CreateInquiryRequest request = new CreateInquiryRequest();
        request.setMessage("I'm interested in this property, please contact me.");
        request.setPreferredContactMethod(InquiryType.EMAIL);

        InquiryResponse expected = InquiryResponse.builder()
                .id(UUID.randomUUID())
                .propertyId(propertyId)
                .message(request.getMessage())
                .build();

        when(inquiryService.createInquiry(propertyId, request, currentUser)).thenReturn(expected);

        ResponseEntity<ApiResponse<InquiryResponse>> response =
                controller.createInquiry(propertyId, request, currentUser);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Inquiry created successfully");
        assertThat(response.getBody().getData()).isEqualTo(expected);

        verify(inquiryService).createInquiry(propertyId, request, currentUser);
        verifyNoMoreInteractions(inquiryService);
    }

    @Test
    void createAppointment_returnsOk_withCreatedAppointment() {
        UUID propertyId = UUID.randomUUID();
        CreateAppointmentRequest request = new CreateAppointmentRequest();
        request.setNote("Would like to visit this weekend.");
        request.setPreferredDateTime(LocalDateTime.now().plusDays(2));

        AppointmentResponse expected = AppointmentResponse.builder()
                .id(UUID.randomUUID())
                .propertyId(propertyId)
                .note(request.getNote())
                .build();

        when(appointmentService.createAppointment(propertyId, request, currentUser)).thenReturn(expected);

        ResponseEntity<ApiResponse<AppointmentResponse>> response =
                controller.createAppointment(propertyId, request, currentUser);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Appointment created successfully");
        assertThat(response.getBody().getData()).isEqualTo(expected);

        verify(appointmentService).createAppointment(propertyId, request, currentUser);
        verifyNoMoreInteractions(appointmentService);
    }
}