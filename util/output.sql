BEGIN TRANSACTION;

DELETE FROM output_vis;

INSERT INTO output_vis(node_id, distance, geom)
    SELECT n.id, o.distance, n.geom from output o inner join nodes n on n.id=o.node;

COMMIT TRANSACTION;
