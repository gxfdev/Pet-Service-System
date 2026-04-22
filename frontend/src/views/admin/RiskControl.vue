<template>
  <div class="risk-page">
    <div class="page-card">
      <div class="card-header-row">
        <h3>🛡️ 风控管理</h3>
      </div>

      <!-- 风控统计 -->
      <div class="risk-stats">
        <div class="risk-stat-card"><span class="rs-icon">🚫</span><div><strong>{{ stats.blocked }}</strong><span>今日拦截</span></div></div>
        <div class="risk-stat-card"><span class="rs-icon">⚠️</span><div><strong>{{ stats.warnings }}</strong><span>风险预警</span></div></div>
        <div class="risk-stat-card"><span class="rs-icon">🔍</span><div><strong>{{ stats.blacklist }}</strong><span>黑名单数</span></div></div>
      </div>

      <!-- 黑名单 -->
      <div class="section-title">黑名单管理</div>
      <el-table :data="blacklist" size="small">
        <el-table-column prop="type" label="类型" width="80">
          <template #default="{row}"><el-tag :type="row.type==='user'?'danger':'warning'" size="small" round>{{ row.type==='user'?'用户':'IP' }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="target" label="目标" min-width="150" />
        <el-table-column prop="reason" label="原因" min-width="200" />
        <el-table-column prop="createdAt" label="加入时间" width="160" />
        <el-table-column label="操作" width="100">
          <template #default="{row}"><el-button size="small" type="danger" round @click="removeBlacklist(row)">移除</el-button></template>
        </el-table-column>
      </el-table>

      <!-- 拦截记录 -->
      <div class="section-title" style="margin-top:24px">拦截记录</div>
      <div class="intercept-list">
        <div class="intercept-item" v-for="item in intercepts" :key="item.id">
          <span class="int-dot"></span>
          <div class="int-content">
            <strong>{{ item.action }}</strong>
            <p>{{ item.detail }}</p>
          </div>
          <span class="int-time">{{ item.time }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'

const stats = ref({ blocked: 23, warnings: 7, blacklist: 15 })
const blacklist = ref([
  { type:'user', target:'spam_user_001', reason:'频繁刷单行为', createdAt:'2026-04-08 14:22' },
  { type:'ip', target:'192.168.45.12', reason:'异常注册攻击', createdAt:'2026-04-07 09:15' },
  { type:'user', target:'fake_review', reason:'恶意差评', createdAt:'2026-04-05 18:30' },
])
const intercepts = ref([
  { id:1, action:'IP拦截', detail:'192.168.x.x 尝试批量注册，已自动拦截', time:'5分钟前' },
  { id:2, action:'异常订单', detail:'订单 #20260410 疑似刷单，已冻结', time:'30分钟前' },
  { id:3, action:'敏感词', detail:'社区帖子包含敏感内容，已自动过滤', time:'1小时前' },
])

function removeBlacklist(row) { ElMessage.success('已移出黑名单') }
</script>

<style scoped>
.page-card { background: #1A2B3C; border-radius: 20px; padding: 24px; border: 1px solid rgba(255,140,66,0.06); }
.card-header-row { display: flex; align-items: center; justify-content: space-between; margin-bottom: 20px; }
.card-header-row h3 { color: #E0E6ED; font-size: 17px; margin: 0; }

.risk-stats { display: grid; grid-template-columns: repeat(3,1fr); gap: 12px; margin-bottom: 24px; }
.risk-stat-card { background: #162231; border-radius: 14px; padding: 16px; display: flex; align-items: center; gap: 12px; border: 1px solid rgba(245,108,108,0.08); }
.rs-icon { font-size: 28px; }
.risk-stat-card strong { display: block; font-family: 'Poppins'; font-size: 24px; font-weight: 800; color: #E0E6ED; }
.risk-stat-card span { display: block; font-size: 12px; color: #6B7A8D; }

.section-title { font-size: 14px; font-weight: 700; color: #E0E6ED; margin-bottom: 12px; padding-left: 4px; }

.intercept-list {}
.intercept-item { display: flex; align-items: flex-start; gap: 12px; padding: 10px 0; border-bottom: 1px solid rgba(255,140,66,0.04); }
.int-dot { width: 8px; height: 8px; border-radius: 50%; background: #F56C6C; margin-top: 6px; flex-shrink: 0; }
.int-content { flex: 1; }
.int-content strong { display: block; color: #E0E6ED; font-size: 13px; }
.int-content p { font-size: 12px; color: #6B7A8D; margin: 2px 0 0; }
.int-time { font-size: 11px; color: #4A5568; flex-shrink: 0; }
</style>
