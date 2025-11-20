package cn.iocoder.yudao.module.dm.dal.mysql.template;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.dm.controller.admin.template.vo.ProfitCalculationTemplatePageReqVO;
import cn.iocoder.yudao.module.dm.dal.dataobject.template.ProfitCalculationTemplateDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 利润计算配置模板 Mapper
 *
 * @author Zeno
 */
@Mapper
public interface ProfitCalculationTemplateMapper extends BaseMapperX<ProfitCalculationTemplateDO> {

    default PageResult<ProfitCalculationTemplateDO> selectPage(ProfitCalculationTemplatePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ProfitCalculationTemplateDO>()
                .likeIfPresent(ProfitCalculationTemplateDO::getTemplateName, reqVO.getTemplateName())
                .eqIfPresent(ProfitCalculationTemplateDO::getCountry, reqVO.getCountry())
                .eqIfPresent(ProfitCalculationTemplateDO::getPlatform, reqVO.getPlatform())
                .betweenIfPresent(ProfitCalculationTemplateDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(ProfitCalculationTemplateDO::getId));
    }

    default List<ProfitCalculationTemplateDO> selectByCountry(String country) {
        return selectList(new LambdaQueryWrapperX<ProfitCalculationTemplateDO>()
                .eq(ProfitCalculationTemplateDO::getCountry, country)
                .orderByDesc(ProfitCalculationTemplateDO::getId));
    }

}