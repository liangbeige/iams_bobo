package com.iams.framework.security.handle;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import com.iams.common.utils.ip.AddressUtils;
import com.iams.common.utils.ip.IpUtils;
import com.iams.system.domain.SysLogininfor;
import com.iams.system.service.ISysLogininforService;
import eu.bitwalker.useragentutils.UserAgent;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import com.alibaba.fastjson2.JSON;
import com.iams.common.constant.Constants;
import com.iams.common.core.domain.AjaxResult;
import com.iams.common.core.domain.model.LoginUser;
import com.iams.common.utils.MessageUtils;
import com.iams.common.utils.ServletUtils;
import com.iams.common.utils.StringUtils;
import com.iams.framework.manager.AsyncManager;
import com.iams.framework.manager.factory.AsyncFactory;
import com.iams.framework.web.service.TokenService;

/**
 * 自定义退出处理类 返回成功
 * 
 * @author ruoyi
 */
@Configuration
public class LogoutSuccessHandlerImpl implements LogoutSuccessHandler
{
    @Autowired
    private TokenService tokenService;


    @Autowired
    private ISysLogininforService logininforService;

//    /**
//     * 退出处理
//     *
//     * @return
//     */
//    @Override
//    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
//            throws IOException, ServletException
//    {
//        LoginUser loginUser = tokenService.getLoginUser(request);
//        if (StringUtils.isNotNull(loginUser))
//        {
//            String userName = loginUser.getUsername();
//            // 删除用户缓存记录
//            tokenService.delLoginUser(loginUser.getToken());
//            // 记录用户退出日志
//            AsyncManager.me().execute(AsyncFactory.recordLogininfor(userName, Constants.LOGOUT, MessageUtils.message("user.logout.success")));
//        }
//        ServletUtils.renderString(response, JSON.toJSONString(AjaxResult.success(MessageUtils.message("user.logout.success"))));
//    }
    /**
     * 退出处理
     *
     * @return
     */
    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException
    {
        System.out.println("=== LogoutSuccessHandlerImpl 开始处理退出 ===");

        LoginUser loginUser = tokenService.getLoginUser(request);
        if (StringUtils.isNotNull(loginUser))
        {
            String userName = loginUser.getUsername();
            String token = loginUser.getToken();

            System.out.println("用户名: " + userName);
            System.out.println("开始处理登录时长计算和记录...");

            try {
                // 处理登录时长计算和退出记录
                handleLogoutDuration(request, userName);
            } catch (Exception e) {
                System.err.println("处理登录时长失败: " + e.getMessage());
                e.printStackTrace();
            }

            // 删除用户缓存记录
            System.out.println("删除用户缓存...");
            tokenService.delLoginUser(token);
            System.out.println("用户缓存删除完成");
        }
        else
        {
            System.out.println("LoginUser为空，无法处理退出");
        }

        System.out.println("=== LogoutSuccessHandlerImpl 处理完成 ===");
        ServletUtils.renderString(response, JSON.toJSONString(AjaxResult.success(MessageUtils.message("user.logout.success"))));
    }


    /**
     * 处理登录时长计算和退出记录
     */
    private void handleLogoutDuration(HttpServletRequest request, String userName) {
        try {
            Date logoutTime = new Date();
            String ip = IpUtils.getIpAddr(request);

            System.out.println("=== 开始计算登录时长 ===");
            System.out.println("查找用户: " + userName + " 的最近登录记录");

            // 1. 查找该用户最近的登录成功记录
            SysLogininfor loginRecord = findLatestLoginRecord(userName);

            long loginDuration = 0;
            if (loginRecord != null && loginRecord.getLoginTime() != null) {
                Date loginTime = loginRecord.getLoginTime();
                // 计算登录时长（秒）
                loginDuration = (logoutTime.getTime() - loginTime.getTime()) / 1000;

                System.out.println("找到登录记录:");
                System.out.println("  登录时间: " + loginTime);
                System.out.println("  退出时间: " + logoutTime);
                System.out.println("  登录时长: " + loginDuration + " 秒");

                // 2. 更新登录记录的登录时长
                loginRecord.setLoginDuration(loginDuration);
                int updateResult = logininforService.updateLogininfor(loginRecord);
                System.out.println("更新登录记录结果: " + updateResult);
            } else {
                System.out.println("未找到对应的登录记录");
            }

            // 3. 插入退出记录
            insertLogoutRecord(request, userName, logoutTime, loginDuration);

        } catch (Exception e) {
            System.err.println("处理登录时长异常: " + e.getMessage());
            e.printStackTrace();
        }
    }


    /**
     * 查找用户最近的登录成功记录
     */
    private SysLogininfor findLatestLoginRecord(String userName) {
        try {
            System.out.println("=== 开始查找最近未计算时长的登录记录 ===");
            System.out.println("用户名: " + userName);

            SysLogininfor loginRecord = logininforService.selectLatestLoginByUsername(userName);

            if (loginRecord != null) {
                System.out.println("找到最近登录记录:");
                System.out.println("  记录ID: " + loginRecord.getInfoId());
                System.out.println("  登录时间: " + loginRecord.getLoginTime());
                System.out.println("  当前登录时长: " + loginRecord.getLoginDuration());
                return loginRecord;
            } else {
                System.out.println("未找到未计算时长的登录记录");
                return null;
            }
        } catch (Exception e) {
            System.err.println("查找登录记录异常: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 插入退出记录
     */
    private void insertLogoutRecord(HttpServletRequest request, String userName, Date logoutTime, long loginDuration) {
        try {
            System.out.println("=== 开始插入退出记录 ===");

            String ip = IpUtils.getIpAddr(request);
            String address = AddressUtils.getRealAddressByIP(ip);

            // 获取用户代理信息
            UserAgent userAgent = UserAgent.parseUserAgentString(request.getHeader("User-Agent"));
            String os = userAgent.getOperatingSystem().getName();
            String browser = userAgent.getBrowser().getName() + " " + userAgent.getBrowserVersion().getVersion();

            // 创建退出记录
            SysLogininfor logoutRecord = new SysLogininfor();
            logoutRecord.setUserName(userName);
            logoutRecord.setIpaddr(ip);
            logoutRecord.setLoginLocation(address);
            logoutRecord.setBrowser(browser);
            logoutRecord.setOs(os);
            logoutRecord.setStatus("0"); // 成功
            logoutRecord.setMsg("退出成功");
            logoutRecord.setLoginTime(logoutTime); // 这里实际是退出时间
            logoutRecord.setLoginDuration(loginDuration); // 设置登录时长

            System.out.println("插入退出记录:");
            System.out.println("  用户名: " + userName);
            System.out.println("  IP: " + ip);
            System.out.println("  消息: 退出成功");
            System.out.println("  退出时间: " + logoutTime);
            System.out.println("  登录时长: " + loginDuration + " 秒");

            // 插入数据库
            logininforService.insertLogininfor(logoutRecord);
            System.out.println("退出记录插入完成");

        } catch (Exception e) {
            System.err.println("插入退出记录异常: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
