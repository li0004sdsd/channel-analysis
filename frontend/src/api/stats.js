import request from './request.js'
export const statsApi = {
  list: (channelId, params) => request.get(`/stats/channel/${channelId}`, { params }),
  create: (data) => request.post('/stats', data),
  update: (id, data) => request.put(`/stats/${id}`, data),
  remove: (id) => request.delete(`/stats/${id}`),
  range: (params) => request.get('/stats/range', { params }),
  batch: (params) => request.get('/stats/batch', { params })
}
