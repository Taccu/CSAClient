package com.csaclient.provider.base;

import com.csaclient.provider.extended.logger.Logger;
import com.opentext.ecm.api.OTAuthentication;
import com.opentext.livelink.service.classifications.Classifications;
import com.opentext.livelink.service.classifications.Classifications_Service;
import com.opentext.livelink.service.core.Authentication;
import com.opentext.livelink.service.core.Authentication_Service;
import com.opentext.livelink.service.core.ChunkedOperationContext;
import com.opentext.livelink.service.docman.CategoryItemsUpgradeInfo;
import com.opentext.livelink.service.docman.DocumentManagement;
import com.opentext.livelink.service.docman.DocumentManagement_Service;
import com.opentext.livelink.service.docman.Node;
import com.opentext.livelink.service.docman.NodeRightUpdateInfo;
import com.opentext.livelink.service.docman.RightOperation;
import com.opentext.livelink.service.docman.RightPropagation;
import com.opentext.livelink.service.memberservice.MemberService;
import com.opentext.livelink.service.memberservice.MemberService_Service;
import com.opentext.livelink.service.searchservices.SearchService;
import com.opentext.livelink.service.searchservices.SearchService_Service;
import com.sun.xml.ws.api.message.Headers;
import com.sun.xml.ws.developer.WSBindingProvider;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;

public abstract class ContentServerTask extends Thread{
    public static final String SEARCH_API = "Livelink Search API V1.1";
    private static final OTAuthentication OT_AUTH = new OTAuthentication();
    public final Logger logger;
    private final Timer timer;
    private final String user, password;
    private int processedItems = 0;
    public final boolean export;
    public final ArrayList<Long> exportIds = new ArrayList<>();
    public static Connection CONNECTION;
    public static String URL;
    /**
     *
     * @param logger
     * @param user
     * @param password
     */
    public ContentServerTask(Logger logger, String user, String password, boolean export){
        this.logger = logger;
        this.user = user;
        this.password = password;
        this.export = export;
        timer = new Timer();
    }
        
    public void setProcessedItems(int items) {
        processedItems = items;
    }
    
    protected void handleError(Exception e) {
        logger.error(e.getMessage());
        e.printStackTrace();
        logger.debug("Interrupting thread...");
        Thread.currentThread().interrupt();
    }

   
    

    
    protected void applyRights(DocumentManagement docManClient, Node from, Node to) {
        logger.info("Setting node rights from node " + from.getName() + "(id:" + from.getID() +")" + " to node " + to.getName() + "(id:" + to.getID() + ")");
        docManClient.setNodeRights(to.getID(), docManClient.getNodeRights(from.getID()));
        
    }
    
    protected void inheritRights(DocumentManagement docManClient, Node from){
        logger.info("Inheriting node right from node "+ from.getName() + "(id:" + from.getID() +")" );
        ChunkedOperationContext updateNodeRightsContext = docManClient.updateNodeRightsContext(from.getID(), RightOperation.ADD_REPLACE, docManClient.getNodeRights(from.getID()).getACLRights(), RightPropagation.TARGET_AND_CHILDREN);
        updateNodeRightsContext.setChunkSize(1);
        try {
            NodeRightUpdateInfo chunkIt = chunkIt(docManClient.updateNodeRights(updateNodeRightsContext),updateNodeRightsContext);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    protected CategoryItemsUpgradeInfo chunkIt(CategoryItemsUpgradeInfo nrui){
        try {
        if(nrui.getUpgradedCount() > 0 ) {
            logger.debug("Updated " + nrui.getUpgradedCount() + " items...");
            DocumentManagement docManClient = getDocManClient();
            ChunkedOperationContext context = nrui.getContext();
            context.setChunkSize(200);
            chunkIt(docManClient.upgradeCategoryItems(context));
        }
        } catch(Exception e) {
            e.printStackTrace();
        }
            return nrui;
    }
    
    protected NodeRightUpdateInfo chunkIt(NodeRightUpdateInfo nrui, ChunkedOperationContext context){
        if(!context.isFinished()) {
            logger.debug("Updated " + nrui.getTotalNodeCount() + " items...");
            DocumentManagement docManClient = getDocManClient();
            context.setChunkSize(1);
            chunkIt(docManClient.updateNodeRights(context), context);
            
        }
        return nrui;
    }
    public SOAPHeaderElement generateSOAPHeaderElement(OTAuthentication oauth) throws SOAPException {
        // The namespace of the OTAuthentication object
        final String ECM_API_NAMESPACE = "urn:api.ecm.opentext.com";

        // Create a SOAP header
        SOAPHeader header = MessageFactory.newInstance().createMessage().getSOAPPart().getEnvelope().getHeader();

        if(header == null) {throw new SOAPException("Header was null");}
        // Add the OTAuthentication SOAP header element
        SOAPHeaderElement otAuthElement = header.addHeaderElement(new QName(ECM_API_NAMESPACE, "OTAuthentication"));

        // Add the AuthenticationToken SOAP element
        SOAPElement authTokenElement = otAuthElement.addChildElement(new QName(ECM_API_NAMESPACE, "AuthenticationToken"));

        authTokenElement.addTextNode(oauth.getAuthenticationToken());
        return otAuthElement;
    }
    
    public OTAuthentication loginUserWithPassword(String user, String password) {
        String authToken;
        try {
            if(OT_AUTH.getAuthenticationToken() == null || OT_AUTH.getAuthenticationToken().isEmpty()) {
                Authentication_Service authService = new Authentication_Service();
                Authentication authClient = authService.getBasicHttpBindingAuthentication();
                authToken = authClient.authenticateUser(user , password);
                // Create the OTAuthentication object and set the authentication token
                OT_AUTH.setAuthenticationToken(authToken);
            }
        } catch (Exception e) {
            handleError(e);
        }
        return OT_AUTH;
    }
    
    public static void writeArrayToPath(List<Long> list, Path path) throws IOException {
        List<String> arrayList = new ArrayList<>(list.size());
        list.forEach((myLong) -> {
            arrayList.add(String.valueOf(myLong));
        });
        Files.write(path,arrayList,Charset.defaultCharset());
    }  
    
    public DocumentManagement getDocManClient() {
        // Create the DocumentManagement service client
        try {
            DocumentManagement_Service docManService = new DocumentManagement_Service();
            DocumentManagement docManClient = docManService.getBasicHttpBindingDocumentManagement();
            SOAPHeaderElement header;
            header = generateSOAPHeaderElement(loginUserWithPassword(user, password));
            ((WSBindingProvider) docManClient).setOutboundHeaders(Headers.create(header));
            return docManClient;
        }
        catch(Exception e) {
            handleError(e);
        }
        return null;
    }
    
    public MemberService getMsClient() {
        try {
            MemberService_Service memServService = new MemberService_Service();
            MemberService msClient = memServService.getBasicHttpBindingMemberService();
            SOAPHeaderElement header;
            header = generateSOAPHeaderElement(loginUserWithPassword(user, password));
            ((WSBindingProvider) msClient).setOutboundHeaders(Headers.create(header));
            return msClient;
        } catch(Exception e) {
            handleError(e);
        }
        return null;
    }
    
    public Classifications getClassifyClient() {
        // Create the DocumentManagement service client
        try {
            Classifications_Service docManService = new Classifications_Service();
            Classifications docManClient = docManService.getBasicHttpBindingClassifications();
            SOAPHeaderElement header;
            header = generateSOAPHeaderElement(loginUserWithPassword(user, password));
            ((WSBindingProvider) docManClient).setOutboundHeaders(Headers.create(header));
            return docManClient;
        }
        catch(Exception e) {
            handleError(e);
        }
        return null;
    }
    
    public SearchService getSearchClient(){
        try {
            SearchService_Service seServService = new SearchService_Service();
            
            SearchService seClient = seServService.getBasicHttpBindingSearchService();
            SOAPHeaderElement header;
            header = generateSOAPHeaderElement(loginUserWithPassword(user, password));
            ((WSBindingProvider) seClient).setOutboundHeaders(Headers.create(header));
            return seClient;
        }
        catch(Exception e){
            handleError(e);
        }
        return null;
    }
    
    
    @Override
    public void run() {
        logger.info("Starting...");
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                logger.info("Be patient, we are still updating...");
            }
            }, 1*30*1000, 1*60*1000);
        
        long startTime = System.currentTimeMillis();
        try {
            doWork();
            if(export && exportIds.size() > 0) {
                try{
                    writeArrayToPath(exportIds,Paths.get(getNameOfTask()+".txt"));
                } catch(IOException ex) {
                    logger.error("Couldn't write " + getNameOfTask() + ".txt" );
                    logger.error(ex.getMessage());
                }
            }
        }catch(Exception e) {
            logger.error(e.getMessage());
        } finally {
            timer.cancel();
            timer.purge();
            long stopTime = System.currentTimeMillis();
            long elapsedTime = stopTime - startTime;
            if(processedItems > 0) {
                logger.info("Finished task " + getNameOfTask() + " with " + processedItems +" items in " + elapsedTime + " milliseconds...");
            } else {
                logger.info("Finished task " + getNameOfTask() + " in " + elapsedTime + " milliseconds...");
            }
        }
    }
    
    public abstract void doWork() throws InterruptedException;
    public abstract String getNameOfTask();
}