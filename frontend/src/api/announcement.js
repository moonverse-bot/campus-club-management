import request from '@/utils/request'

// 获取置顶公告
export function getTopAnnouncements() {
  return request({
    url: '/announcements/top',
    method: 'get'
  })
}

// 分页查询公告
export function getAnnouncementPage(params) {
  return request({
    url: '/announcements',
    method: 'get',
    params
  })
}

// 新增公告
export function addAnnouncement(data) {
  return request({
    url: '/announcements',
    method: 'post',
    data
  })
}

// 更新公告
export function updateAnnouncement(data) {
  return request({
    url: '/announcements',
    method: 'put',
    data
  })
}

// 删除公告
export function deleteAnnouncement(id) {
  return request({
    url: `/announcements/${id}`,
    method: 'delete'
  })
}
