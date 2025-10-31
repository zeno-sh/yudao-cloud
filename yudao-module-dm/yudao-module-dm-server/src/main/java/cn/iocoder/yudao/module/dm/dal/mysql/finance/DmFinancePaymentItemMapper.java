package cn.iocoder.yudao.module.dm.dal.mysql.finance;

import java.math.BigDecimal;
import java.util.*;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.dm.dal.dataobject.finance.FinancePaymentItemDO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * ERP 付款项 Mapper
 *
 * @author Zeno
 */
@Mapper
public interface DmFinancePaymentItemMapper extends BaseMapperX<FinancePaymentItemDO> {

    default List<FinancePaymentItemDO> selectListByPaymentId(Long paymentId) {
        return selectList(FinancePaymentItemDO::getPaymentId, paymentId);
    }

    default int deleteByPaymentId(Long paymentId) {
        return delete(FinancePaymentItemDO::getPaymentId, paymentId);
    }

    default BigDecimal selectPaymentPriceSumByBizIdAndBizType(Long bizId, Integer bizType) {
        // SQL sum 查询
        List<Map<String, Object>> result = selectMaps(new QueryWrapper<FinancePaymentItemDO>()
                .select("SUM(payment_price) AS paymentPriceSum")
                .eq("biz_id", bizId)
                .eq("biz_type", bizType));
        // 获得数量
        if (CollUtil.isEmpty(result)) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(MapUtil.getDouble(result.get(0), "paymentPriceSum", 0D));
    }
}