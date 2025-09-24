<template>
  <div class="scene-container">
    <!-- 3D内容区域 -->
    <div ref="webglContainer" class="webgl-container"></div>

    <!-- 可拖动的文件面板 -->
    <div
      v-if="state.showFilePanel"
      class="file-panel"
      ref="filePanel"
      :style="filePanelStyle"
      :ref="filePanel"
    >
      <div class="file-panel-header" @mousedown="startDrag">
        <h3>档案柜内容</h3>
        <button @click="closeFilePanel">×</button>
      </div>

      <div class="file-list">
        <div
          v-for="(file, index) in state.currentCabinetFiles"
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
    <div v-if="state.previewFile" class="file-preview">
      <div class="preview-header">
        <h4>{{ state.previewFile.name }}</h4>
        <button @click="state.previewFile = null">×</button>
      </div>
      <div class="preview-content">
        <div v-if="state.previewFile.type === 'pdf'" class="pdf-preview">
          <p>PDF预览区域 (实际项目替换为PDF.js)</p>
        </div>

        <div v-else class="unsupported-preview">
          {{ getPreviewText(state.previewFile.type) }}
        </div>
      </div>
    </div>
    <!-- 控制ui按钮 -->
    <div
      v-if="state.showControlPanel"
      class="control-panel"
      :style="controlPanelStyle"
      ref="controlPanel"
    >
      <div v-if="state.selectedCabinet" class="selection-info">
        已选中: 第{{ state.currentSelectedGroup + 1 }}组 - 柜子{{
          state.currentSelectedIndex + 1
        }}
        <div
          class="lock-status"
          :class="{ locked: isColumnLocked(state.currentSelectedGroup) }"
        >
          当前列状态:
          {{ isColumnLocked(state.currentSelectedGroup) ? "未锁定" : "已锁定" }}
        </div>
      </div>
      <div class="control-header" @mousedown="startDrag1">
        柜子控制面板
        <span
          v-if="state.selectedCabinet"
          class="close-btn"
          @click="clearSelection"
          >×</span
        >
      </div>
      <div class="control-buttons">
        <button
          @click="Lock"
          :disabled="
            !state.selectedCabinet || !isColumnLocked(state.currentSelectedGroup)
          "
        >
          锁定
        </button>
        <button
          @click="UnLock"

          :disabled="
            !state.selectedCabinet || isColumnLocked(state.currentSelectedGroup)
          "
        >
          解锁
        </button>
        <button
          @click="LeftMove"
          :disabled="
            !(!state.selectedCabinet || isColumnLocked(state.currentSelectedGroup))
          "
        >
          左移
        </button>
        <button
          @click="RightMove"
          :disabled="
            !(!state.selectedCabinet || isColumnLocked(state.currentSelectedGroup))
          "
        >
          右移
        </button>
        <button @click="StopMove" :disabled="!state.isMoving">停止</button>
        <button @click="Reset">合架</button>
        <button
          @click="handleViewClick"
          :disabled="!state.selectedCabinet"
          class="view-button"
        >
          查看
        </button>
      </div>
      <div v-if="state.selectedCabinet" class="selection-info">
        <!-- 已选中: 组{{ currentSelectedGroup + 1 }} - 柜子{{
          currentSelectedIndex + 1
        }} -->
        <small>再次点击柜子可取消选择</small>
      </div>
    </div>

    <el-select
      v-if="state.cabinetConfigList.length > 0"
      v-model="state.selectedConfigId"
      placeholder="请选择配置"
      style="
        height: 32px;
        width: 240px;
        position: fixed;
        z-index: 9999;
        top: 50px;
        right: 0px;
        background: rgba(255, 255, 255, 0.85);
        box-shadow: none !important;
      "
      popper-class="fixed-select-dropdown"
      size="small"
      @change="onConfigChange"
    >
      <el-option
        v-for="item in state.cabinetConfigList"
        :key="item.id"
        :label="item.company + ' - ' + item.warehouse"
        :value="item.id"
      />
      <el-option
        v-if="state.cabinetConfigList.length === 0"
        disabled
        label="没有配置可选"
        value=""
      />
    </el-select>
    <div v-else>加载配置中...</div>
  </div>
</template>

<script setup>
import {
  ref,
  reactive,
  computed,
  onMounted,
  onBeforeUnmount,
  watch,
  nextTick,
  getCurrentInstance,
} from "vue";
import * as THREE from "three";
import { OrbitControls } from "three/examples/jsm/controls/OrbitControls.js";
import * as TWEEN from "three/examples/jsm/libs/tween.module.js";
import request from "@/utils/request";
import { ElMessage } from 'element-plus';
import {
  lockColumn,
  unlockColumn,
  stopMoveColumn,
  resetColumn,
  leftMoveColumn,
  rightMoveColumn,
} from "@/api/system/move";
const { proxy } = getCurrentInstance();
// 导入markRaw处理Three.js对象
import { markRaw } from "vue";

// DOM引用
const webglContainer = ref(null);
const filePanel = ref(null);
const controlPanel = ref(null);

// 状态管理
const state = reactive({
  // 拖动相关
  isDragging: false,
  panelPosition: { x: 20, y: 20 },
  dragOffset: { x: 0, y: 0 },

  // Three.js相关对象
  scene: markRaw(new THREE.Scene()),
  camera: markRaw(
    new THREE.PerspectiveCamera(
      50,
      window.innerWidth / window.innerHeight,
      0.1,
      20000
    )
  ),
  renderer: markRaw(
    new THREE.WebGLRenderer({ antialias: true, logarithmicDepthBuffer: true })
  ),
  controls: null, // 在init中初始化
  clock: markRaw(new THREE.Clock()),
  raycaster: markRaw(new THREE.Raycaster()),
  mouse: markRaw(new THREE.Vector2()),

  hit: null,
  lastHit: null,
  clickDebounce: null,
  currentFocusGroup: null,
  originalCameraPos: new THREE.Vector3(2400, 2000, 3600),

  // 柜子组
  buildingGroups: Array.from({ length: 4 }, () => markRaw(new THREE.Group())),
  groupStates: Array.from({ length: 4 }, () => ({
    sortedMeshes: [],
    movedMeshes: new Set(),
    isAnimating: false,
    originalPositions: new Map(),
  })),

  // UI状态
  currentCabinetFiles: [],
  previewFile: null,
  showFilePanel: false,
  showControlPanel: true,
  filePanelPosition: { x: 860, y: 20 }, // 初始位置在右侧
  controlPanelPosition: { x: 20, y: 20 }, // 初始位置在左侧

  // 控制状态
  isAnimating: false,
  isLocked: false,
  isMoving: false,
  currentSelectedGroup: null,
  currentSelectedIndex: -1,
  selectedCabinet: null,

  // 数据状态
  selectedConfigId: null,
  cabinetConfig: null,
  configData: null,
  cabinetConfigList: [],
  columnLockStates: {},
  lastClickTime: null,
});

// 计算属性
const controlPanelStyle = computed(() => ({
  left: `${state.controlPanelPosition.x}px`,
  top: `${state.controlPanelPosition.y}px`,
  position: "absolute",
}));
const filePanelStyle = computed(() => ({
  left: `${state.filePanelPosition.x}px`,
  top: `${state.filePanelPosition.y}px`,
  position: "absolute",
}));

const handleViewClick = () => {
  if (!state.selectedCabinet) {
    ElMessage.warning("请先选择一个柜子");
    return;
  }

  // 确保userData存在（防御性编程）
  if (!state.selectedCabinet.userData) {
    state.selectedCabinet.userData = {};
  }

  // 如果柜子没有文件数据，则生成随机文件
  if (!state.selectedCabinet.userData.files) {
    state.selectedCabinet.userData.files = generateRandomFiles();
  }

  // 设置当前文件列表（确保总是数组）
  state.currentCabinetFiles = state.selectedCabinet.userData.files || [];

  // 安全设置预览文件（第一个文件或null）
  state.previewFile = state.currentCabinetFiles[0] || null;

  // 显示文件面板
  state.showFilePanel = true;
  console.log("当前文件列表:", state.currentCabinetFiles);
};
// 初始化Three.js场景
const init = async () => {
  // 初始化相机
  state.camera.position.set(
    state.originalCameraPos.x,
    state.originalCameraPos.y,
    state.originalCameraPos.z
  );
  state.camera.lookAt(state.scene.position);

  // 初始化渲染器
  state.renderer.setClearColor("rgb(175,216,250)", 1.0);
  state.renderer.setSize(window.innerWidth, window.innerHeight);
  state.renderer.shadowMap.enabled = true;
  state.renderer.shadowMap.type = THREE.PCFSoftShadowMap;

  // 添加渲染器DOM
  webglContainer.value.appendChild(state.renderer.domElement);

  // 初始化控制器（使用markRaw避免代理）
  state.controls = markRaw(
    new OrbitControls(state.camera, state.renderer.domElement)
  );
  state.controls.autoRotate = false;
  state.controls.minDistance = 200;
  state.controls.maxDistance = 8000;
  state.controls.maxPolarAngle = Math.PI / 2.01;

  // 添加坐标轴辅助器
  state.scene.add(new THREE.AxesHelper(3000));

  // 添加地面
  const planeGeometry = new THREE.PlaneGeometry(6000, 6000);
  const planeMaterial = new THREE.MeshPhongMaterial({
    color: 0x8080ff,
    specular: 0x4488ee,
    shininess: 12,
  });
  const plane = markRaw(new THREE.Mesh(planeGeometry, planeMaterial));
  plane.receiveShadow = true;
  plane.rotation.x = -0.5 * Math.PI;
  plane.position.set(0, 0, 0);
  state.scene.add(plane);

  // 初始化柜子组
  state.buildingGroups.forEach((group, index) => {
    state.scene.add(group);
  });

  // 加载配置数据
  await fetchCabinetConfigList();

  // 添加墙壁
  addWalls();

  // 添加灯光
  const directionalLight = new THREE.DirectionalLight(0xffffff, 0.5);
  directionalLight.position.set(0, 1000, 0).normalize();
  state.scene.add(directionalLight);

  const ambientLight = new THREE.AmbientLight(0x505050, 0.2);
  state.scene.add(ambientLight);

  // 添加提示元素
  const prompt = document.createElement("div");
  prompt.id = "esc-prompt";
  prompt.textContent = "按 ESC 返回初始位置";
  prompt.style.cssText = `
    position: absolute;
    top: 20px;
    left: 50%;
    transform: translateX(-50%);
    color: white;
    font-family: Arial, sans-serif;
    font-size: 16px;
    text-shadow: 0 0 5px black;
    z-index: 1000;
  `;
  webglContainer.value.appendChild(prompt);

  // 绑定事件监听
  window.addEventListener("mousemove", onMouseMove);
  window.addEventListener("click", onClick);
  window.addEventListener("keydown", handleKeydown);
  window.addEventListener("resize", onWindowResize);
};

// 加载配置列表
// const fetchCabinetConfigList = async () => {
//   try {
//     const res = await request.get("/system/cabinetConfig/list");

//     if (res.code === 200 && res.data) {
//       const configs = Array.isArray(res.data) ? res.data : [res.data];

//       const processedConfigs = configs.map((item) => {
//         const parsedData = safeParseConfigData(item.configData);
//         if (parsedData?.cabinets?.length) {
//           parsedData.cabinets.forEach((cabinet) => {
//             // 获取所有柜子（所有柜子组的所有柜子）
//             const allCabinets = [];
//             cabinet.cabinetGroup.forEach((group) => {
//               allCabinets.push(...group.cabinet);
//             });
//             cabinet.cabinetGroup.forEach((group) => {
//               group.cabinet.forEach((cab) => {
//                 console.log(
//                   `columnNumber: ${cab.columnNumber}, networkConfig:`,
//                   cab.networkConfig
//                 );

//                 if (!cab.networkConfig) {
//                   cab.networkConfig = getNetworkConfigForColumn(
//                     cab.columnNumber,
//                     cabinet.cabinetGroup
//                   );
//                 }
//               });
//             });
//           });
//         }

//         const immobilizationList = [];
//         try {
//           parsedData?.cabinets?.forEach((cabinet) => {
//             cabinet.cabinetGroup.forEach((group) => {
//               group.cabinet.forEach((cab) => {
//                 immobilizationList.push({
//                   columnNumber: cab.columnNumber,
//                   immobilization: cab.immobilization,
//                 });
//               });
//             });
//           });
//         } catch (e) {
//           console.warn(`配置 ${item.id} 的 configData 结构异常`);
//         }

//         return {
//           ...item,
//           configData: parsedData,
//           immobilizationList,
//         };
//       });

//       state.cabinetConfigList = processedConfigs;

//       if (state.cabinetConfigList.length > 0) {
//         state.cabinetConfig = state.cabinetConfigList[0];
//         state.selectedConfigId = state.cabinetConfigList[0].id;
//         loadBuildingData();
//       }
//     }
//   } catch (error) {
//     console.error("配置加载失败:", error);
//     state.cabinetConfigList = [];
//     state.cabinetConfig = null;
//   }
// };
const fetchCabinetConfigList = async () => {
  try {
    const res = await request.get("/system/cabinetConfig/list");

    if (res.code === 200 && res.data) {
      const configs = Array.isArray(res.data) ? res.data : [res.data];

      const processedConfigs = configs.map((item) => {
        const parsedData = safeParseConfigData(item.configData);

        if (parsedData?.cabinets?.length) {
          parsedData.cabinets.forEach((cabinet) => {
            cabinet.cabinetGroup.forEach((group) => {
              // 查找组内第一个有 networkConfig 的柜子作为参考
              const referenceCab = group.cabinet.find(
                (c) =>
                  c.networkConfig && c.networkConfig.ip && c.networkConfig.port
              );

              group.cabinet.forEach((cab) => {
                // 若当前柜子无 networkConfig 且组内有参考柜子，则赋值
                if (!cab.networkConfig && referenceCab) {
                  cab.networkConfig = { ...referenceCab.networkConfig };
                }

                // 调试输出
                console.log(
                  `columnNumber: ${cab.columnNumber}, networkConfig:`,
                  cab.networkConfig
                );
              });
            });
          });
        }

        // 生成 immobilizationList
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

      // 更新状态并加载默认配置
      state.cabinetConfigList = processedConfigs;

      if (state.cabinetConfigList.length > 0) {
        state.cabinetConfig = state.cabinetConfigList[0];
        state.selectedConfigId = state.cabinetConfigList[0].id;
        loadBuildingData();
      }
    }
  } catch (error) {
    console.error("配置加载失败:", error);
    state.cabinetConfigList = [];
    state.cabinetConfig = null;
  }
};

// 安全解析配置数据
const safeParseConfigData = (data) => {
  if (!data) return null;
  try {
    const parsed = typeof data === "string" ? JSON.parse(data) : data;
    if (!parsed.cabinets) throw new Error("缺少cabinets字段");
    return parsed;
  } catch (e) {
    console.error("配置解析失败:", e.message, "原始数据:", data);
    return { cabinets: [] };
  }
};

// 获取网络配置
// const getNetworkConfigForColumn = (columnNumber, cabinets) => {
//   const fixedColumns = cabinets.filter(
//     (cab) =>
//       cab.immobilization === 1 &&
//       cab.networkConfig?.ip &&
//       cab.networkConfig?.port
//   );
//   return fixedColumns[0]?.networkConfig;
// };

// const getNetworkConfigForColumn = (columnNumber, cabinets) => {
//   const targetCabinet = cabinets.find(
//     (cab) =>
//       cab.columnNumber === columnNumber &&
//       cab.networkConfig?.ip &&
//       cab.networkConfig?.port
//   );
//   return targetCabinet?.networkConfig || null;
// };

const getNetworkConfigForColumn = (columnNumber, cabinets) => {
  const fixedColumns = cabinets.filter(
    (cab) =>
      cab.immobilization === 1 &&
      cab.networkConfig?.ip &&
      cab.networkConfig?.port
  );
  return fixedColumns[0]?.networkConfig || null;
};

const onConfigChange = () => {
  const selected = state.cabinetConfigList.find(
    (item) => item.id === state.selectedConfigId
  );
  if (selected) {
    state.cabinetConfig = selected;
    loadBuildingData();
  } else {
    console.warn("未找到匹配的配置项。");
  }
};

// 加载建筑物数据
const loadBuildingData = () => {
  const configSource = state.cabinetConfig;

  if (!configSource) {
    console.warn("loadBuildingData 被调用，但当前未选择配置。");
    return;
  }

  if (!configSource.configData?.cabinets) {
    console.error(
      "所选配置缺少有效的 configData.cabinets 字段：",
      configSource
    );
    return;
  }

  processBuildingData(configSource.configData);
};

// 处理建筑物数据
const processBuildingData = (data) => {
  clearAllCabinets();

  if (!data.cabinets || !Array.isArray(data.cabinets)) {
    console.error("无效的cabinets数据:", data);
    return;
  }

  data.cabinets.forEach((warehouse) => {
    if (!warehouse.cabinetGroup || !Array.isArray(warehouse.cabinetGroup)) {
      console.warn("跳过无效的warehouse:", warehouse);
      return;
    }

    warehouse.cabinetGroup.forEach((group) => {
      processCabinetGroup(group);
    });
  });
};

// 处理柜子组
const processCabinetGroup = (group) => {
  const groupName = group.groupName || "";
  const groupIndex = parseInt(groupName.replace("Group", "")) - 1;

  if (
    isNaN(groupIndex) ||
    groupIndex < 0 ||
    groupIndex >= state.buildingGroups.length
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
      cabinet.networkConfig = getNetworkConfigForColumn(cabinet.columnNumber, [
        group,
      ]);
    }
    createCabinet(cabinet, groupIndex);
  });
};

// 清除所有柜子
const clearAllCabinets = () => {
  state.buildingGroups.forEach((group) => {
    while (group.children.length) {
      const child = group.children[0];
      child.geometry?.dispose();
      child.material?.dispose();
      group.remove(child);
    }
  });

  state.groupStates.forEach((state) => {
    state.sortedMeshes = [];
    state.movedMeshes.clear();
    state.originalPositions.clear();
  });
};

// 创建单个柜子
const createCabinet = (cabinetData, groupIndex) => {
  const geometry = new THREE.BoxGeometry(
    cabinetData.L,
    cabinetData.H,
    cabinetData.W
  );

  const material = new THREE.MeshBasicMaterial({
    color: getCabinetColor(cabinetData.C),
    transparent: true,
    opacity: 0.6,
  });

  const mesh = markRaw(new THREE.Mesh(geometry, material));

  // 添加编号标签
  const label = createCabinetLabel(cabinetData.columnNumber, groupIndex, cabinetData);
  mesh.add(label);

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
    cabinetData: {
      ...cabinetData,
      ...cabinetData.networkConfig, // 把 ip port 也展开进去，方便直接拿
    },
    files: generateRandomFiles(),
    fileBoxes: generateFileBoxes(cabinetData),
    networkConfig: cabinetData.networkConfig || null,
  };

  // 添加到场景
  state.buildingGroups[groupIndex].add(mesh);

  // 记录原始位置
  state.groupStates[groupIndex].originalPositions.set(
    mesh,
    mesh.position.clone()
  );

  // 添加到排序列表
  state.groupStates[groupIndex].sortedMeshes.push(mesh);
};

// 创建柜子编号标签
const createCabinetLabel = (columnNumber, groupIndex, cabinetData) => {
  // 创建Canvas纹理
  const canvas = document.createElement('canvas');
  canvas.width = 100;
  canvas.height = 100;
  const context = canvas.getContext('2d');

  // 绘制编号背景和文字
  context.fillStyle = '#4a90e2';
  context.fillRect(0, 0, canvas.width, canvas.height);
  context.font = 'Bold 60px Arial';
  context.textAlign = 'center';
  context.textBaseline = 'middle';
  context.fillStyle = 'white';
  context.fillText(columnNumber.toString(), canvas.width/2, canvas.height/2);

  // 创建纹理
  const texture = new THREE.CanvasTexture(canvas);
  const material = new THREE.MeshBasicMaterial({
    map: texture,
    transparent: true,
    side: THREE.DoubleSide
  });

  // 创建平面几何体
  const geometry = new THREE.PlaneGeometry(100, 100);
  const label = new THREE.Mesh(geometry, material);

  // 根据组别设置标签位置和朝向
  const isGroup1Or4 = groupIndex === 0 || groupIndex === 3;

  // 计算标签应该放置的位置（柜子侧面中心）
  const offsetZ = isGroup1Or4
      ? cabinetData.W / 2 + 5  // 放在柜子正Z方向外侧
      : -cabinetData.W / 2 - 5; // 放在柜子负Z方向外侧

  // 设置标签位置
  label.position.set(0, 180, offsetZ);

  // 设置标签旋转，使其始终面向相机
  label.rotation.y = isGroup1Or4 ? 0 : Math.PI;

  return label;
};

// 获取柜子颜色
const getCabinetColor = (colorCode) => {
  const colorMap = {
    0: 0xffce45,
    1: 0xdd5545,
    2: 0x15bf83,
    3: 0x4a90e2,
    default: 0xffffff,
  };

  return colorMap[colorCode] || colorMap.default;
};

// 生成随机文件
const generateRandomFiles = () => {
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

  return Array.from({ length: 1 + Math.floor(Math.random() * 10) }, (_, i) => {
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
  });
};

// 生成档案盒数据
const generateFileBoxes = (cabinetData) => {
  if (!cabinetData.layers || cabinetData.layers.length === 0) {
    return [];
  }

  const fileBoxes = [];
  const boxHeight = 100;
  const boxWidth = 80;
  const boxDepth = 20;

  cabinetData.layers.forEach((layer) => {
    const columns = Math.floor(layer.W / boxDepth);
    const rows = Math.floor(layer.H / boxHeight);

    const zOffset = (layer.W - columns * boxDepth) / 2;
    const yOffset = (layer.H - rows * boxHeight) / 2;

    for (let col = 0; col < columns; col++) {
      for (let row = 0; row < rows; row++) {
        fileBoxes.push({
          position: {
            x: layer.X - 15,
            y: layer.Y + yOffset + row * boxHeight + boxHeight / 2,
            z: layer.Z - layer.W / 2 + zOffset + col * boxDepth + boxDepth / 2,
          },
          size: { w: boxWidth, h: boxHeight, d: boxDepth },
          color: 0x333333,
          files: generateRandomFiles(3 + Math.floor(Math.random() * 5)),
        });
      }
    }
  });

  return fileBoxes;
};

// 拖动相关方法
const startDrag = (e) => {
  const panel = filePanel.value;
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
    state.filePanelPosition = {
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
};

const startDrag1 = (e) => {
  const panel = controlPanel.value;
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
    state.controlPanelPosition = {
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
};

// 鼠标移动处理
const onMouseMove = (event) => {
  const rect = state.renderer.domElement.getBoundingClientRect();
  state.mouse.x = ((event.clientX - rect.left) / rect.width) * 2 - 1;
  state.mouse.y = -((event.clientY - rect.top) / rect.height) * 2 + 1;
};

// 点击处理
const onClick = (event) => {
  if (state.isAnimating) return;

  const now = Date.now();
  if (state.lastClickTime && now - state.lastClickTime < 300) return;
  state.lastClickTime = now;

  handleClickEvent(event);
};

// 关闭文件面板
const closeFilePanel = () => {
  state.showFilePanel = false;
  state.currentCabinetFiles = [];
  state.previewFile = null;
};

// 设置预览文件
const setPreviewFile = (file) => {
  state.previewFile = file;
  state.showFilePanel = true; // 确保文件面板是显示的
};

// 获取文件图标
const getFileIcon = (type) => {
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
};

// 获取预览文本
const getPreviewText = (type) => {
  return type === "code"
    ? "代码文件预览 (需集成代码编辑器)"
    : "不支持预览此文件类型";
};

// 动画循环
// const animate = () => {
//   requestAnimationFrame(animate);

//   const delta = state.clock.getDelta();
//   state.controls.update(delta);
//   TWEEN.update();

//   state.raycaster.setFromCamera(state.mouse, state.camera);
//   const intersects = state.raycaster.intersectObjects(state.scene.children);

//   if (intersects.length > 0) {
//     if (intersects[0].object != null) {
//       state.hit = intersects[0].object;
//       if (
//         state.hit !== state.lastHit &&
//         state.lastHit !== null &&
//         state.lastHit !== state.plane
//       ) {
//         state.lastHit.material.transparent = true;
//         state.lastHit.material.opacity = 0.6;
//       }
//       if (state.hit !== state.plane) {
//         state.hit.material.transparent = true;
//         state.hit.material.opacity = 1.0;
//       }
//     }
//   }

//   state.lastHit = state.hit;
//   state.renderer.render(state.scene, state.camera);
// };
const animate = () => {
  requestAnimationFrame(animate);

  const delta = state.clock.getDelta();
  state.controls.update(delta);
  TWEEN.update();

  state.raycaster.setFromCamera(state.mouse, state.camera);
  const intersects = state.raycaster.intersectObjects(state.scene.children);

  if (intersects.length > 0) {
    const hitObject = intersects[0].object;
    if (hitObject && hitObject.material) {
      state.hit = hitObject;

      // 恢复上一个命中对象的透明度
      if (
        state.lastHit &&
        state.lastHit !== state.plane &&
        state.lastHit.material
      ) {
        state.lastHit.material.transparent = true;
        state.lastHit.material.opacity = 0.6;
      }

      // 高亮当前命中对象
      if (state.hit !== state.plane) {
        state.hit.material.transparent = true;
        state.hit.material.opacity = 1.0;
      }
    }
  }

  state.lastHit = state.hit;
  state.renderer.render(state.scene, state.camera);
};

// 添加墙壁
const addWalls = () => {
  const wallMaterial = new THREE.MeshStandardMaterial({
    color: 0xf0f0f0,
    side: THREE.DoubleSide,
  });

  const wallHeight = 1000;

  const createWall = (position, rotation = 0) => {
    const wall = new THREE.Mesh(
      new THREE.PlaneGeometry(6000, wallHeight),
      wallMaterial
    );
    wall.position.copy(position);
    wall.rotation.y = rotation;
    state.scene.add(wall);
    return wall;
  };

  createWall(new THREE.Vector3(0, wallHeight / 2, -3000)); // 前墙
  createWall(new THREE.Vector3(0, wallHeight / 2, 3000)); // 后墙
  createWall(new THREE.Vector3(-3000, wallHeight / 2, 0), Math.PI / 2); // 左墙
  createWall(new THREE.Vector3(3000, wallHeight / 2, 0), -Math.PI / 2); // 右墙
};

// 键盘按下处理
const handleKeydown = (event) => {
  if (event.key === "Escape") {
    resetCamera(() => {
      closeFilePanel();

      state.groupStates.forEach((state) => {
        state.sortedMeshes.forEach((mesh) => {
          if (mesh.userData.hasBeenClicked) {
            mesh.userData.hasBeenClicked = false;
          }
        });
      });
    });
  }
};

// 重置相机
const resetCamera = (callback = null) => {
  if (state.isAnimating) return;
  state.isAnimating = true;

  TWEEN.removeAll();

  if (!state.camera || !state.controls) {
    console.error("Camera 或 Controls 未初始化");
    return;
  }

  const focusPoint = new THREE.Vector3(0, 0, 0);

  new TWEEN.Tween({
    x: state.camera.position.x,
    y: state.camera.position.y,
    z: state.camera.position.z,
    lookX: state.controls.target.x,
    lookY: state.controls.target.y,
    lookZ: state.controls.target.z,
  })
    .to(
      {
        x: state.originalCameraPos.x,
        y: state.originalCameraPos.y,
        z: state.originalCameraPos.z,
        lookX: focusPoint.x,
        lookY: focusPoint.y,
        lookZ: focusPoint.z,
      },
      1500
    )
    .easing(TWEEN.Easing.Quadratic.InOut)
    .onUpdate((obj) => {
      state.camera.position.set(obj.x, obj.y, obj.z);
      state.controls.target.set(obj.lookX, obj.lookY, obj.lookZ);
      state.controls.update();
    })
    .onStart(() => {
      state.controls.enabled = false;
      state.isAnimating = true;
    })
    .onComplete(() => {
      state.controls.enabled = true;
      state.isAnimating = false;
      state.camera.position.copy(state.originalCameraPos);
      state.controls.target.copy(focusPoint);

      // 复原所有拉出的档案盒
      state.buildingGroups.forEach((group) => {
        group.children.forEach((child) => {
          if (child.userData?.isFileBox && child.userData.isPulledOut) {
            child.position.set(
              child.userData.originalPosition.x,
              child.userData.originalPosition.y,
              child.userData.originalPosition.z
            );
            child.userData.isPulledOut = false;
          }
        });
      });

      if (callback) callback();
      state.renderer.render(state.scene, state.camera);
    })
    .start();
};

// 聚焦选中对象
// const focusOnSelected = () => {
//   if (state.selectedCabinet) {
//     focusOnObject(state.selectedCabinet);
//   }
// };

// const focusOnSelected = () => {
//   if (!state.selectedCabinet) return;
//   state.showControlPanel = !state.showControlPanel;
//   // 获取柜子数据
//   const cabinetData = state.selectedCabinet.userData?.cabinetData;
//   if (!cabinetData) return;

//   // 设置当前文件列表（使用 state.currentCabinetFiles）
//   state.currentCabinetFiles = state.selectedCabinet.userData?.files || [];

//   // 设置预览文件
//   state.previewFile =
//     state.currentCabinetFiles.length > 0 ? state.currentCabinetFiles[0] : null;

//   // 显示文件面板
//   state.showFilePanel = true;

//   // 如果需要，可以添加焦点动画
//   focusOnObject(state.selectedCabinet);
// };

// 聚焦对象
const focusOnObject = (object) => {
  if (!object?.geometry) {
    console.warn("无效的聚焦对象");
    return;
  }

  TWEEN.removeAll();

  const bbox = new THREE.Box3().setFromObject(object);
  const size = new THREE.Vector3();
  bbox.getSize(size);

  const targetPosition = new THREE.Vector3(
    object.position.x - size.x * 5.5,
    object.position.y + size.y / 2,
    object.position.z - size.z * 0.5
  );

  new TWEEN.Tween(state.camera.position)
    .to(targetPosition, 800)
    .easing(TWEEN.Easing.Quadratic.InOut)
    .onUpdate(() => {
      state.camera.lookAt(object.position);
    })
    .onComplete(() => {
      state.controls.target.copy(object.position);
    })
    .start();

  new TWEEN.Tween(state.controls.target).to(object.position, 800).start();
};

// 处理点击事件
const handleClickEvent = (event) => {
  if (state.isAnimating) return;

  const rect = state.renderer.domElement.getBoundingClientRect();
  state.mouse.x = ((event.clientX - rect.left) / rect.width) * 2 - 1;
  state.mouse.y = -((event.clientY - rect.top) / rect.height) * 2 + 1;
  state.raycaster.setFromCamera(state.mouse, state.camera);

  let closestIntersect = null;
  let groupIndex = -1;

  state.buildingGroups.forEach((group, idx) => {
    if (state.groupStates[idx].isAnimating) return;

    const intersects = state.raycaster.intersectObjects(group.children);
    if (intersects.length > 0) {
      const closest = intersects[0];
      if (!closestIntersect || closest.distance < closestIntersect.distance) {
        closestIntersect = closest;
        groupIndex = idx;
      }
    }
  });

  if (closestIntersect && groupIndex !== -1) {
    const clickedMesh = closestIntersect.object;
    const cabinetData = clickedMesh.userData.cabinetData;
    const columnNumber = cabinetData.columnNumber;
    const immobilizationList = state.cabinetConfig.immobilizationList || [];
    const immobilization = immobilizationList.find(
      (item) => item.columnNumber === columnNumber
    )?.immobilization;

    if (immobilization === 1) {
      alert(`该列为固定列，不可进行移动操作`);
      return;
    }

    if (
      state.selectedCabinet &&
      state.selectedCabinet.uuid === clickedMesh.uuid
    ) {
      resetCabinetAppearance(clickedMesh);
      state.selectedCabinet = null;
      return;
    }

    if (state.selectedCabinet) {
      resetCabinetAppearance(state.selectedCabinet);
    }

    state.selectedCabinet = clickedMesh;
    state.currentSelectedGroup = groupIndex;
    state.currentSelectedIndex = state.groupStates[
      groupIndex
    ].sortedMeshes.findIndex((mesh) => mesh.uuid === clickedMesh.uuid);

    highlightSelectedCabinet(clickedMesh);
    return;
  }

  if (state.selectedCabinet) {
    resetCabinetAppearance(state.selectedCabinet);
    state.selectedCabinet = null;
  }
};

// 高亮选中柜子
const highlightSelectedCabinet = (cabinet) => {
  clearHighlights();

  if (!cabinet.userData.originalMaterial) {
    cabinet.userData.originalMaterial = cabinet.material;
    cabinet.userData.originalOpacity = cabinet.material.opacity;
  }

  const highlightMaterial = new THREE.MeshPhongMaterial({
    color: cabinet.userData.originalMaterial.color,
    transparent: true,
    opacity: 0.8,
    specular: 0xffffff,
    shininess: 30,
  });

  cabinet.material = highlightMaterial;
};

// 重置柜子外观
const resetCabinetAppearance = (cabinet) => {
  if (cabinet && cabinet.userData.originalMaterial) {
    cabinet.material = cabinet.userData.originalMaterial;
    cabinet.material.opacity = cabinet.userData.originalOpacity ?? 1;
    cabinet.material.transparent = true;
    cabinet.material.needsUpdate = true;
  }
};

// 清除所有高亮
const clearHighlights = () => {
  state.buildingGroups.forEach((group) => {
    group.children.forEach((child) => {
      resetCabinetAppearance(child);
    });
  });
};

// 清除选择
const clearSelection = () => {
  clearHighlights();
  state.selectedCabinet = null;
  state.currentSelectedGroup = null;
  state.currentSelectedIndex = -1;
};

// 锁定柜子
const Lock = async () => {
  if (!state.selectedCabinet?.userData?.cabinetData) {
    proxy.$message({
      message: "请先选择有效的柜子,并先解锁!",
      type: "warning",
      duration: 2000,
      showClose: true,
    });
    console.error("请先选择有效的柜子");
    return;
  }

  const loadingInstance = proxy.$loading({
    lock: true,
    text: "正在锁定柜子...",
    spinner: "el-icon-loading",
  });

  try {
    state.columnLockStates[state.currentSelectedGroup] = false;

    const res = await executeMoveCommand("Lock");
    // await Promise.all([
    //   executeMoveCommand("Lock"),
    //   resetCabinetAppearance(state.selectedCabinet),
    // ]);
    if (res.isSuccess=='1'){
      loadingInstance.close();
      ElMessage({
        message: "柜子锁定成功",
        type: "success",
        duration: 1500,
        showClose: true,
      });
      // resetCabinetAppearance(state.selectedCabinet)
    }
  } catch (error) {
    console.error("锁定失败:", error);
    state.columnLockStates[state.currentSelectedGroup] = false;

    proxy.$message({
      message: error.message || "锁定指令发送失败",
      type: "error",
      duration: 2000,
      showClose: true,
    });
  } finally {
    loadingInstance.close();
  }
};

// 解锁柜子
const UnLock = async () => {
  if (!state.selectedCabinet?.userData?.cabinetData) {
    proxy.$message({
      message: "请先选择有效的柜子,并先解锁!",
      type: "error",
      duration: 2000,
      showClose: true,
    });
    console.error("请先选择有效的柜子");
    return;
  }

  const loadingInstance = proxy.$loading({
    lock: true,
    text: "正在解锁柜子...",
    spinner: "el-icon-loading",
  });

  try {
    state.columnLockStates[state.currentSelectedGroup] = true;
    const res = await executeMoveCommand("UnLock");
    // await Promise.all([
    //   // executeMoveCommand("UnLock"),
    //   // resetCabinetAppearance(state.selectedCabinet),
    // ]);

    if (res.isSuccess=='1'){
      loadingInstance.close();
      ElMessage({
        message: "柜子解锁成功",
        type: "success",
        duration: 1500,
        showClose: true,
      });
      // resetCabinetAppearance(state.selectedCabinet)
    }
  } catch (error) {
    console.error("解锁失败:", error);
    state.columnLockStates[state.currentSelectedGroup] = false;

    proxy.$message({
      message: error.message || "解锁指令发送失败",
      type: "error",
      duration: 2000,
      showClose: true,
    });
  } finally {
    loadingInstance.close();
  }
};

// 检查列是否锁定
const isColumnLocked = (groupIndex) => {
  return !!state.columnLockStates[groupIndex];
};

// 左移柜子
const LeftMove = async () => {
  if (!state.selectedCabinet?.userData?.cabinetData) {
    ElMessage({
      message: "请先选择有效的柜子,并先解锁!",
      type: "warning",
      duration: 1500,
    });
    console.error("请先选择有效的柜子");
    return;
  }

  const loadingInstance = proxy.$loading({
    lock: true,
    text: "正在发送左移指令...",
    spinner: "el-icon-loading",
  });

  try {
    state.columnLockStates[state.currentSelectedGroup] = false;
    state.isMoving = true;

    const res = await executeMoveCommand("LeftMove")
    if (res.isSuccess=='1'){
      loadingInstance.close();
      ElMessage({
        message: "正在左移...",
        type: "success",
        duration: 2500,
      });
      await animateCabinetMove(-300)
    }


    // await Promise.all([
    //   animateCabinetMove(-300),
    //   executeMoveCommand("LeftMove"),
    // ]);
    //
    // proxy.$message.success("左移完成", { duration: 1500 });
  } catch (error) {
    console.error("左移失败:", error);
    proxy.$message({
      message: error.message || "左移指令发送失败",
      type: "error",
      duration: 2000,
      showClose: true,
    });
  } finally {
    loadingInstance.close();
    state.isMoving = false;
  }
};
// 右移柜子
const RightMove = async () => {
  if (!state.selectedCabinet?.userData?.cabinetData) {
    ElMessage({
      message: "请先选择有效的柜子,并先解锁!",
      type: "warning",
      duration: 1500,
    });
    console.error("请先选择有效的柜子");
    return;
  }

  const loadingInstance = proxy.$loading({
    lock: true,
    text: "正在发送右移指令...",
    spinner: "el-icon-loading",
  });

  try {
    state.isMoving = true;
    state.columnLockStates[state.currentSelectedGroup] = false;
    const res = await executeMoveCommand("RightMove")
    if (res.isSuccess=='1'){
      loadingInstance.close();
      ElMessage({
        message: "正在右移...",
        type: "success",
        duration: 2500,
      });
      await animateCabinetMove(300)
    }

    // await Promise.all([
    //   animateCabinetMove(300),
    //   executeMoveCommand("RightMove"),
    // ]);

    // proxy.$message.success("右移完成", { duration: 1500 });
  } catch (error) {
    console.error("右移失败:", error);
    proxy.$message({
      message: error.message || "右移指令发送失败",
      type: "error",
      duration: 2000,
      showClose: true,
    });
  } finally {
    loadingInstance.close();
    state.isMoving = false;
  }
};

// 执行移动命令
// const executeMoveCommand = async (command) => {
//   if (!state.selectedCabinet || !state.selectedCabinet.userData.cabinetData) {
//     throw new Error("未选择有效的柜子");
//   }

//   const cabinetData = state.selectedCabinet.userData.cabinetData;
//   const { columnNumber, X, Y, Z, L, W, H } = cabinetData;

//   try {
//     let response;

//     switch (command) {
//       case "Lock":
//         response = await lockColumn({
//           columnNumber: columnNumber,
//           x: X,
//           y: Y,
//           z: Z,
//           l: L,
//           w: W,
//           h: H,
//         });
//         break;
//       case "UnLock":
//         response = await unlockColumn({
//           columnNumber: columnNumber,
//           x: X,
//           y: Y,
//           z: Z,
//           l: L,
//           w: W,
//           h: H,
//         });
//         break;
//       case "StopMove":
//         response = await stopMoveColumn({
//           columnNumber: columnNumber,
//           x: X,
//           y: Y,
//           z: Z,
//           l: L,
//           w: W,
//           h: H,
//         });
//         break;
//       case "Reset":
//         response = await resetColumn({
//           columnNumber: columnNumber,
//           x: X,
//           y: Y,
//           z: Z,
//           l: L,
//           w: W,
//           h: H,
//         });
//         break;
//       case "LeftMove":
//         response = await leftMoveColumn({
//           columnNumber: columnNumber,
//           x: X,
//           y: Y,
//           z: Z,
//           l: L,
//           w: W,
//           h: H,
//         });
//         break;
//       case "RightMove":
//         response = await rightMoveColumn({
//           columnNumber: columnNumber,
//           x: X,
//           y: Y,
//           z: Z,
//           l: L,
//           w: W,
//           h: H,
//         });
//         break;
//       default:
//         throw new Error(`未知命令: ${command}`);
//     }

//     if (response.code !== 200) {
//       throw new Error(response.message || "命令执行失败");
//     }

//     //构造返回值，返回ip，port，columnNumber
//     const { ip, port } = response;
//     const result = { ip, port };

//     if (command === "LeftMove" || command === "RightMove") {
//       result.columnNumber = columnNumber;
//     }

//     return result;
//   } catch (error) {
//     console.error(`执行命令 ${command} 失败:`, error);
//     throw error;
//   }
// };

// const executeMoveCommand = async (command) => {
//   if (!state.selectedCabinet || !state.selectedCabinet.userData.cabinetData) {
//     throw new Error("未选择有效的柜子");
//   }

//   const cabinetData = state.selectedCabinet.userData.cabinetData;
//   const { columnNumber, ip, port, X, Y, Z, L, W, H } = cabinetData;

//   try {
//     const payload = { columnNumber, x: X, y: Y, z: Z, l: L, w: W, h: H };
//     let response;

//     switch (command) {
//       case "Lock":
//         response = await lockColumn(payload);
//         break;
//       case "UnLock":
//         response = await unlockColumn(payload);
//         break;
//       case "StopMove":
//         response = await stopMoveColumn(payload);
//         break;
//       case "Reset":
//         response = await resetColumn(payload);
//         break;
//       case "LeftMove":
//         response = await leftMoveColumn(ip, port, columnNumber);
//         break;
//       case "RightMove":
//         response = await rightMoveColumn(ip, port, columnNumber);
//         break;
//       default:
//         throw new Error(`未知命令: ${command}`);
//     }

//     if (response.code !== 200) {
//       throw new Error(response.message || "命令执行失败");
//     }

//     // 构造返回值，只返回需要的字段
//     const result = { ip, port };

//     if (command === "LeftMove" || command === "RightMove") {
//       result.columnNumber = columnNumber;
//     }

//     return result;
//   } catch (error) {
//     console.error(`执行命令 ${command} 失败:`, error);
//     throw error;
//   }
// };
// const executeMoveCommand = async (command) => {
//   if (!state.selectedCabinet || !state.selectedCabinet.userData.cabinetData) {
//     throw new Error("未选择有效的柜子");
//   }

//   const cabinetData = state.selectedCabinet.userData.cabinetData;
//   console.log("networkConfig:", cabinetData.networkConfig);
//   // 优先从 networkConfig 读取 ip 和 port
//   const ip = cabinetData.ip || cabinetData.networkConfig?.ip;
//   const port = cabinetData.port || cabinetData.networkConfig?.port;
//   const { columnNumber, networkConfig } = cabinetData;
//   // const { columnNumber, ip, port, X, Y, Z, L, W, H } = cabinetData;

//   if (!ip || !port || columnNumber === undefined || columnNumber === null) {
//     throw new Error(
//       `无效的控制参数，ip=${ip}，port=${port}，columnNumber=${columnNumber}`
//     );
//   }

//   try {
//     const payload = { columnNumber, x: X, y: Y, z: Z, l: L, w: W, h: H };
//     let response;

//     switch (command) {
//       case "Lock":
//         response = await lockColumn(payload);
//         break;
//       case "UnLock":
//         response = await unlockColumn(payload);
//         break;
//       case "StopMove":
//         response = await stopMoveColumn(payload);
//         break;
//       case "Reset":
//         response = await resetColumn(payload);
//         break;
//       case "LeftMove":
//         response = await leftMoveColumn(ip, port, columnNumber);
//         break;
//       case "RightMove":
//         response = await rightMoveColumn(ip, port, columnNumber);
//         break;
//       default:
//         throw new Error(`未知命令: ${command}`);
//     }

//     if (response.code !== 200) {
//       throw new Error(response.message || "命令执行失败");
//     }

//     const result = { ip, port };
//     if (command === "LeftMove" || command === "RightMove") {
//       result.columnNumber = columnNumber;
//     }

//     return result;
//   } catch (error) {
//     console.error(`执行命令 ${command} 失败:`, error);
//     throw error;
//   }
// };

const executeMoveCommand = async (command) => {
  if (!state.selectedCabinet || !state.selectedCabinet.userData.cabinetData) {
    throw new Error("未选择有效的柜子");
  }

  const cabinetData = state.selectedCabinet.userData.cabinetData;
  console.log("networkConfig:", cabinetData.networkConfig);
  console.log("ip:", cabinetData.ip);
  console.log("port:", cabinetData.port);
  // 20250528 重新调换ip和port likang
  const ip = cabinetData.ip || cabinetData.networkConfig?.ip;
  const port = cabinetData.port || cabinetData.networkConfig?.port;
  const { columnNumber } = cabinetData;

  if (!ip || !port || columnNumber === undefined || columnNumber === null) {
    throw new Error(
      `无效的控制参数，ip=${ip}，port=${port}，columnNumber=${columnNumber}`
    );
  }

  try {
    let response;

    switch (command) {
      case "Lock":
        response = await lockColumn(ip, port);
        break;
      case "UnLock":
        response = await unlockColumn(ip, port);
        break;
      case "StopMove":
        response = await stopMoveColumn(ip, port);
        break;
      case "Reset":
        response = await resetColumn(ip, port);
        break;
      case "LeftMove":
        response = await leftMoveColumn(ip, port, columnNumber);
        break;
      case "RightMove":
        response = await rightMoveColumn(ip, port, columnNumber);
        break;
      default:
        throw new Error(`未知命令: ${command}`);
    }

    // if (response.code !== 200) {
    //   throw new Error(response.message || "命令执行失败");
    // }

    const result = { ip, port };
    /**
     * 判断左右移动命令是否成功
     */
    console.log(response,'指令结果')
    if (command === "LeftMove" || command === "RightMove") {
      // result.columnNumber = columnNumber;
      // 从response获取是否成功标识
      result.isSuccess = response.data.data;
      // result.isSuccess = 1;  // 测试 1就是成功
    }else if (command === "Lock" || command === "UnLock") {
      result.isSuccess = response.data.data;
      // result.isSuccess = 1;
    }else if (command === "StopMove") {
      result.isSuccess = response.data.data;
      // result.isSuccess = 1;
    }else if (command === "Reset") {
      result.isSuccess = response.data.data;
      // result.isSuccess = 1;
    }


    return result;
  } catch (error) {
    console.error(`执行命令 ${command} 失败:`, error);
    throw error;
  }
};

// 柜子移动动画
const animateCabinetMove = async (distance) => {
  return new Promise((resolve) => {
    if (!state.selectedCabinet) {
      resolve();
      return;
    }

    const groupIndex = state.currentSelectedGroup;
    if (groupIndex === null || groupIndex === undefined) {
      resolve();
      return;
    }

    const groupState = state.groupStates[groupIndex];
    groupState.isAnimating = true;

    const targetPosition = state.selectedCabinet.position.clone();
    targetPosition.x += distance;

    new TWEEN.Tween(state.selectedCabinet.position)
        .to(targetPosition, 3000)
        .easing(TWEEN.Easing.Quadratic.InOut)
        .onComplete(() => {
          groupState.isAnimating = false;
          resolve();
        })
        .start();
  });

  // // 4. 保留你的网络请求逻辑（如需要）
  // try {
  //   await executeMoveCommand(distance > 0 ? "RightMove" : "LeftMove");
  //   proxy.$message.success("移动完成", {duration: 1500});
  // } catch (error) {
  //   console.error("移动指令发送失败:", error);
  //   // 错误处理（保留你的原始逻辑）
  // }
};

// 停止移动
const StopMove = async () => {
  if (!state.selectedCabinet?.userData?.cabinetData) {
    proxy.$message({
      message: "请先选择有效的柜子,并先解锁!",
      type: "error",
      duration: 2000,
      showClose: true,
    });
    console.error("请先选择有效的柜子");
    return;
  }

  const loadingInstance = proxy.$loading({
    lock: true,
    text: "正在发送停止指令...",
    spinner: "el-icon-loading",
  });

  try {
    await executeMoveCommand("StopMove");
    proxy.$message.success("已停止移动", { duration: 1500 });
  } catch (error) {
    console.error("停止移动失败:", error);
    proxy.$message({
      message: error.message || "移动指令发送失败",
      type: "error",
      duration: 2000,
      showClose: true,
    });
  } finally {
    loadingInstance.close();
  }
};

// 重置柜子位置
const Reset = async () => {
  if (!state.selectedCabinet?.userData?.cabinetData) {
    ElMessage({
      message: "请先选择有效的柜子,并先解锁!",
      type: "warning",
      duration: 1500,
    });
    console.error("请先选择有效的柜子");
    return;
  }
  if (!state.selectedCabinet?.userData?.cabinetData) {
    console.error("请先选择有效的柜子");
    return;
  }

  const loadingInstance = proxy.$loading({
    lock: true,
    text: "正在重置柜子位置...",
    spinner: "el-icon-loading",
  });

  const res = await executeMoveCommand("Reset");
  if(res.isSuccess=='1'){
    loadingInstance.close()
    proxy.$message.success("位置已重置", { duration: 1500 });
  }

  try {
    state.isMoving = true;
    state.columnLockStates[state.currentSelectedGroup] = false;
    const groupIndex = state.currentSelectedGroup;
    const groupState = state.groupStates[groupIndex];

    const originalPosition = groupState.originalPositions.get(
      state.selectedCabinet
    );
    if (!originalPosition) {
      throw new Error("找不到柜子的原始位置");
    }

    new TWEEN.Tween(state.selectedCabinet.position)
      .to(originalPosition, 1000)
      .easing(TWEEN.Easing.Quadratic.InOut)
      .start();

    // const res = await executeMoveCommand("Reset");
    // if(res.isSuccess=='1'){
    //   loadingInstance.close()
    //   proxy.$message.success("位置已重置", { duration: 1500 });
    // }

  } catch (error) {
    // console.error("重置失败:", error);
    // proxy.$message({
    //   message: error.message || "指令发送失败",
    //   type: "error",
    //   duration: 2000,
    //   showClose: true,
    // });
  } finally {
    loadingInstance.close();
    state.isMoving = false;
  }
};

// 窗口大小变化处理
const onWindowResize = () => {
  if (!state.renderer || !state.camera) return;

  const width = window.innerWidth;
  const height = window.innerHeight;

  state.camera.aspect = width / height;
  state.camera.updateProjectionMatrix();
  state.renderer.setSize(width, height);
};

// 检测目标位置是否会与其他柜子碰撞
const willCollide = (movingCabinet, targetX) => {
  const groupIndex = state.currentSelectedGroup;
  const group = state.buildingGroups[groupIndex];
  const movingSize = movingCabinet.userData.cabinetData;

  // 计算移动柜子的新边界
  const movingLeft = targetX - movingSize.L / 2;
  const movingRight = targetX + movingSize.L / 2;

  // 检查同组所有柜子
  for (const cabinet of group.children) {
    if (cabinet.uuid === movingCabinet.uuid) continue; // 跳过自己

    const cabinetData = cabinet.userData.cabinetData;
    const cabinetLeft = cabinet.position.x - cabinetData.L / 2;
    const cabinetRight = cabinet.position.x + cabinetData.L / 2;

    // AABB碰撞检测（轴对齐边界框）
    if (
        movingRight > cabinetLeft &&
        movingLeft < cabinetRight
    ) {
      return true; // 会碰撞
    }
  }
  return false;
};

// 生命周期钩子
onMounted(async () => {
  await init();
  animate();
});

onBeforeUnmount(() => {
  // 清理资源
  window.removeEventListener("mousemove", onMouseMove);
  window.removeEventListener("click", onClick);
  window.removeEventListener("keydown", handleKeydown);
  window.removeEventListener("resize", onWindowResize);

  TWEEN.removeAll();

  if (state.renderer) {
    state.renderer.dispose();
    state.renderer.forceContextLoss();
  }

  if (webglContainer.value) {
    while (webglContainer.value.firstChild) {
      webglContainer.value.removeChild(webglContainer.value.firstChild);
    }
  }
});
</script>

<!-- <style scoped>
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
  width: 250px;
  height: 500px;
  right: 20px;
  background: rgba(255, 255, 255, 0.97);
  border-radius: 12px;
  box-shadow: 0 4px 30px rgba(0, 0, 0, 0.15);
  display: flex;
  flex-direction: column;
  z-index: 1001;
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
  padding: 7px 16px;
  height: 44px; /* 明确设置高度（可选） */
  font-weight: 700; /* 或者使用数值600/700 */

  background: linear-gradient(135deg, #4a90e2, #63b3ff);
  color: white;
  display: flex;
  justify-content: center;
  align-items: center;
  cursor: grab;
  user-select: none;
}
.file-panel-header button {
  position: absolute;
  right: 16px;
  background: none;
  border: none;
  color: white;
  font-size: 18px;
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  padding: 0;
  transition: all 0.2s;
  opacity: 0.8;
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
  font-size: 14px;
  font-weight: 600;
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
  width: 220px;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 8px;
  padding: 10px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.2);
  z-index: 1000;
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
</style> &ndash;&gt;-->
<style scoped>
/* ============ 基础容器样式 ============ */
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

/* ============ 文件面板主样式 ============ */
.file-panel {
  /* 布局属性 */
  position: fixed;
  width: 250px;
  height: 500px;
  right: 20px;
  display: flex;
  flex-direction: column;
  z-index: 1001;
  overflow: hidden;

  /* 视觉样式 */
  background: rgba(255, 255, 255, 0.97);
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.3);
  box-shadow: 0 4px 30px rgba(0, 0, 0, 0.15);
  backdrop-filter: blur(5px);

  /* 过渡效果 */
  transition: transform 0.2s ease, box-shadow 0.2s ease, opacity 0.3s ease;
}

.file-panel:hover {
  box-shadow: 0 8px 40px rgba(0, 0, 0, 0.2);
  transform: translateY(-2px);
}

/* ============ 文件面板头部 ============ */
.file-panel-header {
  /* 布局属性 */
  padding: 7px 16px;
  height: 44px;
  display: flex;
  justify-content: center;
  align-items: center;
  font-weight: 700;

  /* 视觉样式 */
  background: linear-gradient(135deg, #4a90e2, #63b3ff);
  color: white;
  font-weight: 700;

  /* 交互属性 */
  cursor: grab;
  user-select: none;
}
.fixed-select-dropdown {
  position: fixed !important;
  margin-top: 5px !important;
}

.file-panel-header:active {
  cursor: grabbing;
  background: linear-gradient(135deg, #3a7bc8, #4a90e2);
}

.file-panel-header button {
  /* 定位属性 */
  position: absolute;
  right: 16px;

  /* 布局属性 */
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0;

  /* 视觉样式 */
  background: none;
  border: none;
  color: white;
  font-size: 18px;
  opacity: 0.8;

  /* 交互属性 */
  cursor: pointer;
  transition: all 0.2s;
}

/* ============ 文件列表区域 ============ */
.file-list {
  flex: 1;
  overflow-y: auto;
  padding: 10px;
  background: rgba(245, 247, 250, 0.8);
}

.file-item {
  /* 布局属性 */
  display: flex;
  padding: 10px;
  margin-bottom: 8px;

  /* 视觉样式 */
  background: white;
  border-radius: 6px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);

  /* 交互属性 */
  cursor: pointer;
  transition: all 0.2s;
}

.file-item:hover {
  background: #f0f7ff;
  transform: translateX(2px);
}

.file-item.active {
  background: #e1f0ff;
  border-left: 3px solid #4a90e2;
}

/* ============ 文件图标和信息 ============ */
.file-icon {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 12px;
  font-size: 14px;
  font-weight: 600;
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

/* ============ 文件预览区域 ============ */
.file-preview {
  height: 40%;
  display: flex;
  flex-direction: column;
  background: white;
  border-top: 1px solid #eee;
}

.preview-header {
  padding: 12px 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #eee;
}

.preview-content {
  flex: 1;
  padding: 15px;
  overflow: auto;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 预览类型特定样式 */
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

/* ============ 控制面板样式 ============ */
.control-panel {
  /* 布局属性 */
  width: 220px;
  padding: 10px;

  /* 视觉样式 */
  background: rgba(255, 255, 255, 0.9);
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.2);

  /* 其他属性 */
  z-index: 1000;
  user-select: none;
  transition: all 0.3s ease;
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

/* ============ 其他组件样式 ============ */
.selection-info {
  padding: 6px;
  background: #f8f8f8;
  border-radius: 4px;
  font-size: 14px;
  text-align: center;
  color: #333;
}

.selection-info small {
  color: #666;
  font-size: 12px;
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
