<template>
  <div class="app-container">
    <!-- 搜索表单 -->
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="分类名称" prop="name">
        <el-input v-model="queryParams.name" placeholder="请输入分类名称" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="分类编码" prop="code">
        <el-input v-model="queryParams.code" placeholder="请输入分类编码" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 操作按钮行 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAddTree">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Download" @click="handleExport">导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 分类树列表 - 使用4列布局 -->
    <el-row :gutter="10" class="mb6" v-loading="loading">
      <el-col :span="6" v-for="tree in treeList" :key="tree.id">
        <el-card class="mb8">
          <template #header>
            <div class="card-header">
              <span>{{ tree.code }}</span>
              <span>{{ tree.name }}</span>
            </div>
          </template>
          <div class="card-body">
            <el-tree
                style="max-width: 100%"
                :data="treeDataMap[tree.id] || [tree]"
                :props="{ children: 'children', label: 'name' }"
                :default-expanded-keys="getDefaultExpandedKeys(tree.id)"
                :expand-on-click-node="false"
                node-key="id"
            >
              <template #default="{ node, data }">
                <span class="tree-node-preview">
                  <span class="node-code">{{ data.code }}</span>
                  <span class="node-name">{{ data.name }}</span>
                </span>
              </template>
            </el-tree>
          </div>
          <template #footer>
            <div class="card-footer">
              <el-button type="primary" plain @click="handleView(tree)">查看</el-button>
              <el-button type="primary" plain @click="handleCopy(tree)">复制</el-button>
              <el-button type="danger" plain @click="handleDelete(tree)">删除</el-button>
            </div>
          </template>
        </el-card>
      </el-col>
    </el-row>

    <!-- 分页 -->
    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize"
                @pagination="getList" />

    <!-- 新增根节点对话框 -->
    <el-dialog
        v-model="addDialogVisible"
        title="新建分类树"
        width="500px"
    >
      <el-form ref="addFormRef" :model="addForm" :rules="rules" label-width="100px">
        <el-form-item label="分类编码" prop="code">
          <el-input v-model="addForm.code" placeholder="请输入分类编码，如A" />
        </el-form-item>
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="addForm.name" placeholder="请输入分类名称" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="addDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleAddConfirm">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import {ref, reactive, onMounted, getCurrentInstance, toRefs} from 'vue'
import {useRouter} from 'vue-router'
import {ElMessage, ElMessageBox} from 'element-plus'
import {Plus, Edit, Delete, Search, Refresh, Download} from '@element-plus/icons-vue'
import {getCategoryRoots, getCategoryTree, addCategoryRoot, deleteCategory,copyCategoryTree} from '@/api/manage/treeCategory.js'


const {proxy} = getCurrentInstance()
const router = useRouter()

// 树列表数据
const treeList = ref([])
const treeDataMap = ref({}) // 存储每个树的完整数据
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)

// 查询参数
const data = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    name: null,
    code: null,
  }
})
const {queryParams} = toRefs(data)

// 新增对话框
const addDialogVisible = ref(false)
const addFormRef = ref()
const addForm = reactive({
  code: '',
  name: ''
})

// 表单验证规则
const rules = {
  code: [
    {required: true, message: '请输入分类编码', trigger: 'blur'},
    {pattern: /^[A-Z]$/, message: '编码必须是单个大写字母（如A、B、C）', trigger: 'blur'},
  ],
  name: [
    {required: true, message: '请输入分类名称', trigger: 'blur'}
  ]
}

// 检查编码唯一性
function checkCodeUnique(rule, value, callback) {
  if (!value) {
    callback()
    return
  }

  const exists = treeList.value.some(tree => tree.code === value)
  if (exists) {
    callback(new Error('编码已存在，请使用其他编码'))
  } else {
    callback()
  }
}

// 获取列表
async function getList() {
  loading.value = true
  try {
    const response = await getCategoryRoots(queryParams.value)
    treeList.value = response.data || response.rows || []

    // 按分类编码进行字母排序（新增部分）
    treeList.value.sort((a, b) => {
      return a.code.localeCompare(b.code)
    })

    total.value = response.total || treeList.value.length

    // 清空之前的树数据
    treeDataMap.value = {}

    // 异步加载每个树的完整数据
    await loadAllTreeData()
  } catch (error) {
    ElMessage.error('加载分类树失败')
  } finally {
    loading.value = false
  }
}

// 加载所有树的完整数据
async function loadAllTreeData() {
  const promises = treeList.value.map(tree => loadTreeData(tree.id))
  await Promise.all(promises)
}

// 加载单个树的完整数据
async function loadTreeData(treeId) {
  try {
    const response = await getCategoryTree(treeId)
    const data = response.data

    if (data && typeof data === 'object') {
      // 如果API返回的是单个树形对象，直接作为根节点
      if (data.id) {
        treeDataMap.value[treeId] = [data]
      } else {
        // 如果返回的是数组格式的树形数据
        treeDataMap.value[treeId] = Array.isArray(data) ? data : []
      }
    } else {
      treeDataMap.value[treeId] = []
    }
  } catch (error) {
    console.error(`加载分类树 ${treeId} 失败:`, error)
    // 加载失败时使用根节点数据
    const rootNode = treeList.value.find(t => t.id === treeId)
    if (rootNode) {
      treeDataMap.value[treeId] = [rootNode]
    }
  }
}

// 获取默认展开的节点（展开前两层）
function getDefaultExpandedKeys(treeId) {
  const treeData = treeDataMap.value[treeId]
  if (!treeData || !treeData[0]) return []

  const keys = [treeData[0].id] // 展开根节点

  // 展开第一层子节点
  if (treeData[0].children) {
    treeData[0].children.forEach(child => {
      keys.push(child.id)
    })
  }

  return keys
}

// 搜索按钮操作
async function handleQuery() {
  queryParams.value.pageNum = 1
  try {
    loading.value = true
    // 构造查询参数对象
    const params = {
      pageNum: queryParams.value.pageNum,
      pageSize: queryParams.value.pageSize,
      name: queryParams.value.name?.trim(), // 去除前后空格
      code: queryParams.value.code?.trim()  // 去除前后空格
    }

    // 调用API时明确传递参数
    const response = await getCategoryRoots(params)
    treeList.value = response.data || response.rows || []
    total.value = response.total || treeList.value.length

    // 加载树数据
    await loadAllTreeData()
  } catch (error) {
    ElMessage.error('搜索失败: ' + (error.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

// 重置按钮操作
function resetQuery() {
  proxy.resetForm("queryRef")
  handleQuery()
}

// 新建分类树
const handleAddTree = () => {
  // 自动建议下一个可用的编码
  const usedCodes = treeList.value.map(tree => tree.code).sort()
  let suggestedCode = 'A'

  // 找到第一个未使用的字母
  for (let i = 0; i < 26; i++) {
    const code = String.fromCharCode(65 + i) // A-Z
    if (!usedCodes.includes(code)) {
      suggestedCode = code
      break
    }
  }

  addForm.code = suggestedCode
  addForm.name = ''
  addDialogVisible.value = true
}

const handleAddConfirm = async () => {
  try {
    await addFormRef.value.validate()

    const newCategory = {
      code: addForm.code,
      name: addForm.name,
      parentId: null,
      ancestors: '',
      sortOrder: treeList.value.length + 1,
      rootId: null
    }

    const response = await addCategoryRoot(newCategory)
    const createdCategory = response.data

    ElMessage.success('创建成功')
    addDialogVisible.value = false

    // 跳转到设计器页面，传递真实的数据库ID
    router.push({
      path: '/manage/category/add',
      query: {
        id: createdCategory.id,
        rootId: createdCategory.rootId || createdCategory.id,
        rootCode: createdCategory.code,
        rootName: createdCategory.name,
        mode: 'create'
      }
    })
  } catch (error) {
    console.error('创建失败', error)
    ElMessage.error('创建失败：' + (error.message || '未知错误'))
  }
}

// 查看/编辑分类树（合并了原来的查看和编辑功能）
const handleView = (tree) => {
  router.push({
    path: '/manage/category/add',
    query: {
      id: tree.id,
      rootId: tree.rootId || tree.id,
      rootCode: tree.code,
      rootName: tree.name,
      mode: 'edit'
    }
  })
}

// 复制分类树
async function handleCopy(tree) {

  try {
    // 调用新增API
    await copyCategoryTree(tree.rootId)
    ElMessage.success("复制成功")
    getList()
  } catch (error) {
    ElMessage.error("复制失败: " + (error.message || "未知错误"))
  }
}

// 删除分类树
const handleDelete = async (tree) => {
  try {
    await ElMessageBox.confirm(
        `确定要删除分类树"${tree.name}"吗？这将删除整个分类树及其所有子分类！`,
        '删除确认',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }
    )

    await deleteCategory(tree.id)
    ElMessage.success('删除成功')
    getList()
  } catch (error) {
    // 用户取消或删除失败
  }
}

// 导出按钮操作
function handleExport() {
  proxy.download('manage/category/export', {
    ...queryParams.value
  }, `category_${new Date().getTime()}.xlsx`)
}

// 初始化
onMounted(() => {
  getList()
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
}

.card-body {
  height: 150px; /* 增加高度以显示更多内容 */
  overflow-y: auto;
}

.card-footer {
  padding: 0px;
  margin: 0px;
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 10px; /* 按钮之间的间距 */
}

.tree-node-preview {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 12px; /* 稍微缩小字体 */
}

.node-code {
  font-weight: bold;
  color: #409eff;
  min-width: 40px;
}

.node-name {
  color: #303133;
}

:deep(.el-tree-node__content) {
  height: auto;
  padding: 3px 0;
}

:deep(.el-tree-node__expand-icon) {
  font-size: 14px;
}

/* 加载动画样式 */
.el-loading-mask {
  background-color: rgba(255, 255, 255, 0.9);
}
</style>