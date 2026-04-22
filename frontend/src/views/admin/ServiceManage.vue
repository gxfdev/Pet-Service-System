<template>
  <div class="admin-service-mgmt">
    <div class="page-card">
      <div class="card-header-row">
        <h3>🛎️ 服务项目管理</h3>
        <el-button type="primary" size="small" round @click="showAddDialog">+ 添加服务</el-button>
      </div>

      <el-table :data="list" v-loading="loading" size="small">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="name" label="服务名称" min-width="140" />
        <el-table-column prop="category" label="分类" width="90">
          <template #default="{row}"><el-tag size="small" effect="dark" round>{{ catMap[row.category] || '其他' }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="price" label="价格" width="90">
          <template #default="{row}">¥{{ row.price }}</template>
        </el-table-column>
        <el-table-column prop="duration" label="时长" width="80">
          <template #default="{row}">{{ row.duration }}分钟</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{row}"><el-tag :type="row.status===1?'success':'info'" size="small" round>{{ row.status===1?'上架':'下架' }}</el-tag></template>
        </el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="{row}">
            <el-button size="small" round @click="handleToggle(row)">{{ row.status===1?'下架':'上架' }}</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="visible" title="添加服务项目" width="500px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="服务名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="分类">
          <el-select v-model="form.category" style="width:100%">
            <el-option label="美容" :value="1" /><el-option label="寄养" :value="2" />
            <el-option label="医疗" :value="3" /><el-option label="遛狗" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="价格"><el-input-number v-model="form.price" :min="1" :precision="2" /></el-form-item>
        <el-form-item label="时长(分钟)"><el-input-number v-model="form.duration" :min="10" :step="15" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="form.description" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="visible=false" round>取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting" round>添加</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getServices } from '@/api/service'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const list = ref([])
const visible = ref(false)
const submitting = ref(false)
const catMap = { 1:'美容', 2:'寄养', 3:'医疗', 4:'遛狗' }
const form = ref({ name:'', category:1, price:null, duration:60, description:'' })

async function fetchData() {
  loading.value = true
  try {
    const res = await getServices({ page:1, size:100 })
    list.value = res.data?.records || res.data || []
  } finally { loading.value = false }
}

function showAddDialog() { form.value = { name:'', category:1, price:null, duration:60, description:'' }; visible.value = true }

async function handleSubmit() {
  submitting.value = true
  try {
    await request.post('/provider/services', form.value)
    ElMessage.success('添加成功')
    visible.value = false
    fetchData()
  } catch(e) {
    ElMessage.error(e?.message || '添加失败')
  } finally { submitting.value = false }
}

async function handleToggle(row) {
  try {
    await request.put(`/provider/services/${row.id}/toggle`)
    ElMessage.success('操作成功')
    fetchData()
  } catch(e) {
    ElMessage.error(e?.message || '操作失败')
  }
}

onMounted(fetchData)
</script>

<style scoped>
.page-card { background: #1A2B3C; border-radius: 20px; padding: 24px; border: 1px solid rgba(255,140,66,0.06); }
.card-header-row { display: flex; align-items: center; justify-content: space-between; margin-bottom: 20px; }
.card-header-row h3 { color: #E0E6ED; font-size: 17px; margin: 0; }
</style>
