package vn.hoidanit.jobhunter.service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.hoidanit.jobhunter.domain.ChatRealtimeMessage;
import vn.hoidanit.jobhunter.domain.response.DashboardStatisticsResponse;
import vn.hoidanit.jobhunter.repository.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardService {

    private final JobRepository jobRepository;
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final ResumeRepository resumeRepository;
    private final ChatRealtimeMessageRepository chatRealtimeMessageRepository;
    private final ChatMessageRepository chatMessageRepository;

    public DashboardStatisticsResponse getStatistics() {
        // Tính toán các metrics cơ bản
        long totalJobs = jobRepository.count();
        long activeJobs = jobRepository.countByActive(true);
        long totalCompanies = companyRepository.count();
        long totalUsers = userRepository.count();

        // Tính số ứng viên mới trong 7 ngày gần đây
        Instant sevenDaysAgo = Instant.now().minus(7, ChronoUnit.DAYS);
        long newCandidates = userRepository.countByCreatedAtAfter(sevenDaysAgo);

        // Tính số lượng resumes được nộp hôm nay (giả sử đây là "schedule today")
        LocalDate today = LocalDate.now();
        Instant startOfDay = today.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant endOfDay = today.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
        long scheduleToday = resumeRepository.countByCreatedAtBetween(startOfDay, endOfDay);

        // Tính số tin nhắn nhận được hôm nay
        long messagesReceived = chatRealtimeMessageRepository.countByCreatedDateBetween(startOfDay, endOfDay);

        // Tính tổng số lượt xem công việc (giả sử dựa vào tổng số jobs * quantity)
        // Trong thực tế, bạn nên có bảng job_views riêng để track
        long jobViews = chatMessageRepository.countByCreatedAtBetween(startOfDay, endOfDay);
        long jobApplied = resumeRepository.count();

        // Tính trend - so sánh với tuần trước (giả định)
        Instant fourteenDaysAgo = Instant.now().minus(14, ChronoUnit.DAYS);
        long previousWeekCandidates = userRepository.countByCreatedAtBetween(fourteenDaysAgo, sevenDaysAgo);

        double jobViewsTrend = 6.4; // Giá trị mặc định - nên tính toán thực tế
        double jobAppliedTrend = calculateTrend(jobApplied, previousWeekCandidates);

        return DashboardStatisticsResponse.builder()
                .totalJobs(totalJobs)
                .activeJobs(activeJobs)
                .totalCompanies(totalCompanies)
                .totalUsers(totalUsers)
                .newCandidates(newCandidates)
                .scheduleToday(scheduleToday)
                .messagesReceived(messagesReceived)
                .jobViews(jobViews)
                .jobApplied(jobApplied)
                .jobViewsTrend(jobViewsTrend)
                .jobAppliedTrend(jobAppliedTrend)
                .build();
    }

    private double calculateTrend(long current, long previous) {
        if (previous == 0) {
            return current > 0 ? 100.0 : 0.0;
        }
        return Math.round(((double) (current - previous) / previous) * 1000.0) / 10.0;
    }
}
