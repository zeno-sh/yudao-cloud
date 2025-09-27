package cn.iocoder.yudao.module.dm.service.warehouse;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import javax.annotation.Resource;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;

import cn.iocoder.yudao.module.dm.controller.admin.warehouse.vo.*;
import cn.iocoder.yudao.module.dm.dal.dataobject.warehouse.FbsProductStockDO;
import cn.iocoder.yudao.module.dm.dal.mysql.warehouse.FbsProductStockMapper;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

import org.springframework.context.annotation.Import;

import static cn.iocoder.yudao.module.dm.enums.ErrorCodeConstants.*;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.*;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.*;
import static cn.iocoder.yudao.framework.common.util.object.ObjectUtils.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link FbsProductStockServiceImpl} 的单元测试类
 *
 * @author Zeno
 */
@Import(FbsProductStockServiceImpl.class)
public class FbsProductStockServiceImplTest extends BaseDbUnitTest {

    @Resource
    private FbsProductStockServiceImpl fbsProductStockService;

    @Resource
    private FbsProductStockMapper fbsProductStockMapper;

    @Test
    public void testCreateFbsProductStock_success() {
        // 准备参数
        FbsProductStockSaveReqVO createReqVO = randomPojo(FbsProductStockSaveReqVO.class).setId(null);

        // 调用
        Long fbsProductStockId = fbsProductStockService.createFbsProductStock(createReqVO);
        // 断言
        assertNotNull(fbsProductStockId);
        // 校验记录的属性是否正确
        FbsProductStockDO fbsProductStock = fbsProductStockMapper.selectById(fbsProductStockId);
        assertPojoEquals(createReqVO, fbsProductStock, "id");
    }

    @Test
    public void testUpdateFbsProductStock_success() {
        // mock 数据
        FbsProductStockDO dbFbsProductStock = randomPojo(FbsProductStockDO.class);
        fbsProductStockMapper.insert(dbFbsProductStock);// @Sql: 先插入出一条存在的数据
        // 准备参数
        FbsProductStockSaveReqVO updateReqVO = randomPojo(FbsProductStockSaveReqVO.class, o -> {
            o.setId(dbFbsProductStock.getId()); // 设置更新的 ID
        });

        // 调用
        fbsProductStockService.updateFbsProductStock(updateReqVO);
        // 校验是否更新正确
        FbsProductStockDO fbsProductStock = fbsProductStockMapper.selectById(updateReqVO.getId()); // 获取最新的
        assertPojoEquals(updateReqVO, fbsProductStock);
    }

    @Test
    public void testUpdateFbsProductStock_notExists() {
        // 准备参数
        FbsProductStockSaveReqVO updateReqVO = randomPojo(FbsProductStockSaveReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> fbsProductStockService.updateFbsProductStock(updateReqVO), FBS_PRODUCT_STOCK_NOT_EXISTS);
    }

    @Test
    public void testDeleteFbsProductStock_success() {
        // mock 数据
        FbsProductStockDO dbFbsProductStock = randomPojo(FbsProductStockDO.class);
        fbsProductStockMapper.insert(dbFbsProductStock);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbFbsProductStock.getId();

        // 调用
        fbsProductStockService.deleteFbsProductStock(id);
       // 校验数据不存在了
       assertNull(fbsProductStockMapper.selectById(id));
    }

    @Test
    public void testDeleteFbsProductStock_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> fbsProductStockService.deleteFbsProductStock(id), FBS_PRODUCT_STOCK_NOT_EXISTS);
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetFbsProductStockPage() {
       // mock 数据
       FbsProductStockDO dbFbsProductStock = randomPojo(FbsProductStockDO.class, o -> { // 等会查询到
           o.setWarehouseId(null);
           o.setProductSku(null);
       });
       fbsProductStockMapper.insert(dbFbsProductStock);
       // 测试 warehouseId 不匹配
       fbsProductStockMapper.insert(cloneIgnoreId(dbFbsProductStock, o -> o.setWarehouseId(null)));
       // 测试 productSku 不匹配
       fbsProductStockMapper.insert(cloneIgnoreId(dbFbsProductStock, o -> o.setProductSku(null)));
       // 测试 offerId 不匹配
       // 准备参数
       FbsProductStockPageReqVO reqVO = new FbsProductStockPageReqVO();
       reqVO.setWarehouseId(null);
       reqVO.setProductSku(null);

       // 调用
       PageResult<FbsProductStockDO> pageResult = fbsProductStockService.getFbsProductStockPage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbFbsProductStock, pageResult.getList().get(0));
    }

}