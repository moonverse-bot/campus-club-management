import request from '@/utils/request'

export function getUserMemberships(userId) {
  return request({
    url: `/members/user/${userId}`,
    method: 'get'
  })
}

export function getSuccessionCandidates(clubId) {
  return request({
    url: '/successions/candidates',
    method: 'get',
    params: { clubId }
  })
}

export function createSuccession(data) {
  return request({
    url: '/successions',
    method: 'post',
    data
  })
}

export function getMySuccessions() {
  return request({
    url: '/successions/my',
    method: 'get'
  })
}

export function getAdminSuccessions(status) {
  return request({
    url: '/successions/admin',
    method: 'get',
    params: status === null ? {} : { status }
  })
}

export function reviewSuccession(id, data) {
  return request({
    url: `/successions/${id}/review`,
    method: 'put',
    data
  })
}
