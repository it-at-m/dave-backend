--
-- PostgreSQL database dump
--

-- Dumped from database version 16.4
-- Dumped by pg_dump version 16.4

-- Started on 2024-09-26 15:54:19

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- TOC entry 6 (class 2615 OID 16527)
-- Name: dave; Type: SCHEMA; Schema: -; Owner: dave
--

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- TOC entry 225 (class 1259 OID 16528)
-- Name: chatmessage; Type: TABLE; Schema: dave; Owner: dave
--

CREATE TABLE dave.chatmessage (
    id character varying(36) NOT NULL,
    created_time timestamp without time zone NOT NULL,
    version bigint,
    content character varying(32600),
    participant_id integer,
    "timestamp" timestamp(6) without time zone,
    type character varying(255),
    uploaded boolean,
    viewed boolean,
    zaehlung_id character varying(36) NOT NULL
);


ALTER TABLE dave.chatmessage OWNER TO dave;

--
-- TOC entry 226 (class 1259 OID 16535)
-- Name: dienstleister; Type: TABLE; Schema: dave; Owner: dave
--

CREATE TABLE dave.dienstleister (
    id character varying(36) NOT NULL,
    created_time timestamp without time zone NOT NULL,
    version bigint,
    active boolean,
    kennung character varying(255),
    name character varying(255)
);


ALTER TABLE dave.dienstleister OWNER TO dave;

--
-- TOC entry 227 (class 1259 OID 16542)
-- Name: dienstleister_emailaddresses; Type: TABLE; Schema: dave; Owner: dave
--

CREATE TABLE dave.dienstleister_emailaddresses (
    dienstleister_id character varying(36) NOT NULL,
    email_addresses character varying(255)
);


ALTER TABLE dave.dienstleister_emailaddresses OWNER TO dave;

--
-- TOC entry 228 (class 1259 OID 16545)
-- Name: emailaddress; Type: TABLE; Schema: dave; Owner: dave
--

CREATE TABLE dave.emailaddress (
    id character varying(36) NOT NULL,
    created_time timestamp without time zone NOT NULL,
    version bigint,
    email_address character varying(255),
    participant_id integer
);


ALTER TABLE dave.emailaddress OWNER TO dave;

--
-- TOC entry 229 (class 1259 OID 16550)
-- Name: hochrechnungsfaktor; Type: TABLE; Schema: dave; Owner: dave
--

CREATE TABLE dave.hochrechnungsfaktor (
    id character varying(36) NOT NULL,
    created_time timestamp without time zone NOT NULL,
    version bigint,
    active boolean,
    default_faktor boolean,
    gv double precision,
    kfz double precision,
    matrix character varying(255),
    sv double precision
);


ALTER TABLE dave.hochrechnungsfaktor OWNER TO dave;

--
-- TOC entry 230 (class 1259 OID 16555)
-- Name: infomessage; Type: TABLE; Schema: dave; Owner: dave
--

CREATE TABLE dave.infomessage (
    id character varying(36) NOT NULL,
    created_time timestamp without time zone NOT NULL,
    version bigint,
    aktiv boolean,
    content character varying(32600),
    gueltig_bis timestamp(6) without time zone,
    gueltig_von timestamp(6) without time zone
);


ALTER TABLE dave.infomessage OWNER TO dave;

--
-- TOC entry 231 (class 1259 OID 16562)
-- Name: pkweinheit; Type: TABLE; Schema: dave; Owner: dave
--

CREATE TABLE dave.pkweinheit (
    id character varying(36) NOT NULL,
    created_time timestamp without time zone NOT NULL,
    version bigint,
    busse numeric(38,2),
    fahrradfahrer numeric(38,2),
    kraftraeder numeric(38,2),
    lastzuege numeric(38,2),
    lkw numeric(38,2),
    pkw numeric(38,2)
);


ALTER TABLE dave.pkweinheit OWNER TO dave;

--
-- TOC entry 232 (class 1259 OID 16567)
-- Name: shedlock; Type: TABLE; Schema: dave; Owner: dave
--

CREATE TABLE dave.shedlock (
    name character varying(255) NOT NULL,
    lock_until timestamp(6) without time zone NOT NULL,
    locked_at timestamp(6) without time zone NOT NULL,
    locked_by character varying(255) NOT NULL
);


ALTER TABLE dave.shedlock OWNER TO dave;

--
-- TOC entry 233 (class 1259 OID 16574)
-- Name: zeitintervall; Type: TABLE; Schema: dave; Owner: dave
--

CREATE TABLE dave.zeitintervall (
    id character varying(36) NOT NULL,
    created_time timestamp without time zone NOT NULL,
    version bigint,
    busse integer,
    endeuhrzeit timestamp(6) without time zone NOT NULL,
    fahrbeziehung_fahrbewegungkreisverkehr character varying(255),
    fahrbeziehung_nach integer,
    fahrbeziehung_von integer,
    fahrbeziehung_id character varying(36),
    fahrradfahrer integer,
    fussgaenger integer,
    hochrechnung_faktorgv numeric(38,2),
    hochrechnung_faktorkfz numeric(38,2),
    hochrechnung_faktorsv numeric(38,2),
    hochrechnung_hochrechnunggv numeric(38,2),
    hochrechnung_hochrechnungkfz numeric(38,2),
    hochrechnungrad integer,
    hochrechnung_hochrechnungsv numeric(38,2),
    kraftraeder integer,
    lastzuege integer,
    lkw integer,
    pkw integer,
    sortingindex integer,
    startuhrzeit timestamp(6) without time zone NOT NULL,
    type character varying(255),
    zaehlung_id character varying(36) NOT NULL,
    CONSTRAINT zeitintervall_fahrbeziehung_fahrbewegungkreisverkehr_check CHECK (((fahrbeziehung_fahrbewegungkreisverkehr)::text = ANY ((ARRAY['HINEIN'::character varying, 'HERAUS'::character varying, 'VORBEI'::character varying])::text[]))),
    CONSTRAINT zeitintervall_type_check CHECK (((type)::text = ANY ((ARRAY['BLOCK'::character varying, 'BLOCK_SPEZIAL'::character varying, 'GESAMT'::character varying, 'GESAMT_KI'::character varying, 'SPITZENSTUNDE_KFZ'::character varying, 'SPITZENSTUNDE_RAD'::character varying, 'SPITZENSTUNDE_FUSS'::character varying, 'STUNDE_VIERTEL'::character varying, 'STUNDE_HALB'::character varying, 'STUNDE_KOMPLETT'::character varying])::text[])))
);


ALTER TABLE dave.zeitintervall OWNER TO dave;


--
-- TOC entry 4696 (class 2606 OID 16534)
-- Name: chatmessage chatmessage_pkey; Type: CONSTRAINT; Schema: dave; Owner: dave
--

ALTER TABLE ONLY dave.chatmessage
    ADD CONSTRAINT chatmessage_pkey PRIMARY KEY (id);


--
-- TOC entry 4698 (class 2606 OID 16541)
-- Name: dienstleister dienstleister_pkey; Type: CONSTRAINT; Schema: dave; Owner: dave
--

ALTER TABLE ONLY dave.dienstleister
    ADD CONSTRAINT dienstleister_pkey PRIMARY KEY (id);


--
-- TOC entry 4700 (class 2606 OID 16549)
-- Name: emailaddress emailaddress_pkey; Type: CONSTRAINT; Schema: dave; Owner: dave
--

ALTER TABLE ONLY dave.emailaddress
    ADD CONSTRAINT emailaddress_pkey PRIMARY KEY (id);


--
-- TOC entry 4702 (class 2606 OID 16554)
-- Name: hochrechnungsfaktor hochrechnungsfaktor_pkey; Type: CONSTRAINT; Schema: dave; Owner: dave
--

ALTER TABLE ONLY dave.hochrechnungsfaktor
    ADD CONSTRAINT hochrechnungsfaktor_pkey PRIMARY KEY (id);


--
-- TOC entry 4708 (class 2606 OID 16561)
-- Name: infomessage infomessage_pkey; Type: CONSTRAINT; Schema: dave; Owner: dave
--

ALTER TABLE ONLY dave.infomessage
    ADD CONSTRAINT infomessage_pkey PRIMARY KEY (id);


--
-- TOC entry 4710 (class 2606 OID 16566)
-- Name: pkweinheit pkweinheit_pkey; Type: CONSTRAINT; Schema: dave; Owner: dave
--

ALTER TABLE ONLY dave.pkweinheit
    ADD CONSTRAINT pkweinheit_pkey PRIMARY KEY (id);


--
-- TOC entry 4712 (class 2606 OID 16573)
-- Name: shedlock shedlock_pkey; Type: CONSTRAINT; Schema: dave; Owner: dave
--

ALTER TABLE ONLY dave.shedlock
    ADD CONSTRAINT shedlock_pkey PRIMARY KEY (name);


--
-- TOC entry 4706 (class 2606 OID 16586)
-- Name: hochrechnungsfaktor uk60cvh9h36xnd1d3vpluylr2po; Type: CONSTRAINT; Schema: dave; Owner: dave
--

ALTER TABLE ONLY dave.hochrechnungsfaktor
    ADD CONSTRAINT unique_dave_hochrechnungsfaktor_matrix UNIQUE (matrix);


--
-- TOC entry 4718 (class 2606 OID 16582)
-- Name: zeitintervall zeitintervall_pkey; Type: CONSTRAINT; Schema: dave; Owner: dave
--

ALTER TABLE ONLY dave.zeitintervall
    ADD CONSTRAINT zeitintervall_pkey PRIMARY KEY (id);


--
-- TOC entry 4703 (class 1259 OID 16583)
-- Name: index_active; Type: INDEX; Schema: dave; Owner: dave
--

CREATE INDEX index_active ON dave.hochrechnungsfaktor USING btree (active);


--
-- TOC entry 4713 (class 1259 OID 16588)
-- Name: index_combined_1; Type: INDEX; Schema: dave; Owner: dave
--

CREATE INDEX index_combined_1 ON dave.zeitintervall USING btree (zaehlung_id, type, fahrbeziehung_von, fahrbeziehung_nach);


--
-- TOC entry 4714 (class 1259 OID 16589)
-- Name: index_combined_2; Type: INDEX; Schema: dave; Owner: dave
--

CREATE INDEX index_combined_2 ON dave.zeitintervall USING btree (zaehlung_id, startuhrzeit, endeuhrzeit, fahrbeziehung_von, type);


--
-- TOC entry 4715 (class 1259 OID 16590)
-- Name: index_combined_3; Type: INDEX; Schema: dave; Owner: dave
--

CREATE INDEX index_combined_3 ON dave.zeitintervall USING btree (zaehlung_id, startuhrzeit, endeuhrzeit, fahrbeziehung_von, fahrbeziehung_nach, fahrbeziehung_fahrbewegungkreisverkehr, type);


--
-- TOC entry 4704 (class 1259 OID 16584)
-- Name: index_default_faktor; Type: INDEX; Schema: dave; Owner: dave
--

CREATE INDEX index_default_faktor ON dave.hochrechnungsfaktor USING btree (default_faktor);


--
-- TOC entry 4716 (class 1259 OID 16587)
-- Name: index_zaehlung; Type: INDEX; Schema: dave; Owner: dave
--

CREATE INDEX index_zaehlung ON dave.zeitintervall USING btree (zaehlung_id);


--
-- TOC entry 4719 (class 2606 OID 16591)
-- Name: dienstleister_emailaddresses fk2iw1ggeetum6vfgr1jfpo8gsg; Type: FK CONSTRAINT; Schema: dave; Owner: dave
--

ALTER TABLE ONLY dave.dienstleister_emailaddresses
    ADD CONSTRAINT dave_dienstleister_id_fkey FOREIGN KEY (dienstleister_id) REFERENCES dave.dienstleister(id);


-- Completed on 2024-09-26 15:54:20

--
-- PostgreSQL database dump complete
--