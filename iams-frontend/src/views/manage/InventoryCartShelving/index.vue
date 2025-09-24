<template>
  <div class="shelf-management-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>RFID档案上下架管理</h2>
      <div class="header-actions">
        <el-button type="primary" @click="syncFromServer" :loading="isSyncing" icon="Refresh">
          缓存档案数据
        </el-button>
        <el-tag :type="connectionStatus === 'connected' ? 'success' : 'danger'" size="small">
          RFID {{ connectionStatus === 'connected' ? '已连接' : '未连接' }}
        </el-tag>
      </div>
    </div>

    <!-- 统计概览 -->
    <el-row :gutter="20" class="stats-overview">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-item">
            <div class="stat-value">{{ pendingShelfCount }}</div>
            <div class="stat-label">待上架</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-item">
            <div class="stat-value">{{ pendingUnshelfCount }}</div>
            <div class="stat-label">待下架</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-item">
            <div class="stat-value">{{ completedTodayCount }}</div>
            <div class="stat-label">今日完成</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-item">
            <div class="stat-value">{{ cachedArchiveCount }}</div>
            <div class="stat-label">缓存档案</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 扫描控制区 -->
    <el-card class="scan-control" shadow="hover">
      <template #header>
        <span>扫描控制</span>
      </template>

      <div class="control-section">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="操作模式:">
              <el-radio-group v-model="operationMode" @change="handleModeChange">
                <el-radio-button label="shelf">上架模式</el-radio-button>
                <el-radio-button label="unshelf">下架模式</el-radio-button>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <div class="scan-buttons">
              <el-button type="primary" :disabled="isScanning || connectionStatus !== 'connected'" :loading="isStarting"
                @click="startScanning" icon="VideoPlay">
                开始扫描
              </el-button>
              <el-button type="danger" :disabled="!isScanning" :loading="isStopping" @click="stopScanning"
                icon="VideoPause">
                停止扫描
              </el-button>
            </div>
          </el-col>
        </el-row>
      </div>

      <!-- 当前扫描信息 -->
      <div v-if="currentScannedTag" class="current-scan-info">
        <el-alert :title="`检测到标签: ${currentScannedTag.epc}`" type="info" :closable="false">
          <div v-if="matchedArchive" class="matched-archive-info">
            <el-descriptions :column="2" border size="small">
              <el-descriptions-item label="档号">{{ matchedArchive.danghao }}</el-descriptions-item>
              <el-descriptions-item label="档案名称">{{ matchedArchive.name }}</el-descriptions-item>
              <el-descriptions-item label="当前状态">
                <el-tag :type="getStatusType(matchedArchive)">{{ getStatusText(matchedArchive) }}</el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="位置">{{ matchedArchive.exactLocation || '未设置' }}</el-descriptions-item>
            </el-descriptions>

            <!-- 快捷操作 -->
            <div class="quick-action">
              <el-button v-if="operationMode === 'shelf' && canShelf(matchedArchive)" type="success"
                @click="quickShelf()" :loading="isProcessing" icon="Upload">
                确认上架
              </el-button>
              <el-button v-else-if="operationMode === 'unshelf' && canUnshelf(matchedArchive)" type="warning"
                @click="quickUnshelf()" :loading="isProcessing" icon="Download">
                确认下架
              </el-button>
              <el-tag v-else type="info">当前档案不符合{{ operationMode === 'shelf' ? '上架' : '下架' }}条件</el-tag>
            </div>
          </div>
          <div v-else>
            <el-tag type="warning">未匹配到档案信息</el-tag>
          </div>
        </el-alert>
      </div>
    </el-card>

    <!-- 待处理列表 -->
    <el-tabs v-model="activeTab" class="pending-lists">
      <!-- 待上架列表 -->
      <el-tab-pane label="待上架档案" name="pendingShelf">
        <div class="tab-header">
          <span>共 {{ pendingShelfArchives.length }} 条记录</span>
          <el-button size="small" @click="batchConfirmShelf" :disabled="!hasShelfCompleted">
            批量确认上架完成
          </el-button>
        </div>

        <el-table :data="pendingShelfArchives" height="400" stripe size="small">
          <el-table-column prop="danghao" label="档号" width="120" />
          <el-table-column prop="name" label="档案名称" min-width="200" />
          <el-table-column prop="rfid" label="RFID标签" width="180">
            <template #default="{ row }">
              <code>{{ row.rfid }}</code>
              <el-tag v-if="row.tempRfid" type="success" size="small" style="margin-left: 8px">
                已修改为: {{ row.tempRfid }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="目标位置" width="150">
            <template #default="scope">
              {{ scope.row.shitiLocation }} {{ scope.row.exactLocation }}
            </template>
          </el-table-column>
          <el-table-column label="状态" width="120">
            <template #default="{ row }">
              <el-tag :type="row.isScanned ? 'success' : 'info'">
                {{ row.isScanned ? '已扫描' : '待扫描' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150">
            <template #default="{ row }">
              <el-button v-if="row.isScanned" size="small" type="success" @click="confirmShelfComplete(row)">
                确认完成
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <!-- 待下架列表 -->
      <el-tab-pane label="待下架档案" name="pendingUnshelf">
        <div class="tab-header">
          <span>共 {{ pendingUnshelfArchives.length }} 条记录</span>
          <el-button size="small" @click="batchSyncUnshelf" :disabled="!hasUnshelfScanned">
            批量同步下架数据
          </el-button>
        </div>

        <el-table :data="pendingUnshelfArchives" height="400" stripe size="small">
          <el-table-column prop="danghao" label="档号" width="120" />
          <el-table-column prop="name" label="档案名称" min-width="200" />
          <el-table-column prop="rfid" label="RFID标签" width="180">
            <template #default="{ row }">
              <code>{{ row.rfid }}</code>
              <el-tag v-if="row.tempRfid" type="warning" size="small" style="margin-left: 8px">
                已修改为: {{ row.tempRfid }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="目标位置" width="150">
            <template #default="scope">
              {{ scope.row.shitiLocation }} {{ scope.row.exactLocation }}
            </template>
          </el-table-column>
          <el-table-column label="状态" width="120">
            <template #default="{ row }">
              <el-tag :type="row.isScanned ? 'success' : 'warning'">
                {{ row.isScanned ? '已扫描' : '待扫描' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150">
            <template #default="{ row }">
              <el-button v-if="row.isScanned" size="small" type="primary" @click="syncUnshelfToServer(row)">
                同步到系统
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <!-- 操作历史 -->
      <el-tab-pane label="操作历史" name="history">
        <div class="tab-header">
          <span>最近 {{ operationHistory.length }} 条操作记录</span>
          <el-button size="small" @click="clearHistory">清空历史</el-button>
        </div>

        <el-table :data="operationHistory" height="400" stripe size="small">
          <el-table-column prop="time" label="操作时间" width="160">
            <template #default="{ row }">
              {{ formatTime(row.time) }}
            </template>
          </el-table-column>
          <el-table-column prop="type" label="操作类型" width="100">
            <template #default="{ row }">
              <el-tag :type="row.type === 'shelf' ? 'success' : 'warning'">
                {{ row.type === 'shelf' ? '上架' : '下架' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="danghao" label="档号" width="120" />
          <el-table-column prop="name" label="档案名称" min-width="200" />
          <el-table-column prop="status" label="结果" width="100">
            <template #default="{ row }">
              <el-tag :type="row.status === 'success' ? 'success' : 'danger'">
                {{ row.status === 'success' ? '成功' : '失败' }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listArchiveAll, updateArchive } from "@/api/manage/archive.js";
import { addArchiveLog } from "@/api/manage/ArchiveLog"
import { getUserProfile } from "@/api/system/user";
import { updateCabinetSize } from "@/api/manage/cabinet"
import { updateCompartmentSize } from "@/api/manage/compartment"
import dayjs from 'dayjs';

// 获取当前登录用户信息
const userProfile = ref(null)
const loadUserProfile = async () => {
  try {
    const profile = await getUserProfile()
    userProfile.value = profile
  } catch (error) {
    console.error('获取用户信息失败:', error)
  }
}

// IndexedDB 配置
const DB_NAME = 'RFIDArchiveDB'
const DB_VERSION = 2
const STORE_NAME = 'archives_shelf'
const HISTORY_STORE = 'operationHistory'

let db = null

// 响应式数据
const connectionStatus = ref('disconnected')
const isScanning = ref(false)
const isStarting = ref(false)
const isStopping = ref(false)
const isSyncing = ref(false)
const isProcessing = ref(false)
const operationMode = ref('shelf') // shelf/unshelf
const activeTab = ref('pendingShelf')
const currentScannedTag = ref(null)
const matchedArchive = ref(null)

// 档案数据
const allArchives = ref([])
const pendingShelfArchives = ref([])
const pendingUnshelfArchives = ref([])
const operationHistory = ref([])

// SSE 连接
let eventSource = null
const clientId = 'shelf_client_' + Date.now()

const initDB = () => {
  return new Promise((resolve, reject) => {
    const request = indexedDB.open(DB_NAME, DB_VERSION)

    request.onerror = () => reject(request.error)

    request.onsuccess = () => {
      db = request.result
      console.log('数据库打开成功', db.objectStoreNames)

      // 检查必需的 object stores 是否存在
      if (!db.objectStoreNames.contains(STORE_NAME) ||
          !db.objectStoreNames.contains(HISTORY_STORE)) {
        console.error('Object stores 不存在，需要升级数据库版本')
        // 可以选择删除数据库重建
        db.close()
        indexedDB.deleteDatabase(DB_NAME)
        // 重新初始化
        initDB().then(resolve).catch(reject)
        return
      }

      resolve(db)
    }

    request.onupgradeneeded = (event) => {
      console.log('升级数据库...')
      db = event.target.result

      // 档案存储
      if (!db.objectStoreNames.contains(STORE_NAME)) {
        const store = db.createObjectStore(STORE_NAME, { keyPath: 'id' })
        store.createIndex('rfid', 'rfid', { unique: false })
        store.createIndex('availability', 'availability', { unique: false })
        console.log('创建 archives_shelf store')
      }

      // 操作历史存储
      if (!db.objectStoreNames.contains(HISTORY_STORE)) {
        const historyStore = db.createObjectStore(HISTORY_STORE, {
          keyPath: 'id',
          autoIncrement: true
        })
        historyStore.createIndex('time', 'time', { unique: false })
        console.log('创建 operationHistory store')
      }
    }
  })
}

const syncFromServer = async () => {
  isSyncing.value = true
  try {
    const data = await listArchiveAll()

    if (data.code === 200) {
      const archives = data.rows || []

      // 存储到 IndexedDB
      const transaction = db.transaction([STORE_NAME], 'readwrite')
      const store = transaction.objectStore(STORE_NAME)

      // 清空现有数据
      await store.clear()

      // 批量添加
      const promises = archives.map(archive => store.add(archive))
      await Promise.all(promises)

      // 等待事务完成
      await new Promise((resolve, reject) => {
        transaction.oncomplete = resolve
        transaction.onerror = reject
      })

      ElMessage.success(`成功同步 ${archives.length} 条档案数据`)
      await loadArchivesFromDB()
    }
  } catch (error) {
    console.error('同步失败:', error)
    ElMessage.error('数据同步失败')
  } finally {
    isSyncing.value = false
  }
}

// 从 IndexedDB 加载数据
const loadArchivesFromDB = async () => {
  try {
    const transaction = db.transaction([STORE_NAME], 'readonly')
    const store = transaction.objectStore(STORE_NAME)
    const request = store.getAll()

    request.onsuccess = () => {
      allArchives.value = request.result || []
      filterArchives()
    }
  } catch (error) {
    console.error('加载数据失败:', error)
  }
}

// 筛选档案
const filterArchives = () => {
  // 待上架: 可用性=2 且 RFID是00结尾（包括新增上架和归还上架）且状态为archived
  pendingShelfArchives.value = allArchives.value.filter(archive => {
    return archive.exactLocation &&
        archive.availability === 2 &&
        archive.rfid &&
        archive.rfid.endsWith('00') &&
        archive.status === 'Archived' // 新增状态筛选条件
  }).map(archive => ({
    ...archive,
    isScanned: false,
    tempRfid: null // 临时存储修改后的RFID
  }))

  // 待下架: 可用性=1 且 RFID是05结尾且状态为archived
  pendingUnshelfArchives.value = allArchives.value.filter(archive => {
    return archive.rfid &&
        archive.availability === 1 &&
        archive.rfid.endsWith('05') &&
        (archive.status === 'Archived' || archive.status === 'Destroying') // 允许两种状态
  }).map(archive => ({
    ...archive,
    isScanned: false,
    tempRfid: null
  }))
}

// 优化后的SSE连接和事件处理
let reconnectCount = 0
const MAX_RECONNECT = 10
const connectSSE = () => {
  try {
    eventSource = new EventSource(`http://localhost:8080/rfid/connect?clientId=${clientId}`)

    eventSource.addEventListener('connected', () => {
      connectionStatus.value = 'connected'
      reconnectCount = 0 // 重置重连计数
      ElMessage.success('RFID设备已连接')
    })

    // 扫描开始事件 - 重置loading状态
    eventSource.addEventListener('scan_started', () => {
      isScanning.value = true
      isStarting.value = false // 重要：重置启动loading状态
      ElMessage.success('开始扫描')
    })

    // 扫描就绪事件
    eventSource.addEventListener('scan_ready', () => {
      isScanning.value = true
      isStarting.value = false
      console.log('扫描就绪')
    })

    // 扫描停止事件 - 重置loading状态
    eventSource.addEventListener('scan_stopped', () => {
      isScanning.value = false
      isStopping.value = false // 重要：重置停止loading状态
      ElMessage.info('扫描已停止')
    })

    // 标签数据事件
    eventSource.addEventListener('tag_data', (event) => {
      const tagInfo = JSON.parse(event.data)
      handleTagScanned(tagInfo)
    })

    // 写入成功事件
    eventSource.addEventListener('write_success', () => {
      isProcessing.value = false
      ElMessage.success('RFID标签写入成功')
    })

    // 写入错误事件
    eventSource.addEventListener('write_error', (event) => {
      isProcessing.value = false
      const errorInfo = JSON.parse(event.data)
      ElMessage.error('写入失败: ' + errorInfo.error)
    })

    // 错误处理事件 - 重置所有loading状态
    eventSource.addEventListener('error', (event) => {
      try {
        const errorInfo = JSON.parse(event.data)
        ElMessage.error('设备错误: ' + errorInfo.error)
      } catch (e) {
        ElMessage.error('设备发生未知错误')
      }

      // 重置所有loading状态
      isStarting.value = false
      isStopping.value = false
      isProcessing.value = false
      isScanning.value = false
    })

    // 连接错误处理
    eventSource.onerror = (error) => {
      console.error('SSE连接错误:', error)
      connectionStatus.value = 'disconnected'

      // 重置所有状态
      isScanning.value = false
      isStarting.value = false
      isStopping.value = false

      // 重连逻辑
      if (reconnectCount < MAX_RECONNECT) {
        reconnectCount++
        console.log(`尝试第${reconnectCount}次重连...`)
        setTimeout(() => {
          if (eventSource.readyState === EventSource.CLOSED) {
            connectSSE()
          }
        }, 3000 * reconnectCount)  // 递增重连延迟
      } else {
        ElMessage.error('RFID设备连接失败，请检查设备状态')
      }
    }

    // 连接打开事件
    eventSource.onopen = () => {
      console.log('SSE连接已建立')
      reconnectCount = 0
    }

  } catch (error) {
    console.error('SSE连接失败:', error)
    connectionStatus.value = 'disconnected'
  }
}

// 处理标签扫描
const handleTagScanned = (tagInfo) => {
  currentScannedTag.value = tagInfo

  // 根据扫描到的EPC查找档案
  // 注意：扫描到的可能是00或05结尾，需要同时查找
  const baseEpc = tagInfo.epc.slice(0, -2)

  if (operationMode.value === 'shelf') {
    // 上架模式：查找待上架列表中的档案
    matchedArchive.value = pendingShelfArchives.value.find(archive => {
      const archiveBase = archive.rfid.slice(0, -2)
      return archiveBase === baseEpc
    })
  } else {
    // 下架模式：查找待下架列表中的档案
    matchedArchive.value = pendingUnshelfArchives.value.find(archive => {
      const archiveBase = archive.rfid.slice(0, -2)
      return archiveBase === baseEpc
    })
  }

  if (matchedArchive.value) {
    // 标记为已扫描
    matchedArchive.value.isScanned = true
  }
}

// 快速上架
const quickShelf = async () => {
  if (!matchedArchive.value) return

  isProcessing.value = true
  try {
    // 生成05结尾的RFID
    const newRfid = matchedArchive.value.rfid.slice(0, -2) + '05'

    // 写入RFID标签
    await writeRFIDTag(newRfid)

    // 更新临时RFID显示
    matchedArchive.value.tempRfid = newRfid

    // 记录操作历史
    await addOperationHistory({
      type: 'shelf',
      archiveId: matchedArchive.value.id,
      danghao: matchedArchive.value.danghao,
      name: matchedArchive.value.name,
      time: Date.now(),
      status: 'success'
    })

    ElMessage.success('上架操作成功')
  } catch (error) {
    ElMessage.error('上架失败: ' + error.message)
    await addOperationHistory({
      type: 'shelf',
      archiveId: matchedArchive.value.id,
      danghao: matchedArchive.value.danghao,
      name: matchedArchive.value.name,
      time: Date.now(),
      status: 'failed'
    })
  } finally {
    isProcessing.value = false
  }
}

// 快速下架
const quickUnshelf = async () => {
  if (!matchedArchive.value) return

  isProcessing.value = true
  try {
    // 修改RFID标签为00
    const newRfid = matchedArchive.value.rfid.slice(0, -2) + '00'
    await writeRFIDTag(newRfid)

    // 更新本地数据
    matchedArchive.value.tempRfid = newRfid
    matchedArchive.value.isScanned = true

    // 记录操作历史
    await addOperationHistory({
      type: 'unshelf',
      archiveId: matchedArchive.value.id,
      danghao: matchedArchive.value.danghao,
      name: matchedArchive.value.name,
      time: Date.now(),
      status: 'success'
    })

    ElMessage.success('下架操作成功')
  } catch (error) {
    ElMessage.error('下架失败: ' + error.message)
    await addOperationHistory({
      type: 'unshelf',
      archiveId: matchedArchive.value.id,
      danghao: matchedArchive.value.danghao,
      name: matchedArchive.value.name,
      time: Date.now(),
      status: 'failed'
    })
  } finally {
    isProcessing.value = false
  }
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

// 添加工具函数创建const对象
const cleanArchiveForStorage = (archive) => {

  return {
    id: archive.id,
    danghao: archive.danghao,
    name: archive.name,
    rfid: archive.rfid,
    availability: archive.availability,
    shitiLocation: archive.shitiLocation,
    exactLocation: archive.exactLocation,
    guidangTime: archive.guidangTime,
    status: archive.status,
  }
}

// 确认上架完成（更新可用性为0，RFID更新为05）
const confirmShelfComplete = async (archive) => {
  try {
    // 更新本地数据
    archive.availability = 0
    archive.rfid = archive.tempRfid || (archive.rfid.slice(0, -2) + '05')

    // 更新 IndexedDB
    const cleanArchive = cleanArchiveForStorage(archive)
    const transaction = db.transaction([STORE_NAME], 'readwrite')
    const store = transaction.objectStore(STORE_NAME)
    await store.put(cleanArchive)

    // 添加入库日志
    let archiveInfo = '归还'
    if (!archive.guidangTime || archive.guidangTime === '') {
      archiveInfo = '归档'
    }
    await addArchiveLog({
      archiveId: archive.id,
      taskType: '入库',
      initiator: userProfile.userName,
      remark: archiveInfo,
      startDate: dayjs().format('YYYY-MM-DD HH:mm:ss'),
    });

    // TODO: 同步到服务器
    const params = {
      id: archive.id,
      rfid: archive.rfid,
      availability: 0,
      guidangTime: dayjs().format('YYYY-MM-DD HH:mm:ss')
    }
    await updateArchive(params)

    // 从待上架列表移除
    const index = pendingShelfArchives.value.findIndex(a => a.id === archive.id)
    if (index > -1) {
      pendingShelfArchives.value.splice(index, 1)
    }

    ElMessage.success('已确认上架完成')
  } catch (error) {
    ElMessage.error('确认失败: ' + error.message)
  }
}

const batchConfirmShelf = async () => {
  const scannedArchives = pendingShelfArchives.value.filter(a => a.isScanned)
  if (scannedArchives.length === 0) {
    ElMessage.warning('没有已扫描的档案')
    return
  }

  try {
    await ElMessageBox.confirm(
      `确认将 ${scannedArchives.length} 个已扫描档案标记为上架完成？`,
      '批量确认',
      { type: 'warning' }
    )

    let successCount = 0
    let failedCount = 0

    for (const archive of scannedArchives) {
      try {
        await confirmShelfComplete(archive)
        successCount++
      } catch (error) {
        failedCount++
        console.error(`档案 ${archive.danghao} 确认失败:`, error)
      }
    }

    if (failedCount > 0) {
      ElMessage.warning(`成功确认 ${successCount} 个，失败 ${failedCount} 个`)
    } else {
      ElMessage.success(`成功确认 ${successCount} 个档案上架完成`)
    }
  } catch (error) {
    // 用户取消
  }
}

// 同步下架数据到服务器
const syncUnshelfToServer = async (archive) => {
  try {
    // 准备更新的数据
    const updateData = {
      id: archive.id,
      rfid: archive.tempRfid || (archive.rfid.slice(0, -2) + '00'),
      availability: archive.availability, // 保持为1，等待后续流程处理
      shitiLocation: '',
      exactLocation: '',
    }
    await updateArchive(updateData)

    // 更新档案柜和储物格的数量
    await updateCabinetSize(archive.shitiLocation, 'delete')
    await updateCompartmentSize(archive.shitiLocation + '-' + archive.exactLocation, 'delete')

    let archiveInfo = '被借阅'
    if (archive.status === 'Destroyed') {
      archiveInfo = '销毁'
    }
    // 添加出库日志
    await addArchiveLog({
      archiveId: archive.id,
      taskType: '出库',
      initiator: userProfile.userName,
      remark: archiveInfo,
      startDate: dayjs().format('YYYY-MM-DD HH:mm:ss'),
    });

    // 更新本地数据
    archive.rfid = updateData.rfid

    // 更新 IndexedDB
    const cleanArchive = cleanArchiveForStorage(archive)
    const transaction = db.transaction([STORE_NAME], 'readwrite')
    const store = transaction.objectStore(STORE_NAME)
    await store.put(cleanArchive)

    // 模拟成功
    ElMessage.success('下架数据已同步到系统')

    // 从待下架列表移除
    const index = pendingUnshelfArchives.value.findIndex(a => a.id === archive.id)
    if (index > -1) {
      pendingUnshelfArchives.value.splice(index, 1)
    }
  } catch (error) {
    ElMessage.error('同步失败: ' + error.message)
  }
}

// 批量同步下架数据
const batchSyncUnshelf = async () => {
  const scannedArchives = pendingUnshelfArchives.value.filter(a => a.isScanned)
  if (scannedArchives.length === 0) {
    ElMessage.warning('没有已扫描的档案')
    return
  }

  try {
    await ElMessageBox.confirm(
      `确认将 ${scannedArchives.length} 个已扫描档案的下架数据同步到系统？`,
      '批量同步',
      { type: 'warning' }
    )

    for (const archive of scannedArchives) {
      await syncUnshelfToServer(archive)
    }

    ElMessage.success(`成功同步 ${scannedArchives.length} 个档案的下架数据`)
  } catch (error) {
    // 用户取消
  }
}

// 添加操作历史
const addOperationHistory = async (operation) => {
  try {
    const transaction = db.transaction([HISTORY_STORE], 'readwrite')
    const store = transaction.objectStore(HISTORY_STORE)
    await store.add(operation)

    // 重新加载历史
    await loadOperationHistory()
  } catch (error) {
    console.error('添加历史失败:', error)
  }
}

// 加载操作历史
const loadOperationHistory = async () => {
  try {
    const transaction = db.transaction([HISTORY_STORE], 'readonly')
    const store = transaction.objectStore(HISTORY_STORE)
    const index = store.index('time')
    const request = index.openCursor(null, 'prev') // 按时间倒序

    const history = []
    request.onsuccess = (event) => {
      const cursor = event.target.result
      if (cursor && history.length < 100) { // 只显示最近100条
        history.push(cursor.value)
        cursor.continue()
      } else {
        operationHistory.value = history
      }
    }
  } catch (error) {
    console.error('加载历史失败:', error)
  }
}

// 清空历史
const clearHistory = async () => {
  try {
    await ElMessageBox.confirm('确定清空所有操作历史？', '确认', { type: 'warning' })

    const transaction = db.transaction([HISTORY_STORE], 'readwrite')
    const store = transaction.objectStore(HISTORY_STORE)
    await store.clear()

    operationHistory.value = []
    ElMessage.success('操作历史已清空')
  } catch (error) {
    // 用户取消
  }
}

const startScanning = async () => {
  isStarting.value = true
  try {
    const response = await fetch('http://localhost:8080/rfid/start', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' }
    })

    if (!response.ok) throw new Error('启动扫描失败')

    const result = await response.json()
    console.log('启动扫描响应:', result)

    // API调用成功，但实际状态更新通过SSE事件处理
    // isScanning状态会在scan_started事件中更新

  } catch (error) {
    ElMessage.error('启动扫描失败: ' + error.message)
  } finally {
    // 无论成功还是失败都要重置loading状态
    // 注意：不要在这里重置isStarting，因为SSE事件处理会做
    // isStarting会在scan_started事件中被重置，或者如果失败则在这里重置
    setTimeout(() => {
      if (isStarting.value) {
        isStarting.value = false
      }
    }, 5000) // 5秒超时保护
  }
}

const stopScanning = async () => {
  isStopping.value = true
  try {
    const response = await fetch('http://localhost:8080/rfid/stop', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' }
    })

    if (!response.ok) throw new Error('停止扫描失败')

    const result = await response.json()
    console.log('停止扫描响应:', result)

    // API调用成功，但实际状态更新通过SSE事件处理
    // isScanning状态会在scan_stopped事件中更新

  } catch (error) {
    ElMessage.error('停止扫描失败: ' + error.message)
  } finally {
    // 超时保护，防止loading状态卡住
    setTimeout(() => {
      if (isStopping.value) {
        isStopping.value = false
      }
    }, 5000) // 5秒超时保护
  }
}


// 工具函数
const canShelf = (archive) => {
  return archive &&
    archive.exactLocation &&
    archive.rfid?.endsWith('00') && // 修正：待上架的应该是00结尾
    archive.availability === 2
}

const canUnshelf = (archive) => {
  return archive &&
    archive.rfid?.endsWith('05') && // 待下架的是05结尾
    archive.availability === 1
}

const getStatusText = (archive) => {
  if (archive.availability === 0) return '在架'
  if (archive.availability === 1) return '待下架'
  if (archive.availability === 2) return '待上架'
  return '未知'
}

const getStatusType = (archive) => {
  if (archive.availability === 0) return 'success'
  if (archive.availability === 1) return 'warning'
  if (archive.availability === 2) return 'info'
  return 'info'
}

const formatTime = (timestamp) => {
  return new Date(timestamp).toLocaleString('zh-CN')
}

// 计算属性
const pendingShelfCount = computed(() => pendingShelfArchives.value.length)
const pendingUnshelfCount = computed(() => pendingUnshelfArchives.value.length)
const cachedArchiveCount = computed(() => allArchives.value.length)
const completedTodayCount = computed(() => {
  const today = new Date().setHours(0, 0, 0, 0)
  return operationHistory.value.filter(op =>
    op.time >= today && op.status === 'success'
  ).length
})
const hasShelfCompleted = computed(() =>
  pendingShelfArchives.value.some(a => a.isScanned)
)
const hasUnshelfScanned = computed(() =>
  pendingUnshelfArchives.value.some(a => a.isScanned)
)

// 监听操作模式变化
const handleModeChange = () => {
  currentScannedTag.value = null
  matchedArchive.value = null
  activeTab.value = operationMode.value === 'shelf' ? 'pendingShelf' : 'pendingUnshelf'
}

// 生命周期
onMounted(async () => {
  await loadUserProfile()

  await initDB()
  await loadArchivesFromDB()
  await loadOperationHistory()
  connectSSE()
})

onUnmounted(() => {
  if (eventSource) {
    eventSource.close()
  }
})
</script>

<style scoped>
.shelf-management-container {
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
  gap: 12px;
  align-items: center;
}

.stats-overview {
  margin-bottom: 20px;
}

.stat-card {
  text-align: center;
}

.stat-item {
  padding: 20px;
}

.stat-value {
  font-size: 32px;
  font-weight: bold;
  color: #409eff;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 14px;
  color: #909399;
}

.scan-control {
  margin-bottom: 20px;
}

.control-section {
  margin-bottom: 20px;
}

.scan-buttons {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
}

.current-scan-info {
  margin-top: 20px;
}

.matched-archive-info {
  margin-top: 12px;
}

.quick-action {
  margin-top: 15px;
  text-align: center;
}

.pending-lists {
  background: white;
  padding: 20px;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, .1);
}

.tab-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  padding: 0 10px;
}

code {
  background: #f4f4f5;
  padding: 2px 6px;
  border-radius: 3px;
  font-family: 'Courier New', monospace;
  font-size: 12px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    gap: 10px;
  }

  .stats-overview .el-col {
    margin-bottom: 10px;
  }

  .control-section .el-col {
    margin-bottom: 10px;
  }

  .scan-buttons {
    justify-content: center;
  }
}
</style>