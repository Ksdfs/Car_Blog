package com.thiCK.CarBlog.controller;

import com.thiCK.CarBlog.entity.Comment;
import com.thiCK.CarBlog.entity.Post;
import com.thiCK.CarBlog.entity.User;
import com.thiCK.CarBlog.service.CategoryService;
import com.thiCK.CarBlog.service.PostService;
import com.thiCK.CarBlog.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/posts")
public class PostController {

    private final PostService postService;
    private final CategoryService categoryService;
    private final UserService userService;

    public PostController(PostService postService,
                          CategoryService categoryService,
                          UserService userService) {
        this.postService     = postService;
        this.categoryService = categoryService;
        this.userService     = userService;
    }

    /** Danh sách bài viết */
    @GetMapping({"", "/"})
    public String index(Model model) {
        model.addAttribute("posts", postService.findAllWithCategory());
        model.addAttribute("categories", categoryService.findAll());
        return "index";
    }

    /** Form tạo mới */
    @GetMapping({"/new", "/create"})
    public String showCreateForm(Model model) {
        model.addAttribute("post", new Post());
        model.addAttribute("categories", categoryService.findAll());
        return "new_post";
    }

    /** Xử lý tạo mới */
    @PostMapping({"/new", "/create"})
    public String handleCreate(
            @ModelAttribute("post") Post post,
            @RequestParam(value = "mainImageFile", required = false) MultipartFile mainImageFile,
            @RequestParam(value = "imageFiles", required = false) MultipartFile[] imageFiles,
            HttpSession session
    ) throws IOException {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/login";
        }
        post.setUser(currentUser);
        post.setCreatedAt(LocalDateTime.now());
        postService.save(post, mainImageFile, imageFiles);
        return "redirect:/posts/";
    }

    /** Form chỉnh sửa */
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Integer id, Model model) {
        Optional<Post> opt = postService.findById(id);
        if (opt.isEmpty()) {
            return "redirect:/posts/";
        }
        model.addAttribute("post", opt.get());
        model.addAttribute("categories", categoryService.findAll());
        return "edit_post";
    }

    /** Xử lý cập nhật */
    @PostMapping("/{id}/edit")
    public String updatePost(
            @PathVariable Integer id,
            @ModelAttribute("post") Post postData,
            @RequestParam(value = "mainImageFile", required = false) MultipartFile mainImageFile,
            @RequestParam(value = "imageFiles", required = false) MultipartFile[] imageFiles
    ) throws IOException {
        postService.updatePost(id, postData, mainImageFile, imageFiles);
        return "redirect:/posts/";
    }

    /** Xóa bài viết */
    @PostMapping("/{id}/delete")
    public String deletePost(@PathVariable Integer id) {
        postService.deletePost(id);
        return "redirect:/posts/";
    }

    /** Chi tiết bài viết (GET /posts/{id}) */
    @GetMapping("/{id}")
    public String postDetail(@PathVariable Integer id,
                             Model model,
                             HttpSession session) {
        Optional<Post> opt = postService.findById(id);
        if (opt.isEmpty()) {
            return "redirect:/posts/";
        }

        Post post = opt.get();
        model.addAttribute("post", post);
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("newComment", new Comment());

        // Related posts
        List<Post> relatedPosts = postService.findTop5ByCategoryAndNotId(
            post.getCategory().getCategoryId(),
            post.getPostId()
        );
        model.addAttribute("relatedPosts", relatedPosts);

        return "post_detail";
    }
}
