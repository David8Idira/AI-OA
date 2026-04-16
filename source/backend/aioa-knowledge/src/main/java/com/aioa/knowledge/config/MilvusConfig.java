package com.aioa.knowledge.config;

import io.milvus.client.MilvusServiceClient;
import io.milvus.param.ConnectParam;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Milvus 向量数据库配置
 */
@Configuration
@ConfigurationProperties(prefix = "milvus")
@Data
public class MilvusConfig {
    
    private String host = "localhost";
    private int port = 19530;
    private String collectionName = "aioa_knowledge";
    private int vectorDim = 1536; // OpenAI ada-002 embedding dimension
    
    /**
     * 创建Milvus客户端
     */
    @Bean
    public MilvusServiceClient milvusServiceClient() {
        ConnectParam connectParam = ConnectParam.newBuilder()
                .withHost(host)
                .withPort(port)
                .build();
        
        return new MilvusServiceClient(connectParam);
    }
}