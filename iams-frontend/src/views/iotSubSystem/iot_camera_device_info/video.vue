<template>
  <div class="monitor-container" id="showVideo">
    <!-- 使用 v-for 循环生成视频 -->
    <div v-for="(camera, index) in cameraList" :key="index" class="video-container">
      <!-- 摄像头信息 -->
      <div class="camera-info">
        <span class="camera-name">{{ camera.installationLocation }}</span>
        <span class="camera-status" :class="{'online': camera.isNormal === 1, 'offline': camera.isNormal === 0}">
          <!--{{ 'online': camera.isNormal === 1, 'offline': camera.isNormal === 0 }}-->
        </span>
      </div>
      <!-- 视频 -->
      <video v-show="camera.isNormal===1" src="./oceans.mp4" loop autoplay controls muted class="video"></video>
    </div>
  </div>
</template>

<script>
import { listIot_camera_device_info } from "@/api/system/iot_camera_device_info";

export default {
  name: 'RtspPlayer',
  data() {
    return {
      cameraList: []
    };
  },
  mounted() {
    this.queryAllCamera();
  },
  methods: {
    /**
     * 查询所有摄像头信息
     */
    queryAllCamera() {
      // 查询全部
      listIot_camera_device_info({ pageNum: 1, pageSize: 1000 }).then(response => {
        console.log(response.rows)
        this.cameraList = response['rows']
      });
    }
  }
};
</script>

<style scoped lang="scss">
.monitor-container {
  display: grid;
  grid-template-columns: repeat(5, 1fr); // 每行5个
  grid-gap: 10px; // 视频之间的间距
  padding: 10px; // 容器内边距
  background-color: #1a1a1a; // 背景颜色
  height: 100vh; // 全屏高度
  overflow: auto; // 防止滚动条
}

.video-container {
  position: relative;
  width: 100%;
  padding-top: 100%; // 1:1 宽高比，实现方形
  background-color: #333; // 视频容器的背景颜色
  border-radius: 12px; // 圆角
  overflow: hidden; // 隐藏超出部分
  border: 2px solid #444; // 边框颜色
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.3); // 阴影效果
  transition: transform 0.3s ease, box-shadow 0.3s ease; // 平滑过渡
}

.camera-info {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  padding: 8px;
  background-color: rgba(0, 0, 0, 0.7); // 半透明背景
  color: #fff;
  font-size: 14px;
  z-index: 1;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-top-left-radius: 10px;
  border-top-right-radius: 10px;
}

.camera-name {
  font-weight: bold;
}

.camera-status {
  font-size: 12px;
  padding: 2px 6px;
  border-radius: 4px;
}

.online {
  background-color: #28a745; // 在线状态背景颜色
}

.offline {
  background-color: #dc3545; // 离线状态背景颜色
}

.video {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover; // 保持视频比例，填充整个容器
  border-radius: 10px; // 圆角
}

/* 鼠标悬停效果 */
.video-container:hover {
  transform: scale(1.05); // 放大效果
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.5); // 悬停时阴影加深
  border-color: #666; // 悬停时边框颜色变化
}
</style>
