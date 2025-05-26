package com.thiCK.CarBlog.service;

import com.thiCK.CarBlog.entity.Category;
import com.thiCK.CarBlog.repository.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {
    private final CategoryRepository categoryRepo;

    public CategoryService(CategoryRepository categoryRepo) {
        this.categoryRepo = categoryRepo;
    }

    public List<Category> findAll() {
        return categoryRepo.findAll();
    }
    
    public Category findById(Integer id) {
        return categoryRepo.findById(id)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy chuyên mục id=" + id));
    }

}
