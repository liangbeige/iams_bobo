<template>
    <div class="app-container"
         v-loading="loading"
         element-loading-text="正在全力加载档案库..."
         element-loading-spinner="el-icon-loading"
         element-loading-background="rgba(255, 255, 255, 0.8)"
    >
        <el-form :model="archiveParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
            <el-form-item label="档号" prop="danghao">
                <el-input v-model="archiveParams.danghao" placeholder="请输入档号" clearable @keyup.enter="queryArchive" />
            </el-form-item>
            <el-form-item>
                <el-button type="primary" icon="Search" @click="queryArchive">搜索</el-button>
                <el-button icon="Refresh" @click="resetQuery">重置</el-button>
            </el-form-item>
        </el-form>

        <el-row :gutter="2" class="mb8">
            <div style="">
                <el-checkbox-group v-model="quNoList" @change="handleQuery">
                    <el-checkbox v-for="dict in iams_shiti_location_quhao" :key="dict.value" :label="dict.value">
                        {{ dict.label }}
                    </el-checkbox>
                </el-checkbox-group>
            </div>

            <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
        </el-row>

        <el-divider><el-icon><star-filled /></el-icon><el-icon><star-filled /></el-icon><el-icon><star-filled /></el-icon></el-divider>

        <div class="main-content">
            <div class="left-sidebar">
                <div class="directory-container">
                    <div class="directory-header">
                        <h3>档案柜目录</h3>
                    </div>
                    <div class="directory-content">
                        <el-tree :data="cachedTreeData" :props="treeProps" node-key="id" :expand-on-click-node="false"
                                 @node-click="handleNodeClick" class="directory-tree" :render-after-expand="false"
                                 :lazy="false">
                            <template #default="{ node, data }">
                                <span class="tree-node-label" :class="getNodeClass(data)">
                                    {{ node.label }}
                                </span>
                            </template>
                        </el-tree>
                    </div>
                </div>
            </div>

            <div class="right-content">
                <el-row :gutter="20">
                    <el-col v-for="(item, index) in cabinetCard" :key="`${item.quNo}-${item.colNo}`">
                        <el-card style="max-width: 100%; margin-bottom: 20px;">
                            <template #header>
                                <div class="card-header">
                                    <div class="card-header-top">
                                        {{ formatQuCol(item.quNo, item.colNo) }}
                                    </div>

                                    <div class="card-header-bottom">
                                        已用：{{ item.size }}
                                    </div>
                                </div>
                            </template>

                            <div class="card-body">
                                <div class="card-left">
                                    <div class="section-header">{{ getLeftSectionTitle(item.quNo) }}</div>
                                    <div v-if="getCompartmentsByCabinetAndSide(item.quNo, item.colNo, 'left').length > 0"
                                         :class="getGridContainerClass(item.quNo, 'left')">
                                        <div v-for="comp in getCompartmentsByCabinetAndSide(item.quNo, item.colNo, 'left')"
                                             :key="comp.id" :ref="(el) => setGridRef(el, comp.id)" :data-id="comp.id"
                                             :class="['grid-cell', getCellClass(comp), { 'highlighted': isHighlighted(comp.id) }]"
                                             @mouseenter="() => loadArchiveData(item.quNo, item.colNo, getZyNoForSide(item.quNo, 'left'), comp)">
                                            <el-tooltip placement="right" :show-after="300">
                                                <template #content>
                                                    <div class="tooltip-content">
                                                        <p>位置：{{ comp.leNo }}节{{ comp.divNo }}层</p>
                                                        <p>已用：{{ comp.size }}</p>

                                                        <div class="tooltip-table"
                                                             v-if="archiveMap[comp.id]?.length > 0">
                                                            <table>
                                                                <thead>
                                                                <tr>
                                                                    <th>档号</th>
                                                                    <th>名称</th>
                                                                </tr>
                                                                </thead>
                                                                <tbody>
                                                                <tr v-for="archive in archiveMap[comp.id]"
                                                                    :key="archive.id">
                                                                    <td><router-link
                                                                        :to="{ path: '/manage/archive/arc-detail', query: { id: archive.id } }"
                                                                        style="color: #409EFF; text-decoration: underline; cursor: pointer;">
                                                                        {{ archive.danghao }}
                                                                    </router-link></td>
                                                                    <td>{{ archive.name }}</td>
                                                                </tr>
                                                                </tbody>
                                                            </table>
                                                        </div>
                                                        <div v-else class="no-data">暂无档案</div>

                                                    </div>
                                                </template>

                                                <div :class="['grid-cell', getCellClass(comp)]">
                                                    {{ comp.leNo }}-{{ comp.divNo }}
                                                </div>
                                            </el-tooltip>
                                        </div>
                                    </div>
                                    <div v-else class="grid-empty">数据加载中</div>
                                </div>

                                <div class="divider"></div>

                                <div class="card-right">
                                    <div class="section-header">{{ getRightSectionTitle(item.quNo) }}</div>
                                    <div v-if="getCompartmentsByCabinetAndSide(item.quNo, item.colNo, 'right').length > 0"
                                         :class="getGridContainerClass(item.quNo, 'right')">
                                        <div v-for="comp in getCompartmentsByCabinetAndSide(item.quNo, item.colNo, 'right')"
                                             :key="comp.id" :ref="(el) => setGridRef(el, comp.id)" :data-id="comp.id"
                                             :class="['grid-cell', getCellClass(comp), { 'highlighted': isHighlighted(comp.id) }]"
                                             @mouseenter="() => loadArchiveData(item.quNo, item.colNo, getZyNoForSide(item.quNo, 'right'), comp)">
                                            <el-tooltip placement="left" :show-after="300">
                                                <template #content>
                                                    <div class="tooltip-content">
                                                        <p>位置：{{ comp.leNo }}节{{ comp.divNo }}层</p>
                                                        <p>已用：{{ comp.size }}</p>

                                                        <div class="tooltip-table"
                                                             v-if="archiveMap[comp.id]?.length > 0">
                                                            <table>
                                                                <thead>
                                                                <tr>
                                                                    <th>档号</th>
                                                                    <th>名称</th>
                                                                </tr>
                                                                </thead>
                                                                <tbody>
                                                                <tr v-for="archive in archiveMap[comp.id]"
                                                                    :key="archive.id">
                                                                    <td><router-link
                                                                        :to="{ path: '/manage/archive/arc-detail', query: { id: archive.id } }"
                                                                        style="color: #409EFF; text-decoration: underline; cursor: pointer;">
                                                                        {{ archive.danghao }}
                                                                    </router-link></td>
                                                                    <td>{{ archive.name }}</td>
                                                                </tr>
                                                                </tbody>
                                                            </table>
                                                        </div>
                                                        <div v-else class="no-data">暂无档案</div>

                                                    </div>
                                                </template>

                                                <div :class="['grid-cell', getCellClass(comp)]">
                                                    {{ comp.leNo }}-{{ comp.divNo }}
                                                </div>
                                            </el-tooltip>
                                        </div>
                                    </div>
                                    <div v-else class="grid-empty">数据加载中</div>
                                </div>
                            </div>

                        </el-card>
                    </el-col>
                </el-row>
            </div>
        </div>
    </div>
</template>

<script setup name="Cabinet">
import { listCabinet, getCabinet, delCabinet, addCabinet, updateCabinet, listCabinetByQuNoList } from "@/api/manage/cabinet";
import { listCompartment } from "@/api/manage/compartment";
import { listArchiveByLocation, listArchive } from "@/api/manage/archive";
import { useRoute } from 'vue-router';
import { ref, computed, shallowRef, markRaw } from "vue";

const { proxy } = getCurrentInstance();

const cabinetList = ref([]);
const cabinetCard = ref([]);
const quNoList = ref([]);
const compartmentList = ref([]);
const open = ref(false);
const loading = ref(true);
const showSearch = ref(true);
const ids = ref([]);
const single = ref(true);
const multiple = ref(true);
const total = ref(0);
const route = useRoute();
const highlightedCompartment = ref(null);
const gridRefs = ref({});
const sizeCache = {};
const cachedCards = {};
const archiveMap = ref({});

// 使用 shallowRef 优化树形数据
const cachedTreeData = shallowRef([]);
const allTreeData = new Map(); // 缓存所有区域的树形数据

// 树形控件配置
const treeProps = markRaw({
    children: 'children',
    label: 'label'
});

// 优化的储物格查找 - 使用Map缓存
const compartmentMap = new Map();
const cabinetMap = new Map();

// 预构建索引
function buildIndexes() {
    // 清空旧索引
    compartmentMap.clear();
    cabinetMap.clear();

    // 构建档案柜索引
    cabinetList.value.forEach(cab => {
        const key = `${cab.quNo}-${cab.colNo}-${cab.zyNo}`;
        cabinetMap.set(key, cab);
    });

    // 构建储物格索引
    compartmentList.value.forEach(comp => {
        const cabinet = cabinetList.value.find(cab => cab.id === comp.cabinetId);
        if (cabinet) {
            const key = `${cabinet.quNo}-${cabinet.colNo}-${cabinet.zyNo}-${comp.leNo}-${comp.divNo}`;
            compartmentMap.set(key, comp);
        }
    });
}

// 优化的查找储物格函数
function findCompartment(quNo, colNo, side, leNo, divNo) {
    const key = `${quNo}-${colNo}-${side}-${leNo}-${divNo}`;
    return compartmentMap.get(key) || null;
}

// 预生成所有区域的树形数据
function preGenerateAllTreeData() {
    for (let quNo = 1; quNo <= 4; quNo++) {
        if (!allTreeData.has(quNo)) {
            allTreeData.set(quNo, generateTreeDataForRegion(quNo));
        }
    }
}

// 为单个区域生成树形数据
function generateTreeDataForRegion(quNo) {
    const regionNode = {
        id: `region-${quNo}`,
        label: `第${quNo}区`,
        type: 'region',
        quNo: quNo,
        children: []
    };

    // 根据区号决定列数
    const maxColNo = quNo === 2 ? 13 : 18;

    // 生成档案柜
    for (let colNo = 1; colNo <= maxColNo; colNo++) {
        const cabinetNode = {
            id: `cabinet-${quNo}-${colNo}`,
            label: `${String(quNo).padStart(2, '0')}-${String(colNo).padStart(2, '0')}`,
            type: 'cabinet',
            quNo: quNo,
            colNo: colNo,
            children: []
        };

        // 根据区号决定面的结构
        if (quNo === 2) {
            // 第二区：只有A面，分为1-10层和11-20层
            cabinetNode.children = [
                {
                    id: `side-${quNo}-${colNo}-A1`,
                    label: 'A面(1-10层)',
                    type: 'side',
                    quNo: quNo,
                    colNo: colNo,
                    side: 'A1',
                    children: generateCompartmentNodes(quNo, colNo, 'A1')
                },
                {
                    id: `side-${quNo}-${colNo}-A2`,
                    label: 'A面(11-20层)',
                    type: 'side',
                    quNo: quNo,
                    colNo: colNo,
                    side: 'A2',
                    children: generateCompartmentNodes(quNo, colNo, 'A2')
                }
            ];
        } else {
            // 其他区：A面和B面
            cabinetNode.children = [
                {
                    id: `side-${quNo}-${colNo}-A`,
                    label: '左(A)',
                    type: 'side',
                    quNo: quNo,
                    colNo: colNo,
                    side: 'A',
                    children: generateCompartmentNodes(quNo, colNo, 'A')
                },
                {
                    id: `side-${quNo}-${colNo}-B`,
                    label: '右(B)',
                    type: 'side',
                    quNo: quNo,
                    colNo: colNo,
                    side: 'B',
                    children: generateCompartmentNodes(quNo, colNo, 'B')
                }
            ];
        }

        regionNode.children.push(cabinetNode);
    }

    return regionNode;
}

// 优化的树形数据更新
function updateTreeData() {
    const tree = [];
    quNoList.value.forEach(quNo => {
        if (allTreeData.has(quNo)) {
            tree.push(allTreeData.get(quNo));
        }
    });
    cachedTreeData.value = tree;
}

// 生成储物格节点 - 优化版本
function generateCompartmentNodes(quNo, colNo, side) {
    const compartments = [];

    if (quNo === 2) {
        // 第二区：4节20层
        const startDiv = side === 'A1' ? 1 : 11;
        const endDiv = side === 'A1' ? 10 : 20;

        for (let leNo = 1; leNo <= 4; leNo++) {
            for (let divNo = startDiv; divNo <= endDiv; divNo++) {
                const comp = findCompartment(quNo, colNo, 'A', leNo, divNo);

                compartments.push({
                    id: comp ? `comp-${comp.id}` : `comp-${quNo}-${colNo}-${side}-${leNo}-${divNo}`,
                    label: `${leNo}-${divNo}`,
                    type: 'compartment',
                    quNo: quNo,
                    colNo: colNo,
                    side: side,
                    leNo: leNo,
                    divNo: divNo,
                    compartmentId: comp ? comp.id : null,
                    compartment: comp
                });
            }
        }
    } else {
        // 其他区：5节6层
        for (let leNo = 1; leNo <= 5; leNo++) {
            for (let divNo = 1; divNo <= 6; divNo++) {
                const comp = findCompartment(quNo, colNo, side, leNo, divNo);

                compartments.push({
                    id: comp ? `comp-${comp.id}` : `comp-${quNo}-${colNo}-${side}-${leNo}-${divNo}`,
                    label: `${leNo}-${divNo}`,
                    type: 'compartment',
                    quNo: quNo,
                    colNo: colNo,
                    side: side,
                    leNo: leNo,
                    divNo: divNo,
                    compartmentId: comp ? comp.id : null,
                    compartment: comp
                });
            }
        }
    }

    return compartments;
}

// 处理树节点点击
function handleNodeClick(data) {
    if (data.type === 'compartment' && data.compartment) {
        highlightedCompartment.value = data.compartment.id;
        nextTick(() => {
            tryScrollToElement(data.compartment.id);
        });
    }
}

// 获取树节点样式类 - 缓存结果
const nodeClassCache = new Map();
function getNodeClass(data) {
    const cacheKey = `${data.type}-${data.compartment?.id || data.id}`;

    if (nodeClassCache.has(cacheKey)) {
        return nodeClassCache.get(cacheKey);
    }

    const classes = [`tree-node-${data.type}`];

    if (data.type === 'compartment' && data.compartment) {
        const cellClass = getCellClass(data.compartment);
        if (cellClass) {
            classes.push(cellClass.replace('cell-', 'tree-'));
        }
    }

    nodeClassCache.set(cacheKey, classes);
    return classes;
}

// 获取左侧标题
function getLeftSectionTitle(quNo) {
    return quNo === 2 ? 'A面（1-10层）' : '左侧(A面)';
}

// 获取右侧标题
function getRightSectionTitle(quNo) {
    return quNo === 2 ? 'A面（11-20层）' : '右侧(B面)';
}

// 获取网格容器样式类
function getGridContainerClass(quNo, position) {
    if (quNo === 2) {
        return 'grid-container-area2';
    }
    return 'grid-container';
}

// 获取实际的zyNo
function getZyNoForSide(quNo, displaySide) {
    if (quNo === 2) {
        return 'A'; // 第二区统一使用A面
    }
    return displaySide === 'left' ? 'A' : 'B';
}

const data = reactive({
    form: {
        repositoryId: 10000,
        leNo: 5,
        divNo: 6,
    },
    queryParams: {
        pageNum: 1,
        pageSize: 9999,
        bianhao: null,
        name: null,
        repositoryId: 10000,
        quNo: null,
        colNo: null,
        zyNo: null,
    },
    archiveParams: {
        danghao: null,
        name: null,
    },
    iams_shiti_location_quhao: [
        {
            label: '第1区',
            value: 1
        },
        {
            label: '第2区',
            value: 2
        },
        {
            label: '第3区',
            value: 3
        },
        {
            label: '第4区',
            value: 4
        },
    ],
    iams_shiti_location_liehao: [
        {
            label: '第一列',
            value: 1
        },
        {
            label: '第二列',
            value: 2
        },
        {
            label: '第三列',
            value: 3
        },
        {
            label: '第四列',
            value: 4
        },
        {
            label: '第五列',
            value: 5
        },
        {
            label: '第六列',
            value: 6
        },
        {
            label: '第七列',
            value: 7
        },
        {
            label: '第八列',
            value: 8
        },
        {
            label: '第九列',
            value: 9
        },
        {
            label: '第十列',
            value: 10
        },
        {
            label: '第十一列',
            value: 11
        },
        {
            label: '第十二列',
            value: 12
        },
        {
            label: '第十三列',
            value: 13
        },
        {
            label: '第十四列',
            value: 14
        },
        {
            label: '第十五列',
            value: 15
        },
        {
            label: '第十六列',
            value: 16
        },
        {
            label: '第十七列',
            value: 17
        },
        {
            label: '第十八列',
            value: 18
        },
    ],
    iams_shiti_location_zy: [
        {
            label: '左侧(A面)',
            value: 'A'
        },
        {
            label: '右侧(B面)',
            value: 'B'
        }
    ],
});

const { archiveParams, queryParams, form, rules, iams_shiti_location_zy, iams_shiti_location_quhao, iams_shiti_location_liehao } = toRefs(data);

/** 查询档案柜管理列表 */
async function getList() {
    loading.value = true;

    try {
        const [cabinetResponse, compartmentResponse] = await Promise.all([
            listCabinet(queryParams.value),
            listCompartment()
        ]);

        cabinetList.value = cabinetResponse.rows;
        total.value = cabinetResponse.total;
        compartmentList.value = compartmentResponse.data;

        // 构建索引
        buildIndexes();

        // 预生成所有树形数据
        preGenerateAllTreeData();

        // 处理查询
        handleQuery();

    } catch (error) {
        console.error('获取数据失败:', error);
    } finally {
        loading.value = false;
    }
}


// 取消按钮
function cancel() {
    open.value = false;
    reset();
}

// 表单重置
function reset() {
    form.value = {
        id: null,
        bianhao: null,
        name: null,
        repositoryId: null,
        quNo: null,
        colNo: null,
        zyNo: null,
        leNo: null,
        divNo: null,
        capacity: null,
        size: null,
        temperature: null,
        moisture: null,
        oxygen: null,
        waterlogging: null,
        createTime: null,
        createBy: null,
        updateTime: null,
        updateBy: null,
        remark: null
    };
    proxy.resetForm("cabinetRef");
}

// 优化的handleQuery函数
function handleQuery() {
    if (quNoList.value == null || quNoList.value.length === 0) {
        cabinetCard.value = [];
        cachedTreeData.value = [];
        return;
    }

    // 使用requestAnimationFrame优化DOM更新
    requestAnimationFrame(() => {
        updateTreeData();
        updateCabinetCards();
    });
}

function updateCabinetCards() {
    const newData = [];

    quNoList.value.forEach(quNo => {
        const cached = cachedCards[quNo];
        if (cached) {
            newData.push(...cached);
        } else {
            // 根据区号决定列数
            const maxColNo = quNo === 2 ? 13 : 18;
            for (let colNo = 1; colNo <= maxColNo; colNo++) {
                newData.push({
                    id: getMinIdByQuNoAndColNo(quNo, colNo),
                    quNo: quNo,
                    colNo: colNo,
                    // capacity: quNo === 2 ? 1920 : 900, // 第二区：4节*20层*24 = 1920
                    size: getCachedSize(quNo, colNo)
                });
            }
        }
    });

    cabinetCard.value = newData;
}

function formatQuCol(quNo, colNo) {
    return `${String(quNo ?? '').padStart(2, '0')}-${String(colNo ?? '').padStart(2, '0')}`;
}

function getCachedSize(quNo, colNo) {
    const cacheKey = `${quNo}-${colNo}`;
    if (sizeCache[cacheKey]) {
        return sizeCache[cacheKey];
    }

    const matched = cabinetList.value.filter(cab => cab.quNo == quNo && cab.colNo == colNo);
    let size = 0
    if (matched.length > 0) {
        size = matched.reduce((sum, cab) => sum + (cab.size || 0), 0);
    }

    sizeCache[cacheKey] = size;
    return size;
}

// 优化的获取储物格函数
function getCompartmentsByCabinetAndSide(quNo, colNo, displaySide) {
    if (quNo === 2) {
        // 第二区：只有A面的储物格
        const key = `${quNo}-${colNo}-A`;
        const cabinet = cabinetMap.get(key);

        if (!cabinet) return [];

        const allCompartments = compartmentList.value.filter(comp => comp.cabinetId === cabinet.id);

        // 根据displaySide过滤出相应的层
        if (displaySide === 'left') {
            // 左侧显示1-10层
            return allCompartments.filter(comp => comp.divNo >= 1 && comp.divNo <= 10);
        } else {
            // 右侧显示11-20层
            return allCompartments.filter(comp => comp.divNo >= 11 && comp.divNo <= 20);
        }
    } else {
        // 其他区：A面和B面
        const zyNo = displaySide === 'left' ? 'A' : 'B';
        const key = `${quNo}-${colNo}-${zyNo}`;
        const cabinet = cabinetMap.get(key);

        if (!cabinet) return [];

        return compartmentList.value.filter(comp => comp.cabinetId === cabinet.id);
    }
}

function getMinIdByQuNoAndColNo(quNo, colNo) {
    const matched = cabinetList.value.filter(cab => {
        return cab.quNo === quNo && cab.colNo === colNo;
    });

    if (matched.length === 0) return null;
    return Math.min(...matched.map(cab => cab.id));
}

function getCellClass(comp) {
    if (comp.size === 0) return 'cell-green';
    if (comp.size >= 1) return 'cell-yellow';
    // if (comp.size >= 15) return 'cell-red';
    return '';
}

function preLoadCards(quNo) {
    if (cachedCards[quNo]) return cachedCards[quNo];

    const data = [];
    const maxColNo = quNo === 2 ? 13 : 18;
    for (let colNo = 1; colNo <= maxColNo; colNo++) {
        data.push({
            id: getMinIdByQuNoAndColNo(quNo, colNo),
            quNo: quNo,
            colNo: colNo,
            // capacity: quNo === 2 ? 1920 : 900,
            size: getCachedSize(quNo, colNo)
        });
    }

    cachedCards[quNo] = data;
    return data;
}

function startPreLoadingTask() {
    // 初次加载
    [1, 2, 3, 4].forEach(quNo => preLoadCards(quNo));

    // 每隔 5 分钟刷新一次缓存
    setInterval(() => {
        console.log('正在刷新缓存...');
        [1, 2, 3, 4].forEach(quNo => preLoadCards(quNo));
        nodeClassCache.clear(); // 清理样式缓存
    }, 5 * 60 * 1000);
}

// 档案数据加载优化 - 防抖
const loadArchiveDataDebounced = debounce(loadArchiveDataActual, 100);

function debounce(func, wait) {
    let timeout;
    return function executedFunction(...args) {
        const later = () => {
            clearTimeout(timeout);
            func(...args);
        };
        clearTimeout(timeout);
        timeout = setTimeout(later, wait);
    };
}

async function loadArchiveData(quNo, colNo, zyNo, comp) {
    loadArchiveDataDebounced(quNo, colNo, zyNo, comp);
}

async function loadArchiveDataActual(quNo, colNo, zyNo, comp) {
    const shitiLocation = `${quNo}-${colNo}-${zyNo}`;
    const exactLocation = `${comp.leNo}-${comp.divNo}`;

    try {
        const response = await listArchiveByLocation(shitiLocation, exactLocation);
        archiveMap.value = {
            ...archiveMap.value,
            [comp.id]: response.rows || []
        };
    } catch (error) {
        archiveMap.value = {
            ...archiveMap.value,
            [comp.id]: []
        };
        console.error('获取档案失败:', error);
    }
}

function handleMouseOver(comp) {
    if (!archiveMap.value[comp.id]) {
        loadArchiveData(comp);
    }
}

/** 重置按钮操作 */
function resetQuery() {
    proxy.resetForm("queryRef");
    clearAllHighlights();
}

async function queryArchive() {
    if (!archiveParams.value.danghao && !archiveParams.value.name) {
        proxy.$modal.warn("请输入档号或名称进行搜索");
        return;
    }

    try {
        const response = await listArchive(archiveParams.value);
        const archives = response.rows || [];

        if (archives.length === 0) {
            proxy.$modal.msgWarning("未找到相关档案");
            return;
        }

        const archive = archives[0];
        const [leNo, divNo] = archive.exactLocation.split('-');
        const [quNo, colNo, zyNo] = archive.shitiLocation.split('-');

        if (![leNo, divNo, quNo, colNo, zyNo].every(Boolean)) {
            proxy.$modal.msgError("档案位置信息不完整");
            return;
        }

        const compartment = compartmentList.value.find(comp => {
            const cabinet = cabinetList.value.find(cab => cab.id === comp.cabinetId);
            return (
                comp.leNo == leNo &&
                comp.divNo == divNo &&
                cabinet?.quNo == quNo &&
                cabinet?.colNo == colNo &&
                cabinet?.zyNo == zyNo
            );
        });

        if (!compartment) {
            proxy.$modal.msgError("未找到该档案所在储物格");
            return;
        }

        highlightedCompartment.value = compartment.id;

        nextTick(() => {
            tryScrollToElement(compartment.id);
        });

    } catch (error) {
        proxy.$modal.msgError("查询失败，请稍后再试");
        console.error('查询档案出错:', error);
    }
}

function clearAllHighlights() {
    Object.values(gridRefs.value).forEach(el => {
        if (el && el.classList.contains('highlighted')) {
            el.classList.remove('highlighted');
        }
    });
}

function tryScrollToElement(id) {
    let tryCount = 0;
    const maxTry = 10;

    function attempt() {
        const element = gridRefs.value[id];
        if (element) {
            clearAllHighlights();
            element.scrollIntoView({ behavior: 'smooth', block: 'center' });
            element.classList.add('highlighted');
        } else if (tryCount < maxTry) {
            tryCount++;
            setTimeout(attempt, 300);
        } else {
            proxy.$modal.msgError("未找到目标格子，请手动查找");
        }
    }

    attempt();
}

function setGridRef(el, id) {
    if (el) {
        const realEl = el.$el ? el.$el : el;
        gridRefs.value[id] = realEl;
    } else {
        delete gridRefs.value[id];
    }
}

function isHighlighted(id) {
    return highlightedCompartment.value === id;
}

/** 导出按钮操作 */
function handleExport() {
    proxy.download('manage/cabinet/export', {
        ...queryParams.value
    }, `cabinet_${new Date().getTime()}.xlsx`)
}

onMounted(async () => {
    // 确保 route.query.id 存在并且是数字
    if (route.query.id) {
        queryParams.value.repositoryId = parseInt(route.query.id, 10);
    }
    quNoList.value = [1];

    await getList();
    startPreLoadingTask();
});
</script>

<style scoped>
/* 新增：使用 transform 实现更流畅的旋转动画 */
@keyframes rotating {
    from {
        transform: rotate(0deg);
    }
    to {
        transform: rotate(360deg);
    }
}

/* 覆盖 Element Plus 默认的 spinner 动画 */
:deep(.el-loading-spinner .el-icon-loading) {
    animation: rotating 1s linear infinite;
    font-size: 24px;
}

:deep(.el-loading-text) {
    color: #409EFF;
    margin-top: 3px;
}

/* 主要内容区域布局 */
.main-content {
    display: flex;
    gap: 20px;
    min-height: 600px;
    position: relative;
}

/* 左侧目录 (20%) */
.left-sidebar {
    width: 20%;
    position: absolute;
    top: 50px;
    left: 10px;
    height: calc(100vh - 40px);
    z-index: 1000;
}

.directory-container {
    background: #fff;
    border: 1px solid #e4e7ed;
    border-radius: 8px;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
    overflow: hidden;
}

.directory-header {
    background: linear-gradient(135deg, #409EFF, #79bbff);
    color: white;
    padding: 15px;
    text-align: center;
}

.directory-header h3 {
    margin: 0;
    font-size: 16px;
    font-weight: 600;
}

.directory-content {
    padding: 10px;
    height: calc(100vh - 400px);
    overflow-y: auto;
}

.directory-tree {
    background: transparent;
}

.directory-tree .el-tree-node__content {
    padding: 4px 0;
    height: auto;
}

.tree-node-label {
    font-size: 13px;
    padding: 2px 6px;
    border-radius: 3px;
    display: inline-block;
    cursor: pointer;
    transition: all 0.2s ease;
}

.tree-node-region {
    font-weight: bold;
    color: #409EFF;
}

.tree-node-cabinet {
    font-weight: 500;
    color: #606266;
}

.tree-node-side {
    color: #909399;
}

.tree-node-compartment {
    font-size: 12px;
    min-width: 35px;
    text-align: center;
}

.tree-node-compartment:hover {
    background-color: #f5f7fa;
    transform: scale(1.05);
}

/* 储物格状态颜色 */
.tree-green {
    background-color: #eaffea;
    color: #67c23a;
}

.tree-yellow {
    background-color: #fffbe5;
    color: #e6a23c;
}

.tree-red {
    background-color: #ffeaea;
    color: #f56c6c;
}

/* 右侧内容区域 (80%) */
.right-content {
    width: 80%;
    flex: 1;
    margin-left: calc(20% + 40px);
}

/* 原有样式保持不变 */
.card-header {
    padding: 0;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    text-align: center;
}

.card-header-top {
    font-size: 18px;
    font-weight: bold;
    color: #333;
}

.card-header-bottom {
    font-size: 12px;
    color: #666;
    margin-top: 8px;
}

.card-body {
    display: flex;
    justify-content: space-between;
    align-items: center;
    min-height: 120px;
}

.card-left,
.card-right {
    flex: 1;
    font-size: 13px;
    color: #555;
}

.card-left p,
.card-right p {
    margin: 4px 0;
}

.divider {
    width: 1px;
    height: 300px;
    background-color: #000000;
    margin: 0 10px;
}

.el-card {
    box-sizing: border-box;
    width: 100%;
}

.el-checkbox+.el-checkbox {
    margin-left: 10px;
}

/* 通用网格容器 */
.grid-container {
    display: grid;
    grid-template-rows: repeat(6, 1fr);
    grid-template-columns: repeat(5, 1fr);
    grid-auto-flow: column;
    grid-gap: 8px;
    padding: 8px;
    width: 100%;
}

/* 第二区网格容器 */
.grid-container-area2 {
    display: grid;
    grid-template-rows: repeat(10, 1fr);
    grid-template-columns: repeat(4, 1fr);
    grid-auto-flow: column;
    grid-gap: 6px;
    padding: 8px;
    width: 100%;
}

.grid-cell {
    border: 1px solid #dcdfe6;
    border-radius: 4px;
    font-size: 12px;
    text-align: center;
    line-height: 1.2;
    padding: 4px 0;
    color: #333;
    will-change: transform;
}

.grid-empty {
    text-align: center;
    color: #999;
    font-size: 12px;
    padding: 10px;
}

.cell-green {
    background-color: #eaffea !important;
}

.cell-yellow {
    background-color: #fffbe5 !important;
}

.cell-red {
    background-color: #ffeaea !important;
}

.section-header {
    font-size: 14px;
    font-weight: bold;
    text-align: center;
    margin-bottom: 8px;
    color: #333;
}

.card-left .section-header {
    color: #409EFF;
}

.card-right .section-header {
    color: #F56C6C;
}

.tooltip-content {
    padding: 8px;
    background-color: #fff;
    border: 1px solid #e4e7ed;
    border-radius: 4px;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
    font-size: 13px;
    color: #333;
    width: 500px;
    max-width: 600px;
    max-height: 600px;
    overflow-y: auto;
}

.tooltip-content p {
    margin: 4px 0;
}

.tooltip-table table {
    width: 100%;
    border-collapse: collapse;
    margin-top: 8px;
}

.tooltip-table th,
.tooltip-table td {
    border: 1px solid #eee;
    padding: 4px;
    text-align: left;
    font-size: 12px;
}

.tooltip-table th {
    background-color: #f5f7fa;
    width: 60px;
}

.grid-cell.highlighted {
    border: 2px solid #F56C6C !important;
    background-color: #ffeaea !important;
    transform: scale(1.05);
    transition: all 0.3s ease;
}

.tooltip-table a {
    color: #409EFF;
    text-decoration: underline;
}

.tooltip-table a:hover {
    color: #337ecc;
}

/* 目录树滚动条样式 */
.directory-content::-webkit-scrollbar {
    width: 6px;
}

.directory-content::-webkit-scrollbar-track {
    background: #f1f1f1;
    border-radius: 3px;
}

.directory-content::-webkit-scrollbar-thumb {
    background: #c1c1c1;
    border-radius: 3px;
}

.directory-content::-webkit-scrollbar-thumb:hover {
    background: #a8a8a8;
}

/* 响应式调整 */
@media (max-width: 1200px) {
    .main-content {
        flex-direction: column;
    }

    .left-sidebar {
        position: relative;
        width: 100%;
        height: 400px;
        margin-bottom: 20px;
        top: auto;
        left: auto;
    }

    .right-content {
        width: 100%;
        margin-left: 0;
    }

    .directory-content {
        height: 350px;
    }
}

/* 性能优化样式 */
.el-tree-node {
    contain: layout style paint;
}

.grid-container {
    contain: layout style paint;
}

.grid-container-area2 {
    contain: layout style paint;
}
</style>