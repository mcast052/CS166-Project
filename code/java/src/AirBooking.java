/*
 * Template JAVA User Interface
 * =============================
 *
 * Database Management Systems
 * Department of Computer Science &amp; Engineering
 * University of California - Riverside
 *
 * Target DBMS: 'Postgres'
 *
 */


import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;
import java.text.SimpleDateFormat; 
import java.util.Date;

/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */

public class AirBooking{
	//reference to physical database connection
	private Connection _connection = null;
	static BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
	
	public AirBooking(String dbname, String dbport, String user, String passwd) throws SQLException {
		System.out.print("Connecting to database...");
		try{
			// constructs the connection URL
			String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
			System.out.println ("Connection URL: " + url + "\n");
			
			// obtain a physical connection
	        this._connection = DriverManager.getConnection(url, user, passwd);
	        System.out.println("Done");
		}catch(Exception e){
			System.err.println("Error - Unable to Connect to Database: " + e.getMessage());
	        System.out.println("Make sure you started postgres on this machine");
	        System.exit(-1);
		}
	}
	
	/**
	 * Method to execute an update SQL statement.  Update SQL instructions
	 * includes CREATE, INSERT, UPDATE, DELETE, and DROP.
	 * 
	 * @param sql the input SQL string
	 * @throws java.sql.SQLException when update failed
	 * */
	public void executeUpdate (String sql) throws SQLException { 
		// creates a statement object
		Statement stmt = this._connection.createStatement ();

		// issues the update instruction
		stmt.executeUpdate (sql);

		// close the instruction
	    stmt.close ();
	}//end executeUpdate

	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and outputs the results to
	 * standard out.
	 * 
	 * @param query the input query string
	 * @return the number of rows returned
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public int executeQueryAndPrintResult (String query) throws SQLException {
		//creates a statement object
		Statement stmt = this._connection.createStatement ();

		//issues the query instruction
		ResultSet rs = stmt.executeQuery (query);

		/*
		 *  obtains the metadata object for the returned result set.  The metadata
		 *  contains row and column info.
		 */
		ResultSetMetaData rsmd = rs.getMetaData ();
		int numCol = rsmd.getColumnCount ();
		int rowCount = 0;
		
		//iterates through the result set and output them to standard out.
		boolean outputHeader = true;
		while (rs.next()){
			if(outputHeader){
				for(int i = 1; i <= numCol; i++){
					System.out.print(rsmd.getColumnName(i) + "\t");
			    }
			    System.out.println();
			    outputHeader = false;
			}
			for (int i=1; i<=numCol; ++i)
				System.out.print (rs.getString (i) + "\t");
			System.out.println ();
			++rowCount;
		}//end while
		stmt.close ();
		return rowCount;
	}
	
	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and returns the results as
	 * a list of records. Each record in turn is a list of attribute values
	 * 
	 * @param query the input query string
	 * @return the query result as a list of records
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public List<List<String>> executeQueryAndReturnResult (String query) throws SQLException { 
		//creates a statement object 
		Statement stmt = this._connection.createStatement (); 
		
		//issues the query instruction 
		ResultSet rs = stmt.executeQuery (query); 
	 
		/*
		 * obtains the metadata object for the returned result set.  The metadata 
		 * contains row and column info. 
		*/ 
		ResultSetMetaData rsmd = rs.getMetaData (); 
		int numCol = rsmd.getColumnCount (); 
		int rowCount = 0; 
	 
		//iterates through the result set and saves the data returned by the query. 
		boolean outputHeader = false;
		List<List<String>> result  = new ArrayList<List<String>>(); 
		while (rs.next()){
			List<String> record = new ArrayList<String>(); 
			for (int i=1; i<=numCol; ++i) 
				record.add(rs.getString (i)); 
			result.add(record); 
		}//end while 
		stmt.close (); 
		return result; 
	}//end executeQueryAndReturnResult
	
	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and returns the number of results
	 * 
	 * @param query the input query string
	 * @return the number of rows returned
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public int executeQuery (String query) throws SQLException {
		//creates a statement object
		Statement stmt = this._connection.createStatement ();

		//issues the query instruction
		ResultSet rs = stmt.executeQuery (query);

		int rowCount = 0;

		//iterates through the result set and count nuber of results.
		if(rs.next()){
			rowCount++;
		}//end while
		stmt.close ();
		return rowCount;
	}
	
	/**
	 * Method to fetch the last value from sequence. This
	 * method issues the query to the DBMS and returns the current 
	 * value of sequence used for autogenerated keys
	 * 
	 * @param sequence name of the DB sequence
	 * @return current value of a sequence
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	
	public int getCurrSeqVal(String sequence) throws SQLException {
		Statement stmt = this._connection.createStatement ();
		
		ResultSet rs = stmt.executeQuery (String.format("Select currval('%s')", sequence));
		if (rs.next()) return rs.getInt(1);
		return -1;
	}

	/**
	 * Method to close the physical connection if it is open.
	 */
	public void cleanup(){
		try{
			if (this._connection != null){
				this._connection.close ();
			}//end if
		}catch (SQLException e){
	         // ignored.
		}//end try
	}//end cleanup

	/**
	 * The main execution method
	 * 
	 * @param args the command line arguments this inclues the <mysql|pgsql> <login file>
	 */
	public static void main (String[] args) {
		if (args.length != 3) {
			System.err.println (
				"Usage: " + "java [-classpath <classpath>] " + AirBooking.class.getName () +
		            " <dbname> <port> <user>");
			return;
		}//end if
		
		AirBooking esql = null;
		
		try{
			
			try {
				Class.forName("org.postgresql.Driver");
			}catch(Exception e){

				System.out.println("Where is your PostgreSQL JDBC Driver? " + "Include in your library path!");
				e.printStackTrace();
				return;
			}
			
			String dbname = args[0];
			String dbport = args[1];
			String user = args[2];
			
			esql = new AirBooking (dbname, dbport, user, "");
			
			boolean keepon = true;
			while(keepon){
				System.out.println("MAIN MENU");
				System.out.println("---------");
				System.out.println("1. Add Passenger");
				System.out.println("2. Book Flight");
				System.out.println("3. Review Flight");
				System.out.println("4. Insert or Update Flight");
				System.out.println("5. List Flights From Origin to Destination");
				System.out.println("6. List Most Popular Destinations");
				System.out.println("7. List Highest Rated Destinations");
				System.out.println("8. List Flights to Destination in order of Duration");
				System.out.println("9. Find Number of Available Seats on a given Flight");
				System.out.println("10. < EXIT");
				
				switch (readChoice()){
					case 1: AddPassenger(esql); break;
					case 2: BookFlight(esql); break;
					case 3: TakeCustomerReview(esql); break;
					case 4: InsertOrUpdateRouteForAirline(esql); break;
					case 5: ListAvailableFlightsBetweenOriginAndDestination(esql); break;
					case 6: ListMostPopularDestinations(esql); break;
					case 7: ListHighestRatedRoutes(esql); break;
					case 8: ListFlightFromOriginToDestinationInOrderOfDuration(esql); break;
					case 9: FindNumberOfAvailableSeatsForFlight(esql); break;
					case 10: keepon = false; break;
				}
			}
		}catch(Exception e){
			System.err.println (e.getMessage ());
		}finally{
			try{
				if(esql != null) {
					System.out.print("Disconnecting from database...");
					esql.cleanup ();
					System.out.println("Done\n\nBye !");
				}//end if				
			}catch(Exception e){
				// ignored.
			}
		}
	}

	public static int readChoice() {
		int input;
		// returns only if a correct value is given.
		do {
			System.out.print("Please make your choice: ");
			try { // read the integer, parse it and break.
				input = Integer.parseInt(in.readLine());
				break;
			}catch (Exception e) {
				System.out.println("Your input is invalid!");
				continue;
			}//end try
		}while (true);
		return input;
	}//end readChoice
	
	public static void AddPassenger(AirBooking esql){//1
		//Add a new passenger to the database
		try{
			String trigger_query0 = "DROP TRIGGER IF EXISTS pID_trigger ON Passenger;";
			String trigger_query = "CREATE TRIGGER pID_trigger BEFORE INSERT ON Passenger FOR EACH ROW EXECUTE PROCEDURE next_id();"; 
			esql.executeUpdate(trigger_query0); 
			esql.executeUpdate(trigger_query); 
			
			System.out.print("\tEnter your full name: ");
			String name = in.readLine(); 
			System.out.print("\tEnter your birth date (mm/dd/yyyy) : "); 
			String date = in.readLine(); 
			System.out.print("\tEnter your passport number: ");
			String passNum = in.readLine(); 
			System.out.print("\tEnter the country you are from: "); 
			String passCountry = in.readLine(); 
			 
			if(name != null && date != null && passNum != null && passCountry != null) { 
				String query = "INSERT INTO Passenger (passNum, fullName, bdate, country) VALUES (";
				query += "'" + passNum + "', '" + name + "', '" + date + "', '" + passCountry + "');"; 
				//System.out.println(query); 

				esql.executeUpdate(query); 
			}
			else { 
				System.out.print("\tCannot leave entries blank.\n"); 
			}
      }catch(Exception e){
         System.err.println (e.getMessage());
      }
	}
	
	public static void BookFlight(AirBooking esql){//2
		//Book Flight for an existing customer
		try {
			 
		} catch (Exception e) { 
			System.err.println(e.getMessage()); 
		} 
	}
	
	public static void TakeCustomerReview(AirBooking esql){//3
		try {
			//Gets initial information from user for queries
			System.out.print("\tEnter your full name: "); 
			String name = in.readLine(); 
			System.out.print("\tEnter the flight number: "); 
			String flightNum = in.readLine(); 
			
			//Query to find pID 
			String query0 = "SELECT pID FROM Passenger WHERE fullName = '"; 
			query0 += name + "';"; 
			List<List<String>> query0_result = esql.executeQueryAndReturnResult(query0); 
			String passID = query0_result.get(0).get(0); 
			System.out.print(passID); 
			
			//Query checks if passenger is in the booking table for that flight
			String query1 = "SELECT * FROM Booking WHERE flightNum = "; 
			query1 += "'" + flightNum + "' AND pID = '" + passID + "';";
			System.out.print(query1); 
			List<List<String>> query1_result = esql.executeQueryAndReturnResult(query1); 
			
			//If passenger exists, allow them to create a review
			System.out.print("\tEnter your rating score 1-5, where 1 is poor and 5 is excellent: "); 
			int score = Integer.parseInt(in.readLine()); 
			System.out.print("\tEnter a comment (optional): "); 
			String comment = in.readLine(); 
			
			//Query that drops/creates trigger for rID
			String drop_trigger = "DROP TRIGGER IF EXISTS rID_trigger ON Ratings;"; 
			String review_trigger = "CREATE TRIGGER rID_trigger BEFORE INSERT ON Ratings FOR EACH ROW EXECUTE PROCEDURE next_rid();"; 
			esql.executeUpdate(drop_trigger); 
			esql.executeUpdate(review_trigger); 
			
			//Insert customer review into the ratings table
			String insert_query = "INSERT INTO Ratings (pID, flightNum, score, comment) VALUES ('"; 
			insert_query += passID + "', '" + flightNum + "', '" + score + "', '" + comment + "');"; 
			System.out.print(insert_query); 
			esql.executeUpdate(insert_query);  
		}
		catch(Exception e) {
			System.err.println(e.getMessage()); 
			//System.out.print("Could not find passenger for the specified flight\n"); 
		} 
	
		
	}
	
	public static void InsertOrUpdateRouteForAirline(AirBooking esql){//4
		//Insert a new route for the airline
	}
	
	public static void ListAvailableFlightsBetweenOriginAndDestination(AirBooking esql) throws Exception{//5
		//List all flights between origin and distination (i.e. flightNum,origin,destination,plane,duration) 
	}
	
	public static void ListMostPopularDestinations(AirBooking esql){//6
		//Print the k most popular destinations based on the number of flights offered to them (i.e. destination, choices)
		try { 
			System.out.print("\tEnter the number of destinations you would like to see: ");
			int k = Integer.parseInt(in.readLine()); 
			
			String query = "SELECT destination, COUNT(*) FROM Flight GROUP BY destination ORDER BY COUNT(*) DESC;"; 
			List<List<String>> top_Dest = esql.executeQueryAndReturnResult(query); 
			
			if ( k > top_Dest.size() ) { 
				for(int i = 0; i < top_Dest.size(); i++) { 
					System.out.print(i+1); 
					System.out.println(" " + top_Dest.get(i).get(0)); 
				} 
			}
			else { 
				for(int i = 0; i < k; i++) { 
					System.out.print(i+1); 
					System.out.println(". " + top_Dest.get(i).get(0)); 
				}
			}
		} catch(Exception e) { 
			System.err.println(e.getMessage()); 
		}
	}
	
	public static void ListHighestRatedRoutes(AirBooking esql){//7
		//List the k highest rated Routes (i.e. Airline Name, flightNum, Avg_Score)
	}
	
	public static void ListFlightFromOriginToDestinationInOrderOfDuration(AirBooking esql){//8
		//List flight to destination in order of duration (i.e. Airline name, flightNum, origin, destination, duration, plane)
	}
	
	public static void FindNumberOfAvailableSeatsForFlight(AirBooking esql){//9
		//
		
	}
	
}
