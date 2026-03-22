<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-header">
        <h2>Cloud Platform</h2>
        <p>微服务平台管理系统</p>
      </div>
      
      <el-form ref="loginFormRef" :model="loginForm" :rules="rules" class="login-form">
        <el-form-item prop="username">
          <el-input 
            v-model="loginForm.username" 
            placeholder="用户名"
            prefix-icon="User"
            size="large"
          />
        </el-form-item>
        
        <el-form-item prop="password">
          <el-input 
            v-model="loginForm.password" 
            type="password"
            placeholder="密码"
            prefix-icon="Lock"
            size="large"
            show-password
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        
        <el-form-item>
          <el-button 
            type="primary" 
            size="large" 
            :loading="loading" 
            style="width: 100%"
            @click="handleLogin"
          >
            登 录
          </el-button>
        </el-form-item>
      </el-form>
      
      <div class="social-login">
        <el-divider>其他登录方式</el-divider>
        <div class="social-icons">
          <el-tooltip content="微信登录" placement="top">
            <el-button circle @click="handleSocialLogin('wetchat')">
              <span style="font-size: 20px">💬</span>
            </el-button>
          </el-tooltip>
          <el-tooltip content="QQ登录" placement="top">
            <el-button circle @click="handleSocialLogin('qq')">
              <span style="font-size: 20px">🐧</span>
            </el-button>
          </el-tooltip>
          <el-tooltip content="钉钉登录" placement="top">
            <el-button circle @click="handleSocialLogin('dingtalk')">
              <span style="font-size: 20px">🏠</span>
            </el-button>
          </el-tooltip>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login as loginApi, getSocialUrl } from '@/api/auth'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const loginFormRef = ref()
const loading = ref(false)

const loginForm = reactive({
  username: 'admin',
  password: '123456'
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = async () => {
  const valid = await loginFormRef.value.validate().catch(() => false)
  if (!valid) return
  
  loading.value = true
  try {
    const res = await loginApi(loginForm.username, loginForm.password)
    userStore.setUserToken(res.data.token)
    userStore.setUserInfo(res.data)
    ElMessage.success('登录成功')
    router.push('/')
  } catch (error) {
    console.error('登录失败:', error)
  } finally {
    loading.value = false
  }
}

const handleSocialLogin = async (platform) => {
  try {
    const res = await getSocialUrl(platform)
    window.location.href = res.data.url
  } catch (error) {
    ElMessage.error('获取登录地址失败')
  }
}
</script>

<style scoped>
.login-container {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-box {
  width: 400px;
  padding: 40px;
  background: white;
  border-radius: 10px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
}

.login-header {
  text-align: center;
  margin-bottom: 30px;
}

.login-header h2 {
  font-size: 28px;
  color: #333;
  margin-bottom: 10px;
}

.login-header p {
  color: #666;
  font-size: 14px;
}

.social-login {
  margin-top: 20px;
}

.social-icons {
  display: flex;
  justify-content: center;
  gap: 20px;
}
</style>
