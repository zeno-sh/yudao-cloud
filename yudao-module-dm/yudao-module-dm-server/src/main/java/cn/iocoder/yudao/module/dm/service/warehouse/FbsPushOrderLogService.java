package cn.iocoder.yudao.module.dm.service.warehouse;

import java.util.*;
import javax.validation.*;
import cn.iocoder.yudao.module.dm.controller.admin.warehouse.vo.*;
import cn.iocoder.yudao.module.dm.dal.dataobject.warehouse.FbsPushOrderLogDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;

/**
 * 海外仓推单记录 Service 接口
 *
 * @author Zeno
 */
public interface FbsPushOrderLogService {

    /**
     * 创建海外仓推单记录
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createFbsPushOrderLog(@Valid FbsPushOrderLogSaveReqVO createReqVO);

    /**
     * 更新海外仓推单记录
     *
     * @param updateReqVO 更新信息
     */
    void updateFbsPushOrderLog(@Valid FbsPushOrderLogSaveReqVO updateReqVO);

    /**
     * 删除海外仓推单记录
     *
     * @param id 编号
     */
    void deleteFbsPushOrderLog(Long id);

    /**
     * 获得海外仓推单记录
     *
     * @param id 编号
     * @return 海外仓推单记录
     */
    FbsPushOrderLogDO getFbsPushOrderLog(Long id);

    /**
     * 获得海外仓推单记录列表
     *
     * @param orderIds
     * @return
     */
    List<FbsPushOrderLogDO> getFbsPushOrderLogByOrderId(Collection<Long> orderIds);

    /**
     * 获得海外仓推单记录列表
     *
     * @param postingNumbers
     * @return
     */
    List<FbsPushOrderLogDO> getFbsPushOrderLogByPostingNumbers(Collection<String> postingNumbers);

    /**
     * 获得海外仓推单记录分页
     *
     * @param pageReqVO 分页查询
     * @return 海外仓推单记录分页
     */
    PageResult<FbsPushOrderLogDO> getFbsPushOrderLogPage(FbsPushOrderLogPageReqVO pageReqVO);

}