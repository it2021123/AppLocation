Location Tracking App
Overview
The Location Tracking App is a GPS-based application designed for real-time location tracking and management. It integrates with a local SQLite database to store and manage location data, monitors GPS status, and updates location information periodically.

Features
Real-Time Location Tracking: Continuously monitors and updates the user's location.
Database Integration: Stores location data and session information in a local SQLite database.
GPS Status Monitoring: Checks and manages GPS status, activating or deactivating location services as needed.
Periodic Location Updates: Requests and processes location updates at regular intervals.
User Interface: Provides a map view to display the current location and marked regions.
Installation
Clone the Repository:

git clone <repository_url>
Open the Project: Import the project into Android Studio or your preferred IDE.

Configure Dependencies: Ensure all required dependencies are included in your project’s build.gradle file.

Run the Application: Build and run the application on an Android device or emulator.

Usage
Start Location Service: Begin tracking location updates by starting the location service.
View Location on Map: The map view displays the current location and updates it based on GPS data.
Manage Locations: Add, delete, or query location data through the app's interface.
Permissions
The app requires the following permissions:

ACCESS_FINE_LOCATION: To access the device's GPS location.
INTERNET: For downloading map tiles and accessing location services.
Contact
For questions or support, please contact giopou2003@gmail.com.

