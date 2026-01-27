# 🚀 Deploy Elocate API to Render - START HERE

## ⚠️ IMPORTANT: Use Docker Runtime

Render **does not have a "Java" runtime option**. You must select **Docker** from the Language dropdown.

## 📁 Files You Need

All deployment files have been created for you:

✅ `Dockerfile` - Multi-stage Docker build for Java 17  
✅ `.dockerignore` - Excludes unnecessary files from Docker build  
✅ `render.yaml` - Render service configuration  
✅ `system.properties` - Java version specification  
✅ `application-production.yaml` - Production environment config  

## 📖 Which Guide Should I Follow?

### 🎯 Quick Visual Guide (Recommended)
**File:** `RENDER_SETUP_VISUAL.txt`  
**Best for:** Visual learners who want to see exactly what to fill in  
**Time:** 5 minutes

### 🐳 Complete Docker Guide
**File:** `DOCKER_DEPLOY_GUIDE.md`  
**Best for:** Understanding the Docker setup and troubleshooting  
**Time:** 10 minutes

### ⚡ Quick Deploy Checklist
**File:** `QUICK_DEPLOY.md`  
**Best for:** Experienced users who just need the commands  
**Time:** 3 minutes

### 📋 Step-by-Step Text Guide
**File:** `DEPLOYMENT_STEPS.txt`  
**Best for:** Following along step-by-step in terminal  
**Time:** 15 minutes

## 🎬 Quick Start (3 Steps)

### Step 1: Push to GitHub
```bash
git add .
git commit -m "Add Docker deployment for Render"
git push origin main
```

### Step 2: Create Render Service
1. Go to https://render.com and sign up
2. Click "New +" → "Web Service"
3. Connect your GitHub repository

### Step 3: Configure
```
Runtime: Docker ⭐ (Select from dropdown)
Dockerfile Path: ./Dockerfile
Docker Context: .
Instance Type: Free
```

Then add environment variables (see any guide for the full list).

## 🔑 Key Configuration

### Runtime Selection (MOST IMPORTANT!)
```
Language/Runtime: Docker ⭐
```

**NOT** Java, Node, Python, etc. - Select **Docker**!

### Docker Settings
```
Dockerfile Path: ./Dockerfile
Docker Context: .
Docker Command: (leave blank)
```

### Instance Type
```
Free - $0/month
512 MB RAM
Spins down after 15 min inactivity
```

## 🌐 Your App URL

After deployment, your API will be live at:
```
https://elocate-api.onrender.com/elocate
```

Test with:
```bash
curl https://elocate-api.onrender.com/elocate/actuator/health
```

## 📊 What Happens During Deployment?

1. **Render clones your repo** from GitHub
2. **Builds Docker image** (5-10 minutes first time)
   - Stage 1: Maven builds your JAR file
   - Stage 2: Creates lightweight runtime image
3. **Starts container** with your Spring Boot app
4. **Assigns URL** and makes it live

## ⚠️ Free Tier Limitations

- **512 MB RAM** - Already optimized in Dockerfile
- **Spins down after 15 min** - First request takes 30-60s
- **750 hours/month** - Enough for continuous running
- **Build time** - 5-10 minutes per deployment

## 🔧 Environment Variables Required

You'll need to add these in Render dashboard:

```env
SPRING_PROFILES_ACTIVE=production
SERVER_PORT=8080
CONTEXT_PATH=/elocate
DB_URL=jdbc:postgresql://...
DB_USERNAME=neondb_owner
DB_PASSWORD=npg_veoMfz2N9xds
EMAIL_HOST=smtp.gmail.com
EMAIL_PORT=587
EMAIL_USERNAME=kasumalesh@gmail.com
EMAIL_PASSWORD=dwqx mclv bkkf soxt
JWT_SECRET=elocate-super-secret-key-change-in-production-minimum-256-bits-required
JWT_EXPIRATION_MS=86400000
AUTH0_DOMAIN=elocate-dev.us.auth0.com
AUTH0_CLIENT_ID=gSMm494WIxE59MtZxODuYvdCRy4yFT10
AUTH0_CLIENT_SECRET=NalHurNjv35rQu_zgmR_vkOHB2-OmuOofKBY2lMRUz4Lu3d6F2RQVM9T8REVqHtZ
AUTH0_CONNECTION=Username-Password-Authentication
OTP_EXPIRY_MINUTES=10
OTP_MAX_ATTEMPTS=3
```

## 🐛 Common Issues

### "Cannot find Java runtime"
✅ **Solution:** Select **Docker** runtime, not Java

### "Build failed"
✅ **Solution:** Check Dockerfile path is `./Dockerfile`

### "Out of memory"
✅ **Solution:** Already optimized for 512MB, should work fine

### "502 Bad Gateway"
✅ **Solution:** App is starting (cold start), wait 60 seconds

## 📞 Need Help?

1. Check the logs in Render dashboard
2. Read `DOCKER_DEPLOY_GUIDE.md` for troubleshooting
3. Visit https://community.render.com

## ✅ Deployment Checklist

- [ ] All files committed and pushed to GitHub
- [ ] Render account created
- [ ] Web service created with **Docker** runtime
- [ ] Dockerfile path set to `./Dockerfile`
- [ ] Docker context set to `.`
- [ ] Instance type set to **Free**
- [ ] All environment variables added
- [ ] Sensitive variables marked as "Secret"
- [ ] Service deployed successfully
- [ ] Health check endpoint returns `{"status":"UP"}`

## 🎉 Next Steps After Deployment

1. **Test your endpoints** - Use Postman or curl
2. **Update CORS** - Add your Render URL to SecurityConfig.java
3. **Update Auth0** - Add callback URLs in Auth0 dashboard
4. **Monitor logs** - Check Render dashboard for any errors
5. **Set up monitoring** - Use UptimeRobot to prevent cold starts

## 💰 Upgrade Options

**Free Tier ($0/month):**
- Perfect for testing and development
- Spins down after 15 min inactivity

**Starter Tier ($7/month):**
- Always on (no cold starts)
- Better for production use

## 📚 Additional Resources

- **Render Docs:** https://render.com/docs/docker
- **Docker Guide:** `DOCKER_DEPLOY_GUIDE.md`
- **Visual Setup:** `RENDER_SETUP_VISUAL.txt`
- **Quick Reference:** `QUICK_DEPLOY.md`

---

## 🚀 Ready to Deploy?

1. Open `RENDER_SETUP_VISUAL.txt` for visual step-by-step guide
2. Or follow `DOCKER_DEPLOY_GUIDE.md` for complete instructions
3. Push your code and create your Render service
4. Your app will be live in 10 minutes!

**Good luck! 🎉**
