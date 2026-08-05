package vn.hoidanit.jobhunter.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.hoidanit.jobhunter.domain.ChatRealtimeMessage;

import java.time.Instant;
import java.util.List;

@Repository
public interface ChatRealtimeMessageRepository extends JpaRepository<ChatRealtimeMessage, Long> {
    List<ChatRealtimeMessage> findAllByConversationIdOrderByCreatedDateDesc(String conversationId);

    long countByCreatedDateBetween(Instant start, Instant end);
}
