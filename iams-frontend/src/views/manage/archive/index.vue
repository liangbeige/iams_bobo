<template>
  <div class="app-container" v-loading="loadingV" element-loading-text="移动中...请耐心等待..."
    element-loading-spinner="el-icon-loading" element-loading-background="rgba(0, 0, 0, 0.8)">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="100px">
      <el-form-item label="档号" prop="danghao">
        <el-input v-model="queryParams.danghao" placeholder="请输入档号" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="档案名称" prop="name">
        <el-input v-model="queryParams.name" placeholder="请输入档案名称" clearable @keyup.enter="handleQuery" />
      </el-form-item>
<!--      <el-form-item label="射频标签号" prop="rfid">-->
<!--        <el-input v-model="queryParams.rfid" placeholder="请输入射频标签号" clearable @keyup.enter="handleQuery" />-->
<!--      </el-form-item>-->
      <el-form-item label="保密级别" prop="secretLevel">
        <el-select v-model="queryParams.secretLevel" placeholder="请选择保密级别" clearable>
          <el-option v-for="dict in iams_secret_level" :key="dict.value" :label="dict.label" :value="dict.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="保密期限" prop="secretPeroid">
        <el-select v-model="queryParams.secretPeroid" placeholder="请选择保密期限" clearable>
          <el-option v-for="dict in iams_secret_period" :key="dict.value" :label="dict.label" :value="dict.value" />
        </el-select>
      </el-form-item>
      <!--      <el-form-item label="保管期限" prop="retentionPeriod">-->
      <!--        <el-select v-model="queryParams.retentionPeriod" placeholder="请选择保管期限" clearable>-->
      <!--          <el-option v-for="dict in iams_retention_period" :key="dict.value" :label="dict.label" :value="dict.value" />-->
      <!--        </el-select>-->
      <!--      </el-form-item>-->
      <el-form-item label="载体类型" prop="carrierType">
        <el-select v-model="queryParams.carrierType" placeholder="请选择载体类型" clearable>
          <el-option v-for="dict in iams_carrier_type" :key="dict.value" :label="dict.label" :value="dict.value" />
        </el-select>
      </el-form-item>
      <!--      <el-form-item label="归档时间" prop="guidangTime">-->
      <!--        <el-date-picker clearable v-model="queryParams.guidangTime" type="date" value-format="YYYY-MM-DD"-->
      <!--          placeholder="请选择归档时间">-->
      <!--        </el-date-picker>-->
      <!--      </el-form-item>-->
      <!--      <el-form-item label="电子建档时间" prop="startDate">-->
      <!--        <el-date-picker clearable v-model="queryParams.startDate" type="date" value-format="YYYY-MM-DD"-->
      <!--          placeholder="请选择电子建档时间">-->
      <!--        </el-date-picker>-->
      <!--      </el-form-item>-->
      <el-form-item label="档案状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择档案状态" clearable>
          <el-option v-for="dict in iams_archive_status" :key="dict.value" :label="dict.label" :value="dict.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="档案门类" prop="categoryId">
        <el-input v-model="queryParams.categoryId" placeholder="请输入档案门类" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="档案全宗" prop="fondsName">
        <el-input v-model="queryParams.fondsName" placeholder="请输入档案全宗" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="所属项目">
        <el-select v-model="queryParams.projectId" filterable clearable placeholder="选择项目">
          <el-option v-for="project in projectList" :key="project.id" :label="project.name" :value="project.id" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['manage:archive:add']">新增
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate"
          v-if="!hasArchivedInSelection" v-hasPermi="['manage:archive:edit']">修改
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete"
          v-if="!hasArchivedInSelection" v-hasPermi="['manage:archive:remove']">删除
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['manage:archive:export']">导出
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="info" plain icon="Warning" @click="openAppraisalDialog">
          待鉴定档案: {{ pendingAppraisalCount }} / {{ total }}
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Document" @click="openAppraisalLogDialog">
          鉴定日志
        </el-button>

      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>




    <el-table v-loading="loading" :data="archiveList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <!--      <el-table-column label="主键ID" align="center" prop="id">-->
      <!--        <template #default="scope">-->
      <!--          <el-link type="primary" @click="handleDetail(scope.row)" v-hasPermi="['manage:archive:query']">{{ scope.row.id }}</el-link>-->
      <!--        </template>-->
      <!--      </el-table-column>-->
      <el-table-column label="档号" align="center" prop="danghao" />
      <el-table-column label="档案名称" align="center" prop="name" />
      <!--      <el-table-column label="射频标签号" align="center" prop="rfid" show-overflow-tooltip/>-->
      <el-table-column label="保密级别" align="center" prop="secretLevel" width="80">
        <template #default="scope">
          <dict-tag :options="iams_secret_level" :value="scope.row.secretLevel" />
        </template>
      </el-table-column>
      <el-table-column label="保密期限" align="center" prop="secretPeroid" width="80">
        <template #default="scope">
          <dict-tag :options="iams_secret_period" :value="scope.row.secretPeroid" />
        </template>
      </el-table-column>
      <el-table-column label="载体类型" align="center" prop="carrierType" width="100">
        <template #default="scope">
          <dict-tag :options="iams_carrier_type" :value="scope.row.carrierType" />
        </template>
      </el-table-column>
      <el-table-column label="所属项目" prop="projectName" align="center" width="120">
        <template #default="scope">
          <el-tag v-if="scope.row.projectName" type="info" size="small">
            {{ scope.row.projectName }}
          </el-tag>
          <el-tag v-else type="warning" size="small">
            未分配
          </el-tag>
        </template>
      </el-table-column>

      <el-table-column label="形成单位" align="center" prop="formationUnit" width="80" />
      <!--      <el-table-column label="移交单位" align="center" prop="transferUnit"/>-->
      <el-table-column label="档案状态" align="center" prop="status" width="80">
        <template #default="scope">
          <dict-tag :options="iams_archive_status" :value="scope.row.status" />
        </template>
      </el-table-column>
      <el-table-column label="档案门类" align="center" prop="categoryId" width="120" />
      <!--      <el-table-column label="档案全宗" align="center" prop="fondsId"/>-->
      <!-- 实体档案位置-->
      <el-table-column label="实体档案位置" align="center" prop="location" width="80">
        <template #default="scope">
          <el-tag v-if="scope.row.shitiLocation" type="info" size="small">
            {{ scope.row.shitiLocation }}-{{ scope.row.exactLocation }}
          </el-tag>
          <el-tag v-else type="warning" size="small">
            -
          </el-tag>
        </template>
      </el-table-column>

      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button size="small" link type="primary" @click="handleBorrow(scope.row)">借阅</el-button>

          <el-button link type="primary" @click="handleDetail(scope.row)" v-hasPermi="['manage:archive:query']">查看
          </el-button>

          <el-button link type="primary" v-if="scope.row.status !== 'Destroyed'" @click="handleAssemble(scope.row)"
            v-hasPermi="['manage:archive:query']">组卷
          </el-button>

          <el-button link type="primary" v-if="scope.row.status !== 'Archived' && scope.row.status !== 'Destroyed'"
            @click="handleUpdate(scope.row)" v-hasPermi="['manage:archive:edit']">修改
          </el-button>
<!--          <el-button link type="primary" v-if="scope.row.status !== 'Archived' && scope.row.status !== 'Destroyed'"-->
<!--            @click="handleDelete(scope.row)" v-hasPermi="['manage:archive:remove']">删除-->
<!--          </el-button>-->
          <el-button link type="primary"
                     @click="handleDelete(scope.row)" v-hasPermi="['manage:archive:remove']">删除
          </el-button>
          <!--          <el-button link type="primary" @click="handleBorrow(scope.row)">借阅</el-button>-->
          <el-button link type="primary" v-if="scope.row.status !== 'Destroyed'"
            @click="getArchiveLog(scope.row)">操作日志</el-button>
          <el-button link type="primary" v-if="scope.row.status !== 'Destroyed'"
            @click="getBorrowLog(scope.row)">借阅日志</el-button>
          <!-- 新增：浏览日志按钮 -->
          <el-button link type="primary" v-if="scope.row.status !== 'Destroyed'" @click="handleBrowseLog(scope.row)">浏览日志</el-button>
          <el-button link type="primary" v-if="scope.row.status !== 'Archived' && scope.row.status !== 'Destroyed'"
            @click="handleGuidang(scope.row)">归档
          </el-button>
          <!-- 在 el-button 归档按钮附近添加如下代码 -->
          <el-button link type="danger" v-if="scope.row.status === 'Archived' && scope.row.status !== 'Destroyed'"
            @click="handleUnarchive(scope.row)">撤销归档
          </el-button>
          <el-button link type="primary" v-if="scope.row.status === 'Archived' &&
            scope.row.status !== 'Destroyed' &&
            (!scope.row.shitiLocation || scope.row.shitiLocation === null || scope.row.shitiLocation === '')"
            @click="handShangjia(scope.row)">上架
          </el-button>
          <el-button link type="primary" v-if="scope.row.status === 'Archived' &&
            scope.row.status !== 'Destroyed' &&
            (scope.row.shitiLocation && scope.row.shitiLocation !== '')" @click="handleXiajia(scope.row)">下架
          </el-button>
          <el-button size="small" link type="primary" @click="handleCharacteristicsTest(scope.row)"
                     v-if="scope.row.status === 'Archived' && scope.row.status !== 'Destroyed'">检测报告</el-button>
          <el-button size="small" link type="primary" @click="handlePrintRfid(scope.row)"
                     v-if="scope.row.status === 'Archived' && scope.row.status !== 'Destroyed'">打印标签</el-button>
          <!-- 执行移动架子操作 -->
          <el-button link type="primary" v-if="scope.row.status !== 'Destroyed'" @click="handleMove(scope.row)">
            >移架操作< </el-button>
              <el-button link type="primary" v-if="scope.row.status !== 'Destroyed'" @click="handleCombine(scope.row)">
                >合架操作< </el-button>
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

    <!--鉴定弹窗-->
    <el-dialog title="待鉴定档案列表" v-model="appraisalDialogVisible" width="90%" :close-on-click-modal="false"
      append-to-body>

      <!-- 批量操作按钮 -->
      <div class="batch-operations mb-4">
        <el-col :span="1.5">
          <el-button type="primary" :disabled="selectedAppraisalIds.length === 0" @click="openBatchAppraisalDialog">
            批量鉴定 ({{ selectedAppraisalIds.length }})
          </el-button>
        </el-col>
        <!--        <el-button-->
        <!--            type="danger"-->
        <!--            :disabled="selectedAppraisalIds.length === 0"-->
        <!--            @click="handleBatchDestroy">-->
        <!--          批量销毁-->
        <!--        </el-button>-->
        <!--        <el-button-->
        <!--            type="danger"-->
        <!--            plain-->
        <!--            :disabled="selectedAppraisalIds.length === 0"-->
        <!--            @click="handleBatchDelete">-->
        <!--          批量删除-->
        <!--        </el-button>-->
        <el-col :span="1.5">
          <el-button type="danger" :disabled="!canBatchDestroy" @click="handleBatchSubmitDestroy">
            批量销毁
          </el-button>
        </el-col>
        <el-col :span="1.5">
          <el-button type="danger" plain :disabled="!canBatchDelete" @click="handleBatchDelete">
            批量删除
          </el-button>
        </el-col>
        <!--        <el-col :span="1.5">-->
        <!--          <el-button-->
        <!--              type="warning"-->
        <!--              plain-->
        <!--              :disabled="!canBatchSubmitDestroy"-->
        <!--              @click="handleBatchSubmitDestroy">-->
        <!--            批量提交销毁申请-->
        <!--          </el-button>-->
        <!--        </el-col>-->
      </div>

      <!-- 待鉴定档案表格 -->
      <el-table :data="pendingAppraisalList" @selection-change="handleAppraisalSelectionChange" stripe border>
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="档号" prop="danghao" width="180" show-overflow-tooltip />
        <el-table-column label="档案名称" prop="name" width="200" show-overflow-tooltip />

        <!-- 保管期限 - 突出显示缺失或过期 -->
        <el-table-column label="保管期限" width="120" align="center">
          <template #default="scope">
            <span v-if="!scope.row.retentionPeriod" class="missing-field">
              <el-tag type="danger" size="small">未设置</el-tag>
            </span>
            <span v-else-if="isRetentionExpired(scope.row)" class="expired-field">
              <el-tag type="warning" size="small">已过期</el-tag>
              <dict-tag :options="iams_retention_period" :value="scope.row.retentionPeriod" />
            </span>
            <span v-else>
              <dict-tag :options="iams_retention_period" :value="scope.row.retentionPeriod" />
            </span>
          </template>
        </el-table-column>

        <!-- 鉴定保密级别 - 突出显示缺失 -->
        <el-table-column label="保密级别" width="100" align="center">
          <template #default="scope">
            <span v-if="!scope.row.secretLevel" class="missing-field">
              <el-tag type="danger" size="small">未设置</el-tag>
            </span>
            <span v-else>
              <dict-tag :options="iams_secret_level" :value="scope.row.secretLevel" />
            </span>
          </template>
        </el-table-column>

        <!-- 鉴定保密期限 - 突出显示缺失或过期 -->
        <el-table-column label="保密期限" width="120" align="center">
          <template #default="scope">
            <span v-if="!scope.row.secretPeroid" class="missing-field">
              <el-tag type="danger" size="small">未设置</el-tag>
            </span>
            <span v-else-if="isSecretExpired(scope.row)" class="expired-field">
              <el-tag type="warning" size="small">已过期</el-tag>
              <dict-tag :options="iams_secret_period" :value="scope.row.secretPeroid" />
            </span>
            <span v-else>
              <dict-tag :options="iams_secret_period" :value="scope.row.secretPeroid" />
            </span>
          </template>
        </el-table-column>

        <el-table-column label="档案状态" width="100" align="center">
          <template #default="scope">
            <dict-tag :options="iams_archive_status" :value="scope.row.status" />
          </template>
        </el-table-column>

        <el-table-column label="创建时间" prop="createTime" width="180" align="center" />
        <el-table-column label="鉴定原因" width="150" align="center">
          <template #default="scope">
            <div class="appraisal-reasons">
              <el-tag v-for="reason in getAppraisalReasons(scope.row)" :key="reason" :type="getReasonTagType(reason)"
                size="small" class="reason-tag">
                {{ reason }}
              </el-tag>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="150" align="center">
          <template #default="scope">
            <el-button type="primary" size="small" @click="handleSingleAppraisal(scope.row)">
              单独鉴定
            </el-button>
            <!--            <el-button type="danger" size="small" @click="handleSingleDestroy(scope.row)">-->
            <!--              销毁-->
            <!--            </el-button>-->
            <el-button v-if="scope.row.status === 'Archived' && scope.row.availability === 0" type="danger" size="small"
              @click="handleSingleDestroy(scope.row)">
              销毁
            </el-button>
            <el-button v-if="scope.row.status !== 'Archived' || scope.row.availability !== 0" type="danger" size="small"
              @click="handleAppraisalDelete(scope.row)">
              删除
            </el-button>
          </template>
        </el-table-column>

      </el-table>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="appraisalDialogVisible = false">关闭</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="destroyDialog.visible" title="档案销毁申请" width="600px" :close-on-click-modal="false">
      <div class="destroy-content">
        <!-- 显示选中的档案信息 -->
        <div class="archive-info-section"
          style="margin-bottom: 20px; padding: 15px; background-color: #f5f7fa; border-radius: 4px;">
          <h4 style="margin: 0 0 10px 0; color: #303133;">待销毁档案信息</h4>
          <div v-if="destroyDialog.selectedArchives.length === 1" class="single-archive">
            <p style="margin: 5px 0; color: #606266;">
              <strong>档案名称:</strong> {{ destroyDialog.selectedArchives[0].name }}
            </p>
            <p style="margin: 5px 0; color: #606266;">
              <strong>档号:</strong> {{ destroyDialog.selectedArchives[0].danghao }}
            </p>
            <p style="margin: 5px 0; color: #606266;">
              <strong>位置:</strong> {{ destroyDialog.selectedArchives[0].shitiLocation }}-{{
                destroyDialog.selectedArchives[0].exactLocation }}
            </p>
          </div>
          <div v-else class="multiple-archives">
            <p style="margin: 5px 0; color: #606266;">
              <strong>选中档案数量:</strong> {{ destroyDialog.selectedArchives.length }} 个
            </p>
            <div
              style="max-height: 120px; overflow-y: auto; border: 1px solid #e4e7ed; border-radius: 4px; padding: 8px;">
              <div v-for="archive in destroyDialog.selectedArchives" :key="archive.id"
                style="padding: 4px 0; border-bottom: 1px solid #f0f0f0; font-size: 12px;">
                {{ archive.danghao }} - {{ archive.name }}
              </div>
            </div>
          </div>
        </div>

        <!-- 销毁申请表单 -->
        <el-form ref="destroyFormRef" :model="destroyForm" :rules="destroyRules" label-width="120px">
          <el-form-item label="申请人" prop="applicant">
            <el-select v-model="destroyForm.applicant" placeholder="请选择申请人">
              <el-option v-for="user in userList" :key="user.id" :label="user.userName" :value="user.userName" />
            </el-select>
          </el-form-item>

          <el-form-item label="销毁原因" prop="reason">
            <el-select v-model="destroyForm.reason" placeholder="请选择销毁原因">
              <el-option label="保管期限到期" value="保管期限到期" />
              <el-option label="档案损坏" value="档案损坏" />
              <el-option label="内容过时失效" value="内容过时失效" />
              <el-option label="重复归档" value="重复归档" />
              <el-option label="其他" value="其他" />
            </el-select>
          </el-form-item>

          <el-form-item label="详细说明" prop="description">
            <el-input v-model="destroyForm.description" type="textarea" :rows="4" placeholder="请详细说明销毁原因和依据" />
          </el-form-item>

          <el-form-item label="预计销毁时间" prop="scheduledDate">
            <el-date-picker v-model="destroyForm.scheduledDate" type="datetime" placeholder="选择预计销毁时间"
              value-format="YYYY-MM-DD HH:mm:ss" />
          </el-form-item>
        </el-form>
      </div>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="destroyDialog.visible = false">取 消</el-button>
          <el-button type="danger" @click="handleSubmitDestroyApplication" :loading="destroyDialog.loading">
            提交申请
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 单独/批量鉴定弹窗 -->
    <el-dialog :title="batchAppraisalDialog.title" v-model="batchAppraisalDialog.visible" width="600px"
      :close-on-click-modal="false">

      <el-form ref="appraisalFormRef" :model="appraisalForm" :rules="appraisalRules" label-width="120px">
        <el-form-item label="鉴定人员" prop="appraiser">
          <el-select v-model="appraisalForm.appraiser" placeholder="请选择鉴定人员">
            <el-option v-for="user in userList" :key="user.id" :label="user.userName" :value="user.userName" />
          </el-select>
        </el-form-item>

        <!--        <el-form-item label="保管期限" prop="retentionPeriod">-->
        <!--          <el-select v-model="appraisalForm.retentionPeriod" placeholder="请选择保管期限">-->
        <!--            <el-option v-for="dict in iams_retention_period" :key="dict.value" :label="dict.label" :value="dict.value" />-->
        <!--          </el-select>-->
        <!--        </el-form-item>-->

        <!--        <el-form-item label="保密级别" prop="secretLevel">-->
        <!--          <el-select v-model="appraisalForm.secretLevel" placeholder="请选择保密级别">-->
        <!--            <el-option v-for="dict in iams_secret_level" :key="dict.value" :label="dict.label" :value="dict.value" />-->
        <!--          </el-select>-->
        <!--        </el-form-item>-->

        <!--        <el-form-item label="保密期限" prop="secretPeroid">-->
        <!--          <el-select v-model="appraisalForm.secretPeroid" placeholder="请选择保密期限">-->
        <!--            <el-option v-for="dict in iams_secret_period" :key="dict.value" :label="dict.label" :value="dict.value" />-->
        <!--          </el-select>-->
        <!--        </el-form-item>-->
        <el-form-item label="保管期限" prop="afterRetentionPeriod">
          <el-select v-model="appraisalForm.afterRetentionPeriod" placeholder="请选择保管期限">
            <el-option v-for="dict in iams_retention_period" :key="dict.value" :label="dict.label"
              :value="dict.value" />
          </el-select>
        </el-form-item>

        <el-form-item label="保密级别" prop="afterSecretLevel">
          <el-select v-model="appraisalForm.afterSecretLevel" placeholder="请选择保密级别">
            <el-option v-for="dict in iams_secret_level" :key="dict.value" :label="dict.label" :value="dict.value" />
          </el-select>
        </el-form-item>

        <el-form-item label="保密期限" prop="afterSecretPeriod">
          <el-select v-model="appraisalForm.afterSecretPeriod" placeholder="请选择保密期限">
            <el-option v-for="dict in iams_secret_period" :key="dict.value" :label="dict.label" :value="dict.value" />
          </el-select>
        </el-form-item>

        <el-form-item label="鉴定结果" prop="appraisalResult">
          <el-radio-group v-model="appraisalForm.appraisalResult">
            <el-radio label="archive">继续保存</el-radio>
            <el-radio label="destroy">建议销毁</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="鉴定依据" prop="appraisalReason">
          <el-input v-model="appraisalForm.appraisalReason" type="textarea" :rows="3" placeholder="请输入鉴定依据和说明">
          </el-input>
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="batchAppraisalDialog.visible = false">取消</el-button>
          <el-button type="primary" @click="submitAppraisal" :loading="appraisalSubmitting">
            确认鉴定
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 鉴定日志弹窗 -->
    <el-dialog title="档案鉴定日志" v-model="appraisalLogDialogVisible" width="90%" append-to-body>

      <!-- 搜索条件 -->
      <el-form :model="logQueryParams" :inline="true" label-width="80px" class="mb-4">
        <el-form-item label="档案名称">
          <el-input v-model="logQueryParams.archiveName" placeholder="请输入档案名称" clearable />
        </el-form-item>
        <el-form-item label="鉴定人员">
          <el-input v-model="logQueryParams.appraiser" placeholder="请输入鉴定人员" clearable />
        </el-form-item>
        <el-form-item label="鉴定时间">
          <el-date-picker v-model="logQueryParams.appraisalDateRange" type="daterange" range-separator="至"
            start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD">
          </el-date-picker>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="getAppraisalLogList">查询</el-button>
          <el-button @click="resetLogQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 鉴定日志表格 -->
      <el-table :data="appraisalLogList" stripe border>
        <el-table-column label="档号" prop="danghao" width="180" />
        <el-table-column label="档案名称" prop="archiveName" width="200" show-overflow-tooltip />
        <el-table-column label="鉴定人员" prop="appraiser" width="100" />
        <el-table-column label="鉴定时间" prop="appraisalTime" width="160" />
        <el-table-column label="鉴定前状态" width="200">
          <template #default="scope">
            <div class="status-change">
              <span class="before-status">
                保管期限: {{ scope.row.beforeRetentionPeriod || '未设置' }} |
                保密级别: {{ scope.row.beforeSecretLevel || '未设置' }} |
                保密期限: {{ scope.row.beforeSecretPeroid || '未设置' }}
              </span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="鉴定后状态" width="200">
          <template #default="scope">
            <div class="status-change">
              <span class="after-status">
                保管期限: {{ scope.row.afterRetentionPeriod || '未设置' }} |
                保密级别: {{ scope.row.afterSecretLevel || '未设置' }} |
                保密期限: {{ scope.row.afterSecretPeriod || '未设置' }}
              </span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="鉴定结果" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.appraisalResult === 'archive' ? 'success' : 'danger'">
              {{ scope.row.appraisalResult === 'archive' ? '保存' : '销毁' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="鉴定依据" prop="appraisalReason" show-overflow-tooltip />
      </el-table>

      <!-- 分页 -->
      <pagination v-show="appraisalLogTotal > 0" :total="appraisalLogTotal" v-model:page="logQueryParams.pageNum"
        v-model:limit="logQueryParams.pageSize" @pagination="getAppraisalLogList" />

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="appraisalLogDialogVisible = false">关闭</el-button>
        </div>
      </template>
    </el-dialog>


    <!-- 添加或修改档案列表对话框 -->
    <el-dialog :title="title" v-model="open" width="500px" append-to-body>
      <el-form ref="archiveRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="档号" prop="danghao">
          <el-input v-model="form.danghao" placeholder="请输入档号" />
        </el-form-item>
        <el-form-item label="档案名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入档案名称" />
        </el-form-item>
        <el-form-item label="射频标签号" prop="rfid">
          <el-input v-model="form.rfid" placeholder="请输入射频标签号" />
        </el-form-item>
        <el-form-item label="保密级别" prop="secretLevel">
          <el-select v-model="form.secretLevel" placeholder="请选择保密级别">
            <el-option v-for="dict in iams_secret_level" :key="dict.value" :label="dict.label"
              :value="dict.value"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="保密期限" prop="secretPeroid">
          <el-select v-model="form.secretPeroid" placeholder="请选择保密期限">
            <el-option v-for="dict in iams_secret_period" :key="dict.value" :label="dict.label"
              :value="dict.value"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="解密日期" prop="desecretDate">
          <el-date-picker clearable v-model="form.desecretDate" type="date" value-format="YYYY-MM-DD"
            placeholder="请选择解密日期">
          </el-date-picker>
        </el-form-item>
        <el-form-item label="保管期限" prop="retentionPeriod">
          <el-select v-model="form.retentionPeriod" placeholder="请选择保管期限">
            <el-option v-for="dict in iams_retention_period" :key="dict.value" :label="dict.label"
              :value="dict.value"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="载体类型" prop="carrierType">
          <el-select v-model="form.carrierType" placeholder="请选择载体类型">
            <el-option v-for="dict in iams_carrier_type" :key="dict.value" :label="dict.label"
              :value="dict.value"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="起始时间" prop="startDate">
          <el-date-picker clearable v-model="form.startDate" type="date" value-format="YYYY-MM-DD"
            placeholder="请选择起始时间">
          </el-date-picker>
        </el-form-item>
        <el-form-item label="终止时间" prop="endDate">
          <el-date-picker clearable v-model="form.endDate" type="date" value-format="YYYY-MM-DD" placeholder="请选择终止时间">
          </el-date-picker>
        </el-form-item>
        <el-form-item label="归档时间" prop="guidangTime">
          <el-date-picker clearable v-model="form.guidangTime" type="date" value-format="YYYY-MM-DD"
            placeholder="请选择归档时间">
          </el-date-picker>
        </el-form-item>
        <el-form-item label="实体档案位置" prop="shitiLocation">
          <el-input v-model="form.shitiLocation" placeholder="请输入实体档案位置" />
        </el-form-item>
        <el-form-item label="电子档案位置" prop="dianziLocation">
          <el-input v-model="form.dianziLocation" placeholder="请输入电子档案位置" />
        </el-form-item>
        <el-form-item label="档案简介" prop="description">
          <el-input v-model="form.description" type="textarea" placeholder="请输入内容" />
        </el-form-item>
        <el-form-item label="形成单位" prop="formationUnit">
          <el-input v-model="form.formationUnit" placeholder="请输入形成单位" />
        </el-form-item>
        <el-form-item label="移交单位" prop="transferUnit">
          <el-input v-model="form.transferUnit" placeholder="请输入移交单位" />
        </el-form-item>
        <el-form-item label="文件(档)数量" prop="documentCount">
          <el-input v-model="form.documentCount" placeholder="请输入文件(档)数量" />
        </el-form-item>
        <el-form-item label="档案状态" prop="status">
          <el-select v-model="form.status" placeholder="请选择档案状态">
            <el-option v-for="dict in iams_archive_status" :key="dict.value" :label="dict.label"
              :value="dict.value"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="档案门类" prop="categoryId">
          <el-input v-model="form.categoryId" placeholder="请输入档案门类" />
        </el-form-item>
        <el-form-item label="档案全宗" prop="fondsId">
          <el-input v-model="form.fondsId" placeholder="请输入档案全宗" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" placeholder="请输入内容" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
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
              <!-- 档案柜视图中的区号选择也需要修改 -->
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

<script>
// 显示的定义组件名称，和路由地址保持一致，
// 在系统管理->菜单管理 中可以配置是否开启页面缓存
export default {
  name: 'Arc-list'
}
</script>


<script setup name="Archive">
import { handleMoveMeth, handleCombineMeth } from '@/views/iotSubSystem/moveMethods.js'
import {
  listArchive,
  getArchive,
  delArchive,
  addArchive,
  updateArchive,
  ArchivePermission,
  guidang,
  unarchive, addArchiveCategoryRootName
} from "@/api/manage/archive";
import {
  submitDestroyApplication,
  batchSubmitDestroyApplication
} from "@/api/manage/ArchiveDestroyRecord.js";
import { listArchiveLog, addArchiveLog, listOpLogs, listBorrowLogs } from "@/api/manage/ArchiveLog"
import { updateCabinetSize } from "@/api/manage/cabinet";
import { updateCompartmentSize } from "@/api/manage/compartment";
import { ElMessage, ElMessageBox } from "element-plus";
import { getColumnStatus, leftMoveColumn, resetColumn, rightMoveColumn, unlockColumn, cancelSleepColumn } from "@/api/system/move.js";
import { listProject } from "@/api/manage/project";
import { onMounted, computed, ref, nextTick } from 'vue';
import { useRouter } from 'vue-router'
import { listDocumentByArchiveId } from "@/api/manage/document";
import { listUserByRoleId } from "@/api/system/user";
import { getUserProfile } from "@/api/system/user.js"
import {
  listIdentificationRecord,
  submitIdentificationRecord,
  getIdentificationRecordByArchiveId,
  getPendingAppraisalCount,
  listPendingAppraisalArchives,
  destroyArchives,
  getIdentificationStatistics
} from "@/api/manage/identificationApproval";
import dayjs from 'dayjs';
// 引入借阅相关的API
import { addRecord, directRecord } from "@/api/manage/borrowrecord"
import { getUserInfo, listDefinition } from "@/api/activiti/definition"
import { getArchiveIdByMysqlDanghao } from "@/api/manage/archive.js"
import { fourCharacteristicsTest, printRfid } from '@/api/manage/archive.js'

// 引入需要的API
import { listCabinet } from "@/api/manage/cabinet"
import { listCompartment } from "@/api/manage/compartment"
import { status } from "nprogress";
import {Download} from "@element-plus/icons-vue";
import {addCategory} from "@/api/manage/category.js";
import {UpdateCategoryCodesToCodeName} from "@/api/manage/fonds.js";
import {listBrowseLogByArchiveId} from "@/api/manage/browseLog.js";

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

async function handlePrintRfid(row) {
  printRfid(row.id).then(response => {
    console.log(response.data)
    getList()
  });
}

const router = useRouter()
const { proxy } = getCurrentInstance();
// 添加项目列表状态
const projectList = ref([]);
const {
  iams_carrier_type,
  iams_archive_status,
  iams_retention_period,
  iams_secret_period,
  iams_secret_level
} = proxy.useDict('iams_carrier_type', 'iams_archive_status', 'iams_retention_period', 'iams_secret_period', 'iams_secret_level');

const archiveList = ref([]);
const open = ref(false);
const loading = ref(true);
const showSearch = ref(true);
const ids = ref([]);
const single = ref(true);
const multiple = ref(true);
const total = ref(0);
const title = ref("");
const files = ref([]);


const appraisalDialogVisible = ref(false)
const appraisalLogDialogVisible = ref(false)
const batchAppraisalDialog = reactive({
  visible: false,
  title: '',
  isMatching: false
})

const pendingAppraisalList = ref([])
const pendingAppraisalCount = ref(0)
const selectedAppraisalIds = ref([])
const currentAppraisalArchives = ref([])

const appraisalForm = reactive({
  appraiser: '',
  // retentionPeriod: '',
  // secretLevel: '',
  // secretPeroid: '',
  afterRetentionPeriod: '',  // 修改：加上 after 前缀
  afterSecretLevel: '',      // 修改：加上 after 前缀
  afterSecretPeriod: '',     // 修改：加上 after 前缀
  appraisalResult: 'archive',
  appraisalReason: ''
})

const appraisalRules = {
  appraiser: [{ required: true, message: '请选择鉴定人员', trigger: 'change' }],
  appraisalResult: [{ required: true, message: '请选择鉴定结果', trigger: 'change' }],
  appraisalReason: [{ required: true, message: '请输入鉴定依据', trigger: 'blur' }]
}

const appraisalSubmitting = ref(false)
const appraisalFormRef = ref()

// 鉴定日志相关
const appraisalLogList = ref([])
const appraisalLogTotal = ref(0)
const logQueryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  archiveName: '',
  appraiser: '',
  appraisalDateRange: []
})

const destroyDialog = reactive({
  visible: false,
  loading: false,
  selectedArchives: []
})

// 销毁申请表单
const destroyForm = reactive({
  applicant: '',
  reason: '',
  description: '',
  scheduledDate: ''
})

const destroyFormRef = ref()

// 表单验证规则
const destroyRules = {
  applicant: [
    { required: true, message: '请选择申请人', trigger: 'change' }
  ],
  reason: [
    { required: true, message: '请选择销毁原因', trigger: 'change' }
  ],
  description: [
    { required: true, message: '请填写详细说明', trigger: 'blur' }
  ],
  scheduledDate: [
    { required: true, message: '请选择预计销毁时间', trigger: 'change' }
  ]
}

// 计算属性：是否可以批量提交销毁申请
const canBatchSubmitDestroy = computed(() => {
  if (selectedAppraisalIds.value.length === 0) {
    return false
  }

  // 获取选中的档案
  const selectedArchives = pendingAppraisalList.value.filter(item =>
    selectedAppraisalIds.value.includes(item.id)
  )

  // 检查是否所有选中的档案都可以销毁（已归档且可用性为0）
  return selectedArchives.every(item => canDestroy(item))
})

// 单个销毁处理
function handleSingleDestroy(archive) {
  // 检查档案状态
  if (!canDestroy(archive)) {
    proxy.$modal.msgWarning("只有已归档且已上架的档案才能申请销毁")
    return
  }

  destroyDialog.selectedArchives = [archive]
  openDestroyDialog()
}

// 批量销毁处理（从鉴定弹窗中）
function handleBatchSubmitDestroy() {
  // 检查选中的档案
  const selectedArchives = pendingAppraisalList.value.filter(item =>
    selectedAppraisalIds.value.includes(item.id)
  )

  const validArchives = selectedArchives.filter(item => canDestroy(item))
  const invalidArchives = selectedArchives.filter(item => !canDestroy(item))

  if (invalidArchives.length > 0) {
    const invalidNames = invalidArchives.map(item => item.name || item.danghao).join('、')
    proxy.$modal.msgWarning(`以下档案不能销毁：${invalidNames}`)
    return
  }

  if (validArchives.length === 0) {
    proxy.$modal.msgWarning("请选择可以销毁的档案")
    return
  }

  destroyDialog.selectedArchives = validArchives
  openDestroyDialog()
}

// 打开销毁申请弹窗
function openDestroyDialog() {
  // 重置表单
  resetDestroyForm()
  destroyDialog.visible = true
}

// 重置销毁表单
function resetDestroyForm() {
  destroyForm.applicant = ''
  destroyForm.reason = ''
  destroyForm.description = ''
  destroyForm.scheduledDate = ''

  if (destroyFormRef.value) {
    destroyFormRef.value.resetFields()
  }
}

// 提交销毁申请
async function handleSubmitDestroyApplication() {
  try {
    // 表单验证
    await destroyFormRef.value.validate()

    const confirmMessage = destroyDialog.selectedArchives.length === 1
      ? `确定要提交档案【${destroyDialog.selectedArchives[0].name}】的销毁申请吗？`
      : `确定要提交 ${destroyDialog.selectedArchives.length} 个档案的销毁申请吗？`

    await ElMessageBox.confirm(confirmMessage, '确认提交', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    destroyDialog.loading = true

    // 如果是批量申请，使用批量接口
    if (destroyDialog.selectedArchives.length > 1) {
      const batchData = {
        archiveIds: destroyDialog.selectedArchives.map(archive => archive.id),
        purpose: `${destroyForm.reason}：${destroyForm.description}`,
        description: destroyForm.description,
        applicant: destroyForm.applicant
      }

      await batchSubmitDestroyApplication(batchData)
    } else {
      // 单个申请
      const archive = destroyDialog.selectedArchives[0]
      const purposeText = `销毁原因：${destroyForm.reason}\n详细说明：${destroyForm.description}\n申请人：${destroyForm.applicant}`

      const submitData = {
        archiveId: Number(archive.id),
        purpose: purposeText,
        reason: destroyForm.reason,
        description: destroyForm.description,
        applicant: destroyForm.applicant,
        scheduledDate: destroyForm.scheduledDate ?
          new Date(destroyForm.scheduledDate).toISOString().replace('T', ' ').substring(0, 19) : null
      }

      await submitDestroyApplication(submitData)
    }

    ElMessage.success('销毁申请提交成功')
    destroyDialog.visible = false

    // 刷新鉴定列表
    getPendingAppraisalListData()
    getPendingAppraisalCountData()
    getList() // 刷新主列表

  } catch (error) {
    if (error !== 'cancel') {
      console.error('提交销毁申请失败:', error)
      let errorMsg = '提交失败'

      if (error.response?.data?.msg) {
        errorMsg = error.response.data.msg
      } else if (error.message) {
        errorMsg = error.message
      }

      ElMessage.error('提交销毁申请失败：' + errorMsg)
    }
  } finally {
    destroyDialog.loading = false
  }
}

//loadingV
let loadingV = ref(false);
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
  shitiLocation: '', // 格式：区号-列号-左右侧
  exactLocation: '', // 格式：节号-层号
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

// 位置选择项
const iams_shiti_location_quhao = ref([
  { label: '第1区', value: '1' },
  { label: '第2区', value: '2' },
  { label: '第3区', value: '3' },
  { label: '第4区', value: '4' }
]);

// 从这里开始修改===============================================================20250710
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
// 修复后的方法，请替换你现有的相关方法

// 第2区修改后新增的方法
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

  try {
    // 将选中的配置项拼接成字符串
    const configString = selectedOptions.value.join(',');

    // 调用归档 API
    await guidang({
      id: currentRow.value.id,
      testConfig: configString
    });

    // 添加入库日志
    await addArchiveLog({
      archiveId: currentRow.value.id,
      taskType: '入库',
      initiator: user.value.userName,
      remark: '归档入库',
      startDate: dayjs().format('YYYY-MM-DD HH:mm:ss'),
    });

    // 更新状态
    currentRow.value.status = 'Archived';

    ElMessage.success('归档成功');

    // 关闭弹窗
    guidangDialogVisible.value = false;
  } catch (error) {
    console.error(error);
    ElMessage.error('归档失败');
  }
};

const cancelGuidang = () => {
  guidangDialogVisible.value = false;
  selectedOptions.value = [];
  currentRow.value = null;
};

// 提交归档操作
const submitGuidang = async () => {
  try {

    // 调用归档 API
    await guidang({ id: currentRow.value.id });



    // 更新状态
    currentRow.value.status = 'Archived';

    ElMessage.success('归档成功');

    // 关闭弹窗
    rukuDialogVisible.value = false;
  } catch (error) {
    console.error(error);
    ElMessage.error('归档失败');
  }
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
  operator: '',
  reason: ''
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
      await updateArchive({ id: currentRow.value.id, shitiLocation: '', exactLocation: '', availability: 1 });
      // 更新状态字段，假定 status 字段表示状态
      updateCabinetSize(currentRow.value.shitiLocation, 'delete');
      updateCompartmentSize(currentRow.value.shitiLocation + '-' + currentRow.value.exactLocation, 'delete');
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
  switch (carrierType) {
    case 'electronic':
      return 'success'
    case 'tangible':
      return 'warning'
    case 'Mixture':
      return 'primary'
    default:
      return 'info'
  }
}

// 获取载体类型显示名称
const getCarrierTypeLabel = (carrierType) => {
  switch (carrierType) {
    case 'electronic':
      return '电子档案'
    case 'tangible':
      return '实体档案'
    case 'Mixture':
      return '混合档案'
    default:
      return '未知类型'
  }
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

    if (row.shitiLocation && row.shitiLocation != '') {
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

const goToEmptyLocation = () => {
  router.push({
    path: '/manage/cabinet/cbn-list'
  })
}


// 新增：选中项的状态控制
const selectedItems = ref([]);

// 计算属性：检查选中的单个项目是否为已归档状态
const selectedArchiveStatus = computed(() => {
  if (selectedItems.value.length === 1) {
    return selectedItems.value[0].status;
  }
  return null;
});

// 计算属性：检查选中项中是否包含已归档的档案
const hasArchivedInSelection = computed(() => {
  return selectedItems.value.some(item => item.status === 'Archived');
});

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    danghao: null,
    name: null,
    rfid: null,
    secretLevel: null,
    secretPeroid: null,
    retentionPeriod: null,
    carrierType: null,
    startDate: null,
    guidangTime: null,
    directory: null,
    status: null,
    categoryId: null,
    fondsId: null,
    fondsName: null,    // 全宗名称搜索字段
    projectId: null,
    juanhao: null
  },
  logQueryParams: {
    pageNum: 1,
    pageSize: 10,
    archiveId: null,
    taskType: null,
    initiator: null,
    handler: null,
    startDate: null,
    endDate: null,
    taskStatus: null,
  },
  rules: {
    name: [
      { required: true, message: "档案名称不能为空", trigger: "blur" }
    ],
  },

  // 鉴定相关的新变量
  appraisalLogDialogVisible: false,
  batchAppraisalDialog: {
    visible: false,
    title: '',
    isMatching: false
  },

  pendingAppraisalList: [],
  pendingAppraisalCount: 0,
  selectedAppraisalIds: [],
  currentAppraisalArchives: [],

  appraisalForm: {
    appraiser: '',
    afterRetentionPeriod: '',
    afterSecretLevel: '',
    afterSecretPeriod: '',
    appraisalResult: 'archive',
    appraisalReason: ''
  },

  appraisalRules: {
    appraiser: [{ required: true, message: '请选择鉴定人员', trigger: 'change' }],
    appraisalResult: [{ required: true, message: '请选择鉴定结果', trigger: 'change' }],
    appraisalReason: [{ required: true, message: '请输入鉴定依据', trigger: 'blur' }]
  },

  appraisalSubmitting: false,

  // 鉴定日志相关
  appraisalLogList: [],
  appraisalLogTotal: 0,

});

const { queryParams, form, rules } = toRefs(data);
const user = ref({});

// 8. 在组件挂载时初始化选项
onMounted(() => {
  // 初始化默认选项
  initLocationOptions('1');

  // 其他初始化代码...
  getList();
  getProjectList();
  getUserListByRoleId({ roleId: 10007 });
  getUserProfile().then(response => {
    user.value = response.data;
  });
  setTimeout(() => {
    getPendingAppraisalCountData();
  }, 1000);
});

// 获取待鉴定档案统计
function getPendingAppraisalCountData() {
  console.log('开始获取待鉴定档案统计...');

  getPendingAppraisalCount().then(response => {
    console.log('获取待鉴定档案统计成功:', response);
    pendingAppraisalCount.value = response.data || 0;
  }).catch(error => {
    console.error('获取待鉴定档案统计失败:', error);
    pendingAppraisalCount.value = 0;
  });
}

// 打开待鉴定档案弹窗
function openAppraisalDialog() {
  appraisalDialogVisible.value = true
  getPendingAppraisalListData()
}


// 获取待鉴定档案列表 - 修复后的版本
function getPendingAppraisalListData() {
  console.log('开始获取待鉴定档案列表...');

  listPendingAppraisalArchives({
    pageNum: 1,
    pageSize: 9999
  }).then(response => {
    console.log('获取待鉴定档案列表成功:', response);

    // // 过滤掉已销毁和待销毁的档案
    // const filteredList = (response.rows || []).filter(item => {
    //   return item.status !== 'Destroyed' && item.status !== 'Destroying';
    // });
    // pendingAppraisalList.value = filteredList
    // 根据创建时间降序排序 (最新的在前面)
    const sortedList = (response.rows || []).sort((a, b) => {
      return new Date(b.createTime) - new Date(a.createTime);
    });
    pendingAppraisalList.value = sortedList
  }).catch(error => {
    console.error('获取待鉴定档案列表失败:', error);
    pendingAppraisalList.value = []
  });
}

// 判断保管期限是否过期
function isRetentionExpired(archive) {
  if (!archive.retentionPeriod || !archive.createTime) return false

  const createDate = new Date(archive.createTime)
  const currentDate = new Date()
  const yearsDiff = currentDate.getFullYear() - createDate.getFullYear()

  // 根据保管期限判断是否过期
  const retentionYears = parseInt(archive.retentionPeriod) || 0
  return yearsDiff > retentionYears
}

// 判断保密期限是否过期
function isSecretExpired(archive) {
  if (!archive.secretPeroid || !archive.createTime) return false

  const createDate = new Date(archive.createTime)
  const currentDate = new Date()
  const yearsDiff = currentDate.getFullYear() - createDate.getFullYear()

  // 根据保密期限判断是否过期
  const secretYears = parseInt(archive.secretPeroid) || 0
  return yearsDiff > secretYears
}

// 获取鉴定原因
function getAppraisalReasons(archive) {
  const reasons = []

  if (!archive.retentionPeriod) reasons.push('保管期限缺失')
  if (!archive.secretLevel) reasons.push('保密级别缺失')
  if (!archive.secretPeroid) reasons.push('保密期限缺失')
  if (isRetentionExpired(archive)) reasons.push('保管期限过期')
  if (isSecretExpired(archive)) reasons.push('保密期限过期')

  return reasons
}

// 获取原因标签类型
function getReasonTagType(reason) {
  if (reason.includes('缺失')) return 'danger'
  if (reason.includes('过期')) return 'warning'
  return 'info'
}

// 处理鉴定档案选择变化
function handleAppraisalSelectionChange(selection) {
  selectedAppraisalIds.value = selection.map(item => item.id)
  currentAppraisalArchives.value = selection
}

// 打开批量鉴定弹窗
function openBatchAppraisalDialog() {
  batchAppraisalDialog.title = `批量鉴定 (${selectedAppraisalIds.value.length} 个档案)`
  batchAppraisalDialog.visible = true
  resetAppraisalForm()
}

// 单独鉴定
function handleSingleAppraisal(archive) {
  selectedAppraisalIds.value = [archive.id]
  currentAppraisalArchives.value = [archive]
  batchAppraisalDialog.title = `鉴定档案: ${archive.name}`
  batchAppraisalDialog.visible = true
  resetAppraisalForm()
}

// 重置鉴定表单
function resetAppraisalForm() {
  Object.assign(appraisalForm, {
    appraiser: '',
    // retentionPeriod: '',
    // secretLevel: '',
    // secretPeroid: '',
    afterRetentionPeriod: '',   // 修改
    afterSecretLevel: '',       // 修改
    afterSecretPeriod: '',      // 修改
    appraisalResult: 'archive',
    appraisalReason: ''
  })
  if (appraisalFormRef.value) {
    appraisalFormRef.value.resetFields()
  }
}

// 提交鉴定
function submitAppraisal() {
  appraisalFormRef.value.validate((valid) => {
    if (!valid) return

    appraisalSubmitting.value = true

    const appraisalData = {
      archiveIds: selectedAppraisalIds.value,
      ...appraisalForm
    }

    console.log('提交鉴定数据:', appraisalData);

    // 调用鉴定API
    submitIdentificationRecord(appraisalData).then(() => {
      proxy.$modal.msgSuccess('鉴定完成')
      batchAppraisalDialog.visible = false

      // 刷新列表
      getPendingAppraisalListData()
      getPendingAppraisalCountData()
      getList()
    }).catch((error) => {
      console.error('鉴定失败:', error);
      proxy.$modal.msgError('鉴定失败')
    }).finally(() => {
      appraisalSubmitting.value = false
    })
  })
}


// 批量销毁
// function handleBatchDestroy() {
//   proxy.$modal.confirm(`确定要销毁选中的 ${selectedAppraisalIds.value.length} 个档案吗？`, '确认销毁', {
//     type: 'warning'
//   }).then(() => {
//     // 调用销毁API
//     destroyArchives({ ids: selectedAppraisalIds.value }).then(() => {
//       proxy.$modal.msgSuccess('销毁完成')
//       getPendingAppraisalListData()
//       getPendingAppraisalCountData()
//       getList()
//     }).catch((error) => {
//       console.error('销毁失败:', error);
//       proxy.$modal.msgError('销毁失败')
//     })
//   })
// }

// // 单独销毁
// function handleSingleDestroy(archive) {
//   proxy.$modal.confirm(`确定要销毁档案 "${archive.name}" 吗？`, '确认销毁', {
//     type: 'warning'
//   }).then(() => {
//     destroyArchives({ ids: [archive.id] }).then(() => {
//       proxy.$modal.msgSuccess('销毁完成')
//       getPendingAppraisalListData()
//       getPendingAppraisalCountData()
//       getList()
//     }).catch((error) => {
//       console.error('销毁失败:', error);
//       proxy.$modal.msgError('销毁失败')
//     })
//   })
// }

// 鉴定弹窗中的单个删除方法
async function handleAppraisalDelete(row) {
  try {
    // 直接调用现有的删除逻辑
    await handleDelete(row);

    // 删除成功后刷新鉴定列表
    getPendingAppraisalListData();
    getPendingAppraisalCountData();
  } catch (error) {
    console.error('鉴定删除失败:', error);
  }
}


// 鉴定弹窗中的批量删除方法
async function handleBatchDelete() {
  if (selectedAppraisalIds.value.length === 0) {
    proxy.$modal.msgWarning("请选择要删除的档案");
    return;
  }

  // 检查选中的档案是否包含已归档档案
  const selectedArchives = pendingAppraisalList.value.filter(item =>
    selectedAppraisalIds.value.includes(item.id)
  );

  const hasArchivedArchives = selectedArchives.some(item => item.status === 'Archived');
  if (hasArchivedArchives) {
    proxy.$modal.msgWarning("选中的档案中包含已归档档案，不能删除");
    return;
  }

  let canDelete = true;

  // 确认删除
  await proxy.$modal.confirm(`是否确认删除选中的${selectedAppraisalIds.value.length}个档案？`);

  // 逐个检查是否有文档
  for (const id of selectedAppraisalIds.value) {
    try {
      const response = await listDocumentByArchiveId(id);
      const documentList = response.rows;

      if (documentList.length > 0) {
        proxy.$modal.msgWarning("请先删除该档案下的所有文件");
        canDelete = false;
        break;
      }
    } catch (error) {
      proxy.$modal.msgError("删除操作取消或出错");
      console.error('删除操作取消或出错:', error);
      canDelete = false;
      break;
    }
  }

  if (canDelete) {
    const delResponse = await delArchive(selectedAppraisalIds.value);
    if (delResponse.code === 200) {
      proxy.$modal.msgSuccess("删除成功");
      getPendingAppraisalListData();
      getPendingAppraisalCountData();
      getList(); // 刷新主列表
    }
  }
}



// 批量销毁方法
async function handleBatchDestroy() {
  // 二次检查：确保所有选中档案都可以销毁
  const selectedArchives = pendingAppraisalList.value.filter(item =>
    selectedAppraisalIds.value.includes(item.id)
  );

  const cannotDestroyArchives = selectedArchives.filter(item => !canDestroy(item));
  if (cannotDestroyArchives.length > 0) {
    proxy.$modal.msgWarning("选中的档案中包含不能销毁的档案，只有已归档且已上架的档案才能销毁");
    return;
  }

  try {
    await proxy.$modal.confirm(
      `确认提交选中的${selectedAppraisalIds.value.length}个档案的销毁申请吗？`,
      '确认销毁',
      {
        type: 'warning'
      }
    );

    // 调用销毁API
    destroyArchives({ ids: selectedAppraisalIds.value }).then(() => {
      proxy.$modal.msgSuccess('批量销毁申请提交成功');
      getPendingAppraisalListData();
      getPendingAppraisalCountData();
      getList();
    }).catch((error) => {
      console.error('批量销毁失败:', error);
      proxy.$modal.msgError('批量销毁失败');
    });
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量销毁失败:', error);
    }
  }
}

// // 批量删除方法
// async function handleBatchDelete() {
//   // 二次检查：确保所有选中档案都可以删除
//   const selectedArchives = pendingAppraisalList.value.filter(item =>
//       selectedAppraisalIds.value.includes(item.id)
//   );
//
//   const cannotDeleteArchives = selectedArchives.filter(item => !canDelete(item));
//   if (cannotDeleteArchives.length > 0) {
//     proxy.$modal.msgWarning("选中的档案中包含不能删除的档案，已归档且已上架的档案请使用销毁功能");
//     return;
//   }
//
//   let canDeleteAll = true;
//
//   try {
//     await proxy.$modal.confirm(
//         `确认删除选中的${selectedAppraisalIds.value.length}个档案吗？`,
//         '确认删除',
//         {
//           type: 'warning'
//         }
//     );
//
//     // 逐个检查是否有文档（使用你现有的逻辑）
//     for (const id of selectedAppraisalIds.value) {
//       try {
//         const response = await listDocumentByArchiveId(id);
//         const documentList = response.rows;
//
//         if (documentList.length > 0) {
//           proxy.$modal.msgWarning("请先删除该档案下的所有文件");
//           canDeleteAll = false;
//           break;
//         }
//       } catch (error) {
//         proxy.$modal.msgError("删除操作取消或出错");
//         console.error('删除操作取消或出错:', error);
//         canDeleteAll = false;
//         break;
//       }
//     }
//
//     if (canDeleteAll) {
//       const delResponse = await delArchive(selectedAppraisalIds.value);
//       if (delResponse.code === 200) {
//         proxy.$modal.msgSuccess("批量删除成功");
//         getPendingAppraisalListData();
//         getPendingAppraisalCountData();
//         getList();
//       }
//     }
//   } catch (error) {
//     if (error !== 'cancel') {
//       console.error('批量删除失败:', error);
//       proxy.$modal.msgError('批量删除失败');
//     }
//   }
// }



// 判断档案是否可以销毁
function canDestroy(row) {
  return row.status === 'Archived' && row.availability === 0;
}

// 判断档案是否可以删除
function canDelete(row) {
  return row.status !== 'Archived' || row.availability !== 0;
}

// 计算属性：是否可以批量销毁
const canBatchDestroy = computed(() => {
  if (selectedAppraisalIds.value.length === 0) {
    return false;
  }

  // 获取选中的档案
  const selectedArchives = pendingAppraisalList.value.filter(item =>
    selectedAppraisalIds.value.includes(item.id)
  );

  // 检查是否所有选中的档案都可以销毁
  return selectedArchives.every(item => canDestroy(item));
});

// 计算属性：是否可以批量删除
const canBatchDelete = computed(() => {
  if (selectedAppraisalIds.value.length === 0) {
    return false;
  }

  // 获取选中的档案
  const selectedArchives = pendingAppraisalList.value.filter(item =>
    selectedAppraisalIds.value.includes(item.id)
  );

  // 检查是否所有选中的档案都可以删除
  return selectedArchives.every(item => canDelete(item));
});

// 打开鉴定日志弹窗
function openAppraisalLogDialog() {
  appraisalLogDialogVisible.value = true
  getAppraisalLogList()
}

// 获取鉴定日志列表
function getAppraisalLogList() {
  const params = {
    ...logQueryParams,
    beginAppraisalTime: logQueryParams.appraisalDateRange?.[0],
    endAppraisalTime: logQueryParams.appraisalDateRange?.[1]
  }
  delete params.appraisalDateRange
  console.log('params', params)
  listIdentificationRecord(params).then(response => {
    appraisalLogList.value = response.rows || []
    appraisalLogTotal.value = response.total || 0
  }).catch((error) => {
    console.error('获取鉴定日志失败:', error);
    appraisalLogList.value = []
    appraisalLogTotal.value = 0
  })
}

// 重置日志查询
function resetLogQuery() {
  Object.assign(logQueryParams, {
    pageNum: 1,
    pageSize: 10,
    archiveName: '',
    appraiser: '',
    appraisalDateRange: []
  })
  getAppraisalLogList()
}

/** 查询档案列表列表 */
function getList() {
  loading.value = true;
  listArchive(queryParams.value).then(response => {
    archiveList.value = response.rows;
    total.value = response.total;
    loading.value = false;
  });
}

// 日志相关=============================================================================
const archiveLogDialogVisible = ref(false);
const borrowLogDialogVisible = ref(false);
const archiveTypeFilter = ref('all'); // 默认显示全部
const currentArchiveId = ref(null); // 存储当前档案ID
const borrowLogList = ref([]);
const archiveLogList = ref([]);

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
  });
}

/** 筛选日志列表 */
const filteredBorrowLogList = computed(() => {
  return borrowLogList.value.filter(log => {
    // 使用 borrowRemark 字段判断档案类型
    const isElectronic = log.borrowRemark && log.borrowRemark.includes('(电子借阅)');

    switch (archiveTypeFilter.value) {
      case 'paperElectronic':
        // 纸质+电子档案：第一次借阅
        return !isElectronic;
      case 'electronic':
        // 电子档案：后续借阅
        return isElectronic;
      default:
        return true; // 全部
    }
  });
});

/** 获取档案类型标签样式 */
function getArchiveTypeTagType(row) {
  const isElectronic = row.borrowRemark && row.borrowRemark.includes('(电子借阅)');
  return isElectronic ? 'success' : 'warning';
}

/** 获取档案类型显示文本 */
function getArchiveTypeText(row) {
  const borrowRemark = row.borrowRemark || '';
  const isElectronic = borrowRemark.includes('(电子借阅)');
  return isElectronic ? '电子档案' : '纸质+电子档案';
}

function formatInitiator(initiator) {
  if (!initiator) return '';

  // 处理格式如 "10063:李五" 的情况
  const parts = initiator.split(':');
  if (parts.length > 1) {
    return parts[1]; // 返回冒号后的部分
  }
  return initiator;
}

function formatHandler(handler) {
  if (!handler) return '';

  // 移除多余的逗号
  return handler.replace(/,$/, '');
}

/** 查看档案日志 */
function getArchiveLog(row) {
  archiveLogDialogVisible.value = true;
  // logQueryParams.value.archiveId = row.id;

  listOpLogs({ archiveId: row.id }).then(response => {
    archiveLogList.value = response.rows;
  });
}

// 通过角色id获取用户列表
const userList = ref([])
function getUserListByRoleId(roleId) {
  listUserByRoleId(roleId).then(response => {
    userList.value = response;
  });
}


// 移动架子的操作handleMove   likang
async function handleMove(row) {
  await handleMoveMeth(row, proxy)
}

// 简化解锁代码
/**
 * 解锁操作(未引用，但是别删)
 * @param ip
 * @param port
 * @returns {Promise<void>}
 */
async function unlockColumnSim(ip, port) {
  await unlockColumn(ip, port).  //原
    then(async (response) => {
      // console.log('解锁返回',response)
      if (response.data.data == "1") { //原
        ElMessage({
          message: '列解锁成功...',
          type: 'success',
          duration: 1500
        })
      }
    })
    .catch(error => {
      ElMessage({
        message: '解锁失败',
        type: 'error',
        duration: 5000
      })
    })
}

// 合并架子
async function handleCombine(row) {
  await handleCombineMeth(row, proxy)
}

// 取消按钮
function cancel() {
  open.value = false;
  reset();
}

// 表单重置
function reset() {
  form.value = {
    id: null,
    danghao: null,
    name: null,
    rfid: null,
    jianhao: null,
    secretLevel: null,
    secretPeroid: null,
    desecretDate: null,
    retentionPeriod: null,
    carrierType: null,
    startDate: null,
    endDate: null,
    guidangTime: null,
    shitiLocation: null,
    dianziLocation: null,
    directory: null,
    description: null,
    formationUnit: null,
    transferUnit: null,
    documentCount: null,
    status: null,
    authenticity: null,
    integrity: null,
    availability: null,
    security: null,
    categoryId: null,
    fondsId: null,
    createTime: null,
    createBy: null,
    updateTime: null,
    updateBy: null,
    remark: null
  };
  proxy.resetForm("archiveRef");
}

// 在methods中新增getProjectList方法
function getProjectList() {
  listProject({
    pageNum: 1,
    pageSize: 100,
    status: '0' // 只获取启用状态的项目
  }).then(response => {
    if (response.code === 200) {
      projectList.value = response.rows || [];
      console.log("项目列表加载成功:", projectList.value);
    } else {
      proxy.$modal.msgError(response.msg || "获取项目列表失败");
    }
  }).catch(error => {
    console.error("获取项目列表失败:", error);
    proxy.$modal.msgError("项目列表加载失败");
  });
}

/** 搜索按钮操作 */
function handleQuery() {
  queryParams.value.pageNum = 1;
  getList();
}

/** 重置按钮操作 */
function resetQuery() {
  proxy.resetForm("queryRef");
  queryParams.value.juanhao = null;
  queryParams.value.projectId = null;
  queryParams.value.fondsName = null;
  handleQuery();
}

// 多选框选中数据
function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.id);
  selectedItems.value = selection; // 新增：保存选中的完整项目信息
  single.value = selection.length != 1;
  multiple.value = !selection.length;
}

/** 新增按钮操作 */
function handleAdd() {
  proxy.$router.push({ path: "/manage/archive/arc-add" });
}

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


/** 查看详情操作 */
function handleDetail(row) {
  // ArchivePermission(row.id).then(response => {
  //   if (response.data) {
  proxy.$router.push({ path: "/manage/archive/arc-detail", query: { id: row.id } });
  //   } else {
  //     proxy.$modal.msgError("您没有权限查看，请先提交借阅申请");
  //   }
  // });
}

function handleAssemble(row) {
  queryParams.value.projectId = row.projectId;
  queryParams.value.juanhao = row.juanhao;
  getList();
}

/** 修改按钮操作 */
function handleUpdate(row) {
  // 新增：状态检查
  if (row && row.status === 'Archived') {
    proxy.$modal.msgWarning("已归档的档案不能修改");
    return;
  }

  // 处理批量修改的情况
  if (!row && selectedItems.value.length === 1) {
    const selectedRow = selectedItems.value[0];
    if (selectedRow.status === 'Archived') {
      proxy.$modal.msgWarning("已归档的档案不能修改");
      return;
    }
    proxy.$router.push({ path: "/manage/archive/arc-edit", query: { id: selectedRow.id } });
  } else if (row) {
    proxy.$router.push({ path: "/manage/archive/arc-edit", query: { id: row.id } });
  }
}

// /** 借阅按钮操作 */
// function handleBorrow(row) {
//   proxy.$router.push({
//     path: "/manage/record",
//     query: { record_id: row.id, record_name: row.name, record_danghao: row.danghao }
//   });
// }

/** 删除按钮操作 */
function handleDelete_process(row) {
  proxy.$router.push({
    path: "/manage/archive/ArchiveDestroyRecord",
    query: { delete_id: row.id, delete_name: row.name, delete_danghao: row.danghao }
  });
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["archiveRef"].validate(valid => {
    if (valid) {
      if (form.value.id != null) {
        updateArchive(form.value).then(response => {
          proxy.$modal.msgSuccess("修改成功");
          open.value = false;
          getList();
        });
      } else {
        addArchive(form.value).then(response => {
          proxy.$modal.msgSuccess("新增成功");
          open.value = false;
          getList();
        });
      }
    }
  });
}

/** 删除按钮操作 */
async function handleDelete(row) {
  // 新增：状态检查
  if (row && row.status === 'Archived') {
    proxy.$modal.msgWarning("已归档的档案不能删除");
    return;
  }

  // 如果 row 存在，则使用 row.id，否则使用 ids.value
  let _ids = row.id ? [row.id] : ids.value;
  if (_ids.length > 1) {
    proxy.$modal.msgError("请勿批量删除档案！");
    return;
  }

  // 新增：检查批量选择中是否包含已归档档案
  if (!row && hasArchivedInSelection.value) {
    proxy.$modal.msgWarning("选中的档案中包含已归档档案，不能删除");
    return;
  }

  let canDelete = 1;
  // 确认删除
  await proxy.$modal.confirm(`是否确认删除档案列表编号为"${_ids}"的数据项？`);

  for (const id of _ids) {

    try {
      const response = await listDocumentByArchiveId(id);
      const documentList = response.rows;

      if (documentList.length > 0) {
        proxy.$modal.msgWarning("请先删除该档案下的所有文件");
        canDelete = 0;
        console.log("canDelete = 0");
      }

    } catch (error) {
      proxy.$modal.msgError("删除操作取消或出错");
      console.error('删除操作取消或出错:', error);
    }
  }
  if (canDelete == 1) {
    const delResponse = await delArchive(_ids);
    if (delResponse.code === 200) {
      getList();
      proxy.$modal.msgSuccess("删除成功");
    }
  }

}

function collectFileIds(data) {
  let fileIds = [];

  // 如果当前节点有 files 属性，则将其添加到结果数组中
  if (data.files) {
    fileIds = fileIds.concat(data.files);
  }

  // 如果当前节点有 children 属性，则递归处理每个子节点
  if (data.children && data.children.length > 0) {
    data.children.forEach(child => {
      fileIds = fileIds.concat(collectFileIds(child));
    });
  }

  return fileIds;
}

// 处理整个数组
function collectAllFileIds(directory) {
  let allFileIds = [];

  directory.forEach(item => {
    allFileIds = allFileIds.concat(collectFileIds(item));
  });

  return allFileIds;
}

/** 导出按钮操作 */
function handleExport() {
  proxy.download('manage/archive/export', {
    ...queryParams.value
  }, `archive_${new Date().getTime()}.xlsx`)
}

// 添加档案柜相关的数据
const showCabinetView = ref(false) // 控制显示档案柜视图
const selectedQuNo = ref('1') // 选中的区号
const cabinetList = ref([]) // 档案柜列表
const compartmentList = ref([]) // 储物格列表
const cabinetCard = ref([]) // 档案柜卡片数据

// 加载档案柜数据
const loadCabinetData = async () => {
  try {
    // 获取档案柜列表
    const cabinetRes = await listCabinet({
      pageNum: 1,
      pageSize: 9999,
      repositoryId: 10000
    })
    cabinetList.value = cabinetRes.rows

    // 获取储物格列表
    const compartmentRes = await listCompartment()
    compartmentList.value = compartmentRes.data

    // 生成卡片数据
    generateCabinetCards()
  } catch (error) {
    console.error('加载档案柜数据失败:', error)
  }
}

// 生成档案柜卡片数据
// const generateCabinetCards = () => {
//   const mockData = []
//   const quNo = parseInt(selectedQuNo.value)

//   for (let colNo = 1; colNo <= 18; colNo++) {
//     mockData.push({
//       id: getMinIdByQuNoAndColNo(quNo, colNo),
//       quNo: quNo,
//       colNo: colNo,
//       capacity: 900,
//       size: getSize(quNo, colNo)
//     })
//   }

//   cabinetCard.value = mockData
// }

// 复用档案柜页面的辅助方法
// const getSize = (quNo, colNo) => {
//   const matched = cabinetList.value.filter(cab => cab.quNo === quNo && cab.colNo === colNo)
//   if (matched.length >= 2) {
//     return matched[0].size + matched[1].size
//   }
//   return matched.length > 0 ? matched[0].size : 0
// }

// const getMinIdByQuNoAndColNo = (quNo, colNo) => {
//   const matched = cabinetList.value.filter(cab => {
//     return cab.quNo === quNo && cab.colNo === colNo
//   })
//   if (matched.length === 0) return null
//   return Math.min(...matched.map(cab => cab.id))
// }

// const getCompartmentsByCabinetAndSide = (quNo, colNo, side) => {
//   const cabinet = cabinetList.value.find(cab => {
//     return cab.quNo === quNo && cab.colNo === colNo && cab.zyNo === side
//   })
//   if (!cabinet) return []
//   return compartmentList.value.filter(comp => comp.cabinetId === cabinet.id)
// }

const getCellClass = (comp) => {
  if (comp.size === 0) return 'cell-green'
  if (comp.size >= 1 && comp.size <= 14) return 'cell-yellow'
  if (comp.size >= 15) return 'cell-red'
  return ''
}

// 切换显示档案柜视图
const toggleCabinetView = () => {
  showCabinetView.value = !showCabinetView.value
  if (showCabinetView.value && cabinetList.value.length === 0) {
    loadCabinetData()
  }
}

// 选择某个格子
// const selectCompartment = (quNo, colNo, side, comp) => {
//   // 只允许选择空闲的格子
//   if (comp.size >= 15) {
//     ElMessage.warning('该位置已有档案，请选择空闲位置')
//     return
//   }

//   // 设置选中的位置
//   guidangForm.quHao = quNo.toString()
//   guidangForm.lieHao = colNo.toString()
//   guidangForm.ceHao = side
//   guidangForm.jieHao = comp.leNo.toString()
//   guidangForm.cengHao = comp.divNo.toString()

//   // 触发位置变化处理
//   handleLocationChange()

//   // 关闭档案柜视图
//   showCabinetView.value = false

//   ElMessage.success('已选择位置')
// }

// 切换区号
const handleQuNoChange = (val) => {
  selectedQuNo.value = val
  generateCabinetCards()
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

.check-empty-link .el-link {
  font-size: 16px;
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

/* 空闲 - 绿色 */
.cell-green {
  background-color: #67c23a !important;
}

/* 少量 - 黄色 */
.cell-yellow {
  background-color: #e6a23c !important;
}

/* 已满 - 红色 */
.cell-red {
  background-color: #f56c6c !important;
}

/* 图例颜色 */
.legend-box.cell-green {
  background-color: #67c23a;
}

.legend-box.cell-yellow {
  background-color: #e6a23c;
}

.legend-box.cell-red {
  background-color: #f56c6c;
}

/* 优化小格子的样式 */
.mini-cell {
  width: 12px;
  height: 12px;
  border: 1px solid #dcdfe6;
  cursor: pointer;
  transition: all 0.2s;
  background-color: #ffffff;
  /* 默认白色背景 */
}

.mini-cell:hover {
  transform: scale(1.2);
  border-color: #409EFF;
}

.mini-cell.cell-green:hover {
  box-shadow: 0 0 5px rgba(103, 194, 58, 0.5);
}

.mini-cell.cell-yellow:hover {
  box-shadow: 0 0 5px rgba(230, 162, 60, 0.5);
}

.mini-cell.cell-red:hover {
  box-shadow: 0 0 5px rgba(245, 108, 108, 0.5);
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

.operation-info-section {
  border: 1px solid #ebeef5;
  background-color: #f9f9f9;
}

.batch-operations {
  padding: 16px 0;
  border-bottom: 1px solid #ebeef5;
}

.missing-field {
  color: #f56c6c;
  font-weight: bold;
}

.expired-field {
  color: #e6a23c;
  font-weight: bold;
}

.appraisal-reasons {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.reason-tag {
  margin: 2px 0;
}

.status-change {
  font-size: 12px;
  line-height: 1.4;
}

.before-status {
  color: #909399;
}

.after-status {
  color: #67c23a;
  font-weight: 500;
}

.dialog-footer {
  text-align: right;
}

.destroy-content {
  padding: 10px 0;
}

.archive-info-section {
  border: 1px solid #ebeef5;
}

.single-archive p,
.multiple-archives p {
  margin: 8px 0;
  font-size: 14px;
}

/* 原有样式保持不变，新增以下样式 */
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
.report-number {
  position: absolute;
  top: 20px;
  right: 30px;
  font-size: 14px;
  opacity: 0.8;
}
.report-title {
  font-size: 28px;
  font-weight: bold;
  margin: 0 0 10px 0;
  letter-spacing: 2px;
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
/* 报告内容 */
.report-content {
  padding: 30px;
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

.overall-assessment {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 20px;
  border-radius: 12px;
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
  gap: 20px;
}

.assessment-content {
  display: flex;
  align-items: center;
  gap: 30px;
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

.dialog-footer {
  text-align: right;
}
.filter-container {
  margin-bottom: 20px;
  display: flex;
  justify-content: center;
}

.el-radio-group {
  margin-bottom: 15px;
}

.el-tag {
  margin-right: 5px;
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
