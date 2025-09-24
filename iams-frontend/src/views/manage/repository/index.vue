<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="编号" prop="bianhao">
        <el-input
          v-model="queryParams.bianhao"
          placeholder="请输入编号"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="名称" prop="name">
        <el-input
          v-model="queryParams.name"
          placeholder="请输入名称"
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
          v-hasPermi="['manage:repository:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="Edit"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['manage:repository:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="Delete"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['manage:repository:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="Download"
          @click="handleExport"
          v-hasPermi="['manage:repository:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="repositoryList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="主键" align="center" prop="id" />
      <el-table-column label="编号" align="center" prop="bianhao" />
      <el-table-column label="名称" align="center" prop="name" />
      <el-table-column label="档案柜数量" align="center" prop="cabinetCount" />
      <el-table-column label="档案柜排数" align="center" prop="rowCount" />
      <el-table-column label="档案柜列数" align="center" prop="columnCount" />
      <el-table-column label="已有档案数" align="center" prop="archiveCount" />
      <el-table-column label="温度" align="center" prop="temperature" />
      <el-table-column label="湿度" align="center" prop="moisture" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" @click="gotoDetail(scope.row)" v-hasPermi="['manage:repository:query']">查看</el-button>
          <el-button link type="primary" @click="handleUpdate(scope.row)" v-hasPermi="['manage:repository:edit']">修改</el-button>
          <el-button link type="primary" @click="handleDelete(scope.row)" v-hasPermi="['manage:repository:remove']">删除</el-button>
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

    <!-- 添加或修改库房管理对话框 -->
    <el-dialog :title="title" v-model="open" width="500px" append-to-body>
      <el-form ref="repositoryRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="编号" prop="bianhao">
          <el-input v-model="form.bianhao" placeholder="请输入编号" />
        </el-form-item>
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入名称" />
        </el-form-item>
        <el-form-item label="档案柜数量" prop="cabinetCount">
          <el-input v-model="form.cabinetCount" placeholder="请输入档案柜数量" />
        </el-form-item>
        <el-form-item label="档案柜排数" prop="rowCount">
          <el-input v-model="form.rowCount" placeholder="请输入档案柜排数" />
        </el-form-item>
        <el-form-item label="档案柜列数" prop="columnCount">
          <el-input v-model="form.columnCount" placeholder="请输入档案柜列数" />
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

<script setup name="Repository">
import { listRepository, getRepository, delRepository, addRepository, updateRepository } from "@/api/manage/repository";

const { proxy } = getCurrentInstance();

const repositoryList = ref([]);
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
    bianhao: null,
    name: null,
  },
  rules: {
    bianhao: [
      { required: true, message: "编号不能为空", trigger: "blur" }
    ],
    name: [
      { required: true, message: "名称不能为空", trigger: "blur" }
    ],
  }
});

const { queryParams, form, rules } = toRefs(data);

/** 查询库房管理列表 */
function getList() {
  loading.value = true;
  listRepository(queryParams.value).then(response => {
    repositoryList.value = response.rows;
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
    bianhao: null,
    name: null,
    cabinetCount: null,
    rowCount: null,
    columnCount: null,
    archiveCount: null,
    temperature: null,
    moisture: null,
    particles: null,
    oxygen: null,
    waterlogging: null,
    createTime: null,
    createBy: null,
    updateTime: null,
    updateBy: null,
    remark: null
  };
  proxy.resetForm("repositoryRef");
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
  title.value = "添加库房管理";
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset();
  const _id = row.id || ids.value
  getRepository(_id).then(response => {
    form.value = response.data;
    open.value = true;
    title.value = "修改库房管理";
  });
}

/** 查看详情按钮操作 */
function gotoDetail(row) {
  const _id = row.id
  proxy.$router.push({ path: '/manage/cabinet/cbn-list/', query: { id: _id} });
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["repositoryRef"].validate(valid => {
    if (valid) {
      if (form.value.id != null) {
        updateRepository(form.value).then(response => {
          proxy.$modal.msgSuccess("修改成功");
          open.value = false;
          getList();
        });
      } else {
        addRepository(form.value).then(response => {
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
  proxy.$modal.confirm('是否确认删除库房管理编号为"' + _ids + '"的数据项？').then(function() {
    return delRepository(_ids);
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess("删除成功");
  }).catch(() => {});
}

/** 导出按钮操作 */
function handleExport() {
  proxy.download('manage/repository/export', {
    ...queryParams.value
  }, `repository_${new Date().getTime()}.xlsx`)
}

getList();
</script>
