package com.tim.redditclone.repository;

import com.tim.redditclone.model.Post;
import com.tim.redditclone.model.Subreddit;
import com.tim.redditclone.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findByUser(User user);
    List<Post> findAllBySubreddit(Subreddit subreddit);
}
