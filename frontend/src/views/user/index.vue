<template>
  <div class="page-container">
    <div class="page-header">
      <h2>用户管理</h2>
      <p>{{ readOnlyUser ? (isSuperAdmin ? '查看社长和副社长信息' : '查看本社团成员信息') : '管理系统中的用户信息' }}</p>
    </div>

    <div class="card-container">
      <!-- 搜索工具栏 -->
      <div class="table-toolbar">
        <el-form :inline="true" :model="searchForm" class="search-form">
          <el-form-item label="关键词">
            <el-input v-model="searchForm.keyword" placeholder="输入用户名/姓名关键字" clearable style="width: 200px" @input="handleRealtimeSearch" />
          </el-form-item>
          <el-form-item label="学院">
            <el-input v-model="searchForm.college" placeholder="输入学院关键字" clearable style="width: 160px" @input="handleRealtimeSearch" />
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="searchForm.status" placeholder="全部" clearable style="width: 120px" @change="handleSearch">
              <el-option :value="1" label="正常" />
              <el-option :value="0" label="禁用" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">搜索</el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 用户表格 -->
      <el-table :data="tableData" stripe style="width: 100%" v-loading="loading">
        <el-table-column prop="username" label="学号/用户名" width="140" />
        <el-table-column prop="nickname" label="昵称" width="120" />
        <el-table-column prop="realName" label="真实姓名" width="120" />
        <el-table-column prop="gender" label="性别" width="80">
          <template #default="{ row }">
            {{ row.gender === 1 ? '男' : row.gender === 2 ? '女' : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="college" label="学院" width="150" />
        <el-table-column prop="major" label="专业" width="140" />
        <el-table-column prop="grade" label="年级" width="100" />
        <el-table-column prop="roles" label="角色" width="180">
          <template #default="{ row }">
            <el-tag v-for="role in row.roles" :key="role" size="small" style="margin-right: 4px;">
              {{ getRoleName(role) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="在线状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.online ? 'success' : 'info'" size="small">
              {{ row.online ? '在线' : '离线' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="账号状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" link @click="handleView(row)">查看</el-button>
            <template v-if="canManage(row)">
              <el-button size="small" type="primary" link @click="handleEdit(row)">编辑</el-button>
              <el-button
                size="small"
                :type="row.status === 1 ? 'warning' : 'success'"
                link
                @click="handleToggleStatus(row)"
              >
                {{ row.status === 1 ? '禁用' : '启用' }}
              </el-button>
              <el-button size="small" type="danger" link @click="handleDelete(row)">删除</el-button>
            </template>
            <span v-else class="read-only-tip">仅查看</span>
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

    <!-- 用户详情/编辑弹窗 -->
    <el-dialog v-model="editDialogVisible" :title="formReadOnly ? '用户详情' : '编辑用户'" width="500px">
      <el-form ref="editFormRef" :model="editForm" label-width="100px" :disabled="formReadOnly">
        <el-form-item label="学号/用户名">
          <el-input v-model="editForm.username" disabled />
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="editForm.nickname" />
        </el-form-item>
        <el-form-item label="真实姓名">
          <el-input v-model="editForm.realName" />
        </el-form-item>
        <el-form-item label="性别">
          <el-radio-group v-model="editForm.gender">
            <el-radio :value="1">男</el-radio>
            <el-radio :value="2">女</el-radio>
            <el-radio :value="0">未知</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="editForm.phone" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="editForm.email" />
        </el-form-item>
        <el-form-item label="学院">
          <el-input v-model="editForm.college" />
        </el-form-item>
        <el-form-item label="专业">
          <el-input v-model="editForm.major" />
        </el-form-item>
        <el-form-item label="年级">
          <el-input v-model="editForm.grade" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">关闭</el-button>
        <el-button v-if="!formReadOnly" type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onBeforeUnmount } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUserPage, updateUser, deleteUser, updateUserStatus } from '@/api/user'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const total = ref(0)
const editDialogVisible = ref(false)
const viewOnly = ref(false)
const editFormRef = ref()

const searchForm = reactive({
  keyword: '',
  college: '',
  status: null,
  current: 1,
  size: 10
})

const editForm = reactive({
  id: null,
  username: '',
  nickname: '',
  realName: '',
  gender: 0,
  phone: '',
  email: '',
  college: '',
  major: '',
  grade: ''
})

const roleNameMap = {
  SUPER_ADMIN: '超级管理员',
  ADMIN: '管理员',
  PRESIDENT: '社长',
  VICE_PRESIDENT: '副社长',
  MINISTER: '部长',
  VICE_MINISTER: '副部长',
  MEMBER: '普通成员',
  STUDENT: '学生'
}

const roleLevelMap = {
  SUPER_ADMIN: 100,
  ADMIN: 90,
  PRESIDENT: 80,
  VICE_PRESIDENT: 70,
  MINISTER: 60,
  VICE_MINISTER: 50,
  MEMBER: 40,
  STUDENT: 30
}

const currentRoleLevel = computed(() =>
  Math.max(0, ...userStore.roles.map(role => roleLevelMap[role] ?? 0))
)
const isSuperAdmin = computed(() => userStore.roles.includes('SUPER_ADMIN'))
const readOnlyUser = computed(() => isSuperAdmin.value || userStore.roles.includes('PRESIDENT'))
const formReadOnly = computed(() => readOnlyUser.value || viewOnly.value)

const canManage = (row) => {
  if (readOnlyUser.value || !row.roles?.length) return false
  const targetRoleLevel = Math.max(...row.roles.map(role => roleLevelMap[role] ?? Number.MAX_SAFE_INTEGER))
  return targetRoleLevel < currentRoleLevel.value
}

const getRoleName = (code) => roleNameMap[code] || code

let searchTimer
let presenceTimer

const loadData = async (withLoading = true) => {
  if (withLoading) loading.value = true
  try {
    const res = await getUserPage(searchForm)
    tableData.value = res.data?.records || []
    total.value = res.data?.total || 0
  } finally {
    if (withLoading) loading.value = false
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
  searchForm.college = ''
  searchForm.status = null
  handleSearch()
}

onBeforeUnmount(() => {
  clearTimeout(searchTimer)
  clearInterval(presenceTimer)
})

const handleView = (row) => {
  viewOnly.value = true
  Object.assign(editForm, row)
  editDialogVisible.value = true
}

const handleEdit = (row) => {
  if (readOnlyUser.value) return
  viewOnly.value = false
  Object.assign(editForm, row)
  editDialogVisible.value = true
}

const handleSubmit = async () => {
  submitLoading.value = true
  try {
    await updateUser(editForm)
    ElMessage.success('更新成功')
    editDialogVisible.value = false
    loadData()
  } finally {
    submitLoading.value = false
  }
}

const handleToggleStatus = async (row) => {
  const newStatus = row.status === 1 ? 0 : 1
  const action = newStatus === 1 ? '启用' : '禁用'
  try {
    await ElMessageBox.confirm(`确定要${action}用户「${row.username}」吗？`, '提示', {
      type: 'warning'
    })
    await updateUserStatus(row.id, newStatus)
    ElMessage.success('操作成功')
    loadData()
  } catch (e) {
    // 取消
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要删除用户「${row.username}」吗？`, '提示', {
      type: 'warning'
    })
    await deleteUser(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) {
    // 取消
  }
}

onMounted(() => {
  loadData()
  presenceTimer = setInterval(() => loadData(false), 30000)
})
</script>

<style lang="scss" scoped>
.pagination-wrapper {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
.read-only-tip {
  margin-left: 8px;
  color: #909399;
  font-size: 12px;
}
</style>
