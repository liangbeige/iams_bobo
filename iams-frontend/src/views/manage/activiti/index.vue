<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" :inline="true" v-show="showSearch" label-width="88px">
      <el-form-item label="流程KEY" prop="key">
        <el-input
            v-model="queryParams.key"
            placeholder="请输入流程KEY"
            clearable
            size="small"
            @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="名称" prop="name">
        <el-input
            v-model="queryParams.name"
            placeholder="请输入名称"
            clearable
            size="small"
            @keyup.enter.native="handleQuery"
        />
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
            type="primary"
            icon="Plus"
            @click="OnlineDrawingProcess"
        >在线绘制流程</el-button>
      </el-col>
    </el-row>

    <el-table  :data="Modeler">
      <el-table-column label="流程名称" align="center" prop="name"/>
<!--      <el-table-column label="流程ID" align="center" prop="id"/>-->
      <el-table-column label="流程KEY" align="center" prop="key"/>

      <el-table-column label="版本" align="center" prop="version"/>
      <el-table-column label="部署时间" align="center" prop="deploymentTime"/>
<!--      <el-table-column label="部署ID" align="center" prop="deploymentId"/>-->

      <el-table-column label="状态" align="center" prop="suspendState">
        <template v-slot="scope">
          <span>{{ scope.row.suspendState === 1 ? '激活' : '挂起' }}</span>
        </template>
      </el-table-column>

      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">

        <template v-slot="scope">
          <el-button
              size="mini"
              type="text"
              icon="el-icon-edit"
              @click="OnlineModificationProcess(scope.row)"
              v-hasPermi="['activiti:modeler']"
          >查看
          </el-button>

          <el-button
              size="mini"
              type="text"
              icon="el-icon-edit"
              @click="SuspendOrActiveApply(scope.row)"
              v-hasPermi="['activiti:modeler']"
          >{{scope.row.suspendState == 1 ? '挂起' : '激活' }}
          </el-button>

          <el-button
              size="mini"
              type="text"
              icon="el-icon-edit"
              @click="HandleDelete(scope.row)"
              v-hasPermi="['activiti:modeler']"
          >删除
          </el-button>

        </template>

      </el-table-column>>

    </el-table>

    <pagination
        v-show="total>0"
        :total="total"
        v-model:page="queryParams.pageNum"
        v-model:limit="queryParams.pageSize"
        @pagination="getList"
    />



    <!--bpmnjs在线流程设计器-->
    <el-dialog :title="title" v-model="open" width="1680px" append-to-body>
      <div style="position:relative;height: 100%;">
        <iframe
            id="iframe"
            :src="modelerUrl"
            frameborder="0"
            width="100%"
            height="720px"
            scrolling="auto"
        ></iframe>
      </div>
    </el-dialog>
  </div>
</template>

<script setup name="ActIdGroup">
import { ref } from 'vue';
import {listDefinition, suspendOrActiveApply, delDefinition, getDefinitionsByInstanceId} from "@/api/activiti/definition.js";
import { ElMessageBox, ElMessage} from 'element-plus';


// 使用 ref 创建响应式变量
const modelerUrl = ref("");
const title = ref("在线绘制流程");
const open = ref(false);
const showSearch = ref(true);

const Modeler = ref([]);
const total = ref(0);

const queryParams = reactive({
      pageNum: 1,
      pageSize: 5,
      id: null,
      rev: null,
      name: null,
      type: null,
});

// const baseURL = "C:/Users/Altria/JavaProjects/iams_project0319based0125/iams-frontend/dist";

// 在线绘制流程的方法
const OnlineDrawingProcess = () => {
  open.value = true;
  localStorage.setItem("VUE_APP_BASE_API", import.meta.env.VITE_APP_BASE_API)
  modelerUrl.value = './bpmnjs/index.html?type=addBpmn';

};

// 查看流程的方法
const OnlineModificationProcess=(data)=>{
  open.value = true;
  localStorage.setItem("VUE_APP_BASE_API", import.meta.env.VITE_APP_BASE_API)
  modelerUrl.value = './bpmnjs/index.html?type=lookBpmn&deploymentFileUUID=' + data.deploymentId + '&deploymentName=' + encodeURI(data.resourceName);

};

// 删除流程的方法
const HandleDelete=(row)=>{
  ElMessageBox.confirm('是否确认删除编号为"' + row.key + '"的数据项?', "警告",{
      confirmButtonText: "确定",
      cancelButtonText: "取消",
      type: "warning"
  }).then(function () {
    return delDefinition(row.deploymentId);
  }).then(() => {
    getList();
    ElMessage({
      message: '删除成功',
      type: 'success',
      duration: 1000  // 1秒后自动消失
    });
  }).catch(function (){
  });
};



// 挂起流程的方法
const SuspendOrActiveApply=(row)=>{
  var suspendOrActive = row.suspendState === '2' ? '激活' : '挂起';
  ElMessageBox.confirm(`确认要${suspendOrActive}流程吗?`, "警告", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning"
  }).then(function () {
      var data = {"id": row.id, "suspendState": row.suspendState};
      return suspendOrActiveApply(data);
    }).then(() => {
      getList();
      ElMessage({
      message: '修改成功',
      type: 'success',
      duration: 1000  // 1秒后自动消失
    });
    }).catch(function (){
  });

};

const getList = () => {
  listDefinition(queryParams).then(response => {
    Modeler.value = response.rows;
    total.value = response.total;
  });
};



/** 搜索按钮操作 */
const handleQuery = () => {
  queryParams.pageNum = 1;
  getList();
};


// 关闭模型的回调
const modelCancel = () => {
  open.value = false;
};

getList();

</script>
