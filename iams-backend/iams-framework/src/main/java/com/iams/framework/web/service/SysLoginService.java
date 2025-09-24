package com.iams.framework.web.service;

import com.iams.common.exception.user.*;
import com.iams.common.utils.SecurityUtils;
import com.iams.system.domain.SysLogininfor;
import com.iams.system.mapper.SysUserMapper;

import com.iams.system.service.ISysLogininforService;
import com.iams.system.service.impl.SysDeptServiceImpl;
import jakarta.annotation.Resource;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import com.iams.common.constant.CacheConstants;
import com.iams.common.constant.Constants;
import com.iams.common.constant.UserConstants;
import com.iams.common.core.domain.entity.SysUser;
import com.iams.common.core.domain.model.LoginUser;
import com.iams.common.core.redis.RedisCache;
import com.iams.common.exception.ServiceException;
import com.iams.common.utils.DateUtils;
import com.iams.common.utils.MessageUtils;
import com.iams.common.utils.StringUtils;
import com.iams.common.utils.ip.IpUtils;
import com.iams.framework.manager.AsyncManager;
import com.iams.framework.manager.factory.AsyncFactory;
import com.iams.framework.security.context.AuthenticationContextHolder;
import com.iams.system.service.ISysConfigService;
import com.iams.system.service.ISysUserService;

import java.util.Date;
import java.util.Map;

/**
 * 登录校验方法
 * 
 * @author ruoyi
 */
@Component
public class SysLoginService
{
    @Autowired
    private TokenService tokenService;

    @Resource
    private AuthenticationManager authenticationManager;

    @Autowired
    private RedisCache redisCache;
    
    @Autowired
    private ISysUserService userService;

    @Autowired
    private SysDeptServiceImpl deptService;

    @Autowired
    private ISysConfigService configService;

    @Autowired
    private ISysLogininforService logininforService;
    @Autowired
    private SysUserMapper sysUserMapper;



    /**
     * 登录验证
     * 
     * @param username 用户名
     * @param password 密码
     * @param code 验证码
     * @param uuid 唯一标识
     * @return 结果
     */
    public String login(String username, String password, String code, String uuid)
    {

        System.out.println("=== 开始登录流程 ===");
        System.out.println("用户名: " + username);

        // 验证码校验
        validateCaptcha(username, code, uuid);
        // 登录前置校验
        loginPreCheck(username, password);
        // 用户验证
        Authentication authentication = null;
        try
        {
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(username, password);
            AuthenticationContextHolder.setContext(authenticationToken);
            // 该方法会去调用UserDetailsServiceImpl.loadUserByUsername
            authentication = authenticationManager.authenticate(authenticationToken);
        }
        catch (Exception e)
        {
            if (e instanceof BadCredentialsException)
            {
                AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, MessageUtils.message("user.password.not.match")));
                throw new UserPasswordNotMatchException();
            }
            else
            {
                AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, e.getMessage()));
                throw new ServiceException(e.getMessage());
            }
        }
        finally
        {
            AuthenticationContextHolder.clearContext();
        }

        // 检查登录间隔并记录相应的登录状态
//        LoginStatusResult loginResult = checkLoginInterval(username);
//        AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, loginResult.getStatus(), loginResult.getMessage()));
//
//        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
//        recordLoginInfo(loginUser.getUserId());
//        // 生成token
//        return tokenService.createToken(loginUser);


//        LoginStatusResult loginResult = checkLoginInterval(username);
//
//        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
//        recordLoginInfo(loginUser.getUserId());
//
//        // 生成token
//        String token = tokenService.createToken(loginUser);
//
//        // 记录登录成功日志，传入token作为sessionId
//        AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, loginResult.getStatus(), loginResult.getMessage(), token));
//
//        return token;
        // 检查登录间隔并记录相应的登录状态
        LoginStatusResult loginResult = checkLoginInterval(username);
        System.out.println("登录检查结果 - 状态: " + loginResult.getStatus() + ", 消息: " + loginResult.getMessage());

        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        recordLoginInfo(loginUser.getUserId());

        // 生成token
        String token = tokenService.createToken(loginUser);
        System.out.println("生成的token: " + token.substring(0, 20) + "...");

        // 记录登录成功日志，传入token作为sessionId
        System.out.println("=== 准备记录登录日志 ===");
        AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, loginResult.getStatus(), loginResult.getMessage(), token));
        System.out.println("=== 登录日志记录任务已提交 ===");

        return token;
    }


    public String loginSSO(Map<String, Object> responseBody, String access_token)
    {

        // 暂时把用户的id放入备注
        SysUser userFind = sysUserMapper.selectSsoUserByRemark((String) (responseBody.get("id")));
        if (userFind == null)
        {
            // 新建用户
            String userName = (String) responseBody.get("name");
            String organName = (String)responseBody.get("organName");
            String positionName = (String)responseBody.get("positionName");
            String accountName = (String)responseBody.get("accountName");

            SysUser user = new SysUser();
//            user.setUserName(userName + "-" + organName + "-" + positionName);
            user.setUserName(userName);
            user.setRemark((String) responseBody.get("id"));
            user.setNickName(accountName);

            //设置默认的角色状态与岗位信息
            user.setRoleIds(new Long[]{3L});
            user.setPostIds(new Long[]{6L});

            // 设置默认用户密码
            user.setPassword(SecurityUtils.encryptPassword("123456"));

            userService.insertUser(user);

//            return login(userName + "-" + organName + "-" + positionName);
            return login(userName);
        }
        else {
            return login(userFind.getUserName());
        }
    }

    private String login(String username)
    {
        // 验证码校验
        // validateCaptcha(username, code, uuid);
        // 登录前置校验
        // loginPreCheck(username, password);
        // 用户验证
        Authentication authentication = null;
        try
        {
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(username, "ssoLogin");
            AuthenticationContextHolder.setContext(authenticationToken);
            // 该方法会去调用UserDetailsServiceImpl.loadUserByUsername
            authentication = authenticationManager.authenticate(authenticationToken);
        }
        catch (Exception e)
        {
            if (e instanceof BadCredentialsException)
            {
                AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, MessageUtils.message("user.password.not.match")));
                throw new UserPasswordNotMatchException();
            }
            else
            {
                AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, e.getMessage()));
                throw new ServiceException(e.getMessage());
            }
        }
        finally
        {
            AuthenticationContextHolder.clearContext();
        }

//        // 检查登录间隔并记录相应的登录状态
//        LoginStatusResult loginResult = checkLoginInterval(username);
//        AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, loginResult.getStatus(), loginResult.getMessage()));
//
//        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
//        recordLoginInfo(loginUser.getUserId());
//        // 生成token
//        return tokenService.createToken(loginUser);
        // 检查登录间隔并记录相应的登录状态
        LoginStatusResult loginResult = checkLoginInterval(username);

        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        recordLoginInfo(loginUser.getUserId());

        // 生成token
        String token = tokenService.createToken(loginUser);

        // 记录登录成功日志，传入token作为sessionId
        AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, loginResult.getStatus(), loginResult.getMessage(), token));

        return token;
    }

    /**
     * 检查用户登录间隔并返回相应的登录状态和消息
     * @param username 用户名
     * @return 包含状态和消息的结果
     */
    private LoginStatusResult checkLoginInterval(String username) {
        try {
            // 获取配置的间隔天数
            String gapTimeStr = configService.selectConfigByKey("sys.login.gap_time");
            int gapDays = StringUtils.isNotEmpty(gapTimeStr) ? Integer.parseInt(gapTimeStr) : 7;

            // 查询用户上次成功登录时间
            Date lastLoginTime = logininforService.selectLastSuccessLoginTime(username);

            if (lastLoginTime != null) {
                // 计算时间间隔
                long diffInMillies = System.currentTimeMillis() - lastLoginTime.getTime();
                long diffInDays = diffInMillies / (24 * 60 * 60 * 1000);

                if (diffInDays > gapDays) {
                    String warningMsg = String.format("用户已连续%d天未登录，请注意账户安全", diffInDays);
                    return new LoginStatusResult(Constants.LOGIN_WARNING, warningMsg);
                }
            }

            return new LoginStatusResult(Constants.LOGIN_SUCCESS, MessageUtils.message("user.login.success"));
        } catch (Exception e) {
            // 出现异常时默认返回成功状态
            return new LoginStatusResult(Constants.LOGIN_SUCCESS, MessageUtils.message("user.login.success"));
        }
    }

    // 内部类用于返回检查结果
    @Getter
    private static class LoginStatusResult {
        private String status;
        private String message;

        public LoginStatusResult(String status, String message) {
            this.status = status;
            this.message = message;
        }

    }

    /**
     * 校验验证码
     * 
     * @param username 用户名
     * @param code 验证码
     * @param uuid 唯一标识
     * @return 结果
     */
    public void validateCaptcha(String username, String code, String uuid)
    {
        boolean captchaEnabled = configService.selectCaptchaEnabled();
        if (captchaEnabled)
        {
            String verifyKey = CacheConstants.CAPTCHA_CODE_KEY + StringUtils.nvl(uuid, "");
            String captcha = redisCache.getCacheObject(verifyKey);
            if (captcha == null)
            {
                AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, MessageUtils.message("user.jcaptcha.expire")));
                throw new CaptchaExpireException();
            }
            redisCache.deleteObject(verifyKey);
            if (!code.equalsIgnoreCase(captcha))
            {
                AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, MessageUtils.message("user.jcaptcha.error")));
                throw new CaptchaException();
            }
        }
    }

    /**
     * 登录前置校验
     * @param username 用户名
     * @param password 用户密码
     */
    public void loginPreCheck(String username, String password)
    {
        // 用户名或密码为空 错误
        if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password))
        {
            AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, MessageUtils.message("not.null")));
            throw new UserNotExistsException();
        }
        // 密码如果不在指定范围内 错误
        if (password.length() < UserConstants.PASSWORD_MIN_LENGTH
                || password.length() > UserConstants.PASSWORD_MAX_LENGTH)
        {
            AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, MessageUtils.message("user.password.not.match")));
            throw new UserPasswordNotMatchException();
        }
        // 用户名不在指定范围内 错误
        if (username.length() < UserConstants.USERNAME_MIN_LENGTH
                || username.length() > UserConstants.USERNAME_MAX_LENGTH)
        {
            AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, MessageUtils.message("user.password.not.match")));
            throw new UserPasswordNotMatchException();
        }
        // IP黑名单校验
        String blackStr = configService.selectConfigByKey("sys.login.blackIPList");
        if (IpUtils.isMatchedIp(blackStr, IpUtils.getIpAddr()))
        {
            AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, MessageUtils.message("login.blocked")));
            throw new BlackListException();
        }

        // 用户名黑名单校验：
        String blackUserName = configService.selectConfigByKey("sys.login.blackUserList");
        if (StringUtils.isNotEmpty(blackUserName) && blackUserName.contains(username))
        {
            AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, MessageUtils.message("login.userBlocked")));
            throw new BlackUserListException();
        }

    }

    /**
     * 记录登录信息
     *
     * @param userId 用户ID
     */
    public void recordLoginInfo(Long userId)
    {
        SysUser sysUser = new SysUser();
        sysUser.setUserId(userId);
        sysUser.setLoginIp(IpUtils.getIpAddr());
        sysUser.setLoginDate(DateUtils.getNowDate());
        userService.updateUserProfile(sysUser);
    }
}
