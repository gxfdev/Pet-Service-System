<template>
  <div class="logs-page">
    <div class="page-card">
      <div class="card-header-row">
        <h3>📋 系统日志</h3>
        <div class="header-actions">
          <el-select v-model="logType" placeholder="日志类型" clearable style="width:130px" size="small">
            <el-option label="操作日志" value="operation" /><el-option label="登录日志" value="login" /><el-option label="接口日志" value="api" />
          </el-select>
          <el-button size="small" round>导出</el-button>
        </div>
      </div>

      <el-table :data="logs" size="small" v-loading="loading">
        <el-table-column prop="time" label="时间" width="160" />
        <el-table-column prop="type" label="类型" width="90">
          <template #default="{row}"><el-tag :type="logTypeColor(row.type)" size="small" effect="dark" round>{{ logTypeLabel(row.type) }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="operator" label="操作人" width="100" />
        <el-table-column prop="action" label="操作" min-width="200" />
        <el-table-column prop="ip" label="IP" width="130" />
        <el-table-column prop="status" label="结果" width="70">
          <template #default="{row}"><span :style="{color: row.status==='success'?'#98DDCA':'#F56C6C'}">{{ row.status==='success'?'成功':'失败' }}</span></template>
        </el-table-column>
      </el-table>

      <div style="text-align:center;margin-top:16px">
        <el-pagination layout="prev,pager,next" :total="50" :page-size="10" background small />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const loading = ref(false)
const logType = ref(null)

function logTypeColor(t) { return t==='operation'?'':t==='login'?'success':'warning' }
function logTypeLabel(t) { return t==='operation'?'操作':t==='login'?'登录':'接口' }

const logs = ref([
  { time:'2026-04-10 15:30:22', type:'operation', operator:'admin', action:'修改用户 user1 的角色为服务商', ip:'192.168.1.100', status:'success' },
  { time:'2026-04-10 15:28:10', type:'login', operator:'admin', action:'管理员登录系统', ip:'192.168.1.100', status:'success' },
  { time:'2026-04-10 14:55:33', type:'api', operator:'system', action:'/api/admin/users 请求耗时 320ms', ip:'-', status:'success' },
  { time:'2026-04-10 14:22:18', type:'operation', operator:'admin', action:'封禁用户 spam_user_001', ip:'192.168.1.100', status:'success' },
  { time:'2026-04-10 13:10:05', type:'login', operator:'unknown', action:'异常IP尝试登录管理员账号', ip:'45.33.22.11', status:'fail' },
  { time:'2026-04-10 12:00:00', type:'api', operator:'system', action:'/api/auth/login 请求耗时 52ms', ip:'-', status:'success' },
])
</script>

<style scoped>
.page-card { background: #1A2B3C; border-radius: 20px; padding: 24px; border: 1px solid rgba(255,140,66,0.06); }
.card-header-row { display: flex; align-items: center; justify-content: space-between; margin-bottom: 20px; }
.card-header-row h3 { color: #E0E6ED; font-size: 17px; margin: 0; }
.header-actions { display: flex; gap: 8px; }
</style>
