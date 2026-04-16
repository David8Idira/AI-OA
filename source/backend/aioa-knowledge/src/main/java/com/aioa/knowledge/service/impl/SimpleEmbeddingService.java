package com.aioa.knowledge.service.impl;

import com.aioa.knowledge.service.EmbeddingService;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service("simpleEmbeddingService")
public class SimpleEmbeddingService implements EmbeddingService {
    
    @Override
    public List<Float> generateEmbedding(String text) {
        // 生成简单的模拟向量（1536维）
        List<Float> embedding = new ArrayList<>(1536);
        for (int i = 0; i < 1536; i++) {
            embedding.add((float) Math.random() - 0.5f);
        }
        return embedding;
    }
    
    @Override
    public List<List<Float>> batchGenerateEmbedding(List<String> texts) {
        List<List<Float>> embeddings = new ArrayList<>();
        for (String text : texts) {
            embeddings.add(generateEmbedding(text));
        }
        return embeddings;
    }
}
