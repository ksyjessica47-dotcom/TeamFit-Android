package com.teamfit.app.data

enum class ActivityMode { LEADER, MEMBER }

data class Account(
    val name: String,
    val email: String,
    val phone: String,
    val id: String = "",
    val accessToken: String = "",
    val refreshToken: String = "",
    val emailConfirmedAt: String = "",
    val isAdmin: Boolean = false,
)

data class AdminUser(
    val id: String,
    val email: String,
    val name: String,
    val phone: String,
    val emailConfirmedAt: String?,
    val createdAt: String?,
    val lastSignInAt: String?,
)

data class AuthResult(
    val account: Account,
    val expiresIn: Int,
)

data class Competition(
    val id: String,
    val name: String,
    val organizer: String,
    val deadline: String,
    val period: String,
    val description: String,
    val tags: List<String>,
    val accent: String,
    val teamCount: Int,
    val eligibility: String = "대학생 및 대학원생",
    val teamSize: String = "2~5인 팀",
    val benefit: String = "상금 및 수상 인증서",
    val location: String = "온라인 및 오프라인",
    val officialUrl: String = "https://www.wevity.com",
)

data class Team(
    val id: String,
    val competitionId: String,
    val title: String,
    val summary: String,
    val role: String,
    val requiredSkills: List<String>,
    val preferredSkills: List<String>,
    val weeklyHours: Int,
    val goal: String,
    val meetingTimes: List<String>,
    val leader: String,
    val candidateCount: Int,
    val ownerEmail: String = "",
    val ownerPhone: String = "",
    val openings: Int = 2,
    val applicantIds: List<String> = emptyList(),
)

data class Experience(
    val title: String,
    val period: String,
    val role: String?,
    val skills: List<String>,
    val contribution: String,
    val resultUrl: String?,
)

data class Candidate(
    val id: String,
    val name: String,
    val initials: String,
    val accent: String,
    val desiredRole: String?,
    val skills: List<String>,
    val weeklyHours: Int?,
    val meetingTimes: List<String>,
    val goal: String?,
    val joinMessage: String,
    val experiences: List<Experience>,
    val email: String = "",
    val phone: String = "",
    val competitions: List<String> = emptyList(),
)

data class UserProfile(
    val desiredRole: String = "",
    val skills: List<String> = emptyList(),
    val weeklyHours: Int = 8,
    val meetingTime: String = "협의 가능",
    val goal: String = "프로젝트 경험",
    val introduction: String = "",
    val experience: String = "",
    val contribution: String = "",
    val competitions: List<String> = emptyList(),
    val portfolioUrl: String = "",
)

enum class InviteStatus { PENDING, ACCEPTED, DECLINED }

data class TeamInvitation(
    val teamId: String,
    val candidateId: String,
    val status: InviteStatus = InviteStatus.PENDING,
)

data class ChatMessage(
    val id: String,
    val teamId: String,
    val candidateId: String,
    val senderMode: ActivityMode,
    val senderName: String,
    val text: String,
    val sentAt: Long = System.currentTimeMillis(),
)

data class DecisionDraft(
    val teamId: String,
    val decision: String,
    val candidateId: String?,
    val importantFactor: String,
    val reason: String,
)

data class SavedDecision(
    val teamId: String,
    val decision: String,
    val candidateId: String?,
    val importantFactor: String,
    val reason: String,
    val selectedAt: String,
)
