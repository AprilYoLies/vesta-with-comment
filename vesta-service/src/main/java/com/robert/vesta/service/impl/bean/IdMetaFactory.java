package com.robert.vesta.service.impl.bean;

public class IdMetaFactory {
    // byte machineBits, byte seqBits, byte timeBits, byte genMethodBits, byte typeBits, byte versionBits
    //                                          机器号        序列号    时间      生成方式    类型        版本号
    private static IdMeta maxPeak = new IdMeta((byte) 10, (byte) 20, (byte) 30, (byte) 1, (byte) 1, (byte) 1);
    //                                          机器号              序列号    时间      生成方式    类型        版本号
    private static IdMeta minGranularity = new IdMeta((byte) 10, (byte) 10, (byte) 40, (byte) 1, (byte) 1, (byte) 1);
    //                                          机器号        序列号    时间      生成方式    类型        版本号
    private static IdMeta shortId = new IdMeta((byte) 10, (byte) 10, (byte) 30, (byte) 1, (byte) 1, (byte) 1);
    // 可以看出来，该方法仅仅是为了根据 id type 得到对应生成 id 不同号段的位置信息
    public static IdMeta getIdMeta(IdType type) {
        if (IdType.SECONDS.equals(type)) {
            return maxPeak; // 如果是秒级别的 id 生成方式，元信息就是 maxPeak
        } else if (IdType.MILLISECONDS.equals(type)) {
            return minGranularity;  // 如果是秒级别的 id 生成方式，元信息就是 minGranularity
        } else if (IdType.SHORTID.equals(type)) {
            return shortId;
        }
        return null;
    }
}
