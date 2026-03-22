<template>
  <div class="page-container">
    <div class="toolbar">
      <el-button type="primary" @click="handleAdd">新增租户</el-button>
    </div>
    
    <el-table :data="tableData" v-loading="loading" border stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="tenantCode" label="租户编码" />
      <el-table-column prop="tenantName" label="租户名称" />
      <el-table-column prop="contact" label="联系人" />
      <el-table-column prop="mobile" label="手机号" />
      <el-table-column prop="email" label="邮箱" />
      <el-table-column prop="status" label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'">
            {{ row.status === 1 ? '正常' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="expireTime" label="过期时间" width="180" />
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
          <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="租户编码" prop="tenantCode">
          <el-input v-model="form.tenantCode" :disabled="!!form.id" />
        </el-form-item>
        <el-form-item label="租户名称" prop="tenantName">
          <el-input v-model="form.tenantName" />
        </el-form-item>
        <el-form-item label="联系人" prop="contact">
          <el-input v-model="form.contact" />
        </el-form-item>
        <el-form-item label="手机号" prop="mobile">
          <el-input v-model="form.mobile" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" />
        </el-form-item>
        <el-form-item label="过期时间" prop="expireTime">
          <el-date-picker v-model="form.expireTime" type="datetime" placeholder="选择过期时间" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">正常</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getTenantList, addTenant, updateTenant, deleteTenant } from '@/api/system'

const loading = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const formRef = ref()
const isEdit = computed(() => !!form.id)
const dialogTitle = computed(() => isEdit.value ? '编辑租户' : '新增租户')

const form = reactive({
  id: null,
  tenantCode: '',
  tenantName: '',
  contact: '',
  mobile: '',
  email: '',
  expireTime: null,
  status: 1,
  remark: ''
})

const rules = {
  tenantCode: [{ required: true, message: '请输入租户编码', trigger: 'blur' }],
  tenantName: [{ required: true, message: '请输入租户名称', trigger: 'blur' }]
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getTenantList({ page: 1, size: 100 })
    tableData.value = res.data.records || []
  } catch (error) {
    console.error('加载数据失败:', error)
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  Object.assign(form, { id: null, tenantCode: '', tenantName: '', contact: '', mobile: '', email: '', expireTime: null, status: 1, remark: '' })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  Object.assign(form, { ...row })
  dialogVisible.value = true
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该租户吗？', '提示', { type: 'warning' })
    await deleteTenant(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') console.error('删除失败:', error)
  }
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  try {
    if (isEdit.value) {
      await updateTenant(form)
      ElMessage.success('更新成功')
    } else {
      await addTenant(form)
      ElMessage.success('添加成功')
    }
    dialogVisible.value = false
    loadData()
  } catch (error) {
    console.error('操作失败:', error)
  }
}

loadData()
</script>

<style scoped>
.page-container { padding: 20px; }
.toolbar { margin-bottom: 20px; }
</style>
