<template>
  <div class="page-container">
    <div class="page-header">
      <h2>经费申请中心</h2>
      <p>查看并审批社团部长提交的活动经费申请</p>
    </div>

    <div class="card-container">
      <el-alert
        title="待审批申请可通过或拒绝；已处理记录可查看审批结果，支持删除。"
        type="info"
        :closable="false"
        class="center-tip"
      />
      <div class="table-toolbar">
        <el-form :inline="true">
          <el-form-item label="关键词">
            <el-input
              v-model="searchForm.keyword"
              placeholder="输入活动、社团或申请人关键字"
              clearable
              style="width: 260px"
              @input="handleRealtimeSearch"
            />
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="searchForm.status" placeholder="全部" clearable style="width: 120px" @change="handleSearch">
              <el-option :value="0" label="待审批" />
              <el-option :value="1" label="已通过" />
              <el-option :value="2" label="已拒绝" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">搜索</el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <el-table :data="filteredData" stripe style="width: 100%" v-loading="loading">
        <el-table-column prop="activityTitle" label="申请活动" min-width="160" show-overflow-tooltip />
        <el-table-column prop="clubName" label="申请社团" min-width="130" show-overflow-tooltip />
        <el-table-column label="申请人" min-width="120">
          <template #default="{ row }">
            <div>{{ row.applicantName }}</div>
            <div class="sub-text">{{ row.applicantUsername }}</div>
          </template>
        </el-table-column>
        <el-table-column label="申请金额" width="130" align="right">
          <template #default="{ row }">¥ {{ formatAmount(row.amount) }}</template>
        </el-table-column>
        <el-table-column prop="reason" label="经费用途说明" min-width="200" show-overflow-tooltip />
        <el-table-column label="审批状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="审批时间" width="160">
          <template #default="{ row }">{{ formatTime(row.reviewTime) }}</template>
        </el-table-column>
        <el-table-column label="审批意见" min-width="140" show-overflow-tooltip>
          <template #default="{ row }">{{ row.reviewRemark || '-' }}</template>
        </el-table-column>
        <el-table-column label="申请时间" width="155">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="170" fixed="right">
          <template #default="{ row }">
            <template v-if="row.status === 0">
              <el-button size="small" type="success" link :loading="reviewingId === row.id" @click="handleReview(row, 1)">通过</el-button>
              <el-button size="small" type="danger" link :loading="reviewingId === row.id" @click="handleReview(row, 2)">拒绝</el-button>
            </template>
            <el-button size="small" type="danger" link @click="handleDelete(row)">删除</el-button>
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
  </div>
</template>

<script setup>
import { reactive, ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getFundingApplicationPage, deleteFundingApplication, reviewFundingApplication } from '@/api/funding'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const reviewingId = ref(null)
const searchForm = reactive({ keyword: '', status: null, current: 1, size: 10 })
let searchTimer

const filteredData = computed(() =>
  searchForm.status === null || searchForm.status === undefined
    ? tableData.value
    : tableData.value.filter(row => row.status === searchForm.status)
)

const formatTime = (time) => time?.replace('T', ' ').substring(0, 16) || '-'
const formatAmount = (amount) => Number(amount || 0).toLocaleString('zh-CN', {
  minimumFractionDigits: 2,
  maximumFractionDigits: 2
})
const getStatusType = (status) => ({ 0: 'warning', 1: 'success', 2: 'danger' })[status] || 'info'
const getStatusText = (status) => ({ 0: '待审批', 1: '已通过', 2: '已拒绝' })[status] || '-'

const loadData = async () => {
  loading.value = true
  try {
    const res = await getFundingApplicationPage(searchForm)
    tableData.value = res.data?.records || []
    total.value = res.data?.total || 0
  } finally {
    loading.value = false
  }
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

const handleReview = async (row, status) => {
  try {
    let reviewRemark = ''
    if (status === 1) {
      await ElMessageBox.confirm(`确定通过活动「${row.activityTitle}」的经费申请吗？`, '审批经费申请', { type: 'success' })
    } else {
      const result = await ElMessageBox.prompt('可填写拒绝原因', '拒绝经费申请', {
        confirmButtonText: '确定拒绝',
        cancelButtonText: '取消',
        inputPlaceholder: '请输入审批意见（选填）',
        inputValidator: (value) => !value || value.length <= 500 || '审批意见不能超过500个字符'
      })
      reviewRemark = result.value || ''
    }
    reviewingId.value = row.id
    await reviewFundingApplication(row.id, { status, reviewRemark })
    ElMessage.success(status === 1 ? '已通过经费申请' : '已拒绝经费申请')
    loadData()
  } catch (error) { /* 用户取消或请求拦截器已提示 */ } finally {
    reviewingId.value = null
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定删除活动「${row.activityTitle}」的经费申请吗？`,
      '删除申请',
      { type: 'warning', confirmButtonText: '确定删除' }
    )
    await deleteFundingApplication(row.id)
    ElMessage.success('经费申请已删除')
    if (tableData.value.length === 1 && searchForm.current > 1) {
      searchForm.current -= 1
    }
    loadData()
  } catch (error) { /* 用户取消 */ }
}

onMounted(loadData)
onBeforeUnmount(() => clearTimeout(searchTimer))
</script>

<style lang="scss" scoped>
.center-tip { margin-bottom: 18px; }
.table-toolbar { display: flex; justify-content: space-between; }
.sub-text { color: #909399; font-size: 12px; margin-top: 3px; }
.pagination-wrapper {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
