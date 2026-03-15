package com.myliu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.myliu.dto.FileCreateRequest;
import com.myliu.dto.FileUpdateRequest;
import com.myliu.dto.SaveContentRequest;
import com.myliu.entity.FlowchartFile;
import com.myliu.entity.FlowchartVersion;
import com.myliu.exception.BusinessException;
import com.myliu.mapper.FileMapper;
import com.myliu.mapper.VersionMapper;
import com.myliu.service.FileService;
import com.myliu.vo.FileVO;
import com.myliu.vo.PageVO;
import com.myliu.vo.VersionVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 文件服务实现
 *
 * @author MyLiu
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class FileServiceImpl implements FileService {

    private final FileMapper fileMapper;
    private final VersionMapper versionMapper;

    @Override
    public PageVO<FileVO> getFileList(Long userId, Integer page, Integer size, String keyword) {
        Page<FlowchartFile> pageParam = new Page<>(page, size);
        
        LambdaQueryWrapper<FlowchartFile> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FlowchartFile::getCreatorId, userId);
        
        if (StringUtils.hasText(keyword)) {
            wrapper.like(FlowchartFile::getFileName, keyword);
        }
        
        wrapper.orderByDesc(FlowchartFile::getUpdatedAt);
        
        Page<FlowchartFile> result = fileMapper.selectPage(pageParam, wrapper);
        
        List<FileVO> list = result.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        
        return PageVO.of(list, result.getTotal(), (int) result.getCurrent(), (int) result.getSize());
    }

    @Override
    public FileVO getFileById(Long id, Long userId, boolean isAdmin) {
        FlowchartFile file = fileMapper.selectById(id);
        if (file == null) {
            throw new BusinessException("文件不存在");
        }
        // 管理员可以查看任意文件
        if (!isAdmin && !file.getCreatorId().equals(userId)) {
            throw new BusinessException("无权限访问此文件");
        }
        return convertToVO(file);
    }

    @Override
    public FileVO createFile(Long userId, FileCreateRequest request) {
        log.info("创建文件 | userId: {}, fileName: {}", userId, request.getFileName());
        
        FlowchartFile file = new FlowchartFile();
        file.setFileName(request.getFileName());
        file.setCreatorId(userId);
        file.setStatus(1); // 草稿
        
        // 如果有初始内容，计算大小
        if (StringUtils.hasText(request.getContent())) {
            file.setFileSize((long) request.getContent().getBytes().length);
        } else {
            file.setFileSize(0L);
        }
        
        fileMapper.insert(file);
        log.info("文件创建成功 | fileId: {}, fileName: {}", file.getId(), file.getFileName());
        
        // 如果有初始内容，创建第一个版本
        if (StringUtils.hasText(request.getContent())) {
            createVersion(file.getId(), userId, request.getContent(), "初始版本");
        }
        
        return convertToVO(file);
    }

    @Override
    public FileVO updateFile(Long id, Long userId, FileUpdateRequest request) {
        FlowchartFile file = fileMapper.selectById(id);
        if (file == null) {
            throw new BusinessException("文件不存在");
        }
        if (!file.getCreatorId().equals(userId)) {
            throw new BusinessException("无权限修改此文件");
        }
        
        if (StringUtils.hasText(request.getFileName())) {
            file.setFileName(request.getFileName());
        }
        if (request.getStatus() != null) {
            file.setStatus(request.getStatus());
        }
        
        fileMapper.updateById(file);
        return convertToVO(file);
    }

    @Override
    public void deleteFile(Long id, Long userId) {
        log.info("删除文件 | fileId: {}, userId: {}", id, userId);
        
        FlowchartFile file = fileMapper.selectById(id);
        if (file == null) {
            log.warn("文件不存在 | fileId: {}", id);
            throw new BusinessException("文件不存在");
        }
        if (!file.getCreatorId().equals(userId)) {
            log.warn("无权限删除文件 | fileId: {}, userId: {}, creatorId: {}", id, userId, file.getCreatorId());
            throw new BusinessException("无权限删除此文件");
        }
        
        fileMapper.deleteById(id);
        log.info("文件删除成功 | fileId: {}, fileName: {}", id, file.getFileName());
    }

    @Override
    public void saveContent(Long id, Long userId, SaveContentRequest request) {
        log.info("保存文件内容 | fileId: {}, userId: {}, contentSize: {}bytes", 
                id, userId, request.getContent().getBytes().length);
        
        FlowchartFile file = fileMapper.selectById(id);
        if (file == null) {
            log.warn("文件不存在 | fileId: {}", id);
            throw new BusinessException("文件不存在");
        }
        if (!file.getCreatorId().equals(userId)) {
            log.warn("无权限修改文件 | fileId: {}, userId: {}, creatorId: {}", id, userId, file.getCreatorId());
            throw new BusinessException("无权限修改此文件");
        }
        
        // 更新文件大小
        file.setFileSize((long) request.getContent().getBytes().length);
        fileMapper.updateById(file);
        
        // 创建新版本
        String changeLog = StringUtils.hasText(request.getChangeLog()) 
                ? request.getChangeLog() 
                : "自动保存";
        createVersion(id, userId, request.getContent(), changeLog);
        log.info("文件内容保存成功 | fileId: {}, fileName: {}, version: {}", id, file.getFileName(), changeLog);
    }

    @Override
    public String getContent(Long id, Long userId, boolean isAdmin) {
        FlowchartFile file = fileMapper.selectById(id);
        if (file == null) {
            throw new BusinessException("文件不存在");
        }
        // 管理员可以查看任意文件内容
        if (!isAdmin && !file.getCreatorId().equals(userId)) {
            throw new BusinessException("无权限访问此文件");
        }
        
        // 获取最新版本的内容
        FlowchartVersion version = versionMapper.selectOne(
                new LambdaQueryWrapper<FlowchartVersion>()
                        .eq(FlowchartVersion::getFileId, id)
                        .orderByDesc(FlowchartVersion::getCreatedAt)
                        .last("LIMIT 1")
        );
        
        return version != null ? version.getContent() : null;
    }

    @Override
    public List<VersionVO> getVersions(Long id, Long userId) {
        FlowchartFile file = fileMapper.selectById(id);
        if (file == null) {
            throw new BusinessException("文件不存在");
        }
        if (!file.getCreatorId().equals(userId)) {
            throw new BusinessException("无权限访问此文件");
        }
        
        List<FlowchartVersion> versions = versionMapper.selectList(
                new LambdaQueryWrapper<FlowchartVersion>()
                        .eq(FlowchartVersion::getFileId, id)
                        .orderByDesc(FlowchartVersion::getCreatedAt)
        );
        
        return versions.stream()
                .map(this::convertToVersionVO)
                .collect(Collectors.toList());
    }

    /**
     * 创建新版本
     */
    private void createVersion(Long fileId, Long userId, String content, String changeLog) {
        // 获取当前版本数量，生成版本号
        Long count = versionMapper.selectCount(
                new LambdaQueryWrapper<FlowchartVersion>()
                        .eq(FlowchartVersion::getFileId, fileId)
        );
        
        String versionNo = "v" + (count + 1) + ".0";
        
        FlowchartVersion version = new FlowchartVersion();
        version.setFileId(fileId);
        version.setVersionNo(versionNo);
        version.setContent(content);
        version.setChangeLog(changeLog);
        version.setCreatorId(userId);
        
        versionMapper.insert(version);
    }

    /**
     * 实体转VO
     */
    private FileVO convertToVO(FlowchartFile file) {
        FileVO vo = new FileVO();
        BeanUtils.copyProperties(file, vo);
        return vo;
    }

    /**
     * 版本实体转VO
     */
    private VersionVO convertToVersionVO(FlowchartVersion version) {
        VersionVO vo = new VersionVO();
        BeanUtils.copyProperties(version, vo);
        return vo;
    }

    @Override
    public byte[] exportFile(Long id, Long userId, String format) {
        // 验证权限
        FlowchartFile file = fileMapper.selectById(id);
        if (file == null) {
            throw new BusinessException("文件不存在");
        }
        if (!file.getCreatorId().equals(userId)) {
            throw new BusinessException("无权限访问此文件");
        }
        
        // 获取最新版本的内容
        String content = getContent(id, userId, false);
        if (content == null) {
            throw new BusinessException("文件内容为空");
        }
        
        // 根据格式导出
        switch (format.toLowerCase()) {
            case "json":
                return content.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            case "png":
            case "svg":
                // PNG/SVG 需要前端渲染，后端暂不支持
                throw new BusinessException("PNG/SVG 格式导出请在前端编辑器中进行");
            default:
                throw new BusinessException("不支持的导出格式: " + format);
        }
    }
}
