package com.laureninnovations.util;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class SpringApplicationContext implements ApplicationContextAware {
    static private ApplicationContext applicationContext;

    static public Object getBean(String beanName) {
        return applicationContext.getBean(beanName);
    }

    static public <T> T getBean(Class<T> klass) {
        return applicationContext.getBean(klass);
    }

    @Override
    public void setApplicationContext(ApplicationContext ac) throws BeansException {
        SpringApplicationContext.applicationContext = ac;
    }
}
