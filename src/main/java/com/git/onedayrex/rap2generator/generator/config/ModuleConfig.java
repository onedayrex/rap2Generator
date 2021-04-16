package com.git.onedayrex.rap2generator.generator.config;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ModuleConfig extends GlobalParseConfig{
    /**
     * 仓库id
     */
    private Integer repositoryId;

    /**
     * 模块id
     */
    private Integer mod;

    /**
     * 解析java类包名
     */
    private String packageName;

    
}


