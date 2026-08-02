# Azure CLI Cheatsheet

Replace the placeholders:

- <resource-group
- <webapp-name
- <postgres-server

---

# ==========================
# App Service (Backend)
# ==========================

## Start Web App

```powershell
az webapp start `
  --resource-group <resource-group> `
  --name <webapp-name>
```

## Stop Web App

```powershell
az webapp stop `
  --resource-group <resource-group> `
  --name <webapp-name>
```

## Restart Web App

```powershell
az webapp restart `
  --resource-group <resource-group> `
  --name <webapp-name>
```

## Check Web App Status

```powershell
az webapp show `
  --resource-group <resource-group> `
  --name <webapp-name> `
  --query state `
  --output tsv
```

---

# ==========================
# PostgreSQL Flexible Server
# ==========================

## Start Database

```powershell
az postgres flexible-server start `
  --resource-group <resource-group> `
  --name <postgres-server>
```

## Stop Database

```powershell
az postgres flexible-server stop `
  --resource-group <resource-group> `
  --name <postgres-server>
```

## Check Database Status

```powershell
az postgres flexible-server show `
  --resource-group <resource-group> `
  --name <postgres-server> `
  --query state `
  --output tsv
```

---

# ==========================
# App Service Plan
# ==========================

## Show App Service Plan

```powershell
az appservice plan show `
  --resource-group <resource-group> `
  --name <plan-name>
```

---

# ==========================
# Resource Information
# ==========================

## List Web Apps

```powershell
az webapp list `
  --resource-group <resource-group> `
  --output table
```

## List PostgreSQL Servers

```powershell
az postgres flexible-server list `
  --resource-group <resource-group> `
  --output table
```

---

# ==========================
# Container Logs
# ==========================

## Stream Logs

```powershell
az webapp log tail `
  --resource-group <resource-group> `
  --name <webapp-name>
```

## Enable Container Logging

```powershell
az webapp log config `
  --resource-group <resource-group> `
  --name <webapp-name> `
  --docker-container-logging filesystem
```

---

# ==========================
# App Settings
# ==========================

## Show App Settings

```powershell
az webapp config appsettings list `
  --resource-group <resource-group> `
  --name <webapp-name>
```

## Set App Settings

```powershell
az webapp config appsettings set `
  --resource-group <resource-group> `
  --name <webapp-name> `
  --settings KEY=VALUE
```

---

# ==========================
# Container Configuration
# ==========================

## Show Container Configuration

```powershell
az webapp config container show `
  --resource-group <resource-group> `
  --name <webapp-name>
```

## Configure Container Image

```powershell
az webapp config container set `
  --resource-group <resource-group> `
  --name <webapp-name> `
  --container-image-name <image> `
  --container-registry-url <registry-url>
```

---

# ==========================
# Database Information
# ==========================

## List Databases

```powershell
az postgres flexible-server db list `
  --resource-group <resource-group> `
  --server-name <postgres-server> `
  --output table
```

## Show Server Information

```powershell
az postgres flexible-server show `
  --resource-group <resource-group> `
  --name <postgres-server>
```

---

# ==========================
# Subscription
# ==========================

## Current Subscription

```powershell
az account show `
  --query "{Name:name,State:state}" `
  --output table
```
