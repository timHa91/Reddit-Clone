package com.tim.redditclone.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PostResponse {

    private Long postId;
    private String userName;
    private String postName;
    private String subredditName;
    private String url;
    private String description;
    private Integer voteCount;
    private Integer commentCount;
    private String duration;
}
