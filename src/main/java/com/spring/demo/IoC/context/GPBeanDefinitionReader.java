package com.spring.demo.IoC.context;

import com.spring.demo.IoC.beans.GPBeanDefinition;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

//对配置文件进行查找，读取，解析
public class GPBeanDefinitionReader {
    private List<String> registyBeanClasses = new ArrayList<String>();
    private Properties config = new Properties();
    //固定配置文件中的key，相对XML的规范
    private final String SCAN_PACKAGE = "scanPackage";

    public GPBeanDefinitionReader(String... locations) {
        //通过URL定位找到其所对应的文件，然后转换为文件流
        InputStream is = this.getClass().getClassLoader().getResourceAsStream(locations[0].replace("classpath:", ""));
        try {
            config.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != is) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        doScanner(config.getProperty(SCAN_PACKAGE));

    }

    private void doScanner(String scanPackage) {
        //转换为文件路径，实际上就是把.替代为/
        URL url = this.getClass().getClassLoader().getResource("/" + scanPackage.replaceAll("\\.", "/"));
        File classPath = new File(url.getFile());
        for (File file : classPath.listFiles()) {
            if (file.isDirectory()) {
                doScanner(scanPackage + "." + file.getName());
            } else {
                if (!file.getName().endsWith(".class")) {
                    continue;
                }
                String className = (scanPackage + "." + file.getName().replace(".class", ""));
                registyBeanClasses.add(className);
            }
        }
    }

    public Properties getConfig() {
        return this.config;
    }

    //把配置文件中扫描到的所有配置文件转换为GPBeanDefinition对象，以便于之后的IoC操作
    public List<GPBeanDefinition> loadBeanDefinitions() {
        List<GPBeanDefinition> result = new ArrayList<GPBeanDefinition>();
        try {
            for (String className : registyBeanClasses) {
                Class<?> beanClass = Class.forName(className);
                if (beanClass.isInterface()) {
                    continue;
                }
                result.add(doCreateBeanDefinition(toLowerFirstCase(beanClass.getName()), beanClass.getName()));

                Class<?>[] interfaces = beanClass.getInterfaces();
                for (Class<?> i : interfaces) {
                    result.add(doCreateBeanDefinition(i.getName(), beanClass.getName()));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    //把每一个配置信息解析成一个BenaDefinition
    private GPBeanDefinition doCreateBeanDefinition(String factoryBeanName, String beanClassName) {
        GPBeanDefinition beanDefinition = new GPBeanDefinition();
        beanDefinition.setBeanClassName(beanClassName);
        beanDefinition.setFactoryBeanName(factoryBeanName);
        return beanDefinition;
    }

    //将类名首字母改为小写
    //为了简化程序逻辑，就不做其他判断了，大家了解就好
    private String toLowerFirstCase(String simpleName) {
        char[] chars = simpleName.toCharArray();
        //因为大小写字母的ASCII码相差32
        //而且大写字母的ASCII码要小于小写字母的ASCII码
        //java中，对char做算数运算，实际就是对ASCII码做算数运算
        chars[0] += 32;
        return String.valueOf(chars);
    }


}
