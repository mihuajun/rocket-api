package com.github.alenfive.dataway2.entity.vo;

import lombok.Data;

/**
 * @Description:
 * @Copyright: Copyright (c) 2019  ALL RIGHTS RESERVED.
 * @Author: 米华军
 * @CreateDate: 2020/6/11 17:44
 * @UpdateDate: 2020/6/11 17:44
 * @UpdateRemark: init
 * @Version: 1.0
 * @menu 组名修改入参
 */
@Data
public class RenameGroupReq {
    private String newGroup;
    private String oldGroup;
}
