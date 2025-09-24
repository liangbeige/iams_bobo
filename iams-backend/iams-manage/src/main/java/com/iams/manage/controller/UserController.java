package com.iams.manage.controller;

import com.iams.common.core.domain.entity.SysUser;
import com.iams.system.service.ISysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private ISysUserService userService;

    @GetMapping("/roleId")
    public List<SysUser> selectUserListByRoleId(Long roleId){
        return userService.selectUserListByRoleId(roleId);
    }
}
