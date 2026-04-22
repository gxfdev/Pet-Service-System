<template>
  <div class="page-container">
    <div class="card">
      <div class="page-header">
        <h2 class="page-title"><span>📋</span> {{ pageTitle }}</h2>
      </div>

      <!-- 状态Tab -->
      <el-tabs v-model="activeStatus" @tab-change="fetchOrders" class="status-tabs">
        <el-tab-pane label="全部" name="-1" />
        <el-tab-pane label="待支付" name="0" />
        <el-tab-pane label="待服务" name="1" />
        <el-tab-pane label="服务中" name="2" />
        <el-tab-pane label="已完成" name="3" />
        <el-tab-pane label="已取消" name="4" />
      </el-tabs>

      <!-- 订单卡片列表 -->
      <div v-loading="loading" class="order-list">
        <div class="order-card" v-for="order in orderList" :key="order.id">
          <div class="order-header">
            <span class="order-no">订单号：{{ order.orderNo }}</span>
            <el-tag :type="statusType(order.status)" effect="dark" round size="small">{{ statusText(order.status) }}</el-tag>
          </div>

          <!-- 进度跟踪 -->
          <div class="order-progress" v-if="[1,2].includes(order.status)">
            <div class="progress-bar">
              <div class="progress-step" :class="{active: order.status >= 1}"><span>已接单</span></div>
              <div class="progress-line" :class="{active: order.status >= 2}"></div>
              <div class="progress-step" :class="{active: order.status >= 2}"><span>服务中</span></div>
              <div class="progress-line" :class="{active: order.status >= 3}"></div>
              <div class="progress-step" :class="{active: order.status >= 3}"><span>已完成</span></div>
            </div>
          </div>

          <div class="order-body">
            <div class="order-info">
              <p><strong>服务：</strong>{{ order.serviceName || '服务#' + order.serviceId }}</p>
              <p><strong>宠物：</strong><template v-if="showCustomerInfo">{{ order.petName || '宠物#' + order.petId }}</template><span v-else style="color:var(--color-text-placeholder)">***</span></p>
            </div>
            <div class="order-info">
              <p><strong>店员：</strong><template v-if="showStaffInfo">{{ displayStaffName(order) }}</template><span v-else style="color:var(--color-text-placeholder)">***</span></p>
              <p><strong>预约时间：</strong>{{ formatTime(order.appointmentTime) }}</p>
            </div>
            <div class="order-amount">
              <span class="amount">¥{{ order.totalAmount }}</span>
              <span class="time">{{ formatTime(order.createTime) }} 创建</span>
            </div>
          </div>

          <div class="order-actions">
            <!-- 客户操作 -->
            <template v-if="userStore.isUser()">
              <el-button type="primary" size="small" @click="handlePay(order)" v-if="order.status === 0" round>立即支付</el-button>
              <el-button size="small" @click="showCancel(order)" v-if="order.status === 0" round>取消订单</el-button>
              <el-button type="success" size="small" @click="showReview(order)" v-if="order.status === 3 && !order.reviewed" round>写评价</el-button>
            </template>

            <!-- 店员操作 -->
            <template v-else-if="userStore.isShopStaff()">
              <!-- 未实名绑定 -->
              <template v-if="!isRealNameBound">
                <el-tag type="warning" effect="dark" round size="small" style="margin-right:8px">⚠️ 请先完成实名认证</el-tag>
                <el-button type="primary" size="small" round @click="$router.push('/user/profile')">去认证</el-button>
              </template>
              <!-- 实名后可操作 -->
              <template v-else>
                <div class="staff-actions" v-if="order.status === 1">
                  <span class="action-hint">请确认能在预约时间内完成此服务：</span>
                  <el-popconfirm title="确认接单？" :description="'接单后请在约定时间提供服务' + (arrivalTimeMap[order.id] ? '（到岗时间:' + arrivalTimeMap[order.id] + '）' : '')" confirm-button-text="确认接单" cancel-button-text="再想想" @confirm="handleAccept(order)">
                    <el-button type="primary" size="small" round>✅ 确认接单</el-button>
                  </el-popconfirm>
                  <el-select v-model="arrivalTimeMap[order.id]" placeholder="到岗时间(可选)" size="small" clearable style="width:140px; margin-left:8px; vertical-align:middle">
                    <el-option label="09:00 到岗" value="09:00" />
                    <el-option label="10:00 到岗" value="10:00" />
                    <el-option label="11:00 到岗" value="11:00" />
                    <el-option label="14:00 到岗" value="14:00" />
                    <el-option label="15:00 到岗" value="15:00" />
                    <el-option label="16:00 到岗" value="16:00" />
                    <el-option label="17:00 到岗" value="17:00" />
                    <el-option label="18:00 到岗" value="18:00" />
                    <el-option label="19:00 到岗" value="19:00" />
                    <el-option label="20:00 到岗" value="20:00" />
                  </el-select>
                </div>
                <el-button type="warning" size="small" @click="handleComplete(order)" v-if="order.status === 2" round>完成服务</el-button>
                <el-button type="success" size="small" @click="showReview(order)" v-if="order.status === 3 && order.staffId === currentUserId" round>评价服务</el-button>
              </template>
            </template>

            <!-- 店长/管理员操作（仅店长可取消订单） -->
            <template v-else>
              <span class="role-tag owner-tag" v-if="isOwnerOrAdmin">👑 管理视图</span>
              <el-button type="danger" size="small" @click="showCancel(order)" v-if="isOwnerOrAdmin && [0,1,2].includes(order.status)" round>取消订单</el-button>
              <el-tag v-if="!isOwnerOrAdmin && userStore.isShopStaff()" type="info" size="small" effect="plain" round>仅店长可取消</el-tag>
              <el-button type="info" size="small" @click="handleRefund(order)" v-if="order.status === 3 || order.status === 5" round>退款处理</el-button>
              <el-button type="success" size="small" round @click="handleComplete(order)" v-if="[2].includes(order.status)">标记完成</el-button>
            </template>
          </div>
        </div>

        <el-empty v-if="!loading && orderList.length === 0" description="暂无订单" />
      </div>
    </div>

    <!-- 取消对话框 -->
    <el-dialog v-model="cancelVisible" title="取消订单" width="400px">
      <el-input v-model="cancelReason" placeholder="请输入取消原因（选填）" />
      <template #footer>
        <el-button @click="cancelVisible=false" round>取消</el-button>
        <el-button type="danger" @click="confirmCancel" round>确认取消</el-button>
      </template>
    </el-dialog>

    <!-- 评价对话框 -->
    <el-dialog v-model="reviewVisible" title="评价服务" width="500px">
      <div class="review-form">
        <div class="tag-rating">
          <span class="rating-label">点击标签快速评价：</span>
          <div class="rating-tags">
            <el-check-tag v-for="tag in reviewTags" :key="tag" :checked="selectedTags.includes(tag)" @change="toggleTag(tag)">{{ tag }}</el-check-tag>
          </div>
        </div>
        <el-rate v-model="reviewForm.rating" show-text size="large" style="margin:12px 0" />
        <el-input v-model="reviewForm.content" type="textarea" :rows="3" placeholder="分享您的服务体验..." />
      </div>
      <template #footer>
        <el-button @click="reviewVisible=false" round>取消</el-button>
        <el-button type="primary" @click="submitReview" round>提交评价</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useUserStore } from '@/stores/user'
import { getOrders, payOrder, cancelOrder, acceptOrder, completeOrder, addReview as submitAddReview } from '@/api/order'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()
const loading = ref(false)
const orderList = ref([])
const activeStatus = ref(-1)
const cancelVisible = ref(false)
const cancelReason = ref('')
const reviewVisible = ref(false)
const currentOrder = ref(null)
const reviewForm = ref({ rating: 5, content: '' })
const selectedTags = ref([])
const reviewTags = ['很专业', '环境好', '服务态度好', '价格合理', '准时到达', '宠物喜欢']
const arrivalTimeMap = ref({})

const pageTitle = computed(() => {
  if (userStore.isShopStaff()) return '我的工作订单'
  if (userStore.isProvider() || userStore.isAdmin()) return '订单管理'
  return '我的订单'
})

// 店员是否已实名绑定
const isRealNameBound = computed(() => !!(userStore.userInfo.realName && userStore.userInfo.idCard))
const currentUserId = computed(() => userStore.userInfo.id || 0)
const isOwnerOrAdmin = computed(() => {
  const r = userStore.userInfo.role
  return r === 2 || (r === 1 && (!userStore.userInfo.staffRole || userStore.userInfo.staffRole === 1))
})

async function fetchOrders() {
  loading.value = true
  try {
    const params = { page: 1, size: 50 }
    const s = Number(activeStatus.value)
    if (!isNaN(s) && s >= 0) params.status = s
    const res = await getOrders(params)
    orderList.value = res.data.records || res.data || []
  } finally { loading.value = false }
}
fetchOrders()

function statusType(s) { return { 0:'warning', 1:'', 2:'primary', 3:'success', 4:'info', 5:'danger' }[s] || '' }
function statusText(s) { return { 0:'待支付', 1:'待服务', 2:'服务中', 3:'已完成', 4:'已取消', 5:'退款中' }[s] || '未知' }
function formatTime(t) { return t ? new Date(t).toLocaleString() : '-' }

// 根据角色显示店员名称（权限控制）
function displayStaffName(order) {
  // 客户视角：显示分配的店员名
  if (userStore.isUser()) {
    return order.staffName || order.shopName || '未分配'
  }
  // 店员视角：显示自己的名字（如果该订单分配给了自己）
  if (userStore.isShopStaff()) {
    if (order.staffId === currentUserId.value) {
      return userStore.userInfo.realName || userStore.userInfo.username || '我'
    }
    // 其他店员的订单也显示名字（同店铺）
    return order.staffName || order.shopName || '未分配'
  }
  // 店长/管理员：可看全部
  return order.staffName || order.shopName || '-'
}

// 权限过滤：根据角色决定显示哪些字段
const showCustomerInfo = computed(() => userStore.isUser() || userStore.isShopStaff())
const showStaffInfo = computed(() => true)

function toggleTag(tag) {
  const idx = selectedTags.value.indexOf(tag)
  if (idx >= 0) selectedTags.value.splice(idx, 1)
  else selectedTags.value.push(tag)
}

async function handlePay(order) {
  try { await payOrder(order.id); ElMessage.success('支付成功！'); fetchOrders(); } catch(e) { ElMessage.error(e?.message || '支付失败') }
}
function showCancel(o) { currentOrder.value = o; cancelReason.value = ''; cancelVisible.value = true; }
async function confirmCancel() {
  try { await cancelOrder(currentOrder.value.id, cancelReason.value); ElMessage.success('已取消'); cancelVisible.value = false; fetchOrders(); } catch(e) {}
}
async function handleAccept(o) {
  try { await acceptOrder(o.id); ElMessage.success('已接单'); fetchOrders(); } catch(e) {}
}
async function handleComplete(o) {
  try { await completeOrder(o.id); ElMessage.success('已完成'); fetchOrders(); } catch(e) {}
}

// 管理员/店长操作
async function handleRefund(order) {
  try { await request.put(`/admin/orders/${order.id}/intervene`, { action: 'refund', reason: '管理员退款' }); ElMessage.success('已发起退款'); fetchOrders(); } catch(e) {}
}
function showReview(o) { currentOrder.value = o; reviewForm.value = { rating: 5, content: '' }; selectedTags.value = []; reviewVisible.value = true; }
async function submitReview() {
  try {
    await submitAddReview(currentOrder.value.id, { ...reviewForm.value, tags: selectedTags.value })
    ElMessage.success('评价成功')
    reviewVisible.value = false
    fetchOrders()
  } catch(e) {}
}
</script>

<style scoped>
.status-tabs :deep(.el-tabs__item.is-active) { color: var(--color-primary); font-weight: 600; }
.status-tabs :deep(.el-tabs__active-bar) { background-color: var(--color-primary); }

.order-list { display: flex; flex-direction: column; gap: 14px; }

.order-card {
  border: 1.5px solid var(--color-divider);
  border-radius: var(--radius-lg);
  padding: 18px;
  background: var(--color-surface);
  transition: var(--transition-fast);
  border-left: 4px solid transparent;
}
.order-card:hover { box-shadow: var(--shadow-sm); }
.order-card:has(.el-tag--warning) { border-left-color: var(--color-primary); }
.order-card:has(.el-tag--primary) { border-left-color: #6C9EBF; }
.order-card:has(.el-tag--success) { border-left-color: #98DDCA; }

.order-header {
  display: flex; justify-content: space-between; align-items: center;
  padding-bottom: 12px; border-bottom: 1px solid var(--color-divider);
  font-size: 13px; color: var(--color-text-secondary);
}

/* 进度跟踪 */
.order-progress { padding: 16px 0; }
.progress-bar { display: flex; align-items: center; gap: 0; }
.progress-step {
  width: 32px; height: 32px; border-radius: 50%;
  background: var(--color-divider);
  display: flex; align-items: center; justify-content: center;
  font-size: 10px; color: var(--color-text-placeholder);
  flex-shrink: 0; position: relative;
}
.progress-step.active {
  background: var(--color-primary);
  color: #fff;
}
.progress-step span {
  position: absolute;
  top: 36px;
  white-space: nowrap;
  font-size: 11px;
}
.progress-line {
  flex: 1;
  height: 3px;
  background: var(--color-divider);
  margin: 0 4px;
}
.progress-line.active { background: var(--color-primary); }

.order-body {
  display: flex; gap: 20px; padding: 14px 0;
  flex-wrap: wrap;
}
.order-info { flex: 1; min-width: 150px; }
.order-info p { margin: 4px 0; color: var(--color-text-secondary); font-size: 14px; }
.order-amount { text-align: right; }
.amount { color: var(--color-primary); font-size: 20px; font-weight: 700; display: block; }
.time { color: var(--color-text-placeholder); font-size: 12px; }

.order-actions {
  display: flex; gap: 8px; justify-content: flex-end;
  padding-top: 10px; border-top: 1px solid var(--color-divider);
}

/* 评价 */
.review-form { padding: 8px 0; }
.rating-label { font-size: 13px; color: var(--color-text-secondary); margin-bottom: 8px; display: block; }
.rating-tags { display: flex; gap: 8px; flex-wrap: wrap; margin-bottom: 12px; }

@media (max-width: 768px) {
  .order-body { flex-direction: column; gap: 8px; }
  .order-amount { text-align: left; }
}
</style>
