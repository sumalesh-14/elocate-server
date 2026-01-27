# Render Deployment with Docker - Complete Guide

## ✅ What Changed

Since Render doesn't have a native "Java" runtime, we're using **Docker** instead. This is actually better because:
- More control over the environment
- Consistent builds
- Optimized for 512MB RAM
- Multi-stage build (smaller image)

## 📁 New Files Created

- `Dockerfile` - Multi-stage Docker build configuration
- `.dockerignore` - Files to exclude from Docker build
- Updated `render.yaml` - Now uses Docker runtime

## 🚀 Deployment Steps (Updated for Docker)

### Step 1: Push to GitHub

```bash
git add .
git commit -m "Add Docker deployment for Render"
git push origin main
```

### Step 2: Create Render Account

1. Go to https://render.com
2. Click "Get Started for Free"
3. Sign up with GitHub
4. Authorize Render to access your repositories

### Step 3: Create New Web Service

1. From Render Dashboard, click "New +" → "Web Service"
2. Click "Build and deploy from a Git repository"
3. Connect your GitHub account
4. Select your `elocate` repository
5. Click "Connect"

### Step 4: Configure Service

**Basic Settings:**
```
Name:           elocate-api
Region:         Oregon (US West) or closest to you
Branch:         main
Root Directory: (leave blank)
```

**IMPORTANT - Runtime:**
```
Language/Runtime: Docker  ⭐ (Select Docker, NOT Java)
```

**Build & Deploy:**
```
Dockerfile Path: ./Dockerfile
Docker Context:  .
Docker Command:  (leave blank - uses Dockerfile ENTRYPOINT)
```

**Instance Type:**
```
Free (512 MB RAM, Shared CPU)
```

### Step 5: Environment Variables

Click "Add Environment Variable" for each:

```env
SPRING_PROFILES_ACTIVE=production
SERVER_PORT=8080
CONTEXT_PATH=/elocate

# Database
DB_URL=jdbc:postgresql://ep-gentle-salad-admc88pi-pooler.c-2.us-east-1.aws.neon.tech/elocate?sslmode=require
DB_USERNAME=neondb_owner
DB_PASSWORD=npg_veoMfz2N9xds

# Email
EMAIL_HOST=smtp.gmail.com
EMAIL_PORT=587
EMAIL_USERNAME=kasumalesh@gmail.com
EMAIL_PASSWORD=dwqx mclv bkkf soxt

# JWT
JWT_SECRET=elocate-super-secret-key-change-in-production-minimum-256-bits-required
JWT_EXPIRATION_MS=86400000

# Auth0
AUTH0_DOMAIN=elocate-dev.us.auth0.com
AUTH0_CLIENT_ID=gSMm494WIxE59MtZxODuYvdCRy4yFT10
AUTH0_CLIENT_SECRET=NalHurNjv35rQu_zgmR_vkOHB2-OmuOofKBY2lMRUz4Lu3d6F2RQVM9T8REVqHtZ
AUTH0_CONNECTION=Username-Password-Authentication

# OTP
OTP_EXPIRY_MINUTES=10
OTP_MAX_ATTEMPTS=3
```

**💡 Tip:** Mark sensitive variables (passwords, secrets) as "Secret" by clicking the lock icon.

### Step 6: Deploy!

1. Scroll down and click "Create Web Service"
2. Render will start building your Docker image
3. First build takes 5-10 minutes
4. Watch the logs for progress

### Step 7: Verify Deployment

Once deployed, your app will be at:
```
https://elocate-api.onrender.com/elocate
```

Test it:
```bash
# Health check
curl https://elocate-api.onrender.com/elocate/actuator/health

# Expected: {"status":"UP"}
```

## 🐳 Docker Build Process

The Dockerfile uses a **multi-stage build**:

**Stage 1 - Build:**
- Uses Maven with Java 17
- Downloads dependencies
- Compiles and packages the application
- Creates the JAR file

**Stage 2 - Runtime:**
- Uses lightweight Alpine Linux with Java 17 JRE
- Copies only the JAR file (not source code)
- Optimized for 512MB RAM
- Smaller final image (~200MB vs ~800MB)

## 📊 Memory Optimization

The Docker container is configured for Render's 512MB limit:

```dockerfile
ENV JAVA_OPTS="-Xmx512m -Xms256m -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"
```

This means:
- Max heap: 512MB
- Initial heap: 256MB
- Uses container-aware settings
- Limits to 75% of available RAM

## 🧪 Test Locally (Optional)

Before deploying, you can test the Docker build locally:

```bash
# Build the image
docker build -t elocate-api .

# Run the container
docker run -p 8080:8080 --env-file .env elocate-api

# Test
curl http://localhost:8080/elocate/actuator/health
```

## 🔄 Auto-Deployment

Every time you push to your main branch, Render will:
1. Pull the latest code
2. Build a new Docker image
3. Deploy the new version
4. Zero-downtime deployment

```bash
git add .
git commit -m "Update feature"
git push origin main
```

## ⚠️ Important Notes

### Free Tier Limitations:
- **512 MB RAM** - Docker image is optimized for this
- **Spins down after 15 min** - First request takes 30-60s (cold start)
- **750 hours/month** - Enough for continuous running
- **Build time** - Docker builds take 5-10 minutes

### Docker vs Native Java:
- ✅ More control over environment
- ✅ Consistent builds across platforms
- ✅ Better resource management
- ✅ Smaller runtime footprint
- ⚠️ Slightly longer build times

## 🔧 Troubleshooting

### Build Fails

**Error: "Cannot find Dockerfile"**
- Solution: Make sure Dockerfile is in root directory
- Check "Dockerfile Path" is set to `./Dockerfile`

**Error: "Maven build failed"**
- Solution: Check logs for specific error
- Ensure pom.xml is valid
- Try building locally first

**Error: "Out of memory during build"**
- Solution: This shouldn't happen with our config
- Contact Render support if it persists

### Container Crashes

**Error: "OutOfMemoryError"**
- Check logs in Render dashboard
- Memory is already optimized for 512MB
- Consider upgrading to paid tier

**Error: "Application failed to start"**
- Check environment variables are set correctly
- Verify database connection string
- Check logs for specific startup errors

### Slow Performance

**First request is very slow:**
- Normal for free tier (cold start)
- App spins down after 15 min inactivity
- Use UptimeRobot to keep it awake

**All requests are slow:**
- Check database connection
- Review logs for errors
- Monitor memory usage in dashboard

## 📈 Monitoring

View in Render Dashboard:
- **Logs** - Real-time application logs
- **Metrics** - CPU, Memory, Bandwidth
- **Events** - Deploy history
- **Shell** - Access container shell (paid tier only)

## 🔒 Security

Docker deployment includes:
- ✅ Non-root user in container
- ✅ Minimal Alpine Linux base
- ✅ Only JRE (not full JDK)
- ✅ No source code in final image
- ✅ Environment variables for secrets
- ✅ HTTPS by default (free SSL)

## 💰 Upgrade Options

**Free Tier ($0/month):**
- 512 MB RAM
- Spins down after 15 min
- 750 hours/month

**Starter Tier ($7/month):**
- 512 MB RAM
- Always on (no cold starts)
- Unlimited hours
- Better for production

**Standard Tier ($25/month):**
- 2 GB RAM
- Always on
- Better performance

## 📞 Support

- **Render Docs:** https://render.com/docs/docker
- **Community:** https://community.render.com
- **Status:** https://status.render.com

## ✅ Checklist

- [ ] Dockerfile created
- [ ] .dockerignore created
- [ ] Code pushed to GitHub
- [ ] Render account created
- [ ] Web service created with Docker runtime
- [ ] Environment variables configured
- [ ] Service deployed successfully
- [ ] Health check passes
- [ ] CORS updated (if needed)
- [ ] Auth0 URLs updated

## 🎉 Success!

Your Spring Boot application is now running in a Docker container on Render!

**Production URL:** `https://elocate-api.onrender.com/elocate`

**Next Steps:**
1. Test all endpoints
2. Update CORS configuration
3. Update Auth0 callback URLs
4. Set up monitoring (UptimeRobot)
5. Monitor logs and performance

---

**Need help?** Check the logs in your Render dashboard or refer to the troubleshooting section above.
