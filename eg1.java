import com.google.gson.*;
import java.util.*;
class eg1
{
public static void main(String gg[])
{
try
{
Gson gson=new Gson();
List<String> boards=new LinkedList<String>();
List<String> electronicUnits=new LinkedList<>();
JsonObject jo=null;
jo=new JsonObject();
jo.addProperty("electronicUnitId","fan");
jo.addProperty("state",1);
String electronicUnitString=gson.toJson(jo);
electronicUnits.add(electronicUnitString);
jo=new JsonObject();
jo.addProperty("boardId","charlie");
jo.addProperty("electronicUnits",gson.toJson(electronicUnits));
String jsonString=gson.toJson(jo);
boards.add(jsonString);
for(String s:boards) System.out.println(s);
}catch(Exception e)
{
}
}
}