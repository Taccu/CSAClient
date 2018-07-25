/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.csaclient.provider.base;


/**
 *
 * @author bho
 */
public interface Plugin{
    
    public abstract void initPlugin();
    public abstract boolean startPlugin();
    public abstract boolean stopPlugin();
}
