<template>
  <div class="dashboard">
    <div class="dash-welcome">
      <h2>欢迎使用物联网平台</h2>
      <p>实时监控设备状态，掌握全局动态</p>
    </div>

    <el-row :gutter="16">
      <el-col :span="6" v-for="card in statCards" :key="card.label">
        <div class="stat-card" :style="{ '--accent': card.color }" @click="navigateTo(card)" style="cursor: pointer">
          <div class="stat-card__icon-wrap">
            <el-icon :size="28"><component :is="card.icon" /></el-icon>
          </div>
          <div class="stat-card__info">
            <div class="stat-card__value">{{ card.value }}</div>
            <div class="stat-card__label">{{ card.label }}</div>
          </div>
          <div class="stat-card__bg-icon">
            <el-icon :size="80"><component :is="card.icon" /></el-icon>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="chart-row">
      <el-col :span="8">
        <div class="chart-card">
          <div class="chart-card__header">
            <span class="chart-card__title">设备状态分布</span>
          </div>
          <v-chart class="chart" :option="pieOption" autoresize />
        </div>
      </el-col>
      <el-col :span="8">
        <div class="chart-card">
          <div class="chart-card__header">
            <span class="chart-card__title">各产品设备数量</span>
          </div>
          <v-chart class="chart" :option="barOption" autoresize />
        </div>
      </el-col>
      <el-col :span="8">
        <div class="chart-card">
          <div class="chart-card__header">
            <span class="chart-card__title">近7日设备增长趋势</span>
          </div>
          <v-chart class="chart" :option="lineOption" autoresize />
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { PieChart, BarChart, LineChart } from 'echarts/charts'
import {
  TitleComponent, TooltipComponent, LegendComponent,
  GridComponent
} from 'echarts/components'
import VChart from 'vue-echarts'
import { Box, Monitor, CircleCheck, CircleClose } from '@element-plus/icons-vue'
import { getDashboardOverview } from '@/api/iot'

use([
  CanvasRenderer, PieChart, BarChart, LineChart,
  TitleComponent, TooltipComponent, LegendComponent, GridComponent
])

const router = useRouter()

const navigateTo = (card) => {
  const routeMap = {
    '产品总数': '/iot/product',
    '设备总数': '/iot/device',
    '在线设备': '/iot/device?status=1',
    '离线设备': '/iot/device?status=2'
  }
  const path = routeMap[card.label]
  if (path) router.push(path)
}

const overview = ref({
  productCount: 0, deviceCount: 0,
  onlineCount: 0, offlineCount: 0,
  inactiveCount: 0, disabledCount: 0,
  productDeviceStats: [], deviceTrend: []
})

const statCards = computed(() => [
  { label: '产品总数', value: overview.value.productCount, icon: Box, color: '#6366f1' },
  { label: '设备总数', value: overview.value.deviceCount, icon: Monitor, color: '#0ea5e9' },
  { label: '在线设备', value: overview.value.onlineCount, icon: CircleCheck, color: '#22c55e' },
  { label: '离线设备', value: overview.value.offlineCount, icon: CircleClose, color: '#f43f5e' }
])

const chartTextColor = '#8899aa'

const pieOption = computed(() => ({
  tooltip: {
    trigger: 'item',
    backgroundColor: 'rgba(10, 22, 40, 0.9)',
    borderColor: 'rgba(64, 158, 255, 0.3)',
    textStyle: { color: '#c8d6e5' },
    formatter: '{b}: {c} ({d}%)'
  },
  legend: { bottom: 0, textStyle: { fontSize: 12, color: chartTextColor }, itemGap: 16 },
  color: ['#E6A23C', '#22c55e', '#64748b', '#f43f5e'],
  series: [{
    type: 'pie', radius: ['42%', '72%'], center: ['50%', '44%'],
    avoidLabelOverlap: false,
    itemStyle: { borderRadius: 8, borderColor: '#0f1d2e', borderWidth: 3 },
    label: { show: false },
    emphasis: {
      label: { show: true, fontSize: 14, fontWeight: 'bold', color: '#e2e8f0' },
      itemStyle: { shadowBlur: 20, shadowColor: 'rgba(64, 158, 255, 0.3)' }
    },
    data: [
      { value: overview.value.inactiveCount, name: '未激活' },
      { value: overview.value.onlineCount, name: '在线' },
      { value: overview.value.offlineCount, name: '离线' },
      { value: overview.value.disabledCount, name: '已禁用' }
    ]
  }]
}))

const barOption = computed(() => {
  const stats = overview.value.productDeviceStats || []
  return {
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(10, 22, 40, 0.9)',
      borderColor: 'rgba(64, 158, 255, 0.3)',
      textStyle: { color: '#c8d6e5' }
    },
    grid: { left: 8, right: 12, bottom: 36, top: 12, containLabel: true },
    xAxis: {
      type: 'category', data: stats.map(s => s.productName),
      axisLabel: { rotate: 30, fontSize: 11, color: chartTextColor },
      axisLine: { lineStyle: { color: '#1e2d3d' } },
      axisTick: { show: false }
    },
    yAxis: {
      type: 'value', minInterval: 1,
      axisLabel: { color: chartTextColor },
      splitLine: { lineStyle: { color: '#1e2d3d', type: 'dashed' } }
    },
    series: [{
      type: 'bar', barWidth: '50%',
      data: stats.map(s => s.deviceCount),
      itemStyle: {
        borderRadius: [6, 6, 0, 0],
        color: { type: 'linear', x: 0, y: 0, x2: 0, y2: 1, colorStops: [
          { offset: 0, color: '#6366f1' }, { offset: 1, color: '#3b82f6' }
        ]}
      }
    }]
  }
})

const lineOption = computed(() => {
  const trend = overview.value.deviceTrend || []
  return {
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(10, 22, 40, 0.9)',
      borderColor: 'rgba(64, 158, 255, 0.3)',
      textStyle: { color: '#c8d6e5' }
    },
    grid: { left: 8, right: 12, bottom: 8, top: 12, containLabel: true },
    xAxis: {
      type: 'category', data: trend.map(t => t.date), boundaryGap: false,
      axisLabel: { color: chartTextColor },
      axisLine: { lineStyle: { color: '#1e2d3d' } },
      axisTick: { show: false }
    },
    yAxis: {
      type: 'value', minInterval: 1,
      axisLabel: { color: chartTextColor },
      splitLine: { lineStyle: { color: '#1e2d3d', type: 'dashed' } }
    },
    series: [{
      type: 'line', smooth: true,
      data: trend.map(t => t.count),
      symbol: 'circle', symbolSize: 6,
      areaStyle: {
        color: { type: 'linear', x: 0, y: 0, x2: 0, y2: 1, colorStops: [
          { offset: 0, color: 'rgba(99, 102, 241, 0.35)' },
          { offset: 1, color: 'rgba(99, 102, 241, 0.01)' }
        ]}
      },
      lineStyle: { width: 2.5, color: '#6366f1' },
      itemStyle: { color: '#6366f1', borderColor: '#0f1d2e', borderWidth: 2 }
    }]
  }
})

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
  padding: 24px;
}

/* 欢迎语 */
.dash-welcome {
  margin-bottom: 24px;
}
.dash-welcome h2 {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  color: #e2e8f0;
}
.dash-welcome p {
  margin: 4px 0 0;
  font-size: 13px;
  color: #64748b;
}

/* ===== 统计卡片 ===== */
.stat-card {
  position: relative;
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 22px 18px;
  border-radius: 14px;
  background: linear-gradient(135deg, #0f1d2e 0%, #132238 100%);
  border: 1px solid rgba(255,255,255,0.06);
  color: #e2e8f0;
  overflow: hidden;
  transition: transform 0.25s, border-color 0.25s;
}
.stat-card:hover {
  transform: translateY(-4px);
  border-color: var(--accent);
}
.stat-card__icon-wrap {
  width: 48px; height: 48px;
  border-radius: 12px;
  background: rgba(255,255,255,0.06);
  display: flex; align-items: center; justify-content: center;
  color: var(--accent);
  flex-shrink: 0;
  z-index: 1;
}
.stat-card__info { z-index: 1; }
.stat-card__value {
  font-size: 30px; font-weight: 800; line-height: 1;
  background: linear-gradient(135deg, #e2e8f0 30%, var(--accent) 100%);
  -webkit-background-clip: text; -webkit-text-fill-color: transparent;
  background-clip: text;
}
.stat-card__label { font-size: 13px; color: #64748b; margin-top: 6px; }
.stat-card__bg-icon {
  position: absolute; right: -10px; bottom: -14px;
  color: rgba(255,255,255,0.03);
  z-index: 0;
}

/* ===== 图表卡片 ===== */
.chart-row { margin-top: 16px; }

.chart-card {
  border-radius: 14px;
  background: linear-gradient(135deg, #0f1d2e 0%, #132238 100%);
  border: 1px solid rgba(255,255,255,0.06);
  overflow: hidden;
}
.chart-card__header {
  padding: 16px 20px 0;
}
.chart-card__title {
  font-size: 15px;
  font-weight: 600;
  color: #c8d6e5;
}
.chart {
  width: 100%;
  height: 310px;
  padding: 4px 0;
}
</style>
