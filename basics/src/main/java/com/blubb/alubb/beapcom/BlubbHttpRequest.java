package com.blubb.alubb.beapcom;

import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Created by Benjamin Richter on 22.05.2014.
 */
public class BlubbHttpRequest {

    public static String request(String url) {
        HttpClient httpclient = new DefaultHttpClient();
        HttpResponse response;
        String responseString = null;
        Log.i("httpRequest", "Starting request.");
        try {
            response = httpclient.execute(new HttpGet(url));
            StatusLine statusLine = response.getStatusLine();
            if(statusLine.getStatusCode() == HttpStatus.SC_OK){
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                response.getEntity().writeTo(out);
                out.close();
                responseString = out.toString();
            } else{
                //Closes the connection.
                response.getEntity().getContent().close();
                throw new IOException(statusLine.getReasonPhrase());
            }
        } catch (ClientProtocolException e) {
            Log.e("httpRequest", e.getMessage());
            responseString = handleHttpException(e);
        } catch (IOException e) {
            Log.e("httpRequest", e.getMessage());
            responseString = handleHttpException(e);

        }
        Log.i("http-Request", "received Response:\n" + responseString);
        return responseString;
    }

    private static String handleHttpException(Exception e) {
        String response =   "{\n" +
                "\"BeapStatus\" : 407,\n " +
                "\"StatusDescr\": " + e.getMessage() + ",\n " +
                "\"sessInfo\" : {\n" +
                "\"sessId\" : \"\",\n" +
                "\"sessUser\" : \"\",\n" +
                "\"sessRole\" : \"\",\n" +
                "\"sessActive\" : false,\n" +
                "\"expires\" : \"\"\n" +
                "}}{\n";
        return response;
    }

}
