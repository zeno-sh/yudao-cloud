package cn.iocoder.yudao.module.dm.framework.config;


import cn.iocoder.yudao.module.bpm.api.task.BpmProcessInstanceApi;
import cn.iocoder.yudao.module.fbs.api.WarehouseInventoryApi;
import cn.iocoder.yudao.module.fbs.api.WarehouseZoneApi;
import cn.iocoder.yudao.module.infra.api.config.ConfigApi;
import cn.iocoder.yudao.module.infra.api.file.FileApi;
import cn.iocoder.yudao.module.sellfox.api.inventory.FbaInventoryApi;
import cn.iocoder.yudao.module.sellfox.api.order.AmazonOrderService;
import cn.iocoder.yudao.module.system.api.country.CountryApi;
import cn.iocoder.yudao.module.system.api.currency.CurrencyApi;
import cn.iocoder.yudao.module.system.api.currency.ExchangeRateApi;
import cn.iocoder.yudao.module.system.api.dict.DictDataApi;
import cn.iocoder.yudao.module.system.api.notify.NotifyMessageSendApi;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration(value = "erpRpcConfiguration", proxyBeanMethods = false)
@EnableFeignClients(clients = {AdminUserApi.class, ConfigApi.class, FileApi.class, BpmProcessInstanceApi.class,
        NotifyMessageSendApi.class, DictDataApi.class, AmazonOrderService.class, FbaInventoryApi.class,
        WarehouseZoneApi.class, WarehouseInventoryApi.class, CurrencyApi.class, ExchangeRateApi.class, CountryApi.class})
public class RpcConfiguration {
}