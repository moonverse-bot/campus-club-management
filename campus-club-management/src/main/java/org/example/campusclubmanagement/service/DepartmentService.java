package org.example.campusclubmanagement.service;

import org.example.campusclubmanagement.entity.Department;

import java.util.List;

/**
 * 部门服务接口
 */
public interface DepartmentService {

    List<Department> listByClubId(Long clubId);

    Department getById(Long id);

    void add(Department department);

    void update(Department department);

    void delete(Long id);
}
