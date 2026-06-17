import { defineStore } from 'pinia'
import { ref } from 'vue'
import { channelApi } from '@/api/channel.js'

export const useChannelStore = defineStore('channel', () => {
  const channels = ref([])
  const total = ref(0)
  const loading = ref(false)

  async function fetchChannels(params) {
    loading.value = true
    try {
      const res = await channelApi.list(params)
      channels.value = res.data.data.records
      total.value = res.data.data.total
    } finally {
      loading.value = false
    }
  }

  async function fetchAllChannels() {
    const res = await channelApi.all()
    channels.value = res.data.data
    return channels.value
  }

  return { channels, total, loading, fetchChannels, fetchAllChannels }
})
