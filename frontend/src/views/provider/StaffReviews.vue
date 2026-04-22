<template>
  <div class="page-container">
    <div class="perm-banner" :class="isOwner ? 'owner' : 'staff'">
      <span class="perm-icon">{{ isOwner ? '👑' : isCustomer ? '👤' : '🧑‍🍳' }}</span>
      <div class="perm-info">
        <strong>{{ isOwner ? '全部服务评价（含实名）' : isCustomer ? '服务评价中心' : '匿名评价查看区' }}</strong>
        <p>{{ isOwner ? '可查看所有客户对店员的评价，包含完整客户信息' : isCustomer ? '查看您已完成的订单并评价店员服务' : '仅显示匿名化后的客户评价，保护隐私' }}</p>
      </div>
    </div>

    <!-- 统计概览 -->
    <el-row :gutter="14" class="stats-row">
      <el-col :xs="12" :sm="6">
        <div class="stat-card glass-card"><div class="stat-icon">⭐</div><div class="stat-data"><span class="stat-value">{{ avgRating }}</span><span class="stat-label">平均评分</span></div></div>
      </el-col>
      <el-col :xs="12" :sm="6">
        <div class="stat-card glass-card"><div class="stat-icon">💬</div><div class="stat-data"><span class="stat-value">{{ totalReviews }}</span><span class="stat-label">总评价数</span></div></div>
      </el-col>
      <el-col :xs="12" :sm="6">
        <div class="stat-card glass-card"><div class="stat-icon">👍</div><div class="stat-data"><span class="stat-value">{{ goodRate }}%</span><span class="stat-label">好评率</span></div></div>
      </el-col>
      <el-col :xs="12" :sm="6">
        <div class="stat-card glass-card"><div class="stat-icon">📊</div><div class="stat-data"><span class="stat-value">{{ ratedOrders }}</span><span class="stat-label">已评订单</span></div></div>
      </el-col>
    </el-row>

    <!-- 评价列表 -->
    <div class="card">
      <div class="page-header">
        <h3 class="section-title"><span>💬</span> 全部评价</h3>
        <el-select v-model="filterStaff" placeholder="筛选店员" clearable style="width:160px" size="small" v-if="isOwner || staffOptions.length > 0">
          <el-option label="全部" value="" />
          <el-option v-for="s in staffOptions" :key="s.id" :label="s.realName || s.username" :value="s.id" />
        </el-select>
        <el-rate v-model="minRating" :max="5" show-score score-template="{value}分以上" style="margin-left:auto" @change="fetchReviews" />
      </div>

      <div class="review-list" v-loading="loading">
        <div v-for="review in filteredReviews" :key="review.id" class="review-item glass-card">
          <div class="review-header">
            <div class="reviewer-info">
              <el-avatar :size="40" :style="{ background: avatarColor(review) }">
                {{ reviewerName(review).charAt(0) }}
              </el-avatar>
              <div class="reviewer-detail">
                <strong>{{ reviewerName(review) }}</strong>
                <span class="review-meta">
                  {{ isOwner && !review.isAnonymous ? review.customerPhone : '' }}
                  {{ formatTime(review.createTime) }}
                  · 订单 #{{ review.orderId }}
                </span>
              </div>
            </div>
            <el-rate :model-value="review.rating" disabled size="small" />
          </div>

          <div class="review-content">{{ review.content || '用户未填写评价内容' }}</div>

          <div class="review-images" v-if="review.images">
            <img v-for="(img, idx) in parseImages(review.images)" :key="idx" :src="img" alt="" class="review-img" />
          </div>

          <div class="review-service-info">
            <span class="service-tag">📋 {{ review.serviceName }}</span>
            <span class="pet-tag">🐾 {{ review.petName }}</span>
            <span v-if="isOwner" class="staff-tag">🧑‍🍳 {{ review.staffName || '-' }}</span>
          </div>
        </div>

        <el-empty v-if="!loading && filteredReviews.length === 0" description="暂无评价记录" :image-size="100" />

        <!-- 分页 -->
        <div class="pagination-wrap" v-if="total > pageSize">
          <el-pagination background layout="prev, pager, next" :total="total" :current-page="currentPage" :page-size="pageSize" @change="fetchReviews" />
        </div>
      </div>
    </div>

    <!-- 客户：去评价弹窗 -->
    <el-dialog v-model="showReviewDialog" title="评价服务" width="480px" destroy-on-close>
      <div v-if="selectedOrderForReview" class="review-form-ctx">
        <p class="order-summary">订单 #{{ selectedOrderForReview.orderNo }} · {{ selectedOrderForReview.serviceName }} · ¥{{ selectedOrderForReview.totalAmount }}</p>
        <el-form :model="reviewForm" label-width="70px">
          <el-form-item label="评分">
            <el-rate v-model="reviewForm.rating" :max="5" show-text :texts="['很差','较差','一般','满意','非常满意']" />
          </el-form-item>
          <el-form-item label="评价">
            <el-input v-model="reviewForm.content" type="textarea" :rows="3" placeholder="分享您的服务体验..." maxlength="200" show-word-limit />
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <el-button @click="showReviewDialog=false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitReview">提交评价</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const userStore = useUserStore()
const loading = ref(false)
const submitting = ref(false)
const reviews = ref([])
const staffOptions = ref([])
const filterStaff = ref('')
const minRating = ref(0)
const currentPage = ref(1)
const pageSize = 10
const total = ref(0)

const showReviewDialog = ref(false)
const selectedOrderForReview = ref(null)
const reviewForm = ref({ rating: 5, content: '' })

const isOwner = computed(() => {
  const r = userStore.userInfo.role
  return r === 2 || (r === 1 && (!userStore.userInfo.staffRole || userStore.userInfo.staffRole === 1))
})
const isCustomer = computed(() => userStore.userInfo.role === 0)

const filteredReviews = computed(() => {
  let result = reviews.value
  if (filterStaff.value) {
    result = result.filter(r => r.staffId === filterStaff.value)
  }
  if (minRating.value > 0) {
    result = result.filter(r => r.rating >= minRating.value)
  }
  return result
})

// 统计
const avgRating = computed(() => {
  if (!reviews.value.length) return '0.0'
  return (reviews.value.reduce((s, r) => s + r.rating, 0) / reviews.value.length).toFixed(1)
})
const totalReviews = computed(() => reviews.value.length)
const goodRate = computed(() => {
  if (!reviews.value.length) return 0
  return Math.round(reviews.value.filter(r => r.rating >= 4).length / reviews.value.length * 100)
})
const ratedOrders = computed(() => new Set(reviews.value.map(r => r.orderId)).size)

function avatarColor(r) { return ['linear-gradient(135deg,#667eea,#764ba2)','#FF8C42','#98DDCA','#FFD1DC'][(r?.id||0)%4] }

function reviewerName(r) {
  // 店员视角：匿名化
  if (!isOwner.value && userStore.userInfo.role === 1 && userStore.userInfo.staffRole === 2) {
    return r.userName ? (r.userName.charAt(0) + '**') : '匿名用户'
  }
  return r.userName || r.realName || '匿名用户'
}

function parseImages(str) {
  try { return typeof str === 'string' ? JSON.parse(str) : [] } catch { return [] }
}
function formatTime(t) { return t ? new Date(t).toLocaleString() : '-' }

async function fetchReviews() {
  loading.value = true
  try {
    // 调用专门的评价列表接口
    const res = await request.get('/reviews', { params: { page: 1, size: 200 } })
    reviews.value = (res.data || []).map(r => ({
      id: r.id,
      orderId: r.orderId,
      orderNo: r.orderNo || '',
      rating: r.rating || 5,
      content: r.content || '',
      images: r.images || '',
      createTime: r.createTime,
      userName: r.userName || '匿名用户',
      customerPhone: '',
      serviceName: r.serviceName || '',
      petName: r.petName || '',
      staffId: r.staffId,
      staffName: r.staffName || '',
      isAnonymous: r.isAnonymous || false
    }))
    total.value = reviews.value.length
  } catch (e) {
    console.error('获取评价列表失败', e)
  } finally { loading.value = false }
}

// 加载店员列表供筛选
async function fetchStaffList() {
  if (!isOwner.value) return
  try {
    const res = await request.get('/income/staff-list')
    staffOptions.value = res.data || []
  } catch (e) {}
}

// 打开评价弹窗
function openReview(order) {
  if (!userStore.token) { ElMessage.warning('请先登录'); return }
  selectedOrderForReview.value = order
  reviewForm.value = { rating: 5, content: '' }
  showReviewDialog.value = true
}
defineExpose({ openReview })

async function submitReview() {
  if (!selectedOrderForReview.value) return
  submitting.value = true
  try {
    await request.post(`/orders/${selectedOrderForReview.value.id}/review`, {
      rating: reviewForm.value.rating,
      content: reviewForm.value.content
    })
    ElMessage.success('感谢您的评价！')
    showReviewDialog.value = false
    fetchReviews()
  } catch (e) {} finally { submitting.value = false }
}

onMounted(async () => {
  await Promise.all([fetchReviews(), fetchStaffList()])
})
</script>

<style scoped>
.perm-banner {
  display:flex; align-items:center; gap:14px; padding:14px 20px;
  border-radius:var(--radius-md); margin-bottom:20px; font-size:14px;
}
.perm-banner.owner{ background:linear-gradient(135deg,rgba(255,140,66,0.08),rgba(255,209,102,0.06)); border:1.5px solid rgba(255,140,66,0.15); }
.perm-banner.staff{ background:linear-gradient(135deg,rgba(103,194,58,0.06),rgba(156,195,230,0.04)); border:1.5px solid rgba(103,194,58,0.1); }
.perm-icon{ font-size:28px; } .perm-info strong{ display:block; color:var(--color-text); margin-bottom:2px; }
.perm-info p{ color:var(--color-text-secondary); margin:0; font-size:13px; }

.stats-row{ margin-bottom:20px; }
.stat-card{ display:flex; align-items:center; gap:14px; padding:18px; border-radius:var(--radius-lg); }
.stat-icon{ font-size:28px; } .stat-data{ display:flex; flex-direction:column; gap:2px; }
.stat-value{ font-family:'Poppins'; font-size:22px; font-weight:800; color:var(--color-text); }
.stat-label{ font-size:12px; color:var(--color-text-placeholder); }

.section-title{ font-size:17px; font-weight:700; display:flex; align-items:center; gap:8px; margin:0; }
.page-header{ display:flex; align-items:center; justify-content:space-between; margin-bottom:16px; }

/* 评价卡片 */
.review-list{ display:flex; flex-direction:column; gap:14px; }
.review-item{
  padding:18px 22px; border-radius:var(--radius-lg);
  transition:transform 0.2s, box-shadow 0.2s;
}
.review-item:hover{ transform:translateY(-2px); box-shadow:0 4px 16px rgba(0,0,0,0.08); }
.review-header{
  display:flex; align-items:center; justify-content:space-between; margin-bottom:10px;
}
.reviewer-info{ display:flex; align-items:center; gap:10px; }
.reviewer-detail strong{ font-size:14px; color:var(--color-text); display:block; }
.review-meta{ font-size:12px; color:var(--color-text-placeholder); }
.review-content{ font-size:14px; line-height:1.7; color:var(--color-text-secondary); white-space:pre-wrap; word-break:break-all; }
.review-images{ display:flex; gap:6px; margin-top:10px; flex-wrap:wrap; }
.review-img{ width:80px; height:60px; object-fit:cover; border-radius:6px; cursor:pointer; transition:transform 0.2s; }
.review-img:hover{ transform:scale(1.05); }
.review-service-info{ display:flex; gap:8px; margin-top:10px; flex-wrap:wrap; }
.service-tag,.pet-tag,.staff-tag{ font-size:12px; padding:3px 10px; border-radius:10px; background:var(--color-cream); color:var(--color-text-secondary); }

.pagination-wrap{ display:flex; justify-content:center; margin-top:18px; }
.order-summary{ font-size:13px; color:var(--color-text-secondary); margin-bottom:16px; padding:10px; background:var(--color-cream); border-radius:8px; }
@media(max-width:768px){ .stats-row .el-col{margin-bottom:10px;} }
</style>
