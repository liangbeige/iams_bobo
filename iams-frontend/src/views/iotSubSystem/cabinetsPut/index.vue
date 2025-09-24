<template>
  <div class="scene-container">

    <div class="config-selector">
      <select v-model="selectedConfig" @change="loadConfiguration">
        <option value="">请选择配置方案</option>
        <option
            v-for="config in existingConfigs"
            :key="config.id"
            :value="config"
        >
          {{ config.company }} - {{ config.warehouse }} ({{ config.scheme }})
        </option>
      </select>
    </div>

    <!-- 3D内容区域 -->
    <div ref="webglContainer" class="webgl-container"></div>

    <!-- 自定义模态框替代el-dialog -->
    <div v-if="showCabinetConfigModal" class="custom-modal">
      <div class="modal-content">
        <div class="modal-header">
          <h3>柜子配置</h3>
          <span class="close-btn" @click="showCabinetConfigModal = false">&times;</span>
        </div>
        <div class="modal-body">
          <div v-for="(col, index) in currentConfig.columns" :key="index" class="cabinet-item">
            <h4>柜子 {{ index + 1 }}</h4>
            <div class="form-group">
              <label>列号</label>
              <input type="text" v-model="col.column" min="1" max="100" class="form-input">
            </div>

            <template v-if="col.isFixed">
              <div class="form-group">
                <label>固定列端口</label>
                <input v-model="col.port" placeholder="请输入端口号" class="form-input">
              </div>
              <div class="form-group">
                <label>固定列IP</label>
                <input v-model="col.ip" placeholder="请输入IP地址" class="form-input">
              </div>
            </template>
          </div>
        </div>
        <div class="modal-footer">
          <button @click="showCabinetConfigModal = false" class="btn btn-cancel">取消</button>
          <button @click="cabinetConfig" class="btn btn-confirm">确定</button>
        </div>
      </div>
    </div>

    <div v-if="showConfigModal" class="custom-modal">
      <div class="modal-content">
        <div class="modal-header">
          <h3>生成配置文件</h3>
          <span class="close-btn" @click="showConfigModal = false">&times;</span>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>公司名称</label>
            <input type="text" v-model="companyName" class="form-input" placeholder="请输入公司名称">
          </div>
          <div class="form-group">
            <label>仓库编号</label>
            <input type="text" v-model="warehouseCode" class="form-input" placeholder="请输入仓库编号">
          </div>
          <div class="form-group">
            <label>方案编号</label>
            <input type="text" v-model="scheme" class="form-input" placeholder="请输入方案编号">
          </div>
        </div>
        <div class="modal-footer">
          <button @click="showConfigModal = false" class="btn btn-cancel">取消</button>
          <button @click="generateConfigFile" class="btn btn-confirm">确定</button>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount, nextTick, markRaw } from 'vue';
import { saveCabinetConfig, getCabinetConfigList } from "@/api/system/cabinet";
import * as THREE from "three";
import { OrbitControls } from "three/examples/jsm/controls/OrbitControls.js";
import { TransformControls } from 'three/examples/jsm/controls/TransformControls.js';
import GUI from 'lil-gui';

// DOM引用
const webglContainer = ref(null);

// 状态管理
const scene = ref(null);
const camera = ref(null);
const renderer = ref(null);
const orbitControls = ref(null);
const transformControls = ref(null);
const raycaster = ref(null);
const mouse = ref(new THREE.Vector2());
const clickableObjects = ref([]);
const boxGroups = ref([]);
const guiParams = reactive({
  boxCount: 3,
  fixedColumn: 1
});
const plane = ref(null);
const walls = ref([]);
const showCabinetConfigModal = ref(false);
const selectedGroup = ref(null);
const cabinetConfigs = ref([]);
const currentConfig = reactive({
  columns: []
});
const showConfigModal = ref(false);
const companyName = ref('');
const warehouseCode = ref('');
const scheme = ref('');
const existingConfigs = ref([]);
const animationFrameId = ref(null);
const gui = ref(null);
const positionFolder = ref(null);
const positionController = reactive({
  x: 0,
  z: 0
});
const selectedConfig = ref(null);

// 初始化
onMounted(() => {
  nextTick(async () => {
    init();
    animate();
    await getConfigList();
    window.addEventListener("resize", onWindowResize);
    window.addEventListener("click", onClick);
    initGUI();
  });
});

// 清理
onBeforeUnmount(() => {
  // 停止动画循环
  if (animationFrameId.value) {
    cancelAnimationFrame(animationFrameId.value);
  }

  // 移除事件监听器
  window.removeEventListener("mousemove", onMouseMove);
  window.removeEventListener("click", onClick);
  window.removeEventListener("resize", onWindowResize);

  // 清理控制器
  if (orbitControls.value) {
    orbitControls.value.dispose();
  }
  if (transformControls.value) {
    transformControls.value.dispose();
  }

  // 清理GUI
  if (gui.value) {
    gui.value.destroy();
  }

  // 清理场景
  if (renderer.value) {
    renderer.value.dispose();
  }
});

// 初始化GUI
const initGUI = () => {
  gui.value = new GUI({
    autoPlace: false,
    container: webglContainer.value
  });

  // 设置GUI样式
  gui.value.domElement.style.position = 'absolute';
  gui.value.domElement.style.top = '10px';
  gui.value.domElement.style.right = '10px';
  gui.value.domElement.style.zIndex = '100';

  // 箱子数量设置
  const boxCountCtrl = gui.value.add(guiParams, 'boxCount', 1, 20, 1)
      .name('柜子数量');

  // 固定列设置
  const fixedColumnCtrl = gui.value.add(guiParams, 'fixedColumn', 1, guiParams.boxCount, 1)
      .name('固定列');

  // 当箱子数量改变时，更新固定列的最大值
  boxCountCtrl.onChange(value => {
    // 确保固定列不超过新的箱子数量
    if (guiParams.fixedColumn > value) {
      guiParams.fixedColumn = value;
    }
    fixedColumnCtrl.max(value);
    fixedColumnCtrl.updateDisplay();
  });

  // 添加箱子组按钮
  gui.value.add({
    addBoxGroup: () => {
      addBoxGroup();
    }
  }, 'addBoxGroup').name('添加柜子组');

  // 删除选中组按钮
  gui.value.add({
    removeSelectedGroup: () => {
      removeSelectedGroup();
    }
  }, 'removeSelectedGroup').name('删除选中组');

  // 创建位置控制文件夹
  positionFolder.value = gui.value.addFolder('位置控制');
  positionFolder.value.open();

  // 添加位置控制器
  positionFolder.value.add(positionController, 'x', -2775, 2775, 1)
      .name('X 位置')
      .onChange(value => {
        if (transformControls.value && transformControls.value.object) {
          transformControls.value.object.position.x = value;
        }
      });

  positionFolder.value.add(positionController, 'z', -2775, 2775, 1)
      .name('Z 位置')
      .onChange(value => {
        if (transformControls.value && transformControls.value.object) {
          transformControls.value.object.position.z = value;
        }
      });

  gui.value.add({
    configCabinet: () => {
      if (transformControls.value && transformControls.value.object) {
        openConfigDialog();
      } else {
        alert('请先选择一个柜子组');
      }
    }
  }, 'configCabinet').name('配置柜子信息');

  gui.value.add({
    generateConfig: () => {
      generateConfig(); // 改为调用验证方法
    }
  }, 'generateConfig').name('生成配置文件');
};

// 添加箱子组
const addBoxGroup = () => {
  const count = guiParams.boxCount;
  const fixedColumn = guiParams.fixedColumn;

  // 创建组
  const group = markRaw(new THREE.Group());
  group.userData = {
    isBoxGroup: true,
    selectable: true,
    fixedColumn: fixedColumn
  };

  // 创建材质
  const normalMaterial = markRaw(new THREE.MeshPhongMaterial({
    color: 0x8080ff,  // 普通列颜色
    specular: 0x4488ee,
  }));

  const fixedMaterial = markRaw(new THREE.MeshPhongMaterial({
    color: 0xffcc00,  // 固定列颜色(黄色)
    specular: 0xffee88,
  }));

  // 创建指定数量的箱子
  for (let i = 0; i < count; i++) {
    const geometry = markRaw(new THREE.BoxGeometry(100, 610, 750));
    // 判断是否是固定列
    const isFixed = i === fixedColumn - 1;
    const box = markRaw(new THREE.Mesh(
        geometry,
        isFixed ? fixedMaterial : normalMaterial
    ));

    // 所有箱子位置相同(完全重叠)
    box.position.set(i * (100 + 10), 225, 0);

    // 设置renderOrder解决渲染冲突
    box.renderOrder = i;

    // 添加用户数据
    box.userData = {
      selectable: false,
      isPartOfGroup: true,
      parentGroup: group,
      isFixed: isFixed  // 标记是否为固定列
    };

    group.add(box);
  }

  scene.value.add(group);
  boxGroups.value.push(group);
  clickableObjects.value.push(group);
};

// 窗口大小改变处理
const onWindowResize = () => {
  if (!renderer.value || !camera.value) return;

  const width = webglContainer.value.clientWidth;
  const height = webglContainer.value.clientHeight;

  camera.value.aspect = width / height;
  camera.value.updateProjectionMatrix();
  renderer.value.setSize(width, height);
  renderer.value.setPixelRatio(window.devicePixelRatio || 1);
};

// 初始化场景
const init = () => {
  try {
    // 初始化场景
    scene.value = markRaw(new THREE.Scene());
    raycaster.value = markRaw(new THREE.Raycaster());

    // 初始化相机
    camera.value = markRaw(new THREE.PerspectiveCamera(
        50,
        window.innerWidth / window.innerHeight,
        0.1,
        20000
    ));
    camera.value.position.set(1200, 1000, 1800);
    camera.value.lookAt(scene.value.position);

    // 初始化渲染器
    renderer.value = markRaw(new THREE.WebGLRenderer({
      antialias: true,
      logarithmicDepthBuffer: true,
    }));
    renderer.value.setClearColor("rgb(175,216,250)", 1.0);
    renderer.value.setSize(window.innerWidth, window.innerHeight);
    renderer.value.shadowMap.enabled = true;
    webglContainer.value.appendChild(renderer.value.domElement);

    initControls();

    // 添加坐标轴辅助器
    const axesHelper = markRaw(new THREE.AxesHelper(3000));
    scene.value.add(axesHelper);

    // 添加地面
    const planeGeometry = markRaw(new THREE.PlaneGeometry(6000, 6000));
    const planeMaterial = markRaw(new THREE.MeshPhongMaterial({
      color: 0x8080ff,
      specular: 0x4488ee,
      shininess: 12,
    }));
    plane.value = markRaw(new THREE.Mesh(planeGeometry, planeMaterial));
    plane.value.receiveShadow = true;
    plane.value.rotation.x = -0.5 * Math.PI;
    plane.value.position.set(0, 0, 0);
    scene.value.add(plane.value);

    // 添加四周墙壁
    addWalls();

    // 添加光源
    const directionalLight = markRaw(new THREE.DirectionalLight(0xffffff, 0.5));
    directionalLight.position.set(0, 1000, 0).normalize();
    scene.value.add(directionalLight);

    const ambientLight = markRaw(new THREE.AmbientLight(0x505050, 0.2));
    scene.value.add(ambientLight);

    // 绑定鼠标移动事件
    window.addEventListener("mousemove", onMouseMove, false);
  } catch (error) {
    console.error('场景初始化失败:', error);
  }
};

// 初始化控制器
const initControls = () => {
  try {
    // 设置移动范围约束
    const minX = -3000;
    const maxX = 3000;
    const minZ = -3000;
    const maxZ = 3000;

    // 初始化 OrbitControls
    orbitControls.value = markRaw(new OrbitControls(camera.value, renderer.value.domElement));
    orbitControls.value.minDistance = 200;
    orbitControls.value.maxDistance = 8000;
    orbitControls.value.maxPolarAngle = Math.PI / 2.01;

    // 初始化 TransformControls
    transformControls.value = markRaw(new TransformControls(camera.value, renderer.value.domElement));

    // 设置默认模式为平移（translate）
    transformControls.value.setMode('translate');

    // 禁用Y轴移动
    transformControls.value.showY = false;

    // 允许旋转、平移和缩放
    transformControls.value.showX = true;
    transformControls.value.showZ = true;

    // 监听变换事件以强制限制Y轴位置
    transformControls.value.addEventListener('objectChange', () => {
      if (transformControls.value.object?.userData?.isBoxGroup) {
        const group = transformControls.value.object;
        applyGroupPositionLimits(group);
        updatePositionControllers(group.position);
      }
    });

    // 监听变换开始和结束事件
    transformControls.value.addEventListener('mouseDown', () => {
      orbitControls.value.enabled = false; // 变换开始时禁用OrbitControls
    });

    transformControls.value.addEventListener('mouseUp', () => {
      orbitControls.value.enabled = true; // 变换结束后恢复OrbitControls
    });

    scene.value.add(transformControls.value.getHelper());
  } catch (error) {
    console.error('控制器初始化失败:', error);
  }
};

// 添加四周墙壁
const addWalls = () => {
  const wallMaterial = markRaw(new THREE.MeshStandardMaterial({
    color: 0xf0f0f0,
    side: THREE.DoubleSide,
  }));
  const wallHeight = 500;

  // 前墙
  const frontWall = markRaw(new THREE.Mesh(
      new THREE.PlaneGeometry(6000, wallHeight),
      wallMaterial
  ));
  frontWall.position.set(0, wallHeight / 2, -3000);
  scene.value.add(frontWall);
  walls.value.push(frontWall);

  // 后墙
  const backWall = markRaw(new THREE.Mesh(
      new THREE.PlaneGeometry(6000, wallHeight),
      wallMaterial
  ));
  backWall.position.set(0, wallHeight / 2, 3000);
  scene.value.add(backWall);
  walls.value.push(backWall);

  // 左墙
  const leftWall = markRaw(new THREE.Mesh(
      new THREE.PlaneGeometry(6000, wallHeight),
      wallMaterial
  ));
  leftWall.position.set(-3000, wallHeight / 2, 0);
  leftWall.rotation.y = 0.5 * Math.PI;
  scene.value.add(leftWall);
  walls.value.push(leftWall);

  // 右墙
  const rightWall = markRaw(new THREE.Mesh(
      new THREE.PlaneGeometry(6000, wallHeight),
      wallMaterial
  ));
  rightWall.position.set(3000, wallHeight / 2, 0);
  rightWall.rotation.y = -0.5 * Math.PI;
  scene.value.add(rightWall);
  walls.value.push(rightWall);
};

// 鼠标移动事件
const onMouseMove = (event) => {
  if (!renderer.value) return;

  // 更新鼠标位置
  const rect = renderer.value.domElement.getBoundingClientRect();
  mouse.value.x = ((event.clientX - rect.left) / rect.width) * 2 - 1;
  mouse.value.y = -((event.clientY - rect.top) / rect.height) * 2 + 1;
};

// 鼠标点击事件
const onClick = (event) => {
  if (!renderer.value || !raycaster.value || !camera.value) return;

  // 更新鼠标位置
  const rect = renderer.value.domElement.getBoundingClientRect();
  mouse.value.x = ((event.clientX - rect.left) / rect.width) * 2 - 1;
  mouse.value.y = -((event.clientY - rect.top) / rect.height) * 2 + 1;

  // 执行射线检测
  const selectableGroups = clickableObjects.value.filter(
      obj => obj.userData.isBoxGroup
  );
  raycaster.value.setFromCamera(mouse.value, camera.value);
  const intersects = raycaster.value.intersectObjects(selectableGroups, true);

  if (intersects.length > 0) {
    // 找到被点击的组(通过父组判断)
    let selectedGroup = intersects[0].object;
    while(selectedGroup && !selectedGroup.userData.isBoxGroup) {
      selectedGroup = selectedGroup.parent;
    }

    if(selectedGroup) {
      // 只有当不是当前选中对象时才重新附加
      if (!transformControls.value.object || transformControls.value.object !== selectedGroup) {
        transformControls.value.attach(selectedGroup);
        orbitControls.value.enabled = false;
        updatePositionControllers(selectedGroup.position);
      }
      return;
    }
  }
  transformControls.value.detach();
  orbitControls.value.enabled = true;

  // 没有选中任何组时清空位置显示
  clearPositionControllers();
};

// 动画循环
const animate = () => {
  animationFrameId.value = requestAnimationFrame(animate);

  // 更新控制器和渲染场景
  if (orbitControls.value) orbitControls.value.update();
  if (renderer.value && scene.value && camera.value) {
    renderer.value.render(scene.value, camera.value);
  }
};

// 移除选中的组
const removeSelectedGroup = () => {
  // 获取当前选中的组
  const selectedGroup = transformControls.value?.object;

  if (selectedGroup && selectedGroup.userData.isBoxGroup) {
    // 从场景中移除组
    scene.value.remove(selectedGroup);
    // 从boxGroups数组中移除
    boxGroups.value = boxGroups.value.filter(group => group !== selectedGroup);
    // 从clickableObjects数组中移除
    clickableObjects.value = clickableObjects.value.filter(obj => obj !== selectedGroup);
    // 清除transformControls的引用
    transformControls.value.detach();
    console.log('组已删除');
  } else {
    console.warn('没有选中任何组或选中的不是柜子组');
  }
};

// 更新位置控制器显示
const updatePositionControllers = (position) => {
  positionController.x = position.x;
  positionController.z = position.z;

  // 更新GUI控制器值
  if (positionFolder.value) {
    positionFolder.value.controllers.forEach(controller => {
      controller.updateDisplay();
    });
  }
};

// 清空位置控制器显示
const clearPositionControllers = () => {
  positionController.x = 0;
  positionController.z = 0;

  // 更新GUI控制器值
  if (positionFolder.value) {
    positionFolder.value.controllers.forEach(controller => {
      controller.updateDisplay();
    });
  }
};

// 生成配置文件
const generateConfigFile = async () => {
  if (!companyName.value.trim() || !warehouseCode.value.trim()) {
    alert('请填写完整的公司名称和仓库编号');
    return;
  }

  const isDuplicate = existingConfigs.value.some(config =>
      config.warehouse === warehouseCode.value &&
      config.company === companyName.value &&
      config.scheme === scheme.value
  );

  if (isDuplicate) {
    try {
      await confirm('该方案已被保存，是否修改？');
    } catch (cancel) {
      showConfigModal.value = false;
      return;
    }
  }

  // 生成配置文件结构
  const config = {
    cabinets: [
      {
        warehouse: "WarehouseA",
        cabinetGroup: boxGroups.value.map((group, groupIndex) => {
          // 获取该组的配置信息
          const groupConfig = group.userData.config || { columns: [] };
          const groupCValue = Math.floor(Math.random() * 4);
          const groupFixedColumn = group.userData.fixedColumn;

          return {
            groupName: `Group${groupIndex + 1}`,
            cabinet: group.children.map((cabinetMesh, cabinetIndex) => {
              // 获取柜子位置
              const pos = new THREE.Vector3();
              cabinetMesh.getWorldPosition(pos);
              const roundedPos = {
                x: Math.round(pos.x),
                y: Math.round(pos.y),
                z: Math.round(pos.z)
              };

              // 获取对应的列配置
              const columnConfig = groupConfig.columns[cabinetIndex] || {};

              // 生成层数据
              const layers = [
                {Y: 55}, {Y: 155}, {Y: 255}, {Y: 355},{Y: 455}, {Y: 555}
              ].map(layer => ({
                X: roundedPos.x - 1,
                Z: roundedPos.z,
                Y: layer.Y,
                L: 100,
                W: 720,
                H: 90
              }));

              // 构建柜子配置
              const isFixed = (cabinetIndex === groupFixedColumn - 1);
              const cabinetData = {
                X: roundedPos.x,
                Z: roundedPos.z,
                Y: 0,
                L: 100,
                W: 750,
                H: 610,
                C: groupCValue,
                immobilization: isFixed ? 1 : 0,
                columnNumber: columnConfig.column || (cabinetIndex + 1).toString(), // 使用配置的列号或默认值
                layers: layers
              };

              // 如果是固定列则添加网络配置
              if (isFixed) {
                cabinetData.networkConfig = {
                  ip: columnConfig.ip || "192.168.1.100", // 默认IP
                  port: columnConfig.port || "502"        // 默认端口
                };
              }

              return cabinetData;
            })
          };
        })
      }
    ]
  };

  // 创建JSON文件
  const jsonString = JSON.stringify(config, null, 2);
  console.log("jsonString", jsonString);

  const formData = new FormData();
  formData.append('configData', jsonString);
  formData.append('warehouse', warehouseCode.value);
  formData.append('company', companyName.value);
  formData.append('scheme', scheme.value);

  try {
    // 调用保存接口
    await saveCabinetConfig(formData).then(response => {
      if (response) {
        getCabinetConfigList().then(data => {
          existingConfigs.value = data;
        });
        // 下载文件
        const blob = new Blob([jsonString], {type: "application/json"});
        const link = document.createElement("a");
        link.href = URL.createObjectURL(blob);
        link.download = `layout-config_${new Date().getTime()}.json`;
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        showConfigModal.value = false;
        alert('配置已保存并下载');
        setTimeout(() => {
          window.location.reload();
        }, 500);
      }
    })
  } catch (error) {
    console.error('保存失败:', error);
    alert('配置保存失败');
  }
};

// 应用组位置限制
const applyGroupPositionLimits = (group) => {
  if (!group.userData.isBoxGroup) return;

  const children = group.children;
  let minX = Infinity, maxX = -Infinity;
  let minZ = Infinity, maxZ = -Infinity;

  const worldPos = new THREE.Vector3();

  // 计算所有子物体的世界坐标边界
  children.forEach(child => {
    child.getWorldPosition(worldPos);
    minX = Math.min(minX, worldPos.x);
    maxX = Math.max(maxX, worldPos.x);
    minZ = Math.min(minZ, worldPos.z);
    maxZ = Math.max(maxZ, worldPos.z);
  });

  let deltaX = 0;
  let deltaZ = 0;

  // 处理X轴越界
  if (minX < -3000) {
    deltaX = -3000 - minX;
  } else if (maxX > 3000) {
    deltaX = 3000 - maxX;
  }

  // 处理Z轴越界
  if (minZ < -3000) {
    deltaZ = -3000 - minZ;
  } else if (maxZ > 3000) {
    deltaZ = 3000 - maxZ;
  }

  // 应用位置修正
  if (deltaX !== 0 || deltaZ !== 0) {
    group.position.x += deltaX;
    group.position.z += deltaZ;
    // 递归检查修正后的位置
    applyGroupPositionLimits(group);
  }

  // 保持Y轴位置为0
  group.position.y = 0;
};

// 打开配置对话框
const openConfigDialog = () => {
  const group = transformControls.value?.object;

  if (!group || !group.userData.isBoxGroup) {
    console.warn('未选中柜子组或选中的不是柜子组');
    return;
  }

  selectedGroup.value = group;
  currentConfig.columns = [];

  const count = group.children.length;
  const fixedIndex = group.userData.fixedColumn - 1;

  for (let i = 0; i < count; i++) {
    const existingConfig = group.userData.config?.columns[i] || {};
    currentConfig.columns.push({
      column: existingConfig.column || (i + 1).toString(),
      isFixed: i === fixedIndex,
      port: existingConfig.port || '',
      ip: existingConfig.ip || ''
    });
  }

  showCabinetConfigModal.value = true;
};

// 保存柜子配置
const cabinetConfig = () => {
  if (selectedGroup.value) {
    // 确保所有列号都是字符串
    const config = JSON.parse(JSON.stringify(currentConfig));
    config.columns.forEach(col => {
      if (typeof col.column !== 'string') {
        col.column = col.column.toString();
      }
    });
    console.log("config", config);
    selectedGroup.value.userData.config = config;
  }
  showCabinetConfigModal.value = false;
};

// 生成配置
const generateConfig = () => {
  const unConfiguredGroups = boxGroups.value.filter(group => {
    if (!group.userData.config) return true;
    return group.userData.config.columns.some((col, index) => {
      const isFixed = index === group.userData.fixedColumn - 1;
      if (!col.column.trim()) return true;
      if (isFixed && (!col.ip.trim() || !col.port.trim())) return true;
      return false;
    });
  });

  if (unConfiguredGroups.length > 0) {
    alert(`${unConfiguredGroups.length}个柜子组未完成配置`);
    return;
  }

  showConfigModal.value = true; // 显示配置弹窗
};

const getConfigList = async () => {
  try {
    const { data } = await getCabinetConfigList();
    console.log("getConfigList", data);
    existingConfigs.value = data.map(item => ({
      ...item,
      configData: JSON.parse(item.configData) // 解析存储的JSON配置
    }));
    console.log("existingConfigs.value", existingConfigs.value);
  } catch (error) {
    console.error('获取配置列表失败:', error);
  }
};

const loadConfiguration = async () => {
  if (!selectedConfig.value) return;

  // 清空现有场景
  boxGroups.value.forEach(group => {
    scene.value.remove(group);
    group.children.forEach(child => child.geometry.dispose());
  });
  boxGroups.value = [];
  clickableObjects.value = [];

  // 更新基础信息
  companyName.value = selectedConfig.value.company;
  warehouseCode.value = selectedConfig.value.warehouse;
  scheme.value = selectedConfig.value.scheme;

  // 解析配置数据
  const configData = selectedConfig.value.configData;

  // 遍历柜组配置
  configData.cabinets.forEach(cabinet => {
    cabinet.cabinetGroup.forEach(groupConfig => {
      // 创建柜子组
      const group = markRaw(new THREE.Group());
      group.userData = {
        isBoxGroup: true,
        selectable: true,
        fixedColumn: 1,
        config: { columns: [] }
      };

      // 动态计算组位置（关键修改点）
      const firstCabinet = groupConfig.cabinet[0];
      const groupX = firstCabinet.X - (firstCabinet.L / 2); // 假设水平排列
      const groupZ = firstCabinet.Z;
      group.position.set(groupX, 0, groupZ);

      // 创建材质
      const normalMaterial = new THREE.MeshPhongMaterial({ color: 0x8080ff });
      const fixedMaterial = new THREE.MeshPhongMaterial({ color: 0xffcc00 });

      // 创建每个柜子（坐标转换）
      groupConfig.cabinet.forEach((cabinetConfig, index) => {
        const geometry = new THREE.BoxGeometry(
            cabinetConfig.L,
            cabinetConfig.H,
            cabinetConfig.W
        );

        // 判断是否为固定列
        const isFixed = cabinetConfig.immobilization === 1;
        const material = isFixed ? fixedMaterial : normalMaterial;

        const mesh = new THREE.Mesh(geometry, material);

        // 世界坐标转局部坐标（关键修改点）
        const localX = cabinetConfig.X - groupX;
        const localY = cabinetConfig.Y + cabinetConfig.H/2;
        const localZ = cabinetConfig.Z - groupZ;
        mesh.position.set(localX, localY, localZ);

        // 存储配置信息
        group.userData.config.columns.push({
          column: cabinetConfig.columnNumber,
          isFixed: isFixed,
          port: cabinetConfig.networkConfig?.port || '',
          ip: cabinetConfig.networkConfig?.ip || ''
        });

        mesh.userData = {
          selectable: false,
          isPartOfGroup: true,
          parentGroup: group,
          isFixed: isFixed
        };

        group.add(mesh);
      });

      // 强制更新矩阵（关键修改点）
      group.updateMatrixWorld(true);
      scene.value.add(group);
      boxGroups.value.push(group);
      clickableObjects.value.push(group);
    });
  });

  // 场景全局矩阵更新（关键修改点）
  nextTick(() => {
    scene.value.updateMatrixWorld(true);
    renderer.value.render(scene.value, camera.value);
  });

  // 更新视角
  camera.value.position.set(1200, 1000, 1800);
  camera.value.lookAt(scene.value.position);
};
</script>

<style scoped>
/* 样式部分保持不变 */
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
  position: relative;
  z-index: 0;
}

/* 自定义模态框样式 */
.custom-modal {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  width: 60%;
  max-height: 80vh;
  background-color: white;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  display: flex;
  flex-direction: column;
}

.modal-header {
  padding: 20px;
  border-bottom: 1px solid #ebeef5;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.modal-header h3 {
  margin: 0;
  color: #303133;
}

.close-btn {
  font-size: 20px;
  cursor: pointer;
}

.modal-body {
  padding: 20px;
  overflow-y: auto;
  flex-grow: 1;
}

.cabinet-item {
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid #eee;
}

.cabinet-item h4 {
  margin: 0 0 15px 0;
  color: #409EFF;
}

.form-group {
  margin-bottom: 15px;
  display: flex;
  align-items: center;
}

.form-group label {
  width: 100px;
  text-align: right;
  padding-right: 12px;
  font-size: 14px;
  color: #606266;
}

.form-input {
  flex: 1;
  padding: 8px 15px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  color: #606266;
}

.form-input:focus {
  outline: none;
  border-color: #409EFF;
}

.modal-footer {
  padding: 15px 20px;
  border-top: 1px solid #ebeef5;
  display: flex;
  justify-content: flex-end;
}

.btn {
  padding: 9px 15px;
  border-radius: 4px;
  cursor: pointer;
  margin-left: 10px;
}

.btn-cancel {
  background-color: #fff;
  border: 1px solid #dcdfe6;
  color: #606266;
}

.btn-cancel:hover {
  color: #409EFF;
  border-color: #c6e2ff;
  background-color: #ecf5ff;
}

.btn-confirm {
  background-color: #409EFF;
  border: 1px solid #409EFF;
  color: white;
}

.btn-confirm:hover {
  background-color: #66b1ff;
  border-color: #66b1ff;
}

.lil-gui.autoPlace {
  right: 0 !important;
  top: 0 !important;
  z-index: 9999 !important;
}

.high-zindex-confirm {
  z-index: 10001 !important;
}

.config-selector {
  position: absolute;
  top: 20px;
  left: 20px;
  z-index: 100;
  background: rgba(255,255,255,0.8);
  padding: 10px;
  border-radius: 4px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.config-selector select {
  padding: 8px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  min-width: 300px;
}
</style>
