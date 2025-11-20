-- 为dm_product_costs表添加currencyCode字段
ALTER TABLE `dm_product_costs`
    ADD COLUMN `purchase_currency_code` varchar(10) DEFAULT 'CNY' COMMENT '采购成本币种代码' AFTER `purchase_currency`,
    ADD COLUMN `logistics_currency_code` varchar(10) DEFAULT 'CNY' COMMENT '头程成本币种代码' AFTER `logistics_currency`,
    ADD COLUMN `customs_currency_code` varchar(10) DEFAULT 'CNY' COMMENT '海关申报价币种代码' AFTER `customs_currency`,
    ADD COLUMN `fbo_currency_code` varchar(10) DEFAULT 'CNY' COMMENT 'FBO送仓费币种代码' AFTER `fbo_currency`,
    ADD COLUMN `platform_currency_code` varchar(10) DEFAULT 'CNY' COMMENT '平台成本币种代码' AFTER `platform_currency`,
    ADD COLUMN `fbs_currency_code` varchar(10) DEFAULT 'CNY' COMMENT '海外仓币种代码' AFTER `fbs_currency`;

-- 数据迁移：将int类型的currency值转换为String类型的currencyCode
-- 映射关系：10=CNY, 20=RUB, 30=USD, 40=KRW

-- 迁移采购成本币种
UPDATE `dm_product_costs`
SET `purchase_currency_code` = CASE `purchase_currency`
    WHEN 10 THEN 'CNY'
    WHEN 20 THEN 'RUB'
    WHEN 30 THEN 'USD'
    WHEN 40 THEN 'KRW'
    ELSE NULL
END
WHERE `purchase_currency` IS NOT NULL;

-- 迁移头程成本币种
UPDATE `dm_product_costs`
SET `logistics_currency_code` = CASE `logistics_currency`
    WHEN 10 THEN 'CNY'
    WHEN 20 THEN 'RUB'
    WHEN 30 THEN 'USD'
    WHEN 40 THEN 'KRW'
    ELSE NULL
END
WHERE `logistics_currency` IS NOT NULL;

-- 迁移海关申报价币种
UPDATE `dm_product_costs`
SET `customs_currency_code` = CASE `customs_currency`
    WHEN 10 THEN 'CNY'
    WHEN 20 THEN 'RUB'
    WHEN 30 THEN 'USD'
    WHEN 40 THEN 'KRW'
    ELSE NULL
END
WHERE `customs_currency` IS NOT NULL;

-- 迁移FBO送仓费币种
UPDATE `dm_product_costs`
SET `fbo_currency_code` = CASE `fbo_currency`
    WHEN 10 THEN 'CNY'
    WHEN 20 THEN 'RUB'
    WHEN 30 THEN 'USD'
    WHEN 40 THEN 'KRW'
    ELSE NULL
END
WHERE `fbo_currency` IS NOT NULL;

-- 迁移平台成本币种
UPDATE `dm_product_costs`
SET `platform_currency_code` = CASE `platform_currency`
    WHEN 10 THEN 'CNY'
    WHEN 20 THEN 'RUB'
    WHEN 30 THEN 'USD'
    WHEN 40 THEN 'KRW'
    ELSE NULL
END
WHERE `platform_currency` IS NOT NULL;

-- 迁移海外仓币种
UPDATE `dm_product_costs`
SET `fbs_currency_code` = CASE `fbs_currency`
    WHEN 10 THEN 'CNY'
    WHEN 20 THEN 'RUB'
    WHEN 30 THEN 'USD'
    WHEN 40 THEN 'KRW'
    ELSE NULL
END
WHERE `fbs_currency` IS NOT NULL;
