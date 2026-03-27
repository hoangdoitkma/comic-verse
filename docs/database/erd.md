# Database Schema & ERD: Comic & Novel Digital Platform

**Version:** 1.0.0  
**Status:** Approved  
**Description:** Tài liệu đặc tả cấu trúc cơ sở dữ liệu cho nền tảng đọc truyện tranh và tiểu thuyết, bao gồm hệ thống người dùng, thanh toán VIP, quản lý nội dung, tương tác xã hội và AI.

---

## 1. Visual ERD (DBML Script)
*Sử dụng đoạn code dưới đây dán vào [dbdiagram.io](https://dbdiagram.io) hoặc [dbdocs.io](https://dbdocs.io) để xem và xuất biểu đồ trực quan.*

```dbml
// ==============================
// 1. USERS & AUTHENTICATION
// ==============================
Table users {
  id int [pk, increment]
  email varchar(150) [unique, not null]
  password varchar(255)
  display_name varchar(26)
  birthday date [note: "Dùng để kiểm soát độ tuổi"]
  avatar_url text
  role varchar(8) [note: "ADMIN | UPLOADER | USER"]
  status varchar(20) [note: "ACTIVE | BANNED"]
  created_at timestamp [default: `CURRENT_TIMESTAMP`]
  updated_at timestamp [default: `CURRENT_TIMESTAMP`]
}

// ==============================
// 2. VIP SUBSCRIPTION
// ==============================
Table vip_packages {
  id int [pk, increment]
  name varchar(100)
  duration_month int
  price decimal(10,2)
  currency varchar(10)
  is_active boolean [default: true]
  created_at timestamp [default: `CURRENT_TIMESTAMP`]
}

Table subscriptions {
  id int [pk, increment]
  user_id int [ref: > users.id]
  package_id int [ref: > vip_packages.id]
  start_date timestamp
  end_date timestamp
  status varchar(9) [note: "ACTIVE | EXPIRED | CANCELLED"]
  created_at timestamp
}

Table transactions {
  id int [pk, increment]
  user_id int [ref: > users.id]
  package_id int [ref: > vip_packages.id]
  amount decimal(10,2)
  payment_method varchar(50) [note: "MOMO | ZALOPAY"]
  status varchar(7) [note: "PENDING | SUCCESS | FAILED"]
  created_at timestamp
}

// ==============================
// 3. CONTENT MANAGEMENT
// ==============================
Table authors {
  id int [pk, increment]
  name varchar(150)
  studio varchar(150)
  country varchar(100)
  created_at timestamp [default: `CURRENT_TIMESTAMP`]
}

Table age_ratings {
  id int [pk, increment]
  label varchar(20) [note: "All Ages | 13+ | 16+ | 18+"]
  description text
}

Table comics {
  id int [pk, increment]
  title varchar(255) [not null]
  slug varchar(255) [unique]
  synopsis text
  thumbnail_url text
  author_id int [ref: > authors.id]
  age_rating_id int [ref: > age_ratings.id]
  content_type varchar(10) [note: "COMIC | NOVEL"]
  comic_format varchar(15) [note: "COLOR | BLACK_WHITE | NULL if NOVEL"]
  status varchar(30) [note: "ONGOING | COMPLETED"]
  total_chapters int
  view_count int
  average_rating decimal(3,2)
  created_by int [ref: > users.id]
  created_at timestamp
  updated_at timestamp
}

// ==============================
// 4. CHAPTERS & PAGES
// ==============================
Table chapters {
  id int [pk, increment]
  comic_id int [not null, ref: > comics.id]
  chapter_number decimal(6,2)
  sort_order int [note: "Dùng để sắp xếp thứ tự chương hiển thị"]
  title varchar(255)
  content text [note: "Chỉ dùng cho truyện chữ"]
  access_type varchar(20) [note: "FREE | VIP"]
  view_count int
  created_at timestamp
}

Table chapter_pages {
  id int [pk, increment]
  chapter_id int [not null, ref: > chapters.id]
  page_number int
  image_url text
}

// ==============================
// 5. AI SUMMARIES & RECOMMENDATION
// ==============================
Table comic_ai_summaries {
  id int [pk, increment]
  comic_id int [ref: > comics.id]
  summary text
  summary_source varchar(20) [note: "AI_GENERATED | INTERNET | MIXED"]
  generated_from_chapters int
  model varchar(50)
  created_at timestamp
  updated_at timestamp
}

Table chapter_ai_summaries {
  id int [pk, increment]
  chapter_id int [ref: > chapters.id]
  summary text
  model varchar(50)
  created_at timestamp
  updated_at timestamp
}

Table recommendations {
  id int [pk, increment]
  user_id int [ref: > users.id]
  comic_id int [ref: > comics.id]
  score decimal(5,2)
  generated_at timestamp
}

// ==============================
// 6. CLASSIFICATION
// ==============================
Table genres {
  id int [pk, increment]
  name varchar(100)
  description text
}

Table tags {
  id int [pk, increment]
  name varchar(100)
}

Table comic_genres {
  id int [pk, increment]
  comic_id int [ref: > comics.id]
  genre_id int [ref: > genres.id]
}

Table comic_tags {
  id int [pk, increment]
  comic_id int [ref: > comics.id]
  tag_id int [ref: > tags.id]
}

// ==============================
// 7. SOCIAL & INTERACTION
// ==============================
Table follows {
  id int [pk, increment]
  user_id int [ref: > users.id]
  comic_id int [ref: > comics.id]
  created_at timestamp
}

Table ratings {
  id int [pk, increment]
  user_id int [ref: > users.id]
  comic_id int [ref: > comics.id]
  rating_value int
  created_at timestamp
}

Table comments {
  id int [pk, increment]
  user_id int [ref: > users.id]
  chapter_id int [ref: > chapters.id]
  parent_id int [ref: > comments.id]
  content text
  status varchar(20) [note: "VISIBLE | HIDDEN | DELETED"]
  like_count int [default: 0]
  reply_count int [default: 0]
  created_at timestamp
}

Table reports {
  id int [pk, increment]
  reporter_id int [ref: > users.id]
  comment_id int [ref: > comments.id]
  reason text
  status varchar(20)
  created_at timestamp
}

Table chapter_likes {
  id int [pk, increment]
  user_id int [ref: > users.id]
  chapter_id int [ref: > chapters.id]
  created_at timestamp
}

// ==============================
// 8. READING & TRACKING
// ==============================
Table reading_history {
  id int [pk, increment]
  user_id int [ref: > users.id]
  comic_id int [ref: > comics.id]
  chapter_id int [ref: > chapters.id]
  last_page int
  updated_at timestamp
}

Table bookmarks {
  id int [pk, increment]
  user_id int [ref: > users.id]
  chapter_id int [ref: > chapters.id]
  page_number int
  created_at timestamp
}

Table comic_views {
  id int [pk, increment]
  user_id int [ref: > users.id]
  comic_id int [ref: > comics.id]
  chapter_id int [ref: > chapters.id]
  viewed_at timestamp
}

Table search_history {
  id int [pk, increment]
  user_id int [ref: > users.id]
  keyword varchar(255)
  searched_at timestamp
}

// ==============================
// 9. NOTIFICATION SYSTEM
// ==============================
Table notifications {
  id int [pk, increment]
  user_id int [ref: > users.id]
  title varchar(255)
  message text
  type varchar(50) [note: "NEW_CHAPTER | COMMENT_REPLY | SYSTEM"]
  is_read boolean [default: false]
  created_at timestamp
}

Table notification_preferences {
  id int [pk, increment]
  user_id int [ref: > users.id]
  new_chapter boolean [default: true]
  comment_reply boolean [default: true]
  system_notice boolean [default: true]
}

// ==============================
// 10. CMS & SYSTEM MANAGEMENT
// ==============================
Table upload_logs {
  id int [pk, increment]
  uploader_id int [ref: > users.id]
  comic_id int [ref: > comics.id]
  chapter_id int [ref: > chapters.id]
  status varchar(20) [note: "PENDING | APPROVED | REJECTED"]
  reviewed_by int [ref: > users.id]
  reviewed_at timestamp
  created_at timestamp
}

Table system_configs {
  id int [pk, increment]
  config_key varchar(100) [unique]
  config_value text
  description text
}

Table comic_daily_stats {
  id int [pk, increment]
  stat_date date
  comic_id int [ref: > comics.id]
  total_views int
  unique_viewers int
  total_follows int
  new_follows int
  total_likes int
  total_comments int
  total_revenue decimal(10,2)
  created_at timestamp
}