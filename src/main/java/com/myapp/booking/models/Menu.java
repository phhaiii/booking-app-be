package com.myapp.booking.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "menus",
        indexes = {
                @Index(name = "idx_menus_post", columnList = "id"),
                @Index(name = "idx_menus_vendor", columnList = "vendor_id"),
                @Index(name = "idx_menus_active", columnList = "is_active")
        }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"post", "vendor"})
@EqualsAndHashCode(exclude = {"post", "vendor"})
public class Menu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "category", length = 50)
    @Builder.Default
    private String category = "STANDARD";

    @Column(nullable = false, length = 255)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    // ✅ CHỈ 2 FIELD CHÍNH
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price; // Giá tổng của menu

    @Column(name = "guests_per_table", nullable = false)
    @Builder.Default
    private Integer guestsPerTable = 10; // Số khách trên 1 bàn

    // ✅ Danh sách món ăn
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "menu_items",
            joinColumns = @JoinColumn(name = "menu_id")
    )
    @Column(name = "item", length = 255)
    @OrderColumn(name = "item_order")
    @Builder.Default
    private List<String> items = new ArrayList<>();

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    // ═══════════════════════════════════════════════════════════════
    // RELATIONSHIPS
    // ═══════════════════════════════════════════════════════════════

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id", nullable = false)
    private User vendor;

    // ═══════════════════════════════════════════════════════════════
    // TIMESTAMPS
    // ═══════════════════════════════════════════════════════════════

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ═══════════════════════════════════════════════════════════════
    // HELPER METHODS
    // ═══════════════════════════════════════════════════════════════

    /**
     * Set vendor from post automatically
     */
    public void setVendorFromPost() {
        if (this.post != null && this.post.getVendor() != null) {
            this.vendor = this.post.getVendor();
        }
    }

    /**
     * Calculate price per person
     */
    public BigDecimal getPricePerPerson() {
        if (price == null || guestsPerTable == null || guestsPerTable == 0) {
            return BigDecimal.ZERO;
        }
        return price.divide(BigDecimal.valueOf(guestsPerTable), 2, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * Calculate total price for number of tables
     */
    public BigDecimal calculateTotalPrice(int numberOfTables) {
        if (price == null) {
            return BigDecimal.ZERO;
        }
        return price.multiply(BigDecimal.valueOf(numberOfTables));
    }

    /**
     * Calculate number of tables needed for guests
     */
    public int calculateTablesNeeded(int totalGuests) {
        if (guestsPerTable == null || guestsPerTable == 0) {
            return 0;
        }
        return (int) Math.ceil((double) totalGuests / guestsPerTable);
    }

    /**
     * Add item to menu
     */
    public void addItem(String item) {
        if (this.items == null) {
            this.items = new ArrayList<>();
        }
        this.items.add(item);
    }

    /**
     * Remove item from menu
     */
    public void removeItem(String item) {
        if (this.items != null) {
            this.items.remove(item);
        }
    }
}