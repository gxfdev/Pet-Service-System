<template>
  <div class="page-container points-page">
    <!-- 签到区 -->
    <div class="checkin-card card">
      <div class="checkin-left">
        <span class="checkin-icon">🎁</span>
        <div>
          <h3>积分商城</h3>
          <p>当前积分：<strong class="points-val">{{ myPoints }}</strong></p>
        </div>
      </div>
      <el-button type="primary" round @click="handleCheckin" :disabled="checkedIn">
        {{ checkedIn ? '✅ 已签到' : '📋 每日签到 +' + checkinPoints }}
      </el-button>
    </div>

    <!-- 签到日历 -->
    <div class="card" style="margin-bottom:20px">
      <h4 style="margin-bottom:12px;color:var(--text-primary)">📅 本月签到</h4>
      <div class="calendar-grid">
        <div class="cal-day" v-for="d in 30" :key="d" :class="{checked: d <= checkedDays}">
          {{ d }}
        </div>
      </div>
    </div>

    <!-- 兑换商品 -->
    <div class="card">
      <div class="page-header">
        <h2 class="page-title">🛍️ 兑换好物</h2>
      </div>
      <el-row :gutter="16">
        <el-col :xs="12" :sm="8" :md="6" v-for="item in products" :key="item.id">
          <div class="product-card" @click="handleExchange(item)">
            <div class="product-img" :style="{background: item.bg}">{{ item.emoji }}</div>
            <div class="product-info">
              <h4>{{ item.name }}</h4>
              <span class="product-points">{{ item.points }} 积分</span>
              <el-button type="primary" size="small" round :disabled="myPoints < item.points" style="width:100%;margin-top:8px">
                {{ myPoints >= item.points ? '立即兑换' : '积分不足' }}
              </el-button>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'

const myPoints = ref(580)
const checkedIn = ref(false)
const checkinPoints = ref(10)
const checkedDays = ref(12)

const products = ref([
  { id:1, name:'宠物逗猫棒', points:200, emoji:'🐱', bg:'linear-gradient(135deg,#FFD1DC,#FFE8ED)' },
  { id:2, name:'狗粮试吃装', points:350, emoji:'🦴', bg:'linear-gradient(135deg,#FFF3E0,#FFE8C8)' },
  { id:3, name:'20元优惠券', points:100, emoji:'🎫', bg:'linear-gradient(135deg,#C2EBE1,#E8F8F5)' },
  { id:4, name:'宠物项圈', points:500, emoji:'🎀', bg:'linear-gradient(135deg,#F9A8D4,#FFD1DC)' },
  { id:5, name:'猫抓板', points:280, emoji:'🧶', bg:'linear-gradient(135deg,#A3C4DB,#D6EAF8)' },
  { id:6, name:'公益捐赠10元', points:150, emoji:'❤️', bg:'linear-gradient(135deg,#FF8C42,#FFAB76)' },
])

function handleCheckin() {
  checkedIn.value = true
  myPoints.value += checkinPoints.value
  checkedDays.value = Math.min(checkedDays.value + 1, 30)
  ElMessage.success(`签到成功！+${checkinPoints.value}积分`)
}

function handleExchange(item) {
  if (myPoints.value < item.points) return
  ElMessage.success(`兑换成功：${item.name}`)
  myPoints.value -= item.points
}
</script>

<style scoped>
.points-page {}

.checkin-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: linear-gradient(135deg, rgba(255,140,66,0.1), rgba(255,209,220,0.15));
}
.checkin-left { display: flex; align-items: center; gap: 14px; }
.checkin-icon { font-size: 40px; }
.checkin-left h3 { font-size: 20px; font-weight: 700; color: var(--text-primary, #3D2C2C); margin-bottom: 4px; }
.checkin-left p { font-size: 14px; color: var(--text-secondary, #8B7575); margin: 0; }
.points-val { color: var(--primary, #FF8C42); font-size: 22px; font-family: var(--font-title); }

.calendar-grid { display: grid; grid-template-columns: repeat(10, 1fr); gap: 6px; }
.cal-day {
  aspect-ratio: 1;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 600;
  color: var(--text-muted, #B8A5A5);
  background: rgba(255,255,255,0.4);
  border: 1px solid var(--border);
}
.cal-day.checked { color: #fff; background: var(--primary, #FF8C42); border-color: transparent; }

.product-card {
  background: var(--bg-card, rgba(255,255,255,0.78));
  backdrop-filter: blur(16px);
  border-radius: var(--radius-lg, 24px);
  overflow: hidden;
  border: 1px solid var(--border, rgba(255,140,66,0.12));
  margin-bottom: 16px;
  cursor: pointer;
  transition: all 0.3s;
}
.product-card:hover { transform: translateY(-4px); box-shadow: var(--shadow-md); }
.product-img { height: 120px; display: flex; align-items: center; justify-content: center; font-size: 44px; }
.product-info { padding: 12px 14px 16px; }
.product-info h4 { font-size: 14px; color: var(--text-primary); font-weight: 600; margin-bottom: 4px; }
.product-points { font-size: 16px; font-weight: 700; color: var(--primary, #FF8C42); font-family: var(--font-title); }

@media (max-width: 768px) { .calendar-grid { grid-template-columns: repeat(7, 1fr); } }
</style>
