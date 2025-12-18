package cn.iocoder.yudao.module.report.convert.jimu;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import org.jeecg.modules.jmreport.desreport.render.handler.convert.ApiDataConvertAdapter;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author: Zeno
 * @createTime: 2024/08/13 13:20
 */
@Component("TransportItemConvertAdapter")
public class TransportItemConvertAdapter implements ApiDataConvertAdapter {

    @Override
    public String getData(JSONObject jsonObject) {
        TypeReference<CommonResult<List<TransportPlanItemRespVO>>> typeReference = new TypeReference<CommonResult<List<TransportPlanItemRespVO>>>() {
        };

        CommonResult<List<TransportPlanItemRespVO>> apiResult = JSON.parseObject(JSON.toJSONString(jsonObject), typeReference);

        if (!CommonResult.isSuccess(apiResult.getCode())) {
            return "";
        }
        List<TransportPlanItemRespVO> itemRespVOList = apiResult.getData();
        JSONArray jsonArray = new JSONArray();
        jsonArray.addAll(itemRespVOList);
        return jsonArray.toJSONString();
    }
}