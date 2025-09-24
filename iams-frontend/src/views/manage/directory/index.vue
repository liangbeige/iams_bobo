<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="目录名称" prop="name">
        <el-input v-model="queryParams.name" placeholder="请输入目录名称" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="目录结构" prop="structure">
        <el-input v-model="queryParams.structure" placeholder="请输入目录结构" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd"
                   v-hasPermi="['manage:directory:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Download" @click="handleExport"
                   v-hasPermi="['manage:directory:export']">导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>
    <!-- 循环渲染directoryList， 使用el-row el-col布局，每行显示4个el-card -->
    <el-row :gutter="10" class="mb6">
      <el-col :span="6" v-for="(item, index) in directoryList" :key="index">
        <el-card class="mb8">
          <template #header>
            <div class="card-header">
              <span>{{ item.id }}</span>
              <span>{{ item.name }}</span>
            </div>
          </template>
          <div class="card-body">
            <el-tree style="max-width: 100%" :data="get_tree_data(item)" />
          </div>
          <template #footer>
            <div  class="card-footer">
              <el-button type="primary" plain @click="handleDetail(item)">查看</el-button>
              <el-button type="primary" plain @click="handleCopy(item)">复制</el-button>
              <el-button type="primary" plain @click="handleUpdate(item)">修改</el-button>
              <el-button type="danger" plain @click="handleDelete(item)">删除</el-button>

            </div>
          </template>
        </el-card>
      </el-col>
    </el-row>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize"
                @pagination="getList" />
  </div>
</template>

<script setup name="Directory">
import {listDirectory, delDirectory, addDirectory} from "@/api/manage/directory";

const {proxy} = getCurrentInstance();

const directoryList = ref([]);
const loading = ref(true);
const showSearch = ref(true);
const ids = ref([]);
const single = ref(true);
const multiple = ref(true);
const total = ref(0);

const data = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    name: null,
    structure: null,
  }
});

const {queryParams} = toRefs(data);

/** 查询目录列表 */
function getList() {
  loading.value = true;
  listDirectory(queryParams.value).then(response => {
    directoryList.value = response.rows;
    total.value = response.total;
    loading.value = false;
  });
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

/** 新增按钮操作 */
function handleAdd() {
  proxy.$router.push({path: "/manage/directory/drt-add"});
}

/** 查看按钮操作 */
function handleDetail(row) {
  proxy.$router.push({path: "/manage/directory/drt-detail", query: {id: row.id}});
}


/** 复制目录操作 */
function handleCopy(item) {
  // 创建副本数据 - 复用"另存为"逻辑
  const copyData = {
    ...item,          // 复制所有属性
    id: null,         // 重置ID
    name: `${item.name} - 副本` // 添加副本标识
  };

  // 直接调用新增API - 复用"另存为"逻辑
  addDirectory(copyData).then(response => {
    proxy.$modal.msgSuccess("复制成功");
    // 刷新列表
    getList();
  }).catch(error => {
    proxy.$modal.msgError("复制失败: " + (error.message || "未知错误"));
  });
}


/** 修改按钮操作 */
function handleUpdate(row) {
  proxy.$router.push({path: "/manage/directory/drt-edit", query: {id: row.id}});
}

/** 删除按钮操作 */
function handleDelete(row) {
  const _ids = row.id || ids.value;
  proxy.$modal.confirm('是否确认删除目录管理编号为"' + _ids + '"的数据项？').then(function () {
    return delDirectory(_ids);
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess("删除成功");
  }).catch(() => {
  });
}

/** 导出按钮操作 */
function handleExport() {
  proxy.download('manage/directory/export', {
    ...queryParams.value
  }, `directory_${new Date().getTime()}.xlsx`)
}

getList();

function get_tree_data(item) {
  return JSON.parse(item.structure);
}
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
}

.card-body {
  height: 100px;
  overflow-y: auto;
}

.card-footer {
  padding: 0px;
  margin: 0px;
  display: flex;
  justify-content: center;
  align-items: center;
}
</style>