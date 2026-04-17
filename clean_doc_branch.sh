#!/bin/bash
# 清理脚本：在AIOA-DOC分支中删除所有源代码，只保留文档

echo "开始清理AIOA-DOC分支，只保留文档文件..."

# 需要保留的文档目录和文件
KEEP_DIRS=(
    "docs"
    "业务分析"
    "需求设计"
    "项目管理"
    "ui-prototype"
    "AI-OA_UI原型.tar.gz"
    "README.md"
    "LICENSE"
    "deploy"           # 部署文档
    "packages"         # 部署包文档
    "scripts/deploy"   # 部署脚本说明
)

# 需要删除的源代码目录
REMOVE_DIRS=(
    "poc"
    "source"
    "node_modules"
    "target"
    ".gitignore"
    "Dockerfile"
    "docker-compose.yml"
    "*.tar.gz"  # 除了AI-OA_UI原型.tar.gz
)

# 先备份一些重要文件
echo "备份重要文件..."
mkdir -p backup_docs

# 检查当前分支
CURRENT_BRANCH=$(git branch --show-current)
echo "当前分支: $CURRENT_BRANCH"

if [ "$CURRENT_BRANCH" != "AIOA-DOC" ]; then
    echo "错误：必须在AIOA-DOC分支上执行此脚本"
    exit 1
fi

# 删除源代码目录
echo "删除源代码目录..."
rm -rf poc source node_modules target 2>/dev/null || true

# 删除不需要的tar.gz文件
echo "清理不需要的部署包文件..."
find . -maxdepth 1 -name "*.tar.gz" ! -name "AI-OA_UI原型.tar.gz" -delete

# 删除Docker相关文件
echo "删除Docker相关文件..."
rm -f Dockerfile docker-compose.yml .gitignore 2>/dev/null || true

# 清理scripts目录（只保留deploy子目录）
echo "清理scripts目录..."
mkdir -p scripts_backup
mv scripts/deploy scripts_backup/ 2>/dev/null || true
rm -rf scripts/*
mv scripts_backup/deploy scripts/ 2>/dev/null || true
rm -rf scripts_backup

# 清理deploy目录（只保留README.md文件）
echo "整理deploy目录..."
find deploy -type f ! -name "*.md" -delete 2>/dev/null || true

# 清理packages目录（只保留README.md文件）
echo "整理packages目录..."
find packages -type f ! -name "*.md" -delete 2>/dev/null || true

# 添加所有更改
git add -A
git status

echo "清理完成！现在AIOA-DOC分支只包含文档文件。"
echo "请检查git状态，确认无误后执行："
echo "  git commit -m 'docs: 整理文档分支，移除源代码'"
echo "  git push origin AIOA-DOC"