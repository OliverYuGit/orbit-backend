package com.orbit.mission.activity;

import com.orbit.mission.common.PageResponse;
import com.orbit.mission.user.UserEntity;
import com.orbit.mission.user.UserRepository;
import com.orbit.mission.user.UserSummaryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ActivityService {

    private final ActivityRepository activityRepository;
    private final UserRepository userRepository;

    @Transactional
    public void record(Long taskId, Long actorId, ActivityType type, String summary, Map<String, Object> payload) {
        ActivityEntity a = new ActivityEntity();
        a.setTaskId(taskId);
        a.setActorId(actorId);
        a.setType(type);
        a.setSummary(summary);
        a.setPayload(payload);
        activityRepository.save(a);
    }

    public PageResponse<ActivityDto> list(Long taskId, int page, int pageSize) {
        PageRequest pr = PageRequest.of(page - 1, pageSize);
        Page<ActivityEntity> result = taskId != null
                ? activityRepository.findByTaskIdOrderByCreatedAtDesc(taskId, pr)
                : activityRepository.findAllByOrderByCreatedAtDesc(pr);

        // Batch-load actors
        Set<Long> actorIds = result.getContent().stream()
                .map(ActivityEntity::getActorId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());
        Map<Long, UserSummaryDto> userMap = userRepository.findAllById(actorIds).stream()
                .collect(Collectors.toMap(UserEntity::getId, UserSummaryDto::new));

        List<ActivityDto> dtos = result.getContent().stream()
                .map(a -> new ActivityDto(a, a.getActorId() != null ? userMap.get(a.getActorId()) : null))
                .toList();

        return new PageResponse<>(dtos, result.getTotalElements(), page, pageSize);
    }
}
