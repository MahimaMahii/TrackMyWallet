# 🚀 Render Deployment Guide for TrackMyWallet (Docker)

This guide provides step-by-step instructions for deploying the **TrackMyWallet** Spring Boot application to **Render** using Docker.

---

## 📌 Deployment Overview

| Setting | Value |
|---|---|
| **Platform** | Render (Web Service) |
| **Runtime** | Docker |
| **Repository** | `https://github.com/MahimaMahii/TrackMyWallet.git` |
| **Branch** | `main` |
| **Region** | **Singapore** (`singapore`) |
| **Dockerfile Path** | `./Dockerfile` |
| **Docker Context** | `.` |
| **Health Check Path** | `/api/health` |
| **Base Image** | `eclipse-temurin:21-jdk-jammy` (Build) / `eclipse-temurin:21-jre-jammy` (Runtime) |
| **Executable JAR** | `trackmywallet-0.0.1-SNAPSHOT.jar` |

---

## 🗄️ Remote MySQL Database Setup

Render does not host free-tier managed MySQL natively (it offers PostgreSQL). For MySQL 8.0, you can use any cloud-hosted MySQL provider that offers a free or low-cost tier:

### Recommended Cloud MySQL Providers (Free Tier):
1. **Aiven for MySQL** (Free tier, offers managed MySQL with Singapore region)
2. **TiDB Cloud Serverless** (Free 5GB MySQL-compatible cluster)
3. **Clever Cloud MySQL** (Free tier)
4. **Railway / AWS RDS MySQL**

### Database Schema Initialization:
Once your remote database instance is created:
1. Ensure a database named `trackmywallet` exists (or create it: `CREATE DATABASE trackmywallet;`).
2. The application is configured with `spring.jpa.hibernate.ddl-auto=${DDL_AUTO:update}`. On the first deployment boot, Hibernate will automatically create all tables (`users`, `categories`, `expenses`, `budgets`) and constraints.

---

## 🔑 Environment Variables Required on Render

In the Render Dashboard under **Environment**:

| Variable Name | Example / Format | Description |
|---|---|---|
| `PORT` | `8080` (or leave default, Render injects this) | HTTP port the container listens on |
| `DB_URL` | `jdbc:mysql://<host>:<port>/trackmywallet?useSSL=true&allowPublicKeyRetrieval=true&serverTimezone=UTC` | Full JDBC connection URL to remote MySQL |
| `DB_USER` | `avnadmin` (or your cloud DB username) | MySQL username |
| `DB_PASSWORD` | `<your-cloud-db-password>` | MySQL password (securely stored) |
| `DDL_AUTO` | `update` | Safe Hibernate schema sync |
| `SHOW_SQL` | `false` | Keeps production logs clean and fast |

> [!IMPORTANT]
> **Do not use `localhost` in `DB_URL` on Render.**
> `localhost` inside a Docker container refers to the container itself, not your remote database. Always use the public host/domain provided by your cloud MySQL host.

---

## 🛠️ Step-by-Step Render Deployment Instructions

### Method A: Using Render Web Service (Manual Setup)

1. **Push Changes to GitHub:**
   ```powershell
   git add .
   git commit -m "Configure Dockerfile, health check, and Render deployment setup"
   git push origin main
   ```

2. **Log in to Render:**
   - Go to [https://dashboard.render.com](https://dashboard.render.com).

3. **Create a New Web Service:**
   - Click **New +** → **Web Service**.
   - Select **Build and deploy from a Git repository**.
   - Connect and select `MahimaMahii/TrackMyWallet`.

4. **Configure Web Service Settings:**
   - **Name:** `trackmywallet`
   - **Region:** **Singapore** (as requested)
   - **Branch:** `main`
   - **Root Directory:** (leave empty / `.`)
   - **Runtime:** **Docker**
   - **Dockerfile Path:** `./Dockerfile`
   - **Instance Type:** `Free` (or `Starter`)

5. **Configure Health Check:**
   - Click **Advanced Settings**.
   - **Health Check Path:** `/api/health`

6. **Add Environment Variables:**
   Under **Environment Variables**, add:
   - `DB_URL`: `jdbc:mysql://<remote-host>:<port>/trackmywallet?useSSL=true&allowPublicKeyRetrieval=true&serverTimezone=UTC`
   - `DB_USER`: `<your-db-username>`
   - `DB_PASSWORD`: `<your-db-password>`

7. **Deploy:**
   - Click **Create Web Service**.
   - Render will clone the repository, run the multi-stage Docker build, cache Maven layers, package the JAR, start the container, and perform health checks at `/api/health`.
   - Once healthy, your web application will be live at `https://trackmywallet.onrender.com`.

---

### Method B: Using Render Blueprint (`render.yaml`)

Because we have added [`render.yaml`](file:///c:/Users/madhu/Downloads/TrackMyWallet-main/TrackMyWallet-main/render.yaml):
1. In the Render Dashboard, click **New +** → **Blueprint**.
2. Connect your `TrackMyWallet` repository.
3. Render automatically reads `render.yaml` and sets the Singapore region, Docker runtime, health check path, and prompts you to fill in `DB_URL`, `DB_USER`, and `DB_PASSWORD`.
4. Click **Apply**.

---

## 🛡️ Production & Security Considerations

1. **HTTPS & Reverse Proxy Support:**
   - Configured `server.forward-headers-strategy=framework` in [`application.properties`](file:///c:/Users/madhu/Downloads/TrackMyWallet-main/TrackMyWallet-main/src/main/resources/application.properties).
   - This ensures Tomcat correctly handles Render's `X-Forwarded-Proto: https` header and sets session cookies with appropriate attributes.

2. **Session Cookie Security:**
   - `server.servlet.session.cookie.http-only=true` blocks client-side scripts from reading session cookies.
   - `server.servlet.session.cookie.same-site=lax` mitigates CSRF while allowing seamless navigation.

3. **Container Memory Optimization:**
   - `JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"` dynamically adapts Java heap memory to the container's RAM limit (e.g. 512MB on Render free tier).

4. **Dedicated Health Endpoint:**
   - `/api/health` validates database connectivity and is accessible without an authentication session so Render probes succeed.
