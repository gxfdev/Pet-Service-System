<template>
  <div class="page-container">
    <!-- 顶部概览 -->
    <div class="shop-overview glass-card">
      <div class="overview-left">
        <span class="overview-icon">🏪</span>
        <div>
          <h3>店铺管理中心</h3>
          <p>管理您的店员团队与客户资源，配置薪资体系</p>
        </div>
      </div>
      <div class="overview-stats">
        <div class="overview-stat">
          <strong>{{ summary.staffCount || 0 }}</strong>
          <span>在岗店员</span>
        </div>
        <div class="overview-stat">
          <strong>{{ users.filter(u => u.role === 0).length }}</strong>
          <span>注册客户</span>
        </div>
      </div>
    </div>

    <!-- 标签切换 -->
    <el-tabs v-model="activeTab" class="main-tabs">
      <!-- ====== 员工管理 ====== -->
      <el-tab-pane label="👥 员工管理" name="staff">
        <div class="tab-toolbar">
          <div class="toolbar-left">
            <el-input v-model="staffKeyword" placeholder="搜索员工姓名/手机号/用户名" clearable style="width:220px"
              :prefix-icon="Search" @input="fetchStaffList" />
            <el-select v-model="statusFilter" placeholder="状态筛选" clearable style="width:120px" @change="fetchStaffList">
              <el-option label="正常" :value="1" />
              <el-option label="已禁用" :value="0" />
            </el-select>
          </div>
          <div class="toolbar-right">
            <el-button type="primary" round @click="showAddStaffDialog = true" v-if="isOwner">+ 添加店员</el-button>
            <el-tag v-else type="info" effect="plain" round size="small">仅店长可添加员工</el-tag>
          </div>
        </div>

        <el-table :data="staffList" stripe v-loading="loading" class="user-table">
          <el-table-column type="index" label="#" width="50" />
          <el-table-column label="员工信息" min-width="200">
            <template #default="{ row }">
              <div class="user-info-cell">
                <el-avatar :size="36" :style="{ background: avatarColor(row) }">{{ (row.realName || row.username).charAt(0) }}</el-avatar>
                <div class="user-info-text">
                  <strong>{{ row.realName || row.username }}</strong>
                  <span>@{{ row.username }} | {{ row.phone }}</span>
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="薪资方案" width="120">
            <template #default="{ row }">
              <el-tag :type="row.salaryMode === 1 ? 'primary' : row.salaryMode === 2 ? 'success' : 'warning'" size="small" round>
                {{ row.salaryMode === 1 ? '时薪制' : row.salaryMode === 2 ? '固定月薪' : '提成制' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="薪资详情" width="160">
            <template #default="{ row }">
              <span v-if="row.salaryMode === 1" class="salary-text">¥{{ row.hourlyRate || 18 }}/h</span>
              <span v-else-if="row.salaryMode === 2" class="salary-text">¥{{ row.fixedMonthlySalary || '0' }}/月</span>
              <span v-else class="commission-text">¥{{ row.commissionRate || '0' }}/单</span>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="90">
            <template #default="{ row }">
              <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small" effect="light" round>
                {{ row.status === 1 ? '正常' : '禁用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="入职时间" width="160">
            <template #default="{ row }">{{ formatDate(row.createTime) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="260">
            <template #default="{ row }">
              <el-button type="primary" size="small" link @click="openSalaryEdit(row)">薪资</el-button>
              <el-button type="warning" size="small" link @click="toggleUserStatus(row)">
                {{ row.status === 1 ? '禁用' : '启用' }}
              </el-button>
              <el-popconfirm title="确认删除此员工？删除后将无法恢复" confirm-button-text="确认" cancel-button-text="取消" @confirm="handleDelete(row.id)">
                <template #reference><el-button type="danger" size="small" link>删除</el-button></template>
              </el-popconfirm>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-if="!loading && staffList.length === 0" description="暂无员工，点击上方按钮添加" :image-size="100" />

        <!-- 分页 -->
        <div class="pagination-wrap" v-if="staffTotal > pageSize">
          <el-pagination background layout="prev, pager, next" :total="staffTotal" v-model:current-page="staffPage" :page-size="pageSize" @change="fetchStaffList" />
        </div>
      </el-tab-pane>

      <!-- ====== 客户管理 ====== -->
      <el-tab-pane label="🤝 客户管理" name="customer">
        <div class="tab-toolbar">
          <div class="toolbar-left">
            <el-input v-model="customerKeyword" placeholder="搜索客户姓名/手机号" clearable style="width:220px"
              :prefix-icon="Search" @input="fetchCustomerList" />
          </div>
          <div class="toolbar-right">
            <el-button type="success" round @click="showAddCustomerDialog = true" v-if="isOwner">+ 添加客户</el-button>
          </div>
        </div>

        <el-table :data="customerList" stripe v-loading="customerLoading" class="user-table">
          <el-table-column type="index" label="#" width="50" />
          <el-table-column label="客户信息" min-width="200">
            <template #default="{ row }">
              <div class="user-info-cell">
                <el-avatar :size="36" style="background: linear-gradient(135deg,#667eea,#764ba2)">{{ (row.realName || row.username).charAt(0) }}</el-avatar>
                <div class="user-info-text">
                  <strong>{{ row.realName || row.username }}</strong>
                  <span>{{ row.phone }}</span>
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="phone" label="手机号" width="130" />
          <el-table-column label="状态" width="90">
            <template #default="{ row }">
              <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small" effect="light" round>
                {{ row.status === 1 ? '正常' : '禁用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="注册时间" width="160">
            <template #default="{ row }">{{ formatDate(row.createTime) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="180">
            <template #default="{ row }">
              <el-button type="warning" size="small" link @click="toggleUserStatus(row)">
                {{ row.status === 1 ? '禁用' : '启用' }}
              </el-button>
              <el-popconfirm title="确认删除此客户？" @confirm="handleDelete(row.id)">
                <template #reference><el-button type="danger" size="small" link>删除</el-button></template>
              </el-popconfirm>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-if="!customerLoading && customerList.length === 0" description="暂无客户数据" :image-size="80" />
      </el-tab-pane>

      <!-- ====== 薪资配置（双方案） ====== -->
      <el-tab-pane label="💰 薪资配置" name="salary">
        <p class="section-desc">为每位店员选择薪资方案：提成制 / 时薪制 / 固定月薪</p>
        <el-table :data="allStaffForSalary" stripe v-loading="loading" class="salary-config-table">
          <el-table-column label="员工" min-width="120">
            <template #default="{ row }"><strong>{{ row.realName || row.username }}</strong></template>
          </el-table-column>
          <el-table-column label="薪资方案" width="180">
            <template #default="{ row }">
              <el-select v-model="row.salaryMode" size="small" style="width:150px" @change="onSalaryModeChange(row)">
                <el-option :value="0" label="提成制" />
                <el-option :value="1" label="时薪制" />
                <el-option :value="2" label="固定月薪" />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column label="方案参数" min-width="280">
            <template #default="{ row }">
              <!-- 提成制 -->
              <div v-if="row.salaryMode === 0" class="salary-fields">
                <div class="field-item"><span class="field-label">每单提成</span>
                  <el-input-number v-model="row.commissionRate" :min="0" :precision="2" :step="5" size="small" style="width:120px" /></div>
              </div>
              <!-- 时薪制 -->
              <div v-else-if="row.salaryMode === 1" class="salary-fields">
                <div class="field-item"><span class="field-label">正常时薪</span>
                  <el-input-number v-model="row.hourlyRate" :min="0" :precision="2" size="small" style="width:110px" /></div>
                <div class="field-item"><span class="field-label">加班时薪</span>
                  <el-input-number v-model="row.overtimeRate" :min="0" :precision="2" size="small" style="width:110px" /></div>
              </div>
              <!-- 固定月薪 -->
              <div v-else-if="row.salaryMode === 2" class="salary-fields">
                <div class="field-item"><span class="field-label">固定月薪</span>
                  <el-input-number v-model="row.fixedMonthlySalary" :min="0" :precision="2" :step="500" size="small" style="width:140px" /></div>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="方案说明" width="180">
            <template #default="{ row }">
              <el-tag v-if="row.salaryMode === 0" type="warning" size="small" round>按单提成</el-tag>
              <el-tag v-else-if="row.salaryMode === 1" type="primary" size="small" round>¥{{ row.hourlyRate || 18 }}/h + 加班¥{{ row.overtimeRate || 25 }}/h</el-tag>
              <el-tag v-else type="success" size="small" round>月固定 ¥{{ row.fixedMonthlySalary || 0 }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="100" v-if="isOwner">
            <template #default="{ row }">
              <el-button type="primary" size="small" round @click="handleSaveSalaryRow(row)">保存</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-if="!loading && allStaffForSalary.length === 0" description="暂无员工" :image-size="80" />
      </el-tab-pane>
    </el-tabs>

    <!-- 添加店员对话框 -->
    <el-dialog v-model="showAddStaffDialog" title="添加新店员" width="480px" destroy-on-close>
      <el-form :model="addForm" :rules="addRules" ref="addFormRef" label-width="90px">
        <el-form-item label="用户名" prop="username"><el-input v-model="addForm.username" placeholder="用于登录的用户名" /></el-form-item>
        <el-form-item label="密码" prop="password"><el-input v-model="addForm.password" type="password" placeholder="留空则默认 123456" show-password /></el-form-item>
        <el-form-item label="手机号" prop="phone"><el-input v-model="addForm.phone" placeholder="11位手机号" /></el-form-item>
        <el-form-item label="真实姓名" prop="realName"><el-input v-model="addForm.realName" placeholder="员工真实姓名" /></el-form-item>
        <el-divider content-position="left">薪资方案（后续可调整）</el-divider>
        <el-form-item label="薪资方案">
          <el-radio-group v-model="addForm.salaryMode" @change="onAddSalaryModeChange">
            <el-radio :value="0">提成制</el-radio>
            <el-radio :value="1">时薪制</el-radio>
            <el-radio :value="2">固定月薪</el-radio>
          </el-radio-group>
        </el-form-item>
        <template v-if="addForm.salaryMode === 0">
          <el-form-item label="每单提成"><el-input-number v-model="addForm.commissionRate" :min="0" :precision="2" :step="5" style="width:200px" /><span style="margin-left:8px;color:var(--color-text-placeholder)">元/单</span></el-form-item>
        </template>
        <template v-if="addForm.salaryMode === 1">
          <el-form-item label="正常时薪"><el-input-number v-model="addForm.hourlyRate" :min="0" :precision="2" style="width:200px" /><span style="margin-left:8px;color:var(--color-text-placeholder)">元/时</span></el-form-item>
          <el-form-item label="加班时薪"><el-input-number v-model="addForm.overtimeRate" :min="0" :precision="2" style="width:200px" /><span style="margin-left:8px;color:var(--color-text-placeholder)">元/时</span></el-form-item>
        </template>
        <template v-if="addForm.salaryMode === 2">
          <el-form-item label="固定月薪"><el-input-number v-model="addForm.fixedMonthlySalary" :min="0" :precision="2" :step="500" style="width:200px" /><span style="margin-left:8px;color:var(--color-text-placeholder)">元/月</span></el-form-item>
        </template>
      </el-form>
      <template #footer>
        <el-button @click="showAddStaffDialog = false">取消</el-button>
        <el-button type="primary" @click="handleAddStaff" :loading="adding">确认添加</el-button>
      </template>
    </el-dialog>

    <!-- 添加客户对话框 -->
    <el-dialog v-model="showAddCustomerDialog" title="添加新客户" width="440px" destroy-on-close>
      <el-form :model="customerForm" :rules="customerRules" ref="customerFormRef" label-width="90px">
        <el-form-item label="用户名" prop="username"><el-input v-model="customerForm.username" placeholder="用于登录" /></el-form-item>
        <el-form-item label="密码" prop="password"><el-input v-model="customerForm.password" type="password" placeholder="留空默认123456" show-password /></el-form-item>
        <el-form-item label="手机号" prop="phone"><el-input v-model="customerForm.phone" placeholder="11位手机号" /></el-form-item>
        <el-form-item label="真实姓名" prop="realName"><el-input v-model="customerForm.realName" placeholder="客户姓名" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddCustomerDialog = false">取消</el-button>
        <el-button type="success" @click="handleAddCustomer" :loading="adding">确认添加</el-button>
      </template>
    </el-dialog>

    <!-- 快速编辑薪资对话框 -->
    <el-dialog v-model="showSalaryDialog" title="编辑薪资方案" width="480px" destroy-on-close>
      <el-form :model="salaryForm" label-width="90px">
        <el-form-item label="员工"><strong>{{ editingStaff?.realName || editingStaff?.username }}</strong></el-form-item>
        <el-form-item label="薪资方案">
          <el-radio-group v-model="salaryForm.salaryMode" @change="onDialogSalaryModeChange">
            <el-radio :value="0">提成制</el-radio>
            <el-radio :value="1">时薪制</el-radio>
            <el-radio :value="2">固定月薪</el-radio>
          </el-radio-group>
        </el-form-item>
        <template v-if="salaryForm.salaryMode === 0">
          <el-form-item label="每单提成"><el-input-number v-model="salaryForm.commissionRate" :min="0" :precision="2" :step="5" style="width:220px" /><span style="margin-left:6px">元/单</span></el-form-item>
        </template>
        <template v-if="salaryForm.salaryMode === 1">
          <el-form-item label="正常时薪"><el-input-number v-model="salaryForm.hourlyRate" :min="0" :precision="2" style="width:220px" /><span style="margin-left:6px">元/时</span></el-form-item>
          <el-form-item label="加班时薪"><el-input-number v-model="salaryForm.overtimeRate" :min="0" :precision="2" style="width:220px" /><span style="margin-left:6px">元/时</span></el-form-item>
        </template>
        <template v-if="salaryForm.salaryMode === 2">
          <el-form-item label="固定月薪"><el-input-number v-model="salaryForm.fixedMonthlySalary" :min="0" :precision="2" :step="500" style="width:220px" /><span style="margin-left:6px">元/月</span></el-form-item>
        </template>
      </el-form>
      <template #footer>
        <el-button @click="showSalaryDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSaveSalaryFromDialog">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import {
  getShopUsers, getUsersSummary, addStaff, addCustomer,
  updateShopUser, deleteShopUser, setStaffSalary
} from '@/api/shop'

const userStore = useUserStore()
const isOwner = computed(() => {
  const r = userStore.userInfo.role
  return r === 2 || (r === 1 && (!userStore.userInfo.staffRole || userStore.userInfo.staffRole === 1))
})

const loading = ref(false)
const customerLoading = ref(false)
const adding = ref(false)
const activeTab = ref('staff')
const summary = ref({})
const users = ref([])
const staffPage = ref(1)
const customerPage = ref(1)
const pageSize = 20
const staffTotal = ref(0)

// 筛选
const staffKeyword = ref('')
const statusFilter = ref(null)
const customerKeyword = ref('')

// 对话框状态
const showAddStaffDialog = ref(false)
const showAddCustomerDialog = ref(false)
const showSalaryDialog = ref(false)
const editingStaff = ref(null)

// 表单
const addForm = ref({ username: '', password: '', phone: '', realName: '', salaryMode: 1, hourlyRate: 18, overtimeRate: 25, commissionRate: 15, fixedMonthlySalary: 3000 })
const customerForm = ref({ username: '', password: '', phone: '', realName: '' })
const salaryForm = ref({ salaryMode: 0, commissionRate: 0, hourlyRate: 18, overtimeRate: 25, fixedMonthlySalary: 3000 })

const addRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  phone: [{ required: true, pattern: /^1[3-9]\d{9}$/, message: '请输入正确手机号', trigger: 'blur' }]
}
const customerRules = addRules

// 计算属性：过滤出员工列表
const staffList = computed(() => users.value.filter(u => u.staffRole === 2))
// 计算属性：过滤出客户列表
const customers = ref([])
const customerList = computed(() => customers.value)
// 薪资配置用：所有员工
const allStaffForSalary = computed(() => [...staffList.value])

function avatarColor(row) {
  const colors = ['linear-gradient(135deg,#FF8C42,#FFB347)', 'linear-gradient(135deg,#98DDCA,#79BD9A)',
    'linear-gradient(135deg,#A8E6CF,#88D8B0)', 'linear-gradient(135deg,#FFD1DC,#FFB6C1)']
  return colors[(row.id || 0) % colors.length]
}

function formatDate(t) { return t ? new Date(t).toLocaleDateString() : '-' }

async function fetchAllData() {
  loading.value = true
  try {
    const [sumRes] = await Promise.all([getUsersSummary()])
    summary.value = sumRes.data || {}
    await fetchStaffList()
  } finally { loading.value = false }
}

async function fetchStaffList() {
  try {
    const res = await getShopUsers({ page: staffPage.value, size: pageSize, keyword: staffKeyword.value || undefined })
    if (activeTab.value === 'staff') {
      // 只显示本页的员工
      if (res.data.records) {
        users.value = res.data.records
        staffTotal.value = res.data.total
      }
    }
  } catch (e) { console.error('获取员工列表失败:', e) }
}

async function fetchCustomerList() {
  customerLoading.value = true
  try {
    const res = await getShopUsers({ page: customerPage.value, size: 100, keyword: customerKeyword.value || undefined, roleFilter: 0 })
    customers.value = res.data?.records || []
  } catch (e) { console.error('获取客户列表失败:', e) } finally { customerLoading.value = false }
}

watch(activeTab, async (val) => {
  if (val === 'staff') await fetchStaffList()
  if (val === 'customer') await fetchCustomerList()
})

// 添加店员
async function handleAddStaff() {
  adding.value = true
  try {
    await addStaff(addForm.value)
    ElMessage.success(`成功添加店员 ${addForm.value.username}`)
    showAddStaffDialog.value = false
    addForm.value = { username: '', password: '', phone: '', realName: '', salaryMode: 1, hourlyRate: 18, overtimeRate: 25, commissionRate: 15, fixedMonthlySalary: 3000 }
    fetchStaffList()
    const s = await getUsersSummary(); summary.value = s.data || {}
  } catch (e) {} finally { adding.value = false }
}

function onAddSalaryModeChange() {
  if (addForm.value.salaryMode === 1) {
    addForm.value.hourlyRate = 18
    addForm.value.overtimeRate = 25
  } else if (addForm.value.salaryMode === 2) {
    addForm.value.fixedMonthlySalary = 3000
  } else {
    addForm.value.commissionRate = 15
  }
}

// 添加客户
async function handleAddCustomer() {
  adding.value = true
  try {
    await addCustomer(customerForm.value)
    ElMessage.success(`成功添加客户 ${customerForm.value.username}`)
    showAddCustomerDialog.value = false
    customerForm.value = { username: '', password: '', phone: '', realName: '' }
  } catch (e) {} finally { adding.value = false }
}

// 切换用户状态
async function toggleUserStatus(user) {
  const newStatus = user.status === 1 ? 0 : 1
  try {
    await updateShopUser(user.id, { status: newStatus })
    ElMessage.success(newStatus === 0 ? '已禁用' : '已启用')
    if (activeTab.value === 'staff') fetchStaffList()
  } catch (e) {}
}

// 删除用户
async function handleDelete(id) {
  try {
    await deleteShopUser(id)
    ElMessage.success('操作成功')
    if (activeTab.value === 'staff') fetchStaffList()
  } catch (e) {}
}

// 薪资方案变更时设置默认值
function onSalaryModeChange(row) {
  if (row.salaryMode === 1) {
    row.hourlyRate = row.hourlyRate || 18
    row.overtimeRate = row.overtimeRate || 25
  } else if (row.salaryMode === 2) {
    row.fixedMonthlySalary = row.fixedMonthlySalary || 3000
  }
}

function onDialogSalaryModeChange() {
  if (salaryForm.value.salaryMode === 1) {
    salaryForm.value.hourlyRate = salaryForm.value.hourlyRate || 18
    salaryForm.value.overtimeRate = salaryForm.value.overtimeRate || 25
  } else if (salaryForm.value.salaryMode === 2) {
    salaryForm.value.fixedMonthlySalary = salaryForm.value.fixedMonthlySalary || 3000
  }
}

// 打开薪资编辑弹窗
function openSalaryEdit(staff) {
  editingStaff.value = staff
  salaryForm.value = {
    salaryMode: staff.salaryMode ?? 0,
    commissionRate: staff.commissionRate || 0,
    hourlyRate: staff.hourlyRate || 18,
    overtimeRate: staff.overtimeRate || 25,
    fixedMonthlySalary: staff.fixedMonthlySalary || 3000
  }
  showSalaryDialog.value = true
}

// 从弹窗保存薪资
async function handleSaveSalaryFromDialog() {
  try {
    await setStaffSalary(editingStaff.value.id, salaryForm.value)
    ElMessage.success(`已更新 ${editingStaff.value.realName || editingStaff.value.username} 的薪资方案`)
    showSalaryDialog.value = false
    fetchStaffList()
  } catch (e) {}
}

// 表格行保存薪资
async function handleSaveSalaryRow(staff) {
  try {
    const data = {
      staffId: staff.id,
      salaryMode: staff.salaryMode ?? 0,
      commissionRate: staff.salaryMode === 0 ? (staff.commissionRate || 0) : undefined,
      hourlyRate: staff.salaryMode === 1 ? (staff.hourlyRate || 18) : undefined,
      overtimeRate: staff.salaryMode === 1 ? (staff.overtimeRate || 25) : undefined,
      fixedMonthlySalary: staff.salaryMode === 2 ? (staff.fixedMonthlySalary || 3000) : undefined
    }
    await setStaffSalary(staff.id, data)
    ElMessage.success(`${staff.realName || staff.username} 薪资方案已保存`)
  } catch (e) {}
}

onMounted(fetchAllData)
</script>

<style scoped>
.shop-overview {
  display: flex; align-items: center; justify-content: space-between;
  padding: 22px 28px; border-radius: var(--radius-lg); margin-bottom: 20px;
  background: linear-gradient(135deg, rgba(255,140,66,0.08), rgba(255,209,102,0.06));
  border: 1.5px solid rgba(255,140,66,0.15);
}
.overview-left { display: flex; align-items: center; gap: 14px; }
.overview-icon { font-size: 38px; }
.overview-left h3 { font-size: 18px; margin-bottom: 2px; color: var(--color-text); }
.overview-left p { color: var(--color-text-secondary); margin: 0; font-size: 13px; }
.overview-stats { display: flex; gap: 28px; }
.overview-stat { text-align: center; }
.overview-stat strong { display: block; font-family: 'Poppins'; font-size: 26px; font-weight: 800; color: var(--color-primary); }
.overview-stat span { font-size: 12px; color: var(--color-text-placeholder); }

.main-tabs { margin-top: 4px; }
.tab-toolbar {
  display: flex; align-items: center; justify-content: space-between;
  margin-bottom: 16px; padding: 12px 16px; border-radius: var(--radius-md);
  background: rgba(240,242,245,0.5);
}
.toolbar-left { display: flex; gap: 10px; align-items: center; }

.user-info-cell { display: flex; align-items: center; gap: 10px; }
.user-info-text { display: flex; flex-direction: column; }
.user-info-text strong { font-size: 13px; color: var(--color-text); }
.user-info-text span { font-size: 12px; color: var(--color-text-placeholder); }

.salary-text { font-weight: 700; color: #67C23A; font-family:'Poppins'; }
.commission-text { font-weight: 700; color: var(--color-primary); font-family:'Poppins'; }
.estimate-income { font-weight: 800; color: var(--color-primary); font-family:'Poppins'; font-size:15px; }

.pagination-wrap { display: flex; justify-content: center; margin-top: 18px; }
.section-desc { color: var(--color-text-secondary); margin-bottom: 16px; font-size: 13px; }

@media (max-width: 768px) {
  .shop-overview { flex-direction: column; gap: 14px; text-align: center; }
  .overview-stats { gap: 24px; }
  .tab-toolbar { flex-direction: column; gap: 10px; }
}
</style>
