<template>
  <div class="page-container">
    <div class="page-header">
      <h2>经费申请中心</h2>
      <p>查看社团部长提交的活动经费申请</p>
    </div>

    <div class="card-container">
      <el-alert
        title="申请记录仅支持查询、查看和删除，不提供编辑或审核操作。"
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
          <el-form-item>
            <el-button type="primary" @click="handleSearch">搜索</el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <el-table :data="tableData" stripe style="width: 100%" v-loading="loading">
        <el-table-column prop="activityTitle" label="申请活动" min-width="170" show-overflow-tooltip />
        <el-table-column prop="clubName" label="申请社团" min-width="140" show-overflow-tooltip />
        <el-table-column label="申请人" min-width="130">
          <template #default="{ row }">
            <div>{{ row.applicantName }}</div>
            <div class="sub-text">{{ row.applicantUsername }}</div>
          </template>
        </el-table-column>
        <el-table-column label="申请金额" width="140" align="right">
          <template #default="{ row }">¥ {{ formatAmount(row.amount) }}</template>
        </el-table-column>
        <el-table-column prop="reason" label="经费用途说明" min-width="240" show-overflow-tooltip />
        <el-table-column label="申请时间" width="165">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="90" fixed="right">
          <template #default="{ row }">
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
import { reactive, ref, onMounted, onBeforeUnmount } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getFundingApplicationPage, deleteFundingApplication } from '@/api/funding'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const searchForm = reactive({ keyword: '', current: 1, size: 10 })
let searchTimer

const formatTime = (time) => time?.replace('T', ' ').substring(0, 16) || '-'
const formatAmount = (amount) => Number(amount || 0).toLocaleString('zh-CN', {
  minimumFractionDigits: 2,
  maximumFractionDigits: 2
})

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
  handleSearch()
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
