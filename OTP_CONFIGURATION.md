# Application Configuration for OTP Authentication

## Add to application.yml or application.properties

```yaml
# Email Configuration
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: ${EMAIL_USERNAME}  # Set in environment variables
    password: ${EMAIL_APP_PASSWORD}  # Use Gmail App Password, not regular password
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
            required: true
        transport:
          protocol: smtp

# OTP Configuration
app:
  otp:
    expiry-minutes: 10  # OTP validity duration
    max-attempts: 3     # Max OTP generation attempts per hour (optional, for future rate limiting)
```

## Environment Variables

Set these environment variables:

- `EMAIL_USERNAME`: Your SMTP email address (e.g., yourapp@gmail.com)
- `EMAIL_APP_PASSWORD`: Gmail App Password (NOT your regular Gmail password)

### How to Generate Gmail App Password:

1. Go to Google Account settings
2. Security → 2-Step Verification (must be enabled)
3. App Passwords
4. Generate a new app password for "Mail"
5. Copy the 16-character password
6. Use this as `EMAIL_APP_PASSWORD`

## Maven Dependencies

Add to `pom.xml`:

```xml
<!-- Password Hashing -->
<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-crypto</artifactId>
    <version>6.2.0</version>
</dependency>

<!-- Email Sending -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-mail</artifactId>
</dependency>
```
