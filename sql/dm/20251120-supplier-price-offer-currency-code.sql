-- 为dm_supplier_price_offer表添加currencyCode字段
ALTER TABLE `dm_supplier_price_offer`
    ADD COLUMN `currency_code` varchar(10) DEFAULT 'CNY' COMMENT '币种代码' AFTER `currency`;

-- 数据迁移：将int类型的currency值转换为String类型的currencyCode
-- 映射关系：10=CNY, 20=RUB, 30=USD, 40=KRW

-- 迁移币种代码
UPDATE `dm_supplier_price_offer`
SET `currency_code` = CASE `currency`
    WHEN 10 THEN 'CNY'
    WHEN 20 THEN 'RUB'
    WHEN 30 THEN 'USD'
    WHEN 40 THEN 'KRW'
    ELSE 'CNY'
END
WHERE `currency` IS NOT NULL;
