package com.thiCK.CarBlog.service;

import com.thiCK.CarBlog.entity.Comment;
import com.thiCK.CarBlog.entity.Post;
import com.thiCK.CarBlog.repository.CommentRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;

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

    public Page<Comment> getCommentsByUser(Long userId, Long postId, String date, String sort, String search, int page) {
        Pageable pageable = PageRequest.of(page, 6, getSortOrder(sort));
        return commentRepository.findFilteredComments(userId, postId, date, search, pageable);
    }

    private Sort getSortOrder(String sort) {
        if (sort == null) sort = "newest";
        return switch (sort) {
            case "oldest"       -> Sort.by("createdAt").ascending();
            case "most-liked"   -> Sort.by("likeCount").descending();
            case "most-replied" -> Sort.by("replies.size").descending(); // nếu lỗi, hãy xóa dòng này
            default             -> Sort.by("createdAt").descending();
        };
    }

    // Thống kê
    public int countByUser(Long userId) {
        return commentRepository.countByUser_Id(userId);
    }

    public int countPostsCommentedByUser(Long userId) {
        return commentRepository.countDistinctPostByUserId(userId);
    }

    public int countLikesByUser(Long userId) {
        return commentRepository.sumLikeCountByUserId(userId);
    }

    public int countRepliesByUser(Long userId) {
        return commentRepository.sumReplyCountByUserId(userId);
    }

    // Đổi kiểu trả về từ Object[] sang Post
    public List<Post> getPostsUserCommented(Long userId) {
        return commentRepository.findPostsUserCommented(userId);
    }
}
