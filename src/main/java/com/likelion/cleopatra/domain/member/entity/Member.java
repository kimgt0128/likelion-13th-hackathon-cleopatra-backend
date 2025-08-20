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

    // 쿠키의 기기식별값(예: UUID v4 하이픈 제거 32자, 여유 있게 64자로 선언)
    @Column(name = "device_key", nullable = false, length = 64, unique = true)
    private String deviceKey;

    @Column(name = "name")
    private String username;

    @Column(name = "report_count")
    @Builder.Default
    private int reportCount = 0;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public static Member to(String deviceKey, String username) {
        return Member.builder()
                .deviceKey(deviceKey)
                .username(username)
                .build();
    }

}
