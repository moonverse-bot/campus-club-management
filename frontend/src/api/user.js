import request from '@/utils/request'

// 分页查询用户
export function getUserPage(params) {
  return request({
    url: '/users',
    method: 'get',
    params
  })
}

// 根据ID查询用户
export function getUserById(id) {
  return request({
    url: `/users/${id}`,
    method: 'get'
  })
}

// 更新用户
export function updateUser(data) {
  return request({
    url: '/users',
    method: 'put',
    data
  })
}

// 删除用户
export function deleteUser(id) {
  return request({
    url: `/users/${id}`,
    method: 'delete'
  })
}

// 更新用户状态
export function updateUserStatus(id, status) {
  return request({
    url: `/users/${id}/status`,
    method: 'put',
    params: { status }
  })
}
