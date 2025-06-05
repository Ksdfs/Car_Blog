package com.thiCK.CarBlog.repository;

import com.thiCK.CarBlog.entity.Comment;
import com.thiCK.CarBlog.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Integer> {

    List<Comment> findByPost(Post post);

    @Query("SELECT c FROM Comment c WHERE c.user.id = :userId " +
           "AND (:postId IS NULL OR c.post.postId = :postId) " +
           "AND (:search IS NULL OR c.content LIKE %:search%)")
    Page<Comment> findFilteredComments(@Param("userId") Long userId,
                                       @Param("postId") Long postId,
                                       @Param("date") String date,
                                       @Param("search") String search,
                                       Pageable pageable);

    int countByUser_Id(Long userId);

    @Query("SELECT COUNT(DISTINCT c.post.postId) FROM Comment c WHERE c.user.id = :userId")
    int countDistinctPostByUserId(@Param("userId") Long userId);

    @Query("SELECT COALESCE(SUM(c.likeCount), 0) FROM Comment c WHERE c.user.id = :userId")
    int sumLikeCountByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(c2) FROM Comment c1 JOIN c1.replies c2 WHERE c1.user.id = :userId")
    int sumReplyCountByUserId(@Param("userId") Long userId);

    @Query("SELECT DISTINCT c.post FROM Comment c WHERE c.user.id = :userId")
    List<Post> findPostsUserCommented(@Param("userId") Long userId);  // ✅ Đổi về List<Post>
}
