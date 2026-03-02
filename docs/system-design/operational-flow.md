**This section describes the operational behavior of the MVP from user, trusted contact, and system perspectives to clarify interaction flow before UI and backend implementation.**

# Alert State Model (MVP)

## 1.Alert States

INACTIVE | ACTIVE | ESCALATED | RESOLVED | CANCELLED

## 2.State Definitions

*INACTIVE*

No alert currently active.

*ACTIVE*

Alert triggered. Location sharing active. Escalation timer running.

*ESCALATED*

User did not respond within 45 seconds. Alert urgency increased. Location sharing continues.

*RESOLVED*

Emergency confirmed and safely handled.

*CANCELLED*

Alert was triggered unintentionally or no real danger occurred.


## State Transitions

INACTIVE → ACTIVE (User triggers SOS)

ACTIVE → CANCELLED (User cancels)

ACTIVE → RESOLVED (User resolves before escalation)

ACTIVE → ESCALATED (Timer expires)

ESCALATED → RESOLVED (User confirms resolution)


## Behavioral Rules

Location sharing starts in ACTIVE.

Location sharing continues in ESCALATED.

Location sharing stops in CANCELLED or RESOLVED.

Only user can directly resolve while ACTIVE.

Resolution after ESCALATION may involve trusted contact request + user confirmation.

# Operational Flow (MVP)

## User Perspective

### False Alarm Scenario

User logs in.

User presses SOS button.

Alert status becomes ACTIVE.

Location sharing begins.

45-second countdown starts.

User realizes it was a mistake.

User cancels alert.

Alert status becomes CANCELLED.

### Real Emergency (User Responsive)

User feels unsafe.

User presses SOS.

Alert status becomes ACTIVE.

Trusted contacts are notified.

Location sharing begins.

User reaches safety.

User confirms safety and resolves alert.

Alert status becomes RESOLVED.

### Escalation Scenario (User Unresponsive)

User presses SOS.

Alert status becomes ACTIVE.

Countdown begins.

User does not respond.

Alert status becomes ESCALATED.

Trusted contacts receive escalation notification.

Location sharing continues.

Later, user confirms safety.

Alert status becomes RESOLVED.

## Trusted Contact Perspective

### Receiving Alert

Trusted contact receives alert notification.

Opens alert page.

Checks alert status (ACTIVE or ESCALATED).

Views user’s real-time location.

Attempts to contact user.

### After Escalation

Trusted contact receives escalation notification.

Treats situation as urgent.

Goes to user’s location if possible.

May request resolution.

Waits for user confirmation.


## System Perspective

### When SOS is triggered:

Create alert record.

Set alert status to ACTIVE.

Store timestamp.

Start escalation timer (45 seconds).

Enable location tracking.

Notify trusted contacts.

### When timer expires:

If alert status is still ACTIVE → update to ESCALATED.

Notify trusted contacts again.

### When cancelled:

Update status to CANCELLED.

Stop location tracking.

Log lifecycle.

### When resolved:

Update status to RESOLVED.

Stop location tracking.

Log lifecycle.
