package cn.iocoder.yudao.module.dm.service.calculation;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.dm.controller.admin.calculation.vo.ProfitCalculationImportReqVO;
import cn.iocoder.yudao.module.dm.controller.admin.calculation.vo.ProfitCalculationPageReqVO;
import cn.iocoder.yudao.module.dm.controller.admin.calculation.vo.ProfitCalculationSaveReqVO;
import cn.iocoder.yudao.module.dm.dal.dataobject.calculation.ProfitCalculationDO;
import cn.iocoder.yudao.module.dm.dal.mysql.calculation.ProfitCalculationMapper;
import cn.iocoder.yudao.module.dm.service.calculation.dto.ProfitCalculationResultDTO;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.dm.enums.ErrorCodeConstants.PROFIT_CALCULATION_NOT_EXISTS;

/**
 * 利润预测 Service 实现类
 *
 * @author Zeno
 */
@Service
@Validated
public class ProfitCalculationServiceImpl implements ProfitCalculationService {

    @Resource
    private ProfitCalculationMapper profitCalculationMapper;

    @Resource
    private ProfitCalculationAlgorithmService profitCalculationAlgorithmService;



    @Override
    public Long createProfitCalculation(ProfitCalculationSaveReqVO createReqVO) {
        // 1. 直接执行利润计算
        ProfitCalculationResultDTO calculationResultDTO = profitCalculationAlgorithmService.calculateProfit(createReqVO);

        // 2. 构建数据对象并保存计算结果
        ProfitCalculationDO profitCalculation = BeanUtils.toBean(calculationResultDTO, ProfitCalculationDO.class);

        // 3. 插入数据库
        profitCalculationMapper.insert(profitCalculation);

        // 返回
        return profitCalculation.getId();
    }



    @Override
    public void updateProfitCalculation(ProfitCalculationSaveReqVO updateReqVO) {
        // 校验存在
        validateProfitCalculationExists(updateReqVO.getId());
        
        // 1. 执行利润计算
        ProfitCalculationResultDTO calculationResultDTO = profitCalculationAlgorithmService.calculateProfit(updateReqVO);
        
        // 2. 构建数据对象并保存计算结果
        ProfitCalculationDO updateObj = BeanUtils.toBean(calculationResultDTO, ProfitCalculationDO.class);
        updateObj.setId(updateReqVO.getId()); // 保持原有ID
        
        // 3. 更新数据库
        profitCalculationMapper.updateById(updateObj);
    }

    @Override
    public void deleteProfitCalculation(Long id) {
        // 校验存在
        validateProfitCalculationExists(id);
        // 删除
        profitCalculationMapper.deleteById(id);
    }

    private void validateProfitCalculationExists(Long id) {
        if (profitCalculationMapper.selectById(id) == null) {
            throw exception(PROFIT_CALCULATION_NOT_EXISTS);
        }
    }

    @Override
    public ProfitCalculationDO getProfitCalculation(Long id) {
        return profitCalculationMapper.selectById(id);
    }

    @Override
    public PageResult<ProfitCalculationDO> getProfitCalculationPage(ProfitCalculationPageReqVO pageReqVO) {
        return profitCalculationMapper.selectPage(pageReqVO);
    }

    @Override
    public void getImportTemplate(HttpServletResponse response) throws IOException {
        // 创建示例数据
        List<ProfitCalculationImportReqVO> templateData = new ArrayList<>();

        // 输出模板
        ExcelUtils.write(response, "利润预测导入模板.xls", "利润预测列表",
                ProfitCalculationImportReqVO.class, templateData);
    }

}