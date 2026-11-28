package org.example.campusclubmanagement.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.example.campusclubmanagement.entity.Department;
import org.example.campusclubmanagement.exception.BusinessException;
import org.example.campusclubmanagement.mapper.DepartmentMapper;
import org.example.campusclubmanagement.service.DepartmentService;
import org.example.campusclubmanagement.service.PermissionChecker;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 部门服务实现
 */
@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentMapper departmentMapper;
    private final PermissionChecker permissionChecker;

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
        permissionChecker.checkClubPresident(department.getClubId());
        departmentMapper.insert(department);
    }

    @Override
    public void update(Department department) {
        if (department.getId() == null) {
            throw new BusinessException("部门ID不能为空");
        }
        Department existing = departmentMapper.selectById(department.getId());
        if (existing == null) {
            throw new BusinessException("部门不存在");
        }
        permissionChecker.checkClubPresident(existing.getClubId());
        // 部门归属不可通过更新接口变更
        department.setClubId(null);
        departmentMapper.updateById(department);
    }

    @Override
    public void delete(Long id) {
        Department existing = departmentMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException("部门不存在");
        }
        permissionChecker.checkClubPresident(existing.getClubId());
        departmentMapper.deleteById(id);
    }
}
