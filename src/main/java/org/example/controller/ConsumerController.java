package org.example.controller;


import org.example.entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/consumer/users")
public class ConsumerController {

    private final WebClient webClient;

    @Value("${auth.token}")
    private String authToken;

    public ConsumerController(WebClient webClient) {
        this.webClient = webClient;
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> fetchFilteredUsers() {
        List<Map<String, Object>> users = webClient.get()
                .uri("/users")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
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

        return ResponseEntity.ok(users);
    }

    @PostMapping
    public ResponseEntity<User> forwardUserCreation(@RequestBody User user) {
        User createdUser = webClient.post()
                .uri("/users")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
                .bodyValue(user)
                .retrieve()
                .bodyToMono(User.class)
                .block();

        return ResponseEntity.ok(createdUser);
    }
}

