<template>
  <div class="app-container">
    <div class="header">
      <h2>档案分类设计器 - {{ rootName }}（{{ rootCode }}）</h2>
      <div class="actions">
        <!--        <el-button @click="handleBack">-->
        <!--          <el-icon><ArrowLeft /></el-icon>-->
        <!--          返回列表-->
        <!--        </el-button>-->
        <el-button type="primary" @click="handleSave" :loading="saving">
          <el-icon><Check /></el-icon>
          保存设计
        </el-button>
        <el-button @click="handleExport">
          <el-icon><Download /></el-icon>
          导出JSON
        </el-button>


      </div>
    </div>

    <div class="designer-container" v-loading="loading">
      <div class="toolbar">
        <el-button type="primary" @click="addRootNode" v-if="!hasRootNode">
          <el-icon><Plus /></el-icon>
          添加根分类
        </el-button>
        <el-button @click="expandAll" v-if="hasRootNode">展开全部</el-button>
        <el-button @click="collapseAll" v-if="hasRootNode">收起全部</el-button>
        <el-button @click="reorderCodes" type="warning" v-if="hasRootNode">
          <el-icon><Refresh /></el-icon>
          重新排序编码
        </el-button>
        <!-- 新增导入按钮 -->
        <el-button @click="handleImport" type="success">
          <el-icon><Upload /></el-icon>
          导入JSON
        </el-button>

      </div>


      <div class="tree-wrapper">
        <div v-if="!hasRootNode" class="empty-tree">
          <el-empty description="暂无分类数据">
            <el-button type="primary" @click="addRootNode">添加根分类</el-button>
          </el-empty>
        </div>
        <el-tree
            v-else
            ref="treeRef"
            :data="treeData"
            :props="treeProps"
            node-key="id"
            default-expand-all
            :expand-on-click-node="false"
            draggable
            :allow-drop="allowDrop"
            :allow-drag="allowDrag"
            @node-drop="handleDrop"
        >
          <template #default="{ node, data }">
            <div class="tree-node">
              <div class="node-content">
                <span class="node-code">{{ data.code }}</span>
                <span class="node-name">{{ data.name }}</span>
                <span class="node-info">
                  <el-tag size="small" type="info">{{ getNodeLevel(data) }}</el-tag>
                </span>
              </div>
              <div class="node-actions">
                <el-button
                    type="primary"
                    size="small"
                    circle
                    @click.stop="editNode(data)"
                >
                  <el-icon><Edit /></el-icon>
                </el-button>
                <el-button
                    type="success"
                    size="small"
                    circle
                    @click.stop="addChildNode(data)"
                    v-if="canAddChild(data)"
                >
                  <el-icon><Plus /></el-icon>
                </el-button>
                <el-button
                    type="danger"
                    size="small"
                    circle
                    @click.stop="removeNode(data)"
                    v-if="!isRootNode(data)"
                >
                  <el-icon><Delete /></el-icon>
                </el-button>
              </div>
            </div>
          </template>
        </el-tree>
      </div>
    </div>

    <!-- 编辑对话框 -->
    <el-dialog
        v-model="dialogVisible"
        :title="isEdit ? '编辑分类' : '新增分类'"
        width="500px"
    >
      <el-form ref="formRef" :model="nodeForm" :rules="rules" label-width="100px">
        <el-form-item label="分类编码" prop="code">
          <el-input
              v-model="nodeForm.code"
              :placeholder="isEdit ? '修改编码' : '自动生成，可修改'"
              :disabled="isRootNodeEdit"
          />
        </el-form-item>
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="nodeForm.name" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="排序号">
          <el-input-number v-model="nodeForm.sortOrder" :min="1" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleConfirm">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 隐藏的文件输入框 -->
    <input
        ref="fileInputRef"
        type="file"
        accept=".json"
        style="display: none"
        @change="handleFileChange"
    />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus,
  Edit,
  Delete,
  Check,
  Download,
  ArrowLeft,
  Upload, // 新增
  Refresh
} from '@element-plus/icons-vue'
// 在现有的 ref 声明中添加文件输入框的引用
const fileInputRef = ref()

// 导入JSON的处理方法
const handleImport = () => {
  fileInputRef.value.click()
}

// 处理文件选择
const handleFileChange = (event) => {
  const file = event.target.files[0]
  if (!file) return

  // 检查文件类型
  if (!file.name.endsWith('.json')) {
    ElMessage.error('请选择JSON格式的文件')
    return
  }

  const reader = new FileReader()
  reader.onload = (e) => {
    try {
      const jsonData = JSON.parse(e.target.result)
      importTreeData(jsonData)
    } catch (error) {
      console.error('JSON解析失败:', error)
      ElMessage.error('JSON文件格式错误，请检查文件内容')
    }
  }
  reader.readAsText(file)

  // 清空文件输入框的值，以便同一个文件可以重复选择
  event.target.value = ''
}

// 导入树形数据
const importTreeData = async (jsonData) => {
  try {
    // 验证数据格式
    if (!Array.isArray(jsonData)) {
      ElMessage.error('导入的数据格式不正确，应该是数组格式')
      return
    }

    if (jsonData.length === 0) {
      ElMessage.warning('导入的文件中没有数据')
      return
    }

    // 确保有根节点存在
    if (treeData.value.length === 0) {
      ElMessage.error('请先创建根节点再导入子分类')
      return
    }

    const currentRoot = treeData.value[0]
    const importedRoot = jsonData[0]

    // 检查导入数据是否有根节点
    if (!importedRoot || !isRootNode(importedRoot)) {
      ElMessage.error('导入的数据格式不正确，缺少根节点')
      return
    }

    // 检查根节点编码是否匹配
    if (importedRoot.code !== currentRoot.code) {
      ElMessage.error(`导入失败：根节点编码不匹配（当前：${currentRoot.code}，导入：${importedRoot.code}）`)
      return
    }

    // 如果当前根节点已有子分类，询问是否覆盖
    if (currentRoot.children && currentRoot.children.length > 0) {
      try {
        await ElMessageBox.confirm(
            '导入新数据将覆盖当前的子分类设计，根节点信息保持不变，是否继续？',
            '导入确认',
            {
              confirmButtonText: '确定',
              cancelButtonText: '取消',
              type: 'warning'
            }
        )
      } catch (e) {
        // 用户取消导入
        return
      }
    }

    // 验证导入的子节点数据结构
    const childrenToImport = importedRoot.children || []
    if (childrenToImport.length > 0) {
      const isValidData = validateChildrenData(childrenToImport)
      if (!isValidData) {
        ElMessage.error('导入的子分类数据格式不正确')
        return
      }
    }

    // 更新全局ID计数器，避免ID冲突
    const getAllIds = (nodes) => {
      let ids = []
      nodes.forEach(node => {
        ids.push(node.id)
        if (node.children && node.children.length > 0) {
          ids = ids.concat(getAllIds(node.children))
        }
      })
      return ids
    }

    if (childrenToImport.length > 0) {
      const allIds = getAllIds(childrenToImport)
      const maxId = Math.max(...allIds)
      if (maxId >= idCounter) {
        idCounter = maxId + 1
      }
    }

    // 只导入子节点，保持根节点不变
    currentRoot.children = childrenToImport

    // 更新导入的子节点的父级信息，确保指向当前根节点
    const updateParentInfo = (children, parentNode, currentRootId) => {
      children.forEach(child => {
        child.parentId = parentNode.id
        child.ancestors = generateAncestors(parentNode)
        child.rootId = currentRootId // 统一使用当前根节点的ID

        if (child.children && child.children.length > 0) {
          updateParentInfo(child.children, child, currentRootId)
        }
      })
    }

    if (childrenToImport.length > 0) {
      updateParentInfo(childrenToImport, currentRoot, currentRoot.id)
    }

    ElMessage.success(`导入成功！共导入 ${childrenToImport.length} 个一级分类`)

    // 导入后展开所有节点
    setTimeout(() => {
      expandAll()
    }, 100)

  } catch (error) {
    console.error('导入失败:', error)
    ElMessage.error('导入失败，请检查文件格式')
  }
}

// 验证子节点数据的完整性
const validateChildrenData = (children) => {
  const validate = (nodes, parentCode) => {
    return nodes.every(node => {
      // 检查基本字段
      if (!node.id || !node.code || !node.name) {
        return false
      }

      // 检查编码是否符合规范（应该以父编码开头）
      if (parentCode && !node.code.startsWith(parentCode)) {
        return false
      }

      // 递归检查子节点
      if (node.children && node.children.length > 0) {
        return validate(node.children, node.code)
      }

      return true
    })
  }

  return validate(children, rootCode.value)
}

// 辅助函数：验证树形结构的完整性
const validateTreeStructure = (data) => {
  const validate = (nodes, parentId = null) => {
    return nodes.every(node => {
      // 检查基本字段
      if (!node.id || !node.code || !node.name) {
        return false
      }

      // 检查父子关系
      if (node.parentId !== parentId) {
        return false
      }

      // 递归检查子节点
      if (node.children && node.children.length > 0) {
        return validate(node.children, node.id)
      }

      return true
    })
  }

  return validate(data)
}
// 导入API函数
import {
  getCategoryTree,
  updateCategoryTree
} from '@/api/manage/treeCategory.js'

const router = useRouter()
const route = useRoute()

// 加载状态
const loading = ref(false)
const saving = ref(false)

// 从路由参数获取基本信息
const rootId = computed(() => route.query.rootId)
const rootCode = computed(() => route.query.rootCode || 'A')
const rootName = computed(() => route.query.rootName || '新分类树')
const mode = computed(() => route.query.mode || 'edit')
const isCreateMode = computed(() => mode.value === 'create')

// 树形数据
const treeData = ref([])
const treeRef = ref()
const treeProps = {
  children: 'children',
  label: 'name'
}

// 计算属性
const hasRootNode = computed(() => treeData.value.length > 0)

// 对话框相关
const dialogVisible = ref(false)
const isEdit = ref(false)
const isRootNodeEdit = ref(false)
const formRef = ref()
const currentParent = ref(null)
const currentEditNode = ref(null)

// 表单数据
const nodeForm = reactive({
  code: '',
  name: '',
  sortOrder: 1
})

// 表单验证规则
const rules = {
  code: [
    { required: true, message: '请输入分类编码', trigger: 'blur' },
    { pattern: /^[A-Z][A-Z0-9]*$/, message: '编码必须以大写字母开头，只能包含大写字母和数字', trigger: 'blur' }
  ],
  name: [
    { required: true, message: '请输入分类名称', trigger: 'blur' }
  ]
}

// ID生成器（实际应由后端生成）
let idCounter = 1000
const generateId = () => {
  return idCounter++
}

// 判断是否为根节点
const isRootNode = (node) => {
  return node.parentId === null || node.parentId === undefined
}

// 获取节点层级数字
const getNodeLevelNumber = (node) => {
  if (isRootNode(node)) return 0
  const rootCodeLength = rootCode.value.length
  const codeLength = node.code.length
  return (codeLength - rootCodeLength) / 2
}

// 获取节点层级显示
const getNodeLevel = (node) => {
  const level = getNodeLevelNumber(node)
  if (level === 0) return '根节点'
  if (level === 1) return '一级分类'
  if (level === 2) return '二级分类'
  return `Level ${level}`
}

// 判断是否可以添加子节点（最多两级）
const canAddChild = (node) => {
  return getNodeLevelNumber(node) < 2
}

// 生成分类编码的新规律：C01, C02 (一级) 和 C0101, C0102 (二级)
const generateCode = (parentNode, siblings = []) => {
  if (!parentNode) {
    // 根节点：直接使用传入的rootCode
    return rootCode.value
  } else {
    // 子节点：基于父节点编码生成递增编码，每级用两位数字
    const parentCode = parentNode.code

    // 提取已存在的同级编码，找出数字部分
    const existingCodes = siblings
        .map(s => s.code)
        .filter(code => code.startsWith(parentCode) && code.length === parentCode.length + 2)
        .map(code => {
          const suffix = code.substring(parentCode.length)
          return parseInt(suffix)
        })
        .filter(num => !isNaN(num))
        .sort((a, b) => a - b)

    // 找到第一个可用的数字
    let nextNum = 1
    for (const num of existingCodes) {
      if (num === nextNum) {
        nextNum++
      } else {
        break
      }
    }

    // 返回父节点编码 + 两位数字
    return `${parentCode}${nextNum.toString().padStart(2, '0')}`
  }
}

// 生成祖先路径
const generateAncestors = (parentNode) => {
  if (!parentNode) return ''
  const ancestors = parentNode.ancestors ? parentNode.ancestors.split(',').filter(id => id) : []
  ancestors.push(parentNode.id.toString())
  return ancestors.join(',')
}

// 重新排序所有编码
const reorderCodes = () => {
  const reorderNode = (nodes, parentCode = '') => {
    nodes.forEach((node, index) => {
      if (parentCode === '') {
        // 根节点保持原有编码不变
        node.sortOrder = index + 1
      } else {
        // 子节点重新编码：父编码 + 两位数字
        const newNumber = (index + 1).toString().padStart(2, '0')
        node.code = `${parentCode}${newNumber}`
        node.sortOrder = index + 1
      }

      if (node.children && node.children.length > 0) {
        reorderNode(node.children, node.code)
      }
    })
  }

  reorderNode(treeData.value)
  ElMessage.success('编码重新排序完成')
}

// 构建树形结构的辅助函数
const buildTree = (flatData) => {
  if (!flatData || flatData.length === 0) return []

  const map = new Map()
  const roots = []

  // 创建映射
  flatData.forEach(item => {
    map.set(item.id, { ...item, children: [] })
  })

  // 构建树形结构
  flatData.forEach(item => {
    const node = map.get(item.id)
    if (item.parentId === null || item.parentId === undefined) {
      roots.push(node)
    } else {
      const parent = map.get(item.parentId)
      if (parent) {
        parent.children.push(node)
      }
    }
  })

  return roots
}

// 初始化树数据
const initTreeData = () => {
  if (isCreateMode.value) {
    // 新建模式，需要创建根节点
    const rootNode = {
      id: parseInt(route.query.id) || generateId(),
      code: rootCode.value,
      name: rootName.value,
      parentId: null,
      ancestors: '',
      sortOrder: 1,
      rootId: parseInt(route.query.rootId) || parseInt(route.query.id),
      children: [],
    }
    treeData.value = [rootNode]
  } else {
    // 编辑模式，加载完整数据
    loadTreeData(rootId.value)
  }
}

const loadTreeData = async (treeRootId) => {
  if (!treeRootId) {
    ElMessage.error('缺少根节点ID')
    return
  }

  loading.value = true
  try {
    const response = await getCategoryTree(treeRootId)
    const data = response.data

    if (data && typeof data === 'object') {
      // 如果API返回的是单个树形对象，直接作为根节点
      if (data.id) {
        treeData.value = [data]
      } else {
        // 如果返回的是数组格式的树形数据
        treeData.value = Array.isArray(data) ? data : []
      }
    } else {
      treeData.value = []
      ElMessage.warning('该分类树暂无数据')
    }
  } catch (error) {
    console.error('加载分类树失败:', error)
    ElMessage.error('加载分类树失败')
    treeData.value = []
  } finally {
    loading.value = false
  }
}

// 添加根节点（仅在创建模式且没有根节点时使用）
const addRootNode = () => {
  const rootNode = {
    id: parseInt(route.query.id) || generateId(),
    code: rootCode.value,
    name: rootName.value,
    parentId: null,
    ancestors: '',
    sortOrder: 1,
    rootId: parseInt(route.query.rootId) || parseInt(route.query.id),
    children: [],
  }
  treeData.value = [rootNode]
  ElMessage.success('已创建根分类')
}

// 添加子节点
const addChildNode = (parentNode) => {
  // 检查层级限制（最多两级：一级和二级）
  const parentLevel = getNodeLevelNumber(parentNode)
  if (parentLevel >= 2) {
    ElMessage.warning('最多只能创建两级分类')
    return
  }

  isEdit.value = false
  isRootNodeEdit.value = false
  currentParent.value = parentNode

  // 自动生成编码
  const autoCode = generateCode(parentNode, parentNode.children || [])

  Object.assign(nodeForm, {
    code: autoCode,
    name: '',
    sortOrder: (parentNode.children?.length || 0) + 1
  })
  dialogVisible.value = true
}

// 编辑节点
const editNode = (node) => {
  isEdit.value = true
  isRootNodeEdit.value = isRootNode(node)
  currentEditNode.value = node
  Object.assign(nodeForm, {
    code: node.code,
    name: node.name,
    sortOrder: node.sortOrder
  })
  dialogVisible.value = true
}

// 删除节点
const removeNode = async (node) => {
  if (isRootNode(node)) {
    ElMessage.warning('不能删除根节点')
    return
  }

  try {
    await ElMessageBox.confirm(
        `确定要删除分类"${node.name}"吗？${node.children?.length > 0 ? '这将同时删除所有子分类！' : ''}`,
        '删除确认',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }
    )

    const removeFromTree = (data, targetId) => {
      for (let i = 0; i < data.length; i++) {
        if (data[i].id === targetId) {
          data.splice(i, 1)
          return true
        }
        if (data[i].children && removeFromTree(data[i].children, targetId)) {
          return true
        }
      }
      return false
    }

    removeFromTree(treeData.value, node.id)
    ElMessage.success('删除成功')
  } catch (e) {
    // 用户取消
  }
}

// 确认添加/编辑
const handleConfirm = async () => {
  try {
    await formRef.value.validate()

    if (isEdit.value) {
      // 编辑模式
      const oldCode = currentEditNode.value.code
      Object.assign(currentEditNode.value, {
        code: nodeForm.code,
        name: nodeForm.name,
        sortOrder: nodeForm.sortOrder,
      })

      // 如果编码改变了，需要更新所有子节点的编码
      if (oldCode !== nodeForm.code && !isRootNodeEdit.value) {
        updateChildrenCodes(currentEditNode.value, oldCode, nodeForm.code)
      }
    } else {
      // 新增模式
      const newNode = {
        id: generateId(),
        code: nodeForm.code,
        name: nodeForm.name,
        parentId: currentParent.value?.id || null,
        ancestors: generateAncestors(currentParent.value),
        sortOrder: nodeForm.sortOrder,
        rootId: parseInt(rootId.value),
        children: [],
      }

      if (currentParent.value) {
        if (!currentParent.value.children) {
          currentParent.value.children = []
        }
        currentParent.value.children.push(newNode)
      } else {
        treeData.value.push(newNode)
      }
    }

    dialogVisible.value = false
    ElMessage.success(isEdit.value ? '编辑成功' : '添加成功')
  } catch (e) {
    // 验证失败
  }
}

// 更新子节点编码
const updateChildrenCodes = (node, oldParentCode, newParentCode) => {
  if (node.children) {
    node.children.forEach(child => {
      // 替换编码前缀
      child.code = child.code.replace(oldParentCode, newParentCode)
      updateChildrenCodes(child, oldParentCode, newParentCode)
    })
  }
}

// 允许拖拽判断
const allowDrag = (node) => {
  return !isRootNode(node.data) // 根节点不允许拖拽
}

// 允许放置判断
const allowDrop = (draggingNode, dropNode, type) => {
  // 不允许拖拽到根节点同级
  if (type !== 'inner' && isRootNode(dropNode.data)) {
    return false
  }

  // 检查层级限制：拖拽后不能超过两级
  if (type === 'inner') {
    const targetLevel = getNodeLevelNumber(dropNode.data)
    const dragNodeLevel = getNodeLevelNumber(draggingNode.data)

    // 如果拖拽到目标节点内部，目标节点层级不能超过1（因为拖拽的节点会成为它的子节点）
    if (targetLevel >= 2) {
      return false
    }

    // 如果被拖拽的节点有子节点，还需要检查子节点层级
    if (draggingNode.data.children && draggingNode.data.children.length > 0) {
      // 被拖拽节点有子节点，拖拽后子节点会变成第三级，不允许
      if (targetLevel >= 1) {
        return false
      }
    }
  }

  return true
}

// 处理拖拽
const handleDrop = (draggingNode, dropNode, dropType) => {
  const dragData = draggingNode.data

  // 更新拖拽节点的父级信息
  if (dropType === 'inner') {
    dragData.parentId = dropNode.data.id
    dragData.ancestors = generateAncestors(dropNode.data)
  } else {
    dragData.parentId = dropNode.data.parentId
    dragData.ancestors = dropNode.data.ancestors
  }

  // 递归更新子节点的祖先路径
  const updateChildren = (node) => {
    if (node.children) {
      node.children.forEach(child => {
        child.ancestors = generateAncestors(node)
        updateChildren(child)
      })
    }
  }
  updateChildren(dragData)

  // 拖拽后自动重新排序编码
  setTimeout(() => {
    reorderCodes()
  }, 100)

  ElMessage.success('拖拽成功，编码已自动重排')
}

// 展开全部
const expandAll = () => {
  const nodes = treeRef.value.store.nodesMap
  for (const key in nodes) {
    nodes[key].expanded = true
  }
}

// 收起全部
const collapseAll = () => {
  const nodes = treeRef.value.store.nodesMap
  for (const key in nodes) {
    nodes[key].expanded = false
  }
}

// // 返回列表
// const handleBack = () => {
//   router.push('/test')
// }

// 保存设计
// 保存设计
const handleSave = async () => {
  if (treeData.value.length === 0) {
    ElMessage.warning('请先设计分类结构')
    return
  }

  saving.value = true
  try {
    if (isCreateMode.value) {
      // 新增模式：只保存子节点
      const children = treeData.value[0]?.children || []

      if (children.length === 0) {
        ElMessage.warning('请至少添加一个子分类')
        saving.value = false
        return
      }

      // 准备子节点数据
      const flattenChildren = (nodes, result = []) => {
        nodes.forEach(node => {
          const {children, ...nodeData} = node
          result.push({
            ...nodeData,
            createBy: 'admin',
            updateBy: 'admin'
          })
          if (children && children.length > 0) {
            flattenChildren(children, result)
          }
        })
        return result
      }

      const saveData = flattenChildren(children)
      console.log('保存的子节点数据：', saveData)

      // 调用批量添加子节点的API（假设有这个API）
      // await addCategoryChildren(rootId.value, saveData)

      // 或者如果后端API支持忽略已存在的节点，可以包含根节点但标记为更新
      const allData = []

      // 添加根节点（标记为更新，不是新增）
      const rootNode = treeData.value[0]
      allData.push({
        id: rootNode.id,
        code: rootNode.code,
        name: rootNode.name,
        parentId: null,
        ancestors: '',
        sortOrder: 1,
        rootId: rootNode.rootId,
        updateBy: 'admin',
        _operation: 'update' // 标记这是更新操作，不是新增
      })

      // 添加所有子节点
      allData.push(...saveData.map(item => ({
        ...item,
        _operation: 'create' // 标记这是新增操作
      })))

      await updateCategoryTree(rootId.value, allData)

    } else {
      // 编辑模式：保存整棵树
      const flattenTree = (nodes, result = []) => {
        nodes.forEach(node => {
          const {children, ...nodeData} = node
          result.push({
            ...nodeData,
            createBy: 'admin',
            updateBy: 'admin'
          })
          if (children && children.length > 0) {
            flattenTree(children, result)
          }
        })
        return result
      }

      const saveData = flattenTree(treeData.value)
      console.log('保存的完整数据：', saveData)

      await updateCategoryTree(rootId.value, saveData)
    }

    ElMessage.success('保存成功')
    // router.push('/manage/category/index')
  } catch (error) {
    console.error('保存失败:', error)
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

// 导出JSON
const handleExport = () => {
  const dataStr = JSON.stringify(treeData.value, null, 2)
  const dataBlob = new Blob([dataStr], {type: 'application/json'})
  const url = URL.createObjectURL(dataBlob)
  const link = document.createElement('a')
  link.href = url
  link.download = `archive_category_${rootCode.value}_${new Date().getTime()}.json`
  link.click()
  URL.revokeObjectURL(url)
  ElMessage.success('导出成功')
}
initTreeData()
onMounted(() => {
  initTreeData()
})
</script>

<style scoped>
.app-container {
  padding: 20px;
  /* 移除 height: 100vh; 让容器自然伸展 */
  min-height: calc(100vh - 40px); /* 保持最小高度，减去padding */
  display: flex;
  flex-direction: column;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header h2 {
  margin: 0;
}

.actions {
  display: flex;
  gap: 10px;
}

.designer-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: #f5f7fa;
  border-radius: 8px;
  padding: 20px;
  min-height: 400px; /* 设置最小高度确保基本可用性 */
}

.toolbar {
  margin-bottom: 20px;
}

.toolbar .el-button {
  margin-right: 10px;
}

.tree-wrapper {
  flex: 1;
  background: white;
  border-radius: 8px;
  padding: 20px;
  /* 移除 overflow: auto，让内容自然扩展 */
  min-height: 300px; /* 设置最小高度 */
}

.empty-tree {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 300px;
}

.tree-node {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 5px 0;
}

.node-content {
  display: flex;
  align-items: center;
  gap: 10px;
}

.node-code {
  font-weight: bold;
  color: #409eff;
  min-width: 60px;
}

.node-name {
  color: #303133;
}

.node-info {
  display: flex;
  gap: 5px;
}

.node-actions {
  display: flex;
  gap: 5px;
  opacity: 0;
  transition: opacity 0.3s;
}

.el-tree-node__content:hover .node-actions {
  opacity: 1;
}

:deep(.el-tree-node__content) {
  height: auto;
  padding: 8px 12px;
}

:deep(.el-tree-node__expand-icon) {
  font-size: 16px;
}

:deep(.is-dragging) {
  opacity: 0.5;
}

:deep(.el-tree--highlight-current .el-tree-node.is-current > .el-tree-node__content) {
  background-color: #f0f7ff;
}
</style>