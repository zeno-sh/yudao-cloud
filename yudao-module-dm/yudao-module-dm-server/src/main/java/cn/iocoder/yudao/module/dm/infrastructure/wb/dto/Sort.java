
package cn.iocoder.yudao.module.dm.infrastructure.wb.dto;

import javax.annotation.Generated;
import com.google.gson.annotations.SerializedName;

@Generated("net.hexar.json2pojo")
@SuppressWarnings("unused")
public class Sort {

    @SerializedName("ascending")
    private Boolean mAscending;

    public Boolean getAscending() {
        return mAscending;
    }

    public void setAscending(Boolean ascending) {
        mAscending = ascending;
    }

}
