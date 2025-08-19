package cn.lemwood.fileshare.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.Properties;

/**
 * JPA配置类
 * 动态配置Hibernate方言和其他JPA属性
 */
@Configuration
public class JpaConfig {

    @Autowired
    private DatabaseConfig databaseConfig;

    @Autowired
    private DataSource dataSource;

    /**
     * 配置EntityManagerFactory
     */
    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan("cn.lemwood.fileshare.entity");

        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);
        em.setJpaProperties(hibernateProperties());

        return em;
    }

    /**
     * 配置事务管理器
     */
    @Bean
    public PlatformTransactionManager transactionManager(EntityManagerFactory emf) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(emf);
        return transactionManager;
    }

    /**
     * 配置Hibernate属性
     */
    private Properties hibernateProperties() {
        Properties properties = new Properties();
        
        // 动态设置数据库方言
        properties.setProperty("hibernate.dialect", databaseConfig.getDatabaseDialect());
        
        // 基本配置
        properties.setProperty("hibernate.hbm2ddl.auto", "update");
        properties.setProperty("hibernate.show_sql", "false");
        properties.setProperty("hibernate.format_sql", "true");
        properties.setProperty("hibernate.use_sql_comments", "true");
        
        // 根据数据库类型设置特定配置
        if (databaseConfig.isMySQLDatabase()) {
            // MySQL特定配置
            properties.setProperty("hibernate.connection.characterEncoding", "utf8");
            properties.setProperty("hibernate.connection.useUnicode", "true");
            properties.setProperty("hibernate.connection.autoReconnect", "true");
        } else if (databaseConfig.isSQLiteDatabase()) {
            // SQLite特定配置
            properties.setProperty("hibernate.connection.foreign_key_checks", "true");
        }
        
        return properties;
    }
}