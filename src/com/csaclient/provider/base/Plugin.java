/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.csaclient.provider.base;

import com.csaclient.provider.extended.logger.Logger;

/**
 *
 * @author bho
 */
public abstract class Plugin{
    public Plugin(Logger logger, String user, String password, boolean export){
        
    }
    
    public abstract void initPlugin();
}
