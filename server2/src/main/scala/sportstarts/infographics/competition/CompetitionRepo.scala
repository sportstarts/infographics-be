package sportstarts.infographics.competition

import cats.effect.IO
import doobie.implicits.*
import doobie.util.transactor.Transactor

import java.time.LocalDate

trait CompetitionRepo:
  def getCompetitionById(id: CompetitionId): IO[Option[Competition]]
  def storeCompetition(competition: CreateCompetition): IO[Competition]
  def updateCompetition(id: CompetitionId, competition: CreateCompetition): IO[Option[Competition]]
  def deleteCompetition(id: CompetitionId): IO[Boolean]
  def list(
    name: Option[CompetitionName],
    dateFrom: Option[LocalDate],
    dateTo: Option[LocalDate],
    place: Option[CompetitionPlace],
    offset: Offset,
    limit: Limit
  ): IO[List[Competition]]

object CompetitionRepo:
  object queries:
    def getCompetitionById(id: CompetitionId) = sql"SELECT id, name, date, place FROM competitions WHERE id = $id".query[Competition]
    def storeCompetition(competition: CreateCompetition) =
      sql"INSERT INTO competitions (name, date, place) VALUES (${competition.name}, ${competition.date}, ${competition.place})".update.withUniqueGeneratedKeys[Competition]("id", "name", "date", "place")

    def updateCompetition(id: CompetitionId, competition: CreateCompetition) =
      sql"""| UPDATE competitions
            | SET name = ${competition.name}, date = ${competition.date}, place = ${competition.place}
            | WHERE id = $id
            | RETURNING id, name, date, place
      """.stripMargin.query[Competition]

    def deleteCompetition(id: CompetitionId) =
      sql"DELETE FROM competitions WHERE id = $id".update

    def list(
      name: Option[CompetitionName],
      dateFrom: Option[LocalDate],
      dateTo: Option[LocalDate],
      place: Option[CompetitionPlace],
      offset: Offset,
      limit: Limit
    ) = {
      val baseQuery = fr"SELECT id, name, date, place FROM competitions"
      val filters = List(
        name.map(n => fr"name ilike ${"%" + n + "%"}"),
        dateFrom.map(df => fr"date >= $df"),
        dateTo.map(dt => fr"date <= $dt"),
        place.map(p => fr"place = $p")
      ).flatten
      val whereClause = if (filters.nonEmpty) fr"WHERE" ++ filters.reduce(_ ++ fr"AND" ++ _) else fr""
      val orderClause = fr"ORDER BY date DESC"
      val pagination = fr"OFFSET $offset LIMIT $limit"
      val query = (baseQuery ++ whereClause ++ orderClause ++ pagination)
      query.query[Competition]
    }

  class Impl(tx: Transactor[IO]) extends CompetitionRepo:
    def getCompetitionById(id: CompetitionId): IO[Option[Competition]] =
      queries.getCompetitionById(id).option.transact(tx)
    def storeCompetition(competition: CreateCompetition): IO[Competition] =
      queries.storeCompetition(competition).transact(tx)
    def updateCompetition(id: CompetitionId, competition: CreateCompetition): IO[Option[Competition]] =
      queries.updateCompetition(id, competition).option.transact(tx)
    def deleteCompetition(id: CompetitionId): IO[Boolean] =
      queries.deleteCompetition(id).run.map(_ > 0).transact(tx)
    def list(
      name: Option[CompetitionName],
      dateFrom: Option[LocalDate],
      dateTo: Option[LocalDate],
      place: Option[CompetitionPlace],
      offset: Offset,
      limit: Limit
    ): IO[List[Competition]] =
      queries.list(name, dateFrom, dateTo, place, offset, limit).to[List].transact(tx)

  def apply(tx: Transactor[IO]): CompetitionRepo = Impl(tx)
