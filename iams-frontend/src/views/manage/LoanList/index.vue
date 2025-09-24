<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="用户ID" prop="userId">
        <el-input
            v-model="queryParams.userId"
            placeholder="请输入借阅用户的 ID"
            clearable
            @keyup.enter="handleQuery"
        />
      </el-form-item>

      <el-form-item label="档案ID" prop="archiveId">
        <el-input
            v-model="queryParams.archiveId"
            placeholder="请输入借阅的档案ID"
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
            type="warning"
            plain
            icon="Download"
            @click="handleExport"
            v-hasPermi="['manage:record:export']"
        >导出</el-button>
      </el-col>

      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="recordList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="30" align="center" />

      <el-table-column label="用户ID" align="center" prop="userId" />
      <el-table-column label="用户名" align="center" prop="userName" />
      <el-table-column label="部门名" align="center" prop="userDepartment" />
      <el-table-column label="档案ID" align="center" prop="archiveId" />
      <el-table-column label="档案名" align="center" prop="archiveName" />

      <el-table-column label="借阅期限" align="center" width="200">
        <template #default="scope">
          <span>{{ parseTime(scope.row.startDate, '{y}-{m}-{d}') }} - {{ parseTime(scope.row.endDate, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>

      <el-table-column label="借阅目的" align="center" prop="purpose" width="200"/>

      <el-table-column label="实际借阅期限" align="center" width="200">
        <template #default="scope">
          <span>{{ parseTime(scope.row.borrowDate, '{y}-{m}-{d}') }} - {{ parseTime(scope.row.returnDate, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="逾期天数" align="center" prop="overdueDays" />

      <el-table-column label="备注" align="center" prop="remark" />

      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['manage:record:edit']">归还</el-button>
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

  </div>
</template>

<script setup name="LoanList">
import {
  listApprovedRecord, advanceRecord
} from "@/api/manage/borrowrecord";
import {ref} from "vue";
import { useRoute } from 'vue-router';

const {proxy} = getCurrentInstance();

const route = useRoute();

const recordList = ref([]);
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
    status: "approved",
    overdueDays: null
  }
});

const {queryParams, form, rules} = toRefs(data);

/** 查询借阅记录列表 */
function getList() {
  loading.value = true;
  listApprovedRecord(queryParams.value).then(response => {
    recordList.value = response.rows;
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


/** 修改按钮操作 */
function handleUpdate(row) {
  const _id = row.id || ids.value
  advanceRecord(_id).then(response => {
    proxy.$modal.msgSuccess("归还成功");
  });
}


/** 导出按钮操作 */
function handleExport() {
  proxy.download('manage/record/export', {
    ...queryParams.value
  }, `record_${new Date().getTime()}.xlsx`)
}

getList();
</script>
