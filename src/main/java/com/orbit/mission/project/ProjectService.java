package com.orbit.mission.project;

import com.orbit.mission.activity.ActivityService;
import com.orbit.mission.activity.ActivityType;
import com.orbit.mission.common.PageResponse;
import com.orbit.mission.common.ResourceNotFoundException;
import com.orbit.mission.user.UserEntity;
import com.orbit.mission.user.UserRepository;
import com.orbit.mission.user.UserSummaryDto;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ActivityService activityService;
    private final UserRepository userRepository;

    @Transactional
    public ProjectDto create(ProjectCreateRequest req, Long actorId) {
        ProjectEntity project = new ProjectEntity();
        project.setName(req.getName());
        project.setDescription(req.getDescription());
        project.setStatus(ProjectStatus.ACTIVE);
        project.setOwnerId(actorId);
        project.setStartDate(req.getStartDate());
        project.setTargetDate(req.getTargetDate());
        project = projectRepository.save(project);

        activityService.record(null, actorId, ActivityType.TASK_CREATED,
                "Project created: " + project.getName(), 
                Map.of("projectId", project.getId()));
        return toDto(project);
    }

    public ProjectDto get(Long id) {
        return toDto(findActive(id));
    }

    @Transactional
    public ProjectDto update(Long id, ProjectUpdateRequest req, Long actorId) {
        ProjectEntity project = findActive(id);
        List<String> changes = new ArrayList<>();

        if (req.getName() != null && !req.getName().equals(project.getName())) {
            changes.add("name");
            project.setName(req.getName());
        }
        if (req.getDescription() != null) {
            project.setDescription(req.getDescription());
            changes.add("description");
        }
        if (req.getStatus() != null && req.getStatus() != project.getStatus()) {
            changes.add("status:" + project.getStatus() + "->" + req.getStatus());
            project.setStatus(req.getStatus());
        }
        if (req.getOwnerId() != null && !req.getOwnerId().equals(project.getOwnerId())) {
            changes.add("owner");
            project.setOwnerId(req.getOwnerId());
        }
        if (req.getStartDate() != null) {
            project.setStartDate(req.getStartDate());
            changes.add("startDate");
        }
        if (req.getTargetDate() != null) {
            project.setTargetDate(req.getTargetDate());
            changes.add("targetDate");
        }

        project = projectRepository.save(project);
        if (!changes.isEmpty()) {
            activityService.record(null, actorId, ActivityType.TASK_UPDATED,
                    "Project updated: " + String.join(", ", changes),
                    Map.of("projectId", project.getId()));
        }
        return toDto(project);
    }

    @Transactional
    public void delete(Long id, Long actorId) {
        ProjectEntity project = findActive(id);
        project.setDeletedAt(Instant.now());
        projectRepository.save(project);
        activityService.record(null, actorId, ActivityType.TASK_UPDATED,
                "Project deleted: " + project.getName(),
                Map.of("projectId", id));
    }

    public PageResponse<ProjectDto> list(ProjectFilter filter) {
        int cappedPageSize = Math.min(filter.getPageSize(), 100);
        Specification<ProjectEntity> spec = buildSpec(filter);
        Sort sort = Sort.by(Sort.Direction.DESC, "updatedAt");
        Page<ProjectEntity> page = projectRepository.findAll(spec,
                PageRequest.of(filter.getPage() - 1, cappedPageSize, sort));

        // Batch-load owners
        Set<Long> ownerIds = page.getContent().stream()
                .map(ProjectEntity::getOwnerId)
                .collect(Collectors.toSet());
        Map<Long, UserSummaryDto> ownerMap = userRepository.findAllById(ownerIds).stream()
                .collect(Collectors.toMap(UserEntity::getId, UserSummaryDto::new));

        List<ProjectDto> dtos = page.getContent().stream()
                .map(p -> new ProjectDto(p, ownerMap.get(p.getOwnerId())))
                .toList();

        return new PageResponse<>(dtos, page.getTotalElements(), filter.getPage(), cappedPageSize);
    }

    // --- helpers ---

    private ProjectDto toDto(ProjectEntity project) {
        UserSummaryDto owner = userRepository.findById(project.getOwnerId())
                .map(UserSummaryDto::new)
                .orElse(null);
        return new ProjectDto(project, owner);
    }

    private Specification<ProjectEntity> buildSpec(ProjectFilter f) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isNull(root.get("deletedAt")));
            if (f.getStatus() != null && !f.getStatus().isEmpty()) {
                predicates.add(root.get("status").in(f.getStatus()));
            }
            if (f.getOwnerId() != null) {
                predicates.add(cb.equal(root.get("ownerId"), f.getOwnerId()));
            }
            if (f.getQ() != null && !f.getQ().isBlank()) {
                String like = "%" + f.getQ().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("name")), like),
                        cb.like(cb.lower(root.get("description")), like)
                ));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private ProjectEntity findActive(Long id) {
        return projectRepository.findById(id)
                .filter(p -> p.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + id));
    }
}
