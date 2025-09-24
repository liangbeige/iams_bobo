<template>
  <div class="app-container">

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
            type="warning"
            plain
            icon="Download"
            @click="handleExport"
        >导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="recordList">
      <el-table-column type="selection" width="30" align="center" />

      <el-table-column label="档案ID" align="center" prop="archiveId" />
      <el-table-column label="档案名" align="center" prop="archiveName" />
      <el-table-column label="档号" align="center" prop="archiveDangHao" />
      <el-table-column label="保密级别" align="center" prop="secretLevel">
        <template #default="scope">
          <dict-tag :options="iams_secret_level" :value="scope.row.secretLevel"/>
        </template>
      </el-table-column>


      <el-table-column label="保密期限" align="center" prop="secretPeroid">
        <template #default="scope">
          <dict-tag :options="iams_secret_period" :value="scope.row.secretPeroid"/>
        </template>
      </el-table-column>

      <el-table-column label="保管期限" align="center" prop="retentionPeriod">
        <template #default="scope">
          <dict-tag :options="iams_retention_period" :value="scope.row.retentionPeriod"/>
        </template>
      </el-table-column>

      <el-table-column label="归档时间" align="center" prop="guidangTime" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.guidangTime, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>

      <el-table-column label="状态" align="center" prop="status" />
      <el-table-column label="鉴定原因" align="center" prop="evaluationReason"/>
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

<script setup name="PendingEvaluation">
import {listPendingEvaluation} from "@/api/manage/PendingEvaluation.js";

import {ref} from "vue";

const {proxy} = getCurrentInstance();
const { iams_carrier_type, iams_archive_status, iams_retention_period, iams_secret_period, iams_secret_level } = proxy.useDict('iams_carrier_type', 'iams_archive_status', 'iams_retention_period', 'iams_secret_period', 'iams_secret_level');



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
  listPendingEvaluation().then(response => {
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
  proxy.download('task/pendingEvaluation/export', {
    ...queryParams.value
  }, `record_${new Date().getTime()}.xlsx`)
}

getList()

</script>
