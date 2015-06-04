package com.arcsolu.sopda.biz;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import com.arcsolu.sopda.entity.Floor;

public interface BizFloor extends Serializable{
	public List<Floor> Select();
	public boolean Save(ArrayList<Floor> values);
	public boolean Delete(Floor value);
	
	public boolean InsertFloor(Floor f);
	public BizTable getBizTable();
}


