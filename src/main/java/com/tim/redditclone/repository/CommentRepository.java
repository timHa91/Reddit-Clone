package com.tim.redditclone.repository;

import com.tim.redditclone.model.Comment;
import com.tim.redditclone.model.Post;
import com.tim.redditclone.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findAllCommentsByPost(Post post);
    List<Comment> findAllCommentsByUser(User user);
}
