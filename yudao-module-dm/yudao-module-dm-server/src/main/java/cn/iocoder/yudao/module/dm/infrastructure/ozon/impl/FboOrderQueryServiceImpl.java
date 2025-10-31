package cn.iocoder.yudao.module.dm.infrastructure.ozon.impl;

import cn.iocoder.yudao.module.dm.infrastructure.ozon.constant.OzonConfig;
import cn.iocoder.yudao.module.dm.infrastructure.ozon.dto.PostingDTO;
import cn.iocoder.yudao.module.dm.infrastructure.ozon.request.FboOrderQueryRequest;
import cn.iocoder.yudao.module.dm.infrastructure.ozon.response.OzonHttpResponse;
import cn.iocoder.yudao.module.dm.infrastructure.ozon.utils.OzonHttpUtil;
import cn.iocoder.yudao.module.dm.infrastructure.service.OrderQueryService;
import com.alibaba.fastjson2.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author zeno
 * @Date 2024/3/3
 */
@Service("FboOrderQueryService")
@Slf4j
public class FboOrderQueryServiceImpl implements OrderQueryService<OzonHttpResponse, FboOrderQueryRequest> {

    @Autowired
    private OzonHttpUtil ozonHttpUtil;

    @Override
    public OzonHttpResponse<List<PostingDTO>> queryOrder(FboOrderQueryRequest orderQueryRequest) {
        TypeReference<OzonHttpResponse<List<PostingDTO>>> typeReference = new TypeReference<OzonHttpResponse<List<PostingDTO>>>() {
        };

        return ozonHttpUtil.post(OzonConfig.OZON_FBO_POSTING_API, orderQueryRequest, typeReference);
    }
}
