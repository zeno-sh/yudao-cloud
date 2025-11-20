package cn.iocoder.yudao.module.dm.service.calculation;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.dm.controller.admin.calculation.vo.ProfitCalculationPageReqVO;
import cn.iocoder.yudao.module.dm.controller.admin.calculation.vo.ProfitCalculationSaveReqVO;
import cn.iocoder.yudao.module.dm.dal.dataobject.calculation.ProfitCalculationDO;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;

/**
 * 利润预测 Service 接口
 *
 * @author Zeno
 */
public interface ProfitCalculationService {

    /**
     * 创建利润预测
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createProfitCalculation(@Valid ProfitCalculationSaveReqVO createReqVO);

    /**
     * 更新利润预测
     *
     * @param updateReqVO 更新信息
     */
    void updateProfitCalculation(@Valid ProfitCalculationSaveReqVO updateReqVO);

    /**
     * 删除利润预测
     *
     * @param id 编号
     */
    void deleteProfitCalculation(Long id);

    /**
     * 获得利润预测
     *
     * @param id 编号
     * @return 利润预测
     */
    ProfitCalculationDO getProfitCalculation(Long id);

    /**
     * 获得利润预测分页
     *
     * @param pageReqVO 分页查询
     * @return 利润预测分页
     */
    PageResult<ProfitCalculationDO> getProfitCalculationPage(ProfitCalculationPageReqVO pageReqVO);



    /**
     * 获得导入利润预测模板
     *
     * @param response HTTP 响应
     */
    void getImportTemplate(HttpServletResponse response) throws IOException;

}