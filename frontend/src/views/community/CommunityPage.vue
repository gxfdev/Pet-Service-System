<template>
  <div class="page-container community-page">
    <div class="card">
      <div class="page-header">
        <h2 class="page-title">💬 萌宠圈</h2>
        <el-button type="primary" round size="small" @click="showPublish = true">✏️ 发布动态</el-button>
      </div>

      <!-- 话题标签 -->
      <div class="topic-tags">
        <el-tag v-for="t in topics" :key="t" effect="plain" round class="topic-tag" :class="{active: activeTopic===t}" @click="activeTopic=t">{{ t }}</el-tag>
      </div>

      <!-- 瀑布流帖子 -->
      <div class="posts-grid">
        <div class="post-card" v-for="post in posts" :key="post.id" @click="openPost(post)">
          <div class="post-img" :style="{background: post.coverBg}">
            <span class="post-emoji">{{ post.emoji }}</span>
          </div>
          <div class="post-body">
            <h4>{{ post.title }}</h4>
            <div class="post-meta">
              <span>👤 {{ post.author }}</span>
              <span>❤️ {{ post.likes }}</span>
            </div>
          </div>
        </div>
      </div>
      <el-empty v-if="posts.length===0" description="暂无动态，快来发布第一条吧" :image-size="100" />
    </div>

    <!-- 发布弹窗 -->
    <el-dialog v-model="showPublish" title="发布动态" width="500px" class="publish-dialog">
      <el-form :model="publishForm" label-position="top">
        <el-form-item label="标题"><el-input v-model="publishForm.title" placeholder="给你的动态起个标题" /></el-form-item>
        <el-form-item label="内容"><el-input v-model="publishForm.content" type="textarea" :rows="4" placeholder="分享你和宠物的故事..." /></el-form-item>
        <el-form-item label="话题">
          <el-select v-model="publishForm.topic" style="width:100%" placeholder="选择话题">
            <el-option v-for="t in topics" :key="t" :label="t" :value="t" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showPublish=false" round>取消</el-button>
        <el-button type="primary" @click="handlePublish" round>发布</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'

const activeTopic = ref('全部')
const showPublish = ref(false)
const publishForm = ref({ title:'', content:'', topic:'全部' })

const topics = ['全部', '#今日萌宠', '#养宠干货', '#领养代替购买', '#宠物搞笑', '#美容大变身']
const posts = ref([
  { id:1, title:'我家橘猫又胖了2斤', author:'猫奴小王', likes:128, emoji:'🐈', coverBg:'linear-gradient(135deg,#FFD1DC,#FFE8ED)' },
  { id:2, title:'柯基断尾后第一次洗澡', author:'柯基控', likes:89, emoji:'🐕', coverBg:'linear-gradient(135deg,#FFF3E0,#FFE8C8)' },
  { id:3, title:'布偶猫的开箱视频', author:'猫咖老板', likes:256, emoji:'😺', coverBg:'linear-gradient(135deg,#C2EBE1,#E8F8F5)' },
  { id:4, title:'金毛的游泳初体验', author:'遛狗达人', likes:67, emoji:'🐕‍🦺', coverBg:'linear-gradient(135deg,#A3C4DB,#D6EAF8)' },
  { id:5, title:'仓鼠的迷你城堡DIY', author:'小宠世界', likes:45, emoji:'🐹', coverBg:'linear-gradient(135deg,#F9A8D4,#FFD1DC)' },
  { id:6, title:'鹦鹉学说脏话怎么办', author:'鸟友会', likes:33, emoji:'🦜', coverBg:'linear-gradient(135deg,#98DDCA,#C2EBE1)' },
])

function openPost(post) { ElMessage.info(`查看：${post.title}`) }
function handlePublish() { ElMessage.success('发布成功！'); showPublish.value = false }
</script>

<style scoped>
.community-page {}

.topic-tags { display: flex; flex-wrap: wrap; gap: 8px; margin-bottom: 20px; }
.topic-tag { cursor: pointer; transition: all 0.2s; }
.topic-tag.active { background: var(--primary, #FF8C42); color: #fff; border-color: var(--primary); }

.posts-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 16px;
}
.post-card {
  background: var(--bg-card, rgba(255,255,255,0.78));
  backdrop-filter: blur(16px);
  border-radius: var(--radius-lg, 24px);
  overflow: hidden;
  cursor: pointer;
  border: 1px solid var(--border, rgba(255,140,66,0.12));
  transition: all 0.3s cubic-bezier(0.4,0,0.2,1);
}
.post-card:hover { transform: translateY(-6px); box-shadow: var(--shadow-md, 0 4px 20px rgba(255,140,66,0.12)); }
.post-img { height: 140px; display: flex; align-items: center; justify-content: center; }
.post-emoji { font-size: 52px; filter: drop-shadow(0 4px 8px rgba(0,0,0,0.1)); }
.post-body { padding: 12px 14px 16px; }
.post-body h4 { font-size: 14px; color: var(--text-primary, #3D2C2C); margin-bottom: 6px; font-weight: 600; }
.post-meta { display: flex; justify-content: space-between; font-size: 12px; color: var(--text-muted, #B8A5A5); }

@media (max-width: 640px) { .posts-grid { grid-template-columns: repeat(2, 1fr); gap: 10px; } }
</style>
