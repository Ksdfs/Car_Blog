package com.thiCK.CarBlog.repository;

import com.thiCK.CarBlog.entity.PostImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostImageRepository extends JpaRepository<PostImage, Integer> {
}
