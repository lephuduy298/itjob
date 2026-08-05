package vn.hoidanit.jobhunter.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.hoidanit.jobhunter.domain.PaymentTransaction;
import vn.hoidanit.jobhunter.util.constant.PaymentStatus;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {

    @Modifying
    @Query("""
                update PaymentTransaction p
                set p.status = :expired
                where p.status = :pending
                  and p.createdAt < :expiredTime
            """)
    int expirePendingTransactions(
            PaymentStatus pending,
            PaymentStatus expired,
            Instant expiredTime);

    List<PaymentTransaction> findBySubscription_User_Id(Long userId);


    @Modifying
    @Query("""
    delete from PaymentTransaction t
    where t.subscription.user.id = :userId
      and t.id <> :keepTxId
    """)
    int deleteAllByUserIdExcept(@Param("userId") Long userId,
                                @Param("keepTxId") Long keepTxId);


    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from PaymentTransaction t where t.id = :id")
    Optional<PaymentTransaction> findByIdForUpdate(@Param("id") Long id);

    @Modifying
    @Query("""
        delete from PaymentTransaction t
        where t.subscription.user.id = :userId
          and t.id <> :keepTxId
          and t.status <> vn.hoidanit.jobhunter.util.constant.PaymentStatus.SUCCESS
    """)
    int deleteNonSuccessByUserIdExcept(@Param("userId") Long userId,
                                       @Param("keepTxId") Long keepTxId);

}
