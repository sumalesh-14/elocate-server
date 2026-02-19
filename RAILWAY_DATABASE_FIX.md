# Railway Database Connection Fix

## Error Analysis
```
Caused by: java.net.SocketException: Network unreachable
```

This means Railway cannot connect to Supabase database. Possible causes:
1. Supabase firewall blocking Railway's IP
2. Network routing issue
3. SSL/TLS configuration problem
4. Database URL format issue

## Solutions

### Solution 1: Use Railway's PostgreSQL (Recommended)

Railway provides its own PostgreSQL database that's guaranteed to work.

#### Steps:
1. In Railway dashboard, click **"+ New"**
2. Select **"Database" → "PostgreSQL"**
3. Railway will create a database and provide connection details
4. Railway automatically creates these variables:
   - `DATABASE_URL` (full connection string)
   - `PGHOST`
   - `PGPORT`
   - `PGUSER`
   - `PGPASSWORD`
   - `PGDATABASE`

5. Update your environment variables to use Railway's database:

```env
DB_URL=${DATABASE_URL}
DB_USERNAME=${PGUSER}
DB_PASSWORD=${PGPASSWORD}
```

Or use the full URL format:
```env
DB_URL=jdbc:postgresql://${PGHOST}:${PGPORT}/${PGDATABASE}?sslmode=require
```

### Solution 2: Fix Supabase Connection

#### Check Supabase Settings:

1. **Go to Supabase Dashboard**
2. **Settings → Database**
3. **Check "Connection Pooling"** - Use pooler connection string
4. **Check "Network Restrictions"** - Ensure Railway IPs are allowed

#### Update Connection String:

Try using Supabase's connection pooler (port 6543 instead of 5432):

```env
DB_URL=jdbc:postgresql://db.qnnkizacregmdsfgqrsw.supabase.co:6543/postgres?sslmode=require&prepareThreshold=0
```

#### Alternative Supabase Connection Formats:

**Option A: Direct Connection**
```env
DB_URL=jdbc:postgresql://db.qnnkizacregmdsfgqrsw.supabase.co:5432/postgres?sslmode=require
```

**Option B: Connection Pooler (Recommended for Railway)**
```env
DB_URL=jdbc:postgresql://db.qnnkizacregmdsfgqrsw.supabase.co:6543/postgres?sslmode=require&prepareThreshold=0
```

**Option C: IPv4 Only**
```env
DB_URL=jdbc:postgresql://db.qnnkizacregmdsfgqrsw.supabase.co:5432/postgres?sslmode=require&preferQueryMode=simple
```

### Solution 3: Use Neon Database

Neon is designed for serverless and works well with Railway.

```env
DB_URL=jdbc:postgresql://ep-gentle-salad-admc88pi-pooler.c-2.us-east-1.aws.neon.tech/elocate?sslmode=require
DB_USERNAME=neondb_owner
DB_PASSWORD=npg_veoMfz2N9xds
```

### Solution 4: Disable SSL Temporarily (Testing Only)

**⚠️ NOT RECOMMENDED FOR PRODUCTION**

```env
DB_URL=jdbc:postgresql://db.qnnkizacregmdsfgqrsw.supabase.co:5432/postgres?sslmode=disable
```

## Recommended Approach

### Use Railway PostgreSQL + Migrate Data

1. **Create Railway PostgreSQL Database**
   ```
   Railway Dashboard → + New → Database → PostgreSQL
   ```

2. **Get Connection Details**
   Railway provides: `DATABASE_URL`

3. **Update Environment Variables**
   ```env
   DB_URL=${DATABASE_URL}
   ```

4. **Migrate Data from Supabase to Railway**
   
   **Export from Supabase:**
   ```bash
   pg_dump "postgresql://postgres:AL+4kWTv%A+k9DK@db.qnnkizacregmdsfgqrsw.supabase.co:5432/postgres" > backup.sql
   ```

   **Import to Railway:**
   ```bash
   psql "${DATABASE_URL}" < backup.sql
   ```

## Quick Fix for Immediate Deployment

### Option 1: Railway PostgreSQL Variables

Add to Railway:
```env
DB_URL=${DATABASE_URL}
```

Railway will automatically substitute `${DATABASE_URL}` with the actual connection string.

### Option 2: Use Neon (Already Configured)

Update `railway.env`:
```env
DB_URL=jdbc:postgresql://ep-gentle-salad-admc88pi-pooler.c-2.us-east-1.aws.neon.tech/elocate?sslmode=require
DB_USERNAME=neondb_owner
DB_PASSWORD=npg_veoMfz2N9xds
```

## Testing Connection

### Test from Railway Shell

1. Go to Railway service
2. Click **"Shell"** or **"Terminal"**
3. Test connection:

```bash
# Test DNS resolution
nslookup db.qnnkizacregmdsfgqrsw.supabase.co

# Test port connectivity
nc -zv db.qnnkizacregmdsfgqrsw.supabase.co 5432

# Test with psql (if available)
psql "postgresql://postgres:AL+4kWTv%A+k9DK@db.qnnkizacregmdsfgqrsw.supabase.co:5432/postgres?sslmode=require"
```

## Updated Railway Variables

### For Railway PostgreSQL (Recommended):
```env
DB_URL=${DATABASE_URL}
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

### For Neon Database:
```env
DB_URL=jdbc:postgresql://ep-gentle-salad-admc88pi-pooler.c-2.us-east-1.aws.neon.tech/elocate?sslmode=require
DB_USERNAME=neondb_owner
DB_PASSWORD=npg_veoMfz2N9xds
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

## Verification

After updating variables:
1. Railway will automatically redeploy
2. Check logs for successful connection
3. Look for: `HikariPool-1 - Start completed`
4. Test health endpoint: `https://your-app.railway.app/actuator/health`

## Support

If issues persist:
1. Check Railway logs for detailed error
2. Verify database is accessible from Railway's network
3. Try Railway's built-in PostgreSQL
4. Contact Railway support for network issues
