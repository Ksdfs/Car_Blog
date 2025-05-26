package com.thiCK.CarBlog.repository;

import com.thiCK.CarBlog.entity.Comment;
import com.thiCK.CarBlog.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Integer> {
    List<Comment> findByPost(Post post);
}
