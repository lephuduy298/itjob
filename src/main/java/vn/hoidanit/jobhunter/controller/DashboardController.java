package vn.hoidanit.jobhunter.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import vn.hoidanit.jobhunter.domain.response.DashboardStatisticsResponse;
import vn.hoidanit.jobhunter.service.DashboardService;
import vn.hoidanit.jobhunter.util.annotation.ApiMessage;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DashboardController {

    DashboardService dashboardService;

    @GetMapping("/admin/dashboard/statistics")
    @ApiMessage("Fetch dashboard statistics")
    public ResponseEntity<DashboardStatisticsResponse> getStatistics() {
        DashboardStatisticsResponse statistics = dashboardService.getStatistics();
        return ResponseEntity.status(HttpStatus.OK).body(statistics);
    }
}
