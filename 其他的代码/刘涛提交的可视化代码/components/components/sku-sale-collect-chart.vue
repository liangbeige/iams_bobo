<template>
  <div class="box sku-sale-collect">
    <div class="header">
      <div class="title">
        借阅数据
        <span class="sub-title">{{ datePickerFormat[0] }} ~ {{ datePickerFormat[1] }}</span>
      </div>
      <common-week-month-year @handleChange="handleRadioGroupSelChange" />
    </div>

    <div v-if="isLoading" class="loading">数据加载中...</div>
    <div v-else class="charts">
      <sku-sale-collect-line-chart
          id="amount-collect"
          title="借阅量趋势图"
          :chart-option="lineChartOption"
      />
      <sku-sale-collect-bar-chart
          id="region-collect"
          title="档案类型分布"
          :chart-option="barChartOption"
      />
    </div>
  </div>
</template>

<script setup>
import * as echarts from 'echarts';
import { onMounted, ref } from 'vue';
import { getArchiveClassCount, getBrowseCount } from '@/api/chart.js';
import dayjs from 'dayjs';
import CommonWeekMonthYear from '@/components/week-month-year/index.vue';
import SkuSaleCollectLineChart from './sku-sale-collect-line-chart.vue';
import SkuSaleCollectBarChart from './sku-sale-collect-bar-chart.vue';

// 定义变量
const datePickerFormat = ref([dayjs().subtract(1, 'week').format('YYYY.MM.DD'), dayjs().format('YYYY.MM.DD')]);
const radioGroupSel = ref('week');
const isLoading = ref(false);

const lineChartOption = ref({
  xAxisData: [],
  seriesData: [],
  yAxisName: '借阅量',
});

const barChartOption = ref({
  xAxisData: ['电子档', '实体档'],
  seriesData: [],
  yAxisName: '单位：份',
});

// 获取档案数据
const fetchArchiveData = async () => {
  try {
    const response = await getArchiveClassCount();
    if (response?.data) {
      barChartOption.value.seriesData = response.data;
    } else {
      console.error('响应数据格式不正确', response);
    }
  } catch (error) {
    console.error('获取档案数据失败', error);
  }
};

// 获取借阅数据
const fetchBrowseData = async () => {
  try {
    isLoading.value = true;

    const [startDate, endDate] = datePickerFormat.value.map(date =>
        dayjs(date, 'YYYY.MM.DD').format('YYYY-MM-DD')
    );

    const response = await getBrowseCount({ start_date: startDate, end_date: endDate });
    if (response?.data) {
      const { xAxisData, seriesData } = response.data;
      lineChartOption.value.xAxisData = xAxisData;
      lineChartOption.value.seriesData = seriesData;
    } else {
      console.error('响应数据格式不正确', response);
    }
  } catch (error) {
    console.error('获取借阅数据失败', error);
  } finally {
    isLoading.value = false;
  }
};

// 切换时间范围
const handleRadioGroupSelChange = (selectData) => {
  radioGroupSel.value = selectData;
  const startFormat = dayjs()
      .startOf(radioGroupSel.value)
      .subtract(1, radioGroupSel.value)
      .format('YYYY.MM.DD');
  const endFormat = dayjs().endOf('day').format('YYYY.MM.DD');

  datePickerFormat.value = [startFormat, endFormat];
  fetchBrowseData();
};

// 页面加载时初始化数据
onMounted(async () => {
  await fetchArchiveData();
  await fetchBrowseData();
});
</script>

<style lang="scss" scoped>
.sku-sale-collect {
  display: flex;
  flex-direction: column;
  height: calc((100vh - 120px) * 0.4 - 20px);
  min-height: 352px;
  margin-top: 20px;
  background: #fff;
  border-radius: 20px;

  .loading {
    text-align: center;
    font-size: 16px;
    color: #888;
  }

  .charts {
    flex: 1;
    display: flex;
  }
}
</style>
