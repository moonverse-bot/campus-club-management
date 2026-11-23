import request from '@/utils/request'

// 提交活动经费申请
export function createFundingApplication(data) {
  return request({
    url: '/funding-applications',
    method: 'post',
    data
  })
}

// 超级管理员分页查询经费申请
export function getFundingApplicationPage(params) {
  return request({
    url: '/funding-applications',
    method: 'get',
    params
  })
}

// 删除经费申请
export function deleteFundingApplication(id) {
  return request({
    url: `/funding-applications/${id}`,
    method: 'delete'
  })
}
