package com.thiCK.CarBlog.repository;

import com.thiCK.CarBlog.entity.Post;
import com.thiCK.CarBlog.entity.Category; 
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import com.thiCK.CarBlog.entity.User;

public interface PostRepository extends JpaRepository<Post, Integer> {

    // Trả về danh sách bài theo trạng thái (draft, published,...)
    List<Post> findByStatus(String status);

    // Trả về tất cả bài viết kèm theo category (dùng JOIN FETCH)
    @Query("SELECT p FROM Post p JOIN FETCH p.category JOIN FETCH p.user ORDER BY p.createdAt DESC")
    List<Post> findAllWithCategory();
    
    // thêm phương thức lấy tất cả bài của 1 user
    List<Post> findAllByUser(User user);
    
    // Lấy top 5 bài cùng category, loại bỏ bài hiện tại, sắp theo createdAt giảm dần
    List<Post> findTop5ByCategory_CategoryIdAndPostIdNotOrderByCreatedAtDesc(
        Integer categoryId,
        Integer postId
    );
    
    List<Post> findByCategory(Category category);
    
    // Chỉ tìm theo tiêu đề bắt đầu bằng keyword (không phân biệt hoa thường)
    List<Post> findByTitleStartingWithIgnoreCase(String titlePrefix);
    
}
