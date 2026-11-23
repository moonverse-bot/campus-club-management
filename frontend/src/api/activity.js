import request from '@/utils/request'

// 分页查询活动
export function getActivityPage(params) {
  return request({
    url: '/activities',
    method: 'get',
    params
  })
}

// 根据ID查询活动
export function getActivityById(id) {
  return request({
    url: `/activities/${id}`,
    method: 'get'
  })
}

// 新增活动
export function addActivity(data) {
  return request({
    url: '/activities',
    method: 'post',
    data
  })
}

// 更新活动
export function updateActivity(data) {
  return request({
    url: '/activities',
    method: 'put',
    data
  })
}

// 删除活动
export function deleteActivity(id) {
  return request({
    url: `/activities/${id}`,
    method: 'delete'
  })
}

// 当前用户可管理的社团及部门选项
export function getActivityManageOptions() {
  return request({
    url: '/activities/manage-options',
    method: 'get'
  })
}

// 审批活动申请
export function reviewActivity(id, data) {
  return request({
    url: `/activities/${id}/review`,
    method: 'put',
    data
  })
}

// 报名活动
export function registerActivity(id) {
  return request({
    url: `/activities/${id}/registrations`,
    method: 'post'
  })
}

// 活动报名名单
export function getActivityRegistrations(id) {
  return request({
    url: `/activities/${id}/registrations`,
    method: 'get'
  })
}

// 审核活动报名
export function reviewActivityRegistration(id, data) {
  return request({
    url: `/activities/registrations/${id}/review`,
    method: 'put',
    data
  })
}
