/**
 * Класс, содержит SOAP методы сервера www.cbr.ru
 * А так же разбор полученых от сервера ответов.
 *
 * */

package ru.openitr.exinformerlib;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class DailyInfoStub {
    private static final String namespace = "http://web.cbr.ru/";
    private static final String url = "http://www.cbr.ru/DailyInfoWebServ/DailyInfo.asmx";
    //private static final String url = "http://www.cbr.ru/DailyInfoWebServ/DailyInfo.asmx"
    /**
     * Метод запрашиват курс валют на заданную дату
     * и разбирает ответ в ArrayList.
     * Вх. параметр onDate: Дата курса валют, тип - Data
     * Вых. параметр: массив курсов валют, тип ArrayList<Currency>
     *
     **/
    public ArrayList<Currency> getCursOnDate(Date onDate) throws Exception {
        ArrayList<Currency> result = new ArrayList<Currency>();
        String methodName = "GetCursOnDate";
        String soapAction = "http://web.cbr.ru/GetCursOnDate";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T00:00:00'");
        PropertyInfo onDateIn = new PropertyInfo();
        SoapObject request = new SoapObject(namespace, methodName);
        /* Устанавливаем параметры */
        onDateIn.setName("On_date");
        onDateIn.setValue(sdf.format(onDate));
        request.addProperty(onDateIn);
        /*Готовим запрос*/
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.implicitTypes = true;
        envelope.dotNet = true;

        envelope.setOutputSoapObject(request);
        HttpTransportSE androidHttpTransport = new HttpTransportSE(url);
        androidHttpTransport.debug = true;

        androidHttpTransport.setXmlVersionTag("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
        try {
            androidHttpTransport.call(soapAction,envelope);
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

        /* Разбор ответа сервера */
        try {
            SoapObject resultRequest = (SoapObject) envelope.bodyIn;
            SoapObject array = (SoapObject) resultRequest.getProperty(0);
            SoapObject propertyArray= (SoapObject)array.getProperty(1);
            SoapObject valCursArray = (SoapObject) propertyArray.getProperty(0);
            for (int i=0 ; i < valCursArray.getPropertyCount(); i++){
                SoapObject valCursArrayElement = (SoapObject) valCursArray.getProperty(i);
                String valName = valCursArrayElement.getProperty("Vname").toString();
                String valChCode = valCursArrayElement.getProperty("VchCode").toString();
                int valNom = Integer.parseInt((valCursArrayElement.getProperty("Vnom").toString()));
                float valCurs = Float.parseFloat(valCursArrayElement.getProperty("Vcurs").toString());
                int vCode = Integer.parseInt(valCursArrayElement.getProperty("Vcode").toString());
                result.add(new Currency(valName, valNom, valCurs, valChCode,vCode, onDate));
            }
        } catch (ArrayIndexOutOfBoundsException e){
            e.printStackTrace();
            throw e;
        } catch (Exception e){
            e.printStackTrace();
            throw e;
        }
        return result;
    }

}
