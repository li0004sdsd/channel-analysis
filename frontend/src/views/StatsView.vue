<template>
  <div>
    <el-card shadow="never" style="margin-bottom: 16px;">
      <el-row :gutter="16" align="middle">
        <el-col :span="6">
          <el-select v-model="selectedChannel" placeholder="Select channel" style="width: 100%;" @change="loadStats">
            <el-option v-for="c in allChannels" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-col>
        <el-col :span="4" style="text-align: right;">
          <el-button type="primary" :disabled="!selectedChannel" @click="showAddDialog = true">
            <el-icon><Plus /></el-icon> Add Record
          </el-button>
        </el-col>
      </el-row>
    </el-card>

    <el-card shadow="never">
      <el-table :data="statsList" v-loading="loading" stripe>
        <el-table-column prop="statDate" label="Date" />
        <el-table-column prop="impressions" label="Impressions" />
        <el-table-column prop="clicks" label="Clicks" />
        <el-table-column prop="conversions" label="Conversions" />
        <el-table-column prop="cost" label="Cost">
          <template #default="{ row }">¥{{ row.cost }}</template>
        </el-table-column>
        <el-table-column prop="revenue" label="Revenue">
          <template #default="{ row }">¥{{ row.revenue }}</template>
        </el-table-column>
        <el-table-column label="Actions" width="120">
          <template #default="{ row }">
            <el-button size="small" @click="openEdit(row)">Edit</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row.id)">Del</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div style="margin-top: 16px; display: flex; justify-content: flex-end;">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="total"
          layout="total, prev, pager, next"
          @change="loadStats"
        />
      </div>
    </el-card>

    <el-dialog v-model="showAddDialog" :title="editingId ? 'Edit Record' : 'Add Stats Record'" width="500px">
      <el-form :model="statsForm" ref="statsFormRef" label-width="120px">
        <el-form-item label="Date" :rules="[{ required: true }]">
          <el-date-picker v-model="statsForm.statDate" type="date" value-format="YYYY-MM-DD" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="Impressions">
          <el-input-number v-model="statsForm.impressions" :min="0" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="Clicks">
          <el-input-number v-model="statsForm.clicks" :min="0" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="Conversions">
          <el-input-number v-model="statsForm.conversions" :min="0" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="Cost">
          <el-input-number v-model="statsForm.cost" :min="0" :precision="2" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="Revenue">
          <el-input-number v-model="statsForm.revenue" :min="0" :precision="2" style="width: 100%;" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddDialog = false">Cancel</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">Save</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { channelApi } from '@/api/channel.js'
import { statsApi } from '@/api/stats.js'

const allChannels = ref([])
const selectedChannel = ref(null)
const statsList = ref([])
const total = ref(0)
const loading = ref(false)
const saving = ref(false)
const showAddDialog = ref(false)
const editingId = ref(null)
const statsFormRef = ref(null)
const pagination = reactive({ page: 1, size: 10 })

const statsForm = reactive({
  statDate: '',
  impressions: 0,
  clicks: 0,
  conversions: 0,
  cost: 0,
  revenue: 0
})

onMounted(async () => {
  const res = await channelApi.all()
  allChannels.value = res.data.data
})

async function loadStats() {
  if (!selectedChannel.value) return
  loading.value = true
  try {
    const res = await statsApi.list(selectedChannel.value, { page: pagination.page, size: pagination.size })
    statsList.value = res.data.data.records
    total.value = res.data.data.total
  } finally {
    loading.value = false
  }
}

function openEdit(row) {
  editingId.value = row.id
  Object.assign(statsForm, row)
  showAddDialog.value = true
}

async function handleSave() {
  saving.value = true
  try {
    const data = { ...statsForm, channelId: selectedChannel.value }
    if (editingId.value) {
      await statsApi.update(editingId.value, data)
    } else {
      await statsApi.create(data)
    }
    ElMessage.success('Saved')
    showAddDialog.value = false
    editingId.value = null
    Object.assign(statsForm, { statDate: '', impressions: 0, clicks: 0, conversions: 0, cost: 0, revenue: 0 })
    loadStats()
  } finally {
    saving.value = false
  }
}

async function handleDelete(id) {
  await ElMessageBox.confirm('Delete this record?', 'Confirm', { type: 'warning' })
  await statsApi.remove(id)
  ElMessage.success('Deleted')
  loadStats()
}
</script>
