import java.io.*;
import java.net.*;
class RequestProcessor extends Thread //for multi-threaded server
{
private Socket socket;
RequestProcessor(Socket socket) 
{
this.socket=socket;
start(); // will load the run method 
}
public void run() 
{
try
{
//Declaring properties and streams
OutputStream outputStream;
OutputStreamWriter outputStreamWriter;
InputStream inputStream;
InputStreamReader inputStreamReader;
StringBuffer stringBuffer;
String response;
String request;
int x;
int temp1,temp2;
String part1,part2,part3;
String fuelType;
String fuelAmount;
//getting input stream and its reader, for reading request or acknowledgement
inputStream=socket.getInputStream(); 
inputStreamReader=new InputStreamReader(inputStream);
stringBuffer=new StringBuffer();
while(true) 
{
x=inputStreamReader.read();
if(x=='#' || x==-1) break; //reads until terminator
stringBuffer.append((char)x); 
}
request=stringBuffer.toString();
System.out.println("Request : "+request);
//parsing and extracting Request data
temp1=request.indexOf(",");
temp2=request.indexOf(",",temp1+1);
part1=request.substring(0,temp1);
part2=request.substring(temp1+1);
fuelType=part1;
fuelAmount=part2;
System.out.println("fuelType : "+fuelType);
System.out.println("fuelAmount : "+fuelAmount);
 
// handle data
//sending response
response=fuelType + "," + fuelAmount + "," + "Data saved#";
//get output stream and its writer, for sending response or acknowledgement
outputStream=socket.getOutputStream();
outputStreamWriter=new OutputStreamWriter(outputStream);
outputStreamWriter.write(response);
outputStreamWriter.flush(); // response sent
System.out.println("Response sent");
socket.close(); //terminating connection
}catch(Exception exception)
{
System.out.println(exception);
}
}
}
class Server 
{
private ServerSocket serverSocket;
private int portNumber;
Server(int portNumber) 
{
this.portNumber=portNumber;
try
{
//Initiating ServerSocket with TCP port
serverSocket=new ServerSocket(this.portNumber); 
startListening(); 
}catch(Exception e) 
{
System.out.println(e);
System.exit(0); 
}
}
private void startListening() 
{
try
{
Socket socket;
while(true) 
{
System.out.println("Server is listening on port : "+this.portNumber);
socket=serverSocket.accept(); // server is in listening mode
System.out.println("Request arrived..");
// diverting the request to processor with the socket reference
new RequestProcessor(socket); 
}
}catch(Exception e)
{
System.out.println(e); 
}
}
public static void main(String data[]) 
{
int portNumber=Integer.parseInt(data[0]);
Server server=new Server(portNumber); 
}
}
