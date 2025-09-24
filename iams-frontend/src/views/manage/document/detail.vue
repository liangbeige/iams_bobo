<template>
  <div class="app-container">
    <el-row :gutter="10">
      <el-col :span="5">
        <el-card class="box-card">
          <div slot="header" class="clearfix">
            <h3>文档基本信息</h3>
            <el-divider></el-divider>
          </div>
          <el-form label-position="top" label-width="100px">
            <el-form-item label="文件名称">
              <span>{{ document.name }}</span>
            </el-form-item>
            <el-form-item label="文档编号">
              <span>{{ document.id }}</span>
            </el-form-item>
            <el-form-item label="文件类型">
              <span>{{ document.fileType }}</span>
            </el-form-item>
            <el-form-item label="文件大小">
              <span>{{ document.fileSize }} Kb</span>
            </el-form-item>
            <el-form-item label="创建人">
              <span>{{ document.createBy }}</span>
            </el-form-item>
            <el-form-item label="创建时间">
              <span>{{ document.createTime }}</span>
            </el-form-item>
            <el-form-item label="更新人">
              <span>{{ document.updateBy }}</span>
            </el-form-item>
            <el-form-item label="更新时间">
              <span>{{ document.updateTime }}</span>
            </el-form-item>
            <el-form-item label="备注">
              <span>{{ document.remark }}</span>
            </el-form-item>

          </el-form>
        </el-card>
      </el-col>
      <el-col :span="19">
        <el-card class="box-card">
          <el-watermark :content="watermarkContent" :font="config.font" :rotate="config.rotate" :gap="config.gap"
                        :offset="config.offset">
            <div class="media-container">
              <!-- 修改PDF显示部分 -->
              <div v-if="isPDF" class="pdf-container" @contextmenu.prevent>
                <!-- PDF.js渲染区域 -->
                <div v-loading="pdfLoading" class="pdfjs-container">
                  <!-- PDF导航栏 -->
                  <div class="pdf-toolbar">
                    <el-button-group>
                      <el-button
                          size="small"
                          @click="scrollToPage(currentPage - 1)"
                          :disabled="currentPage <= 1"
                          icon="el-icon-arrow-left">
                        上一页
                      </el-button>

                      <!-- 页码输入框 -->
                      <el-input
                          v-model.number="pageInput"
                          size="small"
                          @keyup.enter="jumpToPage"
                          style="width: 60px; margin: 0 5px;"
                          :min="1"
                          :max="totalPages"
                          @blur="jumpToPage">
                      </el-input>

                      <el-button size="small" disabled>
                        {{ currentPage }} / {{ totalPages }}
                      </el-button>

                      <el-button
                          size="small"
                          @click="scrollToPage(currentPage + 1)"
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
                          @click="rotatePDF(-90)"
                          icon="el-icon-refresh-left"
                          title="逆时针旋转90°">
                        左转
                      </el-button>
                      <el-button size="small" disabled>
                        {{ pdfRotation }}°
                      </el-button>
                      <el-button
                          size="small"
                          @click="rotatePDF(90)"
                          icon="el-icon-refresh-right"
                          title="顺时针旋转90°">
                        右转
                      </el-button>
                    </el-button-group>

                  </div>

                  <!-- PDF页面渲染区 - 改为滚动容器 -->
                  <div ref="pdfViewerContainer" class="pdf-viewer-container" @scroll="handleScroll">
                    <div class="pdf-pages-wrapper">
                      <!-- 渲染所有页面 -->
                      <div
                          v-for="pageNum in totalPages"
                          :key="pageNum"
                          :ref="`page-${pageNum}`"
                          class="pdf-page-container"
                          :data-page-num="pageNum">
                        <div class="page-number">第 {{ pageNum }} 页</div>
                        <canvas :ref="`canvas-${pageNum}`" class="pdf-canvas"></canvas>
                      </div>
                    </div>
                  </div>

                  <!-- 防复制覆盖层 -->
                  <div class="pdf-protection-overlay"></div>
                </div>
              </div>

              <img v-else-if="isImage" :src="mediaUrl" alt="预览图片" class="image-preview" />

              <video-player v-else-if="isVideo" :src="mediaUrl" :poster="videoPoster" controls class="video-player" />

              <audio-player v-else-if="isAudio" :src="mediaUrl" controls class="audio-player" />

              <div v-else class="unsupported-file">
                <el-alert title="不支持的文件类型" type="warning" :closable="false" show-icon>
                  <p>系统暂不支持预览该类型文件，请下载后查看</p>
                </el-alert>
              </div>
            </div>
          </el-watermark>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog v-model="sealConfigDialog" title="印章配置" width="600px">
      <el-form label-position="top">
        <el-form-item label="印章位置">
          <el-radio-group v-model="sealConfig.position">
            <el-radio-button v-for="item in positionOptions" :key="item.value" :label="item.value">
              {{ item.label }}
            </el-radio-button>
          </el-radio-group>
        </el-form-item>

        <el-row :gutter="20" v-if="sealConfig.position === 'custom'">
          <el-col :span="12">
            <el-form-item label="X坐标">
              <el-input-number v-model="sealConfig.customX" :min="0" :step="10" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="Y坐标">
              <el-input-number v-model="sealConfig.customY" :min="0" :step="10" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="印章大小">
          <el-slider v-model="sealConfig.size" :min="50" :max="200" :step="10" show-input />
        </el-form-item>

        <el-form-item label="透明度">
          <el-slider v-model="sealConfig.opacity" :min="0.1" :max="1" :step="0.1" show-input />
        </el-form-item>

        <el-form-item label="旋转角度">
          <el-slider v-model="sealConfig.rotation" :min="0" :max="360" :step="15" show-input />
        </el-form-item>

        <el-form-item label="添加页面">
          <el-radio-group v-model="sealConfig.pages">
            <el-radio-button v-for="item in pageOptions" :key="item.value" :label="item.value">
              {{ item.label }}
            </el-radio-button>
          </el-radio-group>
        </el-form-item>

        <el-form-item v-if="sealConfig.pages === 'custom'" label="自定义页码">
          <el-input v-model="sealConfig.customPages" placeholder="例如: 1,3,5 或 1-5" />
          <div class="tip">请输入页码，用逗号分隔或使用范围(如1-3)</div>
        </el-form-item>
      </el-form>


      <template #footer>
        <el-button @click="sealConfigDialog = false">取消</el-button>
        <el-button @click="previewWithConfig">预览效果</el-button>
        <el-button type="primary" @click="applySealConfig">应用配置</el-button>
      </template>
    </el-dialog>

    <!--      盖章预览对话框-->
    <el-dialog v-model="previewVisible" title="盖章预览" width="90%" top="5vh">
      <div v-loading="previewLoading" class="preview-container">
        <iframe v-if="previewPdfUrl" :src="previewPdfUrl" width="100%" height="600px" frameborder="0"></iframe>
        <el-empty v-else description="暂无预览内容" />
      </div>

      <template #footer>
        <el-button @click="previewVisible = false">取消</el-button>
        <el-button type="primary" @click="downloadStampedPdf" :loading="previewLoading">
          确认下载
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, watch, reactive,getCurrentInstance  } from 'vue'
import { getDocument } from '@/api/manage/document'
import { getToken } from "@/utils/auth";
import axios from 'axios'
import { useRoute } from 'vue-router'
// 添加浏览日志相关导入
import { addBrowseLog, updateBrowseLog } from '@/api/manage/browseLog'
import { getDefaultWatermark } from '@/api/manage/watermark'
import { getUserProfile } from "@/api/system/user";
import VideoPlayer from '@/components/Player/VideoPlayer'
import AudioPlayer from '@/components/Player/AudioPlayer'
import { PDFDocument, rgb, degrees } from 'pdf-lib';
import { ElMessage } from 'element-plus'
import { onUnmounted } from 'vue'
import { generateOfficialByDocument } from '@/api/manage/seal'

// 导入 pdfjs-dist
import * as pdfjsLib from 'pdfjs-dist/legacy/build/pdf';
const workerUrl = new URL(
    'pdfjs-dist/legacy/build/pdf.worker.min.js',
    import.meta.url
).toString();

// 设置 worker
pdfjsLib.GlobalWorkerOptions.workerSrc = workerUrl;
const route = useRoute()
// 浏览日志相关变量
const browseLogId = ref(null)
const browseStartTime = ref(null)
const activityTimer = ref(null)
const lastActivityTime = ref(Date.now())
const { proxy } = getCurrentInstance()
const headers = ref({ Authorization: "Bearer " + getToken() });
const data = reactive({
  config: {
    content: '智慧档案管理系统',
    font: {
      fontSize: 16,
      color: 'rgba(0, 0, 0, 0.15)',
      fontFamily: 'sans-serif',
      fontStyle: 'normal',
      fontWeight: 'normal',
    },
    rotate: -22,
    gap: [100, 100],
    offset: [50, 50],
  },
})
const { config } = toRefs(data)

// 获取当前时间戳：年-月-日 时:分:秒 的格式
const nowDate = new Date().toLocaleString()
// 水印内容
const watermarkContent = ref("")

const documentId = ref(0)
const document = ref({})
const pdfUrl = ref('')
const extraInfoDialog = ref(false)
const extraInfo = ref([])
const user = ref({})
const onlineList = ref([]);

// 在原有代码基础上新增以下内容
const mediaUrl = ref('')
const isPDF = ref(false)
const isVideo = ref(false)
const isAudio = ref(false)
const isImage = ref(false)
const videoPoster = ref('') // 视频封面图，可选

// PDF.js 相关状态
const pdfDoc = shallowRef(null)
const currentPage = ref(1)
const totalPages = ref(0)
const pdfScale = ref(1.5)
const pdfLoading = ref(false)
const pdfRotation = ref(0)
const pdfViewerContainer = ref(null)

// 新增：存储所有页面的渲染任务
const renderTasks = ref(new Map())
// 新增：页面可见性追踪
const visiblePages = ref(new Set())

// 新增：页码输入框的值
const pageInput = ref(1)

// 监听currentPage变化，同步更新输入框
watch(currentPage, (newVal) => {
  pageInput.value = newVal
})

// 文件类型判断逻辑
const checkFileType = () => {
  const fileType = document.value.fileType?.toLowerCase()
  isPDF.value = fileType === 'pdf'
  isVideo.value = ['mp4', 'avi', 'mov', 'wmv'].includes(fileType)
  isAudio.value = ['mp3', 'wav', 'ogg'].includes(fileType)
  isImage.value = ['png', 'jpg', 'jpeg', 'gif', 'bmp', 'webp'].includes(fileType)
}

// 获取媒体URL
const getMediaUrl = async () => {
  if (document.value.filePath.startsWith("http://127.0.0.1:8080")) {
    try {
      const response = await axios({
        url: document.value.filePath,
        method: 'GET',
        responseType: 'blob'
      })

      const blob = new Blob([response.data], {
        type: `application/${document.value.fileType}`
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
    mediaUrl.value = document.value.filePath

    // 如果是PDF，初始化PDF.js
    if (isPDF.value) {
      await initPdfJs();
    }
  }
}

// 初始化PDF.js - 修改为渲染所有页面
const initPdfJs = async () => {
  try {
    pdfLoading.value = true;

    // 确保 DOM 已经渲染
    await nextTick();

    // 加载PDF文档
    const loadingTask = pdfjsLib.getDocument({
      url: mediaUrl.value,
      disableTextLayer: true,
      disableAnnotationLayer: true,
      withCredentials: false,
      cMapPacked: true,
    });

    pdfDoc.value = await loadingTask.promise;
    totalPages.value = pdfDoc.value.numPages;

    // 等待 DOM 更新后再渲染
    await nextTick();

    // 渲染所有页面
    await renderAllPages();

    // 设置滚动监听
    setupScrollListener();

    // 禁用键盘快捷键
    disableKeyboardShortcuts();

  } catch (error) {
    console.error('PDF加载失败:', error);
    ElMessage.error('PDF文件加载失败');
  } finally {
    pdfLoading.value = false;
  }
}

// 新增：渲染所有页面
const renderAllPages = async () => {
  // 初始渲染前几页
  const initialPages = Math.min(3, totalPages.value); // 先渲染前3页

  for (let i = 1; i <= initialPages; i++) {
    await renderPage(i);
  }

  // 延迟渲染剩余页面
  if (totalPages.value > initialPages) {
    setTimeout(async () => {
      for (let i = initialPages + 1; i <= totalPages.value; i++) {
        // 检查是否已经在视口中
        if (isPageVisible(i)) {
          await renderPage(i);
        }
      }
    }, 500);
  }
}

// 修改渲染单个页面的方法
const renderPage = async (pageNum) => {
  if (!pdfDoc.value) return;

  try {
    // 如果该页已有渲染任务，先取消
    if (renderTasks.value.has(pageNum)) {
      renderTasks.value.get(pageNum).cancel();
    }

    const page = await pdfDoc.value.getPage(pageNum);
    const viewport = page.getViewport({
      scale: pdfScale.value,
      rotation: pdfRotation.value
    });

    await nextTick();

    // 获取对应页面的canvas
    const canvasRefs = proxy.$refs[`canvas-${pageNum}`];
    const canvas = Array.isArray(canvasRefs) ? canvasRefs[0] : canvasRefs;

    if (!canvas) {
      console.error(`Canvas for page ${pageNum} not found`);
      return;
    }

    const context = canvas.getContext('2d');
    canvas.height = viewport.height;
    canvas.width = viewport.width;

    const renderContext = {
      canvasContext: context,
      viewport: viewport,
      renderInteractiveForms: false,
      includeAnnotationStorage: false
    };

    const renderTask = page.render(renderContext);
    renderTasks.value.set(pageNum, renderTask);

    await renderTask.promise;
    renderTasks.value.delete(pageNum);

  } catch (error) {
    if (error.name !== 'RenderingCancelledException') {
      console.error(`页面 ${pageNum} 渲染失败:`, error);
    }
  }
}

// 新增：处理滚动事件
const handleScroll = () => {
  if (!pdfViewerContainer.value) return;

  const container = pdfViewerContainer.value;
  const scrollTop = container.scrollTop;
  const containerHeight = container.clientHeight;

  // 更新当前页码
  updateCurrentPage(scrollTop);

  // 懒加载：检查哪些页面需要渲染
  checkVisiblePages(scrollTop, containerHeight);
}

// 新增：更新当前页码
const updateCurrentPage = (scrollTop) => {
  const pages = pdfViewerContainer.value.querySelectorAll('.pdf-page-container');

  for (let i = 0; i < pages.length; i++) {
    const page = pages[i];
    const pageTop = page.offsetTop;
    const pageBottom = pageTop + page.offsetHeight;

    // 如果页面中心在视口中，就认为是当前页
    if (pageTop <= scrollTop + 100 && pageBottom > scrollTop + 100) {
      currentPage.value = i + 1;
      break;
    }
  }
}

// 新增：检查可见页面并渲染
const checkVisiblePages = (scrollTop, containerHeight) => {
  const viewportTop = scrollTop - 200; // 预加载上方200px
  const viewportBottom = scrollTop + containerHeight + 200; // 预加载下方200px

  const pages = pdfViewerContainer.value.querySelectorAll('.pdf-page-container');

  pages.forEach((page) => {
    const pageNum = parseInt(page.dataset.pageNum);
    const pageTop = page.offsetTop;
    const pageBottom = pageTop + page.offsetHeight;

    // 检查页面是否在视口范围内
    if (pageBottom >= viewportTop && pageTop <= viewportBottom) {
      if (!visiblePages.value.has(pageNum)) {
        visiblePages.value.add(pageNum);
        // 延迟渲染，避免同时渲染太多页面
        setTimeout(() => renderPage(pageNum), 100 * (pageNum % 3));
      }
    }
  });
}

// 新增：检查页面是否在视口中
const isPageVisible = (pageNum) => {
  const pageRef = proxy.$refs[`page-${pageNum}`];
  const pageElement = Array.isArray(pageRef) ? pageRef[0] : pageRef;

  if (!pageElement || !pdfViewerContainer.value) return false;

  const containerRect = pdfViewerContainer.value.getBoundingClientRect();
  const pageRect = pageElement.getBoundingClientRect();

  return pageRect.bottom >= containerRect.top && pageRect.top <= containerRect.bottom;
}

// 新增：设置滚动监听
const setupScrollListener = () => {
  let scrollTimeout;

  const throttledScroll = () => {
    clearTimeout(scrollTimeout);
    scrollTimeout = setTimeout(() => {
      handleScroll();
    }, 100);
  };

  if (pdfViewerContainer.value) {
    pdfViewerContainer.value.addEventListener('scroll', throttledScroll);

    // 组件销毁时移除监听
    onUnmounted(() => {
      if (pdfViewerContainer.value) {
        pdfViewerContainer.value.removeEventListener('scroll', throttledScroll);
      }
    });
  }
}

// 修改：滚动到指定页面
const scrollToPage = (pageNum) => {
  if (pageNum < 1 || pageNum > totalPages.value) return;

  const pageRef = proxy.$refs[`page-${pageNum}`];
  const pageElement = Array.isArray(pageRef) ? pageRef[0] : pageRef;

  if (pageElement && pdfViewerContainer.value) {
    const containerTop = pdfViewerContainer.value.offsetTop;
    const pageTop = pageElement.offsetTop - containerTop;

    pdfViewerContainer.value.scrollTo({
      top: pageTop,
      behavior: 'smooth'
    });

    currentPage.value = pageNum;
    pageInput.value = pageNum;
  }
}

// 修改：跳转到指定页码
const jumpToPage = () => {
  const targetPage = parseInt(pageInput.value);

  if (isNaN(targetPage)) {
    ElMessage.warning('请输入有效的页码');
    pageInput.value = currentPage.value;
    return;
  }

  if (targetPage < 1) {
    scrollToPage(1);
  } else if (targetPage > totalPages.value) {
    scrollToPage(totalPages.value);
  } else {
    scrollToPage(targetPage);
  }
}

// 修改：旋转PDF
const rotatePDF = async (angle) => {
  pdfRotation.value = (pdfRotation.value + angle) % 360;
  if (pdfRotation.value < 0) {
    pdfRotation.value += 360;
  }

  // 重新渲染所有可见页面
  pdfLoading.value = true;

  try {
    // 清除所有渲染任务
    renderTasks.value.forEach(task => task.cancel());
    renderTasks.value.clear();

    // 重新渲染可见页面
    for (const pageNum of visiblePages.value) {
      await renderPage(pageNum);
    }
  } finally {
    pdfLoading.value = false;
  }
}

// 修改：缩放功能
const zoomIn = async () => {
  pdfScale.value = Math.min(pdfScale.value + 0.25, 3);
  await reRenderAllVisiblePages();
}

const zoomOut = async () => {
  pdfScale.value = Math.max(pdfScale.value - 0.25, 0.5);
  await reRenderAllVisiblePages();
}

// 新增：重新渲染所有可见页面
const reRenderAllVisiblePages = async () => {
  pdfLoading.value = true;

  try {
    // 清除所有渲染任务
    renderTasks.value.forEach(task => task.cancel());
    renderTasks.value.clear();

    // 重新渲染可见页面
    for (const pageNum of visiblePages.value) {
      await renderPage(pageNum);
    }
  } finally {
    pdfLoading.value = false;
  }
}

// 禁用键盘快捷键
const disableKeyboardShortcuts = () => {
  const handleKeydown = (e) => {
    // 禁用打印快捷键 (Ctrl/Cmd + P)
    if ((e.ctrlKey || e.metaKey) && e.key === 'p') {
      e.preventDefault();
      ElMessage.warning('打印功能已被禁用');
      return false;
    }

    // 禁用保存快捷键 (Ctrl/Cmd + S)
    if ((e.ctrlKey || e.metaKey) && e.key === 's') {
      e.preventDefault();
      ElMessage.warning('保存功能已被禁用');
      return false;
    }

    // 禁用全选快捷键 (Ctrl/Cmd + A)
    if ((e.ctrlKey || e.metaKey) && e.key === 'a') {
      const target = e.target;
      if (target.closest('.pdf-container')) {
        e.preventDefault();
        return false;
      }
    }
  };

  window.document.addEventListener('keydown', handleKeydown);

  // 组件销毁时移除监听器
  onUnmounted(() => {
    window.document.removeEventListener('keydown', handleKeydown);
  });
}

// 添加CSS禁用打印
const addPrintProtection = () => {
  const style = window.document.createElement('style');
  style.textContent = `
        @media print {
            .pdf-container, .pdf-canvas {
                display: none !important;
                visibility: hidden !important;
            }
            body::before {
                content: "此文档不允许打印";
                display: block;
                text-align: center;
                font-size: 24px;
                padding: 50px;
            }
        }
    `;
  window.document.head.appendChild(style);
}

const archiveNumber = ref('') // 档号

// 印章预览状态相关
const previewVisible = ref(false)
const previewPdfUrl = ref('')
const previewLoading = ref(false)
const sealPreviewUrl = ref('') // 用于存储印章预览URL
const sealPreviewVisible = ref(false)

// 印章配置相关
const sealConfigDialog = ref(false)
const sealConfig = reactive({
  position: 'bottom-right', // 默认位置
  customX: 0,              // 自定义X坐标
  customY: 0,              // 自定义Y坐标
  size: 100,               // 印章大小
  opacity: 0.7,            // 透明度
  pages: 'all',            // 所有页面
  customPages: '',         // 自定义页码
  rotation: 0              // 旋转角度
})

// 预设位置选项
const positionOptions = [
  { value: 'top-left', label: '左上角' },
  { value: 'top-right', label: '右上角' },
  { value: 'bottom-left', label: '左下角' },
  { value: 'bottom-right', label: '右下角' },
  { value: 'center', label: '居中' },
  { value: 'custom', label: '自定义位置' }
]

// 页面选择选项
const pageOptions = [
  { value: 'all', label: '所有页面' },
  { value: 'first', label: '仅第一页' },
  { value: 'last', label: '仅最后一页' },
  { value: 'custom', label: '自定义页码' }
]

function getUser() {
  getUserProfile().then(response => {
    user.value = response.data;
    watermarkContent.value = config.value.content + '-' + user.value.userName + '-' + user.value.loginIp + '-' + nowDate;
  });
};

//添加应用配置
const applySealConfig = () => {
  sealConfigDialog.value = false
}

//  预览印章方法
const generateStampedPdf = async () => {
  try {
    console.log('Current mediaUrl:', mediaUrl.value);

    // 并行请求
    const [pdfResponse, sealResponse] = await Promise.all([
      fetch(mediaUrl.value),
      generateOfficialByDocument(documentId.value) // 使用封装好的API方法
    ]);

    if (!pdfResponse.ok) throw new Error('PDF获取失败');

    // 获取二进制数据
    const pdfBytes = await pdfResponse.arrayBuffer();
    const sealBytes = await sealResponse.arrayBuffer();

    //  处理PDF
    const pdfDoc = await PDFDocument.load(pdfBytes);
    const sealImage = await pdfDoc.embedPng(sealBytes);
    const pages = pdfDoc.getPages()

    // 确定要添加印章的页面
    let targetPages = []
    if (sealConfig.pages === 'all') {
      targetPages = pages
    } else if (sealConfig.pages === 'first') {
      targetPages = [pages[0]]
    } else if (sealConfig.pages === 'last') {
      targetPages = [pages[pages.length - 1]]
    } else if (sealConfig.pages === 'custom') {
      const pageNumbers = sealConfig.customPages.split(',')
          .map(num => parseInt(num.trim()) - 1) // 转换为0-based索引
          .filter(num => num >= 0 && num < pages.length)
      targetPages = pageNumbers.map(num => pages[num])
    }

    // 为每个目标页面添加印章
    targetPages.forEach(page => {
      const { width, height } = page.getSize()

      // 计算印章位置
      let x, y
      switch (sealConfig.position) {
        case 'top-left':
          x = 50
          y = height - 50 - sealConfig.size
          break
        case 'top-right':
          x = width - 50 - sealConfig.size
          y = height - 50 - sealConfig.size
          break
        case 'bottom-left':
          x = 50
          y = 50
          break
        case 'bottom-right':
          x = width - 50 - sealConfig.size
          y = 50
          break
        case 'center':
          x = (width - sealConfig.size) / 2
          y = (height - sealConfig.size) / 2
          break
        case 'custom':
          x = sealConfig.customX
          y = sealConfig.customY
          break
      }

      page.drawImage(sealImage, {
        x,
        y,
        width: sealConfig.size,
        height: sealConfig.size,
        opacity: sealConfig.opacity,
        rotate: degrees(sealConfig.rotation)
      })
    })

    // 3. 返回生成的Blob
    return new Blob([await pdfDoc.save()], { type: 'application/pdf' });
  } catch (error) {
    console.error('❗ PDF生成全链路错误:', {
      error: error.message,
      stack: error.stack,
      timestamp: new Date().toISOString()
    });
    throw error;

  } finally {
    previewLoading.value = false
  }
}

const previewWithConfig = async () => {
  try {
    console.log('🔄 开始生成预览...');
    sealConfigDialog.value = false;
    previewLoading.value = true;

    const blob = await generateStampedPdf();
    console.log('📄 生成Blob:', blob.size + ' bytes');

    previewPdfUrl.value = URL.createObjectURL(blob);
    previewVisible.value = true;
    console.log('🖼️ 预览URL创建成功');

  } catch (error) {
    console.error('预览失败:', error);
    ElMessage.error(`生成预览失败: ${error.message}`);
  } finally {
    previewLoading.value = false;
  }
}

// 下载盖章后的PDF
const downloadStampedPdf = async () => {
  try {
    previewLoading.value = true;
    const blob = await generateStampedPdf();

    // 增强环境检测：确保存在完整的 DOM API
    if (typeof window !== 'undefined' && window.document && window.document.createElement) {
      const doc = window.document; // 使用安全引用
      const url = URL.createObjectURL(blob);
      const link = doc.createElement('a');

      const filename = `${document.value.name.replace(/\.[^/.]+$/, '')}（已盖章）.pdf`;
      link.href = url;
      link.download = filename;

      doc.body.appendChild(link);
      link.click();

      setTimeout(() => {
        doc.body.removeChild(link);
        URL.revokeObjectURL(url);
      }, 100);

      ElMessage.success('下载已开始');
    } else {
      // 非浏览器环境处理方案
      console.warn('当前环境不支持自动下载，请手动保存文件。');
      const reader = new FileReader();
      reader.readAsDataURL(blob);
      reader.onload = () => {
        const base64Data = reader.result;
        console.log('文件Base64数据:', base64Data);
      };
    }
  } catch (error) {
    console.error('下载失败:', error);
    ElMessage.error(`下载失败: ${error.message}`);
  } finally {
    previewLoading.value = false;
    previewVisible.value = false;
  }
}

function showExtraInfo() {
  extraInfoDialog.value = true
  extraInfo.value = JSON.parse(document.value.extraInfo)
}

// 开始记录浏览日志 - 添加详细日志
async function startBrowseLog() {
  try {
    console.log('[浏览日志] 开始记录浏览日志...')

    // 确保文档数据已加载
    if (!document.value || !document.value.id) {
      console.warn('[浏览日志] 文档数据未准备好，无法记录浏览日志')
      return
    }

    console.log('[浏览日志] 文档数据已加载:', document.value)

    browseStartTime.value = new Date()
    console.log('[浏览日志] 浏览开始时间:', browseStartTime.value.toISOString())

    // 获取用户信息 - 使用安全的方式
    let userId = 0
    let userName = '未知用户'

    try {
      console.log('[浏览日志] 获取用户信息...')
      const userInfo = await getUserProfile()
      console.log('[浏览日志] 用户信息API响应:', userInfo)

      if (userInfo && userInfo.data) {
        userId = userInfo.data.userId || 0
        userName = userInfo.data.userName || '未知用户'
        console.log(`[浏览日志] 用户信息获取成功: ID=${userId}, 姓名=${userName}`)
      } else {
        console.warn('[浏览日志] 用户信息API返回的数据格式不正确')
      }
    } catch (userError) {
      console.error('[浏览日志] 获取用户信息失败:', userError)
    }

    const logData = {
      archive_danghao: document.value.archiveNumber || '', // 档案档号
      archive_id: document.value.archiveId || 0,         // 档案ID
      archive_name: document.value.archiveName || '',    // 档案名称
      viewer_id: userId,                                // 查看人ID
      viewer_name: userName,                            // 查看人名称
      start_time: new Date().toISOString(),// 开始时间
      created_at: new Date().toISOString(), // 使用 ISO 字符串格式
      document_name: document.value.name || '',       // 文档名称
      document_id: document.value.id || 0               // 新增：文档ID
    }

    console.log('[浏览日志] 提交浏览日志数据:', JSON.parse(JSON.stringify(logData)));

    const response = await addBrowseLog(logData)
    console.log('[浏览日志] API响应:', response)

    // 修改响应检查逻辑
    if (response && response.code === 200) {
      // 检查不同可能的ID字段位置
      const logId = response.data?.id || response.id || response.logId

      if (logId) {
        // 确保ID存储为字符串
        browseLogId.value = String(logId);
        console.log(`[浏览日志] 浏览日志开始记录，ID: ${browseLogId.value}`)
      } else {
        console.warn('[浏览日志] API返回成功但缺少ID字段，无法更新结束时间')
        console.warn('响应详情:', response)

        // 使用临时ID作为备选方案
        browseLogId.value = 'temp-' + Date.now() + '-' + Math.random().toString(36).substr(2, 9);
        console.log('[浏览日志] 使用临时ID:', browseLogId.value);
      }
    } else {
      console.error('[浏览日志] API返回状态码非200')
      console.error('响应详情:', response)
    }
  } catch (error) {
    console.error('[浏览日志] 开始记录浏览日志失败:', error)
    console.error('错误详情:', {
      message: error.message,
      stack: error.stack,
      timestamp: new Date().toISOString()
    })
  }
}

// 结束记录浏览日志 - 添加详细日志
async function endBrowseLog() {
  // 确保 browseLogId.value 是字符串类型
  const logId = String(browseLogId.value || '');

  // 检查是否是临时ID或无有效ID
  if (!logId || logId.startsWith('temp-')) {
    console.warn('[浏览日志] 跳过结束记录（无有效ID或临时ID）');
    return;
  }

  try {
    const endTime = new Date();
    const duration = Math.round((endTime - browseStartTime.value) / 1000); // 确保是数字

    console.log(`[浏览日志] 结束浏览日志记录，ID: ${logId}`);
    console.log(`[浏览日志] 浏览时长: ${duration}秒`);
    console.log(`[浏览日志] 开始时间: ${browseStartTime.value.toISOString()}`);
    console.log(`[浏览日志] 结束时间: ${endTime.toISOString()}`);

    const updateData = {
      end_time: endTime,
      duration: duration
    };

    console.log('[浏览日志] 更新数据:', updateData);

    const response = await updateBrowseLog(logId, updateData);
    console.log('[浏览日志] 更新API响应:', response);

    if (response && response.code === 200) {
      console.log('[浏览日志] 浏览日志更新成功');
    } else {
      console.warn('[浏览日志] 浏览日志更新失败');
    }
  } catch (error) {
    console.error('[浏览日志] 结束记录浏览日志失败:', error);
    console.error('错误详情:', {
      message: error.message,
      stack: error.stack,
      timestamp: new Date().toISOString()
    });
  } finally {
    browseLogId.value = null;
  }
}

onMounted(() => {
  documentId.value = proxy.$route.query.id;
  console.log('[初始化] 组件挂载，文档ID:', documentId.value)
  getDocument(documentId.value).then(response => {
    console.log('[初始化] 文档信息API响应:', response)
    if (response && response.data){
      document.value = response.data;
     console.log('[初始化] 文档数据加载成功:', document.value)
      checkFileType()
      getMediaUrl()
    // 只在文档加载完成后开始记录浏览日志
      console.log('[初始化] 开始记录浏览日志...')
      startBrowseLog()
    } else {
      console.error('[初始化] 文档信息API返回的数据格式不正确')
   }
  }).catch(error => {
   console.error('[初始化] 获取文档信息失败:', error)
  })


  getDefaultWatermark().then(response => {
    if (response.code === 200) {
      config.value.content = response.data.content;
      config.value.font.fontSize = response.data.fontsize;
      config.value.font.color = response.data.color;
      config.value.font.fontFamily = response.data.fontfamily;
      config.value.font.fontStyle = response.data.fontstyle;
      config.value.font.fontWeight = response.data.fontweight;
      config.value.rotate = response.data.rotate;
      config.value.gap[0] = response.data.gapX;
      config.value.gap[1] = response.data.gapY;
      config.value.offset[0] = response.data.offsetX;
      config.value.offset[1] = response.data.offsetY;
      getUser();
    }
  })

  // 添加打印保护CSS
  addPrintProtection();

  // 将下载方法挂载到仅客户端可用的上下文
  window.downloadStampedPdf = downloadStampedPdf;
})

onUnmounted(() => {
  console.log('[清理] 组件卸载')
  if (previewPdfUrl.value) {
    URL.revokeObjectURL(previewPdfUrl.value)
  }

  // 清理PDF.js资源
  if (pdfDoc.value) {
    pdfDoc.value.destroy();
  }

  // 取消所有渲染任务
  renderTasks.value.forEach(task => task.cancel());
  renderTasks.value.clear();

  // 清理媒体URL
  if (mediaUrl.value && mediaUrl.value.startsWith('blob:')) {
    URL.revokeObjectURL(mediaUrl.value);
  }
  // 结束记录浏览日志
  console.log('[清理] 结束浏览日志记录')
  endBrowseLog()
})
</script>

<style scoped>
/* 新增样式 */
.media-container {
  width: 100%;
  height: 900px;
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
  height: 900px;
  position: relative;
  background: #f5f5f5;
  /* 禁用选择 */
  -webkit-user-select: none;
  -moz-user-select: none;
  -ms-user-select: none;
  user-select: none;
  /* 禁用拖拽 */
  -webkit-user-drag: none;
}

/* PDF.js 容器样式 */
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
  position: sticky;
  top: 0;
}

/* 修改：PDF查看器容器改为滚动容器 */
.pdf-viewer-container {
  flex: 1;
  overflow-y: auto;
  overflow-x: auto;
  position: relative;
  background: #e5e5e5;
}

/* 新增：PDF页面包装器 */
.pdf-pages-wrapper {
  padding: 20px 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
}

/* 新增：单个PDF页面容器 */
.pdf-page-container {
  position: relative;
  background: white;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  margin: 0 auto;
}

/* 新增：页码标签 */
.page-number {
  position: absolute;
  top: -25px;
  left: 0;
  font-size: 12px;
  color: #666;
  background: rgba(255,255,255,0.9);
  padding: 2px 8px;
  border-radius: 4px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
}

.pdf-canvas {
  display: block;
  /* 防止图像被拖拽 */
  -webkit-user-drag: none;
  user-drag: none;
  /* 确保canvas不可选择 */
  -webkit-user-select: none;
  -moz-user-select: none;
  -ms-user-select: none;
  user-select: none;
}

/* 防复制覆盖层 */
.pdf-protection-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 5;
  /* 透明但阻止交互 */
  background: transparent;
  pointer-events: none;
}

/* 允许工具栏和滚动容器交互 */
.pdf-toolbar,
.pdf-viewer-container {
  pointer-events: auto;
}

/* 自定义滚动条样式 */
.pdf-viewer-container::-webkit-scrollbar {
  width: 12px;
  height: 12px;
}

.pdf-viewer-container::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 6px;
}

.pdf-viewer-container::-webkit-scrollbar-thumb {
  background: #888;
  border-radius: 6px;
}

.pdf-viewer-container::-webkit-scrollbar-thumb:hover {
  background: #555;
}

/* 加载状态优化 */
.pdf-container[loading="true"] .pdf-viewer-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .pdf-toolbar {
    flex-wrap: wrap;
    gap: 10px;
  }

  .pdf-pages-wrapper {
    padding: 10px 0;
  }
}

/* 打印时隐藏 */
@media print {
  .pdf-container,
  .pdfjs-container,
  .pdf-canvas {
    display: none !important;
    visibility: hidden !important;
  }
}

.enhanced-extra-info-dialog {
  border-radius: 8px;
}

.enhanced-extra-info-container {
  padding: 0 10px;
}

.enhanced-extra-info-item {
  margin-bottom: 16px;
  padding-bottom: 16px;
  border-bottom: 1px solid #eee;
}

.enhanced-extra-info-item.last-item {
  border-bottom: none;
  margin-bottom: 0;
  padding-bottom: 0;
}

.info-label {
  margin-bottom: 8px;
}

.info-content {
  padding-left: 8px;
  line-height: 1.6;
  color: #333;
}

.enhanced-dialog-footer {
  text-align: center;
  padding-top: 10px;
}

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

/* 页码输入框样式 */
.pdf-toolbar .el-input {
  text-align: center;
}

.pdf-toolbar .el-input__inner {
  text-align: center;
  padding: 0 5px;
}

/* 优化页面加载时的占位高度 */
.pdf-page-container {
  min-height: 800px; /* 设置一个合理的最小高度，避免加载时布局跳动 */
}

/* 页面正在加载时的样式 */
.pdf-page-container.loading {
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f9f9f9;
}

.pdf-page-container.loading::after {
  content: "加载中...";
  color: #999;
  font-size: 14px;
}
</style>