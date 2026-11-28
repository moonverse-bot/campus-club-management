<template>
  <div class="page-container">
    <div class="page-header">
      <h2>活动管理</h2>
      <p>{{ isSuperAdmin ? '查看、删除活动并审批部长提交的新活动' : '管理本社团活动，浏览并报名其他社团活动' }}</p>
    </div>

    <div class="card-container">
      <el-tabs v-model="activeScope" @tab-change="handleScopeChange">
        <el-tab-pane :label="isSuperAdmin ? '活动列表' : '本社团活动管理'" name="managed" />
        <el-tab-pane v-if="isSuperAdmin" label="活动申请审批" name="approval" />
        <el-tab-pane v-else label="其他活动" name="other" />
      </el-tabs>

      <div class="table-toolbar">
        <el-form :inline="true" class="search-form">
          <el-form-item label="关键词">
            <el-input v-model="searchForm.keyword" placeholder="输入活动标题关键字" clearable style="width: 200px" @input="handleRealtimeSearch" />
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="searchForm.status" placeholder="全部" clearable style="width: 120px" @change="handleSearch">
              <el-option :value="2" label="未开始" />
              <el-option :value="1" label="进行中" />
              <el-option :value="0" label="已结束" />
              <el-option :value="3" label="已取消" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">搜索</el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
        <div class="toolbar-actions">
          <el-button v-if="activeScope === 'managed' && !isSuperAdmin && manageOptions.length" type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon> 新增活动
          </el-button>
          <el-button v-if="activeScope === 'managed' && !isSuperAdmin" @click="handleViewMyFunding">我的经费申请</el-button>
        </div>
      </div>

      <el-alert
        v-if="activeScope !== 'other'"
        :title="scopeTip"
        type="info"
        :closable="false"
        class="scope-tip"
      />

      <el-table :data="tableData" stripe style="width: 100%" v-loading="loading">
        <el-table-column prop="title" label="活动标题" min-width="160" show-overflow-tooltip />
        <el-table-column prop="clubName" label="负责社团" min-width="150" show-overflow-tooltip />
        <el-table-column v-if="activeScope === 'approval'" prop="organizerName" label="申请部长" min-width="110" />
        <el-table-column prop="location" label="场地" min-width="120" show-overflow-tooltip />
        <el-table-column prop="startTime" label="开始时间" width="155">
          <template #default="{ row }">{{ formatTime(row.startTime) }}</template>
        </el-table-column>
        <el-table-column prop="endTime" label="结束时间" width="155">
          <template #default="{ row }">{{ formatTime(row.endTime) }}</template>
        </el-table-column>
        <el-table-column label="报名人数" width="100" align="center">
          <template #default="{ row }">{{ row.currentParticipants || 0 }} / {{ row.maxParticipants || '不限' }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="getActivityStatusType(row.status)" size="small">{{ getActivityStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column v-if="activeScope === 'approval' || (!isSuperAdmin && activeScope === 'managed')" label="审批状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getApprovalStatusType(row.approvalStatus)" size="small">{{ getApprovalStatusText(row.approvalStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" :width="actionColumnWidth" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" link @click="handleDetail(row)">详情</el-button>
            <template v-if="activeScope === 'approval'">
              <el-button size="small" type="success" link :loading="activityReviewingId === row.id" @click="handleActivityReview(row, 1)">同意</el-button>
              <el-button size="small" type="danger" link :loading="activityReviewingId === row.id" @click="handleActivityReview(row, 2)">拒绝</el-button>
              <el-button size="small" type="danger" link @click="handleDelete(row)">删除</el-button>
            </template>
            <template v-else-if="activeScope === 'managed'">
              <el-button v-if="!isSuperAdmin && row.approvalStatus === 1" size="small" type="success" link @click="handleRegistrations(row)">报名名单</el-button>
              <el-button v-if="row.canApplyFunding" size="small" type="warning" link @click="handleFunding(row)">经费申请</el-button>
              <el-button v-if="!isSuperAdmin" size="small" type="primary" link @click="handleEdit(row)">编辑</el-button>
              <el-button size="small" type="danger" link @click="handleDelete(row)">删除</el-button>
            </template>
            <template v-else>
              <el-tag v-if="row.myRegistrationStatus === 0" type="warning" size="small">待审核</el-tag>
              <el-tag v-else-if="row.myRegistrationStatus === 1" type="success" size="small">报名成功</el-tag>
              <el-button
                v-else-if="row.canRegister"
                size="small"
                type="success"
                link
                :loading="registeringId === row.id"
                @click="handleRegister(row)"
              >{{ row.myRegistrationStatus === 2 ? '重新报名' : '报名' }}</el-button>
              <el-tooltip v-else content="当前活动不可报名或人数已满" placement="top">
                <span class="disabled-action">不可报名</span>
              </el-tooltip>
            </template>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="searchForm.current"
          v-model:page-size="searchForm.size"
          :page-sizes="[10, 20, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          background
          @size-change="loadData"
          @current-change="loadData"
        />
      </div>
    </div>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="620px">
      <el-alert v-if="currentRequiresApproval" title="该活动将提交给超级管理员审批，通过后才正式开放。" type="warning" :closable="false" class="scope-tip" />
      <el-form :model="form" label-width="110px">
        <el-form-item label="负责社团" required>
          <el-select v-model="form.clubId" placeholder="请选择负责社团" style="width: 100%">
            <el-option
              v-for="option in manageOptions"
              :key="option.clubId"
              :value="option.clubId"
              :label="option.clubName"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="活动标题" required>
          <el-input v-model="form.title" placeholder="请输入活动标题" />
        </el-form-item>
        <el-form-item label="活动场地">
          <el-input v-model="form.location" placeholder="请输入活动场地" />
        </el-form-item>
        <el-form-item label="开始时间" required>
          <el-date-picker v-model="form.startTime" type="datetime" placeholder="选择开始时间" style="width: 100%" />
        </el-form-item>
        <el-form-item label="结束时间" required>
          <el-date-picker v-model="form.endTime" type="datetime" placeholder="选择结束时间" style="width: 100%" />
        </el-form-item>
        <el-form-item label="最大参与人数">
          <el-input-number v-model="form.maxParticipants" :min="0" :max="9999" />
          <span class="form-tip">0表示不限，只有审核通过的报名计入人数</span>
        </el-form-item>
        <el-form-item label="活动内容">
          <el-input v-model="form.content" type="textarea" :rows="4" placeholder="请输入活动内容" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="2">未开始</el-radio>
            <el-radio :value="1">进行中</el-radio>
            <el-radio :value="0">已结束</el-radio>
            <el-radio :value="3">已取消</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailVisible" title="活动详情" width="620px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="活动标题" :span="2">{{ currentActivity.title }}</el-descriptions-item>
        <el-descriptions-item label="负责社团">{{ currentActivity.clubName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="申请人">{{ currentActivity.organizerName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="活动场地" :span="2">{{ currentActivity.location || '-' }}</el-descriptions-item>
        <el-descriptions-item label="开始时间">{{ formatTime(currentActivity.startTime) }}</el-descriptions-item>
        <el-descriptions-item label="结束时间">{{ formatTime(currentActivity.endTime) }}</el-descriptions-item>
        <el-descriptions-item label="报名人数">{{ currentActivity.currentParticipants || 0 }} / {{ currentActivity.maxParticipants || '不限' }}</el-descriptions-item>
        <el-descriptions-item label="活动状态">{{ getActivityStatusText(currentActivity.status) }}</el-descriptions-item>
        <el-descriptions-item label="审批状态">{{ getApprovalStatusText(currentActivity.approvalStatus) }}</el-descriptions-item>
        <el-descriptions-item label="审批时间">{{ formatTime(currentActivity.reviewTime) }}</el-descriptions-item>
        <el-descriptions-item v-if="currentActivity.reviewRemark" label="审批意见" :span="2">{{ currentActivity.reviewRemark }}</el-descriptions-item>
        <el-descriptions-item label="活动内容" :span="2">
          <div class="activity-content">{{ currentActivity.content || '暂无活动内容' }}</div>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <el-dialog v-model="registrationVisible" :title="`报名名单 - ${currentActivity.title || ''}`" width="1000px">
      <el-table :data="registrationList" v-loading="registrationLoading" stripe>
        <el-table-column label="报名人" min-width="120">
          <template #default="{ row }">{{ row.realName || row.nickname || row.username }}</template>
        </el-table-column>
        <el-table-column prop="username" label="学号" width="110" />
        <el-table-column prop="college" label="学院" min-width="140" />
        <el-table-column prop="major" label="专业" min-width="120" />
        <el-table-column prop="phone" label="手机号" width="120" />
        <el-table-column label="报名时间" width="155">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="getRegistrationStatusType(row.status)" size="small">{{ getRegistrationStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="reviewRemark" label="审核意见" min-width="120" show-overflow-tooltip />
        <el-table-column label="操作" width="130" fixed="right">
          <template #default="{ row }">
            <template v-if="row.status === 0">
              <el-button size="small" type="success" link :loading="reviewingId === row.id" @click="handleReview(row, 1)">通过</el-button>
              <el-button size="small" type="danger" link :loading="reviewingId === row.id" @click="handleReview(row, 2)">拒绝</el-button>
            </template>
            <span v-else class="disabled-action">已审核</span>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <el-dialog v-model="fundingVisible" :title="`活动经费申请 - ${currentActivity.title || ''}`" width="560px">
      <el-alert title="申请将提交至学校超级管理员的经费申请中心。" type="info" :closable="false" class="funding-tip" />
      <el-form label-width="100px">
        <el-form-item label="负责社团">
          <el-input :model-value="currentActivity.clubName" disabled />
        </el-form-item>
        <el-form-item label="申请金额" required>
          <el-input-number
            v-model="fundingForm.amount"
            :min="0.01"
            :max="9999999999.99"
            :precision="2"
            :controls="false"
            placeholder="请输入申请金额（元）"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="用途说明" required>
          <el-input
            v-model="fundingForm.reason"
            type="textarea"
            :rows="5"
            maxlength="500"
            show-word-limit
            placeholder="请说明经费用途、预算明细等"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="fundingVisible = false">取消</el-button>
        <el-button type="primary" :loading="fundingSubmitLoading" @click="submitFunding">提交申请</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="myFundingVisible" title="我的经费申请" width="900px">
      <el-table :data="myFundingList" v-loading="myFundingLoading" stripe>
        <el-table-column prop="activityTitle" label="申请活动" min-width="150" show-overflow-tooltip />
        <el-table-column prop="clubName" label="社团" min-width="120" />
        <el-table-column label="金额" width="120" align="right">
          <template #default="{ row }">¥ {{ Number(row.amount || 0).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 }) }}</template>
        </el-table-column>
        <el-table-column prop="reason" label="用途说明" min-width="160" show-overflow-tooltip />
        <el-table-column label="审批状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getFundingStatusType(row.status)" size="small">{{ getFundingStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="审批人" width="100">
          <template #default="{ row }">{{ row.reviewerName || '-' }}</template>
        </el-table-column>
        <el-table-column label="审批意见" min-width="140" show-overflow-tooltip>
          <template #default="{ row }">{{ row.reviewRemark || '-' }}</template>
        </el-table-column>
        <el-table-column label="申请时间" width="150">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!myFundingLoading && myFundingList.length === 0" description="暂无经费申请" />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onBeforeUnmount } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getActivityPage,
  addActivity,
  updateActivity,
  deleteActivity,
  getActivityManageOptions,
  registerActivity,
  getActivityRegistrations,
  reviewActivityRegistration,
  reviewActivity
} from '@/api/activity'
import { createFundingApplication, getMyFundingApplications } from '@/api/funding'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const isSuperAdmin = computed(() => userStore.roles.includes('SUPER_ADMIN'))

const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const total = ref(0)
const activeScope = ref('managed')
const manageOptions = ref([])
const dialogVisible = ref(false)
const detailVisible = ref(false)
const registrationVisible = ref(false)
const isEdit = ref(false)
const currentActivity = ref({})
const registrationList = ref([])
const registrationLoading = ref(false)
const registeringId = ref(null)
const reviewingId = ref(null)
const activityReviewingId = ref(null)
const fundingVisible = ref(false)
const fundingSubmitLoading = ref(false)
const fundingForm = reactive({ amount: null, reason: '' })
const myFundingVisible = ref(false)
const myFundingLoading = ref(false)
const myFundingList = ref([])

const searchForm = reactive({ keyword: '', status: null, current: 1, size: 10 })
const emptyForm = () => ({
  id: null,
  clubId: null,
  title: '',
  location: '',
  startTime: null,
  endTime: null,
  maxParticipants: 0,
  content: '',
  status: 2
})
const form = reactive(emptyForm())
const currentRequiresApproval = computed(() =>
  manageOptions.value.some(option => option.clubId === form.clubId && option.approvalRequired)
)
const dialogTitle = computed(() => {
  if (!currentRequiresApproval.value) return isEdit.value ? '编辑活动' : '新增活动'
  if (isEdit.value && form.approvalStatus === 2) return '重新提交活动申请'
  return isEdit.value ? '编辑活动申请' : '提交活动申请'
})
const scopeTip = computed(() => {
  if (activeScope.value === 'approval') return '这里显示部长提交的新活动申请，审批通过后活动才正式开放。'
  if (isSuperAdmin.value) return '超级管理员只能查看和删除活动，不能新增或编辑活动信息。'
  return '部长提交的新活动需经超级管理员审批；被拒绝后可修改并重新提交。'
})
const actionColumnWidth = computed(() => {
  if (activeScope.value === 'approval') return 230
  if (activeScope.value === 'other') return 180
  return isSuperAdmin.value ? 130 : 330
})

const formatTime = (time) => time?.replace('T', ' ').substring(0, 16) || '-'
const getActivityStatusType = (status) => ({ 0: 'info', 1: 'success', 2: 'warning', 3: 'danger' })[status] || 'info'
const getActivityStatusText = (status) => ({ 0: '已结束', 1: '进行中', 2: '未开始', 3: '已取消' })[status] || '-'
const getApprovalStatusType = (status) => ({ 0: 'warning', 1: 'success', 2: 'danger' })[status] || 'info'
const getApprovalStatusText = (status) => ({ 0: '待审批', 1: '已通过', 2: '已拒绝' })[status] || '-'
const getRegistrationStatusType = (status) => ({ 0: 'warning', 1: 'success', 2: 'danger' })[status] || 'info'
const getRegistrationStatusText = (status) => ({ 0: '待审核', 1: '报名成功', 2: '已拒绝' })[status] || '-'

let searchTimer
const loadData = async () => {
  loading.value = true
  try {
    const res = await getActivityPage({ ...searchForm, scope: activeScope.value })
    tableData.value = res.data?.records || []
    total.value = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

const loadManageOptions = async () => {
  const res = await getActivityManageOptions()
  manageOptions.value = res.data || []
}

const handleSearch = () => {
  clearTimeout(searchTimer)
  searchForm.current = 1
  loadData()
}
const handleRealtimeSearch = () => {
  clearTimeout(searchTimer)
  searchTimer = setTimeout(handleSearch, 300)
}
const handleReset = () => {
  searchForm.keyword = ''
  searchForm.status = null
  handleSearch()
}
const handleScopeChange = () => {
  searchForm.current = 1
  loadData()
}
onBeforeUnmount(() => clearTimeout(searchTimer))

const handleAdd = () => {
  isEdit.value = false
  Object.assign(form, emptyForm())
  if (manageOptions.value.length === 1) {
    form.clubId = manageOptions.value[0].clubId
  }
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  Object.assign(form, emptyForm(), row)
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!form.clubId) { ElMessage.warning('请选择负责社团'); return }
  if (!form.title?.trim()) { ElMessage.warning('请输入活动标题'); return }
  if (!form.startTime || !form.endTime) { ElMessage.warning('请选择活动开始和结束时间'); return }
  if (new Date(form.endTime) <= new Date(form.startTime)) { ElMessage.warning('结束时间必须晚于开始时间'); return }
  submitLoading.value = true
  try {
    const data = { ...form }
    if (isEdit.value) {
      await updateActivity(data)
      ElMessage.success(currentRequiresApproval.value && form.approvalStatus === 2 ? '活动申请已重新提交' : '更新成功')
    } else {
      await addActivity(data)
      ElMessage.success(currentRequiresApproval.value ? '活动申请已提交，请等待超级管理员审批' : '新增成功')
    }
    dialogVisible.value = false
    loadData()
  } finally {
    submitLoading.value = false
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除活动「${row.title}」吗？`, '提示', { type: 'warning' })
    await deleteActivity(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) { /* 用户取消 */ }
}

const handleDetail = (row) => {
  currentActivity.value = row
  detailVisible.value = true
}

const handleActivityReview = async (row, status) => {
  try {
    let reviewRemark = ''
    if (status === 1) {
      await ElMessageBox.confirm(`确定同意活动「${row.title}」的申请吗？`, '审批活动', { type: 'success' })
    } else {
      const result = await ElMessageBox.prompt('可填写拒绝原因', '拒绝活动申请', {
        confirmButtonText: '确定拒绝',
        cancelButtonText: '取消',
        inputPlaceholder: '请输入审批意见（选填）',
        inputValidator: (value) => !value || value.length <= 500 || '审批意见不能超过500个字符'
      })
      reviewRemark = result.value || ''
    }
    activityReviewingId.value = row.id
    await reviewActivity(row.id, { status, reviewRemark })
    ElMessage.success(status === 1 ? '已同意活动申请' : '已拒绝活动申请')
    loadData()
  } catch (error) { /* 用户取消或请求拦截器已提示 */ } finally {
    activityReviewingId.value = null
  }
}

const handleRegister = async (row) => {
  try {
    await ElMessageBox.confirm(`确定报名「${row.title}」吗？提交后需由负责社团审核。`, '活动报名', { type: 'info' })
    registeringId.value = row.id
    await registerActivity(row.id)
    ElMessage.success('报名已提交，请等待审核')
    loadData()
  } catch (error) {
    if (error !== 'cancel' && error?.message !== 'cancel') loadData()
  } finally {
    registeringId.value = null
  }
}

const handleFunding = (row) => {
  currentActivity.value = row
  Object.assign(fundingForm, { amount: null, reason: '' })
  fundingVisible.value = true
}

const submitFunding = async () => {
  if (!fundingForm.amount || fundingForm.amount <= 0) { ElMessage.warning('请输入大于0的申请金额'); return }
  if (!fundingForm.reason?.trim()) { ElMessage.warning('请输入经费用途说明'); return }
  fundingSubmitLoading.value = true
  try {
    await createFundingApplication({
      activityId: currentActivity.value.id,
      amount: fundingForm.amount,
      reason: fundingForm.reason.trim()
    })
    ElMessage.success('经费申请已提交至学校经费申请中心')
    fundingVisible.value = false
  } finally {
    fundingSubmitLoading.value = false
  }
}

const getFundingStatusType = (status) => ({ 0: 'warning', 1: 'success', 2: 'danger' })[status] || 'info'
const getFundingStatusText = (status) => ({ 0: '待审批', 1: '已通过', 2: '已拒绝' })[status] || '-'

const handleViewMyFunding = async () => {
  myFundingVisible.value = true
  myFundingLoading.value = true
  try {
    const res = await getMyFundingApplications()
    myFundingList.value = res.data || []
  } finally {
    myFundingLoading.value = false
  }
}

const handleRegistrations = async (row) => {
  currentActivity.value = row
  registrationVisible.value = true
  await loadRegistrations()
}

const loadRegistrations = async () => {
  registrationLoading.value = true
  try {
    const res = await getActivityRegistrations(currentActivity.value.id)
    registrationList.value = res.data || []
  } finally {
    registrationLoading.value = false
  }
}

const handleReview = async (row, status) => {
  try {
    let reviewRemark = ''
    if (status === 1) {
      await ElMessageBox.confirm(`确定通过 ${row.realName || row.username} 的报名吗？`, '审核报名', { type: 'success' })
    } else {
      const result = await ElMessageBox.prompt('可填写拒绝原因', '拒绝报名', {
        confirmButtonText: '确定拒绝',
        cancelButtonText: '取消',
        inputPlaceholder: '请输入审核意见（选填）',
        inputValidator: (value) => !value || value.length <= 500 || '审核意见不能超过500个字符'
      })
      reviewRemark = result.value || ''
    }
    reviewingId.value = row.id
    await reviewActivityRegistration(row.id, { status, reviewRemark })
    ElMessage.success(status === 1 ? '已通过报名' : '已拒绝报名')
    await Promise.all([loadRegistrations(), loadData()])
  } catch (error) { /* 用户取消或请求拦截器已提示 */ } finally {
    reviewingId.value = null
  }
}

onMounted(() => {
  loadManageOptions()
  loadData()
})
</script>

<style lang="scss" scoped>
.table-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
}
.scope-tip { margin-bottom: 16px; }
.pagination-wrapper {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
.form-tip {
  margin-left: 8px;
  font-size: 12px;
  color: #909399;
}
.funding-tip { margin-bottom: 18px; }
.activity-content {
  white-space: pre-wrap;
  word-break: break-word;
  line-height: 1.7;
}
.disabled-action {
  color: #909399;
  font-size: 12px;
}
@media (max-width: 900px) {
  .table-toolbar { flex-direction: column; }
}
</style>
