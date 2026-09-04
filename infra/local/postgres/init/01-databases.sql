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

-- Postgres grants CONNECT on every database to PUBLIC by default, so without this every one of
-- the 7 service users above could still open a connection to (and enumerate the schema of) every
-- other service's database, even though table-level grants already block reading actual rows —
-- found during Phase 12's security review. Table ownership already implies CONNECT for the owner,
-- so revoking PUBLIC's grant per database is sufficient; no per-owner GRANT needed.
REVOKE CONNECT ON DATABASE auth_db FROM PUBLIC;
REVOKE CONNECT ON DATABASE session_db FROM PUBLIC;
REVOKE CONNECT ON DATABASE distraction_db FROM PUBLIC;
REVOKE CONNECT ON DATABASE goal_db FROM PUBLIC;
REVOKE CONNECT ON DATABASE analytics_db FROM PUBLIC;
REVOKE CONNECT ON DATABASE accountability_db FROM PUBLIC;
REVOKE CONNECT ON DATABASE notification_db FROM PUBLIC;
