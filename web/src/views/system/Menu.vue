<template>
  <div class="page-container">
    <div class="toolbar">
      <el-button type="primary" @click="handleAdd(null)">新增菜单</el-button>
    </div>
    
    <el-table :data="tableData" v-loading="loading" border stripe row-key="id" default-expand-all>
      <el-table-column prop="menuName" label="菜单名称" width="200" />
      <el-table-column prop="menuType" label="类型" width="100">
        <template #default="{ row }">
          <el-tag v-if="row.menuType === 0">目录</el-tag>
          <el-tag v-else-if="row.menuType === 1" type="success">菜单</el-tag>
          <el-tag v-else type="warning">按钮</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="path" label="路由路径" />
      <el-table-column prop="component" label="组件路径" />
      <el-table-column prop="perms" label="权限标识" />
      <el-table-column prop="icon" label="图标" width="100" />
      <el-table-column prop="orderNum" label="排序" width="80" />
      <el-table-column prop="visible" label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.visible === 1 ? 'success' : 'danger'">
            {{ row.visible === 1 ? '显示' : '隐藏' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="handleAdd(row)">新增</el-button>
          <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
          <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="菜单类型" prop="menuType">
          <el-radio-group v-model="form.menuType">
            <el-radio :label="0">目录</el-radio>
            <el-radio :label="1">菜单</el-radio>
            <el-radio :label="2">按钮</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="上级菜单" prop="parentId">
          <el-tree-select
            v-model="form.parentId"
            :data="menuTreeData"
            :props="{ label: 'menuName', value: 'id', children: 'children' }"
            check-strictly
            placeholder="选择上级菜单"
          />
        </el-form-item>
        <el-form-item label="菜单名称" prop="menuName">
          <el-input v-model="form.menuName" />
        </el-form-item>
        <el-form-item label="路由路径" prop="path" v-if="form.menuType !== 2">
          <el-input v-model="form.path" />
        </el-form-item>
        <el-form-item label="组件路径" prop="component" v-if="form.menuType === 1">
          <el-input v-model="form.component" placeholder="如: system/user/index" />
        </el-form-item>
        <el-form-item label="权限标识" prop="perms" v-if="form.menuType === 2">
          <el-input v-model="form.perms" />
        </el-form-item>
        <el-form-item label="图标" prop="icon">
          <el-input v-model="form.icon" />
        </el-form-item>
        <el-form-item label="排序" prop="orderNum">
          <el-input-number v-model="form.orderNum" :min="0" />
        </el-form-item>
        <el-form-item label="状态" prop="visible">
          <el-radio-group v-model="form.visible">
            <el-radio :label="1">显示</el-radio>
            <el-radio :label="0">隐藏</el-radio>
          </el-radio-group>
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
import { getMenuTree, addMenu, updateMenu, deleteMenu } from '@/api/system'

const loading = ref(false)
const tableData = ref([])
const menuTreeData = ref([])
const dialogVisible = ref(false)
const formRef = ref()
const isEdit = computed(() => !!form.id)
const dialogTitle = computed(() => isEdit.value ? '编辑菜单' : '新增菜单')

const form = reactive({
  id: null,
  parentId: 0,
  menuType: 1,
  menuName: '',
  path: '',
  component: '',
  perms: '',
  icon: '',
  orderNum: 0,
  visible: 1
})

const rules = {
  menuName: [{ required: true, message: '请输入菜单名称', trigger: 'blur' }]
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getMenuTree()
    tableData.value = res.data || []
    menuTreeData.value = [{ id: 0, menuName: '顶级菜单', children: res.data || [] }]
  } catch (error) {
    console.error('加载数据失败:', error)
  } finally {
    loading.value = false
  }
}

const handleAdd = (parent) => {
  Object.assign(form, { id: null, parentId: parent?.id || 0, menuType: 1, menuName: '', path: '', component: '', perms: '', icon: '', orderNum: 0, visible: 1 })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  Object.assign(form, { ...row })
  dialogVisible.value = true
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该菜单吗？', '提示', { type: 'warning' })
    await deleteMenu(row.id)
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
      await updateMenu(form)
      ElMessage.success('更新成功')
    } else {
      await addMenu(form)
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
