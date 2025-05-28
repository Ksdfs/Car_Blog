package com.thiCK.CarBlog.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.thiCK.CarBlog.entity.Comment;
import com.thiCK.CarBlog.entity.Post;
import com.thiCK.CarBlog.entity.User;
import com.thiCK.CarBlog.service.CommentService;
import com.thiCK.CarBlog.service.PostService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/comments")
public class CommentController {

	@Autowired
	private CommentService commentService;

	@Autowired
	private PostService postService;

	private static final String SESSION_USER = "currentUser";

	/** Thêm comment mới */
	@PostMapping("/add")
	public String addComment(@ModelAttribute Comment comment, @RequestParam("postId") Integer postId,
			HttpSession session, Model model) {

		Post post = postService.getPostById(postId).orElse(null);
		if (post == null) {
			model.addAttribute("error", "Không tìm thấy bài viết");
			return "redirect:/";
		}

		User user = (User) session.getAttribute(SESSION_USER);
		if (user == null) {
			return "redirect:/login";
		}

		comment.setPost(post);
		comment.setUser(user);
		comment.setParentComment(null); // đảm bảo comment gốc
		commentService.saveComment(comment);

		return "redirect:/posts/" + postId;
	}

	/** Xóa comment */
	@PostMapping("/delete/{id}")
	public String deleteComment(@PathVariable("id") Integer id, @RequestParam("postId") Integer postId,
			HttpSession session) {

		User user = (User) session.getAttribute(SESSION_USER);
		if (user == null) {
			return "redirect:/login";
		}

		commentService.getCommentById(id).ifPresent(c -> {
			boolean isOwner = c.getUser().getId().equals(user.getId());
			boolean isAdmin = "ADMIN".equalsIgnoreCase(user.getRole());
			if (isOwner || isAdmin) {
				commentService.deleteCommentById(id);
			}
		});

		return "redirect:/posts/" + postId;
	}

	/** Reply vào comment cha */
	@PostMapping("/reply")
	public String replyComment(@RequestParam("postId") Integer postId,
			@RequestParam("parentCommentId") Integer parentCommentId, @RequestParam("content") String content,
			HttpSession session) {

		Post post = postService.getPostById(postId)
				.orElseThrow(() -> new RuntimeException("Không tìm thấy bài viết id = " + postId));

		Comment parent = commentService.getCommentById(parentCommentId)
				.orElseThrow(() -> new RuntimeException("Không tìm thấy comment id = " + parentCommentId));

		User user = (User) session.getAttribute(SESSION_USER);
		if (user == null) {
			return "redirect:/login";
		}

		Comment reply = new Comment();
		reply.setContent(content);
		reply.setPost(post);
		reply.setUser(user);
		reply.setParentComment(parent);

		commentService.saveComment(reply);

		return "redirect:/posts/" + postId;
	}

	/** Like comment */
	@PostMapping("/like")
	public String likeComment(@RequestParam("commentId") Integer commentId, @RequestParam("postId") Integer postId) {
		Comment c = commentService.getCommentById(commentId)
				.orElseThrow(() -> new RuntimeException("Không tìm thấy comment id=" + commentId));
		c.setLikes(c.getLikes() + 1);
		commentService.saveComment(c);

		return "redirect:/posts/" + postId;
	}
}
