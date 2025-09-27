package cn.iocoder.yudao.module.dm.controller.admin.statistics.adapter;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.dm.controller.admin.purchase.order.vo.PurchaseOrderRespVO;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.google.common.collect.Lists;
import org.jeecg.modules.jmreport.desreport.render.handler.convert.ApiDataConvertAdapter;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * @author: Zeno
 * @createTime: 2024/08/12 22:12
 */
@Component("PurchaseOrderConvertAdapter")
public class PurchaseOrderConvertAdapter implements ApiDataConvertAdapter {

    @Override
    public String getData(JSONObject jsonObject) {
        TypeReference<CommonResult<PurchaseOrderRespVO>> typeReference = new TypeReference<CommonResult<PurchaseOrderRespVO>>() {
        };

        CommonResult<PurchaseOrderRespVO> apiResult = JSON.parseObject(JSON.toJSONString(jsonObject), typeReference);

        if (!CommonResult.isSuccess(apiResult.getCode())) {
            return "";
        }
        PurchaseOrderRespVO vo = apiResult.getData();
        JSONArray jsonArray = new JSONArray();
        jsonArray.add(vo);
        return jsonArray.toJSONString();
    }
}
