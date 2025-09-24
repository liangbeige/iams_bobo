<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="任务名称" prop="name">
        <el-input
          v-model="queryParams.name"
          placeholder="请输入任务名称"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-input
          v-model="queryParams.status"
          placeholder="请输入状态"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="创建时间" prop="createdDate">
        <el-date-picker clearable
          v-model="queryParams.createdDate"
          type="date"
          value-format="YYYY-MM-DD"
          placeholder="请选择创建时间">
        </el-date-picker>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="Download"
          @click="handleExport"
          v-hasPermi="['manage:approval:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getTaskList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="approvalList" @selection-change="handleSelectionChange">
<!--      <el-table-column type="selection" width="55" align="center" />-->
      <el-table-column label="任务" align="center" prop="instanceName" width="180" />
      <el-table-column label="当前环节" align="center" prop="name" />
      <el-table-column label="申请人" align="center" prop="applyUserName" />
      <el-table-column label="档案名称" align="center" prop="archiveName" />
      <el-table-column label="借阅期限" align="center" width="200">
        <template #default="scope">
          <span>{{ parseTime(scope.row.startTime, '{y}-{m}-{d}') }} - {{ parseTime(scope.row.endTime, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="目的" align="center" prop="purpose" />
<!--      <el-table-column label="任务状态" align="center" prop="taskStatus" />-->
      <el-table-column label="创建时间" align="center" prop="applyStartTime" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.applyStartTime, '{y}-{m}-{d} {h}:{i}:{s}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="examineAndApprove2(scope.row)" v-hasPermi="['manage:approval:edit']">审批</el-button>
<!--          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['manage:approval:remove']">删除</el-button>-->
          <el-button link type="primary" icon="Edit" @click="historyFormShow(scope.row)" v-hasPermi="['manage:approval:edit']">查看历史</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total>0"
      :total="total"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      @pagination="getTaskList"
    />

<!--     添加或修改借阅审批对话框-->
    <el-dialog :title="title" v-model="open" width="500px" append-to-body>
      <el-form ref="approvalRef" :model="form" :rules="rules" label-width="150px">
        <el-form-item label="借阅申请的ID" prop="applyId">
          <el-input v-model="form.applyId" placeholder="请输入借阅申请的ID" />
        </el-form-item>
        <el-form-item label="审批人的ID" prop="approverId">
          <el-input v-model="form.approverId" placeholder="请输入审批人的ID" />
        </el-form-item>
        <el-form-item label="审批人的用户名" prop="approverName">
          <el-input v-model="form.approverName" placeholder="请输入审批人的用户名" />
        </el-form-item>
        <el-form-item label="审批时间" prop="approvalTime">
          <el-date-picker clearable
            v-model="form.approvalTime"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="请选择审批时间">
          </el-date-picker>
        </el-form-item>
        <el-form-item label="审批意见" prop="approvalComment">
          <el-input v-model="form.approvalComment" type="textarea" placeholder="请输入内容" />
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

    <component
        :is="currentComponent"
        v-if="currentComponent"
        v-model="dialogVisible"
        @confirm="handleConfirm"
    />

    <!-- 历史记录弹窗 -->
    <el-dialog
      title="审批历史"
      v-model="historyDialogVisible"
      width="900px"
      append-to-body
    >
      <el-table :data="historyList" style="width: 100%">
        <el-table-column type="index" label="序号" width="50" align="center" />
        <el-table-column prop="taskName" label="任务名" align="center" />
        <el-table-column prop="approvalResult" label="审批结果" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.approvalResult === '通过' ? 'success' : 'danger'" v-if="scope.row.approvalResult">
              {{ scope.row.approvalResult }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="approvalComment" label="审批意见" align="center" show-overflow-tooltip />
        <el-table-column label="开始时间" align="center">
          <template #default="scope">
            <span>{{ parseTime(scope.row.startTime, '{y}-{m}-{d} {h}:{i}:{s}') }}</span>
          </template>
        </el-table-column>
        <el-table-column label="结束时间" align="center">
          <template #default="scope">
            <span>{{ parseTime(scope.row.endTime, '{y}-{m}-{d} {h}:{i}:{s}') }}</span>
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="historyDialogVisible = false">关 闭</el-button>
        </div>
      </template>
    </el-dialog>

  </div>
</template>

<script setup name="Approval">
import {
  listApproval,
  getApproval,
  delApproval,
  addApproval,
  updateApproval,
  listTaskList, formDataShow, formDataSave, formHistoryDataShow
} from "@/api/manage/borrowapproval";

const { proxy } = getCurrentInstance();

// 动态组件相关变量
const currentComponent = shallowRef(null);
const approvalList = ref([]);

const open = ref(false);

const open_exam = ref(false);
const loading = ref(true);
const showSearch = ref(true);
const ids = ref([]);
const single = ref(true);
const multiple = ref(true);
const total = ref(0);
const title = ref("");
const id = ref("");




const dialogVisible = ref(false)

const historyDialogVisible = ref(false);
const historyList = ref([]);

function examineAndApprove2(row) {
  dialogVisible.value = true;
  id.value = row.id;
  formDataShow(row.id).then(async response => {

    try {
      // 动态导入组件
      const module = await import('./FormTemplates/borrowArchive.vue');
      currentComponent.value = module.default;
    } catch (error) {
      proxy.$modal.msgError("加载审批表单组件失败：" + error.message);
      dialogVisible.value = false;
    }
  }).catch(error => {
    proxy.$modal.msgError("获取审批表单失败：" + error.message);
    dialogVisible.value = false;
  });
}

const handleConfirm = (formData1) => {

  let dateFromWindowSubmit = [];

  Object.entries(formData1).forEach(([key, value]) => {
    // 构建新的数据结构
    dateFromWindowSubmit.push({
      controlId: key,
      controlValue: value,
      controlIsParam: 'i'
    });
  });
  console.log('Form data:', dateFromWindowSubmit)

  formDataSave(id.value, dateFromWindowSubmit).then(response => {
    proxy.$modal.msgSuccess("审批成功");
    open_exam.value = false;
    getTaskList();
  }).catch(error => {
    proxy.$modal.msgError("审批失败：" + error.message);
  });
}


function historyFormShow(row){

  formHistoryDataShow(row.id).then(async response => {
    // 过滤出包含 approvalResult 或 approvalComment 的记录
    historyList.value = response.data.filter(item => 
      item.approvalResult || item.approvalComment
    );
    // 显示弹窗
    historyDialogVisible.value = true;
  }).catch(error => {
    proxy.$modal.msgError("获取历史信息失败：" + error.message);
  });
}

const data = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    name: null,
    status: null,
    createdDate: null
  },
  rules: {
    applyId: [
      { required: true, message: "借阅申请的ID不能为空", trigger: "blur" }
    ],
  }
});

const { queryParams, rules } = toRefs(data);

/** 查询借阅审批列表 */
function getTaskList() {
  loading.value = true;
  listTaskList(queryParams.value).then(response => {
    approvalList.value = response.rows.sort((a, b) => {
      // 按创建时间降序排列（最新的在前面）
      return new Date(b.applyStartTime) - new Date(a.applyStartTime);
      // 如果需要升序排列（最早的在前），使用：
      // return new Date(a.applyStartTime) - new Date(b.applyStartTime);
    });
    total.value = response.total;
    loading.value = false;
  });
}

// 取消按钮
function cancel() {
  open.value = false;
  reset();
}

// 表单重置
function reset() {
  form.value = {
    id: null,
    applyId: null,
    approverId: null,
    approverName: null,
    approvalTime: null,
    approvalComment: null,
    createTime: null,
    createBy: null,
    updateTime: null,
    updateBy: null,
    remark: null
  };
  proxy.resetForm("approvalRef");
}

/** 搜索按钮操作 */
function handleQuery() {
  queryParams.value.pageNum = 1;

  getTaskList()
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

/** 新增按钮操作 */
function handleAdd() {
  reset();
  open.value = true;
  title.value = "添加借阅审批";
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset();
  const _id = row.id || ids.value
  getApproval(_id).then(response => {
    form.value = response.data;
    open.value = true;
    title.value = "修改借阅审批";
  });
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["approvalRef"].validate(valid => {
    if (valid) {
      if (form.value.id != null) {
        updateApproval(form.value).then(response => {
          proxy.$modal.msgSuccess("修改成功");
          open.value = false;

          getTaskList()
        });
      } else {
        addApproval(form.value).then(response => {
          proxy.$modal.msgSuccess("新增成功");
          open.value = false;

          getTaskList()
        });
      }
    }
  });
}

/** 删除按钮操作 */
function handleDelete(row) {
  const _ids = row.id || ids.value;
  proxy.$modal.confirm('是否确认删除借阅审批编号为"' + _ids + '"的数据项？').then(function() {
    return delApproval(_ids);
  }).then(() => {
    getTaskList()
    proxy.$modal.msgSuccess("删除成功");
  }).catch(() => {});
}

/** 导出按钮操作 */
function handleExport() {
  proxy.download('manage/approval/export', {
    ...queryParams.value
  }, `approval_${new Date().getTime()}.xlsx`)
}

getTaskList()
</script>
