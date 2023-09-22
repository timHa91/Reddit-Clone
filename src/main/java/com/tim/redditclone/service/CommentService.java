package com.tim.redditclone.service;

import com.tim.redditclone.dto.CommentDto;
import com.tim.redditclone.exceptions.SpringRedditException;
import com.tim.redditclone.model.Comment;
import com.tim.redditclone.model.NotificationEmail;
import com.tim.redditclone.model.Post;
import com.tim.redditclone.model.User;
import com.tim.redditclone.repository.CommentRepository;
import com.tim.redditclone.repository.PostRepository;
import com.tim.redditclone.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
@Service
@AllArgsConstructor
@Slf4j
public class CommentService {

    private static final String POST_URL = "";
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final AuthenticationService authenticationService;
    private final UserRepository userRepository;
    private final MailService mailService;
    private final MailContentBuilder mailContentBuilder;

    public CommentDto createComment(CommentDto commentRequest) {
        User user = authenticationService.getCurrentUser();
        Post post = postRepository.findById(commentRequest.getPostId())
                .orElseThrow(() -> new SpringRedditException("Post not found"));
        Comment comment = commentRepository.save(mapCommentDto(commentRequest,post, user));

        String message = mailContentBuilder.build(post.getUser().getUsername() + " posted a comment on your post." + POST_URL);
        sendCommentNotification(message, post.getUser());
        return mapToCommentDto(comment);
    }

    public List<CommentDto> getAllCommentsByPost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new SpringRedditException("Post not found"));
        List<Comment> comments = commentRepository.findAllCommentsByPost(post);
        return comments.stream()
                .map(this::mapToCommentDto)
                .toList();
    }

    public List<CommentDto> getAllCommentsByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        List<Comment> comments = commentRepository.findAllCommentsByUser(user);
        return comments.stream()
                .map(this::mapToCommentDto)
                .toList();
    }

    private Comment mapCommentDto(CommentDto commentDto, Post post, User user) {
        return Comment.builder()
                .createdDate(Instant.now()) // Timestamp Backend oder Frontend?
                .post(post)
                .text(commentDto.getText())
                .user(user)
                .build();
    }

    private CommentDto mapToCommentDto(Comment comment) {
        return CommentDto.builder()
                .id(comment.getId())
                .username(comment.getUser().getUsername())
                .text(comment.getText())
                .postId(comment.getPost().getPostId())
                .build();
    }

    private void sendCommentNotification(String message, User user) {
        mailService.sendMail(new NotificationEmail(user.getUsername() + " Commented on your post", user.getEmail(), message));
    }
}
