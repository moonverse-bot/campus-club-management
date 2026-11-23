<template>
  <div class="page-container">
    <div class="page-header">
      <h2>个人中心</h2>
      <p>管理您的个人信息</p>
    </div>

    <el-row :gutter="20">
      <!-- 个人信息卡片 -->
      <el-col :span="8">
        <el-card shadow="hover" class="profile-card">
          <div class="avatar-wrapper">
            <el-avatar :size="100" class="avatar">
              <el-icon :size="50"><UserFilled /></el-icon>
            </el-avatar>
            <h3>{{ userStore.nickname }}</h3>
            <p class="user-id">学号：{{ userStore.username }}</p>
          </div>
          <el-divider />
          <div class="user-roles">
            <el-tag v-for="role in userStore.roles" :key="role" style="margin: 2px;">
              {{ getRoleName(role) }}
            </el-tag>
          </div>
        </el-card>
      </el-col>

      <!-- 基本信息 -->
      <el-col :span="16">
        <el-card shadow="hover" class="info-card">
          <template #header>
            <div class="card-header">
              <span>基本信息</span>
              <el-button type="primary" link @click="handleEdit">编辑资料</el-button>
            </div>
          </template>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="学号/用户名">{{ userInfo.username }}</el-descriptions-item>
            <el-descriptions-item label="昵称">{{ userInfo.nickname || '-' }}</el-descriptions-item>
            <el-descriptions-item label="真实姓名">{{ userInfo.realName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="性别">
              {{ userInfo.gender === 1 ? '男' : userInfo.gender === 2 ? '女' : '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="学院">{{ userInfo.college || '-' }}</el-descriptions-item>
            <el-descriptions-item label="专业">{{ userInfo.major || '-' }}</el-descriptions-item>
            <el-descriptions-item label="年级">{{ userInfo.grade || '-' }}</el-descriptions-item>
            <el-descriptions-item label="手机号">{{ userInfo.phone || '-' }}</el-descriptions-item>
            <el-descriptions-item label="邮箱" :span="2">{{ userInfo.email || '-' }}</el-descriptions-item>
            <el-descriptions-item label="注册时间" :span="2">{{ formatTime(userInfo.createTime) }}</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
    </el-row>

    <!-- 编辑弹窗 -->
    <el-dialog v-model="editDialogVisible" title="编辑个人信息" width="500px">
      <el-form :model="editForm" label-width="100px">
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
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'
import { updateUser, getUserById } from '@/api/user'

const userStore = useUserStore()

const editDialogVisible = ref(false)
const submitLoading = ref(false)
const userInfo = reactive({})
const editForm = reactive({})

const roleNameMap = {
  SUPER_ADMIN: '超级管理员', ADMIN: '管理员', PRESIDENT: '社长',
  VICE_PRESIDENT: '副社长', MINISTER: '部长', VICE_MINISTER: '副部长', MEMBER: '普通成员', STUDENT: '学生'
}
const getRoleName = (code) => roleNameMap[code] || code
const formatTime = (time) => time?.replace('T', ' ').substring(0, 16) || '-'

const handleEdit = () => {
  Object.assign(editForm, userInfo)
  editDialogVisible.value = true
}

const handleSubmit = async () => {
  submitLoading.value = true
  try {
    await updateUser(editForm)
    await userStore.fetchUserInfo()
    Object.assign(userInfo, editForm)
    ElMessage.success('保存成功')
    editDialogVisible.value = false
  } finally {
    submitLoading.value = false
  }
}

onMounted(async () => {
  if (userStore.userInfo) {
    Object.assign(userInfo, userStore.userInfo)
  }
  // 刷新最新信息
  const res = await getUserById(userStore.userInfo?.id)
  if (res.data) Object.assign(userInfo, res.data)
})
</script>

<style lang="scss" scoped>
.profile-card {
  border: none;
  text-align: center;

  .avatar-wrapper {
    padding: 20px 0;

    .avatar {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: #fff;
    }

    h3 {
      margin: 12px 0 4px;
      font-size: 18px;
    }

    .user-id {
      color: #909399;
      font-size: 13px;
    }
  }

  .user-roles {
    display: flex;
    flex-wrap: wrap;
    justify-content: center;
  }
}

.info-card {
  border: none;

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-size: 16px;
    font-weight: 600;
  }
}
</style>
