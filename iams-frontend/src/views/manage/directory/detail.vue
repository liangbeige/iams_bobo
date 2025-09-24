<template>
  <div class="app-container">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card style="max-width: 100%">
          <template #header>
            <div class="card-header">
              <span>目录模板基本信息</span>
            </div>
          </template>
          <p class="text item">
            <el-text tag="b" size="large">目录ID：</el-text>
            <el-text tag="b" size="large">{{ directory?.id }}</el-text>
          </p>
          <p class="text item">
            <el-text tag="b" size="large">目录名称：</el-text>
            <el-text tag="b" size="large">{{ directory?.name }}</el-text>
          </p>
          <p class="text item">
            <el-text tag="b" size="large">创建用户：</el-text>
            <el-text tag="b" size="large">{{ directory?.createBy }}</el-text>
          </p>
          <p class="text item">
            <el-text tag="b" size="large">创建时间：</el-text>
            <el-text tag="b" size="large">{{ directory?.createTime }}</el-text>
          </p>
          <p class="text item">
            <el-text tag="b" size="large">修改用户：</el-text>
            <el-text tag="b" size="large">{{ directory?.updateBy }}</el-text>
          </p>
          <p class="text item">
            <el-text tag="b" size="large">修改时间：</el-text>
            <el-text tag="b" size="large">{{ directory?.updateTime }}</el-text>
          </p>
          <template #footer>
            <p class="text item"><span>备注信息：</span><br /><br /><span>{{ directory?.remark }}</span></p>
          </template>
        </el-card>
      </el-col>
      <el-col :span="18">
        <el-card style="max-width: 100%">
          <template #header>
            <div class="card-header">
              <span>树形目录结构</span>
            </div>
          </template>
          <el-tree style="max-width: 100%" :data="dataSource" :props="defaultProps" default-expand-all  :expand-on-click-node="true" icon="Folder">
            <template #default="{ node, data }">
                            <span class="custom-tree-node">
                                <span> {{ data.label }}</span>
                            </span>
            </template>
          </el-tree>
        </el-card>
      </el-col>
    </el-row>
  </div>

</template>

<script lang="ts" setup>
import { ref, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import {  getDirectory } from "@/api/manage/directory";

const route = useRoute();

interface Tree {
  id: number,
  label: string,
  files: string[],
  children?: Tree[]
}

const defaultProps = {
  children: 'children',
  label: 'label',
  files: 'files',
  id: 'id'
}

const dataSource = ref<Tree[]>([
  {
    id: 1,
    label: 'Level one 1',
    files: [],
    children: [
      {
        id: 4,
        label: 'Level two 1-1',
        files: [],
        children: [
          {
            id: 9,
            label: 'Level three 1-1-1',
            files: [],
          },
          {
            id: 10,
            label: 'Level three 1-1-2',
            files: [],
          },
        ],
      },
    ],
  },
  {
    id: 2,
    label: 'Level one 2',
    files: [],
    children: [
      {
        id: 5,
        label: 'Level two 2-1',
        files: [],
      },
      {
        id: 6,
        label: 'Level two 2-2',
        files: [],
      },
    ],
  },
  {
    id: 3,
    label: 'Level one 3',
    files: [],
    children: [
      {
        id: 7,
        label: 'Level two 3-1',
        files: [],
      },
      {
        id: 8,
        label: 'Level two 3-2',
        files: [],
      },
    ],
  },
])

const handleNodeClick = (data: Tree) => {
  console.log(data)
}

interface Directory {
  id: string;
  name: string;
  structure: string;
  createBy: string;
  createTime: string;
  updateBy: string;
  updateTime: string;
  remark: string;
}

const directory = ref<Directory | null>(null);

/** 获取目录详情操作 */
function handleDetail(_id) {
  getDirectory(_id).then(response => {
    directory.value = response.data;
    dataSource.value =  JSON.parse(directory.value.structure);
  });
}

onMounted(() => {
  const directoryId = route.query.id as number;
  console.log(directoryId)
  if (directoryId) {
    handleDetail(directoryId);
  }
});
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