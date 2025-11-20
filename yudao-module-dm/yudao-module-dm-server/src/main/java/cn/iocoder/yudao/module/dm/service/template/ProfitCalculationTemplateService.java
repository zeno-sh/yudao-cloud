package cn.iocoder.yudao.module.dm.service.template;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.dm.controller.admin.template.vo.ProfitCalculationTemplatePageReqVO;
import cn.iocoder.yudao.module.dm.controller.admin.template.vo.ProfitCalculationTemplateSaveReqVO;
import cn.iocoder.yudao.module.dm.dal.dataobject.template.ProfitCalculationTemplateDO;

import javax.validation.Valid;
import java.util.List;

/**
 * 利润计算配置模板 Service 接口
 *
 * @author Zeno
 */
public interface ProfitCalculationTemplateService {

    /**
     * 创建利润计算配置模板
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createProfitCalculationTemplate(@Valid ProfitCalculationTemplateSaveReqVO createReqVO);

    /**
     * 更新利润计算配置模板
     *
     * @param updateReqVO 更新信息
     */
    void updateProfitCalculationTemplate(@Valid ProfitCalculationTemplateSaveReqVO updateReqVO);

    /**
     * 删除利润计算配置模板
     *
     * @param id 编号
     */
    void deleteProfitCalculationTemplate(Long id);

    /**
     * 获得利润计算配置模板
     *
     * @param id 编号
     * @return 利润计算配置模板
     */
    ProfitCalculationTemplateDO getProfitCalculationTemplate(Long id);

    /**
     * 获得利润计算配置模板分页
     *
     * @param pageReqVO 分页查询
     * @return 利润计算配置模板分页
     */
    PageResult<ProfitCalculationTemplateDO> getProfitCalculationTemplatePage(ProfitCalculationTemplatePageReqVO pageReqVO);

    /**
     * 根据国家获取利润计算配置模板列表
     *
     * @param country 国家代码
     * @return 利润计算配置模板列表
     */
    List<ProfitCalculationTemplateDO> getTemplatesByCountry(String country);

}