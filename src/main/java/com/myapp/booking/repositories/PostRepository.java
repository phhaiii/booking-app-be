package com.myapp.booking.repositories;

import com.myapp.booking.models.Post;
import com.myapp.booking.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    // Find by vendor
    Page<Post> findByVendorAndIsDeletedFalse(User vendor, Pageable pageable);

    List<Post> findByVendorAndIsDeletedFalse(User vendor);

    // Find by status
    Page<Post> findByStatusAndIsDeletedFalse(Post.PostStatus status, Pageable pageable);

    // Find published posts
    @Query("SELECT p FROM Post p WHERE p.status = 'PUBLISHED' AND p.isActive = true AND p.isDeleted = false")
    Page<Post> findAllPublishedPosts(Pageable pageable);

    // Find by vendor and status
    Page<Post> findByVendorAndStatusAndIsDeletedFalse(User vendor, Post.PostStatus status, Pageable pageable);

    // Find by id and not deleted
    Optional<Post> findByIdAndIsDeletedFalse(Long id);

    // Search posts
    @Query("SELECT p FROM Post p WHERE " +
            "(LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.location) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
            "p.status = 'PUBLISHED' AND p.isActive = true AND p.isDeleted = false")
    Page<Post> searchPosts(@Param("keyword") String keyword, Pageable pageable);

    // Filter by price range
    @Query("SELECT p FROM Post p WHERE " +
            "p.price BETWEEN :minPrice AND :maxPrice AND " +
            "p.status = 'PUBLISHED' AND p.isActive = true AND p.isDeleted = false")
    Page<Post> findByPriceRange(@Param("minPrice") BigDecimal minPrice,
                                @Param("maxPrice") BigDecimal maxPrice,
                                Pageable pageable);

    // Filter by capacity
    @Query("SELECT p FROM Post p WHERE " +
            "p.capacity >= :minCapacity AND " +
            "p.status = 'PUBLISHED' AND p.isActive = true AND p.isDeleted = false")
    Page<Post> findByMinCapacity(@Param("minCapacity") Integer minCapacity, Pageable pageable);

    // Filter by style
    Page<Post> findByStyleAndStatusAndIsDeletedFalse(String style, Post.PostStatus status, Pageable pageable);

    // Get popular posts (by view count)
    @Query("SELECT p FROM Post p WHERE " +
            "p.status = 'PUBLISHED' AND p.isActive = true AND p.isDeleted = false " +
            "ORDER BY p.viewCount DESC")
    Page<Post> findPopularPosts(Pageable pageable);

    // Get trending posts (by booking count)
    @Query("SELECT p FROM Post p WHERE " +
            "p.status = 'PUBLISHED' AND p.isActive = true AND p.isDeleted = false " +
            "ORDER BY p.bookingCount DESC, p.likeCount DESC")
    Page<Post> findTrendingPosts(Pageable pageable);

    // Count by vendor
    Long countByVendorAndIsDeletedFalse(User vendor);

    // Count by vendor and status
    Long countByVendorAndStatusAndIsDeletedFalse(User vendor, Post.PostStatus status);

    // Check if post exists by id and vendor
    boolean existsByIdAndVendorAndIsDeletedFalse(Long id, User vendor);
}