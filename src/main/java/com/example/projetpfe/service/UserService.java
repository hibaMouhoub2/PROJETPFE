package com.example.projetpfe.service;

import com.example.projetpfe.dto.UserDto;
import com.example.projetpfe.entity.User;

import java.util.List;

public interface UserService {
    void saveUser(UserDto userDto);

    User findByEmail(String email);
    void deleteUser(Long id);
    void updateUser(Long id, UserDto userDto);
    UserDto findUserById(Long id);
    List<UserDto> findAllUsers();
    User findUserEntityById(Long id);
}
