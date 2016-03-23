package cz.geokuk.core.coordinates;

/**
 * Merkátorové souřadnice jsou v metrech. Střed souřadnic [0,0] je na průsečíku rovníku a nultého poledníku. Osa X směřuje k východu, osa Y k severu.
 *
 *
 * TMS Global Mercator Profile ---------------------------
 *
 * Functions necessary for generation of tiles in Spherical Mercator projection, EPSG:900913 (EPSG:gOOglE, Google Maps Global Mercator), EPSG:3785, OSGEO:41001.
 *
 * Such tiles are compatible with Google Maps, Microsoft Virtual Earth, Yahoo Maps, UK Ordnance Survey OpenSpace API, ... and you can overlay them on top of base maps of those web mapping applications.
 *
 * Pixel and tile coordinates are in TMS notation (origin [0,0] in bottom-left).
 *
 * What coordinate conversions do we need for TMS Global Mercator tiles::
 *
 * LatLon <-> Meters <-> Pixels <-> Tile
 *
 * WGS84 coordinates Spherical Mercator Pixels in pyramid Tiles in pyramid lat/lon XY in metres XY pixels Z zoom XYZ from TMS EPSG:4326 EPSG:900913 .----. --------- -- TMS / \ <-> | | <-> /----/ <-> Google \ / | | /--------/ QuadTree ----- --------- /------------/ KML, public WebMapService Web
 * Clients TileMapService
 *
 * What is the coordinate extent of Earth in EPSG:900913?
 *
 * [-20037508.342789244, -20037508.342789244, 20037508.342789244, 20037508.342789244] Constant 20037508.342789244 comes from the circumference of the Earth in meters, which is 40 thousand kilometers, the coordinate origin is in the middle of extent. In fact you can calculate the constant as: 2 *
 * math.pi * 6378137 / 2.0 $ echo 180 85 | gdaltransform -s_srs EPSG:4326 -t_srs EPSG:900913 Polar areas with abs(latitude) bigger then 85.05112878 are clipped off.
 *
 * What are zoom level constants (pixels/meter) for pyramid with EPSG:900913?
 *
 * whole region is on top of pyramid (zoom=0) covered by 256x256 pixels tile, every lower zoom level resolution is always divided by two initialResolution = 20037508.342789244 * 2 / 256 = 156543.03392804062
 *
 * What is the difference between TMS and Google Maps/QuadTree tile name convention?
 *
 * The tile raster itself is the same (equal extent, projection, pixel size), there is just different identification of the same raster tile. Tiles in TMS are counted from [0,0] in the bottom-left corner, id is XYZ. Google placed the origin [0,0] to the top-left corner, reference is XYZ. Microsoft
 * is referencing tiles by a QuadTree name, defined on the website: http://msdn2.microsoft.com/en-us/library/bb259689.aspx
 *
 * The lat/lon coordinates are using WGS84 datum, yeh?
 *
 * Yes, all lat/lon we are mentioning should use WGS84 Geodetic Datum. Well, the web clients like Google Maps are projecting those coordinates by Spherical Mercator, so in fact lat/lon coordinates on sphere are treated as if the were on the WGS84 ellipsoid.
 *
 * From MSDN documentation: To simplify the calculations, we use the spherical form of projection, not the ellipsoidal form. Since the projection is used only for map display, and not for displaying numeric coordinates, we don't need the extra precision of an ellipsoidal projection. The spherical
 * projection causes approximately 0.33 percent scale distortion in the Y direction, which is not visually noticable.
 *
 * How do I create a raster in EPSG:900913 and convert coordinates with PROJ.4?
 *
 * You can use standard GIS tools like gdalwarp, cs2cs or gdaltransform. All of the tools supports -t_srs 'epsg:900913'.
 *
 * For other GIS programs check the exact definition of the projection: More info at http://spatialreference.org/ref/user/google-projection/ The same projection is degined as EPSG:3785. WKT definition is in the official EPSG database.
 *
 * Proj4 Text: +proj=merc +a=6378137 +b=6378137 +lat_ts=0.0 +lon_0=0.0 +x_0=0.0 +y_0=0 +k=1.0 +units=m +nadgrids=@null +no_defs
 *
 * Human readable WKT format of EPGS:900913: PROJCS["Google Maps Global Mercator", GEOGCS["WGS 84", DATUM["WGS_1984", SPHEROID["WGS 84",6378137,298.2572235630016, AUTHORITY["EPSG","7030"]], AUTHORITY["EPSG","6326"]], PRIMEM["Greenwich",0], UNIT["degree",0.0174532925199433],
 * AUTHORITY["EPSG","4326"]], PROJECTION["Mercator_1SP"], PARAMETER["central_meridian",0], PARAMETER["scale_factor",1], PARAMETER["false_easting",0], PARAMETER["false_northing",0], UNIT["metre",1, AUTHORITY["EPSG","9001"]]]*
 */
public class Mercator extends Misto0 {

	/** Poloměr země v metrech */
	public final double	mx;
	public final double	my;

	public Mercator(final double aMx, final double aMy) {
		mx = aMx;
		my = aMy;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(mx);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(my);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final Mercator other = (Mercator) obj;
		if (Double.doubleToLongBits(mx) != Double.doubleToLongBits(other.mx))
			return false;
		if (Double.doubleToLongBits(my) != Double.doubleToLongBits(other.my))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return String.format("Mercator[%f ; %f]", mx, my);
	}

	public Wgs toWgs() {
		return FGeoKonvertor.toWgs(this);
	}

	@Override
	public Mercator toMercator() {
		return this;
	}

	@Override
	public Mou toMou() {
		return FGeoKonvertor.toMou(this);
	}

	@Override
	public Utm toUtm() {
		return FGeoKonvertor.toUtm(this);
	}

}
