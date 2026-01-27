# Render Deployment Guide - Elocate API (Free Tier)

## Prerequisites
- GitHub account
- Render account (sign up at https://render.com)
- Your code pushed to a GitHub repository

## Step 1: Prepare Your Repository

1. **Commit all the new files created:**
   ```bash
   git add .
   git commit -m "Add Render deployment configuration"
   git push origin main
   ```

## Step 2: Create a Render Account

1. Go to https://render.com
2. Click "Get Started for Free"
3. Sign up with your GitHub account (recommended)
4. Authorize Render to access your repositories

## Step 3: Create a New Web Service

1. **From Render Dashboard:**
   - Click "New +" button (top right)
   - Select "Web Service"

2. **Connect Your Repository:**
   - Select "Build and deploy from a Git repository"
   - Click "Connect" next to your GitHub account
   - Find and select your `elocate` repository
   - Click "Connect"

3. **Configure Your Service:**

   **Basic Settings:**
   - Name: `elocate-api` (or your preferred name)
   - Region: Choose closest to you (e.g., Oregon, Ohio, Frankfurt)
   - Branch: `main` (or your default branch)
   - Root Directory: Leave blank
   - Runtime: `Java`

   **Build Settings:**
   - Build Command: `chmod +x mvnw && ./mvnw clean package -DskipTests`
   - Start Command: `java -Dspring.profiles.active=production -Xmx512m -Xms256m -jar target/elocate-0.0.1-SNAPSHOT.jar`

   **Instance Type:**
   - Select **"Free"** (512 MB RAM, shared CPU)

## Step 4: Configure Environment Variables

In the "Environment Variables" section, add the following:

### Required Variables (Click "Add Environment Variable" for each):

```
SPRING_PROFILES_ACTIVE=production
SERVER_PORT=8080
CONTEXT_PATH=/elocate

# Database (Your existing Neon DB)
DB_URL=jdbc:postgresql://ep-gentle-salad-admc88pi-pooler.c-2.us-east-1.aws.neon.tech/elocate?sslmode=require
DB_USERNAME=neondb_owner
DB_PASSWORD=npg_veoMfz2N9xds

# Email Configuration
EMAIL_HOST=smtp.gmail.com
EMAIL_PORT=587
EMAIL_USERNAME=kasumalesh@gmail.com
EMAIL_PASSWORD=dwqx mclv bkkf soxt

# JWT Configuration
JWT_SECRET=elocate-super-secret-key-change-in-production-minimum-256-bits-required
JWT_EXPIRATION_MS=86400000

# Auth0 Configuration
AUTH0_DOMAIN=elocate-dev.us.auth0.com
AUTH0_CLIENT_ID=gSMm494WIxE59MtZxODuYvdCRy4yFT10
AUTH0_CLIENT_SECRET=NalHurNjv35rQu_zgmR_vkOHB2-OmuOofKBY2lMRUz4Lu3d6F2RQVM9T8REVqHtZ
AUTH0_CONNECTION=Username-Password-Authentication

# OTP Configuration
OTP_EXPIRY_MINUTES=10
OTP_MAX_ATTEMPTS=3

# Java Options
JAVA_TOOL_OPTIONS=-Xmx512m -Xms256m
```

**Important:** Mark sensitive variables as "Secret" by clicking the lock icon.

## Step 5: Deploy

1. Click "Create Web Service" at the bottom
2. Render will start building your application
3. Wait for the build to complete (5-10 minutes for first deployment)
4. Once deployed, you'll get a URL like: `https://elocate-api.onrender.com`

## Step 6: Test Your Deployment

Your API will be available at:
```
https://your-app-name.onrender.com/elocate/api/v1/auth/login
```

Test with curl:
```bash
curl https://your-app-name.onrender.com/elocate/actuator/health
```

## Step 7: Update CORS Configuration

Update `SecurityConfig.java` to allow your Render domain:

```java
configuration.setAllowedOrigins(List.of(
    "http://localhost:3000", 
    "http://localhost:3001",
    "https://your-app-name.onrender.com"
));
```

## Important Notes for Free Tier

### Limitations:
- **512 MB RAM** - Keep your app lightweight
- **Spins down after 15 minutes of inactivity** - First request after inactivity takes 30-60 seconds
- **750 hours/month** - Enough for continuous running
- **100 GB bandwidth/month**

### Optimization Tips:

1. **Reduce Memory Usage:**
   - Already configured with `-Xmx512m -Xms256m`
   - Database connection pool set to 3 max connections

2. **Keep Service Awake (Optional):**
   - Use a service like UptimeRobot (free) to ping your API every 5 minutes
   - Prevents cold starts but uses more free hours

3. **Monitor Logs:**
   - Go to your service dashboard
   - Click "Logs" tab to see real-time logs
   - Check for errors or memory issues

## Troubleshooting

### Build Fails:
- Check Java version in `system.properties` matches your pom.xml
- Ensure `mvnw` has execute permissions
- Check build logs for specific errors

### Application Crashes:
- Check logs for OutOfMemoryError
- Reduce Hikari connection pool size
- Disable SQL logging in production

### Database Connection Issues:
- Verify DB_URL includes `?sslmode=require`
- Check Neon DB is accessible from Render's IP ranges
- Test connection string locally first

### Slow First Request:
- This is normal for free tier (cold start)
- Consider upgrading to paid tier ($7/month) for always-on service

## Updating Your Application

Render auto-deploys when you push to your connected branch:

```bash
git add .
git commit -m "Update feature"
git push origin main
```

Render will automatically rebuild and redeploy.

## Custom Domain (Optional)

1. Go to your service settings
2. Click "Custom Domain"
3. Add your domain
4. Update DNS records as instructed

## Monitoring

Free tier includes:
- Basic metrics (CPU, Memory, Bandwidth)
- Log retention (7 days)
- Deploy history

Access from your service dashboard.

## Cost Considerations

**Free Tier:**
- $0/month
- Perfect for development/testing
- Spins down after inactivity

**Starter Tier ($7/month):**
- Always on (no cold starts)
- 512 MB RAM
- Better for production

## Support

- Render Docs: https://render.com/docs
- Community Forum: https://community.render.com
- Status Page: https://status.render.com

## Security Recommendations

1. **Change JWT Secret:**
   ```bash
   # Generate a strong secret
   openssl rand -base64 64
   ```

2. **Use Environment Variables:**
   - Never commit secrets to Git
   - All sensitive data in Render environment variables

3. **Enable HTTPS:**
   - Render provides free SSL certificates
   - Automatically enabled

4. **Update Auth0 Callback URLs:**
   - Add your Render URL to Auth0 allowed callbacks
   - Update CORS origins

## Next Steps

1. Set up monitoring with UptimeRobot
2. Configure custom domain
3. Set up CI/CD with GitHub Actions (optional)
4. Monitor logs and performance
5. Consider upgrading if you need always-on service

---

**Your Render URL will be:** `https://elocate-api.onrender.com/elocate`

**Health Check:** `https://elocate-api.onrender.com/elocate/actuator/health`
