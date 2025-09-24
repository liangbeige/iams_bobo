<template>
  <div class="archive-add-container">
    <el-card class="form-card">
      <template #header>
        <div class="card-header">
          <h3>填写档案基本信息</h3>
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
                <!-- 使用 uniqueKey 而不是 categoryId -->
                <el-select
                    v-model="form.categoryUniqueKey"
                    :disabled="!form.fondsId"
                    placeholder="请选择档案主门类"
                    style="width: 100%"
                    @change="handleCategoryChange"
                >
                  <!-- 使用 uniqueKey 作为值和键 -->
                  <el-option
                      v-for="category in categoryRoots"
                      :key="category.uniqueKey"
                      :label="`${category.code}:${category.name}`"
                      :value="category.uniqueKey"
                  />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="选择门类">
                <!-- 使用 uniqueKey 而不是 code -->
                <el-tree-select
                    ref="treeSelect"
                    v-model="selectedCategoryUniqueKeys"
                    :disabled="!form.categoryUniqueKey"
                    :data="categoryTreeData"
                    :props="{
        label: 'name',
        value: 'uniqueKey',
        children: 'children'
    }"
                    value-key="uniqueKey"
                    placeholder="请选择子门类"
                    style="width: 100%"
                    :render-after-expand="false"
                    multiple
                    :check-strictly="false"
                    show-checkbox
                    @check-change="handleCheckChange" >
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
                <el-form-item label="所属项目" prop="projectId" required>
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
<!--              <el-form-item label="射频标签号" prop="rfid">-->
<!--                <el-input v-model="form.rfid" placeholder="请输入射频标签号(可以根据档案编号按指定规则生成)">-->
<!--                  <template #append>-->
<!--                    <el-button type="primary" @click="handelRfid">-->
<!--                      自动生成-->
<!--                    </el-button>-->
<!--                  </template>-->
<!--                </el-input>-->
<!--              </el-form-item>-->
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
              添加额外信息
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

    <!-- 目录结构弹窗 -->
    <el-dialog title="目录结构" v-model="open" width="50%">
      <el-tree :data="directory" :default-expand-all="true" :expand-on-click-node="true" />
      <template #footer>
        <el-button @click="closeDirectoryDialog">关 闭</el-button>
      </template>
    </el-dialog>

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

<script setup name="add">
import { Plus, Minus } from '@element-plus/icons-vue';
import { ref, reactive, onMounted, watch } from 'vue'
import { addArchive, getNextArchiveNumber, getRfid } from '@/api/manage/archive'
import { listDirectory } from "@/api/manage/directory";
import { listCategory } from "@/api/manage/category";
import { listFonds } from "@/api/manage/fonds";
import { updateCabinetSize } from "@/api/manage/cabinet";
import { listProject } from "@/api/manage/project";
// import { getCategoryRoots } from '@/api/manage/treeCategory.js' // 引入新的API方法
import { getCategoryList, getCategoryRoots, getCategoryTreeByCode,getCategoryTreeByUniqueKey } from "@/api/manage/treeCategory.js";
import { getFondsCategories } from "@/api/manage/category"; // 新增：获取全宗门类配置
import { dayjs } from 'element-plus';
import useTagsViewStore from '@/store/modules/tagsView.js'
import {useRoute, useRouter} from "vue-router";

const tagsViewStore = useTagsViewStore();
const route = useRoute();
const router = useRouter();

const { proxy } = getCurrentInstance()
const {
  iams_carrier_type,
  iams_retention_period,
  iams_secret_period,
  iams_secret_level
} = proxy.useDict('iams_carrier_type', 'iams_retention_period', 'iams_secret_period', 'iams_secret_level');

// 响应式数据
const projectList = ref([]);
const directoryList = ref([]);
const categoryRoots = ref([]); // 当前可选的门类根节点（会根据全宗变化）
const allCategoryRoots = ref([]); // 所有门类根节点（用于备份）
const fondsList = ref([]);
const loading = ref(true);
const total = ref(0);
const open = ref(false);
const directory = ref(null);
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
    danghao: null,
    name: null,
    jianhao: 1,
    secretLevel: null,
    secretPeroid: null,
    desecretDate: null,
    retentionPeriod: null,
    carrierType: null,
    endDate: null,
    startDate: null,
    shitiLocation: null,
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
    zuojuanUnit: null,
    juanhao: null
  },
  // rules: {
  //   name: [
  //     { required: true, message: "档案名称不能为空", trigger: "blur" }
  //   ],
  //   carrierType: [
  //     { required: true, message: "载体类型不能为空", trigger: "blur" }
  //   ],
  //   jianhao: [
  //     { required: true, message: "件号不能为空", trigger: "blur" },
  //     { type: 'number', min: 1, max: 9999, message: "件号必须在1-9999之间", trigger: "blur" }
  //   ],
  //   fondsId: [
  //     { required: true, message: "请先选择所属全宗", trigger: "change" }
  //   ],
  //
  // },
    rules: {
        // --- 核心与分类信息 ---
        fondsId: [
            { required: true, message: "请先选择所属全宗", trigger: "change" }
        ],
        categoryUniqueKey: [
            { required: true, message: "档案门类不能为空", trigger: "change" }
        ],
        carrierType: [
            { required: true, message: "载体类型不能为空", trigger: "change" }
        ],
        retentionPeriod: [
            { required: true, message: "保管期限不能为空", trigger: "change" }
        ],
        // --- 基础信息 ---
        name: [
            { required: true, message: "档案名称不能为空", trigger: "blur" }
        ],
        projectId: [
            { required: true, message: "所属项目不能为空", trigger: "change" }
        ],
        startDate: [
            { required: true, message: "电子建档时间不能为空", trigger: "change" }
        ],
        // --- 保密信息 ---
        secretLevel: [
            { required: true, message: "保密级别不能为空", trigger: "change" }
        ],
        secretPeroid: [
            { required: true, message: "保密期限不能为空", trigger: "change" }
        ],
        // --- 单位信息 ---
        formationUnit: [
            { required: true, message: "形成单位不能为空", trigger: "blur" }
        ],
        transferUnit: [
            { required: true, message: "移交单位不能为空", trigger: "blur" }
        ],
        // --- 编号信息 ---
        zuojuanUnit: [
            { required: true, message: "组卷单元不能为空", trigger: "blur" }
        ],
        juanhao: [
            { required: true, message: "案卷号不能为空", trigger: "blur" }
        ],
        jianhao: [
            { required: true, message: "件号(册数)不能为空", trigger: "blur" },
            { type: 'number', min: 1, max: 9999, message: "件号必须在1-9999之间", trigger: "blur" }
        ],
        // --- 详细信息 ---
        // 注意：简介和备注通常是可选的，但根据您的要求，这里也添加了非空校验
        description: [
            { required: true, message: "档案简介不能为空", trigger: "blur" }
        ],
        remark: [
            { required: true, message: "备注不能为空", trigger: "blur" }
        ]
    },
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    name: null,
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
  queryParams,
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

// 档案门类多选相关===========================================
// 分类树数据
const categoryTreeData = ref([])
const selectedCategoryUniqueKeys = ref([]); // 使用 uniqueKey 而不是 code
// 当前选中的门类 codes（数组）
const selectedCategoryCodes = ref([])


// 监听全宗选择变化 - 新增核心功能
watch(() => form.value.fondsId, async (newFondsId, oldFondsId) => {
  console.log('全宗选择变化:', newFondsId, '原值:', oldFondsId);

  // 清空相关选择
  form.value.categoryId = null;
  selectedCategoryCodes.value = [];
  categoryTreeData.value = [];

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

// 处理全宗选择变化的函数
const handleFondsChange = async (fondsId) => {
  console.log('全宗选择变化:', fondsId);

  // 清空相关选择
  form.value.categoryId = null;
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
    } else {
      categoryTreeData.value = [];
      proxy.$modal.msgWarning('该门类暂无子分类');
    }
  } catch (error) {
    console.error('获取子门类失败:', error);
    proxy.$modal.msgError('获取子门类失败');
  }
};

// 新增：加载全宗门类模板的方法
async function loadFondsCategoryTemplate(fondsId) {
  try {
    console.log('开始加载全宗门类模板，fondsId:', fondsId);
    const response = await getFondsCategories(fondsId);
    console.log('全宗门类模板响应:', response);

    if (response.data && response.data.length > 0) {
      // 为每个门类添加 uniqueKey
      categoryRoots.value = response.data.map(item => ({
        ...item,
        uniqueKey: `${item.code}:${item.name}`
      }));
      console.log('设置全宗门类模板:', categoryRoots.value);
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


// 监听值变化（可选）
watch(selectedCategoryCodes, (newVal) => {
  console.log('当前选中门类:', newVal);
});

// 在script部分添加加载分类树的方法
async function loadCategoryTree(categoryCode) {
  try {
    // 清空之前的选择
    selectedCategoryCodes.value = [];

    // 加载分类树数据
    const response = await getCategoryTreeByCode(categoryCode)
    if (response.data) {
      categoryTreeData.value = Array.isArray(response.data) ? response.data : [response.data]
    } else {
      categoryTreeData.value = []
    }
  } catch (error) {
    console.error('加载分类树失败:', error)
    proxy.$modal.msgError('加载分类树失败')
  }
}



// 生命周期
onMounted(() => {
  getCategoryRootList();
  getFondsList();
  getProjectList();
});

// 监听位置变化
watch([quHao, lieHao, ceHao, jieHao, cengHao], ([newQuHao, newLieHao, newCeHao, newJieHao, newCengHao]) => {
  if (newQuHao && newLieHao && newCeHao && newCengHao && newJieHao) {
    form.value.shitiLocation = `${newQuHao}-${newLieHao}-${newCeHao}`;
    form.value.exactLocation = `${newJieHao}-${newCengHao}`;
    console.log(form.value.shitiLocation);
  }
}, { immediate: false });

// 监听档号相关字段变化，自动更新档号
watch([
  () => form.value.fondsId,
  () => form.value.retentionPeriod,
  () => form.value.categoryId,
  () => form.value.zuojuanUnit,
  () => form.value.juanhao,
  () => form.value.jianhao
], () => {
  updateDanghao();
}, { immediate: false });

// 处理树选择变化事件
const handleCheckChange = (node, checked, indeterminate) => {
  console.log('节点选中变化:', {
    node,
    checked,
    indeterminate
  });
  // 获取所有选中的节点
  const checkedNodes = proxy.$refs.treeSelect.getCheckedNodes();
  console.log('所有选中节点:', checkedNodes);

  // 提取选中的唯一键
  selectedCategoryUniqueKeys.value = checkedNodes.map(node => node.uniqueKey);

  // 提取选中的代码（如果需要）
  selectedCategoryCodes.value = checkedNodes.map(node => node.code);

  console.log('选中的门类:', {
    uniqueKeys: selectedCategoryUniqueKeys.value,
    codes: selectedCategoryCodes.value
  });
};

// 档号生成相关方法
const getRetentionPeriodCode = (retentionPeriod) => {
  // 确保将值转换为字符串进行比较
  const period = String(retentionPeriod)
  const codeMap = { '200': 'Y', '50': 'D' }
  return codeMap[period] || period || ''
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
  } else if (selectedCategoryCodes.value.length > 0) {
    // 如果没有大门类，使用第一个子门类的代码前缀
    const firstCode = selectedCategoryCodes.value[0];
    categoryCodePart = firstCode.substring(0, 1); // 取第一个字符作为门类代码
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
  const categoryCode = getCategoryCode(form.value.categoryId) || 'SS·类别号';
  const zuojuanUnit = form.value.zuojuanUnit || '组卷单元';
  const juanhao = form.value.juanhao || '案卷号';
  const jianhao = form.value.jianhao || '件号';

  return `档号格式: ${fondsCode} - ${retentionCode} - ${categoryCode} - ${zuojuanUnit}项目 - ${juanhao} - ${jianhao}`;
}

// 方法定义
function getDirectoryList() {
  loading.value = true;
  listDirectory(queryParams.value).then(response => {
    directoryList.value = response.rows;
    total.value = response.total;
    loading.value = false;
  });
}

function getCategoryRootList() {
  getCategoryRoots().then(response => {
    allCategoryRoots.value = response.data; // 保存所有门类
    // 初始状态下如果没有选择全宗，显示所有门类
    if (!form.value.fondsId) {
      categoryRoots.value = []; // 初始状态为空，只有选择全宗后才显示对应门类
    }
    console.log('所有档案门类列表:', allCategoryRoots.value);
  }).catch(error => {
    console.error('获取档案门类失败:', error);
  });
}

function getFondsList() {
  listFonds({}).then(response => {
    fondsList.value = response.rows;
  });
}

function getProjectList() {
  listProject({
    pageNum: 1,
    pageSize: 100,
    status: '0'
  }).then(response => {
    if (response.code === 200) {
      projectList.value = response.rows || [];
    } else {
      proxy.$modal.msgError(response.msg || "获取项目列表失败");
    }
  }).catch(error => {
    console.error("获取项目列表失败:", error);
    proxy.$modal.msgError("项目列表加载失败");
  });
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

function selectArchiveDirectory(row) {
  form.value.directory = row.structure;
}

function viewTreeDirectory(row) {
  directory.value = JSON.parse(row.structure);
  open.value = true;
}

function closeDirectoryDialog() {
  open.value = false;
  directory.value = null;
}

function cancel() {
    // 删除当前 tab
    if (tagsViewStore) {
        tagsViewStore.delView(route);
    }
    // 跳转到列表页
    router.back();
  // reset();
}

function reset() {
  form.value = {
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
    dianziLocation: null,
    description: null,
    formationUnit: null,
    transferUnit: null,
    documentCount: null,
    directory: null,
    categoryId: null,
    fondsId: null,
    zuojuanUnit: null,
    juanhao: null
  };
  proxy.resetForm("archiveRef");
}

// function submitForm() {
//   // 验证是否选择了全宗
//   if (!form.value.fondsId) {
//     proxy.$message({
//       message: '请先选择所属全宗',
//       type: 'warning'
//     });
//     return;
//   }
//
//   if (!form.value.danghao) {
//     proxy.$message({
//       message: '请完善档号信息，档号不能为空',
//       type: 'warning'
//     });
//     return;
//   }
//
//   proxy.$refs["archiveRef"].validate(valid => {
//     if (valid) {
//       form.value = {
//         ...form.value,
//         createTime: dayjs().format("YYYY-MM-DD HH:mm:ss"),
//         categoryId: selectedCategoryCodes.value.join(','), // 替换为你想设置的新 categoryId
//         juanhao: form.value.juanhao
//       }
//       addArchive(form.value).then(response => {
//         proxy.$modal.msgSuccess("新增成功");
//         proxy.$router.push({ path: "/manage/archive/arc-list" });
//       });
//     }
//   });
// }

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
        // 使用唯一键构建门类信息
        categoryInfo = `${rootCode}:${rootName},${subClassCodes.join(',')}`;
      }
      // 如果只有子门类代码（兼容旧格式）
      else if (selectedCategoryUniqueKeys.value.length > 0) {
        categoryInfo = selectedCategoryUniqueKeys.value.join(',');
      }

      // 调试日志
      console.log('提交的门类信息:', categoryInfo);

      // 创建新对象，不要直接修改form.value
      const submitData = {
        ...form.value,
        createTime: dayjs().format("YYYY-MM-DD HH:mm:ss"),
        categoryId: categoryInfo, // 使用完整的门类信息
        juanhao: form.value.juanhao
      };

      // 调试日志
      console.log('提交的门类信息:', submitData.categoryId);

      addArchive(submitData).then(response => {
        proxy.$modal.msgSuccess("新增成功");

        // 删除当前 tab
        if (tagsViewStore) {
          tagsViewStore.delView(route);
        }
        // 跳转到列表页
        router.back();
      }).catch(error => {
        console.error('新增档案失败:', error);
        proxy.$modal.msgError('新增档案失败');
      });
    }
  });
}
// 监听门类选择变化，自动填充档案名称
watch(() => form.value.categoryId, (newCategoryId) => {
  console.log('门类选择变化:', newCategoryId); // ✅ 修正变量名
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
  const name = form.value.name;
  if (newProjectId) {
    // 根据选择的项目ID找到对应的项目对象
    const selectedProject = projectList.value.find(project => project.id === newProjectId);
    if (selectedProject) {
      // 将项目的name赋值给组卷单元
      form.value.zuojuanUnit = selectedProject.name;
      
      form.value.name = selectedProject.name + '-' + name;
    }
  } else {
    // 如果清空了项目选择，也清空组卷单元
    form.value.zuojuanUnit = null;
    form.value.name = name;
  }
}, { immediate: false });

</script>

<style scoped>
.archive-add-container {
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
  .archive-add-container {
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

  .form-tip {
    margin-top: 4px;
  }

  .form-tip .el-text {
    font-size: 12px;
  }

  /* 禁用状态的样式 */
  .el-select.is-disabled .el-select__wrapper {
    background-color: #f5f7fa;
    border-color: #e4e7ed;
    color: #c0c4cc;
    cursor: not-allowed;
  }

  .remove-btn {
    align-self: center;
    margin-top: 8px;
  }
}

.node-code {
  color: #409EFF;
  margin-right: 8px;
  font-weight: 500;
}

.node-name {
  color: #606266;
}
</style>
