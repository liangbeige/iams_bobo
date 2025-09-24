<template>
  <!-- 只在HTTP协议下显示组件 -->
  <div v-if="isHttpProtocol" class="reminder-container">
    <!-- 消息气泡按钮 -->
    <div
        v-if="unreadCount > 0 || alwaysShowBubble"
        class="reminder-bubble"
        @click="toggleMessageList"
        :class="{ 'pulse': hasNewMessage, 'no-message': unreadCount === 0 }"
    >
      <el-badge :value="unreadCount" :max="99" :hidden="unreadCount === 0">
        <i class="el-icon-bell"></i>
      </el-badge>
    </div>

    <!-- 消息列表弹窗 -->
    <transition name="slide-fade">
      <div v-if="showMessageList" class="message-list-container">
        <div class="message-header">
          <span class="title">
            <i class="el-icon-bell"></i>
            催办消息
          </span>
          <div class="header-actions">
            <el-button
                size="mini"
                type="text"
                @click="markAllAsRead"
                v-if="unreadCount > 0"
            >
              全部已读
            </el-button>
            <i class="el-icon-close" @click="closeMessageList"></i>
          </div>
        </div>

        <div class="message-list" v-if="messages.length > 0">
          <div
              v-for="message in messages"
              :key="message.id"
              class="message-item"
              :class="{ 'unread': !message.isRead }"
              @click="handleMessageClick(message)"
          >
            <div class="message-icon">
              <i class="el-icon-warning-outline" v-if="message.messageType === 'REMINDER'"></i>
              <i class="el-icon-info" v-else-if="message.messageType === 'INFO'"></i>
              <i class="el-icon-warning" v-else></i>
            </div>
            <div class="message-content">
              <div class="message-title">{{ message.title }}</div>
              <div class="message-text">{{ message.content }}</div>
              <div class="message-time">{{ formatTime(message.createTime) }}</div>
            </div>
            <div class="message-actions">
              <el-button
                  size="mini"
                  type="text"
                  @click.stop="markAsRead(message.id)"
                  v-if="!message.isRead"
                  class="read-btn"
              >
                标记已读
              </el-button>
              <i class="el-icon-success read-icon" v-else></i>
            </div>
          </div>
        </div>

        <div v-else class="empty-message">
          <i class="el-icon-success"></i>
          <p>暂无催办消息</p>
        </div>

<!--        <div class="message-footer" v-if="messages.length > 5">-->
<!--          <el-button type="text" size="small" @click="showMoreMessages">-->
<!--            查看更多消息-->
<!--          </el-button>-->
<!--        </div>-->
      </div>
    </transition>

    <!-- 实时弹窗消息 -->
    <transition-group name="popup" tag="div" class="popup-messages">
      <div
          v-for="popup in popupMessages"
          :key="popup.id"
          class="popup-message"
          @click="handlePopupClick(popup)"
      >
        <div class="popup-icon">
          <i class="el-icon-warning-outline"></i>
        </div>
        <div class="popup-body">
          <div class="popup-header">
            <span class="popup-title">{{ popup.title }}</span>
            <i class="el-icon-close" @click.stop="removePopup(popup.id)"></i>
          </div>
          <div class="popup-content">{{ popup.content }}</div>
          <div class="popup-time">{{ formatTime(popup.createTime) }}</div>
        </div>
      </div>
    </transition-group>

    <!-- 连接状态指示器 -->
    <div class="connection-status" v-if="!websocketConnected">
      <i class="el-icon-warning"></i>
      <span>连接断开，正在重连...</span>
    </div>
  </div>
</template>

<script>
import {
  getUnreadMessages,
  getUnreadCount,
  markMessageAsRead,
  markAllMessagesAsRead
} from '@/api/manage/reminder'
import useUserStore from '@/store/modules/user' // 根据你的实际路径调整

export default {
  name: 'ReminderPopup',
  props: {
    // 是否总是显示气泡按钮（即使没有未读消息）
    alwaysShowBubble: {
      type: Boolean,
      default: false
    },
    // 弹窗自动关闭时间（毫秒）
    autoCloseTime: {
      type: Number,
      default: 5000
    },
    // 最大同时显示弹窗数量
    maxPopupCount: {
      type: Number,
      default: 3
    }
  },

  data() {
    return {
      websocket: null,
      websocketConnected: false,
      messages: [],
      popupMessages: [],
      unreadCount: 0,
      showMessageList: false,
      hasNewMessage: false,
      reconnectTimer: null,
      reconnectAttempts: 0,
      maxReconnectAttempts: 10,
      popupTimer: {},
      loadingMessages: false,
      userStore: null // 存储 userStore 实例
    }
  },

  computed: {
    // 检查是否为HTTP协议
    isHttpProtocol() {
      return window.location.protocol === 'http:'
    }
  },

  created() {
    // 在 created 钩子中初始化 userStore
    this.userStore = useUserStore()
  },

  mounted() {
    // 只在HTTP协议下初始化功能
    if (!this.isHttpProtocol) {
      console.log('HTTPS协议下，消息提醒功能已禁用')
      return
    }

    // 延迟初始化，确保用户数据已加载
    this.$nextTick(() => {
      setTimeout(() => {
        this.initWebSocket()
      }, 500)
    })

    this.loadUnreadMessages()
    this.loadUnreadCount()

    // 点击外部关闭消息列表
    document.addEventListener('click', this.handleDocumentClick)

    // 页面可见性变化时重连WebSocket
    document.addEventListener('visibilitychange', this.handleVisibilityChange)
  },

  beforeUnmount() {
    // 只在HTTP协议下才需要清理资源
    if (!this.isHttpProtocol) {
      return
    }

    this.closeWebSocket()
    document.removeEventListener('click', this.handleDocumentClick)
    document.removeEventListener('visibilitychange', this.handleVisibilityChange)

    // 清理所有定时器
    Object.values(this.popupTimer).forEach(timer => clearTimeout(timer))
    if (this.reconnectTimer) {
      clearInterval(this.reconnectTimer)
    }
  },

  methods: {
    // 初始化WebSocket连接
    initWebSocket() {
      // 如果不是HTTP协议，直接返回
      if (!this.isHttpProtocol) {
        return
      }

      try {
        // 安全检查
        if (!this.userStore) {
          console.error('UserStore 未初始化')
          setTimeout(() => {
            this.initWebSocket()
          }, 1000)
          return
        }

        const userId = this.userStore.id
        if (!userId) {
          console.warn('用户未登录，无法建立WebSocket连接')
          // 如果用户未登录，可以监听登录状态变化
          setTimeout(() => {
            this.initWebSocket()
          }, 2000)
          return
        }

        console.log('正在为用户初始化WebSocket:', userId)

        // WebSocket URL 配置 - 只支持HTTP环境
        let wsUrl
        if (process.env.NODE_ENV === 'development') {
          wsUrl = `ws://localhost:9992/websocket/reminder?userId=${userId}`
        } else {
          // 生产环境：直接连后端，使用ws
          wsUrl = `ws://192.168.1.11:9992/websocket/reminder?userId=${userId}`
        }

        console.log('WebSocket URL:', wsUrl)

        this.websocket = new WebSocket(wsUrl)

        this.websocket.onopen = () => {
          console.log('WebSocket连接成功')
          this.websocketConnected = true
          this.reconnectAttempts = 0

          if (this.reconnectTimer) {
            clearInterval(this.reconnectTimer)
            this.reconnectTimer = null
          }
        }

        this.websocket.onmessage = (event) => {
          try {
            const message = JSON.parse(event.data)
            this.handleNewMessage(message)
          } catch (error) {
            console.error('解析WebSocket消息失败:', error)
          }
        }

        this.websocket.onclose = (event) => {
          console.log('WebSocket连接关闭:', event.code, event.reason)
          this.websocketConnected = false

          // 如果不是正常关闭，尝试重连
          if (event.code !== 1000) {
            this.reconnect()
          }
        }

        this.websocket.onerror = (error) => {
          console.error('WebSocket连接错误:', error)
          this.websocketConnected = false
          this.reconnect()
        }
      } catch (error) {
        console.error('初始化WebSocket失败:', error)
        this.reconnect()
      }
    },

    // 重连机制
    reconnect() {
      // 如果不是HTTP协议，不进行重连
      if (!this.isHttpProtocol) {
        return
      }

      if (this.reconnectTimer || this.reconnectAttempts >= this.maxReconnectAttempts) {
        return
      }

      this.reconnectAttempts++
      const delay = Math.min(1000 * Math.pow(2, this.reconnectAttempts), 30000)

      console.log(`第${this.reconnectAttempts}次尝试重连WebSocket，${delay}ms后重试...`)

      this.reconnectTimer = setTimeout(() => {
        this.reconnectTimer = null
        this.initWebSocket()
      }, delay)
    },

    // 关闭WebSocket连接
    closeWebSocket() {
      if (this.websocket) {
        this.websocket.close(1000, '正常关闭')
        this.websocket = null
      }

      this.websocketConnected = false

      if (this.reconnectTimer) {
        clearTimeout(this.reconnectTimer)
        this.reconnectTimer = null
      }
    },

    // 处理页面可见性变化
    handleVisibilityChange() {
      // 如果不是HTTP协议，不处理
      if (!this.isHttpProtocol) {
        return
      }

      if (document.visibilityState === 'visible') {
        // 页面变为可见时，重新连接WebSocket
        if (!this.websocketConnected) {
          this.initWebSocket()
        }
        // 刷新消息数据
        this.loadUnreadCount()
      }
    },

    // 处理新消息
    handleNewMessage(message) {
      console.log('收到新消息:', message)

      // 添加到消息列表
      this.messages.unshift(message)
      this.unreadCount++

      // 限制弹窗数量
      if (this.popupMessages.length >= this.maxPopupCount) {
        const oldest = this.popupMessages.shift()
        this.removePopup(oldest.id, false)
      }

      // 显示弹窗
      this.showPopupMessage(message)

      // 气泡动画
      this.hasNewMessage = true
      setTimeout(() => {
        this.hasNewMessage = false
      }, 2000)

      // 播放提示音（可选）
      this.playNotificationSound()
    },

    // 显示弹窗消息
    showPopupMessage(message) {
      this.popupMessages.push({...message})

      // 自动关闭弹窗
      this.popupTimer[message.id] = setTimeout(() => {
        this.removePopup(message.id)
      }, this.autoCloseTime)
    },

    // 移除弹窗
    removePopup(messageId, clearTimer = true) {
      const index = this.popupMessages.findIndex(m => m.id === messageId)
      if (index > -1) {
        this.popupMessages.splice(index, 1)
      }

      if (clearTimer && this.popupTimer[messageId]) {
        clearTimeout(this.popupTimer[messageId])
        delete this.popupTimer[messageId]
      }
    },

    // 播放提示音
    playNotificationSound() {
      try {
        // 创建音频对象（可以替换为自定义音频文件）
        const audio = new Audio('data:audio/wav;base64,UklGRnoGAABXQVZFZm10IBAAAAABAAEAQB8AAEAfAAABAAgAZGF0YQoGAACBhYqFbF1fdJivrJBhNjVgodDbq2EcBj+a2/LDciUFLIHO8tiJNwgZaLvt559NEAxQp+PwtmMcBjiR1/LMeSwFJHfH8N2QQAoUXrTp66hVFApGn+bswmkjBT2V1fPJdCMELhvU9NF4MyoMI2+z9N+ZTAoIS6PZ7rlXGAg7kNT8yGwiCCaUx/TOgS4OEmSw6+OVVQU+jNry0l4SBkttyOm7fSUJNmKz69NgGQQ7kNr7wGYjCy+G0fXKeTELEmCq6tuaWAoLU6Xi34hKCQhmtu3eaTQ=')
        audio.volume = 0.3
        audio.play().catch(() => {
          // 浏览器阻止自动播放，忽略错误
        })
      } catch (error) {
        // 忽略音频播放错误
      }
    },

    // 加载未读消息
    async loadUnreadMessages() {
      // 如果不是HTTP协议，不加载消息
      if (!this.isHttpProtocol) {
        return
      }

      if (this.loadingMessages) return

      this.loadingMessages = true
      try {
        const response = await getUnreadMessages()
        this.messages = response.data || []
      } catch (error) {
        console.error('加载未读消息失败:', error)
        this.$message.error('加载消息失败')
      } finally {
        this.loadingMessages = false
      }
    },

    // 加载未读数量
    async loadUnreadCount() {
      // 如果不是HTTP协议，不加载未读数量
      if (!this.isHttpProtocol) {
        return
      }

      try {
        const response = await getUnreadCount()
        this.unreadCount = response.data || 0
      } catch (error) {
        console.error('加载未读数量失败:', error)
      }
    },

    // 切换消息列表显示
    toggleMessageList() {
      this.showMessageList = !this.showMessageList

      if (this.showMessageList && this.messages.length === 0) {
        this.loadUnreadMessages()
      }
    },

    // 关闭消息列表
    closeMessageList() {
      this.showMessageList = false
    },

    // 处理文档点击事件
    handleDocumentClick(event) {
      const container = this.$el
      if (container && !container.contains(event.target)) {
        this.showMessageList = false
      }
    },

    // 标记消息已读
    async markAsRead(messageId) {
      try {
        await markMessageAsRead(messageId)

        // 更新本地状态
        const message = this.messages.find(m => m.id === messageId)
        if (message && !message.isRead) {
          message.isRead = true
          this.unreadCount = Math.max(0, this.unreadCount - 1)
        }

        this.$message.success('已标记为已读')
      } catch (error) {
        console.error('标记已读失败:', error)
        this.$message.error('操作失败')
      }
    },

    // 全部标记已读
    async markAllAsRead() {
      try {
        await markAllMessagesAsRead()

        // 更新本地状态
        this.messages.forEach(message => {
          message.isRead = true
        })
        this.unreadCount = 0

        this.$message.success('已全部标记为已读')
      } catch (error) {
        console.error('全部标记已读失败:', error)
        this.$message.error('操作失败')
      }
    },

    // 处理消息点击
    handleMessageClick(message) {
      // 标记已读
      if (!message.isRead) {
        this.markAsRead(message.id)
      }

      // 跳转到相关流程页面
      if (message.taskId) {
        // 根据实际路由配置调整
        this.$router.push({
          path: `/workflow/task/detail/${message.taskId}`,
          query: {
            processInstanceId: message.processInstanceId
          }
        }).catch(() => {
          // 路由跳转失败，可能是路由不存在
          this.$message.warning('相关页面不存在')
        })
      }

      // 关闭消息列表
      this.closeMessageList()
    },

    // 处理弹窗点击
    handlePopupClick(popup) {
      this.handleMessageClick(popup)
      this.removePopup(popup.id)
    },

    // // 显示更多消息
    // showMoreMessages() {
    //   // 跳转到消息管理页面
    //   this.$router.push('/system/reminder')
    //   this.closeMessageList()
    // },

    // 格式化时间
    formatTime(time) {
      if (!time) return ''

      const date = new Date(time)
      const now = new Date()
      const diff = now - date

      if (diff < 60000) { // 1分钟内
        return '刚刚'
      } else if (diff < 3600000) { // 1小时内
        return Math.floor(diff / 60000) + '分钟前'
      } else if (diff < 86400000) { // 1天内
        return Math.floor(diff / 3600000) + '小时前'
      } else if (diff < 2592000000) { // 30天内
        return Math.floor(diff / 86400000) + '天前'
      } else {
        return date.toLocaleDateString()
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.reminder-container {
  position: fixed;
  bottom: 20px;
  right: 20px;
  z-index: 2000;
  user-select: none;
}

.reminder-bubble {
  width: 60px;
  height: 60px;
  background: linear-gradient(135deg, #409EFF 0%, #3a8ee6 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
  transition: all 0.3s ease;
  position: relative;

  &:hover {
    transform: scale(1.1);
    box-shadow: 0 6px 16px rgba(64, 158, 255, 0.4);
  }

  &.pulse {
    animation: pulse 1s infinite;
  }

  &.no-message {
    background: linear-gradient(135deg, #909399 0%, #7d848c 100%);
    box-shadow: 0 4px 12px rgba(144, 147, 153, 0.3);
  }

  .el-icon-bell {
    font-size: 24px;
    color: white;
  }

  .el-badge {
    ::v-deep .el-badge__content {
      border: 2px solid white;
    }
  }
}

@keyframes pulse {
  0% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.1);
  }
  100% {
    transform: scale(1);
  }
}

.message-list-container {
  position: absolute;
  bottom: 80px;
  right: 0;
  width: 380px;
  max-height: 500px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
  overflow: hidden;
  border: 1px solid #e4e7ed;
}

.message-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  border-bottom: 1px solid #e9ecef;

  .title {
    font-weight: 600;
    color: #333;
    font-size: 16px;

    .el-icon-bell {
      margin-right: 8px;
      color: #409EFF;
    }
  }

  .header-actions {
    display: flex;
    align-items: center;
    gap: 8px;

    .el-button {
      padding: 4px 8px;
      color: #409EFF;
    }

    .el-icon-close {
      cursor: pointer;
      font-size: 16px;
      color: #999;
      padding: 4px;
      border-radius: 4px;
      transition: all 0.2s;

      &:hover {
        color: #333;
        background-color: #f0f0f0;
      }
    }
  }
}

.message-list {
  max-height: 360px;
  overflow-y: auto;

  &::-webkit-scrollbar {
    width: 6px;
  }

  &::-webkit-scrollbar-track {
    background: #f1f1f1;
  }

  &::-webkit-scrollbar-thumb {
    background: #c0c4cc;
    border-radius: 3px;
  }
}

.message-item {
  display: flex;
  align-items: flex-start;
  padding: 16px 20px;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: background-color 0.2s;
  gap: 12px;

  &:hover {
    background-color: #f8f9fa;
  }

  &.unread {
    background-color: #f0f8ff;
    border-left: 3px solid #409EFF;

    .message-title {
      font-weight: 600;
    }
  }

  &:last-child {
    border-bottom: none;
  }
}

.message-icon {
  font-size: 18px;
  color: #409EFF;
  margin-top: 2px;
  flex-shrink: 0;
}

.message-content {
  flex: 1;
  min-width: 0;

  .message-title {
    color: #333;
    margin-bottom: 4px;
    font-size: 14px;
    line-height: 1.4;
  }

  .message-text {
    color: #666;
    font-size: 13px;
    line-height: 1.4;
    margin-bottom: 8px;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }

  .message-time {
    color: #999;
    font-size: 12px;
  }
}

.message-actions {
  flex-shrink: 0;
  display: flex;
  align-items: center;

  .read-btn {
    padding: 4px 8px;
    font-size: 12px;
  }

  .read-icon {
    color: #67C23A;
    font-size: 16px;
  }
}

.message-footer {
  padding: 12px 20px;
  text-align: center;
  border-top: 1px solid #f0f0f0;
  background-color: #fafafa;

  .el-button {
    font-size: 13px;
  }
}

.empty-message {
  text-align: center;
  padding: 40px 20px;
  color: #999;

  .el-icon-success {
    font-size: 32px;
    margin-bottom: 8px;
    color: #67C23A;
  }

  p {
    font-size: 14px;
    margin: 0;
  }
}

.popup-messages {
  position: absolute;
  bottom: 80px;
  right: 0;
  width: 340px;
}

.popup-message {
  background: white;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  margin-bottom: 12px;
  cursor: pointer;
  border-left: 4px solid #409EFF;
  transition: all 0.3s ease;
  display: flex;
  overflow: hidden;

  &:hover {
    transform: translateX(-4px);
    box-shadow: 0 6px 16px rgba(0, 0, 0, 0.2);
  }
}

.popup-icon {
  background: #409EFF;
  color: white;
  padding: 16px;
  font-size: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.popup-body {
  flex: 1;
  padding: 12px 16px;
}

.popup-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;

  .popup-title {
    font-weight: 600;
    color: #333;
    font-size: 14px;
  }

  .el-icon-close {
    cursor: pointer;
    color: #999;
    font-size: 14px;
    padding: 2px;

    &:hover {
      color: #333;
    }
  }
}

.popup-content {
  color: #666;
  font-size: 13px;
  line-height: 1.4;
  margin-bottom: 8px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.popup-time {
  color: #999;
  font-size: 11px;
}

.connection-status {
  position: fixed;
  bottom: 100px;
  right: 20px;
  background: #f56c6c;
  color: white;
  padding: 8px 12px;
  border-radius: 6px;
  font-size: 12px;
  display: flex;
  align-items: center;
  gap: 6px;
  z-index: 2001;

  .el-icon-warning {
    font-size: 14px;
  }
}

/* 动画效果 */
.slide-fade-enter-active,
.slide-fade-leave-active {
  transition: all 0.3s ease;
}

.slide-fade-enter,
.slide-fade-leave-to {
  transform: translateY(10px);
  opacity: 0;
}

.popup-enter-active,
.popup-leave-active {
  transition: all 0.3s ease;
}

.popup-enter,
.popup-leave-to {
  transform: translateX(100%);
  opacity: 0;
}

.popup-move {
  transition: transform 0.3s ease;
}
</style>