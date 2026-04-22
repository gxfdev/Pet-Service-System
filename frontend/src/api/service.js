import request from '@/utils/request'

export function getServices(params) {
  return request.get('/services', { params })
}

export function getServiceDetail(id) {
  return request.get(`/services/${id}`)
}

export function getServiceProviders(id) {
  return request.get(`/services/${id}/providers`)
}
