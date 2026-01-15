package cn.iocoder.yudao.module.chrome.framework.rpc.config;

import cn.iocoder.yudao.module.pay.api.order.PayOrderApi;
import cn.iocoder.yudao.module.system.api.logger.LoginLogApi;
import cn.iocoder.yudao.module.system.api.mail.MailSendApi;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration(value = "chromeRpcConfiguration", proxyBeanMethods = false)
@EnableFeignClients(clients = {
        PayOrderApi.class,
        MailSendApi.class,
        LoginLogApi.class
})
public class RpcConfiguration {


}
