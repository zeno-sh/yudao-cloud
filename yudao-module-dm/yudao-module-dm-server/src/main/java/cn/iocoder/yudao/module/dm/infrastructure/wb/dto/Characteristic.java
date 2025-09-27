
package cn.iocoder.yudao.module.dm.infrastructure.wb.dto;

import java.util.List;
import javax.annotation.Generated;

import com.alibaba.fastjson2.annotation.JSONField;
import com.google.gson.annotations.SerializedName;

@Generated("net.hexar.json2pojo")
@SuppressWarnings("unused")
public class Characteristic {

    @JSONField(name ="id")
    private Long mId;
    @JSONField(name ="name")
    private String mName;
    @JSONField(name ="value")
    private List<String> mValue;

    public Long getId() {
        return mId;
    }

    public void setId(Long id) {
        mId = id;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public List<String> getValue() {
        return mValue;
    }

    public void setValue(List<String> value) {
        mValue = value;
    }

}
