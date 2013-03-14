CREATE OR REPLACE FUNCTION utmzone(geometry)
   RETURNS integer AS
 $BODY$
 DECLARE
     geomgeog geometry;
     zone int;
     pref int;

 BEGIN
     geomgeog:= ST_Transform($1,4326);

     IF (ST_Y(geomgeog))>0 THEN
        pref:=32600;
     ELSE
        pref:=32700;
     END IF;

     zone:=floor((ST_X(geomgeog)+180)/6)+1;

     RETURN zone+pref;
 END;
 $BODY$ LANGUAGE 'plpgsql' IMMUTABLE
   COST 100;

CREATE OR REPLACE FUNCTION st_createarc(startpoint geometry, endpoint geometry, arcenter geometry, direction text) RETURNS geometry
    AS $$
  DECLARE
    cwpointonarc geometry;
    ccpointonarc geometry;
    ccarc text;
    cwarc text;
    cwdirection float;
    ccdirection float;
    midpointrads float;
    arcenterutm geometry;
    startpointutm geometry;
    endpointutm geometry;
    thearc geometry;
    majorarc text;
    minorarc text;
  BEGIN
        arcenterutm := st_transform(arcenter,utmzone(arcenter));
        startpointutm := st_transform(startpoint,utmzone(arcenter));
        endpointutm := st_transform(endpoint,utmzone(arcenter));

        midpointrads := abs(st_azimuth(arcenterutm,startpointutm) - st_azimuth(arcenterutm,endpointutm));
        IF midpointrads > pi() THEN midpointrads:= (midpointrads - pi())/2; ELSE midpointrads:= midpointrads/2; END IF;
        IF midpointrads > st_azimuth(arcenterutm,startpointutm) THEN midpointrads:= st_azimuth(arcenterutm,startpointutm)/2; END IF;
        IF midpointrads > st_azimuth(arcenterutm,endpointutm) THEN midpointrads:= st_azimuth(arcenterutm,endpointutm)/2; END IF;
        cwdirection := -1*midpointrads;
ccdirection := midpointrads;
cwpointonarc := ST_Translate( ST_Rotate( ST_Translate( startpointutm, -1*ST_X(arcenterutm), -1*ST_Y(arcenterutm)), cwdirection), ST_X(arcenterutm), ST_Y(arcenterutm));
ccpointonarc := ST_Translate( ST_Rotate( ST_Translate( startpointutm, -1*ST_X(arcenterutm), -1*ST_Y(arcenterutm)), ccdirection), ST_X(arcenterutm), ST_Y(arcenterutm));
        cwarc := 'CIRCULARSTRING('||ST_X(startpointutm)||' '||ST_Y(startpointutm)||','||ST_X(cwpointonarc)||' '||ST_Y(cwpointonarc)||','||ST_X(endpointutm)||' '||ST_Y(endpointutm)||')';
ccarc := 'CIRCULARSTRING('||ST_X(startpointutm)||' '||ST_Y(startpointutm)||','||ST_X(ccpointonarc)||' '||ST_Y(ccpointonarc)||','||ST_X(endpointutm)||' '||ST_Y(endpointutm)||')';
IF st_length(st_curvetoline(cwarc)) > st_length(st_curvetoline(ccarc)) THEN majorarc := cwarc; minorarc := ccarc; ELSE majorarc := ccarc; minorarc := cwarc; END IF;
IF direction = 'major' THEN RETURN st_transform(st_setsrid(st_curvetoline(majorarc),utmzone(arcenter)),st_srid(arcenter)); ELSE IF direction = 'minor' THEN RETURN st_transform(st_setsrid(st_curvetoline(minorarc),utmzone(arcenter)),st_srid(arcenter)); END IF; END IF;
        IF direction = 'cw' THEN RETURN st_transform(st_setsrid(st_curvetoline(cwarc),utmzone(arcenter)),st_srid(arcenter)); ELSE IF direction = 'cc' THEN RETURN st_transform(st_setsrid(st_curvetoline(ccarc),utmzone(arcenter)),st_srid(arcenter)); END IF; END IF;
  END;

  $$
  LANGUAGE 'plpgsql' IMMUTABLE;
  COMMENT ON FUNCTION st_createarc(geometry,geometry,geometry,text) IS 'Generates an arc based on starting point, ending point, centre of arc, and direction (clockwise: cw, conter-clockwise: cc, or size major or minor). All geometries must be of type POINT and this function returns a linestring of the arc based on the original SRID and 32 points per quarter circle. Requires the utmzone function.'; 






