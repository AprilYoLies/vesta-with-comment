package com.robert.vesta.service.impl.populater;

import com.robert.vesta.service.bean.Id;
import com.robert.vesta.service.impl.bean.IdMeta;
import com.robert.vesta.service.impl.timer.Timer;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class LockIdPopulator extends BasePopulator {

    private Lock lock = new ReentrantLock();

    public LockIdPopulator() {
        super();
    }

    public void populateId(Timer timer, Id id, IdMeta idMeta) {
        lock.lock();
        try {   // 用于向 id 填充 sequence 和时间戳
            super.populateId(timer, id, idMeta);
        } finally {
            lock.unlock();
        }
    }

}
