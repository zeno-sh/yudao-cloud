package cn.iocoder.yudao.module.dm.infrastructure.ozon.constant;

/**
 * @Author zeno
 * @Date 2024/2/11
 */
public enum NotifyTypeEnums {
    TYPE_PING("TYPE_PING", "在初始连接时和连接后定期检查服务器可用性状态"),
    TYPE_NEW_POSTING("TYPE_NEW_POSTING", "新订单"),
    TYPE_POSTING_CANCELLED("TYPE_POSTING_CANCELLED", "发货取消"),
    TYPE_STATE_CHANGED("TYPE_STATE_CHANGED", "发货状态改变"),
    ;

    private String type;
    private String desc;

    NotifyTypeEnums(String type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    public String getType() {
        return type;
    }

    public String getDesc() {
        return desc;
    }

    public static NotifyTypeEnums fromType(String type) {
        for (NotifyTypeEnums enumType : values()) {
            if (enumType.getType().equals(type)) {
                return enumType;
            }
        }
        throw new IllegalArgumentException("Unknown type: " + type);
    }
}
