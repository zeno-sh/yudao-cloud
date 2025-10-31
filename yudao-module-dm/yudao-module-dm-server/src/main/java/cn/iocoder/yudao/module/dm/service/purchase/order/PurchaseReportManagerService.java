package cn.iocoder.yudao.module.dm.service.purchase.order;

import cn.hutool.core.date.DateUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.dm.controller.admin.product.vo.ProductSimpleInfoVO;
import cn.iocoder.yudao.module.dm.controller.admin.statistics.vo.*;
import cn.iocoder.yudao.module.dm.dal.dataobject.product.SupplierPriceOfferDO;
import cn.iocoder.yudao.module.dm.dal.mysql.purchaseorder.DmPurchaseOrderMapper;
import cn.iocoder.yudao.module.dm.service.product.ProductInfoService;
import cn.iocoder.yudao.module.dm.service.purchase.order.dto.ProductStockDTO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 本地出入库报表 Service
 */
@Service
@Slf4j
public class PurchaseReportManagerService {

    @Resource
    private DmPurchaseOrderMapper dmPurchaseOrderMapper;
    @Resource
    private ProductInfoService productInfoService;

    /**
     * 计算期初库存（本月之前的所有历史累计数据）
     */
    private Map<Long, Integer> calculateInitialInventory(ReportStockReqVO reqVO, Set<Long> productIds) {
        // 创建历史查询对象
        ReportStockReqVO historyReqVO = new ReportStockReqVO();
        historyReqVO.setProductId(reqVO.getProductId());

        // 设置历史日期范围：从系统最早日期到本月初
        LocalDate currentMonth = LocalDate.parse(reqVO.getMonth() + "-01");
        String[] historyDateRange = new String[]{
                "2020-01-01 00:00:00", // 设置一个足够早的起始日期，或者从数据库获取最早的记录日期
                currentMonth.minusDays(1).toString() + " 23:59:59" // 本月第一天的前一天
        };
        historyReqVO.setDate(historyDateRange);

        log.info("[calculateInitialInventory][当前月份: {}][历史日期范围: {} ~ {}]",
                reqVO.getMonth(), historyDateRange[0], historyDateRange[1]);

        // 查询历史数据
        IPage<ProductStockDTO> historyPage = new Page<>(1, Integer.MAX_VALUE);
        IPage<ProductStockDTO> historyStockPage = dmPurchaseOrderMapper.selectMonthlyProductStock(historyPage, historyReqVO);
        List<ProductStockDTO> historyStockList = historyStockPage.getRecords();

        // 按产品ID分组计算历史累计结余
        Map<Long, Integer> initialInventoryMap = new TreeMap<>();
        for (Long productId : productIds) {
            // 计算历史采购总量和发货总量
            int historyPurchaseTotal = 0;
            int historyDeliverTotal = 0;

            for (ProductStockDTO stock : historyStockList) {
                if (productId.equals(stock.getProductId())) {
                    historyPurchaseTotal += stock.getPurchaseNum();
                    historyDeliverTotal += stock.getDeliveryNum();
                }
            }

            // 计算期初库存（历史累计结余）
            int initialInventory = historyPurchaseTotal - historyDeliverTotal;

            log.info("[calculateInitialInventory][productId: {}][历史采购: {}][历史发货: {}][期初库存: {}]",
                    productId, historyPurchaseTotal, historyDeliverTotal, initialInventory);

            initialInventoryMap.put(productId, initialInventory);
        }

        return initialInventoryMap;
    }

    /**
     * 获取月度库存报表（带分页）
     */
    public PageResult<ProductStockVO> getMonthlyInventoryReport(ReportStockReqVO reqVO) {
        // 1. 解析月份，获取月初和月末
        LocalDate monthStart = LocalDate.parse(reqVO.getMonth() + "-01");
        LocalDate monthEnd = monthStart.with(TemporalAdjusters.lastDayOfMonth());
        String[] currentDateRange = new String[]{
                monthStart + " 00:00:00",
                monthEnd + " 23:59:59"
        };

        log.info("[getMonthlyInventoryReport][查询参数：month: {}, productId: {}, dateRange: {} ~ {}]",
                reqVO.getMonth(), reqVO.getProductId(), currentDateRange[0], currentDateRange[1]);

        // 2. 获取日期范围内的所有数据（分页）
        IPage<ProductStockDTO> page = new Page<>(reqVO.getPageNo(), reqVO.getPageSize());
        reqVO.setDate(currentDateRange);
        IPage<ProductStockDTO> stockPage = dmPurchaseOrderMapper.selectMonthlyProductStock(page, reqVO);
        List<ProductStockDTO> stockList = stockPage.getRecords();
        if (CollectionUtils.isEmpty(stockList)) {
            return new PageResult<>(new ArrayList<>(), stockPage.getTotal());
        }

        // 3. 获取所有产品ID
        Set<Long> productIds = stockList.stream()
                .map(ProductStockDTO::getProductId)
                .collect(Collectors.toSet());

        // 4. 获取产品信息
        Map<Long, ProductSimpleInfoVO> productInfoMap = productInfoService.batchQueryProductSimpleInfo(new ArrayList<>(productIds));

        // 查询产品的供应商报价信息
        Map<Long, SupplierPriceOfferDO> priceMap = productInfoService.getSupplierPriceOfferMapByProductIds(new ArrayList<>(productIds));

        // 5. 构建产品库存列表
        List<ProductStockVO> resultStockList = new ArrayList<>();
        for (ProductStockDTO stock : stockList) {
            Long productId = stock.getProductId();
            ProductStockVO stockVO = new ProductStockVO();
            stockVO.setProductId(stock.getProductId());
            stockVO.setProductSimpleInfoVO(productInfoMap.get(stock.getProductId()));
            stockVO.setDate(reqVO.getMonth()); // 设置统计月份
            stockVO.setPreTotal(stock.getPreTotal());
            stockVO.setCurrentTotal(stock.getPurchaseNum());
            stockVO.setCurrentTotalDeliver(stock.getDeliveryNum());

            SupplierPriceOfferDO supplierPriceOfferDO = priceMap.get(productId);
            if (null != supplierPriceOfferDO) {
                BigDecimal price = supplierPriceOfferDO.getPrice();
                BigDecimal taxPrice = supplierPriceOfferDO.getTaxPrice();

                stockVO.setCurrentTotalPrice(price.multiply(new BigDecimal(stock.getCurrentBalance())));
                stockVO.setCurrentTotalTaxPrice(taxPrice.multiply(new BigDecimal(stock.getCurrentBalance())));
            }

            stockVO.setCurrentTotalBalance(stock.getCurrentBalance());

            resultStockList.add(stockVO);
        }

        return new PageResult<>(resultStockList, stockPage.getTotal());
    }

    /**
     * 构建Excel导出数据
     */
    public List<LocalInventoryExportVO> buildExportExcelData(List<ProductStockVO> monthlyList) {
        List<LocalInventoryExportVO> excelDataList = new ArrayList<>();

        // 处理每个产品的数据
        for (ProductStockVO monthlyData : monthlyList) {
            ProductSimpleInfoVO productInfo = monthlyData.getProductSimpleInfoVO();
            // 创建导出对象
            LocalInventoryExportVO excelVO = BeanUtils.toBean(monthlyData, LocalInventoryExportVO.class);
            excelVO.setSkuId(productInfo != null ? productInfo.getSkuId() : "");
            excelVO.setSkuName(productInfo != null ? productInfo.getSkuName() : "");
            excelDataList.add(excelVO);
        }

        return excelDataList;
    }
}
