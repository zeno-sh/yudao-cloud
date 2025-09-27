package cn.iocoder.yudao.module.dm.controller.admin.notify;

import cn.hutool.http.HttpStatus;
import cn.iocoder.yudao.module.dm.infrastructure.ozon.dto.OzonMessageErrorDTO;
import cn.iocoder.yudao.module.dm.infrastructure.ozon.dto.OzonMessageErrorDetailDTO;
import cn.iocoder.yudao.module.dm.infrastructure.ozon.mq.consumer.order.NotificationProcessor;
import cn.iocoder.yudao.module.dm.infrastructure.ozon.request.NotifyRequest;
import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.annotation.security.PermitAll;

/**
 * @author: Zeno
 * @createTime: 2024/07/05 17:16
 */
@RestController
@RequestMapping("/dm/ozon")
@Validated
@Slf4j
public class MessageNotifyController {

    @Resource
    private NotificationProcessor notificationProcessor;

    @RequestMapping(value = "/notify", method = {RequestMethod.POST, RequestMethod.GET})
    @PermitAll
    public ResponseEntity<String> processRequest(@RequestBody NotifyRequest request) {
        try {
            log.info("ozon消息通知处理开始，请求参数：{}", JSON.toJSONString(request));
            String result = notificationProcessor.processNotification(request);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("ozon消息通知处理异常", e);
        }
        OzonMessageErrorDetailDTO errorDetail = new OzonMessageErrorDetailDTO();
        errorDetail.setCode("SERVICE_ERROR");
        errorDetail.setMessage("Server processing exception");
        errorDetail.setDetail(null);

        OzonMessageErrorDTO ozonMessageError = new OzonMessageErrorDTO();
        ozonMessageError.setError(errorDetail);
        return ResponseEntity.status(HttpStatus.HTTP_INTERNAL_ERROR).body(JSON.toJSONString(ozonMessageError));
    }
}
