package cn.iocoder.yudao.module.dm.service.transport;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.dm.controller.admin.product.vo.ProductSimpleInfoVO;
import cn.iocoder.yudao.module.dm.controller.admin.statistics.vo.*;
import cn.iocoder.yudao.module.dm.dal.dataobject.product.SupplierPriceOfferDO;
import cn.iocoder.yudao.module.dm.dal.mysql.transport.TransportPlanMapper;
import cn.iocoder.yudao.module.dm.service.product.ProductInfoService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.util.CollectionUtils;

/**
 * 发货计划报表 Service 实现类
 */
@Service
@Slf4j
public class TransportReportServiceImpl implements TransportReportService {

    @Resource
    private TransportPlanMapper transportPlanMapper;
    @Resource
    private ProductInfoService productInfoService;

    @Override
    public PageResult<TransportReportVO> getTransportReportPage(TransportReportReqVO reqVO) {
        // 1. 解析月份，获取月初和月末
        LocalDate monthStart = LocalDate.parse(reqVO.getMonth() + "-01");
        LocalDate monthEnd = monthStart.with(TemporalAdjusters.lastDayOfMonth());
        String startDate = monthStart.toString();
        String endDate = monthEnd.toString();

        // 2. 分页查询
        IPage<TransportReportVO> page = new Page<>(reqVO.getPageNo(), reqVO.getPageSize());
        IPage<TransportReportVO> pageResult = transportPlanMapper.selectMonthlyTransportReport(page, startDate, endDate, reqVO.getProductId());
        List<TransportReportVO> list = pageResult.getRecords();
        if (CollectionUtils.isEmpty(list)) {
            return new PageResult<>(new ArrayList<>(), pageResult.getTotal());
        }

        // 3. 获取产品信息
        Set<Long> productIds = list.stream().map(TransportReportVO::getProductId).collect(Collectors.toSet());
        Map<Long, ProductSimpleInfoVO> productMap = productInfoService.batchQueryProductSimpleInfo(new ArrayList<>(productIds));
        // 查询产品的供应商报价信息
        Map<Long, SupplierPriceOfferDO> priceMap = productInfoService.getSupplierPriceOfferMapByProductIds(new ArrayList<>(productIds));

        // 4. 设置产品信息和月份
        list.forEach(vo -> {
            vo.setProductSimpleInfoVO(productMap.get(vo.getProductId()));
            vo.setReportDate(reqVO.getMonth()); // 设置正确的月份格式
            SupplierPriceOfferDO supplierPriceOfferDO = priceMap.get(vo.getProductId());
            if (null != supplierPriceOfferDO) {
                BigDecimal price = supplierPriceOfferDO.getPrice();
                BigDecimal taxPrice = supplierPriceOfferDO.getTaxPrice();

                vo.setTotalPrice(price.multiply(new BigDecimal(vo.getEndTransitQuantity())));
                vo.setTotalTaxPrice(taxPrice.multiply(new BigDecimal(vo.getEndTransitQuantity())));
            }
        });

        return new PageResult<>(list, pageResult.getTotal());
    }

    @Override
    public List<TransportReportExportVO> buildExportExcelData(List<TransportReportVO> monthlyList) {
        List<TransportReportExportVO> exportList = new ArrayList<>();
        for (TransportReportVO monthly : monthlyList) {
            TransportReportExportVO exportVO = BeanUtils.toBean(monthly, TransportReportExportVO.class);
            // 设置基础数据
            exportVO.setSkuId(monthly.getProductSimpleInfoVO().getSkuId());
            exportVO.setSkuName(monthly.getProductSimpleInfoVO().getSkuName());
            exportList.add(exportVO);
        }
        return exportList;
    }
} 