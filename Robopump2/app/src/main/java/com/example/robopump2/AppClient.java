package com.example.robopump2;

import java.io.*;
import java.net.*;
public class AppClient
{
    public static void connect(String request)
    {
        // Request string must match the format expected by server
        //String request=rollNumber+","+name+","+gender+"#";
        //"#" acts as a terminator
        try
        {
            // 10.0.2.2 is simulators host machines ip in Android
            Socket socket = new Socket("10.0.2.2" , 5050);
            // Socket is initialized and attempt is made for connecting to the         server
            // Declaring other properties and streams
            OutputStream outputStream;
            OutputStreamWriter outputStreamWriter;
            InputStream inputStream;
            InputStreamReader inputStreamReader;
            StringBuffer stringBuffer;
            String response;
            int x;
            // retrieving output Stream and its writer, for sending request or acknowledgement
            outputStream=socket.getOutputStream();
            outputStreamWriter=new OutputStreamWriter(outputStream);
            outputStreamWriter.write(request);
            outputStreamWriter.flush(); // request is sent
            // retrieving input stream and its reader, for receiving    acknowledgement or response
            inputStream=socket.getInputStream();
            inputStreamReader=new InputStreamReader(inputStream);
            stringBuffer=new StringBuffer();
            while(true)
            {
                x=inputStreamReader.read();
                if(x=='#' || x==-1) break; // reads till the terminator
                stringBuffer.append((char)x);
            }
            response=stringBuffer.toString();
            System.out.println(response);
            socket.close(); //closing the connection
        }catch(Exception exception)
        {
        // Raised in case, connection is refused or some other technical issue
            System.out.println(exception);
        }
    }
}