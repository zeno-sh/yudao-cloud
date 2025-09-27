package cn.iocoder.yudao.module.dm.service.exchangerates;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.Resource;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;

import cn.iocoder.yudao.module.dm.controller.admin.exchangerates.vo.*;
import cn.iocoder.yudao.module.dm.dal.dataobject.exchangerates.ExchangeRatesDO;
import cn.iocoder.yudao.module.dm.dal.mysql.exchangerates.ExchangeRatesMapper;
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
 * {@link ExchangeRatesServiceImpl} 的单元测试类
 *
 * @author Zeno
 */
@Import(ExchangeRatesServiceImpl.class)
public class ExchangeRatesServiceImplTest extends BaseDbUnitTest {

    @Resource
    private ExchangeRatesServiceImpl exchangeRatesService;

    @Resource
    private ExchangeRatesMapper exchangeRatesMapper;

    @Test
    public void testCreateExchangeRates_success() {
        // 准备参数
        ExchangeRatesSaveReqVO createReqVO = randomPojo(ExchangeRatesSaveReqVO.class).setId(null);

        // 调用
        Long exchangeRatesId = exchangeRatesService.createExchangeRates(createReqVO);
        // 断言
        assertNotNull(exchangeRatesId);
        // 校验记录的属性是否正确
        ExchangeRatesDO exchangeRates = exchangeRatesMapper.selectById(exchangeRatesId);
        assertPojoEquals(createReqVO, exchangeRates, "id");
    }

    @Test
    public void testUpdateExchangeRates_success() {
        // mock 数据
        ExchangeRatesDO dbExchangeRates = randomPojo(ExchangeRatesDO.class);
        exchangeRatesMapper.insert(dbExchangeRates);// @Sql: 先插入出一条存在的数据
        // 准备参数
        ExchangeRatesSaveReqVO updateReqVO = randomPojo(ExchangeRatesSaveReqVO.class, o -> {
            o.setId(dbExchangeRates.getId()); // 设置更新的 ID
        });

        // 调用
        exchangeRatesService.updateExchangeRates(updateReqVO);
        // 校验是否更新正确
        ExchangeRatesDO exchangeRates = exchangeRatesMapper.selectById(updateReqVO.getId()); // 获取最新的
        assertPojoEquals(updateReqVO, exchangeRates);
    }

    @Test
    public void testUpdateExchangeRates_notExists() {
        // 准备参数
        ExchangeRatesSaveReqVO updateReqVO = randomPojo(ExchangeRatesSaveReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> exchangeRatesService.updateExchangeRates(updateReqVO), EXCHANGE_RATES_NOT_EXISTS);
    }

    @Test
    public void testDeleteExchangeRates_success() {
        // mock 数据
        ExchangeRatesDO dbExchangeRates = randomPojo(ExchangeRatesDO.class);
        exchangeRatesMapper.insert(dbExchangeRates);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbExchangeRates.getId();

        // 调用
        exchangeRatesService.deleteExchangeRates(id);
       // 校验数据不存在了
       assertNull(exchangeRatesMapper.selectById(id));
    }

    @Test
    public void testDeleteExchangeRates_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> exchangeRatesService.deleteExchangeRates(id), EXCHANGE_RATES_NOT_EXISTS);
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetExchangeRatesPage() {
       // mock 数据
       ExchangeRatesDO dbExchangeRates = randomPojo(ExchangeRatesDO.class, o -> { // 等会查询到
           o.setBaseCurrency(null);
           o.setTargetCurrency(null);
           o.setOfficialRate(null);
           o.setCustomRate(null);
           o.setCreateTime(null);
       });
       exchangeRatesMapper.insert(dbExchangeRates);
       // 测试 baseCurrency 不匹配
       exchangeRatesMapper.insert(cloneIgnoreId(dbExchangeRates, o -> o.setBaseCurrency(null)));
       // 测试 targetCurrency 不匹配
       exchangeRatesMapper.insert(cloneIgnoreId(dbExchangeRates, o -> o.setTargetCurrency(null)));
       // 测试 officialRate 不匹配
       exchangeRatesMapper.insert(cloneIgnoreId(dbExchangeRates, o -> o.setOfficialRate(null)));
       // 测试 customRate 不匹配
       exchangeRatesMapper.insert(cloneIgnoreId(dbExchangeRates, o -> o.setCustomRate(null)));
       // 测试 createTime 不匹配
       exchangeRatesMapper.insert(cloneIgnoreId(dbExchangeRates, o -> o.setCreateTime(null)));
       // 准备参数
       ExchangeRatesPageReqVO reqVO = new ExchangeRatesPageReqVO();
       reqVO.setBaseCurrency(null);
       reqVO.setTargetCurrency(null);
       reqVO.setOfficialRate(null);
       reqVO.setCustomRate(null);
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));

       // 调用
       PageResult<ExchangeRatesDO> pageResult = exchangeRatesService.getExchangeRatesPage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbExchangeRates, pageResult.getList().get(0));
    }

}