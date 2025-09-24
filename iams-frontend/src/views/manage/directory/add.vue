<template>
  <div class="app-container">
    <el-row :gutter="20">
      <el-col :span="7">
        <el-card style="max-width: 100%">
          <template #header>
            <div class="card-header">
              <span>目录模板基本信息编辑区</span>
            </div>
          </template>
          <el-form ref="directoryRef" :model="form" :rules="rules" label-position="top" label-width="80px">
            <el-form-item label="目录模板名称" prop="name">
              <el-input v-model="form.name" placeholder="请输入目录名称" />
            </el-form-item>
            <el-form-item label="目录结构" prop="structure">
              <el-input v-model="form.structure" placeholder="请在右侧编辑好目录结构，点击回填即可" />
            </el-form-item>
            <el-form-item label="创建用户" prop="createBy">
              <el-input v-model="form.createBy" placeholder="请填写创建该目录的用户名" />
            </el-form-item>
            <el-form-item label="备注" prop="remark">
              <el-input v-model="form.remark" type="textarea" rows="5" placeholder="请输入内容" />
            </el-form-item>
          </el-form>
          <template #footer>
            <el-button type="primary" @click="submitForm">确 定</el-button>
            <el-button @click="cancel">取 消</el-button>
          </template>
        </el-card>
      </el-col>
      <el-col :span="17">
        <el-card style="max-width: 100%">
          <template #header>
            <div class="card-header">
              <span>树形目录结构编辑区</span>
            </div>
          </template>
          <el-tree style="max-width: 90%" :data="dataSource" node-key="id" default-expand-all
                   :draggable="true" :expand-on-click-node="false" icon="Folder">
            <template #default="{ node, data }">
                            <span class="custom-tree-node">
                                <span> {{ data.label }}</span>
                                <span>
                                    <a style="margin-left: 8px" @click="appendTreeNode(data)"> 追加 </a>
                                    <a style="margin-left: 8px" @click="removeTreeNode(node, data)"> 删除 </a>
                                    <a style="margin-left: 8px" @click="editTreeNode(data)"> 编辑 </a>
                                </span>
                            </span>
            </template>
          </el-tree>
          <template #footer>
            <el-button type="primary" @click="fillForm">回 填 到 目 录 结 构</el-button>
            <el-button type="primary" @click="addTopLevelNode">添加一级目录</el-button>
          </template>
        </el-card>
      </el-col>
    </el-row>
    <el-dialog title="修改目录名称" v-model="open" width="30%">
      <el-form ref="formRef" :model="directory" label-position="top" label-width="80px">

        <el-form-item label="目录名称" prop="label">
          <el-input v-model="directory.label" placeholder="请输入目录名称" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="confirmDirectory">确 定</el-button>
          <el-button @click="cancelDirectory">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script lang="ts" setup>
import { ref } from 'vue'
import type Node from 'element-plus/es/components/tree/src/model/node'
import { addDirectory } from "@/api/manage/directory";
import useTagsViewStore from '@/store/modules/tagsView.js'
import {useRoute, useRouter} from 'vue-router';


const tagsViewStore = useTagsViewStore();
const router = useRouter();
const route = useRoute();


const { proxy } = getCurrentInstance()

const data = reactive({
  form: {
    name: '',
    structure: '',
    createBy: '',
    updateBy: '',
    remark: ''
  },
  rules: {
    name: [
      { required: true, message: "目录名称不能为空", trigger: "blur" }
    ],
    structure: [
      { required: true, message: "目录结构不能为空", trigger: "blur" }
    ],
    createBy: [
      { required: true, message: "创建用户不能为空", trigger: "blur" }
    ],
  },
  directory: {
    id: 0,
    label: '',
  },
});

const { form, rules, directory } = toRefs(data);


interface Tree {
  id: number,
  label: string,
  files: string[],
  children?: Tree[]
}


const dataSource = ref<Tree[]>([
  {
    id: 1,
    label: '一级目录 1',
    files: [],
    children: [
      {
        id: 4,
        label: '二级目录 1-1',
        files: [],
        children: [
          {
            id: 9,
            label: '三级目录 1-1-1',
            files: [],
          },
          {
            id: 10,
            label: '三级目录 1-1-2',
            files: [],
          },
        ],
      },
    ],
  },
  {
    id: 2,
    label: '一级目录 2',
    files: [],
    children: [
      {
        id: 5,
        label: '二级目录 2-1',
        files: [],
      },
      {
        id: 6,
        label: '二级目录 2-2',
        files: [],
      },
    ],
  },
  {
    id: 3,
    label: '一级目录 3',
    files: [],
    children: [
      {
        id: 7,
        label: '二级目录 3-1',
        files: [],
      },
      {
        id: 8,
        label: '二级目录 3-2',
        files: [],
      },
    ],
  },
])
// 找到dataSource中id的最大值
function findMaxId(data_source) {
  let maxId = 0;

  function traverse(node) {
    if (node.id > maxId) {
      maxId = node.id;
    }
    if (node.children) {
      node.children.forEach(child => traverse(child));
    }
  }

  data_source.forEach(item => traverse(item));
  return maxId;
}

let id = findMaxId(dataSource.value) + 1;

function updateLabelById(dataSource, id, newLabel) {
  for (let item of dataSource) {
    if (item.id === id) {
      item.label = newLabel;
      return true; // 找到并修改后返回 true
    }
    if (item.children && item.children.length > 0) {
      const found = updateLabelById(item.children, id, newLabel);
      if (found) {
        return true; // 如果在子节点中找到并修改，返回 true
      }
    }
  }
  return false; // 如果没有找到对应的 id，返回 false
}

const open = ref(false)

const editTreeNode = (data: Tree) => {
  console.log(data)
  directory.value.id = data.id;
  directory.value.label = data.label;
  open.value = true;
  // data.label = directory.label
  // dataSource.value = [...dataSource.value]
}

function confirmDirectory() {
  open.value = false;
  console.log(directory.value)
  updateLabelById(dataSource.value, directory.value.id, directory.value.label)
}

function cancelDirectory() {
  open.value = false;
}

//  追加节点
const appendTreeNode = (data: Tree) => {
  const newChild = { id: id++, label: '一个新增的目录', files: [], children: [] }
  if (!data.children) {
    data.children = []
  }
  data.children.push(newChild)
  dataSource.value = [...dataSource.value]
}

/**
 * 添加一级目录节点
 */
const addTopLevelNode = () => {
  // 创建新节点
  const newNode = {
    id: id++, // 使用全局递增ID
    label: '一级目录', // 默认名称
    files: [], // 空文件列表
    children: [] // 空子节点列表
  };

  // 如果当前没有节点数据，创建一个新的数组
  if (!dataSource.value || dataSource.value.length === 0) {
    dataSource.value = [newNode];
  } else {
    // 将新节点添加到一级节点列表的末尾
    dataSource.value = [...dataSource.value, newNode];
  }

  // 显示成功提示
  proxy.$modal.msgSuccess("一级目录添加成功");
}

const removeTreeNode = (node: Node, data: Tree) => {
  const parent = node.parent
  const children: Tree[] = parent.data.children || parent.data
  const index = children.findIndex((d) => d.id === data.id)
  children.splice(index, 1)
  dataSource.value = [...dataSource.value]
  id = findMaxId(dataSource.value) + 1;
}


// 回填表单：把树形控件的目录结构填到左侧表单的目录结构中
function fillForm() {
  data.form.structure = JSON.stringify(dataSource.value)
}

/** 提交按钮: 新增目录操作 */
function submitForm() {
  data.form.updateBy = data.form.createBy;

  proxy.$refs["directoryRef"].validate(valid => {
    if (valid) {
      addDirectory(form.value).then(response => {
        proxy.$modal.msgSuccess("新增成功");
        proxy.$router.push({ path: "/manage/directory/drt-list" });
      });
    }
  });

    // 删除当前 tab
    if (tagsViewStore) {
        tagsViewStore.delView(route);
    }
    // 跳转到列表页
    router.replace({ path: '/manage/directory/drt-list' });

}
// 取消按钮
function cancel() {
    // 删除当前 tab
    if (tagsViewStore) {
        tagsViewStore.delView(route);
    }
    // 跳转到列表页
    router.replace({ path: '/manage/directory/drt-list' });
  // reset();
}

// 表单重置
function reset() {
  form.value = {
    name: null,
    structure: null,
    createBy: null,
    updateBy: null,
    remark: null
  };
  proxy.resetForm("directoryRef");
}
</script>

<style scoped>
.custom-tree-node {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 16px;
  padding-right: 12px;
}
</style>