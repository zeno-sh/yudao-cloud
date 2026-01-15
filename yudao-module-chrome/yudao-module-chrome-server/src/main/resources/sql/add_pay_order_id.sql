-- 为chrome_subscription_order表添加pay_order_id字段
-- 用于存储pay模块的支付订单ID

ALTER TABLE `chrome_subscription_order`
ADD COLUMN `pay_order_id` BIGINT(20) NULL COMMENT '支付订单ID（pay模块）' AFTER `order_no`;

-- 添加索引，提高查询性能
CREATE INDEX `idx_pay_order_id` ON `chrome_subscription_order` (`pay_order_id`);
