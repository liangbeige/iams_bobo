<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="名称" prop="gdlName">
        <el-input
            v-model="queryParams.gdlName"
            placeholder="请输入名称"
            clearable
            @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="IP地址" prop="gdlIp">
        <el-input
            v-model="queryParams.gdlIp"
            placeholder="请输入IP地址"
            clearable
            @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="端口号" prop="gdlPort">
        <el-input
            v-model="queryParams.gdlPort"
            placeholder="请输入端口号"
            clearable
            @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="备注信息" prop="gdlRemark">
        <el-input
            v-model="queryParams.gdlRemark"
            placeholder="请输入备注信息"
            clearable
            @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
            type="primary"
            plain
            icon="Plus"
            size="mini"
            @click="handleAdd"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
            type="success"
            plain
            icon="Edit"
            size="mini"
            :disabled="single"
            @click="handleUpdate"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
            type="danger"
            plain
            icon="Delete"
            size="mini"
            :disabled="multiple"
            @click="handleDelete"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
            type="warning"
            plain
            icon="Download"
            size="mini"
            @click="handleExport"
        >导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="infoList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="区号" align="center" prop="gdlNo" />
      <el-table-column label="名称" align="center" prop="gdlName" />
      <el-table-column label="IP地址" align="center" prop="gdlIp" />
      <el-table-column label="端口号" align="center" prop="gdlPort" />
      <el-table-column label="备注信息" align="center" prop="gdlRemark" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button
              size="mini"
              type="text"
              icon="Edit"
              @click="handleUpdate(scope.row)"
          >修改</el-button>
          <el-button
              size="mini"
              type="text"
              icon="Delete"
              @click="handleDelete(scope.row)"
          >删除</el-button>
          <!--  详细信息-->
          <el-button
              size="mini"
              type="text"
              icon="Info"
              @click="handleInfo(scope.row)"
          >详细信息</el-button>
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

    <!-- 添加或修改固定列IP信息管理对话框 -->
    <el-dialog
        v-model="open"
        width="500px"
        :close-on-click-modal="false"
        :modal-append-to-body="false"
        @open="onDialogOpen"
        @close="onDialogClose"
    >
      <template #header="{ close }">
        <div class="flex justify-between items-center w-full">
          <span>{{ title }}</span>
        </div>
      </template>
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <!--<el-form-item label="列号" prop="gdlNo">-->
        <!--  <el-input v-model="form.gdlNo" placeholder="请输入列号" />-->
        <!--</el-form-item>-->
        <el-form-item label="名称" prop="gdlName">
          <el-input v-model="form.gdlName" placeholder="请输入名称" />
        </el-form-item>
        <el-form-item label="IP地址" prop="gdlIp">
          <el-input v-model="form.gdlIp" placeholder="请输入IP地址" />
        </el-form-item>
        <el-form-item label="端口号" prop="gdlPort">
          <el-input v-model="form.gdlPort" placeholder="请输入端口号" />
        </el-form-item>
        <el-form-item label="备注信息" prop="gdlRemark">
          <el-input v-model="form.gdlRemark" placeholder="请输入备注信息" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </template>
    </el-dialog>

    <!-- 详细信息弹窗 -->
    <el-dialog
        v-model="openInfo"
        title="固定列详细信息显示"
        width="1500px"
        :close-on-click-modal="false"
        :modal-append-to-body="false"
        @open="onInfoDialogOpen"
        @close="onInfoDialogClose"
    >
      <gu-ding-lie :ip="GDL_ip" :port="GDL_port"></gu-ding-lie>
      <template #footer>
        <el-button @click="cancelInfo">取 消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { listInfo, getInfo, delInfo, addInfo, updateInfo } from "@/api/system/GDIpInfo.js";
import GuDingLie from "@/views/iotSubSystem/gudinglie/index.vue";
export default {
  name: "Info",
  components: {
    GuDingLie
  },
  data() {
    return {
      GDL_ip: "",
      GDL_port: "",
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
      // 固定列IP信息管理表格数据
      infoList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      openInfo: false,
      // 搜索关键字
      searchKey: '',
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        gdlName: null,
        gdlIp: null,
        gdlPort: null,
        gdlRemark: null
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
      }
    };
  },
  created() {
    this.getList();
  },
  methods: {
    // 对话框生命周期钩子
    onDialogOpen() {
      console.log('对话框打开');
    },
    onDialogClose() {
      console.log('对话框关闭');
    },
    onInfoDialogOpen() {
      console.log('详细信息对话框打开');
    },
    onInfoDialogClose() {
      console.log('详细信息对话框关闭');
    },

    /** 查询固定列IP信息管理列表 */
    getList() {
      this.loading = true;
      listInfo(this.queryParams).then(response => {
        this.infoList = response.rows;
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
        gdlNo: null,
        gdlName: null,
        gdlIp: null,
        gdlPort: null,
        gdlRemark: null
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
      this.ids = selection.map(item => item.gdlNo)
      this.single = selection.length!==1
      this.multiple = !selection.length
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.reset();
      this.open = true;
      this.title = "添加固定列IP信息管理";
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset();
      const gdlNo = row.gdlNo || this.ids
      getInfo(gdlNo).then(response => {
        this.form = response.data;
        this.open = true;
        this.title = "修改固定列IP信息管理";
      });
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.gdlNo != null) {
            updateInfo(this.form).then(response => {
              this.$message.success("修改成功");
              this.open = false;
              this.getList();
            });
          } else {
            addInfo(this.form).then(response => {
              this.$message.success("新增成功");
              this.open = false;
              this.getList();
            });
          }
        }
      });
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      const gdlNos = row.gdlNo || this.ids;
      this.$confirm('是否确认删除固定列IP信息管理编号为"' + gdlNos + '"的数据项？', '警告', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        return delInfo(gdlNos);
      }).then(() => {
        this.getList();
        this.$message.success("删除成功");
      }).catch(() => {});
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('system/info/export', {
        ...this.queryParams
      }, `info_${new Date().getTime()}.xlsx`)
    },
    /** 详细信息 */
    handleInfo(row){
      this.GDL_ip = row.gdlIp;
      this.GDL_port = row.gdlPort;
      this.openInfo = true;
    },
    cancelInfo(){
      this.openInfo = false;
    }
  }
};
</script>
