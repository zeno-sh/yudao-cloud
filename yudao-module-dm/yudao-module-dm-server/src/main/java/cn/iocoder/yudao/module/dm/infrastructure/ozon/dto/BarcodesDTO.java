package cn.iocoder.yudao.module.dm.infrastructure.ozon.dto;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;

/**
 * @Author zeno
 * @Date 2024/3/3
 */
@Data
public class BarcodesDTO {

    @JSONField(name = "lower_barcode")
    private String lowerBarcode;

    @JSONField(name = "upper_barcode")
    private String upperBarcode;
}
