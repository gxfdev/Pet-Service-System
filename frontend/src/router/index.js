import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue'),
    meta: { title: '注册' }
  },

  // ===== 主布局（客户 + 店员 + 店长 统一） =====
  {
    path: '/',
    component: () => import('@/layouts/MainLayout.vue'),
    redirect: '/home',
    children: [
      // 公共页面
      { path: 'home', name: 'Home', component: () => import('@/views/home/HomePage.vue'), meta: { title: '首页' } },
      { path: 'services', name: 'ServiceList', component: () => import('@/views/service/ServiceList.vue'), meta: { title: '服务列表' } },
      { path: 'services/:id', name: 'ServiceDetail', component: () => import('@/views/service/ServiceDetail.vue'), meta: { title: '服务详情' } },
      { path: 'community', name: 'Community', component: () => import('@/views/community/CommunityPage.vue'), meta: { title: '萌宠圈' } },
      { path: 'points', name: 'PointsShop', component: () => import('@/views/points/PointsShop.vue'), meta: { title: '积分商城' } },
      // 用户
      { path: 'user/profile', name: 'Profile', component: () => import('@/views/user/Profile.vue'), meta: { title: '个人中心' } },
      { path: 'user/pets', name: 'MyPets', component: () => import('@/views/user/PetManage.vue'), meta: { title: '我的宠物' } },
      { path: 'orders', name: 'OrderList', component: () => import('@/views/order/OrderList.vue'), meta: { title: '我的订单' } },

      // 服务商工作台（根据角色自动跳转）
      { path: 'provider/dashboard', name: 'ProviderDashboard', component: () => import('@/views/provider/Dashboard.vue'), meta: { title: '工作台', roles: [1] } },
      { path: 'provider/owner-dashboard', name: 'OwnerDashboard', component: () => import('@/views/provider/OwnerDashboard.vue'), meta: { title: '店主工作台' } },
      { path: 'provider/staff-dashboard', name: 'StaffDashboard', component: () => import('@/views/provider/StaffDashboard.vue'), meta: { title: '员工工作台' } },
      { path: 'provider/services', name: 'ProviderServices', component: () => import('@/views/provider/ServiceManage.vue'), meta: { title: '服务管理', roles: [1] } },
      { path: 'provider/income', name: 'IncomeManage', component: () => import('@/views/provider/IncomeManage.vue'), meta: { title: '收入管理', roles: [1] } },
      { path: 'provider/shop-manage', name: 'ShopOwnerManage', component: () => import('@/views/provider/ShopOwnerManage.vue'), meta: { title: '店铺管理', roles: [1] } },

      // 排班管理（店长+店员共用）
      { path: 'provider/schedule-manage', name: 'ScheduleManage', component: () => import('@/views/provider/ScheduleManage.vue'), meta: { title: '排班管理', roles: [1] } },
      { path: 'staff/schedule', name: 'StaffSchedule', component: () => import('@/views/provider/StaffSchedule.vue'), meta: { title: '排班日程', roles: [1] } },
      { path: 'staff/reviews', name: 'StaffReviews', component: () => import('@/views/provider/StaffReviews.vue'), meta: { title: '评价管理', roles: [0, 1] } },
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, _from, next) => {
  document.title = to.meta.title ? `${to.meta.title} - 宠物服务系统` : '宠物服务系统'

  const userStore = useUserStore()
  const publicPages = ['/login', '/register']
  const isPublic = publicPages.includes(to.path)

  if (!isPublic && !userStore.token) {
    next('/login')
  } else if (to.meta.roles && !to.meta.roles.includes(userStore.userInfo.role)) {
    // 角色不匹配：统一跳转
    const role = userStore.userInfo.role
    if (role === 1 || role === 2) next('/provider/dashboard')
    else next('/home')
  } else if (to.path === '/provider/dashboard') {
    // Dashboard 入口自动分流
    const role = userStore.userInfo.role
    if (role === 2 || userStore.isShopOwner()) next('/provider/owner-dashboard')
    else if (userStore.isShopStaff()) next('/provider/staff-dashboard')
    else next(to.path)
  } else {
    next()
  }
})

export default router
