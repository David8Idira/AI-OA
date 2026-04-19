package com.aioa.knowledge.service.impl;

import cn.hutool.core.util.StrUtil;
import com.aioa.knowledge.config.EmbeddingConfig;
import com.aioa.knowledge.service.EmbeddingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 本地Embedding服务实现
 * 注：这是一个简化实现，实际需要集成Sentence Transformers或类似模型
 */
@Slf4j
@Service
public class LocalEmbeddingService implements EmbeddingService {
    
    @Autowired
    private EmbeddingConfig embeddingConfig;
    
    // 在实际实现中，这里会加载本地模型
    // private Model model;
    
    /**
     * 生成单个文本的向量
     */
    public List<Float> generateEmbedding(String text) {
        if (StrUtil.isBlank(text)) {
            return new ArrayList<>();
        }
        
        // 这里应该是调用本地模型生成向量
        // 简化实现：返回随机向量（仅用于演示）
        log.info("本地模型向量生成（演示模式）: {}", text.substring(0, Math.min(50, text.length())));
        
        return generateRandomVector(embeddingConfig.getDimension());
    }
    
    /**
     * 批量生成向量
     */
    public List<List<Float>> batchGenerateEmbedding(List<String> texts) {
        if (texts == null || texts.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<List<Float>> results = new ArrayList<>();
        for (String text : texts) {
            results.add(generateEmbedding(text));
        }
        
        log.info("本地模型批量向量生成完成，数量: {}", results.size());
        return results;
    }
    
    /**
     * 生成随机向量（演示用途）
     */
    private List<Float> generateRandomVector(int dimension) {
        List<Float> vector = new ArrayList<>();
        for (int i = 0; i < dimension; i++) {
            // 生成-1到1之间的随机数
            vector.add((float) (Math.random() * 2 - 1));
        }
        return normalizeVector(vector);
    }
    
    /**
     * 向量归一化
     */
    private List<Float> normalizeVector(List<Float> vector) {
        if (vector == null || vector.isEmpty()) {
            return vector;
        }
        
        // 计算向量的模长
        float sum = 0;
        for (Float value : vector) {
            sum += value * value;
        }
        float magnitude = (float) Math.sqrt(sum);
        
        if (magnitude == 0) {
            return vector;
        }
        
        // 归一化处理
        List<Float> normalized = new ArrayList<>();
        for (Float value : vector) {
            normalized.add(value / magnitude);
        }
        return normalized;
    }
    
    /**
     * 初始化本地模型
     */
    private synchronized void initModel() {
        // 实际实现中会加载本地模型
        // if (model == null && StrUtil.isNotBlank(embeddingConfig.getLocal().getModelPath())) {
        //     try {
        //         model = ModelLoader.load(embeddingConfig.getLocal().getModelPath());
        //         log.info("本地模型加载成功: {}", embeddingConfig.getLocal().getModelPath());
        //     } catch (Exception e) {
        //         log.error("本地模型加载失败", e);
        //     }
        // }
    }
    
    /**
     * 检查服务是否可用
     */
    public boolean isAvailable() {
        // 检查是否有本地模型配置
        return StrUtil.isNotBlank(embeddingConfig.getLocal().getModelPath());
    }
    
    /**
     * 获取配置信息
     */
    public String getConfigInfo() {
        EmbeddingConfig.LocalModelConfig config = embeddingConfig.getLocal();
        return String.format("本地Embedding - Model: %s, Type: %s, GPU: %s",
            config.getModelPath(), config.getModelType(), config.isGpuAccelerated());
    }
}