<template>
  <div class="admin-layout">
    <!-- 侧边栏 -->
    <aside class="admin-sidebar" :class="{ collapsed }">
      <div class="sidebar-header" @click="collapsed = !collapsed">
        <span class="logo-icon">🐾</span>
        <transition name="fade"><span v-show="!collapsed" class="logo-text">管理后台</span></transition>
      </div>

      <nav class="sidebar-nav">
        <div class="nav-section" v-show="!collapsed">数据概览</div>
        <router-link to="/admin/dashboard" class="nav-item" :class="{ active: route.path === '/admin/dashboard' }">
          <el-icon><DataLine /></el-icon><span v-show="!collapsed">综合驾驶舱</span>
        </router-link>

        <div class="nav-section" v-show="!collapsed">业务管理</div>
        <router-link to="/admin/users" class="nav-item" :class="{ active: route.path === '/admin/users' }">
          <el-icon><UserFilled /></el-icon><span v-show="!collapsed">用户管理</span>
        </router-link>
        <router-link to="/admin/providers" class="nav-item" :class="{ active: route.path === '/admin/providers' }">
          <el-icon><Shop /></el-icon><span v-show="!collapsed">服务商审核</span>
        </router-link>
        <router-link to="/admin/orders" class="nav-item" :class="{ active: route.path === '/admin/orders' }">
          <el-icon><Tickets /></el-icon><span v-show="!collapsed">订单监管</span>
        </router-link>

        <div class="nav-section" v-show="!collapsed">运营</div>
        <router-link to="/admin/services" class="nav-item" :class="{ active: route.path === '/admin/services' }">
          <el-icon><Grid /></el-icon><span v-show="!collapsed">服务项目</span>
        </router-link>
        <router-link to="/admin/marketing" class="nav-item" :class="{ active: route.path === '/admin/marketing' }">
          <el-icon><Present /></el-icon><span v-show="!collapsed">营销管理</span>
        </router-link>

        <div class="nav-section" v-show="!collapsed">系统</div>
        <router-link to="/admin/risk" class="nav-item" :class="{ active: route.path === '/admin/risk' }">
          <el-icon><Warning /></el-icon><span v-show="!collapsed">风控管理</span>
        </router-link>
        <router-link to="/admin/config" class="nav-item" :class="{ active: route.path === '/admin/config' }">
          <el-icon><Setting /></el-icon><span v-show="!collapsed">系统配置</span>
        </router-link>
        <router-link to="/admin/logs" class="nav-item" :class="{ active: route.path === '/admin/logs' }">
          <el-icon><Document /></el-icon><span v-show="!collapsed">系统日志</span>
        </router-link>
      </nav>
    </aside>

    <!-- 主区域 -->
    <div class="admin-main">
      <header class="admin-header">
        <div class="header-left">
          <button class="collapse-btn" @click="collapsed = !collapsed">
            <el-icon :size="20"><Fold /></el-icon>
          </button>
          <h3 class="page-title">{{ route.meta.title || '管理后台' }}</h3>
        </div>
        <div class="header-right">
          <el-badge :value="3" :max="9" class="notify-badge">
            <el-icon :size="20" style="cursor:pointer"><Bell /></el-icon>
          </el-badge>
          <el-dropdown @command="handleCommand">
            <div class="admin-user">
              <el-avatar :size="36" :style="{background:'#FF8C42'}">{{ (userStore.userInfo.username || 'A').charAt(0).toUpperCase() }}</el-avatar>
              <div class="user-meta" v-show="true">
                <span class="user-name">{{ userStore.userInfo.username || '管理员' }}</span>
                <span class="user-role">超级管理员</span>
              </div>
              <el-icon><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout"><el-icon><SwitchButton /></el-icon> 退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>
      <main class="admin-content">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const collapsed = ref(false)

function handleCommand(cmd) {
  if (cmd === 'logout') {
    userStore.logout()
    router.push('/login')
  }
}
</script>

<style scoped>
.admin-layout {
  min-height: 100vh;
  display: flex;
  background: #0F1923;
  color: #E0E6ED;
}

/* ===== 侧边栏 ===== */
.admin-sidebar {
  width: 260px;
  background: linear-gradient(180deg, #162231 0%, #0F1923 100%);
  border-right: 1px solid rgba(255,140,66,0.08);
  transition: width 0.3s cubic-bezier(0.4,0,0.2,1);
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
  overflow-x: hidden;
  overflow-y: auto;
}
.admin-sidebar.collapsed { width: 72px; }

.sidebar-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 22px 20px;
  cursor: pointer;
  border-bottom: 1px solid rgba(255,140,66,0.08);
  white-space: nowrap;
}
.logo-icon { font-size: 28px; flex-shrink: 0; }
.logo-text { font-size: 18px; font-weight: 800; color: #FF8C42; letter-spacing: 2px; font-family: 'Poppins', sans-serif; }

.sidebar-nav { padding: 12px 0; flex: 1; }
.nav-section {
  padding: 16px 24px 6px;
  font-size: 11px;
  text-transform: uppercase;
  letter-spacing: 2px;
  color: #4A5568;
  font-weight: 600;
}
.nav-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 11px 24px;
  margin: 2px 10px;
  border-radius: 12px;
  color: #8899AA;
  text-decoration: none;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.25s ease;
  white-space: nowrap;
}
.nav-item:hover { color: #E0E6ED; background: rgba(255,140,66,0.08); }
.nav-item.active {
  color: #FF8C42;
  background: rgba(255,140,66,0.12);
  font-weight: 600;
  box-shadow: 0 0 20px rgba(255,140,66,0.06);
}
.nav-item .el-icon { font-size: 18px; flex-shrink: 0; }

.collapsed .nav-section { text-align: center; padding: 12px 0 4px; font-size: 9px; }
.collapsed .nav-item { justify-content: center; padding: 12px 0; margin: 2px 8px; }

/* ===== 主区域 ===== */
.admin-main { flex: 1; display: flex; flex-direction: column; min-width: 0; }

.admin-header {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 28px;
  background: rgba(15,25,35,0.85);
  backdrop-filter: blur(16px);
  border-bottom: 1px solid rgba(255,140,66,0.06);
  position: sticky;
  top: 0;
  z-index: 20;
}
.header-left { display: flex; align-items: center; gap: 16px; }
.collapse-btn {
  width: 36px; height: 36px;
  border-radius: 10px;
  border: 1px solid rgba(255,140,66,0.1);
  background: transparent;
  color: #8899AA;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}
.collapse-btn:hover { color: #FF8C42; border-color: rgba(255,140,66,0.3); }
.page-title { font-family: 'Poppins', sans-serif; font-size: 18px; font-weight: 700; color: #E0E6ED; }

.header-right { display: flex; align-items: center; gap: 24px; }
.notify-badge { line-height: 1; }
.notify-badge .el-icon { color: #8899AA; cursor: pointer; transition: color 0.2s; }
.notify-badge .el-icon:hover { color: #FF8C42; }

.admin-user {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 12px;
  transition: background 0.2s;
}
.admin-user:hover { background: rgba(255,140,66,0.08); }
.user-meta { display: flex; flex-direction: column; }
.user-name { font-size: 13px; font-weight: 600; color: #E0E6ED; }
.user-role { font-size: 11px; color: #FF8C42; }
.admin-user .el-icon { color: #8899AA; }

.admin-content {
  flex: 1;
  padding: 24px 28px;
  overflow-y: auto;
  background: #0F1923;
}

/* ===== 暗色 Element Plus 覆盖 ===== */
:deep(.el-table) {
  --el-table-bg-color: #162231;
  --el-table-tr-bg-color: #162231;
  --el-table-header-bg-color: #1A2B3C;
  --el-table-row-hover-bg-color: rgba(255,140,66,0.06);
  --el-table-border-color: rgba(255,140,66,0.06);
  --el-table-text-color: #A0AEC0;
  --el-table-header-text-color: #E0E6ED;
  border-radius: 16px;
}
:deep(.el-table th.el-table__cell) { font-weight: 600; }
:deep(.el-tag) { border-radius: 20px; }
:deep(.el-dialog) { background: #162231; border-radius: 24px; }
:deep(.el-dialog__header) { border-bottom: 1px solid rgba(255,140,66,0.08); }
:deep(.el-dialog__title) { color: #E0E6ED; }
:deep(.el-form-item__label) { color: #A0AEC0; }
:deep(.el-input__wrapper) { background: #1A2B3C; box-shadow: none; border: 1px solid rgba(255,140,66,0.1); }
:deep(.el-input__inner) { color: #E0E6ED; }
:deep(.el-pagination) { --el-pagination-bg-color: #162231; --el-pagination-text-color: #A0AEC0; --el-pagination-button-disabled-bg-color: #1A2B3C; }
:deep(.el-select-dropdown) { background: #1A2B3C; }
:deep(.el-select-dropdown__item) { color: #A0AEC0; }
:deep(.el-select-dropdown__item.is-hovering) { background: rgba(255,140,66,0.1); }

.fade-enter-active, .fade-leave-active { transition: opacity 0.15s; }
.fade-enter-from, .fade-leave-to { opacity: 0; }

@media (max-width: 768px) {
  .admin-sidebar { position: fixed; left: -260px; z-index: 100; height: 100vh; transition: left 0.3s; }
  .admin-sidebar:not(.collapsed) { left: 0; }
  .admin-sidebar.collapsed { left: -72px; }
  .admin-content { padding: 16px; }
  .user-meta { display: none; }
}
</style>
