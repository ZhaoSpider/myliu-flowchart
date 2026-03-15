package com.myliu.controller;

import com.myliu.dto.FileCreateRequest;
import com.myliu.dto.FileUpdateRequest;
import com.myliu.dto.SaveContentRequest;
import com.myliu.exception.BusinessException;
import com.myliu.result.Result;
import com.myliu.security.UserPrincipal;
import com.myliu.service.AdminService;
import com.myliu.service.FileService;
import com.myliu.vo.FileVO;
import com.myliu.vo.PageVO;
import com.myliu.vo.VersionVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 文件管理控制器
 *
 * @author MyLiu
 * @since 1.0.0
 */
@Tag(name = "文件管理", description = "流程图文件的增删改查、内容保存、版本管理等接口")
@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;
    private final AdminService adminService;

    /**
     * 获取文件列表
     */
    @Operation(summary = "获取文件列表", description = "分页获取当前用户的文件列表")
    @GetMapping
    public Result<PageVO<FileVO>> getFileList(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword) {
        if (principal == null) {
            return Result.unauthorized();
        }
        PageVO<FileVO> result = fileService.getFileList(principal.getUserId(), page, size, keyword);
        return Result.success(result);
    }

    /**
     * 获取文件详情
     */
    @Operation(summary = "获取文件详情", description = "根据ID获取文件详细信息")
    @GetMapping("/{id}")
    public Result<FileVO> getFileById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            return Result.unauthorized();
        }
        
        // 管理员可以查看任意文件
        boolean isAdmin = adminService.isAdmin(principal.getUserId());
        FileVO file = fileService.getFileById(id, principal.getUserId(), isAdmin);
        return Result.success(file);
    }

    /**
     * 创建文件
     */
    @Operation(summary = "创建文件", description = "创建新的流程图文件")
    @PostMapping
    public Result<FileVO> createFile(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody FileCreateRequest request) {
        if (principal == null) {
            return Result.unauthorized();
        }
        FileVO file = fileService.createFile(principal.getUserId(), request);
        return Result.success("创建成功", file);
    }

    /**
     * 更新文件信息
     */
    @Operation(summary = "更新文件信息", description = "更新文件名或状态")
    @PutMapping("/{id}")
    public Result<FileVO> updateFile(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody FileUpdateRequest request) {
        if (principal == null) {
            return Result.unauthorized();
        }
        FileVO file = fileService.updateFile(id, principal.getUserId(), request);
        return Result.success("更新成功", file);
    }

    /**
     * 删除文件
     */
    @Operation(summary = "删除文件", description = "删除指定的流程图文件")
    @DeleteMapping("/{id}")
    public Result<Void> deleteFile(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            return Result.unauthorized();
        }
        fileService.deleteFile(id, principal.getUserId());
        return Result.success("删除成功", null);
    }

    /**
     * 保存文件内容
     */
    @Operation(summary = "保存文件内容", description = "保存流程图的JSON内容，并创建新版本")
    @PutMapping("/{id}/content")
    public Result<Void> saveContent(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody SaveContentRequest request) {
        if (principal == null) {
            return Result.unauthorized();
        }
        fileService.saveContent(id, principal.getUserId(), request);
        return Result.success("保存成功", null);
    }

    /**
     * 获取文件内容
     */
    @Operation(summary = "获取文件内容", description = "获取流程图的JSON内容")
    @GetMapping("/{id}/content")
    public Result<String> getContent(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            return Result.unauthorized();
        }
        
        // 管理员可以查看任意文件内容
        boolean isAdmin = adminService.isAdmin(principal.getUserId());
        String content = fileService.getContent(id, principal.getUserId(), isAdmin);
        return Result.success(content);
    }

    /**
     * 获取版本历史
     */
    @Operation(summary = "获取版本历史", description = "获取文件的所有历史版本")
    @GetMapping("/{id}/versions")
    public Result<List<VersionVO>> getVersions(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            return Result.unauthorized();
        }
        List<VersionVO> versions = fileService.getVersions(id, principal.getUserId());
        return Result.success(versions);
    }

    /**
     * 导出文件
     */
    @Operation(summary = "导出文件", description = "导出流程图文件（支持JSON格式）")
    @GetMapping("/{id}/export")
    public void exportFile(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "json") String format,
            jakarta.servlet.http.HttpServletResponse response) {
        if (principal == null) {
            response.setStatus(401);
            return;
        }
        
        try {
            byte[] data = fileService.exportFile(id, principal.getUserId(), format);
            
            // 获取文件信息用于设置文件名
            boolean isAdmin = adminService.isAdmin(principal.getUserId());
            FileVO file = fileService.getFileById(id, principal.getUserId(), isAdmin);
            String fileName = file.getFileName();
            
            // 设置响应头
            response.setContentType("application/octet-stream");
            String encodedFileName = java.net.URLEncoder.encode(fileName, "UTF-8");
            
            switch (format.toLowerCase()) {
                case "json":
                    response.setContentType("application/json");
                    response.setHeader("Content-Disposition", "attachment; filename=\"" + encodedFileName + ".json\"");
                    break;
                case "png":
                    response.setContentType("image/png");
                    response.setHeader("Content-Disposition", "attachment; filename=\"" + encodedFileName + ".png\"");
                    break;
                case "svg":
                    response.setContentType("image/svg+xml");
                    response.setHeader("Content-Disposition", "attachment; filename=\"" + encodedFileName + ".svg\"");
                    break;
            }
            
            response.setContentLength(data.length);
            response.getOutputStream().write(data);
            response.getOutputStream().flush();
        } catch (BusinessException e) {
            response.setStatus(400);
            try {
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":400,\"message\":\"" + e.getMessage() + "\"}");
            } catch (java.io.IOException ex) {
                // ignore
            }
        } catch (Exception e) {
            response.setStatus(500);
        }
    }
}
