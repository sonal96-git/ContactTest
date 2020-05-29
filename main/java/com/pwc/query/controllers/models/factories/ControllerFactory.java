package com.pwc.query.controllers.models.factories;

import com.pwc.query.controllers.CollectionController;
import com.pwc.query.controllers.FacetedController;
import com.pwc.query.controllers.QueryController;
import com.pwc.query.enums.ControllerType;


public class ControllerFactory {


    public QueryController getController(String type){

        QueryController controller;
        switch (ControllerType.valueOf(type.toUpperCase())) {
            case COLLECTION:
                controller = new CollectionController();
                break;
            case FACETED:
                controller = new FacetedController();
                break;
            default:
                throw new IllegalArgumentException("Invalid filter " + type);
        }

        return controller;
    }
}
