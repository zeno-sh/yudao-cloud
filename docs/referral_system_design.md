# 推广分销体系技术方案设计

## 1. 需求背景
在现有的Chrome插件用户体系基础上，增加推广分销功能。
- **推广者（所有用户）**：申请推广码 -> 分享 -> 获得佣金。
- **被推广者（新用户）**：使用推广码注册 -> **付费成功后** -> 获得15天免费套餐（或VIP特权）。
- **佣金结算**：被推广者付费后，推广者获得相应比例的佣金。

## 2. 核心流程设计

### 2.1 推广码申请
1.  用户（**所有注册用户**，无论是否付费）在个人中心申请推广码。
2.  系统生成唯一推广码（如: 6位字母数字组合），绑定到用户账号。

### 2.2 推广注册（绑定关系）
1.  新用户注册时，填写推广码（可选）。
2.  系统校验推广码有效性。
3.  注册成功时，记录`referrer_user_id`（推荐人ID）到用户信息中。
4.  **注意**：注册阶段仅绑定关系，**不发放**任何奖励。

### 2.3 佣金结算与被推广者奖励
1.  被推广者发起订阅支付。
2.  支付成功回调（`PayNotify` 或 `SubscriptionOrderService`）触发。
3.  检查支付用户是否存在有效的 `referrer_user_id`。
4.  **被推广者奖励**：
    *   判断是否为**首次付费**（或根据业务规则每次付费都送）。通常仅首单赠送。
    *   **赠送方式**：系统通过代码创建一个 **0元的订阅订单**。
    *   订单内容：15天时长，赠送类型，金额0。
    *   效果：用户的会员时长自动延长15天。
5.  **推广者奖励**：
    *   计算佣金金额（订单金额 * 分成比例，如 10%）。
    *   记录佣金流水（`CommissionRecord`）。
    *   （可选）更新推广者的钱包余额。

## 3. 数据库设计

### 3.1 用户表扩展 (`chrome_user`)
需要增加字段以支持推广关系。

```sql
ALTER TABLE chrome_user ADD COLUMN referral_code VARCHAR(32) COMMENT '我的推广码（唯一索引）';
ALTER TABLE chrome_user ADD COLUMN referrer_user_id BIGINT COMMENT '推荐人用户ID';
```

### 3.2 佣金记录表 (`chrome_commission_record`)
用于记录每一笔推广带来的收益。

```sql
CREATE TABLE chrome_commission_record (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    referrer_user_id BIGINT NOT NULL COMMENT '推广者用户ID',
    invitee_user_id BIGINT NOT NULL COMMENT '被推广者用户ID',
    order_id BIGINT NOT NULL COMMENT '关联的订阅订单ID',
    order_amount DECIMAL(10, 2) NOT NULL COMMENT '订单原始金额',
    commission_rate DECIMAL(5, 2) NOT NULL COMMENT '佣金比例（如0.10表示10%）',
    commission_amount DECIMAL(10, 2) NOT NULL COMMENT '佣金金额',
    status TINYINT NOT NULL DEFAULT 10 COMMENT '状态：10-待结算, 20-已结算, 30-已取消',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted BIT(1) NOT NULL DEFAULT 0 COMMENT '是否删除',
    PRIMARY KEY (id),
    KEY idx_referrer (referrer_user_id),
    KEY idx_invitee (invitee_user_id)
) COMMENT '推广佣金记录表';
```

## 4. 接口设计 (API)

### 4.1 推广管理接口 (`ReferralController`)
*   `POST /chrome/referral/apply`
    *   **功能**：申请开通推广权限。
    *   **逻辑**：生成唯一Code -> update `chrome_user`（取消付费资格检查）。
*   `GET /chrome/referral/info`
    *   **功能**：获取我的推广信息（推广码、累计邀请人数、累计收益）。
*   `GET /chrome/referral/records`
    *   **功能**：分页查询推广佣金记录。

### 4.2 认证接口变更 (`AuthController`)
*   `POST /chrome/auth/register`
    *   **变更**：ReqVO增加 `referralCode` 字段（可选）。
    *   **逻辑**：Service层校验Code有效性 -> 设置 `referrerUserId`。

## 5. 业务逻辑实现细节

### 5.1 注册逻辑 (`ChromeAuthServiceImpl.register`)
```java
// 伪代码
if (reqVO.getReferralCode() != null) {
    UserDO referrer = userService.getUserByReferralCode(reqVO.getReferralCode());
    if (referrer != null) {
        newUser.setReferrerUserId(referrer.getId());
        // 仅绑定关系，不发放奖励
    }
}
```

### 5.2 支付成功逻辑 (`SubscriptionOrderServiceImpl.processOrderPayment`)
```java
// 在支付成功处理逻辑中 (order.getPaymentStatus() == 20)
// 调用独立的推广处理 Service，解耦主逻辑
referralService.processPaySuccess(order);

// --- ReferralServiceImpl.processPaySuccess ---
UserDO user = userService.getUser(order.getUserId());
if (user.getReferrerUserId() == null) {
    return;
}

// 1. 发放被推广者奖励（判断是否首单）
boolean isFirstOrder = subscriptionOrderService.countPaidOrders(user.getId()) == 1; // 需注意即时订单状态
if (isFirstOrder) { // 或者根据需求每次都送
    // 创建0元订单赠送15天
    SubscriptionOrderSaveReqVO freeOrder = new SubscriptionOrderSaveReqVO();
    freeOrder.setUserId(user.getId());
    freeOrder.setPlanId(null); // 或指定一个赠送专用Plan
    freeOrder.setSubscriptionType(user.getSubscriptionType()); // 同类型赠送
    freeOrder.setBillingCycle(BillingCycleEnum.ONE_TIME.getCode()); // 或特殊周期
    freeOrder.setPaymentStatus(20);
    freeOrder.setActualPrice(BigDecimal.ZERO);
    // ... 设置时长15天
    // 调用 createSubscriptionOrder 会自动触发 upgradeSubscription 增加时长
    subscriptionOrderService.createFreeDurationOrder(user.getId(), 15);
}

// 2. 发放推广者佣金
BigDecimal rate = new BigDecimal("0.10"); // 可配置
BigDecimal commission = order.getActualPrice().multiply(rate);

CommissionRecordDO record = new CommissionRecordDO();
record.setReferrerUserId(user.getReferrerUserId());
record.setInviteeUserId(user.getId());
record.setOrderId(order.getId());
record.setAmount(commission);
record.setStatus(CommissionStatusEnum.SETTLED.getCode());
commissionRecordMapper.insert(record);

// 3. 更新余额
walletService.addBalance(user.getReferrerUserId(), commission);
```
