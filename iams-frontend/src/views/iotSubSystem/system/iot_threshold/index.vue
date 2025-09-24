<template>
  <div class="info-panel" style="height: 800px;overflow-y: auto">
    <!-- 仓库名称与区域的下拉菜单 -->
    <el-form :model="queryParams" ref="queryForm" :inline="true" label-width="100px">
      <el-form-item label="选择仓库" prop="warehouseLocation">
        <el-select
          v-model="queryParams.warehouseLocation"
          placeholder="请选择仓库区域"
          @change="handleWarehouseChange"
        >
          <el-option
            v-for="item in warehouseLocationOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>
    </el-form>

    <!-- 温度计图表 -->
<!--      <div style="display: flex; justify-content: space-around; margin-top: 0px;">-->
<!--        <div style="width: 45%; height: 400px;">-->
<!--          <tem :lasttem=this.minTemperatureChart @update-temp="handleTemperatureUpdate"></tem>-->
<!--        </div>-->
<!--        <div style="width: 45%; height: 400px;">-->
<!--          <tem :lasttem=this.maxTemperatureChart @update-temp="handleTemperatureUpdate2"></tem>-->
<!--        </div>-->
<!--      </div>-->
    <!-- 温度计图表 -->
    <div style="display: flex; justify-content: space-around; margin-top: 0px;">
      <!-- 第一个温度图表 -->
      <div
        style="width: 45%; height: 500px; border: 1px solid #ccc; padding: 10px; box-sizing: border-box;"
      >
        <div style="text-align: center; font-size: 16px; font-weight: bold; margin-bottom: 10px;">
          调整最小阈值
        </div>
        <tem :lasttem="minTemperatureChart" @update-temp="handleTemperatureUpdate"></tem>
      </div>

      <!-- 第二个温度图表 -->
      <div
        style="width: 45%; height: 500px; border: 1px solid #ccc; padding: 10px; box-sizing: border-box;"
      >
        <div style="text-align: center; font-size: 16px; font-weight: bold; margin-bottom: 10px;">
          调整最大阈值
        </div>
        <tem :lasttem="maxTemperatureChart" @update-temp="handleTemperatureUpdate2"></tem>
      </div>
    </div>


    <!-- 更新按钮 -->
      <div style="text-align: center; margin-top: 100px;">
        <el-button type="primary" @click="updateThresholdInDatabase">更新阈值</el-button>
      </div>

  </div>
</template>

<script>
import * as echarts from 'echarts';
import { listThreshold, getThreshold, updateThreshold } from '@/api/system/threshold';
import temperature from './temperature_display.vue'

export default {
  name: 'Threshold',
  components: {
    "tem": temperature,
  },
  data() {
    return {
      // 查询参数
      queryParams: {
        warehouseLocation: null, // 仓库区域
      },
      // 仓库区域选项
      warehouseLocationOptions: [],
      // ECharts 实例
      minTemperatureChart: 0, // 最小阈值温度
      maxTemperatureChart: 0, // 最大阈值温度计
      // 当前选中的阈值数据
      currentThreshold: {
        min: 0,
        max: 0,
      },
    };
  },
  created() {
    // 初始化仓库区域下拉菜单
    this.getWarehouseLocationOptions();
  },
  mounted() {
    // 初始化 ECharts 图表
    this.initTemperatureCharts();
  },
  methods: {
    /** 获取仓库区域下拉菜单选项 */
    async getWarehouseLocationOptions() {
      try {
        const response = await listThreshold();
        if (response.code === 200) {
          this.warehouseLocationOptions = response.rows.map(item => ({
            value: item.id,
            label: `${item.warehouseName || '未知仓库'} - ${item.location || '未知区域'}`,
          }));
          this.queryParams.warehouseLocation  = this.warehouseLocationOptions[0].value;
          // console.log(this.warehouseLocationOptions[0])
        } else {
          console.error('接口返回数据格式不正确:', response);
          this.$modal.msgError('获取仓库区域选项失败：数据格式不正确');
        }
      } catch (error) {
        console.error('获取仓库区域选项失败:', error);
        this.$modal.msgError('获取仓库区域选项失败');
      }
    },

    /** 处理仓库区域选择变化 */
    async handleWarehouseChange(value) {
      if (value) {
        const response = await getThreshold(value);
        // console.log(response)
        if (response.code === 200) {
          this.currentThreshold = {
            min: response.data.thresholdMin || 14, // 如果为 null，默认值为 0
            max: response.data.thresholdMax || 24, // 如果为 null，默认值为 100
          };
          // this.updateTemperatureCharts();
        } else {
          console.error('获取阈值数据失败:', response);
          this.$modal.msgError('获取阈值数据失败');
        }
      }
    },

    // 处理子组件传递的温度值
    handleTemperatureUpdate(newTemperature) {
      this.minTemperatureChart = newTemperature;
      // console.log('接收到子组件的温度值：', newTemperature);
    },

    // 处理子组件传递的温度值
    handleTemperatureUpdate2(newTemperature) {
      this.maxTemperatureChart = newTemperature;
      // console.log('接收到子组件的温度值：', newTemperature);
    },


    /** 初始化 ECharts 温度计图表 */
    async initTemperatureCharts() {
      const response = await listThreshold();
      // 最小阈值温度计
      this.minTemperatureChart = response.rows[0].thresholdMin;
      // console.log(response.rows[0].thresholdMin)
      // console.log(response.code)


      // 最大阈值温度计
      this.maxTemperatureChart = response.rows[0].thresholdMax;
      // console.log(response.rows[0].thresholdMax)

    },

    /** 更新数据库中的阈值 */
    async updateThresholdInDatabase() {
      try {
        await updateThreshold({
          id: this.queryParams.warehouseLocation,
          thresholdMin: this.minTemperatureChart,
          thresholdMax: this.maxTemperatureChart,
        });
        this.$modal.msgSuccess('阈值更新成功');
      } catch (error) {
        console.error('阈值更新失败:', error);
        this.$modal.msgError('阈值更新失败');
      }
    },
  },
};
</script>

<style>
.info-panel {
  padding: 20px;
}

</style>

