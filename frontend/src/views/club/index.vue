<template>
  <div class="page-container">
    <div class="page-header">
      <h2>社团管理</h2>
      <p>管理学校所有社团信息</p>
    </div>

    <div class="card-container">
      <!-- 搜索工具栏 -->
      <div class="table-toolbar">
        <el-form :inline="true" :model="searchForm" class="search-form">
          <el-form-item label="关键词">
            <el-input v-model="searchForm.keyword" placeholder="输入社团名称/编码关键字" clearable style="width: 200px" @input="handleRealtimeSearch" />
          </el-form-item>
          <el-form-item label="类别">
            <el-select v-model="searchForm.category" placeholder="全部" clearable style="width: 160px" @change="handleSearch">
              <el-option label="学术科技" value="学术科技" />
              <el-option label="文化艺术" value="文化艺术" />
              <el-option label="体育竞技" value="体育竞技" />
              <el-option label="公益志愿" value="公益志愿" />
              <el-option label="其他" value="其他" />
            </el-select>
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="searchForm.status" placeholder="全部" clearable style="width: 120px" @change="handleSearch">
              <el-option :value="1" label="正常" />
              <el-option :value="2" label="待审核" />
              <el-option :value="0" label="注销" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">搜索</el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon> 新增社团
        </el-button>
      </div>

      <!-- 社团表格 -->
      <el-table :data="tableData" stripe style="width: 100%" v-loading="loading">
        <el-table-column prop="clubName" label="社团名称" width="180">
          <template #default="{ row }">
            <el-link type="primary" @click="handleViewDetail(row)">{{ row.clubName }}</el-link>
          </template>
        </el-table-column>
        <el-table-column prop="clubCode" label="社团编码" width="120" />
        <el-table-column prop="category" label="类别" width="120">
          <template #default="{ row }">
            <el-tag size="small">{{ row.category }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="advisor" label="指导老师" width="120" />
        <el-table-column prop="memberCount" label="成员数" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getClubStatusType(row.status)" size="small">
              {{ getClubStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" link @click="handleViewDetail(row)">详情</el-button>
            <el-button size="small" type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
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

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form ref="formRef" :model="form" label-width="100px">
        <el-form-item label="社团名称" required>
          <el-input v-model="form.clubName" placeholder="请输入社团名称" />
        </el-form-item>
        <el-form-item label="社团编码">
          <el-input v-model="form.clubCode" placeholder="请输入社团编码" />
        </el-form-item>
        <el-form-item label="社团类别">
          <el-select v-model="form.category" placeholder="请选择类别" style="width: 100%">
            <el-option label="学术科技" value="学术科技" />
            <el-option label="文化艺术" value="文化艺术" />
            <el-option label="体育竞技" value="体育竞技" />
            <el-option label="公益志愿" value="公益志愿" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="指导老师">
          <el-input v-model="form.advisor" placeholder="请输入指导老师姓名" />
        </el-form-item>
        <el-form-item label="社团简介">
          <el-input v-model="form.description" type="textarea" :rows="4" placeholder="请输入社团简介" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">正常</el-radio>
            <el-radio :value="2">待审核</el-radio>
            <el-radio :value="0">注销</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getClubPage, addClub, updateClub, deleteClub } from '@/api/club'

const router = useRouter()

const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const isEdit = ref(false)

const searchForm = reactive({
  keyword: '',
  category: '',
  status: null,
  current: 1,
  size: 10
})

const form = reactive({
  id: null,
  clubName: '',
  clubCode: '',
  category: '',
  advisor: '',
  description: '',
  status: 1
})

const dialogTitle = computed(() => isEdit.value ? '编辑社团' : '新增社团')

const formatTime = (time) => time?.replace('T', ' ').substring(0, 16) || '-'

const getClubStatusType = (status) => {
  const map = { 0: 'info', 1: 'success', 2: 'warning' }
  return map[status] || 'info'
}

const getClubStatusText = (status) => {
  const map = { 0: '注销', 1: '正常', 2: '待审核' }
  return map[status] || '未知'
}

let searchTimer

const loadData = async () => {
  loading.value = true
  try {
    const res = await getClubPage(searchForm)
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
  searchForm.category = ''
  searchForm.status = null
  handleSearch()
}

onBeforeUnmount(() => clearTimeout(searchTimer))

const handleAdd = () => {
  isEdit.value = false
  Object.assign(form, {
    id: null, clubName: '', clubCode: '', category: '',
    advisor: '', description: '', status: 1
  })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  Object.assign(form, row)
  dialogVisible.value = true
}

const handleViewDetail = (row) => {
  router.push(`/clubs/${row.id}`)
}

const handleSubmit = async () => {
  if (!form.clubName) {
    ElMessage.warning('请输入社团名称')
    return
  }
  submitLoading.value = true
  try {
    if (isEdit.value) {
      await updateClub(form)
      ElMessage.success('更新成功')
    } else {
      await addClub(form)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    loadData()
  } finally {
    submitLoading.value = false
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要删除社团「${row.clubName}」吗？`, '提示', { type: 'warning' })
    await deleteClub(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) { /* 取消 */ }
}

onMounted(() => loadData())
</script>

<style lang="scss" scoped>
.pagination-wrapper {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
