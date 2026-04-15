package com.aioa.knowledge.mapper;

import com.aioa.knowledge.entity.KnowledgeDoc;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * Knowledge Doc Mapper
 */
@Mapper
public interface KnowledgeMapper extends BaseMapper<KnowledgeDoc> {
}
