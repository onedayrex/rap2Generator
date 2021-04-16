package com.git.onedayrex.rap2generator.generator.core;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.git.onedayrex.rap2generator.generator.config.*;
import com.git.onedayrex.rap2generator.generator.model.Rap2Response;
import com.git.onedayrex.rap2generator.generator.util.HttpUtil;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiPrimitiveType;
import com.intellij.psi.PsiType;
import com.intellij.psi.javadoc.PsiDocComment;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p class="detail">
 * 功能:核心rap generator类
 * </p>
 *
 * @author Kings
 * @version V1.0
 * @date 2019.11.02
 */
public class Rap2Generator {
    private static final Logger LOGGER = LoggerFactory.getLogger(Rap2Generator.class);
    private static final String TARGET_URL = "/properties/update?itf=%d";
    private static final String INFO_URL = "/interface/get?id=%d";
    private static final String ANNOTATION_EXP = "(\\s*|\\t*)\\*(\\s*|\\t*)((?!\\s+|\\s?/).*)|(\\s*|\\t*)/\\**(\\s*|\\t*)(.*)\\*/|(\\s*|\\t*)(//\\s*|//\\t*)(.*)";
    private static final String FIELD_EXP = "(\\s*|\\t*)(private|protected|public)\\s+(.*)\\s+(\\w+);$";
    private static final String TYPE_NUMBER_EXP = "Integer|int|Short|short|Byte|byte|Long|long|BigDecimal|Float|float|Double|double|Character|char|BigInteger";
    private static final String TYPE_STRING_EXP = "String|Date|LocalDate|LocalDateTime|TimeStamp";
    private static final String TYPE_BOOLEAN_EXP = "Boolean|boolean";
    
    private static final String BEGIN_PARSE_CLASS_EXP = "(\\s*|\\t*)public(\\s+abstract)?\\s+class\\s+(\\w+(<\\w+>)?)(\\s+extends\\s+(\\w+)(<\\w+>)?)?(\\s+implements\\s+Serializable)?\\s*\\{";
    private static final String MAP_PROPERTY_EXP = "^(Map|HashMap|LinkedHashMap|TreeMap|SortedMap|Hashtable)(\\s*|\\t*)(<(.*)>)?$";
    
    //KingsBankCard[]
    private static final String PARSE_ARRAY_TYPE_EXP = "(.*)\\[]";
    //List<KingsHobby>
    private static final String PARSE_LIST_TYPE_EXP = "^(List|ArrayList|LinkedList|Set|SortedSet|HashSet|TreeSet)(\\s*|\\t*)(<(.*)>)?$";
    private static final String TYPE_ARRAY_EXP = PARSE_LIST_TYPE_EXP + "|(.*)\\[]" + "|" + MAP_PROPERTY_EXP;
    
    private static final String RESPONSE = "response";
    private static final String REQUEST = "request";
    private static Pattern PATTERN_ANNOTATION = Pattern.compile(ANNOTATION_EXP);
    private static Pattern PATTERN_FIELD = Pattern.compile(FIELD_EXP);
    private static Pattern PATTERN_TYPE_NUMBER = Pattern.compile(TYPE_NUMBER_EXP);
    private static Pattern PATTERN_TYPE_STRING = Pattern.compile(TYPE_STRING_EXP);
    private static Pattern PATTERN_TYPE_BOOLEAN = Pattern.compile(TYPE_BOOLEAN_EXP);
    private static Pattern PATTERN_TYPE_ARRAY = Pattern.compile(TYPE_ARRAY_EXP);
    private static Pattern PATTERN_PARSE_ARRAY_TYPE = Pattern.compile(PARSE_ARRAY_TYPE_EXP);
    private static Pattern PATTERN_PARSE_LIST_TYPE = Pattern.compile(PARSE_LIST_TYPE_EXP);
    private static Pattern PATTERN_BEGIN_PARSE_CLASS = Pattern.compile(BEGIN_PARSE_CLASS_EXP);
    private static Pattern PATTERN_MAP_PROPERTY = Pattern.compile(MAP_PROPERTY_EXP);
    
    //运算中可变
    private static int IDX = 1;
    private ParseWebConfig parseWebConfig;

    private ParseWebConfig getParseWebConfig() {
        return parseWebConfig;
    }

    public void setParseWebConfig(ParseWebConfig parseWebConfig) {
        this.parseWebConfig = parseWebConfig;
    }

    public String generator(JSONArray all) throws IOException {
        //配置开始
        String sid = "YPXvH9ZdQLAFmHtgfkyCwOlZPBzc0rbU";
        String sig = "X79E8kBhBOi9zNBYLGwLhLkuSfg";
        int interfaceId = 161;
        String delosUrl = "";
        final String cookie = "koa.sid=" + sid + ";koa.sid.sig=" + sig;
        final String updateUrl = String.format(delosUrl + TARGET_URL, interfaceId);
        final String infoUrl = String.format(delosUrl + INFO_URL, interfaceId);
//        String infoStr = HttpUtil.get(infoUrl, cookie);
//        JSONObject info = JSON.parseObject(infoStr);
//        if (info != null) {
//            JSONObject data = info.getJSONObject("data");
//            if (data != null) {
//                JSONArray responseProperties = data.getJSONArray("properties");
//                if (responseProperties != null && !responseProperties.isEmpty()) {
//                    responseProperties.addAll(all);
//                    all = responseProperties;
//                }
//            }
//        }
        JSONObject jsonObject = new JSONObject();
        Summary summary = new Summary(Summary.BodyOption.FORM_DATA, Summary.RequestParamsType.QUERY_PARAMS);
        jsonObject.put("properties", new JSONArray());
        jsonObject.put("summary", JSONObject.toJSON(summary));
        //清空历史
//        HttpUtil.post(updateUrl, jsonObject.toString(), cookie);
        jsonObject.put("properties", all);
        String result = HttpUtil.post(updateUrl, jsonObject.toString(), cookie);

        Rap2Response rap2Response = JSONObject.parseObject(result, Rap2Response.class);
        if (rap2Response.getIsOk() != null && !rap2Response.getIsOk()) {
            if ("need login".equals(rap2Response.getErrMsg())) {
                return ("【error】执行错误,cookie中sid和sig已过期，请重新登录拿取覆盖globalConfig.json中的sid和sig配置");
            } else {
                return ("【error】执行错误:" + rap2Response.getErrMsg());
            }
        }
        return "success";
    }

    private JSONArray parseJava(JSONArray jsonArray, String scope, String parentId, String javaDirPath, String className, ResponseResultData responseResultData) throws Exception {
        List<String> annotationList = new ArrayList<>();
        List<String> fieldList = new ArrayList<>();
        List<String> fieldTypeList = new ArrayList<>();
        //时间格式
        Map<String, String> dayFormatMap = new HashMap<>();
        String extendsClass = null;
        int extendParentIdIdx = 0;
        //response为对象进行解析
        if (responseResultData == null || "Object".equals(dealType(responseResultData.getResponseResultDataType().getExp()))) {
            String path = javaDirPath + className + ".java";
            BufferedReader bufferedReader = null;
            try {
                bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(path), StandardCharsets.UTF_8));
            } catch (FileNotFoundException e) {
                LOGGER.error("not find other Object java file,use default");
            }
            String s;
            boolean beginFlag = false;

            //检测字段
            int checkProperty = 1;
            //检测注释
            int checkAnno = 1;

            /*解析开始************************************************************************************/
            while (bufferedReader!=null && (s = bufferedReader.readLine()) != null) {
                Matcher matcherField = PATTERN_FIELD.matcher(s);
                Matcher matcherAnnotation = PATTERN_ANNOTATION.matcher(s);
                Matcher matcherBeginParseClass = PATTERN_BEGIN_PARSE_CLASS.matcher(s);
                //读取到类名开始解析
                if (matcherBeginParseClass.matches()) {
                    beginFlag = true;
                }
                if (beginFlag) {
                    //继承类

                    if (matcherBeginParseClass.matches()) {
                        extendsClass = matcherBeginParseClass.group(6);
                    }

                    //注释部分
                    if (matcherAnnotation.matches()) {
                        String anno;
                        int idx = 3;
                        do {
                            anno = matcherAnnotation.group(idx);
                            idx += 3;
                        } while (idx < 12 && anno == null);
                        annotationList.add(anno);
                        //注释检测
                        checkProperty--;
                        if (checkProperty < 0) {
                            //出现一个字段多个属性,累加属性idx
                            checkAnno++;
                            //出现多个注释需要将起始点-1即从0开始 下一个字段开始匹配到变为1 开始新一轮
                            checkProperty = 0;
                        }
                    }
                    //字段部分
                    if (matcherField.matches()) {
                        String filedType = matcherField.group(3);
                        String fieldName = matcherField.group(4);
                        fieldTypeList.add(filedType);
                        fieldList.add(fieldName);
                        if (judgeDayType(filedType)) {
                            String dayPattern = getDayPattern(javaDirPath,className, fieldName);
                            dayFormatMap.put(fieldName, dayPattern);
                        }
                        //字段检测
                        checkProperty++;
                        if (checkProperty > 1) {
                            //没加注释默认填充""
                            annotationList.add("");
                            LOGGER.error("【warn】类名" + className + "   字段[" + fieldName + "]没有注释");
                            //重置记数
                            checkProperty = 1;
                        }
                        if (checkAnno > 1) {
                            String property = fieldList.get(fieldList.size() - 1);
                            System.out.println("【warn】字段" + property + "有多重注释");

                            //去掉重复的注释并重置次数
                            annotationList.subList(annotationList.size() - checkAnno,annotationList.size()-1).clear();
                            checkAnno = 1;
                        }
                    }
                }
            }
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            /*解析结束************************************************************************************/

            /*组装开始************************************************************************************/
            for (int i = 0; i < fieldList.size(); i++) {
                String fieldString = fieldList.get(i);
                String annotationString = null;
                annotationString = annotationList.get(i);

                String type = fieldTypeList.get(i);
                if (judgeDayType(type)) {
                    String dayPattern = dayFormatMap.get(fieldString);
                    annotationString = annotationString + String.format("[格式:%s]", dayPattern);
                }

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("name", fieldString);
                jsonObject.put("description", annotationString);
                String dealType = dealType(type);
                jsonObject.put("type", dealType);
                String idStr = scope + "-" + IDX;
                jsonObject.put("id", idStr);
                jsonObject.put("parentId", parentId);
                jsonObject.put("scope", scope);
                IDX++;
                //"!Object".equals(type)：字段本来就是Object的不予处理
                if ("Array".equals(dealType) || ("Object".equals(dealType)) && !"Object".equals(type)) {
                    String fileType;
                    Matcher matcherArray = PATTERN_PARSE_ARRAY_TYPE.matcher(type);
                    Matcher matcherList = PATTERN_PARSE_LIST_TYPE.matcher(type);
                    Matcher matcherMap = PATTERN_MAP_PROPERTY.matcher(type);
                    //数组
                    if (matcherArray.matches()) {
                        fileType = matcherArray.group(1);
                        //集合    
                    } else if (matcherList.matches()) {
                        fileType = matcherList.group(4);
                        //对象    
                    } else {
                        fileType = type;
                    }
                    String filedType = dealType(fileType);
                    if (!"Object".equals(filedType) || matcherMap.matches()) {
                        //map
                        if(matcherMap.matches()){
                            String mapGenericity = matcherMap.group(3);
                            String description = mapGenericity == null ? annotationString : String.format(annotationString + "(%s)", mapGenericity);
                            jsonObject.put("description", description);
                        } else {
                            //集合
                            String description = filedType == null ? annotationString : String.format(annotationString + "(%s)", filedType);
                            jsonObject.put("description", description);
                        }
                    }
                    jsonArray.add(jsonObject);
                    //只有对象才会递归解析
                    if ("Object".equals(filedType)) {
                        parseJava(jsonArray, scope, idStr, javaDirPath, fileType, null);
                    }
                } else {
                    jsonArray.add(jsonObject);
                }
            }
            /*组装结束************************************************************************************/
        }
        //编译完删除
        new File(javaDirPath + className + ".class").delete();
        //继承类解析
        if (extendsClass != null) {
            jsonArray = parseJava(jsonArray, scope, parentId, javaDirPath, extendsClass, null);
        }
        return jsonArray;
    }

    /**
     * <p class="detail">
     * 功能:判断字段类型是否为时间类型
     * </p>
     *
     * @param fieldType :
     * @return boolean
     * @author Kings
     * @date 2019.11.02
     */
    private boolean judgeDayType(String fieldType) {
        return "Date".equals(fieldType) | "LocalDate".equals(fieldType) | "LocalDateTime".equals(fieldType);
    }

    private Class getCompileClass(String javaDirPath, String className) throws Exception {
        File f = new File(javaDirPath + className + ".class");
        if(!f.exists()){
            //编译java源代码
            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
            String fileName = javaDirPath + className + ".java";
            Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjects(fileName);
            JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, null, null, null, compilationUnits);
            task.call();
            fileManager.close();
        }
        
        //load into memory and instance
        URL[] urls = new URL[]{new URL("file:" + javaDirPath)};
        URLClassLoader ucl = new URLClassLoader(urls);
        return ucl.loadClass(className);
    }

    /**
     * <p class="detail">
     * 功能:判断字段类型是否为时间类型,若字段写了多个注解则优先级为DateTimeFormat(spring)>JSONField(fastjson)>JsonFormat(jackson),异常就返回""
     * </p>
     *
     * @param className :类名
     * @param fieldName :字段名
     * @return boolean
     * @author Kings
     * @date 2019.11.02
     */
    private String getDayPattern(String javaDirPath,String className, String fieldName) {
        try {
            Class<?> parseClass = getCompileClass(javaDirPath,className);
//            DateTimeFormat dateTimeFormat = parseClass.getDeclaredField(fieldName).getAnnotation(DateTimeFormat.class);
//            if (dateTimeFormat != null) {
//                return dateTimeFormat.pattern();
//            }
            JSONField jsonField = parseClass.getDeclaredField(fieldName).getAnnotation(JSONField.class);
            if (jsonField != null) {
                return jsonField.format();
            }
            JsonFormat jsonFormat = parseClass.getDeclaredField(fieldName).getAnnotation(JsonFormat.class);
            if (jsonFormat != null) {
                return jsonFormat.pattern();
            }
            
            return "";
        } catch (Exception e) {
            return "";
        }
    }

    private String dealType(String fieldType) {
        if(fieldType == null){
            return null;
        }
        String result;
        if (PATTERN_TYPE_NUMBER.matcher(fieldType).matches()) {
            result = "Number";
        } else if (PATTERN_TYPE_STRING.matcher(fieldType).matches()) {
            result = "String";
        } else if (PATTERN_TYPE_BOOLEAN.matcher(fieldType).matches()) {
            result = "Boolean";
        } else if (PATTERN_TYPE_ARRAY.matcher(fieldType).matches()) {
            result = "Array";
        } else {
            result = "Object";
        }
        return result;
    }

    private JSONObject dealCommonParam(String fieldString, String annotationString, String type, String parentId,  String scope,Integer interfaceId) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("name", fieldString);
        jsonObject.put("description", annotationString);
        String dealType = dealType(type);
        jsonObject.put("type", dealType);
        jsonObject.put("memory", true);
        jsonObject.put("pos", 3);
        String idStr = "memory-" + IDX;
        jsonObject.put("id", idStr);
        jsonObject.put("parentId", parentId);
        jsonObject.put("scope", scope);
        jsonObject.put("interfaceId", interfaceId);
        return jsonObject;
    }

    public void parse(PsiClass psiClass) {
        JSONArray all = new JSONArray();
        this.parseJSONArray(psiClass, all);
        System.out.println(all.toJSONString());
        try {
            String generator = this.generator(all);
            System.out.println(generator);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
    }

    public void parseJSONArray(PsiClass psiClass,JSONArray all) {
        for (PsiField psiField : psiClass.getAllFields()) {
            PsiType type = psiField.getType();
            String name = psiField.getName();
            PsiDocComment docComment = psiField.getDocComment();
            String comment = docComment == null ? "" : docComment.getText();
            if (type instanceof PsiPrimitiveType) {
                //basic type
                System.out.println("basic type is ->" + type + " name is->" + name + "comment is ->" + comment);
            } else {
                System.out.println("other type is ->" + type + " name is->" + name + "comment is ->" + comment);
            }
            //生成规则
            JSONObject jsonObject = dealCommonParam(name, comment, type.getPresentableText(), "-1", RESPONSE,161);
            all.add(jsonObject);
            IDX++;
        }
    }
}


