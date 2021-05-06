package com.github.alenfive.rocketapi.entity.vo;

import com.github.alenfive.rocketapi.entity.ApiDirectory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocsInfo {
    private List<ApiDirectory> directoryList;
    private List<DocApi> docApiList;
}
