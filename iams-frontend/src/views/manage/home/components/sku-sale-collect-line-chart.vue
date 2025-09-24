<template>
  <div class="chart">
    <div ref="chartRef" class="monitorContainer" :class="{ 'show': hasData, 'hidden': !hasData }"
      style="width: 100%; height: 300px" />
    <empty-data-chart :is-empty="!hasData" />
  </div>
</template>

<script setup>
import * as echarts from 'echarts';
import { ref, watch, onMounted, onUnmounted, nextTick, computed } from 'vue';
import EmptyDataChart from '@/components/empty-data-chart/index.vue';

const props = defineProps({
  chartOption: {
    type: Object,
    default: () => ({
      xAxisData: [],
      seriesData: [],
      yAxisName: '单位：次'
    }),
  },
});

const chartRef = ref(null);
const chartInstance = ref(null);

// 修复：更严格的数据检查
const hasData = computed(() => {
  const xData = props.chartOption?.xAxisData;
  const sData = props.chartOption?.seriesData;
  return Array.isArray(xData) && Array.isArray(sData) && xData.length > 0 && sData.length > 0;
});

// 样式配置
const chartStyle = {
  axisColor1: '#D9D9D9',
  axisColor2: '#999999',
  lineColor: '#FF5757',
  areaGradient: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
    { offset: 0, color: 'rgba(255, 87, 87, 0.5)' },
    { offset: 0.3, color: 'rgba(255, 169, 169, 0.3)' },
    { offset: 1, color: 'rgba(255, 169, 169, 0)' }
  ])
};

// 基础配置
const baseOption = {
  title: {
    text: '借出数量趋势', // 修改标题
    left: 'center',
    top: 10,
    textStyle: {
      color: '#333',
      fontWeight: 'bolder',
      fontSize: 14
    }
  },
  grid: {
    left: 30,
    right: 33,
    top: 60,
    bottom: 5,
    containLabel: true
  },
  tooltip: {
    trigger: 'axis',
    formatter: '借出数量：{c}<br />{b}', // 修改提示文本
    backgroundColor: '#FFF',
    borderColor: '#eef3f8',
    borderWidth: 1,
    textStyle: {
      color: '#8C8C8C'
    },
    padding: 12,
    axisPointer: {
      lineStyle: {
        color: chartStyle.axisColor1,
        width: 2
      }
    }
  },
  xAxis: {
    type: 'category',
    axisLine: { lineStyle: { color: chartStyle.axisColor1 } },
    axisTick: { lineStyle: { color: chartStyle.axisColor1 } },
    axisLabel: { color: chartStyle.axisColor2 },
    boundaryGap: false
  },
  yAxis: {
    type: 'value',
    axisLine: { show: false },
    axisTick: { show: false },
    axisLabel: { color: chartStyle.axisColor2 },
    splitLine: { lineStyle: { color: chartStyle.axisColor1 } }
  }
};

// 初始化图表 - 移除hasData限制
const initChart = () => {
  if (!chartInstance.value && chartRef.value) {
    console.log('初始化图表'); // 调试信息
    chartInstance.value = echarts.init(chartRef.value);
    window.addEventListener('resize', handleResize);
  }
};

// 更新图表 - 移除hasData限制，让图表始终能更新
const updateChart = () => {
  if (!chartInstance.value) {
    console.log('图表实例不存在');
    return;
  }

  console.log('更新图表数据:', props.chartOption); // 调试信息
  console.log('hasData:', hasData.value); // 调试信息

  // 即使没有数据也要清空图表
  const fullOption = {
    ...baseOption,
    xAxis: {
      ...baseOption.xAxis,
      data: props.chartOption.xAxisData || []
    },
    yAxis: {
      ...baseOption.yAxis,
      name: props.chartOption.yAxisName || '单位：次'
    },
    series: [{
      type: 'line',
      data: props.chartOption.seriesData || [],
      smooth: true,
      lineStyle: {
        color: chartStyle.lineColor,
        width: 3,
        shadowColor: 'rgba(222, 115, 127, 0.23)',
        shadowBlur: 10,
        shadowOffsetY: 8
      },
      areaStyle: {
        color: chartStyle.areaGradient
      }
    }]
  };

  chartInstance.value.setOption(fullOption, true);
  console.log('图表更新完成'); // 调试信息
};

// 窗口resize处理
const handleResize = () => {
  chartInstance.value?.resize();
};

// 生命周期
onMounted(() => {
  console.log('组件挂载，chartOption:', props.chartOption); // 调试信息
  nextTick(() => {
    initChart();
    // 延迟更新，确保初始化完成
    setTimeout(() => {
      updateChart();
    }, 100);
  });
});

onUnmounted(() => {
  if (chartInstance.value) {
    window.removeEventListener('resize', handleResize);
    chartInstance.value.dispose();
    chartInstance.value = null;
  }
});

// 监听数据变化
watch(
  () => props.chartOption,
  (newVal) => {
    console.log('chartOption变化:', newVal); // 调试信息
    nextTick(() => {
      if (chartInstance.value) {
        updateChart();
        handleResize();
      } else {
        // 如果图表实例不存在，重新初始化
        initChart();
        setTimeout(() => {
          updateChart();
        }, 100);
      }
    });
  },
  { deep: true, immediate: true }
);
</script>

<style scoped>
.chart {
  position: relative;
  width: 100%;
  height: 100%;
}

.monitorContainer {
  width: 100%;
  height: 400px;
  /* 必须设置明确高度 */
}

.hidden {
  display: none;
}

.show {
  display: block;
}
</style>