<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryFormRef" size="small" :inline="true" v-show="showSearch" label-width="120px">
      <el-form-item label="生产厂家" prop="manufacturer">
        <el-input
            v-model="queryParams.manufacturer"
            placeholder="请输入生产厂家"
            clearable
            @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="运行状态" prop="isNormal">
        <el-select v-model="queryParams.isNormal" placeholder="请选择运行状态" clearable>
          <el-option
              v-for="dict in cameraStatus"
              :key="dict.value"
              :label="dict.label"
              :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="安装地点" prop="installationLocation">
        <el-input
            v-model="queryParams.installationLocation"
            placeholder="请输入安装地点"
            clearable
            @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="具体型号" prop="deviceModel">
        <el-input
            v-model="queryParams.deviceModel"
            placeholder="请输入具体型号"
            clearable
            @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="安装时间" prop="installationDate">
        <el-date-picker
            clearable
            v-model="queryParams.installationDate"
            type="date"
            value-format="yyyy-MM-dd"
            placeholder="请选择安装时间"
        />
      </el-form-item>
      <el-form-item label="上次维护时间" prop="lastMaintenanceDate">
        <el-date-picker
            clearable
            v-model="queryParams.lastMaintenanceDate"
            type="date"
            value-format="yyyy-MM-dd"
            placeholder="请选择上次维护时间"
        />
      </el-form-item>
      <el-form-item label="IP地址" prop="ipAddress">
        <el-input
            v-model="queryParams.ipAddress"
            placeholder="请输入IP地址"
            clearable
            @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="端口" prop="portNumber">
        <el-input
            v-model="queryParams.portNumber"
            placeholder="请输入端口"
            clearable
            @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="密码" prop="password">
        <el-input
            v-model="queryParams.password"
            placeholder="请输入密码"
            clearable
            @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
            type="primary"
            plain
            icon="Plus"
            size="mini"
            @click="handleAdd"
            v-hasPermi="['system:iot_camera_device_info:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
            type="success"
            plain
            icon="Edit"
            size="mini"
            :disabled="single"
            @click="handleUpdate"
            v-hasPermi="['system:iot_camera_device_info:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
            type="danger"
            plain
            icon="Delete"
            size="mini"
            :disabled="multiple"
            @click="handleDelete"
            v-hasPermi="['system:iot_camera_device_info:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
            type="warning"
            plain
            icon="Download"
            size="mini"
            @click="handleExport"
            v-hasPermi="['system:iot_camera_device_info:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="iotCameraDeviceInfoList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="摄像头编号" align="center" prop="deviceId" />
      <el-table-column label="生产厂家" align="center" prop="manufacturer" />
      <el-table-column label="运行状态" align="center" prop="isNormal">
        <template #default="scope">
          <dict-tag :options="cameraStatus" :value="scope.row.isNormal"/>
        </template>
      </el-table-column>
      <el-table-column label="安装地点" align="center" prop="installationLocation" />
      <el-table-column label="具体型号" align="center" prop="deviceModel" />
      <el-table-column label="安装时间" align="center" prop="installationDate" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.installationDate, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="上次维护时间" align="center" prop="lastMaintenanceDate" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.lastMaintenanceDate, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="IP地址" align="center" prop="ipAddress" />
      <el-table-column label="端口" align="center" prop="portNumber" />
      <el-table-column label="密码" align="center" prop="password" />
      <el-table-column label="备注说明" align="center" prop="remarks" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button
              size="mini"
              type="text"
              icon="Edit"
              @click="handleUpdate(scope.row)"
              v-hasPermi="['system:iot_camera_device_info:edit']"
          >修改</el-button>
          <el-button
              size="mini"
              type="text"
              icon="Delete"
              @click="handleDelete(scope.row)"
              v-hasPermi="['system:iot_camera_device_info:remove']"
          >删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
        v-show="total>0"
        :total="total"
        :page.sync="queryParams.pageNum"
        :limit.sync="queryParams.pageSize"
        @pagination="getList"
    />

    <!-- 添加或修改存储摄像头设备的详细信息对话框 -->
    <el-dialog
        v-model="open"
        width="500px"
        :close-on-click-modal="false"
        :modal-append-to-body="false"
        @open="onDialogOpen"
        @close="onDialogClose"
    >
      <template #header="{ close }">
        <div class="flex justify-between items-center w-full">
          <span>{{ title }}</span>
        </div>
      </template>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="生产厂家" prop="manufacturer">
          <el-input v-model="form.manufacturer" placeholder="请输入生产厂家" />
        </el-form-item>
        <el-form-item label="运行状态" prop="isNormal">
          <el-select v-model="form.isNormal" placeholder="请选择运行状态">
            <el-option
                v-for="dict in cameraStatus"
                :key="dict.value"
                :label="dict.label"
                :value="parseInt(dict.value)"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="安装地点" prop="installationLocation">
          <el-input v-model="form.installationLocation" placeholder="请输入安装地点" />
        </el-form-item>
        <el-form-item label="具体型号" prop="deviceModel">
          <el-input v-model="form.deviceModel" placeholder="请输入具体型号" />
        </el-form-item>
        <el-form-item label="安装时间" prop="installationDate">
          <el-date-picker
              clearable
              v-model="form.installationDate"
              type="date"
              value-format="yyyy-MM-dd"
              placeholder="请选择安装时间"
          />
        </el-form-item>
        <el-form-item label="上次维护时间" prop="lastMaintenanceDate">
          <el-date-picker
              clearable
              v-model="form.lastMaintenanceDate"
              type="date"
              value-format="yyyy-MM-dd"
              placeholder="请选择上次维护时间"
          />
        </el-form-item>
        <el-form-item label="IP地址" prop="ipAddress">
          <el-input v-model="form.ipAddress" placeholder="请输入IP地址" />
        </el-form-item>
        <el-form-item label="端口" prop="portNumber">
          <el-input v-model="form.portNumber" placeholder="请输入端口" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" placeholder="请输入密码" />
        </el-form-item>
        <el-form-item label="备注说明" prop="remarks">
          <el-input v-model="form.remarks" type="textarea" placeholder="请输入内容" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, defineExpose, getCurrentInstance } from 'vue';
import { listIot_camera_device_info, getIot_camera_device_info, delIot_camera_device_info, addIot_camera_device_info,
  updateIot_camera_device_info } from "@/api/system/iot_camera_device_info";
import { useDict } from '@/utils/dict';

// 获取全局属性和实例
const { proxy } = getCurrentInstance();
const { $message, $confirm, download, resetForm, parseTime } = proxy;

// 加载字典数据
const { camera_status: cameraStatus } = useDict("camera_status");

// 状态管理
const loading = ref(true);
const ids = ref([]);
const single = ref(true);
const multiple = ref(true);
const showSearch = ref(true);
const total = ref(0);
const iotCameraDeviceInfoList = ref([]);
const title = ref("");
const open = ref(false);
const searchKey = ref('');

// 查询参数
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  manufacturer: null,
  isNormal: null,
  installationLocation: null,
  deviceModel: null,
  installationDate: null,
  lastMaintenanceDate: null,
  ipAddress: null,
  portNumber: null,
  password: null,
  remarks: null
});

// 表单数据和校验规则
const form = reactive({
  deviceId: null,
  manufacturer: null,
  isNormal: null,
  installationLocation: null,
  deviceModel: null,
  installationDate: null,
  lastMaintenanceDate: null,
  ipAddress: null,
  portNumber: null,
  password: null,
  remarks: null
});

const rules = reactive({
  deviceId: [
    { required: true, message: "摄像头编号不能为空", trigger: "blur" }
  ],
  isNormal: [
    { required: true, message: "运行状态不能为空", trigger: "change" }
  ],
  installationLocation: [
    { required: true, message: "安装地点不能为空", trigger: "blur" }
  ],
  ipAddress: [
    { required: true, message: "IP地址不能为空", trigger: "blur" },
    { pattern: /^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$/, message: '请输入有效的IP地址', trigger: 'blur' }
  ],
  portNumber: [
    { required: true, message: "端口不能为空", trigger: "blur" },
    { type: 'number', message: '端口必须为数字值', trigger: 'blur' },
    { validator: (rule, value) => value >= 0 && value <= 65535, message: '端口范围为0-65535', trigger: 'blur' }
  ],
});

// 表单引用
const queryFormRef = ref(null);
const formRef = ref(null);

// 生命周期钩子
onMounted(() => {
  getList();
});

// 对话框生命周期钩子
const onDialogOpen = () => {
  console.log('对话框打开');
};

const onDialogClose = () => {
  console.log('对话框关闭');
};

// 查询列表
const getList = () => {
  loading.value = true;
  listIot_camera_device_info(queryParams).then(response => {
    iotCameraDeviceInfoList.value = response.rows;
    total.value = response.total;
    loading.value = false;
  }).catch(error => {
    console.error('获取列表失败:', error);
    $message.error('获取列表失败，请稍后重试');
    loading.value = false;
  });
};

// 取消按钮
const cancel = () => {
  open.value = false;
  reset();
};

// 表单重置
const reset = () => {
  form.deviceId = null;
  form.manufacturer = null;
  form.isNormal = null;
  form.installationLocation = null;
  form.deviceModel = null;
  form.installationDate = null;
  form.lastMaintenanceDate = null;
  form.ipAddress = null;
  form.portNumber = null;
  form.password = null;
  form.remarks = null;
};

// 搜索按钮操作
const handleQuery = () => {
  queryParams.pageNum = 1;
  getList();
};

// 重置按钮操作
const resetQuery = () => {
  resetForm("queryForm");
  handleQuery();
};

// 多选框选中数据
const handleSelectionChange = (selection) => {
  ids.value = selection.map(item => item.deviceId);
  single.value = selection.length !== 1;
  multiple.value = !selection.length;
};

// 新增按钮操作
const handleAdd = () => {
  reset();
  open.value = true;
  title.value = "添加存储摄像头设备";
};

// 修改按钮操作
const handleUpdate = (row) => {
  reset();
  const deviceId = row?.deviceId || ids.value[0];

  if (!deviceId) {
    $message.warning("请选择要修改的摄像头设备");
    return;
  }

  getIot_camera_device_info(deviceId).then(response => {
    Object.assign(form, response.data);
    open.value = true;
    title.value = "修改存储摄像头设备";
  }).catch(error => {
    console.error('获取设备信息失败:', error);
    $message.error('获取设备信息失败，请稍后重试');
  });
};

// 提交按钮
const submitForm = () => {
  formRef.value?.validate(valid => {
    if (valid) {
      if (form.deviceId != null) {
        updateIot_camera_device_info(form).then(response => {
          $message.success("修改成功");
          open.value = false;
          getList();
        }).catch(error => {
          console.error('修改设备失败:', error);
          $message.error('修改设备失败，请稍后重试');
        });
      } else {
        addIot_camera_device_info(form).then(response => {
          $message.success("新增成功");
          open.value = false;
          getList();
        }).catch(error => {
          console.error('新增设备失败:', error);
          $message.error('新增设备失败，请稍后重试');
        });
      }
    }
  });
};

// 删除按钮操作
const handleDelete = (row) => {
  // 直接使用传入的行数据，不需要预先选择
  const deviceId = row?.deviceId;

  if (!deviceId) {
    $message.warning("请选择要删除的摄像头设备");
    return;
  }

  $confirm('是否确认删除编号为"' + deviceId + '"的摄像头设备？', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    return delIot_camera_device_info(deviceId);
  }).then(() => {
    $message.success("删除成功");
    getList();
  }).catch(error => {
    console.error('删除设备失败:', error);
    $message.error('删除设备失败，请稍后重试');
  });
};

// 批量删除
const handleBatchDelete = () => {
  if (ids.value.length === 0) {
    $message.warning("请选择要删除的摄像头设备");
    return;
  }

  $confirm(`是否确认删除选中的 ${ids.value.length} 个摄像头设备？`, '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    return delIot_camera_device_info(ids.value);
  }).then(() => {
    $message.success("批量删除成功");
    getList();
  }).catch(error => {
    console.error('批量删除失败:', error);
    $message.error('批量删除失败，请稍后重试');
  });
};

// 导出按钮操作
const handleExport = () => {
  $message.loading('正在准备数据，请稍候...');
  download('system/iot_camera_device_info/export', {
    ...queryParams
  }, `iot_camera_device_info_${new Date().getTime()}.xlsx`);
};

// 暴露方法给父组件
defineExpose({
  getList
});
</script>
