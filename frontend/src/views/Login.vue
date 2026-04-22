<template>
  <div class="login-page">
    <!-- 动态背景 -->
    <div class="login-bg">
      <div class="cloud c1"></div>
      <div class="cloud c2"></div>
      <div class="cloud c3"></div>
      <div class="paw-print p1">🐾</div>
      <div class="paw-print p2">🐾</div>
      <div class="paw-print p3">🐾</div>
      <div class="paw-print p4">🐾</div>
    </div>

    <!-- 登录卡片 -->
    <div class="login-wrapper">
      <!-- 左侧品牌区 -->
      <div class="brand-side">
        <div class="brand-content">
          <div class="brand-mascot">🐕</div>
          <h1 class="brand-title">宠物服务平台</h1>
          <p class="brand-slogan">专业 · 便捷 · 贴心</p>
          <div class="brand-features">
            <div class="bf-item"><span class="bf-icon">✨</span>专业宠物美容护理</div>
            <div class="bf-item"><span class="bf-icon">🏠</span>24小时安心寄养</div>
            <div class="bf-item"><span class="bf-icon">💉</span>医疗健康保障</div>
            <div class="bf-item"><span class="bf-icon">🚶</span>专业遛宠服务</div>
          </div>
        </div>
      </div>

      <!-- 右侧表单区 -->
      <div class="form-side">
        <div class="form-card glass-card">
          <div class="welcome-pet">🐱</div>
          <h2 class="form-title">欢迎回来</h2>
          <p class="form-subtitle">登录您的爱宠账号</p>

          <el-form ref="formRef" :model="form" :rules="rules" @keyup.enter="handleLogin" class="login-form">
            <el-form-item prop="username">
              <el-input v-model="form.username" placeholder="用户名" size="large" class="warm-input">
                <template #prefix><span class="input-icon">👤</span></template>
              </el-input>
            </el-form-item>
            <el-form-item prop="password">
              <el-input v-model="form.password" type="password" placeholder="密码" size="large" show-password class="warm-input">
                <template #prefix><span class="input-icon">🔒</span></template>
              </el-input>
            </el-form-item>
            <el-button type="primary" size="large" :loading="loading" class="login-btn ripple" @click="handleLogin">
              {{ loading ? '登录中...' : '登 录' }}
            </el-button>
          </el-form>

          <div class="divider"><span>或</span></div>

          <!-- 社交登录 -->
          <div class="social-login">
            <button class="social-btn wechat" @click="socialLogin('wechat')">
              <svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor"><path d="M8.691 2.188C3.891 2.188 0 5.476 0 9.53c0 2.212 1.17 4.203 3.002 5.55a.59.59 0 0 1 .213.665l-.39 1.48c-.019.07-.048.141-.048.213 0 .163.13.295.29.295a.326.326 0 0 0 .167-.054l1.903-1.114a.864.864 0 0 1 .717-.098 10.16 10.16 0 0 0 2.837.403c.276 0 .543-.027.811-.05-.857-2.578.157-4.972 1.932-6.446 1.703-1.415 3.882-1.98 5.853-1.838-.576-3.583-4.196-6.348-8.596-6.348zM5.785 5.991c.642 0 1.162.529 1.162 1.18a1.17 1.17 0 0 1-1.162 1.178A1.17 1.17 0 0 1 4.623 7.17c0-.651.52-1.18 1.162-1.18zm5.813 0c.642 0 1.162.529 1.162 1.18a1.17 1.17 0 0 1-1.162 1.178 1.17 1.17 0 0 1-1.162-1.178c0-.651.52-1.18 1.162-1.18zm3.2 4.127c-3.997 0-7.242 2.694-7.242 6.02 0 3.324 3.245 6.02 7.242 6.02.826 0 1.622-.12 2.361-.336a.722.722 0 0 1 .598.082l1.585.926a.272.272 0 0 0 .14.045c.134 0 .24-.11.24-.245 0-.06-.024-.118-.04-.177l-.325-1.233a.492.492 0 0 1 .177-.554C22.024 19.563 24 17.892 24 16.138c0-3.326-3.245-6.02-7.202-6.02zm-2.424 3.39c.535 0 .969.44.969.983a.976.976 0 0 1-.969.983.976.976 0 0 1-.969-.983c0-.543.434-.983.97-.983zm4.848 0c.535 0 .969.44.969.983a.976.976 0 0 1-.97.983.976.976 0 0 1-.968-.983c0-.543.434-.983.969-.983z"/></svg>
              <span>微信登录</span>
            </button>
            <button class="social-btn apple" @click="socialLogin('apple')">
              <svg viewBox="0 0 24 24" width="18" height="18" fill="currentColor"><path d="M17.05 20.28c-.98.95-2.05.88-3.08.4-1.09-.5-2.08-.48-3.24 0-1.44.62-2.2.44-3.06-.4C2.79 15.25 3.51 7.59 9.05 7.31c1.35.07 2.29.74 3.08.8 1.18-.24 2.31-.93 3.57-.84 1.51.12 2.65.72 3.4 1.8-3.12 1.87-2.38 5.98.48 7.13-.57 1.5-1.31 2.99-2.54 4.09zM12.03 7.25c-.15-2.23 1.66-4.07 3.74-4.25.29 2.58-2.34 4.5-3.74 4.25z"/></svg>
              <span>Apple</span>
            </button>
          </div>

          <div class="login-footer">
            还没有账号？<router-link to="/register">立即注册</router-link>
          </div>

          <!-- 演示账号 -->
          <div class="demo-section">
            <p class="demo-label">演示账号（密码 123456）</p>
            <div class="demo-btns">
              <button class="demo-btn" @click="quickFill('user1')">👤 客户</button>
              <button class="demo-btn staff" @click="quickFill('staff1')">🧑‍🍳 店员</button>
              <button class="demo-btn admin" @click="quickFill('provider1')">🏪 管理</button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref()
const loading = ref(false)

const form = reactive({ username: '', password: '' })
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

function quickFill(username) {
  form.username = username
  form.password = '123456'
}

function socialLogin(type) {
  ElMessage.info(`${type === 'wechat' ? '微信' : 'Apple'} 登录开发中，请使用账号密码登录`)
}

async function handleLogin() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    await userStore.login(form)
    ElMessage.success('登录成功')
    await redirectByRole()
  } catch (e) {
    ElMessage.error(e?.message || '登录失败，请检查用户名和密码')
  } finally { loading.value = false }
}

async function redirectByRole() {
  const role = userStore.userInfo?.role
  // 角色统一：不再区分管理员和店长，都走 MainLayout
  if (role === 1 || role === 2) {
    // 根据身份自动跳转对应工作台
    if (userStore.isShopOwner() || role === 2) await router.push('/provider/owner-dashboard')
    else if (userStore.isShopStaff()) await router.push('/provider/staff-dashboard')
    else await router.push('/home')
  } else {
    await router.push('/home')
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--color-bg);
  position: relative;
  overflow: hidden;
}

/* 动态背景 */
.login-bg {
  position: fixed;
  inset: 0;
  background: linear-gradient(135deg, var(--color-cream) 0%, var(--color-pink-light) 50%, #FFF9F0 100%);
  z-index: 0;
}
.cloud {
  position: absolute;
  background: rgba(255,255,255,0.7);
  border-radius: 100px;
  animation: cloud-drift linear infinite;
}
.c1 { width:300px; height:80px; top:10%; left:-50px; animation-duration:25s; }
.c2 { width:200px; height:60px; top:30%; right:-30px; animation-duration:20s; animation-delay:-5s; }
.c3 { width:250px; height:70px; bottom:20%; left:10%; animation-duration:30s; animation-delay:-10s; }
@keyframes cloud-drift {
  0% { transform: translateX(-100px); opacity:0.5; }
  50% { opacity:0.8; }
  100% { transform: translateX(calc(100vw + 100px)); opacity:0.5; }
}
.paw-print {
  position: absolute;
  font-size: 24px;
  opacity: 0.15;
  animation: float 4s ease-in-out infinite;
}
.p1 { top:15%; left:20%; animation-delay:0s; }
.p2 { top:45%; right:15%; animation-delay:1s; }
.p3 { bottom:25%; left:30%; animation-delay:2s; }
.p4 { top:65%; right:35%; animation-delay:3s; }

/* 主容器 */
.login-wrapper {
  position: relative;
  z-index: 1;
  display: flex;
  width: 920px;
  max-width: 95vw;
  min-height: 580px;
  border-radius: var(--radius-xl);
  overflow: hidden;
  box-shadow: var(--shadow-xl);
  animation: bounce-in 0.5s ease-out;
}

/* 品牌区 */
.brand-side {
  flex: 1;
  background: linear-gradient(160deg, var(--color-primary) 0%, #FF6B6B 50%, var(--color-coral) 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 48px 36px;
  position: relative;
  overflow: hidden;
}
.brand-side::before {
  content: '';
  position: absolute;
  width: 300px;
  height: 300px;
  border-radius: 50%;
  background: rgba(255,255,255,0.08);
  top: -80px;
  right: -60px;
}
.brand-side::after {
  content: '';
  position: absolute;
  width: 200px;
  height: 200px;
  border-radius: 50%;
  background: rgba(255,255,255,0.06);
  bottom: -40px;
  left: -30px;
}
.brand-content {
  text-align: center;
  color: #fff;
  z-index: 1;
  position: relative;
}
.brand-mascot {
  font-size: 80px;
  margin-bottom: 16px;
  animation: float 3s ease-in-out infinite;
  filter: drop-shadow(0 8px 16px rgba(0,0,0,0.15));
}
.brand-title {
  font-size: 32px;
  font-weight: 800;
  letter-spacing: 3px;
  margin-bottom: 8px;
  color: #fff;
}
.brand-slogan {
  font-size: 16px;
  opacity: 0.9;
  margin-bottom: 40px;
  letter-spacing: 4px;
}
.brand-features { text-align: left; display: inline-block; }
.bf-item {
  padding: 10px 0;
  font-size: 15px;
  opacity: 0.92;
  display: flex;
  align-items: center;
  gap: 8px;
}
.bf-icon { font-size: 18px; }

/* 表单区 */
.form-side {
  flex: 0 0 440px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--glass-bg);
  backdrop-filter: var(--glass-blur);
  -webkit-backdrop-filter: var(--glass-blur);
}
.form-card {
  width: 360px;
  padding: 40px 32px 32px;
  text-align: center;
  background: rgba(255,255,255,0.85);
  border: none;
  box-shadow: none;
}
.welcome-pet {
  font-size: 48px;
  margin-bottom: 8px;
  animation: float 2.5s ease-in-out infinite;
}
.form-title {
  font-size: 26px;
  color: var(--color-text);
  margin-bottom: 4px;
}
.form-subtitle {
  color: var(--color-text-secondary);
  font-size: 14px;
  margin-bottom: 28px;
}

/* 输入框光晕 */
.warm-input :deep(.el-input__wrapper) {
  border-radius: var(--radius-sm) !important;
  background: var(--color-cream) !important;
  box-shadow: none !important;
  border: 1.5px solid transparent;
  transition: var(--transition-fast);
}
.warm-input :deep(.el-input__wrapper:focus-within) {
  background: #fff !important;
  border-color: var(--color-primary);
  box-shadow: 0 0 0 4px rgba(255,140,66,0.12) !important;
}
.input-icon { font-size: 16px; }

.login-form { margin-top: 4px; }
.login-form .el-form-item { margin-bottom: 20px; }

.login-btn {
  width: 100% !important;
  height: 48px !important;
  font-size: 16px !important;
  font-weight: 700 !important;
  border-radius: var(--radius-full) !important;
  background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-primary-light) 100%) !important;
  border: none !important;
  letter-spacing: 4px;
  transition: var(--transition-normal) !important;
}
.login-btn:hover {
  transform: translateY(-2px) !important;
  box-shadow: 0 8px 24px rgba(255,140,66,0.4) !important;
}

/* 分割线 */
.divider {
  display: flex;
  align-items: center;
  margin: 20px 0;
  color: var(--color-text-placeholder);
  font-size: 13px;
}
.divider::before, .divider::after {
  content: '';
  flex: 1;
  height: 1px;
  background: var(--color-divider);
}
.divider span { padding: 0 12px; }

/* 社交登录 */
.social-login {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
}
.social-btn {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 10px;
  border-radius: var(--radius-sm);
  border: 1.5px solid var(--color-border);
  background: #fff;
  cursor: pointer;
  font-size: 13px;
  font-weight: 500;
  color: var(--color-text);
  transition: var(--transition-fast);
}
.social-btn:hover {
  border-color: var(--color-primary);
  background: var(--color-cream);
  transform: translateY(-1px);
}
.social-btn.wechat:hover { border-color: #07C160; color: #07C160; }
.social-btn.apple:hover { border-color: #333; color: #333; }

.login-footer {
  margin-bottom: 16px;
  color: var(--color-text-secondary);
  font-size: 14px;
}
.login-footer a {
  color: var(--color-primary);
  font-weight: 600;
}

/* 演示账号 */
.demo-section {
  padding-top: 16px;
  border-top: 1px solid var(--color-divider);
}
.demo-label {
  color: var(--color-text-placeholder);
  font-size: 12px;
  margin-bottom: 10px;
}
.demo-btns {
  display: flex;
  gap: 8px;
  justify-content: center;
}
.demo-btn {
  padding: 6px 16px;
  border-radius: var(--radius-full);
  border: 1.5px solid var(--color-border);
  background: #fff;
  cursor: pointer;
  font-size: 13px;
  color: var(--color-text-secondary);
  transition: var(--transition-fast);
}
.demo-btn:hover {
  background: var(--color-cream);
  border-color: var(--color-primary);
  color: var(--color-primary);
  transform: translateY(-1px);
}
.demo-btn.provider:hover { border-color: var(--color-coral); color: var(--color-coral); background: var(--color-pink-light); }
.demo-btn.staff:hover { border-color: #67C23A; color: #67C23A; background: rgba(103,194,58,0.08); }
.demo-btn.admin:hover { border-color: var(--color-blue); color: var(--color-blue); background: var(--color-blue-light); }

/* 响应式 */
@media (max-width: 768px) {
  .login-wrapper {
    flex-direction: column;
    width: 100%;
    min-height: 100vh;
    border-radius: 0;
  }
  .brand-side {
    padding: 32px 24px 24px;
    min-height: auto;
  }
  .brand-mascot { font-size: 48px; margin-bottom: 8px; }
  .brand-title { font-size: 22px; }
  .brand-slogan { margin-bottom: 16px; }
  .brand-features { display: none; }
  .form-side { flex: 1; }
  .form-card { width: 100%; padding: 24px 20px; }
}
</style>
