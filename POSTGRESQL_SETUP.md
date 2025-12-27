# PostgreSQL Setup Guide

## Vấn đề hiện tại
Lỗi: `password authentication failed for user "pgsql"`

## Giải pháp

### 1. Kiểm tra PostgreSQL đã được cài đặt chưa

```bash
# Windows
psql --version

# Hoặc kiểm tra service
Get-Service postgresql*
```

### 2. Kết nối PostgreSQL với user mặc định

```bash
# Windows PowerShell
psql -U postgres

# Hoặc nếu có password
psql -U postgres -h localhost
```

### 3. Tạo database và user (nếu chưa có)

Kết nối PostgreSQL và chạy các lệnh sau:

```sql
-- Tạo database
CREATE DATABASE auth_shop;

-- Tạo user (nếu chưa có)
CREATE USER pgsql WITH PASSWORD '123';

-- Hoặc nếu muốn dùng user postgres mặc định, chỉ cần set password:
ALTER USER postgres WITH PASSWORD '123';

-- Cấp quyền cho user
GRANT ALL PRIVILEGES ON DATABASE auth_shop TO pgsql;
-- Hoặc nếu dùng postgres:
GRANT ALL PRIVILEGES ON DATABASE auth_shop TO postgres;
```

### 4. Cấu hình application.properties

**Option 1: Dùng user `postgres` (khuyến nghị cho development)**

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/auth_shop
spring.datasource.username=postgres
spring.datasource.password=123
```

**Option 2: Dùng user `pgsql` (sau khi đã tạo)**

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/auth_shop
spring.datasource.username=pgsql
spring.datasource.password=123
```

### 5. Kiểm tra kết nối

Test kết nối bằng psql:

```bash
# Với user postgres
psql -U postgres -d auth_shop -h localhost

# Với user pgsql
psql -U pgsql -d auth_shop -h localhost
```

Nếu kết nối thành công, bạn sẽ thấy prompt: `auth_shop=#`

### 6. Kiểm tra pg_hba.conf (nếu vẫn lỗi)

File này nằm ở:
- Windows: `C:\Program Files\PostgreSQL\<version>\data\pg_hba.conf`
- Linux: `/etc/postgresql/<version>/main/pg_hba.conf`

Đảm bảo có dòng:
```
host    all             all             127.0.0.1/32            md5
```

Sau đó restart PostgreSQL service.

### 7. Environment Variables (nếu cần override)

Nếu bạn muốn dùng environment variables:

**Windows PowerShell:**
```powershell
$env:DB_URL = "jdbc:postgresql://localhost:5432/auth_shop"
$env:DB_USERNAME = "postgres"
$env:DB_PASSWORD = "123"
```

**Windows CMD:**
```cmd
set DB_URL=jdbc:postgresql://localhost:5432/auth_shop
set DB_USERNAME=postgres
set DB_PASSWORD=123
```

**Linux/Mac:**
```bash
export DB_URL=jdbc:postgresql://localhost:5432/auth_shop
export DB_USERNAME=postgres
export DB_PASSWORD=123
```

## Quick Fix - Dùng user postgres mặc định

Nếu bạn muốn nhanh chóng, chỉ cần sửa `application.properties`:

```properties
spring.datasource.username=postgres
spring.datasource.password=<your_postgres_password>
```

Thay `<your_postgres_password>` bằng password của user postgres trên máy bạn.

## Troubleshooting

### Lỗi: "role pgsql does not exist"
→ User chưa được tạo. Tạo user hoặc dùng user `postgres`.

### Lỗi: "database auth_shop does not exist"
→ Database chưa được tạo. Chạy: `CREATE DATABASE auth_shop;`

### Lỗi: "password authentication failed"
→ Password không đúng. Kiểm tra lại password hoặc reset:
```sql
ALTER USER postgres WITH PASSWORD 'new_password';
```

### Lỗi: "connection refused"
→ PostgreSQL service chưa chạy. Start service:
```bash
# Windows
net start postgresql-x64-<version>

# Linux
sudo systemctl start postgresql
```

