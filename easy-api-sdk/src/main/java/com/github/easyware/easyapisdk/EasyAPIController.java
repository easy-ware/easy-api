package com.github.easyware.easyapisdk;


import com.alibaba.fastjson.JSON;
import io.swagger.v3.oas.models.*;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.*;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.servers.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory;
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
import java.lang.reflect.Type;
import java.util.*;

//@RestController
public class EasyAPIController implements BeanFactoryAware {
    private static Logger logger = LoggerFactory.getLogger(EasyAPIController.class);
    private static String CONTENT_TYPE_JSON = "application/json";
    private static Class errorControllerClass;
    static {
        try{
            errorControllerClass=Class.forName("org.springframework.boot.autoconfigure.web.ErrorController");
        }catch (Exception e){

        }
    }
    //@Autowired
    //ParameterNameDiscoverer parameterNameDiscoverer;
    private Class[] ignoreParamTypes = new Class[]{javax.servlet.ServletRequest.class,
            javax.servlet.ServletResponse.class,
            javax.servlet.http.HttpServletRequest.class,
            HttpServletResponse.class,
            javax.servlet.http.HttpSession.class,
            javax.servlet.http.HttpSession.class,
            WebRequest.class,
            NativeWebRequest.class,
            java.security.Principal.class,

            HttpMethod.class,
            Locale.class,
            TimeZone.class,
            java.io.InputStream.class,
            java.time.ZoneId.class,
            java.io.Reader.class,
            java.io.OutputStream.class,
            java.io.Writer.class,
            Map.class,
            org.springframework.ui.Model.class,
            org.springframework.ui.ModelMap.class,
            Errors.class,
            BindingResult.class,
            SessionStatus.class,
            UriComponentsBuilder.class,
            RequestAttribute.class};
    private Set<Class> ignoreParamTypeSet = new HashSet<>(Arrays.asList(ignoreParamTypes));

    private static Map<String, String[]> dataTypeMap = new HashMap() {{
        put("byte", new String[]{"integer", "int32"});
        put("short", new String[]{"integer", "int32"});
        put("int", new String[]{"integer", "int32"});
        put("long", new String[]{"integer", "int64"});
        put("float", new String[]{"number", "float"});
        put("double", new String[]{"number", "double"});
        put("string", new String[]{"string", null});
        put("char", new String[]{"string", null});
        put("boolean", new String[]{"boolean", null});
        put("object", new String[]{"object", null});//未知的对象

    }};

    private DefaultParameterNameDiscoverer discoverer = new DefaultParameterNameDiscoverer();


    @Autowired
    private RequestMappingHandlerMapping handlerMapping;

    @RequestMapping(value = "/mappings")
    public String mappings() throws Exception {
        OpenAPI openAPI = new OpenAPI();
        Info info = new Info();
        info.version("1.0.0").title("wap-m");
        Server server = new Server();
        server.url("http://wapmanageapi.test.tiebaobei.com/wapmanageApi");
        Paths paths = new Paths();
        Components components = new Components();

        openAPI.info(info).servers(Arrays.asList(server)).paths(paths).components(components);

        List<HashMap<String, String>> urlList = new ArrayList<HashMap<String, String>>();

        Map<RequestMappingInfo, HandlerMethod> map = handlerMapping.getHandlerMethods();
        logger.info("map count=" + map.size());
        int i = 0;
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
            //if(!p.getPatterns().iterator().next().startsWith("/aa")) continue;
            //if(i++>30) break;

            //-- PathItem
            PathItem item = new PathItem();
            paths.addPathItem(url, item);


            //--Operation（描述对路径的某个操作。）
            Operation operation = new Operation();
            //仅取第一个http method
            PathItem.HttpMethod httpMethod = getHttpMethod(requestMapping, handlerMethod);
            item.operation(httpMethod, operation);

            operation.setSummary("summary");//todo
            operation.setDescription("desc");//todo
            operation.setOperationId(handlerMethod.getMethod().getName());

            //parameters（对应 java 方法的参数）
            System.out.println("-----parameters");
            doParameters(operation, components, requestMapping, handlerMethod);
            // return type
            System.out.println("-----returnType");
            doReturnType(operation, components, requestMapping, handlerMethod);


            //RequestBody b=method.getMethodParameters()[0].getParameterAnnotation(RequestBody.class);
            //RequestParam rp=method.getMethodParameters()[1].getParameterAnnotation(RequestParam.class);
            //RequestParam rp2= org.springframework.core.annotation.AnnotationUtils.findAnnotation(method.getMethod(),RequestParam.class);



            /*hashMap.put("className", method.getMethod().getDeclaringClass().getName()); // 类名
            hashMap.put("method", method.getMethod().getName()); // 方法名
            RequestMethodsRequestCondition methodsCondition = mappingInfo.getMethodsCondition();
            String type = methodsCondition.toString();
            if (type != null && type.startsWith("[") && type.endsWith("]")) {
                type = type.substring(1, type.length() - 1);
                hashMap.put("type", type); // 方法名
            }
            urlList.add(hashMap);*/
        }
        return JSON.toJSONString(openAPI, true);// openAPI.toString();

    }

    private void doParameters(Operation operation, Components components, RequestMappingInfo requestMapping, HandlerMethod handlerMethod) throws IntrospectionException {
        MethodParameter[] methodParameters = handlerMethod.getMethodParameters();
        if (methodParameters == null || methodParameters.length == 0) return;


        List<Parameter> parameters = new ArrayList<>();
        operation.parameters(parameters);

        for (MethodParameter mp : methodParameters) {
            if (ignoreParamTypeSet.contains(mp.getParameterType())) continue;

            mp.initParameterNameDiscovery(discoverer);
            Parameter parameter = new Parameter();

            MediaType requestBodyObject = null;
            String parameterName = mp.getParameterName();

            if (mp.getParameterName() == null) {
                System.out.println("is null");
            }
            parameter.description("desc");//todo
            boolean required = false;

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
                String contentType = getRequestContentType(requestMapping, CONTENT_TYPE_JSON);
                io.swagger.v3.oas.models.parameters.RequestBody requestBody = new io.swagger.v3.oas.models.parameters.RequestBody();
                operation.requestBody(requestBody);
                requestBody.description("requestBody desc");
                Content content = new Content();
                requestBody.content(content);
                requestBodyObject = new MediaType();
                content.addMediaType(contentType, requestBodyObject);

                //parameter.in("path");


            } else {
                RequestParam annotation = mp.getParameterAnnotation(RequestParam.class);
                if (annotation != null && !StringUtils.isEmpty(annotation.value()))
                    parameterName = annotation.value();
                required = annotation != null && annotation.required() && annotation.defaultValue().equals(ValueConstants.DEFAULT_NONE);
                parameter.in("query");
            }

            Type type = mp.getGenericParameterType();
            TypeVisitCallbackImpl typeVisitCallback = new TypeVisitCallbackImpl(components);
            new TypeVisit(typeVisitCallback).visit(type);
            Schema root = typeVisitCallback.root;
            if (requestBodyObject != null) {
                requestBodyObject.schema(root);
            } else {
                parameter.schema(root);
                parameter.name(parameterName);// mp.getParameterName());
                parameter.required(required);
                parameters.add(parameter);

            }

        }

    }

    private void doReturnType(Operation operation, Components components, RequestMappingInfo requestMapping, HandlerMethod handlerMethod) throws IntrospectionException {

        MethodParameter mp = handlerMethod.getReturnType();
        Type type = mp.getGenericParameterType();
        TypeVisitCallbackImpl typeVisitCallback = new TypeVisitCallbackImpl(components);
        new TypeVisit(typeVisitCallback).visit(type);
        Schema root = typeVisitCallback.root;

        ApiResponses apiResponses = new ApiResponses();
        operation.responses(apiResponses);
        ApiResponse apiResponse = new ApiResponse();
        apiResponses.addApiResponse("200", apiResponse);

        String contentType = getResponseContentType(requestMapping, CONTENT_TYPE_JSON);
        Content content = new Content();
        apiResponse.content(content);
        MediaType mediaType = new MediaType();
        content.addMediaType(contentType, mediaType);
        mediaType.schema(root);

    }

    public  class TypeVisitCallbackImpl implements TypeVisitCallback<Schema> {
        Components components;
        Schema root;

        public TypeVisitCallbackImpl(Components components) {
            this.components = components;
        }

        @Override
        public Schema callback(Schema parent, String prop, Type sourceType,Class clazz, boolean array, String baseDataType) {

            //-- 参数的schema
            Schema schema = new Schema();
            if (array) {
                ArraySchema arraySchema = new ArraySchema();
                arraySchema.items(schema);
                if (parent == null) root = arraySchema;
                else parent.addProperties(prop, arraySchema);
            } else {
                if (parent == null) root = schema;
                else parent.addProperties(prop, schema);
            }
            if (baseDataType != null) {
                //System.out.println("ba="+baseDataType);
                String[] dataType = dataTypeMap.get(baseDataType);
                schema.type(dataType[0]);
                schema.format(dataType[1]);
                return schema;

            } else {
                schema.$ref("#/components/schemas/" + sourceType);//"$ref": "#/components/schemas/Pets"
                //-- 组件里面的schema
                ObjectSchema objectSchema = new ObjectSchema();
                components.addSchemas(sourceType.toString(), objectSchema);
                return objectSchema;
            }


        }
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

    private PathItem.HttpMethod getHttpMethod(RequestMappingInfo requestMapping, HandlerMethod method) {
        Set<RequestMethod> requestMethods = requestMapping.getMethodsCondition().getMethods();
        PathItem.HttpMethod httpMethod;
        if (!CollectionUtils.isEmpty(requestMethods)) {
            httpMethod = convert(requestMethods.iterator().next());

        } else if (hasParameterAnnotation(method, RequestBody.class) || hasParameterAnnotation(method, RequestPart.class)) {
            httpMethod = PathItem.HttpMethod.POST;

        } else {
            httpMethod = PathItem.HttpMethod.GET;

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
                return dataTypeMap.get(s);
            }
        }
        return null;

    }


    @RequestMapping(value = "/pet")
    public String pet(HttpServletResponse response) {
        //Access-Control-Allow-*
        response.setHeader("Access-Control-Allow-Origin", "*");

        response.setHeader("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, api_key, Authorization");
        return "{\n" +
                "  \"openapi\": \"3.0.0\",\n" +
                "  \"info\": {\n" +
                "    \"version\": \"1.0.0\",\n" +
                "    \"title\": \"Swagger Petstore\",\n" +
                "    \"license\": {\n" +
                "      \"name\": \"MIT\"\n" +
                "    }\n" +
                "  },\n" +
                "  \"servers\": [\n" +
                "    {\n" +
                "      \"url\": \"http://petstore.swagger.io/v1\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"paths\": {\n" +
                "    \"/pets\": {\n" +
                "      \"get\": {\n" +
                "        \"summary\": \"List all pets\",\n" +
                "        \"operationId\": \"listPets\",\n" +
                "        \"tags\": [\n" +
                "          \"pets\"\n" +
                "        ],\n" +
                "        \"parameters\": [\n" +
                "          {\n" +
                "            \"name\": \"limit\",\n" +
                "            \"in\": \"query\",\n" +
                "            \"description\": \"How many items to return at one time (max 100)\",\n" +
                "            \"required\": false,\n" +
                "            \"schema\": {\n" +
                "              \"type\": \"integer\",\n" +
                "              \"format\": \"int32\"\n" +
                "            }\n" +
                "          }\n" +
                "        ],\n" +
                "        \"responses\": {\n" +
                "          \"200\": {\n" +
                "            \"description\": \"A paged array of pets\",\n" +
                "            \"headers\": {\n" +
                "              \"x-next\": {\n" +
                "                \"description\": \"A link to the next page of responses\",\n" +
                "                \"schema\": {\n" +
                "                  \"type\": \"string\"\n" +
                "                }\n" +
                "              }\n" +
                "            },\n" +
                "            \"content\": {\n" +
                "              \"application/json\": {\n" +
                "                \"schema\": {\n" +
                "                  \"$ref\": \"#/components/schemas/Pets\"\n" +
                "                }\n" +
                "              }\n" +
                "            }\n" +
                "          },\n" +
                "          \"default\": {\n" +
                "            \"description\": \"unexpected error\",\n" +
                "            \"content\": {\n" +
                "              \"application/json\": {\n" +
                "                \"schema\": {\n" +
                "                  \"$ref\": \"#/components/schemas/Error\"\n" +
                "                }\n" +
                "              }\n" +
                "            }\n" +
                "          }\n" +
                "        }\n" +
                "      },\n" +
                "      \"post\": {\n" +
                "        \"summary\": \"Create a pet\",\n" +
                "        \"operationId\": \"createPets\",\n" +
                "        \"tags\": [\n" +
                "          \"pets\"\n" +
                "        ],\n" +
                "        \"responses\": {\n" +
                "          \"201\": {\n" +
                "            \"description\": \"Null response\"\n" +
                "          },\n" +
                "          \"default\": {\n" +
                "            \"description\": \"unexpected error\",\n" +
                "            \"content\": {\n" +
                "              \"application/json\": {\n" +
                "                \"schema\": {\n" +
                "                  \"$ref\": \"#/components/schemas/Error\"\n" +
                "                }\n" +
                "              }\n" +
                "            }\n" +
                "          }\n" +
                "        }\n" +
                "      }\n" +
                "    },\n" +
                "    \"/pets/{petId}\": {\n" +
                "      \"get\": {\n" +
                "        \"summary\": \"Info for a specific pet\",\n" +
                "        \"operationId\": \"showPetById\",\n" +
                "        \"tags\": [\n" +
                "          \"pets\"\n" +
                "        ],\n" +
                "        \"parameters\": [\n" +
                "          {\n" +
                "            \"name\": \"petId\",\n" +
                "            \"in\": \"path\",\n" +
                "            \"required\": true,\n" +
                "            \"description\": \"The id of the pet to retrieve\",\n" +
                "            \"schema\": {\n" +
                "              \"type\": \"string\"\n" +
                "            }\n" +
                "          }\n" +
                "        ],\n" +
                "        \"responses\": {\n" +
                "          \"200\": {\n" +
                "            \"description\": \"Expected response to a valid request\",\n" +
                "            \"content\": {\n" +
                "              \"application/json\": {\n" +
                "                \"schema\": {\n" +
                "                  \"$ref\": \"#/components/schemas/Pet\"\n" +
                "                }\n" +
                "              }\n" +
                "            }\n" +
                "          },\n" +
                "          \"default\": {\n" +
                "            \"description\": \"unexpected error\",\n" +
                "            \"content\": {\n" +
                "              \"application/json\": {\n" +
                "                \"schema\": {\n" +
                "                  \"$ref\": \"#/components/schemas/Error\"\n" +
                "                }\n" +
                "              }\n" +
                "            }\n" +
                "          }\n" +
                "        }\n" +
                "      }\n" +
                "    }\n" +
                "  },\n" +
                "  \"components\": {\n" +
                "    \"schemas\": {\n" +
                "      \"Pet\": {\n" +
                "        \"type\": \"object\",\n" +
                "        \"required\": [\n" +
                "          \"id\",\n" +
                "          \"name\"\n" +
                "        ],\n" +
                "        \"properties\": {\n" +
                "          \"id\": {\n" +
                "            \"type\": \"integer\",\n" +
                "            \"format\": \"int64\"\n" +
                "          },\n" +
                "          \"name\": {\n" +
                "            \"type\": \"string\"\n" +
                "          },\n" +
                "          \"tag\": {\n" +
                "            \"type\": \"string\"\n" +
                "          }\n" +
                "        }\n" +
                "      },\n" +
                "      \"Pets\": {\n" +
                "        \"type\": \"array\",\n" +
                "        \"items\": {\n" +
                "          \"$ref\": \"#/components/schemas/Pet\"\n" +
                "        }\n" +
                "      },\n" +
                "      \"Error\": {\n" +
                "        \"type\": \"object\",\n" +
                "        \"required\": [\n" +
                "          \"code\",\n" +
                "          \"message\"\n" +
                "        ],\n" +
                "        \"properties\": {\n" +
                "          \"code\": {\n" +
                "            \"type\": \"integer\",\n" +
                "            \"format\": \"int32\"\n" +
                "          },\n" +
                "          \"message\": {\n" +
                "            \"type\": \"string\"\n" +
                "          }\n" +
                "        }\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}";
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        AbstractAutowireCapableBeanFactory f = (AbstractAutowireCapableBeanFactory) beanFactory;
        // f.getd
    }
}
