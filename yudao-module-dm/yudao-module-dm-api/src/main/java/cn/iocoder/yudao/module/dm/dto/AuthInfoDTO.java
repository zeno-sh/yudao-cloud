package cn.iocoder.yudao.module.dm.dto;

import lombok.Data;

/**
 * @author: Zeno
 * @createTime: 2024/08/27 10:39
 */
@Data
public class AuthInfoDTO {

    /**
     * token
     */
    private String appToken;
    /**
     * appKey
     */
    private String appKey;
}
