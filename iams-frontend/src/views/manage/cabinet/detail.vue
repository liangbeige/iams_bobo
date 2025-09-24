<template>
    <div class="app-container">
        <div class="cabinet-info">档案柜信息:
            编号：{{ cabinet.bianhao }}，名称：{{ cabinet.name }}， 位置：<span>{{ cabinet.repositoryId }}号库房第{{ cabinet.quNo
            }}区第{{ cabinet.colNo }}列</span>，
            总计容量：{{ cabinet.capacity }}， 已使用：{{ cabinet.size }}, 剩余：{{ cabinet.capacity - cabinet.size }}，
            <span>选中节：{{ selected_section }}，选中层：{{ selected_layer }}</span>
        </div>
        <el-row :gutter="12">
            <el-col :span="16.5">
                <div>
                    <el-table :data="cabinetData" border style="width: 100%">
                        <el-table-column v-for="(section, sectionIndex) in cabinet.leNo" :key="sectionIndex"
                            :label="'节 ' + (sectionIndex + 1)" width="235">
                            <template #default="scope">
                                <div class="cell" @dblclick="handleCellClick(sectionIndex + 1, scope.$index + 1)">
                                    <!-- 新增：节/层信息容器 -->
                                    <div class="section-layer-info">
                                        {{ (sectionIndex + 1) + ' 节 ' + (scope.$index + 1) + ' 层 ' }}
                                    </div>

                                    <div class="cell-upper">
                                        <div class="cell-content">
                                            <!-- 注释掉蓝色小方块 -->
                                            <!-- <draggable :list="getArchivesInSection(scope.$index + 1, sectionIndex + 1)"
                                                :item-key="archive => archive.id" @start="onDragStart" @end="onDragEnd"
                                                :data-section-index="sectionIndex + 1"
                                                :data-layer-index="scope.$index + 1" group="archives">
                                                <template #item="{ element }">
                                                    <div class="rectangle">
                                                        <span class="archive-id">{{ element.id }}</span>
                                                    </div>
                                                </template>
</draggable> -->

                                            <!-- 新增：显示档案数量 -->
                                            <div class="archive-count">
                                                档案数：{{ getArchivesInSection(scope.$index + 1, sectionIndex + 1).length
                                                }}
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </template>
                        </el-table-column>
                    </el-table>
                </div>
            </el-col>
            <!-- <el-col :span="7.5">
                <el-card>
                    <template #header>
                        <h2 class="card-title"><span
                                class="title-arrow">&gt;&gt;&gt;&gt;&gt;&gt;&gt;&gt;&gt;&gt;</span><span
                                class="title-text">档案柜开关次数统计</span><span
                                class="title-arrow">&lt;&lt;&lt;&lt;&lt;&lt;&lt;&lt;&lt;&lt;</span></h2>
                    </template>
                    <div class="card-box" id="open-close-chart"></div>
                </el-card>
                <el-card>
                    <template #header>
                        <h2 class="card-title"><span class="title-arrow">&gt;&gt;&gt;&gt;&gt;&gt;&gt;&gt;</span><span
                                class="title-text">档案柜实时温度仪表盘</span><span
                                class="title-arrow">&lt;&lt;&lt;&lt;&lt;&lt;&lt;&lt;</span></h2>
                    </template>
                    <div class="card-box" id="env-wendu-chart"></div>
                </el-card>
            </el-col> -->
        </el-row>
        <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px" :draggable="true">
            <ul class="archive-list">
                <li class="archive-header">
                    <div style="display: flex; align-items: center; gap: 12px;">
                        <div class="checkbox-wrapper">
                            <el-checkbox v-model="selectAllInDialog" :indeterminate="isIndeterminate" />
                            <span style="margin-left: 6px;">全选</span>
                        </div>
                        <span>档案信息</span>
                    </div>
                    <span>操作</span>
                </li>
                <li class="archive-item" v-for="(item, index) in archiveList" :key="index">
                    <div class="archive-info">
                        <div class="checkbox-wrapper">
                            <el-checkbox v-model="item.selected" />
                        </div>
                        <div class="archive-detail">
                            <div class="archive-label">档号：{{ item.danghao }}</div>
                            <div class="archive-label">名称：{{ item.name }}</div>
                        </div>
                    </div>
                    <el-button type="primary" size="small" @click="handleDetail(item.id)">查看</el-button>
                </li>
            </ul>

            <template #footer>
                <div class="dialog-footer">
                    <el-button type="primary" @click="getUpArchives">选择上架</el-button>
                    <el-button type="danger" @click="handleSelectedUnArchive">下架</el-button>
                </div>
            </template>
        </el-dialog>

        <el-dialog v-model="upArchiveDialogVisible" title="未上架的档案列表" width="600px" :draggable="true">
            <ul class="archive-list">
                <li class="archive-header">
                    <div style="display: flex; align-items: center; gap: 12px;">
                        <div class="checkbox-wrapper">
                            <el-checkbox v-model="selectAll" :indeterminate="isIndeterminate1" />
                            <span style="margin-left: 6px;">全选</span>
                        </div>
                        <span>档案信息</span>
                    </div>
                    <span>操作</span>
                </li>
                <li class="archive-item" v-for="(item, index) in upArchiveList" :key="index">
                    <div class="archive-info">
                        <div class="checkbox-wrapper">
                            <el-checkbox v-model="item.selected" />
                        </div>
                        <div class="archive-detail">
                            <div class="archive-label">档号：{{ item.danghao }}</div>
                            <div class="archive-label">名称：{{ item.name }}</div>
                        </div>
                    </div>
                    <el-button type="primary" size="small" @click="handleDetail(item.id)">查看</el-button>
                </li>
            </ul>

            <template #footer>
                <div class="dialog-footer">
                    <el-button @click="upArchiveDialogVisible = false">取消</el-button>
                    <el-button type="primary" @click="handleUpArchive">上架</el-button>
                </div>
            </template>
        </el-dialog>
    </div>
</template>

<script setup lang="js">
import { onMounted, ref, computed } from 'vue';
import { useRoute } from 'vue-router';
import * as echarts from 'echarts';
import { getCabinet, updateCabinet } from '@/api/manage/cabinet';
import { listArchive, listArchiveByLocation, moveArchive, updateArchivesLocation, getUpArchiveList } from "@/api/manage/archive";
import draggable from 'vuedraggable';
import { ElMessageBox } from 'element-plus';
import { getUserProfile } from "@/api/system/user";

const { proxy } = getCurrentInstance();
const route = useRoute();

const cabinet = ref({
    quNo: null,
    colNo: null,
    zyNo: null,
    leNo: null,
    divNo: null
});
const selected_section = ref(0); // 节
const selected_layer = ref(0); // 层
const dialogVisible = ref(false)
const upArchiveDialogVisible = ref(false);
const dialogTitle = ref('');
const queryParams = ref(
    {
        pageNum: 1,
        pageSize: 10,
    }
);
const total = ref(0);
const archiveList = ref([]);  //根据“1号库房第2排第2列档案柜第3节第3层”得到的档案列表，即某一节层的档案列表
const archive_List = ref([]);  //根据“1号库房第2排第2列档案柜”得到的档案列表，即该档案柜的所有档案列表，用于在页面中显示档案
const upArchiveList = ref([]);  // 需要上架的档案列表
const cabinetData = ref([]); // 只需要一行数据来显示所有节和层

const shitiLocation = ref('');
const exactLocation = ref('');

const drag = ref(false);
const draggedArchive = ref(null);
const state = reactive({
    user: {},
    roleGroup: {},
    postGroup: {}
});
function getCabinetDetail() {
    const _id = route.query.id;
    getCabinet(_id).then(response => {
        cabinet.value = response.data;
        // let loc = cabinet.value.repositoryId + '号库房第' + cabinet.value.quNo + '区第' + cabinet.value.colNo + '列'
        let loc = cabinet.value.quNo + '-' + cabinet.value.colNo + '-' + cabinet.value.zyNo;
        getArchiveList(loc);
        updateCabinetData(); // 更新档案柜信息
    });
}
// 在这里添加点击单元格时的逻辑
function handleCellClick(section, layer) {
    selected_section.value = section;
    selected_layer.value = layer;
    // 重置 archiveList 中的 selected 属性
    archiveList.value.forEach(item => {
        item.selected = false;
    });
    // console.log('选中节：', selected_section.value, '选中层：', selected_layer.value);
    dialogVisible.value = true;
    dialogTitle.value = '第' + selected_section.value + '节 第' + selected_layer.value + '层' + ' 的档案如下';
    shitiLocation.value = cabinet.value.quNo + '-' + cabinet.value.colNo + '-' + cabinet.value.zyNo;
    exactLocation.value = selected_layer.value + '-' + selected_section.value;
    // location.value = cabinet.value.repositoryId + '号库房第' + cabinet.value.pai + '排第' + cabinet.value.lie + '列档案柜' + '第' + selected_section.value + '节第' + selected_layer.value + '层';
    // console.log('location:', location.value);
    getArchiveListByLocation(shitiLocation.value, exactLocation.value);
}

// 根据location查询档案列表
function getArchiveListByLocation(shitiLocation, exactLocation) {
    listArchiveByLocation(shitiLocation, exactLocation).then(response => {
        archiveList.value = response.rows;
        total.value = response.total;
        // console.log('档案列表：', archiveList.value);
    });
}

/** 查询档案列表 */
function getArchiveList(shitiLocation) {
    listArchiveByLocation(shitiLocation, null).then(response => {
        archive_List.value = response.rows;
        // console.log('当前档案柜总的档案列表：', archive_List.value);
        updateCabinetData();
        updateCabinet(cabinet.value)
    });
}

function updateCabinetData() {
    const sectionCount = cabinet.value.leNo;
    const layerCount = cabinet.value.divNo;
    const data = [];

    let totalArchives = 0; // 用于统计所有档案的数量

    for (let layerIndex = 0; layerIndex < layerCount; layerIndex++) {
        const layerData = {};
        for (let sectionIndex = 0; sectionIndex < sectionCount; sectionIndex++) {
            const shitiLocation = `${cabinet.value.quNo}-${cabinet.value.colNo}-${cabinet.value.zyNo}`;
            const exactLocation = `${sectionIndex + 1}-${layerIndex + 1}`;
            const count = archive_List.value.filter(archive => (archive.shitiLocation === shitiLocation && archive.exactLocation === exactLocation)).length;
            layerData[`section${sectionIndex + 1}`] = count;
            totalArchives += count; // 累加档案数量
        }
        data.push(layerData);
    }

    cabinetData.value = data;
    cabinet.value.size = totalArchives; // 更新已使用容量
    // console.log('cabinet:', cabinet.value);
    // console.log('cabinetData:', cabinetData.value);
}

function getArchiveId(section, layer, index) {
    const shitiLocation = `${cabinet.value.quNo}-${cabinet.value.colNo}-${cabinet.value.zyNo}`;
    const exactLocation = `${section}-${layer}`;
    const archives = archive_List.value.filter(archive => (archive.shitiLocation === shitiLocation && archive.exactLocation === exactLocation));
    return archives[index - 1]?.id || ''; // 返回档案的id，如果没有则返回空字符串
}

function handleDetail(id) {
    proxy.$router.push({ path: "/manage/archive/arc-detail", query: { id: id } });
}

let openCloseChart = null;

const initOpenCloseChart = () => {
    let chartDom = document.getElementById('open-close-chart');
    if (chartDom) {
        openCloseChart = echarts.init(chartDom);
        let option = {
            legend: {},
            tooltip: {},
            dataset: {
                dimensions: ['日期', '打开', '关闭'],
                source: [
                    { 日期: '2025-01-10', '打开': 21, '关闭': 21 },
                    { 日期: '2025-01-11', '打开': 14, '关闭': 12 },
                    { 日期: '2025-01-12', '打开': 18, '关闭': 20 },
                    { 日期: '2025-01-13', '打开': 27, '关闭': 23 }
                ]
            },
            xAxis: { type: 'category' },
            yAxis: {},
            series: [{ type: 'bar' }, { type: 'bar' }]
        };
        openCloseChart.setOption(option);
    }
};

let envWenduChart = null;
const initEnvWenduChart = () => {
    let chartDom = document.getElementById('env-wendu-chart');
    if (chartDom) {
        envWenduChart = echarts.init(chartDom);
        let option = {
            series: [
                {
                    type: 'gauge',
                    center: ['50%', '60%'],
                    startAngle: 200,
                    endAngle: -20,
                    min: 0,
                    max: 60,
                    splitNumber: 12,
                    itemStyle: {
                        color: '#FFAB91'
                    },
                    progress: {
                        show: true,
                        width: 30
                    },
                    pointer: {
                        show: false
                    },
                    axisLine: {
                        lineStyle: {
                            width: 30
                        }
                    },
                    axisTick: {
                        distance: -45,
                        splitNumber: 5,
                        lineStyle: {
                            width: 2,
                            color: '#999'
                        }
                    },
                    splitLine: {
                        distance: -52,
                        length: 14,
                        lineStyle: {
                            width: 3,
                            color: '#999'
                        }
                    },
                    axisLabel: {
                        distance: -20,
                        color: '#999',
                        fontSize: 20
                    },
                    anchor: {
                        show: false
                    },
                    title: {
                        show: false
                    },
                    detail: {
                        valueAnimation: true,
                        width: '60%',
                        lineHeight: 40,
                        borderRadius: 8,
                        offsetCenter: [0, '-15%'],
                        fontSize: 60,
                        fontWeight: 'bolder',
                        formatter: '{value}°C',
                        color: 'inherit'
                    },
                    data: [
                        {
                            value: 20
                        }
                    ]
                },
                {
                    type: 'gauge',
                    center: ['50%', '60%'],
                    startAngle: 200,
                    endAngle: -20,
                    min: 0,
                    max: 60,
                    itemStyle: {
                        color: '#FD7347'
                    },
                    progress: {
                        show: true,
                        width: 8
                    },
                    pointer: {
                        show: false
                    },
                    axisLine: {
                        show: false
                    },
                    axisTick: {
                        show: false
                    },
                    splitLine: {
                        show: false
                    },
                    axisLabel: {
                        show: false
                    },
                    detail: {
                        show: false
                    },
                    data: [
                        {
                            value: 20
                        }
                    ]
                }
            ]
        };
        envWenduChart.setOption(option);
    }
}

function onDragStart(event) {
    const sectionIndex = event.from.getAttribute('data-section-index');
    const layerIndex = event.from.getAttribute('data-layer-index');
    const archiveIndex = event.oldIndex + 1;
    const shitiLocation = `${cabinet.value.quNo}-${cabinet.value.colNo}-${cabinet.value.zyNo}`;
    const exactLocation = `${sectionIndex + 1}-${layerIndex + 1}`;
    const archives = archive_List.value.filter(archive => (archive.shitiLocation === shitiLocation && archive.exactLocation === exactLocation));
    draggedArchive.value = archives[archiveIndex - 1];
    drag.value = true;
}

function onDragEnd(event) {
    if (!drag.value) return;
    drag.value = false;

    const targetSectionIndex = event.to.getAttribute('data-section-index');
    const targetLayerIndex = event.to.getAttribute('data-layer-index');
    const targetLocation1 = `${cabinet.value.quNo}-${cabinet.value.colNo}-${cabinet.value.zyNo}`;// 区、列、左右
    const targetLocation2 = `${targetSectionIndex + 1}-${targetLayerIndex + 1}`; // 节、层
    const targetLocation = targetLocation1 + '-' + targetLocation2;
    ElMessageBox.confirm(
        `确认将档案从 ${draggedArchive.value.shitiLocation + '-' + draggedArchive.value.exactLocation} 移动到 ${targetLocation1 + '-' + targetLocation2} 吗？`,
        '提示',
        {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning',
        }
    ).then(() => {
        console.log('档案id:', draggedArchive.value.id);
        console.log('移动后的位置:', targetLocation1 + '-' + targetLocation2);
        // 移动档案
        moveArchive(draggedArchive.value.id, targetLocation).then(response => {
            // console.log('档案移动成功:', response);
            proxy.$modal.msgSuccess("档案移动成功");
            getCabinetDetail(); // 刷新页面
        }).catch(error => {
            // console.error('档案移动失败:', error);
            proxy.$modal.msgError("档案移动失败");
        });
    }).catch(() => {
        // console.log('取消移动');
        proxy.$modal.msgError("取消移动");
        // 取消移动，恢复原位置
        event.item.remove();
        event.from.appendChild(event.item);
    });
}

// 处理选择下架
function handleSelectedUnArchive() {
    const selectedArchives = archiveList.value.filter(item => item.selected);
    if (selectedArchives.length === 0) {
        proxy.$modal.msgWarning("请选择要下架的档案");
        return;
    }

    selectedArchives.forEach(archive => {
        archive.shitiLocation = '';
        archive.exactLocation = '';
    });

    updateArchivesLocation(selectedArchives).then(response => {
        dialogVisible.value = false;
        proxy.$modal.msgSuccess("档案下架成功");
        getCabinetDetail(); // 刷新页面
    }).catch(error => {
        dialogVisible.value = false;
        proxy.$modal.msgError("档案下架失败");
        console.error('档案下架失败:', error);
    });
}

// 获取未上架的档案
function getUpArchives() {
    getUpArchiveList().then(response => {
        dialogVisible.value = false;
        upArchiveList.value = response.rows;
        upArchiveList.value.forEach(item => {
            item.selected = false;
        });
        upArchiveDialogVisible.value = true;
    }).catch(error => {
        proxy.$modal.msgError("获取未上架档案失败");
        console.error('获取未上架档案失败:', error);
    });
}

// 处理上架
function handleUpArchive() {
    const selectedArchives = upArchiveList.value.filter(item => item.selected);
    if (selectedArchives.length === 0) {
        proxy.$modal.msgWarning("请选择要上架的档案");
        return;
    }

    const targetLocation1 = `${cabinet.value.quNo}-${cabinet.value.colNo}-${cabinet.value.zyNo}`;// 区、列、左右
    const targetLocation2 = `${selected_layer.value}-${selected_section.value}`; // 节、层

    selectedArchives.forEach(archive => {
        archive.shitiLocation = targetLocation1;
        archive.exactLocation = targetLocation2;
    });

    updateArchivesLocation(selectedArchives).then(response => {
        upArchiveDialogVisible.value = false;
        proxy.$modal.msgSuccess("档案上架成功");
        getCabinetDetail(); // 刷新页面
    }).catch(error => {
        upArchiveDialogVisible.value = false;
        proxy.$modal.msgError("档案上架失败");
        console.error('档案上架失败:', error);
    });
}

// 全选状态（计算属性）
const selectAllInDialog = computed({
    get() {
        return archiveList.value.length > 0 &&
            archiveList.value.every(item => item.selected);
    },
    set(checked) {
        archiveList.value.forEach(item => {
            item.selected = checked;
        });
    }
});

// 半选状态
const isIndeterminate = computed(() => {
    const selectedCount = archiveList.value.filter(item => item.selected).length;
    return selectedCount > 0 && selectedCount < archiveList.value.length;
});

// 全选状态（计算属性）
const selectAll = computed({
    get() {
        return upArchiveList.value.length > 0 &&
            upArchiveList.value.every(item => item.selected);
    },
    set(checked) {
        upArchiveList.value.forEach(item => {
            item.selected = checked;
        });
    }
});

// 半选状态
const isIndeterminate1 = computed(() => {
    const selectedCount = upArchiveList.value.filter(item => item.selected).length;
    return selectedCount > 0 && selectedCount < upArchiveList.value.length;
});

// 定义 getArchivesInSection 方法
function getArchivesInSection(layerIndex, sectionIndex) {
    const shitiLocation = `${cabinet.value.quNo}-${cabinet.value.colNo}-${cabinet.value.zyNo}`;// 区、列、左右
    const exactLocation = `${layerIndex}-${sectionIndex}`; // 节、层
    return archive_List.value.filter(archive => archive.shitiLocation === shitiLocation && archive.exactLocation === exactLocation);
}

function getUser() {
    getUserProfile().then(response => {
        state.user = response.data;
        state.roleGroup = response.roleGroup;
        state.postGroup = response.postGroup;
    });
};

onMounted(() => {
    getCabinetDetail();

    nextTick(() => {
        initOpenCloseChart();
        initEnvWenduChart();
    });
    setInterval(() => {
        const random = +(25 + Math.random() * 3).toFixed(0);
        envWenduChart.setOption({
            series: [
                {
                    data: [
                        {
                            value: random
                        }
                    ]
                },
                {
                    data: [
                        {
                            value: random
                        }
                    ]
                }
            ]
        });
    }, 1000);
    getUser();
});

</script>

<style scoped>
.cabinet-info {
    margin-bottom: 10px;
    font-size: 18px;
    color: #000000;
    font-weight: 500;
    line-height: 28px;
}

/* 修改.cell样式 */
.cell {
    height: 100px;
    position: relative;
    border-bottom: 1px solid #ebeef5;
    border-right: 1px solid #ebeef5;
    padding: 5px;
    overflow: hidden;
}

/* 新增：节/层信息样式 */
.section-layer-info {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    font-size: 14px;
    color: #606266;
    z-index: 1;
}

.cell:last-child {
    border-bottom: none;
}

/* 修改.cell-upper样式 */
.cell-upper {
    position: absolute;
    bottom: 0;
    right: 0;
    padding: 5px;
    background-color: rgba(255, 255, 255, 0.7);
    /* 半透明背景，提高可读性 */
    border-top-left-radius: 4px;
    z-index: 2;
}

/* 修改.archive-count样式 */
.archive-count {
    font-size: 12px;
    color: #333;
    min-width: 60px;
    text-align: right;
}

.cell-lower {
    flex: 0 0 20px;
    /* 下层占据固定高度 */
    display: flex;
    align-items: center;
    justify-content: center;
    width: 100%;
    font-size: 14px;
    /* 调整字体大小 */
    color: #606266;
    /* 调整字体颜色 */
}

.cell-content {
    display: flex;
    flex-direction: row;
    /* 横着排列长方形 */
    align-items: center;
    justify-content: flex-start;
    /* 靠左对齐 */
    width: 100%;
    height: 100%;
    flex-wrap: wrap;
    /* 允许换行 */
}

.rectangle {
    width: 20px;
    /* 横着排列时，宽度较窄 */
    height: 50px;
    /* 横着排列时，高度较宽 */
    margin: 2px;
    /* 添加间距 */
    background-color: #409EFF;
    /* 档案颜色 */
    border-radius: 4px;
    position: relative;
    /* 用于绝对定位内部元素 */
    display: inline-flex;
    /* 改为 inline-flex */
    align-items: center;
    justify-content: center;

}

.archive-id {
    font-size: 12px;
    color: #fff;
    z-index: 1;
    pointer-events: none;
    /* 防止点击事件穿透到 archive-id */
}

.card-title {
    font-size: 20px;
    text-align: center;
}

.card-box {
    width: 100%;
    height: 250px;
    display: flex;
    flex-direction: row;
    justify-content: center;
    align-items: center;
}

.title-text {
    font-size: 20px;
    color: #1f1c1c;
    margin-left: 10px;
    margin-right: 10px;
}

.title-arrow {
    font-size: 20px;
    color: #d6d0d0;
}

.archive-list {
    width: 100%;
    list-style: none;
    margin: 0;
    padding: 0;
}

.archive-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 12px 16px;
    background-color: #f5f7fa;
    font-weight: bold;
    border-bottom: 1px solid #e4e7ed;
    border-radius: 4px 4px 0 0;
}

.archive-item {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 12px 16px;
    border-bottom: 1px solid #ebeef5;
    transition: background-color 0.2s ease;
}

.archive-item:last-child {
    border-bottom: none;
    border-radius: 0 0 4px 4px;
}

.archive-item:hover {
    background-color: #f9fafb;
}

.archive-info {
    display: flex;
    align-items: flex-start;
    gap: 12px;
    flex-grow: 1;
}

.checkbox-wrapper {
    display: flex;
    align-items: center;
    min-width: 80px;
}

.archive-detail {
    display: flex;
    flex-direction: column;
    flex-grow: 1;
}

.archive-label {
    font-size: 12px;
    color: #666;
}

.archive-title {
    font-size: 14px;
    color: #333;
}
</style>