package com.iams.manage.validator.aspect;


import com.iams.common.utils.SecurityUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import com.iams.manage.validator.ValidGroupPermission;

@Aspect
@Component
public class GroupPermissionAspect {

//    @Around("@annotation(ValidGroupPermission)")
//    public Object checkGroupPermission(ProceedingJoinPoint point, ValidGroupPermission ValidGroupPermission) throws Throwable {
//        String groupCode = ValidGroupPermission.value();
//
//        if (SecurityUtils.hasGroupPermission(SecurityUtils.getLoginUser(), groupCode)) {
//            return point.proceed();
//        }
//        throw new RuntimeException("没有权限");
//    }
}
