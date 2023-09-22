package com.tim.redditclone.repository;

import com.tim.redditclone.model.Post;
import com.tim.redditclone.model.User;
import com.tim.redditclone.model.Vote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VoteRepository extends JpaRepository<Vote, Long> {
    Optional<Vote> findByPostAndUser(Post post, User user);
}
