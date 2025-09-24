<!--<template>-->
<!--  <div class="app-container">-->
<!--    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">-->
<!--      <el-form-item label="印章名称" prop="sealName">-->
<!--        <el-input-->
<!--            v-model="queryParams.sealName"-->
<!--            placeholder="请输入印章名称"-->
<!--            clearable-->
<!--            @keyup.enter="handleQuery"-->
<!--        />-->
<!--      </el-form-item>-->
<!--      <el-form-item label="印章类型" prop="sealType">-->
<!--        <el-select v-model="queryParams.sealType" placeholder="请选择印章类型" clearable>-->
<!--          <el-option label="公章" value="OFFICIAL" />-->
<!--          <el-option label="私章" value="PERSONAL" />-->
<!--        </el-select>-->
<!--      </el-form-item>-->
<!--      <el-form-item>-->
<!--        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>-->
<!--        <el-button icon="Refresh" @click="resetQuery">重置</el-button>-->
<!--      </el-form-item>-->
<!--    </el-form>-->

<!--    <el-row :gutter="10" class="mb8">-->
<!--      <el-col :span="1.5">-->
<!--        <el-button-->
<!--            type="primary"-->
<!--            plain-->
<!--            icon="Plus"-->
<!--            @click="handleAdd"-->
<!--            v-hasPermi="['system:seal:add']"-->
<!--        >新增</el-button>-->
<!--      </el-col>-->
<!--      <el-col :span="1.5">-->
<!--        <el-button-->
<!--            type="success"-->
<!--            plain-->
<!--            icon="Edit"-->
<!--            :disabled="single"-->
<!--            @click="handleUpdate"-->
<!--            v-hasPermi="['system:seal:edit']"-->
<!--        >修改</el-button>-->
<!--      </el-col>-->
<!--      <el-col :span="1.5">-->
<!--        <el-button-->
<!--            type="danger"-->
<!--            plain-->
<!--            icon="Delete"-->
<!--            :disabled="multiple"-->
<!--            @click="handleDelete"-->
<!--            v-hasPermi="['system:seal:remove']"-->
<!--        >删除</el-button>-->
<!--      </el-col>-->
<!--      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>-->
<!--    </el-row>-->

<!--    <el-table v-loading="loading" :data="sealList" @selection-change="handleSelectionChange">-->
<!--      <el-table-column type="selection" width="55" align="center" />-->
<!--      <el-table-column label="印章ID" align="center" prop="id" />-->
<!--      <el-table-column label="印章名称" align="center" prop="sealName" />-->
<!--      <el-table-column label="印章类型" align="center" prop="sealType">-->
<!--        <template #default="scope">-->
<!--          <el-tag :type="scope.row.sealType === 'OFFICIAL' ? 'primary' : 'success'">-->
<!--            {{ scope.row.sealType === 'OFFICIAL' ? '公章' : '私章' }}-->
<!--          </el-tag>-->
<!--        </template>-->
<!--      </el-table-column>-->
<!--      <el-table-column label="主文字" align="center" prop="mainText" />-->
<!--      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">-->
<!--        <template #default="scope">-->
<!--          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['system:seal:edit']">修改</el-button>-->
<!--          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['system:seal:remove']">删除</el-button>-->
<!--          <el-button link type="primary" icon="Picture" @click="handlePreview(scope.row)">预览</el-button>-->
<!--        </template>-->
<!--      </el-table-column>-->
<!--    </el-table>-->

<!--    <pagination-->
<!--        v-show="total>0"-->
<!--        :total="total"-->
<!--        v-model:page="queryParams.pageNum"-->
<!--        v-model:limit="queryParams.pageSize"-->
<!--        @pagination="getList"-->
<!--    />-->

<!--    &lt;!&ndash; 添加或修改印章配置对话框 &ndash;&gt;-->
<!--    <el-dialog :title="title" v-model="open" width="800px" append-to-body>-->
<!--      <el-tabs v-model="activeTab" @tab-click="handleTabChange">-->
<!--        <el-tab-pane label="公章配置" name="OFFICIAL">-->
<!--          <el-form ref="sealRef" :model="form" :rules="rules" label-width="100px">-->
<!--&lt;!&ndash;            <el-form-item label="印章类型" prop="sealType">&ndash;&gt;-->
<!--&lt;!&ndash;              <el-radio-group v-model="form.sealType">&ndash;&gt;-->
<!--&lt;!&ndash;                <el-radio label="OFFICIAL">公章</el-radio>&ndash;&gt;-->
<!--&lt;!&ndash;                <el-radio label="PERSONAL">私章</el-radio>&ndash;&gt;-->
<!--&lt;!&ndash;              </el-radio-group>&ndash;&gt;-->
<!--&lt;!&ndash;            </el-form-item>&ndash;&gt;-->
<!--            <el-form-item label="印章名称" prop="sealName">-->
<!--              <el-input v-model="form.sealName" placeholder="请输入印章名称" />-->
<!--            </el-form-item>-->
<!--            <el-form-item label="主文字" prop="mainText">-->
<!--              <el-input v-model="form.mainText" placeholder="请输入主文字内容" />-->
<!--            </el-form-item>-->
<!--            <el-form-item label="副文字" prop="viceText">-->
<!--              <el-input v-model="form.viceText" placeholder="请输入副文字内容" />-->
<!--            </el-form-item>-->
<!--            <el-form-item label="中心文字" prop="centerText">-->
<!--              <el-input v-model="form.centerText" placeholder="请输入中心文字内容" />-->
<!--            </el-form-item>-->
<!--          </el-form>-->
<!--        </el-tab-pane>-->

<!--        <el-tab-pane label="私章配置" name="PERSONAL" >-->
<!--          <el-form ref="sealRef" :model="form" :rules="rules" label-width="100px">-->
<!--&lt;!&ndash;            <el-form-item label="印章类型" prop="sealType">&ndash;&gt;-->
<!--&lt;!&ndash;              <el-radio-group v-model="form.sealType">&ndash;&gt;-->
<!--&lt;!&ndash;                <el-radio label="OFFICIAL">公章</el-radio>&ndash;&gt;-->
<!--&lt;!&ndash;                <el-radio label="PERSONAL">私章</el-radio>&ndash;&gt;-->
<!--&lt;!&ndash;              </el-radio-group>&ndash;&gt;-->
<!--&lt;!&ndash;            </el-form-item>&ndash;&gt;-->
<!--            <el-form-item label="印章名称" prop="sealName">-->
<!--              <el-input v-model="form.sealName" placeholder="请输入印章名称" />-->
<!--            </el-form-item>-->
<!--            <el-form-item label="姓名" prop="mainText">-->
<!--              <el-input v-model="form.mainText" placeholder="请输入姓名" />-->
<!--            </el-form-item>-->
<!--            <el-form-item label="附加文字" prop="additionalText">-->
<!--              <el-input v-model="form.additionalText" placeholder="如：印、章等" />-->
<!--            </el-form-item>-->
<!--          </el-form>-->
<!--        </el-tab-pane>-->
<!--      </el-tabs>-->

<!--      <div class="seal-preview-container">-->
<!--        <div class="preview-title">印章预览</div>-->
<!--        <div class="preview-content">-->
<!--          <img v-if="previewImage" :src="previewImage" class="seal-preview-image" alt="印章预览"/>-->
<!--          <div v-else class="preview-placeholder">请配置印章参数后点击预览</div>-->
<!--        </div>-->
<!--        <div class="preview-actions">-->
<!--          <el-button type="primary"-->
<!--           @click="handleGeneratePreview"-->
<!--          :disabled="previewLoading"-->
<!--      > {{ previewLoading ? '生成中...' : '生成预览' }}-->
<!--          </el-button>-->
<!--        </div>-->
<!--      </div>-->

<!--      <template #footer>-->
<!--        <div class="dialog-footer">-->
<!--          <el-button type="primary" @click="submitForm">确 定</el-button>-->
<!--          <el-button @click="cancel">取 消</el-button>-->
<!--        </div>-->
<!--      </template>-->
<!--    </el-dialog>-->

<!--    &lt;!&ndash; 大图预览对话框 &ndash;&gt;-->
<!--    <el-dialog v-model="previewDialogVisible" title="印章预览" width="50%">-->
<!--      <img :src="currentPreviewImage" style="width: 100%;" />-->
<!--    </el-dialog>-->
<!--  </div>-->
<!--</template>-->

<!--<script setup name="Seal">-->
<!--import { listSeal, getSeal, delSeal, addSeal, updateSeal, previewOfficialSeal,previewPersonalSeal } from "@/api/manage/seal";-->
<!--import { getToken } from "@/utils/auth";-->
<!--import { ElMessage } from 'element-plus';-->
<!--import { ref, reactive, toRefs, getCurrentInstance, watch } from 'vue';-->

<!--const previewLoading = ref(false);-->
<!--const activeTab = ref('OFFICIAL'); // 默认选中公章Tab-->

<!--// Tab切换时同步更新印章类型-->
<!--const handleTabChange = (tab) => {-->
<!--  form.value.sealType = tab.paneName; // OFFICIAL 或 PERSONAL-->
<!--};-->

<!--// 或者使用watch监听-->
<!--watch(activeTab, (newVal) => {-->
<!--  form.value.sealType = newVal;-->
<!--});-->

<!--const { proxy } = getCurrentInstance();-->

<!--const sealList = ref([]);-->
<!--const open = ref(false);-->
<!--const loading = ref(true);-->
<!--const showSearch = ref(true);-->
<!--const ids = ref([]);-->
<!--const single = ref(true);-->
<!--const multiple = ref(true);-->
<!--const total = ref(0);-->
<!--const title = ref("");-->
<!--const previewImage = ref("");-->
<!--const previewDialogVisible = ref(false);-->
<!--const currentPreviewImage = ref("");-->




<!--const data = reactive({-->
<!--  form: {-->
<!--    sealType: "OFFICIAL",-->
<!--    sealName: "",-->
<!--    mainText: "",-->
<!--    viceText: "",-->
<!--    centerText: "",-->
<!--    additionalText: "印",-->
<!--    mainFontFamily: "楷体",-->
<!--    mainFontSize: 25,-->
<!--    color: "#FF0000",-->
<!--    imageSize: 300-->
<!--  },-->
<!--  queryParams: {-->
<!--    pageNum: 1,-->
<!--    pageSize: 10,-->
<!--    sealName: null,-->
<!--    sealType: null-->
<!--  },-->
<!--  rules: {-->
<!--    sealName: [-->
<!--      { required: true, message: "印章名称不能为空", trigger: "blur" }-->
<!--    ],-->
<!--    mainText: [-->
<!--      { required: true, message: "主文字不能为空", trigger: "blur" }-->
<!--    ],-->
<!--    sealType: [-->
<!--      { required: true, message: "请选择印章类型", trigger: "change" }-->
<!--    ]-->
<!--  }-->
<!--});-->


<!--const { queryParams, form, rules } = toRefs(data);-->

<!--/** 查询印章列表 */-->
<!--function getList() {-->
<!--  loading.value = true;-->
<!--  listSeal(queryParams.value).then(response => {-->
<!--    sealList.value = response.rows;-->
<!--    total.value = response.total;-->
<!--    loading.value = false;-->
<!--  });-->
<!--}-->

<!--// 取消按钮-->
<!--function cancel() {-->
<!--  open.value = false;-->
<!--  reset();-->
<!--}-->

<!--// 表单重置-->
<!--function reset() {-->
<!--  form.value = {-->
<!--    id: null,-->
<!--    sealType: "OFFICIAL",-->
<!--    sealName: "",-->
<!--    mainText: "",-->
<!--    viceText: "",-->
<!--    centerText: "",-->
<!--    additionalText: "印",-->
<!--    mainFontFamily: "楷体",-->
<!--    mainFontSize: 25,-->
<!--    color: "#FF0000",-->
<!--    imageSize: 300,-->
<!--    remark: null-->
<!--  };-->
<!--  previewImage.value = "";-->
<!--  proxy.resetForm("sealRef");-->
<!--}-->

<!--/** 搜索按钮操作 */-->
<!--function handleQuery() {-->
<!--  queryParams.value.pageNum = 1;-->
<!--  getList();-->
<!--}-->

<!--/** 重置按钮操作 */-->
<!--function resetQuery() {-->
<!--  proxy.resetForm("queryRef");-->
<!--  handleQuery();-->
<!--}-->

<!--// 多选框选中数据-->
<!--function handleSelectionChange(selection) {-->
<!--  ids.value = selection.map(item => item.id);-->
<!--  single.value = selection.length != 1;-->
<!--  multiple.value = !selection.length;-->
<!--}-->

<!--/** 新增按钮操作 */-->
<!--function handleAdd() {-->
<!--  reset();-->
<!--  open.value = true;-->
<!--  title.value = "添加印章";-->
<!--  activeTab.value = "OFFICIAL";-->
<!--}-->

<!--/** 修改按钮操作 */-->
<!--function handleUpdate(row) {-->
<!--  reset();-->
<!--  const id = row.id || ids.value[0];-->
<!--  getSeal(id).then(response => {-->
<!--    form.value = response.data;-->
<!--    open.value = true;-->
<!--    title.value = "修改印章";-->
<!--    activeTab.value = form.value.sealType;-->
<!--  });-->
<!--}-->

<!--/** 预览按钮操作 */-->
<!--function handlePreview(row) {-->
<!--  currentPreviewImage.value = `${import.meta.env.VITE_APP_BASE_API}${row.imagePath}`;-->
<!--  previewDialogVisible.value = true;-->
<!--}-->

<!--/** 生成预览图 */-->
<!--function handleGeneratePreview() {-->

<!--  previewLoading.value = true;-->
<!--  // 根据印章类型调用不同API-->
<!--  const apiCall = data.form.sealType === 'OFFICIAL'  // 改用data.form-->
<!--      ? previewOfficialSeal-->
<!--      : previewPersonalSeal;-->

<!--  // 构造对应参数-->
<!--  const params = data.form.sealType === 'OFFICIAL'   // 改用data.form-->
<!--      ? {-->
<!--        mainText: data.form.mainText,               // 公章参数-->
<!--        viceText: data.form.viceText,-->
<!--        centerText: data.form.centerText,-->
<!--        // 可选：传递样式参数（与后端协调）-->
<!--        mainFontFamily: data.form.mainFontFamily,-->
<!--        color: data.form.color,-->
<!--        imageSize: data.form.imageSize-->
<!--      }-->
<!--      : {-->
<!--        name: data.form.mainText,                   // 私章参数-->
<!--        additionalText: data.form.additionalText,-->
<!--        // 可选：传递样式参数-->
<!--        mainFontFamily: data.form.mainFontFamily,-->
<!--        color: data.form.color,-->
<!--        imageSize: data.form.imageSize-->
<!--      };-->


<!--  apiCall(params)-->
<!--      .then(response => {-->
<!--        // console.log('响应数据:', response);-->
<!--        // // 直接处理 ArrayBuffer-->
<!--        // const blob = new Blob([response], { type: 'image/png' });-->
<!--        // previewImage.value = URL.createObjectURL(blob);-->
<!--        // 1. 确保是JSON结构-->
<!--        if (response.code === 200 && response.data) {-->
<!--          // 2. 直接使用Base64数据-->
<!--          previewImage.value = `data:image/png;base64,${response.data}`;-->
<!--        }-->
<!--      })-->
<!--      .catch(error => {-->
<!--        ElMessage.error(`生成失败: ${error.message}`);-->
<!--      }).finally(() => {-->
<!--    previewLoading.value = false;-->
<!--  });-->
<!--}-->

<!--/** 提交按钮 */-->
<!--function submitForm() {-->
<!--  proxy.$refs["sealRef"].validate(valid => {-->
<!--    console.log('表单验证结果:', valid); // 添加这行-->
<!--    if (valid) {-->
<!--      if (form.value.id != null) {-->
<!--        updateSeal(form.value).then(response => {-->
<!--          proxy.$modal.msgSuccess("修改成功");-->
<!--          open.value = false;-->
<!--          getList();-->
<!--        });-->
<!--      } else {-->
<!--        addSeal(form.value).then(response => {-->
<!--          proxy.$modal.msgSuccess("新增成功");-->
<!--          open.value = false;-->
<!--          getList();-->
<!--        });-->
<!--      }-->
<!--    }-->
<!--  });-->
<!--}-->

<!--/** 删除按钮操作 */-->
<!--function handleDelete(row) {-->
<!--  const ids = row.id || ids.value;-->
<!--  proxy.$modal.confirm('是否确认删除印章编号为"' + ids + '"的数据项？').then(function() {-->
<!--    return delSeal(ids);-->
<!--  }).then(() => {-->
<!--    getList();-->
<!--    proxy.$modal.msgSuccess("删除成功");-->
<!--  }).catch(() => {});-->
<!--}-->

<!--getList();-->
<!--</script>-->

<!--<style scoped>-->
<!--.seal-preview-container {-->
<!--  margin-top: 20px;-->
<!--  border: 1px solid #eee;-->
<!--  padding: 20px;-->
<!--  border-radius: 4px;-->
<!--}-->

<!--.preview-title {-->
<!--  font-size: 16px;-->
<!--  font-weight: bold;-->
<!--  margin-bottom: 15px;-->
<!--}-->

<!--.preview-content {-->
<!--  display: flex;-->
<!--  justify-content: center;-->
<!--  align-items: center;-->
<!--  min-height: 200px;-->
<!--  background-color: #f9f9f9;-->
<!--}-->

<!--.seal-preview-image {-->
<!--  max-width: 300px;-->
<!--  max-height: 300px;-->
<!--}-->

<!--.preview-placeholder {-->
<!--  color: #999;-->
<!--}-->

<!--.preview-actions {-->
<!--  margin-top: 15px;-->
<!--  text-align: center;-->
<!--}-->
<!--</style>-->

<template>
  <div>
    <!-- 页面内容 -->
  </div>
</template>

<script setup>// 可选的脚本逻辑
</script>