
package cn.iocoder.yudao.module.dm.infrastructure.wb.dto.request;

import javax.annotation.Generated;

import cn.iocoder.yudao.module.dm.infrastructure.wb.dto.Settings;
import com.google.gson.annotations.SerializedName;

@Generated("net.hexar.json2pojo")
@SuppressWarnings("unused")
public class WbProductOnlineRequest extends WbHttpBaseRequest {

    @SerializedName("settings")
    private Settings mSettings;

    public Settings getSettings() {
        return mSettings;
    }

    public void setSettings(Settings settings) {
        mSettings = settings;
    }

}
