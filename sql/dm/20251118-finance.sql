ALTER TABLE `dm_product_costs`
    ADD COLUMN `fbs_currency` int DEFAULT NULL COMMENT '海外仓币种',
ADD COLUMN `fbs_cost_unit` int DEFAULT NULL COMMENT '海外仓单位',
ADD COLUMN `fbs_cost` decimal(10,0) DEFAULT '0' COMMENT '海外仓成本';