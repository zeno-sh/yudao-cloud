
package cn.iocoder.yudao.module.dm.infrastructure.wb.dto;

import javax.annotation.Generated;
import com.google.gson.annotations.SerializedName;

@Generated("net.hexar.json2pojo")
@SuppressWarnings("unused")
public class User {

    @SerializedName("fio")
    private String mFio;
    @SerializedName("phone")
    private Long mPhone;

    public String getFio() {
        return mFio;
    }

    public void setFio(String fio) {
        mFio = fio;
    }

    public Long getPhone() {
        return mPhone;
    }

    public void setPhone(Long phone) {
        mPhone = phone;
    }

}
