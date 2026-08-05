package vn.hoidanit.jobhunter.domain.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatisticsResponse {
    private long totalJobs;
    private long activeJobs;
    private long totalCompanies;
    private long totalUsers;
    private long newCandidates;
    private long scheduleToday;
    private long messagesReceived;
    private long jobViews;
    private long jobApplied;
    private double jobViewsTrend;
    private double jobAppliedTrend;
}
