<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card stat-card--primary">
          <div class="stat-body">
            <el-icon class="stat-icon"><Box /></el-icon>
            <div>
              <div class="stat-value">{{ overview.productCount }}</div>
              <div class="stat-label">产品总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card stat-card--success">
          <div class="stat-body">
            <el-icon class="stat-icon"><Monitor /></el-icon>
            <div>
              <div class="stat-value">{{ overview.deviceCount }}</div>
              <div class="stat-label">设备总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card stat-card--online">
          <div class="stat-body">
            <el-icon class="stat-icon"><CircleCheck /></el-icon>
            <div>
              <div class="stat-value">{{ overview.onlineCount }}</div>
              <div class="stat-label">在线设备</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card stat-card--offline">
          <div class="stat-body">
            <el-icon class="stat-icon"><CircleClose /></el-icon>
            <div>
              <div class="stat-value">{{ overview.offlineCount }}</div>
              <div class="stat-label">离线设备</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>设备状态分布</span>
          </template>
          <div class="status-grid">
            <div class="status-item">
              <div class="status-dot status-dot--inactive"></div>
              <div class="status-info">
                <span class="status-count">{{ overview.inactiveCount }}</span>
                <span class="status-text">未激活</span>
              </div>
            </div>
            <div class="status-item">
              <div class="status-dot status-dot--online"></div>
              <div class="status-info">
                <span class="status-count">{{ overview.onlineCount }}</span>
                <span class="status-text">在线</span>
              </div>
            </div>
            <div class="status-item">
              <div class="status-dot status-dot--offline"></div>
              <div class="status-info">
                <span class="status-count">{{ overview.offlineCount }}</span>
                <span class="status-text">离线</span>
              </div>
            </div>
            <div class="status-item">
              <div class="status-dot status-dot--disabled"></div>
              <div class="status-info">
                <span class="status-count">{{ overview.disabledCount }}</span>
                <span class="status-text">已禁用</span>
              </div>
            </div>
          </div>
          <div class="status-bar-wrap" v-if="overview.deviceCount > 0">
            <div class="status-bar">
              <div class="status-bar__segment status-bar--inactive"
                   :style="{ width: barPercent(overview.inactiveCount) }"></div>
              <div class="status-bar__segment status-bar--online"
                   :style="{ width: barPercent(overview.onlineCount) }"></div>
              <div class="status-bar__segment status-bar--offline"
                   :style="{ width: barPercent(overview.offlineCount) }"></div>
              <div class="status-bar__segment status-bar--disabled"
                   :style="{ width: barPercent(overview.disabledCount) }"></div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>各产品设备数量</span>
          </template>
          <el-table :data="overview.productDeviceStats" size="small" stripe
                    v-if="overview.productDeviceStats && overview.productDeviceStats.length > 0">
            <el-table-column prop="productName" label="产品名称" />
            <el-table-column prop="deviceCount" label="设备数" width="100" align="center" />
            <el-table-column label="占比" width="120" align="center">
              <template #default="{ row }">
                {{ overview.deviceCount > 0 ? ((row.deviceCount / overview.deviceCount) * 100).toFixed(1) + '%' : '0%' }}
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-else description="暂无产品数据" :image-size="60" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getDashboardOverview } from '@/api/iot'

const overview = ref({
  productCount: 0,
  deviceCount: 0,
  onlineCount: 0,
  offlineCount: 0,
  inactiveCount: 0,
  disabledCount: 0,
  productDeviceStats: []
})

const barPercent = (count) => {
  if (!overview.value.deviceCount) return '0%'
  return ((count / overview.value.deviceCount) * 100) + '%'
}

onMounted(async () => {
  try {
    const res = await getDashboardOverview()
    overview.value = res.data
  } catch (e) {
    console.error('加载看板数据失败', e)
  }
})
</script>

<style scoped>
.dashboard {
  padding: 20px;
}

.stat-card .stat-body {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon {
  font-size: 40px;
}

.stat-card--primary .stat-icon { color: #409EFF; }
.stat-card--success .stat-icon { color: #67C23A; }
.stat-card--online .stat-icon { color: #409EFF; }
.stat-card--offline .stat-icon { color: #909399; }

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #333;
}

.stat-label {
  color: #666;
  font-size: 13px;
  margin-top: 2px;
}

.status-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  margin-bottom: 20px;
}

.status-item {
  display: flex;
  align-items: center;
  gap: 10px;
}

.status-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
}

.status-dot--inactive { background-color: #E6A23C; }
.status-dot--online { background-color: #67C23A; }
.status-dot--offline { background-color: #909399; }
.status-dot--disabled { background-color: #F56C6C; }

.status-info {
  display: flex;
  flex-direction: column;
}

.status-count {
  font-size: 20px;
  font-weight: bold;
  color: #333;
  line-height: 1.2;
}

.status-text {
  font-size: 12px;
  color: #999;
}

.status-bar-wrap {
  margin-top: 4px;
}

.status-bar {
  display: flex;
  height: 8px;
  border-radius: 4px;
  overflow: hidden;
  background-color: #f0f0f0;
}

.status-bar__segment {
  transition: width 0.3s;
  min-width: 2px;
}

.status-bar--inactive { background-color: #E6A23C; }
.status-bar--online { background-color: #67C23A; }
.status-bar--offline { background-color: #909399; }
.status-bar--disabled { background-color: #F56C6C; }
</style>
