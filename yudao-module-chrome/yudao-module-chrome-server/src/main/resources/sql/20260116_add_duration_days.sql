-- ============================================
-- Chrome订阅套餐表 - 添加订阅时长字段
-- 执行时间: 2026-01-16
-- ============================================

-- 1. 添加订阅时长字段
ALTER TABLE `chrome_subscription_plan` 
ADD COLUMN `duration_days` INT NOT NULL DEFAULT 0 COMMENT '订阅时长（天数，0表示永久有效/不提供时长）' AFTER `credits`;

-- 2. 修改货币默认值为 CNY
ALTER TABLE `chrome_subscription_plan` 
MODIFY COLUMN `currency` VARCHAR(10) NOT NULL DEFAULT 'CNY' COMMENT '货币单位';

-- 3. 免费版：duration_days = 0（永久有效，只靠积分控制）
UPDATE `chrome_subscription_plan` 
SET `duration_days` = 0 
WHERE `subscription_type` = 10;

-- 4. 更新时长套餐数据：月付套餐30天
UPDATE `chrome_subscription_plan` 
SET `duration_days` = 30 
WHERE `billing_cycle` = 10 AND `subscription_type` IN (20, 30);

-- 5. 更新时长套餐数据：年付套餐365天
UPDATE `chrome_subscription_plan` 
SET `duration_days` = 365 
WHERE `billing_cycle` = 20 AND `subscription_type` IN (20, 30);

-- 6. 积分包套餐：duration_days = 0（不提供订阅时长，只充积分）
UPDATE `chrome_subscription_plan` 
SET `duration_days` = 0 
WHERE `subscription_type` = 40;
