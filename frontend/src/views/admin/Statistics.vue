<template>
  <div class="page-container admin-dashboard">
    <!-- 驾驶舱头部 -->
    <div class="cockpit-header">
      <div class="cockpit-left">
        <span class="cockpit-icon">📊</span>
        <div>
          <h2>管理控制台</h2>
          <p>系统全局管理与数据监控中心</p>
        </div>
      </div>
      <div class="cockpit-stats">
        <div class="cs-item">
          <strong>{{ globalStats.userCount || 0 }}</strong>
          <span>注册用户</span>
        </div>
        <div class="cs-item">
          <strong>{{ globalStats.orderCount || 0 }}</strong>
          <span>总订单数</span>
        </div>
        <div class="cs-item">
          <strong>{{ globalStats.providerCount || 0 }}</strong>
          <span>服务商</span>
        </div>
      </div>
    </div>

    <!-- 异常预警 -->
    <div class="alert-bar glass-card" v-if="alerts.length > 0">
      <span class="alert-icon">⚠️</span>
      <div class="alert-content">
        <span v-for="a in alerts" :key="a" class="alert-item">{{ a }}</span>
      </div>
    </div>

    <el-row :gutter="16">
      <!-- 用户管理快览 -->
      <el-col :xs="24" :sm="12">
        <div class="card section-block">
          <div class="section-head">
            <h3><span>👥</span> 用户管理</h3>
            <el-button text type="primary" @click="$router.push('/admin/users')">完整面板 →</el-button>
          </div>
          <el-table :data="users" stripe size="small" v-loading="userLoading" max-height="320">
            <el-table-column prop="username" label="用户名" width="90" />
            <el-table-column prop="phone" label="手机号" width="120" />
            <el-table-column prop="role" label="角色" width="80">
              <template #default="{ row }">
                <el-tag :type="roleTag(row.role)" size="small" round>{{ roleLabel(row.role) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="70">
              <template #default="{ row }">
                <el-tag :type="row.status===1?'success':'danger'" size="small" effect="plain" round>{{ row.status===1?'正常':'禁用' }}</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-col>

      <!-- 最近订单 -->
      <el-col :xs="24" :sm="12">
        <div class="card section-block">
          <div class="section-head">
            <h3><span>📦</span> 最近订单</h3>
            <el-button text type="primary" @click="$router.push('/admin/orders')">完整面板 →</el-button>
          </div>
          <el-table :data="orders" stripe size="small" v-loading="orderLoading" max-height="320">
            <el-table-column prop="id" label="#" width="45" type="index" />
            <el-table-column prop="serviceName" label="服务" min-width="100" show-overflow-tooltip />
            <el-table-column prop="totalAmount" label="金额" width="80">
              <template #default="{ row }">¥{{ row.totalAmount }}</template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="80">
              <template #default="{ row }">
                <el-tag :type="orderStatusType(row.status)" size="small" effect="light" round>{{ orderStatusText(row.status) }}</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-col>
    </el-row>

    <!-- 管理快捷入口 -->
    <div class="admin-shortcuts">
      <div class="shortcut-card glass-card" @click="$router.push('/admin/users')">
        <span class="sc-icon">👤</span><strong>用户管理</strong>
      </div>
      <div class="shortcut-card glass-card" @click="$router.push('/admin/orders')">
        <span class="sc-icon">📋</span><strong>订单监管</strong>
      </div>
      <div class="shortcut-card glass-card" @click="$router.push('/admin/risk')">
        <span class="sc-icon">🛡️</span><strong>风控管理</strong>
      </div>
      <div class="shortcut-card glass-card" @click="$router.push('/admin/config')">
        <span class="sc-icon">⚙️</span><strong>系统配置</strong>
      </div>
      <div class="shortcut-card glass-card" @click="$router.push('/admin/marketing')">
        <span class="sc-icon">🎉</span><strong>营销管理</strong>
      </div>
      <div class="shortcut-card glass-card" @click="$router.push('/admin/logs')">
        <span class="sc-icon">📝</span><strong>系统日志</strong>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '@/utils/request'

const globalStats = ref({})
const users = ref([])
const orders = ref([])
const userLoading = ref(false)
const orderLoading = ref(false)
const alerts = ref(['今日差评数较昨日增长20%', '3个服务商证件即将过期'])

function roleTag(r) { return r===2?'danger':r===1?'warning':'' }
function roleLabel(r) { return r===2?'管理员':r===1?'服务商':'用户' }
function orderStatusType(s) { const m={0:'',1:'warning',2:'primary',3:'success',4:'info',5:'danger'}; return m[s]||'' }
function orderStatusText(s) { const m={0:'待支付',1:'待服务',2:'服务中',3:'已完成',4:'已取消',5:'退款中'}; return m[s]||'-' }

onMounted(async () => {
  try {
    const sRes = await request.get('/admin/statistics')
    globalStats.value = sRes.data
  }catch(e){}

  userLoading.value=true
  try {
    const uRes = await request.get('/admin/users',{ params:{ page:1,size:5 }})
    users.value = uRes.data?.records||[]
  }catch(e){} finally { userLoading.value=false }

  orderLoading.value=true
  try {
    const oRes = await request.get('/admin/orders',{ params:{ page:1,size:5 }})
    orders.value = oRes.data?.records||[]
  }catch(e){} finally { orderLoading.value=false }
})
</script>

<style scoped>
/* 驾驶舱头部 */
.cockpit-header {
  background: linear-gradient(135deg, #3D3226 0%, #5C4A3A 50%, var(--color-primary) 100%);
  color: #fff;
  padding: 28px 32px;
  border-radius: var(--radius-lg);
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
  flex-wrap: wrap;
  gap: 16px;
}
.cockpit-left { display: flex; align-items: center; gap: 16px; }
.cockpit-icon { font-size: 42px; }
.cockpit-left h2 { font-size: 24px; margin-bottom: 4px; color: #fff; }
.cockpit-left p { font-size: 13px; opacity: 0.75; margin: 0; }
.cockpit-stats { display: flex; gap: 32px; }
.cs-item { text-align: center; }
.cs-item strong { display: block; font-size: 28px; font-weight: 800; }
.cs-item span { font-size: 12px; opacity: 0.7; }

/* 异常预警 */
.alert-bar {
  display: flex; align-items: center; gap: 10px;
  padding: 12px 20px; border-radius: var(--radius-md);
  margin-bottom: 20px; border: 1px solid #FFE0B2;
}
.alert-icon { font-size: 20px; }
.alert-content { display: flex; gap: 16px; flex-wrap: wrap; }
.alert-item {
  font-size: 13px; color: var(--color-primary);
  font-weight: 500;
  padding: 2px 12px;
  background: #FFF3E0;
  border-radius: var(--radius-full);
}

.section-block { height: 100%; }
.section-head { display: flex; align-items: center; justify-content: space-between; margin-bottom: 16px; }
.section-head h3 { font-size: 17px; font-weight: 600; color: var(--color-text); margin: 0; display: flex; align-items: center; gap: 8px; }

/* 管理快捷入口 */
.admin-shortcuts {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
  gap: 14px;
  margin-top: 20px;
}
.shortcut-card {
  display: flex; flex-direction: column; align-items: center; gap: 8px;
  padding: 20px 12px; cursor: pointer; transition: var(--transition-normal);
}
.shortcut-card:hover { transform: translateY(-4px); box-shadow: var(--shadow-md); }
.sc-icon { font-size: 32px; }
.shortcut-card strong { font-size: 13px; color: var(--color-text-secondary); }

@media (max-width: 768px) {
  .cockpit-header { flex-direction: column; text-align: center; }
  .cockpit-stats { justify-content: center; }
}
</style>
