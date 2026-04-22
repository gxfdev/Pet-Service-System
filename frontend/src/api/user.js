import request from '@/utils/request'

export function getProfile() {
  return request.get('/user/profile')
}

export function updateProfile(data) {
  return request.put('/user/profile', data)
}

export function uploadAvatar(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/file/upload-avatar', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export function uploadFile(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/file/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export function getPets() {
  return request.get('/user/pets')
}

export function addPet(data) {
  return request.post('/user/pet', data)
}

export function updatePet(id, data) {
  return request.put(`/user/pet/${id}`, data)
}

export function deletePet(id) {
  return request.delete(`/user/pet/${id}`)
}
