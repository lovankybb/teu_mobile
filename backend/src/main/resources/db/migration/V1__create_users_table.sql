-- V1__create_users_table.sql
-- Khởi tạo bảng users quản lý hồ sơ người dùng đồng bộ từ Keycloak
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'users' AND schema_id = SCHEMA_ID('dbo'))
BEGIN
    CREATE TABLE dbo.users (
        id BIGINT IDENTITY(1,1) NOT NULL,
        keycloak_id VARCHAR(64) NOT NULL,
        username VARCHAR(100) NOT NULL,
        email VARCHAR(150) NOT NULL,
        full_name NVARCHAR(150) NULL,
        phone_number VARCHAR(20) NULL,
        address NVARCHAR(300) NULL,
        avatar_url VARCHAR(500) NULL,
        is_active BIT NOT NULL CONSTRAINT DF_users_is_active DEFAULT 1,
        created_at DATETIME2(3) NOT NULL CONSTRAINT DF_users_created_at DEFAULT SYSUTCDATETIME(),
        updated_at DATETIME2(3) NOT NULL CONSTRAINT DF_users_updated_at DEFAULT SYSUTCDATETIME(),
        CONSTRAINT PK_users PRIMARY KEY CLUSTERED (id),
        CONSTRAINT UQ_users_keycloak_id UNIQUE (keycloak_id),
        CONSTRAINT UQ_users_email UNIQUE (email),
        CONSTRAINT UQ_users_username UNIQUE (username)
    );

    CREATE NONCLUSTERED INDEX IX_users_keycloak_id ON dbo.users (keycloak_id);
    CREATE NONCLUSTERED INDEX IX_users_email ON dbo.users (email);
END;
GO
