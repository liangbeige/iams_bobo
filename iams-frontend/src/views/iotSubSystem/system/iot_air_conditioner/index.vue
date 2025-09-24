<template>
  <div class="app-container custom-scroll">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="品牌名称" prop="brand">
        <el-input
          v-model="queryParams.brand"
          placeholder="请输入品牌名称"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="空调型号" prop="model">
        <el-input
          v-model="queryParams.model"
          placeholder="请输入空调型号"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="能效等级" prop="energyEfficiency">
        <el-select v-model="queryParams.energyEfficiency" placeholder="请选择能效等级" clearable>
          <el-option
            v-for="dict in dict.type.energy_efficiency"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="功率" prop="power">
        <el-input
          v-model="queryParams.power"
          placeholder="请输入功率"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="el-icon-plus"
          size="mini"
          @click="handleAdd"
          v-hasPermi="['system:iot_air_conditioner:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="el-icon-edit"
          size="mini"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['system:iot_air_conditioner:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="el-icon-delete"
          size="mini"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['system:iot_air_conditioner:remove']"
        >删除</el-button>
      </el-col>

      <el-col :span="1.5">
        <el-button
          type="info"
          plain icon="el-icon-upload2"
          size="mini"
          @click="handleImport"
          v-hasPermi="['system:iot_air_conditioner:import']"
        >导入</el-button>
      </el-col>

      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['system:iot_air_conditioner:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="iot_air_conditionerList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="序列号" align="center" prop="id" />
      <el-table-column label="品牌名称" align="center" prop="brand" />
      <el-table-column label="空调型号" align="center" prop="model" />
      <el-table-column label="能效等级" align="center" prop="energyEfficiency">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.energy_efficiency" :value="scope.row.energyEfficiency"/>
        </template>
      </el-table-column>
      <el-table-column label="功率" align="center" prop="power" />
      <el-table-column label="生产日期" align="center" prop="productionDate" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.productionDate, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="保修期" align="center" prop="warrantyPeriod" />
      <el-table-column label="价格" align="center" prop="price" />
      <el-table-column label="滤网更换周期" align="center" prop="filterReplacementCycle" />
      <el-table-column label="操作" align="center" min-width="100px" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['system:iot_air_conditioner:edit']"
          >修改</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['system:iot_air_conditioner:remove']"
          >删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total>0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 添加或修改空调设备信息，存储空调的静态属性及实时状态对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="500px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="品牌名称" prop="brand">
          <el-input v-model="form.brand" placeholder="请输入品牌名称" />
        </el-form-item>
        <el-form-item label="空调型号" prop="model">
          <el-input v-model="form.model" placeholder="请输入空调型号" />
        </el-form-item>
        <el-form-item label="能效等级" prop="energyEfficiency">
          <el-select v-model="form.energyEfficiency" placeholder="请选择能效等级">
            <el-option
              v-for="dict in dict.type.energy_efficiency"
              :key="dict.value"
              :label="dict.label"
              :value="dict.value"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="功率" prop="power">
          <el-input v-model="form.power" placeholder="请输入功率" />
        </el-form-item>
        <el-form-item label="生产日期" prop="productionDate">
          <el-date-picker clearable
            v-model="form.productionDate"
            type="date"
            value-format="yyyy-MM-dd"
            placeholder="请选择生产日期">
          </el-date-picker>
        </el-form-item>
        <el-form-item label="保修期" prop="warrantyPeriod">
          <el-input v-model="form.warrantyPeriod" placeholder="请输入保修期" />
        </el-form-item>
        <el-form-item label="价格" prop="price">
          <el-input v-model="form.price" placeholder="请输入价格" />
        </el-form-item>
        <el-form-item label="滤网更换周期" prop="filterReplacementCycle" class="custom-label">
          <el-input v-model="form.filterReplacementCycle" placeholder="请输入滤网更换周期" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>

    <!-- 用户导入对话框 -->
    <el-dialog :title="upload.title" :visible.sync="upload.open" width="400px" append-to-body>
      <el-upload
        ref="upload"
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
        <i class="el-icon-upload"></i>
        <div class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div>
        <div class="el-upload__tip text-center" slot="tip">
          <div class="el-upload__tip" slot="tip">
            <el-checkbox v-model="upload.updateSupport" />是否更新已经存在的空调设备数据
          </div>
          <span>仅允许导入xls、xlsx格式文件。</span>
          <el-link type="primary" :underline="false" style="font-size: 12px; vertical-align: baseline" @click="importTemplate">下载模板</el-link>
        </div>
      </el-upload>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitFileForm">确 定</el-button>
        <el-button @click="upload.open = false">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listIot_air_conditioner, getIot_air_conditioner, delIot_air_conditioner, addIot_air_conditioner, updateIot_air_conditioner } from "@/api/system/iot_air_conditioner";
import { getToken } from "@/utils/auth";

export default {
  name: "Iot_air_conditioner",
  dicts: ['energy_efficiency'],
  data() {
    return {
      // 遮罩层
      loading: true,
      // 选中数组
      ids: [],
      // 非单个禁用
      single: true,
      // 非多个禁用
      multiple: true,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 空调设备信息，存储空调的静态属性及实时状态表格数据
      iot_air_conditionerList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNum: 0,
        pageSize: 10,
        brand: null,
        model: null,
        energyEfficiency: null,
        power: null,
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        brand: [
          { required: true, message: "品牌名称不能为空", trigger: "blur" }
        ],
        model: [
          { required: true, message: "空调型号不能为空", trigger: "blur" }
        ],
        energyEfficiency: [
          { required: true, message: "能效等级不能为空", trigger: "change" }
        ],
        warrantyPeriod: [
          { required: true, message: "保修期不能为空", trigger: "blur" }
        ],
        price: [
          { required: true, message: "价格不能为空", trigger: "blur" }
        ],
        filterReplacementCycle: [
          { required: true, message: "滤网更换周期不能为空", trigger: "blur" }
        ],
      },
      upload: {
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
        url: process.env.VUE_APP_BASE_API + "/system/iot_air_conditioner/importData"
      },

    };
  },
  created() {
    this.getList();
  },
  methods: {
    /** 查询空调设备信息，存储空调的静态属性及实时状态列表 */
    getList() {
      this.loading = true;
      listIot_air_conditioner(this.queryParams).then(response => {
        this.iot_air_conditionerList = response.rows;
        this.total = response.total;
        this.loading = false;
      });
    },
    // 取消按钮
    cancel() {
      this.open = false;
      this.reset();
    },
    // 表单重置
    reset() {
      this.form = {
        id: null,
        brand: null,
        model: null,
        energyEfficiency: null,
        power: null,
        productionDate: null,
        warrantyPeriod: null,
        price: null,
        filterReplacementCycle: null,
      };
      this.resetForm("form");
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParams.pageNum = 1;
      this.getList();
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.resetForm("queryForm");
      this.handleQuery();
    },
    // 多选框选中数据
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.id)
      this.single = selection.length!==1
      this.multiple = !selection.length
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.reset();
      this.open = true;
      this.title = "空调设备信息";
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset();
      const id = row.id || this.ids
      getIot_air_conditioner(id).then(response => {
        this.form = response.data;
        this.open = true;
        this.title = "空调设备信息";
      });
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.id != null) {
            updateIot_air_conditioner(this.form).then(response => {
              this.$modal.msgSuccess("修改成功");
              this.open = false;
              this.getList();
            });
          } else {
            this.form.id = "SN1" + (parseInt(this.iot_air_conditionerList.slice(-1)[0].id.slice(-3)) + 1).toString().padStart(3, '0');//自增
            addIot_air_conditioner(this.form).then(response => {
              this.$modal.msgSuccess("新增成功");
              this.open = false;
              this.getList();
            });
          }
        }
      });
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      const ids = row.id || this.ids;
      this.$modal.confirm('是否确认删除空调设备信息，存储空调的静态属性及实时状态编号为"' + ids + '"的数据项？').then(function() {
        return delIot_air_conditioner(ids);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {});
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('system/iot_air_conditioner/export', {
        ...this.queryParams
      }, `iot_air_conditioner_${new Date().getTime()}.xlsx`)
    },
    /** 导入按钮操作 */
    handleImport() {
      this.upload.title = "空调设备导入";
      this.upload.open = true;
    },

    /** 下载模板操作 */
    importTemplate() {
      this.download('system/iot_air_conditioner/importTemplate', {}, `iot_air_conditioner_template_${new Date().getTime()}.xlsx`);
    },

    // 文件上传中处理
    handleFileUploadProgress(event, file, fileList) {
      this.upload.isUploading = true;
    },

    // 文件上传成功处理
    handleFileSuccess(response, file, fileList) {
      this.upload.open = false;
      this.upload.isUploading = false;
      this.$refs.upload.clearFiles();
      this.$alert(
        "<div id='alert-content' style='overflow: auto;overflow-x: hidden;max-height: 70vh;padding: 10px 20px 0;'>" + response.msg + "</div>",
        "导入结果",
        { dangerouslyUseHTMLString: true }
      ).then(() => {
        // 弹窗关闭后，查找弹窗内容并绑定点击事件
        const alertContent = document.querySelector('#alert-content');
        if (alertContent) {
          alertContent.addEventListener('click', () => {
            this.getList();
          });
        }
      });
    },

    // 提交上传文件
    submitFileForm() {
      this.$refs.upload.submit();
    }
  }
};
</script>

<style scoped>
.custom-scroll {
  height: 800px;
  overflow-y: auto;
}
</style>
<!--<style scoped>-->
<!--/* 使用 ::v-deep 穿透样式作用域 */-->
<!--::v-deep .custom-label .el-form-item__label {-->
<!--  font-size: 12px; /* 设置字体大小 */-->
<!--  color: #333; /* 设置字体颜色 */-->
<!--}-->
<!--</style>-->
