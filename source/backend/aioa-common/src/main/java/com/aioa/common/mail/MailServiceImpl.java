package com.aioa.common.mail;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.io.File;

/**
 * 邮件服务实现
 */
@Slf4j
@Service
public class MailServiceImpl implements MailService {
    
    @Autowired
    private JavaMailSender mailSender;
    
    @Value("${spring.mail.username:}")
    private String from;
    
    @Override
    public void sendSimpleMail(String to, String subject, String content) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(content);
            
            mailSender.send(message);
            log.info("邮件发送成功: {} -> {}", from, to);
        } catch (Exception e) {
            log.error("邮件发送失败", e);
        }
    }
    
    @Override
    public void sendHtmlMail(String to, String subject, String htmlContent) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            
            mailSender.send(mimeMessage);
            log.info("HTML邮件发送成功: {}", to);
        } catch (MessagingException e) {
            log.error("HTML邮件发送失败", e);
        }
    }
    
    @Override
    public void sendAttachmentMail(String to, String subject, String content, String... attachments) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content);
            
            // 添加附件
            for (String path : attachments) {
                File file = new File(path);
                if (file.exists()) {
                    helper.addAttachment(file.getName(), new FileSystemResource(file));
                }
            }
            
            mailSender.send(mimeMessage);
            log.info("带附件邮件发送成功: {}", to);
        } catch (MessagingException e) {
            log.error("带附件邮件发送失败", e);
        }
    }
    
    @Override
    public void sendApprovalNotice(String to, String approver, String title, String status, String url) {
        String subject = "【审批通知】" + title;
        String content = String.format(
            "您好 %s，\n\n" +
            "您有一个新的审批请求：\n\n" +
            "标题：%s\n" +
            "状态：%s\n" +
            "点击查看：%s\n\n" +
            "请及时处理。\n\n" +
            "AI-OA系统",
            approver, title, status, url
        );
        sendSimpleMail(to, subject, content);
    }
    
    @Override
    public void sendOcrNotice(String to, String fileName, double confidence, boolean needManual) {
        String subject = needManual 
            ? "【OCR识别需人工确认】" + fileName 
            : "【OCR识别完成】" + fileName;
        
        String content = String.format(
            "您好，\n\n" +
            "文件 %s 已完成OCR识别。\n\n" +
            "识别置信度：%.1f%%\n" +
            "%s\n\n" +
            "%s\n\n" +
            "AI-OA系统",
            fileName,
            confidence,
            confidence >= 85 ? "识别结果可信，可继续审批流程" : "置信度较低，需要人工确认",
            needManual ? "请登录系统进行人工确认" : "如有问题，请联系管理员"
        );
        
        if (needManual) {
            sendHtmlMail(to, subject, buildOcrHtml(fileName, confidence));
        } else {
            sendSimpleMail(to, subject, content);
        }
    }
    
    /**
     * 构建OCR HTML邮件内容
     */
    private String buildOcrHtml(String fileName, double confidence) {
        return String.format(
            "<html><body>" +
            "<h2>OCR识别需人工确认</h2>" +
            "<p>文件：%s</p>" +
            "<p>置信度：<strong style='color:%s'>%.1f%%</strong></p>" +
            "<p>请登录系统进行人工确认</p>" +
            "</body></html>",
            fileName,
            confidence < 85 ? "red" : "green",
            confidence
        );
    }
}