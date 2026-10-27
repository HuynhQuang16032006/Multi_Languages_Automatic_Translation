-- V1: Schema khởi tạo cho hệ thống Audio Tour (MySQL 8.0 / InnoDB, UTF-8mb4)
-- Dùng CREATE TABLE IF NOT EXISTS để trở thành no-op trên DB đã được Hibernate tạo sẵn.
-- Cột và kiểu dữ liệu khớp với mapping của Hibernate 7 để spring.jpa.hibernate.ddl-auto=validate chạy được.

CREATE TABLE IF NOT EXISTS languages (
    id   BIGINT      NOT NULL AUTO_INCREMENT,
    code VARCHAR(10) NOT NULL,
    name VARCHAR(50) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_languages_code UNIQUE (code)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS locations (
    id               BIGINT       NOT NULL AUTO_INCREMENT,
    name             VARCHAR(100) NOT NULL,
    address          VARCHAR(255),
    latitude         DOUBLE,
    longitude        DOUBLE,
    radius_in_meters DOUBLE,
    priority         INTEGER,
    qr_code          VARCHAR(50),
    image_url        VARCHAR(255),
    parent_id        BIGINT,
    PRIMARY KEY (id),
    CONSTRAINT uk_locations_qr_code UNIQUE (qr_code),
    CONSTRAINT fk_locations_parent FOREIGN KEY (parent_id) REFERENCES locations (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS narrations (
    id                  BIGINT NOT NULL AUTO_INCREMENT,
    location_id         BIGINT NOT NULL,
    language_id         BIGINT NOT NULL,
    text_content        TEXT,
    audio_url           VARCHAR(255),
    duration_in_seconds INTEGER,
    PRIMARY KEY (id),
    CONSTRAINT fk_narrations_location FOREIGN KEY (location_id) REFERENCES locations (id),
    CONSTRAINT fk_narrations_language FOREIGN KEY (language_id) REFERENCES languages (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS playback_logs (
    id                      BIGINT      NOT NULL AUTO_INCREMENT,
    location_id             BIGINT      NOT NULL,
    language_code           VARCHAR(10) NOT NULL,
    listen_duration_seconds INTEGER,
    trigger_type            ENUM ('GEOFENCE','MANUAL','QR_CODE') NOT NULL,
    latitude                DOUBLE,
    longitude               DOUBLE,
    device_id               VARCHAR(64),
    created_at              DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_playback_logs_location FOREIGN KEY (location_id) REFERENCES locations (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
