import { defineStore } from 'pinia'
import { ref } from 'vue'
import request from '@/utils/request'

export const useOrderStore = defineStore('order', () => {
  const orderList = ref([])
  const currentOrder = ref(null)
  const loading = ref(false)

  async function fetchOrders(params = {}) {
    loading.value = true
    try {
      const res = await request.get('/orders', { params })
      orderList.value = res.data.records || res.data || []
    } finally {
      loading.value = false
    }
  }

  async function createOrder(data) {
    return await request.post('/orders', data)
  }

  async function payOrder(orderId) {
    return await request.put(`/orders/${orderId}/pay`)
  }

  async function cancelOrder(orderId, reason) {
    return await request.put(`/orders/${orderId}/cancel`, { reason })
  }

  async function acceptOrder(orderId) {
    return await request.put(`/orders/${orderId}/accept`)
  }

  async function completeOrder(orderId) {
    return await request.put(`/orders/${orderId}/complete`)
  }

  async function addReview(orderId, data) {
    return await request.post(`/orders/${orderId}/review`, data)
  }

  return {
    orderList, currentOrder, loading,
    fetchOrders, createOrder, payOrder, cancelOrder,
    acceptOrder, completeOrder, addReview
  }
})
