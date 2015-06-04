package com.arcsolu.sopda.biz;

import java.io.Serializable;
import java.util.List;

import com.arcsolu.sopda.entity.Formule;
import com.arcsolu.sopda.entity.Menu;

public interface BizMenu extends Serializable{
	public List<Menu> Select() ;
	public List<Menu> SelectAvailableMenu() ;
	public boolean Delete(Menu m) ;
	public boolean Undelete(Menu m);
	public boolean EmptyTrush();
	public boolean Save(List<Menu> m);
	public List<Menu> SelectDeletedMenus();
	public Menu GetMenu();
	//public boolean InsertMenu(Menu m);
	public List<Formule> SelectFormule();
	public boolean setFormule(Formule fml);
	public boolean SaveAvailableFormules(List<Formule> m);
	public List<Formule> SelectAvailableFormule();
	public boolean removeFormule(Formule fml);
	public List<String> getAvailableFormules();

}
