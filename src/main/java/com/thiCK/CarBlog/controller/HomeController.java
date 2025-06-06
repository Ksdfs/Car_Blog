package com.thiCK.CarBlog.controller;

import com.thiCK.CarBlog.entity.Category;
import com.thiCK.CarBlog.entity.Post;
import com.thiCK.CarBlog.service.CategoryService;
import com.thiCK.CarBlog.service.PostService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
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

    /**
     * Trang chủ ("/", "/index", "/home"):
     * - Nếu có từ khóa "keyword" thì tìm kiếm prefix trong tất cả bài,
     *   rồi chỉ giữ lại các bài có status = "published".
     * - Nếu không có keyword, lấy page các bài published.
     */
    @GetMapping({ "/", "/index", "/home" })
    public String index(HttpServletRequest request,
                        HttpSession session,
                        Model model,
                        @RequestParam(value = "keyword", required = false) String keyword,
                        @RequestParam(value = "page", defaultValue = "0") int page) {

        // Lấy danh sách tất cả categories
        List<Category> categories = categoryService.findAll();
        model.addAttribute("categories", categories);

        // Tính featuredCategories: 4 category có nhiều bài nhất
        List<Category> featured = categories.stream()
            .sorted(Comparator.comparingInt((Category c) -> c.getPosts().size()).reversed())
            .limit(4)
            .collect(Collectors.toList());
        model.addAttribute("featuredCategories", featured);

        if (keyword != null && !keyword.trim().isEmpty()) {
            // Tìm kiếm tất cả bài chứa prefix (searchByPrefix returns all statuses)
            List<Post> allMatching = postService.searchByPrefix(keyword.trim());

            // Lọc chỉ giữ các post có status = "published"
            List<Post> publishedMatching = allMatching.stream()
                    .filter(p -> "published".equalsIgnoreCase(p.getStatus()))
                    .collect(Collectors.toList());

            model.addAttribute("posts", publishedMatching);
            model.addAttribute("keyword", keyword.trim());
        } else {
            int size = 6;
            // Lấy page chỉ chứa post đã published
            Page<Post> postPage = postService.getPublishedPosts(page, size);

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

    /**
     * API endpoint trả về JSON dùng AJAX live-search:
     * Tương tự, filter chỉ post có status = "published" trên kết quả searchByPrefix.
     */
    @GetMapping("/search")
    @ResponseBody
    public List<Map<String, Object>> searchAjax(@RequestParam("keyword") String keyword) {
        List<Post> allFound = postService.searchByPrefix(keyword.trim());

        // Chỉ giữ post đã published
        List<Post> publishedFound = allFound.stream()
                .filter(p -> "published".equalsIgnoreCase(p.getStatus()))
                .collect(Collectors.toList());

        List<Map<String, Object>> result = new ArrayList<>();
        for (Post p : publishedFound) {
            // Thay đổi chỗ này: nối trực tiếp "/uploads/post/" với tên file chính (mainImage)
            String imgPath = "/uploads/post/" + p.getMainImage();

            Map<String, Object> m = new HashMap<>();
            m.put("id", p.getPostId());
            m.put("title", p.getTitle());
            m.put("image", imgPath);
            result.add(m);
        }
        return result;
    }

    /**
     * API endpoint trả về JSON page posts dùng AJAX (infinite scroll, pagination UI):
     * Lấy page chỉ chứa post đã published
     */
    @GetMapping("/api/posts")
    @ResponseBody
    public Map<String, Object> getPagedPosts(@RequestParam(defaultValue = "0") int page) {
        int size = 6;
        Page<Post> postPage = postService.getPublishedPosts(page, size);

        Map<String, Object> response = new HashMap<>();
        response.put("posts", postPage.getContent());
        response.put("totalPages", postPage.getTotalPages());
        response.put("currentPage", page);
        return response;
    }
}
