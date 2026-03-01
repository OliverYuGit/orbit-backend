package com.orbit.mission.activity;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ActivityRepository extends JpaRepository<ActivityEntity, Long> {
    Page<ActivityEntity> findByTaskIdOrderByCreatedAtDesc(Long taskId, Pageable pageable);
    Page<ActivityEntity> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
