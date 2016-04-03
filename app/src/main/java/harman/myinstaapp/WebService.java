package harman.myinstaapp;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

public class WebService {

    //Namespace of the Webservice - can be found in WSDL
    private static String NAMESPACE = "http://soap_service.harman/";
    //Webservice URL - WSDL File location
    private static String URL = "http://192.168.0.104:4848/HelloWorldWebService/SayHelloService?WSDL";
    //SOAP Action URI again Namespace + Web method name
    private static String SOAP_ACTION = "http://soap_service.harman/";

    private static String METHOD_NAME = "processImage";

    public static String invokeWebService(String effect, byte[] byteArray) {
        String resTxt = null;
        // Create request
        SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
        // Property which holds input parameters
        PropertyInfo setEffectParam = new PropertyInfo();
        // Set Name
        setEffectParam.setName("effect");
        // Set Value
        setEffectParam.setValue(effect);

        request.addProperty("byteArray",byteArray);

        // Set dataType
        setEffectParam.setType(String.class);
        // Add the property to request object
        request.addProperty(setEffectParam);
        // Create envelope
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        // Set output SOAP object
        envelope.setOutputSoapObject(request);
        // Create HTTP call object
        HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);

        try {
            // Invoke web service
            androidHttpTransport.call(SOAP_ACTION+METHOD_NAME, envelope);
            // Get the response
            SoapPrimitive response = (SoapPrimitive) envelope.getResponse();
            // Assign it to resTxt variable static variable
            resTxt = response.toString();

        } catch (Exception e) {
            //Print error
            e.printStackTrace();
            //Assign error message to resTxt
            resTxt = "Error occured";
        }
        //Return resTxt to calling object
        return resTxt;
    }
}
