package com.iams.web.controller.system;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import com.iams.common.constant.Constants;
import com.iams.common.core.domain.AjaxResult;
import com.iams.common.core.domain.entity.SysMenu;
import com.iams.common.core.domain.entity.SysUser;
import com.iams.common.core.domain.model.LoginBody;
import com.iams.common.core.domain.model.LoginUser;
import com.iams.common.utils.SecurityUtils;
import com.iams.framework.web.service.SysLoginService;
import com.iams.framework.web.service.SysPermissionService;
import com.iams.framework.web.service.TokenService;
import org.springframework.web.client.RestTemplate;
import com.iams.system.service.ISysMenuService;

/**
 * 登录验证
 * 
 * @author ruoyi
 */
@RestController
public class SysLoginController
{
    Logger log = org.slf4j.LoggerFactory.getLogger(SysLoginController.class);

    @Autowired
    private SysLoginService loginService;

    @Autowired
    private ISysMenuService menuService;

    @Autowired
    private SysPermissionService permissionService;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private RestTemplate restTemplate;

    /**
     * 登录方法
     * 
     * @param loginBody 登录信息
     * @return 结果
     */
    @PostMapping("/login")
    public AjaxResult login(@RequestBody LoginBody loginBody)
    {
        AjaxResult ajax = AjaxResult.success();
        // 生成令牌
        String token = loginService.login(loginBody.getUsername(), loginBody.getPassword(), loginBody.getCode(),
                loginBody.getUuid());
        ajax.put(Constants.TOKEN, token);
        return ajax;
    }

    @PostMapping("/login-sso")
    public AjaxResult loginV2(@RequestBody Map<String, String> data) {
        String stage = "";
        try {
            stage = "参数验证";
            String access_token = data.get("access_token");
            if (access_token == null || access_token.trim().isEmpty()) {
                return AjaxResult.error("access_token不能为空");
            }

            stage = "构建请求";
            String url = "http://192.168.1.11:20201/fencing5/api/open/auth/get-user-info?access_token=" + access_token;
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            HttpEntity<?> entity = new HttpEntity<>(headers);

            stage = "调用SSO接口";
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);

            stage = "处理SSO响应";
            if (!response.getStatusCode().is2xxSuccessful()) {
                log.error("SSO接口调用失败，状态码: {}", response.getStatusCode());
                return AjaxResult.error("获取用户信息失败，状态码：" + response.getStatusCode());
            }

            Map<String, Object> userInfo = (Map<String, Object>) response.getBody().get("data");
            if (userInfo == null) {
                log.error("SSO接口返回响应体为空");
                return AjaxResult.error("获取用户信息失败，响应为空");
            }

            stage = "执行登录逻辑";
            String result = loginService.loginSSO(userInfo, access_token);

            AjaxResult ajax = AjaxResult.success();
            ajax.put(Constants.TOKEN, result);
            return ajax;

        } catch (Exception e) {
            log.error("SSO登录在{}阶段发生异常: {}", stage, e.getMessage(), e);
            return AjaxResult.error(String.format("在%s阶段发生异常: %s", stage, e.getMessage()));
        }
    }

    /**
     * 获取用户信息
     * 
     * @return 用户信息
     */
    @GetMapping("getInfo")
    public AjaxResult getInfo()
    {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        SysUser user = loginUser.getUser();
        // 角色集合
        Set<String> roles = permissionService.getRolePermission(user);
        // 权限集合
        Set<String> permissions = permissionService.getMenuPermission(user);
        if (!loginUser.getPermissions().equals(permissions))
        {
            loginUser.setPermissions(permissions);
            tokenService.refreshToken(loginUser);
        }
        AjaxResult ajax = AjaxResult.success();
        ajax.put("user", user);
        ajax.put("roles", roles);
        ajax.put("permissions", permissions);
        return ajax;
    }

    /**
     * 获取路由信息
     * 
     * @return 路由信息
     */
    @GetMapping("getRouters")
    public AjaxResult getRouters()
    {
        Long userId = SecurityUtils.getUserId();
        List<SysMenu> menus = menuService.selectMenuTreeByUserId(userId);
        return AjaxResult.success(menuService.buildMenus(menus));
    }
}
