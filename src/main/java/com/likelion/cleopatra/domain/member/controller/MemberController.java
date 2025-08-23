package com.likelion.cleopatra.domain.member.controller;

import com.likelion.cleopatra.domain.member.service.MemberService;
import com.likelion.cleopatra.global.exception.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Member", description = "멤버 API")
@RequiredArgsConstructor
@RequestMapping("/api/member")
@RestController
public class MemberController {

    private final MemberService memberService;
    @PostMapping("/{primary_key}")
    @Operation(
            summary = "멤버 생성",
            description = "경로 변수로 primary_key를 받아 멤버를 생성합니다.\n예) POST /api/member/{primary_key}"
    )
    ApiResponse<?> create(@PathVariable("primary_key") String primaryKey) {
        return ApiResponse.success(memberService.findOrCreate(primaryKey), "Member 생성 성공.");
    }

}
