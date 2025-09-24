<!--<template>-->
<!--  <div class="app-container">-->
<!--    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="100px">-->
<!--      <el-form-item label="档案ID" prop="archiveId">-->
<!--        <el-input-->
<!--          v-model="queryParams.archiveId"-->
<!--          placeholder="请输入档案ID"-->
<!--          clearable-->
<!--          @keyup.enter="handleQuery"-->
<!--        />-->
<!--      </el-form-item>-->

<!--      <el-form-item label="用户ID" prop="userId">-->
<!--        <el-input-->
<!--          v-model="queryParams.userId"-->
<!--          placeholder="请输入用户ID"-->
<!--          clearable-->
<!--          @keyup.enter="handleQuery"-->
<!--        />-->
<!--      </el-form-item>-->

<!--&lt;!&ndash;      <el-form-item label="申请时间" prop="startTime">&ndash;&gt;-->
<!--&lt;!&ndash;        <el-date-picker clearable&ndash;&gt;-->
<!--&lt;!&ndash;          v-model="queryParams.startApplyTime"&ndash;&gt;-->
<!--&lt;!&ndash;          type="date"&ndash;&gt;-->
<!--&lt;!&ndash;          value-format="YYYY-MM-DD"&ndash;&gt;-->
<!--&lt;!&ndash;          placeholder="请选择开始时间">&ndash;&gt;-->
<!--&lt;!&ndash;        </el-date-picker>&ndash;&gt;-->
<!--&lt;!&ndash;      </el-form-item>&ndash;&gt;-->

<!--      <el-form-item>-->
<!--        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>-->
<!--        <el-button icon="Refresh" @click="resetQuery">重置</el-button>-->
<!--      </el-form-item>-->
<!--    </el-form>-->

<!--    <el-row :gutter="10" class="mb8">-->
<!--      <el-col :span="1.5">-->
<!--        <el-button-->
<!--          type="primary"-->
<!--          plain-->
<!--          icon="Plus"-->
<!--          @click="handleAdd"-->
<!--          v-hasPermi="['manage:ArchiveDestroyRecord:add']"-->
<!--        >新增</el-button>-->
<!--      </el-col>-->
<!--      <el-col :span="1.5">-->
<!--        <el-button-->
<!--          type="warning"-->
<!--          plain-->
<!--          icon="Download"-->
<!--          @click="handleExport"-->
<!--          v-hasPermi="['manage:ArchiveDestroyRecord:export']"-->
<!--        >导出</el-button>-->
<!--      </el-col>-->

<!--      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>-->
<!--    </el-row>-->

<!--    <el-table v-loading="loading" :data="ArchiveDestroyRecordList" @selection-change="handleSelectionChange">-->
<!--      <el-table-column type="selection" width="55" align="center" />-->

<!--      <el-table-column label="用户ID" align="center" prop="userId" />-->
<!--      <el-table-column label="用户名" align="center" prop="userName" />-->
<!--      <el-table-column label="用户部门" align="center" prop="userDepartment" />-->

<!--      <el-table-column label="档案ID" align="center" prop="archiveId" />-->
<!--      <el-table-column label="档案名" align="center" prop="archiveName" />-->
<!--      <el-table-column label="档号" align="center" prop="archiveDangHao" />-->

<!--      <el-table-column label="销毁原因" align="center" prop="purpose" />-->
<!--      <el-table-column label="审批状态" align="center" prop="status" />-->

<!--      <el-table-column label="审批发起时间" align="center" prop="startApplyTime" width="180">-->
<!--        <template #default="scope">-->
<!--          <span>{{ parseTime(scope.row.startApplyTime, '{y}-{m}-{d}') }}</span>-->
<!--        </template>-->
<!--      </el-table-column>-->
<!--      <el-table-column label="审批结束时间" align="center" prop="endApplyTime" width="180">-->
<!--        <template #default="scope">-->
<!--          <span>{{ parseTime(scope.row.endApplyTime, '{y}-{m}-{d}') }}</span>-->
<!--        </template>-->
<!--      </el-table-column>-->

<!--      <el-table-column label="备注" align="center" prop="remark" />-->

<!--      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">-->
<!--        <template #default="scope">-->
<!--&lt;!&ndash;          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['manage:ArchiveDestroyRecord:edit']">修改</el-button>&ndash;&gt;-->
<!--          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['manage:ArchiveDestroyRecord:remove']">删除</el-button>-->
<!--        </template>-->
<!--      </el-table-column>-->
<!--    </el-table>-->
<!--    -->
<!--    <pagination-->
<!--      v-show="total>0"-->
<!--      :total="total"-->
<!--      v-model:page="queryParams.pageNum"-->
<!--      v-model:limit="queryParams.pageSize"-->
<!--      @pagination="getList"-->
<!--    />-->

<!--    &lt;!&ndash; 添加或修改档案销毁对话框 &ndash;&gt;-->
<!--    <el-dialog :title="title" v-model="open" width="500px" append-to-body>-->
<!--      <el-form ref="ArchiveDestroyRecordRef" :model="form" :rules="rules" label-width="80px">-->
<!--        <el-form-item label="用户ID" prop="userId">-->
<!--          <el-input v-model="form.userId" placeholder="请输入用户ID" />-->
<!--        </el-form-item>-->
<!--        <el-form-item label="用户名" prop="userName">-->
<!--          <el-input v-model="form.userName" placeholder="请输入用户名" />-->
<!--        </el-form-item>-->
<!--        <el-form-item label="用户部门" prop="userDepartment">-->
<!--          <el-input v-model="form.userDepartment" placeholder="用户部门" />-->
<!--        </el-form-item>-->

<!--        <el-form-item label="档案ID" prop="archiveId">-->
<!--          <el-input v-model="form.archiveId" placeholder="请输入档案的ID" />-->
<!--        </el-form-item>-->
<!--        <el-form-item label="档案名" prop="archiveName">-->
<!--          <el-input v-model="form.archiveName" placeholder="请输入档案名" />-->
<!--        </el-form-item>-->
<!--        <el-form-item label="档号" prop="archiveDanghao">-->
<!--          <el-input v-model="form.archiveDangHao" placeholder="请输入档号" />-->
<!--        </el-form-item>-->

<!--        <el-form-item label="销毁原因" prop="purpose">-->
<!--          <el-input v-model="form.purpose" type="textarea" placeholder="请输入内容" />-->
<!--        </el-form-item>-->

<!--        <el-form-item label="启动流程" prop="processName">-->
<!--          <el-select v-model="form.processName" placeholder="请选择待启动流程" clearable>-->
<!--            <el-option-->
<!--                v-for="dict in Modeler"-->
<!--                :label="dict.name"-->
<!--                :value="dict.key"-->
<!--            />-->
<!--          </el-select>-->
<!--        </el-form-item>-->

<!--        <el-form-item label="备注" prop="remark">-->
<!--          <el-input v-model="form.remark" type="textarea" placeholder="" />-->
<!--        </el-form-item>-->

<!--      </el-form>-->
<!--      <template #footer>-->
<!--        <div class="dialog-footer">-->
<!--          <el-button type="primary" @click="submitForm">确 定</el-button>-->
<!--          <el-button @click="cancel">取 消</el-button>-->
<!--        </div>-->
<!--      </template>-->
<!--    </el-dialog>-->
<!--  </div>-->
<!--</template>-->

<!--<script setup name="ArchiveDestroyRecord">-->
<!--import { listArchiveDestroyRecord, getArchiveDestroyRecord, delArchiveDestroyRecord, addArchiveDestroyRecord, updateArchiveDestroyRecord } from "@/api/manage/ArchiveDestroyRecord";-->

<!--import { useRoute } from 'vue-router';-->
<!--import {ref, onMounted} from "vue";-->
<!--import {listDefinition} from "@/api/activiti/definition.js";-->
<!--import router from "@/router/index.js";-->

<!--const route = useRoute();-->
<!--const queryParams_destroy = ref({-->
<!--  archiveId: route.query.id,-->
<!--  archiveName: route.query.name,-->
<!--  danghao: route.query.danghao,-->
<!--});-->

<!--const { proxy } = getCurrentInstance();-->

<!--const ArchiveDestroyRecordList = ref([]);-->
<!--const open = ref(false);-->
<!--const loading = ref(true);-->
<!--const showSearch = ref(true);-->
<!--const ids = ref([]);-->
<!--const single = ref(true);-->
<!--const multiple = ref(true);-->
<!--const total = ref(0);-->
<!--const title = ref("");-->

<!--const data = reactive({-->
<!--  form: {},-->
<!--  queryParams: {-->
<!--    pageNum: 1,-->
<!--    pageSize: 10,-->
<!--    userId: null,-->
<!--    archiveId: null,-->
<!--    status: null-->
<!--  },-->
<!--  rules: {-->
<!--    archiveId: [-->
<!--      { required: true, message: "档案ID不能为空", trigger: "blur" }-->
<!--    ],-->
<!--    userId: [-->
<!--      { required: true, message: "用户ID不能为空", trigger: "blur" }-->
<!--    ],-->
<!--    purpose: [-->
<!--      { required: true, message: "销毁原因不能为空", trigger: "blur"}-->
<!--    ]-->
<!--  }-->
<!--});-->

<!--const { queryParams, form, rules } = toRefs(data);-->

<!--/** 查询档案销毁列表 */-->
<!--function getList() {-->
<!--  loading.value = true;-->
<!--  listArchiveDestroyRecord(queryParams.value).then(response => {-->
<!--    ArchiveDestroyRecordList.value = response.rows;-->
<!--    total.value = response.total;-->
<!--    loading.value = false;-->
<!--  });-->
<!--}-->

<!--// 取消按钮-->
<!--function cancel() {-->
<!--  open.value = false;-->
<!--  reset();-->
<!--  proxy.$router.push({ path: "/manage/archive/ArchiveDestroyRecord"})-->
<!--}-->

<!--// 表单重置-->
<!--function reset() {-->
<!--  form.value = {-->
<!--    id: null,-->
<!--    userId: null,-->
<!--    userName: null,-->
<!--    userDepartment: null,-->
<!--    archiveId: null,-->
<!--    archiveName: null,-->
<!--    archiveDangHao: null,-->
<!--    purpose: null,-->
<!--    processName: null,-->
<!--  };-->
<!--  proxy.resetForm("ArchiveDestroyRecordRef");-->
<!--}-->

<!--/** 搜索按钮操作 */-->
<!--function handleQuery() {-->
<!--  queryParams.value.pageNum = 1;-->
<!--  getList();-->
<!--}-->

<!--/** 重置按钮操作 */-->
<!--function resetQuery() {-->
<!--  proxy.resetForm("queryRef");-->
<!--  handleQuery();-->
<!--}-->

<!--// 多选框选中数据-->
<!--function handleSelectionChange(selection) {-->
<!--  ids.value = selection.map(item => item.id);-->
<!--  single.value = selection.length != 1;-->
<!--  multiple.value = !selection.length;-->
<!--}-->

<!--const Modeler = ref([]);-->
<!--const total_activiti = ref(0);-->
<!--const queryParams_activitiList = reactive({-->
<!--  pageNum: 1,-->
<!--  pageSize: 5,-->
<!--  id: null,-->
<!--  rev: null,-->
<!--  name: null,-->
<!--  type: null,-->
<!--});-->

<!--/** 新增按钮操作 */-->
<!--function handleAdd() {-->
<!--  reset();-->
<!--  open.value = true;-->
<!--  title.value = "添加档案销毁";-->

<!--  listDefinition(queryParams_activitiList).then(response => {-->
<!--    Modeler.value = response.rows;-->
<!--    total_activiti.value = response.total;-->
<!--  });-->
<!--}-->




<!--/** 修改按钮操作 */-->
<!--function handleUpdate(row) {-->
<!--  reset();-->
<!--  const _id = row.id || ids.value-->
<!--  getArchiveDestroyRecord(_id).then(response => {-->
<!--    form.value = response.data;-->
<!--    open.value = true;-->
<!--    title.value = "修改档案销毁";-->
<!--  });-->
<!--}-->

<!--/** 提交按钮 */-->
<!--function submitForm() {-->
<!--  proxy.$refs["ArchiveDestroyRecordRef"].validate(valid => {-->
<!--    if (valid) {-->
<!--      if (form.value.id != null) {-->
<!--        updateArchiveDestroyRecord(form.value).then(response => {-->
<!--          proxy.$modal.msgSuccess("修改成功");-->
<!--          open.value = false;-->
<!--          getList();-->
<!--        });-->
<!--      } else {-->
<!--        addArchiveDestroyRecord(form.value).then(response => {-->
<!--          proxy.$modal.msgSuccess("新增成功");-->
<!--          open.value = false;-->
<!--          getList();-->
<!--        });-->
<!--      }-->
<!--    }-->
<!--  });-->
<!--}-->

<!--/** 删除按钮操作 */-->
<!--function handleDelete(row) {-->
<!--  const _ids = row.id || ids.value;-->
<!--  proxy.$modal.confirm('是否确认删除档案销毁编号为"' + _ids + '"的数据项？').then(function() {-->
<!--    return delArchiveDestroyRecord(_ids);-->
<!--  }).then(() => {-->
<!--    getList();-->
<!--    proxy.$modal.msgSuccess("删除成功");-->
<!--  }).catch(() => {});-->
<!--}-->

<!--/** 导出按钮操作 */-->
<!--function handleExport() {-->
<!--  proxy.download('manage/ArchiveDestroyRecord/export', {-->
<!--    ...queryParams.value-->
<!--  }, `ArchiveDestroyRecord_${new Date().getTime()}.xlsx`)-->
<!--}-->

<!--// 在组件作用域内声明状态标记-->
<!--const hasHandledInitialParams = ref(false)-->

<!--// 基础数据加载-->
<!--const loadBaseData = () => {-->
<!--  getList()-->
<!--}-->

<!--// 带参数时的附加操作-->
<!--const handleParamsOperations = () => {-->
<!--  open.value = true-->
<!--  form.value.archiveId = route.query.delete_id-->
<!--  form.value.archiveName = route.query.delete_name-->
<!--  form.value.danghao = route.query.delete_danghao-->

<!--  listDefinition(queryParams_activitiList).then(response => {-->
<!--    Modeler.value = response.rows-->
<!--    total_activiti.value = response.total-->
<!--  })-->
<!--}-->

<!--onMounted(() => {-->
<!--  // 仅加载基础数据，不自动打开弹窗-->
<!--  getList()-->

<!--  // 处理直接通过URL带参数访问的情况-->
<!--  if (route.query.delete_id && !hasHandledInitialParams.value) {-->
<!--    handleParamsOperations()-->
<!--    hasHandledInitialParams.value = true-->
<!--  }-->
<!--})-->

<!--watch(-->
<!--    () => route.query.delete_id, // 根据实际参数名调整-->
<!--    (newId) => {-->
<!--      if (route.path === "/manage/archive/ArchiveDestroyRecord") {-->
<!--        // 首次处理参数逻辑-->
<!--        if (newId && !hasHandledInitialParams.value) {-->
<!--          handleParamsOperations()-->
<!--          hasHandledInitialParams.value = true-->
<!--        }-->
<!--        // 参数清除时关闭弹窗-->
<!--        if (!newId && open.value) {-->
<!--          open.value = false-->
<!--        }-->
<!--      }-->
<!--    },-->
<!--    { immediate: true }-->
<!--)-->

<!--import { onBeforeUnmount } from 'vue'-->
<!--import { onBeforeRouteLeave } from 'vue-router'-->

<!--onBeforeRouteLeave((to, from) => {-->
<!--  if (from.path === "/manage/archive/ArchiveDestroyRecord") {-->
<!--    // 保留状态用于同路由切换-->
<!--    // 仅在完全离开路由时重置-->
<!--    if (to.path !== "/manage/archive/ArchiveDestroyRecord") {-->
<!--      hasHandledInitialParams.value = false-->
<!--      open.value = false-->
<!--    }-->
<!--  }-->
<!--})-->

<!--onBeforeUnmount(() => {-->
<!--  hasHandledInitialParams.value = false-->
<!--  open.value = false-->
<!--})-->

<!--</script>-->

<template>
  <!-- 可以留空 -->
</template>

<script>
// 可以留空或只包含基本导出
export default {}
</script>