drop table if exists higher_edge_vis;
drop table if exists higher_node_vis;

create table higher_node_vis(
    id bigint,
    geom geometry(Geometry, 4326)
);

insert into higher_node_vis(id, geom)
    select n.id, n.geom from higher_nodes h inner join nodes n on n.id = h.id;

create table higher_edge_vis(
    geom geometry(Geometry, 4326)
);

insert into higher_edge_vis(geom)
    select ST_MakeLine(sn.geom, en.geom) from higher_edges e
            inner join nodes sn on sn.id = e.start_node 
            inner join nodes en on en.id = e.end_node;
