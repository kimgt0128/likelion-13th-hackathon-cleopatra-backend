package com.likelion.cleopatra.domain.member.repository;

import com.likelion.cleopatra.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {

}
