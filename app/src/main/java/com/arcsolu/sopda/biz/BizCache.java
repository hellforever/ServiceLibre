package com.arcsolu.sopda.biz;

import java.io.Serializable;
import java.util.*;

import com.arcsolu.sopda.entity.*;
import com.arcsolu.sopda.entity.Parametres.ParaKey;

final class BizCache implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static List<Menu> DeletedMenus = new ArrayList<Menu>();
	public static List<Floor> Floors = new ArrayList<Floor>();
	public static Map<ParaKey, String> Params = new HashMap<ParaKey, String>();
	public static List<Menu> Menus=new ArrayList<Menu>();
	public static List<Printer> Printers=new ArrayList<Printer>();
	public static List<User> Users=new ArrayList<User>();
	public static Map<ParaKey, String> FBParams = new HashMap<ParaKey, String>();
	public static List<Script> Scripts=new ArrayList<Script> ();
	public static List<Formule> FormulesAvailable=new ArrayList<Formule> ();
	public static List<Formule> Formules=new ArrayList<Formule> ();
	//public static List<Table> Tables=new ArrayList<Table>();
 	public BizCache(){
 	}
 

	

	/*public boolean GetDeletedMenus() throws SQLException, IOException{
		Statement ps = ImpBizApp.fd.con.createStatement();//创建语句对象
		//ImpBizOrder.fd.CommitTransAction();
		ResultSet rs  =  (ResultSet) ps.executeQuery("select * from menu");
		//ImpBizApp.fd.StartTransAction();
		Statement ps1 = ImpBizApp.fd.con.createStatement();//创建语句对象
		//ImpBizOrder.fd.CommitTransAction();
		ResultSet rs1  =  (ResultSet) ps1.executeQuery("select * from printer " +
				"inner join menu on  menu.kitchen_id=printer.kitchen_id");
		//ResultSet rsTables=(ResultSet) fd.con.getMetaData().getTables(null, "FBDB","Menu", new String[] {"Table"});
		while(rs.next()) {
			Menu item = new Menu();
			item.Id =  rs.getString("menu_id");
			item.NameDisplay =  rs.getString("menu_name_display");
			//item.Index= rs.getInt("menu_code");
			//item.Limit= rs.getInt("f_limit");
			item.Pic=SerializeUtil.serializeObject(rs.getBlob("menu_photo"));
			item.Catalog=rs.getString("menu_family_id");
			//item.Chef=rs.getBoolean("f_chef");
			//item.NEW=rs.getBoolean("f_new");
			//item.Top5=rs.getBoolean("f_Top5");
			while(rs1.next()){
				Printer printer=new Printer();
				if(rs1.getString("printer_name").equals(rs1.getString("menu_name_print"))){
					printer.Address.equals(rs1.getString("printer_address"));
					printer.Id.equals(rs1.getString("printer_id"));
					printer.Name.equals(rs1.getString("printer_name"));
					item.Printers.add(printer);
				}
				rs1.beforeFirst();
			}
			Menus.add(item);
		}
		return true;
	}*/

	
	
	/*@SuppressWarnings("unchecked")
	public boolean GetOrder(User user,Table table) throws SQLException{
		//ImpBizOrder.fd.StartTransAction();
		Order item = new Order(user, table);
		PreparedStatement ps = ImpBizOrder.fd.con.prepareStatement("select * from order_bill where operator_id='"+user.Id+" and rest_table_id='"+table.Id+"'");//创建语句对象
		PreparedStatement ps1 = ImpBizOrder.fd.con.prepareStatement("select * from order_detail inner join order_bill on  order_bill.order_id=order_detail.order_id");
		//ImpBizOrder.fd.CommitTransAction();
		ResultSet rs  =  (ResultSet) ps.executeQuery();
		ResultSet rs1  =  (ResultSet) ps1.executeQuery();
		while(rs.next()) {
			item.Id = rs.getString("order_id");	 
		}
				
          return true;
	}*/
	

	
	
	
	
	}
