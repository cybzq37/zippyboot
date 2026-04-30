package com.zippyboot.kit.jackson.config;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class SpringBeanHolder implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        applicationContext = context;
    }

    public static <T> T getBean(Class<T> type) {
        if (applicationContext == null) {
            return null;
        }
        try {
            return applicationContext.getBean(type);
        } catch (BeansException ex) {
            return null;
        }
    }
}
