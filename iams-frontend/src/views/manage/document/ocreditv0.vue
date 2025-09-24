<template>
    <div class="app-container">
        <el-button type="primary" @click="screenshotStatus = true">截图</el-button>
        <!--截图组件-->
        <screen-short v-if="screenshotStatus" @destroy-component="destroyComponent"
            @get-image-data="getImg"></screen-short>
        <el-row :gutter="4">
            <el-col :span="12">
                <!-- 添加PDF加载部分 -->
                <div class="pdf-container">
                    <iframe :src="document.filePath" width="100%" height="100%" frameborder="0"></iframe>
                </div>
            </el-col>
            <el-col :span="12">
                <editor v-model="documentContent" :min-height="800" />
            </el-col>
        </el-row>
    </div>
</template>

<script setup lang="js">
import { ref } from "vue";
import { useRoute } from "vue-router";
import { getDocument, updateDocument } from "@/api/manage/document";

const { proxy } = getCurrentInstance();

const route = useRoute();

const document = ref({})
const documentContent = ref("")

const screenshotStatus = ref(false);
// 销毁组件函数
const destroyComponent = function (status) {
    screenshotStatus.value = status;
}
// 获取裁剪区域图片信息
const getImg = function (base64) {
    console.log("截图组件传递的图片信息", base64);
}

onMounted(() => {
    const _id = route.query.id;
    getDocument(_id).then(response => {
        document.value = response.data;
        documentContent.value = response.data.content;
    });
});
</script>

<style scoped>
.pdf-container {
    width: 100%;
    height: 900px;
    overflow: auto;
}
</style>