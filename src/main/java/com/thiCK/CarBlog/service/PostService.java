package com.thiCK.CarBlog.service;

import com.thiCK.CarBlog.entity.Post;
import com.thiCK.CarBlog.entity.PostImage;
import com.thiCK.CarBlog.entity.User;
import com.thiCK.CarBlog.entity.Category;
import com.thiCK.CarBlog.repository.PostRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;



import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class PostService {

    // Thư mục lưu file: src/main/resources/static/uploads/post
    private final Path uploadDir = Paths.get("src/main/resources/static/uploads/post");

    private final PostRepository postRepo;

    public PostService(PostRepository postRepo) {
        this.postRepo = postRepo;
        try {
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }
        } catch (IOException e) {
            throw new RuntimeException("Không thể tạo thư mục: " + uploadDir, e);
        }
    }

    /**
     * Tạo mới bài viết, kèm upload ảnh chính và ảnh gallery.
     */
    public Post save(Post post,
                     MultipartFile mainImageFile,
                     MultipartFile[] imageFiles) throws IOException {

        // Lưu ảnh chính nếu có
        if (mainImageFile != null && !mainImageFile.isEmpty()) {
            String filename = saveFile(mainImageFile);
            // **Chỉ lưu filename** (không kèm /uploads/)
            post.setMainImage(filename);
        }

        // Lưu gallery nếu có
        if (imageFiles != null) {
            for (MultipartFile mf : imageFiles) {
                if (!mf.isEmpty()) {
                    String filename = saveFile(mf);
                    PostImage pi = new PostImage();
                    // Chỉ lưu filename
                    pi.setImageUrl(filename);
                    pi.setPost(post);
                    post.getImages().add(pi);
                }
            }
        }

        return postRepo.save(post);
    }

    /** Lấy tất cả bài viết */
    public List<Post> findAll() {
        return postRepo.findAll();
    }

    /** Lấy tất cả bài viết kèm category (JOIN FETCH) */
    public List<Post> findAllWithCategory() {
        return postRepo.findAllWithCategory();
    }

    /** Lấy 1 bài theo ID */
    public Optional<Post> findById(Integer id) {
        return postRepo.findById(id);
    }

    /** Lấy các bài theo trạng thái */
    public List<Post> findByStatus(String status) {
        return postRepo.findByStatus(status);
    }

    /** Helper: chỉ lấy những bài đã xuất bản */
    public List<Post> findPublished() {
        return findByStatus("published");
    }

    /** Lấy bài viết theo ID (cho CommentController) */
    public Optional<Post> getPostById(Integer id) {
        return postRepo.findById(id);
    }

    /** Lấy tất cả bài của một user */
    public List<Post> findAllByUser(User user) {
        return postRepo.findAllByUser(user);
    }

    /**
     * Cập nhật bài viết.
     * Nếu upload ảnh chính mới thì ghi đè, 
     * thêm gallery nếu có file mới.
     */
    public Post updatePost(Integer id,
                           Post updatedPost,
                           MultipartFile mainImageFile,
                           MultipartFile[] imageFiles) throws IOException {
        Post post = postRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bài viết id=" + id));

        // Cập nhật nội dung
        post.setTitle(updatedPost.getTitle());
        post.setExcerpt(updatedPost.getExcerpt());
        post.setContent(updatedPost.getContent());
        post.setCategory(updatedPost.getCategory());
        post.setStatus(updatedPost.getStatus());

        // 1. Ảnh chính
        if (mainImageFile != null && !mainImageFile.isEmpty()) {
            String filename = saveFile(mainImageFile);
            post.setMainImage(filename);
        }

        // 2. Gallery mới
        if (imageFiles != null) {
            for (MultipartFile mf : imageFiles) {
                if (!mf.isEmpty()) {
                    String filename = saveFile(mf);
                    PostImage pi = new PostImage();
                    pi.setImageUrl(filename);
                    pi.setPost(post);
                    post.getImages().add(pi);
                }
            }
        }

        return postRepo.save(post);
    }

    /** Xóa bài viết */
    public void deletePost(Integer id) {
        postRepo.deleteById(id);
    }

    /**
     * Lưu file vào thư mục uploadDir, trả về filename.
     */
    private String saveFile(MultipartFile multipart) throws IOException {
        String original = StringUtils.cleanPath(multipart.getOriginalFilename());
        String ext = "";
        int dot = original.lastIndexOf('.');
        if (dot > 0) {
            ext = original.substring(dot);
        }
        String filename = UUID.randomUUID().toString() + ext;

        Path target = uploadDir.resolve(filename);
        try (InputStream is = multipart.getInputStream()) {
            Files.copy(is, target, StandardCopyOption.REPLACE_EXISTING);
        }
        return filename;
    }

    /** Tìm bài liên quan */
    public List<Post> findRelatedByCategory(Integer categoryId, Integer excludePostId) {
        return postRepo
            .findTop5ByCategory_CategoryIdAndPostIdNotOrderByCreatedAtDesc(categoryId, excludePostId);
    }

    public List<Post> findByCategory(Category category) {
        return postRepo.findByCategory(category);
    }

    /** Tìm bài theo prefix title */
    public List<Post> searchByPrefix(String keyword) {
        return postRepo.findByTitleStartingWithIgnoreCase(keyword);
    }
    
    public List<Post> findTop5ByCategoryAndNotId(Integer categoryId, Integer postId) {
        return postRepo.findTop5ByCategory_CategoryIdAndPostIdNotOrderByCreatedAtDesc(categoryId, postId);
    }
    
    public Page<Post> getLatestPosts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return postRepo.findAll(pageable);
    }
    
    public Page<Post> getUserPostsPaged(User user, int page, int size, String status, Integer categoryId, String sort) {
        Pageable pageable = switch (sort) {
            case "oldest" -> PageRequest.of(page, size, Sort.by("createdAt").ascending());
            case "most-viewed" -> PageRequest.of(page, size, Sort.by("viewCount").descending());
            case "most-commented" -> PageRequest.of(page, size, Sort.by("comments.size").descending()); // hoặc custom query
            default -> PageRequest.of(page, size, Sort.by("createdAt").descending());
        };

        if (status != null && categoryId != null) {
            return postRepo.findByUserAndStatusAndCategory_CategoryId(user, status, categoryId, pageable);
        } else if (status != null) {
            return postRepo.findByUserAndStatus(user, status, pageable);
        } else if (categoryId != null) {
            return postRepo.findByUserAndCategory_CategoryId(user, categoryId, pageable);
        } else {
            return postRepo.findByUser(user, pageable);
        }
    }
}
