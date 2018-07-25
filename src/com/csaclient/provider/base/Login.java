package com.csaclient.provider.base;

import com.opentext.ecm.api.OTAuthentication;
import com.opentext.livelink.service.core.Authentication;
import com.opentext.livelink.service.core.Authentication_Service;
import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;


public class Login{
    private static OTAuthentication OT_AUTH;
    private static volatile String AUTH_USER, AUTH_PASSWORD;
    public Login(){

    }
    private static OTAuthentication login() {
        String authToken;
        Authentication_Service authService = new Authentication_Service();
        Authentication authClient = authService.getBasicHttpBindingAuthentication();
        authToken = authClient.authenticateUser(AUTH_USER , AUTH_PASSWORD);
        // Create the OTAuthentication object and set the authentication token
        OT_AUTH.setAuthenticationToken(authToken);
        return OT_AUTH;
    }

    public static OTAuthentication doLogin() {
        return checkLogin() ? OT_AUTH : login();
    }
    
    public static OTAuthentication doLogin(String user, String password) {
        AUTH_USER = user;
        AUTH_PASSWORD = password;
        return login();
    }
    
    public static SOAPHeaderElement getOTDSHeader() throws SOAPException {
        return generateSOAPHeaderElement(doLogin());
    }
    
    private static boolean checkLogin(){
        return !checkSessionTimeout();
    }
    
    private static boolean checkSessionTimeout(){
        return true;
    }
    
    public boolean hasValidSession() {
        return checkLogin();
    }
    
    private static SOAPHeaderElement generateSOAPHeaderElement(OTAuthentication oauth) throws SOAPException {
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
}