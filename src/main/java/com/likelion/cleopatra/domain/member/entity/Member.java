package com.likelion.cleopatra.domain.member.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor
@Builder
@Table(name = "member")
@Entity
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    // 프론트엔드에서 받은 UUID값
    @Column(name = "primary_key", nullable = false, length = 64, unique = true)
    private String primaryKey;

    @Column(name = "report_count")
    @Builder.Default
    private int reportCount = 0;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public static Member create(String primary) {
        return Member.builder()
                .primaryKey(primary)
                .build();
    }

}
