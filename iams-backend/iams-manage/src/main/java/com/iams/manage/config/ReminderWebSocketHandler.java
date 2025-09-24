package com.iams.manage.config;

import com.alibaba.fastjson2.JSON;
import com.iams.manage.domain.ReminderMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.ConcurrentHashMap;


@Component
public class ReminderWebSocketHandler implements WebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(ReminderWebSocketHandler.class);

    // 存储用户会话
    private static final ConcurrentHashMap<Long, WebSocketSession> userSessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Long userId = getUserIdFromSession(session);
        if (userId != null) {
            userSessions.put(userId, session);
            log.info("用户 {} 建立WebSocket连接", userId);
        }
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        // 处理接收到的消息
        log.info("收到WebSocket消息: {}", message.getPayload());
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("WebSocket传输错误", exception);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        Long userId = getUserIdFromSession(session);
        if (userId != null) {
            userSessions.remove(userId);
            log.info("用户 {} 断开WebSocket连接", userId);
        }
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    /**
     * 发送实时消息给指定用户
     */
    public void sendReminderToUser(Long userId, ReminderMessage message) {
        WebSocketSession session = userSessions.get(userId);
        if (session != null && session.isOpen()) {
            try {
                String jsonMessage = JSON.toJSONString(message);
                session.sendMessage(new TextMessage(jsonMessage));
                log.info("向用户 {} 发送催办消息: {}", userId, message.getTitle());
            } catch (IOException e) {
                log.error("发送WebSocket消息失败", e);
            }
        }
    }

    /**
     * 检查用户是否在线
     */
    public boolean isUserOnline(Long userId) {
        WebSocketSession session = userSessions.get(userId);
        return session != null && session.isOpen();
    }

    /**
     * 获取在线用户数量
     */
    public int getOnlineUserCount() {
        return userSessions.size();
    }

    /**
     * 从session中获取用户ID
     */
    private Long getUserIdFromSession(WebSocketSession session) {
        try {
            URI uri = session.getUri();
            if (uri != null) {
                String query = uri.getQuery();
                if (query != null && query.contains("userId=")) {
                    String userIdStr = query.substring(query.indexOf("userId=") + 7);
                    if (userIdStr.contains("&")) {
                        userIdStr = userIdStr.substring(0, userIdStr.indexOf("&"));
                    }
                    return Long.parseLong(userIdStr);
                }
            }
        } catch (Exception e) {
            log.error("解析用户ID失败", e);
        }
        return null;
    }
}