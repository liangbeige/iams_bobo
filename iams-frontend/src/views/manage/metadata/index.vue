<template>
    <div class="app-container">
        <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
            <el-form-item label="元数据名称" prop="name">
                <el-input v-model="queryParams.name" placeholder="请输入元数据名称" clearable style="width: 200px"
                    @keyup.enter="handleQuery" />
            </el-form-item>
            <el-form-item label="元数据编码" prop="code">
                <el-input v-model="queryParams.code" placeholder="请输入元数据编码" clearable style="width: 200px"
                    @keyup.enter="handleQuery" />
            </el-form-item>
            <el-form-item label="关键字" prop="keywords">
                <el-input v-model="queryParams.keywords" placeholder="请输入关键字" clearable style="width: 200px"
                    @keyup.enter="handleQuery" />
            </el-form-item>
            <el-form-item>
                <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
                <el-button icon="Refresh" @click="resetQuery">重置</el-button>
            </el-form-item>
        </el-form>

        <el-row :gutter="10" class="mb8">
            <el-col :span="1.5">
                <el-button type="primary" plain icon="Plus" @click="handleAdd"
                    v-hasPermi="['system:metadata:add']">新增</el-button>
            </el-col>
            <el-col :span="1.5">
                <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate"
                    v-hasPermi="['system:metadata:edit']">修改</el-button>
            </el-col>
            <el-col :span="1.5">
                <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete"
                    v-hasPermi="['system:metadata:remove']">删除</el-button>
            </el-col>
            <el-col :span="1.5">
                <el-button type="info" plain icon="DocumentCopy" :disabled="single" @click="handleCopy"
                    v-hasPermi="['system:metadata:copy']">复制</el-button>
            </el-col>
            <el-col :span="1.5">
                <el-button type="warning" plain icon="Position" :disabled="multiple" @click="handleApplyMultiple"
                    v-hasPermi="['system:metadata:apply']">多选应用</el-button>
            </el-col>
            <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
        </el-row>

        <el-table v-loading="loading" :data="metadataList" @selection-change="handleSelectionChange">
            <el-table-column type="selection" width="55" align="center" />
            <el-table-column label="序号" align="center" prop="id" />
            <el-table-column label="元数据名称" align="center" prop="name" />
            <el-table-column label="元数据编码" align="center" prop="code" />
            <el-table-column label="关键字" align="center" prop="keywords" show-overflow-tooltip width="500"/>
            <el-table-column label="操作" width="280" align="center" class-name="small-padding fixed-width">
                <template #default="scope">
                    <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)"
                        v-hasPermi="['system:metadata:edit']">修改</el-button>
                    <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)"
                        v-hasPermi="['system:metadata:remove']">删除</el-button>
                    <el-button link type="primary" icon="DocumentCopy" @click="handleCopy(scope.row)"
                        v-hasPermi="['system:metadata:copy']">复制</el-button>
                    <el-button link type="primary" icon="Position" @click="handleApply(scope.row)"
                        v-hasPermi="['system:metadata:apply']">应用</el-button>
                </template>
            </el-table-column>
        </el-table>

        <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum"
            v-model:limit="queryParams.pageSize" @pagination="getList" />

        <!-- 添加或修改元数据对话框 -->
        <el-dialog :title="title" v-model="open" width="600px" append-to-body>
            <el-form ref="metadataRef" :model="form" :rules="rules" label-width="100px">
                <el-form-item label="元数据名称" prop="name">
                    <el-select v-model="form.name" placeholder="请选择元数据名称" @change="handleNameChange">
                        <el-option label="公文标题" value="公文标题" />
                        <el-option label="发文字号" value="发文字号" />
                        <el-option label="收发文单位" value="收发文单位" />
                    </el-select>
                </el-form-item>
                <el-form-item label="元数据编码" prop="code">
                    <el-input v-model="form.code" placeholder="编码会自动生成" readonly />
                </el-form-item>
                <el-form-item label="关键字" prop="keywords">
                    <div v-for="(keyword, index) in keywordList" :key="index" class="keyword-item">
                        <el-input v-model="keywordList[index]" placeholder="请输入关键字"
                            style="width: 300px; margin-bottom: 10px;" />
                        <el-button type="primary" icon="Plus" circle size="small" style="margin-left: 10px;"
                            @click="addKeyword"></el-button>
                        <el-button type="danger" icon="Minus" circle size="small" style="margin-left: 5px;"
                            @click="removeKeyword(index)" :disabled="keywordList.length <= 1"></el-button>
                    </div>
                </el-form-item>
            </el-form>
            <template #footer>
                <div class="dialog-footer">
                    <el-button type="primary" @click="submitForm">确 定</el-button>
                    <el-button @click="cancel">取 消</el-button>
                </div>
            </template>
        </el-dialog>

        <!-- 应用元数据对话框 -->
        <el-dialog title="应用元数据" v-model="applyOpen" width="800px" append-to-body>
            <el-form :model="applyForm" label-width="100px">
                <el-form-item label="选择项目">
                    <el-select v-model="applyForm.projectId" placeholder="请选择项目" @change="handleProjectChange">
                        <el-option v-for="project in projectList" :key="project.id" :label="project.name"
                            :value="project.id" />
                    </el-select>
                </el-form-item>
                <el-form-item label="选择档案" v-if="applyForm.projectId">
                    <el-select v-model="applyForm.archiveId" placeholder="请选择档案" @change="handleArchiveChange">
                        <el-option v-for="archive in archiveList" :key="archive.id" :label="archive.name"
                            :value="archive.id" />
                    </el-select>
                </el-form-item>
                <el-form-item label="选择文档" v-if="applyForm.archiveId">
                    <el-table :data="documentList" @selection-change="handleDocumentSelectionChange" max-height="300">
                        <el-table-column type="selection" width="55" align="center" />
                        <el-table-column label="文档名称" prop="name" />
                        <!-- <el-table-column label="文档类型" prop="type" />
                        <el-table-column label="创建时间" prop="createTime" /> -->
                    </el-table>
                </el-form-item>
            </el-form>
            <template #footer>
                <div class="dialog-footer">
                    <el-button type="primary" @click="submitApply"
                        :disabled="selectedDocuments.length === 0">应用</el-button>
                    <el-button @click="cancelApply">取 消</el-button>
                </div>
            </template>
        </el-dialog>
    </div>
</template>

<script setup name="Metadata">
import { listMetadata, addMetadata, delMetadata, getMetadata, updateMetadata, copyMetadata, applyMetadata } from "@/api/manage/metadata";
import { listDocument } from "@/api/manage/document";
import { listArchive } from "@/api/manage/archive";
import { listProject } from "@/api/manage/project";

const { proxy } = getCurrentInstance();

const metadataList = ref([]);
const open = ref(false);
const applyOpen = ref(false);
const loading = ref(true);
const showSearch = ref(true);
const ids = ref([]);
const single = ref(true);
const multiple = ref(true);
const total = ref(0);
const title = ref("");
const keywordList = ref(['']);

// 应用相关数据
const projectList = ref([]);
const archiveList = ref([]);
const documentList = ref([]);
const selectedDocuments = ref([]);
const currentMetadataId = ref(null);

const data = reactive({
    form: {},
    queryParams: {
        pageNum: 1,
        pageSize: 10,
        name: undefined,
        code: undefined,
        keywords: undefined
    },
    applyForm: {
        projectId: undefined,
        archiveId: undefined
    },
    rules: {
        name: [{ required: true, message: "元数据名称不能为空", trigger: "change" }],
        keywords: [
            {
                required: true,
                validator: (rule, value, callback) => {
                    // 检查 keywordList 是否有有效的关键字
                    const validKeywords = keywordList.value.filter(k => k && k.trim());
                    if (validKeywords.length === 0) {
                        callback(new Error('关键字不能为空'));
                    } else {
                        callback();
                    }
                },
                trigger: "blur"
            }
        ]
    }
});

const { queryParams, form, applyForm, rules } = toRefs(data);

// 元数据名称映射
const nameCodeMap = {
    '公文标题': 'title',
    '发文字号': 'number',
    '收发文单位': 'unit'
};

/** 查询元数据列表 */
function getList() {
    loading.value = true;
    listMetadata(queryParams.value).then(response => {
        metadataList.value = response.rows;
        total.value = response.total;
        loading.value = false;
    });
}

/** 取消按钮 */
function cancel() {
    open.value = false;
    reset();
}

/** 表单重置 */
function reset() {
    form.value = {
        id: undefined,
        name: undefined,
        code: undefined,
        keywords: undefined
    };
    keywordList.value = [''];
    proxy.resetForm("metadataRef");
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

/** 多选框选中数据 */
function handleSelectionChange(selection) {
    ids.value = selection.map(item => item.id);
    single.value = selection.length != 1;
    multiple.value = !selection.length;
}

/** 元数据名称变更 */
function handleNameChange(value) {
    form.value.code = nameCodeMap[value];
}

/** 验证关键字 */
function validateKeywords() {
    // 手动触发表单项验证
    nextTick(() => {
        if (proxy.$refs["metadataRef"]) {
            proxy.$refs["metadataRef"].validateField('keywords');
        }
    });
}

/** 添加关键字 */
function addKeyword() {
    keywordList.value.push('');
    validateKeywords();
}

/** 删除关键字 */
function removeKeyword(index) {
    if (keywordList.value.length > 1) {
        keywordList.value.splice(index, 1);
        validateKeywords();
    }
}

/** 新增按钮操作 */
function handleAdd() {
    reset();
    open.value = true;
    title.value = "添加元数据";
}

/** 修改按钮操作 */
function handleUpdate(row) {
    reset();
    const metadataId = row.id || ids.value[0];
    getMetadata(metadataId).then(response => {
        form.value = response.data;
        // 解析关键字
        if (form.value.keywords) {
            keywordList.value = form.value.keywords.split(',').filter(k => k.trim());
        }
        open.value = true;
        title.value = "修改元数据";
    });
}

/** 提交按钮 */
function submitForm() {
    proxy.$refs["metadataRef"].validate(valid => {
        if (valid) {
            // 将关键字数组拼接成字符串
            form.value.keywords = keywordList.value.filter(k => k.trim()).join(',');

            if (form.value.id != undefined) {
                updateMetadata(form.value).then(response => {
                    proxy.$modal.msgSuccess("修改成功");
                    open.value = false;
                    getList();
                });
            } else {
                addMetadata(form.value).then(response => {
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
    const metadataIds = row.id || ids.value;
    proxy.$modal.confirm('是否确认删除元数据ID为"' + metadataIds + '"的数据项？').then(function () {
        return delMetadata(metadataIds);
    }).then(() => {
        getList();
        proxy.$modal.msgSuccess("删除成功");
    }).catch(() => { });
}

/** 复制按钮操作 */
function handleCopy(row) {
    const metadataId = row.id || ids.value[0];
    proxy.$modal.confirm('是否确认复制该元数据？').then(function () {
        return copyMetadata(metadataId);
    }).then(() => {
        getList();
        proxy.$modal.msgSuccess("复制成功");
    }).catch(() => { });
}

/** 应用按钮操作（单个） */
function handleApply(row) {
    currentMetadataId.value = row.id;
    loadProjectList();
    applyOpen.value = true;
}

/** 应用按钮操作（多个） */
function handleApplyMultiple() {
    if (ids.value.length === 0) {
        proxy.$modal.msgError("请选择要应用的元数据");
        return;
    }
    currentMetadataId.value = ids.value.join(',');
    loadProjectList();
    applyOpen.value = true;
}

/** 加载项目列表 */
function loadProjectList() {
    console.log('开始加载项目列表...'); // 调试日志
    listProject().then(response => {
        console.log('项目列表加载成功:', response.data); // 调试日志
        projectList.value = response.rows;
    }).catch(error => {
        console.error('加载项目列表失败:', error); // 错误日志
    });
}

/** 项目变更 */
function handleProjectChange(projectId) {
    applyForm.value.archiveId = undefined;
    documentList.value = [];
    selectedDocuments.value = [];

    if (projectId) {
        listArchive(projectId).then(response => {
            archiveList.value = response.rows;
        });
    }
}

/** 档案变更 */
function handleArchiveChange(archiveId) {
    documentList.value = [];
    selectedDocuments.value = [];

    if (archiveId) {
        listDocument(archiveId).then(response => {
            documentList.value = response.rows;
        });
    }
}

/** 文档选择变更 */
function handleDocumentSelectionChange(selection) {
    selectedDocuments.value = selection.map(item => item.id);
}

/** 提交应用 */
function submitApply() {
    if (selectedDocuments.value.length === 0) {
        proxy.$modal.msgError("请选择要应用的文档");
        return;
    }

    const params = {
        metadataIds: currentMetadataId.value,
        documentIds: selectedDocuments.value.join(',')
    };

    applyMetadata(params).then(response => {
        proxy.$modal.msgSuccess("应用成功");
        cancelApply();
    });
}

/** 取消应用 */
function cancelApply() {
    applyOpen.value = false;
    applyForm.value = {
        projectId: undefined,
        archiveId: undefined
    };
    projectList.value = [];
    archiveList.value = [];
    documentList.value = [];
    selectedDocuments.value = [];
    currentMetadataId.value = null;
}

getList();
</script>

<style scoped>
.keyword-item {
    display: flex;
    align-items: center;
    margin-bottom: 10px;
}
</style>