drop table if exists intersecting_pairs;
     
drop table if exists intersecting_vis;
        
create table intersecting_pairs(s_node1 bigint, e_node1 bigint, s_node2 bigint, e_node2 bigint);
       
create table intersecting_vis(line geometry(Linestring, 4326));
       
CREATE OR REPLACE FUNCTION ST_GetIntersections(double precision, double precision, double precision, double precision) returns table(start1 bigint, end1 bigint, start2 bigint, end2 bigint) as $$
select r1.start_node, r1.end_node, r2.start_node, r2.end_node from road_net_vis r1 
       inner join road_net_vis r2 on ST_Intersects(r1.linestring, r2.linestring) 
             and r1.start_node!=r2.end_node 
             and r1.end_node!=r2.end_node 
             and r1.start_node!=r2.start_node 
             and r1.end_node!=r2.start_node 
       where ST_Intersects(r1.linestring, ST_SetSRID(ST_MakeBox2D(ST_SetSRID(ST_Point($1, $2), 4326), ST_SetSRID(ST_Point($3, $4), 4326)), 4326)) and
       ST_Intersects(r2.linestring, ST_SetSRID(ST_MakeBox2D(ST_SetSRID(ST_Point($1, $2), 4326), ST_SetSRID(ST_Point($3, $4), 4326)), 4326));
$$ LANGUAGE SQL
