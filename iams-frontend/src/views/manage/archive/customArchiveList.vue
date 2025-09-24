<template>
  <div class="app-container" v-loading="loadingV">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="100px">
      <el-form-item label="档号" prop="danghao">
        <el-input v-model="queryParams.danghao" placeholder="请输入档号" clearable @keyup.enter="handleQuery"/>
      </el-form-item>
      <el-form-item label="档案名称" prop="name">
        <el-input v-model="queryParams.name" placeholder="请输入档案名称" clearable @keyup.enter="handleQuery"/>
      </el-form-item>
<!--      <el-form-item label="射频标签号" prop="rfid">-->
<!--        <el-input v-model="queryParams.rfid" placeholder="请输入射频标签号" clearable @keyup.enter="handleQuery"/>-->
<!--      </el-form-item>-->
      <el-form-item label="保密级别" prop="secretLevel">
        <el-select v-model="queryParams.secretLevel" placeholder="请选择保密级别" clearable>
          <el-option v-for="dict in iams_secret_level" :key="dict.value" :label="dict.label" :value="dict.value"/>
        </el-select>
      </el-form-item>
      <el-form-item label="保密期限" prop="secretPeroid">
        <el-select v-model="queryParams.secretPeroid" placeholder="请选择保密期限" clearable>
          <el-option v-for="dict in iams_secret_period" :key="dict.value" :label="dict.label" :value="dict.value"/>
        </el-select>
      </el-form-item>
      <el-form-item label="保管期限" prop="retentionPeriod">
        <el-select v-model="queryParams.retentionPeriod" placeholder="请选择保管期限" clearable>
          <el-option v-for="dict in iams_retention_period" :key="dict.value" :label="dict.label" :value="dict.value"/>
        </el-select>
      </el-form-item>
      <el-form-item label="载体类型" prop="carrierType">
        <el-select v-model="queryParams.carrierType" placeholder="请选择载体类型" clearable>
          <el-option v-for="dict in iams_carrier_type" :key="dict.value" :label="dict.label" :value="dict.value"/>
        </el-select>
      </el-form-item>
      <el-form-item label="归档时间" prop="guidangTime">
        <el-date-picker clearable v-model="queryParams.guidangTime" type="date" value-format="YYYY-MM-DD"
                        placeholder="请选择归档时间">
        </el-date-picker>
      </el-form-item>
<!--      <el-form-item label="电子建档时间" prop="startDate">-->
<!--        <el-date-picker clearable v-model="queryParams.startDate" type="date" value-format="YYYY-MM-DD"-->
<!--                        placeholder="请选择电子建档时间">-->
<!--        </el-date-picker>-->
<!--      </el-form-item>-->
      <el-form-item label="档案状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择档案状态" clearable>
          <el-option v-for="dict in iams_archive_status" :key="dict.value" :label="dict.label" :value="dict.value"/>
        </el-select>
      </el-form-item>
      <el-form-item label="档案门类" prop="categoryId">
        <el-input v-model="queryParams.categoryId" placeholder="请输入档案门类" clearable @keyup.enter="handleQuery"/>
      </el-form-item>
<!--      <el-form-item label="全宗编号" prop="fondsId">-->
<!--        <el-input v-model="queryParams.fondsId" placeholder="请输入全宗编号" clearable @keyup.enter="handleQuery"/>-->
<!--      </el-form-item>-->
      <el-form-item label="所属项目">
        <el-select v-model="queryParams.projectId" filterable clearable placeholder="选择项目">
          <el-option v-for="project in projectList" :key="project.id"
                     :label="project.name" :value="project.id"/>
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="archiveList">
      <el-table-column label="档号" align="center" prop="danghao" >
        <template #default="scope">
          <div style="white-space: normal; word-break: break-all;">
            {{ scope.row.danghao }}
          </div>
        </template>
      </el-table-column>
      <el-table-column label="档案名称" align="center" prop="name" show-overflow-tooltip/>
      <el-table-column label="保密级别" align="center" prop="secretLevel" width="80">
        <template #default="scope">
          <dict-tag :options="iams_secret_level" :value="scope.row.secretLevel"/>
        </template>
      </el-table-column>
      <el-table-column label="保密期限" align="center" prop="secretPeroid" width="80">
        <template #default="scope">
          <dict-tag :options="iams_secret_period" :value="scope.row.secretPeroid"/>
        </template>
      </el-table-column>
      <el-table-column label="载体类型" align="center" prop="carrierType" width="100">
        <template #default="scope">
          <dict-tag :options="iams_carrier_type" :value="scope.row.carrierType"/>
        </template>
      </el-table-column>
      <el-table-column label="所属项目" prop="projectName" align="center">
        <template #default="scope">
          <el-tag v-if="scope.row.projectName" type="info" size="small">
            {{ scope.row.projectName }}
          </el-tag>
          <el-tag v-else type="warning" size="small">
            未分配
          </el-tag>
        </template>
      </el-table-column>

      <el-table-column label="形成单位" align="center" prop="formationUnit" show-overflow-tooltip />
      <!--      <el-table-column label="移交单位" align="center" prop="transferUnit"/>-->
      <el-table-column label="档案状态" align="center" prop="status" width="80">
        <template #default="scope">
          <dict-tag :options="iams_archive_status" :value="scope.row.status"/>
        </template>
      </el-table-column>
      <el-table-column label="档案门类" align="center" prop="categoryId" width="80"/>
      <!--      <el-table-column label="档案全宗" align="center" prop="fondsId"/>-->
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" @click="handleDetail(scope.row)"
                     v-hasPermi="['manage:archive:query']">查看
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum"
                v-model:limit="queryParams.pageSize" @pagination="getList"/>

  </div>
</template>

<script setup name="Archive">
import {
  listBorrowArchiveInVolume,
} from "@/api/manage/archive";
import {listProject} from "@/api/manage/project";
import {onMounted, computed} from 'vue';
import { useRouter } from 'vue-router'
const router = useRouter()
const {proxy} = getCurrentInstance();
// 添加项目列表状态
const projectList = ref([]);
const {
  iams_carrier_type,
  iams_archive_status,
  iams_retention_period,
  iams_secret_period,
  iams_secret_level
} = proxy.useDict('iams_carrier_type', 'iams_archive_status', 'iams_retention_period', 'iams_secret_period', 'iams_secret_level');

const archiveList = ref([]);

const loading = ref(true);
const showSearch = ref(true);
const total = ref(0);
let loadingV = ref(false);

const data = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    danghao: null,
    name: null,
    rfid: null,
    secretLevel: null,
    secretPeroid: null,
    retentionPeriod: null,
    carrierType: null,
    startDate: null,
    guidangTime: null,
    directory: null,
    status: null,
    categoryId: null,
    fondsId: null,
  }
});

const {queryParams, logQueryParams} = toRefs(data);

// 在onMounted中添加调用
onMounted(() => {
  getList();
  getProjectList(); // 确保页面加载时获取项目列表
});

/** 查询档案列表列表 */
function getList() {
  loading.value = true;
  listBorrowArchiveInVolume(queryParams.value).then(response => {
    archiveList.value = response.rows;
    total.value = response.total;
    loading.value = false;
  });
}


// 在methods中新增getProjectList方法
function getProjectList() {
  listProject({
    pageNum: 1,
    pageSize: 100,
    status: '0' // 只获取启用状态的项目
  }).then(response => {
    if (response.code === 200) {
      projectList.value = response.rows || [];
      console.log("项目列表加载成功:", projectList.value);
    } else {
      proxy.$modal.msgError(response.msg || "获取项目列表失败");
    }
  }).catch(error => {
    console.error("获取项目列表失败:", error);
    proxy.$modal.msgError("项目列表加载失败");
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
/** 查看详情操作 */
function handleDetail(row) {
  proxy.$router.push({path: "/manage/archive/arc-detail", query: {id: row.id}});
}
getList();
</script>
<style scoped>

</style>