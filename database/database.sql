CREATE DATABASE IF NOT EXISTS wedding_booking_app
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE wedding_booking_app;
CREATE TABLE roles (
                       id BIGINT PRIMARY KEY AUTO_INCREMENT,
                       role_name VARCHAR(50) UNIQUE NOT NULL,
                       description TEXT,
                       permissions JSON
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE users (
                       id BIGINT PRIMARY KEY AUTO_INCREMENT,
                       email VARCHAR(100) UNIQUE NOT NULL,
                       password VARCHAR(255) NOT NULL,
                       full_name VARCHAR(100) NOT NULL,
                       phone VARCHAR(10),
                       address VARCHAR(255),
                       date_of_birth DATE,
                       role_id BIGINT NOT NULL,
                       avatar_url VARCHAR(255),
                       email_verified_at TIMESTAMP NULL,
                       verification_token VARCHAR(255),
                       is_active BOOLEAN DEFAULT TRUE,
                       is_locked BOOLEAN DEFAULT FALSE,
                       failed_login_attempts INT DEFAULT 0,
                       locked_until TIMESTAMP NULL,
                       deleted_at TIMESTAMP NULL,
                       last_login_at TIMESTAMP NULL,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                       FOREIGN KEY (role_id) REFERENCES roles(id),
                       INDEX idx_users_email (email),
                       INDEX idx_users_role (role_id),
                       INDEX idx_users_deleted (deleted_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE refresh_tokens (
                                id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                user_id BIGINT NOT NULL,
                                token VARCHAR(512) UNIQUE NOT NULL,
                                expires_at TIMESTAMP NOT NULL,
                                is_revoked BOOLEAN DEFAULT FALSE,
                                device_info VARCHAR(255),
                                ip_address VARCHAR(45),
                                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                revoked_at TIMESTAMP NULL,
                                FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                                INDEX idx_refresh_token (token),
                                INDEX idx_refresh_user (user_id),
                                INDEX idx_refresh_expires (expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE password_reset_tokens (
                                       id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                       user_id BIGINT NOT NULL,
                                       token VARCHAR(255) UNIQUE NOT NULL,
                                       expires_at TIMESTAMP NOT NULL,
                                       is_used BOOLEAN DEFAULT FALSE,
                                       used_at TIMESTAMP NULL,
                                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                       FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                                       INDEX idx_reset_token (token),
                                       INDEX idx_reset_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE wedding_venues (
                                id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                vendor_id BIGINT NOT NULL,
                                name VARCHAR(100) NOT NULL,
                                slug VARCHAR(120) UNIQUE,
                                description TEXT,
                                address VARCHAR(255) NOT NULL,
                                city VARCHAR(100),
                                district VARCHAR(100),
                                latitude DECIMAL(10, 8),
                                longitude DECIMAL(11, 8),
                                capacity INT NOT NULL,
                                price_per_table DECIMAL(10, 2) NOT NULL,
                                deposit_percentage DECIMAL(5, 2) DEFAULT 30.00,
                                images JSON,
                                amenities JSON,
                                is_available BOOLEAN DEFAULT TRUE,
                                rating DECIMAL(2, 1) DEFAULT 0,
                                review_count INT DEFAULT 0,
                                view_count INT DEFAULT 0,
                                deleted_at TIMESTAMP NULL,
                                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                FOREIGN KEY (vendor_id) REFERENCES users(id),
                                INDEX idx_venue_location (latitude, longitude),
                                INDEX idx_venue_city (city),
                                INDEX idx_venue_rating (rating),
                                INDEX idx_venue_vendor (vendor_id),
                                INDEX idx_venue_deleted (deleted_at),
                                FULLTEXT INDEX idx_venue_search (name, description)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE food_categories (
                                 id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                 name VARCHAR(50) NOT NULL,
                                 description TEXT,
                                 display_order INT DEFAULT 0,
                                 created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE food_items (
                            id BIGINT PRIMARY KEY AUTO_INCREMENT,
                            vendor_id BIGINT NOT NULL,
                            category_id BIGINT NOT NULL,
                            name VARCHAR(100) NOT NULL,
                            description TEXT,
                            price DECIMAL(10, 2) NOT NULL,
                            image_url VARCHAR(255),
                            is_available BOOLEAN DEFAULT TRUE,
                            is_popular BOOLEAN DEFAULT FALSE,
                            deleted_at TIMESTAMP NULL,
                            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                            FOREIGN KEY (vendor_id) REFERENCES users(id),
                            FOREIGN KEY (category_id) REFERENCES food_categories(id),
                            INDEX idx_food_vendor (vendor_id),
                            INDEX idx_food_category (category_id),
                            INDEX idx_food_deleted (deleted_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;



CREATE TABLE food_bookings (
                               id BIGINT PRIMARY KEY AUTO_INCREMENT,
                               wedding_booking_id BIGINT NOT NULL,
                               food_item_id BIGINT NOT NULL,
                               quantity INT NOT NULL,
                               unit_price DECIMAL(10, 2) NOT NULL,
                               total_price DECIMAL(10, 2) NOT NULL,
                               notes TEXT,
                               created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                               FOREIGN KEY (wedding_booking_id) REFERENCES wedding_bookings(id) ON DELETE CASCADE,
                               FOREIGN KEY (food_item_id) REFERENCES food_items(id),
                               INDEX idx_food_booking_wedding (wedding_booking_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE checklist_items (
                                 id VARCHAR(36) PRIMARY KEY,  -- UUID
                                 title VARCHAR(200) NOT NULL,
                                 description TEXT,
                                 is_completed BOOLEAN DEFAULT FALSE,
                                 created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                 updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                 completed_at TIMESTAMP NULL,
                                 user_id BIGINT,  -- Link với user nếu cần

                                 INDEX idx_user_id (user_id),
                                 INDEX idx_is_completed (is_completed),
                                 INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE conversations (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    wedding_booking_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    admin_id BIGINT NOT NULL,
    last_message TEXT,
    last_message_at TIMESTAMP NULL,
    unread_count_user INT DEFAULT 0,
    unread_count_admin INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (wedding_booking_id) REFERENCES wedding_bookings(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (admin_id) REFERENCES users(id),
    INDEX idx_conversation_booking (wedding_booking_id),
    INDEX idx_conversation_user (user_id),
    INDEX idx_conversation_admin (admin_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE messages (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    conversation_id BIGINT NOT NULL,
    sender_id BIGINT NOT NULL,
    message_text TEXT NOT NULL,
    message_type ENUM('TEXT', 'IMAGE', 'FILE', 'SYSTEM') DEFAULT 'TEXT',
    file_url VARCHAR(255),
    is_read BOOLEAN DEFAULT FALSE,
    read_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (conversation_id) REFERENCES conversations(id) ON DELETE CASCADE,
    FOREIGN KEY (sender_id) REFERENCES users(id),
    INDEX idx_message_conversation (conversation_id),
    INDEX idx_message_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE reviews (
                         id BIGINT PRIMARY KEY AUTO_INCREMENT,
                         wedding_booking_id BIGINT NOT NULL,
                         reviewer_id BIGINT NOT NULL,
                         vendor_id BIGINT NOT NULL,
                         service_type ENUM('VENUE', 'FOOD', 'CLOTHING', 'PHOTOGRAPHY') NOT NULL,
                         rating INT NOT NULL CHECK (rating >= 1 AND rating <= 5),
                         comment TEXT,
                         images JSON,
                         is_verified BOOLEAN DEFAULT FALSE,
                         created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                         updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

                         FOREIGN KEY (wedding_booking_id) REFERENCES wedding_bookings(id) ON DELETE CASCADE,
                         FOREIGN KEY (reviewer_id) REFERENCES users(id),
                         FOREIGN KEY (vendor_id) REFERENCES users(id),
                         INDEX idx_review_vendor (vendor_id),
                         INDEX idx_review_booking (wedding_booking_id),
                         UNIQUE KEY unique_review (wedding_booking_id, reviewer_id, service_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE payments (
                          id BIGINT PRIMARY KEY AUTO_INCREMENT,
                          wedding_booking_id BIGINT NOT NULL,
                          payment_code VARCHAR(50) UNIQUE NOT NULL,
                          amount DECIMAL(12, 2) NOT NULL,
                          payment_type ENUM('DEPOSIT', 'FULL_PAYMENT', 'REMAINING', 'REFUND') DEFAULT 'DEPOSIT',
                          payment_method VARCHAR(50),
                          payment_status ENUM('PENDING', 'COMPLETED', 'FAILED', 'REFUNDED', 'EXPIRED') DEFAULT 'PENDING',
                          transaction_id VARCHAR(100),
                          gateway_response JSON,
                          paid_at TIMESTAMP NULL,
                          refunded_at TIMESTAMP NULL,
                          expires_at TIMESTAMP NULL,
                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                          FOREIGN KEY (wedding_booking_id) REFERENCES wedding_bookings(id) ON DELETE CASCADE,
                          INDEX idx_payment_booking (wedding_booking_id),
                          INDEX idx_payment_code (payment_code),
                          INDEX idx_payment_status (payment_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE notifications (
                               id BIGINT PRIMARY KEY AUTO_INCREMENT,
                               user_id BIGINT NOT NULL,
                               title VARCHAR(255) NOT NULL,
                               message TEXT NOT NULL,
                               type ENUM('BOOKING', 'PAYMENT', 'REVIEW', 'SYSTEM', 'CHAT') DEFAULT 'SYSTEM',
                               reference_id BIGINT,
                               is_read BOOLEAN DEFAULT FALSE,
                               read_at TIMESTAMP NULL,
                               created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                               FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                               INDEX idx_notification_user (user_id),
                               INDEX idx_notification_read (is_read)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE audit_logs (
                            id BIGINT PRIMARY KEY AUTO_INCREMENT,
                            user_id BIGINT,
                            action VARCHAR(100) NOT NULL,
                            table_name VARCHAR(50),
                            record_id BIGINT,
                            old_values JSON,
                            new_values JSON,
                            ip_address VARCHAR(45),
                            user_agent TEXT,
                            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                            FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
                            INDEX idx_audit_user (user_id),
                            INDEX idx_audit_created (created_at),
                            INDEX idx_audit_table (table_name, record_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE IF NOT EXISTS posts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    content LONGTEXT,
    price DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    currency VARCHAR(3) DEFAULT 'VND',
    location VARCHAR(255),
    city VARCHAR(100),
    district VARCHAR(100),
    ward VARCHAR(100),
    contact_phone VARCHAR(20),
    contact_email VARCHAR(100),
    thumbnail VARCHAR(500),
    images JSON,
    category VARCHAR(50),
    sub_category VARCHAR(50),
    capacity INT,
    area DECIMAL(10, 2),
    amenities JSON,
    services JSON,
    vendor_id BIGINT NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING',
    is_featured BOOLEAN DEFAULT FALSE,
    is_available BOOLEAN DEFAULT TRUE,
    rating DECIMAL(2, 1) DEFAULT 0.0,
    total_reviews INT DEFAULT 0,
    total_bookings INT DEFAULT 0,
    view_count INT DEFAULT 0,
    available_from TIME,
    available_to TIME,
    working_days JSON,
    available_slots INT DEFAULT 4 COMMENT 'Number of booking slots available per day',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL,
    FOREIGN KEY (vendor_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_vendor_id (vendor_id),
    INDEX idx_category (category),
    INDEX idx_status (status),
    FULLTEXT INDEX idx_search (title, description, location)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE IF NOT EXISTS bookings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    booking_code VARCHAR(50) UNIQUE NOT NULL,
    user_id BIGINT NOT NULL,
    customer_name VARCHAR(100) NOT NULL,
    customer_phone VARCHAR(20) NOT NULL,
    customer_email VARCHAR(100),
    post_id BIGINT NOT NULL,
    vendor_id BIGINT NOT NULL,
    booking_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    duration_hours DECIMAL(5, 2),
    number_of_guests INT DEFAULT 1,
    unit_price DECIMAL(10, 2) NOT NULL,
    special_requests TEXT,
    notes TEXT,
    status VARCHAR(20) DEFAULT 'PENDING',
    cancelled_by BIGINT,
    cancelled_at TIMESTAMP NULL,
    cancellation_reason TEXT,
    confirmed_by BIGINT,
    confirmed_at TIMESTAMP NULL,
    completed_at TIMESTAMP NULL,
    rating DECIMAL(2, 1),
    review TEXT,
    reviewed_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
    FOREIGN KEY (vendor_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_booking_code (booking_code),
    INDEX idx_user_id (user_id),
    INDEX idx_vendor_id (vendor_id),
    INDEX idx_status (status)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Create menu_categories table
CREATE TABLE IF NOT EXISTS menu_categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    slug VARCHAR(100) UNIQUE NOT NULL,
    parent_id BIGINT,
    level INT DEFAULT 1,
    icon VARCHAR(100),
    image VARCHAR(500),
    display_order INT DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (parent_id) REFERENCES menu_categories(id) ON DELETE CASCADE,
    INDEX idx_slug (slug),
    INDEX idx_parent_id (parent_id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. Create menus table
CREATE TABLE IF NOT EXISTS menus (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    vendor_id BIGINT NOT NULL,
    post_id BIGINT,
    category VARCHAR(50) NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    unit VARCHAR(20) DEFAULT 'portion',
    currency VARCHAR(3) DEFAULT 'VND',
    ingredients TEXT,
    image VARCHAR(500),
    is_available BOOLEAN DEFAULT TRUE,
    min_order_quantity INT DEFAULT 1,
    max_order_quantity INT,
    rating DECIMAL(2, 1) DEFAULT 0.0,
    guest_per_table INT DEFAULT 10,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL,
    FOREIGN KEY (vendor_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE SET NULL,
    INDEX idx_vendor_id (vendor_id),
    INDEX idx_category (category),
    FULLTEXT INDEX idx_search (name, description)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS booking_menus (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    booking_id BIGINT NOT NULL,
    menu_id BIGINT NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    unit_price DECIMAL(10, 2) NOT NULL,
    total_price DECIMAL(10, 2) NOT NULL,
    customizations JSON,
    special_requests TEXT,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE,
    FOREIGN KEY (menu_id) REFERENCES menus(id) ON DELETE CASCADE,
    INDEX idx_booking_id (booking_id),
    INDEX idx_menu_id (menu_id),
    UNIQUE KEY unique_booking_menu (booking_id, menu_id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO roles (role_name, description, permissions) VALUES
('ADMIN', 'Administrator with full access', JSON_ARRAY('MANAGE_USERS', 'MANAGE_VENDORS', 'VIEW_REPORTS', 'MANAGE_SETTINGS')),
('VENDOR', 'Vendor managing their services', JSON_ARRAY('MANAGE_OWN_SERVICES', 'VIEW_OWN_BOOKINGS', 'MANAGE_OWN_REVIEWS')),
('CUSTOMER', 'Customer booking wedding services', JSON_ARRAY('BROWSE_SERVICES', 'MAKE_BOOKINGS', 'WRITE_REVIEWS', 'VIEW_OWN_BOOKINGS'));