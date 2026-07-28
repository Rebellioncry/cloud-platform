<template>
  <div class="page-container">
    <div class="toolbar">
      <el-button type="primary" @click="handleAdd">新增租户</el-button>
    </div>
    
    <el-table :data="tableData" v-loading="loading" border stripe>
      <el-table-column type="index" label="#" width="60" />
      <el-table-column prop="tenantCode" label="租户编码" />
      <el-table-column prop="tenantName" label="租户名称" />
      <el-table-column prop="contact" label="联系人" />
      <el-table-column prop="mobile" label="手机号" />
      <el-table-column prop="userCount" label="用户数" width="80" />
      <el-table-column label="套餐" width="140">
        <template #default="{ row }">
          {{ getPackageName(row.packageId) }}
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'">
            {{ row.status === 1 ? '正常' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="expireTime" label="过期时间" width="180" />
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column label="操作" width="300" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
          <el-button link type="primary" @click="handleCreateAdmin(row)">创建管理员</el-button>
          <el-button v-if="row.status === 1 && row.userCount > 0" link type="warning" @click="handleImpersonate(row)">模拟登录</el-button>
          <el-button v-if="row.status === 1" link type="warning" @click="handleToggleStatus(row, 0)">禁用</el-button>
          <el-button v-else link type="success" @click="handleToggleStatus(row, 1)">启用</el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <!-- 新增/编辑租户弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-divider content-position="left">租户信息</el-divider>
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
        <el-form-item label="套餐" prop="packageId">
          <el-select v-model="form.packageId" placeholder="请选择套餐（可不选）" clearable style="width: 100%">
            <el-option
              v-for="pkg in packageOptions"
              :key="pkg.id"
              :label="pkg.packageName"
              :value="pkg.id"
            />
          </el-select>
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
          <el-input v-model="form.remark" type="textarea" rows="2" />
        </el-form-item>

        <template v-if="!form.id">
          <el-divider content-position="left">管理员账号</el-divider>
          <el-form-item label="管理员用户名" prop="adminUsername">
            <el-input v-model="form.adminUsername" placeholder="将作为该租户的管理员登录账号" />
          </el-form-item>
          <el-form-item label="管理员密码" prop="adminPassword">
            <el-input v-model="form.adminPassword" type="password" placeholder="默认 123456" show-password />
          </el-form-item>
          <el-form-item label="管理员昵称">
            <el-input v-model="form.adminNickname" placeholder="可选" />
          </el-form-item>
        </template>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 为已有租户创建管理员弹窗 -->
    <el-dialog v-model="adminDialogVisible" title="创建租户管理员" width="500px">
      <el-form ref="adminFormRef" :model="adminForm" :rules="adminRules" label-width="100px">
        <el-form-item label="租户">
          <el-input :model-value="adminTenantName" disabled />
        </el-form-item>
        <el-form-item label="用户名" prop="username">
          <el-input v-model="adminForm.username" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="adminForm.password" type="password" placeholder="默认 123456" show-password />
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="adminForm.nickname" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="adminDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleCreateAdminSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import { getPlatformTenantList, createPlatformTenant, createTenantAdmin, updateTenantStatus, impersonateTenant, getAllPackages } from '@/api/system'
import { getUserInfo } from '@/api/auth'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const adminDialogVisible = ref(false)
const formRef = ref()
const adminFormRef = ref()
const isEdit = computed(() => !!form.id)
const dialogTitle = computed(() => isEdit.value ? '编辑租户' : '新增租户')
const adminTenantName = ref('')
let adminTenantId = ''
const packageOptions = ref([])

const form = reactive({
  id: null,
  tenantCode: '',
  tenantName: '',
  contact: '',
  mobile: '',
  email: '',
  packageId: '',
  expireTime: null,
  status: 1,
  remark: '',
  adminUsername: '',
  adminPassword: '123456',
  adminNickname: ''
})

const adminForm = reactive({
  username: '',
  password: '123456',
  nickname: ''
})

const rules = {
  tenantCode: [{ required: true, message: '请输入租户编码', trigger: 'blur' }],
  tenantName: [{ required: true, message: '请输入租户名称', trigger: 'blur' }],
  adminUsername: [{ required: true, message: '请输入管理员用户名', trigger: 'blur' }]
}

const adminRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }]
}

const handleImpersonate = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要模拟登录为「${row.tenantName}」的管理员吗？`,
      '模拟登录',
      { type: 'warning', confirmButtonText: '确定模拟', cancelButtonText: '取消' }
    )
    const res = await impersonateTenant(row.id)
    userStore.startImpersonate(res.data)
    const infoRes = await getUserInfo()
    userStore.setUserInfo(infoRes.data)
    ElMessage.success(`已模拟登录为 ${res.data.nickname || res.data.username}`)
    router.push('/')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('模拟登录失败:', error)
    }
  }
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getPlatformTenantList({ page: 1, size: 100 })
    tableData.value = res.data.records || []
  } catch (error) {
    console.error('加载数据失败:', error)
  } finally {
    loading.value = false
  }
}

const loadPackages = async () => {
  try {
    const res = await getAllPackages()
    packageOptions.value = res.data || []
  } catch (error) {
    console.error('加载套餐失败:', error)
  }
}

const getPackageName = (packageId) => {
  if (!packageId) return '-'
  const pkg = packageOptions.value.find(p => p.id === packageId)
  return pkg ? pkg.packageName : '-'
}

const handleAdd = () => {
  Object.assign(form, {
    id: null, tenantCode: '', tenantName: '', contact: '', mobile: '',
    email: '', packageId: '', expireTime: null, status: 1, remark: '',
    adminUsername: '', adminPassword: '123456', adminNickname: ''
  })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  Object.assign(form, { ...row, adminUsername: '', adminPassword: '123456', adminNickname: '' })
  dialogVisible.value = true
}

const handleCreateAdmin = (row) => {
  adminTenantId = row.id
  adminTenantName.value = row.tenantName
  Object.assign(adminForm, { username: '', password: '123456', nickname: '' })
  adminDialogVisible.value = true
}

const handleToggleStatus = async (row, status) => {
  const action = status === 0 ? '禁用' : '启用'
  try {
    await ElMessageBox.confirm(`确定要${action}该租户吗？`, '提示', { type: 'warning' })
    await updateTenantStatus(row.id, status)
    ElMessage.success(`${action}成功`)
    loadData()
  } catch (error) {
    if (error !== 'cancel') console.error(`${action}失败:`, error)
  }
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  try {
    if (isEdit.value) {
      const { adminUsername, adminPassword, adminNickname, ...tenantData } = form
      await createPlatformTenant({ tenant: tenantData, admin: null })
      ElMessage.success('更新成功')
    } else {
      await createPlatformTenant({
        tenant: {
          tenantCode: form.tenantCode,
          tenantName: form.tenantName,
          contact: form.contact,
          mobile: form.mobile,
          email: form.email,
          packageId: form.packageId,
          expireTime: form.expireTime,
          status: form.status,
          remark: form.remark
        },
        admin: {
          username: form.adminUsername,
          password: form.adminPassword || '123456',
          nickname: form.adminNickname || form.adminUsername
        }
      })
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadData()
  } catch (error) {
    console.error('操作失败:', error)
  }
}

const handleCreateAdminSubmit = async () => {
  const valid = await adminFormRef.value.validate().catch(() => false)
  if (!valid) return
  try {
    await createTenantAdmin(adminTenantId, {
      username: adminForm.username,
      password: adminForm.password || '123456',
      nickname: adminForm.nickname || adminForm.username
    })
    ElMessage.success('管理员创建成功')
    adminDialogVisible.value = false
    loadData()
  } catch (error) {
    console.error('创建管理员失败:', error)
  }
}

loadData()
loadPackages()
</script>

<style scoped>
.page-container { padding: 20px; }
.toolbar { margin-bottom: 20px; }
</style>
