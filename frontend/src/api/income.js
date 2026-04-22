import request from '@/utils/request'

// 收入统计
export function getIncomeStats() {
  return request.get('/income/statistics')
}

// 收入记录列表
export function getIncomeRecords(params) {
  return request.get('/income/records', { params })
}

// 分配收入（仅店长）- 固定提成模式
export function allocateIncome(data) {
  return request.post('/income/allocate', data)
}

// 批量结算（仅店长）
export function batchSettle(ids) {
  return request.put('/income/settle', ids)
}

// 获取店铺员工列表（仅店长）
export function getStaffList() {
  return request.get('/income/staff-list')
}

// 配置员工薪资（仅店长）
export function updateStaffSalary(data) {
  return request.put('/income/staff-salary', data)
}

// 月度收益汇总
export function getMonthlySummary(params) {
  return request.get('/income/monthly-summary', { params })
}

// 获取待分配订单（仅店长）
export function getUnallocatedOrders() {
  return request.get('/income/unallocated-orders')
}
