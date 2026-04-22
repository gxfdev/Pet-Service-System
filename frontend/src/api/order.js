import request from '@/utils/request'

export function getOrders(params) {
  return request.get('/orders', { params })
}

export function getOrderDetail(id) {
  return request.get(`/orders/${id}`)
}

export function createOrder(data) {
  return request.post('/orders', data)
}

export function payOrder(id) {
  return request.put(`/orders/${id}/pay`)
}

export function cancelOrder(id, reason = '') {
  return request.put(`/orders/${id}/cancel`, { reason })
}

export function acceptOrder(id) {
  return request.put(`/orders/${id}/accept`)
}

export function completeOrder(id) {
  return request.put(`/orders/${id}/complete`)
}

export function addReview(id, data) {
  return request.post(`/orders/${id}/review`, data)
}
