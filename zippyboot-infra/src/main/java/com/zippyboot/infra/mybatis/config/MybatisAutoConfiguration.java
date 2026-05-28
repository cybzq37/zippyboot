package com.zippyboot.infra.mybatis.config;

import com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zippyboot.infra.mybatis.handler.JsonTypeHandler;
import com.zippyboot.infra.mybatis.handler.PgJsonbTypeHandler;
import com.zippyboot.infra.mybatis.interceptor.PageableInterceptor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;

import java.util.List;

/**
 * MyBatis-Plus 自动配置。
 * <p>
 * 提供分页拦截器、{@link PageableInterceptor} 和 ObjectMapper 注入。
 * <p>
 * 需在 application.yml 中配置 handler 扫描路径：
 * <pre>
 * mybatis-plus:
 *   type-handlers-package: com.zippyboot.infra.mybatis.handler
 * </pre>
 * <p>
 * 拦截器执行顺序（MyBatis 后注册先执行）：
 * <ol>
 *   <li>{@link PageableInterceptor} — 参数处理（Pageable → Page）+ 结果处理（IPage → Spring Data Page）</li>
 *   <li>{@link MybatisPlusInterceptor}（含 PaginationInnerInterceptor）— 物理分页</li>
 * </ol>
 */
@AutoConfiguration(after = MybatisPlusAutoConfiguration.class)
@ConditionalOnClass(MybatisPlusInterceptor.class)
public class MybatisAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor());
        return interceptor;
    }

    /**
     * 注册 PageableInterceptor 到所有 SqlSessionFactory。
     * <p>
     * MyBatis 拦截链为装饰器模式：后注册的先执行。
     * MybatisPlusInterceptor 由 MyBatis-Plus 自动注册（先注册），
     * PageableInterceptor 在此手动追加（后注册），因此执行时排在前面。
     */
    @Bean
    @DependsOn("mybatisPlusInterceptor")
    public PageableInterceptorRegistrar pageableInterceptorRegistrar(List<SqlSessionFactory> sqlSessionFactoryList) {
        return new PageableInterceptorRegistrar(sqlSessionFactoryList);
    }

    @Bean
    public TypeHandlerObjectMapperInjector typeHandlerObjectMapperInjector(ObjectMapper objectMapper) {
        JsonTypeHandler.setObjectMapper(objectMapper);
        PgJsonbTypeHandler.setObjectMapper(objectMapper);
        return new TypeHandlerObjectMapperInjector();
    }

    public static class PageableInterceptorRegistrar {

        PageableInterceptorRegistrar(List<SqlSessionFactory> sqlSessionFactoryList) {
            PageableInterceptor interceptor = new PageableInterceptor();
            for (SqlSessionFactory factory : sqlSessionFactoryList) {
                org.apache.ibatis.session.Configuration config = factory.getConfiguration();
                if (config.getInterceptors().stream().noneMatch(PageableInterceptor.class::isInstance)) {
                    config.addInterceptor(interceptor);
                }
            }
        }
    }

    /** 标记 Bean，表示 ObjectMapper 已注入到 TypeHandler。 */
    public static class TypeHandlerObjectMapperInjector {
    }
}
