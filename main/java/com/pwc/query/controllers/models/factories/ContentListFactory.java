package com.pwc.query.controllers.models.factories;

import com.pwc.query.controllers.models.contentlist.CollectionCL;
import com.pwc.query.controllers.models.contentlist.ContentList;
import com.pwc.query.controllers.models.contentlist.FacetedCL;
import com.pwc.query.enums.ControllerType;

public class ContentListFactory {

    public ContentList getContentListType(String type){

        ContentList cl;
        switch (ControllerType.valueOf(type.toUpperCase())) {
            case COLLECTION:
                cl = new CollectionCL();
                break;
            case FACETED:
                cl = new FacetedCL();
                break;
            default:
                throw new IllegalArgumentException("Invalid filter " + type);
        }

        return cl;
    }

}
