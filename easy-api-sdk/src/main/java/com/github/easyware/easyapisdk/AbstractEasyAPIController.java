package com.github.easyware.easyapisdk;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.servlet.http.HttpServletResponse;

public abstract class AbstractEasyAPIController  {
    private static Logger logger = LoggerFactory.getLogger(EasyAPIController.class);

    @Autowired
    private RequestMappingHandlerMapping handlerMapping;

    protected abstract boolean canRun();
    protected abstract EasyAPIConfig getEasyAPIConfig();

    /**
     *
     * @param q  search words. 1) hello   2)hello+world (hello AND world)   3) hello+world,other1,other2 ( (hello AND world) or other1 or other2)
     * @param url search url
     * @param response
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/easyapi")
    public String easyapi(String[] q,String url,HttpServletResponse response) throws Exception {
        //return new EasyAPI(handlerMapping,"wapmanage-api","http://wapmanageapi.test.tiebaobei.com/wapmanageApi","http://localhost:7070/group1").getDoc(search);
        if(!canRun()) return null;
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, api_key, Authorization");
        EasyAPIConfig  config=getEasyAPIConfig();
        return new EasyAPI(handlerMapping, config).getDoc(q,url);
    }

    @RequestMapping(value = "/easyapiDemo")
    public String easyapiDemo(HttpServletResponse response) {
        if(!canRun()) return null;
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
}