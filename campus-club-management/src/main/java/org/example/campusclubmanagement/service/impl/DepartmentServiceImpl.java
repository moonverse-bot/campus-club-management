package org.example.campusclubmanagement.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.example.campusclubmanagement.entity.Department;
import org.example.campusclubmanagement.exception.BusinessException;
import org.example.campusclubmanagement.mapper.DepartmentMapper;
import org.example.campusclubmanagement.service.DepartmentService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 部门服务实现
 */
@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentMapper departmentMapper;

    @Override
    public List<Department> listByClubId(Long clubId) {
        return departmentMapper.selectList(new LambdaQueryWrapper<Department>()
                .eq(Department::getClubId, clubId)
                .orderByAsc(Department::getSortOrder));
    }

    @Override
    public Department getById(Long id) {
        Department department = departmentMapper.selectById(id);
        if (department == null) {
            throw new BusinessException("部门不存在");
        }
        return department;
    }

    @Override
    public void add(Department department) {
        departmentMapper.insert(department);
    }

    @Override
    public void update(Department department) {
        departmentMapper.updateById(department);
    }

    @Override
    public void delete(Long id) {
        departmentMapper.deleteById(id);
    }
}
