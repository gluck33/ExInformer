package ru.openitr.exinformerlib;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: oleg
 * Date: 10.12.12
 * Time: 8:41
 * To change this template use File | Settings | File Templates.
 */
public class Currency {
    private String vName;   // Наименование валюты.
    private int vNom;     // Номинал.
    private Float vCurs;    // Курс.
    private String vchCode; // Код валюты.
    private int vCode; // Внутренний код валюты.
    private Date vDate; // Дата курса.

    public Currency() {
        this.vName = "";
        this.vNom = 0;
        this.vCurs = 0f;
        this.vchCode = "";
        this.vCode = 0;
    }


    public Currency(String vName, int vNom, Float vCurs, String vchCode, int vCode, Date vDate) {
        this.vName = vName;
        this.vNom = vNom;
        this.vCurs = vCurs;
        this.vchCode = vchCode;
        this.vCode = vCode;
        this.vDate = vDate;
    }

    public String getvName(){
        return this.vName;
    }
    public int getvNom() {
        return this.vNom;
    }
    public Float getvCurs(){
        return this.vCurs;
    }
    public String getVchCode(){
        return this.vchCode;
    }
    public void setvName(String vName){
        this.vName = vName;
    }

    public void setvNom(int vNom){
        this.vNom = vNom;
    }
    public void setvCurs(Float vCurs){
        this.vCurs = vCurs;
    }
    public void setVchCode (String vchCode){
        this.vchCode = vchCode;
    }

    public int getvCode() {
        return vCode;
    }

    public void setvCode(int vCode) {
        this.vCode = vCode;
    }

    @Override
    // TODO Продумать и переделать toString
    public String toString(){
      return String.format("%s %s %s %s", this.vchCode, this.vNom, this.vCurs, this.vName);
    }

    public String vNomAsString(){
        return Integer.toString(this.vNom);
    }

    public String vCursAsString(){
        return Float.toString(this.vCurs);
    }

    public Date getvDate() {
        return vDate;
    }

    public void setvDate(Date vDate) {
        this.vDate = vDate;
    }

    public String vDateAsString(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String dat = sdf.format(this.vDate);
        return dat;
    }
}
