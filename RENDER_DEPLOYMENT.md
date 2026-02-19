# Render Deployment Guide

## Why Render Works Better with Supabase

✅ Render has better network connectivity to external databases
✅ Supports Supabase out of the box
✅ No special configuration needed for external PostgreSQL
✅ More reliable for production workloads

## Quick Deploy

### Option 1: Using render.yaml (Recommended)

1. **Push `render.yaml` to your repository**
   - File is already created in `elocate-server/render.yaml`
   - Commit and push to GitHub

2. **Connect to Render**
   - Go to [Render Dashboard](https://dashboard.render.com/)
   - Click **"New +"** → **"Blueprint"**
   - Connect your GitHub repository
   - Render will automatically detect `render.yaml`
   - Click **"Apply"**

3. **Deploy**
   - Render will automatically build and deploy
   - All environment variables are configured in `render.yaml`

### Option 2: Manual Setup

1. **Create New Web Service**
   - Go to [Render Dashboard](https://dashboard.render.com/)
   - Click **"New +"** → **"Web Service"**
   - Connect your GitHub repository
   - Select `elocate-server` as root directory

2. **Configure Build Settings**
   
   **Name:** `elocate-backend`
   
   **Environment:** `Java`
   
   **Build Command:**
   ```bash
   mvn clean package -DskipTests
   ```
   
   **Start Command:**
   ```bash
   java $JAVA_TOOL_OPTIONS -jar target/elocate-0.0.1-SNAPSHOT.jar
   ```

3. **Add Environment Variables**
   
   Go to **Environment** tab and add:

   ```
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

4. **Deploy**
   - Click **"Create Web Service"**
   - Render will build and deploy automatically

## Environment Variables (Copy-Paste Format)

### For Render Environment Tab:

```
DB_URL
jdbc:postgresql://db.qnnkizacregmdsfgqrsw.supabase.co:5432/postgres?sslmode=require

DB_USERNAME
postgres

DB_PASSWORD
AL+4kWTv%A+k9DK

EMAIL_HOST
smtp.gmail.com

EMAIL_PORT
587

EMAIL_USERNAME
kasumalesh@gmail.com

EMAIL_PASSWORD
dwqx mclv bkkf soxt

JWT_SECRET
elocate-super-secret-key-change-in-production-minimum-256-bits-required

JWT_EXPIRATION_MS
86400000

OTP_EXPIRY_MINUTES
10

OTP_MAX_ATTEMPTS
3

AUTH0_DOMAIN
elocate-dev.us.auth0.com

AUTH0_CLIENT_ID
gSMm494WIxE59MtZxODuYvdCRy4yFT10

AUTH0_CLIENT_SECRET
NalHurNjv35rQu_zgmR_vkOHB2-OmuOofKBY2lMRUz4Lu3d6F2RQVM9T8REVqHtZ

AUTH0_CONNECTION
Username-Password-Authentication

SERVER_PORT
8080

SPRING_PROFILES_ACTIVE
production

JAVA_TOOL_OPTIONS
-Xmx512m -Xms256m
```

## Render Configuration

### Instance Type
- **Free Tier:** 512MB RAM, shared CPU (good for testing)
- **Starter:** $7/month, 512MB RAM, 0.1 CPU
- **Standard:** $25/month, 2GB RAM, 0.5 CPU (recommended for production)

### Auto-Deploy
- Enable auto-deploy from main branch
- Render will automatically deploy on git push

### Health Check
Render will automatically monitor:
```
https://your-app.onrender.com/actuator/health
```

## Supabase Configuration

### Why Supabase Works on Render

✅ Render's network can reach Supabase servers
✅ SSL connections work out of the box
✅ No firewall restrictions
✅ Reliable connection pooling

### Supabase Connection Details

**Host:** `db.qnnkizacregmdsfgqrsw.supabase.co`
**Port:** `5432`
**Database:** `postgres`
**Username:** `postgres`
**Password:** `AL+4kWTv%A+k9DK`
**SSL Mode:** `require`

### Connection String Format
```
jdbc:postgresql://db.qnnkizacregmdsfgqrsw.supabase.co:5432/postgres?sslmode=require
```

## Deployment Steps

1. **Prepare Repository**
   ```bash
   cd elocate-server
   git add render.yaml
   git commit -m "Add Render configuration"
   git push
   ```

2. **Create Render Service**
   - Use Blueprint (render.yaml) or Manual setup
   - Connect GitHub repository
   - Select branch (main/master)

3. **Configure Environment**
   - Variables are in render.yaml
   - Or add manually in Render dashboard

4. **Deploy**
   - Render builds automatically
   - Monitor build logs
   - Wait for deployment to complete

5. **Verify**
   - Check health: `https://your-app.onrender.com/actuator/health`
   - Test API: `https://your-app.onrender.com/v3/api-docs`
   - Check logs for any errors

## Build Process

Render will:
1. Clone your repository
2. Install Java 17
3. Run Maven build: `mvn clean package -DskipTests`
4. Start application: `java -jar target/elocate-0.0.1-SNAPSHOT.jar`

## Monitoring

### Logs
- Real-time logs in Render dashboard
- Filter by severity (INFO, WARN, ERROR)
- Download logs for analysis

### Metrics
- CPU usage
- Memory usage
- Request count
- Response times
- Error rates

### Alerts
- Set up email/Slack alerts
- Monitor uptime
- Track deployment status

## Custom Domain

1. Go to **Settings** → **Custom Domain**
2. Add your domain (e.g., `api.elocate.com`)
3. Update DNS records:
   ```
   CNAME api your-app.onrender.com
   ```
4. Render provides free SSL certificate

## Scaling

### Vertical Scaling
- Upgrade instance type in Settings
- More RAM/CPU for better performance

### Horizontal Scaling
- Available on paid plans
- Multiple instances for high availability
- Load balancing included

## Troubleshooting

### Build Fails
- Check Java version (should be 17+)
- Verify Maven dependencies
- Review build logs

### Application Won't Start
- Check environment variables
- Verify database connection
- Review application logs

### Database Connection Issues
- Verify Supabase credentials
- Check SSL mode is set to `require`
- Test connection from Render shell

### Slow Performance
- Upgrade instance type
- Check database query performance
- Enable connection pooling

## Cost Estimate

### Free Tier
- 750 hours/month
- 512MB RAM
- Shared CPU
- Good for testing

### Production (Starter)
- $7/month
- 512MB RAM
- 0.1 CPU
- 99.9% uptime SLA

### Production (Standard)
- $25/month
- 2GB RAM
- 0.5 CPU
- Better performance

## Advantages of Render

✅ Easy deployment
✅ Automatic SSL certificates
✅ Built-in monitoring
✅ Auto-deploy from Git
✅ Better Supabase connectivity
✅ Free tier available
✅ Excellent documentation
✅ Great support

## Comparison: Render vs Railway

| Feature | Render | Railway |
|---------|--------|---------|
| Supabase Support | ✅ Excellent | ⚠️ May have issues |
| Free Tier | ✅ 750 hours | ✅ $5 credit |
| SSL | ✅ Automatic | ✅ Automatic |
| Custom Domain | ✅ Free | ✅ Free |
| Build Time | Fast | Fast |
| Logs | Excellent | Good |
| Pricing | $7/month | $5/month |

## Next Steps

1. Deploy to Render using render.yaml
2. Verify Supabase connection works
3. Test all API endpoints
4. Set up custom domain
5. Configure monitoring alerts
6. Plan for scaling

## Support

- [Render Documentation](https://render.com/docs)
- [Render Community](https://community.render.com/)
- [Render Status](https://status.render.com/)
- Email: support@render.com

## Files Reference

- `render.yaml` - Render Blueprint configuration
- `RENDER_DEPLOYMENT.md` - This guide
- `.env` - Local development variables
- `application.yaml` - Spring Boot configuration
