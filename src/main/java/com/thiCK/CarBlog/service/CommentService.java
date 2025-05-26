package com.thiCK.CarBlog.service;

import com.thiCK.CarBlog.entity.Comment;
import com.thiCK.CarBlog.entity.Post;
import com.thiCK.CarBlog.repository.CommentRepository;
import com.thiCK.CarBlog.repository.PostRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private PostRepository postRepository;

    public List<Comment> getCommentsByPost(Post post) {
        return commentRepository.findByPost(post);
    }

    public Optional<Comment> getCommentById(Integer id) {
        return commentRepository.findById(id);
    }

    public Comment saveComment(Comment comment) {
        return commentRepository.save(comment);
    }

    @Transactional
    public void deleteCommentById(Integer id) {
        commentRepository.deleteById(id);
    }

    public Post getPostById(Integer id) {
        return postRepository.findById(id).orElse(null);
    }
}
