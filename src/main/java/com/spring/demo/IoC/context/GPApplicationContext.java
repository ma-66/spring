package com.spring.demo.IoC.context;

import com.spring.demo.IoC.beans.GPBeanDefinition;
import com.spring.demo.IoC.beans.GPBeanWrapper;
import com.spring.demo.IoC.core.GPBeanFactory;
import com.spring.demo.aop.*;
import com.spring.demo.mvc.annotation.GPAutowired;
import com.spring.demo.mvc.annotation.GPController;
import com.spring.demo.mvc.annotation.GPService;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 按之前源码分析的套路，IoC，DI，MVC，AOP
 */
public class GPApplicationContext extends GPDefaultListableBeanFactory implements GPBeanFactory {

    private String[] configLoactions;
    private GPBeanDefinitionReader reader;

    //单例的IoC容器缓存
    private Map<String, Object> factoryBenaObjectCache = new ConcurrentHashMap<String, Object>();
    //通用的IoC容器
    private Map<String, GPBeanWrapper> factoryBeanInstanceCache = new ConcurrentHashMap<String, GPBeanWrapper>();

    public GPApplicationContext(String... configLoactions) {
        this.configLoactions = configLoactions;
        try {
            refresh();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void refresh() throws Exception {
        //1.定位，定位配置文件
        reader = new GPBeanDefinitionReader(this.configLoactions);
        //2.加载配置文件，扫描相关的类，把它们封装成benaDefinition
        List<GPBeanDefinition> beanDefinitions = reader.loadBeanDefinitions();
        //3.注册，把配置信息放到容器里面（伪IoC容器）
        doRegisterBeanDefinition(beanDefinitions);
        //4.把不是延迟加载的类提前初始化
        doAutowrited();
    }

    //只处理非延迟加载的情况
    private void doAutowrited() {
        for (Map.Entry<String, GPBeanDefinition> beanDefinitionEntry :
                super.beanDefinitionMap.entrySet()) {
            String beanName = beanDefinitionEntry.getKey();
            if (!beanDefinitionEntry.getValue().isLazyInit()) {
                try {
                    getBean(beanName);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void doRegisterBeanDefinition(List<GPBeanDefinition> beanDefinitions) throws Exception {
        for (GPBeanDefinition beanDefinition : beanDefinitions) {
            if (super.beanDefinitionMap.containsKey(beanDefinition.getFactoryBeanName())) {
                throw new Exception("The `" + beanDefinition.getFactoryBeanName() + "` is exists!");
            }
            super.beanDefinitionMap.put(beanDefinition.getFactoryBeanName(), beanDefinition);
        }
        //到这里容器初始化完毕
    }

    @Override
    public Object getBean(Class<?> beanClass) throws Exception {
        return getBean(beanClass.getName());
    }

    //依赖注入，从在这里开始，读取beanDefinition中的信息
    //然后通过反射机制创建一个实例并返回
    //spring做法是，不会吧最原始的对象放出去，会用一个beanWrapper来进行一次包装
    //装饰器模式：
    //1.保留原来的OOP关系
    //2.需要对它进行扩展，增强（为了以后的AOP打基础）
    //依赖注入DI入口
    @Override
    public Object getBean(String beanName) throws Exception {
        GPBeanDefinition beanDefinition = super.beanDefinitionMap.get(beanName);
        try {
            //生成通知事件
            GPBeanPostProcessor beanPostProcessor = new GPBeanPostProcessor();
            Object instance = instantiateBean(beanDefinition);
            if (null == instance) {
                return null;
            }

            //在实列初始化之前调一次
            beanPostProcessor.postProcessBeforeInitialization(instance, beanName);
            GPBeanWrapper beanWrapper = new GPBeanWrapper(instance);
            this.factoryBeanInstanceCache.put(beanName, beanWrapper);

            //在实例初始化之后调一次
            beanPostProcessor.postProcessAfterInitialization(instance, beanName);
            populateBean(beanName, instance);

            //通过这样调用，相当于给我们自己留一个可操作的空间
            return this.factoryBeanInstanceCache.get(beanName).getWrappedInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void populateBean(String beanName, Object instance) {
        Class clazz = instance.getClass();
        if (!(clazz.isAnnotationPresent(GPController.class) ||
                clazz.isAnnotationPresent(GPService.class))) {
            return;
        }

        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            if (!field.isAnnotationPresent(GPAutowired.class)) {
                continue;
            }
            GPAutowired autowired = field.getAnnotation(GPAutowired.class);
            String aotuwiredBeanName = autowired.value().trim();
            if ("".equals(aotuwiredBeanName)) {
                aotuwiredBeanName = field.getType().getName();
            }
            field.setAccessible(true);

            try {
                field.set(instance, this.factoryBeanInstanceCache.get(aotuwiredBeanName).getWrappedInstance());
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

    }

    //传一个beanDefinition，就返回一个实例bean
    private Object instantiateBean(GPBeanDefinition beanDefinition) {
        Object instance = null;
        String className = beanDefinition.getBeanClassName();
        try {
            //因为根据class才能确定一个类是否有实例
            if (this.factoryBenaObjectCache.containsKey(className)) {
                instance = this.factoryBenaObjectCache.get(className);
            } else {
                Class<?> clazz = Class.forName(className);
                instance = clazz.newInstance();
//                this.factoryBenaObjectCache.put(beanDefinition.getFactoryBeanName(),instance);
                GPAdvisedSupport config = instantionAopConfig(beanDefinition);
                config.setTargetClass(clazz);
                config.setTarget(instance);
                if (config.pointCutMatch()) {
                    instance = createProxy(config).getProxy();
                }
                this.factoryBenaObjectCache.put(beanDefinition.getFactoryBeanName(), instance);
            }
            return instance;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private GPAdvisedSupport instantionAopConfig(GPBeanDefinition beanDefinition) throws
            Exception {
        GPAopConfig config = new GPAopConfig();
        config.setPointCut(reader.getConfig().getProperty("pointCut"));
        config.setAspectClass(reader.getConfig().getProperty("aspectClass"));
        config.setAspectBefore(reader.getConfig().getProperty("aspectBefore"));
        config.setAspectAfter(reader.getConfig().getProperty("aspectAfter"));
        config.setAspectAfterThrow(reader.getConfig().getProperty("aspectAfterThrow"));
        config.setAspectAfterThrowingName(reader.getConfig().getProperty("aspectAfterThrowingName"));
        return new GPAdvisedSupport(config);
    }

    private GPAopProxy createProxy(GPAdvisedSupport config) {
        Class targetClass = config.getTargetClass();
        if (targetClass.getInterfaces().length > 0) {
            return new GPJdkDynamicAopProxy(config);
        }
        return new GPCglibAopProxy(config);
    }


    public String[] getBeanDefinitionNames() {
        return this.beanDefinitionMap.keySet().toArray(new String[this.beanDefinitionMap.size()]);
    }

    public int getBeanDefinitionCount() {
        return this.beanDefinitionMap.size();
    }

    public Properties getConfig() {
        return this.reader.getConfig();
    }
}
