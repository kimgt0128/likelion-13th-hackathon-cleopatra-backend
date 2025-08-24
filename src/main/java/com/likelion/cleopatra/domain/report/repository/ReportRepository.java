package com.likelion.cleopatra.domain.report.repository;

import com.likelion.cleopatra.domain.member.entity.Member;
import com.likelion.cleopatra.domain.report.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Long> {

    List<Report> findAllByMemberOrderByCreatedAtDesc(Member member);
}
