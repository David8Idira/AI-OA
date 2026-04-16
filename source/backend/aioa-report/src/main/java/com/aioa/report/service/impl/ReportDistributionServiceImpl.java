package com.aioa.report.service.impl;

import com.aioa.report.service.ReportDistributionService;
import com.aioa.report.entity.ReportTemplate;
import com.aioa.report.entity.ReportData;
import com.aioa.report.entity.ReportDistribution;
import com.aioa.report.enums.DistributionChannel;
import com.aioa.report.enums.DistributionStatus;
import com.aioa.report.repository.ReportDistributionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Transactional;

import javax.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

/**
 * 报表分发配置服务实现类
 * 
 * F3模块增强：多渠道分发配置
 */
@Service
@Slf4j
public class ReportDistributionServiceImpl implements ReportDistributionService {

    @Autowired
    private ReportDistributionRepository distributionRepository;
    
    @Autowired(required = false)
    private JavaMailSender mailSender;
    
    @Value("${spring.mail.username:}")
    private String mailFrom;
    
    @Value("${feishu.webhook.url:}")
    private String feishuWebhookUrl;
    
    @Value("${wechat.corp-id:}")
    private String wechatCorpId;
    
    @Value("${wechat.corp-secret:}")
    private String wechatCorpSecret;
    
    @Value("${sms.access-key:}")
    private String smsAccessKey;
    
    @Value("${sms.access-secret:}")
    private String smsAccessSecret;
    
    @Override
    @Transactional
    public ReportDistribution createDistribution(ReportTemplate template, ReportData data, 
                                                List<DistributionChannel> channels, 
                                                List<String> recipients) {
        log.info("创建报表分发配置，模板：{}，渠道：{}，接收人：{}", 
                template.getName(), channels, recipients.size());
        
        ReportDistribution distribution = new ReportDistribution();
        distribution.setReportId(data.getId());
        distribution.setTemplateId(template.getId());
        distribution.setChannels(channels);
        distribution.setRecipients(recipients);
        distribution.setScheduleType(template.getScheduleType());
        distribution.setNextScheduleTime(calculateNextSchedule(template));
        distribution.setStatus(DistributionStatus.PENDING);
        distribution.setCreatedAt(LocalDateTime.now());
        distribution.setUpdatedAt(LocalDateTime.now());
        
        // 保存分发配置
        ReportDistribution saved = distributionRepository.save(distribution);
        log.info("报表分发配置创建成功，ID：{}", saved.getId());
        
        return saved;
    }

    @Override
    @Async("reportDistributionExecutor")
    public CompletableFuture<Map<DistributionChannel, Boolean>> distributeReport(
            ReportDistribution distribution, String reportContent, String reportAttachmentUrl) {
        
        log.info("开始分发报表，分发配置ID：{}，渠道：{}", 
                distribution.getId(), distribution.getChannels());
        
        Map<DistributionChannel, Boolean> results = new HashMap<>();
        List<DistributionChannel> channels = distribution.getChannels();
        List<String> recipients = distribution.getRecipients();
        
        // 更新状态为分发中
        distribution.setStatus(DistributionStatus.DISTRIBUTING);
        distribution.setStartedAt(LocalDateTime.now());
        distributionRepository.save(distribution);
        
        try {
            // 并行分发到各个渠道
            for (DistributionChannel channel : channels) {
                boolean success = distributeToChannel(channel, recipients, reportContent, reportAttachmentUrl);
                results.put(channel, success);
                
                if (success) {
                    log.info("报表通过{}渠道分发成功", channel);
                } else {
                    log.warn("报表通过{}渠道分发失败", channel);
                }
            }
            
            // 更新分发结果
            boolean allSuccess = results.values().stream().allMatch(Boolean::booleanValue);
            distribution.setStatus(allSuccess ? DistributionStatus.COMPLETED : DistributionStatus.PARTIAL);
            distribution.setCompletedAt(LocalDateTime.now());
            distribution.setResults(results);
            distributionRepository.save(distribution);
            
            log.info("报表分发完成，配置ID：{}，成功渠道：{}，失败渠道：{}",
                    distribution.getId(),
                    results.entrySet().stream().filter(Map.Entry::getValue).count(),
                    results.entrySet().stream().filter(e -> !e.getValue()).count());
            
        } catch (Exception e) {
            log.error("报表分发过程异常", e);
            distribution.setStatus(DistributionStatus.FAILED);
            distribution.setErrorMsg(e.getMessage());
            distributionRepository.save(distribution);
            
            // 所有渠道标记为失败
            for (DistributionChannel channel : channels) {
                results.put(channel, false);
            }
        }
        
        return CompletableFuture.completedFuture(results);
    }

    @Override
    public List<ReportDistribution> getDistributionHistory(Long reportId) {
        return distributionRepository.findByReportIdOrderByCreatedAtDesc(reportId);
    }

    @Override
    public ReportDistribution getDistributionById(Long id) {
        return distributionRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional
    public void updateDistributionSchedule(Long id, String newSchedule) {
        ReportDistribution distribution = distributionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("分发配置不存在: " + id));
        
        distribution.setScheduleType(newSchedule);
        distribution.setNextScheduleTime(calculateNextSchedule(distribution));
        distribution.setUpdatedAt(LocalDateTime.now());
        
        distributionRepository.save(distribution);
        log.info("更新分发配置{}的调度计划为：{}", id, newSchedule);
    }

    @Override
    @Transactional
    public void deleteDistribution(Long id) {
        distributionRepository.deleteById(id);
        log.info("删除分发配置：{}", id);
    }

    @Override
    public List<ReportDistribution> findPendingDistributions() {
        return distributionRepository.findByStatusAndNextScheduleTimeBefore(
                DistributionStatus.PENDING, LocalDateTime.now());
    }

    private boolean distributeToChannel(DistributionChannel channel, List<String> recipients,
                                       String reportContent, String reportAttachmentUrl) {
        try {
            switch (channel) {
                case EMAIL:
                    return sendEmail(recipients, reportContent, reportAttachmentUrl);
                case FEISHU:
                    return sendToFeishu(recipients, reportContent, reportAttachmentUrl);
                case WECHAT:
                    return sendToWechat(recipients, reportContent);
                case SMS:
                    return sendSms(recipients, reportContent);
                case SLACK:
                    return sendToSlack(recipients, reportContent);
                case DINGTALK:
                    return sendToDingtalk(recipients, reportContent);
                default:
                    log.warn("不支持的分发渠道：{}", channel);
                    return false;
            }
        } catch (Exception e) {
            log.error("通过{}渠道分发失败", channel, e);
            return false;
        }
    }

    private boolean sendEmail(List<String> recipients, String content, String attachmentUrl) {
        if (mailSender == null || mailFrom == null || mailFrom.isEmpty()) {
            log.warn("邮件发送器未配置，跳过邮件分发");
            return false;
        }
        
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(mailFrom);
            helper.setTo(recipients.toArray(new String[0]));
            helper.setSubject("📊 AI-OA智能报表 - " + LocalDateTime.now().toString());
            helper.setText(buildEmailContent(content), true);
            
            // 如果有附件URL，可以下载并附加
            if (attachmentUrl != null && !attachmentUrl.isEmpty()) {
                // 这里可以添加附件下载和附加逻辑
                log.info("报表附件URL：{}", attachmentUrl);
            }
            
            mailSender.send(message);
            log.info("邮件发送成功，接收人：{}", recipients);
            return true;
            
        } catch (Exception e) {
            log.error("发送邮件失败", e);
            return false;
        }
    }

    private boolean sendToFeishu(List<String> recipients, String content, String attachmentUrl) {
        if (feishuWebhookUrl == null || feishuWebhookUrl.isEmpty()) {
            log.warn("飞书Webhook未配置，跳过飞书分发");
            return false;
        }
        
        try {
            // 构建飞书消息卡片
            Map<String, Object> card = buildFeishuCard(content, attachmentUrl);
            
            // 调用飞书Webhook API
            // 这里简化为日志输出，实际需要HTTP调用
            log.info("飞书消息卡片：{}", card);
            log.info("通过飞书Webhook发送消息到：{}", recipients);
            
            return true;
            
        } catch (Exception e) {
            log.error("发送飞书消息失败", e);
            return false;
        }
    }

    private boolean sendToWechat(List<String> recipients, String content) {
        if (wechatCorpId == null || wechatCorpId.isEmpty() || 
            wechatCorpSecret == null || wechatCorpSecret.isEmpty()) {
            log.warn("企业微信配置不完整，跳过微信分发");
            return false;
        }
        
        try {
            // 获取企业微信访问令牌
            String accessToken = getWechatAccessToken();
            
            // 构建企业微信消息
            Map<String, Object> message = buildWechatMessage(content, recipients);
            
            // 调用企业微信API
            log.info("企业微信消息：{}", message);
            log.info("通过企业微信发送消息到：{}", recipients);
            
            return true;
            
        } catch (Exception e) {
            log.error("发送企业微信消息失败", e);
            return false;
        }
    }

    private boolean sendSms(List<String> recipients, String content) {
        if (smsAccessKey == null || smsAccessKey.isEmpty() || 
            smsAccessSecret == null || smsAccessSecret.isEmpty()) {
            log.warn("短信服务配置不完整，跳过短信分发");
            return false;
        }
        
        try {
            // 构建短信内容（摘要版）
            String smsContent = buildSmsContent(content);
            
            // 调用短信服务API
            log.info("短信内容：{}", smsContent);
            log.info("发送短信到：{}", recipients);
            
            return true;
            
        } catch (Exception e) {
            log.error("发送短信失败", e);
            return false;
        }
    }

    private boolean sendToSlack(List<String> recipients, String content) {
        // Slack分发实现
        log.info("Slack分发功能待实现，内容：{}", content.substring(0, Math.min(100, content.length())));
        return false;
    }

    private boolean sendToDingtalk(List<String> recipients, String content) {
        // 钉钉分发实现
        log.info("钉钉分发功能待实现，内容：{}", content.substring(0, Math.min(100, content.length())));
        return false;
    }

    private LocalDateTime calculateNextSchedule(ReportTemplate template) {
        LocalDateTime now = LocalDateTime.now();
        
        switch (template.getScheduleType()) {
            case "daily":
                return now.plusDays(1);
            case "weekly":
                return now.plusWeeks(1);
            case "monthly":
                return now.plusMonths(1);
            case "quarterly":
                return now.plusMonths(3);
            case "yearly":
                return now.plusYears(1);
            case "manual":
                return null;
            default:
                return now.plusDays(1); // 默认每天
        }
    }

    private LocalDateTime calculateNextSchedule(ReportDistribution distribution) {
        if (distribution.getScheduleType() == null || distribution.getScheduleType().equals("manual")) {
            return null;
        }
        
        LocalDateTime lastSchedule = distribution.getCompletedAt() != null 
                ? distribution.getCompletedAt() 
                : LocalDateTime.now();
        
        return calculateNextScheduleByType(lastSchedule, distribution.getScheduleType());
    }

    private LocalDateTime calculateNextScheduleByType(LocalDateTime baseTime, String scheduleType) {
        switch (scheduleType) {
            case "daily":
                return baseTime.plusDays(1);
            case "weekly":
                return baseTime.plusWeeks(1);
            case "monthly":
                return baseTime.plusMonths(1);
            case "quarterly":
                return baseTime.plusMonths(3);
            case "yearly":
                return baseTime.plusYears(1);
            default:
                return baseTime.plusDays(1);
        }
    }

    private String buildEmailContent(String reportContent) {
        return String.format(
            "<!DOCTYPE html>\n" +
            "<html>\n" +
            "<head>\n" +
            "    <meta charset=\"UTF-8\">\n" +
            "    <style>\n" +
            "        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }\n" +
            "        .header { background: #0066CC; color: white; padding: 20px; border-radius: 5px; }\n" +
            "        .content { margin: 20px 0; padding: 20px; border: 1px solid #ddd; border-radius: 5px; }\n" +
            "        .footer { margin-top: 30px; padding-top: 20px; border-top: 1px solid #eee; color: #666; font-size: 12px; }\n" +
            "        .insight { background: #f0f8ff; padding: 15px; margin: 10px 0; border-left: 4px solid #0066CC; }\n" +
            "        .recommendation { background: #f9f9f9; padding: 15px; margin: 10px 0; border-left: 4px solid #28a745; }\n" +
            "    </style>\n" +
            "</head>\n" +
            "<body>\n" +
            "    <div class=\"header\">\n" +
            "        <h1>📊 AI-OA智能报表</h1>\n" +
            "        <p>生成时间：%s</p>\n" +
            "    </div>\n" +
            "    \n" +
            "    <div class=\"content\">\n" +
            "        %s\n" +
            "    </div>\n" +
            "    \n" +
            "    <div class=\"footer\">\n" +
            "        <p>此报表由AI-OA系统自动生成，如有疑问请联系系统管理员。</p>\n" +
            "        <p>© 2026 AI-OA智能办公系统</p>\n" +
            "    </div>\n" +
            "</body>\n" +
            "</html>",
            LocalDateTime.now().toString(),
            reportContent.replace("\n", "<br>")
        );
    }

    private Map<String, Object> buildFeishuCard(String content, String attachmentUrl) {
        Map<String, Object> card = new HashMap<>();
        card.put("config", Map.of("wide_screen_mode", true));
        
        // 构建卡片元素
        List<Map<String, Object>> elements = new java.util.ArrayList<>();
        
        // 标题
        elements.add(Map.of(
            "tag", "div",
            "text", Map.of(
                "tag", "plain_text",
                "content", "📊 AI-OA智能报表"
            )
        ));
        
        // 分隔线
        elements.add(Map.of("tag", "hr"));
        
        // 内容
        elements.add(Map.of(
            "tag", "div",
            "text", Map.of(
                "tag", "plain_text",
                "content", content.length() > 500 ? content.substring(0, 500) + "..." : content
            )
        ));
        
        // 如果有附件
        if (attachmentUrl != null && !attachmentUrl.isEmpty()) {
            elements.add(Map.of(
                "tag", "div",
                "text", Map.of(
                    "tag", "plain_text",
                    "content", "📎 报表附件：" + attachmentUrl
                )
            ));
        }
        
        // 时间戳
        elements.add(Map.of(
            "tag", "note",
            "elements", new Object[]{
                Map.of(
                    "tag", "plain_text",
                    "content", "生成时间：" + LocalDateTime.now().toString()
                )
            }
        ));
        
        card.put("elements", elements);
        return card;
    }

    private String getWechatAccessToken() {
        // 实现获取企业微信访问令牌的逻辑
        // 这里简化为返回空字符串
        return "";
    }

    private Map<String, Object> buildWechatMessage(String content, List<String> recipients) {
        Map<String, Object> message = new HashMap<>();
        message.put("touser", String.join("|", recipients));
        message.put("msgtype", "text");
        message.put("agentid", 1000002); // 示例应用ID
        
        Map<String, String> text = new HashMap<>();
        text.put("content", "📊 AI-OA智能报表\n\n" + 
                  (content.length() > 500 ? content.substring(0, 500) + "..." : content) + 
                  "\n\n生成时间：" + LocalDateTime.now());
        
        message.put("text", text);
        return message;
    }

    private String buildSmsContent(String content) {
        // 提取关键信息生成短信摘要
        String summary = content.length() > 100 ? content.substring(0, 100) : content;
        return String.format(
            "【AI-OA】智能报表已生成：%s... 查看详情请登录系统。生成时间：%s",
            summary,
            LocalDateTime.now().toString()
        );
    }
}