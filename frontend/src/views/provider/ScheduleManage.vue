<template>
  <div class="page-container">
    <!-- 店长视角：排班管理 -->
    <template v-if="isOwner">
      <!-- 顶部横幅 -->
      <div class="perm-banner owner">
        <span class="perm-icon">📋</span>
        <div class="perm-info">
          <strong>员工排班管理</strong>
          <p>为店员设置上班时间和班次，查看累计工作时长统计</p>
        </div>
        <el-button type="primary" @click="openAddDialog" style="margin-left:auto">
          <el-icon><Plus /></el-icon> 新增排班
        </el-button>
      </div>

      <!-- 筛选栏 -->
      <div class="filter-bar glass-card">
        <el-button-group>
          <el-button @click="viewMode='day'" :type="viewMode==='day'?'primary':''">今日</el-button>
          <el-button @click="viewMode='week'" :type="viewMode==='week'?'primary':''">本周</el-button>
          <el-button @click="viewMode='month'" :type="viewMode==='month'?'primary':''">本月</el-button>
        </el-button-group>
        <el-date-picker v-model="selectedDate" type="date" placeholder="选择日期"
          format="YYYY年MM月DD日" value-format="YYYY-MM-DD" style="width:180px" @change="fetchSchedules" />
        <el-select v-model="filterStaffId" placeholder="全部店员" clearable style="width:140px" @change="fetchSchedules">
          <el-option v-for="s in staffList" :key="s.id" :label="s.realName || s.username" :value="s.id" />
        </el-select>
        <el-button @click="showStats = !showStats" :type="showStats ? 'warning' : ''">
          📊 {{ showStats ? '隐藏统计' : '工作时长统计' }}
        </el-button>
      </div>

      <!-- 工作时长统计面板 -->
      <transition name="slide-down">
        <div v-if="showStats" class="stats-panel glass-card">
          <div class="stats-header">
            <h3>📊 员工工作时长统计</h3>
            <div class="stats-controls">
              <el-radio-group v-model="statsView" size="small" @change="fetchStats">
                <el-radio-button value="week">本周</el-radio-button>
                <el-radio-button value="month">本月</el-radio-button>
              </el-radio-group>
              <el-select v-model="statsSortBy" size="small" style="width:140px;margin-left:10px" @change="fetchStats">
                <el-option label="时长降序" value="hours" />
                <el-option label="时长升序" value="hours_asc" />
                <el-option label="班次降序" value="count" />
              </el-select>
            </div>
          </div>
          <div class="stats-cards" v-loading="statsLoading">
            <div v-for="stat in statsData" :key="stat.staffId" class="staff-stat-card"
              :class="{ 'low-hours': stat.totalHours < minHoursThreshold }">
              <div class="stat-avatar">{{ (stat.staffName || '?').charAt(0) }}</div>
              <div class="stat-detail">
                <div class="stat-name">{{ stat.staffName }}
                  <el-tag v-if="stat.totalHours < minHoursThreshold" type="danger" size="small" round class="warn-tag">工时不足</el-tag>
                </div>
                <div class="stat-phone">{{ stat.staffPhone }}</div>
              </div>
              <div class="stat-nums">
                <div class="stat-hours">{{ stat.totalHours }}<span class="unit">h</span></div>
                <div class="stat-shifts">{{ stat.shiftCount }} 班次</div>
              </div>
              <div class="stat-bar-wrap">
                <div class="stat-bar" :style="{ width: barWidth(stat.totalHours) }"
                  :class="{ low: stat.totalHours < minHoursThreshold }"></div>
              </div>
            </div>
            <el-empty v-if="!statsLoading && statsData.length === 0" description="暂无排班数据" :image-size="60" />
          </div>
        </div>
      </transition>

      <!-- 排班列表（表格视图） -->
      <div class="card schedule-table-wrap" v-loading="loading">
        <el-table :data="schedules" stripe style="width:100%">
          <el-table-column label="日期" width="120">
            <template #default="{ row }">
              <strong>{{ row.shiftDate }}</strong>
              <div class="weekday">{{ weekdayLabel(row.shiftDate) }}</div>
            </template>
          </el-table-column>
          <el-table-column label="店员" width="110">
            <template #default="{ row }">
              <div class="staff-cell">
                <div class="mini-avatar">{{ (row.staffName || '?').charAt(0) }}</div>
                {{ row.staffName }}
              </div>
            </template>
          </el-table-column>
          <el-table-column label="班次" width="90">
            <template #default="{ row }">
              <el-tag :type="shiftTagType(row.shiftType)" size="small" round>{{ shiftLabel(row.shiftType) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="上班时间" width="100">
            <template #default="{ row }">{{ row.startTime }}</template>
          </el-table-column>
          <el-table-column label="下班时间" width="100">
            <template #default="{ row }">{{ row.endTime }}</template>
          </el-table-column>
          <el-table-column label="工作时长" width="100">
            <template #default="{ row }">
              <span class="hours-text">{{ calcHours(row.startTime, row.endTime) }}h</span>
            </template>
          </el-table-column>
          <el-table-column label="备注" min-width="120">
            <template #default="{ row }">{{ row.remark || '-' }}</template>
          </el-table-column>
          <el-table-column label="操作" width="140" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" size="small" @click="openEditDialog(row)">编辑</el-button>
              <el-popconfirm title="确认删除此排班？" @confirm="handleDelete(row.id)">
                <template #reference>
                  <el-button link type="danger" size="small">删除</el-button>
                </template>
              </el-popconfirm>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-if="!loading && schedules.length === 0" description="暂无排班记录" :image-size="80" />
      </div>
    </template>

    <!-- 店员视角：我的排班 -->
    <template v-else>
      <div class="perm-banner staff">
        <span class="perm-icon">📅</span>
        <div class="perm-info">
          <strong>我的工作排班</strong>
          <p>查看您的排班安排和工作时长</p>
        </div>
      </div>

      <div class="filter-bar glass-card">
        <el-button-group>
          <el-button @click="viewMode='day'" :type="viewMode==='day'?'primary':''">今日</el-button>
          <el-button @click="viewMode='week'" :type="viewMode==='week'?'primary':''">本周</el-button>
          <el-button @click="viewMode='month'" :type="viewMode==='month'?'primary':''">本月</el-button>
        </el-button-group>
        <el-date-picker v-model="selectedDate" type="date" placeholder="选择日期"
          format="YYYY年MM月DD日" value-format="YYYY-MM-DD" style="width:180px" @change="fetchSchedules" />
      </div>

      <!-- 店员排班卡片 -->
      <div class="my-schedule-list" v-loading="loading">
        <div v-for="item in schedules" :key="item.id" class="my-schedule-card glass-card">
          <div class="my-date">
            <div class="my-date-day">{{ item.shiftDate?.split('-')[2] }}</div>
            <div class="my-date-month">{{ item.shiftDate?.split('-')[1] }}月</div>
            <div class="my-date-weekday">{{ weekdayLabel(item.shiftDate) }}</div>
          </div>
          <div class="my-detail">
            <el-tag :type="shiftTagType(item.shiftType)" size="small" round>{{ shiftLabel(item.shiftType) }}</el-tag>
            <span class="my-time">{{ item.startTime }} - {{ item.endTime }}</span>
            <span class="my-hours">{{ calcHours(item.startTime, item.endTime) }}h</span>
          </div>
          <div class="my-remark" v-if="item.remark">{{ item.remark }}</div>
        </div>
        <el-empty v-if="!loading && schedules.length === 0" description="暂无排班安排" :image-size="100" />
      </div>

      <!-- 店员工时统计 -->
      <div class="my-stats glass-card" v-if="schedules.length > 0">
        <div class="my-stat-item">
          <span class="my-stat-val">{{ schedules.length }}</span>
          <span class="my-stat-lbl">排班天数</span>
        </div>
        <div class="my-stat-item">
          <span class="my-stat-val">{{ myTotalHours }}</span>
          <span class="my-stat-lbl">累计工时</span>
        </div>
      </div>
    </template>

    <!-- 新增/编辑排班对话框 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑排班' : '新增排班'" width="520px" destroy-on-close>
      <el-form :model="form" label-width="90px" :rules="formRules" ref="formRef">
        <el-form-item label="店员" prop="staffId" v-if="!isEdit">
          <el-select v-model="form.staffId" placeholder="选择店员" style="width:100%">
            <el-option v-for="s in staffList" :key="s.id" :label="s.realName || s.username" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="排班日期" prop="shiftDate">
          <el-date-picker v-model="form.shiftDate" type="date" placeholder="选择日期"
            value-format="YYYY-MM-DD" style="width:100%" />
        </el-form-item>
        <el-form-item label="班次" prop="shiftType">
          <el-radio-group v-model="form.shiftType">
            <el-radio :value="0">早班</el-radio>
            <el-radio :value="1">中班</el-radio>
            <el-radio :value="2">晚班</el-radio>
            <el-radio :value="3">全天</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="上班时间" prop="startTime">
          <el-time-select v-model="form.startTime" :max-time="form.endTime"
            start="06:00" step="00:30" end="23:00" placeholder="上班时间" style="width:100%" />
        </el-form-item>
        <el-form-item label="下班时间" prop="endTime">
          <el-time-select v-model="form.endTime" :min-time="form.startTime"
            start="06:00" step="00:30" end="23:00" placeholder="下班时间" style="width:100%" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="可选备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">确认</el-button>
      </template>
    </el-dialog>

    <!-- 批量排班对话框 -->
    <el-dialog v-model="batchDialogVisible" title="批量排班（一周）" width="560px" destroy-on-close>
      <el-form :model="batchForm" label-width="90px" ref="batchFormRef">
        <el-form-item label="店员" required>
          <el-select v-model="batchForm.staffId" placeholder="选择店员" style="width:100%">
            <el-option v-for="s in staffList" :key="s.id" :label="s.realName || s.username" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="选择日期" required>
          <el-checkbox-group v-model="batchForm.shiftDates">
            <el-checkbox v-for="d in weekDates" :key="d.value" :value="d.value">
              {{ d.label }}
            </el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <el-form-item label="班次">
          <el-radio-group v-model="batchForm.shiftType">
            <el-radio :value="0">早班</el-radio>
            <el-radio :value="1">中班</el-radio>
            <el-radio :value="2">晚班</el-radio>
            <el-radio :value="3">全天</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="上班时间" required>
          <el-time-select v-model="batchForm.startTime" :max-time="batchForm.endTime"
            start="06:00" step="00:30" end="23:00" placeholder="上班时间" style="width:100%" />
        </el-form-item>
        <el-form-item label="下班时间" required>
          <el-time-select v-model="batchForm.endTime" :min-time="batchForm.startTime"
            start="06:00" step="00:30" end="23:00" placeholder="下班时间" style="width:100%" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="batchForm.remark" type="textarea" :rows="2" placeholder="可选备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="batchDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleBatchSubmit" :loading="submitting">确认排班</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, reactive } from 'vue'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const userStore = useUserStore()
const loading = ref(false)
const statsLoading = ref(false)
const schedules = ref([])
const statsData = ref([])
const staffList = ref([])
const viewMode = ref('week')
const selectedDate = ref(new Date().toISOString().slice(0, 10))
const filterStaffId = ref(null)
const showStats = ref(false)
const statsView = ref('month')
const statsSortBy = ref('hours')
const minHoursThreshold = 40

const isOwner = computed(() => {
  const r = userStore.userInfo.role
  if (r === 2) return true
  return r === 1 && (userStore.userInfo.staffRole == null || userStore.userInfo.staffRole === 1)
})

// 班次标签
const shiftLabels = { 0: '早班', 1: '中班', 2: '晚班', 3: '全天' }
const shiftTagTypes = { 0: 'warning', 1: 'primary', 2: 'info', 3: 'success' }
function shiftLabel(t) { return shiftLabels[t] ?? '未知' }
function shiftTagType(t) { return shiftTagTypes[t] ?? 'info' }

// 计算工作时长
function calcHours(start, end) {
  if (!start || !end) return 0
  const [sh, sm] = start.split(':').map(Number)
  const [eh, em] = end.split(':').map(Number)
  return Math.max(0, Math.round((eh + em / 60 - sh - sm / 60) * 10) / 10)
}

// 星期标签
const weekNames = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
function weekdayLabel(dateStr) {
  if (!dateStr) return ''
  const d = new Date(dateStr + 'T00:00:00')
  return weekNames[d.getDay()]
}

// 店员总工时
const myTotalHours = computed(() =>
  schedules.value.reduce((sum, s) => sum + calcHours(s.startTime, s.endTime), 0).toFixed(1)
)

// 统计进度条宽度
const maxHours = computed(() => Math.max(...statsData.value.map(s => s.totalHours), 1))
function barWidth(hours) { return Math.round(hours / maxHours.value * 100) + '%' }

// ===== 获取数据 =====
async function fetchSchedules() {
  loading.value = true
  try {
    const url = isOwner.value ? '/schedules/provider' : '/schedules/staff'
    const params = { view: viewMode.value, date: selectedDate.value }
    if (isOwner.value && filterStaffId.value) params.staffId = filterStaffId.value
    const res = await request.get(url, { params })
    schedules.value = res.data || []
  } catch (e) { console.error('获取排班失败', e) }
  finally { loading.value = false }
}

async function fetchStaffList() {
  if (!isOwner.value) return
  try {
    const res = await request.get('/shop/available-staff')
    staffList.value = (res.data || []).filter(s => s.staffRole === 2)
  } catch (e) { console.error('获取店员列表失败', e) }
}

async function fetchStats() {
  if (!isOwner.value) return
  statsLoading.value = true
  try {
    const res = await request.get('/schedules/stats', {
      params: { view: statsView.value, date: selectedDate.value, sortBy: statsSortBy.value }
    })
    statsData.value = res.data || []
  } catch (e) { console.error('获取统计失败', e) }
  finally { statsLoading.value = false }
}

// ===== 排班表单 =====
const dialogVisible = ref(false)
const batchDialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref(null)
const batchFormRef = ref(null)
const form = reactive({
  id: null, staffId: null, shiftDate: '', startTime: '', endTime: '', shiftType: 0, remark: ''
})
const batchForm = reactive({
  staffId: null, shiftDates: [], startTime: '09:00', endTime: '18:00', shiftType: 0, remark: ''
})

const formRules = {
  staffId: [{ required: true, message: '请选择店员', trigger: 'change' }],
  shiftDate: [{ required: true, message: '请选择日期', trigger: 'change' }],
  startTime: [{ required: true, message: '请选择上班时间', trigger: 'change' }],
  endTime: [{ required: true, message: '请选择下班时间', trigger: 'change' }],
}

// 本周日期选项
const weekDates = computed(() => {
  const d = new Date(selectedDate.value + 'T00:00:00')
  const monday = new Date(d)
  monday.setDate(d.getDate() - ((d.getDay() + 6) % 7))
  const dates = []
  const dayNames = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
  for (let i = 0; i < 7; i++) {
    const day = new Date(monday)
    day.setDate(monday.getDate() + i)
    const ds = day.toISOString().slice(0, 10)
    dates.push({ value: ds, label: `${dayNames[i]} ${ds}` })
  }
  return dates
})

function openAddDialog() {
  isEdit.value = false
  Object.assign(form, { id: null, staffId: null, shiftDate: '', startTime: '09:00', endTime: '18:00', shiftType: 0, remark: '' })
  dialogVisible.value = true
}

function openEditDialog(row) {
  isEdit.value = true
  Object.assign(form, {
    id: row.id, staffId: row.staffId, shiftDate: row.shiftDate,
    startTime: row.startTime, endTime: row.endTime, shiftType: row.shiftType, remark: row.remark
  })
  dialogVisible.value = true
}

async function handleSubmit() {
  if (!form.staffId || !form.shiftDate || !form.startTime || !form.endTime) {
    return ElMessage.warning('请填写完整排班信息')
  }
  submitting.value = true
  try {
    if (isEdit.value) {
      await request.put(`/schedules/${form.id}`, form)
      ElMessage.success('排班已更新')
    } else {
      await request.post('/schedules', form)
      ElMessage.success('排班已创建')
    }
    dialogVisible.value = false
    fetchSchedules()
    if (showStats.value) fetchStats()
  } catch (e) { console.error('保存失败', e) }
  finally { submitting.value = false }
}

async function handleBatchSubmit() {
  if (!batchForm.staffId || batchForm.shiftDates.length === 0 || !batchForm.startTime || !batchForm.endTime) {
    return ElMessage.warning('请填写完整批量排班信息')
  }
  submitting.value = true
  try {
    await request.post('/schedules/batch', batchForm)
    ElMessage.success(`已为 ${batchForm.shiftDates.length} 天创建排班`)
    batchDialogVisible.value = false
    fetchSchedules()
    if (showStats.value) fetchStats()
  } catch (e) { console.error('批量排班失败', e) }
  finally { submitting.value = false }
}

async function handleDelete(id) {
  try {
    await request.delete(`/schedules/${id}`)
    ElMessage.success('排班已删除')
    fetchSchedules()
    if (showStats.value) fetchStats()
  } catch (e) { console.error('删除失败', e) }
}

onMounted(() => {
  fetchSchedules()
  fetchStaffList()
})
</script>

<style scoped>
.perm-banner {
  display: flex; align-items: center; gap: 14px; padding: 16px 22px;
  border-radius: var(--radius-md); margin-bottom: 20px;
}
.perm-banner.owner { background: linear-gradient(135deg, rgba(255,140,66,0.08), rgba(255,209,102,0.06)); border: 1.5px solid rgba(255,140,66,0.15); }
.perm-banner.staff { background: linear-gradient(135deg, rgba(103,194,58,0.06), rgba(156,195,230,0.04)); border: 1.5px solid rgba(103,194,58,0.1); }
.perm-icon { font-size: 28px; }
.perm-info strong { display: block; color: var(--color-text); margin-bottom: 2px; }
.perm-info p { color: var(--color-text-secondary); margin: 0; font-size: 13px; }

.filter-bar {
  display: flex; align-items: center; gap: 14px; padding: 14px 20px;
  margin-bottom: 18px; flex-wrap: wrap;
}

/* 统计面板 */
.stats-panel {
  padding: 20px; margin-bottom: 18px;
}
.stats-header {
  display: flex; align-items: center; justify-content: space-between; margin-bottom: 16px; flex-wrap: wrap; gap: 10px;
}
.stats-header h3 { font-size: 16px; font-weight: 700; margin: 0; }
.stats-controls { display: flex; align-items: center; flex-wrap: wrap; gap: 8px; }
.stats-cards { display: flex; flex-direction: column; gap: 10px; }
.staff-stat-card {
  display: flex; align-items: center; gap: 14px; padding: 14px 18px;
  background: #fff; border-radius: 10px; border: 1px solid rgba(0,0,0,0.05);
  transition: all 0.2s;
}
.staff-stat-card:hover { box-shadow: 0 4px 16px rgba(0,0,0,0.06); }
.staff-stat-card.low-hours { border-left: 3px solid #F56C6C; background: rgba(245,108,108,0.03); }
.stat-avatar {
  width: 42px; height: 42px; border-radius: 50%; background: linear-gradient(135deg, #FF8C42, #E6A23C);
  color: #fff; display: flex; align-items: center; justify-content: center; font-weight: 700; font-size: 16px; flex-shrink: 0;
}
.stat-detail { min-width: 100px; }
.stat-name { font-weight: 600; font-size: 14px; display: flex; align-items: center; gap: 6px; }
.stat-phone { color: var(--color-text-secondary); font-size: 12px; margin-top: 2px; }
.warn-tag { font-size: 10px; }
.stat-nums { text-align: right; min-width: 80px; }
.stat-hours { font-family: 'Poppins'; font-size: 22px; font-weight: 800; color: var(--color-text); }
.stat-hours .unit { font-size: 12px; font-weight: 400; color: var(--color-text-secondary); margin-left: 2px; }
.stat-shifts { font-size: 12px; color: var(--color-text-secondary); }
.stat-bar-wrap {
  flex: 1; height: 8px; background: rgba(0,0,0,0.04); border-radius: 4px; overflow: hidden; min-width: 80px;
}
.stat-bar { height: 100%; border-radius: 4px; background: linear-gradient(90deg, #67C23A, #409EFF); transition: width 0.5s; }
.stat-bar.low { background: linear-gradient(90deg, #F56C6C, #E6A23C); }

/* 排班表格 */
.schedule-table-wrap { padding: 16px; }
.weekday { font-size: 11px; color: var(--color-text-secondary); }
.staff-cell { display: flex; align-items: center; gap: 6px; }
.mini-avatar {
  width: 24px; height: 24px; border-radius: 50%; background: linear-gradient(135deg, #FF8C42, #E6A23C);
  color: #fff; display: flex; align-items: center; justify-content: center; font-size: 11px; font-weight: 600; flex-shrink: 0;
}
.hours-text { font-family: 'Poppins'; font-weight: 700; color: var(--color-primary); }

/* 店员排班卡片 */
.my-schedule-list { display: flex; flex-direction: column; gap: 10px; margin-bottom: 18px; }
.my-schedule-card {
  display: flex; align-items: center; gap: 18px; padding: 16px 20px; border-radius: 12px;
}
.my-date {
  text-align: center; min-width: 54px; padding-right: 16px;
  border-right: 2px solid rgba(0,0,0,0.06);
}
.my-date-day { font-family: 'Poppins'; font-size: 26px; font-weight: 800; line-height: 1; color: var(--color-text); }
.my-date-month { font-size: 12px; color: var(--color-text-secondary); }
.my-date-weekday { font-size: 11px; color: var(--color-primary); font-weight: 600; margin-top: 2px; }
.my-detail { display: flex; align-items: center; gap: 10px; flex: 1; }
.my-time { font-weight: 600; font-size: 15px; }
.my-hours {
  font-family: 'Poppins'; font-weight: 700; color: var(--color-primary); font-size: 14px;
  background: rgba(255,140,66,0.08); padding: 2px 8px; border-radius: 6px;
}
.my-remark { color: var(--color-text-secondary); font-size: 13px; }

/* 店员统计 */
.my-stats {
  display: flex; gap: 30px; padding: 20px 28px; justify-content: center;
}
.my-stat-item { text-align: center; }
.my-stat-val { font-family: 'Poppins'; font-size: 28px; font-weight: 800; color: var(--color-text); display: block; }
.my-stat-lbl { font-size: 13px; color: var(--color-text-secondary); }

/* 动画 */
.slide-down-enter-active, .slide-down-leave-active { transition: all 0.3s ease; max-height: 600px; }
.slide-down-enter-from, .slide-down-leave-to { max-height: 0; opacity: 0; padding: 0 20px; overflow: hidden; }

@media(max-width: 768px) {
  .filter-bar { flex-wrap: wrap; }
  .staff-stat-card { flex-wrap: wrap; }
  .stat-bar-wrap { min-width: 100%; }
}
</style>
