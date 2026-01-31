# Swagger UI Setup Guide

## What Was Added

Swagger UI (OpenAPI 3.0) has been integrated into your Elocate API for interactive API documentation.

## Accessing Swagger UI

### Local Development
```
http://localhost:8080/elocate/swagger-ui.html
```

### Production (Render)
```
https://elocate-server-m2zi.onrender.com/elocate/swagger-ui.html
```

## How to Use

1. **Open Swagger UI** - Navigate to the URL above
2. **Browse APIs** - All your endpoints are automatically documented
3. **Test Endpoints** - Click "Try it out" on any endpoint

### Authentication

For protected endpoints:

1. Click the **"Authorize"** button (lock icon) at the top right
2. Enter your JWT token (without "Bearer " prefix)
3. Click "Authorize"
4. Now you can test protected endpoints

### Getting a JWT Token

1. Use the `/api/v1/auth/login` endpoint in Swagger
2. Click "Try it out"
3. Enter credentials in the request body
4. Click "Execute"
5. Copy the token from the response
6. Use it in the "Authorize" dialog

## What Changed

### Files Modified:
- `pom.xml` - Added SpringDoc OpenAPI dependency
- `SecurityConfig.java` - Whitelisted Swagger endpoints
- `application.yaml` - Added Swagger configuration
- `application-production.yaml` - Enabled Swagger in production

### Files Created:
- `OpenApiConfig.java` - Swagger configuration with JWT security

## Next Steps

After deploying:

1. Update the production server URL in `OpenApiConfig.java` if needed
2. Run `mvn clean install` to download the new dependency
3. Deploy to Render
4. Access Swagger at your production URL

## Features

- Interactive API testing
- JWT Bearer token authentication
- Automatic endpoint documentation
- Request/response examples
- Multiple server environments (local, production)

## Security Note

Swagger UI is publicly accessible but protected endpoints still require valid JWT tokens. Consider adding basic auth or IP restrictions for production if needed.
