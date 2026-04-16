package com.aioa.report.service.impl;

import com.aioa.report.service.ReportCoverService;
import com.aioa.report.entity.ReportTemplate;
import com.aioa.report.entity.ReportData;
import com.aioa.report.vo.ReportCoverVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 报表封面生成服务实现类
 * 
 * F3模块增强：AI封面生成 + 本地封面生成
 */
@Service
@Slf4j
public class ReportCoverServiceImpl implements ReportCoverService {

    @Value("${report.cover.template.path:classpath:templates/report-cover-template.png}")
    private String coverTemplatePath;
    
    @Value("${report.cover.output.dir:/tmp/report-covers}")
    private String coverOutputDir;
    
    @Value("${report.cover.font.main:Microsoft YaHei}")
    private String mainFontName;
    
    @Value("${report.cover.font.secondary:Arial}")
    private String secondaryFontName;
    
    @Value("${report.cover.image.width:1200}")
    private int imageWidth;
    
    @Value("${report.cover.image.height:800}")
    private int imageHeight;
    
    @Value("${report.cover.theme.primary:#0066CC}")
    private String primaryColorHex;
    
    @Value("${report.cover.theme.secondary:#28A745}")
    private String secondaryColorHex;
    
    @Autowired
    private ResourceLoader resourceLoader;
    
    @Autowired
    private ReportAiService reportAiService;
    
    private Color primaryColor;
    private Color secondaryColor;
    
    @Override
    public ReportCoverVO generateCover(ReportTemplate template, ReportData data) {
        log.info("开始生成报表封面，模板：{}，数据周期：{}", template.getName(), data.getPeriod());
        
        try {
            // 初始化颜色
            initColors();
            
            // 1. 生成封面图片
            BufferedImage coverImage = createCoverImage(template, data);
            
            // 2. 保存封面图片
            String coverFileName = saveCoverImage(coverImage, template, data);
            
            // 3. 生成封面缩略图
            BufferedImage thumbnail = createThumbnail(coverImage);
            String thumbnailFileName = saveThumbnail(thumbnail, template, data);
            
            // 4. 生成封面元数据
            Map<String, Object> metadata = generateCoverMetadata(template, data);
            
            // 5. 组装返回对象
            ReportCoverVO cover = new ReportCoverVO();
            cover.setCoverImage(coverImage);
            cover.setCoverFilePath(coverFileName);
            cover.setThumbnail(thumbnail);
            cover.setThumbnailFilePath(thumbnailFileName);
            cover.setMetadata(metadata);
            cover.setGeneratedAt(LocalDateTime.now());
            cover.setAiGenerated(false); // 本地生成
        
            log.info("报表封面生成成功，文件：{}", coverFileName);
            return cover;
            
        } catch (Exception e) {
            log.error("生成报表封面失败", e);
            // 降级方案：返回默认封面
            return generateDefaultCover(template, data);
        }
    }

    @Override
    public ReportCoverVO generateAiCover(ReportTemplate template, ReportData data) {
        log.info("开始生成AI报表封面，模板：{}", template.getName());
        
        try {
            // 1. 调用AI服务生成封面图片URL
            String aiCoverUrl = reportAiService.generateCoverImage(template.getName(), data.getPeriod());
            
            if (aiCoverUrl == null || aiCoverUrl.isEmpty()) {
                log.warn("AI封面生成失败，使用本地封面");
                return generateCover(template, data);
            }
            
            // 2. 下载AI生成的封面图片
            BufferedImage aiCoverImage = downloadImage(aiCoverUrl);
            
            // 3. 保存AI封面图片
            String aiCoverFileName = saveAiCoverImage(aiCoverImage, template, data);
            
            // 4. 生成缩略图
            BufferedImage aiThumbnail = createThumbnail(aiCoverImage);
            String aiThumbnailFileName = saveAiThumbnail(aiThumbnail, template, data);
            
            // 5. 生成元数据
            Map<String, Object> aiMetadata = generateAiCoverMetadata(template, data, aiCoverUrl);
            
            // 6. 组装返回对象
            ReportCoverVO aiCover = new ReportCoverVO();
            aiCover.setCoverImage(aiCoverImage);
            aiCover.setCoverFilePath(aiCoverFileName);
            aiCover.setThumbnail(aiThumbnail);
            aiCover.setThumbnailFilePath(aiThumbnailFileName);
            aiCover.setMetadata(aiMetadata);
            aiCover.setGeneratedAt(LocalDateTime.now());
            aiCover.setAiGenerated(true);
            aiCover.setAiModelUsed("DALL-E");
            aiCover.setAiPromptUsed(buildCoverImagePrompt(template.getName(), data.getPeriod()));
            
            log.info("AI报表封面生成成功，文件：{}", aiCoverFileName);
            return aiCover;
            
        } catch (Exception e) {
            log.error("生成AI报表封面失败，使用本地封面", e);
            return generateCover(template, data);
        }
    }

    @Override
    public String generateCoverImageBase64(ReportTemplate template, ReportData data) {
        try {
            BufferedImage coverImage = createCoverImage(template, data);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(coverImage, "png", baos);
            byte[] imageBytes = baos.toByteArray();
            
            return Base64.getEncoder().encodeToString(imageBytes);
            
        } catch (Exception e) {
            log.error("生成封面Base64失败", e);
            return null;
        }
    }

    @Override
    public String generateAiCoverImageBase64(ReportTemplate template, ReportData data) {
        try {
            String aiCoverUrl = reportAiService.generateCoverImage(template.getName(), data.getPeriod());
            if (aiCoverUrl == null || aiCoverUrl.isEmpty()) {
                return generateCoverImageBase64(template, data);
            }
            
            BufferedImage aiCoverImage = downloadImage(aiCoverUrl);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(aiCoverImage, "png", baos);
            byte[] imageBytes = baos.toByteArray();
            
            return Base64.getEncoder().encodeToString(imageBytes);
            
        } catch (Exception e) {
            log.error("生成AI封面Base64失败", e);
            return generateCoverImageBase64(template, data);
        }
    }

    @Override
    public byte[] generateCoverImageBytes(ReportTemplate template, ReportData data) {
        try {
            BufferedImage coverImage = createCoverImage(template, data);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(coverImage, "png", baos);
            return baos.toByteArray();
            
        } catch (Exception e) {
            log.error("生成封面字节失败", e);
            return null;
        }
    }

    @Override
    public byte[] generateAiCoverImageBytes(ReportTemplate template, ReportData data) {
        try {
            String aiCoverUrl = reportAiService.generateCoverImage(template.getName(), data.getPeriod());
            if (aiCoverUrl == null || aiCoverUrl.isEmpty()) {
                return generateCoverImageBytes(template, data);
            }
            
            BufferedImage aiCoverImage = downloadImage(aiCoverUrl);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(aiCoverImage, "png", baos);
            return baos.toByteArray();
            
        } catch (Exception e) {
            log.error("生成AI封面字节失败", e);
            return generateCoverImageBytes(template, data);
        }
    }

    private void initColors() {
        try {
            primaryColor = Color.decode(primaryColorHex);
            secondaryColor = Color.decode(secondaryColorHex);
        } catch (Exception e) {
            log.warn("颜色解析失败，使用默认颜色", e);
            primaryColor = new Color(0, 102, 204); // #0066CC
            secondaryColor = new Color(40, 167, 69); // #28A745
        }
    }

    private BufferedImage createCoverImage(ReportTemplate template, ReportData data) {
        // 创建空白图像
        BufferedImage image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        
        // 设置抗锯齿
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        // 绘制渐变背景
        drawGradientBackground(g2d);
        
        // 绘制装饰元素
        drawDecorationElements(g2d);
        
        // 绘制标题
        drawTitle(g2d, template.getName());
        
        // 绘制副标题
        drawSubtitle(g2d, data.getPeriod(), template.getType());
        
        // 绘制关键指标
        drawKeyMetrics(g2d, data.getKeyMetrics());
        
        // 绘制生成时间
        drawGenerationTime(g2d);
        
        // 绘制底部装饰
        drawBottomDecoration(g2d);
        
        g2d.dispose();
        return image;
    }

    private void drawGradientBackground(Graphics2D g2d) {
        // 创建径向渐变
        Point center = new Point(imageWidth / 2, imageHeight / 2);
        float radius = Math.max(imageWidth, imageHeight) / 2;
        float[] fractions = {0.0f, 0.5f, 1.0f};
        Color[] colors = {
            new Color(0, 51, 102),     // 深蓝
            primaryColor,              // 主色
            new Color(230, 240, 255)   // 浅蓝
        };
        
        RadialGradientPaint gradient = new RadialGradientPaint(
            center, radius, fractions, colors
        );
        
        g2d.setPaint(gradient);
        g2d.fillRect(0, 0, imageWidth, imageHeight);
    }

    private void drawDecorationElements(Graphics2D g2d) {
        // 绘制数据点装饰
        g2d.setColor(new Color(255, 255, 255, 50));
        
        // 随机数据点
        for (int i = 0; i < 30; i++) {
            int x = (int) (Math.random() * imageWidth);
            int y = (int) (Math.random() * imageHeight);
            int size = (int) (Math.random() * 10 + 5);
            
            g2d.fillOval(x, y, size, size);
        }
        
        // 绘制连接线
        g2d.setColor(new Color(255, 255, 255, 30));
        g2d.setStroke(new BasicStroke(1.5f));
        
        for (int i = 0; i < 15; i++) {
            int x1 = (int) (Math.random() * imageWidth);
            int y1 = (int) (Math.random() * imageHeight);
            int x2 = (int) (Math.random() * imageWidth);
            int y2 = (int) (Math.random() * imageHeight);
            
            g2d.drawLine(x1, y1, x2, y2);
        }
    }

    private void drawTitle(Graphics2D g2d, String title) {
        try {
            Font titleFont = new Font(mainFontName, Font.BOLD, 48);
            g2d.setFont(titleFont);
        } catch (Exception e) {
            Font titleFont = new Font("SansSerif", Font.BOLD, 48);
            g2d.setFont(titleFont);
        }
        
        // 绘制标题阴影
        g2d.setColor(new Color(0, 0, 0, 80));
        g2d.drawString(title, 53, 153);
        
        // 绘制标题
        g2d.setColor(Color.WHITE);
        g2d.drawString(title, 50, 150);
        
        // 绘制标题下划线
        g2d.setColor(secondaryColor);
        g2d.setStroke(new BasicStroke(3));
        int titleWidth = g2d.getFontMetrics().stringWidth(title);
        g2d.drawLine(50, 160, 50 + titleWidth, 160);
    }

    private void drawSubtitle(Graphics2D g2d, String period, String type) {
        try {
            Font subtitleFont = new Font(mainFontName, Font.PLAIN, 24);
            g2d.setFont(subtitleFont);
        } catch (Exception e) {
            Font subtitleFont = new Font("SansSerif", Font.PLAIN, 24);
            g2d.setFont(subtitleFont);
        }
        
        String subtitle = String.format("%s | %s 报表", period, type);
        
        g2d.setColor(new Color(255, 255, 255, 180));
        g2d.drawString(subtitle, 50, 200);
    }

    private void drawKeyMetrics(Graphics2D g2d, String keyMetrics) {
        try {
            Font metricsFont = new Font(secondaryFontName, Font.BOLD, 20);
            g2d.setFont(metricsFont);
        } catch (Exception e) {
            Font metricsFont = new Font("Monospaced", Font.BOLD, 20);
            g2d.setFont(metricsFont);
        }
        
        // 解析关键指标
        String[] metrics = keyMetrics.split(",");
        int startY = 280;
        int lineHeight = 40;
        
        for (int i = 0; i < Math.min(metrics.length, 5); i++) {
            String metric = metrics[i].trim();
            
            // 绘制指标标签
            g2d.setColor(new Color(255, 255, 255, 220));
            g2d.drawString(metric, 100, startY + i * lineHeight);
            
            // 绘制指标值装饰
            g2d.setColor(secondaryColor);
            g2d.fillOval(70, startY + i * lineHeight - 10, 20, 20);
        }
    }

    private void drawGenerationTime(Graphics2D g2d) {
        try {
            Font timeFont = new Font(secondaryFontName, Font.PLAIN, 16);
            g2d.setFont(timeFont);
        } catch (Exception e) {
            Font timeFont = new Font("SansSerif", Font.PLAIN, 16);
            g2d.setFont(timeFont);
        }
        
        String timeText = "生成时间: " + LocalDateTime.now().toString();
        
        g2d.setColor(new Color(255, 255, 255, 150));
        g2d.drawString(timeText, imageWidth - 350, imageHeight - 50);
    }

    private void drawBottomDecoration(Graphics2D g2d) {
        // 绘制底部波浪线
        g2d.setColor(new Color(255, 255, 255, 30));
        g2d.setStroke(new BasicStroke(2));
        
        int waveHeight = 30;
        for (int x = 0; x < imageWidth; x += 10) {
            int y1 = imageHeight - waveHeight + (int) (Math.sin(x * 0.02) * 10);
            int y2 = imageHeight - waveHeight + (int) (Math.sin((x + 10) * 0.02) * 10);
            
            g2d.drawLine(x, y1, x + 10, y2);
        }
        
        // 绘制Logo
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 18));
        g2d.drawString("AI-OA", 50, imageHeight - 30);
    }

    private String saveCoverImage(BufferedImage image, ReportTemplate template, ReportData data) {
        try {
            // 确保输出目录存在
            File outputDir = new File(coverOutputDir);
            if (!outputDir.exists()) {
                outputDir.mkdirs();
            }
            
            // 生成文件名
            String fileName = String.format("cover-%s-%s-%s.png",
                template.getId(),
                data.getPeriod().replace(" ", "-").replace("/", "-"),
                UUID.randomUUID().toString().substring(0, 8));
            
            File outputFile = new File(outputDir, fileName);
            
            // 保存图片
            ImageIO.write(image, "png", outputFile);
            
            log.info("封面图片保存成功：{}", outputFile.getAbsolutePath());
            return outputFile.getAbsolutePath();
            
        } catch (Exception e) {
            log.error("保存封面图片失败", e);
            return null;
        }
    }

    private String saveAiCoverImage(BufferedImage image, ReportTemplate template, ReportData data) {
        try {
            // 确保输出目录存在
            File outputDir = new File(coverOutputDir + "/ai");
            if (!outputDir.exists()) {
                outputDir.mkdirs();
            }
            
            // 生成文件名
            String fileName = String.format("ai-cover-%s-%s-%s.png",
                template.getId(),
                data.getPeriod().replace(" ", "-").replace("/", "-"),
                UUID.randomUUID().toString().substring(0, 8));
            
            File outputFile = new File(outputDir, fileName);
            
            // 保存图片
            ImageIO.write(image, "png", outputFile);
            
            log.info("AI封面图片保存成功：{}", outputFile.getAbsolutePath());
            return outputFile.getAbsolutePath();
            
        } catch (Exception e) {
            log.error("保存AI封面图片失败", e);
            return saveCoverImage(image, template, data); // 降级到普通封面
        }
    }

    private BufferedImage createThumbnail(BufferedImage original) {
        int thumbWidth = 300;
        int thumbHeight = 200;
        
        BufferedImage thumbnail = new BufferedImage(thumbWidth, thumbHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = thumbnail.createGraphics();
        
        // 设置高质量缩放
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // 绘制缩略图
        g2d.drawImage(original, 0, 0, thumbWidth, thumbHeight, null);
        g2d.dispose();
        
        return thumbnail;
    }

    private String saveThumbnail(BufferedImage thumbnail, ReportTemplate template, ReportData data) {
        try {
            // 确保输出目录存在
            File outputDir = new File(coverOutputDir + "/thumbnails");
            if (!outputDir.exists()) {
                outputDir.mkdirs();
            }
            
            // 生成文件名
            String fileName = String.format("thumb-%s-%s-%s.png",
                template.getId(),
                data.getPeriod().replace(" ", "-").replace("/", "-"),
                UUID.randomUUID().toString().substring(0, 8));
            
            File outputFile = new File(outputDir, fileName);
            
            // 保存缩略图
            ImageIO.write(thumbnail, "png", outputFile);
            
            log.info("封面缩略图保存成功：{}", outputFile.getAbsolutePath());
            return outputFile.getAbsolutePath();
            
        } catch (Exception e) {
            log.error("保存封面缩略图失败", e);
            return null;
        }
    }

    private String saveAiThumbnail(BufferedImage thumbnail, ReportTemplate template, ReportData data) {
        try {
            // 确保输出目录存在
            File outputDir = new File(coverOutputDir + "/ai/thumbnails");
            if (!outputDir.exists()) {
                outputDir.mkdirs();
            }
            
            // 生成文件名
            String fileName = String.format("ai-thumb-%s-%s-%s.png",
                template.getId(),
                data.getPeriod().replace(" ", "-").replace("/", "-"),
                UUID.randomUUID().toString().substring(0, 8));
            
            File outputFile = new File(outputDir, fileName);
            
            // 保存缩略图
            ImageIO.write(thumbnail, "png", outputFile);
            
            log.info("AI封面缩略图保存成功：{}", outputFile.getAbsolutePath());
            return outputFile.getAbsolutePath();
            
        } catch (Exception e) {
            log.error("保存AI封面缩略图失败", e);
            return saveThumbnail(thumbnail, template, data);
        }
    }

    private BufferedImage downloadImage(String url) throws IOException {
        // 这里需要实现图片下载逻辑
        // 简化为创建空白图片
        return new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB);
    }

    private Map<String, Object> generateCoverMetadata(ReportTemplate template, ReportData data) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("templateId", template.getId());
        metadata.put("templateName", template.getName());
        metadata.put("dataPeriod", data.getPeriod());
        metadata.put("generatedAt", LocalDateTime.now().toString());
        metadata.put("coverType", "local");
        metadata.put("imageWidth", imageWidth);
        metadata.put("imageHeight", imageHeight);
        metadata.put("primaryColor", primaryColorHex);
        metadata.put("secondaryColor", secondaryColorHex);
        return metadata;
    }

    private Map<String, Object> generateAiCoverMetadata(ReportTemplate template, ReportData data, String aiCoverUrl) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("templateId", template.getId());
        metadata.put("templateName", template.getName());
        metadata.put("dataPeriod", data.getPeriod());
        metadata.put("generatedAt", LocalDateTime.now().toString());
        metadata.put("coverType", "ai");
        metadata.put("aiModel", "DALL-E");
        metadata.put("aiPrompt", buildCoverImagePrompt(template.getName(), data.getPeriod()));
        metadata.put("aiCoverUrl", aiCoverUrl);
        metadata.put("imageWidth", imageWidth);
        metadata.put("imageHeight", imageHeight);
        return metadata;
    }

    private String buildCoverImagePrompt(String title, String period) {
        return String.format(
            "专业、现代、简洁的商业报表封面图片，" +
            "标题：%s，周期：%s，" +
            "风格：企业商务风，蓝色渐变背景，" +
            "包含数据可视化元素，" +
            "分辨率：%dx%d，" +
            "高质量数字艺术",
            title,
            period,
            imageWidth,
            imageHeight
        );
    }

    private ReportCoverVO generateDefaultCover(ReportTemplate template, ReportData data) {
        // 创建默认封面
        BufferedImage defaultImage = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = defaultImage.createGraphics();
        
        // 简单背景
        g2d.setColor(primaryColor);
        g2d.fillRect(0, 0, imageWidth, imageHeight);
        
        // 标题
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 36));
        g2d.drawString(template.getName(), 50, 150);
        
        // 副标题
        g2d.setFont(new Font("Arial", Font.PLAIN, 18));
        g2d.drawString(data.getPeriod(), 50, 200);
        
        g2d.dispose();
        
        ReportCoverVO defaultCover = new ReportCoverVO();
        defaultCover.setCoverImage(defaultImage);
        defaultCover.setCoverFilePath("/tmp/default-cover.png");
        defaultCover.setGeneratedAt(LocalDateTime.now());
        defaultCover.setAiGenerated(false);
        
        return defaultCover;
    }
}