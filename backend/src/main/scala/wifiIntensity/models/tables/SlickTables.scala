package wifiIntensity.models.tables

// AUTO-GENERATED Slick data model
/** Stand-alone Slick data model for immediate use */
object SlickTables extends {
  val profile = slick.driver.PostgresDriver
} with SlickTables

/** Slick data model trait for extension, choice of backend or usage in the cake pattern. (Make sure to initialize this late.) */
trait SlickTables {
  val profile: slick.driver.JdbcProfile
  import profile.api._
  import slick.model.ForeignKeyAction
  // NOTE: GetResult mappers for plain SQL are only generated for tables where Slick knows how to map the types of all columns.
  import slick.jdbc.{GetResult => GR}

  /** DDL for all tables. Call .create to execute. */
  lazy val schema: profile.SchemaDescription = tBasicShoot.schema ++ tBoxs.schema
  @deprecated("Use .schema instead of .ddl", "3.0")
  def ddl = schema


  /** GetResult implicit for fetching rBasicShoot objects using plain SQL queries */
  implicit def GetResultrBasicShoot(implicit e0: GR[Long], e1: GR[String], e2: GR[Double]): GR[rBasicShoot] = GR{
    prs => import prs._
    rBasicShoot.tupled((<<[Long], <<[String], <<[String], <<[Long], <<[Double], <<[Double], <<[Double]))
  }
  /** Table description of table basic_shoot. Objects of this class serve as prototypes for rows in queries. */
  class tBasicShoot(_tableTag: Tag) extends Table[rBasicShoot](_tableTag, "basic_shoot") {
    def * = (id, boxMac, clientMac, timestamp, rssi1, rssi2, distance) <> (rBasicShoot.tupled, rBasicShoot.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(boxMac), Rep.Some(clientMac), Rep.Some(timestamp), Rep.Some(rssi1), Rep.Some(rssi2), Rep.Some(distance)).shaped.<>({r=>import r._; _1.map(_=> rBasicShoot.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(bigserial), AutoInc, PrimaryKey */
    val id: Rep[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)
    /** Database column box_mac SqlType(varchar), Length(63,true) */
    val boxMac: Rep[String] = column[String]("box_mac", O.Length(63,varying=true))
    /** Database column client_mac SqlType(varchar), Length(63,true) */
    val clientMac: Rep[String] = column[String]("client_mac", O.Length(63,varying=true))
    /** Database column timestamp SqlType(int8) */
    val timestamp: Rep[Long] = column[Long]("timestamp")
    /** Database column rssi1 SqlType(float8) */
    val rssi1: Rep[Double] = column[Double]("rssi1")
    /** Database column rssi2 SqlType(float8) */
    val rssi2: Rep[Double] = column[Double]("rssi2")
    /** Database column distance SqlType(float8), Default(1.0) */
    val distance: Rep[Double] = column[Double]("distance", O.Default(1.0))
  }
  /** Collection-like TableQuery object for table tBasicShoot */
  lazy val tBasicShoot = new TableQuery(tag => new tBasicShoot(tag))


  /** GetResult implicit for fetching rBoxs objects using plain SQL queries */
  implicit def GetResultrBoxs(implicit e0: GR[String], e1: GR[Int], e2: GR[Double]): GR[rBoxs] = GR{
    prs => import prs._
    rBoxs.tupled((<<[String], <<[String], <<[Int], <<[Double], <<[Double], <<[Double]))
  }
  /** Table description of table boxs. Objects of this class serve as prototypes for rows in queries. */
  class tBoxs(_tableTag: Tag) extends Table[rBoxs](_tableTag, "boxs") {
    def * = (boxMac, boxName, rssiSet, distanceLoss, x, y) <> (rBoxs.tupled, rBoxs.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(boxMac), Rep.Some(boxName), Rep.Some(rssiSet), Rep.Some(distanceLoss), Rep.Some(x), Rep.Some(y)).shaped.<>({r=>import r._; _1.map(_=> rBoxs.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column box_mac SqlType(varchar), PrimaryKey, Length(63,true) */
    val boxMac: Rep[String] = column[String]("box_mac", O.PrimaryKey, O.Length(63,varying=true))
    /** Database column box_name SqlType(varchar), Length(255,true) */
    val boxName: Rep[String] = column[String]("box_name", O.Length(255,varying=true))
    /** Database column rssi_set SqlType(int4) */
    val rssiSet: Rep[Int] = column[Int]("rssi_set")
    /** Database column distance_loss SqlType(float8), Default(2.1) */
    val distanceLoss: Rep[Double] = column[Double]("distance_loss", O.Default(2.1))
    /** Database column x SqlType(float8), Default(0.0) */
    val x: Rep[Double] = column[Double]("x", O.Default(0.0))
    /** Database column y SqlType(float8), Default(0.0) */
    val y: Rep[Double] = column[Double]("y", O.Default(0.0))
  }
  /** Collection-like TableQuery object for table tBoxs */
  lazy val tBoxs = new TableQuery(tag => new tBoxs(tag))
}
/** Entity class storing rows of table tBasicShoot
   *  @param id Database column id SqlType(bigserial), AutoInc, PrimaryKey
   *  @param boxMac Database column box_mac SqlType(varchar), Length(63,true)
   *  @param clientMac Database column client_mac SqlType(varchar), Length(63,true)
   *  @param timestamp Database column timestamp SqlType(int8)
   *  @param rssi1 Database column rssi1 SqlType(float8)
   *  @param rssi2 Database column rssi2 SqlType(float8)
   *  @param distance Database column distance SqlType(float8), Default(1.0) */
  case class rBasicShoot(id: Long, boxMac: String, clientMac: String, timestamp: Long, rssi1: Double, rssi2: Double, distance: Double = 1.0)

  /** Entity class storing rows of table tBoxs
   *  @param boxMac Database column box_mac SqlType(varchar), PrimaryKey, Length(63,true)
   *  @param boxName Database column box_name SqlType(varchar), Length(255,true)
   *  @param rssiSet Database column rssi_set SqlType(int4)
   *  @param distanceLoss Database column distance_loss SqlType(float8), Default(2.1)
   *  @param x Database column x SqlType(float8), Default(0.0)
   *  @param y Database column y SqlType(float8), Default(0.0) */
  case class rBoxs(boxMac: String, boxName: String, rssiSet: Int, distanceLoss: Double = 2.1, x: Double = 0.0, y: Double = 0.0)
