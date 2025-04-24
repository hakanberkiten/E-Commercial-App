package com.example.e_commerce.service;

import com.example.e_commerce.entity.User;
import java.util.List;

public interface UserService {
    User create(User user);
    User getById(Integer id);
    List<User> getAll();
    User update(Integer id, User user);
    void delete(Integer id);
}
