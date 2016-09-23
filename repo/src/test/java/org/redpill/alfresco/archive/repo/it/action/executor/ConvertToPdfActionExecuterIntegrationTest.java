package org.redpill.alfresco.archive.repo.it.action.executor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.alfresco.model.ContentModel;
import org.alfresco.model.RenditionModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.audit.AuditQueryParameters;
import org.alfresco.service.cmr.audit.AuditService;
import org.alfresco.service.cmr.audit.AuditService.AuditQueryCallback;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.junit.Test;
import org.redpill.alfresco.archive.repo.action.executor.ConvertToPdfActionExecuter;
import org.redpill.alfresco.test.AbstractRepoIntegrationTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import static org.junit.Assert.*;
import static org.junit.Assert.assertNotNull;
public class ConvertToPdfActionExecuterIntegrationTest extends AbstractRepoIntegrationTest {

  private static final String RENDITION_NAME_PDF = "pdf";
  private static final String RENDITION_NAME_PDFA = "pdfa";

  private static final String DEFAULT_USERNAME = "testuser_" + System.currentTimeMillis();

  private static SiteInfo site;

  @Autowired
  @Qualifier("ActionService")
  private ActionService actionService;

  @Autowired
  @Qualifier("AuditService")
  private AuditService auditService;

  @Override
  public void beforeClassSetup() {
    super.beforeClassSetup();

    createUser(DEFAULT_USERNAME);

    _authenticationComponent.setCurrentUser(DEFAULT_USERNAME);

    site = createSite();
  }

  @Test
  public void testConvertOdtToPdf() throws InterruptedException {

    NodeRef document = uploadDocument(site, "test.docx", null, null, "test" + System.currentTimeMillis() + ".docx").getNodeRef();

    //Make sure we have an odt document. For some reason Alfresco does not seem to detect it automatically. Instead it recognizes an application/zip.
    //ContentData cd = (ContentData) _nodeService.getProperty(document, ContentModel.PROP_CONTENT);
    //ContentData newCD = ContentData.setMimetype(cd, MimetypeMap.MIMETYPE_OPENDOCUMENT_TEXT);
    //_nodeService.setProperty(document, ContentModel.PROP_CONTENT, newCD);
    Action action = actionService.createAction(ConvertToPdfActionExecuter.NAME);
    action.setParameterValue(ConvertToPdfActionExecuter.PARAM_MIME_TYPE, MimetypeMap.MIMETYPE_PDF);
    action.setParameterValue(ConvertToPdfActionExecuter.PARAM_DESTINATION_FOLDER, document);
    action.setParameterValue(ConvertToPdfActionExecuter.PARAM_ASSOC_TYPE_QNAME, RenditionModel.ASSOC_RENDITION);
    QName renditionQName = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) RENDITION_NAME_PDF);
    action.setParameterValue(ConvertToPdfActionExecuter.PARAM_ASSOC_QNAME, renditionQName);
    action.setParameterValue(ConvertToPdfActionExecuter.PARAM_TARGET_NAME, RENDITION_NAME_PDF);
    action.setParameterValue(ConvertToPdfActionExecuter.PARAM_ADD_EXTENSION, true);
    action.setParameterValue(ConvertToPdfActionExecuter.PARAM_OVERWRITE_COPY, false);

    actionService.executeAction(action, document);
    List<ChildAssociationRef> childAssocs = _nodeService.getChildAssocs(document);
    assertNotNull(childAssocs);
    assertEquals(1, childAssocs.size());
    ChildAssociationRef childNode = childAssocs.get(0);
    NodeRef childNodeRef = childNode.getChildRef();
    assertNotNull(childNodeRef);
    //Assert that there is a child node with name pdf.pdf
    assertEquals("pdf.pdf", _nodeService.getProperty(childNodeRef, ContentModel.PROP_NAME));
  }

  @Test
  public void testConvertPdfToPdfa() throws InterruptedException {
    NodeRef document = uploadDocument(site, "test.pdf", null, null, "test" + System.currentTimeMillis() + ".pdf").getNodeRef();

    Action action = actionService.createAction(ConvertToPdfActionExecuter.NAME);
    action.setParameterValue(ConvertToPdfActionExecuter.PARAM_MIME_TYPE, ConvertToPdfActionExecuter.FAKE_MIMETYPE_PDFA);
    action.setParameterValue(ConvertToPdfActionExecuter.PARAM_DESTINATION_FOLDER, document);
    action.setParameterValue(ConvertToPdfActionExecuter.PARAM_ASSOC_TYPE_QNAME, RenditionModel.ASSOC_RENDITION);
    QName renditionQName = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) RENDITION_NAME_PDFA);
    action.setParameterValue(ConvertToPdfActionExecuter.PARAM_ASSOC_QNAME, renditionQName);
    action.setParameterValue(ConvertToPdfActionExecuter.PARAM_TARGET_NAME, RENDITION_NAME_PDFA);
    action.setParameterValue(ConvertToPdfActionExecuter.PARAM_ADD_EXTENSION, false);
    action.setParameterValue(ConvertToPdfActionExecuter.PARAM_OVERWRITE_COPY, false);

    actionService.executeAction(action, document);

    List<ChildAssociationRef> childAssocs = _nodeService.getChildAssocs(document);
    assertNotNull(childAssocs);
    assertEquals(1, childAssocs.size());
    ChildAssociationRef childNode = childAssocs.get(0);
    NodeRef childNodeRef = childNode.getChildRef();
    assertNotNull(childNodeRef);
    //Assert that there is a child node with name pdfa
    assertEquals("pdfa", _nodeService.getProperty(childNodeRef, ContentModel.PROP_NAME));
  }

  @Test
  public void testConvertPdfToPdfaUsingNames() throws InterruptedException {
    NodeRef document = uploadDocument(site, "test.pdf", null, null, "test" + System.currentTimeMillis() + ".pdf").getNodeRef();

    Action action = actionService.createAction(ConvertToPdfActionExecuter.NAME);
    action.setParameterValue(ConvertToPdfActionExecuter.PARAM_MIME_TYPE, ConvertToPdfActionExecuter.FAKE_MIMETYPE_PDFA);
    action.setParameterValue(ConvertToPdfActionExecuter.PARAM_DESTINATION_FOLDER, document);
    action.setParameterValue(ConvertToPdfActionExecuter.PARAM_ASSOC_TYPE_QNAME, RenditionModel.ASSOC_RENDITION);
    QName renditionQName = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) RENDITION_NAME_PDF);
    action.setParameterValue(ConvertToPdfActionExecuter.PARAM_ASSOC_QNAME, renditionQName);
    action.setParameterValue(ConvertToPdfActionExecuter.PARAM_TARGET_NAME, RENDITION_NAME_PDF);
    action.setParameterValue(ConvertToPdfActionExecuter.PARAM_ADD_EXTENSION, false);
    action.setParameterValue(ConvertToPdfActionExecuter.PARAM_OVERWRITE_COPY, false);

    actionService.executeAction(action, document);

    List<ChildAssociationRef> childAssocs = _nodeService.getChildAssocs(document);
    assertNotNull(childAssocs);
    assertEquals(1, childAssocs.size());
    ChildAssociationRef childNode = childAssocs.get(0);
    NodeRef childNodeRef = childNode.getChildRef();
    assertNotNull(childNodeRef);
    //Assert that there is a child node with name pdfa
    assertEquals(RENDITION_NAME_PDF, _nodeService.getProperty(childNodeRef, ContentModel.PROP_NAME));

    action = actionService.createAction(ConvertToPdfActionExecuter.NAME);
    action.setParameterValue(ConvertToPdfActionExecuter.PARAM_MIME_TYPE, ConvertToPdfActionExecuter.FAKE_MIMETYPE_PDFA);
    action.setParameterValue(ConvertToPdfActionExecuter.PARAM_DESTINATION_FOLDER, document);
    action.setParameterValue(ConvertToPdfActionExecuter.PARAM_ASSOC_TYPE_QNAME, RenditionModel.ASSOC_RENDITION);
    renditionQName = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) RENDITION_NAME_PDFA);
    action.setParameterValue(ConvertToPdfActionExecuter.PARAM_ASSOC_QNAME, renditionQName);
    action.setParameterValue(ConvertToPdfActionExecuter.PARAM_TARGET_NAME, RENDITION_NAME_PDFA);
    action.setParameterValue(ConvertToPdfActionExecuter.PARAM_ADD_EXTENSION, false);
    action.setParameterValue(ConvertToPdfActionExecuter.PARAM_OVERWRITE_COPY, false);
    action.setParameterValue(ConvertToPdfActionExecuter.PARAM_SOURCE_FILENAME, RENDITION_NAME_PDF);
    action.setParameterValue(ConvertToPdfActionExecuter.PARAM_SOURCE_FOLDER, document);

    actionService.executeAction(action, childNodeRef);

    childAssocs = _nodeService.getChildAssocs(document);
    assertNotNull(childAssocs);
    assertEquals(2, childAssocs.size());
    childNode = childAssocs.get(0);
    childNodeRef = childNode.getChildRef();
    assertNotNull(childNodeRef);
    //Assert that there is a child node with name pdfa
    assertEquals(RENDITION_NAME_PDF, _nodeService.getProperty(childNodeRef, ContentModel.PROP_NAME));
    childNode = childAssocs.get(1);
    childNodeRef = childNode.getChildRef();
    assertNotNull(childNodeRef);
    //Assert that there is a child node with name pdfa
    assertEquals(RENDITION_NAME_PDFA, _nodeService.getProperty(childNodeRef, ContentModel.PROP_NAME));

  }

  @Test
  public void testActionAuditSuccess() {
    NodeRef document = uploadDocument(site, "test.pdf", null, null, "test" + System.currentTimeMillis() + ".pdf").getNodeRef();

    Action action = actionService.createAction(ConvertToPdfActionExecuter.NAME);
    action.setParameterValue(ConvertToPdfActionExecuter.PARAM_MIME_TYPE, ConvertToPdfActionExecuter.FAKE_MIMETYPE_PDFA);
    action.setParameterValue(ConvertToPdfActionExecuter.PARAM_DESTINATION_FOLDER, document);
    action.setParameterValue(ConvertToPdfActionExecuter.PARAM_ASSOC_TYPE_QNAME, RenditionModel.ASSOC_RENDITION);
    QName renditionQName = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) RENDITION_NAME_PDF);
    action.setParameterValue(ConvertToPdfActionExecuter.PARAM_ASSOC_QNAME, renditionQName);
    action.setParameterValue(ConvertToPdfActionExecuter.PARAM_TARGET_NAME, RENDITION_NAME_PDF);
    action.setParameterValue(ConvertToPdfActionExecuter.PARAM_ADD_EXTENSION, false);
    action.setParameterValue(ConvertToPdfActionExecuter.PARAM_OVERWRITE_COPY, false);

    actionService.executeAction(action, document);
    final List<Boolean> assertList = new ArrayList<>();
    AuditQueryCallback callback = new AuditQueryCallback() {

      @Override
      public boolean valuesRequired() {
        return true;
      }

      @Override
      public boolean handleAuditEntry(Long entryId, String applicationName, String user, long time, Map<String, Serializable> values) {
        assertList.add(new Boolean(true));
        System.out.println("-----------------------------");
        if (values != null) {
          System.out.println("Not empty");
//values.containsKey("/action/archive-toolkit-transform-to-pdf/node");
  //      System.out.println(values.get("/action/archive-toolkit-transform-to-pdf/node"));
          for (String key : values.keySet()) {
            System.out.println(key + ": " + values.get(key));
          }
        } else {
          System.out.println("Empty set of audit values");
        }
        System.out.println("-----------------------------");

        return true;
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
      }

      @Override
      public boolean handleAuditEntryError(Long entryId, String errorMsg, Throwable error) {
        assertTrue("Did not expect any errors while processing audit records", false);
        return false;
      }
    };
    AuditQueryParameters parameters = new AuditQueryParameters();
    parameters.setApplicationName(ConvertToPdfActionExecuter.AUDIT_APPLICATION_NAME);
    parameters.addSearchKey("/alfresco-archive-toolkit/action/archive-toolkit-transform-to-pdf/node", document);
    String oldUserName = _authenticationComponent.getCurrentUserName();
    _authenticationComponent.clearCurrentSecurityContext();
    _authenticationComponent.setCurrentUser(_authenticationComponent.getSystemUserName());
    auditService.auditQuery(callback, parameters, 0);
    _authenticationComponent.clearCurrentSecurityContext();
    _authenticationComponent.setCurrentUser(oldUserName);

    assertEquals("Expected one audit record", 2, assertList.size());
  }

  @Override
  public void afterClassSetup() {
    deleteSite(site);

    _authenticationComponent.setCurrentUser(_authenticationComponent.getSystemUserName());

    deleteUser(DEFAULT_USERNAME);

    _authenticationComponent.clearCurrentSecurityContext();
  }
}
