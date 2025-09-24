package com.iams.web.controller.monitor;

import java.util.*;

import com.iams.system.domain.SysLogininfor;
import com.iams.system.service.ISysLogininforService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.iams.common.annotation.Log;
import com.iams.common.constant.CacheConstants;
import com.iams.common.core.controller.BaseController;
import com.iams.common.core.domain.AjaxResult;
import com.iams.common.core.domain.model.LoginUser;
import com.iams.common.core.page.TableDataInfo;
import com.iams.common.core.redis.RedisCache;
import com.iams.common.enums.BusinessType;
import com.iams.common.utils.StringUtils;
import com.iams.system.domain.SysUserOnline;
import com.iams.system.service.ISysUserOnlineService;

import static com.iams.framework.datasource.DynamicDataSourceContextHolder.log;

/**
 * 在线用户监控
 * 
 * @author ruoyi
 */
@RestController
@RequestMapping("/monitor/online")
public class SysUserOnlineController extends BaseController
{
    @Autowired
    private ISysUserOnlineService userOnlineService;

    @Autowired
    private RedisCache redisCache;


    @Autowired
    private ISysLogininforService logininforService;

//    @PreAuthorize("@ss.hasPermi('monitor:online:list')")
    @GetMapping("/list")
    public TableDataInfo list(String ipaddr, String userName)
    {

        Collection<String> keys = redisCache.keys(CacheConstants.LOGIN_TOKEN_KEY + "*");
        List<SysUserOnline> userOnlineList = new ArrayList<SysUserOnline>();
        for (String key : keys)
        {
            LoginUser user = redisCache.getCacheObject(key);
            SysUserOnline userOnline = null;

            if (StringUtils.isNotEmpty(ipaddr) && StringUtils.isNotEmpty(userName))
            {
                userOnline = userOnlineService.selectOnlineByInfo(ipaddr, userName, user);
            }
            else if (StringUtils.isNotEmpty(ipaddr))
            {
                userOnline = userOnlineService.selectOnlineByIpaddr(ipaddr, user);
            }
            else if (StringUtils.isNotEmpty(userName) && StringUtils.isNotNull(user.getUser()))
            {
                userOnline = userOnlineService.selectOnlineByUserName(userName, user);
            }
            else
            {
                userOnline = userOnlineService.loginUserToUserOnline(user);
            }

            // 计算实时登录时长
            if (userOnline != null && userOnline.getLoginTime() != null) {
                // 因为loginTime是Long类型（时间戳），直接用于计算
                long loginDuration = (System.currentTimeMillis() - userOnline.getLoginTime()) / 1000;
                userOnline.setLoginDuration(loginDuration);
            }

            userOnlineList.add(userOnline);
        }
        Collections.reverse(userOnlineList);
        userOnlineList.removeAll(Collections.singleton(null));
        return getDataTable(userOnlineList);
    }

    /**
     * 强退用户
     */
    @PreAuthorize("@ss.hasPermi('monitor:online:forceLogout')")
    @Log(title = "在线用户", businessType = BusinessType.FORCE)
    @DeleteMapping("/{tokenId}")
    public AjaxResult forceLogout(@PathVariable String tokenId)
    {
//        redisCache.deleteObject(CacheConstants.LOGIN_TOKEN_KEY + tokenId);
//        return success();
        try {
            // 获取登录用户信息
            LoginUser loginUser = redisCache.getCacheObject(CacheConstants.LOGIN_TOKEN_KEY + tokenId);

            if (loginUser != null) {
                // 记录登出信息并计算登录时长
                recordLogoutAndDuration(loginUser, "强制退出");
            }

            // 删除Redis中的token
            redisCache.deleteObject(CacheConstants.LOGIN_TOKEN_KEY + tokenId);
            return success();
        } catch (Exception e) {
            log.error("强制退出用户失败", e);
            return error("强制退出失败");
        }
    }

    /**
     * 记录登出信息并计算登录时长
     */
    private void recordLogoutAndDuration(LoginUser loginUser, String logoutReason) {
        try {
            // 1. 查找对应的登录记录
            SysLogininfor loginRecord = findLatestLoginRecord(loginUser);

            Date logoutTime = new Date();

            if (loginRecord != null) {
                // 2. 计算登录时长
                long duration = (logoutTime.getTime() - loginRecord.getLoginTime().getTime()) / 1000;

                // 3. 更新登录记录的会话ID和登录时长
                loginRecord.setLoginDuration(duration);
                logininforService.updateLogininfor(loginRecord);

                // 4. 创建登出记录
                SysLogininfor logoutRecord = new SysLogininfor();
                logoutRecord.setUserName(loginUser.getUser().getUserName());
                logoutRecord.setIpaddr(loginUser.getIpaddr());
                logoutRecord.setLoginLocation(loginUser.getLoginLocation());
                logoutRecord.setBrowser(loginUser.getBrowser());
                logoutRecord.setOs(loginUser.getOs());
                logoutRecord.setStatus("0"); // 成功
                logoutRecord.setMsg(logoutReason);
                logoutRecord.setLoginTime(logoutTime); // 这里实际是登出时间
                logoutRecord.setLoginDuration(duration); // 设置登录时长

                logininforService.insertLogininfor(logoutRecord);
            }

        } catch (Exception e) {
            log.error("记录登出信息失败", e);
        }
    }

    /**
     * 查找最近的登录记录
     */
    private SysLogininfor findLatestLoginRecord(LoginUser loginUser) {
        try {
            // 将Long类型的时间戳转换为Date类型
            Date loginTime = null;
            if (loginUser.getLoginTime() != null) {
                loginTime = new Date(loginUser.getLoginTime());
            }
            // 可以通过用户名、IP地址、时间范围等条件查找最近的登录记录
            return logininforService.selectLatestLoginRecord(
                    loginUser.getUser().getUserName(),
                    loginUser.getIpaddr(),
                    loginTime
            );
        } catch (Exception e) {
            log.error("查找登录记录失败", e);
            return null;
        }
    }
}
