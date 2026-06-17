import request from './request.js'
export const exportApi = {
  excel: (params) => request.get('/export/excel', { params, responseType: 'blob' }),
  csv: (params) => request.get('/export/csv', { params, responseType: 'blob' })
}
