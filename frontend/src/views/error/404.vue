<template>
  <div class="not-found">
    <div class="content">
      <div class="error-code">404</div>
      <h2>抱歉，您访问的页面不存在</h2>
      <p>请检查URL是否正确，或者点击下方按钮返回首页</p>
      <el-button type="primary" size="large" @click="router.push('/')">返回首页</el-button>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
const router = useRouter()
</script>

<style lang="scss" scoped>
.not-found {
  width: 100%;
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;

  .content {
    text-align: center;

    .error-code {
      font-size: 120px;
      font-weight: 700;
      color: #409eff;
      margin-bottom: 16px;
      line-height: 1;
    }

    h2 { margin: 0 0 12px; color: #303133; }
    p { margin: 0 0 24px; color: #909399; }
  }
}
</style>
