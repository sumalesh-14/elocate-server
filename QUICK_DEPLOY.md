# Quick Deploy to Render - Checklist

## ✅ Pre-Deployment Checklist

- [ ] Code pushed to GitHub
- [ ] All deployment files committed (render.yaml, system.properties, etc.)
- [ ] Render account created and linked to GitHub

## 🚀 Deployment Steps (5 Minutes)

### 1. Create Web Service
- Go to https://dashboard.render.com
- Click "New +" → "Web Service"
- Connect your GitHub repository

### 2. Basic Configuration
```
Name: elocate-api
Region: Oregon (US West)
Branch: main
Runtime: Java
Instance Type: Free
```

### 3. Build & Start Commands
```bash
# Build Command:
chmod +x mvnw && ./mvnw clean package -DskipTests

# Start Command:
java -Dspring.profiles.active=production -Xmx512m -Xms256m -jar target/elocate-0.0.1-SNAPSHOT.jar
```

### 4. Environment Variables (Copy-Paste Ready)

```env
SPRING_PROFILES_ACTIVE=production
SERVER_PORT=8080
CONTEXT_PATH=/elocate
DB_URL=jdbc:postgresql://ep-gentle-salad-admc88pi-pooler.c-2.us-east-1.aws.neon.tech/elocate?sslmode=require
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
JAVA_TOOL_OPTIONS=-Xmx512m -Xms256m
```

### 5. Deploy
- Click "Create Web Service"
- Wait 5-10 minutes for first build
- Get your URL: `https://elocate-api.onrender.com`

## 🧪 Test Your Deployment

```bash
# Health Check
curl https://elocate-api.onrender.com/elocate/actuator/health

# Test Login Endpoint
curl -X POST https://elocate-api.onrender.com/elocate/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"password123"}'
```

## ⚠️ Important Notes

1. **First Request is Slow:** Free tier spins down after 15 min inactivity (30-60s cold start)
2. **Memory Limit:** 512 MB RAM - app is optimized for this
3. **Auto-Deploy:** Pushes to main branch auto-deploy
4. **Logs:** View in Render dashboard under "Logs" tab

## 🔧 Post-Deployment

### Update CORS (if needed)
Add your Render URL to `SecurityConfig.java`:
```java
configuration.setAllowedOrigins(List.of(
    "http://localhost:3000",
    "https://elocate-api.onrender.com"
));
```

### Update Auth0
Add to Auth0 Dashboard → Applications → Settings:
- Allowed Callback URLs: `https://elocate-api.onrender.com/elocate/callback`
- Allowed Logout URLs: `https://elocate-api.onrender.com/elocate`
- Allowed Web Origins: `https://elocate-api.onrender.com`

## 📊 Monitor Your App

- **Dashboard:** https://dashboard.render.com
- **Logs:** Real-time in dashboard
- **Metrics:** CPU, Memory, Bandwidth usage

## 🆘 Common Issues

| Issue | Solution |
|-------|----------|
| Build fails | Check Java version in system.properties |
| App crashes | Check logs for OutOfMemoryError |
| Slow response | Normal for free tier cold starts |
| 502 Bad Gateway | App is starting up, wait 60 seconds |

## 💰 Cost

**Free Tier:**
- $0/month
- 750 hours/month
- Spins down after 15 min inactivity

**Upgrade to Starter ($7/month) for:**
- Always-on (no cold starts)
- Better performance

---

**Done!** Your API is live at: `https://elocate-api.onrender.com/elocate`
