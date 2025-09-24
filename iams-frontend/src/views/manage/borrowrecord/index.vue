<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="用户名" prop="userName">
        <el-input v-model="queryParams.userName" placeholder="请输入借阅用户的用户名" clearable @keyup.enter="handleQuery" />
      </el-form-item>

      <el-form-item label="档案档号" prop="archiveDanghao">
        <el-input v-model="queryParams.archiveDanghao" placeholder="请输入借阅的档案档号" clearable @keyup.enter="handleQuery" />
      </el-form-item>

      <el-form-item label="借阅状态" prop="status">

<!--        <el-input v-model="queryParams.status" placeholder="请输入借阅记录的状态" clearable @keyup.enter="handleQuery" />-->
                <el-select v-model="queryParams.status" placeholder="请选择借阅状态" clearable>
                  <el-option
                      v-for="dict in iams_borrow_status"
                      :key="dict.value"
                      :label="dict.label"
                      :value="String(dict.value)"
                  />
                </el-select>
      </el-form-item>

      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <!--      <el-col :span="1.5">-->
      <!--        <el-button-->
      <!--            type="primary"-->
      <!--            plain-->
      <!--            icon="Plus"-->
      <!--            @click="handleAdd"-->
      <!--            v-hasPermi="['manage:record:add']"-->
      <!--        >新增</el-button>-->
      <!--      </el-col>-->

      <el-col :span="1.5">
        <el-button type="warning" plain icon="Download" @click="handleExport"
          v-hasPermi="['manage:record:export']">导出</el-button>
      </el-col>

      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="recordList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="30" align="center" />

      <!--      <el-table-column label="用户ID" align="center" prop="userId" />-->
      <el-table-column label="用户名" align="center" prop="userName" />
      <!-- <el-table-column label="部门名" align="center" prop="userDepartment" /> -->
      <!--      <el-table-column label="档案ID" align="center" prop="archiveId" />-->
      <!-- <el-table-column label="档案名" align="center" prop="archiveName" /> -->
      <el-table-column label="档案档号" align="center" prop="archiveDangHao" width="200" />

      <el-table-column label="借阅期限" align="center" width="200">
        <template #default="scope">
          <span>{{ parseTime(scope.row.startDate, '{y}-{m}-{d}') }} - {{ parseTime(scope.row.endDate, '{y}-{m}-{d}')
          }}</span>
        </template>
      </el-table-column>

      <el-table-column label="借阅目的" align="center" prop="purpose" width="150" />
      <el-table-column label="借阅状态" align="center" prop="status" width="80">
        <!--        <template #default="scope">-->
        <!--          <dict-tag :options="iams_borrow_status" :value="scope.row.status"/>-->
        <!--        </template>-->
      </el-table-column>

      <el-table-column label="实际借阅期限" align="center" width="200">
        <template #default="scope">
          <span>{{ parseTime(scope.row.borrowDate, '{y}-{m}-{d}') }} - {{ parseTime(scope.row.returnDate,
            '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>

      <el-table-column label="逾期天数" align="center" width="90">
        <template #default="scope">
          <!-- 借阅状态是'已逾期'时显示逾期天数 -->
          <span v-if="scope.row.status === '已逾期'">{{ calculateOverdueDays(scope.row.endDate) }}</span>
          <!-- 其他情况显示 '-' -->
          <span v-else>-</span>
        </template>
      </el-table-column>

      <el-table-column label="备注" align="center" prop="remark" />

      <el-table-column label="操作" align="center" width="150" class-name="small-padding fixed-width" v-hasRole="['commonUser']">
        <template #default="scope">
          <!-- 状态包含"审批"时：显示催办、删除、借阅审批进度 -->
          <template v-if="scope.row.status && scope.row.status.includes('审批')">
            <el-button link type="primary" icon="Bell" @click="handleReminder(scope.row)">
              催办
            </el-button>
            <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)"
              v-hasPermi="['manage:record:remove']">
              取消
            </el-button>
            <el-button link type="info" icon="Clock" @click="handleProgress(scope.row)">
              借阅审批进度
            </el-button>
          </template>

          <!-- 状态是"已批准"时：显示查看、借阅审批进度 -->
          <template v-else-if="scope.row.status === '已批准'">
            <el-button link type="primary" icon="View" @click="handleDetail(scope.row)">
              查看
            </el-button>
            <el-button link type="info" icon="Clock" @click="handleProgress(scope.row)">
              借阅审批进度
            </el-button>
          </template>

          <!-- 状态是"已逾期"、"已结束"时：显示借阅审批进度 -->
          <template v-else-if="scope.row.status === '已逾期' || scope.row.status === '已结束' || scope.row.status === '已归还'">
            <el-button link type="info" icon="Clock" @click="handleProgress(scope.row)">
              借阅审批进度
            </el-button>
          </template>

          <!-- 状态是"已拒绝"时：显示借阅审批进度 -->
          <template v-else-if="scope.row.status === '已拒绝'">
            <el-button link type="info" icon="Clock" @click="handleProgress(scope.row)">
              借阅审批进度
            </el-button>
          </template>

          <!-- 状态是"申请过期"时：显示借阅审批进度 -->
          <template v-else-if="scope.row.status === '申请过期'">
            <el-button link type="info" icon="Clock" @click="handleProgress(scope.row)">
              借阅审批进度
            </el-button>
          </template>

          <!-- 其他状态：显示所有操作（保持原有逻辑作为默认） -->
          <!-- <template v-else>
            <el-button
                link
                type="primary"
                icon="Delete"
                @click="handleDelete(scope.row)"
                v-hasPermi="['manage:record:remove']">
              删除
            </el-button>
            <el-button
                link
                type="primary"
                icon="View"
                @click="handleDetail(scope.row)">
              查看
            </el-button>
            <el-button
                link
                type="primary"
                icon="Bell"
                @click="handleReminder(scope.row)">
              催办
            </el-button>
          </template> -->
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 添加或修改借阅记录对话框 -->
    <el-dialog :title="title" v-model="open" width="500px" append-to-body>
      <el-form ref="recordRef" :model="form" :rules="rules" label-width="80px">

        <el-form-item label="用户ID" prop="userId">
          <el-input v-model="form.userId" placeholder="请输入借阅用户的 ID" />
        </el-form-item>
        <el-form-item label="用户名" prop="userName">
          <el-input v-model="form.userName" placeholder="请输入借阅用户的用户名" />
        </el-form-item>
        <el-form-item label="用户部门" prop="userDepartment">
          <el-input v-model="form.userDepartment" placeholder="请输入借阅用户所在的部门" />
        </el-form-item>
        <!--        <el-form-item label="档案ID" prop="archiveId">-->
        <!--          <el-input v-model="form.archiveId" placeholder="请输入档案的 ID" />-->
        <!--        </el-form-item>-->
        <el-form-item label="档案名" prop="archiveName">
          <el-input v-model="form.archiveName" placeholder="请输入档案名" />
        </el-form-item>
        <el-form-item label="档号" prop="archiveDanghao">
          <el-input v-model="form.archiveDangHao" placeholder="请输入档号" />
        </el-form-item>

        <el-form-item label="借阅目的" prop="purpose">
          <el-input v-model="form.purpose" type="textarea" placeholder="请输入内容" />
        </el-form-item>

        <el-form-item label="开始日期" prop="startDate">
          <el-date-picker clearable v-model="form.startDate" type="date" value-format="YYYY-MM-DD" placeholder="开始日期">
          </el-date-picker>
        </el-form-item>
        <el-form-item label="结束日期" prop="endDate">
          <el-date-picker clearable v-model="form.endDate" type="date" value-format="YYYY-MM-DD"
            placeholder="请选择借阅结束日期">
          </el-date-picker>
        </el-form-item>

        <el-form-item label="启动流程" prop="processName">
          <el-select v-model="form.processName" placeholder="请选择待启动流程" clearable>
            <el-option v-for="dict in Modeler" :label="dict.name" :value="dict.key" />
          </el-select>
        </el-form-item>

        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" placeholder="请输入内容" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 借阅审批进度对话框 -->
    <el-dialog title="借阅审批进度" v-model="progressOpen" width="600px" append-to-body @close="closeProgressDialog">
      <div class="progress-container">
        <div class="record-info">
          <p><strong>档案名称：</strong>{{ currentRecord.archiveName }}</p>
          <p><strong>申请人：</strong>{{ currentRecord.userName }}</p>
          <!-- <p><strong>申请部门：</strong>{{ currentRecord.userDepartment }}</p> -->
          <p><strong>当前状态：</strong>
            <el-tag :type="getStatusTagType(currentRecord.status)" effect="dark">
              {{ currentRecord.status }}
            </el-tag>
          </p>
        </div>

        <el-divider></el-divider>

        <div class="custom-progress">
          <div class="progress-step">
            <div class="step-node completed">
              <div class="node-circle"></div>
            </div>
            <div class="step-content">
              <div class="step-title completed">开始借阅</div>
              <div class="step-description">用户提交借阅申请</div>
            </div>
          </div>

          <div class="step-line" :class="{
            'line-completed': progressSteps.line1Completed,
            'line-error': progressSteps.line1Error
          }"></div>

          <div class="progress-step">
            <div class="step-node" :class="{
              'completed': progressSteps.step2Completed,
              'current': progressSteps.step2Current,
              'error': progressSteps.step2Error
            }">
              <div class="node-circle"></div>
            </div>
            <div class="step-content">
              <div class="step-title" :class="{
                'completed': progressSteps.step2Completed,
                'current': progressSteps.step2Current,
                'error': progressSteps.step2Error
              }">{{ progressSteps.step2Title }}</div>
              <div class="step-description">{{ progressSteps.step2Description }}</div>
            </div>
          </div>

          <div class="step-line" :class="{
            'line-completed': progressSteps.line2Completed,
            'line-error': progressSteps.line2Error
          }"></div>

          <div class="progress-step">
            <div class="step-node" :class="{
              'completed': progressSteps.step3Completed,
              'error': progressSteps.step3Error
            }">
              <div class="node-circle"></div>
            </div>
            <div class="step-content">
              <div class="step-title" :class="{
                'completed': progressSteps.step3Completed,
                'error': progressSteps.step3Error
              }">{{ progressSteps.step3Title }}</div>
              <div class="step-description">{{ progressSteps.step3Description }}</div>
            </div>
          </div>
        </div>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="closeProgressDialog">关 闭</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="Record">
// 将字典值改为与数据库一致的中文
const iams_borrow_status = ref([
  { label: '待审批', value: '待审批' },
  { label: '已批准', value: '已批准' },
  { label: '驳回', value: '驳回' },
  { label: '已结束', value: '已结束' },
  { label: '已逾期', value: '已逾期' },
  { label: '已归还', value: '已归还' }
])
import { listRecord, getRecord, delRecord, addRecord, updateRecord, expediteRecord } from "@/api/manage/borrowrecord";
import { getUserInfo, listDefinition } from "@/api/activiti/definition"
import { ref, onMounted,onActivated,reactive, toRefs, getCurrentInstance, watch } from "vue";
import { useRoute } from 'vue-router';

const { proxy } = getCurrentInstance();

const isInputDisabled = ref(false)

const route = useRoute();
const queryParams_router = ref({
  archiveId: route.query.id,
  archiveName: route.query.name,
});

const queryParams_activitiList = reactive({
  pageNum: 1,
  pageSize: 10,
  id: null,
  rev: null,
  name: null,
  type: null,
});

const recordList = ref([]);
const open = ref(false);
const loading = ref(true);
const showSearch = ref(true);
const ids = ref([]);
const single = ref(true);
const multiple = ref(true);
const total = ref(0);
const title = ref("");

// 借阅审批进度相关状态
const progressOpen = ref(false);
const currentRecord = ref({});
const progressSteps = ref({
  active: 0,
  processStatus: 'process',
  step2Title: '审核员审批',
  step2Description: '等待审核员审批',
  step2Status: 'process',
  step3Title: '审批完成',
  step3Description: '流程结束',
  step3Status: 'wait'
});

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    userName: null,
    archiveDanghao: null,
    status: null,
    overdueDays: null
  },
  rules: {
    // userName: [
    //   {required: true, message: "借阅用户的用户名不能为空", trigger: "blur"}
    // ],
    // archiveDanghao: [
    //   {required: true, message: "借阅的档案档号不能为空", trigger: "blur"}
    // ],
    startDate: [
      { required: true, message: "借阅开始日期不能为空", trigger: "blur" }
    ],
    endDate: [
      { required: true, message: "借阅结束日期不能为空", trigger: "blur" }
    ],
    processName: [
      { required: true, message: "流程名称不能为空", trigger: "blur" }
    ],
    purpose: [
      { required: true, message: "借阅目的不能为空", trigger: "blur" }
    ],
  }
});

const { queryParams, form, rules } = toRefs(data);

/** 查询借阅记录列表 */
function getList() {
  loading.value = true;
  listRecord(queryParams.value).then(response => {
    recordList.value = response.rows;
    total.value = response.total;
    loading.value = false;
  });
}

onActivated(() => {
  // 当从缓存中重新激活时刷新数据
  getList();
});

// 取消按钮
function cancel() {
  open.value = false;
  reset();
  proxy.$router.push({ path: "/manage/record" });
}

// 表单重置
function reset() {
  form.value = {
    id: null,
    userId: null,
    userName: null,
    userDepartment: null,

    archiveId: null,
    archiveName: null,
    archiveDangHao: null,
    purpose: null,

    startDate: null,
    endDate: null,
    processName: null,

    remark: null,
  };
  proxy.resetForm("recordRef");
}

/** 搜索按钮操作 */
function handleQuery() {
  queryParams.value.pageNum = 1;
  getList();
}

/** 重置按钮操作 */
function resetQuery() {
  proxy.resetForm("queryRef");
  handleQuery();
}

// 多选框选中数据
function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.id);
  single.value = selection.length != 1;
  multiple.value = !selection.length;
}

const Modeler = ref([]);
const total_activiti = ref(0);

/** 新增按钮操作 */
function handleAdd() {
  reset();
  open.value = true;
  title.value = "添加借阅记录";
  isInputDisabled.value = false;

  getUserInfo().then(response => {
    form.value.userId = response.userId;
    form.value.userName = response.userName;
    form.value.userDepartment = response.deptName;
  });

  listDefinition(queryParams_activitiList).then(response => {
    Modeler.value = response.rows;
    total_activiti.value = response.total;
  });
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset();
  const _id = row.id || ids.value
  getRecord(_id).then(response => {
    form.value = response.data;
    open.value = true;
    title.value = "修改借阅记录";
  });
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["recordRef"].validate(valid => {
    if (valid) {
      if (form.value.id != null) {
        updateRecord(form.value).then(response => {
          proxy.$modal.msgSuccess("修改成功");
          open.value = false;
          getList();
        });
      } else {
        addRecord(form.value).then(response => {
          proxy.$modal.msgSuccess("新增成功");
          open.value = false;
          getList();
        });
      }
    }
  });
}

/** 删除按钮操作 */
function handleDelete(row) {
  const _ids = row.id || ids.value;
  proxy.$modal.confirm('是否确认删除借阅记录编号为"' + _ids + '"的数据项？').then(function () {
    return delRecord(_ids);
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess("删除成功");
  }).catch(() => {
  });
}

/** 催办按钮操作 */
function handleReminder(row) {
  const _id = row.id || ids.value
  proxy.$modal.confirm('是否确认催办借阅记录编号为"' + _id + '"的数据项？').then(function () {
    return expediteRecord(_id);
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess("催办成功");
  }).catch(() => {
  });
}

function handleDetail(row) {
  ArchivePermission(row.archiveId).then(response => {
    proxy.$router.push({ path: "/manage/archive/arc-detail", query: { id: row.archiveId } });
  }).catch(error => {
  });
}

/** 借阅审批进度按钮操作 */
function handleProgress(row) {
  currentRecord.value = { ...row };
  updateProgressSteps(row.status);
  progressOpen.value = true;
}

/** 根据状态更新进度步骤 */
function updateProgressSteps(status) {
  // 重置步骤状态
  progressSteps.value = {
    // 第一个节点始终完成
    step1Completed: true,

    // 第一条横线
    line1Completed: false,
    line1Error: false,

    // 第二个节点
    step2Title: '审核员审批',
    step2Description: '等待审核员审批',
    step2Completed: false,
    step2Current: false,
    step2Error: false,

    // 第二条横线
    line2Completed: false,
    line2Error: false,

    // 第三个节点
    step3Title: '审批完成',
    step3Description: '流程结束',
    step3Completed: false,
    step3Error: false
  };

  switch (status) {
    case '待审批':
      progressSteps.value.line1Completed = true; // 第一条线绿色
      progressSteps.value.step2Current = true; // 第二个节点当前进行中（绿色）
      progressSteps.value.step2Description = '等待审核员审批';
      break;

    case '已批准':
    case '已逾期':
    case '已归还':
    case '已结束':
      progressSteps.value.line1Completed = true; // 第一条线绿色
      progressSteps.value.step2Completed = true; // 第二个节点完成（绿色）
      progressSteps.value.step2Description = '审核员已批准';
      progressSteps.value.line2Completed = true; // 第二条线绿色
      progressSteps.value.step3Completed = true; // 第三个节点完成（绿色）
      progressSteps.value.step3Description = '已完成';
      break;

    case '已拒绝':
      progressSteps.value.line1Completed = true; // 第一条线绿色
      progressSteps.value.step2Completed = true; // 第二个节点完成（绿色）
      progressSteps.value.step2Description = '审核员已审批';
      progressSteps.value.line2Error = true; // 第二条线红色
      progressSteps.value.step3Error = true; // 第三个节点红色
      progressSteps.value.step3Description = '已拒绝';
      break;

    case '申请过期':
      progressSteps.value.line1Error = true; // 第一条线红色
      progressSteps.value.step2Error = true; // 第二个节点红色
      progressSteps.value.step2Description = '申请过期';
      break;

    default:
      // 默认状态
      break;
  }
}

/** 关闭进度对话框 */
function closeProgressDialog() {
  progressOpen.value = false;
  currentRecord.value = {};
}

/** 获取状态标签类型 */
function getStatusTagType(status) {
  switch (status) {
    case '已批准':
    case '已归还':
    case '已结束':
    case '待审批':
      return 'success';
    case '已拒绝':
    case '申请过期':
    case '已逾期':
      return 'danger';
    default:
      return 'success';
  }
}

/** 导出按钮操作 */
function handleExport() {
  proxy.download('manage/record/export', {
    ...queryParams.value
  }, `record_${new Date().getTime()}.xlsx`)
}

// 在组件作用域内声明状态标记
const hasHandledInitialParams = ref(false)

// 带参数时的附加操作
const handleParamsOperations = () => {
  open.value = true
  form.value.archiveId = route.query.record_id // 使用正确的参数名
  form.value.archiveName = route.query.record_name
  form.value.archiveDangHao = route.query.record_danghao
  isInputDisabled.value = true
  listDefinition(queryParams_activitiList).then(response => {
    Modeler.value = response.rows
    total_activiti.value = response.total
  })
}

import dayjs from 'dayjs';

function calculateOverdueDays(returnDate) {
  const today = dayjs().startOf('day');
  const endDay = dayjs(returnDate).startOf('day');
  const diff = today.diff(endDay, 'day');
  return diff > 0 ? diff : 0;
}

onMounted(() => {
  getList()

  // 处理直接通过URL带参数访问的情况
  if (route.query.record_id && !hasHandledInitialParams.value) {
    handleParamsOperations()
    hasHandledInitialParams.value = true
  }
})

watch(
  () => route.query.record_id, // 根据实际参数名调整
  (newId) => {
    if (route.path === "/manage/record") {
      // 首次处理参数逻辑
      if (newId && !hasHandledInitialParams.value) {
        handleParamsOperations()
        hasHandledInitialParams.value = true
      }
      // 参数清除时关闭弹窗
      if (!newId && open.value) {
        open.value = false
      }
    }
  },
  { immediate: true }
)

import { onBeforeUnmount } from 'vue'
import { onBeforeRouteLeave } from 'vue-router'
import { ArchivePermission, getArchiveIdByMysqlDanghao } from "@/api/manage/archive.js";

onBeforeRouteLeave((to, from) => {
  if (from.path === "/manage/record") {
    // 保留状态用于同路由切换
    // 仅在完全离开路由时重置
    if (to.path !== "/manage/record") {
      hasHandledInitialParams.value = false
      open.value = false
    }
  }
})

onBeforeUnmount(() => {
  hasHandledInitialParams.value = false
  open.value = false
})

</script>

<style scoped>
.progress-container {
  padding: 20px 0;
}

.record-info {
  margin-bottom: 20px;
  padding: 16px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.record-info p {
  margin: 8px 0;
  line-height: 1.5;
}

.custom-progress {
  display: flex;
  align-items: flex-start;
  margin-top: 20px;
  padding: 20px 0;
}

.progress-step {
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
}

.step-node {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 10px;
  position: relative;
  z-index: 2;
}

.node-circle {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background-color: #e4e7ed;
  border: 2px solid #e4e7ed;
  transition: all 0.3s ease;
}

/* 已完成的节点 */
.step-node.completed .node-circle {
  background-color: #67c23a;
  border-color: #67c23a;
}

/* 当前进行中的节点 */
.step-node.current .node-circle {
  background-color: #67c23a;
  border-color: #67c23a;
  box-shadow: 0 0 0 3px rgba(103, 194, 58, 0.2);
}

/* 错误状态的节点 */
.step-node.error .node-circle {
  background-color: #f56c6c;
  border-color: #f56c6c;
}

.step-content {
  text-align: center;
  min-width: 120px;
}

.step-title {
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 4px;
  color: #909399;
  transition: color 0.3s ease;
}

.step-title.completed,
.step-title.current {
  color: #67c23a;
}

.step-title.error {
  color: #f56c6c;
}

.step-description {
  font-size: 12px;
  color: #c0c4cc;
  line-height: 1.4;
}

.step-line {
  flex: 1;
  height: 2px;
  background-color: #e4e7ed;
  margin: 0 20px;
  position: relative;
  top: 10px;
  transition: background-color 0.3s ease;
}

.step-line.line-completed {
  background-color: #67c23a;
}

.step-line.line-error {
  background-color: #f56c6c;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .step-content {
    min-width: 80px;
  }

  .step-line {
    margin: 0 10px;
  }

  .step-title {
    font-size: 13px;
  }

  .step-description {
    font-size: 11px;
  }
}
</style>