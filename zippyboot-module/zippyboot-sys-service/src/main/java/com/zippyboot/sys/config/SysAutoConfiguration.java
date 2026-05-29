package com.zippyboot.sys.config;

import cn.dev33.satoken.stp.StpInterface;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.zippyboot.infra.mybatis.config.MybatisAutoConfiguration;
import com.zippyboot.infra.satoken.config.SaTokenConfig;
import com.zippyboot.sys.manager.PermissionManager;
import com.zippyboot.sys.stp.SaPermissionDelegate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration(after = {SaTokenConfig.class, MybatisAutoConfiguration.class})
@MapperScan("com.zippyboot.sys.mapper")
public class SysAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(StpInterface.class)
    public StpInterface stpInterface(PermissionManager permissionManager) {
        return new SaPermissionDelegate(permissionManager);
    }

    @Bean
    public BeanPostProcessor optimisticLockerProcessor() {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
                if (bean instanceof MybatisPlusInterceptor interceptor) {
                    interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
                }
                return bean;
            }
        };
    }
}
