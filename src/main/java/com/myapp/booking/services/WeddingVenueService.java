package com.myapp.booking.services;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.myapp.booking.dtos.requests.WeddingVenueRequest;
import com.myapp.booking.dtos.responses.WeddingVenueResponse;
import com.myapp.booking.models.WeddingVenues;
import com.myapp.booking.repositories.WeddingVenueRepository;
import com.myapp.booking.services.interfaces.IWeddingVenueService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class WeddingVenueService implements IWeddingVenueService{

    private final WeddingVenueRepository weddingVenueRepository;
    private final Gson gson = new Gson();

    // ======================= CREATE =======================
    @Override
    public WeddingVenueResponse createVenue(WeddingVenueRequest request) {
        WeddingVenues venue = mapToEntity(request);
        WeddingVenues savedVenue = weddingVenueRepository.save(venue);
        return mapToResponse(savedVenue);
    }

    // ======================= UPDATE =======================
    @Override
    public WeddingVenueResponse updateVenue(Long id, WeddingVenueRequest request) {
        WeddingVenues venue = weddingVenueRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy địa điểm cưới với ID: " + id));

        venue.setName(request.getName());
        venue.setSlug(request.getSlug());
        venue.setDescription(request.getDescription());
        venue.setAddress(request.getAddress());
        venue.setCity(request.getCity());
        venue.setDistrict(request.getDistrict());
        venue.setLatitude(request.getLatitude());
        venue.setLongitude(request.getLongitude());
        venue.setCapacity(request.getCapacity());
        venue.setPricePerTable(request.getPricePerTable());
        venue.setDepositPercentage(request.getDepositPercentage());
        venue.setImages(gson.toJson(request.getImages()));
        venue.setAmenities(gson.toJson(request.getAmenities()));
        venue.setIsAvailable(request.getIsAvailable());
        venue.setUpdatedAt(LocalDateTime.now());

        WeddingVenues updatedVenue = weddingVenueRepository.save(venue);
        return mapToResponse(updatedVenue);
    }

    // ======================= READ =======================
    @Override
    public List<WeddingVenueResponse> getAllVenues() {
        return weddingVenueRepository.findByIsAvailableTrueAndDeletedAtIsNull()
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public List<WeddingVenueResponse> getVenuesByVendor(Long vendorId) {
        return weddingVenueRepository.findByVendorIdAndDeletedAtIsNull(vendorId)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public List<WeddingVenueResponse> searchVenuesByName(String name) {
        return weddingVenueRepository.findByNameContainingIgnoreCaseAndDeletedAtIsNull(name)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public Optional<WeddingVenueResponse> getVenueById(Long id) {
        return weddingVenueRepository.findById(id)
                .filter(v -> v.getDeletedAt() == null)
                .map(this::mapToResponse);
    }

    @Override
    public Optional<WeddingVenueResponse> getVenueBySlug(String slug) {
        return weddingVenueRepository.findBySlugAndDeletedAtIsNull(slug)
                .map(this::mapToResponse);
    }

    // ======================= DELETE (Soft Delete) =======================
    @Override
    public void deleteVenue(Long id) {
        WeddingVenues venue = weddingVenueRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy địa điểm cưới với ID: " + id));
        venue.setDeletedAt(LocalDateTime.now());
        weddingVenueRepository.save(venue);
    }

    // ======================= UPDATE STATUS =======================
    @Override
    public void updateAvailability(Long id, Boolean isAvailable) {
        WeddingVenues venue = weddingVenueRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy địa điểm cưới với ID: " + id));
        venue.setIsAvailable(isAvailable);
        venue.setUpdatedAt(LocalDateTime.now());
        weddingVenueRepository.save(venue);
    }

    // ======================= MAPPERS =======================
    private WeddingVenues mapToEntity(WeddingVenueRequest request) {
        return WeddingVenues.builder()
                .vendorId(request.getVendorId())
                .name(request.getName())
                .slug(request.getSlug())
                .description(request.getDescription())
                .address(request.getAddress())
                .city(request.getCity())
                .district(request.getDistrict())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .capacity(request.getCapacity())
                .pricePerTable(request.getPricePerTable())
                .depositPercentage(request.getDepositPercentage())
                .images(gson.toJson(request.getImages()))
                .amenities(gson.toJson(request.getAmenities()))
                .isAvailable(request.getIsAvailable() != null ? request.getIsAvailable() : true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private WeddingVenueResponse mapToResponse(WeddingVenues venue) {
        Type listType = new TypeToken<List<String>>() {}.getType();
        List<String> images = venue.getImages() != null
                ? gson.fromJson(venue.getImages(), listType)
                : Collections.emptyList();
        List<String> amenities = venue.getAmenities() != null
                ? gson.fromJson(venue.getAmenities(), listType)
                : Collections.emptyList();

        return WeddingVenueResponse.builder()
                .id(venue.getId())
                .vendorId(venue.getVendorId())
                .vendorName(venue.getVendor() != null ? venue.getVendor().getFullName() : null)
                .name(venue.getName())
                .slug(venue.getSlug())
                .description(venue.getDescription())
                .address(venue.getAddress())
                .city(venue.getCity())
                .district(venue.getDistrict())
                .latitude(venue.getLatitude())
                .longitude(venue.getLongitude())
                .capacity(venue.getCapacity())
                .pricePerTable(venue.getPricePerTable())
                .depositPercentage(venue.getDepositPercentage())
                .images(images)
                .amenities(amenities)
                .isAvailable(venue.getIsAvailable())
                .rating(venue.getRating())
                .reviewCount(venue.getReviewCount())
                .viewCount(venue.getViewCount())
                .createdAt(venue.getCreatedAt())
                .updatedAt(venue.getUpdatedAt())
                .build();
    }
}

