package com.pwc.query.controllers.models.contents;

import com.pwc.query.controllers.models.Content;
import com.pwc.query.controllers.models.ControllerBean;
import com.pwc.query.controllers.models.elements.CollectionElement;
import com.pwc.query.controllers.models.elements.ControllerElements;
import com.pwc.query.controllers.models.factories.ElementsFactory;

import java.util.ArrayList;
import java.util.List;

public class ControllerContent {


    protected String collectionJSONParsed;

    protected List<ControllerElements> getElementList(List<Content> collectItems, String type, ControllerBean controllerBeans) {

        int index =1;
        List<ControllerElements> elements = new ArrayList<>();
        ElementsFactory elementsFactory = new ElementsFactory();

        for (Content content : collectItems) {

            ControllerElements preElement = elements.size() > 0 ? elements.get(index-2) : null;
            ControllerElements element = elementsFactory.getCollectionContent(type,content,preElement,index,controllerBeans);
            elements.add(element);
            index++;
        }

        return elements;
    }

    public String getCollectionJSONParsed() {
        return collectionJSONParsed;
    }
    public void setCollectionJSONParsed(String jsonParse) {
        this.collectionJSONParsed = jsonParse;
    }

}
