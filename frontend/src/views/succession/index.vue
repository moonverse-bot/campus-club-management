<template>
  <div class="page-container">
    <div class="page-header">
      <h2>部长换届</h2>
      <p>{{ isSuperAdmin ? '审批社团部长卸任推荐，确认后自动完成职位交接' : '推荐本社团普通成员接任部长，由超级管理员审批' }}</p>
    </div>

    <template v-if="isMinister">
      <div class="card-container">
        <div class="section-title">发起卸任推荐</div>
        <el-form :model="form" label-width="90px" class="recommend-form">
          <el-form-item label="所属社团" required>
            <el-select v-model="form.clubId" placeholder="请选择社团" @change="handleClubChange">
              <el-option
                v-for="club in ministerClubs"
                :key="club.clubId"
                :label="club.clubName"
                :value="club.clubId"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="推荐成员" required>
            <el-select
              v-model="form.candidateId"
              placeholder="仅可选择本社团普通成员"
              filterable
              :loading="candidateLoading"
            >
              <el-option
                v-for="candidate in candidates"
                :key="candidate.id"
                :label="`${displayName(candidate)}（${candidate.username}）`"
                :value="candidate.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="推荐理由">
            <el-input
              v-model="form.reason"
              type="textarea"
              :rows="3"
              maxlength="500"
              show-word-limit
              placeholder="请说明卸任原因及推荐理由"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="submitLoading" @click="handleSubmit">提交推荐</el-button>
          </el-form-item>
        </el-form>
      </div>

      <div class="card-container">
        <div class="table-toolbar">
          <span class="section-title">我的卸任申请</span>
          <el-button @click="loadMine">刷新</el-button>
        </div>
        <el-table :data="myRecords" stripe v-loading="mineLoading">
          <el-table-column prop="clubName" label="社团" min-width="130" />
          <el-table-column prop="departmentName" label="部门" min-width="110" />
          <el-table-column label="推荐成员" min-width="140">
            <template #default="{ row }">{{ row.candidateName }}（{{ row.candidateUsername }}）</template>
          </el-table-column>
          <el-table-column prop="reason" label="推荐理由" min-width="180" show-overflow-tooltip />
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="reviewRemark" label="审批意见" min-width="140" show-overflow-tooltip />
          <el-table-column label="申请时间" width="160">
            <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
          </el-table-column>
        </el-table>
      </div>
    </template>

    <div v-if="isSuperAdmin" class="card-container">
      <div class="table-toolbar">
        <el-select v-model="statusFilter" style="width: 140px" @change="loadAdminData">
          <el-option label="待审批" :value="0" />
          <el-option label="已通过" :value="1" />
          <el-option label="已拒绝" :value="2" />
          <el-option label="全部" :value="null" />
        </el-select>
        <el-button @click="loadAdminData">刷新</el-button>
      </div>
      <el-table :data="adminRecords" stripe v-loading="adminLoading">
        <el-table-column prop="clubName" label="社团" min-width="130" />
        <el-table-column prop="departmentName" label="部门" min-width="110" />
        <el-table-column label="原部长" min-width="140">
          <template #default="{ row }">{{ row.ministerName }}（{{ row.ministerUsername }}）</template>
        </el-table-column>
        <el-table-column label="推荐成员" min-width="140">
          <template #default="{ row }">{{ row.candidateName }}（{{ row.candidateUsername }}）</template>
        </el-table-column>
        <el-table-column prop="reason" label="推荐理由" min-width="180" show-overflow-tooltip />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="申请时间" width="160">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <template v-if="row.status === 0">
              <el-button type="primary" link @click="openReview(row, true)">同意</el-button>
              <el-button type="danger" link @click="openReview(row, false)">拒绝</el-button>
            </template>
            <el-tooltip v-else-if="row.reviewRemark" :content="row.reviewRemark" placement="top">
              <span class="reviewed-text">已审批</span>
            </el-tooltip>
            <span v-else class="reviewed-text">已审批</span>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="reviewDialogVisible" :title="reviewForm.approved ? '同意卸任推荐' : '拒绝卸任推荐'" width="500px">
      <el-alert
        v-if="reviewForm.approved"
        title="通过后，推荐成员将接任部长，原部长将降为普通成员。"
        type="warning"
        :closable="false"
        class="review-alert"
      />
      <el-input
        v-model="reviewForm.reviewRemark"
        type="textarea"
        :rows="4"
        maxlength="500"
        show-word-limit
        placeholder="请输入审批意见（选填）"
      />
      <template #footer>
        <el-button @click="reviewDialogVisible = false">取消</el-button>
        <el-button
          :type="reviewForm.approved ? 'primary' : 'danger'"
          :loading="reviewLoading"
          @click="handleReview"
        >确认{{ reviewForm.approved ? '同意' : '拒绝' }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'
import { getClubById } from '@/api/club'
import {
  createSuccession,
  getAdminSuccessions,
  getMySuccessions,
  getSuccessionCandidates,
  getUserMemberships,
  reviewSuccession
} from '@/api/succession'

const userStore = useUserStore()
const isMinister = computed(() => userStore.roles.includes('MINISTER'))
const isSuperAdmin = computed(() => userStore.roles.includes('SUPER_ADMIN'))

const ministerClubs = ref([])
const candidates = ref([])
const myRecords = ref([])
const adminRecords = ref([])
const statusFilter = ref(0)
const candidateLoading = ref(false)
const submitLoading = ref(false)
const mineLoading = ref(false)
const adminLoading = ref(false)
const reviewLoading = ref(false)
const reviewDialogVisible = ref(false)

const form = reactive({ clubId: null, candidateId: null, reason: '' })
const reviewForm = reactive({ id: null, approved: true, reviewRemark: '' })

const displayName = (user) => user.realName || user.nickname || user.username
const formatTime = (time) => time?.replace('T', ' ').substring(0, 16) || '-'
const statusText = (status) => ({ 0: '待审批', 1: '已通过', 2: '已拒绝' })[status] || '未知'
const statusType = (status) => ({ 0: 'warning', 1: 'success', 2: 'danger' })[status] || 'info'

const loadMinisterClubs = async () => {
  const res = await getUserMemberships(userStore.userInfo.id)
  const memberships = (res.data || []).filter(item => item.position === 'minister' && item.status === 1)
  ministerClubs.value = await Promise.all(memberships.map(async item => {
    const clubRes = await getClubById(item.clubId)
    return { clubId: item.clubId, clubName: clubRes.data.clubName }
  }))
  if (ministerClubs.value.length) {
    form.clubId = ministerClubs.value[0].clubId
    await loadCandidates()
  }
}

const loadCandidates = async () => {
  if (!form.clubId) {
    candidates.value = []
    return
  }
  candidateLoading.value = true
  try {
    const res = await getSuccessionCandidates(form.clubId)
    candidates.value = res.data || []
  } finally {
    candidateLoading.value = false
  }
}

const handleClubChange = async () => {
  form.candidateId = null
  await loadCandidates()
}

const loadMine = async () => {
  mineLoading.value = true
  try {
    const res = await getMySuccessions()
    myRecords.value = res.data || []
  } finally {
    mineLoading.value = false
  }
}

const handleSubmit = async () => {
  if (!form.clubId || !form.candidateId) {
    ElMessage.warning('请选择社团和推荐成员')
    return
  }
  submitLoading.value = true
  try {
    await createSuccession(form)
    ElMessage.success('卸任推荐已提交，等待超级管理员审批')
    form.candidateId = null
    form.reason = ''
    await Promise.all([loadMine(), loadCandidates()])
  } finally {
    submitLoading.value = false
  }
}

const loadAdminData = async () => {
  adminLoading.value = true
  try {
    const res = await getAdminSuccessions(statusFilter.value)
    adminRecords.value = res.data || []
  } finally {
    adminLoading.value = false
  }
}

const openReview = (row, approved) => {
  Object.assign(reviewForm, { id: row.id, approved, reviewRemark: '' })
  reviewDialogVisible.value = true
}

const handleReview = async () => {
  reviewLoading.value = true
  try {
    await reviewSuccession(reviewForm.id, {
      approved: reviewForm.approved,
      reviewRemark: reviewForm.reviewRemark
    })
    ElMessage.success(reviewForm.approved ? '已同意，部长职位已完成交接' : '已拒绝该卸任推荐')
    reviewDialogVisible.value = false
    await loadAdminData()
  } finally {
    reviewLoading.value = false
  }
}

onMounted(async () => {
  const tasks = []
  if (isMinister.value) {
    tasks.push(loadMinisterClubs(), loadMine())
  }
  if (isSuperAdmin.value) {
    tasks.push(loadAdminData())
  }
  await Promise.all(tasks)
})
</script>

<style lang="scss" scoped>
.section-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 18px;
}

.recommend-form {
  max-width: 680px;

  .el-select {
    width: 100%;
  }
}

.review-alert {
  margin-bottom: 16px;
}

.reviewed-text {
  color: #909399;
}
</style>
