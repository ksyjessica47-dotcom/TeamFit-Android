package com.teamfit.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.teamfit.app.data.Account
import com.teamfit.app.data.ActivityMode
import com.teamfit.app.data.Candidate
import com.teamfit.app.data.Competition
import com.teamfit.app.data.InviteStatus
import com.teamfit.app.data.Team
import com.teamfit.app.data.UserProfile

private val Canvas = Color(0xFFF5F6FA)
private val Ink = Color(0xFF172034)
private val Muted = Color(0xFF687187)
private val Line = Color(0xFFE1E4EC)
private val Indigo = Color(0xFF604FE0)
private val IndigoDark = Color(0xFF30245D)
private val IndigoSoft = Color(0xFFF0EDFF)
private val Green = Color(0xFF187257)
private val GreenSoft = Color(0xFFEAF8F2)
private val Rose = Color(0xFFB04455)
private val RoseSoft = Color(0xFFFFF0F2)

@Composable
fun TeamFitApp(viewModel: TeamFitViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsState()
    val snackbars = remember { SnackbarHostState() }

    LaunchedEffect(state.message) {
        state.message?.let {
            snackbars.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    if (state.screen == TeamFitScreen.AUTH) {
        AuthScreen(state, viewModel)
        return
    }
    if (state.screen == TeamFitScreen.MODE) {
        ModeScreen(state.authenticatedAccount, viewModel::selectMode, viewModel::openAdmin, viewModel::back)
        return
    }
    if (state.screen == TeamFitScreen.ADMIN) {
        AdminScreen(state, viewModel::loadAdminUsers, viewModel::back)
        return
    }

    Scaffold(
        containerColor = Canvas,
        snackbarHost = { SnackbarHost(snackbars) },
        topBar = {
            TeamFitTopBar(
                state = state,
                showBack = state.screen != TeamFitScreen.COMPETITIONS,
                onBack = viewModel::back,
                onHome = viewModel::home,
                onSwitchMode = viewModel::switchMode,
                onMyPage = { viewModel.showMyPage(true) },
                onNotifications = { viewModel.showNotifications(true) },
                onAdmin = viewModel::openAdmin,
                onLogout = viewModel::logout,
            )
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (state.screen) {
                TeamFitScreen.COMPETITIONS -> CompetitionScreen(state.competitions, viewModel::selectCompetition)
                TeamFitScreen.COMPETITION_DETAIL -> CompetitionDetailScreen(state.selectedCompetition, viewModel::openTeams)
                TeamFitScreen.TEAMS -> TeamListScreen(state, viewModel::selectTeam, { viewModel.setRecruitForm(true) })
                TeamFitScreen.TEAM_DETAIL -> TeamDetailScreen(state, viewModel)
                else -> Unit
            }
        }
    }

    state.profileTarget?.let { candidate ->
        ProfileDialog(candidate, state.profiles[candidate.email], onDismiss = { viewModel.showProfile(null) })
    }
    if (state.showMyPage) {
        state.account?.let { account ->
            MyPageDialog(account, state.profiles[account.email] ?: UserProfile(), onDismiss = { viewModel.showMyPage(false) }, onSave = viewModel::saveMyPage)
        }
    }
    if (state.showRecruitForm) {
        RecruitDialog(onDismiss = { viewModel.setRecruitForm(false) }, onSave = viewModel::createTeam)
    }
    state.chatTarget?.let { ChatDialog(state, viewModel, it) }
    if (state.showNotifications) {
        NotificationsDialog(state, viewModel, onDismiss = { viewModel.showNotifications(false) })
    }
}

@Composable
private fun AuthScreen(state: TeamFitUiState, viewModel: TeamFitViewModel) {
    var signup by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordConfirm by remember { mutableStateOf("") }

    LaunchedEffect(state.authNotice) {
        if (state.authNotice != null) signup = false
    }

    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.White, IndigoSoft)))) {
        LazyColumn(Modifier.fillMaxSize().statusBarsPadding(), contentPadding = PaddingValues(22.dp, 30.dp, 22.dp, 42.dp)) {
            item {
                Text("TEAM BUILDING WORKSPACE", color = Indigo, fontSize = 11.sp, fontWeight = FontWeight.Black)
                Spacer(Modifier.height(18.dp))
                Text("나에게 맞는 공모전 탐색부터,\n완벽한 팀 빌딩까지", color = Ink, fontSize = 33.sp, lineHeight = 41.sp, fontWeight = FontWeight.Black)
                Spacer(Modifier.height(12.dp))
                Text("하나의 계정으로 팀장과 팀원 모드를 자유롭게 전환하세요.", color = Muted, fontSize = 13.sp, lineHeight = 20.sp)
                Spacer(Modifier.height(28.dp))
                Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(22.dp), elevation = CardDefaults.cardElevation(5.dp)) {
                    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(13.dp)) {
                        Row(Modifier.fillMaxWidth().background(Color(0xFFF0F1F5), RoundedCornerShape(12.dp)).padding(4.dp)) {
                            AuthTab("로그인", !signup, Modifier.weight(1f)) { signup = false; viewModel.clearAuthError() }
                            AuthTab("회원가입", signup, Modifier.weight(1f)) { signup = true; viewModel.clearAuthError() }
                        }
                        Text(if (signup) "TeamFit 통합 계정 만들기" else "계정 정보 입력", color = Ink, fontSize = 22.sp, fontWeight = FontWeight.Black)
                        Text(if (signup) "팀장용·팀원용 회원가입은 따로 없습니다. 이메일 인증 후 활동 모드를 선택합니다." else "이메일 인증을 완료한 계정으로 로그인해 주세요.", color = Muted, fontSize = 12.sp, lineHeight = 18.sp)
                        if (signup) AppTextField(name, { name = it; viewModel.clearAuthError() }, "이름")
                        AppTextField(email, { email = it; viewModel.clearAuthError() }, "이메일", KeyboardType.Email)
                        if (signup) AppTextField(phone, { phone = formatPhone(it); viewModel.clearAuthError() }, "연락처", KeyboardType.Phone)
                        AppTextField(password, { password = it; viewModel.clearAuthError() }, "비밀번호 8자 이상", KeyboardType.Password, isPassword = true)
                        if (signup) AppTextField(passwordConfirm, { passwordConfirm = it; viewModel.clearAuthError() }, "비밀번호 확인", KeyboardType.Password, isPassword = true)
                        state.authNotice?.let { Text(it, color = Green, fontSize = 12.sp, lineHeight = 18.sp, fontWeight = FontWeight.Bold) }
                        state.authError?.let { Text(it, color = Rose, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                        Button(
                            onClick = { if (signup) viewModel.signup(name, email, phone, password, passwordConfirm) else viewModel.login(email, password) },
                            enabled = !state.authSubmitting,
                            modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(13.dp), colors = ButtonDefaults.buttonColors(containerColor = Indigo),
                        ) { Text(if (state.authSubmitting) "처리 중..." else if (signup) "인증 메일 받고 회원가입" else "로그인하고 활동 모드 선택", fontWeight = FontWeight.Black) }
                    }
                }
            }
        }
    }
}

@Composable
private fun AuthTab(text: String, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Text(text, modifier.clickable(onClick = onClick).background(if (selected) Color.White else Color.Transparent, RoundedCornerShape(9.dp)).padding(vertical = 10.dp),
        color = if (selected) Indigo else Muted, fontSize = 12.sp, fontWeight = FontWeight.Black, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
}

@Composable
private fun ModeScreen(account: Account?, onSelect: (ActivityMode) -> Unit, onAdmin: () -> Unit, onBack: () -> Unit) {
    Box(Modifier.fillMaxSize().background(Canvas).statusBarsPadding().padding(22.dp)) {
        Column(Modifier.fillMaxWidth().align(Alignment.Center), verticalArrangement = Arrangement.spacedBy(13.dp)) {
            TextButton(onClick = onBack, modifier = Modifier.align(Alignment.Start)) { Text("← 다른 계정으로 로그인", color = Muted) }
            Text("활동 모드 선택", color = Indigo, fontSize = 11.sp, fontWeight = FontWeight.Black)
            Text("${account?.name.orEmpty()}님,\n이번에는 어떻게 참여할까요?", color = Ink, fontSize = 29.sp, lineHeight = 37.sp, fontWeight = FontWeight.Black)
            Text("계정은 하나이며 로그인 후에도 언제든 모드를 바꿀 수 있습니다.", color = Muted, fontSize = 13.sp)
            Spacer(Modifier.height(8.dp))
            ModeCard("👋", "팀원으로 참여", "모집글을 비교하고 팀장에게 지원해요") { onSelect(ActivityMode.MEMBER) }
            ModeCard("🚀", "팀장으로 시작", "모집글을 작성하고 지원자를 관리해요") { onSelect(ActivityMode.LEADER) }
            if (account?.isAdmin == true) ModeCard("⚙", "가입자 관리", "실제 가입자와 이메일 인증 현황을 확인해요", onAdmin)
        }
    }
}

@Composable
private fun ModeCard(icon: String, title: String, body: String, onClick: () -> Unit) {
    Card(Modifier.fillMaxWidth().clickable(onClick = onClick), shape = RoundedCornerShape(17.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(2.dp)) {
        Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(icon, fontSize = 25.sp); Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) { Text(title, color = Ink, fontSize = 16.sp, fontWeight = FontWeight.Black); Text(body, color = Muted, fontSize = 11.sp) }
            Text("→", color = Indigo, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
private fun TeamFitTopBar(state: TeamFitUiState, showBack: Boolean, onBack: () -> Unit, onHome: () -> Unit, onSwitchMode: () -> Unit, onMyPage: () -> Unit, onNotifications: () -> Unit, onAdmin: () -> Unit, onLogout: () -> Unit) {
    val currentCandidateId = state.candidates.find { it.email == state.account?.email }?.id
    val unreadMessages = state.messages.count {
        !state.readMessageIds.contains(it.id) &&
            ((state.mode == ActivityMode.LEADER && it.senderMode == ActivityMode.MEMBER) ||
                (state.mode == ActivityMode.MEMBER && it.senderMode == ActivityMode.LEADER)) &&
            (state.mode == ActivityMode.LEADER || it.candidateId == currentCandidateId)
    }
    val pendingInvitations = state.invitations.count { it.candidateId == currentCandidateId && it.status == InviteStatus.PENDING }
    val notificationCount = unreadMessages + pendingInvitations
    Surface(color = Color.White, shadowElevation = 2.dp) {
        Column(Modifier.fillMaxWidth().statusBarsPadding()) {
            Row(Modifier.fillMaxWidth().height(58.dp).padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                if (showBack) Text("←", Modifier.clickable(onClick = onBack).padding(8.dp), fontSize = 22.sp, fontWeight = FontWeight.Black)
                Row(Modifier.clickable(onClick = onHome), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(33.dp).background(Indigo, RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) { Text("T", color = Color.White, fontWeight = FontWeight.Black) }
                    Spacer(Modifier.width(8.dp)); Text("TeamFit", color = Ink, fontSize = 18.sp, fontWeight = FontWeight.Black)
                }
                Spacer(Modifier.weight(1f))
                Text(if (state.mode == ActivityMode.LEADER) "팀장 모드" else "팀원 모드", Modifier.background(IndigoSoft, RoundedCornerShape(999.dp)).padding(8.dp, 5.dp), color = Indigo, fontSize = 10.sp, fontWeight = FontWeight.Black)
            }
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 12.dp, vertical = 6.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                MiniAction("모드 전환", onSwitchMode); MiniAction("마이페이지", onMyPage); MiniAction(if (notificationCount > 0) "알림 · $notificationCount" else "알림", onNotifications); if (state.account?.isAdmin == true) MiniAction("관리자", onAdmin); MiniAction("로그아웃", onLogout)
            }
        }
    }
}

@Composable private fun MiniAction(text: String, onClick: () -> Unit) { Text(text, Modifier.clickable(onClick = onClick).border(1.dp, Line, RoundedCornerShape(8.dp)).padding(10.dp, 6.dp), color = Muted, fontSize = 10.sp, fontWeight = FontWeight.Bold) }

@Composable
private fun CompetitionScreen(competitions: List<Competition>, onSelect: (Competition) -> Unit) {
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 40.dp)) {
        item {
            Box(Modifier.fillMaxWidth().background(Brush.linearGradient(listOf(IndigoDark, Indigo))).padding(22.dp, 32.dp)) {
                Column { Text("OPEN CONTESTS", color = Color(0xFFD5D0FF), fontSize = 10.sp, fontWeight = FontWeight.Black); Spacer(Modifier.height(10.dp)); Text("공모전부터 고르고,\n팀 빌딩을 시작하세요", color = Color.White, fontSize = 30.sp, lineHeight = 38.sp, fontWeight = FontWeight.Black); Spacer(Modifier.height(10.dp)); Text("카드를 누르면 모집글이 아니라 공모전 상세정보가 먼저 표시됩니다.", color = Color(0xFFDCD9F9), fontSize = 12.sp) }
            }
            Text("모집 중인 공모전", Modifier.padding(20.dp, 22.dp, 20.dp, 10.dp), color = Ink, fontSize = 21.sp, fontWeight = FontWeight.Black)
        }
        items(competitions, key = { it.id }) { competition ->
            Card(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp).clickable { onSelect(competition) }, shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(Modifier.padding(17.dp)) { Row(Modifier.fillMaxWidth()) { Text(competition.deadline, color = Indigo, fontSize = 11.sp, fontWeight = FontWeight.Black); Spacer(Modifier.weight(1f)); Text("${competition.teamCount}개 팀", color = Muted, fontSize = 10.sp) }; Spacer(Modifier.height(8.dp)); Text(competition.name, color = Ink, fontSize = 17.sp, fontWeight = FontWeight.Black); Text(competition.organizer, color = Muted, fontSize = 11.sp); Spacer(Modifier.height(10.dp)); TagRow(competition.tags); Spacer(Modifier.height(10.dp)); Text("상세정보 보기 →", color = Indigo, fontSize = 11.sp, fontWeight = FontWeight.Black) }
            }
        }
    }
}

@Composable
private fun CompetitionDetailScreen(competition: Competition?, onOpenTeams: () -> Unit) {
    if (competition == null) return
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(18.dp, 18.dp, 18.dp, 45.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = IndigoDark)) {
                Column(Modifier.padding(22.dp)) { Text(competition.deadline, color = Color(0xFFD7D1FF), fontWeight = FontWeight.Black); Spacer(Modifier.height(14.dp)); Text(competition.name, color = Color.White, fontSize = 25.sp, lineHeight = 32.sp, fontWeight = FontWeight.Black); Spacer(Modifier.height(10.dp)); Text(competition.description, color = Color(0xFFDCD8F6), fontSize = 13.sp, lineHeight = 20.sp); Spacer(Modifier.height(18.dp)); TagRow(competition.tags, dark = true) }
            }
        }
        item { InfoCard("주최기관", competition.organizer); InfoCard("접수 기간", competition.period); InfoCard("참가 대상", competition.eligibility); InfoCard("참가 형태", competition.teamSize); InfoCard("시상·혜택", competition.benefit); InfoCard("진행 장소", competition.location) }
        item { Button(onClick = onOpenTeams, Modifier.fillMaxWidth().height(52.dp), colors = ButtonDefaults.buttonColors(containerColor = Indigo), shape = RoundedCornerShape(13.dp)) { Text("이 공모전의 팀원 모집글 보기", fontWeight = FontWeight.Black) } }
    }
}

@Composable private fun InfoCard(label: String, value: String) { Row(Modifier.fillMaxWidth().padding(vertical = 8.dp)) { Text(label, Modifier.width(84.dp), color = Muted, fontSize = 11.sp); Text(value, Modifier.weight(1f), color = Ink, fontSize = 13.sp, fontWeight = FontWeight.Bold) } }

@Composable
private fun TeamListScreen(state: TeamFitUiState, onSelect: (Team) -> Unit, onRecruit: () -> Unit) {
    val competition = state.selectedCompetition ?: return
    val teams = state.teams.filter { it.competitionId == competition.id }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp, 18.dp, 16.dp, 50.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Text(competition.name, color = Indigo, fontSize = 11.sp, fontWeight = FontWeight.Black)
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { Text("팀원 모집글", Modifier.weight(1f), color = Ink, fontSize = 25.sp, fontWeight = FontWeight.Black); if (state.mode == ActivityMode.LEADER) Button(onClick = onRecruit, colors = ButtonDefaults.buttonColors(containerColor = Indigo)) { Text("＋ 작성") } }
            Text(if (state.mode == ActivityMode.LEADER) "내 모집글을 관리하거나 새 모집글을 작성하세요." else "팀장의 역량과 모집 조건을 비교한 뒤 지원하세요.", color = Muted, fontSize = 12.sp)
        }
        if (teams.isEmpty()) item { EmptyCard("아직 등록된 모집글이 없습니다.", "팀장 모드에서 첫 모집글을 작성해 보세요.") }
        items(teams, key = { it.id }) { team ->
            val mine = team.ownerEmail == state.account?.email
            Card(Modifier.fillMaxWidth().clickable { onSelect(team) }, shape = RoundedCornerShape(17.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(Modifier.padding(17.dp)) { Row { Text(if (mine) "내 모집글" else team.leader, color = if (mine) Indigo else Muted, fontSize = 10.sp, fontWeight = FontWeight.Black); Spacer(Modifier.weight(1f)); Text("${team.applicantIds.size}명 지원", color = Muted, fontSize = 10.sp) }; Spacer(Modifier.height(8.dp)); Text(team.title, color = Ink, fontSize = 17.sp, fontWeight = FontWeight.Black); Text(team.summary, color = Muted, fontSize = 11.sp, maxLines = 2, overflow = TextOverflow.Ellipsis); Spacer(Modifier.height(11.dp)); TagRow(team.requiredSkills); Spacer(Modifier.height(10.dp)); Text("${team.role} ${team.openings}명 · 주 ${team.weeklyHours}시간 · ${team.meetingTimes.firstOrNull()}", color = Ink, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
            }
        }
    }
}

@Composable
private fun TeamDetailScreen(state: TeamFitUiState, viewModel: TeamFitViewModel) {
    val team = state.selectedTeam ?: return
    val mine = team.ownerEmail == state.account?.email
    val applicants = team.applicantIds.mapNotNull { id -> state.candidates.find { it.id == id } }
    val currentCandidate = state.candidates.find { it.email == state.account?.email }
    val invites = state.invitations.filter { it.teamId == team.id }
    val accepted = invites.count { it.status == InviteStatus.ACCEPTED }
    val formed = accepted >= team.openings
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp, 18.dp, 16.dp, 50.dp), verticalArrangement = Arrangement.spacedBy(13.dp)) {
        item {
            Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = IndigoDark)) {
                Column(Modifier.padding(21.dp)) { Text("${team.leader} 팀장", color = Color(0xFFD5CFFF), fontSize = 11.sp, fontWeight = FontWeight.Black); Text(team.title, color = Color.White, fontSize = 24.sp, lineHeight = 31.sp, fontWeight = FontWeight.Black); Spacer(Modifier.height(8.dp)); Text(team.summary, color = Color(0xFFDCD8F6), fontSize = 12.sp, lineHeight = 18.sp) }
            }
        }
        item {
            Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(15.dp)) {
                Column(Modifier.padding(16.dp)) { Text("모집 조건", color = Ink, fontWeight = FontWeight.Black); Spacer(Modifier.height(9.dp)); InfoCard("모집 역할", "${team.role} ${team.openings}명"); InfoCard("필수 기술", team.requiredSkills.joinToString(" · ")); InfoCard("활동 시간", "주 ${team.weeklyHours}시간 · ${team.meetingTimes.firstOrNull()}"); InfoCard("팀 목표", team.goal) }
            }
        }
        if (state.mode == ActivityMode.MEMBER) {
            item {
                LeaderProfileCard(team, state.profiles[team.ownerEmail]) {
                    val profile = state.profiles[team.ownerEmail] ?: UserProfile()
                    viewModel.showProfile(Candidate("leader-${team.id}", team.leader, team.leader.takeLast(2), "violet", profile.desiredRole, profile.skills, profile.weeklyHours, listOf(profile.meetingTime), profile.goal, profile.introduction, emptyList(), team.ownerEmail, team.ownerPhone, profile.competitions))
                }
            }
            if (currentCandidate != null) item { FitCard(team, currentCandidate) }
            item {
                val applied = currentCandidate != null && team.applicantIds.contains(currentCandidate.id)
                Button(onClick = viewModel::applyToSelectedTeam, enabled = !applied, modifier = Modifier.fillMaxWidth().height(51.dp), colors = ButtonDefaults.buttonColors(containerColor = Indigo), shape = RoundedCornerShape(13.dp)) { Text(if (applied) "지원 완료" else "이 팀에 지원하기", fontWeight = FontWeight.Black) }
                if (applied && currentCandidate != null) OutlinedButton(onClick = { viewModel.openChat(currentCandidate) }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) { Text("팀장과 추가 질문 채팅") }
            }
            val myInvite = currentCandidate?.let { candidate -> invites.find { it.candidateId == candidate.id } }
            if (myInvite != null) item {
                Card(colors = CardDefaults.cardColors(containerColor = if (myInvite.status == InviteStatus.PENDING) IndigoSoft else GreenSoft)) {
                    Column(Modifier.padding(16.dp)) { Text("최종 합류 요청", color = Ink, fontWeight = FontWeight.Black); Text(if (myInvite.status == InviteStatus.PENDING) "팀장이 최종 팀원으로 선택했습니다. 수락 여부를 결정해 주세요." else "응답 상태: ${if (myInvite.status == InviteStatus.ACCEPTED) "수락" else "거절"}", color = Muted, fontSize = 12.sp); if (myInvite.status == InviteStatus.PENDING) Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) { OutlinedButton(onClick = { viewModel.respondInvitation(team.id, false) }, Modifier.weight(1f)) { Text("거절") }; Button(onClick = { viewModel.respondInvitation(team.id, true) }, Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Indigo)) { Text("최종 수락") } } }
                }
            }
        } else if (mine) {
            item {
                val pending = invites.count { it.status == InviteStatus.PENDING }
                Card(colors = CardDefaults.cardColors(containerColor = if (formed) GreenSoft else IndigoSoft), shape = RoundedCornerShape(15.dp)) {
                    Column(Modifier.padding(16.dp)) { Text(if (formed) "팀 결성이 완료되었습니다." else if (pending > 0) "팀원들의 최종 수락이 필요합니다." else "지원자를 비교해 최종 팀원을 선택하세요.", color = if (formed) Green else Indigo, fontWeight = FontWeight.Black); Text("수락 $accepted/${team.openings}명 · 응답 대기 ${pending}명", color = Muted, fontSize = 11.sp) }
                }
            }
            item { Text("지원자 ${applicants.size}명", color = Ink, fontSize = 20.sp, fontWeight = FontWeight.Black) }
            if (applicants.isEmpty()) item { EmptyCard("아직 지원자가 없습니다.", "지원자가 들어오면 역량·조건을 비교할 수 있습니다.") }
            items(applicants, key = { it.id }) { candidate ->
                CandidateCard(candidate, team, state.invitations.find { it.teamId == team.id && it.candidateId == candidate.id }, onProfile = { viewModel.showProfile(candidate) }, onChat = { viewModel.openChat(candidate) }, onInvite = { viewModel.inviteCandidate(candidate) })
            }
        } else {
            item { EmptyCard("다른 팀장의 모집글입니다.", "팀원 모드로 전환하면 이 팀에 지원할 수 있습니다.") }
        }
        if (formed) item { ContactCard(team, applicants.filter { candidate -> invites.any { it.candidateId == candidate.id && it.status == InviteStatus.ACCEPTED } }) }
    }
}

@Composable
private fun LeaderProfileCard(team: Team, profile: UserProfile?, onClick: () -> Unit) {
    Card(Modifier.fillMaxWidth().clickable(onClick = onClick), colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(15.dp)) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) { Avatar(team.leader.takeLast(2)); Spacer(Modifier.width(12.dp)); Column(Modifier.weight(1f)) { Text("팀장 ${team.leader}", color = Ink, fontWeight = FontWeight.Black); Text(profile?.desiredRole ?: "프로필 보기", color = Muted, fontSize = 11.sp); Text((profile?.skills ?: emptyList()).joinToString(" · "), color = Indigo, fontSize = 10.sp) }; Text("프로필 →", color = Indigo, fontSize = 10.sp, fontWeight = FontWeight.Black) }
    }
}

@Composable
private fun FitCard(team: Team, candidate: Candidate) {
    val role = candidate.desiredRole == team.role
    val skills = team.requiredSkills.count { candidate.skills.contains(it) }
    val hours = (candidate.weeklyHours ?: 0) >= team.weeklyHours
    Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(15.dp)) { Column(Modifier.padding(16.dp)) { Text("나와 팀 조건 비교", color = Ink, fontWeight = FontWeight.Black); Spacer(Modifier.height(9.dp)); MatchRow("모집 역할", team.role, candidate.desiredRole.orEmpty(), role); MatchRow("필수 기술", team.requiredSkills.joinToString(", "), "${skills}/${team.requiredSkills.size}개 보유", skills == team.requiredSkills.size); MatchRow("주당 활동", "${team.weeklyHours}시간", "${candidate.weeklyHours ?: 0}시간", hours) } }
}

@Composable private fun MatchRow(label: String, expected: String, actual: String, matched: Boolean) { Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) { Column(Modifier.weight(1f)) { Text(label, color = Muted, fontSize = 10.sp); Text("팀 $expected · 나 $actual", color = Ink, fontSize = 11.sp, fontWeight = FontWeight.Bold) }; Text(if (matched) "일치" else "확인 필요", Modifier.background(if (matched) GreenSoft else RoseSoft, RoundedCornerShape(999.dp)).padding(8.dp, 5.dp), color = if (matched) Green else Rose, fontSize = 9.sp, fontWeight = FontWeight.Black) } }

@Composable
private fun CandidateCard(candidate: Candidate, team: Team, invite: com.teamfit.app.data.TeamInvitation?, onProfile: () -> Unit, onChat: () -> Unit, onInvite: () -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.padding(16.dp)) { Row(verticalAlignment = Alignment.CenterVertically) { Avatar(candidate.initials); Spacer(Modifier.width(11.dp)); Column(Modifier.weight(1f)) { Text(candidate.name, color = Ink, fontWeight = FontWeight.Black); Text("${candidate.desiredRole} · 주 ${candidate.weeklyHours ?: 0}시간", color = Muted, fontSize = 10.sp) }; if (invite != null) Text(if (invite.status == InviteStatus.PENDING) "응답 대기" else if (invite.status == InviteStatus.ACCEPTED) "수락" else "거절", color = Indigo, fontSize = 10.sp, fontWeight = FontWeight.Black) }; Spacer(Modifier.height(9.dp)); TagRow(candidate.skills); Spacer(Modifier.height(10.dp)); FitCardCompact(team, candidate); Row(Modifier.fillMaxWidth().padding(top = 10.dp), horizontalArrangement = Arrangement.spacedBy(7.dp)) { OutlinedButton(onClick = onProfile, Modifier.weight(1f)) { Text("프로필", fontSize = 11.sp) }; OutlinedButton(onClick = onChat, Modifier.weight(1f)) { Text("질문", fontSize = 11.sp) }; Button(onClick = onInvite, Modifier.weight(1f), enabled = invite == null, colors = ButtonDefaults.buttonColors(containerColor = Indigo)) { Text("선택", fontSize = 11.sp) } } }
    }
}

@Composable private fun FitCardCompact(team: Team, candidate: Candidate) { val matched = team.requiredSkills.count { candidate.skills.contains(it) } + if ((candidate.weeklyHours ?: 0) >= team.weeklyHours) 1 else 0 + if (candidate.desiredRole == team.role) 1 else 0; Text("조건 일치 $matched/${team.requiredSkills.size + 2} · ${if (candidate.meetingTimes.contains(team.meetingTimes.firstOrNull())) "회의 가능" else "일정 확인 필요"}", color = if (matched >= 2) Green else Rose, fontSize = 10.sp, fontWeight = FontWeight.Black) }

@Composable
private fun ProfileDialog(candidate: Candidate, profile: UserProfile?, onDismiss: () -> Unit) {
    AlertDialog(onDismissRequest = onDismiss, confirmButton = { TextButton(onClick = onDismiss) { Text("닫기") } }, title = { Text("${candidate.name}님의 역량 프로필", fontWeight = FontWeight.Black) }, text = {
        Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) { Text(profile?.introduction ?: candidate.joinMessage, color = Muted, fontSize = 12.sp, lineHeight = 18.sp); TagRow(candidate.skills); ProfileLine("희망 역할", candidate.desiredRole.orEmpty()); ProfileLine("활동 가능", "주 ${candidate.weeklyHours ?: 0}시간 · ${candidate.meetingTimes.firstOrNull()}"); ProfileLine("참여 목표", candidate.goal.orEmpty()); ProfileLine("대표 이력", profile?.experience ?: candidate.experiences.firstOrNull()?.title.orEmpty()); ProfileLine("담당 기여", profile?.contribution ?: candidate.experiences.firstOrNull()?.contribution.orEmpty()); ProfileLine("대회 경험", (profile?.competitions ?: candidate.competitions).joinToString(" · ").ifBlank { "등록된 경험 없음" }); Text("학교·이메일·전화번호는 팀 결성 전 공개하지 않습니다.", Modifier.background(Canvas, RoundedCornerShape(9.dp)).padding(10.dp), color = Muted, fontSize = 10.sp) }
    }, shape = RoundedCornerShape(20.dp), containerColor = Color.White)
}

@Composable private fun ProfileLine(label: String, value: String) { Column { Text(label, color = Muted, fontSize = 9.sp); Text(value.ifBlank { "미등록" }, color = Ink, fontSize = 12.sp, fontWeight = FontWeight.Bold) } }

@Composable
private fun MyPageDialog(account: Account, profile: UserProfile, onDismiss: () -> Unit, onSave: (Account, UserProfile) -> Unit) {
    var name by remember(account.email) { mutableStateOf(account.name) }; var email by remember(account.email) { mutableStateOf(account.email) }; var phone by remember(account.email) { mutableStateOf(account.phone) }
    var role by remember(account.email) { mutableStateOf(profile.desiredRole) }; var skills by remember(account.email) { mutableStateOf(profile.skills.joinToString(", ")) }; var hours by remember(account.email) { mutableStateOf(profile.weeklyHours.toString()) }; var meeting by remember(account.email) { mutableStateOf(profile.meetingTime) }; var goal by remember(account.email) { mutableStateOf(profile.goal) }; var intro by remember(account.email) { mutableStateOf(profile.introduction) }; var experience by remember(account.email) { mutableStateOf(profile.experience) }; var contribution by remember(account.email) { mutableStateOf(profile.contribution) }; var competitions by remember(account.email) { mutableStateOf(profile.competitions.joinToString("\n")) }; var portfolio by remember(account.email) { mutableStateOf(profile.portfolioUrl) }
    AlertDialog(onDismissRequest = onDismiss, confirmButton = { Button(onClick = { onSave(Account(name.trim(), email.trim().lowercase(), phone), UserProfile(role, skills.split(",").map { it.trim() }.filter { it.isNotBlank() }, hours.toIntOrNull() ?: 8, meeting, goal, intro, experience, contribution, competitions.lines().map { it.trim() }.filter { it.isNotBlank() }, portfolio)) }, colors = ButtonDefaults.buttonColors(containerColor = Indigo)) { Text("저장") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("취소") } }, title = { Text("마이페이지", fontWeight = FontWeight.Black) }, text = {
        Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(9.dp)) { Text("개인정보 · 팀 결성 전 비공개", color = Indigo, fontWeight = FontWeight.Black); AppTextField(name, { name = it }, "이름"); AppTextField(email, { email = it }, "이메일", KeyboardType.Email); AppTextField(phone, { phone = formatPhone(it) }, "연락처", KeyboardType.Phone); Text("공개 역량 프로필", color = Indigo, fontWeight = FontWeight.Black); AppTextField(role, { role = it }, "희망 역할"); AppTextField(skills, { skills = it }, "핵심 기술 (쉼표 구분)"); AppTextField(hours, { hours = it.filter(Char::isDigit) }, "주당 활동 시간", KeyboardType.Number); AppTextField(meeting, { meeting = it }, "선호 회의 시간"); AppTextField(goal, { goal = it }, "참여 목표"); AppTextField(intro, { intro = it }, "자기소개", singleLine = false); AppTextField(experience, { experience = it }, "대표 프로젝트·이력"); AppTextField(contribution, { contribution = it }, "담당 역할과 기여", singleLine = false); AppTextField(competitions, { competitions = it }, "대회·공모전 경험", singleLine = false); AppTextField(portfolio, { portfolio = it }, "포트폴리오 URL") }
    }, shape = RoundedCornerShape(20.dp), containerColor = Color.White)
}

@Composable
private fun RecruitDialog(onDismiss: () -> Unit, onSave: (String, String, String, String, Int, Int) -> Unit) {
    var title by remember { mutableStateOf("") }; var summary by remember { mutableStateOf("") }; var role by remember { mutableStateOf("") }; var skills by remember { mutableStateOf("") }; var hours by remember { mutableStateOf("8") }; var openings by remember { mutableStateOf("2") }
    AlertDialog(onDismissRequest = onDismiss, confirmButton = { Button(onClick = { onSave(title, summary, role, skills, hours.toIntOrNull() ?: 8, openings.toIntOrNull() ?: 2) }, colors = ButtonDefaults.buttonColors(containerColor = Indigo)) { Text("등록") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("취소") } }, title = { Text("팀원 모집글 작성", fontWeight = FontWeight.Black) }, text = { Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(9.dp)) { Text("아이디어를 공개하기보다 필요한 역할과 협업 조건을 중심으로 작성하세요.", color = Muted, fontSize = 11.sp); AppTextField(title, { title = it }, "모집글 제목"); AppTextField(summary, { summary = it }, "팀 소개", singleLine = false); AppTextField(role, { role = it }, "모집 역할"); AppTextField(skills, { skills = it }, "필수 기술 (쉼표 구분)"); AppTextField(hours, { hours = it.filter(Char::isDigit) }, "주당 활동 시간", KeyboardType.Number); AppTextField(openings, { openings = it.filter(Char::isDigit) }, "모집 인원", KeyboardType.Number) } }, shape = RoundedCornerShape(20.dp), containerColor = Color.White)
}

@Composable
private fun ChatDialog(state: TeamFitUiState, viewModel: TeamFitViewModel, candidate: Candidate) {
    val team = state.selectedTeam ?: return; var input by remember(candidate.id) { mutableStateOf("") }; var suggestions by remember(candidate.id) { mutableStateOf(state.mode == ActivityMode.LEADER) }
    val thread = state.messages.filter { it.teamId == team.id && it.candidateId == candidate.id }
    AlertDialog(onDismissRequest = viewModel::closeChat, confirmButton = {}, title = { Text(if (state.mode == ActivityMode.LEADER) "${candidate.name}님과 채팅" else "${team.leader} 팀장과 채팅", fontWeight = FontWeight.Black) }, text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            LazyColumn(Modifier.fillMaxWidth().height(250.dp).background(Canvas, RoundedCornerShape(12.dp)).padding(9.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) { items(thread, key = { it.id }) { message -> val mine = message.senderMode == state.mode; Column(Modifier.fillMaxWidth(), horizontalAlignment = if (mine) Alignment.End else Alignment.Start) { Text(message.senderName, color = Muted, fontSize = 9.sp); Text(message.text, Modifier.background(if (mine) Indigo else Color.White, RoundedCornerShape(11.dp)).padding(10.dp), color = if (mine) Color.White else Ink, fontSize = 11.sp) } } }
            if (suggestions && state.mode == ActivityMode.LEADER) { Text("추천 추가 질문", color = Indigo, fontSize = 10.sp, fontWeight = FontWeight.Black); Suggestion("이전 프로젝트에서 직접 담당한 범위와 결과를 구체적으로 알려주실 수 있나요?") { viewModel.sendMessage(it); suggestions = false }; Suggestion("주당 활동 시간과 회의 일정은 어느 정도까지 조율 가능한가요?") { viewModel.sendMessage(it); suggestions = false } }
            AppTextField(input, { input = it; if (it.isNotBlank()) suggestions = false }, "메시지 입력", singleLine = false)
            Button(onClick = { viewModel.sendMessage(input); input = "" }, Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Indigo)) { Text("전송") }
            TextButton(onClick = viewModel::closeChat, Modifier.align(Alignment.End)) { Text("닫기") }
        }
    }, shape = RoundedCornerShape(20.dp), containerColor = Color.White)
}

@Composable private fun Suggestion(text: String, onClick: (String) -> Unit) { Text(text, Modifier.fillMaxWidth().clickable { onClick(text) }.border(1.dp, Line, RoundedCornerShape(9.dp)).padding(10.dp), color = Ink, fontSize = 10.sp) }

@Composable
private fun NotificationsDialog(state: TeamFitUiState, viewModel: TeamFitViewModel, onDismiss: () -> Unit) {
    val candidate = state.candidates.find { it.email == state.account?.email }
    val invitations = if (candidate == null) emptyList() else state.invitations.filter { it.candidateId == candidate.id }
    val incomingMessages = state.messages.filter {
        !state.readMessageIds.contains(it.id) &&
            ((state.mode == ActivityMode.LEADER && it.senderMode == ActivityMode.MEMBER) ||
                (state.mode == ActivityMode.MEMBER && it.senderMode == ActivityMode.LEADER)) &&
            (state.mode == ActivityMode.LEADER || it.candidateId == candidate?.id)
    }.sortedByDescending { it.sentAt }
    AlertDialog(onDismissRequest = onDismiss, confirmButton = { TextButton(onClick = onDismiss) { Text("닫기") } }, title = { Text("알림", fontWeight = FontWeight.Black) }, text = {
        Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(9.dp)) {
            if (incomingMessages.isEmpty() && invitations.isEmpty()) EmptyCard("새로운 알림이 없습니다.", "메시지나 최종 합류 요청이 도착하면 표시됩니다.")
            incomingMessages.forEach { incoming ->
                val team = state.teams.find { it.id == incoming.teamId }
                val chatCandidate = state.candidates.find { it.id == incoming.candidateId }
                Card(colors = CardDefaults.cardColors(containerColor = IndigoSoft)) {
                    Column(Modifier.padding(13.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Text("${incoming.senderName}님이 메시지를 보냈습니다.", color = Ink, fontWeight = FontWeight.Black)
                        team?.let { Text(it.title, color = Indigo, fontSize = 10.sp, fontWeight = FontWeight.Bold) }
                        Text(incoming.text, color = Muted, fontSize = 11.sp, lineHeight = 16.sp)
                        if (chatCandidate != null) {
                            TextButton(onClick = { viewModel.openChat(incoming.teamId, chatCandidate); onDismiss() }) { Text("채팅 열기") }
                        }
                    }
                }
            }
            invitations.forEach { invite -> val team = state.teams.find { it.id == invite.teamId } ?: return@forEach; Card(colors = CardDefaults.cardColors(containerColor = IndigoSoft)) { Column(Modifier.padding(13.dp)) { Text(team.title, color = Ink, fontWeight = FontWeight.Black); Text(if (invite.status == InviteStatus.PENDING) "최종 합류 요청이 도착했습니다." else "응답: ${if (invite.status == InviteStatus.ACCEPTED) "수락" else "거절"}", color = Muted, fontSize = 11.sp); if (invite.status == InviteStatus.PENDING) Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) { OutlinedButton(onClick = { viewModel.respondInvitation(team.id, false) }) { Text("거절") }; Button(onClick = { viewModel.respondInvitation(team.id, true) }, colors = ButtonDefaults.buttonColors(containerColor = Indigo)) { Text("수락") } } } }
            }
        }
    }, shape = RoundedCornerShape(20.dp), containerColor = Color.White)
}

@Composable
private fun ContactCard(team: Team, accepted: List<Candidate>) {
    Card(colors = CardDefaults.cardColors(containerColor = GreenSoft), shape = RoundedCornerShape(16.dp)) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { Text("팀 결성 완료 · 연락처 공개", color = Green, fontWeight = FontWeight.Black); Text("팀장 ${team.leader}\n${team.ownerEmail}\n${team.ownerPhone}", color = Ink, fontSize = 12.sp); accepted.forEach { Text("팀원 ${it.name}\n${it.email}\n${it.phone}", color = Ink, fontSize = 12.sp) } } }
}

@Composable
private fun AdminScreen(state: TeamFitUiState, onRefresh: () -> Unit, onBack: () -> Unit) {
    val verified = state.adminUsers.count { !it.emailConfirmedAt.isNullOrBlank() }
    Scaffold(
        containerColor = Canvas,
        topBar = {
            Surface(color = Color.White, shadowElevation = 2.dp) {
                Row(
                    Modifier.fillMaxWidth().statusBarsPadding().height(62.dp).padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("←", Modifier.clickable(onClick = onBack).padding(8.dp), color = Ink, fontSize = 22.sp, fontWeight = FontWeight.Black)
                    Column(Modifier.weight(1f)) {
                        Text("가입자 관리", color = Ink, fontSize = 18.sp, fontWeight = FontWeight.Black)
                        Text(state.account?.email.orEmpty(), color = Muted, fontSize = 9.sp)
                    }
                    Text("새로고침", Modifier.clickable(onClick = onRefresh).border(1.dp, Line, RoundedCornerShape(9.dp)).padding(10.dp, 7.dp), color = Indigo, fontSize = 10.sp, fontWeight = FontWeight.Black)
                }
            }
        },
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp, 20.dp, 16.dp, 44.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                Text("MEMBER OPERATIONS", color = Indigo, fontSize = 10.sp, fontWeight = FontWeight.Black)
                Spacer(Modifier.height(7.dp))
                Text("Supabase 실제 가입자", color = Ink, fontSize = 25.sp, fontWeight = FontWeight.Black)
                Text("샘플 계정은 제외되며 실제 회원가입 사용자만 표시됩니다.", color = Muted, fontSize = 11.sp)
                Spacer(Modifier.height(14.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                    AdminStat("전체 가입자", state.adminUsers.size.toString(), Modifier.weight(1f))
                    AdminStat("인증 완료", verified.toString(), Modifier.weight(1f))
                }
            }
            state.adminError?.let { error ->
                item { Card(colors = CardDefaults.cardColors(containerColor = RoseSoft), shape = RoundedCornerShape(13.dp)) { Text(error, Modifier.padding(15.dp), color = Rose, fontSize = 12.sp, fontWeight = FontWeight.Bold) } }
            }
            if (state.adminLoading) {
                item { EmptyCard("가입자를 불러오는 중입니다.", "잠시만 기다려 주세요.") }
            } else if (state.adminUsers.isEmpty() && state.adminError == null) {
                item { EmptyCard("표시할 가입자가 없습니다.", "웹 또는 Android 앱에서 실제 회원가입을 진행해 주세요.") }
            } else {
                items(state.adminUsers, key = { it.id }) { user ->
                    Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(15.dp)) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Avatar(user.name.ifBlank { user.email.substringBefore("@") }.takeLast(2))
                                Spacer(Modifier.width(11.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(user.name.ifBlank { "이름 미등록" }, color = Ink, fontWeight = FontWeight.Black)
                                    Text(user.email, color = Muted, fontSize = 10.sp)
                                }
                                Text(
                                    if (user.emailConfirmedAt.isNullOrBlank()) "인증 대기" else "인증 완료",
                                    Modifier.background(if (user.emailConfirmedAt.isNullOrBlank()) RoseSoft else GreenSoft, RoundedCornerShape(999.dp)).padding(8.dp, 5.dp),
                                    color = if (user.emailConfirmedAt.isNullOrBlank()) Rose else Green,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                )
                            }
                            Text("연락처  ${user.phone.ifBlank { "-" }}", color = Ink, fontSize = 11.sp)
                            Text("가입일  ${user.createdAt?.replace("T", " ")?.take(16) ?: "-"}", color = Muted, fontSize = 10.sp)
                            Text("최근 로그인  ${user.lastSignInAt?.replace("T", " ")?.take(16) ?: "-"}", color = Muted, fontSize = 10.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminStat(label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier, colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(14.dp)) {
        Column(Modifier.padding(15.dp)) {
            Text(label, color = Muted, fontSize = 10.sp)
            Text(value, color = Ink, fontSize = 24.sp, fontWeight = FontWeight.Black)
        }
    }
}

@Composable private fun EmptyCard(title: String, body: String) { Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(14.dp)) { Column(Modifier.fillMaxWidth().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) { Text(title, color = Ink, fontWeight = FontWeight.Black); Text(body, color = Muted, fontSize = 11.sp) } } }
@Composable private fun Avatar(text: String) { Box(Modifier.size(42.dp).background(IndigoSoft, CircleShape), contentAlignment = Alignment.Center) { Text(text, color = Indigo, fontWeight = FontWeight.Black) } }
@Composable private fun TagRow(tags: List<String>, dark: Boolean = false) { Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) { tags.forEach { Text(it, Modifier.background(if (dark) Color(0x22FFFFFF) else IndigoSoft, RoundedCornerShape(999.dp)).padding(8.dp, 5.dp), color = if (dark) Color.White else Indigo, fontSize = 9.sp, fontWeight = FontWeight.Bold) } } }
@Composable private fun AppTextField(value: String, onValueChange: (String) -> Unit, label: String, keyboardType: KeyboardType = KeyboardType.Text, singleLine: Boolean = true, isPassword: Boolean = false) { OutlinedTextField(value, onValueChange, label = { Text(label) }, modifier = Modifier.fillMaxWidth(), singleLine = singleLine, minLines = if (singleLine) 1 else 3, keyboardOptions = KeyboardOptions(keyboardType = keyboardType), visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None, shape = RoundedCornerShape(11.dp)) }
private fun formatPhone(value: String): String { val digits = value.filter(Char::isDigit).take(11); return when { digits.length <= 3 -> digits; digits.length <= 7 -> "${digits.take(3)}-${digits.drop(3)}"; else -> "${digits.take(3)}-${digits.drop(3).take(4)}-${digits.drop(7)}" } }
