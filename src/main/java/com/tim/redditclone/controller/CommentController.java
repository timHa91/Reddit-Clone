package com.tim.redditclone.controller;

import com.tim.redditclone.dto.CommentDto;
import com.tim.redditclone.service.CommentService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@Slf4j
@RequestMapping("/api/comment")
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<CommentDto> createComment(@RequestBody CommentDto commentRequest) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(commentService.createComment(commentRequest));
    }

    // "?postId=1
    @GetMapping("/by-post/{id}")
    public ResponseEntity<List<CommentDto>> getAllCommentsByPost(@PathVariable Long postId) {
        System.out.println("!!!!!!!!!!!!!!!!! post id: " + postId);
        return ResponseEntity.status(HttpStatus.OK)
                .body(commentService.getAllCommentsByPost(postId));
    }

    @GetMapping("/by-post/{username}")
    public ResponseEntity<List<CommentDto>> getAllCommentsByUsername(@PathVariable String username) {
        System.out.println("USERNAME: " + username);
        return ResponseEntity.status(HttpStatus.OK)
                .body(commentService.getAllCommentsByUsername(username));
    }
}
