package com.iams.manage.service;

import jakarta.mail.MessagingException;

public interface IMailService {

    public void sendHtmlMail(String to, String subject, String content, boolean isHtml)throws MessagingException;
}
