package cn.iocoder.yudao.module.dm.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Ozon广告同步任务状态枚举
 *
 * @author Jax
 */
@Getter
@AllArgsConstructor
public enum OzonAdSyncTaskStatusEnum {

    /**
     * 待处理
     */
    PENDING(0, "待处理"),

    /**
     * 处理中
     */
    PROCESSING(10, "处理中"),

    /**
     * 已完成
     */
    COMPLETED(20, "已完成"),

    /**
     * 失败
     */
    FAILED(30, "失败"),

    /**
     * 已作废
     */
    OBSOLETE(40, "已作废");

    /**
     * 状态值
     */
    private final Integer status;

    /**
     * 状态名称
     */
    private final String name;

    /**
     * 获取状态描述
     */
    public String getDesc() {
        return this.name;
    }

    /**
     * 根据状态值获取枚举
     */
    public static OzonAdSyncTaskStatusEnum getByStatus(Integer status) {
        if (status == null) {
            return null;
        }
        for (OzonAdSyncTaskStatusEnum taskStatus : values()) {
            if (taskStatus.getStatus().equals(status)) {
                return taskStatus;
            }
        }
        return null;
    }

    /**
     * 判断是否为待处理状态
     */
    public static boolean isPending(Integer status) {
        return PENDING.getStatus().equals(status);
    }

    /**
     * 判断是否为处理中状态
     */
    public static boolean isProcessing(Integer status) {
        return PROCESSING.getStatus().equals(status);
    }

    /**
     * 判断是否为已完成状态
     */
    public static boolean isCompleted(Integer status) {
        return COMPLETED.getStatus().equals(status);
    }

    /**
     * 判断是否为失败状态
     */
    public static boolean isFailed(Integer status) {
        return FAILED.getStatus().equals(status);
    }

    /**
     * 判断是否为作废状态
     */
    public static boolean isObsolete(Integer status) {
        return OBSOLETE.getStatus().equals(status);
    }

    /**
     * 判断是否为终态（已完成、失败、作废）
     */
    public static boolean isFinalStatus(Integer status) {
        return isCompleted(status) || isFailed(status) || isObsolete(status);
    }

    /**
     * 判断是否可以重试（失败状态）
     */
    public static boolean canRetry(Integer status) {
        return isFailed(status);
    }

}