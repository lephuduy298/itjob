package vn.hoidanit.jobhunter.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hoidanit.jobhunter.domain.Plan;
import vn.hoidanit.jobhunter.domain.Role;
import vn.hoidanit.jobhunter.domain.Subscription;
import vn.hoidanit.jobhunter.domain.User;
import vn.hoidanit.jobhunter.repository.PlanRepository;
import vn.hoidanit.jobhunter.repository.RoleRepository;
import vn.hoidanit.jobhunter.repository.SubscriptionRepository;
import vn.hoidanit.jobhunter.repository.UserRepository;
import vn.hoidanit.jobhunter.domain.request.SubscriptionRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class SubscriptionService {

    private final SubscriptionRepository subRepo;
    private final UserRepository userRepo;
    private final PlanRepository planRepo;
    private final RoleRepository roleRepo;

    public SubscriptionService(SubscriptionRepository subRepo, UserRepository userRepo, PlanRepository planRepo,
            RoleRepository roleRepo) {
        this.subRepo = subRepo;
        this.userRepo = userRepo;
        this.planRepo = planRepo;
        this.roleRepo = roleRepo;
    }

    public List<Subscription> listByUser(Long userId) {
        return subRepo.findAllByUserId(userId);
    }

    public Subscription getById(Long id) {
        return subRepo.findById(id).orElseThrow(() -> new RuntimeException("SUB_NOT_FOUND"));
    }

    @Transactional
    public Subscription create(SubscriptionRequest req) {
        if (req.getUserId() == null)
            throw new RuntimeException("USER_ID_REQUIRED");
        if (req.getPlanId() == null)
            throw new RuntimeException("PLAN_ID_REQUIRED");

        User u = userRepo.findById(req.getUserId()).orElseThrow(() -> new RuntimeException("USER_NOT_FOUND"));
        Plan p = planRepo.findById(req.getPlanId()).orElseThrow(() -> new RuntimeException("PLAN_NOT_FOUND"));

        Subscription s = Subscription.builder()
                .user(u)
                .plan(p)
                .status(req.getStatus() == null ? "PENDING_PAYMENT" : req.getStatus())
                .build();

        // Nếu bạn tạo luôn ACTIVE để test:
        if ("ACTIVE".equalsIgnoreCase(s.getStatus())) {
            LocalDateTime now = LocalDateTime.now();
            s.setStartAt(now);
            s.setEndAt(now.plusMonths(p.getDurationMonths()));
            s.setCurrentPeriodStart(now);
            s.setCurrentPeriodEnd(now.plusMonths(1));
        }

        return subRepo.save(s);
    }

    @Transactional
    public Subscription update(Long id, SubscriptionRequest req) {
        Subscription s = getById(id);

        if (req.getPlanId() != null) {
            Plan p = planRepo.findById(req.getPlanId()).orElseThrow(() -> new RuntimeException("PLAN_NOT_FOUND"));
            s.setPlan(p);
        }
        if (req.getStatus() != null) {
            s.setStatus(req.getStatus());
            if ("ACTIVE".equalsIgnoreCase(req.getStatus())) {
                Plan p = s.getPlan();
                LocalDateTime now = LocalDateTime.now();
                s.setStartAt(now);
                s.setEndAt(now.plusMonths(p.getDurationMonths()));
                s.setCurrentPeriodStart(now);
                s.setCurrentPeriodEnd(now.plusMonths(1));
            }
        }
        return subRepo.save(s);
    }

    @Transactional
    public void delete(Long id) {
        subRepo.deleteById(id);
    }

    @Transactional
    public Subscription activateSubscriptionAndInitMonthlyUsage(Long subscriptionId) {

        Subscription s = subRepo.findById(subscriptionId)
                .orElseThrow(() -> new RuntimeException("SUB_NOT_FOUND"));

        // Idempotent
        if ("ACTIVE".equalsIgnoreCase(s.getStatus())) {
            return s;
        }

        Long userId = s.getUser().getId();
        LocalDateTime now = LocalDateTime.now();
        Plan p = s.getPlan();

        // 1) Tìm subscription ACTIVE cũ (nếu có và khác subscription đang kích hoạt)
        Subscription oldActive = subRepo.findActiveByUserId(userId)
                .filter(active -> !active.getId().equals(s.getId()))
                .orElse(null);

        // 2) Activate subscription mới trước
        s.setStatus("ACTIVE");
        s.setStartAt(now);
        s.setEndAt(now.plusMonths(p.getDurationMonths()));
        s.setCurrentPeriodStart(now);
        s.setCurrentPeriodEnd(now.plusMonths(1));

        Subscription saved = subRepo.save(s);

        // 3) Đóng subscription cũ => EXPIRED (KHÔNG delete)
        if (oldActive != null) {
            oldActive.setStatus("EXPIRED");
            oldActive.setEndAt(now);

            // nếu muốn rõ ràng chu kỳ cũng kết thúc tại đây
            oldActive.setCurrentPeriodEnd(now);

            // (Optional) nếu bạn muốn giữ startAt cũ thì không đụng startAt
            subRepo.save(oldActive);
        }

        return saved;
    }

    @Transactional
    public Subscription createPendingSubscription(Long userId, Long planId) {
        if (userId == null)
            throw new RuntimeException("USER_ID_REQUIRED");
        if (planId == null)
            throw new RuntimeException("PLAN_ID_REQUIRED");

        User u = userRepo.findById(userId).orElseThrow(() -> new RuntimeException("USER_NOT_FOUND"));
        Plan p = planRepo.findById(planId).orElseThrow(() -> new RuntimeException("PLAN_NOT_FOUND"));

        // KHÔNG CHẶN active nữa — vì bạn sẽ replace khi webhook SUCCESS

        // Kiểm tra nếu đang có pending với CÙNG planId thì trả về, nếu khác planId thì
        // hủy và tạo mới
        List<Subscription> pending = subRepo.findByUserIdAndStatus(userId, "PENDING_PAYMENT");
        if (!pending.isEmpty()) {
            Subscription existingPending = pending.get(0);

            // Nếu cùng plan, trả về pending hiện có
            if (existingPending.getPlan().getId().equals(planId)) {
                return existingPending;
            }

            // Nếu khác plan, hủy pending cũ và tạo mới
            existingPending.setStatus("CANCELED");
            subRepo.save(existingPending);
        }

        Subscription s = Subscription.builder()
                .user(u)
                .plan(p)
                .status("PENDING_PAYMENT")
                .build();

        return subRepo.save(s);
    }

    public Subscription findActiveSubscription(Long userId) {
        return subRepo
                .findFirstByUser_IdAndStatusOrderByEndAtDesc(userId, "ACTIVE")
                .filter(s -> s.getEndAt() == null || s.getEndAt().isAfter(LocalDateTime.now()))
                .orElse(null);
    }

}
