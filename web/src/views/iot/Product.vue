<template>
  <div class="page-container">
    <div class="toolbar">
      <el-input v-model="searchName" placeholder="产品名称" clearable style="width: 200px; margin-right: 10px" @clear="loadData" @keyup.enter="loadData" />
      <el-button type="primary" @click="loadData">搜索</el-button>
      <el-button type="primary" @click="handleAdd">新增产品</el-button>
    </div>

    <el-table :data="tableData" v-loading="loading" border stripe>
      <el-table-column type="index" label="#" width="60" :index="(i) => (pagination.page - 1) * pagination.size + i + 1" />
      <el-table-column prop="name" label="产品名称" min-width="150" />
      <el-table-column prop="productKey" label="ProductKey" min-width="160" />
      <el-table-column prop="nodeType" label="节点类型" width="100">
        <template #default="{ row }">
          <el-tag v-if="row.nodeType === 0">直连设备</el-tag>
          <el-tag v-else-if="row.nodeType === 1" type="warning">网关</el-tag>
          <el-tag v-else type="info">子设备</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="protocol" label="协议" width="100" />
      <el-table-column prop="status" label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'">
            {{ row.status === 1 ? '已启用' : '已禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="modelStatus" label="物模型" width="100">
        <template #default="{ row }">
          <el-tag v-if="row.modelStatus === 1" type="success" size="small">已发布</el-tag>
          <el-tag v-else-if="row.modelStatus === 2" type="danger" size="small">已禁用</el-tag>
          <el-tag v-else size="small">草稿</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column label="操作" width="280" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
          <el-button link type="primary" @click="handleThingModel(row)">物模型</el-button>
          <el-button link type="primary" @click="handlePublish(row)" :disabled="row.modelStatus === 1">发布</el-button>
          <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="650px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="产品名称" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="ProductKey">
          <el-input v-model="form.productKey" placeholder="留空自动生成" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="节点类型" prop="nodeType">
          <el-select v-model="form.nodeType" style="width: 100%">
            <el-option label="直连设备" :value="0" />
            <el-option label="网关" :value="1" />
            <el-option label="子设备" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="通信协议" prop="protocol">
          <el-select v-model="form.protocol" style="width: 100%">
            <el-option label="MQTT" value="MQTT" />
            <el-option label="HTTP" value="HTTP" />
            <el-option label="CoAP" value="CoAP" />
          </el-select>
        </el-form-item>
        <el-form-item label="数据格式" prop="dataFormat">
          <el-select v-model="form.dataFormat" style="width: 100%">
            <el-option label="Alink JSON" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="thingModelVisible" title="物模型编辑" width="900px" top="3vh" destroy-on-close>
      <ThingModelEditor v-model="thingModelJson" />
      <template #footer>
        <el-button @click="thingModelVisible = false">关闭</el-button>
        <el-button type="primary" @click="handleSaveThingModel">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getProductList, addProduct, updateProduct, deleteProduct, getThingModel, updateThingModel, publishThingModel } from '@/api/iot'
import ThingModelEditor from './ThingModelEditor.vue'

const loading = ref(false)
const tableData = ref([])
const searchName = ref('')
const dialogVisible = ref(false)
const thingModelVisible = ref(false)
const formRef = ref()
const thingModelJson = ref('')
const currentProductId = ref(null)
const isEdit = computed(() => !!form.id)
const dialogTitle = computed(() => isEdit.value ? '编辑产品' : '新增产品')

const pagination = reactive({ page: 1, size: 10, total: 0 })

const form = reactive({
  id: null,
  name: '',
  code: '',
  nodeType: 0,
  protocol: 'MQTT',
  dataFormat: 0,
  description: '',
  status: 1
})

const rules = {
  name: [{ required: true, message: '请输入产品名称', trigger: 'blur' }],
  nodeType: [{ required: true, message: '请选择节点类型', trigger: 'change' }],
  protocol: [{ required: true, message: '请选择通信协议', trigger: 'change' }]
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getProductList({ page: pagination.page, size: pagination.size, name: searchName.value || undefined })
    tableData.value = res.data?.records || []
    pagination.total = res.data?.total || 0
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  Object.assign(form, { id: null, name: '', productKey: '', nodeType: 0, protocol: 'MQTT', dataFormat: 0, description: '', status: 1 })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  Object.assign(form, { ...row })
  dialogVisible.value = true
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该产品吗？关联的设备也会被删除。', '提示', { type: 'warning' })
    await deleteProduct(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) {
    if (e !== 'cancel') console.error(e)
  }
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  try {
    if (isEdit.value) {
      await updateProduct(form.id, form)
      ElMessage.success('更新成功')
    } else {
      await addProduct(form)
      ElMessage.success('添加成功')
    }
    dialogVisible.value = false
    loadData()
  } catch (e) {
    console.error(e)
  }
}

const handleThingModel = async (row) => {
  currentProductId.value = row.id
  try {
    const res = await getThingModel(row.id)
    thingModelJson.value = res.data || ''
  } catch (e) {
    thingModelJson.value = ''
  }
  thingModelVisible.value = true
}

const handleSaveThingModel = async () => {
  try {
    await updateThingModel(currentProductId.value, thingModelJson.value)
    ElMessage.success('物模型保存成功')
    thingModelVisible.value = false
    loadData()
  } catch (e) {
    console.error(e)
  }
}

const handlePublish = async (row) => {
  try {
    await ElMessageBox.confirm('确定要发布该产品的物模型吗？发布后不可修改。', '提示', { type: 'warning' })
    await publishThingModel(row.id)
    ElMessage.success('物模型发布成功')
    loadData()
  } catch (e) {
    if (e !== 'cancel') console.error(e)
  }
}

loadData()
</script>

<style scoped>
.page-container { padding: 20px; }
.toolbar { margin-bottom: 20px; display: flex; align-items: center; }
</style>
