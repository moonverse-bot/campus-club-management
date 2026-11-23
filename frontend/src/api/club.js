import request from '@/utils/request'

// 分页查询社团
export function getClubPage(params) {
  return request({
    url: '/clubs',
    method: 'get',
    params
  })
}

// 根据ID查询社团
export function getClubById(id) {
  return request({
    url: `/clubs/${id}`,
    method: 'get'
  })
}

// 新增社团
export function addClub(data) {
  return request({
    url: '/clubs',
    method: 'post',
    data
  })
}

// 更新社团
export function updateClub(data) {
  return request({
    url: '/clubs',
    method: 'put',
    data
  })
}

// 删除社团
export function deleteClub(id) {
  return request({
    url: `/clubs/${id}`,
    method: 'delete'
  })
}
