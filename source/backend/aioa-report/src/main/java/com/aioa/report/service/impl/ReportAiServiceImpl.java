package com.aioa.report.service.impl;

import com.aioa.report.service.ReportAiService;
import com.aioa.report.entity.ReportTemplate;
import com.aioa.report.entity.ReportData;
import com.aioa.report.vo.ReportContentVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 智能报表AI生成服务实现类
 * 
 * F3模块增强：AI内容生成 + 封面生成
 */
@Service
@Slf4j
public class ReportAiServiceImpl implements ReportAiService {

    @Value("${ai.openai.api-key:}")
    private String openaiApiKey;
    
    @Value("${ai.openai.model:gpt-4}")
    private String openaiModel;
    
    @Value("${ai.openai.base-url:https://api.openai.com/v1}")
    private String openaiBaseUrl;
    
    @Value("${ai.dalle.api-key:}")
    private String dalleApiKey;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private final OkHttpClient httpClient = new OkHttpClient();

    @Override
    public ReportContentVO generateReportContent(ReportTemplate template, ReportData data) {
        log.info("开始生成AI报表内容，模板：{}, 数据类型：{}", template.getName(), data.getDataType());
        
        try {
            // 1. 生成报表分析文字
            String analysisText = generateAnalysisText(template, data);
            
            // 2. 生成关键洞察
            String keyInsights = generateKeyInsights(data);
            
            // 3. 生成执行建议
            String recommendations = generateRecommendations(data);
            
            // 4. 生成封面图片URL
            String coverImageUrl = generateCoverImage(template.getName(), data.getPeriod());
            
            // 5. 组装返回内容
            ReportContentVO content = new ReportContentVO();
            content.setAnalysisText(analysisText);
            content.setKeyInsights(keyInsights);
            content.setRecommendations(recommendations);
            content.setCoverImageUrl(coverImageUrl);
            content.setGeneratedAt(LocalDateTime.now());
            content.setAiModelUsed(openaiModel);
            
            log.info("AI报表内容生成成功，包含{}字符的分析文本", analysisText.length());
            return content;
            
        } catch (Exception e) {
            log.error("生成AI报表内容失败", e);
            // 降级方案：返回基础模板内容
            return generateFallbackContent(template, data);
        }
    }

    @Override
    public String generateAnalysisText(ReportTemplate template, ReportData data) {
        String prompt = buildAnalysisPrompt(template, data);
        
        try {
            String response = callOpenAI(prompt, 1500);
            return response;
        } catch (IOException e) {
            log.warn("调用OpenAI失败，使用本地模板生成分析文本", e);
            return generateLocalAnalysisText(template, data);
        }
    }

    @Override
    public String generateKeyInsights(ReportData data) {
        String prompt = buildKeyInsightsPrompt(data);
        
        try {
            String response = callOpenAI(prompt, 500);
            return response;
        } catch (IOException e) {
            log.warn("调用OpenAI失败，使用本地逻辑生成关键洞察", e);
            return generateLocalKeyInsights(data);
        }
    }

    @Override
    public String generateRecommendations(ReportData data) {
        String prompt = buildRecommendationsPrompt(data);
        
        try {
            String response = callOpenAI(prompt, 800);
            return response;
        } catch (IOException e) {
            log.warn("调用OpenAI失败，使用本地逻辑生成建议", e);
            return generateLocalRecommendations(data);
        }
    }

    @Override
    public String generateCoverImage(String reportTitle, String period) {
        if (dalleApiKey == null || dalleApiKey.isEmpty()) {
            log.warn("DALL-E API密钥未配置，返回默认封面URL");
            return "https://example.com/default-report-cover.jpg";
        }
        
        String prompt = buildCoverImagePrompt(reportTitle, period);
        
        try {
            String imageUrl = callDalle(prompt);
            return imageUrl;
        } catch (Exception e) {
            log.warn("生成封面图片失败，返回默认图片", e);
            return generateDefaultCoverUrl(reportTitle);
        }
    }

    private String callOpenAI(String prompt, int maxTokens) throws IOException {
        String url = openaiBaseUrl + "/chat/completions";
        
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", openaiModel);
        requestBody.put("messages", new Object[]{
            Map.of("role", "system", "content", "你是一位专业的商业分析师，擅长生成结构化、洞察力强的报表内容。"),
            Map.of("role", "user", "content", prompt)
        });
        requestBody.put("max_tokens", maxTokens);
        requestBody.put("temperature", 0.7);
        
        RequestBody body = RequestBody.create(
            objectMapper.writeValueAsString(requestBody),
            MediaType.parse("application/json")
        );
        
        Request request = new Request.Builder()
            .url(url)
            .header("Authorization", "Bearer " + openaiApiKey)
            .header("Content-Type", "application/json")
            .post(body)
            .build();
        
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("OpenAI API调用失败: " + response.code() + " " + response.message());
            }
            
            String responseBody = response.body().string();
            Map<String, Object> result = objectMapper.readValue(responseBody, Map.class);
            Map<String, Object> choice = (Map<String, Object>) ((java.util.List) result.get("choices")).get(0);
            Map<String, String> message = (Map<String, String>) choice.get("message");
            
            return message.get("content");
        }
    }

    private String callDalle(String prompt) throws IOException {
        String url = openaiBaseUrl + "/images/generations";
        
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("prompt", prompt);
        requestBody.put("n", 1);
        requestBody.put("size", "1024x1024");
        requestBody.put("response_format", "url");
        
        RequestBody body = RequestBody.create(
            objectMapper.writeValueAsString(requestBody),
            MediaType.parse("application/json")
        );
        
        Request request = new Request.Builder()
            .url(url)
            .header("Authorization", "Bearer " + dalleApiKey)
            .header("Content-Type", "application/json")
            .post(body)
            .build();
        
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("DALL-E API调用失败: " + response.code() + " " + response.message());
            }
            
            String responseBody = response.body().string();
            Map<String, Object> result = objectMapper.readValue(responseBody, Map.class);
            java.util.List<Map<String, String>> data = (java.util.List<Map<String, String>>) result.get("data");
            
            return data.get(0).get("url");
        }
    }

    private String buildAnalysisPrompt(ReportTemplate template, ReportData data) {
        return String.format(
            "请为《%s》报表生成专业分析内容。\n" +
            "报表类型：%s\n" +
            "数据周期：%s\n" +
            "关键指标：%s\n" +
            "数据摘要：%s\n" +
            "要求：\n" +
            "1. 包含趋势分析\n" +
            "2. 突出亮点和问题\n" +
            "3. 提供数据支撑的具体分析\n" +
            "4. 语言专业但不晦涩\n" +
            "5. 800-1500字\n",
            template.getName(),
            template.getType(),
            data.getPeriod(),
            data.getKeyMetrics(),
            data.getSummary()
        );
    }

    private String buildKeyInsightsPrompt(ReportData data) {
        return String.format(
            "从以下数据中提炼3-5个关键洞察：\n" +
            "数据摘要：%s\n" +
            "趋势变化：%s\n" +
            "异常情况：%s\n" +
            "要求：\n" +
            "1. 每个洞察不超过100字\n" +
            "2. 突出商业价值\n" +
            "3. 可行动的建议点\n",
            data.getSummary(),
            data.getTrend(),
            data.getAnomalies()
        );
    }

    private String buildRecommendationsPrompt(ReportData data) {
        return String.format(
            "基于以下数据提供可执行的建议：\n" +
            "数据情况：%s\n" +
            "存在问题：%s\n" +
            "目标设定：%s\n" +
            "要求：\n" +
            "1. 3-5条具体建议\n" +
            "2. 每条建议有明确的责任人和时间点\n" +
            "3. 可量化衡量的成果\n",
            data.getSummary(),
            data.getIssues(),
            data.getTargets()
        );
    }

    private String buildCoverImagePrompt(String title, String period) {
        return String.format(
            "专业、现代、简洁的商业报表封面图片，" +
            "标题：%s，周期：%s，" +
            "风格：企业商务风，蓝色渐变背景，" +
            "包含数据可视化元素，" +
            "分辨率：1024x1024，" +
            "高质量数字艺术",
            title,
            period
        );
    }

    private String generateLocalAnalysisText(ReportTemplate template, ReportData data) {
        return String.format(
            "## 《%s》分析报告\n\n" +
            "**数据周期**：%s\n\n" +
            "### 总体概况\n" +
            "本期数据总体表现%s，关键指标%s趋势。\n\n" +
            "### 详细分析\n" +
            "1. 主要指标完成情况：%s\n" +
            "2. 环比变化分析：%s\n" +
            "3. 同比变化分析：%s\n\n" +
            "### 结论\n" +
            "本期表现%s，建议%s。\n",
            template.getName(),
            data.getPeriod(),
            data.getTrend(),
            data.getKeyMetrics(),
            data.getSummary(),
            data.getTrend(),
            data.getAnomalies(),
            data.getTrend().contains("增长") ? "良好" : "有待改进",
            data.getRecommendations()
        );
    }

    private String generateLocalKeyInsights(ReportData data) {
        return String.format(
            "1. **核心发现**：%s\n" +
            "2. **趋势信号**：%s\n" +
            "3. **风险提示**：%s\n" +
            "4. **机会点**：%s\n",
            data.getKeyMetrics(),
            data.getTrend(),
            data.getAnomalies(),
            data.getOpportunities()
        );
    }

    private String generateLocalRecommendations(ReportData data) {
        return String.format(
            "1. **立即行动**：%s（责任人：PM，时间：本周）\n" +
            "2. **中期优化**：%s（责任人：Team Lead，时间：下月）\n" +
            "3. **长期规划**：%s（责任人：Director，时间：季度）\n",
            data.getPriorityActions(),
            data.getImprovementAreas(),
            data.getStrategicGoals()
        );
    }

    private String generateDefaultCoverUrl(String title) {
        // 使用占位图片服务生成默认封面
        String encodedTitle = title.replace(" ", "%20");
        return String.format(
            "https://via.placeholder.com/1024x1024/0066CC/FFFFFF?text=%s",
            encodedTitle
        );
    }

    private ReportContentVO generateFallbackContent(ReportTemplate template, ReportData data) {
        ReportContentVO content = new ReportContentVO();
        content.setAnalysisText(generateLocalAnalysisText(template, data));
        content.setKeyInsights(generateLocalKeyInsights(data));
        content.setRecommendations(generateLocalRecommendations(data));
        content.setCoverImageUrl(generateDefaultCoverUrl(template.getName()));
        content.setGeneratedAt(LocalDateTime.now());
        content.setAiModelUsed("local-fallback");
        return content;
    }
}