<template>
  <div class="home-page">
    <!-- Hero Banner -->
    <section class="hero-banner">
      <div class="hero-bg"></div>
      <div class="hero-content">
        <div class="hero-mascot animate-float">🐕</div>
        <h1 class="hero-title">宠物一站式服务平台</h1>
        <p class="hero-desc">专业美容 · 安心寄养 · 医疗护理 · 贴心遛宠</p>
        <div class="hero-actions">
          <el-button type="primary" size="large" round @click="$router.push('/services')">浏览服务</el-button>
          <el-button size="large" round plain @click="$router.push(userStore.token?'/orders':'/login')" :type="userStore.token?'':'primary'">
            {{ userStore.token?'查看订单':'立即登录' }}
          </el-button>
        </div>
      </div>
      <div class="hero-particles">
        <span class="particle p1">🐕</span>
        <span class="particle p2">🐈</span>
        <span class="particle p3">🐾</span>
        <span class="particle p4">🦜</span>
        <span class="particle p5">🐇</span>
      </div>
    </section>

    <!-- 签到 & 公告 -->
    <section class="notice-bar glass-card">
      <div class="notice-left">
        <span class="notice-icon">📢</span>
        <span class="notice-text">新用户注册即享首单8折优惠！</span>
      </div>
      <button class="checkin-btn" @click="handleCheckin">
        <span>📅</span> 每日签到
      </button>
    </section>

    <!-- 服务分类入口 -->
    <section class="card section-card">
      <h3 class="section-title"><span class="title-icon">📂</span> 服务分类</h3>
      <el-row :gutter="16">
        <el-col :xs="12" :sm="6" v-for="cat in categories" :key="cat.value">
          <div class="category-card" @click="$router.push(`/services?category=${cat.value}`)">
            <div class="cat-icon-wrap" :style="{background: cat.bg}">
              <span class="cat-emoji">{{ cat.emoji }}</span>
            </div>
            <strong>{{ cat.label }}</strong>
            <p>{{ cat.desc }}</p>
          </div>
        </el-col>
      </el-row>
    </section>

    <!-- AI推荐 / 今日特价 -->
    <section class="card section-card" v-if="userStore.token">
      <div class="page-header">
        <h3 class="section-title"><span class="title-icon">🤖</span> AI 为你推荐</h3>
        <el-tag type="warning" effect="dark" round size="small">智能推荐</el-tag>
      </div>
      <el-row :gutter="16" v-loading="loading">
        <el-col :xs="12" :sm="8" :md="6" v-for="(item,idx) in hotServices" :key="item.id">
          <div class="service-card" @click="$router.push(`/services/${item.id}`)">
            <div class="service-cover" :style="{background: coverColors[idx%coverColors.length]}">
              <span class="cover-emoji">{{ categoryEmoji[item.category] || '🐾' }}</span>
              <span class="ai-badge">AI推荐</span>
            </div>
            <div class="card-body">
              <h4>{{ item.name }}</h4>
              <el-tag :type="catTagType(item.category)" size="small" effect="plain" round>{{ categoryMap[item.category] || '其他' }}</el-tag>
              <div class="price-row">
                <span class="price">¥{{ item.price }}</span>
                <small>/ {{ item.duration }}分钟</small>
              </div>
            </div>
          </div>
        </el-col>
      </el-row>
    </section>

    <!-- 热门服务 -->
    <section class="card section-card">
      <div class="page-header">
        <h3 class="section-title"><span class="title-icon">🔥</span> 热门服务</h3>
        <el-button type="primary" text @click="$router.push('/services')">查看全部 →</el-button>
      </div>
      <el-row :gutter="16" v-loading="loading">
        <el-col :xs="12" :sm="8" :md="6" v-for="(item,idx) in hotServices" :key="'hot-'+item.id">
          <div class="service-card" @click="$router.push(`/services/${item.id}`)">
            <div class="service-cover" :style="{background: coverColors[(idx+3)%coverColors.length]}">
              <span class="cover-emoji">{{ categoryEmoji[item.category] || '🐾' }}</span>
            </div>
            <div class="card-body">
              <h4>{{ item.name }}</h4>
              <el-tag :type="catTagType(item.category)" size="small" effect="plain" round>{{ categoryMap[item.category] || '其他' }}</el-tag>
              <div class="price-row">
                <span class="price">¥{{ item.price }}</span>
                <small>/ {{ item.duration }}分钟</small>
              </div>
              <p class="desc">{{ item.description }}</p>
            </div>
          </div>
        </el-col>
      </el-row>
      <el-empty v-if="!loading && hotServices.length === 0" description="暂无服务数据" />
    </section>

    <!-- 萌宠社区入口 -->
    <section class="community-entry glass-card" @click="$router.push('/community')">
      <div class="community-left">
        <span class="community-emoji">🐱</span>
        <div>
          <h3>萌宠社区</h3>
          <p>分享毛孩子日常，和宠友互动</p>
        </div>
      </div>
      <span class="community-arrow">→</span>
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getServices } from '@/api/service'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()
const loading = ref(false)
const hotServices = ref([])

const coverColors = [
  'linear-gradient(135deg,#FF8C42 0%,#FFA96B 100%)',
  'linear-gradient(135deg,#98DDCA 0%,#6CC4A1 100%)',
  'linear-gradient(135deg,#F9A8D4 0%,#F472B6 100%)',
  'linear-gradient(135deg,#6C9EBF 0%,#93C5FD 100%)',
  'linear-gradient(135deg,#FFD1DC 0%,#FFB7C5 100%)',
  'linear-gradient(135deg,#E6A23C 0%,#F6C96B 100%)',
  'linear-gradient(135deg,#A78BFA 0%,#C4B5FD 100%)',
  'linear-gradient(135deg,#FF6B6B 0%,#FFA07A 100%)'
]
const categoryEmoji = { 1:'✨', 2:'🏠', 3:'💉', 4:'🚶' }
const categoryMap = { 1:'美容', 2:'寄养', 3:'医疗', 4:'遛狗' }
function catTagType(c) { return { 1:'', 2:'success', 3:'danger', 4:'warning' }[c] || '' }

const categories = [
  { label:'宠物美容', value:1, emoji:'✂️', desc:'洗护/造型/修剪', bg:'linear-gradient(135deg,#FF8C42,#FFA96B)' },
  { label:'寄养托管', value:2, emoji:'🏠', desc:'24h专人看护', bg:'linear-gradient(135deg,#98DDCA,#6CC4A1)' },
  { label:'医疗健康', value:3, emoji:'💊', desc:'体检/疫苗/诊疗', bg:'linear-gradient(135deg,#F9A8D4,#F472B6)' },
  { label:'遛狗服务', value:4, emoji:'🚶', desc:'专业户外陪护', bg:'linear-gradient(135deg,#6C9EBF,#93C5FD)' }
]

function handleCheckin() {
  ElMessage.success('签到成功 +10积分 🎉')
}

onMounted(async () => {
  loading.value = true
  try {
    const res = await getServices({ page: 1, size: 8 })
    hotServices.value = res.data.records || []
  } finally { loading.value = false }
})
</script>

<style scoped>
.home-page { padding: 0 !important; max-width: 1200px; margin: 0 auto; }

/* ===== Hero Banner ===== */
.hero-banner {
  position: relative;
  height: 320px;
  border-radius: var(--radius-lg);
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 20px;
  animation: fade-up 0.5s ease-out;
}
.hero-bg {
  position: absolute;
  inset: 0;
  background: url('https://img95.699pic.com/photo/60033/0673.jpg_wh300.jpg') center / cover no-repeat;
}
.hero-bg::before {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, rgba(61,50,38,0.82) 0%, rgba(92,74,58,0.75) 30%, rgba(255,140,66,0.65) 70%, rgba(249,168,212,0.6) 100%);
}
.hero-bg::after {
  content: '';
  position: absolute;
  inset: 0;
  background: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 1440 320'%3E%3Cpath fill='%23ffffff' fill-opacity='.04' d='M0,192L48,176C96,160,192,128,288,138.7C384,149,480,203,576,208C672,213,768,171,864,149.3C960,128,1056,128,1152,144C1248,160,1344,192,1392,208L1440,224L1440,320L1392,320C1344,320,1248,320,1152,320C1056,320,960,320,864,320C768,320,672,320,576,320C480,320,384,320,288,320C192,320,96,320,48,320L0,320Z'%3E%3C/path%3E%3C/svg%3E") bottom no-repeat;
  background-size: cover;
}
.hero-content { text-align: center; color: #fff; z-index: 1; position: relative; }
.hero-mascot { font-size: 64px; margin-bottom: 8px; }
.hero-title { font-size: 32px; font-weight: 800; margin-bottom: 10px; letter-spacing: 2px; text-shadow: 0 2px 12px rgba(0,0,0,.2); color: #fff; }
.hero-desc { font-size: 15px; opacity: 0.88; margin-bottom: 24px; letter-spacing: 2px; }
.hero-actions { display: flex; gap: 12px; justify-content: center; }
.hero-particles { position: absolute; inset: 0; pointer-events: none; }
.particle { position: absolute; font-size: 24px; opacity: 0.2; animation: float 5s ease-in-out infinite; }
.p1 { top:15%; left:8%; animation-delay:0s; }
.p2 { top:55%; right:10%; animation-delay:1s; }
.p3 { bottom:20%; left:18%; animation-delay:2s; }
.p4 { top:25%; right:20%; animation-delay:3s; }
.p5 { top:50%; left:42%; animation-delay:4s; }

/* ===== 公告栏 ===== */
.notice-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 20px;
  margin-bottom: 20px;
  border-radius: var(--radius-md);
}
.notice-left { display: flex; align-items: center; gap: 10px; }
.notice-icon { font-size: 18px; }
.notice-text { font-size: 14px; color: var(--color-text-secondary); }
.checkin-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 6px 16px;
  border-radius: var(--radius-full);
  border: 1.5px solid var(--color-primary);
  background: var(--color-cream);
  color: var(--color-primary);
  cursor: pointer;
  font-size: 13px;
  font-weight: 600;
  transition: var(--transition-fast);
}
.checkin-btn:hover {
  background: var(--color-primary);
  color: #fff;
  transform: translateY(-1px);
}

/* ===== 分类卡片 ===== */
.section-card { padding: 24px; margin-bottom: 20px; }
.section-title { font-size: 18px; font-weight: 700; color: var(--color-text); margin-bottom: 16px; display: flex; align-items: center; gap: 8px; }
.title-icon { font-size: 20px; }

.category-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  padding: 24px 12px 18px;
  border-radius: var(--radius-lg);
  background: var(--color-surface);
  cursor: pointer;
  transition: var(--transition-normal);
  border: 1.5px solid var(--color-divider);
}
.category-card:hover {
  transform: translateY(-6px);
  box-shadow: var(--shadow-md);
  border-color: transparent;
}
.cat-icon-wrap {
  width: 56px;
  height: 56px;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
}
.cat-emoji { font-size: 28px; }
.category-card strong { font-size: 14px; color: var(--color-text); }
.category-card p { font-size: 11px; color: var(--color-text-placeholder); margin: 0; }

/* ===== 服务卡片 ===== */
.service-card {
  cursor: pointer;
  margin-bottom: 16px;
  border-radius: var(--radius-lg);
  overflow: hidden;
  background: var(--color-surface);
  border: 1.5px solid var(--color-divider);
  transition: var(--transition-normal);
}
.service-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-md);
  border-color: transparent;
}
.service-cover {
  width: 100%;
  height: 140px;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}
.cover-emoji { font-size: 40px; filter: drop-shadow(0 4px 12px rgba(0,0,0,.15)); }
.ai-badge {
  position: absolute;
  top: 10px;
  right: 10px;
  background: rgba(255,255,255,0.9);
  color: var(--color-primary);
  font-size: 10px;
  font-weight: 700;
  padding: 2px 8px;
  border-radius: var(--radius-full);
}
.card-body { padding: 14px; }
.card-body h4 { font-size: 15px; color: var(--color-text); margin-bottom: 6px; }
.price-row { display: flex; align-items: baseline; gap: 2px; margin: 6px 0 4px; }
.price { color: var(--color-primary); font-size: 18px; font-weight: 700; }
.price-row small { font-size: 12px; color: var(--color-text-placeholder); }
.desc { color: var(--color-text-placeholder); font-size: 12px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; margin: 0; }

/* ===== 社区入口 ===== */
.community-entry {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 24px;
  margin-bottom: 20px;
  cursor: pointer;
  transition: var(--transition-normal);
}
.community-entry:hover { transform: translateY(-2px); box-shadow: var(--shadow-md); }
.community-left { display: flex; align-items: center; gap: 16px; }
.community-emoji { font-size: 40px; animation: float 3s ease-in-out infinite; }
.community-left h3 { font-size: 17px; font-weight: 700; margin-bottom: 2px; }
.community-left p { font-size: 13px; color: var(--color-text-secondary); margin: 0; }
.community-arrow { font-size: 20px; color: var(--color-text-placeholder); font-weight: 700; }

@media (max-width: 768px) {
  .hero-banner { height: 240px; border-radius: 0; margin: -20px -20px 16px; }
  .hero-title { font-size: 24px; }
  .hero-desc { font-size: 13px; }
  .hero-mascot { font-size: 48px; }
}
</style>
