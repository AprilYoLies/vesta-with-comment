package com.robert.vesta.service.impl;

import com.robert.vesta.service.bean.Id;
import com.robert.vesta.service.impl.bean.IdType;
import com.robert.vesta.service.impl.populater.AtomicIdPopulator;
import com.robert.vesta.service.impl.populater.IdPopulator;
import com.robert.vesta.service.impl.populater.LockIdPopulator;
import com.robert.vesta.service.impl.populater.SyncIdPopulator;
import com.robert.vesta.util.CommonUtils;

public class IdServiceImpl extends AbstractIdServiceImpl {

    private static final String SYNC_LOCK_IMPL_KEY = "vesta.sync.lock.impl.key";

    private static final String ATOMIC_IMPL_KEY = "vesta.atomic.impl.key";

    protected IdPopulator idPopulator;

    public IdServiceImpl() {
        super();
    }

    public IdServiceImpl(String type) {
        super(type);
    }

    public IdServiceImpl(long type) {
        super(type);    // 初始化了 id type，1 代表颗粒度为毫秒，0 代表颗粒度为秒，2 代表短 id
    }

    public IdServiceImpl(IdType type) {
        super(type);
    }

    @Override    // 缓存 id 的元数据信息（不同号段长度），id 转换器，timer，初始化 timer（验证当前时间是否过期，打印时间相关日志），获取 machine id，并进行验证，初始化 populator，和并发操作的锁类型有关
    public void init() {
        super.init();   // 缓存 id 的元数据信息（不同号段长度），id 转换器，timer，初始化 timer（验证当前时间是否过期，打印时间相关日志），获取 machine id，并进行验证
        initPopulator();    // 初始化 populator，和并发操作的锁类型有关
    }
    // 初始化 populator，和并发操作的锁类型有关
    public void initPopulator() {
        if (idPopulator != null){
            log.info("The " + idPopulator.getClass().getCanonicalName() + " is used.");
        } else if (CommonUtils.isPropKeyOn(SYNC_LOCK_IMPL_KEY)) {   // 判断是否是开启状态 "ON", "TRUE", "on", "true" 四种之一即可
            log.info("The SyncIdPopulator is used.");
            idPopulator = new SyncIdPopulator();
        } else if (CommonUtils.isPropKeyOn(ATOMIC_IMPL_KEY)) {  // 判断是否是开启状态 "ON", "TRUE", "on", "true" 四种之一即可
            log.info("The AtomicIdPopulator is used.");
            idPopulator = new AtomicIdPopulator();
        } else {
            log.info("The default LockIdPopulator is used.");
            idPopulator = new LockIdPopulator();    // 构建了一个 LockIdPopulator
        }
    }
    // 用于向 id 填充 sequence 和时间戳
    protected void populateId(Id id) {
        idPopulator.populateId(timer, id, idMeta);
    }

    public void setIdPopulator(IdPopulator idPopulator) {
        this.idPopulator = idPopulator;
    }
}
