package com.aioa.aichat.repository;

import com.aioa.aichat.entity.PromptTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 提示词模板Repository
 */
@Repository
public interface PromptTemplateRepository extends JpaRepository<PromptTemplate, Long> {

    Optional<PromptTemplate> findByCode(String code);

    List<PromptTemplate> findByType(String type);

    List<PromptTemplate> findByStatus(Integer status);

    boolean existsByCode(String code);
}
