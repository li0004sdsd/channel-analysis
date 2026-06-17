<template>
  <div>
    <el-card shadow="never" style="margin-bottom: 16px;">
      <el-row :gutter="16" align="middle">
        <el-col :span="6">
          <el-input v-model="search.name" placeholder="Search by name" clearable @keyup.enter="loadChannels" />
        </el-col>
        <el-col :span="4">
          <el-select v-model="search.status" placeholder="Status" clearable>
            <el-option label="Active" value="ACTIVE" />
            <el-option label="Inactive" value="INACTIVE" />
          </el-select>
        </el-col>
        <el-col :span="4">
          <el-button type="primary" @click="loadChannels">Search</el-button>
          <el-button @click="resetSearch">Reset</el-button>
        </el-col>
        <el-col :span="10" style="text-align: right;">
          <el-button type="primary" @click="$router.push('/channels/create')">
            <el-icon><Plus /></el-icon> Add Channel
          </el-button>
        </el-col>
      </el-row>
    </el-card>

    <el-card shadow="never">
      <el-table :data="channels" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="name" label="Name" />
        <el-table-column prop="type" label="Type" />
        <el-table-column prop="budget" label="Budget">
          <template #default="{ row }">{{ row.budget ? `¥${row.budget}` : '-' }}</template>
        </el-table-column>
        <el-table-column prop="status" label="Status">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'info'">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="Created At">
          <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="Actions" width="160">
          <template #default="{ row }">
            <el-button size="small" @click="$router.push(`/channels/${row.id}/edit`)">Edit</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row.id)">Delete</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div style="margin-top: 16px; display: flex; justify-content: flex-end;">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="total"
          layout="total, prev, pager, next"
          @change="loadChannels"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { channelApi } from '@/api/channel.js'

const channels = ref([])
const total = ref(0)
const loading = ref(false)
const search = reactive({ name: '', status: '' })
const pagination = reactive({ page: 1, size: 10 })

function formatDate(dt) {
  if (!dt) return '-'
  return new Date(dt).toLocaleDateString()
}

async function loadChannels() {
  loading.value = true
  try {
    const res = await channelApi.list({
      page: pagination.page,
      size: pagination.size,
      name: search.name || undefined,
      status: search.status || undefined
    })
    channels.value = res.data.data.records
    total.value = res.data.data.total
  } finally {
    loading.value = false
  }
}

function resetSearch() {
  search.name = ''
  search.status = ''
  pagination.page = 1
  loadChannels()
}

async function handleDelete(id) {
  await ElMessageBox.confirm('Delete this channel?', 'Confirm', { type: 'warning' })
  await channelApi.remove(id)
  ElMessage.success('Deleted')
  loadChannels()
}

onMounted(loadChannels)
</script>
