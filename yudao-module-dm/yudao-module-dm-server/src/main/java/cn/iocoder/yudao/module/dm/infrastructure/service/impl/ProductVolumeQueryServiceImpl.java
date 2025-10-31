package cn.iocoder.yudao.module.dm.infrastructure.service.impl;

import cn.hutool.core.date.DatePattern;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.dm.controller.admin.product.vo.ProductSimpleInfoVO;
import cn.iocoder.yudao.module.dm.controller.admin.statistics.vo.ProductVolumeReqVO;
import cn.iocoder.yudao.module.dm.controller.admin.statistics.vo.ProductVolumeRespVO;
import cn.iocoder.yudao.module.dm.dal.dataobject.online.OzonProductOnlineDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.order.OzonOrderItemDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.ozonshopmapping.OzonShopMappingDO;
import cn.iocoder.yudao.module.dm.enums.VolumeQueryTypeEnum;
import cn.iocoder.yudao.module.dm.infrastructure.ozon.utils.DmDateUtils;
import cn.iocoder.yudao.module.dm.infrastructure.service.ProductVolumeQueryService;
import cn.iocoder.yudao.module.dm.infrastructure.service.ProductVolumeShopQueryService;
import cn.iocoder.yudao.module.dm.infrastructure.service.dto.ProductVolumeDTO;
import cn.iocoder.yudao.module.dm.service.online.OzonProductOnlineService;
import cn.iocoder.yudao.module.dm.service.order.OzonOrderService;
import cn.iocoder.yudao.module.dm.service.ozonshopmapping.OzonShopMappingService;
import cn.iocoder.yudao.module.dm.service.product.ProductInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.*;

/**
 * @author: Zeno
 * @createTime: 2024/08/05 16:19
 */
@Service
@Slf4j
public class ProductVolumeQueryServiceImpl implements 
        ProductVolumeQueryService<PageResult<ProductVolumeRespVO>, ProductVolumeReqVO>,
        ProductVolumeShopQueryService {

    @Resource
    private OzonOrderService ozonOrderService;
    @Resource
    private OzonProductOnlineService ozonProductOnlineService;
    @Resource
    private OzonShopMappingService ozonShopMappingService;
    @Resource
    private ProductInfoService productInfoService;

    @Override
    public PageResult<ProductVolumeRespVO> queryProductVolume(ProductVolumeReqVO reqVO) {
        // 记录请求的原始参数，便于调试
        log.info("产品销量查询请求参数: 日期范围={}~{}, 查询类型={}, 产品ID={}", 
                reqVO.getDate()[0], reqVO.getDate()[1], reqVO.getQueryType(), reqVO.getProductId());
        
        // 解析为莫斯科时区的LocalDate对象
        String[] dateRange = reqVO.getDate();
        LocalDate startDateMoscow = LocalDate.parse(dateRange[0], DateTimeFormatter.ISO_DATE);
        LocalDate endDateMoscow = LocalDate.parse(dateRange[1], DateTimeFormatter.ISO_DATE);
        
        log.info("产品销量查询，莫斯科时区日期范围: {} 至 {}", startDateMoscow, endDateMoscow);
        
        // 转换为UTC时间范围，用于SQL查询
        LocalDateTime[] utcTimeRange = DmDateUtils.convertMoscowDateRangeToUtcTime(dateRange[0], dateRange[1]);
        log.info("对应的UTC时间范围: {} 至 {}", 
                DatePattern.NORM_DATETIME_FORMATTER.format(utcTimeRange[0]),
                DatePattern.NORM_DATETIME_FORMATTER.format(utcTimeRange[1]));
                
        if (log.isDebugEnabled()) {
            // 转换回莫斯科时区，用于验证时间边界
            String moscowTimeRangeStr = DmDateUtils.formatUtcTimeRangeToMoscow(utcTimeRange[0], utcTimeRange[1]);
            log.debug("验证转换结果: 莫斯科时区时间范围: {}", moscowTimeRangeStr);
        }

        // 根据查询类型选择不同的查询方法
        PageResult<OzonOrderItemDO> orderItemPage;
        if (VolumeQueryTypeEnum.SHOP_TYPE.getType().equals(reqVO.getQueryType())) {
            // 使用门店维度查询 - 通过XML中的SQL实现
            orderItemPage = ozonOrderService.getShopOrderItemPage(reqVO);
        } else if (VolumeQueryTypeEnum.SKU_TYPE.getType().equals(reqVO.getQueryType())) {
            // 使用SKU维度查询 - 通过XML中的SQL实现
            orderItemPage = ozonOrderService.getSkuOrderItemPage(reqVO);
        } else {
            // 使用商品维度查询
            orderItemPage = ozonOrderService.getOrderItemPage(reqVO);
        }

        // 获取请求中的日期范围，所有数据 - 直接使用已转换的UTC时间范围
        List<OzonOrderItemDO> dmOrderItemList = getDmOrderItemList(reqVO.getClientIds(), utcTimeRange);

        // 获取所有门店信息，用于展示门店名称
        Map<String, OzonShopMappingDO> shopMap = getShopMappingMap();

        PageResult<ProductVolumeRespVO> page = new PageResult<>();
        page.setTotal(orderItemPage.getTotal());

        List<ProductVolumeRespVO> respVOList = new ArrayList<>();

        // 使用DmDateUtils新方法生成日期列表，基于莫斯科时区
        List<String> dateList = DmDateUtils.generateMoscowDateRangeList(reqVO.getDateType(), startDateMoscow, endDateMoscow);
        
        if (log.isDebugEnabled()) {
            log.debug("生成的日期区间列表: {}", dateList);
        }
        
        ProductVolumeRespVO respVO = new ProductVolumeRespVO();
        respVO.setDateList(dateList);

        // 查询类型为门店时的特殊处理
        if (VolumeQueryTypeEnum.SHOP_TYPE.getType().equals(reqVO.getQueryType())) {
            List<ProductVolumeDTO> shopVolumeList = new ArrayList<>();
            for (OzonOrderItemDO item : orderItemPage.getList()) {
                // 创建门店销量DTO
                ProductVolumeDTO shopVolumeDTO = new ProductVolumeDTO();
                shopVolumeDTO.setClientId(item.getClientId());
                
                // 设置门店信息
                setShopInfo(shopVolumeDTO, item.getClientId(), shopMap);
                
                Map<String, Integer> volumeMap = new TreeMap<>(Comparator.reverseOrder());
                // 使用查询结果中已经统计好的总销量
                int totalVolume = item.getQuantity();
                
                // 对每个日期区间计算该门店的销量，基于莫斯科时区
                for (String dateRangeItem : dateList) {
                    // 按门店和日期范围过滤并计算销量
                    int volume = getShopVolumeForDateRange(item.getClientId(), dateRangeItem, dmOrderItemList);
                    volumeMap.put(dateRangeItem, volume);
                }
                
                // 设置总销量和均值
                shopVolumeDTO.setTotal(totalVolume);
                shopVolumeDTO.setAvg(!volumeMap.isEmpty() ? totalVolume / volumeMap.size() : 0);
                shopVolumeDTO.setVolumeMap(volumeMap);
                
                shopVolumeList.add(shopVolumeDTO);
            }
            respVO.setVolumeList(shopVolumeList);
            respVOList.add(respVO);
            page.setList(respVOList);
            return page;
        }
        
        // 查询类型为SKU时的特殊处理
        if (VolumeQueryTypeEnum.SKU_TYPE.getType().equals(reqVO.getQueryType())) {
            List<ProductVolumeDTO> skuVolumeList = new ArrayList<>();
            
            // 检查结果列表
            List<OzonOrderItemDO> validItems = orderItemPage.getList().stream()
                    .filter(item -> item.getDmProductId() != null)
                    .collect(Collectors.toList());
            
            // 如果过滤后的列表与原始列表大小不同，记录日志
            if (validItems.size() != orderItemPage.getList().size()) {
                log.warn("查询结果中存在dmProductId为null的记录，已过滤掉。原始数量：{}，过滤后数量：{}", 
                        orderItemPage.getList().size(), validItems.size());
            }
            
            // 获取所有dmProductIds，用于批量查询
            List<Long> dmProductIds = validItems.stream()
                    .map(OzonOrderItemDO::getDmProductId)
                    .distinct()
                    .collect(Collectors.toList());

            Map<Long, ProductSimpleInfoVO> simpleInfoVOMap = productInfoService.batchQueryProductSimpleInfo(dmProductIds);
            // 批量查询在线商品信息
            List<OzonProductOnlineDO> allRelatedProducts = ozonProductOnlineService.listOzonProductOnlinesByDmProductIds(dmProductIds);
            
            // 按dmProductId分组商品
            Map<Long, List<OzonProductOnlineDO>> productMap = allRelatedProducts.stream()
                    .collect(Collectors.groupingBy(OzonProductOnlineDO::getDmProductId));
            
            // 预处理：收集每个dmProductId对应的所有 platformSkuId，用于后续销量统计
            Map<Long, Set<String>> dmProductPlatformSkuIdsMap = new HashMap<>();
            for (Long dmProductId : dmProductIds) {
                List<OzonProductOnlineDO> products = productMap.getOrDefault(dmProductId, Collections.emptyList());
                Set<String> platformSkuIds = products.stream()
                        .map(OzonProductOnlineDO::getPlatformSkuId)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet());
                dmProductPlatformSkuIdsMap.put(dmProductId, platformSkuIds);
            }
            
            for (OzonOrderItemDO item : validItems) {
                // 创建SKU销量DTO
                ProductVolumeDTO skuVolumeDTO = new ProductVolumeDTO();
                skuVolumeDTO.setDmProductId(item.getDmProductId());
                
                // 获取该dmProductId关联的所有在线商品
                List<OzonProductOnlineDO> relatedProducts = productMap.getOrDefault(item.getDmProductId(), Collections.emptyList());
                
                // 获取相关门店信息，并添加到门店列表中
                List<ProductVolumeDTO.ShopInfo> shopInfoList = new ArrayList<>();
                if (!relatedProducts.isEmpty()) {
                    // 设置商品图片（取第一个商品的图片）
                    skuVolumeDTO.setImage(relatedProducts.get(0).getImage());
                    // 设置offerId
                    skuVolumeDTO.setOfferId(relatedProducts.get(0).getOfferId());
                    skuVolumeDTO.setSkuId(simpleInfoVOMap.get(relatedProducts.get(0).getDmProductId()).getSkuId());

                    // 创建clientIds集合，用于过滤门店
                    Set<String> requestedClientIds = reqVO.getClientIds() != null ? 
                            new HashSet<>(Arrays.asList(reqVO.getClientIds())) : null;
                    
                    // 收集门店信息
                    for (OzonProductOnlineDO product : relatedProducts) {
                        // 如果指定了clientIds参数，则只添加这些指定的门店
                        if (requestedClientIds != null && !requestedClientIds.isEmpty() && !requestedClientIds.contains(product.getClientId())) {
                            continue; // 跳过不在请求clientIds列表中的门店
                        }
                        
                        OzonShopMappingDO shopInfo = shopMap.get(product.getClientId());
                        if (shopInfo != null) {
                            shopInfoList.add(new ProductVolumeDTO.ShopInfo(
                                    product.getClientId(),
                                    shopInfo.getShopName(),
                                    shopInfo.getPlatform()
                            ));
                        }
                    }
                }
                skuVolumeDTO.setShops(shopInfoList);
                
                // 使用SQL查询结果中的总销量作为准确值
                int totalVolume = item.getQuantity();
                
                // 计算各日期区间的销量（基于SQL查询结果的总销量比例分配，而不是重新计算）
                Map<String, Integer> volumeMap = calculateVolumeMapBasedOnSqlTotal(
                        item.getDmProductId(), 
                        dateList, 
                        dmOrderItemList, 
                        dmProductPlatformSkuIdsMap.getOrDefault(item.getDmProductId(), Collections.emptySet()),
                        totalVolume);
                
                // 计算实际总销量
                int calculatedTotal = volumeMap.values().stream().mapToInt(Integer::intValue).sum();
                
                // 设置计算出的实际销量
                skuVolumeDTO.setVolumeMap(volumeMap);
                skuVolumeDTO.setTotal(calculatedTotal);
                skuVolumeDTO.setAvg(!volumeMap.isEmpty() ? calculatedTotal / volumeMap.size() : 0);
                
                // 记录日志，比较计算销量与SQL查询销量的差异
                if (log.isDebugEnabled() && calculatedTotal != totalVolume) {
                    log.debug("SKU[{}]销量差异: SQL查询总销量={}, 计算总销量={}, 差值={}", 
                            item.getDmProductId(), totalVolume, calculatedTotal, calculatedTotal - totalVolume);
                }
                
                skuVolumeList.add(skuVolumeDTO);
            }
            
            respVO.setVolumeList(skuVolumeList);
            respVOList.add(respVO);
            page.setList(respVOList);
            return page;
        }

        // 下面是商品维度的处理代码（原有逻辑）
        // 查询在线商品的基础信息
        List<String> offerIds = convertList(orderItemPage.getList(), OzonOrderItemDO::getOfferId).stream().distinct().collect(Collectors.toList());
        List<OzonProductOnlineDO> productOnlineDOList = ozonProductOnlineService.batchOzonProductOnlineByOfferId(offerIds);
        Map<String, OzonProductOnlineDO> productOnlineDOMap = convertMap(productOnlineDOList, OzonProductOnlineDO::getOfferId);


        List<ProductVolumeDTO> volumeList = new ArrayList<>();
        for (OzonOrderItemDO item : orderItemPage.getList()) {

            ProductVolumeDTO volumeDTO = new ProductVolumeDTO();
            volumeDTO.setOfferId(item.getOfferId());
            volumeDTO.setClientId(item.getClientId());
            
            // 设置门店信息
            setShopInfo(volumeDTO, item.getClientId(), shopMap);

            Map<String, Integer> volumeMap = new TreeMap<>(Comparator.reverseOrder());
            for (String dateRangeItem : dateList) {
                // 根据日期范围和订单项计算销量
                int volume = getVolumeForDateRange(item, dateRangeItem, dmOrderItemList);
                volumeMap.put(dateRangeItem, volume);
            }

            // 计算总销量和均值
            int total = item.getQuantity();
            int avg = !volumeMap.isEmpty() ? total / volumeMap.size() : 0;

            volumeDTO.setTotal(total);
            volumeDTO.setAvg(avg);
            volumeDTO.setVolumeMap(volumeMap);

            OzonProductOnlineDO ozonProductOnlineDO = productOnlineDOMap.get(item.getOfferId());
            if (ozonProductOnlineDO != null) {
                volumeDTO.setImage(ozonProductOnlineDO.getImage());
            }

            volumeList.add(volumeDTO);
        }
        respVO.setVolumeList(volumeList);
        respVOList.add(respVO);
        page.setList(respVOList);
        return page;
    }

    private int getVolumeForDateRange(OzonOrderItemDO item, String dateRange, List<OzonOrderItemDO> itemList) {
        // 解析 dateRange，支持单日期和范围日期 - 已经是莫斯科时区的日期
        String[] dates = dateRange.split("~");
        LocalDate startDate = LocalDate.parse(dates[0], DateTimeFormatter.ISO_DATE);
        LocalDate endDate = dates.length > 1 ? LocalDate.parse(dates[1], DateTimeFormatter.ISO_DATE) : startDate;

        // 初始化销量统计
        int totalVolume = 0;

        // 遍历 itemList，根据 inProcessAt 日期筛选并统计销量
        for (OzonOrderItemDO orderItem : itemList) {
            if (orderItem.getOfferId().equals(item.getOfferId()) && item.getClientId().equals(orderItem.getClientId())) {
                LocalDateTime inProcessAt = orderItem.getInProcessAt();
                if (inProcessAt != null) {
                    // 将UTC时间转换为莫斯科时区的LocalDate
                    LocalDate moscowDate = DmDateUtils.convertUtcToMoscowLocalDate(inProcessAt);
                    
                    // 使用莫斯科时区的日期进行比较
                    if ((moscowDate.isEqual(startDate) || moscowDate.isAfter(startDate)) &&
                            (moscowDate.isEqual(endDate) || moscowDate.isBefore(endDate))) {
                        totalVolume += orderItem.getQuantity();
                    }
                }
            }
        }

        return totalVolume;
    }

    /**
     * 获取订单项列表，直接使用UTC时间范围
     * 
     * @param clientIds 客户端ID数组
     * @param dateTimes UTC时间范围
     * @return 订单项列表
     */
    private List<OzonOrderItemDO> getDmOrderItemList(String[] clientIds, LocalDateTime[] dateTimes) {
        List<OzonOrderItemDO> orderItems = ozonOrderService.getOzonOrderItemListByClientId(clientIds, dateTimes);
        
        if (log.isDebugEnabled()) {
            log.debug("查询到订单项数量: {}", orderItems.size());
        }
        
        return orderItems;
    }

    /**
     * 计算指定门店在指定日期范围内的销量，基于莫斯科时区
     */
    private int getShopVolumeForDateRange(String clientId, String dateRange, List<OzonOrderItemDO> itemList) {
        // 解析日期范围 - 已经是莫斯科时区的日期
        String[] dates = dateRange.split("~");
        LocalDate startDate = LocalDate.parse(dates[0], DateTimeFormatter.ISO_DATE);
        LocalDate endDate = dates.length > 1 ? LocalDate.parse(dates[1], DateTimeFormatter.ISO_DATE) : startDate;
        
        // 对应到数据库时间需要考虑时区
        // 创建开始和结束时间
        LocalDateTime[] utcDateTimeRange = DmDateUtils.convertMoscowDateRangeToUtcTime(
                startDate.toString(), endDate.toString());
        
        LocalDateTime startDateTime = utcDateTimeRange[0];
        LocalDateTime endDateTime = utcDateTimeRange[1];
        
        if (log.isDebugEnabled()) {
            log.debug("计算门店销量，clientId={}, 莫斯科时区范围: {} ~ {}, 对应UTC时间: {} ~ {}", 
                    clientId, startDate, endDate, startDateTime, endDateTime);
        }
        
        // 初始化销量
        int totalVolume = 0;
        
        // 遍历所有订单项，找出符合条件的进行统计
        for (OzonOrderItemDO orderItem : itemList) {
            if (orderItem.getClientId().equals(clientId)) {
                LocalDateTime inProcessAt = orderItem.getInProcessAt();
                if (inProcessAt != null) {
                    // 数据库时间是UTC，直接与UTC时间范围比较
                    if ((inProcessAt.isEqual(startDateTime) || inProcessAt.isAfter(startDateTime)) &&
                            (inProcessAt.isEqual(endDateTime) || inProcessAt.isBefore(endDateTime))) {
                        totalVolume += orderItem.getQuantity();
                    }
                }
            }
        }
        
        return totalVolume;
    }

    /**
     * 获取门店映射Map
     */
    private Map<String, OzonShopMappingDO> getShopMappingMap() {
        List<OzonShopMappingDO> shopList = ozonShopMappingService.getOzonShopList();
        return convertMap(shopList, OzonShopMappingDO::getClientId);
    }

    /**
     * 设置门店信息
     */
    private void setShopInfo(ProductVolumeDTO volumeDTO, String clientId, Map<String, OzonShopMappingDO> shopMap) {
        OzonShopMappingDO shopMapping = shopMap.get(clientId);
        if (shopMapping != null) {
            volumeDTO.setShopName(shopMapping.getShopName());
            volumeDTO.setPlatform(shopMapping.getPlatform());
        } else {
            // 如果找不到门店信息，使用clientId作为门店名称
            volumeDTO.setShopName(clientId);
        }
    }

    /**
     * 根据实际日期分布计算各日期区间的销量
     * 不再使用比例分配的方式，而是直接计算每一天的实际销量后汇总到日期范围
     */
    private Map<String, Integer> calculateVolumeMapBasedOnSqlTotal(
            Long dmProductId, 
            List<String> dateList, 
            List<OzonOrderItemDO> orderItemList,
            Set<String> platformSkuIds,
            int sqlTotalVolume) {
        
        // 按莫斯科日期统计各日期的销量
        Map<String, Integer> volumeMap = new TreeMap<>(Comparator.reverseOrder());
        Map<LocalDate, Integer> dailyVolume = new HashMap<>(); // 临时存储每天的销量
        
        // 初始化所有日期的销量为0
        for (String dateStr : dateList) {
            if (dateStr.contains("~")) {
                // 处理日期范围
                String[] dates = dateStr.split("~");
                LocalDate start = LocalDate.parse(dates[0]);
                LocalDate end = LocalDate.parse(dates[1]);
                // 初始化范围内每一天
                LocalDate current = start;
                while (!current.isAfter(end)) {
                    dailyVolume.put(current, 0);
                    current = current.plusDays(1);
                }
                volumeMap.put(dateStr, 0);
            } else {
                // 单日
                LocalDate date = LocalDate.parse(dateStr);
                dailyVolume.put(date, 0);
                volumeMap.put(dateStr, 0);
            }
        }
        
        // 打印调试信息
        if (log.isDebugEnabled()) {
            log.debug("处理产品销量统计，dmProductId={}, 待匹配的platformSkuIds数量={}",
                    dmProductId, platformSkuIds.size());
        }
        
        int matchCount = 0; // 匹配计数
        
        // 遍历所有订单项，按莫斯科日期统计
        for (OzonOrderItemDO item : orderItemList) {
            // 必须有处理时间
            if (item.getInProcessAt() == null) {
                continue;
            }
            
            if (!platformSkuIds.contains(item.getPlatformSkuId())) {
                continue;
            }
            
            matchCount++; // 增加匹配计数
            
            // 将UTC时间转换为莫斯科时区的LocalDate
            LocalDate moscowDate = DmDateUtils.convertUtcToMoscowLocalDate(item.getInProcessAt());
            
            // 累加到对应日期的销量中
            dailyVolume.compute(moscowDate, (date, count) -> count != null ? count + item.getQuantity() : item.getQuantity());
        }
        
        if (log.isDebugEnabled()) {
            log.debug("产品[{}]：匹配到{}个订单项", dmProductId, matchCount);
        }
        
        // 将每日销量汇总到日期范围中
        int calculatedTotal = 0; // 累计计算的总销量
        for (String dateStr : dateList) {
            if (dateStr.contains("~")) {
                // 处理日期范围
                String[] dates = dateStr.split("~");
                LocalDate start = LocalDate.parse(dates[0]);
                LocalDate end = LocalDate.parse(dates[1]);
                
                // 累加范围内每一天的销量
                int rangeVolume = 0;
                LocalDate current = start;
                while (!current.isAfter(end)) {
                    rangeVolume += dailyVolume.getOrDefault(current, 0);
                    current = current.plusDays(1);
                }
                
                volumeMap.put(dateStr, rangeVolume);
                calculatedTotal += rangeVolume;
            } else {
                // 单日
                LocalDate date = LocalDate.parse(dateStr);
                int dayVolume = dailyVolume.getOrDefault(date, 0);
                volumeMap.put(dateStr, dayVolume);
                calculatedTotal += dayVolume;
            }
        }
        
        // 记录计算结果与SQL查询结果的差异
        if (log.isDebugEnabled()) {
            log.debug("dmProductId: {}, 计算总销量={}, SQL总销量={}, 差值={}", 
                    dmProductId, calculatedTotal, sqlTotalVolume, calculatedTotal - sqlTotalVolume);
        }
        
        return volumeMap;
    }

    @Override
    public List<ProductVolumeRespVO> queryProductVolumeByShop(ProductVolumeReqVO reqVO) {
        // 记录请求的原始参数，便于调试
        log.info("店铺销量查询请求参数: 日期范围={}~{}, 查询类型={}", 
                reqVO.getDate()[0], reqVO.getDate()[1], reqVO.getQueryType());
                
        // 解析为莫斯科时区的LocalDate对象
        String[] dateRange = reqVO.getDate();
        LocalDate startDateMoscow = LocalDate.parse(dateRange[0], DateTimeFormatter.ISO_DATE);
        LocalDate endDateMoscow = LocalDate.parse(dateRange[1], DateTimeFormatter.ISO_DATE);
        
        // 转换为UTC时间范围，用于SQL查询
        LocalDateTime[] utcTimeRange = DmDateUtils.convertMoscowDateRangeToUtcTime(dateRange[0], dateRange[1]);
        
        // 使用DmDateUtils生成日期列表，基于莫斯科时区
        List<String> dateList = DmDateUtils.generateMoscowDateRangeList(reqVO.getDateType(), startDateMoscow, endDateMoscow);
        
        // 获取指定日期范围内的所有订单项（用于按日期统计销量）
        List<OzonOrderItemDO> allOrderItems = getDmOrderItemList(reqVO.getClientIds(), utcTimeRange);
        
        // 按客户端ID分组
        Map<String, List<OzonOrderItemDO>> orderItemsByClient = allOrderItems.stream()
                .collect(Collectors.groupingBy(OzonOrderItemDO::getClientId));
        
        // 创建返回结果
        ProductVolumeRespVO respVO = new ProductVolumeRespVO();
        respVO.setDateList(dateList);
        List<ProductVolumeDTO> volumeList = new ArrayList<>();
        
        // 直接使用新增的getSkuOrderItem方法获取按店铺分组的基础数据
        List<ProductVolumeDTO> baseDtoList = ozonOrderService.getSkuOrderItem(reqVO);
        if (baseDtoList.isEmpty()) {
            return Collections.emptyList();
        }
        
        // 处理每个店铺的数据
        for (ProductVolumeDTO baseDto : baseDtoList) {
            String clientId = baseDto.getClientId();
            if (clientId == null) {
                log.warn("发现没有clientId的记录，offerId={}", baseDto.getOfferId());
                continue;
            }
            
            // 获取该客户端对应的订单项
            List<OzonOrderItemDO> clientOrderItems = orderItemsByClient.getOrDefault(clientId, Collections.emptyList());
            
            // 按莫斯科日期统计各日期的销量
            Map<String, Integer> volumeMap = new TreeMap<>(Comparator.reverseOrder());
            Map<LocalDate, Integer> dailyVolume = new HashMap<>(); // 临时存储每天的销量
            
            // 初始化所有日期的销量为0
            for (String dateStr : dateList) {
                if (dateStr.contains("~")) {
                    // 处理日期范围
                    String[] dates = dateStr.split("~");
                    LocalDate start = LocalDate.parse(dates[0]);
                    LocalDate end = LocalDate.parse(dates[1]);
                    // 初始化范围内每一天
                    LocalDate current = start;
                    while (!current.isAfter(end)) {
                        dailyVolume.put(current, 0);
                        current = current.plusDays(1);
                    }
                    volumeMap.put(dateStr, 0);
                } else {
                    // 单日
                    LocalDate date = LocalDate.parse(dateStr);
                    dailyVolume.put(date, 0);
                    volumeMap.put(dateStr, 0);
                }
            }
            
            // 打印调试信息
            log.info("处理店铺[{}]的销量统计，dmProductId={}, offerId={}, 订单项数量={}",
                    clientId, baseDto.getDmProductId(), baseDto.getOfferId(), clientOrderItems.size());
            
            int matchCount = 0; // 匹配计数
            
            // 遍历该店铺所有订单项，按莫斯科日期统计
            for (OzonOrderItemDO item : clientOrderItems) {
                // 必须有处理时间
                if (item.getInProcessAt() == null) {
                    continue;
                }
                
                if (!Objects.equals(item.getPlatformSkuId(), baseDto.getPlatformSkuId())) {
                    continue;
                }
                
                matchCount++; // 增加匹配计数
                
                // 将UTC时间转换为莫斯科时区的LocalDate
                LocalDate moscowDate = DmDateUtils.convertUtcToMoscowLocalDate(item.getInProcessAt());
                
                // 累加到对应日期的销量中
                dailyVolume.compute(moscowDate, (date, count) -> count != null ? count + item.getQuantity() : item.getQuantity());
            }
            
            log.info("店铺[{}]的产品[{}]：匹配到{}个订单项", clientId, baseDto.getOfferId(), matchCount);
            
            // 将每日销量汇总到日期范围中
            int calculatedTotal = 0; // 累计计算的总销量
            for (String dateStr : dateList) {
                if (dateStr.contains("~")) {
                    // 处理日期范围
                    String[] dates = dateStr.split("~");
                    LocalDate start = LocalDate.parse(dates[0]);
                    LocalDate end = LocalDate.parse(dates[1]);
                    
                    // 累加范围内每一天的销量
                    int rangeVolume = 0;
                    LocalDate current = start;
                    while (!current.isAfter(end)) {
                        rangeVolume += dailyVolume.getOrDefault(current, 0);
                        current = current.plusDays(1);
                    }
                    
                    volumeMap.put(dateStr, rangeVolume);
                    calculatedTotal += rangeVolume;
                } else {
                    // 单日
                    LocalDate date = LocalDate.parse(dateStr);
                    int dayVolume = dailyVolume.getOrDefault(date, 0);
                    volumeMap.put(dateStr, dayVolume);
                    calculatedTotal += dayVolume;
                }
            }
            
            log.info("店铺[{}]的产品[{}]：计算总销量={}, SQL总销量={}", 
                    clientId, baseDto.getOfferId(), calculatedTotal, baseDto.getTotal());
            
            // 设置日期销量映射
            baseDto.setVolumeMap(volumeMap);
            
            // 使用SQL查询结果的total和计算均值
            // total值已经由getSkuOrderItem方法设置好，不需要重新计算
            int total = baseDto.getTotal() != null ? baseDto.getTotal() : 0;
            baseDto.setAvg(!volumeMap.isEmpty() ? total / volumeMap.size() : 0);
            
            volumeList.add(baseDto);
        }
        
        respVO.setVolumeList(volumeList);
        
        // 返回结果列表
        return Collections.singletonList(respVO);
    }

}
