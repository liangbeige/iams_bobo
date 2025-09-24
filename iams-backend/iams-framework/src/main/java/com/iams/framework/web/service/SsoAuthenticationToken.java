package com.iams.framework.web.service;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

/**
 * 自定义Spring-Security身份验证令牌
 *
 * @author LiuTao
 */
public class SsoAuthenticationToken extends UsernamePasswordAuthenticationToken {
    public SsoAuthenticationToken(Object principal, Object credentials) {
        super(principal, credentials);
    }

    public SsoAuthenticationToken(Object principal, Object credentials, Collection<? extends GrantedAuthority> authorities) {
        super(principal, credentials, authorities);
    }
}
