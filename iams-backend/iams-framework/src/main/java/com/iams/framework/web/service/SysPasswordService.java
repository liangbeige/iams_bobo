package com.iams.framework.web.service;

import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import com.iams.common.constant.CacheConstants;
import com.iams.common.core.domain.entity.SysUser;
import com.iams.common.core.redis.RedisCache;
import com.iams.common.exception.user.UserPasswordNotMatchException;
import com.iams.common.exception.user.UserPasswordRetryLimitExceedException;
import com.iams.common.utils.SecurityUtils;
import com.iams.framework.security.context.AuthenticationContextHolder;

/**
 * 登录密码方法
 * 
 * @author ruoyi
 */
@Component
public class SysPasswordService
{
    @Autowired
    private RedisCache redisCache;

    @Value(value = "${user.password.maxRetryCount}")
    private int maxRetryCount;

    @Value(value = "${user.password.lockTime}")
    private int lockTime;

    /**
     * 登录账户密码错误次数缓存键名
     * 
     * @param username 用户名
     * @return 缓存键key
     */
    private String getCacheKey(String username)
    {
        return CacheConstants.PWD_ERR_CNT_KEY + username;
    }

    // 输入user是根据前端输入的username查数据库获得的user
    public void validate(SysUser user)
    {
        // 从登陆函数创建的线程局部变量中拿到上下文信息，
        Authentication usernamePasswordAuthenticationToken = AuthenticationContextHolder.getContext();
        String username = usernamePasswordAuthenticationToken.getName();
        String password = usernamePasswordAuthenticationToken.getCredentials().toString();

        Integer retryCount = redisCache.getCacheObject(getCacheKey(username));

        if("ssoLogin".equals(password))
        {
            clearLoginRecordCache(username);
            return;
        }

        // 记录登陆密码错误次数
        if (retryCount == null)
        {
            retryCount = 0;
        }

        if (retryCount >= Integer.valueOf(maxRetryCount).intValue())
        {
            throw new UserPasswordRetryLimitExceedException(maxRetryCount, lockTime);
        }

        // 第一次输错的话就先保存：（常量字符串+用户名：重试次数）
        if (!matches(user, password))
        {
            retryCount = retryCount + 1;
            redisCache.setCacheObject(getCacheKey(username), retryCount, lockTime, TimeUnit.MINUTES);
            throw new UserPasswordNotMatchException();
        }
        else
        {
            // 如果密码匹配，就清空缓存信息。
            clearLoginRecordCache(username);
        }
    }
    /*
      * SSO登录验证，只验证用户是否存在，不验证密码。
     */
    public void ssoValidate(SysUser user)
    {

    }

    public boolean matches(SysUser user, String rawPassword)
    {
        return SecurityUtils.matchesPassword(rawPassword, user.getPassword());
    }

    public void clearLoginRecordCache(String loginName)
    {
        if (redisCache.hasKey(getCacheKey(loginName)))
        {
            redisCache.deleteObject(getCacheKey(loginName));
        }
    }
}
