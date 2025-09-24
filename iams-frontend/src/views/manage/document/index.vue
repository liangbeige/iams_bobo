<template>
  <div class="app-container">
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Upload" @click="handleImport">导入</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="edit" @click="batchOcr">批量OCR</el-button>
      </el-col>
    </el-row>

    <!-- 导入对话框 -->
    <el-dialog title="导入文档" v-model="importDialog" width="300px" append-to-body>
      <el-select v-model="importArchiveId" placeholder="请选择档案" style="width: 100%;" @change="handleArchiveChange">
        <el-option v-for="archive in archiveList" :key="archive.id" :label="archive.name" :value="archive.id" />
      </el-select>

      <!-- 门类树选择器 -->
      <div v-loading="categoryTreeLoading" style="margin-top: 15px;">
        <el-tree-select v-model="selectedCategoryCode" v-if="importArchiveId" :data="categoryTreeData"
          :props="categoryTreeProps" placeholder="请选择目标门类" style="width: 100%" :render-after-expand="false"
          show-checkbox-indeterminate check-strictly>
          <template #default="{ node, data }">
            <span class="tree-select-node">
              <span class="node-code">{{ data.code }}</span>
              <span class="node-name">{{ data.name }}</span>
            </span>
          </template>
        </el-tree-select>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="handleCancel">取 消</el-button>
          <el-button type="primary" @click="confirmImport">确 定</el-button>
        </div>
      </template>
    </el-dialog>
    <el-row :gutter="20" style="margin-top: 20px;">
      <!-- 已OCR文档 -->
      <el-col :span="12">
        <div style="font-size: 16px; font-weight: bold; margin-bottom: 10px;">已OCR文档</div>
        <el-table v-loading="loading" :data="ocredDocumentList" style="max-height: 500px; overflow-y: auto;">
          <el-table-column label="所属档案" align="center" prop="archiveId">
            <template #default="scope">
              <el-link :underline="false" type="primary" @click="handleArchiveDetail(scope.row)"
                :disabled="!scope.row.archiveId">
                {{ getArchiveNameById(scope.row.archiveId) }}
              </el-link>
            </template>
          </el-table-column>
          <el-table-column label="文件名称" align="center" prop="name" show-overflow-tooltip />
          <el-table-column label="文件类型" align="center" prop="fileType">
            <template #default="scope">
              <dict-tag :options="iams_media_type" :value="scope.row.fileType" />
            </template>
          </el-table-column>
          <el-table-column label="文件大小(Kb)" align="center" prop="fileSize" />
          <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
            <template #default="scope">
              <el-button link type="primary" @click="handleDetail(scope.row)"
                v-hasPermi="['manage:document:query']">查看</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-col>

      <!-- 未OCR文档 -->
      <el-col :span="12">
        <div style="font-size: 16px; font-weight: bold; margin-bottom: 10px;">未OCR文档</div>
        <el-table v-loading="loading" :data="noOcrDocumentList" style="max-height: 500px; overflow-y: auto;">
          <el-table-column label="所属档案" align="center" prop="archiveId">
            <template #default="scope">
              <el-link :underline="false" type="primary" @click="handleArchiveDetail(scope.row)"
                :disabled="!scope.row.archiveId">
                {{ getArchiveNameById(scope.row.archiveId) }}
              </el-link>
            </template>
          </el-table-column>
          <el-table-column label="文件名称" align="center" prop="name" show-overflow-tooltip />
          <el-table-column label="文件类型" align="center" prop="fileType">
            <template #default="scope">
              <dict-tag :options="iams_media_type" :value="scope.row.fileType" />
            </template>
          </el-table-column>
          <el-table-column label="文件大小(Kb)" align="center" prop="fileSize" />
          <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
            <template #default="scope">
              <el-button link type="primary" @click="handleDetail(scope.row)"
                v-hasPermi="['manage:document:query']">查看</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-col>
    </el-row>

  </div>
</template>

<script setup name="Document">
import { listDocument, getDocument, delDocument, addDocument, updateDocument, getNoOcrDocumentList, getOcredDocumentList, doBatchOcr } from "@/api/manage/document";
import { deleteDirectoryFiles, getArchive, listArchive } from "@/api/manage/archive";
import { ElSelect, ElOption } from 'element-plus';
import { getCategoryTreeByCode } from "@/api/manage/treeCategory.js";
import { ElMessage } from 'element-plus';
import { getUserProfile } from "@/api/system/user.js";
import { onMounted } from "vue";

const { proxy } = getCurrentInstance();
const { iams_media_type } = proxy.useDict('iams_media_type');

const noOcrDocumentList = ref([]);
const ocredDocumentList = ref([]);
const open = ref(false);
const loading = ref(true);
const ocredTotal = ref(0);
const noOcrtotal = ref(0);
const user = ref(null);


// 分类树相关状态
const categoryTreeData = ref([])
const categoryTreeLoading = ref(false)
const categoryTreeProps = {
  id: 'id',
  label: 'name',
  children: 'children',
  value: 'code' // 明确指定 value 字段
}

const importDialog = ref(false);
const archiveList = ref([]);
const importArchiveId = ref(null);
const selectedCategoryCode = ref(''); // 初始化为空字符串而不是 null

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 9999,
    archiveId: null,
    name: null,
    fileType: null,
    content: null,
    createBy: null,
  },
  rules: {
    name: [
      { required: true, message: "文件名称不能为空", trigger: "blur" }
    ],
  }
});

const { queryParams, form, rules } = toRefs(data);

function getUser() {
  getUserProfile().then(response => {
    user.value = response.data;
    data.queryParams.createBy = user.value.userName; // 获取成功后再赋值
    console.log("Current user:", user.value);
    getList();
  });
};

// 获取档案列表
function getArchives() {
  listArchive({ pageNum: 1, pageSize: 9999 }).then(response => {
    archiveList.value = response.rows;
  });
}

// 添加一个响应式数据来存储档案信息缓存
const archiveCache = ref(new Map());

/** 查询文档信息列表 */
async function getList() {
  loading.value = true;
  try {
    let ocredRes, noOcrRes;

    // 获取已OCR文档
    ocredRes = await getOcredDocumentList(queryParams.value);
    ocredDocumentList.value = ocredRes.rows;
    ocredTotal.value = ocredRes.total;

    // 获取未OCR文档
    noOcrRes = await getNoOcrDocumentList(queryParams.value);
    noOcrDocumentList.value = noOcrRes.rows;
    noOcrtotal.value = noOcrRes.total;

    // 合并两个列表的所有档案ID
    const allDocs = [...ocredDocumentList.value, ...noOcrDocumentList.value];
    const archiveIds = [...new Set(allDocs.map(doc => doc.archiveId).filter(id => id))];

    // 批量加载档案名称
    await loadArchiveNames(archiveIds);

  } catch (error) {
    console.error('获取文档列表失败:', error);
    // proxy.$message.error('获取文档列表失败');
  } finally {
    loading.value = false;
  }
}

/** 批量加载档案名称 */
async function loadArchiveNames(archiveIds) {
  const needLoadIds = archiveIds.filter(id => !archiveCache.value.has(id));

  if (needLoadIds.length === 0) return;

  // 并发请求所有需要的档案信息
  const promises = needLoadIds.map(async (id) => {
    try {
      const archiveResponse = await getArchive(id);
      // 修复：存储完整的档案对象，而不只是名称
      archiveCache.value.set(id, archiveResponse.data || archiveResponse);
    } catch (error) {
      console.error(`获取档案 ${id} 信息失败:`, error);
      // 修复：保持一致的对象结构
      archiveCache.value.set(id, { name: '获取失败' });
    }
  });

  await Promise.all(promises);
}

/** 根据档案ID获取档案名称的辅助方法 */
function getArchiveNameById(archiveId) {
  if (!archiveId) return '无档案';
  const archive = archiveCache.value.get(archiveId);
  return archive ? archive.name : '未知档案';
}

// 使用 computed 属性优化文档列表显示
const enhancedDocumentList = computed(() => {
  return documentList.value.map(doc => ({
    ...doc,
    archiveName: getArchiveNameById(doc.archiveId)
  }));
});

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

/** 查看文档详情操作 */
function handleDetail(row) {
  proxy.$router.push({ path: '/manage/document/doc-detail/', query: { id: row.id } });
}

/** 查看文档所属档案详情操作 */
function handleArchiveDetail(row) {
  alert(row.archiveId);
  proxy.$router.push({ path: '/manage/archive/arc-detail/', query: { id: row.archiveId } });
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["documentRef"].validate(valid => {
    if (valid) {
      if (form.value.id != null) {
        updateDocument(form.value).then(response => {
          proxy.$modal.msgSuccess("修改成功");
          open.value = false;
          getList();
        });
      } else {
        addDocument(form.value).then(response => {
          proxy.$modal.msgSuccess("新增成功");
          open.value = false;
          getList();
        });
      }
    }
  });
}

/** 导出按钮操作 */
function handleExport() {
  proxy.download('manage/document/export', {
    ...queryParams.value
  }, `document_${new Date().getTime()}.xlsx`)
}

// 打开导入对话框
function handleImport() {
  importDialog.value = true;
  getArchives(); // 获取档案列表
  // 重置状态
  importArchiveId.value = null;
  selectedCategoryCode.value = null;
  categoryTreeData.value = [];
}

function handleCancel() {
  importDialog.value = false;
  importArchiveId.value = null;
  selectedCategoryCode.value = null;
  categoryTreeData.value = [];
}

// 档案选择变化时加载门类树
function handleArchiveChange(archiveId) {
  categoryTreeData.value = [];
  selectedCategoryCode.value = ''; // 重置选择

  const archive = archiveList.value.find(archive => { return archive.id === archiveId; });

  // 加载门类树
  loadCategoryTree(archive.categoryId);
}

// 加载门类树数据
async function loadCategoryTree(categoryCode) {
  if (!categoryCode) {
    console.warn('档案没有设置category_code')
    categoryTreeData.value = []
    return
  }

  categoryTreeLoading.value = true
  try {
    const response = await getCategoryTreeByCode(categoryCode)
    if (response.data) {
      categoryTreeData.value = Array.isArray(response.data) ? response.data : [response.data]
    } else {
      categoryTreeData.value = []
    }
  } catch (error) {
    console.error('加载分类树失败:', error)
    ElMessage.error('加载分类树失败')
    categoryTreeData.value = []
  } finally {
    categoryTreeLoading.value = false
  }
}

// 确认导入
function confirmImport() {
  if (!importArchiveId.value) {
    proxy.$message.error("请选择档案");
    return;
  }

  proxy.$router.push({
    path: '/manage/document/import',
    query: {
      archiveId: importArchiveId.value,
      categoryCode: selectedCategoryCode.value
    }
  });

  importDialog.value = false;
}

function batchOcr() {
  ElMessage.info({
    message: `
    <div style="display: flex; align-items: center; padding: 10px; background-color: #f9fafb; border-left: 4px solid #409EFF;">
      <i class="el-icon-loading" style="margin-right: 10px; font-size: 16px;"></i>
      <span style="font-size: 16px; color: #333;">已提交批量OCR任务，请耐心等待</span>
    </div>
  `,
    duration: 5000,
    type: 'info',
    dangerouslyUseHTMLString: true,
    center: true,
  });
  doBatchOcr();
}
onMounted(async() => {
  // 初始化时获取文档列表
  getUser();
});

</script>

<style scoped>
.tree-select-node {
  display: flex;
  align-items: center;
}

.node-code {
  font-weight: bold;
  margin-right: 8px;
  color: #409EFF;
}

.node-name {
  flex: 1;
}
</style>