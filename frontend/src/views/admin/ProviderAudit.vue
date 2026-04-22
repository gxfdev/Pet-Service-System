<template>
  <div class="provider-audit">
    <div class="page-card">
      <div class="card-header-row">
        <h3>🏪 服务商审核</h3>
        <el-select v-model="statusFilter" placeholder="审核状态" clearable style="width:140px" @change="fetchData">
          <el-option label="待审核" :value="0" />
          <el-option label="已通过" :value="1" />
          <el-option label="已驳回" :value="2" />
        </el-select>
      </div>

      <div class="audit-list" v-loading="loading">
        <div class="audit-card" v-for="item in list" :key="item.id">
          <div class="audit-left">
            <el-avatar :size="56" :style="{background:'#FF8C42',fontSize:'22px'}">{{ (item.shopName||'S').charAt(0) }}</el-avatar>
            <div class="audit-info">
              <strong>{{ item.shopName || `服务商#${item.id}` }}</strong>
              <p>联系人：{{ item.realName || '-' }} | 手机：{{ item.phone || '-' }}</p>
              <p>申请时间：{{ formatTime(item.createTime) }}</p>
            </div>
          </div>
          <div class="audit-right">
            <el-tag :type="auditStatusType(item.auditStatus)" effect="dark" round size="small">{{ auditStatusText(item.auditStatus) }}</el-tag>
            <div class="audit-actions" v-if="item.auditStatus === 0">
              <el-button type="primary" size="small" round @click="handleAudit(item, 1)">通过</el-button>
              <el-button size="small" round @click="handleAudit(item, 2)">驳回</el-button>
            </div>
          </div>
        </div>
        <el-empty v-if="!loading && list.length === 0" description="暂无审核记录" :image-size="80" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const list = ref([])
const statusFilter = ref(null)

function auditStatusType(s) { return s===1?'success':s===2?'danger':'warning' }
function auditStatusText(s) { return s===1?'已通过':s===2?'已驳回':'待审核' }
function formatTime(t) { return t ? new Date(t).toLocaleString() : '-' }

async function fetchData() {
  loading.value = true
  try {
    const params = { page: 1, size: 20 }
    if (statusFilter.value !== null) params.auditStatus = statusFilter.value
    const res = await request.get('/admin/audit/providers', { params })
    list.value = res.data?.records || res.data || []
  } catch(e) {} finally { loading.value = false }
}

async function handleAudit(item, status) {
  try {
    await request.put(`/admin/audit/${item.id}`, { action: status === 1 ? 'approve' : 'reject' })
    ElMessage.success(status === 1 ? '已通过审核' : '已驳回申请')
    fetchData()
  } catch(e) {}
}

onMounted(fetchData)
</script>

<style scoped>
.page-card { background: #1A2B3C; border-radius: 20px; padding: 24px; border: 1px solid rgba(255,140,66,0.06); }
.card-header-row { display: flex; align-items: center; justify-content: space-between; margin-bottom: 20px; }
.card-header-row h3 { color: #E0E6ED; font-size: 17px; margin: 0; }
.audit-list {}
.audit-card { display: flex; align-items: center; justify-content: space-between; padding: 16px; border-radius: 14px; background: #162231; margin-bottom: 10px; border: 1px solid rgba(255,140,66,0.04); transition: all 0.2s; }
.audit-card:hover { border-color: rgba(255,140,66,0.12); }
.audit-left { display: flex; align-items: center; gap: 14px; }
.audit-info strong { display: block; color: #E0E6ED; font-size: 14px; margin-bottom: 4px; }
.audit-info p { font-size: 12px; color: #6B7A8D; margin: 2px 0; }
.audit-right { display: flex; flex-direction: column; align-items: flex-end; gap: 8px; }
.audit-actions { display: flex; gap: 8px; }
</style>
