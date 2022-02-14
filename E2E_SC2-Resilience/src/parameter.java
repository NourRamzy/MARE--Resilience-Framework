import java.util.ArrayList;

public class parameter {
	public String name; 
	public ArrayList<Integer> values; 
 public parameter(String n,  ArrayList<Integer> v )
{
	this.name= n; 
	this.values= v; 
}
 public ArrayList<Integer> get_values()
 {
	 return this.values; 
 }
}
