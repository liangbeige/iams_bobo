package com.iams.system.service;

import java.util.Date;
import java.util.List;
import com.iams.system.domain.SysLogininfor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 系统访问日志情况信息 服务层
 * 
 * @author ruoyi
 */
public interface ISysLogininforService
{
    /**
     * 新增系统登录日志
     * 
     * @param logininfor 访问日志对象
     */
    public void insertLogininfor(SysLogininfor logininfor);

    /**
     * 查询系统登录日志集合
     * 
     * @param logininfor 访问日志对象
     * @return 登录记录集合
     */
    public List<SysLogininfor> selectLogininforList(SysLogininfor logininfor);

    /**
     * 批量删除系统登录日志
     * 
     * @param infoIds 需要删除的登录日志ID
     * @return 结果
     */
    public int deleteLogininforByIds(Long[] infoIds);

    /**
     * 清空系统登录日志
     */
    public void cleanLogininfor();

    /**
     * 查询用户上次成功登录时间
     * @param username 用户名
     * @return 上次登录时间
     */
    Date selectLastSuccessLoginTime(String username);



    /**
     * 更新登录记录
     *
     * @param logininfor 登录记录
     * @return 结果
     */
    public int updateLogininfor(SysLogininfor logininfor);

    /**
     * 查询最近的登录记录
     *
     * @param userName 用户名
     * @param ipaddr IP地址
     * @param loginTime 登录时间
     * @return 登录记录
     */
    public SysLogininfor selectLatestLoginRecord(String userName, String ipaddr, Date loginTime);



    /**
     * 查询超时的登录会话
     *
     * @param timeoutMinutes 超时时间（分钟）
     * @return 超时会话列表
     */
    public List<SysLogininfor> selectTimeoutSessions(int timeoutMinutes);

    SysLogininfor selectLatestLoginByUsername(String userName);
}
