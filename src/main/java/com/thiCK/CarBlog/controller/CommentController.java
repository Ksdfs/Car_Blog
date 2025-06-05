package com.thiCK.CarBlog.controller;

import com.thiCK.CarBlog.entity.Comment;
import com.thiCK.CarBlog.entity.Post;
import com.thiCK.CarBlog.entity.User;
import com.thiCK.CarBlog.service.CommentService;
import com.thiCK.CarBlog.service.PostService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequestMapping("/comments")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @Autowired
    private PostService postService;

    private static final String SESSION_USER = "currentUser";

    /**
     * Thêm comment mới (comment gốc)
     */
    @PostMapping("/add")
    public String addComment(
            @ModelAttribute Comment comment,
            @RequestParam("postId") Integer postId,
            HttpSession session) {

        // Lấy bài viết theo postId
        Post post = postService.getPostById(postId).orElse(null);
        if (post == null) {
            return "redirect:/"; // nếu không tìm thấy post thì quay về trang chủ
        }

        // Kiểm tra user đã đăng nhập chưa
        User user = (User) session.getAttribute(SESSION_USER);
        if (user == null) {
            return "redirect:/login";
        }

        // Thiết lập quan hệ giữa comment và post, user
        comment.setPost(post);
        comment.setUser(user);
        comment.setParentComment(null); // comment gốc
        commentService.saveComment(comment);

        return "redirect:/posts/" + postId;
    }

    /**
     * Trả lời (reply) vào comment cha
     */
    @PostMapping("/reply")
    public String replyComment(
            @RequestParam("postId") Integer postId,
            @RequestParam("parentCommentId") Integer parentCommentId,
            @RequestParam("content") String content,
            HttpSession session) {

        // Lấy bài viết
        Post post = postService.getPostById(postId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bài viết id = " + postId));

        // Lấy comment cha
        Comment parent = commentService.getCommentById(parentCommentId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy comment id = " + parentCommentId));

        // Kiểm tra user đã đăng nhập chưa
        User user = (User) session.getAttribute(SESSION_USER);
        if (user == null) {
            return "redirect:/login";
        }

        // Tạo đối tượng reply và lưu
        Comment reply = new Comment();
        reply.setContent(content);
        reply.setPost(post);
        reply.setUser(user);
        reply.setParentComment(parent);
        commentService.saveComment(reply);

        return "redirect:/posts/" + postId;
    }

    /**
     * Like comment
     */
    @PostMapping("/like")
    public String likeComment(
            @RequestParam("commentId") Integer commentId,
            @RequestParam("postId") Integer postId) {

        Comment c = commentService.getCommentById(commentId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy comment id=" + commentId));
        c.setLikes(c.getLikes() + 1);
        commentService.saveComment(c);

        return "redirect:/posts/" + postId;
    }

    /**
     * Xóa comment (chỉ chủ comment hoặc ADMIN mới xóa được).
     * Controller sẽ tự động lấy postId từ comment trước khi xóa.
     */
    @PostMapping("/delete/{id}")
    public String deleteComment(
            @PathVariable("id") Integer id,
            HttpSession session) {

        // Lấy user hiện tại từ session
        User user = (User) session.getAttribute(SESSION_USER);
        if (user == null) {
            return "redirect:/login";
        }

        // Lấy comment theo id
        Optional<Comment> opt = commentService.getCommentById(id);
        if (opt.isEmpty()) {
            // Nếu comment không tồn tại, quay về trang quản lý comments (nếu cần)
            return "redirect:/users/comments";
        }

        Comment c = opt.get();
        boolean isOwner = c.getUser().getId().equals(user.getId());
        boolean isAdmin = "ADMIN".equalsIgnoreCase(user.getRole());
        Integer postId = c.getPost().getPostId();

        if (isOwner || isAdmin) {
            commentService.deleteCommentById(id);
        }

        return "redirect:/posts/" + postId;
    }

    /**
     * Xử lý POST khi user lưu thay đổi (Edit Comment).
     * URL: /comments/{id}/edit
     */
    @PostMapping("/{id}/edit")
    public String editComment(
            @PathVariable("id") Integer id,
            @RequestParam("content") String newContent,
            HttpSession session,
            RedirectAttributes redirectAttrs) {

        // Lấy user hiện tại từ session
        User user = (User) session.getAttribute(SESSION_USER);
        if (user == null) {
            return "redirect:/login";
        }

        // Lấy comment theo id
        Optional<Comment> optComment = commentService.getCommentById(id);
        if (optComment.isEmpty()) {
            redirectAttrs.addFlashAttribute("errorMessage", "Comment không tồn tại hoặc đã bị xóa.");
            return "redirect:/users/comments";
        }

        Comment comment = optComment.get();
        boolean isOwner = comment.getUser().getId().equals(user.getId());
        boolean isAdmin = "ADMIN".equalsIgnoreCase(user.getRole());

        if (!isOwner && !isAdmin) {
            redirectAttrs.addFlashAttribute("errorMessage", "Bạn không có quyền chỉnh sửa comment này.");
            return "redirect:/users/comments";
        }

        // Cập nhật nội dung mới và lưu
        comment.setContent(newContent);
        commentService.saveComment(comment);

        redirectAttrs.addFlashAttribute("successMessage", "Cập nhật comment thành công.");
        return "redirect:/users/comments";
    }

}
