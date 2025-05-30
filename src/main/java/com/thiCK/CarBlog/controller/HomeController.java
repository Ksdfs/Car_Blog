package com.thiCK.CarBlog.controller;

import com.thiCK.CarBlog.entity.Category;
import com.thiCK.CarBlog.entity.Post;
import com.thiCK.CarBlog.service.CategoryService;
import com.thiCK.CarBlog.service.PostService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;
import java.util.stream.Collectors;

@Controller
public class HomeController {

    private final PostService postService;
    private final CategoryService categoryService;

    public HomeController(PostService postService,
                          CategoryService categoryService) {
        this.postService = postService;
        this.categoryService = categoryService;
    }

    @GetMapping({"/", "/index", "/home"})
    public String index(HttpServletRequest request,
                        HttpSession session,
                        Model model,
                        @RequestParam(value = "keyword", required = false) String keyword,
                        @RequestParam(value = "page", defaultValue = "0") int page) {

        List<Category> categories = categoryService.findAll();
        model.addAttribute("categories", categories);

        List<Category> featured = categories.stream()
            .sorted(Comparator.comparingInt((Category c) -> c.getPosts().size()).reversed())
            .limit(4)
            .collect(Collectors.toList());
        model.addAttribute("featuredCategories", featured);

        if (keyword != null && !keyword.trim().isEmpty()) {
            List<Post> searchResults = postService.searchByPrefix(keyword.trim());
            model.addAttribute("posts", searchResults);
            model.addAttribute("keyword", keyword);
        } else {
            int size = 6;
            var postPage = postService.getLatestPosts(page, size);
            model.addAttribute("posts", postPage.getContent());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", postPage.getTotalPages());
        }

        model.addAttribute("currentUser", session.getAttribute("currentUser"));
        model.addAttribute("currentPath", request.getRequestURI());
        return "index";
    }


    @GetMapping("/about")
    public String about() {
        return "about";
    }

    @GetMapping("/contact")
    public String contact() {
        return "contact";
    }

	/*
	 * @GetMapping("/posts/{id}") public String viewPost(HttpServletRequest request,
	 * 
	 * @PathVariable("id") Integer id, HttpSession session, Model model) { Post post
	 * = postService.findById(id) .orElseThrow(() -> new
	 * RuntimeException("Không tìm thấy bài viết id=" + id));
	 * 
	 * List<Post> userPosts = postService.findAllByUser(post.getUser()).stream()
	 * .filter(p -> !p.getPostId().equals(id)) .collect(Collectors.toList());
	 * 
	 * model.addAttribute("post", post); model.addAttribute("categories",
	 * categoryService.findAll()); model.addAttribute("userPosts", userPosts);
	 * 
	 * return "post_detail"; }
	 */

    // API endpoint trả về JSON dùng AJAX live-search
    @GetMapping("/search")
    @ResponseBody
    public List<Map<String, Object>> searchAjax(@RequestParam("keyword") String keyword) {
        List<Post> found = postService.searchByPrefix(keyword.trim());
        List<Map<String, Object>> result = new ArrayList<>();
        for (Post p : found) {
            String img = p.getMainImage();
            if (!img.startsWith("/")) {
                img = "/" + img;
            }
            Map<String, Object> m = new HashMap<>();
            m.put("id", p.getPostId());
            m.put("title", p.getTitle());
            m.put("image", img);
            result.add(m);
        }
        return result;
    }
    
    @GetMapping("/api/posts")
    @ResponseBody
    public Map<String, Object> getPagedPosts(@RequestParam(defaultValue = "0") int page) {
        int size = 6;
        var postPage = postService.getLatestPosts(page, size);
        Map<String, Object> response = new HashMap<>();
        response.put("posts", postPage.getContent());
        response.put("totalPages", postPage.getTotalPages());
        response.put("currentPage", page);
        return response;
    }

}
