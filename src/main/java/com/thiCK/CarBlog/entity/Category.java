package com.thiCK.CarBlog.entity;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "Categories")
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer categoryId;

    @Column(nullable = false, length = 100)
    private String name;

    // Bỏ cascade SET_NULL, vì JPA không hỗ trợ
    @OneToMany(mappedBy = "category")
    private List<Post> posts;

    public Category() {}

    // getters / setters
    public Integer getCategoryId() { return categoryId; }
    public void setCategoryId(Integer categoryId) { this.categoryId = categoryId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public List<Post> getPosts() { return posts; }
    public void setPosts(List<Post> posts) { this.posts = posts; }
}
