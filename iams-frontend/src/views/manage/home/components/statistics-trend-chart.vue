<template>
    <div class="statistics-trend-chart box">
        <div class="header">
            <div class="title">
                {{ getChartTitle() }}
                <span class="sub-title">{{ getChartSubTitle() }}</span>
            </div>
            <div class="chart-controls">
                <el-radio-group v-model="chartType" size="small">
                    <el-radio-button label="line">折线图</el-radio-button>
                    <el-radio-button label="bar">柱状图</el-radio-button>
                </el-radio-group>
            </div>
        </div>
        <div class="chart-container">
            <div ref="chartRef" class="chart"></div>
            <!-- 添加数据为空时的提示 -->
            <div v-if="!hasData" class="empty-data">
                <p>{{ getEmptyDataText() }}</p>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, onMounted, watch, nextTick, computed, onBeforeUnmount } from 'vue';
import * as echarts from 'echarts';

const props = defineProps({
    trendData: {
        type: Array,
        default: () => []
    },
    timeRange: {
        type: String,
        default: 'hour'
    }
});

const chartRef = ref(null);
const chartType = ref('line');
let chart = null;

// 计算是否有数据
const hasData = computed(() => {
    const hasArrayData = Array.isArray(props.trendData) && props.trendData.length > 0;
    console.log('图表数据检查:', {
        isArray: Array.isArray(props.trendData),
        length: props.trendData.length,
        hasData: hasArrayData,
        data: props.trendData
    });
    return hasArrayData;
});

const getChartTitle = () => {
    switch (props.timeRange) {
        case 'day':
            return '日统计趋势';
        case 'month':
            return '月统计趋势';
        default:
            return '统计趋势';
    }
};

const getChartSubTitle = () => {
    switch (props.timeRange) {
        case 'day':
            return '按小时统计';
        case 'month':
            return '按日统计';
        default:
            return '实时数据';
    }
};

const getEmptyDataText = () => {
    switch (props.timeRange) {
        case 'day':
            return '该日期暂无统计数据';
        case 'month':
            return '该月份暂无统计数据';
        default:
            return '实时模式无趋势数据';
    }
};

const initChart = () => {
    if (!chartRef.value) {
        console.log('图表容器不存在');
        return;
    }

    console.log('初始化图表容器');
    chart = echarts.init(chartRef.value);

    // 初始化后立即尝试更新图表
    updateChart();
};

const updateChart = () => {
    if (!chart) {
        console.log('图表实例不存在，跳过更新');
        return;
    }

    console.log('更新图表，当前数据:', {
        trendData: props.trendData,
        hasData: hasData.value,
        timeRange: props.timeRange
    });

    // 如果没有数据，清空图表但不返回
    if (!hasData.value) {
        console.log('无数据，清空图表');
        chart.clear();
        return;
    }

    try {
        const times = props.trendData.map(item => item.time);
        const borrowsData = props.trendData.map(item => item.totalBorrows || 0);
        const returnsData = props.trendData.map(item => item.totalReturns || 0);
        const archivedData = props.trendData.map(item => item.archivedCount || 0);
        const pendingData = props.trendData.map(item => item.pendingCount || 0);
        const archiveOnShelfData = props.trendData.map(item => item.archiveOnShelf || 0);

        console.log('图表数据处理结果:', {
            times,
            borrowsData,
            returnsData,
            archivedData,
            pendingData,
            archiveOnShelfData
        });

        const option = {
            tooltip: {
                trigger: 'axis',
                axisPointer: {
                    type: 'cross',
                    label: {
                        backgroundColor: '#6a7985'
                    }
                },
                formatter: function (params) {
                    let html = `<div style="margin-bottom: 8px;"><strong>${params[0].name}</strong></div>`;
                    params.forEach(param => {
                        html += `
                <div style="margin-bottom: 4px;">
                  ${param.marker} ${param.seriesName}: <strong>${param.value}</strong>
                </div>
              `;
                    });
                    return html;
                }
            },
            legend: {
                data: ['总借阅', '总归还', '归档数量', '待处理', '档案在架'],
                top: '5%',
                left: 'center'
            },
            grid: {
                left: '3%',
                right: '4%',
                bottom: '8%',
                top: '15%',
                containLabel: true
            },
            xAxis: {
                type: 'category',
                boundaryGap: chartType.value === 'bar',
                data: times,
                axisLabel: {
                    rotate: props.timeRange === 'month' ? 45 : 0,
                    fontSize: 12
                }
            },
            yAxis: [
                {
                    type: 'value',
                    name: '数量',
                    position: 'left',
                    axisLine: {
                        show: true,
                        lineStyle: {
                            color: '#409EFF'
                        }
                    },
                    axisLabel: {
                        formatter: '{value}'
                    }
                }
            ],
            series: [
                {
                    name: '总借阅',
                    type: chartType.value,
                    data: borrowsData,
                    smooth: true,
                    lineStyle: {
                        width: 3
                    },
                    itemStyle: {
                        color: '#409EFF'
                    },
                    areaStyle: chartType.value === 'line' ? {
                        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                            { offset: 0, color: 'rgba(64, 158, 255, 0.3)' },
                            { offset: 1, color: 'rgba(64, 158, 255, 0.1)' }
                        ])
                    } : null
                },
                {
                    name: '总归还',
                    type: chartType.value,
                    data: returnsData,
                    smooth: true,
                    lineStyle: {
                        width: 3
                    },
                    itemStyle: {
                        color: '#67C23A'
                    },
                    areaStyle: chartType.value === 'line' ? {
                        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                            { offset: 0, color: 'rgba(103, 194, 58, 0.3)' },
                            { offset: 1, color: 'rgba(103, 194, 58, 0.1)' }
                        ])
                    } : null
                },
                {
                    name: '归档数量',
                    type: chartType.value,
                    data: archivedData,
                    smooth: true,
                    lineStyle: {
                        width: 3
                    },
                    itemStyle: {
                        color: '#E6A23C'
                    },
                    areaStyle: chartType.value === 'line' ? {
                        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                            { offset: 0, color: 'rgba(230, 162, 60, 0.3)' },
                            { offset: 1, color: 'rgba(230, 162, 60, 0.1)' }
                        ])
                    } : null
                },
                {
                    name: '待处理',
                    type: chartType.value,
                    data: pendingData,
                    smooth: true,
                    lineStyle: {
                        width: 3
                    },
                    itemStyle: {
                        color: '#F56C6C'
                    },
                    areaStyle: chartType.value === 'line' ? {
                        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                            { offset: 0, color: 'rgba(245, 108, 108, 0.3)' },
                            { offset: 1, color: 'rgba(245, 108, 108, 0.1)' }
                        ])
                    } : null
                },
                {
                    name: '档案在架',
                    type: chartType.value,
                    data: archiveOnShelfData,
                    smooth: true,
                    lineStyle: {
                        width: 4
                    },
                    itemStyle: {
                        color: '#909399'
                    },
                    areaStyle: chartType.value === 'line' ? {
                        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                            { offset: 0, color: 'rgba(144, 147, 153, 0.3)' },
                            { offset: 1, color: 'rgba(144, 147, 153, 0.1)' }
                        ])
                    } : null
                }
            ]
        };

        console.log('设置图表配置:', option);
        chart.setOption(option);
        console.log('图表更新完成');

    } catch (error) {
        console.error('更新图表时出错:', error);
    }
};

const resizeChart = () => {
    if (chart) {
        chart.resize();
    }
};

// 监听数据变化
watch(() => props.trendData, (newVal) => {
    console.log('监听到 trendData 变化:', newVal);
    nextTick(() => {
        updateChart();
    });
}, { deep: true, immediate: true });

// 监听图表类型变化
watch(chartType, (newType) => {
    console.log('图表类型变化为:', newType);
    updateChart();
});

// 监听时间范围变化
watch(() => props.timeRange, (newRange) => {
    console.log('时间范围变化为:', newRange);
    nextTick(() => {
        updateChart();
    });
});

onMounted(() => {
    console.log('图表组件挂载，初始数据:', props.trendData);
    nextTick(() => {
        initChart();
    });

    // 监听窗口大小变化
    window.addEventListener('resize', resizeChart);
});

// 组件卸载时清理
onBeforeUnmount(() => {
    console.log('图表组件卸载');
    if (chart) {
        chart.dispose();
        chart = null;
    }
    window.removeEventListener('resize', resizeChart);
});
</script>

<style scoped lang="scss">
.statistics-trend-chart {
    background: #fff;
    border-radius: 20px;
    padding: 20px;
    margin-bottom: 20px;

    .header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 20px;

        .title {
            font-size: 18px;
            font-weight: 600;
            color: #333;
            display: flex;
            align-items: center;

            .sub-title {
                margin-left: 10px;
                font-size: 12px;
                font-weight: 400;
                color: #999;
                background: #f5f7fa;
                padding: 2px 8px;
                border-radius: 10px;
            }
        }

        .chart-controls {
            display: flex;
            gap: 10px;
        }
    }

    .chart-container {
        position: relative;

        .chart {
            width: 100%;
            height: 400px;
        }

        .empty-data {
            position: absolute;
            top: 50%;
            left: 50%;
            transform: translate(-50%, -50%);
            text-align: center;
            color: #909399;
            font-size: 14px;

            p {
                margin: 0;
                padding: 20px;
                background: #f5f7fa;
                border-radius: 8px;
                border: 1px dashed #dcdfe6;
            }
        }
    }
}

// 响应式设计
@media (max-width: 768px) {
    .statistics-trend-chart {
        .header {
            flex-direction: column;
            gap: 10px;
            align-items: flex-start;
        }

        .chart-container .chart {
            height: 300px;
        }
    }
}
</style>