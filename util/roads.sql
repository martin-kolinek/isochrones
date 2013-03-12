drop table roads;
drop table road_net;
drop table road_net_vis;

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
    and (tags -> 'highway') != 'pedestrian';

create table road_net (
    start_node bigint,
    end_node bigint,
    region int
);

insert into road_net (start_node, end_node, region)
    select c.node_id, n.node_id, 0 from way_nodes c 
        inner join way_nodes n on n.sequence_id = c.sequence_id + 1 and n.way_id = c.way_id 
        inner join roads r on r.id = n.way_id
            where (r.tags -> 'oneway') is null or (r.tags -> 'oneway') != '-1';

insert into road_net (start_node, end_node, region)
    select n.node_id, c.node_id, 0 from way_nodes c 
        inner join way_nodes n on n.sequence_id = c.sequence_id + 1 and n.way_id = c.way_id 
        inner join roads r on r.id = n.way_id
            where ((r.tags -> 'oneway') is null)
                and (r.tags -> 'highway') != 'motorway' 
                and (r.tags -> 'highway') != 'motorway_link' 
                and ((r.tags -> 'junction') is null or (r.tags -> 'junction')!='roundabout') 
                or (r.tags -> 'oneway') = '-1'
                or (r.tags -> 'oneway') = 'no';

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
        where prn.direction = 1 or prn.start_node<prn.end_node
            


