package com.mxny.ss.netty.commons.dto;

import java.time.LocalDateTime;

/**
 * 测试消息体
 */
public class MsgBody {

    private Long id;
    private String name;
    private LocalDateTime createTime = LocalDateTime.now();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return "MsgBody [id=" + id + ", name=" + name + ", createTime=" + createTime + "]";
    }
}
