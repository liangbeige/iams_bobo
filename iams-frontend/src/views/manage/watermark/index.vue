<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="水印内容" prop="content">
        <el-input v-model="queryParams.content" placeholder="请输入水印内容" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd"
          v-hasPermi="['manage:watermark:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate"
          v-hasPermi="['manage:watermark:edit']">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete"
          v-hasPermi="['manage:watermark:remove']">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Download" @click="handleExport"
          v-hasPermi="['manage:watermark:export']">导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="watermarkList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="水印ID" align="center" prop="id" />
      <el-table-column label="水印内容" align="center" prop="content" />
      <el-table-column label="水印颜色" align="center" prop="color" />
      <el-table-column label="水印字体大小" align="center" prop="fontsize" />
      <el-table-column label="水印字重" align="center" prop="fontweight" />
      <el-table-column label="水印字体" align="center" prop="fontfamily" />
      <el-table-column label="水印字体样式" align="center" prop="fontstyle" />
      <el-table-column label="旋转角度" align="center" prop="rotate" />
      <el-table-column label="水平间距" align="center" prop="gapX" />
      <el-table-column label="垂直间距" align="center" prop="gapY" />
      <el-table-column label="水平偏移" align="center" prop="offsetX" />
      <el-table-column label="垂直偏移" align="center" prop="offsetY" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" @click="handleUpdate(scope.row)"
            v-hasPermi="['manage:watermark:edit']">修改</el-button>
          <el-button link type="primary" @click="handleDelete(scope.row)"
            v-hasPermi="['manage:watermark:remove']">删除</el-button>
          <el-button link type="primary" @click="handleDefault(scope.row)">设为默认</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 添加或修改水印管理对话框 -->
    <el-dialog :title="title" v-model="open" width="500px" append-to-body>
      <el-form ref="watermarkRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="水印内容" prop="content">
          <el-input v-model="form.content" placeholder="请输入水印内容" />
        </el-form-item>
        <el-form-item label="水印颜色" prop="color">
          <el-input v-model="form.color" placeholder="请输入水印颜色" />
        </el-form-item>
        <el-form-item label="水印字体大小" prop="fontsize">
          <el-input v-model="form.fontsize" placeholder="请输入水印字体大小" />
        </el-form-item>
        <el-form-item label="水印字重" prop="fontweight">
          <el-input v-model="form.fontweight" placeholder="请输入水印字重" />
        </el-form-item>
        <el-form-item label="水印字体" prop="fontfamily">
          <el-input v-model="form.fontfamily" placeholder="请输入水印字体" />
        </el-form-item>
        <el-form-item label="水印字体样式" prop="fontstyle">
          <el-input v-model="form.fontstyle" placeholder="请输入水印字体样式" />
        </el-form-item>
        <el-form-item label="旋转角度" prop="rotate">
          <el-input v-model="form.rotate" placeholder="请输入旋转角度" />
        </el-form-item>
        <el-form-item label="水平间距" prop="gapX">
          <el-input v-model="form.gapX" placeholder="请输入水平间距" />
        </el-form-item>
        <el-form-item label="垂直间距" prop="gapY">
          <el-input v-model="form.gapY" placeholder="请输入垂直间距" />
        </el-form-item>
        <el-form-item label="水平偏移" prop="offsetX">
          <el-input v-model="form.offsetX" placeholder="请输入水平偏移" />
        </el-form-item>
        <el-form-item label="垂直偏移" prop="offsetY">
          <el-input v-model="form.offsetY" placeholder="请输入垂直偏移" />
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
  </div>
</template>

<script setup name="Watermark">
import { listWatermark, getWatermark, delWatermark, addWatermark, updateWatermark, setDefaultWatermark } from "@/api/manage/watermark";

const { proxy } = getCurrentInstance();

const watermarkList = ref([]);
const open = ref(false);
const loading = ref(true);
const showSearch = ref(true);
const ids = ref([]);
const single = ref(true);
const multiple = ref(true);
const total = ref(0);
const title = ref("");

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    content: null,
  },
  rules: {
    content: [
      { required: true, message: "水印内容不能为空", trigger: "blur" }
    ],
  }
});

const { queryParams, form, rules } = toRefs(data);

/** 查询水印管理列表 */
function getList() {
  loading.value = true;
  listWatermark(queryParams.value).then(response => {
    watermarkList.value = response.rows;
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
    content: null,
    color: null,
    fontsize: null,
    fontweight: null,
    fontfamily: null,
    fontstyle: null,
    rotate: null,
    gapX: null,
    gapY: null,
    offsetX: null,
    offsetY: null,
    createTime: null,
    createBy: null,
    updateTime: null,
    updateBy: null,
    remark: null
  };
  proxy.resetForm("watermarkRef");
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
  proxy.$router.push({ path: '/manage/watermark/add' });
}

/** 修改按钮操作 */
// function handleUpdate(row) {
//   reset();
//   const _id = row.id || ids.value
//   getWatermark(_id).then(response => {
//     form.value = response.data;
//     open.value = true;
//     title.value = "修改水印管理";
//   });
// }
function handleUpdate(row) {
  const id = row?.id || (ids.value.length > 0 ? ids.value[0] : null);
  if (!id) {
    proxy.$modal.msgError("请选择一个条目进行修改");
    return;
  }
  proxy.$router.push({ path: '/manage/watermark/edit/', query: { id } });
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["watermarkRef"].validate(valid => {
    if (valid) {
      if (form.value.id != null) {
        updateWatermark(form.value).then(response => {
          proxy.$modal.msgSuccess("修改成功");
          open.value = false;
          getList();
        });
      } else {
        addWatermark(form.value).then(response => {
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
  const _ids = row.id || ids.value;
  proxy.$modal.confirm('是否确认删除水印管理编号为"' + _ids + '"的数据项？').then(function () {
    return delWatermark(_ids);
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess("删除成功");
  }).catch(() => { });
}

/** 导出按钮操作 */
function handleExport() {
  proxy.download('manage/watermark/export', {
    ...queryParams.value
  }, `watermark_${new Date().getTime()}.xlsx`)
}

getList();


function handleDefault(row) {
  proxy.$modal.confirm('是否确认设置该样式默认水印？').then(function () {
    return setDefaultWatermark(row.id).then(res => {
      if (res.code === 200) {
        proxy.$modal.msgSuccess("设置成功");
        getList();
      } else {
        proxy.$modal.msgError(res.msg);
      }
    });
  }).catch(error => {
    console.log("设置失败:", error);
  });
}
</script>
