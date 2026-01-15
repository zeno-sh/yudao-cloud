# Chrome模块订阅积分校验系统

## 概述

本系统实现了Chrome模块的HTTP接口拦截和基于订阅的积分校验功能，确保只有拥有有效订阅和足够积分的用户才能使用付费功能。

## 核心功能

### 1. HTTP接口路径拦截
- 拦截所有 `/plugin-api/chrome/` 路径下的业务接口
- 排除认证、查询等免费接口
- 支持精确匹配和模糊匹配的URL映射

### 2. 订阅状态校验
- 检查用户是否有有效订阅
- 验证订阅是否过期
- 支持免费版、基础版、高级版三种订阅类型

### 3. 积分余额验证
- 每次API调用消耗1积分
- 实时检查用户积分余额
- 积分不足时返回详细错误信息

### 4. 积分消费记录
- 自动记录每次积分消费
- 支持交易类型：充值、消费、赠送、重置
- 完整的交易流水记录

## 系统架构

### 核心组件

#### 1. ChromePermissionInterceptor
**位置**: `framework/security/core/filter/ChromePermissionInterceptor.java`

**功能**:
- Spring Security过滤器链中的拦截器
- 统一处理权限校验逻辑
- 错误处理和日志记录

**拦截流程**:
```
请求进入 -> 检查URL是否需要拦截 -> 获取登录用户 -> 获取功能类型 -> 校验权限 -> 消费积分 -> 记录使用 -> 继续请求
```

#### 2. ChromeInterceptorConfig
**位置**: `framework/security/core/config/ChromeInterceptorConfig.java`

**功能**:
- 统一管理URL映射配置
- 定义排除路径规则
- 支持动态添加/移除映射

**URL映射示例**:
```java
// 评论采集
"/plugin-api/chrome/product-review/query" -> COMMENT_COLLECT

// 销量采集  
"/plugin-api/chrome/product-sales/query" -> SALES_COLLECT

// 趋势分析
"/plugin-api/chrome/trends/category/query" -> TREND_COLLECT
"/plugin-api/chrome/trends/keyword/query" -> TREND_COLLECT
```

#### 3. ChromePermissionService
**位置**: `service/permission/ChromePermissionServiceImpl.java`

**功能**:
- 核心权限校验逻辑
- 订阅状态验证
- 积分余额检查
- 使用记录统计

#### 4. UserCreditsService
**位置**: `service/credits/UserCreditsServiceImpl.java`

**功能**:
- 积分账户管理
- 积分充值/消费
- 积分重置（免费版月重置）
- 交易记录

#### 5. CreditsTransactionService
**位置**: `service/transaction/CreditsTransactionServiceImpl.java`

**功能**:
- 积分交易记录管理
- 支持充值、消费、赠送、重置等交易类型
- 完整的交易流水

## 数据库设计

### 核心表结构

#### 1. chrome_user_credits (用户积分账户表)
```sql
- id: 账户ID
- user_id: 用户ID
- total_credits: 总积分
- used_credits: 已使用积分  
- remaining_credits: 剩余积分
- last_reset_time: 上次重置时间
```

#### 2. chrome_credits_transaction (积分交易记录表)
```sql
- id: 交易ID
- user_id: 用户ID
- transaction_type: 交易类型（10充值 20消费 30赠送 40重置）
- credits_amount: 积分数量
- before_credits: 交易前积分
- after_credits: 交易后积分
- business_type: 业务类型
- business_id: 业务ID
- description: 交易描述
```

#### 3. chrome_usage_record (使用记录表)
```sql
- id: 记录ID
- user_id: 用户ID
- feature_type: 功能类型（10商品采集 20排名采集 30评论采集 40销量采集 50趋势采集 60类目分析）
- usage_date: 使用日期
- usage_count: 使用次数
- credits_consumed: 消耗积分数
```

## 功能类型映射

### FeatureTypeEnum
```java
PRODUCT_COLLECT(10, "商品采集")      // 商品采集相关接口
RANKING_COLLECT(20, "排名采集")      // 排名采集相关接口  
COMMENT_COLLECT(30, "评论采集")      // 评论采集相关接口
SALES_COLLECT(40, "销量采集")        // 销量采集相关接口
TREND_COLLECT(50, "趋势采集")        // 趋势采集相关接口
CATEGORY_ANALYSIS(60, "类目分析")    // 类目分析相关接口
```

## 错误码定义

### 权限相关错误码
```java
CREDITS_INSUFFICIENT(1_030_005_004, "积分不足")
CREDITS_OPERATION_FAILED(1_030_005_005, "积分操作失败") 
SUBSCRIPTION_STATUS_INVALID(1_030_005_006, "订阅状态异常")
```

## 配置说明

### Spring Security配置
**位置**: `framework/security/ChromeSecurityConfiguration.java`

**配置内容**:
- 注册拦截器Bean
- 配置URL权限规则
- 集成到Spring Security过滤器链

### 拦截器配置
**位置**: `framework/security/core/config/ChromeInterceptorConfig.java`

**配置项**:
- URL映射关系
- 排除路径列表
- 动态配置支持

## 使用示例

### 1. 用户访问付费接口
```
POST /plugin-api/chrome/product-review/query
Authorization: Bearer <token>
Content-Type: application/json

{
  "productId": "12345",
  "queryMode": "DEFAULT"
}
```

### 2. 积分充足时的响应
```json
{
  "code": 0,
  "msg": "操作成功",
  "data": {
    // 评论数据
  }
}
```

### 3. 积分不足时的响应
```json
{
  "code": 1030005004,
  "msg": "积分不足，当前剩余积分: 0，请充值后再使用该功能"
}
```

## 扩展说明

### 1. 添加新的付费接口
1. 在 `ChromeInterceptorConfig` 中添加URL映射
2. 确保接口路径符合 `/plugin-api/chrome/` 规范
3. 选择合适的功能类型

### 2. 自定义积分消费规则
1. 修改 `ChromePermissionService.validateFeaturePermission()` 方法
2. 根据不同功能类型设置不同的积分消费量
3. 更新交易记录逻辑

### 3. 添加新的功能类型
1. 在 `FeatureTypeEnum` 中添加新枚举值
2. 确保数据库值唯一
3. 更新URL映射配置

## 监控和日志

### 日志记录
- 拦截器会记录所有权限校验过程
- 积分消费和充值操作都有详细日志
- 异常情况会记录错误日志

### 关键日志示例
```
[ChromePermissionInterceptor][用户(123) 访问Chrome接口(/plugin-api/chrome/product-review/query) 功能类型(COMMENT_COLLECT)]
[validateFeaturePermission][用户(123)功能(COMMENT_COLLECT)权限校验通过，消费积分1]
[consumeCredits][用户(123)消费积分成功，剩余积分: 99]
```

## 注意事项

1. **事务处理**: 积分消费和记录使用了事务保证数据一致性
2. **并发安全**: 积分操作支持并发访问，避免超扣问题
3. **性能优化**: 积分余额查询可考虑加入缓存
4. **错误恢复**: 积分消费失败时会回滚，不影响用户体验
5. **扩展性**: 系统设计支持动态配置，便于后续扩展

## 部署检查清单

- [ ] 数据库表结构已创建
- [ ] 初始订阅套餐数据已导入
- [ ] Spring Security配置已生效
- [ ] 拦截器Bean已注册
- [ ] URL映射配置正确
- [ ] 错误码定义完整
- [ ] 日志级别配置合适
- [ ] 事务配置正确
