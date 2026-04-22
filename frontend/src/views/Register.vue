<template>
  <div class="register-page">
    <!-- 动态背景 -->
    <div class="reg-bg">
      <div class="paw-float p1">🐾</div>
      <div class="paw-float p2">🐾</div>
      <div class="paw-float p3">🐾</div>
    </div>

    <div class="reg-wrapper">
      <!-- 左侧角色选择区 -->
      <div class="role-side">
        <div class="role-content">
          <div class="brand-icon">🐕</div>
          <h1>加入我们</h1>
          <p>开启爱宠服务之旅</p>

          <div class="role-cards">
            <div :class="['role-card', form.role===0?'active':'']" @click="form.role=0">
              <span class="role-emoji">👤</span>
              <strong>宠物主人</strong>
              <p>预约服务 · 管理订单 · 爱宠档案</p>
            </div>
            <div :class="['role-card', form.role===1?'active':'']" @click="form.role=1">
              <span class="role-emoji">🏪</span>
              <strong>店主 / 店员</strong>
              <p>接单服务 · 店铺管理 · 收益统计</p>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧表单区 -->
      <div class="form-side">
        <div class="form-card glass-card">
          <div class="welcome-pet">🐾</div>
          <h2 class="form-title">创建账号</h2>
          <p class="form-subtitle">填写信息完成注册</p>

          <el-form ref="formRef" :model="form" :rules="rules" label-position="top" class="reg-form">
            <el-form-item prop="username" label="用户名">
              <el-input v-model="form.username" placeholder="用于登录的用户名" size="large" class="warm-input">
                <template #prefix><span class="input-icon">👤</span></template>
              </el-input>
            </el-form-item>
            <el-form-item prop="phone" label="手机号">
              <el-input v-model="form.phone" placeholder="11位手机号码" size="large" class="warm-input">
                <template #prefix><span class="input-icon">📱</span></template>
              </el-input>
            </el-form-item>
            <el-form-item prop="password" label="密码">
              <el-input v-model="form.password" type="password" show-password placeholder="至少6位字符" size="large" class="warm-input">
                <template #prefix><span class="input-icon">🔒</span></template>
              </el-input>
            </el-form-item>
            <el-form-item label="角色">
              <div class="role-toggle">
                <button :class="['toggle-btn', form.role===0?'active':'']" @click.prevent="form.role=0">
                  <span>👤</span> 宠物主人
                </button>
                <button :class="['toggle-btn', form.role===1?'active':'']" @click.prevent="form.role=1">
                  <span>🏪</span> 店主/店员
                </button>
              </div>
            </el-form-item>
            <el-button type="primary" size="large" :loading="loading" class="submit-btn ripple" @click="handleRegister">
              {{ loading ? '注册中...' : '注 册' }}
            </el-button>
          </el-form>

          <div class="reg-footer">已有账号？<router-link to="/login">返回登录</router-link></div>
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

const form = reactive({ username: '', phone: '', password: '', role: 0 })
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少6位', trigger: 'blur' }
  ]
}

async function handleRegister() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    await userStore.register(form)
    ElMessage.success('注册成功！请登录')
    router.push('/login')
  } catch (e) {
    ElMessage.error(e?.message || '注册失败')
  } finally { loading.value = false }
}
</script>

<style scoped>
.register-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--color-bg);
  position: relative;
  overflow: hidden;
}

.reg-bg {
  position: fixed;
  inset: 0;
  background: linear-gradient(135deg, #E8F5E9 0%, var(--color-cream) 50%, var(--color-pink-light) 100%);
  z-index: 0;
}
.paw-float {
  position: absolute;
  font-size: 28px;
  opacity: 0.12;
  animation: float 4s ease-in-out infinite;
}
.p1 { top:20%; left:15%; animation-delay:0s; }
.p2 { top:60%; right:20%; animation-delay:1.5s; }
.p3 { bottom:15%; left:40%; animation-delay:3s; }

.reg-wrapper {
  position: relative;
  z-index: 1;
  display: flex;
  width: 940px;
  max-width: 95vw;
  min-height: 600px;
  border-radius: var(--radius-xl);
  overflow: hidden;
  box-shadow: var(--shadow-xl);
  animation: bounce-in 0.5s ease-out;
}

/* 角色选择区 */
.role-side {
  flex: 1;
  background: linear-gradient(160deg, #98DDCA 0%, #6CC4A1 50%, #4CAF7D 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 48px 36px;
  position: relative;
  overflow: hidden;
}
.role-side::before {
  content: '';
  position: absolute;
  width: 250px;
  height: 250px;
  border-radius: 50%;
  background: rgba(255,255,255,0.08);
  top: -60px;
  right: -40px;
}
.role-content {
  text-align: center;
  color: #fff;
  z-index: 1;
}
.brand-icon {
  font-size: 64px;
  margin-bottom: 12px;
  animation: float 3s ease-in-out infinite;
}
.role-content h1 { font-size: 30px; color: #fff; margin-bottom: 6px; }
.role-content > p { font-size: 15px; opacity: 0.85; margin-bottom: 36px; }

.role-cards { display: flex; gap: 14px; }
.role-card {
  flex: 1;
  background: rgba(255,255,255,0.15);
  backdrop-filter: blur(10px);
  border-radius: var(--radius-lg);
  padding: 24px 14px;
  cursor: pointer;
  border: 2px solid transparent;
  transition: var(--transition-normal);
  text-align: center;
}
.role-card:hover {
  transform: translateY(-4px);
  background: rgba(255,255,255,0.22);
}
.role-card.active {
  border-color: #fff;
  background: rgba(255,255,255,0.3);
  transform: translateY(-4px);
}
.role-emoji { font-size: 36px; display: block; margin-bottom: 8px; }
.role-card strong { display: block; font-size: 15px; margin-bottom: 4px; color: #fff; }
.role-card p { font-size: 12px; opacity: 0.8; margin: 0; line-height: 1.5; }

/* 表单区 */
.form-side {
  flex: 0 0 480px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--glass-bg);
  backdrop-filter: var(--glass-blur);
}
.form-card {
  width: 400px;
  padding: 36px 32px 28px;
  text-align: center;
  background: rgba(255,255,255,0.85);
  border: none;
  box-shadow: none;
}
.welcome-pet {
  font-size: 40px;
  margin-bottom: 6px;
  animation: float 2.5s ease-in-out infinite;
}
.form-title { font-size: 24px; margin-bottom: 4px; }
.form-subtitle { color: var(--color-text-secondary); font-size: 14px; margin-bottom: 24px; }

.reg-form { text-align: left; }
.reg-form :deep(.el-form-item__label) {
  font-weight: 600;
  color: var(--color-text);
  font-size: 13px;
}

/* 角色切换 */
.role-toggle {
  display: flex;
  gap: 10px;
  width: 100%;
}
.toggle-btn {
  flex: 1;
  padding: 10px;
  border-radius: var(--radius-sm);
  border: 2px solid var(--color-border);
  background: #fff;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  color: var(--color-text-secondary);
  transition: var(--transition-fast);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
}
.toggle-btn:hover { border-color: var(--color-primary); color: var(--color-primary); }
.toggle-btn.active {
  border-color: var(--color-primary);
  background: var(--color-cream);
  color: var(--color-primary);
  font-weight: 600;
}

.submit-btn {
  width: 100% !important;
  height: 48px !important;
  font-size: 16px !important;
  font-weight: 700 !important;
  border-radius: var(--radius-full) !important;
  background: linear-gradient(135deg, #98DDCA 0%, #6CC4A1 100%) !important;
  border: none !important;
  color: #fff !important;
  letter-spacing: 4px;
  transition: var(--transition-normal) !important;
}
.submit-btn:hover {
  transform: translateY(-2px) !important;
  box-shadow: 0 8px 24px rgba(152,221,202,0.4) !important;
}

/* 输入框 */
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

.reg-footer {
  text-align: center;
  margin-top: 20px;
  color: var(--color-text-secondary);
  font-size: 14px;
}
.reg-footer a { color: #6CC4A1; font-weight: 600; }

@media (max-width: 768px) {
  .reg-wrapper {
    flex-direction: column;
    width: 100%;
    min-height: 100vh;
    border-radius: 0;
  }
  .role-side { padding: 28px 20px 20px; }
  .brand-icon { font-size: 40px; margin-bottom: 8px; }
  .role-content h1 { font-size: 22px; }
  .role-content > p { margin-bottom: 20px; }
  .role-cards { gap: 10px; }
  .role-card { padding: 16px 10px; }
  .form-side { flex: 1; }
  .form-card { width: 100%; padding: 24px 20px; }
}
</style>
