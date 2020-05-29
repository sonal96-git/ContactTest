package com.pwc.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.jcr.RepositoryException;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.day.cq.wcm.api.designer.Designer;
import com.day.cq.wcm.api.reference.Reference;
import com.day.cq.wcm.api.reference.ReferenceProvider;

@Component(service = CanvasReferenceProvider.class, immediate = true)
public class CanvasReferenceProvider
  implements ReferenceProvider
{
  private static final Logger logger = LoggerFactory.getLogger(CanvasReferenceProvider.class);
  private static final String IMPORTER_RESOURCE_TYPE = "pwc/components/content/importer";
  private static final String CQ_CLIENT_LIBRARY_FOLDER = "cq:ClientLibraryFolder";
  private static final String NN_CANVAS = "canvas";
  private static final String PN_REIMPORT = "reimport";
  
  @Override
public List<Reference> findReferences(Resource resource)
  {
    List<Reference> references = new ArrayList();
    
    List<Resource> foundComponents = new LinkedList();
    findImporterComponent(resource, foundComponents);
    for (Resource importerComponent : foundComponents) {
      references.addAll(getReferences(importerComponent));
    }
    Collections.reverse(references);
    return references;
  }
  
  private List<Reference> getReferences(Resource importerComponent)
  {
    boolean hasCanvas = importerComponent.getChild(NN_CANVAS) != null;
    ValueMap properties = importerComponent.adaptTo(ValueMap.class);
    boolean isReimport = Boolean.TRUE.equals(properties.get(PN_REIMPORT));
    
    List<Reference> references = new LinkedList();
    if ((hasCanvas) && (!isReimport)) {
      try
      {
        references.addAll(getDesignReferences(importerComponent));
        references.addAll(getCanvasReferences(importerComponent));
      }
      catch (RepositoryException e)
      {
        logger.error("Error obtaining canvas references for the resource " + importerComponent.getPath(), e);
      }
    }
    return references;
  }
  
  private List<Reference> getDesignReferences(Resource importerComponent)
    throws RepositoryException
  {
    List<Reference> references = new LinkedList();
    
    ResourceResolver resourceResolver = importerComponent.getResourceResolver();
    PageManager pageManager = resourceResolver.adaptTo(PageManager.class);
    Page page = pageManager.getContainingPage(importerComponent);
    Designer designer = resourceResolver.adaptTo(Designer.class);
    String designPath = designer.getDesignPath(page);
    
    String canvasDesignPath = designPath + "/" + NN_CANVAS + importerComponent.getPath();
    Resource canvasDesign = resourceResolver.resolve(canvasDesignPath);
    addReferencesRecursive(canvasDesign, references);
    return references;
  }
  
  private List<Reference> getCanvasReferences(Resource importerComponent)
    throws RepositoryException
  {
    List<Reference> references = new LinkedList();
    Resource canvas = importerComponent.getChild(NN_CANVAS);
    String componentPath = "/apps/" + canvas.getResourceType();
    Resource component = canvas.getResourceResolver().resolve(componentPath);
    addReferencesRecursive(component, references);
    return references;
  }
  
  private void addReferencesRecursive(Resource root, List<Reference> references)
    throws RepositoryException
  {
    if (!isContentNode(root)) {
      if (isClientLibFolder(root)) {
        references.add(new Reference("artifact", root.getName(), root, -1L));
      } else {
        references.add(0, new Reference("artifact", root.getName(), root, -1L));
      }
    }
    Iterator<Resource> iter = root.listChildren();
    while (iter.hasNext()) {
      addReferencesRecursive(iter.next(), references);
    }
  }
  
  private boolean isContentNode(Resource r)
    throws RepositoryException
  {
    return "jcr:content".equals(r.getName());
  }
  
  private boolean isClientLibFolder(Resource r)
    throws RepositoryException
  {
    ValueMap properties = r.adaptTo(ValueMap.class);
    String nodeType = properties.get("jcr:primaryType", String.class);
    return CQ_CLIENT_LIBRARY_FOLDER.equals(nodeType);
  }
  
  private void findImporterComponent(Resource root, List<Resource> components)
  {
    if (IMPORTER_RESOURCE_TYPE.equals(root.getResourceType()))
    {
      components.add(root);
    }
    else
    {
      Iterator<Resource> childIterator = root.listChildren();
      while (childIterator.hasNext())
      {
        Resource child = childIterator.next();
        findImporterComponent(child, components);
      }
    }
  }
}
