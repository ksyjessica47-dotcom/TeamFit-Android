package com.teamfit.app.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class TeamFitApi(
    private val baseUrl: String = "https://teamfitv12.vercel.app/api",
) {
    suspend fun signup(name: String, email: String, phone: String, password: String): String = withContext(Dispatchers.IO) {
        val body = JSONObject()
            .put("name", name)
            .put("email", email)
            .put("phone", phone)
            .put("password", password)
        JSONObject(request("/auth/signup", "POST", body.toString()))
            .optString("message", "인증 메일을 보냈습니다. 메일의 링크를 눌러 가입을 완료해 주세요.")
    }

    suspend fun login(email: String, password: String): AuthResult = withContext(Dispatchers.IO) {
        val body = JSONObject().put("email", email).put("password", password)
        val json = JSONObject(request("/auth/login", "POST", body.toString()))
        val user = json.getJSONObject("user")
        AuthResult(
            account = Account(
                id = user.optString("id"),
                name = user.optString("name", user.optString("email").substringBefore("@")),
                email = user.optString("email"),
                phone = user.optString("phone"),
                accessToken = json.optString("accessToken"),
                refreshToken = json.optString("refreshToken"),
                emailConfirmedAt = user.optString("emailConfirmedAt"),
                isAdmin = user.optBoolean("isAdmin", false),
            ),
            expiresIn = json.optInt("expiresIn", 3600),
        )
    }

    suspend fun adminUsers(accessToken: String): List<AdminUser> = withContext(Dispatchers.IO) {
        val json = JSONObject(request("/admin/users", headers = mapOf("Authorization" to "Bearer $accessToken")))
        json.optJSONArray("users")?.mapObjects { user ->
            AdminUser(
                id = user.optString("id"),
                email = user.optString("email"),
                name = user.optString("name"),
                phone = user.optString("phone"),
                emailConfirmedAt = user.nullableString("emailConfirmedAt"),
                createdAt = user.nullableString("createdAt"),
                lastSignInAt = user.nullableString("lastSignInAt"),
            )
        }.orEmpty()
    }

    suspend fun competitions(): List<Competition> = withContext(Dispatchers.IO) {
        request("/competitions").asArray().mapObjects(::competition)
    }

    suspend fun teams(competitionId: String): List<Team> = withContext(Dispatchers.IO) {
        request("/competitions/$competitionId/teams").asArray().mapObjects(::team)
    }

    suspend fun candidates(teamId: String): List<Candidate> = withContext(Dispatchers.IO) {
        request("/teams/$teamId/candidates").asArray().mapObjects(::candidate)
    }

    suspend fun saveDecision(draft: DecisionDraft): SavedDecision = withContext(Dispatchers.IO) {
        val body = JSONObject()
            .put("teamId", draft.teamId)
            .put("decision", draft.decision)
            .put("candidateId", draft.candidateId ?: JSONObject.NULL)
            .put("importantFactor", draft.importantFactor)
            .put("reason", draft.reason)
        decision(JSONObject(request("/decisions", "POST", body.toString())))
    }

    private fun request(
        path: String,
        method: String = "GET",
        body: String? = null,
        headers: Map<String, String> = emptyMap(),
    ): String {
        val connection = URL(baseUrl + path).openConnection() as HttpURLConnection
        return try {
            connection.requestMethod = method
            connection.connectTimeout = 4_000
            connection.readTimeout = 4_000
            connection.setRequestProperty("Accept", "application/json")
            headers.forEach(connection::setRequestProperty)
            if (body != null) {
                connection.doOutput = true
                connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
                connection.outputStream.bufferedWriter(Charsets.UTF_8).use { it.write(body) }
            }
            val status = connection.responseCode
            val stream = if (status in 200..299) connection.inputStream else connection.errorStream
            val text = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
            if (status !in 200..299) {
                val message = runCatching { JSONObject(text).optString("error") }.getOrNull()
                    ?.takeIf { it.isNotBlank() }
                    ?: "서버 요청에 실패했습니다. ($status)"
                throw TeamFitApiException(message, status)
            }
            text
        } finally {
            connection.disconnect()
        }
    }

    private fun String.asArray() = JSONArray(this)

    private fun <T> JSONArray.mapObjects(transform: (JSONObject) -> T): List<T> =
        (0 until length()).map { transform(getJSONObject(it)) }

    private fun JSONArray.strings(): List<String> = (0 until length()).map { getString(it) }

    private fun competition(json: JSONObject) = Competition(
        id = json.getString("id"),
        name = json.getString("name"),
        organizer = json.getString("organizer"),
        deadline = json.getString("deadline"),
        period = json.getString("period"),
        description = json.getString("description"),
        tags = json.getJSONArray("tags").strings(),
        accent = json.getString("accent"),
        teamCount = json.getInt("teamCount"),
    )

    private fun team(json: JSONObject) = Team(
        id = json.getString("id"),
        competitionId = json.getString("competitionId"),
        title = json.getString("title"),
        summary = json.getString("summary"),
        role = json.getString("role"),
        requiredSkills = json.getJSONArray("requiredSkills").strings(),
        preferredSkills = json.getJSONArray("preferredSkills").strings(),
        weeklyHours = json.getInt("weeklyHours"),
        goal = json.getString("goal"),
        meetingTimes = json.getJSONArray("meetingTimes").strings(),
        leader = json.getString("leader"),
        candidateCount = json.getInt("candidateCount"),
    )

    private fun candidate(json: JSONObject) = Candidate(
        id = json.getString("id"),
        name = json.getString("name"),
        initials = json.getString("initials"),
        accent = json.getString("accent"),
        desiredRole = json.nullableString("desiredRole"),
        skills = json.getJSONArray("skills").strings(),
        weeklyHours = if (json.isNull("weeklyHours")) null else json.getInt("weeklyHours"),
        meetingTimes = json.getJSONArray("meetingTimes").strings(),
        goal = json.nullableString("goal"),
        joinMessage = json.getString("joinMessage"),
        experiences = json.getJSONArray("experiences").mapObjects(::experience),
    )

    private fun experience(json: JSONObject) = Experience(
        title = json.getString("title"),
        period = json.getString("period"),
        role = json.nullableString("role"),
        skills = json.getJSONArray("skills").strings(),
        contribution = json.getString("contribution"),
        resultUrl = json.nullableString("resultUrl"),
    )

    private fun decision(json: JSONObject) = SavedDecision(
        teamId = json.getString("teamId"),
        decision = json.getString("decision"),
        candidateId = json.nullableString("candidateId"),
        importantFactor = json.getString("importantFactor"),
        reason = json.getString("reason"),
        selectedAt = json.getString("selectedAt"),
    )

    private fun JSONObject.nullableString(key: String): String? =
        if (!has(key) || isNull(key)) null else getString(key)
}

class TeamFitApiException(message: String, val statusCode: Int) : Exception(message)
