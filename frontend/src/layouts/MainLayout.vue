<template>
  <el-container class="main-layout">
    <!-- 侧边栏（PC端） -->
    <el-aside :width="collapsed ? '72px' : '260px'" v-if="userStore.token" class="sidebar" :class="{ collapsed }">
      <div class="sidebar-inner">
        <!-- Logo -->
        <div class="logo-area" @click="collapsed = !collapsed">
          <span class="logo-emoji">🐾</span>
          <transition name="fade">
            <span class="logo-text" v-show="!collapsed">宠物服务平台</span>
          </transition>
        </div>

        <!-- 菜单 -->
        <el-menu
          :default-active="currentRoute"
          router
          :collapse="collapsed"
          background-color="transparent"
          text-color="rgba(255,255,255,0.7)"
          active-text-color="#fff"
          class="side-menu"
        >
          <el-menu-item index="/home">
            <el-icon><HomeFilled /></el-icon>
            <template #title>首页</template>
          </el-menu-item>
          <el-menu-item index="/services">
            <el-icon><Grid /></el-icon>
            <template #title>服务列表</template>
          </el-menu-item>
          <el-menu-item index="/orders">
            <el-icon><Document /></el-icon>
            <template #title>我的订单</template>
          </el-menu-item>

          <!-- 个人中心（非管理员） -->
          <el-sub-menu index="user" v-if="!userStore.isAdmin()">
            <template #title>
              <el-icon><User /></el-icon>
              <span>个人中心</span>
            </template>
            <el-menu-item index="/user/profile">个人资料</el-menu-item>
            <el-menu-item index="/user/pets" v-if="userStore.isUser()">我的宠物</el-menu-item>
          </el-sub-menu>

          <!-- 服务商工作台 -->
          <template v-if="userStore.isProvider()">
            <div class="menu-divider">{{ collapsed ? '·' : '工作台' }}</div>
            <el-menu-item index="/provider/dashboard">
              <el-icon><DataLine /></el-icon>
              <template #title>数据看板</template>
            </el-menu-item>
            <el-menu-item index="/provider/services">
              <el-icon><Goods /></el-icon>
              <template #title>服务管理</template>
            </el-menu-item>
            <el-menu-item index="/provider/income">
              <el-icon><Wallet /></el-icon>
              <template #title>收入管理</template>
            </el-menu-item>
            <el-menu-item index="/provider/schedule-manage" v-if="!userStore.isShopStaff()">
              <el-icon><Calendar /></el-icon>
              <template #title>排班管理</template>
            </el-menu-item>
            <el-menu-item index="/provider/shop-manage">
              <el-icon><OfficeBuilding /></el-icon>
              <template #title>店铺管理</template>
            </el-menu-item>
          </template>

          <!-- 店员专属入口（仅店员可见） -->
          <template v-if="userStore.isShopStaff()">
            <div class="menu-divider">{{ collapsed ? '·' : '我的工作' }}</div>
            <el-menu-item index="/provider/schedule-manage">
              <el-icon><Calendar /></el-icon>
              <template #title>我的排班</template>
            </el-menu-item>
            <el-menu-item index="/staff/reviews">
              <el-icon><ChatDotRound /></el-icon>
              <template #title>评价查看</template>
            </el-menu-item>
          </template>

          <!-- 评价中心（所有角色可见） -->
          <el-menu-item index="/staff/reviews" v-if="!userStore.isShopStaff()">
            <el-icon><ChatDotRound /></el-icon>
            <template #title>服务评价</template>
          </el-menu-item>
        </el-menu>
      </div>
    </el-aside>

    <!-- 主内容区 -->
    <el-container class="main-body">
      <!-- 顶部导航栏 -->
      <el-header height="64px" class="header">
        <div class="header-left">
          <button class="collapse-btn" @click="collapsed = !collapsed">
            <el-icon :size="20"><Fold /></el-icon>
          </button>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/home' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-if="route.path !== '/home'">{{ route.meta.title || currentPathName }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <el-tag :type="roleTagType" size="small" effect="dark" round class="role-tag">{{ roleLabel }}</el-tag>
          <el-dropdown @command="handleCommand">
            <div class="user-info">
              <el-avatar :size="36" :style="{background: avatarBg}" :src="userAvatarUrl || undefined" class="user-avatar">
                {{ (userStore.userInfo.username || 'U').charAt(0).toUpperCase() }}
              </el-avatar>
              <span class="username">{{ userStore.userInfo.realName || userStore.userInfo.username || '未登录' }}</span>
              <el-icon><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile"><el-icon><User /></el-icon> 个人中心</el-dropdown-item>
                <el-dropdown-item command="logout" divided><el-icon><SwitchButton /></el-icon> 退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 内容区 -->
      <el-main class="main-content">
        <router-view v-slot="{ Component }">
          <transition name="page-fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>

      <!-- 移动端底部导航 -->
      <div class="mobile-nav">
        <router-link to="/home" class="mobile-nav-item" :class="{ active: currentRoute === '/home' }">
          <span class="nav-icon">🏠</span><span>首页</span>
        </router-link>
        <router-link to="/services" class="mobile-nav-item" :class="{ active: currentRoute === '/services' }">
          <span class="nav-icon">✨</span><span>服务</span>
        </router-link>
        <router-link to="/orders" class="mobile-nav-item" :class="{ active: currentRoute === '/orders' }">
          <span class="nav-icon">📋</span><span>订单</span>
        </router-link>
        <router-link to="/user/profile" class="mobile-nav-item" :class="{ active: currentRoute === '/user' }">
          <span class="nav-icon">👤</span><span>我的</span>
        </router-link>
      </div>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { Fold, SwitchButton, Calendar, ChatDotRound } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const collapsed = ref(false)

const currentRoute = computed(() => '/' + route.path.split('/')[1])
const currentPathName = computed(() => {
  const parts = route.path.split('/').filter(Boolean)
  return parts[parts.length - 1] || ''
})

const roleTagType = computed(() => {
  const r = userStore.userInfo.role
  if (r === 2) return 'danger'
  if (r === 1) {
    const sr = userStore.userInfo.staffRole
    return sr === 2 ? 'success' : 'warning'
  }
  return ''
})
const roleLabel = computed(() => {
  const r = userStore.userInfo.role
  if (r === 2) return '管理员'
  if (r === 1) {
    const sr = userStore.userInfo.staffRole
    return sr === 2 ? '店员' : '店长'
  }
  return '客户'
})
const avatarBg = computed(() => {
  const colors = ['#FF8C42','#E6A23C','#98DDCA','#F9A8D4','#6C9EBF']
  const name = userStore.userInfo.username || 'U'
  const idx = name.charCodeAt(0) % colors.length
  return colors[idx]
})
const userAvatarUrl = computed(() => {
  const url = userStore.userInfo.avatar
  if (!url || !url.trim()) return ''
  // 如果是相对路径，补全后端地址
  if (url.startsWith('http://') || url.startsWith('https://') || url.startsWith('data:')) return url
  return `http://localhost:8081${url.startsWith('/') ? '' : '/'}${url}`
})

function handleCommand(cmd) {
  if (cmd === 'logout') {
    userStore.logout()
    router.push('/login')
  } else if (cmd === 'profile') {
    router.push('/user/profile')
  }
}
</script>

<style scoped>
.main-layout { min-height: 100vh; }

/* ===== 侧边栏 ===== */
.sidebar {
  background: linear-gradient(180deg, #3D3226 0%, #5C4A3A 50%, #6D5A48 100%);
  transition: width 0.3s cubic-bezier(0.4,0,0.2,1);
  overflow-x: hidden;
  overflow-y: auto;
  position: relative;
}
.sidebar::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='60' height='60'%3E%3Cpath d='M30 5 L35 15 L30 12 L25 15Z' fill='rgba(255,255,255,0.02)'/%3E%3C/svg%3E");
  pointer-events: none;
}
.sidebar-inner { position: relative; z-index: 1; }

.logo-area {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 20px 22px;
  cursor: pointer;
  border-bottom: 1px solid rgba(255,255,255,0.06);
  white-space: nowrap;
  overflow: hidden;
  transition: var(--transition-normal);
}
.logo-area:hover { background: rgba(255,255,255,0.04); }
.logo-emoji { font-size: 28px; flex-shrink: 0; }
.logo-text {
  font-size: 16px;
  font-weight: 700;
  color: #fff;
  letter-spacing: 2px;
}

.side-menu {
  border: none;
  padding-top: 8px;
}
.side-menu .el-menu-item {
  border-radius: 12px;
  margin: 2px 10px;
  height: 44px;
  line-height: 44px;
  transition: var(--transition-fast);
}
.side-menu .el-menu-item:hover {
  background: rgba(255,140,66,0.15) !important;
}
.side-menu .el-menu-item.is-active {
  background: linear-gradient(135deg, rgba(255,140,66,0.25), rgba(255,140,66,0.15)) !important;
  color: #fff !important;
  font-weight: 600;
}
.side-menu .el-sub-menu :deep(.el-sub-menu__title) {
  border-radius: 12px;
  margin: 2px 10px;
  height: 44px;
  line-height: 44px;
}
.side-menu .el-sub-menu :deep(.el-sub-menu__title:hover) {
  background: rgba(255,140,66,0.15) !important;
}

.menu-divider {
  padding: 16px 22px 8px;
  font-size: 10px;
  color: rgba(255,255,255,0.35);
  text-transform: uppercase;
  letter-spacing: 2px;
  font-weight: 600;
}

/* ===== 顶部导航 ===== */
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: var(--color-surface);
  border-bottom: 1px solid var(--color-divider);
  padding: 0 24px;
  position: sticky;
  top: 0;
  z-index: 100;
}
.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}
.collapse-btn {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  border: 1.5px solid var(--color-border);
  background: var(--color-cream);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: var(--transition-fast);
  color: var(--color-text-secondary);
}
.collapse-btn:hover {
  border-color: var(--color-primary);
  color: var(--color-primary);
  background: var(--color-pink-light);
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}
.role-tag {
  font-weight: 600;
  letter-spacing: 1px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  color: var(--color-text);
  padding: 4px 8px;
  border-radius: var(--radius-full);
  transition: var(--transition-fast);
}
.user-info:hover { background: var(--color-cream); }
.user-avatar {
  font-weight: 700;
  color: #fff !important;
}
.username {
  font-size: 14px;
  font-weight: 600;
}

/* ===== 内容区 ===== */
.main-content {
  background: var(--color-bg);
  min-height: calc(100vh - 64px);
  padding: 20px;
  position: relative;
}
@media (min-width: 768px) {
  .main-content { padding: 24px; }
}

/* 移动端底部导航 */
.mobile-nav {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  z-index: 999;
  background: var(--glass-bg);
  backdrop-filter: var(--glass-blur);
  -webkit-backdrop-filter: var(--glass-blur);
  border-top: 1px solid var(--color-border);
  display: none;
  padding: 6px 0 env(safe-area-inset-bottom, 8px);
}
.mobile-nav-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
  padding: 6px 0;
  color: var(--color-text-placeholder);
  font-size: 10px;
  font-weight: 500;
  text-decoration: none;
  transition: var(--transition-fast);
}
.mobile-nav-item.active {
  color: var(--color-primary);
}
.mobile-nav-item .nav-icon {
  font-size: 22px;
  line-height: 1;
}
@media (max-width: 768px) {
  .mobile-nav { display: flex; }
  .sidebar { display: none !important; }
  .main-content { padding-bottom: 76px !important; }
  .header-left .collapse-btn { display: none; }
}

/* ===== 过渡动画 ===== */
.fade-enter-active, .fade-leave-active { transition: opacity 0.2s; }
.fade-enter-from, .fade-leave-to { opacity: 0; }

.page-fade-enter-active { transition: all 0.25s ease-out; }
.page-fade-leave-active { transition: all 0.15s ease-in; }
.page-fade-enter-from { opacity: 0; transform: translateY(10px); }
.page-fade-leave-to { opacity: 0; transform: translateY(-5px); }
</style>
