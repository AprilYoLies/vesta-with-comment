package com.robert.vesta.service.impl.converter;

import com.robert.vesta.service.bean.Id;
import com.robert.vesta.service.impl.bean.IdMeta;

public class IdConverterImpl implements IdConverter {

    public IdConverterImpl() {
    }
    // id 实例对象根据 idMeta 信息构建出真正的 id
    public long convert(Id id, IdMeta idMeta) {
        return doConvert(id, idMeta);
    }
    // id 实例对象根据 idMeta 信息构建出真正的 id
    protected long doConvert(Id id, IdMeta idMeta) {
        long ret = 0;

        ret |= id.getMachine();

        ret |= id.getSeq() << idMeta.getSeqBitsStartPos();

        ret |= id.getTime() << idMeta.getTimeBitsStartPos();

        ret |= id.getGenMethod() << idMeta.getGenMethodBitsStartPos();

        ret |= id.getType() << idMeta.getTypeBitsStartPos();

        ret |= id.getVersion() << idMeta.getVersionBitsStartPos();

        return ret;
    }
    // 用于将 id 反解为 id 实例对象
    public Id convert(long id, IdMeta idMeta) {
        return doConvert(id, idMeta);
    }
    // 用于将 id 反解为 id 实例对象
    protected Id doConvert(long id, IdMeta idMeta) {
        Id ret = new Id();

        ret.setMachine(id & idMeta.getMachineBitsMask());

        ret.setSeq((id >>> idMeta.getSeqBitsStartPos()) & idMeta.getSeqBitsMask());

        ret.setTime((id >>> idMeta.getTimeBitsStartPos()) & idMeta.getTimeBitsMask());

        ret.setGenMethod((id >>> idMeta.getGenMethodBitsStartPos()) & idMeta.getGenMethodBitsMask());

        ret.setType((id >>> idMeta.getTypeBitsStartPos()) & idMeta.getTypeBitsMask());

        ret.setVersion((id >>> idMeta.getVersionBitsStartPos()) & idMeta.getVersionBitsMask());

        return ret;
    }

}
