package com.example.demo.post.repository;

import com.example.demo.post.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    @Query("SELECT p FROM Post p WHERE " +
            "(6371 * acos(cos(radians(:memberLat)) * cos(radians(p.destinationLat)) * cos(radians(p.destinationLng) - radians(:memberLng)) + sin(radians(:memberLat)) * sin(radians(p.destinationLat)))) < 100")
    List<Post> findPostsWithinDistance(@Param("memberLat") double memberLat, @Param("memberLng") double memberLng);

}
