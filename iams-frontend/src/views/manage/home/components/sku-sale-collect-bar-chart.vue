<template>
  <div class="chart-container">
    <div ref="chartRef" class="monitorContainer" style="width: 100%; height: 350px;"></div>
  </div>
</template>

<script setup>
import * as echarts from 'echarts';
import { ref, watch, onMounted, onUnmounted, nextTick } from 'vue';

const props = defineProps({
  chartOption: {
    type: Object,
    default: () => ({
      xAxisData: [],
      seriesData: [],
      yAxisName: '',
      legendData: []
    }),
  },
});

const chartRef = ref(null);
let chartInstance = null;

// 样式配置
const chartStyle = {
  axisColor: '#D9D9D9',
  axisTextColor: '#999999',
  barColor: '#91B0FF',
  grid: {
    left: 40,
    right: 30,
    top: 60,
    bottom: 40,  // 增加底部空间
    containLabel: true
  }
};

// 基础配置
const baseOption = {
  title: {
    text: '档案类型分布',
    left: 'center',
    top: 10,
    textStyle: {
      color: '#333',
      fontWeight: 'bolder',
      fontSize: 14
    }
  },
  legend: {
    bottom: 10,
    itemGap: 20,
    data: []
  },
  xAxis: {
    type: 'category',
    axisLine: { lineStyle: { color: chartStyle.axisColor } },
    axisTick: { show: true, lineStyle: { color: chartStyle.axisColor } },
    axisLabel: {
      color: chartStyle.axisTextColor,
      interval: 0,
      formatter: (value) => {
        return value.length > 3 ? value.slice(0, 3) + '...' : value;
      }
    }
  },
  yAxis: {
    type: 'value',
    name: '',
    nameTextStyle: { color: chartStyle.axisTextColor },
    axisLine: { show: false },
    axisTick: { show: false },
    splitLine: { lineStyle: { color: chartStyle.axisColor } }
  },
  series: [{
    type: 'bar',
    itemStyle: {
      color: chartStyle.barColor,
      borderRadius: [4, 4, 0, 0]
    },
    barWidth: 14
  }],
  tooltip: {
    trigger: 'axis',
    formatter: '数量：{c}<br />{b}',
    backgroundColor: '#fff',
    borderColor: '#eef3f8',
    borderWidth: 1,
    textStyle: { color: '#8C8C8C' },
    padding: 12
  }
};

// 初始化图表
const initChart = () => {
  if (!chartInstance && chartRef.value) {
    chartInstance = echarts.init(chartRef.value);
    window.addEventListener('resize', handleResize);
  }
};

// 更新图表
const updateChart = () => {
  if (!chartInstance) return;

  const fullOption = {
    ...baseOption,
    xAxis: {
      ...baseOption.xAxis,
      data: props.chartOption.xAxisData
    },
    yAxis: {
      ...baseOption.yAxis,
      name: props.chartOption.yAxisName
    },
    legend: {
      ...baseOption.legend,
      data: props.chartOption.legendData
    },
    series: [{
      ...baseOption.series[0],
      data: props.chartOption.seriesData
    }]
  };

  chartInstance.setOption(fullOption, true);
};

// 窗口resize处理
const handleResize = () => {
  chartInstance?.resize();
};

onMounted(() => {
  initChart();
  updateChart();
});

onUnmounted(() => {
  if (chartInstance) {
    window.removeEventListener('resize', handleResize);
    chartInstance.dispose();
    chartInstance = null;
  }
});

// 监听数据变化
watch(
  () => props.chartOption,
  () => {
    nextTick(updateChart);
  },
  { deep: true }
);
</script>

<style scoped>
.chart-container {
  width: 100%;
  height: 400px; /* 确保容器有明确高度 */
}

.monitorContainer {
  width: 100%;
  height: 100%;
}
</style>