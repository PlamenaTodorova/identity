package com.identity.api.controller;

import com.identity.api.dto.AppResponse;
import com.identity.core.service.AppService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/apps")
public class AppController {

    private final AppService appService;

    public AppController(AppService appService) {
        this.appService = appService;
    }

    @GetMapping
    public List<AppResponse> getApps() {
        return appService.getApps().stream()
                .map(AppResponse::from)
                .toList();
    }
}