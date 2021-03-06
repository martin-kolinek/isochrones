drop table if exists partition_vis;
create table partition_vis(
    node_id bigint,
    region int,
    geom geometry(Geometry, 4326)
);

insert into partition_vis (node_id, region, geom)
    select n.id, p.region, n.geom from road_nodes p inner join nodes n on n.id = p.id;
