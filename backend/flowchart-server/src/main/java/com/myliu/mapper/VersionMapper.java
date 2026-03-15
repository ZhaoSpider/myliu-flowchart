package com.myliu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.myliu.entity.FlowchartVersion;
import org.apache.ibatis.annotations.Mapper;

/**
 * 版本数据访问层
 *
 * @author MyLiu
 * @since 1.0.0
 */
@Mapper
public interface VersionMapper extends BaseMapper<FlowchartVersion> {
}
