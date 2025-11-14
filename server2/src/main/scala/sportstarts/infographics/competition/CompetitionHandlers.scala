package sportstarts.infographics.competition

import cats.effect.IO
import sportstarts.infographics.competition.CompetitionHandlers.CompetitionDoesntExist

import java.time.LocalDate

trait CompetitionHandlers:
  def getCompetitionById(id: CompetitionId): IO[Either[CompetitionDoesntExist.type, Competition]]
  def createCompetition(competition: CreateCompetition): IO[Competition]
  def editCompetition(competitionId: CompetitionId, competition: CreateCompetition): IO[Either[CompetitionDoesntExist.type, Competition]]
  def deleteCompetition(id: CompetitionId): IO[Boolean]
  def listCompetitions(
    name: Option[CompetitionName],
    dateFrom: Option[LocalDate],
    dateTo: Option[LocalDate],
    place: Option[CompetitionPlace],
    offset: Offset,
    limit: Limit
  ): IO[List[Competition]]

object CompetitionHandlers:
  sealed trait CompetitionHandlersError

  object CompetitionDoesntExist extends CompetitionHandlersError

  class Impl(competitionRepo: CompetitionRepo) extends CompetitionHandlers:
    def getCompetitionById(id: CompetitionId): IO[Either[CompetitionDoesntExist.type, Competition]] =
      competitionRepo.getCompetitionById(id).map {
        case Some(competition) => Right(competition)
        case None => Left(CompetitionDoesntExist)
      }

    def createCompetition(competition: CreateCompetition): IO[Competition] =
      competitionRepo.storeCompetition(competition)

    def editCompetition(competitionId: CompetitionId, competition: CreateCompetition): IO[Either[CompetitionDoesntExist.type, Competition]] =
      competitionRepo.updateCompetition(competitionId, competition).map {
        case Some(competition) => Right(competition)
        case None => Left(CompetitionDoesntExist)
      }

    def deleteCompetition(id: CompetitionId): IO[Boolean] =
      competitionRepo.deleteCompetition(id)

    def listCompetitions(
      name: Option[CompetitionName],
      dateFrom: Option[LocalDate],
      dateTo: Option[LocalDate],
      place: Option[CompetitionPlace],
      offset: Offset,
      limit: Limit
    ): IO[List[Competition]] =
      competitionRepo.list(name, dateFrom, dateTo, place, offset, limit)

  def apply(competitionRepo: CompetitionRepo): CompetitionHandlers =
    new Impl(competitionRepo)
