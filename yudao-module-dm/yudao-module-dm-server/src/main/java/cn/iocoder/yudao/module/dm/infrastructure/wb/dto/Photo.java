
package cn.iocoder.yudao.module.dm.infrastructure.wb.dto;

import javax.annotation.Generated;

import com.alibaba.fastjson2.annotation.JSONField;
import com.google.gson.annotations.SerializedName;

@Generated("net.hexar.json2pojo")
@SuppressWarnings("unused")
public class Photo {

    @JSONField(name ="big")
    private String mBig;
    @JSONField(name ="c246x328")
    private String mC246x328;
    @JSONField(name ="c516x688")
    private String mC516x688;
    @JSONField(name ="square")
    private String mSquare;
    @JSONField(name ="tm")
    private String mTm;

    public String getBig() {
        return mBig;
    }

    public void setBig(String big) {
        mBig = big;
    }

    public String getC246x328() {
        return mC246x328;
    }

    public void setC246x328(String c246x328) {
        mC246x328 = c246x328;
    }

    public String getC516x688() {
        return mC516x688;
    }

    public void setC516x688(String c516x688) {
        mC516x688 = c516x688;
    }

    public String getSquare() {
        return mSquare;
    }

    public void setSquare(String square) {
        mSquare = square;
    }

    public String getTm() {
        return mTm;
    }

    public void setTm(String tm) {
        mTm = tm;
    }

}
