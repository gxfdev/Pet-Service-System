<template>
  <div class="marketing-page">
    <div class="page-card">
      <div class="card-header-row">
        <h3>🎁 营销管理</h3>
        <el-button type="primary" size="small" round @click="showCreateDialog">+ 创建活动</el-button>
      </div>

      <!-- 活动列表 -->
      <div class="campaign-list">
        <div class="campaign-card" v-for="item in campaigns" :key="item.id">
          <div class="campaign-icon" :style="{background: item.bg}">{{ item.icon }}</div>
          <div class="campaign-info">
            <strong>{{ item.title }}</strong>
            <p>{{ item.desc }}</p>
            <span class="campaign-time">{{ item.timeRange }}</span>
          </div>
          <div class="campaign-stats">
            <div class="cstat"><span class="cstat-val">{{ item.used }}</span><span class="cstat-label">已核销</span></div>
            <div class="cstat"><span class="cstat-val">{{ item.total }}</span><span class="cstat-label">发放量</span></div>
          </div>
          <el-tag :type="item.status==='active'?'success':'info'" size="small" effect="dark" round>{{ item.status==='active'?'进行中':'已结束' }}</el-tag>
        </div>
      </div>
    </div>

    <el-dialog v-model="visible" title="创建营销活动" width="520px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="活动名称"><el-input v-model="form.title" placeholder="如：新人专享礼包" /></el-form-item>
        <el-form-item label="活动类型">
          <el-select v-model="form.type" style="width:100%">
            <el-option label="优惠券" value="coupon" /><el-option label="满减" value="discount" />
            <el-option label="秒杀" value="flash" /><el-option label="新人礼包" value="newbie" />
          </el-select>
        </el-form-item>
        <el-form-item label="发放数量"><el-input-number v-model="form.total" :min="1" /></el-form-item>
        <el-form-item label="有效期"><el-date-picker v-model="form.timeRange" type="datetimerange" style="width:100%" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="visible=false" round>取消</el-button>
        <el-button type="primary" @click="visible=false" round>创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const visible = ref(false)
const form = ref({ title:'', type:'coupon', total:100, timeRange:[] })

function showCreateDialog() { visible.value = true }

const campaigns = ref([
  { id:1, icon:'🎉', title:'新人专享礼包', desc:'注册即送3张优惠券', timeRange:'2026-01-01 ~ 2026-12-31', used:342, total:1000, status:'active', bg:'linear-gradient(135deg,#FF8C42,#FFAB76)' },
  { id:2, icon:'🔥', title:'春季美容节', desc:'美容服务满100减20', timeRange:'2026-03-01 ~ 2026-04-30', used:89, total:500, status:'active', bg:'linear-gradient(135deg,#F9A8D4,#FFD1DC)' },
  { id:3, icon:'⚡', title:'限时秒杀日', desc:'寄养5折限时抢', timeRange:'2026-02-14 ~ 2026-02-14', used:200, total:200, status:'ended', bg:'linear-gradient(135deg,#6C9EBF,#A3C4DB)' },
])
</script>

<style scoped>
.page-card { background: #1A2B3C; border-radius: 20px; padding: 24px; border: 1px solid rgba(255,140,66,0.06); }
.card-header-row { display: flex; align-items: center; justify-content: space-between; margin-bottom: 20px; }
.card-header-row h3 { color: #E0E6ED; font-size: 17px; margin: 0; }
.campaign-list {}
.campaign-card { display: flex; align-items: center; gap: 16px; padding: 16px; border-radius: 14px; background: #162231; margin-bottom: 10px; border: 1px solid rgba(255,140,66,0.04); }
.campaign-icon { width: 48px; height: 48px; border-radius: 14px; display: flex; align-items: center; justify-content: center; font-size: 22px; flex-shrink: 0; }
.campaign-info { flex: 1; }
.campaign-info strong { display: block; color: #E0E6ED; font-size: 14px; margin-bottom: 2px; }
.campaign-info p { font-size: 12px; color: #6B7A8D; margin: 0; }
.campaign-time { font-size: 11px; color: #4A5568; }
.campaign-stats { display: flex; gap: 16px; margin-right: 12px; }
.cstat { text-align: center; }
.cstat-val { display: block; font-family: 'Poppins'; font-size: 18px; font-weight: 700; color: #E0E6ED; }
.cstat-label { display: block; font-size: 10px; color: #6B7A8D; }
</style>
