import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录', public: true }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/login/register.vue'),
    meta: { title: '注册', public: true }
  },
  {
    path: '/',
    component: () => import('@/views/layout/index.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '首页', icon: 'HomeFilled' }
      },
      {
        path: 'users',
        name: 'UserList',
        component: () => import('@/views/user/index.vue'),
        meta: {
          title: '用户管理',
          icon: 'User',
          roles: ['SUPER_ADMIN', 'ADMIN', 'PRESIDENT', 'VICE_PRESIDENT', 'MINISTER', 'VICE_MINISTER']
        }
      },
      {
        path: 'successions',
        name: 'MinisterSuccession',
        component: () => import('@/views/succession/index.vue'),
        meta: {
          title: '部长换届',
          icon: 'Refresh',
          roles: ['SUPER_ADMIN', 'MINISTER']
        }
      },
      {
        path: 'clubs',
        name: 'ClubList',
        component: () => import('@/views/club/index.vue'),
        meta: { title: '社团管理', icon: 'OfficeBuilding' }
      },
      {
        path: 'clubs/:id',
        name: 'ClubDetail',
        component: () => import('@/views/club/detail.vue'),
        meta: { title: '社团详情', hidden: true }
      },
      {
        path: 'activities',
        name: 'ActivityList',
        component: () => import('@/views/activity/index.vue'),
        meta: { title: '活动管理', icon: 'Calendar' }
      },
      {
        path: 'funding-applications',
        name: 'FundingApplicationCenter',
        component: () => import('@/views/funding/index.vue'),
        meta: { title: '经费申请中心', icon: 'Coin', roles: ['SUPER_ADMIN'] }
      },
      {
        path: 'announcements',
        name: 'AnnouncementList',
        component: () => import('@/views/announcement/index.vue'),
        meta: { title: '公告管理', icon: 'Bell' }
      },
      {
        path: 'dynamics',
        name: 'DynamicList',
        component: () => import('@/views/dynamic/index.vue'),
        meta: { title: '社团动态', icon: 'ChatDotRound' }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/profile/index.vue'),
        meta: { title: '个人中心', icon: 'UserFilled' }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/error/404.vue'),
    meta: { title: '页面不存在', public: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  
  if (to.meta.public) {
    if (to.path === '/login' && token) {
      next('/dashboard')
    } else {
      next()
    }
  } else {
    if (!token) {
      next({ path: '/login', query: { redirect: to.fullPath } })
    } else {
      const userInfo = JSON.parse(localStorage.getItem('userInfo') || 'null')
      const userRoles = userInfo?.roles || []
      if (to.meta.roles && !to.meta.roles.some(role => userRoles.includes(role))) {
        next('/dashboard')
        return
      }
      next()
    }
  }
})

export default router
