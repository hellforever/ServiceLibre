package com.arcsolu.sopda.biz;

import com.arcsolu.sopda.entity.Formule;
import com.arcsolu.sopda.entity.Order;
import com.arcsolu.sopda.entity.Table;
import com.arcsolu.sopda.entity.User;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.Map;

public interface BizOrder extends Serializable {
    boolean SendOrder(Order order);

    boolean PrintOrder(Order order) throws SQLException;

    boolean SaveOrder(Order order);

    boolean CallService(Order order) throws SQLException;

    Order CreateOrder(User user, Table table, int customer, Map<Formule, Integer> formules) throws SQLException;

    String GetStartTime(Order order);

    String GetLastSendTime(Order order);
}
