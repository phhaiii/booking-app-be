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
                       phone VARCHAR(20),
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

CREATE TABLE clothing_items (
                                id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                vendor_id BIGINT NOT NULL,
                                name VARCHAR(100) NOT NULL,
                                type ENUM('WEDDING_DRESS', 'SUIT', 'ACCESSORIES', 'AO_DAI') NOT NULL,
                                description TEXT,
                                size VARCHAR(20),
                                color VARCHAR(50),
                                brand VARCHAR(100),
                                price DECIMAL(10, 2) NOT NULL,
                                rental_price_per_day DECIMAL(10, 2),
                                images JSON,
                                is_available BOOLEAN DEFAULT TRUE,
                                stock_quantity INT DEFAULT 1,
                                deleted_at TIMESTAMP NULL,
                                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                FOREIGN KEY (vendor_id) REFERENCES users(id),
                                INDEX idx_clothing_vendor (vendor_id),
                                INDEX idx_clothing_type (type),
                                INDEX idx_clothing_deleted (deleted_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE photography_packages (
                                      id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                      vendor_id BIGINT NOT NULL,
                                      name VARCHAR(100) NOT NULL,
                                      description TEXT,
                                      price DECIMAL(10, 2) NOT NULL,
                                      duration_hours INT NOT NULL,
                                      includes JSON,
                                      sample_images JSON,
                                      is_available BOOLEAN DEFAULT TRUE,
                                      is_popular BOOLEAN DEFAULT FALSE,
                                      deleted_at TIMESTAMP NULL,
                                      created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                      updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                      FOREIGN KEY (vendor_id) REFERENCES users(id),
                                      INDEX idx_photo_vendor (vendor_id),
                                      INDEX idx_photo_deleted (deleted_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE wedding_bookings (
                                  id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                  booking_code VARCHAR(50) UNIQUE NOT NULL,
                                  customer_id BIGINT NOT NULL,
                                  venue_id BIGINT NOT NULL,
                                  wedding_date DATE NOT NULL,
                                  wedding_time TIME,
                                  guest_count INT NOT NULL,
                                  total_tables INT NOT NULL,
                                  status ENUM('PENDING', 'CONFIRMED', 'DEPOSIT_PAID', 'PAID', 'CANCELLED', 'COMPLETED') DEFAULT 'PENDING',
                                  venue_price DECIMAL(12, 2) DEFAULT 0,
                                  food_price DECIMAL(12, 2) DEFAULT 0,
                                  clothing_price DECIMAL(12, 2) DEFAULT 0,
                                  photography_price DECIMAL(12, 2) DEFAULT 0,
                                  total_amount DECIMAL(12, 2) DEFAULT 0,
                                  deposit_amount DECIMAL(12, 2) DEFAULT 0,
                                  paid_amount DECIMAL(12, 2) DEFAULT 0,
                                  notes TEXT,
                                  cancellation_reason TEXT,
                                  cancelled_at TIMESTAMP NULL,
                                  confirmed_at TIMESTAMP NULL,
                                  completed_at TIMESTAMP NULL,
                                  deleted_at TIMESTAMP NULL,
                                  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                  FOREIGN KEY (customer_id) REFERENCES users(id),
                                  FOREIGN KEY (venue_id) REFERENCES wedding_venues(id),
                                  INDEX idx_booking_code (booking_code),
                                  INDEX idx_booking_customer (customer_id),
                                  INDEX idx_booking_venue (venue_id),
                                  INDEX idx_booking_date (wedding_date),
                                  INDEX idx_booking_status (status),
                                  INDEX idx_booking_deleted (deleted_at)
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

CREATE TABLE clothing_bookings (
                                   id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                   wedding_booking_id BIGINT NOT NULL,
                                   clothing_item_id BIGINT NOT NULL,
                                   rental_days INT NOT NULL,
                                   unit_price DECIMAL(10, 2) NOT NULL,
                                   total_price DECIMAL(10, 2) NOT NULL,
                                   fitting_date DATETIME,
                                   return_date DATETIME,
                                   notes TEXT,
                                   created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                   FOREIGN KEY (wedding_booking_id) REFERENCES wedding_bookings(id) ON DELETE CASCADE,
                                   FOREIGN KEY (clothing_item_id) REFERENCES clothing_items(id),
                                   INDEX idx_clothing_booking_wedding (wedding_booking_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE photography_bookings (
                                      id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                      wedding_booking_id BIGINT NOT NULL,
                                      package_id BIGINT NOT NULL,
                                      shooting_date DATETIME NOT NULL,
                                      location VARCHAR(255),
                                      price DECIMAL(10, 2) NOT NULL,
                                      special_requests TEXT,
                                      status ENUM('PENDING', 'CONFIRMED', 'COMPLETED', 'CANCELLED') DEFAULT 'PENDING',
                                      created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                                      FOREIGN KEY (wedding_booking_id) REFERENCES wedding_bookings(id) ON DELETE CASCADE,
                                      FOREIGN KEY (package_id) REFERENCES photography_packages(id),
                                      INDEX idx_photo_booking_wedding (wedding_booking_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE checklist_templates (
                                     id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                     name VARCHAR(100) NOT NULL,
                                     description TEXT,
                                     items JSON,
                                     is_default BOOLEAN DEFAULT FALSE,
                                     created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE user_checklists (
                                 id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                 wedding_booking_id BIGINT NOT NULL,
                                 template_id BIGINT,
                                 custom_items JSON,
                                 completed_items JSON,
                                 progress_percentage DECIMAL(5, 2) DEFAULT 0,
                                 created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                 updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

                                 FOREIGN KEY (wedding_booking_id) REFERENCES wedding_bookings(id) ON DELETE CASCADE,
                                 FOREIGN KEY (template_id) REFERENCES checklist_templates(id),
                                 INDEX idx_checklist_booking (wedding_booking_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE chat_rooms (
                            id BIGINT PRIMARY KEY AUTO_INCREMENT,
                            wedding_booking_id BIGINT NOT NULL,
                            name VARCHAR(100),
                            participants JSON,
                            is_active BOOLEAN DEFAULT TRUE,
                            last_message_at TIMESTAMP NULL,
                            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                            FOREIGN KEY (wedding_booking_id) REFERENCES wedding_bookings(id) ON DELETE CASCADE,
                            INDEX idx_chat_booking (wedding_booking_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE messages (
                          id BIGINT PRIMARY KEY AUTO_INCREMENT,
                          chat_room_id BIGINT NOT NULL,
                          sender_id BIGINT NOT NULL,
                          message_text TEXT NOT NULL,
                          message_type ENUM('TEXT', 'IMAGE', 'FILE', 'SYSTEM') DEFAULT 'TEXT',
                          file_url VARCHAR(255),
                          is_read BOOLEAN DEFAULT FALSE,
                          read_at TIMESTAMP NULL,
                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                          FOREIGN KEY (chat_room_id) REFERENCES chat_rooms(id) ON DELETE CASCADE,
                          FOREIGN KEY (sender_id) REFERENCES users(id),
                          INDEX idx_message_room (chat_room_id),
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