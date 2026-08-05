package vn.hoidanit.jobhunter.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import vn.hoidanit.jobhunter.domain.Company;
import vn.hoidanit.jobhunter.domain.User;

import java.time.Instant;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    User findByEmail(String email);

    boolean existsByEmail(String email);

    User findByRefreshTokenAndEmail(String token, String email);

    List<User> findByCompany(Company company);

    /**
     * Tìm users có UserProfile với skills phù hợp với job mới
     *
     * Flow:
     * 1. INNER JOIN User -> UserProfile (u.profile)
     * 2. INNER JOIN UserProfile -> Skill qua bảng user_profile_skill (up.skills)
     * 3. WHERE s.id IN :skillIds - Match skill của UserProfile với skill của Job
     * 4. AND u.verified = true - Chỉ lấy users đã verify email
     * 5. SELECT DISTINCT - Tránh trùng lặp khi user có nhiều skills match
     *
     * @param skillIds Danh sách skill IDs của job mới được tạo
     * @return Danh sách users có ít nhất 1 skill trùng với job
     */
    @Query("SELECT DISTINCT u FROM User u " +
            "INNER JOIN u.profile up " +
            "INNER JOIN up.skills s " +
            "WHERE s.id IN :skillIds ")
    List<User> findUsersWithMatchingSkills(@Param("skillIds") List<Long> skillIds);

    long countByCreatedAtAfter(Instant createdAt);

    long countByCreatedAtBetween(Instant start, Instant end);
}
