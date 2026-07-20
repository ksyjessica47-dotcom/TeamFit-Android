package com.teamfit.app.data

import java.time.Instant

data class RepositoryResult<T>(val data: T, val offlineDemo: Boolean)

class TeamFitRepository(private val api: TeamFitApi = TeamFitApi()) {
    suspend fun signup(name: String, email: String, phone: String, password: String): String =
        api.signup(name, email, phone, password)

    suspend fun login(email: String, password: String): AuthResult =
        api.login(email, password)

    suspend fun adminUsers(accessToken: String): List<AdminUser> =
        api.adminUsers(accessToken)

    suspend fun competitions(): RepositoryResult<List<Competition>> =
        runCatching { RepositoryResult(api.competitions(), false) }
            .getOrElse { RepositoryResult(SampleData.competitions, true) }

    suspend fun teams(competitionId: String): RepositoryResult<List<Team>> =
        runCatching { RepositoryResult(api.teams(competitionId), false) }
            .getOrElse { RepositoryResult(SampleData.teamsFor(competitionId), true) }

    suspend fun candidates(teamId: String): RepositoryResult<List<Candidate>> =
        runCatching { RepositoryResult(api.candidates(teamId), false) }
            .getOrElse { RepositoryResult(SampleData.candidatesFor(teamId), true) }

    suspend fun saveDecision(draft: DecisionDraft): RepositoryResult<SavedDecision> =
        runCatching { RepositoryResult(api.saveDecision(draft), false) }
            .getOrElse {
                RepositoryResult(
                    SavedDecision(draft.teamId, draft.decision, draft.candidateId, draft.importantFactor,
                        draft.reason, Instant.now().toString()), true
                )
            }
}
