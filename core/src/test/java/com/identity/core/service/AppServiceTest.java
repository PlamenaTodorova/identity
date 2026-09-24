package com.identity.core.service;

import com.identity.core.domain.App;
import com.identity.core.repository.AppRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppServiceTest {

    @Mock
    private AppRepository appRepository;

    @Test
    void returnsAllAppsOrderedByAppId() {
        List<App> apps = List.of(
                new App("bingable", "Bingable", "http://localhost:3001"),
                new App("bookworm", "Bookworm", "http://localhost:3002")
        );
        when(appRepository.findAllByOrderByAppIdAsc()).thenReturn(apps);

        AppService appService = new AppService(appRepository);

        assertThat(appService.getApps()).containsExactlyElementsOf(apps);
        verify(appRepository).findAllByOrderByAppIdAsc();
    }
}