package com.aioa.knowledge.service;

import com.aioa.knowledge.config.EmbeddingConfig;
import com.aioa.knowledge.service.impl.LocalEmbeddingService;
import com.aioa.knowledge.service.impl.OpenAIEmbeddingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 向量化服务工厂
 * 根据配置选择合适的向量化服务
 */
@Slf4j
@Component
public class EmbeddingServiceFactory {
    
    @Autowired
    private EmbeddingConfig embeddingConfig;
    
    @Autowired
    private OpenAIEmbeddingService openAIEmbeddingService;
    
    @Autowired
    private LocalEmbeddingService localEmbeddingService;
    
    /**
     * 获取向量化服务
     */
    public EmbeddingService getEmbeddingService() {
        String mode = embeddingConfig.getMode();
        
        switch (mode.toLowerCase()) {
            case "openai":
                if (openAIEmbeddingService.isAvailable()) {
                    log.info("使用OpenAI向量化服务");
                    return new EmbeddingService() {
                        @Override
                        public List<Float> generateEmbedding(String text) {
                            return openAIEmbeddingService.generateEmbedding(text);
                        }
                        
                        @Override
                        public List<List<Float>> batchGenerateEmbedding(List<String> texts) {
                            return openAIEmbeddingService.batchGenerateEmbedding(texts);
                        }
                        
                        @Override
                        public boolean isAvailable() {
                            return openAIEmbeddingService.isAvailable();
                        }
                        
                        @Override
                        public String getConfigInfo() {
                            return openAIEmbeddingService.getConfigInfo();
                        }
                    };
                }
                log.warn("OpenAI服务不可用，尝试使用本地服务");
                break;
                
            case "local":
                if (localEmbeddingService.isAvailable()) {
                    log.info("使用本地向量化服务");
                    return new EmbeddingService() {
                        @Override
                        public List<Float> generateEmbedding(String text) {
                            return localEmbeddingService.generateEmbedding(text);
                        }
                        
                        @Override
                        public List<List<Float>> batchGenerateEmbedding(List<String> texts) {
                            return localEmbeddingService.batchGenerateEmbedding(texts);
                        }
                        
                        @Override
                        public boolean isAvailable() {
                            return localEmbeddingService.isAvailable();
                        }
                        
                        @Override
                        public String getConfigInfo() {
                            return localEmbeddingService.getConfigInfo();
                        }
                    };
                }
                log.warn("本地服务不可用");
                break;
                
            default:
                log.warn("未知的向量化模式: {}", mode);
        }
        
        // 默认返回一个空实现
        log.info("使用空向量化服务（演示模式）");
        return new EmbeddingService() {
            @Override
            public List<Float> generateEmbedding(String text) {
                log.warn("向量化服务不可用，返回空向量");
                return new ArrayList<>();
            }
            
            @Override
            public List<List<Float>> batchGenerateEmbedding(List<String> texts) {
                log.warn("向量化服务不可用，返回空列表");
                return new ArrayList<>();
            }
            
            @Override
            public boolean isAvailable() {
                return false;
            }
            
            @Override
            public String getConfigInfo() {
                return "空向量化服务（演示模式）";
            }
        };
    }
    
    /**
     * 向量化服务接口
     */
    public interface EmbeddingService {
        List<Float> generateEmbedding(String text);
        List<List<Float>> batchGenerateEmbedding(List<String> texts);
        boolean isAvailable();
        String getConfigInfo();
    }
}