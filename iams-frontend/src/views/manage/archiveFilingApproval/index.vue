<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="档案ID" prop="archiveId">
        <el-input
          v-model="queryParams.archiveId"
          placeholder="请输入档案ID"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>

      <el-form-item label="用户ID" prop="userId">
        <el-input
          v-model="queryParams.userId"
          placeholder="请输入用户ID"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>

      <el-form-item label="审批状态" prop="status">
        <el-input
            v-model="queryParams.status"
            placeholder="请输入审批状态"
            clearable
            @keyup.enter="handleQuery"
        />
      </el-form-item>

      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="Plus"
          @click="handleAdd"
          v-hasPermi="['manage:archiveFilingApproval:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="Download"
          @click="handleExport"
          v-hasPermi="['manage:archiveFilingApproval:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="archiveFilingApprovalList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="30" align="center" />

      <el-table-column label="用户ID" align="center" prop="userId" />
      <el-table-column label="用户名" align="center" prop="userName" />
      <el-table-column label="部门名" align="center" prop="userDepartment" />
      <el-table-column label="档案ID" align="center" prop="archiveId" />
      <el-table-column label="档案名" align="center" prop="archiveName" />
      <el-table-column label="目的" align="center" prop="purpose" width="200"/>
      <el-table-column label="申请状态" align="center" prop="status"/>
      <el-table-column label="备注" align="center" prop="remark" />


      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
<!--          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['manage:archiveFilingApproval:edit']">修改</el-button>-->
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['manage:archiveFilingApproval:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <pagination
      v-show="total>0"
      :total="total"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 添加或修改归档申请对话框 -->
    <el-dialog :title="title" v-model="open" width="500px" append-to-body>
      <el-form ref="archiveFilingApprovalRef" :model="form" :rules="rules" label-width="80px">

        <el-form-item label="用户ID" prop="userId">
          <el-input v-model="form.userId" placeholder="请输入用户ID" />
        </el-form-item>
        <el-form-item label="用户名" prop="userName">
          <el-input v-model="form.userName" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="用户部门" prop="userDepartment">
          <el-input v-model="form.userDepartment" placeholder="请输入用户所在的部门" />
        </el-form-item>

        <el-form-item label="档案ID" prop="archiveId">
          <el-input v-model="form.archiveId" placeholder="请输入档案ID" />
        </el-form-item>
        <el-form-item label="档案名" prop="archiveName">
          <el-input v-model="form.archiveName" placeholder="请输入档案名" />
        </el-form-item>
        <el-form-item label="档号" prop="archiveDanghao">
          <el-input v-model="form.archiveDangHao" placeholder="请输入档号" />
        </el-form-item>

        <el-form-item label="归档原因" prop="purpose">
          <el-input v-model="form.purpose" type="textarea" placeholder="请输入内容" />
        </el-form-item>


        <el-form-item label="启动流程" prop="processName">
          <el-select v-model="form.processName" placeholder="请选择待启动流程" clearable>
            <el-option
                v-for="dict in Modeler"
                :label="dict.name"
                :value="dict.key"
            />
          </el-select>
        </el-form-item>

      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="ArchiveFilingApproval">
import { listArchiveFilingApproval, getArchiveFilingApproval, delArchiveFilingApproval, addArchiveFilingApproval, updateArchiveFilingApproval } from "@/api/manage/archiveFilingApproval";
import {ref} from "vue";
import {listDefinition} from "@/api/activiti/definition.js";

const { proxy } = getCurrentInstance();

const archiveFilingApprovalList = ref([]);
const open = ref(false);
const loading = ref(true);
const showSearch = ref(true);
const ids = ref([]);
const single = ref(true);
const multiple = ref(true);
const total = ref(0);
const title = ref("");

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    userId: null,
    archiveId: null,
    status: null
  },
  rules: {
    archiveId: [
      { required: true, message: "档案ID不能为空", trigger: "blur" }
    ],
    userId: [
      { required: true, message: "用户ID不能为空", trigger: "blur" }
    ],
    purpose: [
      { required: true, message: "归档原因不能为空", trigger: "blur"}
    ],
    processName: [
      { required: true, message: "启动流程不能为空", trigger: "blur"}
    ]
  }
});

const { queryParams, form, rules } = toRefs(data);

/** 查询归档申请列表 */
function getList() {
  loading.value = true;
  listArchiveFilingApproval(queryParams.value).then(response => {
    archiveFilingApprovalList.value = response.rows;
    total.value = response.total;
    loading.value = false;
  });
}


const Modeler = ref([]);
const total_activiti = ref(0);
const queryParams_activitiList = reactive({
  pageNum: 1,
  pageSize: 10,
  id: null,
  rev: null,
  name: null,
  type: null,
});

// 取消按钮
function cancel() {
  open.value = false;
  reset();
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
    processName: null,
  };
  proxy.resetForm("archiveFilingApprovalRef");
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

/** 新增按钮操作 */
function handleAdd() {
  reset();
  open.value = true;
  title.value = "添加归档申请";
  listDefinition(queryParams_activitiList).then(response => {
    Modeler.value = response.rows;
    total_activiti.value = response.total;
  });
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset();
  const _id = row.id || ids.value
  getArchiveFilingApproval(_id).then(response => {
    form.value = response.data;
    open.value = true;
    title.value = "修改归档申请";
  });
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["archiveFilingApprovalRef"].validate(valid => {
    if (valid) {
      if (form.value.id != null) {
        updateArchiveFilingApproval(form.value).then(response => {
          proxy.$modal.msgSuccess("修改成功");
          open.value = false;
          getList();
        });
      } else {
        addArchiveFilingApproval(form.value).then(response => {
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
  proxy.$modal.confirm('是否确认删除归档申请编号为"' + _ids + '"的数据项？').then(function() {
    return delArchiveFilingApproval(_ids);
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess("删除成功");
  }).catch(() => {});
}

/** 导出按钮操作 */
function handleExport() {
  proxy.download('manage/archiveFilingApproval/export', {
    ...queryParams.value
  }, `archiveFilingApproval_${new Date().getTime()}.xlsx`)
}

getList();
</script>
