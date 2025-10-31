
package cn.iocoder.yudao.module.dm.infrastructure.wb.dto;

import javax.annotation.Generated;
import com.google.gson.annotations.SerializedName;

@Generated("net.hexar.json2pojo")
@SuppressWarnings("unused")
public class Cursor {

    @SerializedName("limit")
    private Long mLimit;
    @SerializedName("nmID")
    private Long mNmID;
    @SerializedName("updatedAt")
    private String mUpdatedAt;

    public Long getLimit() {
        return mLimit;
    }

    public void setLimit(Long limit) {
        mLimit = limit;
    }

    public Long getNmID() {
        return mNmID;
    }

    public void setNmID(Long nmID) {
        mNmID = nmID;
    }

    public String getUpdatedAt() {
        return mUpdatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        mUpdatedAt = updatedAt;
    }

}
