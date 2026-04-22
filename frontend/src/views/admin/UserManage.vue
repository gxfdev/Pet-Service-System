<template>
  <div class="page-container">
    <div class="card">
      <div class="page-header">
        <h2 class="page-title"><span>👥</span> 用户管理</h2>
        <el-input v-model="keyword" placeholder="搜索用户名/手机号..." clearable class="search-input"
                  @keyup.enter="fetchData">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
      </div>

      <el-table :data="list" stripe class="user-table" v-loading="loading">
        <el-table-column prop="id" label="ID" width="65" align="center" />
        <el-table-column prop="username" label="用户名" width="120">
          <template #default="{ row }">
            <div class="user-cell">
              <el-avatar :size="28" :style="{background:userAvatarColor(row)}" class="u-avatar">
                {{ (row.username||'').charAt(0).toUpperCase() }}
              </el-avatar>
              <strong>{{ row.username }}</strong>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="role" label="角色" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="roleTagType(row.role)" effect="dark" round size="small">{{ roleLabel(row.role) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="shopName" label="店铺名称" min-width="130" show-overflow-tooltip>
          <template #default="{ row }"><span :class="{empty:!row.shopName}">{{ row.shopName || '-' }}</span></template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-switch
              :model-value="row.status===1"
              active-text=""
              inactive-text=""
              @change="(val) => toggleStatus(row, val)"
              inline-prompt
              style="--el-switch-on-color:#98DDCA;--el-switch-off-color:#F56C6C"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" align="center">
          <template #default="{ row }">
            <el-dropdown trigger="click" @command="(cmd)=>changeRole(row,cmd)">
              <el-button size="small" round>修改角色</el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item :command="0">设为普通用户</el-dropdown-item>
                  <el-dropdown-item :command="1">设为服务商</el-dropdown-item>
                  <el-dropdown-item :command="2">设为管理员</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </el-table-column>
      </el-table>

      <div style="text-align:center;margin-top:20px" v-if="total > pageSize">
        <el-pagination layout="prev,pager,next" :total="total" :page-size="pageSize"
                       v-model:current-page="currentPage" @current-change="fetchData" background />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = 10
const keyword = ref('')

function roleTagType(r) { return r===2?'danger':r===1?'warning':'' }
function roleLabel(r) { return r===2?'管理员':r===1?'服务商':'普通用户' }
function userAvatarColor(row) {
  const colors=['#FF8C42','#E6A23C','#98DDCA','#F9A8D4','#6C9EBF']
  return colors[(row.id||0) % colors.length]
}

async function fetchData() {
  loading.value=true
  try {
    const params={ page:currentPage.value, size:pageSize }
    if(keyword.value) params.keyword=keyword.value
    const res=await request.get('/admin/users',{ params })
    list.value=res.data.records||[]
    total.value=res.data.total||0
  } finally{ loading.value=false }
}
onMounted(fetchData)

async function toggleStatus(user, enabled) {
  const action=enabled?'启用':'禁用'
  ElMessageBox.confirm(`确认${action}用户「${user.username}」？`, '操作确认', { type:'warning' }).then(async()=>{
    try{
      await request.put(`/admin/users/${user.id}/status`)
      ElMessage.success('操作成功'); fetchData()
    }catch(e){}
  }).catch(()=>{})
}

function changeRole(user, role) {
  const labels=[null,'服务商','管理员']
  ElMessageBox.confirm(`将「${user.username}」的角色改为 ${labels[role]}?`,'角色变更',{type:'info'}).then(async()=>{
    try{ await request.put(`/admin/users/${user.id}/role`,{role});ElMessage.success('已修改');fetchData(); }catch(e){}
  }).catch(()=>{})
}
</script>

<style scoped>
.search-input { width: 260px; }
.search-input :deep(.el-input__wrapper) {
  border-radius: var(--radius-full) !important;
  background: var(--color-cream) !important;
  box-shadow: none !important;
  border: 1.5px solid transparent;
}
.search-input :deep(.el-input__wrapper:focus-within) {
  background: #fff !important;
  border-color: var(--color-primary);
}

.user-table { border-radius: var(--radius-md); overflow: hidden; }
.user-cell { display: flex; align-items: center; gap: 10px; font-size: 14px; }
.u-avatar { color: #fff !important; font-weight: 700; }
.user-cell strong { color: var(--color-text); }
.empty { color: var(--color-text-placeholder); }
</style>
