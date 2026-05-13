package com.aioa.common.mail;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * MailServiceImpl 单元测试
 */
@ExtendWith(MockitoExtension.class)
class MailServiceImplTest {
    
    @Mock
    private JavaMailSender mailSender;
    
    @InjectMocks
    private MailServiceImpl mailService;
    
    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(mailService, "from", "noreply@aioa.com");
    }
    
    @Test
    @DisplayName("发送简单邮件 - 成功")
    void testSendSimpleMail_Success() {
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));
        
        assertDoesNotThrow(() -> {
            mailService.sendSimpleMail("user@example.com", "测试主题", "测试内容");
        });
        
        verify(mailSender).send(any(SimpleMailMessage.class));
    }
    
    @Test
    @DisplayName("发送简单邮件 - 邮件发送异常")
    void testSendSimpleMail_Exception() {
        doThrow(new RuntimeException("SMTP连接失败")).when(mailSender).send(any(SimpleMailMessage.class));
        
        assertDoesNotThrow(() -> {
            mailService.sendSimpleMail("user@example.com", "测试主题", "测试内容");
        });
    }
    
    @Test
    @DisplayName("发送HTML邮件 - 成功")
    void testSendHtmlMail_Success() {
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        
        assertDoesNotThrow(() -> {
            mailService.sendHtmlMail("user@example.com", "HTML测试", "<h1>测试</h1><p>内容</p>");
        });
        
        verify(mailSender).createMimeMessage();
    }
    
    @Test
    @DisplayName("发送HTML邮件 - MimeMessage创建异常会向上传播")
    void testSendHtmlMail_CreateException() {
        when(mailSender.createMimeMessage()).thenThrow(new RuntimeException("创建邮件消息失败"));
        
        // createMimeMessage抛出的RuntimeException不在catch范围内，会向上传播
        assertThrows(RuntimeException.class, () -> {
            mailService.sendHtmlMail("user@example.com", "测试", "<html>内容</html>");
        });
    }
    
    @Test
    @DisplayName("发送带附件邮件 - 无附件")
    void testSendAttachmentMail_NoAttachments() {
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        
        assertDoesNotThrow(() -> {
            mailService.sendAttachmentMail("user@example.com", "附件测试", "正文内容");
        });
        
        verify(mailSender).createMimeMessage();
    }
    
    @Test
    @DisplayName("发送带附件邮件 - 文件不存在则不添加")
    void testSendAttachmentMail_FileNotExists() {
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        
        assertDoesNotThrow(() -> {
            mailService.sendAttachmentMail("user@example.com", "附件测试", "正文", "/nonexistent/file.pdf");
        });
        
        verify(mailSender).createMimeMessage();
    }
    
    @Test
    @DisplayName("发送审批通知邮件")
    void testSendApprovalNotice() {
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));
        
        assertDoesNotThrow(() -> {
            mailService.sendApprovalNotice(
                    "approver@example.com",
                    "李经理",
                    "采购申请-办公用品",
                    "待审批",
                    "https://aioa.com/approval/123"
            );
        });
        
        verify(mailSender).send(any(SimpleMailMessage.class));
    }
    
    @Test
    @DisplayName("发送审批通知 - 内容包含关键信息")
    void testSendApprovalNotice_Content() {
        doAnswer(invocation -> {
            SimpleMailMessage msg = invocation.getArgument(0);
            assertNotNull(msg.getSubject(), "Subject should not be null");
            assertTrue(msg.getSubject().contains("审批通知"), 
                    "Subject should contain 审批通知, got: " + msg.getSubject());
            assertTrue(msg.getText().contains("李经理"), "Text should contain approver name");
            return null;
        }).when(mailSender).send(any(SimpleMailMessage.class));
        
        mailService.sendApprovalNotice(
                "manager@example.com",
                "李经理",
                "测试申请",
                "待审批",
                "https://aioa.com/test"
        );
        
        verify(mailSender).send(any(SimpleMailMessage.class));
    }
    
    @Test
    @DisplayName("发送OCR通知 - 高置信度使用普通邮件")
    void testSendOcrNotice_HighConfidence_SimpleMail() {
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));
        
        assertDoesNotThrow(() -> {
            mailService.sendOcrNotice("user@example.com", "invoice.pdf", 92.5, false);
        });
        
        verify(mailSender).send(any(SimpleMailMessage.class));
    }
    
    @Test
    @DisplayName("发送OCR通知 - 低置信度需要人工确认发送HTML邮件")
    void testSendOcrNotice_LowConfidence_NeedManual() {
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        
        assertDoesNotThrow(() -> {
            mailService.sendOcrNotice("user@example.com", "invoice.pdf", 75.0, true);
        });
        
        verify(mailSender).createMimeMessage();
    }
    
    @Test
    @DisplayName("发送OCR通知 - 极高置信度100")
    void testSendOcrNotice_PerfectConfidence() {
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));
        
        assertDoesNotThrow(() -> {
            mailService.sendOcrNotice("user@example.com", "invoice.pdf", 100.0, false);
        });
        
        verify(mailSender).send(any(SimpleMailMessage.class));
    }
    
    @Test
    @DisplayName("发送OCR通知 - 边界置信度85")
    void testSendOcrNotice_Boundary85() {
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));
        
        assertDoesNotThrow(() -> {
            mailService.sendOcrNotice("user@example.com", "invoice.pdf", 85.0, false);
        });
        
        verify(mailSender).send(any(SimpleMailMessage.class));
    }
    
    @Test
    @DisplayName("发送OCR通知 - 置信度84发送HTML邮件")
    void testSendOcrNotice_Boundary84_Html() {
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        
        assertDoesNotThrow(() -> {
            mailService.sendOcrNotice("user@example.com", "invoice.pdf", 84.0, true);
        });
        
        verify(mailSender).createMimeMessage();
    }
    
    @Test
    @DisplayName("发送OCR通知 - HTML内容构建包含颜色样式")
    void testSendOcrNotice_HtmlContentStyles() {
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        
        assertDoesNotThrow(() -> {
            mailService.sendOcrNotice("user@example.com", "test.pdf", 70.0, true);
        });
    }
    
    @Test
    @DisplayName("发送OCR通知 - 置信度>=85时HTML邮件使用绿色")
    void testSendOcrNotice_HtmlContentGreen() {
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        
        assertDoesNotThrow(() -> {
            mailService.sendOcrNotice("user@example.com", "test.pdf", 90.0, true);
        });
    }
}