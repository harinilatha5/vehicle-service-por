# Vehicle Service & Management Portal — Spring Boot + CI/CD

A REST API for managing customers, their vehicles, and vehicle service records, with a complete
GitHub Actions CI/CD pipeline that builds, tests, dockerizes, and deploys the app.

## Tech Stack
- Java 17, Spring Boot 3.3 (Web, Data JPA, Validation, Actuator)
- H2 (default/local/CI) and MySQL (docker/production) profiles
- Maven
- Docker + Docker Compose
- Jenkins (CI/CD)

## API Endpoints

| Resource | Endpoint |
|---|---|
| Customers | `GET/POST /api/customers`, `GET/PUT/DELETE /api/customers/{id}` |
| Vehicles | `GET /api/vehicles`, `GET/PUT/DELETE /api/vehicles/{id}`, `GET/POST /api/customers/{customerId}/vehicles` |
| Service Records | `GET /api/services`, `GET/PUT/DELETE /api/services/{id}`, `GET/POST /api/vehicles/{vehicleId}/services`, `PATCH /api/services/{id}/status?status=COMPLETED` |

---

## 1. Run locally (no Docker, uses in-memory H2)

```bash
mvn clean install
mvn spring-boot:run
```
App runs at `http://localhost:8080`. H2 console at `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:vehicleportal`).

## 2. Run with Docker Compose (MySQL)

```bash
docker-compose up --build
```
This starts a MySQL container and the app container. App runs at `http://localhost:8080`.

---

## 3. Push this project to GitHub — step by step

```bash
# 1. Move into the project folder
cd vehicle-service-portal

# 2. Initialize git (skip if already a repo)
git init
git branch -M main

# 3. Add all files and make the first commit
git add .
git commit -m "Initial commit: Vehicle Service & Management Portal"

# 4. Create a new empty repository on GitHub first (via github.com -> New repository)
#    Do NOT initialize it with a README on GitHub's side. Then link it:
git remote add origin https://github.com/<your-username>/<your-repo-name>.git

# 5. Push
git push -u origin main
```

For subsequent changes:
```bash
git add .
git commit -m "your message"
git push
```

---

## 4. Set up CI/CD (Jenkins + Docker)

The pipeline is defined in the included `Jenkinsfile` with 6 stages: **Checkout → Build → Test → Package → Docker Build → Docker Push → Deploy**. The Deploy stage SSHes into your app server, runs MySQL and the app as two Docker containers on a shared Docker network, and restarts the app container on every build.

### 4.1 Install Jenkins (on a server — can be the same server as the app, or a separate one)

```bash
ssh your_user@jenkins_server_ip

sudo apt update
sudo apt install -y openjdk-17-jdk

curl -fsSL https://pkg.jenkins.io/debian-stable/jenkins.io-2023.key | sudo tee \
  /usr/share/keyrings/jenkins-keyring.asc > /dev/null
echo "deb [signed-by=/usr/share/keyrings/jenkins-keyring.asc] \
  https://pkg.jenkins.io/debian-stable binary/" | sudo tee \
  /etc/apt/sources.list.d/jenkins.list > /dev/null

sudo apt update
sudo apt install -y jenkins
sudo systemctl enable --now jenkins
```

Jenkins runs on port `8081` by default in this setup to avoid clashing with the app's `8080` — if not, edit `/etc/default/jenkins` and set `HTTP_PORT=8081`, then `sudo systemctl restart jenkins`.

Open `http://jenkins_server_ip:8081` in your browser, unlock it with:
```bash
sudo cat /var/lib/jenkins/secrets/initialAdminPassword
```
Install the **suggested plugins**, then also install from **Manage Jenkins → Plugins**: `Docker Pipeline`, `SSH Agent`, `Git`.

### 4.2 Give Jenkins access to Docker and Maven

```bash
sudo usermod -aG docker jenkins
curl -fsSL https://get.docker.com | sh   # if Docker isn't installed yet
sudo apt install -y maven
sudo systemctl restart jenkins
```

### 4.3 Add Jenkins credentials

In Jenkins: **Manage Jenkins → Credentials → System → Global credentials → Add Credentials**. Add three:

| Kind | ID (must match exactly) | Value |
|---|---|---|
| Username with password | `dockerhub-creds` | your Docker Hub username + access token |
| SSH Username with private key | `server-ssh-key` | the SSH username and private key that can log into your app server |
| Secret text | `db-password` | your MySQL root password, e.g. `Vehicle@2026Secure` |

To get a Docker Hub access token: hub.docker.com → Account Settings → Security → New Access Token.

### 4.4 Edit the Jenkinsfile for your server

Open `Jenkinsfile` and update these two lines to match your app server:
```groovy
SERVER_USER = 'ubuntu'          // your server's SSH user
SERVER_HOST = '203.0.113.10'    // your server's IP or domain
```
Commit and push this change.

### 4.5 Create the Jenkins pipeline job

1. Jenkins dashboard → **New Item** → name it `vehicle-service-portal` → choose **Pipeline** → OK.
2. Under **Pipeline**, set **Definition** to `Pipeline script from SCM`.
3. **SCM**: Git → paste your repo URL (`https://github.com/<user>/<repo>.git`).
4. If the repo is private, add a Git credential (username + GitHub personal access token).
5. **Script Path**: `Jenkinsfile` (default, already correct).
6. Save.

### 4.6 Trigger builds automatically on git push

Easiest option — **poll SCM**: in the job config, under **Build Triggers**, check **Poll SCM** and set schedule `H/2 * * * *` (checks every 2 minutes).

For instant triggers instead: install the **GitHub plugin**, then in your GitHub repo → **Settings → Webhooks → Add webhook** → Payload URL: `http://jenkins_server_ip:8081/github-webhook/`, content type `application/json`, event: `Just the push event`. Then in the Jenkins job, enable **GitHub hook trigger for GITScm polling**.

### 4.7 Run it

Click **Build Now** on the job, or just push to `main`:
```bash
git add .
git commit -m "trigger ci/cd"
git push
```
Watch progress under **Build History → Console Output** in Jenkins.

---

## 5. Deployment options

### Option A — Your own Linux server (EC2 / DigitalOcean / VPS) — matches the included workflow
1. Provision a Linux server, install Docker: `curl -fsSL https://get.docker.com | sh`
2. Open port 8080 (and 22 for SSH) in the firewall/security group.
3. Add the `SERVER_HOST`, `SERVER_USER`, `SERVER_SSH_KEY` secrets (section 4.2).
4. Push to `main` — GitHub Actions will SSH in, pull the image, and run the container automatically.

### Option B — Render.com / Railway.app (no server management, beginner-friendly)
1. Create an account and a new **Web Service** from your GitHub repo.
2. Choose "Docker" as the build environment — it will detect the `Dockerfile` automatically.
3. Set environment variables in their dashboard (`SPRING_PROFILES_ACTIVE=mysql`, DB credentials, or use their managed Postgres/MySQL add-on).
4. Every push to `main` auto-deploys (you can skip the `deploy` job in the workflow, or remove it, since these platforms deploy on push automatically).

### Option C — Kubernetes
The same Docker image can be deployed to any Kubernetes cluster — ask if you'd like a `deployment.yaml` + `service.yaml` added.

---

## 6. Project structure

```
vehicle-service-portal/
├── src/main/java/com/vehicleportal/
│   ├── VehicleServicePortalApplication.java
│   ├── model/            (Customer, Vehicle, ServiceRecord)
│   ├── repository/       (Spring Data JPA repositories)
│   ├── service/          (business logic)
│   ├── controller/       (REST endpoints)
│   └── exception/        (global error handling)
├── src/main/resources/
│   ├── application.properties
│   ├── application-h2.properties
│   └── application-mysql.properties
├── src/test/java/...     (context-load test)
├── Dockerfile
├── docker-compose.yml
├── Jenkinsfile
└── pom.xml
```
