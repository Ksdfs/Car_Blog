package com.thiCK.CarBlog.controller;

import com.thiCK.CarBlog.entity.Comment;
import com.thiCK.CarBlog.entity.Post;
import com.thiCK.CarBlog.entity.User;
import com.thiCK.CarBlog.service.CategoryService;
import com.thiCK.CarBlog.service.CommentService;
import com.thiCK.CarBlog.service.PostService;
import com.thiCK.CarBlog.service.UserService;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpSession;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
    private final CommentService commentService;
    private final ServletContext servletContext;

    public UserController(PostService postService,
                          CategoryService categoryService,
                          UserService userService,
                          CommentService commentService,
                          ServletContext servletContext) {
        this.postService     = postService;
        this.categoryService = categoryService;
        this.userService     = userService;
        this.commentService  = commentService;
        this.servletContext  = servletContext;
    }

    @GetMapping("/post")
    public String myPosts(HttpSession session,
                          Model model,
                          @RequestParam(name = "status",   required = false) String status,
                          @RequestParam(name = "category", required = false) Integer categoryId,
                          @RequestParam(name = "sort",     defaultValue = "newest") String sort,
                          @RequestParam(name = "page",     defaultValue = "0") int page) {

        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/login";
        }

        List<Post> userPosts = postService.findAllByUser(currentUser);

        List<Post> filtered = userPosts.stream()
            .filter(p -> !StringUtils.hasText(status) || p.getStatus().equals(status))
            .filter(p -> categoryId == null 
                      || (p.getCategory() != null 
                          && p.getCategory().getCategoryId().equals(categoryId)))
            .collect(Collectors.toList());

        Comparator<Post> comp;
        switch (sort) {
            case "oldest"         -> comp = Comparator.comparing(Post::getCreatedAt);
            case "most-viewed"    -> comp = Comparator.comparingInt(Post::getViewCount).reversed();
            case "most-commented" -> comp = Comparator.<Post>comparingInt(p -> p.getComments().size()).reversed();
            default               -> comp = Comparator.comparing(Post::getCreatedAt).reversed();
        }
        filtered.sort(comp);

        int size = 6;
        int totalPosts = filtered.size();
        int totalPages = (int) Math.ceil((double) totalPosts / size);
        int fromIndex = Math.min(page * size, totalPosts);
        int toIndex   = Math.min(fromIndex + size, totalPosts);
        List<Post> pagePosts = filtered.subList(fromIndex, toIndex);

        int totalViews    = filtered.stream().mapToInt(Post::getViewCount).sum();
        int totalComments = filtered.stream().mapToInt(p -> p.getComments().size()).sum();
        int totalLikes    = filtered.stream().mapToInt(Post::getLikeCount).sum();

        model.addAttribute("userPosts",     pagePosts);
        model.addAttribute("categories",    categoryService.findAll());
        model.addAttribute("status",        status);
        model.addAttribute("categoryId",    categoryId);
        model.addAttribute("sort",          sort);
        model.addAttribute("totalPosts",    totalPosts);
        model.addAttribute("totalViews",    totalViews);
        model.addAttribute("totalComments", totalComments);
        model.addAttribute("totalLikes",    totalLikes);
        model.addAttribute("currentPage",   page);
        model.addAttribute("totalPages",    totalPages);

        return "post";
    }

    @GetMapping("/users/comments")
    public String myComments(Model model, HttpSession session,
                             @RequestParam(defaultValue = "0") int page,
                             @RequestParam(required = false) Long post,
                             @RequestParam(required = false) String date,
                             @RequestParam(required = false) String sort,
                             @RequestParam(required = false) String search) {

        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/login";
        }

        Page<Comment> commentsPage = commentService.getCommentsByUser(
            currentUser.getUserId(), post, date, sort, search, page
        );

        model.addAttribute("userComments", commentsPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", commentsPage.getTotalPages());

        model.addAttribute("totalComments", commentService.countByUser(currentUser.getUserId()));
        model.addAttribute("postsCommented", commentService.countPostsCommentedByUser(currentUser.getUserId()));
        model.addAttribute("totalLikes", commentService.countLikesByUser(currentUser.getUserId()));
        model.addAttribute("totalReplies", commentService.countRepliesByUser(currentUser.getUserId()));

        model.addAttribute("commentedPosts", commentService.getPostsUserCommented(currentUser.getUserId()));

        return "my_comments";
    }

    @GetMapping("/profile")
    public String showProfile(HttpSession session,
                              Model model,
                              @RequestParam(value="success", required=false) String success,
                              @RequestParam(value="error",   required=false) String error) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/login";
        }

        List<Post> userPosts = postService.findAllByUser(currentUser);
        Map<String,Integer> userStats = new HashMap<>();
        userStats.put("totalPosts",    userPosts.size());
        userStats.put("totalViews",    userPosts.stream().mapToInt(Post::getViewCount).sum());
        userStats.put("totalComments", userPosts.stream().mapToInt(p->p.getComments().size()).sum());
        userStats.put("totalLikes",    userPosts.stream().mapToInt(Post::getLikeCount).sum());

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("userStats",   userStats);
        model.addAttribute("success",     success != null);
        model.addAttribute("error",       error);

        return "profile";
    }

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

        // Cập nhật các trường thông tin khác …
        if (StringUtils.hasText(fullName))    currentUser.setFullName(fullName);
        if (StringUtils.hasText(phoneNumber)) currentUser.setPhoneNumber(phoneNumber);
        if (StringUtils.hasText(gender))      currentUser.setGender(gender);
        if (StringUtils.hasText(birthDate)) {
            try {
                currentUser.setBirthDate(LocalDate.parse(birthDate));
            } catch (Exception ignore) { }
        }
        currentUser.setBio(bio);

        // Lưu avatar vào src/main/resources/static/uploads/avatars
        if (avatarFile != null && !avatarFile.isEmpty()) {
            /*
             * 1. Xác định thư mục đích là: <project-root>/src/main/resources/static/uploads/avatars
             *    Khi chạy từ IDE, working directory thường là <project-root>.
             */
            String projectDir       = System.getProperty("user.dir"); 
            String avatarsDirPath   = projectDir 
                                       + "/src/main/resources/static/uploads/avatars";
            File   avatarsDir        = new File(avatarsDirPath);
            if (!avatarsDir.exists()) {
                avatarsDir.mkdirs(); 
            }

            // 2. Tạo tên file duy nhất
            String originalFilename = StringUtils.cleanPath(avatarFile.getOriginalFilename());
            String filename = UUID.randomUUID() + "_" + originalFilename;

            // 3. Tạo file đích và lưu
            File dest = new File(avatarsDir, filename);
            avatarFile.transferTo(dest);

            // 4. Lưu đường dẫn tương đối để hiển thị avatar
            //    Khi chạy server, static files ở /static sẽ được serve tại http://localhost:8080/
            //    Do vậy URL avatar sẽ là: /uploads/avatars/{filename}
            currentUser.setAvatar("/uploads/avatars/" + filename);
        }

        userService.save(currentUser);
        session.setAttribute("currentUser", currentUser);

        return "redirect:/profile?success";
    }

    /**
     * POST xử lý thay đổi mật khẩu (plain-text).
     */
    @PostMapping("/profile/change-password")
    public String changePassword(
            @RequestParam("currentPassword") String currentPassword,
            @RequestParam("newPassword")     String newPassword,
            @RequestParam("confirmPassword") String confirmPassword,
            HttpSession session,
            RedirectAttributes redirectAttrs) {

        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            redirectAttrs.addFlashAttribute("error", "Vui lòng đăng nhập để đổi mật khẩu.");
            return "redirect:/login";
        }

        // 1. Kiểm tra mật khẩu hiện tại (plain-text so sánh trực tiếp)
        if (!userService.checkIfValidOldPassword(currentUser, currentPassword)) {
            redirectAttrs.addAttribute("error", "Mật khẩu hiện tại không đúng.");
            return "redirect:/profile";
        }

        // 2. Kiểm tra mật khẩu mới và xác nhận có khớp không
        if (!newPassword.equals(confirmPassword)) {
            redirectAttrs.addAttribute("error", "Mật khẩu mới và xác nhận không khớp.");
            return "redirect:/profile";
        }

        // 3. Cập nhật mật khẩu mới (plain-text)
        userService.updatePassword(currentUser, newPassword);

        // 4. Cập nhật session với user đã đổi mật khẩu
        session.setAttribute("currentUser", currentUser);

        // 5. Redirect với tham số success
        redirectAttrs.addAttribute("success", "true");
        return "redirect:/profile";
    }

    
    /**
     * Xử lý xóa tài khoản khi user bấm “Delete Account”.
     * Khi user xác nhận, JS sẽ redirect GET /profile/delete-account
     */
    @GetMapping("/profile/delete-account")
    public String deleteAccount(HttpSession session,
                                RedirectAttributes redirectAttrs) {
        // 1. Lấy user hiện tại từ session
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            // Nếu chưa login, quay về trang chủ
            redirectAttrs.addFlashAttribute("message", "Bạn cần phải đăng nhập để xóa tài khoản.");
            return "redirect:/";
        }

        // 2. Xóa user
        userService.delete(currentUser);
        // Hoặc: userService.deleteById(currentUser.getUserId());

        // 3. Hủy session để logout
        session.invalidate();

        // 4. Redirect về trang chủ, kèm flash message (nếu cần)
        redirectAttrs.addFlashAttribute("message", "Tài khoản đã được xóa thành công.");
        return "redirect:/";
    }

}
