package com.iams.activiti8.mapper;

import com.iams.activiti8.domain.vo.ActReDeploymentVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;


public interface ActReDeploymentMapper {
        public List<ActReDeploymentVO> selectActReDeploymentByIds(@Param("ids") Set<String> ids);
}
