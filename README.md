#Location Tracking App
#Overview
The Location Tracking App is a GPS-based application designed to provide real-time location tracking and management. It features integration with a local database to store and manage location data, handle GPS status, and update location information periodically.

#Features
Real-Time Location Tracking: Continuously monitors and updates the user's location.
Database Integration: Stores location data and session information in a local SQLite database.
GPS Status Monitoring: Checks and manages the GPS status, including activating or deactivating location services based on the GPS status.
Periodic Location Updates: Requests and processes location updates at regular intervals.
User Interface: Provides a map view to display the current location and marked regions.
Installation
Clone the Repository:

    git clone <repository_url>
Open the Project: Import the project into Android Studio or your preferred IDE.

Configure Dependencies: Ensure that all required dependencies are included in your project’s build.gradle file.

Run the Application: Build and run the application on an Android device or emulator.

#Usage
Start Location Service: The app can start the location service to begin tracking location updates.

View Location on Map: The map view displays the current location and updates it based on GPS data.

Manage Locations: Add, delete, or query location data through the app's interface.

#Permissions
The app requires the following permissions:

ACCESS_FINE_LOCATION: For accessing the device's GPS location.
INTERNET: For downloading map tiles and accessing location services.


Contact
For questions or support, please contact giopou2003@gmail.com.
