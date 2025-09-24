package com.iams.common.exception.user;

/**
 * 黑名单用户异常类
 *
 * @author ruoyi
 */
public class BlackUserListException extends UserException
{
    private static final long serialVersionUID = 1L;

    public BlackUserListException()
    {
        super("login.userBlocked", null);
    }
}
