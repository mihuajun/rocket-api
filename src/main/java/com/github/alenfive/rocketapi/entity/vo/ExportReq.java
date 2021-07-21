package com.github.alenfive.rocketapi.entity.vo;

import lombok.Data;


@Data
public class ExportReq {
    private String token;
    private String fileName;
    private String apiInfoIds;
}
