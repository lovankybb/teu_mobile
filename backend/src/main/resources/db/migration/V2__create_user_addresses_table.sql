-- V2__create_user_addresses_table.sql
-- Khởi tạo bảng user_addresses quản lý sổ địa chỉ nhận hàng của người dùng
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'user_addresses' AND schema_id = SCHEMA_ID('dbo'))
BEGIN
    CREATE TABLE dbo.user_addresses (
        id BIGINT IDENTITY(1,1) NOT NULL,
        user_id BIGINT NOT NULL,
        recipient_name NVARCHAR(150) NOT NULL,
        phone_number VARCHAR(20) NOT NULL,
        province NVARCHAR(100) NOT NULL,
        district NVARCHAR(100) NOT NULL,
        ward NVARCHAR(100) NOT NULL,
        street_address NVARCHAR(255) NOT NULL,
        full_address NVARCHAR(500) NOT NULL,
        address_type VARCHAR(20) NOT NULL CONSTRAINT DF_user_addresses_address_type DEFAULT 'HOME',
        is_default BIT NOT NULL CONSTRAINT DF_user_addresses_is_default DEFAULT 0,
        notes NVARCHAR(255) NULL,
        created_at DATETIME2(3) NOT NULL CONSTRAINT DF_user_addresses_created_at DEFAULT SYSUTCDATETIME(),
        updated_at DATETIME2(3) NOT NULL CONSTRAINT DF_user_addresses_updated_at DEFAULT SYSUTCDATETIME(),
        CONSTRAINT PK_user_addresses PRIMARY KEY CLUSTERED (id),
        CONSTRAINT FK_user_addresses_users FOREIGN KEY (user_id) REFERENCES dbo.users (id) ON DELETE CASCADE
    );

    CREATE NONCLUSTERED INDEX IX_user_addresses_user_id ON dbo.user_addresses (user_id);
    CREATE NONCLUSTERED INDEX IX_user_addresses_user_is_default ON dbo.user_addresses (user_id, is_default);
END;
GO
