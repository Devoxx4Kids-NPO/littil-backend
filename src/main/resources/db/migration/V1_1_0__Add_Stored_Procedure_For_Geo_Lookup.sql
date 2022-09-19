DELIMITER //

CREATE FUNCTION GCDist(
    _lat1 DOUBLE, -- Scaled Degrees north for one point
    _lon1 DOUBLE, -- Scaled Degrees west for one point
    _lat2 DOUBLE, -- other point
    _lon2 DOUBLE
) RETURNS DOUBLE
    DETERMINISTIC
    CONTAINS SQL -- SQL but does not read or write
    SQL SECURITY INVOKER -- No special privileges granted
-- Input is a pair of latitudes/longitudes multiplied by 10000.
--    For example, the south pole has latitude -900000.
-- Multiply output by .0069172 to get miles between the two points
--    or by .0111325 to get kilometers
BEGIN
    -- Hardcoded constant:
    DECLARE _deg2rad DOUBLE DEFAULT PI() / 1800000; -- For scaled by 1e4 to MEDIUMINT
    DECLARE _rlat1 DOUBLE DEFAULT _deg2rad * _lat1;
    DECLARE _rlat2 DOUBLE DEFAULT _deg2rad * _lat2;
    -- compute as if earth's radius = 1.0
    DECLARE _rlond DOUBLE DEFAULT _deg2rad * (_lon1 - _lon2);
    DECLARE _m DOUBLE DEFAULT COS(_rlat2);
    DECLARE _x DOUBLE DEFAULT COS(_rlat1) - _m * COS(_rlond);
    DECLARE _y DOUBLE DEFAULT _m * SIN(_rlond);
    DECLARE _z DOUBLE DEFAULT SIN(_rlat1) - SIN(_rlat2);
    DECLARE _n DOUBLE DEFAULT SQRT(
                    _x * _x +
                    _y * _y +
                    _z * _z);
    RETURN 2 * ASIN(_n / 2) / _deg2rad; -- again--scaled degrees
END;
//
DELIMITER ;

DELIMITER //
CREATE PROCEDURE FindNearest(
    IN _my_lat DOUBLE, -- Latitude of me [-90..90] (not scaled)
    IN _my_lon DOUBLE, -- Longitude [-180..180]
    IN _START_dist DOUBLE, -- Starting estimate of how far to search: miles or km
    IN _max_dist DOUBLE, -- Limit how far to search: miles or km
    IN _limit INT, -- How many items to try to get
    IN _condition VARCHAR(1111) -- will be ANDed in a WHERE clause
)
    DETERMINISTIC
BEGIN
    -- lat and lng are in degrees -90..+90 and -180..+180
    -- All computations done in Latitude degrees.
    -- Thing to tailor
    --   *location* -- the table
    --   Scaling of lat, lon; here using *10000 in MEDIUMINT
    --   Table name
    --   miles versus km.

    -- Hardcoded constant:
    DECLARE _deg2rad DOUBLE DEFAULT PI() / 1800000;
    -- For scaled by 1e4 to MEDIUMINT

    -- Cannot use params in PREPARE, so switch to @variables:
    -- Hardcoded constant:
    SET @my_lat := _my_lat * 10000,
        @my_lon := _my_lon * 10000,
        @deg2dist := 0.0111325, -- 69.172 for miles; 111.325 for km  *** (mi vs km)
        @start_deg := _start_dist / @deg2dist, -- Start with this radius first (eg, 15 miles)
        @max_deg := _max_dist / @deg2dist,
        @cutoff := @max_deg / SQRT(2), -- (slightly pessimistic)
        @dlat := @start_deg, -- note: must stay positive
        @lon2lat := COS(_deg2rad * @my_lat),
        @iterations := 0;
    -- just debugging

    -- Loop through, expanding search
    --   Search a 'square', repeat with bigger square until find enough rows
    --   If the inital probe found _limit rows, then probably the first
    --   iteration here will find the desired data.
    -- Hardcoded table name:
    -- This is the "first SELECT":
    SET @sql = CONCAT(
            "SELECT COUNT(*) INTO @near_ct
                FROM location
                WHERE latitude    BETWEEN @my_lat - @dlat
                                 AND @my_lat + @dlat   -- PARTITION Pruning and bounding box
                  AND longitude    BETWEEN @my_lon - @dlon
                                 AND @my_lon + @dlon   -- first part of PK
                  AND ", _condition);
    PREPARE _sql FROM @sql;
    MainLoop:
    LOOP
        SET @iterations := @iterations + 1;
        -- The main probe: Search a 'square'
        SET @dlon := ABS(@dlat / @lon2lat);
        -- good enough for now  -- note: must stay positive
        -- Hardcoded constants:
        SET @dlon := IF(ABS(@my_lat) + @dlat >= 900000, 3600001, @dlon); -- near a Pole
        EXECUTE _sql;
        IF (@near_ct >= _limit OR -- Found enough
            @dlat >= @cutoff) THEN -- Give up (too far)
            LEAVE MainLoop;
        END IF;
        -- Expand 'square':
        SET @dlat := LEAST(2 * @dlat, @cutoff); -- Double the radius to search
    END LOOP MainLoop;
    DEALLOCATE PREPARE _sql;

    -- Out of loop because found _limit items, or going too far.
    -- Expand range by about 1.4 (but not past _max_dist),
    -- then fetch details on nearest 10.

    -- Hardcoded constant:
    SET @dlat := IF(@dlat >= @max_deg OR @dlon >= 1800000,
                    @max_deg,
                    GCDist(ABS(@my_lat), @my_lon,
                           ABS(@my_lat) - @dlat, @my_lon - @dlon));
    -- ABS: go toward equator to find farthest corner (also avoids poles)
    -- Dateline: not a problem (see GCDist code)

    -- Reach for longitude line at right angle:
    -- sin(dlon)*cos(lat) = sin(dlat)
    -- Hardcoded constant:
    SET @dlon := IFNULL(ASIN(SIN(_deg2rad * @dlat) /
                             COS(_deg2rad * @my_lat))
                            / _deg2rad -- precise
        , 3600001);
    -- must be too near a pole

    -- This is the "last SELECT":
    -- Hardcoded constants:
    IF (ABS(@my_lon) + @dlon < 1800000 OR -- Usual case - not crossing dateline
        ABS(@my_lat) + @dlat < 900000) THEN -- crossing pole, so dateline not an issue
    -- Hardcoded table name:
        SET @sql = CONCAT(
                "SELECT *,
                        @deg2dist * GCDist(@my_lat, @my_lon, latitude, longitude) AS dist
                    FROM location
                    WHERE latitude BETWEEN @my_lat - @dlat
                                  AND @my_lat + @dlat   -- PARTITION Pruning and bounding box
                      AND longitude BETWEEN @my_lon - @dlon
                                  AND @my_lon + @dlon   -- first part of PK
                      AND ", _condition, "
                HAVING dist <= ", _max_dist, "
                ORDER BY dist
                LIMIT ", _limit
            );
    ELSE
        -- Hardcoded constants and table name:
        -- Circle crosses dateline, do two SELECTs, one for each side
        SET @west_lon := IF(@my_lon < 0, @my_lon, @my_lon - 3600000);
        SET @east_lon := @west_lon + 3600000;
        -- One of those will be beyond +/- 180; this gets points beyond the dateline
        SET @sql = CONCAT(
                "( SELECT *,
                        @deg2dist * GCDist(@my_lat, @west_lon, latitude, longitude) AS dist
                    FROM location
                    WHERE latitude BETWEEN @my_lat - @dlat
                                  AND @my_lat + @dlat   -- PARTITION Pruning and bounding box
                      AND longitude BETWEEN @west_lon - @dlon
                                  AND @west_lon + @dlon   -- first part of PK
                      AND ", _condition, "
                HAVING dist <= ", _max_dist, " )
            UNION ALL
            ( SELECT *,
                    @deg2dist * GCDist(@my_lat, @east_lon, latitude, longitude) AS dist
                FROM location
                WHERE latitude BETWEEN @my_lat - @dlat
                              AND @my_lat + @dlat   -- PARTITION Pruning and bounding box
                  AND longitude BETWEEN @east_lon - @dlon
                              AND @east_lon + @dlon   -- first part of PK
                  AND ", _condition, "
                HAVING dist <= ", _max_dist, " )
            ORDER BY dist
            LIMIT ", _limit
            );
    END IF;

    PREPARE _sql FROM @sql;
    EXECUTE _sql;
    DEALLOCATE PREPARE _sql;
END;
//
DELIMITER ;