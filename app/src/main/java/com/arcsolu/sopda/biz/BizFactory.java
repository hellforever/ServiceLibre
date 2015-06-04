package com.arcsolu.sopda.biz;

import java.io.File;
import java.io.Serializable;

public final class BizFactory implements Serializable{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static BizFloor insFloor;
    private static BizOrder insOrder;
    private static BizTable insTable;
    private static BizApp insApp;
    private static BizPrinter insPrinter;
    private static BizMenu insMenu;
    private static BizOrderPrinter insBP;
    public static BizFloor getBizFloor()  {
        if(insFloor == null) {
            synchronized(BizFactory.class) {
                if(insFloor == null)
						insFloor = new ImpBizFloor();
				
            } 
        }
        return insFloor;
    }
    public static BizOrder getBizOrder() {
        if(insOrder == null) {
            synchronized(BizFactory.class) {
                if(insOrder == null)
                    insOrder = new ImpBizOrder();
            } 
        }
        return insOrder;
    }
    public static BizTable getBizTable()  {
        if(insTable == null) {
            synchronized(BizFactory.class) {
                if(insTable == null)
						insTable = new ImpBizTable();
					
            } 
        }
        return insTable;
    }
    public static BizApp getBizApp(File file) {
        if(insTable == null) {
            synchronized(BizFactory.class) {
                if(insApp== null){
                	insApp = new ImpBizApp();
                	insApp.SetFileDir(file);
                }
            } 
        }
        return insApp;
    }
    public static BizPrinter getBizPrinter(){
        if(insPrinter== null) {
            synchronized(BizFactory.class) {
                if(insPrinter == null)
					insPrinter= new ImpBizPrinter();
            } 
        }
        return insPrinter;
    }
    public static BizMenu getBizMenu() {
        if(insMenu== null) {
            synchronized(BizFactory.class) {
                if(insMenu == null)
					insMenu = new ImpBizMenu();
            } 
        }
        return insMenu;
    }
    public static BizOrderPrinter getBizOrderPrinter()  {
        if(insBP == null) {
            synchronized(BizFactory.class) {
                if(insBP == null)
						insBP = new ImpBizOrderPrinter();
				
            } 
        }
        return insBP;
    }
}