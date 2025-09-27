package cn.iocoder.yudao.module.dm.controller.admin.statistics;

import cn.hutool.core.date.*;
import cn.idev.excel.EasyExcel;
import cn.idev.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.dm.controller.admin.product.vo.ProductInfoRespVO;
import cn.iocoder.yudao.module.dm.controller.admin.statistics.vo.*;
import cn.iocoder.yudao.module.dm.dal.dataobject.ad.OzonAdCampaignsDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.order.OzonOrderDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.order.OzonOrderItemDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.ozonshopmapping.OzonShopMappingDO;
import cn.iocoder.yudao.module.dm.infrastructure.AuthShopMappingService;
import cn.iocoder.yudao.module.dm.infrastructure.ozon.OzonTradeStatisticsService;
import cn.iocoder.yudao.module.dm.infrastructure.ozon.utils.DmDateUtils;
import cn.iocoder.yudao.module.dm.infrastructure.service.ProductVolumeQueryService;
import cn.iocoder.yudao.module.dm.infrastructure.service.ProductVolumeShopQueryService;
import cn.iocoder.yudao.module.dm.infrastructure.service.dto.ProductVolumeDTO;
import cn.iocoder.yudao.module.dm.service.ad.OzonAdCampaignsService;
import cn.iocoder.yudao.module.dm.service.order.OzonOrderService;
import cn.iocoder.yudao.module.dm.service.purchase.order.PurchaseReportManagerService;
import cn.iocoder.yudao.module.dm.service.transport.TransportReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.Arrays;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;

/**
 * @author: Zeno
 * @createTime: 2024/06/30 22:04
 */
@Tag(name = "管理后台 - 首页")
@RestController
@RequestMapping("/dm/statistics")
@Validated
public class StatisticsController {

    @Resource
    private OzonOrderService ozonOrderService;
    @Resource
    private OzonAdCampaignsService dmAdCampaignsService;
    @Resource
    private OzonTradeStatisticsService ozonTradeStatisticsService;
    @Resource
    private AuthShopMappingService authShopMappingService;
    @Resource
    private ProductVolumeQueryService<PageResult<ProductVolumeRespVO>, ProductVolumeReqVO> productVolumeQueryService;
    @Resource
    private ProductVolumeShopQueryService productVolumeShopQueryService;
    @Resource
    private PurchaseReportManagerService purchaseReportManagerService;
    @Resource
    private TransportReportService transportReportService;

    @GetMapping("/volume")
    public CommonResult<ProductVolumeVO> volume(@Valid ReportProductVolumeRequest request) {
        String today = DmDateUtils.getMoscowToday();
        String yesterday = DmDateUtils.getMoscowYesterday();

        String[] clientIds = getClientIds(request.getClientIds());

        List<OzonOrderItemDO> todayItemList = getDmOrderItemList(clientIds, today);
        List<OzonOrderItemDO> yesterdayItemList = getDmOrderItemList(clientIds, yesterday);

        List<OzonOrderDO> todayOrderList = getDmOrderList(clientIds, today);
        List<OzonOrderDO> yesterdayOrderList = getDmOrderList(clientIds, yesterday);

        List<String> todayCancelOrders = todayOrderList.stream()
                .filter(order -> "cancelled_from_split_pending".equals(order.getStatus()) || "cancelled".equals(order.getStatus()))
                .map(OzonOrderDO::getPostingNumber)
                .collect(Collectors.toList());

        List<String> yesterdayCancelOrders = yesterdayOrderList.stream()
                .filter(order -> "cancelled_from_split_pending".equals(order.getStatus()) || "cancelled".equals(order.getStatus()))
                .map(OzonOrderDO::getPostingNumber)
                .collect(Collectors.toList());


        ProductVolumeVO productVolumeVO = new ProductVolumeVO();
        productVolumeVO.setTodayOrderVolume(todayOrderList.size());
        productVolumeVO.setYesterdayOrderVolume(yesterdayOrderList.size());
        productVolumeVO.setTodayProductVolume(todayItemList.stream().map(OzonOrderItemDO::getQuantity).reduce(0, Integer::sum));
        productVolumeVO.setYesterdayProductVolume(yesterdayItemList.stream().map(OzonOrderItemDO::getQuantity).reduce(0, Integer::sum));

        // 计算今日订单总金额
        BigDecimal todayTotalAmount = todayOrderList.stream()
                .filter(order -> !"cancelled_from_split_pending".equals(order.getStatus()) && !"cancelled".equals(order.getStatus()))
                .map(OzonOrderDO::getAccrualsForSale)
                .reduce(BigDecimal.ZERO, BigDecimal::add); // 累加金额

        // 计算昨日订单总金额
        BigDecimal yesterdayTotalAmount = yesterdayOrderList.stream()
                .filter(order -> !"cancelled_from_split_pending".equals(order.getStatus()) && !"cancelled".equals(order.getStatus()))
                .map(OzonOrderDO::getAccrualsForSale)
                .reduce(BigDecimal.ZERO, BigDecimal::add); // 累加金额

        productVolumeVO.setTodayAmount(todayTotalAmount);
        productVolumeVO.setYesterdayAmount(yesterdayTotalAmount);
        productVolumeVO.setTodayCancelOrderVolume(todayCancelOrders.size());
        productVolumeVO.setYesterdayCancelOrderVolume(yesterdayCancelOrders.size());

        if (todayTotalAmount.compareTo(BigDecimal.ZERO) > 0 && CollectionUtils.isNotEmpty(todayItemList)) {
            productVolumeVO.setTodayAvgPrice(
                    todayTotalAmount.divide(new BigDecimal(todayItemList.size()), 2, BigDecimal.ROUND_HALF_UP)
            );
        }
        if (yesterdayTotalAmount.compareTo(BigDecimal.ZERO) > 0 && CollectionUtils.isNotEmpty(yesterdayItemList)) {
            productVolumeVO.setYesterdayAvgPrice(
                    yesterdayTotalAmount.divide(new BigDecimal(yesterdayItemList.size()), 2, BigDecimal.ROUND_HALF_UP)
            );
        }
        return success(productVolumeVO);
    }

    @GetMapping("/ad")
    public CommonResult<ProductAdVO> indexAd(@Valid ReportProductVolumeRequest request) {
        String today = DmDateUtils.getMoscowToday();
        String yesterday = DmDateUtils.getMoscowYesterday();

        String[] clientIds = getClientIds(request.getClientIds());

        List<OzonOrderDO> todayOrderList = getDmOrderList(clientIds, today);
        List<OzonOrderDO> yesterdayOrderList = getDmOrderList(clientIds, yesterday);

        // 计算今日订单总金额
        BigDecimal todayTotalAmount = todayOrderList.stream()
                .filter(order -> !"cancelled_from_split_pending".equals(order.getStatus()) && !"cancelled".equals(order.getStatus()))
                .map(OzonOrderDO::getAccrualsForSale)
                .reduce(BigDecimal.ZERO, BigDecimal::add); // 累加金额

        // 计算昨日订单总金额
        BigDecimal yesterdayTotalAmount = yesterdayOrderList.stream()
                .filter(order -> !"cancelled_from_split_pending".equals(order.getStatus()) && !"cancelled".equals(order.getStatus()))
                .map(OzonOrderDO::getAccrualsForSale)
                .reduce(BigDecimal.ZERO, BigDecimal::add); // 累加金额

        List<OzonAdCampaignsDO> todayAdList = getDmAdCampaigns(clientIds, today);
        List<OzonAdCampaignsDO> yesterdayAdList = getDmAdCampaigns(clientIds, yesterday);

        ProductAdVO productAdVO = new ProductAdVO();

        BigDecimal todaySpend = todayAdList.stream().map(OzonAdCampaignsDO::getMoneySpent).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal yesterdaySpend = yesterdayAdList.stream().map(OzonAdCampaignsDO::getMoneySpent).reduce(BigDecimal.ZERO, BigDecimal::add);

        productAdVO.setTodaySpend(todaySpend);
        productAdVO.setYesterdaySpend(yesterdaySpend);
        productAdVO.setTodayAmount(todayAdList.stream().map(OzonAdCampaignsDO::getOrdersMoney).reduce(BigDecimal.ZERO, BigDecimal::add));
        productAdVO.setYesterdayAmount(yesterdayAdList.stream().map(OzonAdCampaignsDO::getOrdersMoney).reduce(BigDecimal.ZERO, BigDecimal::add));
        productAdVO.setTodayAdVolume(todayAdList.stream().map(OzonAdCampaignsDO::getOrders).reduce(0, Integer::sum));
        productAdVO.setYesterdayAdVolume(yesterdayAdList.stream().map(OzonAdCampaignsDO::getOrders).reduce(0, Integer::sum));

        if (todayTotalAmount.compareTo(BigDecimal.ZERO) > 0) {
            productAdVO.setTodayAcoas(todaySpend.divide(todayTotalAmount, 5, BigDecimal.ROUND_HALF_UP));
            productAdVO.setTodayAdRate(productAdVO.getTodayAmount().divide(todayTotalAmount, 5, BigDecimal.ROUND_HALF_UP));
        }
        if (yesterdayTotalAmount.compareTo(BigDecimal.ZERO) > 0) {
            productAdVO.setYesterdayAcoas(yesterdaySpend.divide(yesterdayTotalAmount, 5, BigDecimal.ROUND_HALF_UP));
            productAdVO.setYesterdayAdRate(productAdVO.getYesterdayAmount().divide(yesterdayTotalAmount, 5, BigDecimal.ROUND_HALF_UP));
        }

        if (productAdVO.getTodayAmount().compareTo(BigDecimal.ZERO) > 0) {
            productAdVO.setTodayAcos(productAdVO.getTodaySpend().divide(productAdVO.getTodayAmount(), 5, BigDecimal.ROUND_HALF_UP));
        }
        if (productAdVO.getYesterdayAmount().compareTo(BigDecimal.ZERO) > 0) {
            productAdVO.setYesterdayAcos(productAdVO.getYesterdaySpend().divide(productAdVO.getYesterdayAmount(), 5, BigDecimal.ROUND_HALF_UP));
        }
        return success(productAdVO);
    }

    @GetMapping("/list")
    public CommonResult<List<TradeTrendSummaryRespVO>> getTradeStatisticsList(TradeTrendReqVO request) {
        String[] clientIds = getClientIds(request.getClientIds());
        request.setClientIds(clientIds);
        return success(ozonTradeStatisticsService.getTradeTendSummaryList(request));
    }

    @GetMapping("/analyse")
    public CommonResult<TradeTrendSummaryRespVO> getTradeStatistics(TradeTrendReqVO request) {
        String[] clientIds = getClientIds(request.getClientIds());
        request.setClientIds(clientIds);
        return success(ozonTradeStatisticsService.getTradeTrendSummary(request));
    }

    @GetMapping("/product/volume")
    public CommonResult<PageResult<ProductVolumeRespVO>> getVolumeStatistics(ProductVolumeReqVO request) {
        String[] clientIds = getClientIds(request.getClientIds());
        request.setClientIds(clientIds);
        return success(productVolumeQueryService.queryProductVolume(request));
    }

    @GetMapping("/product/volume/shop")
    public CommonResult<List<ProductVolumeRespVO>> getVolumeStatisticsByShop(ProductVolumeReqVO request) {
        String[] clientIds = getClientIds(request.getClientIds());
        request.setClientIds(clientIds);
        return success(productVolumeShopQueryService.queryProductVolumeByShop(request));
    }

    @GetMapping("/product/volume/export")
    @Operation(summary = "导出产品体积销量报表")
    public void exportVolumeStatistics(ProductVolumeReqVO request,
                                      HttpServletResponse response) throws IOException {
        String[] clientIds = getClientIds(request.getClientIds());
        request.setClientIds(clientIds);
        
        // 获取全量数据（不分页）
        request.setPageNo(1);
        request.setPageSize(Integer.MAX_VALUE);
        PageResult<ProductVolumeRespVO> pageResult = productVolumeQueryService.queryProductVolume(request);
        
        if (pageResult.getList().isEmpty()) {
            return;
        }
        
        // 使用动态表头导出Excel
        exportVolumeWithDynamicHeaders(pageResult.getList(), response);
    }
    
    /**
     * 使用动态表头导出产品体积销量数据
     */
    private void exportVolumeWithDynamicHeaders(List<ProductVolumeRespVO> respVOList, HttpServletResponse response) throws IOException {
        if (CollectionUtils.isEmpty(respVOList)) {
            return;
        }
        
        ProductVolumeRespVO firstResp = respVOList.get(0);
        List<String> dateList = firstResp.getDateList();
        List<ProductVolumeDTO> volumeList = firstResp.getVolumeList();
        
        if (CollectionUtils.isEmpty(volumeList)) {
            return;
        }
        
        // 构建动态表头
        List<List<String>> headList = buildDynamicHeaders(dateList);
        
        // 构建数据行
        List<List<Object>> dataList = buildDynamicData(volumeList, dateList);
        
        // 设置文件名
        String fileName = URLEncoder.encode("产品体积销量报表_" + 
            DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss").format(LocalDateTime.now()), "UTF-8")
            .replaceAll("\\+", "%20");
        
        // 设置响应头
        response.addHeader("Content-Disposition", "attachment;filename=" + fileName + ".xlsx");
        response.setContentType("application/vnd.ms-excel;charset=UTF-8");
        
        // 写入Excel
        EasyExcel.write(response.getOutputStream())
                .autoCloseStream(false)
                .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                .head(headList)
                .sheet("产品体积销量")
                .doWrite(dataList);
    }

    /**
     * 构建动态表头
     */
    private List<List<String>> buildDynamicHeaders(List<String> dateList) {
        List<List<String>> headList = new ArrayList<>();
        
        // 基础列
        headList.add(Arrays.asList("货号"));
        headList.add(Arrays.asList("本地SKU"));
        headList.add(Arrays.asList("总销量"));
        headList.add(Arrays.asList("日均销量"));
        headList.add(Arrays.asList("关联门店"));
        
        // 动态日期列
        if (CollectionUtils.isNotEmpty(dateList)) {
            for (String date : dateList) {
                headList.add(Arrays.asList(date));
            }
        }
        
        return headList;
    }
    
    /**
     * 构建动态数据
     */
    private List<List<Object>> buildDynamicData(List<ProductVolumeDTO> volumeList, List<String> dateList) {
        List<List<Object>> dataList = new ArrayList<>();
        
        for (ProductVolumeDTO volumeDTO : volumeList) {
            List<Object> rowData = new ArrayList<>();
            
            // 基础数据
            rowData.add(volumeDTO.getOfferId() != null ? volumeDTO.getOfferId() : "");
            rowData.add(volumeDTO.getSkuId() != null ? volumeDTO.getSkuId() : "");
            rowData.add(volumeDTO.getTotal() != null ? volumeDTO.getTotal() : 0);
            rowData.add(volumeDTO.getAvg() != null ? volumeDTO.getAvg() : 0);
            
            // 关联门店信息（用于SKU查询类型）
            String associatedShops = "";
            if (CollectionUtils.isNotEmpty(volumeDTO.getShops())) {
                associatedShops = volumeDTO.getShops().stream()
                        .map(ProductVolumeDTO.ShopInfo::getClientId)
                        .collect(Collectors.joining(","));
            }
            rowData.add(associatedShops);
            
            // 动态日期销量数据
            if (CollectionUtils.isNotEmpty(dateList)) {
                Map<String, Integer> volumeMap = volumeDTO.getVolumeMap();
                for (String date : dateList) {
                    Integer volume = volumeMap != null ? volumeMap.get(date) : null;
                    rowData.add(volume != null ? volume : 0);
                }
            }
            
            dataList.add(rowData);
        }
        
        return dataList;
    }

    @GetMapping("/domestic-stock")
    public CommonResult<PageResult<ProductStockVO>> domesticStock(@Valid ReportStockReqVO request) {
        return success(purchaseReportManagerService.getMonthlyInventoryReport(request));
    }

    @GetMapping("/domestic-stock/export")
    @Operation(summary = "导出本地出入库报表")
    public void domesticStockExport(@Valid ReportStockReqVO request,
                                         HttpServletResponse response) throws IOException {
        // 获取月度数据（不分页，获取全量数据）
        request.setPageNo(1);
        request.setPageSize(Integer.MAX_VALUE); // 设置一个足够大的数，确保获取所有数据
        PageResult<ProductStockVO> pageResult = purchaseReportManagerService.getMonthlyInventoryReport(request);
        List<ProductStockVO> monthlyList = pageResult.getList();
        if (CollectionUtils.isEmpty(monthlyList)) {
            return;
        }

        // 获取导出数据
        List<LocalInventoryExportVO> excelDataList = purchaseReportManagerService.buildExportExcelData(monthlyList);
        // 导出 Excel
        String fileName = URLEncoder.encode("库存统计_" + request.getMonth(), "UTF-8").replaceAll("\\+", "%20");
        ExcelUtils.write(response, fileName, "库存统计", LocalInventoryExportVO.class, excelDataList);
    }

    @GetMapping("/transit-stock")
    @Operation(summary = "获取发货计划月度报表分页")
    public CommonResult<PageResult<TransportReportVO>> transitStock(@Valid TransportReportReqVO reqVO) {
        return success(transportReportService.getTransportReportPage(reqVO));
    }

    @GetMapping("/transit-stock/export")
    @Operation(summary = "导出发货计划报表")
    public void transitStockExport(@Valid TransportReportReqVO reqVO,
                                    HttpServletResponse response) throws IOException {
        // 获取月度数据（不分页，获取全量数据）
        reqVO.setPageNo(1);
        reqVO.setPageSize(Integer.MAX_VALUE); // 设置一个足够大的数，确保获取所有数据
        PageResult<TransportReportVO> pageResult = transportReportService.getTransportReportPage(reqVO);
        List<TransportReportVO> monthlyList = pageResult.getList();
        if (CollectionUtils.isEmpty(monthlyList)) {
            return;
        }

        // 获取导出数据
        List<TransportReportExportVO> excelDataList = transportReportService.buildExportExcelData(monthlyList);

        // 导出 Excel
        ExcelUtils.write(response, "发货计划报表_" + reqVO.getMonth(), "数据", TransportReportExportVO.class, excelDataList);
    }

    private List<OzonOrderDO> getDmOrderList(String[] clientIds, String date) {
        //获取指定日期的一天开始时间、结束时间
        String beginDateTime = DmDateUtils.formatStartOfDay(date, DatePattern.NORM_DATETIME_PATTERN);
        String endDateTime = DmDateUtils.formatEndOfDay(date, DatePattern.NORM_DATETIME_PATTERN);

        return ozonOrderService.getOzonOrderList(clientIds,
                LocalDateTimeUtil.of(DateUtil.parseDateTime(beginDateTime).toInstant()),
                LocalDateTimeUtil.of(DateUtil.parseDateTime(endDateTime).toInstant()));
    }

    private List<OzonOrderItemDO> getDmOrderItemList(String[] clientIds, String date) {
        //获取指定日期的一天开始时间、结束时间
        String beginDateTime = DmDateUtils.formatStartOfDay(date, DatePattern.NORM_DATETIME_PATTERN);
        String endDateTime = DmDateUtils.formatEndOfDay(date, DatePattern.NORM_DATETIME_PATTERN);

        LocalDateTime[] dateTimes = new LocalDateTime[2];
        dateTimes[0] = LocalDateTimeUtil.of(DateUtil.parseDateTime(beginDateTime).toInstant());
        dateTimes[1] = LocalDateTimeUtil.of(DateUtil.parseDateTime(endDateTime).toInstant());
        return ozonOrderService.getOzonOrderItemListByClientId(clientIds, dateTimes);
    }

    private List<OzonAdCampaignsDO> getDmAdCampaigns(String[] clientIds, String date) {
        LocalDate localDate = LocalDate.parse(date, DateTimeFormatter.ofPattern(DatePattern.NORM_DATE_PATTERN));
        return dmAdCampaignsService.getOzonAdCampaignsListByClientIds(clientIds, localDate);
    }

    private String[] getClientIds(String[] clientIds) {
        if (null == clientIds || clientIds.length == 0) {
            List<OzonShopMappingDO> authShopList = authShopMappingService.getAuthShopList();
            return convertList(authShopList, OzonShopMappingDO::getClientId).toArray(new String[0]);
        }
        return clientIds;
    }
}
