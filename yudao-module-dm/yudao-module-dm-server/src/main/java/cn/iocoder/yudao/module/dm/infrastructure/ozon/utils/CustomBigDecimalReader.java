package cn.iocoder.yudao.module.dm.infrastructure.ozon.utils;


import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.reader.ObjectReader;

import java.lang.reflect.Type;
import java.math.BigDecimal;

/**
 * @Author zeno
 * @Date 2024/2/5
 */
public class CustomBigDecimalReader implements ObjectReader<BigDecimal> {

    @Override
    public BigDecimal readObject(JSONReader jsonReader, Type type, Object o, long l) {
        String value = jsonReader.readString();
        if (value != null && value.contains(",")) {
            value = value.replace(',', '.');
        }
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException e) {
            throw new JSONException("解析数字失败: " + value, e);
        }
    }
}
