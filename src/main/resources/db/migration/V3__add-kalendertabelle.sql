CREATE TABLE kalendertag (
                             datum DATE NOT NULL UNIQUE,
                             tagestyp INTEGER NOT NULL,
                             created_time TIMESTAMP NOT NULL,
                             VERSION bigint,
                             ID VARCHAR(36) NOT NULL,
                             feiertag VARCHAR(255),
                             ferientyp VARCHAR(255),
                             PRIMARY KEY (ID)
)