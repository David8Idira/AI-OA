package com.aioa.im.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

/**
 * Create Conversation DTO
 */
@Data
public class ConversationCreateDTO {

    /**
     * Conversation type: 1-private, 2-group, 3-channel
     */
    @NotNull(message = "Conversation type is required")
    private Integer type;

    /**
     * Conversation name (required for group/channel)
     */
    private String name;

    /**
     * Avatar URL
     */
    private String avatar;

    /**
     * Member user IDs (for group/channel)
     */
    private List<String> memberIds;

    /**
     * Description
     */
    private String description;

    /**
     * Max members (-1 for unlimited)
     */
    private Integer maxMembers;
}
