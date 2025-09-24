<template>
    <div class="tsg box report" id="app">
        <!-- 动态背景 -->
        <div class="bg-animation">
            <div class="bg-particles"></div>
            <div class="bg-grid"></div>
            <div class="bg-glow"></div>
        </div>

        <!-- head -->
        <div class="tsg_head">
            <div class="tsg_title fc">
                <div class="d1">
                    <div class="time-container">
                        <i class="time-icon">🕒</i>
                        <p id="time">{{ currentTime }}</p>
                    </div>
                </div>
                <div class="d2">
                    <div class="title-glow">智慧档案管理系统大屏看板</div>
                    <div class="title-subtitle">Archive Management Dashboard</div>
                </div>
                <div class="d3"><!-- 占位元素，用于平衡布局 --></div>
            </div>
            <div class="tsg_hul fc">
                <div class="tsg_hli" v-for="(item, index) in statsItems" :key="index">
                    <div class="stat-card">
                        <div class="stat-icon">
                            <i :class="item.icon"></i>
                        </div>
                        <div class="stat-content">
                            <p class="stat-label">{{ item.label }}</p>
                            <p class="stat-value">
                            <div v-if="item.label !== '借阅总次数'">
                                <span class="number">{{ item.value }}</span>
                                <span class="unit">册</span>
                            </div>
                            <div v-else>
                                <span class="number">{{ item.value }}</span>
                                <span class="unit">次</span>
                            </div>
                            </p>
                        </div>
                        <div class="stat-glow"></div>
                    </div>
                </div>
            </div>
        </div>

        <div class="tsg_box fl">
            <!-- 左侧 -->
            <div class="tsg_box_left tsg_box_lis">
                <!-- 归档档案/档案利用分析 -->
                <div class="tsg_box_left_li tsg_table_6">
                    <div class="chart-header">
                        <div class="header-icon">📊</div>
                        <span class="header-title">归档档案/档案利用分析</span>
                        <div class="header-line"></div>
                    </div>
                    <div ref="echarts1" class="echarts_1"></div>
                </div>

                <!-- 在架数/逾期待归还分析 -->
                <div class="tsg_box_left_li tsg_table_6">
                    <div class="chart-header">
                        <div class="header-icon">📈</div>
                        <span class="header-title">在架档案数据分布柱状图</span>
                        <div class="header-line"></div>
                    </div>
                    <div ref="echarts2" class="echarts_1 echarts_tab"></div>
                </div>
            </div>

            <!-- 中间 -->
            <div class="tsg_box_center tsg_box_lis">
                <!-- 档案柜存储情况 -->
                <div class="tsg_box_left_li tsg_table_3">
                    <div class="chart-header">
                        <div class="header-icon">🗄️</div>
                        <span class="header-title">档案柜存储情况</span>
                        <div class="header-line"></div>
                    </div>

                    <!-- 总体统计卡片 -->
                    <div class="cabinet-overview">
                        <div class="overview-card">
                            <div class="overview-icon">🗄️</div>
                            <div class="overview-content">
                                <div class="overview-label">档案柜总数</div>
                                <div class="overview-number">{{ cabinetStats.totalCabinets }}</div>
                                <div class="overview-unit">个</div>
                            </div>
                        </div>
                        <div class="overview-card">
                            <div class="overview-icon">📚</div>
                            <div class="overview-content">
                                <div class="overview-label">存储总量</div>
                                <div class="overview-number">{{ cabinetStats.totalStored }}</div>
                                <div class="overview-unit">册</div>
                            </div>
                        </div>
                        <div class="overview-card">
                            <div class="overview-icon">📊</div>
                            <div class="overview-content">
                                <div class="overview-label">平均存储</div>
                                <div class="overview-number">{{ cabinetStats.averageStorage }}</div>
                                <div class="overview-unit">册/柜</div>
                            </div>
                        </div>
                        <div class="overview-card">
                            <div class="overview-icon">🏢</div>
                            <div class="overview-content">
                                <div class="overview-label">活跃区域</div>
                                <div class="overview-number">{{ cabinetStats.activeAreas }}</div>
                                <div class="overview-unit">个</div>
                            </div>
                        </div>
                    </div>

                    <!-- 各区存储情况柱状图 -->
                    <div class="chart-subtitle">各区存储情况对比</div>
                    <div ref="echarts3" class="echarts_2"></div>
                </div>
            </div>

            <!-- 右侧 -->
            <div class="tsg_box_right tsg_box_lis">
                <!-- 最近的借阅记录 -->
                <div class="tsg_box_left_li tsg_table_7">
                    <div class="chart-header">
                        <div class="header-icon">📋</div>
                        <span class="header-title">最近的借阅记录</span>
                        <div class="header-line"></div>
                    </div>
                    <div class="record-table">
                        <div class="record-header">
                            <div class="record-col">借阅人</div>
                            <div class="record-col">申请时间</div>
                            <div class="record-col">借阅状态</div>
                        </div>
                        <div class="record-body">
                            <div v-for="(record, index) in borrowRecords" :key="record.id || index" class="record-row"
                                :class="{ 'record-animate': index < 3 }">
                                <div class="record-cell">{{ record.userName || '-' }}</div>
                                <div class="record-cell">{{ record.startApplyTime || '-' }}</div>
                                <div class="record-cell">
                                    <span class="status-badge" :class="getStatusClass(record.status)">
                                        {{ record.status || '-' }}
                                    </span>
                                </div>
                            </div>
                            <div v-if="borrowRecords.length === 0" class="record-row">
                                <div class="record-empty">暂无借阅记录</div>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- 温湿度 -->
                <div class="tsg_box_left_li tsg_table_7">
                    <!--<div class="chart-header">-->
                    <!--    <div class="header-icon">🥧</div>-->
                    <!--    <span class="header-title">档案构成情况分析</span>-->
                    <!--    <div class="header-line"></div>-->
                    <!--</div>-->
                    <!--<div class="chart-container">-->
                    <!--    <div ref="echarts4" class="echarts_3"></div>-->
                    <!--</div>-->
                  <div class="chart-header">
                    <div class="header-icon">🥧</div>
                    <span class="header-title">区域温湿度展示</span>
                    <div class="header-line"></div>
                  </div>

                  <select
                      v-model="selectedArea"
                      @change="handleAreaChange(selectedArea)"
                      style="width: 30%; height: 10%; padding: 8px 12px; border: 1px solid #dcdfe6; border-radius: 4px;
                        color: white;
                      font-size: 12px;background-color: #0A2237"
                  >
                    <!-- 占位选项（模拟 placeholder 效果） -->
                    <option value="" disabled selected style="display: none;">选择区号</option>

                    <!-- 循环渲染区号选项 -->
                    <option
                        v-for="area in infoList"
                        :key="area.gdlNo"
                        :value="area.gdlNo"
                    >
                      {{ area.gdlNo }}: {{ area.gdlName }}
                    </option>
                  </select>

                  <div style="height: 90%;display: flex">
                      <!-- 第一部分：温度模块 -->
                        <div style="width: 80px">🌡️温度</div>
                        <tem :lasttem="currentData.Temp"></tem>
                       <!--第二部分：湿度模块 -->
                        <div style="width: 80px">💧湿度</div>
                        <div class="one_box" id="one_box" ref="one_box"></div>
                  </div>

                </div>
            </div>
        </div>
    </div>
</template>

<script setup name="ArchiveDashboard">
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue';
import * as echarts from 'echarts';
import "echarts-liquidfill";
import { getRecentBorrowData, getLatestStatistics } from '@/api/manage/statistics';
import { listRecord } from '../src/api/manage/borrowrecord';
import { listCabinet } from '@/api/manage/cabinet';
import tem from "@/views/iotSubSystem/system/iot_warehouse/webSocketTableDataTemperatureForBig.vue";
import axios from "axios";


//---------------------------
import {getInfo, listInfo} from "@/api/system/GDIpInfo.js";

import {ElMessage, ElMessageBox} from "element-plus";
const createLoading = (proxy, text) => proxy.$loading({ lock: true, text, spinner: "el-icon-loading" });
import {cancelSleepColumn, getColumnStatus,unlockColumn,resetColumn} from "@/api/system/move.js";
const delay = (ms) => new Promise(resolve => setTimeout(resolve, ms));
const showMsg = (msg, type = 'success', duration = 3000) => ElMessage({ type, message: msg, duration });
const showConfirm = (msg, title = '系统提示',type = 'warning') => ElMessageBox.confirm(msg, title, { confirmButtonText: '确定', cancelButtonText: '取消', type: type });

//---------------------------

// 配置变
const CONFIG = {
    BASE_API: '/prod-api',

};

// 响应式数据
const statistics = ref({
    totalBorrows: 0,
    totalReturns: 0,
    totalLoans: 0,
    totalLost: 0,
    archivedCount: 0,
    pendingCount: 0,
    electronicCount: 0,
    physicalCount: 0,
    cabinetCount: 0,
});

const currentData = ref({
      QUNO: '',
      Temp: 0,
      Hum: 0,
      PM2_5: 0,
      PM10: 0,
      TVOC: 0,
      CO2: 0,
      MJJZTLXName: '',
      IsBJ: 0,
      IsLock: 0,
      IsVent: 0,
      IsPower: 0,
      IsZDKJ: 0,
      ColumnStatus: ''
    })
const CO2Level = ref(0);
const PM10Level = ref(0);
const TVOCLevel = ref(0);
const PM2_5Level = ref(0);

const recent7DaysData = ref([]);
const borrowRecords = ref([]);
const cabinetData = ref([]);
const loading = ref(false);
const currentTime = ref('');

// 图表引用
const echarts1 = ref(null);
const echarts2 = ref(null);
const echarts3 = ref(null);
const echarts4 = ref(null);

// 定时器
let timeInterval = null;
let refreshInterval = null;
let refreshWSInterval = null;

// 统计项目配置
const statsItems = computed(() => [
    {
        label: '档案总数',
        value: statistics.value.archivedCount + statistics.value.pendingCount,
        icon: 'stat-icon-total'
    },
    {
        label: '归档档案',
        value: statistics.value.archivedCount,
        icon: 'stat-icon-new'
    },
    {
        label: '在架档案',
        value: statistics.value.cabinetCount,
        icon: 'stat-icon-shelf'
    },
    {
        label: '借阅总次数',
        value: statistics.value.totalBorrows,
        icon: 'stat-icon-borrow'
    },
    {
        label: '待归还数量',
        value: statistics.value.totalLoans,
        icon: 'stat-icon-recent'
    }
]);

// 计算在馆档案数量
const archiveOnShelf = computed(() => {
    return (statistics.value.archivedCount || 0) + (statistics.value.pendingCount || 0) -
        (statistics.value.totalLoans || 0) + (statistics.value.totalReturns || 0);
});

// 档案柜统计数据 - 去掉容量相关
const cabinetStats = computed(() => {
    if (!cabinetData.value || cabinetData.value.length === 0) {
        return {
            totalCabinets: 0,
            totalStored: 0,
            averageStorage: 0,
            activeAreas: 0
        };
    }

    // 按列统计（合并A、B面）
    const columnStats = {};
    const areaSet = new Set();

    cabinetData.value.forEach(cabinet => {
        const area = cabinet.name.substring(0, 2);
        const column = cabinet.name.substring(3, 5);
        const key = `${area}-${column}`;

        areaSet.add(area);

        if (!columnStats[key]) {
            columnStats[key] = {
                stored: 0,
                cabinetCount: 0
            };
        }

        columnStats[key].stored += Math.max(0, cabinet.size || 0);
        columnStats[key].cabinetCount += 1;
    });

    const totalCabinets = Object.keys(columnStats).length;
    const totalStored = statistics.value.cabinetCount;
    const averageStorage = totalCabinets > 0 ? Math.round(totalStored / totalCabinets) : 0;
    const activeAreas = areaSet.size;

    return {
        totalCabinets,
        totalStored,
        averageStorage,
        activeAreas
    };
});

// 获取Token
const getToken = () => {
    return localStorage.getItem('Admin-Token') || '';
};

const headers = computed(() => ({ Authorization: "Bearer " + getToken() }));

// 根据状态返回样式类名
const getStatusClass = (status) => {
    const statusMap = {
        '已结束': 'status-completed',
        '已逾期': 'status-overdue',
        '已批准': 'status-active',
        '已取消': 'status-cancelled'
    };
    return statusMap[status] || 'status-default';
};

// 更新时间
const updateTime = () => {
    const date = new Date();
    const y = date.getFullYear();
    const m = date.getMonth() + 1;
    const d = date.getDate();
    const h = date.getHours();
    const min = date.getMinutes();
    const s = date.getSeconds();

    const formatNumber = (num) => num < 10 ? "0" + num : num;

    currentTime.value = `${y}-${formatNumber(m)}-${formatNumber(d)} ${formatNumber(h)}:${formatNumber(min)}:${formatNumber(s)}`;
};

// API调用函数 - 获取最新统计数据
const handleLatestStatistics = async () => {
    try {
        loading.value = true;
        console.log('正在调用API获取最新统计数据...');

        const response = await getLatestStatistics();
        const data = response.data || response;

        if (data) {
            console.log('API返回的统计数据:', data);
            return {
                totalBorrows: data.totalBorrows || 0,
                totalReturns: data.totalReturns || 0,
                totalLoans: data.totalLoans || 0,
                totalLost: data.totalLost || 0,
                archivedCount: data.archivedCount || 0,
                pendingCount: data.pendingCount || 0,
                electronicCount: data.electronicCount || 0,
                physicalCount: data.physicalCount || 0,
                cabinetCount: data.cabinetCount || 0
            };
        }
    } catch (error) {
        console.error('API调用失败，详细错误:', error);
        console.warn('使用模拟数据代替真实API数据');

        return {
            totalBorrows: 177413,
            totalReturns: 156789,
            totalLoans: 20624,
            totalLost: 1066,
            archivedCount: 76921,
            pendingCount: 19206,
            electronicCount: 45678,
            physicalCount: 50549,
            cabinetCount: 100
        };
    } finally {
        loading.value = false;
    }
};

// API调用函数 - 获取档案柜信息（去掉容量相关）
const getCabinetInfo = async () => {
    try {
        loading.value = true;
        console.log('正在调用API获取档案柜信息...');

        const response = await listCabinet({ pageNum: 1, pageSize: 200 });
        const data = response.rows;

        if (data) {
            console.log('API返回的档案柜信息:', data);
            return data;
        }
    } catch (error) {
        console.error('API调用失败，详细错误:', error);
        console.warn('使用模拟档案柜数据');

        // 返回模拟数据（去掉capacity字段）
        return [
            { id: 1, name: "01-01", size: 320, zyNo: "A" },
            { id: 2, name: "01-01", size: 280, zyNo: "B" },
            { id: 3, name: "01-02", size: 410, zyNo: "A" },
            { id: 4, name: "01-02", size: 390, zyNo: "B" },
            { id: 5, name: "02-01", size: 200, zyNo: "A" },
            { id: 6, name: "02-02", size: 150, zyNo: "A" },
            { id: 7, name: "03-01", size: 380, zyNo: "A" },
            { id: 8, name: "03-01", size: 360, zyNo: "B" },
            { id: 9, name: "04-01", size: 420, zyNo: "A" },
            { id: 10, name: "04-01", size: 400, zyNo: "B" },
            { id: 11, name: "05-01", size: 300, zyNo: "A" },
            { id: 12, name: "05-02", size: 250, zyNo: "A" }
        ];
    } finally {
        loading.value = false;
    }
};

// API调用函数 - 获取最近7天数据
const getRecent7DaysData = async () => {
    try {
        loading.value = true;
        console.log('正在调用API获取最近7天数据...');

        const response = await getRecentBorrowData(7)
        const data = response.data || response;

        if (data && Array.isArray(data)) {
            console.log('API返回的7天数据:', data);
            return data.sort((a, b) => new Date(a.statDate) - new Date(b.statDate));
        }
    } catch (error) {
        console.error('获取7天数据API调用失败，详细错误:', error);
        console.warn('使用模拟7天数据');

        const mockData = [];
        const today = new Date();
        for (let i = 6; i >= 0; i--) {
            const date = new Date(today);
            date.setDate(today.getDate() - i);
            mockData.push({
                statDate: date.toISOString().split('T')[0],
                archivedCount: Math.floor(Math.random() * 100) + 50,
                totalBorrows: Math.floor(Math.random() * 80) + 20,
                totalReturns: Math.floor(Math.random() * 70) + 15,
                totalLoans: Math.floor(Math.random() * 30) + 10,
                cabinetCount: Math.floor(Math.random() * 10) + 5
            });
        }
        console.log('生成的模拟数据:', mockData);
        return mockData;
    } finally {
        loading.value = false;
    }
};

// API调用函数 - 获取借阅记录
const getBorrowRecords = async () => {
    try {
        loading.value = true;
        console.log('正在调用API获取借阅记录...');

        const response = await listRecord({ pageNum: 1, pageSize: 10 })
        const data = response.data || response;

        if (data && data.code === 200 && data.rows) {
            console.log('API返回的借阅记录:', data.rows);
            return data.rows.slice(0, 10);
        }
    } catch (error) {
        console.error('获取借阅记录API调用失败，详细错误:', error);
        console.warn('使用模拟借阅记录数据');

        return [
            { id: 1, userName: '张三', startApplyTime: '2025-07-20', status: '已结束' },
            { id: 2, userName: '李四', startApplyTime: '2025-07-19', status: '已逾期' },
            { id: 3, userName: '王五', startApplyTime: '2025-07-18', status: '进行中' },
            { id: 4, userName: '赵六', startApplyTime: '2025-07-17', status: '已结束' },
            { id: 5, userName: '钱七', startApplyTime: '2025-07-16', status: '已取消' },
            { id: 6, userName: '孙八', startApplyTime: '2025-07-15', status: '已结束' }
        ];
    } finally {
        loading.value = false;
    }
};

// 初始化图表1 - 归档档案/档案利用分析
const initChart1 = (data) => {
    if (!data || data.length === 0) {
        console.warn('图表1：数据为空，使用默认数据');
        data = [
            { statDate: '2024-07-18', archivedCount: 75, totalBorrows: 45 },
            { statDate: '2024-07-19', archivedCount: 82, totalBorrows: 52 },
            { statDate: '2024-07-20', archivedCount: 68, totalBorrows: 38 },
            { statDate: '2024-07-21', archivedCount: 91, totalBorrows: 67 },
            { statDate: '2024-07-22', archivedCount: 77, totalBorrows: 43 },
            { statDate: '2024-07-23', archivedCount: 85, totalBorrows: 58 },
            { statDate: '2024-07-24', archivedCount: 73, totalBorrows: 41 }
        ];
    }

    const chart = echarts.init(echarts1.value);

    const dates = data.map(item => {
        const date = new Date(item.statDate);
        return (date.getMonth() + 1) + '-' + date.getDate();
    });

    const option = {
        backgroundColor: 'transparent',
        tooltip: {
            trigger: 'axis',
            axisPointer: {
                type: 'cross',
                lineStyle: {
                    color: '#00f5ff'
                }
            },
            backgroundColor: 'rgba(0, 20, 40, 0.9)',
            borderColor: '#00f5ff',
            borderWidth: 1,
            textStyle: {
                color: '#fff',
                fontSize: 12
            }
        },
        legend: {
            data: ['归档数量', '借阅次数'],
            textStyle: {
                color: '#fff',
                fontSize: 12
            },
            top: 10,
            itemGap: 20
        },
        grid: {
            left: '8%',
            right: '8%',
            bottom: '15%',
            top: '20%',
            containLabel: true
        },
        xAxis: {
            type: 'category',
            data: dates,
            axisLabel: {
                color: '#9cd9f0',
                fontSize: 11
            },
            axisLine: {
                lineStyle: {
                    color: '#2c5a7a'
                }
            },
            splitLine: {
                show: false
            }
        },
        yAxis: {
            type: 'value',
            axisLabel: {
                color: '#9cd9f0',
                fontSize: 11
            },
            axisLine: {
                lineStyle: {
                    color: '#2c5a7a'
                }
            },
            splitLine: {
                lineStyle: {
                    color: 'rgba(44, 90, 122, 0.3)',
                    type: 'dashed'
                }
            }
        },
        series: [
            {
                name: '归档数量',
                type: 'line',
                data: data.map(item => item.archivedCount || 0),
                smooth: true,
                symbol: 'circle',
                symbolSize: 6,
                lineStyle: {
                    width: 3,
                    shadowColor: '#00f5ff',
                    shadowBlur: 10
                },
                itemStyle: {
                    color: '#00f5ff',
                    borderColor: '#fff',
                    borderWidth: 2
                },
                areaStyle: {
                    color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                        { offset: 0, color: 'rgba(0, 245, 255, 0.4)' },
                        { offset: 1, color: 'rgba(0, 245, 255, 0.1)' }
                    ])
                }
            },
            {
                name: '借阅次数',
                type: 'line',
                data: data.map(item => item.totalBorrows || 0),
                smooth: true,
                symbol: 'circle',
                symbolSize: 6,
                lineStyle: {
                    width: 3,
                    shadowColor: '#ff6b9d',
                    shadowBlur: 10
                },
                itemStyle: {
                    color: '#ff6b9d',
                    borderColor: '#fff',
                    borderWidth: 2
                },
                areaStyle: {
                    color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                        { offset: 0, color: 'rgba(255, 107, 157, 0.4)' },
                        { offset: 1, color: 'rgba(255, 107, 157, 0.1)' }
                    ])
                }
            }
        ]
    };

    chart.setOption(option);
    window.addEventListener('resize', () => chart.resize());
};

// 初始化图表2 - 在架数/逾期待归还分析
const initChart2 = (data) => {
    if (!data || data.length === 0) {
        console.warn('图表2：数据为空，使用默认数据');
        data = [
            { statDate: '2024-07-18', archivedCount: 850, totalLoans: 120, totalReturns: 110, totalLost: 15 },
            { statDate: '2024-07-19', archivedCount: 860, totalLoans: 125, totalReturns: 115, totalLost: 18 },
            { statDate: '2024-07-20', archivedCount: 845, totalLoans: 118, totalReturns: 108, totalLost: 12 },
            { statDate: '2024-07-21', archivedCount: 875, totalLoans: 132, totalReturns: 122, totalLost: 20 },
            { statDate: '2024-07-22', archivedCount: 855, totalLoans: 128, totalReturns: 118, totalLost: 16 },
            { statDate: '2024-07-23', archivedCount: 870, totalLoans: 135, totalReturns: 125, totalLost: 22 },
            { statDate: '2024-07-24', archivedCount: 865, totalLoans: 130, totalReturns: 120, totalLost: 19 }
        ];
    }

    const chart = echarts.init(echarts2.value);
    const dates = data.map(item => {
        const date = new Date(item.statDate);
        return (date.getMonth() + 1) + '-' + date.getDate();
    });

    const inLibraryData = data.map(item => {
        return item.cabinetCount || 0;
    });

    const option = {
        backgroundColor: 'transparent',
        tooltip: {
            trigger: 'axis',
            backgroundColor: 'rgba(0, 20, 40, 0.9)',
            borderColor: '#00f5ff',
            borderWidth: 1,
            textStyle: {
                color: '#fff'
            }
        },
        legend: {
            data: ['在架数量', '待归还'],
            textStyle: {
                color: '#fff',
                fontSize: 12
            },
            top: 10
        },
        grid: {
            left: '8%',
            right: '8%',
            bottom: '15%',
            top: '20%',
            containLabel: true
        },
        xAxis: {
            type: 'category',
            data: dates,
            axisLabel: {
                color: '#9cd9f0',
                fontSize: 11
            },
            axisLine: {
                lineStyle: {
                    color: '#2c5a7a'
                }
            }
        },
        yAxis: {
            type: 'value',
            axisLabel: {
                color: '#9cd9f0',
                fontSize: 11
            },
            axisLine: {
                lineStyle: {
                    color: '#2c5a7a'
                }
            },
            splitLine: {
                lineStyle: {
                    color: 'rgba(44, 90, 122, 0.3)',
                    type: 'dashed'
                }
            }
        },
        series: [
            {
                name: '在架数量',
                type: 'bar',
                data: inLibraryData,
                barWidth: '40%',
                itemStyle: {
                    color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                        { offset: 0, color: '#00f5ff' },
                        { offset: 1, color: '#005577' }
                    ]),
                    shadowColor: '#00f5ff',
                    shadowBlur: 10
                }
            },
            {
                name: '待归还',
                type: 'bar',
                data: data.map(item => item.totalLoans || 0),
                barWidth: '40%',
                itemStyle: {
                    color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                        { offset: 0, color: '#ff6b9d' },
                        { offset: 1, color: '#7a1538' }
                    ]),
                    shadowColor: '#ff6b9d',
                    shadowBlur: 10
                }
            }
        ]
    };

    chart.setOption(option);
    window.addEventListener('resize', () => chart.resize());
};

// 初始化图表3 - 档案柜存储情况（去掉容量相关）
const initChart3 = (cabinetData) => {
    if (!cabinetData || cabinetData.length === 0) {
        console.warn('图表3：档案柜数据为空，使用默认数据');
        cabinetData = [
            { name: "01-01", size: 320, zyNo: "A" },
            { name: "01-01", size: 280, zyNo: "B" },
            { name: "01-02", size: 410, zyNo: "A" },
            { name: "01-02", size: 390, zyNo: "B" },
            { name: "02-01", size: 200, zyNo: "A" },
            { name: "02-02", size: 150, zyNo: "A" },
            { name: "03-01", size: 380, zyNo: "A" },
            { name: "03-01", size: 360, zyNo: "B" },
            { name: "04-01", size: 420, zyNo: "A" },
            { name: "04-01", size: 400, zyNo: "B" }
        ];
    }

    const chart = echarts.init(echarts3.value);

    // 按区统计数据
    const areaStats = {};
    cabinetData.forEach(cabinet => {
        const area = cabinet.name.substring(0, 2);
        if (!areaStats[area]) {
            areaStats[area] = {
                totalStored: 0,
                cabinetCount: 0,
                columns: new Set()
            };
        }

        areaStats[area].totalStored += Math.max(0, cabinet.size || 0);
        areaStats[area].cabinetCount += 1;
        areaStats[area].columns.add(cabinet.name.substring(3, 5));
    });

    const areas = Object.keys(areaStats).sort();
    const storedData = areas.map(area => areaStats[area].totalStored);
    const cabinetCountData = areas.map(area => areaStats[area].cabinetCount);
    const averageStorageData = areas.map(area => {
        const avg = areaStats[area].cabinetCount > 0
            ? Math.round(areaStats[area].totalStored / areaStats[area].cabinetCount)
            : 0;
        return avg;
    });

    const option = {
        backgroundColor: 'transparent',
        tooltip: {
            trigger: 'axis',
            backgroundColor: 'rgba(0, 20, 40, 0.9)',
            borderColor: '#00f5ff',
            borderWidth: 1,
            textStyle: {
                color: '#fff'
            },
            formatter: function (params) {
                let result = `${params[0].name}<br/>`;
                params.forEach(param => {
                    if (param.seriesName === '平均存储') {
                        result += `${param.seriesName}: ${param.value}册/柜<br/>`;
                    } else if (param.seriesName === '档案柜数量') {
                        result += `${param.seriesName}: ${param.value}个<br/>`;
                    } else {
                        result += `${param.seriesName}: ${param.value}册<br/>`;
                    }
                });
                return result;
            }
        },
        legend: {
            data: ['存储总量', '档案柜数量', '平均存储'],
            textStyle: {
                color: '#fff',
                fontSize: 11
            },
            top: 5
        },
        grid: {
            left: '8%',
            right: '8%',
            bottom: '15%',
            top: '25%',
            containLabel: true
        },
        xAxis: {
            type: 'category',
            data: areas.map(area => `第${area}区`),
            axisLabel: {
                color: '#9cd9f0',
                fontSize: 10
            },
            axisLine: {
                lineStyle: {
                    color: '#2c5a7a'
                }
            }
        },
        yAxis: [
            {
                type: 'value',
                name: '数量',
                axisLabel: {
                    color: '#9cd9f0',
                    fontSize: 10
                },
                axisLine: {
                    lineStyle: {
                        color: '#2c5a7a'
                    }
                },
                splitLine: {
                    lineStyle: {
                        color: 'rgba(44, 90, 122, 0.3)',
                        type: 'dashed'
                    }
                }
            },
            {
                type: 'value',
                name: '平均存储(册/柜)',
                axisLabel: {
                    color: '#9cd9f0',
                    fontSize: 10
                },
                axisLine: {
                    lineStyle: {
                        color: '#2c5a7a'
                    }
                },
                splitLine: {
                    show: false
                }
            }
        ],
        series: [
            {
                name: '存储总量',
                type: 'bar',
                data: storedData,
                barWidth: '25%',
                itemStyle: {
                    color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                        { offset: 0, color: '#4facfe' },
                        { offset: 1, color: '#00f2fe' }
                    ]),
                    shadowColor: '#4facfe',
                    shadowBlur: 10
                }
            },
            {
                name: '档案柜数量',
                type: 'bar',
                data: cabinetCountData,
                barWidth: '25%',
                itemStyle: {
                    color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                        { offset: 0, color: '#ff6b9d' },
                        { offset: 1, color: '#c44569' }
                    ]),
                    shadowColor: '#ff6b9d',
                    shadowBlur: 10
                }
            },
            {
                name: '平均存储',
                type: 'line',
                yAxisIndex: 1,
                data: averageStorageData,
                lineStyle: {
                    width: 3,
                    shadowColor: '#f093fb',
                    shadowBlur: 10
                },
                itemStyle: {
                    color: '#f093fb',
                    borderColor: '#fff',
                    borderWidth: 2
                },
                symbol: 'circle',
                symbolSize: 8
            }
        ]
    };

    chart.setOption(option);
    window.addEventListener('resize', () => chart.resize());
};

// 初始化图表4 - 档案构成情况分析
const initChart4 = (electronicCount, physicalCount) => {
    const chart = echarts.init(echarts4.value);

    electronicCount = electronicCount || 45678;
    physicalCount = physicalCount || 50549;

    const option = {
        backgroundColor: 'transparent',
        tooltip: {
            trigger: 'item',
            backgroundColor: 'rgba(0, 20, 40, 0.9)',
            borderColor: '#00f5ff',
            borderWidth: 1,
            textStyle: {
                color: '#fff'
            }
        },
        legend: {
            bottom: 20,
            left: 'center',
            textStyle: {
                color: '#fff',
                fontSize: 11
            },
            itemGap: 20
        },
        series: [
            {
                name: '档案类型',
                type: 'pie',
                radius: ['35%', '65%'],
                center: ['50%', '45%'],
                avoidLabelOverlap: false,
                itemStyle: {
                    borderRadius: 8,
                    borderColor: 'rgba(255, 255, 255, 0.2)',
                    borderWidth: 2,
                    shadowBlur: 20,
                    shadowColor: 'rgba(0, 0, 0, 0.5)'
                },
                label: {
                    show: false
                },
                emphasis: {
                    label: {
                        show: true,
                        fontSize: '14',
                        fontWeight: 'bold',
                        color: '#fff'
                    }
                },
                labelLine: {
                    show: false
                },
                data: [
                    {
                        value: electronicCount,
                        name: '电子档案',
                        itemStyle: {
                            color: new echarts.graphic.LinearGradient(0, 0, 1, 1, [
                                { offset: 0, color: '#00f5ff' },
                                { offset: 1, color: '#0099cc' }
                            ])
                        }
                    },
                    {
                        value: physicalCount,
                        name: '实体档案',
                        itemStyle: {
                            color: new echarts.graphic.LinearGradient(0, 0, 1, 1, [
                                { offset: 0, color: '#ff6b9d' },
                                { offset: 1, color: '#cc3366' }
                            ])
                        }
                    }
                ]
            }
        ]
    };

    chart.setOption(option);
    window.addEventListener('resize', () => chart.resize());
};

// 安全的图表初始化函数
const safeInitChart = async (elementRef, initFunction) => {
    try {
        if (!elementRef || !elementRef.value) {
            console.error(`DOM元素未找到`);
            return false;
        }

        // 确保元素有足够的尺寸
        const rect = elementRef.value.getBoundingClientRect();
        if (rect.width === 0 || rect.height === 0) {
            console.warn('图表容器尺寸为0，延迟初始化');
            setTimeout(() => {
                initFunction();
            }, 100);
            return true;
        }

        initFunction();
        console.log(`图表初始化成功`);
        return true;
    } catch (error) {
        console.error(`图表初始化失败:`, error);
        return false;
    }
};

// 显示消息的函数
const showMessage = (message, type = 'info') => {
    console.log(`[${type.toUpperCase()}] ${message}`);
};

// 初始化数据加载
const initializeData = async () => {
    try {
        showMessage('开始加载数据...', 'info');

        const [statsData, recent7Days, borrowRecordsData, cabinetInfo] = await Promise.all([
            handleLatestStatistics(),
            getRecent7DaysData(),
            getBorrowRecords(),
            getCabinetInfo()
        ]);

        // 更新响应式数据
        statistics.value = statsData;
        recent7DaysData.value = recent7Days;
        borrowRecords.value = borrowRecordsData;
        cabinetData.value = cabinetInfo;

        console.log('数据初始化完成');

        // 等待DOM更新后初始化图表
        await nextTick();

        console.log('开始初始化图表...');
        console.log('图表元素检查:', {
            echarts1: !!echarts1.value,
            echarts2: !!echarts2.value,
            echarts3: !!echarts3.value,
            echarts4: !!echarts4.value
        });

        // 延迟一点时间确保DOM完全渲染
        setTimeout(async () => {
            const results = await Promise.all([
                safeInitChart(echarts1, () => initChart1(recent7Days)),
                safeInitChart(echarts2, () => initChart2(recent7Days)),
                safeInitChart(echarts3, () => initChart3(cabinetInfo)),
                safeInitChart(echarts4, () => initChart4(statistics.value.electronicCount, statistics.value.physicalCount))
            ]);

            const successCount = results.filter(r => r).length;
            showMessage(`图表初始化完成，成功: ${successCount}/4`, successCount === 4 ? 'success' : 'warning');
        }, 300);

    } catch (error) {
        console.error('数据加载失败:', error);
        showMessage('数据加载失败，请检查网络连接', 'error');
    }
};


// 响应式变量定义
const infoList = ref([]);
const selectedArea = ref('');
// 转换后的 handleAreaChange 方法
const handleAreaChange = async (areaNo) => {
  try {
    const response = await getInfo(areaNo);
    console.log('根据区号查询ip', response);

    const ip = response.data['gdlIp'];
    const port = response.data['gdlPort'];

    // 假设 queryAreaData 也是在 setup 中定义的函数
    await queryAreaData(ip, port);

    // 更新气体值
    CO2Level.value = currentData.CO2;
    PM10Level.value = currentData.PM10;
    TVOCLevel.value = currentData.TVOC;
    PM2_5Level.value = currentData.PM2_5;

    // 更新当前湿度值
    currentData.Hum = parseFloat(currentData.Hum / 100).toFixed(2);

    // 重新初始化水滴图
    initChart();
    // 绘制方块
    // drawBlocks();
  } catch (error) {
    console.error('处理区号变更时出错:', error);
    // 可以添加错误处理逻辑
  }
};
/** 查询固定列IP信息管理列表 */
function getIpList() {
  listInfo().then(response => {
    // 存储获取到的IP列表
    infoList.value = response.rows;
    console.log(infoList.value);

    // 获取第一个IP信息作为默认查询
    if (infoList.value.length > 0) {
      const firstIpInfo = infoList.value[0];
      selectedArea.value = firstIpInfo.gdlNo;
      queryAreaData(firstIpInfo.gdlIp, firstIpInfo.gdlPort);
    }

    // 遍历IP列表，执行相关操作
    infoList.value.forEach(ipInfo => {
      // 为每个区域解除休眠、解锁、架体归位(合并)
      // checkConnect(ipInfo.gdlIp, ipInfo.gdlPort, ipInfo.gdlNo);
      // queryAreaDataForXiuMian(ipInfo.gdlIp, ipInfo.gdlPort, ipInfo.gdlNo);
    });
  }).catch(error => {
    console.error('获取IP列表失败:', error);
  });
}

// 获取温湿度数据
/** 查询区号数据 */
async function queryAreaData(ip, port) {
  try {
    const response = await axios({
      url: `http://${ip}:${port}/MjjWebApi?Op=GetMjjStatus`,
      method: 'get',
    });

    // 存储服务器返回的数据
    currentData.value = response.data.data[0];

    // 更新气体值
    CO2Level.value = currentData.value.CO2;
    PM10Level.value = currentData.value.PM10;
    TVOCLevel.value = currentData.value.TVOC;
    PM2_5Level.value = currentData.value.PM2_5;

    // 更新当前湿度值（处理为小数）
    currentData.value.Hum = parseFloat(currentData.value.Hum / 100).toFixed(2);

    // 初始化图表
    initChart();
  } catch (error) {
    console.error('请求失败:', error);
  }
}
// 存储图表实例
// 定义DOM引用（与模板中ref="oneBox"对应）
const one_box = ref(null);
/** 初始化水滴图图表 */
function initChart() {
    // 使用ref获取DOM元素（模板中需有ref="oneBox"的元素）
    const chart = echarts.init(one_box.value);// 图表配置项

  // 保留小数点后三位（处理空值默认0.5）
  const formattedHumidity = parseFloat(currentData.value.Hum || 0.5).toFixed(3);

  // 更新图表配置项（响应式变量需通过.value访问）
  const option2 = {
    // backgroundColor: "white",
    series: [
      {
        type: "liquidFill",
        radius: "80%",
        center: ["50%", "50%"],
        amplitude: 20,
        data: [parseFloat(formattedHumidity)], // 使用格式化后的湿度值
        color: ["#8bd7f6"],
        backgroundStyle: {
          borderWidth: 6,
          borderColor: "#23cc72",
          color: "#485C6D"
        },
        label: {
          position: ["50%", "50%"],
          // 显示百分比形式（乘以100后保留两位小数）
          formatter: `${parseFloat(formattedHumidity * 100).toFixed(2)}%`,
          fontSize: "40px",
          color: "rgba(22,171,78,0.7)"
        },
        outline: {
          show: false
        }
      }
    ]
  };

  // 设置图表配置
  chart.setOption(option2);
}


// 组件挂载
onMounted(async () => {
    console.log('组件挂载开始');

    // 初始化时间
    updateTime();
    timeInterval = setInterval(updateTime, 1000);

    // 初始化数据和图表
    await initializeData();
    getIpList();
    // queryAreaData
    // await queryAreaData('127.0.0.1', '9005')
    // initChart();

    // 设置定时刷新（每5分钟）
    refreshInterval = setInterval(initializeData, 5 * 60 * 1000);
    refreshWSInterval = setInterval(getIpList, 5 * 60 * 1000);

    // 添加温湿度定时刷新功能


    console.log('组件挂载完成');
});

// 组件卸载
onUnmounted(() => {
    if (timeInterval) {
        clearInterval(timeInterval);
    }
    if (refreshInterval) {
        clearInterval(refreshInterval);
    }
    // refreshWSInterval
    if (refreshWSInterval) {
      clearInterval(refreshWSInterval);
    }
    console.log('组件已卸载，定时器已清除');
});
</script>

<style scoped>
/* 全局样式 */
* {
    margin: 0;
    padding: 0;
    box-sizing: border-box;
}

.tsg.box.report {
    width: 100vw;
    height: 100vh;
    background: linear-gradient(135deg, #0c1426 0%, #1a2332 50%, #0f1419 100%);
    color: #fff;
    font-family: 'Microsoft YaHei', Arial, sans-serif;
    overflow: hidden;
    position: relative;
}

/* 动态背景 */
.bg-animation {
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    pointer-events: none;
    z-index: 0;
}

.bg-particles {
    position: absolute;
    width: 100%;
    height: 100%;
    background-image:
        radial-gradient(2px 2px at 20px 30px, rgba(0, 245, 255, 0.3), transparent),
        radial-gradient(2px 2px at 40px 70px, rgba(255, 107, 157, 0.2), transparent),
        radial-gradient(1px 1px at 90px 40px, rgba(240, 147, 251, 0.3), transparent),
        radial-gradient(1px 1px at 130px 80px, rgba(0, 245, 255, 0.2), transparent);
    background-repeat: repeat;
    background-size: 200px 100px;
    animation: particleMove 20s linear infinite;
}

@keyframes particleMove {
    0% {
        transform: translate(0, 0);
    }

    100% {
        transform: translate(-200px, -100px);
    }
}

.bg-grid {
    position: absolute;
    width: 100%;
    height: 100%;
    background-image:
        linear-gradient(rgba(0, 245, 255, 0.1) 1px, transparent 1px),
        linear-gradient(90deg, rgba(0, 245, 255, 0.1) 1px, transparent 1px);
    background-size: 50px 50px;
    animation: gridMove 30s linear infinite;
}

@keyframes gridMove {
    0% {
        transform: translate(0, 0);
    }

    100% {
        transform: translate(50px, 50px);
    }
}

.bg-glow {
    position: absolute;
    width: 100%;
    height: 100%;
    background: radial-gradient(ellipse at center, rgba(0, 245, 255, 0.1) 0%, transparent 70%);
    animation: glowPulse 4s ease-in-out infinite alternate;
}

@keyframes glowPulse {
    0% {
        opacity: 0.3;
    }

    100% {
        opacity: 0.6;
    }
}

/* 头部样式 */
.tsg_head {
    position: relative;
    z-index: 10;
    padding: 15px 20px;
    background: linear-gradient(135deg, rgba(0, 20, 40, 0.8) 0%, rgba(0, 40, 80, 0.6) 100%);
    border-bottom: 2px solid rgba(0, 245, 255, 0.3);
    backdrop-filter: blur(10px);
}

.tsg_title {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 15px;
}

.time-container {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 8px 15px;
    background: linear-gradient(135deg, rgba(0, 245, 255, 0.1) 0%, rgba(0, 150, 200, 0.05) 100%);
    border-radius: 20px;
    border: 1px solid rgba(0, 245, 255, 0.3);
}

.time-icon {
    font-size: 16px;
}

#time {
    color: #00f5ff;
    font-size: 14px;
    font-weight: 600;
    text-shadow: 0 0 10px rgba(0, 245, 255, 0.5);
}

.d2 {
    text-align: center;
}

.title-glow {
    font-size: 32px;
    font-weight: 700;
    background: linear-gradient(135deg, #00f5ff 0%, #ff6b9d 50%, #f093fb 100%);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
    text-shadow: 0 0 30px rgba(0, 245, 255, 0.5);
    margin-bottom: 5px;
}

.title-subtitle {
    font-size: 14px;
    color: #9cd9f0;
    letter-spacing: 2px;
    opacity: 0.8;
}

.d3 {
    width: 200px;
    /* 与d1保持平衡 */
}

/* 统计卡片样式 */
.tsg_hul {
    display: flex;
    justify-content: space-between;
    gap: 15px;
}

.tsg_hli {
    flex: 1;
}

.stat-card {
    display: flex;
    align-items: center;
    padding: 15px 20px;
    background: linear-gradient(135deg, rgba(0, 245, 255, 0.1) 0%, rgba(0, 150, 200, 0.05) 100%);
    border-radius: 12px;
    border: 1px solid rgba(0, 245, 255, 0.3);
    position: relative;
    overflow: hidden;
    transition: all 0.3s ease;
    height: 80px;
}

.stat-card:nth-child(2) {
    background: linear-gradient(135deg, rgba(255, 107, 157, 0.1) 0%, rgba(200, 50, 100, 0.05) 100%);
    border-color: rgba(255, 107, 157, 0.3);
}

.stat-card:nth-child(3) {
    background: linear-gradient(135deg, rgba(240, 147, 251, 0.1) 0%, rgba(200, 100, 200, 0.05) 100%);
    border-color: rgba(240, 147, 251, 0.3);
}

.stat-card:nth-child(4) {
    background: linear-gradient(135deg, rgba(76, 175, 80, 0.1) 0%, rgba(50, 150, 50, 0.05) 100%);
    border-color: rgba(76, 175, 80, 0.3);
}

.stat-card:nth-child(5) {
    background: linear-gradient(135deg, rgba(255, 193, 7, 0.1) 0%, rgba(200, 150, 0, 0.05) 100%);
    border-color: rgba(255, 193, 7, 0.3);
}

.stat-card:hover {
    transform: translateY(-2px);
    box-shadow: 0 10px 30px rgba(0, 245, 255, 0.2);
}

.stat-icon {
    width: 50px;
    height: 50px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-right: 15px;
    position: relative;
}

.stat-icon-total {
    background: linear-gradient(135deg, #00f5ff, #0099cc);
    box-shadow: 0 0 20px rgba(0, 245, 255, 0.4);
}

.stat-icon-new {
    background: linear-gradient(135deg, #ff6b9d, #c44569);
    box-shadow: 0 0 20px rgba(255, 107, 157, 0.4);
}

.stat-icon-shelf {
    background: linear-gradient(135deg, #f093fb, #c44569);
    box-shadow: 0 0 20px rgba(240, 147, 251, 0.4);
}

.stat-icon-borrow {
    background: linear-gradient(135deg, #4caf50, #2e7d32);
    box-shadow: 0 0 20px rgba(76, 175, 80, 0.4);
}

.stat-icon-recent {
    background: linear-gradient(135deg, #ffc107, #f57c00);
    box-shadow: 0 0 20px rgba(255, 193, 7, 0.4);
}

/* .stat-icon::before {
    content: "📊";
    font-size: 20px;
} */

.stat-icon-total::before {
    content: "📚";
}

.stat-icon-new::before {
    content: "📝";
}

.stat-icon-shelf::before {
    content: "🏛️";
}

.stat-icon-borrow::before {
    content: "📖";
}

.stat-icon-recent::before {
    content: "🔄";
}

.stat-content {
    flex: 1;
}

.stat-label {
    font-size: 14px;
    color: #9cd9f0;
    margin-bottom: 5px;
}

.stat-value {
    display: flex;
    align-items: baseline;
    gap: 5px;
}

.number {
    font-size: 24px;
    font-weight: 700;
    color: #00f5ff;
    text-shadow: 0 0 15px rgba(0, 245, 255, 0.6);
}

.stat-card:nth-child(2) .number {
    color: #ff6b9d;
    text-shadow: 0 0 15px rgba(255, 107, 157, 0.6);
}

.stat-card:nth-child(3) .number {
    color: #f093fb;
    text-shadow: 0 0 15px rgba(240, 147, 251, 0.6);
}

.stat-card:nth-child(4) .number {
    color: #4caf50;
    text-shadow: 0 0 15px rgba(76, 175, 80, 0.6);
}

.stat-card:nth-child(5) .number {
    color: #ffc107;
    text-shadow: 0 0 15px rgba(255, 193, 7, 0.6);
}

.unit {
    font-size: 12px;
    color: #9cd9f0;
}

.stat-glow {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: linear-gradient(45deg, transparent, rgba(0, 245, 255, 0.1), transparent);
    transform: translateX(-100%);
    transition: transform 0.6s;
}

.stat-card:hover .stat-glow {
    transform: translateX(100%);
}

/* 主体布局 - 稍微调整间距 */
.tsg_box {
    position: relative;
    z-index: 10;
    padding: 18px;
    /* 从20px减少到18px */
    height: calc(100vh - 180px);
    display: flex;
    gap: 18px;
    /* 从20px减少到18px */
}

.tsg_box_left,
.tsg_box_center,
.tsg_box_right {
    display: flex;
    flex-direction: column;
    gap: 20px;
}

.tsg_box_left,
.tsg_box_right {
    flex: 1;
}

.tsg_box_center {
    flex: 1.2;
}

/* 图表容器样式 - 关键调整 */
.tsg_box_left_li {
    background: linear-gradient(135deg, rgba(0, 20, 40, 0.8) 0%, rgba(0, 40, 80, 0.4) 100%);
    border-radius: 12px;
    border: 1px solid rgba(0, 245, 255, 0.3);
    padding: 12px;
    /* 从15px减少到12px */
    position: relative;
    overflow: hidden;
    backdrop-filter: blur(10px);
}

.tsg_box_left_li::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 2px;
    background: linear-gradient(90deg, transparent, #00f5ff, transparent);
    animation: borderGlow 3s ease-in-out infinite alternate;
}

@keyframes borderGlow {
    0% {
        opacity: 0.5;
    }

    100% {
        opacity: 1;
    }
}

.tsg_table_6 {
    flex: 1;
}

/* 中间容器的整体布局调整 */
.tsg_table_3 {
    height: 100%;
    display: flex;
    flex-direction: column;
}

.tsg_table_3 .chart-header {
    flex-shrink: 0;
    margin-bottom: 6px;
    /* 进一步减少间距 */
}

.tsg_table_3 .cabinet-overview {
    flex-shrink: 0;
}

.tsg_table_3 .chart-subtitle {
    flex-shrink: 0;
    margin: 2px 0;
    /* 进一步减少间距 */
}

/* 确保图表容器不会溢出 */
.tsg_table_3>div:last-child {
    flex: 1;
    min-height: 0;
    display: flex;
    flex-direction: column;
}

.tsg_table_7 {
    flex: 1;
}

/* 图表标题样式 */
.chart-header {
    display: flex;
    align-items: center;
    margin-bottom: 8px;
    /* 从15px减少到8px */
    position: relative;
    height: 24px;
    /* 固定高度 */
    flex-shrink: 0;
}

.header-icon {
    font-size: 16px;
    margin-right: 8px;
}

.header-title {
    font-size: 14px;
    font-weight: 600;
    color: #00f5ff;
    text-shadow: 0 0 10px rgba(0, 245, 255, 0.5);
}

.header-line {
    flex: 1;
    height: 1px;
    background: linear-gradient(90deg, rgba(0, 245, 255, 0.5), transparent);
    margin-left: 12px;
}

.chart-subtitle {
    text-align: center;
    color: #9cd9f0;
    font-size: 10px;
    margin: 3px 0;
    /* 从原来的间距减少 */
    opacity: 0.8;
    padding: 0 10px;
    flex-shrink: 0;
    height: 12px;
    /* 减少高度 */
    line-height: 12px;
}

/* 档案柜总体统计卡片 - 调整为4个卡片 */
.cabinet-overview {
    display: flex;
    gap: 6px;
    padding: 8px 12px;
    justify-content: space-between;
    flex-shrink: 0;
    height: 100px;
}

.overview-card {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 4px;
    background: linear-gradient(135deg, rgba(0, 245, 255, 0.1) 0%, rgba(0, 150, 200, 0.05) 100%);
    border-radius: 8px;
    border: 1px solid rgba(0, 245, 255, 0.3);
    position: relative;
    overflow: hidden;
    transition: all 0.3s ease;
    height: 100%;
    box-sizing: border-box;
    min-width: 0;
    /* 允许缩小 */
}

.overview-card:nth-child(2) {
    background: linear-gradient(135deg, rgba(255, 107, 157, 0.1) 0%, rgba(200, 50, 100, 0.05) 100%);
    border-color: rgba(255, 107, 157, 0.3);
}

.overview-card:nth-child(3) {
    background: linear-gradient(135deg, rgba(240, 147, 251, 0.1) 0%, rgba(200, 100, 200, 0.05) 100%);
    border-color: rgba(240, 147, 251, 0.3);
}

.overview-card:nth-child(4) {
    background: linear-gradient(135deg, rgba(76, 175, 80, 0.1) 0%, rgba(50, 150, 50, 0.05) 100%);
    border-color: rgba(76, 175, 80, 0.3);
}

.overview-card:hover {
    transform: translateY(-1px);
    box-shadow: 0 5px 15px rgba(0, 245, 255, 0.2);
}

.overview-icon {
    font-size: 12px;
    margin-bottom: 2px;
}

.overview-content {
    text-align: center;
    width: 100%;
}

.overview-label {
    font-size: 9px;
    color: #9cd9f0;
    margin-bottom: 2px;
    text-align: center;
    white-space: nowrap;
    /* 防止换行 */
}

.overview-number {
    font-size: 14px;
    font-weight: 700;
    color: #00f5ff;
    text-shadow: 0 0 12px rgba(0, 245, 255, 0.6);
    margin-bottom: 1px;
    text-align: center;
}

.overview-card:nth-child(2) .overview-number {
    color: #ff6b9d;
    text-shadow: 0 0 12px rgba(255, 107, 157, 0.6);
}

.overview-card:nth-child(3) .overview-number {
    color: #f093fb;
    text-shadow: 0 0 12px rgba(240, 147, 251, 0.6);
}

.overview-card:nth-child(4) .overview-number {
    color: #4caf50;
    text-shadow: 0 0 12px rgba(76, 175, 80, 0.6);
}

.overview-unit {
    font-size: 8px;
    color: #9cd9f0;
    text-align: center;
    white-space: nowrap;
    /* 防止换行 */
}

/* 图表容器 */
.echarts_1,
.echarts_2,
.echarts_3 {
    width: 100%;
}

.echarts_1 {
    height: calc(100% - 50px);
}

/* 图表容器 - 精确计算高度 */
.echarts_2 {
    /*
    计算逻辑：
    - chart-header: 24px + 6px margin = 30px
    - cabinet-overview: 100px
    - chart-subtitle: 12px + 4px margin = 16px
    - 容器padding: 12px * 2 = 24px
    - 总共需要减去: 30 + 100 + 16 + 24 = 170px
    */
    height: calc(100% - 170px);
    width: 100%;
    min-height: 180px;
    /* 确保最小高度 */
    padding: 0;
    box-sizing: border-box;
}

.echarts_3 {
    width: 100%;
    height: 100%;
    /* 确保高度充满容器 */
    min-height: 200px;
    /* 给一个最小高度 */
    padding: 0;
    /* 移除内边距 */
    box-sizing: border-box;
}

/* 档案柜总体统计卡片 - 关键调整 */
.cabinet-overview {
    display: flex;
    gap: 8px;
    /* 减少间距 */
    padding: 8px 12px;
    /* 减少内边距 */
    justify-content: space-around;
    flex-shrink: 0;
    height: 100px;
    /* 减少高度 */
}

.overview-card {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 6px;
    /* 减少内边距 */
    background: linear-gradient(135deg, rgba(0, 245, 255, 0.1) 0%, rgba(0, 150, 200, 0.05) 100%);
    border-radius: 10px;
    /* 稍微减小圆角 */
    border: 1px solid rgba(0, 245, 255, 0.3);
    position: relative;
    overflow: hidden;
    transition: all 0.3s ease;
    height: 100%;
    box-sizing: border-box;
}

.overview-card:nth-child(2) {
    background: linear-gradient(135deg, rgba(255, 107, 157, 0.1) 0%, rgba(200, 50, 100, 0.05) 100%);
    border-color: rgba(255, 107, 157, 0.3);
}

.overview-card:nth-child(3) {
    background: linear-gradient(135deg, rgba(240, 147, 251, 0.1) 0%, rgba(200, 100, 200, 0.05) 100%);
    border-color: rgba(240, 147, 251, 0.3);
}

.overview-card:hover {
    transform: translateY(-3px);
    box-shadow: 0 10px 25px rgba(0, 245, 255, 0.2);
}

.overview-icon {
    font-size: 18px;
    /* 稍微减小 */
    margin-bottom: 3px;
    /* 减少间距 */
    filter: drop-shadow(0 0 8px #00f5ff);
}

.overview-label {
    font-size: 10px;
    /* 稍微减小 */
    color: #9cd9f0;
    margin-bottom: 2px;
    /* 减少间距 */
    text-align: center;
}

.overview-number {
    font-size: 16px;
    /* 稍微减小 */
    font-weight: 700;
    color: #00f5ff;
    text-shadow: 0 0 12px rgba(0, 245, 255, 0.6);
    margin-bottom: 1px;
    /* 减少间距 */
    text-align: center;
}

.overview-card:nth-child(2) .overview-number {
    color: #ff6b9d;
    text-shadow: 0 0 12px rgba(255, 107, 157, 0.6);
}

.overview-card:nth-child(3) .overview-number {
    color: #f093fb;
    text-shadow: 0 0 12px rgba(240, 147, 251, 0.6);
}

.overview-unit {
    font-size: 9px;
    /* 稍微减小 */
    color: #9cd9f0;
    text-align: center;
}

.chart-subtitle {
    text-align: center;
    color: #9cd9f0;
    font-size: 10px;
    /* 稍微减小 */
    margin: 3px 0;
    /* 减少间距 */
    opacity: 0.8;
    padding: 0 10px;
    flex-shrink: 0;
    height: 16px;
    /* 减少高度 */
    line-height: 16px;
}

/* 借阅记录表格 - 关键调整 */
.record-table {
    flex: 1;
    display: flex;
    flex-direction: column;
    min-height: 0;
    /* 允许缩小 */
}

.record-header {
    display: flex;
    height: 30px;
    /* 减少高度 */
    background: linear-gradient(135deg, rgba(0, 245, 255, 0.15) 0%, rgba(255, 107, 157, 0.08) 100%);
    border-bottom: 1px solid rgba(0, 245, 255, 0.3);
    flex-shrink: 0;
}

.record-col {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 10px;
    /* 稍微减小 */
    font-weight: 600;
    color: #fff;
    text-shadow: 0 0 5px rgba(0, 245, 255, 0.3);
}

.record-body {
    flex: 1;
    overflow-y: auto;
    min-height: 0;
}

.record-row {
    display: flex;
    height: 40px;
    /* 减少行高 */
    border-bottom: 1px solid rgba(255, 255, 255, 0.1);
    transition: all 0.3s ease;
    position: relative;
}

.record-row:hover {
    background: rgba(0, 245, 255, 0.05);
}

.record-animate {
    animation: record-highlight 2s ease-in-out infinite alternate;
}

@keyframes record-highlight {
    0% {
        background: rgba(0, 245, 255, 0.05);
    }

    100% {
        background: rgba(255, 107, 157, 0.05);
    }
}

.record-cell {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 10px;
    /* 稍微减小 */
    color: #fff;
    padding: 0 6px;
    /* 减少内边距 */
}

.status-badge {
    padding: 2px 6px;
    /* 减少内边距 */
    border-radius: 10px;
    /* 稍微减小 */
    font-size: 9px;
    /* 稍微减小 */
    font-weight: 500;
    text-align: center;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
}

.status-completed {
    background: linear-gradient(135deg, #00f5ff 0%, #0099cc 100%);
    color: #fff;
}

.status-overdue {
    background: linear-gradient(135deg, #ff6b6b 0%, #cc0000 100%);
    color: #fff;
}

.status-active {
    background: linear-gradient(135deg, #ff6b9d 0%, #cc3366 100%);
    color: #fff;
}

.status-cancelled {
    background: linear-gradient(135deg, #999 0%, #666 100%);
    color: #fff;
}

.status-default {
    background: rgba(255, 255, 255, 0.1);
    color: #fff;
}

.record-empty {
    width: 100%;
    text-align: center;
    color: #9cd9f0;
    opacity: 0.7;
    padding: 15px;
    /* 减少内边距 */
    font-size: 11px;
    /* 稍微减小 */
    min-height: 40px;
}

/* 图表容器 - 右侧饼图调整 */
.chart-container {
    flex: 1;
    min-height: 200px;
    /* 给一个最小高度 */
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 5px;
    box-sizing: border-box;
}

/* 滚动条美化 */
.record-body::-webkit-scrollbar {
    width: 4px;
    /* 稍微减小 */
}

.record-body::-webkit-scrollbar-track {
    background: rgba(255, 255, 255, 0.05);
    border-radius: 2px;
}

.record-body::-webkit-scrollbar-thumb {
    background: linear-gradient(135deg, #00f5ff, #ff6b9d);
    border-radius: 2px;
}

.record-body::-webkit-scrollbar-thumb:hover {
    background: linear-gradient(135deg, #ff6b9d, #00f5ff);
}

/* 响应式调整 */
@media (max-width: 1366px) {
    .tsg_box {
        padding: 15px;
        gap: 15px;
    }

    .tsg_box_left_li {
        padding: 10px;
    }

    .echarts_2 {
        height: calc(100% - 160px);
        /* 相应调整 */
        min-height: 160px;
    }

    .chart-header {
        height: 20px;
        margin-bottom: 6px;
    }

    .header-title {
        font-size: 13px;
    }
}

@media (max-width: 1024px) {
    .tsg_box {
        padding: 12px;
        gap: 12px;
    }

    .tsg_box_left_li {
        padding: 8px;
    }

    .echarts_2 {
        height: calc(100% - 150px);
        min-height: 140px;
    }

    .chart-header {
        height: 18px;
        margin-bottom: 4px;
    }

    .header-title {
        font-size: 12px;
    }

    .chart-subtitle {
        height: 10px;
        line-height: 10px;
        font-size: 9px;
    }
}

/* 加载动画 */
.loading {
    display: flex;
    justify-content: center;
    align-items: center;
    height: 100%;
    color: #00f5ff;
    font-size: 16px;
}

.loading::after {
    content: '';
    width: 20px;
    height: 20px;
    border: 2px solid #00f5ff;
    border-top: 2px solid transparent;
    border-radius: 50%;
    animation: loading-spin 1s linear infinite;
    margin-left: 10px;
}

@keyframes loading-spin {
    0% {
        transform: rotate(0deg);
    }

    100% {
        transform: rotate(360deg);
    }
}

/* 档案柜专用动画效果 */
.overview-card::before {
    content: '';
    position: absolute;
    top: 0;
    left: -100%;
    width: 100%;
    height: 100%;
    background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.1), transparent);
    transition: left 0.6s ease;
}

.overview-card:hover::before {
    left: 100%;
}

/* 数据更新闪烁效果 */
@keyframes data-update {
    0% {
        background: rgba(0, 245, 255, 0.1);
    }

    50% {
        background: rgba(0, 245, 255, 0.3);
    }

    100% {
        background: rgba(0, 245, 255, 0.1);
    }
}

.overview-card.updating {
    animation: data-update 1s ease-in-out;
}

.one_box {
  width: 40%;
  height: 200px; /* 或根据需要调整高度 */
  margin-top: 45px;
}
</style>
