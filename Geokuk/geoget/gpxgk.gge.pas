//GeoGet 2

function ExportExtension: string;
begin
  result := 'GPX';
end;

function ExportDescription: string;
begin
  result := 'GeoKuk GPX (bez listingu a logu)';
end;

function ExportHeader: string;
begin
  Result := '<?xml version="1.0" encoding="utf-8"?>' + CRLF;
  Result := Result + '<gpx xmlns:xsd="http://www.w3.org/2001/XMLSchema"'
    + ' xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" version="1.0"'
    + ' creator="Groundspeak Pocket Query"'
    + ' xsi:schemaLocation="http://www.topografix.com/GPX/1/0 http://www.topografix.com/GPX/1/0/gpx.xsd'
    + ' http://www.groundspeak.com/cache/1/0 http://www.groundspeak.com/cache/1/0/cache.xsd"'
    + ' xmlns="http://www.topografix.com/GPX/1/0">' + CRLF;
  Result := Result + ' <desc>Geocache file</desc>' + CRLF;
  Result := Result + ' <author>' + Geoget_Version + '</author>' + CRLF;
  Result := Result + ' <time>' + formatdatetime('yyyy"-"mm"-"dd"T"hh":"nn":"ss"."zzz', now) + '</time>' + CRLF;
  Result := Result + ' <keywords>cache, geocache, groundspeak</keywords>' + CRLF + CRLF;
end;

function ExportFooter: string;
begin
  result := '</gpx>' + CRLF;
end;

function ExportPoint: string;
var
  s: string;
  n: integer;
begin
  Result := '';
  //Export for Geocaches
  if GC.IsListed then
  begin
    
    s := ''
    if (GC.URL = '') and (pos('MU',GC.ID) = 1) then
    begin
        s := Geoget_DB.GetTableString(
        'SELECT gtv.value FROM geocache gc, geotag gt, geotagcategory gtc, geotagvalue gtv '
        +'WHERE gt.id="'+GC.ID+'" and gtc.key=gt.ptrkat and gtc.value="DeployedOrder" and gtv.key=gt.ptrvalue');
        s := 'http://www.munzee.com/m/'+ReplaceString(s,';','/')+'/';
    end;
  
    Result := Result + ' <wpt lat="' + GC.Lat + '" lon="' + GC.Lon + '">' + CRLF;
    Result := Result + '  <time>'
      + formatdatetime('yyyy"-"mm"-"dd"T"hh":"nn":"ss"."zzz', GC.Hidden) + '</time>' + CRLF;
    Result := Result + '  <name>' + GC.ID +'</name>' + CRLF;
    Result := Result + '  <desc>' + cdata(GC.Name + ' by ' + GC.Author
          + ' (' + GC.Difficulty + '/' + GC.Terrain + ')') +'</desc>' + CRLF;
    if s <> '' then
        Result := Result + '  <url>' + cdata(s) +'</url>' + CRLF
    else
        Result := Result + '  <url>' + cdata(GC.URL) +'</url>' + CRLF;
    Result := Result + '  <urlname>' + cdata(GC.Name + ' by ' + GC.Author) + '</urlname>' + CRLF;
    if GC.IsFound then
      Result := Result + '  <sym>Geocache Found</sym>' + CRLF
    else
      Result := Result + '  <sym>Geocache</sym>' + CRLF;
    Result := Result + '  <type>Geocache|' + GC.CacheType + '</type>' + CRLF;
    if GC.IsDisabled then
      s := 'available="False"'
    else
      s := 'available="True"';
    if GC.IsArchived then
      s := s + ' archived="True"'
    else
      s := s + ' archived="False"';
    s := 'id="' + GC.CacheID +'" ' + s;
    Result := Result + '  <groundspeak:cache ' + s + ' xmlns:groundspeak="http://www.groundspeak.com/cache/1/0">' + CRLF;
    Result := Result + '   <groundspeak:name>' + cdata(GC.Name) + '</groundspeak:name>' + CRLF;
    Result := Result + '   <groundspeak:placed_by>' + cdata(GC.Author) +'</groundspeak:placed_by>' + CRLF;
    if GC.OwnerID <> '' then
      Result := Result + '   <groundspeak:owner id="' + GC.OwnerID + '">' + cdata(GC.Author) +'</groundspeak:owner>' + CRLF;
    Result := Result + '   <groundspeak:type>' + GC.CacheType +'</groundspeak:type>' + CRLF;
    Result := Result + '   <groundspeak:container>' + GC.Size +'</groundspeak:container>' + CRLF;
    Result := Result + '   <groundspeak:difficulty>' + GC.Difficulty +'</groundspeak:difficulty>' + CRLF;
    Result := Result + '   <groundspeak:terrain>' + GC.Terrain +'</groundspeak:terrain>' + CRLF;
    Result := Result + '   <groundspeak:country>' + GC.Country +'</groundspeak:country>' + CRLF;
    Result := Result + '   <groundspeak:state>' + GC.State +'</groundspeak:state>' + CRLF;
    Result := Result + '   <groundspeak:short_description html="True">' + cdata(GC.ShortDescription) +'</groundspeak:short_description>' + CRLF;
    Result := Result + '   <groundspeak:encoded_hints>' + cdata(GC.Hint) +'</groundspeak:encoded_hints>' + CRLF;
    Result := Result + '  </groundspeak:cache>' + CRLF;
    Result := Result + ' </wpt>' + CRLF + CRLF;
  end;
  //Export for Waypoints
  for n := 0 to GC.Waypoints.Count - 1 do
    if GC.Waypoints[n].IsListed then
    begin
      Result := Result + ' <wpt lat="' + GC.Waypoints[n].Lat + '" lon="' + GC.Waypoints[n].Lon + '">' + CRLF;
      Result := Result + '  <time>'
        + formatdatetime('yyyy"-"mm"-"dd"T"hh":"nn":"ss"."zzz', GC.Hidden) + '</time>' + CRLF;
      Result := Result + '  <name>' + GC.Waypoints[n].ID +'</name>' + CRLF;
      Result := Result + '  <cmt>' + cdata(GC.Waypoints[n].Description) +'</cmt>' + CRLF;
      Result := Result + '  <desc>' + cdata(GC.Waypoints[n].Name) +'</desc>' + CRLF;
      Result := Result + '  <url>' + cdata(GC.Waypoints[n].URL) +'</url>' + CRLF;
      Result := Result + '  <urlname>' + cdata(GC.Waypoints[n].Name) + '</urlname>' + CRLF;
      Result := Result + '  <sym>' + GC.Waypoints[n].WptType + '</sym>' + CRLF
      Result := Result + '  <type>Waypoint|' + GC.Waypoints[n].WptType + '</type>' + CRLF;
      Result := Result + ' </wpt>' + CRLF + CRLF;
    end;
end;
