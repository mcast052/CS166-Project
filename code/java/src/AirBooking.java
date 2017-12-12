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
			
			String fname, lname, name; 
			do { 
				System.out.print("\tEnter your first name: ");
				fname = in.readLine(); 
				if(fname.length() == 0) { 
					System.out.print("\tInvalid entry. Try again or enter 1 to exit. ");
					String exit = in.readLine();
					if(exit.length() > 0 && Integer.parseInt(exit) == 1) { return; } 
				} 
			} while (fname.length() == 0); 
			do { 
				System.out.print("\tEnter your last name: "); 
				lname = in.readLine(); 
				if(lname.length() == 0) {
					System.out.print("\tInvalid entry. Try again or enter 1 to exit. ");
					String exit = in.readLine();
					if(exit.length() > 0 && Integer.parseInt(exit) == 1) { return; } 
				} 
			} while (lname.length() == 0); 
			
			name = fname + " " + lname; 
			String date; 
			do { 
				System.out.print("\tEnter your birth date (mm/dd/yyyy) : "); 
				date = in.readLine(); 
				if(date.length() == 0 || (date.charAt(2) != '/' && date.charAt(5) != '/') ) { 
					System.out.print("\tInvalid entry. Try again or enter 1 to exit. ");
					String exit = in.readLine();
					if(exit.length() > 0 && Integer.parseInt(exit) == 1) { return; } 
				}
			} while (date.length() == 0); 
			
			String passNum; 
			do { 
				System.out.print("\tEnter your passport number: ");
				passNum = in.readLine(); 
				if(passNum.length() == 0 || passNum.length() > 10) { 
					System.out.print("\tInvalid entry. Try again or enter 1 to exit. ");
					String exit = in.readLine();
					if(exit.length() > 0 && Integer.parseInt(exit) == 1) { return; } 
				}
			} while (passNum.length() == 0 || passNum.length() > 10); 
			
			String passCountry; 
			do { 
				System.out.print("\tEnter the country you are from: "); 
				passCountry = in.readLine(); 
				if (passCountry.length() == 0) { 
					System.out.print("\tInvalid entry. Try again or enter 1 to exit. ");
					String exit = in.readLine();
					if(exit.length() > 0 && Integer.parseInt(exit) == 1) { return; } 
				}
			} while (passCountry.length() == 0); 
			 
			String query = "INSERT INTO Passenger (passNum, fullName, bdate, country) VALUES (";
			query += "'" + passNum + "', '" + name + "', '" + date + "', '" + passCountry + "');"; 
			//System.out.println(query); 

			esql.executeUpdate(query); 
      }catch(Exception e){
         System.err.println (e.getMessage());
      }
	}
	
	public static void BookFlight(AirBooking esql){//2
		//Book Flight for an existing customer
		try {
				String trigger_query0 = "DROP TRIGGER IF EXISTS bookRef_trigger ON Booking;";
				String trigger_query = "CREATE TRIGGER bookRef_trigger BEFORE INSERT ON Booking FOR EACH ROW EXECUTE PROCEDURE next_bookRef();"; 
				esql.executeUpdate(trigger_query0); 
				esql.executeUpdate(trigger_query);
				int exit = 0;
				
				System.out.print("\tEnter your full name: "); 
				String name = in.readLine(); 
				System.out.print("\tEnter your passport number: "); 
				String pass = in.readLine(); 
				
				String query0 = "Select * From Passenger where fullname = '"; 
				query0 += name;
				query0 += "' AND passnum = '";
				query0 += pass;
				query0 += "';"; 
				List<List<String>> q_result = esql.executeQueryAndReturnResult(query0); 
				int query0_result = q_result.size();
				
				if(query0_result == 0)
				{
					while(query0_result == 0)
					{
						System.out.print("\tYou did not enter a valid fullname or passport#. Press 0 to try again or 1 to exit. "); 
						String choice = in.readLine();
						if(Integer.parseInt(choice) == 0)
						{
							System.out.print("\tEnter your full name: "); 
							name = in.readLine(); 
							System.out.print("\tEnter your passport number: "); 
							pass = in.readLine(); 
							
							query0 = "Select * From Passenger where fullname = '"; 
							query0 += name;
							query0 += "' AND passnum = '";
							query0 += pass;
							query0 += "';"; 
							q_result = esql.executeQueryAndReturnResult(query0); 
							query0_result = q_result.size();
						}
						else if(Integer.parseInt(choice) == 1)
						{
							return;
						}
						else
						{
							System.out.println("\tYou did not enter a valid choice.");
						}
					}
				}
				
				int pId = Integer.parseInt(q_result.get(0).get(0));
				System.out.println("\tHi " +name+ "!");
				System.out.print("\tEnter where you plan to fly from: "); 
				String origin = in.readLine(); 
				System.out.print("\tEnter where you plan to fly to: "); 
				String destination = in.readLine(); 
				
				String query1 = "Select * From Flight Where origin = '" + origin + "' AND destination = '" + destination + "';"; 
				List<List<String>> query1_result = esql.executeQueryAndReturnResult(query1); 
				int flightCount = query1_result.size();
				
				while(flightCount == 0)
				{
					System.out.print("\tNo flights from " + origin + " to " + destination + " are available. Press 0 to try again and 1 to exit."); 
					String choice = in.readLine();
					if(Integer.parseInt(choice) == 0)
					{
						System.out.print("\tEnter where you plan to fly from: "); 
						origin = in.readLine(); 
						System.out.print("\tEnter where you plan to fly to: "); 
						destination = in.readLine();
						
						query1 = "Select * From Flight Where origin = '"; 
						query1 += origin;
						query1 += "' AND destination = '";
						query1 += destination;
						query1 += "';"; 
						query1_result = esql.executeQueryAndReturnResult(query1); 
						flightCount = query1_result.size();
					}
					else if(Integer.parseInt(choice) == 1)
					{
						return;
					}
					else
					{
						System.out.println("\tYou did not enter a valid choice.");
					}
				}
				
				for(int i = 0; i < flightCount;i++)
				{
					String tuple = "\t(" + i + ") Flight Number: " + query1_result.get(i).get(1) + " Plane: " + query1_result.get(i).get(4) + " Seats: " + query1_result.get(i).get(5) + " Duration: " + query1_result.get(i).get(6);
					System.out.println(tuple);
				}
				 
				System.out.print("\tEnter the index, in the (), of the flight you would like to take. Enter -1 if you would like to exit. "); 
				String choiceFlight = in.readLine();
				
				if(Integer.parseInt(choiceFlight) == -1)
				{
					return;
				}
				else
				{
					while(Integer.parseInt(choiceFlight) < 0 || Integer.parseInt(choiceFlight) >= flightCount)
					{
						System.out.println("\tYou did not enter a valid index."); 
						System.out.print("\tEnter the index, in the (), of the flight you would like to take. Enter -1 if you would like to exit. "); 
						choiceFlight = in.readLine();
						if(Integer.parseInt(choiceFlight) == -1)
						{
							return;
						}
					}
				}
				
				List<String> flightChosen = query1_result.get(Integer.parseInt(choiceFlight));
				
				System.out.print("\tEnter the year you would like to take the flight. (After 2016) ");
				String year = in.readLine();
				while(Integer.parseInt(year) <= 2016)
				{
					System.out.println("\tPlease enter a valid year.");
					System.out.print("\tEnter the year you would like to take the flight. (After 2016 or -1 to exit) ");
					year = in.readLine();
					if(Integer.parseInt(year) == -1)
					{
						return;
					}
				}
				System.out.print("\tEnter the month you would like to take the flight. (Between 1-12) ");
				String month = in.readLine();
				while(Integer.parseInt(month) < 1 || Integer.parseInt(month) > 12)
				{
					System.out.println("\tPlease enter a valid month.");
					System.out.print("\tEnter the month you would like to take the flight. (Between 1-12 or -1 to exit) ");
					month = in.readLine();
					if(Integer.parseInt(month) == -1)
					{
						return;
					}
				}
				System.out.print("\tEnter the day you would like to take the flight. (Between 1 - 31) ");
				String day = in.readLine();
				while(Integer.parseInt(day) < 1 || Integer.parseInt(day) > 31)
				{
					System.out.println("\tPlease enter a valid day.");
					System.out.print("\tEnter the day you would like to take the flight. (Between 1 - 31 or -1 to exit) ");
					day = in.readLine();
					if(Integer.parseInt(day) == -1)
					{
						return;
					}
				}
				String date = year + "-" + month +"-" + day;
				String query2=  "Select * From Booking B Where B.flightNum = '";
				 query2 += flightChosen.get(1);
				 query2 += "' AND B.departure = '";
				 query2 += date;
				 query2 += "';";
				 List<List<String>> q_result2 = esql.executeQueryAndReturnResult(query2);
				 int query_result2 = q_result2.size();
				 int numSeats = Integer.parseInt(flightChosen.get(5));
				 System.out.println("\tNum of seats left: " + (numSeats - query_result2));
				 if(numSeats - query_result2 > 0)
				 {
					 System.out.print("\tGreat! It seems like that date works. Would you like to book this flight? (Yes or No) ");
					 String bookChoice = in.readLine();
					 if(bookChoice.equals("No"))
					 {
						 return;
					 }
					 while(!bookChoice.equals("Yes"))
					 {
						 System.out.println("\tPlease enter a valid input.");
						 System.out.print("\tGreat! It seems like that date works. Would you like to book this flight? (Yes or No) ");
						 bookChoice = in.readLine();
						 if(bookChoice.equals("No"))
						 {
							 return;
						 }
					 }
					 
					 String query3=  "Select * From Booking B Where B.flightNum = '"+ flightChosen.get(1) + "' AND B.departure = '" + date + "' AND pId = '" +pId+ "';";
					 List<List<String>> q_result3 = esql.executeQueryAndReturnResult(query3);
					 int query_result3 = q_result3.size();
					 if(query_result3 > 0)
					 {
						 System.out.println("\tSorry you already booked this same flight and departure time!"); 
					 }
					 else
					 {
						 String queryLast = "INSERT INTO Booking (departure, flightNum, pId) VALUES (";
						 queryLast += "'" + date + "', '" + flightChosen.get(1) + "', '" + pId +"');"; 
						 System.out.println("\tYour flight has been successfully booked!"); 

						 esql.executeUpdate(queryLast);
					 }
				 }
				 else
				 {
					 System.out.println("\tSorry! That flight is fully booked.");
					 return;
				 }
				 
				 
	//Insert into âBookingâ Values ($departure,$flightNum,$pId) // the first value which is bookref auto increments

			} catch (Exception e) { 
				System.err.println(e.getMessage()); 
			}

	}
	
	public static void TakeCustomerReview(AirBooking esql){//3
		try {
			//Gets initial information from user for queries
			String name, flightNum; 
			String passID = ""; 
			boolean invalid = true; 
			do { 
				System.out.print("\tEnter your full name: "); 
				name = in.readLine(); 
				if (name.length() == 0) { 
					System.out.print("\tInvalid entry. Try again or enter 1 to exit. ");
					String exit = in.readLine();
					if(exit.length() > 0 && Integer.parseInt(exit) == 1) { return; } 
				} 
				else { 
					//Query to find pID 
					String query0 = "SELECT pID FROM Passenger WHERE fullName = '"; 
					query0 += name + "';"; 
					List<List<String>> query0_result = esql.executeQueryAndReturnResult(query0); 
					if(query0_result.size() == 0) { 
						System.out.print("\tInvalid entry. Try again or enter 1 to exit. ");
						String exit = in.readLine();
						if(exit.length() > 0 && Integer.parseInt(exit) == 1) { return; } 
					}
					else { 
						passID = query0_result.get(0).get(0); 
						//System.out.print(passID);
						invalid = false; 
					} 
				}
			} while (invalid); 
			
			invalid = true; 
			do { 
				System.out.print("\tEnter the flight number: "); 
				flightNum = in.readLine(); 
				if(flightNum.length() == 0) { 
					System.out.print("\tInvalid entry. Try again or enter 1 to exit. ");
					String exit = in.readLine();
					if(exit.length() > 0 && Integer.parseInt(exit) == 1) { return; } 
				} 
				else {
					//Query checks if passenger is in the booking table for that flight
					String query1 = "SELECT * FROM Booking WHERE flightNum = "; 
					query1 += "'" + flightNum + "' AND pID = '" + passID + "';";
					//System.out.print(query1); 
					List<List<String>> query1_result = esql.executeQueryAndReturnResult(query1); 
					if(query1_result.size() == 0) { 
						System.out.print("\tInvalid flight number. Passenger not found for this flight. Try again or enter 1 to exit. "); 
						String exit = in.readLine(); 
						if(exit.length() > 0 && Integer.parseInt(exit) == 1) { return; } 
					}
					else { 
						invalid = false; 
					} 
				}
			} while (invalid); 
			
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
			//System.out.print(insert_query); 
			esql.executeUpdate(insert_query);  
		}
		catch(Exception e) {
			System.err.println(e.getMessage()); 
			//System.out.print("Could not find passenger for the specified flight\n"); 
		} 
	
		
	}
	
	public static void InsertOrUpdateRouteForAirline(AirBooking esql){//4
		//Insert a new route for the airline
		try{
			System.out.print("\tWould you like to insert (enter 1) or update(enter 2) a flight? (Press 0 to exit) ");
			String choice = in.readLine();
			if(Integer.parseInt(choice) == 0)
			{
				return;
			}
			while(Integer.parseInt(choice) != 1 && Integer.parseInt(choice) != 2)
			{
				System.out.println("\tPlease insert a valid choice.");
				System.out.print("\tWould you like to insert (enter 1) or update (enter 2) a flight? (Press 0 to exit) ");
				choice = in.readLine();
				if(Integer.parseInt(choice) == 0)
				{
					return;
				}
			}
			
			if(Integer.parseInt(choice) == 1)
			{
				String query = "SELECT * FROM Airline;";
				esql.executeQueryAndPrintResult(query);
				System.out.print("\tGreat! Please select the airline from the list above using its airId. (Enter -1 to return to main menu) " );
				String airId = in.readLine();
				if(Integer.parseInt(airId) == -1)
				{
					return;
				}
				String query0 = "SELECT * FROM Airline WHERE airId = '"; 
				query0 += airId + "';"; 
				List<List<String>> query0_result = esql.executeQueryAndReturnResult(query0);
				while(query0_result.size()== 0)
				{
					System.out.println("\tSorry, you entered an invalid airId." );
					System.out.print("\tPlease select the airline from the list above using its airId. (Enter -1 to return to main menu) " );
					airId = in.readLine();
					if(Integer.parseInt(airId) == -1)
					{
						return;
					}
					query0 = "SELECT * FROM Airline WHERE airId = '"; 
					query0 += airId + "';"; 
					query0_result = esql.executeQueryAndReturnResult(query0);
				}
				//String query1 = "SELECT * FROM Flight Where airId='" + query0_result.get(0).get(0) + "';";
				//esql.executeQueryAndPrintResult(query1);
				System.out.print("\tEnter origin: ");
				String origin = in.readLine();
				System.out.print("\tEnter destination: ");
				String destination = in.readLine();
				System.out.print("\tEnter plane: ");
				String plane = in.readLine();
				//String query1 = "Select * FROM Flight Where Plane = '" + plane + "';";
				//List<List<String>> query1_result = esql.executeQueryAndReturnResult(query1);
				System.out.print("\tEnter seat number: ");
				String seats = in.readLine();
			    while(Integer.parseInt(seats) < 1)
			    {
					System.out.print("\tInvalid seat number. Please enter seat number: ");
					seats = in.readLine();
				}
				System.out.print("\tEnter flight duration: ");
				String duration = in.readLine();
				while(Integer.parseInt(duration) < 1)
			    {
					System.out.print("\tInvalid flight duration. Please enter flight duration: ");
					duration = in.readLine();
				}
				System.out.print("\tEnter flight number: ");
				String flightNum = in.readLine();
				String queryLast = "INSERT INTO Flight (airId, flightNum, origin, destination, plane, seats, duration) VALUES (";
				queryLast += "'" + airId + "', '" + flightNum + "', '" + origin + "', '" + destination + "', '" + plane +"', '" + seats +"', '" + duration +"');"; 
				System.out.println("\tYour have successfully created a flight!"); 
                esql.executeUpdate(queryLast);
			}
			else if(Integer.parseInt(choice) == 2)
			{
				System.out.print("\tEnter flight number: ");
				String flightNum = in.readLine();
				
				String query = "SELECT * FROM Flight Where flightNum = '" +flightNum+ "';";
				List<List<String>> query_result = esql.executeQueryAndReturnResult(query);
				while(query_result.size() == 0)
				{
					System.out.print("\tNo flight found. Please enter a flight number. (Enter Exit to return to main menu) ");
					flightNum = in.readLine();
					if(flightNum.equals("Exit"))
					{
						return;
					}
					query = "SELECT * FROM Flight Where flightNum = '" +flightNum+ "';";
					query_result = esql.executeQueryAndReturnResult(query);
				}
				//airId INTEGER NOT NULL,
				//flightNum CHAR(8) NOT NULL,
				//origin CHAR(16) NOT NULL,
				//destination CHAR(16) NOT NULL,
				//plane CHAR(16) NOT NULL,
				//seats _SEATS NOT NULL,
	//duration _HOURS NOT NULL,
				String airId = query_result.get(0).get(0);
				String origin = query_result.get(0).get(2);
				String destination = query_result.get(0).get(3);
				String plane = query_result.get(0).get(4);
				String seats = query_result.get(0).get(5);
				String duration = query_result.get(0).get(6);
				esql.executeQueryAndPrintResult(query);
				System.out.print("\tWould you like the update the origin? (Yes or No) ");
				String newOrigin = in.readLine();
				
				while(!newOrigin.equals("Yes") && !newOrigin.equals("No"))
				{
					System.out.print("\tYou did not enter a valid response. Would you like the update the origin? (Yes or No or Exit to go to main menu) ");
					newOrigin = in.readLine();
					if(newOrigin.equals("Exit"))
					{
						return;
					}
				}
				if(newOrigin.equals("Yes"))
				{
					System.out.print("\tPlease enter a new origin: ");
					origin = in.readLine();
				}
				
				System.out.print("\tWould you like the update the destination? (Yes or No) ");
				String newDestination = in.readLine();
				while(!newDestination.equals("Yes") && !newDestination.equals("No"))
				{
					System.out.print("\tYou did not enter a valid response. Would you like the update the destination? (Yes or No or Exit to go to main menu) ");
					newDestination = in.readLine();
					if(newDestination.equals("Exit"))
					{
						return;
					}
				}
				if(newDestination.equals("Yes"))
				{
					System.out.print("\tPlease enter a new destination: ");
					destination = in.readLine();
				}
				
				System.out.print("\tWould you like the update the plane? (Yes or No) ");
				String newPlane = in.readLine();
				while(!newPlane.equals("Yes") && !newPlane.equals("No"))
				{
					System.out.print("\tYou did not enter a valid response. Would you like the update the plane? (Yes or No or Exit to go to main menu) ");
					newPlane = in.readLine();
					if(newPlane.equals("Exit"))
					{
						return;
					}
				}
				if(newPlane.equals("Yes"))
				{
					System.out.print("\tPlease enter a new plane: ");
					plane = in.readLine();
				}
				
				System.out.print("\tWould you like the update the seat number? (Yes or No) ");
				String newSeats = in.readLine();
				while(!newSeats.equals("Yes") && !newSeats.equals("No"))
				{
					System.out.print("\tYou did not enter a valid response. Would you like the update the seat number? (Yes or No or Exit to go to main menu) ");
					newSeats = in.readLine();
					if(newSeats.equals("Exit"))
					{
						return;
					}
				}
				if(newSeats.equals("Yes"))
				{
					System.out.print("\tPlease enter a new seat number: ");
					seats = in.readLine();
				}
				
				System.out.print("\tWould you like the update the duration? (Yes or No) ");
				String newDuration = in.readLine();
				while(!newDuration.equals("Yes") && !newDuration.equals("No"))
				{
					System.out.print("\tYou did not enter a valid response. Would you like the update the duration? (Yes or No or Exit to go to main menu) ");
					newDuration = in.readLine();
					if(newDuration.equals("Exit"))
					{
						return;
					}
				}
				if(newDuration.equals("Yes"))
				{
					System.out.print("\tPlease enter a new duration: ");
					duration = in.readLine();
				}
				
				String queryLast = "UPDATE Flight SET origin = '" +origin+ "', destination = '" +destination+ "', plane = '" +plane+ "', seats = '" +seats+ "', duration ='" +duration+ "'Where flightNum = '" +flightNum+ "';";
				System.out.println("\tYou have successfully updated the flight!"); 
				//System.out.println(queryLast); 
                esql.executeUpdate(queryLast);
                esql.executeQueryAndPrintResult(query);
			}
		  }catch(Exception e){
			 System.err.println (e.getMessage());
		  }
	}
	
	public static void ListAvailableFlightsBetweenOriginAndDestination(AirBooking esql) throws Exception{//5
		//List all flights between origin and distination (i.e. flightNum,origin,destination,plane,duration) 
		try{
			 System.out.print("\tEnter origin: ");
			 String origin = in.readLine();
			 System.out.print("\tEnter destination: ");
			 String destination = in.readLine();
			 String query =  "Select flightNum, origin,destination, plane,duration From Flight F Where F.destination = '";
			 query += destination;
			 query += "' AND F.origin = '";
			 query += origin;
			 query += "';";

			 int rowCount = esql.executeQueryAndPrintResult(query);
			 while(rowCount == 0)
			 {
				 System.out.print("\tThere are no flights from "+ origin +" to "+ destination +". Would you like to try again? (Yes or No) ");
				 String response = in.readLine();
				 if(response.equals("No"))
				 {
					 return;
				 }
				 System.out.print("\tEnter origin: ");
				 origin = in.readLine();
				 System.out.print("\tEnter destination: ");
				 destination = in.readLine();
				 query =  "Select flightNum, origin,destination, plane,duration From Flight F Where F.destination = '";
				 query += destination;
				 query += "' AND F.origin = '";
				 query += origin;
				 query += "';";
				 rowCount = esql.executeQueryAndPrintResult(query);
			 }
		  }catch(Exception e){
			 System.err.println (e.getMessage());
		  }
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
					System.out.println(". " + top_Dest.get(i).get(0)); 
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
		try{
			 String query =  "Select origin, destination, AVG(SCORE) as Average_Score,F.flightNum From Ratings R, Flight F Where R.flightNum = F.flightNum Group By F.flightNum,r.rId Order By AVG(SCORE) DESC Limit ";
			 System.out.print("\tEnter k: ");
			 String input = in.readLine();
			 query += input;
			 query += ";";

			 int rowCount = esql.executeQueryAndPrintResult(query);
			 if(rowCount == 0)
			 {
				 System.out.println("\tThere are no reviews.");
			 }
		  }catch(Exception e){
			 System.err.println (e.getMessage());
		  }

	}
	
	public static void ListFlightFromOriginToDestinationInOrderOfDuration(AirBooking esql){//8
		//List flight to destination in order of duration (i.e. Airline name, flightNum, origin, destination, duration, plane)
		try { 
			String origin, dest; 
			boolean invalid = true; 
			do { 
				System.out.print("\tEnter the flight origin: "); 
				origin = in.readLine(); 
				if(origin.length() == 0) { 
					System.out.print("\tCannot leave entry blank. Try again or enter 1 to exit. "); 
					String exit = in.readLine(); 
					if(exit.length() > 0 && Integer.parseInt(exit) == 0) { return; } 
				} 
				else { 
					String origin_check = "SELECT * FROM Flight WHERE origin = '" + origin + "';"; 
					List<List<String>> origin_res = esql.executeQueryAndReturnResult(origin_check); 
					if(origin_res.size() == 0) { 
						System.out.print("\tInvalid origin. Try again or enter 1 to exit. "); 
						String exit = in.readLine(); 
						if(exit.length() > 0 && Integer.parseInt(exit) == 0) { return; } 
					} 
					else { invalid = false; } 
				}
			} while (invalid);
			
			invalid = true; 
			do { 
				System.out.print("\tEnter the flight destination: "); 
				dest = in.readLine(); 
				if(dest.length() == 0) { 
					System.out.print("\tCannot leave entry blank. Try again or enter 1 to exit. "); 
					String exit = in.readLine(); 
					if(exit.length() > 0 && Integer.parseInt(exit) == 0) { return; } 
				} 
				else { 
					String dest_check = "SELECT * FROM Flight WHERE destination = '" + dest + "';"; 
					List<List<String>> dest_res = esql.executeQueryAndReturnResult(dest_check); 
					if(dest_res.size() == 0) { 
						System.out.print("\tInvalid destination. Try again or enter 1 to exit. "); 
						String exit = in.readLine(); 
						if(exit.length() > 0 && Integer.parseInt(exit) == 0) { return; } 
					} 
					else { invalid = false; } 
				} 
			} while (invalid); 
			
			System.out.print("\tEnter the number of flights you would like to see: ");
			int k = Integer.parseInt(in.readLine()); 
			
			String query = "SELECT A.name, F.flightNum, F.origin, F.destination, F.duration, F.plane FROM Airline A, FLight F WHERE F.airId = A.airID AND origin = '";
			query += origin + "' AND destination = '" + dest + "' ORDER BY F.duration ASC"; 
			
			List<List<String>> flights = esql.executeQueryAndReturnResult(query); 
			
			if ( k > flights.size() ) { 
				System.out.print("Airline \t Flight Number \t Origin \t Destination \t Duration \t Plane"); 
				System.out.println();
				for(int i = 0; i < flights.size(); i++) { 
					for(int j = 0; j < flights.get(i).size(); j++) { 
						System.out.print(flights.get(i).get(j));
						System.out.print("\t");  
					}
					System.out.println();
				} 
			}
			else { 
				System.out.print("Airline \t Flight Number \t Origin \t Destination \t Duration \t Plane"); 
				System.out.println();
				for(int i = 0; i < k; i++) { 
					for(int j = 0; j < flights.get(i).size(); j++) { 
						System.out.print(flights.get(i).get(j));
						//System.out.print("\t");  
					}
					System.out.println();
				} 
			}
		} catch(Exception e) { 
			System.err.println(e.getMessage()); 
		}
	}
	
	public static void FindNumberOfAvailableSeatsForFlight(AirBooking esql){//9
		//
		try{
			 System.out.print("\tEnter Flight Number: ");
			 String input = in.readLine();
			 System.out.print("\tEnter the year of the flight you are looking for. (After 2016) ");
				String year = in.readLine();
				while(Integer.parseInt(year) <= 2016)
				{
					System.out.println("\tPlease enter a valid year.");
					System.out.print("\tEnter the year of the flight you are looking for. (After 2016 or -1 to exit) ");
					year = in.readLine();
					if(Integer.parseInt(year) == -1)
					{
						return;
					}
				}
				System.out.print("\tEnter the month of the flight you are looking for. (Between 1-12) ");
				String month = in.readLine();
				while(Integer.parseInt(month) < 1 || Integer.parseInt(month) > 12)
				{
					System.out.println("\tPlease enter a valid month.");
					System.out.print("\tEnter the month of the flight you are looking for. (Between 1-12 or -1 to exit) ");
					month = in.readLine();
					if(Integer.parseInt(month) == -1)
					{
						return;
					}
				}
				System.out.print("\tEnter the day of the flight you are looking for. (Between 1 - 31) ");
				String day = in.readLine();
				while(Integer.parseInt(day) < 1 || Integer.parseInt(day) > 31)
				{
					System.out.println("\tPlease enter a valid day.");
					System.out.print("\tEnter the day of the flight you are looking for. (Between 1 - 31 or -1 to exit) ");
					day = in.readLine();
					if(Integer.parseInt(day) == -1)
					{
						return;
					}
				}
				String date = year + "-" + month +"-" + day;
			 
			 String query1=  "Select * From Flight F Where F.flightNum = '";
			 query1 += input;
			 query1 += "';";
			 List<List<String>> str = esql.executeQueryAndReturnResult(query1);
			 
			 while(str.size() == 0)
			 {
				 System.out.print("\tSorry you did not enter a valid flight number. Type \"Exit\" if you would like the exit or type the flight number again. ");
				 input= in.readLine();
				 if(input.equals("Exit"))
				 {
					 return;
				 }
				 query1=  "Select * From Flight F Where F.flightNum = '";
				 query1 += input;
				 query1 += "';";
				 str = esql.executeQueryAndReturnResult(query1);
			 }
			 
			 String flightNum = str.get(0).get(1);
			 flightNum.replaceAll("\\s+","");
			 String origin = str.get(0).get(2);
			 origin.replaceAll("\\s+","");
			 String destination = str.get(0).get(3);
			 int numSeats = Integer.parseInt(str.get(0).get(5));
			 
			 String query=  "Select * From Booking B Where B.flightNum = '";
			 query += input;
			 query += "' AND B.departure = '";
			 query += date;
			 query += "';";
			 
			 List<List<String>> rcList = esql.executeQueryAndReturnResult(query);
			 int rowcount = rcList.size();
			 
			 int seatsAvailable = numSeats - rowcount;
			 System.out.println("\tFor FlightNum: "+flightNum+", the origin is: " + origin +", the destination is: "+ destination +", the number of booked seats is: "+ rowcount
			 + ", the number of total seats is: " + numSeats + ", and the number of seats available is: " + seatsAvailable);
		  }catch(Exception e){
			 System.err.println (e.getMessage());
		  }
	}
	
}
