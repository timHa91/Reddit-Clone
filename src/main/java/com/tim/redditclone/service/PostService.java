package com.tim.redditclone.service;

import com.github.marlonlom.utilities.timeago.TimeAgo;
import com.tim.redditclone.dto.PostRequest;
import com.tim.redditclone.dto.PostResponse;
import com.tim.redditclone.exceptions.SpringRedditException;
import com.tim.redditclone.model.Post;
import com.tim.redditclone.model.Subreddit;
import com.tim.redditclone.model.User;
import com.tim.redditclone.repository.CommentRepository;
import com.tim.redditclone.repository.PostRepository;
import com.tim.redditclone.repository.SubredditRepository;
import com.tim.redditclone.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

    @Service
    @AllArgsConstructor
    @Slf4j
public class PostService {

    private final PostRepository postRepository;
    private final SubredditRepository subredditRepository;
    private final AuthenticationService authenticationService;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;

    @Transactional
    public PostResponse save(PostRequest postRequest) {
        Post save = postRepository.save(mapPostRequest(postRequest));

        return mapToPostResponse(save);
    }

    @Transactional
    public List<PostResponse> getAll() {
        List <Post> posts = postRepository.findAll();

        return posts.stream()
                .map(this::mapToPostResponse)
                .toList();
    }

    @Transactional
    public PostResponse getPost(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new SpringRedditException("Post not found"));

        return mapToPostResponse(post);
    }

    @Transactional
    public List<PostResponse> getPostsByUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(username));

        return postRepository.findByUser(user).stream()
                .map(this::mapToPostResponse)
                .toList();
    }

    @Transactional
    public List<PostResponse> getPostsBySubreddit(Long id) {
        Subreddit subreddit = subredditRepository.findById(id)
                .orElseThrow(() -> new SpringRedditException("Subreddit not found"));
        List<Post> posts = postRepository.findAllBySubreddit(subreddit);

        return posts.stream()
                .map(this::mapToPostResponse)
                .toList();
    }

    private Post mapPostRequest(PostRequest postRequest) {
        Optional<Subreddit> subredditOptional = subredditRepository.findByName(postRequest.getSubredditName());
        Subreddit subreddit = subredditOptional.orElseThrow(() -> new SpringRedditException("Subreddit not found"));
        User user = authenticationService.getCurrentUser();
        return Post.builder()
                .postName(postRequest.getPostName())
                .description(postRequest.getDescription())
                .url(postRequest.getUrl())
                .subreddit(subreddit) // Correctly set the subreddit
                .createdDate(Instant.now())
                .user(user)
                .voteCount(0)
                .build();
    }


    private PostResponse mapToPostResponse(Post post) {
        Integer commentCountValue = commentRepository.findAllCommentsByPost(post).size();
        String durationValue = TimeAgo.using(post.getCreatedDate().toEpochMilli());
        return PostResponse.builder()
                .postId(post.getPostId())
                .description(post.getDescription())
                .subredditName(post.getSubreddit().getName())
                .userName(post.getUser().getUsername())
                .postName(post.getPostName())
                .url(post.getUrl())
                .voteCount(post.getVoteCount())
                .commentCount(commentCountValue)
                .duration(durationValue)
                .build();
    }
}
