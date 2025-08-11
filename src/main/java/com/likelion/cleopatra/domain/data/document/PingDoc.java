package com.likelion.cleopatra.domain.data.document;

import jakarta.persistence.Id;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "pings")
public class PingDoc {

    @Id
    private String id;

    private String message;

    private Instant at;
}
