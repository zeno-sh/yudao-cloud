package cn.iocoder.yudao.module.dm.infrastructure.service;

import java.util.List;

/**
 * 财务账单利润计算
 *
 * @author: Zeno
 * @createTime: 2024/10/12 11:03
 */
public interface ProfitComputeService {

    void computeProfitReport(String date, List<String> clientIds);
}
