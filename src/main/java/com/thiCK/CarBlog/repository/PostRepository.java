package com.thiCK.CarBlog.repository;

import com.thiCK.CarBlog.entity.Post;
import com.thiCK.CarBlog.entity.Category; 
import com.thiCK.CarBlog.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Integer> {

    // Trả về danh sách bài theo trạng thái (draft, published,...)
    List<Post> findByStatus(String status);

    // Trả về tất cả bài viết kèm theo category và user (JOIN FETCH)
    @Query("SELECT p FROM Post p JOIN FETCH p.category JOIN FETCH p.user ORDER BY p.createdAt DESC")
    List<Post> findAllWithCategory();
    
    // Lấy tất cả bài viết của một user
    List<Post> findAllByUser(User user);
    
    // Lấy top 5 bài cùng category, loại bỏ bài hiện tại, sắp theo createdAt giảm dần
    List<Post> findTop5ByCategory_CategoryIdAndPostIdNotOrderByCreatedAtDesc(Integer categoryId, Integer postId);

    // Lấy tất cả bài viết theo category
    List<Post> findByCategory(Category category);
    
    // Tìm bài viết theo tiêu đề bắt đầu bằng keyword, không phân biệt hoa thường
    List<Post> findByTitleStartingWithIgnoreCase(String titlePrefix);
    
    Page<Post> findByUser(User user, Pageable pageable);

    Page<Post> findByUserAndStatus(User user, String status, Pageable pageable);

    Page<Post> findByUserAndCategory_CategoryId(User user, Integer categoryId, Pageable pageable);

    Page<Post> findByUserAndStatusAndCategory_CategoryId(User user, String status, Integer categoryId, Pageable pageable);
    
    Page<Post> findByStatus(String status, Pageable pageable);


}
