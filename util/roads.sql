begin transaction;

drop table if exists roads;
drop table if exists road_net;
drop table if exists road_net_vis;
drop table if exists road_net_undir;
drop table if exists road_net_undir_vis;
drop table if exists road_nodes;
drop table if exists higher_nodes;
drop table if exists higher_edges;
drop table if exists higher_regions;
drop table if exists road_regions;
DROP TABLE IF EXISTS output;
DROP TABLE IF EXISTS output_vis;

--roads table - contains ways which are roads

create table roads (
    id bigint primary key,
    tags hstore,
    linestring geometry(Geometry, 4326)
);

insert into roads (id, tags, linestring) select id, tags, linestring from ways where (tags -> 'highway') is not null 
    and (tags -> 'highway') != 'path'
    and (tags -> 'highway') != 'cycleway'
    and (tags -> 'highway') != 'footway'
    and (tags -> 'highway') != 'bridleway'
    and (tags -> 'highway') != 'steps'
    and (tags -> 'highway') != 'pedestrian'
    and (tags -> 'highway') != 'proposed';

--road_net table - contains separate edges from roads table

create temporary table road_net_bare on commit drop as 
    select c.node_id as start_node, n.node_id as end_node from way_nodes c 
        inner join way_nodes n on n.sequence_id = c.sequence_id + 1 and n.way_id = c.way_id 
        inner join roads r on r.id = n.way_id
            where (r.tags -> 'oneway') is null or (r.tags -> 'oneway') != '-1';

insert into road_net_bare (start_node, end_node)
    select n.node_id, c.node_id from way_nodes c 
        inner join way_nodes n on n.sequence_id = c.sequence_id + 1 and n.way_id = c.way_id 
        inner join roads r on r.id = n.way_id
            where ((r.tags -> 'oneway') is null)
                and (r.tags -> 'highway') != 'motorway' 
                and (r.tags -> 'highway') != 'motorway_link' 
                and ((r.tags -> 'junction') is null or (r.tags -> 'junction')!='roundabout') 
                or (r.tags -> 'oneway') = '-1'
                or (r.tags -> 'oneway') = 'no';

create table road_net (
    start_node bigint,
    end_node bigint,
    cost double precision
);
insert into road_net(start_node, end_node, cost)
    select sn.id, en.id, ST_Distance(sn.geom::geography, en.geom::geography) 
        from road_net_bare rn 
            inner join nodes sn on sn.id = rn.start_node
            inner join nodes en on en.id = rn.end_node;

--road_net_undir - undirected version of road_net

create table road_net_undir (
    start_node bigint,
    end_node bigint,
    cost double precision
);
insert into road_net_undir(start_node, end_node, cost)
    select start_node, end_node, cost from road_net;
delete from road_net_undir where exists(select * from road_net_undir rn2 where rn2.start_node=road_net_undir.end_node and rn2.end_node=road_net_undir.start_node) 
        and start_node<end_node;
insert into road_net_undir(start_node, end_node, cost)
    select end_node, start_node, cost from road_net_undir;

--road_nodes table - contains nodes which define roads

create table road_nodes (
    id bigint,
    region int
);
insert into road_nodes(id, region)
    select distinct q.id, 0 from (select start_node as id from road_net union select end_node as id from road_net) q;

--higher level tables

create table higher_nodes (
    id bigint,
    region int
);

create table higher_edges (
    start_node bigint,
    end_node bigint,
    cost double precision
);

create table road_regions (
    id int,
    diameter double precision
);

create table higher_regions (
    id int,
    diameter double precision
);


--output table - contains output isochrone
         
create table output (
    node bigint,
    distance double precision
);
        
CREATE TABLE output_vis (
    node_id bigint,
    distance double precision,
    geom geometry(Point, 4326)
);


    
--road_net_vis table - contains information for visualizing road_net

create table road_net_vis (
    start_node bigint,
    end_node bigint,
    direction int,
    linestring geometry(Geometry, 4326)
);

insert into road_net_vis(start_node, end_node, direction, linestring)
    select sn.id, en.id, prn.direction, ST_MakeLine(sn.geom, en.geom) 
        from (select distinct rn.start_node, rn.end_node, case coalesce(rn2.start_node, 1) when 1 then 1 else 0 end as direction 
                from road_net rn 
                    left join road_net rn2 on rn2.start_node=rn.end_node and rn2.end_node=rn.start_node) prn
            inner join nodes sn on sn.id = prn.start_node 
            inner join nodes en on en.id = prn.end_node
        where prn.direction = 1 or prn.start_node<prn.end_node;

create index road_net_vis_geo_idx on road_net_vis using GIST (linestring);

--road_net_undir_vis

create table road_net_undir_vis (
    start_node bigint,
    end_node bigint,
    direction int,
    linestring geometry(Geometry, 4326)
);

insert into road_net_undir_vis(start_node, end_node, direction, linestring)
    select sn.id, en.id, prn.direction, ST_MakeLine(sn.geom, en.geom) 
        from (select distinct rn.start_node, rn.end_node, case coalesce(rn2.start_node, 1) when 1 then 1 else 0 end as direction 
                from road_net_undir rn 
                    left join road_net_undir rn2 on rn2.start_node=rn.end_node and rn2.end_node=rn.start_node) prn
            inner join nodes sn on sn.id = prn.start_node 
            inner join nodes en on en.id = prn.end_node
        where prn.direction = 1 or prn.start_node<prn.end_node;

create index road_net_undir_vis_geo_idx on road_net_undir_vis using GIST (linestring);
       
            
commit transaction;
analyze;

      
