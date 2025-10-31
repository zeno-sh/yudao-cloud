package cn.iocoder.yudao.module.dm.infrastructure.ozon.impl;

import cn.iocoder.yudao.module.dm.infrastructure.ozon.constant.OzonConfig;
import cn.iocoder.yudao.module.dm.infrastructure.ozon.dto.OrderResultDTO;
import cn.iocoder.yudao.module.dm.infrastructure.ozon.request.FbsOrderQueryRequest;
import cn.iocoder.yudao.module.dm.infrastructure.ozon.response.OzonHttpResponse;
import cn.iocoder.yudao.module.dm.infrastructure.ozon.utils.OzonHttpUtil;
import cn.iocoder.yudao.module.dm.infrastructure.service.OrderQueryService;
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
public class FbsOrderQueryServiceImpl implements OrderQueryService<OzonHttpResponse, FbsOrderQueryRequest> {

    @Resource
    private OzonHttpUtil ozonHttpUtil;
    @Override
    public OzonHttpResponse<OrderResultDTO> queryOrder(FbsOrderQueryRequest orderQueryRequest) {
        TypeReference<OzonHttpResponse<OrderResultDTO>> typeReference = new TypeReference<OzonHttpResponse<OrderResultDTO>>() {
        };

        return ozonHttpUtil.post(OzonConfig.OZON_POSTING_API, orderQueryRequest, typeReference);
    }
}
