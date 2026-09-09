package com.microservices.UserService.controller;

import com.microservices.UserService.dto.UserResponse;
import com.microservices.UserService.entity.User;
import com.microservices.UserService.service.UserService;
import com.microservices.UserService.service.UserServiceImplementation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final UserServiceImplementation userServiceImplementation;

    public UserController(UserService userService, UserServiceImplementation userServiceImplementation) {
        this.userService = userService;
        this.userServiceImplementation = userServiceImplementation;
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> users = userService.getAllUser().stream()
                .map(userServiceImplementation::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        // If user not found, service will throw ResourceNotFoundException
        // GlobalExceptionHandler will catch it and return 404
        User user = userService.getUserById(id);
        UserResponse response = userServiceImplementation.mapToResponse(user);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/{id}/exists")
    public ResponseEntity<Boolean> existsById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.existById(id));
    }

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@RequestBody User user) {
        User savedUser = userService.createUser(user);
        UserResponse response = userServiceImplementation.mapToResponse(savedUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long id, @RequestBody User user) {
        // If user not found, service will throw ResourceNotFoundException
        User updatedUser = userService.updateUser(id, user);
        UserResponse response = userServiceImplementation.mapToResponse(updatedUser);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body("User with user id "+id+" deleted successfully");
    }
}
