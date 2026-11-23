<template>
  <div class="page-container">
    <div class="page-header">
      <el-button link @click="router.back()">
        <el-icon><ArrowLeft /></el-icon> 返回列表
      </el-button>
    </div>

    <div v-loading="loading">
      <!-- 社团基本信息 -->
      <el-card shadow="hover" class="info-card">
        <div class="info-header">
          <div class="info-logo">
            <el-icon :size="60"><OfficeBuilding /></el-icon>
          </div>
          <div class="info-detail">
            <h2>{{ club?.clubName }}</h2>
            <div class="info-tags">
              <el-tag size="small">{{ club?.category }}</el-tag>
              <el-tag size="small" :type="getClubStatusType(club?.status)">
                {{ getClubStatusText(club?.status) }}
              </el-tag>
              <span class="info-item"><el-icon><User /></el-icon> 成员 {{ club?.memberCount || 0 }} 人</span>
              <span class="info-item"><el-icon><UserFilled /></el-icon> 社长：{{ presidentName }}</span>
              <span class="info-item"><el-icon><User /></el-icon> 指导老师：{{ club?.advisor || '-' }}</span>
            </div>
            <p class="info-desc">{{ club?.description }}</p>
          </div>
        </div>
      </el-card>

      <!-- Tab标签页 -->
      <el-card shadow="hover" class="content-card">
        <el-tabs v-model="activeTab">
          <!-- 部门 -->
          <el-tab-pane label="部门设置" name="departments">
            <div class="tab-header">
              <span>社团部门架构</span>
              <el-button type="primary" size="small" @click="handleAddDept">
                <el-icon><Plus /></el-icon> 新增部门
              </el-button>
            </div>
            <el-table :data="departments" stripe style="width: 100%">
              <el-table-column prop="deptName" label="部门名称" width="180" />
              <el-table-column prop="description" label="描述" />
              <el-table-column prop="sortOrder" label="排序" width="100" />
              <el-table-column label="操作" width="180">
                <template #default="{ row }">
                  <el-button size="small" type="primary" link @click="handleEditDept(row)">编辑</el-button>
                  <el-button size="small" type="danger" link @click="handleDeleteDept(row)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
            <el-empty v-if="departments.length === 0" description="暂无部门" />
          </el-tab-pane>

          <!-- 活动 -->
          <el-tab-pane label="社团活动" name="activities">
            <div class="tab-header">
              <span>社团近期活动</span>
              <el-button type="primary" size="small" @click="handleAddActivity">
                <el-icon><Plus /></el-icon> 发布活动
              </el-button>
            </div>
            <el-table :data="activities" stripe style="width: 100%">
              <el-table-column prop="title" label="活动标题" />
              <el-table-column prop="location" label="地点" width="140" />
              <el-table-column prop="startTime" label="开始时间" width="180">
                <template #default="{ row }">{{ formatTime(row.startTime) }}</template>
              </el-table-column>
              <el-table-column prop="status" label="状态" width="100">
                <template #default="{ row }">
                  <el-tag :type="getActivityStatusType(row.status)" size="small">
                    {{ getActivityStatusText(row.status) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="180">
                <template #default="{ row }">
                  <el-button size="small" type="primary" link @click="handleEditActivity(row)">编辑</el-button>
                  <el-button size="small" type="danger" link @click="handleDeleteActivity(row)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>
        </el-tabs>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getClubById } from '@/api/club'
import { getDepartmentsByClubId, addDepartment, updateDepartment, deleteDepartment } from '@/api/department'
import { getActivityPage, addActivity, updateActivity, deleteActivity } from '@/api/activity'

const route = useRoute()
const router = useRouter()
const clubId = route.params.id

const loading = ref(false)
const club = ref(null)
const departments = ref([])
const activities = ref([])
const activeTab = ref('departments')

const presidentName = ref('暂未设置')

const formatTime = (time) => time?.replace('T', ' ').substring(0, 16) || '-'

const getClubStatusType = (status) => ({ 0: 'info', 1: 'success', 2: 'warning' })[status] || 'info'
const getClubStatusText = (status) => ({ 0: '注销', 1: '正常', 2: '待审核' })[status] || '-'
const getActivityStatusType = (status) => ({ 0: 'info', 1: 'success', 2: 'warning', 3: 'danger' })[status] || 'info'
const getActivityStatusText = (status) => ({ 0: '已结束', 1: '进行中', 2: '未开始', 3: '已取消' })[status] || '-'

const loadData = async () => {
  loading.value = true
  try {
    const res = await getClubById(clubId)
    club.value = res.data
    if (club.value?.presidentId) {
      presidentName.value = `用户#${club.value.presidentId}`
    }
    // 加载部门和活动
    const [deptRes, actRes] = await Promise.all([
      getDepartmentsByClubId(clubId),
      getActivityPage({ clubId, current: 1, size: 50 })
    ])
    departments.value = deptRes.data || []
    activities.value = actRes.data?.records || []
  } finally {
    loading.value = false
  }
}

// 部门操作
const handleAddDept = async () => {
  try {
    const { value } = await ElMessageBox.prompt('请输入部门名称', '新增部门', {
      inputPlaceholder: '如：技术部'
    })
    await addDepartment({ clubId: Number(clubId), deptName: value, sortOrder: 0 })
    ElMessage.success('新增成功')
    loadData()
  } catch (e) { /* 取消或已存在 */ }
}

const handleEditDept = async (row) => {
  try {
    const { value } = await ElMessageBox.prompt('请输入新的部门名称', '编辑部门', {
      inputValue: row.deptName
    })
    await updateDepartment({ ...row, deptName: value })
    ElMessage.success('更新成功')
    loadData()
  } catch (e) { /* 取消 */ }
}

const handleDeleteDept = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除部门「${row.deptName}」吗？`, '提示', { type: 'warning' })
    await deleteDepartment(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) { /* 取消 */ }
}

// 活动操作
const handleAddActivity = () => {
  router.push('/activities')
}
const handleEditActivity = (row) => {
  router.push('/activities')
}
const handleDeleteActivity = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除活动「${row.title}」吗？`, '提示', { type: 'warning' })
    await deleteActivity(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) { /* 取消 */ }
}

onMounted(() => loadData())
</script>

<style lang="scss" scoped>
.info-card {
  margin-bottom: 20px;
  border: none;

  .info-header {
    display: flex;
    gap: 24px;
  }

  .info-logo {
    width: 100px;
    height: 100px;
    border-radius: 16px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;
    flex-shrink: 0;
  }

  .info-detail {
    flex: 1;

    h2 {
      margin: 0 0 12px;
      font-size: 22px;
    }

    .info-tags {
      display: flex;
      align-items: center;
      flex-wrap: wrap;
      gap: 16px;
      margin-bottom: 12px;

      .info-item {
        display: flex;
        align-items: center;
        gap: 4px;
        color: #606266;
        font-size: 14px;
      }
    }

    .info-desc {
      color: #606266;
      line-height: 1.6;
    }
  }
}

.content-card {
  border: none;

  .tab-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
    font-size: 15px;
    font-weight: 600;
  }
}
</style>
