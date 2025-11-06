-- ========================================
-- 组合产品功能 - 数据库变更脚本
-- 执行日期：2025-10-30
-- ========================================

-- ========== 步骤1：修改现有表 dm_product_info（只需2个字段）==========
ALTER TABLE `dm_product_info`
    ADD COLUMN `product_type` int DEFAULT 0 COMMENT '产品类型：0=普通产品 1=组合产品',
ADD COLUMN `bundle_type` int DEFAULT NULL COMMENT '组合类型：1=自定义成本价 2=自动累计成本价（仅组合产品有效）',
ADD INDEX `idx_product_type` (`product_type`, `deleted`);

-- 说明：
-- product_type = 0: 普通产品（默认）
-- product_type = 1: 组合产品
-- bundle_type = 1: 自定义成本价（cost_price由用户手动设置）
-- bundle_type = 2: 自动累计成本价（cost_price自动计算）
-- cost_price 字段：【复用原有字段】存储最终成本价

-- ========== 步骤2：新增关联表 dm_product_bundle_relation（精简版）==========
CREATE TABLE `dm_product_bundle_relation`
(
    `id`                bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `bundle_product_id` bigint NOT NULL COMMENT '组合产品ID（dm_product_info.id）',
    `sub_product_id`    bigint NOT NULL COMMENT '子产品ID（dm_product_info.id）',
    `quantity`          int    NOT NULL                    DEFAULT '1' COMMENT '数量',
    `sort_order`        int                                DEFAULT '0' COMMENT '排序',
    `remark`            varchar(500) CHARACTER SET utf8mb3 DEFAULT NULL COMMENT '备注',
    `creator`           varchar(64) CHARACTER SET utf8mb3  DEFAULT NULL COMMENT '创建人',
    `create_time`       datetime                           DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater`           varchar(64) CHARACTER SET utf8mb3  DEFAULT NULL COMMENT '更新人',
    `update_time`       datetime                           DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`           bit(1) NOT NULL                    DEFAULT b'0' COMMENT '是否删除',
    `tenant_id`         bigint                             DEFAULT '0' COMMENT '租户ID',
    PRIMARY KEY (`id`),
    KEY                 `idx_bundle_product` (`bundle_product_id`,`deleted`),
    KEY                 `idx_sub_product` (`sub_product_id`),
    KEY                 `idx_tenant` (`tenant_id`,`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='产品组合关系表';


