# Required Environment Variables

SPRING_DATASOURCE_URL
Description:
Azure PostgreSQL JDBC URL.

SPRING_DATASOURCE_USERNAME
Description:
Database administrator username.

SPRING_DATASOURCE_PASSWORD
Description:
Azure PostgreSQL password.

JWT_SECRET
Description:
256-bit secret used for signing JWTs.

JWT_REFRESH_EXPIRATION_MS
Optional.

MAIL_USERNAME
Description:
Gmail address used to send trusted-contact notification emails via SMTP.

MAIL_PASSWORD
Description:
Gmail App Password (not the account password) for the above address. Requires 2-Step
Verification enabled on the Google account. Generate at
https://myaccount.google.com/apppasswords.

TZ
Description:
Server timezone (e.g. Asia/Colombo). Without this, LocalDateTime.now() returns the container's
default OS timezone (UTC on Azure/Linux containers, which differs from the local development
machine's timezone) — set explicitly so alert timestamps and notification emails are consistent
across environments.
