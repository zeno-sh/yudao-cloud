-- 为dm_profit_calculation表添加韩国税务相关字段
ALTER TABLE `dm_profit_calculation`
    ADD COLUMN `sale_rate` decimal(10, 2) DEFAULT NULL COMMENT '销售税率(%)（韩国等国家）' AFTER `vat_rate`,
    ADD COLUMN `sale_cost` decimal(10, 2) DEFAULT NULL COMMENT '销售税费用（韩国等国家）' AFTER `vat_cost`,
    ADD COLUMN `actual_tax_amount` decimal(10, 2) DEFAULT NULL COMMENT '应纳税额（韩国：销项税-进项税）' AFTER `sale_cost`;
