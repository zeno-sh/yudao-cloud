
package cn.iocoder.yudao.module.dm.infrastructure.wb.dto.response;

import java.util.List;
import javax.annotation.Generated;

import cn.iocoder.yudao.module.dm.infrastructure.wb.dto.OrderItem;
import com.google.gson.annotations.SerializedName;

@Generated("net.hexar.json2pojo")
@SuppressWarnings("unused")
public class WbOrderStatusResponse extends WbHttpBaseResponse{

    @SerializedName("orders")
    private List<OrderItem> mOrders;

    public List<OrderItem> getOrders() {
        return mOrders;
    }

    public void setOrders(List<OrderItem> orders) {
        mOrders = orders;
    }

}
