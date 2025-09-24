<template>
  <div>
    <h3 style="text-align: center;">档案名称：【{{ archive.name }}】</h3>
    <el-table :data="archiveList">
      <el-table-column label="档号" align="center" prop="danghao" />
      <el-table-column label="射频标签号" align="center" prop="rfid" />
      <el-table-column label="保密级别" align="center" prop="secretLevel">
        <template #default="scope">
          <dict-tag :options="iams_secret_level" :value="scope.row.secretLevel" />
        </template>
      </el-table-column>
      <el-table-column label="保密期限" align="center" prop="secretPeroid">
        <template #default="scope">
          <dict-tag :options="iams_secret_period" :value="scope.row.secretPeroid" />
        </template>
      </el-table-column>
      <el-table-column label="载体类型" align="center" prop="carrierType" width="180">
        <template #default="scope">
          <dict-tag :options="iams_carrier_type" :value="scope.row.carrierType" />
        </template>
      </el-table-column>
      <el-table-column label="归档时间" align="center" prop="guidangTime" width="120">
        <template #default="scope">
          <span>{{ parseTime(scope.row.guidangTime, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="形成单位" align="center" prop="formationUnit" />
      <el-table-column label="移交单位" align="center" prop="transferUnit" />
      <el-table-column label="档案状态" align="center" prop="status">
        <template #default="scope">
          <dict-tag :options="iams_archive_status" :value="scope.row.status" />
        </template>
      </el-table-column>
      <el-table-column label="档案门类" align="center" prop="categoryId" />
      <el-table-column label="档案全宗" align="center">
        <template #default="scope">
          <span>{{ fondsName }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="150">
        <template #default="scope">
          <!-- 修改 -->
          <el-button link type="primary" @click="handleUpdate(scope.row)" v-hasPermi="['manage:archive:edit']"
            :disabled="archive.status === 'Archived'">修改</el-button>

          <!-- 挂接 -->
          <el-button link type="primary" @click="handleMount()" v-hasPermi="['manage:archive:edit']"
            :disabled="archive.status === 'Archived'">挂接</el-button>

          <!-- 查看额外信息 -->
          <el-button link type="primary" @click="showExtraInfo()">查看额外信息</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-divider />

    <el-row :gutter="20">
      <el-col :span="6">
        <el-card style="max-width: 100%;">
          <template #header>
            <div class="card-header" style="display: flex; flex-direction: column; gap: 10px;">
              <div style="display: flex; justify-content: space-between; align-items: center;">
                <el-text size="large" tag="b">档案分类</el-text>
                <el-link type="primary" size="mini" @click="showAllDocuments()">所有文档</el-link>
              </div>

              <div v-if="archiveCategoryList.length > 0" class="filter-controls">
                <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px;">
                  <el-text size="small" type="info">门类筛选</el-text>
                  <el-button size="small" type="text" @click="clearCategoryFilter" v-if="selectedCategories.length > 0"
                    style="padding: 0; height: auto;">
                    清空筛选
                  </el-button>
                </div>

                <div class="filter-stats">
                  <el-text size="small" type="info">
                    显示: {{ getCurrentPageSize() }} / {{ total }} 条
                  </el-text>
                  <el-text v-if="selectedCategories.length > 0" size="small" type="primary" style="margin-left: 10px;">
                    已选 {{ selectedCategories.length }} 个
                  </el-text>
                </div>
              </div>
            </div>
          </template>

          <div v-if="categoryTreeLoading" style="text-align: center; padding: 20px;">
            <el-icon class="is-loading">
              <Loading />
            </el-icon>
            <p style="margin-top: 10px; color: #909399;">加载分类树...</p>
          </div>

          <div v-else-if="archiveCategoryList.length > 0" class="archive-category-list">
            <div v-for="category in archiveCategoryList" :key="category.code" class="category-item"
              :class="{ 'active': selectedCategories.includes(category.code) }">

              <el-checkbox v-model="selectedCategories" :label="category.code" @change="handleCategoryChange"
                class="category-checkbox">
                <div class="category-content">
                  <div class="category-main-info">
                    <el-icon style="margin-right: 8px;">
                      <Document />
                    </el-icon>
                    <span class="category-code">{{ category.code }}</span>
                    <span class="category-name">{{ category.name }}</span>
                  </div>
                  <span class="category-count">({{ category.documentCount }})</span>
                </div>
              </el-checkbox>
            </div>
          </div>

          <el-empty v-else description="暂无分类数据" :image-size="80" />
        </el-card>
      </el-col>

      <el-col :span="18">
        <el-card>
          <template #header>
            <div class="card-header" style="display: flex; justify-content: space-between; align-items: center;">
              <el-text size="large" tag="b">文档列表</el-text>

              <!-- 搜索信息提示 -->
              <div style="display: flex; align-items: center; gap: 10px;"
                v-if="searchContext.isFromSearch && searchContext.searchText">
                <el-tag type="warning" size="small" effect="light">
                  搜索: "{{ searchContext.searchText }}"
                </el-tag>
                <el-text size="small" type="info">
                  已按匹配度排序
                </el-text>
              </div>

              <!-- 分类筛选信息提示 -->
              <div style="display: flex; align-items: center; gap: 10px;"
                v-if="selectedCategories.length > 0 && !searchContext.isFromSearch">
                <el-tag type="info" size="small" effect="light">
                  已筛选门类: {{ selectedCategories.length }} 个
                </el-tag>
              </div>

              <!-- 按钮容器 -->
              <div style="display: flex; align-items: center; gap: 10px">
                <el-button type="danger" plain icon="Delete" :disabled="multiple || archive.status === 'Archived'"
                  @click="removeDocument" v-hasPermi="['manage:document:remove']">
                  批量删除
                </el-button>
                <el-button link type="primary" @click="handleBatchUploadDialog" v-hasPermi="['manage:document:import']"
                  :disabled="archive.status === 'Archived'">批量上传文档</el-button>
              </div>
            </div>
          </template>

          <el-table v-loading="loading" :data="documentList" @selection-change="handleSelectionChange">
            <!-- 多选列 -->
            <el-table-column type="selection" width="55" align="center" />

            <el-table-column label="文件名称" align="left" prop="name" min-width="200" class-name="document-name-column">
              <template #default="{ row }">
                <div class="document-name-container">
                  <!-- 文件名称（带高亮） -->
                  <div class="document-name-content">
                    <span v-html="getHighlightedDocumentName(row)" class="document-name-text" :title="row.name"></span>
                  </div>

                  <!-- 匹配类型标识 -->
                  <div v-if="searchContext.isFromSearch && searchContext.searchText" class="document-tags-container">
                    <!-- 内容匹配标识 -->
                    <el-tag v-if="row.hasContentMatch" type="success" size="small" effect="light" title="文档内容匹配搜索关键词">
                      内容匹配
                    </el-tag>

                    <!-- 标题匹配标识 -->
                    <el-tag v-else-if="row.isSearchMatch" type="info" size="small" effect="light" title="文档标题匹配搜索关键词">
                      标题匹配
                    </el-tag>
                  </div>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="文件类型" align="center" prop="fileType" />
            <el-table-column label="文件大小(Kb)" align="center" prop="fileSize" />
            <el-table-column label="当前门类" prop="categoryName" align="center" width="300">
              <template #default="scope">
                <div v-if="getDocumentCategories(scope.row).length > 0" class="document-categories">
                  <el-tag v-for="category in getDocumentCategories(scope.row)" :key="category.code" type="info"
                    size="small" :class="{ 'category-selected': selectedCategories.includes(category.code) }"
                    style="margin: 2px;">
                    {{ category.name }}
                  </el-tag>
                </div>
                <span v-else class="text-muted">未设置</span>
              </template>
            </el-table-column>

            <!-- 操作列 -->
            <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
              <template #default="scope">
                <el-button link type="primary" @click="handleDocumentDetail(scope.row)"
                  v-hasPermi="['manage:document:query']">查看</el-button>
                <el-button link type="primary" @click="removeDocument(scope.row)"
                  v-hasPermi="['manage:document:remove', 'manage:document:edit']"
                  :disabled="archive.status === 'Archived'">删除</el-button>
                <!-- 新增：浏览日志按钮 -->
                <el-button link type="primary" @click="handleBrowseLog(scope.row)"
                           v-hasPermi="['manage:document:query']">浏览日志</el-button>
              </template>
            </el-table-column>
          </el-table>

          <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum"
            v-model:limit="queryParams.pageSize" @pagination="getDocumentList" />
        </el-card>
      </el-col>
    </el-row>

    <!-- 额外信息对话框 -->
    <el-dialog title="额外信息" v-model="extraInfoDialog" width="500px" append-to-body class="enhanced-extra-info-dialog">
      <div class="enhanced-extra-info-container">
        <el-scrollbar max-height="400px">
          <div v-for="(item, index) in extraInfo" :key="index" class="enhanced-extra-info-item"
            :class="{ 'last-item': index === extraInfo.length - 1 }">
            <div class="info-label">
              <el-tag type="info" size="small">{{ item.label }}</el-tag>
            </div>
            <div v-html="DOMPurify.sanitize(item.value)" class="info-content"></div>
          </div>
        </el-scrollbar>
      </div>

      <template #footer>
        <div class="enhanced-dialog-footer">
          <el-button @click="extraInfoDialog = false" type="primary" size="small">
            关闭
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 浏览日志弹窗 - 修复版 -->
    <el-dialog
        title="文档浏览日志"
        v-model="browseLogDialogVisible"
        width="1100px"
        append-to-body
        class="browse-log-dialog"
        @opened="handleBrowseLogDialogOpened">

<!--      &lt;!&ndash; 添加调试信息显示 &ndash;&gt;-->
<!--      <div v-if="browseLogList.length > 0" class="debug-info" style="margin-bottom: 10px; padding: 8px; background: #f5f7fa; border-radius: 4px;">-->
<!--        <el-text type="info" size="small">-->
<!--          已加载 {{ browseLogList.length }} 条记录，但表格可能未正确显示-->
<!--        </el-text>-->
<!--      </div>-->

      <el-table
          :data="browseLogList"
          style="width: 100%"
          v-loading="browseLogLoading"
          height="400px"
          stripe
          ref="browseLogTable">
        <el-table-column prop="archive_danghao" label="档案档号" align="center" width="180" />
        <el-table-column prop="document_name" label="文档名称" align="center" min-width="150" />
        <el-table-column prop="viewer_name" label="查看人" align="center" width="100" />
        <el-table-column prop="start_time" label="开始时间" align="center" width="160">
          <template #default="scope">
            {{ formatDateTime(scope.row.start_time) }}
          </template>
        </el-table-column>
        <el-table-column prop="end_time" label="结束时间" align="center" width="160">
          <template #default="scope">
            {{ formatDateTime(scope.row.end_time) }}
          </template>
        </el-table-column>
        <el-table-column prop="duration" label="查看时长" align="center" width="100">
          <template #default="scope">
            {{ formatDuration(scope.row.duration) }}
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页控件 - 使用与第二个弹窗相同的样式 -->
      <div class="pagination-container" style="margin-top: 20px; display: flex; justify-content: space-between; align-items: center;">
        <div class="pagination-info">
          共 {{ browseLogTotal }} 条记录
        </div>
        <el-pagination
            v-model:current-page="browseLogQueryParams.pageNum"
            v-model:page-size="browseLogQueryParams.pageSize"
            :page-sizes="[10, 20, 50, 100]"
            :total="browseLogTotal"
            layout="sizes, prev, pager, next, jumper"
            @size-change="getBrowseLogList"
            @current-change="getBrowseLogList"
        />
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="browseLogDialogVisible = false">关 闭</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog title="选择门类" v-model="uploadDialogVisible" width="600px" append-to-body>
      <el-form label-width="100px">
        <el-form-item label="选择门类">
          <el-select v-model="selectedCategoryCodes" multiple placeholder="请选择门类" style="width: 100%">
            <el-option v-for="category in archiveCategoryList" :key="category.code"
              :label="`${category.code} - ${category.name}`" :value="category.code">
            </el-option>
          </el-select>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="uploadDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmUpload">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, watch, computed } from 'vue'
import { useRoute } from 'vue-router';
import { ElMessageBox, ElMessage } from 'element-plus';
import { getArchive, deleteDirectoryFiles } from "@/api/manage/archive";
import { listDocument, listDocumentByIds, delDocument, getDocument } from "@/api/manage/document";
import {getCategoryByCode, getCategoryByCodeWithRoot} from "@/api/manage/treeCategory.js";
import DOMPurify from 'dompurify'
import { Folder, Document, Loading } from '@element-plus/icons-vue'
import { getFonds } from "@/api/manage/fonds";
import {listBrowseLogByDocumentId} from "@/api/manage/browseLog.js";
import dayjs from 'dayjs'
const { proxy } = getCurrentInstance();
const { iams_carrier_type, iams_archive_status, iams_retention_period, iams_secret_period, iams_secret_level } = proxy.useDict('iams_carrier_type', 'iams_archive_status', 'iams_retention_period', 'iams_secret_period', 'iams_secret_level');

const route = useRoute();

// ================= 状态管理 =================
const isFromSearch = computed(() => route.query.fromSearch === 'true')
const searchText = ref(route.query.searchText || '')
const searchField = ref(route.query.searchField || '')
const highlights = ref(route.query.highlights ? JSON.parse(route.query.highlights) : {})

const archive = ref({});
const archiveList = ref([]);

// 浏览日志相关变量
const browseLogDialogVisible = ref(false)
const browseLogList = ref([])
const browseLogLoading = ref(false)
const browseLogTotal = ref(0)
const currentDocumentId = ref(null)


const documentList = ref([]);
const loading = ref(true);
const total = ref(0);
const queryParams = ref({
  archiveId: route.query.id,
  pageNum: 1,
  pageSize: 10,
});

// 存储当前搜索的所有相关信息
const searchContext = ref({
  isFromSearch: isFromSearch.value,
  searchText: searchText.value,
  searchField: searchField.value,
  highlights: highlights.value,
  // 存储搜索的文档高亮信息
  documentHighlights: route.query.documentHighlights ? JSON.parse(route.query.documentHighlights) : {}
})

// ================= 档案分类相关状态 =================
const categoryTreeLoading = ref(false)
const archiveCategoryList = ref([]) // 档案自带的分类列表

// ================= 筛选状态 =================
const selectedCategories = ref([]) // 选中的门类编码数组

const extraInfoDialog = ref(false)
const extraInfo = ref([])

// 控制对话框显示
const uploadDialogVisible = ref(false)
const selectedCategoryCodes = ref([])

const fondsName = ref('')

// 浏览日志查询参数
const browseLogQueryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  documentId: null
})

// 处理浏览日志按钮点击
const handleBrowseLog = (row) => {
  currentDocumentId.value = row.id
  browseLogQueryParams.documentId = row.id
  browseLogQueryParams.pageNum = 1
  browseLogDialogVisible.value = true
  getBrowseLogList()
}

// 获取浏览日志列表
const getBrowseLogList = () => {
  browseLogLoading.value = true
  listBrowseLogByDocumentId(browseLogQueryParams).then(response => {
    console.log('[浏览日志] API响应:', response)

    // 确保使用响应式方式更新数据
    browseLogList.value = [...(response.rows || [])]
    browseLogTotal.value = response.total || 0
    browseLogLoading.value = false

    // 添加DOM检查
    nextTick(() => {
      console.log('[浏览日志] 表格数据绑定检查:')
      console.log('[浏览日志] browseLogList值:', browseLogList.value)
      console.log('[浏览日志] 表格DOM元素:', document.querySelector('.browse-log-dialog .el-table'))

      // 检查实际渲染的行数
      const rows = document.querySelectorAll('.browse-log-dialog .el-table__row')
      console.log('[浏览日志] 实际渲染的行数:', rows.length)

      if (rows.length === 0 && browseLogList.value.length > 0) {
        console.error('[浏览日志] 数据存在但未渲染，尝试强制重新布局')

        // 尝试强制表格重新布局
        if (proxy.$refs.browseLogTable && proxy.$refs.browseLogTable.doLayout) {
          proxy.$refs.browseLogTable.doLayout()

          // 添加延迟检查
          setTimeout(() => {
            const newRows = document.querySelectorAll('.browse-log-dialog .el-table__row')
            console.log('[浏览日志] 重新布局后的行数:', newRows.length)

            if (newRows.length === 0) {
              console.error('[浏览日志] 重新布局后仍然没有渲染行，尝试其他解决方案')
              forceTableRerender()
            }
          }, 100)
        }
      }
    })
  }).catch(error => {
    console.error('获取浏览日志失败:', error)
    browseLogLoading.value = false
    ElMessage.error('获取浏览日志失败')
  })
}

// 强制表格重新渲染
const forceTableRerender = () => {
  console.log('[浏览日志] 尝试强制重新渲染表格')

  // 方法1: 使用key变化强制重新渲染
  const temp = [...browseLogList.value]
  browseLogList.value = []
  nextTick(() => {
    browseLogList.value = temp
  })

  // 方法2: 使用Vue的forceUpdate
  nextTick(() => {
    if (proxy.$refs.browseLogTable) {
      proxy.$refs.browseLogTable.$forceUpdate()
    }
  })
}

// 格式化日期时间
const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  return dayjs(dateTime).format('YYYY-MM-DD HH:mm:ss')
}

// 格式化时长（秒转换为可读格式）
const formatDuration = (seconds) => {
  if (!seconds) return '-'

  if (seconds < 60) {
    return `${seconds}秒`
  } else if (seconds < 3600) {
    const minutes = Math.floor(seconds / 60)
    const remainingSeconds = seconds % 60
    return `${minutes}分${remainingSeconds}秒`
  } else {
    const hours = Math.floor(seconds / 3600)
    const minutes = Math.floor((seconds % 3600) / 60)
    return `${hours}小时${minutes}分`
  }
}

// 添加对话框打开事件处理
const handleBrowseLogDialogOpened = () => {
  console.log('[浏览日志] 对话框已打开，检查数据渲染状态')

  // 延迟检查，确保DOM已更新
  setTimeout(() => {
    const table = proxy.$refs.browseLogTable
    if (table) {
      console.log('[浏览日志] 表格实例:', table)
      console.log('[浏览日志] 表格数据:', table.store.states.data.value)
    }

    // 检查实际渲染的行数
    const rows = document.querySelectorAll('.browse-log-dialog .el-table__row')
    console.log('[浏览日志] 实际渲染的行数:', rows.length)

    if (browseLogList.value.length > 0 && rows.length === 0) {
      console.error('[浏览日志] 数据存在但未渲染，可能是表格配置问题')

      // 尝试强制更新表格
      if (table && table.doLayout) {
        console.log('[浏览日志] 尝试重新布局表格')
        table.doLayout()
      }
    }
  }, 100)
}

// 添加字段映射检查
const checkFieldMapping = () => {
  if (browseLogList.value && browseLogList.value.length > 0) {
    const firstRecord = browseLogList.value[0]
    console.log('[浏览日志] 字段映射检查:')

    // 检查所有表格列对应的字段是否存在
    const expectedFields = [
      'archive_danghao', 'archive_name', 'viewer_name',
      'start_time', 'end_time', 'duration', 'document_name'
    ]

    expectedFields.forEach(field => {
      console.log(`[浏览日志] 字段 ${field}:`, field in firstRecord ? '存在' : '不存在', firstRecord[field])
    })
  }
}

// 工具函数：安全高亮文本
const highlightText = (text, keyword) => {
  if (!text || !keyword) return text;
  const escapedKeyword = keyword.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
  const regex = new RegExp(escapedKeyword, 'gi');
  return text.replace(regex, match => `<span class="search-highlight">${match}</span>`);
};

/**
 * 获取文档名称的高亮版本
 * 支持处理来自ES嵌套查询的高亮信息
 */
const getHighlightedDocumentName = (document) => {
  if (!searchContext.value.isFromSearch || !searchContext.value.searchText) {
    return document.name;
  }

  // 1. 首先检查是否有来自ES的高亮信息
  if (searchContext.value.documentHighlights && searchContext.value.documentHighlights[document.id]) {
    const docHighlight = searchContext.value.documentHighlights[document.id];

    // 检查docTitle字段的高亮
    if (docHighlight['documents.docTitle'] && docHighlight['documents.docTitle'].length > 0) {
      return docHighlight['documents.docTitle'][0];
    }
    if (docHighlight['docTitle'] && docHighlight['docTitle'].length > 0) {
      return docHighlight['docTitle'][0];
    }
  }

  // 2. 检查全局高亮信息中是否有文档相关的高亮
  if (searchContext.value.documentHighlights) {
    // 遍历所有文档高亮，寻找与当前文档匹配的
    for (const [docKey, highlights] of Object.entries(searchContext.value.documentHighlights)) {
      if (highlights['docTitle'] &&
        document.name &&
        highlights['docTitle'][0].replace(/<[^>]*>/g, '').includes(document.name.substring(0, 10))) {
        return highlights['docTitle'][0];
      }
    }
  }

  // 2. 如果没有ES高亮信息，则使用本地高亮逻辑
  const searchText = searchContext.value.searchText;
  if (document.name && searchText) {
    return highlightText(document.name, searchText);
  }

  return document.name || '';
};

// 检查文档是否匹配搜索条件的方法
const isDocumentMatchingSearch = (document) => {
  if (!searchContext.value.isFromSearch || !searchContext.value.searchText) {
    return false;
  }

  const searchText = searchContext.value.searchText.toLowerCase();
  const searchField = searchContext.value.searchField;

  // 检查是否有ES高亮信息
  if (searchContext.value.documentHighlights && searchContext.value.documentHighlights[document.id]) {
    return true; // 如果有ES高亮信息，说明匹配
  }

  // 根据搜索字段检查匹配
  if (searchField === 'documents.docTitle' || searchField === 'docTitle') {
    return document.name && document.name.toLowerCase().includes(searchText);
  }

  if (searchField === 'documents.docContent' || searchField === 'docContent') {
    return true; // 假设如果搜索字段是内容，则认为匹配
  }

  // 默认检查文档名称
  return document.name && document.name.toLowerCase().includes(searchText);
};

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

    // 获取每个分类的文档数量
    await updateCategoryDocumentCounts()

  } catch (error) {
    console.error('解析档案分类失败:', error)
    ElMessage.error('加载档案分类失败')
    archiveCategoryList.value = []
  } finally {
    categoryTreeLoading.value = false
  }
}

/**
 * 更新分类下的文档数量统计（支持多门类文档）
 */
const updateCategoryDocumentCounts = async () => {
  if (!archiveCategoryList.value || archiveCategoryList.value.length === 0) {
    return
  }

  try {
    // 为每个分类查询包含该门类的文档数量
    for (const category of archiveCategoryList.value) {
      const response = await listDocument({
        archiveId: route.query.id,
        categoryCode: category.code,
        pageNum: 1,
        pageSize: 1, // 只需要总数，不需要具体数据
        countMode: true // 添加标识，告诉后端这是统计查询
      })
      category.documentCount = response.total || 0
    }

    console.log('更新后的分类统计:', archiveCategoryList.value)
  } catch (error) {
    console.error('更新分类文档数量失败:', error)
  }
}

/**
 * 处理分类复选框变化
 */
const handleCategoryChange = () => {
  console.log('分类选择发生变化:', selectedCategories.value)
  // 重置到第一页并重新查询
  queryParams.value.pageNum = 1
  getDocumentList()
}

/**
 * 显示所有文档
 */
const showAllDocuments = () => {
  selectedCategories.value = []
  queryParams.value.pageNum = 1
  getDocumentList()
}

/**
 * 清空门类筛选
 */
const clearCategoryFilter = () => {
  selectedCategories.value = []
  queryParams.value.pageNum = 1
  getDocumentList()
}

/**
 * 处理文档列表（高亮+排序）- 仅用于搜索模式
 */
const processDocumentListForSearch = (list) => {
  if (!searchContext.value.isFromSearch || !searchContext.value.searchText) {
    return list;
  }

  const searchText = searchContext.value.searchText;

  // 处理文档高亮
  const processedList = list.map(doc => {
    const processedDoc = { ...doc };

    // 检查是否匹配搜索条件
    processedDoc.isSearchMatch = isDocumentMatchingSearch(doc);

    // 检查是否有文档内容匹配（通过ES高亮信息判断）
    processedDoc.hasContentMatch = false;
    if (searchContext.value.documentHighlights && searchContext.value.documentHighlights[doc.id]) {
      const docHighlight = searchContext.value.documentHighlights[doc.id];
      processedDoc.hasContentMatch = !!(docHighlight['docContent'] && docHighlight['docContent'].length > 0);
    }

    return processedDoc;
  });

  return processedList.sort((a, b) => {
    // 文档内容匹配优先级最高
    if (a.hasContentMatch && !b.hasContentMatch) return -1;
    if (!a.hasContentMatch && b.hasContentMatch) return 1;

    // 如果都有或都没有内容匹配，则按标题匹配排序
    if (a.isSearchMatch && !b.isSearchMatch) return -1;
    if (!a.isSearchMatch && b.isSearchMatch) return 1;

    // 如果都匹配或都不匹配，按名称匹配度排序
    if (a.isSearchMatch && b.isSearchMatch) {
      const aNameMatch = (a.name || '').toLowerCase().includes(searchText.toLowerCase());
      const bNameMatch = (b.name || '').toLowerCase().includes(searchText.toLowerCase());

      if (aNameMatch && !bNameMatch) return -1;
      if (!aNameMatch && bNameMatch) return 1;
    }

    return 0;
  });
};

/**
 * 获取文档的门类信息（支持多门类）
 */
const getDocumentCategories = (document) => {
  if (!document.categoryCode) return []

  const categoryCodes = document.categoryCode.split(',').map(code => code.trim()).filter(code => code)
  const documentCategories = []

  categoryCodes.forEach(code => {
    const category = archiveCategoryList.value.find(cat => cat.code === code)
    if (category) {
      documentCategories.push(category)
    } else {
      // 如果在档案分类列表中找不到，则创建一个临时的分类对象
      documentCategories.push({
        code: code,
        name: code // 使用编码作为名称的后备方案
      })
    }
  })

  return documentCategories
}

/**
 * 获取当前页面文档数量
 */
const getCurrentPageSize = () => {
  return documentList.value.length
}

/**
 * 获取文档列表 - 重构后的统一方法
 */
function getDocumentList() {
  loading.value = true

  // 构建查询参数
  const params = {
    ...queryParams.value
  }

  // 如果有选中的分类，添加分类筛选条件
  if (selectedCategories.value.length > 0) {
    // 将选中的分类编码用逗号连接
    params.categoryCode = selectedCategories.value.join(',')
  }

  console.log('查询参数:', params)

  listDocument(params).then(response => {
    let resultList = response.rows || []

    // 如果是搜索模式，需要处理高亮和排序
    if (searchContext.value.isFromSearch) {
      resultList = processDocumentListForSearch(resultList)
    }

    documentList.value = resultList
    total.value = response.total || 0
    loading.value = false

    console.log(`文档列表加载完成: ${documentList.value.length} 条, 总计: ${total.value} 条`)
  }).catch(error => {
    console.error('获取文档列表失败:', error)
    ElMessage.error('获取文档列表失败')
    loading.value = false
  })
}

onMounted(async () => {
  const archiveId = route.query.id

  await getArchive(archiveId).then(response => {
    if (route.query.fromSearch && route.query.highlights) {
      const highlights = JSON.parse(route.query.highlights)
      if (highlights.title) response.data.title = highlights.title[0]
      if (highlights.description) response.data.description = highlights.description[0]
    }
    archive.value = response.data

    // 解析档案的分类ID字符串
    if (archive.value.categoryId) {
      // archive.value.categoryId = "B:监理文件,B0101,B0102,B0103,B0104,B0105"
      parseArchiveCategories(archive.value.categoryId)
    }

    archiveList.value.push(archive.value)
    getFondsName(archive.value.fondsId)
  })

  // 获取文档列表
  getDocumentList()
})

/** 修改档案操作 */
function handleUpdate(row) {
  proxy.$router.push({ path: "/manage/archive/arc-edit", query: { id: row.id } });
}

/** 档案挂接文档操作 */
function handleMount() {
  proxy.$router.push({ path: "/manage/archive/arc-mount", query: { id: archive.value.id } });
}

function handleDocumentDetail(row) {
  proxy.$router.push({ path: "/manage/document/doc-detail", query: { id: row.id } });
}

const ids = ref([]);
const multiple = ref(true);

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.id);
  multiple.value = !selection.length;
}

function removeDocument(row) {
  let _ids = row.id ? [row.id] : ids.value;

  proxy.$modal.confirm('是否确认删除文档信息编号为"' + _ids + '"的数据项？').then(async function () {
    try {
      delDocument(_ids).then(res => {
        if (res.code == 200) {
          proxy.$modal.msgSuccess("删除成功");
          // 删除成功后重新获取当前页数据
          getDocumentList();
          // 更新分类统计
          updateCategoryDocumentCounts();
        } else {
          proxy.$modal.msgError(res.msg);
        }
      });
    } catch (error) {
      ElMessage.error(error.message);
    }
  })
}

function showExtraInfo() {
  extraInfoDialog.value = true
  extraInfo.value = JSON.parse(archive.value.extraInfo)
}

function handleBatchUploadDialog() {
  uploadDialogVisible.value = true
}

function confirmUpload() {
  const selectedCategoryCodeStr = selectedCategoryCodes.value.join(",")
  proxy.$router.push({
    path: '/manage/document/import',
    query: {
      archiveId: route.query.id,
      categoryCode: selectedCategoryCodeStr
    }
  })
}

async function getFondsName(fondsId) {
  try {
    const response = await getFonds(fondsId)
    fondsName.value = response.data.name
  } catch (error) {
    console.error('获取全宗名称失败:', error)
    return '未知全宗'
  }
}
</script>

<style scoped>
.search-highlight {
  background-color: yellow;
  color: red;
  font-weight: bold;
  padding: 0 2px;
  border-radius: 2px;
}

.highlight {
  background-color: yellow;
  color: red;
  font-weight: bold;
  padding: 0 2px;
  border-radius: 2px;
}

.filter-controls {
  padding: 8px 12px;
  background-color: #f8f9fa;
  border-radius: 4px;
  border: 1px solid #e9ecef;
}

.filter-stats {
  font-size: 12px;
  color: #6c757d;
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

.category-item:hover {
  background-color: #f5f7fa;
  border-color: #e4e7ed;
}

.category-item.active {
  background-color: #ecf5ff;
  border-color: #409EFF;
}

.category-checkbox {
  width: 100%;
}

.category-checkbox :deep(.el-checkbox__label) {
  width: 100%;
  padding-left: 8px;
}

.category-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  flex-wrap: nowrap;
  /* 防止换行 */
}

.category-main-info {
  display: flex;
  align-items: center;
  flex: 1;
  min-width: 0;
  /* 允许内容缩减 */
}

.category-name {
  max-width: 120px;
  /* 减小最大宽度 */
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  /* 强制单行显示 */
  margin-left: 5px;
}

.category-code {
  color: #409EFF;
  margin-right: 8px;
  font-weight: 500;
  min-width: 50px;
  /* 减小最小宽度 */
  flex-shrink: 0;
  /* 防止缩减 */
}

.category-count {
  color: #909399;
  font-size: 12px;
  flex-shrink: 0;
  /* 防止数量被挤压 */
  margin-left: auto;
  /* 推到最右边 */
}

.category-item.active .category-name,
.category-item.active .category-count,
.category-item.active .category-code {
  color: #409EFF;
}

.enhanced-extra-info-dialog {
  border-radius: 8px;
}

.enhanced-extra-info-container {
  padding: 0 10px;
}

.enhanced-extra-info-item {
  margin-bottom: 16px;
  padding-bottom: 16px;
  border-bottom: 1px solid #eee;
}

.enhanced-extra-info-item.last-item {
  border-bottom: none;
  margin-bottom: 0;
  padding-bottom: 0;
}

.info-label {
  margin-bottom: 8px;
}

.info-content {
  padding-left: 8px;
  line-height: 1.6;
  color: #333;
}

.enhanced-dialog-footer {
  text-align: center;
  padding-top: 10px;
}

.category-selected {
  background-color: #409EFF !important;
  color: white !important;
  border-color: #409EFF !important;
}

/* 文档门类显示样式 */
.document-categories {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  align-items: center;
  justify-content: center;
}

.document-categories .el-tag {
  margin: 0 !important;
  /* 覆盖内联样式 */
}

/* 文档名称列的容器样式 */
.document-name-container {
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 8px 4px;
  min-height: 40px;
}

/* 文档名称内容区域 */
.document-name-content {
  flex: 1;
  line-height: 1.4;
}

/* 文档名称文本样式 */
.document-name-text {
  word-break: break-word;
  white-space: normal;
  display: block;
  font-size: 14px;
  color: #303133;
}

/* 标签容器样式 */
.document-tags-container {
  display: flex;
  gap: 4px;
  flex-wrap: wrap;
  align-items: center;
}

/* 表格行自适应高度 */
:deep(.document-name-column .cell) {
  padding: 4px 8px !important;
  line-height: normal !important;
  white-space: normal !important;
  word-break: break-word !important;
}

/* 长内容优化 */
.document-name-text {
  max-height: 60px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  line-clamp: 3;
  -webkit-box-orient: vertical;
}

/* 鼠标悬停时显示完整内容 */
.document-name-container:hover .document-name-text {
  max-height: none;
  -webkit-line-clamp: unset;
  line-clamp: unset;
  overflow: visible;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .document-name-container {
    gap: 4px;
    padding: 6px 2px;
  }

  .document-name-text {
    font-size: 13px;
  }

  .document-tags-container .el-tag {
    font-size: 10px;
    height: 16px;
    line-height: 14px;
  }
}
   /* 确保表格可见性 */
 .browse-log-dialog :deep(.el-table) {
   visibility: visible !important;
   opacity: 1 !important;
 }

.browse-log-dialog :deep(.el-table__body-wrapper) {
  display: block !important;
  height: auto !important;
}

.browse-log-dialog :deep(.el-table__body) {
  display: table-row-group !important;
}

.browse-log-dialog :deep(.el-table__row) {
  display: table-row !important;
  height: auto !important;
  opacity: 1 !important;
}

/* 分页容器样式 */
.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.pagination-info {
  font-size: 14px;
  color: #606266;
}

/* 调试信息样式 */
.debug-info {
  margin-bottom: 10px;
  padding: 8px;
  background: #f5f7fa;
  border-radius: 4px;
  border: 1px solid #e4e7ed;
}
</style>