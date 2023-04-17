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
import java.lang.Math;



/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */
public class Hotel {

   // reference to physical database connection.
   private Connection _connection = null;

   // handling the keyboard inputs through a BufferedReader
   // This variable can be global for convenience.
   static BufferedReader in = new BufferedReader(
                                new InputStreamReader(System.in));

   /**
    * Creates a new instance of Hotel 
    *
    * @param hostname the MySQL or PostgreSQL server hostname
    * @param database the name of the database
    * @param username the user name used to login to the database
    * @param password the user login password
    * @throws java.sql.SQLException when failed to make a connection.
    */
   public Hotel(String dbname, String dbport, String user, String passwd) throws SQLException {

      System.out.print("Connecting to database...");
      try{
         // constructs the connection URL
         String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
         System.out.println ("Connection URL: " + url + "\n");

         // obtain a physical connection
         this._connection = DriverManager.getConnection(url, user, passwd);
         System.out.println("Done");
      }catch (Exception e){
         System.err.println("Error - Unable to Connect to Database: " + e.getMessage() );
         System.out.println("Make sure you started postgres on this machine");
         System.exit(-1);
      }//end catch
   }//end Hotel

   // Method to calculate euclidean distance between two latitude, longitude pairs. 
   public double calculateDistance (double lat1, double long1, double lat2, double long2){
      double t1 = (lat1 - lat2) * (lat1 - lat2);
      double t2 = (long1 - long2) * (long1 - long2);
      return Math.sqrt(t1 + t2); 
   }
   /**
    * Method to execute an update SQL statement.  Update SQL instructions
    * includes CREATE, INSERT, UPDATE, DELETE, and DROP.
    *
    * @param sql the input SQL string
    * @throws java.sql.SQLException when update failed
    */
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
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the query instruction
      ResultSet rs = stmt.executeQuery (query);

      /*
       ** obtains the metadata object for the returned result set.  The metadata
       ** contains row and column info.
       */
      ResultSetMetaData rsmd = rs.getMetaData ();
      int numCol = rsmd.getColumnCount ();
      int rowCount = 0;

      // iterates through the result set and output them to standard out.
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
   }//end executeQuery

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
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the query instruction
      ResultSet rs = stmt.executeQuery (query);

      /*
       ** obtains the metadata object for the returned result set.  The metadata
       ** contains row and column info.
       */
      ResultSetMetaData rsmd = rs.getMetaData ();
      int numCol = rsmd.getColumnCount ();
      int rowCount = 0;

      // iterates through the result set and saves the data returned by the query.
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
       // creates a statement object
       Statement stmt = this._connection.createStatement ();

       // issues the query instruction
       ResultSet rs = stmt.executeQuery (query);

       int rowCount = 0;

       // iterates through the result set and count nuber of results.
       while (rs.next()){
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
      if (rs.next())
         return rs.getInt(1);
      return -1;
   }

   public int getNewUserID(String sql) throws SQLException {
      Statement stmt = this._connection.createStatement ();
      ResultSet rs = stmt.executeQuery (sql);
      if (rs.next())
         return rs.getInt(1);
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
            "Usage: " +
            "java [-classpath <classpath>] " +
            Hotel.class.getName () +
            " <dbname> <port> <user>");
         return;
      }//end if

      Greeting();
      Hotel esql = null;
      try{
         // use postgres JDBC driver.
         Class.forName ("org.postgresql.Driver").newInstance ();
         // instantiate the Hotel object and creates a physical
         // connection.
         String dbname = args[0];
         String dbport = args[1];
         String user = args[2];
         esql = new Hotel (dbname, dbport, user, "");

         boolean keepon = true;
         while(keepon) {
            // These are sample SQL statements
            System.out.println("MAIN MENU");
            System.out.println("---------");
            System.out.println("1. Create user");
            System.out.println("2. Log in");
            System.out.println("9. < EXIT");
            String authorisedUser = null;
            switch (readChoice()){
               case 1: CreateUser(esql); break;
               case 2: authorisedUser = LogIn(esql); break;
               case 9: keepon = false; break;
               default : System.out.println("Unrecognized choice!"); break;
            }//end switch
            if (authorisedUser != null) {
              boolean usermenu = true;
              while(usermenu) {
                System.out.println("MAIN MENU");
                System.out.println("---------");
                System.out.println("1. View Hotels within 30 units");
                System.out.println("2. View Rooms");
                System.out.println("3. Book a Room");
                System.out.println("4. View recent booking history");

                //the following functionalities basically used by managers
                System.out.println("5. Update Room Information");
                System.out.println("6. View 5 recent Room Updates Info");
                System.out.println("7. View booking history of the hotel");
                System.out.println("8. View 5 regular Customers");
                System.out.println("9. Place room repair Request to a company");
                System.out.println("10. View room repair Requests history");

                System.out.println(".........................");
                System.out.println("20. Log out");
                switch (readChoice()){
                   case 1: viewHotels(esql); break;
                   case 2: viewRooms(esql); break;
                   case 3: bookRooms(esql); break;
                   case 4: viewRecentBookingsfromCustomer(esql); break;
                   case 5: updateRoomInfo(esql); break;
                   case 6: viewRecentUpdates(esql); break;
                   case 7: viewBookingHistoryofHotel(esql); break;
                   case 8: viewRegularCustomers(esql); break;
                   case 9: placeRoomRepairRequests(esql); break;
                   case 10: viewRoomRepairHistory(esql); break;
                   case 20: usermenu = false; break;
                   default : System.out.println("Unrecognized choice!"); break;
                }
              }
            }
         }//end while
      }catch(Exception e) {
         System.err.println (e.getMessage ());
      }finally{
         // make sure to cleanup the created table and close the connection.
         try{
            if(esql != null) {
               System.out.print("Disconnecting from database...");
               esql.cleanup ();
               System.out.println("Done\n\nBye !");
            }//end if
         }catch (Exception e) {
            // ignored.
         }//end try
      }//end try
   }//end main

   public static void Greeting(){
      System.out.println(
         "\n\n*******************************************************\n" +
         "              User Interface      	               \n" +
         "*******************************************************\n");
   }//end Greeting

   /*
    * Reads the users choice given from the keyboard
    * @int
    **/
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

   /*
    * Creates a new user
    **/
   public static void CreateUser(Hotel esql){
      try{
         System.out.print("\tEnter name: ");
         String name = in.readLine();
         System.out.print("\tEnter password: ");
         String password = in.readLine(); 
         String type="Customer";
			String query = String.format("INSERT INTO USERS (name, password, userType) VALUES ('%s','%s', '%s')", name, password, type);
         esql.executeUpdate(query);
         System.out.println ("User successfully created with userID = " + esql.getNewUserID("SELECT last_value FROM users_userID_seq"));
         
      }catch(Exception e){
         System.err.println (e.getMessage ());
      }
   }//end CreateUser


   /*
    * Check log in credentials for an existing user
    * @return User login or null is the user does not exist
    **/
   public static String LogIn(Hotel esql){
      try{
         System.out.print("\tEnter userID: ");
         String userID = in.readLine();
         System.out.print("\tEnter password: ");
         String password = in.readLine();

         String query = String.format("SELECT * FROM USERS WHERE userID = '%s' AND password = '%s'", userID, password);
         int userNum = esql.executeQuery(query);
         if (userNum > 0)
            return userID;
         return null;
      }catch(Exception e){
         System.err.println (e.getMessage ());
         return null;
      }
   }//end

// Rest of the functions definition go in here

public static void viewHotels(Hotel esql) { //(Mihir Jain)
   try {
      System.out.println("Please enter your current latitude: ");
      double userLatitude = Double.parseDouble(in.readLine());

      System.out.println("Please enter your current longitude: ");
      double userLongitude = Double.parseDouble(in.readLine());

      //System.out.println(userLatitude + " " + userLongitude);

      //double decimaltoInt = Hotel.latitude.doubleValue();

      String query = String.format("SELECT hotelName, latitude, longitude FROM Hotel WHERE calculate_distance(%s, %s, Hotel.latitude, Hotel.longitude) < 30.0", userLatitude, userLongitude);

      List<List<String>>HotelQueryList = esql.executeQueryAndReturnResult(query);
      for (int i = 0; i < HotelQueryList.size() - 1; ++i) {
         //double euclideanDistance = calculate_distance(userLatitude, userLongitude, HotelQueryList.get(i).get(1).parseDouble(), HotelQueryList.get(i).get(2).parseDouble());
         System.out.println(HotelQueryList.get(i) + "\n");
      }
      System.out.println("Hotels within 30 units of current location: " + HotelQueryList.size());
      esql.executeQueryAndPrintResult(query);
   } catch (Exception e) {
      System.err.println(e.getMessage());
   }
}

public static void viewRooms(Hotel esql) { //(Mihir Jain)
   try {
      System.out.println("Please enter the id for the hotel you want to view the rooms for: ");
      String hotelID = in.readLine();

      String query = String.format("SELECT H.hotelName, R.roomNumber, R.price, R.imageURL from Hotel H join Rooms R on R.hotelID = H.hotelID WHERE '%s' = R.hotelID", hotelID);

      List<List<String>>hotelReturnList = esql.executeQueryAndReturnResult(query);

      for (int i = 0; i < hotelReturnList.size() - 1; ++i) {
            System.out.println(hotelReturnList.get(i) + "\n");
      }

      System.out.println("Numbers of rooms in the hotel: "+ hotelReturnList.size());
      esql.executeQueryAndPrintResult(query);
   } catch (Exception e) {
      System.err.println(e.getMessage());
   }
}

public static void bookRooms(Hotel esql) { //Mihir Jain)
   try {
      System.out.print("Please enter userID: ");
      String userID = in.readLine();

      System.out.println("Please enter the id for the hotel you want to view the rooms for: ");
      String hotelID = in.readLine();

      System.out.println("Please enter the room number: ");
      String roomNumber = in.readLine();

      String query = String.format("SELECT roomNumber FROM RoomBookings RB WHERE '%s' = RB.roomNumber", roomNumber);
      List<List<String>> roomNumberCheck = esql.executeQueryAndReturnResult(query);

      while (roomNumberCheck.size() == 0) { //error check for room number
         System.out.println("\tInvalid room number! Enter a new room number: ");
         roomNumber = in.readLine();
         query = String.format("SELECT roomNumber FROM RoomBookings RB WHERE '%s' = RB.roomNumber", roomNumber);
      }

      System.out.println("Please enter the current date in YYYY-MM-DD format: ");
      String bookingDate = in.readLine();

      String query1 = String.format("SELECT bookingDate FROM RoomBookings RB WHERE '%s' = RB.bookingDate AND '%s' = RB.roomNumber", bookingDate, roomNumber);
      List<List<String>> dateCheck = esql.executeQueryAndReturnResult(query1);

      while (dateCheck.size() >= 1) { //error check for bookingDate
         System.out.println("Invalid date! Enter the current date in YYYY-MM-DD format: ");
         bookingDate = in.readLine();
         query1 = String.format("SELECT bookingDate FROM RoomBookings RB WHERE '%s' = RB.bookingDate AND '%s' = RB.roomNumber", bookingDate, roomNumber);
         dateCheck = esql.executeQueryAndReturnResult(query1);
      }

      //find unavailable rooms
      String bookedRooms = String.format("SELECT R.hotelID, R.roomNumber FROM Rooms R, RoomBookings RB WHERE R.hotelID = '%s' AND R.hotelID = RB.hotelID AND RB.roomNumber = '%s'", hotelID, roomNumber);
      List<List<String>> listBookedRooms = esql.executeQueryAndReturnResult(bookedRooms);
      esql.executeQueryAndPrintResult(bookedRooms);

      //max booking id by finding query of max booking id
      String findNewBookingID = String.format("SELECT MAX(RoomBookings.bookingID) FROM RoomBookings");
      List<List<String>> newBookingID = esql.executeQueryAndReturnResult(findNewBookingID);
      int newID = Integer.parseInt(newBookingID.get(0).get(0)) + 1;

      //if statement if room doesn't exist after query  
      if (listBookedRooms.size() == 0) {
         String query2 = String.format("INSERT INTO RoomBookings (bookingID, customerID, roomNumber, bookingDate) VALUES ('%s', '%s', '%s', '%s')", newID, userID, roomNumber, bookingDate);
         //String query3 = String.format("UPDATE RoomBookings SET bookingID = '%s' WHERE bookingDate = '%s' AND roomNumber = '%s' AND customerID = '%s'", newID, bookingDate, roomNumber, userID);
         esql.executeUpdate(query2);
         //esql.executeUpdate(query3);
         System.out.println("Successfully booked room!");
      }
      else {
         System.out.println("Sorry, the room selected isn't available on the date desired");
      }
   } catch (Exception e) {
      System.err.println(e.getMessage());
   }
}

public static void viewRecentBookingsfromCustomer(Hotel esql) { //(Mihir Jain)
   try {
      System.out.println("Please enter Customer userID: "); //input for customer id
      String userID = in.readLine();

      String query = String.format(
            "SELECT RB.hotelID, RB.roomNumber, R.price, RB.bookingDate FROM RoomBookings RB, Users U, Rooms R WHERE RB.hotelID = R.hotelID AND RB.customerID = U.userID AND RB.bookingID IN (SELECT RB.bookingID FROM RoomBookings RB, Hotel H, Users U WHERE RB.customerID = '%s') ORDER BY RB.bookingDate DESC LIMIT 5", userID);
      
      /*for (int i = 0; i < orderHistory.size() - 1; ++i) {
         for (int j = 0; j < 3; ++j) {
            System.out.println(orderHistory.get(i).get(j));
         }
      }*/

     esql.executeQueryAndPrintResult(query);
     

   } catch (Exception e) {
      System.err.println(e.getMessage());
   }
}

public static void updateRoomInfo(Hotel esql) { //(Mihir Jain)
   try {
      System.out.println("Please enter the Manager user id: ");
      String userID = in.readLine();

      System.out.println("Please enter the id for the hotel you want to view the rooms for: ");
      String hotelID = in.readLine();

      System.out.println("Please enter the room number: ");
      Integer roomNumber = Integer.parseInt(in.readLine());

      System.out.println("Please enter the new price of the room: ");
      Integer newPrice = Integer.parseInt(in.readLine());

      System.out.println("Please enter the new image url of the room: ");
      String imageURL = in.readLine();
      char[] imageURLchar = imageURL.toCharArray();

      String query1 = String.format( //pull rooms up
            "UPDATE Rooms SET imageURL = '%s', price = '%s' IN (SELECT R.hotelID, R.price, R.imageURL FROM Rooms R WHERE '%s' = (SELECT managerUserID FROM Hotel))",imageURLchar, newPrice, userID);
      String query2 = String.format( //update room price and
            "INSERT INTO Rooms SET imageURL = '%s', price = '%s'",imageURLchar, newPrice);

      esql.executeUpdate(query1);
      esql.executeUpdate(query2);

      System.out.println("\tRoom Info updated.");
   } catch (Exception e) {
      System.err.println(e.getMessage());
   }
}


   public static void viewRecentUpdates(Hotel esql) {//(Parth Desai)

 try{
   System.out.print("Please enter your Manager ID for Update History:");
   String valmanagerID = in.readLine();

   String query = String.format("SELECT * FROM RoomUpdatesLog WHERE managerID = " + valmanagerID + " ORDER BY updateNumber DESC LIMIT 5");
   System.out.print("\n");

   esql.executeQueryAndPrintResult(query);
 }

catch(Exception e){
   System.err.println (e.getMessage());
} 



   }

   

  
   public static void viewBookingHistoryofHotel(Hotel esql) {//(Parth Desai)
try{

   System.out.print("Please enter the HotelID of the hotels you manage:");
   String valhotelID = in.readLine();

   String query = "SELECT * FROM RoomBookings WHERE hotelID = " + valhotelID;
   System.out.print("\n");

   esql.executeQueryAndPrintResult(query);

}
catch(Exception e){
   System.err.println (e.getMessage());
} 
   }
   
   public static void viewRegularCustomers(Hotel esql) {//(Parth Desai)
      try{

   System.out.print("Please enter the HotelID of the hotels you manage:");
   String valhotelID = in.readLine();

   String query = String.format("SELECT customerID, COUNT(*) FROM RoomBookings WHERE hotelID = " + valhotelID + " GROUP BY customerID ORDER BY COUNT(*) DESC LIMIT 5");
   System.out.print("\n");

   esql.executeQueryAndPrintResult(query);
 }

catch(Exception e){
   System.err.println (e.getMessage());
} 


      }
   

   public static void placeRoomRepairRequests(Hotel esql) {//(Parth Desai)
      try{

   System.out.print("Please enter the Hotel ID of the hotel for the repair request:");
   String valhotelID = in.readLine();

   System.out.print("Please enter the Room Number for the repair request: ");
   String valroomNumber = in.readLine(); 

   System.out.print("Please enter the Maintenance Company ID: ");
   String valcompanyID = in.readLine();

   System.out.print("Please enter the Repair Date (YYYY-MM-DD):");
   String repairDate = in.readLine();
   

 
String query = String.format("INSERT INTO RoomRepairs (companyID, hotelID, roomNumber, repairDate) VALUES ('%s', '%s', '%s', '%s')", valcompanyID, valhotelID, valroomNumber, repairDate);
         esql.executeUpdate(query);
         System.out.print("Room Repair Request Successfuly Sent! \t");
         System.out.print("\n");
      }


catch(Exception e){
   System.err.println (e.getMessage());
} 

}

   
   public static void viewRoomRepairHistory(Hotel esql) {//(Parth Desai)
 try{
   System.out.print("Please enter the Hotel ID of the hotel for the repair request:");
   String valhotelID = in.readLine();

      String query = "SELECT * FROM RoomRepairs WHERE hotelID = " + valhotelID;
   System.out.print("\n");

   esql.executeQueryAndPrintResult(query);
 }

catch(Exception e){
   System.err.println (e.getMessage());
} 



   }

}//end Hotel



