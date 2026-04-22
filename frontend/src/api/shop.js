import request from '@/utils/request'

// 获取本店用户列表（含店员+客户）
export function getShopUsers(params) {
  return request.get('/shop/users', { params })
}

// 获取本店成员概览
export function getUsersSummary() {
  return request.get('/shop/users-summary')
}

// 添加店员
export function addStaff(data) {
  return request.post('/shop/add-staff', data)
}

// 添加客户
export function addCustomer(data) {
  return request.post('/shop/add-customer', data)
}

// 更新用户信息
export function updateShopUser(id, data) {
  return request.put(`/shop/users/${id}`, data)
}

// 删除/禁用用户
export function deleteShopUser(id) {
  return request.delete(`/shop/users/${id}`)
}

// 设置员工薪资方案（双方案：提成制/时薪制/固定月薪）
export function setStaffSalary(staffId, data) {
  // 优先使用 IncomeController 的双方案接口
  return request.put('/income/staff-salary', { ...data, staffId })
}

// 获取店铺可用店员列表（供下单选择）
export function getAvailableStaff(providerId) {
  const params = providerId ? { providerId } : {}
  return request.get('/shop/available-staff', { params })
}
