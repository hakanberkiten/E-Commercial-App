package com.example.e_commerce.controller;

import com.example.e_commerce.entity.Role;
import com.example.e_commerce.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
public class RoleController {

    @Autowired
    private RoleRepository roleRepository;

    @GetMapping
    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    @PostMapping("/save")
    public Role saveRole(@RequestBody Role role) {
        return roleRepository.save(role);
    }
}
