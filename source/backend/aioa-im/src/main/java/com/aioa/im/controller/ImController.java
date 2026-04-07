package com.aioa.im.controller;

import com.aioa.common.annotation.Login;
import com.aioa.common.result.Result;
import com.aioa.im.dto.ConversationCreateDTO;
import com.aioa.im.dto.ReadConfirmDTO;
import com.aioa.im.dto.SendMessageDTO;
import com.aioa.im.service.ConversationService;
import com.aioa.im.service.MessageService;
import com.aioa.im.vo.ConversationVO;
import com.aioa.im.vo.MessageVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * IM Controller - Enterprise Instant Messaging API
 */
@RestController
@RequestMapping("/api/v1/im")
@RequiredArgsConstructor
@Tag(name = "Instant Messaging", description = "Enterprise chat and messaging APIs")
public class ImController {

    private final ConversationService conversationService;
    private final MessageService messageService;

    // ==================== Conversation APIs ====================

    /**
     * GET /api/v1/im/conversations
     * Get conversation list for current user
     */
    @GetMapping("/conversations")
    @Login
    @Operation(summary = "Get conversation list", description = "Get paginated conversation list for current user")
    public Result<List<ConversationVO>> getConversationList(
            @RequestAttribute("userId") String userId,
            @RequestParam(required = false) Integer type,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        List<ConversationVO> list = conversationService.getConversationList(userId, type, page, size);
        return Result.success(list);
    }

    /**
     * GET /api/v1/im/conversations/{id}
     * Get conversation detail by ID
     */
    @GetMapping("/conversations/{id}")
    @Login
    @Operation(summary = "Get conversation detail")
    public Result<ConversationVO> getConversationById(
            @RequestAttribute("userId") String userId,
            @PathVariable("id") String conversationId) {
        ConversationVO vo = conversationService.getConversationById(conversationId, userId);
        return Result.success(vo);
    }

    /**
     * POST /api/v1/im/conversations
     * Create new conversation (group/channel)
     */
    @PostMapping("/conversations")
    @Login
    @Operation(summary = "Create conversation", description = "Create group or channel conversation")
    public Result<ConversationVO> createConversation(
            @RequestAttribute("userId") String userId,
            @Valid @RequestBody ConversationCreateDTO dto) {
        ConversationVO vo = conversationService.createConversation(userId, dto);
        return Result.success(vo);
    }

    /**
     * GET /api/v1/im/conversations/private
     * Get or create private conversation with another user
     */
    @GetMapping("/conversations/private")
    @Login
    @Operation(summary = "Get or create private conversation")
    public Result<ConversationVO> getOrCreatePrivateConversation(
            @RequestAttribute("userId") String userId,
            @RequestParam("userId") String otherUserId) {
        ConversationVO vo = conversationService.getOrCreatePrivateConversation(userId, otherUserId);
        return Result.success(vo);
    }

    /**
     * DELETE /api/v1/im/conversations/{id}
     * Leave/delete a conversation
     */
    @DeleteMapping("/conversations/{id}")
    @Login
    @Operation(summary = "Leave/delete conversation")
    public Result<Void> deleteConversation(
            @RequestAttribute("userId") String userId,
            @PathVariable("id") String conversationId) {
        boolean success = conversationService.deleteConversation(conversationId, userId);
        return success ? Result.success() : Result.error("Failed to delete conversation");
    }

    /**
     * PUT /api/v1/im/conversations/{id}/mute
     * Mute/unmute conversation
     */
    @PutMapping("/conversations/{id}/mute")
    @Login
    @Operation(summary = "Mute/unmute conversation")
    public Result<Void> muteConversation(
            @RequestAttribute("userId") String userId,
            @PathVariable("id") String conversationId,
            @RequestParam Integer muteStatus) {
        boolean success = conversationService.muteConversation(conversationId, userId, muteStatus);
        return success ? Result.success() : Result.error("Failed to update mute status");
    }

    /**
     * PUT /api/v1/im/conversations/{id}/top
     * Pin/unpin conversation
     */
    @PutMapping("/conversations/{id}/top")
    @Login
    @Operation(summary = "Pin/unpin conversation")
    public Result<Void> topConversation(
            @RequestAttribute("userId") String userId,
            @PathVariable("id") String conversationId,
            @RequestParam Integer topStatus) {
        boolean success = conversationService.topConversation(conversationId, userId, topStatus);
        return success ? Result.success() : Result.error("Failed to update top status");
    }

    /**
     * PUT /api/v1/im/conversations/{id}
     * Update conversation info (name, avatar)
     */
    @PutMapping("/conversations/{id}")
    @Login
    @Operation(summary = "Update conversation info")
    public Result<Void> updateConversationInfo(
            @RequestAttribute("userId") String userId,
            @PathVariable("id") String conversationId,
            @RequestBody Map<String, String> params) {
        String name = params.get("name");
        String avatar = params.get("avatar");
        boolean success = conversationService.updateConversationInfo(conversationId, userId, name, avatar);
        return success ? Result.success() : Result.error("Failed to update conversation");
    }

    // ==================== Message APIs ====================

    /**
     * GET /api/v1/im/conversations/{id}/messages
     * Get message list for a conversation
     */
    @GetMapping("/conversations/{id}/messages")
    @Login
    @Operation(summary = "Get message list", description = "Get paginated message list with optional beforeMsgId for infinite scroll")
    public Result<List<MessageVO>> getMessageList(
            @RequestAttribute("userId") String userId,
            @PathVariable("id") String conversationId,
            @RequestParam(required = false) String beforeMsgId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        List<MessageVO> list = messageService.getMessageList(conversationId, userId, beforeMsgId, page, size);
        return Result.success(list);
    }

    /**
     * POST /api/v1/im/messages
     * Send a message
     */
    @PostMapping("/messages")
    @Login
    @Operation(summary = "Send message", description = "Send text, image, file or other types of messages")
    public Result<MessageVO> sendMessage(
            @RequestAttribute("userId") String userId,
            @Valid @RequestBody SendMessageDTO dto) {
        MessageVO vo = messageService.sendMessage(userId, dto);
        return Result.success(vo);
    }

    /**
     * PUT /api/v1/im/messages/{id}/recall
     * Recall a message (within 2 minutes)
     */
    @PutMapping("/messages/{id}/recall")
    @Login
    @Operation(summary = "Recall message", description = "Recall a sent message (only within 2 minutes)")
    public Result<Void> recallMessage(
            @RequestAttribute("userId") String userId,
            @PathVariable("id") String messageId) {
        boolean success = messageService.recallMessage(messageId, userId);
        return success ? Result.success() : Result.error("Failed to recall message");
    }

    /**
     * DELETE /api/v1/im/messages/{id}
     * Delete a message (soft delete for sender)
     */
    @DeleteMapping("/messages/{id}")
    @Login
    @Operation(summary = "Delete message")
    public Result<Void> deleteMessage(
            @RequestAttribute("userId") String userId,
            @PathVariable("id") String messageId) {
        boolean success = messageService.deleteMessage(messageId, userId);
        return success ? Result.success() : Result.error("Failed to delete message");
    }

    /**
     * GET /api/v1/im/messages/{id}
     * Get message by ID
     */
    @GetMapping("/messages/{id}")
    @Login
    @Operation(summary = "Get message by ID")
    public Result<MessageVO> getMessageById(@PathVariable("id") String messageId) {
        MessageVO vo = messageService.getMessageById(messageId);
        return Result.success(vo);
    }

    // ==================== Read Status APIs ====================

    /**
     * POST /api/v1/im/conversations/{id}/read
     * Mark messages as read
     */
    @PostMapping("/conversations/{id}/read")
    @Login
    @Operation(summary = "Mark as read", description = "Mark all messages up to lastReadMsgId as read")
    public Result<Void> markAsRead(
            @RequestAttribute("userId") String userId,
            @PathVariable("id") String conversationId,
            @RequestBody ReadConfirmDTO dto) {
        String lastReadMsgId = dto.getLastReadMsgId();
        boolean success = conversationService.markAsRead(conversationId, userId, lastReadMsgId);
        return success ? Result.success() : Result.error("Failed to mark as read");
    }

    /**
     * GET /api/v1/im/unread/count
     * Get total unread message count
     */
    @GetMapping("/unread/count")
    @Login
    @Operation(summary = "Get total unread count")
    public Result<Long> getTotalUnreadCount(@RequestAttribute("userId") String userId) {
        long count = messageService.getTotalUnreadCount(userId);
        return Result.success(count);
    }

    /**
     * GET /api/v1/im/conversations/{id}/unread
     * Get unread count for a specific conversation
     */
    @GetMapping("/conversations/{id}/unread")
    @Login
    @Operation(summary = "Get unread count for conversation")
    public Result<Long> getConversationUnreadCount(
            @RequestAttribute("userId") String userId,
            @PathVariable("id") String conversationId) {
        long count = messageService.getConversationUnreadCount(conversationId, userId);
        return Result.success(count);
    }
}
