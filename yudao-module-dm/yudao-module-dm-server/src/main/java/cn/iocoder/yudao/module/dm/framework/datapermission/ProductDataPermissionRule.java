package cn.iocoder.yudao.module.dm.framework.datapermission;

import cn.iocoder.yudao.framework.common.biz.system.permission.PermissionCommonApi;
import cn.iocoder.yudao.framework.datapermission.core.rule.DataPermissionRule;
import cn.iocoder.yudao.framework.mybatis.core.util.MyBatisUtils;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import cn.iocoder.yudao.module.system.enums.permission.RoleCodeEnum;
import com.google.common.collect.Sets;
import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.expression.operators.relational.ParenthesedExpressionList;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @Author: Jax
 * @Date: Created in 20:50 2025/9/25
 */
@Component
public class ProductDataPermissionRule implements DataPermissionRule {

    @Resource
    private AdminUserApi adminUserApi;
    @Resource
    private PermissionCommonApi permissionCommonApi;

    @Override
    public Set<String> getTableNames() {
        return Sets.newHashSet("dm_product_info");
    }

    @Override
    public Expression getExpression(String tableName, Alias tableAlias) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        if (userId == null) {
            return null;
        }

        // 获取用户信息
        AdminUserRespDTO user = adminUserApi.getUser(userId).getCheckedData();
        // 非自定义管理员，则不进行数据权限控制
        if (!permissionCommonApi.hasAnyRoles(userId, RoleCodeEnum.CUSTOM_ADMIN.getCode()).getCheckedData()) {
            return null;
        }

        // 如果授权产品列表为空，表示可以访问所有产品
        if (CollectionUtils.isEmpty(user.getProductIds())) {
            return null; // 无限制，可以访问所有产品
        }

        // 构建条件：creator = userId OR id IN (授权产品ID)
        List<Expression> conditions = new ArrayList<>();

        // 添加创建者条件
        conditions.add(new EqualsTo(
                MyBatisUtils.buildColumn(tableName, tableAlias, "creator"),
                new StringValue(userId.toString())
        ));

        // 添加授权产品条件
        Set<Long> authorizedProductIds = user.getProductIds();
        if (!authorizedProductIds.isEmpty()) {
            ExpressionList<LongValue> idList = new ExpressionList<>();
            authorizedProductIds.forEach(id -> idList.getExpressions().add(new LongValue(id)));
            conditions.add(new InExpression(
                    MyBatisUtils.buildColumn(tableName, tableAlias, "id"),
                    // Parenthesis 的目的，是提供 (1,2,3) 的 () 左右括号
                    new Parenthesis(idList)
            ));
        }

        // 合并所有条件，使用括号包裹 OR 表达式，避免 AND/OR 优先级问题
        // 正确生成: (creator = ? OR id IN (?))，而不是裸的 OR 导致条件被错误拆分
        Expression result = conditions.get(0);
        for (int i = 1; i < conditions.size(); i++) {
            result = new OrExpression(result, conditions.get(i));
        }

        return new ParenthesedExpressionList(result);
    }
}

