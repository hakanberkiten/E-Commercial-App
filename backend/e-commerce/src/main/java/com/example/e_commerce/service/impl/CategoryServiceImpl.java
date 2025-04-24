package com.example.e_commerce.service.impl;


import com.example.e_commerce.dto.CategoryDTO;
import com.example.e_commerce.entity.Category;
import com.example.e_commerce.exception.ResourceNotFoundException;
import com.example.e_commerce.repository.CategoryRepository;
import com.example.e_commerce.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository repo;

    private CategoryDTO toDto(Category e) {
        var dto = new CategoryDTO();
        dto.setId(e.getId());
        dto.setName(e.getName());
        return dto;
    }

    private Category toEntity(CategoryDTO dto) {
        var e = new Category();
        e.setName(dto.getName());
        return e;
    }

    @Override
    public CategoryDTO create(CategoryDTO dto) {
        Category saved = repo.save(toEntity(dto));
        return toDto(saved);
    }

    @Override
    public CategoryDTO getById(Integer id) {
        Category e = repo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Category","id",id));
        return toDto(e);
    }

    @Override
    public List<CategoryDTO> getAll() {
        return repo.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public CategoryDTO update(Integer id, CategoryDTO dto) {
        Category e = repo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Category","id",id));
        e.setName(dto.getName());
        return toDto(repo.save(e));
    }

    @Override
    public void delete(Integer id) {
        Category e = repo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Category","id",id));
        repo.delete(e);
    }
}
