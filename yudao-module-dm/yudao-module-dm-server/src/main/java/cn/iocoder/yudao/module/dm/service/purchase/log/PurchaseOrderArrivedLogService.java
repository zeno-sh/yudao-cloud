package cn.iocoder.yudao.module.dm.service.purchase.log;

import javax.validation.*;

import cn.iocoder.yudao.module.dm.controller.admin.purchase.log.vo.PurchaseOrderArrivedLogListSaveReqVO;
import cn.iocoder.yudao.module.dm.controller.admin.purchase.log.vo.PurchaseOrderArrivedLogPageReqVO;
import cn.iocoder.yudao.module.dm.controller.admin.purchase.log.vo.PurchaseOrderArrivedLogSaveReqVO;
import cn.iocoder.yudao.module.dm.dal.dataobject.purchaseorderlog.PurchaseOrderArrivedLogDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

import java.util.Collection;
import java.util.List;

/**
 * 采购单到货日志 Service 接口
 *
 * @author Zeno
 */
public interface PurchaseOrderArrivedLogService {

    /**
     * 创建采购单到货日志
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createPurchaseOrderArrivedLog(@Valid PurchaseOrderArrivedLogSaveReqVO createReqVO);

    /**
     * 批量创建采购单到货日志
     *
     * @param arrivedLogListSaveReqVO
     * @return
     */
    Boolean batchCreatePurchaseOrderArrivedLog(@Valid PurchaseOrderArrivedLogListSaveReqVO arrivedLogListSaveReqVO);

    /**
     * 更新采购单到货日志
     *
     * @param updateReqVO 更新信息
     */
    void updatePurchaseOrderArrivedLog(@Valid PurchaseOrderArrivedLogSaveReqVO updateReqVO);

    /**
     * 删除采购单到货日志
     *
     * @param id 编号
     */
    void deletePurchaseOrderArrivedLog(Long id);

    /**
     * 获得采购单到货日志
     *
     * @param id 编号
     * @return 采购单到货日志
     */
    PurchaseOrderArrivedLogDO getPurchaseOrderArrivedLog(Long id);

    /**
     * 获得采购单到货日志分页
     *
     * @param pageReqVO 分页查询
     * @return 采购单到货日志分页
     */
    PageResult<PurchaseOrderArrivedLogDO> getPurchaseOrderArrivedLogPage(PurchaseOrderArrivedLogPageReqVO pageReqVO);

    /**
     * 根据采购单明细ID查询采购单到货日志
     *
     * @param purchaseItemIds
     * @return
     */
    List<PurchaseOrderArrivedLogDO> batchPurchaseOrderArrivedLogByPurchaseItemIds(Collection<Long> purchaseItemIds);
}