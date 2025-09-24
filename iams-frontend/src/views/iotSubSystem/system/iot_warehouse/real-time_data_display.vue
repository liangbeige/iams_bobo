<template>
  <div class="info-panel">
    <div class="container mx-auto p-4 text-center">
      <el-row :gutter="25" class="mb8">
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
        <!--<h1 class="text-2xl font-bold mb-4">自动刷新示例</h1>-->
        <!--<p class="mb-4">页面将在 <span class="font-bold text-red-500">5</span> 分钟后刷新</p>-->
        <!--<div class="bg-gray-100 p-4 rounded-lg mb-4">-->
        <!--  <p>当前时间: {{ currentTime }}</p>-->
        <!--  <p>随机数: {{ randomNumber }}</p>-->
        <!--</div>-->
        <el-tag>此页面数据每2分钟刷新一次,您也可以点击按钮进行立即刷新</el-tag>
        <el-tag type="warning">如发现倒计时异常请刷新浏览器即可解决</el-tag>
        <el-button type="success" @click="refreshNow" class="bg-blue-500 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded">
          立即刷新页面状态
        </el-button>
      </el-row>
    </div>

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

    <!-- 状态信息 -->
    <el-row :gutter="25" class="mb8">
      <el-col :span="24" class="bordered-module">
        <div class="module-title">状态信息</div>
        <div class="status-container">
          <div style="width: 33.3%" class="status-item" v-for="(value, key) in statusData" :key="key">
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
            <!--<span class="status-value">-->
              <!-- 通风状态按钮 -->
              <el-button
                  :class="{ 'btn-active': !currentData.IsVent, 'btn-inactive': currentData.IsVent }"
                  @click="toggleVentilation"
              >
                {{ !currentData.IsVent ? '开启通风' : '关闭通风并合架' }}
              </el-button>
              <!--  一键为所有区域进行通风按钮-->
              <el-button type="primary" @click="toggleVentilationAll">
                一键为所有区域进行通风
              </el-button>
              <el-button type="danger" @click="toggleCloseJiaTiAll">
                一键为所有区域闭架
              </el-button>
            <!--</span>-->
            <!-- 自动开架按钮 -->
            <!--<span class="status-label"></span>-->
            <!--<span class="status-value">-->
            <!--  <el-button-->
            <!--      :class="{ 'btn-active': !currentData.IsZDKJ, 'btn-inactive': currentData.IsZDKJ }"-->
            <!--      @click="toggleAutoOpen"-->
            <!--  >-->
            <!--    {{ currentData.IsZDKJ ? '关闭自动开架' : '开启自动开架' }}-->
            <!--  </el-button>-->
            <!--</span>-->
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 新增方块可视化区域 -->
    <!-- 列到位状态可视化区域 -->
    <el-row :gutter="25" class="mb8">
      <el-col :span="24" class="bordered-module">
        <div class="module-title">列到位状态可视化</div>
        <div class="block-container" ref="blockContainer"></div>
      </el-col>
    </el-row>

    <!-- 饼状气体含量图 -->
    <!--<el-row :gutter="25" class="mb8 center-row bordered-module">-->
    <!--  <gas :PM25Level="currentData.PM2_5" :PM10Level="currentData.PM10" :TVOCLevel="currentData.TVOC" :CO2Level="currentData.CO2"></gas>-->
    <!--</el-row>-->

  </div>
</template>

<script>
import * as echarts from "echarts";
import {getInfo, listInfo} from "@/api/system/GDIpInfo.js";
import temperature from './webSocketTableDataTemperature.vue'
import gas from './webSocketTableDataGas.vue'
import axios from "axios";
import {ElMessage, ElMessageBox} from "element-plus";
const createLoading = (proxy, text) => proxy.$loading({ lock: true, text, spinner: "el-icon-loading" });
import {cancelSleepColumn, getColumnStatus,unlockColumn,resetColumn} from "@/api/system/move.js";
const delay = (ms) => new Promise(resolve => setTimeout(resolve, ms));
const showMsg = (msg, type = 'success', duration = 3000) => ElMessage({ type, message: msg, duration });
const showConfirm = (msg, title = '系统提示',type = 'warning') => ElMessageBox.confirm(msg, title, { confirmButtonText: '确定', cancelButtonText: '取消', type: type });

// 仅解锁
const checkAndUnlock = async (proxy,ip, port,quHao) => {
  const unlockRes = await unlockColumn(ip, port);
  if (unlockRes.data.data !== "1") {
    showMsg('解锁失败', 'error');
    return false;
  }

  let loading_judge;
  let loading_lock = createLoading(proxy, '判断列是否解锁中...请稍候');


  // 循环检测解锁状态，直到IsLock变为0-------------------------------------------   已现场测试无异常
  let retryCount = 0;
  let maxRetries = 60; // 最大重试次数
  let retryInterval = 500; // 重试间隔(毫秒)
  while (retryCount < maxRetries) {
    await delay(retryInterval);
    retryCount++;

    const statusRes = await getColumnStatus(ip, port);
    const { IsLock, MJJZTLX } = statusRes.data.data[0];

    console.log('当前IsLock状态码:', IsLock)

    // 判断是否解锁成功 且 MJJZTLX为ZT_0E(解除禁止)时，跳出循环
    if (IsLock == 0 && MJJZTLX == 'ZT_0E') {
      loading_lock = createLoading(proxy, '正在检测当前状态...');
      await delay(1500)
      loading_lock.setText('列解锁成功');
      loading_lock.close();


      break; // 解锁成功，跳出循环
    }

    // 更新提示信息
    loading_lock.setText(`正在为对应区列解锁中...请稍等 ${retryCount}/${maxRetries}`);
  }
  // 如果达到最大重试次数仍未解锁成功
  if (retryCount >= maxRetries) {
    // showMsg('列解锁超时，请手动检查', 'error');
    await showConfirm('列解锁超时，请手动检查', '系统警告')
    loading_lock.close();
    return false;
  }
}
// 解锁并检查错误码，然后循环判断合架
const unlockAndCheckErrorAndReset = async (proxy,ip, port,quHao) => {
  const unlockRes = await unlockColumn(ip, port);
  if (unlockRes.data.data !== "1") {
    showMsg('解锁失败', 'error');
    return false;
  }

  let loading_judge;
  let loading_lock = createLoading(proxy, '正在为对应区列解锁中...请稍候');


  // 循环检测解锁状态，直到IsLock变为0-------------------------------------------   已现场测试无异常
  let retryCount = 0;
  let maxRetries = 60; // 最大重试次数
  let retryInterval = 500; // 重试间隔(毫秒)
  while (retryCount < maxRetries) {
    await delay(retryInterval);
    retryCount++;

    const statusRes = await getColumnStatus(ip, port);
    const { IsLock, MJJZTLX } = statusRes.data.data[0];

    console.log('当前IsLock状态码:', IsLock)

    // 判断是否解锁成功 且 MJJZTLX为ZT_0E(解除禁止)时，跳出循环
    if (IsLock == 0 && MJJZTLX == 'ZT_0E') {
      loading_lock.setText('列解锁成功');
      loading_lock.close();
      loading_judge = createLoading(proxy, '正在检测当前状态...');
      await delay(1000)
      // 解锁确认成功，发送合架指令
      const res = await resetColumn(ip, port);
      if (res.data.data == "1") {
        loading_judge.setText('初次上电,架体归位中...');
        await delay(3000)  // 等待解锁状态 变为移动状态
      }
      break; // 解锁成功，跳出循环
    }

    // 更新提示信息
    loading_lock.setText(`正在为对应区列解锁中...请稍等 ${retryCount}/${maxRetries}`);
  }
  // 如果达到最大重试次数仍未解锁成功
  if (retryCount >= maxRetries) {
    // showMsg('列解锁超时，请手动检查', 'error');
    await showConfirm('列解锁超时，请手动检查', '系统警告')
    loading_lock.close();
    return false;
  }

  // 循环检测合架是否进行或者已经成功
  retryCount = 0;
  maxRetries = 300; // 最大重试次数
  retryInterval = 500; // 重试间隔(毫秒)
  let currentMJJZTLX;
  let currentMJJZTLXArr = [];
  while (retryCount < maxRetries) {
    await delay(retryInterval);
    retryCount++;
    const statusRes = await getColumnStatus(ip, port);
    // 检测状态码是否异常
    const { MJJZTLX, MJJZTLXName } = statusRes.data.data[0];

    // 检测状态码是否为ZT_07,为ZT_07则提示成功并结束
    if(MJJZTLX=='ZT_07'){
      loading_judge.close();// 关闭加载提示
      // await showConfirm('第'+quHao+'区所有架体成功归位', '系统成功提示','success')
      ElMessage({
        message: '第'+quHao+'区所有架体成功归位。',
        type: 'success',
        duration: 3000
      })
      loading_judge.close(); // 关闭加载提示
      return true
    }

    // 检测MJJZTLX不存在于currentMJJZTLXArr,就加入
    if (!currentMJJZTLXArr.includes(MJJZTLX)) {
      currentMJJZTLXArr.push(MJJZTLX);
    }
    currentMJJZTLX = MJJZTLX+':'+MJJZTLXName;

    // MMJZTLX在执行了移动命令之后,未立即响应,仍未ZT_0E，即解除禁止状态，这里判断状态码是否为ZT_0E，如果是 则continue
    if (MJJZTLX == 'ZT_0E' || MJJZTLXName.includes('解除禁止')) {
      loading_judge.setText(`架体归位中...请稍等 ${retryCount}/${maxRetries}`);
      continue;
    }

    // 当currentMJJZTLXArr中的状态码数量>3，则提醒用户是否继续重试   或者 不是移动中时候，则提醒用户是否继续重试  或者不是解除禁止  也不是未知指令
    if (currentMJJZTLXArr.length > 3 || (MJJZTLX != 'ZT_0C' && MJJZTLX != 'ZT_00')) {
      console.log('当前的不同状态码集合:currentMJJZTLXArr:', currentMJJZTLXArr)
      const confirmResult = await showConfirm('检测到柜体异常,建议人工检查架体是否异常(如过道有人、遮挡物等);若确认无异常，请点击确认继续合架', '严重警告','error')
          .then(async () => {
            // 继续执行合架
            const res = await resetColumn(ip, port);
            if (res.data.data == "1") {
              currentMJJZTLXArr = []
              loading_judge.setText('重新发送合架指令,持续检测合架状态中...');
            }
          })
          .catch(async () => {
            loading_judge.close();
            await showConfirm('用户取消合架,点击确定进行重试，点击取消进行取消操作!', '系统警告');
            return false;
          });
    }

    loading_judge.setText(`架体归位中...请稍等 ${retryCount}/${maxRetries}`);
  }
  // 如果达到最大重试次数仍未解锁成功
  if (retryCount >= maxRetries) {
    await showConfirm('架体归位超时,当前状态为['+currentMJJZTLX+'],请手动检查柜体异常', '严重警告','error')
    loading_lock.close();
    return false;
  }
}
const handleSleepStatus = async (proxy, ip, port, isPower,gdlNo) => {
  // 输出当前时间
  console.log(`标志位置isPower`, isPower);
  if (isPower == 0) {
    let loading_handleSleepStatus = createLoading(proxy, '检测到架体已经休眠,正在解除各个柜体的休眠状态...');
    await delay(1000); // 初始延迟，确保指令发送

    const res = await cancelSleepColumn(ip, port);
    if (res.data.data !== "1") {
      loading_handleSleepStatus.close();
      await showConfirm('关闭休眠失败,请手动移动至固定列打开休眠状态!', '系统警告');
      return false;
    }
    // 持续检测休眠状态，直到 isPower 变为 1
    let retryCount = 0;
    const maxRetries = 60; // 最大重试次数
    const retryInterval = 500; // 重试间隔(毫秒)

    while (retryCount < maxRetries) {
      await delay(retryInterval);
      retryCount++;

      const statusRes = await getColumnStatus(ip, port);
      const currentIsPower = statusRes.data.data[0].IsPower;
      console.log('当前IsPower状态码:', currentIsPower)
      if (currentIsPower == 1) {
        loading_handleSleepStatus.setText(`第`+gdlNo+`区成功解锁休眠状态,初始化中...`);
        await delay(7000)
        console.log('休眠状态关闭成功')
        ElMessage({
          message: '休眠状态关闭成功...数据获取中',
          type: 'success',
          duration: 3000
        })
        await unlockAndCheckErrorAndReset(proxy,ip, port,gdlNo)
        loading_handleSleepStatus.close();
        return true;
      }

      // 更新加载提示
      loading_handleSleepStatus.setText(`正在关闭休眠状态: 请稍等 ${retryCount}/${maxRetries}`);
    }

    // 达到最大重试次数仍未成功
    loading_handleSleepStatus.close();
    showMsg('休眠状态关闭超时，请手动检查', 'error');
    return false;
  }
  return true;
};

export default {
  components: {
    "tem":temperature,
    gas
  },
  data() {
    return {
      toggleVentilationAllFlag:false,
      // 定时器 120s {2min}后自动刷新页面
      countdown: 2,
      currentTime: new Date().toLocaleTimeString(),
      randomNumber: Math.random(),
      refreshInterval: null,
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
        '通风状态': !this.currentData.IsVent ? '未通风' : '通风中',
        '电源状态': this.currentData.IsPower ? '正常' : '断电',
        // '自动开架': this.currentData.IsZDKJ ? '启用' : '禁用',
        // '列到位状态': this.currentData.ColumnStatus
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
    this.startCountdown()
    // 初始化时绘制方块
    this.drawBlocks();
    // 监听列状态变化
    this.$watch('currentData.ColumnStatus', this.drawBlocks);
  },
  methods: {
    // 一键为所有区域进行通风
    async toggleVentilationAll(){
      ElMessageBox.confirm('是否确认一键为所有区域进行通风?', '系统提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }).then(async () => {
        listInfo().then(async response => {
          this.infoList = response.rows;
          console.log(this.infoList)
          // 遍历 infoList，对每个 IP 和 Port 调用 queryAreaData
          for (const ipInfo of this.infoList) {
            // this.queryAreaData(ipInfo.gdlIp, ipInfo.gdlPort);
            // 先解锁
            await checkAndUnlock(this,ipInfo.gdlIp, ipInfo.gdlPort,ipInfo.gdlNo)
            await axios({
              url: 'http://' + ipInfo.gdlIp + ':' + ipInfo.gdlPort + '/MjjWebApi?Op=Ventilate',
              method: 'get',
            })
                .then(response => {
                  if (response.data.data == "1") {
                    this.currentData.IsVent = 1;
                    ElMessage({
                      message: ipInfo.gdlNo + "区已开启通风",
                      type: "success",
                      duration: 2500,
                    });
                  }
                })
                .catch(error => {
                  ElMessageBox.confirm('第'+ipInfo.gdlNo+'区通风接口请求失败,请检查此区的网络连接!', '严重警告', {
                    confirmButtonText: '确定',
                    cancelButtonText: '取消',
                    type: 'error',
                  })
                  // ElMessage({
                  //   message: "通风接口请求失败",
                  //   type: "error",
                  //   duration: 2500,
                  // });
                })
            await delay(1000)
          }
        });
      }).catch(() => {
        ElMessage({
          type: 'info',
          message: '已取消一键通风',
        });
      });

    },
    // 一键为所有区域关闭通风
    toggleCloseJiaTiAll(){
      ElMessageBox.confirm('是否确认一键为所有区域闭架?', '系统提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }).then(async () => {
        listInfo().then(async response => {
          this.infoList = response.rows;
          console.log(this.infoList)
          // 遍历 infoList，对每个 IP 和 Port 调用 queryAreaData
          for (const ipInfo of this.infoList) {
            // 先解锁
            await checkAndUnlock(this,ipInfo.gdlIp, ipInfo.gdlPort,ipInfo.gdlNo)
            // this.queryAreaData(ipInfo.gdlIp, ipInfo.gdlPort);
            await  axios({
              url: 'http://'+ ipInfo.gdlIp +':'+ ipInfo.gdlPort +'/MjjWebApi?Op=Reset',
              method: 'get',
            })
                .then(response => {
                  if (response.data.data == "1"){
                    this.currentData.IsVent = 0
                    ElMessage({
                      message: ipInfo.gdlNo + "区已发送闭架请求",
                      type: "success",
                      duration: 2500,
                    });
                  }
                })
                .catch(error => {
                  ElMessageBox.confirm('第'+ipInfo.gdlNo+'区合架接口请求失败,请检查此区的网络连接!', '严重警告', {
                    confirmButtonText: '确定',
                    cancelButtonText: '取消',
                    type: 'error',
                  })
                  // ElMessage({
                  //   message: "通风接口请求失败",
                  //   type: "error",
                  //   duration: 2500,
                  // });
                })
            await delay(1000)
          }
        });
      }).catch(() => {
        ElMessage({
          type: 'info',
          message: '已取消一键闭架',
        });
      })

    },
    /** 查询固定列IP信息管理列表 */
    getIpList() {
      listInfo().then(response => {
        this.infoList = response.rows;
        console.log(this.infoList)
        // 获取第一个IP信息作为默认查询
        if (this.infoList.length > 0) {
          const firstIpInfo = this.infoList[0];
          this.selectedArea = firstIpInfo.gdlNo;
          this.queryAreaData(firstIpInfo.gdlIp, firstIpInfo.gdlPort);
        }
        // 遍历 infoList，对每个 IP 和 Port 调用 queryAreaData
        this.infoList.forEach(ipInfo => {
          // this.queryAreaData(ipInfo.gdlIp, ipInfo.gdlPort);
          // 为每个区域解除休眠、解锁、架体归位(合并)
          this.checkConnect(ipInfo.gdlIp, ipInfo.gdlPort, ipInfo.gdlNo);
          this.queryAreaDataForXiuMian(ipInfo.gdlIp, ipInfo.gdlPort,ipInfo.gdlNo);
        });
      });
    },

    /** 查询区号数据 */
    async queryAreaData(ip, port) {
      await axios({
        url: 'http://'+ip+':'+port+'/MjjWebApi?Op=GetMjjStatus',
        method: 'get',
      }).then(response => {
        // console.log(response,'PZZ')
        this.currentData = response.data.data[0]; // 将服务器返回的数据存储到 data 中
      })
      .catch(error => {
        console.error('请求失败:', error);
      });


      //更新气体值
      this.CO2Level = this.currentData.CO2;
      this.PM10Level = this.currentData.PM10;
      this.TVOCLevel = this.currentData.TVOC;
      this.PM2_5Level = this.currentData.PM2_5;

      // 更新当前湿度值
      this.currentData.Hum = parseFloat( this.currentData.Hum / 100).toFixed(2);
      this.initChart();
    },

    // 网络连通检查
    async checkConnect(ip, port, gdlNo){
      await axios({
        url: 'http://'+ip+':'+port+'/MjjWebApi?Op=GetMjjStatus',
        method: 'get',
      })
          .then(async response => {

          })
          .catch(error => {
            // console.log('PZZ222')
            showConfirm(gdlNo+'区连接异常,请检查网络是否通畅!','严重警告','error')
            console.error('请求失败:', error);
          });
    },


    // 解除所有柜体的休眠
    async queryAreaDataForXiuMian(ip, port, gdlNo) {
      await axios({
        url: 'http://'+ip+':'+port+'/MjjWebApi?Op=GetMjjStatus',
        method: 'get',
      })
        .then(async response => {
          // console.log('PZZ111')
          let IsPower = response.data.data[0].IsPower; // 将服务器返回的数据存储到 data 中
          await handleSleepStatus(this,ip, port,IsPower,gdlNo)
      })
          .catch(error => {
            // console.log('PZZ222')
            // showConfirm(gdlNo+'区连接异常,请检查网络是否通畅!','严重警告','error')
            console.error('请求失败:', error);
          });
    },

    /** 处理区号变更 */
    handleAreaChange(areaNo) {
      getInfo(areaNo).then(response => {
        console.log('根据区号查询ip',response)
        const ip = response.data['gdlIp'];
        const port = response.data['gdlPort'];
        this.queryAreaData(ip, port)
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
        // 绘制方块
        this.drawBlocks();
      })
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
      let status;
      if(this.currentData.IsVent==0){
        status = '开启通风';
      }
      else {
        status = '关闭通风并合架';
      }
      ElMessageBox.confirm("确定要为"+this.selectedArea+"区"+status+"吗？", "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning"
      })
          .then(async () => {
        //获取区间的IP和port
        const gdlinfo = this.infoList.find(item => item.gdlNo === this.selectedArea);
        // console.log('http://'+ gdlinfo.gdlIp +':'+ gdlinfo.gdlPort +'/MjjWebApi?Op= Ventilate')
        // 直接请求通风接口
        if (!this.currentData.IsVent) {
          await checkAndUnlock(this, gdlinfo.gdlIp, gdlinfo.gdlPort, gdlinfo.gdlNo)
          await axios({
            url: 'http://' + gdlinfo.gdlIp + ':' + gdlinfo.gdlPort + '/MjjWebApi?Op=Ventilate',
            method: 'get',
          }).then(response => {
            if (response.data.data == "1") {
              this.currentData.IsVent = !this.currentData.IsVent;
              ElMessage({
                message: "正在通风中",
                type: "success",
                duration: 2500,
              });
              ElMessageBox.confirm("正在为" + this.selectedArea + "区通风中...同时您也可以选择其他区进行通风", "友情提示", {
                confirmButtonText: "确定",
                showCancelButton: false, // 不显示取消按钮
                closeOnClickModal: false, // 点击模态框外部不关闭
                closeOnPressEscape: false, // 按下ESC键不关闭
              })
            }
          }).catch(error => {
            ElMessage({
              message: "通风接口请求失败",
              type: "error",
              duration: 2500,
            });
          })
        }
        else {
          axios({
            url: 'http://' + gdlinfo.gdlIp + ':' + gdlinfo.gdlPort + '/MjjWebApi?Op=Reset', // 合架
            method: 'get',
          }).then(response => {
            if (response.data.data == "1") {
              this.currentData.IsVent = !this.currentData.IsVent;
              ElMessage({
                message: "已关闭通风",
                type: "success",
                duration: 2500,
              });
            }
          }).catch(error => {
            ElMessage({
              message: "通风接口请求失败",
              type: "error",
              duration: 2500,
            });
          })
        }
      }).catch(() => {
        ElMessage({
          message: "已取消切换通风状态",
          type: "info"
        });
      });
    },

    /** 切换自动开架状态 */
    toggleAutoOpen() {
      // ElMessage({
      //   message: "正在切换开架状态",
      //   type: "success",
      //   duration: 2500,
      // });
      //获取区间的IP和port
      const gdlinfo = this.infoList.find(item => item.gdlNo === this.selectedArea);
      // 直接请求开架接口
      if (!this.currentData.IsVent){
        axios({
          // url: 'http://固定列Ip地址:端口/MjjWebApi?Op=GetQuXX',
          url: 'http://'+ gdlinfo.gdlIp +':'+ gdlinfo.gdlPort +'/MjjWebApi?Op=OpenAutoMove',
          method: 'get',
          // params: query
        })
            .then(response => {
              if (response.data.data == "1"){
                this.currentData.IsZDKJ = !this.currentData.IsZDKJ;
                ElMessage({
                  message: "已开启自动开架",
                  type: "success",
                  duration: 2500,
                });
              }
        })
            .catch(error => {
          ElMessage({
            message: "开架接口请求失败",
            type: "error",
            duration: 2500,
          });
        })
      }
      else {
        axios({
          // url: 'http://固定列Ip地址:端口/MjjWebApi?Op=GetQuXX',
          url: 'http://'+ gdlinfo.gdlIp +':'+ gdlinfo.gdlPort +'/MjjWebApi?Op=CloseAutoMove',
          method: 'get',
          // params: query
        })
            .then(response => {
              if (response.data.data == "1"){
                this.currentData.IsZDKJ = !this.currentData.IsZDKJ;
                ElMessage({
                  message: "已关闭自动开架",
                  type: "success",
                  duration: 2500,
                });
              }

        })
            .catch(error => {
          ElMessage({
            message: "开架接口请求失败",
            type: "error",
            duration: 2500,
          });
        })
      }
    },

    // 新增优化后的方块绘制方法
    drawBlocks() {
      const container = this.$refs.blockContainer;
      container.innerHTML = '';

      if (!this.currentData.ColumnStatus) return;

      const blocks = this.columnBlocks;
      let length = blocks.length;
      blocks.forEach((block, index) => {
        const div = document.createElement('div');
        Object.assign(div.style, {
          display: 'inline-block',
          position: 'relative',
          verticalAlign: 'top',
          marginRight: this.calculateMargin(block, index, blocks)
        });

        // 创建状态方块
        const statusBlock = document.createElement('div');
        Object.assign(statusBlock.style, {
          width: '40px',
          height: '100px',
          backgroundColor: this.getBlockColor(block.status),
          borderRadius: '4px',
          position: 'relative'
        });

        // 添加列号
        const columnNumber = document.createElement('div');
        console.log('所选区域',this.selectedArea)
        if(this.selectedArea == 3 || this.selectedArea == 4){
          columnNumber.textContent = length--
        }else {
          columnNumber.textContent = index + 1;
        }
        Object.assign(columnNumber.style, {
          position: 'absolute',
          top: '5px',
          left: '5px',
          color: 'white',
          fontSize: '14px',
          fontWeight: 'bold'
        });

        // 添加状态文字
        const statusText = document.createElement('div');
        statusText.textContent = this.getStatusText(block.status);
        Object.assign(statusText.style, {
          position: 'absolute',
          bottom: '5px',
          left: '50%',
          transform: 'translateX(-50%)',
          color: 'white',
          fontSize: '12px'
        });

        statusBlock.appendChild(columnNumber);
        statusBlock.appendChild(statusText);
        div.appendChild(statusBlock);

        // 添加连接线
        this.addConnectionLines(div, block, index, blocks);
        container.appendChild(div);
      });
    },

    calculateMargin(block, index, blocks) {
      if (index === blocks.length - 1) return '0';
      const nextBlock = blocks[index + 1];
      const shouldConnect = block.rightConnected && nextBlock.leftConnected;
      return shouldConnect ? '0' : '10px';
    },

    addConnectionLines(div, block, index, blocks) {
      // 左侧连接线
      if (index > 0 && block.leftConnected) {
        const leftLine = document.createElement('div');
        Object.assign(leftLine.style, {
          position: 'absolute',
          left: '-6px',
          top: '50%',
          width: '6px',
          height: '2px',
          backgroundColor: '#409EFF'
        });
        div.appendChild(leftLine);
      }

      // 右侧连接线
      if (block.rightConnected && index < blocks.length - 1) {
        const rightLine = document.createElement('div');
        Object.assign(rightLine.style, {
          position: 'absolute',
          right: '-6px',
          top: '50%',
          width: '6px',
          height: '2px',
          backgroundColor: '#409EFF'
        });
        div.appendChild(rightLine);
      }
    },

    getStatusText(status) {
      switch (status) {
        case 0: return '未到位';
        case 1: return '左到位';
        case 2: return '右到位';
        case 3: return '全到位';
        default: return '';
      }
    },

    getBlockColor(status) {
      switch (status) {
        case 0: return '#ff4d4d'; // 红色-未到位
        case 1: return '#409EFF'; // 蓝色-左到位
        case 2: return '#4467ff'; // 深蓝-右到位
        case 3: return '#16ab4e'; // 绿色-全到位
        default: return '#ddd';
      }
    },

    refreshPage() {
      // window.location.reload()
      // this.$router.push('/iot/warehouse/real-time_data_display')
      clearInterval(this.refreshInterval)
      this.currentTime = new Date().toLocaleTimeString()
      this.randomNumber = Math.random()
      this.countdown = 2
      this.startCountdown()
      this.getIpList();
      // 初始化时绘制方块
      this.drawBlocks();
      // 监听列状态变化
      this.$watch('currentData.ColumnStatus', this.drawBlocks);
    },
    refreshNow() {
      // window.location.reload()
      // this.$router.push('/iot/warehouse/real-time_data_display')
      clearInterval(this.refreshInterval)
      this.refreshPage()
      this.startCountdown()
      this.getIpList();
      // 初始化时绘制方块
      this.drawBlocks();
      // 监听列状态变化
      this.$watch('currentData.ColumnStatus', this.drawBlocks);
    },
    startCountdown() {
      this.refreshInterval = setInterval(() => {
        this.countdown--
        if (this.countdown <= 0) {
          this.refreshPage()
        }
      }, 60000)
    },

  },
  beforeDestroy() {
    if (this.refreshInterval) {
      clearInterval(this.refreshInterval)
    }
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
  width: 80%;
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


/* 新增样式 */
.block-container {
  padding: 15px;
  background-color: #f0f2f5;
  border-radius: 4px;
  min-height: 120px;
}

/* 连接线动画效果 */
@keyframes connect {
  from { background-position: 0 0; }
  to { background-position: 10px 0; }
}
</style>
