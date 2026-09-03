<template>
  <div class="page-container">
    <div class="toolbar">
      <el-button type="primary" @click="handleAdd">新增套餐</el-button>
    </div>

    <el-table :data="tableData" v-loading="loading" border stripe>
      <el-table-column type="index" label="#" width="60" />
      <el-table-column prop="id" label="套餐ID" width="120" />
      <el-table-column prop="packageName" label="套餐名称" />
      <el-table-column label="关联菜单数" width="120">
        <template #default="{ row }">
          <el-tag v-if="row.menuIds" type="info">{{ countMenus(row.menuIds) }} 个</el-tag>
          <el-tag v-else type="warning">0 个</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'">
            {{ row.status === 1 ? '正常' : '停用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="remark" label="备注" show-overflow-tooltip />
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
          <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="700px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="套餐名称" prop="packageName">
          <el-input v-model="form.packageName" placeholder="请输入套餐名称" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">正常</el-radio>
            <el-radio :label="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" rows="2" />
        </el-form-item>
        <el-form-item label="菜单配置" prop="menuIds">
          <div class="menu-tree-wrapper">
            <div class="tree-toolbar">
              <el-checkbox v-model="checkAll" :indeterminate="isIndeterminate" @change="handleCheckAll">全选/全不选</el-checkbox>
            </div>
            <el-tree
              ref="treeRef"
              :data="menuTreeData"
              :props="{ label: 'menuName', children: 'children' }"
              show-checkbox
              node-key="id"
              default-expand-all
              :check-strictly="false"
            />
          </div>
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
import { ref, reactive, computed, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMenuTree, getPackageList, createPackage, updatePackage, deletePackage } from '@/api/system'

const loading = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const formRef = ref()
const treeRef = ref()
const menuTreeData = ref([])
const checkAll = ref(false)
const isIndeterminate = ref(false)

const isEdit = computed(() => !!form.id)
const dialogTitle = computed(() => isEdit.value ? '编辑套餐' : '新增套餐')

const form = reactive({
  id: null,
  packageName: '',
  menuIds: '',
  status: 1,
  remark: ''
})

const rules = {
  packageName: [{ required: true, message: '请输入套餐名称', trigger: 'blur' }]
}

const countMenus = (menuIds) => {
  if (!menuIds) return 0
  return menuIds.split(',').filter(s => s.trim()).length
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getPackageList({ page: 1, size: 100 })
    tableData.value = res.data.records || []
  } catch (error) {
    console.error('加载数据失败:', error)
  } finally {
    loading.value = false
  }
}

const loadMenuTree = async () => {
  try {
    const res = await getMenuTree()
    menuTreeData.value = res.data || []
  } catch (error) {
    console.error('加载菜单失败:', error)
  }
}

const getLeafNodeIds = (nodes) => {
  const ids = []
  const traverse = (list) => {
    for (const node of list) {
      if (node.children && node.children.length > 0) {
        traverse(node.children)
      } else {
        ids.push(node.id)
      }
    }
  }
  traverse(nodes)
  return ids
}

const getSelectableIds = (nodes) => {
  const ids = []
  const traverse = (list) => {
    for (const node of list) {
      ids.push(node.id)
      if (node.children && node.children.length > 0) {
        traverse(node.children)
      }
    }
  }
  traverse(nodes)
  return ids
}

const handleCheckAll = (val) => {
  if (val) {
    const allIds = getSelectableIds(menuTreeData.value)
    treeRef.value.setCheckedKeys(allIds)
  } else {
    treeRef.value.setCheckedKeys([])
  }
  isIndeterminate.value = false
}

const setCheckedByMenuIds = (menuIds) => {
  if (!menuIds || !treeRef.value) return
  const ids = menuIds.split(',').map(s => s.trim()).filter(s => s)
  const allSelectable = getSelectableIds(menuTreeData.value)
  const validIds = ids.filter(id => allSelectable.includes(id))
  treeRef.value.setCheckedKeys(validIds)
}

const getCheckedMenuIds = () => {
  if (!treeRef.value) return ''
  const checkedKeys = treeRef.value.getCheckedKeys()
  const halfCheckedKeys = treeRef.value.getHalfCheckedKeys()
  const allIds = [...new Set([...checkedKeys, ...halfCheckedKeys])]
  const allSelectable = getSelectableIds(menuTreeData.value)
  return allIds.filter(id => allSelectable.includes(id)).join(',')
}

const handleAdd = () => {
  Object.assign(form, { id: null, packageName: '', menuIds: '', status: 1, remark: '' })
  dialogVisible.value = true
  nextTick(() => {
    if (treeRef.value) treeRef.value.setCheckedKeys([])
    checkAll.value = false
    isIndeterminate.value = false
  })
}

const handleEdit = (row) => {
  Object.assign(form, { ...row })
  dialogVisible.value = true
  nextTick(() => {
    setCheckedByMenuIds(row.menuIds)
  })
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除套餐「${row.packageName}」吗？`,
      '删除确认',
      { type: 'warning' }
    )
    await deletePackage(row.id)
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

  form.menuIds = getCheckedMenuIds()

  try {
    if (isEdit.value) {
      await updatePackage(form.id, { ...form })
      ElMessage.success('更新成功')
    } else {
      await createPackage({ ...form })
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadData()
  } catch (error) {
    console.error('操作失败:', error)
  }
}

loadData()
loadMenuTree()
</script>

<style scoped>
.page-container { padding: 20px; }
.toolbar { margin-bottom: 20px; }

.menu-tree-wrapper {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  padding: 12px;
  width: 100%;
  max-height: 400px;
  overflow-y: auto;
}

.tree-toolbar {
  margin-bottom: 8px;
  padding-bottom: 8px;
  border-bottom: 1px solid #ebeef5;
}
</style>
