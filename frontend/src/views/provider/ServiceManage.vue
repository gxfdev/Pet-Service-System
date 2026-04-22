<template>
  <div class="page-container">
    <div class="card">
      <div class="page-header">
        <h2 class="page-title"><span>⚙️</span> 服务项目管理</h2>
        <el-button type="primary" @click="showAddDialog" round>+ 添加服务</el-button>
      </div>

      <div class="service-grid">
        <div class="svc-card" v-for="item in list" :key="item.id">
          <div class="svc-status" :class="item.status === 1 ? 'on' : 'off'">
            {{ item.status === 1 ? '上架中' : '已下架' }}
          </div>
          <h4>{{ item.serviceName || '服务#' + item.serviceId }}</h4>
          <div class="svc-price">¥{{ item.price }}</div>
          <div class="svc-actions">
            <el-button size="small" @click="handleToggle(item)" round>
              {{ item.status === 1 ? '下架' : '上架' }}
            </el-button>
            <el-button size="small" type="danger" @click="handleDelete(item.id)" round>删除</el-button>
          </div>
        </div>
      </div>

      <el-empty v-if="!loading && list.length === 0" description="暂无服务项目" />
    </div>

    <!-- 添加服务对话框 -->
    <el-dialog v-model="visible" title="添加服务" width="450px">
      <el-form :model="form" label-width="90px" class="svc-form">
        <el-form-item label="服务项目">
          <el-select v-model="form.serviceId" placeholder="选择要提供的服务项" style="width:100%">
            <el-option v-for="item in allServices" :key="item.id"
                       :label="`${item.name} (参考¥${item.price})`" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="我的定价">
          <el-input-number v-model="form.price" :precision="2" :min="1" :max="9999" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="visible=false" round>取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit" round>添加</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getServices } from '@/api/service'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const list = ref([])
const allServices = ref([])
const visible = ref(false)
const submitting = ref(false)
const form = ref({ serviceId: null, price: null })

onMounted(async () => {
  loading.value = true
  try {
    const [myRes, allRes] = await Promise.all([
      request.get('/provider/services'),
      getServices({ page: 1, size: 100 })
    ])
    list.value = myRes.data
    allServices.value = allRes.data.records || []
  } finally { loading.value = false }
})

function showAddDialog() { form.value = { serviceId: null, price: null }; visible.value = true }

async function handleSubmit() {
  if (!form.value.serviceId || !form.value.price) return ElMessage.warning('请填写完整')
  submitting.value = true
  try {
    await request.post('/provider/services', form.value)
    ElMessage.success('添加成功'); visible.value = false; reload()
  } catch(e) {} finally { submitting.value = false }
}

async function handleToggle(row) {
  try {
    await request.put(`/provider/services/${row.id}/toggle`)
    ElMessage.success('操作成功'); reload()
  } catch(e) {}
}

async function handleDelete(id) {
  ElMessageBox.confirm('确认删除此服务？','提示',{type:'warning'}).then(async()=>{
    ElMessage.info('删除功能可后续实现')
  }).catch(()=>{})
}

async function reload() {
  loading.value = true
  try { const res = await request.get('/provider/services'); list.value = res.data; } finally{ loading.value=false }
}
</script>

<style scoped>
.service-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 14px;
}
.svc-card {
  padding: 20px;
  border-radius: var(--radius-lg);
  background: var(--color-cream);
  border: 1.5px solid var(--color-divider);
  transition: var(--transition-normal);
  text-align: center;
}
.svc-card:hover { transform: translateY(-2px); box-shadow: var(--shadow-sm); }
.svc-status {
  display: inline-block;
  padding: 2px 12px;
  border-radius: var(--radius-full);
  font-size: 11px;
  font-weight: 600;
  margin-bottom: 10px;
}
.svc-status.on { background: #E8F5E9; color: #4CAF50; }
.svc-status.off { background: #FFF3E0; color: var(--color-primary); }
.svc-card h4 { font-size: 16px; margin-bottom: 6px; }
.svc-price { font-size: 24px; font-weight: 800; color: var(--color-primary); margin: 8px 0 12px; }
.svc-actions { display: flex; gap: 8px; justify-content: center; }

.svc-form :deep(.el-input__wrapper),
.svc-form :deep(.el-select__wrapper) {
  border-radius: var(--radius-sm) !important;
}
</style>
