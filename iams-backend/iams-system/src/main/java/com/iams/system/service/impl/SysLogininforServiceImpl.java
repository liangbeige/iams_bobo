package com.iams.system.service.impl;

import java.util.Date;
import java.util.List;

import com.iams.common.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.iams.system.domain.SysLogininfor;
import com.iams.system.mapper.SysLogininforMapper;
import com.iams.system.service.ISysLogininforService;

/**
 * 系统访问日志情况信息 服务层处理
 * 
 * @author ruoyi
 */
@Service
public class SysLogininforServiceImpl implements ISysLogininforService
{

    @Autowired
    private SysLogininforMapper logininforMapper;

    /**
     * 新增系统登录日志
     * 
     * @param logininfor 访问日志对象
     */
    @Override
    public void insertLogininfor(SysLogininfor logininfor)
    {
//        logininforMapper.insertLogininfor(logininfor);
        try {
            System.out.println("=== SysLogininforServiceImpl.insertLogininfor 开始 ===");
            System.out.println("插入数据:");
            System.out.println("  用户名: " + logininfor.getUserName());
            System.out.println("  IP: " + logininfor.getIpaddr());
            System.out.println("  消息: " + logininfor.getMsg());
            System.out.println("  状态: " + logininfor.getStatus());
            System.out.println("  登录时间: " + logininfor.getLoginTime());

            logininforMapper.insertLogininfor(logininfor);

            System.out.println("=== 数据库插入操作完成 ===");

        } catch (Exception e) {
            System.err.println("=== 数据库插入失败 ===");
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * 查询系统登录日志集合
     * 
     * @param logininfor 访问日志对象
     * @return 登录记录集合
     */
    @Override
    public List<SysLogininfor> selectLogininforList(SysLogininfor logininfor)
    {
        return logininforMapper.selectLogininforList(logininfor);
    }

    /**
     * 批量删除系统登录日志
     * 
     * @param infoIds 需要删除的登录日志ID
     * @return 结果
     */
    @Override
    public int deleteLogininforByIds(Long[] infoIds)
    {
        return logininforMapper.deleteLogininforByIds(infoIds);
    }

    /**
     * 清空系统登录日志
     */
    @Override
    public void cleanLogininfor()
    {
        logininforMapper.cleanLogininfor();
    }

    @Override
    public Date selectLastSuccessLoginTime(String username) {
        return logininforMapper.selectLastSuccessLoginTime(username);
    }
    /**
     * 更新登录记录
     *
     * @param logininfor 登录记录
     * @return 结果
     */
    @Override
    public int updateLogininfor(SysLogininfor logininfor)
    {
        return logininforMapper.updateLogininfor(logininfor);
    }

    /**
     * 查询最近的登录记录
     *
     * @param userName 用户名
     * @param ipaddr IP地址
     * @param loginTime 登录时间
     * @return 登录记录
     */
    @Override
    public SysLogininfor selectLatestLoginRecord(String userName, String ipaddr, Date loginTime)
    {
        return logininforMapper.selectLatestLoginRecord(userName, ipaddr, loginTime);
    }



    /**
     * 查询超时的登录会话
     *
     * @param timeoutMinutes 超时时间（分钟）
     * @return 超时会话列表
     */
    @Override
    public List<SysLogininfor> selectTimeoutSessions(int timeoutMinutes)
    {
        return logininforMapper.selectTimeoutSessions(timeoutMinutes);
    }

    /**
     * 查询用户最近的登录成功记录（未计算时长的）
     * @param userName 用户名
     * @return 登录记录
     */
    @Override
    public SysLogininfor selectLatestLoginByUsername(String userName) {
        return logininforMapper.selectLatestLoginByUsername(userName);
    }
}
