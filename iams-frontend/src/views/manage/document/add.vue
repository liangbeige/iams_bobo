<template>
    <div class="app-container">
        <el-card class="box-card">
            <div slot="header" class="clearfix">
                <h1 style="text-align: center;">文档新增页面</h1>
            </div>
            <el-form ref="documentRef" :model="form" :rules="rules" label-position="top" label-width="80px">
                <el-row :gutter="20">
                    <el-col :span="8">
                        <el-form-item label="①所属档案" prop="archiveId">
                            <el-select v-model="form.archiveId" placeholder="请选择档案" @change="getArchiveById"
                                style="width: 100%;">
                                <el-option v-for="archive in archiveList" :key="archive.id" :label="archive.name"
                                    :value="archive.id" />
                            </el-select>
                        </el-form-item>
                        <el-form-item label="文件名称" prop="name">
                            <el-input v-model="form.name" placeholder="请输入文件名称" />
                        </el-form-item>
                        <el-row :gutter="20">
                            <el-col :span="12">
                                <el-form-item label="文件类型" prop="fileType">
                                    <el-select v-model="form.fileType" placeholder="请选择文件类型">
                                        <el-option v-for="dict in iams_media_type" :key="dict.value" :label="dict.label"
                                            :value="dict.value"></el-option>
                                    </el-select>
                                </el-form-item>
                            </el-col>
                            <el-col :span="12">
                                <el-form-item label="文件大小(Kb)" prop="fileSize">
                                    <el-input type="number" v-model="form.fileSize" placeholder="请输入文件大小(Kb)" />
                                </el-form-item>
                            </el-col>
                        </el-row>
                        <!--                        <el-form-item label="文件路径" prop="filePath">-->
                        <!--                            <el-input v-model="form.filePath" placeholder="请输入文件路径" />-->
                        <!--                        </el-form-item>-->

                        <!-- <el-form-item label="存放位置" prop="fileLocation">
                            <el-input v-model="form.fileLocation" placeholder="请输入存放位置" />
                        </el-form-item> -->
                        <el-form-item label="备注" prop="remark">
                            <el-input v-model="form.remark" type="textarea" placeholder="请输入备注内容" />
                        </el-form-item>
                        <el-row :gutter="20">
                            <el-col :span="6">
                                <el-form-item label="真实性" prop="authenticity" label-position="left">
                                    <el-switch v-model="form.authenticity" />
                                </el-form-item>
                            </el-col>
                            <el-col :span="6">
                                <el-form-item label="完整性" prop="integrity">
                                    <el-switch v-model="form.integrity" />
                                </el-form-item>
                            </el-col>
                            <el-col :span="6">
                                <el-form-item label="可用性" prop="availability">
                                    <el-switch v-model="form.availability" />
                                </el-form-item>
                            </el-col>
                            <el-col :span="6">
                                <el-form-item label="安全性" prop="security">
                                    <el-switch v-model="form.security" />
                                </el-form-item>
                            </el-col>
                        </el-row>
                    </el-col>
                    <el-col :span="16">
                        <el-form-item label="②上传文件">
                            <el-upload ref="upload" class="upload-demo" :headers="headers" :limit="1"
                                :on-change="handleChange" :on-exceed="handleExceed" :auto-upload="false">

                                <template #trigger><el-button type="primary"
                                        style="width: 200px;">选择文件</el-button></template>

                                <el-select v-model="selectedCategoryCodes" multiple placeholder="请选择门类"
                                    :render-after-expand="false" style="width: 200px;margin-left: 20px;">
                                    <el-option v-for="category in archiveCategoryList" :key="category" :label="category"
                                        :value="category">
                                    </el-option>
                                </el-select>

                                <el-button class="ml-3" type="success" @click="submitUpload"
                                    style="width: 200px;margin-left: 20px;">
                                    上传至服务器
                                </el-button>

                                <template #tip>
                                    <div class="el-upload__tip text-red"> 一次只能上传一个文件，重复上传会覆盖之前上传的文件 </div>
                                </template>
                            </el-upload>
                        </el-form-item>


                        <!-- <el-form-item label="文件内容">
                            <div class="editor-container">
                                <editor v-model="form.content" />
                            </div>
                        </el-form-item> -->

                        <div class="media-container" v-show="mediaUrl">
                            <!-- PDF 文件 -->
                            <div v-if="isPDF" class="pdf-container">
                                <iframe :src="mediaUrl" :headers="headers" width="100%" height="100%"
                                    frameborder="0"></iframe>
                            </div>

                            <!-- 图片文件 -->
                            <img v-else-if="isImage" :src="mediaUrl" alt="预览图片" class="image-preview" />

                            <!-- 视频文件 -->
                            <video-player v-else-if="isVideo" :src="mediaUrl" :poster="videoPoster" controls
                                class="video-player" />

                            <!-- 音频文件 -->
                            <audio-player v-else-if="isAudio" :src="mediaUrl" controls class="audio-player" />

                            <!-- 其他文件类型提示 -->
                            <div v-else class="unsupported-file">
                                <el-alert title="不支持的文件类型" type="warning" :closable="false" show-icon>
                                    <p>系统暂不支持预览该类型文件，请下载后查看</p>
                                </el-alert>
                            </div>
                        </div>


                    </el-col>
                </el-row>
            </el-form>
            <template #footer>
                <el-button @click="cancel">取 消</el-button>
                <el-button type="primary" :loading="loading" @click="submitForm">
                    <span v-if="!loading">确定</span>
                    <span v-else>上传中...</span></el-button>
            </template>
        </el-card>
    </div>
</template>

<script setup>
import { ref, getCurrentInstance, reactive } from 'vue'
import { genFileId, ElTreeSelect } from 'element-plus'
import { addDocument, fullTextOcr, doOcr } from "@/api/manage/document";
import { listArchive, getArchive, addDirectoryFiles } from "@/api/manage/archive";     //updateDirectoryFiles => addDirectoryFiles已修改，后端也要做相应的修改
import { getToken } from "@/utils/auth";
import { getUserProfile } from "@/api/system/user";
import { ElSelect, ElOption } from 'element-plus';
import 'element-plus/dist/index.css';
import axios from 'axios';
import SparkMD5 from 'spark-md5'
import CryptoJS from 'crypto-js'
import pLimit from 'p-limit'
import VideoPlayer from '@/components/Player/VideoPlayer'
import AudioPlayer from '@/components/Player/AudioPlayer'


import useTagsViewStore from '@/store/modules/tagsView.js'
import {useRoute, useRouter} from 'vue-router';


const tagsViewStore = useTagsViewStore();
const router = useRouter();
const route = useRoute();

const MAX_RETRY = 3
// 根据CPU核心数设置并行度
const CONCURRENCY = Math.max(1, navigator.hardwareConcurrency - 1);

const isUploading = ref(false)
const uploadStatus = ref('')
const uploadManager = ref(null)
const mediaUrl = ref(null)
const isPDF = ref(false)
const isVideo = ref(false)
const isAudio = ref(false)
const isImage = ref(false)

const { proxy } = getCurrentInstance();
const { iams_media_type } = proxy.useDict('iams_media_type');

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
        authenticity: 0,
        integrity: 0,
        availability: 0,
        security: 0,
        createTime: null,
        createBy: null,
        updateTime: null,
        updateBy: null,
        remark: null,
        directory: null,
        isOcr: 0
    },
    rules: {
        // name: [
        //     { required: true, message: "文件名称不能为空", trigger: "blur" }
        // ],
        filePath: [
            { required: true, message: "文件上传失败，请检查网络后重试" }
        ]
    },
});

const archive = ref({});
const directory = ref([]);
const archiveList = ref([]);
const selectedFile = ref(null);
const ocrResult = ref(null);
const uploadFileUrl = ref("");
const upload = ref(null)
const currentFile = ref(null);
const loading = ref(false);
const { form, rules } = toRefs(data);
const encrypt = ref(false)
const result = ref(null)
const user = ref(null);
const projectId = ref(null);
const selectedCategoryCodes = ref([])
const archiveCategoryList = ref([]) // 档案自带的分类列表

// 取消按钮
function cancel() {
    // 删除当前 tab
    if (tagsViewStore) {
        tagsViewStore.delView(route);
    }
    // 跳转到列表页
    router.replace({ path: '/manage/document/doc-list' });
    //reset();
}

// 表单重置
function reset() {
    form.value = {
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
        directory: null
    };
    proxy.resetForm("documentRef");
}

/* 上传请求头 */
const headers = ref({ Authorization: "Bearer " + getToken() });

/** 提交按钮 */
function submitForm() {
    proxy.$refs["documentRef"].validate(valid => {
        if (valid) {
            // 将布尔值转换为整型
            const formData = {
                ...form.value,
                authenticity: form.value.authenticity ? 1 : 0,
                integrity: form.value.integrity ? 1 : 0,
                availability: form.value.availability ? 1 : 0,
                security: form.value.security ? 1 : 0,
            };
            console.log('Current form:', formData); // 添加日志输出
            formData.createBy = user.value.userName;
            formData.projectId = projectId.value;
            const selectedCategoryCodeStr = selectedCategoryCodes.value.join(",")
            formData.categoryCode = selectedCategoryCodeStr;

            addDocument(formData).then(response => {
                if (response) {
                    proxy.$modal.msgSuccess("新增成功");
                } else {
                    proxy.$modal.msgError("新增失败");
                }
                proxy.$router.push({ path: "/manage/document/doc-list" });
            });
        }
    });


    // 删除当前 tab
    if (tagsViewStore) {
        tagsViewStore.delView(route);
    }
    // 跳转到列表页
    router.replace({ path: '/manage/document/doc-list' });

}
/** 搜索档案 */
function getArchiveById() {
    // console.log('getArchiveById triggered')
    if (!form.value.archiveId) {
        proxy.$modal.msgError("请选择档案");
        return;
    }

    getArchive(form.value.archiveId).then(response => {
        // console.log('API Response:', response.data); // 添加日志输出
        if (response.code == 200 && response.data) {
            proxy.$modal.msgSuccess("档案存在");
            archive.value = response.data;
            projectId.value = archive.value.projectId
            archiveCategoryList.value = archive.value.categoryId.split(",").splice(1);
            if (archive.value.secretLevel == '3' || archive.value.secretLevel == '4') {
                encrypt.value = true
            } else {
                encrypt.value = false
            }
        } else {
            proxy.$modal.msgError("档案不存在");
        }
    }).catch(error => {
        console.error('API Error:', error); // 添加日志输出
        proxy.$modal.msgError("查询档案失败：" + error.message);
    });
}

const handleChange = async (uploadFile) => {
    currentFile.value = uploadFile.raw

    const isVideo = currentFile.value.type.startsWith('video/') || currentFile.value.name.match(/\.(mp4|avi|mov)$/i)
    const isAudio = currentFile.value.type.startsWith('audio/') || currentFile.value.name.match(/\.(mp3|wav)$/i)

    // 处理文件名和基础信息
    data.form.name = currentFile.value.name
    data.form.fileSize = Number((currentFile.value.size / 1024).toFixed(1))

    // 设置文件类型逻辑
    if (isVideo) {
        data.form.fileType = 'mp4'
    } else if (isAudio) {
        data.form.fileType = 'mp3'
    } else {
        data.form.fileType = currentFile.value.name.substring(currentFile.value.name.lastIndexOf(".") + 1)
    }
}

const handleExceed = (files) => {
    upload.value.clearFiles()
    const file = files[0]
    file.uid = genFileId()
    upload.value.handleStart(file)
}

const handleSuccess = (response) => {
    if (response.code === 200) {
        proxy.$modal.msgSuccess("上传成功");
        data.form.filePath = response.msg;
        checkFileType()
        mediaUrl.value = response.msg;
        console.log('mediaUrl.value:', response.msg);
    } else {
        proxy.$modal.msgError(response.msg);
    }
}

const checkFileType = () => {
    const fileType = data.form.fileType?.toLowerCase()
    isPDF.value = fileType === 'pdf'
    isVideo.value = ['mp4', 'avi', 'mov', 'wmv'].includes(fileType)
    isAudio.value = ['mp3', 'wav', 'ogg'].includes(fileType)
    isImage.value = ['png', 'jpg', 'jpeg', 'gif', 'bmp', 'webp'].includes(fileType)
}

const handleOcrSubmit = async (fileName) => {
    if (!fileName) {
        alert('请选择一个文件');
        return;
    }

    // const formData = new FormData();
    // formData.append('file', selectedFile.value);

    try {
        // const response = await doOcr(formData);
        const response = await fullTextOcr(fileName);
        if (response.code !== 200) {
            proxy.$modal.msgError("上传失败");
            return;
        }
        ocrResult.value = response.data; // 假设 response.data 是 OCR 识别结果
        console.log('OCR 识别结果:', response.data);
        form.value.content = response.data;
        form.value.isOcr = 1;
        // 处理 OCR 识别结果
    } catch (error) {
        console.error('上传失败:', error);
    }
}

const submitUpload = async () => {
    loading.value = true;
    try {
        if (!form.value.archiveId) {
            proxy.$message.error("请先选择档案");
            return;
        }
        if (!currentFile.value) {
            proxy.$message.error("请选择一个文件");
            return;
        }

        uploadManager.value = new UploadManager(currentFile.value, encrypt.value);
        await uploadManager.value.initialize();
        await uploadManager.value.startUpload();

        handleSuccess(result.value);

    } catch (error) {
        console.error('上传失败:', error);
        proxy.$message.error('上传过程中发生错误，请查看控制台日志');
    } finally {
        loading.value = false;
    }
}

function getUser() {
    getUserProfile().then(response => {
        user.value = response.data;
        console.log("Current user:", user.value);

    });
};

onMounted(() => {
    listArchive({}).then(response => {
        archiveList.value = response.rows;
    });

    getUser();
})

class UploadManager {
    constructor(file, isEncrypted = false) {
        this.file = file
        this.isEncrypted = isEncrypted
        this.chunkSize = this.calculateDynamicChunkSize(file.size);
        this.chunks = reactive([])
        this.fileHash = ''
        this.encryptionKey = null
        this.iv = null
        this.salt = null
        this.currentIV = null
        this.progress = reactive({
            percentage: 0,
            uploadedSize: 0
        })
        this.uploadedChunks = new Set()
        this.retryCounts = new Map()
        this.limiter = pLimit(CONCURRENCY)
    }

    async initialize() {
        this.fileHash = await this.calculateHash()
        this.loadLocalProgress()
        // 恢复salt和密钥
        const storedParams = localStorage.getItem(`encParams-${this.fileHash}`);
        if (storedParams) {
            const { salt } = JSON.parse(storedParams);
            this.salt = CryptoJS.enc.Hex.parse(salt);
            this.encryptionKey = this.deriveKey(this.salt);
        }

        try {
            const { data } = await axios.get(`${import.meta.env.VITE_APP_BASE_API}/minio/check`, {
                params: {
                    fileHash: this.fileHash,
                    filename: this.file.name
                },
                headers: {
                    'Authorization': headers.value.Authorization,
                }
            })
            data.uploadedChunks?.forEach(chunk => this.uploadedChunks.add(chunk))

            if (this.isEncrypted) {
                if (!this.salt) {
                    await this.generateEncryptionParams()
                    await this.validateEncryption()
                }
            }
        } catch (error) {
            console.error('初始化失败:', error)
        }
    }

    calculateDynamicChunkSize(fileSize) {
        const sizeMB = fileSize / 1024 / 1024;
        if (sizeMB > 1024) return 20 * 1024 * 1024; // 大文件20MB分片
        if (sizeMB > 100) return 10 * 1024 * 1024;  // 中等文件10MB
        return 5 * 1024 * 1024;                     // 小文件5MB
    }

    async validateEncryption() {
        // 添加salt检查
        if (!this.salt || this.salt.words.length !== 4) { // 128-bit salt = 4 words
            throw new Error('Salt未正确生成');
        }
    }

    // generateEncryptionParams 方法中移除IV生成
    async generateEncryptionParams() {
        // 只生成salt和密钥
        this.salt = CryptoJS.lib.WordArray.random(128 / 8);
        this.encryptionKey = this.deriveKey(this.salt); // 密钥派生方法需独立
        //  保存密钥和salt
        localStorage.setItem(`encParams-${this.fileHash}`, JSON.stringify({
            salt: CryptoJS.enc.Hex.stringify(this.salt),
            encryptionKey: CryptoJS.enc.Hex.stringify(this.encryptionKey)
        }));
    }

    // 新增密钥派生方法
    deriveKey(salt) {
        return CryptoJS.PBKDF2(
            CryptoJS.enc.Utf8.parse("secure-passphrase-" + this.fileHash),
            salt,
            { keySize: 8, iterations: 100000, hasher: CryptoJS.algo.SHA256 } // AES-256
        );
    }

    async calculateHash() {
        return new Promise((resolve) => {
            const spark = new SparkMD5.ArrayBuffer();
            const reader = new FileReader();
            const chunkSize = 5 * 1024 * 1024;
            const samples = [];
            const SPARSE_SAMPLING_INTERVAL = this.file.size > 1_000_000_000 ? 20 : 10;

            // 稀疏采样（减少数据量）
            for (let offset = 0; offset < this.file.size; offset += chunkSize * SPARSE_SAMPLING_INTERVAL) {
                samples.push(offset);
            }

            // 强制采样关键区域
            samples.push(0); // 头部
            samples.push(Math.floor(this.file.size / 2)); // 中部
            samples.push(Math.max(0, this.file.size - chunkSize)); // 尾部

            // 去重并排序
            const uniqueSamples = [...new Set(samples)].sort((a, b) => a - b);

            let current = 0;
            const loadNext = () => {
                if (current >= uniqueSamples.length) {
                    resolve(spark.end());
                    return;
                }
                const start = uniqueSamples[current];
                const end = Math.min(start + chunkSize, this.file.size);
                const blob = this.file.slice(start, end);
                reader.readAsArrayBuffer(blob);
            };

            reader.onload = (e) => {
                spark.append(e.target.result);
                current++;
                loadNext();
            };

            loadNext();
        });
    }

    async startUpload() {
        isUploading.value = true
        uploadStatus.value = '开始上传...'

        try {
            const generator = this.chunkGenerator()
            this.prepareChunks(generator)
            await this.uploadAllChunks()
            await this.mergeFile()
            // PDF 特殊处理
            // if (currentFile.value.type === 'application/pdf' || currentFile.value.name.endsWith('.pdf')) {
            //     await handleOcrSubmit(currentFile.value.name);
            // }
            uploadStatus.value = '上传完成!'
        } catch (error) {
            uploadStatus.value = `上传失败: ${error.message}`
        } finally {
            isUploading.value = false
            this.cleanLocalProgress()
        }
    }

    *chunkGenerator() {
        let offset = 0
        while (offset < this.file.size) {
            const chunk = this.file.slice(offset, offset + this.chunkSize)
            yield { index: Math.floor(offset / this.chunkSize), chunk }
            offset += this.chunkSize
        }
    }

    prepareChunks(generator) {
        for (const { index, chunk } of generator) {
            if (!this.uploadedChunks.has(index)) {
                this.chunks.push({
                    index,
                    chunk,
                    progress: 0,
                    status: 'pending'
                })
            } else {
                // 标记已上传分片
                this.chunks.push({
                    index,
                    chunk,
                    progress: 100,
                    status: 'success'
                });
            }
        }
    }

    async uploadAllChunks() {
        const uploadTasks = this.chunks
            .filter(chunk => chunk.status !== 'success')
            .map(chunk => this.limiter(() => this.uploadChunkWithRetry(chunk)))

        await Promise.all(uploadTasks)
    }

    async uploadChunkWithRetry(chunk) {
        // this.checkMemoryUsage(); // 上传前检查内存

        for (let retry = 0; retry <= MAX_RETRY; retry++) {
            try {
                const response = await this.uploadChunk(chunk)
                if (response.data.code == 200) {
                    chunk.status = 'success'
                    this.uploadedChunks.add(chunk.index)
                    this.saveLocalProgress()
                }
                return
            } catch (error) {
                if (retry === MAX_RETRY) {
                    chunk.status = 'error'
                    throw error
                }
                await new Promise(resolve =>
                    setTimeout(resolve, 1000 * Math.pow(2, retry))
                )
            }
        }
    }

    checkMemoryUsage() {
        if (window.performance?.memory) {
            const usedMB = window.performance.memory.usedJSHeapSize / 1024 / 1024;
            if (usedMB > 500) {
                console.warn(`内存使用过高: ${usedMB.toFixed(1)}MB, 建议减小分片大小`);
                this.chunkSize = Math.max(1 * 1024 * 1024, this.chunkSize / 2);
            }
        }
    }

    async uploadChunk(chunk) {
        let processedChunk = chunk.chunk;

        const formData = new FormData();


        if (this.isEncrypted) {
            // 分块加密处理
            processedChunk = await this.encryptChunk(chunk);

            console.assert(
                processedChunk.size % 16 === 0,
                `无效的加密块大小: ${processedChunk.size} bytes`
            );
            // 首分片携带salt，后续分片不携带
            if (chunk.index === 0) {
                formData.append('salt', CryptoJS.enc.Hex.stringify(this.salt));
            }
        }

        formData.append('file', processedChunk);
        formData.append('hash', this.fileHash);
        formData.append('index', chunk.index.toString());

        return axios.post(`${import.meta.env.VITE_APP_BASE_API}/minio/upload`, formData, {
            headers: {
                ...headers.value,
                'X-Encrypted': this.isEncrypted.toString()
            },
            onUploadProgress: (progressEvent) => {
                const percent = Math.round(
                    (progressEvent.loaded / progressEvent.total) * 100
                );
                chunk.progress = percent;
                this.updateTotalProgress();
            }
        });
    }

    // Blob -> WordArray
    async blobToWordArray(blob) {
        try {
            const arrayBuffer = await blob.arrayBuffer();
            const uint8Array = new Uint8Array(arrayBuffer);
            return CryptoJS.lib.WordArray.create(uint8Array);
        } catch (error) {
            console.error('Blob转换失败:', error);
            throw new Error('文件分片读取失败');
        }
    }

    // WordArray -> Blob
    wordArrayToBlob(wordArray) {
        try {
            // 1. 创建 Uint8Array 并填充数据
            const uint8Array = new Uint8Array(wordArray.sigBytes);
            let offset = 0;

            // 2. 遍历 WordArray 的每个 32 位字（4 字节）
            wordArray.words.forEach((word) => {
                uint8Array[offset++] = (word >> 24) & 0xff; // 最高有效字节
                uint8Array[offset++] = (word >> 16) & 0xff;
                uint8Array[offset++] = (word >> 8) & 0xff;
                uint8Array[offset++] = word & 0xff;         // 最低有效字节
            });

            // 3. 创建 Blob（截断到实际长度）
            return new Blob([uint8Array.slice(0, wordArray.sigBytes)], {
                type: 'application/octet-stream'
            });
        } finally {
            // 4. 强制释放内存（可选）
            wordArray.words = null;
            wordArray.sigBytes = 0;
        }
    }

    // UploadManager 类中修改 encryptChunk 方法
    async encryptChunk(chunk) {
        try {
            // 生成分片专属IV（基于文件哈希和分片索引）
            const ivSource = CryptoJS.SHA256(
                CryptoJS.enc.Utf8.parse(this.fileHash + chunk.index.toString())
            ).toString(); // SHA256哈希
            // console.log("[前端 IV 输入]", this.fileHash + chunk.index.toString()); // 记录输入字符串
            // console.log("[前端 IV 源]", ivSource.slice(0, 32)); // 记录生成的 IV 源
            const chunkIV = CryptoJS.enc.Hex.parse(ivSource.slice(0, 32)); // 取前128位作为IV

            // 转换Blob为WordArray
            const chunkData = await this.blobToWordArray(chunk.chunk);

            // 加密
            const encrypted = CryptoJS.AES.encrypt(
                chunkData,
                this.encryptionKey,
                {
                    iv: chunkIV,
                    mode: CryptoJS.mode.CBC,
                    padding: CryptoJS.pad.Pkcs7
                }
            );

            return this.wordArrayToBlob(encrypted.ciphertext);
        } catch (error) {
            console.error('分片加密失败:', error);
            throw error;
        }
    }

    updateTotalProgress() {
        const totalLoaded = this.chunks.reduce((sum, chunk) => {
            return sum + (chunk.progress / 100) * chunk.chunk.size;
        }, 0);
        this.progress.percentage = Math.round(
            (totalLoaded / this.file.size) * 100)
    }

    // 在mergeFile方法中添加加密参数处理
    async mergeFile() {
        const minioFileName = this.generateUniqueFileName(this.file.name);
        const payload = {
            filename: minioFileName, // 使用唯一文件名而不是原始文件名
            originalFilename: this.file.name, // 保留原始文件名
            fileHash: this.fileHash,
            totalChunks: Math.ceil(this.file.size / this.chunkSize),
            encrypted: this.isEncrypted,
            uploadedChunksCount: this.uploadedChunks.size
        };

        const response = await axios.post(
            `${import.meta.env.VITE_APP_BASE_API}/minio/merge`,
            payload,
            {
                headers: {
                    'Authorization': headers.value.Authorization,
                    'X-Encrypted': this.isEncrypted.toString()
                }
            }
        );
        result.value = response.data

        if (response.data.code === 200) {
            this.cleanLocalProgress
        } else {
            console.error('合并文件失败，文档上传失败');
        }
    }

    generateUniqueFileName(originalFileName) {
        // 生成13位时间戳 + 6位随机数
        const timestamp = Date.now();
        const randomNum = Math.floor(Math.random() * 999999).toString().padStart(6, '0');
        const uniquePrefix = `${timestamp}_${randomNum}`;

        // 获取文件扩展名
        const lastDotIndex = originalFileName.lastIndexOf('.');
        const extension = lastDotIndex > -1 ? originalFileName.substring(lastDotIndex) : '';
        const nameWithoutExt = lastDotIndex > -1 ? originalFileName.substring(0, lastDotIndex) : originalFileName;

        // 组合成新的文件名
        // return `${uniquePrefix}_${nameWithoutExt}${extension}`;
        const newFileName =  `${projectId.value}_${archive.value.id}_${uniquePrefix}${extension}`;
        console.log(newFileName)
        return newFileName;
    }

    // 本地存储相关方法
    saveLocalProgress() {
        const progressData = {
            fileHash: this.fileHash,
            uploadedChunks: Array.from(this.uploadedChunks)
        }
        localStorage.setItem(this.fileHash, JSON.stringify(progressData))
    }

    loadLocalProgress() {
        const data = localStorage.getItem(this.fileHash)
        if (data) {
            const { uploadedChunks } = JSON.parse(data)
            this.uploadedChunks = new Set(uploadedChunks)
        }
    }

    cleanLocalProgress() {
        localStorage.removeItem(this.fileHash)
    }
}
</script>

<style scoped>
.media-container {
    width: 100%;
    height: 400px;
    display: flex;
    justify-content: center;
    align-items: center;
    background: #f5f7fa;
}

.video-player {
    width: 100%;
    height: 100%;
    max-width: 1200px;
    background: #000;
}

.audio-player {
    width: 80%;
    margin: 0 auto;
}

.unsupported-file {
    padding: 20px;
    text-align: center;
}

.pdf-container {
    width: 100%;
    height: 100%;
    overflow: auto;
}

/* 设置编辑器容器高度并允许滚动 */
.editor-container {
    /* 固定高度 */
    height: 200px;
    /* 或使用 max-height: 400px; */

    /* 超出内容显示滚动条 */
    overflow-y: auto;

    /* 可选样式 */
    border: 1px solid #dcdfe6;
    border-radius: 4px;
    padding: 12px;
}

/* 如果编辑器内部有内容容器（如 .ql-editor），可能需要额外设置 */
.editor-container :deep(.ql-editor) {
    height: 100%;
    overflow-y: hidden;
    /* 防止内部产生双滚动条 */
}

.image-preview {
    max-width: 100%;
    /* 宽度不超过容器 */
    max-height: 100%;
    /* 高度不超过容器 */
    width: auto;
    /* 保持宽高比 */
    height: auto;
    /* 保持宽高比 */
    display: block;
    /* 避免 inline 元素底部间隙 */
    margin: 0 auto;
    /* 居中显示 */
    object-fit: contain;
    /* 图片按比例缩放，完整显示 */
    background-color: #fff;
    /* 可选背景色 */
    box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
    /* 可选阴影效果 */
}
</style>