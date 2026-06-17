import request from './request.js'
export const channelApi = {
  list: (params) => request.get('/channels', { params }),
  all: () => request.get('/channels/all'),
  get: (id) => request.get(`/channels/${id}`),
  create: (data) => request.post('/channels', data),
  update: (id, data) => request.put(`/channels/${id}`, data),
  remove: (id) => request.delete(`/channels/${id}`)
}
