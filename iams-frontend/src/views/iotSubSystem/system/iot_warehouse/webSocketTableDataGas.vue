<template>
  <div ref="chartRef" style="width: 1000px; height: 400px;"></div>
</template>

<script>
import * as echarts from 'echarts';

export default {
  name: 'PieChart',
  props:{
    PM25Level: {
      type: Number,
      required: true,
    },
    PM10Level: {
      type: Number,
      required: true,
    },
    TVOCLevel: {
      type: Number,
      required: true,
    },
    CO2Level: {
      type: Number,
      required: true,
    },
  },
  data() {
    return {
      chartInstance: null,
      PM25: this.PM25Level,
      PM10: this.PM10Level,
      TVOC: this.TVOCLevel,
      CO2: this.CO2Level,
      showCO2: true // 控制CO2是否显示
    };
  },
  watch: {
    // 监听 lasttem 属性的变化
    PM25Level(newVal, oldVal) {
      this.updateChart1(newVal);
      this.initChart();
    },
    PM10Level(newVal, oldVal) {
      this.updateChart2(newVal);
      this.initChart();
    },
    TVOCLevel(newVal, oldVal) {
      this.updateChart3(newVal);
      this.initChart();
    },
    CO2Level(newVal, oldVal) {
      this.updateChart4(newVal);
      this.initChart();
    },
  },
  methods: {
    updateChart1(PM25Level) {
      // 在这里更新氮气图表
      this.PM25=this.PM25Level;
    },
    updateChart2(PM10Level) {
      // 在这里更新氧气图表
      this.PM10=this.PM10Level;
    },
    updateChart3(TVOCLevel) {
      // 在这里更新氧气图表
      this.TVOC=this.TVOCLevel;
    },
    updateChart4(CO2Level) {
      // 在这里更新氧气图表
      this.CO2=this.CO2Level;
    },
    updateChart() {
      if (this.chartInstance) {
        this.chartInstance.setOption(this.option);
      }
    },
    initChart() {
      // this.chartInstance = echarts.init(this.$refs.chartRef); // 初始化 ECharts 实例
      // this.chartInstance.setOption(this.option); // 设置配置项
      this.chartInstance = echarts.init(this.$refs.chartRef);
      this.chartInstance.setOption(this.option);

      // 监听图例选择变化事件
      this.chartInstance.on('legendselectchanged', (params) => {
        if (params.name === 'CO2') {
          this.showCO2 = params.selected['CO2'];
          this.updateChart();
        }
      });

    }
  },
  mounted() {
    this.initChart();
    // 初始隐藏CO2
    // this.chartInstance.dispatchAction({
    //   type: 'legendUnSelect',
    //   name: 'CO2'
    // });
  },
  beforeDestroy() {
    if (this.chartInstance) {
      this.chartInstance.dispose(); // 在组件销毁前销毁 ECharts 实例
    }
  },
  computed: {
    option() {
      return {
        title: {
          text: '气体含量',
          left: 'center',
          textStyle: {
            fontSize: 25, // 增大标题字体大小
          },
        },
        tooltip: {
          trigger: 'item',
          formatter: '{a} <br/>{b} : {c}', // 显示提示框的百分比
        },
        legend: {
          orient: 'vertical',
          left: 'left'
        },
        series: [
          {
            name: '',
            type: 'pie',
            radius: ['10%', '70%'],
            avoidLabelOverlap: false,
            itemStyle: {
              borderRadius: 10,
              borderColor: '#fff',
              borderWidth: 2
            },
            label: {
              show: true,
              formatter: '{b} : {c}', // 在扇区上显示百分比
            },
            roseType: 'area',
            emphasis: {
              label: {
                show: true,
                fontSize: 40,
                fontWeight: 'bold'
              }
            },
            labelLine: {
              show: true
            },
            data: [
              { value: this.PM25, name: 'PM2.5' },
              { value: this.PM10, name: 'PM10' },
              { value: this.TVOC, name: 'TVOC' },
              {
                value: this.CO2,
                name: 'CO2',
                itemStyle: {
                  // 初始隐藏CO2
                  opacity: this.showCO2 ? 1 : 0
                },
                label: {
                  show: this.showCO2
                },
                labelLine: {
                  show: this.showCO2
                }
              },
            ]
          }
        ]
      };
    }
  }
};
</script>
