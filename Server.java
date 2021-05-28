import java.io.*;
import java.net.*;
import java.util.*;
import javax.activation.*;
import java.nio.file.*;
import com.google.gson.*;
class Model 
{
private List<Board> boards;
private HashMap<String,List<String>> commands;
private List<String> commandsList;
Model()
{
this.boards=new LinkedList<Board>();
this.commands=new HashMap<>();
this.commandsList=null;
}
public void addBoard(Board board)
{
this.boards.add(board);
}
public void deleteBoard(String boardId)
{
int index=0;
for(Board b:boards)
{
if(b.getId().equals(boardId)) break;
index++;
}
this.boards.remove(index);
if(this.commands.containsKey(boardId))
{
this.commands.remove(boardId);
}
}
public List<Board> getBoards()
{
return this.boards;
}
public HashMap<String,List<String>> getCommandsMap()
{
return this.commands;
}
public boolean isBoardExists(String boardId)
{
for(Board board:boards)
{
if(board.getId().equals(boardId)) return true;
}
return false;
}
public void registerCommand(String boardId,String commandToRegister)
{
if(this.commands.containsKey(boardId))
{
commandsList=this.commands.get(boardId);
commandsList.add(commandToRegister);
}
else
{
commandsList=new LinkedList<String>();
commandsList.add(commandToRegister);
this.commands.put(boardId,commandsList);
}
}
}
class RequestProcessor extends Thread
{
private JsonObject jsonObject;
private Board board;
private Model model;
private Socket socket;
private InputStream inputStream;
private OutputStream outputStream;
private OutputStreamWriter outputStreamWriter;
private InputStreamReader inputStreamReader;
private BufferedReader bufferedReader;
private StringBuilder stringBuilder;
private StringBuilder stringBuilderForNano;
private String request,response;
private int x;
private String baseURL="web-app";
RequestProcessor(Socket socket,Model model)
{
this.socket=socket;
this.model=model;
start();
}
public void run()
{
try
{
System.out.println("Processing request , wait...");
inputStream=socket.getInputStream();
inputStreamReader=new InputStreamReader(inputStream);
bufferedReader=new BufferedReader(inputStreamReader);
stringBuilder=new StringBuilder();
//loop will read line by line on each cycle and append the read string into string builder
String requestFromNano="";
boolean isRequestFromNano=false;
boolean flag=false;
String line="";
while(true)
{
if(bufferedReader.ready()) line=bufferedReader.readLine();
if(!flag && line.startsWith("GET"))
{
flag=true;
isRequestFromNano=false;
}
if(!flag) isRequestFromNano=true;
if(isRequestFromNano)
{
//reading from nano end starts here....
while(true)
{
int x=inputStreamReader.read();
if(x==-1 || x=='#') break;
stringBuilder.append((char)x);
requestFromNano+=(char)x;
}
}
if(isRequestFromNano) break;
if(line==null || line.equals("") || line.contains("#")) break;
line+="\r\n";
stringBuilder.append(line);
}
if(isRequestFromNano) request=requestFromNano;
else request=stringBuilder.toString();
//after accumulating the requested string , parseRequest("") will do required parsing....
parseRequest(request);
}catch(Exception e)
{
e.printStackTrace();
}
}
//below send response is response to send response back to nano end specifically...
public void sendResponse(String response)
{
try
{
outputStream=socket.getOutputStream();
outputStreamWriter=new OutputStreamWriter(outputStream);
outputStreamWriter.write(response);
outputStreamWriter.flush();
outputStreamWriter.close();
socket.close();
}catch(Exception e)
{
e.printStackTrace();
}
}
//below send response is responsible to send response to client side with appropriate header format
public void sendResponse(int statusCode,String contentType,byte[] response)
{
try
{
//getting the reference of output stream attached to the socket
outputStream=socket.getOutputStream();
outputStream.write(("HTTP/1.1 \r\n"+String.valueOf(statusCode)).getBytes());
outputStream.write(("ContentType: "+contentType+"\r\n").getBytes());
outputStream.write("\r\n".getBytes());
outputStream.write(response);
outputStream.write("\r\n\r\n".getBytes());
outputStream.flush();
outputStream.close();
socket.close();
}catch(Exception e)
{
e.printStackTrace();
}
}
//parseRequest("") will be responsible to do the appropriate parsing of requested URL
public void parseRequest(String request)
{
if(request.equals("")) return;
String splits[]=request.split(" ");
String requestURL="";
if(splits[0].startsWith("BC")) 
{
//request arrived from nano end...
requestURL=request;
}
else
{
requestURL=splits[1];
}
String mimeType="";
String fileToBeServed="";
byte[] fileContentBytes=null;
boolean isFileExists=false;
Path path=null;

if(requestURL.equals("/") || requestURL.equals(""))
{
//index page needs to be server
mimeType="text/html";
fileToBeServed=baseURL+"/index.html";
}
else
{
//check if it is a client side resource or server side resource
//if it contains <.extension> then it will be treated as client side resource
//if it does'nt contain <.extension> then treat it as a server side resource
if(requestURL.toLowerCase().trim().contains("."))
{
//it is a client side resource...
//check mime type & do required processing....
mimeType=new MimetypesFileTypeMap().getContentType(requestURL);
fileToBeServed=baseURL+requestURL;
if(fileToBeServed.contains("?"))
{
int questionMarkIndex=fileToBeServed.indexOf("?");
fileToBeServed=fileToBeServed.substring(0,questionMarkIndex);
}
}
else
{
serverSideProcessor(requestURL);
return;
}
}
//check for existance of file that is supposed to serve....
isFileExists=getFileExistStatus(fileToBeServed);
if(!isFileExists)
{
//do nothing just serve 404 response code with html , because the file not found
serveErrorPage(404,requestURL);
return;
}
try
{
path=Paths.get(fileToBeServed);
fileContentBytes=Files.readAllBytes(path);
}catch(Exception e)
{
}
sendResponse(100,mimeType,fileContentBytes);
}

//Server side processor is responsible for parsing the commands that are not meant to be client side resource
public void serverSideProcessor(String requestURL)
{
//not we have to check & allow only limited available commands....
if(requestURL.startsWith("/")) requestURL=requestURL.substring(1);
requestURL=requestURL.toUpperCase().trim();
if(requestURL.contains(",")==false)
{
//invalid request url...
serveErrorPage(404,requestURL);
return;
}
String splits[]=requestURL.split(",");
String command=splits[0];
String act="";
String boardId="";
List<ElectronicUnit> electronicUnits=null;
String electronicUnitId="";
int state;
boolean boardIdFound=false;
boolean electronicUnitIdFound=false;
ElectronicUnit electronicUnit=null;
JsonObject jsonObject=null;
String responseJson="";
List<String> commandsList=null;
if(splits.length<2) 
{
//invalid request url...
serveErrorPage(404,requestURL);
return;
}
if(command.equals("CC"))
{
act=splits[1];
if(act.equals("CMD"))
{
//CC,CMD,<boardId>,<electronicUnitId>,<state>
boardId=splits[2].trim();
electronicUnitId=splits[3].trim();
for(Board b:this.model.getBoards())
{
if(b.getId().equals(boardId))
{
boardIdFound=true;
electronicUnits=b.getElectronicUnits();
break;
}
}
if(boardIdFound)
{
for(ElectronicUnit eu:electronicUnits)
{
if(eu.getId().equals(electronicUnitId))
{
electronicUnitIdFound=true;
electronicUnit=eu;
break;
}
}
}
if(boardIdFound && electronicUnitIdFound)
{
state=electronicUnit.getState();
if(state==0) state=1;
else state=0;
electronicUnit.setState(state);
command=electronicUnitId+","+state;
this.model.registerCommand(boardId,command);
jsonObject=new JsonObject();
jsonObject.addProperty("success","true");
responseJson=new Gson().toJson(jsonObject);
sendResponse(100,"application/json",responseJson.getBytes());
return;
}
if(boardIdFound && !electronicUnitIdFound)
{
jsonObject=new JsonObject();
jsonObject.addProperty("success","false");
jsonObject.addProperty("exception","Invalid electronic unit id : "+electronicUnitId);
responseJson=new Gson().toJson(jsonObject);
sendResponse(100,"application/json",responseJson.getBytes());
return;
}
if(!boardIdFound)
{
jsonObject=new JsonObject();
jsonObject.addProperty("success","false");
jsonObject.addProperty("exception","Invalid board id : "+boardId);
responseJson=new Gson().toJson(jsonObject);
sendResponse(100,"application/json",responseJson.getBytes());
return;
}
}
else if(act.equals("LS"))
{
//send list of all the available boards in response...
List<String> responseJsonList=new LinkedList<String>();
List<String> eUnits=new LinkedList<String>();
JsonObject electronicUnitJson=null;
JsonObject boardJson=null;
String electronicUnitJsonString="";
String boardJsonString="";
Gson gson=new Gson();
for(Board b:this.model.getBoards())
{
boardJson=new JsonObject();
boardJson.addProperty("boardId",b.getId());
for(ElectronicUnit eu:b.getElectronicUnits())
{
electronicUnitJson=new JsonObject();
electronicUnitJson.addProperty("electronicUnitId",eu.getId());
electronicUnitJson.addProperty("state",eu.getState());
electronicUnitJsonString=gson.toJson(electronicUnitJson);
eUnits.add(electronicUnitJsonString.trim());
}
boardJson.addProperty("electronicUnits",gson.toJson(eUnits));
eUnits.clear();
boardJsonString=gson.toJson(boardJson);
responseJsonList.add(boardJsonString.trim());
}
responseJson=gson.toJson(responseJsonList).trim();
sendResponse(100,"application/json",responseJson.getBytes());
}
else
{
//invalid request url...
serveErrorPage(404,requestURL);
return;
}
}
else if(command.equals("BR"))
{
//Board is registering....
boardId=splits[1];
boolean isBoardExists=this.model.isBoardExists(boardId);
if(isBoardExists)
{
jsonObject=new JsonObject();
jsonObject.addProperty("success","false");
jsonObject.addProperty("exception","Board already exists");
responseJson=new Gson().toJson(jsonObject);
sendResponse(100,"application/json",responseJson.getBytes());
return;
}
electronicUnits=new LinkedList<ElectronicUnit>();
for(int i=2;i<splits.length;i++)
{
electronicUnitId=splits[i];
electronicUnit=new ElectronicUnit();
electronicUnit.setId(electronicUnitId);
electronicUnit.setState(0);
electronicUnits.add(electronicUnit);
}
board=new Board();
board.setId(boardId);
board.setElectronicUnits(electronicUnits);
this.model.addBoard(board);
jsonObject=new JsonObject();
jsonObject.addProperty("success","true");
responseJson=new Gson().toJson(jsonObject);
sendResponse(100,"application/json",responseJson.getBytes());
}
else if(command.equals("BD"))
{
//board delete request....
boardId=splits[1];
boolean isBoardExists=this.model.isBoardExists(boardId);
if(!isBoardExists)
{
jsonObject=new JsonObject();
jsonObject.addProperty("success","false");
jsonObject.addProperty("exception","Invalid board id (bad request)");
responseJson=new Gson().toJson(jsonObject);
sendResponse(100,"application/json",responseJson.getBytes());
return;
}
this.model.deleteBoard(boardId);
jsonObject=new JsonObject();
jsonObject.addProperty("success","success");
responseJson=new Gson().toJson(jsonObject);
sendResponse(100,"application/json",responseJson.getBytes());
}
else if(command.equals("BC"))
{
//Nano is asking for commands....
//BC,<boardId>#
boardId=splits[1].trim();
boardId=boardId.substring(0,boardId.length());
String response="";
if(this.model.getCommandsMap().containsKey(boardId))
{
commandsList=this.model.getCommandsMap().get(boardId);
if(commandsList!=null)
{
if(commandsList.size()>0)
{
for(String cmnds:commandsList)
{
if(response.length()>0) response+=",";
response+=cmnds;
}
commandsList.clear();
}
}
}
response+="#";
sendResponse(response);
return;
}
else if(command.equals("ALL"))
{
//will be added soon...
serveErrorPage(404,"This feature will be made available soon.  :)");
return;
}
else
{
serveErrorPage(404,requestURL);
return;
}
}
//serveErrorPage is responsible for serving the error page contents of corresponding error codes
public void serveErrorPage(int errorCode,String requestURL)
{
byte[] errorPageBytes=null;
String errorPageToBeServed="";
String errorPageContents="";
File file=null;
Scanner scanner=null;
int startIndex,endIndex;
String leftPart,rightPart,middlePart;
if(errorCode==404)
{
errorPageToBeServed=baseURL+"/errorPages/404.html";
file=new File(errorPageToBeServed);
if(!file.exists()) return;
try
{
//reading the error page file contents in string format using scanner so that formatting can be done....
scanner=new Scanner(file);
while(scanner.hasNextLine()) errorPageContents+=scanner.nextLine()+"\r\n";
}catch(Exception e)
{
}
//fetch the start and end index where we want to put the requested url
startIndex=errorPageContents.indexOf("{");
endIndex=errorPageContents.indexOf("}");
leftPart=errorPageContents.substring(0,startIndex+1);
rightPart=errorPageContents.substring(endIndex);
middlePart="<b>"+requestURL+"</b>";
errorPageContents=leftPart+middlePart+rightPart;
errorPageBytes=errorPageContents.getBytes();
sendResponse(errorCode,"text/html",errorPageBytes);
}
}
public boolean getFileExistStatus(String fileToBeServed)
{
boolean existStatus=false;
try
{
File file=new File(fileToBeServed);
existStatus=file.exists();
}catch(Exception e)
{
e.printStackTrace();
}
return existStatus;
}
}
class Server
{
private int port;
private ServerSocket serverSocket;
private Model model;
Server(int port,Model model)
{
try
{
this.port=port;
this.model=model;
serverSocket=new ServerSocket(this.port);
}catch(Exception e)
{
}
startListening();
}
public void startListening()
{
Socket socket;
try
{
while(true)
{
System.out.println("Server is listening port : "+this.port);
socket=serverSocket.accept();
new RequestProcessor(socket,this.model);
}
}catch(Exception e)
{
e.printStackTrace();
}
}
public static void main(String gg[])
{
Model model=new Model();
Server server=new Server(5050,model);
}
}