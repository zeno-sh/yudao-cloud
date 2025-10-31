
package cn.iocoder.yudao.module.dm.infrastructure.wb.dto.response;

import java.util.List;
import javax.annotation.Generated;

import cn.iocoder.yudao.module.dm.infrastructure.wb.dto.Card;
import cn.iocoder.yudao.module.dm.infrastructure.wb.dto.Cursor;
import com.alibaba.fastjson2.annotation.JSONField;

@Generated("net.hexar.json2pojo")
@SuppressWarnings("unused")
public class WbProductResponse extends WbHttpBaseResponse{

    @JSONField(name ="cards")
    private List<Card> mCards;
    @JSONField(name ="cursor")
    private Cursor mCursor;

    public List<Card> getCards() {
        return mCards;
    }

    public void setCards(List<Card> cards) {
        mCards = cards;
    }

    public Cursor getCursor() {
        return mCursor;
    }

    public void setCursor(Cursor cursor) {
        mCursor = cursor;
    }

}
