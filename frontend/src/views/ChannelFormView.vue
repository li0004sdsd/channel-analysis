<template>
  <el-card shadow="never" style="max-width: 600px;">
    <el-form :model="form" :rules="rules" ref="formRef" label-width="120px">
      <el-form-item label="Name" prop="name">
        <el-input v-model="form.name" placeholder="Channel name" />
      </el-form-item>
      <el-form-item label="Type" prop="type">
        <el-select v-model="form.type" placeholder="Select type" style="width: 100%;">
          <el-option label="Social Media" value="SOCIAL" />
          <el-option label="Search Engine" value="SEARCH" />
          <el-option label="Display Ads" value="DISPLAY" />
          <el-option label="Email" value="EMAIL" />
          <el-option label="Affiliate" value="AFFILIATE" />
          <el-option label="Other" value="OTHER" />
        </el-select>
      </el-form-item>
      <el-form-item label="Description">
        <el-input v-model="form.description" type="textarea" rows="3" />
      </el-form-item>
      <el-form-item label="Budget">
        <el-input-number v-model="form.budget" :min="0" :precision="2" style="width: 100%;" />
      </el-form-item>
      <el-form-item label="Status">
        <el-select v-model="form.status" style="width: 100%;">
          <el-option label="Active" value="ACTIVE" />
          <el-option label="Inactive" value="INACTIVE" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :loading="loading" @click="handleSubmit">
          {{ isEdit ? 'Update' : 'Create' }}
        </el-button>
        <el-button @click="$router.push('/channels')">Cancel</el-button>
      </el-form-item>
    </el-form>
  </el-card>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { channelApi } from '@/api/channel.js'

const route = useRoute()
const router = useRouter()
const formRef = ref(null)
const loading = ref(false)
const isEdit = computed(() => !!route.params.id)

const form = reactive({
  name: '',
  type: '',
  description: '',
  budget: 0,
  status: 'ACTIVE'
})

const rules = {
  name: [{ required: true, message: 'Name is required', trigger: 'blur' }]
}

onMounted(async () => {
  if (isEdit.value) {
    const res = await channelApi.get(route.params.id)
    Object.assign(form, res.data.data)
  }
})

async function handleSubmit() {
  await formRef.value.validate()
  loading.value = true
  try {
    if (isEdit.value) {
      await channelApi.update(route.params.id, form)
      ElMessage.success('Channel updated')
    } else {
      await channelApi.create(form)
      ElMessage.success('Channel created')
    }
    router.push('/channels')
  } finally {
    loading.value = false
  }
}
</script>
