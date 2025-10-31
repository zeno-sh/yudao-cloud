package cn.iocoder.yudao.module.dm.service.warehouse;

import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import cn.iocoder.yudao.module.dm.controller.admin.warehouse.vo.*;
import cn.iocoder.yudao.module.dm.dal.dataobject.warehouse.FbsPushOrderLogDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;

import cn.iocoder.yudao.module.dm.dal.mysql.warehouse.FbsPushOrderLogMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.dm.enums.ErrorCodeConstants.*;

/**
 * 海外仓推单记录 Service 实现类
 *
 * @author Zeno
 */
@Service
@Validated
public class FbsPushOrderLogServiceImpl implements FbsPushOrderLogService {

    @Resource
    private FbsPushOrderLogMapper fbsPushOrderLogMapper;

    @Override
    public Long createFbsPushOrderLog(FbsPushOrderLogSaveReqVO createReqVO) {
        // 插入
        FbsPushOrderLogDO fbsPushOrderLog = BeanUtils.toBean(createReqVO, FbsPushOrderLogDO.class);
        fbsPushOrderLogMapper.insert(fbsPushOrderLog);
        // 返回
        return fbsPushOrderLog.getId();
    }

    @Override
    public void updateFbsPushOrderLog(FbsPushOrderLogSaveReqVO updateReqVO) {
        // 校验存在
        validateFbsPushOrderLogExists(updateReqVO.getId());
        // 更新
        FbsPushOrderLogDO updateObj = BeanUtils.toBean(updateReqVO, FbsPushOrderLogDO.class);
        fbsPushOrderLogMapper.updateById(updateObj);
    }

    @Override
    public void deleteFbsPushOrderLog(Long id) {
        // 校验存在
        validateFbsPushOrderLogExists(id);
        // 删除
        fbsPushOrderLogMapper.deleteById(id);
    }

    private void validateFbsPushOrderLogExists(Long id) {
//        if (fbsPushOrderLogMapper.selectById(id) == null) {
//            throw exception(FBS_PUSH_ORDER_LOG_NOT_EXISTS);
//        }
    }

    @Override
    public FbsPushOrderLogDO getFbsPushOrderLog(Long id) {
        return fbsPushOrderLogMapper.selectById(id);
    }

    @Override
    public List<FbsPushOrderLogDO> getFbsPushOrderLogByOrderId(Collection<Long> orderIds) {
        return fbsPushOrderLogMapper.selectList(new LambdaQueryWrapperX<FbsPushOrderLogDO>()
                .inIfPresent(FbsPushOrderLogDO::getOrderId, orderIds)
                .orderByDesc(FbsPushOrderLogDO::getCreateTime));
    }

    @Override
    public List<FbsPushOrderLogDO> getFbsPushOrderLogByPostingNumbers(Collection<String> postingNumbers) {
        return fbsPushOrderLogMapper.selectList(new LambdaQueryWrapperX<FbsPushOrderLogDO>()
                .inIfPresent(FbsPushOrderLogDO::getPostingNumber, postingNumbers)
                .orderByDesc(FbsPushOrderLogDO::getCreateTime));
    }

    @Override
    public PageResult<FbsPushOrderLogDO> getFbsPushOrderLogPage(FbsPushOrderLogPageReqVO pageReqVO) {
        return fbsPushOrderLogMapper.selectPage(pageReqVO);
    }

}