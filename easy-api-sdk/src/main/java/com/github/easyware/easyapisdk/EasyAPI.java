package com.github.easyware.easyapisdk;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.serializer.SimplePropertyPreFilter;
import io.swagger.v3.oas.models.*;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.*;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.servers.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpMethod;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.condition.MediaTypeExpression;
import org.springframework.web.servlet.mvc.condition.PatternsRequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletResponse;
import java.beans.IntrospectionException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.*;

//@RestController
public class EasyAPI  {
    private static Logger logger = LoggerFactory.getLogger(EasyAPI.class);
    private static String CONTENT_TYPE_JSON = "application/json";
    private static byte BODY_NO=0;
    private static byte BODY_YES=1;
    private static byte BODY_FORM=2;
    private static Class errorControllerClass;
    static {
        try{
            errorControllerClass=Class.forName("org.springframework.boot.autoconfigure.web.ErrorController");
        }catch (Exception e){

        }
    }
    //@Autowired
    //ParameterNameDiscoverer parameterNameDiscoverer;

    private static DefaultParameterNameDiscoverer discoverer = new DefaultParameterNameDiscoverer();

    private JSONObject groupMethods;
    private JSONObject groupClasses;
    RequestMappingHandlerMapping handlerMapping;
    /*String projectTitle;
    String projectUrl;
    String easyAPIServerUrl;*/
    private EasyAPIConfig  config;
    public EasyAPI(RequestMappingHandlerMapping handlerMapping,EasyAPIConfig  config){

       /* this.projectTitle=projectTitle;
        this.projectUrl=projectUrl;
        this.easyAPIServerUrl=easyAPIServerUrl;*/
        this.handlerMapping=handlerMapping;
        this.config=config;
        if(!StringUtils.hasText(config.getEasyAPIAppUrl())) throw new RuntimeException("easyAPIAppUrl need");
        if(!StringUtils.hasText(config.getEasyAPIAppGroup()))throw new RuntimeException("easyAPIAppGroup need");
    }

    //@Autowired
    //private RequestMappingHandlerMapping handlerMapping;


    //@RequestMapping(value = "/mappings")
    // 第一级 or  ， 第二级 and
    private boolean match(String[] search,String text){
        if(search==null || search.length==0) return false;
        for(String searchItem:search) {
            String[] items=searchItem.trim().split("[;+；\\s]+");
            boolean allMatch=true;
                for (String item : items) {
                    if (text.toLowerCase().indexOf(item.trim().toLowerCase()) ==-1){
                        allMatch=false;
                        break;
                    }
                }
                if(allMatch) return true;
        }
       return false;
    }

    private String getKey(String[] search,String key){
        String cacheKey="EasyAPI::getDoc:url:"+key+":";
        if(search!=null){
           for(String s:search) cacheKey+=s;
        }
        return cacheKey;
    }
    private String[] trim(String[] items){
        if(items==null) return null;
       List<String> result=new ArrayList<>();
       for(String item:items){
           if(item!=null && item.trim().length()>0){
               result.add(item);
           }
       }
       return result.toArray(new String[0]);
    }

    public String getDoc(String[] search,String urlMatch) throws Exception {
        search=trim(search);
        String cacheKey=getKey(search,urlMatch);
        String data=Global.getCache(cacheKey);
        if(data!=null) return data;

        long t=System.currentTimeMillis();

        String groupUrl=config.getEasyAPIAppUrl();
        if(!groupUrl.endsWith("/")) groupUrl+="/";
        groupUrl+=config.getEasyAPIAppGroup();
        groupMethods=Global.httpGetObject(groupUrl+"/methods",config.getCommentCacheSeconds());
        groupClasses=Global.httpGetObject(groupUrl+"/classes",config.getCommentCacheSeconds());

        OpenAPI openAPI = new OpenAPI();
        Info info = new Info();
        info.version("1.0.0").title(config.getTitle());//"wap-m");
        List<Server> servers=new ArrayList<>();
        for(String url:config.getServerUrls()){
            Server server = new Server();
            server.url(url);//"http://wapmanageapi.test.tiebaobei.com/wapmanageApi");
            servers.add(server);
        }


        Paths paths = new Paths();
        Components allComponents = new Components();

        openAPI.info(info).servers(servers).paths(paths).components(allComponents);

        List<HashMap<String, String>> urlList = new ArrayList<HashMap<String, String>>();

        Map<RequestMappingInfo, HandlerMethod> map = handlerMapping.getHandlerMethods();
        logger.info("map count=" + map.size());
        int i = 0;
        //String[][] searchItems=null;
       /* if(search!=null && search.length>0){
            searchItems= search.toLowerCase().split(",");
        }*/
        Set<String> tags=new HashSet<>();
        for (Map.Entry<RequestMappingInfo, HandlerMethod> m : map.entrySet()) {

            HashMap<String, String> hashMap = new HashMap<String, String>();
            //  RequestMappingInfo 对应 @RequestMapping 的内容
            RequestMappingInfo requestMapping = m.getKey();
            HandlerMethod handlerMethod = m.getValue();
            if(errorControllerClass!=null && errorControllerClass.isAssignableFrom(handlerMethod.getBeanType()) )continue;


            //-- url
            PatternsRequestCondition p = requestMapping.getPatternsCondition();
            if (CollectionUtils.isEmpty(p.getPatterns())) continue;
            String url = p.getPatterns().iterator().next();// only get first url
            System.out.println("-----"+url);
            if(StringUtils.hasText(urlMatch)){
                if(url.toLowerCase().indexOf(urlMatch.toLowerCase())==-1){
                    continue;
                }
            }
            //if(!p.getPatterns().iterator().next().startsWith("/aa")) continue;
            //if(i++>30) break;

            Components components = new Components();

            //-- PathItem
            PathItem item = new PathItem();

            //--Operation（描述对路径的某个操作。）
            Operation operation = new Operation();


            Method javaMethod=handlerMethod.getMethod();
            //String serverUrl=easyAPIServerUrl+"/method?g=group1&m="+;
            JSONObject comment= groupMethods.getJSONObject(javaMethod.getDeclaringClass().getName()+"."+javaMethod.getName());
            if(comment!=null) {
                operation.setSummary(comment.getString("summary"));//"summary");//todo
                operation.setDescription(comment.getString("desc"));//todo
            }
            operation.setOperationId(javaMethod.getName());

           String tag= handlerMethod.getBeanType().getSimpleName();
           /*if(tags.contains(tag)){
               tag=handlerMethod.getBeanType().getName();
           }*/
            tags.add(tag);
            operation.addTagsItem(tag);

            //parameters（对应 java 方法的参数）
            System.out.println("-----parameters");
            boolean hasbody= doParameters(operation, components, requestMapping, handlerMethod,comment==null?null:comment.getJSONObject("params"));

            // return type
            System.out.println("-----returnType");
            doReturnType(operation, components, requestMapping, handlerMethod,comment==null?null:comment.getString("response"));

            //仅取第一个http method
            PathItem.HttpMethod httpMethod = getHttpMethod(requestMapping, handlerMethod);
            if(httpMethod==null) httpMethod=hasbody?PathItem.HttpMethod.POST:PathItem.HttpMethod.GET;
            item.operation(httpMethod, operation);

            if(search==null || search.length==0 || match(search,url+JSON.toJSONString(item)+JSON.toJSONString(components))){
                paths.addPathItem(url, item);
                if(components.getSchemas()!=null){
                    components.getSchemas().forEach((k,v)-> allComponents.addSchemas(k,v));
                }

            }

        }

        SimplePropertyPreFilter filter = new SimplePropertyPreFilter();
        filter.getExcludes().add("exampleSetFlag");
        logger.info("cost {}ms",(System.currentTimeMillis()-t));
        data= JSON.toJSONString(openAPI, filter, SerializerFeature.PrettyFormat);// openAPI.toString();
        Global.addCache(cacheKey,data,config.getCacheSeconds());
        return data;

    }

    /**
     *
     * @param operation
     * @param components
     * @param requestMapping
     * @param handlerMethod
     * @param descMap
     * @return  是否包含body
     * @throws IntrospectionException
     */
    private boolean doParameters(Operation operation, Components components, RequestMappingInfo requestMapping, HandlerMethod handlerMethod, JSONObject descMap) throws IntrospectionException {
        MethodParameter[] methodParameters = handlerMethod.getMethodParameters();
        boolean hasBody=false;
        if (methodParameters == null || methodParameters.length == 0) return hasBody;


        List<Parameter> parameters = new ArrayList<>();
        operation.parameters(parameters);
        //-- 是否有@RequestBody
        for (MethodParameter mp : methodParameters) {
            if (Global.ignoreParamTypeSet.contains(mp.getParameterType())) continue;
            if (mp.hasParameterAnnotation(RequestBody.class)) {
                hasBody=true;
            }
        }
        for (MethodParameter mp : methodParameters) {
            if (Global.ignoreParamTypeSet.contains(mp.getParameterType())) continue;

            //--初始化参数名称读取器
            mp.initParameterNameDiscovery(discoverer);
            Parameter parameter = new Parameter();

            String parameterName = mp.getParameterName();

            String paramDesc=null;
            if(descMap!=null) {
                paramDesc=descMap.getString(parameterName);
            }

            String bodyContentType=null;
            boolean required = false;
            //-- 解析参数注解
            if (mp.hasParameterAnnotation(RequestHeader.class)) {
                parameter.in("header");
                RequestHeader annotation = mp.getParameterAnnotation(RequestHeader.class);
                if (annotation != null && !StringUtils.isEmpty(annotation.value()))
                    parameterName = annotation.value();
                required = annotation != null && annotation.required() && annotation.defaultValue().equals(ValueConstants.DEFAULT_NONE);
            } else if (mp.hasParameterAnnotation(CookieValue.class)) {
                parameter.in("cookie");
                CookieValue annotation = mp.getParameterAnnotation(CookieValue.class);
                if (annotation != null && !StringUtils.isEmpty(annotation.value()))
                    parameterName = annotation.value();
                required = annotation != null && annotation.required() && annotation.defaultValue().equals(ValueConstants.DEFAULT_NONE);
            } else if (mp.hasParameterAnnotation(PathVariable.class)) {
                PathVariable annotation = mp.getParameterAnnotation(PathVariable.class);
                if (annotation != null && !StringUtils.isEmpty(annotation.value()))
                    parameterName = annotation.value();
                required = annotation != null && annotation.required();
                parameter.in("path");
            } else if (mp.hasParameterAnnotation(RequestBody.class)) {
                //operation/requestBody/content/"application/json"/schema
                RequestBody annotation = mp.getParameterAnnotation(RequestBody.class);
                required = annotation != null && annotation.required();
                bodyContentType = getRequestContentType(requestMapping, CONTENT_TYPE_JSON);

            } else {
                RequestParam annotation = mp.getParameterAnnotation(RequestParam.class);
                if (annotation != null && !StringUtils.isEmpty(annotation.value())) {
                    parameterName = annotation.value();
                }
                required = annotation != null && annotation.required() && annotation.defaultValue().equals(ValueConstants.DEFAULT_NONE);
                //参数如果是对象类型，则设置为openapi中的RequestBody对象或者 parameter.in("body")，这样才会显示schema（包含说明），如果设为query则只显示example，不显示schema
                //优先设置第一个对象为RequestBody,便于正确执行try it out；其它对象设置为parameter.in("body")，能显示schema，但不能正确执行try it out！！！
                if(Global.getBase(mp.getParameterType())==null){

                    if(!hasBody) {
                        bodyContentType = "application/x-www-form-urlencoded";
                        hasBody=true;
                    }else{
                        parameter.in("body");
                    }
                }else{
                    parameter.in("query");
                }

            }


            Type type = mp.getGenericParameterType();
            DefaultTypeVisitCallback typeVisitCallback = new DefaultTypeVisitCallback(components,groupClasses);
            new TypeVisit(typeVisitCallback).visit(type);
            Schema root = typeVisitCallback.root;
            if (bodyContentType != null) {
                MediaType  requestBodyObject = new MediaType();
                requestBodyObject.schema(root);

                Content content = new Content();
                content.addMediaType(bodyContentType, requestBodyObject);

                io.swagger.v3.oas.models.parameters.RequestBody requestBody = new io.swagger.v3.oas.models.parameters.RequestBody();
                requestBody.description(paramDesc);
                requestBody.content(content);
                operation.requestBody(requestBody);

            } else {
                parameter.description(paramDesc);
                parameter.schema(root);
                parameter.name(parameterName);// mp.getParameterName());
                parameter.required(required);
                parameters.add(parameter);
            }

        }
        return hasBody;

    }

    private void doReturnType(Operation operation, Components components, RequestMappingInfo requestMapping, HandlerMethod handlerMethod,String desc) throws IntrospectionException {

        MethodParameter mp = handlerMethod.getReturnType();
        Type type = mp.getGenericParameterType();
        DefaultTypeVisitCallback typeVisitCallback = new DefaultTypeVisitCallback(components,groupClasses);
        new TypeVisit(typeVisitCallback).visit(type);
        Schema root = typeVisitCallback.root;

        ApiResponses apiResponses = new ApiResponses();
        operation.responses(apiResponses);
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.description(desc);
        apiResponses.addApiResponse("200", apiResponse);

        String contentType = getResponseContentType(requestMapping, CONTENT_TYPE_JSON);
        Content content = new Content();
        apiResponse.content(content);
        MediaType mediaType = new MediaType();
        content.addMediaType(contentType, mediaType);
        mediaType.schema(root);

    }




    private String getRequestContentType(RequestMappingInfo requestMapping, String def) {
        //ConsumesRequestCondition 用于存储@RequestMapping注解中的consumes、headers中的Content-Type
        Set<MediaTypeExpression> expressions = requestMapping.getConsumesCondition().getExpressions();
        if (!expressions.isEmpty()) {
            MediaTypeExpression expression = expressions.iterator().next();
            return expression.getMediaType().toString();
        }
        return def;
    }

    private String getResponseContentType(RequestMappingInfo requestMapping, String def) {
        //ProceduresRequestCondition 用于存储@RequestMapping注解中的produces 、 headers中的accept。
        Set<MediaTypeExpression> expressions = requestMapping.getProducesCondition().getExpressions();
        if (!expressions.isEmpty()) {
            MediaTypeExpression expression = expressions.iterator().next();
            return expression.getMediaType().toString();
        }
        return def;
    }

    /**
     *
     * @param requestMapping
     * @param method
     * @return
     */
    private PathItem.HttpMethod getHttpMethod(RequestMappingInfo requestMapping, HandlerMethod method) {
        Set<RequestMethod> requestMethods = requestMapping.getMethodsCondition().getMethods();
        PathItem.HttpMethod httpMethod=null;
        if (!CollectionUtils.isEmpty(requestMethods)) {
            httpMethod = convert(requestMethods.iterator().next());

        } else if (hasParameterAnnotation(method, RequestBody.class) || hasParameterAnnotation(method, RequestPart.class)) {
            httpMethod = PathItem.HttpMethod.POST;

        }
        return httpMethod;
    }

    private PathItem.HttpMethod convert(RequestMethod requestMethod) {
        return PathItem.HttpMethod.valueOf(requestMethod.name());
    }

    public boolean hasParameterAnnotation(HandlerMethod method, Class annotationType) {
        if (method.getMethodParameters() != null) {
            for (MethodParameter mp : method.getMethodParameters()) {
                if (mp.hasParameterAnnotation(annotationType)) return true;
            }
        }
        return false;
    }

    private String[] getDataType(Type type) {
        if (type instanceof Class) {
            Class c = (Class) type;
            String s = Global.getBase(c);
            if (s != null) {
                return Global.dataTypeMap.get(s);
            }
        }
        return null;

    }




}
