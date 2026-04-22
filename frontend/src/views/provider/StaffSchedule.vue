<template>
  <div class="page-container">
    <!-- 权限提示 -->
    <div class="perm-banner" :class="isOwner ? 'owner' : 'staff'">
      <span class="perm-icon">{{ isOwner ? '👑' : '🧑‍🍳' }}</span>
      <div class="perm-info">
        <strong>{{ isOwner ? '店铺日程总览 - 全部员工排班' : '我的工作排班 - 个人预约' }}</strong>
        <p>{{ isOwner ? '查看所有员工的预约安排，支持按日/周/月切换' : '查看您的个人预约排班，按小时精确显示' }}</p>
      </div>
    </div>

    <!-- 日期切换 -->
    <div class="date-toolbar glass-card">
      <el-button-group>
        <el-button @click="viewMode='day'" :type="viewMode==='day'?'primary':''">今日</el-button>
        <el-button @click="viewMode='week'" :type="viewMode==='week'?'primary':''">本周</el-button>
        <el-button @click="viewMode='month'" :type="viewMode==='month'?'primary':''">本月</el-button>
      </el-button-group>
      <span class="current-date">{{ displayDate }}</span>
      <el-date-picker v-model="selectedDate" type="date" placeholder="选择日期"
        format="YYYY年MM月DD日" value-format="YYYY-MM-DD" style="width:180px" @change="fetchSchedule" />
    </div>

    <!-- 日视图：时间线 -->
    <template v-if="viewMode === 'day' || viewMode === 'week'">
      <div class="schedule-card card" v-loading="loading">
        <div class="timeline-header">
          <h3><span>⏰</span> {{ viewMode === 'day' ? '今日排班' : '本周排班' }}</h3>
          <span class="order-count">{{ scheduleOrders.length }} 个预约</span>
        </div>

        <!-- 时间网格 -->
        <div class="time-grid" v-if="!loading">
          <div class="time-labels">
            <div v-for="h in hours" :key="h" class="hour-label">
              {{ String(h).padStart(2,'0') }}:00
            </div>
          </div>
          <div class="time-slots">
            <div v-for="h in hours" :key="'slot-'+h" class="hour-slot" :class="{ active: hasAppointmentAt(h), current: isCurrentHour(h) }">
              <!-- 预约卡片 -->
              <div v-if="getAppointmentsAt(h).length > 0" class="appointment-cards">
                <div v-for="apt in getAppointmentsAt(h)" :key="apt.id" class="appt-card"
                  :style="{ borderLeftColor: apt.statusColor }">
                  <div class="appt-time">{{ apt.timeStr }}</div>
                  <div class="apct-service">{{ apt.serviceName }}</div>
                  <div class="apct-pet">🐾 {{ apt.petName || '宠物' }} · {{ customerAnonym(apt) }}</div>
                  <el-tag size="small" :type="apt.statusType" round>{{ apt.statusText }}</el-tag>
                </div>
              </div>
              <div v-else class="empty-slot"></div>
            </div>
          </div>
        </div>

        <el-empty v-if="!loading && scheduleOrders.length === 0" description="暂无预约安排" :image-size="100" />
      </div>
    </template>

    <!-- 月视图：列表 -->
    <template v-if="viewMode === 'month'">
      <div class="card month-view">
        <el-table :data="scheduleOrders" stripe v-loading="loading" class="schedule-table">
          <el-table-column label="时间" width="170">
            <template #default="{ row }"><strong>{{ formatDate(row.appointmentTime) }}</strong></template>
          </el-table-column>
          <el-table-column prop="orderNo" label="订单号" width="150" />
          <el-table-column label="服务项目" min-width="120">
            <template #default="{ row }">{{ row.serviceName || '-' }}</template>
          </el-table-column>
          <el-table-column label="客户/宠物" min-width="140">
            <template #default="{ row }">🐾 {{ row.petName || '-' }} · {{ customerAnonym(row) }}</template>
          </el-table-column>
          <el-table-column label="金额" width="90">
            <template #default="{ row }"><span class="amount">¥{{ row.totalAmount }}</span></template>
          </el-table-column>
          <el-table-column label="状态" width="90">
            <template #default="{ row }">
              <el-tag :type="statusTag(row.status)" size="small" round>{{ statusText(row.status) }}</el-tag>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-if="!loading && scheduleOrders.length === 0" description="当月无预约记录" :image-size="80" />
      </div>
    </template>

    <!-- 统计概览 -->
    <el-row :gutter="14" class="stats-row">
      <el-col :xs="12" :sm="6">
        <div class="stat-card glass-card"><div class="stat-icon">📅</div><div class="stat-data"><span class="stat-value">{{ todayCount }}</span><span class="stat-label">今日预约</span></div></div>
      </el-col>
      <el-col :xs="12" :sm="6">
        <div class="stat-card glass-card"><div class="stat-icon">✅</div><div class="stat-data"><span class="stat-value">{{ completedCount }}</span><span class="stat-label">已完成</span></div></div>
      </el-col>
      <el-col :xs="12" :sm="6">
        <div class="stat-card glass-card"><div class="stat-icon">⏳</div><div class="stat-data"><span class="stat-value">{{ pendingCount }}</span><span class="stat-label">待服务</span></div></div>
      </el-col>
      <el-col :xs="12" :sm="6">
        <div class="stat-card glass-card"><div class="stat-icon">💰</div><div class="stat-data"><span class="stat-value">¥{{ todayIncome }}</span><span class="stat-label">今日收入</span></div></div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import request from '@/utils/request'

const userStore = useUserStore()
const loading = ref(false)
const viewMode = ref('day')
const selectedDate = ref(new Date().toISOString().slice(0, 10))
const scheduleOrders = ref([])

const isOwner = computed(() => {
  const r = userStore.userInfo.role
  if (r === 2) return true
  return r === 1 && (userStore.userInfo.staffRole == null || userStore.userInfo.staffRole === 1)
})

// 时间轴（8:00 - 21:00）
const hours = Array.from({ length: 14 }, (_, i) => i + 8)

const displayDate = computed(() => {
  const d = new Date(selectedDate.value)
  return `${d.getFullYear()}年${d.getMonth()+1}月${d.getDate()}日`
})

const todayCount = computed(() => scheduleOrders.value.filter(o => {
  if (!o.appointmentTime) return false
  const d = new Date(o.appointmentTime)
  return d.toDateString() === new Date(selectedDate.value).toDateString()
}).length)

const completedCount = computed(() => scheduleOrders.value.filter(o => o.status === 3).length)
const pendingCount = computed(() => scheduleOrders.value.filter(o => [1,2].includes(o.status)).length)
const todayIncome = computed(() => {
  return scheduleOrders.value.filter(o => o.status === 3 && (() => {
    const d = new Date(o.appointmentTime); return d.toDateString() === new Date(selectedDate.value).toDateString()
  })()).reduce((sum, o) => sum + (parseFloat(o.totalAmount) || 0), 0).toFixed(2)
})

function isCurrentHour(h) {
  if (viewMode.value !== 'day') return false
  const now = new Date()
  return now.getHours() === h && new Date(selectedDate.value).toDateString() === now.toDateString()
}

function hasAppointmentAt(hour) {
  return getAppointmentsAt(hour).length > 0
}

function getAppointmentsAt(hour) {
  return scheduleOrders.value.filter(o => {
    if (!o.appointmentTime) return false
    const d = new Date(o.appointmentTime)
    // 日视图只看当天，周视图看本周同一天
    if (viewMode.value === 'day') {
      return d.getHours() === hour && d.toDateString() === new Date(selectedDate.value).toDateString()
    }
    return d.getHours() === hour
  }).map(o => ({
    ...o,
    timeStr: `${String(d.getMonth()+1)}-${d.getDate()} ${String(d.getHours()).padStart(2,'0')}:${String(d.getMinutes()).padStart(2,'0')}`,
    statusColor: { 1:'#E6A23C', 2:'#409EFF', 3:'#67C23A', 4:'#F56C6C', 5:'#F56C6C', 0:'#909399' }[o.status] || '#ccc',
    statusType: { 1:'warning', 2:'primary', 3:'success', 4:'info', 5:'danger', 0:'info' }[o.status],
    statusText: { 0:'待支付', 1:'待接单', 2:'服务中', 3:'已完成', 4:'已取消', 5:'退款中' }[o.status] || '未知',
    customerHint: o.userName ? (o.userName.length > 1 ? o.userName.substring(0,1)+'***' : '*'+o.userName) : '匿名'
  }))
}

function customerAnonym(row) {
  if (!row.userName) return '匿名'
  return row.userName.length > 1 ? row.userName.charAt(0) + '**' : '*'
}

function statusTag(s) { return s==3?'success':s==1?'warning':s==2?'primary':s==4?'info':'danger' }
function statusText(s) { return {0:'待支付',1:'待接单',2:'服务中',3:'已完成',4:'已取消',5:'退款中'}[s]||'未知' }
function formatDate(t) { return t ? new Date(t).toLocaleString() : '-' }

async function fetchSchedule() {
  loading.value = true
  try {
    const res = await request.get('/orders', { params: { page: 1, size: 200 } })
    const all = res.data?.records || []
    let filtered = Array.isArray(all) ? all.filter(o => o.appointmentTime) : []
    // 店员视角：只看分配给自己的订单
    if (!isOwner.value && userStore.userInfo.role === 1 && userStore.userInfo.staffRole === 2) {
      filtered = filtered.filter(o => o.staffId === userStore.userInfo.id)
    }
    scheduleOrders.value = filtered
  } catch (e) {} finally { loading.value = false }
}

onMounted(fetchSchedule)
</script>

<style scoped>
.perm-banner {
  display: flex; align-items: center; gap: 14px; padding: 14px 20px;
  border-radius: var(--radius-md); margin-bottom: 20px; font-size: 14px;
}
.perm-banner.owner { background: linear-gradient(135deg, rgba(255,140,66,0.08), rgba(255,209,102,0.06)); border: 1.5px solid rgba(255,140,66,0.15); }
.perm-banner.staff { background: linear-gradient(135deg, rgba(103,194,58,0.06), rgba(156,195,230,0.04)); border: 1.5px solid rgba(103,194,58,0.1); }
.perm-icon { font-size: 28px; }
.perm-info strong { display: block; color: var(--color-text); margin-bottom: 2px; }
.perm-info p { color: var(--color-text-secondary); margin: 0; font-size: 13px; }

.date-toolbar {
  display: flex; align-items: center; gap: 16px; padding: 16px 22px;
  margin-bottom: 20px;
}
.current-date { font-family:'Poppins'; font-weight:700; font-size:16px; color:var(--color-primary); }

.schedule-card { padding: 20px; }
.timeline-header { display:flex; align-items:center; justify-content:space-between; margin-bottom:18px; }
.timeline-header h3 { font-size:17px; font-weight:700; display:flex; align-items:center; gap:8px; margin:0; }
.order-count { font-size:13px; color:var(--color-text-placeholder); background:var(--color-cream); padding:3px 10px; border-radius:10px; }

/* 时间轴网格 */
.time-grid { display:flex; gap:0; overflow-x:auto; }
.time-labels { display:flex; flex-direction:column; gap:0; padding-top:36px; min-width:60px; }
.hour-label { height:52px; text-align:right; padding-right:8px; font-size:11px; color:var(--color-text-placeholder); line-height:52px; border-right:1px dashed var(--color-divider); }
.time-slots { flex:1; display:flex; flex-direction:column; gap:0; }

.hour-slot {
  height:52px; position:relative; border-bottom:1px solid rgba(0,0,0,0.04);
  transition:background 0.2s;
}
.hour-slot.active { background: rgba(255,140,66,0.04); }
.hour-slot.current { background: rgba(64,158,255,0.08); box-shadow:inset 0 0 0 2px rgba(64,158,255,0.2); }
.empty-slot { width:100%; height:100%; }

.appointment-cards { padding:2px 6px; display:flex; gap:4px; flex-wrap:wrap; align-content:flex-start; }
.appt-card {
  padding:6px 10px; border-radius:6px; border-left-width:3px; background:#fff;
  box-shadow:0 1px 4px rgba(0,0,0,0.06); font-size:12px; min-width:160px;
}
.apct-time { color:var(--color-primary); font-weight:600; font-size:11px; }
.apct-service { font-weight:600; color:var(--color-text); margin:1px 0; }
.apct-pet { color:var(--color-text-secondary); font-size:11px; margin-bottom:3px; }

.stats-row { margin-bottom: 20px; }
.stat-card {
  display:flex; align-items:center; gap:14px; padding:18px; border-radius:var(--radius-lg);
}
.stat-icon { font-size:28px; }
.stat-data { display:flex; flex-direction:column; gap:2px; }
.stat-value { font-family:'Poppins'; font-size:22px; font-weight:800; color:var(--color-text); }
.stat-label { font-size:12px; color:var(--color-text-placeholder); }
.amount { color:var(--color-primary); font-weight:600; }

.month-view { padding: 20px; }
@media(max-width:768px){ .stats-row .el-col{margin-bottom:10px;} .time-grid{overflow-x:auto} }
</style>
