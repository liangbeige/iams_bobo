<template>
  <div class="archive-edit-container">
    <el-card class="form-card">
      <template #header>
        <div class="card-header">
          <h3>修改档案基本信息</h3>
        </div>
      </template>

      <el-form ref="archiveRef" :model="form" :rules="rules" label-position="top" class="archive-form">

        <!-- 全宗选择区域 - 移到最前面 -->
        <div class="form-section">
          <h4 class="section-title">全宗信息</h4>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="所属全宗" prop="fondsId" required>
                <el-select
                    v-model="form.fondsId"
                    placeholder="请先选择所属全宗"
                    style="width: 100%"
                    @change="handleFondsChange"
                >
                  <el-option
                      v-for="fonds in fondsList"
                      :key="fonds.id"
                      :label="`${fonds.name} (${fonds.bianhao})`"
                      :value="fonds.id"
                  />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="全宗门类模板">
                <el-tag v-if="form.fondsId && categoryRoots.length > 0" type="success">
                  已配置 {{ categoryRoots.length }} 个门类
                </el-tag>
                <el-tag v-else-if="form.fondsId && categoryRoots.length === 0" type="warning">
                  该全宗暂未配置门类模板
                </el-tag>
                <el-tag v-else type="info">
                  请先选择全宗
                </el-tag>
              </el-form-item>
            </el-col>
          </el-row>
        </div>

        <!-- 分类信息区域 -->
        <div class="form-section">
          <h4 class="section-title">分类信息</h4>
          <el-row :gutter="20">
            <el-col :span="8">
              <el-form-item label="档案门类" prop="categoryUniqueKey">
                <el-select
                    v-model="form.categoryUniqueKey"
                    :disabled="!form.fondsId"
                    placeholder="请选择档案主门类"
                    style="width: 100%"
                    @change="handleCategoryChange"
                >
                  <el-option v-for="category in categoryRoots" :key="category.uniqueKey" :label="`${category.code}:${category.name}`"
                    :value="category.uniqueKey" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="选择门类">
                <el-tree-select v-model="selectedCategoryUniqueKeys" :disabled="!form.categoryUniqueKey" :data="categoryTreeData" :props="{
                  label: 'name',
                  value: 'uniqueKey',
                  children: 'children'
                }" value-key="uniqueKey" placeholder="请选择子门类" style="width: 100%" :render-after-expand="false" multiple
                  :check-strictly="false" show-checkbox>
                  <template #default="{ node, data }">
                    <span class="tree-select-node">
                      <span class="node-code">{{ data.code }}</span>
                      <span class="node-name">{{ data.name }}</span>
                    </span>
                  </template>
                </el-tree-select>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="载体类型" prop="carrierType">
                <el-select v-model="form.carrierType" placeholder="请选择载体类型" style="width: 100%">
                  <el-option v-for="dict in iams_carrier_type" :key="dict.value" :label="dict.label"
                    :value="dict.value" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="保管期限" prop="retentionPeriod">
                <el-select v-model="form.retentionPeriod" placeholder="请选择保管期限" style="width: 100%">
                  <el-option v-for="dict in iams_retention_period" :key="dict.value" :label="dict.label"
                    :value="dict.value" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
        </div>

        <!-- 基础信息区域 -->
        <div class="form-section">
          <h4 class="section-title">基础信息</h4>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="所属项目">
                <el-select v-model="form.projectId" placeholder="请选择项目" style="width: 100%" clearable>
                  <el-option v-for="project in projectList" :key="project.id" :label="project.name" :value="project.id">
                    <span>{{ project.name }}</span>
                    <span class="option-code">{{ project.code }}</span>
                  </el-option>
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="电子建档时间" prop="startDate">
                <el-date-picker v-model="form.startDate" type="date" value-format="YYYY-MM-DD" placeholder="请选择电子建档时间"
                  style="width: 100%" clearable />
              </el-form-item>
            </el-col>
            <el-col :span="24">
              <el-form-item label="档案名称" prop="name">
                <el-input v-model="form.name" placeholder="请输入档案名称" clearable />
              </el-form-item>
            </el-col>
          </el-row>
        </div>

        <!-- 保密信息区域 -->
        <div class="form-section">
          <h4 class="section-title">保密信息</h4>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="保密级别" prop="secretLevel">
                <el-select v-model="form.secretLevel" placeholder="请选择保密级别" style="width: 100%">
                  <el-option v-for="dict in iams_secret_level" :key="dict.value" :label="dict.label"
                    :value="dict.value" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="保密期限" prop="secretPeroid">
                <el-select v-model="form.secretPeroid" placeholder="请选择保密期限" style="width: 100%">
                  <el-option v-for="dict in iams_secret_period" :key="dict.value" :label="dict.label"
                    :value="dict.value" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
        </div>

        <!-- 单位信息区域 -->
        <div class="form-section">
          <h4 class="section-title">单位信息</h4>
          <el-row :gutter="20">
<!--            <el-col :span="8">-->
<!--              <el-form-item label="所属全宗" prop="fonds">-->
<!--                <el-select v-model="form.fondsId" placeholder="请选择所属全宗" style="width: 100%">-->
<!--                  <el-option v-for="fonds in fondsList" :key="fonds.id" :label="fonds.name" :value="fonds.id" />-->
<!--                </el-select>-->
<!--              </el-form-item>-->
<!--            </el-col>-->
            <el-col :span="8">
              <el-form-item label="形成单位" prop="formationUnit">
                <el-input v-model="form.formationUnit" placeholder="请输入形成单位" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="移交单位" prop="transferUnit">
                <el-input v-model="form.transferUnit" placeholder="请输入移交单位" />
              </el-form-item>
            </el-col>
          </el-row>
        </div>

        <!-- 详细信息区域 -->
        <div class="form-section">
          <h4 class="section-title">详细信息</h4>
          <el-form-item label="档案简介" prop="description">
            <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入内容" />
          </el-form-item>
          <el-form-item label="备注" prop="remark">
            <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入内容" />
          </el-form-item>
        </div>

        <!-- 编号信息区域 -->
        <div class="form-section">
          <h4 class="section-title">编号信息</h4>
          <el-row :gutter="20">
            <el-col :span="24">
              <el-form-item label="档案编号" prop="danghao">
                <el-input v-model="form.danghao" placeholder="档号将根据下面的信息自动生成" :disabled="true"
                  style="background-color: #f5f7fa;" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="组卷单元" prop="zuojuanUnit">
                <el-input v-model="form.zuojuanUnit" placeholder="请输入组卷单元" clearable @input="updateDanghao" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="案卷号" prop="juanhao">
                <el-input v-model="form.juanhao" placeholder="请输入案卷号" clearable @input="updateDanghao" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="件号(册数)" prop="jianhao">
                <el-input-number v-model="form.jianhao" :min="1" :max="9999" placeholder="请输入册数" style="width: 100%"
                  controls-position="right" @change="updateDanghao" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="射频标签号" prop="rfid">
                <el-input v-model="form.rfid" placeholder="请输入射频标签号(可以根据档案编号按指定规则生成)">
                  <template #append>
                    <el-button type="primary" @click="handelRfid">
                      自动生成
                    </el-button>
                  </template>
                </el-input>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row>
            <el-col :span="24">
              <div class="danghao-preview">
                <el-alert :title="getDanghaoParts()" type="info" :closable="false" show-icon />
              </div>
            </el-col>
          </el-row>
        </div>

        <!-- 额外信息区域 -->
        <div class="form-section">
          <h4 class="section-title">额外信息</h4>
          <el-form-item label="额外信息" prop="extraInfo">
            <el-button type="primary" @click="showDialog">
              <el-icon>
                <Plus />
              </el-icon>
              修改额外信息
            </el-button>
          </el-form-item>
        </div>
      </el-form>

      <template #footer>
        <div class="form-footer">
          <el-button @click="cancel" size="large">取 消</el-button>
          <el-button type="primary" @click="submitForm" size="large">
            提 交
          </el-button>
        </div>
      </template>
    </el-card>

    <!-- 额外信息弹窗 -->
    <el-dialog v-model="dialogFormVisible" title="档案额外信息" width="600px">
      <el-form :model="extraInfo" class="extra-info-form">
        <div v-for="(info, index) in extraInfoList" :key="index" class="extra-info-item">
          <el-form-item label="属性">
            <el-input v-model="info.label" autocomplete="off" placeholder="请输入属性名称" />
          </el-form-item>
          <el-form-item label="内容">
            <el-input v-model="info.value" autocomplete="off" placeholder="请输入属性内容" />
          </el-form-item>
          <el-button v-if="extraInfoList.length > 1" type="danger" @click="removeExtraInfo(index)" :icon="Minus" circle
            class="remove-btn" />
        </div>
        <div class="add-info-btn">
          <el-button type="primary" @click="addExtraInfo" :icon="Plus" circle />
        </div>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="cancelDialog">取消</el-button>
          <el-button type="primary" @click="saveExtraInfoList">确定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="edit">
import { Plus, Minus } from '@element-plus/icons-vue';
import { ref, reactive, onMounted, watch, toRefs, getCurrentInstance } from 'vue'
import { useRoute, useRouter } from 'vue-router';
import useTagsViewStore from '@/store/modules/tagsView.js'
import { getArchive, updateArchive, getNextArchiveNumber } from '@/api/manage/archive'
import { listFonds } from "@/api/manage/fonds";
import { listProject } from "@/api/manage/project";
import { getCategoryRoots, getCategoryTreeByCode,getCategoryTreeByUniqueKey } from "@/api/manage/treeCategory.js";
import { getFondsCategories } from "@/api/manage/category"; // 新增：获取全宗门类配置
// 添加 dayjs 导入
import dayjs from 'dayjs';

const { proxy } = getCurrentInstance()
const {
  iams_carrier_type,
  iams_retention_period,
  iams_secret_period,
  iams_secret_level
} = proxy.useDict('iams_carrier_type', 'iams_retention_period', 'iams_secret_period', 'iams_secret_level');

const tagsViewStore = useTagsViewStore();
const route = useRoute();
const router = useRouter();

const categoryId = ref(null);
const selectedCategoryCodes = ref([]);
const categoryTreeData = ref([]);
const selectedCategoryUniqueKeys = ref([]); // 使用 uniqueKey 而不是 code

// 响应式数据
const projectList = ref([]);
const categoryRoots = ref([]);
const fondsList = ref([]);
const allCategoryRoots = ref([]); // 所有门类根节点（用于备份）

const extraInfoList = ref([{ label: '', value: '' }]);
const dialogFormVisible = ref(false);

// 位置选择变量
const quHao = ref('');
const lieHao = ref('');
const ceHao = ref('');
const jieHao = ref('');
const cengHao = ref('');

const data = reactive({
  form: {
    id: null,
    danghao: null,
    name: null,
    jianhao: 1, // 默认为第1册
    secretLevel: null,
    secretPeroid: null,
    desecretDate: null,
    retentionPeriod: null,
    carrierType: null,
    startDate: null,
    endDate: null,
    shitiLocation: null,
    exactLocation: null,
    dianziLocation: null,
    description: null,
    formationUnit: null,
    transferUnit: null,
    documentCount: null,
    directory: null,
    extraInfo: null,
    categoryId: null,
    fondsId: null,
    rfid: null,
    projectId: null,
    remark: null,
    zuojuanUnit: null, // 组卷单元
    juanhao: null // 案卷号
  },
  rules: {
    name: [
      { required: true, message: "档案名称不能为空", trigger: "blur" }
    ],
    carrierType: [
      { required: true, message: "载体类型不能为空", trigger: "blur" }
    ],
    jianhao: [
      { required: true, message: "件号不能为空", trigger: "blur" },
      { type: 'number', min: 1, max: 9999, message: "件号必须在1-9999之间", trigger: "blur" }
    ],
    fondsId: [
      { required: true, message: "请先选择所属全宗", trigger: "change" }
    ]

  },
  // 位置选择项
  iams_shiti_location_quhao: [
    { label: '第1区', value: '1' },
    { label: '第2区', value: '2' },
    { label: '第3区', value: '3' },
    { label: '第4区', value: '4' }
  ],
  iams_shiti_location_liehao: Array.from({ length: 18 }, (_, i) => ({
    label: `第${i + 1}列`,
    value: (i + 1).toString()
  })),
  iams_shiti_location_zy: [
    { label: '左侧(A面)', value: 'A' },
    { label: '右侧(B面)', value: 'B' }
  ],
  iams_shiti_location_cenghao: Array.from({ length: 6 }, (_, i) => ({
    label: `第${i + 1}层`,
    value: (i + 1).toString()
  })),
  iams_shiti_location_jiehao: Array.from({ length: 5 }, (_, i) => ({
    label: `第${i + 1}节`,
    value: (i + 1).toString()
  }))
});

const {
  form,
  rules,
  iams_shiti_location_quhao,
  iams_shiti_location_liehao,
  iams_shiti_location_zy,
  iams_shiti_location_cenghao,
  iams_shiti_location_jiehao
} = toRefs(data);

const extraInfo = reactive({
  label: '',
  value: '',
});

async function loadCategoryTree(categoryCode) {
  try {
    // 清空之前的选择
    selectedCategoryCodes.value = [];
    const response = await getCategoryTreeByCode(categoryCode)
    if (response.data) {
      categoryTreeData.value = Array.isArray(response.data) ? response.data : [response.data]

      console.log('categoryTreeData', categoryTreeData.value)
    } else {
      categoryTreeData.value = []
    }
  } catch (error) {
    console.error('加载分类树失败:', error)
    proxy.$modal.msgError('加载分类树失败')
  }
}

// 处理门类选择变化
const handleCategoryChange = async (uniqueKey) => {
  selectedCategoryUniqueKeys.value = [];
  categoryTreeData.value = [];

  if (!uniqueKey) {
    return;
  }

  try {
    // 使用新的 API 方法，传递 uniqueKey
    const response = await getCategoryTreeByUniqueKey(uniqueKey);

    if (response.data) {
      // 为每个节点添加 uniqueKey
      const addUniqueKeys = (nodes) => {
        return nodes.map(node => {
          const nodeWithKey = {
            ...node,
            uniqueKey: `${node.code}:${node.name}`
          };

          if (node.children && node.children.length > 0) {
            nodeWithKey.children = addUniqueKeys(node.children);
          }

          return nodeWithKey;
        });
      };

      categoryTreeData.value = addUniqueKeys(
          Array.isArray(response.data) ? response.data : [response.data]
      );

      // 如果有已有的子门类选择，设置选中状态
      if (selectedCategoryUniqueKeys.value.length > 0) {
        // 这里可能需要额外的逻辑来设置树的选择状态
      }
    } else {
      categoryTreeData.value = [];
      proxy.$modal.msgWarning('该门类暂无子分类');
    }
  } catch (error) {
    console.error('获取子门类失败:', error);
    proxy.$modal.msgError('获取子门类失败');
  }
};


async function someAsyncFunction() {
  await getCategoryRootList();
  await getFondsList();
  await getProjectList();

  const _id = route.query.id;
  const response = await getArchive(_id);
  form.value = response.data;

  // 根据档案的全宗ID加载对应的门类模板
  if (form.value.fondsId) {
    await loadFondsCategoryTemplate(form.value.fondsId);
  }

  // 解析已有的门类信息（categoryId）
  if (form.value.categoryId) {
    const parts = form.value.categoryId.split(',');
    if (parts.length > 0) {
      // 第一部分是大门类信息（格式：大门类代码:大门类名称）
      const rootPart = parts[0];
      // 设置大门类的uniqueKey
      form.value.categoryUniqueKey = rootPart;

      // 剩余部分是子门类信息（格式：子门类代码:子门类名称）
      const subParts = parts.slice(1);
      // 设置子门类的uniqueKey（只取代码部分）
      selectedCategoryUniqueKeys.value = subParts.map(part => {
        const [code] = part.split(':');
        return `${code}:${code}`; // 只保留代码部分
      });
    }
  }

  // 确保jianhao有值，如果没有默认为1
  if (!form.value.jianhao) {
    form.value.jianhao = 1;
  }

  // 设置位置选择器的值
  if (form.value.shitiLocation) {
    const locationParts = form.value.shitiLocation.split('-');
    quHao.value = locationParts[0] || '';
    lieHao.value = locationParts[1] || '';
    ceHao.value = locationParts[2] || '';
  }

  if (form.value.exactLocation) {
    const exactParts = form.value.exactLocation.split('-');
    jieHao.value = exactParts[0] || '';
    cengHao.value = exactParts[1] || '';
  }

  // 设置额外信息
  if (response.data.extraInfo) {
    extraInfoList.value = JSON.parse(response.data.extraInfo);
  }
}

// 生命周期
onMounted(async () => {
  reset();

  await someAsyncFunction();

  const selectedProject = projectList.value.find(project => project.id === form.value.projectId);
  if (selectedProject) {
    // 将项目的name赋值给组卷单元
    form.value.zuojuanUnit = selectedProject.name;
  }
  updateDanghao();

});

// 监听位置变化
watch([quHao, lieHao, ceHao, jieHao, cengHao], ([newQuHao, newLieHao, newCeHao, newJieHao, newCengHao]) => {
  if (newQuHao && newLieHao && newCeHao && newCengHao && newJieHao) {
    form.value.shitiLocation = `${newQuHao}-${newLieHao}-${newCeHao}`;
    form.value.exactLocation = `${newJieHao}-${newCengHao}`;
  }
}, { immediate: false });

// 监听档号相关字段变化，自动更新档号
watch([
  () => form.value.fondsId,
  () => form.value.retentionPeriod,
  () => form.value.categoryId,
  () => categoryId.value,
  () => form.value.zuojuanUnit,
  () => form.value.juanhao,
  () => form.value.jianhao
], () => {
  updateDanghao();
}, { immediate: false });

// 新增：监听全宗选择变化 - 与添加页面保持一致
watch(() => form.value.fondsId, async (newFondsId, oldFondsId) => {
  console.log('全宗选择变化:', newFondsId, '原值:', oldFondsId);

  // 只有在用户主动改变全宗时才清空相关选择（初始化时不清空）
  if (oldFondsId !== undefined) {
    // 清空相关选择
    categoryId.value = null;
    selectedCategoryCodes.value = [];
    categoryTreeData.value = [];
  }

  if (newFondsId) {
    try {
      await loadFondsCategoryTemplate(newFondsId);
    } catch (error) {
      console.error('加载全宗门类模板失败:', error);
    }
  } else {
    // 如果清空全宗选择，恢复显示所有门类
    categoryRoots.value = [];
  }
});

// 新增：处理全宗选择变化的函数 - 与添加页面保持一致
const handleFondsChange = async (fondsId) => {
  console.log('全宗选择变化:', fondsId);

  // 清空相关选择
  categoryId.value = null;
  selectedCategoryCodes.value = [];
  categoryTreeData.value = [];

  if (fondsId) {
    try {
      await loadFondsCategoryTemplate(fondsId);
    } catch (error) {
      console.error('加载全宗门类模板失败:', error);
      proxy.$modal.msgError('加载全宗门类模板失败');
    }
  } else {
    // 如果清空全宗选择，恢复显示所有门类
    categoryRoots.value = [...allCategoryRoots.value];
  }
};

// 新增：加载全宗门类模板的方法 - 与添加页面保持一致
async function loadFondsCategoryTemplate(fondsId) {
  try {
    console.log('开始加载全宗门类模板，fondsId:', fondsId);
    const response = await getFondsCategories(fondsId);
    console.log('全宗门类模板响应:', response);

    if (response.data && response.data.length > 0) {
      categoryRoots.value = response.data.map(item => ({
        ...item,
        uniqueKey: `${item.code}:${item.name}`
      }));
    } else {
      // 如果全宗没有配置门类模板，显示提示并清空选项
      categoryRoots.value = [];
      proxy.$modal.msgWarning('该全宗暂未配置门类模板，请先配置门类模板');
    }
  } catch (error) {
    console.error('加载全宗门类模板失败:', error);
    proxy.$modal.msgError('加载全宗门类模板失败');
    categoryRoots.value = [];
  }
}


// 档号生成相关方法
function getRetentionPeriodCode(retentionPeriod) {
  // 根据保管期限值映射编码
  if (retentionPeriod === '200') { // 永久
    return 'Y';
  } else if (retentionPeriod === '50') { // 50年
    return 'D';
  }
  return retentionPeriod || '';
}

function getCategoryCode(categoryId) {
  // 根据类别生成SS·类别格式
  if (categoryId) {
    return `SS·${categoryId}`;
  }
  return '';
}

function getFondsCode(fondsId) {
  // 从全宗列表中获取对应的编号
  const fonds = fondsList.value.find(f => f.id === fondsId);
  return fonds ? fonds.bianhao : '';
}

function updateDanghao() {
  const parts = [];

  // 全宗编号
  const fondsCode = getFondsCode(form.value.fondsId);
  if (fondsCode) parts.push(fondsCode);

  // 保管期限编码
  const retentionCode = getRetentionPeriodCode(form.value.retentionPeriod);
  if (retentionCode) parts.push(retentionCode);

  // 固定部分 "SS"
  parts.push("SS");

  // 门类代码（从选择的门类中提取）
  let categoryCodePart = "";
  if (form.value.categoryUniqueKey) {
    const [code] = form.value.categoryUniqueKey.split(':');
    categoryCodePart = code;
  } else if (selectedCategoryUniqueKeys.value.length > 0) {
    // 如果没有大门类，使用第一个子门类的代码前缀
    const firstCode = selectedCategoryUniqueKeys.value[0];
    const parts = firstCode.split(':');
    categoryCodePart = parts[0]; // 取代码部分
  }
  if (categoryCodePart) parts.push(categoryCodePart);

  // 组卷单元
  if (form.value.zuojuanUnit) parts.push(form.value.zuojuanUnit + '项目');

  // 案卷号
  if (form.value.juanhao) parts.push(form.value.juanhao);

  // 件号
  if (form.value.jianhao) parts.push(form.value.jianhao.toString());

  form.value.danghao = parts.join('-');
}

function getDanghaoParts() {
  const fondsCode = getFondsCode(form.value.fondsId) || '全宗编号';
  const retentionCode = getRetentionPeriodCode(form.value.retentionPeriod) || '保管期限';
  // const categoryCode = getCategoryCode(form.value.categoryId) || 'SS·类别号';
  const categoryCode = getCategoryCode(categoryId.value) || 'SS·类别号';
  const zuojuanUnit = form.value.zuojuanUnit || '组卷单元';
  const juanhao = form.value.juanhao || '案卷号';
  const jianhao = form.value.jianhao || '件号';

  return `档号格式: ${fondsCode} - ${retentionCode} - ${categoryCode} - ${zuojuanUnit}项目 - ${juanhao} - ${jianhao}`;
}

// 方法定义
async function getCategoryRootList() {
  try {
    const response = await getCategoryRoots();
    if (!form.value.fondsId) {
      categoryRoots.value = []; // 初始状态为空，只有选择全宗后才显示对应门类
    }

    categoryRoots.value = []; // 初始状态为空，只有选择全宗后才显示对应门类
    console.log('所有档案门类列表:', allCategoryRoots.value);
    return response; // 返回结果供后续链式调用
  } catch (error) {
    console.error('获取档案门类失败:', error);
    throw error; // 抛出错误供上层捕获
  }

}

async function getFondsList() {
  try {
    const response = await listFonds({});
    fondsList.value = response.rows;
    return response;
  } catch (error) {
    console.error('获取全宗列表失败:', error);
    throw error;
  }
}

async function getProjectList() {
  try {
    const response = await listProject({
      pageNum: 1,
      pageSize: 100,
      status: '0'
    });
    if (response.code === 200) {
      projectList.value = response.rows || [];
    } else {
      proxy.$modal.msgError(response.msg || "获取项目列表失败");
    }
    return response;
  } catch (error) {
    console.error("获取项目列表失败:", error);
    proxy.$modal.msgError("项目列表加载失败");
    throw error;
  }
}

function handelGenerateDanghao() {
  // 这个方法现在已经不需要了，因为档号是自动生成的
  updateDanghao();
}

function handelRfid() {
  const length = 16;
  const characters = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
  let result = '';
  const charactersLength = characters.length;
  for (let i = 0; i < length; i++) {
    result += characters.charAt(Math.floor(Math.random() * charactersLength));
  }
  form.value.rfid = result;
}

function showDialog() {
  dialogFormVisible.value = true;
}

function cancelDialog() {
  dialogFormVisible.value = false;
  extraInfoList.value = [{ label: '', value: '' }];
}

function addExtraInfo() {
  extraInfoList.value.push({ label: '', value: '' });
}

function removeExtraInfo(index) {
  extraInfoList.value.splice(index, 1);
}

function saveExtraInfoList() {
  if (extraInfoList.value.length === 0) {
    proxy.$message({
      message: '请至少添加一个额外信息',
      type: 'warning'
    });
    return;
  }
  form.value.extraInfo = JSON.stringify(extraInfoList.value);
  proxy.$modal.msgSuccess("额外信息保存成功");
  dialogFormVisible.value = false;
}

function cancel() {
    // 删除当前 tab
    if (tagsViewStore) {
        tagsViewStore.delView(route);
    }
    // 跳转到列表页
    router.back();
}

function reset() {
  form.value = {
    id: null,
    danghao: null,
    name: null,
    jianhao: 1,
    secretLevel: null,
    secretPeroid: null,
    desecretDate: null,
    retentionPeriod: null,
    carrierType: null,
    startDate: null,
    endDate: null,
    shitiLocation: null,
    exactLocation: null,
    dianziLocation: null,
    description: null,
    formationUnit: null,
    transferUnit: null,
    documentCount: null,
    directory: null,
    categoryId: null,
    fondsId: null,
    rfid: null,
    projectId: null,
    remark: null,
    zuojuanUnit: null,
    juanhao: null
  };
  proxy.resetForm("archiveRef");
}

function submitForm() {
  // 验证是否选择了全宗
  if (!form.value.fondsId) {
    proxy.$message({
      message: '请先选择所属全宗',
      type: 'warning'
    });
    return;
  }

  if (!form.value.danghao) {
    proxy.$message({
      message: '请完善档号信息，档号不能为空',
      type: 'warning'
    });
    return;
  }

  proxy.$refs["archiveRef"].validate(valid => {
    if (valid) {
      // 构建完整的门类信息
      let categoryInfo = '';

      // 如果有大门类信息和子门类代码
      if (form.value.categoryUniqueKey && selectedCategoryUniqueKeys.value.length > 0) {
        const rootParts = form.value.categoryUniqueKey.split(':');
        const rootCode = rootParts[0];
        const rootName = rootParts[1] || '';

        // 处理子门类：只取代码部分，不取名称
        const subClassCodes = selectedCategoryUniqueKeys.value.map(uniqueKey => {
          const parts = uniqueKey.split(':');
          return parts[0]; // 只返回代码部分
        });

        categoryInfo = `${rootCode}:${rootName},${subClassCodes.join(',')}`;
      }
      // 如果只有子门类代码（兼容旧格式）
      else if (selectedCategoryUniqueKeys.value.length > 0) {
        // 处理子门类：只取代码部分，不取名称
        const subClassCodes = selectedCategoryUniqueKeys.value.map(uniqueKey => {
          const parts = uniqueKey.split(':');
          return parts[0]; // 只返回代码部分
        });

        categoryInfo = subClassCodes.join(',');
      }

      // 创建提交数据
      const submitData = {
        ...form.value,
        categoryId: categoryInfo, // 使用完整的门类信息
        updateTime: dayjs().format("YYYY-MM-DD HH:mm:ss")
      };

      updateArchive(submitData).then(response => {
        proxy.$modal.msgSuccess("修改成功");
        // 删除当前 tab
        if (tagsViewStore) {
          tagsViewStore.delView(route);
        }
        // 跳转到列表页
        router.back();
      }).catch(error => {
        console.error('修改档案失败:', error);
        proxy.$modal.msgError('修改档案失败');
      });
    }
  });
}

// 监听门类选择变化，自动填充档案名称
watch(() => form.value.categoryId, (newCategoryId) => {
  if (newCategoryId) {
    // 根据选择的门类code找到对应的门类对象
    const selectedCategory = categoryRoots.value.find(category => category.code === newCategoryId);
    if (selectedCategory) {
      // 将门类的name赋值给档案名称
      form.value.name = selectedCategory.name;
    }
  } else {
    // 如果清空了门类选择，也清空档案名称
    form.value.name = null;
  }
}, { immediate: false });

// 监听项目选择变化，自动填充组卷单元
watch(() => form.value.projectId, (newProjectId) => {
  if (newProjectId) {
    // 根据选择的项目ID找到对应的项目对象
    const selectedProject = projectList.value.find(project => project.id === newProjectId);
    if (selectedProject) {
      // 将项目的name赋值给组卷单元
      form.value.zuojuanUnit = selectedProject.name;
    }
  } else {
    // 如果清空了项目选择，也清空组卷单元
    form.value.zuojuanUnit = null;
  }
}, { immediate: false });

</script>

<style scoped>
.archive-edit-container {
  padding: 20px;
  background-color: #f5f5f5;
  min-height: 100vh;
}

.form-card {
  max-width: 1200px;
  margin: 0 auto;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.card-header h3 {
  margin: 0;
  color: #303133;
  font-weight: 600;
}

.archive-form {
  padding: 20px 0;
}

.form-section {
  margin-bottom: 40px;
  padding: 20px;
  background-color: #fafafa;
  border-radius: 8px;
  border-left: 4px solid #409eff;
}

.section-title {
  margin: 0 0 20px 0;
  color: #409eff;
  font-size: 16px;
  font-weight: 600;
  display: flex;
  align-items: center;
}

.section-title::before {
  content: '';
  width: 4px;
  height: 16px;
  background-color: #409eff;
  margin-right: 8px;
  border-radius: 2px;
}

.location-selector {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.location-item {
  flex: 1;
  min-width: 140px;
}

.option-code {
  float: right;
  color: #8492a6;
  font-size: 13px;
}

.danghao-preview {
  margin-top: 16px;
}

.form-footer {
  text-align: center;
  padding: 20px 0;
  border-top: 1px solid #ebeef5;
  margin-top: 20px;
}

.extra-info-form {
  max-height: 400px;
  overflow-y: auto;
}

.extra-info-item {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 16px;
  padding: 16px;
  background-color: #f8f9fa;
  border-radius: 6px;
  border: 1px solid #e9ecef;
}

.extra-info-item .el-form-item {
  flex: 1;
  margin-bottom: 0;
}

.remove-btn {
  flex-shrink: 0;
}

.add-info-btn {
  text-align: center;
  margin-top: 16px;
}

.dialog-footer {
  text-align: right;
}

@media (max-width: 768px) {
  .archive-edit-container {
    padding: 10px;
  }

  .location-selector {
    flex-direction: column;
  }

  .location-item {
    min-width: auto;
  }

  .extra-info-item {
    flex-direction: column;
    align-items: stretch;
  }

  .remove-btn {
    align-self: center;
    margin-top: 8px;
  }
}
</style>