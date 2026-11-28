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

// 部长查询自己提交的经费申请及审批结果
export function getMyFundingApplications() {
  return request({
    url: '/funding-applications/my',
    method: 'get'
  })
}

// 超级管理员审批经费申请：status 1-通过 2-拒绝
export function reviewFundingApplication(id, data) {
  return request({
    url: `/funding-applications/${id}/review`,
    method: 'put',
    data
  })
}

// 删除经费申请
export function deleteFundingApplication(id) {
  return request({
    url: `/funding-applications/${id}`,
    method: 'delete'
  })
}
