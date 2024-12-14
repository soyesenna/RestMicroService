package apihive.config;

import apihive.config.strategy.InterfaceDiscoveryStrategy;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

public class InterfaceLoaderFactoryBean implements FactoryBean<InterfaceLoader>, InitializingBean {
    private final ApiHiveConfig config;
    private final InterfaceDiscoveryStrategy strategy;
    private InterfaceLoader interfaceLoader;

    public InterfaceLoaderFactoryBean(ApiHiveConfig config, InterfaceDiscoveryStrategy strategy) {
        this.config = config;
        this.strategy = strategy;
    }

    @Override
    public InterfaceLoader getObject() {
        return interfaceLoader;
    }

    @Override
    public Class<?> getObjectType() {
        return InterfaceLoader.class;
    }

    @Override
    public void afterPropertiesSet() {
        this.interfaceLoader = new InterfaceLoader(strategy);
    }

    public void loadInterfaces() {
        if (config.getEnableApiHive()) {
            interfaceLoader.loadInterfaces();
        }
    }
} 