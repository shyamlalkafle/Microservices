package com.microservices.UserService.service;

import com.microservices.UserService.entity.User;

import java.util.List;

public interface UserService {

    List<User> getAllUser();

    User getUserById(Long id);

    boolean existById(Long id);

    User createUser(User user);

    User updateUser(Long id, User updatedUser);

    boolean deleteUser(Long id);
}
