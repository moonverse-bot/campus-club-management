<template>
  <router-view />
</template>

<script setup>
import { onBeforeUnmount, watch } from 'vue'
import { heartbeat } from '@/api/auth'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
let heartbeatTimer

const stopHeartbeat = () => {
  clearInterval(heartbeatTimer)
  heartbeatTimer = null
}

const sendHeartbeat = () => {
  if (!localStorage.getItem('token')) {
    stopHeartbeat()
    return
  }
  heartbeat().catch(() => {})
}

const startHeartbeat = () => {
  stopHeartbeat()
  if (!userStore.token) return
  sendHeartbeat()
  heartbeatTimer = setInterval(sendHeartbeat, 30000)
}

watch(() => userStore.token, startHeartbeat, { immediate: true })
onBeforeUnmount(stopHeartbeat)
</script>

<style>
#app {
  width: 100%;
  height: 100vh;
}
</style>
