<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="100px">
      <el-form-item label="仓库名称" prop="warehouseName">
        <el-input
          v-model="queryParams.warehouseName"
          placeholder="请输入仓库名称"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="仓库区域位置" prop="location">
        <el-input
          v-model="queryParams.location"
          placeholder="请输入仓库区域位置"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="仓库容量" prop="capacity">
        <el-input
          v-model="queryParams.capacity"
          placeholder="请输入仓库容量"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="仓库温度" prop="temperature">
        <el-input
          v-model="queryParams.temperature"
          placeholder="请输入仓库温度"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="仓库湿度" prop="humidity">
        <el-input
          v-model="queryParams.humidity"
          placeholder="请输入仓库湿度"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="氧气含量" prop="oxygenLevel">
        <el-input
          v-model="queryParams.oxygenLevel"
          placeholder="请输入氧气含量"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="氮气含量" prop="nitrogenLevel">
        <el-input
          v-model="queryParams.nitrogenLevel"
          placeholder="请输入氮气含量"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="是否火灾" prop="isFire">
        <el-select v-model="queryParams.isFire" placeholder="请选择是否火灾" clearable>
          <el-option
            v-for="dict in dict.type.is_fire"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
<!--      <el-form-item label="最后更新时间" prop="lastUpdated">-->
<!--        <el-date-picker clearable-->
<!--          v-model="queryParams.lastUpdated"-->
<!--          type="date"-->
<!--          value-format="yyyy-MM-dd"-->
<!--          placeholder="请选择最后更新时间">-->
<!--        </el-date-picker>-->
<!--      </el-form-item>-->
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery"
                   v-hasPermi="['system:warehouse:query']"
        >搜索</el-button>
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
          v-hasPermi="['system:warehouse:add']"
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
          v-hasPermi="['system:warehouse:edit']"
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
          v-hasPermi="['system:warehouse:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['system:warehouse:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="warehouseList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="仓库编号" align="center" prop="warehouseId" />
      <el-table-column label="仓库名称" align="center" prop="warehouseName" />
      <el-table-column label="仓库区域位置" align="center" prop="location" />
      <el-table-column label="仓库容量" align="center" prop="capacity" />
      <el-table-column label="仓库温度" align="center" prop="temperature" >
        <template slot-scope="scope">
          <span>
            {{ scope.row.temperature }}℃
          </span>
        </template>
      </el-table-column>
      <el-table-column label="仓库湿度" align="center" prop="humidity" >
        <template slot-scope="scope">
          <span>
            {{ scope.row.humidity }}%
          </span>
        </template>
      </el-table-column>
      <el-table-column label="氧气含量" align="center" prop="oxygenLevel">
        <template slot-scope="scope">
          <span>
            {{ scope.row.oxygenLevel }}%
          </span>
        </template>
      </el-table-column>
      <el-table-column label="氮气含量" align="center" prop="nitrogenLevel">
        <template slot-scope="scope">
          <span>
            {{ scope.row.nitrogenLevel }}%
          </span>
        </template>
      </el-table-column>
      <el-table-column label="火灾情况" align="center" prop="isFire" >
        <template slot-scope="scope">
          <span :class="scope.row.isFire ? 'fire-yes' : 'fire-no'">
            <i v-if="scope.row.isFire" class="el-icon-warning" style="color: red;"></i>
            <i v-else class="el-icon-success" style="color: green;"></i>
            <dict-tag :options="dict.type.is_fire" :value="scope.row.isFire" />
          </span>
        </template>
      </el-table-column>
      <el-table-column label="最后更新时间" align="center" prop="lastUpdated" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.lastUpdated, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['system:warehouse:edit']"
          >修改</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['system:warehouse:remove']"
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

    <!-- 添加或修改仓库管理对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="500px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="仓库名称" prop="warehouseName">
          <el-input v-model="form.warehouseName" placeholder="请输入仓库名称" />
        </el-form-item>
        <el-form-item label="仓库区域位置" prop="location">
          <el-input v-model="form.location" placeholder="请输入仓库区域位置" />
        </el-form-item>
        <el-form-item label="仓库容量" prop="capacity">
          <el-input v-model="form.capacity" placeholder="请输入仓库容量" />
        </el-form-item>
        <el-form-item label="仓库温度" prop="temperature">
          <el-input v-model="form.temperature" placeholder="请输入仓库温度" />
        </el-form-item>
        <el-form-item label="仓库湿度" prop="humidity">
          <el-input v-model="form.humidity" placeholder="请输入仓库湿度" />
        </el-form-item>
        <el-form-item label="氧气含量" prop="oxygenLevel">
          <el-input v-model="form.oxygenLevel" placeholder="请输入氧气含量" />
        </el-form-item>
        <el-form-item label="氮气含量" prop="nitrogenLevel">
          <el-input v-model="form.nitrogenLevel" placeholder="请输入氮气含量" />
        </el-form-item>
        <el-form-item label="是否火灾" prop="isFire">
          <el-select v-model="form.isFire" placeholder="请选择是否火灾">
            <el-option
              v-for="dict in dict.type.is_fire"
              :key="dict.value"
              :label="dict.label"
              :value="parseInt(dict.value)"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="最后更新时间" prop="lastUpdated">
          <el-date-picker clearable
            v-model="form.lastUpdated"
            type="datetime"
            value-format="yyyy-MM-dd HH:mm:ss"
            placeholder="请选择最后更新时间">
          </el-date-picker>
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
import { listWarehouse, getWarehouse, delWarehouse, addWarehouse, updateWarehouse } from "@/api/system/warehouse";

export default {
  name: "Warehouse",
  dicts: ['is_fire'],
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
      // 仓库管理表格数据
      warehouseList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        warehouseName: null,
        location: null,
        capacity: null,
        temperature: null,
        humidity: null,
        oxygenLevel: null,
        nitrogenLevel: null,
        isFire: null,
        lastUpdated: null
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        warehouseName: [
          { required: true, message: "仓库名称不能为空", trigger: "blur" }
        ],
        location: [
          { required: true, message: "仓库区域位置不能为空", trigger: "blur" }
        ],
        capacity: [
          { required: true, message: "仓库容量不能为空", trigger: "blur" }
        ],
      }
    };
  },
  created() {
    this.getList();
  },
  methods: {
    /** 查询仓库管理列表 */
    getList() {
      this.loading = true;
      listWarehouse(this.queryParams).then(response => {
        this.warehouseList = response.rows;
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
        warehouseId: null,
        warehouseName: null,
        location: null,
        capacity: null,
        temperature: null,
        humidity: null,
        oxygenLevel: null,
        nitrogenLevel: null,
        isFire: null,
        lastUpdated: null
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
      this.ids = selection.map(item => item.warehouseId)
      this.single = selection.length!==1
      this.multiple = !selection.length
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.reset();
      this.open = true;
      this.title = "添加仓库管理";
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset();
      const warehouseId = row.warehouseId || this.ids
      getWarehouse(warehouseId).then(response => {
        this.form = response.data;
        this.open = true;
        this.title = "修改仓库管理";
      });
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.warehouseId != null) {
            updateWarehouse(this.form).then(response => {
              this.$modal.msgSuccess("修改成功");
              this.open = false;
              this.getList();
            });
          } else {
            addWarehouse(this.form).then(response => {
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
      const warehouseIds = row.warehouseId || this.ids;
      this.$modal.confirm('是否确认删除仓库管理编号为"' + warehouseIds + '"的数据项？').then(function() {
        return delWarehouse(warehouseIds);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {});
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('system/warehouse/export', {
        ...this.queryParams
      }, `warehouse_${new Date().getTime()}.xlsx`)
    }
  }
};
</script>

