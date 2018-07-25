package com.csaclient.provider.base;

import com.csaclient.provider.extended.logger.Logger;
import com.opentext.ecm.api.OTAuthentication;
import com.opentext.livelink.service.classifications.Classifications;
import com.opentext.livelink.service.classifications.Classifications_Service;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import javax.xml.soap.SOAPHeaderElement;

public abstract class ContentServerTask extends Thread{
    public static final String SEARCH_API = "Livelink Search API V1.1";
    public final Logger logger;
    private final Timer timer;
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
    public ContentServerTask(Logger logger, boolean export){
        this.logger = logger;
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
            header = Login.getOTDSHeader();
            ((WSBindingProvider) docManClient).setOutboundHeaders(Headers.create(header));
            return docManClient;
        }
        catch(Exception e) {
            handleError(e);
        }
        return null;
    }
    public DocumentManagement getDocManClient(boolean force) {
        // Create the DocumentManagement service client
        try {
            DocumentManagement_Service docManService = new DocumentManagement_Service();
            DocumentManagement docManClient = docManService.getBasicHttpBindingDocumentManagement();
            SOAPHeaderElement header;
            header = Login.getOTDSHeader();
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
            header = Login.getOTDSHeader();
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
            header = Login.getOTDSHeader();
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
            header = Login.getOTDSHeader();
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