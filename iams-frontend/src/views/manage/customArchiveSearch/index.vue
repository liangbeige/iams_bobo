<template>
  <div>
    <el-form ref="formRef" :model="formData" :rules="rules" size="default" label-width="0px" label-position="left"
      :style="{ marginTop: '20px' }">
      <!-- 第一行: 下拉框 + 输入框 + 按钮 -->
      <el-row gutter="13" justify="center">
        <el-col :span="2">
          <el-form-item label-width="-1px" label="" prop="MainTypeLogic">
            <el-select v-model="formData.MainTypeLogic" placeholder="AND" :style="{ width: '100%' }">
              <el-option v-for="(item, index) in LogicOptions" :key="index" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="15">
          <el-form-item label-width="-1px" label="" prop="MainTypeValue">
            <el-input v-model="formData.MainTypeValue" placeholder="请输入" clearable class="input-with-select">
              <template #prepend>
                <el-select v-model="formData.MainType" placeholder="关键词" style="width: 100px">
                  <el-option v-for="(item, index) in MainTypeOptions" :key="index" :label="item.label"
                    :value="item.value" />
                </el-select>
              </template>
            </el-input>
          </el-form-item>
        </el-col>
        <el-col :span="4">
          <el-form-item label-width="-2px" label="">
            <el-button @click="addRow">
              <el-icon>
                <Plus />
              </el-icon>
            </el-button>
          </el-form-item>
        </el-col>
      </el-row>

      <!--      &lt;!&ndash; 第二行: 下拉框 + 输入框 + 按钮 &ndash;&gt;-->
      <!--      <el-row gutter="13" justify="center">-->
      <!--        <el-col :span="2">-->
      <!--          <el-form-item label-width="-1px" label="" prop="secondMainTypeLogic">-->
      <!--            <el-select-->
      <!--                v-model="formData.secondMainTypeLogic"-->
      <!--                placeholder="AND"-->
      <!--                :style="{width: '100%'}"-->
      <!--            >-->
      <!--              <el-option-->
      <!--                  v-for="(item, index) in LogicOptions"-->
      <!--                  :key="index"-->
      <!--                  :label="item.label"-->
      <!--                  :value="item.value"-->
      <!--              />-->
      <!--            </el-select>-->
      <!--          </el-form-item>-->
      <!--        </el-col>-->
      <!--        <el-col :span="15">-->
      <!--          <el-form-item label-width="-1px" label="" prop="secondMainTypeValue">-->
      <!--            <el-input-->
      <!--                v-model="formData.secondMainTypeValue"-->
      <!--                placeholder="请输入"-->
      <!--                clearable-->
      <!--                class="input-with-select"-->
      <!--            >-->
      <!--              <template #prepend>-->
      <!--                <el-select-->
      <!--                    v-model="formData.secondMainType"-->
      <!--                    placeholder="关键词"-->
      <!--                    style="width: 100px"-->
      <!--                >-->
      <!--                  <el-option-->
      <!--                      v-for="(item, index) in SecondTypeOptions"-->
      <!--                      :key="index"-->
      <!--                      :label="item.label"-->
      <!--                      :value="item.value"-->
      <!--                      :disabled="item.disabled"-->
      <!--                  />-->
      <!--                </el-select>-->
      <!--              </template>-->
      <!--            </el-input>-->
      <!--          </el-form-item>-->
      <!--        </el-col>-->
      <!--        <el-col :span="4">-->
      <!--          <el-form-item label-width="-2px" label="">-->
      <!--            <el-button @click="addRow">-->
      <!--              <el-icon>-->
      <!--                <Plus/>-->
      <!--              </el-icon>-->
      <!--            </el-button>-->
      <!--            <el-button @click="removeRow">-->
      <!--              <el-icon>-->
      <!--                <Minus/>-->
      <!--              </el-icon>-->
      <!--            </el-button>-->
      <!--          </el-form-item>-->
      <!--        </el-col>-->
      <!--      </el-row>-->

      <!-- 动态条件行 -->
      <el-row v-for="(row, index) in dynamicRows" :key="index" gutter="13" justify="center">
        <el-col :span="2">
          <el-form-item label-width="-1px" label="" :prop="`dynamicRows[${index}].logic`">
            <el-select v-model="row.logic" placeholder="AND" :style="{ width: '100%' }">
              <el-option v-for="(item, optIndex) in LogicOptions" :key="optIndex" :label="item.label"
                :value="item.value" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="15">
          <el-form-item label-width="-2px" label="" :prop="`dynamicRows[${index}].value`">
            <el-input v-model="row.value" placeholder="请输入" clearable class="input-with-select">
              <template #prepend>
                <el-select v-model="row.field" placeholder="关键词" style="width: 100px" clearable>
                  <el-option v-for="(item, optIndex) in MainTypeOptions" :key="optIndex" :label="item.label"
                    :value="item.value" />
                </el-select>
              </template>
            </el-input>
          </el-form-item>
        </el-col>
        <el-col :span="4">
          <el-form-item label-width="-2px" label="">
            <el-button @click="addRow">
              <el-icon>
                <Plus />
              </el-icon>
            </el-button>
            <el-button @click="removeDynamicRow(index)">
              <el-icon>
                <Minus />
              </el-icon>
            </el-button>
          </el-form-item>
        </el-col>
      </el-row>

      <!-- 第二行: 多选框 -->
      <el-row justify="center">
        <el-form-item label="" prop="DocumentType">
          <el-checkbox-group v-model="formData.DocumentType" size="default">
            <el-checkbox v-for="(item, index) in DocumentTypeOptions" :key="index" :label="item.value"
              :disabled="item.disabled">{{ item.label }}
            </el-checkbox>
          </el-checkbox-group>
        </el-form-item>
      </el-row>

      <!-- 提交按钮 -->
      <el-row gutter="20" justify="center">
        <el-form-item label-width="3px" label="">
          <el-button type="primary" icon="Search" size="default" @click="submitForm"> 搜索</el-button>
        </el-form-item>
        <el-form-item label-width="3px" label="">
          <el-button type="primary" icon="Refresh" size="default" @click="resetQuery"> 重置</el-button>
        </el-form-item>
      </el-row>
    </el-form>

    <el-table v-loading="loading" :data="SearchList">
      <el-table-column label="档号" align="center" prop="mysqlDanghao">
        <template #default="{ row }">
          <span v-if="row.highlights && row.highlights.mysqlDanghao" v-html="row.highlights.mysqlDanghao[0]"></span>
          <span v-else>{{ row.mysqlDanghao }}</span>
        </template>
      </el-table-column>
      <el-table-column label="标题" align="center" prop="title">
        <template #default="{ row }">
          <span v-if="row.highlights && row.highlights.title" v-html="row.highlights.title[0]"></span>
          <span v-else>{{ row.title }}</span>
        </template>
      </el-table-column>
      <el-table-column label="项目" align="center" prop="project">
        <template #default="{ row }">
          <span v-if="row.highlights && row.highlights.project" v-html="row.highlights.project[0]"></span>
          <span v-else>{{ row.project }}</span>
        </template>
      </el-table-column>
      <el-table-column label="项目代号" align="center" prop="projectCode">
        <template #default="{ row }">
          <span v-if="row.highlights && row.highlights.projectCode" v-html="row.highlights.projectCode[0]"></span>
          <span v-else>{{ row.projectCode }}</span>
        </template>
      </el-table-column>
      <el-table-column label="简介/描述" align="center" prop="description">
        <template #default="{ row }">
          <span v-if="row.highlights && row.highlights.description" v-html="row.highlights.description[0]"></span>
          <span v-else>{{ row.description }}</span>
        </template>
      </el-table-column>
      <el-table-column label="保密等级" align="center" prop="secretLevel" />
      <!--      <el-table-column label="文件类型" align="center" prop="fileType"/>-->
      <!--      <el-table-column label="文件路径" align="center" prop="filePath">-->
      <!--        <template #default="{row}">-->
      <!--          <span v-if="row.highlights && row.highlights.filePath"-->
      <!--                v-html="row.highlights.filePath[0]"></span>-->
      <!--          <span v-else>{{ row.filePath }}</span>-->
      <!--        </template>-->
      <!--      </el-table-column>-->
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" @click="handleDetail(scope.row)" v-hasPermi="['manage:document:query']">查看
            <!--            v-hasPermi="['manage:document:query']"-->
          </el-button>
          <!-- 新添加的借阅按钮 -->
          <el-button link type="primary" @click="handleBorrow(scope.row)">借阅</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-container">
      <el-pagination v-model:current-page="currentPage" v-model:page-size="pageSize" :page-sizes="[10, 20, 50, 100]"
        :total="total" layout="total, sizes, prev, pager, next, jumper" @size-change="handleSizeChange"
        @current-change="handleCurrentChange" />
    </div>

    <el-dialog v-model="borrowOpen" :title="borrowTitle" width="600px">
      <el-form ref="borrowFormRef" :model="borrowForm" :rules="borrowRules" label-width="120px">
        <!-- 档案信息（自动填充） -->
        <el-form-item label="档案档号">
          <el-input v-model="borrowForm.archiveDangHao" disabled />
        </el-form-item>
        <el-form-item label="档案名称">
          <el-input v-model="borrowForm.archiveName" disabled />
        </el-form-item>

        <!-- 用户信息（手动输入） -->
        <el-form-item label="借阅人" prop="userName">
          <el-input v-model="borrowForm.userName" placeholder="请输入借阅人姓名" />
        </el-form-item>
        <!-- <el-form-item label="所属部门">
          <el-input v-model="borrowForm.userDepartment" placeholder="请输入所属部门（可选）" />
        </el-form-item> -->

        <!-- 借阅时间 -->
        <el-form-item label="借阅时间" required>
          <el-col :span="11">
            <el-form-item prop="startDate">
              <el-date-picker v-model="borrowForm.startDate" type="date" placeholder="开始日期" style="width: 100%"
                value-format="YYYY-MM-DD" />
            </el-form-item>
          </el-col>
          <el-col :span="2" class="text-center">至</el-col>
          <el-col :span="11">
            <el-form-item prop="endDate">
              <el-date-picker v-model="borrowForm.endDate" type="date" placeholder="结束日期" style="width: 100%"
                value-format="YYYY-MM-DD" />
            </el-form-item>
          </el-col>
        </el-form-item>

        <!-- 借阅目的 -->
        <el-form-item label="借阅目的" prop="purpose">
          <el-input v-model="borrowForm.purpose" type="textarea" :rows="3" placeholder="请详细说明借阅目的" show-word-limit
            maxlength="200" />
        </el-form-item>

        <!-- 流程选择 -->
        <el-form-item label="流程名称" prop="processName">
          <el-select v-model="borrowForm.processName" placeholder="请选择借阅流程" style="width: 100%">
            <el-option v-for="item in Modeler" :key="item.name" :label="item.name" :value="item.key" />
          </el-select>
        </el-form-item>

        <!-- 备注 -->
        <el-form-item label="备注">
          <el-input v-model="borrowForm.remark" type="textarea" :rows="2" placeholder="可输入额外说明信息" maxlength="100" />
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="borrowOpen = false">取消</el-button>
          <el-button type="primary" @click="submitBorrowForm">提交借阅</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 添加嵌套文档的高亮显示对话框 -->
    <el-dialog v-model="documentDialogVisible" title="文档内容" width="70%">
      <div v-if="currentDocument">
        <h3 v-if="currentDocumentHighlights && currentDocumentHighlights.docTitle"
          v-html="currentDocumentHighlights.docTitle[0]"></h3>
        <h3 v-else>{{ currentDocument.docTitle }}</h3>

        <div v-if="currentDocumentHighlights && currentDocumentHighlights.docContent"
          v-html="currentDocumentHighlights.docContent[0]"></div>
        <div v-else>{{ currentDocument.docContent }}</div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
// 显示的定义组件名称，和路由地址保持一致，
// 在系统管理->菜单管理 中可以配置是否开启页面缓存
export default {
  name: 'CustomArchiveSearch'
}
</script>
<script setup>
import { ElMessage, ElMessageBox } from "element-plus";
import { ElasticsearchArchiveList } from "@/api/manage/ElasticsearchArchive.js";
import { Plus, Minus } from '@element-plus/icons-vue';
import { ref, reactive, toRefs, getCurrentInstance, computed } from 'vue';
import { useRouter } from 'vue-router';
import axios from 'axios';
import { ArchivePermission, getArchiveIdByMysqlDanghao, getArchiveLocationByMysqlDanghao, getArchive } from "@/api/manage/archive.js";
import { getInfo } from "@/api/system/GDIpInfo.js";
import { unlockColumn, leftMoveColumn, rightMoveColumn, getColumnStatus, resetColumn } from "@/api/system/move.js";

// 引入借阅相关的API
import { addRecord, directRecord } from "@/api/manage/borrowrecord";
import { getUserInfo, listDefinition } from "@/api/activiti/definition"

const { proxy } = getCurrentInstance();
const router = useRouter();

const loading = ref(false);
const SearchList = ref([]);
const total = ref(0);
const dynamicRows = ref([{ field: 'project', value: '', logic: 'AND' }]);

// 新增：分页相关数据
const currentPage = ref(1);
const pageSize = ref(10);

// 借阅弹窗相关状态
const borrowOpen = ref(false);
const borrowTitle = ref("快速借阅");
const borrowFormRef = ref();
const Modeler = ref([]);

// 借阅表单数据
const borrowForm = ref({
  userId: null,
  userName: null,
  userDepartment: null,
  archiveId: null,
  archiveName: null,
  archiveDangHao: null,
  purpose: null,
  startDate: null,
  endDate: null,
  processName: null,
  remark: null,
});

// 借阅表单验证规则
const borrowRules = {
  startDate: [
    { required: true, message: "借阅开始日期不能为空", trigger: "blur" }
  ],
  endDate: [
    { required: true, message: "借阅结束日期不能为空", trigger: "blur" }
  ],
  processName: [
    { required: true, message: "流程名称不能为空", trigger: "blur" }
  ],
  purpose: [
    { required: true, message: "借阅目的不能为空", trigger: "blur" }
  ],
};

// 计算当前文档的高亮内容
const currentDocument = ref(null);
const documentDialogVisible = ref(false);
const currentDocumentHighlights = computed(() => {
  if (!currentDocument.value || !currentDocument.value._parent) return null;
  return currentDocument.value._parent.highlights;
});


const handleSizeChange = (val) => {
  if (val < 1 || val > 100) {
    ElMessage.warning('每页显示数量范围为1-100');
    return;
  }

  pageSize.value = val;
  currentPage.value = 1; // 改变页面大小时重置到第一页
  performSearch(); // 重新执行搜索
};

const handleCurrentChange = (val) => {
  // 验证页码有效性
  if (val < 1) {
    ElMessage.warning('页码不能小于1');
    currentPage.value = 1;
    return;
  }

  const maxPage = Math.ceil(total.value / pageSize.value);
  if (val > maxPage && maxPage > 0) {
    ElMessage.warning(`页码不能大于${maxPage}`);
    currentPage.value = maxPage;
    val = maxPage;
  }

  currentPage.value = val;
  performSearch(); // 重新执行搜索
};


// 执行搜索的通用方法
const performSearch = () => {
  // 合并静态条件和动态条件
  const searchParams = {
    MainType: formData.value.MainType,
    MainTypeValue: formData.value.MainTypeValue,
    MainTypeLogic: formData.value.MainTypeLogic,
    secondMainType: formData.value.secondMainType,
    secondMainTypeValue: formData.value.secondMainTypeValue,
    secondMainTypeLogic: formData.value.secondMainTypeLogic,
    DocumentType: formData.value.DocumentType.join(','),
    // 分页参数
    pageNum: currentPage.value,
    pageSize: pageSize.value
  };

  // 添加动态条件
  dynamicRows.value.forEach((row, index) => {
    searchParams[`dynamicField${index}`] = row.field;
    searchParams[`dynamicValue${index}`] = row.value;
    searchParams[`dynamicLogic${index}`] = row.logic;
  });

  getList(searchParams);
};

const formRef = ref()
const data = reactive({
  formData: {
    MainType: 'project',
    MainTypeValue: undefined,
    MainTypeLogic: 'AND',
    secondMainType: 'project',
    secondMainTypeValue: undefined,
    secondMainTypeLogic: 'AND',
    DocumentType: [],
  },
  rules: {
    MainType: [{
      required: true,
      message: '关键词',
      trigger: 'change'
    }],

    secondMainType: [{
      required: false,
      message: '关键词',
      trigger: 'change'
    }],
    MainTypeValue: [{
      required: true,
      message: '请输入',
      trigger: 'blur'
    }],
    secondMainTypeValue: [{
      required: false,
      message: '关键字',
      trigger: 'change'
    }],
  }
})
const {
  formData,
  rules
} = toRefs(data)
const MainTypeOptions = ref([
  {
    "label": "标题",
    "value": "title"
  },
  // {
  //   "label": "简介",
  //   "value": "description"
  // }, {
  //   "label": "档号",
  //   "value": "mysqlDanghao"
  // },
  {
    "label": "所属项目",
    "value": "project"
  },
  {
    "label": "项目代号",
    "value": "projectCode"
  }
  // ,{
  //   "label": "路径",
  //   "value": "filePath"
  // }
  , { label: "文档标题", value: "documents.docTitle" },
  { label: "文档内容", value: "documents.docContent" }
])
const SecondTypeOptions = ref([
  {
    "label": "标题",
    "value": "title"
  },
  // {
  //   "label": "简介",
  //   "value": "description"
  // },
  {
    "label": "所属项目",
    "value": "project"
  },
  {
    "label": "项目代号",
    "value": "projectCode"
  }
  // ,{
  //   "label": "档号",
  //   "value": "mysqlDanghao"
  // }, {
  //   "label": "路径",
  //   "value": "filePath"
  // }
  , { label: "文档标题", value: "documents.docTitle" },
  { label: "文档内容", value: "documents.docContent" }
])
const DocumentTypeOptions = ref([{
  "label": "电子档案",
  "value": 1
}, {
  "label": "实体档案",
  "value": 2
}])

const LogicOptions = ref([
  { label: 'AND', value: 'AND', disabled: false },
  { label: 'OR', value: 'OR', disabled: false },
  { label: 'NOT', value: 'NOT', disabled: false }
]);

const queryParams_activitiList = reactive({
  pageNum: 1,
  pageSize: 10,
  id: null,
  rev: null,
  name: null,
  type: null,
});

// 电子档案借阅标志
const elecBorrow = ref(false);

// 借阅功能方法
async function handleBorrow(row) {
  console.log('快速借阅档案:', row);

  // 重置借阅表单
  resetBorrowForm();

  // 自动填充档案信息
  borrowForm.value.archiveDangHao = row.mysqlDanghao;
  borrowForm.value.archiveName = row.title;

  // 获取档案ID
  try {
    const response = await getArchiveIdByMysqlDanghao(row.mysqlDanghao);
    borrowForm.value.archiveId = response.data || response;

    // 获取档案信息
    const res = await getArchive(borrowForm.value.archiveId);
    const archive = res.data;
    console.log('档案信息:', archive);

    // 如果档案没有归档则不能借阅
    if (archive.status !== 'Archived') {
      ElMessage.warning('档案还未归档，不能借阅！');
      return;
    }

    // 如果实体档案已借出则提示
    if (archive.availability && archive.availability !== 0) {
      const confirm = window.confirm('当前实体档案无法借阅，是否继续借阅电子档案？');
      if (confirm) {
        elecBorrow.value = true;
      } else {
        return;
      }
    }

  } catch (error) {
    console.error('获取档案ID失败:', error);
  }

  try {
    const userResponse = await getUserInfo();
    borrowForm.value.userId = userResponse.userId;
    borrowForm.value.userName = userResponse.userName;
    // borrowForm.value.userDepartment = userResponse.deptName;
  } catch (error) {
    console.error('获取用户信息失败:', error);
    ElMessage.error('获取用户信息失败');
    return;
  }

  // 获取流程列表
  try {
    const processResponse = await listDefinition(queryParams_activitiList);
    Modeler.value = processResponse.rows;
  } catch (error) {
    console.error('获取流程列表失败:', error);
  }

  // 显示借阅弹窗
  borrowOpen.value = true;
}

// 重置借阅表单
function resetBorrowForm() {
  borrowForm.value = {
    userId: null,
    userName: null,
    userDepartment: null,
    archiveId: null,
    archiveName: null,
    archiveDangHao: null,
    purpose: null,
    startDate: null,
    endDate: null,
    processName: null,
    remark: null,
  };
}



// 取消借阅
function cancelBorrow() {
  borrowOpen.value = false;
  resetBorrowForm();
}

// 提交借阅表单
function submitBorrowForm() {
  console.log('是否电子借阅:', elecBorrow.value);
  borrowFormRef.value.validate(valid => {
    if (valid) {
      if (elecBorrow.value) {
        if (borrowForm.value.remark) {
          borrowForm.value.remark = borrowForm.value.remark + '(电子借阅)';
        } else {
          borrowForm.value.remark = '(电子借阅)';
        }
      }
      console.log('借阅表单:', borrowForm.value);
      directRecord(borrowForm.value).then(response => {
        proxy.$modal.msgSuccess("借阅申请提交成功");
        borrowOpen.value = false;
        resetBorrowForm();
      }).catch(error => {
        console.error('借阅申请失败:', error);
        ElMessage.error('借阅申请失败: ' + (error.message || '未知错误'));
      });
    } else {
      console.log('借阅表单验证失败');
    }
  });
}
//
// function handleDetail(row) {
//   const mysqlDanghao = row.mysqlDanghao;
//   console.log('mysqlDanghao:', mysqlDanghao);
//   // 调用后端接口根据 mysqlDanghao 获取 archiveId
//   getArchiveIdByMysqlDanghao(mysqlDanghao).then(response => {
//     console.log('response:', response);
//     // const archiveId = response.data;
//     // 根据archiveId返回类型进行不同的处理
//     let archiveId = typeof response === 'number' ? response : response.data;
//     console.log('archiveId:', archiveId);
//     console.log('row highlights:', row.highlights); // 调试用
//
//     //我要开始写屎山了，在这里判断权限问题
//     ArchivePermission(archiveId).then(responsePermission => {
//       // 跳转参数
//       proxy.$router.push({
//         path: '/manage/archive/arc-detail/',
//         query: {
//           id: response.data || response,
//           fromSearch: true,
//           searchText: formData.value.MainTypeValue, // 传递搜索词
//           searchField: formData.value.MainType,     // 传递搜索字段
//           globalSort: true
//         }
//       });
//
//     }).catch(error => {
//     })
//
//   });
// }

function handleDetail(row) {
  const mysqlDanghao = row.mysqlDanghao;
  console.log('mysqlDanghao:', mysqlDanghao);
  console.log('row highlights:', row.highlights); // 调试用

  // 提取文档相关的高亮信息
  const documentHighlights = {};

  if (row.highlights) {
    // 遍历所有高亮字段，提取文档相关的高亮
    Object.keys(row.highlights).forEach(key => {
      if (key.startsWith('documents.')) {
        // 为每个匹配的文档创建高亮信息
        // 这里需要根据你的ES返回结构来调整
        if (row.documents && row.documents.length > 0) {
          row.documents.forEach((doc, index) => {
            // 假设我们为每个文档创建一个唯一标识
            const docKey = doc.docId || `doc_${index}`;
            if (!documentHighlights[docKey]) {
              documentHighlights[docKey] = {};
            }

            // 根据高亮字段类型分类存储
            if (key === 'documents.docTitle') {
              documentHighlights[docKey]['docTitle'] = row.highlights[key];
            } else if (key === 'documents.docContent') {
              documentHighlights[docKey]['docContent'] = row.highlights[key];
            }
          });
        }
      }
    });
  }

  console.log('提取的文档高亮信息:', documentHighlights); // 调试用

  // 调用后端接口根据 mysqlDanghao 获取 archiveId
  getArchiveIdByMysqlDanghao(mysqlDanghao).then(response => {
    console.log('response:', response);
    if (!response || !response.data){
      ElMessage.error('此档案不存在,或者档案位置信息不完整，请检查此档案是否存在');
      ElMessageBox.confirm('此档案不存在,或者档案位置信息不完整，请检查此档案是否存在')
      return false
    }else {
      // 根据archiveId返回类型进行不同的处理
      let archiveId = typeof response === 'number' ? response : response.data;
      console.log('archiveId:', archiveId);

      // 构建跳转参数
      const queryParams = {
        id: response.data || response,
        fromSearch: true,
        searchText: formData.value.MainTypeValue, // 传递搜索词
        searchField: formData.value.MainType,     // 传递搜索字段
        globalSort: true
      };

      // 如果存在档案级别的高亮信息，添加到参数中
      if (row.highlights) {
        const archiveHighlights = {};
        Object.keys(row.highlights).forEach(key => {
          if (!key.startsWith('documents.')) {
            archiveHighlights[key] = row.highlights[key];
          }
        });

        if (Object.keys(archiveHighlights).length > 0) {
          queryParams.highlights = JSON.stringify(archiveHighlights);
        }
      }

      // 如果存在文档高亮信息，添加到参数中
      if (Object.keys(documentHighlights).length > 0) {
        queryParams.documentHighlights = JSON.stringify(documentHighlights);
      }

      console.log('跳转参数:', queryParams); // 调试用

      // 用户权限检查（保留原有的权限验证逻辑）
      ArchivePermission(archiveId).then(responsePermission => {
        // 权限验证通过后跳转，使用完整的查询参数
        proxy.$router.push({
          path: '/manage/archive/arc-detail/',
          query: queryParams
        });

      }).catch(error => {
        // 权限验证失败的处理逻辑
        console.error('权限验证失败:', error);
      });
    }


  });
}
// 查看文档内容（点击查看嵌套文档内容的方法）
function showDocumentDetails(document, parent) {
  // 将父级的高亮信息附加到文档对象上
  document._parent = parent;
  currentDocument.value = document;
  documentDialogVisible.value = true;
}


// 添加条件行
const addRow = () => {
  dynamicRows.value.push({ field: 'project', value: '', logic: 'AND' });
};

// 静态删除条件行
const removeRow = () => {
  if (dynamicRows.value.length > 0) {
    dynamicRows.value.pop();
  }
};
// 动态行删除方法
const removeDynamicRow = (index) => {
  if (dynamicRows.value.length > 0) {
    dynamicRows.value.splice(index, 1)
  }
}

/** 重置按钮操作 */
function resetQuery() {
  // 重置表单验证状态
  formRef.value.resetFields();

  // 重置静态表单数据
  formData.value = {
    MainType: 'project',
    MainTypeValue: undefined,
    MainTypeLogic: 'AND',
    secondMainType: 'project',
    secondMainTypeValue: undefined,
    secondMainTypeLogic: 'AND',
    DocumentType: [],
  };

  // 重置动态行内容
  dynamicRows.value = dynamicRows.value.map(() => ({
    field: 'project',
    value: '',
    logic: 'AND'
  }));

  // 新增：重置分页参数
  currentPage.value = 1;
  pageSize.value = 10;
  SearchList.value = [];
  total.value = 0;
}

// 提交表单
const submitForm = () => {
  // formRef.value.validate((valid) => {
  //   if (valid) {
  //     // 合并静态条件和动态条件
  //     const searchParams = {
  //       MainType: formData.value.MainType,
  //       MainTypeValue: formData.value.MainTypeValue,
  //       MainTypeLogic: formData.value.MainTypeLogic, // 添加静态条件的逻辑
  //       secondMainType: formData.value.secondMainType,
  //       secondMainTypeValue: formData.value.secondMainTypeValue,
  //       secondMainTypeLogic: formData.value.secondMainTypeLogic, // 添加静态条件的逻辑
  //       DocumentType: formData.value.DocumentType.join(','), // 将数组转换为字符串
  //     };
  //     // 添加动态条件
  //     dynamicRows.value.forEach((row, index) => {
  //       searchParams[`dynamicField${index}`] = row.field; // 动态字段名
  //       searchParams[`dynamicValue${index}`] = row.value; // 动态字段值
  //       searchParams[`dynamicLogic${index}`] = row.logic; // 动态字段值
  //     });
  //     // 动态条件改为数组格式（推荐）
  //     // dynamicConditions: dynamicRows.value.map(row => ({
  //     //   logic: row.logic,  // 动态条件的逻辑
  //     //   field: row.field,  // 动态条件的字段
  //     //   value: row.value   // 动态条件的值
  //     // }));
  //     console.log('提交的参数：', searchParams); // 打印参数
  //     // 调用搜索接口
  //     getList(searchParams);
  //   } else {
  //     console.log('表单验证失败');
  //   }
  // });
  formRef.value.validate((valid) => {
    if (valid) {
      currentPage.value = 1; // 新搜索时重置到第一页
      performSearch(); // 使用通用搜索方法
    } else {
      console.log('表单验证失败');
    }
  });
};

function getList(searchParams) {
  loading.value = true;
  ElasticsearchArchiveList(searchParams).then(response => {
    // 🔥 添加防御性检查
    if (!response) {
      console.error('响应数据为空');
      SearchList.value = [];
      total.value = 0;
      return;
    }

    // 🔥 检查 response.rows 是否存在
    if (!response.rows) {
      console.error('响应数据中没有rows字段:', response);
      SearchList.value = [];
      total.value = response.total || 0;

      // 如果是最后一页且没有数据，可能是页码超出范围
      if (currentPage.value > 1 && total.value > 0) {
        const maxPage = Math.ceil(total.value / pageSize.value);
        if (currentPage.value > maxPage) {
          console.warn(`当前页码${currentPage.value}超出最大页码${maxPage}，自动跳转到最后一页`);
          currentPage.value = maxPage;
          // performSearch(); // 重新搜索
          return;
        }
      }

      ElMessage.warning('当前页面暂无数据');
      return;
    }

    // 🔥 确保 rows 是数组
    if (!Array.isArray(response.rows)) {
      console.error('response.rows 不是数组:', typeof response.rows, response.rows);
      SearchList.value = [];
      total.value = response.total || 0;
      return;
    }


    // 处理返回数据，确保高亮信息正确
    SearchList.value = response.rows.map(item => {
      // 处理嵌套文档的高亮
      if (item.documents && item.highlights) {
        item.documents.forEach(doc => {
          // 将父级的高亮信息附加到文档对象上
          doc._parent = item;
        });
      }
      return item;
    });
    total.value = response.total;

    // 2. 新增：显示权限提示
    if (response.msg) {
      ElMessage({
        type: 'warning',      // 使用警告类型（黄色）
        message: response.msg,
        duration: 5000,       // 显示5秒
        showClose: true       // 显示关闭按钮
      });
    }
    loading.value = false;

  }).catch(error => {
    loading.value = false;
    ElMessage.error('搜索失败: ' + error.message);
  });
};


</script>
<style>
/* 高亮样式 */
.highlight {
  background-color: transparent;
  /* 透明背景 */
  color: red;
  /* 红色字体 */
  font-weight: bold;
  padding: 0 2px;
  border-radius: 2px;
  text-decoration: none;

}

/* 调整组合输入框样式 */
.input-with-select {
  width: 100%;
}

/* 调整选择框宽度 */
:deep(.input-with-select .el-select) {
  width: 120px;
}

/* 保持输入框和选择框高度一致 */
:deep(.input-with-select .el-input-group__prepend) {
  background-color: #fff;
  padding: 0 12px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
</style>
