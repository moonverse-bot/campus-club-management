-- =============================================
-- 大学生社团管理系统 - 数据库初始化脚本
-- =============================================

CREATE DATABASE IF NOT EXISTS campus_club DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE campus_club;

-- =============================================
-- 1. 用户表
-- =============================================
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名/学号',
    `password` VARCHAR(100) NOT NULL COMMENT '密码',
    `nickname` VARCHAR(50) DEFAULT NULL COMMENT '昵称',
    `real_name` VARCHAR(50) DEFAULT NULL COMMENT '真实姓名',
    `gender` TINYINT DEFAULT 0 COMMENT '性别：0-未知，1-男，2-女',
    `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `college` VARCHAR(100) DEFAULT NULL COMMENT '学院',
    `major` VARCHAR(100) DEFAULT NULL COMMENT '专业',
    `grade` VARCHAR(20) DEFAULT NULL COMMENT '年级',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-正常',
    `last_login_time` DATETIME DEFAULT NULL COMMENT '最后登录时间',
    `last_active_time` DATETIME DEFAULT NULL COMMENT '最后在线活动时间',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- =============================================
-- 2. 角色表
-- =============================================
DROP TABLE IF EXISTS `role`;
CREATE TABLE `role` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `role_name` VARCHAR(50) NOT NULL COMMENT '角色名称',
    `role_code` VARCHAR(50) NOT NULL COMMENT '角色编码',
    `description` VARCHAR(255) DEFAULT NULL COMMENT '角色描述',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_code` (`role_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- =============================================
-- 3. 用户角色关联表
-- =============================================
DROP TABLE IF EXISTS `user_role`;
CREATE TABLE `user_role` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `role_id` BIGINT NOT NULL COMMENT '角色ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- =============================================
-- 4. 社团表
-- =============================================
DROP TABLE IF EXISTS `club`;
CREATE TABLE `club` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `club_name` VARCHAR(100) NOT NULL COMMENT '社团名称',
    `club_code` VARCHAR(50) DEFAULT NULL COMMENT '社团编码',
    `description` TEXT DEFAULT NULL COMMENT '社团简介',
    `logo` VARCHAR(255) DEFAULT NULL COMMENT '社团Logo',
    `category` VARCHAR(50) DEFAULT NULL COMMENT '社团类别（学术科技、文化艺术、体育竞技、公益志愿等）',
    `advisor` VARCHAR(50) DEFAULT NULL COMMENT '指导老师',
    `president_id` BIGINT DEFAULT NULL COMMENT '社长用户ID',
    `member_count` INT DEFAULT 0 COMMENT '成员数量',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-注销，1-正常，2-待审核',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_club_name` (`club_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='社团表';

-- =============================================
-- 5. 部门表
-- =============================================
DROP TABLE IF EXISTS `department`;
CREATE TABLE `department` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `club_id` BIGINT NOT NULL COMMENT '所属社团ID',
    `dept_name` VARCHAR(50) NOT NULL COMMENT '部门名称',
    `description` VARCHAR(255) DEFAULT NULL COMMENT '部门描述',
    `parent_id` BIGINT DEFAULT 0 COMMENT '父部门ID，0表示顶级',
    `sort_order` INT DEFAULT 0 COMMENT '排序',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    KEY `idx_club_id` (`club_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='部门表';

-- =============================================
-- 6. 社团成员表
-- =============================================
DROP TABLE IF EXISTS `club_member`;
CREATE TABLE `club_member` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `club_id` BIGINT NOT NULL COMMENT '社团ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `department_id` BIGINT DEFAULT NULL COMMENT '部门ID',
    `position` VARCHAR(50) DEFAULT 'member' COMMENT '职位：president-社长，vice_president-副社长，minister-部长，vice_minister-副部长，member-成员',
    `join_date` DATE DEFAULT NULL COMMENT '加入日期',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-已退出，1-在社，2-待审核',
    `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    KEY `idx_club_id` (`club_id`),
    KEY `idx_user_id` (`user_id`),
    UNIQUE KEY `uk_club_user` (`club_id`, `user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='社团成员表';

-- =============================================
-- 7. 部长卸任推荐表
-- =============================================
DROP TABLE IF EXISTS `minister_succession`;
CREATE TABLE `minister_succession` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `club_id` BIGINT NOT NULL COMMENT '社团ID',
    `department_id` BIGINT DEFAULT NULL COMMENT '原部长所属部门ID',
    `minister_id` BIGINT NOT NULL COMMENT '原部长用户ID',
    `candidate_id` BIGINT NOT NULL COMMENT '推荐接任成员用户ID',
    `reason` VARCHAR(500) DEFAULT NULL COMMENT '卸任及推荐理由',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0-待审批，1-已通过，2-已拒绝',
    `reviewer_id` BIGINT DEFAULT NULL COMMENT '审批人用户ID',
    `review_remark` VARCHAR(500) DEFAULT NULL COMMENT '审批意见',
    `review_time` DATETIME DEFAULT NULL COMMENT '审批时间',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    KEY `idx_club_status` (`club_id`, `status`),
    KEY `idx_minister_id` (`minister_id`),
    KEY `idx_candidate_id` (`candidate_id`),
    KEY `idx_reviewer_id` (`reviewer_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='部长卸任推荐表';

-- =============================================
-- 8. 活动表
-- =============================================
DROP TABLE IF EXISTS `activity`;
CREATE TABLE `activity` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `club_id` BIGINT NOT NULL COMMENT '负责社团ID',
    `title` VARCHAR(200) NOT NULL COMMENT '活动标题',
    `content` TEXT DEFAULT NULL COMMENT '活动内容',
    `cover_image` VARCHAR(255) DEFAULT NULL COMMENT '封面图片',
    `location` VARCHAR(200) DEFAULT NULL COMMENT '活动地点',
    `start_time` DATETIME DEFAULT NULL COMMENT '开始时间',
    `end_time` DATETIME DEFAULT NULL COMMENT '结束时间',
    `max_participants` INT DEFAULT 0 COMMENT '最大参与人数，0表示不限',
    `current_participants` INT DEFAULT 0 COMMENT '当前参与人数',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-已结束，1-进行中，2-未开始，3-已取消',
    `organizer_id` BIGINT DEFAULT NULL COMMENT '组织者用户ID',
    `approval_status` TINYINT NOT NULL DEFAULT 1 COMMENT '审批状态：0-待审批，1-已通过，2-已拒绝',
    `reviewer_id` BIGINT DEFAULT NULL COMMENT '活动审批人用户ID',
    `review_time` DATETIME DEFAULT NULL COMMENT '活动审批时间',
    `review_remark` VARCHAR(500) DEFAULT NULL COMMENT '活动审批意见',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    KEY `idx_club_id` (`club_id`),
    KEY `idx_approval_status` (`approval_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='活动表';

-- =============================================
-- 9. 活动报名表
-- =============================================
DROP TABLE IF EXISTS `activity_registration`;
CREATE TABLE `activity_registration` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `activity_id` BIGINT NOT NULL COMMENT '活动ID',
    `user_id` BIGINT NOT NULL COMMENT '报名用户ID',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0-待审核，1-已通过，2-已拒绝',
    `reviewer_id` BIGINT DEFAULT NULL COMMENT '审核人用户ID',
    `review_time` DATETIME DEFAULT NULL COMMENT '审核时间',
    `review_remark` VARCHAR(500) DEFAULT NULL COMMENT '审核意见',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_activity_user` (`activity_id`, `user_id`),
    KEY `idx_activity_status` (`activity_id`, `status`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_reviewer_id` (`reviewer_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='活动报名表';

-- =============================================
-- 10. 活动经费申请表
-- =============================================
DROP TABLE IF EXISTS `activity_funding_application`;
CREATE TABLE `activity_funding_application` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `activity_id` BIGINT NOT NULL COMMENT '活动ID',
    `club_id` BIGINT NOT NULL COMMENT '申请社团ID',
    `applicant_id` BIGINT NOT NULL COMMENT '申请部长用户ID',
    `amount` DECIMAL(12,2) NOT NULL COMMENT '申请金额',
    `reason` VARCHAR(500) NOT NULL COMMENT '经费用途说明',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_activity_id` (`activity_id`),
    KEY `idx_club_id` (`club_id`),
    KEY `idx_applicant_id` (`applicant_id`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='活动经费申请表';

-- =============================================
-- 11. 公告表
-- =============================================
DROP TABLE IF EXISTS `announcement`;
CREATE TABLE `announcement` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `club_id` BIGINT DEFAULT NULL COMMENT '社团ID，NULL表示系统公告',
    `title` VARCHAR(200) NOT NULL COMMENT '公告标题',
    `content` TEXT DEFAULT NULL COMMENT '公告内容',
    `is_top` TINYINT DEFAULT 0 COMMENT '是否置顶：0-否，1-是',
    `publisher_id` BIGINT DEFAULT NULL COMMENT '发布者ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    KEY `idx_club_id` (`club_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公告表';

-- =============================================
-- 10. 动态表
-- =============================================
DROP TABLE IF EXISTS `dynamic`;
CREATE TABLE `dynamic` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `club_id` BIGINT DEFAULT NULL COMMENT '社团ID',
    `user_id` BIGINT NOT NULL COMMENT '发布者用户ID',
    `content` TEXT DEFAULT NULL COMMENT '动态内容',
    `images` VARCHAR(1000) DEFAULT NULL COMMENT '图片URL列表，逗号分隔',
    `like_count` INT DEFAULT 0 COMMENT '点赞数',
    `comment_count` INT DEFAULT 0 COMMENT '评论数',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    KEY `idx_club_id` (`club_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='动态表';
