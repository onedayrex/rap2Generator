//package com.git.onedayrex.rap2generator.generator.util;
//
//import com.alibaba.fastjson.JSONObject;
//import com.git.onedayrex.rap2generator.generator.config.GlobalParseConfig;
//import com.git.onedayrex.rap2generator.generator.config.ModuleConfig;
//import com.git.onedayrex.rap2generator.generator.config.ParseConfig;
//import org.codehaus.groovy.ast.tools.BeanUtils;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.InputStreamReader;
//import java.util.stream.Collectors;
//
///**
// * <p class="detail">
// * 功能:配置json解析类
// * </p>
// *
// * @author Kings
// * @version V1.0
// * @date 2019.11.02
// */
//public class ParseConfigJsonUtil {
//    private static final Logger LOGGER = LoggerFactory.getLogger(ParseConfigJsonUtil.class);
//
//    /**
//     * <p class="detail">
//     * 功能:解析
//     * </p>
//     *
//     * @param fileName :解析文件路径
//     * @return parse config
//     * @throws IOException the io exception
//     * @author Kings
//     * @date 2019.11.02
//     */
//    public static ParseConfig parseByJsonFile(String fileName) throws IOException {
//        ParseConfig resultConfig = new ParseConfig();
//
//        fileName = fileName.startsWith("/") ? fileName : "/" + fileName;
//        InputStream inputStream = ParseConfigJsonUtil.class.getResourceAsStream(fileName);
//        String jsonString = new BufferedReader(new InputStreamReader(inputStream)).lines().parallel().collect(Collectors.joining(System.lineSeparator()));
//        JSONObject jsonObject = JSONObject.parseObject(jsonString);
//        ParseConfig parseConfig = jsonObject.toJavaObject(ParseConfig.class);
//
//        //自定义模板common配置覆盖
//        String moduleConfigPath = parseConfig.getModuleConfigPath();
//        moduleConfigPath = moduleConfigPath.startsWith("/") ? moduleConfigPath : "/" + moduleConfigPath;
//        InputStream inputStreamModuleConfig = ParseConfigJsonUtil.class.getResourceAsStream(moduleConfigPath);
//        if (inputStreamModuleConfig == null) {
//            LOGGER.error("【error】未设置自定义模块解析模板路径...请在模板中设置customeCommonParseConfigPath属性");
//            return null;
//        } else {
//            String jsonStringModuleConfig = new BufferedReader(new InputStreamReader(inputStreamModuleConfig)).lines().parallel().collect(Collectors.joining(System.lineSeparator()));
//            JSONObject jsonObjectModuleConfig = JSONObject.parseObject(jsonStringModuleConfig);
//            ModuleConfig moduleConfig = jsonObjectModuleConfig.toJavaObject(ModuleConfig.class);
//            (moduleConfig, parseConfig);
//        }
//
//
//
//        //全局配置
//        InputStream inputStreamGlobalCfg = ParseConfigJsonUtil.class.getResourceAsStream("/globalConfig.json");
//        if (inputStreamGlobalCfg == null) {
//            LOGGER.error("【error】请在resource目录下定义globalConfig.json");
//            return null;
//        } else {
//            String jsonStringGlobal = new BufferedReader(new InputStreamReader(inputStreamGlobalCfg)).lines().parallel().collect(Collectors.joining(System.lineSeparator()));
//            JSONObject jsonObjectGlobal = JSONObject.parseObject(jsonStringGlobal);
//            GlobalParseConfig globalParseConfig = jsonObjectGlobal.toJavaObject(GlobalParseConfig.class);
//            BeanUtils.copyProperties(globalParseConfig, parseConfig);
//        }
//
//        return parseConfig;
//    }
//
//}
//
//
