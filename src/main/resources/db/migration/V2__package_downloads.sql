CREATE TABLE IF NOT EXISTS package_downloads (
    id BIGINT NOT NULL AUTO_INCREMENT,
    location_id BIGINT NOT NULL,
    language_code VARCHAR(10) NOT NULL,
    device_id VARCHAR(64),
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_package_downloads_location FOREIGN KEY (location_id) REFERENCES locations (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
