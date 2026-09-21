package com.identity.core.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "user_app_preferences",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "app_id"})
)
@Getter
@Setter
@NoArgsConstructor
public class UserAppPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "app_id", nullable = false, length = 64)
    private String appId;

    @Column(nullable = false)
    private boolean enabled;

    public UserAppPreference(Long userId, String appId, boolean enabled) {
        this.userId = userId;
        this.appId = appId;
        this.enabled = enabled;
    }
}
