package com.github.easyware.easyapisdk;

import com.alibaba.fastjson.JSONObject;
import io.swagger.v3.oas.models.*;
import io.swagger.v3.oas.models.media.*;

import java.util.ArrayList;
import java.util.List;

public  class DefaultTypeVisitCallback implements TypeVisitCallback<DefaultTypeVisitObject> {
        Components components;
        Schema root;

    JSONObject groupClasses;
        public DefaultTypeVisitCallback(Components components, JSONObject groupClasses) {
            this.components = components;
            this.groupClasses=groupClasses;
        }

        private JSONObject getComments(Class clazz){
            JSONObject comments=new JSONObject();
            Class c=clazz;
            List<Class> list=new ArrayList<>();
            list.add(c);
            for(int i=0;i<3;i++){
                c=c.getSuperclass();
                if(c.getName().startsWith("java.")){break;}
                list.add(c);

            }
           for(int i=list.size()-1;i>=0;i--){
               JSONObject j= groupClasses.getJSONObject(clazz.getName());
               if(j!=null){
                   comments.putAll(j);
               }

           }
           return comments;

        }

        @Override
        public DefaultTypeVisitObject callback(DefaultTypeVisitObject parent, String prop, Class clazz, boolean array, String baseDataType) {

            //-- 参数的schema
            Schema schema = new Schema();
            DefaultTypeVisitObject current=new DefaultTypeVisitObject();
            JSONObject comments= getComments(clazz);
            current.setPropComments(comments);

            String desc=null;
            if(parent!=null && parent.getPropComments()!=null) desc=parent.getPropComments().getString(prop);

            if (array) {
                ArraySchema arraySchema = new ArraySchema();
                arraySchema.items(schema);
                arraySchema.description(desc);
                if (parent == null) root = arraySchema;
                else parent.getSchema().addProperties(prop, arraySchema);


            } else {
                schema.description(desc);
                if (parent == null) root = schema;
                else parent.getSchema().addProperties(prop, schema);
            }

            if (baseDataType != null) {
                //System.out.println("ba="+baseDataType);
                String[] dataType = Global.dataTypeMap.get(baseDataType);
                schema.type(dataType[0]);
                schema.format(dataType[1]);
                current.setSchema(schema);
               // return schema;

            } else {
                schema.$ref("#/components/schemas/" + clazz.getName());//"$ref": "#/components/schemas/Pets"
                //-- 组件里面的schema
                ObjectSchema objectSchema = new ObjectSchema();
                components.addSchemas(clazz.getName(), objectSchema);
                current.setSchema(objectSchema);
                //return objectSchema;
            }

            return current;


        }
    }