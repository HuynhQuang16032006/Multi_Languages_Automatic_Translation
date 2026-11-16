package vn.edu.sgu.audiotour.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "admin_auth")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AdminAuth {

    @Id
    private Integer id;

    @Column(nullable = false, length = 50)
    private String username;

    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
