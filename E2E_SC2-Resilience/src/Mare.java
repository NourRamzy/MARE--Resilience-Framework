import java.io.BufferedReader;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.update.UpdateAction;
import org.topbraid.jenax.util.JenaUtil;
import org.topbraid.shacl.rules.RuleUtil;


import io.github.galbiston.geosparql_jena.configuration.GeoSPARQLOperations;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.lang.Math;


public class Mare {
	public static int time_big= 10; 
	public static int frequency= 2; 
	public static String strategy= "late";  
	public static ArrayList<String> listtt=new ArrayList<String>();  
	static String SOURCE = "http://www.semanticweb.org/ramzy/ontologies/2021/3/untitled-ontology-6";
    static String NS = SOURCE + "#";
    static String Scor= "http://purl.org/eis/vocab/scor#";
    public static 	 HashMap<String, ArrayList<Integer>> hash_map ;
	public static HashMap<String, ArrayList<Integer>> hash_map_customer ;
	public static HashMap<String,ArrayList<String>> sc_uniques_final ;
	
	public static List<String> times; 
	private static String Prefix;
	public static BufferedWriter out = null;
	public static BufferedWriter out2 = null;
	public static void main(String[] args) throws IOException {
		times= new ArrayList<String>();
		Prefix= "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\r\n" + 
				"PREFIX owl: <http://www.w3.org/2002/07/owl#>\r\n" + 
				"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\r\n" + 
				"PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\r\n" + 
				"Prefix : <http://www.semanticweb.org/ramzy/ontologies/2021/3/untitled-ontology-6#>"; 
		hash_map = new HashMap<String, ArrayList<Integer>>();
		hash_map_customer = new HashMap<String, ArrayList<Integer>>();
		sc_uniques_final = new HashMap<String, ArrayList<String>>();
		out = new BufferedWriter (new FileWriter("C:/Users/Ramzy/Desktop/Resilience/october_out.ttl"));
		OntModel model = ModelFactory.createOntologyModel();
		model.read("src/resources/output.ttl");
		create_disruptions(model); 
	    model.write(out, "TURTLE");
      out.close();
	}
	private static void recovery_evaluation(OntModel model, int strategy) {
		 String query= Prefix+"Select ?order ?portfolio where{?order :hasPortfolio ?portfolio. }";
		List<QuerySolution>orders= execute(query,model);
		for (int i=0; i<orders.size(); i++)
		{ 	
			String order= orders.get(i).get("order").toString().split("#")[1];
			String port= orders.get(i).get("portfolio").toString().split("#")[1];
			
		get_plan_quantity(model, order, port); 
		get_plan_price( model, order, port);
		get_latest_plan_time( model, order,port); 
		}
		String get_recovered_orders= Prefix+ "Select DISTINCT (SUM(?op) as ?originaltotalprice)  (SUM(?cost) as?costt) (SUM(?delay) as?delayy) where{  "
				+ "?order :hasPortfolio ?portfolio. ?portfolio :hasTotalPrice ?price. ?order :hasDeliveryTime ?dt. "
				+ " ?portfolio :hasLatestTime ?time. ?portfolio :hasTotalQuantity ?quantity. ?order :hasOriginalQuantity ?oq. ?order :hasTotalOriginalPrice ?op"
			+ " BIND (xsd:integer(?dt) as ?oemtime).  "
				+ "BIND (xsd:integer(?time)-xsd:integer(?oemtime) AS ?delay) . "
				+ "BIND (xsd:float(?oq)-xsd:float(?quantity) AS ?NoNfulfillment)."
				+ " BIND (xsd:integer(?price)-xsd:integer(?op) AS ?cost)"
				+ " FILTER (xsd:float(?NoNfulfillment)=0.0) } " ; 
		print_results(execute(get_recovered_orders, model),1);
		
		
		String get_recovered_orders_customer= Prefix+ "Select DISTINCT (SUM(?op) as ?originaltotalprice)  (SUM(?cost) as?costt) (SUM(?delay) as?delayy) ?customer where{  "
				+ "?customer :makes ?order. ?order :hasPortfolio ?portfolio. ?portfolio :hasTotalPrice ?price. ?order :hasDeliveryTime ?dt. "
				+ " ?portfolio :hasLatestTime ?time. ?portfolio :hasTotalQuantity ?quantity. ?order :hasOriginalQuantity ?oq. ?order :hasTotalOriginalPrice ?op"
			+ " BIND (xsd:integer(?dt) as ?oemtime).  "
				+ "BIND (xsd:integer(?time)-xsd:integer(?oemtime) AS ?delay) . "
				+ "BIND (xsd:float(?oq)-xsd:float(?quantity) AS ?NoNfulfillment)."
				+ " BIND (xsd:integer(?price)-xsd:integer(?op) AS ?cost)"
				+ " FILTER (xsd:float(?NoNfulfillment)=0.0) } GROUP by (?customer) " ; 
		print_results(execute(get_recovered_orders_customer, model),1);
		
		
		
		String count_orders= Prefix+ "Select * where{  ?order a :Order.}"; 
		List<QuerySolution>order_count= execute(count_orders,model);
		System.out.println("Order Count"+order_count.size());
		
		String get_non_recovered_orders= Prefix+ "Select DISTINCT ?customer ?order ?portfolio where{  ?customer :makes ?order. ?order :hasPortfolio ?portfolio. ?portfolio :hasTotalPrice ?price. ?order :hasDeliveryTime ?dt. "
				+ " ?portfolio :hasLatestTime ?time. ?portfolio :hasTotalQuantity ?quantity. ?order :hasOriginalQuantity ?oq. ?order :hasTotalOriginalPrice ?op"
			+ " BIND (xsd:integer(?dt) as ?oemtime).  "
				+ "BIND (xsd:integer(?time)-xsd:integer(?oemtime) AS ?delay) . "
				+ "BIND (xsd:float(?oq)-xsd:float(?quantity) AS ?NoNfulfillment)."
				+ " BIND (xsd:integer(?price)-xsd:integer(?op) AS ?cost)"
				+ "<< ?portfolio :needsNode ?node>> :recoveredBy ?recovery "
				+ " FILTER (xsd:float(?NoNfulfillment>0))} ORDER by (?order) " ; 
		List<QuerySolution>ll= execute(get_non_recovered_orders,model);
		System.out.println("NON Recovered Orders: "+ll.size()); 
		print_results(ll,1); 
	}
	private static void create_disruptions(OntModel model) throws IOException {
		
		 OntClass disruption_class = model.getOntClass( NS + "Disruption");
		 List<String> scopes= new ArrayList<String>();
		 List<String> cause= new ArrayList<String>();
		 List<String> severity= new ArrayList<String>();
		 List<String> nodes= new ArrayList<String>();
		 List<Integer> durations= new ArrayList<Integer>();
		 List <String> timestamps= new ArrayList<String>();
				 
			// low =0.1, M=0.3; h=0.7
		 cause.add("Capacity Shortage"); scopes.add("Production");severity.add("0.3");durations.add(5); nodes.add("OEM1"); timestamps.add("9"); 
		 cause.add("Fire");scopes.add("Production"); severity.add("0.1");durations.add(1);nodes.add("OEM1"); timestamps.add("56");
		 cause.add("Contamination Issues"); scopes.add("Inventory"); severity.add("0.3"); durations.add(3); nodes.add("OEM1"); timestamps.add("78");
		 cause.add("Block In Transport Mode"); scopes.add("Logistics");severity.add("0.7"); durations.add(45); nodes.add("OEM1"); timestamps.add("92");
		 cause.add("Supply Shortage"); scopes.add("Production");severity.add("0.7"); durations.add(100);nodes.add("SupplierNode3.1"); timestamps.add("30"); 
		 cause.add("Supply Shortage"); scopes.add("Production");severity.add("0.3"); durations.add(60);nodes.add("SupplierNode4.1");timestamps.add("95");
		 
		 List<String> transports= new ArrayList<String>(); 
		 transports.add("vehicle");
		 transports.add("maritime");
		 transports.add("air");
		Property lat_min  = model.getProperty(NS+"hasLatMin");
		Property lat_max  = model.getProperty(NS+"hasLatMax");
		Property long_min  = model.getProperty(NS+"hasLongMin");
		Property long_max  = model.getProperty(NS+"hasLongMax");
		Property hasSeverity  = model.getProperty(NS+"hasSeverity");		
		Property hasCause  = model.getProperty(NS+"hasCause");
		Property hasScope  = model.getProperty(NS+"hasScope");
		Property time_end = model.getProperty(NS+"hasEndTime");
		Property time_begin = model.getProperty(NS+"hasStartTime");
		Property affectsNode = model.getProperty(NS+"affectsNode");
		
		////////////////////////////////////////////////////////////////////
		 model = ModelFactory.createOntologyModel();
		 model.read("src/resources/output.ttl");
		 String s = Prefix+ "SELECT * where { << :PortfolioOZzMZj7s  :needsNode ?node>> :hasTimeStamp ?t.  ?node :hasLongitude ?long. ?node :hasLatitude ?lat.}"; 
		 List<QuerySolution> ll= execute(Prefix+s,model); 
		 Individual disruption = model.createIndividual(NS+"Disruption"+7, disruption_class);
		 int y= (int) (2.0 * Math.random());
		String ca= cause.get(y); 
		OntClass cause_class = model.getOntClass(NS+ca);
		 Individual cause_ind = model.createIndividual(NS+"Cause"+7,cause_class);
		 cause_ind.addProperty(hasScope, scopes.get(0));
		 disruption.addProperty(hasCause,cause_ind);
		 disruption.addProperty(hasSeverity, severity.get(3)+"");
		 disruption.addProperty(time_begin, ll.get(0).get("t").toString());
		 
		 disruption.addProperty(time_end,85+"");
		 int longi= Integer.parseInt(ll.get(0).get("long").toString()); 
		 int lati= Integer.parseInt(ll.get(0).get("lat").toString()); 
		 
		 disruption.addProperty(lat_min, (lati)+"");
		 disruption.addProperty(lat_max, (lati+10)+"");
		 
		 disruption.addProperty(long_min, longi+"");
		 disruption.addProperty(long_max, (longi+10)+"");

		 for (int i=0; i<ll.size(); i++)
		{
		
			
		 disruption.addProperty(affectsNode, ll.get(i).get("node").toString().split("#")[1]);
		 String insert= Prefix+ "Insert { << <"+ll.get(i).get("node").toString()+"> :isDisruptedBy :Disruption7 >> :hasTimeStamp \""+ll.get(i).get("t").toString()+"\"} WHERE{ }\r\n" ; 
		 UpdateAction.parseExecute(insert, model);
		}
		 model.write(out, "TURTLE"); 
		 get_disrupted_plans(model); 
		 recover(model); 

		//////////////////////////////////////////////////////////////////////
			 String get_portfolios = "Select * where {?order :hasQuantity ?q. ?order :hasDeliveryTime ?d.  ?order :hasPortfolio ?p. <<?p :needsNode ?node>> :hasTimeStamp ?t.  ?node :hasLongitude ?long. ?node :hasLatitude ?lat.}"; 
			 List<QuerySolution> l= execute(Prefix+get_portfolios,model); 
			
		 for (int i=0; i<6; i++ )
		 {
			 model = ModelFactory.createOntologyModel();
			 model.read("src/resources/output.ttl");
			 Individual disruption1 = model.createIndividual( NS+"Disruption"+i, disruption_class); 
			 disruption1.addProperty(hasSeverity, severity.get(0)+"");
			int y1= (int) (2.0 * Math.random());
			String ca1= cause.get(y1); 
			OntClass cause_class1 = model.getOntClass(NS+ca1);
			 Individual cause_ind1 = model.createIndividual(NS+cause.get(i),cause_class1);
			 //cause_ind.addProperty(hasScope, scopes.get((int)(3.0 * Math.random())));
			 cause_ind1.addProperty(hasScope, scopes.get(i));
			 disruption1.addProperty(hasCause,cause_ind1);
			
				 int s1= Integer.parseInt( timestamps.get(i))+durations.get(i); 
				 disruption1.addProperty(time_begin,  timestamps.get(i));
				 disruption1.addProperty(time_end,s1+"");
				
				  longi= Integer.parseInt(l.get(i).get("long").toString()); 
				  lati= Integer.parseInt(l.get(i).get("lat").toString()); 
				 disruption1.addProperty(lat_min, (lati)+"");
				 disruption1.addProperty(lat_max, (lati+10)+"");
				 disruption1.addProperty(long_min, longi+"");
				 disruption1.addProperty(long_max, (longi+10)+"");
				 disruption1.addProperty(affectsNode, nodes.get(i));
				 String insert= Prefix+ "Insert { << :"+nodes.get(i)+" :isDisruptedBy <"+disruption1+">>> :hasTimeStamp \""+timestamps.get(i)+"\"} WHERE{ <"+disruption1+"> a :Disruption}\r\n" ; 
				 UpdateAction.parseExecute(insert, model);
				 get_disrupted_plans(model); 
				 recover(model); 
		 }
		}
	
	private static void get_disrupted_plans(OntModel model) {
		List<QuerySolution> nodes= null; 
 		String get_nodes=Prefix+ "Select * where {  ?dis :hasStartTime ?start.  ?dis :hasEndTime ?end. \r\n << ?plan :needsNode ?node >> :hasTimeStamp ?t. "
		 				+ "<<?node :isDisruptedBy ?dis>> :hasTimeStamp ?x. "
				 		+ " FILTER(  xsd:integer(?t) >= xsd:integer(?start)  " 
					 		+" && xsd:integer(?t) < xsd:integer(?end) )}"; 
			nodes= execute (get_nodes,model); 
			for (int j=0; j<nodes.size(); j++)
			{
				String node= nodes.get(j).get("node").toString().split("#")[1]; 
				String dis= nodes.get(j).get("dis").toString().split("#")[1]; 
				String time=  nodes.get(j).get("t").toString(); 
			    String port= nodes.get(j).get("plan").toString().split("#")[1]; 
			    String query= Prefix+ "INSERT { :"+port+" :isDisrupted 'True'. << :"+port+" :needsNode :"+node+" >> :isDisrupted 'True'. << :"+port+" :needsNode :"+node+" >> :isDisruptedBy :"+dis+"} WHERE { }"; // :"+port+" a :Portfolio. "
			    UpdateAction.parseExecute(query, model) ;
			    String insert= Prefix+ "Insert { << :"+node+" :isDisruptedBy :"+dis+" >> :hasTimeStamp \""+time+"\"} WHERE{ }\r\n" ; 
			    UpdateAction.parseExecute(insert, model);
			}
			model.write(out, "Turtle"); 
	}

	private static void print_results(List<QuerySolution> l, int time ) {
		// TODO Auto-generated method stub
		for (int i=0; i<l.size(); i++)
		{	
			Iterator <String> variables= l.get(i).varNames(); 
			while (variables.hasNext())
			{ 
			String var= variables.next(); 
			if (l.get(i).get(var).toString().contains("integer")|| l.get(i).get(var).toString().contains("float")|| l.get(i).get(var).toString().contains("decimal"))
			{
				System.out.print(var+": "+l.get(i).get(var).toString().split("\\^")[0]+" ");
			}
			else if (l.get(i).get(var).toString().contains("#"))
			System.out.print(var+": "+l.get(i).get(var).toString().split("#")[1]+" ");
			else
				System.out.print(var+": "+l.get(i).get(var).toString()+" ");
			
		}
			System.out.println(" ");	
		}
			
		
	}
	
private static void get_plan_price(OntModel model, String order, String port)
	
{	
	String get_price= Prefix+ "SELECT * WHERE{  :"+order+" :hasPortfolio :"+port+". <<:"+port+" :needsNode ?node>> :hasQuantity ?q.  <<:"+port+" :needsNode ?node>> :hasUnitPrice ?p. BIND (?p*?q AS ?price)} "; 
	List<QuerySolution> prices= execute(get_price,model);
	float total_price= 0;
	//System.out.println("number of rows"+ prices.size()); 
	for (int price=0; price<prices.size(); price++)
	{
		//System.out.println("TotalPrice"+ total_price); 
		total_price= total_price+ (Float.parseFloat(prices.get(price).get("?q").toString().split("\\^")[0])*
				Integer.parseInt(prices.get(price).get("?p").toString().split("\\^")[0])); 
		//System.out.println("TotalPrice"+ total_price);
	}
	String query= Prefix+ "INSERT { :"+port+" :hasTotalPrice "+total_price+"} WHERE { :"
			+port+" rdf:type :Portfolio }"; 
	UpdateAction.parseExecute(query, model) ;
		
	}
private static void get_plan_quantity(OntModel model, String order, String port)

{	
	
	String get_price= Prefix+ "SELECT * WHERE{ :"+order+" :hasPortfolio :"+port+". <<:"+port+" :needsNode ?node>> :hasQuantity ?q. } "; 
	List<QuerySolution> prices= execute(get_price,model);
	//print_results(prices,1); 
	float total_quantity= 0; 
	for (int price=0; price<prices.size(); price++)
	{
		total_quantity= total_quantity+ (Float.parseFloat(prices.get(price).get("?q").toString().split("\\^")[0])); 
		}
	String query= Prefix+ "INSERT { :"+port+" :hasTotalQuantity "+total_quantity+"} WHERE {  }"; 
	UpdateAction.parseExecute(query, model) ;
		
	}
	
private static void get_latest_plan_time(OntModel model, String order, String port)
{
	
			String get_price= Prefix+ "SELECT * WHERE{ :"+order+" :hasPortfolio :"+port+".  <<:"+port+" :needsNode ?node>> :hasQuantity ?q."
					+ "  <<:"+port+" :needsNode ?node>> :hasTimeStamp ?t. ?node :hasLeadTime ?lt. BIND (xsd:integer(?t) + xsd:integer(?lt) as ?final)."
							+ " OPTIONAL { ?node :hasOEM ?oem. ?oem :hasLeadTime ?oemlt. BIND (xsd:integer(?oemlt) + xsd:integer(?final) as ?finall ).} } order by desc (?t)" ; 
			// 
	List<QuerySolution> times= execute(get_price,model);
	int  final_time= Integer.parseInt(times.get(0).get("final").toString().split("\\^")[0]); 
	if (times.get(0).get("oemlt")!= null)
	{
		final_time= Integer.parseInt(times.get(0).get("final").toString().split("\\^")[0])+
				Integer.parseInt(times.get(0).get("oemlt").toString().split("\\^")[0]) ; }
	String query= Prefix+ "INSERT { :"+port+" :hasLatestTime "+final_time+"} WHERE { }"; 
	
	UpdateAction.parseExecute(query, model) ;
	
}

private static void recover (OntModel model) throws IOException
	{
		OntModel copyOfOntModel  = ModelFactory.createOntologyModel(model.getSpecification()) ; 
	    copyOfOntModel.add( model.getBaseModel() );
	    OntClass portfolio_class = model.getOntClass( NS + "Portfolio");
		String q= Prefix+ "SELECT  * WHERE { ?order :hasPortfolio ?portfolio. ?customer :makes ?order. ?customer :hasPriority ?priority."
						+ " ?portfolio :isDisrupted ?x. } ORDER BY DESC (?priority)"; 
		List<QuerySolution> portfolios= execute (q,model); 
		String order;
		System.out.println("Disrupted Portfolios"+ portfolios.size());
		int new_portf=0; 
		for (int strategy=0; strategy<3; strategy++)
		{
		for (int i=0; i< portfolios.size(); i++)
		{
			int count=0; 
			order=  portfolios.get(i).get("?order").toString().split("#")[1];
			 String portfo= portfolios.get(i).get("?portfolio").toString().split("#")[1];
			 q= Prefix+ "SELECT  * WHERE { ?customer :makes ?order. ?customer :hasPriority ?priority. ?order :hasPortfolio :"+portfo+". "
						+ " :"+portfo+" :isDisrupted ?x. << :"+portfo+" :needsNode ?node>> :isDisrupted ?x. << :"+portfo+" :needsNode ?node>> :hasTimeStamp ?t. << :"+portfo+" :needsNode ?node >> :isDisruptedBy ?disruption."  
						+ "<< :"+portfo+" :needsNode ?node>> :getsProduct  ?p. << :"+portfo+" :needsNode ?node>> :hasQuantity  ?q. << :"+portfo+" :needsNode ?node>> :hasUnitPrice  ?price . << :"+portfo+" :needsNode ?node>> :isDisruptedBy ?disruption."
						+ "  << ?node :isDisruptedBy ?disruption>> :hasTimeStamp ?t. ?disruption :hasSeverity ?s. ?disruption :hasCause ?cause. ?cause :hasScope ?scope. BIND (xsd:integer(?q)* xsd:float(?s) as ?current)"
						+ "BIND (xsd:integer(?q)- xsd:float(?current) as ?torecover) } ORDER BY DESC (?priority)"; 
			  
			String portfolio= portfolios.get(i).get("?portfolio").toString().split("#")[1];
			Individual portfolio_ind = model.createIndividual( NS+"Portfolio"+order+i+strategy, portfolio_class);
			count= count+1; new_portf++; 
			Individual order_ind= model.getIndividual(portfolios.get(i).get("order").toString()); 
			Property hasPortfolio = model.getProperty(NS+"hasPortfolio");
			order_ind.addProperty(hasPortfolio,portfolio_ind);
			List<QuerySolution> recovery= new ArrayList<QuerySolution>(); 
		 q= Prefix+ "SELECT  * WHERE { ?customer :makes ?order. ?customer :hasPriority ?priority. ?order :hasPortfolio :"+portfolio+". "
					+ " :"+portfolio+" :isDisrupted ?x. << :"+portfolio+" :needsNode ?node>> :isDisrupted ?x. << :"+portfolio+" :needsNode ?node>> :hasTimeStamp ?t. << :"+portfolio+" :needsNode ?node >> :isDisruptedBy ?disruption."  
					+ "<< :"+portfolio+" :needsNode ?node>> :getsProduct  ?p. << :"+portfolio+" :needsNode ?node>> :hasQuantity  ?q. << :"+portfolio+" :needsNode ?node>> :hasUnitPrice  ?price . << :"+portfolio+" :needsNode ?node>> :isDisruptedBy ?disruption."
					+ "  << ?node :isDisruptedBy ?disruption>> :hasTimeStamp ?t. ?disruption :hasSeverity ?s. ?disruption :hasCause ?cause. ?cause :hasScope ?scope. BIND (xsd:integer(?q)* xsd:float(?s) as ?current)"
					+ "BIND (xsd:integer(?q)- xsd:float(?current) as ?torecover) } ORDER BY DESC (?priority)"; 
		  
		List<QuerySolution> nodes= execute (q,model); 

		for (int n=0;n<nodes.size(); n++)
		{
		String node=nodes.get(n).get("node").toString();

		if (node.contains("OEM")) // or check if cause is internal or external 
		{
			if (strategy==0)
			{
					recovery.add(try_strategic_stock(nodes.get(n),model, node.split("#")[1],portfolio_ind.toString().split("#")[1], portfolio));
					}
			 else if (strategy==1)
					
				recovery.add(try_alternative_mode(nodes.get(n),model, node.split("#")[1],portfolio_ind.toString().split("#")[1], portfolio)); //try alternative transport mode
				
			 else if (strategy==2)
			recovery.add(try_later_recovery(nodes.get(n),model,node.split("#")[1],portfolio_ind.toString().split("#")[1], portfolio)); 
			
		}
		else   //if external
		{
			 recovery.add(try_alternative_suppliers(nodes.get(n),model, node.split("#")[1],portfolio_ind.toString().split("#")[1], portfolio, order)); 
			
		}
		}

		add_rest_portfolio(portfolio, model, portfolio_ind);
		
		}
		System.out.println("Model Size"+ model.size() + " Copy Model Size:"+ copyOfOntModel.size());
		recovery_evaluation(model, strategy);
		model=copyOfOntModel; 
		System.out.println("Model Size"+ model.size() + " Copy Model Size:"+ copyOfOntModel.size()); 
		   	
		}
		System.out.println("New created Portfolios"+ new_portf);
	}
	
		private static QuerySolution try_alternative_mode(QuerySolution details, OntModel model,String node, String portfolio , String portfolio_old) {
			String query= Prefix+ "SELECT ?node ?mode \r\n" + 
					"	WHERE { ?node a :OEM. ?node :hasTransportMode ?mode.}"; 
			List <QuerySolution> modes= execute(query,model);
			String recovery; 
			String neww; 
			if (modes.size()>1)
			{// original quantity
			neww=details.get("q").toString(); 
			recovery= "AlternativeMode"; 
			}
			else 
			{
				neww=details.get("current").toString();  
				recovery= "Not Recovered"; 
			}
				String q= 	 Prefix+
						/*"DELETE   \r\n" + 
						"{ << :"+portfolio+" :needsNode :"+node+" >> :hasQuantity ?q  } \r\n" + */
						"Insert {  << :"+portfolio+" :needsNode :"+node+" >> :hasQuantity \""+neww+"\"."
						+"<< :"+portfolio+" :needsNode :"+node+">> :getsProduct ?p. "+
						"<< :"+portfolio+" :needsNode :"+node+">> :hasUnitPrice ?price. "+
						"<< :"+portfolio+" :needsNode :"+node+">> :hasTimeStamp ?t.   "+ 
						"<< :"+portfolio+" :needsNode :"+node+">> :recoveredBy '"+recovery+"'}   "+ 
						"where \r\n" + 
						"{ << :"+portfolio_old+" :needsNode :"+node+" >> :hasQuantity ?q.  "
						+"<< :"+portfolio_old+" :needsNode :"+node+">> :getsProduct ?p. "+
						"<< :"+portfolio_old+" :needsNode :"+node+">> :hasUnitPrice ?price. "+
						"<< :"+portfolio_old+" :needsNode :"+node+">> :hasTimeStamp ?t.   }";  
				UpdateAction.parseExecute(q, model) ;
				String retrieve= Prefix+ "Select * WHERE { <<:"+portfolio+" :needsNode :"+node+">> :hasQuantity ?q. "
						+"<< :"+portfolio+" :needsNode :"+node+">> :getsProduct ?p. "+
						"<< :"+portfolio+" :needsNode :"+node+">> :hasUnitPrice ?price. "+
						"<< :"+portfolio+" :needsNode :"+node+">> :hasTimeStamp ?t.   }";  
			return execute(retrieve, model).get(0); //String portfolio= details.get("?portfolio").toString(); 
				
	
			
				 
	}
		private static QuerySolution try_strategic_stock(QuerySolution details, OntModel model, String node, String portfolio, String portfolio_old) {
			String product=details.get("p").toString().split("#")[1];
			float quantity=Float.parseFloat(details.get("torecover").toString().split("\\^")[0]);
			String t= details.get("t").toString();
			String query= Prefix+ "SELECT *\r\n" + 
					"	WHERE { ?node a :OEM. ?node :hasInventory ?inv. ?inv :hasProduct ?p.  ?inv :hasPrice ?price. "
					+ "?inv :hasTimeStamp \""+t+"\". ?inv :hasQuantity ?q. BIND(xsd:integer(?price)+\"2\"^^xsd:integer as ?newprice)"
					+ "Filter(regex(str(?p),\""+product+"\"))}"; 
			List <QuerySolution> oem_inventory= execute(query,model); 
			float oem_q= Float.parseFloat(oem_inventory.get(0).get("q").toString());
			String recovery; 
			String toUpdate; 
			String price; 
			if (oem_q>= quantity)
			{
				float neww= oem_q- quantity; 
				int new_price= Integer.parseInt(oem_inventory.get(0).get("newprice").toString().split("\\^")[0]); 
				 query = Prefix+"DELETE   \r\n" + 
						"{ ?inv :hasQuantity ?q."
						+ "?inv :hasPrice ?price. }\r\n" + 
						"Insert {?inv :hasQuantity \""+neww+"\". ?inv :hasPrice \""+new_price+"\".}\r\n" + 
						"where \r\n" + 
						"{ ?node rdf:type :OEM. "
						+ "?node :hasInventory ?inv. "
						+ "?inv :hasProduct ?p. \r\n" + 
						"?inv :hasTimeStamp \""+t+"\". "
						+ "?inv :hasQuantity ?q. ?inv :hasPrice ?price.  }";  
				UpdateAction.parseExecute(query, model) ;
			//	System.out.println("at time"+t+"OEM was: "+oem_q+"now at"+ (oem_q- quantity)); 
			 recovery= "StartegicStock";
			 toUpdate= details.get("q").toString(); 
			 price= oem_inventory.get(0).get("newprice").toString().split("\\^")[0];
			}
			else { 
				toUpdate= details.get("current").toString();
			recovery= "NotRecovered"; 
			price= oem_inventory.get(0).get("price").toString().split("\\^")[0];
				
			}
			String q= 	 Prefix+
					"Insert {  << :"+portfolio+" :needsNode :"+node+" >> :hasQuantity \""+toUpdate+"\"."
					+"<< :"+portfolio+" :needsNode :"+node+">> :getsProduct ?p. "+
					"<< :"+portfolio+" :needsNode :"+node+">> :hasUnitPrice "+price+". "+
					"<< :"+portfolio+" :needsNode :"+node+">> :hasTimeStamp ?t.   "+ 
					"<< :"+portfolio+" :needsNode :"+node+">> :recoveredBy '"+recovery+"'}   "+ 
						"where \r\n" + 
					"{ << :"+portfolio_old+" :needsNode :"+node+" >> :hasQuantity ?q.  "
					+"<< :"+portfolio_old+" :needsNode :"+node+">> :getsProduct ?p. "+
					"<< :"+portfolio_old+" :needsNode :"+node+">> :hasUnitPrice ?price. "+
					"<< :"+portfolio_old+" :needsNode :"+node+">> :hasTimeStamp ?t.   }";  
			UpdateAction.parseExecute(q, model) ;
			String retrieve= Prefix+ "Select * WHERE { <<:"+portfolio+" :needsNode :"+node+">> :hasQuantity ?q. "
					+"<< :"+portfolio+" :needsNode :"+node+">> :getsProduct ?p. "+
					"<< :"+portfolio+" :needsNode :"+node+">> :hasUnitPrice ?price. "+
					"<< :"+portfolio+" :needsNode :"+node+">> :hasTimeStamp ?t.   }";  
		return execute(retrieve, model).get(0); //String portfolio= details.get("?portfolio").toString(); 
				
	}
		private static void add_rest_portfolio(String portfolio, OntModel model, Individual potfolio_ind) {
			String get_non_disrupted_nodes= Prefix+"Select * Where {"
					+ " << :"+portfolio+" :needsNode ?node>>  :hasTimeStamp ?t."
					+ " << :"+portfolio+" :needsNode ?node>>  :hasQuantity  ?q."
					+ " << :"+portfolio+" :needsNode ?node>>  :hasUnitPrice  ?price."
					+ " << :"+portfolio+" :needsNode ?node>>  :getsProduct  ?p. FILTER NOT EXISTS { << :"+portfolio+" :needsNode ?node>>  :isDisrupted  \"True\".}"
							+ " }"; 
			List<QuerySolution> non_nodes = execute (get_non_disrupted_nodes, model);
 			for (int k=0; k<non_nodes.size(); k++)
			{
				
				String node= non_nodes.get(k).get("node").toString().split("#")[1];
				int t= Integer.parseInt(non_nodes.get(k).get("t").toString());
				String product= non_nodes.get(k).get("p").toString();
				String quantity=non_nodes.get(k).get("q").toString();
				String price=non_nodes.get(k).get("price").toString();
				create_order_portfolio(model,node,potfolio_ind, quantity, t, product,price ); 
				
			}
	}
		private static QuerySolution try_alternative_suppliers(QuerySolution details, OntModel model, String node, String portfolio, String portfolio_old, String order) throws IOException {
		
			String t= details.get("t").toString();
			int t_= Integer.parseInt(t);
			String product= details.get("p").toString();
			String quantity= details.get("torecover").toString().split("\\^")[0];
			float quant= Float.parseFloat(quantity); 
			String price_old= details.get("price").toString();
			String price= details.get("price").toString();
		QuerySolution l= 	allocateComponent_supplier(quant, product,model, t_, node); 
		String oldNode= node; 
		String neww; 
		String recovery; 
		if (l==null) {		
			neww= details.get("?current").toString().split("\\^")[0]; 
			recovery= "NotRecvered"; 
		}
		else
		{
			neww= details.get("q").toString().split("\\^")[0];
			node= l.get("node").toString().split("#")[1]; 
			recovery= "AlternatvieSupplier"; 
			price= l.get("price").toString(); 
			if (Integer.parseInt(price)<Integer.parseInt(price_old))
			{price= price_old; }
			
		}
		String q= 	 Prefix+
				"Insert {  << :"+portfolio+" :needsNode :"+node+" >> :hasQuantity \""+neww+"\"."
				+"<< :"+portfolio+" :needsNode :"+node+">> :getsProduct ?p. "+
				"<< :"+portfolio+" :needsNode :"+node+">> :hasUnitPrice \""+price+"\"."+
				"<< :"+portfolio+" :needsNode :"+node+">> :hasTimeStamp ?t.   "+
				"<< :"+portfolio+" :needsNode :"+node+">> :recoveredBy '"+recovery+"'}   "+ 
				"where \r\n" + 
				"{ << :"+portfolio_old+" :needsNode :"+oldNode+" >> :hasQuantity ?q.  "
				+"<< :"+portfolio_old+" :needsNode :"+oldNode+">> :getsProduct ?p. "+
				"<< :"+portfolio_old+" :needsNode :"+oldNode+">> :hasUnitPrice ?price. "+
				"<< :"+portfolio_old+" :needsNode :"+oldNode+">> :hasTimeStamp ?t.   }";  
		UpdateAction.parseExecute(q, model) ;
		String retrieve= Prefix+ "Select * WHERE { <<:"+portfolio+" :needsNode :"+node+">> :hasQuantity ?q. "
				+"<< :"+portfolio+" :needsNode :"+node+">> :getsProduct ?p. "+
				"<< :"+portfolio+" :needsNode :"+node+">> :hasUnitPrice ?price. "+
				"<< :"+portfolio+" :needsNode :"+node+">> :hasTimeStamp ?t.   }";  
	return execute(retrieve, model).get(0); 

	}
		private static QuerySolution allocateComponent_supplier(float quant, String component, OntModel model,int t, String node) throws IOException {
			String q= Prefix+ "Select * \r\n" + 
					"where \r\n" + 
					"{"
					+ "?node :hasOEM :OEM1. "
					+ "?node :belongsToTier ?tier.\r\n" + 
					"?node :hasCapacity ?cap. "
					+ "?cap :hasProduct ?p. "
					+ "?cap :hasQuantity ?q. "
					+ "?cap :hasPrice ?price."
					+"?cap :hasTimeStamp ?t. "
					+ "?node :hasCapacitySaturation ?saturation. "+
			//		"NOT EXISTS { :"+node+" rdf:type :Supplier }"+
					"BIND (xsd:integer("+t+") as ?allocationtime).  \r\n" +
				"BIND (xsd:float(?q) + xsd:float("+quant+") as ?diff).   \r\n" +
			//		"BIND (xsd:integer(?quantity) as ?diff).   \r\n" +
					"FILTER  (regex(str(?tier), \"SupplierTier1\")"
			+ "&& (xsd:integer(?saturation)>= xsd:float(?diff)) && regex(str(?p),\""+component.split("#")[1]+"\")"  
					+ " && (xsd:integer(?allocationtime)= xsd:integer(?t)) " 
					+ "&& !(regex(str(?node),\'"+node+"\')) ).\r\n }"  ;  
			List<QuerySolution> l= execute (Prefix+q, model); 
			if (l.size()>0)
			{
				String allocation_t= l.get(0).get("?t").toString();  
			//	System.out.println("Supplier"+ l.get(0).get("node").toString().split("#")[1]+ "Quantity"+l.get(0).get("diff").toString().split("\\^")[0]+"component"+component+"time: "+ allocation_t); 
				allocate_supplier_product(l.get(0).get("node").toString().split("#")[1], l.get(0).get("diff").toString().split("\\^")[0], component, model,  allocation_t, t);
				//create_order_portfolio(model, l.get(0).get("snode").toString().split("#")[1], portfolio, tofullfil+"",Integer.parseInt(allocation_t),component, l.get(0).get("price").toString()); 
				return l.get(0);
			}
			else
			{
				return null;
		}
		}
		private static void allocate_supplier_product(String supplier, String y, String component, OntModel model, String allocation_t, int t) {
				String q= Prefix+"DELETE   \r\n" + 
						"{?cap :hasQuantity ?quantity.\r\n }\r\n" + 
						"Insert {?cap :hasQuantity \""+y+"\"}\r\n" + 
						"where \r\n" + 
						"{:"+supplier+" :hasCapacity ?cap.\r\n" + 
						"?cap :hasProduct ?p.\r\n" + 
						"?cap :hasQuantity ?quantity.\r\n" + 
						"?cap :hasTimeStamp \""+allocation_t+"\"."
						+ "}\r\n" ; 
				UpdateAction.parseExecute(q, model) ;
				//System.out.println("Chosen Supplier at time "+ allocation_t);
				propagate_capacity(allocation_t, t, supplier, y, model); 
				 
			}
		private static void propagate_capacity(String allocation_t, int t, String supplier, String y, OntModel model) {
			for (int i=Integer.parseInt(allocation_t); i<=t; i++)
			{
				String q= Prefix+"DELETE   \r\n" + 
						"{?cap :hasQuantity ?quantity.\r\n }\r\n" + 
						"Insert {?cap :hasQuantity \""+y+"\"}\r\n" + 
						"where \r\n" + 
						"{:"+supplier+" :hasCapacity ?cap.\r\n" + 
						"?cap :hasProduct ?p.\r\n" + 
						"?cap :hasQuantity ?quantity.\r\n" + 
						"?cap :hasPrice ?price.\r\n" + 
						"?cap :hasTimeStamp \""+i+"\"."
						+ "}\r\n" ; 
				UpdateAction.parseExecute(q, model) ;
			}
		}
		private static QuerySolution try_later_recovery(QuerySolution node_supply, OntModel model, String node, String portfolio, String portfolio_old) {
			String t= node_supply.get("t").toString();
			int t_future= Integer.parseInt(t);
			String product= node_supply.get("p").toString();
			String quantity= node_supply.get("torecover").toString();
			float quant= Float.parseFloat(quantity.split("\\^")[0]); 
			String toUpdate= node_supply.get("current").toString();;
			String recovery="NotRecovered";
			
	for (int i=1; i<5; i++) // try 5 periods after
	{
		t_future= t_future+i; 
		String query= Prefix+ "SELECT * \r\n" + 
				"	WHERE { ?node rdf:type :OEM. ?node :hasInventory ?inv. ?inv :hasProduct ?p. ?inv :hasPrice ?price. "
				+ "?inv :hasTimeStamp \""+t_future+"\". ?inv :hasQuantity ?q. "
				+ "Filter(regex(str(?p),\""+product+"\"))}"; 
		List <QuerySolution> oem_inventory= null; 
		oem_inventory= execute(query,model);
		if (oem_inventory.size()==0) // no inventory at that time in included in the model 
		{break; }
		float oem_quantity=  Float.parseFloat(oem_inventory.get(0).get("q").toString().split("\\^")[0]);
		if (oem_quantity>= quant) // can fulfill later
		{
			float neww= oem_quantity- quant; 
			 query = Prefix+"DELETE   \r\n" + 
					"{ ?inv :hasQuantity ?q."
					+ "}\r\n" + 
					"Insert {?inv :hasQuantity \""+neww+"\".}\r\n" + 
					"where \r\n" + 
					"{ ?node rdf:type :OEM. "
					+ "?node :hasInventory ?inv. "
					+ "?inv :hasProduct ?p. \r\n" + 
					"?inv :hasTimeStamp \""+t+"\". "
					+ "?inv :hasQuantity ?q. }";  
			UpdateAction.parseExecute(query, model) ;
			toUpdate=node_supply.get("q").toString();  
			recovery= "LateRecovery"; 
			System.out.println("found at time"+i+ "toupdate"+ toUpdate); 
			break; 
		}
	}
		String q= 	 Prefix+
				"Insert {  << :"+portfolio+" :needsNode :"+node+" >> :hasQuantity \""+toUpdate+"\"."
				+"<< :"+portfolio+" :needsNode :"+node+">> :getsProduct ?p. "+
				"<< :"+portfolio+" :needsNode :"+node+">> :hasUnitPrice ?price. "+
				"<< :"+portfolio+" :needsNode :"+node+">> :hasTimeStamp \""+t_future+"\".   "+ 
				"<< :"+portfolio+" :needsNode :"+node+">> :recoveredBy '"+recovery+"'}   "+ 
					
				"where \r\n" + 
				"{ << :"+portfolio_old+" :needsNode :"+node+" >> :hasQuantity ?q.  "
				+"<< :"+portfolio_old+" :needsNode :"+node+">> :getsProduct ?p. "+
				"<< :"+portfolio_old+" :needsNode :"+node+">> :hasUnitPrice ?price. "+
				"<< :"+portfolio_old+" :needsNode :"+node+">> :hasTimeStamp ?t.   }";  
		UpdateAction.parseExecute(q, model) ;
		String retrieve= Prefix+ "Select * WHERE { <<:"+portfolio+" :needsNode :"+node+">> :hasQuantity ?q. "
				+"<< :"+portfolio+" :needsNode :"+node+">> :getsProduct ?p. "+
				"<< :"+portfolio+" :needsNode :"+node+">> :hasUnitPrice ?price. "+
				"<< :"+portfolio+" :needsNode :"+node+">> :hasTimeStamp ?t.   }";  
	return execute(retrieve, model).get(0); //String portfolio= details.get("?portfolio").toString(); 
	}
		private static void create_order_portfolio(OntModel model, String node,Individual portfolio,String quantity,  int time, String product, String price) {
			Property needsNode = model.getProperty(NS+"needsNode");
			Property getsProduct = model.getProperty(NS+"getsProduct");
			Property hasTime = model.getProperty(NS+"hasTimeStamp");
			Property hasQuantity = model.getProperty(NS+"hasQuantity");
			Property hasUnitPrice = model.getProperty(NS+"hasUnitPrice");
				Statement S= model.createStatement(portfolio, needsNode,model.createResource("http://www.semanticweb.org/ramzy/ontologies/2021/3/untitled-ontology-6#"+node)); 
				Resource r = model.createResource(S);
				r.addProperty(getsProduct, product); 
				r.addProperty(hasTime,time+"");
				r.addProperty(hasQuantity,quantity);
				r.addProperty(hasUnitPrice,price);
		}

		private static List<QuerySolution> execute(String query, OntModel model )
	{
		
		Query query2 = QueryFactory.create(query); 
		QueryExecution qe2 = QueryExecutionFactory.create(query2, model);
		ResultSet results = qe2.execSelect();
		List<QuerySolution> l= new ArrayList<QuerySolution> (); 
		while (results.hasNext())
		{l.add(results.next()); }
		return l;
		
	}

}
