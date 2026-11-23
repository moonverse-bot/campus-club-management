import request from '@/utils/request'

// 根据社团ID查询部门列表
export function getDepartmentsByClubId(clubId) {
  return request({
    url: `/departments/club/${clubId}`,
    method: 'get'
  })
}

// 新增部门
export function addDepartment(data) {
  return request({
    url: '/departments',
    method: 'post',
    data
  })
}

// 更新部门
export function updateDepartment(data) {
  return request({
    url: '/departments',
    method: 'put',
    data
  })
}

// 删除部门
export function deleteDepartment(id) {
  return request({
    url: `/departments/${id}`,
    method: 'delete'
  })
}
