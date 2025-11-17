package com.myapp.booking.repositories;

import com.myapp.booking.models.Menu;
import com.myapp.booking.models.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MenuRepository extends JpaRepository<Menu, Long> {

    // ✅ Find all active menus by post ID
    List<Menu> findByPostIdAndIsActiveTrue(Long postId);

    // ✅ Find active menu by ID
    Optional<Menu> findByIdAndIsActiveTrue(Long id);

    // ✅ Check if menu name exists for a post (active only)
    boolean existsByPostIdAndNameAndIsActiveTrue(Long postId, String name);

    // ✅ Find all menus by vendor ID
    List<Menu> findByVendorIdAndIsActiveTrue(Long vendorId);

    // ✅ Count active menus for a post
    long countByPostIdAndIsActiveTrue(Long postId);

    // ✅ Find all active menus
    List<Menu> findByIsActiveTrue();

    // ✅ Custom query to check if name exists excluding current menu (for updates)
    @Query("SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END " +
            "FROM Menu m " +
            "WHERE m.post.id = :postId " +
            "AND m.name = :name " +
            "AND m.id != :menuId " +
            "AND m.isActive = true")
    boolean existsByPostIdAndNameAndIdNotAndIsActiveTrue(
            @Param("postId") Long postId,
            @Param("name") String name,
            @Param("menuId") Long menuId
    );
}