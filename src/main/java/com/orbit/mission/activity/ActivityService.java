package com.orbit.mission.activity;

import com.orbit.mission.common.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class ActivityService {

    private final ActivityRepository activityRepository;

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
        return new PageResponse<>(result.getContent().stream().map(ActivityDto::new).toList(),
                result.getTotalElements(), page, pageSize);
    }
}
