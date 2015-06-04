package com.arcsolu.sopda.biz;

import java.io.Serializable;
import java.util.ArrayList;

import com.arcsolu.sopda.entity.Floor;
import com.arcsolu.sopda.entity.Table;

public interface BizTable extends Serializable{
	public boolean Save(ArrayList<Table> value);
	public boolean Delete(Table value);
	public boolean InsertTable(Table t);
	public Table CreateTable(Floor floor, String number);
}


