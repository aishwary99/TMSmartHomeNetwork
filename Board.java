import java.util.*;
public class Board
{
private String id;
private List<ElectronicUnit> electronicUnits;
public Board()
{
this.id="";
this.electronicUnits=null;
}
public void setId(String id)
{
this.id=id;
}
public String getId()
{
return this.id;
}
public void setElectronicUnits(List<ElectronicUnit> electronicUnits)
{
this.electronicUnits=electronicUnits;
}
public List<ElectronicUnit> getElectronicUnits()
{
return this.electronicUnits;
}
}