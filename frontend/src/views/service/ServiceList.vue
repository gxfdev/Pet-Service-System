<template>
  <div class="page-container">
    <div class="card">
      <div class="page-header">
        <h2 class="page-title"><span class="title-icon">✨</span> 服务列表</h2>
        <div class="filters">
          <el-radio-group v-model="filterCategory" @change="fetchData" size="small" class="filter-group">
            <el-radio-button :value="null">全部</el-radio-button>
            <el-radio-button :value="1">✂️ 美容</el-radio-button>
            <el-radio-button :value="2">🏠 寄养</el-radio-button>
            <el-radio-button :value="3">💊 医疗</el-radio-button>
            <el-radio-button :value="4">🚶 遛狗</el-radio-button>
          </el-radio-group>
          <el-input v-model="keyword" placeholder="搜索服务..." clearable class="search-input"
                    @keyup.enter="fetchData" @clear="fetchData">
            <template #prefix><el-icon><Search /></el-icon></template>
          </el-input>
        </div>
      </div>

      <!-- 今日特价横幅 -->
      <div class="sale-banner" v-if="filterCategory === null">
        <span class="sale-emoji">🎉</span>
        <span>今日特价专区 — 部分服务限时8折！</span>
        <el-tag type="danger" effect="dark" round size="small">HOT</el-tag>
      </div>

      <el-row :gutter="16" v-loading="loading">
        <el-col :xs="12" :sm="8" :md="6" v-for="(item,idx) in list" :key="item.id">
          <div class="service-card" @click="$router.push(`/services/${item.id}`)">
            <div class="service-cover" :style="{background: coverColors[idx%coverColors.length]}">
              <span class="cover-emoji">{{ categoryEmoji[item.category] || '🐾' }}</span>
              <span class="category-badge" v-if="categoryMap[item.category]">{{ categoryMap[item.category] }}</span>
            </div>
            <div class="card-body">
              <h4>{{ item.name }}</h4>
              <div class="price-row">
                <span class="price">¥{{ item.price }}</span>
                <small>/ {{ item.duration }}分钟</small>
              </div>
              <p class="desc">{{ item.description }}</p>
              <div class="card-tags">
                <el-tag type="success" size="small" effect="plain" round>随时退款</el-tag>
                <el-tag size="small" effect="plain" round>专业认证</el-tag>
              </div>
            </div>
          </div>
        </el-col>
      </el-row>

      <el-empty v-if="!loading && list.length === 0" description="暂无服务数据" />
      <div style="text-align:center;margin-top:24px" v-if="total > pageSize">
        <el-pagination layout="prev,pager,next" :total="total" :page-size="pageSize"
                       v-model:current-page="currentPage" @current-change="fetchData" background />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getServices } from '@/api/service'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = 8
const filterCategory = ref(null)
const keyword = ref('')

const categoryMap = { 1:'美容', 2:'寄养', 3:'医疗', 4:'遛狗' }
const categoryEmoji = { 1:'✂️', 2:'🏠', 3:'💊', 4:'🚶' }

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

async function fetchData() {
  loading.value = true
  try {
    const res = await getServices({
      page: currentPage.value, size: pageSize,
      category: filterCategory.value ?? undefined,
      keyword: keyword.value || undefined
    })
    list.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (e) {
    ElMessage.error('获取服务列表失败')
  } finally { loading.value = false }
}

fetchData()
</script>

<style scoped>
.page-header { flex-wrap: wrap; }
.filters { display: flex; align-items: center; flex-wrap: wrap; gap: 10px; }
.filter-group :deep(.el-radio-button__inner) {
  border-radius: var(--radius-full) !important;
  font-weight: 500;
}
.search-input { width: 200px; }
.search-input :deep(.el-input__wrapper) {
  border-radius: var(--radius-full) !important;
  background: var(--color-cream) !important;
  box-shadow: none !important;
  border: 1.5px solid transparent;
}
.search-input :deep(.el-input__wrapper:focus-within) {
  background: #fff !important;
  border-color: var(--color-primary);
  box-shadow: 0 0 0 4px rgba(255,140,66,0.12) !important;
}

/* 特价横幅 */
.sale-banner {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 20px;
  background: linear-gradient(135deg, var(--color-cream), var(--color-pink-light));
  border-radius: var(--radius-md);
  margin-bottom: 20px;
  font-size: 14px;
  font-weight: 500;
  color: var(--color-text);
}
.sale-emoji { font-size: 22px; }

/* 服务卡片 */
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
.category-badge {
  position: absolute;
  top: 10px;
  left: 10px;
  background: rgba(255,255,255,0.9);
  color: var(--color-text);
  font-size: 11px;
  font-weight: 600;
  padding: 2px 10px;
  border-radius: var(--radius-full);
}
.card-body { padding: 14px; }
.card-body h4 { font-size: 15px; color: var(--color-text); margin-bottom: 6px; }
.price-row { display: flex; align-items: baseline; gap: 2px; margin: 6px 0 4px; }
.price { color: var(--color-primary); font-size: 18px; font-weight: 700; }
.price-row small { font-size: 12px; color: var(--color-text-placeholder); }
.desc { color: var(--color-text-placeholder); font-size: 12px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; margin: 0; }
.card-tags { display: flex; gap: 6px; margin-top: 8px; flex-wrap: wrap; }

@media (max-width: 768px) {
  .search-input { width: 100%; }
  .filters { width: 100%; }
}
</style>
