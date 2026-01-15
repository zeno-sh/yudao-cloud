# Chrome支付功能使用说明

参考：`PayDemoOrderController` 和 `PayOrderController` 的设计

## 完整支付流程

### 1. 创建Chrome订单
```http
POST /api/chrome/payment/create
Content-Type: application/json

{
  "email": "user@example.com",
  "planId": 1,
  "channelCode": "alipay_wap"
}

响应: { "code": 0, "data": 123 }  // 返回orderId
```

### 2. 获取订单详情（含payOrderId）
```http
GET /api/chrome/payment/get?orderId=123

响应: {
  "code": 0,
  "data": {
    "orderId": 123,
    "orderNo": "CHR1234567890",
    "payOrderId": 456,  // ⭐用于下一步提交支付
    "actualPrice": 9.99,
    "currency": "USD",
    "paymentStatus": 10,
    "expireTime": "2024-01-01T12:00:00"
  }
}
```

### 3. 提交支付（调用chrome模块）
```http
POST /api/chrome/payment/submit
Content-Type: application/json

{
  "id": 456,  // 使用上一步得到的payOrderId
  "channelCode": "alipay_wap",
  "displayMode": "url",
  "returnUrl": "https://your-domain.com/payment/return"
}

响应: {
  "code": 0,
  "data": {
    "status": 10,
    "displayMode": "url",
    "displayContent": "https://openapi.alipay.com/gateway.do?..."
  }
}
```

### 4. 跳转支付
```javascript
// 前端跳转到支付URL
window.location.href = displayContent;
```

### 5. 支付回调（自动）
用户完成支付后，支付平台会回调pay模块，pay模块会自动调用：
```http
POST /api/chrome/payment/notify-paid
```
Chrome模块会自动处理订单状态更新和积分充值。

### 6. 前端轮询检查支付状态
```http
GET /api/chrome/payment/check-status?orderId=123

响应: { "code": 0, "data": true }  // true表示已支付
```

## 前端示例代码

```javascript
async function handlePayment(email, planId) {
  // Step 1: 创建订单
  const createRes = await fetch('/api/chrome/payment/create', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email, planId, channelCode: 'alipay_wap' })
  });
  const orderId = (await createRes.json()).data;
  
  // Step 2: 获取订单详情
  const orderRes = await fetch(`/api/chrome/payment/get?orderId=${orderId}`);
  const order = (await orderRes.json()).data;
  const payOrderId = order.payOrderId;
  
  // Step 3: 提交支付
  const submitRes = await fetch('/api/chrome/payment/submit', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      id: payOrderId,
      channelCode: 'alipay_wap',
      displayMode: 'url',
      returnUrl: window.location.origin + '/payment/result'
    })
  });
  const payInfo = (await submitRes.json()).data;
  
  // Step 4: 跳转支付
  window.location.href = payInfo.displayContent;
}
```

## API列表

| 接口 | 方法 | 说明 | 需要登录 |
|-----|------|------|---------|
| `/chrome/payment/create` | POST | 创建支付订单 | ❌ |
| `/chrome/payment/get` | GET | 获取订单详情 | ❌ |
| `/chrome/payment/submit` | POST | 提交支付 | ❌ |
| `/chrome/payment/check-status` | GET | 检查支付状态 | ❌ |
| `/chrome/payment/cancel` | PUT | 取消订单 | ❌ |
| `/chrome/payment/notify-paid` | POST | 支付回调 | ❌ |

## 设计说明

参考pay模块的app端设计（`AppPayOrderController`）：
- Chrome模块是独立的plugin-api路径，类似app端，需要单独实现submit接口
- `ChromePaymentController.createPaymentOrder()` - 创建订单并返回订单ID
- `ChromePaymentController.submitPayOrder()` - 提交支付获取支付URL（内部调用PayOrderService）
- Chrome模块所有接口都无需登录，通过邮箱识别用户，适配插件端场景
