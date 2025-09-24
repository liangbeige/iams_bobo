<template>
   <div class="app-container">
      <el-form :model="queryParams" ref="queryRef" :inline="true">
         <el-form-item label="登录地址" prop="ipaddr">
            <el-input
               v-model="queryParams.ipaddr"
               placeholder="请输入登录地址"
               clearable
               style="width: 200px"
               @keyup.enter="handleQuery"
            />
         </el-form-item>
         <el-form-item label="用户名称" prop="userName">
            <el-input
               v-model="queryParams.userName"
               placeholder="请输入用户名称"
               clearable
               style="width: 200px"
               @keyup.enter="handleQuery"
            />
         </el-form-item>
         <el-form-item>
            <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
            <el-button icon="Refresh" @click="resetQuery">重置</el-button>
           <el-button
               type="success"
               icon="Refresh"
               @click="handleRefresh"
               :loading="refreshing"
           >
             刷新在线状态
           </el-button>
         </el-form-item>
      </el-form>
      <el-table
         v-loading="loading"
         :data="onlineList.slice((pageNum - 1) * pageSize, pageNum * pageSize)"
         style="width: 100%;"
      >
         <el-table-column label="序号" width="50" type="index" align="center">
            <template #default="scope">
               <span>{{ (pageNum - 1) * pageSize + scope.$index + 1 }}</span>
            </template>
         </el-table-column>
         <el-table-column label="会话编号" align="center" prop="tokenId" :show-overflow-tooltip="true" />
         <el-table-column label="登录名称" align="center" prop="userName" :show-overflow-tooltip="true" />
         <el-table-column label="所属部门" align="center" prop="deptName" :show-overflow-tooltip="true" />
         <el-table-column label="主机" align="center" prop="ipaddr" :show-overflow-tooltip="true" />
         <el-table-column label="登录地点" align="center" prop="loginLocation" :show-overflow-tooltip="true" />
         <el-table-column label="操作系统" align="center" prop="os" :show-overflow-tooltip="true" />
         <el-table-column label="浏览器" align="center" prop="browser" :show-overflow-tooltip="true" />
         <el-table-column label="登录时间" align="center" prop="loginTime" width="180">
            <template #default="scope">
               <span>{{ parseTime(scope.row.loginTime) }}</span>
            </template>
         </el-table-column>
        <el-table-column label="在线时长" align="center" width="120">
          <template #default="scope">
            <el-tag
                :type="getDurationTagType(scope.row.loginDuration)"
                effect="light"
            >
              {{ formatDuration(scope.row.loginDuration) }}
            </el-tag>
          </template>
        </el-table-column>
         <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
            <template #default="scope">
               <el-button link type="primary" icon="Delete" @click="handleForceLogout(scope.row)" v-hasPermi="['monitor:online:forceLogout']">强退</el-button>
            </template>
         </el-table-column>
      </el-table>

      <pagination v-show="total > 0" :total="total" v-model:page="pageNum" v-model:limit="pageSize" />
   </div>
</template>

<script setup name="Online">
import { forceLogout, list as initData } from "@/api/monitor/online";

const { proxy } = getCurrentInstance();

const onlineList = ref([]);
const loading = ref(true);
const total = ref(0);
const pageNum = ref(1);
const pageSize = ref(10);


// 定时器引用
let refreshTimer = null;

const queryParams = ref({
  ipaddr: undefined,
  userName: undefined
});


/** 格式化在线时长 */
function formatDuration(seconds) {
  if (!seconds || seconds <= 0) {
    return "刚刚上线";
  }

  const hours = Math.floor(seconds / 3600);
  const minutes = Math.floor((seconds % 3600) / 60);
  const remainingSeconds = seconds % 60;

  let result = "";
  if (hours > 0) {
    result += hours + "小时";
  }
  if (minutes > 0) {
    result += minutes + "分钟";
  }
  if (hours === 0 && minutes === 0) {
    result += remainingSeconds + "秒";
  }

  return result || "刚刚上线";
}

/** 根据在线时长返回标签颜色类型 */
function getDurationTagType(seconds) {
  if (!seconds || seconds <= 0) {
    return "info";
  }

  const hours = seconds / 3600;

  if (hours < 1) {
    return "success";  // 绿色：1小时内
  } else if (hours < 4) {
    return "warning";  // 橙色：1-4小时
  } else if (hours < 8) {
    return "danger";   // 红色：4-8小时
  } else {
    return "";         // 默认色：8小时以上
  }
}


/** 刷新在线状态（手动刷新） */
function handleRefresh() {
  refreshing.value = true;
  initData(queryParams.value).then(response => {
    onlineList.value = response.rows;
    total.value = response.total;
    refreshing.value = false;
    proxy.$modal.msgSuccess("刷新成功");
  }).catch(() => {
    refreshing.value = false;
  });
}

/** 自动刷新在线时长 */
function autoRefresh() {
  // 只有在页面可见且有数据时才自动刷新
  if (document.visibilityState === 'visible' && onlineList.value.length > 0) {
    initData(queryParams.value).then(response => {
      // 静默更新，不显示loading
      onlineList.value = response.rows;
      total.value = response.total;
    }).catch(() => {
      // 静默失败，不提示错误
    });
  }
}


// 生命周期钩子
onMounted(() => {
  getList();

  // 设置自动刷新定时器（每10秒刷新一次）
  refreshTimer = setInterval(autoRefresh, 10000);

  // 监听页面可见性变化
  document.addEventListener('visibilitychange', () => {
    if (document.visibilityState === 'visible') {
      // 页面变为可见时立即刷新一次
      autoRefresh();
    }
  });
});

onUnmounted(() => {
  // 清除定时器
  if (refreshTimer) {
    clearInterval(refreshTimer);
    refreshTimer = null;
  }
});


/** 查询登录日志列表 */
function getList() {
  loading.value = true;
  initData(queryParams.value).then(response => {
    onlineList.value = response.rows;
    total.value = response.total;
    loading.value = false;
  });
}
/** 搜索按钮操作 */
function handleQuery() {
  pageNum.value = 1;
  getList();
}
/** 重置按钮操作 */
function resetQuery() {
  proxy.resetForm("queryRef");
  handleQuery();
}
/** 强退按钮操作 */
function handleForceLogout(row) {
    proxy.$modal.confirm('是否确认强退名称为"' + row.userName + '"的用户?').then(function () {
  return forceLogout(row.tokenId);
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess("删除成功");
  }).catch(() => {});
}

getList();
</script>

<style scoped>
.el-tag {
  font-weight: 500;
}

/* 自定义标签样式 */
.el-tag--success {
  --el-tag-bg-color: #f0f9ff;
  --el-tag-border-color: #67c23a;
  --el-tag-text-color: #529b2e;
}

.el-tag--warning {
  --el-tag-bg-color: #fdf6ec;
  --el-tag-border-color: #e6a23c;
  --el-tag-text-color: #b88230;
}

.el-tag--danger {
  --el-tag-bg-color: #fef0f0;
  --el-tag-border-color: #f56c6c;
  --el-tag-text-color: #c45656;
}
</style>