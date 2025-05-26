package com.thiCK.CarBlog.controller;

import com.thiCK.CarBlog.entity.Post;
import com.thiCK.CarBlog.entity.User;
import com.thiCK.CarBlog.service.CategoryService;
import com.thiCK.CarBlog.service.PostService;
import com.thiCK.CarBlog.service.UserService;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class UserController {

    private final PostService postService;
    private final CategoryService categoryService;
    private final UserService userService;
    private final ServletContext servletContext;

    public UserController(PostService postService,
                          CategoryService categoryService,
                          UserService userService,
                          ServletContext servletContext) {
        this.postService     = postService;
        this.categoryService = categoryService;
        this.userService     = userService;
        this.servletContext  = servletContext;
    }

    /**
     * Trang My Posts của user
     */
    @GetMapping("/post")
    public String myPosts(HttpSession session,
                          Model model,
                          @RequestParam(name = "status",   required = false) String   status,
                          @RequestParam(name = "category", required = false) Integer  categoryId,
                          @RequestParam(name = "sort",     defaultValue = "newest") String sort) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/login";
        }

        // Lấy tất cả bài của user
        List<Post> userPosts = postService.findAllByUser(currentUser);

        // Lọc theo status và category nếu có
        List<Post> filtered = userPosts.stream()
            .filter(p -> !StringUtils.hasText(status) || p.getStatus().equals(status))
            .filter(p -> categoryId == null 
                      || (p.getCategory() != null 
                          && p.getCategory().getCategoryId().equals(categoryId)))
            .collect(Collectors.toList());

        // Sắp xếp
        Comparator<Post> comp;
        switch (sort) {
            case "oldest"        -> comp = Comparator.comparing(Post::getCreatedAt);
            case "most-viewed"   -> comp = Comparator.comparingInt(Post::getViewCount).reversed();
            case "most-commented"-> comp = Comparator.<Post>comparingInt(p -> p.getComments().size()).reversed();
            default              -> comp = Comparator.comparing(Post::getCreatedAt).reversed();
        }
        filtered.sort(comp);

        // Tính các số liệu tổng hợp
        int totalPosts    = filtered.size();
        int totalViews    = filtered.stream().mapToInt(Post::getViewCount).sum();
        int totalComments = filtered.stream().mapToInt(p -> p.getComments().size()).sum();
        int totalLikes    = filtered.stream().mapToInt(Post::getLikeCount).sum();

        // Đẩy vào model
        model.addAttribute("userPosts",     filtered);
        model.addAttribute("categories",    categoryService.findAll());
        model.addAttribute("status",        status);
        model.addAttribute("categoryId",    categoryId);
        model.addAttribute("sort",          sort);
        model.addAttribute("totalPosts",    totalPosts);
        model.addAttribute("totalViews",    totalViews);
        model.addAttribute("totalComments", totalComments);
        model.addAttribute("totalLikes",    totalLikes);

        return "post";
    }

    /**
     * Hiển thị trang Profile
     * Nếu ?success có mặt sẽ trả về model flag để bạn show toast hoặc badge
     */
    @GetMapping("/profile")
    public String showProfile(HttpSession session,
                              Model model,
                              @RequestParam(value="success", required=false) String success) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/login";
        }

        // Tính stats
        List<Post> userPosts = postService.findAllByUser(currentUser);
        Map<String,Integer> userStats = new HashMap<>();
        userStats.put("totalPosts",    userPosts.size());
        userStats.put("totalViews",    userPosts.stream().mapToInt(Post::getViewCount).sum());
        userStats.put("totalComments", userPosts.stream().mapToInt(p->p.getComments().size()).sum());
        userStats.put("totalLikes",    userPosts.stream().mapToInt(Post::getLikeCount).sum());

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("userStats",    userStats);
        model.addAttribute("success",      success != null);
        return "profile";
    }

    /**
     * Xử lý POST từ form update Profile (có upload avatar)
     */
    @PostMapping("/profile/update")
    public String updateProfile(
            @RequestParam(value = "fullName",    required = false) String      fullName,
            @RequestParam(value = "phoneNumber", required = false) String      phoneNumber,
            @RequestParam(value = "gender",      required = false) String      gender,
            @RequestParam(value = "birthDate",   required = false) String      birthDate,
            @RequestParam(value = "bio",         required = false) String      bio,
            @RequestParam(value = "avatar",      required = false) MultipartFile avatarFile,
            HttpSession session
    ) throws IOException {

        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/login";
        }

        // Cập nhật thông tin text
        if (StringUtils.hasText(fullName))    currentUser.setFullName(fullName);
        if (StringUtils.hasText(phoneNumber)) currentUser.setPhoneNumber(phoneNumber);
        if (StringUtils.hasText(gender))      currentUser.setGender(gender);
        if (StringUtils.hasText(birthDate)) {
            try {
                currentUser.setBirthDate(LocalDate.parse(birthDate));
            } catch (Exception ignore) { }
        }
        currentUser.setBio(bio);

        // Upload avatar nếu có
        if (avatarFile != null && !avatarFile.isEmpty()) {
            // Đường dẫn thật đến folder trong webapp: .../webapps/ROOT/uploads/avatars/
            String realPath  = servletContext.getRealPath("/uploads/avatars/");
            File   uploadDir = new File(realPath);
            if (!uploadDir.exists()) uploadDir.mkdirs();

            // Tạo filename ngẫu nhiên + gốc
            String filename = UUID.randomUUID() + "_" +
                    StringUtils.cleanPath(avatarFile.getOriginalFilename());
            File dest = new File(uploadDir, filename);

            // Ghi file
            avatarFile.transferTo(dest);

            // Lưu đường dẫn public (so với webroot)
            currentUser.setAvatar("/uploads/avatars/" + filename);
        }

        // Lưu vào DB & cập nhật session
        userService.save(currentUser);
        session.setAttribute("currentUser", currentUser);

        // Redirect về /profile?success để show thông báo
        return "redirect:/profile?success";
    }

}
