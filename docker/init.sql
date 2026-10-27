-- one database + one user per service (no cross-service table access)
CREATE USER auth_user WITH PASSWORD 'auth_pw';              CREATE DATABASE auth_db OWNER auth_user;
CREATE USER ancestry_user WITH PASSWORD 'ancestry_pw';      CREATE DATABASE ancestry_db OWNER ancestry_user;
CREATE USER certificate_user WITH PASSWORD 'certificate_pw'; CREATE DATABASE certificate_db OWNER certificate_user;
CREATE USER validation_user WITH PASSWORD 'validation_pw';  CREATE DATABASE validation_db OWNER validation_user;
