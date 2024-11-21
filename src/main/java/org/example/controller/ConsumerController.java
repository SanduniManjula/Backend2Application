package org.example.controller;

import org.example.entity.User;
import org.example.response.ApiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/consumer/users")
public class ConsumerController {

    private final WebClient webClient;

   /* @Value("${auth.token}")
    private String authToken;
    */

    public ConsumerController(WebClient webClient) {
        this.webClient = webClient;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> fetchFilteredUsers(HttpServletRequest request) {
        String token = extractAuthToken(request);
        List<Map<String, Object>> users = webClient.get()
                .uri("/users")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .retrieve()
                .bodyToFlux(User.class)
                .filter(user -> user.getAge() > 28)
                .map(user -> {
                    Map<String,Object> userMap = new HashMap<>();
                    userMap.put("userId",user.getId());
                    userMap.put("fullName", user.getName().toUpperCase());
                    userMap.put("age",user.getAge());
                    return userMap;
                })
                .collectList()
                .block();

        return ResponseEntity.ok(new ApiResponse<>(true, "Filtered users fetched successfully", users));

    }

    @PostMapping
    public ResponseEntity<ApiResponse<User>> forwardUserCreation(@RequestBody User user,HttpServletRequest request) {
        String token = extractAuthToken(request);
        User createdUser = webClient.post()
                .uri("/users")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .bodyValue(user)
                .retrieve()
                .bodyToMono(User.class)
                .block();

        return ResponseEntity.ok(new ApiResponse<>(true, "User created successfully", createdUser));
    }
    private String extractAuthToken(HttpServletRequest request) {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        throw new RuntimeException("Missing or invalid Authorization header");
    }
}

