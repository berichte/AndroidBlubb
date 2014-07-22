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
 * Offers static functions to send a http request and receive the response.
 * Created by Benjamin Richter on 22.05.2014.
 */
public class BlubbHttpRequest {

    /**
     * Send a http request to a url and get the response string.
     * Exceptions will be packed in the response, e.g. ClientProtocolEx will be a valid
     * beap response with a connection error.
     *
     * @param url The url the request will be send to.
     * @return Response string for that request.
     */
    public static String request(String url) {
        HttpClient httpclient = new DefaultHttpClient();
        HttpResponse response;
        String responseString;
        Log.v("BlubbHttpRequest", "Starting request");
        try {
            response = httpclient.execute(new HttpGet(url));
            StatusLine statusLine = response.getStatusLine();
            if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                response.getEntity().writeTo(out);
                out.close();
                responseString = out.toString();
            } else {
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
        Log.v("http-Request", "received Response:\n" + responseString);
        return responseString;
    }

    /**
     * Builds a beap valid response string in case there is a exception thrown.
     *
     * @param e The exception occurred while executing a http request.
     * @return A beap response with beap status 407 - connection error.
     */
    private static String handleHttpException(Exception e) {
        return "{\n" +
                "\"BeapStatus\" : 407,\n " +
                "\"StatusDescr\": \"connection error " + e.getClass().getName() + "\",\n " +
                "\"sessInfo\" : {\n" +
                "\"sessId\" : \"\",\n" +
                "\"sessUser\" : \"\",\n" +
                "\"sessRole\" : \"\",\n" +
                "\"sessActive\" : false,\n" +
                "\"expires\" : \"\"\n" +
                "}}{\n";
    }

}
