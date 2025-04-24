package com.example.e_commerce.service.impl;

import com.example.e_commerce.entity.User;
import com.example.e_commerce.exception.ResourceNotFoundException;
import com.example.e_commerce.repository.UserRepository;
import com.example.e_commerce.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepo;

    @Override
    public User create(User user) {
        return userRepo.save(user);
    }

    @Override
    public User getById(Integer id) {
        return userRepo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
    }

    @Override
    public List<User> getAll() {
        return userRepo.findAll();
    }

    @Override
    public User update(Integer id, User user) {
        User existing = getById(id);
        existing.setUsername(user.getUsername());
        existing.setEmail(user.getEmail());
        existing.setPassword(user.getPassword());
        existing.setPhoneNumber(user.getPhoneNumber());
        existing.setUserRole(user.getUserRole());
        return userRepo.save(existing);
    }

    @Override
    public void delete(Integer id) {
        User existing = getById(id);
        userRepo.delete(existing);
    }
}
