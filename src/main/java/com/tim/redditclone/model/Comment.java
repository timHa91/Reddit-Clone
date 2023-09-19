package com.tim.redditclone.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotEmpty
    private String text;
    //
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "postId", referencedColumnName = "postId")
    private Post post;
    // Die @ManyToOne-Beziehung verknüpft diesen Comment mit einem bestimmten Post basierend auf der postId. Dadurch wird festgelegt, zu welchem Post dieser Comment gehört.
    private Instant createdDate;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", referencedColumnName = "userId")
    private User user;
    // Speichert nur die user_id im Comment, um die Beziehung zwischen einem Comment und dem dazugehörigen User herzustellen. Die User-Tabelle hat keine direkte Referenz auf Comment, da die Beziehung in deinem Fall nur in eine Richtung benötigt wird, um herauszufinden, welchem User ein bestimmter Comment gehört.

}
