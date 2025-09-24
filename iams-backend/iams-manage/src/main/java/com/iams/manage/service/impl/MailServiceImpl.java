package com.iams.manage.service.impl;

import com.iams.manage.service.IMailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;




@Service
public class MailServiceImpl implements IMailService {

    @Autowired
    private JavaMailSender javaMailSender;


    @Override
    public void sendHtmlMail(String to, String subject, String content, boolean isHtml) throws MessagingException {

        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setFrom("1490866637@qq.com");  // 发件人需与配置中的username一致
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(content, isHtml);  // true表示内容为HTML

        javaMailSender.send(message);
    }

}
