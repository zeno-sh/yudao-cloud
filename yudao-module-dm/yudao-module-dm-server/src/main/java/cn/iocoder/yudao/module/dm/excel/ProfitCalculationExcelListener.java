package cn.iocoder.yudao.module.dm.excel;

import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.common.util.spring.SpringUtils;
import cn.iocoder.yudao.framework.dict.core.DictFrameworkUtils;
import cn.iocoder.yudao.module.dm.controller.admin.calculation.vo.ProfitCalculationImportReqVO;
import cn.iocoder.yudao.module.dm.controller.admin.calculation.vo.ProfitCalculationSaveReqVO;
import cn.iocoder.yudao.module.dm.service.calculation.ProfitCalculationService;
import cn.iocoder.yudao.module.system.api.country.CountryApi;
import cn.iocoder.yudao.module.system.api.country.dto.CountryRespDTO;
import cn.iocoder.yudao.module.system.api.currency.CurrencyApi;
import cn.iocoder.yudao.module.system.api.currency.dto.CurrencyRespDTO;
import cn.iocoder.yudao.module.system.api.dict.DictDataApi;
import cn.idev.excel.context.AnalysisContext;
import cn.idev.excel.read.listener.ReadListener;
import lombok.Getter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 利润预测Excel导入监听器
 *
 * @author Zeno
 */
@Component
public class ProfitCalculationExcelListener implements ReadListener<ProfitCalculationImportReqVO> {

    // 获取错误信息
    @Getter
    private List<String> errorMessages = new ArrayList<>();
    
    // 有效数据列表
    private List<ProfitCalculationImportReqVO> validData = new ArrayList<>();

    @Override
    public void invoke(ProfitCalculationImportReqVO data, AnalysisContext context) {
        int index = context.readRowHolder().getRowIndex() + 1;

        // 记录当前行数据的错误
        List<String> currentRowErrors = new ArrayList<>();

        // 1. 使用JSR303校验注解进行基础校验
        Validator validator = SpringUtils.getBean(Validator.class);
        Set<ConstraintViolation<ProfitCalculationImportReqVO>> violations = validator.validate(data);
        for (ConstraintViolation<ProfitCalculationImportReqVO> violation : violations) {
            currentRowErrors.add(String.format("【%s】%s", getFieldDisplayName(violation.getPropertyPath().toString()), violation.getMessage()));
        }

        // 2. 业务逻辑校验（只保留复杂的业务校验，基础校验交给Bean Validation）
        validateBusinessLogic(data, currentRowErrors);

        // 如果当前行有错误，添加到错误列表
        if (!currentRowErrors.isEmpty()) {
            for (String error : currentRowErrors) {
                addErrorMessage(index, error);
            }
        } else {
            // 没有错误则添加到有效数据
            validData.add(data);
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        // 如果没有错误，则进行数据保存和利润计算
        if (CollectionUtils.isNotEmpty(validData) && errorMessages.isEmpty()) {
            ProfitCalculationService profitCalculationService = SpringUtils.getBean(ProfitCalculationService.class);
            CountryApi countryApi = SpringUtils.getBean(CountryApi.class);
            DictDataApi dictDataApi = SpringUtils.getBean(DictDataApi.class);

            for (ProfitCalculationImportReqVO importReqVO : validData) {
                try {
                    // 构建保存对象
                    ProfitCalculationSaveReqVO saveReqVO = buildSaveReqVO(importReqVO, countryApi, dictDataApi);
                    // 调用createProfitCalculation方法，该方法已经集成了利润计算逻辑
                    profitCalculationService.createProfitCalculation(saveReqVO);
                } catch (Exception e) {
                    // 如果保存过程中出现异常，记录错误
                    errorMessages.add(String.format("保存数据时发生错误：%s", e.getMessage()));
                }
            }
        }
    }

    /**
     * 业务逻辑校验（只保留复杂的业务校验）
     */
    private void validateBusinessLogic(ProfitCalculationImportReqVO data, List<String> currentRowErrors) {
        CountryApi countryApi = SpringUtils.getBean(CountryApi.class);
        CurrencyApi currencyApi = SpringUtils.getBean(CurrencyApi.class);

        // 校验国家是否存在
        if (StringUtils.isNotBlank(data.getCountry())) {
            try {
                CountryRespDTO country = countryApi.getCountryByName(data.getCountry()).getCheckedData();
                if (country == null) {
                    currentRowErrors.add(String.format("【国家】[%s]不存在", data.getCountry()));
                }
            } catch (Exception e) {
                currentRowErrors.add(String.format("【国家】[%s]校验失败：%s", data.getCountry(), e.getMessage()));
            }
        }

        // 校验币种是否有效（如果有填写）- 修复类型问题
        if (data.getCurrencyCode() != null) {
            try {
                CurrencyRespDTO currencyRespDTO = currencyApi.getCurrencyByCode(data.getCurrencyCode()).getCheckedData();
                if (currencyRespDTO == null) {
                    currentRowErrors.add(String.format("【币种】[%s]不存在", data.getCurrencyCode()));
                }
            } catch (Exception e) {
                currentRowErrors.add(String.format("【币种】[%s]校验失败：%s", data.getCurrencyCode(), e.getMessage()));
            }
        }

        // 校验平台是否有效（字典校验）
        if (data.getPlatform() != null) {
            try {
                String platformLabel = DictFrameworkUtils.parseDictDataLabel("dm_platform", data.getPlatform());
                if (StringUtils.isBlank(platformLabel)) {
                    currentRowErrors.add(String.format("【平台】[%s]不存在", data.getPlatform()));
                }
            } catch (Exception e) {
                currentRowErrors.add(String.format("【平台】[%s]校验失败：%s", data.getPlatform(), e.getMessage()));
            }
        }

        // 校验售价必须大于0（业务逻辑校验）
        if (data.getPrice() != null && data.getPrice().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            currentRowErrors.add("【售价】必须大于0");
        }

        // 校验产品尺寸逻辑完整性
        if (hasAnyProductDimension(data) && !hasAllProductDimensions(data)) {
            currentRowErrors.add("【产品尺寸】如果填写产品尺寸，长宽高必须全部填写");
        }
    }

    /**
     * 检查是否有任何产品尺寸字段被填写
     */
    private boolean hasAnyProductDimension(ProfitCalculationImportReqVO data) {
        return data.getProductLength() != null || 
               data.getProductWidth() != null || 
               data.getProductHeight() != null || 
               data.getProductWeight() != null;
    }

    /**
     * 检查是否所有必要的产品尺寸字段都被填写
     */
    private boolean hasAllProductDimensions(ProfitCalculationImportReqVO data) {
        return data.getProductLength() != null && 
               data.getProductWidth() != null && 
               data.getProductHeight() != null && 
               data.getProductWeight() != null;
    }

    /**
     * 构建保存对象
     */
    private ProfitCalculationSaveReqVO buildSaveReqVO(ProfitCalculationImportReqVO importReqVO, 
                                                      CountryApi countryApi, DictDataApi dictDataApi) {
        ProfitCalculationSaveReqVO saveReqVO = BeanUtils.toBean(importReqVO, ProfitCalculationSaveReqVO.class);
        
        // 设置国家代码
        if (StringUtils.isNotBlank(importReqVO.getCountry())) {
            CountryRespDTO country = countryApi.getCountryByName(importReqVO.getCountry()).getCheckedData();
            if (country != null) {
                saveReqVO.setCountry(country.getCountry());
            }
        }

        return saveReqVO;
    }

    /**
     * 获取字段显示名称
     */
    private String getFieldDisplayName(String propertyPath) {
        switch (propertyPath) {
            case "productId": return "本地产品ID";
            case "planName": return "选品计划名称";
            case "platform": return "平台";
            case "country": return "国家";
            case "templateId": return "配置模板ID";
            case "sku": return "产品SKU";
            case "productLength": return "产品长度(cm)";
            case "productWidth": return "产品宽度(cm)";
            case "productHeight": return "产品高度(cm)";
            case "productWeight": return "产品重量(kg)";
            case "price": return "售价";
            case "currency": return "币种";
            case "exchangeRate": return "汇率";
            case "purchaseCost": return "采购价（CNY，不含税）";
            case "deliveryCost": return "配送费";
            case "storageCost": return "仓储费";
            default: return propertyPath;
        }
    }

    /**
     * 添加错误信息的方法
     */
    private void addErrorMessage(int index, String message) {
        errorMessages.add(String.format("第%d行：%s", index, message));
    }

}