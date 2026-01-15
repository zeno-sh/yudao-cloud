package cn.iocoder.yudao.module.chrome.service.email;

import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.time.LocalDateTime;
import cn.iocoder.yudao.module.chrome.controller.admin.email.vo.*;
import cn.iocoder.yudao.module.chrome.dal.dataobject.email.EmailCodeDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import cn.iocoder.yudao.module.system.api.mail.MailSendApi;
import cn.iocoder.yudao.module.system.api.mail.dto.MailSendSingleToUserReqDTO;
import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.module.chrome.enums.ChromeConstants;
import lombok.extern.slf4j.Slf4j;

import cn.iocoder.yudao.module.chrome.dal.mysql.email.EmailCodeMapper;
import cn.iocoder.yudao.module.chrome.service.user.UserService;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.chrome.enums.ErrorCodeConstants.*;

/**
 * Chrome邮箱验证码 Service 实现类
 *
 * @author Jax
 */
@Service
@Validated
@Slf4j
public class EmailCodeServiceImpl implements EmailCodeService {

    /**
     * 邮件模板编码常量
     */
    private static final String MAIL_TEMPLATE_CODE_VERIFICATION = "test";

    @Resource
    private EmailCodeMapper emailCodeMapper;
    
    @Resource
    private MailSendApi mailSendApi;
    
    @Resource
    private UserService userService;

    @Override
    public Long createEmailCode(EmailCodeSaveReqVO createReqVO) {
        // 插入
        EmailCodeDO emailCode = BeanUtils.toBean(createReqVO, EmailCodeDO.class);
        emailCodeMapper.insert(emailCode);
        // 返回
        return emailCode.getId();
    }

    @Override
    public void updateEmailCode(EmailCodeSaveReqVO updateReqVO) {
        // 校验存在
        validateEmailCodeExists(updateReqVO.getId());
        // 更新
        EmailCodeDO updateObj = BeanUtils.toBean(updateReqVO, EmailCodeDO.class);
        emailCodeMapper.updateById(updateObj);
    }

    @Override
    public void deleteEmailCode(Long id) {
        // 校验存在
        validateEmailCodeExists(id);
        // 删除
        emailCodeMapper.deleteById(id);
    }

    private void validateEmailCodeExists(Long id) {
        if (emailCodeMapper.selectById(id) == null) {
            throw exception(EMAIL_CODE_NOT_EXISTS);
        }
    }

    @Override
    public EmailCodeDO getEmailCode(Long id) {
        return emailCodeMapper.selectById(id);
    }

    @Override
    public PageResult<EmailCodeDO> getEmailCodePage(EmailCodePageReqVO pageReqVO) {
        return emailCodeMapper.selectPage(pageReqVO);
    }

    @Override
    public void sendEmailCode(String email, Integer scene, String clientIP) {
        // 1. 校验邮箱存在性（注册场景：邮箱不能存在；其他场景：邮箱必须存在）
        validateEmailExistence(email, scene);
        
        // 2. 检查发送频率限制
        validateSendFrequency(email, scene);
        
        // 3. 生成6位数字验证码
        String code = String.format("%06d", new Random().nextInt(1000000));
        
        // 4. 设置过期时间
        LocalDateTime expireTime = LocalDateTime.now().plusMinutes(ChromeConstants.EmailCode.EXPIRE_MINUTES);
        
        // 5. 创建验证码记录
        EmailCodeDO emailCode = EmailCodeDO.builder()
                .email(email)
                .code(code)
                .scene(scene)
                .used(false)
                .expireTime(expireTime)
                .build();
        
        // 6. 保存到数据库
        emailCodeMapper.insert(emailCode);
        
        // 7. 发送邮件
        try {
            Map<String, Object> templateParams = new HashMap<>();
            templateParams.put("code", code);
            templateParams.put("expireMinutes", ChromeConstants.EmailCode.EXPIRE_MINUTES);
            
            MailSendSingleToUserReqDTO reqDTO = new MailSendSingleToUserReqDTO();
            reqDTO.setToMails(Collections.singletonList(email));
            reqDTO.setTemplateCode(MAIL_TEMPLATE_CODE_VERIFICATION);
            reqDTO.setTemplateParams(templateParams);
            
            mailSendApi.sendSingleMailToMember(reqDTO);
        } catch (Exception e) {
            log.error("邮箱验证码发送失败，邮箱：{}，错误：{}", email, e.getMessage(), e);
            throw exception(EMAIL_CODE_SEND_FAILED);
        }
    }

    @Override
    public void validateEmailCode(String email, String code, Integer scene) {
        // 1. 查询该邮箱和场景下最新的未使用验证码
        LambdaQueryWrapper<EmailCodeDO> wrapper = new LambdaQueryWrapper<EmailCodeDO>()
                .eq(EmailCodeDO::getEmail, email)
                .eq(EmailCodeDO::getScene, scene)
                .eq(EmailCodeDO::getUsed, false)
                .orderByDesc(EmailCodeDO::getCreateTime)
                .last("LIMIT 1");
        
        EmailCodeDO emailCode = emailCodeMapper.selectOne(wrapper);
        
        // 2. 验证码不存在或场景不匹配
        if (emailCode == null) {
            throw exception(EMAIL_CODE_NOT_EXISTS);
        }
        
        // 3. 验证码已过期
        if (LocalDateTime.now().isAfter(emailCode.getExpireTime())) {
            // 删除过期的验证码
            emailCodeMapper.deleteById(emailCode.getId());
            throw exception(EMAIL_CODE_EXPIRED);
        }
        
        // 4. 验证码不匹配
        if (!code.equals(emailCode.getCode())) {
            throw exception(EMAIL_CODE_INVALID);
        }
        
        // 5. 标记验证码为已使用
        emailCode.setUsed(true);
        emailCodeMapper.updateById(emailCode);
    }

    /**
     * 校验邮箱存在性
     *
     * @param email 邮箱
     * @param scene 场景（10注册 20忘记密码 30修改密码）
     */
    private void validateEmailExistence(String email, Integer scene) {
        boolean userExists = userService.getUserByEmail(email) != null;
        
        // 注册场景：邮箱不能存在
        if (scene == 10 && userExists) {
            throw exception(USER_EMAIL_EXISTS);
        }
        
        // 忘记密码和修改密码场景：邮箱必须存在
        if ((scene == 20 || scene == 30) && !userExists) {
            throw exception(USER_NOT_EXISTS);
        }
    }

    /**
     * 校验发送频率限制
     *
     * @param email 邮箱
     * @param scene 场景
     */
    private void validateSendFrequency(String email, Integer scene) {
        LocalDateTime now = LocalDateTime.now();
        
        // 1. 检查发送间隔内是否已发送过验证码
        LocalDateTime intervalAgo = now.minusSeconds(ChromeConstants.EmailCode.SEND_INTERVAL_SECONDS);
        LambdaQueryWrapper<EmailCodeDO> recentWrapper = new LambdaQueryWrapper<EmailCodeDO>()
                .eq(EmailCodeDO::getEmail, email)
                .eq(EmailCodeDO::getScene, scene)
                .ge(EmailCodeDO::getCreateTime, intervalAgo);
        
        long recentCount = emailCodeMapper.selectCount(recentWrapper);
        if (recentCount > 0) {
            throw exception(EMAIL_CODE_SEND_TOO_FREQUENT);
        }
        
        // 2. 检查当天发送次数是否超过限制
        LocalDateTime todayStart = now.toLocalDate().atStartOfDay();
        LambdaQueryWrapper<EmailCodeDO> todayWrapper = new LambdaQueryWrapper<EmailCodeDO>()
                .eq(EmailCodeDO::getEmail, email)
                .eq(EmailCodeDO::getScene, scene)
                .ge(EmailCodeDO::getCreateTime, todayStart);
        
        long todayCount = emailCodeMapper.selectCount(todayWrapper);
        if (todayCount >= ChromeConstants.EmailCode.MAX_SEND_COUNT_PER_DAY) {
            throw exception(EMAIL_CODE_DAILY_LIMIT_EXCEEDED);
        }
    }

}