# 组合产品功能测试指南

## 一、数据库初始化

### 1. 执行数据库变更脚本

```bash
# 连接到你的 MySQL 数据库
mysql -u username -p database_name < /path/to/bundle_product_init.sql
```

或者手动执行 `sql/dm/bundle_product_init.sql` 中的 SQL 语句。

### 2. 验证表结构

```sql
-- 查看 dm_product_info 表是否新增了 2 个字段
DESC dm_product_info;
-- 应该能看到 product_type 和 bundle_type 字段

-- 查看新建的关系表
DESC dm_product_bundle_relation;
```

## 二、准备测试数据

### 1. 准备子产品（普通产品）

```sql
-- 假设你已经有一些普通产品，或者手动插入测试数据
-- 示例：插入两个子产品
INSERT INTO dm_product_info (
  tenant_id, sku_id, sku_name, model_number, unit, 
  sale_status, cost_price, product_type, creator
) VALUES 
  (1, 'SUB-001', '牛排机', 'SUB-001', '个', 10, 118.00, 0, 'admin'),
  (1, 'SUB-002', '叠叠锅', 'SUB-002', '个', 10, 159.00, 0, 'admin');

-- 记录这两个产品的 ID，稍后会用到
SELECT id, sku_id, sku_name, cost_price FROM dm_product_info WHERE sku_id IN ('SUB-001', 'SUB-002');
```

## 三、API测试

### 1. 创建组合产品（自定义成本价）

**请求示例：**

```http
POST /admin-api/dm/product-info/create
Content-Type: application/json

{
  "skuId": "COMBO-001",
  "skuName": "锅具3件套（自定义成本价）",
  "modelNumber": "COMBO-001",
  "unit": "套",
  "saleStatus": 10,
  "productType": 1,
  "bundleType": 1,
  "costPrice": 350.00,
  "bundleItems": [
    {
      "subProductId": 5,
      "quantity": 2,
      "sortOrder": 1
    },
    {
      "subProductId": 7,
      "quantity": 1,
      "sortOrder": 2
    }
  ]
}
```

**验证点：**
- 返回的产品 ID 不为空
- 成本价应该是 **350.00**（用户自定义）
- 数据库中应该有 2 条 `dm_product_bundle_relation` 记录

### 2. 创建组合产品（自动累计成本价）

**请求示例：**

```http
POST /admin-api/dm/product-info/create
Content-Type: application/json

{
  "skuId": "COMBO-002",
  "skuName": "锅具3件套（自动累计成本价）",
  "modelNumber": "COMBO-002",
  "unit": "套",
  "saleStatus": 10,
  "productType": 1,
  "bundleType": 2,
  "bundleItems": [
    {
      "subProductId": 5,
      "quantity": 2,
      "sortOrder": 1
    },
    {
      "subProductId": 7,
      "quantity": 1,
      "sortOrder": 2
    }
  ]
}
```

**验证点：**
- 返回的产品 ID 不为空
- 成本价应该自动计算：**118*2 + 159*1 = 395.00**
- 数据库中应该有 2 条 `dm_product_bundle_relation` 记录

**验证 SQL：**

```sql
-- 查看组合产品信息
SELECT id, sku_id, sku_name, product_type, bundle_type, cost_price 
FROM dm_product_info 
WHERE product_type = 1;

-- 查看组合关系
SELECT * FROM dm_product_bundle_relation WHERE bundle_product_id = ?;
```

### 3. 查询组合产品明细

**请求示例：**

```http
GET /admin-api/dm/product-info/bundle-relations?bundleProductId=100
```

**验证点：**
- 返回的明细列表包含子产品信息（SKU、名称、单价）
- 总成本价 = 单位成本价 * 数量
- 数据是实时从 `dm_product_info` 表 JOIN 获取的

### 4. 更新组合产品

**请求示例：**

```http
PUT /admin-api/dm/product-info/update
Content-Type: application/json

{
  "id": 100,
  "skuId": "COMBO-001",
  "skuName": "锅具5件套（更新）",
  "modelNumber": "COMBO-001",
  "unit": "套",
  "saleStatus": 10,
  "productType": 1,
  "bundleType": 2,
  "bundleItems": [
    {
      "subProductId": 5,
      "quantity": 3,
      "sortOrder": 1
    },
    {
      "subProductId": 7,
      "quantity": 2,
      "sortOrder": 2
    }
  ]
}
```

**验证点：**
- 成本价更新为：**118*3 + 159*2 = 672.00**
- 旧的关系记录被删除，新的关系记录被插入

### 5. 重新计算成本价（仅自动累计模式）

**场景：** 当子产品成本价发生变化时，需要重新计算组合产品成本价。

**步骤1：修改子产品成本价**

```sql
-- 将牛排机成本价从 118 改为 130
UPDATE dm_product_info SET cost_price = 130.00 WHERE id = 5;
```

**步骤2：调用重新计算接口**

```http
POST /admin-api/dm/product-info/recalculate-bundle-cost?bundleProductId=101
```

**验证点：**
- 自动累计模式的组合产品成本价会更新
- 自定义成本价模式的组合产品不会更新

**验证 SQL：**

```sql
SELECT id, sku_id, bundle_type, cost_price 
FROM dm_product_info 
WHERE id = 101;

-- 成本价应该更新为：130*3 + 159*2 = 708.00
```

### 6. 批量重新计算所有组合产品成本价

**请求示例：**

```http
POST /admin-api/dm/product-info/batch-recalculate-bundle-cost
```

**验证点：**
- 所有 `bundle_type=2`（自动累计）的组合产品成本价都会重新计算
- 可以通过日志查看处理进度

**验证 SQL：**

```sql
-- 查看所有自动累计模式的组合产品
SELECT id, sku_id, bundle_type, cost_price 
FROM dm_product_info 
WHERE product_type = 1 AND bundle_type = 2;
```

## 四、边界测试

### 1. 测试组合产品嵌套（应该被拒绝）

```http
POST /admin-api/dm/product-info/create
Content-Type: application/json

{
  "skuId": "COMBO-003",
  "skuName": "二级组合产品",
  "modelNumber": "COMBO-003",
  "productType": 1,
  "bundleType": 2,
  "bundleItems": [
    {
      "subProductId": 100,
      "quantity": 1
    }
  ]
}
```

**验证点：**
- 应该返回错误码 `2_001_001_104`
- 错误消息：`组合产品不能嵌套，子产品不能是组合产品`

### 2. 测试子产品不存在

```http
POST /admin-api/dm/product-info/create
Content-Type: application/json

{
  "skuId": "COMBO-004",
  "skuName": "测试不存在子产品",
  "modelNumber": "COMBO-004",
  "productType": 1,
  "bundleType": 2,
  "bundleItems": [
    {
      "subProductId": 99999,
      "quantity": 1
    }
  ]
}
```

**验证点：**
- 应该返回错误码 `2_001_001_103`
- 错误消息：`子产品ID[99999]不存在`

### 3. 测试自定义模式未填写成本价

```http
POST /admin-api/dm/product-info/create
Content-Type: application/json

{
  "skuId": "COMBO-005",
  "skuName": "测试未填成本价",
  "modelNumber": "COMBO-005",
  "productType": 1,
  "bundleType": 1,
  "bundleItems": [
    {
      "subProductId": 5,
      "quantity": 1
    }
  ]
}
```

**验证点：**
- 应该返回错误码 `2_001_001_101`
- 错误消息：`自定义成本价模式时，成本价不能为空`

## 五、性能测试（可选）

### 1. 测试大量组合产品的查询性能

```sql
-- 创建索引（如果还没有）
CREATE INDEX idx_product_type ON dm_product_info(product_type, deleted);

-- 测试查询性能
EXPLAIN SELECT * FROM dm_product_info WHERE product_type = 1 AND deleted = 0;
```

### 2. 测试批量重新计算的性能

```bash
# 记录开始时间
time curl -X POST "http://localhost:48080/admin-api/dm/product-info/batch-recalculate-bundle-cost"
```

## 六、清理测试数据

```sql
-- 删除测试的组合产品
DELETE FROM dm_product_bundle_relation WHERE bundle_product_id IN (100, 101, 102);
DELETE FROM dm_product_info WHERE sku_id LIKE 'COMBO-%';

-- 删除测试的子产品（如果是临时创建的）
DELETE FROM dm_product_info WHERE sku_id IN ('SUB-001', 'SUB-002');
```

## 七、常见问题

### Q1: 为什么我的成本价没有自动更新？

**A:** 检查以下几点：
1. 产品的 `bundle_type` 是否为 2（自动累计模式）
2. 是否调用了 `recalculateBundleCostPrice` 接口
3. 子产品的 `cost_price` 是否为 NULL

### Q2: 如何设置定时任务自动重新计算成本价？

**A:** 可以使用 Spring 的 `@Scheduled` 注解，例如：

```java
@Scheduled(cron = "0 0 2 * * ?") // 每天凌晨2点执行
public void scheduledRecalculate() {
    productInfoService.batchRecalculateBundleCostPrice();
}
```

### Q3: 组合产品的明细查询很慢怎么办？

**A:** 确保以下索引存在：
- `dm_product_bundle_relation` 表的 `idx_bundle_product` 索引
- `dm_product_info` 表的主键索引

---

**测试完成后，请更新方案文档中的测试结果！**

