package com.pwc.query.controllers.models.factories;


import com.pwc.query.controllers.models.Content;
import com.pwc.query.controllers.models.ControllerBean;
import com.pwc.query.controllers.models.elements.CollectionElement;
import com.pwc.query.controllers.models.elements.ControllerElements;
import com.pwc.query.controllers.models.elements.FacetedElement;
import com.pwc.query.enums.ControllerType;

import java.util.List;


public class ElementsFactory {

    public ControllerElements getCollectionContent(String type, Content content,ControllerElements preElement, int index ,ControllerBean controllerBeans){

        ControllerElements element;
        switch (ControllerType.valueOf(type.toUpperCase())) {
            case COLLECTION:
                element = new CollectionElement(content,(CollectionElement) preElement,index,controllerBeans);
                break;
            case FACETED:
                element = new FacetedElement(content,index,controllerBeans);
                break;
            default:
                throw new IllegalArgumentException("Invalid filter " + type);
        }

        return element;
    }
}


