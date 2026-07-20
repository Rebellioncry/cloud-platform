<template>
  <div class="page-container">
    <div class="toolbar">
      <el-input v-model="searchName" placeholder="规则名称" clearable style="width: 200px; margin-right: 10px" @clear="loadData" @keyup.enter="loadData" />
      <el-button type="primary" @click="loadData">搜索</el-button>
      <el-button type="primary" @click="handleAdd">新增规则</el-button>
      <div style="margin-left: auto">
        <el-button-group>
          <el-button :type="viewMode === 'card' ? 'primary' : ''" @click="viewMode = 'card'" size="small">卡片</el-button>
          <el-button :type="viewMode === 'table' ? 'primary' : ''" @click="viewMode = 'table'" size="small">列表</el-button>
        </el-button-group>
      </div>
    </div>

    <!-- 卡片视图 -->
    <template v-if="viewMode === 'card'">
      <div v-if="tableData.length === 0 && !loading" class="empty-tip">暂无规则数据</div>
      <div class="card-grid" v-loading="loading">
        <div v-for="row in tableData" :key="row.id" class="rule-card" :class="{ 'card-disabled': row.status === 0 }">
          <div class="card-top" :style="{ borderLeftColor: row.status === 1 ? '#67c23a' : '#909399' }">
            <div class="card-title-row">
              <span class="card-title">{{ row.name }}</span>
              <el-tag v-if="row.status === 1" type="success" size="small">运行中</el-tag>
              <el-tag v-else type="info" size="small">已停止</el-tag>
            </div>
            <div class="card-desc">{{ row.description || '暂无描述' }}</div>
          </div>
          <div class="card-body">
            <div class="card-info-row">
              <span class="info-label">规则类型</span>
              <el-tag v-if="row.ruleType === 0" size="small">设备触发</el-tag>
              <el-tag v-else-if="row.ruleType === 1" type="warning" size="small">定时触发</el-tag>
              <el-tag v-else type="info" size="small">外部触发</el-tag>
            </div>
            <div class="card-info-row">
              <span class="info-label">匹配次数</span>
              <span class="info-value">{{ row.matchCount || 0 }}</span>
            </div>
            <div class="card-info-row">
              <span class="info-label">最后执行</span>
              <span class="info-value">{{ row.lastExecuteTime || '从未执行' }}</span>
            </div>
            <div class="card-info-row">
              <span class="info-label">创建时间</span>
              <span class="info-value">{{ row.createTime }}</span>
            </div>
          </div>
          <div class="card-footer">
            <el-button link type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="primary" size="small" @click="handleDesigner(row)">设计</el-button>
            <el-button link type="primary" size="small" @click="handleViewLogs(row)">日志</el-button>
            <el-button
              v-if="row.status === 0"
              link type="success" size="small"
              @click="handleStart(row)"
            >启动</el-button>
            <el-button
              v-else
              link type="warning" size="small"
              @click="handleStop(row)"
            >停止</el-button>
            <el-button link type="danger" size="small" :disabled="row.status === 1" @click="handleDelete(row)">删除</el-button>
          </div>
        </div>
      </div>
    </template>

    <!-- 表格视图 -->
    <template v-else>
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column type="index" label="#" width="60" :index="(i) => (pagination.page - 1) * pagination.size + i + 1" />
        <el-table-column prop="name" label="规则名称" min-width="150" />
        <el-table-column prop="ruleType" label="规则类型" width="110">
          <template #default="{ row }">
            <el-tag v-if="row.ruleType === 0" size="small">设备触发</el-tag>
            <el-tag v-else-if="row.ruleType === 1" type="warning" size="small">定时触发</el-tag>
            <el-tag v-else type="info" size="small">外部触发</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '运行中' : '已停止' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="matchCount" label="匹配次数" width="100" />
        <el-table-column prop="lastExecuteTime" label="最后执行" width="180">
          <template #default="{ row }">
            {{ row.lastExecuteTime || '从未执行' }}
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="300" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="primary" @click="handleDesigner(row)">设计</el-button>
            <el-button link type="primary" @click="handleViewLogs(row)">日志</el-button>
            <el-button
              v-if="row.status === 0"
              link type="success"
              @click="handleStart(row)"
            >启动</el-button>
            <el-button
              v-else
              link type="warning"
              @click="handleStop(row)"
            >停止</el-button>
            <el-button link type="danger" :disabled="row.status === 1" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </template>

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

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="550px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="规则名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入规则名称" />
        </el-form-item>
        <el-form-item label="规则类型" prop="ruleType">
          <el-select v-model="form.ruleType" style="width: 100%">
            <el-option label="设备触发" :value="0" />
            <el-option label="定时触发" :value="1" />
            <el-option label="外部触发" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入规则描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 执行日志抽屉 -->
    <el-drawer v-model="logDrawerVisible" :title="'规则日志 - ' + logRuleName" size="70%" direction="rtl">
      <el-table :data="logData" v-loading="logLoading" border stripe size="small" :row-style="{ background: '#132238' }" :header-cell-style="{ background: '#0f1c32', color: '#e2e8f0' }">
        <el-table-column prop="nodeName" label="节点名称" min-width="120" />
        <el-table-column prop="nodeType" label="节点类型" width="110">
          <template #default="{ row }">
            <el-tag v-if="row.nodeType === 'trigger'" type="warning" size="small">触发</el-tag>
            <el-tag v-else-if="row.nodeType === 'transform'" size="small">转换</el-tag>
            <el-tag v-else-if="row.nodeType === 'action'" type="success" size="small">动作</el-tag>
            <el-tag v-else type="info" size="small">{{ row.nodeType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 0 ? 'success' : 'danger'" size="small">
              {{ row.status === 0 ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="duration" label="耗时(ms)" width="90" />
        <el-table-column prop="deviceKey" label="设备Key" min-width="140" show-overflow-tooltip />
        <el-table-column prop="productKey" label="产品Key" min-width="140" show-overflow-tooltip />
        <el-table-column prop="executeTime" label="执行时间" width="180" />
        <el-table-column label="详情" width="80">
          <template #default="{ row }">
            <el-popover trigger="hover" placement="left" :width="400">
              <div style="max-height: 300px; overflow-y: auto; font-size: 12px;">
                <div v-if="row.inputData"><b>输入：</b><pre style="white-space:pre-wrap;margin:4px 0">{{ row.inputData }}</pre></div>
                <div v-if="row.outputData"><b>输出：</b><pre style="white-space:pre-wrap;margin:4px 0">{{ row.outputData }}</pre></div>
                <div v-if="row.errorMessage" style="color:#f56c6c"><b>错误：</b>{{ row.errorMessage }}</div>
              </div>
              <template #reference>
                <el-button link type="primary" size="small">查看</el-button>
              </template>
            </el-popover>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        v-model:current-page="logPagination.page"
        v-model:page-size="logPagination.size"
        :total="logPagination.total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @size-change="loadLogs(logData.length > 0 ? logData[0].ruleId : '')"
        @current-change="loadLogs(logData.length > 0 ? logData[0].ruleId : '')"
        style="margin-top: 16px; justify-content: flex-end"
      />
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getRuleList, addRule, updateRule, deleteRule, startRule, stopRule, getRuleLogList } from '@/api/iot'

const router = useRouter()
const loading = ref(false)
const tableData = ref([])
const searchName = ref('')
const dialogVisible = ref(false)
const formRef = ref()
const viewMode = ref('card')
const isEdit = computed(() => !!form.id)
const dialogTitle = computed(() => isEdit.value ? '编辑规则' : '新增规则')

const pagination = reactive({ page: 1, size: 10, total: 0 })

const logDrawerVisible = ref(false)
const logRuleName = ref('')
const logLoading = ref(false)
const logData = ref([])
const logPagination = reactive({ page: 1, size: 20, total: 0 })

const form = reactive({
  id: null,
  name: '',
  ruleType: 0,
  description: '',
  status: 0
})

const rules = {
  name: [{ required: true, message: '请输入规则名称', trigger: 'blur' }],
  ruleType: [{ required: true, message: '请选择规则类型', trigger: 'change' }]
}

const handleViewLogs = async (row) => {
  logRuleName.value = row.name
  logDrawerVisible.value = true
  logPagination.page = 1
  await loadLogs(row.id)
}

const loadLogs = async (ruleId) => {
  logLoading.value = true
  try {
    const res = await getRuleLogList({
      page: logPagination.page,
      size: logPagination.size,
      ruleId: ruleId
    })
    logData.value = res.data?.records || []
    logPagination.total = res.data?.total || 0
  } catch (e) {
    console.error(e)
  } finally {
    logLoading.value = false
  }
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getRuleList({ page: pagination.page, size: pagination.size, name: searchName.value || undefined })
    tableData.value = res.data?.records || []
    pagination.total = res.data?.total || 0
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  Object.assign(form, { id: null, name: '', ruleType: 0, description: '', status: 0 })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  Object.assign(form, { ...row })
  dialogVisible.value = true
}

const handleDesigner = (row) => {
  router.push({ path: `/iot/rule/${row.id}/design` })
}

const handleDelete = async (row) => {
  if (row.status === 1) {
    ElMessage.warning('运行中的规则不能删除，请先停止')
    return
  }
  try {
    await ElMessageBox.confirm(
      `确定要删除规则「${row.name}」吗？此操作不可恢复。`,
      '删除确认',
      { type: 'warning', confirmButtonText: '确定删除', cancelButtonText: '取消' }
    )
    await deleteRule(row.id)
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
      await updateRule(form.id, form)
      ElMessage.success('更新成功')
    } else {
      await addRule(form)
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
    await ElMessageBox.confirm(
      `确定要启动规则「${row.name}」吗？`,
      '启动确认',
      { type: 'info', confirmButtonText: '确定', cancelButtonText: '取消' }
    )
    await startRule(row.id)
    ElMessage.success('规则已启动')
    loadData()
  } catch (e) {
    if (e !== 'cancel') console.error(e)
  }
}

const handleStop = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要停止规则「${row.name}」吗？停止后规则将不再处理数据。`,
      '停止确认',
      { type: 'warning', confirmButtonText: '确定', cancelButtonText: '取消' }
    )
    await stopRule(row.id)
    ElMessage.success('规则已停止')
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
.empty-tip { text-align: center; color: #5a6d80; padding: 60px 0; }

/* ===== 卡片视图 ===== */
.card-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 16px;
}

.rule-card {
  background: #132238;
  border: 1px solid #1e3350;
  border-radius: 12px;
  overflow: hidden;
  transition: border-color 0.25s, box-shadow 0.25s;
}
.rule-card:hover {
  border-color: #2a4060;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.25);
}
.rule-card.card-disabled {
  opacity: 0.7;
}

.card-top {
  padding: 16px 18px 12px;
  border-left: 3px solid #67c23a;
}
.card-title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 4px;
}
.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #e2e8f0;
}
.card-desc {
  font-size: 12px;
  color: #5a6d80;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.card-body {
  padding: 0 18px 12px;
}
.card-info-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 6px 0;
  border-bottom: 1px solid rgba(30, 51, 80, 0.5);
}
.card-info-row:last-child {
  border-bottom: none;
}
.info-label {
  font-size: 13px;
  color: #8899aa;
}
.info-value {
  font-size: 13px;
  color: #c8d6e5;
}

.card-footer {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 10px 18px;
  border-top: 1px solid #1e3350;
  background: rgba(10, 22, 40, 0.3);
}
</style>
