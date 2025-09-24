<template>
  <div class="info-panel" style="height: 1500px;overflow-y: auto">
    <!-- 仓库选择器 -->
    <el-form ref="queryForm" :inline="true" label-width="80px">
      <el-row :gutter="20">
        <el-form-item label="选择区号" prop="selectedArea">
          <el-select
            v-model="selectedArea"
            placeholder="选择区号"
            @change="handleAreaChange"
          >
            <el-option
              v-for="area in infoList"
              :key="area.gdlNo"
              :label="`${area.gdlNo}: ${area.gdlName}`"
              :value="area.gdlNo"
            >
              <span>{{ area.gdlNo }}: {{ area.gdlName}}</span>
            </el-option>
          </el-select>
        </el-form-item>

        <!-- 时间选择器 -->
        <el-form-item label="选择日期" prop="selectedDate">
          <el-date-picker
            v-model="selectedDate"
            type="date"
            placeholder="选择日期"
            value-format="yyyy-MM-dd"
            @change="fetchDailyData"
          />
        </el-form-item>
      </el-row>
    </el-form>

    <!-- 温度仪表盘 -->
    <div class="temperature-gauge">
      <div ref="temperatureChart" class="chart"></div>
    </div>

    <!-- 湿度折线图 -->
    <div class="temperature-gauge">
      <div ref="humidityChart" class="chart-item"></div>
    </div>

    <!-- 气体含量图表 -->
    <div class="temperature-gauge">
      <div ref="gasChart" class="chart-item"></div>
    </div>
  </div>
</template>

<script>
import * as echarts from 'echarts';
import { listInfo } from "@/api/system/GDIpInfo.js";
import axios from "axios";


export default {
  name: 'WarehouseInfoPanel',
  data() {
    return {
      // 区号相关数据
      infoList: [],
      selectedArea: '',

      // 时间相关
      selectedDate: new Date().toISOString().split('T')[0],

      // 图表实例
      temperatureChart: null,
      humidityChart: null,
      gasChart: null,

      // 存储当天数据
      dailyData: []
    };
  },
  mounted() {
    this.getIpList();
    this.initCharts();
    this.fetchDailyData(); // 初始加载当天数据
  },
  methods: {
    /** 查询固定列IP信息管理列表 */
    getIpList() {
      listInfo().then(response => {
        this.infoList = response.rows;
        if (this.infoList.length > 0) {
          this.selectedArea = this.infoList[0].gdlNo;
        }
        this.fetchDailyData(); // 初始加载当天数据
      });
    },

    /** 初始化图表 */
    initCharts() {
      this.temperatureChart = echarts.init(this.$refs.temperatureChart);
      this.humidityChart = echarts.init(this.$refs.humidityChart);
      this.gasChart = echarts.init(this.$refs.gasChart);

      window.addEventListener('resize', () => {
        this.temperatureChart.resize();
        this.humidityChart.resize();
        this.gasChart.resize();
      });
    },

    /** 处理区号变更 */
    handleAreaChange(areaNo) {
      this.selectedArea = areaNo;
      this.fetchDailyData();
    },

    /** 获取某天的数据 */
    fetchDailyData() {
      if (!this.selectedArea || !this.selectedDate) return;

      // 模拟API请求，实际项目中替换为真实的API调用
      this.mockFetchDailyData().then(response => {
        this.dailyData = response.data;
        console.log('dailyData', this.dailyData)
        this.updateCharts();
      });

      //获取区间的IP和port
      const gdlinfo = this.infoList.find(item => item.gdlNo === this.selectedArea);
      const date = new Date(this.selectedDate)
      // 请求api 获取数据
      axios.post('http://'+ gdlinfo.gdlIp +':'+ gdlinfo.gdlPort +'/MjjWebApi',{
        year: date.getFullYear(),
        month: date.getMonth()+1,
        day: date.getDate(),
      })
        .then(response => {
          this.dailyData = response.data; // 将服务器返回的数据存储到 data 中
        })
        .catch(error => {
          console.error('请求失败:', error);
        });
    },

    /** 模拟获取每日数据 */
    mockFetchDailyData() {
      return new Promise(resolve => {
        // 生成24小时的模拟数据
        const mockData = [];
        const dateStr = this.selectedDate;

        for (let hour = 0; hour < 24; hour++) {
          const timeStr = `${hour.toString().padStart(2, '0')}:00`;
          const timestamp = `${dateStr} ${timeStr}`;

          mockData.push({
            QUNO: this.selectedArea,
            Temp: 20 + Math.sin(hour / 3) * 5 + Math.random() * 2, // 温度波动
            Hum: 50 + Math.cos(hour / 4) * 10 + Math.random() * 5, // 湿度波动
            PM2_5: 10 + Math.random() * 15,
            PM10: 20 + Math.random() * 20,
            TVOC: 5 + Math.random() * 10,
            CO2: 400 + Math.random() * 200,
            Time: timestamp
          });
        }
        resolve({
          data: mockData
        });
      });
    },

    /** 更新所有图表 */
    updateCharts() {
      this.updateTemperatureChart();
      this.updateHumidityChart();
      this.updateGasChart();
    },

    updateTemperatureChart() {
      const option = {
        title: {
          text: `${this.selectedDate} 温度查看 (${this.selectedArea})`,
          left: 'center',
        },
        tooltip: {
          trigger: 'axis',
          formatter: params => {
            const time = params[0].name;
            const temp = params[0].value;
            return `时间: ${time}<br/>温度: ${temp.toFixed(1)}°C`;
          },
        },
        xAxis: {
          type: 'category',
          data: this.dailyData.map(item => item.Time.split(' ')[1]), // 显示时间部分
          axisLabel: {
            rotate: 30
          }
        },
        yAxis: {
          type: 'value',
          name: '温度 (°C)',
          min: value => Math.max(0, Math.floor(value.min - 5)),
          max: value => Math.ceil(value.max + 5)
        },
        series: [
          {
            name: '温度',
            type: 'line',
            smooth: true,
            data: this.dailyData.map(item => item.Temp),
            itemStyle: {
              color: '#c23531'
            },
            lineStyle: {
              width: 3
            },
            markPoint: {
              data: [
                { type: 'max', name: '最大值' },
                { type: 'min', name: '最小值' }
              ]
            },
            markLine: {
              data: [{ type: 'average', name: '平均值' }]
            }
          }
        ],
        grid: {
          left: '3%',
          right: '4%',
          bottom: '10%',
          containLabel: true
        }
      };
      this.temperatureChart.setOption(option);
    },

    updateHumidityChart() {
      const option = {
        title: {
          text: `${this.selectedDate} 湿度查看 (${this.selectedArea})`,
          left: 'center',
        },
        tooltip: {
          trigger: 'axis',
          formatter: params => {
            const time = params[0].name;
            const hum = params[0].value;
            return `时间: ${time}<br/>湿度: ${hum.toFixed(1)}%`;
          },
        },
        xAxis: {
          type: 'category',
          data: this.dailyData.map(item => item.Time.split(' ')[1]), // 显示时间部分
          axisLabel: {
            rotate: 30
          }
        },
        yAxis: {
          type: 'value',
          name: '湿度 (%)',
          min: 0,
          max: 100
        },
        series: [
          {
            name: '湿度',
            type: 'line',
            smooth: true,
            data: this.dailyData.map(item => item.Hum),
            itemStyle: {
              color: '#2f4554'
            },
            lineStyle: {
              width: 3
            },
            markPoint: {
              data: [
                { type: 'max', name: '最大值' },
                { type: 'min', name: '最小值' }
              ]
            },
            markLine: {
              data: [{ type: 'average', name: '平均值' }]
            }
          }
        ],
        grid: {
          left: '3%',
          right: '4%',
          bottom: '10%',
          containLabel: true
        }
      };
      this.humidityChart.setOption(option);
    },

    updateGasChart() {
      const option = {
        title: {
          text: `${this.selectedDate} 气体含量查看 (${this.selectedArea})`,
          left: 'center',
        },
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'shadow'
          },
          formatter: params => {
            const time = params[0].name;
            let result = `时间: ${time}<br/>`;
            params.forEach(param => {
              result += `${param.seriesName}: ${param.value.toFixed(1)}${param.seriesIndex < 2 ? 'μg/m³' : 'ppm'}<br/>`;
            });
            return result;
          }
        },
        legend: {
          data: ['PM2.5', 'PM10', 'TVOC', 'CO2'],
          top: 30
        },
        xAxis: {
          type: 'category',
          data: this.dailyData.map(item => item.Time.split(' ')[1]), // 显示时间部分
          axisLabel: {
            rotate: 30
          }
        },
        yAxis: [
          {
            type: 'value',
            name: 'PM2.5/PM10 (μg/m³)',
            min: 0
          },
          {
            type: 'value',
            name: 'TVOC/CO2 (ppm)',
            min: 0
          }
        ],
        series: [
          {
            name: 'PM2.5',
            type: 'bar',
            barWidth: 15,
            data: this.dailyData.map(item => item.PM2_5),
            itemStyle: {
              color: '#61a0a8'
            }
          },
          {
            name: 'PM10',
            type: 'bar',
            barWidth: 15,
            data: this.dailyData.map(item => item.PM10),
            itemStyle: {
              color: '#d48265'
            }
          },
          {
            name: 'TVOC',
            type: 'line',
            yAxisIndex: 1,
            smooth: true,
            data: this.dailyData.map(item => item.TVOC),
            itemStyle: {
              color: '#91c7ae'
            },
            lineStyle: {
              width: 3
            }
          },
          {
            name: 'CO2',
            type: 'line',
            yAxisIndex: 1,
            smooth: true,
            data: this.dailyData.map(item => item.CO2),
            itemStyle: {
              color: '#749f83'
            },
            lineStyle: {
              width: 3
            }
          }
        ],
        grid: {
          left: '3%',
          right: '4%',
          bottom: '10%',
          containLabel: true
        }
      };
      this.gasChart.setOption(option);
    }
  }
};
</script>

<style scoped>
.info-panel {
  padding: 20px;
}

.chart-container {
  margin-bottom: 20px;
}

.chart-item {
  width: 100%;
  height: 400px;
}

.temperature-gauge {
  width: 100%;
  height: 400px;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f7f7f7;
  border-radius: 10px;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
  margin-bottom: 20px;
}

.chart {
  width: 100%;
  height: 100%;
}
</style>
