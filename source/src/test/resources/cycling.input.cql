-- KEYSPACE

CREATE KEYSPACE IF NOT EXISTS cycling WITH REPLICATION = { 'class' : 'NetworkTopologyStrategy', 'datacenter1' : 3 };

-- TABLE

CREATE TABLE cycling.cyclist_alt_stats ( id UUID PRIMARY KEY, lastname text, birthday timestamp, nationality text, weight text, height text );

CREATE TABLE cycling.whimsey ( id UUID PRIMARY KEY, lastname text, cyclist_teams set<text>, events list<text>, teams map<int,text> );

CREATE TABLE cyclist_mv (cid UUID PRIMARY KEY, name text, age int, birthday date, country text);

CREATE TABLE cycling.route (race_id int, race_name text, point_id int, lat_long tuple<text, tuple<float,float>>, PRIMARY KEY (race_id, point_id));

-- INDEX
CREATE TABLE cycling.rank_by_year_and_name (
  race_year int,
  race_name text,
  cyclist_name text,
  rank int,
  PRIMARY KEY ((race_year, race_name), rank)
);

CREATE INDEX ryear ON cycling.rank_by_year_and_name (race_year);
CREATE INDEX rrank ON cycling.rank_by_year_and_name (rank);
-- MATERIALIZED VIEW

CREATE MATERIALIZED VIEW cycling.cyclist_by_age
AS SELECT age, birthday, name, country
FROM cyclist_mv
WHERE age IS NOT NULL AND cid IS NOT NULL
PRIMARY KEY (age, cid);

CREATE MATERIALIZED VIEW cycling.cyclist_by_country
AS SELECT age,birthday, name, country
FROM cyclist_mv
WHERE country IS NOT NULL AND cid IS NOT NULL
PRIMARY KEY (country, cid);
CREATE MATERIALIZED VIEW cycling.cyclist_by_birthday
AS SELECT age, birthday, name, country
FROM cyclist_mv
WHERE birthday IS NOT NULL AND cid IS NOT NULL
PRIMARY KEY (birthday, cid);

-- TABLE WITH SET TYPE
CREATE TABLE cycling.cyclist_career_teams ( id UUID PRIMARY KEY, lastname text, teams set<text> );

-- TABLE WITH LIST TYPE
 CREATE TABLE cycling.upcoming_calendar ( year int, month int, events list<text>, PRIMARY KEY ( year, month) );

-- TABLE WITH MAP TYPE
CREATE TABLE cycling.cyclist_teams ( id UUID PRIMARY KEY, lastname text, firstname text, teams map<int,text> );

-- TABLE WITH MAP TUPLE TYPE
CREATE TABLE cycling.route (race_id int, race_name text, point_id int, lat_long tuple<text, tuple<float,float>>, PRIMARY KEY (race_id, point_id));

CREATE TABLE cycling.nation_rank ( nation text PRIMARY KEY, info tuple<int,text,int> );

CREATE TABLE cycling.popular (rank int PRIMARY KEY, cinfo tuple<text,text,int> );

-- USER DEFINED TYPE

CREATE TYPE cycling.basic_info (
  birthday timestamp,
  nationality text,
  weight text,
  height text
);

CREATE TABLE cycling.cyclist_stats ( id uuid PRIMARY KEY, lastname text, basics FROZEN<basic_info>);

CREATE TYPE cycling.race (race_title text, race_date timestamp, race_time text);

CREATE TABLE cycling.cyclist_races ( id UUID PRIMARY KEY, lastname text, firstname text, races list<FROZEN <race>> );

-- FUNCTION

CREATE OR REPLACE FUNCTION cycling.fLog (input double) CALLED ON NULL INPUT RETURNS double LANGUAGE java AS 'return Double.valueOf(Math.log(input.doubleValue()));';

-- USER DEFINED FUNCTION
-- state function

CREATE OR REPLACE FUNCTION cycling.avgState ( state tuple<int,bigint>, val int ) CALLED ON NULL INPUT RETURNS tuple<int,bigint> LANGUAGE java AS
  'if (val !=null) { state.setInt(0, state.getInt(0)+1); state.setLong(1, state.getLong(1)+val.intValue()); } return state;';

-- final function
CREATE OR REPLACE FUNCTION cycling.avgFinal ( state tuple<int,bigint> ) CALLED ON NULL INPUT RETURNS double LANGUAGE java AS
  'double r = 0; if (state.getInt(0) == 0) return null; r = state.getLong(1); r/= state.getInt(0); return Double.valueOf(r);';

-- USER DEFINED AGGREGATE FUNCTION
--  aggregate function using these two functions
CREATE AGGREGATE IF NOT EXISTS cycling.average ( int ) SFUNC avgState STYPE tuple<int,bigint> FINALFUNC avgFinal INITCOND (0,0);