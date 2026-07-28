package com.teamfit.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teamfit.app.data.Account
import com.teamfit.app.data.ActivityMode
import com.teamfit.app.data.AdminUser
import com.teamfit.app.data.Candidate
import com.teamfit.app.data.ChatMessage
import com.teamfit.app.data.Competition
import com.teamfit.app.data.Experience
import com.teamfit.app.data.InviteStatus
import com.teamfit.app.data.SampleData
import com.teamfit.app.data.Team
import com.teamfit.app.data.TeamInvitation
import com.teamfit.app.data.TeamFitRepository
import com.teamfit.app.data.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class TeamFitScreen { AUTH, MODE, ADMIN, COMPETITIONS, COMPETITION_DETAIL, TEAMS, TEAM_DETAIL }

data class TeamFitUiState(
    val screen: TeamFitScreen = TeamFitScreen.AUTH,
    val accounts: List<Account> = emptyList(),
    val authenticatedAccount: Account? = null,
    val account: Account? = null,
    val mode: ActivityMode? = null,
    val competitions: List<Competition> = SampleData.competitions,
    val teams: List<Team> = SampleData.teams,
    val candidates: List<Candidate> = SampleData.candidates,
    val profiles: Map<String, UserProfile> = SampleData.profiles,
    val selectedCompetition: Competition? = null,
    val selectedTeam: Team? = null,
    val profileTarget: Candidate? = null,
    val chatTarget: Candidate? = null,
    val messages: List<ChatMessage> = emptyList(),
    val readMessageIds: Set<String> = emptySet(),
    val invitations: List<TeamInvitation> = emptyList(),
    val showMyPage: Boolean = false,
    val showNotifications: Boolean = false,
    val showRecruitForm: Boolean = false,
    val authError: String? = null,
    val authNotice: String? = null,
    val authSubmitting: Boolean = false,
    val adminUsers: List<AdminUser> = emptyList(),
    val adminLoading: Boolean = false,
    val adminError: String? = null,
    val message: String? = null,
)

class TeamFitViewModel(
    private val repository: TeamFitRepository = TeamFitRepository(),
) : ViewModel() {
    private val _uiState = MutableStateFlow(TeamFitUiState())
    val uiState: StateFlow<TeamFitUiState> = _uiState.asStateFlow()

    fun clearMessage() = _uiState.update { it.copy(message = null) }
    fun clearAuthError() = _uiState.update { it.copy(authError = null) }

    fun signup(name: String, email: String, phone: String, password: String, passwordConfirm: String) {
        val cleanEmail = email.trim().lowercase()
        when {
            name.isBlank() || !cleanEmail.contains("@") -> _uiState.update { it.copy(authError = "회원가입 정보를 확인해 주세요.") }
            !phone.matches(Regex("010-\\d{4}-\\d{4}")) -> _uiState.update { it.copy(authError = "전화번호를 010-0000-0000 형식으로 입력해 주세요.") }
            password.length < 8 -> _uiState.update { it.copy(authError = "비밀번호는 8자 이상으로 만들어 주세요.") }
            password != passwordConfirm -> _uiState.update { it.copy(authError = "비밀번호 확인이 일치하지 않습니다.") }
            else -> viewModelScope.launch {
                _uiState.update { it.copy(authSubmitting = true, authError = null, authNotice = null) }
                runCatching { repository.signup(name.trim(), cleanEmail, phone, password) }
                    .onSuccess { notice -> _uiState.update { it.copy(authSubmitting = false, authNotice = notice) } }
                    .onFailure { error -> _uiState.update { it.copy(authSubmitting = false, authError = error.message ?: "회원가입하지 못했습니다.") } }
            }
        }
    }

    fun login(email: String, password: String) {
        val cleanEmail = email.trim().lowercase()
        if (!cleanEmail.contains("@") || password.isBlank()) {
            _uiState.update { it.copy(authError = "이메일과 비밀번호를 입력해 주세요.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(authSubmitting = true, authError = null, authNotice = null) }
            runCatching { repository.login(cleanEmail, password) }
                .onSuccess { result ->
                    val account = result.account
                    _uiState.update {
                        it.copy(
                            authenticatedAccount = account,
                            accounts = (it.accounts.filterNot { saved -> saved.email == account.email } + account),
                            screen = TeamFitScreen.MODE,
                            authSubmitting = false,
                            authError = null,
                        )
                    }
                }
                .onFailure { error -> _uiState.update { it.copy(authSubmitting = false, authError = error.message ?: "로그인하지 못했습니다.") } }
        }
    }

    fun selectMode(mode: ActivityMode) {
        val account = _uiState.value.authenticatedAccount ?: _uiState.value.account ?: return
        _uiState.update {
            it.copy(account = account, authenticatedAccount = null, mode = mode, screen = TeamFitScreen.COMPETITIONS,
                selectedCompetition = null, selectedTeam = null, message = if (mode == ActivityMode.LEADER) "팀장 모드로 시작합니다." else "팀원 모드로 시작합니다.")
        }
    }

    fun openAdmin() {
        val account = _uiState.value.account ?: _uiState.value.authenticatedAccount
        if (account == null || !account.isAdmin) {
            _uiState.update { it.copy(adminError = "관리자 권한이 없습니다.") }
            return
        }
        _uiState.update { it.copy(account = account, screen = TeamFitScreen.ADMIN, adminError = null) }
        loadAdminUsers()
    }

    fun loadAdminUsers() {
        val account = _uiState.value.account ?: _uiState.value.authenticatedAccount ?: return
        if (!account.isAdmin || account.accessToken.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(adminLoading = true, adminError = null) }
            runCatching { repository.adminUsers(account.accessToken) }
                .onSuccess { users -> _uiState.update { it.copy(adminUsers = users, adminLoading = false) } }
                .onFailure { error -> _uiState.update { it.copy(adminLoading = false, adminError = error.message ?: "가입자 목록을 불러오지 못했습니다.") } }
        }
    }

    fun switchMode() {
        val current = _uiState.value.mode ?: return
        val next = if (current == ActivityMode.LEADER) ActivityMode.MEMBER else ActivityMode.LEADER
        _uiState.update { it.copy(mode = next, screen = TeamFitScreen.COMPETITIONS, selectedCompetition = null, selectedTeam = null,
            profileTarget = null, chatTarget = null, message = if (next == ActivityMode.LEADER) "팀장 모드로 전환했습니다." else "팀원 모드로 전환했습니다.") }
    }

    fun logout() = _uiState.update {
        it.copy(screen = TeamFitScreen.AUTH, authenticatedAccount = null, account = null, mode = null,
            selectedCompetition = null, selectedTeam = null, profileTarget = null, chatTarget = null,
            showMyPage = false, showNotifications = false, authError = null)
    }

    fun selectCompetition(competition: Competition) = _uiState.update {
        it.copy(selectedCompetition = competition, selectedTeam = null, screen = TeamFitScreen.COMPETITION_DETAIL)
    }

    fun openTeams() = _uiState.update { state ->
        if (state.selectedCompetition == null) state else state.copy(screen = TeamFitScreen.TEAMS, selectedTeam = null)
    }

    fun selectTeam(team: Team) = _uiState.update { it.copy(selectedTeam = team, screen = TeamFitScreen.TEAM_DETAIL) }

    fun back() = _uiState.update {
        when (it.screen) {
            TeamFitScreen.AUTH -> it
            TeamFitScreen.MODE -> it.copy(screen = TeamFitScreen.AUTH, authenticatedAccount = null)
            TeamFitScreen.ADMIN -> if (it.mode == null) it.copy(screen = TeamFitScreen.MODE) else it.copy(screen = TeamFitScreen.COMPETITIONS)
            TeamFitScreen.COMPETITIONS -> it
            TeamFitScreen.COMPETITION_DETAIL -> it.copy(screen = TeamFitScreen.COMPETITIONS, selectedCompetition = null)
            TeamFitScreen.TEAMS -> it.copy(screen = TeamFitScreen.COMPETITION_DETAIL, selectedTeam = null)
            TeamFitScreen.TEAM_DETAIL -> it.copy(screen = TeamFitScreen.TEAMS, selectedTeam = null, profileTarget = null, chatTarget = null)
        }
    }

    fun home() = _uiState.update { it.copy(screen = TeamFitScreen.COMPETITIONS, selectedCompetition = null, selectedTeam = null, profileTarget = null, chatTarget = null) }

    fun setRecruitForm(show: Boolean) = _uiState.update { it.copy(showRecruitForm = show) }

    fun createTeam(title: String, summary: String, role: String, skills: String, weeklyHours: Int, openings: Int) {
        val state = _uiState.value
        val account = state.account ?: return
        val competition = state.selectedCompetition ?: return
        if (title.isBlank() || role.isBlank()) {
            _uiState.update { it.copy(message = "모집글 제목과 모집 역할을 입력해 주세요.") }
            return
        }
        val profile = state.profiles[account.email]
        val team = Team(
            id = "team-${System.currentTimeMillis()}", competitionId = competition.id, title = title.trim(), summary = summary.trim(),
            role = role.trim(), requiredSkills = skills.split(",").map { it.trim() }.filter { it.isNotBlank() }, preferredSkills = emptyList(),
            weeklyHours = weeklyHours.coerceIn(1, 40), goal = profile?.goal ?: "프로젝트 경험", meetingTimes = listOf(profile?.meetingTime ?: "협의 가능"),
            leader = account.name, candidateCount = 0, ownerEmail = account.email, ownerPhone = account.phone,
            openings = openings.coerceIn(1, 5), applicantIds = emptyList(),
        )
        _uiState.update { it.copy(teams = listOf(team) + it.teams, showRecruitForm = false, message = "팀원 모집글을 등록했습니다.") }
    }

    private fun candidateForAccount(state: TeamFitUiState): Candidate? {
        val account = state.account ?: return null
        return state.candidates.find { it.email == account.email } ?: run {
            val profile = state.profiles[account.email] ?: UserProfile()
            Candidate(
                id = account.email.substringBefore("@").replace(Regex("[^a-zA-Z0-9]"), "").ifBlank { "user-${account.email.hashCode()}" },
                name = account.name, initials = account.name.takeLast(2), accent = "violet", desiredRole = profile.desiredRole.ifBlank { "역할 협의" },
                skills = profile.skills, weeklyHours = profile.weeklyHours, meetingTimes = listOf(profile.meetingTime), goal = profile.goal,
                joinMessage = profile.introduction, experiences = listOf(Experience(profile.experience, "최근", profile.desiredRole, profile.skills,
                    profile.contribution, profile.portfolioUrl.ifBlank { null })), email = account.email, phone = account.phone, competitions = profile.competitions,
            )
        }
    }

    fun applyToSelectedTeam() {
        val state = _uiState.value
        val team = state.selectedTeam ?: return
        val candidate = candidateForAccount(state) ?: return
        if (team.ownerEmail == state.account?.email) {
            _uiState.update { it.copy(message = "본인이 작성한 모집글에는 지원할 수 없습니다.") }
            return
        }
        if (team.applicantIds.contains(candidate.id)) {
            _uiState.update { it.copy(message = "이미 지원한 팀입니다.") }
            return
        }
        val updatedTeam = team.copy(applicantIds = team.applicantIds + candidate.id, candidateCount = team.candidateCount + 1)
        _uiState.update {
            it.copy(teams = it.teams.map { item -> if (item.id == team.id) updatedTeam else item }, selectedTeam = updatedTeam,
                candidates = if (it.candidates.any { item -> item.id == candidate.id }) it.candidates else it.candidates + candidate,
                message = "팀장에게 지원서를 보냈습니다.")
        }
    }

    fun showProfile(candidate: Candidate?) = _uiState.update { it.copy(profileTarget = candidate) }
    fun showMyPage(show: Boolean) = _uiState.update { it.copy(showMyPage = show) }
    fun showNotifications(show: Boolean) = _uiState.update { it.copy(showNotifications = show) }

    fun saveMyPage(account: Account, profile: UserProfile) {
        val state = _uiState.value
        val old = state.account ?: return
        if (!account.phone.matches(Regex("010-\\d{4}-\\d{4}"))) {
            _uiState.update { it.copy(message = "전화번호 형식을 확인해 주세요.") }
            return
        }
        if (state.accounts.any { it.email == account.email && it.email != old.email }) {
            _uiState.update { it.copy(message = "이미 사용 중인 이메일입니다.") }
            return
        }
        val updatedAccount = old.copy(name = account.name, email = account.email, phone = account.phone)
        val accounts = state.accounts.map { if (it.email == old.email) updatedAccount else it }
        val profiles = state.profiles - old.email + (updatedAccount.email to profile)
        val candidates = state.candidates.map {
            if (it.email == old.email) it.copy(name = account.name, initials = account.name.takeLast(2), email = account.email, phone = account.phone,
                desiredRole = profile.desiredRole, skills = profile.skills, weeklyHours = profile.weeklyHours, meetingTimes = listOf(profile.meetingTime),
                goal = profile.goal, joinMessage = profile.introduction, competitions = profile.competitions,
                experiences = listOf(Experience(profile.experience, "최근", profile.desiredRole, profile.skills, profile.contribution, profile.portfolioUrl.ifBlank { null }))) else it
        }
        val teams = state.teams.map { if (it.ownerEmail == old.email) it.copy(leader = account.name, ownerEmail = account.email, ownerPhone = account.phone) else it }
        _uiState.update { it.copy(accounts = accounts, account = updatedAccount, profiles = profiles, candidates = candidates, teams = teams,
            selectedTeam = it.selectedTeam?.let { team -> teams.find { updated -> updated.id == team.id } }, showMyPage = false, message = "마이페이지를 저장했습니다.") }
    }

    fun openChat(candidate: Candidate) {
        val team = _uiState.value.selectedTeam ?: return
        openChat(team, candidate)
    }

    fun openChat(teamId: String, candidate: Candidate) {
        val team = _uiState.value.teams.find { it.id == teamId } ?: return
        openChat(team, candidate)
    }

    private fun openChat(team: Team, candidate: Candidate) {
        val state = _uiState.value
        var messages = state.messages
        if (state.mode == ActivityMode.LEADER && messages.none { it.teamId == team.id && it.candidateId == candidate.id }) {
            messages = messages + ChatMessage("m-${System.currentTimeMillis()}", team.id, candidate.id, ActivityMode.LEADER,
                state.account?.name.orEmpty(), "안녕하세요 ${candidate.name}님, 지원해 주셔서 감사합니다. 몇 가지 추가로 확인하고 싶습니다.")
        }
        val incomingIds = messages
            .filter { it.teamId == team.id && it.candidateId == candidate.id && it.senderMode != state.mode }
            .map { it.id }
            .toSet()
        _uiState.update { it.copy(selectedTeam = team, chatTarget = candidate, messages = messages, readMessageIds = it.readMessageIds + incomingIds) }
    }

    fun closeChat() = _uiState.update { it.copy(chatTarget = null) }

    fun sendMessage(text: String) {
        val state = _uiState.value
        val team = state.selectedTeam ?: return
        val target = state.chatTarget ?: return
        val account = state.account ?: return
        val mode = state.mode ?: return
        if (text.isBlank()) return
        _uiState.update {
            it.copy(
                messages = it.messages + ChatMessage("m-${System.currentTimeMillis()}", team.id, target.id, mode, account.name, text.trim()),
                message = "메시지를 보냈습니다. 상대방 알림에 표시됩니다.",
            )
        }
    }

    fun inviteCandidate(candidate: Candidate) {
        val state = _uiState.value
        val team = state.selectedTeam ?: return
        val current = state.invitations.filter { it.teamId == team.id && it.status != InviteStatus.DECLINED }
        if (current.any { it.candidateId == candidate.id }) {
            _uiState.update { it.copy(message = "이미 최종 합류 요청을 보낸 지원자입니다.") }
            return
        }
        if (current.size >= team.openings) {
            _uiState.update { it.copy(message = "모집 인원 ${team.openings}명까지만 선택할 수 있습니다.") }
            return
        }
        _uiState.update { it.copy(invitations = it.invitations + TeamInvitation(team.id, candidate.id), message = "${candidate.name}님에게 최종 합류 요청을 보냈습니다.") }
    }

    fun respondInvitation(teamId: String, accept: Boolean) {
        val state = _uiState.value
        val candidate = candidateForAccount(state) ?: return
        val status = if (accept) InviteStatus.ACCEPTED else InviteStatus.DECLINED
        _uiState.update { it.copy(invitations = it.invitations.map { invite -> if (invite.teamId == teamId && invite.candidateId == candidate.id) invite.copy(status = status) else invite },
            message = if (accept) "최종 합류를 수락했습니다." else "최종 합류를 거절했습니다.") }
    }
}
