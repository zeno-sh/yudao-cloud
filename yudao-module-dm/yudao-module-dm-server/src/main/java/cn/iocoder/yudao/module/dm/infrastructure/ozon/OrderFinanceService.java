package cn.iocoder.yudao.module.dm.infrastructure.ozon;

import cn.iocoder.yudao.module.dm.infrastructure.ozon.constant.OzonConfig;
import cn.iocoder.yudao.module.dm.infrastructure.ozon.dto.OrderFinanceResultDTO;
import cn.iocoder.yudao.module.dm.infrastructure.ozon.request.OrderFinanceRequest;
import cn.iocoder.yudao.module.dm.infrastructure.ozon.response.OzonHttpResponse;
import cn.iocoder.yudao.module.dm.infrastructure.ozon.utils.OzonHttpUtil;
import cn.iocoder.yudao.module.dm.service.transaction.OzonFinanceTransactionService;
import com.alibaba.fastjson2.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @Author zeno
 * @Date 2024/1/27
 */
@Service
@Slf4j
public class OrderFinanceService {

    @Resource
    private OzonFinanceTransactionService ozonFinanceTransactionService;
    @Resource
    private OzonHttpUtil ozonHttpUtil;

    public OzonHttpResponse<OrderFinanceResultDTO> getOrderFinanceResult(OrderFinanceRequest request) {
        TypeReference<OzonHttpResponse<OrderFinanceResultDTO>> typeReference = new TypeReference<OzonHttpResponse<OrderFinanceResultDTO>>() {
        };
        return ozonHttpUtil.post(OzonConfig.OZON_FINANCE_API, request, typeReference);
    }
}
