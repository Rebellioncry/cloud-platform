#!/bin/sh
taos -s "CREATE DATABASE IF NOT EXISTS iot_db KEEP 365 DURATION 30 COMP 2 REPLICA 1 PRECISION 'ms' CACHEMODEL 'both';"
taos -s "CREATE STABLE IF NOT EXISTS iot_db.device_telemetry (ts TIMESTAMP, \`value\` VARCHAR(1024), value_type NCHAR(32), quality NCHAR(16)) TAGS (device_id VARCHAR(64), product_key VARCHAR(20), property_id VARCHAR(64), device_name VARCHAR(100), product_name VARCHAR(100));"
taos -s "CREATE STABLE IF NOT EXISTS iot_db.device_log (ts TIMESTAMP, log_type NCHAR(32), content VARCHAR(4096), message_id VARCHAR(64)) TAGS (device_id VARCHAR(64), product_key VARCHAR(20), device_name VARCHAR(100));"
