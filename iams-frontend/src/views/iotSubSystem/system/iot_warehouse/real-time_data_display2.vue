<template>
  <div class="info-panel">
    <el-form :inline="true" label-width="100px">
      <el-row :gutter="25" class="mb8">
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
      </el-row>
    </el-form>
    <el-row :gutter="25" class="mb8">
      <!-- 第一部分：温度模块 -->
      <el-col :span="12" class="bordered-module">
        <div class="module-title">温度</div>
        <tem :lasttem="currentData.Temp"></tem>
      </el-col>
      <!-- 第二部分：湿度模块 -->
      <el-col :span="12" class="bordered-module">
        <div class="module-title">湿度</div>
        <div class="one_box" id="one_box" ref="one_box"></div>
      </el-col>
    </el-row>

    <!-- 饼状气体含量图 -->
    <el-row :gutter="25" class="mb8 center-row bordered-module">
      <gas :PM25Level="currentData.PM2_5" :PM10Level="currentData.PM10" :TVOCLevel="currentData.TVOC" :CO2Level="currentData.CO2"></gas>
    </el-row>

    <!-- 状态信息 -->
    <el-row :gutter="25" class="mb8">
      <el-col :span="24" class="bordered-module">
        <div class="module-title">状态信息</div>
        <div class="status-container">
          <div class="status-item" v-for="(value, key) in statusData" :key="key">
            <span class="status-label">{{ key }}:</span>
            <span class="status-value" :class="{'warning': value === '报警', 'error': value === '异常'}">{{ value }}</span>
          </div>
        </div>
      </el-col>

      <el-col :span="24" class="bordered-module">
        <div class="module-title">按钮</div>
        <div class="status-container">
          <div class="status-item">
            <span class="status-label" ></span>
            <span class="status-value">
              <!-- 通风状态按钮 -->
              <el-button
                  :class="{ 'btn-active': currentData.IsVent, 'btn-inactive': !currentData.IsVent }"
                  @click="toggleVentilation"
              >
                {{ currentData.IsVent ? '开启通风' : '关闭通风' }}
              </el-button>
            </span>
            <!-- 自动开架按钮 -->
            <span class="status-label"></span>
            <span class="status-value">
              <el-button
                  :class="{ 'btn-active': currentData.IsZDKJ, 'btn-inactive': !currentData.IsZDKJ }"
                  @click="toggleAutoOpen"
              >
                {{ currentData.IsZDKJ ? '关闭自动开架' : '开启自动开架' }}
              </el-button>
            </span>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 新增方块可视化区域 -->
    <el-row :gutter="25" class="mb8">
      <el-col :span="24" class="bordered-module">
        <div class="module-title">列到位状态可视化</div>
        <div class="block-container" ref="blockContainer"></div>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import * as echarts from "echarts";
import { listInfo } from "@/api/system/GDIpInfo.js";
import temperature from './webSocketTableDataTemperature.vue'
import gas from './webSocketTableDataGas.vue'
import axios from "axios";
import {ElMessage} from "element-plus";
export default {
  components: {
    "tem":temperature,
    gas
  },
  data() {
    return {
      // 区号列表
      areaList: [],
      // 定义一个全局数组，用于存储所有区号数据
      areaDataList: [],
      // 当前选择的区号
      selectedArea: '',
      // 气体
      PM2_5Level: 0,
      PM10Level: 0,
      TVOCLevel: 0,
      CO2Level: 0,
      // 当前显示的数据
      currentData: {
        QUNO: '',
        Temp: 0,
        Hum: 0,
        PM2_5: 0,
        PM10: 0,
        TVOC: 0,
        CO2: 0,
        MJJZTLXName: '',
        IsBJ: 0,
        IsLock: 0,
        IsVent: 0,
        IsPower: 0,
        IsZDKJ: 0,
        ColumnStatus: ''
      },
      // IP信息列表
      infoList: [],
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 9999999,
        gdlName: null,
        gdlIp: null,
        gdlPort: null,
        gdlRemark: null
      },
      // 新增方块样式配置
      blockStyle: {
        width: 40,
        height: 40,
        margin: 2,
        border: '1px solid #ddd',
        display: 'inline-block',
        position: 'relative'
      },
      connectionStyle: {
        width: '100%',
        height: 2,
        position: 'absolute'
      }
    };
  },
  created() {
    this.getIpList();
  },
  computed: {
    // 计算状态数据
    statusData() {
      return {
        '状态': this.currentData.MJJZTLXName,
        '报警状态': this.currentData.IsBJ ? '报警' : '正常',
        '锁定状态': this.currentData.IsLock ? '锁定' : '未锁定',
        '通风状态': this.currentData.IsVent ? '未通风' : '通风中',
        '电源状态': this.currentData.IsPower ? '断电' : '正常',
        '自动开架': this.currentData.IsZDKJ ? '启用' : '禁用',
        '列到位状态': this.currentData.ColumnStatus
      };
    },
    // 新增列状态解析
    columnBlocks() {
      return this.currentData.ColumnStatus.split('').map(status => ({
        status: parseInt(status),
        leftConnected: [1, 3].includes(parseInt(status)),
        rightConnected: [2, 3].includes(parseInt(status))
      }));
    }
  },
  mounted() {
    // 初始化时绘制方块
    this.drawBlocks();
    // 监听列状态变化
    this.$watch('currentData.ColumnStatus', this.drawBlocks);
  },
  methods: {
    /** 查询固定列IP信息管理列表 */
    getIpList() {
      listInfo().then(response => {
        this.infoList = response.rows;
        console.log(this.infoList)
        // 获取第一个IP信息作为默认查询
        // if (this.infoList.length > 0) {
        //   const firstIpInfo = this.infoList[0];
        //   this.queryAreaData(firstIpInfo.gdlIp, firstIpInfo.gdlPort);
        // }
        // 遍历 infoList，对每个 IP 和 Port 调用 queryAreaData
        this.infoList.forEach(ipInfo => {
          this.queryAreaData(ipInfo.gdlIp, ipInfo.gdlPort);
        });
      });
    },

    /** 查询区号数据 */
    async queryAreaData(ip, port) {
      // 模拟后端返回的区号数据
      const mockAreaData = [
        {
          QUNO: 1,
          Temp: 25,
          Hum: 60,
          PM2_5: 10,
          PM10: 20,
          TVOC: 5,
          CO2: 20,
          MJJZTLXName: "正常",
          IsBJ: 0,
          IsLock: 0,
          IsVent: 1,
          IsPower: 0,
          IsZDKJ: 0,
          ColumnStatus: "12333333"
        },
        {
          QUNO: 2,
          Temp: 26,
          Hum: 65,
          PM2_5: 15,
          PM10: 25,
          TVOC: 10,
          CO2: 450,
          MJJZTLXName: "正常",
          IsBJ: 0,
          IsLock: 0,
          IsVent: 0,
          IsPower: 0,
          IsZDKJ: 0,
          ColumnStatus: "1233333333333"
        },
        {
          QUNO: 3,
          Temp: 25,
          Hum: 60,
          PM2_5: 10,
          PM10: 20,
          TVOC: 5,
          CO2: 400,
          MJJZTLXName: "正常",
          IsBJ: 0,
          IsLock: 0,
          IsVent: 0,
          IsPower: 0,
          IsZDKJ: 0,
          ColumnStatus: "12333333333"
        },
        {
          QUNO: 4,
          Temp: 25,
          Hum: 60,
          PM2_5: 10,
          PM10: 20,
          TVOC: 5,
          CO2: 120,
          MJJZTLXName: "正常",
          IsBJ: 0,
          IsLock: 0,
          IsVent: 0,
          IsPower: 0,
          IsZDKJ: 0,
          ColumnStatus: "12222222"
        },
      ];


      // await axios({
      //   url: 'http://'+ip+':'+port+'/MjjWebApi?Op=GetMjjStatus',
      //   method: 'get',
      // }).then(response => {
      //   this.areaList = response.data; // 将服务器返回的数据存储到 data 中
      // })
      // .catch(error => {
      //   console.error('请求失败:', error);
      // });

      // 模拟从后端获取数据
      this.areaList = mockAreaData;
      if (this.areaList.length > 0) {
        this.selectedArea = this.infoList[0].gdlNo;
        this.currentData = { ...this.areaList[0] };
      }

      //更新气体值
      this.CO2Level = this.currentData.CO2;
      this.PM10Level = this.currentData.PM10;
      this.TVOCLevel = this.currentData.TVOC;
      this.PM2_5Level = this.currentData.PM2_5;

      // 更新当前湿度值
      this.currentData.Hum = parseFloat( this.currentData.Hum / 100).toFixed(2);
      this.initChart();
    },

    /** 处理区号变更 */
    handleAreaChange(areaNo) {
      const selectedArea = this.areaList.find(item => item.QUNO == areaNo);
      console.log(selectedArea,'555')
      if (selectedArea) {
        this.currentData = { ...selectedArea };
      }
      // console.log(this.selectedArea)
      //更新气体值
      this.CO2Level = this.currentData.CO2;
      this.PM10Level = this.currentData.PM10;
      this.TVOCLevel = this.currentData.TVOC;
      this.PM2_5Level = this.currentData.PM2_5;

      // 更新当前湿度值
      this.currentData.Hum = parseFloat( this.currentData.Hum / 100).toFixed(2);

      // 重新初始化水滴图
      this.initChart();
    },
    initChart() {
      if (!this.chart) {
        this.chart = echarts.init(this.$refs.one_box);
      }

      // 保留小数点后一位
      const formattedHumidity = parseFloat(this.currentData.Hum || 0.5).toFixed(3);


      this.option2 = {
        backgroundColor: "white",
        series: [
          {
            type: "liquidFill",
            radius: "80%",
            center: ["50%", "50%"],
            amplitude: 20,
            data: [parseFloat(formattedHumidity)], // 使用格式化后的湿度值
            color: ["#23cc72"],
            backgroundStyle: {
              borderWidth: 6,
              borderColor: "#23cc72",
              color: "#485C6D"
            },
            label: {
              position: ["50%", "50%"],
              formatter: `${parseFloat(formattedHumidity * 100).toFixed(2)}%`, // 显示当前湿度值（百分比形式）
              fontSize: "52px",
              color: "#fff"
            },
            outline: {
              show: false
            }
          }
        ]
      };

      this.chart.setOption(this.option2);
    },
    /** 切换通风状态 */
    toggleVentilation() {
      ElMessage({
        message: "正在切换通风状态",
        type: "success",
        duration: 2500,
      });
      // this.currentData.IsVent = !this.currentData.IsVent;
      //获取区间的IP和port
      const gdlinfo = this.infoList.find(item => item.gdlNo === this.selectedArea);
      // console.log('http://'+ gdlinfo.gdlIp +':'+ gdlinfo.gdlPort +'/MjjWebApi?Op= Ventilate')
      // 直接请求通风接口
      if (this.currentData.IsVent){
        axios({
          url: 'http://'+ gdlinfo.gdlIp +':'+ gdlinfo.gdlPort +'/MjjWebApi?Op=Ventilate',
          method: 'get',
        }).then(response => {
          this.currentData.IsVent = !this.currentData.IsVent;
          ElMessage({
            message: "已开启自动通风",
            type: "success",
            duration: 2500,
          });
        }).catch(error => {
          ElMessage({
            message: "通风接口请求失败",
            type: "error",
            duration: 2500,
          });
        })
      } else {
        axios({
          url: 'http://'+ gdlinfo.gdlIp +':'+ gdlinfo.gdlPort +'/MjjWebApi?Op=Stop_Ventilate',
          method: 'get',
        }).then(response => {
          this.currentData.IsVent = !this.currentData.IsVent;
          ElMessage({
            message: "已关闭自动通风",
            type: "success",
            duration: 2500,
          });
        }).catch(error => {
          ElMessage({
            message: "通风接口请求失败",
            type: "error",
            duration: 2500,
          });
        })
      }
    },

    /** 切换自动开架状态 */
    toggleAutoOpen() {
      ElMessage({
        message: "正在切换开架状态",
        type: "success",
        duration: 2500,
      });
      //获取区间的IP和port
      const gdlinfo = this.infoList.find(item => item.gdlNo === this.selectedArea);
      // 直接请求开架接口
      if (this.currentData.IsVent){
        axios({
          // url: 'http://固定列Ip地址:端口/MjjWebApi?Op=GetQuXX',
          url: 'http://'+ gdlinfo.gdlIp +':'+ gdlinfo.gdlPort +'/MjjWebApi?Op=OpenAutoMove',
          method: 'get',
          // params: query
        }).then(response => {
          this.currentData.IsZDKJ = !this.currentData.IsZDKJ;
          ElMessage({
            message: "已开启自动开架",
            type: "success",
            duration: 2500,
          });
        }).catch(error => {
          ElMessage({
            message: "开架接口请求失败",
            type: "error",
            duration: 2500,
          });
        })
      } else {
        axios({
          // url: 'http://固定列Ip地址:端口/MjjWebApi?Op=GetQuXX',
          url: 'http://'+ gdlinfo.gdlIp +':'+ gdlinfo.gdlPort +'/MjjWebApi?Op=CloseAutoMove',
          method: 'get',
          // params: query
        }).then(response => {
          this.currentData.IsZDKJ = !this.currentData.IsZDKJ;
          ElMessage({
            message: "已关闭自动开架",
            type: "success",
            duration: 2500,
          });
        }).catch(error => {
          ElMessage({
            message: "开架接口请求失败",
            type: "error",
            duration: 2500,
          });
        })
      }
    },

    // 新增方块绘制方法
    drawBlocks() {
      const container = this.$refs.blockContainer;
      container.innerHTML = ''; // 清空原有内容

      if (!this.currentData.ColumnStatus) return;

      const blocks = this.columnBlocks;
      blocks.forEach((block, index) => {
        const div = document.createElement('div');
        Object.assign(div.style, this.blockStyle);

        // 绘制左侧连接
        if (index > 0 && blocks[index-1].rightConnected && block.leftConnected) {
          const leftLine = document.createElement('div');
          Object.assign(leftLine.style, this.connectionStyle, {
            top: 0,
            backgroundColor: '#409EFF'
          });
          div.appendChild(leftLine);
        }

        // 绘制方块主体
        const statusCircle = document.createElement('div');
        statusCircle.style.cssText = `
          width: 50px;
          height: 100px;
          background-color: ${this.getBlockColor(block.status)};
          border-radius: 4px;
          display: flex;
          align-items: center;
          justify-content: center;
          color: white;
        `;
        statusCircle.textContent = block.status;
        div.appendChild(statusCircle);

        // 绘制右侧连接
        if (index < blocks.length - 1 && block.rightConnected && blocks[index+1].leftConnected) {
          const rightLine = document.createElement('div');
          Object.assign(rightLine.style, this.connectionStyle, {
            bottom: 0,
            backgroundColor: '#409EFF'
          });
          div.appendChild(rightLine);
        }

        container.appendChild(div);
      });
    },

    // 获取方块颜色
    getBlockColor(status) {
      switch (status) {
        case 0: return 'rgba(255,0,0,0.5)'; // 未到位
        case 1: return '#409EFF'; // 左到位
        case 2: return '#4467ff'; // 右到位
        case 3: return '#16ab4e'; // 全到位
        default: return '#ddd';
      }
    },
  },

};
</script>

<style scoped>
.info-panel {
  padding: 20px;
}

.bordered-module {
  border: 1px solid #ebeef5;
  border-radius: 4px;
  padding: 15px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.module-title {
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 15px;
  color: #409EFF;
}

.humidity-value, .data-value {
  font-size: 24px;
  text-align: center;
  margin: 20px 0;
}

.status-container {
  display: flex;
  flex-wrap: wrap;
}

.status-item {
  width: 33.33%;
  padding: 8px 0;
}

.status-label {
  font-weight: bold;
  margin-right: 10px;
}

.status-value {
  color: #67C23A;
}

.status-value.warning {
  color: #E6A23C;
}

.status-value.error {
  color: #F56C6C;
}

.center-row {
  display: flex;
  justify-content: center;
}
.one_box {
  width: 100%;
  height: 400px;
}

/* 自定义按钮样式 */
.status-item .el-button {
  padding: 10px 20px; /* 调整按钮大小 */
  border-radius: 5px; /* 添加圆角 */
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2); /* 添加阴影 */
  margin: 0 10px; /* 调整按钮间距 */
}

/* 按钮激活状态样式 */
.btn-active {
  background-color: #4caf50; /* 激活状态背景颜色 */
  color: white; /* 激活状态文字颜色 */
}

/* 按钮非激活状态样式 */
.btn-inactive {
  background-color: #f44336; /* 非激活状态背景颜色（红色） */
  color: white; /* 非激活状态文字颜色 */
}

/* 状态信息间隔 */
.spacer {
  display: inline-block;
  width: 10px; /* 调整间隔宽度 */
}


/* 新增方块样式 */
.block-container {
  padding: 15px;
  background-color: #78d79c;
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}
</style>
