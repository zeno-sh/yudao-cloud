-- Add referral fields to chrome_user
ALTER TABLE chrome_user ADD COLUMN referral_code VARCHAR(32) DEFAULT NULL COMMENT '我的推广码';
ALTER TABLE chrome_user ADD COLUMN referrer_user_id BIGINT DEFAULT NULL COMMENT '推荐人用户ID';
ALTER TABLE chrome_user ADD UNIQUE INDEX idx_referral_code (referral_code);
ALTER TABLE chrome_user ADD INDEX idx_referrer_user_id (referrer_user_id);

-- Create commission record table
CREATE TABLE chrome_commission_record (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    referrer_user_id BIGINT NOT NULL COMMENT '推广者用户ID',
    invitee_user_id BIGINT NOT NULL COMMENT '被推广者用户ID',
    order_id BIGINT NOT NULL COMMENT '关联的订阅订单ID',
    order_amount DECIMAL(10, 2) NOT NULL COMMENT '订单原始金额',
    commission_rate DECIMAL(5, 2) NOT NULL COMMENT '佣金比例',
    commission_amount DECIMAL(10, 2) NOT NULL COMMENT '佣金金额',
    status TINYINT NOT NULL DEFAULT 10 COMMENT '状态：10-待结算, 20-已结算, 30-已取消',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted BIT(1) NOT NULL DEFAULT 0 COMMENT '是否删除',
    tenant_id BIGINT NOT NULL DEFAULT 0 COMMENT '租户编号',
    creator VARCHAR(64) DEFAULT '' COMMENT '创建者',
    updater VARCHAR(64) DEFAULT '' COMMENT '更新者',
    PRIMARY KEY (id),
    KEY idx_referrer (referrer_user_id),
    KEY idx_invitee (invitee_user_id)
) COMMENT '推广佣金记录表';
