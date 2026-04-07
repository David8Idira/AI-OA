package com.aioa.im.service;

import com.aioa.im.dto.SendMessageDTO;
import com.aioa.im.entity.Message;
import com.aioa.im.vo.MessageVO;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;

/**
 * Message Service Interface
 */
public interface MessageService extends IService<Message> {

    /**
     * Get message list for a conversation
     * @param conversationId conversation ID
     * @param userId current user ID
     * @param beforeMsgId load messages before this message ID (for pagination)
     * @param page page number
     * @param size page size
     * @return message list
     */
    List<MessageVO> getMessageList(String conversationId, String userId, String beforeMsgId, Integer page, Integer size);

    /**
     * Send a message
     * @param userId sender user ID
     * @param dto send message DTO
     * @return message VO
     */
    MessageVO sendMessage(String userId, SendMessageDTO dto);

    /**
     * Recall a message
     * @param messageId message ID
     * @param userId current user ID
     * @return true if success
     */
    boolean recallMessage(String messageId, String userId);

    /**
     * Delete a message (soft delete for sender)
     * @param messageId message ID
     * @param userId current user ID
     * @return true if success
     */
    boolean deleteMessage(String messageId, String userId);

    /**
     * Get message by ID
     * @param messageId message ID
     * @return message VO
     */
    MessageVO getMessageById(String messageId);

    /**
     * Mark message as read
     * @param conversationId conversation ID
     * @param userId current user ID
     * @param messageId message ID
     * @return true if success
     */
    boolean markMessageRead(String conversationId, String userId, String messageId);

    /**
     * Get unread count for user
     * @param userId user ID
     * @return total unread count
     */
    long getTotalUnreadCount(String userId);

    /**
     * Get total unread count for a conversation
     * @param conversationId conversation ID
     * @param userId user ID
     * @return unread count
     */
    long getConversationUnreadCount(String conversationId, String userId);
}
