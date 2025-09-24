<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="档号" prop="danghao">
        <el-input
          v-model="queryParams.danghao"
          placeholder="请输入档号"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="档案名称" prop="name">
        <el-input
          v-model="queryParams.name"
          placeholder="请输入档案名称"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="射频标签号" prop="rfid">
        <el-input
          v-model="queryParams.rfid"
          placeholder="请输入射频标签号"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="保密级别" prop="secretLevel">
        <el-select v-model="queryParams.secretLevel" placeholder="请选择保密级别" clearable>
          <el-option
            v-for="dict in iams_secret_level"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="保密期限" prop="secretPeroid">
        <el-select v-model="queryParams.secretPeroid" placeholder="请选择保密期限" clearable>
          <el-option
            v-for="dict in iams_secret_period"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="保管期限" prop="retentionPeriod">
        <el-select v-model="queryParams.retentionPeriod" placeholder="请选择保管期限" clearable>
          <el-option
            v-for="dict in iams_retention_period"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="载体类型" prop="carrierType">
        <el-select v-model="queryParams.carrierType" placeholder="请选择载体类型" clearable>
          <el-option
            v-for="dict in iams_carrier_type"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="归档时间" prop="guidangTime">
        <el-date-picker clearable
          v-model="queryParams.guidangTime"
          type="date"
          value-format="YYYY-MM-DD"
          placeholder="请选择归档时间">
        </el-date-picker>
      </el-form-item>
      <el-form-item label="档案状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择档案状态" clearable>
          <el-option
            v-for="dict in iams_archive_status"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="档案门类" prop="categoryId">
        <el-input
          v-model="queryParams.categoryId"
          placeholder="请输入档案门类"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="档案全宗" prop="fondsId">
        <el-input
          v-model="queryParams.fondsId"
          placeholder="请输入档案全宗"
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
          v-hasPermi="['manage:archive:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="Edit"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['manage:archive:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="Delete"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['manage:archive:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="Download"
          @click="handleExport"
          v-hasPermi="['manage:archive:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="archiveList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="主键ID" align="center" prop="id" />
      <el-table-column label="档号" align="center" prop="danghao" />
      <el-table-column label="档案名称" align="center" prop="name" />
      <el-table-column label="射频标签号" align="center" prop="rfid" />
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
      <el-table-column label="解密日期" align="center" prop="desecretDate" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.desecretDate, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="保管期限" align="center" prop="retentionPeriod">
        <template #default="scope">
          <dict-tag :options="iams_retention_period" :value="scope.row.retentionPeriod"/>
        </template>
      </el-table-column>
      <el-table-column label="载体类型" align="center" prop="carrierType">
        <template #default="scope">
          <dict-tag :options="iams_carrier_type" :value="scope.row.carrierType"/>
        </template>
      </el-table-column>
      <el-table-column label="起始时间" align="center" prop="startDate" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.startDate, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="终止时间" align="center" prop="endDate" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.endDate, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="归档时间" align="center" prop="guidangTime" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.guidangTime, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="形成单位" align="center" prop="formationUnit" />
      <el-table-column label="移交单位" align="center" prop="transferUnit" />
      <el-table-column label="档案状态" align="center" prop="status">
        <template #default="scope">
          <dict-tag :options="iams_archive_status" :value="scope.row.status"/>
        </template>
      </el-table-column>
      <el-table-column label="档案门类" align="center" prop="categoryId" />
      <el-table-column label="档案全宗" align="center" prop="fondsId" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['manage:archive:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['manage:archive:remove']">删除</el-button>
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

    <!-- 添加或修改档案列表对话框 -->
    <el-dialog :title="title" v-model="open" width="500px" append-to-body>
      <el-form ref="archiveRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="档号" prop="danghao">
          <el-input v-model="form.danghao" placeholder="请输入档号" />
        </el-form-item>
        <el-form-item label="档案名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入档案名称" />
        </el-form-item>
        <el-form-item label="射频标签号" prop="rfid">
          <el-input v-model="form.rfid" placeholder="请输入射频标签号" />
        </el-form-item>
        <el-form-item label="保密级别" prop="secretLevel">
          <el-select v-model="form.secretLevel" placeholder="请选择保密级别">
            <el-option
              v-for="dict in iams_secret_level"
              :key="dict.value"
              :label="dict.label"
              :value="dict.value"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="保密期限" prop="secretPeroid">
          <el-select v-model="form.secretPeroid" placeholder="请选择保密期限">
            <el-option
              v-for="dict in iams_secret_period"
              :key="dict.value"
              :label="dict.label"
              :value="dict.value"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="解密日期" prop="desecretDate">
          <el-date-picker clearable
            v-model="form.desecretDate"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="请选择解密日期">
          </el-date-picker>
        </el-form-item>
        <el-form-item label="保管期限" prop="retentionPeriod">
          <el-select v-model="form.retentionPeriod" placeholder="请选择保管期限">
            <el-option
              v-for="dict in iams_retention_period"
              :key="dict.value"
              :label="dict.label"
              :value="dict.value"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="载体类型" prop="carrierType">
          <el-select v-model="form.carrierType" placeholder="请选择载体类型">
            <el-option
              v-for="dict in iams_carrier_type"
              :key="dict.value"
              :label="dict.label"
              :value="dict.value"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="起始时间" prop="startDate">
          <el-date-picker clearable
            v-model="form.startDate"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="请选择起始时间">
          </el-date-picker>
        </el-form-item>
        <el-form-item label="终止时间" prop="endDate">
          <el-date-picker clearable
            v-model="form.endDate"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="请选择终止时间">
          </el-date-picker>
        </el-form-item>
        <el-form-item label="归档时间" prop="guidangTime">
          <el-date-picker clearable
            v-model="form.guidangTime"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="请选择归档时间">
          </el-date-picker>
        </el-form-item>
        <el-form-item label="实体档案位置" prop="shitiLocation">
          <el-input v-model="form.shitiLocation" placeholder="请输入实体档案位置" />
        </el-form-item>
        <el-form-item label="电子档案位置" prop="dianziLocation">
          <el-input v-model="form.dianziLocation" placeholder="请输入电子档案位置" />
        </el-form-item>
        <el-form-item label="档案简介" prop="description">
          <el-input v-model="form.description" type="textarea" placeholder="请输入内容" />
        </el-form-item>
        <el-form-item label="形成单位" prop="formationUnit">
          <el-input v-model="form.formationUnit" placeholder="请输入形成单位" />
        </el-form-item>
        <el-form-item label="移交单位" prop="transferUnit">
          <el-input v-model="form.transferUnit" placeholder="请输入移交单位" />
        </el-form-item>
        <el-form-item label="文件(档)数量" prop="documentCount">
          <el-input v-model="form.documentCount" placeholder="请输入文件(档)数量" />
        </el-form-item>
        <el-form-item label="档案状态" prop="status">
          <el-select v-model="form.status" placeholder="请选择档案状态">
            <el-option
              v-for="dict in iams_archive_status"
              :key="dict.value"
              :label="dict.label"
              :value="dict.value"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="档案门类" prop="categoryId">
          <el-input v-model="form.categoryId" placeholder="请输入档案门类" />
        </el-form-item>
        <el-form-item label="档案全宗" prop="fondsId">
          <el-input v-model="form.fondsId" placeholder="请输入档案全宗" />
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
  </div>
</template>

<script setup name="Archive">
import { listArchive, getArchive, delArchive, addArchive, updateArchive } from "@/api/manage/archive";

const { proxy } = getCurrentInstance();
const { iams_carrier_type, iams_archive_status, iams_retention_period, iams_secret_period, iams_secret_level } = proxy.useDict('iams_carrier_type', 'iams_archive_status', 'iams_retention_period', 'iams_secret_period', 'iams_secret_level');

const archiveList = ref([]);
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
    danghao: null,
    name: null,
    rfid: null,
    secretLevel: null,
    secretPeroid: null,
    retentionPeriod: null,
    carrierType: null,
    guidangTime: null,
    directory: null,
    status: null,
    categoryId: null,
    fondsId: null,
  },
  rules: {
    name: [
      { required: true, message: "档案名称不能为空", trigger: "blur" }
    ],
  }
});

const { queryParams, form, rules } = toRefs(data);

/** 查询档案列表列表 */
function getList() {
  loading.value = true;
  listArchive(queryParams.value).then(response => {
    archiveList.value = response.rows;
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
    danghao: null,
    name: null,
    rfid: null,
    jianhao: null,
    secretLevel: null,
    secretPeroid: null,
    desecretDate: null,
    retentionPeriod: null,
    carrierType: null,
    startDate: null,
    endDate: null,
    guidangTime: null,
    shitiLocation: null,
    dianziLocation: null,
    directory: null,
    description: null,
    formationUnit: null,
    transferUnit: null,
    documentCount: null,
    status: null,
    authenticity: null,
    integrity: null,
    availability: null,
    security: null,
    categoryId: null,
    fondsId: null,
    createTime: null,
    createBy: null,
    updateTime: null,
    updateBy: null,
    remark: null
  };
  proxy.resetForm("archiveRef");
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
  title.value = "添加档案列表";
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset();
  const _id = row.id || ids.value
  getArchive(_id).then(response => {
    form.value = response.data;
    open.value = true;
    title.value = "修改档案列表";
  });
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["archiveRef"].validate(valid => {
    if (valid) {
      if (form.value.id != null) {
        updateArchive(form.value).then(response => {
          proxy.$modal.msgSuccess("修改成功");
          open.value = false;
          getList();
        });
      } else {
        addArchive(form.value).then(response => {
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
  proxy.$modal.confirm('是否确认删除档案列表编号为"' + _ids + '"的数据项？').then(function() {
    return delArchive(_ids);
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess("删除成功");
  }).catch(() => {});
}

/** 导出按钮操作 */
function handleExport() {
  proxy.download('manage/archive/export', {
    ...queryParams.value
  }, `archive_${new Date().getTime()}.xlsx`)
}

getList();
</script>
