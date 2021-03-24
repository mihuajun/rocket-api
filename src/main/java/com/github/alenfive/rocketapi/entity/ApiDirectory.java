package com.github.alenfive.rocketapi.entity;

import com.github.alenfive.rocketapi.annotation.ApiTable;
import com.github.alenfive.rocketapi.annotation.ApiUpdateField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiTable("api_directory")
public class ApiDirectory extends ApiEntity{

    @ApiUpdateField
    private String service;

    @ApiUpdateField
    private String name;

    @ApiUpdateField
    private String path;

    @ApiUpdateField
    private String parentId;
}
