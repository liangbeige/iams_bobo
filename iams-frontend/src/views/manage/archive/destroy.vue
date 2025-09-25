<template>
    <div class="app-container">
        <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="100px">
            <el-form-item label="档号" prop="danghao">
                <el-input v-model="queryParams.danghao" placeholder="请输入档号" clearable @keyup.enter="handleQuery" />
            </el-form-item>
            <el-form-item label="档案名称" prop="name">
                <el-input v-model="queryParams.name" placeholder="请输入档案名称" clearable @keyup.enter="handleQuery" />
            </el-form-item>
            <el-form-item label="档案门类" prop="categoryId">
                <el-input v-model="queryParams.categoryId" placeholder="请输入档案门类" clearable @keyup.enter="handleQuery" />
            </el-form-item>
            <el-form-item label="档案状态" prop="status">
                <el-select v-model="queryParams.status" placeholder="请选择档案状态" clearable>
                    <el-option label="已归档" value="Archived" />
                    <el-option label="销毁中" value="Destroying" />
                    <el-option label="已销毁" value="Destroyed" />
                </el-select>
            </el-form-item>
            <el-form-item>
                <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
                <el-button icon="Refresh" @click="resetQuery">重置</el-button>
            </el-form-item>
        </el-form>

        <el-row :gutter="10" class="mb8">
            <el-col :span="1.5">
                <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleBatchDestroy"
                           v-hasPermi="['manage:archive:destroy']">
                    批量提交销毁申请
                </el-button>
            </el-col>
            <el-col :span="1.5">
                <el-button type="info" plain icon="View" @click="handleViewDestroyHistory">
                    查看销毁历史
                </el-button>
            </el-col>
            <el-col :span="1.5">
                <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['manage:archive:export']">
                    导出
                </el-button>
            </el-col>
            <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
        </el-row>

        <el-table v-loading="loading" :data="archiveList" @selection-change="handleSelectionChange">
            <el-table-column type="selection" width="55" align="center" />
            <el-table-column label="档号" align="center" prop="danghao"  width="120"/>
            <el-table-column label="档案名称" align="center" prop="name"  />
            <el-table-column label="保密级别" align="center" prop="secretLevel" width="70">
                <template #default="scope">
                    <dict-tag :options="iams_secret_level" :value="scope.row.secretLevel" />
                </template>
            </el-table-column>
            <el-table-column label="保密期限" align="center" prop="secretPeroid" width="70">
                <template #default="scope">
                    <dict-tag :options="iams_secret_period" :value="scope.row.secretPeroid" />
                </template>
            </el-table-column>
            <el-table-column label="载体类型" align="center" prop="carrierType" width="100">
                <template #default="scope">
                    <dict-tag :options="iams_carrier_type" :value="scope.row.carrierType" />
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
            <el-table-column label="档案状态" align="center" prop="status" width="80">
                <template #default="scope">
                    <dict-tag :options="iams_archive_status" :value="scope.row.status" />
                </template>
            </el-table-column>
            <el-table-column label="档案门类" align="center" prop="categoryId" width="80" />
            <el-table-column label="实体档案位置" align="center" prop="location" show-overflow-tooltip>
                <template #default="scope">
                    <el-tag v-if="scope.row.shitiLocation" type="info" size="small">
                        {{ scope.row.shitiLocation }}-{{ scope.row.exactLocation }}
                    </el-tag>
                    <el-tag v-else type="warning" size="small">
                        -
                    </el-tag>
                </template>
            </el-table-column>
            <el-table-column label="RFID状态" align="center" prop="rfid" width="80">
                <template #default="scope">
                    <el-tag v-if="scope.row.rfid && scope.row.rfid.endsWith('05')" type="success" size="small">
                        上架
                    </el-tag>
                    <el-tag v-else-if="scope.row.rfid && scope.row.rfid.endsWith('00')" type="warning" size="small">
                        下架
                    </el-tag>
                    <el-tag v-else type="info" size="small">
                        未知
                    </el-tag>
                </template>
            </el-table-column>
            <el-table-column label="可用性状态" align="center" prop="availability" width="80">
                <template #default="scope">
                    <el-tag v-if="scope.row.availability === 0" type="success" size="small">
                        在架
                    </el-tag>
                    <el-tag v-else-if="scope.row.availability === 1" type="warning" size="small">
                        待下架
                    </el-tag>
                    <el-tag v-else-if="scope.row.availability === 2" type="info" size="small">
                        待上架
                    </el-tag>
                    <el-tag v-else type="danger" size="small">
                        未知
                    </el-tag>
                </template>
            </el-table-column>
            <el-table-column label="归档时间" align="center" prop="guidangTime" width="100">
                <template #default="scope">
                    <span>{{ parseTime(scope.row.guidangTime, '{y}-{m}-{d}') }}</span>
                </template>
            </el-table-column>

            <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="220">
                <template #default="scope">
                    <el-button link type="primary" @click="handleDetail(scope.row)" v-hasPermi="['manage:archive:query']">
                        详情
                    </el-button>
                    <el-button v-if="!scope.row.hasDestructionCertificate" link type="primary" icon="Upload"
                               @click="handleUploadCertificate(scope.row)" v-hasPermi="['manage:archive:edit']">
                        上传凭证
                    </el-button>
                    <el-button v-if="scope.row.hasDestructionCertificate" link type="primary" icon="View" @click="viewCertificate(scope.row)"
                               v-hasPermi="['manage:archive:query']">
                        查看凭证
                    </el-button>
                    <el-button
                        v-if="scope.row.status === 'Archived' && scope.row.availability === 0"
                        link
                        type="danger"
                        @click="handleDestroy(scope.row)"
                        v-hasPermi="['manage:archive:destroy']">
                        提交销毁
                    </el-button>
                    <el-button
                        v-if="scope.row.status === 'Destroying' && scope.row.availability === 1"
                        link
                        type="warning"
                        @click="handleCancelDestroy(scope.row)"
                        v-hasPermi="['manage:archive:destroy']">
                        撤销销毁
                    </el-button>
                    <el-button
                        v-if="scope.row.status === 'Destroying' && scope.row.availability === 1 && scope.row.rfid && scope.row.rfid.endsWith('00')"
                        link
                        type="danger"
                        @click="handleCompleteDestroy(scope.row)"
                        v-hasPermi="['manage:archive:destroy']">
                        确认销毁
                    </el-button>
                </template>
            </el-table-column>
        </el-table>

        <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum"
                    v-model:limit="queryParams.pageSize" @pagination="getList" />

        <el-dialog title="上传销毁佐证材料" v-model="uploadDialogVisible" width="500px" append-to-body>
            <el-upload class="upload-demo" drag :action="uploadUrl" :headers="headers" :before-upload="beforeUpload"
                       :on-success="handleUploadSuccess" :on-error="handleUploadError" :show-file-list="false">
                <el-icon class="el-icon--upload"><upload-filled /></el-icon>
                <div class="el-upload__text">拖拽文件到此处或<em>点击上传</em></div>
                <template #tip>
                    <div class="el-upload__tip">仅支持上传PDF文件（大小不超过10MB）</div>
                </template>
            </el-upload>
        </el-dialog>

        <el-dialog title="销毁佐证材料预览" v-model="previewDialogVisible" width="80%" top="5vh" append-to-body>
            <iframe v-if="url" :src="url" frameborder="0" width="100%" height="600px">
            </iframe>
            <template #footer>
                <div class="dialog-footer">
                    <el-button @click="previewDialogVisible = false">关 闭</el-button>
                    <el-button type="primary" @click="downloadCertificate()">下 载</el-button>
                </div>
            </template>
        </el-dialog>

        <el-dialog v-model="destroyDialog.visible" title="档案销毁申请" width="600px" :close-on-click-modal="false">
            <div class="destroy-content">
                <div class="archive-info-section" style="margin-bottom: 20px; padding: 15px; background-color: #f5f7fa; border-radius: 4px;">
                    <h4 style="margin: 0 0 10px 0; color: #303133;">待销毁档案信息</h4>
                    <div v-if="destroyDialog.selectedArchives.length === 1" class="single-archive">
                        <p style="margin: 5px 0; color: #606266;">
                            <strong>档案名称:</strong> {{ destroyDialog.selectedArchives[0].name }}
                        </p>
                        <p style="margin: 5px 0; color: #606266;">
                            <strong>档号:</strong> {{ destroyDialog.selectedArchives[0].danghao }}
                        </p>
                        <p style="margin: 5px 0; color: #606266;">
                            <strong>位置:</strong> {{ destroyDialog.selectedArchives[0].shitiLocation }}-{{ destroyDialog.selectedArchives[0].exactLocation }}
                        </p>
                    </div>
                    <div v-else class="multiple-archives">
                        <p style="margin: 5px 0; color: #606266;">
                            <strong>选中档案数量:</strong> {{ destroyDialog.selectedArchives.length }} 个
                        </p>
                        <div style="max-height: 120px; overflow-y: auto; border: 1px solid #e4e7ed; border-radius: 4px; padding: 8px;">
                            <div v-for="archive in destroyDialog.selectedArchives" :key="archive.id"
                                 style="padding: 4px 0; border-bottom: 1px solid #f0f0f0; font-size: 12px;">
                                {{ archive.danghao }} - {{ archive.name }}
                            </div>
                        </div>
                    </div>
                </div>

                <el-form ref="destroyFormRef" :model="destroyForm" :rules="destroyRules" label-width="120px">
                    <el-form-item label="申请人" prop="applicant">
                        <el-select v-model="destroyForm.applicant" placeholder="请选择申请人">
                            <el-option v-for="user in userList" :key="user.id" :label="user.userName" :value="user.userName" />
                        </el-select>
                    </el-form-item>

                    <el-form-item label="销毁原因" prop="reason">
                        <el-select v-model="destroyForm.reason" placeholder="请选择销毁原因">
                            <el-option label="保管期限到期" value="保管期限到期" />
                            <el-option label="档案损坏" value="档案损坏" />
                            <el-option label="内容过时失效" value="内容过时失效" />
                            <el-option label="重复归档" value="重复归档" />
                            <el-option label="其他" value="其他" />
                        </el-select>
                    </el-form-item>

                    <el-form-item label="详细说明" prop="description">
                        <el-input v-model="destroyForm.description" type="textarea" :rows="4"
                                  placeholder="请详细说明销毁原因和依据" />
                    </el-form-item>

                    <el-form-item label="预计销毁时间" prop="scheduledDate">
                        <el-date-picker v-model="destroyForm.scheduledDate" type="datetime"
                                        placeholder="选择预计销毁时间" value-format="YYYY-MM-DD HH:mm:ss" />
                    </el-form-item>
                </el-form>
            </div>

            <template #footer>
        <span class="dialog-footer">
          <el-button @click="destroyDialog.visible = false">取 消</el-button>
          <el-button type="danger" @click="handleSubmitDestroyApplication" :loading="destroyDialog.loading">
            提交申请
          </el-button>
        </span>
            </template>
        </el-dialog>

        <el-dialog v-model="historyDialog.visible" title="档案销毁历史" width="1000px" append-to-body>
            <el-form :model="historyQueryParams" :inline="true" class="mb-4">
                <el-form-item label="申请人">
                    <el-input v-model="historyQueryParams.applicant" placeholder="申请人" clearable />
                </el-form-item>
                <el-form-item label="状态">
                    <el-select v-model="historyQueryParams.status" placeholder="请选择状态" clearable>
                        <el-option label="待下架" value="待下架" />
                        <el-option label="已销毁" value="已销毁" />
                        <el-option label="销毁失败" value="销毁失败" />
                    </el-select>
                </el-form-item>
                <el-form-item>
                    <el-button type="primary" @click="getDestroyHistory">查询</el-button>
                    <el-button @click="resetHistoryQuery">重置</el-button>
                </el-form-item>
            </el-form>

            <el-table :data="historyList" style="width: 100%" v-loading="historyLoading">
                <el-table-column prop="id" label="申请编号" align="center" width="120" />
                <el-table-column prop="applicant" label="申请人" align="center" width="120">
                    <template #default="scope">
                        <span>{{ extractApplicantFromPurpose(scope.row.purpose) || '-' }}</span>
                    </template>
                </el-table-column>
                <el-table-column prop="purpose" label="销毁原因" align="center" width="240">
                    <template #default="scope">
                        <span>{{ extractDestroyReason(scope.row.purpose) }}</span>
                    </template>
                </el-table-column>
                <el-table-column prop="startApplyTime" label="申请时间" align="center" width="180">
                    <template #default="scope">
                        <span>{{ parseTime(scope.row.startApplyTime) }}</span>
                    </template>
                </el-table-column>
                <el-table-column prop="status" label="状态" align="center" width="100">
                    <template #default="scope">
                        <el-tag :type="getStatusTagType(scope.row.status)">
                            {{ scope.row.status }}
                        </el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="endApplyTime" label="完成时间" align="center" width="180">
                    <template #default="scope">
                        <span>{{ parseTime(scope.row.endApplyTime) }}</span>
                    </template>
                </el-table-column>
            </el-table>

            <pagination v-show="historyTotal > 0" :total="historyTotal"
                        v-model:page="historyQueryParams.pageNum" v-model:limit="historyQueryParams.pageSize"
                        @pagination="getDestroyHistory" />

            <template #footer>
                <div class="dialog-footer">
                    <el-button @click="historyDialog.visible = false">关 闭</el-button>
                </div>
            </template>
        </el-dialog>
    </div>
</template>

<script setup name="ArchiveDestroy">
import {
    listAvailableArchives,
    submitDestroyApplication,
    batchSubmitDestroyApplication,
    listDestroyRecords,
    cancelDestroyByArchiveId,
    completeDestroy,
} from "@/api/manage/ArchiveDestroyRecord.js";
import {
    listArchive,
    getArchive
} from "@/api/manage/archive";
import { listProject } from "@/api/manage/project";
import { listUserByRoleId } from "@/api/system/user";
import useUserStore from '@/store/modules/user';
import { getCurrentInstance } from 'vue'
import { ElMessage, ElMessageBox } from "element-plus";
import { onMounted } from 'vue';
import { useRouter } from 'vue-router'
import { getFondsCategories } from "@/api/manage/category";
import { parseTime } from "../../../utils/ruoyi.js";
import {getToken} from "@/utils/auth";

const router = useRouter()
const { proxy } = getCurrentInstance();
const userStore = useUserStore()

// 字典数据
const {
    iams_carrier_type,
    iams_archive_status,
    iams_retention_period,
    iams_secret_period,
    iams_secret_level
} = proxy.useDict('iams_carrier_type', 'iams_archive_status', 'iams_retention_period', 'iams_secret_period', 'iams_secret_level');

// 基础数据
const archiveList = ref([]);
const loading = ref(true);
const showSearch = ref(true);
const ids = ref([]);
const multiple = ref(true);
const total = ref(0);
const projectList = ref([]);
const userList = ref([]);
// [MODIFIED] 认证头信息
const headers = ref({ Authorization: "Bearer " + getToken() });

// 查询参数
const queryParams = reactive({
    pageNum: 1,
    pageSize: 10,
    danghao: null,
    name: null,
    rfid: null,
    secretLevel: null,
    secretPeroid: null,
    carrierType: null,
    categoryId: null,
    fondsName: null,
    projectId: null,
    status: null,
    availability: null
});


// === 佐证材料上传与预览 ===
const uploadDialogVisible = ref(false);
const previewDialogVisible = ref(false);
const url = ref(""); // 用于预览和下载的URL
const currentArchive = ref(null); // 用于存储当前操作的档案对象

// 上传URL，动态绑定当前操作的档案ID
const uploadUrl = computed(() => {
    // 确保 currentArchive.value 和其 id 属性存在
    const archiveId = currentArchive.value ? currentArchive.value.id : '';
    // [COMMENT] 后端接口地址，VITE_APP_BASE_API是环境变量，指向后端服务地址
    return `${import.meta.env.VITE_APP_BASE_API}/manage/archive/uploadDestructionCertificate?archiveId=${archiveId}`;
});

// 点击“上传凭证”按钮
const handleUploadCertificate = (row) => {
    currentArchive.value = row; // 保存当前档案行数据
    uploadDialogVisible.value = true;
};

// 文件上传前的校验
const beforeUpload = (file) => {
    const isPDF = file.type === 'application/pdf';
    const isLt10M = file.size / 1024 / 1024 < 10;
    if (!isPDF) {
        proxy.$modal.msgError('只能上传 PDF 格式文件！');
        return false;
    }
    if (!isLt10M) {
        proxy.$modal.msgError('文件大小不能超过 10MB!');
        return false;
    }
    return true;
};

// 文件上传成功后的回调
const handleUploadSuccess = (response, file) => {
    if (response.code === 200) {
        proxy.$modal.msgSuccess('上传成功');
        uploadDialogVisible.value = false;
        getList(); // 刷新列表，以更新“上传凭证”/“查看凭证”按钮的状态和凭证URL
    } else {
        proxy.$modal.msgError(response.msg || '上传失败');
    }
};

// [ADDED] 文件上传失败的回调
const handleUploadError = (err) => {
    proxy.$modal.msgError("上传失败，请检查网络或联系管理员");
    console.error("Upload error:", err);
};


// 查看凭证
const viewCertificate = (row) => {
    // [COMMENT] 后端返回的档案对象中应包含 destructionCertificateUrl 字段, 并且在前端实体中映射为 certUrl 或直接使用
    // 这里我们假设它被映射为 `destructionCertificateUrl`
    url.value = row.destructionCertificateUrl;
    currentArchive.value = row;
    previewDialogVisible.value = true;
};

// 下载凭证
const downloadCertificate = () => {
    if (!currentArchive.value || !url.value) {
        proxy.$modal.msgError("凭证URL无效");
        return;
    }
    const fileName = `销毁佐证-${currentArchive.value.danghao || 'file'}.pdf`;
    downloadFileWithFetch(url.value, fileName);
};

// 下载文件的辅助函数
const downloadFileWithFetch = async (presignedUrl, filename) => {
    try {
        const response = await fetch(presignedUrl);
        if (!response.ok) throw new Error('文件下载失败');
        const blob = await response.blob();
        const downloadUrl = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = downloadUrl;
        a.download = filename;
        document.body.appendChild(a);
        a.click();
        window.URL.revokeObjectURL(downloadUrl);
        document.body.removeChild(a);
    } catch (error) {
        console.error('下载出错:', error);
        proxy.$modal.msgError('下载失败');
    }
};

// === 销毁申请 ===

// 销毁申请弹窗
const destroyDialog = reactive({
    visible: false,
    loading: false,
    selectedArchives: []
});

// 销毁申请表单
const destroyForm = reactive({
    applicant: '',
    reason: '',
    description: '',
    scheduledDate: ''
});

const destroyFormRef = ref();

// 表单验证规则
const destroyRules = {
    applicant: [
        { required: true, message: '请选择申请人', trigger: 'change' }
    ],
    reason: [
        { required: true, message: '请选择销毁原因', trigger: 'change' }
    ],
    description: [
        { required: true, message: '请填写详细说明', trigger: 'blur' }
    ],
    scheduledDate: [
        { required: true, message: '请选择预计销毁时间', trigger: 'change' }
    ]
};

// 销毁历史弹窗
const historyDialog = reactive({
    visible: false
});

const historyList = ref([]);
const historyLoading = ref(false);
const historyTotal = ref(0);

// 历史查询参数
const historyQueryParams = reactive({
    pageNum: 1,
    pageSize: 10,
    applicant: '',
    status: ''
});

// 页面初始化
onMounted(() => {
    getList();
    getProjectList();
    getUserListByRoleId({ roleId: 10007 });
});

/** 查询档案列表 */
function getList() {
    loading.value = true;
    const params = {
        pageNum: queryParams.pageNum,
        pageSize: queryParams.pageSize,
        danghao: queryParams.danghao || undefined,
        name: queryParams.name || undefined,
        rfid: queryParams.rfid || undefined,
        carrierType: queryParams.carrierType || undefined,
        categoryId: queryParams.categoryId || undefined,
        fondsName: queryParams.fondsName || undefined,
        projectId: queryParams.projectId || undefined,
        status: queryParams.status || undefined,
        availability: queryParams.availability || undefined
    };
    const filteredParams = Object.fromEntries(
        Object.entries(params).filter(([_, v]) => v !== undefined)
    );

    listAvailableArchives(filteredParams)
        .then(response => {
            archiveList.value = response.rows || [];
            total.value = response.total || 0;
        })
        .catch(error => {
            console.error('获取档案列表失败:', error);
            archiveList.value = [];
            total.value = 0;
            ElMessage.error('获取数据失败，请稍后重试');
        })
        .finally(() => {
            loading.value = false;
        });
}

/** 从销毁原因中提取申请人 */
function extractApplicantFromPurpose(purpose) {
    if (!purpose) return '';
    const applicantMatch = purpose.match(/申请人：(\S+)/);
    return applicantMatch ? applicantMatch[1] : '';
}

/** 从销毁原因中提取销毁原因部分 */
function extractDestroyReason(purpose) {
    if (!purpose) return '';
    return purpose.replace(/申请人：\S+/, '').replace(/预计销毁时间：.+/, '').trim();
}

/** 获取项目列表 */
function getProjectList() {
    listProject({
        pageNum: 1,
        pageSize: 100,
        status: '0'
    }).then(response => {
        if (response.code === 200) {
            projectList.value = response.rows || [];
        }
    });
}

/** 获取用户列表 */
function getUserListByRoleId(roleId) {
    listUserByRoleId(roleId).then(response => {
        userList.value = response;
    });
}

/** 搜索按钮操作 */
function handleQuery() {
    queryParams.pageNum = 1;
    getList();
}

/** 重置按钮操作 */
function resetQuery() {
    proxy.resetForm("queryRef");
    queryParams.projectId = null;
    queryParams.fondsName = null;
    queryParams.status = null;
    handleQuery();
}

/** 多选框选中数据 */
function handleSelectionChange(selection) {
    ids.value = selection.map(item => item.id);
    multiple.value = !selection.length;
    destroyDialog.selectedArchives = selection;
}

/** 查看详情 */
function handleDetail(row) {
    router.push({ path: "/manage/archive/arc-detail", query: { id: row.id } });
}

/** 单个销毁申请 */
function handleDestroy(row) {
    const checkResult = checkArchiveCanDestroy(row);
    if (!checkResult.canDestroy) {
        ElMessage.warning(checkResult.message);
        return;
    }
    destroyDialog.selectedArchives = [row];
    openDestroyDialog();
}

/** 批量销毁申请 */
function handleBatchDestroy() {
    if (destroyDialog.selectedArchives.length === 0) {
        ElMessage.warning('请先选择要销毁的档案');
        return;
    }
    const batchCheckResult = batchCheckArchivesCanDestroy(destroyDialog.selectedArchives);

    if (batchCheckResult.hasInvalid) {
        const errorMessages = batchCheckResult.invalidArchives.map(item =>
            `• 档案【${item.archive.name || item.archive.danghao}】：${item.reason}`
        );
        const errorMsg = `以下 ${batchCheckResult.invalidArchives.length} 个档案无法销毁：\n\n${errorMessages.join('\n')}`;

        if (batchCheckResult.validArchives.length > 0) {
            ElMessageBox.confirm(
                `${errorMsg}\n\n是否继续处理其余 ${batchCheckResult.validArchives.length} 个可销毁的档案？`,
                '部分档案无法销毁',
                {
                    confirmButtonText: `继续处理${batchCheckResult.validArchives.length}个`,
                    cancelButtonText: '取消',
                    type: 'warning',
                    dangerouslyUseHTMLString: true
                }
            ).then(() => {
                destroyDialog.selectedArchives = batchCheckResult.validArchives;
                openDestroyDialog();
            }).catch(() => {});
        } else {
            ElMessageBox.alert(errorMsg, '无法提交销毁申请', {
                confirmButtonText: '确定',
                type: 'error',
                dangerouslyUseHTMLString: true
            });
        }
        return;
    }
    openDestroyDialog();
}

/** 打开销毁申请弹窗 */
function openDestroyDialog() {
    resetDestroyForm();
    destroyDialog.visible = true;
}

/** 重置销毁表单 */
function resetDestroyForm() {
    destroyForm.applicant = '';
    destroyForm.reason = '';
    destroyForm.description = '';
    destroyForm.scheduledDate = '';
    if (destroyFormRef.value) {
        destroyFormRef.value.resetFields();
    }
}

/** 提交销毁申请 */
async function handleSubmitDestroyApplication() {
    try {
        await destroyFormRef.value.validate();
        const confirmMessage = destroyDialog.selectedArchives.length === 1
            ? `确定要提交档案【${destroyDialog.selectedArchives[0].name}】的销毁申请吗？`
            : `确定要提交 ${destroyDialog.selectedArchives.length} 个档案的销毁申请吗？`;

        await ElMessageBox.confirm(confirmMessage, '确认提交', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
        });
        destroyDialog.loading = true;

        if (destroyDialog.selectedArchives.length > 1) {
            const batchData = {
                archiveIds: destroyDialog.selectedArchives.map(archive => archive.id),
                purpose: `${destroyForm.reason}：${destroyForm.description}`,
                description: destroyForm.description,
                applicant: destroyForm.applicant
            };
            await batchSubmitDestroyApplication(batchData);
        } else {
            const archive = destroyDialog.selectedArchives[0];
            const purposeText = `销毁原因：${destroyForm.reason}\n详细说明：${destroyForm.description}\n申请人：${destroyForm.applicant}`;
            const submitData = {
                archiveId: Number(archive.id),
                purpose: purposeText,
                reason: destroyForm.reason,
                description: destroyForm.description,
                applicant: destroyForm.applicant,
                scheduledDate: destroyForm.scheduledDate ?
                    new Date(destroyForm.scheduledDate).toISOString().replace('T', ' ').substring(0, 19) : null
            };
            await submitDestroyApplication(submitData);
        }
        ElMessage.success('销毁申请提交成功');
        destroyDialog.visible = false;
        getList();
    } catch (error) {
        if (error !== 'cancel') {
            let errorMsg = '提交失败';
            if (error.response && error.response.data && error.response.data.msg) {
                errorMsg = error.response.data.msg;
            } else if (error.message) {
                errorMsg = error.message;
            }
            ElMessage.error('提交销毁申请失败：' + errorMsg);
        }
    } finally {
        destroyDialog.loading = false;
    }
}

/** 检查档案是否可以销毁 */
function checkArchiveCanDestroy(archive) {
    if (!archive) return { canDestroy: false, message: '档案信息异常' };
    if (archive.status !== 'Archived') {
        return { canDestroy: false, message: '只能销毁已归档的档案' };
    }
    if (archive.availability !== 0) {
        return { canDestroy: false, message: '只能销毁在架状态的档案' };
    }
    return { canDestroy: true, message: '档案在架，可以销毁' };
}

/** 批量检查档案状态 */
function batchCheckArchivesCanDestroy(archives) {
    const validArchives = [];
    const invalidArchives = [];
    archives.forEach(archive => {
        const checkResult = checkArchiveCanDestroy(archive);
        if (checkResult.canDestroy) {
            validArchives.push(archive);
        } else {
            invalidArchives.push({ archive: archive, reason: checkResult.message });
        }
    });
    return { validArchives, invalidArchives, hasInvalid: invalidArchives.length > 0 };
}

/** 确认完成销毁 */
async function handleCompleteDestroy(row) {
    try {
        const confirmMessage = `确定要确认销毁档案【${row.name}】吗？\n\n此操作不可撤销！`;
        await ElMessageBox.confirm(confirmMessage, '确认销毁档案', {
            confirmButtonText: '确定销毁',
            cancelButtonText: '取消',
            type: 'warning',
            dangerouslyUseHTMLString: true
        });
        await completeDestroy(row.id);
        ElMessage.success('档案销毁确认成功');
        getList();
        if (historyDialog.visible) getDestroyHistory();
    } catch (error) {
        if (error !== 'cancel') {
            const errorMsg = error.response?.data?.msg || error.message || '确认销毁失败';
            ElMessage.error('确认销毁失败：' + errorMsg);
        }
    }
}

/** 撤销销毁申请 */
async function handleCancelDestroy(row) {
    try {
        if (row.availability !== 1) {
            ElMessage.warning('只能撤销待下架状态的销毁申请');
            return;
        }
        const confirmMessage = `确定要撤销档案【${row.name}】的销毁申请吗？`;
        await ElMessageBox.confirm(confirmMessage, '确认撤销销毁申请', {
            confirmButtonText: '确定撤销',
            cancelButtonText: '取消',
            type: 'warning',
            dangerouslyUseHTMLString: true
        });
        await cancelDestroyByArchiveId(row.id);
        ElMessage.success('销毁申请已撤销');
        getList();
        if (historyDialog.visible) getDestroyHistory();
    } catch (error) {
        if (error !== 'cancel') {
            const errorMsg = error.response?.data?.msg || error.message || '撤销失败';
            ElMessage.error('撤销销毁申请失败：' + errorMsg);
        }
    }
}

/** 查看销毁历史 */
function handleViewDestroyHistory() {
    historyDialog.visible = true;
    getDestroyHistory();
}

/** 获取销毁历史 */
function getDestroyHistory() {
    historyLoading.value = true;
    listDestroyRecords(historyQueryParams).then(response => {
        historyList.value = response.rows;
        historyTotal.value = response.total;
        historyLoading.value = false;
    }).catch(() => {
        historyLoading.value = false;
        ElMessage.error('获取销毁历史失败');
    });
}

/** 重置历史查询 */
function resetHistoryQuery() {
    historyQueryParams.applicant = '';
    historyQueryParams.status = '';
    getDestroyHistory();
}

/** 获取状态标签类型 */
function getStatusTagType(status) {
    const typeMap = { '待下架': 'warning', '已销毁': 'success', '销毁失败': 'danger' };
    return typeMap[status] || '';
}

/** 导出按钮操作 */
function handleExport() {
    proxy.download('manage/archive/export', { ...queryParams }, `archived_archives_${new Date().getTime()}.xlsx`);
}
</script>

<style scoped>
.destroy-content {
    padding: 10px 0;
}
.archive-info-section {
    border: 1px solid #ebeef5;
}
.single-archive p,
.multiple-archives p {
    margin: 8px 0;
    font-size: 14px;
}
.dialog-footer {
    display: flex;
    justify-content: flex-end;
}
.mb-4 {
    margin-bottom: 16px;
}
</style>