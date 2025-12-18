package cn.iocoder.yudao.module.report.convert.jimu;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import org.jeecg.modules.jmreport.desreport.render.handler.convert.ApiDataConvertAdapter;
import org.springframework.stereotype.Component;

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
