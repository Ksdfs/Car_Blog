package com.thiCK.CarBlog.controller;

import com.thiCK.CarBlog.entity.Category;
import com.thiCK.CarBlog.entity.Post;
import com.thiCK.CarBlog.service.CategoryService;
import com.thiCK.CarBlog.service.PostService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class CategoryController {

    private final CategoryService categoryService;
    private final PostService postService;

    public CategoryController(CategoryService categoryService, PostService postService) {
        this.categoryService = categoryService;
        this.postService = postService;
    }

    @GetMapping("/category/{id}")
    public String viewCategory(@PathVariable("id") Integer id, Model model) {
        Category category = categoryService.findById(id);
        List<Post> categoryPosts = postService.findByCategory(category);

        model.addAttribute("category", category);
        model.addAttribute("categoryPosts", categoryPosts);
        model.addAttribute("totalPosts", categoryPosts.size());

        int totalViews = categoryPosts.stream()
                                      .mapToInt(Post::getViewCount)
                                      .sum();
        model.addAttribute("totalViews", totalViews);

        List<Category> otherCategories = categoryService.findAll().stream()
            .filter(c -> !c.getCategoryId().equals(id))
            .collect(Collectors.toList());
        model.addAttribute("otherCategories", otherCategories);

        // Sắp xếp viewCount giảm dần
        List<Post> popularPosts = categoryPosts.stream()
            .sorted(Comparator.comparingInt(Post::getViewCount).reversed())
            .limit(5)
            .collect(Collectors.toList());
        model.addAttribute("popularPosts", popularPosts);

        return "category_post";
    }
}
