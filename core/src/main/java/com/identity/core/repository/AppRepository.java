package com.identity.core.repository;

import com.identity.core.domain.App;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppRepository extends JpaRepository<App, String> {

	List<App> findAllByOrderByAppIdAsc();
}