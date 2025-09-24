<template>
  <div>
    <h3 style="text-align: center;">档案名称：【{{ archive.name }}】</h3>
    <el-divider />
    <el-row :gutter="2">
      <el-col :span="5">
        <el-card style="max-width: 100%;">
          <template #header>
            <div class="card-header"
              style="display: flex; flex-direction: row;justify-content: space-between; align-items: center;">
              <el-text size="large" tag="b">档案分类列表</el-text>
            </div>
          </template>

          <!-- 加载状态 -->
          <div v-if="categoryTreeLoading" style="text-align: center; padding: 20px;">
            <el-icon class="is-loading">
              <Loading />
            </el-icon>
            <p style="margin-top: 10px; color: #909399;">加载分类树...</p>
          </div>

          <div v-else-if="archiveCategoryList.length > 0" class="archive-category-list">
            <div v-for="category in archiveCategoryList" :key="category.code" class="category-item">
              <div class="category-content">
                <!-- <el-icon style="margin-right: 8px;">
                  <Document />
                </el-icon> -->
                <span class="category-code">{{ category.code }}</span>
                <span class="category-name">{{ category.name }}</span>
              </div>
            </div>
          </div>

          <!-- 无数据提示 -->
          <el-empty v-else description="暂无分类数据" :image-size="80" />
        </el-card>
      </el-col>

      <el-col :span="19">
        <div class="toolbar"
          style="margin-bottom: 15px; display: flex; justify-content: space-between; align-items: center;">
          <span style="margin-left: 12px; color: var(--el-color-info); font-size: 0.9em;">
            已选择 {{ selectedDocuments.length }} 个文档
          </span>
          <div>
            <el-button type="primary" @click="handleBatchCategoryChange" :disabled="selectedDocuments.length === 0"
              v-hasPermi="['manage:archive:edit']">
              批量修改门类
            </el-button>
          </div>
        </div>

        <el-table ref="tableRef" v-loading="loading" :data="documentList" @selection-change="handleSelectionChange">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column label="ID" align="center" prop="id" width="80" />
          <el-table-column label="文件名称" align="center" prop="name" min-width="200" />
          <el-table-column label="当前门类" prop="categoryName" align="center" width="150">
            <template #default="scope">
              <el-tag v-if="scope.row.categoryName" type="info" size="small">
                {{ scope.row.categoryName }}
              </el-tag>
              <span v-else class="text-muted">未设置</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="200">
            <template #default="scope">
              <el-button link type="primary" @click="handleCategoryChange(scope.row)"
                v-hasPermi="['manage:archive:edit']">修改门类</el-button>
              <el-button link type="primary" @click="handleDocumentDetail(scope.row)"
                v-hasPermi="['manage:document:query']">查看</el-button>
            </template>
          </el-table-column>
        </el-table>

        <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum"
          v-model:limit="queryParams.pageSize" @pagination="getDocumentList" />
      </el-col>
    </el-row>

    <!-- 单个文档修改门类对话框 -->
    <el-dialog title="修改文档门类" v-model="categoryOpen" width="600px" append-to-body>
      <el-form label-width="100px">
        <el-form-item label="文档名称">
          <el-input v-model="selectedDocument.name" disabled></el-input>
        </el-form-item>
        <el-form-item label="当前门类">
          <el-tag v-if="selectedDocument.categoryName" type="info">
            {{ selectedDocument.categoryName }}
          </el-tag>
          <span v-else>未设置</span>
        </el-form-item>
        <el-form-item label="选择门类">
<!--          <el-tree-select v-model="selectedCategoryCode" :data="categoryTreeData" :props="{-->
<!--            label: 'name',-->
<!--            value: 'code',-->
<!--            children: 'children'-->
<!--          }" placeholder="请选择门类" style="width: 100%" :render-after-expand="false" show-checkbox-indeterminate-->
<!--            check-strictly>-->
<!--            <template #default="{ node, data }">-->
<!--              <span class="tree-select-node">-->
<!--                <span class="node-code">{{ data.code }}</span>-->
<!--                <span class="node-name">{{ data.name }}</span>-->
<!--              </span>-->
<!--            </template>-->
<!--          </el-tree-select>-->
          <!-- 加载状态 -->
          <div v-if="categoryTreeLoading" style="text-align: center; padding: 10px 0;">
            <el-icon class="is-loading">
              <Loading />
            </el-icon>
            <span style="margin-left: 8px;">加载分类列表...</span>
          </div>

          <!-- 普通多选下拉框 -->
          <el-select
              v-else
              v-model="selectedCategoryCode"
              multiple
              placeholder="请选择门类"
              style="width: 100%"
              filterable
              clearable
          >
            <el-option
                v-for="category in archiveCategoryList"
                :key="category.code"
                :label="`${category.name}`"
                :value="category.code"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="categoryOpen = false">取 消</el-button>
        <el-button type="primary" @click="saveDocumentCategory">确 定</el-button>
      </template>
    </el-dialog>

    <!-- 批量修改门类对话框 -->
    <el-dialog title="批量修改文档门类" v-model="batchCategoryOpen" width="600px" append-to-body>
      <el-alert :title="`即将修改 ${selectedDocuments.length} 个文档的门类`" type="warning" show-icon :closable="false"
        style="margin-bottom: 20px" />

      <el-form label-width="100px">
        <el-form-item label="目标门类">
<!--          <el-tree-select v-model="selectedCategoryCode" :data="archiveCategoryList" :props="{-->
<!--            label: 'name',-->
<!--            value: 'code',-->
<!--            children: 'children'-->
<!--          }" placeholder="请选择目标门类" style="width: 100%" :render-after-expand="false"-->
<!--            show-checkbox-indeterminate-->
<!--            value-key="code"-->
<!--            multiple-->
<!--            check-strictly>-->
<!--            <template #default="{ node, data }">-->
<!--              <span class="tree-select-node">-->
<!--                <span class="node-code">{{ data.code }}</span>-->
<!--                <span class="node-name">{{ data.name }}</span>-->
<!--              </span>-->
<!--            </template>-->
<!--          </el-tree-select>-->
          <!-- 加载状态 -->
          <div v-if="categoryTreeLoading" style="text-align: center; padding: 10px 0;">
            <el-icon class="is-loading">
              <Loading />
            </el-icon>
            <span style="margin-left: 8px;">加载分类列表...</span>
          </div>

          <!-- 普通多选下拉框 -->
          <el-select
              v-else
              v-model="selectedCategoryCode"
              multiple
              placeholder="请选择门类"
              style="width: 100%"
              filterable
              clearable
          >
            <el-option
                v-for="category in archiveCategoryList"
                :key="category.code"
                :label="`${category.name}`"
                :value="category.code"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="预览">
          <el-tag v-if="selectedCategoryName" type="success">
            {{ selectedCategoryName }}
          </el-tag>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="batchCategoryOpen = false">取 消</el-button>
        <el-button type="primary" @click="saveBatchCategory" :loading="batchSaving">确 定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { getArchive } from "@/api/manage/archive";
import { listDocument, updateDocument, batchUpdateDocumentCategory } from "@/api/manage/document";
import {getCategoryTreeByCode, getCategoryByCode, getCategoryByCodeWithRoot} from "@/api/manage/treeCategory.js";
import { Folder, Document, Loading } from '@element-plus/icons-vue'

const { proxy } = getCurrentInstance();
const route = useRoute();

// 基础状态
const archive = ref({});
const documentList = ref([]);
const selectedDocument = ref({});
const loading = ref(true);
const total = ref(0);
const queryParams = ref({
  archiveId: route.query.id,
  pageNum: 1,
  pageSize: 10,
});

// 分类树相关状态
const categoryTreeData = ref([])
const categoryTreeLoading = ref(false)
const categoryTreeProps = {
  id: 'id',
  label: 'name',
  children: 'children',
}

// 门类相关状态
const categoryOpen = ref(false)
const batchCategoryOpen = ref(false)
// const selectedCategoryCode = ref('')
const selectedCategoryCode = ref([])
const batchSaving = ref(false)

// 表格选择相关状态
const selectedDocuments = ref([]);
const tableRef = ref();

// 计算属性：获取选中的门类名称
const selectedCategoryName = computed(() => {
  if (!selectedCategoryCode.value || categoryTreeData.value.length === 0) {
    return ''
  }

  // const findNodeByCode = (nodes, code) => {
  //   for (const node of nodes) {
  //     if (node.code === code) {
  //       return node.name
  //     }
  //     if (node.children && node.children.length > 0) {
  //       const found = findNodeByCode(node.children, code)
  //       if (found) return found
  //     }
  //   }
  //   return ''
  // }
  //
  // return findNodeByCode(categoryTreeData.value, selectedCategoryCode.value)
  return selectedCategoryCode.value.map(code => {
    const category = archiveCategoryList.value.find(item => item.code === code)
    return category ? category.name : code
  }).join(', ')
})

// 处理表格多选
const handleSelectionChange = (selection) => {
  selectedDocuments.value = selection;
};

// 加载分类树数据
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

// 处理分类树节点点击
function handleCategoryNodeClick(data, node) {
  console.log('点击分类节点:', data)
  // 可以在这里添加筛选逻辑
}

// 单个文档修改门类
function handleCategoryChange(row) {
  selectedDocument.value = row
  // selectedCategoryCode.value = row.categoryCode || ''
  selectedCategoryCode.value = row.categoryCode
      ? row.categoryCode.split(',')
      : []
  categoryOpen.value = true
}

// 保存单个文档门类修改
async function saveDocumentCategory() {
  if (!selectedCategoryCode.value) {
    ElMessage.warning('请选择门类')
    return
  }

  try {
    // const updateData = {
    //   ...selectedDocument.value,
    //   categoryCode: selectedCategoryCode.value,
    //   categoryName: selectedCategoryName.value
    // }
    const categoryNames = selectedCategoryCode.value.map(code => {
      const category = archiveCategoryList.value.find(item => item.code === code)
      return category ? category.name : code
    })

    const updateData = {
      ...selectedDocument.value,
      // 将数组转换为逗号分隔的字符串
      categoryCode: Array.isArray(selectedCategoryCode.value)
          ? selectedCategoryCode.value.join(',')
          : selectedCategoryCode.value,
      categoryName: categoryNames.join(',')
    }

    await updateDocument(updateData)

    // // 更新列表中的数据
    // const index = documentList.value.findIndex(doc => doc.id === selectedDocument.value.id)
    // if (index !== -1) {
    //   documentList.value[index].categoryCode = selectedCategoryCode.value
    //   documentList.value[index].categoryName = selectedCategoryName.value
    // }
    // 更新列表数据
    const index = documentList.value.findIndex(doc => doc.id === selectedDocument.value.id)
    if (index !== -1) {
      documentList.value[index].categoryCode = updateData.categoryCode
      documentList.value[index].categoryName = updateData.categoryName
    }

    ElMessage.success('门类修改成功')
    categoryOpen.value = false
    selectedCategoryCode.value = ''
  } catch (error) {
    ElMessage.error('修改失败：' + error.message)
  }
}

// 批量修改门类
function handleBatchCategoryChange() {
  if (selectedDocuments.value.length === 0) {
    ElMessage.warning('请至少选择一个文档')
    return
  }
  selectedCategoryCode.value = ''
  batchCategoryOpen.value = true
}

// 保存批量门类修改
async function saveBatchCategory() {
  if (!selectedCategoryCode.value || selectedCategoryCode.value.length === 0) {
    ElMessage.warning('请选择目标门类')
    return
  }

  try {
    // 获取所有选中的门类名称（处理多选）
    const categoryNames = selectedCategoryCode.value.map(code => {
      const category = archiveCategoryList.value.find(item => item.code === code)
      return category ? category.name : code
    }).join('、') // 使用中文顿号分隔

    await ElMessageBox.confirm(
        `确定要将选中的 ${selectedDocuments.value.length} 个文档的门类修改为: ${categoryNames} 吗？`,
        '确认',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning',
        }
    )

    batchSaving.value = true

    // 构造批量更新参数
    const categoryCodeStr = Array.isArray(selectedCategoryCode.value)
        ? selectedCategoryCode.value.join(',')
        : selectedCategoryCode.value

    const updateParams = {
      docIds: selectedDocuments.value.map(doc => doc.id),
      categoryCode: categoryCodeStr,
      categoryName: categoryNames
    }

    // 调用批量更新接口
    await batchUpdateDocumentCategory(updateParams)

    // 正确更新前端数据（关键修改）
    const updatedIds = new Set(selectedDocuments.value.map(doc => doc.id))

    documentList.value = documentList.value.map(item => {
      if (updatedIds.has(item.id)) {
        // 创建新对象确保响应式更新
        return {
          ...item,
          categoryCode: categoryCodeStr,
          categoryName: categoryNames
        }
      }
      return item
    })

    ElMessage.success(`已成功更新 ${selectedDocuments.value.length} 个文档的门类`)

  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('批量更新失败：' + error.message)
    }
  } finally {
    // 重置状态
    tableRef.value?.clearSelection()
    selectedCategoryCode.value = []
    batchCategoryOpen.value = false
    batchSaving.value = false

    // 如果需要强制刷新
    // await getDocumentList()
  }
}

// 获取文档列表
async function getDocumentList() {
  loading.value = true;
  try {
    const res = await listDocument(queryParams.value);

    console.log('获取文档列表成功:', res)

    documentList.value = res.rows;

  } finally {
    loading.value = false;
  }
}

// 查看文档详情
function handleDocumentDetail(row) {
  proxy.$router.push({ path: "/manage/document/doc-detail", query: { id: row.id } });
}

// 侧边分类相关=========================================
const archiveCategoryList = ref([]) // 档案自带的分类列表
/**
 * 解析档案分类ID字符串，获取分类信息
 */
async function parseArchiveCategories(categoryIdStr) {


  if (!categoryIdStr) {
    console.warn('档案没有设置categoryId')
    archiveCategoryList.value = []
    return
  }

  categoryTreeLoading.value = true
  try {
    const parts = categoryIdStr.split(',').filter(code => code.trim())

    // 第一个元素是根分类信息 "B:监理文件"
    const rootInfo = parts[0]
    let rootCode = '' // B
    let rootName = '' // 监理文件

    // 解析根分类信息
    if (rootInfo && rootInfo.includes(':')) {
      const [code, name] = rootInfo.split(':')
      rootCode = code.trim()
      rootName = name.trim()
    }

    const categoryCodes = parts.slice(1)

    const categoryPromises = categoryCodes.map(code => getCategoryByCodeWithRoot({
      rootCode: rootCode,
      rootName: rootName,
      categoryId: code.trim()
    }))

    const categoryResults = await Promise.all(categoryPromises)

    archiveCategoryList.value = categoryResults
        .filter(result => result.data) // 过滤掉获取失败的
        .map(result => ({
          code: result.data.code,
          name: result.data.name,
          documentCount: 0 // 初始化为0，后面根据实际文档统计
        }))

    console.log('解析后的档案分类:', archiveCategoryList.value)

  } catch (error) {
    console.error('解析档案分类失败:', error)
    ElMessage.error('加载档案分类失败')
    archiveCategoryList.value = []
  } finally {
    categoryTreeLoading.value = false
  }
}

// 页面初始化
onMounted(() => {
  const archiveId = route.query.id;
  getArchive(archiveId).then(response => {
    archive.value = response.data;

    if (archive.value.categoryId) {
      parseArchiveCategories(archive.value.categoryId)
    }
  });
  getDocumentList();
});
</script>

<style scoped>
.category-tree-node {
  display: flex;
  align-items: center;
  font-size: 14px;
}

.node-code {
  color: #409EFF;
  margin-right: 8px;
  font-weight: 500;
}

.node-name {
  color: #606266;
}

.category-tree-node:hover .node-name {
  color: #409EFF;
}

.tree-select-node {
  display: flex;
  align-items: center;
  gap: 8px;
}

.tree-select-node .node-code {
  color: #409EFF;
  font-weight: 500;
}

.tree-select-node .node-name {
  color: #606266;
}

.text-muted {
  color: #909399;
  font-size: 12px;
}

.toolbar {
  padding: 10px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.archive-category-list {
  padding: 10px 0;
}

.category-item {
  padding: 8px 12px;
  margin-bottom: 8px;
  border-radius: 4px;
  transition: all 0.2s;
  border: 1px solid transparent;
}

.category-content {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  /* 允许换行 */
}

.category-name {
  display: block;
  /* 让 name 字段独占一行 */
  max-width: 150px;
  /* 根据需要调整最大宽度 */
  overflow: hidden;
  /* 隐藏超出部分 */
  text-overflow: ellipsis;
  /* 使用省略号表示超出部分 */
  white-space: normal;
  /* 允许文本换行 */
  margin-left: 5px;
  /* 添加一些左边距 */
}

.category-code {
  color: #409EFF;
  margin-right: 8px;
  font-weight: 500;
  min-width: 60px;
}
</style>