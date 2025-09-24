<template>
    <div class="app-container home">
        <el-row :gutter="20">
            <el-col :span="18">
                <div class="repository-container">
                    <div class="repository-header">库房一</div>
                    <el-row :gutter="20">
                        <el-col :span="13">
                            <home-user-task-stats :taskData="userTaskData1"/>
                        </el-col>
                        <el-col :span="11">
                            <home-sku-sale-stats :skuData="skuSaleData1"/>
                        </el-col>
                    </el-row>
                </div>
                <div class="repository-container">
                    <div class="repository-header">库房二</div>
                    <el-row :gutter="20">
                        <el-col :span="13">
                            <home-user-task-stats :taskData="userTaskData2"/>
                        </el-col>
                        <el-col :span="11">
                            <home-sku-sale-stats :skuData="skuSaleData2"/>
                        </el-col>
                    </el-row>
                </div>
                <div class="repository-container">
                    <div class="repository-header">库房三</div>
                    <el-row :gutter="20">
                        <el-col :span="13">
                            <home-user-task-stats :taskData="userTaskData3"/>
                        </el-col>
                        <el-col :span="11">
                            <home-sku-sale-stats :skuData="skuSaleData3"/>
                        </el-col>
                    </el-row>
                </div>
                <div class="repository-container">
                    <div class="repository-header">库房四</div>
                    <el-row :gutter="20">
                        <el-col :span="13">
                            <home-user-task-stats :taskData="userTaskData4"/>
                        </el-col>
                        <el-col :span="11">
                            <home-sku-sale-stats :skuData="skuSaleData4"/>
                        </el-col>
                    </el-row>
                </div>
                <div class="repository-container">
                    <div class="repository-header">库房五</div>
                    <el-row :gutter="20">
                        <el-col :span="13">
                            <home-user-task-stats :taskData="userTaskData5"/>
                        </el-col>
                        <el-col :span="11">
                            <home-sku-sale-stats :skuData="skuSaleData5"/>
                        </el-col>
                    </el-row>
                </div>
            </el-col>
        </el-row>
    </div>
</template>

<script setup name="Home">
import { ref } from 'vue';
import HomeUserTaskStats from '@/views/manage/home/components/home-user-task-stats.vue';
import HomeSkuSaleStats from '@/views/manage/home/components/home-sku-sale-stats.vue';

const userTaskData1 = ref({
    // 初始数据
    data1: 78,
    data2: 34,
    data3: 44,
    data4: 12
});

const skuSaleData1 = ref({
    // 初始数据
    data1: 6543,
    data2: 3456,
    data3: 6345,
    data4: 10
});

const userTaskData2 = ref({
    // 初始数据
    data1: 56,
    data2: 23,
    data3: 34,
    data4: 8
});

const skuSaleData2 = ref({
    // 初始数据
    data1: 5432,
    data2: 2345,
    data3: 5342,
    data4: 8
});

const userTaskData3 = ref({
    // 初始数据
    data1: 45,
    data2: 12,
    data3: 23,
    data4: 6
});

const skuSaleData3 = ref({
    // 初始数据
    data1: 4321,
    data2: 1234,
    data3: 4231,
    data4: 6
});

const userTaskData4 = ref({
    // 初始数据
    data1: 34,
    data2: 11,
    data3: 12,
    data4: 4
});

const skuSaleData4 = ref({
    // 初始数据
    data1: 3210,
    data2: 1122,
    data3: 3210,
    data4: 4
});

const userTaskData5 = ref({
    // 初始数据
    data1: 23,
    data2: 10,
    data3: 11,
    data4: 3
});

const skuSaleData5 = ref({
    // 初始数据
    data1: 2109,
    data2: 1011,
    data3: 2109,
    data4: 3
});
</script>

<style scoped lang="scss">
.home {
    blockquote {
        padding: 10px 20px;
        margin: 0 0 20px;
        font-size: 17.5px;
        border-left: 5px solid #eee;
    }

    hr {
        margin-top: 20px;
        margin-bottom: 20px;
        border: 0;
        border-top: 1px solid #eee;
    }

    .col-item {
        margin-bottom: 20px;
    }

    ul {
        padding: 0;
        margin: 0;
    }

    font-family: 'open sans',
    'Helvetica Neue',
    Helvetica,
    Arial,
    sans-serif;
    font-size: 13px;
    color: #676a6c;
    overflow-x: hidden;

    ul {
        list-style-type: none;
    }

    h4 {
        margin-top: 0px;
    }

    h2 {
        margin-top: 10px;
        font-size: 26px;
        font-weight: 100;
    }

    p {
        margin-top: 10px;

        b {
            font-weight: 700;
        }
    }

    .update-log {
        ol {
            display: block;
            list-style-type: decimal;
            margin-block-start: 1em;
            margin-block-end: 1em;
            margin-inline-start: 0;
            margin-inline-end: 0;
            padding-inline-start: 40px;
        }
    }

    :deep(.box) {
        padding: 20px;
        border-radius: 20px;

        .title,
        .header {
            display: flex;

            .el-icon {
                cursor: pointer;
            }
        }

        .title {
            align-items: center;
            flex: 1;
            font-size: 16px;
            font-weight: 600;
            color: #333;

        }

        .sub-title {
            margin-left: 10px;
            font-size: 12px;
            font-weight: 400;
            color: #999;
        }
    }

    .chart {
        position: relative;
        display: inline-block;
        width: 50%;
        height: 100%;
    }

    .sku-sale-rank {
        flex-direction: column;
        height: calc(60vh - 72px);
        min-height: 538px;
        background: #fff;
        border-radius: 20px;
    }

    .repository-container {
        background: #fff;
        border-radius: 20px;
        padding: 40px 20px 20px 20px; /* 增加上内边距 */
        position: relative;
        margin-bottom: 20px;
    }

    .repository-header {
        position: absolute;
        top: 10px;
        left: 10px;
        font-size: 18px;
        font-weight: 600;
        color: #333;
    }
}
</style>