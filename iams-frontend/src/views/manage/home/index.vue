<template>
  <div class="app-container home">
    <!-- 新增控制按钮容器 -->
    <div class="control-bar">
      <!-- 位置统计按钮 -->
      <el-button type="success" @click="handleLocationStats" class="control-button" :loading="locationStatsLoading">
        <el-icon>
          <Location />
        </el-icon>
        位置统计
      </el-button>

      <!-- <el-button type="primary" @click="dialogVisible = true" class="control-button">
        主页控制台
      </el-button> -->
      <!-- 新增时间范围选择 -->
      <div class="time-range-selector">
        <el-radio-group v-model="timeRange" @change="handleTimeRangeChange">
          <el-radio-button label="hour">实时</el-radio-button>
          <el-radio-button label="day">日统计</el-radio-button>
          <el-radio-button label="month">月统计</el-radio-button>
          <el-radio-button label="bigscreen">大屏展示</el-radio-button>
        </el-radio-group>
        <el-date-picker v-if="timeRange === 'day'" v-model="selectedDate" type="date" placeholder="选择日期"
          @change="handleDateChange" style="margin-left: 10px;" />
        <el-date-picker v-if="timeRange === 'month'" v-model="selectedMonth" type="month" placeholder="选择月份"
          @change="handleMonthChange" style="margin-left: 10px;" />
      </div>
    </div>

    <!-- 统计时间提示 -->
    <div class="time-indicator">
      <el-tag :type="timeRange === 'hour' ? 'primary' : 'success'">
        {{ getTimeIndicatorText() }}
      </el-tag>
    </div>

    <!-- 位置统计对话框 -->
    <el-dialog title="档案位置统计" v-model="locationStatsVisible" width="800px" class="location-stats-dialog">
      <div class="location-stats-content">
        <div class="stats-summary">
          <el-statistic title="总档案数" :value="totalArchives" />
          <el-statistic title="不同位置数" :value="Object.keys(locationStats).length" />
        </div>

        <el-divider />

        <div class="location-table">
          <el-table :data="locationStatsTable" height="400" style="width: 100%">
            <el-table-column prop="location" label="存储位置" width="200" />
            <el-table-column prop="count" label="档案数量" width="100" align="center">
              <template #default="scope">
                <el-tag :type="getCountTagType(scope.row.count)">{{ scope.row.count }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="percentage" label="占比" width="100" align="center">
              <template #default="scope">
                {{ scope.row.percentage }}%
              </template>
            </el-table-column>
            <el-table-column label="进度条" align="center">
              <template #default="scope">
                <el-progress :percentage="scope.row.percentage" :stroke-width="8" />
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="synchronizeData" type="primary">同步数据</el-button>
          <el-button @click="locationStatsVisible = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>

    <el-dialog title="主页组件显示控制" v-model="dialogVisible" width="400px" class="component-control-dialog">
      <div class="switch-container">
        <el-form label-position="left" label-width="220px">
          <el-form-item label="借阅统计">
            <el-switch v-model="showUserTaskStats" active-color="#409EFF" inactive-color="#dcdfe6"></el-switch>
          </el-form-item>

          <el-form-item label="归档统计">
            <el-switch v-model="showSkuSaleStats" active-color="#409EFF" inactive-color="#dcdfe6"></el-switch>
          </el-form-item>

          <el-form-item label="借阅数据图表">
            <el-switch v-model="showSkuSaleCollectChart" active-color="#409EFF" inactive-color="#dcdfe6"></el-switch>
          </el-form-item>

          <el-form-item label="趋势图表">
            <el-switch v-model="showTrendChart" active-color="#409EFF" inactive-color="#dcdfe6"></el-switch>
          </el-form-item>
        </el-form>
      </div>
    </el-dialog>

    <!-- 主要统计区域 -->
    <el-row :gutter="20">
      <el-col :span="12" v-if="showUserTaskStats">
        <home-user-task-stats :taskData="userTaskData" :timeRange="timeRange" />
      </el-col>
      <el-col :span="12" v-if="showSkuSaleStats">
        <home-sku-sale-stats :skuData="skuSaleData" :timeRange="timeRange" />
      </el-col>
    </el-row>

    <!-- 图表展示区域 -->
    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="24" v-if="showSkuSaleCollectChart">
        <sku-sale-collect-chart :collectData="skuSaleCollectData" :timeRange="timeRange" />
      </el-col>
    </el-row>

    <!-- 趋势图表 -->
    <el-row :gutter="20" style="margin-top: 20px" v-if="showTrendChart">
      <el-col :span="24">
        <statistics-trend-chart :trendData="trendData" :timeRange="timeRange" />
      </el-col>
    </el-row>

    <reminder-popup />
  </div>
</template>

<script setup name="Home">
import HomeUserTaskStats from './components/home-user-task-stats.vue';
import HomeSkuSaleStats from './components/home-sku-sale-stats.vue';
import SkuSaleCollectChart from './components/sku-sale-collect-chart.vue';
import StatisticsTrendChart from './components/statistics-trend-chart.vue';

import { onMounted, ref, nextTick } from 'vue';
import {
  getLatestStatistics,
  getDailyStatistics,
  getMonthlyStatistics
} from '@/api/manage/statistics';
import { getArchivedList } from '@/api/manage/archive'; // 假设这是获取档案列表的API
import { synchronizeLocationStats } from '@/api/manage/cabinet';
import ReminderPopup from '@/components/ReminderPopup/index.vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Location } from '@element-plus/icons-vue';

const dialogVisible = ref(false);
const showUserTaskStats = ref(true);
const showSkuSaleStats = ref(true);
const showSkuSaleCollectChart = ref(true);
const showTrendChart = ref(true);

// 位置统计相关
const locationStatsVisible = ref(false);
const locationStatsLoading = ref(false);
const locationStats = ref({});
const locationStatsTable = ref([]);
const totalArchives = ref(0);

// 时间范围控制
const timeRange = ref('hour'); // 'hour', 'day', 'month'
const selectedDate = ref(new Date());
const selectedMonth = ref(new Date());

// 数据
const userTaskData = ref({});
const skuSaleData = ref({});
const skuSaleCollectData = ref([]);
const trendData = ref([]);

// 处理位置统计
const handleLocationStats = async () => {
  locationStatsLoading.value = true;

  try {
    // 获取已上架的档案列表
    const response = await getArchivedList();
    const archivedList = response.rows || response;

    if (!archivedList || archivedList.length === 0) {
      ElMessage.warning('暂无已上架的档案数据');
      return;
    }

    // 统计位置信息
    const locationStatsMap = {};

    archivedList.forEach(archive => {
      // 检查必要的位置信息是否存在
      if (archive.shitiLocation && archive.exactLocation) {
        // 拼接完整的位置信息
        const fullLocation = `${archive.shitiLocation}-${archive.exactLocation}`;

        // 统计数量
        if (locationStatsMap[fullLocation]) {
          locationStatsMap[fullLocation]++;
        } else {
          locationStatsMap[fullLocation] = 1;
        }
      }
    });

    // 更新统计数据
    locationStats.value = locationStatsMap;
    totalArchives.value = archivedList.length;

    // 转换为表格数据并按数量排序
    const tableData = Object.entries(locationStatsMap)
      .map(([location, count]) => ({
        location,
        count,
        percentage: ((count / archivedList.length) * 100).toFixed(1)
      }))
      .sort((a, b) => b.count - a.count);

    locationStatsTable.value = tableData;

    // 显示统计对话框
    locationStatsVisible.value = true;

    ElMessage.success(`位置统计完成，共统计 ${archivedList.length} 个档案，涉及 ${Object.keys(locationStatsMap).length} 个不同位置`);

  } catch (error) {
    console.error('位置统计失败:', error);
    ElMessage.error('位置统计失败，请稍后重试');
  } finally {
    locationStatsLoading.value = false;
  }
};

// 获取数量标签类型
const getCountTagType = (count) => {
  if (count >= 15) return 'danger';
  if (count >= 10) return 'warning';
  if (count >= 1) return 'success';
  return 'info';
};

// 同步数据
const  synchronizeData = async() => {
  try {
    const response = await synchronizeLocationStats(locationStats.value);
    if (response.success) {
      ElMessage.success('同步成功');
    } else {
      ElMessage.error('同步失败，请稍后重试');
    }
  } catch (error) {
    console.error('同步失败:', error);
    ElMessage.error('同步失败，请稍后重试');
  }
};

// 获取时间指示器文本
const getTimeIndicatorText = () => {
  switch (timeRange.value) {
    case 'hour':
      return '实时统计 (最新数据)';
    case 'day':
      return `日统计 - ${selectedDate.value.toLocaleDateString()}`;
    case 'month':
      return `月统计 - ${selectedMonth.value.getFullYear()}年${selectedMonth.value.getMonth() + 1}月`;
    default:
      return '实时统计';
  }
};

// 处理时间范围变化（仅影响趋势图表）
// const handleTimeRangeChange = (value) => {
//   timeRange.value = value;
//   // 只加载趋势数据，不影响其他组件
//   loadStatisticsData();
// };
//大屏展示
const handleTimeRangeChange = (value) => {
  if (value === 'bigscreen') {
    window.location.href = '/bigscreen.html'; // 跳转到 bigscreen.html
    return;
  }

  timeRange.value = value;
  loadStatisticsData(); // 加载其他统计页面的数据
};

// 处理日期变化（仅影响趋势图表）
const handleDateChange = (date) => {
  selectedDate.value = date;
  // 只加载趋势数据，不影响其他组件
  loadStatisticsData();
};

// 处理月份变化（仅影响趋势图表）
const handleMonthChange = (month) => {
  selectedMonth.value = month;
  // 只加载趋势数据，不影响其他组件
  loadStatisticsData();
};

// 加载统计数据
const loadStatisticsData = async () => {
  try {
    let response;

    console.log('加载统计数据，时间范围:', timeRange.value); // 调试信息

    switch (timeRange.value) {
      case 'hour':
        response = await getLatestStatistics();
        console.log('实时数据响应:', response); // 调试信息
        // 检查数据结构，可能是 response 或 response.data
        const realtimeData = response.data || response;
        processRealTimeData(realtimeData);
        break;
      case 'day':
        response = await getDailyStatistics(selectedDate.value);
        console.log('日统计响应:', response); // 调试信息
        const dailyData = response.data || response;
        processDailyData(dailyData);
        break;
      case 'month':
        response = await getMonthlyStatistics(selectedMonth.value);
        console.log('月统计响应:', response); // 调试信息
        const monthlyData = response.data || response;
        processMonthlyData(monthlyData);
        break;
    }
  } catch (error) {
    console.error('加载统计数据失败:', error);
    ElMessage.error('加载统计数据失败');
  }
};

// 加载实时数据（用于统计卡片和图表）
const loadRealTimeData = async () => {
  try {
    const response = await getLatestStatistics();
    const data = response;

    // 更新统计卡片数据
    userTaskData.value = {
      data1: data.totalBorrows,
      data2: data.totalReturns,
      data3: data.totalLoans,
      data4: data.totalLost,
    };

    skuSaleData.value = {
      data1: data.archivedCount,
      data2: data.pendingCount,
    };

    skuSaleCollectData.value = [data.electronicCount, data.physicalCount];

  } catch (error) {
    console.error('加载实时数据失败:', error);
    ElMessage.error('加载实时数据失败');
  }
};

// 处理实时数据（仅影响趋势图表）
const processRealTimeData = (data) => {
  console.log('处理实时数据:', data); // 调试信息

  // 实时数据不显示趋势
  trendData.value = [];
  console.log('实时模式，清空趋势数据');
};

// 处理月统计数据（仅影响趋势图表）
const processMonthlyData = (data) => {
  console.log('处理月统计数据:', data); // 调试信息

  if (!data) {
    console.warn('月统计数据为空');
    ElMessage.warning('该月份暂无统计数据');
    trendData.value = []; // 清空数据
    return;
  }

  // 检查 dailyData 是否存在
  const dailyDataArray = data.dailyData || [];

  if (!dailyDataArray || dailyDataArray.length === 0) {
    console.warn('月统计 dailyData 为空:', dailyDataArray);
    ElMessage.warning('该月份暂无统计数据');
    trendData.value = []; // 清空数据
    return;
  }

  console.log('dailyData数组:', dailyDataArray); // 调试信息

  // 按日期分组，每天只取第一条数据（去重）
  const groupedByDate = {};
  dailyDataArray.forEach(item => {
    const dateKey = item.statDate; // 使用日期作为key
    if (!groupedByDate[dateKey]) {
      groupedByDate[dateKey] = item; // 每个日期只保留第一条数据
    }
  });

  console.log('按日期分组后的数据:', groupedByDate); // 调试信息

  // 转换为数组并按日期排序
  const uniqueDailyData = Object.values(groupedByDate).sort((a, b) => {
    return new Date(a.statDate) - new Date(b.statDate);
  });

  console.log('去重排序后的数据:', uniqueDailyData); // 调试信息

  // 设置月度趋势数据 - 显示当月每天的档案在架数量情况
  const processedData = uniqueDailyData.map(item => {
    const dateObj = new Date(item.statDate);
    const day = dateObj.getDate();

    return {
      time: day + '日',
      totalBorrows: item.totalBorrows || 0,
      totalReturns: item.totalReturns || 0,
      archivedCount: item.archivedCount || 0,
      pendingCount: item.pendingCount || 0,
      // 计算档案在架数量：归档数量 - 借出数量 + 归还数量
      archiveOnShelf: item.cabinetCount || 0,
    };
  });

  console.log('处理后的趋势数据:', processedData); // 调试信息
  trendData.value = processedData;

  // 强制触发响应式更新
  nextTick(() => {
    console.log('月统计数据更新完成，当前 trendData.value:', trendData.value);
  });
};

// 处理日统计数据（仅影响趋势图表）
const processDailyData = (data) => {
  console.log('处理日统计数据:', data); // 调试信息

  if (!data || !data.hourlyData) {
    console.warn('日统计数据为空');
    ElMessage.warning('该日期暂无统计数据');
    trendData.value = []; // 清空数据
    return;
  }

  // 设置趋势数据 - 显示当天24小时档案在架数量情况
  const processedData = data.hourlyData.map(item => ({
    time: new Date(item.createdAt).getHours() + ':00',
    totalBorrows: item.totalBorrows || 0,
    totalReturns: item.totalReturns || 0,
    archivedCount: item.archivedCount || 0,
    pendingCount: item.pendingCount || 0,
    // 计算档案在架数量：归档数量 - 借出数量 + 归还数量
    archiveOnShelf: (item.archivedCount || 0) - (item.totalLoans || 0) + (item.totalReturns || 0),
  }));

  console.log('处理后的日统计趋势数据:', processedData); // 调试信息
  trendData.value = processedData;
};

onMounted(() => {
  // 加载实时数据用于统计卡片和图表
  loadRealTimeData();
  // 加载趋势数据
  loadStatisticsData();
});
</script>

<style scoped lang="scss">
.home {
  blockquote {
    padding: 10px 20px;
    margin: 0 0 20px;
    font-size: 17.5px;
    border-left: 5px solid #eee;
  }

  hr {
    margin-top: 20px;
    margin-bottom: 20px;
    border: 0;
    border-top: 1px solid #eee;
  }

  .col-item {
    margin-bottom: 20px;
  }

  ul {
    padding: 0;
    margin: 0;
  }

  font-family: 'open sans',
  'Helvetica Neue',
  Helvetica,
  Arial,
  sans-serif;
  font-size: 13px;
  color: #676a6c;
  overflow-x: hidden;

  ul {
    list-style-type: none;
  }

  h4 {
    margin-top: 0px;
  }

  h2 {
    margin-top: 10px;
    font-size: 26px;
    font-weight: 100;
  }

  p {
    margin-top: 10px;

    b {
      font-weight: 700;
    }
  }

  .update-log {
    ol {
      display: block;
      list-style-type: decimal;
      margin-block-start: 1em;
      margin-block-end: 1em;
      margin-inline-start: 0;
      margin-inline-end: 0;
      padding-inline-start: 40px;
    }
  }

  :deep(.box) {
    padding: 20px;
    border-radius: 20px;

    .title,
    .header {
      display: flex;

      .el-icon {
        cursor: pointer;
      }
    }

    .title {
      align-items: center;
      flex: 1;
      font-size: 16px;
      font-weight: 600;
      color: #333;

    }

    .sub-title {
      margin-left: 10px;
      font-size: 12px;
      font-weight: 400;
      color: #999;
    }
  }

  .chart {
    position: relative;
    display: inline-block;
    width: 50%;
    height: 100%;
  }

  // 新增控制栏样式
  .control-bar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    padding: 0 10px;
  }

  // 时间范围选择器样式
  .time-range-selector {
    display: flex;
    align-items: center;
    gap: 10px;
  }

  // 时间指示器样式
  .time-indicator {
    margin-bottom: 20px;
    display: flex;
    justify-content: center;

    .el-tag {
      padding: 8px 16px;
      font-size: 14px;
      border-radius: 20px;
    }
  }

  // 调整按钮样式
  .control-button {
    padding: 12px 24px;
    font-size: 14px;
    border-radius: 6px;
    box-shadow: 0 2px 8px rgba(64, 158, 255, 0.2);
    transition: all 0.3s ease;

    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
    }
  }

  .component-control-dialog {
    border-radius: 8px;
  }

  .switch-container {
    padding: 20px 30px;
  }

  .el-form-item {
    margin-bottom: 18px;
    padding: 12px 0;
    border-bottom: 1px solid #ebeef5;
  }

  .el-form-item:last-child {
    border-bottom: none;
    margin-bottom: 0;
  }

  .el-form-item__label {
    font-size: 14px;
    color: #606266;
    font-weight: 500;
  }

  /* 调整开关大小 */
  .el-switch {
    transform: scale(1.1);
    margin-left: 10px;
  }

  /* 标题样式 */
  :deep(.el-dialog__title) {
    font-size: 16px;
    color: #303133;
    font-weight: 600;
  }

  // 位置统计对话框样式
  .location-stats-dialog {
    .location-stats-content {
      .stats-summary {
        display: flex;
        justify-content: space-around;
        margin-bottom: 20px;

        :deep(.el-statistic) {
          text-align: center;

          .el-statistic__head {
            font-size: 14px;
            color: #909399;
            margin-bottom: 8px;
          }

          .el-statistic__content {
            font-size: 24px;
            font-weight: bold;
            color: #409eff;
          }
        }
      }

      .location-table {
        .el-table {
          border-radius: 8px;
          overflow: hidden;
        }

        :deep(.el-table__header) {
          background-color: #f5f7fa;
        }

        :deep(.el-table__row:hover) {
          background-color: #f0f9ff;
        }
      }
    }

    .dialog-footer {
      display: flex;
      justify-content: flex-end;
      gap: 10px;
    }
  }

  // 位置统计按钮特殊样式
  .control-button.el-button--success {
    background: linear-gradient(135deg, #67c23a, #85ce61);
    border: none;
    color: white;

    &:hover {
      background: linear-gradient(135deg, #85ce61, #67c23a);
    }

    .el-icon {
      margin-right: 5px;
    }
  }
}
</style>
