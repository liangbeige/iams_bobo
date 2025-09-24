<template>
  <div ref="chartRef" class="chart-container"></div>
</template>

<script setup>
import * as echarts from 'echarts';
import { onMounted, onUnmounted, ref, watch, nextTick } from 'vue';

const props = defineProps({
  chartOption: {
    type: Array,
    default: () => ([]),
  },
});

const chartRef = ref(null);
let chartInstance = null;

// 基础配置
const baseOption = {
  tooltip: {
    trigger: 'item',
    formatter: '{b}<br />总占比：{d}%',
    backgroundColor: '#FFFFFF',
    borderColor: '#eef3f8',
    borderWidth: 1,
    textStyle: {
      color: '#8C8C8C',
    },
    padding: 12,
  },
  series: [
    {
      type: 'pie',
      roseType: 'radius',
      radius: [33, 100],
      center: ['50%', '50%'],
      animationEasing: 'cubicInOut',
      animationDuration: 2600,
      label: {
        formatter: ['{name|{b}}', '{percentage|{d}%}'].join('\n'),
        rich: {
          name: {
            color: '#333333',
            fontWeight: 'bolder',
            align: 'left',
          },
          percentage: {
            color: '#000000',
            lineHeight: 15,
            align: 'left',
          },
        },
      },
      labelLine: {
        lineStyle: {
          color: '#BFBFBF',
        },
      },
    },
  ],
  color: ['#85A7FF', '#99EBBD', '#FFB18B', '#C6EBFF', '#BECCE6'],
};

// 初始化图表
const initChart = () => {
  if (!chartInstance && chartRef.value) {
    chartInstance = echarts.init(chartRef.value);
    window.addEventListener('resize', handleResize);
  }
};

// 更新图表配置
const updateChart = () => {
  if (!chartInstance) return;

  const fullOption = {
    ...baseOption,
    series: [
      {
        ...baseOption.series[0],
        data: props.chartOption || [],
      },
    ],
  };

  chartInstance.setOption(fullOption, true);
};

// 窗口resize处理
const handleResize = () => {
  chartInstance?.resize();
};

onMounted(() => {
  nextTick(() => {
    initChart();
    updateChart();
  });
});

// 监听数据变化
watch(
  () => props.chartOption,
  (newVal) => {
    if (newVal) {
      updateChart();
    }
  },
  { deep: true }
);

// 销毁实例
onUnmounted(() => {
  if (chartInstance) {
    chartInstance.dispose();
    chartInstance = null;
    window.removeEventListener('resize', handleResize);
  }
});
</script>

<style scoped>
.chart-container {
  width: 100%;
  height: 300px; /* 必须设置明确高度 */
}
</style>