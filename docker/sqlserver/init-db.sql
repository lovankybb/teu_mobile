-- Script kiểm tra và khởi tạo Database teu_mobile_db nếu chưa tồn tại
IF DB_ID(N'teu_mobile_db') IS NULL
BEGIN
    PRINT 'Đang tạo Database teu_mobile_db...';
    CREATE DATABASE [teu_mobile_db]
    COLLATE SQL_Latin1_General_CP1_CI_AS;
    PRINT 'Tạo Database teu_mobile_db thành công!';
END
ELSE
BEGIN
    PRINT 'Database teu_mobile_db đã tồn tại.';
END
GO
