package com.identity.core.repository;

import com.identity.core.domain.UserAppPreference;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAppPreferenceRepository extends JpaRepository<UserAppPreference, Long> {

    List<UserAppPreference> findByUserIdOrderByAppIdAsc(Long userId);

    Optional<UserAppPreference> findByUserIdAndAppId(Long userId, String appId);
}
