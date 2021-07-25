package com.github.alenfive.rocketapi.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NotifyEntity {
    /**
     * 事件类型
     */
    private NotifyEventType eventType;

    /**
     * eventType=RefreshMapping时存在
     */
    private RefreshMapping refreshMapping;
}
