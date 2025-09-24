<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="任务名称" prop="jobName">
        <el-input
            v-model="queryParams.jobName"
            placeholder="请输入任务名称"
            clearable
            style="width: 200px"
            @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="任务分组" prop="jobGroup">
        <el-select v-model="queryParams.jobGroup" placeholder="请选择任务分组" clearable style="width: 200px">
          <el-option
              v-for="dict in sys_job_group"
              :key="dict.value"
              :label="dict.label"
              :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="任务状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择任务状态" clearable style="width: 200px">
          <el-option
              v-for="dict in sys_job_status"
              :key="dict.value"
              :label="dict.label"
              :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
            type="primary"
            plain
            icon="Plus"
            @click="handleAdd"
            v-hasPermi="['monitor:job:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
            type="success"
            plain
            icon="Edit"
            :disabled="single"
            @click="handleUpdate"
            v-hasPermi="['monitor:job:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
            type="danger"
            plain
            icon="Delete"
            :disabled="multiple"
            @click="handleDelete"
            v-hasPermi="['monitor:job:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
            type="warning"
            plain
            icon="Download"
            @click="handleExport"
            v-hasPermi="['monitor:job:export']"
        >导出</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
            type="info"
            plain
            icon="Operation"
            @click="handleJobLog"
            v-hasPermi="['monitor:job:query']"
        >执行记录</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="jobList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="任务编号" width="100" align="center" prop="jobId" />
      <el-table-column label="任务名称" align="center" prop="jobName" :show-overflow-tooltip="true" />
      <el-table-column label="任务分组" align="center" prop="jobGroup">
        <template #default="scope">
          <dict-tag :options="sys_job_group" :value="scope.row.jobGroup" />
        </template>
      </el-table-column>
      <el-table-column label="执行方法" align="center" prop="invokeTarget" :show-overflow-tooltip="true">
        <template #default="scope">
          <el-tooltip :content="scope.row.invokeTarget" placement="top">
            <span>{{ formatInvokeTarget(scope.row.invokeTarget) }}</span>
          </el-tooltip>
        </template>
      </el-table-column>
      <el-table-column label="执行时间规则" align="center" width="180">
        <template #default="scope">
          <el-tooltip :content="scope.row.cronExpression" placement="top">
            <span>{{ formatCronToReadable(scope.row.cronExpression) }}</span>
          </el-tooltip>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center">
        <template #default="scope">
          <el-switch
              v-model="scope.row.status"
              active-value="0"
              inactive-value="1"
              active-text="启用"
              inactive-text="停用"
              @change="handleStatusChange(scope.row)"
          ></el-switch>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="200" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-tooltip content="修改" placement="top">
            <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['monitor:job:edit']"></el-button>
          </el-tooltip>
          <el-tooltip content="删除" placement="top">
            <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['monitor:job:remove']"></el-button>
          </el-tooltip>
          <el-tooltip content="立即执行" placement="top">
            <el-button link type="primary" icon="CaretRight" @click="handleRun(scope.row)" v-hasPermi="['monitor:job:changeStatus']"></el-button>
          </el-tooltip>
          <el-tooltip content="任务详情" placement="top">
            <el-button link type="primary" icon="View" @click="handleView(scope.row)" v-hasPermi="['monitor:job:query']"></el-button>
          </el-tooltip>
          <el-tooltip content="执行记录" placement="top">
            <el-button link type="primary" icon="Operation" @click="handleJobLog(scope.row)" v-hasPermi="['monitor:job:query']"></el-button>
          </el-tooltip>
        </template>
      </el-table-column>
    </el-table>

    <pagination
        v-show="total > 0"
        :total="total"
        v-model:page="queryParams.pageNum"
        v-model:limit="queryParams.pageSize"
        @pagination="getList"
    />

    <!-- 添加或修改定时任务对话框 -->
    <el-dialog :title="title" v-model="open" width="820px" append-to-body>
      <el-form ref="jobRef" :model="form" :rules="rules" label-width="120px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="任务名称" prop="jobName">
              <el-input v-model="form.jobName" placeholder="请输入任务名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="任务分组" prop="jobGroup">
              <el-select v-model="form.jobGroup" placeholder="请选择任务分组">
                <el-option
                    v-for="dict in sys_job_group"
                    :key="dict.value"
                    :label="dict.label"
                    :value="dict.value"
                ></el-option>
              </el-select>
            </el-form-item>
          </el-col>
<!--          <el-col :span="24">-->
<!--            <el-form-item prop="invokeTarget">-->
<!--              <template #label>-->
<!--                        <span>-->
<!--                           执行方法-->
<!--                           <el-tooltip placement="top">-->
<!--                              <template #content>-->
<!--                                 <div>-->
<!--                                    <p>指定要执行的任务方法，格式为：类名.方法名(参数)</p>-->
<!--                                    <p>示例：</p>-->
<!--                                    <p>- ryTask.ryNoParams：执行ryTask类的ryNoParams方法，无参数</p>-->
<!--                                    <p>- ryTask.ryParams('ry')：执行ryTask类的ryParams方法，参数为'ry'</p>-->
<!--                                    <p>支持字符串、布尔值、整数、浮点数等参数类型</p>-->
<!--                                 </div>-->
<!--                              </template>-->
<!--                              <el-icon><question-filled /></el-icon>-->
<!--                           </el-tooltip>-->
<!--                        </span>-->
<!--              </template>-->
<!--              <el-input v-model="form.invokeTarget" placeholder="请输入要执行的方法，如：ryTask.ryNoParams" />-->
<!--            </el-form-item>-->
<!--          </el-col>-->
          <!-- 新增的时间规则选择器 -->
          <el-col :span="24">
            <el-form-item label="执行频率">
              <el-radio-group v-model="timeRuleType" @change="handleTimeRuleChange">
                <el-radio-button label="daily">每日</el-radio-button>
                <el-radio-button label="weekly">每周</el-radio-button>
                <el-radio-button label="monthly">每月</el-radio-button>
                <el-radio-button label="hourly">每小时</el-radio-button>
<!--                <el-radio-button label="custom">自定义</el-radio-button>-->
              </el-radio-group>
            </el-form-item>
          </el-col>

          <el-col :span="24" v-if="timeRuleType !== 'custom'">
            <el-form-item label="具体时间">
              <!-- 每日选择 -->
              <div v-if="timeRuleType === 'daily'" class="time-selector">
                <span>每天</span>
                <el-select
                    v-model="dailyHour"
                    placeholder="小时"
                    style="width: 80px"
                    @change="updateCronExpression">
                  <el-option
                      v-for="hour in 24"
                      :key="hour"
                      :label="String(hour).padStart(2, '0')"
                      :value="String(hour).padStart(2, '0')"
                  ></el-option>
                </el-select>
                <span>:</span>
                <el-select
                    v-model="dailyMinute"
                    placeholder="分钟"
                    style="width: 80px"
                    @change="updateCronExpression">
                  <el-option
                      v-for="minute in 60"
                      :key="minute"
                      :label="String(minute).padStart(2, '0')"
                      :value="String(minute).padStart(2, '0')"
                  ></el-option>
                </el-select>
                <span>执行</span>
              </div>

              <!-- 每周选择 -->
              <div v-if="timeRuleType === 'weekly'" class="time-selector">
                <span>每周</span>
                <el-select v-model="weeklyDay" placeholder="选择星期" @change="updateCronExpression">
                  <el-option label="周一" value="1"></el-option>
                  <el-option label="周二" value="2"></el-option>
                  <el-option label="周三" value="3"></el-option>
                  <el-option label="周四" value="4"></el-option>
                  <el-option label="周五" value="5"></el-option>
                  <el-option label="周六" value="6"></el-option>
                  <el-option label="周日" value="7"></el-option>
                </el-select>
                <el-time-picker
                    v-model="weeklyTime"
                    format="HH:mm"
                    value-format="HH:mm"
                    placeholder="选择时间"
                    @change="updateCronExpression"
                />
                <span>执行</span>
              </div>

              <!-- 每月选择 -->
              <div v-if="timeRuleType === 'monthly'" class="time-selector">
                <span>每月</span>
                <el-select v-model="monthlyDay" placeholder="选择日期" @change="updateCronExpression">
                  <el-option
                      v-for="day in 31"
                      :key="day"
                      :label="`${day}号`"
                      :value="day"
                  ></el-option>
                </el-select>
                <el-time-picker
                    v-model="monthlyTime"
                    format="HH:mm"
                    value-format="HH:mm"
                    placeholder="选择时间"
                    @change="updateCronExpression"
                />
                <span>执行</span>
              </div>

              <!-- 每小时选择 -->
              <div v-if="timeRuleType === 'hourly'" class="time-selector">
                <span>每</span>
                <el-input-number
                    v-model="hourlyInterval"
                    :min="1"
                    :max="23"
                    controls-position="right"
                    @change="updateCronExpression"
                />
                <span>小时执行一次</span>
              </div>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="执行时间规则" prop="cronExpression">
              <!-- 使用只读输入框显示绿色提示文字 -->
              <el-input
                  :value="getCronDescription(form.cronExpression)"
                  readonly
                  placeholder="请选择执行频率和时间"
                  class="cron-description-input"
              >
                <!-- 移除原来的生成器按钮 -->
              </el-input>
<!--                <template #append>-->
<!--                  <el-button type="primary" @click="handleShowCron">-->
<!--                    时间规则生成器-->
<!--                    <i class="el-icon-time el-icon&#45;&#45;right"></i>-->
<!--                  </el-button>-->
<!--                </template>-->
<!--              <div class="cron-description" v-if="form.cronExpression">-->
<!--                {{ getCronDescription(form.cronExpression) }}-->
<!--              </div>-->
            </el-form-item>
          </el-col>
          <el-col :span="24" v-if="form.jobId !== undefined">
            <el-form-item label="状态">
              <el-radio-group v-model="form.status">
                <el-radio
                    v-for="dict in sys_job_status"
                    :key="dict.value"
                    :label="dict.value"
                >{{ dict.label }}</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="错过执行策略" prop="misfirePolicy">
              <el-radio-group v-model="form.misfirePolicy">
                <el-radio-button label="1">
                  立即执行
                  <el-tooltip content="如果任务错过执行时间，立即执行一次">
                    <el-icon><question-filled /></el-icon>
                  </el-tooltip>
                </el-radio-button>
                <el-radio-button label="2">
                  执行一次
                  <el-tooltip content="如果任务错过执行时间，只执行一次错过的任务">
                    <el-icon><question-filled /></el-icon>
                  </el-tooltip>
                  </el-radio-button>
<!--                  <el-radio-button label="3">-->
<!--                    放弃执行-->
<!--                    <el-tooltip content="如果任务错过执行时间，不再执行">-->
<!--                      <el-icon><question-filled /></el-icon>-->
<!--                    </el-tooltip>-->
<!--                  </el-radio-button>-->
              </el-radio-group>
            </el-form-item>
          </el-col>
<!--          <el-col :span="12">-->
<!--            <el-form-item label="是否允许并发" prop="concurrent">-->
<!--              <el-radio-group v-model="form.concurrent">-->
<!--                <el-radio-button label="0">-->
<!--                  允许-->
<!--                  <el-tooltip content="允许同一任务同时执行多个实例">-->
<!--                    <el-icon><question-filled /></el-icon>-->
<!--                  </el-tooltip>-->
<!--                </el-radio-button>-->
<!--                <el-radio-button label="1">-->
<!--                  禁止-->
<!--                  <el-tooltip content="同一时间只允许执行一个任务实例">-->
<!--                    <el-icon><question-filled /></el-icon>-->
<!--                  </el-tooltip>-->
<!--                </el-radio-button>-->
<!--              </el-radio-group>-->
<!--            </el-form-item>-->
<!--          </el-col>-->
        </el-row>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog title="时间规则生成器" v-model="openCron" append-to-body destroy-on-close>
      <crontab ref="crontabRef" @hide="openCron=false" @fill="crontabFill" :expression="expression"></crontab>
    </el-dialog>

    <!-- 任务详情对话框 -->
    <el-dialog title="任务详情" v-model="openView" width="700px" append-to-body>
      <el-form :model="form" label-width="120px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="任务编号：">{{ form.jobId }}</el-form-item>
            <el-form-item label="任务名称：">{{ form.jobName }}</el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="任务分组：">{{ jobGroupFormat(form) }}</el-form-item>
            <el-form-item label="创建时间：">{{ form.createTime }}</el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="时间规则：">{{ form.cronExpression }}</el-form-item>
            <el-form-item label="规则说明：">{{ getCronDescription(form.cronExpression) }}</el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="下次执行时间：">{{ parseTime(form.nextValidTime) }}</el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="执行方法：">{{ form.invokeTarget }}</el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="任务状态：">
              <dict-tag :options="sys_job_status" :value="form.status" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="是否允许并发：">
              <span v-if="form.concurrent == 0">允许</span>
              <span v-else-if="form.concurrent == 1">禁止</span>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="错过执行策略：">
              <span v-if="form.misfirePolicy == 1">立即执行</span>
              <span v-else-if="form.misfirePolicy == 2">执行一次</span>
<!--              <span v-else-if="form.misfirePolicy == 3">放弃执行</span>-->
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="openView = false">关 闭</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="Job">
import { listJob, getJob, delJob, addJob, updateJob, runJob, changeJobStatus } from "@/api/monitor/job";
import Crontab from '@/components/Crontab'
const router = useRouter();
const { proxy } = getCurrentInstance();
const { sys_job_group, sys_job_status } = proxy.useDict("sys_job_group", "sys_job_status");

const jobList = ref([]);
const open = ref(false);
const loading = ref(true);
const showSearch = ref(true);
const ids = ref([]);
const single = ref(true);
const multiple = ref(true);
const total = ref(0);
const title = ref("");
const openView = ref(false);
const openCron = ref(false);
const expression = ref("");

// 在data或setup中添加
const dailyHour = ref('00');
const dailyMinute = ref('00');

// 在setup()函数内部添加响应式数据
const timeRuleType = ref('daily');
const dailyTime = ref('00:00');
const weeklyDay = ref('1');
const weeklyTime = ref('00:00');
const monthlyDay = ref('1');
const monthlyTime = ref('00:00');
const hourlyInterval = ref(1);

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    jobName: undefined,
    jobGroup: undefined,
    status: undefined
  },
  rules: {
    jobName: [{ required: true, message: "任务名称不能为空", trigger: "blur" }],
    invokeTarget: [{ required: true, message: "执行方法不能为空", trigger: "blur" }],
    cronExpression: [{ required: true, message: "执行时间规则不能为空", trigger: "change" }]
  }
});

const { queryParams, form, rules } = toRefs(data);

/** 查询定时任务列表 */
function getList() {
  loading.value = true;
  listJob(queryParams.value).then(response => {
    jobList.value = response.rows;
    total.value = response.total;
    loading.value = false;
  });
}

// 处理时间规则类型变化
function handleTimeRuleChange(type) {
  if (type !== 'custom') {
    updateCronExpression();
  }
}

// 更新updateCronExpression方法
const updateCronExpression = () => {
  let cronExpression = '';

  switch (timeRuleType.value) {
    case 'daily':
      // 确保值有效
      const hour = dailyHour.value || '00';
      const minute = dailyMinute.value || '00';
      cronExpression = `0 ${minute} ${hour} * * ?`;
      break;

    case 'weekly':
      // 每周逻辑
      const [weeklyHour, weeklyMinute] = weeklyTime.value.split(':');
      cronExpression = `0 ${weeklyMinute} ${weeklyHour} ? * ${weeklyDay.value}`;
      break;

    case 'monthly':
      // 每月逻辑
      const [monthlyHour, monthlyMinute] = monthlyTime.value.split(':');
      cronExpression = `0 ${monthlyMinute} ${monthlyHour} ${monthlyDay.value} * ?`;
      break;

    case 'hourly':
      // 每小时逻辑
      cronExpression = `0 0 */${hourlyInterval.value} * * ?`;
      break;

    default:
      // 自定义模式不自动生成
      return;
  }

  form.value.cronExpression = cronExpression;
};


// 从cron表达式解析时间规则
function parseCronExpression(cronExpression) {
  if (!cronExpression) return;

  const parts = cronExpression.split(' ');
  if (parts.length !== 6) return;

  const [second, minute, hour, day, month, week] = parts;

  // 尝试解析每日规则
  if (day === '*' && month === '*' && week === '?') {
    timeRuleType.value = 'daily';
    dailyTime.value = `${hour}:${minute}`;
    return;
  }

  // 尝试解析每周规则
  if (day === '?' && month === '*' && week !== '*' && week !== '?') {
    timeRuleType.value = 'weekly';
    weeklyDay.value = week;
    weeklyTime.value = `${hour}:${minute}`;
    return;
  }

  // 尝试解析每月规则
  if (day !== '*' && day !== '?' && month === '*' && week === '?') {
    timeRuleType.value = 'monthly';
    monthlyDay.value = day;
    monthlyTime.value = `${hour}:${minute}`;
    return;
  }

  // 尝试解析每小时规则
  if (minute === '0' && hour.startsWith('*/') && day === '*' && month === '*' && week === '?') {
    timeRuleType.value = 'hourly';
    hourlyInterval.value = parseInt(hour.split('/')[1]);
    return;
  }

  // 如果不匹配任何预设规则，使用自定义模式
  timeRuleType.value = 'custom';
}

/** 格式化执行方法显示 */
function formatInvokeTarget(invokeTarget) {
  if (!invokeTarget) return '';
  // 简化显示，只显示方法名部分
  const parts = invokeTarget.split('.');
  if (parts.length > 1) {
    return parts[1] + '()';
  }
  return invokeTarget;
}

/** 格式化cron表达式显示 */
function formatCronExpression(cronExpression) {
  if (!cronExpression) return '';
  // 简化显示，只显示关键部分
  return cronExpression;
}

/** 获取cron表达式描述 */
function getCronDescription(cronExpression) {
  if (!cronExpression) return '';

  try {
    const parts = cronExpression.split(' ');
    if (parts.length !== 6 && parts.length !== 7) return '时间格式不正确';

    const second = parts[0];
    const minute = parts[1];
    const hour = parts[2];
    const day = parts[3];
    const month = parts[4];
    const week = parts[5];

    let description = '';

    // 解析秒
    if (second === '*') {
      description += '每秒';
    } else if (second.includes('/')) {
      const interval = second.split('/')[1];
      description += `每${interval}秒`;
    } else {
      description += `在${second}秒`;
    }

    // 解析分钟
    if (minute === '*') {
      description += '';
    } else if (minute.includes('/')) {
      const interval = minute.split('/')[1];
      description += `，每${interval}分钟`;
    } else {
      description += `，在${minute}分钟`;
    }

    // 解析小时
    if (hour === '*') {
      description += '';
    } else if (hour.includes('/')) {
      const interval = hour.split('/')[1];
      description += `，每${interval}小时`;
    } else {
      description += `，在${hour}点`;
    }

    // 解析日期
    if (day === '*') {
      description += '';
    } else if (day !== '?') {
      description += `，每月${day}日`;
    }

    // 解析星期
    if (week !== '*') {
      if (week === '1') description += '，每周日';
      else if (week === '2') description += '，每周一';
      else if (week === '3') description += '，每周二';
      else if (week === '4') description += '，每周三';
      else if (week === '5') description += '，每周四';
      else if (week === '6') description += '，每周五';
      else if (week === '7') description += '，每周六';
    }

    return description + '执行一次';
  } catch (e) {
    return '时间格式说明';
  }
}

/** 任务组名字典翻译 */
function jobGroupFormat(row, column) {
  return proxy.selectDictLabel(sys_job_group.value, row.jobGroup);
}

/** 取消按钮 */
function cancel() {
  open.value = false;
  reset();
}

/** 表单重置 */
// 修改reset方法
function reset() {
  form.value = {
    jobId: undefined,
    jobName: undefined,
    jobGroup: undefined,
    invokeTarget: undefined,
    cronExpression: undefined,
    misfirePolicy: 1,
    concurrent: 1,
    status: "0"
  };

  // 重置时间变量
  timeRuleType.value = 'daily';
  dailyTime.value = '00:00';
  weeklyDay.value = '1';
  weeklyTime.value = '00:00';
  monthlyDay.value = '1';
  monthlyTime.value = '00:00';
  hourlyInterval.value = 1;
  timeRuleType.value = 'daily';
  dailyHour.value = '00';
  dailyMinute.value = '00';

  proxy.resetForm("jobRef");
}

// 在您的script部分添加这个函数
function formatCronToReadable(cronExpression) {
  if (!cronExpression) return '未设置';

  try {
    const parts = cronExpression.split(' ');
    if (parts.length < 5) return '格式错误';

    const [minute, hour, dayOfMonth, month, dayOfWeek] = parts;

    let description = '';

    // 解析分钟
    if (minute === '*') {
      description += '每分钟';
    } else if (minute.includes('/')) {
      const interval = minute.split('/')[1];
      description += `每${interval}分钟`;
    } else if (minute.includes('-')) {
      const range = minute.split('-');
      description += `${range[0]}分到${range[1]}分`;
    } else if (minute !== '*') {
      description += `${minute}分`;
    }

    // 解析小时
    if (hour === '*') {
      description += '';
    } else if (hour.includes('/')) {
      const interval = hour.split('/')[1];
      description += `每${interval}小时`;
    } else if (hour.includes('-')) {
      const range = hour.split('-');
      description += `${range[0]}点到${range[1]}点`;
    } else if (hour !== '*') {
      description += `${hour}点`;
    }

    // 解析日期
    if (dayOfMonth === '*') {
      description += '';
    } else if (dayOfMonth.includes('/')) {
      const interval = dayOfMonth.split('/')[1];
      description += `每${interval}天`;
    } else if (dayOfMonth.includes('-')) {
      const range = dayOfMonth.split('-');
      description += `${range[0]}日到${range[1]}日`;
    } else if (dayOfMonth !== '*') {
      description += `${dayOfMonth}日`;
    }

    // 解析月份
    if (month === '*') {
      description += '';
    } else if (month.includes('/')) {
      const interval = month.split('/')[1];
      description += `每${interval}个月`;
    } else if (month.includes('-')) {
      const range = month.split('-');
      description += `${range[0]}月到${range[1]}月`;
    } else if (month !== '*') {
      const months = ['一月', '二月', '三月', '四月', '五月', '六月', '七月', '八月', '九月', '十月', '十一月', '十二月'];
      description += `${months[parseInt(month) - 1] || month}月`;
    }

    // 解析星期
    if (dayOfWeek === '*') {
      description += '';
    } else if (dayOfWeek.includes('/')) {
      const interval = dayOfWeek.split('/')[1];
      description += `每${interval}周`;
    } else if (dayOfWeek.includes('-')) {
      const range = dayOfWeek.split('-');
      description += `${range[0]}到${range[1]}`;
    } else if (dayOfWeek !== '*') {
      const days = ['周日', '周一', '周二', '周三', '周四', '周五', '周六'];
      description += days[parseInt(dayOfWeek)] || dayOfWeek;
    }

    // 处理特殊情况
    if (minute === '0' && hour.includes('/')) {
      const interval = hour.split('/')[1];
      description = `每${interval}小时整点执行`;
    }

    if (minute.includes('0/') && hour === '*') {
      const interval = minute.split('/')[1];
      description = `每${interval}分钟执行`;
    }

    return description + '执行';
  } catch (e) {
    console.error('解析cron表达式错误:', e);
    return cronExpression;
  }
}

/** 搜索按钮操作 */
function handleQuery() {
  queryParams.value.pageNum = 1;
  getList();
}

/** 重置按钮操作 */
function resetQuery() {
  proxy.resetForm("queryRef");
  handleQuery();
}

// 多选框选中数据
function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.jobId);
  single.value = selection.length != 1;
  multiple.value = !selection.length;
}

// 任务状态修改
function handleStatusChange(row) {
  let text = row.status === "0" ? "启用" : "停用";
  proxy.$modal.confirm('确认要"' + text + '""' + row.jobName + '"任务吗?').then(function () {
    return changeJobStatus(row.jobId, row.status);
  }).then(() => {
    proxy.$modal.msgSuccess(text + "成功");
  }).catch(function () {
    row.status = row.status === "0" ? "1" : "0";
  });
}

/* 立即执行一次 */
function handleRun(row) {
  proxy.$modal.confirm('确认要立即执行一次"' + row.jobName + '"任务吗?').then(function () {
    return runJob(row.jobId, row.jobGroup);
  }).then(() => {
    proxy.$modal.msgSuccess("执行成功");})
      .catch(() => {});
}

/** 任务详细信息 */
function handleView(row) {
  getJob(row.jobId).then(response => {
    form.value = response.data;
    openView.value = true;
  });
}

/** cron表达式按钮操作 */
function handleShowCron() {
  expression.value = form.value.cronExpression;
  openCron.value = true;
}

/** 确定后回传值 */
function crontabFill(value) {
  form.value.cronExpression = value;
}

/** 任务日志列表查询 */
function handleJobLog(row) {
  const jobId = row.jobId || 0;
  router.push('/monitor/job-log/index/' + jobId)
}

/** 新增按钮操作 */
// 修改handleAdd方法
function handleAdd() {
  reset();
  open.value = true;
  title.value = "添加任务";

  // 设置默认时间规则
  timeRuleType.value = 'daily';
  dailyTime.value = '00:00';
  updateCronExpression();
}

// 修改handleUpdate方法
function handleUpdate(row) {
  reset();
  const jobId = row.jobId || ids.value;
  getJob(jobId).then(response => {
    form.value = response.data;
    open.value = true;
    title.value = "修改任务";

    // 解析cron表达式，设置时间规则
    parseCronExpression(form.value.cronExpression);
  });
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["jobRef"].validate(valid => {
    if (valid) {
      if (form.value.jobId != undefined) {
        updateJob(form.value).then(response => {
          proxy.$modal.msgSuccess("修改成功");
          open.value = false;
          getList();
        });
      } else {
        addJob(form.value).then(response => {
          proxy.$modal.msgSuccess("新增成功");
          open.value = false;
          getList();
        });
      }
    }
  });
}

/** 删除按钮操作 */
function handleDelete(row) {
  const jobIds = row.jobId || ids.value;
  proxy.$modal.confirm('是否确认删除定时任务编号为"' + jobIds + '"的数据项?').then(function () {
    return delJob(jobIds);
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess("删除成功");
  }).catch(() => {});
}

/** 导出按钮操作 */
function handleExport() {
  proxy.download("monitor/job/export", {
    ...queryParams.value,
  }, `job_${new Date().getTime()}.xlsx`);
}

getList();
</script>

<style scoped>
.cron-description {
  font-size: 12px;
  color: #67c23a;
  margin-top: 5px;
  padding: 5px;
  background-color: #f0f9eb;
  border-radius: 4px;
}
.time-selector {
  display: flex;
  align-items: center;
  gap: 8px;
}

.time-selector .el-select,
.time-selector .el-time-picker,
.time-selector .el-input-number {
  width: 120px;
}

.cron-readonly {
  font-size: 13px;
  color: #606266;
  line-height: 1.4;
  padding: 8px;
  background-color: #f5f7fa;
  border-radius: 4px;
  margin-top: 4px;
}
.time-selector {
  display: flex;
  align-items: center;
  gap: 8px;
}

.time-selector .el-time-picker {
  width: 120px;
}

/* 确保时间选择器下拉菜单使用24小时格式 */
.el-time-panel {
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
}

.el-time-spinner__wrapper .el-scrollbar__wrap {
  font-family: monospace;
}

/* 确保选择器中的时间显示为24小时格式 */
.el-time-panel__content::after {
  content: "24小时制";
  position: absolute;
  right: 10px;
  bottom: 5px;
  font-size: 12px;
  color: #909399;
}
</style>