# Use Cases

## UC-01: Register / Login
Primary Actor: User
Secondary Actor: System

Entry Conditions: User has access to the application

Description: Allows a user to register for an account or log into the system in order to access SOS features securely.

Exit Conditions: User is authenticated
User session is active

## UC-02: Manage Trusted Contacts
Primary Actor: User
Secondary Actor: System

Entry Conditions: User is logged into the system

Description: Allows the user to add, edit, or remove trusted contacts who will receive emergency alerts.

Exit Conditions: Trusted contact list is updated and saved

## UC-03: Trigger SOS Alert
Primary Actor: User
Secondary Actor: System

Entry Conditions: User is logged in
At least one trusted contact is configured

Description: Allows the user to trigger an SOS alert during a dangerous situation so that trusted contacts are notified immediately.

Exit Conditions: SOS alert is activated
Emergency notifications are sent to trusted contacts
Location sharing and escalation timer are started

## UC-04: Cancel SOS Alert
Primary Actor: User
Secondary Actor: System

Entry Conditions: An SOS alert is currently active

Description: Allows the user to cancel an active SOS alert if the emergency situation is resolved or if the alert was triggered accidentally.

Exit Conditions: SOS alert status is changed to cancelled
Location tracking is stopped
Trusted contacts are notified of cancellation

## UC-05: View SOS Status
Primary Actor: User
Secondary Actor: System

Entry Conditions: User has triggered an SOS alert

Description: Allows the user to view the current status of an SOS alert (active, escalated, resolved, or cancelled).

Exit Conditions: Current SOS status is displayed to the user

## UC-06: Receive Emergency Alert
Primary Actor: Trusted Contact
Secondary Actor: System

Entry Conditions: User has triggered an SOS alert
Trusted contact is registered in the system

Description: Notifies trusted contacts when an SOS alert is triggered by the user.

Exit Conditions: Emergency alert notification is delivered to the trusted contact

## UC-07: View User Location
Primary Actor: Trusted Contact
Secondary Actor: System

Entry Conditions: SOS alert is active
Trusted contact has received the alert

Description: Allows the trusted contact to view the user’s real-time location during an active SOS alert.

Exit Conditions: User’s current location is displayed while the alert remains active

## UC-08: Acknowledge Alert
Primary Actor: Trusted Contact
Secondary Actor: System

Entry Conditions: Trusted contact has received an emergency alert

Description: Allows the trusted contact to acknowledge that they are aware of the emergency situation.

Exit Conditions: Alert acknowledgement is recorded in the system

## UC-09: Request SOS Alert Resolution
Primary Actor: Trusted Contact
Secondary Actor: System

Entry Conditions: SOS alert is active
Trusted contact has reached the user or believes the situation is resolved

Description: Allows a trusted contact to request the resolution of an active SOS alert. The system records the request and waits for user confirmation before finalizing the alert resolution.

Exit Conditions: Resolution request is recorded
User is prompted to confirm safety
SOS alert remains active until user confirmation or system-defined fallback occurs

## UC-10: Escalate SOS Alert
Primary Actor: System
Secondary Actor: —

Entry Conditions: SOS alert is active
User does not respond within the defined time period

Description: Automatically escalates the SOS alert when the user becomes unresponsive, ensuring continued notification and urgency.

Exit Conditions: Alert escalation is triggered
Trusted contacts receive escalation notification

# UC-11: Confirm SOS Resolution
Primary Actor: User
Secondary Actor: System

Entry Conditions: An SOS alert is currently active
A resolution request has been initiated (e.g., by a trusted contact or system prompt)
The user is authenticated and able to interact with the system
(This ensures the user is conscious and intentionally confirming safety.)

Description: Allows the user to confirm that they are safe after a resolution request, finalizing the termination of the SOS alert.

Exit Conditions: SOS alert status is updated to resolved
Real-time location tracking is stopped
Escalation timers are terminated
Trusted contacts are notified that the alert has been resolved
