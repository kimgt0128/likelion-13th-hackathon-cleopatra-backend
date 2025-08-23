package com.likelion.cleopatra.domain.member.service;

import com.likelion.cleopatra.domain.member.entity.Member;
import com.likelion.cleopatra.domain.member.repository.MemberRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class MemberService {

    private final MemberRepository memberRepository;

    @Transactional
    public void findOrCreate(String primaryKey) {
        memberRepository.findByPrimaryKey(primaryKey)
                .orElseGet(() -> memberRepository.save(Member.create(primaryKey)));
    }

}
