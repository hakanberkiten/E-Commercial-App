package com.example.e_commerce.service.impl;

import com.example.e_commerce.entity.Role;
import com.example.e_commerce.entity.User;
import com.example.e_commerce.repository.RoleRepository;
import com.example.e_commerce.repository.UserRepository;
import com.example.e_commerce.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;

    @Override
public User saveUser(User user) {
    Integer roleId = user.getRole().getRoleId();
    Role role = roleRepository.findById(roleId)
                               .orElseThrow(() -> new RuntimeException("Role not found"));

    user.setRole(role);  
    return userRepository.save(user);
}


    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }
}
