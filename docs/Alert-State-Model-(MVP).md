**1.Alert States**

INACTIVE
ACTIVE
ESCALATED
RESOLVED
CANCELLED

**2.State Definitions**

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


**State Transitions**
INACTIVE → ACTIVE (User triggers SOS)

ACTIVE → CANCELLED (User cancels)

ACTIVE → RESOLVED (User resolves before escalation)

ACTIVE → ESCALATED (Timer expires)

ESCALATED → RESOLVED (User confirms resolution)


**Behavioral Rules**
Location sharing starts in ACTIVE.

Location sharing continues in ESCALATED.

Location sharing stops in CANCELLED or RESOLVED.

Only user can directly resolve while ACTIVE.

Resolution after ESCALATION may involve trusted contact request + user confirmation.
