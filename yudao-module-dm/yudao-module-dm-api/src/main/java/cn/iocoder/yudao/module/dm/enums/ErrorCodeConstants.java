package cn.iocoder.yudao.module.dm.enums;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;

public interface ErrorCodeConstants {

    // ==========  大麦系统 模块 2-001-000-000 ==========


    // ==========  产品 模块 2-001-001-000 ==========
    ErrorCode PRODUCT_INFO_NOT_EXISTS = new ErrorCode(2_001_001_000, "产品信息不存在");
    ErrorCode PRODUCT_INFO_SELECTION_PLAN_EXISTS = new ErrorCode(2_001_001_001, "存在选品计划，不可以删除");
    ErrorCode PRODUCT_INFO_PURCHASE_PLAN_EXISTS = new ErrorCode(2_001_001_002, "存在采购计划，不可以删除");
    ErrorCode PRODUCT_INFO_PURCHASE_ORDER_EXISTS = new ErrorCode(2_001_001_003, "存在采购单，不可以删除");
    ErrorCode PRODUCT_INFO_TRANSPORT_EXISTS = new ErrorCode(2_001_001_004, "存在发货记录，不可以删除");
    ErrorCode PRODUCT_INFO_SKU_EXISTS = new ErrorCode(2_001_001_005, "已存在相同的Sku");
    ErrorCode PRODUCT_COSTS_NOT_EXISTS = new ErrorCode(2_001_001_006, "产品成本结构不存在");
    
    // ========== 组合产品相关错误码（新增）==========
    ErrorCode BUNDLE_TYPE_REQUIRED = new ErrorCode(2_001_001_100, "组合产品必须指定组合类型");
    ErrorCode COST_PRICE_REQUIRED = new ErrorCode(2_001_001_101, "自定义成本价模式时，成本价不能为空");
    ErrorCode BUNDLE_ITEMS_REQUIRED = new ErrorCode(2_001_001_102, "组合产品明细列表不能为空");
    ErrorCode SUB_PRODUCT_NOT_EXISTS = new ErrorCode(2_001_001_103, "子产品ID[{}]不存在");
    ErrorCode BUNDLE_PRODUCT_CANNOT_NEST = new ErrorCode(2_001_001_104, "组合产品不能嵌套，子产品不能是组合产品");
    ErrorCode PRODUCT_NOT_BUNDLE = new ErrorCode(2_001_001_105, "该产品不是组合产品");
    ErrorCode BUNDLE_PRODUCT_NOT_EXISTS = new ErrorCode(2_001_001_106, "组合产品不存在");

    // ========== 类目佣金 模块 2-001-002-000 ==========
    ErrorCode CATEGORY_COMMISSION_NOT_EXISTS = new ErrorCode(2_001_002_000, "类目佣金不存在");
    ErrorCode CATEGORY_COMMISSION_EXITS_CHILDREN = new ErrorCode(2_001_002_001, "存在存在子类目佣金，无法删除");
    ErrorCode CATEGORY_COMMISSION_PARENT_NOT_EXITS = new ErrorCode(2_001_002_002, "父级类目佣金不存在");
    ErrorCode CATEGORY_COMMISSION_PARENT_ERROR = new ErrorCode(2_001_002_003, "不能设置自己为父类目佣金");
    ErrorCode CATEGORY_COMMISSION_CATEGORY_NAME_DUPLICATE = new ErrorCode(2_001_002_004, "已经存在该类目名称的类目佣金");
    ErrorCode CATEGORY_COMMISSION_PARENT_IS_CHILD = new ErrorCode(2_001_002_005, "不能设置自己的子CategoryCommission为父CategoryCommission");


    // ========== 供应商 模块 2-001-003-000 ==========
    ErrorCode PRODUCT_SUPPLIER_NOT_EXISTS = new ErrorCode(2_001_003_000, "供应商信息不存在");


    // ========== 产品 模块 2-001-004-000 ==========
    ErrorCode PRODUCT_CATEGORY_NOT_EXISTS = new ErrorCode(2_001_004_000, "产品分类不存在");
    ErrorCode PRODUCT_CATEGORY_EXITS_CHILDREN = new ErrorCode(2_001_004_001, "存在存在子产品分类，无法删除");
    ErrorCode PRODUCT_CATEGORY_PARENT_NOT_EXITS = new ErrorCode(2_001_004_002,"父级产品分类不存在");
    ErrorCode PRODUCT_CATEGORY_PARENT_ERROR = new ErrorCode(2_001_004_003, "不能设置自己为父产品分类");
    ErrorCode PRODUCT_CATEGORY_NAME_DUPLICATE = new ErrorCode(2_001_004_004, "已经存在该分类名称的产品分类");
    ErrorCode PRODUCT_CATEGORY_PARENT_IS_CHILD = new ErrorCode(2_001_004_005, "不能设置自己的子DmProductCategory为父DmProductCategory");

    // ========== 品牌 模块 2-001-005-000 ==========
    ErrorCode PRODUCT_BRAND_NOT_EXISTS = new ErrorCode(2_001_005_000, "品牌信息不存在");

    // ========== 选品 模块 2-001-006-000 ==========
    ErrorCode PRODUCT_SELECTION_PLAN_NOT_EXISTS = new ErrorCode(2_001_006_000, "选品计划不存在");
    ErrorCode PRODUCT_SELECTION_PLAN_FAIL = new ErrorCode(2_001_006_001, "一键生成选品计划失败");
    ErrorCode PRODUCT_SELECTION_PRICE_STRATEGY_NOT_EXIST = new ErrorCode(2_001_006_002, "未设置价格策略");
    ErrorCode PRODUCT_SELECTION_SUPPLIER_PRICE_OFFER_NOT_EXIST = new ErrorCode(2_001_001_003, "未设置供应商报价");
    ErrorCode PRODUCT_SELECTION_PURCHASE_NOT_EXIST = new ErrorCode(2_001_001_004, "未设置采购规格");
    ErrorCode PRODUCT_SELECTION_PLAN_CATEGORY_COMMISSION_NOT_EXIST = new ErrorCode(2_001_001_005, "未设置类目佣金");
    // ========== 采购 模块 2-001-007-000 ==========
    ErrorCode PURCHASE_PLAN_NOT_EXISTS = new ErrorCode(2_001_007_000, "采购计划不存在");
    ErrorCode PURCHASE_ORDER_NOT_EXISTS = new ErrorCode(2_001_007_001, "采购单不存在");
    ErrorCode PURCHASE_ORDER_ARRIVED_LOG_NOT_EXISTS = new ErrorCode(2_001_007_002, "采购单到货日志不存在");

    ErrorCode PURCHASE_ORDER_ARRIVED_LOG_EXCEED = new ErrorCode(2_001_007_003, "到货数量不得超过未到货数量");
    ErrorCode PURCHASE_ORDER_ARRIVED_LOG_EXCEED_ITEM = new ErrorCode(2_001_007_004, "到货数量({})超过({})未到货数量({})");
    ErrorCode PURCHASE_PLAN_IMPORT_ERROR = new ErrorCode(2_001_007_005, "导入数据不正确,错误行({})");
    ErrorCode PURCHASE_PLAN_IMPORT_EMPTY_ERROR = new ErrorCode(2_001_007_006, "导入数据为空");
    ErrorCode PURCHASE_PLAN_LIFE_EMPTY_ERROR = new ErrorCode(2_001_007_007, "查询参数不能全部为空");
    ErrorCode PURCHASE_PLAN_ITEM_NOT_ALLOW_CANCEL = new ErrorCode(2_001_007_008, "此状态下不允许作废");

    // ========== 财务 模块 2-001-008-000 ==========
    ErrorCode FINANCE_PAYMENT_NOT_EXISTS = new ErrorCode(2_001_008_000, "付款单不存在");
    ErrorCode FINANCE_PAYMENT_NO_EXISTS = new ErrorCode(2_001_008_001, "生成付款单号失败，请重新提交");
    ErrorCode PURCHASE_IN_FAIL_PAYMENT_PRICE_EXCEED = new ErrorCode(2_001_008_002, "付款金额({})超过采购单总金额({})");
    ErrorCode FINANCE_PAYMENT_UPDATE_FAIL_APPROVE = new ErrorCode(2_001_008_003, "付款单({})已审核，无法删除");

    // ========== 发货 模块 2-001-009-000 ==========
    ErrorCode TRANSPORT_PLAN_NOT_EXISTS = new ErrorCode(2_001_009_000, "头程计划不存在");

    ErrorCode TRANSPORT_PLAN_ITEM_QUANTITY_NOT_ENOUGH = new ErrorCode(2_001_009_001, "产品({})发货数量({})超过到货数量({})");

    // ========== 授权 模块 2-001-010-000 ==========
    ErrorCode OZON_SHOP_MAPPING_NOT_EXISTS = new ErrorCode(2_001_010_000, "店铺不存在");
    ErrorCode OZON_AD_CONFIG_NOT_EXISTS = new ErrorCode(2_001_012_001, "获取Token失败，门店Api未配置");
    ErrorCode OZON_SHOP_MAPPING_NO_AUTH = new ErrorCode(2_001_012_002, "未获取到授权门店");
    ErrorCode OZON_SHOP_MAPPING_EXISTS = new ErrorCode(2_001_012_003, "店铺已存在");

    // ========== 订单中心 模块 2-001-011-000 ==========
    ErrorCode OZON_ORDER_NOT_EXISTS = new ErrorCode(2_001_011_000, "Ozon订单不存在");
    ErrorCode OZON_PRODUCT_ONLINE_NOT_EXISTS = new ErrorCode(2_001_011_001, "在线商品不存在");
    ErrorCode OZON_PRODUCT_ONLINE_MAPPING_NOT_EXISTS = new ErrorCode(2_001_011_001, "在线商品映射不存在");
    ErrorCode OZON_PRODUCT_ONLINE_MAPPING_EXISTS = new ErrorCode(2_001_011_001, "在线商品映射已存在，不可以重复配对");

    // ========== 数据中心 模块 2-001-012-000 ==========
    ErrorCode OZON_AD_CAMPAIGNS_NOT_EXISTS = new ErrorCode(2_001_012_000, "广告活动不存在");

    // ========== 数据中心 模块 2-001-013-000 ==========
    ErrorCode OZON_FINANCE_TRANSACTION_NOT_EXISTS = new ErrorCode(2_001_013_000, "交易记录不存在");

    // ========== WB 模块 2-001-014-000 ==========
    ErrorCode WB_HTTP_ERROR = new ErrorCode(2_001_014_000, "WB请求异常");

    // ========== 海外仓 模块 2-001-015-000 ==========
    ErrorCode FBS_WAREHOUSE_NOT_EXISTS = new ErrorCode(2_001_015_000, "海外仓仓库不存在");
    ErrorCode FBS_FEE_SERVICES_NOT_EXISTS = new ErrorCode(2_001_015_001, "收费项目不存在");
    ErrorCode FBS_PRODUCT_STOCK_NOT_EXISTS = new ErrorCode(2_001_015_002, "海外仓产品库存不存在");
    ErrorCode FBS_PRODUCT_STOCK_MAPPING_NOT_EXISTS = new ErrorCode(2_001_015_004, "海外仓产品映射不存在");
    ErrorCode FBS_WAREHOUSE_MAPPING_NOT_EXISTS = new ErrorCode(2_001_015_005, "海外仓映射不存在");
    ErrorCode FBS_WAREHOUSE_AUTH_NOT_EXISTS = new ErrorCode(2_001_015_006, "海外仓授权信息不存在");
    ErrorCode FBS_WAREHOUSE_PRODUCT_MAPPING_EXISTS = new ErrorCode(2_001_015_007, "海外仓产品映射已存在，不可以重复配对");

    // ========== 海外仓 模块 2-001-016-000 ==========
    ErrorCode OZON_SUPPLY_ORDER_NOT_EXISTS = new ErrorCode(2_001_016_000, "供应订单不存在");
    ErrorCode OZON_SUPPLY_ORDER_EXISTS = new ErrorCode(2_001_016_001, "供应订单已存在");

    // ========== 汇率配置 模块 2-001-017-000 ==========
    ErrorCode EXCHANGE_RATES_NOT_EXISTS = new ErrorCode(2_001_017_000, "汇率不存在");

    // ========== Ozon广告同步任务 模块 2-001-018-000 ==========
    ErrorCode OZON_AD_SYNC_TASK_ERROR = new ErrorCode(2_001_018_000, "Ozon广告同步错误：{}");

    // ========== 利润预测 1-003-001-000 ==========
    ErrorCode PROFIT_CALCULATION_NOT_EXISTS = new ErrorCode(2_001_019_000, "利润预测不存在");
    ErrorCode PROFIT_CALCULATION_IMPORT_LIST_IS_EMPTY = new ErrorCode(2_001_019_001, "导入利润预测数据不能为空！");
    ErrorCode PROFIT_CALCULATION_TEMPLATE_NOT_EXISTS = new ErrorCode(2_001_019_002, "导入利润模板不存在");

    // ========== 利润计算算法相关错误码 1-003-001-003 ~ 1-003-001-020 ==========
    ErrorCode PROFIT_CALCULATION_REQUEST_NULL = new ErrorCode(2_001_019_003, "利润计算请求参数不能为空");
    ErrorCode PROFIT_CALCULATION_PRODUCT_ID_NULL = new ErrorCode(2_001_019_004, "产品ID不能为空");
    ErrorCode PROFIT_CALCULATION_COUNTRY_NULL = new ErrorCode(2_001_019_005, "国家不能为空");
    ErrorCode PROFIT_CALCULATION_PLATFORM_NULL = new ErrorCode(2_001_019_006, "平台不能为空");
    ErrorCode PROFIT_CALCULATION_PRICE_INVALID = new ErrorCode(2_001_019_007, "商品价格必须大于0");
    ErrorCode PROFIT_CALCULATION_PURCHASE_COST_INVALID = new ErrorCode(2_001_019_008, "采购成本必须大于0");
    ErrorCode PROFIT_CALCULATION_PRODUCT_SIZE_INVALID = new ErrorCode(2_001_019_009, "产品尺寸必须大于0");
    ErrorCode PROFIT_CALCULATION_PRODUCT_WEIGHT_INVALID = new ErrorCode(2_001_019_010, "产品重量必须大于0");
    ErrorCode PROFIT_CALCULATION_COUNTRY_NOT_SUPPORTED = new ErrorCode(2_001_019_011, "不支持的国家: {}");
    ErrorCode PROFIT_CALCULATION_TEMPLATE_NOT_FOUND = new ErrorCode(2_001_019_012, "未找到模板ID为{}的利润计算模板");
    ErrorCode PROFIT_CALCULATION_TEMPLATE_NOT_FOUND_FOR_COUNTRY = new ErrorCode(2_001_019_013, "未找到国家{}平台{}的默认利润计算模板");

}


