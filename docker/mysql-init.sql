-- MySQL Remote Access Configuration
-- Allow root to connect remotely

ALTER USER 'root'@'%' IDENTIFIED WITH mysql_native_password BY 'root123';
FLUSH PRIVILEGES;
