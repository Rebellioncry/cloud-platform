<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-header">
        <h2>Cloud Platform</h2>
        <p>微服务平台管理系统</p>
      </div>

      <el-tabs v-model="activeTab" stretch>
        <el-tab-pane label="账号登录" name="password">
          <el-form ref="passwordFormRef" :model="passwordForm" :rules="passwordRules" class="login-form">
            <el-form-item prop="username">
              <el-input v-model="passwordForm.username" placeholder="用户名" prefix-icon="User" size="large" />
            </el-form-item>
            <el-form-item prop="password">
              <el-input v-model="passwordForm.password" type="password" placeholder="密码" prefix-icon="Lock" size="large" show-password @keyup.enter="handlePasswordLogin" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" size="large" :loading="loading" style="width: 100%" @click="handlePasswordLogin">
                登 录
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="手机号登录" name="sms">
          <el-form ref="smsFormRef" :model="smsForm" :rules="smsRules" class="login-form">
            <el-form-item prop="mobile">
              <el-input v-model="smsForm.mobile" placeholder="手机号" prefix-icon="Iphone" size="large" />
            </el-form-item>
            <el-form-item prop="code">
              <div class="code-row">
                <el-input v-model="smsForm.code" placeholder="验证码" prefix-icon="Message" size="large" @keyup.enter="handleSmsLogin" />
                <el-button size="large" :disabled="smsCountdown > 0" @click="handleSendCode('sms', smsForm.mobile)">
                  {{ smsCountdown > 0 ? smsCountdown + 's' : '获取验证码' }}
                </el-button>
              </div>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" size="large" :loading="loading" style="width: 100%" @click="handleSmsLogin">
                登 录
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="邮箱登录" name="email">
          <el-form ref="emailFormRef" :model="emailForm" :rules="emailRules" class="login-form">
            <el-form-item prop="email">
              <el-input v-model="emailForm.email" placeholder="邮箱" prefix-icon="Message" size="large" />
            </el-form-item>
            <el-form-item prop="code">
              <div class="code-row">
                <el-input v-model="emailForm.code" placeholder="验证码" prefix-icon="Promotion" size="large" @keyup.enter="handleEmailLogin" />
                <el-button size="large" :disabled="emailCountdown > 0" @click="handleSendCode('email', emailForm.email)">
                  {{ emailCountdown > 0 ? emailCountdown + 's' : '获取验证码' }}
                </el-button>
              </div>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" size="large" :loading="loading" style="width: 100%" @click="handleEmailLogin">
                登 录
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login as loginApi, sendCode, codeLogin } from '@/api/auth'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const activeTab = ref('password')
const loading = ref(false)
const smsCountdown = ref(0)
const emailCountdown = ref(0)

const passwordFormRef = ref()
const smsFormRef = ref()
const emailFormRef = ref()

const passwordForm = reactive({ username: 'admin', password: '123456' })
const smsForm = reactive({ mobile: '', code: '' })
const emailForm = reactive({ email: '', code: '' })

const passwordRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}
const smsRules = {
  mobile: [{ required: true, message: '请输入手机号', trigger: 'blur' }],
  code: [{ required: true, message: '请输入验证码', trigger: 'blur' }]
}
const emailRules = {
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  code: [{ required: true, message: '请输入验证码', trigger: 'blur' }]
}

const startCountdown = (type) => {
  const countdown = type === 'sms' ? smsCountdown : emailCountdown
  countdown.value = 60
  const timer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) clearInterval(timer)
  }, 1000)
}

const handleSendCode = async (type, target) => {
  if (!target) {
    ElMessage.warning(type === 'sms' ? '请输入手机号' : '请输入邮箱')
    return
  }
  try {
    const targetObj = type === 'sms' ? { mobile: target } : { email: target }
    await sendCode(type, targetObj)
    ElMessage.success('验证码已发送')
    startCountdown(type)
  } catch (error) {
    console.error('发送验证码失败:', error)
  }
}

const handlePasswordLogin = async () => {
  const valid = await passwordFormRef.value.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    const res = await loginApi(passwordForm.username, passwordForm.password)
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

const handleSmsLogin = async () => {
  const valid = await smsFormRef.value.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    const res = await codeLogin('sms', { mobile: smsForm.mobile }, smsForm.code)
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

const handleEmailLogin = async () => {
  const valid = await emailFormRef.value.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    const res = await codeLogin('email', { email: emailForm.email }, emailForm.code)
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
</script>

<style scoped>
.login-container {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #0a1628 0%, #122a45 50%, #0f1d2e 100%);
}

.login-box {
  width: 420px;
  padding: 40px;
  background: #132238;
  border-radius: 10px;
  border: 1px solid rgba(64, 158, 255, 0.15);
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.4);
}

.login-header {
  text-align: center;
  margin-bottom: 20px;
}

.login-header h2 {
  font-size: 28px;
  color: #e2e8f0;
  margin-bottom: 10px;
}

.login-header p {
  color: #8899aa;
  font-size: 14px;
}

.login-form {
  margin-top: 15px;
}

.code-row {
  display: flex;
  gap: 10px;
  width: 100%;
}

.code-row .el-input {
  flex: 1;
}

.code-row .el-button {
  width: 120px;
  flex-shrink: 0;
}
</style>
