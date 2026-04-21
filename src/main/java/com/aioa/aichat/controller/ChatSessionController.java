package com.aioa.aichat.controller;

import com.aioa.aichat.dto.ChatSessionDTO;
import com.aioa.aichat.dto.ChatSessionResponseDTO;
import com.aioa.aichat.service.ChatSessionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * AI会话Controller
 */
@RestController
@RequestMapping("/chat/sessions")
@RequiredArgsConstructor
public class ChatSessionController {

    private final ChatSessionService sessionService;

    @PostMapping
    public ResponseEntity<ChatSessionResponseDTO> createSession(@Valid @RequestBody ChatSessionDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(sessionService.createSession(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ChatSessionResponseDTO> getSession(@PathVariable Long id) {
        return ResponseEntity.ok(sessionService.getSessionById(id));
    }

    @GetMapping("/sid/{sessionId}")
    public ResponseEntity<ChatSessionResponseDTO> getSessionBySid(@PathVariable String sessionId) {
        return ResponseEntity.ok(sessionService.getSessionBySessionId(sessionId));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ChatSessionResponseDTO>> getUserSessions(@PathVariable Long userId) {
        return ResponseEntity.ok(sessionService.getUserSessions(userId));
    }

    @PutMapping("/{id}/close")
    public ResponseEntity<Void> closeSession(@PathVariable Long id) {
        sessionService.closeSession(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSession(@PathVariable Long id) {
        sessionService.deleteSession(id);
        return ResponseEntity.noContent().build();
    }
}
