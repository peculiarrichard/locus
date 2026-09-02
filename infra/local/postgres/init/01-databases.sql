-- Local dev bootstrap: one least-privilege owner-user and one database per backend service.

CREATE USER auth_service WITH PASSWORD 'auth_service_local';
CREATE DATABASE auth_db OWNER auth_service;

CREATE USER session_service WITH PASSWORD 'session_service_local';
CREATE DATABASE session_db OWNER session_service;

CREATE USER distraction_service WITH PASSWORD 'distraction_service_local';
CREATE DATABASE distraction_db OWNER distraction_service;

CREATE USER goal_service WITH PASSWORD 'goal_service_local';
CREATE DATABASE goal_db OWNER goal_service;

CREATE USER analytics_service WITH PASSWORD 'analytics_service_local';
CREATE DATABASE analytics_db OWNER analytics_service;

CREATE USER accountability_service WITH PASSWORD 'accountability_service_local';
CREATE DATABASE accountability_db OWNER accountability_service;

CREATE USER notification_service WITH PASSWORD 'notification_service_local';
CREATE DATABASE notification_db OWNER notification_service;
