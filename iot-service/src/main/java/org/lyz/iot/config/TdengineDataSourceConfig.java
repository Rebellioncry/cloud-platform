package org.lyz.iot.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
@MapperScan(basePackages = "org.lyz.iot.mapper.tdengine", sqlSessionFactoryRef = "tdengineSqlSessionFactory")
public class TdengineDataSourceConfig {

    @Bean("tdengineDataSource")
    @ConfigurationProperties(prefix = "tdengine.datasource.hikari")
    public DataSource tdengineDataSource() {
        return new HikariDataSource();
    }

    @Bean("tdengineMybatisPlusInterceptor")
    public MybatisPlusInterceptor tdengineMybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }

    @Bean("tdengineSqlSessionFactory")
    public SqlSessionFactory tdengineSqlSessionFactory(
            @Qualifier("tdengineDataSource") DataSource dataSource,
            @Qualifier("tdengineMybatisPlusInterceptor") MybatisPlusInterceptor interceptor) throws Exception {
        MybatisSqlSessionFactoryBean factory = new MybatisSqlSessionFactoryBean();
        factory.setDataSource(dataSource);
        factory.setPlugins(interceptor);
        factory.setMapperLocations(
                new PathMatchingResourcePatternResolver().getResources("classpath*:mapper/tdengine/**/*.xml"));

        MybatisConfiguration configuration = new MybatisConfiguration();
        configuration.setMapUnderscoreToCamelCase(true);
        factory.setConfiguration(configuration);
        factory.setTypeAliasesPackage("org.lyz.iot.entity");

        return factory.getObject();
    }

    @Bean("tdengineSqlSessionTemplate")
    public SqlSessionTemplate tdengineSqlSessionTemplate(
            @Qualifier("tdengineSqlSessionFactory") SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }

    @Bean("tdengineTransactionManager")
    public PlatformTransactionManager tdengineTransactionManager(@Qualifier("tdengineDataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }
}
