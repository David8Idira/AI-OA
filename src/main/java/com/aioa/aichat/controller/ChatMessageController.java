package com.aioa.aichat.controller;

import com.aioa.aichat.dto.ChatMessageDTO;
import com.aioa.aichat.dto.ChatMessageResponseDTO;
import com.aioa.aichat.service.ChatMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * AI聊天消息Controller
 */
@RestController
@RequestMapping("/chat/messages")
@RequiredArgsConstructor
public class ChatMessageController {

    private final ChatMessageService messageService;

    @PostMapping
    public ResponseEntity<ChatMessageResponseDTO> sendMessage(@RequestBody ChatMessageDTO dto) {
        return ResponseEntity.ok(messageService.sendMessage(dto));
    }

    @GetMapping("/session/{sessionEntityId}")
    public ResponseEntity<List<ChatMessageResponseDTO>> getSessionMessages(@PathVariable Long sessionEntityId) {
        return ResponseEntity.ok(messageService.getSessionMessages(sessionEntityId));
    }
}
