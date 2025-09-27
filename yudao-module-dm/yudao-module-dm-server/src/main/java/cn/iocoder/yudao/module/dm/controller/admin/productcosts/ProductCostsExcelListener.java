package cn.iocoder.yudao.module.dm.controller.admin.productcosts;

import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.common.util.spring.SpringUtils;
import cn.iocoder.yudao.framework.dict.core.DictFrameworkUtils;
import cn.iocoder.yudao.module.dm.controller.admin.productcosts.vo.ProductCostsExcelVO;
import cn.iocoder.yudao.module.dm.controller.admin.productcosts.vo.ProductCostsSaveReqVO;
import cn.iocoder.yudao.module.dm.dal.dataobject.commission.CategoryCommissionDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.product.ProductInfoDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.productcosts.ProductCostsDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.warehouse.FbsWarehouseDO;
import cn.iocoder.yudao.module.dm.service.commission.CategoryCommissionService;
import cn.iocoder.yudao.module.dm.service.product.ProductInfoService;
import cn.iocoder.yudao.module.dm.service.productcosts.ProductCostsService;
import cn.iocoder.yudao.module.dm.service.warehouse.FbsWarehouseService;
import lombok.Getter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;

/**
 * @author: Zeno
 * @createTime: 2024/10/05 18:07
 */
@Component
public class ProductCostsExcelListener {

    // 获取错误信息
    @Getter
    private List<String> errorMessages = new ArrayList<>();
    private List<ProductCostsExcelVO> validData = new ArrayList<>();

    /**
     * 处理 Excel 数据的主方法
     * @param dataList Excel 读取的数据列表
     * @return 错误信息列表
     */
    public List<String> processData(List<ProductCostsExcelVO> dataList) {
        // 重置错误信息和有效数据
        errorMessages.clear();
        validData.clear();
        
        ProductInfoService productInfoService = SpringUtils.getBean(ProductInfoService.class);
        CategoryCommissionService categoryCommissionService = SpringUtils.getBean(CategoryCommissionService.class);
        FbsWarehouseService fbsWarehouseService = SpringUtils.getBean(FbsWarehouseService.class);
        
        // 处理每一行数据
        for (int i = 0; i < dataList.size(); i++) {
            ProductCostsExcelVO data = dataList.get(i);
            int index = i + 2; // Excel 行号从2开始（第1行是标题）
            
            String skuId = data.getSkuId();
            
            // 校验 SKU ID
            ProductInfoDO productInfo = productInfoService.getProductInfoBySkuId(skuId);
            if (productInfo == null) {
                addErrorMessage(index, "【SkuId】不存在");
            }

            // 校验逻辑
            validateDictData(index, "【目标平台】非法", "dm_platform", data.getPlatform());
            if (data.getPurchaseCost() != null) {
                validateDictData(index, "【采购成本币种】非法", "dm_currency_code", data.getPurchaseCurrency());
            }
            if (data.getPurchaseShippingCost() != null) {
                validateDictData(index, "【采购运费单位】非法", "dm_fbs_unit_type", data.getPurchaseShippingUnit());
            }
            if (data.getLogisticsShippingCost() != null) {
                validateDictData(index, "【头程成本币种】非法", "dm_currency_code", data.getLogisticsCurrency());
                validateDictData(index, "【头程运费计费单位】非法", "dm_fbs_unit_type", data.getLogisticsUnit());
            }
            if (data.getCustomsCurrency() != null) {
                validateDictData(index, "【海关申报价币种】非法", "dm_currency_code", data.getCustomsCurrency());
            }
            if (data.getFboCurrency() != null) {
                validateDictData(index, "【FBO币种】非法", "dm_currency_code", data.getFboCurrency());
            }
            if (data.getFboDeliveryCostUnit() != null) {
                validateDictData(index, "【FBO送仓费计费单位】非法", "dm_fbs_unit_type", data.getFboDeliveryCostUnit());
            }
            if (data.getFboInspectionCostUnit() != null) {
                validateDictData(index, "【FBO验收费计费单位】非法", "dm_fbs_unit_type", data.getFboInspectionCostUnit());
            }
            if (data.getPlatformCurrency() != null) {
                validateDictData(index, "【平台成本币种】非法", "dm_currency_code", data.getPlatformCurrency());
            }
            if (data.getCategoryCommissionId() == null) {
                addErrorMessage(index, "【类目佣金ID】为空");
            } else {
                CategoryCommissionDO categoryCommission = categoryCommissionService.getCategoryCommission(data.getCategoryCommissionId());
                if (categoryCommission == null) {
                    addErrorMessage(index, "【类目佣金ID】非法");
                }
            }

            List<Long> fbsWarehouseIds = convertToLongList(data.getFbsWarehouseIds());
            if (CollectionUtils.isNotEmpty(fbsWarehouseIds)) {
                List<FbsWarehouseDO> fbsWarehouseDOS = fbsWarehouseService.batchFbsWarehouse(fbsWarehouseIds);
                Map<Long, FbsWarehouseDO> warehouseDOMap = convertMap(fbsWarehouseDOS, FbsWarehouseDO::getId);
                for (Long fbsWarehouseId : fbsWarehouseIds) {
                    if (!warehouseDOMap.containsKey(fbsWarehouseId)) {
                        addErrorMessage(index, "【FBS仓ID】非法");
                    }
                }
            }

            // 如果当前行没有错误，将数据加入有效数据列表
            if (!hasErrorForCurrentRow(index)) {
                validData.add(data);
            }
        }
        
        // 处理所有有效数据
        processValidData();
        
        return errorMessages;
    }

    /**
     * 处理所有有效数据
     */
    private void processValidData() {
        ProductInfoService productInfoService = SpringUtils.getBean(ProductInfoService.class);
        ProductCostsService productCostsService = SpringUtils.getBean(ProductCostsService.class);
        if (CollectionUtils.isNotEmpty(validData)) {
            for (ProductCostsExcelVO productCostsExcelVO : validData) {
                String skuId = productCostsExcelVO.getSkuId();
                ProductInfoDO productInfo = productInfoService.getProductInfoBySkuId(skuId);
                Long productId = productInfo.getId();
                productCostsExcelVO.setProductId(productId);
                
                // 检查是否存在相同的 productId + platform 组合
                ProductCostsDO existingCosts = productCostsService.getProductCostsByProductIdAndPlatform(productId, productCostsExcelVO.getPlatform());
                
                String fbsWarehouseIds = productCostsExcelVO.getFbsWarehouseIds();
                ProductCostsSaveReqVO saveReqVO = BeanUtils.toBean(productCostsExcelVO, ProductCostsSaveReqVO.class, productCostsSaveReqVO ->
                        productCostsSaveReqVO.setFbsWarehouseIds(convertToLongList(fbsWarehouseIds))
                );
                
                if (existingCosts != null) {
                    // 存在相同组合，执行更新操作
                    saveReqVO.setId(existingCosts.getId());
                    productCostsService.updateProductCosts(saveReqVO);
                } else {
                    // 不存在相同组合，执行插入操作
                    productCostsService.createProductCosts(saveReqVO);
                }
            }
        }
    }

    public List<Long> convertToLongList(String fbsWarehouseIdsStr) {
        // 如果字符串为空或null，返回一个空列表
        if (StringUtils.isBlank(fbsWarehouseIdsStr)) {
            return new ArrayList<>();
        }

        // 使用逗号分割字符串并转换为 Long 类型的列表
        return Arrays.stream(fbsWarehouseIdsStr.split(","))
                .map(String::trim) // 去除每个元素的前后空格
                .filter(StringUtils::isNotBlank) // 过滤掉空字符串
                .map(this::safeParseLong) // 使用安全转换方法
                .filter(java.util.Objects::nonNull) // 过滤掉转换失败的项（即null）
                .collect(Collectors.toList());
    }

    /**
     * 安全的 Long 转换方法，无法转换时返回 null
     */
    private Long safeParseLong(String str) {
        try {
            return Long.valueOf(str);
        } catch (NumberFormatException e) {
            // 如果转换失败，返回 null 以便后续过滤
            return null;
        }
    }

    /**
     * 通用字典数据校验方法
     */
    private void validateDictData(int index, String errorMessage, String dictType, Integer value) {
        if (StringUtils.isBlank(DictFrameworkUtils.parseDictDataLabel(dictType, value))) {
            addErrorMessage(index, errorMessage);
        }
    }

    /**
     * 添加错误信息的方法
     */
    private void addErrorMessage(int index, String message) {
        errorMessages.add(String.format("第%d行：%s", index, message));
    }
    
    /**
     * 检查当前行是否有错误
     */
    private boolean hasErrorForCurrentRow(int index) {
        String prefix = String.format("第%d行：", index);
        return errorMessages.stream().anyMatch(msg -> msg.startsWith(prefix));
    }

}
