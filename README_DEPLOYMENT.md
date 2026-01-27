# Elocate API - Deployment Documentation

## 🌐 Live Deployment

**Production URL:** `https://elocate-api.onrender.com/elocate`

## 📁 Deployment Files

This repository includes the following deployment configuration files:

- `render.yaml` - Render service configuration
- `system.properties` - Java runtime version specification
- `build.sh` - Build script for Render
- `start.sh` - Startup script for the application
- `application-production.yaml` - Production environment configuration
- `RENDER_DEPLOYMENT_GUIDE.md` - Detailed deployment instructions
- `QUICK_DEPLOY.md` - Quick reference for deployment

## 🚀 Quick Deploy

See [QUICK_DEPLOY.md](QUICK_DEPLOY.md) for a 5-minute deployment checklist.

## 📖 Full Documentation

See [RENDER_DEPLOYMENT_GUIDE.md](RENDER_DEPLOYMENT_GUIDE.md) for comprehensive deployment instructions.

## 🔗 API Endpoints

Base URL: `https://elocate-api.onrender.com/elocate`

### Authentication Endpoints
- `POST /api/v1/auth/register` - Register new user
- `POST /api/v1/auth/login` - User login
- `POST /api/v1/auth/verify-email` - Verify email with OTP
- `POST /api/v1/auth/resend-otp` - Resend OTP
- `POST /api/v1/auth/forgot-password` - Request password reset
- `POST /api/v1/auth/refresh` - Refresh access token

### Health Check
- `GET /actuator/health` - Application health status

## 🔐 Environment Variables

All sensitive configuration is managed through Render environment variables:

- Database credentials
- Email service credentials
- JWT secret
- Auth0 credentials
- Application settings

## 🛠️ Technology Stack

- **Framework:** Spring Boot 3.5.8
- **Java Version:** 17
- **Database:** PostgreSQL (Neon)
- **Authentication:** Auth0 + JWT
- **Email:** Gmail SMTP
- **Hosting:** Render (Free Tier)

## 📊 Free Tier Specifications

- **RAM:** 512 MB
- **CPU:** Shared
- **Bandwidth:** 100 GB/month
- **Hours:** 750 hours/month
- **Cold Start:** ~30-60 seconds after 15 min inactivity

## 🔄 Auto-Deployment

The application automatically deploys when changes are pushed to the `main` branch.

```bash
git add .
git commit -m "Your changes"
git push origin main
```

Render will automatically:
1. Pull the latest code
2. Run the build command
3. Deploy the new version
4. Zero-downtime deployment

## 📝 Local Development

```bash
# Run with development profile
./mvnw spring-boot:run

# Run with production profile locally
./mvnw spring-boot:run -Dspring-boot.run.profiles=production
```

## 🧪 Testing the Deployment

```bash
# Health check
curl https://elocate-api.onrender.com/elocate/actuator/health

# Test authentication endpoint
curl -X POST https://elocate-api.onrender.com/elocate/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "password123"
  }'
```

## 🐛 Troubleshooting

### Viewing Logs
1. Go to https://dashboard.render.com
2. Select your service
3. Click "Logs" tab

### Common Issues

**502 Bad Gateway:**
- App is starting up (cold start)
- Wait 30-60 seconds and retry

**OutOfMemoryError:**
- Check logs in Render dashboard
- Memory optimizations already applied
- Consider upgrading to paid tier

**Database Connection Failed:**
- Verify environment variables are set correctly
- Check Neon DB is accessible

## 📈 Monitoring

Monitor your application through Render dashboard:
- Real-time logs
- CPU usage
- Memory usage
- Bandwidth usage
- Deploy history

## 🔒 Security

- All secrets stored as environment variables
- HTTPS enabled by default (free SSL)
- CORS configured for allowed origins
- JWT token-based authentication
- Auth0 integration for user management

## 💡 Performance Tips

1. **Keep Service Awake:** Use UptimeRobot to ping every 5 minutes
2. **Optimize Queries:** Database connection pool limited to 3
3. **Disable Debug Logging:** Production profile has `show-sql: false`
4. **Memory Management:** JVM configured with `-Xmx512m -Xms256m`

## 📞 Support

- **Render Support:** https://render.com/docs
- **Community:** https://community.render.com
- **Status:** https://status.render.com

## 📄 License

[Your License Here]

## 👥 Contributors

[Your Name/Team]

---

**Last Updated:** January 2026
