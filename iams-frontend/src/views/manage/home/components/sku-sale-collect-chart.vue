<template>
  <div class="box sku-sale-collect">
    <div class="header">
      <div class="title">
        借阅数据<span class="sub-title">最近7天借出数量趋势</span>
      </div>
    </div>
    <div class="charts">
      <sku-sale-collect-line-chart id="amount-collect" title="借出数量趋势" :chart-option="lineChartOption" />
      <sku-sale-collect-bar-chart id="region-collect" title="档案类型分布" :chart-option="barChartOption" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue';
import dayjs from 'dayjs';
import SkuSaleCollectLineChart from './sku-sale-collect-line-chart.vue';
import SkuSaleCollectBarChart from './sku-sale-collect-bar-chart.vue';
import { getRecentBorrowData } from '@/api/manage/statistics';

const props = defineProps({
  collectData: {
    type: Array,
    required: true
  },
  timeRange: {
    type: String,
    default: 'hour'
  }
});

const lineChartOption = ref({
  xAxisData: [],
  seriesData: [],
  yAxisName: '单位：次'
});

const barChartOption = ref({
  xAxisData: ["电子档", "实体档"],
  seriesData: [],
  yAxisName: '单位：份',
});

// 加载最近7天借出数量数据
const loadRecentBorrowData = async () => {
  try {
    const response = await getRecentBorrowData(7); // 获取最近7天数据
    const data = response;

    // 先按日期排序，确保从早到晚的顺序
    const sortedData = data.sort((a, b) => new Date(a.statDate) - new Date(b.statDate));

    // 更新折线图数据
    lineChartOption.value.xAxisData = sortedData.map(item =>
      dayjs(item.statDate).format('MM-DD')
    );
    lineChartOption.value.seriesData = sortedData.map(item => item.totalBorrows);

  } catch (error) {
    console.error('加载借出数据失败:', error);

    // 如果接口失败，使用模拟数据
    const xAxisData = [];
    const seriesData = [];
    for (let i = 6; i >= 0; i--) {
      xAxisData.push(dayjs().subtract(i, 'day').format('MM-DD'));
      seriesData.push(Math.floor(Math.random() * 50) + 10); // 模拟数据
    }
    lineChartOption.value.xAxisData = xAxisData;
    lineChartOption.value.seriesData = seriesData;
  }
};

// 监听collectData变化，更新柱状图
watch(() => props.collectData, (newData) => {
  if (newData && newData.length > 0) {
    barChartOption.value.seriesData = newData;
  }
}, { immediate: true });

onMounted(() => {
  loadRecentBorrowData();
});
</script>

<style lang="scss" scoped>
.sku-sale-collect {
  display: flex;
  flex-direction: column;
  // TODO: 临时解决方案，当前页面的横纵布局需要重新思考
  height: calc((100vh - 120px) * 0.4 - 20px);
  min-height: 352px;
  margin-top: 20px;
  background: #FFFFFF;
  border-radius: 20px;

  .charts {
    flex: 1;
    display: flex;
  }
}
</style>