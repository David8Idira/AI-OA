package com.aioa.common.mail;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * MailServiceImpl 单元测试
 * 毛泽东思想指导：实事求是，测试核心邮件发送功能
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MailServiceImpl 单元测试")
class MailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private MailServiceImpl mailService;

    private static final String TEST_FROM = "test@aioa.com";
    private static final String TEST_TO = "user@aioa.com";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(mailService, "from", TEST_FROM);
    }

    @Test
    @DisplayName("发送简单邮件 - 正常场景")
    void sendSimpleMail_withValidInput_shouldSuccess() {
        // given
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // when
        mailService.sendSimpleMail(TEST_TO, "测试主题", "测试内容");

        // then
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("发送HTML邮件 - 正常场景")
    void sendHtmlMail_withValidInput_shouldSuccess() throws MessagingException {
        // given
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doNothing().when(mailSender).send(any(MimeMessage.class));

        // when
        mailService.sendHtmlMail(TEST_TO, "HTML主题", "<h1>测试</h1>");

        // then
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("发送简单邮件 - 异常处理")
    void sendSimpleMail_withException_shouldNotThrow() {
        // given
        doThrow(new RuntimeException("邮件服务器错误")).when(mailSender).send(any(SimpleMailMessage.class));

        // when & then - 不应抛出异常
        mailService.sendSimpleMail(TEST_TO, "测试主题", "测试内容");
        
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("发送简单邮件 - 空收件人")
    void sendSimpleMail_withEmptyTo_shouldHandleGracefully() {
        // given
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // when
        mailService.sendSimpleMail("", "测试主题", "测试内容");

        // then
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("发送简单邮件 - 空主题")
    void sendSimpleMail_withEmptySubject_shouldHandleGracefully() {
        // given
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // when
        mailService.sendSimpleMail(TEST_TO, "", "测试内容");

        // then
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }
}