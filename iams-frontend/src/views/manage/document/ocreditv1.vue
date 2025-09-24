<template>
    <div class="app-container">
        <h1 style="text-align: center;">OCR著录</h1>
        <el-row>
            <el-col :span="12">
                <div class="pdf-container">
                    <iframe :src="pdfUrl" width="100%" height="700px" frameborder="0"></iframe>
                </div>
            </el-col>

            <el-col :span="12">
                <div class="ocr-container">
                    <h1>上传图片进行OCR识别</h1>
                    <form @submit.prevent="handleOcrSubmit">
                        <input type="file" ref="fileInput" accept="image/*" @change="handleFileChange" />
                        <button type="submit">上传并识别</button>
                    </form>
                </div>

                <div v-show="showScreenShort">
                    <el-button @click="jietu">快捷截图</el-button>
                </div>
                <screen-short v-if="screenshotStatus" @get-image-data="getImg" @destroy-component="destroyComponent">
                </screen-short>

                <div class="ocr-result">
                    <h1 class="h2">OCR识别结果</h1>
                    <div v-if="ocrResult">
                        <ul>
                            <li v-for="(word, index) in ocrResult" :key="index">
                                <div contenteditable="true" class="editable-div"
                                    @input="handleContentEditableInput($event, index)">{{ word }}</div>
                            </li>
                        </ul>
                    </div>
                    <a href="/">返回首页</a>
                </div>

            </el-col>
        </el-row>
    </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { doOcr } from '@/api/manage/document'

const route = useRoute()
const pdfUrl = ref('https://aclanthology.org/2024.acl-short.1.pdf')
const fileInput = ref(null)
const selectedFile = ref(null)
const ocrResult = ref(null)
const showScreenShort = ref(true);
const screenshotStatus = ref(false);

onMounted(() => {
    if (route.query.url) {
        pdfUrl.value = route.query.url
        // 如果需要对PDF URL进行OCR处理，可以在这里调用doOcr方法
    }
})

function jietu() {
    showScreenShort.value = false;//隐藏弹框
    screenshotStatus.value = true;//显示截图插件
}

//取消截图 显示弹窗 隐藏截图插件
function destroyComponent() {
    screenshotStatus.value = false;
    showScreenShort.value = true;
}

const handleFileChange = (event) => {
    selectedFile.value = event.target.files[0]
}

const getImg = async (base64data) => {
    const bytes = window.atob(base64data.split(',')[1]);
    const buffer = new ArrayBuffer(bytes.length);
    const byteArray = new Uint8Array(buffer);
    for (let i = 0; i < bytes.length; i++) {
        byteArray[i] = bytes.charCodeAt(i);
    }
    selectedFile.value = new File([byteArray], 'screenshot.png', { type: 'image/png' });
    handleOcrSubmit(); // 调用 handleOcrSubmit 方法
}

const handleOcrSubmit = async () => {
    if (!selectedFile.value) {
        alert('请选择一个图片文件')
        return
    }

    const formData = new FormData()
    formData.append('file', selectedFile.value)

    try {
        const response = await doOcr(formData)
        ocrResult.value = response.data // 假设response.data是OCR识别结果
        console.log('OCR识别结果:', response.data)
        // 处理OCR识别结果
    } catch (error) {
        console.error('上传失败:', error)
    }
}
</script>

<style scoped>
.ocr-container {
    font-family: Arial, sans-serif;
    margin: 0;
    padding: 0;
    display: flex;
    flex-direction: column;
    align-items: center;
    background-color: #f8f9fa;
}

h1 {
    color: #343a40;
    margin-top: 20px;
}

.h2 {
    color: #343a40;
    margin-top: 20px;
}

form {
    margin: 20px 0;
    padding: 20px;
    border: 1px solid #dee2e6;
    border-radius: 5px;
    background-color: #fff;
    box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
}

input[type="file"] {
    margin-bottom: 10px;
}

button {
    background-color: #007bff;
    color: white;
    padding: 10px 20px;
    border: none;
    border-radius: 5px;
    cursor: pointer;
}

button:hover {
    background-color: #0056b3;
}

.ocr-result {
    font-family: Arial, sans-serif;
    margin: 0;
    padding: 0;
    display: flex;
    flex-direction: column;
    align-items: center;
    background-color: #f8f9fa;
}

ul {
    list-style-type: none;
    padding: 0;
}

li {
    padding: 5px 0;
    border-bottom: 1px solid #dee2e6;
}

a {
    margin-top: 20px;
    color: #007bff;
    text-decoration: none;
}

a:hover {
    text-decoration: underline;
}

.editable-div {
    width: 600px;
    box-sizing: border-box;
    padding: 5px;
    margin: 2px 0;
    min-height: 30px;
    /* 设置最小高度 */
    max-height: 450px;
    border: 1px solid #dee2e6;
    border-radius: 5px;
    font-family: Arial, sans-serif;
    font-size: 14px;
    line-height: 1.5;
    overflow: visible;
    /* 确保内容不会被隐藏 */
}
</style>