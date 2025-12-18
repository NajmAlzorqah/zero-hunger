# ZeroHunger - Food Donation Platform

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Jakarta EE](https://img.shields.io/badge/Jakarta%20EE-10-blue.svg)](https://jakarta.ee/)
[![Next.js](https://img.shields.io/badge/Next.js-16-black.svg)](https://nextjs.org/)
[![React](https://img.shields.io/badge/React-19-blue.svg)](https://reactjs.org/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-blue.svg)](https://www.mysql.com/)

A full-stack food donation platform connecting food donors with recipients to reduce food waste and fight hunger. Built with Jakarta EE 10 backend and Next.js frontend.

## 📋 Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Technology Stack](#technology-stack)
- [System Requirements](#system-requirements)
- [Installation & Setup](#installation--setup)
- [Running the Application](#running-the-application)
- [API Documentation](#api-documentation)
- [Project Structure](#project-structure)
- [Troubleshooting](#troubleshooting)
- [Contributing](#contributing)
- [License](#license)

## 🎯 Overview

ZeroHunger is an enterprise-grade food donation management system that enables:

- **Donors**: Post available food donations with location and expiry details
- **Recipients**: Browse and claim nearby food donations
- **Real-time**: Map-based donation discovery with geolocation
- **Secure**: JWT-based authentication and role-based access control

### Architecture

- **Backend**: Jakarta EE 10 (JAX-RS REST API + JPA + EJB)
- **Frontend**: Next.js 16 with React 19
- **Database**: MySQL 8.0
- **Server**: GlassFish 7.x / WildFly 27+

## ✨ Features

### Core Functionality

- ✅ User authentication (Register/Login/Logout)
- ✅ Role-based access (Donor/Recipient)
- ✅ Food donation management (Create/Update/Delete)
- ✅ Claim system with pickup codes
- ✅ Geolocation-based donation search
- ✅ Real-time notifications
- ✅ Interactive donation map
- ✅ Donation status tracking (Available/Claimed/Picked Up/Expired)

### Technical Features

- JWT token-based authentication
- BCrypt password hashing
- CORS support for cross-origin requests
- RESTful API design
- JPA entity relationships
- Container-managed transactions (JTA)
- Responsive UI with Tailwind CSS
- Interactive maps with Leaflet

## 🛠 Technology Stack

### Backend (zero-hunger-api)

| Technology        | Version | Purpose              |
| ----------------- | ------- | -------------------- |
| Java              | 21      | Programming Language |
| Jakarta EE        | 10.0.0  | Enterprise Framework |
| Hibernate         | 6.2.7   | JPA Implementation   |
| MySQL Connector   | 8.2.0   | Database Driver      |
| JWT (jjwt)        | 0.12.3  | Token Authentication |
| BCrypt            | 0.4     | Password Hashing     |
| Maven             | 3.8+    | Build Tool           |
| GlassFish/WildFly | 7.x/27+ | Application Server   |

### Frontend (zero-hunger-frontend)

| Technology   | Version | Purpose         |
| ------------ | ------- | --------------- |
| Next.js      | 16.0.8  | React Framework |
| React        | 19.2.1  | UI Library      |
| Axios        | 1.13.2  | HTTP Client     |
| Leaflet      | 1.9.4   | Map Integration |
| Tailwind CSS | 4.x     | Styling         |
| pnpm         | 10.15.0 | Package Manager |

### Database

| Technology | Version | Purpose             |
| ---------- | ------- | ------------------- |
| MySQL      | 8.0     | Relational Database |
| Docker     | Latest  | Container Runtime   |

## 📦 System Requirements

### Required Software

1. **Java Development Kit (JDK)**

   - Version: 21 or higher
   - Download: [Oracle JDK](https://www.oracle.com/java/technologies/downloads/) or [OpenJDK](https://adoptium.net/)

2. **Application Server** (Choose one)

   - **GlassFish 7.x** (Recommended)
     - Download: [GlassFish Downloads](https://glassfish.org/download)
   - **WildFly 27+**
     - Download: [WildFly Downloads](https://www.wildfly.org/downloads/)

3. **Apache Maven**

   - Version: 3.8 or higher
   - Download: [Maven Downloads](https://maven.apache.org/download.cgi)

4. **Node.js & pnpm**

   - Node.js: 18.x or higher
   - pnpm: 10.x or higher
   - Download: [Node.js](https://nodejs.org/)
   - Install pnpm: `npm install -g pnpm`

5. **Docker & Docker Compose**

   - Required for MySQL database
   - Download: [Docker Desktop](https://www.docker.com/products/docker-desktop/)

6. **Git** (Optional, for cloning)
   - Download: [Git](https://git-scm.com/downloads)

### Minimum Hardware

- **CPU**: 2 cores
- **RAM**: 4 GB (8 GB recommended)
- **Disk**: 2 GB free space
- **OS**: Windows 10/11, macOS 10.15+, or Linux

## 🚀 Installation & Setup

### Step 1: Clone the Repository

```bash
git clone https://github.com/NajmAlzorqah/zero-hunger.git
cd zero-hunger
```

### Step 2: Database Setup (Docker)

The project includes a Docker Compose configuration for MySQL.

```bash
# Navigate to the API directory
cd zero-hunger-api

# Start MySQL container
docker-compose up -d

# Verify container is running
docker ps
```

**Database Credentials (from docker-compose.yml):**

```
Host: localhost
Port: 3306
Database: zerohunger
Username: user
Password: password
Root Password: password
```

**Alternative: Manual MySQL Installation**

If not using Docker, create the database manually:

```sql
CREATE DATABASE zerohunger CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'user'@'localhost' IDENTIFIED BY 'password';
GRANT ALL PRIVILEGES ON zerohunger.* TO 'user'@'localhost';
FLUSH PRIVILEGES;
```

### Step 3: Configure Application Server

#### For GlassFish 7.x

1. **Start GlassFish**

   ```bash
   cd <GLASSFISH_HOME>/bin
   ./asadmin start-domain
   ```

2. **Create JDBC Connection Pool**

   ```bash
   asadmin create-jdbc-connection-pool \
     --datasourceclassname com.mysql.cj.jdbc.MysqlDataSource \
     --restype javax.sql.DataSource \
     --property serverName=localhost:portNumber=3306:databaseName=zerohunger:user=user:password=password:useSSL=false:allowPublicKeyRetrieval=true \
     ZeroHungerPool
   ```

3. **Create JDBC Resource**

   ```bash
   asadmin create-jdbc-resource \
     --connectionpoolid ZeroHungerPool \
     jdbc/ZeroHungerDS
   ```

4. **Verify Configuration**

   ```bash
   asadmin ping-connection-pool ZeroHungerPool
   ```

5. **Configure HTTP Listener Port (if needed)**
   ```bash
   # Set to port 9090
   asadmin set server-config.network-config.network-listeners.network-listener.http-listener-1.port=9090
   ```

#### For WildFly 27+

1. **Add MySQL Module**

   - Copy `mysql-connector-j-8.2.0.jar` to `<WILDFLY_HOME>/modules/com/mysql/main/`
   - Create `module.xml` in the same directory:

   ```xml
   <?xml version="1.0" encoding="UTF-8"?>
   <module xmlns="urn:jboss:module:1.9" name="com.mysql">
       <resources>
           <resource-root path="mysql-connector-j-8.2.0.jar"/>
       </resources>
       <dependencies>
           <module name="javax.api"/>
           <module name="javax.transaction.api"/>
       </dependencies>
   </module>
   ```

2. **Configure Datasource in standalone.xml**

   - Add under `<datasources>`:

   ```xml
   <datasource jndi-name="java:/jdbc/ZeroHungerDS" pool-name="ZeroHungerPool">
       <connection-url>jdbc:mysql://localhost:3306/zerohunger</connection-url>
       <driver>mysql</driver>
       <security>
           <user-name>user</user-name>
           <password>password</password>
       </security>
   </datasource>
   ```

3. **Add Driver**
   ```xml
   <driver name="mysql" module="com.mysql">
       <driver-class>com.mysql.cj.jdbc.Driver</driver-class>
   </driver>
   ```

### Step 4: Build Backend

```bash
cd zero-hunger-api

# Clean and build the project
mvn clean package

# This creates: target/zero-hunger-1.0-SNAPSHOT.war
```

### Step 5: Deploy Backend

#### GlassFish Deployment

```bash
# Option 1: Command line
asadmin deploy target/zero-hunger-1.0-SNAPSHOT.war

# Option 2: Admin Console
# Navigate to http://localhost:4848
# Applications > Deploy > Choose WAR file > Deploy
```

#### WildFly Deployment

```bash
# Copy WAR to deployments directory
cp target/zero-hunger-1.0-SNAPSHOT.war <WILDFLY_HOME>/standalone/deployments/
```

**Verify Backend:**

```bash
curl http://localhost:9090/zero-hunger/api/v1/donations
```

### Step 6: Setup Frontend

```bash
cd ../zero-hunger-frontend

# Install dependencies
pnpm install

# Create environment file
echo "NEXT_PUBLIC_API_URL=http://localhost:9090/zero-hunger/api/v1" > .env.local
```

## 🎮 Running the Application

### Start All Services

1. **Database** (if not running):

   ```bash
   cd zero-hunger-api
   docker-compose up -d
   ```

2. **Backend** (if not already deployed):

   ```bash
   # GlassFish should be running with deployed application
   # Check status:
   curl http://localhost:9090/zero-hunger/api/v1/donations
   ```

3. **Frontend**:

   ```bash
   cd zero-hunger-frontend

   # Development mode
   pnpm dev

   # Production build
   pnpm build
   pnpm start
   ```

### Access Points

| Service         | URL                                      | Port |
| --------------- | ---------------------------------------- | ---- |
| Frontend        | http://localhost:3000                    | 3000 |
| Backend API     | http://localhost:9090/zero-hunger/api/v1 | 9090 |
| MySQL Database  | localhost:3306                           | 3306 |
| GlassFish Admin | http://localhost:4848                    | 4848 |

### Default Test Accounts

After first deployment, JPA will create tables automatically. You can register new accounts or use the API to create test users.

**Example Registration:**

```bash
curl -X POST http://localhost:9090/zero-hunger/api/v1/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test Donor",
    "email": "donor@test.com",
    "password": "password123",
    "phone": "+1234567890",
    "role": "donor"
  }'
```

## 📚 API Documentation

### Base URL

```
http://localhost:9090/zero-hunger/api/v1
```

### Authentication Endpoints

| Method | Endpoint    | Description       | Auth Required |
| ------ | ----------- | ----------------- | ------------- |
| POST   | `/register` | User registration | No            |
| POST   | `/login`    | User login        | No            |
| POST   | `/logout`   | User logout       | Yes           |
| GET    | `/me`       | Get current user  | Yes           |

### Donation Endpoints

| Method | Endpoint                | Description                  | Auth Required   |
| ------ | ----------------------- | ---------------------------- | --------------- |
| GET    | `/donations`            | List all available donations | No              |
| POST   | `/donations`            | Create new donation          | Yes (Donor)     |
| GET    | `/donations/{id}`       | Get donation details         | No              |
| PUT    | `/donations/{id}`       | Update donation              | Yes (Owner)     |
| DELETE | `/donations/{id}`       | Delete donation              | Yes (Owner)     |
| GET    | `/donations/nearby`     | Find nearby donations        | No              |
| POST   | `/donations/{id}/claim` | Claim a donation             | Yes (Recipient) |
| GET    | `/my-donations`         | Get user's donations         | Yes             |

### Claim Endpoints

| Method | Endpoint              | Description        | Auth Required |
| ------ | --------------------- | ------------------ | ------------- |
| GET    | `/claims`             | List user's claims | Yes           |
| POST   | `/claims/{id}/pickup` | Mark as picked up  | Yes           |
| PUT    | `/claims/{id}/cancel` | Cancel claim       | Yes           |

### Request Examples

**Login:**

```bash
curl -X POST http://localhost:9090/zero-hunger/api/v1/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "donor@test.com",
    "password": "password123"
  }'
```

**Create Donation:**

```bash
curl -X POST http://localhost:9090/zero-hunger/api/v1/donations \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "foodType": "Fresh Vegetables",
    "quantity": 10,
    "unit": "kg",
    "expiryDate": "2025-12-25T10:00:00",
    "pickupLocation": "123 Main St, City",
    "latitude": 40.7128,
    "longitude": -74.0060,
    "description": "Fresh organic vegetables"
  }'
```

**Find Nearby Donations:**

```bash
curl "http://localhost:9090/zero-hunger/api/v1/donations/nearby?latitude=40.7128&longitude=-74.0060&radius=10"
```

### Response Format

All API responses follow this format:

**Success:**

```json
{
  "success": true,
  "data": { ... },
  "message": "Operation successful"
}
```

**Error:**

```json
{
  "success": false,
  "error": "Error message",
  "details": "Detailed error information"
}
```

## 📁 Project Structure

```
zero-hunger/
├── zero-hunger-api/              # Jakarta EE Backend
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/codemavricks/zerohunger/
│   │   │   │   ├── model/        # JPA Entities
│   │   │   │   │   ├── User.java
│   │   │   │   │   ├── Donation.java
│   │   │   │   │   ├── Claim.java
│   │   │   │   │   ├── Notification.java
│   │   │   │   │   └── Token.java
│   │   │   │   ├── resource/     # JAX-RS REST Endpoints
│   │   │   │   │   ├── AuthResource.java
│   │   │   │   │   ├── DonationResource.java
│   │   │   │   │   ├── ClaimResource.java
│   │   │   │   │   └── NotificationResource.java
│   │   │   │   ├── service/      # EJB Business Logic
│   │   │   │   │   ├── AuthService.java
│   │   │   │   │   ├── DonationService.java
│   │   │   │   │   ├── ClaimService.java
│   │   │   │   │   └── NotificationService.java
│   │   │   │   ├── dto/          # Data Transfer Objects
│   │   │   │   ├── filter/       # Security & CORS Filters
│   │   │   │   │   ├── AuthFilter.java
│   │   │   │   │   ├── CorsFilter.java
│   │   │   │   │   └── Secured.java
│   │   │   │   └── util/         # Utilities
│   │   │   │       └── JwtUtil.java
│   │   │   ├── resources/
│   │   │   │   └── META-INF/
│   │   │   │       ├── persistence.xml  # JPA Configuration
│   │   │   │       └── beans.xml        # CDI Configuration
│   │   │   └── webapp/
│   │   │       └── WEB-INF/
│   │   │           └── web.xml          # Web Application Config
│   │   └── test/                 # Unit Tests
│   ├── pom.xml                   # Maven Configuration
│   ├── docker-compose.yml        # MySQL Container Config
│   └── README.md                 # Backend Documentation
│
└── zero-hunger-frontend/         # Next.js Frontend
    ├── src/
    │   ├── app/                  # Next.js App Router
    │   │   ├── page.js           # Home Page
    │   │   ├── layout.js         # Root Layout
    │   │   ├── login/            # Login Page
    │   │   ├── register/         # Registration Page
    │   │   └── dashboard/        # Dashboard Page
    │   ├── components/           # React Components
    │   │   ├── CreateDonationForm.js
    │   │   ├── DonationMap.js
    │   │   └── ClaimActions.js
    │   ├── contexts/             # React Contexts
    │   │   └── AuthContext.js    # Authentication State
    │   └── lib/                  # Utilities
    │       └── api.js            # API Client (Axios)
    ├── public/                   # Static Assets
    ├── package.json              # Node Dependencies
    ├── next.config.mjs           # Next.js Configuration
    ├── tailwind.config.js        # Tailwind Configuration
    └── README.md                 # Frontend Documentation
```

## 🔧 Troubleshooting

### Database Connection Issues

**Problem**: `Cannot connect to database`

**Solution**:

```bash
# Check if MySQL container is running
docker ps

# Check MySQL logs
docker logs zerohunger-db

# Restart container
docker-compose restart

# Test connection manually
mysql -h localhost -P 3306 -u user -ppassword zerohunger
```

### JDBC Resource Not Found

**Problem**: `jdbc/ZeroHungerDS not found`

**Solution**:

```bash
# Verify JDBC resource exists
asadmin list-jdbc-resources

# Recreate if missing (see Step 3)
asadmin create-jdbc-resource --connectionpoolid ZeroHungerPool jdbc/ZeroHungerDS

# Test connection
asadmin ping-connection-pool ZeroHungerPool
```

### Port Already in Use

**Problem**: `Port 9090 already in use`

**Solution**:

```bash
# Windows
netstat -ano | findstr :9090
taskkill /PID <PID> /F

# Linux/Mac
lsof -i :9090
kill -9 <PID>

# Or change GlassFish port
asadmin set server-config.network-config.network-listeners.network-listener.http-listener-1.port=8080
```

### Frontend Cannot Connect to Backend

**Problem**: CORS errors or connection refused

**Solution**:

1. Verify backend is running:

   ```bash
   curl http://localhost:9090/zero-hunger/api/v1/donations
   ```

2. Check `.env.local`:

   ```bash
   cat .env.local
   # Should show: NEXT_PUBLIC_API_URL=http://localhost:9090/zero-hunger/api/v1
   ```

3. Restart frontend:
   ```bash
   pnpm dev
   ```

### Maven Build Fails

**Problem**: Dependencies cannot be downloaded

**Solution**:

```bash
# Clear Maven cache
rm -rf ~/.m2/repository

# Rebuild with force update
mvn clean package -U

# Skip tests if needed
mvn clean package -DskipTests
```

### Tables Not Created

**Problem**: Database tables don't exist

**Solution**:
The tables are created automatically by JPA on first deployment. Check `persistence.xml`:

```xml
<property name="jakarta.persistence.schema-generation.database.action" value="create"/>
```

To manually trigger:

```bash
# Undeploy and redeploy application
asadmin undeploy zero-hunger-1.0-SNAPSHOT
asadmin deploy target/zero-hunger-1.0-SNAPSHOT.war
```

### GlassFish Admin Console Not Accessible

**Problem**: Cannot access http://localhost:4848

**Solution**:

```bash
# Check if domain is running
asadmin list-domains

# Start domain
asadmin start-domain

# Enable secure admin (if needed)
asadmin enable-secure-admin
asadmin restart-domain
```

## 🛠 Development

### Running in Development Mode

**Backend**:

- Make code changes
- Rebuild: `mvn clean package`
- Redeploy: `asadmin redeploy target/zero-hunger-1.0-SNAPSHOT.war`

**Frontend**:

```bash
pnpm dev  # Hot reload enabled
```

### Database Management

**View Tables**:

```bash
docker exec -it zerohunger-db mysql -u user -ppassword zerohunger -e "SHOW TABLES;"
```

**Backup Database**:

```bash
docker exec zerohunger-db mysqldump -u user -ppassword zerohunger > backup.sql
```

**Restore Database**:

```bash
docker exec -i zerohunger-db mysql -u user -ppassword zerohunger < backup.sql
```

### Viewing Logs

**GlassFish Logs**:

```bash
tail -f <GLASSFISH_HOME>/glassfish/domains/domain1/logs/server.log
```

**MySQL Logs**:

```bash
docker logs -f zerohunger-db
```

**Frontend Logs**:
Visible in the terminal where `pnpm dev` is running.

## 📖 Additional Documentation

- **API Comparison**: See `zero-hunger-api/API-COMPARISON.md`
- **Java EE Concepts**: See `zero-hunger-api/JAVA-EE-CONCEPTS-ANALYSIS.md`
- **Backend Details**: See `zero-hunger-api/README.md`
- **Frontend Details**: See `zero-hunger-frontend/README.md`

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/YourFeature`
3. Commit changes: `git commit -m 'Add YourFeature'`
4. Push to branch: `git push origin feature/YourFeature`
5. Submit a pull request

See [CONTRIBUTING.md](CONTRIBUTING.md) for detailed contribution guidelines.

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 👥 Authors

- **Najm Alzorqah** - [GitHub](https://github.com/NajmAlzorqah)

## 🙏 Acknowledgments

- Jakarta EE Platform
- Next.js Team
- MySQL Community
- GlassFish Project
- All contributors who help make this project better

## 📞 Support

For issues and questions:

- 📝 [Create an issue](https://github.com/NajmAlzorqah/zero-hunger/issues) on GitHub
- 📖 Check the [troubleshooting section](#troubleshooting)
- 📚 Review the documentation in the respective project folders
- 🤝 Read our [Code of Conduct](CODE_OF_CONDUCT.md)

## 🌟 Show Your Support

Give a ⭐️ if this project helped you!

## 🔗 Related Projects

- [Jakarta EE Platform](https://jakarta.ee/)
- [Next.js Documentation](https://nextjs.org/docs)
- [GlassFish Server](https://glassfish.org/)

---

**Version**: 1.0.0  
**Last Updated**: December 19, 2025  
**Status**: Production Ready
