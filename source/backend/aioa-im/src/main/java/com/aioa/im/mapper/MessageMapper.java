package com.aioa.im.mapper;

import com.aioa.im.entity.Message;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * Message Mapper
 */
@Mapper
public interface MessageMapper extends BaseMapper<Message> {
}
