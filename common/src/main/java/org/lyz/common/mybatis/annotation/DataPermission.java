package org.lyz.common.mybatis.annotation;

import org.lyz.common.mybatis.enums.DataScopeType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DataPermission {
    DataScopeType type() default DataScopeType.ALL;
    DataColumn column() default @DataColumn("user_id");
}
