<template>
  <div class="dashboard">
    <!-- 顶部统计卡片 -->
    <div class="stats-grid">
      <div class="stat-card" v-for="item in statCards" :key="item.label">
        <div class="stat-icon-wrap" :style="{background: item.bg}">
          <span class="stat-icon">{{ item.icon }}</span>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ item.value }}</span>
          <span class="stat-label">{{ item.label }}</span>
        </div>
        <div class="stat-trend" :class="item.trend > 0 ? 'up' : 'down'">
          {{ item.trend > 0 ? '↑' : '↓' }} {{ Math.abs(item.trend) }}%
        </div>
      </div>
    </div>

    <el-row :gutter="20">
      <!-- 订单趋势 -->
      <el-col :xs="24" :lg="16">
        <div class="dash-card">
          <div class="card-header">
            <h3>📈 订单趋势</h3>
            <el-radio-group v-model="trendRange" size="small">
              <el-radio-button value="week">近7天</el-radio-button>
              <el-radio-button value="month">近30天</el-radio-button>
            </el-radio-group>
          </div>
          <div class="chart-placeholder">
            <div class="bar-chart">
              <div class="bar" v-for="(v, i) in trendData" :key="i" :style="{height: v + '%', background: barColors[i % barColors.length]}">
                <span class="bar-val">{{ v }}</span>
              </div>
            </div>
            <div class="bar-labels">
              <span v-for="(d, i) in trendLabels" :key="i">{{ d }}</span>
            </div>
          </div>
        </div>
      </el-col>

      <!-- 实时动态 -->
      <el-col :xs="24" :lg="8">
        <div class="dash-card">
          <div class="card-header"><h3>🔔 实时动态</h3></div>
          <div class="activity-list">
            <div class="activity-item" v-for="(a, i) in activities" :key="i">
              <span class="act-dot" :style="{background: a.color}"></span>
              <div class="act-content">
                <p>{{ a.text }}</p>
                <span class="act-time">{{ a.time }}</span>
              </div>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top:20px">
      <!-- 用户管理快捷 -->
      <el-col :xs="24" :lg="12">
        <div class="dash-card">
          <div class="card-header">
            <h3>👥 最近注册用户</h3>
            <router-link to="/admin/users" class="link-btn">查看全部 →</router-link>
          </div>
          <el-table :data="recentUsers" size="small" v-loading="userLoading">
            <el-table-column prop="username" label="用户名" width="100" />
            <el-table-column prop="phone" label="手机号" width="130" />
            <el-table-column prop="role" label="角色" width="90">
              <template #default="{row}"><el-tag :type="roleTag(row.role)" size="small" effect="dark" round>{{ roleLabel(row.role) }}</el-tag></template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="70">
              <template #default="{row}"><el-tag :type="row.status===1?'success':'danger'" size="small" effect="plain" round>{{ row.status===1?'正常':'禁用' }}</el-tag></template>
            </el-table-column>
          </el-table>
        </div>
      </el-col>

      <!-- 异常预警 -->
      <el-col :xs="24" :lg="12">
        <div class="dash-card">
          <div class="card-header"><h3>⚠️ 异常预警</h3></div>
          <div class="alert-list">
            <div class="alert-item warning" v-for="(alert, i) in alerts" :key="i">
              <span class="alert-icon">{{ alert.icon }}</span>
              <div class="alert-body">
                <strong>{{ alert.title }}</strong>
                <p>{{ alert.desc }}</p>
              </div>
              <span class="alert-time">{{ alert.time }}</span>
            </div>
            <el-empty v-if="alerts.length===0" description="暂无异常" :image-size="60" />
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '@/utils/request'

const trendRange = ref('week')
const userLoading = ref(false)
const recentUsers = ref([])

const statCards = ref([
  { icon: '📦', label: '今日订单', value: '128', trend: 12, bg: 'linear-gradient(135deg,#FF8C42,#FFAB76)' },
  { icon: '💰', label: '今日GMV', value: '¥12,680', trend: 8, bg: 'linear-gradient(135deg,#98DDCA,#C2EBE1)' },
  { icon: '👥', label: '注册用户', value: '2,847', trend: 5, bg: 'linear-gradient(135deg,#F9A8D4,#FFD1DC)' },
  { icon: '🏪', label: '服务商', value: '156', trend: -2, bg: 'linear-gradient(135deg,#6C9EBF,#A3C4DB)' },
])

const trendData = ref([65, 78, 52, 90, 72, 85, 68])
const trendLabels = ref(['周一','周二','周三','周四','周五','周六','周日'])
const barColors = ['#FF8C42','#FFAB76','#FFD1DC','#98DDCA','#6C9EBF','#F9A8D4','#FFAB76']

const activities = ref([
  { text: '新用户 user123 完成注册', time: '2分钟前', color: '#98DDCA' },
  { text: '服务商「萌宠乐园」提交审核', time: '15分钟前', color: '#FF8C42' },
  { text: '订单 #20260410 退款申请', time: '30分钟前', color: '#F9A8D4' },
  { text: '系统配置已更新', time: '1小时前', color: '#6C9EBF' },
  { text: '风控拦截异常IP 192.168.x.x', time: '2小时前', color: '#F56C6C' },
])

const alerts = ref([
  { icon: '🔴', title: '退款率异常', desc: '服务商「爱宠之家」近7天退款率达35%', time: '10分钟前' },
  { icon: '🟡', title: '差评预警', desc: '服务「宠物寄养」近3天收到5条差评', time: '1小时前' },
  { icon: '🟠', title: '订单量下降', desc: '今日订单量较昨日同期下降18%', time: '3小时前' },
])

function roleTag(r) { return r===2?'danger':r===1?'warning':'' }
function roleLabel(r) { return r===2?'管理员':r===1?'服务商':'用户' }

onMounted(async () => {
  userLoading.value = true
  try {
    const res = await request.get('/admin/users', { params: { page: 1, size: 5 } })
    recentUsers.value = res.data?.records || []
  } catch (e) {} finally { userLoading.value = false }
})
</script>

<style scoped>
.dashboard {}

.stats-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; margin-bottom: 20px; }
.stat-card {
  background: #1A2B3C;
  border-radius: 20px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 14px;
  border: 1px solid rgba(255,140,66,0.06);
  transition: all 0.3s;
}
.stat-card:hover { border-color: rgba(255,140,66,0.15); transform: translateY(-2px); }
.stat-icon-wrap { width: 48px; height: 48px; border-radius: 14px; display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
.stat-icon { font-size: 24px; }
.stat-info { flex: 1; }
.stat-value { display: block; font-family: 'Poppins', sans-serif; font-size: 24px; font-weight: 800; color: #E0E6ED; }
.stat-label { display: block; font-size: 12px; color: #6B7A8D; margin-top: 2px; }
.stat-trend { font-size: 12px; font-weight: 600; padding: 2px 8px; border-radius: 8px; }
.stat-trend.up { color: #98DDCA; background: rgba(152,221,202,0.1); }
.stat-trend.down { color: #F56C6C; background: rgba(245,108,108,0.1); }

.dash-card {
  background: #1A2B3C;
  border-radius: 20px;
  padding: 20px;
  border: 1px solid rgba(255,140,66,0.06);
  margin-bottom: 0;
}
.card-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 16px; }
.card-header h3 { font-size: 15px; font-weight: 700; color: #E0E6ED; margin: 0; }
.link-btn { color: #FF8C42; text-decoration: none; font-size: 13px; font-weight: 500; }

/* 柱状图 */
.chart-placeholder { padding: 10px 0; }
.bar-chart { display: flex; align-items: flex-end; gap: 12px; height: 180px; padding: 0 10px; }
.bar { flex: 1; border-radius: 8px 8px 0 0; position: relative; min-height: 20px; transition: height 0.6s ease; }
.bar-val { position: absolute; top: -20px; left: 50%; transform: translateX(-50%); font-size: 11px; color: #8899AA; font-weight: 600; }
.bar-labels { display: flex; gap: 12px; padding: 8px 10px 0; }
.bar-labels span { flex: 1; text-align: center; font-size: 11px; color: #6B7A8D; }

/* 实时动态 */
.activity-list {}
.activity-item { display: flex; gap: 12px; padding: 10px 0; border-bottom: 1px solid rgba(255,140,66,0.04); }
.activity-item:last-child { border-bottom: none; }
.act-dot { width: 8px; height: 8px; border-radius: 50%; margin-top: 6px; flex-shrink: 0; }
.act-content { flex: 1; }
.act-content p { font-size: 13px; color: #A0AEC0; margin: 0; }
.act-time { font-size: 11px; color: #6B7A8D; }

/* 预警 */
.alert-list {}
.alert-item { display: flex; align-items: flex-start; gap: 12px; padding: 12px; border-radius: 12px; margin-bottom: 8px; background: rgba(245,108,108,0.04); border: 1px solid rgba(245,108,108,0.08); }
.alert-icon { font-size: 18px; flex-shrink: 0; margin-top: 2px; }
.alert-body { flex: 1; }
.alert-body strong { font-size: 13px; color: #E0E6ED; display: block; margin-bottom: 2px; }
.alert-body p { font-size: 12px; color: #6B7A8D; margin: 0; }
.alert-time { font-size: 11px; color: #6B7A8D; flex-shrink: 0; }

@media (max-width: 1024px) { .stats-grid { grid-template-columns: repeat(2, 1fr); } }
@media (max-width: 640px) { .stats-grid { grid-template-columns: 1fr; } }
</style>
