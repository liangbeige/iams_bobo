<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="120px">
      <el-form-item label="项目代号" prop="bianhao">
        <el-input v-model="queryParams.bianhao" placeholder="请输入项目代号" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="项目名称" prop="name">
        <el-input v-model="queryParams.name" placeholder="请输入项目名称" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="项目负责人" prop="leader">
        <el-input v-model="queryParams.leader" placeholder="请输入项目负责人" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <!-- <el-form-item label="项目起始时间" prop="startTime">
        <el-date-picker clearable v-model="queryParams.startTime" type="date" value-format="YYYY-MM-DD"
          placeholder="请选择项目起始时间">
        </el-date-picker>
      </el-form-item>
      <el-form-item label="项目终止时间" prop="endTime">
        <el-date-picker clearable v-model="queryParams.endTime" type="date" value-format="YYYY-MM-DD"
          placeholder="请选择项目终止时间">
        </el-date-picker>
      </el-form-item> -->
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd"
          v-hasPermi="['manage:project:add']">新增</el-button>
      </el-col>
      <!--      <el-col :span="1.5">-->
      <!--        <el-button-->
      <!--          type="success"-->
      <!--          plain-->
      <!--          icon="Edit"-->
      <!--          :disabled="single"-->
      <!--          @click="handleUpdate"-->
      <!--          v-hasPermi="['manage:project:edit']"-->
      <!--        >修改</el-button>-->
      <!--      </el-col>-->

      <el-col :span="1.5">
        <el-button type="warning" plain icon="Download" @click="handleExport"
          v-hasPermi="['manage:project:export']">导出</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete"
          v-hasPermi="['manage:project:remove']">删除</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="projectList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
<!--      <el-table-column label="项目ID" align="center" prop="id" />-->
      <el-table-column label="项目代号" align="center" prop="bianhao" width="280" />
      <el-table-column label="项目名称" align="center" prop="name" width="150" />
      <el-table-column label="项目描述" align="center" prop="description" />
      <el-table-column label="项目负责人" align="center" prop="leader" width="100" />
      <!-- <el-table-column label="项目起始时间" align="center" prop="startTime" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.startTime, '{y}-{m}-{d}') }}</span>
        </template>
</el-table-column>
<el-table-column label="项目终止时间" align="center" prop="endTime" width="180">
  <template #default="scope">
          <span>{{ parseTime(scope.row.endTime, '{y}-{m}-{d}') }}</span>
        </template>
</el-table-column> -->
      <el-table-column label="档案数量" align="center" prop="archiveCount" width="120"/>
      <el-table-column label="项目证明书" align="center" width="130">
        <template #default="scope">
          <el-tag v-if="scope.row.hasCertificate" type="success" size="small">已上传</el-tag>
          <el-tag v-else type="info" size="small">未上传</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="备注" align="center" prop="remark" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleViewArchives(scope.row)"
            v-hasPermi="['manage:project:query']">查看</el-button>
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)"
            v-hasPermi="['manage:project:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)"
            v-hasPermi="['manage:project:remove']">删除</el-button>
          <!--          <el-button link type="primary" icon="Upload" @click="handleUploadCertificate(scope.row)" v-hasPermi="['manage:project:upload']">上传</el-button>-->
          <el-button v-if="!scope.row.hasCertificate" link type="primary" icon="Upload"
            @click="handleUploadCertificate(scope.row)" v-hasPermi="['manage:project:upload']">
            上传
          </el-button>
          <el-button v-else link type="primary" icon="Upload"
            @click="handleUploadCertificate(scope.row)" v-hasPermi="['manage:project:upload']">
            重新上传
          </el-button>
          <el-button v-if="scope.row.hasCertificate" link type="primary" icon="View" @click="viewCertificate(scope.row)"
            v-hasPermi="['manage:project:view']">
            查看证书
          </el-button>

        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 上传弹窗 -->
    <el-dialog title="上传项目证明书" v-model="uploadDialogVisible" width="500px">
      <el-upload class="upload-demo" drag :action="uploadUrl" :before-upload="beforeUpload"
        :on-success="handleUploadSuccess" :show-file-list="false" :headers="headers">
        <el-icon class="el-icon--upload"><upload-filled /></el-icon>
        <div class="el-upload__text">拖拽文件到此处或<em>点击上传</em></div>
        <template #tip>
          <div class="el-upload__tip">仅支持上传PDF文件（大小不超过10MB）</div>
        </template>
      </el-upload>
    </el-dialog>

    <!-- PDF预览弹窗 -->
    <el-dialog title="项目证明书预览" v-model="previewDialogVisible" width="80%" top="5vh">
      <iframe :src="url" frameborder="0" width="100%" height="600px">
      </iframe>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="previewDialogVisible = false">关 闭</el-button>
          <el-button type="primary" @click="downloadCertificate()">下 载</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 添加或修改项目管理对话框 -->
    <el-dialog :title="title" v-model="open" width="500px" append-to-body>
      <el-form ref="projectRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="项目代号" prop="bianhao">
          <el-input v-model="form.bianhao" placeholder="请输入项目代号" />
        </el-form-item>
        <el-form-item label="项目名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入项目名称" />
        </el-form-item>
        <el-form-item label="项目描述" prop="description">
          <el-input v-model="form.description" type="textarea" placeholder="请输入内容" />
        </el-form-item>
        <el-form-item label="项目负责人" prop="leader">
          <el-input v-model="form.leader" placeholder="请输入项目负责人" />
        </el-form-item>
        <!-- <el-form-item label="项目起始时间" prop="startTime">
          <el-date-picker clearable
            v-model="form.startTime"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="请选择项目起始时间">
          </el-date-picker>
        </el-form-item>
        <el-form-item label="项目终止时间" prop="endTime">
          <el-date-picker clearable
            v-model="form.endTime"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="请选择项目终止时间">
          </el-date-picker>
        </el-form-item> -->
        <!-- <el-form-item label="项目名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入项目名称" />
        </el-form-item> -->
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


<script>
// 显示的定义组件名称，和路由地址保持一致，
// 在系统管理->菜单管理 中可以配置是否开启页面缓存
export default {
  name: 'Proj-list'
}
</script>


<script setup>
import { listProject, getProject, delProject, addProject, updateProject } from "@/api/manage/project";
import { useRouter } from 'vue-router'
import { ref } from "vue";
import { getToken } from "@/utils/auth.js";
import axios from "axios";
const selectedProjects = ref([]); // 新增存储选中项目对象

const { proxy } = getCurrentInstance();
const router = useRouter()

const projectList = ref([]);
const open = ref(false);
const loading = ref(true);
const showSearch = ref(true);
const ids = ref([]);
const single = ref(true);
const multiple = ref(true);
const total = ref(0);
const title = ref("");

// 新增证明书上传状态
const uploadDialogVisible = ref(false);
const previewDialogVisible = ref(false);
const previewUrl = ref("");
const currentProject = ref(null);
const certificateUrl = ref("");
const url = ref("");

// const headers = computed(() => {
//   const token = userStore.token || localStorage.getItem('token') || '';
//   return {
//     Authorization: `Bearer ${token}`
//   };
// });

const headers = ref({ Authorization: "Bearer " + getToken() });

// 上传URL（根据实际API地址修改）
const uploadUrl = computed(() => {
  const projectId = currentProject.value ? currentProject.value.id : '';
  return `${import.meta.env.VITE_APP_BASE_API}/minio/uploadProjectCertificate?projectId=${projectId}&&originalFileName=${originalFileName.value}`;
});
// const uploadUrl = computed(() => {
//   return `${import.meta.env.VITE_APP_BASE_API}/minio/uploadProjectCertificate`;
// });

// 文件上传前的校验
const beforeUpload = (file) => {
  console.log("Upload URL:", uploadUrl.value);
  console.log("Headers:", headers.value);
  console.log("Project ID:", currentProject.value.id);
  console.log('Before upload file:', file);
  const isPDF = file.type === 'application/pdf';
  const isLt10M = file.size / 1024 / 1024 < 10;

  if (!isPDF) {
    // proxy.$modal.msgSuccess('上传成功');
    proxy.$modal.msgError('只能上传 PDF 格式文件！');
    return false;
  }
  if (!isLt10M) {
    proxy.$modal.msgError('文件大小不能超过 10MB!');
    return false;
  }

  return true;
};

const tempUploadUrl = ref("");
const originalFileName = ref(null);

const handleUploadCertificate = async (row) => {
  if (row.hasCertificate) {
    originalFileName.value = row.certUrl.split('/iams-project-cert/')[1];
    console.log("原始文件名:", originalFileName.value);
  }
  uploadDialogVisible.value = true;
  currentProject.value = row;
};


// 上传成功处理
const handleUploadSuccess = async (response, file) => {
  console.log("上传响应:", response);

  if (response.code === 200) {
    proxy.$modal.msgSuccess('上传成功');
    getList();

    // 更新项目信息
    // currentProject.value.hasCertificate = true;
    // currentProject.value.certUrl = response.data.url;

    // // 调用 API 更新数据库
    // try {
    //   const updateRes = await updateProject(currentProject.value);
    //   if (updateRes.code === 200) {
    //     proxy.$modal.msgSuccess('项目信息已更新');
    //     getList(); // 刷新列表
    //   } else {
    //     proxy.$modal.msgError('更新失败');
    //   }
    // } catch (err) {
    //   console.error('更新项目失败:', err);
    //   proxy.$modal.msgError('更新项目失败');
    // }

    uploadDialogVisible.value = false;
  } else {
    proxy.$modal.msgError(response.msg || '上传失败');
  }
};

// 查看证明书
const viewCertificate = async (row) => {
  url.value = row.certUrl;
  currentProject.value = row;
  previewDialogVisible.value = true;

  // try {
  //   loading.value = true;

  //   const url = `${import.meta.env.VITE_APP_BASE_API}/minio/getProjectCertificateUrl`;
  //   console.log('请求地址:', url); // 打印地址用于调试

  //   const res = await axios.get(url, {
  //     params: {
  //       projectId: row.id,
  //       certificateName: row.certificateName
  //     },
  //     headers: {
  //       'Authorization': headers.value.Authorization,
  //     }
  //   });

  //   if (res.code === 200) {
  //     previewUrl.value = res.data;
  //     certificateUrl.value = res.data;
  //     previewDialogVisible.value = true;
  //   } else {
  //     proxy.$modal.msgError(res.msg);
  //   }
  // } catch (err) {
  //   console.error('请求失败:', err);
  //   proxy.$modal.msgError('获取证明书失败');
  // } finally {
  //   loading.value = false;
  // }
};

// 下载证明书
const downloadCertificate = () => {
  downloadFileWithFetch(url.value, `项目证明书-${currentProject.value.bianhao}.pdf`);
};

const downloadFileWithFetch = async (presignedUrl, filename) => {
  try {
    const response = await fetch(presignedUrl);
    if (!response.ok) throw new Error('文件下载失败');

    const blob = await response.blob(); // 获取 Blob 数据
    const downloadUrl = window.URL.createObjectURL(blob);

    const a = document.createElement('a');
    a.href = downloadUrl;
    a.download = filename;
    document.body.appendChild(a);
    a.click();
    window.URL.revokeObjectURL(downloadUrl); // 释放内存
    document.body.removeChild(a);
  } catch (error) {
    console.error('下载出错:', error);
  }
};

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    bianhao: null,
    name: null,
    leader: null,
    startTime: null,
    endTime: null,

  },
  rules: {
    bianhao: [
      { required: true, message: "项目代号不能为空", trigger: "blur" }
    ],
    name: [
      { required: true, message: "项目名称不能为空", trigger: "blur" }
    ],
    leader: [
      { required: true, message: "项目负责人不能为空", trigger: "blur" }
    ],
  }
});

const { queryParams, form, rules } = toRefs(data);

// const handleUploadCertificate = (row) => {
//   router.push({
//     path: '/manage/project/upload-certificate',
//     query: { projectId: row.id }
//   })
// }

/** 查询项目管理列表 */
function getList() {
  loading.value = true;
  listProject(queryParams.value).then(response => {
    projectList.value = response.rows;
    total.value = response.total;
    loading.value = false;
  });
}

// 取消按钮
function cancel() {
  open.value = false;
  reset();
}

// 查看项目相关档案
function handleViewArchives(row) {
  router.push({
    path: '/manage/project/proj-detail',
    query: {
      projectId: row.id,
      projectName: row.name
    }
  })
}

// 表单重置
function reset() {
  form.value = {
    id: null,
    bianhao: null,
    name: null,
    description: null,
    leader: null,
    startTime: null,
    endTime: null,
    archiveCount: null,
    createTime: null,
    createBy: null,
    updateTime: null,
    updateBy: null,
    remark: null
  };
  proxy.resetForm("projectRef");
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
const handleSelectionChange = (selection) => {
  ids.value = selection.map(item => item.id);
  selectedProjects.value = selection; // 存储完整项目对象
  single.value = selection.length !== 1;
  multiple.value = !selection.length;
};

/** 新增按钮操作 */
function handleAdd() {
  reset();
  open.value = true;
  title.value = "添加项目管理";
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset();
  const _id = row.id || ids.value
  getProject(_id).then(response => {
    form.value = response.data;
    open.value = true;
    title.value = "修改项目管理";
  });
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["projectRef"].validate(valid => {
    if (valid) {
      if (form.value.id != null) {
        updateProject(form.value).then(response => {
          proxy.$modal.msgSuccess("修改成功");
          open.value = false;
          getList();
        });
      } else {
        addProject(form.value).then(response => {
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
  // 单个删除校验
  if (row) {
    if (row.archiveCount > 0) {
      proxy.$modal.msgError(`项目"${row.name}"包含${row.archiveCount}份档案，无法删除！`);
      return;
    }

    const _ids = row.id;
    proxy.$modal.confirm(`确认删除项目"${row.name}"?`).then(() => {
      return delProject(_ids);
    }).then(() => {
      getList();
      proxy.$modal.msgSuccess("删除成功");
    }).catch(() => {});
    return;
  }
  // 批量删除校验
  const hasArchives = selectedProjects.value.some(project => project.archiveCount > 0);
  if (hasArchives) {
    const names = selectedProjects.value
        .filter(p => p.archiveCount > 0)
        .map(p => p.name)
        .join(', ');
    proxy.$modal.msgError(`以下项目包含档案无法删除：${names}`);
    return;
  }

  // 执行批量删除
  const _ids = ids.value.join(",");
  proxy.$modal.confirm(`确认删除选中的${selectedProjects.value.length}个项目?`).then(() => {
    return delProject(_ids);
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess("删除成功");
  }).catch(() => {});
}
/** 导出按钮操作 */
function handleExport() {
  proxy.download('manage/project/export', {
    ...queryParams.value
  }, `project_${new Date().getTime()}.xlsx`)
}

getList();
</script>
