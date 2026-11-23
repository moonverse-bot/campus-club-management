<template>
  <div class="page-container">
    <div class="page-header">
      <h2>公告管理</h2>
      <p>管理系统公告和社团公告</p>
    </div>

    <div class="card-container">
      <div class="table-toolbar">
        <span>共 {{ total }} 条公告</span>
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon> 发布公告
        </el-button>
      </div>

      <el-table :data="tableData" stripe style="width: 100%" v-loading="loading">
        <el-table-column label="置顶" width="80">
          <template #default="{ row }">
            <el-tag v-if="row.isTop === 1" type="danger" size="small">置顶</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="公告标题" />
        <el-table-column prop="createTime" label="发布时间" width="180">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="current"
          v-model:page-size="size"
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
      <el-form :model="form" label-width="100px">
        <el-form-item label="公告标题" required>
          <el-input v-model="form.title" placeholder="请输入公告标题" />
        </el-form-item>
        <el-form-item label="公告内容" required>
          <el-input v-model="form.content" type="textarea" :rows="6" placeholder="请输入公告内容" />
        </el-form-item>
        <el-form-item label="置顶">
          <el-switch v-model="form.isTop" :active-value="1" :inactive-value="0" />
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
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAnnouncementPage, addAnnouncement, updateAnnouncement, deleteAnnouncement } from '@/api/announcement'

const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const total = ref(0)
const current = ref(1)
const size = ref(10)
const dialogVisible = ref(false)
const isEdit = ref(false)

const form = reactive({
  id: null,
  title: '',
  content: '',
  isTop: 0
})

const dialogTitle = computed(() => isEdit.value ? '编辑公告' : '发布公告')
const formatTime = (time) => time?.replace('T', ' ').substring(0, 16) || '-'

const loadData = async () => {
  loading.value = true
  try {
    const res = await getAnnouncementPage({ current: current.value, size: size.value })
    tableData.value = res.data?.records || []
    total.value = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  isEdit.value = false
  Object.assign(form, { id: null, title: '', content: '', isTop: 0 })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  Object.assign(form, row)
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!form.title || !form.content) { ElMessage.warning('请填写完整信息'); return }
  submitLoading.value = true
  try {
    if (isEdit.value) {
      await updateAnnouncement(form)
      ElMessage.success('更新成功')
    } else {
      await addAnnouncement(form)
      ElMessage.success('发布成功')
    }
    dialogVisible.value = false
    loadData()
  } finally {
    submitLoading.value = false
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除公告「${row.title}」吗？`, '提示', { type: 'warning' })
    await deleteAnnouncement(row.id)
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
