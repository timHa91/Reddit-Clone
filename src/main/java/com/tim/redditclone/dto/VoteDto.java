package com.tim.redditclone.dto;

import com.tim.redditclone.model.VoteType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@AllArgsConstructor
@Slf4j
@Builder
public class VoteDto {

    private VoteType voteType;
    private Long postId;
}
