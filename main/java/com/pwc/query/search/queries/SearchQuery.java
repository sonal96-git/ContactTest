package com.pwc.query.search.queries;

import com.pwc.query.controllers.models.ControllerBean;
import com.pwc.query.search.factories.FiltersFactory;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.query.Query;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class SearchQuery {

	private long numberHits;
	protected final FiltersFactory filterFactory = new FiltersFactory();
	
	
	
	public abstract  List<Resource> getResults(ControllerBean contrBean) throws RepositoryException, IOException;
	public abstract String[] getPaths(ControllerBean contrBean);


	protected List<Resource> getResources(List<String> paths,ResourceResolver resolver) throws RepositoryException{
		
		List<Resource> resources =new ArrayList<Resource>();
		for (String path : paths) {
			Resource resource = resolver.getResource(path);
			resources.add(resource);
		}
		return resources;
		
	}


	protected List<String>  getQueryResults(Query query) throws RepositoryException {

		List<String> paths = new ArrayList<String>();

        NodeIterator searchResult = query.execute().getNodes();
        while (searchResult.hasNext()) {
            String resPath = "";

            Node nextItem = (Node)searchResult.next();
            resPath = nextItem.getPath();
            paths.add(resPath);

        }
		return paths;
	}

	// convert a string to array
	protected String[] convertToArray(String type) {

		String[] fileArray = new String[1];
		fileArray[0] = type;

		return fileArray;
	}


	public long getNumberHits() {
		return numberHits;
	}
	public void setNumberHits(long numberHits) {
		this.numberHits = numberHits;
	}
}
