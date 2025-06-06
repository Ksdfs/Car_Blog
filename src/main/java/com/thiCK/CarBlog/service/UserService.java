package com.thiCK.CarBlog.service;

import com.thiCK.CarBlog.entity.User;
import com.thiCK.CarBlog.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepo;

    public UserService(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    /** Lưu thẳng password vào DB */
    public User register(User user) {
        return userRepo.save(user);
    }

    /** So sánh raw password */
    public User login(String username, String rawPassword) {
        return userRepo.findByUsername(username)
            .filter(u -> rawPassword.equals(u.getPassword()))
            .orElse(null);
    }

    /** Kiểm tra username đã tồn tại */
    public boolean existsByUsername(String username) {
        return userRepo.findByUsername(username).isPresent();
    }

    /** Kiểm tra email đã tồn tại */
    public boolean existsByEmail(String email) {
        return userRepo.findByEmail(email).isPresent();
    }

    /** Tìm user theo username */
    public Optional<User> findByUsername(String username) {
        return userRepo.findByUsername(username);
    }

    /** Tìm user theo id */
    public Optional<User> findById(Long id) {
        return userRepo.findById(id);
    }
    
    /** Lưu hoặc cập nhật user */
    public User save(User user) {
        return userRepo.save(user);
    }

    /** Kiểm tra xem raw currentPassword có đúng với mật khẩu hiện tại (lưu plain-text) */
    public boolean checkIfValidOldPassword(User user, String currentPassword) {
        // So sánh trực tiếp raw password
        return currentPassword.equals(user.getPassword());
    }

    /** Cập nhật mật khẩu mới đơn giản: lưu thẳng raw password vào DB */
    public void updatePassword(User user, String newPassword) {
        user.setPassword(newPassword);
        userRepo.save(user);
    }
    
    /** Xóa user theo thực thể */
    public void delete(User user) {
        userRepo.delete(user);
    }

    /** Xóa user theo id */
    public void deleteById(Long userId) {
        userRepo.deleteById(userId);
    }
}
