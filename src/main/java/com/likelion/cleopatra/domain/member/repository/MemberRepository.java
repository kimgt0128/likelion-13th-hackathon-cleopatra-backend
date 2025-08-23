package com.likelion.cleopatra.domain.member.repository;

import com.likelion.cleopatra.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByPrimaryKey(String primaryKey);
}
