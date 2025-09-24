package com.iams.manage.mapper;

import com.github.pagehelper.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;


@Mapper
public interface UserPostMapper {
    /**
     * 根据用户ID查询岗位编码列表
     * @param userId 用户ID
     * @return 岗位编码列表（如 ["user", "auditor"]）
     */
    @Select("SELECT p.post_code FROM sys_post p " +
            "JOIN sys_user_post up ON p.post_id = up.post_id " +
            "WHERE up.user_id = #{userId}")
    Page<String> selectPostCodesByUserId(Long userId);
}
