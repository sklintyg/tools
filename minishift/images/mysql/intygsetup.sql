-- REHABSTOD
CREATE USER 'rehabstod'@'localhost' IDENTIFIED BY 'rehabstod';
GRANT ALL PRIVILEGES ON *.* TO 'rehabstod'@'localhost' IDENTIFIED BY 'rehabstod' WITH GRANT OPTION;
FLUSH PRIVILEGES;
CREATE DATABASE rehabstod;


-- WEBCERT
CREATE USER 'webcert'@'localhost' IDENTIFIED BY 'webcert';
GRANT ALL PRIVILEGES ON *.* TO 'webcert'@'localhost' IDENTIFIED BY 'webcert' WITH GRANT OPTION;
FLUSH PRIVILEGES;
CREATE DATABASE webcert;


-- INTYGSTJANST
CREATE USER 'intyg'@'localhost' IDENTIFIED BY 'intyg';
GRANT ALL PRIVILEGES ON *.* TO 'intyg'@'localhost' IDENTIFIED BY 'intyg' WITH GRANT OPTION;
FLUSH PRIVILEGES;
CREATE DATABASE intyg;


-- SRS
CREATE USER 'srs'@'localhost' IDENTIFIED BY 'srs';
GRANT ALL PRIVILEGES ON *.* TO 'srs'@'localhost' IDENTIFIED BY 'srs' WITH GRANT OPTION;
FLUSH PRIVILEGES;
CREATE DATABASE srs;


-- PRIVATLAKARPORTAL
CREATE USER 'privatlakarportal'@'localhost' IDENTIFIED BY 'privatlakarportal';
GRANT ALL PRIVILEGES ON *.* TO 'privatlakarportal'@'localhost' IDENTIFIED BY 'privatlakarportal' WITH GRANT OPTION;
FLUSH PRIVILEGES;
CREATE DATABASE privatlakarportal;


-- STATISTIK
CREATE USER 'statistik'@'localhost' IDENTIFIED BY 'statistik';
GRANT ALL PRIVILEGES ON *.* TO 'statistik'@'localhost' IDENTIFIED BY 'statistik' WITH GRANT OPTION;
FLUSH PRIVILEGES;
CREATE DATABASE statistik;
