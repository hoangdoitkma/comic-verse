-- ==============================================================================
-- Database Schema: Comic & Novel Digital Platform
-- RDBMS: MySQL / MariaDB
-- ==============================================================================

SET FOREIGN_KEY_CHECKS = 0;

-- ==============================
-- DROP EXISTING TABLES
-- ==============================
DROP TABLE IF EXISTS comic_daily_stats, system_configs, upload_logs, notification_preferences, notifications;
DROP TABLE IF EXISTS search_history, comic_views, bookmarks, reading_history;
DROP TABLE IF EXISTS chapter_likes, reports, comments, ratings, follows;
DROP TABLE IF EXISTS comic_tags, comic_genres, tags, genres;
DROP TABLE IF EXISTS recommendations, chapter_ai_summaries, comic_ai_summaries;
DROP TABLE IF EXISTS chapter_pages, chapters;
DROP TABLE IF EXISTS comics, age_ratings, authors;
DROP TABLE IF EXISTS transactions, subscriptions, vip_packages;
DROP TABLE IF EXISTS users;

-- ==============================
-- 1. USERS & AUTHENTICATION
-- ==============================
CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(150) UNIQUE NOT NULL,
    password VARCHAR(255),
    display_name VARCHAR(26),
    birthday DATE COMMENT 'Dùng để kiểm soát độ tuổi',
    avatar_url TEXT,
    role VARCHAR(8) DEFAULT 'USER' COMMENT 'ADMIN | UPLOADER | USER',
    status VARCHAR(20) DEFAULT 'ACTIVE' COMMENT 'ACTIVE | BANNED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- ==============================
-- 2. VIP SUBSCRIPTION
-- ==============================
CREATE TABLE vip_packages (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100),
    duration_month INT,
    price DECIMAL(10,2),
    currency VARCHAR(10) DEFAULT 'VND',
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE subscriptions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    package_id INT,
    start_date TIMESTAMP,
    end_date TIMESTAMP,
    status VARCHAR(9) COMMENT 'ACTIVE | EXPIRED | CANCELLED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (package_id) REFERENCES vip_packages(id) ON DELETE SET NULL
);

CREATE TABLE transactions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    package_id INT,
    amount DECIMAL(10,2),
    payment_method VARCHAR(50) COMMENT 'MOMO | ZALOPAY',
    status VARCHAR(7) COMMENT 'PENDING | SUCCESS | FAILED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (package_id) REFERENCES vip_packages(id) ON DELETE SET NULL
);

-- ==============================
-- 3. CONTENT MANAGEMENT
-- ==============================
CREATE TABLE authors (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(150),
    studio VARCHAR(150),
    country VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE age_ratings (
    id INT AUTO_INCREMENT PRIMARY KEY,
    label VARCHAR(20) COMMENT 'All Ages | 13+ | 16+ | 18+',
    description TEXT
);

CREATE TABLE comics (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    slug VARCHAR(255) UNIQUE,
    synopsis TEXT,
    thumbnail_url TEXT,
    author_id INT,
    age_rating_id INT,
    content_type VARCHAR(10) COMMENT 'COMIC | NOVEL',
    comic_format VARCHAR(15) COMMENT 'COLOR | BLACK_WHITE | NULL if NOVEL',
    status VARCHAR(30) COMMENT 'ONGOING | COMPLETED',
    total_chapters INT DEFAULT 0,
    view_count INT DEFAULT 0,
    average_rating DECIMAL(3,2) DEFAULT 0.00,
    created_by INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (author_id) REFERENCES authors(id) ON DELETE SET NULL,
    FOREIGN KEY (age_rating_id) REFERENCES age_ratings(id) ON DELETE SET NULL,
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL
);

-- ==============================
-- 4. CHAPTERS & PAGES
-- ==============================
CREATE TABLE chapters (
    id INT AUTO_INCREMENT PRIMARY KEY,
    comic_id INT NOT NULL,
    chapter_number DECIMAL(6,2),
    sort_order INT COMMENT 'Dùng để sắp xếp thứ tự chương hiển thị',
    title VARCHAR(255),
    content TEXT COMMENT 'Chỉ dùng cho truyện chữ',
    access_type VARCHAR(20) DEFAULT 'FREE' COMMENT 'FREE | VIP',
    view_count INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (comic_id) REFERENCES comics(id) ON DELETE CASCADE
);

CREATE TABLE chapter_pages (
    id INT AUTO_INCREMENT PRIMARY KEY,
    chapter_id INT NOT NULL,
    page_number INT,
    image_url TEXT,
    FOREIGN KEY (chapter_id) REFERENCES chapters(id) ON DELETE CASCADE
);

-- ==============================
-- 5. AI SUMMARIES & RECOMMENDATION
-- ==============================
CREATE TABLE comic_ai_summaries (
    id INT AUTO_INCREMENT PRIMARY KEY,
    comic_id INT,
    summary TEXT,
    summary_source VARCHAR(20) COMMENT 'AI_GENERATED | INTERNET | MIXED',
    generated_from_chapters INT,
    model VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (comic_id) REFERENCES comics(id) ON DELETE CASCADE
);

CREATE TABLE chapter_ai_summaries (
    id INT AUTO_INCREMENT PRIMARY KEY,
    chapter_id INT,
    summary TEXT,
    model VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (chapter_id) REFERENCES chapters(id) ON DELETE CASCADE
);

CREATE TABLE recommendations (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    comic_id INT,
    score DECIMAL(5,2),
    generated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (comic_id) REFERENCES comics(id) ON DELETE CASCADE
);

-- ==============================
-- 6. CLASSIFICATION
-- ==============================
CREATE TABLE genres (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100),
    description TEXT
);

CREATE TABLE tags (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100)
);

CREATE TABLE comic_genres (
    id INT AUTO_INCREMENT PRIMARY KEY,
    comic_id INT,
    genre_id INT,
    FOREIGN KEY (comic_id) REFERENCES comics(id) ON DELETE CASCADE,
    FOREIGN KEY (genre_id) REFERENCES genres(id) ON DELETE CASCADE
);

CREATE TABLE comic_tags (
    id INT AUTO_INCREMENT PRIMARY KEY,
    comic_id INT,
    tag_id INT,
    FOREIGN KEY (comic_id) REFERENCES comics(id) ON DELETE CASCADE,
    FOREIGN KEY (tag_id) REFERENCES tags(id) ON DELETE CASCADE
);

-- ==============================
-- 7. SOCIAL & INTERACTION
-- ==============================
CREATE TABLE follows (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    comic_id INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (comic_id) REFERENCES comics(id) ON DELETE CASCADE
);

CREATE TABLE ratings (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    comic_id INT,
    rating_value INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (comic_id) REFERENCES comics(id) ON DELETE CASCADE
);

CREATE TABLE comments (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    chapter_id INT,
    parent_id INT,
    content TEXT,
    status VARCHAR(20) DEFAULT 'VISIBLE' COMMENT 'VISIBLE | HIDDEN | DELETED',
    like_count INT DEFAULT 0,
    reply_count INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (chapter_id) REFERENCES chapters(id) ON DELETE CASCADE,
    FOREIGN KEY (parent_id) REFERENCES comments(id) ON DELETE CASCADE
);

CREATE TABLE reports (
    id INT AUTO_INCREMENT PRIMARY KEY,
    reporter_id INT,
    comment_id INT,
    reason TEXT,
    status VARCHAR(20) DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (reporter_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (comment_id) REFERENCES comments(id) ON DELETE CASCADE
);

CREATE TABLE chapter_likes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    chapter_id INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (chapter_id) REFERENCES chapters(id) ON DELETE CASCADE
);

-- ==============================
-- 8. READING & TRACKING
-- ==============================
CREATE TABLE reading_history (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    comic_id INT,
    chapter_id INT,
    last_page INT,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (comic_id) REFERENCES comics(id) ON DELETE CASCADE,
    FOREIGN KEY (chapter_id) REFERENCES chapters(id) ON DELETE CASCADE
);

CREATE TABLE bookmarks (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    chapter_id INT,
    page_number INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (chapter_id) REFERENCES chapters(id) ON DELETE CASCADE
);

CREATE TABLE comic_views (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    comic_id INT,
    chapter_id INT,
    viewed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (comic_id) REFERENCES comics(id) ON DELETE CASCADE,
    FOREIGN KEY (chapter_id) REFERENCES chapters(id) ON DELETE CASCADE
);

CREATE TABLE search_history (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    keyword VARCHAR(255),
    searched_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- ==============================
-- 9. NOTIFICATION SYSTEM
-- ==============================
CREATE TABLE notifications (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    title VARCHAR(255),
    message TEXT,
    type VARCHAR(50) COMMENT 'NEW_CHAPTER | COMMENT_REPLY | SYSTEM',
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE notification_preferences (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    new_chapter BOOLEAN DEFAULT TRUE,
    comment_reply BOOLEAN DEFAULT TRUE,
    system_notice BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- ==============================
-- 10. CMS & SYSTEM MANAGEMENT
-- ==============================
CREATE TABLE upload_logs (
    id INT AUTO_INCREMENT PRIMARY KEY,
    uploader_id INT,
    comic_id INT,
    chapter_id INT,
    status VARCHAR(20) COMMENT 'PENDING | APPROVED | REJECTED',
    reviewed_by INT,
    reviewed_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (uploader_id) REFERENCES users(id) ON DELETE SET NULL,
    FOREIGN KEY (comic_id) REFERENCES comics(id) ON DELETE CASCADE,
    FOREIGN KEY (chapter_id) REFERENCES chapters(id) ON DELETE CASCADE,
    FOREIGN KEY (reviewed_by) REFERENCES users(id) ON DELETE SET NULL
);

CREATE TABLE system_configs (
    id INT AUTO_INCREMENT PRIMARY KEY,
    config_key VARCHAR(100) UNIQUE,
    config_value TEXT,
    description TEXT
);

CREATE TABLE comic_daily_stats (
    id INT AUTO_INCREMENT PRIMARY KEY,
    stat_date DATE,
    comic_id INT,
    total_views INT DEFAULT 0,
    unique_viewers INT DEFAULT 0,
    total_follows INT DEFAULT 0,
    new_follows INT DEFAULT 0,
    total_likes INT DEFAULT 0,
    total_comments INT DEFAULT 0,
    total_revenue DECIMAL(10,2) DEFAULT 0.00,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (comic_id) REFERENCES comics(id) ON DELETE CASCADE
);

SET FOREIGN_KEY_CHECKS = 1;