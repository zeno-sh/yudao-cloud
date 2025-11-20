package cn.iocoder.yudao.module.dm.service.template;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.dm.controller.admin.template.vo.ProfitCalculationTemplatePageReqVO;
import cn.iocoder.yudao.module.dm.controller.admin.template.vo.ProfitCalculationTemplateSaveReqVO;
import cn.iocoder.yudao.module.dm.dal.dataobject.template.ProfitCalculationTemplateDO;
import cn.iocoder.yudao.module.dm.dal.mysql.template.ProfitCalculationTemplateMapper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.dm.enums.ErrorCodeConstants.PROFIT_CALCULATION_TEMPLATE_NOT_EXISTS;


/**
 * 利润计算配置模板 Service 实现类
 *
 * @author Zeno
 */
@Service
@Validated
public class ProfitCalculationTemplateServiceImpl implements ProfitCalculationTemplateService {

    @Resource
    private ProfitCalculationTemplateMapper profitCalculationTemplateMapper;

    @Override
    public Long createProfitCalculationTemplate(ProfitCalculationTemplateSaveReqVO createReqVO) {
        // 插入
        ProfitCalculationTemplateDO profitCalculationTemplate = BeanUtils.toBean(createReqVO, ProfitCalculationTemplateDO.class);
        profitCalculationTemplateMapper.insert(profitCalculationTemplate);
        // 返回
        return profitCalculationTemplate.getId();
    }

    @Override
    public void updateProfitCalculationTemplate(ProfitCalculationTemplateSaveReqVO updateReqVO) {
        // 校验存在
        validateProfitCalculationTemplateExists(updateReqVO.getId());
        // 更新
        ProfitCalculationTemplateDO updateObj = BeanUtils.toBean(updateReqVO, ProfitCalculationTemplateDO.class);
        profitCalculationTemplateMapper.updateById(updateObj);
    }

    @Override
    public void deleteProfitCalculationTemplate(Long id) {
        // 校验存在
        validateProfitCalculationTemplateExists(id);
        // 删除
        profitCalculationTemplateMapper.deleteById(id);
    }

    private void validateProfitCalculationTemplateExists(Long id) {
        if (profitCalculationTemplateMapper.selectById(id) == null) {
            throw exception(PROFIT_CALCULATION_TEMPLATE_NOT_EXISTS);
        }
    }

    @Override
    public ProfitCalculationTemplateDO getProfitCalculationTemplate(Long id) {
        return profitCalculationTemplateMapper.selectById(id);
    }

    @Override
    public PageResult<ProfitCalculationTemplateDO> getProfitCalculationTemplatePage(ProfitCalculationTemplatePageReqVO pageReqVO) {
        return profitCalculationTemplateMapper.selectPage(pageReqVO);
    }

    @Override
    public List<ProfitCalculationTemplateDO> getTemplatesByCountry(String country) {
        return profitCalculationTemplateMapper.selectByCountry(country);
    }

}