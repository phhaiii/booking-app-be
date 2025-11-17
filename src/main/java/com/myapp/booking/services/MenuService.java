package com.myapp.booking.services;

import com.myapp.booking.dtos.requests.CreateMenuRequest;
import com.myapp.booking.dtos.requests.UpdateMenuRequest;
import com.myapp.booking.dtos.responses.MenuResponse;
import com.myapp.booking.exceptions.BadRequestException;
import com.myapp.booking.exceptions.ForbiddenException;
import com.myapp.booking.exceptions.ResourceNotFoundException;
import com.myapp.booking.models.Menu;
import com.myapp.booking.models.Post;
import com.myapp.booking.models.User;
import com.myapp.booking.enums.RoleName;
import com.myapp.booking.repositories.MenuRepository;
import com.myapp.booking.repositories.PostRepository;
import com.myapp.booking.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MenuService {

    private final MenuRepository menuRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    // ═══════════════════════════════════════════════════════════════
    // CREATE MENU
    // ═══════════════════════════════════════════════════════════════

    @Transactional
    public MenuResponse createMenu(CreateMenuRequest request, Long postId, Long userId) {
        log.info("═══════════════════════════════════════");
        log.info("🍽️ CREATING MENU FOR POST: {}", postId);
        log.info("User ID: {}", userId);
        log.info("Menu name: {}", request.getName());
        log.info("Price: {}", request.getPrice());
        log.info("Guests per table: {}", request.getGuestsPerTable());
        log.info("═══════════════════════════════════════");

        // Validate
        validateCreateMenuRequest(request);

        // Check post exists
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + postId));

        // Check authorization
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // ✅ FIX: Check role properly
        boolean isAdmin = user.getRole() != null &&
                user.getRole().getRoleName() == RoleName.ADMIN;
        boolean isOwner = post.getVendor().getId().equals(userId);

        if (!isOwner && !isAdmin) {
            log.error("❌ User {} is not authorized to create menu for post {}", userId, postId);
            throw new ForbiddenException("You can only create menu for your own posts");
        }

        // Check duplicate name
        if (menuRepository.existsByPostIdAndNameAndIsActiveTrue(postId, request.getName())) {
            throw new BadRequestException("Menu with name '" + request.getName() + "' already exists for this post");
        }

        // Create menu
        Menu menu = Menu.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .guestsPerTable(request.getGuestsPerTable())
                .items(request.getItems())
                .post(post)
                .vendor(post.getVendor())
                .isActive(true)
                .build();

        Menu savedMenu = menuRepository.save(menu);

        log.info("✅ Menu created successfully with ID: {}", savedMenu.getId());
        log.info("Price per person: {}", savedMenu.getPricePerPerson());
        log.info("═══════════════════════════════════════");

        return mapToResponse(savedMenu);
    }

    // ═══════════════════════════════════════════════════════════════
    // UPDATE MENU
    // ═══════════════════════════════════════════════════════════════

    @Transactional
    public MenuResponse updateMenu(Long menuId, UpdateMenuRequest request, Long userId) {
        log.info("═══════════════════════════════════════");
        log.info("🔄 UPDATING MENU: {}", menuId);
        log.info("User ID: {}", userId);
        log.info("═══════════════════════════════════════");

        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new ResourceNotFoundException("Menu not found with id: " + menuId));

        // Check authorization
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // ✅ FIX: Check role properly
        boolean isAdmin = user.getRole() != null &&
                user.getRole().getRoleName() == RoleName.ADMIN;
        boolean isOwner = menu.getVendor().getId().equals(userId);

        if (!isOwner && !isAdmin) {
            log.error("❌ User {} is not authorized to update menu {}", userId, menuId);
            throw new ForbiddenException("You can only update your own menus");
        }

        // Update name with duplicate check
        if (request.getName() != null && !request.getName().equals(menu.getName())) {
            if (menuRepository.existsByPostIdAndNameAndIsActiveTrue(
                    menu.getPost().getId(), request.getName())) {
                throw new BadRequestException("Menu with name '" + request.getName() +
                        "' already exists for this post");
            }
            menu.setName(request.getName());
            log.info("Updated menu name to: {}", request.getName());
        }

        // Update description
        if (request.getDescription() != null) {
            menu.setDescription(request.getDescription());
            log.info("Updated menu description");
        }

        // Update price
        if (request.getPrice() != null) {
            if (request.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BadRequestException("Price must be greater than 0");
            }
            menu.setPrice(request.getPrice());
            log.info("Updated menu price to: {}", request.getPrice());
        }

        // Update guests per table
        if (request.getGuestsPerTable() != null) {
            if (request.getGuestsPerTable() <= 0) {
                throw new BadRequestException("Guests per table must be greater than 0");
            }
            menu.setGuestsPerTable(request.getGuestsPerTable());
            log.info("Updated guests per table to: {}", request.getGuestsPerTable());
        }

        // Update items
        if (request.getItems() != null && !request.getItems().isEmpty()) {
            menu.setItems(request.getItems());
            log.info("Updated menu items (count: {})", request.getItems().size());
        }

        // Update active status
        if (request.getIsActive() != null) {
            menu.setIsActive(request.getIsActive());
            log.info("Updated menu active status to: {}", request.getIsActive());
        }

        Menu updatedMenu = menuRepository.save(menu);

        log.info("✅ Menu updated successfully");
        log.info("New price per person: {}", updatedMenu.getPricePerPerson());
        log.info("═══════════════════════════════════════");

        return mapToResponse(updatedMenu);
    }

    // ═══════════════════════════════════════════════════════════════
    // GET MENUS
    // ═══════════════════════════════════════════════════════════════

    @Transactional(readOnly = true)
    public List<MenuResponse> getMenusByPost(Long postId) {
        log.info("📋 Getting menus for post: {}", postId);

        if (!postRepository.existsById(postId)) {
            throw new ResourceNotFoundException("Post not found with id: " + postId);
        }

        List<Menu> menus = menuRepository.findByPostIdAndIsActiveTrue(postId);

        log.info("✅ Found {} active menus for post {}", menus.size(), postId);

        return menus.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public MenuResponse getMenuById(Long menuId) {
        log.info("🔍 Getting menu by ID: {}", menuId);

        Menu menu = menuRepository.findByIdAndIsActiveTrue(menuId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Menu not found or inactive with id: " + menuId));

        log.info("✅ Found menu: {}", menu.getName());

        return mapToResponse(menu);
    }

    @Transactional(readOnly = true)
    public List<MenuResponse> getMenusByVendor(Long vendorId) {
        log.info("📋 Getting menus for vendor: {}", vendorId);

        if (!userRepository.existsById(vendorId)) {
            throw new ResourceNotFoundException("Vendor not found with id: " + vendorId);
        }

        List<Menu> menus = menuRepository.findByVendorIdAndIsActiveTrue(vendorId);

        log.info("✅ Found {} active menus for vendor {}", menus.size(), vendorId);

        return menus.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ═══════════════════════════════════════════════════════════════
    // DELETE MENU
    // ═══════════════════════════════════════════════════════════════

    @Transactional
    public void deleteMenu(Long menuId, Long userId) {
        log.info("═══════════════════════════════════════");
        log.info("🗑️ DELETING MENU: {}", menuId);
        log.info("User ID: {}", userId);
        log.info("═══════════════════════════════════════");

        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new ResourceNotFoundException("Menu not found with id: " + menuId));

        // Check authorization
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // ✅ FIX: Check role properly
        boolean isAdmin = user.getRole() != null &&
                user.getRole().getRoleName() == RoleName.ADMIN;
        boolean isOwner = menu.getVendor().getId().equals(userId);

        if (!isOwner && !isAdmin) {
            log.error("❌ User {} is not authorized to delete menu {}", userId, menuId);
            throw new ForbiddenException("You can only delete your own menus");
        }

        // Soft delete
        menu.setIsActive(false);
        menuRepository.save(menu);

        log.info("✅ Menu soft deleted successfully");
        log.info("Menu name: {}", menu.getName());
        log.info("═══════════════════════════════════════");
    }

    // ═══════════════════════════════════════════════════════════════
    // VALIDATION
    // ═══════════════════════════════════════════════════════════════

    private void validateCreateMenuRequest(CreateMenuRequest request) {
        log.debug("Validating create menu request...");

        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new BadRequestException("Menu name is required");
        }

        if (request.getName().length() < 3 || request.getName().length() > 255) {
            throw new BadRequestException("Menu name must be between 3 and 255 characters");
        }

        if (request.getPrice() == null || request.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Price must be greater than 0");
        }

        if (request.getGuestsPerTable() == null || request.getGuestsPerTable() <= 0) {
            throw new BadRequestException("Guests per table must be greater than 0");
        }

        if (request.getGuestsPerTable() > 20) {
            throw new BadRequestException("Guests per table cannot exceed 20");
        }

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new BadRequestException("Menu must have at least one item");
        }

        if (request.getItems().size() > 50) {
            throw new BadRequestException("Menu cannot have more than 50 items");
        }

        // Validate each item
        for (String item : request.getItems()) {
            if (item == null || item.trim().isEmpty()) {
                throw new BadRequestException("Menu items cannot be empty");
            }
        }

        log.debug("✅ Validation passed");
    }

    // ═══════════════════════════════════════════════════════════════
    // MAPPER
    // ═══════════════════════════════════════════════════════════════

    private MenuResponse mapToResponse(Menu menu) {
        BigDecimal pricePerPerson = menu.getPricePerPerson();

        return MenuResponse.builder()
                .id(menu.getId())
                .name(menu.getName())
                .description(menu.getDescription())
                // Main fields
                .price(menu.getPrice())
                .guestsPerTable(menu.getGuestsPerTable())
                .pricePerPerson(pricePerPerson)
                // Items
                .items(menu.getItems())
                .itemCount(menu.getItems() != null ? menu.getItems().size() : 0)
                .isActive(menu.getIsActive())
                // Post info
                .postId(menu.getPost().getId())
                .postTitle(menu.getPost().getTitle())
                // Vendor info
                .vendorId(menu.getVendor().getId())
                .vendorName(menu.getVendor().getFullName())
                .vendorEmail(menu.getVendor().getEmail())
                .vendorPhone(menu.getVendor().getPhone())
                // Timestamps
                .createdAt(menu.getCreatedAt())
                .updatedAt(menu.getUpdatedAt())
                // Formatted
                .formattedPrice(formatPrice(menu.getPrice()))
                .formattedPricePerPerson(formatPrice(pricePerPerson))
                .build();
    }

    private String formatPrice(BigDecimal price) {
        if (price == null) {
            return "0₫";
        }

        DecimalFormat df = new DecimalFormat("#,###");

        // Format millions (triệu)
        if (price.compareTo(BigDecimal.valueOf(1000000)) >= 0) {
            BigDecimal millions = price.divide(BigDecimal.valueOf(1000000), 1, RoundingMode.HALF_UP);
            return df.format(millions) + " triệu";
        }

        // Format thousands (nghìn)
        if (price.compareTo(BigDecimal.valueOf(1000)) >= 0) {
            BigDecimal thousands = price.divide(BigDecimal.valueOf(1000), 0, RoundingMode.HALF_UP);
            return df.format(thousands) + "K";
        }

        // Format regular
        return df.format(price) + "₫";
    }
}