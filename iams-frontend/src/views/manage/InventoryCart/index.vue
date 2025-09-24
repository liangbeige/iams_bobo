<template>
  <div class="archive-inventory-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>RFID档案盘点系统</h2>
      <div class="header-actions">
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
        <!-- 数据管理卡片 -->
        <el-card class="control-card" shadow="hover">
          <template #header>
            <span>数据管理</span>
          </template>

          <div class="data-actions">
            <el-button
                type="primary"
                icon="Download"
                :loading="isLoadingArchives"
                :disabled="networkStatus !== 'online'"
                @click="loadArchivesFromServer"
            >
              从服务器加载档案数据
            </el-button>

            <el-button
                type="warning"
                icon="Upload"
                :disabled="networkStatus !== 'online' || inventoryData.length === 0"
                @click="uploadInventoryData"
            >
              上传盘点结果
            </el-button>

            <el-button
                type="success"
                icon="Document"
                :disabled="inventoryData.length === 0"
                @click="exportInventoryResults"
            >
              导出盘点结果
            </el-button>

            <el-button
                type="danger"
                icon="Delete"
                @click="clearLocalData"
            >
              清空本地数据
            </el-button>
          </div>

          <el-divider></el-divider>

          <el-descriptions :column="1" border size="small">
            <el-descriptions-item label="缓存档案数">
              <el-tag type="info">{{ cachedArchives.length }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="在架档案数">
              <el-tag type="success">{{ onShelfArchivesCount }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="当前层架">
              <el-tag v-if="currentShelf" type="primary">{{ currentShelf.location }}-{{ currentShelf.sectionNumber }}</el-tag>
              <span v-else>未选择</span>
            </el-descriptions-item>
            <el-descriptions-item label="已盘点档案">
              <el-tag type="success">{{ inventoryData.length }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="状态异常">
              <el-tag :type="statusAnomalyCount > 0 ? 'danger' : 'success'">
                {{ statusAnomalyCount }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="本地存储状态">
              <el-tag :type="localStorageStatus ? 'success' : 'danger'">
                {{ localStorageStatus ? '正常' : '异常' }}
              </el-tag>
            </el-descriptions-item>
          </el-descriptions>
        </el-card>

        <!-- 扫描控制卡片 -->
        <el-card class="control-card" shadow="hover">
          <template #header>
            <span>扫描控制</span>
          </template>

          <!-- RFID连接控制 -->
          <div class="connection-control">
            <el-button
                v-if="connectionStatus !== 'connected'"
                type="primary"
                icon="Link"
                :loading="isConnecting"
                @click="connectRFIDDevice"
                style="width: 100%;"
            >
              连接RFID设备
            </el-button>
            <el-button
                v-else
                type="danger"
                icon="LinkOff"
                @click="disconnectRFIDDevice"
                style="width: 100%;"
            >
              断开RFID设备
            </el-button>
          </div>

          <el-divider></el-divider>

          <div class="scan-mode">
            <el-radio-group v-model="scanMode" :disabled="isScanning || connectionStatus !== 'connected'">
              <el-radio-button label="shelf">扫描层架签</el-radio-button>
              <el-radio-button label="archive">扫描档案</el-radio-button>
            </el-radio-group>
          </div>

          <div class="scan-actions">
            <el-button
                type="primary"
                :disabled="isScanning || connectionStatus !== 'connected'"
                :loading="isStarting"
                @click="startScanning"
                icon="VideoPlay"
            >
              开始扫描
            </el-button>

            <el-button
                type="danger"
                :disabled="!isScanning"
                :loading="isStopping"
                @click="stopScanning"
                icon="VideoPause"
            >
              停止扫描
            </el-button>
          </div>

          <el-alert
              v-if="scanMode === 'shelf'"
              title="层架扫描模式"
              type="info"
              :closable="false"
              show-icon
          >
            扫描层架签以筛选该位置的在架档案
          </el-alert>

          <el-alert
              v-else
              title="档案扫描模式"
              type="success"
              :closable="false"
              show-icon
          >
            扫描档案RFID标签进行盘点，自动检测状态异常
          </el-alert>
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
              <el-descriptions-item label="类型">
                <el-tag :type="latestScan.type === 'shelf' ? 'warning' : 'success'">
                  {{ latestScan.type === 'shelf' ? '层架签' : '档案' }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="时间">
                {{ formatTime(latestScan.timestamp) }}
              </el-descriptions-item>
              <el-descriptions-item v-if="latestScan.archive" label="档案名称">
                {{ latestScan.archive.name }}
              </el-descriptions-item>
              <el-descriptions-item v-if="latestScan.statusInfo" label="RFID状态">
                <el-tag :type="latestScan.statusInfo.isOnShelf ? 'success' : 'danger'">
                  {{ latestScan.statusInfo.isOnShelf ? '在架' : '不在架' }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item v-if="latestScan.statusAnomaly" label="状态异常">
                <el-tag type="danger">物理在架但系统显示不在架</el-tag>
              </el-descriptions-item>
            </el-descriptions>
          </div>

          <el-empty v-else description="暂无扫描数据" :image-size="60" />
        </el-card>
      </el-col>

      <!-- 右侧数据展示区 -->
      <el-col :span="16">
        <!-- 当前层架档案列表 -->
        <el-card class="archive-list" shadow="hover">
          <template #header>
            <div class="list-header">
              <span>档案列表</span>
              <div class="header-controls">
                <el-switch
                    v-model="showOffShelfArchives"
                    active-text="显示不在架"
                    inactive-text="仅在架"
                    @change="updateFilteredArchives"
                />
                <el-input
                    v-model="searchText"
                    placeholder="搜索档号或名称"
                    style="width: 200px; margin-left: 10px"
                    clearable
                    @input="handleSearch"
                >
                  <template #prefix>
                    <el-icon><Search /></el-icon>
                  </template>
                </el-input>
              </div>
            </div>
          </template>

          <el-table
              :key="tableKey"
              :data="paginatedArchives"
              height="600"
              size="small"
              stripe
              :row-class-name="getRowClassName"
              @row-click="handleArchiveClick"
          >
            <el-table-column type="index" width="50" label="序号" />
            <el-table-column prop="danghao" label="档号" width="120" />
            <el-table-column prop="name" label="档案名称" min-width="200" show-overflow-tooltip />
            <el-table-column prop="rfid" label="RFID" width="180">
              <template #default="{ row }">
                <code class="rfid-code">{{ row.rfid }}</code>
              </template>
            </el-table-column>
            <el-table-column prop="shitiLocation" label="位置" width="100" />
            <el-table-column prop="exactLocation" label="详细位置" width="80" />
            <el-table-column label="RFID状态" width="80">
              <template #default="{ row }">
                <el-tag :type="parseRfidStatus(row.rfid).isOnShelf ? 'success' : 'danger'" size="small">
                  {{ parseRfidStatus(row.rfid).isOnShelf ? '在架' : '不在架' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="盘点状态" width="100" align="center">
              <template #default="{ row }">
                <el-tag v-if="isArchiveScanned(row.rfid)" type="success" effect="dark">
                  已盘点
                </el-tag>
                <el-tag v-else type="info">未盘点</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="状态异常" width="80" align="center">
              <template #default="{ row }">
                <el-tag v-if="hasStatusAnomaly(row.rfid)" type="danger" size="small">
                  异常
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120" fixed="right">
              <template #default="{ row }">
                <el-button
                    v-if="!isArchiveScanned(row.rfid)"
                    size="small"
                    type="text"
                    @click="manualInventory(row)"
                >
                  手动盘点
                </el-button>
                <el-button
                    v-else
                    size="small"
                    type="text"
                    @click="cancelInventory(row)"
                >
                  取消盘点
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <div class="pagination-wrapper">
            <el-pagination
                v-model:current-page="currentPage"
                v-model:page-size="pageSize"
                :page-sizes="[20, 50, 100, 200]"
                :total="filteredArchives.length"
                layout="total, sizes, prev, pager, next"
                small
            />
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 盘点统计区 -->
    <el-card class="statistics-card" shadow="hover">
      <template #header>
        <span>盘点统计</span>
      </template>

      <el-row :gutter="20">
        <el-col :span="5">
          <div class="stat-item">
            <div class="stat-value">{{ currentShelfArchives.length }}</div>
            <div class="stat-label">当前层架档案数</div>
          </div>
        </el-col>
        <el-col :span="5">
          <div class="stat-item">
            <div class="stat-value">{{ currentShelfOnShelfCount }}</div>
            <div class="stat-label">应在架数</div>
          </div>
        </el-col>
        <el-col :span="5">
          <div class="stat-item">
            <div class="stat-value">{{ currentShelfScannedCount }}</div>
            <div class="stat-label">已盘点数</div>
          </div>
        </el-col>
        <el-col :span="4">
          <div class="stat-item">
            <div class="stat-value">{{ currentShelfProgress }}%</div>
            <div class="stat-label">盘点进度</div>
          </div>
        </el-col>
        <el-col :span="5">
          <div class="stat-item" :class="{ 'stat-danger': currentShelfAnomalyCount > 0 }">
            <div class="stat-value">{{ currentShelfAnomalyCount }}</div>
            <div class="stat-label">状态异常数</div>
          </div>
        </el-col>
      </el-row>

      <el-divider></el-divider>

      <!-- 状态异常列表 -->
      <div v-if="statusAnomalies.length > 0" class="anomaly-section">
        <h4>状态异常档案</h4>
        <el-table :data="statusAnomalies" size="small" max-height="200">
          <el-table-column prop="danghao" label="档号" width="120" />
          <el-table-column prop="archiveName" label="档案名称" min-width="200" show-overflow-tooltip />
          <el-table-column prop="rfid" label="RFID" width="180">
            <template #default="{ row }">
              <code class="rfid-code">{{ row.rfid }}</code>
            </template>
          </el-table-column>
          <el-table-column label="问题描述" min-width="200">
            <template #default="{ row }">
              <el-tag type="danger" size="small">
                物理在架但系统显示不在架
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="timestamp" label="发现时间" width="160">
            <template #default="{ row }">
              {{ formatTime(row.timestamp) }}
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 盘点历史 -->
      <div class="inventory-history">
        <h4>盘点历史</h4>
        <el-timeline>
          <el-timeline-item
              v-for="(shelf, index) in inventoryHistory"
              :key="index"
              :timestamp="formatTime(shelf.timestamp)"
              placement="top"
          >
            <el-card>
              <h4>{{ shelf.location }}</h4>
              <p>应在架: {{ shelf.onShelfCount }}，已盘点: {{ shelf.scannedCount }}，异常: {{ shelf.anomalyCount }}</p>
              <el-progress
                  :percentage="shelf.progress"
                  :status="shelf.progress === 100 ? 'success' : ''"
              />
              <div v-if="shelf.anomalyCount > 0" style="margin-top: 8px;">
                <el-tag type="danger" size="small">发现 {{ shelf.anomalyCount }} 个状态异常</el-tag>
              </div>
            </el-card>
          </el-timeline-item>
        </el-timeline>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted, watch } from 'vue'
import { ElMessage, ElMessageBox, ElLoading } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import {uploadInventoryResult, exportInventoryResult } from '@/api/manage/archive' // 根据实际路径调整
import {listArchiveAll} from '@/api/manage/archive'

// 响应式数据
const connectionStatus = ref('disconnected')
const networkStatus = ref('online')
const isScanning = ref(false)
const isStarting = ref(false)
const isStopping = ref(false)
const isLoadingArchives = ref(false)
const localStorageStatus = ref(false)
const isConnecting = ref(false) // 新增：连接中状态

// 档案相关数据
const cachedArchives = ref([])
const currentShelf = ref(null)
const currentShelfArchives = ref([])
const inventoryData = ref([]) // 所有已盘点的档案
const statusAnomalies = ref([]) // 状态异常记录
const inventoryHistory = ref([]) // 层架盘点历史
const searchText = ref('')
const filteredArchives = ref([])
const tableKey = ref(0) // 用于强制刷新表格
const showOffShelfArchives = ref(false) // 是否显示不在架档案

// 扫描相关
const scanMode = ref('shelf') // shelf or archive
const latestScan = ref(null)

// 分页
const currentPage = ref(1)
const pageSize = ref(50)

// SSE连接
let eventSource = null
const clientId = 'archive_inventory_' + Date.now()
let reconnectTimer = null // 重连定时器

// IndexedDB配置
const DB_NAME = 'ArchiveInventoryDB'
const DB_VERSION = 2 // 升级版本以支持新的数据结构
const STORE_NAME = 'archives'
const INVENTORY_STORE = 'inventory'
const ANOMALY_STORE = 'anomalies' // 新增异常记录存储
let db = null

// 解析RFID状态
const parseRfidStatus = (rfid) => {
  if (!rfid || rfid.length < 2) {
    return { isOnShelf: false, statusCode: '00' }
  }

  const statusCode = rfid.slice(-2).toUpperCase()
  return {
    isOnShelf: statusCode === '05',
    statusCode: statusCode
  }
}

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

      // 创建档案存储
      if (!db.objectStoreNames.contains(STORE_NAME)) {
        const archiveStore = db.createObjectStore(STORE_NAME, { keyPath: 'id' })
        archiveStore.createIndex('rfid', 'rfid', { unique: true })
        archiveStore.createIndex('shitiLocation', 'shitiLocation', { unique: false })
      }

      // 创建盘点数据存储
      if (!db.objectStoreNames.contains(INVENTORY_STORE)) {
        const inventoryStore = db.createObjectStore(INVENTORY_STORE, { keyPath: 'rfid' })
        inventoryStore.createIndex('timestamp', 'timestamp', { unique: false })
      }

      // 创建异常记录存储
      if (!db.objectStoreNames.contains(ANOMALY_STORE)) {
        const anomalyStore = db.createObjectStore(ANOMALY_STORE, { keyPath: 'rfid' })
        anomalyStore.createIndex('timestamp', 'timestamp', { unique: false })
      }
    }
  })
}

// 保存档案到IndexedDB
const saveArchivesToDB = async (archives) => {
  if (!db) return

  const transaction = db.transaction([STORE_NAME], 'readwrite')
  const store = transaction.objectStore(STORE_NAME)

  // 清空现有数据
  await store.clear()

  // 批量保存
  archives.forEach(archive => {
    store.add(archive)
  })

  return new Promise((resolve, reject) => {
    transaction.oncomplete = () => resolve()
    transaction.onerror = () => reject(transaction.error)
  })
}

// 从IndexedDB加载档案
const loadArchivesFromDB = () => {
  if (!db) return Promise.resolve([])

  return new Promise((resolve, reject) => {
    const transaction = db.transaction([STORE_NAME], 'readonly')
    const store = transaction.objectStore(STORE_NAME)
    const request = store.getAll()

    request.onsuccess = () => {
      resolve(request.result || [])
    }

    request.onerror = () => {
      reject(request.error)
    }
  })
}

// 保存盘点数据到IndexedDB
const saveInventoryToDB = async (inventoryItem) => {
  if (!db) return

  const transaction = db.transaction([INVENTORY_STORE], 'readwrite')
  const store = transaction.objectStore(INVENTORY_STORE)

  store.put(inventoryItem)

  return new Promise((resolve, reject) => {
    transaction.oncomplete = () => resolve()
    transaction.onerror = () => reject(transaction.error)
  })
}

// 保存异常记录到IndexedDB
const saveAnomalyToDB = async (anomalyItem) => {
  if (!db) return

  const transaction = db.transaction([ANOMALY_STORE], 'readwrite')
  const store = transaction.objectStore(ANOMALY_STORE)

  store.put(anomalyItem)

  return new Promise((resolve, reject) => {
    transaction.oncomplete = () => resolve()
    transaction.onerror = () => reject(transaction.error)
  })
}

// 加载盘点数据
const loadInventoryFromDB = () => {
  if (!db) return Promise.resolve([])

  return new Promise((resolve, reject) => {
    const transaction = db.transaction([INVENTORY_STORE], 'readonly')
    const store = transaction.objectStore(INVENTORY_STORE)
    const request = store.getAll()

    request.onsuccess = () => {
      resolve(request.result || [])
    }

    request.onerror = () => {
      reject(request.error)
    }
  })
}

// 加载异常记录
const loadAnomaliesFromDB = () => {
  if (!db) return Promise.resolve([])

  return new Promise((resolve, reject) => {
    const transaction = db.transaction([ANOMALY_STORE], 'readonly')
    const store = transaction.objectStore(ANOMALY_STORE)
    const request = store.getAll()

    request.onsuccess = () => {
      resolve(request.result || [])
    }

    request.onerror = () => {
      reject(request.error)
    }
  })
}

// 导出盘点结果
const exportInventoryResults = () => {
  if (inventoryData.value.length === 0) {
    ElMessage.warning('没有数据可导出')
    return
  }

  // 准备CSV数据
  const csvContent = [
    '档号,档案名称,RFID,位置,详细位置,盘点时间,天线,信号强度,RFID状态,状态异常',
    ...inventoryData.value.map(item => {
      const statusInfo = parseRfidStatus(item.rfid)
      const hasAnomaly = hasStatusAnomaly(item.rfid)
      return `${item.danghao},${item.archiveName},${item.rfid},${item.shitiLocation},${item.exactLocation},${formatTime(item.timestamp)},${item.antenna},${item.rssi},${statusInfo.isOnShelf ? '在架' : '不在架'},${hasAnomaly ? '是' : '否'}`
    })
  ].join('\n')

  // 添加BOM以支持中文
  const BOM = '\uFEFF'
  const blob = new Blob([BOM + csvContent], { type: 'text/csv;charset=utf-8;' })
  const link = document.createElement('a')
  link.href = URL.createObjectURL(blob)
  link.download = `档案盘点结果_${new Date().toISOString().slice(0, 19).replace(/:/g, '-')}.csv`
  link.click()

  ElMessage.success('导出成功')
}

// 从服务器加载档案数据
const loadArchivesFromServer = async () => {
  isLoadingArchives.value = true
  try {
    const response = await listArchiveAll()
    const archives = response.rows || []

    // 保存到IndexedDB
    await saveArchivesToDB(archives)

    // 更新内存中的数据
    cachedArchives.value = archives
    updateFilteredArchives()

    ElMessage.success(`成功加载 ${archives.length} 条档案数据`)
  } catch (error) {
    console.error('加载档案失败:', error)
    ElMessage.error('加载档案数据失败，请检查网络连接')
  } finally {
    isLoadingArchives.value = false
  }
}

// 上传盘点结果
const uploadInventoryData = async () => {
  if (inventoryData.value.length === 0) {
    ElMessage.warning('没有盘点数据需要上传')
    return
  }

  const anomalyCount = statusAnomalies.value.length

  ElMessageBox.confirm(
      `确定要上传 ${inventoryData.value.length} 条盘点记录吗？${anomalyCount > 0 ? `\n包含 ${anomalyCount} 个状态异常记录` : ''}`,
      '确认上传',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: anomalyCount > 0 ? 'warning' : 'info'
      }
  ).then(async () => {
    try {
      const loading = ElLoading.service({
        lock: true,
        text: '正在上传盘点数据...',
        background: 'rgba(0, 0, 0, 0.7)'
      })

      // 准备上传数据
      const uploadData = {
        inventoryTime: new Date().getTime(), // 使用时间戳而不是ISO字符串
        totalCount: cachedArchives.value.length,
        onShelfCount: onShelfArchivesCount.value,
        scannedCount: inventoryData.value.length,
        anomalyCount: statusAnomalies.value.length,
        records: inventoryData.value.map(item => ({
          archiveId: item.archiveId,
          rfid: item.rfid,
          danghao: item.danghao,
          scanTime: item.timestamp, // 直接使用时间戳
          antenna: item.antenna,
          rssi: item.rssi,
          shitiLocation: item.shitiLocation,
          exactLocation: item.exactLocation,
          rfidStatus: parseRfidStatus(item.rfid).statusCode,
          statusAnomaly: hasStatusAnomaly(item.rfid),
          manual: item.manual
        })),
        anomalies: statusAnomalies.value.map(anomaly => ({
          archiveId: anomaly.archiveId,
          rfid: anomaly.rfid,
          danghao: anomaly.danghao,
          foundTime: anomaly.timestamp, // 直接使用时间戳
          problemDescription: anomaly.problemDescription
        }))
      }

      const response = await uploadInventoryResult(uploadData)

      loading.close()

      if (response.code === 200) {
        ElMessage.success('盘点数据上传成功')

        // 清空本地盘点数据
        if (db) {
          const transaction = db.transaction([INVENTORY_STORE, ANOMALY_STORE], 'readwrite')
          transaction.objectStore(INVENTORY_STORE).clear()
          transaction.objectStore(ANOMALY_STORE).clear()
        }
        inventoryData.value = []
        statusAnomalies.value = []
        inventoryHistory.value = []

      } else {
        ElMessage.error(response.msg || '上传失败')
      }
    } catch (error) {
      console.error('上传失败:', error)
      ElMessage.error('上传失败: ' + error.message)
    }
  })
}

// 清空本地数据
const clearLocalData = () => {
  ElMessageBox.confirm('确定要清空所有本地数据吗？包括缓存的档案和盘点记录', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    if (db) {
      const transaction = db.transaction([STORE_NAME, INVENTORY_STORE, ANOMALY_STORE], 'readwrite')
      transaction.objectStore(STORE_NAME).clear()
      transaction.objectStore(INVENTORY_STORE).clear()
      transaction.objectStore(ANOMALY_STORE).clear()
    }

    cachedArchives.value = []
    inventoryData.value = []
    statusAnomalies.value = []
    inventoryHistory.value = []
    currentShelf.value = null
    currentShelfArchives.value = []
    filteredArchives.value = []

    ElMessage.success('本地数据已清空')
  })
}

// 解析层架签EPC
const parseShelfEpc = (epc) => {
  // EPC格式: 0101A01FFFFFFFFFFFFFFFFFF
  // 0101A -> shitiLocation (1-1-A)
  // 01 -> 节号（对应exactLocation的第一位）

  const pattern = /^(\d{2})(\d{2})([A-Z])(\d{2})(\d{2})/
  const match = epc.match(pattern)

  if (match) {
    const [, row1, row2, col, shelfSection, layer] = match
    const location = `${parseInt(row1)}-${parseInt(row2)}-${col}`
    const sectionNumber = `${parseInt(shelfSection)}-${parseInt(layer)}`// 将 "01" 转换为 "1"

    return {
      location,
      sectionNumber,  // 节号-层号
      original: epc
    }
  }

  return null
}

// 处理层架签扫描
const handleShelfScan = (epc) => {
  const shelfInfo = parseShelfEpc(epc)

  if (!shelfInfo) {
    ElMessage.warning('无法识别的层架签格式')
    return
  }

  currentShelf.value = shelfInfo

  // 筛选该层架的档案，只显示在架的档案
  const allShelfArchives = cachedArchives.value.filter(archive => {
    return archive.shitiLocation === shelfInfo.location &&
        archive.exactLocation=== shelfInfo.sectionNumber
  })

  // 按照开关状态决定是否只显示在架档案
  currentShelfArchives.value = showOffShelfArchives.value
      ? allShelfArchives
      : allShelfArchives.filter(archive => parseRfidStatus(archive.rfid).isOnShelf)

  updateFilteredArchives()

  const onShelfCount = allShelfArchives.filter(archive => parseRfidStatus(archive.rfid).isOnShelf).length
  const totalCount = allShelfArchives.length

  ElMessage.success(`已切换到层架: ${shelfInfo.location}-${shelfInfo.sectionNumber}，在架档案: ${onShelfCount}/${totalCount}`)

  // 自动切换到档案扫描模式
  scanMode.value = 'archive'
}

// 处理档案扫描
const handleArchiveScan = async (epc, tagInfo) => {
  // 解析RFID状态
  const statusInfo = parseRfidStatus(epc)

  // 查找对应的档案
  const archive = cachedArchives.value.find(a => a.rfid === epc)

  if (!archive) {
    ElMessage.warning('未找到对应的档案信息')
    return
  }
  // 检查是否属于当前层架
  if (currentShelf.value) {
    const belongsToCurrentShelf = archive.shitiLocation === currentShelf.value.location &&
        archive.exactLocation === currentShelf.value.sectionNumber

    if (!belongsToCurrentShelf) {
      ElMessage.warning(`注意：档案 "${archive.name}" 属于 ${archive.shitiLocation}-${archive.exactLocation}，不属于当前层架`)
    }
  }

  // 检查状态异常：物理在架但数据库显示不在架
  let statusAnomaly = false
  if (!statusInfo.isOnShelf) {
    statusAnomaly = true

    // 记录状态异常
    const anomalyRecord = {
      rfid: epc,
      archiveId: archive.id,
      danghao: archive.danghao,
      archiveName: archive.name,
      shitiLocation: archive.shitiLocation,
      exactLocation: archive.exactLocation,
      problemDescription: '物理在架但系统显示不在架',
      timestamp: Date.now(),
      shelfEpc: currentShelf.value?.original
    }

    // 检查是否已经记录过此异常
    const existingAnomaly = statusAnomalies.value.find(item => item.rfid === epc)
    if (!existingAnomaly) {
      statusAnomalies.value.push(anomalyRecord)
      await saveAnomalyToDB(anomalyRecord)
    }

    ElMessage.warning(`状态异常：档案 ${archive.name} 物理在架但系统显示不在架`)
  }

  // 检查是否已盘点
  const existing = inventoryData.value.find(item => item.rfid === epc)
  if (existing) {
    ElMessage.info(`档案 ${archive.name} 已盘点`)
    return
  }

  // 添加到盘点数据
  const inventoryItem = {
    rfid: epc,
    archiveId: archive.id,
    archiveName: archive.name,
    danghao: archive.danghao,
    shitiLocation: archive.shitiLocation,
    exactLocation: archive.exactLocation,
    timestamp: Date.now(),
    shelfEpc: currentShelf.value?.original,
    statusAnomaly: statusAnomaly,
    ...tagInfo
  }

  inventoryData.value.push(inventoryItem)
  await saveInventoryToDB(inventoryItem)

  // 更新表格显示
  updateFilteredArchives()
  tableKey.value++

  if (statusAnomaly) {
    ElMessage.success(`已盘点: ${archive.name} (状态异常)`)
  } else {
    ElMessage.success(`已盘点: ${archive.name}`)
  }
}

// 手动盘点档案
const manualInventory = async (archive) => {
  const statusInfo = parseRfidStatus(archive.rfid)

  let confirmMessage = `确定要手动盘点档案 "${archive.name}" 吗？`
  if (!statusInfo.isOnShelf) {
    confirmMessage += '\n\n注意：该档案RFID状态显示为"不在架"'
  }

  ElMessageBox.confirm(confirmMessage, '确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: statusInfo.isOnShelf ? 'info' : 'warning'
  }).then(async () => {
    const inventoryItem = {
      rfid: archive.rfid,
      archiveId: archive.id,
      archiveName: archive.name,
      danghao: archive.danghao,
      shitiLocation: archive.shitiLocation,
      exactLocation: archive.exactLocation,
      timestamp: Date.now(),
      shelfEpc: currentShelf.value?.original,
      antenna: 'Manual',
      rssi: '0',
      manual: true,
      statusAnomaly: !statusInfo.isOnShelf
    }

    inventoryData.value.push(inventoryItem)
    await saveInventoryToDB(inventoryItem)

    // 如果是状态异常，记录异常信息
    if (!statusInfo.isOnShelf) {
      const anomalyRecord = {
        rfid: archive.rfid,
        archiveId: archive.id,
        danghao: archive.danghao,
        archiveName: archive.name,
        shitiLocation: archive.shitiLocation,
        exactLocation: archive.exactLocation,
        problemDescription: '手动盘点时发现系统显示不在架',
        timestamp: Date.now(),
        shelfEpc: currentShelf.value?.original
      }

      const existingAnomaly = statusAnomalies.value.find(item => item.rfid === archive.rfid)
      if (!existingAnomaly) {
        statusAnomalies.value.push(anomalyRecord)
        await saveAnomalyToDB(anomalyRecord)
      }
    }

    // 强制更新视图
    updateFilteredArchives()
    tableKey.value++

    ElMessage.success(`已手动盘点: ${archive.name}`)
  })
}

// 取消盘点
const cancelInventory = async (archive) => {
  ElMessageBox.confirm(
      `确定要取消档案 "${archive.name}" 的盘点记录吗？`,
      '确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
  ).then(async () => {
    // 从盘点数据中移除
    const index = inventoryData.value.findIndex(item => item.rfid === archive.rfid)
    if (index > -1) {
      inventoryData.value.splice(index, 1)

      // 从IndexedDB中删除
      if (db) {
        const transaction = db.transaction([INVENTORY_STORE], 'readwrite')
        const store = transaction.objectStore(INVENTORY_STORE)
        store.delete(archive.rfid)
      }

      // 同时移除相关的异常记录
      const anomalyIndex = statusAnomalies.value.findIndex(item => item.rfid === archive.rfid)
      if (anomalyIndex > -1) {
        statusAnomalies.value.splice(anomalyIndex, 1)
        if (db) {
          const transaction = db.transaction([ANOMALY_STORE], 'readwrite')
          const store = transaction.objectStore(ANOMALY_STORE)
          store.delete(archive.rfid)
        }
      }

      // 强制更新视图
      updateFilteredArchives()
      tableKey.value++

      ElMessage.success('已取消盘点记录')
    }
  })
}

// 检查档案是否已盘点
const isArchiveScanned = (rfid) => {
  return inventoryData.value.some(item => item.rfid === rfid)
}

// 检查档案是否有状态异常
const hasStatusAnomaly = (rfid) => {
  return statusAnomalies.value.some(item => item.rfid === rfid)
}

// 更新筛选后的档案列表
const updateFilteredArchives = () => {
  let archives = currentShelf.value ? currentShelfArchives.value : cachedArchives.value

  // 根据开关状态过滤
  if (!showOffShelfArchives.value) {
    archives = archives.filter(archive => parseRfidStatus(archive.rfid).isOnShelf)
  }

  if (searchText.value) {
    const search = searchText.value.toLowerCase()
    archives = archives.filter(archive =>
        archive.danghao?.toLowerCase().includes(search) ||
        archive.name?.toLowerCase().includes(search) ||
        archive.rfid?.toLowerCase().includes(search)
    )
  }

  filteredArchives.value = archives
  tableKey.value++ // 强制刷新表格
}

// 处理搜索
const handleSearch = () => {
  updateFilteredArchives()
  currentPage.value = 1
}

// 处理档案行点击
const handleArchiveClick = (row) => {
  const statusInfo = parseRfidStatus(row.rfid)
  const hasAnomaly = hasStatusAnomaly(row.rfid)

  ElMessageBox({
    title: '档案详情',
    message: `
      <div style="text-align: left;">
        <p><strong>档号:</strong> ${row.danghao}</p>
        <p><strong>名称:</strong> ${row.name}</p>
        <p><strong>RFID:</strong> ${row.rfid}</p>
        <p><strong>位置:</strong> ${row.shitiLocation} - ${row.exactLocation}</p>
        <p><strong>RFID状态:</strong> <span style="color: ${statusInfo.isOnShelf ? 'green' : 'red'}">${statusInfo.isOnShelf ? '在架' : '不在架'}</span></p>
        <p><strong>盘点状态:</strong> ${isArchiveScanned(row.rfid) ? '已盘点' : '未盘点'}</p>
        ${hasAnomaly ? '<p><strong>状态异常:</strong> <span style="color: red">是</span></p>' : ''}
      </div>
    `,
    dangerouslyUseHTMLString: true,
    confirmButtonText: '确定'
  })
}

// 获取行样式
const getRowClassName = ({row}) => {
  const classes = []
  if (isArchiveScanned(row.rfid)) {
    classes.push('scanned-row')
  }
  if (hasStatusAnomaly(row.rfid)) {
    classes.push('anomaly-row')
  }
  if (!parseRfidStatus(row.rfid).isOnShelf) {
    classes.push('off-shelf-row')
  }
  return classes.join(' ')
}

// 格式化时间
const formatTime = (timestamp) => {
  return new Date(timestamp).toLocaleString('zh-CN')
}

// 连接RFID设备（新增）
const connectRFIDDevice = () => {
  isConnecting.value = true

  ElMessage.info('正在连接RFID设备...')

  // 清理可能存在的重连定时器
  if (reconnectTimer) {
    clearTimeout(reconnectTimer)
    reconnectTimer = null
  }

  connectSSE()
}

// 断开RFID设备（新增）
const disconnectRFIDDevice = () => {
  ElMessageBox.confirm(
      '确定要断开RFID设备连接吗？断开后将无法进行扫描操作。',
      '确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
  ).then(() => {
    // 如果正在扫描，先停止扫描
    if (isScanning.value) {
      stopScanning()
    }

    // 关闭SSE连接
    if (eventSource) {
      eventSource.close()
      eventSource = null
    }

    // 清理重连定时器
    if (reconnectTimer) {
      clearTimeout(reconnectTimer)
      reconnectTimer = null
    }

    // 更新状态
    connectionStatus.value = 'disconnected'
    isConnecting.value = false

    ElMessage.success('已断开RFID设备连接')
  })
}

// 连接SSE（修改版，移除自动重连）
const connectSSE = () => {
  try {
    eventSource = new EventSource(`http://localhost:8080/rfid/connect?clientId=${clientId}`)

    eventSource.addEventListener('connected', () => {
      connectionStatus.value = 'connected'
      isConnecting.value = false
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

      // 更新盘点历史
      if (currentShelf.value && currentShelfScannedCount.value > 0) {
        inventoryHistory.value.unshift({
          location: `${currentShelf.value.location}-${currentShelf.value.sectionNumber}`,
          onShelfCount: currentShelfOnShelfCount.value,
          scannedCount: currentShelfScannedCount.value,  // 基于实际位置计算的
          anomalyCount: currentShelfAnomalyCount.value,  // 基于实际位置计算的
          progress: currentShelfProgress.value,
          timestamp: Date.now()
        })
      }
    })

    eventSource.addEventListener('tag_data', (event) => {
      const tagInfo = JSON.parse(event.data)
      const epc = tagInfo.epc

      latestScan.value = {
        epc,
        type: scanMode.value,
        timestamp: tagInfo.timestamp,
        statusInfo: parseRfidStatus(epc),
        ...tagInfo
      }

      if (scanMode.value === 'shelf') {
        handleShelfScan(epc)
      } else {
        const archive = cachedArchives.value.find(a => a.rfid === epc)
        if (archive) {
          latestScan.value.archive = archive
          latestScan.value.statusAnomaly = !parseRfidStatus(epc).isOnShelf
        }
        handleArchiveScan(epc, tagInfo)
      }
    })

    eventSource.addEventListener('error', (event) => {
      const errorInfo = JSON.parse(event.data)
      ElMessage.error('错误: ' + errorInfo.error)
    })

    eventSource.onerror = () => {
      connectionStatus.value = 'disconnected'
      isConnecting.value = false

      // 不再自动重连，只提示用户
      ElMessage.error('RFID设备连接中断，请点击"连接RFID设备"按钮重新连接')
    }
  } catch (error) {
    console.error('建立SSE连接失败:', error)
    connectionStatus.value = 'disconnected'
    isConnecting.value = false
    ElMessage.error('无法连接到RFID服务，请确保设备已开启')
  }
}

// 开始扫描
const startScanning = async () => {
  if (!currentShelf.value && scanMode.value === 'archive') {
    ElMessage.warning('请先扫描层架签')
    return
  }

  isStarting.value = true
  try {
    const response = await fetch('http://localhost:8080/rfid/start', {
      method: 'POST',
      headers: {'Content-Type': 'application/json'}
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
      headers: {'Content-Type': 'application/json'}
    })

    if (!response.ok) {
      throw new Error('停止扫描失败')
    }
  } catch (error) {
    isStopping.value = false
    ElMessage.error('停止扫描失败: ' + error.message)
  }
}

// 监听网络状态
const updateNetworkStatus = () => {
  networkStatus.value = navigator.onLine ? 'online' : 'offline'
}

// 监听搜索文本变化
watch(searchText, () => {
  updateFilteredArchives()
})

// 计算属性
const paginatedArchives = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredArchives.value.slice(start, end)
})

const onShelfArchivesCount = computed(() => {
  return cachedArchives.value.filter(archive => parseRfidStatus(archive.rfid).isOnShelf).length
})

const statusAnomalyCount = computed(() => {
  return statusAnomalies.value.length
})

const currentShelfOnShelfCount = computed(() => {
  if (!currentShelf.value) return 0

  return currentShelfArchives.value.filter(archive => parseRfidStatus(archive.rfid).isOnShelf).length
})

const currentShelfScannedCount = computed(() => {
  if (!currentShelf.value) return 0

  // 改为基于档案实际位置判断
  return inventoryData.value.filter(item => {
    // 通过RFID找到对应的档案信息
    const archive = cachedArchives.value.find(a => a.rfid === item.rfid)
    if (!archive) return false

    // 判断档案是否属于当前层架
    return archive.shitiLocation === currentShelf.value.location &&
        archive.exactLocation === currentShelf.value.sectionNumber
  }).length
})

const currentShelfAnomalyCount = computed(() => {
  if (!currentShelf.value) return 0

  // 同样改为基于档案实际位置判断
  return statusAnomalies.value.filter(item => {
    // 通过RFID找到对应的档案信息
    const archive = cachedArchives.value.find(a => a.rfid === item.rfid)
    if (!archive) return false

    // 判断档案是否属于当前层架
    return archive.shitiLocation === currentShelf.value.location &&
        archive.exactLocation === currentShelf.value.sectionNumber
  }).length
})

const currentShelfProgress = computed(() => {
  if (!currentShelf.value || currentShelfOnShelfCount.value === 0) return 0

  return Math.round((currentShelfScannedCount.value / currentShelfOnShelfCount.value) * 100)
})

// 生命周期
onMounted(async () => {
  // 初始化IndexedDB
  try {
    await initIndexedDB()

    // 加载本地数据
    const localArchives = await loadArchivesFromDB()
    const localInventory = await loadInventoryFromDB()
    const localAnomalies = await loadAnomaliesFromDB()

    if (localArchives.length > 0) {
      cachedArchives.value = localArchives
      updateFilteredArchives()
      ElMessage.success(`已从本地加载 ${localArchives.length} 条档案数据`)
    }

    if (localInventory.length > 0) {
      inventoryData.value = localInventory
      ElMessage.success(`已恢复 ${localInventory.length} 条盘点记录`)
    }

    if (localAnomalies.length > 0) {
      statusAnomalies.value = localAnomalies
      ElMessage.warning(`发现 ${localAnomalies.length} 条状态异常记录`)
    }
  } catch (error) {
    console.error('初始化本地存储失败:', error)
    localStorageStatus.value = false
  }

  // 不再自动连接RFID服务
  // connectSSE()  // 移除自动连接

  // 监听网络状态
  window.addEventListener('online', updateNetworkStatus)
  window.addEventListener('offline', updateNetworkStatus)
  updateNetworkStatus()
})

onUnmounted(() => {
  if (eventSource) {
    eventSource.close()
  }

  // 清理重连定时器
  if (reconnectTimer) {
    clearTimeout(reconnectTimer)
    reconnectTimer = null
  }

  window.removeEventListener('online', updateNetworkStatus)
  window.removeEventListener('offline', updateNetworkStatus)
})
</script>

<style scoped>
.archive-inventory-container {
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
}

.control-card {
  margin-bottom: 20px;
}

.data-actions {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.connection-control {
  margin-bottom: 15px;
}

.scan-mode {
  margin-bottom: 15px;
  text-align: center;
}

.scan-actions {
  display: flex;
  gap: 10px;
  justify-content: center;
  margin-bottom: 15px;
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
  height: calc(100vh - 200px);
}

.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-controls {
  display: flex;
  align-items: center;
}

.rfid-code {
  background: #f4f4f5;
  padding: 2px 6px;
  border-radius: 3px;
  font-family: 'Courier New', monospace;
  font-size: 12px;
}

:deep(.scanned-row) {
  background-color: #f0f9ff !important;
}

:deep(.scanned-row:hover > td) {
  background-color: #e6f7ff !important;
}

:deep(.anomaly-row) {
  background-color: #fef0f0 !important;
}

:deep(.anomaly-row:hover > td) {
  background-color: #fde2e2 !important;
}

:deep(.off-shelf-row) {
  background-color: #fafafa !important;
  color: #909399;
}

:deep(.off-shelf-row:hover > td) {
  background-color: #f0f0f0 !important;
}

.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.statistics-card {
  margin-top: 20px;
}

.stat-item {
  text-align: center;
  padding: 20px;
  background: #fafafa;
  border-radius: 8px;
}

.stat-item.stat-danger {
  background: #fef0f0;
}

.stat-item.stat-danger .stat-value {
  color: #f56c6c;
}

.stat-value {
  font-size: 32px;
  font-weight: bold;
  color: #409eff;
  margin-bottom: 10px;
}

.stat-label {
  font-size: 14px;
  color: #909399;
}

.anomaly-section {
  margin-bottom: 20px;
}

.anomaly-section h4 {
  margin-top: 0;
  color: #f56c6c;
}

.inventory-history {
  max-height: 400px;
  overflow-y: auto;
  padding: 10px;
}

.inventory-history h4 {
  margin-top: 0;
  color: #303133;
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