<template>
  <div class="page-container">
    <div class="toolbar">
      <el-input v-model="searchName" placeholder="配置名称" clearable style="width: 200px; margin-right: 10px" @clear="loadData" @keyup.enter="loadData" />
      <el-button type="primary" @click="loadData">搜索</el-button>
      <el-button type="primary" @click="handleAdd">新增配置</el-button>
    </div>

    <el-table :data="tableData" v-loading="loading" border stripe>
      <el-table-column type="index" label="#" width="60" :index="(i) => (pagination.page - 1) * pagination.size + i + 1" />
      <el-table-column prop="name" label="配置名称" min-width="150" />
      <el-table-column prop="broker" label="Broker地址" min-width="160" />
      <el-table-column prop="port" label="端口" width="80" />
      <el-table-column prop="qos" label="QoS" width="80" />
      <el-table-column prop="keepAlive" label="心跳(s)" width="80" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag v-if="row.status === 1" type="success" size="small">
            <span class="status-dot running"></span>运行中
          </el-tag>
          <el-tag v-else size="small">已停止</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column label="操作" width="280" fixed="right">
        <template #default="{ row }">
          <el-button v-if="row.status !== 1" link type="success" @click="handleStart(row)">启动</el-button>
          <el-button v-else link type="warning" @click="handleStop(row)">停止</el-button>
          <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
          <el-button link type="danger" @click="handleDelete(row)" :disabled="row.status === 1">删除</el-button>
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
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-form-item label="配置名称" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="Broker地址" prop="broker">
          <el-input v-model="form.broker" placeholder="如: 127.0.0.1 或 broker.emqx.io" />
        </el-form-item>
        <el-form-item label="端口" prop="port">
          <el-input-number v-model="form.port" :min="1" :max="65535" />
        </el-form-item>
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" show-password />
        </el-form-item>
        <el-form-item label="ClientID前缀">
          <el-input v-model="form.clientIdPrefix" placeholder="如: iot-server-" />
        </el-form-item>
        <el-form-item label="共享组名">
          <el-input v-model="form.sharedGroup" placeholder="共享订阅组名，默认: iot-service" />
        </el-form-item>
        <el-form-item label="QoS">
          <el-radio-group v-model="form.qos">
            <el-radio :label="0">0</el-radio>
            <el-radio :label="1">1</el-radio>
            <el-radio :label="2">2</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="心跳间隔(s)">
          <el-input-number v-model="form.keepAlive" :min="10" :max="600" />
        </el-form-item>
        <el-form-item label="自动重连">
          <el-switch v-model="form.autoReconnect" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="启用SSL">
          <el-switch v-model="form.useSsl" :active-value="1" :inactive-value="0" />
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
import { getMqttConfigList, addMqttConfig, updateMqttConfig, deleteMqttConfig, startMqttConfig, stopMqttConfig } from '@/api/iot'

const loading = ref(false)
const tableData = ref([])
const searchName = ref('')
const dialogVisible = ref(false)
const formRef = ref()
const isEdit = computed(() => !!form.id)
const dialogTitle = computed(() => isEdit.value ? '编辑MQTT配置' : '新增MQTT配置')

const pagination = reactive({ page: 1, size: 10, total: 0 })

const form = reactive({
  id: null,
  name: '',
  description: '',
  broker: '127.0.0.1',
  port: 1883,
  username: '',
  password: '',
  clientIdPrefix: '',
  sharedGroup: 'iot-service',
  qos: 1,
  keepAlive: 60,
  autoReconnect: 1,
  useSsl: 0
})

const rules = {
  name: [{ required: true, message: '请输入配置名称', trigger: 'blur' }],
  broker: [{ required: true, message: '请输入Broker地址', trigger: 'blur' }],
  port: [{ required: true, message: '请输入端口', trigger: 'blur' }]
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getMqttConfigList({ page: pagination.page, size: pagination.size, name: searchName.value || undefined })
    tableData.value = res.data?.records || []
    pagination.total = res.data?.total || 0
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  Object.assign(form, {
    id: null, name: '', description: '', broker: '127.0.0.1', port: 1883,
    username: '', password: '', clientIdPrefix: '', sharedGroup: 'iot-service', qos: 1,
    keepAlive: 60, autoReconnect: 1, useSsl: 0
  })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  Object.assign(form, { ...row })
  dialogVisible.value = true
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该MQTT配置吗？', '提示', { type: 'warning' })
    await deleteMqttConfig(row.id)
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
      await updateMqttConfig(form.id, form)
      ElMessage.success('更新成功')
    } else {
      await addMqttConfig(form)
      ElMessage.success('添加成功')
    }
    dialogVisible.value = false
    loadData()
  } catch (e) {
    console.error(e)
  }
}

const handleStart = async (row) => {
  try {
    await startMqttConfig(row.id)
    ElMessage.success('MQTT客户端已启动')
    loadData()
  } catch (e) {
    console.error(e)
  }
}

const handleStop = async (row) => {
  try {
    await ElMessageBox.confirm('确定要停止该MQTT客户端吗？', '提示', { type: 'warning' })
    await stopMqttConfig(row.id)
    ElMessage.success('MQTT客户端已停止')
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
.status-dot { display: inline-block; width: 6px; height: 6px; border-radius: 50%; margin-right: 4px; vertical-align: middle; }
.status-dot.running { background-color: #67c23a; }
</style>
