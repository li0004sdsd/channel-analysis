<template>
  <div>
    <el-row :gutter="16" style="margin-bottom: 24px;">
      <el-col :span="6" v-for="card in summaryCards" :key="card.label">
        <el-card shadow="never">
          <div style="display: flex; justify-content: space-between; align-items: center;">
            <div>
              <div style="font-size: 13px; color: #999;">{{ card.label }}</div>
              <div style="font-size: 28px; font-weight: 600; margin-top: 8px;">{{ card.value }}</div>
            </div>
            <el-icon :size="40" :style="{ color: card.color }">
              <component :is="card.icon" />
            </el-icon>
          </div>
        </el-card>
      </el-col>
    </el-row>
    <el-row :gutter="16">
      <el-col :span="24">
        <el-card shadow="never" header="Recent Analysis (Last 30 Days)">
          <div v-if="reportData.length === 0" style="text-align: center; padding: 40px; color: #999;">
            No data available. Add channels and statistics to see analysis.
          </div>
          <el-table v-else :data="reportData" stripe>
            <el-table-column prop="channelName" label="Channel" />
            <el-table-column prop="totalImpressions" label="Impressions" />
            <el-table-column prop="totalClicks" label="Clicks" />
            <el-table-column prop="totalConversions" label="Conversions" />
            <el-table-column label="CTR">
              <template #default="{ row }">{{ row.ctr }}%</template>
            </el-table-column>
            <el-table-column label="CVR">
              <template #default="{ row }">{{ row.cvr }}%</template>
            </el-table-column>
            <el-table-column label="ROI">
              <template #default="{ row }">{{ row.roi }}%</template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { channelApi } from '@/api/channel.js'
import { analysisApi } from '@/api/analysis.js'

const summaryCards = ref([
  { label: 'Total Channels', value: 0, icon: 'Connection', color: '#1890ff' },
  { label: 'Active Channels', value: 0, icon: 'CircleCheck', color: '#52c41a' },
  { label: 'Total Impressions', value: 0, icon: 'View', color: '#722ed1' },
  { label: 'Total Conversions', value: 0, icon: 'Star', color: '#fa8c16' }
])
const reportData = ref([])

onMounted(async () => {
  const [channelsRes, reportRes] = await Promise.allSettled([
    channelApi.all(),
    analysisApi.report({
      start: new Date(Date.now() - 30 * 86400000).toISOString().split('T')[0],
      end: new Date().toISOString().split('T')[0]
    })
  ])

  if (channelsRes.status === 'fulfilled') {
    const all = channelsRes.value.data.data
    summaryCards.value[0].value = all.length
    summaryCards.value[1].value = all.filter(c => c.status === 'ACTIVE').length
  }

  if (reportRes.status === 'fulfilled') {
    const data = reportRes.value.data.data
    reportData.value = data
    summaryCards.value[2].value = data.reduce((s, r) => s + (r.totalImpressions || 0), 0).toLocaleString()
    summaryCards.value[3].value = data.reduce((s, r) => s + (r.totalConversions || 0), 0).toLocaleString()
  }
})
</script>
