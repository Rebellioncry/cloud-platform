<template>
  <div class="dashboard">
    <h2>欢迎使用 Cloud Platform 微服务平台</h2>
    
    <el-row :gutter="20" style="margin-top: 30px">
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <el-icon class="stat-icon" style="color: #409EFF"><User /></el-icon>
            <div class="stat-info">
              <div class="stat-value">{{ stats.userCount }}</div>
              <div class="stat-label">用户数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <el-icon class="stat-icon" style="color: #67C23A"><Key /></el-icon>
            <div class="stat-info">
              <div class="stat-value">{{ stats.roleCount }}</div>
              <div class="stat-label">角色数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <el-icon class="stat-icon" style="color: #E6A23C"><Menu /></el-icon>
            <div class="stat-info">
              <div class="stat-value">{{ stats.menuCount }}</div>
              <div class="stat-label">菜单数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <el-icon class="stat-icon" style="color: #F56C6C"><OfficeBuilding /></el-icon>
            <div class="stat-info">
              <div class="stat-value">{{ stats.tenantCount }}</div>
              <div class="stat-label">租户数</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <el-card style="margin-top: 30px">
      <template #header>
        <span>系统信息</span>
      </template>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="系统版本">1.0.0</el-descriptions-item>
        <el-descriptions-item label="Spring Boot">3.5.12</el-descriptions-item>
        <el-descriptions-item label="Spring Cloud">2025.0.1</el-descriptions-item>
        <el-descriptions-item label="Java">21</el-descriptions-item>
        <el-descriptions-item label="数据库">MySQL 8.0</el-descriptions-item>
        <el-descriptions-item label="缓存">Redis</el-descriptions-item>
      </el-descriptions>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const stats = ref({
  userCount: 0,
  roleCount: 0,
  menuCount: 0,
  tenantCount: 0
})

onMounted(() => {
  // 获取统计数据
})
</script>

<style scoped>
.dashboard {
  padding: 20px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 20px;
}

.stat-icon {
  font-size: 48px;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #333;
}

.stat-label {
  color: #666;
  font-size: 14px;
}
</style>
