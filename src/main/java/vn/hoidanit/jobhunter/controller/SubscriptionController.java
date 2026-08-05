package vn.hoidanit.jobhunter.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.hoidanit.jobhunter.domain.Subscription;
import vn.hoidanit.jobhunter.domain.response.SubscriptionResponse;
import vn.hoidanit.jobhunter.service.SubscriptionService;
import vn.hoidanit.jobhunter.domain.request.SubscriptionRequest;

import java.util.List;

@RestController
@RequestMapping("/api/v1/subscriptions")
public class SubscriptionController {

    private final SubscriptionService service;

    public SubscriptionController(SubscriptionService service) {
        this.service = service;
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Subscription>> listByUser(@PathVariable Long userId) {
        return ResponseEntity.status(HttpStatus.OK).body(service.listByUser(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Subscription> getById(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(service.getById(id));
    }

    @PostMapping
    public ResponseEntity<SubscriptionResponse > create(@RequestBody SubscriptionRequest req) {

        Subscription s = service.create(req);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                SubscriptionResponse.builder()
                        .id(s.getId())
                        .userId(s.getUser().getId())
                        .planId(s.getPlan().getId())
                        .planCode(s.getPlan().getCode())
                        .status(s.getStatus())
                        .startAt(s.getStartAt())
                        .endAt(s.getEndAt())
                        .build()
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<Subscription> update(@PathVariable Long id, @RequestBody SubscriptionRequest req) {
        return ResponseEntity.status(HttpStatus.OK).body(service.update(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.status(HttpStatus.OK).body(null);
    }

    @GetMapping("/user/{userId}/active")
    public ResponseEntity<SubscriptionResponse> getActiveSubscription(@PathVariable Long userId) {
        Subscription active = service.findActiveSubscription(userId);

        // Không có subscription đang hoạt động
        if (active == null) {
            return ResponseEntity.ok(null);
        }

        SubscriptionResponse dto = SubscriptionResponse.builder()
                .id(active.getId())
                .userId(active.getUser().getId())
                .planId(active.getPlan().getId())
                .planCode(active.getPlan().getCode())
                .status(active.getStatus())
                .startAt(active.getStartAt())
                .endAt(active.getEndAt())
                .build();

        return ResponseEntity.ok(dto);
    }

}