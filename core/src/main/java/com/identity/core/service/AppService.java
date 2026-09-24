package com.identity.core.service;

import com.identity.core.domain.App;
import com.identity.core.repository.AppRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AppService {

    private final AppRepository appRepository;

    public AppService(AppRepository appRepository) {
        this.appRepository = appRepository;
    }

    @Transactional(readOnly = true)
    public List<App> getApps() {
        return appRepository.findAllByOrderByAppIdAsc();
    }
}