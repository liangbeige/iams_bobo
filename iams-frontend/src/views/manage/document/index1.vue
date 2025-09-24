<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="所属档号" prop="archivedanghao">
        <el-input v-model="queryParams.archivedanghao" placeholder="请输入所属档号" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="文件名称" prop="name">
        <el-input v-model="queryParams.name" placeholder="请输入文件名称" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="文件类型" prop="fileType">
        <el-select v-model="queryParams.fileType" placeholder="请选择文件类型" clearable>
          <el-option v-for="dict in iams_media_type" :key="dict.value" :label="dict.label" :value="dict.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="创建人" prop="createBy">
        <el-input v-model="queryParams.createBy" placeholder="请输入创建人" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd"
          v-hasPermi="['manage:document:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate"
          v-hasPermi="['manage:document:edit']">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete"
          v-hasPermi="['manage:document:remove']">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Download" @click="handleExport"
          v-hasPermi="['manage:document:export']">导出</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Upload" @click="handleImport">批量上传</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="Edit" @click="categoryConnect" v-hasPermi="['manage:document:edit']"
          :disabled="multiple">门类挂接</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="edit" @click="batchOcr">批量OCR</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 导入对话框 -->
    <el-dialog title="导入文档" v-model="importDialog" width="300px" append-to-body>
      <el-form>
        <el-form-item label="选择档案">
          <el-select v-model="importArchiveId" placeholder="请选择档案" style="width: 100%;" @change="handleArchiveChange">
            <el-option v-for="archive in archiveList" :key="archive.id" :label="archive.danghao" :value="archive.id" />
          </el-select>
        </el-form-item>

        <el-form-item label="选择门类">
          <el-select v-model="selectedCategoryCodes" multiple placeholder="请选择门类" style="width: 100%">
            <el-option v-for="category in archiveCategoryList" :key="category" :label="category" :value="category">
            </el-option>
          </el-select>
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="handleCancel">取 消</el-button>
          <el-button type="primary" @click="confirmUpload">确 定</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog title="门类挂接" v-model="categoryDialog" width="300px" append-to-body>
      <el-form>
        <!-- <el-form-item label="主门类" prop="categoryId">
          <el-select v-model="categoryId" placeholder="请选择档案主门类" style="width: 100%"
            @change="loadCategoryTree(categoryId)">
            <el-option v-for="category in categoryRoots" :key="category.id" :label="category.name"
              :value="category.code" />
          </el-select>
        </el-form-item>

        <el-form-item label="子门类">
          <el-tree-select v-model="selectedCategoryId" :data="categoryTreeData" :props="{
            label: 'name',
            value: 'code',
            children: 'children'
          }" value-key="code" placeholder="请选择子门类" style="width: 100%" :render-after-expand="false" multiple
            :check-strictly="false" show-checkbox>
            <template #default="{ node, data }">
              <span class="tree-select-node">
                <span class="node-code">{{ data.code }}</span>
                <span class="node-name">{{ data.name }}</span>
              </span>
            </template>
          </el-tree-select>
        </el-form-item> -->
        <el-form-item label="选择档案">
          <el-select v-model="importArchiveId" placeholder="请选择档案" style="width: 100%;" @change="handleArchiveChange">
            <el-option v-for="archive in archiveList" :key="archive.id" :label="archive.danghao" :value="archive.id" />
          </el-select>
        </el-form-item>

        <el-form-item label="选择门类">
          <el-select v-model="selectedCategoryCodes" multiple placeholder="请选择门类" style="width: 100%">
            <el-option v-for="category in archiveCategoryList" :key="category" :label="category" :value="category">
            </el-option>
          </el-select>
        </el-form-item>
      </el-form>

      <!-- 底部按钮插槽 -->
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="cancelCategory">取 消</el-button>
          <el-button type="primary" @click="confirmCategory">确 定</el-button>
        </div>
      </template>
    </el-dialog>

    <el-table v-loading="loading" :data="documentList" @selection-change="handleSelectionChange"
      @sort-change="handleSortChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="所属档案" align="center" prop="archiveId">
        <template #default="scope">
          <el-link :underline="false" type="primary" @click="handleArchiveDetail(scope.row)"
            :disabled="!scope.row.archiveId">
            <!-- 直接从 archiveCache 中获取档案名称 -->
            {{ getArchiveNameById(scope.row.archiveId) }}
          </el-link>
        </template>
      </el-table-column>
      <el-table-column label="创建人" align="center" prop="createBy" show-overflow-tooltip />
      <el-table-column label="文件名称" align="center" prop="name" show-overflow-tooltip />
      <el-table-column label="文件类型" align="center" prop="fileType">
        <template #default="scope">
          <dict-tag :options="iams_media_type" :value="scope.row.fileType" />
        </template>
      </el-table-column>
      <el-table-column label="文件大小(Kb)" align="center" prop="fileSize" />
      <!-- <el-table-column label="文件路径" align="center" prop="filePath" /> -->
      <!-- <el-table-column label="存放位置" align="center" prop="fileLocation" /> -->
      <!-- 是否已做OCR -->
      <el-table-column label="是否已做OCR" align="center">
        <template #default="scope">
          <el-tag :type="scope.row.content ? 'success' : 'danger'">
            {{ scope.row.content ? '是' : '否' }}
          </el-tag>
        </template>
      </el-table-column>

      <!-- 是否已挂接门类 -->
      <el-table-column label="门类挂接状态" align="center" sortable="custom" prop="categoryCode">
        <template #default="scope">
          <el-tag :type="scope.row.categoryCode ? 'success' : 'danger'">
            {{ scope.row.categoryCode ? scope.row.categoryCode : '未挂接' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" @click="handleDetail(scope.row)"
            v-hasPermi="['manage:document:query']">查看</el-button>
          <el-button link type="primary" @click="handleUpdate(scope.row)"
            v-hasPermi="['manage:document:edit']">修改</el-button>
          <el-button link type="primary" @click="handleDelete(scope.row)"
            v-hasPermi="['manage:document:remove']">删除</el-button>
          <el-button link type="warning" @click="handleAttachCategory(scope.row)"
            v-hasPermi="['manage:document:edit']">门类挂接</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize" @pagination="getList" />
  </div>
</template>
<script>
// 显示的定义组件名称，和路由地址保持一致，
// 在系统管理->菜单管理 中可以配置是否开启页面缓存
export default {
  name: 'Doclist'
}
</script>
<script setup name="Document">
import { listDocument, getDocument, delDocument, addDocument, updateDocument, listDocumentByProjectId, doBatchOcr } from "@/api/manage/document";
import { deleteDirectoryFiles, getArchive, listArchive } from "@/api/manage/archive";
import { ElSelect, ElOption, ElMessage } from 'element-plus';
import { listCategory } from "@/api/manage/category";
import { onMounted } from "vue";
import { useRoute, useRouter } from 'vue-router'
import { getCategoryList, getCategoryRoots, getCategoryTreeByCode } from "@/api/manage/treeCategory.js";

const { proxy } = getCurrentInstance();
const { iams_media_type } = proxy.useDict('iams_media_type');

const documentList = ref([]);
const open = ref(false);
const loading = ref(true);
const showSearch = ref(true);
const ids = ref([]);
const single = ref(true);
const multiple = ref(true);
const total = ref(0);
const title = ref("");
const route = useRoute();

const importDialog = ref(false);
const archiveList = ref([]);
const importArchiveId = ref(null);

const categoryList = ref([]);
const categoryDialog = ref(false);
const selectedCategoryId = ref(null);
const selectedCategoryCodes = ref([])
const archiveCategoryList = ref([]) // 档案自带的分类列表

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    archiveId: null,
    archivedanghao:null,
    name: null,
    fileType: null,
    content: null,
    createBy: null,
    projectId: null,
    categoryCode: null,
    // 新增排序参数
    orderByColumn: null,  // 排序字段
    isAsc: null          // 排序方向：asc 或 desc
  },
  rules: {
    name: [
      { required: true, message: "文件名称不能为空", trigger: "blur" }
    ],
  }
});

const { queryParams, form, rules } = toRefs(data);

// 档案门类多选相关===========================================
// 分类树数据
const categoryTreeData = ref([])
const categoryId = ref(null);
const categoryRoots = ref([]);

function getCategoryRootList() {
  getCategoryRoots().then(response => {
    categoryRoots.value = response.data;
    console.log('档案门类列表:', categoryRoots.value);
  }).catch(error => {
    console.error('获取档案门类失败:', error);
  });
}

// 监听值变化（可选）
watch(selectedCategoryId, (newVal) => {
  console.log('当前选中门类:', newVal);
});

// 加载分类树的方法
async function loadCategoryTree(categoryCode) {
  selectedCategoryId.value = [];
  try {
    const response = await getCategoryTreeByCode(categoryCode)
    if (response.data) {
      categoryTreeData.value = Array.isArray(response.data) ? response.data : [response.data]

      console.log('categoryTreeData', categoryTreeData.value)
    } else {
      categoryTreeData.value = []
    }
  } catch (error) {
    console.error('加载分类树失败:', error)
  }
}

// 获取档案列表
function getArchives() {
  let projectId = null;
  if (route.query.projectId) {
    projectId = route.query.projectId
  }
  listArchive({ pageNum: 1, pageSize: 9999, projectId: projectId }).then(response => {
    archiveList.value = response.rows;
  });
}

function getCategory() {
  listCategory().then(response => {
    categoryList.value = response.rows;
  });
}

// 添加一个响应式数据来存储档案信息缓存
const archiveCache = ref(new Map());

/** 查询文档信息列表 */
async function getList() {
    console.log("✅ 1. getList 函数开始执行", new Date().toLocaleTimeString());
    loading.value = true;
    let response;
    try {
        if (route.query.projectId) {
            queryParams.value.projectId = route.query.projectId;
        }

        // 使用深拷贝打印，避免因对象引用导致控制台信息不准确
        console.log("🔍 2. 发起 API 请求前的查询参数 (queryParams):", JSON.parse(JSON.stringify(queryParams.value)));

        response = await listDocument(queryParams.value);

        console.log("📬 3. API 请求成功，收到的原始响应 (response):", response);

        // 增加对 response 和 response.rows 的健壮性检查
        if (response && Array.isArray(response.rows)) {
            documentList.value = response.rows;
            // 这是您原来未打印出来的日志
            console.log('📄 4. 成功赋值文档列表 (documentList):', documentList.value);
            total.value = response.total;

            const archiveIds = [...new Set(response.rows.map(doc => doc.archiveId).filter(id => id))];
            console.log("🗂️ 5. 提取出的档案ID (archiveIds):", archiveIds);

            await loadArchiveNames(archiveIds);
            console.log("✅ 6. 批量获取档案信息完成");

        } else {
            console.error("❌ 警告: API响应格式不正确，缺少 'rows' 数组或格式错误。", response);
            // 设置为空数组，防止页面因数据格式错误而渲染失败
            documentList.value = [];
            total.value = 0;
        }

    } catch (error) {
        console.error('❌❌❌ 获取文档列表失败，错误已被 catch 捕获 ❌❌❌');
        console.error('详细错误对象 (error):', error);

        // 如果是axios等HTTP客户端的错误，通常会包含 response 对象
        if (error.response) {
            console.error('服务器响应的错误数据 (error.response.data):', error.response.data);
            console.error('服务器响应的状态码 (error.response.status):', error.response.status);
        }

    } finally {
        loading.value = false;
        console.log("🏁 7. getList 函数执行完毕 (finally 块)");
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
  return archive ? archive.danghao : '未知档案';
}

// 使用 computed 属性优化文档列表显示
const enhancedDocumentList = computed(() => {
  return documentList.value.map(doc => ({
    ...doc,
    archiveName: getArchiveNameById(doc.archiveId)
  }));
});

// 取消按钮
function cancel() {
  open.value = false;
  reset();
}

// 表单重置
function reset() {
  form.value = {
    id: null,
    archiveId: null,
    xuhao: null,
    name: null,
    fileType: null,
    fileSize: null,
    filePath: null,
    fileLocation: null,
    content: null,
    authenticity: null,
    integrity: null,
    availability: null,
    security: null,
    createTime: null,
    createBy: null,
    updateTime: null,
    updateBy: null,
    remark: null
  };
  proxy.resetForm("documentRef");
}

// 3. 确保搜索和重置时清除排序参数
function handleQuery() {
  queryParams.value.pageNum = 1;
  getList();
}

function resetQuery() {
  proxy.resetForm("queryRef");
  // 重置排序参数
  queryParams.value.orderByColumn = null;
  queryParams.value.isAsc = null;
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
  proxy.$router.push({ path: '/manage/document/doc-add/' });
}

/** 修改按钮操作 */
function handleUpdate(row) {
  const id = row?.id || (ids.value.length > 0 ? ids.value[0] : null);
  proxy.$router.push({ path: '/manage/document/doc-edit/', query: { id } });
}

/** 通过OCR识别修改文章内容操作 */
function handleUpdateContentByOCR(row) {
  if (row.fileType != 'pdf') {
    proxy.$modal.msgError("该文件不是pdf文件，无法识别");
    return;
  }
  proxy.$router.push({ path: '/manage/document/doc-ocr-edit/', query: { id: row.id } });
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

/** 删除按钮操作 */
function handleDelete(row) {
  // 如果 row 存在，则使用 row.id，否则使用 ids.value
  let _ids = row.id ? [row.id] : ids.value;

  proxy.$modal.confirm('是否确认删除文档信息编号为"' + _ids + '"的数据项？').then(async function () {
    for (const id of _ids) {
      try {
        const documentData = await getDocument(id);
        if (!documentData || !documentData.data) {
          throw new Error(`无法获取文档编号为"${id}"的数据`);
        }
        await delDocument(id);
        proxy.$modal.msgSuccess(`文档编号为"${id}"删除成功`);
      } catch (error) {
        proxy.$modal.msgError(`删除文档编号为"${id}"时发生错误: ${error.message}`);
      }
    }
    getList();
  }).catch(() => { })
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
}

function handleCancel() {
  importDialog.value = false;
  importArchiveId.value = null;
  selectedCategoryCode.value = null;
}

function handleArchiveChange() {
  const archive = archiveList.value.find(archive => { return archive.id === importArchiveId.value; });
  archiveCategoryList.value = archive.categoryId.split(",").slice(1);
}

// 确认导入
function confirmUpload() {
  const selectedCategoryCodeStr = selectedCategoryCodes.value.join(",")
  proxy.$router.push({
    path: '/manage/document/import',
    query: {
      archiveId: importArchiveId.value,
      categoryCode: selectedCategoryCodeStr
    }
  })
}

function handleAttachCategory(row) {
  ids.value = [row.id];
  categoryConnect();
}

function categoryConnect() {
  if (ids.value.length === 0) {
    proxy.$modal.msgWarning("请至少选择一条文档");
    return;
  }
  getArchives();
  // categoryId.value = null;
  // selectedCategoryId.value = null; // 重置选择
  // getCategory(); // 获取最新的门类列表
  importArchiveId.value = null;
  categoryDialog.value = true;
}

async function confirmCategory() {
  if (!selectedCategoryCodes.value || selectedCategoryCodes.value.length === 0) {
    proxy.$message.error("请至少选择一个门类");
    return;
  }

  try {
    // 显示加载状态
    loading.value = true;

    const updatePromises = ids.value.map(id => {
      let categoryValue = selectedCategoryCodes.value;

      if (Array.isArray(categoryValue)) {
        if (categoryValue.length === 0) {
          categoryValue = '';
        } else if (categoryValue.length === 1) {
          categoryValue = categoryValue[0];
        } else {
          categoryValue = categoryValue.join(',');
        }
      }

      return updateDocument({
        id: id,
        archiveId: importArchiveId.value,
        categoryCode: categoryValue,
      });
    });

    // 等待所有更新完成
    await Promise.all(updatePromises);

    // 显示成功消息
    proxy.$modal.msgSuccess(`成功将${ids.value.length}个文档关联到所选门类`);

    // 关闭对话框
    categoryDialog.value = false;

    // 刷新列表
    await getList();

    // 重置选择
    importArchiveId.value = null;
    selectedCategoryCodes.value = [];

  } catch (error) {
    console.error('批量更新失败:', error);
    proxy.$modal.msgError("批量更新失败：" + (error.message || error));
  } finally {
    loading.value = false;
  }
}

function cancelCategory() {
  categoryDialog.value = false;
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

/**
 * 处理表格排序变化 - 后端排序版本
 * @param {Object} sortInfo - 排序信息 { column, prop, order }
 */
function handleSortChange(sortInfo) {
  const { column, prop, order } = sortInfo;

  if (prop === 'categoryCode') {
    if (order === 'ascending') {
      queryParams.value.orderByColumn = 'categoryCode';
      queryParams.value.isAsc = 'asc';
    } else if (order === 'descending') {
      queryParams.value.orderByColumn = 'categoryCode';
      queryParams.value.isAsc = 'desc';
    } else {
      // 取消排序
      queryParams.value.orderByColumn = null;
      queryParams.value.isAsc = null;
    }

    // 重置到第一页并重新查询
    queryParams.value.pageNum = 1;
    getList();
  }
}

onMounted(() => {
    console.log("🚀 页面挂载 (onMounted)，即将调用 getList");
  getList();
  getCategoryRootList();
});
</script>
