package com.example.e_commerce.service;

import com.example.e_commerce.entity.User;
import java.util.List;

public interface UserService {
    User saveUser(User user);
    List<User> getAllUsers();
    User getUserById(Long id);
    
}
