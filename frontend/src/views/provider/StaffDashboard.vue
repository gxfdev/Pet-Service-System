<template>
  <div class="page-container">
    <!-- 欢迎横幅 -->
    <div class="welcome-banner glass-card">
      <div class="banner-left">
        <span class="banner-emoji">👋</span>
        <div>
          <h2>您好，{{ userStore.userInfo.realName || userStore.userInfo.username || '店员' }}!</h2>
          <p>今天有 <strong>{{ todayIncome.totalOrders || 0 }}</strong> 个订单待处理</p>
        </div>
      </div>
      <el-tag :type="salaryTagType" effect="dark" round size="large">{{ salaryLabel }}</el-tag>
    </div>

    <!-- 今日实时收入（根据店主设定的薪资制度动态展示） -->
    <div class="today-income-banner glass-card">
      <div class="income-left">
        <span class="income-icon">💰</span>
        <div>
          <h3>今日实时收入</h3>

          <!-- 时薪制：显示工时、时薪、加班费 -->
          <template v-if="todayIncome.salaryMode === 1">
            <div class="income-detail">
              <span class="income-big">¥{{ todayIncome.todayEarnings || '0.00' }}</span>
              <span class="income-sub">
                工作时长 <strong>{{ todayIncome.todayHours || 0 }}</strong> 小时 ·
                正常时薪 ¥{{ todayIncome.hourlyRate || 18 }} ·
                加班时薪 ¥{{ todayIncome.overtimeRate || 25 }} ·
                已完成 {{ todayIncome.completedCount || 0 }} 单
              </span>
            </div>
          </template>

          <!-- 固定月薪：显示月薪金额 -->
          <template v-else-if="todayIncome.salaryMode === 2">
            <div class="income-detail">
              <span class="income-big">月薪 ¥{{ todayIncome.fixedMonthlySalary || '0.00' }}</span>
              <span class="income-sub">
                固定月薪制 · 今日完成 {{ todayIncome.completedCount || 0 }} 单 ·
                今日订单总额 ¥{{ todayIncome.orderTotal || '0' }}
              </span>
            </div>
          </template>

          <!-- 提成制：显示提成率和单数 -->
          <template v-else>
            <div class="income-detail">
              <span class="income-big">¥{{ todayIncome.todayEarnings || todayIncome.orderTotal || '0.00' }}</span>
              <span class="income-sub">
                每单提成 <strong>{{ todayIncome.commissionRate || 0 }}</strong> 元 ·
                完成 {{ todayIncome.completedCount || 0 }} 单 ·
                共 {{ todayIncome.totalOrders || 0 }} 单
              </span>
            </div>
          </template>
        </div>
      </div>
      <div class="income-right">
        <el-tag type="warning" effect="dark" round>{{ salaryLabel }}</el-tag>
      </div>
    </div>

    <!-- 我的订单数据 -->
    <el-row :gutter="16" class="stats-row">
      <el-col :xs="12" :sm="8">
        <div class="stat-card glass-card">
          <div class="stat-ring blue">
            <svg viewBox="0 0 100 100"><circle cx="50" cy="50" r="42" fill="none" stroke="#F0E6D8" stroke-width="8"/>
            <circle cx="50" cy="50" r="42" fill="none" stroke="#FF8C42" stroke-width="8"
              :stroke-dasharray="264" :stroke-dashoffset="264 - (264 * (stats.completedOrders||0) / Math.max((stats.completedOrders||0)+(stats.pendingOrders||0),1))"
              stroke-linecap="round" transform="rotate(-90 50 50)" style="transition:stroke-dashoffset 0.8s ease"/></svg>
            <div class="ring-value">{{ stats.completedOrders || 0 }}</div>
          </div>
          <div class="stat-info"><h3>已完成</h3><p>订单数</p></div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="8">
        <div class="stat-card glass-card">
          <div class="stat-ring orange">
            <svg viewBox="0 0 100 100"><circle cx="50" cy="50" r="42" fill="none" stroke="#F0E6D8" stroke-width="8"/>
            <circle cx="50" cy="50" r="42" fill="none" stroke="#E6A23C" stroke-width="8"
              :stroke-dasharray="264" :stroke-dashoffset="264 - (264 * (stats.pendingOrders||0) / Math.max((stats.completedOrders||0)+(stats.pendingOrders||0),1))"
              stroke-linecap="round" transform="rotate(-90 50 50)" style="transition:stroke-dashoffset 0.8s ease"/></svg>
            <div class="ring-value">{{ stats.pendingOrders || 0 }}</div>
          </div>
          <div class="stat-info"><h3>待处理</h3><p>订单数</p></div>
        </div>
      </el-col>
      <el-col :xs="24" :sm="8">
        <div class="stat-card glass-card">
          <div class="stat-ring green">
            <svg viewBox="0 0 100 100"><circle cx="50" cy="50" r="42" fill="none" stroke="#F0E6D8" stroke-width="8"/>
            <circle cx="50" cy="50" r="42" fill="none" stroke="#98DDCA" stroke-width="8"
              :stroke-dasharray="264" :stroke-dashoffset="stats.totalIncome > 0 ? Math.max(10, 264 - (Math.min(parseFloat(stats.totalIncome)/200,1)*254)) : 230"
              stroke-linecap="round" transform="rotate(-90 50 50)" style="transition:stroke-dashoffset 0.8s ease"/></svg>
            <div class="ring-value">¥{{ stats.totalIncome || '0' }}</div>
          </div>
          <div class="stat-info"><h3>累计收益</h3><p>预估</p></div>
        </div>
      </el-col>
    </el-row>

    <!-- 薪资制度说明卡片 -->
    <div class="salary-info-card glass-card">
      <div class="si-header">
        <span class="si-icon">📋</span>
        <h3>我的薪资制度</h3>
        <el-tag :type="salaryTagType" size="small" round>{{ salaryLabel }}</el-tag>
      </div>
      <div class="si-body">
        <template v-if="todayIncome.salaryMode === 1">
          <div class="si-grid">
            <div class="si-item">
              <span class="si-label">正常时薪</span>
              <strong class="si-val primary">¥{{ todayIncome.hourlyRate || 18 }}/小时</strong>
            </div>
            <div class="si-item">
              <span class="si-label">加班时薪</span>
              <strong class="si-val warning">¥{{ todayIncome.overtimeRate || 25 }}/小时</strong>
            </div>
            <div class="si-item">
              <span class="si-label">今日工时</span>
              <strong class="si-val">{{ todayIncome.todayHours || 0 }} 小时</strong>
            </div>
          </div>
          <p class="si-note">* 薪资制度由店主设定，每日8小时内按正常时薪计算，超出部分按加班时薪计算。</p>
        </template>
        <template v-else-if="todayIncome.salaryMode === 2">
          <div class="si-grid">
            <div class="si-item">
              <span class="si-label">固定月薪</span>
              <strong class="si-val primary">¥{{ todayIncome.fixedMonthlySalary || '0.00' }}/月</strong>
            </div>
            <div class="si-item">
              <span class="si-label">本月已完成</span>
              <strong class="si-val">{{ todayIncome.completedCount || 0 }} 单</strong>
            </div>
          </div>
          <p class="si-note">* 固定月薪制由店主设定，每月固定发放，不受订单数量影响。</p>
        </template>
        <template v-else>
          <div class="si-grid">
            <div class="si-item">
              <span class="si-label">每单提成</span>
              <strong class="si-val primary">¥{{ todayIncome.commissionRate || 0 }}/单</strong>
            </div>
            <div class="si-item">
              <span class="si-label">本月完成</span>
              <strong class="si-val">{{ todayIncome.completedCount || 0 }} 单</strong>
            </div>
            <div class="si-item">
              <span class="si-label">预估收入</span>
              <strong class="si-val warning">¥{{ stats.totalIncome || '0' }}</strong>
            </div>
          </div>
          <p class="si-note">* 提成制按完成订单数计算，多劳多得。薪资制度由店主统一配置。</p>
        </template>
      </div>
    </div>

    <!-- 待处理/最新订单 -->
    <div class="card">
      <div class="page-header">
        <h3 class="section-title"><span>📋</span> 我的订单</h3>
        <el-button text type="primary" @click="$router.push('/orders')">查看全部 →</el-button>
      </div>
      <el-table :data="recentOrders" stripe v-loading="loading" class="order-table">
        <el-table-column prop="id" label="#" width="45" type="index" />
        <el-table-column label="客户信息" min-width="140">
          <template #default="{ row }">
            <div class="cust-cell">
              <strong>{{ row.userName || '-' }}</strong>
              <span class="sub-info">🐾 {{ row.petName || '-' }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="serviceName" label="服务项目" min-width="110">
          <template #default="{ row }">{{ row.serviceName || '服务#' + row.serviceId }}</template>
        </el-table-column>
        <el-table-column prop="totalAmount" label="金额" width="95">
          <template #default="{ row }"><span class="amount">¥{{ row.totalAmount }}</span></template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" effect="light" round size="small">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160">
          <template #default="{ row }">
            <template v-if="row.status === 1 && isRealNameBound">
              <el-button type="success" size="small" round @click="handleAccept(row)">确认接单</el-button>
            </template>
            <template v-if="row.status === 1 && !isRealNameBound">
              <el-button type="warning" size="small" round @click="$router.push('/user/profile')">去实名认证</el-button>
            </template>
            <el-button type="warning" size="small" round @click="handleComplete(row)" v-if="row.status === 2">完成服务</el-button>
            <span v-if="row.status !==1 && row.status !==2" style="color:var(--color-text-placeholder);font-size:13px">-</span>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!loading && recentOrders.length === 0" description="暂无订单" :image-size="100" />
    </div>

    <!-- 店员快捷入口 -->
    <div class="staff-shortcuts">
      <div class="shortcut-card glass-card" @click="$router.push('/provider/income')">
        <span class="sc-icon">💰</span><strong>我的收入</strong>
      </div>
      <div class="shortcut-card glass-card" @click="$router.push('/staff/schedule')">
        <span class="sc-icon">📅</span><strong>我的排班</strong>
      </div>
      <div class="shortcut-card glass-card" @click="$router.push('/staff/reviews')">
        <span class="sc-icon">⭐</span><strong>评价查看</strong>
      </div>
      <div class="shortcut-card glass-card" @click="$router.push('/user/profile')">
        <span class="sc-icon">👤</span><strong>个人中心</strong>
      </div>
      <div class="shortcut-card glass-card" @click="$router.push('/community')">
        <span class="sc-icon">🎫</span><strong>萌宠社区</strong>
      </div>
      <div class="shortcut-card glass-card" @click="$router.push('/points')">
        <span class="sc-icon">🎉</span><strong>积分商城</strong>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { acceptOrder, completeOrder, getOrders } from '@/api/order'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const userStore = useUserStore()
const loading = ref(false)
const isRealNameBound = computed(() => !!(userStore.userInfo.realName && userStore.userInfo.idCard))
const stats = ref({})
const recentOrders = ref([])
const todayIncome = ref({})

// 根据店主设定的薪资模式计算标签和样式
const salaryLabel = computed(() => {
  const mode = todayIncome.value.salaryMode
  if (mode === 1) return '时薪制'
  if (mode === 2) return '固定月薪'
  return '提成制'
})
const salaryTagType = computed(() => {
  const mode = todayIncome.value.salaryMode
  if (mode === 1) return 'warning'
  if (mode === 2) return ''
  return 'success'
})

function statusType(s) { const m={0:'warning',1:'',2:'primary',3:'success',4:'info',5:'danger'};return m[s]||'' }
function statusText(s) { const m={0:'待支付',1:'待接单',2:'服务中',3:'已完成',4:'已取消',5:'退款中'};return m[s]||'未知' }

async function fetchMyOrders() {
  const res = await getOrders({ page: 1, size: 10 })
  return res.data?.records || res.data || []
}

onMounted(async () => {
  loading.value = true
  try {
    const [todayRes, ordersData] = await Promise.all([
      request.get('/income/today'),
      fetchMyOrders()
    ])
    todayIncome.value = todayRes.data || {}
    recentOrders.value = Array.isArray(ordersData) ? ordersData.slice(0, 10) : (ordersData.records || [])
    try { const s = await request.get('/provider/statistics'); stats.value = s.data; } catch(e) {}
  } finally { loading.value = false }
})

async function handleAccept(o) {
  try { await acceptOrder(o.id); ElMessage.success('已接单'); loadData(); } catch(e) {}
}
async function handleComplete(o) {
  try { await completeOrder(o.id); ElMessage.success('已完成'); loadData(); } catch(e) {}
}
async function loadData() {
  loading.value = true
  try {
    const [t, o] = await Promise.all([request.get('/income/today'), fetchMyOrders()])
    todayIncome.value = t.data || {}
    recentOrders.value = Array.isArray(o) ? o.slice(0, 10) : (o.records || [])
    try { const s = await request.get('/provider/statistics'); stats.value = s.data; } catch(e) {}
  } finally { loading.value = false }
}
</script>

<style scoped>
.welcome-banner {
  display: flex; align-items: center; justify-content: space-between;
  padding: 24px 28px; border-radius: var(--radius-lg); margin-bottom: 20px;
}
.banner-left { display: flex; align-items: center; gap: 16px; }
.banner-emoji { font-size: 40px; }
.banner-left h2 { font-size: 20px; margin-bottom: 4px; }
.banner-left p { opacity: 0.85; font-size: 14px; color: var(--color-text-secondary); margin: 0; }
.banner-left p strong { color: var(--color-primary); }

/* 收入横幅 */
.today-income-banner {
  display: flex; align-items: center; justify-content: space-between;
  padding: 20px 28px; border-radius: var(--radius-lg); margin-bottom: 16px;
  background: linear-gradient(135deg, rgba(255,140,66,0.08), rgba(255,209,102,0.06));
  border: 1.5px solid rgba(255,140,66,0.15);
}
.income-left { display: flex; align-items: center; gap: 16px; }
.income-icon { font-size: 36px; }
.income-left h3 { font-size: 15px; font-weight: 600; margin-bottom: 4px; }
.income-big { font-family: 'Poppins'; font-size: 28px; font-weight: 800; color: var(--color-primary); margin-right: 12px; }
.income-sub { font-size: 13px; color: var(--color-text-secondary); }
.income-sub strong { color: var(--color-primary); }

/* 统计圆环 */
.stats-row { margin-bottom: 20px; }
.stat-card { display: flex; align-items: center; gap: 16px; padding: 20px; border-radius: var(--radius-lg); }
.stat-ring { width: 80px; height: 80px; position: relative; flex-shrink: 0; }
.stat-ring svg { width: 100%; height: 100%; }
.ring-value { position: absolute; inset: 0; display: flex; align-items: center; justify-content: center; font-size: 12px; font-weight: 800; color: var(--color-text); }
.stat-info h3 { font-size: 22px; font-weight: 800; color: var(--color-text); margin-bottom: 2px; }
.stat-info p { color: var(--color-text-placeholder); font-size: 12px; margin: 0; }
.stat-card.blue .stat-info h3 { color: #FF8C42; }
.stat-card.orange .stat-info h3 { color: #E6A23C; }
.stat-card.green .stat-info h3 { color: #98DDCA; }

.section-title { font-size: 17px; font-weight: 700; display: flex; align-items: center; gap: 8px; }
.amount { color: var(--color-primary); font-weight: 600; }

/* 薪资说明卡片 */
.salary-info-card {
  margin-bottom: 20px;
}
.si-header {
  display: flex; align-items: center; gap: 10px;
  margin-bottom: 16px;
}
.si-icon { font-size: 22px; }
.si-header h3 { font-size: 17px; font-weight: 700; margin: 0; flex: 1; }
.si-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 12px; margin-bottom: 12px; }
.si-item {
  background: var(--color-surface);
  border-radius: var(--radius-md);
  padding: 14px;
  text-align: center;
  border: 1.5px solid var(--color-divider);
}
.si-label { display: block; font-size: 12px; color: var(--color-text-placeholder); margin-bottom: 6px; }
.si-val { font-size: 18px; display: block; color: var(--color-text); }
.si-val.primary { color: var(--color-primary); }
.si-val.warning { color: #E6A23C; }
.si-note { font-size: 12px; color: var(--color-text-placeholder); margin: 0; line-height: 1.6; }

/* 快捷入口 */
.staff-shortcuts {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
  margin-bottom: 20px;
}
.shortcut-card {
  display: flex; flex-direction: column; align-items: center; gap: 6px;
  padding: 18px 12px; cursor: pointer; transition: var(--transition-normal);
}
.shortcut-card:hover { transform: translateY(-3px); box-shadow: var(--shadow-md); }
.sc-icon { font-size: 28px; }
.shortcut-card strong { font-size: 13px; color: var(--color-text-secondary); }

.cust-cell { display: flex; flex-direction: column; gap: 2px; }
.cust-cell .sub-info { font-size: 12px; color: var(--color-text-placeholder); }
.order-table { margin-top: 4px; }

@media (max-width: 768px) {
  .welcome-banner { flex-direction: column; gap: 12px; text-align: center; }
  .si-grid { grid-template-columns: 1fr; }
  .staff-shortcuts { grid-template-columns: repeat(2, 1fr); }
}
</style>
