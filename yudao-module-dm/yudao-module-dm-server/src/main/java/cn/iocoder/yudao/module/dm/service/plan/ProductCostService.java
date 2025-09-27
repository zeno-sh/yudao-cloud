package cn.iocoder.yudao.module.dm.service.plan;

import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.dm.controller.admin.plan.vo.*;
import cn.iocoder.yudao.module.dm.dal.dataobject.commission.CategoryCommissionDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.plan.ProductSelectionPlanDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.product.*;
import cn.iocoder.yudao.module.dm.dal.mysql.product.ProductPlatformTrendMapper;
import cn.iocoder.yudao.module.dm.dal.mysql.product.ProductPriceMapper;
import cn.iocoder.yudao.module.dm.dal.mysql.product.ProductPurchaseMapper;
import cn.iocoder.yudao.module.dm.dal.mysql.product.SupplierPriceOfferMapper;
import cn.iocoder.yudao.module.dm.service.commission.CategoryCommissionService;
import cn.iocoder.yudao.module.dm.service.product.ProductInfoService;
import cn.iocoder.yudao.module.infra.api.config.ConfigApi;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author: Zeno
 * @createTime: 2024/04/19 19:16
 */
@Component
public class ProductCostService {

    @Resource
    private ProductInfoService dmProductInfoService;
    @Resource
    private ProductPurchaseMapper productPurchaseMapper;
    @Resource
    private CategoryCommissionService dmCategoryCommissionService;
    @Resource
    private ProductPlatformTrendMapper productPlatformTrendMapper;
    @Resource
    private ProductPriceMapper productPriceMapper;
    @Resource
    private SupplierPriceOfferMapper supplierPriceOfferMapper;


    //卢布汇率
    private final BigDecimal RUB_EXCHANGE_RATE = new BigDecimal("0.078");
    //千克
    private final BigDecimal KILOGRAM = new BigDecimal("1000");
    //海外仓上架费用 0.8RMB/M3
    private final BigDecimal FBS_SHELF = new BigDecimal("0.8");
    //海外仓散货卸货费用 15RMB/KG
    private final BigDecimal FBS_UNLOAD = new BigDecimal("15");
    //广告费率
    private final BigDecimal AD_RATE = new BigDecimal("0.065");
    //货损
    private final BigDecimal LOSS_RATE = new BigDecimal("0.02");

    public List<ProductPlanVO> createPlanVO(List<ProductSelectionPlanDO> productSelectionPlanList) {
        List<ProductPlanVO> productPlanVOList = new ArrayList<>();
        List<Long> productIds = productSelectionPlanList.stream().map(ProductSelectionPlanDO::getProductId).collect(Collectors.toList());
        List<Long> noExistProductIds = new ArrayList<>();
        for (Long productId : productIds) {
            ProductPlanVO planVO = null;
            //TODO：查询缓存
            if (Objects.nonNull(planVO)) {
                productPlanVOList.add(planVO);
            } else {
                noExistProductIds.add(productId);
            }
        }

        if (CollectionUtils.isEmpty(noExistProductIds)) {
            return productPlanVOList;
        }

        List<ProductInfoDO> dmProductInfos = dmProductInfoService.batchQueryProductInfoList(noExistProductIds);
        List<ProductPurchaseDO> dmProductPurchases = productPurchaseMapper.batchQueryByProductIds(productIds);
        List<Long> categoryCommissionIds = dmProductInfos.stream().map(ProductInfoDO::getCategoryCommissionId).collect(Collectors.toList());
        List<CategoryCommissionDO> dmCategoryCommissions = dmCategoryCommissionService.batchQueryByIds(categoryCommissionIds);

        Map<Long, ProductInfoDO> productInfoMap = dmProductInfos.stream().collect(Collectors.toMap(ProductInfoDO::getId, Function.identity(), (k1, k2) -> k1));
        Map<Long, ProductPurchaseDO> productPurchaseMap = dmProductPurchases.stream().collect(Collectors.toMap(ProductPurchaseDO::getProductId, Function.identity(), (k1, k2) -> {
            if (k1.getFirstChoice().equals("Y")) {
                return k1;
            }
            return k2;
        }));
        Map<Long, CategoryCommissionDO> commissionMap = dmCategoryCommissions.stream().collect(Collectors.toMap(CategoryCommissionDO::getId, Function.identity(), (k1, k2) -> k1));
        List<ProductPlatformTrendDO> dmProductPlatformTrends = productPlatformTrendMapper.batchQueryByProductIds(productIds);
        Map<Long, List<ProductPlatformTrendDO>> trendMap = dmProductPlatformTrends.stream().collect(Collectors.groupingBy(ProductPlatformTrendDO::getProductId));

        for (ProductSelectionPlanDO plan : productSelectionPlanList) {
            ProductInfoDO dmProductInfo = productInfoMap.get(plan.getProductId());

            if (dmProductInfo == null) {
                continue;
            }

            ProductPlanVO vo = new ProductPlanVO();
            vo.setId(plan.getId());
            vo.setProductId(plan.getProductId());
            vo.setPlanName(plan.getPlanName());
            vo.setCreateTime(plan.getCreateTime());

            CategoryCommissionDO dmCategoryCommission = commissionMap.get(dmProductInfo.getCategoryCommissionId());
            ProductInfoVO productInfoVO = buildProductInfoVO(plan, productInfoMap, productPurchaseMap);
            vo.setProductInfo(productInfoVO);

            ProductPriceDO dmProductPrice = productPriceMapper.selectById(plan.getPriceId());
            if (dmProductPrice == null) {
                dmProductPrice = new ProductPriceDO();
                vo.setProductPrice(BeanUtils.toBean(dmProductPrice, ProductPriceVO.class));
                productPlanVOList.add(vo);
                continue;
            }
            vo.setProductPrice(BeanUtils.toBean(dmProductPrice, ProductPriceVO.class));
            ProductCostVO productCostVO = buildProductCostVO(plan, dmProductPrice.getSellingPrice(), productInfoVO, dmCategoryCommission);

            if (Objects.nonNull(plan.getForecastPurchasePrice()) && plan.getForecastPurchasePrice().compareTo(BigDecimal.ZERO) > 0) {
                productCostVO.setPurchasePrice(plan.getForecastPurchasePrice().toString());
            } else if (Objects.nonNull(plan.getSupplierPriceOfferId())) {
                SupplierPriceOfferDO offer = supplierPriceOfferMapper.selectById(plan.getSupplierPriceOfferId());
                productCostVO.setPurchasePrice(offer == null ? BigDecimal.ZERO.toString() : offer.getPrice().toString());
            } else {
                productCostVO.setPurchasePrice(dmProductInfo.getCostPrice() == null ? BigDecimal.ZERO.toString() : dmProductInfo.getCostPrice().toString());
            }


            vo.setProductCost(productCostVO);

            BigDecimal categoryCommission = dmCategoryCommission.getRate();
            //结算价=前端定价-(前端定价*类目佣金率)-ozon物流-最后一公里
            BigDecimal settlePrice = dmProductPrice.getSellingPrice().subtract(
                            dmProductPrice.getSellingPrice().multiply(categoryCommission.divide(new BigDecimal("100")))
                    ).multiply(RUB_EXCHANGE_RATE).setScale(2, RoundingMode.HALF_UP)
                    .subtract(new BigDecimal(productCostVO.getOzonDeliveryPrice()))
                    .subtract(new BigDecimal(productCostVO.getLastMilePrice()));

            vo.setSettlementPrice(settlePrice.toString());

            BigDecimal adRate = plan.getAdRate() == null ? AD_RATE : plan.getAdRate().divide(new BigDecimal("100"));
            BigDecimal lossRate = plan.getLossRate() == null ? LOSS_RATE : plan.getLossRate().divide(new BigDecimal("100"));
            //利润=结算价-采购成本-头程成本-海外仓成本（卸货+上架+订单操作+送货）-店铺税费-银行手续费-广告-货损
            vo.setProfitPrice(settlePrice
                    .subtract(new BigDecimal(productCostVO.getPurchasePrice()))
                    .subtract(new BigDecimal(productCostVO.getFirstLegPrice()))
                    .subtract(new BigDecimal(productCostVO.getFbsTotalPrice()))
                    // todo: 配置
                    .subtract(settlePrice.multiply(new BigDecimal("0.07")))
                    .subtract(settlePrice.multiply(new BigDecimal("0.01")))
                    .subtract(dmProductPrice.getSellingPrice().multiply(adRate).multiply(RUB_EXCHANGE_RATE).setScale(2, RoundingMode.HALF_UP))
                    .subtract(dmProductPrice.getSellingPrice().multiply(lossRate).multiply(RUB_EXCHANGE_RATE).setScale(2, RoundingMode.HALF_UP))
                    .setScale(2, RoundingMode.HALF_UP)
                    .toString());
            //毛利率=利润/销售价
//            vo.setGrossProfitRate(new BigDecimal(vo.getProfitPrice()).divide(settlePrice, 5, RoundingMode.HALF_UP).multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP) + "%");
            if (Objects.nonNull(dmProductPrice.getSellingPrice()) || dmProductPrice.getSellingPrice().compareTo(BigDecimal.ZERO) > 0) {
                vo.setGrossProfitRate(new BigDecimal(vo.getProfitPrice())
                        .divide(RUB_EXCHANGE_RATE, 5, RoundingMode.HALF_UP)
                        .divide(dmProductPrice.getSellingPrice(), 5, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP) + "%");
            }

            //ROI=利润/（采购价+头程物流费用）
            vo.setRoiRate(new BigDecimal(vo.getProfitPrice())
                    .divide(new BigDecimal(productCostVO.getPurchasePrice()).add(new BigDecimal(productCostVO.getFirstLegPrice())), 5, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"))
                    .setScale(2, RoundingMode.HALF_UP) + "%"
            );

            List<ProductPlatformTrendDO> productPlatformTrendDOS = trendMap.get(plan.getPlanSkuId());
            if (CollectionUtils.isNotEmpty(productPlatformTrendDOS)) {
                vo.setProductPlatformTrendList(BeanUtils.toBean(productPlatformTrendDOS, ProductPlatformTrendVO.class));
            }
            productPlanVOList.add(vo);
        }

        return productPlanVOList;
    }

    private ProductInfoVO buildProductInfoVO(ProductSelectionPlanDO dmProductSelectionPlan, Map<Long, ProductInfoDO> productInfoMap,
                                             Map<Long, ProductPurchaseDO> productPurchaseMap) {

        Long productId = dmProductSelectionPlan.getProductId();
        ProductInfoDO dmProductInfo = productInfoMap.get(productId);
        ProductPurchaseDO dmProductPurchase = productPurchaseMap.get(productId);

        ProductInfoVO productInfoVO = new ProductInfoVO();
        productInfoVO.setSkuName(dmProductInfo.getSkuName());
        productInfoVO.setSkuId(dmProductInfo.getSkuId());
        productInfoVO.setPictureUrl(dmProductInfo.getPictureUrl());

        if (Objects.nonNull(dmProductPurchase)) {
            //cm
            BigDecimal length_CM = dmProductPurchase.getLength();
            BigDecimal width_CM = dmProductPurchase.getWidth();
            BigDecimal height_CM = dmProductPurchase.getHeight();

            //m 米
            BigDecimal length_M = length_CM.divide(new BigDecimal("100"));
            BigDecimal width_M = width_CM.divide(new BigDecimal("100"));
            BigDecimal height_M = height_CM.divide(new BigDecimal("100"));

            //mm 毫米
            BigDecimal length_MM = length_CM.multiply(new BigDecimal("10"));
            BigDecimal width_MM = width_CM.multiply(new BigDecimal("10"));
            BigDecimal height_MM = height_CM.multiply(new BigDecimal("10"));

            //体积
            BigDecimal volume = length_M.multiply(width_M).multiply(height_M).setScale(3, RoundingMode.HALF_UP);
            //体积重
            BigDecimal volumeWeight = length_CM.multiply(width_CM).multiply(height_CM).divide(new BigDecimal(6000), 5, RoundingMode.HALF_UP).setScale(2, RoundingMode.HALF_UP);
            BigDecimal volumeRise = length_MM.multiply(width_MM).multiply(height_MM).divide(new BigDecimal("1000000")).setScale(2, RoundingMode.HALF_UP);

            productInfoVO.setVolume(volume.toString());
            productInfoVO.setVolumeWeight(volumeWeight.toString());

            //箱规体积
            BigDecimal boxLength_CM = dmProductPurchase.getBoxLength().divide(new BigDecimal(100));
            BigDecimal boxHeight_CM = dmProductPurchase.getBoxHeight().divide(new BigDecimal(100));
            BigDecimal boxWidth_CM = dmProductPurchase.getBoxWidth().divide(new BigDecimal(100));
            BigDecimal boxWeight_kg = dmProductPurchase.getBoxWeight().divide(new BigDecimal(1000));

            BigDecimal boxVolume = boxHeight_CM.multiply(boxLength_CM).multiply(boxWidth_CM).setScale(3, RoundingMode.HALF_UP);
            if (volume.compareTo(BigDecimal.ZERO) != 0) {
                productInfoVO.setDensity(dmProductPurchase.getGrossWeight().divide(KILOGRAM).divide(volume, 5, RoundingMode.HALF_UP).toString());
            }
            productInfoVO.setVolumeRise(volumeRise.toString());
            productInfoVO.setDensity(boxWeight_kg.divide(boxVolume, RoundingMode.CEILING).setScale(0, RoundingMode.HALF_UP).toString());
            productInfoVO.setProductPurchase(BeanUtils.toBean(dmProductPurchase, ProductPurchaseVO.class));
        } else {
            ProductPurchaseDO emptyOjb = new ProductPurchaseDO();
            emptyOjb.setProductId(productId);
            emptyOjb.setLength(new BigDecimal("0"));
            emptyOjb.setWidth(new BigDecimal("0"));
            emptyOjb.setHeight(new BigDecimal("0"));
            emptyOjb.setNetWeight(new BigDecimal("0"));
            emptyOjb.setMaterial("");
            emptyOjb.setBoxLength(new BigDecimal("0"));
            emptyOjb.setBoxWidth(new BigDecimal("0"));
            emptyOjb.setBoxHeight(new BigDecimal("0"));
            emptyOjb.setQuantityPerBox(0);
            emptyOjb.setBoxWeight(new BigDecimal("0"));
            emptyOjb.setGrossWeight(new BigDecimal("0"));
            productInfoVO.setVolume("0");
            productInfoVO.setVolumeWeight("0");
            productInfoVO.setVolumeRise("0");
            productInfoVO.setDensity("0");
            productInfoVO.setProductPurchase(BeanUtils.toBean(emptyOjb, ProductPurchaseVO.class));
        }

        return productInfoVO;
    }

    private ProductCostVO buildProductCostVO(ProductSelectionPlanDO plan, BigDecimal sellingPrice, ProductInfoVO productInfoVO, CategoryCommissionDO dmCategoryCommission) {
        ProductCostVO productCostVO = new ProductCostVO();
        productCostVO.setFbsUnloadPrice(new BigDecimal(productInfoVO.getVolume()).multiply(FBS_UNLOAD).setScale(2, RoundingMode.HALF_UP).toString());
        productCostVO.setFbsShelfPrice(productInfoVO.getProductPurchase().getGrossWeight().divide(KILOGRAM).multiply(FBS_SHELF).setScale(2, RoundingMode.HALF_UP).toString());
        productCostVO.setFbsOrderPrice(getPriceByWeight(productInfoVO.getProductPurchase().getGrossWeight().divide(KILOGRAM)).toString());
        productCostVO.setFbsDeliveryPrice("5");
        productCostVO.setFbsTotalPrice(new BigDecimal(productCostVO.getFbsUnloadPrice())
                .add(new BigDecimal(productCostVO.getFbsShelfPrice()))
                .add(new BigDecimal(productCostVO.getFbsOrderPrice()))
                .add(new BigDecimal(productCostVO.getFbsDeliveryPrice()))
                .toString());

        BigDecimal forwarderPrice = plan.getForwarderPrice();
        if (Objects.nonNull(forwarderPrice)) {
            productCostVO.setFirstLegPrice(productInfoVO.getProductPurchase().getGrossWeight().divide(KILOGRAM).multiply(forwarderPrice).setScale(2, RoundingMode.HALF_UP).toString());
        } else {
            productCostVO.setFirstLegPrice("0");
        }

        productCostVO.setOzonDeliveryPrice(calculatePrice(new BigDecimal(productInfoVO.getVolumeRise())).multiply(RUB_EXCHANGE_RATE).setScale(2, RoundingMode.HALF_UP).toString());
        //最后一公里：前端售价*5.5%，最大不会超过500卢布

        //todo: 最后一公里费率配置
        BigDecimal lastMilePriceRUB = sellingPrice.multiply(new BigDecimal("5.5").divide(new BigDecimal(100)));
        if (lastMilePriceRUB.compareTo(new BigDecimal("500")) > 0) {
            lastMilePriceRUB = new BigDecimal("500");
        }

        productCostVO.setLastMilePrice(lastMilePriceRUB.multiply(RUB_EXCHANGE_RATE).setScale(2, RoundingMode.HALF_UP).toString());

        BigDecimal adRate = plan.getAdRate() == null ? AD_RATE : plan.getAdRate().divide(new BigDecimal("100"));
        BigDecimal lossRate = plan.getLossRate() == null ? LOSS_RATE : plan.getLossRate().divide(new BigDecimal("100"));

        productCostVO.setAdPrice(sellingPrice.multiply(adRate).multiply(RUB_EXCHANGE_RATE).setScale(2, RoundingMode.HALF_UP).toString());
        productCostVO.setLossPrice(sellingPrice.multiply(lossRate).multiply(RUB_EXCHANGE_RATE).setScale(2, RoundingMode.HALF_UP).toString());

        BigDecimal commission = sellingPrice.multiply(dmCategoryCommission.getRate().divide(new BigDecimal("100"))).multiply(RUB_EXCHANGE_RATE).setScale(2, RoundingMode.HALF_UP);
        productCostVO.setCategoryCommission(commission + "");

        //todo: 替换成配置
        productCostVO.setStoreRate("7" + "%");
        productCostVO.setBankRate("1" + "%");
        productCostVO.setLastMileRate("5.5" + "%");
        productCostVO.setCategoryRate(dmCategoryCommission.getRate() + "%");

        productCostVO.setAdRate(adRate.multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP) + "%");
        productCostVO.setLossRate(lossRate.multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP) + "%");


        return productCostVO;
    }

    private BigDecimal getPriceByWeight(BigDecimal weight) {
        // 将价格定义为常量，方便管理和修改
        final BigDecimal price0to1 = new BigDecimal("5.00");
        final BigDecimal price1to2 = new BigDecimal("6.00");
        final BigDecimal price2to5 = new BigDecimal("8.00");
        final BigDecimal price5to10 = new BigDecimal("10.00");
        final BigDecimal price10plus = new BigDecimal("15.00");

        // 设置四舍五入模式为HALF_UP，并保留两位小数
        weight = weight.setScale(2, RoundingMode.HALF_UP);

        if (weight.compareTo(BigDecimal.ZERO) >= 0 && weight.compareTo(new BigDecimal("1.00")) < 0) {
            return price0to1;
        } else if (weight.compareTo(new BigDecimal("1.00")) >= 0 && weight.compareTo(new BigDecimal("2.00")) < 0) {
            return price1to2;
        } else if (weight.compareTo(new BigDecimal("2.00")) >= 0 && weight.compareTo(new BigDecimal("5.00")) < 0) {
            return price2to5;
        } else if (weight.compareTo(new BigDecimal("5.00")) >= 0 && weight.compareTo(new BigDecimal("10.00")) < 0) {
            return price5to10;
        } else if (weight.compareTo(new BigDecimal("10.00")) >= 0) {
            return price10plus;
        }

        // 如果输入的重量不在任何已知区间内，返回null或者抛出异常
        return null;
    }

    private BigDecimal calculatePrice(BigDecimal volume) {
        // Define price
        BigDecimal price;

        // Define volume limits and price constants
        BigDecimal lowerLimit = new BigDecimal("0.1");
        BigDecimal upperLimit = new BigDecimal("5");
        BigDecimal secondLimit = new BigDecimal("175");
        BigDecimal basePrice = new BigDecimal("76");
        BigDecimal additionalPricePerLiter = new BigDecimal("9");
        BigDecimal highVolumePrice = new BigDecimal("1615");

        // Compare volume limits
        if (volume.compareTo(lowerLimit) >= 0 && volume.compareTo(upperLimit) <= 0) {
            // Volume is between 0.1 and 5 liters (inclusive), fixed price of 76 rubles
            price = basePrice;
        } else if (volume.compareTo(upperLimit) > 0 && volume.compareTo(secondLimit) <= 0) {
            // Volume is between 5.1 and 175 liters (inclusive), price is base plus an additional 9 rubles per liter over 5
            // Use setScale(0, RoundingMode.CEILING) for rounding up
            BigDecimal extraVolume = volume.subtract(upperLimit);
            BigDecimal extraLitersRoundedUp = extraVolume.setScale(0, RoundingMode.CEILING);
            BigDecimal extraCost = extraLitersRoundedUp.multiply(additionalPricePerLiter);
            price = basePrice.add(extraCost);
        } else if (volume.compareTo(secondLimit) > 0) {
            // If volume exceeds 175 liters, fixed price of 1615 rubles
            price = highVolumePrice;
        } else {
            // If volume does not meet any conditions
            price = BigDecimal.ZERO;
        }
        return price;
    }
}
