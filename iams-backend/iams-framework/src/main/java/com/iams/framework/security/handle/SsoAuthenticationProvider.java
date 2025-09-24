package com.iams.framework.security.handle;

import com.iams.framework.web.service.SsoAuthenticationToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

@Component
public class SsoAuthenticationProvider implements AuthenticationProvider {

    @Autowired
    private UserDetailsService  userDetailsService;


    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException{
        String userName = authentication.getName();
        String password = authentication.getCredentials().toString();

        // 密码是 ssoLogin 则强制登录
        if ("ssoLogin".equals(password)) {
            UserDetails user = userDetailsService.loadUserByUsername(userName);
            return new SsoAuthenticationToken(user, password, user.getAuthorities());
        }

        // 使用用户详情服务进行正常的身份认证
        UserDetails user = userDetailsService.loadUserByUsername(userName);
        if (user != null && user.getPassword().equals(password)) {
            return new UsernamePasswordAuthenticationToken(user, password, user.getAuthorities());
        }

        return null;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }
}
