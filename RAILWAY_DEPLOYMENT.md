# Railway Deployment Guide

## Quick Setup

### Option 1: Copy-Paste Raw Variables (Recommended)

1. Go to your Railway project
2. Click on your service
3. Go to **Variables** tab
4. Click **RAW Editor**
5. Copy and paste the content from `railway.env`:

```env
DB_URL=jdbc:postgresql://db.qnnkizacregmdsfgqrsw.supabase.co:5432/postgres?sslmode=require
DB_USERNAME=postgres
DB_PASSWORD=AL+4kWTv%A+k9DK
EMAIL_HOST=smtp.gmail.com
EMAIL_PORT=587
EMAIL_USERNAME=kasumalesh@gmail.com
EMAIL_PASSWORD=dwqx mclv bkkf soxt
JWT_SECRET=elocate-super-secret-key-change-in-production-minimum-256-bits-required
JWT_EXPIRATION_MS=86400000
OTP_EXPIRY_MINUTES=10
OTP_MAX_ATTEMPTS=3
AUTH0_DOMAIN=elocate-dev.us.auth0.com
AUTH0_CLIENT_ID=gSMm494WIxE59MtZxODuYvdCRy4yFT10
AUTH0_CLIENT_SECRET=NalHurNjv35rQu_zgmR_vkOHB2-OmuOofKBY2lMRUz4Lu3d6F2RQVM9T8REVqHtZ
AUTH0_CONNECTION=Username-Password-Authentication
SERVER_PORT=8080
SPRING_PROFILES_ACTIVE=production
JAVA_TOOL_OPTIONS=-Xmx512m -Xms256m
```

6. Click **Save**
7. Railway will automatically redeploy

### Option 2: Import JSON

1. Go to your Railway project
2. Click on your service
3. Go to **Variables** tab
4. Use the JSON from `railway.json` if your platform supports JSON import

### Option 3: Add Variables Manually

Add each variable individually in Railway's Variables tab:

| Variable Name | Value |
|---------------|-------|
| DB_URL | `jdbc:postgresql://db.qnnkizacregmdsfgqrsw.supabase.co:5432/postgres?sslmode=require` |
| DB_USERNAME | `postgres` |
| DB_PASSWORD | `AL+4kWTv%A+k9DK` |
| EMAIL_HOST | `smtp.gmail.com` |
| EMAIL_PORT | `587` |
| EMAIL_USERNAME | `kasumalesh@gmail.com` |
| EMAIL_PASSWORD | `dwqx mclv bkkf soxt` |
| JWT_SECRET | `elocate-super-secret-key-change-in-production-minimum-256-bits-required` |
| JWT_EXPIRATION_MS | `86400000` |
| OTP_EXPIRY_MINUTES | `10` |
| OTP_MAX_ATTEMPTS | `3` |
| AUTH0_DOMAIN | `elocate-dev.us.auth0.com` |
| AUTH0_CLIENT_ID | `gSMm494WIxE59MtZxODuYvdCRy4yFT10` |
| AUTH0_CLIENT_SECRET | `NalHurNjv35rQu_zgmR_vkOHB2-OmuOofKBY2lMRUz4Lu3d6F2RQVM9T8REVqHtZ` |
| AUTH0_CONNECTION | `Username-Password-Authentication` |
| SERVER_PORT | `8080` |
| SPRING_PROFILES_ACTIVE | `production` |
| JAVA_TOOL_OPTIONS | `-Xmx512m -Xms256m` |

## Railway Configuration

### Build Settings

**Build Command:**
```bash
mvn clean package -DskipTests
```

**Start Command:**
```bash
java $JAVA_TOOL_OPTIONS -jar target/elocate-0.0.1-SNAPSHOT.jar
```

### Root Directory
```
elocate-server
```

### Port
Railway will automatically detect port 8080 from `SERVER_PORT` variable.

## Deployment Steps

1. **Connect Repository**
   - Link your GitHub repository to Railway
   - Select the `elocate-server` directory as root

2. **Add Environment Variables**
   - Use RAW Editor and paste content from `railway.env`
   - Or manually add each variable

3. **Deploy**
   - Railway will automatically build and deploy
   - Monitor logs for any errors

4. **Verify Deployment**
   - Check deployment logs
   - Test health endpoint: `https://your-app.railway.app/actuator/health`
   - Test API: `https://your-app.railway.app/v3/api-docs`

## Environment Variables Explained

### Database
- **DB_URL**: Supabase PostgreSQL connection string with SSL
- **DB_USERNAME**: Database username
- **DB_PASSWORD**: Database password

### Email (Gmail SMTP)
- **EMAIL_HOST**: Gmail SMTP server
- **EMAIL_PORT**: SMTP port (587 for TLS)
- **EMAIL_USERNAME**: Gmail address
- **EMAIL_PASSWORD**: Gmail App Password (not regular password)

### Security
- **JWT_SECRET**: Secret key for JWT token signing (256+ bits)
- **JWT_EXPIRATION_MS**: Token expiration (24 hours = 86400000ms)

### OTP
- **OTP_EXPIRY_MINUTES**: OTP validity period
- **OTP_MAX_ATTEMPTS**: Maximum OTP verification attempts

### Auth0
- **AUTH0_DOMAIN**: Auth0 tenant domain
- **AUTH0_CLIENT_ID**: Auth0 application client ID
- **AUTH0_CLIENT_SECRET**: Auth0 application client secret
- **AUTH0_CONNECTION**: Auth0 database connection name

### Server
- **SERVER_PORT**: Application port (8080)
- **SPRING_PROFILES_ACTIVE**: Spring profile (production)
- **JAVA_TOOL_OPTIONS**: JVM memory settings

## Troubleshooting

### Build Fails
- Check if Maven is properly configured
- Verify Java version (17 or higher)
- Check build logs for errors

### Application Won't Start
- Verify all environment variables are set
- Check database connection
- Review application logs

### Database Connection Issues
- Verify Supabase database is accessible
- Check SSL mode is set correctly
- Verify credentials are correct

### Email Not Sending
- Verify Gmail App Password (not regular password)
- Check if 2FA is enabled on Gmail account
- Verify SMTP settings

## Health Check

Railway will automatically monitor:
```
https://your-app.railway.app/actuator/health
```

Expected response:
```json
{
  "status": "UP"
}
```

## API Documentation

Once deployed, access Swagger UI:
```
https://your-app.railway.app/swagger-ui.html
```

## Logs

View logs in Railway dashboard:
1. Go to your service
2. Click **Deployments**
3. Click on latest deployment
4. View **Logs** tab

## Scaling

Railway automatically scales based on:
- Memory: 512MB (configured via JAVA_TOOL_OPTIONS)
- CPU: Auto-scaled
- Instances: 1 (can be increased)

## Custom Domain

1. Go to **Settings** in Railway
2. Click **Domains**
3. Add your custom domain
4. Update DNS records as instructed

## Monitoring

Railway provides:
- CPU usage
- Memory usage
- Network traffic
- Request metrics
- Error logs

## Backup Strategy

1. **Database**: Supabase handles automatic backups
2. **Code**: Version controlled in Git
3. **Environment Variables**: Documented in this guide

## Security Best Practices

✅ Use environment variables (not hardcoded)
✅ Enable SSL for database connections
✅ Use strong JWT secrets
✅ Use Gmail App Passwords
✅ Rotate secrets regularly
✅ Monitor logs for suspicious activity

## Support

For issues:
1. Check Railway logs
2. Verify environment variables
3. Test database connection
4. Review application logs
5. Contact Railway support

## Files Reference

- `railway.env` - Raw environment variables
- `railway.json` - JSON format variables
- `.env` - Local development variables
- `application.yaml` - Spring Boot configuration
