package com.thiCK.CarBlog.controller;

import com.thiCK.CarBlog.entity.User;
import com.thiCK.CarBlog.service.UserService;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class AuthController {
    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    // --- Form Đăng nhập ---
    @GetMapping("/login")
    public String loginForm(HttpSession session) {
        // Nếu đã login thì redirect về trang chủ
        if (session.getAttribute("currentUser") != null) {
            return "redirect:/";
        }
        return "login";
    }

    @PostMapping("/login")
    public String doLogin(@RequestParam String username,
                          @RequestParam String password,
                          HttpSession session,
                          Model model) {
        User u = userService.login(username, password);
        if (u != null) {
            session.setAttribute("currentUser", u);
            return "redirect:/";
        }
        model.addAttribute("error", "Sai tên hoặc mật khẩu");
        return "login";
    }

    // --- Form Đăng ký ---
    @GetMapping("/register")
    public String registerForm(HttpSession session, Model model) {
        // Nếu đã login thì không cho vào trang register
        if (session.getAttribute("currentUser") != null) {
            return "redirect:/";
        }
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String doRegister(@ModelAttribute("user") User user,
                             HttpSession session,
                             Model model) {
        // kiểm tra username/email đã tồn tại
        if (userService.existsByUsername(user.getUsername())) {
            model.addAttribute("error", "Tên đăng nhập đã tồn tại");
            return "register";
        }
        if (userService.existsByEmail(user.getEmail())) {
            model.addAttribute("error", "Email đã được sử dụng");
            return "register";
        }
        // lưu user mới
        User saved = userService.register(user);
        // tự động login sau khi đăng ký
        session.setAttribute("currentUser", saved);
        return "redirect:/";
    }

    // --- Logout ---
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

}
