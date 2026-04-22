<template>
  <div class="page-container">
    <!-- 权限提示条 -->
    <div class="perm-banner" :class="isOwner ? 'owner' : 'staff'">
      <span class="perm-icon">{{ isOwner ? '👑' : '👤' }}</span>
      <div class="perm-info">
        <strong>{{ isOwner ? '店长管理视图 - 完整权限' : '店员视图 - 我的收益' }}</strong>
        <p>{{ isOwner ? '可查看店铺全部收入、分配订单提成、管理员工薪资、执行结算' : '查看个人提成收益明细与月度收入汇总' }}</p>
      </div>
    </div>

    <!-- ====== 收入概览卡片 ====== -->
    <el-row :gutter="14" class="stats-row">
      <!-- 店长视角 -->
      <template v-if="isOwner">
        <el-col :xs="12" :sm="6">
          <div class="stat-card glass-card revenue-card">
            <div class="stat-icon">💰</div>
            <div class="stat-data">
              <span class="stat-value">¥{{ stats.totalRevenue || '0.00' }}</span>
              <span class="stat-label">总营收</span>
            </div>
          </div>
        </el-col>
        <el-col :xs="12" :sm="6">
          <div class="stat-card glass-card shop-card">
            <div class="stat-icon">🏪</div>
            <div class="stat-data">
              <span class="stat-value">¥{{ stats.shopIncome || '0.00' }}</span>
              <span class="stat-label">店铺净收入</span>
            </div>
          </div>
        </el-col>
        <el-col :xs="12" :sm="6">
          <div class="stat-card glass-card staff-cost-card">
            <div class="stat-icon">👥</div>
            <div class="stat-data">
              <span class="stat-value">¥{{ stats.totalStaffCost || '0.00' }}</span>
              <span class="stat-label">员工提成支出</span>
            </div>
          </div>
        </el-col>
        <el-col :xs="12" :sm="6">
          <div class="stat-card glass-card salary-card">
            <div class="stat-icon">📋</div>
            <div class="stat-data">
              <span class="stat-value">¥{{ stats.totalBaseSalary || '0.00' }}</span>
              <span class="stat-label">月底薪支出</span>
            </div>
          </div>
        </el-col>
      </template>
      <!-- 店员视角 -->
      <template v-else>
        <el-col :xs="12" :sm="6">
          <div class="stat-card glass-card income-card">
            <div class="stat-icon">💎</div>
            <div class="stat-data">
              <span class="stat-value">¥{{ stats.totalIncome || '0.00' }}</span>
              <span class="stat-label">{{ stats.salaryModeLabel || '月总收益' }}</span>
            </div>
          </div>
        </el-col>
        <el-col :xs="12" :sm="6">
          <div class="stat-card glass-card base-card">
            <div class="stat-icon">⏰</div>
            <div class="stat-data">
              <span class="stat-value">{{ stats.monthHours || 0 }}h</span>
              <span class="stat-label">{{ stats.salaryMode === 1 ? '本月工时' : '提成收入' }}</span>
            </div>
          </div>
        </el-col>
        <el-col :xs="12" :sm="6">
          <div class="stat-card glass-card commission-card">
            <div class="stat-icon">⭐</div>
            <div class="stat-data">
              <span class="stat-value">¥{{ stats.salaryMode === 1 ? (stats.hourlyRate || 18) : (stats.totalCommission || '0.00') }}</span>
              <span class="stat-label">{{ stats.salaryMode === 1 ? '正常时薪' : '提成收入' }}</span>
            </div>
          </div>
        </el-col>
        <el-col :xs="12" :sm="6">
          <div class="stat-card glass-card rate-card">
            <div class="stat-icon">📌</div>
            <div class="stat-data">
              <span class="stat-value">¥{{ stats.salaryMode === 1 ? (stats.overtimeRate || 25) : (stats.commissionRate || 0) }}</span>
              <span class="stat-label">{{ stats.salaryMode === 1 ? '加班时薪' : '每单提成' }}</span>
            </div>
          </div>
        </el-col>
      </template>
    </el-row>

    <!-- ====== 店长专属：收入分配面板 ====== -->
    <div v-if="isOwner" class="card allocate-section">
      <h3 class="section-title"><span>💵</span> 收入分配</h3>
      <p class="section-desc">选择已完成订单，为执行店员分配固定提成收入</p>

      <el-form :model="allocForm" label-width="90px" inline class="alloc-form">
        <el-form-item label="选择订单">
          <el-select v-model="allocForm.orderId" placeholder="选择待分配订单" style="width:240px" @change="onOrderSelect" filterable>
            <el-option v-for="o in unallocatedOrders" :key="o.id"
              :label="`订单#${o.id} ¥${o.totalAmount} ${o.orderNo||''}`" :value="o.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="执行员工">
          <el-select v-model="allocForm.staffId" placeholder="选择店员" style="width:160px" @change="onStaffSelect">
            <el-option v-for="s in staffList" :key="s.id"
              :label="`${s.realName || s.username} (提成¥${s.commissionRate||0})`" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="固定提成">
          <el-input-number v-model="allocForm.commission" :min="0" :precision="2" :step="5" style="width:140px" />
          <span style="color:var(--color-text-placeholder);margin-left:4px">元/单</span>
        </el-form-item>
        <el-button type="primary" round @click="handleAllocate" :loading="allocating">确认分配</el-button>
      </el-form>

      <!-- 分配预览 - 清晰展示计算过程 -->
      <div class="preview-box" v-if="allocPreview.visible">
        <div class="preview-title">📊 提成计算预览</div>
        <div class="calc-step">
          <div class="calc-row"><span class="calc-label">订单金额</span><span class="calc-value">¥{{ allocPreview.orderAmount }}</span></div>
          <div class="calc-row highlight"><span class="calc-label">员工提成（固定 ¥{{ allocPreview.commission }}/单）</span><span class="calc-value accent">+ ¥{{ allocPreview.staffShare }}</span></div>
          <div class="calc-row shop"><span class="calc-label">店铺收入（订单金额 - 提成）</span><span class="calc-value warn">= ¥{{ allocPreview.shopShare }}</span></div>
        </div>
        <div class="calc-formula">
          计算公式：店铺收入 = ¥{{ allocPreview.orderAmount }} - ¥{{ allocPreview.staffShare }} = ¥{{ allocPreview.shopShare }}
        </div>
      </div>
    </div>

    <!-- ====== 店长专属：员工薪资配置 ====== -->
    <div v-if="isOwner" class="card salary-section">
      <h3 class="section-title"><span>⚙️</span> 员工薪资方案配置</h3>
      <p class="section-desc">为每位店员设置薪资方案：提成制 / 时薪制（正常18元/h+加班25元/h）/ 固定月薪</p>
      <el-table :data="staffList" stripe v-if="staffList.length > 0" class="salary-table">
        <el-table-column label="员工" min-width="100">
          <template #default="{ row }">{{ row.realName || row.username }}</template>
        </el-table-column>
        <el-table-column label="薪资方案" width="150">
          <template #default="{ row }">
            <el-select v-model="row.salaryMode" size="small" style="width:130px" @change="onIncomeSalaryModeChange(row)">
              <el-option :value="0" label="提成制" />
              <el-option :value="1" label="时薪制" />
              <el-option :value="2" label="固定月薪" />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column label="方案参数" min-width="220">
          <template #default="{ row }">
            <div v-if="row.salaryMode === 0" style="display:flex;align-items:center;gap:6px">
              <span style="font-size:12px;color:var(--color-text-secondary)">每单提成</span>
              <el-input-number v-model="row.commissionRate" :min="0" :precision="2" :step="5" size="small" style="width:110px" />
            </div>
            <div v-else-if="row.salaryMode === 1" style="display:flex;align-items:center;gap:8px">
              <span style="font-size:12px;color:var(--color-text-secondary)">正常</span>
              <el-input-number v-model="row.hourlyRate" :min="0" :precision="2" size="small" style="width:100px" />
              <span style="font-size:12px;color:var(--color-text-secondary)">加班</span>
              <el-input-number v-model="row.overtimeRate" :min="0" :precision="2" size="small" style="width:100px" />
            </div>
            <div v-else-if="row.salaryMode === 2" style="display:flex;align-items:center;gap:6px">
              <span style="font-size:12px;color:var(--color-text-secondary)">月薪</span>
              <el-input-number v-model="row.fixedMonthlySalary" :min="0" :precision="2" :step="500" size="small" style="width:130px" />
            </div>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="80">
          <template #default="{ row }">
            <el-button type="primary" size="small" round @click="handleSaveSalary(row)">保存</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-else description="暂无店员，请先在用户管理中添加店员" :image-size="80" />
    </div>

    <!-- ====== 月度收益汇总 ====== -->
    <div class="card">
      <div class="page-header">
        <h3 class="section-title"><span>📅</span> 月度收益汇总</h3>
        <div class="month-picker">
          <el-date-picker v-model="summaryMonth" type="month" placeholder="选择月份" format="YYYY年MM月"
            value-format="YYYY-MM" @change="fetchMonthlySummary" style="width:160px" />
        </div>
      </div>

      <!-- 店员视角：个人汇总 -->
      <template v-if="!isOwner">
        <div v-for="s in monthlySummary" :key="s.staffId" class="summary-card glass-card">
          <div class="summary-header">
            <span class="summary-name">{{ s.staffName }}</span>
            <el-tag type="primary" round size="small">{{ s.year }}年{{ s.month }}月</el-tag>
            <el-tag :type="s.salaryMode === 1 ? 'primary' : s.salaryMode === 2 ? 'success' : 'warning'" round size="small" style="margin-left:6px">{{ s.salaryModeLabel || '提成制' }}</el-tag>
          </div>
          <div class="summary-calc-box">
            <div class="calc-step-detail">
              <!-- 时薪制 -->
              <template v-if="s.salaryMode === 1">
                <div class="step-row">
                  <span class="step-label">① 正常工时 {{ s.monthHours || 0 }}h × ¥{{ s.hourlyRate || 18 }}/h</span>
                  <span class="step-value">¥{{ ((s.monthHours || 0) * (s.hourlyRate || 18)).toFixed(2) }}</span>
                </div>
                <div class="step-row">
                  <span class="step-label">② 加班工时（超出8h/天部分）</span>
                  <span class="step-value">¥{{ ((s.totalIncome || 0) - (s.monthHours || 0) * (s.hourlyRate || 18)).toFixed(2) }}</span>
                </div>
              </template>
              <!-- 固定月薪 -->
              <template v-else-if="s.salaryMode === 2">
                <div class="step-row">
                  <span class="step-label">固定月薪</span>
                  <span class="step-value">¥{{ s.fixedMonthlySalary || 0 }}</span>
                </div>
              </template>
              <!-- 提成制 -->
              <template v-else>
                <div class="step-row">
                  <span class="step-label">① 提成收入（{{ s.orderCount }}单 × ¥{{ s.commissionRate }}/单）</span>
                  <span class="step-value">¥{{ s.totalCommission }}</span>
                </div>
              </template>
              <div class="step-row total">
                <span class="step-label">月总收益</span>
                <span class="step-value total-value">¥{{ s.totalIncome }}</span>
              </div>
            </div>
          </div>
        </div>
      </template>

      <!-- 店长视角：所有员工汇总 -->
      <template v-else>
        <div v-for="s in monthlySummary" :key="s.staffId" class="summary-card glass-card">
          <div class="summary-header">
            <span class="summary-name">{{ s.staffName }}</span>
            <el-tag :type="s.orderCount > 0 ? 'primary' : 'info'" round size="small">{{ s.year }}年{{ s.month }}月</el-tag>
            <el-tag :type="s.salaryMode === 1 ? 'primary' : s.salaryMode === 2 ? 'success' : 'warning'" round size="small" style="margin-left:6px">{{ s.salaryModeLabel || '提成制' }}</el-tag>
          </div>
          <div class="summary-calc-box">
            <div class="calc-step-detail">
              <template v-if="s.salaryMode === 1">
                <div class="step-row">
                  <span class="step-label">工时</span>
                  <span class="step-value">{{ s.monthHours || 0 }}h</span>
                </div>
                <div class="step-row">
                  <span class="step-label">正常时薪 / 加班时薪</span>
                  <span class="step-value">¥{{ s.hourlyRate || 18 }} / ¥{{ s.overtimeRate || 25 }}</span>
                </div>
              </template>
              <template v-else-if="s.salaryMode === 2">
                <div class="step-row">
                  <span class="step-label">固定月薪</span>
                  <span class="step-value">¥{{ s.fixedMonthlySalary || 0 }}</span>
                </div>
              </template>
              <template v-else>
                <div class="step-row">
                  <span class="step-label">提成（{{ s.orderCount }}单 × ¥{{ s.commissionRate }}/单）</span>
                  <span class="step-value">¥{{ s.totalCommission }}</span>
                </div>
              </template>
              <div class="step-row">
                <span class="step-label">已结算</span>
                <span class="step-value settled">¥{{ s.settledCommission || '0.00' }}</span>
              </div>
              <div class="step-row total">
                <span class="step-label">月总收益</span>
                <span class="step-value total-value">¥{{ s.totalIncome }}</span>
              </div>
            </div>
          </div>
        </div>
        <el-empty v-if="monthlySummary.length === 0" description="暂无数据" :image-size="80" />
      </template>
    </div>

    <!-- ====== 收入记录表格 ====== -->
    <div class="card">
      <div class="page-header">
        <h3 class="section-title"><span>📊</span> 提成记录明细</h3>
        <el-button v-if="isOwner && selectedIds.length > 0" type="success" size="small" round @click="handleBatchSettle">
          批量结算 ({{ selectedIds.length }})
        </el-button>
      </div>

      <el-table :data="records" stripe v-loading="tableLoading" @selection-change="(rows)=>selectedIds=rows.map(r=>r.id)" class="record-table">
        <el-table-column v-if="isOwner" type="selection" width="45" />
        <el-table-column type="index" label="#" width="50" />
        <el-table-column label="订单号" min-width="110">
          <template #default="{ row }">#{{ row.orderId }}</template>
        </el-table-column>
        <el-table-column label="订单金额" width="100">
          <template #default="{ row }"><span class="amount">¥{{ row.orderAmount }}</span></template>
        </el-table-column>
        <el-table-column label="固定提成" width="100">
          <template #default="{ row }">
            <span class="commission-tag">¥{{ row.commissionPerOrder || row.staffShare || '0.00' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="店铺收入" width="100" v-if="isOwner">
          <template #default="{ row }">¥{{ row.shopShare || '0.00' }}</template>
        </el-table-column>
        <el-table-column v-if="isOwner" label="执行员工" width="100">
          <template #default="{ row }">
            <span v-if="row.calcDetail">{{ parseCalcDetail(row.calcDetail).staffName || '-' }}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" size="small" round>{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="计算明细" width="80">
          <template #default="{ row }">
            <el-popover trigger="hover" :width="320" v-if="row.calcDetail">
              <template #reference>
                <el-button text type="primary" size="small">查看</el-button>
              </template>
              <div class="calc-popover">
                <div class="calc-pop-title">📐 提成计算过程</div>
                <div v-if="parseCalcDetail(row.calcDetail).formula" class="calc-pop-row">
                  <span class="calc-pop-label">员工收益</span>
                  <span class="calc-pop-value">{{ parseCalcDetail(row.calcDetail).formula }}</span>
                </div>
                <div v-if="parseCalcDetail(row.calcDetail).shopFormula" class="calc-pop-row">
                  <span class="calc-pop-label">店铺收入</span>
                  <span class="calc-pop-value">{{ parseCalcDetail(row.calcDetail).shopFormula }}</span>
                </div>
                <div class="calc-pop-row result">
                  <span class="calc-pop-label">员工提成</span>
                  <span class="calc-pop-value accent">¥{{ row.staffShare }}</span>
                </div>
                <div class="calc-pop-row result">
                  <span class="calc-pop-label">店铺收入</span>
                  <span class="calc-pop-value warn">¥{{ row.shopShare }}</span>
                </div>
              </div>
            </el-popover>
            <span v-else style="color:var(--color-text-placeholder)">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="时间" width="160">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!tableLoading && records.length === 0" description="暂无提成记录" />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getIncomeStats, getIncomeRecords, allocateIncome, batchSettle, getStaffList, updateStaffSalary, getMonthlySummary, getUnallocatedOrders } from '@/api/income'

const userStore = useUserStore()
const tableLoading = ref(false)
const records = ref([])
const stats = ref({})
const selectedIds = ref([])
const allocating = ref(false)
const staffList = ref([])
const unallocatedOrders = ref([])
const monthlySummary = ref([])
const summaryMonth = ref(new Date().toISOString().slice(0, 7))

const allocForm = ref({ orderId: null, staffId: null, commission: 0 })
const allocPreview = ref({ visible: false, orderAmount: 0, commission: 0, staffShare: 0, shopShare: 0 })

const isOwner = computed(() => {
  const r = userStore.userInfo.role
  if (r === 2) return true
  const sr = userStore.userInfo.staffRole
  return r === 1 && (sr === undefined || sr === null || sr === 1)
})

function statusTagType(s) { return s == 0 ? 'warning' : s == 1 ? 'primary' : 'success' }
function statusText(s) { return s == 0 ? '待分配' : s == 1 ? '已分配' : '已结算' }
function formatTime(t) { return t ? new Date(t).toLocaleString() : '-' }
function parseCalcDetail(str) {
  try { return typeof str === 'string' ? JSON.parse(str) : {} }
  catch { return {} }
}

onMounted(async () => {
  tableLoading.value = true
  try {
    const [statsRes, recRes] = await Promise.all([getIncomeStats(), getIncomeRecords({ page: 1, size: 50 })])
    stats.value = statsRes.data
    records.value = recRes.data.records || []

    if (isOwner.value) {
      try {
        const [stRes, ordersRes] = await Promise.all([getStaffList(), getUnallocatedOrders()])
        console.log('staffList raw:', stRes)
        console.log('ordersRes raw:', ordersRes)
        staffList.value = stRes.data || []
        unallocatedOrders.value = ordersRes.data || []
      } catch (e) {
        console.error('加载员工列表/待分配订单失败:', e)
      }
    }

    await fetchMonthlySummary()
  } finally { tableLoading.value = false }
})

async function fetchMonthlySummary() {
  try {
    const [year, month] = (summaryMonth.value || '').split('-').map(Number)
    const res = await getMonthlySummary({ year, month })
    monthlySummary.value = Array.isArray(res.data) ? res.data : [res.data]
  } catch (e) {
    console.error('加载月度汇总失败:', e)
  }
}

function onOrderSelect(orderId) {
  const order = unallocatedOrders.value.find(o => o.id === orderId)
  if (!order) { allocPreview.value.visible = false; return }
  updateAllocPreview(order.totalAmount)
}

function onStaffSelect(staffId) {
  const staff = staffList.value.find(s => s.id === staffId)
  if (staff && staff.commissionRate && !allocForm.value.commission) {
    allocForm.value.commission = staff.commissionRate
  }
  if (allocForm.value.orderId) {
    const order = unallocatedOrders.value.find(o => o.id === allocForm.value.orderId)
    if (order) updateAllocPreview(order.totalAmount)
  }
}

function updateAllocPreview(orderAmount) {
  const commission = allocForm.value.commission || 0
  const staffShare = commission
  const shopShare = Math.max(0, orderAmount - staffShare).toFixed(2)
  allocPreview.value = { visible: true, orderAmount, commission, staffShare: staffShare.toFixed(2), shopShare }
}

watch(() => allocForm.value.commission, () => {
  if (allocForm.value.orderId) {
    const order = unallocatedOrders.value.find(o => o.id === allocForm.value.orderId)
    if (order) updateAllocPreview(order.totalAmount)
  }
})

async function handleAllocate() {
  if (!allocForm.value.orderId || !allocForm.value.staffId) return ElMessage.warning('请选择订单和员工')
  if (!allocForm.value.commission || allocForm.value.commission <= 0) return ElMessage.warning('请输入有效的提成金额')

  allocating.value = true
  try {
    await allocateIncome({
      orderId: allocForm.value.orderId,
      staffId: allocForm.value.staffId,
      commissionPerOrder: allocForm.value.commission,
      remark: ''
    })
    ElMessage.success('分配成功！员工将获得 ¥' + allocForm.value.commission + ' 提成')
    allocForm.value = { orderId: null, staffId: null, commission: 0 }
    allocPreview.value.visible = false
    // 刷新数据
    const [recRes, ordersRes] = await Promise.all([getIncomeRecords({ page: 1, size: 50 }), getUnallocatedOrders()])
    records.value = recRes.data.records || []
    unallocatedOrders.value = ordersRes.data || []
    const sRes = await getIncomeStats()
    stats.value = sRes.data
    await fetchMonthlySummary()
  } catch (e) { console.error('分配失败:', e) } finally { allocating.value = false }
}

async function handleSaveSalary(staff) {
  try {
    await updateStaffSalary({
      staffId: staff.id,
      salaryMode: staff.salaryMode ?? 0,
      commissionRate: staff.salaryMode === 0 ? (staff.commissionRate || 0) : undefined,
      hourlyRate: staff.salaryMode === 1 ? (staff.hourlyRate || 18) : undefined,
      overtimeRate: staff.salaryMode === 1 ? (staff.overtimeRate || 25) : undefined,
      fixedMonthlySalary: staff.salaryMode === 2 ? (staff.fixedMonthlySalary || 3000) : undefined
    })
    ElMessage.success(`已更新 ${(staff.realName || staff.username)} 的薪资方案`)
  } catch (e) { console.error('保存薪资失败:', e) }
}

function onIncomeSalaryModeChange(row) {
  if (row.salaryMode === 1) {
    row.hourlyRate = row.hourlyRate || 18
    row.overtimeRate = row.overtimeRate || 25
  } else if (row.salaryMode === 2) {
    row.fixedMonthlySalary = row.fixedMonthlySalary || 3000
  }
}

async function handleBatchSettle() {
  ElMessageBox.confirm(`确认将 ${selectedIds.value.length} 条记录标记为已结算？`, '批量结算', { type: 'warning' }).then(async () => {
    try {
      await batchSettle(selectedIds.value)
      ElMessage.success(`成功结算 ${selectedIds.value.length} 条`)
      selectedIds.value = []
      const res = await getIncomeRecords({ page: 1, size: 50 })
      records.value = res.data.records || []
      const sRes = await getIncomeStats()
      stats.value = sRes.data
      await fetchMonthlySummary()
    } catch (e) { console.error('批量结算失败:', e) }
  }).catch(() => {})
}
</script>

<style scoped>
.perm-banner {
  display: flex; align-items: center; gap: 14px;
  padding: 14px 20px; border-radius: var(--radius-md);
  margin-bottom: 20px; font-size: 14px;
}
.perm-banner.owner {
  background: linear-gradient(135deg, rgba(255,140,66,0.08), rgba(255,209,102,0.06));
  border: 1.5px solid rgba(255,140,66,0.15);
}
.perm-banner.staff {
  background: linear-gradient(135deg, rgba(108,158,191,0.06), rgba(156,195,230,0.04));
  border: 1.5px solid rgba(108,158,191,0.1);
}
.perm-icon { font-size: 28px; }
.perm-info strong { display: block; color: var(--color-text); margin-bottom: 2px; }
.perm-info p { color: var(--color-text-secondary); margin: 0; font-size: 13px; }

/* 统计卡片 */
.stats-row { margin-bottom: 20px; }
.stat-card {
  display: flex; align-items: center; gap: 14px;
  padding: 20px; border-radius: var(--radius-lg);
}
.stat-icon { font-size: 30px; }
.stat-data { display: flex; flex-direction: column; gap: 2px; }
.stat-value { font-family: 'Poppins'; font-size: 22px; font-weight: 800; color: var(--color-text); }
.stat-label { font-size: 12px; color: var(--color-text-placeholder); }

.revenue-card { background: linear-gradient(135deg, rgba(255,215,0,0.12), rgba(255,243,205,0.08)); border: 1.5px solid rgba(255,180,0,0.15); }
.shop-card { background: linear-gradient(135deg, rgba(152,221,202,0.1), rgba(198,239,206,0.06)); border: 1.5px solid rgba(76,175,80,0.1); }
.staff-cost-card { background: linear-gradient(135deg, rgba(249,168,212,0.1), rgba(253,186,240,0.06)); border: 1.5px solid rgba(245,108,108,0.1); }
.salary-card { background: linear-gradient(135deg, rgba(108,158,191,0.08), rgba(156,195,230,0.05)); border: 1.5px solid rgba(108,158,191,0.1); }

.income-card { background: linear-gradient(135deg, rgba(255,215,0,0.12), rgba(255,243,205,0.08)); border: 1.5px solid rgba(255,180,0,0.15); }
.base-card { background: linear-gradient(135deg, rgba(152,221,202,0.1), rgba(198,239,206,0.06)); border: 1.5px solid rgba(76,175,80,0.1); }
.commission-card { background: linear-gradient(135deg, rgba(255,140,66,0.1), rgba(255,193,7,0.06)); border: 1.5px solid rgba(255,140,66,0.12); }
.rate-card { background: linear-gradient(135deg, rgba(108,158,191,0.06), rgba(156,195,230,0.04)); border: 1.5px solid rgba(108,158,191,0.1); }

.section-title { font-size: 17px; font-weight: 700; margin-bottom: 4px; display: flex; align-items: center; gap: 8px; }
.section-desc { color: var(--color-text-secondary); font-size: 13px; margin-bottom: 16px; }
.page-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 16px; }

/* 分配面板 */
.allocate-section { margin-bottom: 20px; }
.alloc-form { margin-bottom: 8px; }
.preview-box {
  margin-top: 16px; padding: 18px 22px; border-radius: var(--radius-md);
  background: linear-gradient(135deg, rgba(255,248,230,0.8), rgba(255,243,224,0.5));
  border: 1.5px solid rgba(255,180,0,0.12);
}
.preview-title { font-size: 15px; font-weight: 700; margin-bottom: 12px; color: var(--color-text); }
.calc-row { display: flex; justify-content: space-between; padding: 6px 0; font-size: 14px; }
.calc-row span:first-child { color: var(--color-text-secondary); }
.calc-row.highlight .calc-value { color: var(--color-primary); font-size: 16px; font-weight: 700; }
.calc-row.shop .calc-value { color: #E6A23C; font-size: 15px; font-weight: 600; }
.calc-formula {
  margin-top: 10px; padding-top: 10px; border-top: 1px dashed var(--color-border);
  font-size: 12px; color: var(--color-text-secondary); font-style: italic;
}

/* 薪资配置 */
.salary-section { margin-bottom: 20px; }
.salary-table { margin-top: 8px; }

/* 月度汇总 */
.month-picker { margin-bottom: 0; }
.summary-card {
  padding: 18px 22px; margin-bottom: 14px; border-radius: var(--radius-lg);
}
.summary-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 12px; }
.summary-name { font-size: 16px; font-weight: 700; color: var(--color-text); }

.calc-step-detail { width: 100%; }
.step-row {
  display: flex; justify-content: space-between; align-items: center;
  padding: 8px 0; font-size: 14px;
  border-bottom: 1px solid rgba(0,0,0,0.04);
}
.step-row:last-child { border-bottom: none; }
.step-label { color: var(--color-text-secondary); }
.step-value { font-weight: 600; color: var(--color-text); font-family: 'Poppins'; }
.step-value.settled { color: #67C23A; }
.step-row.total {
  border-top: 2px solid rgba(255,140,66,0.2);
  margin-top: 4px; padding-top: 10px;
  border-bottom: none;
}
.total-value { font-size: 20px; font-weight: 800; color: var(--color-primary) !important; }

.calc-formula-text {
  margin-top: 8px; padding: 10px 14px; border-radius: var(--radius-sm);
  background: rgba(255,140,66,0.05); border: 1px dashed rgba(255,140,66,0.15);
  font-size: 12px; color: var(--color-text-secondary); line-height: 1.6;
  font-family: 'Poppins', monospace;
}

/* 记录表格 */
.record-table { margin-top: 8px; }
.commission-tag { color: var(--color-primary); font-weight: 700; }
.amount { color: var(--color-text); font-weight: 600; }

/* 计算明细 Popover */
.calc-popover { padding: 4px 0; }
.calc-pop-title { font-size: 14px; font-weight: 700; margin-bottom: 10px; color: var(--color-text); }
.calc-pop-row { display: flex; justify-content: space-between; padding: 5px 0; font-size: 13px; }
.calc-pop-label { color: var(--color-text-secondary); }
.calc-pop-value { font-weight: 500; color: var(--color-text); max-width: 180px; text-align: right; }
.calc-pop-value.accent { color: var(--color-primary); font-weight: 700; }
.calc-pop-value.warn { color: #E6A23C; font-weight: 700; }
.calc-pop-row.result { border-top: 1px solid #f0f0f0; margin-top: 4px; padding-top: 8px; }

@media (max-width: 768px) {
  .stats-row .el-col { margin-bottom: 10px; }
  .alloc-form :deep(.el-form-item) { display: block; margin-bottom: 12px; }
}
</style>
