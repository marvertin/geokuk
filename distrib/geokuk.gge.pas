//GEOVEV export
const
  TAG_EVAL = 'Hodnoceni';
  TAG_EVALCOUNT = 'Hodnoceni-Pocet';
  TAG_BESTOF = 'BestOf';
  TAG_ZNAMKA = 'Znamka';

function ExportExtension: string;
begin
  result := 'geokuk';
end;

function ExportDescription: string;
begin
  result := 'GeoKuk export';
end;

function ExportHeader: string;
begin
  Result := '*geokuk:exportversion=2' + CRLF;
end;

function ExportFooter: string;
begin
  Result := '';
end;

function ExportAfter(value: string): string;
begin
  Result := '';
end;

function bools(b : boolean) : string;
begin
  if b then Result := 'true' else Result := 'false';
end;


function ExportPoint: string;
var
  n: integer;

begin
    Result := ':'
      + GC.ID
//      + '|' + GC.CacheID
      + '|' + GC.CacheType
      + '|' + GC.Size
      + '|' + GC.Difficulty
      + '|' + GC.Terrain
      + '|' + bools(GC.IsArchived)
      + '|' + bools(GC.IsDisabled)
      + '|' + bools(GC.IsFound)
      + '|' + bools(GC.isOwner)
      + '|' + GC.Author
      + '|' + formatdatetime('yyyy"-"mm"-"dd"T"hh":"nn":"ss"."zzz', GC.Hidden)
      + '|' + GC.Country
      + '|' + GC.State
      + '|' + GC.TagValues(TAG_BESTOF)
      + '|' + GC.TagValues(TAG_EVAL)
      + '|' + GC.TagValues(TAG_EVALCOUNT)
      + '|' + GC.TagValues(TAG_ZNAMKA)
      + '|' + RegexReplace('[\r\n\|]', GC.hint, ' ', false)
      + '|' + GC.URL
      + CRLF;

  //Export for Waypoints

   Result := Result +  '-GC|Cache'
       + '|' + GC.Lat
       + '|' + GC.Lon
       + '|' + GC.Name
       +  CRLF;

  for n := 0 to GC.Waypoints.Count - 1 do
  begin
    if (not GC.Waypoints[n].isEmptyCoord) and (GC.Waypoints[n].isListed) then
    begin
     Result := Result +  '-'
       + GC.Waypoints[n].PrefixID
       + '|' + GC.Waypoints[n].WptType
       + '|' + GC.Waypoints[n].Lat
       + '|' + GC.Waypoints[n].Lon
       + '|' + GC.Waypoints[n].Name
       +  CRLF;
    end;
  end;

  Result := Result + '/' + CRLF;

end;

