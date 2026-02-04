CREATE TABLE "configuration"
(
    "id" character varying(36) NOT NULL,
    "created_time" timestamp without time zone NOT NULL,
    "version" bigint,
    "keyname" VARCHAR(255) NOT NULL,
	"valuefield" VARCHAR(1024) NOT NULL,
	"category" VARCHAR(255) NOT NULL,
    "datatype" VARCHAR(50) NOT NULL,
    CONSTRAINT "configuration_unique_key" UNIQUE("keyname"),
    CONSTRAINT "configuration_pkey" PRIMARY KEY ("id")
);
