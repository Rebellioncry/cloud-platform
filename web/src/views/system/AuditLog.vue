<template>
  <div class="audit-log">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>审计日志</span>
        </div>
      </template>

      <el-form :inline="true" :model="query" class="search-form">
        <el-form-item label="用户名">
          <el-input v-model="query.username" placeholder="用户名" clearable style="width: 140px" />
        </el-form-item>
        <el-form-item label="模块">
          <el-select v-model="query.module" placeholder="全部" clearable style="width: 130px">
            <el-option label="产品管理" value="产品管理" />
            <el-option label="设备管理" value="设备管理" />
            <el-option label="MQTT配置" value="MQTT配置" />
            <el-option label="用户管理" value="用户管理" />
            <el-option label="角色管理" value="角色管理" />
            <el-option label="菜单管理" value="菜单管理" />
            <el-option label="租户管理" value="租户管理" />
            <el-option label="认证" value="认证" />
            <el-option label="看板" value="看板" />
          </el-select>
        </el-form-item>
        <el-form-item label="方法">
          <el-select v-model="query.httpMethod" placeholder="全部" clearable style="width: 100px">
            <el-option label="GET" value="GET" />
            <el-option label="POST" value="POST" />
            <el-option label="PUT" value="PUT" />
            <el-option label="DELETE" value="DELETE" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width: 100px">
            <el-option label="成功" :value="1" />
            <el-option label="失败" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item label="URL">
          <el-input v-model="query.url" placeholder="请求路径" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData(1)">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="list" v-loading="loading" stripe size="default">
        <el-table-column prop="username" label="用户" width="100" />
        <el-table-column prop="module" label="模块" width="100" />
        <el-table-column prop="httpMethod" label="方法" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="methodType(row.httpMethod)" size="small">{{ row.httpMethod }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="url" label="请求路径" min-width="200" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="duration" label="耗时" width="80" align="center">
          <template #default="{ row }">
            <span :class="{ 'text-warn': row.duration > 1000 }">{{ row.duration }}ms</span>
          </template>
        </el-table-column>
        <el-table-column prop="ip" label="IP" width="130" />
        <el-table-column prop="createTime" label="时间" width="170">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="详情" width="80" align="center">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="showDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="query.page"
        v-model:page-size="query.size"
        :page-sizes="[20, 50, 100]"
        :total="total"
        layout="total, sizes, prev, pager, next"
        style="margin-top: 16px; justify-content: flex-end"
        @size-change="loadData(1)"
        @current-change="loadData"
      />
    </el-card>

    <el-dialog v-model="detailVisible" title="请求详情" width="700px">
      <el-descriptions :column="2" border size="small">
        <el-descriptions-item label="用户">{{ detail.username }}</el-descriptions-item>
        <el-descriptions-item label="模块">{{ detail.module }}</el-descriptions-item>
        <el-descriptions-item label="HTTP方法">{{ detail.httpMethod }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="detail.status === 1 ? 'success' : 'danger'" size="small">
            {{ detail.status === 1 ? '成功' : '失败' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="请求路径" :span="2">{{ detail.url }}</el-descriptions-item>
        <el-descriptions-item label="方法名" :span="2">{{ detail.method }}</el-descriptions-item>
        <el-descriptions-item label="IP">{{ detail.ip }}</el-descriptions-item>
        <el-descriptions-item label="耗时">{{ detail.duration }}ms</el-descriptions-item>
        <el-descriptions-item label="时间" :span="2">{{ formatTime(detail.createTime) }}</el-descriptions-item>
      </el-descriptions>

      <div class="detail-section">
        <div class="detail-title">请求参数</div>
        <pre class="detail-code">{{ detail.params ? JSON.stringify(detail.params, null, 2) : '无' }}</pre>
      </div>
      <div class="detail-section">
        <div class="detail-title">返回结果</div>
        <pre class="detail-code">{{ detail.result || '无' }}</pre>
      </div>
      <div class="detail-section" v-if="detail.errorMsg">
        <div class="detail-title" style="color: #F56C6C">错误信息</div>
        <pre class="detail-code detail-code--error">{{ detail.errorMsg }}</pre>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getAuditLogList } from '@/api/system'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const query = reactive({
  page: 1,
  size: 20,
  username: '',
  module: '',
  httpMethod: '',
  status: null,
  url: ''
})

const detailVisible = ref(false)
const detail = ref({})

const loadData = async (page) => {
  if (page) query.page = page
  loading.value = true
  try {
    const params = {}
    Object.keys(query).forEach(k => {
      if (query[k] !== null && query[k] !== '' && query[k] !== undefined) {
        params[k] = query[k]
      }
    })
    const res = await getAuditLogList(params)
    list.value = res.data.list
    total.value = res.data.total
  } catch (e) {
    console.error('加载审计日志失败', e)
  } finally {
    loading.value = false
  }
}

const resetQuery = () => {
  query.username = ''
  query.module = ''
  query.httpMethod = ''
  query.status = null
  query.url = ''
  query.page = 1
  loadData(1)
}

const showDetail = (row) => {
  detail.value = row
  detailVisible.value = true
}

const methodType = (m) => {
  if (m === 'GET') return 'success'
  if (m === 'POST') return 'primary'
  if (m === 'PUT') return 'warning'
  if (m === 'DELETE') return 'danger'
  return 'info'
}

const formatTime = (t) => {
  if (!t) return ''
  return t.replace('T', ' ').substring(0, 19)
}

onMounted(() => loadData())
</script>

<style scoped>
.search-form {
  margin-bottom: 16px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.text-warn {
  color: #E6A23C;
  font-weight: bold;
}
.detail-section {
  margin-top: 16px;
}
.detail-title {
  font-size: 14px;
  font-weight: bold;
  color: #333;
  margin-bottom: 8px;
}
.detail-code {
  background: #f5f7fa;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  padding: 12px;
  font-size: 12px;
  max-height: 200px;
  overflow: auto;
  white-space: pre-wrap;
  word-break: break-all;
  margin: 0;
}
.detail-code--error {
  background: #fef0f0;
  border-color: #fbc4c4;
  color: #F56C6C;
}
</style>
