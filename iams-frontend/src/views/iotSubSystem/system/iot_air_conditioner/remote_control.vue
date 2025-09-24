<template>
  <div class="air-conditioner-container">
    <!-- 遍历遥控器列表 -->
    <div v-for="(remote, index) in remoteList" :key="index" class="remote-wrapper">
      <div class="serial-number">{{ remote.id }}</div>
      <div class="remote">
        <!-- 显示屏 -->
        <div class="display">
          <div class="temp-display" :style="{ color: remote.isPowerOn ? '#333' : '#666' }">
            {{ remote.currentTemp }}℃
          </div>
          <div class="status">
            {{ remote.isPowerOn ? `${remote.modeName}模式` : '已关机' }}
          </div>
          <!-- 风速区域，始终保留空间 -->
          <div class="fan-status" :style="{ visibility: remote.isPowerOn ? 'visible' : 'hidden' }">
            风速：{{ remote.fanSpeedName }}
          </div>
        </div>

        <!-- 按钮区域 -->
        <div class="controls">
          <!-- 温度调节 -->
          <button class="temp-btn" @click="changeTemp(remote, 1)">+</button>
          <button class="power-btn" :class="{ on: remote.isPowerOn }" @click="togglePower(remote)">
            电源
          </button>
          <button class="temp-btn" @click="changeTemp(remote, -1)">-</button>

          <!-- 模式切换 -->
          <button
            v-for="mode in modes"
            :key="mode.value"
            class="mode-btn"
            :class="{ active: remote.currentMode === mode.value }"
            @click="setMode(remote, mode.value)"
          >
            {{ mode.label }}
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
      暂无空调信息
    </div>
  </div>
</template>

<script>
import {
  changeAirTemp,
  listIot_air_conditioner,
  PowerOnOff,
  updateFanSpeed,
  updateMode
} from "@/api/system/iot_air_conditioner";

export default {
  name: 'AirConditionerMain',
  data() {
    return {
      iot_air_conditionerList: [], // 空调列表
      total: 0, // 总数
      remoteList: [], // 遥控器列表
      modes: [
        { value: 'cool', label: '制冷' },
        { value: 'heat', label: '制热' },
        { value: 'dry', label: '除湿' },
      ],
      fanSpeeds: [
        { value: 1, label: '低速' },
        { value: 2, label: '中速' },
        { value: 3, label: '高速' },
      ],
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
    // 获取空调列表
    getList() {
      listIot_air_conditioner({
        pageNum: 0,
        pageSize: 999999,
      }).then(response => {
        console.log("response.rows",response.rows);
        this.iot_air_conditionerList = response.rows;
        this.total = response.total;
        // 初始化遥控器列表
        this.remoteList = this.iot_air_conditionerList.map(item => ({
          id: item.id,
          isPowerOn: false,
          currentTemp: 25,
          currentMode: 'cool',
          currentFanSpeed: 1,
          modeName: '制冷',
          fanSpeedName: '低速',
        }));
      });
    },
    // 开关机
    togglePower(remote) {
      remote.isPowerOn = !remote.isPowerOn;
      PowerOnOff(remote).then(response => {
        console.log("开关机成功", response);
      });
    },
    // 调节温度
    changeTemp(remote, amount) {
      if (!remote.isPowerOn) return;
      remote.currentTemp = Math.min(30, Math.max(16, remote.currentTemp + amount));
      changeAirTemp(remote).then(response => {
        console.log("修改温度成功", response);
      });
    },
    // 设置模式
    setMode(remote, mode) {
      if (!remote.isPowerOn) return;
      remote.currentMode = mode;
      remote.modeName = this.modes.find(m => m.value === mode).label;
      updateMode(remote).then(response => {
        console.log("修改模式成功", response);
      });
    },
    // 设置风速
    setFanSpeed(remote, speed) {
      if (!remote.isPowerOn) return;
      remote.currentFanSpeed = speed;
      remote.fanSpeedName = this.fanSpeeds.find(s => s.value === speed).label;
      updateFanSpeed(remote).then(response => {
        console.log("修改风速成功", response);
      });
    },
  },
};
</script>

<style scoped>
/* 容器样式 */
.air-conditioner-container {
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

/* 序列号样式 */
.serial-number {
  font-size: 14px; /* 缩小字体 */
  font-weight: bold;
  text-align: center;
  color: #00ff9d;
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

.temp-display {
  font-size: 32px; /* 缩小字体 */
  font-weight: bold;
}

.status {
  font-size: 14px; /* 缩小字体 */
  color: #333;
}

.fan-status {
  font-size: 12px; /* 缩小字体 */
  color: #333;
  margin-top: 6px; /* 缩小外边距 */
  height: 18px; /* 固定高度 */
  visibility: hidden; /* 默认隐藏 */
}

.controls {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 6px; /* 缩小间隙 */
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

.power-btn {
  background: #ff4444;
}

.power-btn.on {
  background: #00c851;
}

.mode-btn.active {
  background: #007bff;
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
