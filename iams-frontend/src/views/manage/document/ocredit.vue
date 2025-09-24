<template>
    <div class="app-container">
        <el-row>
            <el-col :span="3">
                <div class="paste-area">
                    <div class="paste-container" @paste="handlePaste">
                        <p v-if="!selectedFile">请粘贴图片(Ctrl+V 或 Cmd+V)</p>
                        <img v-else :src="imageUrl" alt="Pasted Image" class="pasted-image" />
                    </div>
                </div>
            </el-col>
            <el-col :span="12">
                <div v-show="showScreenShort">
                    <!-- <el-button @click="jietu">画框截图</el-button> -->
                    <el-button type="primary" @click="extractOcrResult">提取OCR结果到富文本编辑器</el-button>
                </div>
                <screen-short v-if="screenshotStatus" @get-image-data="getImg" @destroy-component="destroyComponent">
                </screen-short>
            </el-col>
        </el-row>
        <el-row>
            <el-col :span="12">
                <div class="pdf-container">
                    <iframe :src="pdfUrl" width="100%" height="100%" frameborder="0"></iframe>
                </div>
            </el-col>
            <el-col :span="12">
                <editor v-model="documentContent" :min-height="800" />
            </el-col>
        </el-row>
        <el-row>
            <el-col :span="12">
                <div class="ocr-container">
                    <h1>上传图片进行OCR识别</h1>
                    <form @submit.prevent="handleOcrSubmit">
                        <input type="file" ref="fileInput" accept="image/*" @change="handleFileChange" />
                        <button type="submit">上传并识别</button>
                    </form>
                </div>
            </el-col>
            <el-col :span="12">
                <div class="ocr-result">
                    <h1 class="h2">OCR识别结果</h1>
                    <div v-if="ocrResult">
                        <ul>
                            <li v-for="(word, index) in ocrResult" :key="index">
                                <div id="ocr-result-div" contenteditable="true" class="editable-div"
                                    @input="handleContentEditableInput($event, index)">{{ word }}</div>
                            </li>
                        </ul>
                    </div>
                </div>
            </el-col>
        </el-row>

        <el-dialog v-model="dialogVisible" title="Tips" width="30%" :before-close="handleClose">
            <div style="width: 240px">
                <el-col>
                    <el-row>
                        <el-select v-model="value" class="m-2" placeholder="Select" size="large" clearable>
                            <el-option v-for="item in options" :key="item.label" :label="item.label"
                                :value="item.label" />
                        </el-select>
                    </el-row>
                    <el-row>
                        <el-alert title="没有就在下面新建一个" type="info" center show-icon :closable="false" />
                    </el-row>

                    <el-row>
                        <el-input v-model="input" class="in-1" placeholder="请输入" clearable />
                    </el-row>

                </el-col>
            </div>
            <div>
                <el-col>
                    <editor v-model="documentContent" :min-height="300" />
                </el-col>
            </div>
            <template #footer>
                <span class="dialog-footer">
                    <el-button @click="dialogVisible = false">取消</el-button>
                    <el-button type="primary" @click="updateExtraInfo">
                        确定
                    </el-button>
                </span>
            </template>
        </el-dialog>
    </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { doOcr } from '@/api/manage/document'
import { ElMessageBox } from 'element-plus'
import { getDocument, updateDocExtraInfo } from "@/api/manage/document";
import html2canvas from 'html2canvas';
import axios from 'axios'

const handleClose = (done) => {
    ElMessageBox.confirm('您确定要关闭对话框吗?')
        .then(() => {
            done()
        })
        .catch(() => {
            // catch error
        })
}

const route = useRoute();
const pdfUrl = ref('https://aclanthology.org/2024.acl-short.1.pdf');
const fileInput = ref(null);
const selectedFile = ref(null);
const ocrResult = ref(null);
const showScreenShort = ref(true);
const screenshotStatus = ref(false);
const dialogVisible = ref(false);
const documentContent = ref('');
const value = ref('');
const input = ref('');
const options = ref([]);
const updateInfo = ref({});
const _id = ref(1);

const data = reactive({
    form: {
        id: null,
        archiveId: null,
        xuhao: null,
        name: null,
        fileType: null,
        fileSize: 0,
        filePath: null,
        fileLocation: null,
        content: null,
        authenticity: null,
        integrity: null,
        availability: null,
        security: null,
        createTime: null,
        createBy: null,
        updateTime: null,
        updateBy: null,
        remark: null,
        directory: null,
        extraInfo: []
    },
});
const { form } = toRefs(data);

onMounted(() => {
    // if (route.query.url) {
    //     pdfUrl.value = route.query.url
    //     // 如果需要对PDF URL进行OCR处理，可以在这里调用doOcr方法
    // }

    // 要将 route.query.id 转换成数字类型，可以使用 JavaScript 的 Number 函数或一元加号运算符
    updateInfo.value.id = +route.query.id;
    _id.value = route.query.id;
    if (_id.value) {
        getDocument(_id.value).then(response => {
            form.value = response.data;
            pdfUrl.value = form.value.filePath;
            // console.log("Current document:", form.value);
            // console.log("Current extraInfo type:", typeof form.value.extraInfo);
            // console.log("Current extraInfo isArray:", Array.isArray(form.value.extraInfo));
            // console.log("Current extraInfo:", form.value.extraInfo);

            // Fetch the PDF file and create a Blob URL
            if (pdfUrl.value.startsWith("http://127.0.0.1:8080")) {
                console.log("pdfUrl.value", pdfUrl.value);
                axios({
                    url: pdfUrl.value,
                    method: 'GET',
                    responseType: 'blob'
                }).then(response => {
                    const blob = new Blob([response.data], { type: 'application/pdf' });
                    pdfUrl.value = URL.createObjectURL(blob);
                }).catch(error => {
                    console.error('Error fetching PDF:', error);
                });
            }
            showExtraInfo();
        }).catch(error => {
            console.error("Failed to fetch document:", error);
            options.value = []; // 确保 options 为空数组
            value.value = ''; // 重置 value 为默认值
        });
    } else {
        console.error("No document ID provided in route query.");
        options.value = []; // 确保 options 为空数组
        value.value = ''; // 重置 value 为默认值
    }
})

function showExtraInfo() {
    let extraInfo = form.value.extraInfo;

    // 如果 extraInfo 是字符串，尝试解析为 JSON 数组
    if (typeof extraInfo === 'string') {
        try {
            extraInfo = JSON.parse(extraInfo);
            // console.log("Parsed extraInfo:", extraInfo);
        } catch (error) {
            console.error("Failed to parse extraInfo as JSON:", error);
            extraInfo = [];
        }
    }

    // 确保 extraInfo 是一个数组
    if (Array.isArray(extraInfo)) {
        options.value = extraInfo;
        // console.log("Updated options:", options.value);

        // 检查 value 是否在 options 中
        if (!options.value.some(option => option.value === value.value)) {
            value.value = ''; // 重置 value 为默认值
        }
    } else {
        console.error("extraInfo is not an array:", extraInfo);
        options.value = []; // 确保 options 为空数组
        value.value = ''; // 重置 value 为默认值
    }
}

function jietu() {
    showScreenShort.value = false; // 隐藏弹框
    screenshotStatus.value = true; // 显示截图插件
}

// 取消截图 显示弹窗 隐藏截图插件
function destroyComponent() {
    screenshotStatus.value = false;
    showScreenShort.value = true;
}

const handleFileChange = (event) => {
    selectedFile.value = event.target.files[0];
    handleOcrSubmit(); // 调用 handleOcrSubmit 方法
}

const handlePaste = (event) => {
    const items = (event.clipboardData || event.originalEvent.clipboardData).items;
    let blob = null;

    for (const item of items) {
        if (item.type.indexOf('image') !== -1) {
            blob = item.getAsFile();
            break;
        }
    }

    if (blob) {
        const reader = new FileReader();
        reader.onload = (e) => {
            imageUrl.value = e.target.result;
            selectedFile.value = blob;
            handleOcrSubmit(); // 调用 handleOcrSubmit 方法
        };
        reader.readAsDataURL(blob);
    }
};

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
        alert('请选择一个图片文件');
        return;
    }

    const formData = new FormData();
    formData.append('file', selectedFile.value);

    try {
        const response = await doOcr(formData);
        ocrResult.value = response.data; // 假设 response.data 是 OCR 识别结果
        console.log('OCR 识别结果:', response.data);

        // 处理 OCR 识别结果
    } catch (error) {
        console.error('上传失败:', error);
    }
}

// 提取 id="ocr-result-div" 的 div 中的内容，赋值给 documentContent
function extractOcrResult() {
    const ocrResultDiv = document.getElementById('ocr-result-div');
    if (ocrResultDiv) {
        let ocrcontent = JSON.parse(ocrResultDiv.innerText);
        documentContent.value = ocrcontent.join('\n');
        // documentContent.value = ocrResultDiv.innerText;
        // 将OCR识别结果按行赋值给documentContent变量

        dialogVisible.value = true;

    } else {
        console.error('元素id="ocr-result-div"未找到');
    }
}

function updateExtraInfo() {
    dialogVisible.value = false;
    if (value.value !== '') {
        updateInfo.value.label = value.value;
        updateInfo.value.value = documentContent.value;
    } else if (input.value !== '') {
        updateInfo.value.label = input.value;
        updateInfo.value.value = documentContent.value;
    } else {
        console.error("请选择标签或输入标签！");
    }
    console.log("updateInfo:", updateInfo);
    updateDocExtraInfo(updateInfo.value).then(response => {
        getDocument(_id.value).then(response => {
            form.value = response.data;
            showExtraInfo();
        });
    });
}

const imageUrl = ref('');

</script>

<style scoped>
.pdf-container {
    width: 100%;
    height: 900px;
    overflow: auto;
}

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

.dialog-footer button:first-child {
    margin-right: 10px;
}

.el-select-dropdown__item {
    text-align: center;
    /* 居中对齐 */
    /* text-align: right; 如果需要右对齐，可以使用这个属性 */
}

.el-alert {
    margin: 10px 0 0;
}

.in-1 {
    margin: 10px 0 0;
    height: 40px;
}

.paste-area {
    background: #f9f9f9;
    border: 1px dashed #ccc;
    border-radius: 10px;
    padding: 10px; /* 减小内边距 */
    text-align: center;
    margin-bottom: 10px; /* 减小外边距 */
    width: 200px; /* 设置固定宽度 */
    height: 100px; /* 设置固定高度 */
}

.paste-container {
    min-height: 150px; /* 减小最小高度 */
    position: relative;
    width: 100%; /* 使容器宽度适应粘贴区域 */
    height: 100%; /* 使容器高度适应粘贴区域 */
}

.pasted-image {
    max-width: 100%;
    max-height: 100%;
    display: block;
    margin: 0 auto;
}
</style>