<template>
  <div class="dashboard">
    <div class="page-header">
      <h2>欢迎回来，{{ userStore.nickname }}！</h2>
      <p>这是大学生社团管理系统的首页</p>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6" v-for="stat in statsList" :key="stat.title">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" :style="{ background: stat.color }">
              <el-icon :size="30"><component :is="stat.icon" /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stat.value }}</div>
              <div class="stat-title">{{ stat.title }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20">
      <!-- 最新公告 -->
      <el-col :span="14">
        <el-card shadow="hover" class="content-card">
          <template #header>
            <div class="card-header">
              <span class="card-title">
                <el-icon><Bell /></el-icon>
                最新公告
              </span>
              <router-link to="/announcements">查看更多</router-link>
            </div>
          </template>
          <el-table :data="announcements" stripe style="width: 100%">
            <el-table-column prop="title" label="标题">
              <template #default="{ row }">
                <span>
                  <el-tag v-if="row.isTop === 1" type="danger" size="small" style="margin-right: 6px;">置顶</el-tag>
                  {{ row.title }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="发布时间" width="180">
              <template #default="{ row }">
                {{ formatTime(row.createTime) }}
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <!-- 最新活动 -->
      <el-col :span="10">
        <el-card shadow="hover" class="content-card">
          <template #header>
            <div class="card-header">
              <span class="card-title">
                <el-icon><Calendar /></el-icon>
                近期活动
              </span>
              <router-link to="/activities">查看更多</router-link>
            </div>
          </template>
          <div class="activity-list">
            <div v-for="item in activities" :key="item.id" class="activity-item">
              <div class="activity-info">
                <div class="activity-title">{{ item.title }}</div>
                <div class="activity-meta">
                  <span><el-icon><Location /></el-icon>{{ item.location || '待定' }}</span>
                  <span><el-icon><Clock /></el-icon>{{ formatTime(item.startTime) }}</span>
                </div>
              </div>
              <el-tag :type="getActivityStatusType(item.status)" size="small">
                {{ getActivityStatusText(item.status) }}
              </el-tag>
            </div>
            <el-empty v-if="activities.length === 0" description="暂无活动" />
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useUserStore } from '@/store/user'
import { getTopAnnouncements } from '@/api/announcement'
import { getActivityPage } from '@/api/activity'
import { getClubPage } from '@/api/club'
import { getUserPage } from '@/api/user'

const userStore = useUserStore()

const statsList = ref([
  { title: '社团总数', value: 0, icon: 'OfficeBuilding', color: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)' },
  { title: '用户总数', value: 0, icon: 'User', color: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)' },
  { title: '活动总数', value: 0, icon: 'Calendar', color: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)' },
  { title: '公告总数', value: 0, icon: 'Bell', color: 'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)' }
])

const announcements = ref([])
const activities = ref([])

const formatTime = (time) => {
  if (!time) return '-'
  return time.replace('T', ' ').substring(0, 16)
}

const getActivityStatusType = (status) => {
  const map = { 0: 'info', 1: 'success', 2: 'warning', 3: 'danger' }
  return map[status] || 'info'
}

const getActivityStatusText = (status) => {
  const map = { 0: '已结束', 1: '进行中', 2: '未开始', 3: '已取消' }
  return map[status] || '未知'
}

const loadData = async () => {
  try {
    // 统计数据
    const [clubRes, userRes, activityRes] = await Promise.all([
      getClubPage({ current: 1, size: 1 }),
      getUserPage({ current: 1, size: 1 }),
      getActivityPage({ current: 1, size: 1 })
    ])
    statsList.value[0].value = clubRes.data?.total || 0
    statsList.value[1].value = userRes.data?.total || 0
    statsList.value[2].value = activityRes.data?.total || 0

    // 公告
    const annRes = await getTopAnnouncements()
    announcements.value = annRes.data || []

    // 活动
    const actRes = await getActivityPage({ current: 1, size: 5 })
    activities.value = actRes.data?.records || []
  } catch (e) {
    console.error('加载首页数据失败', e)
  }
}

onMounted(() => {
  loadData()
})
</script>

<style lang="scss" scoped>
.dashboard {
  .stats-row {
    margin-bottom: 20px;
  }

  .stat-card {
    border: none;

    .stat-content {
      display: flex;
      align-items: center;
      gap: 16px;
    }

    .stat-icon {
      width: 60px;
      height: 60px;
      border-radius: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      color: #fff;
    }

    .stat-info {
      .stat-value {
        font-size: 28px;
        font-weight: 700;
        color: #303133;
      }

      .stat-title {
        font-size: 14px;
        color: #909399;
        margin-top: 4px;
      }
    }
  }

  .content-card {
    border: none;
    margin-bottom: 20px;

    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;

      .card-title {
        font-size: 16px;
        font-weight: 600;
        display: flex;
        align-items: center;
        gap: 6px;
      }

      a {
        font-size: 13px;
        color: #409eff;
        &:hover { text-decoration: underline; }
      }
    }
  }

  .activity-list {
    .activity-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 12px 0;
      border-bottom: 1px solid #f0f2f5;

      &:last-child { border-bottom: none; }

      .activity-info {
        flex: 1;
        margin-right: 12px;

        .activity-title {
          font-size: 14px;
          color: #303133;
          font-weight: 500;
          margin-bottom: 6px;
        }

        .activity-meta {
          display: flex;
          gap: 12px;
          font-size: 12px;
          color: #909399;

          span {
            display: flex;
            align-items: center;
            gap: 3px;
          }
        }
      }
    }
  }
}
</style>
