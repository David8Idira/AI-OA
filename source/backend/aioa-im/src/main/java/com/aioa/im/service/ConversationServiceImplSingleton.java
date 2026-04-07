package com.aioa.im.service;

/**
 * Singleton placeholder for ConversationService - used to avoid circular dependency in ImWebSocketHandler.
 * Actual implementation is injected by Spring.
 */
public class ConversationServiceImplSingleton {
    public static ConversationService INSTANCE;
}
