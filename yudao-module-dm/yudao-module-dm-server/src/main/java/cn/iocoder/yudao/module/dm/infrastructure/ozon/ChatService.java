//package cn.iocoder.yudao.module.dm.infrastructure.ozon;
//
//import cn.iocoder.yudao.module.dm.dal.dataobject.ozonshopmapping.OzonShopMappingDO;
//import cn.iocoder.yudao.module.dm.infrastructure.ozon.request.ChatCreateRequest;
//import cn.iocoder.yudao.module.dm.infrastructure.ozon.utils.OzonHttpUtil;
//import cn.iocoder.yudao.module.dm.service.ozonshopmapping.OzonShopMappingService;
//import com.alibaba.fastjson2.TypeReference;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.commons.collections4.CollectionUtils;
//import org.apache.commons.lang3.StringUtils;
//import org.springframework.stereotype.Service;
//
//import javax.annotation.Resource;
//import java.util.List;
//import java.util.Objects;
//
///**
// * @Author zeno
// * @Date 2024/2/12
// */
//@Service
//@Slf4j
//public class ChatService {
//
//    @Resource
//    private OzonShopMappingService dmShopMappingService;
////    @Autowired
////    private IDmBuyerChatService dmBuyerChatService;
//    @Resource
//    private OzonHttpUtil httpUtil;
//
//    public void sendMessage2NewOrder(String clientId, String postingNumber) {
//        OzonShopMappingDO query = new OzonShopMappingDO();
//        query.setClientId(clientId);
//        OzonShopMappingDO dmShopMapping = dmShopMappingService.getOzonShopMappingByClientId(clientId);
//        if (null == dmShopMapping) {
//            log.error("未找到对应店铺");
//            throw new ServiceException("未找到对应店铺");
//        }
//
//        ChatCreateRequest chatCreateRequest = new ChatCreateRequest();
//        chatCreateRequest.setApiKey(dmShopMapping.getApiKey());
//        chatCreateRequest.setClientId(dmShopMapping.getClientId());
//        chatCreateRequest.setPostingNumber(postingNumber);
//
//        DmBuyerChat existChat = getExistChat(clientId, postingNumber);
//        if (Objects.nonNull(existChat)) {
//            log.error("该订单已存在聊天记录, postingNumber={}", postingNumber);
//            throw new ServiceException("该订单已存在聊天记录, postingNumber=" + postingNumber);
//        }
//
//        OzonHttpResponse<ChatDTO> createResponse = createChat(chatCreateRequest);
//        if (Objects.isNull(createResponse) || Objects.isNull(createResponse.getResult())) {
//            log.error("创建聊天失败, postingNumber={}", postingNumber);
//            throw new ServiceException("创建聊天失败, postingNumber=" + postingNumber);
//        }
//
//        ChatDTO chatDTO = createResponse.getResult();
//        addChat(clientId, postingNumber, chatDTO.getChatId());
//
//        // Send message
//        String message = postingNumber;
//        if (StringUtils.isBlank(message)) {
//            return;
//        }
//        sendMessageToChat(dmShopMapping, chatDTO.getChatId(), message);
//
//        // Send secondary message
//        message = DictUtils.getDictValue("dm_chat_message", "问候语");
//        sendMessageToChat(dmShopMapping, chatDTO.getChatId(), message);
//    }
//
//    private void sendMessageToChat(DmShopMapping dmShopMapping, String chatId, String message) {
//        ChatSendMessageRequest chatSendMessageRequest = new ChatSendMessageRequest();
//        chatSendMessageRequest.setClientId(dmShopMapping.getClientId());
//        chatSendMessageRequest.setApiKey(dmShopMapping.getApiKey());
//        chatSendMessageRequest.setChatId(chatId);
//        chatSendMessageRequest.setText(message);
//
//        OzonHttpResponse<String> response = sendMessage(chatSendMessageRequest);
//        if (response == null || response.getResult() == null || !response.getResult().equals("success")) {
//            log.error("发送消息失败, postingNumber={}", message);
//            throw new ServiceException("发送消息失败");
//        } else {
//            log.info("发送消息成功, postingNumber={}", message);
//        }
//    }
//
//    private void addChat(String clientId, String postingNumber, String chatId) {
//        DmBuyerChat dmBuyerChat = new DmBuyerChat();
//        dmBuyerChat.setClientId(clientId);
//        dmBuyerChat.setPostingNumber(postingNumber);
//        dmBuyerChat.setChatId(chatId);
//        dmBuyerChatService.insertDmBuyerChat(dmBuyerChat);
//    }
//
//    private DmBuyerChat getExistChat(String clientId, String postingNumber) {
//        DmBuyerChat dmBuyerChat = new DmBuyerChat();
//        dmBuyerChat.setClientId(clientId);
//        dmBuyerChat.setPostingNumber(postingNumber);
//        List<DmBuyerChat> dmBuyerChats = dmBuyerChatService.selectDmBuyerChatList(dmBuyerChat);
//        if (CollectionUtils.isEmpty(dmBuyerChats)) {
//            return null;
//        }
//        return dmBuyerChats.get(0);
//    }
//
//    private OzonHttpResponse<ChatDTO> createChat(ChatCreateRequest request) {
//        TypeReference<OzonHttpResponse<ChatDTO>> typeReference = new TypeReference<OzonHttpResponse<ChatDTO>>() {
//        };
//
//        return httpUtil.post(OzonConfig.OZON_CHAT_START, request, typeReference);
//    }
//
//    private OzonHttpResponse<String> sendMessage(ChatSendMessageRequest request) {
//        TypeReference<OzonHttpResponse<String>> typeReference = new TypeReference<OzonHttpResponse<String>>() {
//        };
//        return httpUtil.post(OzonConfig.OZON_CHAT_SEND_MESSAGE, request, typeReference);
//    }
//}
