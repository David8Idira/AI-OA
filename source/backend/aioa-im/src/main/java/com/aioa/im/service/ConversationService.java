package com.aioa.im.service;

import com.aioa.im.dto.ConversationCreateDTO;
import com.aioa.im.entity.Conversation;
import com.aioa.im.vo.ConversationVO;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;

/**
 * Conversation Service Interface
 */
public interface ConversationService extends IService<Conversation> {

    /**
     * Get conversation list for current user
     * @param userId current user ID
     * @param type filter by type (optional)
     * @param page page number
     * @param size page size
     * @return conversation list
     */
    List<ConversationVO> getConversationList(String userId, Integer type, Integer page, Integer size);

    /**
     * Get conversation by ID
     * @param conversationId conversation ID
     * @param userId current user ID
     * @return conversation VO
     */
    ConversationVO getConversationById(String conversationId, String userId);

    /**
     * Create new conversation
     * @param userId current user ID
     * @param dto create DTO
     * @return conversation VO
     */
    ConversationVO createConversation(String userId, ConversationCreateDTO dto);

    /**
     * Get or create private conversation with another user
     * @param userId current user ID
     * @param otherUserId other user ID
     * @return conversation VO
     */
    ConversationVO getOrCreatePrivateConversation(String userId, String otherUserId);

    /**
     * Mark conversation as read
     * @param conversationId conversation ID
     * @param userId current user ID
     * @param lastReadMsgId last read message ID
     * @return true if success
     */
    boolean markAsRead(String conversationId, String userId, String lastReadMsgId);

    /**
     * Delete conversation (leave)
     * @param conversationId conversation ID
     * @param userId current user ID
     * @return true if success
     */
    boolean deleteConversation(String conversationId, String userId);

    /**
     * Mute/unmute conversation
     * @param conversationId conversation ID
     * @param userId current user ID
     * @param muteStatus mute status
     * @return true if success
     */
    boolean muteConversation(String conversationId, String userId, Integer muteStatus);

    /**
     * Pin/unpin conversation
     * @param conversationId conversation ID
     * @param userId current user ID
     * @param topStatus top status
     * @return true if success
     */
    boolean topConversation(String conversationId, String userId, Integer topStatus);

    /**
     * Update conversation info
     * @param conversationId conversation ID
     * @param userId current user ID
     * @param name new name
     * @param avatar new avatar
     * @return true if success
     */
    boolean updateConversationInfo(String conversationId, String userId, String name, String avatar);
}
