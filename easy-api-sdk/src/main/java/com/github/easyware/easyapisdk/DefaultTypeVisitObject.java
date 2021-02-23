package com.github.easyware.easyapisdk;

import com.alibaba.fastjson.JSONObject;
import io.swagger.v3.oas.models.media.Schema;

public class DefaultTypeVisitObject {
    private Schema schema;
    private JSONObject propComments;

    public Schema getSchema() {
        return schema;
    }

    public void setSchema(Schema schema) {
        this.schema = schema;
    }

    public JSONObject getPropComments() {
        return propComments;
    }

    public void setPropComments(JSONObject propComments) {
        this.propComments = propComments;
    }
}
