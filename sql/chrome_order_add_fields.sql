-- 添加订单表字段
ALTER TABLE chrome_subscription_order ADD COLUMN duration_days INT COMMENT '订阅时长（天数）' AFTER expire_time;
ALTER TABLE chrome_subscription_order ADD COLUMN remark VARCHAR(255) COMMENT '备注（如：推广赠送、首次订阅、续费等）' AFTER duration_days;
