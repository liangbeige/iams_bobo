package com.iams.manage.validator;


import com.iams.common.core.domain.entity.SysUser;
import com.iams.manage.domain.workflow.dto.CommonRecordBaseDTO;
import com.iams.system.service.ISysUserService;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.springframework.beans.factory.annotation.Autowired;

public class UserInfoValidator implements ConstraintValidator<ValidUserInfo, CommonRecordBaseDTO> {

    @Autowired
    private ISysUserService userService;


    @Override
    public boolean isValid(CommonRecordBaseDTO value, ConstraintValidatorContext context) {

        if(value == null)
        {   // 空数据是可以的，比如getList函数
            return true;
        }
        context.disableDefaultConstraintViolation();
        SysUser user = userService.selectUserById(value.getUserId());
        if(user == null)
        {
            // 检查用户是否存在
            context.buildConstraintViolationWithTemplate("用户ID、用户名和部门不一致").addConstraintViolation();
            return false;
        }


        if(user.getUserName().equals(value.getUserName())
                && user.getDept().getDeptName().equals(value.getUserDepartment()))
        {
            return true;
        }
        context.buildConstraintViolationWithTemplate("用户ID、用户名和部门不一致").addConstraintViolation();
        return false;
    }
}
