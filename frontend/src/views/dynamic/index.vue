<template>
  <div class="page-container">
    <div class="page-header">
      <h2>社团动态</h2>
      <p>发布和浏览校园社团动态</p>
    </div>

    <div class="card-container">
      <el-card class="publish-card">
        <el-form :model="publishForm" label-width="80px">
          <el-form-item label="所属社团">
            <el-select v-model="publishForm.clubId" placeholder="选择发布到哪个社团（可选）" clearable style="width: 260px">
              <el-option
                v-for="club in clubOptions"
                :key="club.id"
                :value="club.id"
                :label="club.clubName"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="动态内容">
            <el-input
              v-model="publishForm.content"
              type="textarea"
              :rows="3"
              maxlength="500"
              show-word-limit
              placeholder="说点什么吧..."
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="submitting" @click="submit">发布动态</el-button>
          </el-form-item>
        </el-form>
      </el-card>

      <div class="dynamic-list" v-loading="loading">
        <el-card v-for="item in list" :key="item.id" class="dynamic-item" shadow="never">
          <div class="dynamic-meta">
            <span class="club-tag">{{ clubMap[item.clubId] || '校园动态' }}</span>
            <span class="time">{{ formatTime(item.createTime) }}</span>
          </div>
          <div class="dynamic-content">{{ item.content }}</div>
          <div class="dynamic-footer">
            <span class="action"><el-icon><Star /></el-icon> {{ item.likeCount || 0 }}</span>
            <span class="action"><el-icon><ChatDotRound /></el-icon> {{ item.commentCount || 0 }}</span>
            <el-button v-if="isMine(item)" size="small" type="danger" link @click="handleDelete(item)">删除</el-button>
          </div>
        </el-card>
        <el-empty v-if="!loading && list.length === 0" description="还没有动态，快来发布第一条吧" />
      </div>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="query.current"
          v-model:page-size="query.size"
          :page-sizes="[10, 20, 50]"
          :total="total"
          layout="total, prev, pager, next"
          background
          @current-change="loadData"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Star, ChatDotRound } from '@element-plus/icons-vue'
import { getDynamicPage, addDynamic, deleteDynamic } from '@/api/dynamic'
import { getClubPage } from '@/api/club'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const list = ref([])
const total = ref(0)
const query = reactive({ current: 1, size: 10 })
const clubOptions = ref([])
const clubMap = ref({})
const publishForm = reactive({ clubId: null, content: '' })
const loading = ref(false)
const submitting = ref(false)

const loadClubs = async () => {
  try {
    const res = await getClubPage({ current: 1, size: 100 })
    const records = res.data?.records || []
    clubOptions.value = records
    const map = {}
    records.forEach(c => { map[c.id] = c.clubName })
    clubMap.value = map
  } catch (e) { /* 忽略 */ }
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getDynamicPage(query)
    list.value = res.data?.records || []
    total.value = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

const submit = async () => {
  if (!publishForm.content?.trim()) {
    ElMessage.warning('请输入动态内容')
    return
  }
  submitting.value = true
  try {
    await addDynamic({
      clubId: publishForm.clubId,
      content: publishForm.content.trim(),
      images: ''
    })
    ElMessage.success('发布成功')
    publishForm.content = ''
    publishForm.clubId = null
    query.current = 1
    loadData()
  } finally {
    submitting.value = false
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定删除该动态吗？', '提示', { type: 'warning' })
    await deleteDynamic(row.id)
    ElMessage.success('已删除')
    loadData()
  } catch (e) { /* 用户取消 */ }
}

const isMine = (row) => userStore.userInfo?.id === row.userId
const formatTime = (time) => time?.replace('T', ' ').substring(0, 16) || ''

onMounted(async () => {
  await loadClubs()
  loadData()
})
</script>

<style lang="scss" scoped>
.publish-card { margin-bottom: 18px; }
.dynamic-list { display: flex; flex-direction: column; gap: 12px; }
.dynamic-item { border-radius: 8px; }
.dynamic-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  .club-tag {
    color: #409eff;
    font-size: 13px;
    font-weight: 600;
  }
  .time { color: #909399; font-size: 12px; }
}
.dynamic-content {
  font-size: 14px;
  line-height: 1.7;
  color: #303133;
  white-space: pre-wrap;
  word-break: break-word;
}
.dynamic-footer {
  display: flex;
  gap: 20px;
  margin-top: 10px;
  color: #909399;
  font-size: 13px;
  .action { display: flex; align-items: center; gap: 4px; }
  margin-left: auto;
}
.pagination-wrapper {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
