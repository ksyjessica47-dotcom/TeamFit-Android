package com.teamfit.app.data

object SampleData {
    val accounts = listOf(
        Account("정우진", "demo@teamfit.kr", "010-1234-5678"),
        Account("김민서", "minseo@teamfit.kr", "010-2345-6789"),
        Account("김지우", "jiwoo@teamfit.kr", "010-3456-7891"),
        Account("김하린", "harin@teamfit.kr", "010-4567-8912"),
    )

    val competitions = listOf(
        Competition("public-data", "제14회 범정부 공공데이터·AI 활용 창업경진대회", "행정안전부 · 공공데이터포털", "기관별 상이", "2026.05 – 2026.08", "공공데이터와 AI를 활용해 사회문제를 해결하는 창업 아이디어 및 서비스 경진대회", listOf("공공데이터", "AI", "창업"), "violet", 1, benefit = "대통령상 및 창업 지원"),
        Competition("chai-ai", "2026 CHAI 대학생 AI 광고 공모전", "차이커뮤니케이션", "D-11", "2026.07.01 – 07.31", "AI 기술을 활용한 새로운 광고 아이디어를 제안하는 대학생 공모전", listOf("AI", "광고", "기획"), "blue", 1),
        Competition("ai-city", "2026 AI 도시·지역혁신 아이디어 공모전", "국토교통부 · 충청북도", "D-40", "2026.07.10 – 08.29", "AI로 도시와 지역의 문제를 해결하는 아이디어 공모전", listOf("AI", "도시", "서비스"), "coral", 1),
        Competition("nan-hack", "NHN 게임 × AI 해커톤 NAN 2026", "NHN · 한국콘텐츠진흥원", "D-22", "2026.07.05 – 08.10", "게임과 AI를 결합한 서비스를 짧은 기간 안에 구현하는 해커톤", listOf("게임", "AI", "개발"), "violet", 1),
        Competition("smart-livestock", "제4회 스마트축산 AI 경진대회", "축산물품질평가원", "D-43", "2026.07.15 – 09.01", "축산 데이터를 활용한 AI 분석 및 서비스 개발 경진대회", listOf("데이터", "AI", "농축산"), "blue", 0),
        Competition("display", "2026 디스플레이 챌린지", "산업통상부 · 한국디스플레이산업협회", "D-12", "2026.07.01 – 08.01", "미래 디스플레이 산업을 위한 기술 및 서비스 아이디어 공모전", listOf("기술", "서비스", "아이디어"), "coral", 0),
    )

    val candidates = listOf(
        Candidate("minseo", "김민서", "민서", "violet", "프론트엔드", listOf("React", "TypeScript", "Tailwind CSS"), 8, listOf("평일 저녁"), "수상 및 포트폴리오", "사용자가 바로 이해할 수 있는 화면을 만드는 데 강점이 있습니다.", listOf(Experience("공모전 대시보드 웹", "2026.03 – 2026.06", "프론트엔드", listOf("React", "TypeScript"), "대시보드 UI, 필터와 오류 화면을 직접 구현했습니다.", "https://github.com")), "minseo@teamfit.kr", "010-2345-6789", listOf("교내 서비스 해커톤 우수상")),
        Candidate("jiwoo", "김지우", "지우", "blue", "백엔드", listOf("Spring Boot", "Kotlin", "MySQL"), 10, listOf("평일 저녁"), "실제 서비스 출시", "안정적인 API와 데이터 모델을 설계하는 백엔드 개발자입니다.", listOf(Experience("학사 일정 알림 서비스", "2026.01 – 2026.04", "백엔드", listOf("Spring Boot", "MySQL"), "REST API와 알림 스케줄러를 구현했습니다.", "https://github.com")), "jiwoo@teamfit.kr", "010-3456-7891", listOf("대학생 SW 경진대회 본선")),
        Candidate("harin", "김하린", "하린", "coral", "UI/UX 디자인", listOf("Figma", "Prototyping", "User Research"), 7, listOf("주말 낮"), "프로젝트 경험", "사용자 리서치부터 프로토타입 검증까지 연결합니다.", listOf(Experience("모바일 앱 프로토타입", "2025.09 – 2025.12", "UI/UX 디자인", listOf("Figma"), "사용자 인터뷰와 와이어프레임을 담당했습니다.", null)), "harin@teamfit.kr", "010-4567-8912", listOf("UX 문제해결 해커톤 참가")),
    )

    val teams = listOf(
        Team("team-woojin", "public-data", "공공데이터·AI 서비스 팀원 모집", "공공데이터를 활용해 생활 속 정보 접근 문제를 해결할 팀원을 찾습니다.", "프론트엔드", listOf("React", "TypeScript"), listOf("Figma", "공공데이터 API"), 8, "수상 및 포트폴리오", listOf("평일 저녁"), "정우진", 3, "demo@teamfit.kr", "010-1234-5678", 2, listOf("minseo", "jiwoo", "harin")),
        Team("team-chai", "chai-ai", "AI 광고 기획·디자인 팀원 모집", "광고 아이디어를 빠르게 시각화하고 발표 자료를 함께 완성할 팀원 모집", "UI/UX 디자인", listOf("Figma"), listOf("영상 편집"), 6, "수상", listOf("주말 낮"), "오서연", 2, "seoyeon@teamfit.kr", "010-0000-0000", 2, listOf("harin", "minseo")),
        Team("team-city", "ai-city", "지역문제 데이터 분석 팀", "교통 데이터를 분석해 지역 이동 문제를 해결할 아이디어를 발굴합니다.", "데이터 분석", listOf("Python", "SQL"), listOf("시각화"), 8, "포트폴리오", listOf("협의 가능"), "김도윤", 1, "doyun@teamfit.kr", "010-0000-0000", 1, listOf("jiwoo")),
        Team("team-game", "nan-hack", "AI 게임 프로토타입 개발팀", "짧은 기간에 플레이 가능한 AI 게임 프로토타입을 제작합니다.", "백엔드", listOf("Kotlin", "API"), listOf("Unity"), 12, "출시", listOf("평일 저녁"), "이예진", 1, "yejin@teamfit.kr", "010-0000-0000", 2, listOf("jiwoo")),
    )

    val profiles = candidates.associate { candidate ->
        candidate.email to UserProfile(
            desiredRole = candidate.desiredRole.orEmpty(),
            skills = candidate.skills,
            weeklyHours = candidate.weeklyHours ?: 8,
            meetingTime = candidate.meetingTimes.firstOrNull() ?: "협의 가능",
            goal = candidate.goal.orEmpty(),
            introduction = candidate.joinMessage,
            experience = candidate.experiences.firstOrNull()?.title.orEmpty(),
            contribution = candidate.experiences.firstOrNull()?.contribution.orEmpty(),
            competitions = candidate.competitions,
            portfolioUrl = candidate.experiences.firstOrNull()?.resultUrl.orEmpty(),
        )
    } + ("demo@teamfit.kr" to UserProfile("팀장·풀스택", listOf("Kotlin", "Spring Boot", "기획"), 10, "평일 저녁", "수상 및 포트폴리오", "일정을 투명하게 공유하고 빠르게 실행하는 팀을 만들고 싶습니다.", "대학생 프로젝트 매칭 서비스", "기획, API 설계와 일정 관리를 담당했습니다.", listOf("교내 캡스톤 프로젝트"), "https://github.com"))

    fun teamsFor(competitionId: String) = teams.filter { it.competitionId == competitionId }
    fun candidatesFor(teamId: String) = teams.find { it.id == teamId }?.applicantIds.orEmpty().mapNotNull { id -> candidates.find { it.id == id } }
}
