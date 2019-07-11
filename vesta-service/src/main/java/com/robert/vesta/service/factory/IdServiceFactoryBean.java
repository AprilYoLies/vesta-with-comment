package com.robert.vesta.service.factory;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.robert.vesta.service.impl.IdServiceImpl;
import com.robert.vesta.service.impl.provider.DbMachineIdProvider;
import com.robert.vesta.service.impl.provider.IpConfigurableMachineIdProvider;
import com.robert.vesta.service.impl.provider.PropertyMachineIdProvider;
import com.robert.vesta.service.intf.IdService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.jdbc.core.JdbcTemplate;

import java.beans.PropertyVetoException;

public class IdServiceFactoryBean implements FactoryBean<IdService> {
    protected final Logger log = LoggerFactory
            .getLogger(IdServiceFactoryBean.class);

    public enum Type {
        PROPERTY, IP_CONFIGURABLE, DB
    }

    private Type providerType;

    private long machineId;

    private String ips;

    private String dbUrl;
    private String dbName;
    private String dbUser;
    private String dbPassword;

    private long genMethod = -1;
    private long type = -1;
    private long version = -1;

    private IdService idService;
    // 注意这里的 init 方法是由 spring 配置文件指定的，在初始化 IdServiceFactoryBean 实例时会调用此方法，另外在配置文件中还对当前实例的一些字段值进行了设置
    public void init() {    // 该方法主要是根据 providerType 构建 IdServiceImpl，完成其 初始化，主要是缓存 id 的元数据信息（不同号段长度），id 转换器，timer，初始化 timer（验证当前时间是否过期，打印时间相关日志），获取 machine id，并进行验证，初始化 populator，和并发操作的锁类型有关
        if (providerType == null) {
            log.error("The type of Id service is mandatory.");
            throw new IllegalArgumentException(
                    "The type of Id service is mandatory.");
        }

        switch (providerType) {
            case PROPERTY:  // 根据 type 构建 IdServiceImpl，完成其 初始化，主要是缓存 id 的元数据信息（不同号段长度），id 转换器，timer，初始化 timer（验证当前时间是否过期，打印时间相关日志），获取 machine id，并进行验证，初始化 populator，和并发操作的锁类型有关
                idService = constructPropertyIdService(machineId);
                break;
            case IP_CONFIGURABLE:
                idService = constructIpConfigurableIdService(ips);
                break;
            case DB:    // 主要是构建了 dbMachineIdProvider，创建 idServiceImpl，缓存 id 的元数据信息（不同号段长度），id 转换器，timer，初始化 timer（验证当前时间是否过期，打印时间相关日志），获取 machine id，并进行验证，初始化 populator，和并发操作的锁类型有关
                idService = constructDbIdService(dbUrl, dbName, dbUser, dbPassword);
                break;
        }
    }

    public IdService getObject() throws Exception {
        return idService;
    }
    // 根据 type 构建 IdServiceImpl，完成其 初始化，主要是缓存 id 的元数据信息（不同号段长度），id 转换器，timer，初始化 timer（验证当前时间是否过期，打印时间相关日志），获取 machine id，并进行验证，初始化 populator，和并发操作的锁类型有关
    private IdService constructPropertyIdService(long machineId) {
        log.info("Construct Property IdService machineId {}", machineId);
        // PropertyMachineIdProvider 好像仅仅是一个 machine id holder
        PropertyMachineIdProvider propertyMachineIdProvider = new PropertyMachineIdProvider();
        propertyMachineIdProvider.setMachineId(machineId);

        IdServiceImpl idServiceImpl;
        if (type != -1)
            idServiceImpl = new IdServiceImpl(type);    // 初始化了 id type，1 代表颗粒度为毫秒，0 代表颗粒度为秒，2 代表短 id
        else
            idServiceImpl = new IdServiceImpl();    // 默认颗粒度为秒级别

        idServiceImpl.setMachineIdProvider(propertyMachineIdProvider);  // 设置 machine id
        if (genMethod != -1)
            idServiceImpl.setGenMethod(genMethod);  // 设置 id 的生成方式
        if (version != -1)
            idServiceImpl.setVersion(version);  // 设置 id 的版本号
        idServiceImpl.init();   // 缓存 id 的元数据信息（不同号段长度），id 转换器，timer，初始化 timer（验证当前时间是否过期，打印时间相关日志），获取 machine id，并进行验证，初始化 populator，和并发操作的锁类型有关

        return idServiceImpl;
    }

    private IdService constructIpConfigurableIdService(String ips) {
        log.info("Construct Ip Configurable IdService ips {}", ips);

        IpConfigurableMachineIdProvider ipConfigurableMachineIdProvider = new IpConfigurableMachineIdProvider(
                ips);

        IdServiceImpl idServiceImpl;
        if (type != -1)
            idServiceImpl = new IdServiceImpl(type);
        else
            idServiceImpl = new IdServiceImpl();

        idServiceImpl.setMachineIdProvider(ipConfigurableMachineIdProvider);
        if (genMethod != -1)
            idServiceImpl.setGenMethod(genMethod);
        if (version != -1)
            idServiceImpl.setVersion(version);
        idServiceImpl.init();

        return idServiceImpl;
    }
    // 主要是构建了 dbMachineIdProvider，创建 idServiceImpl，缓存 id 的元数据信息（不同号段长度），id 转换器，timer，初始化 timer（验证当前时间是否过期，打印时间相关日志），获取 machine id，并进行验证，初始化 populator，和并发操作的锁类型有关
    private IdService constructDbIdService(String dbUrl, String dbName,
                                           String dbUser, String dbPassword) {
        log.info(
                "Construct Db IdService dbUrl {} dbName {} dbUser {} dbPassword {}",
                dbUrl, dbName, dbUser, dbPassword);
        // 以下均是与数据库连接相关的代码
        ComboPooledDataSource comboPooledDataSource = new ComboPooledDataSource();

        String jdbcDriver = "com.mysql.jdbc.Driver";
        try {
            comboPooledDataSource.setDriverClass(jdbcDriver);
        } catch (PropertyVetoException e) {
            log.error("Wrong JDBC driver {}", jdbcDriver);
            log.error("Wrong JDBC driver error: ", e);
            throw new IllegalStateException("Wrong JDBC driver ", e);
        }

        comboPooledDataSource.setMinPoolSize(5);
        comboPooledDataSource.setMaxPoolSize(30);
        comboPooledDataSource.setIdleConnectionTestPeriod(20);
        comboPooledDataSource.setMaxIdleTime(25);
        comboPooledDataSource.setBreakAfterAcquireFailure(false);
        comboPooledDataSource.setCheckoutTimeout(3000);
        comboPooledDataSource.setAcquireRetryAttempts(50);
        comboPooledDataSource.setAcquireRetryDelay(1000);

        String url = String
                .format("jdbc:mysql://%s/%s?useUnicode=true&amp;characterEncoding=UTF-8&amp;autoReconnect=true",
                        dbUrl, dbName);

        comboPooledDataSource.setJdbcUrl(url);
        comboPooledDataSource.setUser(dbUser);
        comboPooledDataSource.setPassword(dbPassword);

        JdbcTemplate jdbcTemplate = new JdbcTemplate();
        jdbcTemplate.setLazyInit(false);
        jdbcTemplate.setDataSource(comboPooledDataSource);
        // 创建一个 DbMachineIdProvider，它需要持有 jdbcTemplate
        DbMachineIdProvider dbMachineIdProvider = new DbMachineIdProvider();
        dbMachineIdProvider.setJdbcTemplate(jdbcTemplate);  // DbMachineIdProvider 持有 jdbcTemplate
        dbMachineIdProvider.init(); // 主要就是获取 machine id，优先从数据库中查询，如果没有，就占用数据库中的一个坑，然后对应的 id 就是自身的 machine id

        IdServiceImpl idServiceImpl;
        if (type != -1)
            idServiceImpl = new IdServiceImpl(type);    // 初始化了 id type，1 代表颗粒度为毫秒，0 代表颗粒度为秒，2 代表短 id
        else
            idServiceImpl = new IdServiceImpl();    // 初始化了 id type，1 代表颗粒度为毫秒，0 代表颗粒度为秒，2 代表短 id

        idServiceImpl.setMachineIdProvider(dbMachineIdProvider);    // idServiceImpl 持有 id provider
        if (genMethod != -1)
            idServiceImpl.setGenMethod(genMethod);  // 指定生成方式
        if (version != -1)
            idServiceImpl.setVersion(version);  // 啥版本嗯？？
        idServiceImpl.init();   // 缓存 id 的元数据信息（不同号段长度），id 转换器，timer，初始化 timer（验证当前时间是否过期，打印时间相关日志），获取 machine id，并进行验证，初始化 populator，和并发操作的锁类型有关

        return idServiceImpl;
    }

    public Class<?> getObjectType() {
        return IdService.class;
    }

    public boolean isSingleton() {
        return true;
    }

    public Type getProviderType() {
        return providerType;
    }

    public void setProviderType(Type providerType) {
        this.providerType = providerType;
    }

    public long getMachineId() {
        return machineId;
    }

    public void setMachineId(long machineId) {
        this.machineId = machineId;
    }

    public String getIps() {
        return ips;
    }

    public void setIps(String ips) {
        this.ips = ips;
    }

    public String getDbUrl() {
        return dbUrl;
    }

    public void setDbUrl(String dbUrl) {
        this.dbUrl = dbUrl;
    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public String getDbUser() {
        return dbUser;
    }

    public void setDbUser(String dbUser) {
        this.dbUser = dbUser;
    }

    public String getDbPassword() {
        return dbPassword;
    }

    public void setDbPassword(String dbPassword) {
        this.dbPassword = dbPassword;
    }

    public long getGenMethod() {
        return genMethod;
    }

    public void setGenMethod(long genMethod) {
        this.genMethod = genMethod;
    }

    public long getType() {
        return type;
    }

    public void setType(long type) {
        this.type = type;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }
}
