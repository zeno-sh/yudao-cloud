package cn.iocoder.yudao.module.bpm.api.task.dto;

import lombok.Data;

/**
 * @Author: Jax
 * @Date: Created in 16:46 2025/9/25
 */
@Data
public class UserDTO {

    /**
     * 用户编号
     */
    private Long id;

    /**
     * 用户昵称
     */
    private String nickname;

    /**
     * 部门编号
     */
    private Long deptId;

    /**
     * 部门名称
     */
    private String deptName;
}
