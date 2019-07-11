package com.robert.vesta.service.impl.provider;

import com.robert.vesta.util.IpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.StringUtils;

public class DbMachineIdProvider implements MachineIdProvider {
    private static final Logger log = LoggerFactory
            .getLogger(DbMachineIdProvider.class);

    private long machineId;

    private JdbcTemplate jdbcTemplate;

    public DbMachineIdProvider() {
        log.debug("IpConfigurableMachineIdProvider constructed.");
    }
    // 主要就是获取 machine id，优先从数据库中查询，如果没有，就占用数据库中的一个坑，然后对应的 id 就是自身的 machine id
    public void init() {
        String ip = IpUtils.getHostIp();    // 获取公网地址

        if (StringUtils.isEmpty(ip)) {  // 如果没有公网地址，直接抛出一样
            String msg = "Fail to get host IP address. Stop to initialize the DbMachineIdProvider provider.";

            log.error(msg);
            throw new IllegalStateException(msg);
        }

        Long id = null;
        try {
            id = jdbcTemplate.queryForObject(   // 从数据库中查询当前机器的 id
                    "select ID from DB_MACHINE_ID_PROVIDER where IP = ?",
                    new Object[]{ip}, Long.class);

        } catch (EmptyResultDataAccessException e) {
            // Ignore the exception
            log.error("No allocation before for ip {}.", ip);
        }

        if (id != null) {
            machineId = id;
            return;
        }

        log.info(   // 执行到这里，说明从数据库中获取 ip 失败
                "Fail to get ID from DB for host IP address {}. Next step try to allocate one.",
                ip);
        // 占用数据库中的一条记录，将 ip 设置为自己的
        int count = jdbcTemplate
                .update("update DB_MACHINE_ID_PROVIDER set IP = ? where IP is null limit 1",
                        ip);

        if (count <= 0 || count > 1) {  // 上一条依据应该只会更新一条才对，这里只是为了增强代码的健壮性
            String msg = String
                    .format("Fail to allocte ID for host IP address {}. The {} records are updated. Stop to initialize the DbMachineIdProvider provider.",
                            ip, count);

            log.error(msg);
            throw new IllegalStateException(msg);
        }

        try {   // 获取被更新的 ip
            id = jdbcTemplate.queryForObject(
                    "select ID from DB_MACHINE_ID_PROVIDER where IP = ?",
                    new Object[]{ip}, Long.class);

        } catch (EmptyResultDataAccessException e) {
            // Ignore the exception
            log.error("Fail to do allocation for ip {}.", ip);
        }

        if (id == null) {
            String msg = String
                    .format("Fail to get ID from DB for host IP address {} after allocation. Stop to initialize the DbMachineIdProvider provider.",
                            ip);

            log.error(msg);
            throw new IllegalStateException(msg);
        }

        machineId = id; // 获取 machine id
    }

    public long getMachineId() {
        return machineId;
    }

    public void setMachineId(long machineId) {
        this.machineId = machineId;
    }

    public JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
}
