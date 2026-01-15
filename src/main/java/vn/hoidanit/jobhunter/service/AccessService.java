package vn.hoidanit.jobhunter.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hoidanit.jobhunter.domain.*;
import vn.hoidanit.jobhunter.repository.*;
import vn.hoidanit.jobhunter.util.SecurityUtil;
import vn.hoidanit.jobhunter.util.constant.AccessAction;
import vn.hoidanit.jobhunter.domain.response.access.AccessResponse;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class AccessService {

    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final PlanEntitlementRepository planEntitlementRepository;
    private final SubscriptionUsageRepository usageRepository;

    public AccessService(UserRepository userRepository,
                         SubscriptionRepository subscriptionRepository,
                         PlanEntitlementRepository planEntitlementRepository,
                         SubscriptionUsageRepository usageRepository) {
        this.userRepository = userRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.planEntitlementRepository = planEntitlementRepository;
        this.usageRepository = usageRepository;
    }

    @Transactional(readOnly = true)
    public AccessResponse check(AccessAction action) {
        User user = getCurrentUser();

        // 1) Role rule tối thiểu (bạn có thể chỉnh theo tên role DB)
        if (!isAllowedByRole(user, action)) {
            return AccessResponse.builder()
                    .status("NOT_ALLOWED")
                    .redirect(null)
                    .build();
        }

        // 2) Subscription
        Subscription sub = subscriptionRepository.findActiveByUserId(user.getId()).orElse(null);
        if (sub == null) {
            return AccessResponse.builder()
                    .status("NO_SUBSCRIPTION")
                    .redirect(rolePricingRedirect(user, "NO_SUBSCRIPTION"))
                    .build();
        }

        // 3) Entitlement của plan cho action
        PlanEntitlement ent = planEntitlementRepository
                .findByPlanAndAction(sub.getPlan().getId(), action)
                .orElse(null);

        if (ent == null) {
            return AccessResponse.builder()
                    .status("NOT_ALLOWED")
                    .planCode(sub.getPlan().getCode())
                    .build();
        }

        if (ent.getQuotaPerPeriod() == -1) {
            return AccessResponse.builder()
                    .status("OK")
                    .planCode(sub.getPlan().getCode())
                    .remaining(-1)
                    .build();
        }

        // Tính remaining theo kỳ hiện tại (không trừ)
        LocalDateTime now = LocalDateTime.now();
        Period p = ensurePeriod(sub, now); // chỉ tính, không save ở check(readOnly) -> OK nếu bạn muốn save thì bỏ readOnly
        SubscriptionUsage usage = usageRepository
                .findBySubAndActionAndPeriodStart(sub.getId(), action, p.start)
                .orElse(null);

        int used = usage == null ? 0 : usage.getUsedValue();
        int remaining = Math.max(ent.getQuotaPerPeriod() - used, 0);

        return AccessResponse.builder()
                .status(remaining > 0 ? "OK" : "QUOTA_EXCEEDED")
                .planCode(sub.getPlan().getCode())
                .remaining(remaining)
                .redirect(remaining > 0 ? null : rolePricingRedirect(user, "QUOTA_EXCEEDED"))
                .build();
    }

    //Consume quota (gọi trong nghiệp vụ create/apply)
    @Transactional
    public void consumeOrThrow(AccessAction action) {
        User user = getCurrentUser();

        if (!isAllowedByRole(user, action)) {
            throw new RuntimeException("NOT_ALLOWED");
        }

        Subscription sub = subscriptionRepository.findActiveByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("NO_SUBSCRIPTION"));

        PlanEntitlement ent = planEntitlementRepository
                .findByPlanAndAction(sub.getPlan().getId(), action)
                .orElseThrow(() -> new RuntimeException("NOT_ALLOWED"));

        if (ent.getQuotaPerPeriod() == -1) {
            return; // unlimited
        }

        LocalDateTime now = LocalDateTime.now();
        Period p = ensureAndPersistPeriod(sub, now); // đảm bảo kỳ, có save

        SubscriptionUsage usage = usageRepository
                .findBySubAndActionAndPeriodStart(sub.getId(), action, p.start)
                .orElseGet(() -> SubscriptionUsage.builder()
                        .subscription(sub)
                        .action(action)
                        .periodStart(p.start)
                        .periodEnd(p.end)
                        .usedValue(0)
                        .build());

        int next = usage.getUsedValue() + ent.getConsumeUnit();
        if (next > ent.getQuotaPerPeriod()) {
            throw new RuntimeException("QUOTA_EXCEEDED");
        }

        usage.setUsedValue(next);
        usageRepository.save(usage);
    }

    //  Helpers
    private User getCurrentUser() {
        String email = SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new RuntimeException("UNAUTHORIZED"));
        return userRepository.findByEmail(email);
//                .orElseThrow(() -> new RuntimeException("USER_NOT_FOUND"));
    }

    private boolean isAllowedByRole(User user, AccessAction action) {
        String role = user.getRole() == null
                ? ""
                : user.getRole().getName().toUpperCase();

        // ADMIN làm được tất cả
        if (role.contains("ADMIN")) {
            return true;
        }

        // HR & HR_VIP
        boolean isHR = role.contains("HR");

        // USER & USER_VIP
        boolean isUSER = role.contains("USER");

        return switch (action) {
            case CREATE_JOB -> isHR;
            case APPLY_JOB, CHAT_AI -> isUSER;
        };
    }


    private String rolePricingRedirect(User user, String status) {
        String roleName = user.getRole() != null ? user.getRole().getName() : "";
        roleName = roleName.trim().toUpperCase();

        // Chỉ redirect pricing khi QUOTA_EXCEEDED hoặc NO_SUBSCRIPTION
        if (!"QUOTA_EXCEEDED".equals(status) && !"NO_SUBSCRIPTION".equals(status)) {
            return null;
        }

        // USER*, USER_VIP
        if (roleName.startsWith("USER")) {
            return "/pricing";
        }

        // HR*, HR_VIP, ADMIN
        if (roleName.startsWith("HR") || roleName.startsWith("ADMIN")) {
            return "/admin/pricing";
        }

        // fallback
        return "/pricing";
    }


    // Kỳ tháng kiểu "rolling" (theo thời điểm bắt đầu hiện tại)
    private Period ensurePeriod(Subscription sub, LocalDateTime now) {
        LocalDateTime start = sub.getCurrentPeriodStart();
        LocalDateTime end = sub.getCurrentPeriodEnd();

        if (start == null || end == null || now.isAfter(end)) {
            start = now;
            end = now.plusMonths(1);
        }
        return new Period(start, end);
    }

    private Period ensureAndPersistPeriod(Subscription sub, LocalDateTime now) {
        Period p = ensurePeriod(sub, now);
        // nếu period thay đổi thì persist vào subscription
        if (sub.getCurrentPeriodStart() == null || sub.getCurrentPeriodEnd() == null || now.isAfter(sub.getCurrentPeriodEnd())) {
            sub.setCurrentPeriodStart(p.start);
            sub.setCurrentPeriodEnd(p.end);
            // save subscription (Jpa dirty checking cũng được nếu sub managed)
        }
        return p;
    }

    private record Period(LocalDateTime start, LocalDateTime end) {}
}
