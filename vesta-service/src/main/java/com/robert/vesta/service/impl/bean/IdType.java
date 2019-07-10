package com.robert.vesta.service.impl.bean;

public enum IdType {
    SECONDS("seconds"), MILLISECONDS("milliseconds"), SHORTID("short_id");

    private String name;

    private IdType(String name) {
        this.name = name;
    }
    // 根据 id 类型获取对应的唯一 id 号
    public long value() {
        switch (this) {
            case SECONDS:
                return 0;
            case MILLISECONDS:
                return 1;
            case SHORTID:
                return 2;
            default:
                return 0;
        }
    }

    @Override
    public String toString() {
        return this.name;
    }

    public static IdType parse(String name) {
        if ("seconds".equals(name)) {
            return SECONDS;
        } else if ("milliseconds".equals(name)) {
            return MILLISECONDS;
        } else if ("short_id".equals(name)) {
            return SHORTID;
        }
        throw new IllegalArgumentException("Illegal IdType name <[" + name
                + "]>, available names are seconds and milliseconds");
    }
    // 将 type 号解析为 IdType，1 代表颗粒度为毫秒，0 代表颗粒度为秒，2 代表短 id
    public static IdType parse(long type) {
        if (type == 1) {
            return MILLISECONDS;
        } else if (type == 0) {
            return SECONDS;
        }
        else if(type==2){
            return SHORTID;
        }

        throw new IllegalArgumentException("Illegal IdType value <[" + type
                + "]>, available values are 0 (for seconds) and 1 (for milliseconds)");
    }
}
