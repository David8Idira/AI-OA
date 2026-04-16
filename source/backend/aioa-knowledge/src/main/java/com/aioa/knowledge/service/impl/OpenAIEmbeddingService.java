package com.aioa.knowledge.service.impl;

import cn.hutool.core.util.StrUtil;
import com.aioa.knowledge.config.EmbeddingConfig;
// import com.theokanning.openai.OpenAiService;
import com.theokanning.openai.embedding.Embedding;
import com.theokanning.openai.embedding.EmbeddingRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * OpenAI Embedding 服务实现
 */
@Slf4j
@Service
public class OpenAIEmbeddingService implements EmbeddingService {
    
    @Autowired
    private EmbeddingConfig embeddingConfig;
    
    // private OpenAiService openAiService;
    
    /**
     * 初始化OpenAI服务
     */
    private synchronized void initService() {
        if (openAiService == null && StrUtil.isNotBlank(embeddingConfig.getOpenai().getApiKey())) {
            try {
                openAiService = new OpenAiService(
                    embeddingConfig.getOpenai().getApiKey(),
                    embeddingConfig.getOpenai().getBaseUrl(),
                    embeddingConfig.getTimeoutSeconds()
                );
                log.info("OpenAI Embedding服务初始化成功");
            } catch (Exception e) {
                log.error("OpenAI Embedding服务初始化失败", e);
            }
        }
    }
    
    /**
     * 生成单个文本的向量
     */
    public List<Float> generateEmbedding(String text) {
        if (StrUtil.isBlank(text)) {
            return new ArrayList<>();
        }
        
        initService();
        if (openAiService == null) {
            log.warn("OpenAI服务未初始化，无法生成向量");
            return new ArrayList<>();
        }
        
        try {
            EmbeddingRequest request = EmbeddingRequest.builder()
                .model(embeddingConfig.getOpenai().getModel())
                .input(List.of(text))
                .build();
            
            List<Embedding> embeddings = openAiService.createEmbeddings(request).getData();
            if (embeddings != null && !embeddings.isEmpty()) {
                return embeddings.get(0).getEmbedding();
            }
        } catch (Exception e) {
            log.error("OpenAI向量生成失败: {}", e.getMessage(), e);
        }
        
        return new ArrayList<>();
    }
    
    /**
     * 批量生成向量
     */
    public List<List<Float>> batchGenerateEmbedding(List<String> texts) {
        if (texts == null || texts.isEmpty()) {
            return new ArrayList<>();
        }
        
        initService();
        if (openAiService == null) {
            log.warn("OpenAI服务未初始化，无法批量生成向量");
            return new ArrayList<>();
        }
        
        try {
            EmbeddingRequest request = EmbeddingRequest.builder()
                .model(embeddingConfig.getOpenai().getModel())
                .input(texts)
                .build();
            
            List<Embedding> embeddings = openAiService.createEmbeddings(request).getData();
            List<List<Float>> results = new ArrayList<>();
            
            for (Embedding embedding : embeddings) {
                results.add(embedding.getEmbedding());
            }
            
            log.info("批量生成向量完成，数量: {}", results.size());
            return results;
        } catch (Exception e) {
            log.error("OpenAI批量向量生成失败: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }
    
    /**
     * 检查服务是否可用
     */
    public boolean isAvailable() {
        if (StrUtil.isBlank(embeddingConfig.getOpenai().getApiKey())) {
            return false;
        }
        
        initService();
        return openAiService != null;
    }
    
    /**
     * 获取配置信息
     */
    public String getConfigInfo() {
        EmbeddingConfig.OpenaiConfig config = embeddingConfig.getOpenai();
        return String.format("OpenAI Embedding - Model: %s, BaseURL: %s, MaxTokens: %d",
            config.getModel(), config.getBaseUrl(), config.getMaxTokens());
    }
}