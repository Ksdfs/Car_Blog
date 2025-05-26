package com.thiCK.CarBlog.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "Post_Images")
public class PostImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "image_url", length = 500, nullable = false)
    private String imageUrl;

    @ManyToOne
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    public PostImage() {}

    public PostImage(String imageUrl, Post post) {
        this.imageUrl = imageUrl;
        this.post = post;
    }

    // getters & setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public Post getPost() { return post; }
    public void setPost(Post post) { this.post = post; }
}
