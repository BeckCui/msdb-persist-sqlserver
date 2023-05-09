package com.dhl.fin.api.common.util;

import com.dhl.fin.api.common.service.CommonService;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.EmbeddedValueResolverAware;
import org.springframework.core.ResolvableType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringValueResolver;

import java.util.Map;

@Component
public class SpringContextUtil implements BeanFactoryPostProcessor, EmbeddedValueResolverAware {

    private static ConfigurableListableBeanFactory applicationContext;

    private static StringValueResolver stringValueResolver;


    /**
     * 动态获取 bean 对象
     *
     * @return
     */
    public static Object getBean(String beanId) throws BeansException {
        return applicationContext.getBean(beanId);
    }

    public static <T> T getBean(Class<T> clazz, Object... args) {
        return applicationContext.getBean(clazz, args);
    }


    public static Map<String, Object> getBeanOfType(Class parentClass) {
        return applicationContext.getBeansOfType(parentClass);
    }

    /**
     * 动态获取 ServiceImpl 对象
     *
     * @param domainClass
     * @return
     */
    public static CommonService getServiceImplByDomain(Class domainClass) {
        String[] beanNamesForType = applicationContext.getBeanNamesForType(ResolvableType.forClassWithGenerics(CommonService.class, domainClass));
        return (CommonService) applicationContext.getBean(beanNamesForType[0]);
    }

    /**
     * 动态获取配置文件中的值
     *
     * @param name
     * @return
     */
    public static String getPropertiesValue(String name) {
        try {
            name = "${" + name + "}";
            return stringValueResolver.resolveStringValue(name);
        } catch (Exception e) {
            return null;
        }
    }


    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory configurableListableBeanFactory) throws BeansException {
        applicationContext = configurableListableBeanFactory;
    }

    @Override
    public void setEmbeddedValueResolver(StringValueResolver resolver) {
        stringValueResolver = resolver;
    }
}
