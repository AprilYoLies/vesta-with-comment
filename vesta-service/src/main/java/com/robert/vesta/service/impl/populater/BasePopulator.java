package com.robert.vesta.service.impl.populater;

import com.robert.vesta.service.bean.Id;
import com.robert.vesta.service.impl.bean.IdMeta;
import com.robert.vesta.service.impl.timer.Timer;

public abstract class BasePopulator implements IdPopulator, ResetPopulator {
    protected long sequence = 0;
    protected long lastTimestamp = -1;

    public BasePopulator() {
        super();
    }
    // 用于向 id 填充 sequence 和时间戳
    public void populateId(Timer timer, Id id, IdMeta idMeta) {
        long timestamp = timer.genTime();   // 计算相对于 EPOCH 时间点的，已经流逝的毫秒或者秒时间（需要验证是否已经超过了最大时间）
        timer.validateTimestamp(lastTimestamp, timestamp);  // 验证时间是否发生了回拨，如果是，日志记录并抛出异常

        if (timestamp == lastTimestamp) {   // 如果时间戳未发生变化，这时需要变更序列号来保证 id 的唯一性
            sequence++;
            sequence &= idMeta.getSeqBitsMask();
            if (sequence == 0) {    // 如果序列号超出了上限
                timestamp = timer.tillNextTimeUnit(lastTimestamp);  // 线程自旋到下一个最小时间单元
            }
        } else {
            lastTimestamp = timestamp;
            sequence = 0;   // sequence 清零
        }
        // 将 sequence 和时间戳填充到 id 中
        id.setSeq(sequence);
        id.setTime(timestamp);
    }

    public void reset() {
        this.sequence = 0;
        this.lastTimestamp = -1;
    }
}
