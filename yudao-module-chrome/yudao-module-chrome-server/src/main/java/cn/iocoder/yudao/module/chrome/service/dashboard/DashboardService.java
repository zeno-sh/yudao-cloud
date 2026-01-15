package cn.iocoder.yudao.module.chrome.service.dashboard;

import cn.iocoder.yudao.module.chrome.controller.admin.dashboard.vo.DashboardStatisticsRespVO;
import cn.iocoder.yudao.module.chrome.controller.admin.dashboard.vo.UserCreditsConsumeRespVO;

import java.time.LocalDate;
import java.util.List;

/**
 * Chrome插件大盘统计 Service 接口
 *
 * @author Jax
 */
public interface DashboardService {

    /**
     * 获取指定日期的统计数据
     *
     * @param date 统计日期
     * @return 统计数据
     */
    DashboardStatisticsRespVO getStatistics(LocalDate date);

    /**
     * 获取指定日期每个用户的积分消耗明细
     *
     * @param date 统计日期
     * @return 用户积分消耗明细列表
     */
    List<UserCreditsConsumeRespVO> getUserCreditsConsume(LocalDate date);
}
