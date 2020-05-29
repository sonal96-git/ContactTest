package com.pwc.workflow;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.io.FilenameUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.dam.api.Asset;
import com.day.cq.dam.api.Rendition;
import com.day.cq.dam.commons.process.AbstractAssetWorkflowProcess;
import com.day.cq.workflow.WorkflowException;
import com.day.cq.workflow.WorkflowSession;
import com.day.cq.workflow.exec.WorkItem;
import com.day.cq.workflow.exec.WorkflowProcess;
import com.day.cq.workflow.metadata.MetaDataMap;
import com.pwc.AdminResourceResolver;

@Component(service = WorkflowProcess.class, immediate = true, property = {
		"process.label=" + "Convet to PwC Video Object" })
public class PwCConvetToPwCVideoWorkflowImpl extends AbstractAssetWorkflowProcess {
	private static final String DC_FORMAT = "dc:format";

	private Logger log = LoggerFactory.getLogger(this.getClass());

	@Reference
	AdminResourceResolver adminResourceResolver;

	@Override
	public void execute(WorkItem workItem, WorkflowSession workflowSession, MetaDataMap metaData)
			throws WorkflowException {

		Session session = workflowSession.getSession();

		Asset asset = getAssetFromPayload(workItem, session);
		if (null != asset) {
			try {
				Node assetNode = asset.adaptTo(Node.class);

				Node metadata = assetNode.getNode("jcr:content/metadata");
				String assetName = FilenameUtils.removeExtension(assetNode.getName());
				Property formatProp = metadata.getProperty(DC_FORMAT);
				Rendition rendition = asset.getRendition("original");
				if (rendition != null && formatProp != null && formatProp.getString().contains("image/")) {
					metadata.setProperty("dc:title", assetName);
					metadata.setProperty(DC_FORMAT, "video/pwcvideo");
					assetNode.getSession().move(assetNode.getPath(),
					assetNode.getParent().getPath() + "/" + assetName);
					assetNode.getSession().save();
				}
			} catch (RepositoryException e) {
				log.error("execute: repository error while converting to PwC Video for asset [{}] in workflow ["
						+ workItem.getId() + "]", asset.getPath(), e);
			}
		} else {
			String wfPayload = workItem.getWorkflowData().getPayload().toString();
			String message = "execute: cannot convert to PwC Video, asset [{" + wfPayload
					+ "}] in payload doesn't exist for workflow [{" + workItem.getId() + "}].";
			throw new WorkflowException(message);
		}
	}

	@Override
	public Asset getAssetFromPayload(WorkItem workItem, Session session) {

		String payload = workItem.getWorkflowData().getPayload().toString().replace("/jcr:content/metadata", "");
		Asset asset = null;
		ResourceResolver adminResolver = adminResourceResolver.getAdminResourceResolver();
		if (adminResolver != null && !payload.isEmpty()) {
			Resource payloadResource = adminResolver.getResource(payload);
			asset = payloadResource.adaptTo(Asset.class);
		}

		return asset;
	}
}