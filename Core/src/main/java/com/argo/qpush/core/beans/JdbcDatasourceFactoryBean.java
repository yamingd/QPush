package com.argo.qpush.core.beans;

import com.jolbox.bonecp.BoneCPConfig;
import com.jolbox.bonecp.BoneCPDataSource;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.Properties;

/**
 * Created by yaming_deng on 14-9-10.
 */
public class JdbcDatasourceFactoryBean implements FactoryBean<BoneCPDataSource>, InitializingBean, DisposableBean {

    private Properties jdbcConfig;

    private BoneCPDataSource dataSource;

    @Override
    public BoneCPDataSource getObject() throws Exception {
        return dataSource;
    }

    @Override
    public Class<?> getObjectType() {
        return BoneCPDataSource.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(jdbcConfig, "JdbcConfig must not be NULL.");

        BoneCPConfig config = new BoneCPConfig();
        Field[] fields = BoneCPConfig.class.getDeclaredFields();
        Iterator<Object> itor = jdbcConfig.keySet().iterator();
        while (itor.hasNext()){
            String name = (String)itor.next();
            Object value = jdbcConfig.get(name);
            for (Field field: fields){
                if (field.getName().equalsIgnoreCase(name)){
                    try {
                        field.setAccessible(true);
                        field.set(config, value);
                    } catch (Exception e) {
                        // should never happen
                    }
                }

            }
        }

        dataSource = new BoneCPDataSource(config);
    }

    public Properties getJdbcConfig() {
        return jdbcConfig;
    }

    public void setJdbcConfig(Properties jdbcConfig) {
        this.jdbcConfig = jdbcConfig;
    }

    @Override
    public void destroy() throws Exception {
        this.dataSource.close();
    }
}
