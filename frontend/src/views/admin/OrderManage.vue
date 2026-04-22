<template>
  <div class="page-container">
    <div class="card">
      <div class="page-header">
        <h2 class="page-title"><span>📋</span> 订单管理</h2>
        <el-select v-model="statusFilter" placeholder="状态筛选" clearable style="width:150px" @change="fetchData">
          <el-option label="待支付" :value="0" />
          <el-option label="待服务" :value="1" />
          <el-option label="服务中" :value="2" />
          <el-option label="已完成" :value="3" />
          <el-option label="已取消" :value="4" />
        </el-select>
      </div>

      <el-table :data="list" stripe v-loading="loading">
        <el-table-column prop="orderNo" label="订单号" width="210" />
        <el-table-column prop="userName" label="用户" width="100" />
        <el-table-column prop="shopName" label="服务商" width="120" />
        <el-table-column prop="totalAmount" label="金额" width="90">
          <template #default="{ row }"><span class="amount">¥{{ row.totalAmount }}</span></template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag size="small" :type="statusType(row.status)" round>{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170">
          <template #default="{ row }"> {{ formatTime(row.createTime) }} </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button type="danger" size="small" @click="intervene(row, 'cancel')" v-if="[0,1,2].includes(row.status)" round>强制取消</el-button>
            <el-button type="warning" size="small" @click="intervene(row, 'refund')" v-if="row.status === 2" round>退款</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div style="text-align:center;margin-top:16px" v-if="total > pageSize">
        <el-pagination layout="prev,pager,next" :total="total" :page-size="pageSize"
                       v-model:current-page="currentPage" @current-change="fetchData" background />
      </div>
    </div>

    <!-- 仲裁对话框 -->
    <el-dialog v-model="interveneVisible" title="订单仲裁" width="400px">
      <div class="intervene-info">
        <p><strong>订单号：</strong>{{ currentOrder?.orderNo }}</p>
        <p><strong>当前金额：</strong><span class="amount">¥{{ currentOrder?.totalAmount }}</span></p>
      </div>
      <el-input v-model="interveneReason" placeholder="请输入操作原因（选填）" style="margin-top:12px" />
      <template #footer>
        <el-button @click="interveneVisible=false" round>取消</el-button>
        <el-button type="danger" @click="confirmIntervene" round>确认执行</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = 10
const statusFilter = ref(null)
const interveneVisible = ref(false)
const currentOrder = ref(null)
const interveneReason = ref('')
const interveneAction = ref('')

function statusType(s) { const m={0:'warning',1:'',2:'primary',3:'success',4:'info',5:'danger'}; return m[s]||'' }
function statusText(s) { const m={0:'待支付',1:'待服务',2:'服务中',3:'已完成',4:'已取消',5:'退款中'}; return m[s]||'未知' }
function formatTime(t) { return t ? new Date(t).toLocaleString() : '-' }

async function fetchData() {
  loading.value = true
  try {
    const params = { page: currentPage.value, size: pageSize }
    if (statusFilter.value !== null) params.status = statusFilter.value
    const res = await request.get('/admin/orders', { params })
    list.value = res.data.records || []
    total.value = res.data.total || 0
  } finally { loading.value = false }
}
onMounted(fetchData)

function intervene(order, action) {
  currentOrder.value = order
  interveneAction.value = action
  interveneReason.value = ''
  interveneVisible.value = true
}

async function confirmIntervene() {
  try {
    await request.put(`/admin/orders/${currentOrder.value.id}/intervene`, { action: interveneAction.value, reason: interveneReason.value })
    ElMessage.success('操作成功')
    interveneVisible.value = false
    fetchData()
  } catch (e) {}
}
</script>

<style scoped>
.amount { color: var(--color-primary); font-weight: 600; }
.intervene-info { background: var(--color-cream); border-radius: var(--radius-md); padding: 16px; }
.intervene-info p { margin: 6px 0; font-size: 14px; }
</style>
