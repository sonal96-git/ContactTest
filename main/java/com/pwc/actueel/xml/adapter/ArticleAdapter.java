package com.pwc.actueel.xml.adapter;

import java.util.ArrayList;
import java.util.List;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.tagging.Tag;
import com.day.cq.tagging.TagManager;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.foundation.Image;
import com.pwc.actueel.xml.model.Article;

/**
 * Adapter to map the properties of a Page resource to an Article of an Actueel RSS Feed XML.
 */
public class ArticleAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(ArticleAdapter.class);
    private static final String TITLE_PROPERTY_NAME = "pageTitle";
    private static final String CATEGORIES_PROPERTY_NAME = "cq:tags";
    private static final String DESCRIPTION_PROPERTY_NAME = "jcr:description";
    private static final String PUBDATE_PROPERTY_NAME = "pwcReleaseDate";
    private static final String IMAGE_NODE_NAME = "image";
    private static final String PAGE_CONTENT_NODE_NAME = "jcr:content";
    /**
     * Returns a new {@link Article} object after mapping the required properties of a Page Resource.
     *
     * @param pageRes {@link Resource} The properties of this page resource will be added to the Article
     * @return {@link Article}
     */
    public Article adaptPageResourceToArticle(final Resource pageRes) {
        Article article = null;
        if (pageRes != null) {
            final Resource contentRes = pageRes.getChild(PAGE_CONTENT_NODE_NAME);
            if (contentRes != null) {
                final ValueMap properties = contentRes.adaptTo(ValueMap.class);
                final String pubDate = properties.get(PUBDATE_PROPERTY_NAME, "");
                final String title = properties.get(TITLE_PROPERTY_NAME, "");
                final String[] tags = properties.get(CATEGORIES_PROPERTY_NAME, new String[0]);
                final String description = properties.get(DESCRIPTION_PROPERTY_NAME, "");
                final String link = pageRes.getPath();
                final String image = getImageLinkFromPage(pageRes.adaptTo(Page.class));
                final List<String> categories = getCategoriesFromTags(pageRes.getResourceResolver(), tags);
                article = new Article(title, link, pubDate, description, categories, image);
                LOGGER.debug("Adapting Page resource at path " + link + " to Article: " + article.toString());
                return article;
            }
        }
        return article;
    }
    
    /**
     * Fetches the node named {@value #IMAGE_NODE_NAME} under the given page and returns the value of fileReference
     * property.
     *
     * @param page {@link Page}
     * @return {@link String} The image path of the image added to the page
     */
    private String getImageLinkFromPage(final Page page) {
        final Resource imageRes = page.getContentResource(IMAGE_NODE_NAME);
        return imageRes == null ? "" : new Image(imageRes).getFileReference();
    }
    
    /**
     * Returns a list of Tag Titles corresponding to each of the provided tags.
     *
     * @param resolver {@link ResourceResolver}
     * @param tags {@link String[]}
     * @return {@link List} List of Tag Titles
     */
    private List<String> getCategoriesFromTags(final ResourceResolver resolver, final String[] tags) {
        final List<String> categories = new ArrayList<String>();
        final TagManager tagManager = resolver.adaptTo(TagManager.class);
        for (final String tagId : tags) {
            final Tag tag = tagManager.resolve(tagId);
            if (tag != null) {
                categories.add(tag.getTitle());
            }
        }
        return categories;
    }
}
