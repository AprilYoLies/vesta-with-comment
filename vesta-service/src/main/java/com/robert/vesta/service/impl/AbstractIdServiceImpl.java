package com.robert.vesta.service.impl;

import com.robert.vesta.service.bean.Id;
import com.robert.vesta.service.impl.bean.IdMeta;
import com.robert.vesta.service.impl.bean.IdMetaFactory;
import com.robert.vesta.service.impl.bean.IdType;
import com.robert.vesta.service.impl.converter.IdConverter;
import com.robert.vesta.service.impl.converter.IdConverterImpl;
import com.robert.vesta.service.impl.provider.MachineIdProvider;
import com.robert.vesta.service.impl.timer.SimpleTimer;
import com.robert.vesta.service.impl.timer.Timer;
import com.robert.vesta.service.intf.IdService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

public abstract class AbstractIdServiceImpl implements IdService {

    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    protected long machineId = -1;
    protected long genMethod = 0;
    protected long version = 0;

    protected IdType idType;
    protected IdMeta idMeta;

    protected IdConverter idConverter;

    protected MachineIdProvider machineIdProvider;

    protected Timer timer;

    public AbstractIdServiceImpl() {    // 默认颗粒度为秒级别
        idType = IdType.SECONDS;
    }

    public AbstractIdServiceImpl(String type) {
        idType = IdType.parse(type);
    }

    public AbstractIdServiceImpl(long type) {
        idType = IdType.parse(type);    // 将 type 号解析为 IdType，1 代表颗粒度为毫秒，0 代表颗粒度为秒，2 代表短 id
    }

    public AbstractIdServiceImpl(IdType type) {
        idType = type;
    }
    // 缓存 id 的元数据信息（不同号段长度），id 转换器，timer，初始化 timer（验证当前时间是否过期，打印时间相关日志），获取 machine id，并进行验证
    public void init() {
        if (this.idMeta == null) {
            setIdMeta(IdMetaFactory.getIdMeta(idType)); // 可以看出来，该方法仅仅是为了根据 id type 得到对应生成 id 不同号段的位置信息
        }
        if (this.idConverter == null) {
            setIdConverter(new IdConverterImpl());  // 设置 id 转换器，算是附加性的功能
        }
        if (this.timer == null) {
            setTimer(new SimpleTimer());    // 仅仅是设置了一个定时器
        }
        this.timer.init(idMeta, idType);    // 初始化 timer，缓存 idMeta、maxTime（从 EPOCH 开始，时间位所能记录的最大时刻数）、idType，验证时间是否会导致重复，打印关于时间的日志

        this.machineId = machineIdProvider.getMachineId();  // 从 id provider 中得到 machine id
        validateMachineId(this.machineId);  // 验证 machine id 是否正确，0 - 1023 之间，供 1024 个
    }

    public long genId() {
        Id id = new Id();   // 用于承载生成的 id
        // 设置对应的参数
        id.setMachine(machineId);
        id.setGenMethod(genMethod);
        id.setType(idType.value());
        id.setVersion(version);

        populateId(id); // 用于向 id 填充 sequence 和时间戳
        // id 实例对象根据 idMeta 信息构建出真正的 id
        long ret = idConverter.convert(id, this.idMeta);

        // Use trace because it cause low performance
        if (log.isTraceEnabled())
            log.trace(String.format("Id: %s => %d", id, ret));

        return ret;
    }
    // 验证 machine id 是否正确，0 - 1023 之间，供 1024 个
    public void validateMachineId(long machineId){
        if (machineId < 0) {
            log.error("The machine ID is not configured properly (" + machineId + " < 0) so that Vesta Service refuses to start.");

            throw new IllegalStateException(
                    "The machine ID is not configured properly (" + machineId + " < 0) so that Vesta Service refuses to start.");

        } else if (machineId >= (1 << this.idMeta.getMachineBits())) {  // 获取 id 的机器号段长度
            log.error("The machine ID is not configured properly ("
                    + machineId + " >= " + (1 << this.idMeta.getMachineBits()) + ") so that Vesta Service refuses to start.");

            throw new IllegalStateException("The machine ID is not configured properly ("
                    + machineId + " >= " + (1 << this.idMeta.getMachineBits()) + ") so that Vesta Service refuses to start.");

        }
    }

    protected abstract void populateId(Id id);

    public Date transTime(final long time) {
        return timer.transTime(time);
    }


    public Id expId(long id) {
        return idConverter.convert(id, this.idMeta);
    }

    public long makeId(long time, long seq) {
        return makeId(time, seq, machineId);
    }

    public long makeId(long time, long seq, long machine) {
        return makeId(genMethod, time, seq, machine);
    }

    public long makeId(long genMethod, long time, long seq, long machine) {
        return makeId(idType.value(), genMethod, time, seq, machine);
    }

    public long makeId(long type, long genMethod, long time,
                       long seq, long machine) {
        return makeId(version, type, genMethod, time, seq, machine);
    }

    public long makeId(long version, long type, long genMethod,
                       long time, long seq, long machine) {
        Id id = new Id(machine, seq, time, genMethod, type, version);
        return idConverter.convert(id, this.idMeta);
    }

    public void setMachineId(long machineId) {
        this.machineId = machineId;
    }

    public void setGenMethod(long genMethod) {
        this.genMethod = genMethod;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public void setIdConverter(IdConverter idConverter) {
        this.idConverter = idConverter;
    }

    public void setIdMeta(IdMeta idMeta) {
        this.idMeta = idMeta;
    }
    // 设置 machine id
    public void setMachineIdProvider(MachineIdProvider machineIdProvider) {
        this.machineIdProvider = machineIdProvider;
    }

    public void setTimer(Timer timer) {
        this.timer = timer;
    }
}