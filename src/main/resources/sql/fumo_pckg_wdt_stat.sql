DROP TABLE IF EXISTS "fumo_pckg_wdt_stat";

CREATE TABLE "public"."fumo_pckg_wdt_stat" (
  "ID" BIGSERIAL PRIMARY KEY,
  "VER_ID" varchar(30),
  "BRAND" varchar(30),
  "PCKG_NM" varchar(255),
  "VER_ALIAS" varchar(255),
  "PCKG_ST" varchar(30),
  "TGT_VER" varchar(255),
  "MS_TYPE" varchar(30),
  "SW_CNT" int4,
  "SW_SZ" varchar(50),
  "DV_CNT" int4,
  "WDT_ST" varchar(50),
  "CREATED_AT" date DEFAULT CURRENT_DATE,
  "LINK_YN" varchar(1) DEFAULT 'N',
  "LINK_TIME" timestamp(6)
);