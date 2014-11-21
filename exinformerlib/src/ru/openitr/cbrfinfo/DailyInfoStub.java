/**
 * Класс, содержит SOAP методы сервера www.cbr.ru
 * А так же разбор полученых от сервера ответов.
 *
 * */

package ru.openitr.cbrfinfo;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class DailyInfoStub {
    public static final Boolean DEBUG = true;
    private static final String namespace = "http://web.cbr.ru/";
    private static final String url = "http://www.cbr.ru/DailyInfoWebServ/DailyInfo.asmx";
    //private static final String url = "http://192.168.71.33:8080/DailyInfoWebServ/DailyInfo.asmx";
    private static final String LOG_TAG = "CBInfo";

    /**
     * Метод запрашиват курс валют на заданную дату
     * и разбирает ответ в ArrayList.
     * Вх. параметр onDate: Дата курса валют, тип - Data
     * Вых. параметр: массив курсов валют, тип ArrayList<Icurrency>
     */
    public ArrayList<Icurrency> getCursOnDate(Calendar onDate) throws Exception {
        ArrayList<Icurrency> result = new ArrayList<Icurrency>();
        String methodName = "GetCursOnDate";
        String soapAction = "http://web.cbr.ru/GetCursOnDate";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T00:00:00'");
        PropertyInfo onDateIn = new PropertyInfo();
        SoapObject request = new SoapObject(namespace, methodName);
        /* Устанавливаем параметры */
        onDateIn.setName("On_date");
        onDateIn.setValue(sdf.format(onDate.getTime()));
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
             LogSystem.logInFile (LOG_TAG, "InfoStub: Getting info from CB server on date "+ sdf.format(onDate.getTime()) + ".");
            androidHttpTransport.call(soapAction, envelope);
            LogSystem.logInFile(LOG_TAG, "InfoStub: Info recieved from SB server. ");

        } catch (IOException e) {
            e.printStackTrace();
             LogSystem.logInFile (LOG_TAG, "InfoStub: getCursOnDate IOException: "+e.getMessage());
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
             LogSystem.logInFile (LOG_TAG, "InfoStub: getCursOnDate Exception: "+e.getMessage());
            throw e;
        }

        /* Разбор ответа сервера */
         LogSystem.logInFile (LOG_TAG, "InfoStub: Begin data parsing.");
        try {
            SoapObject resultRequest = (SoapObject) envelope.bodyIn;
            SoapObject array = (SoapObject) resultRequest.getProperty(0);
            SoapObject propertyArray = (SoapObject) array.getProperty(1);
            SoapObject valCursArray = (SoapObject) propertyArray.getProperty(0);
            for (int i = 0; i < valCursArray.getPropertyCount(); i++) {
                SoapObject valCursArrayElement = (SoapObject) valCursArray.getProperty(i);
                String valName = valCursArrayElement.getProperty("Vname").toString();
                String valChCode = valCursArrayElement.getProperty("VchCode").toString();
                int valNom = Integer.parseInt((valCursArrayElement.getProperty("Vnom").toString()));
                float valCurs = Float.parseFloat(valCursArrayElement.getProperty("Vcurs").toString());
                int vCode = Integer.parseInt(valCursArrayElement.getProperty("Vcode").toString());
                valCurs = valCurs/valNom;
                result.add(new Icurrency(valName, valCurs, valChCode, vCode, onDate, 0));
            }

        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
             LogSystem.logInFile (LOG_TAG, "InfoStub: IOException: "+e.getMessage());
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
             LogSystem.logInFile (LOG_TAG, "InfoStub: Exception: "+e.getMessage());
            throw e;
        }
         LogSystem.logInFile (LOG_TAG, "InfoStub: Returning result data on date " + sdf.format(onDate.getTime()) + ".");
        return result;
    }


    /**
     * Метод запрашиват динамику курса валют на заданную дату и дату, днем ранее,
     * из этого делается вывод о направлении движения.
     * Результат помещается в ArrayList.
     * Вх. параметр onDate: Дата курса валют, тип - Data
     * Вых. параметр: массив курсов валют, тип ArrayList<Icurrency>
     */
    public ArrayList<Icurrency> getDynamicCursOnDate(Calendar onDate) throws Exception {
        ArrayList<Icurrency> result = new ArrayList<Icurrency>();
        String methodName = "GetCursOnDate";
        String soapAction = "http://web.cbr.ru/GetCursOnDate";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T00:00:00'");
        PropertyInfo onDateIn = new PropertyInfo();
        SoapObject request = new SoapObject(namespace, methodName);
        /* Устанавливаем параметры */
        onDateIn.setName("On_date");
        onDateIn.setValue(sdf.format(onDate.getTime()));
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
            LogSystem.logInFile (LOG_TAG, "InfoStub: Getting info from CB server on date "+ sdf.format(onDate.getTime()) + ".");
            androidHttpTransport.call(soapAction, envelope);
            LogSystem.logInFile(LOG_TAG, "InfoStub: Info recieved from SB server. ");

        } catch (IOException e) {
            e.printStackTrace();
            LogSystem.logInFile (LOG_TAG, "InfoStub: getCursOnDate IOException: "+e.getMessage());
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            LogSystem.logInFile (LOG_TAG, "InfoStub: getCursOnDate Exception: "+e.getMessage());
            throw e;
        }

        /* Разбор ответа сервера */
        LogSystem.logInFile (LOG_TAG, "InfoStub: Begin data parsing.");
        try {
            SoapObject resultRequest = (SoapObject) envelope.bodyIn;
            SoapObject array = (SoapObject) resultRequest.getProperty(0);
            SoapObject propertyArray = (SoapObject) array.getProperty(1);
            SoapObject valCursArray = (SoapObject) propertyArray.getProperty(0);
            for (int i = 0; i < valCursArray.getPropertyCount(); i++) {
                SoapObject valCursArrayElement = (SoapObject) valCursArray.getProperty(i);
                String valName = valCursArrayElement.getProperty("Vname").toString();
                String valChCode = valCursArrayElement.getProperty("VchCode").toString();
                int valNom = Integer.parseInt((valCursArrayElement.getProperty("Vnom").toString()));
                float valCurs = Float.parseFloat(valCursArrayElement.getProperty("Vcurs").toString());
                int vCode = Integer.parseInt(valCursArrayElement.getProperty("Vcode").toString());
                valCurs = valCurs/valNom;
                result.add(new Icurrency(valName, valCurs, valChCode, vCode, onDate, 0));
            }

        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
            LogSystem.logInFile (LOG_TAG, "InfoStub: IOException: "+e.getMessage());
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            LogSystem.logInFile (LOG_TAG, "InfoStub: Exception: "+e.getMessage());
            throw e;
        }
        LogSystem.logInFile (LOG_TAG, "InfoStub: Returning result data on date " + sdf.format(onDate.getTime()) + ".");
        return result;
    }



    public Calendar getLatestCurrencyDateFromServer() throws Exception {
        String soapAction = "http://web.cbr.ru/GetLatestDateTime";
        String methodName = "GetLatestDateTime";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'mm:hh:ss");
        SoapObject request = new SoapObject(namespace, methodName);

        /*Готовим запрос*/
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.implicitTypes = true;
        envelope.dotNet = true;
        envelope.setOutputSoapObject(request);
        HttpTransportSE androidHttpTransport = new HttpTransportSE(url);
        androidHttpTransport.debug = true;

        androidHttpTransport.setXmlVersionTag("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
        try {
             LogSystem.logInFile (LOG_TAG, "InfoStub: Getting GetLatestDate info from CB server.");
             androidHttpTransport.call(soapAction, envelope);
             LogSystem.logInFile(LOG_TAG, "InfoStub: Info getLatestCurrencyDate recieved from SB server. ");
        } catch (IOException e) {
             LogSystem.logInFile (LOG_TAG, "InfoStub: IOException: "+e.getMessage());
            e.printStackTrace();

            throw e;
        } catch (Exception e) {
             LogSystem.logInFile (LOG_TAG, "InfoStub: Exception: "+e.getMessage());
            e.printStackTrace();
            throw e;
        }
           /* Разбор ответа сервера */
         LogSystem.logInFile(LOG_TAG, "InfoStub: Begin data parsing.");
        try {
            SoapObject resultRequest = (SoapObject) envelope.bodyIn;
            String result = resultRequest.getPropertyAsString(0);
            sdf.parse(result);
            return sdf.getCalendar();
        } catch (Exception e) {
            e.printStackTrace();
             LogSystem.logInFile (LOG_TAG, "InfoStub: getLastDate Exception: "+e.getMessage());
            throw e;
        }
    }

    /**
     * Метод запрашиват цены драгметаллов на заданную дату
     * и разбирает ответ в ArrayList.
     * Вх. параметр onDate: Дата, тип - Data
     * Вых. параметр: массив объектов типа DragMetal
     */
    public ArrayList <DragMetal> getMetPrice(Calendar fromDate, Calendar toDate) throws Exception {
        ArrayList<DragMetal> result = new ArrayList<DragMetal>();
        String methodName = "DragMetDynamic";
        String soapAction = "http://web.cbr.ru/DragMetDynamic";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T00:00:00'");
        PropertyInfo fromDateIn = new PropertyInfo();
        PropertyInfo toDateIn = new PropertyInfo();
        SoapObject request = new SoapObject(namespace, methodName);

        /* Устанавливаем параметры */

        toDateIn.setName("ToDate");
        toDateIn.setValue(sdf.format(toDate.getTime()));

        fromDateIn.setName("fromDate");
        fromDateIn.setValue(sdf.format(fromDate.getTime()));

        request.addProperty(fromDateIn);
        request.addProperty(toDateIn);
    /*Готовим запрос*/
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.implicitTypes = true;
        envelope.dotNet = true;

        envelope.setOutputSoapObject(request);
        HttpTransportSE androidHttpTransport = new HttpTransportSE(url);
        androidHttpTransport.debug = true;

        androidHttpTransport.setXmlVersionTag("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
        try

        {
            LogSystem.logInFile(LOG_TAG, this, "Getting info from CB server on date " + sdf.format(fromDate.getTime()) + ".");
            androidHttpTransport.call(soapAction, envelope);
            LogSystem.logInFile(LOG_TAG, this,"Info recieved from SB server. ");
        } catch (
                IOException e
                )

        {
            e.printStackTrace();
            LogSystem.logInFile(LOG_TAG, this,"getCursOnDate IOException: " + e.getMessage());
            throw e;
        } catch (
                Exception e
                )

        {
            e.printStackTrace();
            LogSystem.logInFile(LOG_TAG, this,"getCursOnDate Exception: " + e.getMessage());
            throw e;
        }

        /* Разбор ответа сервера */

        LogSystem.logInFile(LOG_TAG, this,"Begin data parsing.");
        try

        {
            SoapObject resultRequest = (SoapObject) envelope.bodyIn;
            SoapObject array = (SoapObject) resultRequest.getProperty(0);
            SoapObject propertyArray = (SoapObject) array.getProperty(1);
            SoapObject dragMetArray = (SoapObject) propertyArray.getProperty(0);
            for (int i = 0; i < dragMetArray.getPropertyCount(); i++) {
                SoapObject valCursArrayElement = (SoapObject) dragMetArray.getProperty(i);
                String metDateString = valCursArrayElement.getProperty("DateMet").toString();
                int metCode = Integer.parseInt((valCursArrayElement.getProperty("CodMet").toString()));
                float metPrice = Float.parseFloat(valCursArrayElement.getProperty("price").toString());
                Calendar metPriceDate = Calendar.getInstance();
                metPriceDate.setTime(sdf.parse(metDateString));
                result.add(new DragMetal(metCode, metPrice, metPriceDate));
            }
        } catch (
                ArrayIndexOutOfBoundsException e
                )

        {
            e.printStackTrace();
            //LogSystem.logInFile(LOG_TAG, "InfoStub: IOException: " + e.getMessage());
            LogSystem.logInFile(LOG_TAG, this,"IOException: " + e.getMessage());
            throw e;
        } catch (
                Exception e
                )

        {
            e.printStackTrace();
            //LogSystem.logInFile(LOG_TAG, "InfoStub: Exception: " + e.getMessage());
            LogSystem.logInFile(LOG_TAG, this,"Exception: " + e.getMessage());
            throw e;
        }

        //LogSystem.logInFile(LOG_TAG, "InfoStub: Returning result data on date " + sdf.format(onDate.getTime()) + ".");
        LogSystem.logInFile(LOG_TAG, this,"Returning result data on date " + sdf.format(fromDate.getTime()) + ".");
        return result;
    }

    public Calendar getLatestMetalDateFromServer() {
        Calendar fromDate = Calendar.getInstance();
        Calendar toDate = Calendar.getInstance();
        fromDate.roll(Calendar.DAY_OF_YEAR, -4);
        toDate.roll(Calendar.DAY_OF_YEAR,2);
        try {
            ArrayList<DragMetal> metalPrices = getMetPrice(fromDate, toDate);
            return metalPrices.get(metalPrices.size()-1).getOnDate();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


}
