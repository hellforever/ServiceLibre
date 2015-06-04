package com.arcsolu.sopda.biz;

import java.io.Serializable;
import java.util.List;


import com.arcsolu.sopda.entity.Printer;
import com.arcsolu.sopda.entity.Script;

public interface BizPrinter extends Serializable{
	public List<Printer> Select();
	public boolean Test(Printer printer);
	public boolean UpdateScript(Script scpt);
	public boolean InsertPrinter(Printer p);
	public List<Script> SelectScript();
}
