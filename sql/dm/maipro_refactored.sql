-- 菜单权限表数据重构 SQL
-- 移除自增ID，使用变量处理父子菜单关系，bit类型使用 b'0' 格式

-- ===== 产品管理模块 =====
-- 产品管理主菜单
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, component_name, status, visible, keep_alive, always_show,
    creator, create_time, updater, update_time, deleted
)
VALUES (
    '产品管理', '', 1, 1, 0,
    '/dm/product', 'ep:goods-filled', '', '', 0, b'1', b'1', b'1',
    '1', '2024-04-15 15:41:18', '1', '2024-09-08 16:41:24', b'0'
);

-- 获取产品管理主菜单ID
SELECT @productManagementId := LAST_INSERT_ID();

-- 产品信息菜单
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, component_name, status, visible, keep_alive, always_show,
    creator, create_time, updater, update_time, deleted
)
VALUES (
    '产品信息', '', 2, 1, @productManagementId,
    'product', 'fa-solid:list-ul', 'dm/product/index', 'ProductInfo', 0, b'1', b'1', b'1',
    '', '2024-04-15 16:00:37', '1', '2024-04-19 15:30:25', b'0'
);

-- 获取产品信息菜单ID
SELECT @productInfoId := LAST_INSERT_ID();

-- 产品信息相关按钮
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, component_name, status, visible, keep_alive, always_show,
    creator, create_time, updater, update_time, deleted
)
VALUES 
    ('产品信息查询', 'dm:product-info:query', 3, 1, @productInfoId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-04-15 16:00:37', '', '2024-04-15 16:00:37', b'0'),
    ('产品信息创建', 'dm:product-info:create', 3, 2, @productInfoId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-04-15 16:00:37', '', '2024-04-15 16:00:37', b'0'),
    ('产品信息更新', 'dm:product-info:update', 3, 3, @productInfoId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-04-15 16:00:37', '', '2024-04-15 16:00:37', b'0'),
    ('产品信息删除', 'dm:product-info:delete', 3, 4, @productInfoId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-04-15 16:00:37', '', '2024-04-15 16:00:37', b'0'),
    ('产品信息导出', 'dm:product-info:export', 3, 5, @productInfoId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-04-15 16:00:37', '', '2024-04-15 16:00:37', b'0');

-- 产品分类菜单
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, component_name, status, visible, keep_alive, always_show,
    creator, create_time, updater, update_time, deleted
)
VALUES (
    '产品分类', '', 2, 1, @productManagementId,
    'product-category', 'ep:takeaway-box', 'dm/dmcategory/index', 'DmProductCategory', 0, b'1', b'1', b'1',
    '', '2024-04-18 18:24:10', '1', '2024-04-19 14:24:56', b'0'
);

-- 获取产品分类菜单ID
SELECT @productCategoryId := LAST_INSERT_ID();

-- 产品分类相关按钮
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, component_name, status, visible, keep_alive, always_show,
    creator, create_time, updater, update_time, deleted
)
VALUES 
    ('产品分类查询', 'dm:product-category:query', 3, 1, @productCategoryId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-04-18 18:24:10', '', '2024-04-18 18:24:10', b'0'),
    ('产品分类创建', 'dm:product-category:create', 3, 2, @productCategoryId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-04-18 18:24:10', '', '2024-04-18 18:24:10', b'0'),
    ('产品分类更新', 'dm:product-category:update', 3, 3, @productCategoryId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-04-18 18:24:10', '', '2024-04-18 18:24:10', b'0'),
    ('产品分类删除', 'dm:product-category:delete', 3, 4, @productCategoryId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-04-18 18:24:10', '', '2024-04-18 18:24:10', b'0'),
    ('产品分类导出', 'dm:product-category:export', 3, 5, @productCategoryId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-04-18 18:24:10', '', '2024-04-18 18:24:10', b'0');

-- 类目佣金菜单
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, component_name, status, visible, keep_alive, always_show,
    creator, create_time, updater, update_time, deleted
)
VALUES (
    '类目佣金', '', 2, 2, @productManagementId,
    'category-commission', 'fa-solid:money-check-alt', 'dm/commission/index', 'CategoryCommission', 0, b'1', b'1', b'1',
    '', '2024-04-17 13:28:31', '1', '2024-04-19 14:25:21', b'0'
);

-- 获取类目佣金菜单ID
SELECT @categoryCommissionId := LAST_INSERT_ID();

-- 类目佣金相关按钮
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, component_name, status, visible, keep_alive, always_show,
    creator, create_time, updater, update_time, deleted
)
VALUES 
    ('类目佣金查询', 'dm:category-commission:query', 3, 1, @categoryCommissionId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-04-17 13:28:31', '', '2024-04-17 13:28:31', b'0'),
    ('类目佣金创建', 'dm:category-commission:create', 3, 2, @categoryCommissionId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-04-17 13:28:31', '', '2024-04-17 13:28:31', b'0'),
    ('类目佣金更新', 'dm:category-commission:update', 3, 3, @categoryCommissionId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-04-17 13:28:31', '', '2024-04-17 13:28:31', b'0'),
    ('类目佣金删除', 'dm:category-commission:delete', 3, 4, @categoryCommissionId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-04-17 13:28:31', '', '2024-04-17 13:28:31', b'0'),
    ('类目佣金导出', 'dm:category-commission:export', 3, 5, @categoryCommissionId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-04-17 13:28:31', '', '2024-04-17 13:28:31', b'0');

-- 供应商菜单
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, component_name, status, visible, keep_alive, always_show,
    creator, create_time, updater, update_time, deleted
)
VALUES (
    '供应商', '', 2, 3, @productManagementId,
    'product-supplier', 'fa:address-book-o', 'dm/supplier/index', 'ProductSupplier', 0, b'1', b'1', b'1',
    '', '2024-04-18 14:24:27', '1', '2024-04-19 14:26:11', b'0'
);

-- 获取供应商菜单ID
SELECT @productSupplierId := LAST_INSERT_ID();

-- 供应商相关按钮
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, component_name, status, visible, keep_alive, always_show,
    creator, create_time, updater, update_time, deleted
)
VALUES 
    ('供应商信息查询', 'dm:product-supplier:query', 3, 1, @productSupplierId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-04-18 14:24:27', '', '2024-04-18 14:24:27', b'0'),
    ('供应商信息创建', 'dm:product-supplier:create', 3, 2, @productSupplierId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-04-18 14:24:27', '', '2024-04-18 14:24:27', b'0'),
    ('供应商信息更新', 'dm:product-supplier:update', 3, 3, @productSupplierId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-04-18 14:24:27', '', '2024-04-18 14:24:27', b'0'),
    ('供应商信息删除', 'dm:product-supplier:delete', 3, 4, @productSupplierId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-04-18 14:24:27', '', '2024-04-18 14:24:27', b'0'),
    ('供应商信息导出', 'dm:product-supplier:export', 3, 5, @productSupplierId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-04-18 14:24:27', '', '2024-04-18 14:24:27', b'0');

-- 品牌信息菜单
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, component_name, status, visible, keep_alive, always_show,
    creator, create_time, updater, update_time, deleted
)
VALUES (
    '品牌信息', '', 2, 4, @productManagementId,
    'product-brand', 'fa:apple', 'dm/brand/index', 'DmProductBrand', 0, b'1', b'1', b'1',
    '', '2024-04-19 11:36:23', '1', '2024-04-19 14:26:29', b'0'
);

-- 获取品牌信息菜单ID
SELECT @productBrandId := LAST_INSERT_ID();

-- 品牌信息相关按钮
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, component_name, status, visible, keep_alive, always_show,
    creator, create_time, updater, update_time, deleted
)
VALUES 
    ('品牌信息查询', 'dm:product-brand:query', 3, 1, @productBrandId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-04-19 11:36:23', '', '2024-04-19 11:36:23', b'0'),
    ('品牌信息创建', 'dm:product-brand:create', 3, 2, @productBrandId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-04-19 11:36:23', '', '2024-04-19 11:36:23', b'0'),
    ('品牌信息更新', 'dm:product-brand:update', 3, 3, @productBrandId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-04-19 11:36:23', '', '2024-04-19 11:36:23', b'0'),
    ('品牌信息删除', 'dm:product-brand:delete', 3, 4, @productBrandId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-04-19 11:36:23', '', '2024-04-19 11:36:23', b'0'),
    ('品牌信息导出', 'dm:product-brand:export', 3, 5, @productBrandId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-04-19 11:36:23', '', '2024-04-19 11:36:23', b'0');

-- 产品成本结构管理菜单
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, component_name, status, visible, keep_alive, always_show,
    creator, create_time, updater, update_time, deleted
)
VALUES (
    '产品成本结构管理', '', 2, 0, @productManagementId,
    'product-costs', '', 'dm/productcosts/index', 'ProductCosts', 0, b'0', b'1', b'0',
    '', '2024-11-12 15:45:22', '1', '2024-11-12 15:45:49', b'0'
);

-- 获取产品成本结构管理菜单ID
SELECT @productCostsId := LAST_INSERT_ID();

-- 产品成本结构管理相关按钮
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, component_name, status, visible, keep_alive, always_show,
    creator, create_time, updater, update_time, deleted
)
VALUES 
    ('产品成本结构查询', 'dm:product-costs:query', 3, 1, @productCostsId, '', '', '', '', 0, b'1', b'1', b'1', '', '2024-11-12 15:45:22', '1', '2024-11-12 15:56:11', b'0'),
    ('产品成本结构创建', 'dm:product-costs:create', 3, 2, @productCostsId, '', '', '', '', 0, b'1', b'1', b'1', '', '2024-11-12 15:45:22', '1', '2024-11-12 15:56:16', b'0'),
    ('产品成本结构更新', 'dm:product-costs:update', 3, 3, @productCostsId, '', '', '', '', 0, b'1', b'1', b'1', '', '2024-11-12 15:45:22', '1', '2024-11-12 15:56:22', b'0'),
    ('产品成本结构删除', 'dm:product-costs:delete', 3, 4, @productCostsId, '', '', '', '', 0, b'1', b'1', b'1', '', '2024-11-12 15:45:22', '1', '2024-11-12 15:56:27', b'0'),
    ('产品成本结构导出', 'dm:product-costs:export', 3, 5, @productCostsId, '', '', '', '', 0, b'1', b'1', b'1', '', '2024-11-12 15:45:22', '1', '2024-11-12 15:56:32', b'0'),
    ('导入', 'dm:product-costs:import', 3, 6, @productCostsId, '', '', '', '', 0, b'1', b'1', b'1', '1', '2024-11-12 16:03:09', '1', '2024-11-12 16:03:09', b'0');

-- 利润试算菜单
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, component_name, status, visible, keep_alive, always_show,
    creator, create_time, updater, update_time, deleted
)
VALUES (
    '利润试算', '', 2, 0, @productManagementId,
    'profit-calculation', 'fa:thumbs-up', 'multiple/calculation/index', 'ProfitCalculation', 0, b'1', b'1', b'1',
    '', '2025-06-07 13:38:39', '1', '2025-07-19 10:53:19', b'0'
);

-- 获取利润试算菜单ID
SELECT @profitCalculationId := LAST_INSERT_ID();

-- 利润试算相关按钮
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, component_name, status, visible, keep_alive, always_show,
    creator, create_time, updater, update_time, deleted
)
VALUES 
    ('利润预测查询', 'multiple.platform:profit-calculation:query', 3, 1, @profitCalculationId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2025-06-07 13:38:39', '', '2025-06-07 13:38:39', b'0'),
    ('利润预测创建', 'multiple.platform:profit-calculation:create', 3, 2, @profitCalculationId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2025-06-07 13:38:39', '', '2025-06-07 13:38:39', b'0'),
    ('利润预测更新', 'multiple.platform:profit-calculation:update', 3, 3, @profitCalculationId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2025-06-07 13:38:39', '', '2025-06-07 13:38:39', b'0'),
    ('利润预测删除', 'multiple.platform:profit-calculation:delete', 3, 4, @profitCalculationId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2025-06-07 13:38:39', '', '2025-06-07 13:38:39', b'0'),
    ('利润预测导出', 'multiple.platform:profit-calculation:export', 3, 5, @profitCalculationId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2025-06-07 13:38:39', '', '2025-06-07 13:38:39', b'0');

-- 选品计划菜单（已禁用）
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, component_name, status, visible, keep_alive, always_show,
    creator, create_time, updater, update_time, deleted
)
VALUES (
    '选品计划', '', 2, 0, @productManagementId,
    'product-selection-plan', 'fa:thumbs-up', 'dm/plan/index', 'ProductSelectionPlan', 1, b'0', b'1', b'0',
    '', '2024-04-19 15:27:21', '1', '2025-07-19 10:52:23', b'0'
);

-- 获取选品计划菜单ID
SELECT @productSelectionPlanId := LAST_INSERT_ID();

-- 选品计划相关按钮
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, component_name, status, visible, keep_alive, always_show,
    creator, create_time, updater, update_time, deleted
)
VALUES 
    ('选品计划查询', 'dm:product-selection-plan:query', 3, 1, @productSelectionPlanId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-04-19 15:27:21', '', '2024-04-19 15:27:21', b'0'),
    ('选品计划创建', 'dm:product-selection-plan:create', 3, 2, @productSelectionPlanId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-04-19 15:27:21', '', '2024-04-19 15:27:21', b'0'),
    ('选品计划更新', 'dm:product-selection-plan:update', 3, 3, @productSelectionPlanId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-04-19 15:27:21', '', '2024-04-19 15:27:21', b'0'),
    ('选品计划删除', 'dm:product-selection-plan:delete', 3, 4, @productSelectionPlanId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-04-19 15:27:21', '', '2024-04-19 15:27:21', b'0'),
    ('选品计划导出', 'dm:product-selection-plan:export', 3, 5, @productSelectionPlanId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-04-19 15:27:21', '', '2024-04-19 15:27:21', b'0');

-- ===== 采购管理模块 =====
-- 采购管理主菜单
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, component_name, status, visible, keep_alive, always_show,
    creator, create_time, updater, update_time, deleted
)
VALUES (
    '采购管理', '', 1, 2, 0,
    '/dm/purchase', 'fa:shopping-cart', '', '', 0, b'1', b'1', b'1',
    '1', '2024-04-24 15:33:59', '1', '2024-09-08 16:41:29', b'0'
);

-- 获取采购管理主菜单ID
SELECT @purchaseManagementId := LAST_INSERT_ID();

-- 采购计划菜单
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, component_name, status, visible, keep_alive, always_show,
    creator, create_time, updater, update_time, deleted
)
VALUES (
    '采购计划', '', 2, 0, @purchaseManagementId,
    'purchase-plan', 'fa:paper-plane', 'dm/purchase/index', 'PurchasePlan', 0, b'1', b'1', b'1',
    '', '2024-04-25 14:08:28', '1', '2024-04-26 18:15:33', b'0'
);

-- 获取采购计划菜单ID
SELECT @purchasePlanId := LAST_INSERT_ID();

-- 采购计划相关按钮
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, component_name, status, visible, keep_alive, always_show,
    creator, create_time, updater, update_time, deleted
)
VALUES 
    ('采购计划查询', 'dm:purchase-plan:query', 3, 1, @purchasePlanId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-04-25 14:08:28', '', '2024-04-25 14:08:28', b'0'),
    ('采购计划创建', 'dm:purchase-plan:create', 3, 2, @purchasePlanId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-04-25 14:08:28', '', '2024-04-25 14:08:28', b'0'),
    ('采购计划更新', 'dm:purchase-plan:update', 3, 3, @purchasePlanId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-04-25 14:08:28', '', '2024-04-25 14:08:28', b'0'),
    ('采购计划删除', 'dm:purchase-plan:delete', 3, 4, @purchasePlanId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-04-25 14:08:28', '', '2024-04-25 14:08:28', b'0'),
    ('采购计划导出', 'dm:purchase-plan:export', 3, 5, @purchasePlanId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-04-25 14:08:28', '', '2024-04-25 14:08:28', b'0');

-- 采购单菜单
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, component_name, status, visible, keep_alive, always_show,
    creator, create_time, updater, update_time, deleted
)
VALUES (
    '采购单', '', 2, 1, @purchaseManagementId,
    'purchase-order', 'ep:notebook', 'dm/purchaseorder/index', 'PurchaseOrder', 0, b'1', b'1', b'1',
    '', '2024-04-30 15:20:34', '1', '2024-04-30 15:35:33', b'0'
);

-- 获取采购单菜单ID
SELECT @purchaseOrderId := LAST_INSERT_ID();

-- 采购单相关按钮
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, component_name, status, visible, keep_alive, always_show,
    creator, create_time, updater, update_time, deleted
)
VALUES 
    ('采购单查询', 'dm:purchase-order:query', 3, 1, @purchaseOrderId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-04-30 15:20:34', '', '2024-04-30 15:20:34', b'0'),
    ('采购单创建', 'dm:purchase-order:create', 3, 2, @purchaseOrderId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-04-30 15:20:34', '', '2024-04-30 15:20:34', b'0'),
    ('采购单更新', 'dm:purchase-order:update', 3, 3, @purchaseOrderId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-04-30 15:20:34', '', '2024-04-30 15:20:34', b'0'),
    ('采购单删除', 'dm:purchase-order:delete', 3, 4, @purchaseOrderId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-04-30 15:20:34', '', '2024-04-30 15:20:34', b'0'),
    ('采购单导出', 'dm:purchase-order:export', 3, 5, @purchaseOrderId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-04-30 15:20:34', '', '2024-04-30 15:20:34', b'0');

-- 采购单到货日志管理菜单
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, component_name, status, visible, keep_alive, always_show,
    creator, create_time, updater, update_time, deleted
)
VALUES (
    '采购单到货日志管理', '', 2, 0, @purchaseManagementId,
    'purchase-order-arrived-log', '', 'dm/purchaseorderlog/index', 'PurchaseOrderArrivedLog', 0, b'0', b'0', b'0',
    '', '2024-05-25 21:37:55', '131', '2024-06-11 15:26:24', b'0'
);

-- 获取采购单到货日志管理菜单ID
SELECT @purchaseOrderArrivedLogId := LAST_INSERT_ID();

-- 采购单到货日志管理相关按钮
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, component_name, status, visible, keep_alive, always_show,
    creator, create_time, updater, update_time, deleted
)
VALUES 
    ('采购单到货日志查询', 'dm:purchase-order-arrived-log:query', 3, 1, @purchaseOrderArrivedLogId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-05-25 21:37:55', '', '2024-05-25 21:37:55', b'0'),
    ('采购单到货日志创建', 'dm:purchase-order-arrived-log:create', 3, 2, @purchaseOrderArrivedLogId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-05-25 21:37:55', '', '2024-05-25 21:37:55', b'0'),
    ('采购单到货日志更新', 'dm:purchase-order-arrived-log:update', 3, 3, @purchaseOrderArrivedLogId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-05-25 21:37:55', '', '2024-05-25 21:37:55', b'0'),
    ('采购单到货日志删除', 'dm:purchase-order-arrived-log:delete', 3, 4, @purchaseOrderArrivedLogId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-05-25 21:37:55', '', '2024-05-25 21:37:55', b'0'),
    ('采购单到货日志导出', 'dm:purchase-order-arrived-log:export', 3, 5, @purchaseOrderArrivedLogId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-05-25 21:37:55', '', '2024-05-25 21:37:55', b'0');

-- 进度查询菜单
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, component_name, status, visible, keep_alive, always_show,
    creator, create_time, updater, update_time, deleted
)
VALUES (
    '进度查询', '', 2, 1, @purchaseManagementId,
    'purchase-life', 'ep:search', 'dm/purchaselife/index', 'PurchaseLife', 0, b'1', b'1', b'1',
    '131', '2024-06-12 16:43:29', '131', '2024-06-12 16:43:29', b'0'
);

-- ===== 物流管理模块 =====
-- 物流管理主菜单
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, component_name, status, visible, keep_alive, always_show,
    creator, create_time, updater, update_time, deleted
)
VALUES (
    '物流管理', '', 1, 3, 0,
    '/dm/transport', 'fa-solid:truck-moving', '', '', 0, b'1', b'1', b'1',
    '1', '2024-05-17 16:26:06', '1', '2024-09-08 16:41:40', b'0'
);

-- 获取物流管理主菜单ID
SELECT @transportManagementId := LAST_INSERT_ID();

-- 头程计划菜单
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, component_name, status, visible, keep_alive, always_show,
    creator, create_time, updater, update_time, deleted
)
VALUES (
    '头程计划', '', 2, 0, @transportManagementId,
    'transport-plan', 'fa:plane', 'dm/transport/index', 'TransportPlan', 0, b'1', b'1', b'1',
    '', '2024-05-17 16:57:26', '1', '2024-05-20 10:14:31', b'0'
);

-- 获取头程计划菜单ID
SELECT @transportPlanId := LAST_INSERT_ID();

-- 头程计划相关按钮
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, component_name, status, visible, keep_alive, always_show,
    creator, create_time, updater, update_time, deleted
)
VALUES 
    ('头程计划查询', 'dm:transport-plan:query', 3, 1, @transportPlanId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-05-17 16:57:26', '', '2024-05-17 16:57:26', b'0'),
    ('头程计划创建', 'dm:transport-plan:create', 3, 2, @transportPlanId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-05-17 16:57:26', '', '2024-05-17 16:57:26', b'0'),
    ('头程计划更新', 'dm:transport-plan:update', 3, 3, @transportPlanId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-05-17 16:57:26', '', '2024-05-17 16:57:26', b'0'),
    ('头程计划删除', 'dm:transport-plan:delete', 3, 4, @transportPlanId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-05-17 16:57:26', '', '2024-05-17 16:57:26', b'0'),
    ('头程计划导出', 'dm:transport-plan:export', 3, 5, @transportPlanId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-05-17 16:57:26', '', '2024-05-17 16:57:26', b'0');

-- 头程明细菜单
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, component_name, status, visible, keep_alive, always_show,
    creator, create_time, updater, update_time, deleted
)
VALUES (
    '头程明细', 'dm:transport-plan:query', 2, 1, @transportManagementId,
    'transport-plan-detail', 'ep:search', 'dm/transport/components/TransportPlanDetailList', 'TransportPlanDetailList', 0, b'1', b'1', b'1',
    '1', '2025-01-17 01:05:30', '1', '2025-03-23 21:16:50', b'0'
);

-- ===== 财务管理模块 =====
-- 财务管理主菜单
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, component_name, status, visible, keep_alive, always_show,
    creator, create_time, updater, update_time, deleted
)
VALUES (
    '财务管理', '', 1, 4, 0,
    '/dm/finance', 'ep:money', '', '', 0, b'1', b'1', b'1',
    '1', '2024-05-13 17:08:20', '1', '2024-09-08 16:41:54', b'0'
);

-- 获取财务管理主菜单ID
SELECT @financeManagementId := LAST_INSERT_ID();

-- 付款单菜单
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, component_name, status, visible, keep_alive, always_show,
    creator, create_time, updater, update_time, deleted
)
VALUES (
    '付款单', '', 2, 0, @financeManagementId,
    'finance-payment', 'ep:list', 'dm/finance/index', 'FinancePayment', 0, b'1', b'1', b'1',
    '', '2024-05-13 17:09:11', '1', '2024-05-13 17:09:32', b'0'
);

-- 获取付款单菜单ID
SELECT @financePaymentId := LAST_INSERT_ID();

-- 付款单相关按钮
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, component_name, status, visible, keep_alive, always_show,
    creator, create_time, updater, update_time, deleted
)
VALUES 
    ('付款单查询', 'dm:finance-payment:query', 3, 1, @financePaymentId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-05-13 17:09:11', '', '2024-05-13 17:09:11', b'0'),
    ('付款单创建', 'dm:finance-payment:create', 3, 2, @financePaymentId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-05-13 17:09:11', '', '2024-05-13 17:09:11', b'0'),
    ('付款单更新', 'dm:finance-payment:update', 3, 3, @financePaymentId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-05-13 17:09:11', '', '2024-05-13 17:09:11', b'0'),
    ('付款单删除', 'dm:finance-payment:delete', 3, 4, @financePaymentId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-05-13 17:09:11', '', '2024-05-13 17:09:11', b'0'),
    ('付款单导出', 'dm:finance-payment:export', 3, 5, @financePaymentId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-05-13 17:09:11', '', '2024-05-13 17:09:11', b'0');

-- 财务核算菜单
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, component_name, status, visible, keep_alive, always_show,
    creator, create_time, updater, update_time, deleted
)
VALUES (
    '财务核算', '', 2, 0, @financeManagementId,
    'profit-report', 'fa-solid:money-check-alt', 'dm/profitreport/index', 'ProfitReport', 0, b'1', b'1', b'1',
    '', '2024-10-12 10:58:35', '1', '2024-10-18 14:09:01', b'0'
);

-- 获取财务核算菜单ID
SELECT @profitReportId := LAST_INSERT_ID();

-- 财务核算相关按钮
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, component_name, status, visible, keep_alive, always_show,
    creator, create_time, updater, update_time, deleted
)
VALUES 
    ('财务核算查询', 'dm:profit-report:query', 3, 1, @profitReportId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-10-12 10:58:35', '', '2024-10-12 10:58:35', b'0'),
    ('财务核算创建', 'dm:profit-report:create', 3, 2, @profitReportId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-10-12 10:58:35', '', '2024-10-12 10:58:35', b'0'),
    ('财务核算更新', 'dm:profit-report:update', 3, 3, @profitReportId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-10-12 10:58:35', '', '2024-10-12 10:58:35', b'0'),
    ('财务核算删除', 'dm:profit-report:delete', 3, 4, @profitReportId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-10-12 10:58:35', '', '2024-10-12 10:58:35', b'0'),
    ('财务核算导出', 'dm:profit-report:export', 3, 5, @profitReportId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-10-12 10:58:35', '', '2024-10-12 10:58:35', b'0');

-- ===== 平台管理模块 =====
-- 平台管理主菜单
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, component_name, status, visible, keep_alive, always_show,
    creator, create_time, updater, update_time, deleted
)
VALUES (
    '平台管理', '', 1, 0, 0,
    '/dm/platform', 'ep:list', '', '', 0, b'1', b'1', b'1',
    '1', '2024-06-24 14:53:08', '1', '2024-09-08 16:41:18', b'0'
);

-- 获取平台管理主菜单ID
SELECT @platformManagementId := LAST_INSERT_ID();

-- 订单信息菜单
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, component_name, status, visible, keep_alive, always_show,
    creator, create_time, updater, update_time, deleted
)
VALUES (
    '订单信息', '', 2, 0, @platformManagementId,
    'ozon-order', 'fa-solid:clipboard-list', 'dm/order/index', 'OzonOrder', 0, b'1', b'1', b'1',
    '', '2024-06-24 15:06:32', '1', '2024-06-24 15:08:52', b'0'
);

-- 获取订单信息菜单ID
SELECT @ozonOrderId := LAST_INSERT_ID();

-- 订单信息相关按钮
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, component_name, status, visible, keep_alive, always_show,
    creator, create_time, updater, update_time, deleted
)
VALUES 
    ('Ozon订单查询', 'dm:ozon-order:query', 3, 1, @ozonOrderId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-06-24 15:06:32', '', '2024-06-24 15:06:32', b'0'),
    ('Ozon订单创建', 'dm:ozon-order:create', 3, 2, @ozonOrderId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-06-24 15:06:32', '', '2024-06-24 15:06:32', b'0'),
    ('Ozon订单更新', 'dm:ozon-order:update', 3, 3, @ozonOrderId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-06-24 15:06:32', '', '2024-06-24 15:06:32', b'0'),
    ('Ozon订单删除', 'dm:ozon-order:delete', 3, 4, @ozonOrderId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-06-24 15:06:32', '', '2024-06-24 15:06:32', b'0'),
    ('Ozon订单导出', 'dm:ozon-order:export', 3, 5, @ozonOrderId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-06-24 15:06:32', '', '2024-06-24 15:06:32', b'0');

-- 在线商品菜单
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, component_name, status, visible, keep_alive, always_show,
    creator, create_time, updater, update_time, deleted
)
VALUES (
    '在线商品', '', 2, 1, @platformManagementId,
    'ozon-product-online', 'ep:goods', 'dm/online/index', 'OzonProductOnline', 0, b'1', b'1', b'1',
    '', '2024-06-26 17:05:49', '1', '2024-06-26 17:08:39', b'0'
);

-- 获取在线商品菜单ID
SELECT @ozonProductOnlineId := LAST_INSERT_ID();

-- 在线商品相关按钮
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, component_name, status, visible, keep_alive, always_show,
    creator, create_time, updater, update_time, deleted
)
VALUES 
    ('在线商品查询', 'dm:ozon-product-online:query', 3, 1, @ozonProductOnlineId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-06-26 17:05:49', '', '2024-06-26 17:05:49', b'0'),
    ('在线商品创建', 'dm:ozon-product-online:create', 3, 2, @ozonProductOnlineId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-06-26 17:05:49', '', '2024-06-26 17:05:49', b'0'),
    ('在线商品更新', 'dm:ozon-product-online:update', 3, 3, @ozonProductOnlineId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-06-26 17:05:49', '', '2024-06-26 17:05:49', b'0'),
    ('在线商品删除', 'dm:ozon-product-online:delete', 3, 4, @ozonProductOnlineId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-06-26 17:05:49', '', '2024-06-26 17:05:49', b'0'),
    ('在线商品导出', 'dm:ozon-product-online:export', 3, 5, @ozonProductOnlineId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-06-26 17:05:49', '', '2024-06-26 17:05:49', b'0');

-- 交易记录菜单
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, component_name, status, visible, keep_alive, always_show,
    creator, create_time, updater, update_time, deleted
)
VALUES (
    '交易记录', '', 2, 2, @platformManagementId,
    'ozon-finance-transaction', 'ep:edit-pen', 'dm/transaction/index', 'OzonFinanceTransaction', 0, b'1', b'1', b'1',
    '', '2024-07-05 10:44:23', '1', '2024-07-05 10:47:00', b'0'
);

-- 获取交易记录菜单ID
SELECT @ozonFinanceTransactionId := LAST_INSERT_ID();

-- 交易记录相关按钮
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, component_name, status, visible, keep_alive, always_show,
    creator, create_time, updater, update_time, deleted
)
VALUES 
    ('交易记录查询', 'dm:ozon-finance-transaction:query', 3, 1, @ozonFinanceTransactionId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-07-05 10:44:23', '', '2024-07-05 10:44:23', b'0'),
    ('交易记录创建', 'dm:ozon-finance-transaction:create', 3, 2, @ozonFinanceTransactionId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-07-05 10:44:23', '', '2024-07-05 10:44:23', b'0'),
    ('交易记录更新', 'dm:ozon-finance-transaction:update', 3, 3, @ozonFinanceTransactionId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-07-05 10:44:23', '', '2024-07-05 10:44:23', b'0'),
    ('交易记录删除', 'dm:ozon-finance-transaction:delete', 3, 4, @ozonFinanceTransactionId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-07-05 10:44:23', '', '2024-07-05 10:44:23', b'0'),
    ('交易记录导出', 'dm:ozon-finance-transaction:export', 3, 5, @ozonFinanceTransactionId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-07-05 10:44:23', '', '2024-07-05 10:44:23', b'0');

-- FBO进仓菜单
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, component_name, status, visible, keep_alive, always_show,
    creator, create_time, updater, update_time, deleted
)
VALUES (
    'FBO进仓', '', 2, 4, @platformManagementId,
    'ozon-supply-order', 'ep:credit-card', 'dm/ozonsupplyorder/index', 'OzonSupplyOrder', 0, b'1', b'1', b'1',
    '', '2025-01-04 13:39:51', '1', '2025-01-05 23:28:21', b'0'
);

-- 获取FBO进仓菜单ID
SELECT @ozonSupplyOrderId := LAST_INSERT_ID();

-- FBO进仓相关按钮
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, component_name, status, visible, keep_alive, always_show,
    creator, create_time, updater, update_time, deleted
)
VALUES 
    ('供应订单查询', 'dm:ozon-supply-order:query', 3, 1, @ozonSupplyOrderId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2025-01-04 13:39:51', '', '2025-03-23 21:25:17', b'0'),
    ('供应订单创建', 'dm:ozon-supply-order:create', 3, 2, @ozonSupplyOrderId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2025-01-04 13:39:51', '', '2025-03-23 21:25:21', b'0'),
    ('供应订单更新', 'dm:ozon-supply-order:update', 3, 3, @ozonSupplyOrderId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2025-01-04 13:39:51', '', '2025-03-23 21:25:24', b'0'),
    ('供应订单删除', 'dm:ozon-supply-order:delete', 3, 4, @ozonSupplyOrderId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2025-01-04 13:39:51', '', '2025-03-23 21:25:28', b'0'),
    ('供应订单导出', 'dm:ozon-supply-order:export', 3, 5, @ozonSupplyOrderId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2025-01-04 13:39:51', '', '2025-03-23 21:25:36', b'0');

-- ===== 数据统计模块 =====
-- 数据统计主菜单
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, component_name, status, visible, keep_alive, always_show,
    creator, create_time, updater, update_time, deleted
)
VALUES (
    '数据统计', '', 1, 6, 0,
    '/dm/statistics', 'ep:histogram', '', '', 0, b'1', b'1', b'1',
    '1', '2024-06-30 13:26:38', '1', '2024-09-08 16:42:38', b'0'
);

-- 获取数据统计主菜单ID
SELECT @statisticsManagementId := LAST_INSERT_ID();

-- 业绩概览菜单
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, component_name, status, visible, keep_alive, always_show,
    creator, create_time, updater, update_time, deleted
)
VALUES (
    '业绩概览', '', 2, 0, @statisticsManagementId,
    'performance', 'ep:data-line', 'dm/statistics/overview/index', 'StatisticsOverview', 0, b'1', b'1', b'1',
    '1', '2025-03-04 17:23:20', '1', '2025-03-18 11:26:47', b'0'
);

-- 广告活动菜单
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, component_name, status, visible, keep_alive, always_show,
    creator, create_time, updater, update_time, deleted
)
VALUES (
    '广告活动', '', 2, 1, @statisticsManagementId,
    'ozon-ad-campaigns', 'ep:data-analysis', 'dm/ad/index', 'OzonAdCampaigns', 0, b'1', b'1', b'1',
    '', '2024-06-30 13:30:46', '1', '2025-03-23 21:12:41', b'0'
);

-- 获取广告活动菜单ID
SELECT @ozonAdCampaignsId := LAST_INSERT_ID();

-- 广告活动相关按钮
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, component_name, status, visible, keep_alive, always_show,
    creator, create_time, updater, update_time, deleted
)
VALUES 
    ('广告活动查询', 'dm:ozon-ad-campaigns:query', 3, 1, @ozonAdCampaignsId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-06-30 13:30:46', '', '2024-06-30 13:30:46', b'0'),
    ('广告活动创建', 'dm:ozon-ad-campaigns:create', 3, 2, @ozonAdCampaignsId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-06-30 13:30:46', '', '2024-06-30 13:30:46', b'0'),
    ('广告活动更新', 'dm:ozon-ad-campaigns:update', 3, 3, @ozonAdCampaignsId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-06-30 13:30:46', '', '2024-06-30 13:30:46', b'0'),
    ('广告活动删除', 'dm:ozon-ad-campaigns:delete', 3, 4, @ozonAdCampaignsId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-06-30 13:30:46', '', '2024-06-30 13:30:46', b'0'),
    ('广告活动导出', 'dm:ozon-ad-campaigns:export', 3, 5, @ozonAdCampaignsId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-06-30 13:30:46', '', '2024-06-30 13:30:46', b'0');

-- 销量统计菜单
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, component_name, status, visible, keep_alive, always_show,
    creator, create_time, updater, update_time, deleted
)
VALUES (
    '销量统计', '', 2, 2, @statisticsManagementId,
    'ozon-order-item', 'ep:data-line', 'dm/volume/index', 'OzonOrderItem', 0, b'1', b'1', b'1',
    '', '2024-08-08 16:40:13', '1', '2025-03-23 21:12:51', b'0'
);

-- 获取销量统计菜单ID
SELECT @ozonOrderItemId := LAST_INSERT_ID();

-- 销量统计相关按钮
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, component_name, status, visible, keep_alive, always_show,
    creator, create_time, updater, update_time, deleted
)
VALUES 
    ('销量查询', 'dm:ozon-order-item:query', 3, 1, @ozonOrderItemId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-08-08 16:40:14', '', '2024-08-08 16:40:14', b'0'),
    ('销量创建', 'dm:ozon-order-item:create', 3, 2, @ozonOrderItemId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-08-08 16:40:14', '', '2024-08-08 16:40:14', b'0'),
    ('销量更新', 'dm:ozon-order-item:update', 3, 3, @ozonOrderItemId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-08-08 16:40:14', '', '2024-08-08 16:40:14', b'0'),
    ('销量删除', 'dm:ozon-order-item:delete', 3, 4, @ozonOrderItemId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-08-08 16:40:14', '', '2024-08-08 16:40:14', b'0'),
    ('销量导出', 'dm:ozon-order-item:export', 3, 5, @ozonOrderItemId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-08-08 16:40:14', '', '2024-08-08 16:40:14', b'0');

-- 进销存报表菜单
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, component_name, status, visible, keep_alive, always_show,
    creator, create_time, updater, update_time, deleted
)
VALUES (
    '进销存报表', '', 1, 3, 0,
    '/report', 'fa:dashboard', '', '', 0, b'1', b'1', b'1',
    '1', '2024-12-27 21:50:35', '1', '2024-12-27 21:52:43', b'0'
);

-- 获取进销存报表主菜单ID
SELECT @reportManagementId := LAST_INSERT_ID();

-- 国内采购出货统计菜单
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, component_name, status, visible, keep_alive, always_show,
    creator, create_time, updater, update_time, deleted
)
VALUES (
    '国内采购出货统计', '', 2, 1, @reportManagementId,
    'stock', '', 'dm/storage/index', 'StorageStatistics', 0, b'1', b'1', b'1',
    '1', '2024-12-27 21:56:59', '1', '2024-12-29 12:15:53', b'0'
);

-- 在途进销存统计菜单
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, component_name, status, visible, keep_alive, always_show,
    creator, create_time, updater, update_time, deleted
)
VALUES (
    '在途进销存统计', '', 2, 2, @reportManagementId,
    'stock/report', '', 'dm/transport/components/TransportReportTable', 'TransportReportTable', 0, b'1', b'1', b'1',
    '1', '2025-01-02 20:31:17', '1', '2025-01-02 20:33:11', b'0'
);

-- FBO进销存统计菜单
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, component_name, status, visible, keep_alive, always_show,
    creator, create_time, updater, update_time, deleted
)
VALUES (
    'FBO进销存统计', '', 2, 3, @reportManagementId,
    'fbo/report', '', 'dm/ozonsupplyorder/components/FboInboundReportTable', 'FboInboundReportTable', 0, b'1', b'1', b'1',
    '1', '2025-01-04 22:11:47', '1', '2025-01-04 22:12:35', b'0'
);

-- ===== 店铺授权模块 =====
-- 店铺授权主菜单
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, component_name, status, visible, keep_alive, always_show,
    creator, create_time, updater, update_time, deleted
)
VALUES (
    '店铺授权', '', 1, 7, 0,
    '/dm/shop', 'fa-solid:store', '', '', 0, b'1', b'1', b'1',
    '1', '2024-06-14 15:59:20', '1', '2024-06-30 13:26:49', b'0'
);

-- 获取店铺授权主菜单ID
SELECT @shopManagementId := LAST_INSERT_ID();

-- 店铺管理菜单
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, component_name, status, visible, keep_alive, always_show,
    creator, create_time, updater, update_time, deleted
)
VALUES (
    '店铺管理', '', 2, 0, @shopManagementId,
    'ozon-shop-mapping', 'ep:shop', 'dm/ozonshopmapping/index', 'OzonShopMapping', 0, b'1', b'1', b'1',
    '', '2024-06-14 16:18:44', '1', '2024-08-16 13:17:34', b'0'
);

-- 获取店铺管理菜单ID
SELECT @ozonShopMappingId := LAST_INSERT_ID();

-- 店铺管理相关按钮
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, component_name, status, visible, keep_alive, always_show,
    creator, create_time, updater, update_time, deleted
)
VALUES 
    ('ozon店铺查询', 'dm:ozon-shop-mapping:query', 3, 1, @ozonShopMappingId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-06-14 16:18:44', '', '2024-06-14 16:18:44', b'0'),
    ('ozon店铺创建', 'dm:ozon-shop-mapping:create', 3, 2, @ozonShopMappingId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-06-14 16:18:44', '', '2024-06-14 16:18:44', b'0'),
    ('ozon店铺更新', 'dm:ozon-shop-mapping:update', 3, 3, @ozonShopMappingId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-06-14 16:18:44', '', '2024-06-14 16:18:44', b'0'),
    ('ozon店铺删除', 'dm:ozon-shop-mapping:delete', 3, 4, @ozonShopMappingId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-06-14 16:18:44', '', '2024-06-14 16:18:44', b'0'),
    ('ozon店铺导出', 'dm:ozon-shop-mapping:export', 3, 5, @ozonShopMappingId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-06-14 16:18:44', '', '2024-06-14 16:18:44', b'0');

-- ===== 海外仓模块 =====
-- 海外仓主菜单
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, component_name, status, visible, keep_alive, always_show,
    creator, create_time, updater, update_time, deleted
)
VALUES (
    '海外仓', '', 1, 8, 0,
    '/dm/warehouse', 'fa-solid:house-damage', '', '', 0, b'1', b'1', b'1',
    '1', '2024-08-19 16:53:27', '1', '2024-08-26 16:31:30', b'0'
);

-- 获取海外仓主菜单ID
SELECT @warehouseManagementId := LAST_INSERT_ID();

-- 海外仓管理菜单
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, component_name, status, visible, keep_alive, always_show,
    creator, create_time, updater, update_time, deleted
)
VALUES (
    '海外仓管理', '', 2, 0, @warehouseManagementId,
    'fbs-warehouse', 'fa-solid:warehouse', 'dm/warehouse/index', 'FbsWarehouse', 0, b'1', b'1', b'1',
    '', '2024-08-19 16:59:52', '1', '2024-09-08 16:43:17', b'0'
);

-- 获取海外仓管理菜单ID
SELECT @fbsWarehouseId := LAST_INSERT_ID();

-- 海外仓管理相关按钮
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, component_name, status, visible, keep_alive, always_show,
    creator, create_time, updater, update_time, deleted
)
VALUES 
    ('海外仓查询', 'dm:fbs-warehouse:query', 3, 1, @fbsWarehouseId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-08-19 16:59:52', '', '2024-08-19 16:59:52', b'0'),
    ('海外仓创建', 'dm:fbs-warehouse:create', 3, 2, @fbsWarehouseId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-08-19 16:59:52', '', '2024-08-19 16:59:52', b'0'),
    ('海外仓更新', 'dm:fbs-warehouse:update', 3, 3, @fbsWarehouseId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-08-19 16:59:52', '', '2024-08-19 16:59:52', b'0'),
    ('海外仓删除', 'dm:fbs-warehouse:delete', 3, 4, @fbsWarehouseId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-08-19 16:59:52', '', '2024-08-19 16:59:52', b'0'),
    ('海外仓导出', 'dm:fbs-warehouse:export', 3, 5, @fbsWarehouseId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-08-19 16:59:52', '', '2024-08-19 16:59:52', b'0');

-- 收费项目管理菜单
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, component_name, status, visible, keep_alive, always_show,
    creator, create_time, updater, update_time, deleted
)
VALUES (
    '收费项目管理', '', 2, 1, @warehouseManagementId,
    'fbs-fee-services', 'fa-solid:dollar-sign', 'dm/logistics/index', 'FbsFeeServices', 0, b'1', b'1', b'1',
    '', '2024-08-19 17:18:21', '1', '2024-09-08 16:45:14', b'0'
);

-- 获取收费项目管理菜单ID
SELECT @fbsFeeServicesId := LAST_INSERT_ID();

-- 收费项目管理相关按钮
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, component_name, status, visible, keep_alive, always_show,
    creator, create_time, updater, update_time, deleted
)
VALUES 
    ('收费项目查询', 'dm:fbs-fee-services:query', 3, 1, @fbsFeeServicesId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-08-19 17:18:21', '', '2024-08-19 17:18:21', b'0'),
    ('收费项目创建', 'dm:fbs-fee-services:create', 3, 2, @fbsFeeServicesId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-08-19 17:18:21', '', '2024-08-19 17:18:21', b'0'),
    ('收费项目更新', 'dm:fbs-fee-services:update', 3, 3, @fbsFeeServicesId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-08-19 17:18:21', '', '2024-08-19 17:18:21', b'0'),
    ('收费项目删除', 'dm:fbs-fee-services:delete', 3, 4, @fbsFeeServicesId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-08-19 17:18:21', '', '2024-08-19 17:18:21', b'0'),
    ('收费项目导出', 'dm:fbs-fee-services:export', 3, 5, @fbsFeeServicesId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-08-19 17:18:21', '', '2024-08-19 17:18:21', b'0');

-- 产品库存菜单
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, component_name, status, visible, keep_alive, always_show,
    creator, create_time, updater, update_time, deleted
)
VALUES (
    '产品库存', '', 2, 2, @warehouseManagementId,
    'fbs-product-stock', 'fa-solid:folder-open', 'dm/fbsstock/index', 'FbsProductStock', 0, b'1', b'1', b'1',
    '', '2024-08-29 09:59:07', '1', '2024-09-08 16:46:14', b'0'
);

-- 获取产品库存菜单ID
SELECT @fbsProductStockId := LAST_INSERT_ID();

-- 产品库存相关按钮
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, component_name, status, visible, keep_alive, always_show,
    creator, create_time, updater, update_time, deleted
)
VALUES 
    ('海外仓产品库存查询', 'dm:fbs-product-stock:query', 3, 1, @fbsProductStockId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-08-29 09:59:07', '', '2024-08-29 09:59:07', b'0'),
    ('海外仓产品库存创建', 'dm:fbs-product-stock:create', 3, 2, @fbsProductStockId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-08-29 09:59:07', '', '2024-08-29 09:59:07', b'0'),
    ('海外仓产品库存更新', 'dm:fbs-product-stock:update', 3, 3, @fbsProductStockId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-08-29 09:59:07', '', '2024-08-29 09:59:07', b'0'),
    ('海外仓产品库存删除', 'dm:fbs-product-stock:delete', 3, 4, @fbsProductStockId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-08-29 09:59:07', '', '2024-08-29 09:59:07', b'0'),
    ('海外仓产品库存导出', 'dm:fbs-product-stock:export', 3, 5, @fbsProductStockId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-08-29 09:59:07', '', '2024-08-29 09:59:07', b'0');

-- 推单记录管理菜单（已禁用）
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, component_name, status, visible, keep_alive, always_show,
    creator, create_time, updater, update_time, deleted
)
VALUES (
    '推单记录管理', '', 2, 99, @warehouseManagementId,
    'fbs-push-order-log', '', 'dm/warehouse/index', 'FbsPushOrderLog', 1, b'0', b'1', b'0',
    '', '2024-08-30 21:40:54', '1', '2024-09-08 13:31:54', b'0'
);

-- 获取推单记录管理菜单ID
SELECT @fbsPushOrderLogId := LAST_INSERT_ID();

-- 推单记录管理相关按钮
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, component_name, status, visible, keep_alive, always_show,
    creator, create_time, updater, update_time, deleted
)
VALUES 
    ('海外仓推单记录查询', 'dm:fbs-push-order-log:query', 3, 1, @fbsPushOrderLogId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-08-30 21:40:54', '', '2024-08-30 21:40:54', b'0'),
    ('海外仓推单记录创建', 'dm:fbs-push-order-log:create', 3, 2, @fbsPushOrderLogId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-08-30 21:40:54', '', '2024-08-30 21:40:54', b'0'),
    ('海外仓推单记录更新', 'dm:fbs-push-order-log:update', 3, 3, @fbsPushOrderLogId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-08-30 21:40:54', '', '2024-08-30 21:40:54', b'0'),
    ('海外仓推单记录删除', 'dm:fbs-push-order-log:delete', 3, 4, @fbsPushOrderLogId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-08-30 21:40:54', '', '2024-08-30 21:40:54', b'0'),
    ('海外仓推单记录导出', 'dm:fbs-push-order-log:export', 3, 5, @fbsPushOrderLogId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2024-08-30 21:40:54', '', '2024-08-30 21:40:54', b'0');

-- ===== 汇率管理模块 =====
-- 汇率管理菜单（在系统管理下）
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, component_name, status, visible, keep_alive, always_show,
    creator, create_time, updater, update_time, deleted
)
VALUES (
    '汇率管理', '', 2, 0, 1,
    'exchange-rates', 'fa-solid:money-bill-wave', 'dm/exchangerates/index', 'ExchangeRates', 0, b'1', b'1', b'1',
    '', '2025-03-23 20:43:53', '1', '2025-03-24 11:01:24', b'0'
);

-- 获取汇率管理菜单ID
SELECT @exchangeRatesId := LAST_INSERT_ID();

-- 汇率管理相关按钮
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, component_name, status, visible, keep_alive, always_show,
    creator, create_time, updater, update_time, deleted
)
VALUES 
    ('汇率查询', 'dm:exchange-rates:query', 3, 1, @exchangeRatesId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2025-03-23 20:43:53', '', '2025-03-23 20:43:53', b'0'),
    ('汇率创建', 'dm:exchange-rates:create', 3, 2, @exchangeRatesId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2025-03-23 20:43:53', '', '2025-03-23 20:43:53', b'0'),
    ('汇率更新', 'dm:exchange-rates:update', 3, 3, @exchangeRatesId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2025-03-23 20:43:53', '', '2025-03-23 20:43:53', b'0'),
    ('汇率删除', 'dm:exchange-rates:delete', 3, 4, @exchangeRatesId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2025-03-23 20:43:53', '', '2025-03-23 20:43:53', b'0'),
    ('汇率导出', 'dm:exchange-rates:export', 3, 5, @exchangeRatesId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2025-03-23 20:43:53', '', '2025-03-23 20:43:53', b'0');

-- ===== CouPang平台模块 =====
-- CouPang主菜单（在平台管理下）
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, component_name, status, visible, keep_alive, always_show,
    creator, create_time, updater, update_time, deleted
)
VALUES (
    'CouPang', '', 1, 9, @platformManagementId,
    'cp', 'ep:aim', '', '', 0, b'1', b'1', b'1',
    '1', '2025-02-07 21:14:53', '1', '2025-06-14 12:06:31', b'0'
);

-- 获取CouPang主菜单ID
SELECT @coupangManagementId := LAST_INSERT_ID();

-- 产品信息菜单
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, component_name, status, visible, keep_alive, always_show,
    creator, create_time, updater, update_time, deleted
)
VALUES (
    '产品信息', '', 2, 1, @coupangManagementId,
    'product-online', '', 'coupang/product/index', 'CoupangProductOnline', 0, b'1', b'1', b'1',
    '', '2025-02-08 13:27:26', '1', '2025-02-11 16:43:13', b'0'
);

-- 获取CouPang产品信息菜单ID
SELECT @coupangProductOnlineId := LAST_INSERT_ID();

-- CouPang产品信息相关按钮
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, component_name, status, visible, keep_alive, always_show,
    creator, create_time, updater, update_time, deleted
)
VALUES 
    ('产品信息查询', 'coupang:product-online:query', 3, 1, @coupangProductOnlineId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2025-02-08 13:27:26', '', '2025-02-08 13:27:26', b'0'),
    ('产品信息创建', 'coupang:product-online:create', 3, 2, @coupangProductOnlineId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2025-02-08 13:27:26', '', '2025-02-08 13:27:26', b'0'),
    ('产品信息更新', 'coupang:product-online:update', 3, 3, @coupangProductOnlineId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2025-02-08 13:27:26', '', '2025-02-08 13:27:26', b'0'),
    ('产品信息删除', 'coupang:product-online:delete', 3, 4, @coupangProductOnlineId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2025-02-08 13:27:26', '', '2025-02-08 13:27:26', b'0'),
    ('产品信息导出', 'coupang:product-online:export', 3, 5, @coupangProductOnlineId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2025-02-08 13:27:26', '', '2025-02-08 13:27:26', b'0');

-- 订单销售菜单
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, component_name, status, visible, keep_alive, always_show,
    creator, create_time, updater, update_time, deleted
)
VALUES (
    '订单销售', '', 2, 2, @coupangManagementId,
    'order-fee-details', '', 'coupang/order/index', 'CoupangOrderFeeDetails', 0, b'1', b'1', b'1',
    '', '2025-02-08 13:19:35', '1', '2025-02-08 13:28:35', b'0'
);

-- 获取订单销售菜单ID
SELECT @coupangOrderFeeDetailsId := LAST_INSERT_ID();

-- 订单销售相关按钮
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, component_name, status, visible, keep_alive, always_show,
    creator, create_time, updater, update_time, deleted
)
VALUES 
    ('订单销售手续费明细查询', 'coupang:order-fee-details:query', 3, 1, @coupangOrderFeeDetailsId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2025-02-08 13:19:35', '', '2025-02-08 13:19:35', b'0'),
    ('订单销售手续费明细创建', 'coupang:order-fee-details:create', 3, 2, @coupangOrderFeeDetailsId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2025-02-08 13:19:35', '', '2025-02-08 13:19:35', b'0'),
    ('订单销售手续费明细更新', 'coupang:order-fee-details:update', 3, 3, @coupangOrderFeeDetailsId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2025-02-08 13:19:35', '', '2025-02-08 13:19:35', b'0'),
    ('订单销售手续费明细删除', 'coupang:order-fee-details:delete', 3, 4, @coupangOrderFeeDetailsId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2025-02-08 13:19:35', '', '2025-02-08 13:19:35', b'0'),
    ('订单销售手续费明细导出', 'coupang:order-fee-details:export', 3, 5, @coupangOrderFeeDetailsId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2025-02-08 13:19:35', '', '2025-02-08 13:19:35', b'0');

-- 仓库操作费菜单
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, component_name, status, visible, keep_alive, always_show,
    creator, create_time, updater, update_time, deleted
)
VALUES (
    '仓库操作费', '', 2, 3, @coupangManagementId,
    'warehouse-operation-detail', '', 'coupang/warehouse/index', 'CoupangWarehouseOperationDetail', 0, b'1', b'1', b'1',
    '', '2025-02-08 13:25:31', '1', '2025-02-08 13:28:51', b'0'
);

-- 获取仓库操作费菜单ID
SELECT @coupangWarehouseOperationDetailId := LAST_INSERT_ID();

-- 仓库操作费相关按钮
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, component_name, status, visible, keep_alive, always_show,
    creator, create_time, updater, update_time, deleted
)
VALUES 
    ('仓库操作费明细查询', 'coupang:warehouse-operation-detail:query', 3, 1, @coupangWarehouseOperationDetailId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2025-02-08 13:25:31', '', '2025-02-08 13:25:31', b'0'),
    ('仓库操作费明细创建', 'coupang:warehouse-operation-detail:create', 3, 2, @coupangWarehouseOperationDetailId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2025-02-08 13:25:31', '', '2025-02-08 13:25:31', b'0'),
    ('仓库操作费明细更新', 'coupang:warehouse-operation-detail:update', 3, 3, @coupangWarehouseOperationDetailId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2025-02-08 13:25:31', '', '2025-02-08 13:25:31', b'0'),
    ('仓库操作费明细删除', 'coupang:warehouse-operation-detail:delete', 3, 4, @coupangWarehouseOperationDetailId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2025-02-08 13:25:31', '', '2025-02-08 13:25:31', b'0'),
    ('仓库操作费明细导出', 'coupang:warehouse-operation-detail:export', 3, 5, @coupangWarehouseOperationDetailId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2025-02-08 13:25:31', '', '2025-02-08 13:25:31', b'0');

-- 配送费菜单
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, component_name, status, visible, keep_alive, always_show,
    creator, create_time, updater, update_time, deleted
)
VALUES (
    '配送费', '', 2, 4, @coupangManagementId,
    'delivery-fee-detail', '', 'coupang/delivery/index', 'CoupangDeliveryFeeDetail', 0, b'1', b'1', b'1',
    '', '2025-02-08 13:21:19', '1', '2025-02-08 13:29:03', b'0'
);

-- 获取配送费菜单ID
SELECT @coupangDeliveryFeeDetailId := LAST_INSERT_ID();

-- 配送费相关按钮
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, component_name, status, visible, keep_alive, always_show,
    creator, create_time, updater, update_time, deleted
)
VALUES 
    ('配送费明细查询', 'coupang:delivery-fee-detail:query', 3, 1, @coupangDeliveryFeeDetailId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2025-02-08 13:21:19', '', '2025-02-08 13:21:19', b'0'),
    ('配送费明细创建', 'coupang:delivery-fee-detail:create', 3, 2, @coupangDeliveryFeeDetailId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2025-02-08 13:21:19', '', '2025-02-08 13:21:19', b'0'),
    ('配送费明细更新', 'coupang:delivery-fee-detail:update', 3, 3, @coupangDeliveryFeeDetailId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2025-02-08 13:21:19', '', '2025-02-08 13:21:19', b'0'),
    ('配送费明细删除', 'coupang:delivery-fee-detail:delete', 3, 4, @coupangDeliveryFeeDetailId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2025-02-08 13:21:19', '', '2025-02-08 13:21:19', b'0'),
    ('配送费明细导出', 'coupang:delivery-fee-detail:export', 3, 5, @coupangDeliveryFeeDetailId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2025-02-08 13:21:19', '', '2025-02-08 13:21:19', b'0');

-- 仓储费菜单
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, component_name, status, visible, keep_alive, always_show,
    creator, create_time, updater, update_time, deleted
)
VALUES (
    '仓储费', '', 2, 5, @coupangManagementId,
    'storage-fee-detail', '', 'coupang/storage/index', 'CoupangStorageFeeDetail', 0, b'1', b'1', b'1',
    '', '2025-02-08 13:13:17', '1', '2025-02-08 13:29:14', b'0'
);

-- 获取仓储费菜单ID
SELECT @coupangStorageFeeDetailId := LAST_INSERT_ID();

-- 仓储费相关按钮
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, component_name, status, visible, keep_alive, always_show,
    creator, create_time, updater, update_time, deleted
)
VALUES 
    ('仓储费明细查询', 'coupang:storage-fee-detail:query', 3, 1, @coupangStorageFeeDetailId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2025-02-08 13:13:17', '', '2025-02-08 13:13:17', b'0'),
    ('仓储费明细创建', 'coupang:storage-fee-detail:create', 3, 2, @coupangStorageFeeDetailId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2025-02-08 13:13:17', '', '2025-02-08 13:13:17', b'0'),
    ('仓储费明细更新', 'coupang:storage-fee-detail:update', 3, 3, @coupangStorageFeeDetailId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2025-02-08 13:13:17', '', '2025-02-08 13:13:17', b'0'),
    ('仓储费明细删除', 'coupang:storage-fee-detail:delete', 3, 4, @coupangStorageFeeDetailId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2025-02-08 13:13:17', '', '2025-02-08 13:13:17', b'0'),
    ('仓储费明细导出', 'coupang:storage-fee-detail:export', 3, 5, @coupangStorageFeeDetailId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2025-02-08 13:13:17', '', '2025-02-08 13:13:17', b'0');

-- 广告活动菜单
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, component_name, status, visible, keep_alive, always_show,
    creator, create_time, updater, update_time, deleted
)
VALUES (
    '广告活动', '', 2, 6, @coupangManagementId,
    'ad-campaign-report', '', 'coupang/ad/index', 'CoupangAdCampaignReport', 0, b'1', b'1', b'1',
    '', '2025-02-08 13:22:45', '1', '2025-02-08 13:29:23', b'0'
);

-- 获取广告活动菜单ID
SELECT @coupangAdCampaignReportId := LAST_INSERT_ID();

-- 广告活动相关按钮
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, component_name, status, visible, keep_alive, always_show,
    creator, create_time, updater, update_time, deleted
)
VALUES 
    ('广告活动报表查询', 'coupang:ad-campaign-report:query', 3, 1, @coupangAdCampaignReportId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2025-02-08 13:22:45', '', '2025-02-08 13:22:45', b'0'),
    ('广告活动报表创建', 'coupang:ad-campaign-report:create', 3, 2, @coupangAdCampaignReportId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2025-02-08 13:22:45', '', '2025-02-08 13:22:45', b'0'),
    ('广告活动报表更新', 'coupang:ad-campaign-report:update', 3, 3, @coupangAdCampaignReportId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2025-02-08 13:22:45', '', '2025-02-08 13:22:45', b'0'),
    ('广告活动报表删除', 'coupang:ad-campaign-report:delete', 3, 4, @coupangAdCampaignReportId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2025-02-08 13:22:45', '', '2025-02-08 13:22:45', b'0'),
    ('广告活动报表导出', 'coupang:ad-campaign-report:export', 3, 5, @coupangAdCampaignReportId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2025-02-08 13:22:45', '', '2025-02-08 13:22:45', b'0');

-- 库存成本菜单
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, component_name, status, visible, keep_alive, always_show,
    creator, create_time, updater, update_time, deleted
)
VALUES (
    '库存成本', 'coupang:data-report:query', 2, 7, @coupangManagementId,
    'coupang/stock', '', 'coupang/dataReport/inventoryCost', 'CoupangInventoryCost', 0, b'1', b'1', b'1',
    '1', '2025-08-04 22:13:23', '1', '2025-08-07 17:06:45', b'0'
);

-- 销售统计菜单
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, component_name, status, visible, keep_alive, always_show,
    creator, create_time, updater, update_time, deleted
)
VALUES (
    '销售统计', '', 2, 8, @coupangManagementId,
    'coupang/volume', '', 'coupang/dataReport/productSales', 'CoupangProductSales', 0, b'1', b'1', b'1',
    '1', '2025-08-05 13:39:10', '1', '2025-08-05 13:40:37', b'0'
);

-- 销售看板菜单
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, component_name, status, visible, keep_alive, always_show,
    creator, create_time, updater, update_time, deleted
)
VALUES (
    '销售看板', 'coupang:profit-report:query', 2, 0, @coupangManagementId,
    'coupang/home/index', '', 'coupang/report/index', 'CoupangReport', 0, b'1', b'1', b'1',
    '1', '2025-02-11 16:42:44', '1', '2025-08-07 17:11:01', b'0'
);

-- ===== 插件管理模块 =====
-- 插件管理主菜单
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, component_name, status, visible, keep_alive, always_show,
    creator, create_time, updater, update_time, deleted
)
VALUES (
    '插件管理', '', 1, 21, 0,
    '/chrome', 'fa-solid:tools', '', '', 0, b'1', b'1', b'1',
    '1', '2025-08-14 11:53:25', '1', '2025-08-14 11:53:25', b'0'
);

-- 获取插件管理主菜单ID
SELECT @chromeManagementId := LAST_INSERT_ID();

-- 套餐配置菜单
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, component_name, status, visible, keep_alive, always_show,
    creator, create_time, updater, update_time, deleted
)
VALUES (
    '套餐配置', '', 2, 1, @chromeManagementId,
    'subscription-plan', '', 'chrome/plan/index', 'SubscriptionPlan', 0, b'1', b'1', b'1',
    '', '2025-08-28 20:33:03', '1', '2025-08-29 15:07:33', b'0'
);

-- 获取套餐配置菜单ID
SELECT @subscriptionPlanId := LAST_INSERT_ID();

-- 套餐配置相关按钮
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, component_name, status, visible, keep_alive, always_show,
    creator, create_time, updater, update_time, deleted
)
VALUES 
    ('订阅套餐配置查询', 'chrome:subscription-plan:query', 3, 1, @subscriptionPlanId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2025-08-28 20:33:03', '', '2025-08-28 20:33:03', b'0'),
    ('订阅套餐配置创建', 'chrome:subscription-plan:create', 3, 2, @subscriptionPlanId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2025-08-28 20:33:03', '', '2025-08-28 20:33:03', b'0'),
    ('订阅套餐配置更新', 'chrome:subscription-plan:update', 3, 3, @subscriptionPlanId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2025-08-28 20:33:03', '', '2025-08-28 20:33:03', b'0'),
    ('订阅套餐配置删除', 'chrome:subscription-plan:delete', 3, 4, @subscriptionPlanId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2025-08-28 20:33:03', '', '2025-08-28 20:33:03', b'0'),
    ('订阅套餐配置导出', 'chrome:subscription-plan:export', 3, 5, @subscriptionPlanId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2025-08-28 20:33:03', '', '2025-08-28 20:33:03', b'0');

-- 订单管理菜单
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, component_name, status, visible, keep_alive, always_show,
    creator, create_time, updater, update_time, deleted
)
VALUES (
    '订单管理', '', 2, 2, @chromeManagementId,
    'subscription-order', '', 'chrome/order/index', 'SubscriptionOrder', 0, b'1', b'1', b'1',
    '', '2025-08-28 20:32:51', '1', '2025-08-29 15:33:49', b'0'
);

-- 获取订单管理菜单ID
SELECT @subscriptionOrderId := LAST_INSERT_ID();

-- 订单管理相关按钮
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, component_name, status, visible, keep_alive, always_show,
    creator, create_time, updater, update_time, deleted
)
VALUES 
    ('订阅订单查询', 'chrome:subscription-order:query', 3, 1, @subscriptionOrderId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2025-08-28 20:32:51', '', '2025-08-28 20:32:51', b'0'),
    ('订阅订单创建', 'chrome:subscription-order:create', 3, 2, @subscriptionOrderId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2025-08-28 20:32:51', '', '2025-08-28 20:32:51', b'0'),
    ('订阅订单更新', 'chrome:subscription-order:update', 3, 3, @subscriptionOrderId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2025-08-28 20:32:51', '', '2025-08-28 20:32:51', b'0'),
    ('订阅订单删除', 'chrome:subscription-order:delete', 3, 4, @subscriptionOrderId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2025-08-28 20:32:51', '', '2025-08-28 20:32:51', b'0'),
    ('订阅订单导出', 'chrome:subscription-order:export', 3, 5, @subscriptionOrderId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2025-08-28 20:32:51', '', '2025-08-28 20:32:51', b'0');

-- 订阅管理菜单
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, component_name, status, visible, keep_alive, always_show,
    creator, create_time, updater, update_time, deleted
)
VALUES (
    '订阅管理', '', 2, 3, @chromeManagementId,
    'subscription', '', 'chrome/subscription/index', 'Subscription', 0, b'1', b'1', b'1',
    '', '2025-08-14 16:41:36', '1', '2025-08-29 15:34:44', b'0'
);

-- 获取订阅管理菜单ID
SELECT @subscriptionId := LAST_INSERT_ID();

-- 订阅管理相关按钮
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, component_name, status, visible, keep_alive, always_show,
    creator, create_time, updater, update_time, deleted
)
VALUES 
    ('插件订阅查询', 'chrome:subscription:query', 3, 1, @subscriptionId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2025-08-14 16:41:36', '', '2025-08-14 16:41:36', b'0'),
    ('插件订阅创建', 'chrome:subscription:create', 3, 2, @subscriptionId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2025-08-14 16:41:36', '', '2025-08-14 16:41:36', b'0'),
    ('插件订阅更新', 'chrome:subscription:update', 3, 3, @subscriptionId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2025-08-14 16:41:36', '', '2025-08-14 16:41:36', b'0'),
    ('插件订阅删除', 'chrome:subscription:delete', 3, 4, @subscriptionId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2025-08-14 16:41:36', '', '2025-08-14 16:41:36', b'0'),
    ('插件订阅导出', 'chrome:subscription:export', 3, 5, @subscriptionId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2025-08-14 16:41:36', '', '2025-08-14 16:41:36', b'0');

-- 用户管理菜单
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, component_name, status, visible, keep_alive, always_show,
    creator, create_time, updater, update_time, deleted
)
VALUES (
    '用户管理', '', 2, 4, @chromeManagementId,
    'user', '', 'chrome/user/index', 'User', 0, b'1', b'1', b'1',
    '', '2025-08-14 16:26:57', '1', '2025-08-29 15:35:21', b'0'
);

-- 获取用户管理菜单ID
SELECT @chromeUserId := LAST_INSERT_ID();

-- 用户管理相关按钮
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, component_name, status, visible, keep_alive, always_show,
    creator, create_time, updater, update_time, deleted
)
VALUES 
    ('用户查询', 'chrome:user:query', 3, 1, @chromeUserId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2025-08-14 16:26:57', '', '2025-08-14 16:26:57', b'0'),
    ('用户创建', 'chrome:user:create', 3, 2, @chromeUserId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2025-08-14 16:26:57', '', '2025-08-14 16:26:57', b'0'),
    ('用户更新', 'chrome:user:update', 3, 3, @chromeUserId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2025-08-14 16:26:57', '', '2025-08-14 16:26:57', b'0'),
    ('用户删除', 'chrome:user:delete', 3, 4, @chromeUserId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2025-08-14 16:26:57', '', '2025-08-14 16:26:57', b'0'),
    ('用户导出', 'chrome:user:export', 3, 5, @chromeUserId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2025-08-14 16:26:57', '', '2025-08-14 16:26:57', b'0');

-- 积分账户菜单
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, component_name, status, visible, keep_alive, always_show,
    creator, create_time, updater, update_time, deleted
)
VALUES (
    '积分账户', '', 2, 5, @chromeManagementId,
    'user-credits', '', 'chrome/credits/index', 'UserCredits', 0, b'1', b'1', b'1',
    '', '2025-08-28 20:33:14', '1', '2025-08-29 15:35:59', b'0'
);

-- 获取积分账户菜单ID
SELECT @userCreditsId := LAST_INSERT_ID();

-- 积分账户相关按钮
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, component_name, status, visible, keep_alive, always_show,
    creator, create_time, updater, update_time, deleted
)
VALUES 
    ('用户积分账户查询', 'chrome:user-credits:query', 3, 1, @userCreditsId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2025-08-28 20:33:14', '', '2025-08-28 20:33:14', b'0'),
    ('用户积分账户创建', 'chrome:user-credits:create', 3, 2, @userCreditsId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2025-08-28 20:33:14', '', '2025-08-28 20:33:14', b'0'),
    ('用户积分账户更新', 'chrome:user-credits:update', 3, 3, @userCreditsId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2025-08-28 20:33:14', '', '2025-08-28 20:33:14', b'0'),
    ('用户积分账户删除', 'chrome:user-credits:delete', 3, 4, @userCreditsId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2025-08-28 20:33:14', '', '2025-08-28 20:33:14', b'0'),
    ('用户积分账户导出', 'chrome:user-credits:export', 3, 5, @userCreditsId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2025-08-28 20:33:14', '', '2025-08-28 20:33:14', b'0');

-- 积分交易菜单
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, component_name, status, visible, keep_alive, always_show,
    creator, create_time, updater, update_time, deleted
)
VALUES (
    '积分交易', '', 2, 6, @chromeManagementId,
    'credits-transaction', '', 'chrome/transaction/index', 'CreditsTransaction', 0, b'1', b'1', b'1',
    '', '2025-08-28 20:32:43', '1', '2025-08-29 15:36:06', b'0'
);

-- 获取积分交易菜单ID
SELECT @creditsTransactionId := LAST_INSERT_ID();

-- 积分交易相关按钮
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, component_name, status, visible, keep_alive, always_show,
    creator, create_time, updater, update_time, deleted
)
VALUES 
    ('积分交易记录查询', 'chrome:credits-transaction:query', 3, 1, @creditsTransactionId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2025-08-28 20:32:43', '', '2025-08-28 20:32:43', b'0'),
    ('积分交易记录创建', 'chrome:credits-transaction:create', 3, 2, @creditsTransactionId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2025-08-28 20:32:43', '', '2025-08-28 20:32:43', b'0'),
    ('积分交易记录更新', 'chrome:credits-transaction:update', 3, 3, @creditsTransactionId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2025-08-28 20:32:43', '', '2025-08-28 20:32:43', b'0'),
    ('积分交易记录删除', 'chrome:credits-transaction:delete', 3, 4, @creditsTransactionId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2025-08-28 20:32:43', '', '2025-08-28 20:32:43', b'0'),
    ('积分交易记录导出', 'chrome:credits-transaction:export', 3, 5, @creditsTransactionId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2025-08-28 20:32:43', '', '2025-08-28 20:32:43', b'0');

-- Cookie服务器菜单
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, component_name, status, visible, keep_alive, always_show,
    creator, create_time, updater, update_time, deleted
)
VALUES (
    'Cookie服务器', '', 2, 99, @chromeManagementId,
    'client-server', '', 'chrome/server/index', 'ClientServer', 0, b'1', b'1', b'1',
    '', '2025-08-14 16:10:34', '1', '2025-08-29 15:08:00', b'0'
);

-- 获取Cookie服务器菜单ID
SELECT @clientServerId := LAST_INSERT_ID();

-- Cookie服务器相关按钮
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, component_name, status, visible, keep_alive, always_show,
    creator, create_time, updater, update_time, deleted
)
VALUES 
    ('Chrome 插件cookie服务器查询', 'chrome:client-server:query', 3, 1, @clientServerId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2025-08-14 16:10:34', '', '2025-08-14 16:10:34', b'0'),
    ('Chrome 插件cookie服务器创建', 'chrome:client-server:create', 3, 2, @clientServerId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2025-08-14 16:10:34', '', '2025-08-14 16:10:34', b'0'),
    ('Chrome 插件cookie服务器更新', 'chrome:client-server:update', 3, 3, @clientServerId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2025-08-14 16:10:34', '', '2025-08-14 16:10:34', b'0'),
    ('Chrome 插件cookie服务器删除', 'chrome:client-server:delete', 3, 4, @clientServerId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2025-08-14 16:10:35', '', '2025-08-14 16:10:35', b'0'),
    ('Chrome 插件cookie服务器导出', 'chrome:client-server:export', 3, 5, @clientServerId, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2025-08-14 16:10:35', '', '2025-08-14 16:10:35', b'0');
