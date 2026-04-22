<template>
  <div class="page-container">
    <!-- 个人主页头部 -->
    <div class="profile-hero glass-card">
      <div class="profile-bg"></div>
      <div class="profile-content">
        <div class="avatar-wrap">
          <el-avatar :size="72" :style="{background: avatarBg}" class="user-avatar" :src="avatarUrl || undefined">
            <template v-if="!avatarUrl">{{ (userStore.userInfo.username || 'U').charAt(0).toUpperCase() }}</template>
          </el-avatar>
          <span class="level-badge">Lv.{{ userLevel }}</span>
          <label class="avatar-upload-btn" title="更换头像">
            📷
            <input type="file" accept="image/*" @change="handleAvatarUpload" style="display:none" />
          </label>
        </div>
        <div class="profile-info">
          <h2>{{ userStore.userInfo.realName || userStore.userInfo.username || '用户' }}</h2>
          <p class="profile-role">
            <el-tag :type="roleTagType" size="small" effect="dark" round>{{ roleLabel }}</el-tag>
          </p>
          <div class="profile-stats">
            <div class="stat-item"><strong>{{ userPoints }}</strong><span>积分</span></div>
            <div class="stat-item"><strong>{{ orderCount }}</strong><span>订单</span></div>
            <div class="stat-item"><strong>{{ petCount }}</strong><span>宠物</span></div>
          </div>
        </div>
      </div>
    </div>

    <!-- 编辑资料卡片 -->
    <div class="card">
      <h3 class="section-title"><span>📝</span> 编辑资料</h3>
      <el-form :model="form" label-width="100px" style="max-width:600px;margin-top:16px" class="profile-form">
        <el-form-item label="用户名">
          <el-input v-model="form.username" disabled class="warm-input" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="form.phone" disabled class="warm-input" />
        </el-form-item>
        <el-form-item label="头像">
          <div class="avatar-edit-row">
            <el-avatar :size="44" :src="form.avatar || undefined" :style="{background: avatarBg}">
              <template v-if="!form.avatar">{{ (form.username || 'U').charAt(0).toUpperCase() }}</template>
            </el-avatar>
            <label class="upload-link">
              <input type="file" accept="image/*" @change="handleAvatarUpload" style="display:none" />
              <span>上传头像</span>
            </label>
            <el-input v-model="form.avatar" placeholder="或输入头像URL" class="warm-input" style="flex:1" />
          </div>
        </el-form-item>
        <template v-if="userStore.isProvider()">
          <el-form-item label="真实姓名">
            <el-input v-model="form.realName" class="warm-input" />
          </el-form-item>
          <el-form-item label="店铺名称">
            <el-input v-model="form.shopName" class="warm-input" />
          </el-form-item>
        </template>
        <el-form-item>
          <el-button type="primary" :loading="saving" @click="handleSave" round>保存修改</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 实名认证（店员必须） -->
    <div class="card" v-if="userStore.isProvider()">
      <h3 class="section-title"><span>🔐</span> 实名认证</h3>
      <div class="verify-status" v-if="isRealNameBound">
        <el-tag type="success" effect="dark" round size="large">✅ 已完成实名认证</el-tag>
        <div class="verify-info">
          <p><strong>认证姓名：</strong>{{ userStore.userInfo.realName }}</p>
          <p><strong>证件号码：</strong>{{ maskIdCard(userStore.userInfo.idCard) }}</p>
        </div>
      </div>
      <div class="verify-form" v-else>
        <el-alert type="warning" :closable="false" show-icon style="margin-bottom:16px">
          <template #title>
            <strong>⚠️ 您尚未完成实名认证</strong>
          </template>
          <p style="margin:4px 0 0">实名认证是接单的前提条件。未完成认证将无法接收客户订单。</p>
        </el-alert>
        <el-form :model="realNameForm" label-width="100px" style="max-width:500px" class="profile-form">
          <el-form-item label="真实姓名" required>
            <el-input v-model="realNameForm.realName" placeholder="请输入身份证上的姓名" class="warm-input" maxlength="20" />
          </el-form-item>
          <el-form-item label="身份证号" required>
            <el-input v-model="realNameForm.idCard" placeholder="请输入18位身份证号码" class="warm-input" maxlength="18" show-word-limit />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="verifying" @click="handleRealNameVerify" round>提交实名认证</el-button>
          </el-form-item>
        </el-form>
      </div>
    </div>

    <!-- 会员等级 -->
    <div class="card">
      <h3 class="section-title"><span>👑</span> 会员等级</h3>
      <div class="level-progress">
        <div class="level-bar">
          <div class="level-fill" :style="{width: levelPercent + '%'}"></div>
        </div>
        <div class="level-info">
          <span>Lv.{{ userLevel }} {{ levelName }}</span>
          <span>还需{{ nextLevelPoints }}积分升级</span>
        </div>
      </div>
      <div class="level-perks">
        <div class="perk-item" v-for="perk in currentPerks" :key="perk.icon">
          <span class="perk-icon">{{ perk.icon }}</span>
          <span>{{ perk.text }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { getProfile, updateProfile, uploadAvatar } from '@/api/user'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()
const saving = ref(false)
const verifying = ref(false)
const form = ref({ username: '', phone: '', avatar: '', realName: '', shopName: '' })
const realNameForm = ref({ realName: '', idCard: '' })
const userPoints = ref(280)
const orderCount = ref(12)
const petCount = ref(2)

const avatarBg = computed(() => {
  const colors = ['#FF8C42','#98DDCA','#F9A8D4','#6C9EBF']
  const name = userStore.userInfo.username || 'U'
  return colors[name.charCodeAt(0) % colors.length]
})
const avatarUrl = computed(() => {
  const url = userStore.userInfo.avatar
  if (!url || !url.trim()) return ''
  if (url.startsWith('http://') || url.startsWith('https://') || url.startsWith('data:')) return url
  return `http://localhost:8081${url.startsWith('/') ? '' : '/'}${url}`
})
const roleTagType = computed(() => {
  const r = userStore.userInfo.role
  return r === 2 ? 'danger' : r === 1 ? 'warning' : ''
})
const roleLabel = computed(() => {
  const r = userStore.userInfo.role
  if (r === 2) return '管理员'
  if (r === 1) {
    const sr = userStore.userInfo.staffRole
    return sr === 2 ? '店员' : '店长'
  }
  return '宠物主人'
})

const userLevel = computed(() => {
  const pts = userPoints.value
  if (pts >= 2000) return 5
  if (pts >= 1000) return 4
  if (pts >= 500) return 3
  if (pts >= 200) return 2
  return 1
})
const levelName = computed(() => ['萌新', '爱宠达人', '资深宠友', '尊享会员', '钻石宠主'][userLevel.value - 1])
const levelPercent = computed(() => {
  const thresholds = [0, 200, 500, 1000, 2000]
  const curr = thresholds[userLevel.value - 1]
  const next = thresholds[userLevel.value] || 3000
  return Math.min(100, ((userPoints.value - curr) / (next - curr)) * 100)
})
const nextLevelPoints = computed(() => {
  const thresholds = [200, 500, 1000, 2000, 3000]
  return Math.max(0, (thresholds[userLevel.value] || 3000) - userPoints.value)
})
const currentPerks = computed(() => {
  const all = [
    { icon: '🎫', text: '新用户8折券' },
    { icon: '🎁', text: '每月免费寄养1天' },
    { icon: '💎', text: '专属客服通道' },
    { icon: '👑', text: '全品类9折' },
    { icon: '🏆', text: '生日双倍积分' },
  ]
  return all.slice(0, userLevel.value)
})

onMounted(async () => {
  try {
    const res = await getProfile()
    form.value = { ...res.data }
  } catch (e) {}
})

async function handleSave() {
  saving.value = true
  try {
    await updateProfile(form.value)
    ElMessage.success('保存成功')
    await userStore.fetchCurrentUser()
  } catch (e) { ElMessage.error(e?.message || '保存失败') }
  finally { saving.value = false }
}

async function handleAvatarUpload(e) {
  const file = e.target.files[0]
  if (!file) return
  if (file.size > 5 * 1024 * 1024) { ElMessage.warning('图片不能超过5MB'); return }
  try {
    const res = await uploadAvatar(file)
    if (res.data) {
      form.value.avatar = res.data
      await updateProfile({ avatar: res.data })
      await userStore.fetchCurrentUser()
      ElMessage.success('头像更新成功')
    }
  } catch (err) { ElMessage.error('头像上传失败') }
  finally { e.target.value = '' }
}

const isRealNameBound = computed(() => !!(userStore.userInfo.realName && userStore.userInfo.idCard))

function maskIdCard(id) {
  if (!id || id.length < 10) return '***'
  return id.substring(0, 4) + '****' + id.substring(id.length - 4)
}

async function handleRealNameVerify() {
  if (!realNameForm.value.realName?.trim()) { ElMessage.warning('请输入真实姓名'); return }
  if (!realNameForm.value.idCard?.trim() || realNameForm.value.idCard.length < 15) {
    ElMessage.warning('请输入有效的身份证号码'); return
  }
  verifying.value = true
  try {
    await updateProfile({
      realName: realNameForm.value.realName.trim(),
      idCard: realNameForm.value.idCard.trim()
    })
    await userStore.fetchCurrentUser()
    ElMessage.success('实名认证成功！现在可以接单了')
  } catch (e) { ElMessage.error(e?.message || '认证失败') }
  finally { verifying.value = false }
}
</script>

<style scoped>
/* 头部 */
.profile-hero {
  border-radius: var(--radius-lg);
  overflow: hidden;
  margin-bottom: 20px;
  position: relative;
}
.profile-bg {
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-coral) 50%, var(--color-pink) 100%);
  opacity: 0.15;
}
.profile-content {
  position: relative;
  display: flex;
  align-items: center;
  gap: 24px;
  padding: 28px;
}
.avatar-wrap { position: relative; }
.user-avatar { font-size: 28px; font-weight: 700; color: #fff !important; }
.level-badge {
  position: absolute;
  bottom: -2px;
  right: -4px;
  background: linear-gradient(135deg, #FFD700, #FFA500);
  color: #fff;
  font-size: 10px;
  font-weight: 700;
  padding: 2px 8px;
  border-radius: var(--radius-full);
  box-shadow: 0 2px 6px rgba(255,165,0,0.4);
}
.avatar-upload-btn {
  position: absolute;
  top: -2px;
  left: -2px;
  width: 26px;
  height: 26px;
  border-radius: 50%;
  background: var(--color-primary);
  color: #fff;
  font-size: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: 0 2px 6px rgba(0,0,0,0.2);
  transition: transform 0.2s;
}
.avatar-upload-btn:hover { transform: scale(1.15); }
.avatar-edit-row { display: flex; align-items: center; gap: 12px; width: 100%; }
.upload-link { cursor: pointer; color: var(--color-primary); font-size: 13px; white-space: nowrap; }
.upload-link:hover { text-decoration: underline; }
.profile-info h2 { font-size: 22px; margin-bottom: 6px; }
.profile-role { margin-bottom: 12px; }
.profile-stats { display: flex; gap: 24px; }
.stat-item { text-align: center; }
.stat-item strong { display: block; font-size: 22px; color: var(--color-primary); font-weight: 800; }
.stat-item span { font-size: 12px; color: var(--color-text-placeholder); }

.section-title { font-size: 17px; font-weight: 700; margin-bottom: 4px; display: flex; align-items: center; gap: 8px; }

/* 实名认证 */
.verify-status { display: flex; flex-direction: column; gap: 14px; margin-top: 16px; }
.verify-info { background: var(--color-cream); padding: 14px 18px; border-radius: var(--radius-md); }
.verify-info p { margin: 6px 0; font-size: 14px; color: var(--color-text-secondary); }
.verify-form { margin-top: 12px; }

/* 输入框 */
.warm-input :deep(.el-input__wrapper) {
  border-radius: var(--radius-sm) !important;
  background: var(--color-cream) !important;
  box-shadow: none !important;
  border: 1.5px solid transparent;
}
.warm-input :deep(.el-input__wrapper:focus-within) {
  background: #fff !important;
  border-color: var(--color-primary);
  box-shadow: 0 0 0 4px rgba(255,140,66,0.12) !important;
}

/* 会员等级 */
.level-progress { margin-top: 16px; }
.level-bar {
  height: 10px;
  background: var(--color-cream);
  border-radius: 5px;
  overflow: hidden;
}
.level-fill {
  height: 100%;
  background: linear-gradient(90deg, var(--color-primary), var(--color-coral));
  border-radius: 5px;
  transition: width 0.6s ease;
}
.level-info {
  display: flex;
  justify-content: space-between;
  margin-top: 6px;
  font-size: 12px;
  color: var(--color-text-placeholder);
}
.level-perks {
  display: flex;
  gap: 16px;
  margin-top: 16px;
  flex-wrap: wrap;
}
.perk-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 14px;
  background: var(--color-cream);
  border-radius: var(--radius-full);
  font-size: 13px;
  color: var(--color-text-secondary);
}
.perk-icon { font-size: 16px; }

@media (max-width: 768px) {
  .profile-content { flex-direction: column; text-align: center; }
  .profile-stats { justify-content: center; }
}
</style>
