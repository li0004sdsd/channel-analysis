import request from './request.js'
export const analysisApi = {
  report: (params) => request.get('/analysis/report', { params }),
  reportByType: (params) => request.get('/analysis/report-by-type', { params })
}
