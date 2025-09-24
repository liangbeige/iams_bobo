<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="用户名" prop="userName">
        <el-input
            v-model="queryParams.userName"
            placeholder="请输入借阅用户的 名称"
            clearable
            @keyup.enter="handleQuery"
        />
      </el-form-item>

      <el-form-item label="档号" prop="archiveDanghao">
        <el-input
            v-model="queryParams.archiveDanghao"
            placeholder="请输入借阅的档号"
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

    <el-table v-loading="loading" :data="recordList">
      <el-table-column type="selection" width="30" align="center" />

<!--      <el-table-column label="用户ID" align="center" prop="userId" />-->
      <el-table-column label="用户名" align="center" prop="userName" width="90" />
      <el-table-column label="部门名" align="center" prop="userDepartment" width="80" />
      <el-table-column label="档号" align="center" prop="archiveDanghao" width="120" />
      <el-table-column label="档案名" align="center" prop="archiveName" width="100"/>

      <el-table-column label="借阅期限" align="center" width="200">
        <template #default="scope">
          <span>{{ parseTime(scope.row.startDate, '{y}-{m}-{d}') }} - {{ parseTime(scope.row.endDate, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>

      <el-table-column label="借阅目的" align="center" prop="purpose" width="200"/>
      <el-table-column label="借阅状态" align="center" prop="status">
      </el-table-column>

      <el-table-column label="实际借阅期限" align="center" width="200">
        <template #default="scope">
          <span>{{ parseTime(scope.row.borrowDate, '{y}-{m}-{d}') }} - {{ parseTime(scope.row.returnDate, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="逾期天数" align="center">
        <template #default="scope">
          <!-- 显示处理后的结果 -->
          <span>{{ calculateOverdueDays(scope.row.endDate) }}</span>
        </template>
      </el-table-column>

      <el-table-column label="备注" align="center" prop="remark" />

      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <!-- <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['manage:record:edit']">归还</el-button> -->
          <el-button link type="primary" icon="Delete" @click="handleReminder(scope.row)">催还</el-button>
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

<script setup name="Overdue">
import {updateRecord, listRecordOverdue, RemindOverdue} from "@/api/manage/borrowrecord";

import {ref} from "vue";

import dayjs from 'dayjs';

function calculateOverdueDays(returnDate) {
  const today = dayjs().startOf('day');
  const endDay = dayjs(returnDate).startOf('day');
  const diff = today.diff(endDay, 'day');
  return diff > 0 ? diff : 0;
}

const {proxy} = getCurrentInstance();


const recordList = ref([]);

const loading = ref(true);
const showSearch = ref(true);

const total = ref(0);

const data = reactive({
  form: {
    id: null
  },
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    userId: null,
    archiveId: null,
  },

});

const {queryParams, form, rules} = toRefs(data);

/** 查询逾期借阅记录列表 */
function getList() {
  loading.value = true;
  listRecordOverdue(queryParams.value).then(response => {
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


/** 手动归还按钮操作 */
function handleUpdate(row) {
  form.value.id = row.id;
  updateRecord(form.value).then(response => {
    proxy.$modal.msgSuccess("操作成功");
    getList();
  });
}


function handleReminder(row){
  form.value.id = row.id;
  RemindOverdue(form.value).then(response => {
    proxy.$modal.msgSuccess("操作成功");
    getList();
  })
}


/** 导出按钮操作 */
function handleExport() {
  proxy.download('manage/record/export', {
    ...queryParams.value
  }, `record_${new Date().getTime()}.xlsx`)
}

getList()

</script>
