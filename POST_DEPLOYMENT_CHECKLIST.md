# 🎉 Post-Deployment Checklist

## ✅ Your App is Live!

Congratulations! Your Elocate API is successfully deployed on Render.

---

## 📋 Important Tasks to Complete

### 1. ✏️ Update CORS Configuration

**File:** `src/main/java/com/elocate/elocate/config/SecurityConfig.java`

Replace this line:
```java
"https://your-app-name.onrender.com"  // TODO: Replace with your actual Render URL
```

With your actual Render URL, for example:
```java
"https://elocate-api-xyz.onrender.com"
```

Then commit and push:
```bash
git add src/main/java/com/elocate/elocate/config/SecurityConfig.java
git commit -m "Update CORS with production URL"
git push origin main
```

Render will automatically redeploy with the new CORS settings.

---

### 2. 🔐 Update Auth0 Settings

Go to your Auth0 Dashboard: https://manage.auth0.com

**Navigate to:** Applications → Your Application → Settings

**Add your Render URL to:**

**Allowed Callback URLs:**
```
https://your-app-name.onrender.com/elocate/callback
```

**Allowed Logout URLs:**
```
https://your-app-name.onrender.com/elocate
```

**Allowed Web Origins:**
```
https://your-app-name.onrender.com
```

**Allowed Origins (CORS):**
```
https://your-app-name.onrender.com
```

Click **Save Changes** at the bottom.

---

### 3. 🧪 Test Your Endpoints

#### Health Check:
```bash
curl https://your-app-name.onrender.com/elocate/actuator/health
```

Expected response:
```json
{"status":"UP"}
```

#### Test Registration:
```bash
curl -X POST https://your-app-name.onrender.com/elocate/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "Test123!@#",
    "fullName": "Test User",
    "mobileNumber": "+919876543210"
  }'
```

#### Test Login:
```bash
curl -X POST https://your-app-name.onrender.com/elocate/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "Test123!@#"
  }'
```

---

### 4. 📊 Set Up Monitoring (Optional but Recommended)

**Use UptimeRobot to prevent cold starts:**

1. Go to https://uptimerobot.com (free account)
2. Click "Add New Monitor"
3. Configure:
   - Monitor Type: HTTP(s)
   - Friendly Name: Elocate API
   - URL: `https://your-app-name.onrender.com/elocate/actuator/health`
   - Monitoring Interval: 5 minutes
4. Click "Create Monitor"

This will ping your app every 5 minutes, preventing it from spinning down on the free tier.

---

### 5. 🔒 Security Improvements (Recommended)

#### Generate a Strong JWT Secret:

```bash
# On Linux/Mac:
openssl rand -base64 64

# On Windows (PowerShell):
[Convert]::ToBase64String((1..64 | ForEach-Object { Get-Random -Minimum 0 -Maximum 256 }))
```

Update the `JWT_SECRET` environment variable in Render dashboard with the new value.

#### Review Sensitive Data:

Make sure these are marked as "Secret" in Render:
- ✅ DB_PASSWORD
- ✅ EMAIL_PASSWORD
- ✅ JWT_SECRET
- ✅ AUTH0_CLIENT_SECRET

---

### 6. 📱 Update Frontend Configuration

If you have a frontend application, update the API base URL:

```javascript
// Before (development)
const API_BASE_URL = 'http://localhost:8080/elocate';

// After (production)
const API_BASE_URL = 'https://your-app-name.onrender.com/elocate';
```

Or use environment variables:
```javascript
const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/elocate';
```

---

### 7. 📝 Document Your API

Create a simple API documentation file or use Swagger/OpenAPI.

**Quick Documentation Template:**

```markdown
# Elocate API Documentation

## Base URL
Production: https://your-app-name.onrender.com/elocate
Development: http://localhost:8080/elocate

## Authentication Endpoints

### Register
POST /api/v1/auth/register

### Login
POST /api/v1/auth/login

### Verify Email
POST /api/v1/auth/verify-email

## Protected Endpoints
All other endpoints require JWT token in Authorization header:
Authorization: Bearer <your-jwt-token>
```

---

### 8. 🔄 Set Up CI/CD (Optional)

Your app already auto-deploys when you push to GitHub. To add more control:

**Create `.github/workflows/deploy.yml`:**
```yaml
name: Deploy to Render

on:
  push:
    branches: [ main ]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'
      - name: Run tests
        run: ./mvnw test
```

---

## ⚠️ Important Reminders

### Free Tier Limitations:
- ✅ 512 MB RAM (optimized)
- ⚠️ Spins down after 15 min inactivity
- ⚠️ First request after inactivity takes 30-60 seconds
- ✅ 750 hours/month (enough for continuous running)

### Performance Tips:
1. Use UptimeRobot to keep app awake
2. Optimize database queries
3. Enable caching where appropriate
4. Monitor logs for errors

### Cost Considerations:
- **Free Tier:** $0/month (current)
- **Starter Tier:** $7/month (always on, no cold starts)
- **Standard Tier:** $25/month (2GB RAM, better performance)

---

## 📊 Monitoring Your App

### Render Dashboard:
- **Logs:** Real-time application logs
- **Metrics:** CPU, Memory, Bandwidth usage
- **Events:** Deploy history and status
- **Shell:** Access container (paid tier only)

### Check Logs:
```bash
# Install Render CLI (optional)
npm install -g @render/cli

# View logs
render logs
```

---

## 🐛 Troubleshooting

### App is slow on first request:
- Normal for free tier (cold start)
- Use UptimeRobot to keep it awake

### CORS errors:
- Make sure you updated SecurityConfig.java
- Verify Auth0 allowed origins

### Database connection errors:
- Check environment variables in Render
- Verify Neon database is accessible

### 502 Bad Gateway:
- App is starting up, wait 60 seconds
- Check logs for startup errors

---

## 🎯 Next Steps

1. [ ] Update CORS configuration with your Render URL
2. [ ] Update Auth0 callback URLs
3. [ ] Test all endpoints
4. [ ] Set up UptimeRobot monitoring
5. [ ] Generate new JWT secret
6. [ ] Update frontend API URL
7. [ ] Document your API
8. [ ] Monitor logs for errors
9. [ ] Consider upgrading if you need always-on service

---

## 📞 Support Resources

- **Render Docs:** https://render.com/docs
- **Render Community:** https://community.render.com
- **Render Status:** https://status.render.com
- **Auth0 Docs:** https://auth0.com/docs

---

## 🎉 Congratulations!

Your Elocate API is now live in production! 🚀

**Production URL:** https://your-app-name.onrender.com/elocate

Keep monitoring your logs and performance. Consider upgrading to a paid tier if you need:
- Always-on service (no cold starts)
- Better performance
- More resources

Good luck with your project! 🎊
