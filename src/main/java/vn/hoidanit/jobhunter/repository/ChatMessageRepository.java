package vn.hoidanit.jobhunter.repository;

import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import vn.hoidanit.jobhunter.domain.ChatMessage;
import vn.hoidanit.jobhunter.domain.ChatSession;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long>,
        JpaSpecificationExecutor<ChatMessage> {

    List<ChatMessage> findByChatSessionOrderByCreatedAtAsc(ChatSession chatSession);

    long countByCreatedAtBetween(Instant start, Instant end);
}
