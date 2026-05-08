-- Users creation statements
-- Grant all privileges to the application user
CREATE USER IF NOT EXISTS 'bender'@'%' IDENTIFIED BY 'your_password';

GRANT ALL PRIVILEGES ON bender_bot.* TO 'bender'@'%';

FLUSH PRIVILEGES;
