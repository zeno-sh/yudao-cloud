-- 币种表增加 status 字段
-- 日期：2026-01-15

-- 1. 增加 status 字段（0-禁用，1-启用）
ALTER TABLE system_currency ADD COLUMN status TINYINT NOT NULL DEFAULT 1 COMMENT '状态（0-禁用，1-启用）' AFTER symbol;

-- 2. 新增缺失的币种数据
INSERT INTO system_currency (currency_code, name, symbol, status, deleted, creator, create_time, update_time) VALUES
('BRL', '巴西雷亚尔', 'R$', 1, b'0', '1', NOW(), NOW()),
('CHF', '瑞士法郎', 'CHF', 1, b'0', '1', NOW(), NOW()),
('CZK', '捷克克朗', 'Kč', 1, b'0', '1', NOW(), NOW()),
('DKK', '丹麦克朗', 'kr', 1, b'0', '1', NOW(), NOW()),
('HKD', '港元', 'HK$', 1, b'0', '1', NOW(), NOW()),
('HUF', '匈牙利福林', 'Ft', 1, b'0', '1', NOW(), NOW()),
('IDR', '印尼盾', 'Rp', 1, b'0', '1', NOW(), NOW()),
('ILS', '以色列新谢克尔', '₪', 1, b'0', '1', NOW(), NOW()),
('INR', '印度卢比', '₹', 1, b'0', '1', NOW(), NOW()),
('ISK', '冰岛克朗', 'kr', 1, b'0', '1', NOW(), NOW()),
('MXN', '墨西哥比索', 'MX$', 1, b'0', '1', NOW(), NOW()),
('MYR', '马来西亚林吉特', 'RM', 1, b'0', '1', NOW(), NOW()),
('NOK', '挪威克朗', 'kr', 1, b'0', '1', NOW(), NOW()),
('NZD', '新西兰元', 'NZ$', 1, b'0', '1', NOW(), NOW()),
('PHP', '菲律宾比索', '₱', 1, b'0', '1', NOW(), NOW()),
('PLN', '波兰兹罗提', 'zł', 1, b'0', '1', NOW(), NOW()),
('RON', '罗马尼亚列伊', 'lei', 1, b'0', '1', NOW(), NOW()),
('SEK', '瑞典克朗', 'kr', 1, b'0', '1', NOW(), NOW()),
('SGD', '新加坡元', 'S$', 1, b'0', '1', NOW(), NOW()),
('THB', '泰铢', '฿', 1, b'0', '1', NOW(), NOW()),
('TRY', '土耳其里拉', '₺', 1, b'0', '1', NOW(), NOW()),
('ZAR', '南非兰特', 'R', 1, b'0', '1', NOW(), NOW());
