package com.csaclient.provider.base;


public Login{
    private static OTAuthentication OT_AUTH;
    public Login(){

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



}