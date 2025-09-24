<template>
    <div class="archive-return-container">
        <!-- 页面标题 -->
        <div class="page-header">
            <h2>RFID档案归还系统</h2>
            <div class="header-actions">
                <el-button type="primary" @click="cacheArchiveData" :loading="isCaching" icon="Download">
                    缓存档案数据
                </el-button>
                <el-button type="success" @click="syncToServer" :loading="isSyncing"
                    :disabled="networkStatus !== 'online' || cachedReturns.length === 0" icon="Upload">
                    同步到服务器 ({{ cachedReturns.length }})
                </el-button>
                <el-tag :type="connectionStatus === 'connected' ? 'success' : 'danger'" size="small">
                    RFID {{ connectionStatus === 'connected' ? '已连接' : '未连接' }}
                </el-tag>
                <el-tag :type="networkStatus === 'online' ? 'success' : 'info'" size="small">
                    {{ networkStatus === 'online' ? '在线' : '离线' }}
                </el-tag>
            </div>
        </div>

        <!-- 主要操作区 -->
        <el-row :gutter="20">
            <!-- 左侧控制面板 -->
            <el-col :span="8">
                <!-- 扫描控制卡片 -->
                <el-card class="control-card" shadow="hover">
                    <template #header>
                        <span>扫描控制</span>
                    </template>

                    <div class="scan-actions">
                        <el-button type="primary" :disabled="isScanning || connectionStatus !== 'connected'"
                            :loading="isStarting" @click="startScanning" icon="VideoPlay" size="large">
                            开始扫描
                        </el-button>

                        <el-button type="danger" :disabled="!isScanning" :loading="isStopping" @click="stopScanning"
                            icon="VideoPause" size="large">
                            停止扫描
                        </el-button>
                    </div>

                    <el-alert title="扫描档案RFID标签进行归还识别" type="info" :closable="false" show-icon
                        style="margin-top: 15px;">
                        请将需要归还的档案放置在RFID扫描区域
                    </el-alert>
                </el-card>

                <!-- 缓存状态卡片 -->
                <el-card class="cache-card" shadow="hover">
                    <template #header>
                        <span>缓存状态</span>
                    </template>

                    <el-descriptions :column="1" border size="small">
                        <el-descriptions-item label="缓存档案数">
                            <el-tag type="info">{{ cachedArchiveCount }}</el-tag>
                        </el-descriptions-item>
                        <el-descriptions-item label="待同步归还">
                            <el-tag type="warning">{{ cachedReturns.length }}</el-tag>
                        </el-descriptions-item>
                        <el-descriptions-item label="本地存储">
                            <el-tag :type="localStorageStatus ? 'success' : 'danger'">
                                {{ localStorageStatus ? '正常' : '异常' }}
                            </el-tag>
                        </el-descriptions-item>
                    </el-descriptions>
                </el-card>

                <!-- 扫描统计 -->
                <el-card class="stats-card" shadow="hover">
                    <template #header>
                        <span>扫描统计</span>
                    </template>

                    <el-descriptions :column="1" border size="small">
                        <el-descriptions-item label="已扫描档案">
                            <el-tag type="success">{{ scannedArchives.length }}</el-tag>
                        </el-descriptions-item>
                        <el-descriptions-item label="可归还档案">
                            <el-tag type="primary">{{ returnableCount }}</el-tag>
                        </el-descriptions-item>
                        <el-descriptions-item label="已归还档案">
                            <el-tag type="info">{{ returnedCount }}</el-tag>
                        </el-descriptions-item>
                        <el-descriptions-item label="异常档案">
                            <el-tag :type="errorCount > 0 ? 'danger' : 'success'">{{ errorCount }}</el-tag>
                        </el-descriptions-item>
                    </el-descriptions>
                </el-card>

                <!-- 最新扫描信息 -->
                <el-card class="latest-scan" shadow="hover">
                    <template #header>
                        <span>最新扫描</span>
                    </template>

                    <div v-if="latestScan" class="scan-info">
                        <el-descriptions :column="1" border size="small">
                            <el-descriptions-item label="EPC">
                                <code>{{ latestScan.epc }}</code>
                            </el-descriptions-item>
                            <el-descriptions-item label="档号">
                                {{ latestScan.archive?.danghao || '未找到档案' }}
                            </el-descriptions-item>
                            <el-descriptions-item label="借阅状态">
                                <el-tag :type="latestScan.borrowStatus?.type || 'info'">
                                    {{ latestScan.borrowStatus?.text || '无借阅记录' }}
                                </el-tag>
                            </el-descriptions-item>
                            <el-descriptions-item label="扫描时间">
                                {{ formatTime(latestScan.timestamp) }}
                            </el-descriptions-item>
                        </el-descriptions>
                    </div>

                    <el-empty v-else description="暂无扫描数据" :image-size="60" />
                </el-card>
            </el-col>

            <!-- 右侧档案列表 -->
            <el-col :span="16">
                <el-card class="archive-list" shadow="hover">
                    <template #header>
                        <div class="list-header">
                            <span>扫描档案列表</span>
                            <div class="header-controls">
                                <el-button type="warning" @click="clearScannedList" icon="Delete">
                                    清空列表
                                </el-button>
                            </div>
                        </div>
                    </template>

                    <el-table :data="scannedArchives" height="600" size="small" stripe
                        :row-class-name="getRowClassName">
                        <el-table-column type="index" width="50" label="序号" />
                        <el-table-column prop="danghao" label="档号" width="300" show-overflow-tooltip />
                        <el-table-column prop="name" label="档案名称" min-width="180" show-overflow-tooltip />
                        <el-table-column label="原位置" width="100">
                            <template #default="{ row }">
                                <span v-if="row.originalLocation">{{ row.originalLocation }}</span>
                                <el-tag v-else type="warning" size="small">位置缺失</el-tag>
                            </template>
                        </el-table-column>
                        <el-table-column label="借阅人" width="100">
                            <template #default="{ row }">
                                <span v-if="row.borrowRecord">{{ row.borrowRecord.borrowerName }}</span>
                                <el-tag v-else type="info" size="small">无记录</el-tag>
                            </template>
                        </el-table-column>
                        <el-table-column label="应还日期" width="120">
                            <template #default="{ row }">
                                <span v-if="row.borrowRecord">{{ formatDate(row.borrowRecord.endDate) }}</span>
                                <span v-else>-</span>
                            </template>
                        </el-table-column>
                        <el-table-column label="归还状态" width="90" align="center">
                            <template #default="{ row }">
                                <el-tag v-if="row.isReturned" type="success" effect="dark">
                                    已归还
                                </el-tag>
                                <el-tag v-else-if="row.canReturn" type="primary">
                                    可归还
                                </el-tag>
                                <el-tag v-else type="danger">
                                    不可归还
                                </el-tag>
                            </template>
                        </el-table-column>
                        <el-table-column label="操作" width="120" fixed="right">
                            <template #default="{ row }">
                                <el-button v-if="row.canReturn && !row.isReturned" size="small" type="primary"
                                    @click="singleReturn(row)" :loading="row.isProcessing">
                                    归还上架
                                </el-button>
                                <el-button v-else-if="row.isReturned" size="small" type="text" disabled>
                                    已完成
                                </el-button>
                                <el-tooltip v-else content="该档案无有效借阅记录或缺少位置信息" placement="top">
                                    <el-button size="small" type="text" disabled>
                                        无法归还
                                    </el-button>
                                </el-tooltip>
                            </template>
                        </el-table-column>
                    </el-table>
                </el-card>
            </el-col>
        </el-row>

        <!-- 归还历史 -->
        <el-card class="history-card" shadow="hover">
            <template #header>
                <span>本次归还历史</span>
            </template>

            <el-table :data="returnHistory" height="300" size="small" stripe>
                <el-table-column prop="time" label="归还时间" width="160">
                    <template #default="{ row }">
                        {{ formatTime(row.time) }}
                    </template>
                </el-table-column>
                <el-table-column prop="danghao" label="档号" width="400" show-overflow-tooltip />
                <el-table-column prop="name" label="档案名称" min-width="300" />
                <el-table-column prop="originalLocation" label="归还位置" width="120" />
                <el-table-column prop="borrowerName" label="借阅人" width="100" />
                <el-table-column prop="overdueDays" label="逾期天数" width="100">
                    <template #default="{ row }">
                        <el-tag :type="row.overdueDays > 0 ? 'danger' : 'success'" size="small">
                            {{ row.overdueDays > 0 ? `逾期${row.overdueDays}天` : '正常' }}
                        </el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="status" label="状态" width="100">
                    <template #default="{ row }">
                        <el-tag :type="getStatusTagType(row.status)" size="small">
                            {{ getStatusText(row.status) }}
                        </el-tag>
                    </template>
                </el-table-column>
            </el-table>
        </el-card>
    </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted, watch } from 'vue'
import { ElMessage, ElMessageBox, ElLoading } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { updateRecord, listRecord } from '@/api/manage/borrowrecord'
import { getArchivesByRfids, getArchiveByRfid, updateArchive, listArchiveAll } from '@/api/manage/archive'
import { addArchiveLog, listArchiveLog } from '@/api/manage/archiveLog'
import { getUserProfile, getUser } from '@/api/system/user'
import { updateCompartmentSize } from '@/api/manage/compartment'
import { updateCabinetSize } from '@/api/manage/cabinet'
import dayjs from 'dayjs'

// IndexedDB配置
const DB_NAME = 'ArchiveReturnDB'
const DB_VERSION = 1
const ARCHIVE_STORE = 'archives'
const RETURN_STORE = 'cached_returns'
let db = null

// 获取当前登录用户信息
const userProfile = ref(null)

// 响应式数据
const connectionStatus = ref('disconnected')
const networkStatus = ref('online')
const isScanning = ref(false)
const isStarting = ref(false)
const isStopping = ref(false)
const isCaching = ref(false)
const isSyncing = ref(false)
const localStorageStatus = ref(false)

// 缓存相关
const cachedArchives = ref([])
const cachedReturns = ref([])

// 扫描相关
const latestScan = ref(null)
const scannedArchives = ref([])
const returnHistory = ref([])

// SSE连接
let eventSource = null
const clientId = 'archive_return_' + Date.now()

// 计算属性
const returnableCount = computed(() => {
    return scannedArchives.value.filter(archive => archive.canReturn && !archive.isReturned).length
})

const returnedCount = computed(() => {
    return scannedArchives.value.filter(archive => archive.isReturned).length
})

const errorCount = computed(() => {
    return scannedArchives.value.filter(archive => !archive.canReturn && !archive.isReturned).length
})

const cachedArchiveCount = computed(() => {
    return cachedArchives.value.length
})

// 初始化IndexedDB
const initIndexedDB = () => {
    return new Promise((resolve, reject) => {
        const request = indexedDB.open(DB_NAME, DB_VERSION)

        request.onerror = () => {
            console.error('IndexedDB打开失败')
            reject(request.error)
        }

        request.onsuccess = () => {
            db = request.result
            localStorageStatus.value = true
            resolve(db)
        }

        request.onupgradeneeded = (event) => {
            db = event.target.result

            // 创建档案缓存存储
            if (!db.objectStoreNames.contains(ARCHIVE_STORE)) {
                const archiveStore = db.createObjectStore(ARCHIVE_STORE, { keyPath: 'id' })
                archiveStore.createIndex('rfid', 'rfid', { unique: false })
            }

            // 创建归还缓存存储
            if (!db.objectStoreNames.contains(RETURN_STORE)) {
                const returnStore = db.createObjectStore(RETURN_STORE, {
                    keyPath: 'id',
                    autoIncrement: true
                })
                returnStore.createIndex('archiveId', 'archiveId', { unique: false })
                returnStore.createIndex('timestamp', 'timestamp', { unique: false })
            }
        }
    })
}

// 缓存档案数据
const cacheArchiveData = async () => {
    isCaching.value = true
    try {
        // 获取所有档案数据
        const response = await listArchiveAll()
        const archives = response.rows || []

        // 保存到IndexedDB
        const transaction = db.transaction([ARCHIVE_STORE], 'readwrite')
        const store = transaction.objectStore(ARCHIVE_STORE)

        // 清空现有数据
        await store.clear()

        // 批量保存
        for (const archive of archives) {
            await store.add(archive)
        }

        cachedArchives.value = archives
        ElMessage.success(`成功缓存 ${archives.length} 条档案数据`)
    } catch (error) {
        console.error('缓存档案数据失败:', error)
        ElMessage.error('缓存档案数据失败')
    } finally {
        isCaching.value = false
    }
}

// 从IndexedDB加载缓存数据
const loadCachedData = async () => {
    if (!db) return

    try {
        // 加载档案缓存
        const archiveTransaction = db.transaction([ARCHIVE_STORE], 'readonly')
        const archiveStore = archiveTransaction.objectStore(ARCHIVE_STORE)
        const archiveRequest = archiveStore.getAll()

        archiveRequest.onsuccess = () => {
            cachedArchives.value = archiveRequest.result || []
        }

        // 加载归还缓存
        const returnTransaction = db.transaction([RETURN_STORE], 'readonly')
        const returnStore = returnTransaction.objectStore(RETURN_STORE)
        const returnRequest = returnStore.getAll()

        returnRequest.onsuccess = () => {
            cachedReturns.value = returnRequest.result || []
        }
    } catch (error) {
        console.error('加载缓存数据失败:', error)
    }
}

// 初始化用户信息
const initUserProfile = async () => {
    try {
        const profile = await getUserProfile()
        userProfile.value = profile.data
    } catch (error) {
        console.error('获取用户信息失败:', error)
        ElMessage.error('获取用户信息失败')
    }
}

// 连接SSE
const connectSSE = () => {
    try {
        eventSource = new EventSource(`http://localhost:8080/rfid/connect?clientId=${clientId}`)

        eventSource.addEventListener('connected', () => {
            connectionStatus.value = 'connected'
            ElMessage.success('已连接到RFID服务')
        })

        eventSource.addEventListener('scan_started', () => {
            isScanning.value = true
            isStarting.value = false
            ElMessage.success('扫描已开始')
        })

        eventSource.addEventListener('scan_stopped', () => {
            isScanning.value = false
            isStopping.value = false
            ElMessage.info('扫描已停止')
        })

        eventSource.addEventListener('tag_data', (event) => {
            const tagInfo = JSON.parse(event.data)
            handleTagScanned(tagInfo)
        })

        // 监听RFID写入成功事件
        eventSource.addEventListener('write_success', () => {
            ElMessage.success('RFID标签写入成功')
        })

        // 监听RFID写入错误事件
        eventSource.addEventListener('write_error', (event) => {
            const errorInfo = JSON.parse(event.data)
            ElMessage.error('RFID标签写入失败: ' + errorInfo.error)
        })

        eventSource.addEventListener('error', (event) => {
            const errorInfo = JSON.parse(event.data)
            ElMessage.error('错误: ' + errorInfo.error)
        })

        eventSource.onerror = () => {
            connectionStatus.value = 'disconnected'
            ElMessage.error('连接中断，正在重连...')

            setTimeout(() => {
                if (eventSource.readyState === EventSource.CLOSED) {
                    connectSSE()
                }
            }, 3000)
        }
    } catch (error) {
        console.error('建立SSE连接失败:', error)
        ElMessage.error('无法连接到RFID服务')
    }
}

// 处理标签扫描
const handleTagScanned = async (tagInfo) => {
    const epc = tagInfo.epc

    // 更新最新扫描信息
    latestScan.value = {
        epc,
        timestamp: tagInfo.timestamp,
        ...tagInfo
    }

    // 检查是否已经扫描过
    const existingIndex = scannedArchives.value.findIndex(archive => archive.rfid === epc)
    if (existingIndex > -1) {
        ElMessage.info('该档案已扫描过')
        return
    }

    try {
        // 优先从缓存中查找档案信息
        let archiveInfo = cachedArchives.value.find(archive => archive.rfid === epc)

        // 如果缓存中没有，尝试从服务器获取
        if (!archiveInfo && networkStatus.value === 'online') {
            archiveInfo = await queryArchiveByRfid(epc)
        }

        if (!archiveInfo) {
            ElMessage.warning(`未找到RFID为 ${epc} 的档案信息，请先缓存档案数据`)
            return
        }

        // 查询借阅记录
        const borrowRecord = await queryActiveBorrowRecord(archiveInfo.id)

        // 查询原位置信息（从最近的下架记录中获取）
        const originalLocation = await queryOriginalLocation(archiveInfo.id)

        // 构建扫描档案对象
        const scannedArchive = {
            ...archiveInfo,
            rfid: epc,
            borrowRecord,
            originalLocation,
            borrowStatus: getBorrowStatus(borrowRecord),
            canReturn: canReturn(borrowRecord, originalLocation),
            isReturned: false,
            isProcessing: false,
            scannedTime: Date.now()
        }

        // 更新最新扫描信息
        latestScan.value.archive = archiveInfo
        latestScan.value.originalLocation = originalLocation
        latestScan.value.borrowStatus = scannedArchive.borrowStatus

        // 添加到扫描列表
        scannedArchives.value.unshift(scannedArchive)

        if (scannedArchive.canReturn) {
            ElMessage.success(`扫描到可归还档案: ${archiveInfo.name}`)
        } else {
            const reason = !borrowRecord ? '无有效借阅记录' : '缺少原位置信息'
            ElMessage.warning(`扫描到档案: ${archiveInfo.name}，但${reason}`)
        }
    } catch (error) {
        console.error('处理扫描标签失败:', error)
        ElMessage.error('处理扫描数据失败')
    }
}

// 查询档案信息
const queryArchiveByRfid = async (rfid) => {
    try {
        const res = await getArchiveByRfid(rfid)
        return res.data
    } catch (error) {
        console.error('查询档案失败:', error)
        return null
    }
}

// 查询有效借阅记录
const queryActiveBorrowRecord = async (archiveId) => {
    try {
        const res = await listRecord({ archiveId })
        const borrowRecords = res.rows || []

        let borrowRecord = borrowRecords.find(record =>
            (record.status === '已批准' || record.status === '已逾期') &&
            (!record.remark || record.remark === null || !record.remark.includes('电子借阅'))
        )

        if (borrowRecord) {
            try {
                if (borrowRecord.userId) {
                    const borrowUser = await getUser(borrowRecord.userId)
                    borrowRecord.borrowerName = borrowUser.data.userName
                } else {
                    borrowRecord.borrowerName = '未知用户'
                }
            } catch (error) {
                console.error('获取用户信息失败:', error)
                borrowRecord.borrowerName = '未知用户'
            }
        }

        return borrowRecord
    } catch (error) {
        console.error('查询借阅记录失败:', error)
        return null
    }
}

// 查询原位置信息（从最近的下架记录中获取）
const queryOriginalLocation = async (archiveId) => {
    try {
        // 查询档案最近的下架日志
        const res = await listArchiveLog({
            archiveId,
            taskType: '上架',
            pageNum: 1,
            pageSize: 1
        })

        const logs = res.rows || []
        if (logs.length > 0) {
            const latestLog = logs[0]
            // 从日志的备注或其他字段中获取原位置信息
            // 这里假设原位置信息存储在某个字段中，需要根据实际情况调整
            return latestLog.taskStatus || null
        }

        return null
    } catch (error) {
        console.error('查询原位置失败:', error)
        return null
    }
}

// 获取借阅状态
const getBorrowStatus = (borrowRecord) => {
    if (!borrowRecord) {
        return { text: '无借阅记录', type: 'info' }
    }

    const isOverdueFlag = borrowRecord.status === '已逾期'
    return {
        text: isOverdueFlag ? '逾期未还' : '正常借阅',
        type: isOverdueFlag ? 'danger' : 'warning'
    }

}

// 判断是否可以归还
const canReturn = (borrowRecord, originalLocation) => {
    return borrowRecord &&
        (borrowRecord.status === '已批准' || borrowRecord.status === '已逾期') &&
        originalLocation // 必须有原位置信息才能归还上架
}

// 判断是否逾期
const isOverdue = (borrowRecord) => {
    if (!borrowRecord || !borrowRecord.endDate) return false

    const endDate = new Date(borrowRecord.endDate)
    const currentDate = new Date()
    currentDate.setHours(0, 0, 0, 0)
    endDate.setHours(0, 0, 0, 0)

    return currentDate > endDate
}

// 计算逾期天数
const calculateOverdueDays = (borrowRecord) => {
    if (borrowRecord.status !== '已逾期') return 0

    const endDate = new Date(borrowRecord.endDate)
    const currentDate = new Date()
    currentDate.setHours(0, 0, 0, 0)
    endDate.setHours(0, 0, 0, 0)

    const diffTime = currentDate - endDate
    return Math.ceil(diffTime / (1000 * 60 * 60 * 24))
}

// 写入RFID标签
const writeRFIDTag = async (rfidData) => {
    const response = await fetch('http://localhost:8080/rfid/write', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ epcData: rfidData })
    })

    if (!response.ok) {
        throw new Error('写入请求失败')
    }

    return new Promise((resolve, reject) => {
        const timeout = setTimeout(() => reject(new Error('写入超时')), 10000)

        const successHandler = () => {
            clearTimeout(timeout)
            resolve()
        }

        const errorHandler = (event) => {
            clearTimeout(timeout)
            const errorInfo = JSON.parse(event.data)
            reject(new Error(errorInfo.error))
        }

        eventSource.addEventListener('write_success', successHandler, { once: true })
        eventSource.addEventListener('write_error', errorHandler, { once: true })
    })
}

// 单个归还
const singleReturn = async (archive) => {
    archive.isProcessing = true

    try {
        // 先写入RFID标签，将标签改为05结尾表示在架
        const newRfid = archive.rfid.slice(0, -2) + '05'
        await writeRFIDTag(newRfid)

        // RFID写入成功后再处理归还业务
        await processReturn(archive, newRfid)
        archive.isReturned = true
        ElMessage.success(`档案 ${archive.name} 归还上架成功`)
    } catch (error) {
        ElMessage.error(`归还失败: ${error.message}`)
    } finally {
        archive.isProcessing = false
    }
}

// 处理归还逻辑（包含上架操作）
const processReturn = async (archive, newRfid) => {
    const borrowRecord = archive.borrowRecord
    if (!borrowRecord) {
        throw new Error('无有效借阅记录')
    }

    if (!archive.originalLocation) {
        throw new Error('缺少原位置信息')
    }

    const overdueDays = calculateOverdueDays(borrowRecord)
    const currentTime = dayjs().format('YYYY-MM-DD HH:mm:ss')

    // 解析原位置信息
    const locationParts = archive.originalLocation.split('-')
    const shitiLocation = locationParts.slice(0, 3).join('-') // 例如: "1-1-A"
    const exactLocation = locationParts.slice(3).join('-') // 例如: "1-1"

    // 构建归还数据（包含上架信息）
    const returnData = {
        // 借阅记录更新
        borrowRecordUpdate: {
            id: borrowRecord.id,
            status: '已结束',
            returnDate: currentTime,
            overdueDays: overdueDays
        },
        // 档案上架更新
        archiveUpdate: {
            id: archive.id,
            rfid: newRfid, // 使用写入后的新RFID
            availability: 0, // 设为在架状态
            shitiLocation: shitiLocation,
            exactLocation: exactLocation
        },
        // 归还日志
        returnLog: {
            archiveId: archive.id,
            taskType: '档案归还',
            initiator: borrowRecord.borrowerName,
            handler: userProfile.value?.userName || '系统',
            endDate: currentTime,
            taskStatus: overdueDays > 0 ? '逾期归还' : '正常归还',
            remark: `归还并上架到 ${archive.originalLocation}`
        },
        // 上架日志  
        shelfLog: {
            archiveId: archive.id,
            taskType: '入库',
            initiator: userProfile.value?.userName || '系统',
            taskStatus: archive.originalLocation,
            remark: '归还时入库并自动上架',
            startDate: currentTime
        },
        // 元数据
        timestamp: Date.now(),
        archiveInfo: {
            id: archive.id,
            danghao: archive.danghao,
            name: archive.name,
            originalLocation: archive.originalLocation
        }
    }

    try {
        if (networkStatus.value === 'online') {
            // 在线模式：直接同步到服务器
            await syncReturnToServer(returnData)

            // 添加到历史记录
            returnHistory.value.unshift({
                time: Date.now(),
                danghao: archive.danghao,
                name: archive.name,
                originalLocation: archive.originalLocation,
                borrowerName: borrowRecord.borrowerName,
                overdueDays: overdueDays,
                status: 'synced'
            })
        } else {
            // 离线模式：缓存到本地
            await cacheReturnData(returnData)

            // 添加到历史记录
            returnHistory.value.unshift({
                time: Date.now(),
                danghao: archive.danghao,
                name: archive.name,
                originalLocation: archive.originalLocation,
                borrowerName: borrowRecord.borrowerName,
                overdueDays: overdueDays,
                status: 'cached'
            })
        }
    } catch (error) {
        // 添加失败记录
        returnHistory.value.unshift({
            time: Date.now(),
            danghao: archive.danghao,
            name: archive.name,
            originalLocation: archive.originalLocation,
            borrowerName: borrowRecord.borrowerName,
            overdueDays: overdueDays,
            status: 'failed'
        })

        throw error
    }
}

// 缓存归还数据到本地
const cacheReturnData = async (returnData) => {
    if (!db) throw new Error('本地存储不可用')

    const transaction = db.transaction([RETURN_STORE], 'readwrite')
    const store = transaction.objectStore(RETURN_STORE)

    await store.add(returnData)

    // 更新缓存列表
    cachedReturns.value.push(returnData)

    return new Promise((resolve, reject) => {
        transaction.oncomplete = () => resolve()
        transaction.onerror = () => reject(transaction.error)
    })
}

// 同步归还数据到服务器
const syncReturnToServer = async (returnData) => {
    // 1. 更新借阅记录
    await updateRecord(returnData.borrowRecordUpdate)

    // 2. 更新档案信息（上架）
    await updateArchive(returnData.archiveUpdate)

    // 3. 添加归还日志
    await addArchiveLog(returnData.returnLog)

    // 4. 添加上架日志
    await addArchiveLog(returnData.shelfLog)

    // 5. 更新档案柜和储物格数量
    updateCabinetSize(returnData.archiveUpdate.shitiLocation, 'add')
    updateCompartmentSize(returnData.archiveUpdate.shitiLocation + '-' + returnData.archiveUpdate.exactLocation, 'add')
}

// 同步所有缓存数据到服务器
const syncToServer = async () => {
    if (cachedReturns.value.length === 0) {
        ElMessage.warning('没有待同步的数据')
        return
    }

    isSyncing.value = true
    const loading = ElLoading.service({
        lock: true,
        text: '正在同步归还数据到服务器...',
        background: 'rgba(0, 0, 0, 0.7)'
    })

    let successCount = 0
    let failCount = 0

    try {
        for (const returnData of cachedReturns.value) {
            try {
                await syncReturnToServer(returnData)
                successCount++

                // 更新历史记录状态
                const historyItem = returnHistory.value.find(item =>
                    item.danghao === returnData.archiveInfo.danghao &&
                    item.status === 'cached'
                )
                if (historyItem) {
                    historyItem.status = 'synced'
                }
            } catch (error) {
                console.error(`同步失败:`, error)
                failCount++

                // 更新历史记录状态
                const historyItem = returnHistory.value.find(item =>
                    item.danghao === returnData.archiveInfo.danghao &&
                    item.status === 'cached'
                )
                if (historyItem) {
                    historyItem.status = 'failed'
                }
            }
        }

        if (successCount > 0) {
            // 清空已同步的缓存数据
            const transaction = db.transaction([RETURN_STORE], 'readwrite')
            const store = transaction.objectStore(RETURN_STORE)
            await store.clear()
            cachedReturns.value = []
        }

        loading.close()

        if (failCount > 0) {
            ElMessage.warning(`同步完成：成功 ${successCount} 个，失败 ${failCount} 个`)
        } else {
            ElMessage.success(`成功同步 ${successCount} 个归还记录`)
        }
    } catch (error) {
        loading.close()
        ElMessage.error('同步过程中发生错误')
    } finally {
        isSyncing.value = false
    }
}

// 清空扫描列表
const clearScannedList = () => {
    ElMessageBox.confirm('确定要清空扫描列表吗？', '确认', {
        type: 'warning'
    }).then(() => {
        scannedArchives.value = []
        latestScan.value = null
        ElMessage.success('扫描列表已清空')
    }).catch(() => {
        // 用户取消
    })
}

// 开始扫描
const startScanning = async () => {
    isStarting.value = true
    try {
        const response = await fetch('http://localhost:8080/rfid/start', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' }
        })

        if (!response.ok) {
            throw new Error('启动扫描失败')
        }
    } catch (error) {
        isStarting.value = false
        ElMessage.error('启动扫描失败: ' + error.message)
    }
}

// 停止扫描
const stopScanning = async () => {
    isStopping.value = true
    try {
        const response = await fetch('http://localhost:8080/rfid/stop', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' }
        })

        if (!response.ok) {
            throw new Error('停止扫描失败')
        }
    } catch (error) {
        isStopping.value = false
        ElMessage.error('停止扫描失败: ' + error.message)
    }
}

// 获取行样式
const getRowClassName = ({ row }) => {
    const classes = []
    if (row.isReturned) {
        classes.push('returned-row')
    } else if (row.canReturn) {
        classes.push('returnable-row')
    } else {
        classes.push('error-row')
    }
    return classes.join(' ')
}

// 获取状态标签类型
const getStatusTagType = (status) => {
    switch (status) {
        case 'synced': return 'success'
        case 'cached': return 'warning'
        case 'failed': return 'danger'
        default: return 'info'
    }
}

// 获取状态文本
const getStatusText = (status) => {
    switch (status) {
        case 'synced': return '已同步'
        case 'cached': return '已缓存'
        case 'failed': return '同步失败'
        default: return '未知'
    }
}

// 格式化时间
const formatTime = (timestamp) => {
    return new Date(timestamp).toLocaleString('zh-CN')
}

// 格式化日期
const formatDate = (dateStr) => {
    return new Date(dateStr).toLocaleDateString('zh-CN')
}

// 监听网络状态
const updateNetworkStatus = () => {
    networkStatus.value = navigator.onLine ? 'online' : 'offline'
}

// 生命周期
onMounted(async () => {
    await initUserProfile()
    await initIndexedDB()
    await loadCachedData()
    connectSSE()

    // 监听网络状态
    window.addEventListener('online', updateNetworkStatus)
    window.addEventListener('offline', updateNetworkStatus)
    updateNetworkStatus()
})

onUnmounted(() => {
    if (eventSource) {
        eventSource.close()
    }

    window.removeEventListener('online', updateNetworkStatus)
    window.removeEventListener('offline', updateNetworkStatus)
})
</script>

<style scoped>
.archive-return-container {
    padding: 20px;
    background: #f5f7fa;
    min-height: 100vh;
}

.page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    padding: 0 10px;
}

.page-header h2 {
    margin: 0;
    color: #303133;
}

.header-actions {
    display: flex;
    gap: 10px;
    align-items: center;
}

.control-card {
    margin-bottom: 20px;
}

.scan-actions {
    display: flex;
    flex-direction: column;
    gap: 15px;
}

.cache-card {
    margin-bottom: 20px;
}

.stats-card {
    margin-bottom: 20px;
}

.latest-scan {
    margin-bottom: 20px;
}

.scan-info code {
    background: #f4f4f5;
    padding: 4px 8px;
    border-radius: 4px;
    font-family: 'Courier New', monospace;
    font-size: 12px;
}

.archive-list {
    margin-bottom: 20px;
}

.list-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.header-controls {
    display: flex;
    gap: 10px;
}

.rfid-code {
    background: #f4f4f5;
    padding: 2px 6px;
    border-radius: 3px;
    font-family: 'Courier New', monospace;
    font-size: 12px;
}

:deep(.returned-row) {
    background-color: #f0f9ff !important;
}

:deep(.returned-row:hover > td) {
    background-color: #e6f7ff !important;
}

:deep(.returnable-row) {
    background-color: #f6ffed !important;
}

:deep(.returnable-row:hover > td) {
    background-color: #d9f7be !important;
}

:deep(.error-row) {
    background-color: #fff2f0 !important;
}

:deep(.error-row:hover > td) {
    background-color: #ffece6 !important;
}

.history-card {
    margin-top: 20px;
}

/* 响应式设计 */
@media (max-width: 1200px) {
    .el-col-8 {
        width: 100%;
        margin-bottom: 20px;
    }

    .el-col-16 {
        width: 100%;
    }
}
</style>