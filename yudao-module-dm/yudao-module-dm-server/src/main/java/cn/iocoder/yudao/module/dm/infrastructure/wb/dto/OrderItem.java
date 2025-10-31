
package cn.iocoder.yudao.module.dm.infrastructure.wb.dto;

import javax.annotation.Generated;
import com.google.gson.annotations.SerializedName;

@Generated("net.hexar.json2pojo")
@SuppressWarnings("unused")
public class OrderItem {

    @SerializedName("id")
    private Long mId;
    @SerializedName("supplierStatus")
    private String mSupplierStatus;
    @SerializedName("wbStatus")
    private String mWbStatus;

    public Long getId() {
        return mId;
    }

    public void setId(Long id) {
        mId = id;
    }

    public String getSupplierStatus() {
        return mSupplierStatus;
    }

    public void setSupplierStatus(String supplierStatus) {
        mSupplierStatus = supplierStatus;
    }

    public String getWbStatus() {
        return mWbStatus;
    }

    public void setWbStatus(String wbStatus) {
        mWbStatus = wbStatus;
    }

}
