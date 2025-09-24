<template>
    <div class="upload-container">
        <div class="upload-area" @dragover.prevent="handleDragOver" @drop.prevent="handleFileDrop">
            <input type="file" multiple @change="handleFileSelect" ref="fileInput" class="file-input">
            <div class="upload-prompt">
                <span class="material-icons"></span>
                <p>点击选择文件或拖拽文件到此区域</p>
                <p class="hint">支持批量上传，单文件最大5GB</p>
            </div>
        </div>

        <transition-group name="file-list" tag="div" class="file-list">
            <div v-for="file in uploadQueue" :key="file.id" class="file-item" :class="{
                'status-uploading': file.status === STATUS.UPLOADING,
                'status-success': file.status === STATUS.SUCCESS,
                'status-error': file.status === STATUS.ERROR,
                'status-stalled': isConnectionLost(file)
            }">
                <div class="file-info">
                    <span class="file-icon">
                        <span class="material-icons">{{ getFileIcon(file.name) }}</span>
                    </span>
                    <div class="file-meta">
                        <div class="filename">{{ file.name }}</div>
                        <div class="file-size">{{ formatFileSize(file.size) }}</div>
                        <!-- 错误信息展示区域 -->
                        <div v-if="file.status === STATUS.ERROR" class="error-detail">
                            {{ getErrorDetail(file) }}
                        </div>
                    </div>
                    <div class="file-actions">
                        <button v-if="file.status === STATUS.ERROR" @click="retryUpload(file)" class="retry-btn">
                            <span class="material-icons">refresh</span>
                            重试 ({{ MAX_FILE_RETRIES - file.retries }})
                        </button>
                        <button v-if="!['uploading', 'success'].includes(file.status)" @click="removeFile(file)"
                            class="remove-btn">
                            <span class="material-icons">close</span>
                        </button>
                    </div>
                </div>

                <div class="progress-container">
                    <div class="progress-bar">
                        <div class="progress-fill" :style="{ width: `${file.progress}%` }"></div>
                    </div>
                    <div class="progress-info">
                        <span class="status-text">{{ getStatusText(file) }}</span>
                        <span class="percentage">{{ file.progress }}%</span>
                    </div>
                </div>
            </div>
        </transition-group>

        <div class="global-controls">
            <button class="start-btn" @click="handleComplete" :disabled="!uploadQueue.length || isUploading">
                {{ uploadButtonText }}
            </button>
            <button class="clear-btn" @click="clearCompleted" :disabled="!hasCompletedFiles">
                清除已完成
            </button>
        </div>
    </div>
</template>

<script setup>
import { ref, reactive, computed, watch, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import SparkMD5 from 'spark-md5'
import CryptoJS from 'crypto-js'
import pLimit from 'p-limit'
import { getToken } from "@/utils/auth";
import { useRoute } from 'vue-router';
import { getArchive } from "@/api/manage/archive";
import { addDocument, listDocument, updateDocument, fullTextOcr, doOcr } from "@/api/manage/document";
import { getUserProfile } from "@/api/system/user.js";

const MAX_RETRY = 3
// 最大文件级重试次数
const MAX_FILE_RETRIES = 2
// 根据CPU核心数设置并行度
const CONCURRENCY = Math.max(1, navigator.hardwareConcurrency - 1);

const uploadStatus = ref('')
const route = useRoute()
const router = useRouter()
const user = ref(null);
const archive = ref(null)
const isEncrypt = ref(false)
/* 上传请求头 */
const headers = ref({ Authorization: "Bearer " + getToken() });
// 获取上传档案的信息
const importArchiveId = route.query.archiveId
const projectId = ref(null)

onMounted(async () => {
    getArchive(importArchiveId).then(response => {
        archive.value = response.data;
        if (archive.value.secretLevel === '3l') {
            isEncrypt.value = true
        }
        projectId.value = archive.value.projectId
    })

    getUser()

    // 网络状态监听
    isOnline.value = navigator.onLine
    window.addEventListener('online', () => isOnline.value = true)
    window.addEventListener('offline', () => isOnline.value = false)
})

onBeforeUnmount(() => {
    window.removeEventListener('online', () => isOnline.value = true)
    window.removeEventListener('offline', () => isOnline.value = false)
})

function getUser() {
    getUserProfile().then(response => {
        user.value = response.data;
        console.log("Current user:", user.value);
    });
};

// 页面修改
const STATUS = {
    PENDING: 'pending',
    UPLOADING: 'uploading',
    SUCCESS: 'success',
    ERROR: 'error'
}
const fileInput = ref(null)
const uploadQueue = reactive([])
let nextFileId = 1

// 计算属性
const isUploading = computed(() =>
    uploadQueue.some(file => file.status === STATUS.UPLOADING)
)

const uploadButtonText = computed(() => {
    if (isUploading.value) return '上传中...'
    const pendingCount = uploadQueue.filter(f =>
        [STATUS.PENDING, STATUS.ERROR].includes(f.status)
    ).length
    return pendingCount ? `开始上传 (${pendingCount})` : '全部完成'
})

const hasCompletedFiles = computed(() =>
    uploadQueue.some(file => file.status === STATUS.SUCCESS)
)

// 网络状态检测
const isOnline = ref(true)

// 自动恢复上传
watch(isOnline, (newVal) => {
    if (newVal) {
        startAllUploads()
    }
})

// 文件处理方法
const handleFileSelect = (e) => {
    addFilesToQueue([...e.target.files])
    e.target.value = null // 清除input值允许重复选择
}

const handleFileDrop = (e) => {
    addFilesToQueue([...e.dataTransfer.files])
}

const addFilesToQueue = (files) => {
    files.forEach(file => {
        if (!uploadQueue.some(f => f.name === file.name && f.size === file.size)) {
            uploadQueue.push({
                id: nextFileId++,
                file,
                name: file.name,
                size: file.size,
                progress: 0,
                status: STATUS.PENDING,
                retries: 0,
                manager: null,
                errorMsg: '',
                lastProgressTime: Date.now()
            })
        }
    })
}

const handleComplete = () => {
    if (uploadButtonText.value === '全部完成') {
        router.push({ path: '/manage/archive/arc-detail', query: { id: importArchiveId } })
    } else {
        startAllUploads()
    }
}

// 上传控制方法
const startAllUploads = async () => {
    for (const fileItem of uploadQueue) {
        // 跳过已完成文件
        if (fileItem.status === STATUS.SUCCESS) continue

        try {
            // 检查重试次数
            if (fileItem.retries >= MAX_FILE_RETRIES) {
                fileItem.status = STATUS.ERROR
                continue
            }

            // 执行上传
            await startSingleUpload(fileItem)
        } catch (error) {
            // 网络错误特殊处理
            fileItem.status = STATUS.ERROR
            fileItem.progress = 0
            continue // 继续下一个文件
        }
    }
}

// 改进的单文件上传方法
const startSingleUpload = async (fileItem) => {
    try {
        fileItem.status = STATUS.UPLOADING
        fileItem.progress = 0 // 重置进度
        fileItem.errorMsg = '' // 清除错误信息
        fileItem.manager = new UploadManager(fileItem.file, isEncrypt.value)
        await fileItem.manager.initialize()

        let lastProgressTime = Date.now()
        let lastProgress = 0

        fileItem.manager.onProgress = (progress) => {
            // 防止进度倒退
            if (progress >= lastProgress) {
                fileItem.progress = progress
                lastProgress = progress
                lastProgressTime = Date.now()
                fileItem.lastProgressTime = lastProgressTime
            }
        }

        // 添加超时监控（30秒无进展视为失败）
        const progressWatcher = setInterval(() => {
            if (Date.now() - lastProgressTime > 30000 && fileItem.progress < 100) {
                clearInterval(progressWatcher)
                throw new Error('上传超时')
            }
        }, 1000)

        // 执行上传
        const success = await fileItem.manager.startUpload()
        clearInterval(progressWatcher)

        if (success) {
            fileItem.status = STATUS.SUCCESS
            fileItem.progress = 100
        } else {
            fileItem.status = STATUS.ERROR
            fileItem.progress = 0
            fileItem.errorMsg = '上传失败'
        }
    } catch (error) {
        console.error(`[${fileItem.name}] 上传失败:`, error)
        fileItem.retries++
        fileItem.status = fileItem.retries < MAX_FILE_RETRIES
            ? STATUS.PENDING
            : STATUS.ERROR
        fileItem.progress = 0 // 失败时重置进度
        fileItem.errorMsg = error.message // 保存错误信息
        throw error // 向上传播错误
    }
}

const retryUpload = async (fileItem) => {
    fileItem.status = STATUS.PENDING
    fileItem.progress = 0
    fileItem.errorMsg = ''
    await startSingleUpload(fileItem)
}

// UI辅助方法
const getStatusText = (file) => {
    switch (file.status) {
        case STATUS.UPLOADING: return '上传中...'
        case STATUS.SUCCESS: return '上传成功'
        case STATUS.ERROR: return `上传失败 (已重试 ${file.retries} 次)`
        default: return '等待上传'
    }
}

const formatFileSize = (bytes) => {
    if (bytes === 0) return '0 Bytes'
    const k = 1024
    const sizes = ['Bytes', 'KB', 'MB', 'GB']
    const i = Math.floor(Math.log(bytes) / Math.log(k))
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

const getFileIcon = (filename) => {
    const ext = filename.split('.').pop().toLowerCase()
    const icons = {
        pdf: 'pdf文档',
        mp4: 'mp4视频',
        mp3: 'mp3音频',
        png: 'png图片',
        jpg: 'jpg图片',
        jpeg: 'jpeg图片',
        gif: 'gif图片',
        doc: 'doc文档',
        docx: 'docx文档',
        xls: 'xls文档',
        xlsx: 'xlsx文档',
        ppt: 'ppt文档',
        pptx: 'pptx文档',
        txt: 'txt文档',
        default: '未知文件'
    }
    return icons[ext] || icons.default
}

// 清理方法
const removeFile = (fileItem) => {
    const index = uploadQueue.findIndex(f => f.id === fileItem.id)
    if (index !== -1) {
        uploadQueue.splice(index, 1)
    }
}

const clearCompleted = () => {
    for (let i = uploadQueue.length - 1; i >= 0; i--) {
        if (uploadQueue[i].status === STATUS.SUCCESS) {
            uploadQueue.splice(i, 1)
        }
    }
}

// 错误详情生成方法
const getErrorDetail = (fileItem) => {
    if (fileItem.errorMsg?.includes('413')) {
        return '文件分片过大，已自动调整分片大小重试'
    }
    if (fileItem.errorMsg?.includes('Network Error')) {
        return '网络连接已中断，请检查网络后重试'
    }
    if (fileItem.errorMsg?.includes('timeout') || fileItem.errorMsg?.includes('超时')) {
        return '上传超时，请检查网络稳定性'
    }
    if (fileItem.retries >= MAX_FILE_RETRIES) {
        return '上传失败（已达最大重试次数）'
    }
    return `上传失败（${fileItem.errorMsg || '未知错误'}）`
}

// 网络状态检测
const isConnectionLost = (fileItem) => {
    return fileItem.status === STATUS.UPLOADING &&
        !navigator.onLine &&
        Date.now() - fileItem.lastProgressTime > 5000
}

// 改进的文件上传类
class UploadManager {
    constructor(file, isEncrypted = false) {
        this.file = file
        this.isEncrypted = isEncrypted
        this.originalChunkSize = this.calculateDynamicChunkSize(file.size);
        this.chunkSize = this.originalChunkSize;
        this.maxChunkSize = 1 * 1024 * 1024; // 1MB硬限制
        this.chunks = reactive([])
        this.fileHash = ''
        this.encryptionKey = null
        this.iv = null
        this.salt = null
        this.currentIV = null
        this.hasRechunked = false // 标记是否已重新分片
        this.totalProgress = 0 // 添加总进度跟踪
        this.failedChunks = new Set() // 跟踪失败的分片
        this.progress = reactive({
            percentage: 0,
            uploadedSize: 0
        })
        this.uploadedChunks = new Set()
        this.retryCounts = new Map()
        this.limiter = pLimit(CONCURRENCY)
        this.onProgress = null // 进度回调
    }

    // 改进的分片大小计算
    calculateDynamicChunkSize(fileSize) {
        const sizeMB = fileSize / 1024 / 1024;
        // 大幅减小分片大小，避免413错误
        if (sizeMB > 1024) return 2 * 1024 * 1024;   // 大文件2MB分片
        if (sizeMB > 100) return 1 * 1024 * 1024;    // 中等文件1MB分片  
        return 512 * 1024;                           // 小文件512KB分片
    }

    async initialize() {
        this.fileHash = await this.calculateHash()

        // 检查本地断点续传信息
        const localProgress = this.loadLocalProgress();
        const serverProgress = await this.checkServerProgress();

        // 如果本地和服务器都有进度信息，验证兼容性
        if (localProgress && serverProgress) {
            const isCompatible = await this.validateProgressCompatibility(localProgress, serverProgress);
            if (!isCompatible) {
                console.warn('检测到分片大小不兼容，清除断点续传信息');
                this.cleanLocalProgress();
                this.uploadedChunks.clear();
            }
        }

        // 恢复salt和密钥
        const storedParams = localStorage.getItem(`encParams-${this.fileHash}`);
        if (storedParams) {
            const { salt } = JSON.parse(storedParams);
            this.salt = CryptoJS.enc.Hex.parse(salt);
            this.encryptionKey = this.deriveKey(this.salt);
        }

        if (this.isEncrypted && !this.salt) {
            await this.generateEncryptionParams()
            await this.validateEncryption()
        }
    }

    async checkServerProgress() {
        try {
            const { data } = await axios.get(`${import.meta.env.VITE_APP_BASE_API}/minio/check`, {
                params: {
                    fileHash: this.fileHash,
                    filename: this.file.name,
                    chunkSize: this.chunkSize // 传递当前分片大小
                },
                headers: {
                    'Authorization': headers.value.Authorization,
                }
            });

            if (data.uploadedChunks) {
                data.uploadedChunks.forEach(chunk => this.uploadedChunks.add(chunk));
                return data;
            }
            return null;
        } catch (error) {
            console.error('检查服务器进度失败:', error);
            return null;
        }
    }

    async validateProgressCompatibility(localProgress, serverProgress) {
        // 检查分片大小是否匹配
        const localChunkSize = localProgress.chunkSize || this.originalChunkSize;
        const serverChunkSize = serverProgress.chunkSize || this.originalChunkSize;

        return localChunkSize === serverChunkSize &&
            localChunkSize === this.chunkSize;
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

    // 改进的自适应分片调整
    async adjustChunkSizeOnError(error) {
        if (error.response?.status === 413) {
            // 标记已重新分片，清除不兼容的断点信息
            this.hasRechunked = true;
            this.cleanLocalProgress();
            this.uploadedChunks.clear();

            // 调整分片大小
            this.chunkSize = Math.max(this.chunkSize / 2, 256 * 1024);
            console.log(`检测到413错误，调整分片大小为: ${this.formatFileSize(this.chunkSize)}`);
            return true;
        }
        return false;
    }

    async startUpload() {
        uploadStatus.value = '开始上传...';
        this.totalProgress = 0; // 重置总进度

        try {
            let rechunkAttempts = 0;
            const maxRechunkAttempts = 2;

            while (rechunkAttempts <= maxRechunkAttempts) {
                try {
                    const generator = this.chunkGenerator();
                    this.prepareChunks(generator);

                    // 初始化进度
                    this.updateTotalProgress();

                    await this.uploadAllChunks();
                    const result = await this.mergeFile();

                    if (result === 200) {
                        // 完成时设置100%
                        if (this.onProgress) {
                            this.onProgress(100);
                        }
                        uploadStatus.value = '上传完成!';
                        return true;
                    } else {
                        uploadStatus.value = '合并文件失败!';
                        return false;
                    }
                } catch (error) {
                    if (error.message === 'RECHUNK_NEEDED' && rechunkAttempts < maxRechunkAttempts) {
                        rechunkAttempts++;
                        console.log(`第 ${rechunkAttempts} 次重新分片，新分片大小: ${this.formatFileSize(this.chunkSize)}`);

                        // 重置状态
                        this.chunks.length = 0;
                        this.uploadedChunks.clear();
                        this.failedChunks.clear();
                        this.totalProgress = 0;

                        continue;
                    }
                    throw error;
                }
            }

            throw new Error('重新分片次数已达上限');
        } catch (error) {
            uploadStatus.value = `上传失败: ${error.message}`;
            console.error('上传失败:', error);
            throw error;
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
            .map(chunk => this.limiter(() =>
                this.uploadChunkWithRetry(chunk).finally(() => {
                    // 每个分片完成时更新进度
                    this.updateTotalProgress();
                })
            ));

        await Promise.all(uploadTasks)
    }

    async uploadChunkWithRetry(chunk) {
        chunk.status = 'uploading';
        this.failedChunks.delete(chunk.index); // 重试时移除失败标记

        for (let retry = 0; retry <= MAX_RETRY; retry++) {
            try {
                const response = await this.uploadChunk(chunk);
                if (response.data.code === 500) {
                    throw new Error('服务器返回错误');
                }

                // 上传成功
                chunk.status = 'success';
                chunk.progress = 100;
                this.uploadedChunks.add(chunk.index);
                this.saveLocalProgress(); // 保存进度
                this.updateTotalProgress(); // 更新总进度
                return response;

            } catch (error) {
                console.error(`分片 ${chunk.index} 上传失败 (重试 ${retry}):`, error.message);

                // 更新分片状态为失败
                chunk.status = 'error';
                chunk.progress = 0; // 失败时重置进度
                this.failedChunks.add(chunk.index);

                // 检查413错误
                if (error.response?.status === 413 && retry === 0) {
                    const needReChunk = await this.adjustChunkSizeOnError(error);
                    if (needReChunk) {
                        throw new Error('RECHUNK_NEEDED');
                    }
                }

                // 网络错误立即失败
                if (error.message.includes('Network Error')) {
                    this.updateTotalProgress(); // 确保进度条反映失败状态
                    throw new Error('网络中断');
                }

                if (retry === MAX_RETRY) {
                    this.updateTotalProgress(); // 最终失败时更新进度
                    throw new Error(`分片 ${chunk.index} 上传失败: ${error.message}`);
                }

                // 重试前等待
                await new Promise(resolve =>
                    setTimeout(resolve, 1000 * Math.pow(2, retry))
                );
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
                if (progressEvent.total > 0) {
                    const percent = Math.round((progressEvent.loaded / progressEvent.total) * 100);
                    chunk.progress = percent;

                    // 实时更新总进度
                    this.updateTotalProgress();
                }
            },
            timeout: 60000 // 60秒超时
        });
    }

    // 改进的进度更新方法
    updateTotalProgress() {
        if (this.chunks.length === 0) return;

        // 计算已完成的分片（成功上传的）
        const completedChunks = this.chunks.filter(chunk =>
            chunk.status === 'success' || this.uploadedChunks.has(chunk.index)
        );

        // 计算正在上传的分片进度
        const uploadingProgress = this.chunks
            .filter(chunk => chunk.status === 'uploading')
            .reduce((sum, chunk) => sum + (chunk.progress || 0), 0);

        // 总进度计算：已完成分片 + 正在上传分片的部分进度
        const completedSize = completedChunks.length * this.chunkSize;
        const uploadingSize = (uploadingProgress / 100) * this.chunkSize *
            this.chunks.filter(chunk => chunk.status === 'uploading').length;

        const totalSize = this.file.size;
        let percentage = totalSize > 0
            ? Math.round(((completedSize + uploadingSize) / totalSize) * 100)
            : 0;

        // 确保进度不超过99%（完成前）且不倒退
        percentage = Math.min(percentage, 99);
        percentage = Math.max(percentage, this.totalProgress); // 防止倒退

        this.totalProgress = percentage;

        if (this.onProgress) {
            this.onProgress(percentage);
        }
    }

    // Blob -> WordArray
    async blobToWordArray(blob) {
        try {
            const arrayBuffer = await blob.arrayBuffer();
            const uint8Array = new Uint8Array(arrayBuffer);
            return CryptoJS.lib.WordArray.create(uint8Array);
        } catch (error) {
            console.error('Blob转换失败:', error);
            throw new Error('文件读取失败');
        }
    }

    // WordArray -> Blob
    wordArrayToBlob(wordArray) {
        try {
            const uint8Array = new Uint8Array(wordArray.sigBytes);
            for (let i = 0; i < wordArray.sigBytes; i++) {
                const wordIndex = Math.floor(i / 4);
                const byteIndex = 3 - (i % 4);
                uint8Array[i] = (wordArray.words[wordIndex] >>> (byteIndex * 8)) & 0xff;
            }
            return new Blob([uint8Array]);
        } catch (error) {
            console.error('WordArray转换失败:', error);
            throw new Error('加密数据处理失败');
        }
    }

    async encryptChunk(chunk) {
        try {
            const wordArray = await this.blobToWordArray(chunk.chunk);

            // 为每个分片生成独立的IV
            const chunkIV = CryptoJS.lib.WordArray.random(128 / 8);

            const encrypted = CryptoJS.AES.encrypt(wordArray, this.encryptionKey, {
                iv: chunkIV,
                mode: CryptoJS.mode.CBC,
                padding: CryptoJS.pad.Pkcs7
            });

            // 将IV和加密数据合并：IV(16字节) + 加密数据
            const ivWords = chunkIV.words;
            const encryptedWords = encrypted.ciphertext.words;
            const combinedWords = [...ivWords, ...encryptedWords];

            const combinedWordArray = CryptoJS.lib.WordArray.create(
                combinedWords,
                16 + encrypted.ciphertext.sigBytes
            );

            return this.wordArrayToBlob(combinedWordArray);
        } catch (error) {
            console.error('分片加密失败:', error);
            throw new Error('文件加密失败');
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

        // 组合成新的文件名
        // return `${uniquePrefix}_${nameWithoutExt}${extension}`;
        const newFileName = `${projectId.value}_${archive.value.id}_${uniquePrefix}${extension}`;
        console.log(newFileName)
        return newFileName;
    }

    async mergeFile() {
        try {
            const minioFileName = this.generateUniqueFileName(this.file.name);

            const { data } = await axios.post(`${import.meta.env.VITE_APP_BASE_API}/minio/merge`, {
                fileHash: this.fileHash,
                filename: minioFileName,
                totalChunks: this.chunks.length,
                archiveId: importArchiveId,
                encrypted: this.isEncrypted
            }, {
                headers: headers.value,
                timeout: 120000
            });

            if (data.code === 200) {
                const filePath = data.msg;
                // 传递原始文件名和minio文件名
                this.createDocumentRecord(this.file, filePath, null, minioFileName);
                this.cleanLocalProgress();
                return 200;
            }
            return data.code;
        } catch (error) {
            console.error('合并文件失败:', error);
            throw new Error('文件合并失败');
        }
    }

    /**
   * 创建文档记录
   * @param {File} rawFile - 原始文件对象
   * @param {string} filePath - 服务端返回的存储路径
   * @param {string} content - OCR 提取的内容
   * @param {string} minioFileName - minio中的唯一文件名
   */
    createDocumentRecord = async (rawFile, filePath, content, minioFileName) => {
        // 验证必要参数存在
        if (!rawFile || !rawFile.name) {
            throw new Error('无效的文件对象，缺少必要属性');
        }

        if (!minioFileName) {
            throw new Error('minio文件名不能为空');
        }

        // 更可靠的文件类型获取方式
        const getFileType = () => {
            // 优先使用MIME类型判断
            const mimeType = rawFile.type.split('/').pop(); // 例如：pdf从application/pdf中提取
            if (mimeType && mimeType !== 'octet-stream') {
                return mimeType.toLowerCase();
            }
            // 回退到文件扩展名
            const ext = rawFile.name.split('.').pop();
            return ext ? ext.toLowerCase() : 'unknown';
        };

        const importCategoryCode = route.query.categoryCode ? route.query.categoryCode : '';

        // 构造文档数据
        const docData = {
            archiveId: importArchiveId,
            projectId: projectId.value,
            categoryCode: importCategoryCode,
            createBy: user.value.userName,
            name: rawFile.name, // 保存原始文件名
            minioName: minioFileName, // 保存minio中的唯一文件名
            fileType: getFileType(), // 改进的类型获取
            fileSize: Math.round(rawFile.size / 1024 * 10) / 10, // 更精确的四舍五入
            filePath: filePath, // minio返回的完整URL路径
            content: content,
            uploadTime: new Date().toISOString(),
            encryptFlag: isEncrypt.value ? 1 : 0 // 修正变量名拼写
        };
        // 参数验证
        if (!docData.filePath || docData.fileSize <= 0) {
            throw new Error('无效的文档参数: ' + JSON.stringify(docData));
        }
        try {
            console.log('[DEBUG] 创建文档记录请求:', {
                原始文件名: docData.name,
                minio文件名: docData.minioName,
                文件路径: docData.filePath,
                其他数据: docData
            });
            // 往数据库添加一条文档记录
            const response = await addDocument(docData);
            console.log('[DEBUG] 创建文档记录响应:', response);
            return response;
        } catch (error) {
            console.error('[ERROR] 文档记录创建失败:', {
                requestData: docData,
                errorDetails: {
                    message: error.message,
                    stack: error.stack,
                    response: error.response?.data
                }
            });
            throw new Error(`创建失败: ${error.message}`);
        }
    }

    // 改进的本地存储
    saveLocalProgress() {
        const progressData = {
            fileHash: this.fileHash,
            uploadedChunks: Array.from(this.uploadedChunks),
            chunkSize: this.chunkSize, // 保存分片大小
            hasRechunked: this.hasRechunked,
            timestamp: Date.now()
        }
        localStorage.setItem(this.fileHash, JSON.stringify(progressData))
    }

    loadLocalProgress() {
        const data = localStorage.getItem(this.fileHash)
        if (data) {
            try {
                const parsed = JSON.parse(data);

                // 检查数据完整性和时效性（7天过期）
                if (parsed.timestamp && Date.now() - parsed.timestamp > 7 * 24 * 60 * 60 * 1000) {
                    this.cleanLocalProgress();
                    return null;
                }

                if (parsed.chunkSize === this.chunkSize) {
                    this.uploadedChunks = new Set(parsed.uploadedChunks || []);
                    this.hasRechunked = parsed.hasRechunked || false;
                    return parsed;
                } else {
                    console.warn('分片大小不匹配，忽略本地进度');
                    this.cleanLocalProgress();
                }
            } catch (error) {
                console.error('解析本地进度失败:', error);
                this.cleanLocalProgress();
            }
        }
        return null;
    }

    cleanLocalProgress() {
        localStorage.removeItem(this.fileHash);
        localStorage.removeItem(`encParams-${this.fileHash}`);
    }

    formatFileSize(bytes) {
        if (bytes === 0) return '0 Bytes'
        const k = 1024
        const sizes = ['Bytes', 'KB', 'MB', 'GB']
        const i = Math.floor(Math.log(bytes) / Math.log(k))
        return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
    }
}

const handleDragOver = (e) => {
    e.preventDefault()
    e.dataTransfer.dropEffect = 'copy'
}
</script>

<style scoped>
.upload-container {
    max-width: 800px;
    margin: 0 auto;
    padding: 20px;
}

.upload-area {
    border: 2px dashed #ddd;
    border-radius: 8px;
    padding: 40px;
    text-align: center;
    cursor: pointer;
    transition: all 0.3s ease;
    position: relative;
}

.upload-area:hover {
    border-color: #007bff;
    background-color: #f8f9fa;
}

.file-input {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    opacity: 0;
    cursor: pointer;
}

.upload-prompt {
    pointer-events: none;
}

.upload-prompt .material-icons {
    font-size: 48px;
    color: #6c757d;
    margin-bottom: 16px;
}

.upload-prompt p {
    margin: 8px 0;
    color: #6c757d;
}

.hint {
    font-size: 14px;
    opacity: 0.8;
}

.file-list {
    margin: 20px 0;
}

.file-item {
    border: 1px solid #e9ecef;
    border-radius: 8px;
    padding: 16px;
    margin-bottom: 12px;
    background: white;
    transition: all 0.3s ease;
}

.file-item.status-uploading {
    border-color: #007bff;
    box-shadow: 0 2px 4px rgba(0, 123, 255, 0.1);
}

.file-item.status-success {
    border-color: #28a745;
    background-color: #f8fff9;
}

.file-item.status-error {
    border-color: #dc3545;
    background-color: #fff8f8;
}

.file-item.status-stalled {
    border-color: #ffc107;
    background-color: #fffdf5;
}

.file-info {
    display: flex;
    align-items: center;
    margin-bottom: 12px;
}

.file-icon {
    margin-right: 12px;
    color: #6c757d;
}

.file-meta {
    flex: 1;
}

.filename {
    font-weight: 500;
    margin-bottom: 4px;
}

.file-size {
    font-size: 14px;
    color: #6c757d;
}

.error-detail {
    font-size: 12px;
    color: #dc3545;
    margin-top: 4px;
    padding: 4px 8px;
    background-color: rgba(220, 53, 69, 0.1);
    border-radius: 4px;
}

.file-actions {
    display: flex;
    gap: 8px;
}

.retry-btn,
.remove-btn {
    padding: 6px 12px;
    border: none;
    border-radius: 4px;
    cursor: pointer;
    font-size: 12px;
    display: flex;
    align-items: center;
    gap: 4px;
    transition: all 0.2s ease;
}

.retry-btn {
    background-color: #007bff;
    color: white;
}

.retry-btn:hover {
    background-color: #0056b3;
}

.remove-btn {
    background-color: #dc3545;
    color: white;
}

.remove-btn:hover {
    background-color: #c82333;
}

.progress-container {
    margin-top: 8px;
}

.progress-bar {
    height: 6px;
    background-color: #e9ecef;
    border-radius: 3px;
    overflow: hidden;
    margin-bottom: 8px;
}

.progress-fill {
    height: 100%;
    background: linear-gradient(90deg, #007bff, #0056b3);
    transition: width 0.3s ease;
    border-radius: 3px;
}

.status-success .progress-fill {
    background: linear-gradient(90deg, #28a745, #1e7e34);
}

.status-error .progress-fill {
    background: linear-gradient(90deg, #dc3545, #c82333);
}

.progress-info {
    display: flex;
    justify-content: space-between;
    font-size: 12px;
    color: #6c757d;
}

.status-text {
    font-weight: 500;
}

.percentage {
    font-weight: bold;
}

.global-controls {
    display: flex;
    gap: 12px;
    justify-content: center;
    margin-top: 20px;
}

.start-btn,
.clear-btn {
    padding: 12px 24px;
    border: none;
    border-radius: 6px;
    font-weight: 500;
    cursor: pointer;
    transition: all 0.2s ease;
}

.start-btn {
    background-color: #007bff;
    color: white;
}

.start-btn:hover:not(:disabled) {
    background-color: #0056b3;
}

.start-btn:disabled {
    background-color: #6c757d;
    cursor: not-allowed;
}

.clear-btn {
    background-color: #6c757d;
    color: white;
}

.clear-btn:hover:not(:disabled) {
    background-color: #545b62;
}

.clear-btn:disabled {
    background-color: #adb5bd;
    cursor: not-allowed;
}

/* 动画效果 */
.file-list-enter-active,
.file-list-leave-active {
    transition: all 0.3s ease;
}

.file-list-enter-from {
    opacity: 0;
    transform: translateY(-20px);
}

.file-list-leave-to {
    opacity: 0;
    transform: translateX(20px);
}

/* 响应式设计 */
@media (max-width: 768px) {
    .upload-container {
        padding: 16px;
    }

    .upload-area {
        padding: 24px 16px;
    }

    .file-info {
        flex-direction: column;
        align-items: flex-start;
    }

    .file-actions {
        margin-top: 8px;
        align-self: flex-end;
    }

    .global-controls {
        flex-direction: column;
    }

    .start-btn,
    .clear-btn {
        width: 100%;
    }
}

/* 深色模式支持 */
@media (prefers-color-scheme: dark) {
    .upload-area {
        border-color: #495057;
        background-color: #343a40;
    }

    .upload-area:hover {
        border-color: #007bff;
        background-color: #495057;
    }

    .file-item {
        background-color: #343a40;
        border-color: #495057;
    }

    .upload-prompt p {
        color: #adb5bd;
    }
}
</style>
