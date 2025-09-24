<template>
    <div class="wrapper">
        <el-watermark class="watermark" :content="config.content" :font="config.font" :rotate="config.rotate"
            :gap="config.gap" :offset="config.offset">
            <div class="demo">
                <h1 style="text-align: center;">请在右侧面板设置水印参数调整显示效果</h1>
                <!-- 添加PDF加载部分 -->
                <div class="pdf-container">
                    <iframe :src="pdfUrl" width="100%" height="700px" frameborder="0"></iframe>
                </div>
            </div>
        </el-watermark>
        <el-form class="form" :model="config" label-position="top" label-width="50px">
            <el-form-item label="水印内容">
                <el-input v-model="config.content" />
            </el-form-item>
            <el-form-item label="水印颜色">
                <el-color-picker v-model="config.font.color" show-alpha />
            </el-form-item>
            <el-form-item label="字体大小">
                <el-slider v-model="config.font.fontSize" />
            </el-form-item>
            <el-form-item label="字体">
                <el-select v-model="config.font.fontFamily">
                    <el-option label="Arial" value="Arial"></el-option>
                    <el-option label="Times New Roman" value="Times New Roman"></el-option>
                    <el-option label="Courier New" value="Courier New"></el-option>
                    <el-option label="Sans-Serif" value="sans-serif"></el-option>
                    <el-option label="宋体" value="宋体"></el-option>
                    <el-option label="黑体" value="黑体"></el-option>
                    <!-- 可以根据需要添加更多字体选项 -->
                </el-select>
            </el-form-item>
            <el-form-item label="字体样式">
                <el-select v-model="config.font.fontStyle">
                    <el-option label="Normal" value="normal"></el-option>
                    <el-option label="Italic" value="italic"></el-option>
                    <el-option label="Oblique" value="oblique"></el-option>
                </el-select>
            </el-form-item>
            <el-form-item label="字重">
                <el-select v-model="config.font.fontWeight">
                    <el-option label="Normal" value="normal"></el-option>
                    <el-option label="100" value="100"></el-option>
                    <el-option label="200" value="200"></el-option>
                    <el-option label="300" value="300"></el-option>
                    <el-option label="400" value="400"></el-option>
                    <el-option label="500" value="500"></el-option>
                    <el-option label="600" value="600"></el-option>
                    <el-option label="700" value="700"></el-option>
                    <el-option label="800" value="800"></el-option>
                    <el-option label="900" value="900"></el-option>
                </el-select>
            </el-form-item>
            <el-form-item label="旋转角度">
                <el-slider v-model="config.rotate" :min="-180" :max="180" />
            </el-form-item>
            <el-form-item label="水印间距">
                <el-space>
                    <el-input-number v-model="config.gap[0]" controls-position="right" />
                    <el-input-number v-model="config.gap[1]" controls-position="right" />
                </el-space>
            </el-form-item>
            <el-form-item label="左上角偏移量">
                <el-space>
                    <el-input-number v-model="config.offset[0]" placeholder="offsetLeft" controls-position="right" />
                    <el-input-number v-model="config.offset[1]" placeholder="offsetTop" controls-position="right" />
                </el-space>
            </el-form-item>
            <!-- 添加一个保存按钮 -->
            <el-button type="primary" @click="handleEditWatermark()">保存</el-button>
        </el-form>
    </div>
</template>

<script setup lang="js">
import { ref, onMounted, reactive, toRefs } from 'vue'
import { getWatermark, updateWatermark } from '@/api/manage/watermark'
import {useRoute, useRouter} from 'vue-router'
import useTagsViewStore from '@/store/modules/tagsView.js'
const tagsViewStore = useTagsViewStore();
const route = useRoute();
const router = useRouter();

const { proxy } = getCurrentInstance();
const watermark = ref({});
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


// 定义PDF文件的URL
const pdfUrl = '/pdf/white.pdf'

function handleEditWatermark() {
    watermark.value.content = config.value.content;
    watermark.value.fontsize = config.value.font.fontSize;
    watermark.value.color = config.value.font.color;
    watermark.value.fontfamily = config.value.font.fontFamily;
    watermark.value.fontstyle = config.value.font.fontStyle;
    watermark.value.fontweight = config.value.font.fontWeight;
    watermark.value.rotate = config.value.rotate;
    watermark.value.gapX = config.value.gap[0];
    watermark.value.gapY = config.value.gap[1];
    watermark.value.offsetX = config.value.offset[0];
    watermark.value.offsetY = config.value.offset[1];
    updateWatermark(watermark.value).then(res => {
        if (res.code == 200) {
            proxy.$modal.msgSuccess("修改成功");
            proxy.$router.push({ path: "/manage/watermark/list" });
        } else {
            proxy.$modal.msgError("修改失败");
        }
    })

    // 删除当前 tab
    if (tagsViewStore) {
        tagsViewStore.delView(route);
    }
    // 跳转到列表页
    router.replace({ path: '/manage/watermark/list' });
    // reset();
}

onMounted(() => {
    const _id = route.query.id;
    getWatermark(_id).then(res => {
        watermark.value = res.data;
        config.value.content = watermark.value.content;
        config.value.font.fontSize = watermark.value.fontsize;
        config.value.font.color = watermark.value.color;
        config.value.font.fontFamily = watermark.value.fontfamily;
        config.value.font.fontStyle = watermark.value.fontstyle;
        config.value.font.fontWeight = watermark.value.fontweight;
        config.value.rotate = watermark.value.rotate;
        config.value.gap[0] = watermark.value.gapX;
        config.value.gap[1] = watermark.value.gapY;
        config.value.offset[0] = watermark.value.offsetX;
        config.value.offset[1] = watermark.value.offsetY;
    })
})
</script>

<style scoped>
.wrapper {
    display: flex;
}

.watermark {
    display: flex;
    flex: auto;
}

.demo {
    flex: auto;
}

.form {
    width: 330px;
    margin-left: 20px;
    border-left: 1px solid #eee;
    padding-left: 20px;
}

img {
    z-index: 10;
    width: 100%;
    max-width: 300px;
    position: relative;
}

.pdf-container {
    margin-top: 20px;
    width: 100%;
}
</style>