<template>
  <div class="app-container">
    <!-- 页面标题和返回按钮 -->
    <div class="flex items-center justify-between mb-4 w-full">
      <!-- 左侧内容 -->
      <div class="flex items-center">
<!--        <el-button icon="el-icon-arrow-left" size="small" @click="goBack" class="mr-3">返回</el-button>-->
        <h2 class="text-lg font-medium">
          {{ projectName }} - 档案列表
          <el-tag v-if="projectName" type="success" size="small" class="ml-2">
            共{{ total }}条记录
          </el-tag>
        </h2>
      </div>

      <!-- 右侧按钮组 -->
      <div class="flex button-group" style="margin-left: auto;" v-if="!isOrdinaryUser">
        <el-button type="primary" @click="importArchive" v-hasPermi="['manage:archive:add']">批量导入档案</el-button>
        <el-button type="primary" @click="downloadDocumentTemplate"
          v-hasPermi="['manage:document:add']">下载文档导入模板</el-button>
        <el-button type="primary" @click="importDocument" :disabled="importingDocument"
          v-hasPermi="['manage:document:add']">
          {{ importingDocument ? '请等待' : '导入文档' }}
        </el-button>
        <el-button type="primary" @click="openFileLibrary" v-hasPermi="['manage:document:query']">
          挂接文档数/总数 : {{ mountedCount }} / {{ mountedCount + unmountedCount }}
        </el-button>
      </div>
    </div>

    <!-- 导入文件夹对话框 -->
    <el-dialog title="请选择文件夹" v-model="documentDialogVisible" width="40%">
      <div class="upload-container" @dragover.prevent @drop="handleDrop">
        <input type="file" webkitdirectory @change="handleDirectoryChange" style="display: none" ref="folderInput" />
        <el-button @click="$refs.folderInput.click()">点击选择文件夹</el-button>
        <p>或拖拽文件夹到此处</p>
      </div>
    </el-dialog>

    <!-- 搜索区域 -->
    <el-form :model="queryParams" ref="queryRef" :inline="true" class="mb-4">
      <el-form-item label="档号" prop="danghao">
        <el-input v-model="queryParams.danghao" placeholder="请输入档号" clearable @keyup.enter="getList" />
      </el-form-item>
      <el-form-item label="档案名称" prop="name">
        <el-input v-model="queryParams.name" placeholder="请输入档案名称" clearable @keyup.enter="getList" />
      </el-form-item>

      <!-- 新增门类搜索 -->
      <el-form-item label="大门类" prop="rootCategoryCode">
        <el-select v-model="queryParams.rootCategoryCode" placeholder="请选择大门类" clearable style="width: 200px"
          @change="handleRootCategoryChange">
          <el-option v-for="category in categoryRoots" :key="category.id" :label="`${category.code} - ${category.name}`"
            :value="`${category.code}:${category.name}`" />
        </el-select>
      </el-form-item>

      <el-form-item label="小门类" prop="categoryCode">
        <el-tree-select v-model="queryParams.categoryCode" :data="categoryTreeData" :props="{
          label: 'name',
          value: 'code',
          children: 'children'
        }" value-key="code" placeholder="请选择小门类" style="width: 250px" clearable :render-after-expand="false"
          :check-strictly="true" :disabled="!queryParams.rootCategoryCode">
          <template #default="{ node, data }">
            <span class="tree-select-node">
              <span class="node-code">{{ data.code }}</span>
              <span class="node-name">{{ data.name }}</span>
            </span>
          </template>
        </el-tree-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 档案表格 -->
    <el-table v-loading="loading" :data="archiveList" border fit highlight-current-row
      @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" v-if="!isOrdinaryUser" />
      <el-table-column label="档号" prop="danghao" align="center" min-width="120" />
      <el-table-column label="档案名称" prop="name" align="center" min-width="180" />
      <el-table-column label="保密级别" align="center" min-width="80">
        <template #default="scope">
          <dict-tag :options="iams_secret_level" :value="scope.row.secretLevel" />
        </template>
      </el-table-column>
      <el-table-column label="载体类型" align="center" min-width="120">
        <template #default="scope">
          <dict-tag :options="iams_carrier_type" :value="scope.row.carrierType" />
        </template>
      </el-table-column>
      <el-table-column label="档案状态" align="center" min-width="100" v-hasPermi="['manage:archive:add']">
        <template #default="scope">
          <dict-tag :options="iams_archive_status" :value="scope.row.status" />
        </template>
      </el-table-column>
      <el-table-column label="创建时间" prop="createTime" align="center" min-width="120"
        v-hasPermi="['manage:archive:add']">
        <template #default="{ row }">
          {{ row.createTime }}<!-- 直接返回原始时间数据-->
        </template>
      </el-table-column>
      <!-- 实体档案位置-->
      <el-table-column label="实体档案位置" align="center" min-width="120" prop="location" show-overflow-tooltip
        v-hasPermi="['manage:archive:add']">
        <template #default="scope">
          <el-tag v-if="scope.row.shitiLocation" type="info" size="small">
            {{ scope.row.shitiLocation }}-{{ scope.row.exactLocation }}
          </el-tag>
          <el-tag v-else type="warning" size="small">
            -
          </el-tag>
        </template>
      </el-table-column>

      <!-- 操作列 -->
      <el-table-column label="操作" align="center" min-width="220">
        <template #default="{ row }">
          <!-- 借阅按钮 - 所有用户可见 -->
          <el-button size="small" link type="primary" @click="handleBorrow(row)">借阅</el-button>

          <!-- 其他操作按钮 - 普通用户隐藏 -->
          <template v-if="!isOrdinaryUser">
            <!-- 基础操作按钮 -->
            <el-button
                size="small"
                link
                type="primary"
                @click="handleDetail(row)"
                v-hasPermi="['manage:archive:query']">
              查看
            </el-button>

            <el-button
                size="small"
                link
                type="primary"
                @click="handleUpdate(row)"
                v-if="row.status !== 'Archived' && row.status !== 'Destroyed'"
                v-hasPermi="['manage:archive:edit']">
              修改
            </el-button>

            <el-button
                size="small"
                link
                type="primary"
                @click="handleDelete(row)"
                v-if="row.status !== 'Archived' && row.status !== 'Destroyed'"
                v-hasPermi="['manage:archive:remove']">
              删除
            </el-button>

            <!-- 日志按钮 -->
            <el-button
                link
                type="primary"
                v-if="row.status !== 'Destroyed'"
                @click="getArchiveLog(row.id)">
              操作日志
            </el-button>

            <el-button
                link
                type="primary"
                v-if="row.status !== 'Destroyed'"
                @click="getBorrowLog(row)">
              借阅日志
            </el-button>
            <!-- 新增：浏览日志按钮 -->
            <el-button link type="primary" v-if="row.status !== 'Destroyed'" @click="handleBrowseLog(row)">浏览日志</el-button>
            <!-- 归档相关按钮 -->
            <el-button
                link
                type="primary"
                v-if="row.status !== 'Archived' && row.status !== 'Destroyed'"
                @click="handleGuidang(row)">
              归档
            </el-button>

            <el-button
                link
                type="danger"
                v-if="row.status === 'Archived' && row.status !== 'Destroyed'"
                @click="handleUnarchive(row)">
              撤销归档
            </el-button>

            <!-- RFID标签和上下架逻辑 - 仅归档状态显示 -->
            <template v-if="row.status === 'Archived' && row.status !== 'Destroyed'">

              <!-- RFID为空时：只显示打印标签按钮 -->
              <el-button
                  size="small"
                  link
                  type="primary"
                  v-if="!row.rfid || row.rfid === ''"
                  @click="handlePrintRfid(row)">
                打印标签
              </el-button>

              <!-- RFID不为空时：显示重新打印和上下架按钮 -->
              <template v-else>
                <!-- 重新打印按钮 -->
                <el-button
                    size="small"
                    link
                    type="primary"
                    @click="handleReprintRfid(row)">
                  重新打印
                </el-button>

                <!-- 上架按钮：RFID存在但位置为空 -->
                <el-button
                    link
                    type="primary"
                    v-if="!row.shitiLocation || row.shitiLocation === ''"
                    @click="handShangjia(row)">
                  上架
                </el-button>

                <!-- 下架按钮：RFID存在且位置不为空 -->
                <el-button
                    link
                    type="primary"
                    v-if="row.shitiLocation && row.shitiLocation !== ''"
                    @click="handleXiajia(row)">
                  下架
                </el-button>
              </template>

              <!-- 其他归档后的操作按钮 -->
              <el-button
                  size="small"
                  link
                  type="primary"
                  @click="handleCharacteristicsTest(row)">
                检测报告
              </el-button>
            </template>

            <!-- 移架和合架操作 -->
            <el-button
                size="small"
                link
                type="primary"
                v-if="row.status !== 'Destroyed'"
                @click="handleMove(row)">
              移架操作
            </el-button>

            <el-button
                size="small"
                link
                type="primary"
                v-if="row.status !== 'Destroyed'"
                @click="handleCombine(row)">
              合架操作
            </el-button>
          </template>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 操作日志 -->
    <el-dialog title="操作日志" v-model="archiveLogDialogVisible" width="900px" append-to-body>
      <el-table :data="archiveLogList" style="width: 100%">
        <!-- <el-table-column type="index" label="序号" width="50" align="center" /> -->
        <el-table-column prop="taskType" label="操作类型" align="center" />
        <el-table-column prop="initiator" label="操作人员" align="center" />
        <!-- <el-table-column prop="handler" label="经办人" align="center" show-overflow-tooltip /> -->
        <el-table-column prop="startDate" label="操作时间" align="center" />
        <el-table-column prop="taskStatus" label="上下架位置" align="center" />
        <el-table-column prop="remark" label="备注" align="center" />
      </el-table>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="archiveLogDialogVisible = false">关 闭</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 借阅日志 -->
    <el-dialog title="借阅日志" v-model="borrowLogDialogVisible" width="900px" append-to-body>
      <!-- 筛选区域 -->
      <div class="filter-container">
        <el-radio-group v-model="archiveTypeFilter" @change="filterBorrowLogs">
          <el-radio-button label="all">全部</el-radio-button>
          <el-radio-button label="paperElectronic">纸质+电子档案</el-radio-button>
          <el-radio-button label="electronic">电子档案</el-radio-button>
        </el-radio-group>
      </div>

      <el-table :data="filteredBorrowLogList" style="width: 100%">
        <el-table-column prop="taskType" label="操作类型" align="center" />
        <el-table-column label="发起人" align="center">
          <template #default="scope">
            {{ formatInitiator(scope.row.initiator) }}
          </template>
        </el-table-column>
        <el-table-column label="审核员" align="center" show-overflow-tooltip>
          <template #default="scope">
            {{ formatHandler(scope.row.handler) }}
          </template>
        </el-table-column>
        <el-table-column prop="startDate" label="起始时间" align="center" />
        <el-table-column prop="endDate" label="终止时间" align="center" />
        <el-table-column prop="taskStatus" label="任务状态" align="center" />
        <el-table-column prop="remark" label="备注" align="center" />
<!--        <el-table-column label="借阅备注" align="center">-->
<!--          <template #default="scope">-->
<!--            {{ scope.row.borrowRemark }}-->
<!--          </template>-->
<!--        </el-table-column>-->
        <!-- 档案类型列 -->
        <el-table-column label="档案类型" align="center">
          <template #default="scope">
            <el-tag :type="getArchiveTypeTagType(scope.row)">
              {{ getArchiveTypeText(scope.row) }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="borrowLogDialogVisible = false">关 闭</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 浏览日志弹窗 - 优化版 -->
    <el-dialog
        title="文档浏览日志"
        v-model="browseLogDialogVisible"
        width="1100px"
        append-to-body
        class="browse-log-dialog"
    >
      <el-table
          :data="browseLogList"
          style="width: 100%"
          v-loading="browseLogLoading"
          height="400px"
          stripe
      >
        <el-table-column prop="archive_danghao" label="档案档号" align="center" width="180" />
        <el-table-column prop="document_name" label="文档名称" align="center" min-width="150" />
        <!--        <el-table-column prop="archive_name" label="档案名称" align="center" width="150" show-overflow-tooltip />-->
        <el-table-column prop="viewer_name" label="查看人" align="center" width="100" />
        <el-table-column prop="start_time" label="开始时间" align="center" width="160">
          <template #default="scope">
            {{ formatDateTime(scope.row.start_time) }}
          </template>
        </el-table-column>
        <el-table-column prop="end_time" label="结束时间" align="center" width="160">
          <template #default="scope">
            {{ formatDateTime(scope.row.end_time) }}
          </template>
        </el-table-column>
        <el-table-column prop="duration" label="查看时长" align="center" width="100">
          <template #default="scope">
            {{ formatDuration(scope.row.duration) }}
          </template>
        </el-table-column>

      </el-table>

      <!-- 分页控件 - 优化布局 -->
      <div class="pagination-container" style="margin-top: 20px; display: flex; justify-content: space-between; align-items: center;">
        <div class="pagination-info">
          共 {{ browseLogTotal }} 条记录
        </div>
        <el-pagination
            v-model:current-page="browseLogQueryParams.pageNum"
            v-model:page-size="browseLogQueryParams.pageSize"
            :page-sizes="[10, 20, 50, 100]"
            :total="browseLogTotal"
            layout="sizes, prev, pager, next, jumper"
            @size-change="getBrowseLogList"
            @current-change="getBrowseLogList"
        />
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="browseLogDialogVisible = false">关 闭</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 上架弹窗 -->
    <el-dialog v-model="guidangDialog.visible" title="档案上架" width="600px" :close-on-click-modal="false">
      <!-- 新增：操作信息区域 -->
      <div class="operation-info-section"
        style="margin-bottom: 10px; padding: 10px; background-color: #f9f9f9; border-radius: 2px;">
        <h4 style="margin: 0 0 10px 0; color: #303133;">操作信息</h4>

        <el-form ref="operationFormRef" :model="operationInfo" label-width="100px" :rules="operationRules">
          <el-form-item label="操作人员" prop="operator">
            <el-select v-model="operationInfo.operator" placeholder="请选择操作人员">
              <el-option v-for="user in userList" :key="user.id" :label="user.userName" :value="user.userName" />
            </el-select>
          </el-form-item>

          <el-form-item label="上架原因" prop="reason">
            <el-input v-model="operationInfo.reason" type="textarea" :rows="2" placeholder="请输入本次操作的原因" />
          </el-form-item>
        </el-form>
      </div>

      <div class="guidang-content">
        <!-- 显示档案信息 -->
        <div class="archive-info"
          style="margin-bottom: 20px; padding: 15px; background-color: #f5f7fa; border-radius: 4px;">
          <h4 style="margin: 0 0 10px 0; color: #303133;">档案信息</h4>
          <p style="margin: 5px 0; color: #606266;">
            <strong>档案名称:</strong> {{ guidangDialog.archiveInfo.name }}
          </p>
          <p style="margin: 5px 0; color: #606266;">
            <strong>档号:</strong> {{ guidangDialog.archiveInfo.danghao }}
          </p>
          <p style="margin: 5px 0; color: #606266;">
            <strong>载体类型:</strong>
            <el-tag :type="getCarrierTypeTag(guidangDialog.archiveInfo.carrierType)">
              {{ getCarrierTypeLabel(guidangDialog.archiveInfo.carrierType) }}
            </el-tag>
          </p>
        </div>

        <!-- 电子档案归档提示 -->
        <div v-if="guidangDialog.archiveInfo.carrierType === 'electronic'" class="electronic-tip">
          <el-alert title="电子档案归档" description="电子档案无需选择实体位置，将直接进行归档处理。" type="info" :closable="false"
            style="margin-bottom: 20px;">
          </el-alert>
        </div>

        <!-- 在实体档案位置选择部分修改 -->
        <div v-else>
          <!-- 查看空余位置链接 - 改为展开/收起 -->
          <div class="check-empty-link">
            <el-link type="primary" :underline="false" @click="toggleCabinetView">
              <i :class="showCabinetView ? 'el-icon-arrow-up' : 'el-icon-arrow-down'"></i>
              {{ showCabinetView ? '收起' : '查看' }}空余位置信息
            </el-link>
          </div>

          <!-- 档案柜视图 -->
          <el-collapse-transition>
            <div v-show="showCabinetView" class="cabinet-view-container">
              <!-- 区号选择 -->
              <div class="qu-selector">
                <el-radio-group v-model="selectedQuNo" @change="handleQuNoChange">
                  <el-radio-button v-for="dict in iams_shiti_location_quhao" :key="dict.value" :label="dict.value">
                    {{ dict.label }}
                  </el-radio-button>
                </el-radio-group>
              </div>

              <!-- 档案柜网格 -->
              <div class="cabinet-grid">
                <el-row :gutter="10">
                  <el-col v-for="(item, index) in cabinetCard" :key="index" :span="8" style="margin-bottom: 10px;">
                    <div class="mini-cabinet-card">
                      <!-- 卡片头部 -->
                      <div class="mini-card-header">
                        <span>{{ item.quNo }}区-{{ item.colNo }}列</span>
                        <span class="capacity-info">已用:{{ item.size }}</span>
                      </div>

                      <!-- 左右侧格子 -->
                      <div class="mini-card-body">
                        <!-- 左侧 -->
                        <div class="mini-side">
                          <div class="side-label">{{ getLeftSectionLabel(item.quNo) }}</div>
                          <div :class="getMiniGridClass(item.quNo)">
                            <div v-for="comp in getCompartmentsByCabinetAndSide(item.quNo, item.colNo, 'left')"
                              :key="comp.id" :class="['mini-cell', getCellClass(comp)]"
                              @click="selectCompartment(item.quNo, item.colNo, 'left', comp)"
                              :title="`${comp.leNo}节${comp.divNo}层 已用:${comp.size}`">
                            </div>
                          </div>
                        </div>

                        <!-- 分隔线 -->
                        <div v-if="item.quNo !== 2" class="mini-divider1"></div>
                        <div v-else class="mini-divider2"></div>

                        <!-- 右侧 -->
                        <div class="mini-side">
                          <div class="side-label">{{ getRightSectionLabel(item.quNo) }}</div>
                          <div :class="getMiniGridClass(item.quNo)">
                            <div v-for="comp in getCompartmentsByCabinetAndSide(item.quNo, item.colNo, 'right')"
                              :key="comp.id" :class="['mini-cell', getCellClass(comp)]"
                              @click="selectCompartment(item.quNo, item.colNo, 'right', comp)"
                              :title="`${comp.leNo}节${comp.divNo}层 已用:${comp.size}`">
                            </div>
                          </div>
                        </div>
                      </div>
                    </div>
                  </el-col>
                </el-row>
              </div>

              <!-- 图例 -->
              <div class="cabinet-legend">
                <span class="legend-item">
                  <i class="legend-box cell-green"></i>空闲
                </span>
                <span class="legend-item">
                  <i class="legend-box cell-yellow"></i>已用
                </span>
                <span class="legend-item">
                  <i class="legend-box cell-red"></i>快满
                </span>
              </div>
            </div>
          </el-collapse-transition>

          <!-- 实体位置选择 -->
          <el-form ref="guidangFormRef" :model="guidangForm" :rules="guidangRules" label-width="120px">
            <el-form-item label="实体档案位置" prop="location">
              <div class="location-selector">
                <el-select v-model="guidangForm.quHao" placeholder="区号" class="location-item"
                  @change="handleLocationChange">
                  <el-option v-for="dict in iams_shiti_location_quhao" :key="dict.value" :label="dict.label"
                    :value="dict.value" />
                </el-select>

                <el-select v-model="guidangForm.lieHao" placeholder="列号" class="location-item"
                  @change="handleLocationChange">
                  <el-option v-for="dict in iams_shiti_location_liehao" :key="dict.value" :label="dict.label"
                    :value="dict.value" />
                </el-select>

                <el-select v-model="guidangForm.ceHao" placeholder="左右侧(AB面)" class="location-item"
                  @change="handleLocationChange">
                  <el-option v-for="dict in iams_shiti_location_zy" :key="dict.value" :label="dict.label"
                    :value="dict.value" />
                </el-select>

                <el-select v-model="guidangForm.jieHao" placeholder="节号" class="location-item"
                  @change="handleLocationChange">
                  <el-option v-for="dict in iams_shiti_location_jiehao" :key="dict.value" :label="dict.label"
                    :value="dict.value" />
                </el-select>

                <el-select v-model="guidangForm.cengHao" placeholder="层号" class="location-item"
                  @change="handleLocationChange">
                  <el-option v-for="dict in iams_shiti_location_cenghao" :key="dict.value" :label="dict.label"
                    :value="dict.value" />
                </el-select>
              </div>
            </el-form-item>

            <el-form-item label="位置信息预览" v-if="guidangForm.locationDisplay">
              <el-tag type="info">{{ guidangForm.locationDisplay }}</el-tag>
              <div style="margin-top: 5px; font-size: 12px; color: #909399;">
                存储位置：{{ guidangForm.shitiLocation || '未设置' }} |
                精确位置：{{ guidangForm.exactLocation || '未设置' }}
              </div>
            </el-form-item>
          </el-form>

          <el-divider style="margin: 20px 0; border-color: #e0e0e0;" />
        </div>
      </div>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="guidangDialog.visible = false">取 消</el-button>
          <el-button type="primary" @click="submitShangjia" :loading="guidangDialog.loading">
            确 认
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 四性检测报告弹窗 -->
    <el-dialog title="档案四性检测报告" v-model="testReportDialogVisible" width="90%"
      :style="{ '--el-dialog-width': 'min(1200px, 90vw)' }" append-to-body :close-on-click-modal="false" top="5vh">
      <div v-loading="testReportLoading" id="app">
        <div class="report-container">
          <!-- 报告头部 -->
          <div class="report-header">
            <div class="report-number">报告编号：AR-{{ getCurrentDate() }}-001</div>
            <h1 class="report-title">档案四性检测报告</h1>
            <p class="report-subtitle">Archive Quadruple Properties Test Report</p>
          </div>

          <!-- 报告内容 -->
          <div class="report-content">
            <!-- 基本信息 -->
            <table class="basic-info-table">
              <thead>
                <tr>
                  <th colspan="6" style="font-size: 16px; padding: 15px;">档案基本信息</th>
                </tr>
              </thead>
              <tbody>
                <tr>
                  <td style="font-weight: 600; background: #f8fafc;">档案编号</td>
                  <td>{{ testReportData.archiveInfo?.archiveNumber || 'DA-2024-001' }}</td>
                  <td style="font-weight: 600; background: #f8fafc;">档案名称</td>
                  <td>{{ testReportData.archiveInfo?.archiveName || '重要项目技术文档' }}</td>
                  <td style="font-weight: 600; background: #f8fafc;">RFID标识</td>
                  <td>{{ testReportData.archiveInfo?.rfid || 'RF240620001' }}</td>
                </tr>
                <tr>
                  <td style="font-weight: 600; background: #f8fafc;">项目编号</td>
                  <td>{{ testReportData.archiveInfo?.projectId || 'PRJ-2024-001' }}</td>
                  <td style="font-weight: 600; background: #f8fafc;">检测时间</td>
                  <td>{{ testReportData.testTime || getCurrentDateTime() }}</td>
                  <td style="font-weight: 600; background: #f8fafc;">文档总数</td>
                  <td>{{ testReportData.documentCount || 8 }} 个</td>
                </tr>
              </tbody>
            </table>

            <!-- 四性检测结果 -->
            <table class="test-results-table">
              <thead>
                <tr>
                  <th width="15%">检测项目</th>
                  <th width="12%">检测状态</th>
                  <th width="25%">检测方法</th>
                  <th width="35%">检测详情</th>
                  <th width="13%">检测结果</th>
                </tr>
              </thead>
              <tbody>
                <!-- 真实性检测 -->
                <tr>
                  <td style="font-weight: 600;">
                    真实性检测<br>
                    <small style="color: #909399;">Authenticity</small>
                  </td>
                  <td class="status-cell">
                    <div style="display: flex; align-items: center; justify-content: center;">
                      <span :class="'status-icon status-' + (testReportData.authenticity?.status || 'passed')">
                        {{ getStatusIcon(testReportData.authenticity?.status || 'passed') }}
                      </span>
                      <span class="status-text"
                        :style="'color: ' + getStatusColor(testReportData.authenticity?.status || 'passed')">
                        {{ getStatusText(testReportData.authenticity?.status || 'passed') }}
                      </span>
                    </div>
                  </td>
                  <td>基础信息 + 文档内容哈希验证</td>
                  <td class="detail-info">
                    <p><strong>当前哈希：</strong>{{ testReportData.authenticity?.currentHash || 'a1b2c3d4e5f6...' }}</p>
                    <p><strong>原始哈希：</strong>{{ testReportData.authenticity?.originalHash || 'a1b2c3d4e5f6...' }}</p>
                    <p><strong>检测说明：</strong>{{ testReportData.authenticity?.description || '文档内容未被篡改，数字指纹匹配' }}</p>
                  </td>
                  <td class="status-cell"
                    :style="'font-weight: 600; color: ' + getStatusColor(testReportData.authenticity?.status || 'passed')">
                    {{ getResultText(testReportData.authenticity?.status || 'passed') }}
                  </td>
                </tr>

                <!-- 完整性检测 -->
                <tr>
                  <td style="font-weight: 600;">
                    完整性检测<br>
                    <small style="color: #909399;">Integrity</small>
                  </td>
                  <td class="status-cell">
                    <div style="display: flex; align-items: center; justify-content: center;">
                      <span :class="'status-icon status-' + (testReportData.integrity?.status || 'passed')">
                        {{ getStatusIcon(testReportData.integrity?.status || 'passed') }}
                      </span>
                      <span class="status-text"
                        :style="'color: ' + getStatusColor(testReportData.integrity?.status || 'passed')">
                        {{ getStatusText(testReportData.integrity?.status || 'passed') }}
                      </span>
                    </div>
                  </td>
                  <td>基础信息 + 文件字节哈希验证</td>
                  <td class="detail-info">
                    <p><strong>当前哈希：</strong>{{ testReportData.integrity?.currentHash || 'f6e5d4c3b2a1...' }}</p>
                    <p><strong>原始哈希：</strong>{{ testReportData.integrity?.originalHash || 'f6e5d4c3b2a1...' }}</p>
                    <p><strong>检测说明：</strong>{{ testReportData.integrity?.description || '文件结构完整，无缺失或损坏' }}</p>
                  </td>
                  <td class="status-cell"
                    :style="'font-weight: 600; color: ' + getStatusColor(testReportData.integrity?.status || 'passed')">
                    {{ getResultText(testReportData.integrity?.status || 'passed') }}
                  </td>
                </tr>

                <!-- 可用性检测 -->
                <tr>
                  <td style="font-weight: 600;">
                    可用性检测<br>
                    <small style="color: #909399;">Usability</small>
                  </td>
                  <td class="status-cell">
                    <div style="display: flex; align-items: center; justify-content: center;">
                      <span :class="'status-icon status-' + (testReportData.usability?.status || 'warning')">
                        {{ getStatusIcon(testReportData.usability?.status || 'warning') }}
                      </span>
                      <span class="status-text"
                        :style="'color: ' + getStatusColor(testReportData.usability?.status || 'warning')">
                        {{ getStatusText(testReportData.usability?.status || 'warning') }}
                      </span>
                    </div>
                  </td>
                  <td>文档可读性、格式完整性检测</td>
                  <td class="detail-info">
                    <p><strong>可读文档：</strong>{{ testReportData.usability?.readableCount || 7 }} / {{
                      testReportData.documentCount || 8 }}</p>
                    <p><strong>检测说明：</strong>{{ testReportData.usability?.description || 'PDF文档可读，格式完整' }}</p>
                  </td>
                  <td class="status-cell"
                    :style="'font-weight: 600; color: ' + getStatusColor(testReportData.usability?.status || 'warning')">
                    {{ getResultText(testReportData.usability?.status || 'warning') }}
                  </td>
                </tr>

                <!-- 安全性检测 -->
                <tr>
                  <td style="font-weight: 600;">
                    安全性检测<br>
                    <small style="color: #909399;">Security</small>
                  </td>
                  <td class="status-cell">
                    <div style="display: flex; align-items: center; justify-content: center;">
                      <span :class="'status-icon status-' + (testReportData.security?.status || 'passed')">
                        {{ getStatusIcon(testReportData.security?.status || 'passed') }}
                      </span>
                      <span class="status-text"
                        :style="'color: ' + getStatusColor(testReportData.security?.status || 'passed')">
                        {{ getStatusText(testReportData.security?.status || 'passed') }}
                      </span>
                    </div>
                  </td>
                  <td>访问权限、加密状态检测</td>
                  <td class="detail-info">
                    <p><strong>权限状态：</strong>{{ testReportData.security?.permissionStatus || '正常授权访问' }}</p>
                    <p><strong>检测说明：</strong>{{ testReportData.security?.description || '访问控制正常，敏感文档已加密保护' }}</p>
                  </td>
                  <td class="status-cell"
                    :style="'font-weight: 600; color: ' + getStatusColor(testReportData.security?.status || 'passed')">
                    {{ getResultText(testReportData.security?.status || 'passed') }}
                  </td>
                </tr>
              </tbody>
            </table>

            <!-- 总体评估 -->
            <div class="overall-assessment">
              <div class="assessment-content">
                <div :class="'score-circle ' + getOverallStatusClass()">{{ getOverallScore() }}</div>
                <div class="score-info">
                  <h3 class="score-title">总体评估：{{ getOverallStatus() }}</h3>
                  <p class="score-desc">{{ getAssessmentSummary() }}</p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <template #footer>
        <span class="dialog-footer">
          <el-button type="success" @click="exportReport" :loading="exportLoading">
            <el-icon>
              <Download />
            </el-icon>
            导出报告
          </el-button>
          <el-button type="primary" @click="testReportDialogVisible = false">
            确 认
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 批量导入档案弹窗 -->
    <el-dialog v-model="batchImportVisible" title="批量添加档案" width="90%">
      <!-- 门类选择区 -->
      <div class="batch-header">
        <el-form :inline="true">

          <!-- 新增：全宗选择 -->
          <el-form-item label="选择全宗">
            <el-select v-model="selectedFonds" placeholder="请先选择全宗" style="width: 300px"
              @change="handleFondsChangeInBatch">
              <el-option v-for="fonds in fondsList" :key="fonds.id" :label="`${fonds.name} (${fonds.bianhao})`"
                :value="fonds.id" />
            </el-select>
          </el-form-item>
          <!--          <el-form-item label="选择门类大类">-->
          <!--            <el-select v-model="selectedRootCategory" placeholder="请选择档案主门类" style="width: 300px"-->
          <!--              @change="handleRootCategoryChange">-->
          <!--              <el-option v-for="category in categoryRoots" :key="category.id"-->
          <!--                :label="`${category.code} - ${category.name}`" :value="category.code" />-->
          <!--            </el-select>-->
          <!--          </el-form-item>-->
          <el-form-item label="选择门类大类">
            <el-select v-model="selectedRootCategory" placeholder="请选择档案主门类"
                       style="width: 300px" @change="handleRootCategoryChange">
              <!-- 使用 sortedCategoryRoots 而不是 categoryRoots -->
              <el-option
                  v-for="category in sortedCategoryRoots"
                  :key="category.uniqueKey"
                  :label="`${category.code} - ${category.name}`"
                  :value="category.uniqueKey" />
            </el-select>
          </el-form-item>

          <!-- 二级分类选择 -->
          <el-form-item label="选择二级分类">
            <el-select v-model="selectedSecondCategory" placeholder="请选择二级分类" style="width: 300px"
              :disabled="!selectedRootCategory" @change="handleSecondCategoryChange">
              <el-option v-for="category in secondLevelCategories" :key="category.uniqueKey"
                :label="`${category.code} - ${category.name}`" :value="category.uniqueKey" />
            </el-select>
          </el-form-item>

          <el-form-item>
            <!--            <el-button type="primary" @click="generateSubClassRows" :disabled="!selectedSecondCategory">生成小类行</el-button>-->
            <el-button type="primary" @click="generateSubClassRows" :disabled="!selectedSecondCategory || isGenerating"
              :loading="isGenerating">
              {{ isGenerating ? `生成中 ${generatingProgress}%` : '生成小类行' }}
            </el-button>
          </el-form-item>
        </el-form>

        <div v-if="selectedCategoryName" style="margin-top: 10px;">
          <!--          当前选择大类：<strong>{{ selectedCategoryName }}</strong>-->
          <!--          <span v-if="batchDynamicColumns.length" style="margin-left: 15px;">-->
          当前选择大类：<strong>{{ selectedCategoryName }}</strong>
          <span v-if="selectedSecondCategoryName" style="margin-left: 15px;">
            二级分类：<strong>{{ selectedSecondCategoryName }}</strong>
          </span>
          <span v-if="filteredSubClasses.length" style="margin-left: 15px;">
            <!--            包含小类数量：{{ batchDynamicColumns.length }} 个-->
            包含小类数量：{{ filteredSubClasses.length }} 个
          </span>
        </div>
      </div>

      <!-- 档案表格 -->
      <div class="batch-table-container">
        <el-table :data="batchTableData" border stripe height="500">
          <!-- 操作列 -->
          <el-table-column label="操作" width="160" align="center" fixed>
            <template #default="scope">
              <div class="row-actions">
                <el-button type="danger" size="small" icon="el-icon-delete" @click="deleteBatchRow(scope.$index)"
                  title="删除此行" class="action-btn">
                  删除
                </el-button>
                <el-button type="primary" size="small" icon="el-icon-plus" @click="addBatchRowAt(scope.$index)"
                  title="添加行" class="action-btn">
                  添加
                </el-button>
              </div>
            </template>
          </el-table-column>

          <!-- 门类编码列 -->
          <el-table-column label="门类编码" min-width="120" align="center">
            <template #default="scope">
              <div class="category-codes">
                <el-tag v-for="code in scope.row.selectedCategoryCodes" :key="code" type="info" size="small"
                  style="margin: 2px;">
                  {{ code }}
                </el-tag>
                <!-- 兼容显示：如果没有选中但有原始编码，显示原始编码 -->
                <el-tag v-if="!scope.row.selectedCategoryCodes.length && scope.row.subClassCode" type="info"
                  size="small">
                  {{ scope.row.subClassCode }}
                </el-tag>
              </div>
            </template>
          </el-table-column>

          <!-- 小类名称列 - 修改为可编辑的树形选择器 -->
          <el-table-column label="档案小类" min-width="230" prop="name" fixed>
            <template #default="scope">
              <el-tree-select v-model="scope.row.selectedCategoryCodes" :data="categoryTreeData" :props="{
                label: 'name',
                value: 'code',
                children: 'children'
              }" value-key="code" placeholder="请选择子门类" style="width: 100%" :render-after-expand="false" multiple
                :check-strictly="false" show-checkbox size="small" @change="updateSubClassInfo(scope.row)">
                <template #default="{ node, data }">
                  <span class="tree-select-node">
                    <span class="node-code">{{ data.code }}</span>
                    <span class="node-name">{{ data.name }}</span>
                  </span>
                </template>
              </el-tree-select>
            </template>
          </el-table-column>
          <el-table-column label="全宗" width="150" prop="fondsId">
            <template #default="scope">
              <el-select v-model="scope.row.fondsId" placeholder="请选择全宗" size="small" style="width: 100%"
                @change="updateDanghao(scope.row)">
                <el-option v-for="fonds in fondsList" :key="fonds.id" :label="`${fonds.name}`" :value="fonds.id" />
              </el-select>
            </template>
          </el-table-column>

          <!-- 固定字段 -->
          <el-table-column label="档案名称" min-width="210" prop="name">
            <template #default="scope">
              <el-input v-model="scope.row.name" placeholder="档案名称" size="small" />
            </template>
          </el-table-column>

          <el-table-column label="档号" min-width="350" align="center">
            <template #default="scope">
              <el-input v-model="scope.row.danghao" :disabled="false" :title="getBatchDanghaoParts(scope.row)"
                class="danghao-input">
                <template #append>
                  <el-tooltip content="档号规则提示" placement="top">
                    <el-icon>
                      <InfoFilled />
                    </el-icon>
                  </el-tooltip>
                </template>
              </el-input>
            </template>
          </el-table-column>

          <el-table-column label="件号" width="100" prop="jianhao">
            <template #default="scope">
              <el-input-number v-model="scope.row.jianhao" :min="1" :max="9999" controls-position="right" size="small"
                style="width: 100%" @change="updateDanghao(scope.row)" />
            </template>
          </el-table-column>

          <el-table-column label="案卷号" width="120" prop="juanhao">
            <template #default="scope">
              <el-input v-model="scope.row.juanhao" placeholder="案卷号" size="small" @input="updateDanghao(scope.row)" />
            </template>
          </el-table-column>

          <el-table-column label="组卷单元" width="150" prop="zuojuanUnit">
            <template #default="scope">
              <el-input v-model="scope.row.zuojuanUnit" :placeholder="projectName + '（当前项目）'" size="small"
                @input="updateDanghao(scope.row)" />
            </template>
          </el-table-column>

          <el-table-column label="载体类型" width="120">
            <template #default="scope">
              <el-select v-model="scope.row.carrierType" placeholder="载体类型" size="small" style="width: 100%">
                <el-option v-for="dict in iams_carrier_type" :key="dict.value" :label="dict.label"
                  :value="dict.value" />
              </el-select>
            </template>
          </el-table-column>

          <el-table-column label="保密级别" width="120">
            <template #default="scope">
              <el-select v-model="scope.row.secretLevel" placeholder="保密级别" size="small" style="width: 100%">
                <el-option v-for="dict in iams_secret_level" :key="dict.value" :label="dict.label"
                  :value="dict.value" />
              </el-select>
            </template>
          </el-table-column>

          <el-table-column label="保密期限" width="120">
            <template #default="scope">
              <el-select v-model="scope.row.secretPeroid" placeholder="保密期限" size="small" style="width: 100%">
                <el-option v-for="dict in iams_secret_period" :key="dict.value" :label="dict.label"
                  :value="dict.value" />
              </el-select>
            </template>
          </el-table-column>

          <el-table-column label="保管期限" width="120">
            <template #default="scope">
              <el-select v-model="scope.row.retentionPeriod" placeholder="保管期限" size="small" style="width: 100%"
                @change="updateDanghao(scope.row)">
                <el-option v-for="dict in iams_retention_period" :key="dict.value" :label="dict.label"
                  :value="dict.value" />
              </el-select>
            </template>
          </el-table-column>

          <el-table-column label="电子建档时间" width="150" align="center">
            <template #default="scope">
              <el-date-picker v-model="scope.row.startDate" type="date" value-format="YYYY-MM-DD" placeholder="选择建档时间"
                size="small" style="width: 100%" clearable />
            </template>
          </el-table-column>

          <el-table-column label="形成单位" width="150">
            <template #default="scope">
              <el-input v-model="scope.row.formationUnit" placeholder="形成单位" size="small" />
            </template>
          </el-table-column>

          <el-table-column label="移交单位" width="150">
            <template #default="scope">
              <el-input v-model="scope.row.transferUnit" placeholder="移交单位" size="small" />
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 操作按钮 -->
      <div class="batch-table-footer">
        <el-button type="success" @click="submitBatch" :loading="batchSubmitting" size="small">
          提交批量添加（共 {{ batchTableData.length }} 条）
        </el-button>
        <el-button @click="batchImportVisible = false" size="small">
          取消
        </el-button>
      </div>
    </el-dialog>

    <!--  入库对话框  -->
    <el-dialog v-model="rukuDialogVisible" title="归档确认" width="500px">
      <el-form ref="rukuFormRef" :model="rukuForm" label-width="100px" :rules="rukuRules">
        <el-form-item label="操作人员" prop="operator">
          <el-select v-model="rukuForm.archivist" placeholder="请选择操作人员">
            <el-option v-for="user in userList" :key="user.id" :label="user.userName" :value="user.userName" />
          </el-select>
        </el-form-item>
        <el-form-item label="归档信息" prop="archiveInfo">
          <el-input v-model="rukuForm.archiveInfo" type="textarea" :rows="3" placeholder="请输入归档信息" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="rukuDialogVisible = false">取 消</el-button>
          <el-button type="primary" @click="submitGuidang">确 定</el-button>
        </div>
      </template>
    </el-dialog>

    <!--  下架对话框  -->
    <el-dialog v-model="xiajiaDialogVisible" title="归档确认" width="500px">
      <el-form ref="xiajiaFormRef" :model="xiajiaForm" label-width="100px" :rules="xiajiaRules">
        <el-form-item label="操作人员" prop="operator">
          <el-select v-model="xiajiaForm.operator" placeholder="请选择操作人员">
            <el-option v-for="user in userList" :key="user.id" :label="user.userName" :value="user.userName" />
          </el-select>
        </el-form-item>
        <el-form-item label="下架原因" prop="reason">
          <el-input v-model="xiajiaForm.reason" type="textarea" :rows="3" placeholder="请输入下架原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="xiajiaDialogVisible = false">取 消</el-button>
          <el-button type="primary" @click="submitXiajia">确 定</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 四性检测配置弹窗 -->
    <el-dialog v-model="guidangDialogVisible" title="四性检测配置" width="500px" :before-close="cancelGuidang">
      <div class="config-dialog">
        <p class="dialog-desc">请选择需要进行四性检测的配置项：</p>

        <!-- 档案项目 -->
        <div class="config-group">
          <h4 class="group-title">档案项目</h4>
          <el-checkbox-group v-model="selectedOptions" class="checkbox-group">
            <el-checkbox v-for="option in configOptions.filter(item => item.category === '档案项目')" :key="option.value"
              :value="option.value" :label="option.label" />
          </el-checkbox-group>
        </div>

        <!-- 文档项目 -->
        <div class="config-group">
          <h4 class="group-title">文档项目</h4>
          <el-checkbox-group v-model="selectedOptions" class="checkbox-group">
            <el-checkbox v-for="option in configOptions.filter(item => item.category === '文档项目')" :key="option.value"
              :value="option.value" :label="option.label" />
          </el-checkbox-group>
        </div>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="cancelGuidang">取消</el-button>
          <el-button type="primary" @click="confirmGuidang" :disabled="selectedOptions.length === 0">
            确定归档
          </el-button>
        </div>
      </template>
    </el-dialog>




    <!-- 借阅弹窗 -->
    <el-dialog v-model="borrowOpen" :title="borrowTitle" width="600px">
      <el-form ref="borrowFormRef" :model="borrowForm" :rules="borrowRules" label-width="120px">
        <!-- 档案信息（自动填充） -->
        <el-form-item label="档案档号">
          <el-input v-model="borrowForm.archiveDangHao" disabled />
        </el-form-item>
        <el-form-item label="档案名称">
          <el-input v-model="borrowForm.archiveName" disabled />
        </el-form-item>

        <!-- 用户信息（手动输入） -->
        <el-form-item label="借阅人" prop="userName">
          <el-input v-model="borrowForm.userName" placeholder="请输入借阅人姓名" />
        </el-form-item>
        <el-form-item label="所属部门">
          <el-input v-model="borrowForm.userDepartment" placeholder="请输入所属部门（可选）" />
        </el-form-item>

        <!-- 借阅时间 -->
        <el-form-item label="借阅时间" required>
          <el-col :span="11">
            <el-form-item prop="startDate">
              <el-date-picker v-model="borrowForm.startDate" type="date" placeholder="开始日期" style="width: 100%"
                value-format="YYYY-MM-DD" />
            </el-form-item>
          </el-col>
          <el-col :span="2" class="text-center">至</el-col>
          <el-col :span="11">
            <el-form-item prop="endDate">
              <el-date-picker v-model="borrowForm.endDate" type="date" placeholder="结束日期" style="width: 100%"
                value-format="YYYY-MM-DD" />
            </el-form-item>
          </el-col>
        </el-form-item>

        <!-- 借阅目的 -->
        <el-form-item label="借阅目的" prop="purpose">
          <el-input v-model="borrowForm.purpose" type="textarea" :rows="3" placeholder="请详细说明借阅目的" show-word-limit
            maxlength="200" />
        </el-form-item>

        <!-- 流程选择 -->
        <el-form-item label="流程名称" prop="processName">
          <el-select v-model="borrowForm.processName" placeholder="请选择借阅流程" style="width: 100%">
            <el-option v-for="item in Modeler" :key="item.name" :label="item.name" :value="item.key" />
          </el-select>
        </el-form-item>

        <!-- 备注 -->
        <el-form-item label="备注">
          <el-input v-model="borrowForm.remark" type="textarea" :rows="2" placeholder="可输入额外说明信息" maxlength="100" />
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="borrowOpen = false">取消</el-button>
          <el-button type="primary" @click="submitBorrowForm">提交借阅</el-button>
        </div>
      </template>
    </el-dialog>



  </div>
</template>
<!--<script>-->
<!--// 显示的定义组件名称，和路由地址保持一致，-->
<!--// 在系统管理->菜单管理 中可以配置是否开启页面缓存-->
<!--export default {-->
<!--  name: 'Proj-detail'-->
<!--}-->
<!--</script>-->


<script setup>
import { ref, onMounted, reactive, computed, getCurrentInstance } from 'vue'
import { getFondsCategories } from "@/api/manage/category"; // 确保已导入
import { useRoute, useRouter } from 'vue-router'

// 引入借阅相关的API
import { addRecord, directRecord } from "@/api/manage/borrowrecord"
import { getUserInfo, listDefinition } from "@/api/activiti/definition"
import { getArchiveIdByMysqlDanghao } from "@/api/manage/archive.js"

import {
  getProjectArchives,
  selectArchiveByCategoryId,
  listArchive,
  getArchive,
  delArchive,
  updateArchive,
  guidang,
  unarchive
} from '@/api/manage/archive'
import { handleMoveMeth, handleCombineMeth } from '@/views/iotSubSystem/moveMethods.js'
import { listArchiveLog, addArchiveLog, listOpLogs, listBorrowLogs } from '@/api/manage/ArchiveLog'
import { updateCabinetSize } from '@/api/manage/cabinet'
import { updateCompartmentSize } from '@/api/manage/compartment'
import {listBrowseLogByArchiveId} from "@/api/manage/browseLog.js";
import { listCabinet } from "@/api/manage/cabinet"
import { listCompartment } from "@/api/manage/compartment"
import {ElLoading, ElMessage, ElMessageBox} from "element-plus"
import { parseTime } from '@/utils/ruoyi'
import axios from 'axios'
import { getToken } from "@/utils/auth"
import {addDocument, listDocumentByArchiveId, listDocumentByProjectId} from "@/api/manage/document"
import pLimit from 'p-limit'
import SparkMD5 from 'spark-md5'
import CryptoJS from 'crypto-js'
import { getUserProfile } from "@/api/system/user.js"
import { getCategoryRoots, getCategoryTreeByUniqueKey } from "@/api/manage/treeCategory.js"
import { listUserByRoleId } from "@/api/system/user";
import request from '@/utils/request'
import dayjs from 'dayjs';
const route = useRoute()
const router = useRouter()
const { proxy } = getCurrentInstance()

// 借阅弹窗相关状态
const borrowOpen = ref(false)
const borrowTitle = ref("快速借阅")
const borrowFormRef = ref()
const Modeler = ref([])

// 浏览日志相关变量
const browseLogDialogVisible = ref(false)
const browseLogList = ref([])
const browseLogLoading = ref(false)
const browseLogTotal = ref(0)
// 浏览日志查询参数
const browseLogQueryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  archiveId: null
})

// 处理浏览日志按钮点击
const handleBrowseLog = (row) => {
  currentArchiveId.value = row.id
  browseLogQueryParams.archiveId = row.id
  browseLogQueryParams.pageNum = 1
  browseLogDialogVisible.value = true
  getBrowseLogList()
}

// 获取浏览日志列表
const getBrowseLogList = () => {
  browseLogLoading.value = true
  listBrowseLogByArchiveId(browseLogQueryParams).then(response => {
    console.log('浏览日志数据:', response.rows) // 添加这行来调试
    browseLogList.value = response.rows || []
    browseLogTotal.value = response.total || 0
    browseLogLoading.value = false
  }).catch(error => {
    console.error('获取浏览日志失败:', error)
    browseLogLoading.value = false
    ElMessage.error('获取浏览日志失败')
  })
}

// 格式化日期时间
const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  return dayjs(dateTime).format('YYYY-MM-DD HH:mm:ss')
}

// 格式化时长（秒转换为可读格式）
const formatDuration = (seconds) => {
  if (!seconds) return '-'

  if (seconds < 60) {
    return `${seconds}秒`
  } else if (seconds < 3600) {
    const minutes = Math.floor(seconds / 60)
    const remainingSeconds = seconds % 60
    return `${minutes}分${remainingSeconds}秒`
  } else {
    const hours = Math.floor(seconds / 3600)
    const minutes = Math.floor((seconds % 3600) / 60)
    return `${hours}小时${minutes}分`
  }
}

// 借阅表单数据
const borrowForm = ref({
  userId: null,
  userName: null,
  userDepartment: null,
  archiveId: null,
  archiveName: null,
  archiveDangHao: null,
  purpose: null,
  startDate: null,
  endDate: null,
  processName: null,
  remark: null,
})

// 借阅表单验证规则
const borrowRules = {
  startDate: [
    { required: true, message: "借阅开始日期不能为空", trigger: "blur" }
  ],
  endDate: [
    { required: true, message: "借阅结束日期不能为空", trigger: "blur" }
  ],
  processName: [
    { required: true, message: "流程名称不能为空", trigger: "blur" }
  ],
  purpose: [
    { required: true, message: "借阅目的不能为空", trigger: "blur" }
  ],
}

// 流程查询参数
const queryParams_activitiList = reactive({
  pageNum: 1,
  pageSize: 10,
  id: null,
  rev: null,
  name: null,
  type: null,
})

// 进度显示相关状态
const processedCount = ref(0)
const totalCount = ref(0)
const currentBatchSize = ref(8)
const memoryUsage = ref('')
const memoryWarning = ref(false)
const estimatedTimeLeft = ref('计算中...')
const startTime = ref(0)
const batchTimes = ref([])

// 进度条颜色
const progressColor = computed(() => {
  if (generatingProgress.value < 30) return '#f56c6c'
  if (generatingProgress.value < 70) return '#e6a23c'
  return '#67c23a'
})


// 内存监控函数
const monitorMemory = () => {
  if (performance.memory) {
    const used = performance.memory.usedJSHeapSize / 1024 / 1024 // MB
    const total = performance.memory.totalJSHeapSize / 1024 / 1024 // MB
    const limit = performance.memory.jsHeapSizeLimit / 1024 / 1024 // MB
    const usagePercentage = (used / limit) * 100

    memoryUsage.value = `${Math.round(used)}MB (${Math.round(usagePercentage)}%)`
    memoryWarning.value = usagePercentage > 70

    // 详细的Console日志
    console.log(`🔍 内存监控:`, {
      '已使用': `${Math.round(used)}MB`,
      '总分配': `${Math.round(total)}MB`,
      '内存限制': `${Math.round(limit)}MB`,
      '使用率': `${Math.round(usagePercentage)}%`,
      '状态': usagePercentage > 80 ? '⚠️ 内存压力大' : usagePercentage > 50 ? '⚡ 正常使用' : '✅ 内存充足'
    })

    // 内存使用过高时的建议
    if (usagePercentage > 80) {
      console.warn(`⚠️ 内存使用过高 (${Math.round(usagePercentage)}%)，建议：`)
      console.warn(`   • 减少批次大小到 ${Math.max(3, Math.floor(currentBatchSize.value * 0.6))}`)
      console.warn(`   • 增加延时到 200ms 以上`)
      console.warn(`   • 考虑分批选择数据`)
    }

    return {
      used: Math.round(used),
      percentage: Math.round(usagePercentage),
      status: usagePercentage > 80 ? 'danger' : usagePercentage > 50 ? 'warning' : 'normal'
    }
  }
  return null
}

// 计算预计剩余时间
const calculateEstimatedTime = (processed, total, batchTimes) => {
  if (batchTimes.length === 0 || processed === 0) {
    estimatedTimeLeft.value = '计算中...'
    return
  }

  const avgBatchTime = batchTimes.reduce((sum, time) => sum + time, 0) / batchTimes.length
  const remainingBatches = Math.ceil((total - processed) / currentBatchSize.value)
  const estimatedMs = remainingBatches * avgBatchTime

  if (estimatedMs < 1000) {
    estimatedTimeLeft.value = '即将完成'
  } else if (estimatedMs < 60000) {
    estimatedTimeLeft.value = `约 ${Math.round(estimatedMs / 1000)} 秒`
  } else {
    const minutes = Math.floor(estimatedMs / 60000)
    const seconds = Math.round((estimatedMs % 60000) / 1000)
    estimatedTimeLeft.value = `约 ${minutes}分${seconds}秒`
  }
}


// 位置选择项
const iams_shiti_location_quhao = ref([
  { label: '第1区', value: '1' },
  { label: '第2区', value: '2' },
  { label: '第3区', value: '3' },
  { label: '第4区', value: '4' }
]);

const iams_shiti_location_liehao = ref([]);
const iams_shiti_location_zy = ref([]);
const iams_shiti_location_cenghao = ref([]);
const iams_shiti_location_jiehao = ref([]);

const initLocationOptions = (quNo) => {
  const isArea2 = (quNo === '2' || quNo === 2);

  // 列号选项
  const maxCols = isArea2 ? 13 : 18;
  iams_shiti_location_liehao.value = Array.from({ length: maxCols }, (_, i) => ({
    label: `第${i + 1}列`,
    value: String(i + 1)
  }));

  // 左右侧选项
  if (isArea2) {
    iams_shiti_location_zy.value = [{ label: '右侧(A面)', value: 'A' }];
  } else {
    iams_shiti_location_zy.value = [
      { label: '左侧(A面)', value: 'A' },
      { label: '右侧(B面)', value: 'B' }
    ];
  }

  // 层号选项
  const maxCeng = isArea2 ? 20 : 6;
  iams_shiti_location_cenghao.value = Array.from({ length: maxCeng }, (_, i) => ({
    label: `第${i + 1}层`,
    value: String(i + 1)
  }));

  // 节号选项
  const maxJie = isArea2 ? 4 : 5;
  iams_shiti_location_jiehao.value = Array.from({ length: maxJie }, (_, i) => ({
    label: `第${i + 1}节`,
    value: String(i + 1)
  }));
};

// 获取左侧标签
function getLeftSectionLabel(quNo) {
  return quNo === 2 ? 'A面(1-10层)' : '左(A)';
}

// 获取右侧标签  
function getRightSectionLabel(quNo) {
  return quNo === 2 ? 'A面(11-20层)' : '右(B)';
}

// 获取小网格的样式类
function getMiniGridClass(quNo) {
  return quNo === 2 ? 'mini-grid-area2' : 'mini-grid';
}

// const DEFAULT_FONDS_CODE = '10000'
const MAX_RETRY = 3
const CONCURRENCY = Math.max(1, navigator.hardwareConcurrency - 1)

// 基础状态
const headers = ref({ Authorization: "Bearer " + getToken() })
const loading = ref(false)
const user = ref(null)
const userPostCodes = ref([])
const isEncrypt = ref(false)
const projectName = ref('')
const archiveList = ref([])
const total = ref(0)
const selectedItems = ref([])
const ids = ref([])

// 定义普通用户的岗位编码（根据你的实际岗位编码调整）
const ORDINARY_USER_POST_CODES = [
  'staff',         // 员工
  'employee',      // 雇员
  'user',          // 用户
  // 在这里添加你系统中定义的普通用户岗位编码
]

// 判断是否为普通用户（仅这一个判断就够了）
const isOrdinaryUser = computed(() => {
  // 如果用户的岗位编码中包含任何一个普通用户岗位编码，就认为是普通用户
  return userPostCodes.value.some(code => ORDINARY_USER_POST_CODES.includes(code))
})

// 加载用户信息
const loadUserInfo = async () => {
  try {
    const response = await getUserProfile()

    user.value = response.data

    // 修复：正确获取 postCodes
    userPostCodes.value = Array.from(response.postCodes || [])

    // 调试信息
    console.log('用户岗位编码:', userPostCodes.value)
    console.log('是否普通用户:', isOrdinaryUser.value)

  } catch (error) {
    console.error('获取用户信息失败:', error)
    ElMessage.error('获取用户信息失败')
  }
}

// function getCurrentDate() {
//   return new Date().toLocaleDateString('zh-CN').replace(/\//g, '');
// }
// function getCurrentDateTime() {
//   return new Date().toLocaleString('zh-CN');
// }

const {
  iams_carrier_type,
  iams_archive_status,
  iams_retention_period,
  iams_secret_period,
  iams_secret_level
} = proxy.useDict('iams_carrier_type', 'iams_archive_status', 'iams_retention_period', 'iams_secret_period', 'iams_secret_level');


// 归档弹窗相关
const guidangDialog = reactive({
  visible: false,
  loading: false,
  archiveId: null,
  archiveInfo: {}
})

const guidangFormRef = ref()
const guidangForm = reactive({
  quHao: '',
  lieHao: '',
  ceHao: '',
  jieHao: '',
  cengHao: '',
  shitiLocation: '',
  exactLocation: '',
  locationDisplay: ''
})

// 3. 修改 watch 监听器 - 只在手动选择时才清空其他字段
let isSelectingFromCabinet = false; // 标记是否从档案柜选择

watch(
  () => guidangForm.quHao,
  (newValue, oldValue) => {
    console.log('区号变化:', { newValue, oldValue, isSelectingFromCabinet });

    // 初始化选项
    if (newValue) {
      initLocationOptions(newValue);
    }

    // 只有在手动选择下拉框时才清空其他字段
    if (!isSelectingFromCabinet && oldValue && newValue !== oldValue) {
      guidangForm.lieHao = '';
      guidangForm.ceHao = '';
      guidangForm.jieHao = '';
      guidangForm.cengHao = '';
      guidangForm.shitiLocation = '';
      guidangForm.exactLocation = '';
      guidangForm.locationDisplay = '';
    }
  }
);

// 查询参数
const queryParams = ref({
  projectId: undefined,
  pageNum: 1,
  pageSize: 10,
  danghao: undefined,
  name: undefined,

  rootCategoryCode: undefined,
  categoryCode: undefined
})

// 电子借阅标志
const elecBorrow = ref(false)

// 借阅功能方法
async function handleBorrow(row) {
  console.log('快速借阅档案:', row);

  // 重置借阅表单
  resetBorrowForm();

  // 自动填充档案信息
  borrowForm.value.archiveDangHao = row.danghao;
  borrowForm.value.archiveName = row.name;
  borrowForm.value.archiveId = row.id;

  // 获取档案信息
  const res = await getArchive(borrowForm.value.archiveId);
  const archive = res.data;
  console.log('档案信息:', archive);

  // 如果档案没有归档则不能借阅
  if (archive.status !== 'Archived') {
    ElMessage.warning('档案还未归档，不能借阅！');
    return;
  }

  // 如果实体档案已借出则提示
  if (archive.availability && archive.availability !== 0) {
    const confirm = window.confirm('当前实体档案无法借阅，是否继续借阅电子档案？');
    if (confirm) {
      elecBorrow.value = true;
    } else {
      return;
    }
  }

  try {
    const userResponse = await getUserInfo();
    borrowForm.value.userId = userResponse.userId;
    borrowForm.value.userName = userResponse.userName;
    borrowForm.value.userDepartment = userResponse.deptName;
  } catch (error) {
    console.error('获取用户信息失败:', error);
    ElMessage.error('获取用户信息失败');
    return;
  }

  // 获取流程列表
  try {
    const processResponse = await listDefinition(queryParams_activitiList);
    Modeler.value = processResponse.rows;
  } catch (error) {
    console.error('获取流程列表失败:', error);
  }

  // 显示借阅弹窗
  borrowOpen.value = true;
}

// 重置借阅表单
function resetBorrowForm() {
  borrowForm.value = {
    userId: null,
    userName: null,
    userDepartment: null,
    archiveId: null,
    archiveName: null,
    archiveDangHao: null,
    purpose: null,
    startDate: null,
    endDate: null,
    processName: null,
    remark: null,
  };
}

// 提交借阅表单
function submitBorrowForm() {
  borrowFormRef.value.validate(valid => {
    if (valid) {
      if (elecBorrow.value) {
        borrowForm.value.remark = borrowForm.value.remark + '(电子借阅)';
      }
      directRecord(borrowForm.value).then(response => {
        proxy.$modal.msgSuccess("借阅申请提交成功");
        borrowOpen.value = false;
        resetBorrowForm();
      }).catch(error => {
        console.error('借阅申请失败:', error);
        ElMessage.error('借阅申请失败: ' + (error.message || '未知错误'));
      });
    } else {
      console.log('借阅表单验证失败');
    }
  });
}



// 日志相关
const archiveLogDialogVisible = ref(false)
const archiveLogList = ref([])
const logQueryParams = ref({
  pageNum: 1,
  pageSize: 9999,
  archiveId: null,
})

// 档案柜相关
const showCabinetView = ref(false)
const selectedQuNo = ref('1')
const cabinetList = ref([])
const compartmentList = ref([])
const cabinetCard = ref([])

// 文档导入相关
const documentDialogVisible = ref(false)
const importingDocument = ref(false)
const documentList = ref([])
const mountedCount = ref(0)
const unmountedCount = ref(0)

// 批量导入相关
const batchImportVisible = ref(false)
const selectedRootCategory = ref('')
const selectedCategoryName = ref('')

const selectedSecondCategory = ref('')
const selectedSecondCategoryName = ref('')
const secondLevelCategories = ref([])
const filteredSubClasses = ref([])

const batchDynamicColumns = ref([])
const batchTableData = ref([])
const batchSubmitting = ref(false)
const categoryRoots = ref([])
const categoryTreeData = ref([]) // 添加树形数据

// 初始化
onMounted(() => {
  initLocationOptions('1');

  initPage()
  loadUserInfo() // 加载用户信息
  loadCategoryRoots()
  loadFondsList()
  getUserProfile().then(response => {
    user.value = response.data
  })
  getDocumentInfo()
  getUserListByRoleId({ roleId: 10007 })
})

// 新增：加载全宗列表
const loadFondsList = async () => {
  try {
    const response = await listFonds({
      pageNum: 1,
      pageSize: 9999
    })
    if (response.rows) {
      fondsList.value = response.rows
      // 创建ID到全宗对象的映射
      fondsMap.value = new Map(response.rows.map(fonds => [fonds.id, fonds]))
      console.log('全宗列表:', fondsMap)
    }
  } catch (error) {
    console.error('加载全宗列表失败:', error)
    ElMessage.error('加载全宗列表失败')
  }
}

// 获取全宗名称的辅助方法
const getFondsName = (fondsId) => {
  const fonds = fondsMap.value.get(fondsId)
  return fonds ? `${fonds.bianhao} - ${fonds.name}` : '未知全宗'
}

// 监听门类选择变化（可选，用于调试）
watch(() => queryParams.value.rootCategoryCode, (newVal, oldVal) => {
  if (oldVal && newVal !== oldVal) {
    console.log('大门类已变更，重置小门类选择')
  }
})

watch(() => queryParams.value.categoryCode, (newVal) => {
  console.log('当前选中小门类:', newVal)
})

// 初始化页面
function initPage() {
  const projectIdParam = route.query.projectId
  projectName.value = route.query.projectName || '项目档案'
  queryParams.value.projectId = projectIdParam
  getList()
}

//批量导入档案时自动复制形成单位和移交单位
// 监听第一行的形成单位和移交单位变化
watch(() => {
  if (batchTableData.value.length > 0) {
    const firstRow = batchTableData.value[0];
    return {
      formationUnit: firstRow.formationUnit,
      transferUnit: firstRow.transferUnit
    };
  }
  return null;
}, (newVal) => {
  // 当第一行的形成单位或移交单位发生变化时
  if (newVal) {
    // 从第二行开始，更新所有行的形成单位和移交单位
    for (let i = 1; i < batchTableData.value.length; i++) {
      batchTableData.value[i].formationUnit = newVal.formationUnit;
      batchTableData.value[i].transferUnit = newVal.transferUnit;
    }
  }
}, { deep: true });

// 获取档案列表
function getList() {
  loading.value = true

  // 构建查询参数，过滤掉空值
  const searchParams = Object.keys(queryParams.value).reduce((acc, key) => {
    const value = queryParams.value[key]
    if (value !== undefined && value !== null && value !== '') {
      acc[key] = value
    }
    return acc
  }, {})

  console.log('搜索参数:', searchParams) // 调试用

  getProjectArchives(searchParams)
    .then(response => {
      archiveList.value = response.rows
      total.value = response.total
    })
    .catch(error => {
      console.error('获取项目档案失败:', error)
      ElMessage.error('获取档案列表失败')
    })
    .finally(() => {
      loading.value = false
    })
}

// 通过角色id获取用户列表
const userList = ref([])
function getUserListByRoleId(roleId) {
  listUserByRoleId(roleId).then(response => {
    userList.value = response;
  });
}

// 搜索按钮操作
function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

// 重置按钮操作
function resetQuery() {
  if (proxy.$refs.queryRef) {
    proxy.$refs.queryRef.resetFields()
  }
  // 手动重置所有查询参数
  queryParams.value.danghao = undefined
  queryParams.value.name = undefined
  queryParams.value.rootCategoryCode = undefined
  queryParams.value.categoryCode = undefined
  categoryTreeData.value = []
  handleQuery()
}

// 多选框选中数据
function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.id)
  selectedItems.value = selection
}

// 返回项目列表
function goBack() {
  router.push('/manage/project/xxx-list')
}

// 查看档案详情
function handleDetail(row) {
  router.push({
    path: '/manage/archive/arc-detail',
    query: { id: row.id }
  })
}

// 修改档案
function handleUpdate(row) {
  if (row && row.status === 'Archived') {
    proxy.$modal.msgWarning("已归档的档案不能修改")
    return
  }
  const id = row?.id
  if (id) {
    router.push({
      path: '/manage/archive/arc-edit',
      query: { id: id }
    })
  } else {
    proxy.$modal.msgError("无法获取档案ID")
  }
}

// 删除档案
async function handleDelete(row) {
  if (row && row.status === 'Archived') {
    proxy.$modal.msgWarning("已归档的档案不能删除")
    return
  }

  try {
    // 修改：使用正确的检查方法（与另一个页面一致）
    const fileRes = await listDocumentByArchiveId(row.id);

    // 修改：检查rows数组长度
    if (fileRes.rows && fileRes.rows.length > 0) {
      proxy.$modal.msgWarning("该档案包含文件，请先删除文件")
      return;
    }

    const response = await getArchive(row.id)
    const archiveData = response.data

    if (archiveData.status === 'Archived') {
      proxy.$modal.msgWarning("已归档的档案不能删除")
      return
    }

    await proxy.$modal.confirm(`是否确认删除档案"${archiveData.name || row.id}"?`)

    const shitiLocation = archiveData?.shitiLocation
    const exactLocation = archiveData?.exactLocation

    const deleteResponse = await delArchive(row.id)

    if (deleteResponse.code === 200) {
      // if (shitiLocation) {
      //   try {
      //     await updateCabinetSize(shitiLocation, "delete")
      //     if (exactLocation) {
      //       const compartmentKey = `${shitiLocation}-${exactLocation}`
      //       await updateCompartmentSize(compartmentKey, "delete")
      //     }
      //   } catch (updateError) {
      //     console.warn("柜子容量更新失败:", updateError)
      //   }
      // }
      proxy.$modal.msgSuccess("删除成功")
    } else {
      proxy.$modal.msgError(`删除失败: ${deleteResponse.msg}`)
    }
    getList()
  } catch (error) {
    console.error('删除操作失败:', error)
  }
}

// 获取档案日志
// function getArchiveLog(row) {
//   archiveLogDialogVisible.value = true
//   logQueryParams.archiveId = row.id
//   listArchiveLog(logQueryParams).then(response => {
//     archiveLogList.value = response.rows
//   }).catch(error => {
//     console.error('获取档案日志失败:', error)
//     ElMessage.error('获取日志失败')
//   })
// }

const borrowLogDialogVisible = ref(false)
const borrowLogList = ref([]);
const archiveTypeFilter = ref('all');
const currentArchiveId = ref(null);

/** 查看借阅日志 */
function getBorrowLog(row) {
  borrowLogDialogVisible.value = true;
  currentArchiveId.value = row.id;
  fetchBorrowLogs();
}

/** 获取借阅日志数据 */
function fetchBorrowLogs() {
  listBorrowLogs({ archiveId: currentArchiveId.value }).then(response => {
    borrowLogList.value = response.rows;

    // 添加详细的调试信息
    console.log('原始借阅日志数据:');
    response.rows.forEach(row => {
      const borrowRemark = row.borrowRemark || '';
      const isElectronic = borrowRemark.includes('(电子借阅)');

      console.log({
        id: row.id,
        borrowRemark,
        isElectronic,
        taskType: row.taskType,
        startDate: row.startDate
      });
    });
  }).catch(error => {
    console.error('获取借阅日志失败:', error);
    ElMessage.error('获取借阅日志失败');
  });
}

watch(archiveTypeFilter, (newVal) => {
  console.log(`筛选条件变化: ${newVal}`);
  console.log(`当前筛选后的记录数: ${filteredBorrowLogList.value.length}`);
});

/** 筛选日志列表 */
const filteredBorrowLogList = computed(() => {
  return borrowLogList.value.filter(log => {
    // 确保正确处理 null 值
    const borrowRemark = log.borrowRemark || '';

    // 使用更精确的匹配方式
    const isElectronic = borrowRemark.includes('(电子借阅)');

    console.log(`日志ID: ${log.id}, borrowRemark: ${borrowRemark}, isElectronic: ${isElectronic}`);

    switch (archiveTypeFilter.value) {
      case 'paperElectronic':
        return !isElectronic;
      case 'electronic':
        return isElectronic;
      default:
        return true;
    }
  });
});

// 发起人显示格式化函数
function formatInitiator(initiator) {
  if (!initiator) return '';

  // 处理格式如 "10063:李五" 的情况
  const parts = initiator.split(':');
  if (parts.length > 1) {
    return parts[1]; // 返回冒号后的部分
  }
  return initiator;
}

// 审核员显示格式化函数
function formatHandler(handler) {
  if (!handler) return '';

  // 移除多余的逗号
  return handler.replace(/,$/, '');
}

// 获取档案类型标签样式
function getArchiveTypeTagType(row) {
  const isElectronic = row.remark && row.remark.includes('(电子借阅)');
  return isElectronic ? 'success' : 'warning';
}

// 获取档案类型显示文本
function getArchiveTypeText(row) {
  const borrowRemark = row.borrowRemark || '';
  const isElectronic = borrowRemark.includes('(电子借阅)');
  return isElectronic ? '电子档案' : '纸质+电子档案';
}

/** 查看操作日志 */
function getArchiveLog(archiveId) {
  archiveLogDialogVisible.value = true;

  listOpLogs({ archiveId: archiveId }).then(response => {
    archiveLogList.value = response.rows;
  });
}

// 归档入库日志生成================================================================================
const rukuDialogVisible = ref(false);
const rukuForm = reactive({
  archivist: '',     // 归档人
  archiveInfo: ''    // 归档信息
});
const rukuFormRef = ref();

// 验证规则（简单非空校验）
const rukuRules = {
  archivist: [
    { required: true, message: '归档人不能为空', trigger: 'blur' }
  ],
  archiveInfo: [
    { required: true, message: '归档信息不能为空', trigger: 'blur' }
  ]
};

// 当前正在操作的档案对象
// 在 data 或 ref 中添加弹窗控制和配置项
const guidangDialogVisible = ref(false);
const selectedOptions = ref([]);
const currentRow = ref(null);
// 四性检测配置项
const configOptions = [
  { value: 'archiveName', label: '档案名', category: '档案项目' },
  { value: 'archiveNumber', label: '档号', category: '档案项目' },
  { value: 'rfid', label: 'RFID', category: '档案项目' },
  { value: 'docName', label: '文档名', category: '文档项目' },
  { value: 'fileSize', label: '文件大小', category: '文档项目' },
  { value: 'fileType', label: '文件类型', category: '文档项目' }
];


// 归档操作
const handleGuidang = async (row) => {
  if (row.retentionPeriod && row.secretLevel && row.secretPeroid) {
    // ElMessageBox.confirm('确定要归档吗？', '提示', {
    //   confirmButtonText: '确定',
    //   cancelButtonText: '取消',
    //   type: 'warning'
    // }).then(async () => {
    // })
    currentRow.value = row;
    selectedOptions.value = []; // 重置选择
    guidangDialogVisible.value = true; // 打开自定义弹窗
  } else {
    ElMessage.error('缺少信息，请先鉴定！')
  }

};

// 确认归档操作
const confirmGuidang = async () => {
  if (selectedOptions.value.length === 0) {
    ElMessage.warning('请至少选择一个检测配置项');
    return;
  }

  // 显示加载提示
  const loading = ElLoading.service({
    lock: true,
    text: '归档处理中，此过程可能需要几分钟，请耐心等待...',
    background: 'rgba(0, 0, 0, 0.7)'
  });

  try {
    const configString = selectedOptions.value.join(',');

    // 还是调用原来的 guidang 接口，只是超时时间更长
    await guidang({
      id: currentRow.value.id,
      testConfig: configString
    });

    // 成功后的处理
    await addArchiveLog({
      archiveId: currentRow.value.id,
      taskType: '入库',
      initiator: user.value.userName,
      remark: '归档入库',
      startDate: dayjs().format('YYYY-MM-DD HH:mm:ss'),
    });

    currentRow.value.status = 'Archived';
    ElMessage.success('归档成功');
    guidangDialogVisible.value = false;

  } catch (error) {
    console.error(error);
    // 优化错误提示
    if (error.code === 'ECONNABORTED' || error.message?.includes('timeout')) {
      ElMessage.error('归档处理超时，请稍后刷新页面查看是否成功');
    } else {
      ElMessage.error(error.response?.data?.message || '归档失败');
    }
  } finally {
    loading.close();
  }
};

const cancelGuidang = () => {
  guidangDialogVisible.value = false;
  selectedOptions.value = [];
  currentRow.value = null;
};

// 处理归档按钮点击
const handShangjia = (row) => {
  if (row.carrierType === 'electronic') {
    ElMessage.error('电子档案无需上架！')
    return
  }

  if (!row.rfid) {
    ElMessage.error('请先打印标签！')
    return
  }

  // 重置表单
  resetGuidangForm()

  // 设置当前归档的档案信息
  guidangDialog.archiveId = row.id
  guidangDialog.archiveInfo = row

  // 预加载档案柜数据
  loadCabinetData()

  // 打开弹窗
  guidangDialog.visible = true
}

// 下架操作以及日志生成==========================================================================
const xiajiaDialogVisible = ref(false);
const xiajiaForm = reactive({
  operator: '',     // 归档人
  reason: ''    // 归档信息
});
const xiajiaFormRef = ref();

// 验证规则（简单非空校验）
const xiajiaRules = {
  operator: [
    { required: true, message: '操作人员不能为空', trigger: 'blur' }
  ],
  reason: [
    { required: true, message: '下架原因不能为空', trigger: 'blur' }
  ]
};

const handleXiajia = (row) => {
  currentRow.value = row;
  xiajiaDialogVisible.value = true;
};

const submitXiajia = async () => {
  ElMessageBox.confirm(`确定将档案【${currentRow.value.name}】从
  【${currentRow.value.shitiLocation}-${currentRow.value.exactLocation}】位置下架吗？`, '下架确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await updateArchive({ id: currentRow.value.id, availability: 1 });
      // 更新状态字段，假定 status 字段表示状态
      // updateCabinetSize(currentRow.value.shitiLocation, 'delete');
      // updateCompartmentSize(currentRow.value.shitiLocation + '-' + currentRow.value.exactLocation, 'delete');
      xiajiaDialogVisible.value = false

      ElMessage.success('下架成功')

      await addArchiveLog({
        archiveId: currentRow.value.id,
        taskType: '下架',
        initiator: xiajiaForm.operator,
        remark: xiajiaForm.reason,
        startDate: dayjs().format('YYYY-MM-DD HH:mm:ss'),
        taskStatus: currentRow.value.shitiLocation + '-' + currentRow.value.exactLocation
      });

      getList()
    } catch (error) {
      console.error(error);
    }
  }).catch(() => {
    ElMessage.info('已取消上架')
  })
}

// 6. 修复表单重置方法
const resetGuidangForm = () => {
  // 重置标记
  isSelectingFromCabinet = false;

  guidangForm.quHao = '';
  guidangForm.lieHao = '';
  guidangForm.ceHao = '';
  guidangForm.jieHao = '';
  guidangForm.cengHao = '';
  guidangForm.shitiLocation = '';
  guidangForm.exactLocation = '';
  guidangForm.locationDisplay = '';

  // 重置操作信息
  operationInfo.operator = '';
  operationInfo.reason = '';

  // 初始化默认选项
  initLocationOptions('1'); // 默认第1区的选项

  if (guidangFormRef.value) {
    guidangFormRef.value.resetFields();
  }
  if (operationFormRef.value) {
    operationFormRef.value.resetFields();
  }
};

// 获取载体类型标签样式
const getCarrierTypeTag = (carrierType) => {
  const tagMap = {
    'electronic': 'success',
    'tangible': 'warning',
    'Mixture': 'primary'
  }
  return tagMap[carrierType] || 'info'
}

// 获取载体类型显示名称
const getCarrierTypeLabel = (carrierType) => {
  const labelMap = {
    'electronic': '电子档案',
    'tangible': '实体档案',
    'Mixture': '混合档案'
  }
  return labelMap[carrierType] || '未知类型'
}

// 修改归档表单校验规则 - 根据载体类型动态调整
const guidangRules = computed(() => {
  // 如果是电子档案，不需要位置验证
  if (guidangDialog.archiveInfo.carrierType === 'electronic') {
    return {}
  }

  // 实体档案和混合档案需要位置验证
  return {
    location: [
      {
        required: true,
        validator: (rule, value, callback) => {
          if (!guidangForm.quHao || !guidangForm.lieHao || !guidangForm.ceHao ||
            !guidangForm.jieHao || !guidangForm.cengHao) {
            callback(new Error('请选择完整的位置信息'))
          } else {
            callback()
          }
        },
        trigger: 'change'
      }
    ]
  }
})

const handleUnarchive = async (row) => {
  ElMessageBox.confirm('确定要撤销归档吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {

    if (row.shitiLocation && row.shitiLocation !== '') {
      ElMessage.warning('已上架的档案无法撤销归档！请先下架档案！')
      return
    }

    try {
      unarchive({ id: row.id, status: 'Unarchived' }); // =========撤销归档操作=========复用方法，不是写错
      row.status = 'Unarchived'
      // 更新状态字段，假定 status 字段表示状态
      // updateCabinetSize(row.shitiLocation, 'delete');
      // updateCompartmentSize(row.shitiLocation + '-' + row.exactLocation, 'delete');
      ElMessage.success('撤销归档成功')
    } catch (error) {
      console.error(error);
    }
  })
}

// 电子档案归档方法
const submitElectronicGuidang = async () => {
  try {
    guidangDialog.loading = true

    // 调用电子档案归档接口
    const params = {
      id: guidangDialog.archiveId,
      status: 'Archived' // 归档状态
      // 电子档案不需要位置信息
    }

    await guidang(params)

    ElMessage.success('电子档案归档成功')
    guidangDialog.visible = false

    // 刷新列表
    getList()

  } catch (error) {
    ElMessage.error('电子档案归档失败：' + error.message)
  } finally {
    guidangDialog.loading = false
  }
}

// 实体档案上架方法================================================================
const operationInfo = reactive({
  operator: '',   // 操作人
  reason: ''      // 上架原因
});

const operationRules = {
  reason: [
    { required: true, message: '上架原因不能为空', trigger: 'blur' }
  ],
  operator: [
    { required: true, message: '操作人员不能为空', trigger: 'blur' }
  ]
};

const operationFormRef = ref();

// 7. 修复实体档案上架方法 - 增加详细的错误处理
const submitPhysicalShangjia = async () => {
  try {
    console.log('开始上架，当前表单数据:', { ...guidangForm });
    console.log('操作信息:', { ...operationInfo });

    // 检查操作信息
    if (!operationInfo.operator) {
      ElMessage.error('请选择操作人员');
      return;
    }

    if (!operationInfo.reason) {
      ElMessage.error('请填写上架原因');
      return;
    }

    // 检查必要的位置信息
    if (!guidangForm.shitiLocation || !guidangForm.exactLocation) {
      ElMessage.error('请先选择档案位置');
      return;
    }

    // 表单校验
    await guidangFormRef.value?.validate();
    await operationFormRef.value?.validate();

    guidangDialog.loading = true;

    // 调用实体档案归档接口
    const params = {
      id: guidangDialog.archiveId,
      shitiLocation: guidangForm.shitiLocation,
      exactLocation: guidangForm.exactLocation,
      availability: 2,
    };

    console.log('上架参数:', params);

    const updateResult = await updateArchive(params);
    console.log('更新档案结果:', updateResult);

    // 更新柜子容量信息
    if (guidangForm.shitiLocation) {
      await updateCabinetSize(guidangForm.shitiLocation, "add");
    }
    if (guidangForm.exactLocation) {
      const compartmentLocation = guidangForm.shitiLocation + '-' + guidangForm.exactLocation;
      await updateCompartmentSize(compartmentLocation, "add");
    }

    ElMessage.success('实体档案上架成功');
    guidangDialog.visible = false;

    await addArchiveLog({
      archiveId: guidangDialog.archiveId,
      taskType: '上架',
      initiator: operationInfo.operator,
      remark: operationInfo.reason,
      startDate: dayjs().format('YYYY-MM-DD HH:mm:ss'),
      taskStatus: guidangForm.shitiLocation + '-' + guidangForm.exactLocation
    });

    // 刷新列表
    getList();

  } catch (error) {
    console.error('上架失败详细信息:', error);

    let errorMessage = '上架失败';

    if (error?.response?.data?.msg) {
      errorMessage = error.response.data.msg;
    } else if (error?.message) {
      errorMessage = error.message;
    } else if (typeof error === 'string') {
      errorMessage = error;
    }

    ElMessage.error('实体档案上架失败：' + errorMessage);
  } finally {
    guidangDialog.loading = false;
  }
};

// 修改后的提交上架方法
const submitShangjia = async () => {
  const carrierType = guidangDialog.archiveInfo.carrierType
  const archiveName = guidangDialog.archiveInfo.name

  let confirmMessage = ''

  if (carrierType === 'electronic') {
    confirmMessage = `确定要将电子档案【${archiveName}】进行归档吗？`
  } else {
    confirmMessage = `
    确定要将档案【${archiveName}】上架到【${guidangForm.locationDisplay}】吗？`
  }

  ElMessageBox.confirm(confirmMessage, '上架确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    // 根据载体类型调用不同的归档方法
    if (carrierType === 'electronic') {
      await submitElectronicGuidang()
    } else {
      await submitPhysicalShangjia()
    }
  }).catch(() => {
    ElMessage.info('已取消上架')
  })
}

// 5. 修复位置变化处理方法
const handleLocationChange = () => {
  console.log('处理位置变化:', { ...guidangForm });

  // 生成位置信息 - 确保所有字段都有值
  if (guidangForm.quHao && guidangForm.lieHao && guidangForm.ceHao &&
    guidangForm.jieHao && guidangForm.cengHao) {

    // 生成 shitiLocation: 区号-列号-左右侧
    guidangForm.shitiLocation = `${guidangForm.quHao}-${guidangForm.lieHao}-${guidangForm.ceHao}`;

    // 生成 exactLocation: 节号-层号
    guidangForm.exactLocation = `${guidangForm.jieHao}-${guidangForm.cengHao}`;

    // 生成显示文本
    const qu = iams_shiti_location_quhao.value.find(item => item.value === guidangForm.quHao)?.label || `第${guidangForm.quHao}区`;
    const lie = iams_shiti_location_liehao.value.find(item => item.value === guidangForm.lieHao)?.label || `第${guidangForm.lieHao}列`;
    const ce = iams_shiti_location_zy.value.find(item => item.value === guidangForm.ceHao)?.label || (guidangForm.ceHao === 'A' ? 'A面' : 'B面');
    const jie = iams_shiti_location_jiehao.value.find(item => item.value === guidangForm.jieHao)?.label || `第${guidangForm.jieHao}节`;
    const ceng = iams_shiti_location_cenghao.value.find(item => item.value === guidangForm.cengHao)?.label || `第${guidangForm.cengHao}层`;

    guidangForm.locationDisplay = `${qu} ${lie} ${ce} ${jie} ${ceng}`;

    console.log('生成的位置信息:', {
      shitiLocation: guidangForm.shitiLocation,
      exactLocation: guidangForm.exactLocation,
      locationDisplay: guidangForm.locationDisplay
    });

  } else {
    guidangForm.shitiLocation = '';
    guidangForm.exactLocation = '';
    guidangForm.locationDisplay = '';
    console.log('位置信息不完整，已清空');
  }
};

async function handlePrintRfid(row) {
  printRfid(row.id).then(response => {
    console.log(response.data)
    getList()
  });
}

async function handleReprintRfid(row) {
  try {
    await ElMessageBox.confirm(
        '重新打印RFID标签将清空该档案的现有位置信息（实体位置、精确位置等），是否确认继续？',
        '重要提示',
        {
          confirmButtonText: '确认重新打印',
          cancelButtonText: '取消',
          type: 'warning',
        }
    );

    // 检查档案是否有现有位置信息
    const hasLocation = row.shitiLocation && row.shitiLocation.trim() !== '';

    // 如果有位置信息，需要先更新容量（释放空间）
    if (hasLocation) {
      try {
        // 更新档案柜容量（减少使用量）
        await updateCabinetSize(row.shitiLocation, 'delete');
        await updateCompartmentSize(row.shitiLocation + '-' + row.exactLocation, 'delete');


        console.log(`已释放位置容量: 档案柜=${row.shitiLocation}, 储物格=${row.exactLocation || '无'}`);
      } catch (capacityError) {
        console.error('更新容量时出错:', capacityError);
        ElMessage.error('更新存储容量失败，但将继续重新打印流程');
      }
    }

    // 清空位置信息的参数
    const params = {
      id: row.id,
      shitiLocation: '',
      exactLocation: '',
      availability: 0,
    };

    // 更新档案信息
    const updateResult = await updateArchive(params);

    // 重新打印RFID标签
    printRfid(row.id).then(response => {
      console.log(response.data);
      getList();
      if (hasLocation) {
        ElMessage.success('RFID标签重新打印成功！已释放原存储位置。');
      } else {
        ElMessage.success('RFID标签重新打印成功！');
      }
    }).catch(printError => {
      console.error('打印RFID标签失败:', printError);
      ElMessage.error('RFID标签打印失败，请重试');
    });

  } catch (error) {
    // 用户取消或其他错误
    if (error === 'cancel') {
      ElMessage.info('已取消重新打印');
    } else {
      console.error('重新打印RFID过程中出错:', error);
      ElMessage.error('重新打印过程中出现错误，请重试');
    }
  }
}

// 移动架子的操作handleMove   likang
async function handleMove(row) {
  await handleMoveMeth(row, proxy)
}
// 合并架子
async function handleCombine(row) {
  await handleCombineMeth(row, proxy)
}

// 加载档案柜数据
const loadCabinetData = async () => {
  try {
    const cabinetRes = await listCabinet({
      pageNum: 1,
      pageSize: 9999,
      repositoryId: 10000
    })
    cabinetList.value = cabinetRes.rows

    const compartmentRes = await listCompartment()
    compartmentList.value = compartmentRes.data

    generateCabinetCards()
  } catch (error) {
    console.error('加载档案柜数据失败:', error)
  }
}

// 获取储物格数据 - 适配第2区结构（修复版本）
function getCompartmentsByCabinetAndSide(quNo, colNo, displaySide) {
  if (quNo === 2) {
    // 第2区：只有A面的储物格，需要根据displaySide分层显示
    const cabinet = cabinetList.value.find(cab =>
      cab.quNo == quNo && cab.colNo == colNo && cab.zyNo == 'A'
    );

    if (!cabinet) return [];

    const allCompartments = compartmentList.value.filter(comp => comp.cabinetId === cabinet.id);

    // 根据displaySide过滤出相应的层
    if (displaySide === 'left') {
      // 左侧显示1-10层
      return allCompartments.filter(comp => comp.divNo >= 1 && comp.divNo <= 10);
    } else {
      // 右侧显示11-20层  
      return allCompartments.filter(comp => comp.divNo >= 11 && comp.divNo <= 20);
    }
  } else {
    // 其他区：A面和B面
    const zyNo = displaySide === 'left' ? 'A' : 'B';
    const cabinet = cabinetList.value.find(cab =>
      cab.quNo == quNo && cab.colNo == colNo && cab.zyNo == zyNo
    );

    if (!cabinet) return [];

    return compartmentList.value.filter(comp => comp.cabinetId === cabinet.id);
  }
}

// 获取单元格样式
const getCellClass = (comp) => {
  if (comp.size === 0) return 'cell-green'
  if (comp.size >= 1 && comp.size <= 14) return 'cell-yellow'
  if (comp.size >= 15) return 'cell-red'
  return ''
}

// 添加状态变量
const isGenerating = ref(false)
const generatingProgress = ref(0)

// 切换显示档案柜视图
const toggleCabinetView = () => {
  showCabinetView.value = !showCabinetView.value
  if (showCabinetView.value && cabinetList.value.length === 0) {
    loadCabinetData()
  }
}

// 4. 修复选择储物格方法
function selectCompartment(quNo, colNo, displaySide, comp) {
  // 只允许选择空闲的格子
  // if (comp.size >= 15) {
  //   ElMessage.warning('该位置已满，请选择空闲位置');
  //   return;
  // }

  console.log('选择储物格:', { quNo, colNo, displaySide, comp });

  // 设置标记，防止 watch 清空字段
  isSelectingFromCabinet = true;

  // 先初始化选项数组
  initLocationOptions(quNo);

  // 等待下一个 tick 确保选项数组更新完成
  nextTick(() => {
    // 设置表单数据 - 确保类型一致
    guidangForm.quHao = String(quNo);
    guidangForm.lieHao = String(colNo);

    if (quNo === 2 || quNo === '2') {
      // 第2区统一设置为A面
      guidangForm.ceHao = 'A';
    } else {
      // 其他区根据displaySide设置
      guidangForm.ceHao = displaySide === 'left' ? 'A' : 'B';
    }

    guidangForm.jieHao = String(comp.leNo);
    guidangForm.cengHao = String(comp.divNo);

    console.log('设置后的表单数据:', { ...guidangForm });

    // 再等一个 tick 确保表单值设置完成
    nextTick(() => {
      handleLocationChange();
      console.log('位置变化处理完成:', { ...guidangForm });

      // 重置标记
      isSelectingFromCabinet = false;
    });
  });

  // 关闭档案柜视图
  showCabinetView.value = false;

  ElMessage.success('已选择位置');
}

// 修复后的辅助方法
const getMinIdByQuNoAndColNo = (quNo, colNo) => {
  const matched = cabinetList.value.filter(cab => {
    return cab.quNo === quNo && cab.colNo === colNo;
  });
  if (matched.length === 0) return null;
  return Math.min(...matched.map(cab => cab.id));
}

// 修复generateCabinetCards方法
const generateCabinetCards = () => {
  const mockData = []
  const quNo = parseInt(selectedQuNo.value)

  // 根据区号确定列数
  const maxCols = quNo === 2 ? 13 : 18;

  for (let colNo = 1; colNo <= maxCols; colNo++) {
    mockData.push({
      id: getMinIdByQuNoAndColNo(quNo, colNo),
      quNo: quNo,
      colNo: colNo,
      // capacity: quNo === 2 ? 1920 : 900,
      size: getSize(quNo, colNo)
    })
  }

  cabinetCard.value = mockData
}

// 修复getSize方法
const getSize = (quNo, colNo) => {
  const matched = cabinetList.value.filter(cab => cab.quNo === quNo && cab.colNo === colNo)
  if (matched.length >= 2) {
    return matched[0].size + matched[1].size
  }
  return matched.length > 0 ? matched[0].size : 0
}

// 切换区号
const handleQuNoChange = (val) => {
  selectedQuNo.value = val
  generateCabinetCards()
}

// 批量导入相关方法
const importArchive = () => {
  batchImportVisible.value = true
  selectedFonds.value = '';              // 新增：重置全宗选择
  selectedFondsName.value = '';          // 新增：重置全宗名称
  selectedRootCategory.value = '';
  selectedCategoryName.value = '';
  selectedSecondCategory.value = '';
  selectedSecondCategoryName.value = '';
  secondLevelCategories.value = [];
  filteredSubClasses.value = [];
  batchTableData.value = [];
  categoryRoots.value = [];

  // 新增重置
  selectedSecondCategory.value = ''
  selectedSecondCategoryName.value = ''
  secondLevelCategories.value = []
  filteredSubClasses.value = []

  batchDynamicColumns.value = []
  categoryTreeData.value = []
}

// 加载大类列表
const loadCategoryRoots = async () => {
  try {
    const response = await getCategoryRoots()
    if (response.data) {
      // 对获取到的根节点数组按照code进行升序排序（A, B, C, D, E）
      categoryRoots.value = Array.isArray(response.data)
          ? response.data.sort((a, b) => a.code.localeCompare(b.code))
          : [response.data];
    } else {
      ElMessage.error('获取档案门类失败')
    }
  } catch (error) {
    console.error('加载档案门类大类失败:', error)
    ElMessage.error('加载档案门类大类失败')
  }
}

// 新增：全宗相关变量
const selectedFonds = ref('');        // 当前选择的全宗ID
const selectedFondsName = ref('');    // 当前选择的全宗名称
const allCategoryRoots = ref([]);     // 备份所有门类（用于恢复）

// 处理门类大类选择变化
const handleRootCategoryChange = async (uniqueKey) => {
  // 重置小门类选择
  selectedSecondCategory.value = '';
  selectedSecondCategoryName.value = '';
  secondLevelCategories.value = [];
  filteredSubClasses.value = [];
  categoryTreeData.value = [];

  if (!uniqueKey) {
    return;
  }

  try {
    // 添加日志
    console.log('正在获取门类树，uniqueKey:', uniqueKey);
    // 关键修改：标准化冒号格式
    const standardizedKey = uniqueKey
        .replace(/：/g, ":")      // 将中文全角冒号替换为英文半角冒号
        .replace(/\s+/g, " ")    // 标准化空格
        .trim();

    console.log('标准化后的uniqueKey:', standardizedKey);
    // 直接使用 uniqueKey 调用新接口
    const response = await getCategoryTreeByUniqueKey(standardizedKey);

    if (response.data) {
      categoryTreeData.value = Array.isArray(response.data) ? response.data : [response.data];
      console.log('获取到的门类树:', categoryTreeData.value);
      // 提取二级分类时添加 uniqueKey
      extractSecondLevelCategories(categoryTreeData.value);
      console.log('提取的二级分类:', secondLevelCategories.value);

      const categoryTree = Array.isArray(response.data) ? response.data : [response.data];
      batchDynamicColumns.value = extractLeafNodes(categoryTree);
    } else {
      console.warn('未找到该门类的子类');
      categoryTreeData.value = [];
      secondLevelCategories.value = [];
      batchDynamicColumns.value = [];
      ElMessage.warning('未找到该门类的子类');
    }
  } catch (error) {
    console.error('获取门类树失败:', error);
    ElMessage.error('获取门类子类失败');
  }
};

// 修改提取二级分类方法
const extractSecondLevelCategories = (categories) => {
  const secondLevel = [];
  categories.forEach(category => {
    if (category.children && category.children.length > 0) {
      category.children.forEach(child => {
        // 为每个二级分类添加 uniqueKey
        const uniqueKey = `${child.code}:${child.name}`;
        secondLevel.push({
          id: child.id,
          code: child.code,
          name: child.name,
          uniqueKey: uniqueKey, // 添加唯一标识符
          children: child.children || []
        });
      });
    }
  });
  secondLevelCategories.value = secondLevel;
};

// 修改处理二级分类选择变化的方法
const handleSecondCategoryChange = (uniqueKey) => {
  selectedSecondCategoryName.value = '';
  filteredSubClasses.value = [];

  if (!uniqueKey) {
    selectedCategoryName.value = '';
    return;
  }

  // 根据 uniqueKey 找到选中的二级分类
  const selectedSecond = secondLevelCategories.value.find(c => c.uniqueKey === uniqueKey);
  if (selectedSecond) {
    selectedSecondCategoryName.value = `${selectedSecond.code} - ${selectedSecond.name}`;

    // 提取该二级分类下的所有三级分类（叶子节点）
    filteredSubClasses.value = extractLeafNodes([selectedSecond]);
  }
};

// 提取子类方法 - 保持原有逻辑
const extractLeafNodes = (categories) => {
  const leafNodes = []
  const traverse = (nodes) => {
    nodes.forEach(node => {
      if (!node.children || node.children.length === 0) {
        leafNodes.push({
          id: node.id,
          code: node.code,
          name: node.name
        })
      } else {
        traverse(node.children)
      }
    })
  }
  traverse(categories)
  return leafNodes
}

const generateSubClassRows = async () => {
  if (!selectedFonds.value) {
    ElMessage.warning('请先选择全宗');
    return;
  }

  if (!filteredSubClasses.value.length) {
    ElMessage.warning('请先选择二级分类')
    return
  }

  // 开始生成
  isGenerating.value = true
  generatingProgress.value = 0
  batchTableData.value = []

  const totalCount = filteredSubClasses.value.length
  const batchSize = 20 // 每批处理30条

  try {
    // 显示进度提示
    ElMessage.info(`开始生成 ${totalCount} 条记录...`)

    // 分批处理
    for (let i = 0; i < filteredSubClasses.value.length; i += batchSize) {
      const batch = filteredSubClasses.value.slice(i, i + batchSize)

      // 使用 requestAnimationFrame 避免阻塞UI
      await new Promise(resolve => {
        requestAnimationFrame(() => {
          // 处理当前批次
          const batchRows = batch.map(subClass => {
            const newRow = initialBatchRow()
            newRow.subClassName = subClass.name
            newRow.name = `${projectName.value}-${subClass.name}`
            newRow.subClassCode = subClass.code
            newRow.subClassId = subClass.id
            newRow.selectedCategoryCodes = [subClass.code]
            // 确保设置完整的门类信息
            newRow.rootCategoryUniqueKey = selectedRootCategory.value
            newRow.danghao = generateDanghao(newRow)
            return newRow
          })

          // 添加到表格
          batchTableData.value.push(...batchRows)

          // 更新进度
          generatingProgress.value = Math.round(((i + batch.length) / totalCount) * 100)

          resolve()
        })
      })

      // 添加小延时，让UI有时间更新
      if (i + batchSize < filteredSubClasses.value.length) {
        await new Promise(resolve => setTimeout(resolve, 20))
      }
    }

    ElMessage.success(`成功生成 ${totalCount} 条记录`)
  } catch (error) {
    ElMessage.error('生成失败：' + error.message)
  } finally {
    isGenerating.value = false
    generatingProgress.value = 0
  }
}
// // 生成小类行 - 修改为使用筛选后的小类
// const generateSubClassRows = () => {
//   if (!selectedFonds.value) {
//     ElMessage.warning('请先选择全宗');
//     return;
//   }
//
//   if (!filteredSubClasses.value.length) {
//     ElMessage.warning('请先选择二级分类')
//     return
//   }
//
//   batchTableData.value = []
//
//
//   filteredSubClasses.value.forEach(subClass => {
//     const newRow = initialBatchRow()
//     newRow.subClassName = subClass.name
//     newRow.name = `${projectName.value}-${subClass.name}`
//     newRow.subClassCode = subClass.code
//     newRow.subClassId = subClass.id
//     // 预设选中当前小类（作为数组）
//     newRow.selectedCategoryCodes = [subClass.code]
//     newRow.danghao = generateDanghao(newRow)
//     batchTableData.value.push(newRow)
//   })
//
//   ElMessage.success(`已生成 ${filteredSubClasses.value.length} 个小类的档案记录行`)
// }

// 初始化批量行数据 - 保持原有字段结构，添加多选字段
const initialBatchRow = () => {
  const row = reactive({
    name: '',
    subClassName: '', // 保持原有字段
    subClassId: '',   // 保持原有字段
    subClassCode: '', // 保持原有字段
    selectedCategoryCodes: [], // 新增多选字段
    fondsId: selectedFonds.value || 10009, // 默认使用选择的全宗
    jianhao: 1,
    juanho: '',
    zuojuanUnit: projectName.value,
    carrierType: 'Mixture',
    secretLevel: '2',
    secretPeroid: '100',
    retentionPeriod: '200', // 设置默认值
    formationUnit: '',
    transferUnit: '',
    danghao: '',
    startDate: new Date().toISOString().split('T')[0] // 默认当天日期，格式：YYYY-MM-DD
  })

  // 初始生成档号
  row.danghao = generateDanghao(row)

  return row
}
// 更新小类信息 - 新增方法，当用户修改选择时同步更新相关字段
const updateSubClassInfo = (row) => {
  // 更新档号
  updateDanghao(row)

  // 同步更新原有的单选字段（向后兼容）
  if (row.selectedCategoryCodes.length > 0) {
    const firstSelected = row.selectedCategoryCodes[0]
    const selectedSubClass = findSubClassByCode(firstSelected)
    if (selectedSubClass) {
      row.subClassCode = selectedSubClass.code
      row.subClassName = selectedSubClass.name
      row.subClassId = selectedSubClass.id

      // 新增：同时更新档案名称为项目名-门类名格式
      row.name = `${projectName.value}-${selectedSubClass.name}`
    }
  } else {
    row.subClassCode = ''
    row.subClassName = ''
    row.subClassId = ''
  }
}

// 根据编码查找小类信息 - 辅助方法
const findSubClassByCode = (code) => {
  const findInTree = (nodes) => {
    for (const node of nodes) {
      if (node.code === code) {
        return node
      }
      if (node.children && node.children.length > 0) {
        const found = findInTree(node.children)
        if (found) return found
      }
    }
    return null
  }

  return findInTree(categoryTreeData.value)
}
// 添加新行 - 保持原有逻辑
// const addBatchRowAt = (index) => {
//   const currentRow = batchTableData.value[index]
//   const newRow = initialBatchRow()
//
//   if (currentRow) {
//     newRow.subClassName = currentRow.subClassName || ''
//     newRow.subClassCode = currentRow.subClassCode || ''
//     newRow.subClassId = currentRow.subClassId || ''
//     // newRow.name = currentRow.subClassName || ''
//     // 复制选中的小类
//     newRow.name = currentRow.subClassName ? `${projectName.value}-${currentRow.subClassName}` : ''
//     newRow.selectedCategoryCodes = [...(currentRow.selectedCategoryCodes || [])]
//     newRow.fondsId = currentRow.fondsId || 10009 // 复制全宗ID
//     newRow.secretLevel = currentRow.secretLevel
//     newRow.secretPeroid = currentRow.secretPeroid
//     newRow.danghao = generateDanghao(newRow)
//     newRow.transferUnit = currentRow.transferUnit || ''
//     newRow.formationUnit = currentRow.formationUnit || ''
//   }
//
//   batchTableData.value.splice(index + 1, 0, newRow)
//   ElMessage.success('已添加新行')
// }
// 终极保险方案 - 新增行，几乎不可能卡死
const addBatchRowAt = async (index) => {

  const loadingMessage = ElMessage.info({
    message: '正在添加新行...',
    duration: 0
  })

  try {
    // 第一阶段：准备数据（异步执行，避免阻塞）
    await new Promise(resolve => setTimeout(resolve, 20))
    await new Promise(resolve => requestAnimationFrame(resolve))

    const currentRow = batchTableData.value[index]
    let newRow = null

    // 第二阶段：创建新行对象（分帧执行）
    await new Promise(resolve => setTimeout(resolve, 50))
    await new Promise(resolve => requestAnimationFrame(resolve))

    await new Promise(resolve => {
      requestAnimationFrame(() => {
        try {
          // 创建基础行对象
          newRow = initialBatchRow()
          resolve()
        } catch (error) {
          console.error('创建基础行对象失败:', error)
          resolve()
        }
      })
    })

    // 第三阶段：复制当前行数据（如果存在）
    if (currentRow && newRow) {
      await new Promise(resolve => setTimeout(resolve, 50))
      await new Promise(resolve => requestAnimationFrame(resolve))

      await new Promise(resolve => {
        requestAnimationFrame(() => {
          try {
            // 复制当前行的数据
            newRow.subClassName = currentRow.subClassName || ''
            newRow.subClassCode = currentRow.subClassCode || ''
            newRow.subClassId = currentRow.subClassId || ''
            newRow.name = currentRow.subClassName ? `${projectName.value}-${currentRow.subClassName}` : ''
            newRow.selectedCategoryCodes = [...(currentRow.selectedCategoryCodes || [])]
            newRow.fondsId = currentRow.fondsId || 10009
            newRow.secretLevel = currentRow.secretLevel
            newRow.secretPeroid = currentRow.secretPeroid
            newRow.transferUnit = currentRow.transferUnit || ''
            newRow.formationUnit = currentRow.formationUnit || ''
            resolve()
          } catch (error) {
            console.error('复制行数据失败:', error)
            resolve()
          }
        })
      })
    }

    // 第四阶段：生成档号（可能是复杂计算）
    await new Promise(resolve => setTimeout(resolve, 50))
    await new Promise(resolve => requestAnimationFrame(resolve))

    await new Promise(resolve => {
      requestAnimationFrame(() => {
        try {
          if (newRow) {
            newRow.danghao = generateDanghao(newRow)
          }
          resolve()
        } catch (error) {
          console.error('档号生成失败:', error)
          if (newRow) {
            newRow.danghao = '档号生成失败'
          }
          resolve()
        }
      })
    })

    // 第五阶段：执行实际的插入操作
    await new Promise(resolve => setTimeout(resolve, 70))
    await new Promise(resolve => requestAnimationFrame(resolve))

    if (newRow) {
      batchTableData.value.splice(index + 1, 0, newRow)
    }

    // 第六阶段：等待DOM更新完成
    await new Promise(resolve => setTimeout(resolve, 70))
    await new Promise(resolve => requestAnimationFrame(resolve))

    loadingMessage.close()
    ElMessage.success('已添加新行')

  } catch (error) {
    console.error('添加过程出错:', error)
    loadingMessage.close()
    ElMessage.error('添加失败：' + error.message)
  }
}

// 更新档号
const updateDanghao = (row) => {
  row.danghao = generateDanghao(row)
}


// 终极保险方案 - 几乎不可能卡死
const deleteBatchRow = async (index) => {
  if (batchTableData.value.length <= 1) {
    ElMessage.warning('至少保留一行数据')
    return
  }

  // 数据量检查
  if (batchTableData.value.length > 1000) {
    try {
      await ElMessageBox.confirm('数据量较大，删除操作可能较慢，是否继续？', '性能提示')
    } catch {
      return
    }
  }

  const loadingMessage = ElMessage.info({
    message: '正在删除...',
    duration: 0
  })

  try {
    // 三重异步 + 延时，确保绝对不会卡死
    await new Promise(resolve => setTimeout(resolve, 50))
    await new Promise(resolve => requestAnimationFrame(resolve))
    await new Promise(resolve => setTimeout(resolve, 50))
    await new Promise(resolve => requestAnimationFrame(resolve))

    batchTableData.value.splice(index, 1)

    await new Promise(resolve => setTimeout(resolve, 50))
    await new Promise(resolve => requestAnimationFrame(resolve))

    loadingMessage.close()
    ElMessage.success('已删除该行')

  } catch (error) {
    loadingMessage.close()
    ElMessage.error('删除失败')
  }
}


// 档号生成规则
const getRetentionPeriodCode = (retentionPeriod) => {
  // 确保将值转换为字符串进行比较
  const period = String(retentionPeriod)
  const codeMap = { '200': 'Y', '50': 'D' }
  return codeMap[period] || period || ''
}

const getCategoryCode = (categoryId) => {

  return categoryId ? `SS·${categoryId}` : ''
}

const generateDanghao = (row) => {
  const parts = []
  // // 使用行数据中的全宗ID，如果没有则使用默认值
  // const fondsCode = row.fondsId || 10009
  // parts.push(fondsCode)
  // 修复：通过 fondsId 查找对应的 bianhao
  const fondsCode = getFondsCodeByFondsId(row.fondsId)
  if (fondsCode) parts.push(fondsCode)

  const retentionCode = getRetentionPeriodCode(row.retentionPeriod)
  if (retentionCode) parts.push(retentionCode)

  if (row.subClassCode) {
    parts.push(`SS·${row.subClassCode[0]}`)
  }

  if (row.zuojuanUnit) parts.push(`${row.zuojuanUnit}项目`)
  if (row.juanhao) parts.push(row.juanhao)
  if (row.jianhao) parts.push(row.jianhao.toString())

  return parts.join('-') || '等待输入必要信息'
}

// 根据全宗ID获取全宗编号的方法
const getFondsCodeByFondsId = (fondsId) => {
  if (!fondsId) return ''
  const fonds = fondsList.value.find(f => f.id === fondsId)
  return fonds ? fonds.bianhao : ''
}

const getBatchDanghaoParts = (row) => {
  // const fondsCode = DEFAULT_FONDS_CODE
  const fondsCode = getFondsCodeByFondsId(row.fondsId) || '全宗编号'
  const retentionCode = getRetentionPeriodCode(row.retentionPeriod) || '保管期限'
  const categoryCode = getCategoryCode(row.subClassCode) || '门类编码'

  const zuojuanUnit = row.zuojuanUnit || '组卷单元'
  const juanhao = row.juanhao || '案卷号'
  const jianhao = row.jianhao || '件号'

  return `档号规则: ${fondsCode} - ${retentionCode} - ${categoryCode} - ${zuojuanUnit}项目 - ${juanhao} - ${jianhao}`
}

// 在批量导入弹窗的相关代码区域添加这个计算方法
const sortedCategoryRoots = computed(() => {
  // 使用扩展运算符创建新数组避免修改原数组
  const roots = [...categoryRoots.value];

  // 按编码首字母排序
  return roots.sort((a, b) => {
    // 仅取编码的第一个字符进行比较（假设编码格式如"A-1"）
    const aCode = a.code.charAt(0);
    const bCode = b.code.charAt(0);

    // 直接比较字母顺序
    return aCode.localeCompare(bCode);
  });
});

// 提交批量添加
const submitBatch = async () => {
  try {
    batchSubmitting.value = true
    // 添加调试日志
    console.log('批量数据内容:', batchTableData.value)
    const danghaos = batchTableData.value
        .map(row => row.danghao?.trim())
        .filter(danghao => danghao && danghao !== '等待输入必要信息')

    if (new Set(danghaos).size !== danghaos.length) {
      ElMessage.error('存在重复档号，请检查后重新提交')
      return
    }

    // 数据验证
    let isValid = true
    const messages = []

    batchTableData.value.forEach((row, index) => {
      if (!row.name) {
        messages.push(`第 ${index + 1} 行: 档案名称不能为空`)
        isValid = false
      }
      if (!row.juanhao) {
        messages.push(`第 ${index + 1} 行: 案卷号不能为空`)
        isValid = false
      }
      if (!row.formationUnit) {
        messages.push(`第 ${index + 1} 行: 形成单位不能为空`)
        isValid = false
      }
      if (row.subClassName && !row.subClassId) {
        messages.push(`第 ${index + 1} 行: 缺少小类ID信息`)
        isValid = false
      }
      // 修改门类信息验证
      if (!row.rootCategoryUniqueKey && !row.subClassCode && !row.categoryId) {
        messages.push(`第 ${index + 1} 行: 请选择门类信息`)
        isValid = false
      } else if (row.rootCategoryUniqueKey && !row.selectedCategoryCodes?.length && !row.subClassCode) {
        messages.push(`第 ${index + 1} 行: 请选择子门类`)
        isValid = false
      }
    })

    if (!isValid) {
      ElMessage.error({
        message: messages.join('<br>'),
        dangerouslyUseHTMLString: true,
        duration: 5000
      })
      return
    }

    const payload = batchTableData.value.map(row => {
      // 构建完整的门类信息 - 修复点
      let fullCategoryInfo = '';
        // 添加调试日志
      console.log('当前行数据:', {
        rootCategoryUniqueKey: row.rootCategoryUniqueKey,
        selectedCategoryCodes: row.selectedCategoryCodes,
        subClassCode: row.subClassCode
      })

      // 检查是否有大门类信息和子门类代码
      if (row.rootCategoryUniqueKey && row.selectedCategoryCodes?.length > 0) {
        const rootParts = row.rootCategoryUniqueKey.split(':');
        const rootCode = rootParts[0];
        const rootName = rootParts[1] || '';

        fullCategoryInfo = `${rootCode}:${rootName},${row.selectedCategoryCodes.join(',')}`;
      }
      // 检查是否有子门类代码
      else if (row.subClassCode) {
        fullCategoryInfo = row.subClassCode;
      }
      // 如果都没有，尝试从其他字段推断
      else if (row.categoryId) {
        fullCategoryInfo = row.categoryId;
      }

      return {
        name: row.name,
        danghao: row.danghao,
        jianhao: row.jianhao,
        categoryId: fullCategoryInfo, // 使用完整的门类信息
        createTime: dayjs().format('YYYY-MM-DD HH:mm:ss'),
        juanhao: row.juanhao,
        carrierType: row.carrierType,
        secretLevel: row.secretLevel,
        secretPeroid: row.secretPeroid,
        retentionPeriod: row.retentionPeriod,
        startDate: row.startDate,
        formationUnit: row.formationUnit,
        transferUnit: row.transferUnit,
        projectId: queryParams.value.projectId,
        fondsId: row.fondsId
      }
    })
// 调试日志
    console.log('提交的门类信息:', payload.map(p => p.categoryId))

    const response = await request.post('/manage/archive/batchImport', {
      records: payload,
      projectId: queryParams.value.projectId
    })

    ElMessage.success(`成功添加 ${payload.length} 条档案记录`)
    getList()
    batchImportVisible.value = false
  } catch (error) {
    console.error('批量添加失败:', error)
    ElMessage.error('批量添加失败: ' + (error.response?.data?.msg || error.message))
  } finally {
    batchSubmitting.value = false
  }
}

// 文档相关方法
function openFileLibrary() {
  router.push({
    path: '/manage/document/Doclist',
    query: { projectId: route.query.projectId }
  })
}

function importDocument() {
  if (importingDocument.value) {
    ElMessage.info('当前有上传任务正在进行')
    return
  }
  documentDialogVisible.value = true
}

function downloadDocumentTemplate() {
  axios({
    method: 'get',
    url: `${import.meta.env.VITE_APP_BASE_API}/minio/download`,
    responseType: 'blob',
    headers: {
      'Authorization': headers.value.Authorization,
    }
  }).then(response => {
    const url = window.URL.createObjectURL(new Blob([response.data]))
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', "文档导入模板，上传前记得在这更新成您的项目名.zip")
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
  }).catch(error => {
    console.error('下载失败:', error)
  })
}

function getDocumentInfo() {
  listDocumentByProjectId({ pageNum: 1, pageSize: 9999, projectId: route.query.projectId }).then(response => {
    documentList.value = response.rows
    mountedCount.value = documentList.value.filter(doc => doc.categoryCode && doc.categoryCode.trim() !== '').length
    unmountedCount.value = documentList.value.filter(doc => !doc.categoryCode || doc.categoryCode.trim() === '').length
  })
}

async function handleDirectoryChange(event) {
  importingDocument.value = true
  const files = Array.from(event.target.files)
  if (files.length > 0) {
    documentDialogVisible.value = false
    // 批量上传文件
    await processFilesAndUpload(files)
    // 提示上传完毕
    proxy.$modal.msgSuccess("上传完毕")
    importingDocument.value = false
    getDocumentInfo()
  }
}

function handleDrop(event) {
  const files = Array.from(event.dataTransfer.files)
  if (files.length > 0) {
    processFilesAndUpload(files)
  }
  documentDialogVisible.value = false
}

async function processFilesAndUpload(files) {
  const dirMap = new Map()

  for (const file of files) {
    const fullPath = file.webkitRelativePath || ''
    const pathParts = fullPath.split('/')

    if (pathParts.length >= 3) {
      const projectName = pathParts[0]
      const secondLevelDir = pathParts[1]

      let categoryCode = ''
      if (secondLevelDir === '需要特殊处理的文档请放在这个文件夹下' || !/^[A-Z]/.test(secondLevelDir)) {
        categoryCode = ''
      } else {
        categoryCode = secondLevelDir.slice(0, 5)
      }

      file.projectName = projectName
      file.categoryCode = categoryCode

      const dirKey = `${projectName}/${secondLevelDir}`
      if (!dirMap.has(dirKey)) {
        dirMap.set(dirKey, {
          projectName,
          dirName: secondLevelDir,
          files: []
        })
      }

      dirMap.get(dirKey).files.push(file)
    }
  }

  const validDirs = [...dirMap.entries()]
    .filter(([_, dirInfo]) => dirInfo.files.length > 0)
    .map(([_, dirInfo]) => dirInfo)

  if (validDirs.length === 0) {
    ElMessage.warning('未找到有效的文件')
    return
  }

  const pdfFiles = validDirs.flatMap(dirInfo => dirInfo.files)
  await uploadPdfFiles(pdfFiles)
}

async function uploadPdfFiles(files) {
  for (const file of files) {
    const manager = new UploadManager(file)
    await manager.initialize()
    await manager.startUpload()
  }
}

// 文件上传管理器类
class UploadManager {
  constructor(file, isEncrypted = false) {
    this.file = file
    this.isEncrypted = isEncrypted
    this.chunkSize = this.calculateDynamicChunkSize(file.size)
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
    this.onProgress = null
  }

  async initialize() {
    this.fileHash = await this.calculateHash()
    this.loadLocalProgress()

    const storedParams = localStorage.getItem(`encParams-${this.fileHash}`)
    if (storedParams) {
      const { salt } = JSON.parse(storedParams)
      this.salt = CryptoJS.enc.Hex.parse(salt)
      this.encryptionKey = this.deriveKey(this.salt)
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
    const sizeMB = fileSize / 1024 / 1024
    if (sizeMB > 1024) return 20 * 1024 * 1024
    if (sizeMB > 100) return 10 * 1024 * 1024
    return 5 * 1024 * 1024
  }

  async validateEncryption() {
    if (!this.salt || this.salt.words.length !== 4) {
      throw new Error('Salt未正确生成')
    }
  }

  async generateEncryptionParams() {
    this.salt = CryptoJS.lib.WordArray.random(128 / 8)
    this.encryptionKey = this.deriveKey(this.salt)
    localStorage.setItem(`encParams-${this.fileHash}`, JSON.stringify({
      salt: CryptoJS.enc.Hex.stringify(this.salt),
      encryptionKey: CryptoJS.enc.Hex.stringify(this.encryptionKey)
    }))
  }

  deriveKey(salt) {
    return CryptoJS.PBKDF2(
      CryptoJS.enc.Utf8.parse("secure-passphrase-" + this.fileHash),
      salt,
      { keySize: 8, iterations: 100000, hasher: CryptoJS.algo.SHA256 }
    )
  }

  async calculateHash() {
    return new Promise((resolve) => {
      const spark = new SparkMD5.ArrayBuffer()
      const reader = new FileReader()
      const chunkSize = 5 * 1024 * 1024
      const samples = []
      const SPARSE_SAMPLING_INTERVAL = this.file.size > 1_000_000_000 ? 20 : 10

      for (let offset = 0; offset < this.file.size; offset += chunkSize * SPARSE_SAMPLING_INTERVAL) {
        samples.push(offset)
      }

      samples.push(0)
      samples.push(Math.floor(this.file.size / 2))
      samples.push(Math.max(0, this.file.size - chunkSize))

      const uniqueSamples = [...new Set(samples)].sort((a, b) => a - b)

      let current = 0
      const loadNext = () => {
        if (current >= uniqueSamples.length) {
          resolve(spark.end())
          return
        }
        const start = uniqueSamples[current]
        const end = Math.min(start + chunkSize, this.file.size)
        const blob = this.file.slice(start, end)
        reader.readAsArrayBuffer(blob)
      }

      reader.onload = (e) => {
        spark.append(e.target.result)
        current++
        loadNext()
      }

      loadNext()
    })
  }

  async startUpload() {
    try {
      const generator = this.chunkGenerator()
      this.prepareChunks(generator)
      await this.uploadAllChunks()
      const result = await this.mergeFile()
      return result === 200
    } catch (error) {
      console.error('上传失败:', error)
      throw error
    }
  }

  * chunkGenerator() {
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
        this.chunks.push({
          index,
          chunk,
          progress: 100,
          status: 'success'
        })
      }
    }
  }

  async uploadAllChunks() {
    const uploadTasks = this.chunks
      .filter(chunk => chunk.status !== 'success')
      .map(chunk => this.limiter(() =>
        this.uploadChunkWithRetry(chunk).finally(() => {
          this.updateTotalProgress()
        })
      ))

    await Promise.all(uploadTasks)
  }

  async uploadChunkWithRetry(chunk) {
    for (let retry = 0; retry <= MAX_RETRY; retry++) {
      try {
        const response = await this.uploadChunk(chunk)
        if (response.data.code === 500) {
          throw new Error('上传失败')
        }
        return response
      } catch (error) {
        if (error.message.includes('Network Error')) {
          throw new Error('网络中断')
        }
        if (retry === MAX_RETRY) {
          throw new Error(`分片 ${chunk.index} 上传失败: ${error.message}`)
        }
        await new Promise(resolve =>
          setTimeout(resolve, 1000 * Math.pow(2, retry))
        )
      }
    }
  }

  async uploadChunk(chunk) {
    let processedChunk = chunk.chunk
    const formData = new FormData()

    if (this.isEncrypted) {
      processedChunk = await this.encryptChunk(chunk)
      console.assert(
        processedChunk.size % 16 === 0,
        `无效的加密块大小: ${processedChunk.size} bytes`
      )
      if (chunk.index === 0) {
        formData.append('salt', CryptoJS.enc.Hex.stringify(this.salt))
      }
    }

    formData.append('file', processedChunk)
    formData.append('hash', this.fileHash)
    formData.append('index', chunk.index.toString())

    return axios.post(`${import.meta.env.VITE_APP_BASE_API}/minio/upload`, formData, {
      headers: {
        ...headers.value,
        'X-Encrypted': this.isEncrypted.toString()
      },
      onUploadProgress: (progressEvent) => {
        const percent = Math.round(
          (progressEvent.loaded / progressEvent.total) * 100
        )
        chunk.progress = percent
        this.updateTotalProgress()
      }
    })
  }

  async blobToWordArray(blob) {
    try {
      const arrayBuffer = await blob.arrayBuffer()
      const uint8Array = new Uint8Array(arrayBuffer)
      return CryptoJS.lib.WordArray.create(uint8Array)
    } catch (error) {
      console.error('Blob转换失败:', error)
      throw new Error('文件分片读取失败')
    }
  }

  wordArrayToBlob(wordArray) {
    try {
      const uint8Array = new Uint8Array(wordArray.sigBytes)
      let offset = 0

      wordArray.words.forEach((word) => {
        uint8Array[offset++] = (word >> 24) & 0xff
        uint8Array[offset++] = (word >> 16) & 0xff
        uint8Array[offset++] = (word >> 8) & 0xff
        uint8Array[offset++] = word & 0xff
      })

      return new Blob([uint8Array.slice(0, wordArray.sigBytes)], {
        type: 'application/octet-stream'
      })
    } finally {
      wordArray.words = null
      wordArray.sigBytes = 0
    }
  }

  async encryptChunk(chunk) {
    try {
      const ivSource = CryptoJS.SHA256(
        CryptoJS.enc.Utf8.parse(this.fileHash + chunk.index.toString())
      ).toString()
      const chunkIV = CryptoJS.enc.Hex.parse(ivSource.slice(0, 32))
      const chunkData = await this.blobToWordArray(chunk.chunk)
      const encrypted = CryptoJS.AES.encrypt(
        chunkData,
        this.encryptionKey,
        {
          iv: chunkIV,
          mode: CryptoJS.mode.CBC,
          padding: CryptoJS.pad.Pkcs7
        }
      )
      return this.wordArrayToBlob(encrypted.ciphertext)
    } catch (error) {
      console.error('分片加密失败:', error)
      throw error
    }
  }

  updateTotalProgress() {
    const totalLoaded = this.chunks.reduce((sum, chunk) => {
      return sum + (chunk.progress / 100) * chunk.chunk.size
    }, 0)
    const totalSize = this.file.size
    const percentage = totalSize > 0
      ? Math.round((totalLoaded / totalSize) * 100)
      : 0
    if (this.onProgress) {
      this.onProgress(Math.min(percentage, 99))
    }
  }

  generateUniqueFileName(originalFileName) {
    const timestamp = Date.now()
    const randomNum = Math.floor(Math.random() * 9999).toString().padStart(4, '0')
    const uniquePrefix = `${timestamp}_${randomNum}`
    const lastDotIndex = originalFileName.lastIndexOf('.')
    const extension = lastDotIndex > -1 ? originalFileName.substring(lastDotIndex) : ''
    const nameWithoutExt = lastDotIndex > -1 ? originalFileName.substring(0, lastDotIndex) : originalFileName
    return `${uniquePrefix}_${nameWithoutExt}${extension}`
  }

  async mergeFile() {
    const minioFileName = this.generateUniqueFileName(this.file.name)
    const payload = {
      filename: minioFileName,
      originalFilename: this.file.name,
      fileHash: this.fileHash,
      totalChunks: Math.ceil(this.file.size / this.chunkSize),
      encrypted: this.isEncrypted,
      uploadedChunksCount: this.uploadedChunks.size
    }

    const response = await axios.post(
      `${import.meta.env.VITE_APP_BASE_API}/minio/merge`,
      payload,
      {
        headers: {
          'Authorization': headers.value.Authorization,
          'X-Encrypted': this.isEncrypted.toString()
        }
      }
    )

    if (response.data.code === 200) {
      const filePath = response.data.msg
      await this.createDocumentRecord(this.file, filePath, null, minioFileName)
      this.cleanLocalProgress()
      return 200
    } else {
      console.error('合并文件失败，文档上传失败')
      return 500
    }
  }

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
    localStorage.removeItem(`encParams-${this.fileHash}`)
  }

  async createDocumentRecord(rawFile, filePath, content, minioFileName) {
    if (!rawFile || !rawFile.name) {
      throw new Error('无效的文件对象，缺少必要属性')
    }
    if (!minioFileName) {
      throw new Error('minio文件名不能为空')
    }

    const getFileType = () => {
      const mimeType = rawFile.type.split('/').pop()
      if (mimeType && mimeType !== 'octet-stream') {
        return mimeType.toLowerCase()
      }
      const ext = rawFile.name.split('.').pop()
      return ext ? ext.toLowerCase() : 'unknown'
    }

    if (rawFile.categoryCode == '') {
      const docData = {
        archiveId: '',
        projectId: route.query.projectId,
        categoryCode: '',
        createBy: user.value.userName,
        name: rawFile.name,
        minioName: minioFileName,
        fileType: getFileType(),
        fileSize: Math.round(rawFile.size / 1024 * 10) / 10,
        filePath: filePath,
        content: content,
        uploadTime: new Date().toISOString(),
        encryptFlag: isEncrypt.value ? 1 : 0
      }

      if (!docData.filePath || docData.fileSize <= 0) {
        throw new Error('无效的文档参数: ' + JSON.stringify(docData))
      }

      try {
        await addDocument(docData)
      } catch (error) {
        console.error('[ERROR] 文档记录创建失败:', error)
        throw new Error(`创建失败: ${error.message}`)
      }
    } else {
      const archives = await selectArchiveByCategoryId({
        categoryId: rawFile.categoryCode,
        projectId: route.query.projectId
      })

      if (!archives || archives.length === 0) {
        const docData = {
          archiveId: '',
          projectId: route.query.projectId,
          categoryCode: rawFile.categoryCode,
          createBy: user.value.userName,
          name: rawFile.name,
          minioName: minioFileName,
          fileType: getFileType(),
          fileSize: Math.round(rawFile.size / 1024 * 10) / 10,
          filePath: filePath,
          content: content,
          uploadTime: new Date().toISOString(),
          encryptFlag: isEncrypt.value ? 1 : 0
        }

        if (!docData.filePath || docData.fileSize <= 0) {
          throw new Error('无效的文档参数: ' + JSON.stringify(docData))
        }

        try {
          await addDocument(docData)
        } catch (error) {
          console.error('[ERROR] 文档记录创建失败:', error)
          throw new Error(`创建失败: ${error.message}`)
        }
      } else {
        for (const archive of archives) {
          const docData = {
            archiveId: archive.id,
            projectId: route.query.projectId,
            categoryCode: rawFile.categoryCode,
            createBy: user.value.userName,
            name: rawFile.name,
            minioName: minioFileName,
            fileType: getFileType(),
            fileSize: Math.round(rawFile.size / 1024 * 10) / 10,
            filePath: filePath,
            content: content,
            uploadTime: new Date().toISOString(),
            encryptFlag: isEncrypt.value ? 1 : 0
          }

          if (!docData.filePath || docData.fileSize <= 0) {
            throw new Error('无效的文档参数: ' + JSON.stringify(docData))
          }

          try {
            await addDocument(docData)
          } catch (error) {
            console.error('[ERROR] 文档记录创建失败:', error)
            throw new Error(`创建失败: ${error.message}`)
          }
        }
      }
    }
  }
}

import { Document, Check, Close, Warning, Folder, TrendCharts, Download } from '@element-plus/icons-vue'
import { fourCharacteristicsTest, printRfid } from '@/api/manage/archive.js'
import { listFonds } from '@/api/manage/fonds.js' //

// 添加全宗相关状态
const fondsList = ref([])
const fondsMap = ref(new Map()) // 用于快速查找全宗信息

// 弹窗控制
const testReportDialogVisible = ref(false)
const testReportLoading = ref(false)
const exportLoading = ref(false)

// 检测报告数据
const testReportData = reactive({
  archiveInfo: {},
  testTime: '',
  documentCount: 0,
  authenticity: {},
  integrity: {},
  usability: {},
  security: {},
  documents: []
})

// 处理四性检测
async function handleCharacteristicsTest(row) {
  testReportDialogVisible.value = true
  testReportLoading.value = true

  try {
    const response = await fourCharacteristicsTest(row.id)
    console.log('检测报告数据:', response.data)

    // 更新报告数据
    Object.assign(testReportData, {
      archiveInfo: {
        archiveNumber: row.archiveNumber || row.id,
        archiveName: row.archiveName || row.name,
        rfid: row.rfid,
        projectId: row.projectId
      },
      testTime: getCurrentDateTime(),
      ...response.data
    })
  } catch (error) {
    console.error('获取检测报告失败:', error)
    ElMessage.error('获取检测报告失败')
  } finally {
    testReportLoading.value = false
  }
}

// 获取状态图标
function getStatusIcon(status) {
  switch (status) {
    case 'passed': return '✓'
    case 'failed': return '✗'
    case 'warning': return '!'
    default: return '?'
  }
}

// 获取状态颜色
function getStatusColor(status) {
  switch (status) {
    case 'passed': return '#67c23a'
    case 'failed': return '#f56c6c'
    case 'warning': return '#e6a23c'
    default: return '#909399'
  }
}

// 获取状态文本
function getStatusText(status) {
  switch (status) {
    case 'passed': return '通过'
    case 'failed': return '失败'
    case 'warning': return '警告'
    default: return '未知'
  }
}

// 获取结果文本
function getResultText(status) {
  switch (status) {
    case 'passed': return '合格'
    case 'failed': return '不合格'
    case 'warning': return '待改进'
    default: return '未知'
  }
}

// 获取总体得分
function getOverallScore() {
  const tests = [
    testReportData.authenticity?.status,
    testReportData.integrity?.status,
    testReportData.usability?.status,
    testReportData.security?.status
  ]

  const scores = tests.map(status => {
    switch (status) {
      case 'passed': return 25
      case 'warning': return 15
      case 'failed': return 0
      default: return 20
    }
  })

  return scores.reduce((sum, score) => sum + score, 0)
}


/**
 * 处理全宗选择变化 - 新增方法
 */
const handleFondsChangeInBatch = async (fondsId) => {
  console.log('批量导入中全宗选择变化:', fondsId);

  // 重置后续选择
  selectedRootCategory.value = '';
  selectedCategoryName.value = '';
  selectedSecondCategory.value = '';
  selectedSecondCategoryName.value = '';
  secondLevelCategories.value = [];
  filteredSubClasses.value = [];
  categoryTreeData.value = [];

  // 清空表格数据
  batchTableData.value = [];

  if (fondsId) {
    try {
      // 设置全宗名称用于显示
      const selectedFonds = fondsList.value.find(f => f.id === fondsId);
      selectedFondsName.value = selectedFonds ? `${selectedFonds.name} (${selectedFonds.bianhao})` : '';

      // 加载该全宗的门类模板
      await loadFondsCategoryTemplateForBatch(fondsId);

      // 同步到表格中的全宗选择
      syncFondsToTable(fondsId);
    } catch (error) {
      console.error('加载全宗门类模板失败:', error);
      proxy.$modal.msgError('加载全宗门类模板失败');
    }
  } else {
    // 清空全宗相关信息
    selectedFondsName.value = '';
    categoryRoots.value = [];
  }
};

/**
 * 加载全宗门类模板 - 新增方法
 */
async function loadFondsCategoryTemplateForBatch(fondsId) {
  try {
    console.log('开始加载批量导入的全宗门类模板，fondsId:', fondsId);
    const response = await getFondsCategories(fondsId);
    console.log('全宗门类模板响应:', response);

    if (response.data && response.data.length > 0) {
      // 使用全宗配置的门类作为可选项
      // 添加唯一标识符
      categoryRoots.value = response.data.map(item => ({
        ...item,
        uniqueKey: `${item.code}:${item.name}`
      }));
      console.log('设置全宗门类模板:', categoryRoots.value);
    } else {
      // 如果全宗没有配置门类模板，显示提示并清空选项
      categoryRoots.value = [];
      proxy.$modal.msgWarning('该全宗暂未配置门类模板，请先配置门类模板');
    }
  } catch (error) {
    console.error('加载全宗门类模板失败:', error);
    proxy.$modal.msgError('加载全宗门类模板失败');
    categoryRoots.value = [];
  }
}

/**
 * 同步全宗到表格 - 新增方法
 */
function syncFondsToTable(fondsId) {
  // 将选择的全宗同步到所有表格行
  batchTableData.value.forEach(row => {
    if (!row.fondsId) { // 只有未设置全宗的行才自动设置
      row.fondsId = fondsId;
      updateDanghao(row); // 更新档号
    }
  });
}
// 获取总体状态
function getOverallStatus() {
  const score = getOverallScore()
  if (score >= 90) return '优秀'
  if (score >= 75) return '良好'
  if (score >= 60) return '合格'
  return '不合格'
}

// 获取总体状态样式类
function getOverallStatusClass() {
  const score = getOverallScore()
  if (score >= 90) return 'score-excellent'
  if (score >= 75) return 'score-good'
  if (score >= 60) return 'score-pass'
  return 'score-fail'
}

// 获取评估总结
function getAssessmentSummary() {
  const failedTests = [
    testReportData.authenticity?.status,
    testReportData.integrity?.status,
    testReportData.usability?.status,
    testReportData.security?.status
  ].filter(status => status === 'failed')

  const warningTests = [
    testReportData.authenticity?.status,
    testReportData.integrity?.status,
    testReportData.usability?.status,
    testReportData.security?.status
  ].filter(status => status === 'warning')

  const score = getOverallScore()

  if (failedTests.length > 0) {
    return `档案存在 ${failedTests.length} 项检测不通过，需要立即处理相关问题后重新检测。`
  } else if (warningTests.length > 0) {
    return `档案检测基本通过，存在 ${warningTests.length} 项警告，建议及时优化处理以确保档案质量。`
  } else if (score >= 90) {
    return '档案四性检测全部通过，档案状态优秀，完全符合归档标准。'
  } else {
    return '档案检测通过，档案状态良好，符合归档要求，建议定期进行四性检测。'
  }
}

// 获取当前日期
function getCurrentDate() {
  const now = new Date()
  return now.getFullYear().toString() +
    (now.getMonth() + 1).toString().padStart(2, '0') +
    now.getDate().toString().padStart(2, '0')
}

// 获取当前日期时间
function getCurrentDateTime() {
  const now = new Date()
  return now.getFullYear() + '-' +
    (now.getMonth() + 1).toString().padStart(2, '0') + '-' +
    now.getDate().toString().padStart(2, '0') + ' ' +
    now.getHours().toString().padStart(2, '0') + ':' +
    now.getMinutes().toString().padStart(2, '0') + ':' +
    now.getSeconds().toString().padStart(2, '0')
}

// 导出报告
async function exportReport() {
  exportLoading.value = true
  try {
    // 这里可以调用后端导出接口
    // const response = await exportTestReport(testReportData)

    // 临时实现：生成并下载HTML报告
    const reportHtml = generateReportHtml()
    downloadHtmlReport(reportHtml, `四性检测报告_${testReportData.archiveInfo.archiveNumber}_${getCurrentDate()}.html`)

    ElMessage.success('报告导出成功')
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('导出失败')
  } finally {
    exportLoading.value = false
  }
}

// 生成报告HTML
function generateReportHtml() {
  return `
    <!DOCTYPE html>
    <html>
    <head>
      <meta charset="UTF-8">
      <title>档案四性检测报告</title>
      <style>
        body { font-family: Arial, sans-serif; margin: 20px; }
        .report-header { text-align: center; margin-bottom: 30px; }
        .report-title { color: #2c3e50; font-size: 24px; margin: 10px 0; }
        table { width: 100%; border-collapse: collapse; margin: 20px 0; }
        th, td { border: 1px solid #ddd; padding: 12px; text-align: left; }
        th { background-color: #f5f5f5; font-weight: bold; }
        .status-pass { color: #67c23a; }
        .status-warning { color: #e6a23c; }
        .status-fail { color: #f56c6c; }
        .overall-assessment { margin-top: 30px; padding: 20px; background: #f9f9f9; border-radius: 8px; }
      </style>
    </head>
    <body>
      <div class="report-header">
        <div>报告编号：AR-${getCurrentDate()}-001</div>
        <h1 class="report-title">档案四性检测报告</h1>
        <p>Archive Quadruple Properties Test Report</p>
      </div>

      <h2>档案基本信息</h2>
      <table>
        <tr><td>档案编号</td><td>${testReportData.archiveInfo.archiveNumber}</td></tr>
        <tr><td>档案名称</td><td>${testReportData.archiveInfo.archiveName}</td></tr>
        <tr><td>RFID标识</td><td>${testReportData.archiveInfo.rfid}</td></tr>
        <tr><td>检测时间</td><td>${testReportData.testTime}</td></tr>
      </table>

      <h2>检测结果</h2>
      <table>
        <tr><th>检测项目</th><th>检测结果</th><th>详细说明</th></tr>
        <tr>
          <td>真实性检测</td>
          <td class="status-${testReportData.authenticity?.status || 'pass'}">${getResultText(testReportData.authenticity?.status || 'passed')}</td>
          <td>${testReportData.authenticity?.description || '文档内容未被篡改'}</td>
        </tr>
        <tr>
          <td>完整性检测</td>
          <td class="status-${testReportData.integrity?.status || 'pass'}">${getResultText(testReportData.integrity?.status || 'passed')}</td>
          <td>${testReportData.integrity?.description || '文件结构完整'}</td>
        </tr>
        <tr>
          <td>可用性检测</td>
          <td class="status-${testReportData.usability?.status || 'warning'}">${getResultText(testReportData.usability?.status || 'warning')}</td>
          <td>${testReportData.usability?.description || '存在轻微格式问题'}</td>
        </tr>
        <tr>
          <td>安全性检测</td>
          <td class="status-${testReportData.security?.status || 'pass'}">${getResultText(testReportData.security?.status || 'passed')}</td>
          <td>${testReportData.security?.description || '访问控制正常'}</td>
        </tr>
      </table>

      <div class="overall-assessment">
        <h2>总体评估</h2>
        <p><strong>综合得分：</strong>${getOverallScore()}分</p>
        <p><strong>评估结果：</strong>${getOverallStatus()}</p>
        <p><strong>评估说明：</strong>${getAssessmentSummary()}</p>
      </div>

      <div style="margin-top: 40px; text-align: center; color: #666; font-size: 12px;">
        <p>本报告由档案管理系统自动生成 - 生成时间：${getCurrentDateTime()}</p>
      </div>
    </body>
    </html>
  `
}

// 下载HTML报告
function downloadHtmlReport(htmlContent, filename) {
  const blob = new Blob([htmlContent], { type: 'text/html;charset=utf-8' })
  const link = document.createElement('a')
  link.href = URL.createObjectURL(blob)
  link.download = filename
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  URL.revokeObjectURL(link.href)
}

// 暴露方法供外部调用
defineExpose({
  handleCharacteristicsTest
})

</script>

<style scoped>
.app-container {
  padding: 20px;
}

.flex {
  display: flex;
}

.items-center {
  align-items: center;
}

.mb-4 {
  margin-bottom: 1rem;
}

.mr-3 {
  margin-right: 0.75rem;
}

.text-lg {
  font-size: 1.125rem;
}

.font-medium {
  font-weight: 500;
}

.ml-2 {
  margin-left: 0.5rem;
}

/* 归档相关样式 */
.guidang-content {
  padding: 10px 0;
}

.check-empty-link {
  margin-bottom: 20px;
  padding: 10px;
  background-color: #f0f9ff;
  border-radius: 4px;
  text-align: center;
}

.location-selector {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.location-item {
  width: 110px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
}

.cabinet-view-container {
  margin: 20px 0;
  padding: 15px;
  background-color: #f5f7fa;
  border-radius: 4px;
  max-height: 400px;
  overflow-y: auto;
}

.qu-selector {
  margin-bottom: 15px;
  text-align: center;
}

.cabinet-grid {
  margin-top: 10px;
}

.mini-cabinet-card {
  background: white;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  padding: 8px;
  cursor: pointer;
  transition: all 0.3s;
}

.mini-cabinet-card:hover {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.mini-card-header {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  margin-bottom: 6px;
  color: #606266;
}

.capacity-info {
  color: #909399;
  font-size: 11px;
}

.mini-card-body {
  display: flex;
  align-items: center;
  gap: 5px;
}

.mini-side {
  flex: 1;
}

.side-label {
  text-align: center;
  font-size: 11px;
  color: #909399;
  margin-bottom: 4px;
}

.mini-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  grid-template-rows: repeat(6, 1fr);
  grid-auto-flow: column;
  gap: 2px;
}

.mini-cell {
  width: 12px;
  height: 12px;
  border: 1px solid #dcdfe6;
  cursor: pointer;
  transition: all 0.2s;
}

.mini-cell:hover {
  transform: scale(1.2);
  border-color: #409EFF;
}

.mini-cell.cell-green:hover {
  box-shadow: 0 0 5px rgba(103, 194, 58, 0.5);
}

.mini-divider {
  width: 1px;
  height: 80px;
  background-color: #dcdfe6;
}

.cabinet-legend {
  margin-top: 10px;
  display: flex;
  justify-content: center;
  gap: 20px;
  font-size: 12px;
  color: #606266;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 5px;
}

.legend-box {
  display: inline-block;
  width: 16px;
  height: 16px;
  border: 1px solid #dcdfe6;
}

.cell-green {
  background-color: #67c23a;
}

.cell-yellow {
  background-color: #e6a23c;
}

.cell-red {
  background-color: #f56c6c;
}

/* 操作按钮间距 */
.el-table .cell .el-button+.el-button {
  margin-left: 5px;
}

.button-group .el-button:last-child {
  margin-right: 0;
}

.upload-container {
  border: 2px dashed #ccc;
  padding: 20px;
  text-align: center;
}

.batch-header {
  margin-bottom: 20px;
  padding: 15px;
  background-color: #f8f8f8;
  border-radius: 4px;
  border: 1px solid #ebeef5;
}

.batch-table-container {
  max-height: 60vh;
  overflow-y: auto;
  border: 1px solid #ebeef5;
  border-radius: 4px;
}

.batch-table-footer {
  margin-top: 20px;
  text-align: right;
  padding: 10px 0;
}

.row-actions {
  display: flex;
  gap: 8px;
  justify-content: center;
  align-items: center;
}

.action-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  text-align: center;
}

/* 确保图标和文字垂直居中 */
.action-btn .el-icon {
  margin-right: 4px;
}

.tree-select-node {
  display: flex;
  align-items: center;
  gap: 8px;
}

.node-code {
  color: #909399;
  font-size: 12px;
  background: #f0f2f5;
  padding: 2px 6px;
  border-radius: 2px;
}

.node-name {
  color: #303133;
}

/* 搜索表单样式优化 */
.el-form--inline .el-form-item {
  margin-right: 16px;
  margin-bottom: 16px;
}

.el-form--inline .el-form-item:last-child {
  margin-right: 0;
}

/* 门类选择器样式 */
.el-tree-select .el-select__tags {
  max-width: 200px;
}

.test-report-container {
  max-height: 70vh;
  overflow-y: auto;
}

.section-title {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.section-title .el-icon {
  margin-right: 8px;
  color: #409eff;
}

.archive-info-section {
  margin-bottom: 24px;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 8px;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.info-item {
  display: flex;
  align-items: center;
}

.info-item label {
  font-weight: 600;
  color: #606266;
  min-width: 80px;
}

.info-item span {
  color: #303133;
  word-break: break-all;
}

.test-results-section {
  margin-bottom: 24px;
}

.test-results-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.test-item {
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 16px;
  background: #fff;
}

.test-header {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
}

.test-icon {
  margin-right: 8px;
  font-size: 18px;
}

.test-icon.status-success {
  color: #67c23a;
}

.test-icon.status-danger {
  color: #f56c6c;
}

.test-icon.status-warning {
  color: #e6a23c;
}

.test-icon.status-info {
  color: #909399;
}

.test-name {
  font-weight: 600;
  margin-right: auto;
}

.test-details {
  font-size: 14px;
  line-height: 1.6;
}

.test-details p {
  margin: 4px 0;
  color: #606266;
}

.test-message {
  color: #303133 !important;
  font-weight: 500;
}

.documents-section {
  margin-bottom: 24px;
}

.overall-assessment {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 20px;
  border-radius: 12px;
}

.assessment-header {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
  font-size: 16px;
  font-weight: 600;
}

.assessment-header .el-icon {
  margin-right: 8px;
}

.assessment-content {
  display: flex;
  align-items: center;
  gap: 20px;
}

.assessment-score {
  display: flex;
  align-items: center;
  gap: 12px;
}

.score-circle {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  font-weight: bold;
  position: relative;
}

.score-circle.score-excellent {
  background: #67c23a;
}

.score-circle.score-good {
  background: #409eff;
}

.score-circle.score-pass {
  background: #e6a23c;
}

.score-circle.score-fail {
  background: #f56c6c;
}

.score-circle::after {
  content: '%';
  font-size: 12px;
  position: absolute;
  bottom: 8px;
  right: 4px;
}

.score-text {
  text-align: left;
}

.score-label {
  margin: 0;
  font-size: 14px;
  opacity: 0.9;
}

.score-status {
  margin: 4px 0 0 0;
  font-size: 16px;
  font-weight: 600;
}

.assessment-summary {
  flex: 1;
}

.assessment-summary p {
  margin: 0;
  line-height: 1.6;
  opacity: 0.95;
}

.dialog-footer {
  text-align: right;
}

.dialog-footer .el-button {
  margin-left: 8px;
}

.report-container {
  max-width: 1200px;
  margin: 0 auto;
  background: white;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  border-radius: 8px;
  overflow: hidden;
}

/* 报告头部 */
.report-header {
  background: linear-gradient(135deg, #1e3c72 0%, #2a5298 100%);
  color: white;
  padding: 30px;
  text-align: center;
  position: relative;
}

.report-title {
  font-size: 28px;
  font-weight: bold;
  margin: 0 0 10px 0;
  letter-spacing: 2px;
}

.report-subtitle {
  font-size: 16px;
  opacity: 0.9;
  margin: 0;
}

.report-number {
  position: absolute;
  top: 20px;
  right: 30px;
  font-size: 14px;
  opacity: 0.8;
}

/* 报告内容 */
.report-content {
  padding: 30px;
}

/* 基本信息表格 */
.basic-info-table {
  width: 100%;
  border-collapse: collapse;
  margin-bottom: 30px;
  border: 2px solid #e4e7ed;
}

.basic-info-table th {
  background: #f8fafc;
  padding: 12px;
  border: 1px solid #e4e7ed;
  font-weight: 600;
  text-align: center;
  color: #606266;
  font-size: 14px;
}

.basic-info-table td {
  padding: 12px;
  border: 1px solid #e4e7ed;
  font-size: 14px;
  color: #303133;
}

.basic-info-table tr:nth-child(even) {
  background: #fafbfc;
}

/* 检测结果表格 */
.test-results-table {
  width: 100%;
  border-collapse: collapse;
  margin-bottom: 30px;
  border: 2px solid #e4e7ed;
}

.test-results-table th {
  background: linear-gradient(to bottom, #f8fafc 0%, #eef1f6 100%);
  padding: 15px 12px;
  border: 1px solid #e4e7ed;
  font-weight: 600;
  text-align: center;
  color: #606266;
  font-size: 14px;
}

.test-results-table td {
  padding: 15px 12px;
  border: 1px solid #e4e7ed;
  font-size: 14px;
  vertical-align: top;
}

.test-results-table tr:hover {
  background: #f5f7fa;
}

/* 状态标识 */
.status-cell {
  text-align: center;
}

.status-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  margin-right: 8px;
  font-size: 14px;
  font-weight: bold;
}

.status-passed {
  background: #e7f5e7;
  color: #67c23a;
}

.status-failed {
  background: #fef0f0;
  color: #f56c6c;
}

.status-warning {
  background: #fdf6ec;
  color: #e6a23c;
}

.status-text {
  font-weight: 600;
}

/* 详细信息 */
.detail-info {
  font-size: 13px;
  color: #606266;
  line-height: 1.5;
}

.detail-info p {
  margin: 5px 0;
}

.detail-info strong {
  color: #303133;
}

/* 文档列表部分 */
.documents-section {
  margin-bottom: 30px;
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 2px solid #e4e7ed;
  display: flex;
  align-items: center;
}

.section-title .el-icon {
  margin-right: 8px;
  font-size: 20px;
  color: #409eff;
}

/* 总体评估 */
.overall-assessment {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 25px;
  border-radius: 8px;
  margin-bottom: 20px;
}

.assessment-content {
  display: flex;
  align-items: center;
  gap: 30px;
}

.score-circle {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  font-weight: bold;
  border: 3px solid rgba(255, 255, 255, 0.3);
  background: rgba(255, 255, 255, 0.1);
}

.score-info {
  flex: 1;
}

.score-title {
  font-size: 20px;
  font-weight: 600;
  margin: 0 0 5px 0;
}

.score-desc {
  font-size: 14px;
  opacity: 0.9;
  margin: 0;
}

/* 签名区域 */
.signature-section {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr;
  gap: 30px;
  margin-top: 40px;
  padding-top: 30px;
  border-top: 1px solid #e4e7ed;
}

.signature-item {
  text-align: center;
}

.signature-label {
  font-size: 14px;
  color: #606266;
  margin-bottom: 30px;
}

.signature-line {
  border-bottom: 1px solid #ddd;
  height: 40px;
  position: relative;
}

.signature-date {
  font-size: 12px;
  color: #909399;
  margin-top: 10px;
}

/* 响应式 */
@media (max-width: 768px) {
  .report-content {
    padding: 20px;
  }

  .assessment-content {
    flex-direction: column;
    text-align: center;
  }

  .signature-section {
    grid-template-columns: 1fr;
    gap: 20px;
  }
}

.config-dialog {
  padding: 10px 0;
}

.dialog-desc {
  margin-bottom: 20px;
  color: #606266;
  font-size: 14px;
}

.config-group {
  margin-bottom: 25px;
}

.group-title {
  margin: 0 0 12px 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  border-bottom: 1px solid #e4e7ed;
  padding-bottom: 8px;
}

.checkbox-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding-left: 15px;
}

.selected-info {
  margin-top: 20px;
  padding: 15px;
  background-color: #f5f7fa;
  border-radius: 6px;
}

.selected-title {
  margin: 0 0 10px 0;
  font-size: 14px;
  font-weight: 500;
  color: #409eff;
}

.selected-tag {
  margin-right: 8px;
  margin-bottom: 5px;
}

.dialog-footer {
  text-align: right;
}

/* 第2区小卡片网格布局 */
.mini-grid-area2 {
  display: grid;
  grid-template-rows: repeat(10, 1fr);
  grid-template-columns: repeat(4, 1fr);
  grid-auto-flow: column;
  grid-gap: 1px;
  padding: 4px;
  width: 100%;
  min-height: 120px;
}

.mini-divider1 {
  width: 1px;
  height: 110px;
  background-color: #dcdfe6;
}

.mini-divider2 {
  width: 1px;
  height: 155px;
  background-color: #dcdfe6;
}
.filter-container {
  margin-bottom: 20px;
  display: flex;
  justify-content: center;
}

.el-tag--success {
  background-color: #f0f9eb;
  border-color: #e1f3d8;
  color: #67c23a;
}

.el-tag--warning {
  background-color: #fdf6ec;
  border-color: #faecd8;
  color: #e6a23c;
}

.pagination-container {
  padding: 16px 0;
  text-align: right;
}
.browse-log-dialog .el-dialog__body {
  padding: 15px 20px;
}

.pagination-container {
  padding: 10px 0;
}

:deep(.browse-log-dialog) .el-table .cell {
  padding: 4px 8px;
}

:deep(.browse-log-dialog) .el-table th {
  background-color: #f5f7fa;
  font-weight: bold;
}
</style>
