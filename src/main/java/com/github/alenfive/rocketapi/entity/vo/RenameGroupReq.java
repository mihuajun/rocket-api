package com.github.alenfive.rocketapi.entity.vo;

import lombok.Data;

/**
 * API组名修改入参
 */
@Data
public class RenameGroupReq {
    private String newGroupName;
    private String oldGroupName;
}
