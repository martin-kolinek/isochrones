BEGIN TRANSACTION;

DROP TABLE IF EXISTS output_vis;

CREATE TABLE output_vis (
    node_id bigint,
    distance double precision,
    geom geometry(Point, 4326)
);

INSERT INTO output_vis(node_id, distance, geom)
    SELECT n.id, o.distance, n.geom from output o inner join nodes n on n.id=o.node;

COMMIT TRANSACTION;
