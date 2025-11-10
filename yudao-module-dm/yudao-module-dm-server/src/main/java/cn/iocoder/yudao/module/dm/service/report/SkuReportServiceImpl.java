package cn.iocoder.yudao.module.dm.service.report;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.SortingField;
import cn.iocoder.yudao.module.dm.controller.admin.product.vo.ProductSimpleInfoVO;
import cn.iocoder.yudao.module.dm.controller.admin.report.vo.SkuReportQueryReqVO;
import cn.iocoder.yudao.module.dm.controller.admin.report.vo.SkuReportRespVO;
import cn.iocoder.yudao.module.dm.dal.dataobject.product.ProductInfoDO;
import cn.iocoder.yudao.module.dm.dal.mysql.product.ProductInfoMapper;
import cn.iocoder.yudao.module.fbs.api.WarehouseInventoryApi;
import cn.iocoder.yudao.module.fbs.api.WarehouseZoneApi;
import cn.iocoder.yudao.module.fbs.api.dto.WarehouseSalesDTO;
import cn.iocoder.yudao.module.fbs.spi.dto.InventoryDTO;
import cn.iocoder.yudao.module.sellfox.api.order.AmazonOrderService;
import cn.iocoder.yudao.module.sellfox.api.order.dto.AmazonOrderDTO;
import cn.iocoder.yudao.module.sellfox.api.order.dto.AmazonOrderItemDTO;
import cn.iocoder.yudao.module.sellfox.api.order.dto.AmazonOrderQueryReqDTO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * SKU报表 Service 实现类
 *
 * @author Jax
 */
@Service
@Slf4j
public class SkuReportServiceImpl implements SkuReportService {

    @Resource
    private ProductInfoMapper productInfoMapper;

    @Resource
    private AmazonOrderService amazonOrderService;

    @Resource
    private WarehouseZoneApi warehouseZoneApi;

    @Resource
    private WarehouseInventoryApi warehouseInventoryApi;

    @Override
    public PageResult<SkuReportRespVO> querySkuReport(SkuReportQueryReqVO reqVO) {
        // 1. 查询全部数据
        List<SkuReportRespVO> allData = querySkuReportList(reqVO);
        
        // 2. 排序
        List<SkuReportRespVO> sortedData = sortSkuReportList(allData, reqVO.getSortingFields());
        
        // 3. 分页
        return pageSkuReportList(sortedData, reqVO.getPageNo(), reqVO.getPageSize());
    }

    @Override
    public List<SkuReportRespVO> querySkuReportList(SkuReportQueryReqVO reqVO) {
        // 1. 查询产品基础信息
        List<ProductInfoDO> products = queryProducts(reqVO);
        if (CollUtil.isEmpty(products)) {
            return Collections.emptyList();
        }

        List<Long> productIds = products.stream()
                .map(ProductInfoDO::getId)
                .collect(Collectors.toList());

        // 2. 并行执行：查询订单 和 查询库存
        CompletableFuture<List<AmazonOrderDTO>> ordersFuture = CompletableFuture.supplyAsync(
                () -> queryAmazonOrders(reqVO)
        );

        CompletableFuture<Map<Long, Map<String, Integer>>> inventoryFuture = CompletableFuture.supplyAsync(
                () -> queryWarehouseInventory(productIds)
        );

        // 3. 等待订单查询完成后，并行执行：统计仓库销量
        CompletableFuture<Map<Long, List<WarehouseSalesDTO>>> warehouseSalesFuture = ordersFuture.thenApplyAsync(
                orders -> calculateWarehouseSalesByProduct(orders, reqVO)
        );

        CompletableFuture<Map<Long, Map<Integer, Double>>> platformSalesFuture = ordersFuture.thenApplyAsync(
                orders -> calculatePlatformSalesBySku(orders, reqVO)
        );

        // 4. 等待所有异步任务完成
        CompletableFuture.allOf(warehouseSalesFuture, platformSalesFuture, inventoryFuture).join();

        // 5. 获取结果
        Map<Long, List<WarehouseSalesDTO>> warehouseSalesByProductMap = warehouseSalesFuture.join();
        Map<Long, Map<Integer, Double>> platformSalesByProductIdMap = platformSalesFuture.join();
        Map<Long, Map<String, Integer>> inventoryMap = inventoryFuture.join();
        
        // 6. 计算天数
        int days = calculateDays(reqVO.getStartTime(), reqVO.getEndTime());

        // 7. 组装返回数据
        List<SkuReportRespVO> result = products.stream()
                .map(product -> buildSkuReport(product, warehouseSalesByProductMap, platformSalesByProductIdMap, inventoryMap, days))
                .collect(Collectors.toList());

        return result;
    }

    /**
     * 查询产品信息
     */
    private List<ProductInfoDO> queryProducts(SkuReportQueryReqVO reqVO) {
        LambdaQueryWrapper<ProductInfoDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(CollUtil.isNotEmpty(reqVO.getProductIds()),
                ProductInfoDO::getId, reqVO.getProductIds());
        wrapper.in(CollUtil.isNotEmpty(reqVO.getSkuIds()),
                ProductInfoDO::getSkuId, reqVO.getSkuIds());
        return productInfoMapper.selectList(wrapper);
    }

    /**
     * 查询亚马逊订单
     */
    private List<AmazonOrderDTO> queryAmazonOrders(SkuReportQueryReqVO reqVO) {
        try {
            AmazonOrderQueryReqDTO orderReq = new AmazonOrderQueryReqDTO();
            orderReq.setShopId(reqVO.getShopId());
            orderReq.setMarketplace(reqVO.getMarketplace());
            
            // 将 LocalDate 转换为 LocalDateTime
            if (reqVO.getStartTime() != null) {
                orderReq.setStartTime(reqVO.getStartTime().atStartOfDay());
            }
            if (reqVO.getEndTime() != null) {
                orderReq.setEndTime(reqVO.getEndTime().atTime(23, 59, 59));
            }

            CommonResult<List<AmazonOrderDTO>> result = amazonOrderService.queryOrdersByCondition(orderReq);
            if (result != null && result.getData() != null) {
                return result.getCheckedData();
            }
        } catch (Exception e) {
            log.error("查询亚马逊订单失败", e);
        }
        return Collections.emptyList();
    }

    /**
     * 按产品维度统计各仓库日均销量
     * 从订单明细中提取产品对应的订单邮编，批量调用RPC接口统计，然后除以天数得到日均销量
     */
    private Map<Long, List<WarehouseSalesDTO>> calculateWarehouseSalesByProduct(List<AmazonOrderDTO> orders, SkuReportQueryReqVO reqVO) {
        if (CollUtil.isEmpty(orders)) {
            return Collections.emptyMap();
        }

        try {
            // 计算天数
            int days = calculateDays(reqVO.getStartTime(), reqVO.getEndTime());
            if (days <= 0) {
                days = 1; // 至少按1天计算
            }
            
            // 按产品ID分组收集订单邮编
            // Map<产品ID, List<邮编>>
            Map<Long, List<String>> productZipCodesMap = new HashMap<>();
            
            for (AmazonOrderDTO order : orders) {
                // 跳过没有邮编的订单
                if (StringUtils.isBlank(order.getPostalCode())) {
                    continue;
                }
                
                // 邮编取前3位
                String zipCode = order.getPostalCode().length() >= 3 
                        ? order.getPostalCode().substring(0, 3) 
                        : order.getPostalCode();
                
                // 跳过没有商品明细的订单
                if (CollUtil.isEmpty(order.getItems())) {
                    continue;
                }
                
                // 遍历订单中的每个商品，记录该产品的订单邮编
                for (AmazonOrderItemDTO item : order.getItems()) {
                    Long localProductId = item.getLocalProductId();
                    if (localProductId == null) {
                        continue;
                    }
                    
                    // 获取商品数量
                    Integer quantity = item.getQuantityOrdered();
                    if (quantity == null || quantity <= 0) {
                        continue;
                    }
                    
                    // 根据商品数量重复添加邮编，以便统计商品销量而非订单数量
                    List<String> zipCodeList = productZipCodesMap.computeIfAbsent(localProductId, k -> new ArrayList<>());
                    for (int i = 0; i < quantity; i++) {
                        zipCodeList.add(zipCode);
                    }
                }
            }

            if (productZipCodesMap.isEmpty()) {
                return Collections.emptyMap();
            }

            // 批量调用RPC接口统计各产品在各仓库的销量
            CommonResult<Map<Long, List<WarehouseSalesDTO>>> result = 
                    warehouseZoneApi.countOrdersByProductAndWarehouse(productZipCodesMap);
            
            if (result != null && result.getData() != null) {
                Map<Long, List<WarehouseSalesDTO>> warehouseSalesMap = result.getData();
                log.info("按产品维度统计仓库销量完成: 产品数={}, 统计天数={}", warehouseSalesMap.size(), days);
                return warehouseSalesMap;
            }
        } catch (Exception e) {
            log.error("按产品维度统计仓库销量失败", e);
        }
        return Collections.emptyMap();
    }

    /**
     * 按本地产品ID统计各平台日均销量
     * 统计逻辑：遍历订单的商品明细列表，按 localProductId 累加销量，然后除以天数得到日均销量（保留2位小数）
     */
    private Map<Long, Map<Integer, Double>> calculatePlatformSalesBySku(List<AmazonOrderDTO> orders, SkuReportQueryReqVO reqVO) {
        if (CollUtil.isEmpty(orders)) {
            return Collections.emptyMap();
        }
        
        // 亚马逊平台ID
        Integer amazonPlatformId = 1;
        
        // 计算天数
        int days = calculateDays(reqVO.getStartTime(), reqVO.getEndTime());
        if (days <= 0) {
            days = 1; // 至少按1天计算
        }
        
        // 统计各本地产品ID的总销量
        Map<Long, Integer> productTotalSalesMap = new HashMap<>();
        
        for (AmazonOrderDTO order : orders) {
            // 跳过没有商品明细的订单
            if (CollUtil.isEmpty(order.getItems())) {
                continue;
            }
            
            // 遍历订单中的每个商品
            for (AmazonOrderItemDTO item : order.getItems()) {
                // 使用本地产品ID作为统计维度
                Long localProductId = item.getLocalProductId();
                if (localProductId == null) {
                    continue;
                }
                
                // 获取商品数量
                Integer quantity = item.getQuantityOrdered();
                if (quantity == null || quantity <= 0) {
                    continue;
                }
                
                // 累加该产品ID的销量
                productTotalSalesMap.merge(localProductId, quantity, Integer::sum);
            }
        }
        
        // 计算各产品的日均销量（保留2位小数）
        Map<Long, Map<Integer, Double>> result = new HashMap<>();
        for (Map.Entry<Long, Integer> entry : productTotalSalesMap.entrySet()) {
            Long productId = entry.getKey();
            Integer totalSales = entry.getValue();
            
            // 日均销量 = 总销量 / 天数，保留2位小数
            double dailyAvgSales = Math.round((double) totalSales / days * 100.0) / 100.0;
            
            // 构建平台销量Map
            Map<Integer, Double> platformSalesMap = new HashMap<>();
            platformSalesMap.put(amazonPlatformId, dailyAvgSales);
            
            result.put(productId, platformSalesMap);
        }
        
        log.info("按本地产品ID统计销量完成: 总订单数={}, 统计天数={}, 产品数={}", 
                orders.size(), days, result.size());
        
        return result;
    }

    /**
     * 计算时间范围的天数
     */
    private int calculateDays(java.time.LocalDate startTime, java.time.LocalDate endTime) {
        if (startTime == null || endTime == null) {
            return 7; // 默认7天
        }
        
        long days = java.time.temporal.ChronoUnit.DAYS.between(startTime, endTime);
        
        return (int) (days + 1); // 包含开始和结束日期
    }

    /**
     * 查询海外仓库存，并按产品ID和仓库代码统计库存数量
     * 
     * @param productIds 产品ID列表
     * @return Map<产品ID, Map<仓库代码, 库存数量>>
     */
    private Map<Long, Map<String, Integer>> queryWarehouseInventory(List<Long> productIds) {
        try {
            CommonResult<List<InventoryDTO>> result =
                    warehouseInventoryApi.batchGetInventoryByLocalProductIds(productIds);

            if (result != null && result.getData() != null) {
                // 按产品ID分组，然后按仓库代码统计库存数量
                return result.getData().stream()
                        .filter(inv -> inv.getLocalProductId() != null)
                        .filter(inv -> StrUtil.isNotBlank(inv.getWarehouseCode()))
                        .collect(Collectors.groupingBy(
                                InventoryDTO::getLocalProductId,
                                Collectors.groupingBy(
                                        InventoryDTO::getWarehouseCode,
                                        Collectors.summingInt(inv -> inv.getAvailableQty() != null ? inv.getAvailableQty() : 0)
                                )
                        ));
            }
        } catch (Exception e) {
            log.error("查询海外仓库存失败", e);
        }
        return Collections.emptyMap();
    }

    /**
     * 组装SKU报表数据
     */
    private SkuReportRespVO buildSkuReport(
            ProductInfoDO product,
            Map<Long, List<WarehouseSalesDTO>> warehouseSalesByProductMap,
            Map<Long, Map<Integer, Double>> platformSalesByProductIdMap,
            Map<Long, Map<String, Integer>> inventoryMap,
            int days) {

        SkuReportRespVO vo = new SkuReportRespVO();

        // 基础信息
        vo.setProductId(product.getId());
        vo.setSku(product.getSkuId());
        vo.setPlatformSkuId(""); // TODO: 需要补充平台关联信息逻辑
        
        // 构建产品简单信息
        ProductSimpleInfoVO productSimpleInfo = new ProductSimpleInfoVO();
        productSimpleInfo.setProductId(product.getId());
        productSimpleInfo.setSkuId(product.getSkuId());
        productSimpleInfo.setSkuName(product.getSkuName());
        productSimpleInfo.setImage(product.getPictureUrl());
        productSimpleInfo.setProductType(product.getProductType());
        vo.setProductSimpleInfo(productSimpleInfo);

        // 获取该产品的各仓库库存Map（已经按仓库代码统计好的）
        Map<String, Integer> warehouseInventoryMap = inventoryMap.getOrDefault(
                product.getId(), 
                Collections.emptyMap()
        );

        // 设置各仓库库存Map
        vo.setWarehouseInventoryMap(warehouseInventoryMap);

        // 海外仓总库存
        int overseasTotal = warehouseInventoryMap.values().stream()
                .mapToInt(Integer::intValue)
                .sum();
        vo.setOverseasTotalQty(overseasTotal);

        // TODO: FBA库存需要从其他地方获取
        vo.setFbaAvailableQty(0);
        vo.setFbaInboundQty(0);

        // 总库存 = 海外仓 + FBA
        vo.setTotalQty(overseasTotal + vo.getFbaAvailableQty());

        // 从 List<WarehouseSalesDTO>中获取该产品的各仓库销量，计算日均销量并转换为Map
        List<WarehouseSalesDTO> salesList = warehouseSalesByProductMap.get(product.getId());
        Map<String, Double> warehouseSalesMap = CollUtil.isEmpty(salesList) 
                ? Collections.emptyMap()
                : salesList.stream().collect(Collectors.toMap(
                        WarehouseSalesDTO::getWarehouseCode,
                        sales -> Math.round((double) sales.getSalesCount() / days * 100.0) / 100.0,  // 总销量除以天数得到日均销量，保留2位小数
                        Double::sum  // 合并相同仓库的销量（理论上不会重复，但为了安全性）
                ));
        vo.setWarehouseSalesMap(warehouseSalesMap);

        // 获取该产品的平台日均销量Map（按本地产品ID从销量Map中获取）
        Map<Integer, Double> platformSalesMap = platformSalesByProductIdMap.getOrDefault(
                product.getId(),
                Collections.emptyMap()
        );
        vo.setPlatformSalesMap(platformSalesMap);
        
        // 计算平台合计日销量：各平台日销量之和除以平台数量
        if (!platformSalesMap.isEmpty()) {
            double totalPlatformSales = platformSalesMap.values().stream()
                    .mapToDouble(Double::doubleValue)
                    .sum();
            double avgPlatformSales = totalPlatformSales / platformSalesMap.size();
            // 保留2位小数
            vo.setPlatformAvgDailySales(Math.round(avgPlatformSales * 100.0) / 100.0);
        } else {
            vo.setPlatformAvgDailySales(0.0);
        }
        
        // 设置统计天数
        vo.setDays(days);

        // 计算可销售天数
        calculateSalesMetrics(vo, platformSalesMap);

        return vo;
    }

    /**
     * 计算销售指标
     */
    private void calculateSalesMetrics(SkuReportRespVO vo, Map<Integer, Double> platformSalesMap) {
        // 计算所有平台的日均销量
        double totalSales = platformSalesMap.values().stream()
                .mapToDouble(Double::doubleValue)
                .sum();

        // 可销售天数 = 总库存 / 日均销量
        // 如果日均销量为0，则显示最大值90天
        if (totalSales > 0) {
            int availableDays = (int) (vo.getTotalQty() / totalSales);
            vo.setAvailableDays(availableDays);
        }
    }

    /**
     * 对SKU报表数据进行排序
     */
    private List<SkuReportRespVO> sortSkuReportList(List<SkuReportRespVO> list, List<SortingField> sortingFields) {
        if (CollUtil.isEmpty(list) || CollUtil.isEmpty(sortingFields)) {
            return list;
        }

        // 使用Java 8 Stream进行多字段排序
        Comparator<SkuReportRespVO> comparator = null;
        
        for (SortingField sortingField : sortingFields) {
            Comparator<SkuReportRespVO> fieldComparator = getComparator(sortingField.getField(), sortingField.getOrder());
            if (fieldComparator != null) {
                comparator = (comparator == null) ? fieldComparator : comparator.thenComparing(fieldComparator);
            }
        }
        
        if (comparator != null) {
            return list.stream().sorted(comparator).collect(Collectors.toList());
        }
        
        return list;
    }

    /**
     * 根据字段名和排序方向获取比较器
     */
    private Comparator<SkuReportRespVO> getComparator(String field, String order) {
        boolean isAsc = SortingField.ORDER_ASC.equals(order);
        
        Comparator<SkuReportRespVO> comparator = null;
        
        switch (field) {
            case "overseasTotalQty":
                comparator = Comparator.comparing(SkuReportRespVO::getOverseasTotalQty, 
                        Comparator.nullsLast(Integer::compareTo));
                break;
            case "totalQty":
                comparator = Comparator.comparing(SkuReportRespVO::getTotalQty, 
                        Comparator.nullsLast(Integer::compareTo));
                break;
            case "platformAvgDailySales":
                comparator = Comparator.comparing(SkuReportRespVO::getPlatformAvgDailySales, 
                        Comparator.nullsLast(Double::compareTo));
                break;
            case "availableDays":
                comparator = Comparator.comparing(SkuReportRespVO::getAvailableDays, 
                        Comparator.nullsLast(Integer::compareTo));
                break;
            case "sku":
                comparator = Comparator.comparing(SkuReportRespVO::getSku, 
                        Comparator.nullsLast(String::compareTo));
                break;
            case "productId":
                comparator = Comparator.comparing(SkuReportRespVO::getProductId, 
                        Comparator.nullsLast(Long::compareTo));
                break;
            default:
                log.warn("不支持的排序字段: {}", field);
                return null;
        }
        
        // 如果是降序,反转比较器
        return isAsc ? comparator : comparator.reversed();
    }

    /**
     * 对SKU报表数据进行内存分页
     */
    private PageResult<SkuReportRespVO> pageSkuReportList(List<SkuReportRespVO> list, Integer pageNo, Integer pageSize) {
        if (CollUtil.isEmpty(list)) {
            return PageResult.empty();
        }
        
        // 计算总数
        long total = list.size();
        
        // 计算分页起始位置
        int start = (pageNo - 1) * pageSize;
        if (start >= total) {
            return new PageResult<>(Collections.emptyList(), total);
        }
        
        // 计算分页结束位置
        int end = Math.min(start + pageSize, (int) total);
        
        // 截取分页数据
        List<SkuReportRespVO> pageData = list.subList(start, end);
        
        return new PageResult<>(pageData, total);
    }
}
