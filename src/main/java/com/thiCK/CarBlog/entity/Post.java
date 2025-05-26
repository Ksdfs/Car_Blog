package com.thiCK.CarBlog.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Posts")
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Integer postId;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String excerpt;

    @Column(nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String content;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(length = 20, nullable = false)
    private String status;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    // Đường dẫn ảnh đại diện bài viết, VD: /uploads/post/abc.jpg
    @Column(name = "main_image", length = 500)
    private String mainImage;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostImage> images = new ArrayList<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    @Column(name="view_count", nullable=false)
    private int viewCount = 0;

    @Column(name="like_count", nullable=false)
    private int likeCount = 0;

    // -------- Các trường upload tạm thời, không lưu DB --------
    /** Ảnh đại diện khi upload form */
    @Transient
    private MultipartFile mainImageFile;

    /** Danh sách file ảnh phụ khi upload form */
    @Transient
    private List<MultipartFile> imageFiles = new ArrayList<>();

    /** Danh sách URL ảnh phụ để hiển thị */
    @Transient
    private List<String> imageUrls = new ArrayList<>();

    public Post() {}

    // ==== Getter & Setter ====

    public Integer getPostId() {
        return postId;
    }
    public void setPostId(Integer postId) {
        this.postId = postId;
    }

    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    public String getExcerpt() {
        return excerpt;
    }
    public void setExcerpt(String excerpt) {
        this.excerpt = excerpt;
    }

    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }

    public User getUser() {
        return user;
    }
    public void setUser(User user) {
        this.user = user;
    }

    public Category getCategory() {
        return category;
    }
    public void setCategory(Category category) {
        this.category = category;
    }

    public String getMainImage() {
        return mainImage;
    }
    public void setMainImage(String mainImage) {
        this.mainImage = mainImage;
    }

    public List<PostImage> getImages() {
        return images;
    }
    public void setImages(List<PostImage> images) {
        this.images = images;
    }

    public List<Comment> getComments() {
        return comments;
    }
    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    public int getViewCount() {
        return viewCount;
    }
    public void setViewCount(int viewCount) {
        this.viewCount = viewCount;
    }

    public int getLikeCount() {
        return likeCount;
    }
    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }

    public MultipartFile getMainImageFile() {
        return mainImageFile;
    }
    public void setMainImageFile(MultipartFile mainImageFile) {
        this.mainImageFile = mainImageFile;
    }

    public List<MultipartFile> getImageFiles() {
        return imageFiles;
    }
    public void setImageFiles(List<MultipartFile> imageFiles) {
        this.imageFiles = imageFiles;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }
    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }
}
