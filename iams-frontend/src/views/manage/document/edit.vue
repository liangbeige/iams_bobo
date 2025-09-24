<template>
  <div class="app-container">
    <el-card class="box-card">
      <div slot="header" class="clearfix">
        <h1 style="text-align: center;">文档修改页面</h1>
      </div>
      <el-form ref="documentRef" :model="form" :rules="rules" label-position="top" label-width="80px">
        <el-row :gutter="20">
          <el-col :span="8">
            <!-- 左侧表单保持不变 -->
            <el-form-item label="所属档案" prop="archiveId">
              <el-select v-model="form.archiveId" placeholder="请选择档案" @change="getArchiveById" style="width: 100%;">
                <el-option v-for="archive in archiveList" :key="archive.id" :label="archive.name" :value="archive.id" />
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
                  <el-input v-model="form.fileSize" placeholder="请输入文件大小(Kb)" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-select v-model="selectedCategoryCodes" multiple placeholder="请选择门类" :render-after-expand="false"
                       style="width:100%;">
              <el-option v-for="category in archiveCategoryList" :key="category" :label="category" :value="category">
              </el-option>
            </el-select>
            <el-form-item label="备注" prop="remark">
              <el-input v-model="form.remark" type="textarea" placeholder="请输入备注内容" />
            </el-form-item>
            <el-row :gutter="20">
              <el-col :span="6">
                <el-form-item label="真实性" prop="authenticity" label-position="left">
                  <el-switch v-model="formBoolean.authenticity" />
                </el-form-item>
              </el-col>
              <el-col :span="6">
                <el-form-item label="完整性" prop="integrity">
                  <el-switch v-model="formBoolean.integrity" />
                </el-form-item>
              </el-col>
              <el-col :span="6">
                <el-form-item label="可用性" prop="availability">
                  <el-switch v-model="formBoolean.availability" />
                </el-form-item>
              </el-col>
              <el-col :span="6">
                <el-form-item label="安全性" prop="security">
                  <el-switch v-model="formBoolean.security" />
                </el-form-item>
              </el-col>
            </el-row>
          </el-col>
          <el-col :span="16">
            <div class="media-container">
              <!-- PDF 文件 -->
              <div v-if="isPDF" class="pdf-container">
                <div v-loading="pdfLoading" class="pdfjs-container">
                  <!-- PDF导航栏 -->
                  <div class="pdf-toolbar">
                    <el-button-group>
                      <el-button
                          size="small"
                          @click="changePage(-1)"
                          :disabled="currentPage <= 1"
                          icon="el-icon-arrow-left">
                        上一页
                      </el-button>
                      <el-button size="small" disabled>
                        {{ currentPage }} / {{ totalPages }}
                      </el-button>
                      <el-button
                          size="small"
                          @click="changePage(1)"
                          :disabled="currentPage >= totalPages"
                          icon="el-icon-arrow-right">
                        下一页
                      </el-button>
                    </el-button-group>

                    <el-button-group style="margin-left: 20px;">
                      <el-button size="small" @click="zoomOut" icon="el-icon-zoom-out">缩小</el-button>
                      <el-button size="small" disabled>{{ Math.round(pdfScale * 100) }}%</el-button>
                      <el-button size="small" @click="zoomIn" icon="el-icon-zoom-in">放大</el-button>
                    </el-button-group>

                    <!-- 新增旋转按钮组 -->
                    <el-button-group style="margin-left: 20px;">
                      <el-button
                          size="small"
                          @click="rotatePage(-90)"
                          icon="el-icon-refresh-left"
                          title="逆时针旋转90°">
                        左转
                      </el-button>
                      <el-button size="small" disabled>
                        {{ pdfRotation }}°
                      </el-button>
                      <el-button
                          size="small"
                          @click="rotatePage(90)"
                          icon="el-icon-refresh-right"
                          title="顺时针旋转90°">
                        右转
                      </el-button>
                    </el-button-group>
                  </div>

                  <!-- PDF页面渲染区 -->
                  <div id="pdfViewerContainer" class="pdf-viewer-container">
                    <canvas ref="pdfCanvas" class="pdf-canvas"></canvas>
                  </div>
                </div>
              </div>

              <!-- 图片文件 -->


              <!-- 视频文件 -->
              <video-player v-else-if="isVideo" :src="mediaUrl" :poster="videoPoster" controls class="video-player" />

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
        <el-button type="primary" @click="submitForm">确 定</el-button>
      </template>
    </el-card>
  </div>
</template>

<script setup lang="js">
import { ref, computed, reactive, toRefs, onMounted, nextTick, shallowRef, onUnmounted } from "vue";
import { getDocument, updateDocument } from "@/api/manage/document";
import { getArchive, listArchive } from "@/api/manage/archive";
import { getUserProfile } from "@/api/system/user";
import VideoPlayer from '@/components/Player/VideoPlayer'
import AudioPlayer from '@/components/Player/AudioPlayer'
import axios from 'axios'

import useTagsViewStore from '@/store/modules/tagsView.js'
import {useRoute, useRouter} from 'vue-router';

// 导入 pdfjs-dist
import * as pdfjsLib from 'pdfjs-dist/legacy/build/pdf';
const workerUrl = new URL(
    'pdfjs-dist/legacy/build/pdf.worker.min.js',
    import.meta.url
).toString();

// 设置 worker
pdfjsLib.GlobalWorkerOptions.workerSrc = workerUrl;

const tagsViewStore = useTagsViewStore();
const router = useRouter();
const route = useRoute();

const { proxy } = getCurrentInstance();
const { iams_media_type } = proxy.useDict('iams_media_type');

const user = ref({});

const data = reactive({
  form: {},
  rules: {
    name: [
      { required: true, message: "文件名称不能为空", trigger: "blur" }
    ],
  }
});

const archive = ref({});
const directory = ref([]);
const oldData = ref({});
const archiveList = ref([]);

const mediaUrl = ref(null)
const isPDF = ref(false)
const isVideo = ref(false)
const isAudio = ref(false)
const isImage = ref(false)
const selectedCategoryCodes = ref([])
const archiveCategoryList = ref([]) // 档案自带的分类列表

// PDF.js 相关状态
const pdfDoc = shallowRef(null)
const currentPage = ref(1)
const totalPages = ref(0)
const pdfScale = ref(1.5)
const pdfLoading = ref(false)
const renderTask = shallowRef(null)
const pdfCanvas = ref(null)
const pdfRotation = ref(0)

const { form, rules } = toRefs(data);

// 定义需要转换的字段列表
const booleanFields = ['authenticity', 'integrity', 'availability', 'security'];

// 创建中间计算属性对象
const formBoolean = reactive({});
booleanFields.forEach(field => {
  formBoolean[field] = computed({
    get: () => form.value[field] === 1,
    set: (val) => { form.value[field] = val ? 1 : 0; }
  });
});

// 取消按钮
function cancel() {
  // 删除当前 tab
  if (tagsViewStore) {
    tagsViewStore.delView(route);
  }
  // 跳转到列表页
  router.replace({ path: '/manage/document/doc-list' });
}

// 表单重置
function reset() {
  form.value = {
    id: null,
    archiveId: null,
    xuhao: null,
    name: null,
    fileType: null,
    fileSize: null,
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

function changeDirectory(value) {
  console.log('Directory changed to:', value);
  form.value.directory = value;
}

/** 搜索档案 */
function getArchiveById() {
  getArchive(form.value.archiveId).then(response => {
    if (response.code == 200 && response.data) {
      archive.value = response.data;
      archiveCategoryList.value = archive.value.categoryId.split(",");
    } else {
      proxy.$modal.msgError("档案不存在");
    }
  }).catch(error => {
    console.error('API Error:', error);
    proxy.$modal.msgError("查询档案失败：" + error.message);
  });
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["documentRef"].validate(valid => {
    if (valid) {
      if (form.value.archiveId != oldData.value.archiveId || form.value.directory != oldData.value.directory) {
        console.log('Directory changed, updating...');
      }
      form.value.updateBy = user.value.userName;
      form.value.categoryCode = selectedCategoryCodes.value.join(",")
      updateDocument(form.value).then(response => {
        proxy.$modal.msgSuccess("修改成功");
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

// 文件类型判断逻辑
const checkFileType = () => {
  const fileType = form.value.fileType?.toLowerCase()
  isPDF.value = fileType === 'pdf'
  isVideo.value = ['mp4', 'avi', 'mov', 'wmv'].includes(fileType)
  isAudio.value = ['mp3', 'wav', 'ogg'].includes(fileType)
  isImage.value = ['png', 'jpg', 'jpeg', 'gif', 'bmp', 'webp'].includes(fileType)
}

// 获取媒体URL
const getMediaUrl = async () => {
  if (form.value.filePath.startsWith("http://127.0.0.1:8080")) {
    try {
      const response = await axios({
        url: form.value.filePath,
        method: 'GET',
        responseType: 'blob'
      })

      const blob = new Blob([response.data], {
        type: `application/${form.value.fileType}`
      })

      mediaUrl.value = URL.createObjectURL(blob)

      // 如果是PDF，初始化PDF.js
      if (isPDF.value) {
        await initPdfJs();
      }
    } catch (error) {
      console.error('Error fetching media:', error)
    }
  } else {
    mediaUrl.value = form.value.filePath

    // 如果是PDF，初始化PDF.js
    if (isPDF.value) {
      await initPdfJs();
    }
  }
}

// 初始化PDF.js
const initPdfJs = async () => {
  try {
    pdfLoading.value = true;

    // 确保 DOM 已经渲染
    await nextTick();

    // 加载PDF文档
    const loadingTask = pdfjsLib.getDocument({
      url: mediaUrl.value,
      withCredentials: false,
      cMapPacked: true,
    });

    pdfDoc.value = await loadingTask.promise;
    totalPages.value = pdfDoc.value.numPages;

    // 等待 DOM 更新后再渲染
    await nextTick();

    // 渲染第一页
    await renderPage(1);

  } catch (error) {
    console.error('PDF加载失败:', error);
    proxy.$modal.error('PDF文件加载失败');
  } finally {
    pdfLoading.value = false;
  }
}

// 渲染PDF页面
const renderPage = async (pageNum) => {
  if (!pdfDoc.value) return;

  try {
    // 如果有正在进行的渲染任务，先取消
    if (renderTask.value) {
      renderTask.value.cancel();
    }

    const page = await pdfDoc.value.getPage(pageNum);
    // 在 getViewport 时应用旋转角度
    const viewport = page.getViewport({
      scale: pdfScale.value,
      rotation: pdfRotation.value
    });

    // 使用 ref 而不是 getElementById
    await nextTick(); // 确保 DOM 已更新

    const canvas = pdfCanvas.value;
    if (!canvas) {
      console.error('Canvas element not found');
      return;
    }

    const context = canvas.getContext('2d');
    canvas.height = viewport.height;
    canvas.width = viewport.width;

    const renderContext = {
      canvasContext: context,
      viewport: viewport,
    };

    renderTask.value = page.render(renderContext);
    await renderTask.value.promise;

    currentPage.value = pageNum;

  } catch (error) {
    if (error.name !== 'RenderingCancelledException') {
      console.error('页面渲染失败:', error);
    }
  }
}

// 翻页功能
const changePage = (delta) => {
  const newPage = currentPage.value + delta;
  if (newPage >= 1 && newPage <= totalPages.value) {
    renderPage(newPage);
  }
}

// 添加旋转功能函数
const rotatePage = (angle) => {
  // 计算新的旋转角度
  pdfRotation.value = (pdfRotation.value + angle) % 360;
  // 处理负值的情况
  if (pdfRotation.value < 0) {
    pdfRotation.value += 360;
  }
  // 重新渲染当前页面
  renderPage(currentPage.value);
}

// 缩放功能
const zoomIn = () => {
  pdfScale.value = Math.min(pdfScale.value + 0.25, 3);
  renderPage(currentPage.value);
}

const zoomOut = () => {
  pdfScale.value = Math.max(pdfScale.value - 0.25, 0.5);
  renderPage(currentPage.value);
}

function getUser() {
  getUserProfile().then(response => {
    user.value = response.data;
  });
};

onMounted(() => {
  reset();

  listArchive().then(response => {
    archiveList.value = response.rows;
  });

  getUser();

  const _id = route.query.id;
  getDocument(_id).then(response => {
    form.value = response.data;
    selectedCategoryCodes.value = form.value.categoryCode.split(",");
    checkFileType()
    getMediaUrl()
    oldData.value = JSON.parse(JSON.stringify(response.data));
    getArchiveById()
  });
})

onUnmounted(() => {
  if (mediaUrl.value) {
    URL.revokeObjectURL(mediaUrl.value)
  }

  // 清理PDF.js资源
  if (pdfDoc.value) {
    pdfDoc.value.destroy();
  }

  // 取消渲染任务
  if (renderTask.value) {
    renderTask.value.cancel();
  }
})
</script>

<style scoped>
/* 预览区域样式 */
.media-container {
  width: 100%;
  height: 600px;
  display: flex;
  justify-content: center;
  align-items: center;
  background: #f5f7fa;
}

.pdf-container {
  width: 100%;
  height: 100%;
  position: relative;
}

.pdfjs-container {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  position: relative;
}

.pdf-toolbar {
  background: #fff;
  padding: 10px;
  border-bottom: 1px solid #e0e0e0;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  z-index: 10;
}

.pdf-viewer-container {
  flex: 1;
  overflow: auto;
  display: flex;
  justify-content: center;
  align-items: flex-start;
  padding: 20px;
  position: relative;
}

.pdf-canvas {
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  background: white;
}

/* 图片预览样式 */
.image-preview {
  max-width: 100%;
  max-height: 100%;
  width: auto;
  height: auto;
  display: block;
  margin: 0 auto;
  object-fit: contain;
  background-color: #fff;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
}

/* 视频播放器样式 */
.video-player {
  width: 100%;
  height: 100%;
  max-width: 1200px;
  background: #000;
}

/* 音频播放器样式 */
.audio-player {
  width: 80%;
  margin: 0 auto;
}

/* 不支持的文件类型提示 */
.unsupported-file {
  padding: 20px;
  text-align: center;
}
</style>