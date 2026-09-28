package com.identity.core.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "apps")
@Getter
@Setter
@NoArgsConstructor
public class App {

    @Id
    @Column(name = "app_id", nullable = false, length = 64)
    private String appId;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(name = "base_url", nullable = false, length = 512)
    private String baseUrl;

    @Column(length = 512)
    private String icon;

    @Column(columnDefinition = "TEXT")
    private String description;

    public App(String appId, String name, String baseUrl) {
        this.appId = appId;
        this.name = name;
        this.baseUrl = baseUrl;
    }

    public App(String appId, String name, String baseUrl, String icon, String description) {
        this(appId, name, baseUrl);
        this.icon = icon;
        this.description = description;
    }
}