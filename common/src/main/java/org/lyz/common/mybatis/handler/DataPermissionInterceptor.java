package org.lyz.common.mybatis.handler;

import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.lyz.common.mybatis.annotation.DataColumn;
import org.lyz.common.mybatis.annotation.DataPermission;
import org.lyz.common.mybatis.enums.DataScopeType;
import org.lyz.common.mybatis.helper.DataPermissionHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DataPermissionInterceptor implements InnerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(DataPermissionInterceptor.class);
    private static final Map<String, DataScopeInfo> CACHE = new ConcurrentHashMap<>();

    @Override
    public void beforeQuery(Executor executor, MappedStatement ms, Object parameter,
                            RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) {
        String userId = DataPermissionHelper.getUserId();
        if (userId == null) {
            return;
        }

        DataScopeInfo scopeInfo = getDataScopeInfo(ms);
        if (scopeInfo == null || scopeInfo.type == DataScopeType.ALL) {
            return;
        }

        try {
            String sql = boundSql.getSql();
            Select select = (Select) CCJSqlParserUtil.parse(sql);
            if (!(select.getSelectBody() instanceof PlainSelect plainSelect)) {
                return;
            }

            EqualsTo equalsTo = new EqualsTo();
            equalsTo.setLeftExpression(new Column(scopeInfo.column));
            equalsTo.setRightExpression(new StringValue(userId));

            Expression where = plainSelect.getWhere();
            if (where == null) {
                plainSelect.setWhere(equalsTo);
            } else {
                plainSelect.setWhere(new AndExpression(where, equalsTo));
            }

            try {
                java.lang.reflect.Field sqlField = BoundSql.class.getDeclaredField("sql");
                sqlField.setAccessible(true);
                sqlField.set(boundSql, select.toString());
            } catch (Exception e) {
                log.warn("Failed to set modified SQL", e);
            }
        } catch (JSQLParserException e) {
            log.warn("Failed to parse SQL for data permission", e);
        }
    }

    private DataScopeInfo getDataScopeInfo(MappedStatement ms) {
        String id = ms.getId();
        return CACHE.computeIfAbsent(id, k -> {
            try {
                String className = k.substring(0, k.lastIndexOf('.'));
                String methodName = k.substring(k.lastIndexOf('.') + 1);
                Class<?> clazz = Class.forName(className);
                for (Method method : clazz.getMethods()) {
                    if (method.getName().equals(methodName)) {
                        DataPermission ann = method.getAnnotation(DataPermission.class);
                        if (ann != null) {
                            DataScopeInfo info = new DataScopeInfo();
                            info.type = ann.type();
                            info.column = ann.column().value();
                            return info;
                        }
                        return null;
                    }
                }
            } catch (ClassNotFoundException e) {
                log.warn("Class not found: {}", k, e);
            }
            return null;
        });
    }

    private static class DataScopeInfo {
        DataScopeType type;
        String column;
    }
}
