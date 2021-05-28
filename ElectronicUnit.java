public class ElectronicUnit
{
private String id;
private int state;
public ElectronicUnit()
{
this.id="";
this.state=0;
}
public void setId(String id)
{
this.id=id;
}
public String getId()
{
return this.id;
}
public void setState(int state)
{
this.state=state;
}
public int getState()
{
return this.state;
}
}