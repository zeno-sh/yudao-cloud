package cn.iocoder.yudao.module.dm.infrastructure.ozon.dto;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;

/**
 * @Author zeno
 * @Date 2024/2/12
 */
@Data
public class ChatDTO {
    @JSONField(name = "chat_id")
    private String chatId;

}
