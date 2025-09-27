package cn.iocoder.yudao.module.dm.service.ozonsupplyorder;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.Resource;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;

import cn.iocoder.yudao.module.dm.controller.admin.ozonsupplyorder.vo.*;
import cn.iocoder.yudao.module.dm.dal.dataobject.ozonsupplyorder.OzonSupplyOrderDO;
import cn.iocoder.yudao.module.dm.dal.mysql.ozonsupplyorder.OzonSupplyOrderMapper;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

import javax.annotation.Resource;
import org.springframework.context.annotation.Import;
import java.util.*;
import java.time.LocalDateTime;

import static cn.hutool.core.util.RandomUtil.*;
import static cn.iocoder.yudao.module.dm.enums.ErrorCodeConstants.*;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.*;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.*;
import static cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.*;
import static cn.iocoder.yudao.framework.common.util.object.ObjectUtils.*;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * {@link OzonSupplyOrderServiceImpl} 的单元测试类
 *
 * @author Zeno
 */
@Import(OzonSupplyOrderServiceImpl.class)
public class OzonSupplyOrderServiceImplTest extends BaseDbUnitTest {

    @Resource
    private OzonSupplyOrderServiceImpl ozonSupplyOrderService;

    @Resource
    private OzonSupplyOrderMapper ozonSupplyOrderMapper;

    @Test
    public void testCreateOzonSupplyOrder_success() {
        // 准备参数
        OzonSupplyOrderSaveReqVO createReqVO = randomPojo(OzonSupplyOrderSaveReqVO.class).setId(null);

        // 调用
        Long ozonSupplyOrderId = ozonSupplyOrderService.createOzonSupplyOrder(createReqVO);
        // 断言
        assertNotNull(ozonSupplyOrderId);
        // 校验记录的属性是否正确
        OzonSupplyOrderDO ozonSupplyOrder = ozonSupplyOrderMapper.selectById(ozonSupplyOrderId);
        assertPojoEquals(createReqVO, ozonSupplyOrder, "id");
    }

    @Test
    public void testUpdateOzonSupplyOrder_success() {
        // mock 数据
        OzonSupplyOrderDO dbOzonSupplyOrder = randomPojo(OzonSupplyOrderDO.class);
        ozonSupplyOrderMapper.insert(dbOzonSupplyOrder);// @Sql: 先插入出一条存在的数据
        // 准备参数
        OzonSupplyOrderSaveReqVO updateReqVO = randomPojo(OzonSupplyOrderSaveReqVO.class, o -> {
            o.setId(dbOzonSupplyOrder.getId()); // 设置更新的 ID
        });

        // 调用
        ozonSupplyOrderService.updateOzonSupplyOrder(updateReqVO);
        // 校验是否更新正确
        OzonSupplyOrderDO ozonSupplyOrder = ozonSupplyOrderMapper.selectById(updateReqVO.getId()); // 获取最新的
        assertPojoEquals(updateReqVO, ozonSupplyOrder);
    }

    @Test
    public void testUpdateOzonSupplyOrder_notExists() {
        // 准备参数
        OzonSupplyOrderSaveReqVO updateReqVO = randomPojo(OzonSupplyOrderSaveReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> ozonSupplyOrderService.updateOzonSupplyOrder(updateReqVO), OZON_SUPPLY_ORDER_NOT_EXISTS);
    }

    @Test
    public void testDeleteOzonSupplyOrder_success() {
        // mock 数据
        OzonSupplyOrderDO dbOzonSupplyOrder = randomPojo(OzonSupplyOrderDO.class);
        ozonSupplyOrderMapper.insert(dbOzonSupplyOrder);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbOzonSupplyOrder.getId();

        // 调用
        ozonSupplyOrderService.deleteOzonSupplyOrder(id);
       // 校验数据不存在了
       assertNull(ozonSupplyOrderMapper.selectById(id));
    }

    @Test
    public void testDeleteOzonSupplyOrder_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> ozonSupplyOrderService.deleteOzonSupplyOrder(id), OZON_SUPPLY_ORDER_NOT_EXISTS);
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetOzonSupplyOrderPage() {
       // mock 数据
       OzonSupplyOrderDO dbOzonSupplyOrder = randomPojo(OzonSupplyOrderDO.class, o -> { // 等会查询到
           o.setClientId(null);
           o.setSupplyOrderId(null);
           o.setState(null);
           o.setWarehouseId(null);
           o.setWarehouseName(null);
           o.setTimeslotFrom(null);
           o.setTimeslotTo(null);
           o.setTotalItems(null);
           o.setTotalVolume(null);
           o.setCreateTime(null);
       });
       ozonSupplyOrderMapper.insert(dbOzonSupplyOrder);
       // 测试 clientId 不匹配
       ozonSupplyOrderMapper.insert(cloneIgnoreId(dbOzonSupplyOrder, o -> o.setClientId(null)));
       // 测试 supplyOrderId 不匹配
       ozonSupplyOrderMapper.insert(cloneIgnoreId(dbOzonSupplyOrder, o -> o.setSupplyOrderId(null)));
       // 测试 state 不匹配
       ozonSupplyOrderMapper.insert(cloneIgnoreId(dbOzonSupplyOrder, o -> o.setState(null)));
       // 测试 warehouseId 不匹配
       ozonSupplyOrderMapper.insert(cloneIgnoreId(dbOzonSupplyOrder, o -> o.setWarehouseId(null)));
       // 测试 warehouseName 不匹配
       ozonSupplyOrderMapper.insert(cloneIgnoreId(dbOzonSupplyOrder, o -> o.setWarehouseName(null)));
       // 测试 timeslotFrom 不匹配
       ozonSupplyOrderMapper.insert(cloneIgnoreId(dbOzonSupplyOrder, o -> o.setTimeslotFrom(null)));
       // 测试 timeslotTo 不匹配
       ozonSupplyOrderMapper.insert(cloneIgnoreId(dbOzonSupplyOrder, o -> o.setTimeslotTo(null)));
       // 测试 totalItems 不匹配
       ozonSupplyOrderMapper.insert(cloneIgnoreId(dbOzonSupplyOrder, o -> o.setTotalItems(null)));
       // 测试 totalVolume 不匹配
       ozonSupplyOrderMapper.insert(cloneIgnoreId(dbOzonSupplyOrder, o -> o.setTotalVolume(null)));
       // 测试 createTime 不匹配
       ozonSupplyOrderMapper.insert(cloneIgnoreId(dbOzonSupplyOrder, o -> o.setCreateTime(null)));
       // 准备参数
       OzonSupplyOrderPageReqVO reqVO = new OzonSupplyOrderPageReqVO();
       reqVO.setSupplyOrderId(null);
       reqVO.setState(null);
       reqVO.setWarehouseId(null);
       reqVO.setWarehouseName(null);
       reqVO.setTimeslotFrom(null);
       reqVO.setTimeslotTo(null);
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));

       // 调用
       PageResult<OzonSupplyOrderDO> pageResult = ozonSupplyOrderService.getOzonSupplyOrderPage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbOzonSupplyOrder, pageResult.getList().get(0));
    }

}