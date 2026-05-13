package com.aioa.im.service;

import com.aioa.common.exception.BusinessException;
import com.aioa.im.dto.ConversationCreateDTO;
import com.aioa.im.entity.Conversation;
import com.aioa.im.entity.ConversationMember;
import com.aioa.im.mapper.ConversationMapper;
import com.aioa.im.mapper.ConversationMemberMapper;
import com.aioa.im.mapper.MessageMapper;
import com.aioa.im.service.impl.ConversationServiceImpl;
import com.aioa.im.vo.ConversationVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ConversationServiceImpl 单元测试
 * 覆盖会话列表查询、已读标记、创建会话、删除会话等核心功能
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ConversationServiceImplTest 会话服务测试")
class ConversationServiceImplTest {

    @Mock
    private ConversationMapper conversationMapper;

    @Mock
    private ConversationMemberMapper memberMapper;

    @Mock
    private MessageMapper messageMapper;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private ConversationServiceImpl conversationService;

    private static final String USER_ID = "user-001";
    private static final String OTHER_USER_ID = "user-002";
    private static final String CONV_ID = "conv-001";

    @BeforeEach
    void setUp() throws Exception {
        conversationService = new ConversationServiceImpl(memberMapper, messageMapper, redisTemplate);
        // ServiceImpl 从 IService 继承 baseMapper 字段，Spring 环境外为 null，手动注入
        injectBaseMapper(conversationService, conversationMapper);
        // Pre-warm MyBatis-Plus lambda cache so LambdaUpdateWrapper doesn't fail with "can not find lambda cache"
        warmLambdaCache(ConversationMember.class, "com.aioa.im.mapper.ConversationMemberMapper");
    }

    // 预热 MyBatis-Plus lambda 缓存
    private void warmLambdaCache(Class<?> entityClass, String mapperNamespace) throws Exception {
        Method initMethod = TableInfoHelper.class.getDeclaredMethod(
                "initTableInfo", Configuration.class, String.class, Class.class);
        initMethod.setAccessible(true);
        initMethod.invoke(null, new Configuration(), mapperNamespace, entityClass);
    }

    /**
     * 通过反射将 mock mapper 注入 ServiceImpl 的 baseMapper 字段
     * 这样 IService.list()/getById()/save() 等方法才能正常工作
     */
    @SuppressWarnings("unchecked")
    private void injectBaseMapper(Object service, Object mapper) throws Exception {
        Class<?> clazz = service.getClass();
        while (clazz != null) {
            for (java.lang.reflect.Field field : clazz.getDeclaredFields()) {
                if ("baseMapper".equals(field.getName())) {
                    field.setAccessible(true);
                    // baseMapper field is typed as BaseMapper (raw), assign via set directly
                    field.set(service, mapper);
                    return;
                }
            }
            clazz = clazz.getSuperclass();
        }
        throw new IllegalStateException("baseMapper field not found in class hierarchy");
    }

    // 通用反射设置字段值工具
    private void setField(Object target, String fieldName, Object value) throws Exception {
        Class<?> clazz = target.getClass();
        while (clazz != null) {
            for (java.lang.reflect.Field f : clazz.getDeclaredFields()) {
                if (f.getName().equals(fieldName)) {
                    f.setAccessible(true);
                    f.set(target, value);
                    return;
                }
            }
            clazz = clazz.getSuperclass();
        }
        throw new IllegalStateException("Field '" + fieldName + "' not found");
    }

    // ==================== getConversationList ====================

    @Test
    @DisplayName("获取会话列表 - 用户无任何会话，返回空列表")
    void getConversationList_empty() {
        when(memberMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());

        List<ConversationVO> result = conversationService.getConversationList(USER_ID, null, 1, 20);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(memberMapper, times(1)).selectList(any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("获取会话列表 - 成功返回会话列表")
    void getConversationList_success() {
        ConversationMember member = createConversationMember(CONV_ID, USER_ID, 1);
        when(memberMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(member));

        Conversation conv = createConversation(CONV_ID, 1, "私聊");
        when(conversationMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(conv));

        List<ConversationVO> result = conversationService.getConversationList(USER_ID, null, 1, 20);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(CONV_ID, result.get(0).getId());
    }

    @Test
    @DisplayName("获取会话列表 - 按类型筛选")
    void getConversationList_filterByType() {
        ConversationMember member = createConversationMember(CONV_ID, USER_ID, 1);
        when(memberMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(member));

        Conversation conv = createConversation(CONV_ID, 2, "群聊");
        when(conversationMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(conv));

        List<ConversationVO> result = conversationService.getConversationList(USER_ID, 2, 1, 20);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(2, result.get(0).getType());
    }

    @Test
    @DisplayName("获取会话列表 - 分页正常")
    void getConversationList_pagination() {
        ConversationMember m1 = createConversationMember("conv-1", USER_ID, 1);
        ConversationMember m2 = createConversationMember("conv-2", USER_ID, 1);
        ConversationMember m3 = createConversationMember("conv-3", USER_ID, 1);
        when(memberMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of(m1, m2, m3));

        Conversation c1 = createConversation("conv-1", 1, "会话1");
        c1.setUpdateTime(LocalDateTime.now().minusHours(1));
        Conversation c2 = createConversation("conv-2", 1, "会话2");
        c2.setUpdateTime(LocalDateTime.now());
        Conversation c3 = createConversation("conv-3", 1, "会话3");
        c3.setUpdateTime(LocalDateTime.now().minusHours(2));
        when(conversationMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of(c1, c2, c3));

        List<ConversationVO> result = conversationService.getConversationList(USER_ID, null, 1, 2);

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    // ==================== getConversationById ====================

    @Test
    @DisplayName("获取会话详情 - 会话不存在")
    void getConversationById_notFound() {
        when(conversationMapper.selectById(CONV_ID)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> conversationService.getConversationById(CONV_ID, USER_ID));
        assertTrue(ex.getMessage().contains("not found"));
    }

    @Test
    @DisplayName("获取会话详情 - 用户不是成员（无权限）")
    void getConversationById_notMember() {
        Conversation conv = createConversation(CONV_ID, 1, "私聊");
        when(conversationMapper.selectById(CONV_ID)).thenReturn(conv);
        when(memberMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> conversationService.getConversationById(CONV_ID, USER_ID));
        assertTrue(ex.getMessage().contains("not a member"));
    }

    @Test
    @DisplayName("获取会话详情 - 成功返回会话详情")
    void getConversationById_success() {
        Conversation conv = createConversation(CONV_ID, 1, "私聊");
        when(conversationMapper.selectById(CONV_ID)).thenReturn(conv);

        ConversationMember member = createConversationMember(CONV_ID, USER_ID, 1);
        when(memberMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(member);
        when(memberMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(member));

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(null);

        ConversationVO result = conversationService.getConversationById(CONV_ID, USER_ID);

        assertNotNull(result);
        assertEquals(CONV_ID, result.getId());
        assertEquals(1, result.getType());
    }

    // ==================== markAsRead ====================

    @Test
    @DisplayName("标记已读 - 非成员抛出异常")
    void markAsRead_notMember() {
        when(memberMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> conversationService.markAsRead(CONV_ID, USER_ID, "msg-001"));
        assertTrue(ex.getMessage().contains("not a member"));
    }

    @Test
    @DisplayName("标记已读 - 成功")
    void markAsRead_success() {
        ConversationMember member = createConversationMember(CONV_ID, USER_ID, 1);
        when(memberMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(member);
        when(memberMapper.update(any(), any(LambdaUpdateWrapper.class))).thenReturn(1);
        when(redisTemplate.delete(anyString())).thenReturn(true);

        boolean result = conversationService.markAsRead(CONV_ID, USER_ID, "msg-001");

        assertTrue(result);
        verify(redisTemplate, times(1)).delete(contains(CONV_ID));
    }

    // ==================== createConversation ====================

    @Test
    @DisplayName("创建会话 - 私聊类型不允许从此接口创建")
    void createConversation_privateChatTypeError() {
        ConversationCreateDTO dto = new ConversationCreateDTO();
        dto.setType(1);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> conversationService.createConversation(USER_ID, dto));
        assertTrue(ex.getMessage().contains("private chat"));
    }

    @Test
    @DisplayName("创建会话 - 群聊名称必填校验")
    void createConversation_groupNameRequired() {
        ConversationCreateDTO dto = new ConversationCreateDTO();
        dto.setType(2);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> conversationService.createConversation(USER_ID, dto));
        assertTrue(ex.getMessage().contains("name"));
    }

    @Test
    @DisplayName("创建会话 - 群聊成功创建")
    void createConversation_groupSuccess() {
        ConversationCreateDTO dto = new ConversationCreateDTO();
        dto.setType(2);
        dto.setName("测试群聊");
        dto.setMemberIds(List.of(OTHER_USER_ID));

        when(conversationMapper.insert(any(Conversation.class))).thenAnswer(inv -> {
            Conversation c = inv.getArgument(0);
            if (c.getId() == null) {
                setField(c, "id", CONV_ID);
            }
            return 1;
        });
        when(memberMapper.insert(any(ConversationMember.class))).thenReturn(1);

        // getById needs id set on entity - use doReturn for lenient matching
        Conversation conv = createConversation(CONV_ID, 2, "测试群聊");
        doReturn(conv).when(conversationMapper).selectById(anyString());

        ConversationMember member = createConversationMember(CONV_ID, USER_ID, 1);
        ConversationMember otherMember = createConversationMember(CONV_ID, OTHER_USER_ID, 3);
        when(memberMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(member);
        when(memberMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of(member, otherMember));

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(null);

        ConversationVO result = conversationService.createConversation(USER_ID, dto);

        assertNotNull(result);
        assertEquals(CONV_ID, result.getId());
        assertEquals(2, result.getType());
        // 创建者 + 另一个成员
        verify(memberMapper, times(2)).insert(any(ConversationMember.class));
    }

    // ==================== getOrCreatePrivateConversation ====================

    @Test
    @DisplayName("获取或创建私聊 - 对方ID为空抛出异常")
    void getOrCreatePrivateConversation_blankOtherUserId() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> conversationService.getOrCreatePrivateConversation(USER_ID, ""));
        assertTrue(ex.getMessage().contains("required"));
    }

    @Test
    @DisplayName("获取或创建私聊 - 已存在则直接返回")
    void getOrCreatePrivateConversation_existing() {
        Conversation privateConv = createConversation(CONV_ID, 1, null);
        when(conversationMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of(privateConv));
        // getConversationById calls this.getById(id), which uses baseMapper.selectById
        doReturn(privateConv).when(conversationMapper).selectById(anyString());

        ConversationMember member = createConversationMember(CONV_ID, USER_ID, 1);
        ConversationMember otherMember = createConversationMember(CONV_ID, OTHER_USER_ID, 1);
        when(memberMapper.selectOne(any(LambdaQueryWrapper.class)))
                .thenReturn(member)
                .thenReturn(otherMember)
                .thenReturn(member);

        when(memberMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of(member, otherMember));

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(null);

        ConversationVO result = conversationService.getOrCreatePrivateConversation(USER_ID, OTHER_USER_ID);

        assertNotNull(result);
        assertEquals(CONV_ID, result.getId());
        verify(conversationMapper, never()).insert(any(Conversation.class));
    }

    @Test
    @DisplayName("获取或创建私聊 - 不存在则新建")
    void getOrCreatePrivateConversation_createNew() {
        when(conversationMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenAnswer(inv -> Collections.emptyList());
        when(conversationMapper.insert(any(Conversation.class))).thenAnswer(inv -> {
            Conversation c = inv.getArgument(0);
            if (c.getId() == null) {
                setField(c, "id", CONV_ID);
            }
            return 1;
        });
        when(memberMapper.insert(any(ConversationMember.class))).thenReturn(1);

        Conversation conv = createConversation(CONV_ID, 1, null);
        doReturn(conv).when(conversationMapper).selectById(anyString());
        ConversationMember member = createConversationMember(CONV_ID, USER_ID, 1);
        when(memberMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(member);
        when(memberMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(member));

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(null);

        ConversationVO result = conversationService.getOrCreatePrivateConversation(USER_ID, OTHER_USER_ID);

        assertNotNull(result);
        verify(conversationMapper, times(1)).insert(any(Conversation.class));
        verify(memberMapper, times(2)).insert(any(ConversationMember.class));
    }

    // ==================== deleteConversation ====================

    @Test
    @DisplayName("删除会话 - 会话不存在")
    void deleteConversation_notFound() {
        when(conversationMapper.selectById(CONV_ID)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> conversationService.deleteConversation(CONV_ID, USER_ID));
        assertTrue(ex.getMessage().contains("not found"));
    }

    @Test
    @DisplayName("删除会话 - 私聊删除后同时删除会话本身")
    void deleteConversation_privateChatDeletesConv() {
        Conversation conv = createConversation(CONV_ID, 1, null);
        doReturn(conv).when(conversationMapper).selectById(anyString());
        when(memberMapper.update(any(), any(LambdaUpdateWrapper.class))).thenReturn(1);
        when(memberMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(conversationMapper.deleteById(any())).thenReturn(1);

        boolean result = conversationService.deleteConversation(CONV_ID, USER_ID);

        assertTrue(result);
        verify(conversationMapper, times(1)).deleteById(any());
    }

    // ==================== muteConversation & topConversation ====================

    @Test
    @DisplayName("禁言会话 - 非成员抛出异常")
    void muteConversation_notMember() {
        when(memberMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> conversationService.muteConversation(CONV_ID, USER_ID, 1));
        assertTrue(ex.getMessage().contains("not a member"));
    }

    @Test
    @DisplayName("禁言会话 - 成功")
    void muteConversation_success() {
        ConversationMember member = createConversationMember(CONV_ID, USER_ID, 1);
        when(memberMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(member);
        when(memberMapper.update(any(), any(LambdaUpdateWrapper.class))).thenReturn(1);

        boolean result = conversationService.muteConversation(CONV_ID, USER_ID, 1);

        assertTrue(result);
    }

    @Test
    @DisplayName("置顶会话 - 成功")
    void topConversation_success() {
        ConversationMember member = createConversationMember(CONV_ID, USER_ID, 1);
        when(memberMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(member);
        when(memberMapper.update(any(), any(LambdaUpdateWrapper.class))).thenReturn(1);

        boolean result = conversationService.topConversation(CONV_ID, USER_ID, 1);

        assertTrue(result);
    }

    // ==================== Helper Methods ====================

    private ConversationMember createConversationMember(String conversationId, String userId, Integer role) {
        ConversationMember member = new ConversationMember();
        member.setId("member-" + conversationId + "-" + userId);
        member.setConversationId(conversationId);
        member.setUserId(userId);
        member.setRole(role);
        member.setJoinTime(LocalDateTime.now());
        member.setInviterId(userId);
        member.setUnreadCount(0);
        member.setMuteStatus(0);
        member.setTopStatus(0);
        member.setSortOrder(0);
        member.setStatus(1);
        return member;
    }

    private Conversation createConversation(String id, Integer type, String name) {
        Conversation conv = new Conversation();
        conv.setId(id);
        conv.setType(type);
        conv.setName(name);
        conv.setAvatar("http://example.com/avatar.png");
        conv.setOwnerId(USER_ID);
        conv.setStatus(1);
        conv.setUnreadCount(0);
        conv.setMuteStatus(0);
        conv.setTopStatus(0);
        conv.setArchiveStatus(0);
        conv.setMaxMembers(-1);
        conv.setCreateTime(LocalDateTime.now());
        conv.setUpdateTime(LocalDateTime.now());
        return conv;
    }
}
