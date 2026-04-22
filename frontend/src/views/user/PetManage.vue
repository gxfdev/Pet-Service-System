<template>
  <div class="page-container">
    <div class="card">
      <div class="page-header">
        <h2 class="page-title"><span>🐾</span> 我的宠物</h2>
        <el-button type="primary" @click="showAddDialog" round>+ 添加宠物</el-button>
      </div>

      <el-row :gutter="16" v-loading="loading">
        <el-col :xs="12" :sm="8" v-for="(pet, pIdx) in pets" :key="pet.id">
          <div class="pet-card">
            <div class="pet-avatar-wrap" :style="{background: petColors[pIdx % petColors.length]}">
              <span class="pet-type-emoji">{{ pet.type === '狗' ? '🐕' : pet.type === '猫' ? '🐈' : '🐾' }}</span>
            </div>
            <h4>{{ pet.name }}</h4>
            <p class="pet-info">{{ pet.type }} / {{ pet.breed || '未知品种' }}</p>
            <p class="pet-info">
              {{ pet.gender === 1 ? '♂ 公' : pet.gender === 0 ? '♀ 母' : '' }}
              {{ pet.weight ? ` / ${pet.weight}kg` : '' }}
            </p>
            <!-- 疫苗提醒 -->
            <div class="vaccine-tag" v-if="needsVaccine(pet)">
              <span>💉</span> 疫苗到期提醒
            </div>
            <div class="pet-actions">
              <el-button size="small" @click="showEditDialog(pet)" round>编辑</el-button>
              <el-button size="small" type="danger" @click="handleDelete(pet.id)" round>删除</el-button>
            </div>
          </div>
        </el-col>
      </el-row>

      <el-empty v-if="!loading && pets.length === 0" description="还没有宠物，快来添加吧" />
    </div>

    <!-- 新增/编辑对话框 -->
    <el-dialog :title="dialogTitle" v-model="visible" width="500px">
      <el-form :model="form" label-width="80px" class="pet-form">
        <el-form-item label="昵称"><el-input v-model="form.name" class="warm-input" /></el-form-item>
        <el-form-item label="类型">
          <el-select v-model="form.type" placeholder="选择类型" style="width:100%">
            <el-option label="🐕 狗" value="狗" />
            <el-option label="🐈 猫" value="猫" />
            <el-option label="🐾 其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="品种"><el-input v-model="form.breed" placeholder="选填" class="warm-input" /></el-form-item>
        <el-form-item label="性别">
          <el-radio-group v-model="form.gender">
            <el-radio :value="1">♂ 公</el-radio>
            <el-radio :value="0">♀ 母</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="生日"><el-date-picker v-model="form.birthday" type="date" placeholder="选填" style="width:100%" /></el-form-item>
        <el-form-item label="体重(kg)"><el-input-number v-model="form.weight" :precision="2" :min="0" :max="200" /></el-form-item>
        <el-form-item label="疫苗日期"><el-date-picker v-model="form.lastVaccine" type="date" placeholder="上次接种日期" style="width:100%" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="visible = false" round>取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit" round>确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getPets, addPet, updatePet, deletePet } from '@/api/user'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const pets = ref([])
const visible = ref(false)
const submitting = ref(false)
const isEdit = ref(false)
const editId = ref(null)
const form = ref({ name: '', type: '', breed: '', gender: null, birthday: '', weight: null, lastVaccine: '' })

const petColors = [
  'linear-gradient(135deg,#FF8C42,#FFA96B)',
  'linear-gradient(135deg,#98DDCA,#6CC4A1)',
  'linear-gradient(135deg,#F9A8D4,#F472B6)',
  'linear-gradient(135deg,#6C9EBF,#93C5FD)',
]

const dialogTitle = computed(() => isEdit.value ? '编辑宠物' : '添加宠物')

function needsVaccine(pet) {
  if (!pet.lastVaccine) return true
  const last = new Date(pet.lastVaccine)
  const diff = (Date.now() - last.getTime()) / (1000 * 60 * 60 * 24)
  return diff > 300
}

onMounted(fetchPets)

async function fetchPets() {
  loading.value = true
  try { const res = await getPets(); pets.value = res.data; }
  finally { loading.value = false }
}

function showAddDialog() {
  isEdit.value = false; editId.value = null
  form.value = { name: '', type: '', breed: '', gender: null, birthday: '', weight: null, lastVaccine: '' }
  visible.value = true
}

function showEditDialog(pet) {
  isEdit.value = true; editId.value = pet.id
  form.value = { ...pet }
  visible.value = true
}

async function handleSubmit() {
  if (!form.value.name) return ElMessage.warning('请填写昵称')
  submitting.value = true
  try {
    if (isEdit.value) await updatePet(editId.value, form.value)
    else await addPet(form.value)
    ElMessage.success(isEdit.value ? '修改成功' : '添加成功')
    visible.value = false
    fetchPets()
  } catch (e) { ElMessage.error(e?.message || '操作失败') }
  finally { submitting.value = false }
}

function handleDelete(id) {
  ElMessageBox.confirm('确认删除此宠物？', '提示', { type: 'warning' }).then(async () => {
    await deletePet(id); ElMessage.success('已删除'); fetchPets()
  }).catch(() => {})
}
</script>

<style scoped>
.pet-card {
  text-align: center;
  padding: 24px 16px;
  border-radius: var(--radius-lg);
  background: var(--color-surface);
  border: 1.5px solid var(--color-divider);
  margin-bottom: 16px;
  transition: var(--transition-normal);
}
.pet-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-md);
  border-color: transparent;
}
.pet-avatar-wrap {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 12px;
}
.pet-type-emoji { font-size: 36px; }
.pet-card h4 { font-size: 17px; color: var(--color-text); margin-bottom: 4px; }
.pet-info { color: var(--color-text-placeholder); font-size: 13px; margin: 2px 0; }
.vaccine-tag {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  margin-top: 8px;
  padding: 4px 12px;
  background: #FFF3E0;
  color: var(--color-primary);
  font-size: 11px;
  font-weight: 600;
  border-radius: var(--radius-full);
}
.pet-actions {
  margin-top: 12px;
  display: flex;
  gap: 8px;
  justify-content: center;
}

.warm-input :deep(.el-input__wrapper) {
  border-radius: var(--radius-sm) !important;
  background: var(--color-cream) !important;
  box-shadow: none !important;
  border: 1.5px solid transparent;
}
.warm-input :deep(.el-input__wrapper:focus-within) {
  background: #fff !important;
  border-color: var(--color-primary);
}
</style>
