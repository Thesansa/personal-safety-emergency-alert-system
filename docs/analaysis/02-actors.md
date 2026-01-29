# Actors

## Primary Actor
- User (Person in Danger)

## Secondary Actor
- Trusted Contact

## System Actor

As the System, it should be able to:
- Detect and process SOS activation requests from users
- Validate the user’s identity and alert request
- Capture and store the user’s real-time location during an active SOS alert
- Notify all configured trusted contacts when an SOS alert is triggered
- Initiate and manage a countdown timer for user responsiveness
- Automatically escalate the alert if the user does not respond within the defined time period
- Control access to the user’s location data, ensuring it is available only during an active alert
- Stop location tracking when an SOS alert is cancelled or resolved
- Log all emergency events securely for auditing and review purposes
- Update and maintain the current status of each SOS alert

