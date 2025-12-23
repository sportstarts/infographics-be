package sportstarts.infographics.lap

import cats.effect.IO
import doobie.implicits.*
import doobie.postgres.implicits.*
import doobie.util.transactor.Transactor
import sportstarts.infographics.competition.CompetitionId

trait LapRepo:
  def storeLap(competitionId: CompetitionId, lap: StoreLap): IO[Lap]
  def updateLap(id: LapId, lap: UpdateLap): IO[Option[Lap]]
  def deleteLap(id: LapId): IO[Boolean]
  def listByCompetition(competitionId: CompetitionId): IO[List[Lap]]

object LapRepo:
  object queries:
    def storeLap(competitionId: CompetitionId, lap: StoreLap) =
      sql"""| INSERT INTO laps (
            |   competition_id, sport_kind_id, lats, lons, description, 
            |   lap_distance_meters, points_distances_meters
            | ) VALUES (
            |   $competitionId, ${lap.sportKindId}, ${lap.lats}, ${lap.lons}, 
            |   ${lap.description}, ${lap.lapDistanceMeters}, ${lap.pointsDistancesMeters}
            | )
            |"""
        .stripMargin
        .update
        .withUniqueGeneratedKeys[Lap]("id", "competition_id", "sport_kind_id", "lats",
          "lons", "description", "lap_distance_meters", "points_distances_meters"
        )

    def updateLap(id: LapId, lap: UpdateLap) =
      sql"""| UPDATE laps
            | SET sport_kind_id = ${lap.sportKindId}, description = ${lap.description}
            | WHERE id = $id
            | RETURNING id, competition_id, sport_kind_id, lats, lons, description, 
            |           lap_distance_meters, points_distances_meters
            |""".query[Lap]

    def deleteLap(id: LapId) = sql"DELETE FROM laps WHERE id = $id".update

    def listByCompetition(competitionId: CompetitionId) =
      sql"""| SELECT id, competition_id, sport_kind_id, lats, lons, description, 
            |        lap_distance_meters, points_distances_meters 
            | FROM laps WHERE competition_id = $competitionId ORDER BY id ASC
            |""".stripMargin.query[Lap]

  class Impl(tx: Transactor[IO]) extends LapRepo:
    def storeLap(competitionId: CompetitionId, lap: StoreLap): IO[Lap] =
      queries.storeLap(competitionId, lap).transact(tx)

    def updateLap(id: LapId, lap: UpdateLap): IO[Option[Lap]] =
      queries.updateLap(id, lap).option.transact(tx)

    def deleteLap(id: LapId): IO[Boolean] =
      queries.deleteLap(id).run.map(_ > 0).transact(tx)

    def listByCompetition(competitionId: CompetitionId): IO[List[Lap]] =
      queries.listByCompetition(competitionId).to[List].transact(tx)

  def apply(tx: Transactor[IO]): LapRepo = Impl(tx)
