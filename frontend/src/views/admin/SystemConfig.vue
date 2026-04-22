<template>
  <div class="config-page">
    <div class="page-card">
      <div class="card-header-row"><h3>⚙️ 系统配置</h3></div>

      <div class="config-sections">
        <!-- 轮播图 -->
        <div class="config-section">
          <h4>🎠 首页轮播图</h4>
          <div class="banner-list">
            <div class="banner-item" v-for="(b, i) in banners" :key="i">
              <div class="banner-preview" :style="{background: b.color}">{{ b.title }}</div>
              <el-button size="small" type="danger" round>删除</el-button>
            </div>
            <el-button size="small" round @click="banners.push({title:'新轮播',color:'#FF8C42'})">+ 添加</el-button>
          </div>
        </div>

        <!-- 公告 -->
        <div class="config-section">
          <h4>📢 系统公告</h4>
          <el-input v-model="announcement" type="textarea" :rows="3" placeholder="输入公告内容..." />
          <el-button type="primary" size="small" round style="margin-top:10px" @click="saveAnnouncement">发布公告</el-button>
        </div>

        <!-- 基础配置 -->
        <div class="config-section">
          <h4>🔧 基础配置</h4>
          <el-form label-width="140px" size="small">
            <el-form-item label="平台名称"><el-input v-model="config.platformName" /></el-form-item>
            <el-form-item label="客服电话"><el-input v-model="config.servicePhone" /></el-form-item>
            <el-form-item label="新用户赠送积分"><el-input-number v-model="config.newUserPoints" :min="0" /></el-form-item>
            <el-form-item label="服务商抽成比例(%)"><el-input-number v-model="config.commissionRate" :min="0" :max="100" :precision="1" /></el-form-item>
            <el-form-item label="开启注册"><el-switch v-model="config.registerEnabled" /></el-form-item>
            <el-form-item><el-button type="primary" round @click="saveConfig">保存配置</el-button></el-form-item>
          </el-form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'

const banners = ref([
  { title: '春季美容节', color: '#FF8C42' },
  { title: '新人礼包', color: '#F9A8D4' },
])

const announcement = ref('')
const config = reactive({
  platformName: '宠物服务平台',
  servicePhone: '400-888-9999',
  newUserPoints: 100,
  commissionRate: 10,
  registerEnabled: true,
})

function saveAnnouncement() { ElMessage.success('公告已发布') }
function saveConfig() { ElMessage.success('配置已保存') }
</script>

<style scoped>
.page-card { background: #1A2B3C; border-radius: 20px; padding: 24px; border: 1px solid rgba(255,140,66,0.06); }
.card-header-row { display: flex; align-items: center; justify-content: space-between; margin-bottom: 20px; }
.card-header-row h3 { color: #E0E6ED; font-size: 17px; margin: 0; }

.config-sections { display: flex; flex-direction: column; gap: 24px; }
.config-section { background: #162231; border-radius: 16px; padding: 20px; border: 1px solid rgba(255,140,66,0.04); }
.config-section h4 { color: #E0E6ED; font-size: 15px; margin: 0 0 14px; }

.banner-list { display: flex; flex-wrap: wrap; gap: 10px; }
.banner-item { display: flex; align-items: center; gap: 10px; }
.banner-preview { width: 120px; height: 50px; border-radius: 10px; display: flex; align-items: center; justify-content: center; color: #fff; font-size: 13px; font-weight: 600; }
</style>
