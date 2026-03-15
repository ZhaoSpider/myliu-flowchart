package com.myliu.service;

import com.myliu.dto.FileCreateRequest;
import com.myliu.dto.FileUpdateRequest;
import com.myliu.dto.SaveContentRequest;
import com.myliu.vo.FileVO;
import com.myliu.vo.PageVO;
import com.myliu.vo.VersionVO;

import java.util.List;

/**
 * 文件服务接口
 *
 * @author MyLiu
 * @since 1.0.0
 */
public interface FileService {

    /**
     * 获取文件列表（分页）
     *
     * @param userId  用户ID
     * @param page    页码
     * @param size    每页大小
     * @param keyword 搜索关键词
     * @return 分页结果
     */
    PageVO<FileVO> getFileList(Long userId, Integer page, Integer size, String keyword);

    /**
     * 获取文件详情
     *
     * @param id       文件ID
     * @param userId   用户ID
     * @param isAdmin  是否是管理员
     * @return 文件信息
     */
    FileVO getFileById(Long id, Long userId, boolean isAdmin);

    /**
     * 创建文件
     *
     * @param userId  用户ID
     * @param request 创建请求
     * @return 文件信息
     */
    FileVO createFile(Long userId, FileCreateRequest request);

    /**
     * 更新文件信息
     *
     * @param id      文件ID
     * @param userId  用户ID
     * @param request 更新请求
     * @return 文件信息
     */
    FileVO updateFile(Long id, Long userId, FileUpdateRequest request);

    /**
     * 删除文件
     *
     * @param id     文件ID
     * @param userId 用户ID
     */
    void deleteFile(Long id, Long userId);

    /**
     * 保存文件内容
     *
     * @param id      文件ID
     * @param userId  用户ID
     * @param request 保存请求
     */
    void saveContent(Long id, Long userId, SaveContentRequest request);

    /**
     * 获取文件内容
     *
     * @param id      文件ID
     * @param userId  用户ID
     * @param isAdmin 是否是管理员
     * @return 文件内容
     */
    String getContent(Long id, Long userId, boolean isAdmin);

    /**
     * 获取版本历史
     *
     * @param id     文件ID
     * @param userId 用户ID
     * @return 版本列表
     */
    List<VersionVO> getVersions(Long id, Long userId);

    /**
     * 导出文件
     *
     * @param id     文件ID
     * @param userId 用户ID
     * @param format 导出格式（json/png/svg）
     * @return 文件内容（JSON格式返回字符串，其他格式暂不支持）
     */
    byte[] exportFile(Long id, Long userId, String format);
}
