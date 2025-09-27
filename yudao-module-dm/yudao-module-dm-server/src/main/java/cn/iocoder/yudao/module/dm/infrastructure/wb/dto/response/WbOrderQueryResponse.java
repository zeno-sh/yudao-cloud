
package cn.iocoder.yudao.module.dm.infrastructure.wb.dto.response;

import java.util.List;
import javax.annotation.Generated;

import cn.iocoder.yudao.module.dm.infrastructure.wb.dto.Order;
import com.alibaba.fastjson2.annotation.JSONField;

@Generated("net.hexar.json2pojo")
@SuppressWarnings("unused")
public class WbOrderQueryResponse extends WbHttpBaseResponse{

    @JSONField(name = "next")
    private Long mNext;
    @JSONField(name = "orders")
    private List<Order> mOrders;

    public Long getNext() {
        return mNext;
    }

    public void setNext(Long next) {
        mNext = next;
    }

    public List<Order> getOrders() {
        return mOrders;
    }

    public void setOrders(List<Order> orders) {
        mOrders = orders;
    }

}
