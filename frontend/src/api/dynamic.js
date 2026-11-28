import request from '@/utils/request'

// 分页查询动态
export function getDynamicPage(params) {
  return request({
    url: '/dynamics',
    method: 'get',
    params
  })
}

// 根据ID查询动态
export function getDynamicById(id) {
  return request({
    url: `/dynamics/${id}`,
    method: 'get'
  })
}

// 发布动态
export function addDynamic(data) {
  return request({
    url: '/dynamics',
    method: 'post',
    data
  })
}

// 更新动态
export function updateDynamic(data) {
  return request({
    url: '/dynamics',
    method: 'put',
    data
  })
}

// 删除动态
export function deleteDynamic(id) {
  return request({
    url: `/dynamics/${id}`,
    method: 'delete'
  })
}
