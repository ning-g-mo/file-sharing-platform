package cn.lemwood.fileshare.config;

import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PropertySourceFactory;

import java.io.IOException;
import java.util.Properties;

/**
 * YAML属性源工厂
 * 用于支持在@PropertySource注解中使用YAML文件
 */
public class YamlPropertySourceFactory implements PropertySourceFactory {

    @Override
    public PropertySource<?> createPropertySource(String name, EncodedResource resource) throws IOException {
        YamlPropertiesFactoryBean factory = new YamlPropertiesFactoryBean();
        factory.setResources(resource.getResource());
        
        Properties properties = factory.getObject();
        if (properties == null) {
            properties = new Properties();
        }
        
        return new PropertiesPropertySource(
            name != null ? name : resource.getResource().getFilename(), 
            properties
        );
    }
}