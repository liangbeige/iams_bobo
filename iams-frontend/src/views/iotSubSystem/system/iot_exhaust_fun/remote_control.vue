<template>
  <div class="exhaust-fan-container">
    <!-- 遍历遥控器列表 -->
    <div v-for="(remote, index) in remoteList" :key="index" class="remote-wrapper">
      <!-- 在遥控器外显示 deviceId -->
      <div class="device-id">
        <span v-for="(char, charIndex) in remote.deviceId" :key="charIndex" :style="{ color: getColor(charIndex) }">
          {{ char }}
        </span>
      </div>
      <div class="remote">
        <!-- 显示屏 -->
        <div class="display">
          <div class="status">
            {{ remote.isPowerOn ? '运行中' : '已关机' }}
          </div>
          <!-- 风速区域，始终保留空间 -->
          <div class="fan-status" :style="{ visibility: remote.isPowerOn ? 'visible' : 'hidden' }">
            风速：{{ remote.fanSpeedName }}
          </div>
        </div>

        <!-- 按钮区域 -->
        <div class="controls">
          <!-- 开关机按钮 -->
          <button class="power-btn" :class="{ on: remote.isPowerOn }" @click="togglePower(remote)">
            {{ remote.isPowerOn ? '关机' : '开机' }}
          </button>

          <!-- 风速调节 -->
          <button
            v-for="speed in fanSpeeds"
            :key="speed.value"
            class="fan-btn"
            :class="{ active: remote.currentFanSpeed === speed.value }"
            @click="setFanSpeed(remote, speed.value)"
          >
            {{ speed.label }}
          </button>
        </div>
      </div>
    </div>

    <!-- 占位符 -->
    <div v-for="index in placeholderCount" :key="'placeholder-' + index" class="placeholder">
      暂无抽风机信息
    </div>
  </div>
</template>

<script>
import {FunPowerOnOff, listIot_exhaust_fun, updateFanSpeed} from "@/api/system/iot_exhaust_fun";

export default {
  name: 'ExhaustFanMain',
  data() {
    return {
      iot_exhaust_funList: [], // 抽风机列表
      total: 0, // 总数
      remoteList: [], // 遥控器列表
      fanSpeeds: [
        { value: 1, label: '低速' },
        { value: 2, label: '中速' },
        { value: 3, label: '高速' },
      ],
      colors: ['#ff0000', '#00ff00', '#0000ff', '#ff00ff', '#ffff00', '#00ffff'], // 颜色数组
    };
  },
  computed: {
    // 计算占位符数量
    placeholderCount() {
      const remainder = this.remoteList.length % 5;
      return remainder === 0 ? 0 : 5 - remainder;
    },
  },
  created() {
    this.getList();
  },
  methods: {
    // 获取抽风机列表
    getList() {
      listIot_exhaust_fun({
        pageNum: 0,
        pageSize: 999999,
      }).then(response => {
        this.iot_exhaust_funList = response.rows;
        this.total = response.total;
        // 初始化遥控器列表
        this.remoteList = this.iot_exhaust_funList.map(item => ({
          deviceId: item.deviceId, // 添加 deviceId
          id: item.id,
          isPowerOn: false,
          currentFanSpeed: 1,
          fanSpeedName: '低速',
        }));
      });
    },
    // 开关机
    togglePower(remote) {
      remote.isPowerOn = !remote.isPowerOn;
      FunPowerOnOff(remote).then(response => {
        console.log("开关机成功", response);
      });
    },
    // 设置风速
    setFanSpeed(remote, speed) {
      if (!remote.isPowerOn) return;
      remote.currentFanSpeed = speed;
      remote.fanSpeedName = this.fanSpeeds.find(s => s.value === speed).label;
      updateFanSpeed(remote).then(response => {
        console.log("风扇修改风速成功", response);
      });
    },
    // 获取字母颜色
    getColor(index) {
      return this.colors[index % this.colors.length];
    },
  },
};
</script>

<style scoped>
/* 容器样式 */
.exhaust-fan-container {
  display: flex;
  flex-wrap: wrap;
  gap: 8px; /* 缩小间隙 */
  padding: 8px; /* 缩小内边距 */
  background-color: white;
}

/* 遥控器外层样式 */
.remote-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px; /* 缩小间隙 */
  padding: 8px; /* 缩小内边距 */
  border: 1px solid #ccc;
  border-radius: 8px; /* 缩小圆角 */
  background-color: black;
  width: 200px; /* 缩小宽度 */
}

/* deviceId 样式 */
.device-id {
  font-size: 16px; /* 字体稍大 */
  font-weight: bold;
  text-align: center;
  margin-bottom: 8px; /* 与遥控器的间距 */
}

.device-id span {
  display: inline-block;
  animation: flow 2s infinite;
}

@keyframes flow {
  0% {
    filter: hue-rotate(0deg);
  }
  100% {
    filter: hue-rotate(360deg);
  }
}

/* 遥控器样式 */
.remote {
  background: #333;
  padding: 12px; /* 缩小内边距 */
  border-radius: 10px; /* 缩小圆角 */
  width: 180px; /* 缩小宽度 */
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.3); /* 缩小阴影 */
}

.display {
  background: #a0d8ef;
  padding: 8px; /* 缩小内边距 */
  border-radius: 8px; /* 缩小圆角 */
  text-align: center;
  margin-bottom: 12px; /* 缩小外边距 */
}

.status {
  font-size: 18px; /* 字体稍大 */
  color: #333;
}

.fan-status {
  font-size: 14px; /* 字体稍大 */
  color: #333;
  margin-top: 6px; /* 缩小外边距 */
  height: 18px; /* 固定高度 */
  visibility: hidden; /* 默认隐藏 */
}

.controls {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px; /* 缩小间隙 */
}

button {
  padding: 8px; /* 缩小内边距 */
  border: none;
  border-radius: 6px; /* 缩小圆角 */
  background: #666;
  color: white;
  font-size: 14px; /* 缩小字体 */
  cursor: pointer;
  transition: 0.3s;
}

button:hover {
  background: #888;
}

/* 开关机按钮样式 */
.power-btn {
  width: 60px; /* 圆形按钮 */
  height: 60px; /* 圆形按钮 */
  border-radius: 50%; /* 圆形 */
  background: #ff4444;
  font-size: 16px; /* 字体稍大 */
}

.power-btn.on {
  background: #00c851;
}

/* 风速按钮样式 */
.fan-btn {
  width: 80px; /* 固定宽度 */
}

.fan-btn.active {
  background: #ff8800;
}

/* 占位符样式 */
.placeholder {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 180px; /* 缩小宽度 */
  height: 150px; /* 缩小高度 */
  border: 1px dashed #ccc;
  border-radius: 8px; /* 缩小圆角 */
  background-color: #f0f0f0;
  color: #666;
  font-size: 14px; /* 缩小字体 */
}
</style>
