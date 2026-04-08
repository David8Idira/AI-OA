package com.aioa.common.mail;

/**
 * 邮件服务接口
 */
public interface MailService {
    
    /**
     * 发送简单邮件
     * @param to 收件人
     * @param subject 主题
     * @param content 内容
     */
    void sendSimpleMail(String to, String subject, String content);
    
    /**
     * 发送HTML邮件
     * @param to 收件人
     * @param subject 主题
     * @param htmlContent HTML内容
     */
    void sendHtmlMail(String to, String subject, String htmlContent);
    
    /**
     * 发送带附件的邮件
     * @param to 收件人
     * @param subject 主题
     * @param content 内容
     * @param attachments 附件路径数组
     */
    void sendAttachmentMail(String to, String subject, String content, String... attachments);
    
    /**
     * 发送审批通知邮件
     * @param to 收件人
     * @param approver 审批人
     * @param title 审批标题
     * @param status 审批状态
     * @param url 审批链接
     */
    void sendApprovalNotice(String to, String approver, String title, String status, String url);
    
    /**
     * 发送OCR识别结果通知
     * @param to 收件人
     * @param fileName 文件名
     * @param confidence 置信度
     * @param needManual 需要人工确认
     */
    void sendOcrNotice(String to, String fileName, double confidence, boolean needManual);
}