package vn.hoidanit.jobhunter.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import vn.hoidanit.jobhunter.domain.Resume;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface ResumeRepository extends JpaRepository<Resume, Long>,
                JpaSpecificationExecutor<Resume> {

        Page<Resume> findByUserId(long id, Pageable pageable);

        boolean existsByUserIdAndJobId(Long userId, Long jobId);

        Page<Resume> findByJobId(Long jobId, Pageable pageable);

        long countByCreatedAtBetween(Instant start, Instant end);
}
