package com.orbit.mission.user;

import com.orbit.mission.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserDto>>> list(@RequestParam(required = false) String q) {
        List<UserEntity> users = (q != null && !q.isBlank())
                ? userRepository.findByUsernameContainingIgnoreCaseOrDisplayNameContainingIgnoreCase(q, q)
                : userRepository.findAll();
        List<UserDto> dtos = users.stream().map(UserDto::new).toList();
        return ResponseEntity.ok(ApiResponse.ok(dtos));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDto>> get(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(u -> ResponseEntity.ok(ApiResponse.ok(new UserDto(u))))
                .orElse(ResponseEntity.notFound().build());
    }
}
