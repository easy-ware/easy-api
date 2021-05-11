package com.github.easyware.easyapisdk;

import com.alibaba.fastjson.JSONObject;
import io.swagger.v3.oas.models.*;
import io.swagger.v3.oas.models.media.*;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public  class DefaultTypeVisitCallback implements TypeVisitCallback<ComponentHolder> {
        Components components;
        Schema root;

    JSONObject groupClasses;
        public DefaultTypeVisitCallback(Components components, JSONObject groupClasses) {
            this.components = components;
            this.groupClasses=groupClasses;
        }

        private JSONObject getComments(Class clazz){
            JSONObject comments=new JSONObject();
            if(clazz.equals(Object.class)) return comments;
            Class c=clazz;
            List<Class> list=new ArrayList<>();
            list.add(c);
            for(int i=0;i<3;i++){
                c=c.getSuperclass();
                if(c==null || c.getName().startsWith("java.")){break;}
                list.add(c);

            }
           for(int i=list.size()-1;i>=0;i--){
               JSONObject j= groupClasses.getJSONObject(list.get(i).getName());
               if(j!=null){
                   comments.putAll(j);
               }

           }
           return comments;

        }

        @Override
        public ComponentHolder callback(ComponentHolder parent, String prop, Type sourceType, Class clazz, boolean array, String baseDataType) {

             if(parent==null) return callbackRoot(prop,sourceType,clazz,array,baseDataType);

            return callbackComponent(parent,prop,sourceType,clazz,array,baseDataType);


        }

    /**
     *"responses": {
     * 					"200": {
     * 						"content": {
     * 							"application/json": {
     * 								"schema": {
     * 									"$ref": "#/components/schemas/com.cehome.jishou.mobile_api.entity.Result>"
     *                                                                }* 							}
     * 						}
     *
     * @param prop
     * @param sourceType
     * @param clazz
     * @param array
     * @param baseDataType
     * @return
     */
    public ComponentHolder callbackRoot(String prop, Type sourceType, Class clazz, boolean array, String baseDataType) {

        //-- 参数的schema
        Schema schema = new Schema();
        root = array?createArraySchema(schema):schema;

        if (baseDataType != null) {
            setBaseData(schema,baseDataType);
            return null;

        } else {
            schema.$ref("#/components/schemas/" + sourceType);//"$ref": "#/components/schemas/Pets"

            //-- 组件里面的schema
            return createEmptyComponent(sourceType,clazz);

        }

    }

   private ComponentHolder createEmptyComponent(Type sourceType, Class clazz){
       if(componentExists(sourceType)){
           return null;
       }
        //-- 组件里面的schema
        ComponentHolder current=new ComponentHolder();
        JSONObject comments= getComments(clazz);
        current.setPropComments(comments);
        //
        ObjectSchema componentSchema = new ObjectSchema();
        components.addSchemas(sourceType.toString(), componentSchema);
        current.setSchema(componentSchema);
        return current;
    }

    /**
     * "components": {
     * 		"schemas": {
     * 			"com.cehome.jishou.mobile_api.entity.Result>": {
     * 				"properties": {
     * 					"msg": {
     * 						"type": "string"
     *                                        },
     * 					"result[]": {
     * 						"items": {
     * 							"$ref": "#/components/schemas/class com.cehome.jishou.mobile_api.entity.ProductBrandModels"
     *                        },
     * 						"type": "array"
     *                    },
     * 					"ret": {
     * 						"format": "int32",
     * 						"type": "integer"
     *                    }* 				},
     * 				"type": "object"* 			}
     * @param parent
     * @param prop
     * @param sourceType
     * @param clazz
     * @param array
     * @param baseDataType
     * @return
     */
    public ComponentHolder callbackComponent(ComponentHolder parent, String prop, Type sourceType, Class clazz, boolean array, String baseDataType) {

        //-- 参数的schema
        Schema schema = new Schema();

        String desc=null;
        if(parent!=null && parent.getPropComments()!=null) desc=parent.getPropComments().getString(prop);

        Schema  wrap = array?createArraySchema(schema):schema;
        wrap.description(desc);
        parent.getSchema().addProperties(prop, wrap);


        if (baseDataType != null) {
            setBaseData(schema,baseDataType);
            return null;

        } else {
            schema.$ref("#/components/schemas/" + sourceType);//"$ref": "#/components/schemas/Pets"
            //-- 组件里面的schema
            return createEmptyComponent(sourceType,clazz);

        }




    }

    private boolean componentExists(Type sourceType){
        return components.getSchemas() !=null && components.getSchemas().get(sourceType.toString())!=null;
    }

    private void setBaseData(Schema schema,String baseDataType){
        String[] dataType = Global.dataTypeMap.get(baseDataType);
        schema.type(dataType[0]);
        schema.format(dataType[1]);

    }

    private ArraySchema createArraySchema(Schema schema){
        ArraySchema arraySchema = new ArraySchema();
        arraySchema.items(schema);
        return arraySchema;
    }


    }