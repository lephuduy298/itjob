package vn.hoidanit.jobhunter.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import vn.hoidanit.jobhunter.domain.Subscription;

import java.util.List;
import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    @Query("SELECT s FROM Subscription s WHERE s.user.id = :userId")
    List<Subscription> findAllByUserId(Long userId);

    @Query("SELECT s FROM Subscription s WHERE s.user.id = :userId AND s.status = 'ACTIVE'")
    Optional<Subscription> findActiveByUserId(Long userId);

    @Query("SELECT s FROM Subscription s WHERE s.user.id = :userId AND s.status = :status")
    List<Subscription> findByUserIdAndStatus(Long userId, String status);


    Optional<Subscription> findFirstByUser_IdAndStatusOrderByEndAtDesc(Long userId, String status);


}
