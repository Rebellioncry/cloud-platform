<template>
  <div class="page-container">
    <div class="toolbar">
      <el-button type="primary" @click="handleAdd">新增角色</el-button>
    </div>
    
    <el-table :data="tableData" v-loading="loading" border stripe>
      <el-table-column type="index" label="#" width="60" />
      <el-table-column prop="roleCode" label="角色编码" />
      <el-table-column prop="roleName" label="角色名称" />
      <el-table-column prop="roleSort" label="排序" width="80" />
      <el-table-column prop="status" label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'">
            {{ row.status === 1 ? '正常' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column prop="remark" label="备注" />
      <el-table-column label="操作" width="250" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
          <el-button link type="primary" @click="handleMenus(row)">菜单</el-button>
          <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="角色编码" prop="roleCode">
          <el-input v-model="form.roleCode" :disabled="!!form.id" />
        </el-form-item>
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="form.roleName" />
        </el-form-item>
        <el-form-item label="排序" prop="roleSort">
          <el-input-number v-model="form.roleSort" :min="0" />
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

    <el-dialog v-model="menuDialogVisible" title="分配菜单" width="500px">
      <div style="margin-bottom: 10px; display: flex; align-items: center; gap: 10px;">
        <el-checkbox v-model="allMenuChecked" :indeterminate="allMenuIndeterminate" @change="handleCheckAllMenu">全选/取消全选</el-checkbox>
      </div>
      <el-tree
        ref="menuTreeRef"
        :data="menuTreeData"
        :props="{ label: 'menuName', children: 'children' }"
        show-checkbox
        node-key="id"
        default-expand-all
        @check-change="updateCheckAllState"
      />
      <template #footer>
        <el-button @click="menuDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitMenus">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getRoleList, addRole, updateRole, deleteRole, getMenuTree, assignMenus, getRole } from '@/api/system'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

const loading = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const menuDialogVisible = ref(false)
const formRef = ref()
const menuTreeRef = ref()
const menuTreeData = ref([])
const allMenuChecked = ref(false)
const allMenuIndeterminate = ref(false)
const isEdit = computed(() => !!form.id)
const dialogTitle = computed(() => isEdit.value ? '编辑角色' : '新增角色')
let currentRoleId = ''

const form = reactive({
  id: null,
  roleCode: '',
  roleName: '',
  roleSort: 0,
  status: 1,
  remark: ''
})

const rules = {
  roleCode: [{ required: true, message: '请输入角色编码', trigger: 'blur' }],
  roleName: [{ required: true, message: '请输入角色名称', trigger: 'blur' }]
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getRoleList({ page: 1, size: 100 })
    tableData.value = res.data.records || []
  } catch (error) {
    console.error('加载数据失败:', error)
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  Object.assign(form, { id: null, roleCode: '', roleName: '', roleSort: 0, status: 1, remark: '' })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  Object.assign(form, { ...row })
  dialogVisible.value = true
}

const handleMenus = async (row) => {
  currentRoleId = row.id
  try {
    const [treeRes, roleRes] = await Promise.all([getMenuTree(), getRole(row.id)])
    menuTreeData.value = treeRes.data || []
    menuDialogVisible.value = true
    await nextTick()
    if (menuTreeRef.value) {
      const menuIds = roleRes.data?.menuIds || []
      menuTreeRef.value.setCheckedKeys(menuIds.map(String))
    }
    updateCheckAllState()
  } catch (error) {
    console.error('加载菜单失败:', error)
  }
}

const handleSubmitMenus = async () => {
  if (!menuTreeRef.value) return
  const checkedKeys = menuTreeRef.value.getCheckedKeys()
  const halfCheckedKeys = menuTreeRef.value.getHalfCheckedKeys()
  const menuIds = [...checkedKeys, ...halfCheckedKeys].map(String)
  try {
    await assignMenus(currentRoleId, menuIds)
    ElMessage.success('菜单分配成功')
    menuDialogVisible.value = false
  } catch (error) {
    console.error('分配菜单失败:', error)
  }
}

function getAllLeafAndParentKeys(data) {
  const keys = []
  function walk(nodes) {
    for (const n of nodes) {
      keys.push(n.id)
      if (n.children && n.children.length) walk(n.children)
    }
  }
  walk(data)
  return keys
}

const handleCheckAllMenu = (val) => {
  if (!menuTreeRef.value) return
  if (val) {
    menuTreeRef.value.setCheckedKeys(getAllLeafAndParentKeys(menuTreeData.value))
  } else {
    menuTreeRef.value.setCheckedKeys([])
  }
  allMenuIndeterminate.value = false
}

const updateCheckAllState = () => {
  if (!menuTreeRef.value) return
  const allKeys = getAllLeafAndParentKeys(menuTreeData.value)
  const checked = menuTreeRef.value.getCheckedKeys()
  const half = menuTreeRef.value.getHalfCheckedKeys()
  const total = checked.length + half.length
  allMenuChecked.value = total === allKeys.length
  allMenuIndeterminate.value = total > 0 && total < allKeys.length
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该角色吗？', '提示', { type: 'warning' })
    await deleteRole(row.id)
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
      await updateRole(form.id, form)
      ElMessage.success('更新成功')
    } else {
      await addRole(form)
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
