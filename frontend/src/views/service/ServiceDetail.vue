<template>
  <div class="page-container" v-loading="loading">
    <!-- 服务详情卡片 -->
    <div class="detail-card card" v-if="service">
      <div class="detail-hero" :style="{background: heroColor}">
        <div class="hero-inner">
          <span class="hero-emoji">{{ categoryEmoji[service.category] || '🐾' }}</span>
          <div class="hero-text">
            <h2>{{ service.name }}</h2>
            <div class="hero-tags">
              <el-tag :type="catTagType(service.category)" size="large" effect="dark" round>{{ categoryMap[service.category] || '其他' }}</el-tag>
              <el-tag type="success" effect="dark" round size="small">随时退款</el-tag>
              <el-tag effect="dark" round size="small">专业认证</el-tag>
            </div>
          </div>
          <div class="hero-price">
            <span class="price-num">¥{{ service.price }}</span>
            <small>/ {{ service.duration }}分钟</small>
          </div>
        </div>
      </div>
      <div class="detail-desc">{{ service.description }}</div>

      <!-- 相似服务推荐 -->
      <div class="similar-section" v-if="similarServices.length > 0">
        <h4>相似服务推荐</h4>
        <div class="similar-list">
          <div class="similar-item" v-for="s in similarServices" :key="s.id" @click="$router.push(`/services/${s.id}`)">
            <span class="similar-emoji">{{ categoryEmoji[s.category] || '🐾' }}</span>
            <span class="similar-name">{{ s.name }}</span>
            <span class="similar-price">¥{{ s.price }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 服务商选择 -->
    <div class="card">
      <h3 class="card-title"><span>🧑‍🍳</span> 可选店员</h3>
      <p class="card-subtitle">选择您信任的店员，点击立即下单</p>

      <div class="provider-list" v-if="providers.length > 0">
        <div class="provider-card" v-for="p in providers" :key="p.providerId">
          <div class="provider-info">
            <el-avatar :size="44" :style="{background:'var(--color-primary)', color:'#fff', fontWeight:'bold', fontSize:'18px'}">
              {{ (p.shopName || '#'+p.providerId).charAt(0) }}
            </el-avatar>
            <div class="provider-detail">
              <strong>{{ p.shopName || `店员 #${p.providerId}` }}</strong>
              <div class="provider-meta">
                <el-rate :model-value="4.5" disabled size="small" />
                <span class="review-count">4.5分</span>
              </div>
            </div>
          </div>
          <div class="provider-action">
            <span class="provider-price">¥{{ p.price }}</span>
            <el-button type="primary" @click="showOrderDialog(p)" round>立即预约</el-button>
          </div>
        </div>
      </div>
      <el-empty v-else description="暂无店员提供此服务" :image-size="120" />
    </div>

    <!-- 下单对话框 -->
    <el-dialog v-model="orderVisible" title="确认订单" width="520px" class="order-dialog">
      <div class="dialog-summary" v-if="selectedProvider && service">
        <div class="summary-row"><label>店员：</label><strong>{{ selectedProvider.shopName || '店员#' + selectedProvider.providerId }}</strong></div>
        <div class="summary-row"><label>服务项目：</label><strong>{{ service.name }}</strong></div>
        <div class="summary-row price-row"><label>价格：</label><strong class="hl-price">¥{{ selectedProvider.price }}</strong></div>
      </div>
      <el-form :model="orderForm" label-width="90px" style="margin-top:16px">
        <el-form-item label="选择宠物">
          <el-select v-model="orderForm.petId" placeholder="请选择您的宠物" style="width:100%" size="large">
            <el-option v-for="pet in myPets" :key="pet.id" :label="`${pet.name} (${pet.type})`" :value="pet.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="指定店员">
          <el-select v-model="orderForm.staffId" placeholder="不选则随机分配（可选）" style="width:100%" size="large" clearable>
            <el-option v-for="s in availableStaffList" :key="s.id"
              :label="`${s.realName || s.username} (提成¥${s.commissionRate||0}/单)`"
              :value="s.id">
              <div style="display:flex;justify-content:space-between;align-items:center;width:100%">
                <span>{{ s.realName || s.username }}</span>
                <span style="color:var(--color-primary);font-size:12px">提成 ¥{{ s.commissionRate||0 }}/单</span>
              </div>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="预约日期" required>
          <el-date-picker v-model="orderForm.appointmentDate" type="date" placeholder="选择预约日期"
                          style="width:100%" size="large" :disabled-date="(d) => d < new Date(Date.now()-86400000)"
                          value-format="YYYY-MM-DD" @change="loadTimeSlots" />
        </el-form-item>
        <el-form-item label="预约时段" required>
          <div class="time-slot-grid" v-if="timeSlots.length > 0">
            <div v-for="slot in timeSlots" :key="slot.value"
              class="time-slot-item" :class="{ selected: orderForm.startTime === slot.start, disabled: slot.disabled }"
              @click="!slot.disabled && selectTimeSlot(slot)">
              <span class="slot-time">{{ slot.label }}</span>
              <span class="slot-status">{{ slot.disabled ? '已约满' : '可预约' }}</span>
            </div>
          </div>
          <div v-else class="slot-hint">请先选择预约日期</div>
        </el-form-item>
        <el-form-item label="优惠券">
          <el-select v-model="orderForm.couponId" placeholder="选择优惠券（可选）" style="width:100%" size="large" clearable>
            <el-option label="新用户8折券" :value="1" />
            <el-option label="满100减20" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="orderForm.remark" type="textarea" :rows="2" placeholder="选填：特殊需求说明" maxlength="200" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="orderVisible=false" round>取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitOrder" round>确认提交订单</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { getServiceDetail, getServiceProviders, getServices } from '@/api/service'
import { getPets } from '@/api/user'
import { createOrder } from '@/api/order'
import { getAvailableStaff } from '@/api/shop'

const route = useRoute()
const userStore = useUserStore()
const loading = ref(false)
const service = ref(null)
const providers = ref([])
const myPets = ref([])
const orderVisible = ref(false)
const selectedProvider = ref(null)
const submitting = ref(false)

const categoryMap = { 1:'美容', 2:'寄养', 3:'医疗', 4:'遛狗' }
const categoryEmoji = { 1:'✂️', 2:'🏠', 3:'💊', 4:'🚶' }
function catTagType(c) { return { 1:'', 2:'success', 3:'danger', 4:'warning' }[c] || '' }

const heroColors = [
  'linear-gradient(135deg,#FF8C42 0%,#FFA96B 100%)',
  'linear-gradient(135deg,#98DDCA 0%,#6CC4A1 100%)',
  'linear-gradient(135deg,#F9A8D4 0%,#F472B6 100%)',
  'linear-gradient(135deg,#6C9EBF 0%,#93C5FD 100%)',
]
const heroColor = computed(() => heroColors[(service.value?.category||1)-1] || heroColors[0])

const similarServices = ref([])
const availableStaffList = ref([])
const orderForm = ref({ petId: null, appointmentDate: '', startTime: '', endTime: '', timeSlot: '', remark: '', couponId: null, staffId: null })
const timeSlots = ref([])

onMounted(async () => {
  loading.value = true
  try {
    const [sRes, pRes] = await Promise.all([
      getServiceDetail(route.params.id),
      getServiceProviders(route.params.id)
    ])
    service.value = sRes.data
    providers.value = pRes.data
    // 加载同分类的相似服务
    try {
    const simRes = await getServices({ page: 1, size: 6, category: sRes.data?.category })
    const all = simRes.data?.records || []
    similarServices.value = all.filter((s) => s.id !== Number(route.params.id)).slice(0, 5)
    } catch (_) {}
    if (userStore.isUser()) {
      try { const res = await getPets(); myPets.value = res.data; } catch (e) {}
    }
  } finally { loading.value = false }
})

function showOrderDialog(provider) {
  if (!userStore.token) { ElMessage.warning('请先登录'); return }
  if (!userStore.isUser()) { ElMessage.warning('只有客户可以下单'); return }
  if (myPets.value.length === 0) { ElMessage.warning('请先添加宠物'); return }
  selectedProvider.value = provider
  orderForm.value = { petId: null, appointmentDate: '', startTime: '', endTime: '', timeSlot: '', remark: '', couponId: null, staffId: null }
  timeSlots.value = []
  fetchStaffForProvider(provider.providerId)
  orderVisible.value = true
}

async function fetchStaffForProvider(providerId) {
  try {
    const res = await getAvailableStaff(providerId)
    availableStaffList.value = res.data || []
  } catch (e) { availableStaffList.value = [] }
}

// 生成时间段列表（08:00-21:00，每小时一个段）
function loadTimeSlots() {
  const slots = []
  for (let h = 8; h <= 20; h++) {
    const start = String(h).padStart(2, '0') + ':00'
    const end = String(h + 1).padStart(2, '0') + ':00'
    slots.push({ label: `${start} - ${end}`, value: `${start}-${end}`, start, end, disabled: false })
  }
  timeSlots.value = slots
}

function selectTimeSlot(slot) {
  orderForm.value.startTime = slot.start
  orderForm.value.endTime = slot.end
  orderForm.value.timeSlot = slot.value
}

async function submitOrder() {
  if (!orderForm.value.petId) { ElMessage.warning('请选择宠物'); return }
  if (!orderForm.value.appointmentDate || !orderForm.value.startTime) { ElMessage.warning('请选择预约日期和时段'); return }
  submitting.value = true
  try {
    const appointmentTime = `${orderForm.value.appointmentDate}T${orderForm.value.startTime}:00`
    await createOrder({
      providerId: selectedProvider.value.providerId,
      serviceId: service.value.id,
      petId: orderForm.value.petId,
      appointmentTime,
      startTime: orderForm.value.startTime,
      endTime: orderForm.value.endTime,
      timeSlot: orderForm.value.timeSlot,
      remark: orderForm.value.remark,
      staffId: orderForm.value.staffId || undefined
    })
    ElMessage.success('订单创建成功！')
    orderVisible.value = false
  } catch(e) {
    ElMessage.error(e?.message || '下单失败')
  }
  finally{ submitting.value = false }
}
</script>

<style scoped>
.detail-card { padding: 0; overflow: hidden; }
.detail-hero { padding: 32px 28px; color: #fff; }
.hero-inner { display: flex; align-items: center; gap: 20px; }
.hero-emoji { font-size: 56px; filter: drop-shadow(0 4px 12px rgba(0,0,0,.15)); }
.hero-text h2 { font-size: 24px; margin-bottom: 10px; color: #fff; }
.hero-tags { display: flex; gap: 8px; flex-wrap: wrap; }
.hero-price { margin-left: auto; text-align: right; }
.price-num { font-size: 32px; font-weight: 800; display: block; }
.hero-price small { opacity: 0.85; }
.detail-desc { padding: 20px 28px; color: var(--color-text-secondary); line-height: 1.8; background: var(--color-cream); font-size: 14px; }

/* 相似推荐 */
.similar-section { padding: 16px 28px; border-top: 1px solid var(--color-divider); }
.similar-section h4 { font-size: 14px; color: var(--color-text-secondary); margin-bottom: 10px; }
.similar-list { display: flex; gap: 12px; overflow-x: auto; padding-bottom: 4px; }
.similar-item {
  display: flex; align-items: center; gap: 8px;
  padding: 8px 14px; border-radius: var(--radius-full);
  background: var(--color-cream); cursor: pointer;
  white-space: nowrap; font-size: 13px;
  transition: var(--transition-fast);
  border: 1.5px solid transparent;
}
.similar-item:hover { border-color: var(--color-primary); background: var(--color-pink-light); }
.similar-emoji { font-size: 16px; }
.similar-name { font-weight: 500; }
.similar-price { color: var(--color-primary); font-weight: 600; }

/* 店员列表 */
.card-title { font-size: 18px; font-weight: 700; margin-bottom: 4px; display: flex; align-items: center; gap: 8px; }
.card-subtitle { color: var(--color-text-placeholder); font-size: 13px; margin-bottom: 16px; }
.provider-list { display: flex; flex-direction: column; gap: 12px; }
.provider-card {
  display: flex; align-items: center; justify-content: space-between;
  padding: 16px 20px; border-radius: var(--radius-md);
  background: var(--color-cream); border: 1.5px solid var(--color-divider);
  transition: var(--transition-fast);
}
.provider-card:hover { border-color: var(--color-primary); box-shadow: var(--shadow-sm); }
.provider-info { display: flex; align-items: center; gap: 14px; }
.provider-detail strong { font-size: 15px; display: block; margin-bottom: 4px; }
.provider-meta { display: flex; align-items: center; gap: 8px; }
.review-count { font-size: 12px; color: var(--color-text-placeholder); }
.provider-action { display: flex; align-items: center; gap: 14px; }
.provider-price { font-size: 20px; font-weight: 700; color: var(--color-primary); }

/* 对话框 */
.dialog-summary { background: var(--color-cream); border-radius: var(--radius-md); padding: 16px 20px; }
.summary-row { padding: 6px 0; color: var(--color-text-secondary); font-size: 14px; }
.summary-row label { color: var(--color-text-placeholder); min-width: 72px; display: inline-block; }
.hl-price { color: var(--color-primary); font-size: 22px; }

/* 时间段选择 */
.time-slot-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 8px; }
.time-slot-item {
  padding: 10px 8px; border-radius: 8px; text-align: center; cursor: pointer;
  border: 1.5px solid var(--color-divider); background: var(--color-cream);
  transition: all 0.2s;
}
.time-slot-item:hover:not(.disabled) { border-color: var(--color-primary); }
.time-slot-item.selected { border-color: var(--color-primary); background: rgba(255,140,66,0.12); }
.time-slot-item.disabled { opacity: 0.5; cursor: not-allowed; background: var(--color-divider); }
.slot-time { display: block; font-weight: 600; font-size: 14px; }
.slot-status { display: block; font-size: 11px; color: var(--color-text-placeholder); margin-top: 2px; }
.slot-hint { color: var(--color-text-placeholder); font-size: 13px; text-align: center; padding: 16px; }

@media (max-width: 768px) {
  .hero-inner { flex-wrap: wrap; }
  .hero-price { margin-left: 0; text-align: left; }
  .provider-card { flex-direction: column; gap: 12px; align-items: flex-start; }
  .provider-action { width: 100%; justify-content: space-between; }
}
</style>
