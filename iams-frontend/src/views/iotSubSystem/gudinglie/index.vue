<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="100px">
      <el-form-item label="区号" prop="quNo">
        <el-input
          v-model="queryParams.quNo"
          placeholder="请输入区号"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="固定列列号" prop="gdlNo">
        <el-input
          v-model="queryParams.gdlNo"
          placeholder="请输入固定列列号"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="列编号规则" prop="colNoRule">
        <el-select
          v-model="queryParams.colNoRule"
          placeholder="请选择列编号规则"
          clearable
        >
          <el-option
            v-for="item in colNoRuleOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

<!--    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="el-icon-plus"
          size="mini"
          @click="handleAdd"
          v-hasPermi="['system:column:add']"
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
          v-hasPermi="['system:column:edit']"
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
          v-hasPermi="['system:column:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['system:column:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>-->

    <el-table v-loading="loading" :data="columnList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="区号" align="center" prop="quNo" />
      <el-table-column label="列数" align="center" prop="colCn" />
      <el-table-column label="固定列列号" align="center" prop="gdlNo" />
      <el-table-column label="节数" align="center" prop="leCn" />
      <el-table-column label="层数" align="center" prop="divCn" />
      <el-table-column label="列编号规则" align="center" prop="colNoRule">
        <template slot-scope="scope">
          <dict-tag :options="colNoRuleOptions" :value="scope.row.colNoRule"/>
        </template>
      </el-table-column>
      <el-table-column label="起始列号" align="center" prop="firstColNo" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['system:column:edit']"
          >修改</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['system:column:remove']"
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

    <!-- 添加或修改固定列配置对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="500px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="区号" prop="quNo">
          <el-input v-model="form.quNo" placeholder="请输入区号" />
        </el-form-item>
        <el-form-item label="列数" prop="colCn">
          <el-input-number v-model="form.colCn" :min="1" placeholder="请输入列数" />
        </el-form-item>
        <el-form-item label="固定列列号" prop="gdlNo">
          <el-input v-model="form.gdlNo" placeholder="请输入固定列列号" />
        </el-form-item>
        <el-form-item label="节数" prop="leCn">
          <el-input-number v-model="form.leCn" :min="1" placeholder="请输入节数" />
        </el-form-item>
        <el-form-item label="层数" prop="divCn">
          <el-input-number v-model="form.divCn" :min="1" placeholder="请输入层数" />
        </el-form-item>
        <el-form-item label="列编号规则" prop="colNoRule">
          <el-select v-model="form.colNoRule" placeholder="请选择列编号规则">
            <el-option
              v-for="item in colNoRuleOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="起始列号" prop="firstColNo">
          <el-input v-model="form.firstColNo" placeholder="请输入起始列号" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listColumn, getColumn, delColumn, addColumn, updateColumn } from "@/api/system/columnConfig";

export default {
  props: {
    ip: {
      type: String,
      required: true
    },
    port: {
      type: String,
      required: true
    }
  },
  name: "ColumnConfig",
  data() {
    return {
      // 遮罩层
      loading: false,
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
      // 固定列配置表格数据
      columnList: [],
      // 列编号规则字典
      colNoRuleOptions: [
        { value: '1', label: '从左到右' },
        { value: '2', label: '从右到左' }
      ],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        quNo: null,
        gdlNo: null,
        colNoRule: null
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        quNo: [
          { required: true, message: "区号不能为空", trigger: "blur" }
        ],
        colCn: [
          { required: true, message: "列数不能为空", trigger: "blur" }
        ],
        colNoRule: [
          { required: true, message: "列编号规则不能为空", trigger: "change" }
        ]
      }
    };
  },
  created() {
    // this.getList();
  },
  methods: {
    /** 查询固定列配置列表 */
    getList() {
      this.loading = true;
      listColumn(this.ip, this.port).then(response => {
        this.columnList = response.rows;
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
        quNo: null,
        colCn: null,
        gdlNo: null,
        leCn: null,
        divCn: null,
        colNoRule: null,
        firstColNo: null
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
      this.ids = selection.map(item => item.quNo)
      this.single = selection.length!==1
      this.multiple = !selection.length
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.reset();
      this.open = true;
      this.title = "添加固定列配置";
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset();
      const quNo = row.quNo || this.ids
      getColumn(quNo).then(response => {
        this.form = response.data;
        this.open = true;
        this.title = "修改固定列配置";
      });
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.quNo != null) {
            updateColumn(this.form).then(response => {
              this.$modal.msgSuccess("修改成功");
              this.open = false;
              this.getList();
            });
          } else {
            addColumn(this.form).then(response => {
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
      const quNos = row.quNo || this.ids;
      this.$modal.confirm('是否确认删除固定列配置编号为"' + quNos + '"的数据项？').then(function() {
        return delColumn(quNos);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {});
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('system/column/export', {
        ...this.queryParams
      }, `column_${new Date().getTime()}.xlsx`)
    }
  }
};
</script>
