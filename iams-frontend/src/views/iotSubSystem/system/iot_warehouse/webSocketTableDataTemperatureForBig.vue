<template>
  <div class="item" ref="chartContainer"></div>
</template>

<script>
import * as echarts from 'echarts';

export default {
  name: "webSocketTableDataTemperature",
  props:{
    lasttem: {
      type: Number,
    }
  },
  data() {
    return {
      temp: this.lasttem,
    };
  },
  watch: {
    // 监听 lasttem 属性的变化
    lasttem(newVal, oldVal) {
      this.updateChart(newVal);
      this.temCharts();
    }
  },
  methods: {
    updateChart(temperature) {
      // 在这里更新温度图表
      this.temp=temperature;
    },
    temCharts() {
      var TP_value = this.temp;
      var kd = [];
      var Gradient = [];
      var leftColor = "";
      var showValue = "";
      var boxPosition = [45, 0];  // 温度显示位置
      var TP_txt = "";
      for (var i = 0, len = 135; i <= len; i++) {
        if (i < 10 || i > 130) {
          kd.push("");
        } else {
          if ((i - 10) % 20 === 0) {
            kd.push("-3");
          } else if ((i - 10) % 4 === 0) {
            kd.push("-1");
          } else {
            kd.push("");
          }
        }
      }

      if (TP_value > 25) {
        TP_txt = "温度偏高";
        Gradient.push(
          { offset: 0, color: "#93FE94" },
          { offset: 0.5, color: "#E4D225" },
          { offset: 1, color: "#E01F28" }
        );
      } else if (TP_value > -20) {
        TP_txt = "温度正常";
        Gradient.push(
          { offset: 0, color: "#93FE94" },
          { offset: 1, color: "#E4D225" }
        );
      } else {
        TP_txt = "温度偏低";
        Gradient.push({ offset: 1, color: "#93FE94" });
      }

      if (TP_value > 62) {
        showValue = 62;
      } else {
        if (TP_value < -60) {
          showValue = -60;
        } else {
          showValue = TP_value;
        }
      }

      if (TP_value < -10) {
        boxPosition = [65, -120];
      }

      leftColor = Gradient[Gradient.length - 1].color;

      const chart = this.chartInstance || echarts.init(this.$refs.chartContainer);
      this.chartInstance = chart;

      const options = {
        title: {
          text: "温度计",
          show: false
        },
        grid: {
          left: '12%',
          bottom: '20%',
          top: '21%',
          right: '-30%'
        },
        yAxis: [
          {
            show: false,
            data: [],
            min: 0,
            max: 135,
            axisLine: {
              show: false
            }
          },
          {
            show: false,
            min: 0,
            max: 50
          },
          {
            type: "category",
            position: "left",
            offset: -80,
            axisLabel: {
              fontSize: 10,
              color: "white"
            },
            axisLine: {
              show: false
            },
            axisTick: {
              show: false
            }
          }
        ],
        xAxis: [
          {
            show: false,
            min: -10,
            max: 80,
            data: []
          },
          {
            show: false,
            min: -10,
            max: 80,
            data: []
          },
          {
            show: false,
            min: -10,
            max: 80,
            data: []
          },
          {
            show: false,
            min: -5,
            max: 80
          }
        ],
        series: [
          {
            name: "条",
            type: "bar",
            xAxisIndex: 0,
            data: [
              {
                value: showValue + 70,
                label: {
                  normal: {
                    show: true,
                    position: boxPosition,
                    formatter:
                      "{back| " + TP_value + " }{unit|°C}\n{downTxt|" + TP_txt + "}",
                    rich: {
                      back: {
                        align: "center",
                        lineHeight: 50,
                        fontSize: 40,
                        fontFamily: "digifacewide",
                        color: leftColor
                      },
                      unit: {
                        fontFamily: "微软雅黑",
                        fontSize: 30,
                        lineHeight: 50,
                        color: leftColor
                      },
                      downTxt: {
                        lineHeight: 50,
                        fontSize: 25,
                        align: "center",
                        color: "rgb(72,109,99)"
                      }
                    }
                  }
                }
              }
            ],
            barWidth: 18,
            itemStyle: {
              normal: {
                color: new echarts.graphic.LinearGradient(0, 1, 0, 0, Gradient)
              }
            },
            z: 2
          },
          {
            name: "白框",
            type: "bar",
            xAxisIndex: 1,
            barGap: "-100%",
            data: [132],
            barWidth: 30,
            itemStyle: {
              normal: {
                color: "#0C2E6D",
                barBorderRadius: 50
              }
            },
            z: 1
          },
          {
            name: "外框",
            type: "bar",
            xAxisIndex: 2,
            barGap: "-100%",
            data: [135],
            barWidth: 38,
            itemStyle: {
              normal: {
                color: "#4577BA",
                barBorderRadius: 50
              }
            },
            z: 0
          },
          {
            name: "圆",
            type: "scatter",
            hoverAnimation: false,
            data: [0],
            xAxisIndex: 0,
            symbolSize: 48,
            itemStyle: {
              normal: {
                color: "#93FE94",
                opacity: 1
              }
            },
            z: 2
          },
          {
            name: "白圆",
            type: "scatter",
            hoverAnimation: false,
            data: [0],
            xAxisIndex: 1,
            symbolSize: 60,
            itemStyle: {
              normal: {
                color: "#0C2E6D",
                opacity: 1
              }
            },
            z: 1
          },
          {
            name: "外圆",
            type: "scatter",
            hoverAnimation: false,
            data: [0],
            xAxisIndex: 2,
            symbolSize: 70,
            itemStyle: {
              normal: {
                color: "#4577BA",
                opacity: 1
              }
            },
            z: 0
          },
          {
            name: "刻度",
            type: "bar",
            yAxisIndex: 0,
            xAxisIndex: 3,
            label: {
              normal: {
                show: true,
                position: "left",
                distance: 10,
                color: "white", // 刻度数值颜色
                fontSize: 14,
                formatter: function(params) {
                  if (params.dataIndex > 130 || params.dataIndex < 10) {
                    return "";
                  }
                  else {
                    if ((params.dataIndex - 10) % 20 === 0) {
                      return params.dataIndex - 70;
                    }
                    else {
                      return "";
                    }
                  }
                }
              }
            },
            barGap: "-100%",
            data: kd,
            barWidth: 1,
            itemStyle: {
              normal: {
                color: "white",  // 刻度颜色
                barBorderRadius: 120
              }
            },
            z: 0
          }
        ]
      };

      chart.setOption(options);
    }
  },
  mounted() {
    this.temCharts();
  },
};
</script>

<style scoped>
.item {
  width: 45%;
  height: 250px;
}
</style>
