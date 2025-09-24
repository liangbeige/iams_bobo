<template>
  <div class="app-container">
    <el-form
        :model="queryParams"
        ref="queryRef"
        :inline="true"
        v-show="showSearch"
        class="stable-search-form"
        label-width="80px">  <!-- 固定标签宽度 -->

      <!-- 全宗编号 -->
      <el-form-item label="全宗编号" prop="bianhao">
        <el-input
            v-model="queryParams.bianhao"
            placeholder="请输入全宗编号"
            clearable
            style="width: 180px"
        @keyup.enter="handleQuery"
        />
      </el-form-item>

      <!-- 全宗名称 -->
      <el-form-item label="全宗名称" prop="name">
        <el-input
            v-model="queryParams.name"
            placeholder="请输入全宗名称"
            clearable
            style="width: 180px"
            @keyup.enter="handleQuery"
        />
      </el-form-item>

      <!-- 保管期限 -->
      <el-form-item label="保管期限" prop="retentionPeriod">
        <el-select
            v-model="queryParams.retentionPeriod"
            placeholder="请选择保管期限"
            clearable
            style="width: 180px">
          <el-option
              v-for="dict in iams_retention_period"
              :key="dict.value"
              :label="dict.label"
              :value="dict.value"
          />
        </el-select>
      </el-form-item>

      <!-- 全宗状态 -->
      <el-form-item label="全宗状态" prop="status">
        <el-select
            v-model="queryParams.status"
            placeholder="请选择全宗状态"
            clearable
            style="width: 180px">
          <el-option
              v-for="dict in iams_fonds_state"
              :key="dict.value"
              :label="dict.label"
              :value="dict.value"
          />
        </el-select>
      </el-form-item>

      <!-- 操作按钮 -->
      <el-form-item class="search-buttons">
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
            type="primary"
            plain
            icon="Plus"
            @click="handleAdd"
            v-hasPermi="['manage:fonds:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
            type="success"
            plain
            icon="Edit"
            :disabled="single"
            @click="handleUpdate"
            v-hasPermi="['manage:fonds:edit']"
        >修改</el-button>
      </el-col>

      <el-col :span="1.5">
        <el-button
            type="danger"
            plain
            icon="Delete"
            :disabled="multiple"
            @click="handleDelete"
            v-hasPermi="['manage:fonds:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
            type="warning"
            plain
            icon="Download"
            @click="handleExport"
            v-hasPermi="['manage:fonds:export']"
        >导出</el-button>
      </el-col>

      <el-col :span="1.5">
        <el-button
            icon="Upload"
            @click="handleImport"
        >导入</el-button>
      </el-col>

      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>

    </el-row>

    <el-table v-loading="loading" :data="fondsList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
<!--      <el-table-column label="全宗ID" align="center" prop="id" />-->
      <el-table-column label="全宗编号" align="center" prop="bianhao" />
      <el-table-column label="全宗名称" align="center" prop="name" />
      <el-table-column label="起始时间" align="center" prop="startDate" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.startDate, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="档案数量" align="center" prop="archiveCount" />
      <el-table-column label="形成单位" align="center" prop="formationUnit" />
      <el-table-column label="保管期限" align="center" prop="retentionPeriod">
        <template #default="scope">
          <dict-tag :options="iams_retention_period" :value="scope.row.retentionPeriod"/>
        </template>
      </el-table-column>
      <el-table-column label="全宗状态" align="center" prop="status">
        <template #default="scope">
          <dict-tag :options="iams_fonds_state" :value="scope.row.status"/>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['manage:fonds:edit']">修改</el-button>
          <el-button
              link
              type="success"
              icon="Setting"
              @click="handleCategoryConfig(scope.row)"
              v-hasPermi="['manage:fonds:config']"
          >配置门类</el-button>

          <el-button
              link
              type="info"
              icon="View"
              @click="handleViewTemplate(scope.row)"
              v-hasPermi="['manage:fonds:query']"
          >查看模板</el-button>
          <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['manage:fonds:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total>0"
      :total="total"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 添加或修改全宗管理对话框 -->
    <el-dialog :title="title" v-model="open" width="500px" append-to-body>
      <el-form ref="fondsRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="全宗编号" prop="bianhao">
          <el-input v-model="form.bianhao" placeholder="请输入全宗编号" />
        </el-form-item>
        <el-form-item label="全宗名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入全宗名称" />
        </el-form-item>
        <el-form-item label="起始时间" prop="startDate">
          <el-date-picker clearable
            v-model="form.startDate"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="请选择起始时间">
          </el-date-picker>
        </el-form-item>
        <el-form-item label="全宗简介" prop="description">
          <el-input v-model="form.description" type="textarea" placeholder="请输入内容" />
        </el-form-item>
        <el-form-item label="形成单位" prop="formationUnit">
          <el-input v-model="form.formationUnit" placeholder="请输入形成单位" />
        </el-form-item>
        <el-form-item label="移交单位" prop="transferUnit">
          <el-input v-model="form.transferUnit" placeholder="请输入移交单位" />
        </el-form-item>
        <el-form-item label="档案数量" prop="archiveCount">
          <el-input v-model="form.archiveCount" placeholder="档案数量" :disabled="true" />
        </el-form-item>
        <el-form-item label="保管期限" prop="retentionPeriod">
          <el-select v-model="form.retentionPeriod" placeholder="请选择保管期限">
            <el-option
              v-for="dict in iams_retention_period"
              :key="dict.value"
              :label="dict.label"
              :value="dict.value"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="全宗状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio
              v-for="dict in iams_fonds_state"
              :key="dict.value"
              :label="dict.value"
            >{{dict.label}}</el-radio>
          </el-radio-group>
        </el-form-item>
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

    <!-- 用户导入对话框 -->
    <el-dialog :title="upload.title" v-model="upload.open" width="400px" append-to-body>
      <el-upload
          ref="uploadRef"
          :limit="1"
          accept=".xlsx, .xls"
          :headers="upload.headers"
          :action="upload.url + '?updateSupport=' + upload.updateSupport"
          :disabled="upload.isUploading"
          :on-progress="handleFileUploadProgress"
          :on-success="handleFileSuccess"
          :auto-upload="false"
          drag
      >
        <el-icon class="el-icon--upload"><upload-filled /></el-icon>
        <div class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div>
        <template #tip>
          <div class="el-upload__tip text-center">
            <div class="el-upload__tip">
              <el-checkbox v-model="upload.updateSupport" />是否更新已经存在的全宗数据
            </div>
            <span>仅允许导入xls、xlsx格式文件。</span>
            <el-link type="primary" :underline="false" style="font-size:12px;vertical-align: baseline;" @click="importTemplate">下载模板</el-link>
          </div>
        </template>
      </el-upload>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitFileForm">确 定</el-button>
          <el-button @click="upload.open = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>
    <!-- 门类配置对话框 -->
    <el-dialog
        :title="`${currentFonds.name || ''} - 门类配置`"
        v-model="categoryConfigVisible"
        width="600px"
        append-to-body
    >
      <div class="config-header">
        <div class="fonds-info">
          <el-tag type="primary">{{ currentFonds.bianhao || '' }}</el-tag>
          <span class="fonds-name">{{ currentFonds.name || '' }}</span>
        </div>
        <el-alert
            message="请选择该全宗适用的门类，用户添加档案时将从这些门类中选择"
            type="info"
            :closable="false"
            show-icon
        />
      </div>

      <div class="category-selection" v-loading="loadingCategories">
        <h4>选择门类大类：</h4>
        <div class="category-grid">
          <div
              v-for="category in allCategories"
              :key="category.id"
              class="category-card"
              :class="{ 'selected': selectedCategoryIds.includes(category.id) }"
              @click="toggleCategorySelection(category.id)"
          >
            <div class="category-content">
              <div class="category-code">{{ category.code }}</div>
              <div class="category-name">{{ category.name }}</div>
            </div>
            <div class="category-check">
              <el-icon v-if="selectedCategoryIds.includes(category.id)" color="#67c23a">
                <Check />
              </el-icon>
            </div>
          </div>
        </div>

        <div v-if="selectedCategoryIds.length > 0" class="selected-summary">
          <p>已选择 {{ selectedCategoryIds.length }} 个门类：</p>
          <div class="selected-tags">
            <el-tag
                v-for="categoryId in selectedCategoryIds"
                :key="categoryId"
                type="success"
                closable
                @close="removeCategorySelection(categoryId)"
            >
              {{ getCategoryName(categoryId) }}
            </el-tag>
          </div>
        </div>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="categoryConfigVisible = false">取 消</el-button>
          <el-button
              type="primary"
              @click="saveCategoryConfig"
              :loading="savingConfig"
              :disabled="selectedCategoryIds.length === 0"
          >
            确 定
          </el-button>
        </div>
      </template>
    </el-dialog>


    <!-- 查看门类模板对话框 -->
    <el-dialog
        :title="`${currentFonds.name || ''} - 门类模板`"
        v-model="templateViewVisible"
        width="500px"
        append-to-body
    >
      <div class="template-header">
        <div class="fonds-info">
          <el-tag type="primary">{{ currentFonds.bianhao || '' }}</el-tag>
          <span class="fonds-name">{{ currentFonds.name || '' }}</span>
        </div>
      </div>

      <div class="template-content" v-loading="loadingTemplate">
        <div v-if="currentTemplate.length === 0" class="no-template">
          <el-empty description="该全宗暂未配置门类模板">
            <el-button type="primary" @click="showConfigFromView">
              立即配置
            </el-button>
          </el-empty>
        </div>
        <div v-else class="template-list">
          <h4>已配置的门类：</h4>
          <div class="template-items">
            <div
                v-for="(category, index) in currentTemplate"
                :key="category.id"
                class="template-item"
            >
              <div class="item-index">{{ index + 1 }}</div>
              <div class="item-content">
                <div class="item-code">{{ category.code }}</div>
                <div class="item-name">{{ category.name }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="templateViewVisible = false">关 闭</el-button>
          <el-button
              v-if="currentTemplate.length > 0"
              type="primary"
              @click="showConfigFromView"
          >
            重新配置
          </el-button>
        </div>
      </template>
    </el-dialog>

  </div>
</template>

<script setup name="Fonds">
import {listFonds, getFonds, delFonds, addFonds, updateFonds, UpdateCategoryCodesToCodeName} from "@/api/manage/fonds";
import { getCategoryRoots } from "@/api/manage/treeCategory.js"; // 使用你现有的API方法
import {getToken} from "@/utils/auth.js";
// import CategoryConfigDialog from "./components/CategoryConfigDialog.vue";
import { Check } from '@element-plus/icons-vue';
import { getFondsCategories, saveFondsCategories } from "@/api/manage/category";

const { proxy } = getCurrentInstance();
const { iams_fonds_state, iams_retention_period } = proxy.useDict('iams_fonds_state', 'iams_retention_period');

const fondsList = ref([]);
const open = ref(false);
const loading = ref(true);
const showSearch = ref(true);
const ids = ref([]);
const single = ref(true);
const multiple = ref(true);
const total = ref(0);
const title = ref("");

// 门类配置相关 - 重要：初始化所有响应式变量
const categoryConfigVisible = ref(false);
const templateViewVisible = ref(false);
const currentFonds = ref({
  id: '',
  bianhao: '',
  name: '',
  formationUnit: ''
});
const allCategories = ref([]);
const selectedCategoryIds = ref([]);
const currentTemplate = ref([]);
const loadingCategories = ref(false);
const loadingTemplate = ref(false);
const savingConfig = ref(false);




const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    bianhao: null,
    name: null,
    retentionPeriod: null,
    status: null,
  },
  rules: {
    bianhao: [
      { required: true, message: "全宗编号不能为空", trigger: "blur" }
    ],
    name: [
      { required: true, message: "全宗名称不能为空", trigger: "blur" }
    ],
  }
});

const { queryParams, form, rules } = toRefs(data);

/** 查询全宗列表 */
function getList() {
  loading.value = true;
  listFonds(queryParams.value).then(response => {
    fondsList.value = response.rows;
    total.value = response.total;
    loading.value = false;
  });
}

// 取消按钮
function cancel() {
  open.value = false;
  reset();
}

// 表单重置
function reset() {
  form.value = {
    id: null,
    bianhao: null,
    name: null,
    startDate: null,
    endDate: null,
    description: null,
    formationUnit: null,
    transferUnit: null,
    retentionPeriod: null,
    archiveCount: null,
    status: null,
    createTime: null,
    createBy: null,
    updateTime: null,
    updateBy: null,
    remark: null
  };
  proxy.resetForm("fondsRef");
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
function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.id);
  single.value = selection.length != 1;
  multiple.value = !selection.length;
}

/** 新增按钮操作 */
function handleAdd() {
  reset();
  open.value = true;
  title.value = "添加全宗管理";
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset();
  const _id = row.id || ids.value
  getFonds(_id).then(response => {
    form.value = response.data;
    open.value = true;
    title.value = "修改全宗管理";
  });
}

/** 门类配置按钮操作 - 修改版 */
function handleCategoryConfig(row) {
  console.log('门类配置按钮点击', row);

  // 设置当前全宗信息
  currentFonds.value = {
    id: row.id,
    bianhao: row.bianhao,
    name: row.name,
    formationUnit: row.formationUnit,
    categoryCodes: row.categoryCodes // 保存原始的 categoryCodes
  };

  // 关闭其他对话框
  templateViewVisible.value = false;

  // 加载所有门类
  loadAllCategories();

  // 解析当前全宗已配置的门类
  const configuredCategories = parseCategoryCodes(row.categoryCodes);
  currentTemplate.value = configuredCategories;

  // 设置选中的门类ID（基于code匹配）
  selectedCategoryIds.value = [];
  configuredCategories.forEach(config => {
    const matchedCategory = allCategories.value.find(cat => cat.code === config.code);
    if (matchedCategory) {
      selectedCategoryIds.value.push(matchedCategory.id);
    }
  });

  // 显示配置对话框
  categoryConfigVisible.value = true;
}


/** 查看模板按钮操作 - 修改版 */
function handleViewTemplate(row) {
  console.log('查看模板按钮点击', row);

  // 设置当前全宗信息
  currentFonds.value = {
    id: row.id,
    bianhao: row.bianhao,
    name: row.name,
    formationUnit: row.formationUnit,
    categoryCodes: row.categoryCodes
  };

  // 关闭其他对话框
  categoryConfigVisible.value = false;

  // 直接解析 categoryCodes 字段
  const template = parseCategoryCodes(row.categoryCodes);
  currentTemplate.value = template;

  console.log('解析的模板数据:', template);

  // 显示查看模板对话框
  templateViewVisible.value = true;
}

/** 从查看页面跳转到配置页面 - 修改版 */
function showConfigFromView() {
  templateViewVisible.value = false;
  loadAllCategories();

  // 设置已选中的门类
  const configuredCategories = currentTemplate.value;
  selectedCategoryIds.value = [];

  // 等待 allCategories 加载完成后设置选中状态
  nextTick(() => {
    configuredCategories.forEach(config => {
      const matchedCategory = allCategories.value.find(cat => cat.code === config.code);
      if (matchedCategory) {
        selectedCategoryIds.value.push(matchedCategory.id);
      }
    });
  });

  categoryConfigVisible.value = true;
}

/** 加载所有门类 */
async function loadAllCategories() {
  loadingCategories.value = true;
  try {
    // 调用你现有的获取根节点API，传入空的查询条件获取所有根节点
    const response = await getCategoryRoots({});
    allCategories.value = response.data || [];
    console.log('加载门类根节点成功:', allCategories.value);
  } catch (error) {
    console.error('加载门类失败:', error);
    proxy.$modal.msgError('加载门类失败');
    allCategories.value = [];
  } finally {
    loadingCategories.value = false;
  }
}

/** 加载当前全宗模板 */
async function loadCurrentTemplate(fondsId) {
  loadingTemplate.value = true;
  try {
    console.log('开始加载全宗模板，fondsId:', fondsId);
    const response = await getFondsCategories(fondsId);
    console.log('API响应:', response);

    const template = response.data || [];
    console.log('解析后的模板数据:', template);

    currentTemplate.value = [...template];
    selectedCategoryIds.value = template.map(item => item.id);

    console.log('加载模板完成', {
      fondsId,
      template,
      selectedIds: selectedCategoryIds.value,
      currentTemplate: currentTemplate.value
    });
  } catch (error) {
    console.error('加载门类模板失败:', error);
    // 如果失败，初始化为空
    currentTemplate.value = [];
    selectedCategoryIds.value = [];
  } finally {
    loadingTemplate.value = false;
  }
}

function parseCategoryCodes(categoryCodes) {
  if (!categoryCodes) return [];

  // 移除末尾的逗号（如果有）
  const cleanedCodes = categoryCodes.replace(/,$/, '');

  // 按逗号分割
  const items = cleanedCodes.split(',').filter(item => item.trim());

  // 解析每个项
  return items.map((item, index) => {
    const [code, name] = item.split(':').map(s => s.trim());
    return {
      id: `${code}_${index}`, // 生成唯一ID
      code: code || '',
      name: name || ''
    };
  });
}

/** 将选中的门类转换为 categoryCodes 格式 */
function formatCategoryCodes(selectedCategories) {
  if (!selectedCategories || selectedCategories.length === 0) return '';

  return selectedCategories.map(cat => `${cat.code}:${cat.name}`).join(',') + ',';
}


/** 切换门类选择 */
function toggleCategorySelection(categoryId) {
  const index = selectedCategoryIds.value.indexOf(categoryId);
  if (index > -1) {
    selectedCategoryIds.value.splice(index, 1);
  } else {
    selectedCategoryIds.value.push(categoryId);
  }
  console.log('选择状态切换', { categoryId, selectedIds: selectedCategoryIds.value });
}

/** 移除门类选择 */
function removeCategorySelection(categoryId) {
  const index = selectedCategoryIds.value.indexOf(categoryId);
  if (index > -1) {
    selectedCategoryIds.value.splice(index, 1);
  }
}

/** 获取门类名称 */
function getCategoryName(categoryId) {
  const category = allCategories.value.find(cat => cat.id === categoryId);
  return category ? `${category.code} - ${category.name}` : '';
}

async function saveCategoryConfig() {
  if (selectedCategoryIds.value.length === 0) {
    proxy.$modal.msgWarning('请至少选择一个门类');
    return;
  }

  savingConfig.value = true;

  try {
    // 获取选中的门类对象
    const selectedCategories = selectedCategoryIds.value.map(categoryId => {
      return allCategories.value.find(cat => cat.id === categoryId);
    }).filter(cat => cat !== null);

    // 格式化为 code:name 格式
    const categoryCodes = formatCategoryCodes(selectedCategories);

    console.log('保存的 categoryCodes:', categoryCodes);

    // 构建更新的表单数据
    const updateData = {
      id: currentFonds.value.id,
      categoryCodes: categoryCodes
    };

    // 调用更新全宗的API（需要后端支持部分更新）
    await updateFonds(updateData);

    proxy.$modal.msgSuccess('门类配置保存成功');
    categoryConfigVisible.value = false;

    // 刷新列表
    getList();

    // 重置数据
    selectedCategoryIds.value = [];
  } catch (error) {
    console.error('保存门类配置失败:', error);
    proxy.$modal.msgError('保存失败：' + (error.message || '未知错误'));
  } finally {
    savingConfig.value = false;
  }
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["fondsRef"].validate(valid => {
    if (valid) {
      if (form.value.id != null) {
        updateFonds(form.value).then(response => {
          proxy.$modal.msgSuccess("修改成功");
          open.value = false;
          getList();
        });
      } else {
        addFonds(form.value).then(response => {
          proxy.$modal.msgSuccess("新增成功");
          open.value = false;
          getList();
        });
      }
    }
  });
}


/** 删除按钮操作 */
/** 删除按钮操作 */
function handleDelete(row) {
  const _ids = row.id || ids.value;

  // 检查是否可以删除
  if (!canDeleteFonds(row)) {
    return;
  }

  proxy.$modal.confirm('是否确认删除全宗管理编号为"' + _ids + '"的数据项？').then(function() {
    return delFonds(_ids);
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess("删除成功");
  }).catch(() => {});
}

/** 检查全宗是否可以删除 */
function canDeleteFonds(row) {
  // 单条删除检查
  if (row.id) {
    if (row.archiveCount > 0) {
      proxy.$modal.msgError("该全宗下存在项目/档案，不能删除");
      return false;
    }
    return true;
  }

  // 批量删除检查
  if (ids.value.length > 0) {
    const hasNonZero = fondsList.value.some(item =>
        ids.value.includes(item.id) && item.archiveCount > 0
    );

    if (hasNonZero) {
      proxy.$modal.msgError("选中的全宗中包含有项目/档案的记录，不能删除");
      return false;
    }
  }

  return true;
}

/** 导出按钮操作 */
function handleExport() {
  proxy.download('manage/fonds/export', {
    ...queryParams.value
  }, `fonds_${new Date().getTime()}.xlsx`)
}

/*** 用户导入参数 */
const upload = reactive({
  // 是否显示弹出层（用户导入）
  open: false,
  // 弹出层标题（用户导入）
  title: "",
  // 是否禁用上传
  isUploading: false,
  // 是否更新已经存在的用户数据
  updateSupport: 0,
  // 设置上传的请求头部
  headers: { Authorization: "Bearer " + getToken() },
  // 上传的地址
  url: import.meta.env.VITE_APP_BASE_API + "/manage/fonds/importDate"
});


/*** 用户导入 */
function handleImport(){
  upload.open = true;
  upload.title = "全宗信息导入";
}

/** 下载模板操作 */
function importTemplate() {
  proxy.download("manage/fonds/importTemplate", {
  }, `fonds_template_${new Date().getTime()}.xlsx`);
}
/**文件上传中处理 */
const handleFileUploadProgress = (event, file, fileList) => {
  upload.isUploading = true;
}

/**文件上传成功处理 */
const handleFileSuccess = (response, file, fileList) => {
  upload.open = false;
  upload.isUploading = false;
  proxy.$refs["uploadRef"].handleRemove(file);
  proxy.$alert("<div style='overflow: auto;overflow-x: hidden;max-height: 70vh;padding: 10px 20px 0;'>" + response.msg + "</div>", "导入结果", { dangerouslyUseHTMLString: true });
  getList();
}

/** 提交上传文件 */
function submitFileForm() {
  proxy.$refs["uploadRef"].submit();
}

getList();
</script>

<style scoped>
/* 门类配置样式 */
.config-header {
  margin-bottom: 20px;
}

.fonds-info {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 15px;
  padding: 15px;
  background: #f5f7fa;
  border-radius: 8px;
}

.fonds-name {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}

.category-selection h4 {
  margin-bottom: 15px;
  color: #303133;
}

.category-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 12px;
  margin-bottom: 20px;
}

.category-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px;
  border: 2px solid #e4e7ed;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
}

.category-card:hover {
  border-color: #c0c4cc;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.category-card.selected {
  border-color: #409eff;
  background: #e1f3ff;
}

.category-content {
  flex: 1;
}

.category-code {
  font-weight: bold;
  color: #409eff;
  font-size: 16px;
  margin-bottom: 4px;
}

.category-name {
  color: #606266;
  font-size: 14px;
}

.category-check {
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.selected-summary {
  padding: 15px;
  background: #f0f9ff;
  border-radius: 8px;
  border: 1px solid #b3d8ff;
}

.selected-summary p {
  margin: 0 0 10px 0;
  color: #303133;
  font-weight: 500;
}

.selected-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

/* 模板查看样式 */
.template-header {
  margin-bottom: 20px;
}

.template-content h4 {
  margin-bottom: 15px;
  color: #303133;
}

.template-items {
  max-height: 300px;
  overflow-y: auto;
}

.template-item {
  display: flex;
  align-items: center;
  padding: 12px;
  margin-bottom: 8px;
  background: #f8f9fa;
  border-radius: 6px;
  border-left: 4px solid #409eff;
}

.item-index {
  width: 30px;
  height: 30px;
  background: #409eff;
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  margin-right: 15px;
}

.item-content {
  flex: 1;
}

.item-code {
  font-weight: bold;
  color: #409eff;
  font-size: 16px;
  margin-bottom: 2px;
}

.item-name {
  color: #606266;
  font-size: 14px;
}

.no-template {
  text-align: center;
  padding: 40px 20px;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .category-grid {
    grid-template-columns: 1fr;
  }

  .selected-tags {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>