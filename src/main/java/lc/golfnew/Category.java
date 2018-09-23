/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package lc.golfnew;

import java.io.Serializable;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import javax.enterprise.context.SessionScoped;
import javax.faces.model.SelectItem;
import javax.inject.Named; 

@Named("category")
@SessionScoped

public class Category implements Serializable {
 
    private static final long serialVersionUID = 1L;
    private String catname, subcatname;
 
    public String getCatname() {
        return catname;
    }
 
    public void setCatname(String catname) {
        this.catname = catname;
    }
 
    public String getSubcatname() {
        return subcatname;
    }
 
    public void setSubcatname(String subcatname) {
        this.subcatname = subcatname;
    }
 
    public List<SelectItem> getCategoryName() {
        List<SelectItem> cat = new ArrayList<SelectItem>();
        try {
            utils.DBConnection dbc = new utils.DBConnection();
            Connection conn = dbc.getConnection() ;
            Statement st = conn.createStatement();
            ResultSet rs = null;
            String myQuery = "select idclub, clubname from club ORDER BY clubname";
 
            rs = st.executeQuery(myQuery);
            while (rs.next()) {
                cat.add(new SelectItem(rs.getString("clubname")));
            }
 
        } catch (Exception ex) {
            ex.printStackTrace();
        }
 
        return cat;
    }
 
    @SuppressWarnings("unchecked")
    public List<SelectItem> getSubCategoryName() {
        List<SelectItem> subcat = new ArrayList<>();
        //if (catname != null && !catname.equals("")) {            
        try {
            utils.DBConnection dbc = new utils.DBConnection();
            Connection con = dbc.getConnection();
            Statement st = con.createStatement();
            ResultSet rs = null;
            String myQuery = "select idcourse, coursename from course"
                    + " where club_idclub = idclub = "
                    + "(select idclub from club where clubname = '" + catname + "')";
            //System.out.println(myQuery);
            rs = st.executeQuery(myQuery);
            while (rs.next()) {
                subcat.add(new SelectItem(rs.getString("coursename")));
            }
 
        } catch (Exception ex) {
        }
        // }
        return subcat;
    }
 
    public void processScat() {
        getSubCategoryName();
    }
}
