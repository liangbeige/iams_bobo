<template>
  <div class="scene-container">
    <!-- 3D内容区域 -->
    <div ref="webglContainer" class="webgl-container"></div>

    <!-- 可拖动的文件面板 -->
    <div
        v-if="showFilePanel"
        class="file-panel"
        ref="filePanel"
        :style="{
        left: panelPosition.x + 'px',
        top: panelPosition.y + 'px',
        position: 'absolute',
      }"
    >
      <div class="file-panel-header" @mousedown="startDrag">
        <h3>档案盒内容</h3>
        <button @click="closeFilePanel">×</button>
      </div>

      <div class="file-list">
        <div
            v-for="(file, index) in currentCabinetFiles"
            :key="index"
            class="file-item"
            @click="setPreviewFile(file)"
            :class="{ active: previewFile && previewFile.name === file.name }"
        >
          <div class="file-icon" :class="file.type">
            {{ getFileIcon(file.type) }}
          </div>
          <div class="file-info">
            <div class="file-name">{{ file.name }}</div>
            <div class="file-meta">{{ file.size }} · {{ file.date }}</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 文件预览区域 -->
    <div v-if="previewFile" class="file-preview">
      <div class="preview-header">
        <h4>{{ previewFile.name }}</h4>
        <button @click="previewFile = null">×</button>
      </div>
      <div class="preview-content">
        <div v-if="previewFile.type === 'pdf'" class="pdf-preview">
          <p>PDF预览区域 (实际项目替换为PDF.js)</p>
        </div>
        <img
            v-else-if="previewFile.type === 'image'"
            :src="previewFile.url"
            alt="预览图片"
            class="preview-image"
        />
        <div v-else class="unsupported-preview">
          {{ getPreviewText(previewFile.type) }}
        </div>
      </div>
    </div>
    <!-- 控制ui按钮 -->
    <div class="control-panel" :style="panelStyle" ref="controlPanel">
      <div v-if="selectedCabinet" class="selection-info">
        已选中: 第{{ currentSelectedGroup + 1 }}列 - 柜子{{
          currentSelectedIndex + 1
        }}
        <div
            class="lock-status"
            :class="{ locked: isColumnLocked(currentSelectedGroup) }"
        >
          当前列状态:
          {{ isColumnLocked(currentSelectedGroup) ? "已锁定" : "未锁定" }}
        </div>
      </div>
      <div class="control-header" @mousedown="startDrag1">
        柜子控制面板
        <span v-if="selectedCabinet" class="close-btn" @click="clearSelection"
        >×</span
        >
      </div>
      <div class="control-buttons">
        <button
            @click="Lock"
            :disabled="!selectedCabinet || isColumnLocked(currentSelectedGroup)"
        >
          锁定
        </button>
        <button
            @click="UnLock"
            :disabled="!selectedCabinet || !isColumnLocked(currentSelectedGroup)"
        >
          解锁
        </button>
        <button
            @click="LeftMove"
            :disabled="!selectedCabinet || isColumnLocked(currentSelectedGroup)"
        >
          左移
        </button>
        <button
            @click="RightMove"
            :disabled="!selectedCabinet || isColumnLocked(currentSelectedGroup)"
        >
          右移
        </button>
        <button @click="StopMove" :disabled="!isMoving">停止</button>
        <button @click="Reset">合架</button>
        <button
            @click="focusOnSelected"
            :disabled="!selectedCabinet"
            class="view-button"
        >
          查看
        </button>
      </div>
      <div v-if="selectedCabinet" class="selection-info">
        <!-- 已选中: 组{{ currentSelectedGroup + 1 }} - 柜子{{
          currentSelectedIndex + 1
        }} -->
        <small>再次点击柜子可取消选择</small>
      </div>
    </div>

    <el-select
        v-if="cabinetConfigList.length > 0"
        v-model="selectedConfigId"
        placeholder="请选择配置"
        style="
        height: 32px;
        width: 240px;
        position: fixed;
        z-index: 9999;
        top: 50px;
        right: 0px;
        background: rgba(255, 255, 255, 0.85);
        box-shadow: 0 2px 10px rgba(0, 0, 0, 0.15);
      "
        size="small"
        @change="onConfigChange"
    >
      <el-option
          v-for="item in cabinetConfigList"
          :key="item.id"
          :label="item.company + ' - ' + item.warehouse"
          :value="item.id"
      />
      <el-option
          v-if="cabinetConfigList.length === 0"
          disabled
          label="没有配置可选"
          value=""
      />
    </el-select>
    <div v-else>加载配置中...</div>
  </div>
</template>

<script>
import { reactive, markRaw } from 'vue';
import * as THREE from "three";
import { OrbitControls } from "three/examples/jsm/controls/OrbitControls.js";
import * as TWEEN from "three/examples/jsm/libs/tween.module.js";
import request from "@/utils/request";
import {
  lockColumn,
  unlockColumn,
  stopMoveColumn,
  resetColumn,
  leftMoveColumn,
  rightMoveColumn,
} from "@/api/system/move";

export default {
  name: "ThreeScene",
  data() {
    return {
      // 拖动相关状态
      isDragging: false,
      panelPosition: { x: 20, y: 20 },
      dragOffset: { x: 0, y: 0 },
      // Three.js 相关
      scene: null,
      camera: null,
      renderer: null,
      controls: null,
      clock: null,
      raycaster: null,
      mouse: null,

      hit: null,
      lastHit: null,
      clickDebounce: null, // 添加防抖函数
      currentFocusGroup: null, // 添加当前焦点组
      originalCameraPos: new THREE.Vector3(2400, 2000, 3600), // 添加初始相机位置
      buildingGroups: [], // 存储四个柜子组
      groupStates: [
        // 每个柜子组独立的状态
        {
          sortedMeshes: [], // 存储排序后的 mesh
          movedMeshes: new Set(), // 存储已移动的 mesh
          isAnimating: false, // 动画状态标志
          originalPositions: new Map(), // 存储原始位置
        },
        {
          sortedMeshes: [],
          movedMeshes: new Set(),
          isAnimating: false,
          originalPositions: new Map(),
        },
        {
          sortedMeshes: [],
          movedMeshes: new Set(),
          isAnimating: false,
          originalPositions: new Map(),
        },
        {
          sortedMeshes: [],
          movedMeshes: new Set(),
          isAnimating: false,
          originalPositions: new Map(),
        },
      ],
      currentCabinetFiles: [],
      previewFile: null,
      showFilePanel: false, // 默认显示面板用于测试

      // 控制状态
      isAnimating: false, // 添加动画状态标志
      isLocked: false,
      isMoving: false,
      currentSelectedGroup: null, // 当前选中的柜子组索引
      currentSelectedIndex: -1, // 当前选中的柜子在组中的索引
      selectedCabinet: null, // 当前选中的柜子对象

      //数据相关
      selectedConfigId: null, // 用户选中的配置项
      cabinetConfig: null,
      configData: null,
      cabinetConfigList: [], // 配置列表数据
      columnLockStates: {}, // 格式: { 'groupIndex': boolean }
      selectedValue: null,
    };
  },
  computed: {
    panelStyle() {
      return {
        left: `${this.panelPosition.x}px`,
        top: `${this.panelPosition.y}px`,
      };
    },
  },

  async mounted() {
    await this.init(); // 等待 init 完成
    // 最终验证
    this.$nextTick(() => {
      console.group("配置加载最终检查");
      console.log("cabinetConfig:", this.cabinetConfig);
      console.log("cabinetConfigList:", this.cabinetConfigList);
      console.log("selectedConfigId:", this.selectedConfigId);

      if (this.cabinetConfig) {
        console.log("当前配置内容:", {
          id: this.cabinetConfig.id,
          company: this.cabinetConfig.company,
          hasConfigData: !!this.cabinetConfig.configData,
          cabinets: this.cabinetConfig.configData?.cabinets?.length,
        });
      }
      console.groupEnd();
    });
    this.animate();

    const prompt = document.createElement("div");
    prompt.id = "esc-prompt";
    prompt.textContent = "按 ESC 返回初始位置";
    prompt.style.position = "absolute";
    prompt.style.top = "20px";
    prompt.style.left = "50%";
    prompt.style.transform = "translateX(-50%)";
    prompt.style.color = "white";
    prompt.style.fontFamily = "Arial, sans-serif";
    prompt.style.fontSize = "16px";
    prompt.style.textShadow = "0 0 5px black";
    prompt.style.zIndex = "1000";
    this.$refs.webglContainer.appendChild(prompt);

    window.addEventListener("resize", this.onWindowResize);
    this.$nextTick(() => {
      this.onWindowResize(); // 初始调用
    });
    window.addEventListener("keydown", this.handleKeydown, false); // 添加 keydown 事件监听

    this.fetchCabinetConfigList();
  },

  methods: {
    // 加载配置列表
    async fetchCabinetConfigList() {
      try {
        const res = await request.get("/system/cabinetConfig/list");

        if (res.code === 200 && res.data) {
          const configs = Array.isArray(res.data) ? res.data : [res.data];

          const processedConfigs = configs.map((item) => {
            const parsedData = this.safeParseConfigData(item.configData);

            // 遍历所有柜体，给每个柜体补 networkConfig
            if (parsedData?.cabinets?.length) {
              parsedData.cabinets.forEach((cabinet) => {
                cabinet.cabinetGroup.forEach((group) => {
                  group.cabinet.forEach((cab) => {
                    // 如果没 networkConfig，则补一个
                    if (!cab.networkConfig) {
                      cab.networkConfig = this.getNetworkConfigForColumn(
                          cab.columnNumber,
                          cabinet.cabinetGroup
                      );
                    }
                  });
                });
              });
            }

            // 继续解析 immobilizationList 等
            const immobilizationList = [];
            try {
              parsedData?.cabinets?.forEach((cabinet) => {
                cabinet.cabinetGroup.forEach((group) => {
                  group.cabinet.forEach((cab) => {
                    immobilizationList.push({
                      columnNumber: cab.columnNumber,
                      immobilization: cab.immobilization,
                    });
                  });
                });
              });
            } catch (e) {
              console.warn(`配置 ${item.id} 的 configData 结构异常`);
            }

            return {
              ...item,
              configData: parsedData,
              immobilizationList,
            };
          });

          // this.$set(this, "cabinetConfigList", processedConfigs);
          this.cabinetConfigList = processedConfigs;

          if (this.cabinetConfigList.length > 0) {
            // this.$set(this, "cabinetConfig", this.cabinetConfigList[0]);
            this.cabinetConfig = this.cabinetConfigList[0];
            this.selectedConfigId = this.cabinetConfigList[0].id;
            this.loadBuildingData();
          }
        }
      } catch (error) {
        console.error("配置加载失败:", error);
        // this.$set(this, "cabinetConfigList", []);
        this.cabinetConfigList = [];
        // this.$set(this, "cabinetConfig", null);
        this.cabinetConfig = null;
      }
    },

    safeParseConfigData(data) {
      if (!data) return null;
      try {
        const parsed = typeof data === "string" ? JSON.parse(data) : data;
        // 验证必须字段
        if (!parsed.cabinets) throw new Error("缺少cabinets字段");
        return parsed;
      } catch (e) {
        console.error("配置解析失败:", e.message, "原始数据:", data);
        return { cabinets: [] }; // 返回安全数据
      }
    },
    // 从柜子数据中提取有效的 networkConfig
    getNetworkConfigForColumn(columnNumber, cabinets) {
      const fixedColumns = cabinets.filter(
          (cab) =>
              cab.immobilization === 1 &&
              cab.networkConfig?.ip &&
              cab.networkConfig?.port
      );
      // 返回最近固定列的配置
      return fixedColumns[0]?.networkConfig;
    },


    onConfigChange(id) {
      const selected = this.cabinetConfigList.find((item) => item.id === id);
      if (selected) {
        this.cabinetConfig = selected;
        this.loadBuildingData(); //  根据新配置重新加载场景
      } else {
        this.$message.error("未找到对应的配置数据");
      }
    },

    onWindowResize() {
      // 1. 获取更新后的窗口尺寸
      const width = this.$el.clientWidth; // 使用组件容器宽度
      const height = this.$el.clientHeight; // 使用组件容器高度

      // 2. 更新相机参数
      this.camera.aspect = width / height;
      this.camera.updateProjectionMatrix();

      // 3. 更新渲染器尺寸（必须设置像素比以防HiDPI屏幕模糊）
      this.renderer.setSize(width, height);
      this.renderer.setPixelRatio(window.devicePixelRatio || 1); // 处理HiDPI屏幕

      // 4. 可选：更新控制器（如OrbitControls）
      if (this.controls) {
        this.controls.enabled = true;
      }

      // 5. 立即触发一次渲染（防止出现空白帧）
      this.renderer.render(this.scene, this.camera);
    },
    // 生成随机文件数据
    generateRandomFiles() {
      const fileTypes = ["pdf", "image", "doc", "xls", "code"];
      const fileIcons = {
        pdf: "📄",
        image: "🖼️",
        doc: "📝",
        xls: "📊",
        code: "💻",
      };
      const fileExtensions = {
        pdf: "pdf",
        image: ["jpg", "png", "gif"],
        doc: "docx",
        xls: "xlsx",
        code: ["js", "json", "py", "java"],
      };
      const sampleFileNames = {
        pdf: ["项目文档", "报告", "合同", "手册"],
        image: ["设计图", "截图", "照片", "图表"],
        doc: ["会议记录", "说明文档", "计划书", "报告"],
        xls: ["预算表", "数据统计", "时间表", "清单"],
        code: ["源代码", "配置文件", "脚本", "模块"],
      };
      // 为每个柜子生成1-5个随机文件
      return Array.from(
          { length: 1 + Math.floor(Math.random() * 5) },
          (_, i) => {
            const type = fileTypes[Math.floor(Math.random() * fileTypes.length)];
            const nameSample = sampleFileNames[type];
            const name =
                nameSample[Math.floor(Math.random() * nameSample.length)] +
                (i > 0 ? ` ${i + 1}` : "");

            let ext = fileExtensions[type];
            if (Array.isArray(ext)) {
              ext = ext[Math.floor(Math.random() * ext.length)];
            }

            const size = (0.5 + Math.random() * 5).toFixed(1) + "MB";
            const date = new Date(
                2025,
                Math.floor(Math.random() * 12),
                Math.floor(Math.random() * 28) + 1
            )
                .toISOString()
                .split("T")[0];

            return {
              name: `${name}.${ext}`,
              type: type,
              size: size,
              date: date,
              url: `/${type}/${name.toLowerCase().replace(/ /g, "-")}.${ext}`,
            };
          }
      );
    },

    startDrag(e) {
      const panel = this.$refs.filePanel;
      if (!panel) {
        console.warn("filePanel DOM 尚未挂载");
        return;
      }

      const startX = e.clientX;
      const startY = e.clientY;
      const startLeft = panel.offsetLeft;
      const startTop = panel.offsetTop;

      const move = (e) => {
        const dx = e.clientX - startX;
        const dy = e.clientY - startY;
        this.panelPosition = {
          x: startLeft + dx,
          y: startTop + dy,
        };
      };

      const up = () => {
        document.removeEventListener("mousemove", move);
        document.removeEventListener("mouseup", up);
      };

      document.addEventListener("mousemove", move);
      document.addEventListener("mouseup", up);
    },
    startDrag1(e) {
      const panel = this.$refs.controlPanel;
      if (!panel) {
        console.warn("controlPanel 未挂载");
        return;
      }

      const startX = e.clientX;
      const startY = e.clientY;
      const startLeft = panel.offsetLeft;
      const startTop = panel.offsetTop;

      const move = (e) => {
        const dx = e.clientX - startX;
        const dy = e.clientY - startY;
        this.panelPosition = {
          x: startLeft + dx,
          y: startTop + dy,
        };
      };

      const up = () => {
        document.removeEventListener("mousemove", move);
        document.removeEventListener("mouseup", up);
      };

      document.addEventListener("mousemove", move);
      document.addEventListener("mouseup", up);
    },

    handleDrag(e) {
      if (!this.isDragging) return;

      // 计算边界限制
      const maxX = window.innerWidth - this.$refs.filePanel.offsetWidth;
      const maxY = window.innerHeight - this.$refs.filePanel.offsetHeight;

      // 计算新位置
      let newX = e.clientX - this.dragOffset.x;
      let newY = e.clientY - this.dragOffset.y;

      // 限制在视窗范围内
      newX = Math.max(0, Math.min(newX, maxX));
      newY = Math.max(0, Math.min(newY, maxY));

      this.panelPosition = { x: newX, y: newY };
    },

    stopDrag() {
      this.isDragging = false;
      document.removeEventListener("mousemove", this.handleDrag);
      document.removeEventListener("mouseup", this.stopDrag);
    },

    getPreviewText(type) {
      return type === "code"
          ? "代码文件预览 (需集成代码编辑器)"
          : "不支持预览此文件类型";
    },

    async init() {
      // 初始化场景、相机、渲染器等
      this.scene = new THREE.Scene();

      this.camera = new THREE.PerspectiveCamera(
          50,
          window.innerWidth / window.innerHeight,
          0.1,
          20000
      );
      this.camera.position.set(2400, 2000, 3600);
      this.camera.lookAt(this.scene.position);

      this.renderer = new THREE.WebGLRenderer({
        antialias: true,
        logarithmicDepthBuffer: true,
      });
      this.renderer.setClearColor("rgb(175,216,250)", 1.0);
      this.renderer.setSize(window.innerWidth, window.innerHeight);
      this.renderer.shadowMap.enabled = true;
      this.renderer.shadowMap.type = THREE.PCFSoftShadowMap;

      this.$refs.webglContainer.appendChild(this.renderer.domElement);

      this.controls = new OrbitControls(this.camera, this.renderer.domElement);
      this.controls.autoRotate = false;
      this.controls.minDistance = 200;
      this.controls.maxDistance = 8000;
      this.controls.maxPolarAngle = Math.PI / 2.01;

      this.clock = new THREE.Clock();
      this.raycaster = new THREE.Raycaster();
      this.mouse = new THREE.Vector2();

      // 添加坐标轴辅助器
      const axesHelper = new THREE.AxesHelper(3000);
      this.scene.add(axesHelper);

      // 添加地面
      const planeGeometry = new THREE.PlaneGeometry(6000, 6000);
      const planeMaterial = new THREE.MeshPhongMaterial({
        color: 0x8080ff,
        specular: 0x4488ee,
        shininess: 12,
      });
      const plane = new THREE.Mesh(planeGeometry, planeMaterial);
      plane.receiveShadow = true;
      plane.rotation.x = -0.5 * Math.PI;
      plane.position.set(0, 0, 0);
      this.scene.add(plane);

      const geometry = new THREE.BoxGeometry(100, 300, 450);
      const material = new THREE.MeshPhongMaterial({
        color: 0x8080ff,
        specular: 0x4488ee,
      });
      const mesh = new THREE.Mesh(geometry, material);
      mesh.position.set(0, 0, 0);
      mesh.updateMatrix();
      mesh.updateMatrixWorld(true);
      this.scene.add(mesh);

      // 初始化柜子组
      for (let i = 0; i < 4; i++) {
        this.buildingGroups[i] = new THREE.Group();
        this.scene.add(this.buildingGroups[i]);
      }

      // 加载建筑物数据
      await this.fetchCabinetConfigList();

      // 添加加载状态管理
      this.isLoading = true;
      console.log("配置列表：", this.cabinetConfigList);

      this.addWalls();

      const directionalLight = new THREE.DirectionalLight(0xffffff, 0.5);
      directionalLight.position.set(0, 1000, 0).normalize();
      this.scene.add(directionalLight);

      const ambientLight = new THREE.AmbientLight(0x505050, 0.2);
      this.scene.add(ambientLight);

      // 绑定鼠标移动事件
      window.addEventListener("mousemove", this.onMouseMove, false);
      // 绑定点击事件
      window.addEventListener("click", this.onClick, false);
    },


    //现有API的解析

    loadBuildingData() {
      const configSource = this.cabinetConfig;

      if (!configSource) {
        this.$message.warning("请先选择一个配置再加载柜体数据");
        console.warn("loadBuildingData 被调用，但当前未选择配置。");
        return;
      }

      if (!configSource.configData?.cabinets) {
        console.error(
            "所选配置缺少有效的 configData.cabinets 字段：",
            configSource
        );
        this.$message.error("配置数据结构错误，无法加载柜体");
        return;
      }

      this.processBuildingData(configSource.configData);
    },

    processBuildingData(data) {
      // 清除现有模型
      this.clearAllCabinets();

      // 验证数据结构
      if (!data.cabinets || !Array.isArray(data.cabinets)) {
        console.error("无效的cabinets数据:", data);
        return;
      }

      // 处理每个仓库
      data.cabinets.forEach((warehouse) => {
        if (!warehouse.cabinetGroup || !Array.isArray(warehouse.cabinetGroup)) {
          console.warn("跳过无效的warehouse:", warehouse);
          return;
        }

        warehouse.cabinetGroup.forEach((group) => {
          this.processCabinetGroup(group);
        });
      });
    },

    processCabinetGroup(group) {
      const groupName = group.groupName || "";
      const groupIndex = parseInt(groupName.replace("Group", "")) - 1;

      if (
          isNaN(groupIndex) ||
          groupIndex < 0 ||
          groupIndex >= this.buildingGroups.length
      ) {
        console.error(`无效的组索引: ${groupIndex} (来自 ${groupName})`);
        return;
      }

      if (!group.cabinet || !Array.isArray(group.cabinet)) {
        console.warn(`组 ${groupName} 没有有效的cabinet数据`);
        return;
      }

      group.cabinet.forEach((cabinet) => {
        if (!cabinet.networkConfig) {
          cabinet.networkConfig = this.getNetworkConfigForColumn(
              cabinet.columnNumber,
              [group]
          );
        }
        this.createCabinet(cabinet, groupIndex);
      });
    },
    // 清除所有现有柜体
    clearAllCabinets() {
      this.buildingGroups.forEach((group) => {
        while (group.children.length) {
          const child = group.children[0];
          child.geometry?.dispose();
          child.material?.dispose();
          group.remove(child);
        }
      });
      this.groupStates.forEach((state) => {
        state.sortedMeshes = [];
        state.movedMeshes.clear();
        state.originalPositions.clear();
      });
    },

    // 创建单个柜体
    createCabinet(cabinetData, groupIndex) {
      const geometry = new THREE.BoxGeometry(
          cabinetData.L,
          cabinetData.H,
          cabinetData.W
      );

      const material = new THREE.MeshBasicMaterial({
        color: this.getCabinetColor(cabinetData.C),
        transparent: true,
        opacity: 0.6,
      });

      const mesh = new THREE.Mesh(geometry, material);
      // 如果是不可移动柜子，添加边框
      if (cabinetData.immobilization === 1) {
        const edges = new THREE.EdgesGeometry(geometry);
        const line = new THREE.LineSegments(
            edges,
            new THREE.LineBasicMaterial({ color: 0xffffff, linewidth: 5 })
        );
        mesh.add(line);
      }
      mesh.position.set(
          cabinetData.X,
          cabinetData.Y + cabinetData.H / 2,
          cabinetData.Z
      );

      // 添加用户数据
      mesh.userData = {
        isCabinet: true,
        cabinetData: cabinetData,
        files: this.generateRandomFiles(),
        fileBoxes: this.generateFileBoxes(cabinetData),
        networkConfig: cabinetData.networkConfig || null, // 直接用补好的networkConfig
      };

      // 添加到场景
      this.buildingGroups[groupIndex].add(mesh);

      // 记录原始位置
      this.groupStates[groupIndex].originalPositions.set(
          mesh,
          mesh.position.clone()
      );

      // 添加到排序列表
      this.groupStates[groupIndex].sortedMeshes.push(mesh);
    },
    getCabinetColor(colorCode) {
      const colorMap = {
        0: 0xffce45, // 黄色
        1: 0xdd5545, // 红色
        2: 0x15bf83, // 绿色
        3: 0x4a90e2, // 蓝色
        default: 0xffffff, // 白色
      };

      return colorMap[colorCode] || colorMap.default;
    },
    // 生成档案盒数据
    generateFileBoxes(cabinetData) {
      if (!cabinetData.layers || cabinetData.layers.length === 0) {
        return [];
      }

      const fileBoxes = [];
      const boxHeight = 100; // 档案盒高度
      const boxWidth = 80; // 档案盒宽度
      const boxDepth = 20; // 档案盒深度

      cabinetData.layers.forEach((layer) => {
        // 计算每层可以放置的档案盒行列数
        const columns = Math.floor(layer.W / boxDepth);
        const rows = Math.floor(layer.H / boxHeight);

        // 计算居中偏移量
        const zOffset = (layer.W - columns * boxDepth) / 2;
        const yOffset = (layer.H - rows * boxHeight) / 2;

        // 在每层生成档案盒
        for (let col = 0; col < columns; col++) {
          for (let row = 0; row < rows; row++) {
            fileBoxes.push({
              position: {
                x: layer.X - 15, // 稍微向前突出
                y: layer.Y + yOffset + row * boxHeight + boxHeight / 2,
                z:
                    layer.Z -
                    layer.W / 2 +
                    zOffset +
                    col * boxDepth +
                    boxDepth / 2,
              },
              size: { w: boxWidth, h: boxHeight, d: boxDepth },
              color: 0x333333, // 档案盒颜色
              files: this.generateRandomFiles(
                  3 + Math.floor(Math.random() * 5)
              ),
            });
          }
        }
      });

      return fileBoxes;
    },

    onMouseMove(event) {
      const rect = this.renderer.domElement.getBoundingClientRect();
      this.mouse.x = ((event.clientX - rect.left) / rect.width) * 2 - 1;
      this.mouse.y = -((event.clientY - rect.top) / rect.height) * 2 + 1;
    },

    onClick(event) {
      if (this.isAnimating) return; // 如果动画正在运行，则不响应点击事件
      // 使用防抖但保持即时响应
      const now = Date.now();
      if (this.lastClickTime && now - this.lastClickTime < 300) return;
      this.lastClickTime = now;

      this.handleClickEvent(event);
    },

    // 关闭文件面板
    closeFilePanel() {
      this.showFilePanel = false;
      this.currentCabinetFiles = [];
      this.previewFile = null;
      //清除当前激活柜子的档案盒
      // if (this.activeCabinet) {
      //   const groupIndex = this.buildingGroups.findIndex((group) =>
      //     group.children.includes(this.activeCabinet)
      //   );
      //   if (groupIndex !== -1) {
      //     this.clearFileBoxes(groupIndex);
      //   }
      // }
    },

    // 预览文件
    setPreviewFile(file) {
      this.previewFile = file;
    },

    getFileIcon(type) {
      switch (type) {
        case "pdf":
          return "📄";
        case "image":
          return "🖼️";
        case "doc":
          return "📝";
        case "xls":
          return "📊";
        default:
          return "📁";
      }
    },


    animate() {
      requestAnimationFrame(this.animate);

      const delta = this.clock.getDelta();
      this.controls.update(delta);
      TWEEN.update(); // 更新 Tween.js 动画

      // 原有的 Raycaster 检测逻辑（用于 buildingGroup 和其他物体）
      this.raycaster.setFromCamera(this.mouse, this.camera);
      const intersects = this.raycaster.intersectObjects(this.scene.children);
      if (intersects.length > 0) {
        if (intersects[0].object != null) {
          this.hit = intersects[0].object;
          if (
              this.hit !== this.lastHit &&
              this.lastHit !== null &&
              this.lastHit !== this.plane
          ) {
            this.lastHit.material.transparent = true;
            this.lastHit.material.opacity = 0.6;
          }
          if (this.hit !== this.plane) {
            this.hit.material.transparent = true;
            this.hit.material.opacity = 1.0;
          }
        }
      }
      this.lastHit = this.hit;

      // 渲染场景
      this.renderer.render(this.scene, this.camera);
    },

    // 添加四周墙壁的函数
    addWalls() {
      const wallMaterial = new THREE.MeshStandardMaterial({
        color: 0xf0f0f0, // 灰白色
        side: THREE.DoubleSide, // 双面渲染
      });
      // 墙壁高度
      const wallHeight = 1000;

      // 前墙
      const frontWall = new THREE.Mesh(
          new THREE.PlaneGeometry(6000, wallHeight),
          wallMaterial
      );
      frontWall.position.set(0, wallHeight / 2, -3000); // 放置在前方
      //frontWall.rotation.x = -0.5 * Math.PI; // 旋转使其直立
      this.scene.add(frontWall);

      // 后墙
      const backWall = new THREE.Mesh(
          new THREE.PlaneGeometry(6000, wallHeight),
          wallMaterial
      );
      backWall.position.set(0, wallHeight / 2, 3000); // 放置在后方
      //backWall.rotation.x = -0.5 * Math.PI; // 旋转使其直立
      this.scene.add(backWall);

      // 左墙
      const leftWall = new THREE.Mesh(
          new THREE.PlaneGeometry(6000, wallHeight),
          wallMaterial
      );
      leftWall.position.set(-3000, wallHeight / 2, 0); // 放置在左侧
      //leftWall.rotation.x = -0.5 * Math.PI; // 旋转使其直立
      leftWall.rotation.y = 0.5 * Math.PI; // 旋转使其面向右侧
      this.scene.add(leftWall);

      // 右墙
      const rightWall = new THREE.Mesh(
          new THREE.PlaneGeometry(6000, wallHeight),
          wallMaterial
      );
      rightWall.position.set(3000, wallHeight / 2, 0); // 放置在右侧
      //rightWall.rotation.x = -0.5 * Math.PI; // 旋转使其直立
      rightWall.rotation.y = -0.5 * Math.PI; // 旋转使其面向左侧
      this.scene.add(rightWall);
    },

    handleKeydown(event) {
      if (event.key === "Escape") {
        // 1. 复位相机
        this.resetCamera(() => {
          // 2. 清除所有柜子的抽屉和档案盒
          // for (let i = 0; i < this.buildingGroups.length; i++) {
          //   this.clearLayersForCabinet(i); // 清除抽屉层
          //   // this.clearFileBoxes(i); // 清除档案盒
          // }

          // 3. 关闭文件面板
          this.closeFilePanel();

          // 4. 重置所有柜子的点击状态
          this.groupStates.forEach((state) => {
            state.sortedMeshes.forEach((mesh) => {
              if (mesh.userData.hasBeenClicked) {
                mesh.userData.hasBeenClicked = false;
              }
            });
          });
        });
      }
    },
    resetCamera(callback = null) {
      if (this.isAnimating) return; // 阻止重复执行
      this.isAnimating = true;

      // 停止所有进行中的动画
      TWEEN.removeAll();

      // 防御性检查
      if (!this.camera || !this.controls) {
        console.error("Camera 或 Controls 未初始化");
        return;
      }

      // 定义焦点位置（可配置为场景中心或其他坐标）
      const focusPoint = new THREE.Vector3(0, 0, 0); // 默认看向场景中心上方500单位

      // 创建并行动画
      new TWEEN.Tween({
        x: this.camera.position.x,
        y: this.camera.position.y,
        z: this.camera.position.z,
        lookX: this.controls.target.x,
        lookY: this.controls.target.y,
        lookZ: this.controls.target.z,
      })
          .to(
              {
                x: this.originalCameraPos.x,
                y: this.originalCameraPos.y,
                z: this.originalCameraPos.z,
                lookX: focusPoint.x,
                lookY: focusPoint.y,
                lookZ: focusPoint.z,
              },
              1500
          )
          .easing(TWEEN.Easing.Quadratic.InOut)
          .onUpdate((obj) => {
            // 同步更新位置和视角
            this.camera.position.set(obj.x, obj.y, obj.z);
            this.controls.target.set(obj.lookX, obj.lookY, obj.lookZ);
            this.controls.update();
          })
          .onStart(() => {
            // 禁用交互
            this.controls.enabled = false;
            this.isAnimating = true;
          })
          .onComplete(() => {
            // 恢复交互
            this.controls.enabled = true;
            this.isAnimating = false;
            // 强制精确位置
            this.camera.position.copy(this.originalCameraPos);
            this.controls.target.copy(focusPoint);

            //复原所有拉出的档案盒
            this.buildingGroups.forEach((group) => {
              group.children.forEach((child) => {
                if (child.userData?.isFileBox && child.userData.isPulledOut) {
                  // 复原位置
                  child.position.set(
                      child.userData.originalPosition.x,
                      child.userData.originalPosition.y,
                      child.userData.originalPosition.z
                  );
                  child.userData.isPulledOut = false; // 标记为未拉出
                }
              });
            });
            // //清除所有档案盒
            // for (let i = 0; i < this.buildingGroups.length; i++) {
            //   this.clearFileBoxes(i);
            // }

            // 确保回调执行且完成
            Promise.resolve().then(() => {
              callback && callback();
              this.renderer.render(this.scene, this.camera); // 强制更新
            });
          })
          .start();
    },

    focusOnSelected() {
      if (this.selectedCabinet) {
        this.focusOnObject(this.selectedCabinet);
      }
    },

    // 优化后的聚焦方法
    focusOnObject(object) {
      if (!object?.geometry) {
        console.warn("无效的聚焦对象");
        return;
      }

      TWEEN.removeAll(); // 清除现有动画

      // 计算最佳观察位置
      const bbox = new THREE.Box3().setFromObject(object);
      const size = new THREE.Vector3();
      bbox.getSize(size);

      const targetPosition = new THREE.Vector3(
          object.position.x - size.x * 5.5, // 向左偏移（X轴负方向）
          object.position.y + size.y / 2, // 垂直居中
          object.position.z - size.z * 0.5 // 稍微后退一点（更好观察）
      );

      // );

      // 相机移动动画
      new TWEEN.Tween(this.camera.position)
          .to(targetPosition, 800)
          .easing(TWEEN.Easing.Quadratic.InOut)
          .onUpdate(() => {
            this.camera.lookAt(object.position);
          })
          .onComplete(() => {
            this.controls.target.copy(object.position);
          })
          .start();

      // 控制目标点动画
      new TWEEN.Tween(this.controls.target).to(object.position, 800).start();
    },

    handleClickEvent(event) {
      if (this.isAnimating) return;

      // 更新鼠标位置
      const rect = this.renderer.domElement.getBoundingClientRect();
      this.mouse.x = ((event.clientX - rect.left) / rect.width) * 2 - 1;
      this.mouse.y = -((event.clientY - rect.top) / rect.height) * 2 + 1;
      this.raycaster.setFromCamera(this.mouse, this.camera);

      // 检测柜子点击
      let closestIntersect = null;
      let groupIndex = -1;

      this.buildingGroups.forEach((group, idx) => {
        if (this.groupStates[idx].isAnimating) return;

        const intersects = this.raycaster.intersectObjects(group.children);
        if (intersects.length > 0) {
          const closest = intersects[0];
          if (
              !closestIntersect ||
              closest.distance < closestIntersect.distance
          ) {
            closestIntersect = closest;
            groupIndex = idx;
          }
        }
      });

      if (closestIntersect && groupIndex !== -1) {
        const clickedMesh = closestIntersect.object;
        // 获取该列的 immobilization 信息
        const cabinetData = clickedMesh.userData.cabinetData; // 获取柜子数据
        const columnNumber = cabinetData.columnNumber;
        const immobilizationList = this.cabinetConfig.immobilizationList || []; // 获取当前配置的 immobilization 列表
        const immobilization = immobilizationList.find(
            (item) => item.columnNumber === columnNumber
        )?.immobilization;

        // 如果该列是固定列，不进行任何操作，弹出提示
        if (immobilization === 1) {
          alert(`该列为固定列，不可进行移动操作`);
          return; // 退出，不做其他操作
        }

        // 检查是否是点击了已选中的柜子
        if (
            this.selectedCabinet &&
            this.selectedCabinet.uuid === clickedMesh.uuid
        ) {
          // 二次点击相同柜子，取消选择
          this.resetCabinetAppearance(clickedMesh);
          this.selectedCabinet = null;
          return;
        }

        // 清除之前的高亮
        if (this.selectedCabinet) {
          this.resetCabinetAppearance(this.selectedCabinet);
        }

        // 设置新的选中柜子
        this.selectedCabinet = clickedMesh;
        this.currentSelectedGroup = groupIndex;
        this.currentSelectedIndex = this.groupStates[
            groupIndex
            ].sortedMeshes.findIndex((mesh) => mesh.uuid === clickedMesh.uuid);

        // 高亮新选中的柜子
        this.highlightSelectedCabinet(clickedMesh);

        return;
      }

      // 点击空白处，取消选择
      if (this.selectedCabinet) {
        this.resetCabinetAppearance(this.selectedCabinet);
        this.selectedCabinet = null;
      }
    },

    // 高亮选中的柜子

    highlightSelectedCabinet(cabinet) {
      // 清除高亮
      this.clearHighlights();

      // 第一次点击时保存原始材质和透明度
      if (!cabinet.userData.originalMaterial) {
        cabinet.userData.originalMaterial = cabinet.material;
        cabinet.userData.originalOpacity = cabinet.material.opacity;
      }

      // 创建一个新的高亮材质（不要覆盖 userData 中的原始）
      const highlightMaterial = new THREE.MeshPhongMaterial({
        color: cabinet.userData.originalMaterial.color,
        transparent: true,
        opacity: 0.8,
        specular: 0xffffff,
        shininess: 30,
      });

      // 应用高亮材质
      cabinet.material = highlightMaterial;
    },

    // 重置柜子外观
    resetCabinetAppearance(cabinet) {
      if (cabinet && cabinet.userData.originalMaterial) {
        cabinet.material = cabinet.userData.originalMaterial;
        cabinet.material.opacity = cabinet.userData.originalOpacity ?? 1;
        cabinet.material.transparent = true;
        cabinet.material.needsUpdate = true;
      }
    },

    // 清除所有高亮
    clearHighlights() {
      this.buildingGroups.forEach((group) => {
        group.children.forEach((child) => {
          this.resetCabinetAppearance(child);
        });
      });
    },

    // 清除选择
    clearSelection() {
      this.clearHighlights();
      this.selectedCabinet = null;
      this.currentSelectedGroup = null;
      this.currentSelectedIndex = -1;
    },
    async Lock() {
      // 1. 检查当前选中分组
      if (!this.selectedCabinet?.userData?.cabinetData) {
        this.$message.error("请先选择有效的柜子");
        return;
      }

      // 2. 显示即时反馈
      const loadingInstance = this.$loading({
        lock: true,
        text: "正在锁定柜子...",
        spinner: "el-icon-loading",
      });

      try {
        // 3. 更新本地锁定状态（立即响应）
        // this.$set(this.columnLockStates, this.currentSelectedGroup, true);
        this.columnLockStates[this.currentSelectedGroup] = true;

        // 4. 并行执行锁定指令和UI更新
        await Promise.all([
          this.executeMoveCommand(), // 提取请求逻辑
          this.resetCabinetAppearance(this.selectedCabinet),
        ]);

        // 5. 成功提示（优化显示时间）
        this.$message({
          message: "柜子锁定成功",
          type: "success",
          duration: 1500,
          showClose: true,
        });
      } catch (error) {
        console.error("锁定失败:", error);

        // 6. 失败时回滚状态
        // this.$set(this.columnLockStates, this.currentSelectedGroup, false);
        this.columnLockStates[this.currentSelectedGroup] = false;

        this.$message({
          message: error.message || "锁定指令发送失败",
          type: "error",
          duration: 2000,
          showClose: true,
        });
      } finally {
        // 7. 确保加载状态关闭
        loadingInstance.close();
      }
    },


    async UnLock() {
      // 1. 检查当前选中分组
      if (!this.selectedCabinet?.userData?.cabinetData) {
        this.$message.error("请先选择有效的柜子");
        return;
      }

      // 2. 显示即时反馈
      const loadingInstance = this.$loading({
        lock: true,
        text: "正在解锁柜子...",
        spinner: "el-icon-loading",
      });

      try {
        // 3. 更新本地锁定状态（立即响应）
        // this.$set(this.columnLockStates, this.currentSelectedGroup, true);
        this.columnLockStates[this.currentSelectedGroup] = true;

        // 4. 并行执行锁定指令和UI更新
        await Promise.all([
          this.executeMoveCommand(), // 提取请求逻辑
          this.resetCabinetAppearance(this.selectedCabinet),
        ]);

        // 5. 成功提示（优化显示时间）
        this.$message({
          message: "柜子解锁成功",
          type: "success",
          duration: 1500,
          showClose: true,
        });
      } catch (error) {
        console.error("解锁失败:", error);

        // 6. 失败时回滚状态
        // this.$set(this.columnLockStates, this.currentSelectedGroup, false);
        this.columnLockStates[this.currentSelectedGroup] = false;

        this.$message({
          message: error.message || "解锁指令发送失败",
          type: "error",
          duration: 2000,
          showClose: true,
        });
      } finally {
        // 7. 确保加载状态关闭
        loadingInstance.close();
      }
    },

    // 检查指定列是否锁定
    isColumnLocked(groupIndex) {
      return !!this.columnLockStates[groupIndex];
    },

    async LeftMove() {
      // 检查选中柜子
      if (!this.selectedCabinet?.userData?.cabinetData) {
        this.$message.error("请先选择有效的柜子");
        return;
      }

      // 显示即时反馈（立即显示）
      const loadingInstance = this.$loading({
        lock: true,
        text: "正在发送左移指令...",
        spinner: "el-icon-loading",
      });

      try {
        this.isMoving = true;

        // 并行执行动画和网络请求
        await Promise.all([
          this.animateCabinetMove(-300),
          this.executeMoveCommand(), // 提取请求逻辑
        ]);

        this.$message.success("左移完成", { duration: 1500 }); // 缩短提示时间
      } catch (error) {
        console.error("左移失败:", error);
        this.$message.error(error.message || "指令发送失败", {
          duration: 2000,
          showClose: true, // 允许手动关闭
        });
      } finally {
        loadingInstance.close();
        this.isMoving = false;
      }
    },

    async RightMove() {
      // 检查选中柜子
      if (!this.selectedCabinet?.userData?.cabinetData) {
        this.$message.error("请先选择有效的柜子");
        return;
      }

      // 显示即时反馈（立即显示）
      const loadingInstance = this.$loading({
        lock: true,
        text: "正在发送右移指令...",
        spinner: "el-icon-loading",
      });

      try {
        this.isMoving = true;

        // 并行执行动画和网络请求
        await Promise.all([
          this.animateCabinetMove(300),
          this.executeMoveCommand(), // 提取请求逻辑
        ]);

        this.$message.success("右移完成", { duration: 1500 }); // 缩短提示时间
      } catch (error) {
        console.error("右移失败:", error);
        this.$message.error(error.message || "指令发送失败", {
          duration: 2000,
          showClose: true, // 允许手动关闭
        });
      } finally {
        loadingInstance.close();
        this.isMoving = false;
      }
    },

    async executeMoveCommand(action = "RightMove") {
      // 1. 强化空值检查
      if (!this.selectedCabinet?.userData?.networkConfig?.ip) {
        const columnNumber =
            this.selectedCabinet?.userData?.cabinetData?.columnNumber;

        // 尝试动态获取配置
        const fallbackConfig = this.getNetworkConfigForColumn(
            columnNumber,
            this.cabinetConfig?.configData?.cabinets?.[0]?.cabinetGroup?.[0]
                ?.cabinet || []
        );

        if (!fallbackConfig?.ip) {
          throw new Error(`柜子 ${columnNumber} 无有效网络配置`);
        }
        this.selectedCabinet.userData.networkConfig = fallbackConfig; // 缓存配置
      }

      // 2. 解构网络配置
      const { ip, port } = this.selectedCabinet.userData.networkConfig;
      if (!ip || !port) throw new Error("网络配置不完整");

      // 3. 获取列号
      const columnNumber =
          this.selectedCabinet.userData.cabinetData.columnNumber;

      // 4. 创建AbortController和超时控制
      const controller = new AbortController();
      const timeoutId = setTimeout(() => controller.abort(), 8000);

      try {
        let apiResponse;

        // 根据不同的action调用对应的API
        switch (action) {
          case "RightMove":
            apiResponse = await rightMoveColumn(ip, port, columnNumber, {
              signal: controller.signal,
            });
            break;
          case "LeftMove":
            apiResponse = await leftMoveColumn(ip, port, columnNumber, {
              signal: controller.signal,
            });
            break;
          case "Lock":
            apiResponse = await lockColumn(ip, port, {
              signal: controller.signal,
            });
            break;
          case "UnLock":
            apiResponse = await unlockColumn(ip, port, {
              signal: controller.signal,
            });
            break;
          case "StopMove":
            apiResponse = await stopMoveColumn(ip, port, {
              signal: controller.signal,
            });
            break;
          case "Reset":
            apiResponse = await resetColumn(ip, port, {
              signal: controller.signal,
            });
            break;
          default:
            throw new Error(`不支持的操作类型: ${action}`);
        }

        // 统一检查响应
        const response = apiResponse.response || apiResponse.data;
        if (response !== "1") throw new Error("设备返回失败状态");

        return {
          success: true,
          response: response,
          action: action,
          columnNo: columnNumber,
        };
      } catch (error) {
        console.error(`${action}操作失败:`, error);
        throw new Error(`${action}操作失败: ${error.message}`);
      } finally {
        clearTimeout(timeoutId);
      }
    },

    // 柜子移动动画
    animateCabinetMove(distance, callback) {
      if (!this.selectedCabinet) return;

      const targetPosition = new THREE.Vector3(
          this.selectedCabinet.position.x + distance,
          this.selectedCabinet.position.y,
          this.selectedCabinet.position.z
      );

      new TWEEN.Tween(this.selectedCabinet.position)
          .to(targetPosition, 1000)
          .easing(TWEEN.Easing.Quadratic.InOut)
          .onComplete(() => {
            if (callback) callback();
          })
          .start();
    },

    // 停止动画

    async StopMove() {
      // 1. 检查选中柜子
      if (!this.selectedCabinet?.userData?.cabinetData) {
        this.$message.error("请先选择有效的柜子");
        return;
      }

      // 2. 显示即时反馈
      const loadingInstance = this.$loading({
        lock: true,
        text: "正在停止移动...",
        spinner: "el-icon-loading",
      });

      try {
        // 3. 先停止动画（同步操作）
        TWEEN.removeAll();
        this.isMoving = false;

        // 4. 发送停止指令
        await this.executeMoveCommand();

        // 5. 显示操作结果
        this.$message({
          message: "已停止移动",
          type: "success",
          duration: 1500,
        });
      } catch (error) {
        console.error("停止失败:", error);
        this.$message.error(error.message || "停止指令发送失败", {
          duration: 2000,
          showClose: true,
        });
      } finally {
        // 6. 清理状态
        loadingInstance.close();
        this.clearHighlights();
      }
    },

    async Reset() {
      const loadingInstance = this.$loading({
        lock: true,
        text: "正在复位所有柜子...",
        spinner: "el-icon-loading",
      });

      try {
        TWEEN.removeAll();

        const resetTasks = [];
        let hasError = false;

        for (
            let groupIndex = 0;
            groupIndex < this.buildingGroups.length;
            groupIndex++
        ) {
          if (this.isColumnLocked(groupIndex)) continue;

          const cabinets = this.groupStates[groupIndex].sortedMeshes;

          for (const cabinet of cabinets) {
            const columnNumber = cabinet.userData?.cabinetData?.columnNumber;
            const ip = cabinet.userData?.networkConfig?.ip;
            const port = cabinet.userData?.networkConfig?.port;
            const originalPos =
                this.groupStates[groupIndex].originalPositions.get(cabinet);

            if (!ip || !port || !columnNumber || !originalPos) continue;

            const task = new Promise((resolve) => {
              // 将 timeoutId 声明提升到这里
              let timeoutId;

              new TWEEN.Tween(cabinet.position)
                  .to(originalPos, 800)
                  .easing(TWEEN.Easing.Quadratic.InOut)
                  .onComplete(async () => {
                    try {
                      const controller = new AbortController();
                      timeoutId = setTimeout(() => controller.abort(), 8000);

                      const res = await resetColumn(ip, port, {
                        signal: controller.signal,
                      });

                      if (res?.data !== "1") {
                        console.warn(
                            `柜子 ${columnNumber} 复位失败，设备返回状态:`,
                            res?.data
                        );
                        hasError = true;
                      }
                    } catch (error) {
                      console.warn(
                          `柜子 ${columnNumber} 复位异常:`,
                          error.message
                      );
                      hasError = true;
                    } finally {
                      clearTimeout(timeoutId);
                      resolve();
                    }
                  })
                  .start();
            });

            resetTasks.push(task);
          }
        }

        await Promise.all(resetTasks);

        if (hasError) {
          this.$message({
            message: "复位完成，但部分柜子出现异常",
            type: "warning",
            duration: 2000,
            showClose: true,
          });
        } else {
          this.$message({
            message: "所有柜子复位完成",
            type: "success",
            duration: 1500,
          });
        }

        this.clearSelection();
      } catch (error) {
        console.error("复位过程中出现错误:", error);
        this.$message.error(error.message || "复位过程中出现异常", {
          duration: 2000,
          showClose: true,
        });
      } finally {
        loadingInstance.close();
        this.clearHighlights();
      }
    },
  },

  beforeDestroy() {
    // 移除事件监听器
    window.removeEventListener("mousemove", this.onMouseMove, false);
    window.removeEventListener("click", this.onClick, false);
    window.removeEventListener("keydown", this.handleKeydown, false); // 移除 keydown 事件监听
    window.removeEventListener("resize", this.onWindowResize); // 移除 resize 事件监听
    // 移除控制器
    if (this.controls) {
      this.controls.dispose();
    }
    const prompt = document.getElementById("esc-prompt");
    if (prompt) prompt.remove();
  },
};
</script>

<style scoped>
.scene-container {
  position: relative;
  width: 100%;
  height: 100vh;
}

.webgl-container {
  width: 100%;
  height: 100vh;
  margin: 0;
  padding: 0;
  overflow: hidden;
}

/* 文件面板样式 */
.file-panel {
  position: absolute;
  width: 350px;
  height: 500px;
  background: rgba(255, 255, 255, 0.97);
  border-radius: 12px;
  box-shadow: 0 4px 30px rgba(0, 0, 0, 0.15);
  display: flex;
  flex-direction: column;
  z-index: 1000;
  overflow: hidden;
  backdrop-filter: blur(5px);
  border: 1px solid rgba(255, 255, 255, 0.3);
  transition: transform 0.2s, box-shadow 0.2s;
}

.file-panel:hover {
  box-shadow: 0 8px 40px rgba(0, 0, 0, 0.2);
  transform: translateY(-2px);
}

.file-panel-header {
  padding: 14px 16px;
  background: linear-gradient(135deg, #4a90e2, #63b3ff);
  color: white;
  display: flex;
  justify-content: space-between;
  align-items: center;
  cursor: grab;
  user-select: none;
}

.file-panel-header:active {
  cursor: grabbing;
  background: linear-gradient(135deg, #3a7bc8, #4a90e2);
}

.file-list {
  flex: 1;
  overflow-y: auto;
  padding: 10px;
  background: rgba(245, 247, 250, 0.8);
}

.file-item {
  display: flex;
  padding: 10px;
  margin-bottom: 8px;
  border-radius: 6px;
  background: white;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  transition: all 0.2s;
  cursor: pointer;
}

.file-item:hover {
  background: #f0f7ff;
  transform: translateX(2px);
}

.file-item.active {
  background: #e1f0ff;
  border-left: 3px solid #4a90e2;
}

.file-icon {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 12px;
  font-size: 18px;
}

.file-info {
  flex: 1;
  min-width: 0;
}

.file-name {
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.file-meta {
  font-size: 12px;
  color: #666;
  margin-top: 2px;
}

.file-preview {
  border-top: 1px solid #eee;
  height: 40%;
  display: flex;
  flex-direction: column;
  background: white;
}

.preview-header {
  padding: 12px 16px;
  border-bottom: 1px solid #eee;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.preview-content {
  flex: 1;
  padding: 15px;
  overflow: auto;
  display: flex;
  align-items: center;
  justify-content: center;
}

.preview-image {
  max-width: 100%;
  max-height: 100%;
  border-radius: 4px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.unsupported-preview {
  color: #666;
  text-align: center;
  padding: 20px;
}

.pdf-preview {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f5f5;
  color: #444;
  font-style: italic;
}

/* 控制面板样式 */
.control-panel {
  position: absolute;
  width: 220px;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 8px;
  padding: 10px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.2);
  z-index: 100;
  user-select: none;
}

.control-header {
  padding: 8px;
  background: #4a90e2;
  color: white;
  border-radius: 5px;
  margin-bottom: 12px;
  cursor: move;
  text-align: center;
  font-weight: bold;
}

.control-buttons {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 8px;
  margin-bottom: 10px;
}

.control-buttons button {
  padding: 8px;
  cursor: pointer;
  background: #f5f5f5;
  border: 1px solid #ddd;
  border-radius: 4px;
  transition: all 0.2s;
}

.control-buttons button:hover:not(:disabled) {
  background: #e0e0e0;
}

.control-buttons button:disabled {
  background: #f0f0f0;
  color: #aaa;
  cursor: not-allowed;
}

.selection-info {
  padding: 6px;
  background: #f8f8f8;
  border-radius: 4px;
  font-size: 14px;
  text-align: center;
  color: #333;
}
.close-btn {
  float: right;
  cursor: pointer;
  font-size: 18px;
  line-height: 1;
  padding: 0 5px;
}

.close-btn:hover {
  color: #ff4444;
}

.selection-info small {
  color: #666;
  font-size: 12px;
}
/* 添加透明度过渡效果 */
.file-panel {
  transition: opacity 0.3s ease;
}

.control-panel {
  transition: all 0.3s ease;
}
.lock-status {
  padding: 4px;
  margin-top: 5px;
  border-radius: 3px;
  font-size: 12px;
}

.lock-status.locked {
  background-color: #ffebee;
  color: #c62828;
}

.lock-status:not(.locked) {
  background-color: #e8f5e9;
  color: #2e7d32;
}
.view-button {
  background-color: #4a90e2;
  color: black;
}

.view-button:hover:not(:disabled) {
  background-color: #3a7bc8;
}

.view-button:disabled {
  background-color: #b0bec5;
  cursor: not-allowed;
}
</style>
