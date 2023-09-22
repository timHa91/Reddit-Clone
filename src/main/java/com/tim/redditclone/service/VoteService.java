package com.tim.redditclone.service;

import com.tim.redditclone.dto.VoteDto;
import com.tim.redditclone.exceptions.SpringRedditException;
import com.tim.redditclone.model.Post;
import com.tim.redditclone.model.User;
import com.tim.redditclone.model.Vote;
import com.tim.redditclone.repository.PostRepository;
import com.tim.redditclone.repository.VoteRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static com.tim.redditclone.model.VoteType.UPVOTE;

@Service
@AllArgsConstructor
@Slf4j
public class VoteService {

    private final VoteRepository voteRepository;
    private final PostRepository postRepository;
    private final AuthenticationService authenticationService;

    public void vote(VoteDto voteDto) {
        Post post = postRepository.findById(voteDto.getPostId())
                .orElseThrow(() -> new SpringRedditException("Post not found"));
        User user = authenticationService.getCurrentUser();
        Optional<Vote> voteByPostAndUser = voteRepository.findByPostAndUser(post, user);
        //Check if User has already Upvoted the Post
        if (voteByPostAndUser.isPresent() && voteByPostAndUser.get()
                .getVoteType().equals(voteDto.getVoteType())) {
            throw new SpringRedditException("You have already "
                    + voteDto.getVoteType() + "'d for this post");
        }
        if(UPVOTE.equals(voteDto.getVoteType())) {
            post.setVoteCount(post.getVoteCount() + 1);
        } else {
            post.setVoteCount(post.getVoteCount() -1);
        }

        postRepository.save(post);
        voteRepository.save(mapVoteDto(voteDto, user, post));
    }

    private Vote mapVoteDto(VoteDto voteDto, User user, Post post) {
        return Vote.builder()
                .voteType(voteDto.getVoteType())
                .post(post)
                .user(user)
                .build();
    }
}
