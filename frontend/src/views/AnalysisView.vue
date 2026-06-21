<template>
  <div>
    <el-card shadow="never" style="margin-bottom: 16px;">
      <el-row :gutter="16" align="middle">
        <el-col :span="8">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="to"
            start-placeholder="Start Date"
            end-placeholder="End Date"
            value-format="YYYY-MM-DD"
            style="width: 100%;"
          />
        </el-col>
        <el-col :span="4">
          <el-radio-group v-model="viewMode" size="default">
            <el-radio-button label="channel">By Channel</el-radio-button>
            <el-radio-button label="type">By Type</el-radio-button>
          </el-radio-group>
        </el-col>
        <el-col :span="4">
          <el-button type="primary" @click="loadReport" :loading="loading">Generate Report</el-button>
        </el-col>
        <el-col :span="8" style="text-align: right;" v-if="reportData.length > 0">
          <el-button @click="exportFile('csv')">Export CSV</el-button>
          <el-button type="success" @click="exportFile('excel')">Export Excel</el-button>
        </el-col>
      </el-row>
    </el-card>

    <el-card shadow="never" v-if="reportData.length > 0">
      <el-table :data="reportData" stripe border>
        <el-table-column v-if="viewMode === 'channel'" prop="channelName" label="Channel" min-width="120" />
        <el-table-column :prop="viewMode === 'type' ? 'channelType' : 'channelType'" :label="viewMode === 'type' ? 'Channel Type' : 'Type'" width="120" />
        <el-table-column prop="totalImpressions" label="Impressions" sortable />
        <el-table-column prop="totalClicks" label="Clicks" sortable />
        <el-table-column prop="totalConversions" label="Conversions" sortable />
        <el-table-column label="Cost" sortable>
          <template #default="{ row }">¥{{ Number(row.totalCost).toFixed(2) }}</template>
        </el-table-column>
        <el-table-column label="Revenue" sortable>
          <template #default="{ row }">¥{{ Number(row.totalRevenue).toFixed(2) }}</template>
        </el-table-column>
        <el-table-column label="CTR(%)" sortable>
          <template #default="{ row }">
            <el-tag :type="row.ctr >= 5 ? 'success' : 'warning'">{{ Number(row.ctr).toFixed(2) }}%</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="CVR(%)" sortable>
          <template #default="{ row }">
            <el-tag :type="row.cvr >= 3 ? 'success' : 'warning'">{{ Number(row.cvr).toFixed(2) }}%</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="ROI(%)" sortable>
          <template #default="{ row }">
            <el-tag :type="row.roi >= 100 ? 'success' : row.roi >= 0 ? 'warning' : 'danger'">
              {{ Number(row.roi).toFixed(2) }}%
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="CPC" sortable>
          <template #default="{ row }">¥{{ Number(row.cpc).toFixed(4) }}</template>
        </el-table-column>
        <el-table-column label="CPA" sortable>
          <template #default="{ row }">¥{{ Number(row.cpa).toFixed(4) }}</template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-empty v-else description="Select a date range and generate a report" />
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { analysisApi } from '@/api/analysis.js'
import { exportApi } from '@/api/export.js'

const dateRange = ref([
  new Date(Date.now() - 30 * 86400000).toISOString().split('T')[0],
  new Date().toISOString().split('T')[0]
])
const viewMode = ref('channel')
const reportData = ref([])
const loading = ref(false)

async function loadReport() {
  if (!dateRange.value || dateRange.value.length < 2) {
    ElMessage.warning('Please select a date range')
    return
  }
  loading.value = true
  try {
    const params = { start: dateRange.value[0], end: dateRange.value[1] }
    const apiCall = viewMode.value === 'type' ? analysisApi.reportByType(params) : analysisApi.report(params)
    const res = await apiCall
    reportData.value = res.data.data
    if (reportData.value.length === 0) {
      ElMessage.info('No data found for the selected period')
    }
  } finally {
    loading.value = false
  }
}

async function exportFile(type) {
  try {
    const params = { start: dateRange.value[0], end: dateRange.value[1] }
    const res = type === 'excel' ? await exportApi.excel(params) : await exportApi.csv(params)
    const url = URL.createObjectURL(new Blob([res.data]))
    const a = document.createElement('a')
    a.href = url
    a.download = type === 'excel' ? 'analysis-report.xlsx' : 'analysis-report.csv'
    a.click()
    URL.revokeObjectURL(url)
    ElMessage.success('Export successful')
  } catch {
    ElMessage.error('Export failed')
  }
}
</script>
