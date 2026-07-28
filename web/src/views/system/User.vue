<template>
  <div class="page-container">
    <div class="toolbar">
      <el-button type="primary" @click="handleAdd">新增用户</el-button>
    </div>
    
    <el-table :data="tableData" v-loading="loading" border stripe>
      <el-table-column type="index" label="#" width="60" :index="(i) => (pagination.page - 1) * pagination.size + i + 1" />
      <el-table-column prop="username" label="用户名" />
      <el-table-column prop="nickname" label="昵称" />
      <el-table-column v-if="isPlatformAdmin" prop="tenantName" label="所属租户" width="150" />
      <el-table-column prop="email" label="邮箱" />
      <el-table-column prop="mobile" label="手机号" />
      <el-table-column prop="status" label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'">
            {{ row.status === 1 ? '正常' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column label="操作" width="250" fixed="right">
        <template #default="{ row }">
          <template v-if="row.id !== '2'">
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="primary" @click="handleRoles(row)">角色</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
          <el-tag v-else type="info" size="small">超级管理员</el-tag>
        </template>
      </el-table-column>
    </el-table>
    
    <el-pagination
      v-model:current-page="pagination.page"
      v-model:page-size="pagination.size"
      :total="pagination.total"
      :page-sizes="[10, 20, 50, 100]"
      layout="total, sizes, prev, pager, next"
      @size-change="loadData"
      @current-change="loadData"
      style="margin-top: 20px; justify-content: flex-end"
    />
    
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" :disabled="!!form.id" />
        </el-form-item>
        <el-form-item label="密码" prop="password" v-if="!form.id">
          <el-input v-model="form.password" type="password" show-password />
        </el-form-item>
        <el-form-item label="所属租户" prop="tenantId" v-if="isPlatformAdmin">
          <el-select v-model="form.tenantId" placeholder="请选择租户" filterable style="width: 100%">
            <el-option
              v-for="t in tenantOptions"
              :key="t.id"
              :label="t.tenantName"
              :value="t.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="角色" prop="roleIds">
          <el-select v-model="form.roleIds" placeholder="请选择角色" multiple collapse-tags collapse-tags-tooltip style="width: 100%">
            <el-option
              v-for="r in roleOptions"
              :key="r.id"
              :label="r.roleName"
              :value="r.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="form.nickname" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" />
        </el-form-item>
        <el-form-item label="手机号" prop="mobile">
          <el-input v-model="form.mobile" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">正常</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="roleDialogVisible" title="分配角色" width="500px">
      <el-select v-model="selectedRoleIds" multiple collapse-tags collapse-tags-tooltip placeholder="请选择角色" style="width: 100%">
        <el-option
          v-for="r in roleOptions"
          :key="r.id"
          :label="r.roleName"
          :value="r.id"
        />
      </el-select>
      <template #footer>
        <el-button @click="roleDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitRoles">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUserList, addUser, updateUser, deleteUser, assignRoles, getRoleList, getTenantList } from '@/api/system'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const isPlatformAdmin = computed(() => userStore.userInfo?.tenantScope === 'PLATFORM')
const currentTenantId = computed(() => userStore.userInfo?.tenantId || '')

const loading = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const roleDialogVisible = ref(false)
const formRef = ref()
const isEdit = computed(() => !!form.id)
const dialogTitle = computed(() => isEdit.value ? '编辑用户' : '新增用户')

const tenantOptions = ref([])
const roleOptions = ref([])
const selectedRoleIds = ref([])
let currentUserId = ''

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const form = reactive({
  id: null,
  username: '',
  password: '',
  nickname: '',
  email: '',
  mobile: '',
  status: 1,
  tenantId: '',
  roleIds: []
})

const rules = computed(() => ({
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: !isEdit.value, message: '请输入密码', trigger: 'blur' }],
  tenantId: isPlatformAdmin.value ? [{ required: true, message: '请选择租户', trigger: 'change' }] : [],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}))

const loadTenantOptions = async () => {
  if (!isPlatformAdmin.value) return
  try {
    const res = await getTenantList({ page: 1, size: 999 })
    tenantOptions.value = res.data?.records || res.data || []
  } catch (e) {
    console.error('加载租户列表失败:', e)
  }
}

const loadRoleOptions = async () => {
  try {
    const res = await getRoleList({ page: 1, size: 999 })
    roleOptions.value = res.data?.records || res.data || []
  } catch (e) {
    console.error('加载角色列表失败:', e)
  }
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getUserList({
      page: pagination.page,
      size: pagination.size
    })
    tableData.value = res.data.records || []
    pagination.total = res.data.total || 0
  } catch (error) {
    console.error('加载数据失败:', error)
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  Object.assign(form, {
    id: null,
    username: '',
    password: '',
    nickname: '',
    email: '',
    mobile: '',
    status: 1,
    tenantId: isPlatformAdmin.value ? '' : currentTenantId.value,
    roleIds: []
  })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  Object.assign(form, { ...row, password: '' })
  dialogVisible.value = true
}

const handleRoles = (row) => {
  currentUserId = row.id
  selectedRoleIds.value = row.roleIds ? [...row.roleIds] : []
  roleDialogVisible.value = true
}

const handleSubmitRoles = async () => {
  try {
    await assignRoles(currentUserId, selectedRoleIds.value)
    ElMessage.success('角色分配成功')
    roleDialogVisible.value = false
    loadData()
  } catch (error) {
    console.error('分配角色失败:', error)
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该用户吗？', '提示', { type: 'warning' })
    await deleteUser(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
    }
  }
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  
  try {
    if (isEdit.value) {
      await updateUser(form)
      ElMessage.success('更新成功')
    } else {
      await addUser(form)
      ElMessage.success('添加成功')
    }
    dialogVisible.value = false
    loadData()
  } catch (error) {
    console.error('操作失败:', error)
  }
}

loadTenantOptions()
loadRoleOptions()
loadData()
</script>

<style scoped>
.page-container {
  padding: 20px;
}

.toolbar {
  margin-bottom: 20px;
}
</style>
